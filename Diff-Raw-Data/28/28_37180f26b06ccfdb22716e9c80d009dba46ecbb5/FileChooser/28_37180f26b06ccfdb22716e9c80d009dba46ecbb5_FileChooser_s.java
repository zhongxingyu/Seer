 package net.sourceforge.importscrubber;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 
 // note: could have used threads rather than the iterator approach used here,
 // but that would have bought us nothing in the command line version (we don't WANT
 // a thread scrubbing while this finds files, because most disk systems handle
 // that kind of load very poorly).  so since our only concern is allowing the GUI
 // to update as files are found, this is just as effective -- and simpler.
 
 public class FileChooser implements Iterator
 {
     private String _sourceRoot, _classRoot;
     private boolean _recurse;
     // pre-computed for efficiency
     int relStart;
 
     private LinkedList possibles = new LinkedList();
     private FilePair nextFp;
 
     @SuppressWarnings("unchecked")
     public FileChooser(String sourceRoot, String classRoot, boolean recurse) throws IOException
     {
         File file = new File(sourceRoot);
         if (!file.exists())
            throw new IllegalArgumentException(Resources.ERR_DIR_NOT_EXIST);
         _sourceRoot = file.getCanonicalPath();
 
         file = new File(classRoot);
         if (!file.isDirectory())
            throw new IllegalArgumentException(Resources.ERR_NOT_DIR);
         if (!file.exists())
            throw new IllegalArgumentException(Resources.ERR_DIR_NOT_EXIST);
         _classRoot = file.getCanonicalPath();
 
         _recurse = recurse;
 
         relStart = ImportScrubber.getDirectory(_sourceRoot).length();
 
         possibles.add(_sourceRoot);
     }
 
     public void remove
         ()
     {
         throw new UnsupportedOperationException();
     }
 
     public boolean hasNext()
     {
         if (nextFp == null) {
             try {
                 nextFp = (FilePair)next();
             } catch (NoSuchElementException e) {}
         }
         return nextFp != null;
     }
 
     public Object next() throws NoSuchElementException
     {
         if (nextFp != null) {
             FilePair f = nextFp;
             nextFp = null;
             return f;
         }
 
         FilePair fp = null;
         while (fp == null) {
             fp = nextPossible();
         }
         return fp;
     }
 
     // returns null if the sourcefile didn't have a matching classfile.
     @SuppressWarnings("unchecked")
     private FilePair nextPossible() throws NoSuchElementException
     {
         String s = (String)possibles.removeFirst();
         File tmp = new File(s);
         if (tmp.isDirectory()) {
             if (_recurse) {
                 String[] entries = tmp.list(new JavaFileFilter());
                 for (int i = 0; i < entries.length; i++) {
                     possibles.add(s + ImportScrubber.FILE_SEPARATOR + entries[i]);
                 }
             }
             return nextPossible();
         } else {
             return getPair(tmp);
         }
     }
 
     private FilePair getPair(File source)
     {
         if (!(source.canRead() && source.canWrite())) {
             return null;
         }
         String relativeSourceFilename = source.getParent().substring(relStart);
 
         String cfilename = source.getName().substring(0, source.getName().length() - 5) + ".class";
         String classfilename = _classRoot + relativeSourceFilename + ImportScrubber.FILE_SEPARATOR + cfilename;
         File classfile = new File(classfilename);
         if(classfile.exists()) {
             return new FilePair(source, classfile);
         }
 
         return null;
     }
 }
