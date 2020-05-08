 //
 // CERRunner.java
 // darkcircle dot 0426 at gmail dot com
 //
 // This source can be distributed under the terms of GNU General Public License version 3
 // which is derived from the license of Manalith bot.
 package org.manalith.ircbot.plugin.CER;
 
 import java.io.File;
 import java.util.GregorianCalendar;
 
 import org.manalith.ircbot.plugin.CER.Exceptions.EmptyTokenStreamException;
 
 public class CERRunner {
 	
 	private String args;
 	
 	public CERRunner ( )
 	{
 		this.setArgs ( "" );
 	}
 	public CERRunner ( String newArgs )
 	{
 		this.setArgs( newArgs );
 	}
 	
 	public void setArgs ( String newArgs )
 	{
 		this.args = newArgs;
 	}
 	private String getArgs ( )
 	{
 		return this.args;
 	}
 	
 	public String run ( )
 	{
 		String result = "";
 		String realpath = this.checkFullDataPath ( );
 		
 		result = this.checkUpdate(realpath);
 
 		
 		CERInfoProvider cip = new CERInfoProvider ( realpath, this.getArgs() );
 
 		try
 		{
 			result = cip.commandInterpreter();
 		}
 		catch ( EmptyTokenStreamException e )
 		{
 			System.out.println ("No argument specified.");
 			cip = new CERInfoProvider ( "--help" );
 			try
 			{
 				result = cip.commandInterpreter();
 			}
 			catch ( Exception ex )
 			{
 				;
 			}
 		}
 		catch ( Exception e )
 		{
 			result = e.getMessage();
 		}
 		
 		return result;
 	}
 	
 	private String checkFullDataPath ( )
 	{
 		String result = "";
 		
 		ClassLoader loader = Thread.currentThread().getContextClassLoader();
 		String path = loader.getResource("").getPath();
 
 		//System.out.println(path);
 		//*
 		result = path + "org/manalith/ircbot/plugin/CER/data/";
 		File realpath = new File ( result );
 		if ( !realpath.exists() )
 			realpath.mkdirs();
 		
 		return result;
 	}
 	private String checkUpdate ( String realpath )
 	{
 		String result = "";
 		//GregorianCalendar now = new GregorianCalendar();
 		//int thishour = now.get(GregorianCalendar.HOUR_OF_DAY);
 		//int thisday = now.get(GregorianCalendar.DAY_OF_WEEK);
 		
 		// if now is in trading time.
 		//if ( ( thisday != GregorianCalendar.SUNDAY && thisday != GregorianCalendar.SATURDAY ) && ( thishour > 9 && thishour < 18 ) )
 		//{
 			try
 			{
 				/// Update database for listing money exchange information
 				/// if time is expired.
 	
 				String propFilename = realpath + "LatestUpdatedDatetime.prop";
 
 				RemoteLocalDatetimeChecker check = new RemoteLocalDatetimeChecker(
 						"http://info.finance.naver.com/marketindex/exchangeMain.nhn",
 						propFilename );
 				
 				DateTimeRound local = check.checkLatestUpdatedLocalDateandTime();
 				DateTimeRound remote = check.checkLatestNoticeDateandTime();
 				
 				if ( remote.compareTo(local) > 0 )
 				{
 					// set properties using the value.
 					PropertyManager pm = new PropertyManager ( propFilename );
 					pm.loadProperties();
 					
 					String date = Integer.toString(remote.getCalendar().get(GregorianCalendar.YEAR));
 					date += ".";
					date += Integer.toString(remote.getCalendar().get(GregorianCalendar.MONTH) + 1);
 					date += ".";
 					date += Integer.toString(remote.getCalendar().get(GregorianCalendar.DAY_OF_MONTH));
 					date += " ";			
 					date += Integer.toString(remote.getCalendar().get(GregorianCalendar.HOUR_OF_DAY));
 					date += ":";
 					date += Integer.toString(remote.getCalendar().get(GregorianCalendar.MINUTE));
 					pm.setValue("date", date);
 					
 					String round = Integer.toString(remote.getRoundVal());
 					while ( round.length() < 3 )
 						round = "0" + round;
 					pm.setValue("round", round);
 					
 					// commit into prop file.
 					pm.storeProperties();
 					
 					// update Current Exchange Rate Table.
 					CERTableUpdater updater = new CERTableUpdater ( "http://info.finance.naver.com/marketindex/exchangeList.nhn" );
 					updater.update();
 				}
 			}
 			catch (Exception e)
 			{
 				result = e.getMessage();
 				return result;
 			}
 		//}
 		return result;
 	}
 }
