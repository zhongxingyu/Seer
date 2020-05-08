 package com.seeburger.vfs2.provider.digestarc;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.security.DigestInputStream;
 import java.security.DigestOutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
import java.security.Signature;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.DeflaterOutputStream;
 import java.util.zip.InflaterInputStream;
 
 import org.apache.commons.vfs2.FileContent;
 import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
 
 public class DarcTree
 {
     static final Charset ASCII = Charset.forName("ASCII");
     static final byte[] BLOB_HEADER = "blob ".getBytes(ASCII);
     static final byte[] EMPTY_BYTES = new byte[0];
 
     DarcTree.Directory root;
 
     public DarcTree()
     {
         this.root = new Directory(new HashMap<String, Entry>());
     }
 
     /** Read the root directory of this tree from InputStream. */
     public DarcTree(InputStream is, String expectedHash) throws IOException
     {
 //System.out.println("Initiating DarcTree from hash " + expectedHash);
         root = new Directory(new DataInputStream(is), expectedHash);
     }
 
 
     public Entry resolveName(String name, BlobStorageProvider provider) throws IOException
     {
         if (name.equals("/"))
             return root;
 
         String[] parts = name.split("/");
 
         Entry me = root;
         for(int i=1;i<parts.length;i++)
         {
             Entry child = me.getChild(parts[i], provider);
             if (child == null)
             {
                 return null; // if file or parent does not exist
             }
             me = child;
         }
         return me;
     }
 
 
     /** Adds a mutable parent, and adds this path to it.
      * @throws IOException */
     public void createFolder(String name, BlobStorageProvider provider) throws IOException
     {
         String[] parts = name.split("/");
         Entry me = root;
         for(int i=1;i<parts.length;i++)
         {
             Entry child = me.getChild(parts[i], provider);
             if (child == null)
             {
                 // me is the last existing entry
                 if (me instanceof File)
                     throw new IOException("File " + me + " is not a folder to create " + name); // TODO: me.toString()
                 Directory parentDir = (Directory)me;
                 Directory newChild = null;
                 for(int j=i;j<parts.length;j++)
                 {
                     newChild = new Directory(new HashMap<String, Entry>());
                     parentDir.addDirectory(parts[j], newChild);
                     parentDir = newChild;
                 }
                 return;
             }
             me = child;
         }
     }
 
 
     public void addFile(String name, String hash, long length, BlobStorageProvider provider) throws IOException
     {
         if (name.equals("/"))
             throw new RuntimeException("Cannot overwrite root.");
 
         String[] parts = name.split("/");
         Entry me = root;
         Entry child = root;
         for(int i=1;i<parts.length-1;i++)
         {
             me = child;
             child = me.getChild(parts[i], provider);
 //System.out.println("addFile " + name + " me=" + me + " child=" + child + " i=" + i);
             if (child == null)
             {
                 // me is the last existing entry
                 if (me instanceof File)
                     throw new IOException("File " + me + " is not a folder to create " + name); // TODO: me.toString()
                 Directory parentDir = (Directory)me;
                 Directory newChild = null;
                 for(int j=i;j<parts.length-1;j++)
                 {
                     newChild = new Directory(new HashMap<String, Entry>());
                     parentDir.addDirectory(parts[j], newChild);
                     parentDir = newChild;
                 }
                 return;
             }
         }
         // me is the parent of the file
         ((Directory)child).addFile(parts[parts.length-1], length, hash);
     }
 
     public void delete(String name, BlobStorageProvider provider) throws IOException
     {
         if (name.equals("/"))
             throw new RuntimeException("Cannot delete root.");
 
         String[] parts = name.split("/");
 
         Entry me = null;
         Entry child = root;
         for(int i=1;i<parts.length;i++)
         {
             me = child;
             child = me.getChild(parts[i], provider);
             if (child == null)
             {
                 throw new RuntimeException("Not found " + name);
             }
         }
         ((Directory)me).removeChild(parts[parts.length-1]);
     }
 
 
     abstract class Entry
     {
         String hash;
         long size;
 
         abstract Entry getChild(String string, BlobStorageProvider provider) throws IOException;
 
         abstract String getHash();
 
         abstract long getTime();
     }
 
     class File extends Entry
     {
         long size;
         String hash;
 
         File(long size, String string)
         {
             this.size = size;
             this.hash = string;
         }
 
         @Override
         String getHash()
         {
             return hash;
         }
 
         @Override
         Entry getChild(String name, BlobStorageProvider provider) throws IOException
         {
             //throw new FileNotFolderException(name); // TODO: not sure if name arg works as it is relative
             throw new IOException("This Entry is no Folder. name=" + name);
         }
 
         long getSize()
         {
             return size;
         }
 
         @Override
         long getTime()
         {
             // TODO Auto-generated method stub
             return 0;
         }
 
     }
 
     class Directory extends Entry
     {
         private static final byte DIRECTORY_MARKER = 'D';
         private static final byte FILE_MARKER = 'F';
 
         Map<String, Entry> content;
         boolean modified = true;
 
         Directory(String hash)
         {
             this.hash = hash;
             this.modified = false;
         }
 
         Directory(DataInputStream dis, String expectedHash) throws IOException
         {
             this.content = readFromStream(dis, expectedHash);
             this.hash = expectedHash;
         }
 
         Directory(Map<String, Entry> content)
         {
             this.hash = null;
             this.content = content;
             this.modified = true;
         }
 
         public String[] getChildrenNames(BlobStorageProvider provider) throws IOException
         {
             materializeContent(provider);
             Set<String> keys = content.keySet();
             return keys.toArray(new String[keys.size()]);
         }
 
         public void addFile(String name, long length, String hash)
         {
             // TODO: materializeContent(provider);
             Entry file = new File(length,  hash);
             content.put(name, file);
             modified = true;
         }
 
         public void removeChild(String name)
         {
             // TODO: materializeContent(provider);
             Entry entry = content.remove(name);
             if (entry != null)
                 modified = true;
         }
 
         public void addDirectory(String name, Directory newChild)
         {
             // TODO: materializeContent(provider);
             modified = true;
             content.put(name, newChild);
         }
 
         public long getTime()
         {
             // TODO Auto-generated method stub
             return 0;
         }
 
         Entry getChild(String name, BlobStorageProvider provider) throws IOException
         {
             materializeContent(provider);
             return content.get(name); // might be null
         }
 
         String writeToStream(OutputStream target) throws IOException
         {
             // First we create a in-memory buffer
             ByteArrayOutputStream bos = new ByteArrayOutputStream(content.size() * 30);
             DataOutputStream out = new DataOutputStream(bos);
             ArrayList<String> names = new ArrayList<String>(content.keySet());
             Collections.sort(names);
 
             for(String name : names)
             {
                 Entry e = content.get(name);
                 if (e instanceof Directory)
                 {
                     out.writeByte(DIRECTORY_MARKER);
                     out.writeByte(1); // record format version (1=plain)
                     out.writeUTF(name);
                     out.writeUTF(e.getHash());
                 }
                 else if (e instanceof File)
                 {
                     File f = (File)e;
                     out.writeByte(FILE_MARKER);
                     out.writeByte(1);
                     out.writeUTF(name);
                     out.writeLong(f.getSize());
                     out.writeUTF(f.getHash());
                 }
             }
             out.close();
             byte[] buf = bos.toByteArray(); bos = null;
 
             OutputStream cout = new DeflaterOutputStream(target);
             DigestOutputStream digester = new DigestOutputStream(cout, getDigester());
             // then we write it all to a compressed stream
             DataOutputStream dout = new DataOutputStream(digester);
             dout.writeBytes("seetree ");
             String size = String.valueOf(buf.length);
             dout.writeBytes(size);
             dout.writeByte(0);
             dout.write(buf);
             dout.close();
             return asHex(digester.getMessageDigest().digest());
         }
 
         @Override
         String getHash()
         {
             return hash;
         }
 
         private void materializeContent(BlobStorageProvider provider) throws IOException
         {
             if (content != null)
                 return;
 
             FileObject dir = provider.resolveFileHash(hash);
             try
             {
                 FileContent fileContent = dir.getContent();
                 InputStream is = fileContent.getInputStream();
                 content = readFromStream(is, hash);
             } finally {
                 dir.close();
             }
         }
 
         /** Read directory from stream and compare the hash.
          * @throws IOException */
         private Map<String, Entry> readFromStream(InputStream is, String expectedHash) throws IOException
         {
             InflaterInputStream inflated = new InflaterInputStream(is);
             DigestInputStream digester = new DigestInputStream(inflated, getDigester());
             DataInputStream dis = new DataInputStream(digester);
             long len = readHeader(dis, "seetree");
             Map<String, Entry> newContent = new HashMap<String, Entry>(20);
             while(true)
             {
                 int type = dis.read();
                 if (type == -1)
                     break;
                 switch(type)
                 {
                     case DIRECTORY_MARKER:
                         byte ver = dis.readByte();
                         if (ver != 1)
                            throw new IOException("Directory Entry with version " + String.valueOf((int)ver)+" is unknown");
                         String name = dis.readUTF();
                         String hash = dis.readUTF();
                         Entry entry = new Directory(hash);
                         newContent.put(name,  entry);
                         break;
                     case FILE_MARKER:
                         ver = dis.readByte();
                         if (ver != 1)
                            throw new IOException("File Entry with version " + String.valueOf((int)ver)+" is unknown");
                         name = dis.readUTF();
                         size = dis.readLong();
                         hash = dis.readUTF();
                         entry = new File(size, hash);
                         newContent.put(name,  entry);
                         break;
                     default:
                        throw new IOException("Unknown record identifier " + String.valueOf((int)type));
                 }
             } // end while
             dis.close();
             String digest = asHex(digester.getMessageDigest().digest());
             if (!expectedHash.equals(digest))
                 throw new IOException("While readig file with expected hash=" + expectedHash + " we read corrupted data with hash=" + digest);
             return newContent;
         }
 
         /** Read and verify the tree header
          * @throws IOException */
         private long readHeader(InputStream in, String header) throws IOException
         {
             int sigLen = header.length() + 1;
             byte[] buf = new byte[sigLen + 19 + 1]; // "tree <long digits>\0"
             in.read(buf, 0, sigLen);
             int i;
             for(i=sigLen;i<buf.length;i++)
             {
                 int c = in.read();
                 if (c == -1)
                     throw new IOException("EOF while reading header at pos=" + i);
                 buf[i] = (byte)c;
                 if (c == 0)
                     break;
             }
             if (i == buf.length)
             {
                 throw new IOException("Missing end of header at pos=" + i);
             }
 
             String signature = new String(buf, 0, i, ASCII);
             if (!signature.startsWith(header+" "))
             {
                 throw new IOException("File Header does not start with signature, but=" + signature);
             }
 
             return Long.parseLong(signature.substring(sigLen));
         }
     }
 
     public MessageDigest getDigester()
     {
         try
         {
             return MessageDigest.getInstance("SHA1");
         }
         catch (NoSuchAlgorithmException e)
         {
             throw new RuntimeException("Cannot resolve SHA1 hash.", e);
         }
     }
 
     public static String asHex(byte[] bytes)
     {
         char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
         char[] result = new char[bytes.length * 2];
         for ( int j = 0; j < bytes.length; j++ ) {
             int i = bytes[j] & 0xFF;
             result[j*2] = hexArray[i >> 4];
             result[j*2 + 1] = hexArray[i & 0xf];
         }
         return new String(result);
     }
 
     public String commitChanges(BlobStorageProvider provider) throws IOException
     {
         // depth-first search to write out all dirty directories
         return writeChange(root, provider);
     }
 
     private String writeChange(Directory dir, BlobStorageProvider provider) throws IOException
     {
         Set<java.util.Map.Entry<String, Entry>> childrens = dir.content.entrySet();
         for(java.util.Map.Entry<String, Entry> e : childrens)
         {
             Entry ent = e.getValue();
             // traverse all directories (not only dirty ones as they might have dirty childs)
             if (ent instanceof Directory)
             {
                 Directory dir2 = (Directory)ent;
                 String oldHash = dir2.getHash();
                 String hash = writeChange(dir2, provider);
                 // if hash is recalculated we see if if affects current dir
                 if (hash != null)
                 {
                     if (oldHash == null || !oldHash.equals(hash))
                     {
                         dir.modified = true; // write out this parent as well as child hash changed
                     }
                 }
             }
         }
 
         if (dir.modified)
         {
             OutputStream os = provider.getTempStream();
             String hash = dir.writeToStream(os);
             os.close(); // TODO: close always or never
             provider.storeTempBlob(os, hash);
             dir.hash = hash;
             dir.modified = false;
             return hash;
         }
         return null;
     }
 }
