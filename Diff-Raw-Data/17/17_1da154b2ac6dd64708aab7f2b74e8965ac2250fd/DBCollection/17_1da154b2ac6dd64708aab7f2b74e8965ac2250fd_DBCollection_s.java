 // DBCollection.java
 
 /**
  *      Copyright (C) 2008 10gen Inc.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.mongodb;
 
 import java.util.*;
 
 import org.bson.types.*;
 
 /** This class provides a skeleton implementation of a database collection.  
  * <p>A typical invocation sequence is thus
  * <blockquote><pre>
  *     Mongo mongo = new Mongo( new DBAddress( "localhost", 127017 ) );
  *     DB db = mongo.getDB( "mydb" );
  *     DBCollection collection = db.getCollection( "test" );
  * </pre></blockquote>
  * @dochub collections
  */
 @SuppressWarnings("unchecked")
 public abstract class DBCollection {
 
     final static boolean DEBUG = Boolean.getBoolean( "DEBUG.DB" );
 
     /**
      * Saves an document to the database.
      * if doc doesn't have an _id, one will be added
      * you can get the _id that was added from doc after the insert
      * @param doc object to save
      * @dochub insert
      */
     public abstract void insert(DBObject doc) throws MongoException;
 
     /**
      * Saves an array of documents to the database.
      *
      * @param arr  array of documents to save
      * @dochub insert
      */
     public abstract void insert(DBObject[] arr) throws MongoException;
 
     /**
      * Saves an array of documents to the database.
      *
      * @param list list of documents to save
      * @dochub insert
      */
     public abstract void insert(List<DBObject> list) throws MongoException;
 
     /**
      * Performs an update operation.
      * @param q search query for old object to update
      * @param o object with which to update <tt>q</tt>
      * @param upsert if the database should create the element if it does not exist
      * @param multi if the update should be applied to all objects matching (db version 1.1.3 and above)
      * See http://www.mongodb.org/display/DOCS/Atomic+Operations
      * @dochub update
      */
     public abstract void update( DBObject q , DBObject o , boolean upsert , boolean multi ) throws MongoException ;
 
     /**
      * @dochub update
      */
     public void update( DBObject q , DBObject o ) throws MongoException {
         update( q , o , false , false );
     }
 
     /**
      * @dochub update
      */
     public void updateMulti( DBObject q , DBObject o ) throws MongoException {
         update( q , o , false , true );
     }
 
     /** Adds any necessary fields to a given object before saving it to the collection.
      * @param o object to which to add the fields
      */
     protected abstract void doapply( DBObject o );
 
     /** Removes objects from the database collection.
      * @param o the object that documents to be removed must match
      * @dochub remove
      */
     public abstract void remove( DBObject o ) throws MongoException ;
 
     /** Finds an object.
      * @param ref query used to search
      * @param fields the fields of matching objects to return
      * @param numToSkip will not return the first <tt>numToSkip</tt> matches
      * @param batchSize if positive, is the # of objects per batch sent back from the db.  all objects that match will be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or the # that fit in a batch
      * @param options - see Bytes QUERYOPTION_*
      * @return the objects, if found
      * @dochub find
      */
     abstract Iterator<DBObject> __find( DBObject ref , DBObject fields , int numToSkip , int batchSize , int options ) throws MongoException ;
     
     /** Finds an object.
      * @param ref query used to search
      * @param fields the fields of matching objects to return
      * @param numToSkip will not return the first <tt>numToSkip</tt> matches
      * @param batchSize if positive, is the # of objects per batch sent back from the db.  all objects that match will be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or the # that fit in a batch
      * @param options - see Bytes QUERYOPTION_*
      * @return the objects, if found
      * @dochub find
      */
     public final DBCursor find( DBObject ref , DBObject fields , int numToSkip , int batchSize , int options ) throws MongoException{
    	return new DBCursor(this, ref, fields).skip(numToSkip).batchSize(batchSize).addOption(options);
     }
     
 
     /** Finds an object.
      * @param ref query used to search
      * @param fields the fields of matching objects to return
      * @param numToSkip will not return the first <tt>numToSkip</tt> matches
      * @param batchSize if positive, is the # of objects per batch sent back from the db.  all objects that match will be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or the # that fit in a batch
      * @return the objects, if found
      * @dochub find
      */
     public final DBCursor find( DBObject ref , DBObject fields , int numToSkip , int batchSize ) {
    	return find(ref, fields).skip(numToSkip).batchSize(batchSize);
     }
 
     /** Finds an object.
      * @param ref query used to search
      * @param fields the fields of matching objects to return
      * @param numToSkip will not return the first <tt>numToSkip</tt> matches
      * @param batchSize if positive, is the # of objects per batch sent back from the db.  all objects that match will be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or the # that fit in a batch
      * @return the objects, if found
      * @dochub find
      */
     Iterator<DBObject> __find( DBObject ref , DBObject fields , int numToSkip , int batchSize ) 
         throws MongoException {
         return __find( ref , fields , numToSkip , batchSize , 0 );
     }
 
     protected abstract void createIndex( DBObject keys , DBObject options ) throws MongoException ;
 
 
     // ------
 
     /**
      * Finds an object by its id.  
      * This compares the passed in value to the _id field of the document
      * 
      * @param obj any valid object
      * @return the object, if found, otherwise <code>null</code>
      */
     public final DBObject findOne( Object obj ) 
         throws MongoException {
         return findOne(obj, null);
     }
 
     /**
      * Finds an object by its id.  
      * This compares the passed in value to the _id field of the document
      * 
      * @param obj any valid object
      * @param fields fields to return
      * @return the object, if found, otherwise <code>null</code>
      * @dochub find
      */
     public final DBObject findOne( Object obj, DBObject fields ) {
         Iterator<DBObject> iterator = __find(new BasicDBObject("_id", obj), fields, 0, -1, 0);
         return (iterator != null ? iterator.next() : null);
     }
 
     // --- START INDEX CODE ---
 
     /** Forces creation of an index on a set of fields, if one does not already exist.
      * @param keys an object with a key set of the fields desired for the index
      */
     public final void createIndex( final DBObject keys )
         throws MongoException {
         createIndex( keys , defaultOptions( keys ) );
     }
 
     public final void ensureIndex( final String name ){
         ensureIndex( new BasicDBObject( name , 1 ) );
     }
 
     /** Creates an index on a set of fields, if one does not already exist.
      * @param keys an object with a key set of the fields desired for the index
      */
     public final void ensureIndex( final DBObject keys )
         throws MongoException {
         ensureIndex( keys , defaultOptions( keys ) );
     }
 
     /** Ensures an index on this collection (that is, the index will be created if it does not exist).
      * ensureIndex is optimized and is inexpensive if the index already exists.
      * @param keys fields to use for index
      * @param name an identifier for the index
      * @dochub indexes
      */
     public void ensureIndex( DBObject keys , String name ) 
         throws MongoException {
         ensureIndex( keys , name , false );
     }
 
     /** Ensures an optionally unique index on this collection.
      * @param keys fields to use for index
      * @param name an identifier for the index
      * @param unique if the index should be unique
      */
     public void ensureIndex( DBObject keys , String name , boolean unique ) 
         throws MongoException {
         DBObject options = defaultOptions( keys );
         options.put( "name" , name );
         if ( unique )
             options.put( "unique" , Boolean.TRUE );
         ensureIndex( keys , options );
     }
 
     public final void ensureIndex( final DBObject keys , final DBObject optionsIN )
         throws MongoException {
 
         if ( checkReadOnly( false ) ) return;
 
         final DBObject options = defaultOptions( keys );
         for ( String k : optionsIN.keySet() )
             options.put( k , optionsIN.get( k ) );
 
         final String name = options.get( "name" ).toString();
 
         boolean doEnsureIndex = false;
         if ( ! _createIndexes.contains( name ) )
             doEnsureIndex = true;
         else if ( _anyUpdateSave && ! _createIndexesAfterSave.contains( name ) )
             doEnsureIndex = true;
 
         if ( ! doEnsureIndex )
             return;
 
         createIndex( keys , options );
 
         _createIndexes.add( name );
         if ( _anyUpdateSave )
             _createIndexesAfterSave.add( name );
     }
 
     /** Clears all indices that have not yet been applied to this collection. */
     public void resetIndexCache(){
         _createIndexes.clear();
     }
 
     DBObject defaultOptions( DBObject keys ){
         DBObject o = new BasicDBObject();
         o.put( "name" , genIndexName( keys ) );
         o.put( "ns" , _fullName );
         return o;
     }
 
     /** Generate an index name from the set of fields it is over.
      * @param keys the names of the fields used in this index
      * @return a string representation of this index's fields
      */
     public static String genIndexName( DBObject keys ){
         String name = "";
         for ( String s : keys.keySet() ){
             if ( name.length() > 0 )
                 name += "_";
             name += s + "_";
             Object val = keys.get( s );
             if ( val instanceof Number || val instanceof String )
                 name += val.toString().replace( ' ' , '_' );
         }
         return name;
     }
 
     // --- END INDEX CODE ---
 
     /** Set hint fields for this collection.
      * @param lst a list of <code>DBObject</code>s to be used as hints
      */
     public void setHintFields( List<DBObject> lst ){
         _hintFields = lst;
     }
 
     /** Queries for an object in this collection.
      * @param ref object for which to search
      * @return an iterator over the results
      * @dochub find
      */
     public final DBCursor find( DBObject ref ){
         return new DBCursor( this, ref, null );
     }
 
     /** Queries for an object in this collection.
      *
      * <p>
      * An empty DBObject will match every document in the collection.
      * Regardless of fields specified, the _id fields are always returned.
      * </p>
      * <p>
      * An example that returns the "x" and "_id" fields for every document 
      * in the collection that has an "x" field:
      * </p>
      * <blockquote><pre>
      * BasicDBObject keys = new BasicDBObject();
      * keys.put("x", 1);
      *
      * DBCursor cursor = collection.find(new BasicDBObject(), keys); 
      * </pre></blockquote>
      *
      * @param ref object for which to search
      * @param keys fields to return
      * @return a cursor to iterate over results
      * @dochub find
      */
     public final DBCursor find( DBObject ref , DBObject keys ){
         return new DBCursor( this, ref, keys );
     }
 
     /** Queries for all objects in this collection. 
      * @return a cursor which will iterate over every object
      * @dochub find
      */
     public final DBCursor find(){
         return new DBCursor( this, new BasicDBObject(), null );
     }
 
     /** 
      * Returns a single object from this collection.
      * @return the object found, or <code>null</code> if the collection is empty
      */
     public final DBObject findOne()
         throws MongoException {
         return findOne( new BasicDBObject() );
     }
 
     /** 
      * Returns a single object from this collection matching the query.
      * @param o the query object
      * @return the object found, or <code>null</code> if no such object exists
      */
     public final DBObject findOne( DBObject o )
         throws MongoException {
         return findOne(o, null);
     }
 
     /** 
      * Returns a single object from this collection matching the query.
      * @param o the query object
      * @param fields fields to return
      * @return the object found, or <code>null</code> if no such object exists
      * @dochub find
      */
     public final DBObject findOne( DBObject o, DBObject fields ) {
         Iterator<DBObject> i = __find( o , fields , 0 , -1 , 0 );
         if ( i == null || ! i.hasNext() )
             return null;
         return i.next();
     }
 
     /** Adds the "private" fields _id to an object.
      * @param o <code>DBObject</code> to which to add fields
      * @return the modified parameter object
      */
     public final Object apply( DBObject o ){
         return apply( o , true );
     }
     
     /** Adds the "private" fields _id to an object.
      * @param jo object to which to add fields
      * @param ensureID whether to add an <code>_id</code> field or not
      * @return the modified object <code>o</code>
      */
     public final Object apply( DBObject jo , boolean ensureID ){
         
         Object id = jo.get( "_id" );
         if ( ensureID && id == null ){
             id = ObjectId.get();
             jo.put( "_id" , id );
         }
 
         doapply( jo );
 
         return id;
     }
 
     /** Saves an object to this collection.
      * @param jo the <code>DBObject</code> to save
      *        will add <code>_id</code> field to jo if needed
      */
     public final void save( DBObject jo )
         throws MongoException {
         if ( checkReadOnly( true ) ) 
             return;
 
         _checkObject( jo , false , false );
         
         //_findSubObject( s , jo , null );
 
         Object id = jo.get( "_id" );
         if ( DEBUG ) System.out.println( "id : " + id );
 
         if ( id == null || ( id instanceof ObjectId && ((ObjectId)id).isNew() ) ){
             if ( DEBUG ) System.out.println( "saving new object" );
             if ( id != null && id instanceof ObjectId )
                 ((ObjectId)id).notNew();
             insert( jo );
             return;
         }
 
         if ( DEBUG ) System.out.println( "doing implicit upsert : " + jo.get( "_id" ) );
         DBObject q = new BasicDBObject();
         q.put( "_id" , id );
         update( q , jo , true , false );
     }
     
     // ---- DB COMMANDS ----
     /** Drops all indices from this collection
      */
     public void dropIndexes()
         throws MongoException {
         dropIndexes( "*" );
     }
         
 
     public void dropIndexes( String name )
         throws MongoException {
         DBObject cmd = BasicDBObjectBuilder.start()
             .add( "deleteIndexes" , getName() )
             .add( "index" , name )
             .get();
         
         CommandResult res = _db.command( cmd );
         if ( res.ok() || res.getErrorMessage().equals( "ns not found" ) ){
             resetIndexCache();
             return;
         }
         
         throw new MongoException( "error dropping indexes : " + res );
     }
     
     /** Drops (deletes) this collection
      */
     public void drop()
         throws MongoException {
         CommandResult res =_db.command( BasicDBObjectBuilder.start().add( "drop" , getName() ).get() );
         if ( res.ok() || res.getErrorMessage().equals( "ns not found" ) )
             return;
         throw new MongoException( "error dropping : " + res );
     }
 
     public long count()
         throws MongoException {
         return getCount(new BasicDBObject(), null);
     }
 
     public long count(DBObject query)
         throws MongoException {
         return getCount(query, null);
     }
 
 
     /**
      *  Returns the number of documents in the collection
      *  @return number of documents that match query
      */
     public long getCount()
         throws MongoException {
         return getCount(new BasicDBObject(), null);
     }
 
     /**
      *  Returns the number of documents in the collection
      *  that match the specified query
      *
      *  @param query query to select documents to count
      *  @return number of documents that match query
      */
     public long getCount(DBObject query)
         throws MongoException {
         return getCount(query, null);
     }
 
     /**
      *  Returns the number of documents in the collection
      *  that match the specified query
      *
      *  @param query query to select documents to count
      *  @param fields fields to return
      *  @return number of documents that match query and fields
      */
     public long getCount(DBObject query, DBObject fields)
         throws MongoException {
 
         BasicDBObject cmd = new BasicDBObject();
         cmd.put("count", getName());
         cmd.put("query", query);
         if (fields != null) {
             cmd.put("fields", fields);
         }
 
         CommandResult res = _db.command(cmd);
 
         if ( ! res.ok() ){
             String errmsg = res.getErrorMessage();
             
             if ( errmsg.equals("ns does not exist") || 
                  errmsg.equals("ns missing" ) ){
                 // for now, return 0 - lets pretend it does exist
                 return 0;
             }
             
             throw new MongoException( "error counting : " + res );
         }
 
         return res.getLong("n");
     }
 
     /**
      * does a rename of this collection to newName
      * @param newName new collection name (not a full namespace)
      * @return the new collection
      */
     public DBCollection rename( String newName ) 
         throws MongoException {
         
         CommandResult ret = 
             _db.getSisterDB( "admin" )
             .command( BasicDBObjectBuilder.start()
                       .add( "renameCollection" , _fullName )
                       .add( "to" , _db._name + "." + newName )
                       .get() );
         ret.throwOnError();
         return _db.getCollection( newName );
     }
 
     /**
      * @param key - { a : true }
      * @param cond - optional condition on query 
      * @param reduce javascript reduce function 
      * @param initial initial value for first match on a key
      */
     public DBObject group( DBObject key , DBObject cond , DBObject initial , String reduce )
         throws MongoException {
         CommandResult res =  _db.command( new BasicDBObject( "group" , 
                                                              BasicDBObjectBuilder.start()
                                                              .add( "ns" , getName() )
                                                              .add( "key" , key )
                                                              .add( "cond" , cond )
                                                              .add( "$reduce" , reduce )
                                                              .add( "initial" , initial )
                                                              .get() ) );
         res.throwOnError();
         return (DBObject)res.get( "retval" );
     }
     
     /**
      * find distinct values for a key
      */
     public List distinct( String key ){
         return distinct( key , new BasicDBObject() );
     }
     
     /**
      * find distinct values for a key
      * @param query query to apply on collection
      */
 	public List distinct( String key , DBObject query ){
         DBObject c = BasicDBObjectBuilder.start()
             .add( "distinct" , getName() )
             .add( "key" , key )
             .add( "query" , query )
             .get();
         
         CommandResult res = _db.command( c );
         res.throwOnError();
         return (List)(res.get( "values" ));
     }
 
     /**
        performs a map reduce operation
        * @param outputCollection optional - leave null if want to use temp collection
        * @param query optional - leave null if you want all objects
        * @dochub mapreduce
      */
     public MapReduceOutput mapReduce( String map , String reduce , String outputCollection , DBObject query )
         throws MongoException {
         BasicDBObjectBuilder b = BasicDBObjectBuilder.start()
             .add( "mapreduce" , _name )
             .add( "map" , map )
             .add( "reduce" , reduce );
 
         if ( outputCollection != null )
             b.add( "out" , outputCollection );
         
         if ( query != null )
             b.add( "query" , query );
 
         return mapReduce( b.get() );
     }
     
     public MapReduceOutput mapReduce( DBObject command )
         throws MongoException {
         if ( command.get( "mapreduce" ) == null )
             throw new IllegalArgumentException( "need mapreduce arg" );
         CommandResult res = _db.command( command );
         res.throwOnError();
         return new MapReduceOutput( this , res );
     }
     
     /**
      *   Return a list of the indexes for this collection.  Each object
      *   in the list is the "info document" from MongoDB
      *
      *   @return list of index documents
      */
     public List<DBObject> getIndexInfo() {
         BasicDBObject cmd = new BasicDBObject();
         cmd.put("ns", getFullName());
 
         DBCursor cur = _db.getCollection("system.indexes").find(cmd);
 
         List<DBObject> list = new ArrayList<DBObject>();
 
         while(cur.hasNext()) {
             list.add(cur.next());
         }
 
         return list;
     }
 
     public void dropIndex( DBObject keys )
         throws MongoException {
         dropIndexes( genIndexName( keys ) );
     }
 
     public void dropIndex( String name )
         throws MongoException {
         dropIndexes( name );
     }
 
     // ------
 
     /** Initializes a new collection.
      * @param base database in which to create the collection
      * @param name the name of the collection
      */
     protected DBCollection( DB base , String name ){
         _db = base;
         _name = name;
         _fullName = _db.getName() + "." + name;
     }
 
     private  DBObject _checkObject( DBObject o , boolean canBeNull , boolean query ){
         if ( o == null ){
             if ( canBeNull )
                 return null;
             throw new IllegalArgumentException( "can't be null" );
         }
 
         if ( o.isPartialObject() && ! query )
             throw new IllegalArgumentException( "can't save partial objects" );
         
         if ( ! query ){
             _checkKeys(o);
         }
         return o;
     }
 
     /**
      * Checks key strings for invalid characters.
      */
     private void _checkKeys( DBObject o ) {
         for ( String s : o.keySet() ){
             if ( s.contains( "." ) )
                 throw new IllegalArgumentException( "fields stored in the db can't have . in them" );
             if ( s.charAt( 0 ) == '$' )
                 throw new IllegalArgumentException( "fields stored in the db can't start with '$'" );
 
             Object inner;
             if ( (inner = o.get( s )) instanceof DBObject ) {
                 _checkKeys( (DBObject)inner );
             }
         }
     }
 
     /** Find a collection that is prefixed with this collection's name.
      * A typical use of this might be 
      * <blockquote><pre>
      *    DBCollection users = mongo.getCollection( "wiki" ).getCollection( "users" );
      * </pre></blockquote>
      * Which is equilalent to
      * <pre><blockquote>
      *   DBCollection users = mongo.getCollection( "wiki.users" );
      * </pre></blockquote>
      * @param n the name of the collection to find
      * @return the matching collection
      */
     public DBCollection getCollection( String n ){
         return _db.getCollection( _name + "." + n );
     }
 
     /** Returns the name of this collection.
      * @return  the name of this collection
      */
     public String getName(){
         return _name;
     }
 
     /** Returns the full name of this collection, with the database name as a prefix.
      * @return  the name of this collection
      */
     public String getFullName(){
         return _fullName;
     }
 
     /** Returns the database this collection is a member of.
      * @return this collection's database
      */
     public DB getDB(){
         return _db;
     }
 
     /** Returns if this collection's database is read-only
      * @param strict if an exception should be thrown if the database is read-only
      * @return if this collection's database is read-only
      * @throws RuntimeException if the database is read-only and <code>strict</code> is set
      */
     protected boolean checkReadOnly( boolean strict ){
         if ( ! _db._readOnly )
             return false;
 
         if ( ! strict )
             return true;
 
         throw new IllegalStateException( "db is read only" );
     }
 
     /** Calculates the hash code for this collection.
      * @return the hash code
      */
     public int hashCode(){
         return _fullName.hashCode();
     }
 
     /** Checks if this collection is equal to another object.
      * @param o object with which to compare this collection
      * @return if the two collections are the same object
      */
     public boolean equals( Object o ){
         return o == this;
     }
 
     /** Returns name of the collection.
      * @return name of the collection.
      */
     public String toString(){
         return _name;
     }
 
     /** Set a default class for objects in this collection
      * @param c the class
      * @throws IllegalArgumentException if <code>c</code> is not a DBObject
      */
     public void setObjectClass( Class c ){
         if ( ! DBObject.class.isAssignableFrom( c ) )
             throw new IllegalArgumentException( c.getName() + " is not a DBObject" );
         _objectClass = c;
         if ( ReflectionDBObject.class.isAssignableFrom( c ) )
             _wrapper = ReflectionDBObject.getWrapper( c );
         else 
             _wrapper = null;
     }
     
     /** Gets the default class for objects in the collection
      * @return the class
      */
     public Class getObjectClass(){
         return _objectClass;
     }
 
     public void setInternalClass( String path , Class c ){
         _internalClass.put( path , c );
     }
 
     protected Class getInternalClass( String path ){
         Class c = _internalClass.get( path );
         if ( c != null )
             return c;
 
         if ( _wrapper == null )
             return null;
         return _wrapper.getInternalClass( path );
     }
 
     /**
      * Set the write concern for this collection. Will be used for
      * writes to this collection. Overrides any setting of write
      * concern at the DB level. See the documentation for
      * {@link DB.WriteConcern} for more information.
      *
      * @param concern write concern to use
      */
     public void setWriteConcern( DB.WriteConcern concern ){
         _concern = concern;
     }
 
     /**
      * Get the write concern for this collection.
      */
     public DB.WriteConcern getWriteConcern(){
         if ( _concern != null )
             return _concern;
         return _db.getWriteConcern();
     }
     
     final DB _db;
 
     final protected String _name;
     final protected String _fullName;
 
     protected List<DBObject> _hintFields;
     private DB.WriteConcern _concern = null;
 
     protected Class _objectClass = null;
     private Map<String,Class> _internalClass = Collections.synchronizedMap( new HashMap<String,Class>() );
     private ReflectionDBObject.JavaWrapper _wrapper = null;
 
     private boolean _anyUpdateSave = false;
 
     final private Set<String> _createIndexes = new HashSet<String>();
     final private Set<String> _createIndexesAfterSave = new HashSet<String>();
 }
