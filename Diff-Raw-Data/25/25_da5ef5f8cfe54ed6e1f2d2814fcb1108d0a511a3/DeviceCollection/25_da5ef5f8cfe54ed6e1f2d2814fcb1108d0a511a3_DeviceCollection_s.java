 package oly.netpowerctrl.datastructure;
 
 import android.util.JsonReader;
 import android.util.JsonWriter;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 /**
  * Contains DeviceInfos. Used for NFC and backup transfers
  */
 public class DeviceCollection {
     public static final int PROTOCOLVERSION = 1;
     public ArrayList<DeviceInfo> devices;
 
     public static DeviceCollection fromDevices(ArrayList<DeviceInfo> devices) {
         DeviceCollection dc = new DeviceCollection();
         dc.devices = devices;
         return dc;
     }
 
     public static DeviceCollection fromJSON(String devices_as_string) throws IOException {
         DeviceCollection dc = new DeviceCollection();
 
         // Get JsonReader from String
         byte[] bytes;
         try {
             bytes = devices_as_string.getBytes("UTF-8");
         } catch (UnsupportedEncodingException ignored) {
             return null;
         }
         ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         JsonReader reader = new JsonReader(new InputStreamReader(bais));
 
         dc.devices = new ArrayList<DeviceInfo>();
 
         reader.beginObject();
         while (reader.hasNext()) {
             String name = reader.nextName();
             if (name.equals("devices")) {
                 reader.beginArray();
                 while (reader.hasNext()) {
                     dc.devices.add(DeviceInfo.fromJSON(reader));
                 }
                 reader.endArray();
             } else {
                 reader.skipValue();
             }
         }
         reader.endObject();
         return dc;
     }
 
     public String toJSON() throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         JsonWriter writer;
         try {
             writer = new JsonWriter(new OutputStreamWriter(baos, "UTF-8"));
         } catch (UnsupportedEncodingException e) {
             return null;
         }
 
         writer.beginObject();
         writer.name("version").value(PROTOCOLVERSION);
         writer.name("devices").beginArray();
         for (DeviceInfo di : devices) {
             di.toJSON(writer);
         }
         writer.endArray();
         writer.endObject();
 
         writer.close();
 
         return baos.toString();
     }
 }
