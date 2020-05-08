 package com.aptana.rdt.internal.core.gems;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.eclipse.core.internal.resources.XMLWriter;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
 import org.rubypeople.rdt.launching.IVMInstall;
 import org.rubypeople.rdt.launching.RubyRuntime;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import com.aptana.rdt.AptanaRDTPlugin;
 import com.aptana.rdt.core.gems.Gem;
 import com.aptana.rdt.core.gems.GemListener;
 import com.aptana.rdt.core.gems.IGemManager;
 import com.aptana.rdt.ui.gems.GemsMessages;
 import com.aptana.rdt.ui.gems.RemoveGemDialog;
 
 public class GemManager implements IGemManager {
 
 	private static final String LOCAL_SWITCH = "-l";
 	private static final String LIST_COMMAND = "list";
 	private static final String INSTALL_COMMAND = "install";
 	private static final String VERSION_SWITCH = "-v";
 	private static final String UNINSTALL_COMMAND = "uninstall";
 	private static final String UPDATE_COMMAND = "update";
 	private static final String EXECUTABLE = "ruby";
 
 	private static final String VM_ARGS = "-e STDOUT.sync=true -e STDERR.sync=true -e load(ARGV.shift)";
 
 	private static final String REMOTE_GEMS_CACHE_FILE = "remote_gems.xml";
 	private static final String LOCAL_GEMS_CACHE_FILE = "local_gems.xml";
 
 	private static final String GEM_INDEX_URL = "http://gems.rubyforge.org/yaml.Z";
 
 	private static GemManager fgInstance;
 
 	private Set<Gem> gems;
 	private Set<Gem> remoteGems;
 	private Set<GemListener> listeners;
 	private IPath fGemInstallPath;
 	
 	protected boolean isInitialized;
 
 	private GemManager() {
 		gems = new HashSet<Gem>();
 		// FIXME Somehow allow user to refresh remote gem list
 		// FIXME Do an incremental check for new remote gems somehow?
 		remoteGems = new HashSet<Gem>();
 		listeners = new HashSet<GemListener>();
 	}
 	
 	public boolean isInitialized() {
 		return isInitialized;
 	}
 
 	protected Set<Gem> loadLocalCache(File file) {
 		FileReader fileReader = null;
 		try {
 			fileReader = new FileReader(file);
 			XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
 					.getXMLReader();
 			GemManagerContentHandler handler = new GemManagerContentHandler();
 			reader.setContentHandler(handler);
 			reader.parse(new InputSource(fileReader));
 
 			return handler.getGems();
 		} catch (FileNotFoundException e) {
 			// This is okay, will get thrown if no config exists yet
 		} catch (SAXException e) {
 			AptanaRDTPlugin.log(e);
 		} catch (ParserConfigurationException e) {
 			AptanaRDTPlugin.log(e);
 		} catch (FactoryConfigurationError e) {
 			AptanaRDTPlugin.log(e);
 		} catch (IOException e) {
 			AptanaRDTPlugin.log(e);
 		} finally {
 			try {
 				if (fileReader != null)
 					fileReader.close();
 			} catch (IOException e) {
 				// ignore
 			}
 		}
 		return new HashSet<Gem>();
 	}
 
 	protected void storeGemCache(Set<Gem> gems, File file) {
 		XMLWriter out = null;
 		try {
 			out = new XMLWriter(new FileOutputStream(file));
 			writeXML(gems, out);
 		} catch (FileNotFoundException e) {
 			AptanaRDTPlugin.log(e);
 		} catch (IOException e) {
 			AptanaRDTPlugin.log(e);
 		} finally {
 			if (out != null)
 				out.close();
 		}
 	}
 
 	private File getConfigFile(String fileName) {
 		return AptanaRDTPlugin.getDefault().getStateLocation().append(fileName)
 				.toFile();
 	}
 
 	/**
 	 * Writes each server configuration to file in XML format.
 	 * 
 	 * @param gems
 	 * 
 	 * @param out
 	 *            the writer to use
 	 */
 	private void writeXML(Set<Gem> gems, XMLWriter out) {
 		out.startTag("gems", null);
 		for (Gem gem : gems) {
 			out.startTag("gem", null);
 			out.printSimpleTag("name", gem.getName());
 			out.printSimpleTag("version", gem.getVersion());
 			out.printSimpleTag("description", gem.getDescription());
 			out.printSimpleTag("platform", gem.getPlatform());
 			out.endTag("gem");
 		}
 		out.endTag("gems");
 		out.flush();
 	}
 
 	private Set<Gem> loadRemoteGems() {
 
 		try {
 			List<String> lines = new ArrayList<String>();
 			try {
 				lines = getContents();
 			} catch (DataFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return convertToGems(lines);
 		} catch (MalformedURLException e) {
 			AptanaRDTPlugin.log(e);
 		} catch (IOException e) {
 			AptanaRDTPlugin.log(e);
 		}
 		return new HashSet<Gem>();
 	}
 
 	private Set<Gem> convertToGems(List<String> lines) {
 		Set<Gem> gems = new HashSet<Gem>();
 		String name = null;
 		String version = null;
 		String description = null;
 		String platform = null;
 		boolean nextIsRealVersion = false;
 		for (String line : lines) {
 			if (nextIsRealVersion && line.trim().startsWith("version: ")) {
 				version = line.trim().substring(9);
 				if (version.charAt(0) == '"')
 					version = version.substring(1);
 				if (version.charAt(version.length() - 1) == '"')
 					version = version.substring(0, version.length() - 1);
 				nextIsRealVersion = false;
 			} else if (line.trim().equals("version: !ruby/object:Gem::Version")) {
 				nextIsRealVersion = true;
 			}
 			if (line.trim().startsWith("name:")) {
 				name = line.trim().substring(6);
 			}
 			if (line.trim().startsWith("platform:")) {
 				if (line.trim().length() == 9) {
 					platform = Gem.RUBY_PLATFORM;
 				} else {
 					platform = line.trim().substring(10);
 				}
 			}
 			if (line.trim().startsWith("summary:")) {
 				description = line.trim().substring(9);
 			}
 			if (description != null && name != null && version != null
 					&& platform != null) {
 				gems.add(new Gem(name, version, description, platform));
 				description = null;
 				version = null;
 				name = null;
 				platform = null;
 			}
 		}
 		return gems;
 	}
 
 	private List<String> getContents() throws MalformedURLException,
 			IOException, DataFormatException {
 		String outputString = decompress(getZippedGemIndex());		
 		String[] lineArray = outputString.split("\n");
 		return Arrays.asList(lineArray);
 	}
 
 	private byte[] getZippedGemIndex() throws MalformedURLException, IOException {
 		URL url = new URL(GEM_INDEX_URL);
 		URLConnection con = url.openConnection();
 		InputStream content = (InputStream) con.getContent();
 		byte[] input = new byte[1024];
 		int index = 0;
 		while (true) {
 			int bytesToRead = content.available();
 			byte[] tmp = new byte[bytesToRead];
 			int length = content.read(tmp);
 			if (length == -1)
 				break;
 			while ((index + length) > input.length) { // if we'll overflow the
 														// array, we need to
 														// expand it
 				byte[] newInput = new byte[input.length * 2];
 				System.arraycopy(input, 0, newInput, 0, input.length);
 				input = newInput;
 			}
 			System.arraycopy(tmp, 0, input, index, length);
 			index += length;
 		}
 		// Strip byte array down to just length of the content we actually read in.
 		byte[] newInput = new byte[index];
 		System.arraycopy(input, 0, newInput, 0, index);
 		return newInput;
 	}
 
 	private String decompress(byte[] input) throws DataFormatException {
 		// Decompress the bytes
 		Inflater decompresser = new Inflater();
 		decompresser.setInput(input);
 		byte[] result = new byte[input.length * 10]; // XXX This is a hack. I
 														// have no idea what the
 														// length should be here
 		int resultLength = decompresser.inflate(result);
 		decompresser.end();
 
 		// Decode the bytes into a String
 		return new String(result, 0, resultLength);
 	}
 
 	private Set<Gem> loadLocalGems() {
 		ILaunchConfiguration config = createGemLaunchConfiguration(LIST_COMMAND + " " + LOCAL_SWITCH, false);
 		List<String> lines = readOutput(config);
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < lines.size(); i++) {
 			buffer.append(lines.get(i));
 			if (i < (lines.size() - 1)) buffer.append("\n");
 		}
 		GemParser parser = new GemParser();
 		return parser.parse(buffer.toString());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#update(com.aptana.rdt.internal.gems.Gem)
 	 */
 	public boolean update(final Gem gem) {
 		try {
 			String command = UPDATE_COMMAND + " " + gem.getName();
 			ILaunchConfiguration config = createGemLaunchConfiguration(command, true);
 			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
			Job job = new Job("Updating gem " + gem.getName()) {
 			
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					while (!launch.isTerminated()) {
 						Thread.yield();
 					}
 					refresh();
 					return Status.OK_STATUS;
 				}
 			
 			};
 			job.schedule();			
 		} catch (CoreException e) {
 			AptanaRDTPlugin.log(e);
 			return false;
 		}
 		return true;
 	}
 
 	private ILaunchConfigurationType getRubyApplicationConfigType() {
 		return getLaunchManager().getLaunchConfigurationType(
 				IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
 	}
 
 	private ILaunchManager getLaunchManager() {
 		return DebugPlugin.getDefault().getLaunchManager();
 	}
 
 	private ILaunchConfiguration createGemLaunchConfiguration(String arguments, boolean interactive) {
 		String gemPath = getGemScriptPath();
 		ILaunchConfiguration config = null;
 		try {
 			ILaunchConfigurationType configType = getRubyApplicationConfigType();
 			ILaunchConfigurationWorkingCopy wc = configType
 					.newInstance(null, getLaunchManager()
 							.generateUniqueLaunchConfigurationNameFrom(gemPath));
 			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME,
 					gemPath);
 			wc.setAttribute(
 					IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,
 					RubyRuntime.getDefaultVMInstall().getName());
 			wc.setAttribute(
 					IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE,
 					RubyRuntime.getDefaultVMInstall().getVMInstallType()
 							.getId());
 			wc.setAttribute(
 					IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
 					arguments);
 			wc.setAttribute(
 					IRubyLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
 					VM_ARGS);
 			Map<String, String> map = new HashMap<String, String>();
 			map.put(IRubyLaunchConfigurationConstants.ATTR_RUBY_COMMAND,
 					EXECUTABLE);
 			wc
 					.setAttribute(
 							IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP,
 							map);
 			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
 			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND,
 					!interactive);
 			if (!interactive) {
 				wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE,
 						false);
 				IPath outFilePath = AptanaRDTPlugin.getDefault()
 						.getStateLocation();
 				outFilePath = outFilePath.append(System.currentTimeMillis()
 						+ ".txt");
 				wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE,
 						outFilePath.toPortableString());
 			}
 			config = wc.doSave();
 		} catch (CoreException ce) {
 			// ignore for now
 		}
 		return config;
 	}
 
 	private static String getGemScriptPath() {
 		IVMInstall vm = RubyRuntime.getDefaultVMInstall();
 		File installLocation = vm.getInstallLocation();
 		String path = installLocation.getAbsolutePath();
 		return path + File.separator + "bin" + File.separator + "gem";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#installGem(com.aptana.rdt.internal.gems.Gem)
 	 */
 	public boolean installGem(final Gem gem) {
 		try {
 			String command = INSTALL_COMMAND + " " + gem.getName();
 			if (gem.getVersion() != null
 					&& gem.getVersion().trim().length() > 0) {
 				command += " " + VERSION_SWITCH + " " + gem.getVersion();
 			}
 			ILaunchConfiguration config = createGemLaunchConfiguration(command, true);
 			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);	
			Job job = new Job("Installing gem " + gem.getName()) {
 			
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					while (!launch.isTerminated()) {
 						Thread.yield();
 					}
 					refresh();
 					// Need to wait until uninstall is finished
 					for (GemListener listener : listeners) {
 						listener.gemAdded(gem);
 					} 
 					return Status.OK_STATUS;
 				}
 			
 			};
 			job.schedule();		
 		} catch (CoreException e) {
 			AptanaRDTPlugin.log(e);
 			return false;
 		}
 		for (GemListener listener : listeners) {
 			listener.gemAdded(gem);
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#removeGem(com.aptana.rdt.internal.gems.Gem)
 	 */
 	public boolean removeGem(final Gem gem) {
 		if (gem.hasMultipleVersions()) {
 			RemoveGemDialog dialog = new RemoveGemDialog(Display.getDefault().getActiveShell(), gem.versions());
 			if (dialog.open() == Dialog.OK) {
 				return removeGem(new Gem(gem.getName(), dialog.getVersion(), null));
 			} else {
 				return false;
 			}
 		}
 		try {
 			String command = UNINSTALL_COMMAND + " " + gem.getName();			
 			if (gem.getVersion() != null
 					&& gem.getVersion().trim().length() > 0) {
 				command += " " + VERSION_SWITCH + " " + gem.getVersion();
 			}
 			ILaunchConfiguration config = createGemLaunchConfiguration(command, true);
 			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
			Job job = new Job("Notifying gem listeners of uninstalled gem") {
 			
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					while (!launch.isTerminated()) {
 						Thread.yield();
 					}
 					refresh();
 					// Need to wait until uninstall is finished
 					for (GemListener listener : listeners) {
 						listener.gemRemoved(gem);
 					} 
 					return Status.OK_STATUS;
 				}
 			
 			};
 			job.schedule();			
 		} catch (CoreException e) {
 			AptanaRDTPlugin.log(e);
 			return false;
 		}
 		
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#getGems()
 	 */
 	public Set<Gem> getGems() {
 		return Collections.unmodifiableSortedSet(new TreeSet<Gem>(gems));
 	}
 
 	public static GemManager getInstance() {
 		if (fgInstance == null)
 			fgInstance = new GemManager();
 		return fgInstance;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#refresh()
 	 */
 	public boolean refresh() {
 		Set<Gem> newGems = loadLocalGems();
 		if (!newGems.isEmpty()) {
 			gems = newGems;
 			storeGemCache(gems, getConfigFile(LOCAL_GEMS_CACHE_FILE));
 			for (GemListener listener : listeners) {
 				listener.gemsRefreshed();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#addGemListener(com.aptana.rdt.internal.gems.GemManager.GemListener)
 	 */
 	public synchronized void addGemListener(GemListener listener) {
 		listeners.add(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#getRemoteGems()
 	 */
 	public Set<Gem> getRemoteGems() {
 		SortedSet<Gem> sorted = new TreeSet<Gem>(remoteGems);
 		SortedSet<Gem> logical = new TreeSet<Gem>();
 		String name = null;
 		Collection<Gem> temp = new HashSet<Gem>();
 		for (Gem gem : sorted) {
 			if (name != null && !gem.getName().equals(name)) {
 				logical.add(LogicalGem.create(temp));
 				temp.clear();
 			}
 			name = gem.getName();
 			temp.add(gem);
 		}
 		return Collections.unmodifiableSortedSet(logical);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#gemInstalled(java.lang.String)
 	 */
 	public boolean gemInstalled(String gemName) {
 		Set<Gem> gems = getGems();
 		for (Gem gem : gems) {
 			if (gem.getName().equalsIgnoreCase(gemName))
 				return true;
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aptana.rdt.internal.gems.IGemManager#removeGemListener(com.aptana.rdt.internal.gems.GemManager.GemListener)
 	 */
 	public synchronized void removeGemListener(GemListener listener) {
 		listeners.remove(listener);
 	}
 
 	public IPath getGemInstallPath() {
 		if (fGemInstallPath == null) {
 			ILaunchConfiguration config = createGemLaunchConfiguration("environment", false);
 			List<String> lines = readOutput(config);
 			if (lines == null || lines.size() < 3) return null;
 			String path = lines.get(2);
 			path = path.substring(path.indexOf("INSTALLATION DIRECTORY:") + 23);
 			fGemInstallPath = new Path(path.trim());
 		}
 		return fGemInstallPath;
 	}
 
 	private List<String> readOutput(ILaunchConfiguration config) {
 		List<String> lines = new ArrayList<String>();
 		File file = null;
 		BufferedReader reader = null;
 		try {
 			ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
 			IProcess[] processes = launch.getProcesses();
 			IProcess p = processes[0];
 			while (!p.isTerminated()) {
 				Thread.yield();
 			}
 			file = new File(config.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String) null));
 			reader = new BufferedReader(new FileReader(file));			
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				lines.add(line);
 			}
 		} catch(CoreException e) {
 			AptanaRDTPlugin.log(e);
 		} catch (FileNotFoundException e) {
 			AptanaRDTPlugin.log(e);
 		} catch (IOException e) {
 			AptanaRDTPlugin.log(e);
 		} finally {
 			try {
 				if (reader != null) reader.close();
 			} catch (IOException e) {
 				// ignore
 			}
 			if (file != null) {
 				file.delete();
 			}
 		}
 		return lines;
 	}
 
 	public IPath getGemPath(String gemName) {
 		IPath path = getGemInstallPath();
 		path = path.append("gems");
 		File gemFolder = path.toFile();
 		File[] gems = gemFolder.listFiles();
 		for (int i = 0; i < gems.length; i++) {
 			if (gems[i].getName().startsWith(gemName)) 
 				return new Path(gems[i].getAbsolutePath()).append("lib");
 		}
 		return null;
 	}
 	
 	public IPath getGemPath(String gemName, String version) {
 		return getGemPath(gemName + "-" + version);
 	}
 
 	public boolean updateAll() {
 		try {
 			ILaunchConfiguration config = createGemLaunchConfiguration(UPDATE_COMMAND, true);
 			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
			Job job = new Job("Updating gem listing") {
 			
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					while (!launch.isTerminated()) {
 						Thread.yield();
 					}
 					refresh();
 					return Status.OK_STATUS;
 				}
 			
 			};
 			job.schedule();			
 		} catch (CoreException e) {
 			AptanaRDTPlugin.log(e);
 			return false;
 		}
 		return true;
 	}
 
 	public void initialize() {
 		scheduleLoadingRemoteGems();
 		scheduleLoadingLocalGems();		
 	}
 
 	private void scheduleLoadingLocalGems() {
 		Job job2 = new Job(GemsMessages.GemManager_loading_local_gems) {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					gems = loadLocalCache(getConfigFile(LOCAL_GEMS_CACHE_FILE));
 					if (gems.isEmpty()) {
 						gems = loadLocalGems();
 						storeGemCache(gems, getConfigFile(LOCAL_GEMS_CACHE_FILE));
 					}
 					isInitialized = true;
 					synchronized (listeners) {
 						for (GemListener listener : new ArrayList<GemListener>(listeners)) {
 							listener.managerInitialized();
 						}
 					}
 					synchronized (listeners) {
 						for (GemListener listener : new ArrayList<GemListener>(listeners)) {
 							listener.gemsRefreshed();
 						}
 					}
 				} catch (Exception e) {
 					AptanaRDTPlugin.log(e);
 					return Status.CANCEL_STATUS;
 				}
 				return Status.OK_STATUS;
 			}
 
 		};
 		job2.schedule();
 	}
 
 	private void scheduleLoadingRemoteGems() {
 		Job job = new Job(GemsMessages.GemManager_loading_remote_gems) {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					remoteGems = loadLocalCache(getConfigFile(REMOTE_GEMS_CACHE_FILE));
 					if (remoteGems.isEmpty()) {
 						remoteGems = loadRemoteGems();
 						storeGemCache(remoteGems,
 								getConfigFile(REMOTE_GEMS_CACHE_FILE));
 					}
 				} catch (Exception e) {
 					AptanaRDTPlugin.log(e);
 					return Status.CANCEL_STATUS;
 				}
 				return Status.OK_STATUS;
 			}
 
 		};
 		job.schedule();
 	}
 }
