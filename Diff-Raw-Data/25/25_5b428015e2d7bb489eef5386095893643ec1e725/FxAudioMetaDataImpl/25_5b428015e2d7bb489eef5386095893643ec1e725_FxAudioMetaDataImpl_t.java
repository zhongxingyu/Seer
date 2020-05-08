 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 package com.flexive.extractor.audio;
 
 import com.flexive.shared.media.FxMediaType;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import java.util.List;
import org.apache.commons.lang.time.DurationFormatUtils;
 
 import static com.flexive.shared.FxXMLUtils.writeSimpleTag;
 
 /**
  * The FxMedaData implementation for audio files
  *
  * @author Christopher Blasnik, cblasnik@flexive.com, unique computing solutions gmbh
  */
 class FxAudioMetaDataImpl extends FxAudioMetadata {
 
     private String mimeType;
     private String fileName;
     private List<FxMetadataItem> metadata;
     // the length in micro seconds
     private long length;
 
     /**
      * Ctor
      *
      * @param mimeType the mime type
      * @param fileName the file name
      * @param metadata the List of FxMetadataItems
      * @param length the length of the audio file in microseconds
      */
     public FxAudioMetaDataImpl(String mimeType, String fileName, List<FxMetadataItem> metadata, long length) {
         this.mimeType = mimeType;
         this.fileName = fileName;
         this.metadata = metadata;
         this.length = length;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxMediaType getMediaType() {
         return FxMediaType.Audio;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getMimeType() {
         return mimeType;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getFilename() {
         return fileName;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxMetadataItem> getMetadata() {
         return metadata;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getLengthAsTimeString() {
        return DurationFormatUtils.formatDurationHMS(length / 1000L);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long getLength() {
         return length;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void writeXMLTags(XMLStreamWriter writer) throws XMLStreamException {
         // remaining audio data (sample rate, etc): FxMetadataItems
         writer.writeStartElement("audioData");
         writeSimpleTag(writer, "length", getLengthAsTimeString(), false);
         writeSimpleTag(writer, "durationmicros", getLength(), false);
         writer.writeEndElement();
     }
 }
