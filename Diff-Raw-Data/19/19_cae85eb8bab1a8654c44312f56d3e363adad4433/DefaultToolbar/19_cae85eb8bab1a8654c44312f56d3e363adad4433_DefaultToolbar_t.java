 package com.argility.centralpages.ui;
 
 import com.argility.centralpages.CentralpagesApplication;
 import com.argility.centralpages.ui.nav.ReplicationNavigationTree;
 import com.argility.centralpages.ui.nav.SwitchingErrorNavigationTree;
 import com.argility.centralpages.ui.nav.SwitchingNavigationTree;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 
 @SuppressWarnings("serial")
 public class DefaultToolbar extends HorizontalLayout implements Button.ClickListener{
 	
 	private Button switchingButton = new Button("Switching");
 	private Button replicationButton = new Button("Replication");
 	private Button switchingErrorsButton = new Button("Switching Errors");
 	
 	public DefaultToolbar() {
 		
 		switchingButton.setStyleName("toolbar-selected");
 		switchingButton.setIcon(new ThemeResource("icons/switching.png"));
 		switchingButton.addListener((Button.ClickListener)this);
 		addComponent(switchingButton);
 		
 		replicationButton.setIcon(new ThemeResource("icons/replication.png"));
 		replicationButton.addListener((Button.ClickListener)this);
 		addComponent(replicationButton);
 		
 		switchingErrorsButton.setIcon(new ThemeResource("icons/sw_errors.png"));
 		switchingErrorsButton.addListener((Button.ClickListener)this);
 		addComponent(switchingErrorsButton);
 		
 		setMargin(true);
 		setSpacing(true);
 		
 		setWidth("100%");
 		
 		String image = "cooltext528009102.png";
 		image = "cp_logo_metallic.png";
 		Embedded em = new Embedded("", new ThemeResource("images/" + image));
 		em.setHeight("55px");
 		addComponent(em);
 		setComponentAlignment(em, Alignment.MIDDLE_RIGHT);
 		setExpandRatio(em, 1);
 		
 		setStyleName("toolbar");
 	}
 
 	public void buttonClick(ClickEvent event) {
 		final Button source = event.getButton();
 		
 		CentralpagesApplication app = CentralpagesApplication.getInstance();
 		
 		switchingButton.setStyleName("toolbar-deselected");
 		replicationButton.setStyleName("toolbar-deselected");
 		switchingErrorsButton.setStyleName("toolbar-deselected");
 		
 		source.setStyleName("toolbar-selected");
 		
 		if (source == switchingButton) {
 			app.setNavigationTree(new SwitchingNavigationTree());
 		} else if (source == replicationButton) {
 			app.setNavigationTree(new ReplicationNavigationTree());
 		} else if (source == switchingErrorsButton) {
 			app.setNavigationTree(new SwitchingErrorNavigationTree());
 		}
 	}
 }
