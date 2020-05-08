 /**
  * Copyright 2011 Jason Ferguson.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jason.mapmaker.shared.model;
 
 import com.google.gwt.user.client.rpc.CustomFieldSerializer;
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.client.rpc.SerializationStreamReader;
 import com.google.gwt.user.client.rpc.SerializationStreamWriter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * GWT CustomFieldSerializer class for serializing/deserializing ShapefileMetadata objects. The idea is to
  * get around the Hibernate PersistentBag issue w/o complicated external libraries (Gilead/Dozer/etc)
  *
 * @since 0.4.3
  * @author Jason Ferguson
  */
 @SuppressWarnings("unused")
 public class ShapefileMetadata_CustomFieldSerializer extends CustomFieldSerializer<ShapefileMetadata> {
 
     private int id;
     private String geoId;
     private String mtfccCode;
     private String url;
     private String version;
     private String currentStatus;
 
     private List<Location> locationList;
 
 
     @Override
     public void deserializeInstance(SerializationStreamReader reader, ShapefileMetadata instance) throws SerializationException {
 
         instance.setId(reader.readInt());
         instance.setGeoId(reader.readString());
         instance.setMtfccCode(reader.readString());
         instance.setUrl(reader.readString());
         instance.setVersion(reader.readString());
         instance.setCurrentStatus(reader.readString());
 
         List locationList = (List) reader.readObject();
         if (locationList.size() == 0) {
             instance.setLocationList(new ArrayList<Location>());
         } else {
             instance.setLocationList((List) reader.readObject());
         }
     }
 
     @Override
     public void serializeInstance(SerializationStreamWriter writer, ShapefileMetadata instance) throws SerializationException {
         writer.writeInt(instance.getId());
         writer.writeString(instance.getGeoId());
         writer.writeString(instance.getMtfccCode());
         writer.writeString(instance.getUrl());
         writer.writeString(instance.getVersion());
         writer.writeString(instance.getCurrentStatus());
 
        writer.writeObject(new ArrayList<Location>(instance.getLocationList()));
     }
 
     public static void serialize(SerializationStreamWriter writer, ShapefileMetadata instance) throws SerializationException {
         writer.writeInt(instance.getId());
         writer.writeString(instance.getGeoId());
         writer.writeString(instance.getMtfccCode());
         writer.writeString(instance.getUrl());
         writer.writeString(instance.getVersion());
         writer.writeString(instance.getCurrentStatus());
        writer.writeObject(new ArrayList<Location>(instance.getLocationList()));
     }
 
     public static void deserialize(SerializationStreamReader reader, ShapefileMetadata instance) throws SerializationException {
         instance.setId(reader.readInt());
         instance.setGeoId(reader.readString());
         instance.setMtfccCode(reader.readString());
         instance.setUrl(reader.readString());
         instance.setVersion(reader.readString());
         instance.setCurrentStatus(reader.readString());
         instance.setLocationList((List) reader.readObject());
     }
 }
