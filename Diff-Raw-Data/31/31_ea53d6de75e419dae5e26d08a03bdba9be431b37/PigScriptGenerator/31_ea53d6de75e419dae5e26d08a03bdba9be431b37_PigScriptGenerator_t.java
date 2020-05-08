 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hcatalog.hcatmix;
 
 import org.apache.hadoop.hive.metastore.api.FieldSchema;
 import org.apache.hcatalog.hcatmix.conf.HiveTableSchema;
 import org.apache.pig.test.utils.DataType;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: malakar
  */
 public class PigScriptGenerator {
    public final static String LOAD_FORMAT = "input_data = load ''{0}'' USING PigStorage(''{1}'') AS ({2});\n"
                                            + "STORE input_data into ''{3}'' USING  org.apache.hcatalog.pig.HCatStorer();\n";
 
     public static String getPigLoadScript(String inputLocation, HiveTableSchema hiveTableSchema) {
         /*
         in = load '/user/malakar/hcatmix_uniform_bug/page_views_20000000_0/part-00000' USING PigStorage(',') AS (user:chararray, timespent:int, query_term:chararray, ip_addr:int, estimated_revenue:int, page_info:chararray, action:int);
 
         STORE in into 'page_views_20000000_0' USING org.apache.hcatalog.pig.HCatStorer();
         */
 
         List<FieldSchema> allFields = new ArrayList<FieldSchema>();
         allFields.addAll(hiveTableSchema.getColumnFieldSchemas());
         allFields.addAll(hiveTableSchema.getPartitionFieldSchemas());
 
         StringBuilder fields = new StringBuilder();
         String delim = "";
         for (FieldSchema field : allFields) {
             fields.append(delim).append(field.getName()).append(':').append(toPigType(field.getType()));
             delim = ", ";
         }
         return MessageFormat.format(LOAD_FORMAT, inputLocation, HiveTableCreator.SEPARATOR, fields, hiveTableSchema.getName());
     }
 
     public static void generatePigStoreScript(HiveTableSchema hiveTableSchema) {}
 
     public static String toPigType(String type) {
         DataType dataType = DataType.fromString(type);
         if(dataType == DataType.STRING) {
             return "chararray";
         } else {
             return type;
         }
     }
 }
