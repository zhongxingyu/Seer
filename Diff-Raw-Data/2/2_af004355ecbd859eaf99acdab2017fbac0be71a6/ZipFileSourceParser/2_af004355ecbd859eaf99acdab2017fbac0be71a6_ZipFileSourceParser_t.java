 /*
  * Copyright 2012 C24 Technologies.
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
 package biz.c24.io.spring.batch.config;
 
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
 import org.springframework.util.StringUtils;
 import org.w3c.dom.Element;
 
 import biz.c24.io.spring.batch.reader.source.ZipFileSource;
 
 /**
  * Parser for C24ItemReader's ZipFileSource child element
  * 
  * @author Andrew Elmore
  *
  */
 public class ZipFileSourceParser extends AbstractSingleBeanDefinitionParser {
 
     /*
      * (non-Javadoc)
      * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
      */
     @Override
     protected Class<?> getBeanClass(Element element) {
         return ZipFileSource.class;
     }
 
     /*
      * (non-Javadoc)
      * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
      */
     @Override
     protected void doParse(Element element, BeanDefinitionBuilder bean) {
     
         // Optional
         String resource = element.getAttribute("resource");
         if(StringUtils.hasText(resource)) {
             bean.addPropertyValue("resource", resource);            
         }
         
         // Optional
         String skipLines = element.getAttribute("skip-lines");
         if(StringUtils.hasText(skipLines)) {
             int numLines = Integer.parseInt(skipLines);
             bean.addPropertyValue("skipLines", numLines);
         }
         
         // Optional
         String encoding = element.getAttribute("encoding");
        if(StringUtils.hasText(encoding)) {
             bean.addPropertyValue("encoding", encoding);            
         }
         
         // Optional
         String consistentLineTerminators = element.getAttribute("consistent-line-terminators");
         if(StringUtils.hasText(consistentLineTerminators)) {
             bean.addPropertyValue("consistentLineTerminators", Boolean.valueOf(consistentLineTerminators));
         }
     }
 }
