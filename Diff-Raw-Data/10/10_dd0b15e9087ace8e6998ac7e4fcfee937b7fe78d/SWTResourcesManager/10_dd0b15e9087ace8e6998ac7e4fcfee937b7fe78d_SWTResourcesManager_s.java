 package org.pescuma.jfg.gui.swt;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 
 public class SWTResourcesManager
 {
 	private final Map<String, Image> images = new HashMap<String, Image>();
 	
 	public Image newImage(String image)
 	{
 		Image result = images.get(image);
 		if (result == null)
 		{
 			result = new Image(Display.getCurrent(), image);
 			images.put(image, result);
 		}
 		return result;
 	}
 	
 	private final Map<String, Color> colors = new HashMap<String, Color>();
 	
 	public Color newColor(int r, int g, int b)
 	{
 		String key = r + "_" + g + "_" + b;
 		Color result = colors.get(key);
 		if (result == null)
 		{
 			result = new Color(Display.getCurrent(), r, g, b);
 			colors.put(key, result);
 		}
 		return result;
 	}
 	
 	private final Map<List<FontData>, Font> fonts = new HashMap<List<FontData>, Font>();
 	
 	public Font newFont(FontData[] fontData)
 	{
 		List<FontData> key = Arrays.asList(fontData);
 		Font result = fonts.get(key);
 		if (result == null)
 		{
 			result = new Font(Display.getCurrent(), fontData);
 			fonts.put(key, result);
 		}
 		return result;
 	}
 	
 	public void disposeAll()
 	{
 		for (Image image : images.values())
 			image.dispose();
 		images.clear();
 		for (Color color : colors.values())
 			color.dispose();
 		colors.clear();
 	}
 	
 }
