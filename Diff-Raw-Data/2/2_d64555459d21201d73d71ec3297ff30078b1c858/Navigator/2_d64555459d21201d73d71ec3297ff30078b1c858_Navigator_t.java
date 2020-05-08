 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.web.dojo.navigation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.util.StringUtils;
 
 import com.evinceframework.web.dojo.json.JsonSerializationContext;
 import com.evinceframework.web.dojo.json.conversion.AbstractTypedJsonConverter;
 
 public class Navigator {
 
 	private boolean isSiteNavigation = true;
 	
 	private String implementation;
 	
 	private List<NavigationItem> items = new ArrayList<NavigationItem>();
 	
 	public boolean isSiteNavigation() {
 		return isSiteNavigation;
 	}
 
 	public void setSiteNavigation(boolean isSiteNavigation) {
 		this.isSiteNavigation = isSiteNavigation;
 	}
 
 	public String getImplementation() {
 		return implementation;
 	}
 
 	public void setImplementation(String implementation) {
 		this.implementation = implementation;
 	}
 	
 	public List<NavigationItem> getItems() {
 		return items;
 	}
 
 	public void setItems(List<NavigationItem> items) {
 		this.items = items;
 	}
 
 	public static class JsonConverter extends AbstractTypedJsonConverter<Navigator> {
 
 		public static final String SITE_TYPE = "evf.siteNav";
 		
		public static final String CONTEXT_TYPE = "evf.contextNav";
 
 		public static final String IMPL_PROPERTY = "impl";
 
 		public static final String ITEMS_PROPERTY = "items";
 
 		public JsonConverter() {
 			super(Navigator.class);
 		}
 
 		@Override
 		protected String onDetermineIdentifier(Navigator nav) {
 			return String.valueOf(System.identityHashCode(nav));
 		}
 
 		@Override
 		protected String onDetermineType(Navigator nav) {
 			return nav.isSiteNavigation() ? SITE_TYPE : CONTEXT_TYPE;
 		}
 
 		@Override
 		protected void onWriteObjectProperties(JsonSerializationContext context, Navigator nav) throws IOException {
 			if(StringUtils.hasText(nav.getImplementation())) {
 				context.writeProperty(IMPL_PROPERTY, nav.getImplementation());
 			}
 			context.writeProperty(ITEMS_PROPERTY, nav.getItems());
 		}
 	}
 	
 }
