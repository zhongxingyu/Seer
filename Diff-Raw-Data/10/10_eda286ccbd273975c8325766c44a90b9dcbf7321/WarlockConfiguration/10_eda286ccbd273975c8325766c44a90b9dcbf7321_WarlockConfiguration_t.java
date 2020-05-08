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
 package cc.warlock.core.configuration;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 public class WarlockConfiguration {
 	protected File configFile;
 	
 	protected Document document;
 	protected Element warlockConfigElement;
 	
 	protected ArrayList<IConfigurationProvider> providers = new ArrayList<IConfigurationProvider>();
 	protected ArrayList<Element> unhandledElements = new ArrayList<Element>();
 	protected HashMap<Element, IConfigurationProvider> elementProviders = new HashMap<Element, IConfigurationProvider>();
 	
 	protected static HashMap<String, WarlockConfiguration> configurations  = new HashMap<String, WarlockConfiguration>();
 	
 	public static WarlockConfiguration getWarlockConfiguration(String configFilename)
 	{
		/*
		 * Without this, settings WILL NOT SAVE between Warlock Instances.
		 * Please test before removing again.
		 */
		if (!configurations.containsKey(configFilename))
 		{
 			WarlockConfiguration config = new WarlockConfiguration(configFilename);
 			configurations.put(configFilename, config);
 		}
		return configurations.get(configFilename);
 	}
 	
 	public static WarlockConfiguration getMainConfiguration ()
 	{
 		return getWarlockConfiguration(ConfigurationUtil.MAIN_CONFIGURATION_FILE);
 	}
 	
 	public static void saveAll ()
 	{
 		for (WarlockConfiguration config : configurations.values())
 		{
 			config.save();
 		}
 	}
 	
 	protected WarlockConfiguration (String filename)
 	{
 		configFile = ConfigurationUtil.getConfigurationFile(filename, true);
 		
 		loadXML();
 	}
 	
 	protected void loadXML ()
 	{
 		if (configFile.length() == 0)
 			return;
 		
 		try {
 			SAXReader reader = new SAXReader();
 			document = reader.read(configFile);
 			warlockConfigElement = document.getRootElement();
 			
 			for (Element element : (List<Element>)warlockConfigElement.elements())
 			{
 				unhandledElements.add(element);
 			}
 			
 		} catch (DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void addConfigurationProvider (IConfigurationProvider provider)
 	{
 		if (!providers.contains(provider))
 		{
 			providers.add(provider);
 			
 			for (Iterator<Element> iter = unhandledElements.iterator(); iter.hasNext(); )
 			{
 				Element element = iter.next();
 				if (provider.supportsElement(element))
 				{
 					provider.parseElement(element);
 					elementProviders.put(element, provider);
 					iter.remove();
 				}
 			}
 		}
 	}
 	
 	public void removeConfigurationProvider (IConfigurationProvider provider)
 	{
 		if (providers.remove(provider)) {
 			if (elementProviders.containsValue(provider)) {
 				for (Iterator<Map.Entry<Element,IConfigurationProvider>> iter = elementProviders.entrySet().iterator();
 							iter.hasNext(); ) {
 					Map.Entry<Element, IConfigurationProvider> entry = iter.next();
 					
 					if (entry.getValue().equals(provider))
 						iter.remove();
 				}
 			}
 		}
 	}
 	
 	public void save ()
 	{
 		Document document = DocumentHelper.createDocument();
 		Element warlockConfig = DocumentHelper.createElement("warlock-config");
 		
 		document.setRootElement(warlockConfig);
 		
 		
 		for (IConfigurationProvider provider : providers)
 		{
 			List<Element> elements = provider.getTopLevelElements();
 			
 			for (Element element : elements)
 			{
 				warlockConfig.add(element);
 			}
 		}
 		
 		for (Element unhandled : unhandledElements)
 		{
 			// Make sure to resave unhandled elements, just in case the corresponding handler wasn't instantiated
 			warlockConfig.add(unhandled.createCopy());
 		}
 		
 		try {
 			OutputFormat format = OutputFormat.createPrettyPrint();
 			FileOutputStream stream = new FileOutputStream(configFile);
 			XMLWriter writer = new XMLWriter(stream, format);
 			writer.write(document);
 			stream.close();
 			
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
