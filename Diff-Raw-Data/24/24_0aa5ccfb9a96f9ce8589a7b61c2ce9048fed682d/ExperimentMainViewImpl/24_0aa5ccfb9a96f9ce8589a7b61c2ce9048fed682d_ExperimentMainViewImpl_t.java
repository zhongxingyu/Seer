 package org.gemsjax.client.experiment.view.impl;
 
 import org.gemsjax.client.admin.UserLanguage;
 import org.gemsjax.client.admin.adminui.TabEnviroment;
 import org.gemsjax.client.experiment.ExperimentUserImpl;
 import org.gemsjax.client.experiment.view.ExperimentHeader;
 import org.gemsjax.client.experiment.view.ExperimentMainView;
 
 import com.smartgwt.client.types.Side;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 public class ExperimentMainViewImpl implements ExperimentMainView{
 	
 	private VLayout uiLayout;
 	private UserLanguage language;
 	private ExperimentUserImpl user;
 	
 	private ExperimentHeader header;
 	
 	
 	public ExperimentMainViewImpl(UserLanguage language, ExperimentUserImpl user){
 		this.language = language;
 		this.user = user;
 		
 		createUI();
 	}
 	
 	
 	private void createUI()
 	{
		TabEnviroment.getInstance().setTabBarPosition(Side.TOP);
 		TabEnviroment.getInstance().setTabBarAlign(Side.LEFT);
 		TabEnviroment.getInstance().setWidth100();
 		TabEnviroment.getInstance().setHeight("*");
 		
 		
 		header = new ExperimentHeader(language, user.getExperiementName(), user.getExperimetnGroupName(), user.getStartDate(), user.getEndDate(), user.getDisplayedName());
 		
 		
 		// Set the 
 		uiLayout = new VLayout();
 		uiLayout.setWidth100();
 		uiLayout.setHeight100();
 		uiLayout.setMargin(5);
 		
 		uiLayout.addMember(header);
 		uiLayout.addMember(TabEnviroment.getInstance());
 		TabEnviroment.getInstance().setWidth100();
 		
 		uiLayout.draw();
 		uiLayout.bringToFront();
 	}
 
 
 	@Override
 	public void show() {
 		uiLayout.show();
 	}
 
 }
