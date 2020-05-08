 package com.rathravane.drumlin.app.userAgents.devices.computers;
 
 import com.rathravane.drumlin.app.userAgents.devices.genericDevice;
 import com.rathravane.drumlin.app.userAgents.devices.screenInfo;
 import com.rathravane.drumlin.app.userAgents.devices.unknownFixedScreen;
 
 public class macintosh extends genericDevice
 {
 	public macintosh ()
 	{
		super ( new unknownFixedScreen (), false );
 	}
 
 	public macintosh ( screenInfo si )
 	{
		super ( si, false );
 	}
 
 	@Override
 	public String getName ()
 	{
 		return "Apple Macintosh";
 	}
 
 	@Override
 	public String getOsName ()
 	{
 		return "OS X";
 	}
 }
