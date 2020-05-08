 /*******************************************************************************
  * Copyright (C) 2012 p000ison
  *
  * This work is licensed under the Creative Commons
  * Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of
  * this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send
  * a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco,
  * California, 94105, USA.
  ******************************************************************************/
 
 package com.p000ison.dev.copybooks.util;
 
 import com.p000ison.dev.copybooks.CopyBooks;
 import com.p000ison.dev.copybooks.api.CraftWrittenBook;
 import com.p000ison.dev.copybooks.api.InvalidBookException;
 import com.p000ison.dev.copybooks.api.WrittenBook;

import org.spout.nbt.CompoundMap;
 import org.spout.nbt.CompoundTag;
 import org.spout.nbt.ListTag;
 import org.spout.nbt.StringTag;
 import org.spout.nbt.stream.NBTInputStream;
 import org.spout.nbt.stream.NBTOutputStream;
 
 import java.io.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents a BookIO
  */
 public final class BookIO {
 
 
     public static void writeBook(List<String> book, Writer writer) throws IOException
     {
 
         for (String page : book) {
             writer.write(page + "\n");
         }
 
         writer.flush();
         writer.close();
     }
 
     public static boolean writeNBTBook(WrittenBook book, FileOutputStream fileOutputStream) throws IOException
     {
         NBTOutputStream outputStream;
 
         try {
             outputStream = new NBTOutputStream(fileOutputStream);
 
            CompoundMap contents = new CompoundMap();
 
            contents.put(new StringTag("title", book.getTitle()));
            contents.put(new StringTag("author", book.getAuthor()));
 
             List<StringTag> tagPages = new ArrayList<StringTag>();
 
             for (String page : book.getPages()) {
                 tagPages.add(new StringTag("page", page));
             }
 
             ListTag<StringTag> pages = new ListTag<StringTag>("pages", StringTag.class, tagPages);
 
            contents.put(pages);
 
             CompoundTag root = new CompoundTag("Book", contents);
 
             outputStream.writeTag(root);
 
             outputStream.close();
 
             return true;
         } catch (IOException e) {
             CopyBooks.debug("Failed at writing book: " + book.getTitle(), e);
             return false;
         }
     }
 
    @SuppressWarnings("unchecked")
	public static WrittenBook readNBTBook(FileInputStream fileInputStream) throws IOException
     {
         NBTInputStream inputStream;
         try {
             inputStream = new NBTInputStream(fileInputStream);
 
             CraftWrittenBook book = new CraftWrittenBook();
 
 
             CompoundTag root = (CompoundTag) inputStream.readTag();
 
             if (root == null) {
            	inputStream.close();
                 return null;
             }
 
            CompoundMap contents = root.getValue();
 
             if (contents == null) {
            	inputStream.close();
                 return null;
             }
 
             StringTag title = (StringTag) contents.get(0);
             StringTag author = (StringTag) contents.get(1);
             ListTag<StringTag> pages = (ListTag<StringTag>) contents.get(2);
 
             List<String> convertedPages = new ArrayList<String>();
 
             for (StringTag page : pages.getValue()) {
                 convertedPages.add(page.getValue());
             }
 
 
             book.setTitle(title.getValue());
             book.setAuthor(author.getValue());
             book.setPages(convertedPages);
 
             inputStream.close();
 
             return book;
         } catch (IOException e) {
             CopyBooks.debug("Failed at reading book", e);
             return null;
         } catch (Exception e) {
             CopyBooks.debug("Failed at reading book", e);
             return null;
         }
     }
 
     public static List<String> readBook(BufferedReader reader) throws IOException, InvalidBookException
     {
         List<String> list = new ArrayList<String>();
 
         String page;
 
         while ((page = reader.readLine()) != null) {
             list.add(page);
         }
 
 
         reader.close();
         return list;
     }
 
     /**
      * Reads pages from a stream unformated  and adds them to a list
      *
      * @param reader The input stream to reate the pages from
      * @return The created pages from this stream
      * @throws IOException
      */
     public static List<String> readUnformattedBook(Reader reader) throws IOException
     {
         char[] buffer = new char[256];
         StringBuilder sb = new StringBuilder();
         List<String> pages = new ArrayList<String>();
 
         while (reader.read(buffer) != -1) {
             sb.append(buffer);
         }
 
         String[] lines = sb.toString().split("\\r?\\n");
 
         StringBuilder stringBuilder = new StringBuilder();
 
         for (String line : lines) {
             stringBuilder.append(line);
         }
 
         String[] stringPages = Helper.splitByLength(stringBuilder.toString(), 256);
 
         for (int i = 0; i < stringPages.length; i++) {
 
             if (i == 50) {
                 break;
             }
 
             pages.add(stringPages[i]);
         }
 
         reader.close();
 
         return pages;
     }
 
     /**
      * Reads pages from a website unformated  and adds them to a list
      *
      * @param url The input url to read the pages from
      * @return The created pages from this website
      * @throws IOException
      */
     public static List<String> readBookUnformattedFromURL(String url) throws IOException
     {
         URL realURL = new URL(url);
         InputStream is;
         BufferedReader reader;
 
         is = realURL.openStream();
         reader = new BufferedReader(new InputStreamReader(is));
 
         return readUnformattedBook(reader);
     }
 }
