 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.web.infopages.config;
 
 import org.agatom.springatom.core.module.AbstractModuleConfiguration;
 import org.agatom.springatom.web.component.config.ComponentBuilderModuleConfiguration;
 import org.agatom.springatom.web.infopages.annotation.DomainInfoPage;
 import org.agatom.springatom.web.infopages.annotation.InfoPage;
 import org.agatom.springatom.web.infopages.component.helper.InfoPageComponentHelper;
 import org.agatom.springatom.web.infopages.component.helper.impl.DefaultInfoPageComponentHelper;
 import org.agatom.springatom.web.infopages.mapping.InfoPageMappings;
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.BeanInitializationException;
 import org.springframework.beans.factory.ListableBeanFactory;
 import org.springframework.beans.factory.config.ConfigurableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.annotation.*;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 @Configuration(value = InfoPageModuleConfiguration.MODULE_NAME)
@PropertySource(value = "classpath:org/agatom/springatom/web/infopages/infopage.properties")
 @ComponentScan(
         nameGenerator = ComponentBuilderModuleConfiguration.NameGen.class,
         basePackages = {
                 "org.agatom.springatom"
         },
         useDefaultFilters = false,
         includeFilters = {
                 @ComponentScan.Filter(value = InfoPage.class, type = FilterType.ANNOTATION),
                 @ComponentScan.Filter(value = DomainInfoPage.class, type = FilterType.ANNOTATION)
         }
 )
 public class InfoPageModuleConfiguration
         extends AbstractModuleConfiguration
         implements BeanFactoryAware,
                    ApplicationContextAware {
     protected static final String MODULE_NAME = "InfoPageConfiguration";
     private static final   Logger LOGGER      = Logger.getLogger(InfoPageModuleConfiguration.class);
     private ListableBeanFactory beanFactory;
     private ApplicationContext  applicationContext;
 
     @Bean
     @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
     public InfoPageMappings getInfoPageMapping() {
         this.logRegistering(InfoPageMappings.class, LOGGER);
         final InfoPageMappings mappings = new InfoPageMappings();
         mappings.setInfoPageConfigurationSource(this.getInfoPageConfigurationSource());
         return mappings;
     }
 
     @Bean
     @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
     public InfoPageComponentProvider getInfoPageComponentProvider() {
         this.logRegistering(InfoPageComponentProvider.class, LOGGER);
         return new InfoPageComponentProvider();
     }
 
     @Bean
     @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
     public InfoPageAwareBeanPostProcessor getInfoPagePostProcessor() {
         this.logRegistering(InfoPageAwareBeanPostProcessor.class, LOGGER);
         final InfoPageAwareBeanPostProcessor processor = new InfoPageAwareBeanPostProcessor();
         processor.setBeanFactory(this.beanFactory);
         processor.setBasePackage(this.applicationContext.getEnvironment().getProperty("springatom.infoPages.basePackage"));
         return processor;
     }
 
     @Bean
     @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
     public InfoPageConfigurationSource getInfoPageConfigurationSource() {
         this.logRegistering(InfoPageConfigurationSource.class, LOGGER);
         final InfoPageConfigurationSourceImpl source = new InfoPageConfigurationSourceImpl()
                 .setProvider(this.getInfoPageComponentProvider())
                 .setBasePackage(this.applicationContext.getEnvironment().getProperty("springatom.infoPages.basePackage"));
         source.setApplicationContext(this.applicationContext);
         return source;
     }
 
     @Bean
     @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
     public InfoPageComponentHelper getInfoPageComponentHelper() {
         this.logRegistering(DefaultInfoPageComponentHelper.class, LOGGER);
         return new DefaultInfoPageComponentHelper();
     }
 
     @Override
     public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
         if (beanFactory instanceof ListableBeanFactory) {
             this.beanFactory = (ListableBeanFactory) beanFactory;
             LOGGER.trace(String.format("/setBeanFactory -> %s", beanFactory));
             return;
         }
         throw new BeanInitializationException(String.format("%s is not %s", beanFactory, ListableBeanFactory.class));
     }
 
     @Override
     public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
     }
 
 }
