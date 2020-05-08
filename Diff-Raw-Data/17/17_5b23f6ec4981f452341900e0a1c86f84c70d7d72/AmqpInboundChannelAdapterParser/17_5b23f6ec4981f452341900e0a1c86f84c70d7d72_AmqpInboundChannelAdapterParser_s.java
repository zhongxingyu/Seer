 /*
  * Copyright 2002-2011 the original author or authors.
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
 
 package org.springframework.integration.amqp.config;
 
 import org.w3c.dom.Element;
 
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
 import org.springframework.util.StringUtils;
 
 /**
  * Parser for the AMQP 'inbound-channel-adapter' element.
  * 
  * @author Mark Fisher
  * @since 2.1
  */
 public class AmqpInboundChannelAdapterParser extends AbstractSingleBeanDefinitionParser {
 
 	@Override
 	protected String getBeanClassName(Element element) {
 		return "org.springframework.integration.amqp.AmqpInboundChannelAdapter";
 	}
 
 	@Override
 	protected boolean shouldGenerateId() {
 		return false;
 	}
 
 	@Override
 	protected boolean shouldGenerateIdAsFallback() {
 		return true;
 	}
 
 	@Override
 	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
 		String connectionFactoryRef = element.getAttribute("connection-factory");
 		if (!StringUtils.hasText(connectionFactoryRef)) {
 			connectionFactoryRef = "connectionFactory";
 		}
 		builder.addConstructorArgReference(connectionFactoryRef);
 		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "queue-name");
 		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "channel", "outputChannel");
 	}
 
 }
