 /***************************************************************************
     begin........: January 2012
     copyright....: Sebastian Fedrau
     email........: lord-kefir@arcor.de
  ***************************************************************************/
 
 /***************************************************************************
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     General Public License for more details.
  ***************************************************************************/
 package accounting;
 
 import java.awt.event.*;
 import java.io.IOException;
 import accounting.data.sqlite.ConnectionPool;
 import accounting.gui.*;
 
 public final class Main extends WindowAdapter
 {
 	private Main() { }
 
 	public static void main(String[] args)
 	{
 		Main app = new Main();
 		TransactionFrame frame;
 
        // set look & feel:
         GuiUtil.setLookAndFeel();
 
         try
         {
        	// prepare home directory:
 			Configuration.prepareHomeDir();
 
	        // open main window:
 			frame = new TransactionFrame();
 			frame.addWindowListener(app);
 			frame.open();
 		}
        catch(IOException e)
        {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void windowClosing(WindowEvent event)
 	{
 		// clear database connection pool:
 		ConnectionPool.getInstance().clear();
 
        // save configuration:
 		try
 		{
 			Configuration.storeProperties(Configuration.getProperties());
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
