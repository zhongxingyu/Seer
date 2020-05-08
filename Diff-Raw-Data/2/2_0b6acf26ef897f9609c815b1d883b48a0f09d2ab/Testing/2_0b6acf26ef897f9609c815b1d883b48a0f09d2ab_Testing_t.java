 // Copyright 2011 Waterken Inc. under the terms of the MIT X license found at
 // http://www.opensource.org/licenses/mit-license.html
 package org.k2v.test;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Random;
 
 import org.k2v.Document;
 import org.k2v.Folder;
 import org.k2v.K2V;
 import org.k2v.MissingValue;
 import org.k2v.Query;
 import org.k2v.Update;
 import org.k2v.trie.Trie;
 
 /**
  * Unit tests.
  */
 public final class Testing {
   private Testing() {}
   
   static public void
   clean(final File dir) throws Exception {
     final String[] unexpected = dir.list(new FilenameFilter() {
       public boolean accept(final File _, final String name) {
         return name.endsWith(Trie.Ext) && !new File(dir, name).delete();
       }
     });
     if (0 != unexpected.length) {
       throw new RuntimeException("unable to delete file: " + unexpected[0]);
     }
   }
   
   static public void
   test(final K2V db) throws Exception {
     testLarge(db);
     testFanout(db);
     testZigZag(db);
     testTrails(db);
   }
   
   static private void
   testLarge(final K2V db) throws Exception {
     final byte[] folderKey = { 1, 2 };
     final byte[] docKey = {};
     final Query pre = db.query();
     try {
       ((MissingValue)pre.find(pre.root, folderKey)).getClass();
     } finally {
       pre.close();
     }
     final byte[] docValue = new byte[0xFFFF];
     for (int i = docValue.length; 0 != i--;) {
       docValue[i] = (byte)i;
     }
     final Update create = db.update();
     final Folder folder;
     try {
       folder = create.nest(pre.root, folderKey);
       final OutputStream doc = create.open(folder, docKey);
       doc.write(docValue);
       doc.close();
       create.commit();
     } finally {
       create.close();
     }
     final Query post = db.query();
     try {
       final Document doc = (Document)post.find(folder, docKey);
       if (docValue.length != doc.length) { throw new RuntimeException(); }
       final BufferedInputStream buffer = new BufferedInputStream(doc);
       for (final byte head : docValue) {
         if ((head & 0xFF) != buffer.read()) { throw new RuntimeException(); }
       }
       if (-1 != buffer.read()) { throw new RuntimeException(); }
     } finally {
       post.close();
     }
     final Update delete = db.update();
     try {
       delete.open(post.root, folderKey).close();
      delete.commit();
     } finally {
       delete.close();
     }
   }
   
   static private void
   testFanout(final K2V db) throws Exception {
     final Thread[] threads = new Thread[(1 << 4) / 2];
     for (int i = threads.length; i-- != 0;) {
       final int high = i << 4;
       threads[i] = new Thread(new Runnable() {
         public void run() {
           try {
             for (int low = 1 << 4; low-- != 0;) {
               final int key = high | low;
               
               final Query pre = db.query();
               try {
                 ((MissingValue)pre.find(pre.root,
                                         new byte[] { (byte)key })).getClass();
                 ((MissingValue)pre.find(pre.root,
                                         new byte[] { (byte)~key })).getClass();
               } finally {
                 pre.close();
               }
 
               final Update update = db.update();
               try {
                 update.open(pre.root, new byte[] { (byte)key }).close();
                 final OutputStream doc =
                   update.open(pre.root, new byte[] { (byte)key });
                 doc.write(low);
                 doc.flush();
                 doc.close();
                 
                 update.open(pre.root, new byte[] { (byte)~key }).close();
 
                 update.commit();
               } finally {
                 update.close();
               }
               
               final Query post = db.query();
               try {
                 {
                   final Document doc =
                     (Document)post.find(post.root, new byte[] { (byte)key });
                   if (doc.length != 1) { throw new RuntimeException(); }
                   if (doc.read() != low) { throw new RuntimeException(); }
                 }
                 {
                   final Document doc =
                     (Document)post.find(post.root, new byte[] { (byte)~key });
                   if (doc.length != 0) { throw new RuntimeException(); }
                   if (doc.read() != -1) { throw new RuntimeException(); }
                 }
               } finally {
                 post.close();
               }
             }
           } catch (final IOException e) {
             throw new RuntimeException(e);
           }
         }
       });
       threads[i].start();
     }
     for (final Thread thread : threads) { thread.join(); }
     try {
       final Query done = db.query();
       try {
         for (int i = 0; i != 0x80; ++i) {
           final Document doc =
             (Document)done.find(done.root, new byte[] { (byte)i });
           if (doc.length != 1) { throw new RuntimeException(); }
           if (doc.read() != (i & 0x0F)) { throw new RuntimeException(); }
         }
         for (int i = 0x80; i != 0x100; ++i) {
           final Document doc =
             (Document)done.find(done.root, new byte[] { (byte)i });
           if (doc.length != 0) { throw new RuntimeException(); }
           if (doc.read() != -1) { throw new RuntimeException(); }
         }
       } finally {
         done.close();
       }
     } catch (final IOException e) {
       throw new RuntimeException(e);
     }
   }
   
   static private void
   testZigZag(final K2V db) throws Exception {
     final Folder root;
     final Query init = db.query();
     try {
       root = init.root;
     } finally {
       init.close();
     }
     final Thread[] threads = new Thread[8];
     for (int n = threads.length; 0 != n--;) {
       final byte[] key = new byte[] { (byte)n };
       threads[n] = new Thread(new Runnable() {
         public void run() {
           try {
             final Update createParent = db.update();
             final Folder parent = createParent.nest(root, key);
             createParent.commit();
             createParent.close();
             {
               final byte[] child = new byte[] { 0, 1 };
               final Update update = db.update();
               update.open(parent, child).close();
               update.nest(parent, child);
               final OutputStream tmp = update.open(parent, child);
               tmp.write(1);
               tmp.write(key);
               tmp.flush();
               tmp.close();
               update.open(parent, child).close();
               final Folder done = update.touch(parent);
               update.commit();
               update.close();                  
 
               final Query query = db.query();
               final Document doc = (Document)query.find(done, child);
               if (0 != doc.length) { throw new RuntimeException(); }
               if (-1 != doc.read()) { throw new RuntimeException(); }
               query.close();
             }
             {
               final byte[] a = new byte[] { 0, 1, 2 };
               final byte[] b = new byte[] {};
               final Update update = db.update();
               final OutputStream out = update.open(update.nest(parent, a), b);
               out.write(key);
               out.flush();
               out.close();
               final Folder done = update.touch(parent);
               update.commit();
               update.close();
               final Folder folder;
               {
                 final Query query = db.query();
                 folder = (Folder)query.find(done, a);
                 final Document doc = (Document)query.find(folder, b);
                 if (key.length != doc.length) { throw new RuntimeException(); }
                 for (int i = 0; i != key.length; ++i) {
                   if ((key[i] & 0xFF) != doc.read()) {
                     throw new RuntimeException();
                   }
                 }
                 if (-1 != doc.read()) { throw new RuntimeException(); }
                 query.close();
               }
               final Update killb = db.update();
               killb.open(folder, b).close();
               final Folder doneb = killb.touch(folder);
               killb.commit();
               killb.close();
               {
                 final Query query = db.query();
                 if (0 != ((Document)query.find(doneb, b)).length) {
                   throw new RuntimeException();
                 }
                 query.close();
               }
               final Update killa = db.update();
               killa.open(parent, a).close();
               final Folder donea = killa.touch(parent);
               killa.commit();
               killa.close();
               {
                 final Query query = db.query();
                 if (0 != ((Document)query.find(donea, a)).length) {
                   throw new RuntimeException();
                 }
                 query.close();
               }
             }
           } catch (final IOException e) {
             throw new RuntimeException(e);
           }
         }
       });
       threads[n].start();
     }
     for (final Thread thread : threads) { thread.join(); }
   }
 
   static private void
   testTrails(final K2V db) throws Exception {
     final Folder root;
     final Query init = db.query();
     try {
       root = init.root;
     } finally {
       init.close();
     }
     final Random sequence = new Random(42);
     final Thread[] threads = new Thread[128];
     for (int n = threads.length; 0 != n--;) {
       final byte[] key = new byte[2 + sequence.nextInt(7)];
       sequence.nextBytes(key);
       threads[n] = new Thread(new Runnable() {
         public void run() {
           try {
             final Update update = db.update();
             final OutputStream out = update.open(root, key);
             out.write(key);
             out.flush();
             out.close();
             final Folder done = update.touch(root);
             update.commit();
             update.close();
 
             final Query query = db.query();
             final Document doc = (Document)query.find(done, key);
             if (key.length != doc.length) { throw new RuntimeException(); }
             for (int i = 0; i != key.length; ++i) {
               if ((key[i] & 0xFF) != doc.read()) {
                 throw new RuntimeException();
               }
             }
             if (-1 != doc.read()) { throw new RuntimeException(); }
             query.close();
           } catch (final IOException e) {
             throw new RuntimeException(e);
           }
         }
       });
       threads[n].start();
     }
     for (final Thread thread : threads) { thread.join(); }
   }
 }
