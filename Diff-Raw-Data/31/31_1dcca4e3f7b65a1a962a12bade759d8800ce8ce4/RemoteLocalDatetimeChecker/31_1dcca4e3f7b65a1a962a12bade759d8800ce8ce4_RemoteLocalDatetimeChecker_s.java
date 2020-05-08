 //
 // RemoteLocalDatetimeChecker.java
 // darkcircle dot 0426 at gmail dot com
 //
 // This source can be distributed under the terms of GNU General Public License version 3
 // which is derived from the license of Manalith bot.
 
 package org.manalith.ircbot.plugin.CER;
 
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 
 import org.manalith.ircbot.plugin.CER.Exceptions.EmptyTokenStreamException;
 
 
 public class RemoteLocalDatetimeChecker {
 	
 	private String RemoteAddress;
 	private String LocalFilename;
 	
 	public RemoteLocalDatetimeChecker ()
 	{
 		this.setRemoteAddress ( "" );
 		this.setLocalFilename( "" );
 	}
 	public RemoteLocalDatetimeChecker ( String newRemoteAddress, String newLocalFilename )
 	{
 		this.setRemoteAddress ( newRemoteAddress );
 		this.setLocalFilename ( newLocalFilename );
 	}
 	
 	public void setRemoteAddress ( String newRemoteAddress )
 	{
 		this.RemoteAddress = newRemoteAddress;
 	}
 	public String getRemoteAddress ( )
 	{
 		return this.RemoteAddress;
 	}
 	
 	public void setLocalFilename ( String newLocalFilename )
 	{
 		this.LocalFilename = newLocalFilename;
 	}
 	public String getLocalFilename ( )
 	{
 		return this.LocalFilename;
 	}
 	
 	public DateTimeRound checkLatestUpdatedLocalDateandTime() throws IOException
 	{
 		DateTimeRound result = new DateTimeRound();
 		PropertyManager prop = new PropertyManager( this.getLocalFilename() );
 		
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
 	public DateTimeRound checkLatestNoticeDateandTime() throws FileNotFoundException, IOException, EmptyTokenStreamException
 	{
 		DateTimeRound result = new DateTimeRound();
 		
 		String filename = "org/manalith/ircbot/plugin/CER/data/dateandtime.html";
 		Downloader d = new Downloader ();
 		d.setUrl (this.getRemoteAddress() );
 		d.setLocalFilename(filename);
 		d.downloadDataFile();
 		
 		StreamFileReader fr = new StreamFileReader(filename);
 		
 		String data = fr.bringUpStreamFromFile();
 		TokenArray tArray = new TokenArray();
 		NoticeDTTokenAnalyzer tokenAnalyzer = new NoticeDTTokenAnalyzer();
 			
 		tokenAnalyzer.setTokenStringData( data );
 		tArray = tokenAnalyzer.analysisTokenStream();
 			
 		// System.out.println ( tArray );
 		int sizeOftArray = tArray.getSize();
 		TokenUnit t = null;
 		String str = "";
 		for ( int i = 0 ; i < sizeOftArray ; i++ )
 		{
 			t = tArray.getElement(i);
 			if ( t.getTokenSubtype() == TokenSubtype.SpanOpen )
 			{
 				str = t.getTokenString();
 				str = str.substring(1, str.length() - 1);
 				String [] strArray = str.split("\\s");
 				String [] keyval = strArray[1].split("\\=");
 				String valueString = keyval[1].substring(1, keyval[1].length() - 1);
 				
 				if ( valueString.equals("date") )
 				{
 					t = tArray.getElement(++i);
 					str = t.getTokenString();
 					
 					String [] datetime = str.split("\\s");
 					String [] date = datetime[0].split("\\.");
 					String [] time = datetime[1].split("\\:");
 					
 					int year = Integer.parseInt(date[0]);
 					int month = Integer.parseInt(date[1]);
 					int day = Integer.parseInt(date[2]);
 					
 					int hour = Integer.parseInt(time[0]);
 					int minute = Integer.parseInt(time[1]);
 					
					GregorianCalendar tCalendar = new GregorianCalendar ( year, month, day, hour, minute );
 					tCalendar.set(GregorianCalendar.ZONE_OFFSET, 32400000);
 					tCalendar.set(GregorianCalendar.ERA, 1);
 					tCalendar.set(GregorianCalendar.DST_OFFSET, 0);
 					tCalendar.set(GregorianCalendar.MILLISECOND, 1);
 					
 					result.setCalendar(tCalendar);
 				}
 				else if ( valueString.equals("round") )
 				{
 					while ( true )
 					{
 						t = tArray.getElement(++i);
 						if ( t.getTokenSubtype() == TokenSubtype.EMOpen ) break;
 					}
 					t = tArray.getElement(++i);
 					result.setRoundVal(Integer.parseInt(t.getTokenString()));
 					break;
 				}
 				else 
 				{
 					continue;
 				}
 			}
 		}
 
 		return result;
 	}
 	
 }
