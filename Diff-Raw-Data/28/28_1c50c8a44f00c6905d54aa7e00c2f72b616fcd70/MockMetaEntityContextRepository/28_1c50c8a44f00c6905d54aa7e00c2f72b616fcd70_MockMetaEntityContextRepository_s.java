 /**
  *        Licensed to the Apache Software Foundation (ASF) under one
  *        or more contributor license agreements.  See the NOTICE file
  *        distributed with this work for additional information
  *        regarding copyright ownership.  The ASF licenses this file
  *        to you under the Apache License, Version 2.0 (the
  *        "License"); you may not use this file except in compliance
  *        with the License.  You may obtain a copy of the License at
  *
  *          http://www.apache.org/licenses/LICENSE-2.0
  *
  *        Unless required by applicable law or agreed to in writing,
  *        software distributed under the License is distributed on an
  *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *        KIND, either express or implied.  See the License for the
  *        specific language governing permissions and limitations
  *        under the License.
  *
  */
 package org.intelligentsia.dowsers.entity;
 
 import java.util.Map;
 
 import org.intelligentsia.dowsers.entity.meta.MetaAttributeDefinition;
 import org.intelligentsia.dowsers.entity.meta.MetaEntityContext;
 import org.intelligentsia.dowsers.entity.meta.MetaEntityContextBuilder;
 import org.intelligentsia.dowsers.entity.meta.MetaEntityContextRepository;
 import org.intelligentsia.keystone.api.artifacts.Version;
 
 import com.google.common.collect.Maps;
 
 /**
  * MockMetaEntityContextRepository.
  * 
  * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
  */
 public class MockMetaEntityContextRepository implements MetaEntityContextRepository {
 
 	Map<String, MetaEntityContext> metaEntityContexts;
 
 	public MockMetaEntityContextRepository() {
 		super();
 		metaEntityContexts = Maps.newHashMap();
		metaEntityContexts.put(SampleEntity.class.getName(),
				new MetaEntityContextBuilder(SampleEntity.class.getName(), new Version(1)).add(new MetaAttributeDefinition("name", String.class, null)).add(new MetaAttributeDefinition("description", String.class, null)).build());
 		metaEntityContexts.put(
 				CustomizableSampleEntity.class.getName(),
				new MetaEntityContextBuilder(CustomizableSampleEntity.class.getName(), new Version(1)).add(new MetaAttributeDefinition("name", String.class, null)).add(new MetaAttributeDefinition("description", String.class, null))
						.add(new MetaAttributeDefinition("order", Long.class, null)).build());
		metaEntityContexts.put(
				SampleEntityMetaAware.class.getName(),
				new MetaEntityContextBuilder(SampleEntityMetaAware.class.getName(), new Version(1)).add(new MetaAttributeDefinition("name", String.class, null)).add(new MetaAttributeDefinition("description", String.class, null))
						.add(new MetaAttributeDefinition("order", Long.class, null)).build());
 	}
 
 	@Override
 	public MetaEntityContext find(final Class<?> className) {
 		return metaEntityContexts.get(className.getName());
 	}
 
 }
