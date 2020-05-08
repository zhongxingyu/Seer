/*  $Id: YAMMWrite.java,v 1.25 2001/05/27 08:57:00 fredde Exp $
 *  Copyright (C) 1999-2001 Fredrik Ehnbom
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
 package org.gjt.fredde.yamm;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 import org.gjt.fredde.util.gui.*;
 import org.gjt.fredde.yamm.encode.*;
 import org.gjt.fredde.yamm.gui.*;
 
 /**
  * The class for writing mails
  * @author Fredrik Ehnbom
 * @version $Revision: 1.25 $
  */
 public class YAMMWrite extends JFrame {
 
 	/** A list of files to attach to the mail */
 	static protected Vector         attach;
 
 	/** The textarea for writing in the message */
 	public JTextArea      myTextArea;
 
 	private JTextField  toField      = new JTextField();
 	private JTextField  subjectField = new JTextField();
 	private JTextField  ccField      = new JTextField();
 	private JComboBox   fromField; // new JComboBox();
 	private AttachList	myList;
 
 
 	/**
 	 * Creates a new empty mail with no subject and recipent
 	 */
 	public YAMMWrite() {
 		this("", "", "","");
 	}
 
 	/**
 	 * Creates a new empty mail with no subject and with the specified
 	 * recipent
 	 * @param to The recipents address
 	 */
 	public YAMMWrite(String to) {
 		this(to, "", "","");
 	}
 
 	/**
 	 * Creates a new empty mail with the specified subject and recipent
 	 * @param to The recipents address
 	 * @param subject The subject
 	 */
 	public YAMMWrite(String to, String subject) {
 		this(to, "", subject, "");
 	}
 
 	public YAMMWrite(String to, String from, String subject, String body) {
 		super(subject);
 
 		setBounds(
 			Integer.parseInt(YAMM.getProperty("writex", "0")),
 			Integer.parseInt(YAMM.getProperty("writey", "0")),
 			Integer.parseInt(YAMM.getProperty("writew", "500")),
 			Integer.parseInt(YAMM.getProperty("writeh", "300"))
 		);
 
 		JMenuBar  Meny = new JMenuBar();
 		JMenu     arkiv = new JMenu(YAMM.getString("file"));
 		JMenuItem rad;
 		Meny.add(arkiv);
 
 		rad = new JMenuItem(
 			YAMM.getString("button.cancel"),
 			new ImageIcon(getClass().getResource("/images/buttons/cancel.gif"))
 		);
 		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		rad.addActionListener(MListener);
 		arkiv.add(rad);
 
 		setJMenuBar(Meny);
 
 		Box fields = Box.createHorizontalBox();
 		Box vert3 = Box.createVerticalBox();
 		Box hori1 = Box.createHorizontalBox();
 		Box hori2 = Box.createHorizontalBox();
 
 		JButton myButton = new JButton();
 		myButton.setToolTipText(YAMM.getString("button.send"));
 
 		if (YAMM.text) myButton.setText(YAMM.getString("button.send"));
 		if (YAMM.ico) myButton.setIcon(new ImageIcon(getClass().getResource("/images/buttons/send.gif")));
 
 		myButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		myButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		myButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 		myButton.setBorderPainted(false);
 		myButton.addActionListener(BListener);
 		hori1.add(myButton);
 
 		myButton = new JButton();
 		myButton.setToolTipText(YAMM.getString("button.cancel"));
 
 		if (YAMM.text) myButton.setText(YAMM.getString("button.cancel"));
 		if (YAMM.ico) myButton.setIcon(new ImageIcon(getClass().getResource("/images/buttons/cancel.gif")));
 
 		myButton.addActionListener(BListener);
 		myButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		myButton.setBorderPainted(false);
 		myButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		myButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 		hori1.add(myButton);
 		vert3.add(hori1);
 
 		FontMetrics fm = getFontMetrics(new Font("Dialog", Font.BOLD, 12));
 		int height = 25;
 		Dimension size = new Dimension(fm.stringWidth(YAMM.getString("mail.subject")) + 25, height);
 
 		JLabel myLabel = new JLabel(YAMM.getString("mail.to"));
 		myLabel.setMaximumSize(size);
 		myLabel.setMinimumSize(size);
 		fields.add(myLabel);
 
 		toField.setMaximumSize(new Dimension(1200, height));
 		toField.setMinimumSize(new Dimension(75, height));
 		toField.setText(to);
 		toField.setToolTipText(YAMM.getString("tofield.tooltip"));
 		fields.add(toField);
 
 		vert3.add(fields);
 		fields = Box.createHorizontalBox();
 
 		myLabel = new JLabel(YAMM.getString("mail.from"));
 		myLabel.setMaximumSize(size);
 		myLabel.setMinimumSize(size);
 		fields.add(myLabel);
 
 		fromField = new JComboBox(YAMM.profiler.getProfileList());
 		fromField.setMaximumSize(new Dimension(1200, height));
 		fromField.setMinimumSize(new Dimension(75, height));
 		fromField.setFont(toField.getFont());
 		fromField.setEditable(true);
 		fields.add(fromField);
 
 		if (!from.equals("")) {
 			String prof = YAMM.profiler.getProfileString(from);
 
 			fromField.addItem(from);
 			fromField.setSelectedItem(prof);
 		} else if (fromField.getItemCount() != 0) {
 			fromField.setSelectedIndex(YAMM.profiler.getDefault());
 		}
 
 		vert3.add(fields);
 		fields = Box.createHorizontalBox();
 
 		myLabel = new JLabel(YAMM.getString("mail.subject"));
 		myLabel.setMaximumSize(size);
 		myLabel.setMinimumSize(size);
 		fields.add(myLabel);
 
 		subjectField.addKeyListener(
 			new KeyAdapter() {
 				public void keyReleased(KeyEvent ke) {
 					setTitle(subjectField.getText());
 				}
 
 				public void keyPressed(KeyEvent ke) {
 					setTitle(subjectField.getText());
 				}
 			}
 		);
 		subjectField.setMaximumSize(new Dimension(1200, height));
 		subjectField.setMinimumSize(new Dimension(75, height));
 		subjectField.setText(subject);
 		fields.add(subjectField);
 
 		vert3.add(fields);
 		fields = Box.createHorizontalBox();
 
 		myLabel = new JLabel(YAMM.getString("mail.cc"));
 		myLabel.setMaximumSize(size);
 		myLabel.setMinimumSize(size);
 		fields.add(myLabel);
 
 		ccField.setMaximumSize(new Dimension(1200, height));
 		ccField.setMinimumSize(new Dimension(75, height));
 		fields.add(ccField);
 		vert3.add(fields);
 
 		myTextArea = new JTextArea();
 		myTextArea.setText(body);
 
 		if (body.equals("")) sign();
 
 		JTabbedPane JTPane = new JTabbedPane(JTabbedPane.BOTTOM);
 		JTPane.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		JTPane.addTab(
 			YAMM.getString("mail"),
 			new ImageIcon(getClass().getResource("/images/buttons/mail.gif")),
 			new JScrollPane(myTextArea)
 		);
 
 
 		JPanel myPanel = new JPanel(new BorderLayout());
 
 		hori1 = Box.createHorizontalBox();
 
 		myButton = new JButton();
 		myButton.setToolTipText(YAMM.getString("button.add"));
 
 		if (YAMM.text) myButton.setText(YAMM.getString("button.add"));
 		if (YAMM.ico) myButton.setIcon(new ImageIcon(getClass().getResource("/images/buttons/new.gif")));
 
 		myButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		myButton.addActionListener(BListener);
 		hori1.add(myButton);
 
 		myButton = new JButton();
 		myButton.setToolTipText(YAMM.getString("button.delete"));
 
 		if (YAMM.text) myButton.setText(YAMM.getString("button.delete"));
 		if (YAMM.ico) myButton.setIcon(new ImageIcon(getClass().getResource("/images/buttons/delete.gif")));
 
 		myButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
 		myButton.addActionListener(BListener);
 		hori1.add(myButton);
 
 		attach = new Vector();
 		ListModel dataModel = new AbstractListModel() {
 			public Object getElementAt(int index) {
 				return attach.elementAt(index);
 			}
 			public int getSize() {
 				return attach.size();
 			}
 		};
 
 		myList = new AttachList(dataModel, attach, AttachList.DROPMODE);
 		myPanel.add("Center", myList);
 		myPanel.add("South", hori1);
 
 		Border ram = BorderFactory.createEtchedBorder();
 		myList.setBorder(ram);
 
 
 		JTPane.addTab(
 			YAMM.getString("mail.attachment"),
 			new ImageIcon(getClass().getResource("/images/buttons/attach.gif")),
 			myPanel
 		);
 
 		vert3.add(JTPane);
 		getContentPane().add(vert3);
 
 		addWindowListener(new FLyssnare());
 		show();
 	}
 
 	public void sign() {
 		FileInputStream in = null;
 		if (fromField.getSelectedItem() == null) {
 			return;
 		}
 
 		Profile prof = YAMM.profiler.getProfileFor(fromField.getSelectedItem().toString());
 
 		if (prof == null || prof.sign == null || prof.sign.trim().equals("")) {
 			return;
 		}
 
 		try {
			in = new FileInputStream(prof.sign);
 			String tmp = null;
 			byte[] singb = new byte[in.available()];
 
 			in.read(singb);
 			myTextArea.append(new String(singb));  
 			in.close();
 		} catch (IOException ioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ioe,
 				YAMM.exceptionNames
 			);
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 			} catch (IOException ioe) {}
 		}
 	}
 
 	boolean isSendReady() {
 		if (toField.getText().indexOf('@') != -1) return true;
 
 		return false;
 	}
 
 	/**
 	 * Send this letter
 	 */
 	public void send() {
 		PrintWriter outFile = null;
 		try {
 			outFile = new PrintWriter(
 				new FileOutputStream(
 					YAMM.home + "/boxes/" +
 					YAMM.getString("box.outbox"),
 					true
 				)
 			);
 			SimpleDateFormat df = new SimpleDateFormat(
 				"EEE, dd MMM yyyy " +
 				"HH:mm:ss zzz",
 				Locale.US
 			);
 
 			String to   = toField.getText();
 			String from = fromField.getSelectedItem().toString();
 			String cc   = ccField.getText();
 			String temp = null;
 			StringTokenizer tok = new StringTokenizer(to, ",");
 
 			to = "";
 			while (tok.hasMoreTokens()) {
 				temp = tok.nextToken().trim();
 
 				if (to.equals("")) to = temp;
 				else to += "      " + temp;
 
 				if (tok.hasMoreTokens()) to += ",\n";
 			}
 			if (!cc.equals("")) {
 				tok = new StringTokenizer(cc, ",");
 
 				cc = "";
 				while (tok.hasMoreTokens()) {
 					temp = tok.nextToken().trim();
 
 					if (cc.equals("")) cc = temp;
 					else cc += "      " + temp;
 
 					if (tok.hasMoreTokens()) cc += ",\n";
 				}
 				to += "\ncc: " + cc;
 			}
 
 			outFile.println(
 				"Date: " + df.format(new Date()) +
 				"\nFrom: " + from +
 				"\nTo: " + to +
 				"\nSubject: " + subjectField.getText() +
 				"\nX-Mailer: Yet Another Mail Manager " + YAMM.version
 			);
 
 			if (attach.size() == 0) {
 				outFile.println(
 					"\n" + myTextArea.getText() +
 					"\n" +
 					"\n." +
 					"\n"
 				);
 			} else {
 				outFile.println(
 					"MIME-Version: 1.0" +
 					"\nContent-Type: multipart/mixed; boundary=\"AttachThis\"" +
 					"\n" +
 					"\nThis is a multi-part message in MIME format." +
 					"\n" +
 					"\n--AttachThis" +
 					"\nContent-Type: text/plain; charset=us-ascii" +
 					"\nContent-Transfer-Encoding: 7bit" +
 					"\n" +
 					"\n" + myTextArea.getText() +
 					"\n" +
 					"\n"
 				);
 			}
 			outFile.close();
 			if (attach.size() > 0) {
 				if (YAMM.base64enc) new Base64Encode(attach);
 				else new UUEncode(attach);
 			}
 		} catch (IOException ioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ioe, YAMM.exceptionNames);
 		} finally {
 			if (outFile != null) {
 				outFile.close();
 			}
 		}
 	}
 
 	ActionListener BListener = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			String arg = ((JButton)e.getSource()).getToolTipText();
 
 			if (arg.equals(YAMM.getString("button.send"))) {
 				if (isSendReady()) {
 					send();
 					quit();
 				} else {
 					new MsgDialog(YAMMWrite.this, YAMM.getString("msg.error"), "Missing \"@\" in address field!");
 				}
 			} else if (arg.equals(YAMM.getString("button.add"))) {
 				addAttach();
 			} else if (arg.equals(YAMM.getString("button.delete"))) {
 				int rem = myList.getSelectedIndex();
 
 				if (rem != -1 && rem < attach.size()) {
 					attach.remove(rem);
 					myList.updateUI();
 				}
 			} else {
 				quit();
 			}
 		}
 	};
 
 	void addAttach() {
 		JFileChooser jfs = new JFileChooser();
 		jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		jfs.setMultiSelectionEnabled(true);
 		int ret = jfs.showOpenDialog(this);
 
 		if (ret == JFileChooser.APPROVE_OPTION) {
 			// Why can't I get getSelectedFiles() to work?!
 			// File[] file = jfs.getSelectedFiles();
 			File[] file = {
 				jfs.getSelectedFile()
 			};
 
 			if (file != null) {
 
 				for (int i = 0; i < file.length; i++) {
 					attach.add(file[i]);
 				}
 				myList.updateUI();
 			}
 		}
 	}
 
 	private void quit() {
 		Rectangle rv = new Rectangle();
 		getBounds(rv);
 
 		YAMM.setProperty("writex", "" + rv.x);
 		YAMM.setProperty("writey", "" + rv.y);
 		YAMM.setProperty("writew", "" + rv.width);
 		YAMM.setProperty("writeh", "" + rv.height);
 
 		dispose();
 	}
 
 	ActionListener MListener = new ActionListener() {
 		public void actionPerformed(ActionEvent ae) {
 			String kommando = ((JMenuItem)ae.getSource()).getText();
 
 			if (kommando.equals(YAMM.getString("button.cancel"))) {
 				quit();
 			}
 		}
 	};
 
 	class FLyssnare extends WindowAdapter {
 		public void windowClosing(WindowEvent event) {
 			quit();
 		}
 	}
 }
 /*
  * Changes:
  * $Log: YAMMWrite.java,v $
 * Revision 1.25  2001/05/27 08:57:00  fredde
 * Fixed signature stuff thanks to wYRd
 *
  * Revision 1.24  2001/04/21 09:31:26  fredde
  * drag and drop from attachlist
  *
  * Revision 1.23  2000/08/09 16:26:19  fredde
  * readability fixes, added support for base64 encoding
  * also fixed alot of bugs when buttons where in
  * icononly mode
  *
  * Revision 1.22  2000/04/15 13:05:06  fredde
  * lots of fixes when there are no profiles
  *
  * Revision 1.21  2000/04/01 20:50:09  fredde
  * fixed to make the profiling system work
  *
  * Revision 1.20  2000/03/26 15:27:08  fredde
  * added cc- and fromfields among other fixes
  *
  * Revision 1.19  2000/03/15 13:41:14  fredde
  * cleaned up
  *
  * Revision 1.18  2000/03/05 18:06:08  fredde
  * gets it's images for the jar file. Also fixed a nullpointer in the attachment list.
  *
  */
