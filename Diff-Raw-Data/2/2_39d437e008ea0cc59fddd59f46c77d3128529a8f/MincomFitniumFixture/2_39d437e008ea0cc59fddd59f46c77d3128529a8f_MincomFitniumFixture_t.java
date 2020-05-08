 package com.mincom.fitnium;
 
 import com.mincom.ellipse.rc.apiv2.Application;
 
 public class MincomFitniumFixture extends BaseMincomFitniumFixture {
 	
 	private Application application;
 	
 	public MincomFitniumFixture() {
 		super();
 	}
 	
 	public void loadApplication(String app) {
 		this.application = mfuiv2.loadApp(app).waitForReady();
 	}
 
 	public void setWidgetWithValue(String id, String value) {
 		this.application = application.setWidgetValue(id, value);
 	}
 
 	public String getWidgetWithValue(String widgetName) {
 		return application.getWidget(widgetName).getValue();
 	}
 	public void selectTheTab(String tabname) {
 		this.application = application.selectTab(tabname).waitForReady();
 	}
 	public void callAnAction(String action ) {
 		this.application = application.toolbarAction(action);
 	}
 	
 	public void waitForLoadedApp() {
 		this.application = application.waitForLoadedApplication();
 	}
 
 	public void confirmAction(String action) {
		this.application = application.dialogButton(action).waitForReady();
 	}
 	
 	public boolean assertWidgetEditable(String widget) {
 		return application.getWidget(widget).isEditable();
 	}
 	
     public void captureScreenToFile(String filename) {
 //        FitniumScreenCaptureAPI.captureScreenToFile(this, filename);
         mfuiv2.captureScreenshot(filename);
     }
 
 }
