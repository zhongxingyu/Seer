 package org.purl.wf4ever.rosrs.client;
 
 import java.io.Serializable;
 import java.net.URI;
 
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 /**
  * ro:FolderEntry.
  * 
  * @author piotrekhol
  * 
  */
 public class FolderEntry implements Serializable {
 
     /** id. */
     private static final long serialVersionUID = 3984592381741315385L;
 
     /** aggregating folder. */
     private Folder folder;
 
     /** name in the folder. */
     private String name;
 
     /** folder entry URI. */
     private URI uri;
 
     /** URI of the resource it points to. */
     private URI resourceUri;
 
     /** Resource pointed by the entry, lazily loaded. */
     private Resource resource;
 
 
     /**
      * Constructor.
      * 
      * @param folder
      *            aggregating folder
      * @param uri
      *            folder entry URI
      * @param resourceUri
      *            URI of the resource it points to
      * @param entryName
      *            name in the folder
      */
     public FolderEntry(Folder folder, URI uri, URI resourceUri, String entryName) {
         this.folder = folder;
         this.uri = uri;
         this.resourceUri = resourceUri;
         this.name = entryName;
     }
 
 
     public Folder getFolder() {
         return folder;
     }
 
 
     public String getName() {
         return name;
     }
 
 
     public URI getUri() {
         return uri;
     }
 
 
     public URI getResourceUri() {
         return resourceUri;
     }
 
 
     /**
      * Return the resource instance or null if it's not aggregated in the RO.
      * 
      * @return a resource instance or null
      */
     public Resource getResource() {
         if (resource == null) {
             resource = folder.getResearchObject().getResource(resourceUri);
         }
         if (resource == null) {
             resource = folder.getResearchObject().getFolder(resourceUri);
         }
         return resource;
     }
 
 
     /**
      * Remove the folder entry from the folder. Does not delete the resource.
      * 
      * @throws ROSRSException
      *             unexpected response from the server
      */
     public void delete()
             throws ROSRSException {
         folder.getResearchObject().getRosrs().deleteResource(uri);
        folder.deleteFolderEntry(this);
     }
 
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((resourceUri == null) ? 0 : resourceUri.hashCode());
         result = prime * result + ((uri == null) ? 0 : uri.hashCode());
         return result;
     }
 
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         FolderEntry other = (FolderEntry) obj;
         if (resourceUri == null) {
             if (other.resourceUri != null) {
                 return false;
             }
         } else if (!resourceUri.equals(other.resourceUri)) {
             return false;
         }
         if (uri == null) {
             if (other.uri != null) {
                 return false;
             }
         } else if (!uri.equals(other.uri)) {
             return false;
         }
         return true;
     }
 
 }
