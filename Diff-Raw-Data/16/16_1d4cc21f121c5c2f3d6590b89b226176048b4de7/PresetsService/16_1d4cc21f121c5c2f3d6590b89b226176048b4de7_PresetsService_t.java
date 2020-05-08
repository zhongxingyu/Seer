 package com.senselessweb.cradionative.radio.library;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 public class PresetsService
 {
 
 	private static final String presetsXmlLocation = "http://v-serv.dyndns.org/remote-radio/presets.xml"; 
 	
 	private final List<Item> presets;
 	
 	public PresetsService() 
 	{
 		try
 		{
 			this.presets = new ArrayList<Item>();
 			final XPath xpath = XPathFactory.newInstance().newXPath();
 			final InputSource inputSource = new InputSource(
 					new URL(presetsXmlLocation).openConnection().getInputStream());
 			
 			final NodeList presets = (NodeList) xpath.evaluate("//preset", inputSource, XPathConstants.NODESET);
 			for (int i = 0; i < presets.getLength(); i++)
 			{
 				final Node preset = presets.item(i);
 				this.presets.add(new Item(
 						preset.getAttributes().getNamedItem("name").getNodeValue(),
 						preset.getAttributes().getNamedItem("src").getNodeValue(),
 						Boolean.valueOf(preset.getAttributes().getNamedItem("isPlaylist").getNodeValue())));
 			}
 		}
 		catch (final Exception e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public Item getPreset(final int preset)
 	{
		return preset <= this.presets.size() ? this.presets.get(preset - 1) : null;
 	}	
 	
 }
