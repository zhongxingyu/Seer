 /*
  * Licensed to ElasticSearch and Shay Banon under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. ElasticSearch licenses this
  * file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.elasticsearch.test.unit.index.mapper.boost;
 
 import org.apache.lucene.index.IndexableField;
 import org.elasticsearch.common.xcontent.XContentFactory;
 import org.elasticsearch.index.mapper.DocumentMapper;
 import org.elasticsearch.index.mapper.ParsedDocument;
 import org.elasticsearch.test.unit.index.mapper.MapperTests;
 import org.testng.annotations.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 /**
  */
 @Test
 public class BoostMappingTests {
 
     @Test
     public void testDefaultMapping() throws Exception {
         String mapping = XContentFactory.jsonBuilder().startObject().startObject("type").endObject().endObject().string();
 
         DocumentMapper mapper = MapperTests.newParser().parse(mapping);
 
         ParsedDocument doc = mapper.parse("type", "1", XContentFactory.jsonBuilder().startObject()
                 .field("_boost", 2.0f)
                 .field("field", "a")
                 .field("field", "b")
                 .endObject().bytes());
 
        assertThat(doc.rootDoc().getFields().size(), equalTo(2));
        float sum = 0.0f;
        for (IndexableField field : doc.rootDoc().getFields()) {
            sum += field.boost();
        }
        assertThat(3.0f, equalTo(sum)); // 2.0 (for first field) + 1.0 (for second field)
     }
 
     @Test
     public void testCustomName() throws Exception {
         String mapping = XContentFactory.jsonBuilder().startObject().startObject("type")
                 .startObject("_boost").field("name", "custom_boost").endObject()
                 .endObject().endObject().string();
 
         DocumentMapper mapper = MapperTests.newParser().parse(mapping);
 
         ParsedDocument doc = mapper.parse("type", "1", XContentFactory.jsonBuilder().startObject()
                 .field("field", "a")
                 .field("_boost", 2.0f)

                 .endObject().bytes());
        assertThat(doc.rootDoc().getFields().size(), equalTo(1));
        for (IndexableField field : doc.rootDoc().getFields()) {
            assertThat(field.boost(), equalTo(1.0f));
        }
 
         doc = mapper.parse("type", "1", XContentFactory.jsonBuilder().startObject()
                 .field("field", "a")
                 .field("custom_boost", 2.0f)
                 .endObject().bytes());
        assertThat(doc.rootDoc().getFields().size(), equalTo(1));
        for (IndexableField field : doc.rootDoc().getFields()) {
            assertThat(field.boost(), equalTo(2.0f));
        }
     }
 }
