 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.storage;
 
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.Map;
 
 public interface StorageClient {
 
 	/**
 	 * Lookup an object by key
 	 * @param keySpace the keyspace to search
 	 * @param columnFamily the group of columns we're considering
 	 * @param key the key of the row
 	 * @return the key value pairs in the row key or null
 	 * @throws StorageClientException
 	 */
     Map<String, Object> get(String keySpace, String columnFamily, String key)
             throws StorageClientException;
 
     /**
      * Insert or update a row in the store.
      * @param keySpace the keyspace to search
	 * @param columnFamily the group of columns we're considering
	 * @param key the key of the row
      * @param values the Map of column values to associate with this key
      * @param probablyNew whether or not the row is probably new
      * @throws StorageClientException
      */
     void insert(String keySpace, String columnFamily, String key, Map<String, Object> values, boolean probablyNew)
             throws StorageClientException;
 
     /**
      * Remove a row in the store.
      * @param keySpace the keyspace to search
	 * @param columnFamily the group of columns we're considering
	 * @param key the key of the row
      * @throws StorageClientException
      */
     void remove(String keySpace, String columnFamily, String key) throws StorageClientException;
 
     /**
      * Get an {@link InputStream} to read a stream of content.
      * @param keySpace the keyspace to search
	 * @param columnFamily the group of columns we're considering
      * @param contentId the id of the content item
      * @param contentBlockId the block offset
      * @param streamId the id of the correct stream for this piece of content
      * @param content the properties of the content item
      * @return an stream that will read the block
      * @throws StorageClientException
      * @throws AccessDeniedException
      * @throws IOException
      */
     InputStream streamBodyOut(String keySpace, String columnFamily, String contentId,
             String contentBlockId, String streamId, Map<String, Object> content) throws StorageClientException,
             AccessDeniedException, IOException;
 
     /**
      * Write in the body of a piece of content.
      * @param keySpace the keyspace to search
      * @param columnFamily the group of columns we're considering
      * @param contentId the id of the content item
      * @param contentBlockId the block offset
      * @param streamId the id of the correct stream for this piece of content
      * @param content the properties of the content item
      * @param in a stream pointing to the data
      * @return the content item after the write it will be modified.
      * @throws StorageClientException
      * @throws AccessDeniedException
      * @throws IOException
      */
     Map<String, Object> streamBodyIn(String keySpace, String columnFamily, String contentId,
             String contentBlockId, String streamId, Map<String, Object> content, InputStream in)
             throws StorageClientException, AccessDeniedException, IOException;
 
     /**
      * Search for a piece of content.
      * @param keySpace the keyspace to search
      * @param authorizableColumnFamily the id of the column family
      * @param properties column and values to search
      * @return an iterator of results
      * @throws StorageClientException
      */
     Iterator<Map<String, Object>> find(String keySpace, String authorizableColumnFamily,
             Map<String, Object> properties) throws StorageClientException;
 
     /**
      * Close this client.
      */
     void close();
 
     /**
      * Find all of the children of a certain node.
      * @param keySpace
      * @param columnFamily
      * @param key the row id
      * @return an iterator of content items below this content item
      * @throws StorageClientException
      */
     DisposableIterator<Map<String, Object>> listChildren(String keySpace, String columnFamily,
             String key) throws StorageClientException;
 
     /**
      * Does this content item have a stream body by this id?
      * @param content
      * @param streamId
      * @return whether or not the stream exists for this content item
      */
     boolean hasBody( Map<String, Object> content, String streamId);
 
 }
