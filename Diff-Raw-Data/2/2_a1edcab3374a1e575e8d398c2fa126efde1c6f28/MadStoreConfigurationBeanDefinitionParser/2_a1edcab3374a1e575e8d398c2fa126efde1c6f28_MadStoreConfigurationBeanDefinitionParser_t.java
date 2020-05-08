 /**
  * Copyright 2008 - 2009 Pro-Netics S.P.A.
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package it.pronetics.madstore.common.configuration.spring;
 
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean.AtomPublishingProtocolConfiguration;
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean.CrawlerConfiguration;
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean.GridConfiguration;
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean.IndexConfiguration;
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean.JcrConfiguration;
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean.OpenSearchConfiguration;
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean.SimpleTriggerConfiguration;
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean.IndexConfiguration.Property;
 import it.pronetics.madstore.common.configuration.support.MadStoreConfigurationException;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.support.ManagedList;
 import org.springframework.beans.factory.support.ManagedMap;
 import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.util.xml.DomUtils;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Element;
 
 /**
  * Spring-based configuration parser for creating the {@link it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean}.
  *
  * @author Salvatore Incandela
  */
 public class MadStoreConfigurationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
 
     private static final String TASK_NAME = "name";
     private static final String MADSTORE_HOME_BEAN_PROPERTY = "madStoreHome";
     private static final String CRAWLER = "crawler";
     private static final String REPOSITORY = "repository";
     private static final String SERVER = "server";
     private static final String TARGET_SITE = "targetSite";
     private static final String HOST_NAME = "hostName";
     private static final String START_LINK = "startLink";
     private static final String MAX_CONCURRENT_DOWNLOADS = "maxConcurrentDownloads";
     private static final String MAX_VISITED_LINKS = "maxVisitedLinks";
     private static final String START_DELAY = "startDelay";
     private static final String REPEAT_INTERVAL = "repeatInterval";
     private static final String WORKSPACE = "workspace";
     private static final String SHORT_NAME = "shortName";
     private static final String DESCRIPTION = "description";
     private static final String INDEXED_PROPERTIES_NAMESPACES = "indexedPropertiesNamespaces";
     private static final String NAMESPACE = "namespace";
     private static final String NAMESPACE_PREFIX = "prefix";
     private static final String NAMESPACE_URL = "url";
     private static final String INDEXED_PROPERTIES = "indexedProperties";
     private static final String PROPERTY = "property";
     private static final String PROPERTY_NAME = TASK_NAME;
     private static final String BOOST = "boost";
     private static final String XPATH = "xpath";
     private static final String DEFAULT_INDEX_FOLDER = "index";
     private static final String DEFAULT_JCR_FOLDER = "jcr";
     private static final String CRAWLER_CONFIGURATIONS_BEAN_PROPERTY = "crawlerConfigurations";
     private static final String SIMPLE_TRIGGER_CONFIGURATION_TAG = "simpleTrigger";
     private static final String TASKS_CONFIGURATION_BEAN_PROPERTY = "tasks";
     private static final String GRID_ENABLED_TAG = "grid-enabled";
     private static final String GRID_CONFIGURATION_BEAN_PROPERTY = "gridConfiguration";
     private static final String GRID_FOLDER = "gridgain";
     private static final String JCR_CONFIGURATION_BEAN_PROPERTY = "jcrConfiguration";
     private static final String INDEX_CONFIGURATION_TAG = "index";
     private static final String INDEX_CONFIGURATION_BEAN_PROPERTY = "indexConfiguration";
     private static final String HTTP_CACHE_ENABLED_BEAN_PROPERTY = "httpCacheEnabled";
     private static final String MAX_AGE_ATTRIBUTE = "max-age";
    private static final String HTTPCACHE_ENABLED_TAG = "httpCache-enabled";
     private static final String OS_CONFIGURATION_TAG = "openSearch";
     private static final String OS_CONFIGURATION_BEAN_PROPERTY = "openSearchConfiguration";
     private static final String APP_CONFIGURATION_TAG = "atomPub";
     private static final String APP_CONFIGURATION_BEAN_PROPERTY = "atomPublishingProtocolConfiguration";
     private static final String JCR_MAX_HISTORY = "maxHistory";
     private static final String TASKS_TAG = "tasks";
     private static final String TASK_TAG = "task";
     private static final String CRAWLER_TASK = "crawlerTask";
 
     @SuppressWarnings("unchecked")
     protected Class getBeanClass(Element element) {
         return MadStoreConfigurationBean.class;
     }
 
     protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
         String madStoreDir = MadStoreConfigurationManager.getInstance().getMadStoreHome();
         beanDefinitionBuilder.addPropertyValue(MADSTORE_HOME_BEAN_PROPERTY, madStoreDir);
 
         Element crawlerElement = DomUtils.getChildElementByTagName(element, CRAWLER);
         Element repositoryElement = DomUtils.getChildElementByTagName(element, REPOSITORY);
         Element serverElement = DomUtils.getChildElementByTagName(element, SERVER);
         Element tasksElement = DomUtils.getChildElementByTagName(element, TASKS_TAG);
         if (repositoryElement == null) {
             throw new IllegalStateException("At least one " + REPOSITORY + " element should be present.");
         }
         if (crawlerElement != null) {
             parseCrawlerConfigurations(crawlerElement, beanDefinitionBuilder);
             parseGridConfiguration(crawlerElement, beanDefinitionBuilder);
         } else {
             dummyCrawlerConfigurations(beanDefinitionBuilder);
         }
         parseJcrConfiguration(repositoryElement, beanDefinitionBuilder);
         parseIndexConfiguration(repositoryElement, beanDefinitionBuilder);
         if (serverElement != null) {
             Integer maxAge = new Integer(DomUtils.getChildElementByTagName(serverElement, HTTPCACHE_ENABLED_TAG).getAttribute(MAX_AGE_ATTRIBUTE));
             beanDefinitionBuilder.addPropertyValue(HTTP_CACHE_ENABLED_BEAN_PROPERTY, maxAge);
             parseAppConfiguration(serverElement, beanDefinitionBuilder);
             parseOsConfiguration(serverElement, beanDefinitionBuilder);
         } else {
             dummyAppConfiguration(beanDefinitionBuilder);
             dummyOsConfiguration(beanDefinitionBuilder);
         }
         parseTasksConfiguration(tasksElement, beanDefinitionBuilder);
     }
 
     @Override
     protected boolean shouldGenerateIdAsFallback() {
         return true;
     }
 
     @SuppressWarnings("unchecked")
     private void dummyCrawlerConfigurations(BeanDefinitionBuilder factory) {
         List<CrawlerConfiguration> crawlerConfigurations = new ManagedList();
         factory.addPropertyValue(CRAWLER_CONFIGURATIONS_BEAN_PROPERTY, crawlerConfigurations);
     }
 
     @SuppressWarnings("unchecked")
     private void parseCrawlerConfigurations(Element element, BeanDefinitionBuilder factory) {
         List<Element> targetSites = DomUtils.getChildElementsByTagName(element, TARGET_SITE);
         List<CrawlerConfiguration> crawlerConfigurations = new ManagedList(targetSites.size());
         for (Element targetSiteEle : targetSites) {
             String hostName = DomUtils.getChildElementByTagName(targetSiteEle, HOST_NAME).getTextContent();
             String startLink = DomUtils.getChildElementByTagName(targetSiteEle, START_LINK).getTextContent();
             String maxConcurrentDownloads = DomUtils.getChildElementByTagName(targetSiteEle, MAX_CONCURRENT_DOWNLOADS).getTextContent();
             String maxVisitedLinks = DomUtils.getChildElementByTagName(targetSiteEle, MAX_VISITED_LINKS).getTextContent();
             CrawlerConfiguration crawlerConfiguration = new CrawlerConfiguration();
             crawlerConfiguration.setHostName(hostName);
             crawlerConfiguration.setStartLink(startLink);
             crawlerConfiguration.setMaxConcurrentDownloads(new Integer(maxConcurrentDownloads).intValue());
             crawlerConfiguration.setMaxVisitedLinks(new Integer(maxVisitedLinks).intValue());
             crawlerConfigurations.add(crawlerConfiguration);
         }
         factory.addPropertyValue(CRAWLER_CONFIGURATIONS_BEAN_PROPERTY, crawlerConfigurations);
     }
 
     @SuppressWarnings("unchecked")
     private void parseTasksConfiguration(Element tasksElement, BeanDefinitionBuilder beanDefinitionBuilder) throws NumberFormatException, MadStoreConfigurationException, DOMException {
         List<Element> taskElements = DomUtils.getChildElementsByTagName(tasksElement, TASK_TAG);
         Map<String, SimpleTriggerConfiguration> triggerTasks = new ManagedMap();
         for (Element task : taskElements) {
             String key = task.getAttribute(TASK_NAME);
             Element simpleTriggerConfigurationElement = DomUtils.getChildElementByTagName(task, SIMPLE_TRIGGER_CONFIGURATION_TAG);
             String startDelaySt = DomUtils.getChildElementByTagName(simpleTriggerConfigurationElement, START_DELAY).getTextContent();
             String repeatIntervalSt = DomUtils.getChildElementByTagName(simpleTriggerConfigurationElement, REPEAT_INTERVAL).getTextContent();
             if ("".equals(startDelaySt) || "".equals(repeatIntervalSt)) {
                 throw new MadStoreConfigurationException("Parameters startDelay and repeatInterval in " + SIMPLE_TRIGGER_CONFIGURATION_TAG + " tag cannot be empty.");
             }
             SimpleTriggerConfiguration simpleTriggerConfiguration = new SimpleTriggerConfiguration();
             simpleTriggerConfiguration.setStartDelay(new Integer(startDelaySt).intValue());
             simpleTriggerConfiguration.setRepeatInterval(new Integer(repeatIntervalSt).intValue());
             triggerTasks.put(key, simpleTriggerConfiguration);
         }
         beanDefinitionBuilder.addPropertyValue(TASKS_CONFIGURATION_BEAN_PROPERTY, triggerTasks);
 
         List<CrawlerConfiguration> crawlerConfigurations = (List<CrawlerConfiguration>) beanDefinitionBuilder.getBeanDefinition().getPropertyValues().getPropertyValue(
                 CRAWLER_CONFIGURATIONS_BEAN_PROPERTY).getValue();
 
         if (triggerTasks.keySet().contains(CRAWLER_TASK) && crawlerConfigurations.size() == 0) {
             throw new MadStoreConfigurationException("Crawler task cannot exist without crawler tag definition!");
         }
     }
 
     private void parseGridConfiguration(Element crawlerElement, BeanDefinitionBuilder beanDefinitionBuilder) {
         Element gridEnabled = DomUtils.getChildElementByTagName(crawlerElement, GRID_ENABLED_TAG);
         if (gridEnabled != null) {
             String madStoreDir = MadStoreConfigurationManager.getInstance().getMadStoreHome();
             String gridDir = new StringBuilder(madStoreDir).append("/").append(GRID_FOLDER).toString();
             GridConfiguration gridConfiguration = new GridConfiguration();
             gridConfiguration.setHomeDir(gridDir);
             beanDefinitionBuilder.addPropertyValue(GRID_CONFIGURATION_BEAN_PROPERTY, gridConfiguration);
         }
     }
 
     private void parseJcrConfiguration(Element repositoryElement, BeanDefinitionBuilder beanDefinitionBuilder) throws DOMException {
         JcrConfiguration jcrConfiguration = new JcrConfiguration();
         String madStoreDir = MadStoreConfigurationManager.getInstance().getMadStoreHome();
         String jcrDir = new StringBuilder(madStoreDir).append("/").append(DEFAULT_JCR_FOLDER).toString();
         jcrConfiguration.setHomeDir(jcrDir);
         jcrConfiguration.setUsername("");
         jcrConfiguration.setPassword("".toCharArray());
         Integer maxHistory = Integer.valueOf(DomUtils.getChildElementByTagName(repositoryElement, JCR_MAX_HISTORY).getTextContent());
         jcrConfiguration.setMaxHistory(maxHistory);
         beanDefinitionBuilder.addPropertyValue(JCR_CONFIGURATION_BEAN_PROPERTY, jcrConfiguration);
     }
 
     @SuppressWarnings("unchecked")
     private void parseIndexConfiguration(Element repositoryElement, BeanDefinitionBuilder beanDefinitionBuilder) {
         IndexConfiguration indexConfiguration = new IndexConfiguration();
 
         Element indexConfigurationElement = DomUtils.getChildElementByTagName(repositoryElement, INDEX_CONFIGURATION_TAG);
         String madStoreDir = MadStoreConfigurationManager.getInstance().getMadStoreHome();
         String indexDir = new StringBuilder(madStoreDir).append("/").append(DEFAULT_INDEX_FOLDER).toString();
         indexConfiguration.setIndexDir(indexDir);
 
         List<Element> indexedPropertiesNamespacesElementList = DomUtils.getChildElementsByTagName(DomUtils.getChildElementByTagName(indexConfigurationElement, INDEXED_PROPERTIES_NAMESPACES),
                 NAMESPACE);
         Map<String, String> indexedPropertiesNamespaces = new HashMap<String, String>();
         for (Element namespaceElement : indexedPropertiesNamespacesElementList) {
             String name = namespaceElement.getAttribute(NAMESPACE_PREFIX);
             String xPath = namespaceElement.getAttribute(NAMESPACE_URL);
             indexedPropertiesNamespaces.put(name, xPath);
         }
         indexConfiguration.setIndexedPropertiesNamespaces(indexedPropertiesNamespaces);
 
         List<Element> indexedPropertiesElementList = DomUtils.getChildElementsByTagName(DomUtils.getChildElementByTagName(indexConfigurationElement, INDEXED_PROPERTIES), PROPERTY);
         List<Property> indexedProperties = new ManagedList(indexedPropertiesElementList.size());
         for (Element propertyElement : indexedPropertiesElementList) {
             String name = propertyElement.getAttribute(PROPERTY_NAME);
             String xPath = DomUtils.getChildElementByTagName(propertyElement, XPATH).getTextContent();
             int boost = Integer.valueOf((DomUtils.getChildElementByTagName(propertyElement, BOOST).getTextContent()));
             Property indexedProperty = indexConfiguration.new Property(name, xPath, boost);
             indexedProperties.add(indexedProperty);
         }
         indexConfiguration.setIndexedProperties(indexedProperties);
         beanDefinitionBuilder.addPropertyValue(INDEX_CONFIGURATION_BEAN_PROPERTY, indexConfiguration);
     }
 
     private void dummyAppConfiguration(BeanDefinitionBuilder beanDefinitionBuilder) {
         AtomPublishingProtocolConfiguration atomPublishingProtocolConfiguration = new AtomPublishingProtocolConfiguration();
         atomPublishingProtocolConfiguration.setWorkspace(new String());
         beanDefinitionBuilder.addPropertyValue(APP_CONFIGURATION_BEAN_PROPERTY, atomPublishingProtocolConfiguration);
     }
 
     private void parseAppConfiguration(Element serverElement, BeanDefinitionBuilder beanDefinitionBuilder) throws DOMException {
         Element appConfigurationElement = DomUtils.getChildElementByTagName(serverElement, APP_CONFIGURATION_TAG);
         AtomPublishingProtocolConfiguration atomPublishingProtocolConfiguration = new AtomPublishingProtocolConfiguration();
         atomPublishingProtocolConfiguration.setWorkspace(DomUtils.getChildElementByTagName(appConfigurationElement, WORKSPACE).getTextContent());
         beanDefinitionBuilder.addPropertyValue(APP_CONFIGURATION_BEAN_PROPERTY, atomPublishingProtocolConfiguration);
     }
 
     private void dummyOsConfiguration(BeanDefinitionBuilder beanDefinitionBuilder) {
         OpenSearchConfiguration openSearchConfiguration = new OpenSearchConfiguration();
         openSearchConfiguration.setShortName(new String());
         openSearchConfiguration.setDescription(new String());
         beanDefinitionBuilder.addPropertyValue(OS_CONFIGURATION_BEAN_PROPERTY, openSearchConfiguration);
     }
 
     private void parseOsConfiguration(Element serverElement, BeanDefinitionBuilder beanDefinitionBuilder) throws DOMException {
         Element osConfigurationElement = DomUtils.getChildElementByTagName(serverElement, OS_CONFIGURATION_TAG);
         OpenSearchConfiguration openSearchConfiguration = new OpenSearchConfiguration();
         openSearchConfiguration.setShortName(DomUtils.getChildElementByTagName(osConfigurationElement, SHORT_NAME).getTextContent());
         openSearchConfiguration.setDescription(DomUtils.getChildElementByTagName(osConfigurationElement, DESCRIPTION).getTextContent());
         beanDefinitionBuilder.addPropertyValue(OS_CONFIGURATION_BEAN_PROPERTY, openSearchConfiguration);
     }
 
 }
