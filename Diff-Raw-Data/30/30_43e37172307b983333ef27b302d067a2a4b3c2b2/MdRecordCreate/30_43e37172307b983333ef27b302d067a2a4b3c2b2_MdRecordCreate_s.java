 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.business.fedora.resources.create;
 
 import de.escidoc.core.common.business.fedora.resources.RepositoryIndicator;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.ItemFoXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProviderConstants;
 import org.escidoc.core.utils.io.MimeTypes;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * MetadataRecord for create. This his a helper construct until all values can be handled within the standard MdRecord
  * class.
  * <p/>
  * This class represent a metadata record.
  *
  * @author Steffen Wagner
  */
 public class MdRecordCreate implements Serializable {
 
     private static final long serialVersionUID = 1123744337465781247L;
 
     private final RepositoryIndicator ri = new RepositoryIndicator();
 
     private String content;
 
     private String nameSpace;
 
     private String mdRecordName;
 
     /*
      * Required as alternate ID.
      */
     private String type;
 
     private String schema;
 
     private String mimeType = MimeTypes.TEXT_XML;
 
     /*
      * Storage (Fedora) specific values
      */
     private String label;
 
     private String checksum;
 
     private boolean checksumEnabled;
 
     private String controlGroup; // Stream.CONTROL_GROUP_MANAGED;
 
     private String datastreamLocation;
 
     /**
      * Set Name of Metadata Record.
      *
      * @param name Name of MdRecord.
      * @throws InvalidContentException Thrown if name is an empty String.
      */
     public void setName(final String name) throws InvalidContentException {
 
         if (name == null || name.length() == 0) {
             throw new InvalidContentException("Empty name of meta data record");
         }
 
         this.mdRecordName = name;
     }
 
     /**
      * Get Name of Metadata Record.
      *
      * @return name of metadata record.
      */
     public String getName() {
 
         return this.mdRecordName;
     }
 
     /**
      * Set type. Type is a String with unknown content and no influence of data structure of the infrastructure.
      *
      * @param type Type of Md Record (Type is defined by Solution and has no influence of framework.)
      */
     public void setType(final String type) {
 
         this.type = type;
     }
 
     /**
      * Get type. Type is a String with unknown content and no influence of data structure of the infrastructure.
      *
      * @return Type of MetaData.
      */
     public String getType() {
 
         return this.type;
     }
 
     /**
      * Set schema for metadata record.
      *
      * @param schema XML Schema URL
      */
     public void setSchema(final String schema) {
 
         this.schema = schema;
     }
 
     /**
      * Get schema of metadata record.
      *
      * @return XML Schema URL
      */
     public String getSchema() {
 
         return this.schema;
     }
 
     /**
      * Set Content of MetadataRecord (the record itself).
      *
      * @param content The content itself.
      */
     public void setContent(final String content) {
 
         this.content = content;
     }
 
     /**
      * Set Content of MetadataRecord (the record itself).
      *
      * @param content The content itself.
      * @throws UnsupportedEncodingException Thrown if content has unsupported character for default entcoding
      */
     public void setContent(final ByteArrayOutputStream content) throws UnsupportedEncodingException {
 
         this.content = content.toString(XmlUtility.CHARACTER_ENCODING);
     }
 
     /**
      * Get OutputStream of MdRecord.
      *
      * @return Content of MdRecord.
      */
     public String getContent() {
 
         // if (this.mdRecord == null) {
         // this.mdRecord = new ByteArrayOutputStream();
         // }
         return this.content;
     }
 
     /**
      * Set the namespace of Metadata Record.
      *
      * @param nameSpace Namespace of Md Record.
      */
     public void setNameSpace(final String nameSpace) {
 
         this.nameSpace = nameSpace;
     }
 
     /**
      * Get the namespace of Metadata Record.
      *
      * @return Namespace of Md Record.
      */
     public String getNameSpace() {
 
         return this.nameSpace;
     }
 
     /**
      * Persist Component to Repository.
      *
      * @return FoXML representation of metadata record.
      * @throws SystemException Thrown if rendering failed.
      */
     public String getFOXML() throws SystemException {
 
         final Map<String, String> templateValues = getValueMap();
         return ItemFoXmlProvider.getInstance().getMetadataFoXml(templateValues);
     }
 
     /**
      * Get a HashMap with all values of MdRecord (for FoXML renderer).
      *
      * @return HashMap with all values of MdRecord
      * @throws SystemException Thrown if character encoding failed.
      */
     public Map<String, String> getValueMap() {
 
         final Map<String, String> templateValues = new HashMap<String, String>();
 
         templateValues.put(XmlTemplateProviderConstants.MD_RECORD_TYPE, this.type);
         templateValues.put(XmlTemplateProviderConstants.MD_RECORD_SCHEMA, this.schema);
         templateValues.put(XmlTemplateProviderConstants.MD_RECORD_NAME, this.mdRecordName);
 
         // add Metadata (as BLOB)
         templateValues.put(XmlTemplateProviderConstants.MD_RECORD_CONTENT, getContent());
 
         return templateValues;
     }
 
     /**
      * Set Fedora datastream label.
      *
      * @param label Fedora datastream label
      */
     public void setLabel(final String label) {
         this.label = label;
     }
 
     /**
      * Get Fedora datastream label.
      *
      * @return datastream label
      */
     public String getLabel() {
         return this.label;
     }
 
     /**
      * Set datastream checksum.
      *
      * @param checksum Fedora datastream checksum
      */
     public void setChecksum(final String checksum) {
         this.checksum = checksum;
     }
 
     /**
      * Get datastream checksum.
      *
      * @return datastream checksum
      */
     public String getChecksum() {
         return this.checksum;
     }
 
     /**
      * Enable datastream checksum for storage (Fedora).
      *
      * @param checksumEnabled Enable checksum
      */
     public void setChecksumEnabled(final boolean checksumEnabled) {
         this.checksumEnabled = checksumEnabled;
     }
 
     /**
      * Check if datastream checksum is enabled.
      *
      * @return true if checksum is enabled, false otherwise
      */
     public boolean isChecksumEnabled() {
         return this.checksumEnabled;
     }
 
     /**
      * Set mime type of meta data. Default is text/xml.
      *
      * @param mimeType mime type
      */
     public void setMimeType(final String mimeType) {
         this.mimeType = mimeType;
     }
 
     /**
      * Get meta data mime type.
      *
      * @return mime type of meta data
      */
     public String getMimeType() {
         return this.mimeType;
     }
 
     /**
      * Set ControlGroup (Fedora storage).
      *
      * @param controlGroup Fedora ControlGroup
      */
     public void setControlGroup(final String controlGroup) {
         this.controlGroup = controlGroup;
     }
 
     /**
      * Get Fedora ControlGroup.
      *
      * @return ControlGroup
      */
     public String getControlGroup() {
         return this.controlGroup;
     }
 
     /**
      * Set location of datastream (Fedora).
      *
      * @param datastreamLocation location of datastream
      */
     public void setDatastreamLocation(final String datastreamLocation) {
         this.datastreamLocation = datastreamLocation;
     }
 
     /**
      * Get location of datastream.
      *
      * @return location of datastream
      */
     public String getDatastreamLocation() {
         return this.datastreamLocation;
     }
 
     /**
      * Merge values from new MdRecord into current object.
      *
      * @param mdrecord MdRecord which has values to merge
      * @return number of changes
      * @throws InvalidContentException Thrown if values contain invalid content
      */
     public int merge(final MdRecordCreate mdrecord) throws InvalidContentException {
 
         int changes = 0;
 
         // content
         changes += mergeContent(mdrecord);
 
         // namespace
         if (this.nameSpace == null) {
            if (mdrecord.getNameSpace() != null) {
                 setNameSpace(mdrecord.getNameSpace());
                 changes++;
             }
         }
         else if (!this.nameSpace.equals(mdrecord.getNameSpace())) {
             this.nameSpace = mdrecord.getNameSpace();
             changes++;
         }
 
         // name
         if (this.mdRecordName == null) {
             if (mdrecord.getName() != null) {
                 setName(mdrecord.getName());
                 changes++;
             }
         }
         else if (!this.mdRecordName.equals(mdrecord.getName())) {
             this.mdRecordName = mdrecord.getName();
             changes++;
         }
 
         // type
         if (this.type == null) {
             if (mdrecord.getType() != null) {
                 setType(mdrecord.getType());
                 changes++;
             }
         }
         else if (!this.type.equals(mdrecord.getType())) {
             this.type = mdrecord.getType();
             changes++;
         }
 
         // schema
         if (this.schema == null) {
             if (mdrecord.getSchema() != null) {
                 setSchema(mdrecord.getSchema());
                 changes++;
             }
         }
         else if (!this.schema.equals(mdrecord.getSchema())) {
             this.schema = mdrecord.getSchema();
             changes++;
         }
         // mime type
         if (this.mimeType == null) {
             if (mdrecord.getMimeType() != null) {
                 setMimeType(mdrecord.getMimeType());
                 changes++;
             }
         }
         else if (!this.mimeType.equals(mdrecord.getMimeType())) {
             this.mimeType = mdrecord.getMimeType();
             changes++;
         }
         // label
         if (this.label == null) {
             if (mdrecord.getLabel() != null) {
                 setLabel(mdrecord.getLabel());
                 changes++;
             }
         }
         else if (mdrecord.getLabel() != null && !this.label.equals(mdrecord.getLabel())) {
             this.label = mdrecord.getLabel();
             changes++;
         }
 
         // checksum
         if (this.checksum == null) {
             if (mdrecord.getChecksum() != null) {
                 setChecksum(mdrecord.getChecksum());
                 changes++;
             }
         }
         else if (mdrecord.getChecksum() != null && !this.checksum.equals(mdrecord.getChecksum())) {
             this.checksum = mdrecord.getChecksum();
             changes++;
         }
 
         // checksum enabled
         if (this.checksumEnabled != mdrecord.isChecksumEnabled()) {
             this.checksumEnabled = mdrecord.isChecksumEnabled();
             changes++;
         }
 
         // control Group
         if (this.controlGroup == null) {
             if (mdrecord.getControlGroup() != null) {
                 setControlGroup(mdrecord.getControlGroup());
                 changes++;
             }
         }
         else if (mdrecord.getControlGroup() != null && !this.controlGroup.equals(mdrecord.getControlGroup())) {
             this.controlGroup = mdrecord.getControlGroup();
             changes++;
         }
 
         // datastream location
         // TODO: Discuss this implementation!
         /*if (this.datastreamLocation == null) {
             if (mdrecord.getDatastreamLocation() != null) {
                 this.datastreamLocation = mdrecord.getDatastreamLocation();
                 changes++;
             }
         }
         else if (!this.datastreamLocation.equals(mdrecord.getDatastreamLocation())) {
             this.datastreamLocation = mdrecord.getDatastreamLocation();
             changes++;
         }*/
 
         if (changes > 0) {
             this.ri.setResourceChanged(true);
         }
 
         return changes;
     }
 
     /**
      * Get the repository indicator.
      *
      * @return RepositoryIndicator
      */
     public RepositoryIndicator getRepositoryIndicator() {
         return this.ri;
     }
 
     /**
      * Merge XML content.
      *
      * @param mdrecord The mdrecord which content is to merge.
      * @return number of changes.
      * @throws InvalidContentException Thrown if content is invalid.
      */
     private int mergeContent(final MdRecordCreate mdrecord) {
 
         int changes = 0;
         if (this.content == null) {
             if (mdrecord.getContent() != null) {
                 setContent(mdrecord.getContent());
                 changes++;
             }
         }
         else {
             if (!XmlUtility.isIdentical(this.content, mdrecord.getContent())) {
                 this.content = mdrecord.getContent();
                 changes++;
             }
         }
         return changes;
     }
 }
