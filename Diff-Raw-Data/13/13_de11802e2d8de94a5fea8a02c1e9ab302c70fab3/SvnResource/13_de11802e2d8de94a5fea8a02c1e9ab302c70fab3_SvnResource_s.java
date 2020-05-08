 package net.oneandone.lavender.modules;
 
 import net.oneandone.lavender.index.Hex;
 import net.oneandone.lavender.index.Label;
 import net.oneandone.sushi.fs.Node;
 
 import java.io.IOException;
 import java.net.URI;
 
 public class SvnResource extends Resource {
     private final SvnModule module;
     private final long revision;
     private final URI origin;
     private final String path;
     private final long length;
     private final long lastModified;
     private final String folder;
 
     public SvnResource(SvnModule module, long revision, URI origin, String path, long length, long lastModified, String folder, Node dataNode, byte[] dataBytes, byte[] lazyMd5) {
         this.origin = origin;
         this.path = path;
         this.length = length;
         this.lastModified = lastModified;
         this.folder = folder;
 
         this.dataNode = dataNode;
         this.dataBytes = dataBytes;
 
         this.lazyMd5 = lazyMd5;
         this.module = module;
         this.revision = revision;
     }
 
     @Override
     public byte[] getMd5() throws IOException {
         if (lazyMd5 == null) {
             lazyMd5 = md5(getData());
             module.index().add(new Label(getPath(), Long.toString(revision), lazyMd5));
         }
         return lazyMd5;
     }
 
     // dataNode xor dataBytes is null
     private Node dataNode;
     private byte[] dataBytes;
 
     protected byte[] lazyMd5;
 
     public String getPath() {
         return path;
     }
 
     public long getLastModified() throws IOException {
         return lastModified;
     }
 
     public URI getOrigin() {
         return origin;
     }
 
     public byte[] getData() throws IOException {
         if (dataBytes == null) {
             dataBytes = dataNode.readBytes();
             dataNode = null;
         }
         return dataBytes;
     }
 
     public Label labelLavendelized(String pathPrefix) throws IOException {
 
         String filename;
         String md5str;
 
         filename = path.substring(path.lastIndexOf('/') + 1); // ok when not found
         md5str = Hex.encodeString(getMd5());
         if (md5str.length() < 3) {
             throw new IllegalArgumentException(md5str);
         }
         return new Label(path, pathPrefix + md5str.substring(0, 3) + "/" + md5str.substring(3) + "/" + folder + "/" + filename, getMd5());
     }
 
     public Label labelNormal(String pathPrefix) throws IOException {
         return new Label(path, pathPrefix + path, getMd5());
     }
 }
