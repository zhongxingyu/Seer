 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.store;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.Collection;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.annotation.Nullable;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Sets;
 import com.google.common.io.Closeables;
 import com.google.common.io.Files;
 import com.google.common.io.InputSupplier;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 /**
  * File system based implementation of the {@link Store} interface.
  * 
  * <p>
  *   This implementation uses {@link UUID} to generate unique identifiers.
  *   {@link InputStream}s will be saved as files in sub-directories.
  *   Each sub directory represents on segment of the generated uuid, splitted
  *   by the dash signs.
  * </p>
  *
  * @author Willi Schoenborn
  */
 public final class FileSystemStore extends AbstractByteStore implements ByteStore {
 
     private static final Logger LOG = LoggerFactory.getLogger(FileSystemStore.class);
     
     private final File directory;
     
     private IdGenerator generator = new UUIDGenerator();
     
     private FileIdentifier fileIdentifier = new DefaultFileIdentifier();
 
     private final Function<File, String> toIdentifier = new Function<File, String>() {
         
         @Override
         public String apply(File from) {
             return fileIdentifier.toIdentifier(directory, from);
         }
         
     };
     
     private String unixOwner;
     
     private String unixPermissions;
     
     @Inject
     FileSystemStore(@Named(FileSystemStoreConfig.DIRECTORY) File directory) throws IOException {
         Preconditions.checkNotNull(directory, "Directory");
         FileUtils.forceMkdir(directory);
         this.directory = directory;
     }
     
     @Inject(optional = true)
     void setGenerator(@Named(StoreConfig.ID_GENERATOR) IdGenerator generator) {
         this.generator = Preconditions.checkNotNull(generator, "Generator");
     }
     
     @Inject(optional = true)
     void setFileIdentifier(@Named(FileSystemStoreConfig.FILE_IDENTIFIER) FileIdentifier identifier) {
         this.fileIdentifier = identifier;
     }
     
     @Inject(optional = true)
     void setUnixOwner(@Named(FileSystemStoreConfig.UNIX_OWNER) @Nullable String unixOwner) {
         this.unixOwner = unixOwner;
     }
     
     @Inject(optional = true)
     void setUnixPermissions(@Named(FileSystemStoreConfig.UNIX_PERMISSIONS) @Nullable String unixPermissions) {
         this.unixPermissions = unixPermissions;
     }
     
     public FileIdentifier getFileIdentifier() {
         return fileIdentifier;
     }
     
     @Override
     public String create(InputStream stream) throws IOException {
         Preconditions.checkNotNull(stream, "Stream");
         final String uuid = generator.generate();
         create(stream, uuid);
         return uuid;
     }
     
     @Override
     public void create(final InputStream stream, String identifier) throws IOException {
         Preconditions.checkNotNull(stream, "Stream");
         final File file = getFile(identifier);
         Preconditions.checkState(!file.exists(), "File %s is already present", file);
         LOG.trace("Storing {} to {}", stream, file);
         
         Files.createParentDirs(file);
         Files.copy(asSupplier(stream), file);
         
         final Process chown = setOwner(file);
         final Process chmod = setPermissions(file);
 
         try {
            chown.waitFor();
            chmod.waitFor();
         } catch (InterruptedException e) {
             throw new IOException(e);
         } finally {
             close(chown);
             close(chmod);
         }
     }
     
     private void close(Process process) {
         Closeables.closeQuietly(process.getInputStream());
         Closeables.closeQuietly(process.getOutputStream());
         Closeables.closeQuietly(process.getErrorStream());
     }
     
     private Process setOwner(File file) throws IOException {
         if (unixOwner == null) {
             LOG.trace("No unix owner configured, using defaults");
             return DummyProcess.getInstance();
         } else {
             final String path = file.getAbsolutePath();
             LOG.trace("Setting unix owner to {} for file {}", unixOwner, path);
             return exec("chown %s %s", unixOwner, path);
         }
     }
     
     private Process setPermissions(File file) throws IOException {
         if (unixPermissions == null) {
             LOG.trace("No unix permissions configured, using defaults");
             return DummyProcess.getInstance();
         } else {
             final String path = file.getAbsolutePath();
             LOG.trace("Setting unix permissions to {} for file {}", unixPermissions, path);
             return exec("chmod %s %s", unixPermissions, path);
         }
     }
     
    private Process exec(String command, Object... arguments) throws IOException {
        return Runtime.getRuntime().exec(String.format(command, arguments));
     }
     
     private InputSupplier<InputStream> asSupplier(final InputStream stream) {
         return new InputSupplier<InputStream>() {
             
             @Override
             public InputStream getInput() throws IOException {
                 return stream;
             }
             
         };
     }
     
     @Override
     public ByteBuffer view(String identifier) throws IOException {
         Preconditions.checkNotNull(identifier, "Identifier");
         final File file = getFile(identifier);
         Preconditions.checkState(file.exists(), "%s does not exist", file);
         LOG.trace("Reading file from {}", file);
         final FileChannel channel = new RandomAccessFile(file, "r").getChannel();
         return channel.map(MapMode.READ_ONLY, 0, channel.size());
     }
     
     @Override
     public Set<String> list() throws IOException {
         final IOFileFilter fileFilter = FileFilterUtils.fileFileFilter();
         final IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
         @SuppressWarnings("unchecked")
         final Collection<File> files = FileUtils.listFiles(directory, fileFilter, directoryFilter);
         return Sets.newHashSet(Collections2.transform(files, toIdentifier));
     }
     
     /**
      * {@inheritDoc}
      * 
      * <p>
      *   Recursively deletes empty parent directories.
      * </p>
      */
     @Override
     public void delete(String identifier) throws IOException {
         Preconditions.checkNotNull(identifier, "Identifier");
         final File file = getFile(identifier);
         Preconditions.checkState(file.exists(), "%s does not exist", file);
         LOG.trace("Removing {} from store", file);
         FileUtils.forceDelete(file);
         deleteEmptyParent(file.getParentFile());
     }
     
     /**
      * Provides a file pointing to the target as specified by
      * the given identifier.
      * 
      * @param identifier the file identifier
      * @return a file (may not exist)
      */
     private File getFile(String identifier) {
         return fileIdentifier.toFile(directory, identifier);
     }
     
     /**
      * Reads a file from this store.
      * 
      * @param identifier the identifier of the binary data being retrieved
      * @return the file associated with the given identifier
      * @throws IOException if file does not exist
      */
     public File readFile(String identifier) throws IOException {
         Preconditions.checkNotNull(identifier, "Identifier");
         final File file = getFile(identifier);
         if (file.exists()) {
             return file;
         } else {
             throw new FileNotFoundException(file.getAbsolutePath());
         }
     }
     
     private void deleteEmptyParent(File file) throws IOException {
         Preconditions.checkArgument(file.isDirectory(), "%s has to be a directory", file);
         
         // do not delete configured directory
         if (directory.equals(file)) return;
         
         if (file.list().length > 0) {
             LOG.trace("Keeping non empty directory {}", file);
             return;
         }
         
         LOG.trace("Deleting empty directory {}", file);
         FileUtils.deleteDirectory(file);
         deleteEmptyParent(file.getParentFile());
     }
 
 }
