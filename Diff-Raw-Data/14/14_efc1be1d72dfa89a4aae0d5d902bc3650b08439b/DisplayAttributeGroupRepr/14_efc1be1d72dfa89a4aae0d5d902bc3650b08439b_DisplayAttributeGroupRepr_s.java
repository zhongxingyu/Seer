 package com.brightinteractive.assetbank.restapi.representations;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 
 @XmlRootElement(name="displayAttributeGroup")
 public class DisplayAttributeGroupRepr 
 {
 	public URL url;
	public List<URL> attributeUrls;
 	public String name = null;
 
 	public DisplayAttributeGroupRepr() throws MalformedURLException 
     {
     	url = new URL("http://example.com");
     }
 
 	public DisplayAttributeGroupRepr(URL a_url,
					List<URL> a_attributeUrls,
 					String a_name) throws MalformedURLException 
 	{
 		url = a_url;
		attributeUrls = a_attributeUrls;
 		name = a_name;
 	}
 }
