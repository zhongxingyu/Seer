 package fr.iutvalence.ubpe.ubpe2012.services;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import fr.iutvalence.ubpe.core.interfaces.DataEvent;
 import fr.iutvalence.ubpe.core.services.AbstractFileBuilderDataEventListenerService;
 import fr.iutvalence.ubpe.misc.HexUtils;
 import fr.iutvalence.ubpe.misc.NumbersUtils;
 import fr.iutvalence.ubpe.misc.TimeOfDay;
 import fr.iutvalence.ubpe.ubpe2012.UBPE2012Data;
 import fr.iutvalence.ubpe.ubpe2012.UBPE2012DataEvent;
 
 public class UBPE2012JsonProducerDataEventListenerService extends AbstractFileBuilderDataEventListenerService
 {
 	public final static String TOKEN = "EVENT";
 
 	public UBPE2012JsonProducerDataEventListenerService(File file, String charset)
 	{
		super(0,file, "json", charset, "//@@", "@@//");
 	}
 
 	public String insertDataEventText(DataEvent event, String token, boolean firstTime)
 	{
 		if (!token.equals(TOKEN))
 			return null;
 
 		UBPE2012DataEvent ubpeDataEvent = (UBPE2012DataEvent) event;
 		UBPE2012Data ubpeData = (UBPE2012Data) ubpeDataEvent.getParsedData();
 
 		try
 		{
 			if (!ubpeData.isValidData())
 				return null;
 
 		}
 		catch (NullPointerException e)
 		{
 			return null;
 		}
 
 		String readerName = "<unknown>";
 
 		try
 		{
 			readerName = (String) event.getMetadataFieldByName("metadata.reader.name").getValue();
 		}
 		catch (Exception e)
 		{
 		}
 
 		long readerTimestamp = System.currentTimeMillis();
 		try
 		{
 			readerTimestamp = (Long) event.getMetadataFieldByName("metadata.reader.timestamp").getValue();
 		}
 		catch (Exception e)
 		{
 		}
 
 		String[] frameTokens = ubpeData.getFrameTokens();
 
 		String result = "[\n";
 
 		// Inserting reader timestamp (from metadata)
 		result += readerTimestamp + ",";
 
 		// Inserting reader name (from metadata), with quotes
 		result += "\"" + readerName + "\", ";
 
 		// Inserting object name (frame field 0), with quotes
 		if (frameTokens[0].equals(""))
 			result += "null, ";
 		else
 			result += "\"" + frameTokens[0] + "\"" + ", ";
 		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
 		try
 		{
 			TimeOfDay.changeTimeofDay(calendar, frameTokens[1]);
 			result += calendar.getTimeInMillis() + ", ";
 		}
 		catch (Exception e)
 		{
 			//
 			// System.out.println("failed to parse date");
 			// return "";
 			result += "null, ";
 		}
 
 		// Inserting latitude as a string
 		if (frameTokens[2].trim().equals(""))
 			result += "null, ";
 		else
 			result += "\"" + frameTokens[2] + "\"" + ", ";
 
 		// Inserting longitude as a string
 		if (frameTokens[3].trim().equals(""))
 			result += "null, ";
 		else
 			result += "\"" + frameTokens[3] + "\"" + ", ";
 
 		// Inserting altitude without leading zeros
 		if (frameTokens[4].trim().equals(""))
 			result += "null, ";
 		else
 			result += NumbersUtils.removeLeadingZeros(frameTokens[4]) + ", ";
 
 		// Inserting checksum as a string
 		if (frameTokens[5].trim().equals(""))
 			result += "null, ";
 		else
 			result += "\"" + frameTokens[5] + "\"" + ", ";
 
 		// Inserting fix as a string
 		if (frameTokens[6].trim().equals(""))
 			result += "null, ";
 		else
 			result += "\"" + frameTokens[6] + "\"" + ", ";
 
 		// Inserting numSats without leading zeros
 		if (frameTokens[7].trim().equals(""))
 			result += "null, ";
 		else
 			result += NumbersUtils.removeLeadingZeros(frameTokens[7]) + ", ";
 
 		// Inserting speed without leading zeros
 		if (frameTokens[8].trim().equals(""))
 			result += "null, ";
 		else
 			result += NumbersUtils.removeLeadingZeros(frameTokens[8]) + ", ";
 
 		// Inserting bearing without leading zeros
 //		try
 //		{
 //			System.out.println(HexUtils.byteArrayToHexString((frameTokens[9].trim() + "+").getBytes("US-ASCII")));
 //		}
 //		catch (UnsupportedEncodingException e)
 //		{
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 		if (frameTokens[9].trim().equals(""))
 			result += "null, ";
 		else
 			result += NumbersUtils.removeLeadingZeros(frameTokens[9]) + ", ";
 
 		for (int i = 10; i < UBPE2012Data.FRAME_TOKENS_NAMES.length - 1; i++)
 		{
 			if (frameTokens[i].trim().equals(""))
 				result += "null, ";
 			else
 				result += frameTokens[i] + ", ";
 		}
 		if (frameTokens[UBPE2012Data.FRAME_TOKENS_NAMES.length - 1].equals(""))
 			result += "null";
 		else
 			result += NumbersUtils.removeLeadingZeros(frameTokens[UBPE2012Data.FRAME_TOKENS_NAMES.length - 1]);
 		result += "\n]";
 		if (!firstTime)
 			result += ",";
 		return result;
 	}
 }
