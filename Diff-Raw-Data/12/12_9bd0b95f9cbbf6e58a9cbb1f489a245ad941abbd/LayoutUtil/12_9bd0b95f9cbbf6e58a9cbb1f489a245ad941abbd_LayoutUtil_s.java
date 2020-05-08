 /**
  * 
  */
 package org.savara.sam.web.client.view;
 
 import org.savara.sam.web.client.NameTokens;
 
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.types.VisibilityMode;
 import com.smartgwt.client.widgets.HTMLFlow;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.layout.SectionStack;
 import com.smartgwt.client.widgets.layout.SectionStackSection;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 /**
  * 
  * @author Jeff Yu
  * @date Nov 18, 2011
  */
 public class LayoutUtil {
 	
 	public static VLayout getPagePanel() {
 		VLayout panel = new VLayout();
 		panel.setWidth("1180");
 		panel.setPadding(0);
 		panel.setBorder("1px solid black");
 		panel.setAlign(Alignment.CENTER);
 		panel.setAlign(VerticalAlignment.TOP);
 		return panel;
 	}
 	
 	public static Label getHeaderLayout() {
 		Label headerLabel = new Label();
 		headerLabel.setMargin(0);
 		headerLabel.setSize("100%", "100");
 		headerLabel.setAlign(Alignment.LEFT);
 		headerLabel.setStyleName("headerLabel");
 		return headerLabel;
 	}
 	
 	public static Label getFooterLayout() {
 		Label footerLabel = new Label();
 		footerLabel.setContents("Savara SAM :: Footer ");
 		footerLabel.setSize("100%", "30");
 		footerLabel.setAlign(Alignment.CENTER);
 		footerLabel.setBorder("1px solid #808080");
 		return footerLabel;
 	}
 	
 	
 	public static SectionStack getMenuStack() {
 		final SectionStack  linkStack = new SectionStack();
 		linkStack.setVisibilityMode(VisibilityMode.MUTEX);
 		linkStack.setCanResizeSections(false);
 		linkStack.setWidth(180);
 		linkStack.setHeight(200);
 		linkStack.setMargin(5);
 		
 		SectionStackSection dashboard = new SectionStackSection("Menus");
 		dashboard.setCanCollapse(true);
 		dashboard.setExpanded(true);	
 		
 		linkStack.addSection(dashboard);
 		
 		VerticalPanel links = new VerticalPanel();
		Hyperlink chartLink = new Hyperlink("AQ and Charts", NameTokens.MAIN_VIEW);
		Hyperlink notificationList = new Hyperlink("Notifications", NameTokens.SITUATION_VIEW);
 		links.add(chartLink);
 		links.add(notificationList);
 		links.setSpacing(5);
 		
 		VLayout linkPanel = new VLayout();
 		linkPanel.setMargin(10);
 		linkPanel.addChild(links);
 		
 		
 		dashboard.addItem(linkPanel);
 		
 		return linkStack;
 	}
 	
 	
 	public static Label getLink(String description, final String tokenName) {
 		Label link = new Label(description);
 		link.setStyleName("menu_link");
 		link.setHeight(30);
 		link.setAlign(Alignment.LEFT);
 		link.setWidth(150);
 		link.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event) {
 				
 			}});
 		return link;
 	}
 
 }
