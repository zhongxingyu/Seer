 package com.webadmin.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.TabItemConfig;
 import com.sencha.gxt.widget.core.client.TabPanel;
 import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.info.Info;
 import com.webadmin.client.mainForm.ArmyPage;
 import com.webadmin.client.mainForm.views.*;
 import com.webadmin.client.services.CommonService;
 import com.webadmin.client.services.CommonServiceAsync;
 
 /**
  * Entry point classes define
  * <code>onModuleLoad()</code>.
  */
 public class admin implements EntryPoint {
 
 	interface MyUiBinder extends UiBinder<Widget, admin> {
 	}
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 	private final CommonServiceAsync commonService = GWT.create(CommonService.class);
 
     @UiField
     HorizontalLayoutContainer attackTyteGridContainer;
     @UiField
     HorizontalLayoutContainer specialRuleGridContainer;
     @UiField
     HorizontalLayoutContainer unitTypeGridContainer;
     @UiField
     HorizontalLayoutContainer weaponTypeGridContainer;
     @UiField
     HorizontalLayoutContainer unitBaseGridContainer;
     @UiField
     HorizontalLayoutContainer armyContainer;
     @UiField
     HorizontalLayoutContainer codexAndFractionsGridContainer;
 
     public Widget asWidget() {
         Widget d=uiBinder.createAndBindUi(this);
         attackTyteGridContainer.add(new AttackTypeContainer());
         specialRuleGridContainer.add(new SpecialRuleContainer());
         unitTypeGridContainer.add(new UnitTypeContainer());
         weaponTypeGridContainer.add(new WeaponTypeContainer());
         unitBaseGridContainer.add(new UnitBaseContainer());
         armyContainer.add(new ArmyPage());
         VerticalLayoutContainer vc = new VerticalLayoutContainer();
         vc.add(new CodexContainer());
      //  vc.add(new FractionContainer());
         codexAndFractionsGridContainer.add(vc);
         return d;
 	}
 
     /**
      * This is the entry point method.
      */
 	public void onModuleLoad() {
         RootPanel.get().add(asWidget());
 	}
 
 	@UiHandler(value = {"folder"})
 	void onSelection(SelectionEvent<Widget> event) {
 		TabPanel panel = (TabPanel) event.getSource();
 		Widget w = event.getSelectedItem();
 		TabItemConfig config = panel.getConfig(w);
 		Info.display("Message", "'" + config.getText() + "' Selected");
 	}
 }
