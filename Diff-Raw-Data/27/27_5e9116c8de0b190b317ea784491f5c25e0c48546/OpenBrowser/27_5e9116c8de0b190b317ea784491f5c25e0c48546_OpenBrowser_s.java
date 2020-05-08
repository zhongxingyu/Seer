 /*
   File: OpenBrowser.java
 
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.util;
 
 import cytoscape.CytoscapeInit;
 import cytoscape.logger.CyLogger;
 
 import java.io.IOException;
 
 import java.util.Properties;
 
 
 /**
  *
  */
 public abstract class OpenBrowser {
	static String UNIX_PROTOCOL = "file:";
//	static String UNIX_PATH = "gnome-moz-remote";
 
 	static String UNIX_PATH = "htmlview";
	static String UNIX_FLAG = "-remote openURL";
 
	//static String WINDOWS_PATH = "cmd.exe /c start";
 	static String MAC_PATH = "open";
 	private static final String WIN_PATH = "rundll32";
 	private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param url DOCUMENT ME!
 	 */
 	public static void openURL(String url) {
 		Properties prop = CytoscapeInit.getProperties();
 		String defBrowser = prop.getProperty("defaultWebBrowser");
 
 		String osName = System.getProperty("os.name");
 
 		try {
 			String cmd;
 
 			if (osName.startsWith("Windows")) {
 				cmd = WIN_PATH + " " + WIN_FLAG + " " + url;

				// cmd = WINDOWS_PATH + " " + url;
 			} else if (osName.startsWith("Mac")) {
 				cmd = MAC_PATH + " " + url;
 			} else {
				// cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
				if (defBrowser != null) {
 					cmd = defBrowser + " " + url;
					CyLogger.getLogger().info("Opening URL by command \"" + defBrowser + "\"");
 				} else {
 					cmd = UNIX_PATH + " " + url;
					CyLogger.getLogger().info("Opening URL by command \"" + UNIX_PATH + "\"");
 				}
 			}
 
			// CyLogger.getLogger().info("cmd=" + cmd);
 			Process p = Runtime.getRuntime().exec(cmd);
 
 			try {
 				int exitCode = p.waitFor();
 
 				if (exitCode != 0) {
 					CyLogger.getLogger().warn("cmd failed, start new browser");
 					cmd = UNIX_PATH + " " + url;
 					p = Runtime.getRuntime().exec(cmd);
 				}
 			} catch (InterruptedException ex) {
 			}
 		} catch (IOException ioe) {
 		}
 	}
 }
