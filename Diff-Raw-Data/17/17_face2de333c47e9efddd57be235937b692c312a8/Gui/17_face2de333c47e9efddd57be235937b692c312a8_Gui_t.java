 /*******************************************************************************
  * Copyright (c) 2013 sgelb.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sgelb - initial API and implementation
  ******************************************************************************/
 package com.github.sgelb.springerlinkdownloader.view;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import com.github.sgelb.springerlinkdownloader.controller.BrowseBtnController;
 import com.github.sgelb.springerlinkdownloader.controller.StartCancelBtnController;
 import com.github.sgelb.springerlinkdownloader.controller.UrlFieldController;
 import com.github.sgelb.springerlinkdownloader.helper.Clipboard;
 import com.github.sgelb.springerlinkdownloader.helper.RegEx;
 
 public class Gui {
 
 	public void run() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException | InstantiationException
 				| IllegalAccessException | UnsupportedLookAndFeelException e) {
 		}
 		JFrame frame = new JFrame("SpringerLink Downloader");
 		frame.setLocationByPlatform(true);
 		frame.setLayout(new BorderLayout());
 
 		JPanel upperArea = new JPanel();
 		upperArea.setLayout(new GridBagLayout());
 
 		JPanel lowerArea = new JPanel();
 		lowerArea.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		
 		int textFieldWidth = 40;
 		JButton startBtn = new JButton("Start");
 
 		// UPPER AREA
 
 		GridBagConstraints c;
 		Insets set = new Insets(6, 6, 6, 6);
 
 		// COLUMN 0
 
 		// "URL"-Label
 		c = new GridBagConstraints();
 		c.insets = set;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.LINE_START;
 		upperArea.add(new JLabel("URL: "), c);
 
 		// "Save folder"-Label
 		c.gridy = 1;
 		upperArea.add(new JLabel("Save folder: "), c);
 
 		// Separator
 		c = new GridBagConstraints();
 		c.insets = set;
 		c.weightx = 1.0;
 		c.gridy = 2;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = GridBagConstraints.LINE_START;
 		upperArea.add(new JSeparator(), c);
 
 		// "Progress"-Label
 		c.gridy = 3;
 		JLabel progressText = new JLabel(
				"Not started yet…"); //Enter URL, select save folder and press »Start«");
		progressText.setEnabled(false);
 		upperArea.add(progressText, c);
 
 		// Progressbar
 		c.gridy = 4;
 		JProgressBar progressBar = new JProgressBar();
 		progressBar.setStringPainted(true);
 		progressBar.setString("");
 		upperArea.add(progressBar, c);
 
 		// COLUMN 1
 
 		// "URL"-Textfield
 		c.gridx = 1;
 		c.gridy = 0;
 		JTextField urlField = new JTextField(textFieldWidth);
 		urlField.setText(Clipboard.getUrlfromClipboard());
 		urlField.getDocument().addDocumentListener(
 				new UrlFieldController(startBtn));
		String urlToolTip = "Enter book download URL – it's the one ending in »/page/1«";
 		urlField.setToolTipText(urlToolTip);
 		upperArea.add(urlField, c);
 
 		// "save Folder"-Textfield
 		c.gridy = 1;
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.anchor = GridBagConstraints.LINE_START;
 		JLabel saveFolderLabel = new JLabel();
 		saveFolderLabel.setText(System.getProperty("user.home"));
 		saveFolderLabel.setToolTipText(saveFolderLabel.getText());
 		upperArea.add(saveFolderLabel, c);
 
 		// COLUMN 2
 
 		// "Browse"-Button
 		c = new GridBagConstraints();
 		c.insets = set;
 		c.gridx = 2;
 		c.gridy = 1;
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.anchor = GridBagConstraints.EAST;
 
 		JButton browseBtn = new JButton("Browse…");
 		browseBtn.addActionListener(new BrowseBtnController(saveFolderLabel));
 		browseBtn.setMnemonic(KeyEvent.VK_B);
 		upperArea.add(browseBtn, c);
 
 		frame.add(upperArea, BorderLayout.CENTER);
 
 		// LOWER AREA
 
 		JButton cancelBtn = new JButton("Cancel");
 		cancelBtn.setMnemonic(KeyEvent.VK_C);
 		startBtn.setEnabled(RegEx.matchUrl(urlField.getText()));
 		startBtn.setMnemonic(KeyEvent.VK_S);
 
 		StartCancelBtnController startCancelBtnController;
 		startBtn.addActionListener((startCancelBtnController = new StartCancelBtnController(
 				urlField, saveFolderLabel, progressText, progressBar, startBtn,
 				browseBtn, cancelBtn)));
 		cancelBtn.addActionListener(startCancelBtnController);
 
 		lowerArea.add(cancelBtn);
 		lowerArea.add(startBtn);
 
 		frame.add(lowerArea, BorderLayout.SOUTH);
 
 		//////
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setMinimumSize(frame.getPreferredSize());
 		frame.setVisible(true);
 	}
 
 }
