 /*
  * Licensed to Elastic Search and Shay Banon under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. Elastic Search licenses this
  * file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.elasticsearch.cluster.metadata;
 
 import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
 import org.elasticsearch.common.collect.ImmutableOpenMap;
 import org.elasticsearch.common.collect.MapBuilder;
 import org.elasticsearch.common.compress.CompressedString;
 import org.elasticsearch.common.io.stream.StreamInput;
 import org.elasticsearch.common.io.stream.StreamOutput;
 import org.elasticsearch.common.settings.ImmutableSettings;
 import org.elasticsearch.common.settings.Settings;
 import org.elasticsearch.common.settings.loader.SettingsLoader;
 import org.elasticsearch.common.xcontent.ToXContent;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.elasticsearch.common.xcontent.XContentFactory;
 import org.elasticsearch.common.xcontent.XContentParser;
 
 import java.io.IOException;
 import java.util.Map;
 
 /**
  *
  */
 public class IndexTemplateMetaData {
 
     private final String name;
 
     private final int order;
 
     private final String template;
 
     private final Settings settings;
 
     // the mapping source should always include the type as top level
     private final ImmutableOpenMap<String, CompressedString> mappings;
 
     private final ImmutableOpenMap<String, IndexMetaData.Custom> customs;
 
     public IndexTemplateMetaData(String name, int order, String template, Settings settings, ImmutableOpenMap<String, CompressedString> mappings, ImmutableOpenMap<String, IndexMetaData.Custom> customs) {
         this.name = name;
         this.order = order;
         this.template = template;
         this.settings = settings;
         this.mappings = mappings;
         this.customs = customs;
     }
 
     public String name() {
         return this.name;
     }
 
     public int order() {
         return this.order;
     }
 
     public int getOrder() {
         return order();
     }
 
     public String getName() {
         return this.name;
     }
 
     public String template() {
         return this.template;
     }
 
     public String getTemplate() {
         return this.template;
     }
 
     public Settings settings() {
         return this.settings;
     }
 
     public Settings getSettings() {
         return settings();
     }
 
     public ImmutableOpenMap<String, CompressedString> mappings() {
         return this.mappings;
     }
 
     public ImmutableOpenMap<String, CompressedString> getMappings() {
         return this.mappings;
     }
 
     public ImmutableOpenMap<String, IndexMetaData.Custom> customs() {
         return this.customs;
     }
 
     public ImmutableOpenMap<String, IndexMetaData.Custom> getCustoms() {
         return this.customs;
     }
 
     public <T extends IndexMetaData.Custom> T custom(String type) {
         return (T) customs.get(type);
     }
 
     public static Builder builder(String name) {
         return new Builder(name);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         IndexTemplateMetaData that = (IndexTemplateMetaData) o;
 
         if (order != that.order) return false;
         if (!mappings.equals(that.mappings)) return false;
         if (!name.equals(that.name)) return false;
         if (!settings.equals(that.settings)) return false;
         if (!template.equals(that.template)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name.hashCode();
         result = 31 * result + order;
         result = 31 * result + template.hashCode();
         result = 31 * result + settings.hashCode();
         result = 31 * result + mappings.hashCode();
         return result;
     }
 
     public static class Builder {
 
         private String name;
 
         private int order;
 
         private String template;
 
         private Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;
 
         private final ImmutableOpenMap.Builder<String, CompressedString> mappings;
 
         private final ImmutableOpenMap.Builder<String, IndexMetaData.Custom> customs;
 
         public Builder(String name) {
             this.name = name;
             mappings = ImmutableOpenMap.builder();
             customs = ImmutableOpenMap.builder();
         }
 
         public Builder(IndexTemplateMetaData indexTemplateMetaData) {
             this.name = indexTemplateMetaData.name();
             order(indexTemplateMetaData.order());
             template(indexTemplateMetaData.template());
             settings(indexTemplateMetaData.settings());
 
             mappings = ImmutableOpenMap.builder(indexTemplateMetaData.mappings());
             customs = ImmutableOpenMap.builder(indexTemplateMetaData.customs());
         }
 
         public Builder order(int order) {
             this.order = order;
             return this;
         }
 
         public Builder template(String template) {
             this.template = template;
             return this;
         }
 
         public String template() {
             return template;
         }
 
         public Builder settings(Settings.Builder settings) {
             this.settings = settings.build();
             return this;
         }
 
         public Builder settings(Settings settings) {
             this.settings = settings;
             return this;
         }
 
         public Builder removeMapping(String mappingType) {
             mappings.remove(mappingType);
             return this;
         }
 
         public Builder putMapping(String mappingType, CompressedString mappingSource) throws IOException {
             mappings.put(mappingType, mappingSource);
             return this;
         }
 
         public Builder putMapping(String mappingType, String mappingSource) throws IOException {
             mappings.put(mappingType, new CompressedString(mappingSource));
             return this;
         }
 
         public Builder putCustom(String type, IndexMetaData.Custom customIndexMetaData) {
             this.customs.put(type, customIndexMetaData);
             return this;
         }
 
         public Builder removeCustom(String type) {
             this.customs.remove(type);
             return this;
         }
 
         public IndexMetaData.Custom getCustom(String type) {
             return this.customs.get(type);
         }
 
         public IndexTemplateMetaData build() {
             return new IndexTemplateMetaData(name, order, template, settings, mappings.build(), customs.build());
         }
 
         public static void toXContent(IndexTemplateMetaData indexTemplateMetaData, XContentBuilder builder, ToXContent.Params params) throws IOException {
             builder.startObject(indexTemplateMetaData.name(), XContentBuilder.FieldCaseConversion.NONE);
 
             builder.field("order", indexTemplateMetaData.order());
             builder.field("template", indexTemplateMetaData.template());
 
             builder.startObject("settings");
             for (Map.Entry<String, String> entry : indexTemplateMetaData.settings().getAsMap().entrySet()) {
                 builder.field(entry.getKey(), entry.getValue());
             }
             builder.endObject();
 
             if (params.paramAsBoolean("reduce_mappings", false)) {
                 builder.startObject("mappings");
                 for (ObjectObjectCursor<String, CompressedString> cursor : indexTemplateMetaData.mappings()) {
                     byte[] mappingSource = cursor.value.uncompressed();
                     XContentParser parser = XContentFactory.xContent(mappingSource).createParser(mappingSource);
                     Map<String, Object> mapping = parser.map();
                     if (mapping.size() == 1 && mapping.containsKey(cursor.key)) {
                         // the type name is the root value, reduce it
                         mapping = (Map<String, Object>) mapping.get(cursor.key);
                     }
                     builder.field(cursor.key);
                     builder.map(mapping);
                 }
                 builder.endObject();
             } else {
                 builder.startArray("mappings");
                 for (ObjectObjectCursor<String, CompressedString> cursor : indexTemplateMetaData.mappings()) {
                     byte[] data = cursor.value.uncompressed();
                     XContentParser parser = XContentFactory.xContent(data).createParser(data);
                     Map<String, Object> mapping = parser.mapOrderedAndClose();
                     builder.map(mapping);
                 }
                 builder.endArray();
             }
 
             for (ObjectObjectCursor<String, IndexMetaData.Custom> cursor : indexTemplateMetaData.customs()) {
                 builder.startObject(cursor.key, XContentBuilder.FieldCaseConversion.NONE);
                 IndexMetaData.lookupFactorySafe(cursor.key).toXContent(cursor.value, builder, params);
                 builder.endObject();
             }
 
             builder.endObject();
         }
 
         public static IndexTemplateMetaData fromXContentStandalone(XContentParser parser) throws IOException {
             XContentParser.Token token = parser.nextToken();
             if (token == null) {
                 throw new IOException("no data");
             }
             if (token != XContentParser.Token.START_OBJECT) {
                 throw new IOException("should start object");
             }
             token = parser.nextToken();
             if (token != XContentParser.Token.FIELD_NAME) {
                 throw new IOException("the first field should be the template name");
             }
             return fromXContent(parser);
         }
 
         public static IndexTemplateMetaData fromXContent(XContentParser parser) throws IOException {
             Builder builder = new Builder(parser.currentName());
 
             String currentFieldName = null;
             XContentParser.Token token = parser.nextToken();
             while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                 if (token == XContentParser.Token.FIELD_NAME) {
                     currentFieldName = parser.currentName();
                 } else if (token == XContentParser.Token.START_OBJECT) {
                     if ("settings".equals(currentFieldName)) {
                        ImmutableSettings.Builder templateSettingsBuilder = ImmutableSettings.settingsBuilder();
                        for (Map.Entry<String, String> entry : SettingsLoader.Helper.loadNestedFromMap(parser.mapOrdered()).entrySet()) {
                            if (!entry.getKey().startsWith("index.")) {
                                templateSettingsBuilder.put("index." + entry.getKey(), entry.getValue());
                            } else {
                                templateSettingsBuilder.put(entry.getKey(), entry.getValue());
                            }
                        }
                        builder.settings(templateSettingsBuilder.build());
                     } else if ("mappings".equals(currentFieldName)) {
                         while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                             if (token == XContentParser.Token.FIELD_NAME) {
                                 currentFieldName = parser.currentName();
                             } else if (token == XContentParser.Token.START_OBJECT) {
                                 String mappingType = currentFieldName;
                                 Map<String, Object> mappingSource = MapBuilder.<String, Object>newMapBuilder().put(mappingType, parser.mapOrdered()).map();
                                 builder.putMapping(mappingType, XContentFactory.jsonBuilder().map(mappingSource).string());
                             }
                         }
                     } else {
                         // check if its a custom index metadata
                         IndexMetaData.Custom.Factory<IndexMetaData.Custom> factory = IndexMetaData.lookupFactory(currentFieldName);
                         if (factory == null) {
                             //TODO warn
                             parser.skipChildren();
                         } else {
                             builder.putCustom(factory.type(), factory.fromXContent(parser));
                         }
                     }
                 } else if (token == XContentParser.Token.START_ARRAY) {
                     if ("mappings".equals(currentFieldName)) {
                         while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                             Map<String, Object> mapping = parser.mapOrdered();
                             if (mapping.size() == 1) {
                                 String mappingType = mapping.keySet().iterator().next();
                                 String mappingSource = XContentFactory.jsonBuilder().map(mapping).string();
 
                                 if (mappingSource == null) {
                                     // crap, no mapping source, warn?
                                 } else {
                                     builder.putMapping(mappingType, mappingSource);
                                 }
                             }
                         }
                     }
                 } else if (token.isValue()) {
                     if ("template".equals(currentFieldName)) {
                         builder.template(parser.text());
                     } else if ("order".equals(currentFieldName)) {
                         builder.order(parser.intValue());
                     }
                 }
             }
             return builder.build();
         }
 
         public static IndexTemplateMetaData readFrom(StreamInput in) throws IOException {
             Builder builder = new Builder(in.readString());
             builder.order(in.readInt());
             builder.template(in.readString());
             builder.settings(ImmutableSettings.readSettingsFromStream(in));
             int mappingsSize = in.readVInt();
             for (int i = 0; i < mappingsSize; i++) {
                 builder.putMapping(in.readString(), CompressedString.readCompressedString(in));
             }
             int customSize = in.readVInt();
             for (int i = 0; i < customSize; i++) {
                 String type = in.readString();
                 IndexMetaData.Custom customIndexMetaData = IndexMetaData.lookupFactorySafe(type).readFrom(in);
                 builder.putCustom(type, customIndexMetaData);
             }
             return builder.build();
         }
 
         public static void writeTo(IndexTemplateMetaData indexTemplateMetaData, StreamOutput out) throws IOException {
             out.writeString(indexTemplateMetaData.name());
             out.writeInt(indexTemplateMetaData.order());
             out.writeString(indexTemplateMetaData.template());
             ImmutableSettings.writeSettingsToStream(indexTemplateMetaData.settings(), out);
             out.writeVInt(indexTemplateMetaData.mappings().size());
             for (ObjectObjectCursor<String, CompressedString> cursor : indexTemplateMetaData.mappings()) {
                 out.writeString(cursor.key);
                 cursor.value.writeTo(out);
             }
             out.writeVInt(indexTemplateMetaData.customs().size());
             for (ObjectObjectCursor<String, IndexMetaData.Custom> cursor : indexTemplateMetaData.customs()) {
                 out.writeString(cursor.key);
                 IndexMetaData.lookupFactorySafe(cursor.key).writeTo(cursor.value, out);
             }
         }
     }
 
 }
