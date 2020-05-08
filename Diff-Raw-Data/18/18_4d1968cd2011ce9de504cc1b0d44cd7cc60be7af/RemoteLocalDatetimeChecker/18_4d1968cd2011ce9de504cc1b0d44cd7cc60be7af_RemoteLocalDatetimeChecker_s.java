 package org.manalith.ircbot.plugin.newCER;
 
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 
 import org.manalith.ircbot.plugin.newCER.Exceptions.FileDoesntSpecifiedException;
 
 public class RemoteLocalDatetimeChecker {
 	
 	private String LocalPath;
 	private String LocalFilename;
 	
 	
 	public RemoteLocalDatetimeChecker ()
 	{
 		this.setLocalPath ( "" );
 		this.setLocalFilename( "" );
 	}
 	public RemoteLocalDatetimeChecker ( String newLocalFilename )
 	{
 		this.setLocalPath ( "" );
 		this.setLocalFilename ( newLocalFilename );
 	}
 	public RemoteLocalDatetimeChecker ( String newLocalPath , String newLocalFilename )
 	{
 		this.setLocalPath(newLocalPath);
 		this.setLocalFilename(newLocalFilename);
 	}
 	
 	public void setLocalPath ( String newLocalPath )
 	{
 		this.LocalPath = newLocalPath;
 	}
 	public String getLocalPath ( )
 	{
 		return this.LocalPath;
 	}
 	public void setLocalFilename ( String newLocalFilename )
 	{
 		this.LocalFilename = newLocalFilename;
 	}
 	public String getLocalFilename ( )
 	{
 		return this.LocalFilename;
 	}
 	
 	public DateTimeRound checkLatestUpdatedLocalDateandTime() throws IOException, FileDoesntSpecifiedException
 	{
 		DateTimeRound result = new DateTimeRound();
 		
 		if ( this.getLocalFilename().equals("") ) throw new FileDoesntSpecifiedException();
 		
 		PropertyManager prop = new PropertyManager( this.getLocalPath(), this.getLocalFilename() );
 		
 		try
 		{
 			prop.loadProperties();
 		}
 		catch ( IOException e )
 		{
 			prop.setProp(new Properties());
 			prop.setValue("date", "");
 			prop.setValue("round", "0" );
 			
 			prop.storeProperties();
 		}
 		
 		if ( prop.getValue("date") == null || prop.getValue("date").equals("") )
 		{
 			GregorianCalendar tCalendar = new GregorianCalendar();
 			tCalendar.setTime(new Date (1L) );
 			result.setCalendar(tCalendar);
 		}
 		else 
 		{
 			String [] dateandtime = ( (String)prop.getValue("date") ).split("\\s");
 		
 			String [] YYMMDD = dateandtime[0].split("\\.");
 			String [] hhmm = dateandtime[1].split("\\:");
 		
 			int year = Integer.parseInt(YYMMDD[0]);
 			int month = Integer.parseInt(YYMMDD[1]);
 			int date = Integer.parseInt(YYMMDD[2]);
 			
 			int hour = Integer.parseInt(hhmm[0]);
 			int minute = Integer.parseInt(hhmm[1]);
 			
			GregorianCalendar tCalendar = new GregorianCalendar(year, month, date, hour, minute);
 			tCalendar.set(GregorianCalendar.ZONE_OFFSET, 32400000);
 			tCalendar.set(GregorianCalendar.ERA, 1);
 			tCalendar.set(GregorianCalendar.DST_OFFSET, 0);
 			tCalendar.set(GregorianCalendar.MILLISECOND, 1);
 			
 			
 			result.setCalendar(tCalendar);
 		}
 		
 		if ( prop.getValue("round") == null )
 		{
 			result.setRoundVal( 0 );
 		}
 		else
 		{
 			String temp = (String)prop.getValue("round");
 			result.setRoundVal(Integer.parseInt(temp));
 		}
 		
 		return result;
 	}
 	public DateTimeRound checkLatestNoticeDateandTime() throws FileNotFoundException, IOException //, EmptyTokenStreamException
 	{
 		DateTimeRound result = new DateTimeRound();
 	
 		String url = "http://info.finance.naver.com/marketindex/exchangeMain.nhn";
 		Element exchange_info = Jsoup.connect( url ).get().select("div.exchange_info").get(0);
 		
 
 		String dateStr =  exchange_info.select("span.date").get(0).text();
 						
 		String [] datetime = dateStr.split("\\s");
 		String [] date = datetime[0].split("\\.");
 		String [] time = datetime[1].split("\\:");
 					
 		int year = Integer.parseInt(date[0]);
 		int month = Integer.parseInt(date[1]);
 		int day = Integer.parseInt(date[2]);
 					
 		int hour = Integer.parseInt(time[0]);
 		int minute = Integer.parseInt(time[1]);
 					
 		GregorianCalendar tCalendar = new GregorianCalendar ( year, month - 1, day, hour, minute );
 		tCalendar.set(GregorianCalendar.ZONE_OFFSET, 32400000);
 		tCalendar.set(GregorianCalendar.ERA, 1);
 		tCalendar.set(GregorianCalendar.DST_OFFSET, 0);
 		tCalendar.set(GregorianCalendar.MILLISECOND, 1);
 					
 		result.setCalendar(tCalendar);
 					
 		String roundStr =  exchange_info.select("span.round").get(0).select("em").text();
 		result.setRoundVal(Integer.parseInt(roundStr));
 
 		return result;
 	}
 	
 }
