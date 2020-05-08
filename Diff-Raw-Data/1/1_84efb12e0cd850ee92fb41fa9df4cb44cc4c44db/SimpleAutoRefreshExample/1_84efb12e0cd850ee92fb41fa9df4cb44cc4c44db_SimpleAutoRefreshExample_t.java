 package wicket.contrib.dojo.examples;
 
 import wicket.Component;
 import wicket.contrib.dojo.autoupdate.DojoAutoUpdateHandler;
 import wicket.markup.html.WebPage;
 import wicket.markup.html.basic.Label;
 import wicket.model.Model;
 
 public class SimpleAutoRefreshExample extends WebPage {
 
 	Label label;
 	String display;
 	int timer;
 	
 	public SimpleAutoRefreshExample() {
 		timer = 0;
 		updateTimer();
		label  = new Label(this, "label");
 		label.add(new DojoAutoUpdateHandler(1000){
 
 			@Override
 			protected void update(Component component) {
 				updateTimer();
 				component.setModel(new Model<String>(display));
 				
 			}
 			
 		});
 	}
 	
 	public void updateTimer(){
 		display = "resfreshed " + timer + " time(s)";
 		timer++;
 	}
 	
 	
 
 }
