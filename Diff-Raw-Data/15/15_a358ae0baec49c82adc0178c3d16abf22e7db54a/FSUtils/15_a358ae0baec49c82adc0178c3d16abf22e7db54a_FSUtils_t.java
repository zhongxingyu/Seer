 package org.webreformatter.commons.fs;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * @author kotelnikov
  */
 public class FSUtils {
 
     public static void copy(IFile from, IFile to) throws IOException {
         InputStream input = from.getInputStream();
         try {
             OutputStream output = to.getOutputStream();
             try {
                 copy(input, output);
             } finally {
                 output.close();
             }
         } finally {
             input.close();
         }
     }
 
     public static void copy(InputStream in, OutputStream out)
         throws IOException {
         try {
             try {
                 byte[] buf = new byte[1024];
                 int len;
                 while ((len = in.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
             } finally {
                 out.close();
             }
         } finally {
             in.close();
         }
     }
 
     public static IDirectory getDirectory(IDirectory root, String path)
         throws FileSystemException {
         return getDirectory(root, path, true);
     }
 
     public static IDirectory getDirectory(
         IDirectory root,
         String path,
         boolean create) throws FileSystemException {
         IFileSystemEntry entry = root.getEntry(path);
         IDirectory dir = null;
         if (entry instanceof IDirectory) {
             dir = (IDirectory) entry;
         } else if (entry == null && create) {
             dir = root.createDirectory(path);
        } else if (entry != null) {
            throw new IllegalArgumentException("Can not create a directory. "
                + "There is already a file "
                + "corresponding to the specified path. "
                + "Path: '"
                + path
                + "'.");
         }
         return dir;
     }
 
     public static IFile getFile(IDirectory root, String path)
         throws FileSystemException {
         return getFile(root, path, true);
     }
 
     public static IFile getFile(IDirectory root, String path, boolean create)
         throws FileSystemException {
         IFileSystemEntry entry = root.getEntry(path);
         IFile file = null;
         if (entry instanceof IFile) {
             file = (IFile) entry;
         } else if (entry == null && create) {
             file = root.createFile(path);
        } else if (entry != null) {
            throw new IllegalArgumentException("Can not create a file. "
                + "There is already a directory "
                + "(or something else) corresponding to the specified path. "
                + "Path: '"
                + path
                + "'.");
         }
         return file;
     }
 
     public static String readString(IFile file) throws IOException {
         return readString(file.getInputStream());
     }
 
     public static String readString(InputStream input) throws IOException {
         try {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             try {
                 copy(input, out);
             } finally {
                 out.close();
             }
             byte[] array = out.toByteArray();
             return new String(array, "UTF-8");
         } finally {
             input.close();
         }
     }
 
     public static void writeString(String str, IFile file) throws IOException {
         writeString(str, file.getOutputStream());
     }
 
     public static void writeString(String str, OutputStream output)
         throws IOException {
         try {
             byte[] array = str.getBytes("UTF-8");
             ByteArrayInputStream input = new ByteArrayInputStream(array);
             copy(input, output);
         } finally {
             output.close();
         }
     }
 }
