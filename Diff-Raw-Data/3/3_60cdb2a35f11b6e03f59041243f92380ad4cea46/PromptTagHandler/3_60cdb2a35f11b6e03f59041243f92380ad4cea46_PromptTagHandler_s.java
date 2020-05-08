 /*
  * Created on Jan 15, 2005
  */
 package cc.warlock.stormfront.internal;
 
 import java.util.Map;
 
 import cc.warlock.client.stormfront.IStormFrontClient;
 import cc.warlock.stormfront.IStormFrontProtocolHandler;
 
 
 /**
  * @author Sean Proctor
  */
 public class PromptTagHandler extends DefaultTagHandler {
 	
 	protected int currentTime = 0;
 	protected RoundtimeTagHandler roundtimeHandler;
 	protected IStormFrontClient client;
 	protected String prompt;
 	
 	public PromptTagHandler (IStormFrontProtocolHandler handler, RoundtimeTagHandler roundtimeHandler) {
 		super(handler);
 		this.roundtimeHandler = roundtimeHandler;
 		client = handler.getClient();
 	}
 	
 	public String[] getTagNames() {
 		return new String[] { "prompt" };
 	}
 	
 	public void handleStart(Map<String,String> attributes) {
 		//System.out.println("got prompt");
 		prompt = "";
 		
 		if (attributes.get("time") != null)
 		{
 			currentTime = Integer.parseInt(attributes.get("time"));
 			if (roundtimeHandler.isWaitingForPrompt()) {
 				roundtimeHandler.processFollowingPrompt(currentTime);
 			}
 		}
 	}
 	
 	public boolean handleCharacters(char[] ch, int start, int length) {
 		prompt += String.copyValueOf(ch, start, length);
 		return true;
 	}
 	
 	@Override
 	public void handleEnd() {
		if (prompt != null)
			client.getDefaultStream().prompt(prompt);
 	}
 }
