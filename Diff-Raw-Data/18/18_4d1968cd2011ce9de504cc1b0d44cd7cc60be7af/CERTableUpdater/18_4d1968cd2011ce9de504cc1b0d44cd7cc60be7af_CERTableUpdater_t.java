 //
 // CERTableUpdater.java
 // darkcircle dot 0426 at gmail dot com
 //
 // This source can be distributed under the terms of GNU General Public License version 3
 // which is derived from the license of Manalith bot.package org.manalith.ircbot.plugin.CER;
 package org.manalith.ircbot.plugin.newCER;
 
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.GregorianCalendar;
 
 import org.manalith.ircbot.plugin.newCER.PropertyManager;
 import org.manalith.ircbot.plugin.newCER.Exceptions.FileDoesntSpecifiedException;
 
 public class CERTableUpdater {
 	
 	private String LocalPath;
 	private String propFileName;
 	private SQLiteTableManager sqlman;
 	
 	private DateTimeRound local;
 	private DateTimeRound remote;
 	
 	public CERTableUpdater ( )
 	{
 		this.setLocalPath( "" );
 		propFileName = "LatestUpdatedDatetime.prop";
 		sqlman = null;
 		local = null;
 		remote = null;
 	}
 	public CERTableUpdater ( String newLocalPath )
 	{
 		this.setLocalPath(newLocalPath);
 		propFileName = "LatestUpdatedDatetime.prop";
 		sqlman = null;
 		local = null;
 		remote = null;
 	}
 	
 	private void setLocalPath ( String newLocalPath )
 	{
 		this.LocalPath = newLocalPath;
 	}
 	private String getLocalPath ( )
 	{
 		return this.LocalPath;
 	}
 		
 	private void initSQLiteTable ( ) throws SQLException, ClassNotFoundException
 	{
 		if ( this.getLocalPath().equals("") )
 		{
 			sqlman = new SQLiteTableManager( "currency.db" );
 		}
 		else
 		{
 			sqlman = new SQLiteTableManager( this.getLocalPath(), "currency.db" );
 		}
 	}
 	
 	public boolean checkLocalLastRoundExpired () throws FileDoesntSpecifiedException, IOException
 	{
 		RemoteLocalDatetimeChecker check = new RemoteLocalDatetimeChecker( this.getLocalPath(), propFileName );
 		local = check.checkLatestUpdatedLocalDateandTime();
 		remote = check.checkLatestNoticeDateandTime();
 		
 		return (remote.compareTo(local) > 0);
 	}
 	
 	public void updateLastRound() throws IOException 
 	{
 		// set properties using the value.
 		PropertyManager pm = new PropertyManager ( this.getLocalPath(), propFileName );
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
 	}
 	
 	public void updateDataTable () throws ClassNotFoundException, SQLException
 	{
 		this.initSQLiteTable();		
 		sqlman.insertDataToTable();
 		sqlman.close();
 	}
 	
 	public void update() throws IOException, SQLException, ClassNotFoundException, FileDoesntSpecifiedException
 	{
		boolean data = this.checkLocalLastRoundExpired();
		if ( data )
 		{
 			this.updateLastRound();
 			this.updateDataTable();
 		}
 	}
 }
