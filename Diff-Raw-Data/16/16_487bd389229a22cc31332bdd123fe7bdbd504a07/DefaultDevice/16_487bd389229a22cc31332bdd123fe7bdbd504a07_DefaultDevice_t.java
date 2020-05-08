 package falgout.backup.app;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.nio.file.FileStore;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Objects;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.UUID;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 
 import falgout.backup.FileStoreIdentifier;
 import falgout.backup.FileStoreLocator;
 import falgout.backup.guice.ConfigurationDirectory;
 
 class DefaultDevice extends AbstractDevice {
     static class Data extends AbstractDeviceData {
         private final UUID id;
         private final Path confFile;
         
         final boolean wasLoaded;
         final Set<Path> roots = new LinkedHashSet<>();
         final Set<Path> pathsToBackup = new TreeSet<>();
         final Map<Path, Hash> hashes = new LinkedHashMap<>();
         
         @AssistedInject
         public Data(@Assisted UUID id, @ConfigurationDirectory Path dir) throws IOException {
             this.id = id;
             confFile = dir.resolve(id.toString());
             
             wasLoaded = load();
         }
         
         private boolean load() throws IOException {
             boolean exists = Files.exists(confFile);
             
             if (exists) {
                 Set<String> roots;
                 Set<String> pathsToBackup;
                 Map<String, Hash> hashes;
                 
                 try (InputStream in = Files.newInputStream(confFile);
                         ObjectInputStream ois = new ObjectInputStream(in)) {
                     roots = (Set<String>) ois.readObject();
                     pathsToBackup = (Set<String>) ois.readObject();
                     hashes = (Map<String, Hash>) ois.readObject();
                 } catch (ClassNotFoundException e) {
                    throw new Error("Malformed device file for " + id + ".", e);
                 }
                 
                 for (String s : roots) {
                     this.roots.add(Paths.get(s));
                 }
                 for (String s : pathsToBackup) {
                     this.pathsToBackup.add(Paths.get(s));
                 }
                 for (Entry<String, Hash> e : hashes.entrySet()) {
                     this.hashes.put(Paths.get(e.getKey()), e.getValue());
                 }
             }
             
             return exists;
         }
         
         void save() throws IOException {
             Set<String> roots = new LinkedHashSet<>(this.roots.size());
             Set<String> pathsToBackup = new LinkedHashSet<>(this.pathsToBackup.size());
             Map<String, Hash> hashes = new LinkedHashMap<>(this.hashes.size());
             
             for (Path p : this.roots) {
                 roots.add(p.toString());
             }
             for (Path p : this.pathsToBackup) {
                 pathsToBackup.add(p.toString());
             }
             for (Entry<Path, Hash> e : this.hashes.entrySet()) {
                 hashes.put(e.getKey().toString(), e.getValue());
             }
             
             try (OutputStream out = Files.newOutputStream(confFile);
                     ObjectOutputStream oos = new ObjectOutputStream(out)) {
                 oos.writeObject(roots);
                 oos.writeObject(pathsToBackup);
                 oos.writeObject(hashes);
             }
         }
         
         @Override
         public UUID getID() {
             return id;
         }
         
         @Override
         public Set<Path> getPreviousRoots() {
             return Collections.unmodifiableSet(roots);
         }
         
         @Override
         public Set<Path> getPathsToBackup() {
             return Collections.unmodifiableSet(pathsToBackup);
         }
         
         @Override
         public Map<Path, Hash> getHashes() {
             return Collections.unmodifiableMap(hashes);
         }
     }
     
     private final Data data;
     private final FileStore store;
     private final Path root;
     
     private boolean setID;
     private final FileStoreIdentifier i;
     
     @AssistedInject
     public DefaultDevice(@Assisted FileStore store, FileStoreLocator l, FileStoreIdentifier i,
             @ConfigurationDirectory Path dir) throws IOException {
         UUID id = i.getID(store);
         setID = id == null;
         this.i = i;
         
         data = new Data(setID ? UUID.randomUUID() : id, dir);
         this.store = store;
         root = l.getRootLocation(store);
         
         if (data.roots.add(root) && data.wasLoaded) {
             save();
         }
     }
     
     @Override
     public UUID getID() {
         return data.getID();
     }
     
     @Override
     public Set<Path> getPreviousRoots() {
         return data.getPreviousRoots();
     }
     
     @Override
     public Set<Path> getPathsToBackup() {
         Set<Path> ret = new LinkedHashSet<>(data.pathsToBackup.size());
         for (Path p : data.pathsToBackup) {
             ret.add(root.resolve(p).normalize());
         }
         return Collections.unmodifiableSet(ret);
     }
     
     @Override
     public Map<Path, Hash> getHashes() {
         return data.getHashes();
     }
     
     @Override
     public FileStore getFileStore() {
         return store;
     }
     
     @Override
     public Path getRoot() {
         return root;
     }
     
     @Override
     protected boolean doUpdateHash(Path p, Hash hash) {
         Hash old = data.hashes.put(p, hash);
         return !Objects.equals(hash, old);
     }
     
     @Override
     protected boolean doAddPathToBackup(Path p) {
         Path rel = p;
         if (!p.isAbsolute()) {
             p = getRoot().resolve(p).normalize();
         }
         if (p.isAbsolute()) {
             rel = getRoot().relativize(p);
         }
         
         if (!p.startsWith(getRoot())) { throw new IllegalArgumentException(p + " is not a child of " + getRoot()); }
         if (Files.notExists(p)) { throw new IllegalArgumentException(p + " does not exist."); }
         
         // same as root
         if (Paths.get("").equals(rel)) {
             data.pathsToBackup.clear();
             return data.pathsToBackup.add(rel);
         }
         
         Iterator<Path> itr = data.pathsToBackup.iterator();
         while (itr.hasNext()) {
             Path path = itr.next();
             
             // same as root directory
             if (Paths.get("").equals(path)) { return false; }
             
             if (rel.startsWith(path)) {
                 return false;
             } else if (path.startsWith(rel)) {
                 itr.remove();
             }
         }
         
         return data.pathsToBackup.add(rel);
     }
     
     @Override
     protected boolean doRemovePathToBackup(Path p) {
         p = p.normalize();
         if (p.isAbsolute()) {
             p = getRoot().relativize(p);
         }
         return data.pathsToBackup.remove(p);
     }
     
     @Override
     protected void save() throws IOException {
         if (setID) {
             i.setID(store, data.getID());
             setID = false;
         }
         
         data.save();
     }
 }
