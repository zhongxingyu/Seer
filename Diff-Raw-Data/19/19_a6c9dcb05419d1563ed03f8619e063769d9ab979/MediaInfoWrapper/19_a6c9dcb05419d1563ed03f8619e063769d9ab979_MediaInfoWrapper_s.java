 package org.debox.mediainfo;
 
 /*
  * #%L
  * debox-photos
  * %%
  * Copyright (C) 2012 - 2013 Debox
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 import org.apache.commons.io.IOUtils;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class MediaInfoWrapper {
     
     private static final Logger log = LoggerFactory.getLogger(MediaInfoWrapper.class);
     
     protected Path path;
     
     public MediaInfoWrapper(Path path) {
         if (path == null) {
             throw new NullPointerException("Path cannot be null.");
         } else if (!path.toFile().exists()) {
             throw new IllegalArgumentException("Not any file exists at path: " + path.toString());
         } else if (!path.toFile().canRead()) {
             throw new IllegalArgumentException("File is not readable: " + path.toString());
         }
         this.path = path;
     }
     
     public MediaInfoWrapper(File file) {
         this(file.getAbsolutePath());
     }
     
     public MediaInfoWrapper(String path) {
         this(Paths.get(path));
     }
     
     public VideoMetadata getMetadata() {
         Process process = null;
         try {
             process = Runtime.getRuntime().exec(new String[]{"mediainfo", "--Output=XML", path.toString()});
             process.waitFor();
         } catch (IOException | InterruptedException ex) {
            log.error("Error during mediainfo process", ex);
         }
         if (process == null) {
             return null;
         }
         
         VideoMetadata result = null;
         InputStream is = null;
         try {
             is = process.getInputStream();
             result = convert(is);
         } catch (JDOMException | IOException ex) {
             log.error("An error occured during stream reading", ex);
         } finally {
             IOUtils.closeQuietly(is);
         }
         return result;
     }
     
     protected VideoMetadata convert(InputStream inputStream) throws IOException, JDOMException {
         VideoMetadata metadata = new VideoMetadata();
         metadata.setFilename(path.toString());
         
         SAXBuilder builder = new SAXBuilder();
         Document document = builder.build(inputStream);
         
         Element root = document.getRootElement();
         Element file = root.getChild("File");
         List<Element> tracks = file.getChildren("track");
         
         for (Element track : tracks) {
             switch (track.getAttributeValue("type")) {
                 case "General":
                     metadata.setFormat(track.getChildText("Format"));
                     metadata.setFormatProfile(track.getChildText("Format_profile"));
                     metadata.setCodecId(track.getChildText("Codec_ID"));
                     metadata.setFileSize(track.getChildText("File_size"));
                     metadata.setDuration(track.getChildText("Duration"));
                     metadata.setEncodedDate(track.getChildText("Encoded_date"));
                     break;
                 case "Video":
                     metadata.setWidth(track.getChildText("Width"));
                     metadata.setHeight(track.getChildText("Height"));
                     metadata.setAspectRatio(track.getChildText("Display_aspect_ratio"));
                     break;
                 case "Audio":
                     metadata.setBitrate(track.getChildText("Bit_rate"));
                     metadata.setFrequency(track.getChildText("Sampling_rate"));
                     break;
             }
         }
         
         return metadata;
     }
     
 }
