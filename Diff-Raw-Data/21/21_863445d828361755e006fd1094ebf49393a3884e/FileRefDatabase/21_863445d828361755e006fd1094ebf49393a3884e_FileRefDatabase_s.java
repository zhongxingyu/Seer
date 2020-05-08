 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.storage.fs;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Map;
 
 import org.geogit.api.ObjectId;
 import org.geogit.api.Platform;
 import org.geogit.api.plumbing.ResolveGeogitDir;
 import org.geogit.storage.RefDatabase;
 
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 import com.google.common.io.Files;
 import com.google.inject.Inject;
 
 /**
  * Provides an implementation of a GeoGit ref database that utilizes the file system for the storage
  * of refs.
  */
 public class FileRefDatabase implements RefDatabase {
 
     private static final Charset CHARSET = Charset.forName("UTF-8");
 
     private Platform platform;
 
     /**
      * Constructs a new {@code FileRefDatabase} with the given platform.
      * 
      * @param platform the platform to use
      */
     @Inject
     public FileRefDatabase(Platform platform) {
         this.platform = platform;
     }
 
     /**
      * Creates the reference database.
      */
     @Override
     public void create() {
         URL envHome = new ResolveGeogitDir(platform).call();
         if (envHome == null) {
             throw new IllegalStateException("Not inside a geogit directory");
         }
         if (!"file".equals(envHome.getProtocol())) {
             throw new UnsupportedOperationException(
                     "This References Database works only against file system repositories. "
                             + "Repository location: " + envHome.toExternalForm());
         }
         File repoDir;
         try {
             repoDir = new File(envHome.toURI());
         } catch (URISyntaxException e) {
             throw Throwables.propagate(e);
         }
         File refs = new File(repoDir, "refs");
         if (!refs.exists() && !refs.mkdir()) {
             throw new IllegalStateException("Cannot create refs directory '"
                     + refs.getAbsolutePath() + "'");
         }
     }
 
     /**
      * Closes the reference database.
      */
     @Override
     public void close() {
         // nothing to close
     }
 
     /**
      * @param name the name of the ref (e.g. {@code "refs/remotes/origin"}, etc).
      * @return the ref, or {@code null} if it doesn't exist
      */
     @Override
     public String getRef(String name) {
         checkNotNull(name);
         File refFile = toFile(name);
         if (!refFile.exists()) {
             return null;
         }
         String value = readRef(refFile);
         if (value == null) {
             return null;
         }
         try {
             ObjectId.valueOf(value);
         } catch (IllegalArgumentException e) {
             throw e;
         }
         return value;
     }
 
     /**
      * @param name the name of the symbolic ref (e.g. {@code "HEAD"}, etc).
      * @return the ref, or {@code null} if it doesn't exist
      */
     @Override
     public String getSymRef(String name) {
         checkNotNull(name);
         File refFile = toFile(name);
         if (!refFile.exists()) {
             return null;
         }
         String value = readRef(refFile);
         if (value == null) {
             return null;
         }
         if (!value.startsWith("ref: ")) {
             throw new IllegalArgumentException(name + " is not a symbolic ref: '" + value + "'");
         }
         return value.substring("ref: ".length());
     }
 
     /**
      * @param refName the name of the ref
      * @param refValue the value of the ref
      * @return {@code null} if the ref didn't exist already, its old value otherwise
      */
     @Override
     public void putRef(String refName, String refValue) {
         checkNotNull(refName);
         checkNotNull(refValue);
         try {
             ObjectId.forString(refValue);
         } catch (IllegalArgumentException e) {
             throw e;
         }
         store(refName, refValue);
     }
 
     /**
      * @param name the name of the symbolic ref
      * @param val the value of the symbolic ref
      * @return {@code null} if the ref didn't exist already, its old value otherwise
      */
     @Override
     public void putSymRef(String name, String val) {
         checkNotNull(name);
         checkNotNull(val);
         val = "ref: " + val;
         store(name, val);
     }
 
     /**
      * @param refName the name of the ref to remove (e.g. {@code "HEAD"},
      *        {@code "refs/remotes/origin"}, etc).
      * @return the value of the ref before removing it, or {@code null} if it didn't exist
      */
     @Override
     public String remove(String refName) {
         checkNotNull(refName);
         File refFile = toFile(refName);
         String oldRef;
         if (refFile.exists()) {
             oldRef = readRef(refFile);
             if (!refFile.delete()) {
                 throw new RuntimeException("Unable to delete ref file '"
                         + refFile.getAbsolutePath() + "'");
             }
         } else {
             oldRef = null;
         }
         return oldRef;
     }
 
     /**
      * @param refPath
      * @return
      */
     private File toFile(String refPath) {
         URL envHome = new ResolveGeogitDir(platform).call();
 
         String[] path = refPath.split("/");
 
         try {
             File file = new File(envHome.toURI());
             for (String subpath : path) {
                 file = new File(file, subpath);
             }
             return file;
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
     }
 
     private String readRef(File refFile) {
         try {
             synchronized (refFile.getCanonicalPath().intern()) {
                 return Files.readFirstLine(refFile, CHARSET);
             }
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
     }
 
     private void store(String refName, String refValue) {
         final File refFile = toFile(refName);
         final File tmpFile = new File(refFile.getParentFile(), "." + refFile.getName() + ".tmp");
 
         try {
             Files.createParentDirs(tmpFile);
             Files.write(refValue + "\n", tmpFile, CHARSET);
             boolean renamed;
             synchronized (refFile.getCanonicalPath().intern()) {
                 renamed = tmpFile.renameTo(refFile);
             }
             checkState(renamed, "unable to save ref " + refName);
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
     }
 
     /**
      * @return all known references under the "refs" namespace (i.e. not top level ones like HEAD,
      *         etc), key'ed by ref name
      */
     @Override
     public Map<String, String> getAll() {
         File refsRoot;
         try {
             URL envHome = new ResolveGeogitDir(platform).call();
             refsRoot = new File(new File(envHome.toURI()), "refs");
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
         Map<String, String> refs = Maps.newTreeMap();
         findRefs(refsRoot, "refs/", refs);
         return ImmutableMap.copyOf(refs);
     }
 
     private void findRefs(File refsDir, String prefix, Map<String, String> target) {
 
         File[] children = refsDir.listFiles();
         for (File f : children) {
             if (f.isDirectory()) {
                 findRefs(f, prefix + f.getName() + "/", target);
             } else if (!f.getName().startsWith(".")) {
                 String refName = prefix + f.getName();
                 String refValue = readRef(f);
                 target.put(refName, refValue);
             }
         }
     }
 
 }
