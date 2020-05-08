 package cc.warlock.core.stormfront.serversettings.server;
 
 import java.util.ArrayList;
 
 public class MacroKey extends ServerSetting {
 
 	protected String keyString;
 	protected String action;
 	protected String key;
 	protected ArrayList<String> modifiers = new ArrayList<String>();
 	
 	public MacroKey (ServerSettings settings, String keyString, String action)
 	{
 		super(settings);
 		
		setKeyString(keyString);
 		setAction(action);
 	}
 	
 	private void parseKeyString ()
 	{
 		modifiers.clear();
 		
 		String tokens[] = keyString.split("-");
 		if (tokens.length > 1)
 		{
 			for (int i = 0; i < tokens.length - 1; i++) {
 				modifiers.add(tokens[i]);
 			}
 			key = tokens[tokens.length-1];
 		}
 		else {
 			key = keyString;
 		}
 	}
 	
 	@Override
 	protected void saveToDOM() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected String toStormfrontMarkup() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getKey() {
 		return key;
 	}
 
 	public ArrayList<String> getModifiers() {
 		return modifiers;
 	}
 
 	public String getKeyString() {
 		return keyString;
 	}
 	
 	public void setKeyString (String keyString)
 	{
 		this.keyString = keyString;
 		
 		parseKeyString();
 	}
 	
 	public String getAction ()
 	{
 		return action;
 	}
 	
 	public void setAction (String action)
 	{
 		this.action = action;
 	}
 
 }
