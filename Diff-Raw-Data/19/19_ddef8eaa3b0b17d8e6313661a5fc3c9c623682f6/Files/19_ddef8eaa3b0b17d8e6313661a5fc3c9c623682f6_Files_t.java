 /*
  * Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.rjeschke.neetutils.io;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import com.github.rjeschke.neetutils.Once;
 import com.github.rjeschke.neetutils.Strings;
 import com.github.rjeschke.neetutils.SysUtils;
 import com.github.rjeschke.neetutils.collections.Colls;
 import com.github.rjeschke.neetutils.rng.RNG;
 import com.github.rjeschke.neetutils.rng.RNGFactory;
 import com.github.rjeschke.neetutils.rng.RNGType;
 
 public final class Files implements Runnable
 {
     private final static ConcurrentLinkedQueue<File> TEMP_FOLDERS = new ConcurrentLinkedQueue<>();
 
     private Files()
     {
         //
     }
 
     static
     {
         Runtime.getRuntime().addShutdownHook(new Thread(new Files(), "neetutils-files-cleaner"));
     }
 
     /**
      * Recursively lists all files at the given path.
      *
      * @param parent
      *            Path.
      * @return List of files.
      */
     public final static List<File> listFiles(final File parent)
     {
         final List<File> files = Colls.list();
         listFiles(parent, files);
         return files;
     }
 
     private final static void listFiles(final File parent, final List<File> files)
     {
         if (parent.isFile())
         {
             files.add(parent);
         }
         else
         {
             final File[] fs = parent.listFiles();
            if (fs != null)
             {
                for (final File f : fs)
                 {
                    if (f.isFile())
                    {
                        files.add(f);
                    }
                    else
                    {
                        listFiles(f, files);
                    }
                 }
             }
         }
     }
 
     public final static String getExtension(final File file)
     {
         return getExtension(file.getName());
     }
 
     public final static String getExtension(final String filename)
     {
         final int idx = filename.lastIndexOf('.');
         return idx != -1 ? filename.substring(idx + 1) : "";
     }
 
     public final static String getFullExtension(final File file)
     {
         return getFullExtension(file.getName());
     }
 
     public final static String getFullExtension(final String filename)
     {
         final int idx = filename.lastIndexOf('.');
         return idx != -1 ? filename.substring(idx) : "";
     }
 
     public final static File createUniqueTempFolder()
     {
         return createUniqueTempFolder("neetutils", true);
     }
 
     public final static File createUniqueTempFolder(final String prefix, final boolean autoCleanup)
     {
         final String tmp = System.getProperty("java.io.tmpdir");
         final RNG rnd = RNGFactory.create(RNGType.LCG);
         for (int i = 0; i < 0x40000000; i++)
         {
             final File t = new File(tmp, String.format("%s-tmp-%08x", prefix, rnd.nextInt()));
             if (!t.exists())
             {
                 if (t.mkdir())
                 {
                     if (autoCleanup) TEMP_FOLDERS.offer(t);
                     return t;
                 }
             }
         }
 
         throw new RuntimeException("Could not create unique temp folder, please clean up your /tmp.");
     }
 
     public final static void recurseDeleteFolder(final File path)
     {
         if (path.isDirectory())
         {
             final File[] files = path.listFiles();
             for (final File f : files)
             {
                 if (f.isDirectory())
                 {
                     recurseDeleteFolder(f);
                 }
                 else
                 {
                     f.delete();
                 }
             }
             path.delete();
         }
         else
         {
             path.delete();
         }
     }
 
     public final static void copy(final File input, final File output) throws IOException
     {
         try (final InputStream in = new FileInputStream(input))
         {
             try (final OutputStream out = new FileOutputStream(output))
             {
                 copy(in, out);
             }
         }
     }
 
     public final static void copy(final InputStream in, final File output) throws IOException
     {
         try (final OutputStream out = new FileOutputStream(output))
         {
             copy(in, out);
         }
     }
 
     public final static void copy(final File input, final OutputStream out) throws IOException
     {
         try (final InputStream in = new FileInputStream(input))
         {
             copy(in, out);
         }
     }
 
     public final static void copy(final InputStream input, final OutputStream output) throws IOException
     {
         final byte[] buffer = new byte[65536];
 
         for (;;)
         {
             final int read = input.read(buffer);
             if (read == -1) break;
             output.write(buffer, 0, read);
         }
     }
 
     public final static byte[] asBytes(final String filename) throws IOException
     {
         return asBytes(new File(filename));
     }
 
     public final static byte[] asBytes(final File file) throws IOException
     {
         try (final FileInputStream fis = new FileInputStream(file))
         {
             final byte[] buffer = new byte[(int)file.length()];
             int p = 0;
             while (p < buffer.length)
             {
                 final int r = fis.read(buffer, p, buffer.length - p);
                 if (r == -1) break;
                 p += r;
             }
             if (p != buffer.length) throw new IOException("Unexpected end of stream, expected " + buffer.length + ", got " + p + " bytes");
             return buffer;
         }
     }
 
     public final static byte[] asBytes(final InputStream in) throws IOException
     {
         byte[] buffer = new byte[65536];
         try
         {
             int p = 0;
             for (;;)
             {
                 final int r = in.read(buffer, p, buffer.length - p);
                 if (r < 0) break;
                 p += r;
                 if (p >= buffer.length) buffer = Arrays.copyOf(buffer, buffer.length + 65536);
             }
             return p != buffer.length ? Arrays.copyOf(buffer, p) : buffer;
         }
         finally
         {
             in.close();
         }
     }
 
     public final static String asString(final String filename, final String charsetName) throws IOException
     {
         return new String(asBytes(filename), charsetName);
     }
 
     public final static String asString(final File file, final String charsetName) throws IOException
     {
         return new String(asBytes(file), charsetName);
     }
 
     public final static String asString(final InputStream in, final String charsetName) throws IOException
     {
         return new String(asBytes(in), charsetName);
     }
 
     public final static void saveBytes(final String filename, final byte[] bytes) throws IOException
     {
         saveBytes(filename, bytes, 0, bytes.length);
     }
 
     public final static void saveBytes(final String filename, final byte[] bytes, final int offs, final int len) throws IOException
     {
         saveBytes(new File(filename), bytes, offs, len);
     }
 
     public final static void saveBytes(final File file, final byte[] bytes) throws IOException
     {
         saveBytes(file, bytes, 0, bytes.length);
     }
 
     public final static void saveBytes(final File file, final byte[] bytes, final int offs, final int len) throws IOException
     {
         try (final FileOutputStream fos = new FileOutputStream(file))
         {
             fos.write(bytes, offs, len);
         }
     }
 
     /**
      * Lists all resources in the given package.
      *
      * @param pkgName
      *            The package name
      * @return List of all resources inside the package (recursively)
      */
     public final static List<String> list(final String pkgName)
     {
         final URL url = SysUtils.class.getResource("/" + pkgName.replace('.', '/'));
         if (url == null) return null;
 
         if (url.getProtocol().equals("jar")) return getJars(url, pkgName.replace('.', '/'));
 
         return getFiles(url.getPath(), pkgName);
     }
 
     /**
      * Gets a list of all resources on the classpath.
      *
      * @return A list of Strings.
      */
     public final static List<String> getFilesOnClasspath()
     {
         final ArrayList<String> ret = new ArrayList<>();
         final char sep = System.getProperty("path.separator").charAt(0);
         final List<String> paths = Strings.split(System.getProperty("java.class.path"), sep);
 
         for (final String path : paths)
         {
             final File file = new File(path);
             if (file.isDirectory())
             {
                 final List<File> files = listFiles(file);
                 final int cut = file.toString().length();
                 for (final File f : files)
                 {
                     ret.add(f.toString().substring(cut).replace('\\', '/'));
                 }
             }
             else if (file.isFile() && file.getName().toLowerCase().endsWith(".jar"))
             {
                 ret.addAll(getJarFiles(file));
             }
         }
         return ret;
     }
 
     private final static List<String> getFiles(final String path, final String basePackage)
     {
         final ArrayList<String> classes = new ArrayList<>();
         final String basePath = "/" + basePackage.replace('.', '/') + "/";
         final File dir = new File(path).getAbsoluteFile();
         final File[] files = dir.listFiles();
         for (final File f : files)
         {
             final String fn = f.getName();
             if (f.isDirectory())
             {
                 classes.addAll(getFiles(new File(dir, fn).getAbsolutePath(), basePackage + "." + fn));
             }
             else if (f.isFile())
             {
                 classes.add(basePath + fn);
             }
         }
         return classes;
     }
 
     private final static List<String> getJarFiles(final File file)
     {
         final ArrayList<String> classes = new ArrayList<>();
         try
         {
             try (final JarFile jar = new JarFile(file))
             {
                 final Enumeration<JarEntry> j = jar.entries();
                 while (j.hasMoreElements())
                 {
                     final JarEntry je = j.nextElement();
                     if (!je.isDirectory())
                     {
                         classes.add("/" + je.getName());
                     }
                 }
             }
         }
         catch (final Exception e)
         {
             e.printStackTrace();
             return null;
         }
         return classes;
     }
 
     private final static List<String> getJars(final URL furl, final String pkgname)
     {
         final ArrayList<String> classes = new ArrayList<>();
         try
         {
             final URL url = new URL(furl.getPath());
             final String f = URLDecoder.decode(url.getFile(), System.getProperty("file.encoding"));
             final File dir = new File(f.substring(0, f.lastIndexOf('!')));
             try (final JarFile jar = new JarFile(dir))
             {
                 final Enumeration<JarEntry> j = jar.entries();
                 while (j.hasMoreElements())
                 {
                     final JarEntry je = j.nextElement();
                     if (!je.isDirectory())
                     {
                         if (je.getName().startsWith(pkgname))
                         {
                             classes.add("/" + je.getName());
                         }
                     }
                 }
             }
         }
         catch (final Exception e)
         {
             e.printStackTrace();
             return null;
         }
         return classes;
     }
 
     public final static String home(final String path)
     {
         return new File(System.getProperty("user.home"), path).getAbsolutePath();
     }
 
     public final static File home(final File path)
     {
         return new File(new File(System.getProperty("user.home")), path.toString());
     }
 
     public final static File home()
     {
         return new File(System.getProperty("user.home"));
     }
 
     public final static String cwd(final String path)
     {
         return new File(System.getProperty("user.dir"), path).getAbsolutePath();
     }
 
     public final static File cwd(final File path)
     {
         return new File(new File(System.getProperty("user.dir")), path.toString());
     }
 
     public final static File cwd()
     {
         return new File(System.getProperty("user.dir"));
     }
 
     public final static File normalize(final File file)
     {
         final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
         final char sep = System.getProperty("file.separator").charAt(0);
 
         final List<String> toks = Strings.split(file.toString(), sep);
         final LinkedList<String> stack = new LinkedList<>();
 
         for (int i = 0; i < toks.size(); i++)
         {
             final String t = toks.get(i);
             if (Strings.isEmpty(t))
             {
                 if (i == 0 && !isWindows)
                 {
                     stack.addLast(t);
                 }
             }
             else
             {
                 if (!t.equals("."))
                 {
                     if (t.equals("..") && !stack.isEmpty())
                     {
                         stack.removeLast();
                     }
                     else
                     {
                         stack.addLast(t);
                     }
                 }
             }
         }
 
         final StringBuilder sb = new StringBuilder();
 
         final Once<String> once = Once.of("", System.getProperty("file.separator"));
 
         for (final String s : stack)
         {
             sb.append(once.get());
             sb.append(s);
         }
 
         return new File(sb.toString());
     }
 
     public final static File relativeTo(final File root, final File child)
     {
         final List<String> rt = Strings.split(Files.normalize(new File(Strings.replace(root.getAbsolutePath(), '\\', '/'))).getAbsolutePath(), '/');
         final List<String> ct = Strings.split(Files.normalize(new File(Strings.replace(child.getAbsolutePath(), '\\', '/'))).getAbsolutePath(), '/');
 
         int i = 0;
         final int len = Math.min(rt.size(), ct.size());
         while (i < len && rt.get(i).equals(ct.get(i)))
         {
             ++i;
         }
 
         if (i == 0)
         {
             return Files.normalize(child.getAbsoluteFile());
         }
 
         final StringBuilder sb = new StringBuilder();
 
         while (i < rt.size())
         {
             sb.append("../");
             ++i;
         }
 
         final Once<String> once = Once.of("", "/");
 
         for (int n = i; n < ct.size(); n++)
         {
             sb.append(once.get());
             sb.append(ct.get(n));
         }
 
         return new File(sb.toString());
     }
 
     public final static File absolute(final File parent, final File possibleChild)
     {
         return possibleChild.isAbsolute() ? possibleChild : new File(parent, possibleChild.toString());
     }
 
     @Override
     public void run()
     {
         File file;
         while ((file = TEMP_FOLDERS.poll()) != null)
             recurseDeleteFolder(file);
     }
 
 }
