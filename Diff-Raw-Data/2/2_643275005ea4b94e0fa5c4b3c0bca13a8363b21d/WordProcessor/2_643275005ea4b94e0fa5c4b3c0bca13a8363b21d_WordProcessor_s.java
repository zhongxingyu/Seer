package com.shiwaforce.nova.dp.builder;
 
 public class WordProcessor {
 
 	public void convert(final DocumentConverter dc, final Document document) {
 		dc.open();
 		dc.printAuthor(document.author);
 		dc.printTitle(document.title);
 		dc.printCatalog(document.catalog);
 		dc.printContent(document.content);
 		dc.printFooter(document.footer);
 		dc.close();
 	}
 }
