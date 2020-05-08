 /**
  *  Copyright 2013 Giwi Softwares (http://giwi.free.fr)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0 
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.giwi.camel.dav;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.InvalidPayloadException;
 import org.apache.camel.component.file.FileComponent;
 import org.apache.camel.component.file.GenericFile;
 import org.apache.camel.component.file.GenericFileEndpoint;
 import org.apache.camel.component.file.GenericFileExist;
 import org.apache.camel.component.file.GenericFileOperationFailedException;
 import org.apache.camel.util.FileUtil;
 import org.apache.camel.util.IOHelper;
 import org.apache.camel.util.ObjectHelper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.googlecode.sardine.DavResource;
 import com.googlecode.sardine.Sardine;
 import com.googlecode.sardine.impl.SardineException;
 
 /**
  * The Class DavOperations.
  * 
  * @author Giwi Softwares
  */
 public class DavOperations implements RemoteFileOperations<DavResource> {
 
     /** The Constant LOG. */
     protected static final Logger LOG = LoggerFactory
 	    .getLogger(DavOperations.class);
 
     /** The client. */
     protected final Sardine client;
 
     /** The endpoint. */
     protected RemoteFileEndpoint<DavResource> endpoint;
 
     /**
      * Instantiates a new dav operations.
      * 
      * @param client
      *            the Sardine client
      */
     public DavOperations(Sardine client) {
 	this.client = client;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#setEndpoint(org
      * .apache.camel.component.file.GenericFileEndpoint)
      */
     @Override
     public void setEndpoint(GenericFileEndpoint<DavResource> endpoint) {
 	this.endpoint = (RemoteFileEndpoint<DavResource>) endpoint;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#deleteFile(java
      * .lang.String)
      */
     @Override
     public boolean deleteFile(String name)
 	    throws GenericFileOperationFailedException {
 	name = sanitizeWithHost(name);
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("deleteFile : " + name);
 	}
 	try {
 	    client.delete(name);
 	} catch (SardineException e) {
 	    throw new GenericFileOperationFailedException(e.getStatusCode(),
 		    e.getMessage(), e);
 	} catch (IOException e) {
 	    throw new GenericFileOperationFailedException(e.getMessage(), e);
 	}
 	return true;
     }
 
     /**
      * Sanitize path.
      * 
      * @param path
      *            the path to be sanitized
      * @return a clean path
      */
     private String sanitizePath(String path) {
 	// WTF : INFO deleteFile :
 	// http://localhost:80/webdav/movefile//http:/localhost:80/webdav/movefile/.done/goodday.txt
 	String sPath = path.replaceAll(endpoint.getConfiguration()
 		.getProtocol() + ":/" + endpoint.getConfiguration().getHost(),
 		endpoint.getConfiguration().getProtocol() + "://"
 			+ endpoint.getConfiguration().getHost());
 	sPath = sPath.replaceAll(endpoint.getConfiguration()
 		.getRemoteServerInformation(), "");
 	sPath = FileUtil.stripLeadingSeparator(sPath);
 
	if (LOG.isTraceEnabled()) {
	    LOG.trace("sanitize from path " + path + " to " + sPath);
 	}
 	return sPath;
     }
 
     /**
      * Sanitize with host.
      * 
      * @param path
      *            the path to be sanitized with the hostname
      * @return a clean path
      */
     private String sanitizeWithHost(String path) {
 	String sPath = sanitizePath(path);
 	sPath = endpoint.getConfiguration().getRemoteServerInformation()
 		+ FileUtil.stripLeadingSeparator(path.replaceFirst(endpoint
 			.getConfiguration().getInitialDirectory(), ""));
	if (LOG.isTraceEnabled()) {
	    LOG.trace("sanitize with host from path " + path + " to " + sPath);
 	}
 	return sPath;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#existsFile(java
      * .lang.String)
      */
     @Override
     public boolean existsFile(String name)
 	    throws GenericFileOperationFailedException {
 	if (!name.startsWith(endpoint.getConfiguration().getHostPath())) {
 	    name = sanitizeWithHost(name);
 	}
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("existsFile : " + name);
 	}
 	try {
 	    return client.exists(name);
 	} catch (SardineException e) {
 	    throw new GenericFileOperationFailedException(e.getStatusCode(),
 		    e.getMessage(), e);
 	} catch (IOException e) {
 	    throw new GenericFileOperationFailedException(e.getMessage(), e);
 	}
     }
 
     /**
      * Exists init dir.
      * 
      * @param name
      *            the name
      * @return true, if successful
      * @throws GenericFileOperationFailedException
      *             the generic file operation failed exception
      */
     public boolean existsInitDir(String name)
 	    throws GenericFileOperationFailedException {
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("existsInitDir : " + name);
 	}
 	try {
 	    return client.exists(name);
 	} catch (UnknownHostException e) {
 	    throw new GenericFileOperationFailedException(404,
 		    "Unknown Host : " + e.getMessage(), e);
 	} catch (SardineException e) {
 	    throw new GenericFileOperationFailedException(e.getStatusCode(),
 		    e.getMessage(), e);
 	} catch (IOException e) {
 	    throw new GenericFileOperationFailedException(e.getMessage(), e);
 	}
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#renameFile(java
      * .lang.String, java.lang.String)
      */
     @Override
     public boolean renameFile(String from, String to)
 	    throws GenericFileOperationFailedException {
 	try {
 	    to = sanitizeWithHost(to);
 	    from = sanitizeWithHost(from);
	    if (LOG.isTraceEnabled()) {
		LOG.trace("renameFile from " + from + " to : " + to);
 	    }
 	    client.move(from, to);
 	} catch (SardineException e) {
 	    throw new GenericFileOperationFailedException(e.getStatusCode(),
 		    e.getMessage(), e);
 	} catch (IOException e) {
 	    throw new GenericFileOperationFailedException(e.getMessage(), e);
 	}
 	return true;
     }
 
     /**
      * Inits the component.
      * 
      * @param path
      *            the path
      */
     public void initComponent(String path) {
 	String[] dirs = path.split("/");
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("initComponent : " + Arrays.asList(dirs));
 	}
 	StringBuilder dirToBuild = new StringBuilder();
 	for (String dir : dirs) {
 	    if (!"".equals(dir.trim())) {
 		dirToBuild.append(dir).append("/");
 		if (LOG.isTraceEnabled()) {
 		    LOG.trace("initComponent dirToBuild : " + dirToBuild);
 		}
 		try {
 		    if (!existsInitDir(endpoint.getConfiguration()
 			    .getHostPath() + dirToBuild.toString())) {
 			if (LOG.isTraceEnabled()) {
 			    LOG.trace("initComponent : "
 				    + endpoint.getConfiguration().getHostPath()
 				    + dirToBuild.toString());
 			}
 			client.createDirectory(endpoint.getConfiguration()
 				.getHostPath() + dirToBuild.toString());
 		    }
 		} catch (SardineException e) {
 		    throw new GenericFileOperationFailedException(
 			    e.getStatusCode(), e.getMessage(), e);
 		} catch (IOException e) {
 		    throw new GenericFileOperationFailedException(
 			    e.getMessage(), e);
 		}
 	    }
 
 	}
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#buildDirectory(
      * java.lang.String, boolean)
      */
     @Override
     public boolean buildDirectory(String directory, boolean absolute)
 	    throws GenericFileOperationFailedException {
 	// WTF ?!? http:/localhost:80/webdav/ in directory
 	directory = sanitizeWithHost(directory);
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("buildDirectory 1 : " + directory + ", absolute : "
 		    + absolute);
 	}
 
 	directory = directory.replaceAll(endpoint.getConfiguration()
 		.getRemoteServerInformation(), "");
 	String[] dirs = directory.split("/");
 	StringBuilder dirToBuild = new StringBuilder();
 	for (String dir : dirs) {
 	    if (!"".equals(dir.trim())) {
 		dirToBuild.append(dir).append("/");
 		if (LOG.isTraceEnabled()) {
 		    LOG.trace("buildDirectory : " + dirToBuild);
 		}
 		try {
 		    if (!existsFile(endpoint.getConfiguration()
 			    .getRemoteServerInformation()
 			    + dirToBuild.toString())) {
 			if (LOG.isTraceEnabled()) {
 			    LOG.trace("buildDirectory : " + dirToBuild);
 			}
 			client.createDirectory(endpoint.getConfiguration()
 				.getRemoteServerInformation()
 				+ dirToBuild.toString());
 		    }
 		} catch (SardineException e) {
 		    throw new GenericFileOperationFailedException(
 			    e.getStatusCode(), e.getMessage(), e);
 		} catch (IOException e) {
 		    throw new GenericFileOperationFailedException(
 			    e.getMessage(), e);
 		}
 	    }
 
 	}
 	return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#retrieveFile(java
      * .lang.String, org.apache.camel.Exchange)
      */
     @Override
     public boolean retrieveFile(String name, Exchange exchange)
 	    throws GenericFileOperationFailedException {
 	name = sanitizeWithHost(name);
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("retrieveFile : " + name);
 	}
 	if (ObjectHelper.isNotEmpty(endpoint.getLocalWorkDirectory())) {
 	    // local work directory is configured so we should store file
 	    // content as files in this local directory
 	    return retrieveFileToFileInLocalWorkDirectory(name, exchange);
 	} else {
 	    // store file content directory as stream on the body
 	    return retrieveFileToStreamInBody(name, exchange);
 	}
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#storeFile(java.
      * lang.String, org.apache.camel.Exchange)
      */
     @Override
     public boolean storeFile(String name, Exchange exchange)
 	    throws GenericFileOperationFailedException {
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("storeFile({})", name);
 	}
 	String targetName = name;
 	// store the file
 	return doStoreFile(name, targetName, exchange);
     }
 
     /**
      * Do store file.
      * 
      * @param name
      *            the name
      * @param targetName
      *            the target name
      * @param exchange
      *            the exchange
      * @return true, if successful
      * @throws GenericFileOperationFailedException
      *             the generic file operation failed exception
      */
     private boolean doStoreFile(String name, String targetName,
 	    Exchange exchange) throws GenericFileOperationFailedException {
 	LOG.trace("doStoreFile({})", targetName);
 	// if an existing file already exists what should we do?
 	if (endpoint.getFileExist() == GenericFileExist.Ignore
 		|| endpoint.getFileExist() == GenericFileExist.Fail
 		|| endpoint.getFileExist() == GenericFileExist.Move) {
 	    boolean existFile = existsFile(targetName);
 	    if (existFile && endpoint.getFileExist() == GenericFileExist.Ignore) {
 		// ignore but indicate that the file was written
 		if (LOG.isDebugEnabled()) {
 		    LOG.debug(
 			    "An existing file already exists: {}. Ignore and do not override it.",
 			    name);
 		}
 		return true;
 	    } else if (existFile
 		    && endpoint.getFileExist() == GenericFileExist.Fail) {
 		throw new GenericFileOperationFailedException(
 			"File already exist: " + name
 				+ ". Cannot write new file.");
 	    } else if (existFile
 		    && endpoint.getFileExist() == GenericFileExist.Move) {
 		// move any existing file first
 		doMoveExistingFile(name, targetName);
 	    }
 	}
 
 	InputStream is = null;
 	if (exchange.getIn().getBody() == null) {
 	    // Do an explicit test for a null body and decide what to do
 	    if (endpoint.isAllowNullBody()) {
 		if (LOG.isDebugEnabled()) {
 		    LOG.debug("Writing empty file.");
 		}
 		is = new ByteArrayInputStream(new byte[] {});
 	    } else {
 		throw new GenericFileOperationFailedException(
 			"Cannot write null body to file: " + name);
 	    }
 	}
 
 	try {
 
 	    if (endpoint.getFileExist() == GenericFileExist.Append
 		    && existsFile(name)) {
 		if (LOG.isTraceEnabled()) {
 		    LOG.trace("Client appendFile: {}", targetName);
 		}
 		File tmp = FileUtil.createTempFile("", "");
 		FileOutputStream os = new FileOutputStream(tmp, true);
 
 		IOHelper.copy(
 			client.get(endpoint.getConfiguration()
 				.getRemoteServerInformation() + name), os);
 
 		IOHelper.copy(
 			exchange.getIn().getMandatoryBody(InputStream.class),
 			os);
 		IOHelper.close(os, "store: " + name, LOG);
 		is = new FileInputStream(tmp);
 		client.put(endpoint.getConfiguration()
 			.getRemoteServerInformation() + name, is);
 	    } else {
 		if (is == null) {
 		    is = exchange.getIn().getMandatoryBody(InputStream.class);
 		}
 		if (LOG.isTraceEnabled()) {
 		    LOG.trace("Client storeFile: {}", targetName);
 		}
 		client.put(endpoint.getConfiguration()
 			.getRemoteServerInformation() + name, is);
 	    }
 
 	    return true;
 	} catch (SardineException e) {
 	    throw new GenericFileOperationFailedException(e.getStatusCode(),
 		    e.getMessage(), e);
 	} catch (IOException e) {
 	    throw new GenericFileOperationFailedException(e.getMessage(), e);
 	} catch (InvalidPayloadException e) {
 	    throw new GenericFileOperationFailedException("Cannot store file: "
 		    + name, e);
 	} finally {
 	    IOHelper.close(is, "store: " + name, LOG);
 	}
     }
 
     /**
      * Moves any existing file due fileExists=Move is in use.
      * 
      * @param name
      *            the name
      * @param targetName
      *            the target name
      * @throws GenericFileOperationFailedException
      *             the generic file operation failed exception
      */
     private void doMoveExistingFile(String name, String targetName)
 	    throws GenericFileOperationFailedException {
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("doMoveExistingFile name=" + name + " targetName="
 		    + targetName);
 	}
 	// need to evaluate using a dummy and simulate the file first, to have
 	// access to all the file attributes
 	// create a dummy exchange as Exchange is needed for expression
 	// evaluation
 	// we support only the following 3 tokens.
 	Exchange dummy = endpoint.createExchange();
 	// we only support relative paths for the dav component, so dont provide
 	// any parent
 	String parent = null;
 	String onlyName = FileUtil.stripPath(targetName);
 	dummy.getIn().setHeader(Exchange.FILE_NAME, targetName);
 	dummy.getIn().setHeader(Exchange.FILE_NAME_ONLY, onlyName);
 	dummy.getIn().setHeader(Exchange.FILE_PARENT, parent);
 
 	String to = endpoint.getMoveExisting().evaluate(dummy, String.class);
 	// we only support relative paths for the dav component, so strip any
 	// leading paths
 	to = FileUtil.stripLeadingSeparator(to);
 	// normalize accordingly to configuration
 	// to = endpoint.getConfiguration().normalizePath(to);
 	if (ObjectHelper.isEmpty(to)) {
 	    throw new GenericFileOperationFailedException(
 		    "moveExisting evaluated as empty String, cannot move existing file: "
 			    + name);
 	}
 
 	// do we have a sub directory
 	String dir = FileUtil.onlyPath(to);
 	if (dir != null) {
 	    // ensure directory exists
 	    buildDirectory(dir, false);
 	}
 
 	// deal if there already exists a file
 	if (existsFile(to)) {
 	    if (endpoint.isEagerDeleteTargetFile()) {
 		if (LOG.isTraceEnabled()) {
 		    LOG.trace("Deleting existing file: {}", to);
 		}
 		try {
 		    client.delete(endpoint.getConfiguration()
 			    .getRemoteServerInformation()
 			    + FileUtil.stripLeadingSeparator(to));
 		} catch (SardineException e) {
 		    throw new GenericFileOperationFailedException(
 			    e.getStatusCode(), e.getMessage(), e);
 		} catch (IOException e) {
 		    throw new GenericFileOperationFailedException(
 			    "Cannot delete file: " + to, e);
 		}
 	    } else {
 		throw new GenericFileOperationFailedException(
 			"Cannot moved existing file from: " + name + " to: "
 				+ to + " as there already exists a file: " + to);
 	    }
 	}
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("Moving existing file: {} to: {}", name, to);
 	}
 	if (!renameFile(targetName, to)) {
 	    throw new GenericFileOperationFailedException(
 		    "Cannot rename file from: " + name + " to: " + to);
 	}
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#getCurrentDirectory
      * ()
      */
     @Override
     public String getCurrentDirectory()
 	    throws GenericFileOperationFailedException {
 	// noop
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("noop : getCurrentDirectory()");
 	}
 	return "***";
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#changeCurrentDirectory
      * (java.lang.String)
      */
     @Override
     public void changeCurrentDirectory(String path)
 	    throws GenericFileOperationFailedException {
 	// noop
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("noop : changeCurrentDirectory(String path)");
 	}
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#changeToParentDirectory
      * ()
      */
     @Override
     public void changeToParentDirectory()
 	    throws GenericFileOperationFailedException {
 	// noop
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("noop : changeToParentDirectory()");
 	}
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.apache.camel.component.file.GenericFileOperations#listFiles()
      */
     @Override
     public List<DavResource> listFiles()
 	    throws GenericFileOperationFailedException {
 	// noop
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("noop : listFiles()");
 	}
 	return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.camel.component.file.GenericFileOperations#listFiles(java.
      * lang.String)
      */
     @Override
     public List<DavResource> listFiles(String path)
 	    throws GenericFileOperationFailedException {
 	try {
 	    if ("".equals(path)) {
 		path = endpoint.getConfiguration().getRemoteServerInformation();
 	    } else if (path.startsWith(endpoint.getConfiguration()
 		    .getHostPath())) {
 		path = sanitizeWithHost(path);
 	    } else {
 		path = path.replaceAll(endpoint.getConfiguration()
 			.getProtocol()
 			+ ":/"
 			+ endpoint.getConfiguration().getHost(), endpoint
 			.getConfiguration().getProtocol()
 			+ "://"
 			+ endpoint.getConfiguration().getHost());
 	    }
 	    if (!path.startsWith(endpoint.getConfiguration().getHostPath())) {
 		if (path.startsWith(endpoint.getConfiguration()
 			.getInitialDirectory())) {
 		    path = endpoint.getConfiguration().getHostPath()
 			    + FileUtil.stripLeadingSeparator(path);
 		} else {
 		    path = endpoint.getConfiguration()
 			    .getRemoteServerInformation()
 			    + FileUtil.stripLeadingSeparator(path);
 		}
 	    }
 	    if (LOG.isTraceEnabled()) {
 		LOG.trace("listFiles " + path);
 	    }
 	    List<DavResource> response = new ArrayList<DavResource>();
 	    List<DavResource> resources = client.list(path);
 	    for (DavResource res : resources) {
 		if (!endpoint
 			.getConfiguration()
 			.getInitialDirectory()
 			.equals(FileUtil.stripLeadingSeparator(FileUtil
 				.stripTrailingSeparator(res.getPath())))) {
 		    response.add(res);
 		}
 	    }
 	    return response;
 	} catch (SardineException e) {
 	    throw new GenericFileOperationFailedException(e.getStatusCode(),
 		    e.getMessage(), e);
 	} catch (UnknownHostException e) {
 	    throw new GenericFileOperationFailedException(404,
 		    "Unknown Host : " + e.getMessage(), e);
 	} catch (IOException e) {
 	    throw new GenericFileOperationFailedException(e.getMessage(), e);
 	}
     }
 
     /**
      * Retrieve file to stream in body.
      * 
      * @param name
      *            the name
      * @param exchange
      *            the exchange
      * @return true, if successful
      * @throws GenericFileOperationFailedException
      *             the generic file operation failed exception
      */
     @SuppressWarnings("unchecked")
     private boolean retrieveFileToStreamInBody(String name, Exchange exchange)
 	    throws GenericFileOperationFailedException {
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("retrieveFileToStreamInBody({})", name);
 	}
 	OutputStream os = null;
 	boolean result = false;
 	try {
 	    os = new ByteArrayOutputStream();
 	    GenericFile<DavResource> target = (GenericFile<DavResource>) exchange
 		    .getProperty(FileComponent.FILE_EXCHANGE_FILE);
 	    ObjectHelper.notNull(target, "Exchange should have the "
 		    + FileComponent.FILE_EXCHANGE_FILE + " set");
 
 	    String remoteName = FileUtil.stripPath(name);
 	    if (LOG.isTraceEnabled()) {
 		LOG.trace("Client retrieveFile: {}", remoteName);
 	    }
 	    DavResource file = client.list(name).get(0);
 	    String localName = (endpoint.getConfiguration().getHostPath() + FileUtil
 		    .stripLeadingSeparator(file.getPath())).replaceAll(endpoint
 		    .getConfiguration().getRemoteServerInformation(), "");
 	    if (endpoint.getConfiguration().isDownload()) {
 		target.setBody(os);
 		if (!name.startsWith(endpoint.getConfiguration().getHostPath())) {
 		    name = endpoint.getConfiguration()
 			    .getRemoteServerInformation() + name;
 		}
 		InputStream is = client.get(name);
 
 		IOHelper.copyAndCloseInput(is, os);
 		if (LOG.isTraceEnabled()) {
 		    LOG.trace("Client retrieveFile: {}", localName);
 		}
 		exchange.getIn().setHeader("CamelFileLength",
 			file.getContentLength());
 		exchange.getIn().setHeader(Exchange.FILE_LAST_MODIFIED,
 			file.getModified());
 
 	    } else {
 		exchange.getIn().setBody(null);
 	    }
 	    exchange.getIn().setHeader(Exchange.FILE_NAME,
 		    FileUtil.stripLeadingSeparator(localName));
 	    exchange.getIn().setHeader(Exchange.FILE_NAME_ONLY, file.getName());
 	    exchange.getIn().setHeader(Exchange.FILE_NAME_PRODUCED, localName);
 	    result = true;
 	} catch (SardineException e) {
 	    throw new GenericFileOperationFailedException(e.getStatusCode(),
 		    e.getMessage(), e);
 	} catch (IOException e) {
 	    throw new GenericFileOperationFailedException(e.getMessage(), e);
 	} finally {
 	    IOHelper.close(os, "retrieve: " + name, LOG);
 	}
 
 	return result;
     }
 
     /**
      * Retrieve file to file in local work directory.
      * 
      * @param name
      *            the name
      * @param exchange
      *            the exchange
      * @return true, if successful
      * @throws GenericFileOperationFailedException
      *             the generic file operation failed exception
      */
     @SuppressWarnings("unchecked")
     private boolean retrieveFileToFileInLocalWorkDirectory(String name,
 	    Exchange exchange) throws GenericFileOperationFailedException {
 	if (LOG.isTraceEnabled()) {
 	    LOG.trace("retrieveFileToFileInLocalWorkDirectory({})", name);
 	}
 	File temp;
 	File local = new File(FileUtil.normalizePath(endpoint
 		.getLocalWorkDirectory()));
 	OutputStream os;
 	try {
 	    // use relative filename in local work directory
 	    GenericFile<DavResource> target = (GenericFile<DavResource>) exchange
 		    .getProperty(FileComponent.FILE_EXCHANGE_FILE);
 	    ObjectHelper.notNull(target, "Exchange should have the "
 		    + FileComponent.FILE_EXCHANGE_FILE + " set");
 	    String relativeName = FileUtil.normalizePath(target
 		    .getRelativeFilePath());
 	    temp = new File(local, relativeName + ".inprogress");
 	    local = new File(local, relativeName);
 	    // create directory to local work file
 	    local.mkdirs();
 
 	    // delete any existing files
 	    if (temp.exists()) {
 		if (!FileUtil.deleteFile(temp)) {
 		    throw new GenericFileOperationFailedException(
 			    "Cannot delete existing local work file: " + temp);
 		}
 	    }
 	    if (local.exists()) {
 		if (!FileUtil.deleteFile(local)) {
 		    throw new GenericFileOperationFailedException(
 			    "Cannot delete existing local work file: " + local);
 		}
 	    }
 
 	    // create new temp local work file
 	    if (!temp.createNewFile()) {
 		throw new GenericFileOperationFailedException(
 			"Cannot create new local work file: " + temp);
 	    }
 
 	    // store content as a file in the local work directory in the temp
 	    // handle
 	    os = new FileOutputStream(temp);
 
 	    // set header with the path to the local work file
 	    exchange.getIn().setHeader(Exchange.FILE_LOCAL_WORK_PATH,
 		    local.getPath());
 
 	} catch (Exception e) {
 	    throw new GenericFileOperationFailedException(
 		    "Cannot create new local work file: " + local);
 	}
 
 	boolean result = false;
 	try {
 	    GenericFile<DavResource> target = (GenericFile<DavResource>) exchange
 		    .getProperty(FileComponent.FILE_EXCHANGE_FILE);
 	    // store the java.io.File handle as the body
 	    target.setBody(local);
 	    if (LOG.isTraceEnabled()) {
 		LOG.trace("Client retrieveFileToFileInLocalWorkDirectory: {}",
 			name);
 	    }
 	    InputStream is = client.get(name);
 	    DavResource file = client.list(name).get(0);
 	    exchange.getIn().setHeader("CamelFileLength",
 		    file.getContentLength());
 	    exchange.getIn().setHeader(Exchange.FILE_LAST_MODIFIED,
 		    file.getModified());
 	    exchange.getIn().setHeader(Exchange.FILE_NAME, file.getName());
 	    exchange.getIn().setHeader(Exchange.FILE_NAME_ONLY, file.getName());
 	    exchange.getIn().setHeader(Exchange.FILE_NAME_PRODUCED,
 		    endpoint.getConfiguration().getHostPath() + file.getPath());
 	    IOHelper.copyAndCloseInput(is, os);
 	    result = true;
 
 	} catch (SardineException e) {
 	    throw new GenericFileOperationFailedException(e.getStatusCode(),
 		    e.getMessage(), e);
 	} catch (IOException e) {
 	    throw new GenericFileOperationFailedException(
 		    "Cannot create new local work file: " + local);
 	} finally {
 	    // need to close the stream before rename it
 	    IOHelper.close(os, "retrieve: " + name, LOG);
 	}
 	if (LOG.isDebugEnabled()) {
 	    LOG.debug("Retrieve file to local work file result: {}", result);
 	}
 
 	if (result) {
 	    if (LOG.isTraceEnabled()) {
 		LOG.trace("Renaming local in progress file from: {} to: {}",
 			temp, local);
 	    }
 	    // operation went okay so rename temp to local after we have
 	    // retrieved the data
 	    try {
 		if (!FileUtil.renameFile(temp, local, false)) {
 		    throw new GenericFileOperationFailedException(
 			    "Cannot rename local work file from: " + temp
 				    + " to: " + local);
 		}
 	    } catch (IOException e) {
 		throw new GenericFileOperationFailedException(
 			"Cannot rename local work file from: " + temp + " to: "
 				+ local, e);
 	    }
 	}
 
 	return result;
     }
 
 }
