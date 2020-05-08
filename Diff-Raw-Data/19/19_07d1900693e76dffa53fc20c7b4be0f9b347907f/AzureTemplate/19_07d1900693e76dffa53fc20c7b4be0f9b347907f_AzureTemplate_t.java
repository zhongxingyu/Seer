 /* Copyright 2009-2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.opencredo.cloud.storage.azure;
 
 import org.apache.commons.io.IOUtils;
 import org.opencredo.cloud.storage.BlobDetails;
 import org.opencredo.cloud.storage.ContainerStatus;
 import org.opencredo.cloud.storage.StorageCommunicationException;
 import org.opencredo.cloud.storage.StorageOperations;
 import org.opencredo.cloud.storage.StorageResponseHandlingException;
 import org.opencredo.cloud.storage.StorageUtils;
 import org.opencredo.cloud.storage.azure.model.FileBlob;
 import org.opencredo.cloud.storage.azure.model.InputStreamBlob;
 import org.opencredo.cloud.storage.azure.model.StringBlob;
 import org.opencredo.cloud.storage.azure.rest.AzureRestCommunicationException;
 import org.opencredo.cloud.storage.azure.rest.AzureRestRequestCreationException;
 import org.opencredo.cloud.storage.azure.rest.AzureRestResponseHandlingException;
 import org.opencredo.cloud.storage.azure.rest.AzureRestService;
 import org.opencredo.cloud.storage.azure.rest.internal.DefaultAzureRestService;
 import org.opencredo.cloud.storage.azure.rest.internal.XPathContainerNamesListFactory;
 import org.opencredo.cloud.storage.azure.rest.internal.XPathContainerObjectDetailsListFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 import org.springframework.xml.xpath.Jaxp13XPathTemplate;
 import org.springframework.xml.xpath.XPathOperations;
 
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

 /**
  * This is template class for interacting with Azure cloud storage.
  * 
  * @author Tomas Lukosius (tomas.lukosius@opencredo.com)
  * 
  */
 public class AzureTemplate implements StorageOperations {
     private final static Logger LOG = LoggerFactory.getLogger(AzureTemplate.class);
 
     public static final String DEFAULT_CONTAINER_NAME = "container1";
 
     private final String defaultContainerName;
 
     private final AzureRestService restService;
 
     /**
      * Constructor with Azure credentials. Default container name is set to
      * {@link #DEFAULT_CONTAINER_NAME}.
      * 
      * @param credentials
      */
     public AzureTemplate(final AzureCredentials credentials) {
         this(credentials, DEFAULT_CONTAINER_NAME);
     }
 
     /**
      * 
      * @param credentials
      *            Azure credentials
      * @param defaultContainerName
      *            Default container name.
      */
     public AzureTemplate(final AzureCredentials credentials, String defaultContainerName) {
         super();
         Assert.hasText(defaultContainerName, "Default container name is not provided");
         this.defaultContainerName = defaultContainerName;
 
         XPathOperations xpathOperations = new Jaxp13XPathTemplate();
         restService = new DefaultAzureRestService(credentials, new XPathContainerNamesListFactory(xpathOperations),
                 new XPathContainerObjectDetailsListFactory(xpathOperations));
     }
 
     /**
      * @param containerName
      * @return
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#checkContainerStatus(java.lang.String)
      */
     public ContainerStatus checkContainerStatus(String containerName) throws StorageCommunicationException {
         try {
             return restService.checkContainerStatus(containerName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'check container status' has failed [container: '%s'].", containerName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'check container status' has failed [container: '%s'].",
                     containerName);
         }
     }
 
     /**
      * 
      * @param objectName
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#deleteObject(java.lang.String)
      */
     public void deleteObject(String objectName) throws StorageCommunicationException {
         deleteObject(defaultContainerName, objectName);
     }
 
     /**
      * @param containerName
      * @param objectName
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#deleteObject(java.lang.String,
      *      java.lang.String)
      */
     public void deleteObject(String containerName, String objectName) throws StorageCommunicationException {
         try {
             restService.deleteObject(containerName, objectName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'delete object' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'delete object' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         }
     }

    public String createdSignedUrl(String containerName, String objectName, Date expiryDate) throws StorageCommunicationException {
        throw new StorageCommunicationException("Method not currently supported for Azure");
    }

 
     /**
      * @return
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#listContainerObjectDetails()
      */
     public List<BlobDetails> listContainerObjectDetails() throws StorageCommunicationException {
         return listContainerObjectDetails(defaultContainerName);
     }
 
     /**
      * @param containerName
      * @return
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#listContainerObjectDetails(java.lang.String)
      */
     public List<BlobDetails> listContainerObjectDetails(String containerName) throws StorageCommunicationException {
         try {
             return restService.listContainerObjectDetails(containerName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'list container object details' has failed [container: '%s'].",
                     containerName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'list container object details' has failed [container: '%s'].",
                     containerName);
         }
     }
 
     /**
      * @return
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#listContainerNames()
      */
     public List<String> listContainerNames() throws StorageCommunicationException {
         try {
             return restService.listContainerNames();
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException("Azure cloud storage request 'list container names' has failed", e);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     "Response handling for Azure cloud storage request 'list container names' has failed has failed", e);
         }
     }
 
     /**
      * @param containerName
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#createContainer(java.lang.String)
      */
     public void createContainer(String containerName) throws StorageCommunicationException {
         try {
             restService.createContainer(containerName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'create container' has failed [container: '%s'].", containerName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'create container' has failed [container: '%s'].",
                     containerName);
         }
     }
 
     /**
      * @param containerName
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#deleteContainer(java.lang.String)
      */
     public void deleteContainer(String containerName) throws StorageCommunicationException {
         try {
             restService.deleteContainer(containerName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'delete container' has failed [container: '%s'].", containerName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'delete container' has failed [container: '%s'].",
                     containerName);
         }
     }
 
     /**
      * @param objectName
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#receiveAndSaveToFile(java.lang.String,
      *      File)
      */
     public void receiveAndSaveToFile(String objectName, File toFile) throws StorageCommunicationException,
             StorageResponseHandlingException {
         receiveAndSaveToFile(defaultContainerName, objectName, toFile);
     }
 
     /**
      * @param containerName
      * @param objectName
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#receiveAndSaveToFile(java.lang.String,
      *      java.lang.String, File)
      */
     public void receiveAndSaveToFile(String containerName, String objectName, File toFile)
             throws StorageCommunicationException, StorageResponseHandlingException {
         Assert.notNull(toFile, "File to save received data must be specified");
         
         LOG.debug("Receive file from from blob '{}' in container '{}' and save it to file '{}'", new Object[] {
                 containerName, objectName, toFile.getAbsolutePath() });
         
         try {
             StorageUtils.createParentDirs(toFile);
         } catch (IOException e) {
             throw new StorageResponseHandlingException(e, "Failed to create parent directories for file: %s", toFile
                     .getAbsolutePath());
         }
         
         InputStreamBlob streamBlob;
 
         try {
             streamBlob = restService.getObject(containerName, objectName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'receive and save as file' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'receive and save as file' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         }
 
         try {
             StorageUtils.writeStreamToFile(streamBlob.getData(), toFile);
         } catch (IOException e) {
             throw new StorageResponseHandlingException(e,
                     "Converting response from container '%s' blob '%s' to file IO problem", containerName, objectName);
         } finally {
             if (streamBlob != null && streamBlob.getData() != null) {
                 try {
                     streamBlob.getData().close();
                 } catch (IOException e) {
                     throw new StorageResponseHandlingException(e,
                             "Container '%s' blob '%s' response stream close IO problem", containerName, objectName);
                 }
             }
         }
     }
 
     /**
      * @param objectName
      * @return
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#receiveAsInputStream(java.lang.String)
      */
     public InputStream receiveAsInputStream(String objectName) throws StorageCommunicationException,
             StorageResponseHandlingException {
         return receiveAsInputStream(defaultContainerName, objectName);
     }
 
     /**
      * @param containerName
      * @param objectName
      * @return
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#receiveAsInputStream(java.lang.String,
      *      java.lang.String)
      */
     public InputStream receiveAsInputStream(String containerName, String objectName)
             throws StorageCommunicationException, StorageResponseHandlingException {
         try {
             return restService.getObject(containerName, objectName).getData();
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'receive as input stream' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'receive as input stream' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         }
     }
 
     /**
      * @param objectName
      * @return
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#receiveAsString(java.lang.String)
      */
     public String receiveAsString(String objectName) throws StorageCommunicationException,
             StorageResponseHandlingException {
         return receiveAsString(defaultContainerName, objectName);
     }
 
     /**
      * @param containerName
      * @param objectName
      * @return
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#receiveAsString(java.lang.String,
      *      java.lang.String)
      */
     public String receiveAsString(String containerName, String objectName) throws StorageCommunicationException,
             StorageResponseHandlingException {
         LOG.debug("Receive string from from blob '{}' in container '{}'", objectName, containerName);
         InputStreamBlob streamBlob;
         try {
             streamBlob = restService.getObject(containerName, objectName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'receive as string' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'receive as string' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         }
 
         try {
             return IOUtils.toString(streamBlob.getData());
         } catch (IOException e) {
             throw new StorageResponseHandlingException(e,
                     "Converting response from container '%s' blob '%s' to string IO problem", containerName, objectName);
         } finally {
             if (streamBlob != null && streamBlob.getData() != null) {
                 try {
                     streamBlob.getData().close();
                 } catch (IOException e) {
                     throw new StorageResponseHandlingException(e,
                             "Container '%s' blob '%s' response stream close IO problem", containerName, objectName);
                 }
             }
         }
     }
 
     /**
      * @param objectName
      * @param stringToSend
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#send(java.lang.String,
      *      java.lang.String)
      */
     public void send(String objectName, String stringToSend) throws StorageCommunicationException {
         send(defaultContainerName, objectName, stringToSend);
     }
 
     /**
      * @param containerName
      * @param objectName
      * @param stringToSend
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#send(java.lang.String,
      *      java.lang.String, java.lang.String)
      */
     public void send(String containerName, String objectName, String stringToSend) throws StorageCommunicationException {
         try {
             restService.putObject(containerName, new StringBlob(objectName, stringToSend));
         } catch (AzureRestRequestCreationException e) {
             throw new StorageCommunicationException(e,
                     "Creation of Azure cloud storage request from string has failed [container: '%s', blob: '%s']", containerName,
                     objectName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'send from string' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'send from string' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         }
     }
 
     /**
      * @param fileToSend
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#send(java.io.File)
      */
     public void send(File fileToSend) throws StorageCommunicationException {
         send(defaultContainerName, fileToSend);
     }
 
     /**
      * @param containerName
      * @param fileToSend
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#send(java.lang.String,
      *      java.io.File)
      */
     public void send(String containerName, File fileToSend) throws StorageCommunicationException {
         Assert.notNull(fileToSend, "File to send can not be null");
         send(defaultContainerName, fileToSend.getName(), fileToSend);
     }
 
     /**
      * @param containerName
      * @param objectName
      * @param fileToSend
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#send(java.lang.String,
      *      java.lang.String, java.io.File)
      */
     public void send(String containerName, String objectName, File fileToSend) throws StorageCommunicationException {
         Assert.notNull(fileToSend, "File to send can not be null");
         try {
             restService.putObject(containerName, new FileBlob(objectName, fileToSend));
         } catch (AzureRestRequestCreationException e) {
             throw new StorageCommunicationException(e,
                     "Creation of Azure cloud storage request from file has failed [container: '%s', blob: '%s', file: '%s']",
                     containerName, objectName, fileToSend.getAbsolutePath());
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'send from file' has failed [container: '%s', blob: '%s', file: '%s']",
                     containerName, objectName, fileToSend.getAbsolutePath());
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'send from file' has failed [container: '%s', blob: '%s', file: '%s']",
                     containerName, objectName, fileToSend.getAbsolutePath());
         }
     }
 
     /**
      * @param objectName
      * @param is
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#send(java.lang.String,
      *      java.io.InputStream)
      */
     public void send(String objectName, InputStream is) throws StorageCommunicationException {
         send(defaultContainerName, objectName, is);
     }
 
     /**
      * @param containerName
      * @param objectName
      * @param is
      * @throws StorageCommunicationException
      * @see org.opencredo.cloud.storage.StorageOperations#send(java.lang.String,
      *      java.lang.String, java.io.InputStream)
      */
     public void send(String containerName, String objectName, InputStream is) throws StorageCommunicationException {
         try {
             restService.putObject(containerName, new InputStreamBlob(objectName, is));
         } catch (AzureRestRequestCreationException e) {
             throw new StorageCommunicationException(e,
                     "Creation of Azure cloud storage request from input stream has failed [container: '%s', blob: '%s']", containerName,
                     objectName);
         } catch (AzureRestCommunicationException e) {
             throw new StorageCommunicationException(e,
                     "Azure cloud storage request 'send from input stream' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         } catch (AzureRestResponseHandlingException e) {
             throw new StorageCommunicationException(
                     e,
                     "Response handling for Azure cloud storage request 'send from input stream' has failed [container: '%s', blob: '%s']",
                     containerName, objectName);
         }
     }
 
     /**
      * @return the defaultContainerName
      */
     public String getDefaultContainerName() {
         return defaultContainerName;
     }
 }
