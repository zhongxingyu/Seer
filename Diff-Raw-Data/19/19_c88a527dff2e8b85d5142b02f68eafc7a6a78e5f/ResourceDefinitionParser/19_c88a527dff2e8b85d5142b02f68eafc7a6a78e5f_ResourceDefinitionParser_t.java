 package net.northfuse.resources.config;
 
 import net.northfuse.resources.ResourceGenerator;
 import net.northfuse.resources.ResourceHandler;
 import net.northfuse.resources.ResourceHandlerAdapter;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.parsing.BeanComponentDefinition;
 import org.springframework.beans.factory.support.ManagedMap;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.beans.factory.xml.BeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.core.Ordered;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Parses a resource definition for a given resource handler.
  *
  * @param <T> The type of ResourceHandler
  * @author tylers2
  */
 abstract class ResourceDefinitionParser<T extends ResourceHandler> implements BeanDefinitionParser {
 	private final AtomicBoolean registeredAdapter = new AtomicBoolean();
 	private final Object lock = new Object();
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final BeanDefinition parse(Element element, ParserContext parserContext) {
 		doParse(parserContext, element, "false", "", "");
 		return null;
 	}
 
 	/**
 	 * Parses the config.
 	 *
 	 * @param parserContext The ParserContext
 	 * @param element The element
 	 * @param defaultDebug the default debug value if not set
 	 * @param baseMapping The base mapping url
 	 * @param defaultOrder The default order
 	 */
 	public final void doParse(ParserContext parserContext, Element element, String defaultDebug, String baseMapping, String defaultOrder) {
 		Object source = parserContext.extractSource(element);
 		Data data = registerResourceHandler(parserContext, element, source, defaultDebug, baseMapping);
 
 		if (data == null) {
 			return;
 		}
 
 		String resourceMapping = data.resourceMapping;
 		String handlerBeanName = data.handlerBeanName;
 
 		registerAdapterIfNeeded(parserContext);
 
 		RootBeanDefinition handlerMappingDefinition = new RootBeanDefinition(SimpleUrlHandlerMapping.class);
 		handlerMappingDefinition.setSource(source);
 		String order = element.getAttribute("order");
 		if (!StringUtils.hasText(order)) {
 			if (StringUtils.hasText(defaultOrder)) {
 				order = defaultOrder;
 			} else {
 				order = Integer.toString(Ordered.LOWEST_PRECEDENCE - 1);
 			}
 		}
 		handlerMappingDefinition.getPropertyValues().add("order", order);
 
 		Map<String, String> urlMap = new ManagedMap<String, String>();
 		urlMap.put(resourceMapping, handlerBeanName);
 		handlerMappingDefinition.getPropertyValues().add("urlMap", urlMap);
 
 		String handlerMappingBeanName = parserContext.getReaderContext().generateBeanName(handlerMappingDefinition);
 		parserContext.getRegistry().registerBeanDefinition(handlerMappingBeanName, handlerMappingDefinition);
 		parserContext.registerBeanComponent(new BeanComponentDefinition(handlerMappingDefinition, handlerMappingBeanName));
 	}
 
 	/**
 	 * Registers the resource handler.
 	 *
 	 * @param parserContext The ParserContext
 	 * @param element The element
 	 * @param source the source
 	 * @param defaultDebug the default debug value if not set
 	 * @param baseMapping The base mapping url
 	 *
 	 * @return The resource handler config information
 	 */
 	private Data registerResourceHandler(ParserContext parserContext, Element element, Object source, String defaultDebug, String baseMapping) {
 		RootBeanDefinition handlerDefinition = new RootBeanDefinition(getImplementation());
 
 		String debug = element.getAttribute("debug");
 		if (!StringUtils.hasText(debug)) {
 			debug = defaultDebug;
 		}
 		handlerDefinition.getPropertyValues().add("debug", debug);
 		handlerDefinition.setSource(source);
 
 		String resourceMapping = element.getAttribute("mapping");
 		if (!StringUtils.hasText(resourceMapping)) {
 			parserContext.getReaderContext().error("The 'mapping' attribute is required.", parserContext.extractSource(element));
 			return null;
 		}
 		handlerDefinition.getPropertyValues().add("mapping", baseMapping + resourceMapping);
 
 		//find resource locations
 		RootBeanDefinition generatorDefinition = new RootBeanDefinition(ResourceGenerator.class.getName());
 		generatorDefinition.getPropertyValues().add("resources", findLocations(element));
 
 		handlerDefinition.getPropertyValues().add("resourceGenerator", generatorDefinition);
 		String handlerBeanName = parserContext.getReaderContext().generateBeanName(handlerDefinition);
 		parserContext.getRegistry().registerBeanDefinition(handlerBeanName, handlerDefinition);
 		parserContext.registerBeanComponent(new BeanComponentDefinition(handlerDefinition, handlerBeanName));
 
 		Data data = new Data();
 		data.handlerBeanName = handlerBeanName;
		data.resourceMapping = baseMapping + resourceMapping;
 		return data;
 	}
 
 	/**
 	 * Finds the locations from the resource child nodes.
 	 *
 	 * @param element The Element that contains the resource nodes
 	 *
 	 * @return A list of resources
 	 */
 	private List<String> findLocations(Element element) {
 		List<String> resources = new LinkedList<String>();
 
 		final NodeList nodeList = element.getElementsByTagNameNS(ResourceNamespaceHandler.NAMESPACE, "resource");
 		for (Element e : new NodeListIterator<Element>(nodeList)) {
 			resources.add(e.getAttribute("location"));
 		}
 		return resources;
 	}
 
 	/**
 	 * Registers the adapter if it hasn't already been registered.
 	 *
 	 * @param parserContext The parserContext
 	 */
 	private void registerAdapterIfNeeded(ParserContext parserContext) {
 		synchronized (lock) {
 			if (!registeredAdapter.get()) {
 				registeredAdapter.set(true);
 
 				RootBeanDefinition adapterDefinition = new RootBeanDefinition(ResourceHandlerAdapter.class);
 				parserContext.getRegistry().registerBeanDefinition(ResourceHandlerAdapter.class.getName(), adapterDefinition);
 			}
 		}
 
 	}
 
 	/**
 	 * Gets the implementation class for this resource handler.
 	 *
 	 * @return The implementation class
 	 */
 	protected abstract Class<T> getImplementation();
 
 	/**
 	 * Simple data holder.
 	 */
 	private static class Data {
 		private String handlerBeanName;
 		private String resourceMapping;
 	}
 }
