 package com.xebialabs.overthere.nio.file;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SeekableByteChannel;
 import java.nio.file.*;
 import java.nio.file.DirectoryStream.Filter;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.nio.file.attribute.FileAttribute;
 import java.nio.file.attribute.FileAttributeView;
 import java.nio.file.spi.FileSystemProvider;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.io.Closeables;
 
 import com.xebialabs.overthere.*;
 
 import static com.google.common.collect.Maps.newHashMap;
 import static com.google.common.collect.Sets.newHashSet;
 
 public abstract class OverthereFileSystemProvider extends FileSystemProvider {
     Map<URI, OverthereFileSystem> cache = newHashMap();
 
     @Override
     public synchronized FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
         if (cache.containsKey(uri)) {
             throw new FileSystemAlreadyExistsException(uri.toString());
         }
         ConnectionOptions options = buildOptionsFromUri(uri);
         copyEnvironment(env, options);
 
         OverthereConnection connection = getConnection(options);
         OverthereFileSystem overthereFileSystem = new OverthereFileSystem(this, connection, uri);
         cache.put(uri, overthereFileSystem);
         return overthereFileSystem;
     }
 
     private ConnectionOptions buildOptionsFromUri(final URI uri) {
         ConnectionOptions options = new ConnectionOptions();
         copyHostAndPort(uri, options);
         copyUserInfo(uri, options);
         copyQuery(uri, options);
         return options;
     }
 
     protected OverthereConnection getConnection(final ConnectionOptions options) {
         return Overthere.getConnection(getScheme(), options);
     }
 
 
     protected void copyHostAndPort(URI uri, ConnectionOptions options) {
         String host = uri.getHost();
         if (host != null) {
             options.set(ConnectionOptions.ADDRESS, host);
         }
         int port = uri.getPort();
         if (port != -1) {
             options.set(ConnectionOptions.PORT, port);
         }
     }
 
     protected void copyUserInfo(URI uri, ConnectionOptions options) {
         String userInfo = uri.getUserInfo();
         if (userInfo != null) {
             String[] userInfoParts = userInfo.split(":", 2);
             if (userInfoParts.length >= 1) {
                 options.set(ConnectionOptions.USERNAME, userInfoParts[0]);
                 if (userInfoParts.length >= 2) {
                     options.set(ConnectionOptions.PASSWORD, userInfoParts[1]);
                 }
             }
         }
     }
 
     protected void copyQuery(URI uri, ConnectionOptions options) {
         List<NameValuePair> parsedQuery = URLEncodedUtils.parse(uri, null);
         for (NameValuePair queryEntry : parsedQuery) {
             options.set(queryEntry.getName(), queryEntry.getValue());
         }
     }
 
     protected void copyEnvironment(Map<String, ?> env, ConnectionOptions options) {
         for (Map.Entry<String, ?> envEntry : env.entrySet()) {
             options.set(envEntry.getKey(), envEntry.getValue());
         }
     }
 
     @Override
     public FileSystem getFileSystem(URI uri) {
         if (!cache.containsKey(uri)) {
             throw new FileSystemNotFoundException(uri.toString());
         }
         return cache.get(uri);
     }
 
     @Override
     public Path getPath(URI uri) {
         try {
             URI uri1 = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "/", uri.getQuery(), uri.getFragment());
             if (!cache.containsKey(uri1)) {
                 newFileSystem(uri1, Maps.<String, Object>newHashMap());
             }
             return cache.get(uri1).getPath(uri.getPath());
         } catch (URISyntaxException e) {
             throw new IllegalArgumentException("Could not create URI for FileSystem lookup/creation", e);
         } catch (IOException e) {
             throw new FileSystemNotFoundException("Could not get or create FileSystem");
         }
     }
 
     @Override
     public SeekableByteChannel newByteChannel(final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>... attrs) throws IOException {
         OverthereFile ofile = ((OvertherePath) path).getOverthereFile();
         final InputStream in;
         final OutputStream out;
 
         if (options.contains(StandardOpenOption.READ)) {
             in = ofile.getInputStream();
         } else {
             in = null;
         }
 
         if (!Sets.intersection(options, newHashSet(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.CREATE_NEW)).isEmpty()) {
             out = ofile.getOutputStream();
         } else {
             out = null;
         }
 
         return new SeekableByteChannel() {
             @Override
             public boolean isOpen() {
                 return true;
             }
 
             @Override
             public void close() throws IOException {
                 Closeables.closeQuietly(in);
                 Closeables.closeQuietly(out);
             }
 
             @Override
             public int write(ByteBuffer src) throws IOException {
                 int remaining = src.remaining();
                 out.write(src.array(), 0, remaining);
                 src.position(remaining);
                 return remaining;
             }
 
             @Override
             public SeekableByteChannel truncate(long size) throws IOException {
                 throw new UnsupportedOperationException();
             }
 
             @Override
             public long size() throws IOException {
                 throw new UnsupportedOperationException();
             }
 
             @Override
             public int read(ByteBuffer dst) throws IOException {
                 byte[] buf = new byte[dst.remaining()];
                 int bytesRead = in.read(buf);
                 dst.put(buf, 0, bytesRead);
                 return bytesRead;
             }
 
             @Override
             public SeekableByteChannel position(long newPosition) throws IOException {
                 throw new UnsupportedOperationException();
             }
 
             @Override
             public long position() throws IOException {
                 throw new UnsupportedOperationException();
             }
         };
     }
 
     @Override
     public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
         if (!Files.isDirectory(dir)) {
             throw new NotDirectoryException(dir.toString());
         }
 
         return new OverthereDirectoryStream(((OvertherePath) dir), filter);
     }
 
     @Override
     public void createDirectory(final Path dir, final FileAttribute<?>... attrs) throws IOException {
         if (Files.exists(dir)) {
             throw new FileAlreadyExistsException(dir.toString());
         }
         try {
             ((OvertherePath) dir).getOverthereFile().mkdir();
         } catch (RuntimeIOException rio) {
             throw new IOException(rio);
         }
     }
 
     @Override
     public void delete(Path path) throws IOException {
         if (!Files.exists(path)) {
             throw new NoSuchFileException(path.toString());
         }
         ((OvertherePath) path).getOverthereFile().delete();
     }
 
     @Override
     public void copy(Path source, Path target, CopyOption... options) throws IOException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void move(Path source, Path target, CopyOption... options) throws IOException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean isSameFile(Path path, Path path2) throws IOException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean isHidden(Path path) throws IOException {
         return ((OvertherePath) path).getOverthereFile().isHidden();
     }
 
     @Override
     public FileStore getFileStore(Path path) throws IOException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void checkAccess(Path path, AccessMode... modes) throws IOException {
         OverthereFile overthereFile = ((OvertherePath) path).getOverthereFile();
         if (!overthereFile.exists()) {
             throw new NoSuchFileException(path.toString());
         }
 
         if (modes == null) {
             return;
         }
         for (AccessMode mode : modes) {
             switch (mode) {
                 case READ:
                     checkAccess(overthereFile.canRead(), path, "Can not read");
                     break;
                 case WRITE:
                     checkAccess(overthereFile.canWrite(), path, "Can not write");
                     break;
                 case EXECUTE:
                     checkAccess(overthereFile.canExecute(), path, "Can not execute");
                     break;
             }
         }
     }
 
     private static void checkAccess(boolean access, Path path, String message) throws AccessDeniedException {
         if (!access) {
             throw new AccessDeniedException(path.toString(), null, message);
         }
     }
 
     @Override
     public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
         if (!BasicFileAttributes.class.isAssignableFrom(type)) {
             throw new UnsupportedOperationException("Don't support non BasicFileAttributes.");
         }
 
         OverthereFile overthereFile = ((OvertherePath) path).getOverthereFile();
         return (A) new OverthereFileAttributes(overthereFile);
     }
 
     @Override
     public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
         throw new UnsupportedOperationException();
     }
 }
