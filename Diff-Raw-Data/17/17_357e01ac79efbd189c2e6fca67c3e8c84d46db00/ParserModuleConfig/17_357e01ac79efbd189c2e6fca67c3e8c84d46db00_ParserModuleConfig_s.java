 package org.hazelwire.xml;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 
 import org.hazelwire.modules.Flag;
 import org.hazelwire.modules.Module;
 import org.hazelwire.modules.ModulePackage;
 import org.hazelwire.modules.Option;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 
 /**
  * This class parses the configuration files that accompany the modules. All the information is extracted from the XML configuration file and
  * with that information a module object is created that is linked to the original module files.
  * 
  * @author Tim Strijdhorst
  *
  */
 public class ParserModuleConfig extends XMLParser
 {
 	Module tempModule;
 	
 	public ParserModuleConfig(String filePath) throws FileNotFoundException
 	{
 		super(filePath);
 		tempModule = new Module();
 	}
 	
 	public ParserModuleConfig(InputStream in)
 	{
 		super(in);
 	}
 
 	@Override
 	public Object parseDocument() throws Exception
 	{
         //get the root element
         Element docElement = dom.getDocumentElement();
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 
         //Get a nodelist of names (should be just one...)
         NodeList generalNl = docElement.getElementsByTagName("general-info");
 
         if(generalNl != null && generalNl.getLength() > 0)
         {
         	//For now just get the name
         	Element el = (Element)generalNl.item(0);
         	//get the nodelist for general-info
         	
         	tempModule.setName(this.getTextValue(el, "name"));
         	tempModule.setAuthor(this.getTextValue(el, "author"));
         	tempModule.setDate(dateFormat.parse(this.getTextValue(el, "date")));
         	tempModule.setFileName(this.getTextValue(el, "file"));
        	tempModule.setDeployPath(this.getTextValue(el, "deploy"));
         	tempModule.setHidden(Boolean.valueOf(this.getTextValue(el, "hidden")));
         	
         	
         	//serviceport is a bit trickier
         	NodeList portNl = el.getElementsByTagName("serviceport");
         	
         	if(portNl != null && portNl.getLength() > 0)
         	{
     	        Element portEl = (Element)portNl.item(0);
         	
 	        	tempModule.setServicePort(Integer.valueOf(portEl.getFirstChild().getNodeValue())); //set the default port
 	        	
 	        	//serviceport is editable, add an option for this
 	        	//this is a bit of a dirty trick, but because this is done here it will always have id=0 ;)
 	        	if(Boolean.valueOf(portEl.getAttribute("editable")))
 	        	{
 	        		tempModule.addOption(new Option("Service port","integer",String.valueOf(tempModule.getServicePort())));
 	        	}
         	}
         }
         
         NodeList packageNl = docElement.getElementsByTagName("package");
         
         if(packageNl != null && packageNl.getLength() > 0)
         {
         	Element el = (Element)packageNl.item(0);
         	ModulePackage tempPackage = new ModulePackage(this.getIntValue(el, "id"),this.getTextValue(el, "name"));
         	tempModule.setModulePackage(tempPackage);
         }
         
         
         NodeList tagsNl = docElement.getElementsByTagName("tag");
         
         if(tagsNl != null && tagsNl.getLength() > 0)
         {
         	for(int i=0;i<tagsNl.getLength();i++)
         	{
         		Element el = (Element)tagsNl.item(i);
         		tempModule.addTag(el.getFirstChild().getNodeValue());
         	}
         }
         
         NodeList flagsNl = docElement.getElementsByTagName("flag");
         
         if(flagsNl != null)
         {
         	for(int i=0;i<flagsNl.getLength();i++)
         	{
         		Element el = (Element)flagsNl.item(i);
         		tempModule.addFlag(new Flag(this.getIntValue(el, "points"),
         				this.getIntValue(el, "points"))); //this might throw an exception
         	}        	
         }
         
         NodeList optionNl = docElement.getElementsByTagName("option");
         
         if(optionNl != null)
         {
         	for(int i=0;i<optionNl.getLength();i++)
         	{
         		Element el = (Element)optionNl.item(i);        		
         		
         		String name = this.getTextValue(el, "name");
         		String value = getTextValue(el, "value");
         		
         		NodeList valueNl = el.getElementsByTagName("value");
         		el = (Element)valueNl.item(0);
         		String type = el.getAttribute("type");
         		
         		
         		Option option = new Option(name,type,value);
         		
         		tempModule.addOption(option);
         	}
         }
         
         return tempModule;
 	}
 	
 	public Module getModule()
 	{
 		return tempModule;
 	}
 }
