 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.elasticgrid.storage.amazon.s3;
 
 import com.elasticgrid.storage.Container;
 import com.elasticgrid.storage.ContainerNotFoundException;
 import com.elasticgrid.storage.StorageException;
 import com.elasticgrid.storage.spi.StorageEngine;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.security.AWSCredentials;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * {@link StorageEngine} providing support for Amazon S3.
  *
  * @author Jerome Bernard
  */
 public class S3StorageEngine implements StorageEngine {
     private final S3Service s3;
     private final Logger logger = Logger.getLogger(S3StorageEngine.class.getName());
 
     public S3StorageEngine(String awsAccessId, String awsSecretKey) throws S3ServiceException {
         s3 = new RestS3Service(new AWSCredentials(awsAccessId, awsSecretKey));
     }
 
     public String getStorageName() {
         return "Amazon S3";
     }
 
     public List<Container> getContainers() throws StorageException {
         try {
             logger.log(Level.FINE, "Retrieving list of S3 buckets");
             S3Bucket[] buckets = s3.listAllBuckets();
             List<Container> containers = new ArrayList<Container>(buckets.length);
             for (S3Bucket bucket : buckets) {
                 containers.add(new S3Container(s3, bucket));
             }
             return containers;
         } catch (S3ServiceException e) {
             throw new StorageException("Can't get list of containers", e);
         }
     }
 
     public Container createContainer(String name) throws StorageException {
         try {
             logger.log(Level.FINE, "Creating S3 bucket {0}", name);
            S3Bucket bucket = s3.createBucket(name);
             return new S3Container(s3, bucket);
         } catch (S3ServiceException e) {
             throw new StorageException("Can't create container", e);
         }
     }
 
     public Container findContainerByName(String name) throws StorageException {
         try {
             logger.log(Level.FINE, "Searching for S3 bucket {0}", name);
             S3Bucket bucket = s3.getBucket(name);
             if (bucket == null)
                 throw new ContainerNotFoundException(name);
             return new S3Container(s3, bucket);
         } catch (S3ServiceException e) {
             throw new StorageException("Can't find container", e);
         }
     }
 
     public void deleteContainer(String name) throws StorageException {
         try {
             logger.log(Level.FINE, "Deleting S3 bucket {0}", name);
             S3Bucket bucket;
             if ((bucket = s3.getBucket(name)) == null)
                 throw new ContainerNotFoundException(name);
             S3Object[] objects = s3.listObjects(bucket);
             for (S3Object o : objects)
                 s3.deleteObject(bucket, o.getKey());
             s3.deleteBucket(bucket);
         } catch (S3ServiceException e) {
             throw new StorageException("Can't delete container", e);
         }
     }
 }
