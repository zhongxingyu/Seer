 /*
  * This file is part of DrFTPD, Distributed FTP Daemon.
  *
  * DrFTPD is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * DrFTPD is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with DrFTPD; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.drftpd.commands.list;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.Logger;
 import org.bushe.swing.event.annotation.AnnotationProcessor;
 import org.bushe.swing.event.annotation.EventSubscriber;
 import org.drftpd.GlobalContext;
 import org.drftpd.commandmanager.CommandInterface;
 import org.drftpd.commandmanager.CommandRequest;
 import org.drftpd.commandmanager.CommandResponse;
 import org.drftpd.commandmanager.StandardCommandManager;
 import org.drftpd.event.LoadPluginEvent;
 import org.drftpd.event.UnloadPluginEvent;
 import org.drftpd.master.BaseFtpConnection;
 import org.drftpd.master.ConnectionManager;
 import org.drftpd.master.FtpReply;
 import org.drftpd.master.TransferState;
 import org.drftpd.usermanager.User;
 import org.drftpd.util.CommonPluginUtils;
 import org.drftpd.util.MasterPluginUtils;
 import org.drftpd.vfs.DirectoryHandle;
 import org.drftpd.vfs.FileHandle;
 import org.drftpd.vfs.InodeHandleInterface;
 import org.drftpd.vfs.LinkHandle;
 import org.drftpd.vfs.ObjectNotValidException;
 import org.tanesha.replacer.ReplacerEnvironment;
 
 /**
  * @author mog
  * 
  * @version $Id$
  */
 public class LIST extends CommandInterface {
 	private final static DateFormat AFTER_SIX = new SimpleDateFormat(" yyyy");
 
 	private final static DateFormat BEFORE_SIX = new SimpleDateFormat("HH:mm");
 
 	private final static String DELIM = " ";
 
 	private final static DateFormat FULL = new SimpleDateFormat("HH:mm:ss yyyy");
 
 	private static final Logger logger = Logger.getLogger(LIST.class);
 
 	private final static String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May",
 		"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
 
 	protected final static String NEWLINE = "\r\n";
 
 	private ArrayList<AddListElementsInterface> _listAddons = new ArrayList<AddListElementsInterface>();
 
 	private StandardCommandManager _cManager;
 	
 	private ResourceBundle _bundle;
 
 	private String _keyPrefix;
 
 	@Override
 	public void initialize(String method, String pluginName, StandardCommandManager cManager) {
 		super.initialize(method, pluginName, cManager);
 		_cManager = cManager;
 		
     	_bundle = cManager.getResourceBundle();
     	_keyPrefix = this.getClass().getName()+".";
 		
 		// Subscribe to events
 		AnnotationProcessor.process(this);
 
 		// Load any additional element providers from plugins
 		try {
 			List<AddListElementsInterface> loadedListAddons =
 				CommonPluginUtils.getPluginObjects(this, "org.drftpd.commands.list", "AddElements", "Class");
 			for (AddListElementsInterface listAddon : loadedListAddons) {
 				listAddon.initialize();
 				_listAddons.add(listAddon);
 			}
 		} catch (IllegalArgumentException e) {
 			logger.error("Failed to load plugins for org.drftpd.commands.list extension point 'AddElements', possibly the "+
 					"org.drftpd.commands.list extension point definition has changed in the plugin.xml",e);
 		}
 	}
 
 	/**
 	 * Get permission string.
 	 */
 	private static String getPermission(InodeHandleInterface fl)
 	throws FileNotFoundException {
 		StringBuffer sb = new StringBuffer(13);
 
 		if (fl.isLink()) {
 			sb.append("l");
 		} else if (fl.isDirectory()) {
 			sb.append("d");
 		} else {
 			sb.append("-");
 		}
 		sb.append("rw");
 		sb.append(fl.isDirectory() ? "x" : "-");
 
 		sb.append("rw");
 		sb.append(fl.isDirectory() ? "x" : "-");
 
 		sb.append("rw");
 		sb.append(fl.isDirectory() ? "x" : "-");
 
 		return sb.toString();
 	}
 
 	public static String getUnixDate(long date, boolean fulldate) {
 		Date date1 = new Date(date);
 		long dateTime = date1.getTime();
 
 		if (dateTime < 0) {
 			return "------------";
 		}
 
 		Calendar cal = new GregorianCalendar();
 		cal.setTime(date1);
 
 		String firstPart = MONTHS[cal.get(Calendar.MONTH)] + ' ';
 
 		String dateStr = String.valueOf(cal.get(Calendar.DATE));
 
 		if (dateStr.length() == 1) {
 			dateStr = ' ' + dateStr;
 		}
 
 		firstPart += (dateStr + ' ');
 
 		long nowTime = System.currentTimeMillis();
 
 		if (fulldate) {
 			synchronized(FULL) {
 				return firstPart + FULL.format(date1);
 			}
 		} else if (Math.abs(nowTime - dateTime) > (183L * 24L * 60L * 60L * 1000L)) {
 			synchronized(AFTER_SIX) {
 				return firstPart + AFTER_SIX.format(date1);
 			}
 		} else {
 			synchronized(BEFORE_SIX) {
 				return firstPart + BEFORE_SIX.format(date1);
 			}
 		}
 	}
 
 	/**
 	 * Get each directory line.
 	 */
 	private static void printLine(InodeHandleInterface fl, Writer out,
 			boolean fulldate) throws IOException {
 		StringBuffer line = new StringBuffer();
 
 		if (fl instanceof FileHandle
 				&& !((FileHandle) fl).isAvailable()) {
 			line.append("----------");
 		} else {
 			line.append(getPermission(fl));
 		}
 
 		line.append(DELIM);
 		line.append((fl.isDirectory() ? "3" : "1"));
 		line.append(DELIM);
 		line.append(ListUtils.padToLength(fl.getUsername(), 8));
 		line.append(DELIM);
 		line.append(ListUtils.padToLength(fl.getGroup(), 8));
 		line.append(DELIM);
 		line.append(fl.getSize());
 		line.append(DELIM);
 		line.append(getUnixDate(fl.lastModified(), fulldate));
 		line.append(DELIM);
 		line.append(fl.getName());
 		if (fl.isLink()) {
 			line.append(DELIM + "->" + DELIM + ((LinkHandle)fl).getTargetString());
 		}
 		line.append(NEWLINE);
 		out.write(line.toString());
 	}
 
 	/**
 	 * Print file list. Detail listing.
 	 * 
 	 * <pre>
 	 *    -a : display all (including hidden files)
 	 * </pre>
 	 * 
 	 * @return true if success
 	 */
 	private static void printList(Collection<InodeHandleInterface> files, Writer os, boolean fulldate)
 	throws IOException {
 		//os.write("total 0" + NEWLINE); - do we need this?
 
 		// print file list
 		for (Iterator<InodeHandleInterface> iter = files.iterator(); iter.hasNext();) {
 			InodeHandleInterface file = iter.next();
 			LIST.printLine(file, os, fulldate);
 		}
 	}
 
 	/**
 	 * <code>NLST [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
 	 * 
 	 * This command causes a directory listing to be sent from server to user
 	 * site. The pathname should specify a directory or other system-specific
 	 * file group descriptor; a null argument implies the current directory. The
 	 * server will return a stream of names of files and no other information.
 	 * 
 	 * 
 	 * <code>LIST [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
 	 * 
 	 * This command causes a list to be sent from the server to the passive DTP.
 	 * If the pathname specifies a directory or other group of files, the server
 	 * should transfer a list of files in the specified directory. If the
 	 * pathname specifies a file then the server should send current information
 	 * on the file. A null argument implies the user's current working or
 	 * default directory. The data transfer is over the data connection
 	 * 
 	 * LIST 125, 150 226, 250 425, 426, 451 450 500, 501, 502, 421, 530
 	 */
 	public CommandResponse list(CommandRequest request, boolean isStat) {
 		try {
 			String directoryName = null;
 			String options = "";
 			BaseFtpConnection conn = (BaseFtpConnection) request.getSession();
 			TransferState ts = conn.getTransferState();
 
 			// String pattern = "*";
 			// get options, directory name and pattern
 			// argument == null if there was no argument for LIST
 			if (request.hasArgument()) {
 				// argument = argument.trim();
 				StringBuffer optionsSb = new StringBuffer(4);
 				StringTokenizer st = new StringTokenizer(request.getArgument(),
 				" ");
 
 				while (st.hasMoreTokens()) {
 					String token = st.nextToken();
 
 					if (token.charAt(0) == '-') {
 						if (token.length() > 1) {
 							optionsSb.append(token.substring(1));
 						}
 					} else {
 						directoryName = token;
 					}
 				}
 
 				options = optionsSb.toString();
 			}
 
 			// check options
 			// boolean allOption = options.indexOf('a') != -1;
 			boolean fulldate = options.indexOf('T') != -1;
 
 			if (!ts.isPasv() && !ts.isPort() && !isStat) {
 				//ts.reset(); issued on the finally block.
 				return StandardCommandManager.genericResponse("RESPONSE_503_BAD_SEQUENCE_OF_COMMANDS");
 			}
 
 			DirectoryHandle directoryFile;
 			CommandResponse response = null;	
 			User user = request.getSession().getUserNull(request.getUser());
 
 			if (directoryName != null) {
 				try {
 					directoryFile = conn.getCurrentDirectory().getDirectory(directoryName, user);
 				} catch (FileNotFoundException ex) {
 					return StandardCommandManager.genericResponse("RESPONSE_550_REQUESTED_ACTION_NOT_TAKEN");
 				} catch (ObjectNotValidException e) {
 					return StandardCommandManager.genericResponse("RESPONSE_504_COMMAND_NOT_IMPLEMENTED_FOR_PARM");
 				}
 			} else {
 				directoryFile = conn.getCurrentDirectory();
 			}
 
 			Writer os = null;
 
 			if (isStat) {
 				response = new CommandResponse(213, "End of STAT");
 				conn.printOutput("213-STAT"+NEWLINE);
 				os = conn.getControlWriter();
 			} else {
 				if (!ts.getSendFilesEncrypted()
 						&& GlobalContext.getConfig().checkPermission(
 								"denydiruncrypted", conn.getUserNull())) {
 					return new CommandResponse(550, "Secure Listing Required");
 				}
 
 				conn.printOutput(new FtpReply(StandardCommandManager.genericResponse("RESPONSE_150_OK")));
 
 				try {
 					os = new PrintWriter(new OutputStreamWriter(ts.getDataSocketForLIST().getOutputStream()));
 				} catch (IOException ex) {
 					logger.warn("from master", ex);
 					return new CommandResponse(425, ex.getMessage());
 				}
 			}
 
 			// //////////////
 			logger.debug("Listing directoryFile - " + directoryFile);
 
 			ListElementsContainer container = new ListElementsContainer(request.getSession(), request.getUser(), _cManager);
 
 			try {
 				container = ListUtils.list(directoryFile, container);
 			} catch (IOException e) {
 				logger.error(e , e);
 				return new CommandResponse(450, e.getMessage());
 			}
 
 			// execute list addons.
 			for (AddListElementsInterface listAddon : _listAddons) {
 				container = listAddon.addElements(directoryFile,container);
 			}
 
 			try {
 				printList(container.getElements(), os, fulldate);
 
 				if (isStat)
 					return response;
 				else {
 					os.close(); // also flushes the Writer.
 					response = StandardCommandManager.genericResponse("RESPONSE_226_CLOSING_DATA_CONNECTION");
 					response.addComment(conn.status());
 					return response;
 				}
 			} catch (IOException ex) {
 				logger.warn("from master", ex);
 				return new CommandResponse(450, ex.getMessage());
 			}
 		} finally {
 			BaseFtpConnection conn = (BaseFtpConnection) request.getSession();
 			conn.getTransferState().reset();
 		}
 	}
 
 	public CommandResponse doSTAT(CommandRequest request) {
 		if (!request.hasArgument()) {
 			BaseFtpConnection conn = (BaseFtpConnection) request.getSession();
 			
 			ReplacerEnvironment env = new ReplacerEnvironment();
 			
 			env.add("ssl.enabled", conn.isSecure() ? "Yes" : "No");
 			env.add("user", conn.getUsername());
			env.add("user.ip", conn.getClientAddress().getHostAddress()); // TODO FIX
 			env.add("user.timeout", conn.getUserNull().getIdleTime());
 			env.add("conns", ConnectionManager.getConnectionManager().getConnections().size()); // TODO sync this.
 			env.add("version", GlobalContext.VERSION);
 			
 			CommandResponse response = new CommandResponse(211, "End of status");
 			response.addComment(conn.jprintf(_bundle, env, _keyPrefix+ "daemon.stat"));
 			
 			return response;
 		}
 		
 		return list(request, true);
 	}
 
 	public CommandResponse doLIST(CommandRequest request) {
 		return list(request, false);
 	}
 
 	public CommandResponse doNLST(CommandRequest request) {
 		//printNList(listFiles, detailOption, os);
 		return null;
 	}
 
 	@EventSubscriber
 	public synchronized void onUnloadPluginEvent(UnloadPluginEvent event) {
 		Set<AddListElementsInterface> unloadedListAddons =
 			MasterPluginUtils.getUnloadedExtensionObjects(this, "AddElements", event, _listAddons);
 		if (!unloadedListAddons.isEmpty()) {
 			ArrayList<AddListElementsInterface> clonedListAddons = new ArrayList<AddListElementsInterface>(_listAddons);
 			boolean addonRemoved = false;
 			for (Iterator<AddListElementsInterface> iter = clonedListAddons.iterator(); iter.hasNext();) {
 				AddListElementsInterface listAddon = iter.next();
 				if (unloadedListAddons.contains(listAddon)) {
 					logger.debug("Unloading list element addon provided by plugin "
 							+CommonPluginUtils.getPluginIdForObject(listAddon));
 					iter.remove();
 					addonRemoved = true;
 				}
 			}
 			if (addonRemoved) {
 				_listAddons = clonedListAddons;
 			}
 		}
 	}
 
 	@EventSubscriber
 	public synchronized void onLoadPluginEvent(LoadPluginEvent event) {
 		try {
 			List<AddListElementsInterface> loadedListAddons =
 				MasterPluginUtils.getLoadedExtensionObjects(this, "org.drftpd.commands.list", "AddElements", "Class", event);
 			if (!loadedListAddons.isEmpty()) {
 				ArrayList<AddListElementsInterface> clonedListAddons = new ArrayList<AddListElementsInterface>(_listAddons);
 				for (AddListElementsInterface listAddon : loadedListAddons) {
 					clonedListAddons.add(listAddon);
 				}
 				_listAddons = clonedListAddons;
 			}
 		} catch (IllegalArgumentException e) {
 			logger.error("Failed to load plugins for a loadplugin event for org.drftpd.commands.list extension point 'AddElements'"+
 					", possibly the org.drftpd.commands.list extension point definition has changed in the plugin.xml",e);
 		}
 	}
 }
