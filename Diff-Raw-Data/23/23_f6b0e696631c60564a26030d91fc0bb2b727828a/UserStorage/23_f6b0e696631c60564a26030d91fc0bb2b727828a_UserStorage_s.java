 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.web.service.file;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Map;
 import org.apache.commons.lang3.Validate;
 import org.lafayette.server.core.log.Log;
 import org.lafayette.server.core.log.Logger;
 
 /**
  * Stores {@link StoredFile} for a user by a resource id.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class UserStorage implements Serializable {
 
     /**
      * Logger facility.
      */
    private  final Logger log = Log.getLogger(UserStorage.class);
     /**
      * Holds the stored files.
      */
     private final Map<String, StoredFile> data = Maps.newConcurrentMap();
 
     /**
      * Get the file by an id.
      *
      * @param id must not be empty or {@code nul}
      * @return may return {@code null} if id not present
      */
     public StoredFile get(final String id) {
         Validate.notEmpty(id, "Id must not be empty!");
         log.debug(String.format("Get file for id '%s'.", id));
         return data.get(id);
     }
 
     /**
      * Set a file under an resource id.
      *
      * @param id must not be empty or {@code nul}
      * @param datum must not be {@code null}
      * @return may return {@code null} if no previous datum present
      */
     public StoredFile set(final String id, final StoredFile datum) {
         Validate.notEmpty(id, "Id must not be empty!");
         Validate.notNull(datum, "Datum must not be null!");
         log.debug(String.format("Put file for user id '%s'.", id));
         return data.put(id, datum);
     }
 
     /**
      * Removes a stored file.
      *
      * @param id must not be empty or {@code nul}
      * @return may return {@code null} if no datum present
      */
     public StoredFile remove(final String id) {
         Validate.notEmpty(id, "Id must not be empty!");
         log.debug(String.format("Remove file for id '%s'.", id));
         // TODO Remove from disk.
         return data.remove(id);
     }
 
     @Override
     public int hashCode() {
         return data.hashCode();
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!(obj instanceof UserStorage)) {
             return false;
         }
 
         final UserStorage other = (UserStorage) obj;
         return data.equals(other.data);
     }
 
     @Override
     public String toString() {
         return data.toString();
     }
 
     /**
      * Get all stored ids.
      *
      * @return never {@code null}
      */
     public Collection<String> getIds() {
         return Sets.newHashSet(data.keySet());
     }
 
 }
