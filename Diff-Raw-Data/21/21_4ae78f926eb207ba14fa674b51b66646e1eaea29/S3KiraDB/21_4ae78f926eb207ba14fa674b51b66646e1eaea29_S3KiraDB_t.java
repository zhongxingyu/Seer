 package com.bdt.kiradb;
 
 /**
  * Construct an Amazon S3-backed instance of KiraDb.  The constructors require
  * the usual Amazon AWS S3 key, secret, and bucket name.
  */
 public class S3KiraDB extends Core {
     /**
      * Construct an S3 KiraDB instance with specified indexPath, AWS key, secret, and bucket name.
      *
      * @param indexPath The index path
      * @param awsKey    AWS S3 key
      * @param awsSecret AWS S3 secret
      * @param awsBucket AWS S3 bucketname
      */
     public S3KiraDB(String indexPath, String awsKey, String awsSecret, String awsBucket) {
         super(indexPath);
        super.setBackingStore(new S3BackingStore(awsKey, awsSecret, awsBucket));
     }
 
     /**
      * Construct an S3 KiraDB instance with specified indexPath, AWS key, secret, and bucket name.
      *
      * @param indexPath      The index path
      * @param disableCaching cache enabled
      * @param awsKey         AWS S3 key
      * @param awsSecret      AWS S3 secret
      * @param awsBucket      AWS S3 bucketname
      */
     public S3KiraDB(String indexPath, Boolean disableCaching, String awsKey, String awsSecret, String awsBucket) {
         super(indexPath, disableCaching);
        super.setBackingStore(new S3BackingStore(awsKey, awsSecret, awsBucket));
     }
 
     /**
      * Construct an S3 KiraDB instance with specified indexPath, AWS key, secret, and bucket name.
      *
      * @param indexPath  The index path
     * @param cacheStore cacheStore
      * @param awsKey     AWS S3 key
      * @param awsSecret  AWS S3 secret
      * @param awsBucket  AWS S3 bucketname
      */
     public S3KiraDB(String indexPath, BackingStore cacheStore, String awsKey, String awsSecret, String awsBucket) {
        super(indexPath, cacheStore);
        super.setBackingStore(new S3BackingStore(awsKey, awsSecret, awsBucket));
     }
 
     /**
      * A no-op method that does nothing.  The backing store will have already been set by virtue of using this class to begin with.
      *
      * @param backingStore
      * @return
      */
     public Core setBackingStore(BackingStore backingStore) {
         return this;
     }
 }
