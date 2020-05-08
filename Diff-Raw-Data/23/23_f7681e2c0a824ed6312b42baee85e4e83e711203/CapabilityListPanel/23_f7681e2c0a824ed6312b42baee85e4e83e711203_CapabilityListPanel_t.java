 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rdonasco.security.capability.views;
 
 import com.rdonasco.common.exceptions.WidgetInitalizeException;
 import com.rdonasco.common.i18.I18NResource;
 import com.rdonasco.common.vaadin.view.ControlledView;
import com.rdonasco.security.app.themes.SecurityDefaultTheme;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 import javax.enterprise.context.Dependent;
 
 /**
  *
  * @author Roy F. Donasco
  */
 @Dependent
 public class CapabilityListPanel extends Panel implements
 		ControlledView
 {
 	private static final long serialVersionUID = 1L;
 	private Table dataViewListTable;
 
 	public CapabilityListPanel()
 	{
 		setCaption(I18NResource.localize("Capabilities"));
		setStyleName(SecurityDefaultTheme.CSS_PANEL_BUBBLE);
 	}
 
 	public Table getDataViewListTable()
 	{
 		return dataViewListTable;
 	}
 
 	public void setDataViewListTable(Table dataViewListTable)
 	{
 		this.dataViewListTable = dataViewListTable;
		this.dataViewListTable.addStyleName(SecurityDefaultTheme.CSS_TABLE_SMALL_STRIPED);
		this.dataViewListTable.addStyleName(SecurityDefaultTheme.CSS_TABLE_BORDERLESS);
 	}
 
 
 	@Override
 	public void initWidget() throws WidgetInitalizeException
 	{
 		try
 		{
 			VerticalLayout content = ((VerticalLayout) getContent());
 			content.setMargin(true);
 			content.setHeight(600, UNITS_PIXELS);
 			content.removeAllComponents();
 			if (null != getDataViewListTable())
 			{				
 				getDataViewListTable().setSizeFull();
 				content.addComponent(getDataViewListTable());
 				content.setExpandRatio(getDataViewListTable(), 1);
 			}
 			
 		}
 		catch (Exception e)
 		{
 			throw new WidgetInitalizeException(e);
 		}
 	}
 }
