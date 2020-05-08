 /*
  * DocumentId
  * Date of creation: 2010-01-20
  * 
  * Copyright (c) CompuGROUP Software GmbH, 
  * This software is the confidential and proprietary information of
  * CompuGROUP Software GmbH. You shall not disclose such confidential
  * information and shall use it only in accordance with the terms of
  * the license agreement you entered into with CompuGROUP Software GmbH.
  */
 
 package com.cg.ihe.doc.domain;
 
 import com.cg.ihe.exception.DocumentMetadataTransformerException;
 
 /**
  * The identifier of a single document.
  * <p>This class contains all keys necessary to uniquely identify the document across all source adapters and to
  * query the document content via the corresponding source adapter.</p>
  *
  * @author Martin Jachs, AT
  */
 public class DocumentId {
 
     /**
      * UUID the document has received from the infrastructure. According to the IHE ITI document, this is not meant to be an external reference. Therefore this field is marked
      * transient.
      * <br></br>
      * From the IHE ITI Specification:
      * <p>
      * "This globally unique identifier is primarily intended for use as a document registry management identifier.
      * It is not meant to be an external reference (outside of the Document Registry) for XDS Documents (e.g., in links within other documents).
      * The uniqueId is meant for that purpose so that such links remain valid beyond the XDS Affinity Domain.
      * Values of this attribute are in one of two formats: properly formatted UUID (including urn:uuid: prefix) or symbolic name (anything that does not have the urn:uuid: prefix).
      * Provide and Register Document Set-b [ITI-41] and Register Document Set-b [ITI-42] transactions may carry UUID or symbolic formats. All other transactions shall carry only the UUID format.
      * In processing the Register Document Set-b [ITI-42] transaction, the Document Registry actor shall accept and store the offered UUID values and assign UUID values to all symbolic values."
      * </p>
      */
     private String uuid;
 
     /**
      * The unique identifier that is assigned by the  document creator.
      * <br></br>
      * From the IHE ITI Specification:
      * <p>"The globally unique identifier assigned by the document creator to this document.
      * This unique identifier may be used in the body of other XDS Documents to reference this document.
      * The length of Unique Identifier shall not exceed 128 bytes. The structure and format of this Id shall be consistent with the specification corresponding to the format attribute.
      * (e.g., for a DICOM standard document a 64 character numeric UID, for an HL7 CDA format a serialization of the CDA Document id extension and root in the form oid^extension, where
      * OID is a 64 digits max, and the ID is a 16 UTF-8 char max). If the oid is coded without the extension then the '^' character shall not be included.
      * This uniqueId is intended to respond to the following types of usage:
      * The means to reference this XDS document from within the content of another document. Neither the XDS Registry nor the Repository is aware of such references, but the
      * Document Sources and Consumers are." </p>
      * <br></br>
      * For our system this identifier is not 'unique', because one document might exist in more than one <code>domain</code>. Only the complete ID including the domain
      * identifier is 'unique'  {@see #getUniqueId()}.
      * <br></br>
      * As specified in the IHE ITI Framework this identifier consits of 2 parts: the id extension and the oid of the assigning authority  (e.g.: "1.3.6.1.4.1.21367.2005.3.7^11379", where
      * "1.3.6.1.4.1.21367.2005.3.7" is the OID of the document source).  If the document is not stored in an IHE compliant system no restrictions on the format apply.
      */
     private String id;
 
     /**
      * For IHE compliant systems:
      * Set the globally unique identifier of the repository where the document is stored.
      * <br></br>
      * From the IHE ITI Specification:
      * <p>"This unique identifier for the Document Repository may be used to identify and connect to the specific Document Repository where the
      * document is stored once its metadata has been retrieved from a Document Registry.
      * This repositoryUniqueId is intended to respond to the following types of usage:
      * The means to reference the Document Repository where this XDS document is stored. The repositoryUniqueId represents an immutable id for the Document Repository.
      * The means to ensure that a XDS Document is retrieved from the appropriate Document Repository. "</p>
      */
     private String repositoryId;
 
     /**
      * For IHE compliant systems:
      * A globally unique identifier for a community
      */
     private String homeCommunityId;
 
     /**
      * Identifies the internal adapter.
      */
     private String domain;
 
     /**
      * URI to the document contents to pass to the document source adaptor (e.g. "/Repository/IHERetrieveDocument?documentUID=1.3.9.8.2.5.17673.2006.3 .8.1246457552081.1").
      *
      * @see java.net.URI
      */
     private String uri;
 
     /**
      * Default constructor.
      */
     public DocumentId() {
         // Empty constructor
     }
 
     /**
      * @param uuid            UUID id the document has received from the infrastructure
      * @param id              the unique identifier that is assigned by the  document creator.
      * @param repositoryId    repository OID (for IHE-compatible infrastructure)
      * @param homeCommunityId home community OID (for IHE-compatible infrastructure)
      * @param domain          identifies the internal adapter
      */
     public DocumentId(String uuid, String id, String repositoryId, String homeCommunityId, String domain) {
         super();
         this.uuid = uuid;
         this.id = id;
         this.repositoryId = repositoryId;
         this.homeCommunityId = homeCommunityId;
         this.domain = domain;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public void setId(String value, String assigningAuthorityId) {
         this.id = assigningAuthorityId + '^' + value;
     }
 
     public String getRepositoryId() {
         return repositoryId;
     }
 
     public void setRepositoryId(String repositoryId) {
         if (repositoryId != null && !repositoryId.isEmpty()) {
             this.repositoryId = repositoryId;
         }
     }
 
     public String getDomain() {
         return domain;
     }
 
     public void setDomain(String domain) {
         this.domain = domain;
     }
 
     public String getUri() {
         return uri;
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     public String getHomeCommunityId() {
         return homeCommunityId;
     }
 
     public void setHomeCommunityId(String homeCommunityId) {
         if (homeCommunityId != null && !homeCommunityId.isEmpty()) {
             this.homeCommunityId = homeCommunityId;
         }
     }
 
     public String getUniqueId() {
         StringBuilder string = new StringBuilder(256);
         return string.append(id).
                 append('_').append(repositoryId).
                 append('_').append(homeCommunityId).
                 append('_').append(uuid).
                 append('_').append(domain).toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (!(o instanceof DocumentId)) {
             return false;
         }
         DocumentId that = (DocumentId) o;
         if (domain != null ? !domain.equals(that.domain) : that.domain != null) {
             return false;
         }
         if (homeCommunityId != null ? !homeCommunityId.equals(that.homeCommunityId) : that.homeCommunityId != null) {
             return false;
         }
         if (id != null ? !id.equals(that.id) : that.id != null) {
             return false;
         }
         if (repositoryId != null ? !repositoryId.equals(that.repositoryId) : that.repositoryId != null) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id != null ? id.hashCode() : 0;
         result = 31 * result + (repositoryId != null ? repositoryId.hashCode() : 0);
         result = 31 * result + (homeCommunityId != null ? homeCommunityId.hashCode() : 0);
         result = 31 * result + (domain != null ? domain.hashCode() : 0);
         return result;
     }
 
     /**
      * Returns a string representation suitable for use as a participant identifier.
      *
      * @return string representation for audit
      */
     public String toAuditString() {
         StringBuilder string = new StringBuilder();
         string.append(DocumentId.class.getSimpleName());
         string.append('{');
         string.append("id=").append(id);
         string.append(", ").append("domain=").append(domain);
         if (repositoryId != null) {
             string.append(", ").append("repositoryId=").append(repositoryId);
         }
         if (homeCommunityId != null) {
             string.append(", ").append("homeCommunityId=").append(homeCommunityId);
         }
         string.append('}');
         return string.toString();
     }
 
     /**
      * {@inheritDoc}<br>
      * <br>
      * Generated by Eclipse.
      */
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("DocumentId");
         sb.append('[');
         sb.append("id=").append(id);
         sb.append(',');
         sb.append("repositoryId=").append(repositoryId);
         sb.append(',');
         sb.append("homeCommunityId=").append(homeCommunityId);
         sb.append(',');
         sb.append("domain=").append(domain);
         sb.append(',');
         sb.append("uri=").append(uri);
         sb.append(']');
         return sb.toString();
     }
 
     public String getUuid() {
         return uuid;
     }
 
     public void setUuid(String uuid) {
         this.uuid = uuid;
     }

    public String getAssigningAuthorityId() throws DocumentMetadataTransformerException {
        if (id != null) {
            if (!id.contains("^")) {
                throw new DocumentMetadataTransformerException(this, "The coded uniqueId could not be converted. Missing the '^' delimiter character");
            } else {
                int delimiterIndex = id.indexOf('^');
                if (delimiterIndex < 1 || delimiterIndex >= id.length()) {
                    throw new DocumentMetadataTransformerException(this, "The  coded uniqueId could not be converted. Missing the extension and/or OID part");
                }
                return id.substring(0, delimiterIndex);
            }
        }
        return null;
    }

    public String getIdValue() throws DocumentMetadataTransformerException {
        if (id != null) {
            if (!id.contains("^")) {
                throw new DocumentMetadataTransformerException(this, "The coded uniqueId could not be converted. Missing the '^' delimiter character");
            }
            int delimiterIndex = id.indexOf('^');
            if (delimiterIndex < 1 || delimiterIndex >= id.length()) {
                throw new DocumentMetadataTransformerException(this, "The coded uniqueId could not be converted. Missing the extension and/or OID part");
            }
            return id.substring(delimiterIndex + 1);
        }
        return null;
    }

 }
