 package org.gemsjax.client.experiment.view;
 
 import org.gemsjax.client.admin.UserLanguage;
 import org.gemsjax.client.admin.tabs.LoadingTab;
 import org.gemsjax.client.admin.widgets.Title;
 
 import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 public class ExperimentDescriptionTab extends LoadingTab {
 	
 	public ExperimentDescriptionTab(UserLanguage language, String title, String description){
 		super(title, language);
 		this.setTitle(title);
 		
 		VLayout contentLayout = new VLayout();
 		contentLayout.setWidth100();
 		contentLayout.setHeight100();
 		
 		
 		Title t = new Title("Description");
 		t.setWidth100();
 		
		Label label = new Label("<p align=\"justify\">"+description+"</p>");
 		label.setHeight("*");
 		label.setWidth100();
 		label.setWrap(true);
 		label.setStyleName("ExperimentDescription");
		label.setValign(VerticalAlignment.TOP);
 		
 		
 		label.setOverflow(Overflow.SCROLL);
 		
 		contentLayout.addMember(t);
 		contentLayout.addMember(label);
 		
 		
 		this.setContent(contentLayout);
 		this.showContent();
 		this.setCanClose(false);
 	}
 
 }
