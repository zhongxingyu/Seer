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
 package com.github.sgelb.springerlinkdownloader;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 
 import javax.swing.SwingUtilities;
 
 import com.github.sgelb.springerlinkdownloader.helper.Clipboard;
 import com.github.sgelb.springerlinkdownloader.model.Book;
 import com.github.sgelb.springerlinkdownloader.model.Parser;
 import com.github.sgelb.springerlinkdownloader.model.Pdf;
 import com.github.sgelb.springerlinkdownloader.view.Gui;
 import com.itextpdf.text.DocumentException;
 
 public class SpringerLinkDownloader {
 
 	public void runCLI() {
 
 		// url to book
 		// http://link.springer.com/book/${doi}/[page/1]
 		// doi = ${prefix}/${onlineISBN}
 		// /page/1 is optional
 		// String url =
 		// "http://link.springer.com/book/10.1007/978-3-8348-9931-6";
		
		// No subtitle and two authors
		// String url = "http://link.springer.com/book/10.1007/978-1-4614-3262-3";
 		
 		String url = Clipboard.getUrlfromClipboard();
 		File saveFolder = new File(System.getProperty("user.home"));
 		boolean delTmpPdfs = true;
 
 		// begin menu
 
 		System.out.println("SpringerLink Downloader");
 		Scanner scanner = new Scanner(System.in);
 		System.out.println("Enter url:");
 		if (url != null) {
 			System.out.print("[" + url + "]\n> ");
 		} else {
 			System.out.print("> ");
 		}
 		String tmp = scanner.nextLine().trim();
 		if (!tmp.isEmpty()) url = tmp;
 
 		System.out.print("Enter save folder ["
 				+ System.getProperty("user.home") + "]\n> ");
 		tmp = scanner.nextLine().trim();
 		if (!tmp.isEmpty()) saveFolder = new File(tmp);
 
 		System.out.print("Delete temporary files? [Yn]\n> ");
 		if (scanner.nextLine().trim().equalsIgnoreCase("n")) {
 			delTmpPdfs = false;
 		}
 		scanner.close();
 
 		// end menu
 
 		Book book = new Book();
 		Parser parsePage = new Parser(url, book);
 		parsePage.run();
 		Pdf pdf = new Pdf(book, saveFolder);
 
 		pdf.download(delTmpPdfs);
 		try {
 			pdf.create();
 		} catch (DocumentException | IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void runGUI() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				Gui gb = new Gui();
 				gb.run();
 			}
 		});
 	}
 
 	public static void main(String[] args) {
 		SpringerLinkDownloader sl = new SpringerLinkDownloader();
 		sl.runCLI();
		//sl.runGUI();
 	}
 
 }
