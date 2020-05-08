 package org.dragonfly.weatherwave;
 
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.dragonfly.wunderground.exception.DragonflySaxException;
 import org.dragonfly.wunderground.service.WUService;
 
 import com.google.wave.api.AbstractRobot;
 import com.google.wave.api.Blip;
 import com.google.wave.api.event.DocumentChangedEvent;
 import com.google.wave.api.event.WaveletParticipantsChangedEvent;
 import com.google.wave.api.event.WaveletSelfAddedEvent;
 
 @SuppressWarnings("serial")
 public class WeatherWaveRobot extends AbstractRobot
 {
	private static final String ERROR_MSG = "Oops something has gone terribly wrong! OK not really but, you might have found a bug or " +
						"the weather service is on the fritz. If the problem persists add |debug to the end of your WW " +
						"query like this:  [query|command|debug] (WW code omitted intentionally) and send me the junk in " +
						"the debug blip and I'll have our vast army of code monkeys puzzle it over (and fix it if they want " +
						"any more bananas and Mtn. Dew!)";
 	private static final String DEBUG = "debug";
 	private static final String BOT_HELP_URL = "http://weatherwaves.appsopt.com";
	private static final String INITIAL_CONTENT_MSG = "\nYou have added WeatherWaves to your wave. Insert current local weather information from the wundergound.com \nFor instructions on use check out our help page."
									+ BOT_HELP_URL + " to getinstructions";
 	// TODO: Ensure only one match at a time. Currently it could match multiples
 	// at once.
 	public static final String WW_REG_PATTERN = "@WW\\[.{4}.*\\]";// regex:
 
 	// @WW\[.....*\]
 
 	@Override
 	protected String getRobotName()
 	{
 		return "WeatherWaves Robot";
 	}
 
 	@Override
 	protected String getRobotProfilePageUrl()
 	{
 		return null;
 	}
 
 	@Override
 	protected String getRobotAvatarUrl()
 	{
 		return BOT_HELP_URL;
 	}
 
 	@Override
 	public void onWaveletSelfAdded(WaveletSelfAddedEvent event)
 	{
		Blip blip = event.getWavelet().reply(INITIAL_CONTENT_MSG);
 	}
 
 	/**
 	 * Event fires on Doc Changed event if the filter, @WW[query] or
 	 * @WW[query:serviceName] is present. Valid queries should be a zip code, a
 	 * city and state or even an airport name prefixed with a 'K' ie 'KTPA'.
 	 * This is the default behavior which returns the current weather
 	 * observation.
 	 * 
 	 * Additional serviceNames: 
 	 * <li>
 	 * 		<ul> alert | a 
 	 * 		<ul> forecast | f 
 	 * </li> 
 	 * @see com.google.wave.api.AbstractRobot#onDocumentChanged(com.google.wave.api.event.DocumentChangedEvent)
 	 */
 	@Capability(filter = WW_REG_PATTERN)
 	@Override
 	public void onDocumentChanged(DocumentChangedEvent event)
 	{
 		boolean debugBlip = false;
 		StringBuilder debug = new StringBuilder("\n WW Debug: ");
 		Blip blip = event.getBlip();
 		String text = blip.getContent();
 
 		try
 		{
 			Pattern p = Pattern.compile(WW_REG_PATTERN);
 			Matcher m = p.matcher(text);
 			int i = 0;
 
 			while (m.find())
 			{
 				String query = text.substring(m.start() + 4, m.end() - 1);
 				debug.append("\nWW_query= ").append(query);
 				String rtn = "";
 				if (query.contains("|"))
 				{
 					//check for debug
 					debugBlip = query.contains(DEBUG);
 					rtn = callWWCommand(query);
 				} else
 				{
 					rtn = new WUService().getCurrentConditionsJSON(query);
 				}
 				
 				if(rtn != null)
 				{
 					blip.all("@WW[" + query + "]").replace("\n" + rtn);
 				}
 				
 				i++;
 			}
 			debug.append("pattern ct match=").append(i);
 		} catch (Exception e)
 		{
 			debug.append("Captain we have a problem!" + e);
			blip.append(ERROR_MSG);
 		}
 		debug.append("    Weatherwaves has tried to update.");
 		if(debugBlip)
 		{
 			Blip dbgblip = event.getWavelet().reply(debug.toString());
 		}
 	}
 
 	/**
 	 * Simply attempts to take the command search request and add it to 
 	 * @param - query string formatted as query|command  (DEBUG key is stripped out here.)
 	 * @return - query results
 	 * @throws DragonflySaxException 
 	 */
 	private String callWWCommand(String query) throws DragonflySaxException
 	{
 		String rtn = null;
 		//remove debug keyword
 		query = query.contains(DEBUG)? query.substring(0,query.indexOf(DEBUG)-1):query;
 		StringTokenizer st = new StringTokenizer(query,"|");
 		if(st.countTokens() == 1)
 			rtn =  new WUService().getCurrentConditionsJSON(query);
 		else
 		{
 			String q = st.nextToken();
 			String cmd = st.nextToken();
 			if(cmd.equalsIgnoreCase("F")||cmd.equalsIgnoreCase("forecast"))
 			{
 				rtn =  new WUService().getForcastJSON(q);
 			}
 			else if(cmd.equalsIgnoreCase("A")||cmd.equalsIgnoreCase("Alert"))
 			{
 				rtn =  new WUService().getAlertsJSON(q);
 			}
 		}
 		return rtn;
 	}
 }
