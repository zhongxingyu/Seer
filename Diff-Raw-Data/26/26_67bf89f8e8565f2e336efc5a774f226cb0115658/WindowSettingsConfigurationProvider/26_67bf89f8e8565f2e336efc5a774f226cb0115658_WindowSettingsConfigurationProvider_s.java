 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.client.settings.internal;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import cc.warlock.core.client.settings.IWindowSettings;
 import cc.warlock.core.client.settings.IWindowSettingsProvider;
 
 public class WindowSettingsConfigurationProvider extends ClientConfigurationProvider implements IWindowSettingsProvider {
 
 	protected ArrayList<IWindowSettings> windowSettings = new ArrayList<IWindowSettings>();
 	
 	public WindowSettingsConfigurationProvider() {
 		super("windows");
 		setHandleChildren(false);
 	}
 	
 	public void addWindowSettings(IWindowSettings settings) {
		windowSettings.add(settings);
 	}
 
 	public Collection<? extends IWindowSettings> getWindowSettings() {
 		return windowSettings;
 	}
 
 	public IWindowSettings getWindowSettings(String windowId) {
 		for (IWindowSettings settings : windowSettings)
 		{
 			if (settings.getId().equals(windowId)) return settings;
 		}
 		return null;
 	}
 
 	public void removeWindowSettings(IWindowSettings settings) {
 		if (windowSettings.contains(settings)) {
 			windowSettings.remove(settings);
 		}
 	}
 
 	@Override
 	protected void parseData() {
 		
 	}
 	
 	@Override
 	protected void parseChild(Element child) {
 		if (child.getName().equals("window"))
 		{
 			WindowSettings settings = (WindowSettings)getWindowSettings(child.attributeValue("id"));
 			if (settings == null){
 				settings = new WindowSettings(this);
 			}
 			
 			settings.setBackgroundColor(colorValue(child, "background"));
 			settings.setForegroundColor(colorValue(child, "foreground"));
 			settings.setFont(elementToFont(child.element("font")));
 			settings.setColumnFont(elementToFont(child.element("columnFont")));
 			settings.setId(child.attributeValue("id"));
 			
			windowSettings.add(settings);
 		}
 	}
 	
 	@Override
 	protected void saveTo(List<Element> elements) {
 		Element windows = DocumentHelper.createElement("windows");
 		
 		for (IWindowSettings settings : windowSettings)
 		{
 			Element window = windows.addElement("window");
 			window.addAttribute("id", settings.getId());
 			window.addAttribute("background", colorString(settings.getBackgroundColor()));
 			window.addAttribute("foreground", colorString(settings.getForegroundColor()));
 			
 			window.add(fontToElement(settings.getFont(), "font"));
 			window.add(fontToElement(settings.getColumnFont(), "columnFont"));
 		}
 		
 		elements.add(windows);
 	}
 }
