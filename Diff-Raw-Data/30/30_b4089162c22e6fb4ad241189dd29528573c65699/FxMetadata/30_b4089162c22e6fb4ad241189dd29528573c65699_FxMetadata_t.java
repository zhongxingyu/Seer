 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.media;
 
 import com.flexive.shared.exceptions.FxApplicationException;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import java.io.StringWriter;
 import java.io.Serializable;
 import java.util.List;
 
import org.apache.commons.lang.StringUtils;

 /**
  * Generic media metadata
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev
  */
 public abstract class FxMetadata {
 
     /**
      * An item holding metadata
      */
     public static class FxMetadataItem implements Serializable {
         private static final long serialVersionUID = -4856169406370122927L;
         
         private String key;
         private String value;
 
         /**
          * Ctor
          *
          * @param key   key
          * @param value valud
          */
         public FxMetadataItem(String key, String value) {
             this.key = key;
             this.value = value;
         }
 
         /**
          * Getter for the key
          *
          * @return key
          */
         public String getKey() {
             return key;
         }
 
         /**
          * Getter for the value
          *
          * @return value
          */
         public String getValue() {
             return value;
         }
     }
 
     /**
      * Get the type of this metadata instance to allow easier upcasts
      *
      * @return type of this metadata instance
      */
     public abstract FxMediaType getMediaType();
 
     /**
      * Get the mime type
      *
      * @return mime type
      */
     public abstract String getMimeType();
 
     /**
      * Get the file name, can be <code>null</code> if unknown
      *
      * @return filename or <code>null</code>
      */
     public abstract String getFilename();
 
     /**
      * Get a list of defined metadata items
      *
      * @return list of defined metadata items
      */
     public abstract List<FxMetadataItem> getMetadata();
 
     /**
      * Get this metadata object as an FxImageMetadata instance
      *
      * @return FxImageMetadata instance
      * @throws FxApplicationException on errors
      */
     public FxImageMetadata asImageMetadata() throws FxApplicationException {
         if (this instanceof FxImageMetadata)
             return (FxImageMetadata) this;
         throw new FxApplicationException("ex.general.wrongClass", this.getClass().getCanonicalName(), FxImageMetadata.class.getCanonicalName());
     }
 
     /**
      * Get this metadata object as XML document
      *
      * @return XML document
      * @throws FxApplicationException on errors
      */
     public String toXML() throws FxApplicationException {
         StringWriter sw = new StringWriter(2000);
         try {
             XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
             writer.writeStartDocument();
             writer.writeStartElement("metadata");
             writer.writeAttribute("mediatype", getMediaType().name());
             writer.writeAttribute("mimetype", getMimeType());
             writer.writeAttribute("filename", getFilename());
             writeXMLTags(writer);
             for (FxMetadataItem mdi : getMetadata()) {
                final String value = mdi.getValue().replaceAll("[\\x00-\\x1F]", ""); //filter out control characters
                if( StringUtils.isEmpty(value))
                    continue;
                 writer.writeStartElement("meta");
                 writer.writeAttribute("key", mdi.getKey());
                writer.writeCData(value);
                 writer.writeEndElement();
             }
             writer.writeEndElement();
             writer.writeEndDocument();
             writer.flush();
             writer.close();
         } catch (XMLStreamException e) {
             throw new FxApplicationException(e, "ex.general.xml", e.getMessage());
         }
         return sw.getBuffer().toString();
     }
 
     /**
      * Write implementation specific XML tags
      *
      * @param writer XMLStreamWriter
      * @throws XMLStreamException on errors
      */
     protected abstract void writeXMLTags(XMLStreamWriter writer) throws XMLStreamException;
 
 }
