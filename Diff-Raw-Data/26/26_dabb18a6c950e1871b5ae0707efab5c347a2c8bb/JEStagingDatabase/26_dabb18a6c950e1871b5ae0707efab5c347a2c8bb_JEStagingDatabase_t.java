 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 package org.geogit.storage.bdbje;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import org.geogit.api.ObjectId;
import org.geogit.api.Platform;
 import org.geogit.api.RevCommit;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject;
 import org.geogit.api.RevTag;
 import org.geogit.api.RevTree;
import org.geogit.api.plumbing.ResolveGeogitDir;
 import org.geogit.api.plumbing.merge.Conflict;
 import org.geogit.storage.ObjectDatabase;
 import org.geogit.storage.ObjectInserter;
 import org.geogit.storage.ObjectSerializingFactory;
 import org.geogit.storage.StagingDatabase;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.io.Files;
 import com.google.common.io.LineProcessor;
 import com.google.inject.Inject;
 import com.sleepycat.je.Environment;
 
 /**
  * The Index (or Staging Area) object database.
  * <p>
  * This is a composite object database holding a reference to the actual repository object database
  * and a separate object database for the staging area itself.
  * <p>
  * Object look ups are first performed against the staging area database. If the object is not
  * found, then the look up is deferred to the actual repository database.
  * <p>
  * Object writes are always performed against the staging area object database.
  * <p>
  * The staging area database holds references to two root {@link RevTree trees}, one for the staged
  * objects and another one for the unstaged objects. When objects are added/changed/deleted to/from
  * the index, those modifications are written to the unstaged root tree. When objects are staged to
  * be committed, the unstaged objects are moved to the staged root tree.
  * <p>
  * A diff operation between the repository root tree and the index unstaged root tree results in the
  * list of unstaged objects.
  * <p>
  * A diff operation between the repository root tree and the index staged root tree results in the
  * list of staged objects.
  * 
  */
 public class JEStagingDatabase implements ObjectDatabase, StagingDatabase {
 
     /**
      * do not use it for anything else than constructing the delegate JEObjectDatabase or it'll open
      * a new environment for the same db each time
      */
     private final EnvironmentBuilder envProvider;
 
     /**
      * The db environment, created at open(), nullified at close(), owned by the delegate
      * JEObjectDatabase
      */
     private Environment environment;
 
     // /////////////////////////////////////////
     /**
      * The staging area object database, contains only differences between the index and the
      * repository
      */
     private ObjectDatabase stagingDb;
 
     /**
      * The persistent repository objects. Lookup operations delegate to this one for any object not
      * found on the {@link #stagingDb}
      */
     private ObjectDatabase repositoryDb;
 
     private ObjectSerializingFactory sfac;
 
    private Platform platform;

     /**
      * @param referenceDatabase the repository reference database, used to get the head re
      * @param repoDb
      * @param stagingDb
      */
     @Inject
     public JEStagingDatabase(final ObjectSerializingFactory sfac,
            final ObjectDatabase repositoryDb, final EnvironmentBuilder envBuilder,
            final Platform platform) {
         this.sfac = sfac;
         this.repositoryDb = repositoryDb;
         this.envProvider = envBuilder;
        this.platform = platform;
     }
 
     @Override
     public boolean isOpen() {
         return stagingDb != null;
     }
 
     @Override
     public void open() {
         if (isOpen()) {
             return;
         }
         envProvider.setRelativePath("index");
         environment = envProvider.get();
         stagingDb = new JEObjectDatabase(sfac, environment);
         stagingDb.open();
         // {
         // DatabaseConfig stagedDbConfig = new DatabaseConfig();
         // stagedDbConfig.setAllowCreate(true);
         // stagedDbConfig.setTransactional(environment.getConfig().getTransactional());
         // // stagedDbConfig.setDeferredWrite(true);
         // stagedDbConfig.setSortedDuplicates(false);
         // }
     }
 
     @Override
     public void close() {
         if (stagingDb != null) {
             stagingDb.close();// this closes the environment since it took control over it
             stagingDb = null;
             environment = null;
         }
     }
 
     // /////////////////////////////////////////////////////////////////////
 
     @Override
     public boolean exists(ObjectId id) {
         boolean exists = stagingDb.exists(id);
         if (!exists) {
             exists = repositoryDb.exists(id);
         }
         return exists;
     }
 
     @Override
     public InputStream getRaw(ObjectId id) {
         if (stagingDb.exists(id)) {
             return stagingDb.getRaw(id);
         }
         return repositoryDb.getRaw(id);
     }
 
     @Override
     public List<ObjectId> lookUp(String partialId) {
         Set<ObjectId> lookUp = new HashSet<ObjectId>(stagingDb.lookUp(partialId));
         lookUp.addAll(repositoryDb.lookUp(partialId));
         return new ArrayList<ObjectId>(lookUp);
     }
 
     @Override
     public <T extends RevObject> T get(ObjectId id, Class<T> type) {
         T obj = getIfPresent(id, type);
         if (obj == null) {
             obj = repositoryDb.get(id, type);
         }
         return obj;
     }
 
     @Override
     public RevObject get(ObjectId id) {
         RevObject obj = stagingDb.getIfPresent(id);
         if (obj == null) {
             obj = repositoryDb.get(id);
         }
         return obj;
     }
 
     @Override
     @Nullable
     public RevObject getIfPresent(ObjectId id) {
         RevObject obj = stagingDb.getIfPresent(id);
         if (obj == null) {
             obj = repositoryDb.getIfPresent(id);
         }
         return obj;
     }
 
     @Override
     @Nullable
     public <T extends RevObject> T getIfPresent(ObjectId id, Class<T> type)
             throws IllegalArgumentException {
         T obj = stagingDb.getIfPresent(id, type);
         if (obj == null) {
             obj = repositoryDb.getIfPresent(id, type);
         }
         return obj;
     }
 
     @Override
     public ObjectInserter newObjectInserter() {
         return stagingDb.newObjectInserter();
     }
 
     @Override
     public boolean delete(ObjectId objectId) {
         return stagingDb.delete(objectId);
     }
 
     @Override
     public boolean put(RevObject object) {
         return stagingDb.put(object);
     }
 
     @Override
     public void putAll(Iterator<? extends RevObject> objects) {
         stagingDb.putAll(objects);
     }
 
     @Override
     public boolean put(ObjectId objectId, InputStream raw) {
         return stagingDb.put(objectId, raw);
     }
 
     @Override
     public RevTree getTree(ObjectId id) {
         return get(id, RevTree.class);
     }
 
     @Override
     public RevFeature getFeature(ObjectId id) {
         return get(id, RevFeature.class);
     }
 
     @Override
     public RevFeatureType getFeatureType(ObjectId id) {
         return get(id, RevFeatureType.class);
     }
 
     @Override
     public RevCommit getCommit(ObjectId id) {
         return get(id, RevCommit.class);
     }
 
     @Override
     public RevTag getTag(ObjectId id) {
         return get(id, RevTag.class);
     }
 
     // TODO:
     // *****************************************************************************************
     // The following methods are a temporary implementation of conflict storage that relies on a
     // conflict file in the index folder
     // *****************************************************************************************
 
     @Override
     public List<Conflict> getConflicts(@Nullable String namespace, final String pathFilter) {
         Optional<File> conflictsFile = findOrCreateConflictsFile(namespace);
         if (!conflictsFile.isPresent()) {
             return ImmutableList.of();
         }
         File file = conflictsFile.get();
         List<Conflict> conflicts = Lists.newArrayList();
         try {
             synchronized (file.getCanonicalPath().intern()) {
                 conflicts = Files.readLines(file, Charsets.UTF_8,
                         new LineProcessor<List<Conflict>>() {
                             List<Conflict> conflicts = Lists.newArrayList();
 
                             @Override
                             public List<Conflict> getResult() {
                                 return conflicts;
                             }
 
                             @Override
                             public boolean processLine(String s) throws IOException {
                                 Conflict c = Conflict.valueOf(s);
                                 if (pathFilter == null) {
                                     conflicts.add(c);
                                 } else if (c.getPath().startsWith(pathFilter)) {
                                     conflicts.add(c);
                                 }
                                 return true;
                             }
                         });
             }
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
         return conflicts;
     }
 
     @Override
     public void addConflict(@Nullable String namespace, Conflict conflict) {
         Optional<File> file = findOrCreateConflictsFile(namespace);
         Preconditions.checkState(file.isPresent());
         try {
             Files.append(conflict.toString() + "\n", file.get(), Charsets.UTF_8);
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
     }
 
     @Override
     public void removeConflict(@Nullable String namespace, String path) {
         List<Conflict> conflicts = getConflicts(namespace, null);
         Optional<File> file = findOrCreateConflictsFile(namespace);
         Preconditions.checkState(file.isPresent());
 
         StringBuilder sb = new StringBuilder();
         try {
             for (Conflict conflict : conflicts) {
                 if (!path.equals(conflict.getPath())) {
                     sb.append(conflict.toString() + "\n");
                 }
             }
             String s = sb.toString();
             if (!s.isEmpty()) {
                 Files.append(s, file.get(), Charsets.UTF_8);
             }
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
     }
 
     @Override
     public Optional<Conflict> getConflict(@Nullable String namespace, final String path) {
         Optional<File> file = findOrCreateConflictsFile(namespace);
         if (!file.isPresent()) {
             return Optional.absent();
         }
         Conflict conflict = null;
         try {
             File conflictsFile = file.get();
             synchronized (conflictsFile.getCanonicalPath().intern()) {
                 conflict = Files.readLines(conflictsFile, Charsets.UTF_8,
                         new LineProcessor<Conflict>() {
                             Conflict conflict = null;
 
                             @Override
                             public Conflict getResult() {
                                 return conflict;
                             }
 
                             @Override
                             public boolean processLine(String s) throws IOException {
                                 Conflict c = Conflict.valueOf(s);
                                 if (c.getPath().equals(path)) {
                                     conflict = c;
                                     return false;
                                 } else {
                                     return true;
                                 }
                             }
                         });
             }
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
         return Optional.fromNullable(conflict);
     }
 
     private Optional<File> findOrCreateConflictsFile(@Nullable String namespace) {
         if (namespace == null) {
             namespace = "conflicts";
         }
         Optional<File> conflicts = Optional.absent();
         if (environment != null) {
            URL repoPath = new ResolveGeogitDir(platform).call();
            File file = null;
            try {
                file = new File(repoPath.toURI());
            } catch (URISyntaxException e1) {
                Throwables.propagate(e1);
            }
             file = new File(file, namespace);
             if (!file.exists()) {
                 try {
                    file.getParentFile().mkdirs();
                     file.createNewFile();
                 } catch (IOException e) {
                     throw Throwables.propagate(e);
                 }
             }
             conflicts = Optional.of(file);
         }
         return conflicts;
     }
 
     @Override
     public void removeConflicts(@Nullable String namespace) {
         Optional<File> file = findOrCreateConflictsFile(namespace);
         if (file.isPresent()) {
             file.get().delete();
         }
     }
 
 }
