 package cc.warlock.core.stormfront.client.internal;
 
import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.internal.Stream;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.serversettings.server.IgnoreSetting;
 
 public class StormFrontStream extends Stream {

	protected IStormFrontClient client;
 	
 	protected StormFrontStream (IStormFrontClient client, String streamName)
 	{
 		super(client, streamName);
 	}
 	
 	protected static Stream fromNameAndClient (IStormFrontClient client, String name)
 	{
 		if (streams.containsKey(name))
 			return streams.get(name);
 		
 		else return new StormFrontStream(client, name);
 	}
 	
 	@Override
	public IWarlockClient getClient() {
		return client;
	}
	
	@Override
 	public void send(WarlockString text) {
 		if (client != null)
 		{
			for (IgnoreSetting ignore : client.getServerSettings().getIgnores())
 			{
 				String ignoreText = ignore.getText();
				int pos = text.toString().indexOf(ignoreText);
				while(pos >= 0)
 				{
 					System.out.println("Ignore matched text in: " + text.toString());
 					
 					String str = text.toString();
 					int start = str.substring(0, pos).lastIndexOf('\n');
 					int end = str.indexOf('\n', pos);
 					
 					// we are the first line and don't have anything to remove there
 					if(start < 0) {
 						// we only have one line, don't show anything
 						if(end < 0 || end + 1 >= str.length())
 							return;
 						
 						text = text.substring(end + 1);
 					} else {
 						WarlockString newText = text.substring(0, start + 1);
 						
 						// if we have text after the line after the ignore, add it
 						if(end >= 0 && end + 1 < str.length())
 							newText.append(text.substring(end + 1));
 
 						text = newText;
 					}
 				}
 			}
 		}
 		
 		super.send(text);
 	}
 }
