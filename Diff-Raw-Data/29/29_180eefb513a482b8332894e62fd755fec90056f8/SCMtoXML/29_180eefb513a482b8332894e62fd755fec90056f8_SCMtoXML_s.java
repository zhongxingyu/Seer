 import java.io.FileWriter;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.jdom.Element;
 import org.jdom.Document;
 import org.jdom.Attribute;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 public class SCMtoXML {
 	
 	private Document xmlDoc;
 	private Element codeInfoElement;
 	private Element logInfoElement;
 	
	public SCMtoXML(String projectName, int lastVersion)
 	{
		this.createXMLDocument(projectName, lastVersion);
 	}	
 	
	private void createXMLDocument(String projectName, int lastVersion)
 	{	
 		Element rootElement = new Element("Project");
 		rootElement.setAttribute(new Attribute("name", projectName));
		rootElement.setAttribute(new Attribute("lastVersion", lastVersion+""));
 		//
 		codeInfoElement = new Element("CodeInfo");
 		logInfoElement = new Element("LogInfo");
 		rootElement.addContent(codeInfoElement);
 		rootElement.addContent(logInfoElement);
 		//
 		xmlDoc = new Document(rootElement);
 	}
 	
 	public Element addPackage(String packagePath)
 	{		
 		Element element = codeInfoElement;
 		StringTokenizer st = new StringTokenizer(packagePath, ".");
 		//System.out.println("package path: "+packagePath);
 		while(st.hasMoreTokens())
 		{
 			String token = st.nextToken();
 			//System.out.println("Token: "+token);
 			Element child = hasChildElement(element, token);
 			if(child==null)
 			{
 				Element newPackage = new Element("package");
 				newPackage.setAttribute("name", token);
 				//newPackage.setAttribute("sum_loc", "0");
 				element.addContent(newPackage);
 				element = newPackage;
 			}
 			else 
 				element = child;
 		}
 		return element;
 	}
 
 	public Element addClassToPackage(Element existingPackage, String className, String classType)
 	{
 	    if (existingPackage == null || !existingPackage.getName().equals("package"))
 	        return null;
 
       Element child = hasChildElement(existingPackage, className);
       if(child == null)
       {
         child = new Element("class");
         child.setAttribute("name", className);
         child.setAttribute("type", classType);
         child.setAttribute("maxLoc", "0");
         existingPackage.addContent(child);
       }
       return child;
 	}
 	
 	public boolean addVersionToClass(Element existingClass, long version, long loc) throws Exception
 	{
 		if (existingClass == null) {
 			System.err.println("existingClass is null");
 			return false;
 		}
 		
 		if(existingClass!=null && existingClass.getName().equals("class"))
 		{
 			Element newVersion = new Element("version");
 			newVersion.setAttribute("num", version+"");
 			newVersion.setAttribute("curr_loc", loc+"");
 			newVersion.setAttribute("changed", "1");
 			long churn = calculateCodeChurn(existingClass, version, loc);
 			newVersion.setAttribute("churn", churn+"");
 			existingClass.addContent(newVersion);
 			//Now, update class max loc
 			long maxLoc = getClassMaxLoc(existingClass);
 			if ( loc > maxLoc )
 				existingClass.setAttribute("maxLoc", loc+"");
 			return true;
 		}
 		return false;
 	}
 	
 	private long getClassMaxLoc(Element classElement)
 	{
 		long maxLoc = 0;
 		//assure we're dealing with the class element
 		if(classElement!=null && classElement.getName().equals("class"))
 		{
 			// if the class already has versions registered for it, just get the maxloc attribute.
 			//Otherwise, is the first version, then return maxLoc = 0.
 			List<Element> childrenVersions = classElement.getChildren();
 			if((childrenVersions != null) && (!childrenVersions.isEmpty()))
 				maxLoc = Long.valueOf(classElement.getAttributeValue("maxLoc")).longValue();
 		}
 		return maxLoc;
 	}
 	
 	private long calculateCodeChurn(Element existingClass, long version, long loc) throws Exception
 	{
 		List<Element> childrenVersions = existingClass.getChildren();
 		if((childrenVersions != null) && (!childrenVersions.isEmpty()))
 		{
 			//Code churn value is cumulative and is always increasing
 			
 			//Assuring that the version being added is greater than the last one 
 			Element lastVersion = childrenVersions.get(childrenVersions.size()-1);
 			long lastVersionNum = Long.valueOf(lastVersion.getAttributeValue("num")).longValue();
 			if(lastVersionNum < version)
 			{
 				long churn = Long.valueOf(lastVersion.getAttributeValue("churn")).longValue();
 				if (churn!=0)//it sounds like that this if is useless
 				{
 					long locLastVersion = Integer.valueOf(lastVersion.getAttributeValue("curr_loc")).intValue();
 					if(locLastVersion != loc) //more code churn happened. So... update churn value
 					{
 						if(locLastVersion > loc) 
 							churn += (locLastVersion - loc);
 						else
 							churn += (loc - locLastVersion);
 						
 					}
 				} 
 				return churn; 
 			}else throw new Exception("Error! Trying to add a new version lower than (or equals than) the last one.");
 		}
 		return loc; //at the first time a class changed, the code churn is assumed to be the loc value committed
 	}
 	
 	//Check whether or not a given element has a child with the given name
 	private Element hasChildElement(Element element, String name)
 	{
 		List<Element> children = element.getChildren(); 
 		for(Element e: children)
 		{
 			if (e.getAttributeValue("name").equals(name))
 				return e;
 		}
 		return null;
 	}
 	
 	public void addLog(long version, String sha, String author, String date, String msg)
 	{
 		Element newVersionLog = new Element("versionLog");
 		newVersionLog.setAttribute("num", version+"");
 		newVersionLog.setAttribute("sha", sha);
 		newVersionLog.setAttribute("author", author);
 		newVersionLog.setAttribute("date", date);
 		newVersionLog.setAttribute("msg", msg);
 		logInfoElement.addContent(newVersionLog);
 	}
 	
 //	public void generateVersionsNotChanged(long currentVersion)
 //	{
 //		Element rootElement = xmlDoc.getRootElement();
 //		List<Element> topLevelPackages = rootElement.getChildren(); 
 //		for(Element p: topLevelPackages)
 //		{
 //			List<Element> children = p.getChildren();
 //			for(Element c: children)
 //			{
 //				if (c.getName().equals("class"))
 //					generateVersionNotChangedInClass(c, currentVersion);
 //				else if (c.getName().equals("package"))
 //					generateVersionsNotChanged(c, currentVersion);
 //			}
 //		}
 //	}
 	
 //	private void generateVersionsNotChanged(Element packageElement, long currentVersion)
 //	{
 //		List<Element> children = packageElement.getChildren(); 
 //		for(Element c: children)
 //		{
 //			if (c.getName().equals("class"))
 //				generateVersionNotChangedInClass(c, currentVersion);
 //			else if (c.getName().equals("package"))
 //				generateVersionsNotChanged(c, currentVersion);
 //		}
 //	}
 	
 //	private void generateVersionNotChangedInClass(Element classElement, long version)
 //	{
 //		List<Element> versions = classElement.getChildren();
 //		for(Element v: versions)
 //		{
 //			long version_of_v = Long.valueOf(v.getAttributeValue("num")).longValue();
 //			if(version > version_of_v)
 //			{
 //				Element versionNotChanged = new Element("version");
 //				versionNotChanged.setAttribute("num", version+"");
 //				versionNotChanged.setAttribute("curr_loc", v.getAttributeValue("curr_loc"));
 //				versionNotChanged.setAttribute("changed", "0");
 //				versionNotChanged.setAttribute("churn", v.getAttributeValue("churn"));
 //				classElement.addContent(versionNotChanged);
 //			}
 //		}
 //	}
 	
 	public void writeToFile()
 	{
 		try {
 		    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
 		    //outputter.output(xmlDoc, System.out);
		    FileWriter writer = new FileWriter("SCMtoXML.xml");
 		    outputter.output(xmlDoc, writer);
 		    writer.close();
 		} catch (java.io.IOException e) {
 		    e.printStackTrace();
 		}
 	}
 
 }
