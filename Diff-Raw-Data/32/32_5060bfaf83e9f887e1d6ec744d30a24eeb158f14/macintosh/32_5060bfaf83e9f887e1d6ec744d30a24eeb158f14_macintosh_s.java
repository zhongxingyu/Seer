 package com.rathravane.drumlin.app.userAgents.devices.computers;
 
 import com.rathravane.drumlin.app.userAgents.devices.genericDevice;
 import com.rathravane.drumlin.app.userAgents.devices.screenInfo;
 import com.rathravane.drumlin.app.userAgents.devices.unknownFixedScreen;
 
 public class macintosh extends genericDevice
 {
 	public macintosh ()
 	{
		super ( new unknownFixedScreen (), true );
 	}
 
 	public macintosh ( screenInfo si )
 	{
		super ( si, true );
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
