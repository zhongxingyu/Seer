 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rdonasco.security.capability.views;
 
 import com.rdonasco.common.exceptions.WidgetInitalizeException;
 import com.rdonasco.common.i18.I18NResource;
 import com.rdonasco.common.vaadin.view.ControlledView;
 import com.rdonasco.security.app.themes.SecurityDefaultTheme;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  *
  * @author Roy F. Donasco
  */
 public class CapabilityEditorView extends VerticalLayout implements
 		ControlledView
 {
 	private static final long serialVersionUID = 1L;
 	private Panel capabilityDetailPanel = new Panel();
 	private Panel capabilityActionsPanel = new Panel();
 	private HorizontalLayout buttonLayout = new HorizontalLayout();
 	private Button saveButton = new Button();
 	private Button editButton = new Button();
 
 	@Override
 	public void initWidget() throws WidgetInitalizeException
 	{
 		capabilityDetailPanel.setCaption(I18NResource.localize("Capability Editor"));
 		capabilityActionsPanel.setCaption(I18NResource.localize("Allowed Actions"));
 		editButton.setCaption(I18NResource.localize("Edit"));
 		editButton.setIcon(new ThemeResource(SecurityDefaultTheme.ICONS_16x16_EDIT));
 		saveButton.setCaption(I18NResource.localize("Save"));
 		saveButton.setIcon(new ThemeResource(SecurityDefaultTheme.ICONS_16x16_SAVE));
 		buttonLayout.addComponent(editButton);
 		buttonLayout.addComponent(saveButton);
 		buttonLayout.setSpacing(true);
 
 		addComponent(capabilityDetailPanel);
 		addComponent(capabilityActionsPanel);
 		addComponent(buttonLayout);
 		setExpandRatio(capabilityActionsPanel, 1);
 
 		addStyleName(SecurityDefaultTheme.CSS_CAPABILITY_EDITOR);
 		setMargin(true);
 		setSpacing(true);
 	}
 
 	public Button getSaveButton()
 	{
 		return saveButton;
 	}
 
 	public Button getEditButton()
 	{
 		return editButton;
 	}
 
 }
