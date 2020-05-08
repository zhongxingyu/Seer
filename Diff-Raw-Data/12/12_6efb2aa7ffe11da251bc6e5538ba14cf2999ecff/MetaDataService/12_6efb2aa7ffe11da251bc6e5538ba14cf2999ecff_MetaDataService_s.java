 /**
  * Copyright (c) 2013 Christian Pelster.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Christian Pelster - initial API and implementation
  */
 package de.pellepelster.myadmin.server.services;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import net.sf.extcos.ComponentQuery;
 import net.sf.extcos.ComponentScanner;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.core.annotation.AnnotationUtils;
 import org.springframework.stereotype.Component;
 
 import de.pellepelster.myadmin.client.base.db.vos.IBaseVO;
 import de.pellepelster.myadmin.client.base.db.vos.IMobileBaseVO;
 import de.pellepelster.myadmin.db.util.BeanUtil;
 import de.pellepelster.myadmin.server.core.xml.XmlVOMapping;
 
 @Component
 public class MetaDataService implements InitializingBean
 {
 	private final static Logger LOG = Logger.getLogger(MetaDataService.class);
 
 	private List<Class<? extends IBaseVO>> voClasses = new ArrayList<Class<? extends IBaseVO>>();
 	private final Map<Class<? extends IBaseVO>, Class<?>> voXmlMappings = new HashMap<Class<? extends IBaseVO>, Class<?>>();
 	private final Map<Class<? extends IBaseVO>, Class<?>> voXmlListWrapperMappings = new HashMap<Class<? extends IBaseVO>, Class<?>>();
 
 	private final Map<Class<? extends IBaseVO>, Class<?>> voXmlReferenceListWrapperMappings = new HashMap<Class<? extends IBaseVO>, Class<?>>();
 	private final Map<Class<? extends IBaseVO>, Class<?>> voXmlReferenceMappings = new HashMap<Class<? extends IBaseVO>, Class<?>>();
 
 	private final Map<String, Class<?>> xmlRootElementClasses = new HashMap<String, Class<?>>();
 
 	@Override
 	public void afterPropertiesSet() throws Exception
 	{
 		ComponentScanner scanner = new ComponentScanner();
 
 		findVOClasses(scanner);
 
 		LOG.info(String.format("searching for xml vo mapping classes..."));
 		Set<Class<?>> xmlVoMappingClasses = scanner.getClasses(new ComponentQuery()
 		{
 			@Override
 			protected void query()
 			{
 				select().from("de").returning(allAnnotatedWith(XmlVOMapping.class));
 			}
 		});
 
 		for (Class<?> xmlVoMappingClass : xmlVoMappingClasses)
 		{
 			XmlVOMapping xmlVOMapping = AnnotationUtils.findAnnotation(xmlVoMappingClass, XmlVOMapping.class);
 
 			if (xmlVOMapping.isListWrapper())
 			{
 				LOG.info(String.format("found list wrapper '%s' for vo '%s'", xmlVoMappingClass.getName(), xmlVOMapping.voClass()));
 				this.voXmlListWrapperMappings.put(xmlVOMapping.voClass(), xmlVoMappingClass);
 			}
 
 			if (!xmlVOMapping.isListWrapper() && !xmlVOMapping.isReference() && !xmlVOMapping.isReferenceListWrapper())
 			{
 				LOG.info(String.format("found xml class '%s' for vo '%s'", xmlVoMappingClass.getName(), xmlVOMapping.voClass()));
 				this.voXmlMappings.put(xmlVOMapping.voClass(), xmlVoMappingClass);
 			}
 
 			if (xmlVOMapping.isReferenceListWrapper())
 			{
 				LOG.info(String.format("found xml reference list wrapper class '%s' for vo '%s'", xmlVoMappingClass.getName(), xmlVOMapping.voClass()));
 				this.voXmlReferenceListWrapperMappings.put(xmlVOMapping.voClass(), xmlVoMappingClass);
 			}
 
 			if (xmlVOMapping.isReference())
 			{
 				LOG.info(String.format("found xml reference class '%s' for vo '%s'", xmlVoMappingClass.getName(), xmlVOMapping.voClass()));
 				this.voXmlReferenceMappings.put(xmlVOMapping.voClass(), xmlVoMappingClass);
 			}
 
 		}
 
 		LOG.info(String.format("searching for xml root element classes..."));
 		Set<Class<?>> xmlRootElementClasses = scanner.getClasses(new ComponentQuery()
 		{
 
 			@Override
 			protected void query()
 			{
 				select().from("de").returning(allAnnotatedWith(XmlRootElement.class));
 			}
 		});
 
 		for (Class<?> xmlRootElementClass : xmlRootElementClasses)
 		{
 			XmlRootElement xmlRootElement = AnnotationUtils.findAnnotation(xmlRootElementClass, XmlRootElement.class);
 			LOG.info(String.format("found xml root class '%s' for element '%s'", xmlRootElementClass.getName(), xmlRootElement.name()));
 			this.xmlRootElementClasses.put(xmlRootElement.name(), xmlRootElementClass);
 		}
 
 	}
 
 	private void findVOClasses(ComponentScanner scanner)
 	{
 		LOG.info(String.format("searching for vo classes..."));
 		Set<Class<?>> tmpVoClasses = scanner.getClasses(new ComponentQuery()
 		{
 			@Override
 			protected void query()
 			{
 				select().from("de").returning(allImplementing(IBaseVO.class));
 			}
 		});
 
 		for (Class<?> voClass : tmpVoClasses)
 		{
 			if (IMobileBaseVO.class.isAssignableFrom(voClass))
 			{
 				LOG.info(String.format("ignoring mobile vo '%s'", voClass.getName()));
 			}
 			else if (voClass.getPackage() != null && voClass.getPackage().getName() != null && voClass.getPackage().getName().contains(".test."))
 			{
 				LOG.info(String.format("ignoring test vo '%s'", voClass.getName()));
 			}
 			else
 			{
 				LOG.info(String.format("found '%s'", voClass.getName()));
 				this.voClasses.add((Class<? extends IBaseVO>) voClass);
 			}
 		}
 		LOG.info(String.format("%d classes found", this.voClasses.size()));
 
 		DirectedGraph<Class<? extends IBaseVO>> directedGraph = new DirectedGraph<Class<? extends IBaseVO>>();
 		for (Class<? extends IBaseVO> voClass : this.voClasses)
 		{
 			directedGraph.addNode(voClass);
 		}
 
 		for (Class<? extends IBaseVO> voClass : this.voClasses)
 		{
 			Set<Class<? extends IBaseVO>> dependentClasses = BeanUtil.getDependentVOs(voClass);
 
 			for (Class<? extends IBaseVO> dependentClass : dependentClasses)
 			{
 				if (!voClass.equals(dependentClass))
 				{
 					directedGraph.addEdge(voClass, dependentClass);
 				}
 			}
 		}
 
		this.voClasses = TopologicalSort.sort(directedGraph);
 
		Collections.reverse(this.voClasses);
 	}
 
 	public Class<? extends IBaseVO> getVOClassForXmlClass(Class<?> xmlClass)
 	{
 		XmlVOMapping xmlVOMapping = AnnotationUtils.findAnnotation(xmlClass, XmlVOMapping.class);
 
 		if (xmlVOMapping == null)
 		{
 			throw new RuntimeException(String.format("class '%s' has no xml vo mapping information", xmlClass.getName()));
 		}
 
 		return xmlVOMapping.voClass();
 	}
 
 	public Class<?> getXmlClassForVOClass(Class<? extends IBaseVO> voClass)
 	{
 		return this.voXmlMappings.get(voClass);
 	}
 
 	public Class<?> getXmlListWrapperClassForVOClass(Class<? extends IBaseVO> voClass)
 	{
 		if (this.voXmlListWrapperMappings.containsKey(voClass))
 		{
 			return this.voXmlListWrapperMappings.get(voClass);
 		}
 		else
 		{
 			throw new RuntimeException(String.format("no xml list wrapper found for '%s'", voClass.getName()));
 		}
 	}
 
 	public Class<?> getXmlReferenceClassForVOClass(Class<? extends IBaseVO> voClass)
 	{
 		return this.voXmlReferenceMappings.get(voClass);
 	}
 
 	public Class<?> getXmlReferenceListWrapperClassForVOClass(Class<? extends IBaseVO> voClass)
 	{
 		return this.voXmlReferenceListWrapperMappings.get(voClass);
 	}
 
 	public Class<?> getXmlRootClassByElementName(String elementName)
 	{
 		return this.xmlRootElementClasses.get(elementName);
 	}
 
 	public Collection<Class<?>> getXmlRootClasses()
 	{
 		return this.xmlRootElementClasses.values();
 	}
 
 	public List<Class<? extends IBaseVO>> getVOClasses()
 	{
 		return this.voClasses;
 	}
 
 }
