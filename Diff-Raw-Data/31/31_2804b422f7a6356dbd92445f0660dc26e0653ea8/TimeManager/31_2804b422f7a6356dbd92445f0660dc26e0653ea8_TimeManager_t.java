 package com.jpii.navalbattle.pavo;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 import com.jpii.navalbattle.renderer.RenderConstants;
 
 public class TimeManager extends Renderable {
 	public static int DayNightTotalLengthSeconds = 120;
 	public static int NightDarkness = 175;
 	private int hour = 0;
 	private String desc = "";
 	private double currentTime = 0.0;
 	private Color cdr = new Color(0,0,0,0);
 	private Color lcd = new Color(0,0,0,0);
 	private int timeD = 0;
 	private int minute = 0;
	private int lsw, lsh;
 	public TimeManager() {
		lsw = DynamicConstants.WND_WDTH;
		lsh = DynamicConstants.WND_HGHT;
 	}
 	public void update() {
 		int le = DayNightTotalLengthSeconds;
     	int alph = 0;
     	double tofd = currentTime;
     	if (currentTime < DayNightTotalLengthSeconds)
     		currentTime += (DayNightTotalLengthSeconds/1000.0);
     	else
     		currentTime = 0;
     	int nightl = 5;
     	if (tofd > 0 && tofd < le / nightl/2)
     	{
     		double t = tofd;
     		alph = (int)(t * NightDarkness / (le / nightl/2));
     		if (alph < 0)
     			alph = 0;
     		if (alph > 255)
     			alph = 255;
     		desc = "Sunset";
     		timeD = 0;
     	}
     	else if (tofd > le / nightl/2 && tofd < le / nightl * 2)
     	{
     		alph = NightDarkness;
     		desc = "Night";
     		timeD = 1;
     	}
     	else if (tofd > le / nightl * 2 && tofd < (le / nightl * 2) + (le / nightl / 2))
     	{
     		double t = ((le / nightl * 2) + (le / nightl / 2))- tofd;
     		alph = (int)(t * NightDarkness / (le / nightl /2));
     		if (alph < 0)
     			alph = 0;
     		if (alph > 255)
     			alph = 255;
     		desc = "Sunrise";
     		timeD = 2;
     	}
     	else {
     		desc = "Day";
     		timeD = 3;
     	}
     	cdr = new Color(11,15,23,alph);
    	if (!lcd.equals(cdr) || lsw != DynamicConstants.WND_WDTH || lsh != DynamicConstants.WND_HGHT) {
     		buffer = new BufferedImage(DynamicConstants.WND_WDTH,DynamicConstants.WND_HGHT,BufferedImage.TYPE_INT_ARGB);
    		lsw = DynamicConstants.WND_WDTH;
    		lsh = DynamicConstants.WND_HGHT;
     		Graphics g = buffer.getGraphics();
     		g.setColor(cdr);
     		g.fillRect(0,0,DynamicConstants.WND_WDTH,DynamicConstants.WND_HGHT);
     		lcd = cdr;
     	}
     	double thour = (((tofd) * 24) / DayNightTotalLengthSeconds)+18.0;
     	if (thour > 24) {
     		thour = 24 - thour;
     	}
     	if (thour < 0)
     		thour = thour * -1;
     	//minute = ((int)((tofd * 1440) / DayNightTotalLengthSeconds));// - (hour * 60);
     	int d = (int)(thour);
     	minute = (int)((thour - d) * 60);
     	hour = d;
 	}
 	public String getTimeDescription() {
 		return desc;
 	}
 	public int getCurrentHour() {
 		return hour;
 	}
 	public int getCurrentMinutes() {
 		return minute;
 	}
 }
