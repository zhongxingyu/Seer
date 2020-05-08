 /*
  * Copyright 2013 Roy F. Donasco.
  * File Created on: 11-Apr-2013
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
 package com.rdonasco.datamanager.listeditor.controller;
 
 import com.rdonasco.common.exceptions.DataAccessException;
 import com.rdonasco.common.exceptions.WidgetException;
 import com.rdonasco.common.exceptions.WidgetInitalizeException;
 import com.rdonasco.common.i18.I18NResource;
 import com.rdonasco.common.vaadin.controller.ApplicationExceptionPopupProvider;
 import com.rdonasco.common.vaadin.controller.ApplicationPopupProvider;
 import com.rdonasco.common.vaadin.controller.ViewController;
 import com.rdonasco.common.vaadin.view.terminal.StreamResourceBuilder;
 import com.rdonasco.datamanager.controller.DataDeleteStrategy;
 import com.rdonasco.datamanager.controller.DataManagerContainer;
 import com.rdonasco.datamanager.controller.DataRetrieveListStrategy;
 import com.rdonasco.datamanager.controller.DataSaveStrategy;
 import com.rdonasco.datamanager.controller.DataUpdateStrategy;
 import com.rdonasco.datamanager.listeditor.view.ListEditorItem;
 import com.rdonasco.datamanager.listeditor.view.ListEditorTheme;
 import com.rdonasco.datamanager.listeditor.view.ListEditorView;
import com.rdonasco.datamanager.services.DataManager;
 import com.rdonasco.datamanager.utils.TableHelper;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.event.FieldEvents;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.MouseEvents;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
 
 /**
  *
  * @author Roy F. Donasco
  */
 public abstract class ListEditorViewPanelController<VO extends ListEditorItem>
 		implements ViewController<ListEditorView>
 {
 
 	private static final Logger LOG = Logger.getLogger(ListEditorViewPanelController.class.getName());
 	private static final long serialVersionUID = 1L;
 	private ListEditorView editorViewPanel = new ListEditorView();
 	private static final String TABLE_PROPERTY_ICON = "icon";
 	private DataManagerContainer<VO> dataContainer;
 	@Inject
 	private Instance<ApplicationExceptionPopupProvider> exceptionPopupProviderInstances;
 	private ApplicationExceptionPopupProvider exceptionPopupProvider;
 	@Inject
 	private Instance<ApplicationPopupProvider> popupProviderFactory;
 	private ApplicationPopupProvider popupProvider;
 	private Map<Object, Map<Object, TextField>> fieldMap = new HashMap<Object, Map<Object, TextField>>();
 
 	public void setDataContainer(
 			DataManagerContainer<VO> dataContainer)
 	{
 		this.dataContainer = dataContainer;
 	}
 
 	@Override
 	public void initializeControlledViewBehavior()
 	{
 		try
 		{
 			getEditorViewPanel().initWidget();
 			getEditorViewPanel().setCaption(getListName());
 			setupEditorViewListeners();
 			configureDataContainerDefaultStrategies();
 			configureEditorTableBehavior();
 			configureButtonBehavior();
 		}
 		catch (WidgetInitalizeException ex)
 		{
 			getExceptionPopupProvider().popUpErrorException(ex);
 		}
 	}
 
 	@Override
 	public ListEditorView getControlledView()
 	{
 		return getEditorViewPanel();
 	}
 
 	@Override
 	public void refreshView() throws WidgetException
 	{
 		try
 		{
 			dataContainer.refresh();
 		}
 		catch (DataAccessException ex)
 		{
 			throw new WidgetException(ex);
 		}
 	}
 
 	public void configureDataContainerDefaultStrategies()
 	{
 		if (null == dataContainer)
 		{
 			throw new NullPointerException("dataContainer cannot be null");
 		}
 		if (dataContainer.getDataManager() == null)
 		{
 			LOG.log(Level.WARNING, "dataContainer.dataManager is null, defaultStrategies may fail if it is not set properly. An alternative is to use custom strategies");
 		}
 		dataContainer.setDataRetrieveListStrategy(new DataRetrieveListStrategy<VO>()
 		{
 			@Override
 			public List<VO> retrieve() throws DataAccessException
 			{
 				List<VO> listItems;
 				listItems = dataContainer.getDataManager().retrieveAllData();
 				Embedded icon;
 				for (VO listItem : listItems)
 				{
 					icon = createDeleteIcon();
 					icon.setDescription(I18NResource.localize("Delete Item"));
 					listItem.setIcon(icon);
 					setupDeleteIconClickListener(icon, listItem);
 				}
 				return listItems;
 			}
 		});
 		dataContainer.setDataSaveStrategy(new DataSaveStrategy<VO>()
 		{
 			@Override
 			public VO save(VO dataToSaveAndReturn)
 					throws
 					DataAccessException
 			{
 				return dataContainer.getDataManager().saveData(dataToSaveAndReturn);
 			}
 		});
 		dataContainer.setDataUpdateStrategy(new DataUpdateStrategy<VO>()
 		{
 			@Override
 			public void update(VO dataToUpdate) throws
 					DataAccessException
 			{
 				dataContainer.getDataManager().updateData(dataToUpdate);
 			}
 		});
 		dataContainer.setDataDeleteStrategy(new DataDeleteStrategy<VO>()
 		{
 			@Override
 			public void delete(VO dataToDelete) throws
 					DataAccessException
 			{
 				dataContainer.getDataManager().deleteData(dataToDelete);
 			}
 		});
 	}
 
 	private void setupDeleteIconClickListener(Embedded icon,
 			final VO itemVO)
 	{
 		icon.addListener(new MouseEvents.ClickListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void click(MouseEvents.ClickEvent event)
 			{
 				try
 				{
 					dataContainer.removeItem(itemVO);
 				}
 				catch (RuntimeException ex)
 				{
 					LOG.log(Level.WARNING, ex.getMessage(), ex);
 					getPopupProvider().popUpError(I18NResource.localize("Error deleting an item"));
 				}
 			}
 		});
 	}
 
 	private void setupEditorViewListeners()
 	{
 		getEditorViewPanel().setAttachStrategy(new ListEditorAttachStrategy()
 		{
 			@Override
 			public void attached(Component component)
 			{
 				try
 				{
 					refreshView();
 				}
 				catch (WidgetException ex)
 				{
 					getExceptionPopupProvider().popUpErrorException(ex);
 				}
 
 			}
 		});
 	}
 
 	private void configureEditorTableBehavior()
 	{
 		if (null == getVisibleColumns())
 		{
 			throw new IllegalStateException("visibleColumns not yet set");
 		}
 		if (null == getColumnHeaders())
 		{
 			throw new IllegalStateException("columnHeaders not yet set");
 		}
 		getEditorViewPanel().getEditorTable().setContainerDataSource(dataContainer);
 		List<String> realVisibleColumns = new ArrayList<String>();
 		realVisibleColumns.add(TABLE_PROPERTY_ICON);
 		realVisibleColumns.addAll(Arrays.asList(getVisibleColumns()));
 		getEditorViewPanel().getEditorTable().setVisibleColumns(realVisibleColumns.toArray(new String[0]));
 		List<String> realColumnHeaders = new ArrayList<String>();
 		realColumnHeaders.add(""); // empty header for the icon
 		realColumnHeaders.addAll(Arrays.asList(getColumnHeaders()));
 		getEditorViewPanel().getEditorTable().setColumnHeaders(realColumnHeaders.toArray(new String[0]));
 		setupDefaultCellStyleGenerator();
 		getEditorViewPanel().getEditorTable().setReadOnly(false);
 
 		Table.ColumnGenerator columnGenerator = new Table.ColumnGenerator()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public Object generateCell(final Table source,
 					final Object itemId,
 					final Object columnId)
 			{
 				final TextField textField = new TextField();
 				try
 				{
 					textField.setPropertyDataSource(source.getContainerProperty(itemId, columnId));
 				}
 				catch (Exception ex)
 				{
 					LOG.log(Level.SEVERE, ex.getMessage(), ex);
 				}
 				textField.setReadOnly(true);
 				textField.setWriteThrough(true);
 				addFieldToFieldCache(itemId, columnId, textField);
 				textField.addListener(new FieldEvents.BlurListener()
 				{
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void blur(FieldEvents.BlurEvent event)
 					{
 						try
 						{
 							BeanItem<VO> itemToUpdate = (BeanItem) source.getItem(itemId);
 							textField.commit();
 							dataContainer.updateItem(itemToUpdate.getBean());
 							textField.setReadOnly(true);
 						}
						catch (DataAccessException ex)
 						{
							getExceptionPopupProvider().popUpErrorException(ex);
 						}
 					}
 				});
 				return textField;
 			}
 		};
 		for (String propertyName : getVisibleColumns())
 		{
 			getEditorViewPanel().getEditorTable().addGeneratedColumn(propertyName, columnGenerator);
 		}
 		getEditorViewPanel().getEditorTable().addListener(new ItemClickEvent.ItemClickListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void itemClick(ItemClickEvent event)
 			{
 				if (event.isDoubleClick())
 				{
 					TextField textField = getFieldFromCache(event.getItemId(), event.getPropertyId());
 					textField.setReadOnly(false);
 					textField.focus();
 				}
 			}
 		});
 		TableHelper.setupTable(getEditorViewPanel().getEditorTable());
 		getEditorViewPanel().getEditorTable().setDragMode(Table.TableDragMode.ROW);
 	}
 
 	public abstract String[] getColumnHeaders();
 
 	public abstract String[] getVisibleColumns();
 
 	public abstract VO createNewListEditorItem();
 
 	public abstract String getItemName();
 
 	public abstract String getListName();
 
 	private void setupDefaultCellStyleGenerator()
 	{
 		getEditorViewPanel().getEditorTable()
 				.setCellStyleGenerator(new Table.CellStyleGenerator()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public String getStyle(Object itemId, Object propertyId)
 			{
 				String style;
 				if (TABLE_PROPERTY_ICON.equals(propertyId))
 				{
 					style = ListEditorTheme.CSS_ICON_IN_A_CELL;
 				}
 				else
 				{
 					style = ListEditorTheme.CSS_FULL_WIDTH;
 				}
 				return style;
 			}
 		});
 	}
 
 	private void addFieldToFieldCache(Object itemId, Object columnId,
 			TextField textField)
 	{
 		Map<Object, TextField> subFieldMap = fieldMap.get(itemId);
 		if (null == subFieldMap)
 		{
 			subFieldMap = new HashMap<Object, TextField>();
 			fieldMap.put(itemId, subFieldMap);
 		}
 		subFieldMap.put(columnId, textField);
 	}
 
 	private TextField getFirstFieldFromCacheFor(ListEditorItem item)
 	{
 		return fieldMap.get(item).values().iterator().next();
 	}
 
 	private TextField getFieldFromCache(Object itemId, Object propertyId)
 	{
 		Map<Object, TextField> subFieldMap = fieldMap.get(itemId);
 		TextField textField = subFieldMap.get(propertyId);
 		return textField;
 	}
 
 	protected ApplicationExceptionPopupProvider getExceptionPopupProvider()
 	{
 		if (exceptionPopupProvider == null)
 		{
 			exceptionPopupProvider = exceptionPopupProviderInstances.get();
 		}
 		return exceptionPopupProvider;
 	}
 
 	protected ApplicationPopupProvider getPopupProvider()
 	{
 		if (popupProvider == null)
 		{
 			popupProvider = popupProviderFactory.get();
 		}
 		return popupProvider;
 	}
 
 	protected ListEditorView getEditorViewPanel()
 	{
 		return editorViewPanel;
 	}
 
 	private void configureButtonBehavior()
 	{
 		getEditorViewPanel().getAddItemButton().setCaption(I18NResource.localizeWithParameter("add new _", getItemName()));
 		getEditorViewPanel().getAddItemButton().addListener(new Button.ClickListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void buttonClick(Button.ClickEvent event)
 			{
 				addNewResourceItemVO();
 			}
 		});
 	}
 
 	private void addNewResourceItemVO()
 	{
 		try
 		{
 			Embedded icon = createDeleteIcon();
 			VO resourceItemVO = createNewListEditorItem();
 			setupDeleteIconClickListener(icon, resourceItemVO);
 			resourceItemVO.setIcon(icon);
 			BeanItem<VO> beanItem = dataContainer.addItem(resourceItemVO);
 			getEditorViewPanel().getEditorTable().setCurrentPageFirstItemId(beanItem.getBean());
 			getEditorViewPanel().getEditorTable().select(resourceItemVO);
 			TextField field = getFirstFieldFromCacheFor(resourceItemVO);
 			field.setReadOnly(false);
 			field.focus();
 			field.selectAll();
 		}
 		catch (RuntimeException e)
 		{
 			LOG.log(Level.SEVERE, e.getMessage(), e);
			getPopupProvider().popUpInfo(I18NResource.
 					localizeWithParameter("unable to add new item on", getListName()));
 		}
 
 	}
 
 	private Embedded createDeleteIcon()
 	{
 		Embedded icon;
 		icon = new Embedded(null, new StreamResourceBuilder()
 				.setReferenceClass(ListEditorView.class)
 				.setRelativeResourcePath("images/delete.png")
 				.setApplication(getEditorViewPanel().getApplication())
 				.createStreamResource());
 		return icon;
 	}
 }
