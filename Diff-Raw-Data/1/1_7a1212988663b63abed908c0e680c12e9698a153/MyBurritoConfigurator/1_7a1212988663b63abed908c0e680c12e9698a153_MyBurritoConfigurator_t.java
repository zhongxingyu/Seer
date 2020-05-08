 package texmex.dinner;
 
 import texmex.dinner.models.TestModel;
 import burrito.BroadcastSettings;
 
 public class MyBurritoConfigurator extends burrito.Configurator {
 

 	@Override
 	protected BroadcastSettings configureBroadcastSettings() {
 		return new BroadcastSettings("secret-used-on-server-when-broadcasting");
 	}
 
 	@Override
 	protected String configureSiteIdentifier() {
 		return "blandat";
 	}
 
 	@Override
 	protected void init() {
 		addCrudable(TestModel.class);
 		
 		addLinkable(TestModel.class);
 	}
 
 }
