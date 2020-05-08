 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  *  contributor license agreements.  See the NOTICE file distributed with
  *  this work for additional information regarding copyright ownership.
  *  The ASF licenses this file to You under the Apache License, Version 2.0
  *  (the "License"); you may not use this file except in compliance with
  *  the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 
 /* $Id: ContentHandler.java 473841 2006-11-12 00:46:38Z gregor $  */
 
 package org.apache.lenya.search.crawler;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import websphinx.Crawler;
 import websphinx.Form;
 import websphinx.Link;
 import websphinx.LinkTransformer;
 import websphinx.Page;
 
 /**
  * 
  */
 public class SimpleCrawler extends Crawler {
 
     private String crawlRoot;
 
     private File dumpRoot;
     
     /**
      * @see websphinx.StandardClassifier
      */
    private static final String[] LINK_TYPES = {"hyperlink", "image", "code", "header-link"};
 
     public SimpleCrawler(String crawlRoot, File dumpRoot) {
         try {
             this.setRoot(new Link(crawlRoot));
             this.crawlRoot = crawlRoot.substring(0, crawlRoot.lastIndexOf("/")+1);
         } catch (MalformedURLException e) {
             this.setRoot(null);
         }
         this.dumpRoot = dumpRoot;
         this.setSynchronous(true);
         this.setDomain(Crawler.SERVER);
         this.setLinkType(LINK_TYPES);
     }
 
     public void visit(Page page) {
         System.out.println("Visiting [" + page.getURL() + "]");
         String pageURL = page.getURL().toString();
         String baseURL = this.crawlRoot;
         System.out.println("pageURL: " + pageURL);
         System.out.println("baseURL: " + baseURL);
         if (pageURL.startsWith(baseURL)) {
             File file = new File(this.dumpRoot, pageURL.substring(baseURL.length()));
             System.out.println("writing file: " + file.getAbsolutePath());
             try {
                 if (!file.exists()) {
                     file.getParentFile().mkdirs();
                     file.createNewFile();
                 }
                 LinkTransformer linkTransformer = new LinkTransformer(new FileOutputStream(file));
                 linkTransformer.setBase(page.getBase());
                 linkTransformer.writePage(page);
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         System.out.println("    Done.");
     }
 
     public static void main(String[] args) {
         SimpleCrawler crawler = new SimpleCrawler(args[0], new File(
                 "/tmp/dump"));
         crawler.run();
     }
 
 }
