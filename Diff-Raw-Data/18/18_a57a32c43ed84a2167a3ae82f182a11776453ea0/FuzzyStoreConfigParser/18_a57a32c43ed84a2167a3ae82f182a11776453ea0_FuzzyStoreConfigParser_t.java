 package org.fuzzydb.spring.config;
 
import static org.fuzzydb.spring.config.Constants.DEFAULT_STORE_ID;
 import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.*;
 
 import org.springframework.beans.factory.BeanDefinitionStoreException;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.util.StringUtils;
 import org.w3c.dom.Element;
 
 import com.wwm.db.EmbeddedClientFactory;
 import com.wwm.db.Store;
 
 public class FuzzyStoreConfigParser extends AbstractBeanDefinitionParser {
 
 	@Override
 	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
 		String id = element.getAttribute("id");
 		if (!StringUtils.hasText(id)) {
 			id = DEFAULT_STORE_ID;
 		}
 		return id;
 	}
 	
 	/* 
 	 * Aiming for this
 	 * 	<bean id="clientFactory" class="com.wwm.db.EmbeddedClientFactory"
 		factory-method="getInstance" />
 
 	<bean id="store" factory-bean="clientFactory" factory-method="openStore">
 		<constructor-arg value="wwmdb:/TravelDataStore"/>
 	</bean>
 	 */
 	@Override
 	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
 		
 		// Need to create the embedded client factory if we want embedded
 	    BeanDefinitionBuilder embeddedFactoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(EmbeddedClientFactory.class)
 	    		.setFactoryMethod("getInstance");
 
 	    String clientFactoryRef = registerWithGeneratedName(embeddedFactoryBuilder.getBeanDefinition(), parserContext.getRegistry());      
 
 	    String storeUrl = element.getAttribute("url");
 	    if (!StringUtils.hasText(storeUrl)) {
 	    	storeUrl = "wwmdb:/org.fuzzydb.DefaultStore";
 	    }
 		// TODO: Finish this... can now use bean definition as constructor args and refs in other bean defs
 	    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Store.class)
 	    		.addConstructorArgValue(storeUrl);
 	    builder.getRawBeanDefinition().setFactoryBeanName(clientFactoryRef);
 	    builder.getRawBeanDefinition().setFactoryMethodName("openStore");
 		return builder.getBeanDefinition();
 	}
 
 }
