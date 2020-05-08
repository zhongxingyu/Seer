 /*
  * Copyright 2010 SpringSource
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
 
 package org.springframework.aws.ivy;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.ivy.plugins.repository.AbstractRepository;
 import org.apache.ivy.plugins.repository.RepositoryCopyProgressListener;
 import org.apache.ivy.plugins.repository.Resource;
 import org.apache.ivy.plugins.repository.TransferEvent;
 import org.apache.ivy.util.FileUtil;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.security.AWSCredentials;
 
 /**
  * A repository the allows you to upload and download from an S3 repository.
  * 
  * @author Ben Hale
  */
 public class S3Repository extends AbstractRepository {
 
 	private String accessKey;
 
 	private String secretKey;
 
 	private S3Service service;
 
 	private Map<String, S3Resource> resourceCache = new HashMap<String, S3Resource>();
 
 	public void setAccessKey(String accessKey) {
 		this.accessKey = accessKey;
 	}
 
 	public void setSecretKey(String secretKey) {
 		this.secretKey = secretKey;
 	}
 
 	public void get(String source, File destination) throws IOException {
 		Resource resource = getResource(source);
 		try {
 			fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
 			RepositoryCopyProgressListener progressListener = new RepositoryCopyProgressListener(this);
 			progressListener.setTotalLength(resource.getContentLength());
 			FileUtil.copy(resource.openStream(), new FileOutputStream(destination), progressListener);
 		}
 		catch (IOException e) {
 			fireTransferError(e);
 			throw e;
 		}
 		catch (RuntimeException e) {
 			fireTransferError(e);
 			throw e;
 		}
 		finally {
 			fireTransferCompleted(resource.getContentLength());
 		}
 	}
 
 	public Resource getResource(String source) throws IOException {
 		if (!resourceCache.containsKey(source)) {
 			resourceCache.put(source, new S3Resource(getService(), source));
 		}
 		return resourceCache.get(source);
 	}
 
 	public List<String> list(String parent) throws IOException {
 		S3Bucket bucket = S3Utils.getBucket(parent);
 		String key = S3Utils.getKey(parent);
 
 		try {
 			S3Object[] objects = getService().listObjects(bucket, key, "");
 			List<String> keys = new ArrayList<String>(objects.length);
 			for (S3Object object : objects) {
				keys.add(object.getKey());
 			}
 			return keys;
 		}
 		catch (S3ServiceException e) {
 			throw new S3RepositoryException(e);
 		}
 	}
 
 	@Override
 	protected void put(File source, String destination, boolean overwrite) throws IOException {
 		S3Bucket bucket = S3Utils.getBucket(destination);
 		String key = S3Utils.getKey(destination);
 		buildDestinationPath(bucket, getDestinationPath(key));
 
 		S3Object object = new S3Object(bucket, key);
 		object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
 		object.setDataInputFile(source);
 		object.setContentLength(source.length());
 		try {
 			getService().putObject(bucket, object);
 		}
 		catch (S3ServiceException e) {
 			throw new S3RepositoryException(e);
 		}
 	}
 
 	private S3Service getService() {
 		if (service == null) {
 			try {
 				service = new RestS3Service(getCredentials());
 			}
 			catch (S3ServiceException e) {
 				throw new S3RepositoryException(e);
 			}
 		}
 		return service;
 	}
 
 	private AWSCredentials getCredentials() {
 		if (accessKey.length() > 0 && secretKey.length() > 0) {
 			return new AWSCredentials(accessKey, secretKey);
 		}
 		return null;
 	}
 
 	private void buildDestinationPath(S3Bucket bucket, String destination) {
 		S3Object object = new S3Object(bucket, destination + "/");
 		object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
 		object.setContentLength(0);
 		try {
 			getService().putObject(bucket, object);
 		}
 		catch (S3ServiceException e) {
 			throw new S3RepositoryException(e);
 		}
 
 		int index = destination.lastIndexOf('/');
 		if (index != -1) {
 			buildDestinationPath(bucket, destination.substring(0, index));
 		}
 	}
 
 	private String getDestinationPath(String destination) {
 		return destination.substring(0, destination.lastIndexOf('/'));
 	}
 
 }
