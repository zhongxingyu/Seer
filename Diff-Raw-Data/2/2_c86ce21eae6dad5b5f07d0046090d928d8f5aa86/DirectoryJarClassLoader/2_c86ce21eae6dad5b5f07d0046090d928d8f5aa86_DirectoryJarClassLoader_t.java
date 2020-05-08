 /**
  * 
  */
 package org.dotplot.util;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 /**
  * Instances of this class provide on the fly class loading mechanisms for
  * classes storred in jarfiles in a given directory.
  * 
  * @author Christian Gerhardt <case42@gmx.net>
 * @version 1.0
  */
 public class DirectoryJarClassLoader extends ClassLoader {
 
     /**
      * The associated directory of the <code>DirectoryJarClassLoader</code>.
      */
     private File directory;
 
     /**
      * Creates a new <code>DirectoryJarClassLoader</code> object.
      * 
      * @param directory
      *            - The directory associated with the
      *            <code>DirectoryJarClassLoader</code>.
      * @throws IllegalArgumentException
      *             if <code>directory</code> isn't a directory or doesn't exist.
      */
     public DirectoryJarClassLoader(File directory) {
 	super();
 	if (directory == null) {
 	    throw new NullPointerException();
 	}
 	if (!(directory.isDirectory() && directory.exists())) {
 	    throw new IllegalArgumentException(
 		    "The assigned argument must be an existing directory");
 	}
 
 	this.directory = directory;
     }
 
     /**
      * Creates a new <code>DirectoryJarClassLoader</code> object.
      * 
      * @param directory
      *            - The directory associated with the
      *            <code>DirectoryJarClassLoader</code>.
      * @param loader
      *            - The parent <code>ClassLoader</code> of the the
      *            <code>DirectoryJarClassLoader</code>.
      * @throws IllegalArgumentException
      *             if <code>directory</code> isn't a directory or doesn't exist.
      */
     public DirectoryJarClassLoader(File directory, ClassLoader loader) {
 	super(loader);
 	if (directory == null) {
 	    throw new NullPointerException();
 	}
 	if (!(directory.isDirectory() && directory.exists())) {
 	    throw new IllegalArgumentException(
 		    "The assigned argument must be an existing directory");
 	}
 
 	this.directory = directory;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.ClassLoader#findClass(java.lang.String)
      */
     @Override
     protected Class<?> findClass(String name) throws ClassNotFoundException {
 	byte[] b = loadClassData(name);
 	if (b == null) {
 	    throw new ClassNotFoundException(name);
 	}
 	return defineClass(name, b, 0, b.length);
     }
 
     /**
      * Returns the <code>File</code> object representing the
      * <code>DirectoryJarClassLoader</code>'s associated directory.
      * 
      * @return - The <code>File</code> representing the directory.
      */
     public File getDirectory() {
 	return this.directory;
     }
 
     /**
      * Loads the class data. All jar-files of the associated directory are
      * searched for the wanted class. The first hit is loaded.
      * 
      * @param name
      *            - The name of the class to be loaded.
      * @return - The binary data of the class.
      */
     private byte[] loadClassData(String name) {
 	JarFile jarFile;
 	ZipEntry entry;
 
 	// die jar dateien in dem angegebenen verzeichnis auflisten.
 	File[] jarList = this.directory.listFiles(new FilenameFilter() {
 
 	    public boolean accept(File arg0, String arg1) {
 		return arg1.endsWith(".jar");
 	    }
 	});
 
 	// name des zipeintrags im jarfile erzeugen.
 	String entryName = name.replaceAll("\\.", "/") + ".class";
 
 	if (jarList.length > 0) {
 	    // die liste der jar files durchsuchen
 	    for (int i = 0; i < jarList.length; i++) {
 		try {
 		    // den eintrag in den jarfiles suchen
 		    jarFile = new JarFile(jarList[i]);
 		    entry = jarFile.getEntry(entryName);
 
 		    if (entry != null) {
 			// eintrag gefunden: die daten laden und zurück geben.
 			byte[] data = new byte[(int) entry.getSize()];
 			jarFile.getInputStream(entry)
 				.read(data, 0, data.length);
 			return data;
 		    }
 		} catch (IOException e) {
 		    // TODO könnte ein guter platz für debug ausgabe sein
 		    continue;
 		}
 	    }
 	} else {
 	    return null;
 	}
 	return null;
     }
 
 }
