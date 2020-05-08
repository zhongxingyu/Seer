 package com.neopets;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 
 import org.apache.http.client.CookieStore;
 import org.apache.http.impl.client.BasicCookieStore;
 
 public class CookieJar {
   private File file;
 
   public CookieJar(File file) {
     this.file = file;
   }
 
   public CookieJar(String path) {
     this(new File(path));
   }
 
   public CookieStore load() throws IOException {
     if (!file.exists()) {
       return new BasicCookieStore();
     }
 
     InputStream is = new FileInputStream(file);
     ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is));
     try {
       Object object = ois.readObject();
       if (object instanceof CookieStore) {
         return (CookieStore) object;
       }
       else {
         return new BasicCookieStore();
       }
     }
     catch (ClassNotFoundException e) {
       return new BasicCookieStore();
     }
     finally {
       ois.close();
     }
   }
 
   public void save(CookieStore cookieStore) throws IOException {
     OutputStream os = new FileOutputStream(file);
     ObjectOutputStream oss = new ObjectOutputStream(new BufferedOutputStream(os));
     try {
       oss.writeObject(cookieStore);
     }
     finally {
       oss.close();
     }
   }
 }
