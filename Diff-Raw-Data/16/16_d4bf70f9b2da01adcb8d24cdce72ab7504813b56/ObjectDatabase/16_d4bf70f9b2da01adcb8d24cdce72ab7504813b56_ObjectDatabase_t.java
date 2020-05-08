 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 package org.geogit.storage;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.Nullable;
 
 import org.geogit.api.ObjectId;
 import org.geogit.api.RevCommit;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject;
 import org.geogit.api.RevTag;
 import org.geogit.api.RevTree;
 
 /**
  * Provides an interface for implementations of GeoGit object databases.
  */
 public interface ObjectDatabase {
 
     /**
      * Initializes/opens the databse. It's safe to call this method multiple times, and only the
      * first call shall take effect.
      */
     public void open();
 
     /**
      * @return true if the database is open, false otherwise
      */
     public boolean isOpen();
 
     /**
      * Closes the database.
      */
     public void close();
 
     /**
      * Determines if the given {@link ObjectId} exists in the object database.
      * 
      * @param id the id to search for
      * @return true if the object exists, false otherwise
      */
     public boolean exists(final ObjectId id);
 
     /**
      * Searches the database for {@link ObjectId}s that match the given partial id.
      * 
      * @param partialId the partial id to search for
      * @return a list of matching results
      */
     public List<ObjectId> lookUp(final String partialId);
 
     /**
      * Reads an object with the given {@link ObjectId id} out of the database.
      * 
      * @throws IllegalArgumentException if no blob exists for the given {@code id}
      */
     public RevObject get(ObjectId id) throws IllegalArgumentException;
 
     /**
      * Reads an object with the given {@link ObjectId id} out of the database.
      * 
      * @throws IllegalArgumentException if no blob exists for the given {@code id}, or the object
      *         found is not of the required {@code type}
      */
     public <T extends RevObject> T get(ObjectId id, Class<T> type) throws IllegalArgumentException;
 
     /**
      * Reads an object with the given {@link ObjectId id} out of the database.
      * 
      * @return the object found, or {@code null} if no object is found
      */
     public @Nullable
     RevObject getIfPresent(ObjectId id);
 
     /**
      * Reads an object with the given {@link ObjectId id} out of the database.
      * 
      * @return the object found, or {@code null} if no object is found
      * @throws IllegalArgumentException if the object is found but is not of the required
      *         {@code type}
      */
     public @Nullable
     <T extends RevObject> T getIfPresent(ObjectId id, Class<T> type)
             throws IllegalArgumentException;
 
     /**
      * Shortcut for {@link #get(ObjectId, Class) get(id, RevTree.class)}
      */
     public RevTree getTree(ObjectId id);
 
     /**
      * Shortcut for {@link #get(ObjectId, Class) get(id, RevFeature.class)}
      */
     public RevFeature getFeature(ObjectId id);
 
     /**
      * Shortcut for {@link #get(ObjectId, Class) get(id, RevFeatureType.class)}
      */
     public RevFeatureType getFeatureType(ObjectId id);
 
     /**
      * Shortcut for {@link #get(ObjectId, Class) get(id, RevCommit.class)}
      */
     public RevCommit getCommit(ObjectId id);
 
     /**
      * Shortcut for {@link #get(ObjectId, Class) get(id, RevTag.class)}
      */
     public RevTag getTag(ObjectId id);
 
     /**
      * Adds an object to the database with the given {@link ObjectId id}. If an object with the same
      * id already exists, it will not be inserted.
      * 
      * @param object the object to insert, key'ed by its {@link RevObject#getId() id}
      * @return true if the object was inserted, false otherwise
      */
    public boolean put(final RevObject object);
 
     /**
      * @return a newly constructed {@link ObjectInserter} for this database
      */
     public ObjectInserter newObjectInserter();
 
     /**
      * Deletes the object with the provided {@link ObjectId id} from the database.
      * 
      * @param objectId the id of the object to delete
      * @return true if the object was deleted, false if it was not found
      */
     public boolean delete(ObjectId objectId);
 
     /**
      * @param iterator
      */
     public void putAll(Iterator<? extends RevObject> objects);
 
     public long deleteAll(Iterator<ObjectId> ids);
 
 }
