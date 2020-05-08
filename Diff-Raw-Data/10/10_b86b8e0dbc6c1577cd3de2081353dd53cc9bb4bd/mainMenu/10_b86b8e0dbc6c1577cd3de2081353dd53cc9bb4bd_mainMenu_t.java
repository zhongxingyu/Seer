 /*  mainMenu.java - mainMenu class
  *  Copyright (C) 1999, 2000 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.gjt.fredde.yamm.gui.main;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import java.awt.Rectangle;
 import java.awt.Font;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import org.gjt.fredde.util.gui.MsgDialog;
 import org.gjt.fredde.yamm.gui.sourceViewer;
 import org.gjt.fredde.yamm.mail.Mailbox;
 import org.gjt.fredde.yamm.YAMMWrite;
 // import org.gjt.fredde.yamm.Options;
 import org.gjt.fredde.yamm.gui.confwiz.ConfigurationWizard;
 import org.gjt.fredde.yamm.YAMM;
 
 /**
  * The mainMenu class.
  * This is the menu that the mainwindow uses.
  * @author Fredrik Ehnbom
 * @version $Id: mainMenu.java,v 1.22 2000/03/15 13:43:00 fredde Exp $
  */
 public class mainMenu extends JMenuBar {
 
 	/**
 	 * This frame is used to do som framestuff
 	 */
 	static YAMM frame = null;
 
 	/**
 	 * Makes the menu, adds a menulistener etc...
 	 * @param frame2 The JFrame that will be used when displaying error
 	 * messages etc
 	 */
 	public mainMenu(YAMM frame2) {
 		frame = frame2;
 
 		JMenu file = new JMenu(YAMM.getString("file"));
 		file.setFont(new Font("SansSerif", Font.BOLD, 12));
 		JMenuItem rad;
 		add(file);
 
 
 		// the file menu
 		rad = new JMenuItem(YAMM.getString("file.new"),
 				new ImageIcon(getClass().getResource("/images/buttons/new_mail.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		file.add(rad);
 
 		rad = new JMenuItem(YAMM.getString("file.save_as"),
 				new ImageIcon(getClass().getResource("/images/buttons/save_as.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		file.add(rad);
 
 		file.addSeparator();
 
 		rad = new JMenuItem(YAMM.getString("file.exit"),
 				new ImageIcon(getClass().getResource("/images/buttons/exit.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		file.add(rad);
 
 		// the edit menu
 		JMenu edit = new JMenu(YAMM.getString("edit"));
 		edit.setFont(new Font("SansSerif", Font.BOLD, 12));
 		add(edit);
 
 		rad = new JMenuItem(YAMM.getString("edit.settings"),
 				new ImageIcon(getClass().getResource("/images/buttons/prefs.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		edit.add(rad);
 
 		rad = new JMenuItem(YAMM.getString("edit.view_source"),
 				new ImageIcon(getClass().getResource("/images/buttons/search.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		edit.add(rad);
 
 		// the help menu
 		JMenu help = new JMenu(YAMM.getString("help"));
 		help.setFont(new Font("SansSerif", Font.BOLD, 12));
 		add(help);
 
 		rad = new JMenuItem(YAMM.getString("help.about_you"),
 				new ImageIcon(getClass().getResource("/images/buttons/help.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		help.add(rad);
 
 		rad = new JMenuItem(YAMM.getString("help.about"),
 				new ImageIcon(getClass().getResource("/images/buttons/help.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		help.add(rad);
 
 		rad = new JMenuItem(YAMM.getString("help.license"),
 				new ImageIcon(getClass().getResource("/images/types/text.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		help.add(rad);
 
 		help.addSeparator();
 
 		rad = new JMenuItem(YAMM.getString("help.bug_report"),
 				new ImageIcon(getClass().getResource("/images/buttons/bug.gif")));
 		rad.addActionListener(MListener);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		help.add(rad);
 	}
 
 	ActionListener MListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			String kommando = ((JMenuItem)ae.getSource()).getText();
 
 			if (kommando.equals(YAMM.getString("file.exit"))) {
 				frame.Exit();
 			} else if (kommando.equals(YAMM.getString(
 							"help.about_you"))) {
 				String host = null;
 				String ipaddress = null;
 
 				try {
 					InetAddress myInetaddr = InetAddress.
 								getLocalHost();
 
 					ipaddress = myInetaddr.getHostAddress();
 					host = myInetaddr.getHostName();
 				} catch (UnknownHostException uhe) {}
 
 				if (ipaddress == null) {
 					ipaddress = YAMM.getString("unknown");
 				}
 				if (host == null) {
 					host = YAMM.getString("unknown");
 				}
 
 				Object[] args = {
 					System.getProperty("os.name"),
 					System.getProperty("os.version"),
 					System.getProperty("os.arch"),
 					ipaddress,
 					host,
 					System.getProperty("java.version"),
 					System.getProperty("java.vendor"),
 					System.getProperty("java.vendor.url"),
 					System.getProperty("user.name"),
 					System.getProperty("user.home")
 				};
 
 				new MsgDialog(null,
 					YAMM.getString("help.about_you"),
 					YAMM.getString("info.about.you", args));
 			} else if (kommando.equals(YAMM.getString(
 							"help.about"))) {
 
 				Object[] args = {
 					YAMM.version,
 					YAMM.compDate,
 					"http://www.gjt.org/~fredde/yamm.html",
 					"<fredde@gjt.org>"
 				};
 
 				new MsgDialog(null,
 						YAMM.getString("help.about"),
 					"Copyright (C) 1999, 2000 Fredrik Ehnbom\n" +
 					YAMM.getString("info.about", args)
 				);
 			} else if (kommando.equals(YAMM.getString(
 							"help.bug_report"))) {
 				YAMMWrite yw = new YAMMWrite("fredde@gjt.org",
 								"Bug report");
 				JTextArea jt = yw.myTextArea;
 				jt.append("What is the problem?\n\n");
 				jt.append("How did you make it happen?\n\n");
 				jt.append("Can you make it happen again?\n\n");
 				jt.append("\nAnd now some info about your " + 
 								"system:\n");
 				p("java.version", jt);
 				p("java.vendor", jt);
 				p("java.vendor.url", jt);
 				p("java.home", jt);
 				p("java.vm.specification.version", jt);
 				p("java.vm.specification.vendor", jt); 
 				p("java.vm.specification.name", jt);
 				p("java.vm.version", jt);
 				p("java.vm.vendor", jt);
 				p("java.vm.name", jt);
 				p("java.specification.version", jt);
 				p("java.specification.vendor", jt);
 				p("java.specification.name", jt);
 				p("java.class.version", jt);
 				p("java.class.path", jt);
 				p("os.name", jt);
 				p("os.arch", jt);
 				p("os.version", jt);
				jt.append("YAMM.version: " + YAMM.version + "\n");
				jt.append("YAMM.compDate: " + YAMM.compDate);
 			} else if (kommando.equals(YAMM.getString(
 							"help.license"))) {
 				new MsgDialog(null,
 					YAMM.getString("help.license"), 
 					"Yet Another Mail Manager " +
 					YAMM.version + " E-Mail Client\n" +
 					"Copyright (C) 1999 Fredrik Ehnbom\n" +
 					YAMM.getString("license"),
 					MsgDialog.OK, JLabel.LEFT
 				);
 			}  else if (kommando.equals(YAMM.getString(
 							"edit.settings"))) {
 				new ConfigurationWizard(frame);
 			} else if (kommando.equals(YAMM.getString(
 								"file.new"))) {
 				new YAMMWrite();
 			} else if (kommando.equals(YAMM.getString(
 							"edit.view_source"))) {
 				JTable tmp = frame.mailList;
 				int test = tmp.getSelectedRow();
 
 				if (test >= 0 &&
 					test < frame.listOfMails.size()) {
 
 					int i = 0;
 
 					while (i < 4) {
 						if (tmp.getColumnName(i).
 								equals("#")) {
 							break;
 						}
 						i++;
 					}
 
 					int msg = Integer.parseInt(
 							tmp.getValueAt(
 							tmp.getSelectedRow(),i).
 								toString());
 
 					long skip = Long.parseLong(
 						((Vector) frame.listOfMails.
 						elementAt(tmp.getSelectedRow())).elementAt(5).
 						toString());
 
 					if (msg != -1) {
 						sourceViewer sv =
 							new sourceViewer();
 						int ret = Mailbox.viewSource(
 							frame.selectedbox, msg,
 							skip,
 							sv.jtarea
 						);
 						if (ret != -1) {
 							JScrollBar jsb = sv.jsp.getVerticalScrollBar();
 							jsb.updateUI();
 
 							jsb.setValue(0);
 						} else {
 							sv.dispose();
 							sv = null;
 						}
 					}
 				}
 			} else if (kommando.equals(YAMM.getString(
 							"file.save_as"))) {
 				int test = ((JTable)frame.mailList).
 							getSelectedRow();
 
 				if (test != -1 &&
 					test <= frame.listOfMails.size()) {
 
 					JFileChooser jfs = new JFileChooser();
 					jfs.setFileSelectionMode(
 							JFileChooser.FILES_ONLY
 					);
 					jfs.setMultiSelectionEnabled(false);
 					jfs.setFileFilter(new filter());
 					jfs.setSelectedFile(
 						new File("mail.html")
 					);
 					int ret = jfs.showSaveDialog(frame);
 
 					if (ret == JFileChooser.APPROVE_OPTION) {
 						String tmp = frame.selectedbox;
 						String boxName = tmp.substring(
 							tmp.indexOf("boxes") +
 							6,
 							tmp.length()
 						);
 						tmp = frame.mailPageString;
 						boxName = tmp.substring(8,
 							tmp.length()) +
 							boxName + YAMM.sep + 
 							frame.mailList.
 							getSelectedRow() +
 							".html";
 
 						new File(boxName).renameTo(
 							jfs.getSelectedFile()
 						);
 					}
 				}
 			}
 		}
 		void p(String prop, JTextArea ta) {
 			ta.append(
 				prop + " : " +
 				System.getProperty(prop) + "\n"
 			);
 		}
 
 	};
 
 	protected class filter extends FileFilter {
 		public boolean accept(File f) {
 			if (f.isDirectory()) {
 				return true;
 			}
 
 			String s = f.getName();
 			int i = s.lastIndexOf('.');
 
 			if (i > 0 &&  i < s.length() - 1) {
 				String extension = s.substring(i + 1).
 								toLowerCase();
 				if ("htm".equals(extension) ||
 						"html".equals(extension)) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 			return false;
 		}
 
 		// The description of this filter
 		public String getDescription() {
 			return "HTML files";
 		}
 	}
 }
 /*
  * Changes:
  * $Log: mainMenu.java,v $
 * Revision 1.22  2000/03/15 13:43:00  fredde
 * added YAMM.compDate to bug report
 *
  * Revision 1.21  2000/03/05 18:02:53  fredde
  * now gets the images used for the jar-file
  *
  * Revision 1.20  2000/02/28 13:46:42  fredde
  * added some javadoc tags and changelog. Cleaned up
  *
  */
