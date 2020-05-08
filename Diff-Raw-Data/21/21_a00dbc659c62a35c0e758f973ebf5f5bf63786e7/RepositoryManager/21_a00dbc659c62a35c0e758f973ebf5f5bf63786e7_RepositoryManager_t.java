 /*******************************************************************************
  * Copyright (c) 2010 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.p2.internal.deploy.server;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 import org.eclipse.swordfish.p2.deploy.server.IRepositoryManager;
 
 public class RepositoryManager implements IRepositoryManager {
 	private static final Log LOG = LogFactory.getLog(RepositoryManager.class);
 	private IProvisioningAgent agent;
 
 	private IProgressMonitor monitor = new NullProgressMonitor();
 
 	private IMetadataRepositoryManager metadataRepositoryManager;
 	private IArtifactRepositoryManager artifactRepositoryManager;
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.swordfish.p2.internal.deploy.server.RepositoryManager#load(java.lang.String, java.io.InputStream)
 	 */
 	public URI load(String iuIdVersion, InputStream input) throws IOException, ProvisionException, OperationCanceledException {
 		LOG.info("Load " + iuIdVersion);
 		assertAgentAndServicesAvailable();
		
		if (input == null) {
			throw new IllegalArgumentException("Input stream must not be null!");
		}
		
 		File repository = unzip(iuIdVersion, input);
 		URI repoUri = repository.toURI();
 		
 		metadataRepositoryManager.loadRepository(repoUri, monitor);
 		artifactRepositoryManager.loadRepository(repoUri, monitor);
 		LOG.info("Loaded repository " + repoUri);
 		return repoUri;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swordfish.p2.internal.deploy.server.RepositoryManager#remove(java.net.URI)
 	 */
 	public boolean remove(URI repoUri) {
 		assertAgentAndServicesAvailable();
 		boolean removeArtifactRepo = artifactRepositoryManager.removeRepository(repoUri);
 		LOG.debug("Removed artifact repository = " + removeArtifactRepo);
 		boolean removeMetaRepo = metadataRepositoryManager.removeRepository(repoUri);
 		LOG.debug("Removed metadata repository = " + removeMetaRepo);
 		return removeMetaRepo && removeArtifactRepo;
 	}
 
 
 	public final void setProvisioningAgent(IProvisioningAgent agent) {
 		this.agent = agent;
 		
 		if (this.agent != null) {
 			this.artifactRepositoryManager = (IArtifactRepositoryManager) this.agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
 			this.metadataRepositoryManager = (IMetadataRepositoryManager) this.agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
 		}
 	}
 
 	
 	private final File unzip(String iu, InputStream repoZipStream) throws IOException {
 		File tempDir = new File(System.getProperty("java.io.tmpdir"), iu + "_" + new java.util.Random().nextInt());
 		tempDir.mkdir();
 		tempDir.deleteOnExit();
 		
 		ZipInputStream zin = new ZipInputStream(repoZipStream);
 		ZipEntry ze = null;
 		int extracted = 0;
 		
 	    while ((ze = zin.getNextEntry()) != null) {
 	    	File outFile = new File(tempDir.getCanonicalPath(), ze.getName());
 
 	    	if (ze.isDirectory()) {
 	    		if (!outFile.exists()) {
 	    			outFile.mkdir();
 	    			outFile.deleteOnExit();
 	    			extracted++;
 	    		}
 	    	} else  {
 		        FileOutputStream fout = new FileOutputStream(outFile);
 		        outFile.deleteOnExit();
 		        
 		        for (int c = zin.read(); c != -1; c = zin.read()) {
 		          fout.write(c);
 		        }		        
 		        fout.close();
 		        extracted++;
 	    	}
 	    
 	    	zin.closeEntry();
 	      }
 	    
 	      zin.close();
 
 	      if (extracted == 0) {
 	    	  throw new IOException("Empty or invalid archive.");
 	      }
 		return tempDir;
 	}
 
 	
 	/**
 	 * Make sure that an agent is set and metadata and artifact managers are available 
 	 * before using this class.
 	 */
 	private void assertAgentAndServicesAvailable() {
 		assertNotNull(this.agent, "provisioning agent");
 		assertNotNull(this.metadataRepositoryManager, "metadata repository manager");
 		assertNotNull(this.artifactRepositoryManager, "artifact repository manager");
 	}
 
 	
 	private void assertNotNull(Object o, String name) {
 		if (o == null) {
 			String message = "No " + name + " available!";
 			LOG.error(message);
 			throw new IllegalStateException(message);
 		}
 	}
 }
