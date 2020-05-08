 package com.brightinteractive.assetbank.restapi.representations;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 
 
 @XmlRootElement(name="displayAttributeGroup")
 public class DisplayAttributeGroupRepr 
 {
 	public URL url;
	
	@XmlElementWrapper
    @XmlElement(name = "attribute")
    public List<URL> attributes;
 	public String name = null;
 
 	public DisplayAttributeGroupRepr() throws MalformedURLException 
     {
     	url = new URL("http://example.com");
     }
 
 	public DisplayAttributeGroupRepr(URL a_url,
					List<URL> a_attributes,
 					String a_name) throws MalformedURLException 
 	{
 		url = a_url;
		attributes = a_attributes;
 		name = a_name;
 	}
 }
