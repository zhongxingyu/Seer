 /*
  * Copyright 2002-2010 the original author or authors.
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
 package org.springframework.integration.feed.config;
 
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
 import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
 import org.springframework.util.StringUtils;
 import org.w3c.dom.Element;
 
 /**
  * Handles parsing the configuration for the feed inbound channel adapter.
  *
  * @author Josh Long
  * @author Oleg Zhurakousky
  */
 public class FeedMessageSourceBeanDefinitionParser extends AbstractPollingInboundChannelAdapterParser {
 
 	@Override
 	protected String parseSource(final Element element, final ParserContext parserContext) {
 
 		BeanDefinitionBuilder feedEntryBuilder = 
 			BeanDefinitionBuilder.genericBeanDefinition("org.springframework.integration.feed.FeedEntryReaderMessageSource");
 		IntegrationNamespaceUtils.setValueIfAttributeDefined(feedEntryBuilder, element, "id", "persistentIdentifier");
 		BeanDefinitionBuilder feedBuilder = 
 			BeanDefinitionBuilder.genericBeanDefinition("org.springframework.integration.feed.FeedReaderMessageSource");
		feedBuilder.addConstructorArgValue(element.getAttribute("feed-url"));
 		
 		String metadataStoreStrategy = element.getAttribute("metadata-store");
 		if (StringUtils.hasText(metadataStoreStrategy)){
 			feedEntryBuilder.addPropertyReference("metadataStore", metadataStoreStrategy);
 		}
 		
 		feedEntryBuilder.addConstructorArgValue(feedBuilder.getBeanDefinition());
 		
 		return BeanDefinitionReaderUtils.registerWithGeneratedName(feedEntryBuilder.getBeanDefinition(), parserContext.getRegistry());
 	}
 }
