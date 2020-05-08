 package de.tuberlin.dima.presslufthammer.data.hierarchical.json;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import de.tuberlin.dima.presslufthammer.data.SchemaNode;
 import de.tuberlin.dima.presslufthammer.data.hierarchical.RecordDecoder;
 import de.tuberlin.dima.presslufthammer.data.hierarchical.RecordIterator;
 
 class JSONRecordFileIterator implements RecordIterator {
     private Scanner scan;
     private SchemaNode schema;
 
     public JSONRecordFileIterator(SchemaNode schema, String filename) {
         this.schema = schema;
         try {
             scan = new Scanner(new File(filename));
         } catch (FileNotFoundException e) {
             scan = null;
         }
     }
 
     public RecordDecoder next() {
         if (scan == null) {
             return null;
         }
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.equals("")) {
                continue;
            }
            JSONObject job = (JSONObject) JSONValue.parse(line);
             RecordDecoder decoder = new JSONRecordDecoder(schema, job);
             return decoder;
         }
        return null;
     }
 }
