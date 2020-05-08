 package org.meta_environment.eclipse.actions;
 
 import org.meta_environment.eclipse.Tool;
 
 public class LanguageActionsTool extends Tool {
 	private static class InstanceKeeper{
 		private static LanguageActionsTool sInstance = new LanguageActionsTool();
 		static{
 			sInstance.connect();
 		}
 	}
 	
 	private LanguageActionsTool() {
 		super("language-actions");
 	}
 	
 	public static LanguageActionsTool getInstance(){
 		return InstanceKeeper.sInstance;
 	}
 	
 	public void PerformAction (String Action, String language, String Filename) {
 		this.sendEvent(factory.make("perform-action(<str>,<str>,<str>)", Action, language, Filename));
 	}
	
 }
