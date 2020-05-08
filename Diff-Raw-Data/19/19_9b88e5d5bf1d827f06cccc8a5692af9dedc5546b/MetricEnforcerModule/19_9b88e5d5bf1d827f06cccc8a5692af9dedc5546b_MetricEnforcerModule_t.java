 package modules;
 
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.measure.converter.UnitConverter;
 import javax.measure.unit.NonSI;
 import javax.measure.unit.SI;
 import javax.measure.unit.Unit;
 
 import org.pircbotx.hooks.events.MessageEvent;
 
 import backend.Bot;
 import backend.Util;
 
 public class MetricEnforcerModule extends Module
 {
 	public static final int MAX_NUMBER_LENGTH = 10;
 
	HashMap<String,Unit<?>> symbols;
 
	HashMap<Unit<?>,Unit<?>> conversions;
 	
 	String[] quirkyMessages;
 	
 	@Override
 	protected void initialize()
 	{
 		setHelpText("Start using the metric system, ya wanker.");
 		setName("MetricEnforcer");
 		
 		symbols = new HashMap<>();
 		
 		symbols.put("in", NonSI.INCH);
 		symbols.put("inch", NonSI.INCH);
 		symbols.put("inches", NonSI.INCH);
 		
 		symbols.put("ft", NonSI.FOOT);
 		symbols.put("foot", NonSI.FOOT);
 		symbols.put("feet", NonSI.FOOT);
 		
 		symbols.put("yd", NonSI.YARD);
 		symbols.put("yds", NonSI.YARD);
 		symbols.put("yard", NonSI.YARD);
 		symbols.put("yards", NonSI.YARD);
 		
 		symbols.put("f", NonSI.FAHRENHEIT);
 		symbols.put("°f", NonSI.FAHRENHEIT);
 		symbols.put("fahrenheit", NonSI.FAHRENHEIT);
 		
 		symbols.put("mi",NonSI.MILE);
 		symbols.put("mile",NonSI.MILE);
 		symbols.put("miles",NonSI.MILE);
 		
 		symbols.put("mph",NonSI.MILES_PER_HOUR);
 		
 		symbols.put("gal",NonSI.GALLON_LIQUID_US);
 		symbols.put("gallon",NonSI.GALLON_LIQUID_US);
 		symbols.put("gallons",NonSI.GALLON_LIQUID_US);
 		
 		symbols.put("oz", NonSI.OUNCE);
 		symbols.put("ounce", NonSI.OUNCE);
 		symbols.put("ounces", NonSI.OUNCE);
 		
 		symbols.put("oz_fl", NonSI.OUNCE_LIQUID_US);
 		
 		symbols.put("lb", NonSI.POUND);
 		symbols.put("lbs", NonSI.POUND);
 		symbols.put("pound", NonSI.POUND);
 		symbols.put("pounds", NonSI.POUND);
 		
 		symbols.put("ton", NonSI.TON_US);
 		symbols.put("tons", NonSI.TON_US);
 		
 		conversions = new HashMap<>();
 		
 		conversions.put(NonSI.INCH, SI.CENTIMETER);
 		conversions.put(NonSI.FOOT, SI.METER);
 		conversions.put(NonSI.YARD, SI.METER);
 		conversions.put(NonSI.FAHRENHEIT, SI.CELSIUS);
 		conversions.put(NonSI.MILE, SI.KILOMETER);
 		conversions.put(NonSI.MILES_PER_HOUR, NonSI.KILOMETERS_PER_HOUR);
 		conversions.put(NonSI.GALLON_LIQUID_US, NonSI.LITER);
 		conversions.put(NonSI.OUNCE, SI.GRAM);
 		conversions.put(NonSI.OUNCE_LIQUID_US, NonSI.LITER.divide(1000));
 		conversions.put(NonSI.POUND, SI.KILOGRAM);
 		conversions.put(NonSI.TON_US, SI.KILOGRAM);
 		
 		
 		quirkyMessages = new String[] {
 			"You must be an American.",
 			"What an idiot...",
 			"Are you kidding me?",
 			"What century do you think it is?",
 			"You're like the Hitler for math and science!",
 			"I don't even know how you can still use those units.",
 			"In the future, they use the metric system. How can you hope to time travel if you can't even use it?"
 		};
 		
 	}
 	
 	Pattern pattern = Pattern.compile("((?i)\\d*\\.?\\d+|an?)\\s*(\\w+)");
 	
 	@Override
 	public void onMessage(MessageEvent<Bot> event) throws Exception
 	{
 		super.onMessage(event);
 		String msg = event.getMessage();
 		
 		if(Util.isCommand(msg, event.getBot()))
 			return;
 			
 		Matcher matcher = pattern.matcher(msg);
 		while(matcher.find())
 		{
 			String valueStr = matcher.group(1);
 			if(valueStr.equalsIgnoreCase("a") || valueStr.equalsIgnoreCase("an"))
 				valueStr = "1.0";
 			String fromStr = matcher.group(2).toLowerCase();
 			if(symbols.containsKey(fromStr))
 			{
 				double value = Double.parseDouble(valueStr);
				Unit<?> from = symbols.get(fromStr);
 				
				Unit<?> to = conversions.get(from);
 				UnitConverter converter = from.getConverterTo(to);
 				double result = converter.convert(value);
 				
 				passMessage(event.getBot(), event.getChannel(), event.getUser(), 
 							getMessage(value, from.toString(), result, to.toString()));
 			}
 		}
 	}
 	
 	public String getMessage(double original, String originalUnit, double converted, String convertedUnit)
 	{
 		return quirkyMessages[(int)(quirkyMessages.length * Math.random())] + 
 						" Try "+String.format("%g",converted)+" "+convertedUnit+" instead of "+String.format("%G",original)+" "+originalUnit;
 	}
 	
 }
