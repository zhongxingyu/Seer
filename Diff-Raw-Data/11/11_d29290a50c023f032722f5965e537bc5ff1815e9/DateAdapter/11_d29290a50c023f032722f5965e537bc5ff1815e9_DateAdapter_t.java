 package com.spidey01.notessystem.terentatek;
 
 import com.google.gson.TypeAdapter;
 import com.google.gson.stream.JsonReader;
 import com.google.gson.stream.JsonWriter;
 import com.google.gson.stream.JsonToken;
 
 import java.util.Date;
 import java.io.IOException;
 
 /** Simple Gson type adapter for java.util.Date */
 public class DateAdapter extends TypeAdapter<Date>
 {
     public Date read(JsonReader reader)
         throws IOException
     {
         if (reader.peek() == JsonToken.NULL) {
             reader.nextNull();
             return null;
         }
 
        /* get the data in seconds and make a Date via miliseconds */
        return new Date(((long)reader.nextDouble())*1000);
     }
 
     public void write(JsonWriter writer, Date value)
         throws IOException
     {
         if (value == null) {
             writer.nullValue();
             return;
         }
        /* do writer.value(...) with data from the Date value as seconds */
         writer.value(value.getTime()/1000);
     }
 }
 
