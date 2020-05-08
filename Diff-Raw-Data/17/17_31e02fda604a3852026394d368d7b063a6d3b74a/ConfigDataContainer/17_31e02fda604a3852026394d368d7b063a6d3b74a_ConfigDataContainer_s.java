 /*
  * Copyright 2011 Roy F. Donasco.
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
 package com.rdonasco.config.view;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.util.HierarchicalContainer;
 import com.vaadin.ui.Field;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import com.rdonasco.config.data.ConfigAttribute;
 import com.rdonasco.config.data.ConfigData;
 import com.rdonasco.config.data.ConfigElement;
 import com.rdonasco.config.services.ConfigDataManagerLocal;
 import com.rdonasco.common.exceptions.DataAccessException;
 import com.rdonasco.common.exceptions.WidgetInitalizeException;
 import com.rdonasco.common.vaadin.view.ViewWidget;
 import com.rdonasco.common.i18.I18NResource;
 
 /**
  *
  * @author Roy F. Donasco
  */
 public class ConfigDataContainer extends HierarchicalContainer implements
 		ViewWidget
 {
	private static final long serialVersionUID = 1L;
 
 	@EJB
 	private ConfigDataManagerLocal dataManager;
 	protected static final String PROPERTY_CONFIG_NAME = "name";
 	protected static final String PROPERTY_CONFIG_VALUE = "value";
 	protected static final String PROPERTY_CONFIG_VALUE_FIELD = "field";
 	protected static final String PROPERTY_CONFIG_CHANGE_APPLICATOR = "changeApplicator";
 
 	@PostConstruct
 	@Override
 	public void initWidget() throws WidgetInitalizeException
 	{
 		try
 		{
 			String nullString = null;
 			Map<Object, Field> nullFieldMap = null;
 			ConfigValueChangeApplicator nullValueApplicator = null;
 			addContainerProperty(PROPERTY_CONFIG_NAME, String.class,
 					nullString);
 			addContainerProperty(PROPERTY_CONFIG_VALUE,
 					String.class,
 					nullString);
 
 			addContainerProperty(PROPERTY_CONFIG_VALUE_FIELD, Map.class,
 					nullFieldMap);
 			addContainerProperty(PROPERTY_CONFIG_CHANGE_APPLICATOR,
 					ConfigValueChangeApplicator.class,
 					nullValueApplicator);
 
 			loadConfigFromDatabase();
 		}
 		catch (Exception e)
 		{
 			throw new WidgetInitalizeException(e);
 		}
 	}
 
 	private void loadConfigFromDatabase() throws DataAccessException
 	{
 		List<ConfigElement> configElements = dataManager.retrieveAllData();
 
 		addConfigElementsToTreeTable(configElements, null);
 
 	}
 
 	private void addConfigElementsToTreeTable(List<ConfigElement> configElements,
 			ConfigElement parentElement)
 	{
 		for (ConfigElement configElement : configElements)
 		{
 			addConfigElementToTreeTable(configElement, parentElement);
 			List<ConfigAttribute> configAttributes = configElement.getAttributes();
 			if (configAttributes != null && !configAttributes.isEmpty())
 			{
 				for (ConfigAttribute configAttribute : configAttributes)
 				{
 					addConfigAttributeToTreeTable(configAttribute, configElement);
 				}
 			}
 			List<ConfigElement> subConfigElements = configElement.
 					getSubConfigElements();
 			if (subConfigElements != null && !subConfigElements.isEmpty())
 			{
 				addConfigElementsToTreeTable(subConfigElements, configElement);
 			}
 		}
 	}
 
 	private Item addConfigElementToTreeTable(ConfigElement configElement,
 			ConfigElement parentConfigElement)
 	{
 		Item configElementItem = addItem(configElement);
 		bindConfigElementToItem(configElementItem, configElement);
 		setChildrenAllowed(configElementItem, true);
 		if (null != parentConfigElement)
 		{
 			setParent(configElement, parentConfigElement);
 		}
 		return configElementItem;
 
 	}
 
 	private void bindConfigElementToItem(final Item configElementItem,
 			final ConfigElement configElement)
 	{
 		configElementItem.getItemProperty(PROPERTY_CONFIG_NAME).setValue(
 				configElement.getName());
 		configElementItem.getItemProperty(PROPERTY_CONFIG_CHANGE_APPLICATOR).
 				setValue(new ConfigValueChangeApplicator()
 		{

 			@Override
 			public void applyChanges() throws ValueChangeException
 			{
 				try
 				{
 					if (null != configElement && null != configElementItem)
 					{
 						configElement.setName(configElementItem.getItemProperty(
 								PROPERTY_CONFIG_NAME).getValue().toString());
 						dataManager.updateData(configElement);
 					}
 
 				}
 				catch (DataAccessException ex)
 				{
 					throw new ValueChangeException(ex);
 				}
 			}
 		});
 
 	}
 	private static long elementID = 0;
 
 	private ConfigElement createConfigElement(ConfigElement parent) throws
 			DataAccessException
 	{
 		ConfigElement element = new ConfigElement();
 		element.setAttributes(new ArrayList<ConfigAttribute>());
 		element.setName(I18NResource.localizeWithParameter(
 				"New Element Name", elementID++));
 		element.setSubConfigElements(new ArrayList<ConfigElement>());
 		if (parent != null)
 		{
 			element.setParentConfig(parent);
 			parent.getSubConfigElements().add(element);
 		}
 		element = dataManager.saveData(element);
 
 		return element;
 
 	}
 	private static long attributeID = 0;
 
 	private ConfigAttribute createConfigAttribute(ConfigElement parent) throws
 			DataAccessException
 	{
 		ConfigAttribute attribute = new ConfigAttribute();
 
 		attribute.setName(I18NResource.localizeWithParameter(
 				"New Attribute Name", attribute.getId()));
 		attribute.setValue(I18NResource.localizeWithParameter(
 				"New Attribute Value", attribute.getId()));
 		attribute.setParentConfig(parent);
 		attribute = dataManager.saveAttribute(attribute);
 
 		return attribute;
 	}
 
 	private Item addConfigAttributeToTreeTable(ConfigAttribute configAttribute,
 			ConfigElement configElement)
 	{
 		Item configAttributeItem = addItem(configAttribute);
		bindConfigAttributeToItem(configAttributeItem, configAttribute);
		setChildrenAllowed(configAttribute, false);
		setParent(configAttribute, configElement);
 		return configAttributeItem;
 	}
 
 	private void bindConfigAttributeToItem(final Item configAttributeItem,
 			final ConfigAttribute configAttribute)
 	{
 		configAttributeItem.getItemProperty(PROPERTY_CONFIG_NAME).setValue(
 				configAttribute.getName());
 		configAttributeItem.getItemProperty(PROPERTY_CONFIG_VALUE).setValue(configAttribute.
 				getValue());
 		configAttributeItem.getItemProperty(PROPERTY_CONFIG_CHANGE_APPLICATOR).
 				setValue(new ConfigValueChangeApplicator()
 		{

 			@Override
 			public void applyChanges() throws ValueChangeException
 			{
 				try
 				{
 					// possible null pointer exception here
 					configAttribute.setName(configAttributeItem.getItemProperty(
 							PROPERTY_CONFIG_NAME).getValue().toString());
 					configAttribute.setValue(configAttributeItem.getItemProperty(
 							PROPERTY_CONFIG_VALUE).getValue().toString());
 					dataManager.updateAttribute(configAttribute);
 				}
 				catch (DataAccessException ex)
 				{
 					throw new ValueChangeException(ex);
 				}
 			}
 		});
 	}
 
 	public ConfigElement createNewConfigElement() throws DataAccessException
 	{
 		ConfigElement configElement = createConfigElement(null);
 		addConfigElementToTreeTable(configElement, null);
 		return configElement;
 	}
 
 	public ConfigElement createNewSubConfigElement(ConfigElement parentElement)
 			throws DataAccessException
 	{
 		ConfigElement configElement = createConfigElement(parentElement);
 		addConfigElementToTreeTable(configElement, parentElement);
 		setParent(configElement, parentElement);
 		return configElement;
 	}
 
 	public ConfigAttribute createNewAttribute(ConfigElement parentElement)
 			throws DataAccessException
 	{
 		ConfigAttribute configAttribute = createConfigAttribute(parentElement);
 		addConfigAttributeToTreeTable(configAttribute, parentElement);
 		return configAttribute;
 	}
 
 	@Override
 	public boolean removeItemRecursively(Object itemId)
 	{
 		boolean removed = super.removeItemRecursively(itemId);
 		return removed;
 	}
 
 	void removeConfigData(ConfigData configData) throws DataAccessException
 	{
 		if (configData instanceof ConfigElement)
 		{
 			dataManager.deleteData((ConfigElement) configData);
 		}
 		else if (configData instanceof ConfigAttribute)
 		{
 			dataManager.deleteAttribute((ConfigAttribute) configData);
 		}
 		removeItemRecursively(configData);
 	}
 
 	void refreshData() throws DataAccessException
 	{
 		removeAllItems();
 		loadConfigFromDatabase();
 	}
 }
