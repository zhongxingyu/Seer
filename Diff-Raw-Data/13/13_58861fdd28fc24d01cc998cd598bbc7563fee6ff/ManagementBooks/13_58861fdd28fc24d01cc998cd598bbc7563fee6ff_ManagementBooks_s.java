 package com.mirantis.aminakov.bigdatacourse;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class ManagementBooks {
 	List<Book> books;
 	List<String> titles;
 	List<String> authors;
 	List<String> genres;
 	List<InputStream> texts;
 	
 	public List<Book> generateBooks() {
 		books = new ArrayList<Book>();
 		titles = new ArrayList<String>();
 		authors = new ArrayList<String>();
 		genres = new ArrayList<String>();
 		texts = new ArrayList<InputStream>();
 		
 		char[] arr = { 'a', 'b', 'c', 'd', 'e', ' ', 'f', 'g', 'h', 'i', 'j', ' ', 'k', 'l', 'm', 'n', 'o',
                 ' ', 'p', 'q', 'r', 's', 't', ' ', 'u', 'v', 'w', 'x', 'y', 'z' };
        int length = 0; 
         Random rand = new Random();
         for (int i = 0; i < 50; i++) {
         	 while (length < 10) {
         	 	 length = rand.nextInt(35);
 			 }
         	 StringBuilder strb = new StringBuilder();
              for(int j = 0; j < length; j++) {
                  strb.append(arr[rand.nextInt( arr.length )]);
                 
              }
              titles.add(strb.toString());
 		}
         for (int i = 0; i < 15; i++) {
        	 	while (length < 8) {
        	 		length = rand.nextInt(15);
        	 	}
        	 	StringBuilder strb = new StringBuilder();
             for(int j = 0; j < length; j++) {
                 strb.append(arr[rand.nextInt( arr.length )]);
             }
             authors.add(strb.toString());
 		}
         for (int i = 0; i < 7; i++) {
         	while (length < 10) {
        	 	 	length = rand.nextInt(15);
 			}
        	 	StringBuilder strb = new StringBuilder();
             for(int j = 0; j < length; j++) {
                 strb.append(arr[rand.nextInt( arr.length )]);
             }
             genres.add(strb.toString());
 		}
         for (int i = 0; i < 50; i++) {
       	 	length = rand.nextInt(200);
        	 	StringBuilder strb = new StringBuilder();
             for(int j = 0; j < length; j++) {
                 strb.append(arr[rand.nextInt( arr.length )]);
             }
             FileOutputStream fos = null;
             File file;
             try {
             	file = new File("file" + i + ".txt");
             	if(!file.exists()) {
              	   try {
 					file.createNewFile();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
                 }
 				fos = new FileOutputStream(file);
 				try {
 					fos.write(strb.toString().getBytes());  //maybe use utf8
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             FileInputStream fis = null;
             File file_r = new File("file" + i + ".txt");
     		try {
     			fis = new FileInputStream(file_r);
     		} catch (FileNotFoundException e1) {
     			// TODO Auto-generated catch block
     			e1.printStackTrace();
     		}
             texts.add(fis);
 		}
         for (int i = 0; i < 50; i++) {
         	 Book book = new Book();
              book.setTitle(titles.get(i));
              book.setAuthor(authors.get(rand.nextInt(15)));
              book.setGenre(genres.get(rand.nextInt(7)));
              book.setText(texts.get(i));
              books.add(book);
 		}
 		return books;
 	}
 	
 	public void deleteFiles() {
 		for (int del = 0; del < 50; del++) {
         	File file_del = new File("file" + del + ".txt");
         	file_del.delete();
 		}
 	}
 }
