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
 package com.github.sgelb.springerlinkdownloader.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 
 import com.github.sgelb.springerlinkdownloader.helper.GuiErrorMessage;
 import com.github.sgelb.springerlinkdownloader.helper.NoAccessException;
 import com.github.sgelb.springerlinkdownloader.model.Book;
 import com.github.sgelb.springerlinkdownloader.model.Parser;
 import com.github.sgelb.springerlinkdownloader.model.Pdf;
 import com.itextpdf.text.DocumentException;
 
 public class Worker extends SwingWorker<Void, Integer> {
 
 	private Book book = new Book();
 	private JTextField urlField;
 	private JProgressBar progressBar;
 	private File saveFolder;
 	private JLabel progressText;
 	private JButton startBtn;
 	private JButton browseBtn;
 	private Pdf pdf;
 	private TreeMap<String, URL> chapters = new TreeMap<>();
 
 	public Worker(JTextField urlField, JLabel saveFolderLabel,
 			JProgressBar progressBar, JLabel progressBarLabel,
 			JButton startBtn, JButton browseBtn) {
 
 		this.urlField = urlField;
 		this.progressBar = progressBar;
 		this.saveFolder = new File(saveFolderLabel.getText());
 		this.progressText = progressBarLabel;
 		this.startBtn = startBtn;
 		this.browseBtn = browseBtn;
 	}
 
 	protected Void doInBackground() {
 		if (!isCancelled()) {
 			browseBtn.setEnabled(false);
 			startBtn.setEnabled(false);
 			urlField.setEditable(false);
 
			progressText.setEnabled(true);
 			progressText.setText("Parsing page…");
 			progressBar.setIndeterminate(true);
 
 			Parser parsePage = new Parser(urlField.getText(), book);
 			try {
 				parsePage.parseHtml();
 				parsePage.setBookData();
 			} catch (NoAccessException | IOException e) {
 				GuiErrorMessage.show(this, progressBar, e);
 			}
 
 			chapters = book.getChapters();
 		}
 
 		if (!isCancelled()) {
 			progressText.setText("Downloading files…");
 			progressBar.setIndeterminate(false);
 			progressBar.setMaximum(chapters.size());
 
 			try {
 				pdf = new Pdf(book, saveFolder);
 			} catch (IOException e) {
 				GuiErrorMessage.show(this, progressBar, e);
 			}
 			Integer count = 1;
 			for (Entry<String, URL> chapter : chapters.entrySet()) {
 				if (!isCancelled()) {
 					try {
 						pdf.download(chapter);
 					} catch (IOException e) {
 						GuiErrorMessage.show(this, progressBar, e);
 					}
 				}
 				progressBar
 						.setString("[" + count + "/" + chapters.size() + "]");
 				progressBar.setValue(count++);
 			}
 			progressBar.setString("");
 		}
 
 		if (!isCancelled()) {
 			progressText.setText("Merging files…");
 			try {
 				pdf.create();
 			} catch (DocumentException | IOException e) {
 				GuiErrorMessage.show(this, progressBar, e);
 			}
 		}
 
 		if (isCancelled()) {
 			progressText.setText("Cancelled.");
 			progressBar.setValue(0);
 			if (book.getInfo("saveFile") != null) {
 				File saveFile = new File(book.getInfo("saveFile"));
 				if (saveFile.exists()) {
 					saveFile.delete();
 				}
 			}
 		} else {
 			progressText.setText("Created " + book.getInfo("saveFile") + ".");
 		}
 
 		progressBar.setIndeterminate(false);
 		browseBtn.setEnabled(true);
 		startBtn.setEnabled(true);
 		urlField.setEditable(true);
 		pdf.deleteTemp();
 		return null;
 	}
 
 	protected void done() {
 		try {
 			super.get();
 		} catch (CancellationException e) {
 		} catch (InterruptedException | ExecutionException e) {
 			GuiErrorMessage.show(this, progressBar, e);
 		}
 	}
 
 }
