 package grisu.frontend.view.swing.utils.ssh.wizard;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.frontend.control.clientexceptions.FileTransactionException;
 import grisu.frontend.control.login.LoginManager;
 import grisu.jcommons.view.html.VelocityUtils;
 import grisu.model.FileManager;
 import grisu.model.GrisuRegistryManager;
 import grisu.model.MountPoint;
 import grisu.model.UserEnvironmentManager;
 import grisu.model.dto.DtoMountPoints;
 import grisu.model.dto.GridFile;
 import grisu.model.info.dto.Directory;
 import grisu.model.info.dto.Queue;
 import grith.jgrith.cred.AbstractCred.PROPERTY;
 import grith.jgrith.cred.Cred;
 import grith.jgrith.cred.SLCSCred;
 import grith.jgrith.utils.GridSshKey;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Point;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.ciscavate.cjwizard.WizardContainer;
 import org.ciscavate.cjwizard.WizardListener;
 import org.ciscavate.cjwizard.WizardPage;
 import org.ciscavate.cjwizard.WizardSettings;
 import org.ciscavate.cjwizard.pagetemplates.DefaultPageTemplate;
 import org.ciscavate.cjwizard.pagetemplates.PageTemplate;
 import org.ciscavate.cjwizard.pagetemplates.TitledPageTemplate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.jgoodies.common.base.SystemUtils;
 import com.jgoodies.looks.Options;
 import com.kenai.jaffl.annotations.Direct;
 
 public class NesiSetupWizard extends JFrame implements WizardListener,
 		PropertyChangeListener {
 
 //	public static final ImmutableSet<String> SITES = ImmutableSet.of(
 //			"auckland", "canterbury");
 //	public static final ImmutableSet<String> HOSTS = ImmutableSet
 //			.of("pan.nesi.org.nz", "gram5p7.canterbury.ac.nz");
 	public static final ImmutableSet<String> SITES = ImmutableSet.of(
 			"auckland");
 	public static final ImmutableSet<String> HOSTS = ImmutableSet
 			.of("pan.nesi.org.nz");
 	
 	public static final ImmutableMap<String, String> BOOKMARK_NAMES = ImmutableMap.of("gsiftp://pan.nesi.org.nz/~/", "Pan", "gsiftp://gram5p7.canterbury.ac.nz/~/", "Power 7 (SUSE)"); 
 	public static final ImmutableMap<String, String> BOOKMARK_HOSTS = ImmutableMap.of("gsiftp://pan.nesi.org.nz/~/", "login.uoa.nesi.org.nz", "gsiftp://gram5p7.canterbury.ac.nz/~/", "beatrice.canterbury.ac.nz"); 
 
 	public static final Logger myLogger = LoggerFactory
 			.getLogger(NesiSetupWizard.class);
 
 	public static final String NO_FILE = "NO_FILE";
 
 	public static final String GRISU_BACKEND = "bestgrid";
 	
 	private boolean nextPressedAtLeastOnce = false;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 
 		LoginManager.initGrisuClient("nesi-setup");
 		
 		try {
 			myLogger.debug("Setting look and feel.");
 
 			UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
 			Options.setDefaultIconSize(new Dimension(18, 18));
 
 			String lafName = null;
 			if (SystemUtils.IS_OS_WINDOWS) {
 				lafName = Options.JGOODIES_WINDOWS_NAME;
 			} else {
 				lafName = UIManager.getSystemLookAndFeelClassName();
 			}
 
 			try {
 				myLogger.debug("Look and feel name:" + lafName);
 				UIManager.setLookAndFeel(lafName);
 			} catch (Exception e) {
 				System.err.println("Can't set look & feel:" + e);
 			}
 
 			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (final Exception e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 		}
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 
 					NesiSetupWizard frame = new NesiSetupWizard();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	private JPanel contentPane;
 
 	private final WizardContainer wizard;
 
 	private final NesiSetupWizardPageFactory pageFactory;
 	private Cred credential;
 	private ServiceInterface si = null;
 	private FileManager fm = null;
 
 	private UserEnvironmentManager uem = null;
 	private GridSshKey sshkey;
 	private boolean forceCreateNewKey = false;
 
 	private Set<String> sites;
 
 	private Map<PROPERTY, Object> credconfig;
 	private final Map<String, File> sshAuthFilesMap = Maps.newConcurrentMap();
 
 	private final Map<String, String> sshUsernameMap = Maps.newConcurrentMap();
 
 	private List<MountPoint> mountpoints = null;
 	
 	private boolean createMobaConfig = false;
 	private boolean createSshConfig = false;
 	
 	private Set<SshBookmark> bookmarks = Sets.newTreeSet();
 
 
 	/**
 	 * Create the frame.
 	 */
 	public NesiSetupWizard() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 680, 507);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 
 		pageFactory = new NesiSetupWizardPageFactory(this);
 		PageTemplate pt = new DefaultPageTemplate();
 		wizard = new WizardContainer(pageFactory, pt);
 		wizard.setNextEnabled(false);
 		wizard.addWizardListener(this);
 		getContentPane().add(wizard);
 	}
 
 	private void copySshKey(WizardSSHCopyProgressPage progressPage,
 			boolean forceCreateNewKey) throws Exception {
 
 		progressPage.setLoginStarted();
 
 		try {
 
 			if (sshkey.exists() && !forceCreateNewKey) {
 				progressPage.addMessage("Using existing ssh key '"
 						+ sshkey.getKeyPath() + "'");
 			} else {
 				if (sshkey.exists() && forceCreateNewKey) {
 					progressPage.addMessage("Overwriting existing ssh key '"
 							+ sshkey.getKeyPath() + "'");
 					FileUtils.deleteQuietly(new File(sshkey.getCertPath()));
 					FileUtils.deleteQuietly(new File(sshkey.getKeyPath()));
 				} else {
 					progressPage.addMessage("Creating new ssh key: "
 							+ sshkey.getKeyPath());
 				}
 				try {
 					if (sshkey.getPassword() == null
 							|| sshkey.getPassword().length == 0) {
 						sshkey.setPassword((char[]) credconfig
 								.get(PROPERTY.Password));
 					}
 					sshkey.createIfNecessary();
 
 				} catch (Exception e) {
 					progressPage.addMessage("Could not create ssh key: "
 							+ e.getLocalizedMessage());
 					throw e;
 				}
 			}
 
 			String publicKey = sshkey.getCertPath();
 
 			String pubContent = FileUtils.readFileToString(new File(publicKey));
 			// progressPage.addMessage("\n\t" + pubContent + "\n");
 
 			for (String site : sites) {
 				copyToSite(site, progressPage);
 			}
 
 		} finally {
 			progressPage.setLoginFinished();
 		}
 
 	}
 
 	private void copyToSite(String site, LogRenderer r) {
 
 		Set<Directory> dirs = getDirectoriesForSite(site);
 
 		Set<String> urls = new HashSet<String>();
 		for (Directory d : dirs) {
 			urls.add(d.toUrl());
 		}
 
 		for (String url : urls) {
 			boolean useUrl = false;
 			for (String allowed_host : HOSTS) {
 				if (url.contains(allowed_host)) {
 					useUrl = true;
 					break;
 				}
 			}
 			if (useUrl) {
 				ensureKeyIsPresent(url, r);
 			}
 
 		}
 
 	}
 
 	private void ensureKeyIsPresent(String baseUrl, LogRenderer r) {
 
 		r.addMessage("FileSystem: " + baseUrl);
 		File file = getSshAuthorizedKeysForDirectory(baseUrl);
 		String content;
 		try {
 			content = FileUtils.readFileToString(file);
 		} catch (IOException e1) {
 			throw new RuntimeException(e1);
 		}
 		String pubKey;
 		try {
 			pubKey = FileUtils.readFileToString(new File(sshkey.getCertPath()));
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		if (content.contains(pubKey)) {
 			r.addMessage("\tKey already present");
 		} else {
 			r.addMessage("\tAdding public key...");
 			try {
 				FileUtils.writeStringToFile(file, pubKey, true);
 
 				fm.uploadFile(file, baseUrl + ".ssh/authorized_keys", true);
 				r.addMessage("\tauthorized_keys file updated.");
 			} catch (Exception e) {
 				r.addMessage("\tCould not update/upload authorized_keys file: "
 						+ e.getLocalizedMessage());
 			}
 		}
 
 	}
 
 	private Set<Directory> getDirectoriesForSite(String site) {
 		Set<Directory> dirs = Sets.newHashSet();
 
 		for (Queue q : uem.getAllAvailableSubmissionLocations()) {
 			if (!site.equalsIgnoreCase((q.getGateway().getSite().getName()))) {
 				continue;
 			}
 
 			for (Directory d : q.getDirectories()) {
 				dirs.add(d);
 			}
 		}
 
 		return dirs;
 	}
 
 	private synchronized List<MountPoint> getMountPoints() {
 		if (mountpoints == null) {
 			DtoMountPoints temp = si.df();
 			if (temp == null || temp.getMountpoints().size() == 0) {
 				throw new RuntimeException("No filesystems available for user.");
 			}
 			mountpoints = temp.getMountpoints();
 		}
 		return mountpoints;
 	}
 
 	private File getSshAuthorizedKeysForDirectory(String baseUrl) {
 
 		synchronized (baseUrl.intern()) {
 
 			File authFile = sshAuthFilesMap.get(baseUrl);
 			if (authFile == null) {
 				try {
 					System.out.println("URL: " + baseUrl);
 					authFile = fm
 							.downloadFile(baseUrl + ".ssh/authorized_keys");
 					sshAuthFilesMap.put(baseUrl, authFile);
 					System.out.println("CONTENT " + baseUrl + ": " + authFile);
 				} catch (FileTransactionException e) {
 					myLogger.debug("Can't access " + baseUrl
 							+ ".ssh/authorized_keys: "
 							+ e.getLocalizedMessage());
 					throw new RuntimeException(e);
 				}
 
 			}
 			return authFile;
 		}
 
 	}
 
 	private String getUsernameForUrl(String url) {
 
 		synchronized (url.intern()) {
 			String username = sshUsernameMap.get(url);
 
 			if (StringUtils.isBlank(username)) {
 
 				GridFile file = null;
 				try {
 					file = fm.ls(url);
 				} catch (RemoteFileSystemException e) {
 					myLogger.debug("Can't access " + url + ": "
 							+ e.getLocalizedMessage());
 					throw new RuntimeException(e);
 				}
 
 				String tmp = FileManager.removeTrailingSlash(file.getUrl());
 				username = tmp.substring(tmp.lastIndexOf("/") + 1);
 				sshUsernameMap.put(url, username);
 				System.out.println("Username: " + username);
 			}
 			return username;
 		}
 	}
 
 	private void login(WizardLoginProgressPage infoPage,
 			Map<PROPERTY, Object> config) {
 
 		if (si != null) {
 			wizard.setNextEnabled(true);
 			return;
 		}
 
 		try {
 
 			String idp = (String) config.get(PROPERTY.IdP);
 			String un = (String) config.get(PROPERTY.Username);
 
 			infoPage.setLoginStarted(idp, un);
 
 			credential = new SLCSCred();
 
 			credential.init(config);
 
 			infoPage.addMessage("Logging in successful.");
 
 			infoPage.addMessage("Connecting to compute infrastructure...");
 			si = LoginManager.login(GRISU_BACKEND, credential, false);
 			fm = GrisuRegistryManager.getDefault(si).getFileManager();
 			uem = GrisuRegistryManager.getDefault(si)
 					.getUserEnvironmentManager();
 			Thread t = new Thread() {
 				public void run() {
 					preload();
 				}
 			};
 			t.setName("getAllSshAuthFiles");
 			t.start();
 			infoPage.addMessage("Success!");
 			setServiceInterface(si);
 			infoPage.setLoginFinished();
 			wizard.setNextEnabled(true);
 		} catch (Exception e) {
 			infoPage.setLoginError(e);
 		}
 	}
 
 	@Override
 	public void onCanceled(List<WizardPage> path, WizardSettings settings) {
 
 		System.exit(0);
 	}
 
 	@Override
 	public void onFinished(List<WizardPage> path, WizardSettings settings) {
 
 		System.exit(0);
 	}
 
 	@Override
 	public void onPageAboutToChange(final WizardPage oldPage,
 			final List<WizardPage> path) throws Exception {
 		nextPressedAtLeastOnce = true;
 		// login
 		if (oldPage instanceof WizardLoginPage) {
 
 			if ( si == null ) {
 				wizard.setNextEnabled(false);
 			}
 			WizardLoginPage page = (WizardLoginPage) oldPage;
 			credconfig = page.getCredentialConfig();
 
 		} else if (oldPage instanceof WizardSSHCopyPage) {
 			wizard.setNextEnabled(false);
 			final WizardSSHCopyPage sshcopyPage = (WizardSSHCopyPage) oldPage;
 			sshkey = sshcopyPage.getGridSshKey();
 			sites = sshcopyPage.getSelectedSites();
 			forceCreateNewKey = sshcopyPage.isForceCreateNewKey();
 		} else if (oldPage instanceof SshConfigSetupPage) {
 			final SshConfigSetupPage page = (SshConfigSetupPage) oldPage;
 
 			for (String site : sites) {
 				for ( Directory d : getDirectoriesForSite(site)) {
 					System.out.println("DIR: "+d.toString());
 					SshBookmark b = null;
 					try {
 						b = getBookmark(d);
 					} catch (Exception e) {
 						myLogger.error("Could not get bookmark: "+d.toString());
 						continue;
 					}
 					if ( b != null ) {
 						bookmarks.add(getBookmark(d));
 					}
 				}
 			}
 
 			createMobaConfig = page.createMobaXTermConfig();
 			createSshConfig = page.createSshConfig();
 			
 		}
 
 	}
 	
 	public SshBookmark getBookmark(Directory d) {
 		
 		SshBookmark b = new SshBookmark();
 		b.setUsername(getUsernameForUrl(d.toUrl()));
 		String host = BOOKMARK_HOSTS.get(d.toUrl());
 		String name = BOOKMARK_NAMES.get(d.toUrl());
 		if ( StringUtils.isBlank(host)) {
 			myLogger.debug("Can't find host for: "+d.toUrl());
 			return null;
 		}
 		if ( StringUtils.isBlank(name)) {
 			myLogger.debug("Can't find name for: "+d.toUrl());
 			return null;
 		}
 		b.setHost(host);
 		b.setName(name);
 		return b;
 	}
 
 	
 	@Override
 	public void onPageChanged(final WizardPage newPage, List<WizardPage> path) {
 		
 		wizard.setFinishEnabled(false);
 		wizard.setCancelEnabled(true);
 		
 
 		if (newPage instanceof WizardLoginProgressPage) {
 			
 			if (si != null) {
 				return;
 			}
 			wizard.setPrevEnabled(false);
 			((WizardLoginProgressPage) newPage).clearProgressLog();
 			Thread t = new Thread() {
 				public void run() {
 
 					login((WizardLoginProgressPage) newPage, credconfig);
 					
 					wizard.setPrevEnabled(true);
 				}
 			};
 			t.setName("LoginThread");
 			t.start();
 
 		} else if (newPage instanceof WizardSSHCopyProgressPage) {
 
 			if ( newPage instanceof LogRenderer ) {
 				((LogRenderer)newPage).clearProgressLog();
 			}
 
 			final WizardSSHCopyProgressPage page = (WizardSSHCopyProgressPage) newPage;
 
 			page.clearProgressLog();
 			wizard.setPrevEnabled(false);
 			wizard.setFinishEnabled(false);
 
 			Thread t = new Thread() {
 				public void run() {
 					try {
 						copySshKey(page, forceCreateNewKey);
 						wizard.setNextEnabled(true);
 					} catch (Exception e) {
 						e.printStackTrace();
 					} finally {
 						wizard.setPrevEnabled(true);
 //						wizard.setFinishEnabled(true);
 					}
 				}
 			};
 			t.setName("SshCopyThread");
 			t.start();
 		} else if (newPage instanceof FinishPage) {
 			if ( newPage instanceof LogRenderer ) {
 				((LogRenderer)newPage).clearProgressLog();
 			}
 
 			wizard.setFinishEnabled(true);
 			wizard.setNextEnabled(false);
 			wizard.setCancelEnabled(false);
 
 			FinishPage page = (FinishPage)newPage;
 			if ( createMobaConfig ) {
 				createMobaXTermConfig(page);
 			}
 			if ( createSshConfig ) {
 				createSshConfig(page);
 			}
 		}
 
 	}
 
 	private void createSshConfig(LogRenderer page) {
 		SshConfigCreator c = new SshConfigCreator(bookmarks);
 		try {
 			String msg = c.addEntries(sshkey.getKeyPath());
 			page.addMessage(msg);
 		} catch (IOException e) {
 			page.addMessage("Error reading writing ssh config file '"+c.getSshConfigPath()+"':\n\t"+e.getLocalizedMessage());
 		}
 	}
 
 	private void createMobaXTermConfig(LogRenderer page) {
 
 
 			MobaXtermIniCreator c = new MobaXtermIniCreator(bookmarks);
 			
			if ( new File(c.getMobaXtermPath()).exists() ) {
 				
 			    JOptionPane pane = new JOptionPane(
			            "MobaXterm config file already exists at: "+c.getMobaXtermPath()+"\nOverwrite?");
 			        Object[] options = new String[] { "Overwrite", "Cancel" };
 			        pane.setOptions(options);
 			        Point loc = this.getLocation();
			        JDialog dialog = pane.createDialog(new JFrame(), "Write config file");
 			        dialog.setLocation(new Point((int)loc.getX(), (int)loc.getY() + 250));
 			        dialog.setVisible(true);
 			        Object obj = pane.getValue(); 
 			        int result = -1;
 			        for (int k = 0; k < options.length; k++)
 			          if (options[k].equals(obj))
 			            result = k;
 			        if ( result == 0 ) {
 						c.create();
						page.addMessage("Config file for MobaXterm created:\n\t"+c.getMobaXtermPath()+"\n");
 			        } else {
 			        	page.addMessage("Skipped creating config file for MobaXTerm.\n");
 			        }
 				
 			} else {
 				c.create();
				page.addMessage("Config file for MobaXterm created:\n\t"+c.getMobaXtermPath()+"\n");
 			}
 
 	}
 
 	private void preload() {
 
 		for (String site : uem.getAllAvailableSites()) {
 			preloadForSite(site);
 		}
 
 	}
 
 	private void preloadForSite(String site) {
 
 		final Set<Directory> dirs = getDirectoriesForSite(site);
 
 		for (final Directory d : dirs) {
 			Thread t = new Thread() {
 				public void run() {
 					try {
 						getSshAuthorizedKeysForDirectory(d.toUrl());
 					} catch (Exception e) {
 						myLogger.error("Error preloading environment.", e);
 					}
 				}
 			};
 			t.setName("Lookup: " + d.toUrl());
 			t.start();
 			Thread t2 = new Thread() {
 				public void run() {
 					try {
 						getUsernameForUrl(d.toUrl());
 					} catch (Exception e) {
 						myLogger.error("Error preloading username.", e);
 					}
 				}
 			};
 			t2.setName("UsernameLookup: " + d.toUrl());
 			t2.start();
 		}
 
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent arg0) {
 
		System.out.println("Event: " + arg0.getPropertyName());
		System.out.println("Value: " + arg0.getNewValue());
 		
 		if ( "idpsLoaded".equals(arg0.getPropertyName() )) {
 			if ( nextPressedAtLeastOnce ) {
 				return;
 			}
 			boolean loaded = (Boolean)arg0.getNewValue();
 			if ( loaded ) {
 				wizard.setNextEnabled(true);
 			} else {
 				wizard.setNextEnabled(false);
 			}
 		}
 		
 
 	}
 
 	public void setServiceInterface(final ServiceInterface si) {
 
 		Thread t = new Thread() {
 			public void run() {
 				WizardSSHCopyPage page = (WizardSSHCopyPage) pageFactory
 						.getPage(NesiSetupWizardPageFactory.SSH_COPY_NAME);
 				page.setServiceInterface(si);
 
 			}
 		};
 
 		t.setName("InitPages");
 		t.start();
 
 	}
 
 }
