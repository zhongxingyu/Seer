 /*
  * Copyright (C) 2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.jasdoc.util;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * Extended File class which also supports navigating the content of JAR files.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public class ExtFile extends File
 {
     /** Serial version UID. */
     private static final long serialVersionUID = 1302988882843703423L;
 
     /** The jar file. */
     private File jarFile;
 
     /** The file name in the jar. */
     private String fileInJar;
 
     /**
      * Creates a new extended file from the specified standard file.
      *
      * @param file
      *            The file.
      */
     public ExtFile(final File file)
     {
         this(file.getPath());
     }
 
     /**
      * Creates a new file from the specified path name.
      *
      * @param pathName
      *            The path name.
      */
     public ExtFile(final String pathName)
     {
         super(pathName);
         processJar();
     }
 
     /**
      * Creates a new file with the specified parent file and child file name.
      *
      * @param parent
      *            The parent file.
      * @param child
      *            The child file name.
      */
     public ExtFile(final ExtFile parent, final String child)
     {
         super(parent.isJarFile() ? new ExtFile(parent.getPath() + "!") :
             parent, child);
         processJar();
     }
 
     /**
      * Creates a new file with the specified parent file and child file name.
      *
      * @param parent
      *            The parent file.
      * @param child
      *            The child file name.
      */
     public ExtFile(final File parent, final String child)
     {
         this(new ExtFile(parent), child);
     }
 
     /**
      * Creates a new file with the specified parent file name and child file
      * name.
      *
      * @param parent
      *            The parent file name.
      * @param child
      *            The child file name.
      */
     public ExtFile(final String parent, final String child)
     {
         this(new ExtFile(parent), child);
     }
 
     /**
      * Creates a file from the specified URI.
      *
      * @param uri
      *            The URI.
      */
     public ExtFile(final URI uri)
     {
         super(fixJarUri(uri));
         processJar();
     }
 
     /**
      * Creates a file from the specified URL.
      *
      * @param url
      *            The URL.
      */
     public ExtFile(final URL url)
     {
         super(fixJarUri(urlToUri(url)));
         processJar();
     }
 
     /**
      * Converts the specified URL to an URI.
      *
      * @param url
      *            The URL.
      * @return The URI.
      */
     private static final URI urlToUri(final URL url)
     {
         try
         {
             return url.toURI();
         }
         catch (final URISyntaxException e)
         {
             throw new RuntimeException(e.toString(), e);
         }
     }
 
     /**
      * Fixes a JAR file URI so it can be parsed by the File class. If URI is not
      * a JAR URI or can not be fixed for some reason then the URI is returned
      * unchanged.
      *
      * @param uri
      *            The original URI.
      * @return The fixed URI.
      */
     private static final URI fixJarUri(final URI uri)
     {
         if (uri.getScheme().equalsIgnoreCase("jar"))
         {
             try
             {
                 return new URI(uri.toString().substring(4));
             }
             catch (final URISyntaxException e)
             {
                 return uri;
             }
         }
         return uri;
     }
 
     /**
      * Checks if file is inside a JAR file and prepares some internal variables
      * for easier handling of the JAR file.
      */
     private void processJar()
     {
         final String path = getAbsolutePath();
         final int p = path.toLowerCase().indexOf(".jar!");
         if (p >= 0)
         {
             this.jarFile = new File(path.substring(0, p + 4));
             this.fileInJar = path.substring(p + 5).replace('\\', '/')
                 .replaceAll("^/*(.*?)/*$", "$1");
         }
         else
         {
             this.fileInJar = null;
             this.jarFile = null;
         }
     }
 
     /**
      * Checks if this file is inside a JAR file.
      *
      * @return True if inside a JAR, false if not.
      */
     public boolean isInJar()
     {
         return this.jarFile != null;
     }
 
     /**
      * Returns the JAR file this file is inside of. Null if it is not in a JAR
      * file.
      *
      * @return The JAR file this file is inside of or null if not inside a JAR.
      */
     public File getJarFile()
     {
         return this.jarFile;
     }
 
     /**
      * Returns the file inside the JAR file. Null if not in a JAR file.
      *
      * @return The file inside the JAR file or null if not in a JAR.
      */
     public String getFileInJar()
     {
         return this.fileInJar;
     }
 
     /**
      * @see java.io.File#getParent()
      */
     @Override
     public String getParent()
     {
         final String parent = super.getParent();
         if (isInJar() && parent.endsWith("!"))
             return parent.substring(0, parent.length() - 1);
         else
             return parent;
     }
 
     /**
      * @see java.io.File#getParentFile()
      */
     @Override
     public ExtFile getParentFile()
     {
         return new ExtFile(getParent());
     }
 
     /**
      * @see java.io.File#isAbsolute()
      */
     @Override
     public boolean isAbsolute()
     {
         if (isInJar()) return this.jarFile.isAbsolute();
         return super.isAbsolute();
     }
 
     /**
      * @see java.io.File#getAbsoluteFile()
      */
     @Override
     public ExtFile getAbsoluteFile()
     {
         return new ExtFile(getAbsolutePath());
     }
 
     /**
      * @see java.io.File#getCanonicalFile()
      */
     @Override
     public File getCanonicalFile() throws IOException
     {
         return new ExtFile(getCanonicalPath());
     }
 
     /**
      * @see java.io.File#toURL()
      */
    @SuppressWarnings({ "deprecation", "javadoc" })
     @Deprecated
     @Override
     public URL toURL() throws MalformedURLException
     {
         if (isInJar())
             return new URL("jar", null, "file:" + getAbsolutePath());
         return super.toURL();
     }
 
     /**
      * @see java.io.File#toURI()
      */
     @Override
     public URI toURI()
     {
         if (isInJar()) try
         {
             return new URI("jar:file", null, getAbsolutePath(), null);
         }
         catch (final URISyntaxException e)
         {
             throw new RuntimeException(e.toString(), e);
         }
         return super.toURI();
     }
 
     /**
      * @see java.io.File#canRead()
      */
     @Override
     public boolean canRead()
     {
         if (isInJar()) return this.jarFile.canRead();
         return super.canRead();
     }
 
     /**
      * @see java.io.File#canWrite()
      */
     @Override
     public boolean canWrite()
     {
         if (isInJar()) return false;
         return super.canWrite();
     }
 
     /**
      * Returns the JAR entry of this file.
      *
      * @return The JAR entry. Null if not found.
      */
     private JarEntry getJarEntry()
     {
         return getJarEntry(this.fileInJar);
     }
 
     /**
      * Returns the JAR entry of the specified filename.
      *
      * @param filename
      *            The filename.
      * @return The JAR entry. Null if not found.
      */
     private JarEntry getJarEntry(final String filename)
     {
         try
         {
             final JarFile jarFile = new JarFile(this.jarFile);
             try
             {
                 return jarFile.getJarEntry(filename);
             }
             finally
             {
                 jarFile.close();
             }
         }
         catch (final IOException e)
         {
             throw new RuntimeException(e.toString(), e);
         }
     }
 
     /**
      * @see java.io.File#exists()
      */
     @Override
     public boolean exists()
     {
         if (isInJar())
         {
             if (!this.jarFile.exists()) return false;
             return getJarEntry() != null;
         }
         return super.exists();
     }
 
     /**
      * @see java.io.File#isDirectory()
      */
     @Override
     public boolean isDirectory()
     {
         if (isInJar())
         {
             if (!this.jarFile.exists()) return false;
             return getJarEntry(this.fileInJar + "/") != null;
         }
         return super.isDirectory();
     }
 
     /**
      * Checks if this file is a JAR file.
      *
      * @return True if file is a JAR file, false if not.
      */
     public boolean isJarFile()
     {
         if (isInJar()) return false;
         return getName().toLowerCase().endsWith(".jar");
     }
 
     /**
      * @see java.io.File#isFile()
      */
     @Override
     public boolean isFile()
     {
         if (isInJar())
         {
             if (!this.jarFile.exists()) return false;
             if (!exists()) return false;
             return getJarEntry(this.fileInJar + "/") == null;
         }
         return super.isFile();
     }
 
     /**
      * @see java.io.File#isHidden()
      */
     @Override
     public boolean isHidden()
     {
         if (isInJar()) return false;
         return super.isHidden();
     }
 
     /**
      * @see java.io.File#lastModified()
      */
     @Override
     public long lastModified()
     {
         if (isInJar())
         {
             final JarEntry jarEntry = getJarEntry();
             if (jarEntry == null) return 0;
             return jarEntry.getTime();
         }
         return super.lastModified();
     }
 
     /**
      * @see java.io.File#length()
      */
     @Override
     public long length()
     {
         if (isInJar())
         {
             final JarEntry jarEntry = getJarEntry();
             if (jarEntry == null) return 0;
             return jarEntry.getSize();
         }
         return super.length();
     }
 
     /**
      * @see java.io.File#createNewFile()
      */
     @Override
     public boolean createNewFile() throws IOException
     {
         if (isInJar())
         {
             if (exists()) return false;
             throw new IOException("Can't create file inside a JAR");
         }
         return super.createNewFile();
     }
 
     /**
      * @see java.io.File#delete()
      */
     @Override
     public boolean delete()
     {
         if (isInJar()) return false;
         return super.delete();
     }
 
     /**
      * @see java.io.File#deleteOnExit()
      */
     @Override
     public void deleteOnExit()
     {
         if (isInJar()) return;
         super.deleteOnExit();
     }
 
     /**
      * @see java.io.File#list()
      */
     @Override
     public String[] list()
     {
         if (isInJar())
         {
             try
             {
                 final JarFile jarFile = new JarFile(this.jarFile);
                 try
                 {
                     final Enumeration<JarEntry> entries = jarFile.entries();
                     final String dir = this.fileInJar + "/";
                     final Set<String> result = new HashSet<String>();
                     boolean isDir = false;
                     while (entries.hasMoreElements())
                     {
                         final JarEntry entry = entries.nextElement();
                         String name = entry.getName();
                         if (name.equals(dir))
                         {
                             isDir = true;
                             continue;
                         }
                         if (!name.startsWith(dir)) continue;
                         name = name.substring(dir.length());
                         if (name.endsWith("/"))
                             name = name.substring(0, name.length() - 1);
                         if (name.indexOf('/') >= 0) continue;
                         result.add(name);
                     }
                     if (!isDir) return null;
                     return result.toArray(new String[result.size()]);
                 }
                 finally
                 {
                     jarFile.close();
                 }
             }
             catch (final IOException e)
             {
                 throw new RuntimeException(e.toString(), e);
             }
         }
         return super.list();
     }
 
     /**
      * @see java.io.File#listFiles()
      */
     @Override
     public ExtFile[] listFiles()
     {
         final String[] names = list();
         if (names == null) return null;
         final int n = names.length;
         final ExtFile[] files = new ExtFile[n];
         for (int i = 0; i < n; i++)
         {
             files[i] = new ExtFile(this, names[i]);
         }
         return files;
     }
 
     /**
      * @see java.io.File#listFiles(java.io.FilenameFilter)
      */
     @Override
     public ExtFile[] listFiles(final FilenameFilter filter)
     {
         final String[] names = list(filter);
         if (names == null) return null;
         final int n = names.length;
         final ExtFile[] files = new ExtFile[n];
         for (int i = 0; i < n; i++)
         {
             files[i] = new ExtFile(this, names[i]);
         }
         return files;
     }
 
     /**
      * @see java.io.File#listFiles(java.io.FileFilter)
      */
     @Override
     public ExtFile[] listFiles(final FileFilter filter)
     {
         final String names[] = list();
         if (names == null) return null;
         final ArrayList<ExtFile> files = new ArrayList<ExtFile>();
         for (final String name: names)
         {
             final ExtFile file = new ExtFile(this, name);
             if ((filter == null) || filter.accept(file))
                 files.add(file);
         }
         return files.toArray(new ExtFile[files.size()]);
     }
 
     /**
      * @see java.io.File#mkdir()
      */
     @Override
     public boolean mkdir()
     {
         if (isInJar()) return false;
         return super.mkdir();
     }
 
     /**
      * @see java.io.File#mkdirs()
      */
     @Override
     public boolean mkdirs()
     {
         if (isInJar()) return false;
         return super.mkdirs();
     }
 
     /**
      * @see java.io.File#renameTo(java.io.File)
      */
     @Override
     public boolean renameTo(final File dest)
     {
         if (isInJar()) return false;
         return super.renameTo(dest);
     }
 
     /**
      * @see java.io.File#setLastModified(long)
      */
     @Override
     public boolean setLastModified(final long time)
     {
         if (isInJar()) return false;
         return super.setLastModified(time);
     }
 
     /**
      * @see java.io.File#setReadOnly()
      */
     @Override
     public boolean setReadOnly()
     {
         if (isInJar()) return false;
         return super.setReadOnly();
     }
 
     /**
      * @see java.io.File#setWritable(boolean, boolean)
      */
     @Override
     public boolean setWritable(final boolean writable, final boolean ownerOnly)
     {
         if (isInJar()) return false;
         return super.setWritable(writable, ownerOnly);
     }
 
     /**
      * @see java.io.File#setWritable(boolean)
      */
     @Override
     public boolean setWritable(final boolean writable)
     {
         if (isInJar()) return false;
         return super.setWritable(writable);
     }
 
     /**
      * @see java.io.File#setReadable(boolean, boolean)
      */
     @Override
     public boolean setReadable(final boolean readable, final boolean ownerOnly)
     {
         if (isInJar()) return false;
         return super.setReadable(readable, ownerOnly);
     }
 
     /**
      * @see java.io.File#setReadable(boolean)
      */
     @Override
     public boolean setReadable(final boolean readable)
     {
         if (isInJar()) return false;
         return super.setReadable(readable);
     }
 
     /**
      * @see java.io.File#setExecutable(boolean, boolean)
      */
     @Override
     public boolean setExecutable(final boolean executable,
         final boolean ownerOnly)
     {
         if (isInJar()) return false;
         return super.setExecutable(executable, ownerOnly);
     }
 
     /**
      * @see java.io.File#setExecutable(boolean)
      */
     @Override
     public boolean setExecutable(final boolean executable)
     {
         if (isInJar()) return false;
         return super.setExecutable(executable);
     }
 
     /**
      * @see java.io.File#canExecute()
      */
     @Override
     public boolean canExecute()
     {
         if (isInJar()) return false;
         return super.canExecute();
     }
 
     /**
      * @see java.io.File#getTotalSpace()
      */
     @Override
     public long getTotalSpace()
     {
         if (isInJar()) return this.jarFile.getTotalSpace();
         return super.getTotalSpace();
     }
 
     /**
      * @see java.io.File#getFreeSpace()
      */
     @Override
     public long getFreeSpace()
     {
         if (isInJar()) return this.jarFile.getFreeSpace();
         return super.getFreeSpace();
     }
 
     /**
      * @see java.io.File#getUsableSpace()
      */
     @Override
     public long getUsableSpace()
     {
         if (isInJar()) return this.jarFile.getUsableSpace();
         return super.getUsableSpace();
     }
 
     /**
      * Opens an input stream to this file. For a normal file it simply returns a
      * FileInputStream. For a Jar entry file it opens the input stream for the
      * Jar Entry instead.
      *
      * @return The input stream. Remember to close it when you no longer need
      *         it.
      * @throws IOException
      *             When input stream could not be opened.
      */
     public InputStream openInputStream() throws IOException
     {
         if (isInJar())
         {
             final JarFile jarFile = new JarFile(this.jarFile);
             return jarFile.getInputStream(jarFile
                 .getJarEntry(this.fileInJar));
         }
         else
         {
             return new FileInputStream(this);
         }
     }
 }
