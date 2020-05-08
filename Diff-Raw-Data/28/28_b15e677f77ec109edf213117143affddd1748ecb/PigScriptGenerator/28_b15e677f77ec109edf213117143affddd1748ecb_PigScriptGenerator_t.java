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
     private final static String PIG_SCRIPT_TEMPLATE = "input_data = load ''{0}'' USING {1} AS ({2});\n"
                                             + "STORE input_data into ''{3}'' USING  {4};\n";
     private final static String PIG_SCRIPT_HCATLOADER_TEMPLATE = "input_data = load ''{0}'' USING {1};\n"
             + "STORE input_data into ''{2}'' USING  {3};\n";
     public final static String HCAT_LOADER = "org.apache.hcatalog.pig.HCatLoader()";
     public final static String HCAT_STORER = "org.apache.hcatalog.pig.HCatStorer()";
     public final static String PIG_LOADER = "PigStorage('" + HCatMixSetup.SEPARATOR + "')";
     public final static String PIG_STORER = "PigStorage()";
 
     private final String inputLocation;
     private final String pigOutputLocation;
     private final String pigSchema;
     private final String hcatTableName;
 
     public PigScriptGenerator(String inputLocation, String pigOutputRoot, HiveTableSchema hiveTableSchema) {
         this.inputLocation = inputLocation;
         this.hcatTableName = hiveTableSchema.getDatabaseName() + "." + hiveTableSchema.getName();
        this.pigOutputLocation = HCatMixUtils.getPigOutputLocation(pigOutputRoot, hiveTableSchema.getDatabaseName(),
                hiveTableSchema.getName());
         this.pigSchema = getPigFieldSchema(hiveTableSchema);
     }
     public String getPigLoaderHCatStorerScript() {
         return MessageFormat.format(PIG_SCRIPT_TEMPLATE, inputLocation, PIG_LOADER, pigSchema,
             hcatTableName, HCAT_STORER);
     }
 
     public String getPigLoaderPigStorerScript() {
         return MessageFormat.format(PIG_SCRIPT_TEMPLATE, inputLocation, PIG_LOADER, pigSchema,
             pigOutputLocation + "pig_load_pig_store", PIG_STORER);
     }
 
     public String getHCatLoaderHCatStorerScript() {
         return MessageFormat.format(PIG_SCRIPT_HCATLOADER_TEMPLATE, hcatTableName,
                 HCAT_LOADER, HCatMixUtils.getCopyTableName(hcatTableName), HCAT_STORER);
     }
 
     public String getHCatLoaderPigStorerScript() {
         return MessageFormat.format(PIG_SCRIPT_HCATLOADER_TEMPLATE, hcatTableName,
                 HCAT_LOADER, pigOutputLocation + "hcat_load_pig_store", PIG_STORER);
     }
 
     public static String getPigFieldSchema(HiveTableSchema hiveTableSchema) {
         List<FieldSchema> allFields = new ArrayList<FieldSchema>();
         allFields.addAll(hiveTableSchema.getColumnFieldSchemas());
         allFields.addAll(hiveTableSchema.getPartitionFieldSchemas());
 
         StringBuilder fields = new StringBuilder();
         String delim = "";
         for (FieldSchema field : allFields) {
             fields.append(delim).append(field.getName()).append(':').append(toPigType(field.getType()));
             delim = ", ";
         }
         return fields.toString();
     }
 
     public static String toPigType(String type) {
         DataType dataType = DataType.fromString(type);
         if(dataType == DataType.STRING) {
             return "chararray";
         } else {
             return type;
         }
     }
 }
