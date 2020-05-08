 package cc.warlock.core.stormfront.serversettings.server;
 
 import cc.warlock.core.stormfront.client.StormFrontColor;
 import cc.warlock.core.stormfront.xml.StormFrontElement;
 
 public abstract class ColorSetting extends ServerSetting implements Comparable<ColorSetting> {
 
 	protected String foregroundColor, backgroundColor;
 	protected Palette palette;
 	protected String foregroundKey = KEY_FGCOLOR;
 	protected boolean needsUpdate;
 	
 	public static final String KEY_FGCOLOR = "color";
 	public static final String KEY_BGCOLOR = "bgcolor";
 	
 	public ColorSetting (ServerSettings serverSettings, Palette palette)
 	{
 		super(serverSettings);
 		
 		this.palette = palette;
 	}
 	
 	public ColorSetting (ServerSettings settings, StormFrontElement element, Palette palette)
 	{
 		super(settings, element);
 		
 		this.palette = palette;
 	}
 	
 	public abstract String getId ();
 	
 	public StormFrontColor getDefaultForegroundColor ()
 	{
		return serverSettings.getDefaultSkin().getDefaultWindowForeground();
 	}
 	
 	public StormFrontColor getDefaultBackgroundColor ()
 	{
		return serverSettings.getDefaultSkin().getDefaultWindowBackground();
 	}
 	
 	public StormFrontColor getForegroundColor() {
 		return getForegroundColor(true);
 	}
 	
 	public StormFrontColor getForegroundColor (boolean skinFallback)
 	{
 		return getColorFromString(foregroundKey, foregroundColor, skinFallback);
 	}
 
 	public StormFrontColor getBackgroundColor() {
 		return getBackgroundColor(true);
 	}
 	
 	public StormFrontColor getBackgroundColor (boolean skinFallback)
 	{
 		return getColorFromString(KEY_BGCOLOR, backgroundColor, skinFallback);
 	}
 	
 	protected StormFrontColor getColorFromString (String key, String color, boolean skinFallback)
 	{
 		if (color == null || color.length() == 0)
 		{
 			if (KEY_FGCOLOR.equals(key) || foregroundKey.equals(key))
 				return getDefaultForegroundColor();
 			else if (KEY_BGCOLOR.equals(key))
 				return getDefaultBackgroundColor();
 			else return StormFrontColor.DEFAULT_COLOR;
 		}
 		else if (color.charAt(0) == '@')
 		{
 			StormFrontColor paletteColor = palette.getPaletteColor(color.substring(1));
 			paletteColor.addPaletteReference(this);
 			
 			return paletteColor;
 		}
 		else if ("skin".equals(color))
 		{
 			if (!skinFallback)
 				return StormFrontColor.DEFAULT_COLOR;
 			
 			if (KEY_FGCOLOR.equals(key) || foregroundKey.equals(key))
 				return serverSettings.getDefaultSkin().getSkinForegroundColor(this);
 			else if (KEY_BGCOLOR.equals(key))
 				return serverSettings.getDefaultSkin().getSkinBackgroundColor(this);
 		}
 		
 		return new StormFrontColor(color);
 	}
 
 	protected String assignColor (StormFrontColor color, String currentColor)
 	{
 		color.assignToPalette(palette);
 		String stormfrontString = color.toStormfrontString();
 		
 		if (!stormfrontString.equals(currentColor))
 			needsUpdate = true;
 		
 		return stormfrontString;
 	}
 	
 	public Palette getPalette() {
 		return palette;
 	}
 
 	public void setPalette(Palette palette) {
 		this.palette = palette;
 	}
 
 	public void setForegroundColor(StormFrontColor foregroundColor) {
 		this.foregroundColor = assignColor(foregroundColor, this.foregroundColor);
 	}
 	
 	public void setBackgroundColor(StormFrontColor backgroundColor) {
 		this.backgroundColor = assignColor(backgroundColor, this.backgroundColor);
 	}
 	
 	public void setDefaultForegroundColor ()
 	{
 		this.foregroundColor = null;
 	}
 	
 	public void setSkinForegroundColor ()
 	{
 		this.foregroundColor = "skin";
 	}
 	
 	public void setDefaultBackgroundColor ()
 	{
 		if (this.foregroundColor != null)
 			needsUpdate = true;
 		this.foregroundColor = null;
 	}
 	
 	public void setSkinBackgroundColor ()
 	{
 		if (!"skin".equals(this.backgroundColor))
 			needsUpdate = true;
 			
 		this.backgroundColor = "skin";
 	}
 
 	public boolean needsUpdate() {
 		return needsUpdate;
 	}
 	
 	public void setNeedsUpdate (boolean needsUpdate)
 	{
 		this.needsUpdate = needsUpdate;
 	}
 	
 	public int compareTo(ColorSetting o) {
 		return foregroundColor.compareTo(o.foregroundColor);
 	}
 }
