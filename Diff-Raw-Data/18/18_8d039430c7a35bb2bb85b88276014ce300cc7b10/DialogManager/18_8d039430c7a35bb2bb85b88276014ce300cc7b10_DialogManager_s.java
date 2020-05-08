 package com.xaf.form;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 import javax.xml.parsers.*;
 import javax.servlet.*;
 
 import org.w3c.dom.*;
 
 import com.xaf.config.*;
 import com.xaf.form.field.*;
 import com.xaf.xml.*;
 import com.xaf.skin.*;
 import com.xaf.Metric;
 
 public class DialogManager extends XmlSource
 {
 	public static class DialogInfo
 	{
 		public Element defnElement;
 		public String pkgName;
 		public String lookupName;
 		public Dialog dialog;
 		public Class dialogClass;
 		public Class dialogContextClass;
         public Class directorClass;
 
 		public DialogInfo(ServletContext servletContext, String pkgName, Element elem)
 		{
 			this.pkgName = pkgName;
 			this.defnElement = elem;
 			this.dialog = null;
 			this.lookupName = pkgName != null ? (pkgName + "." + elem.getAttribute("name")) : elem.getAttribute("name");
 
 			boolean autoFind = false;
 			String dialogClassName = defnElement.getAttribute("class");
 			if(dialogClassName == null || dialogClassName.length() == 0)
             {
 				dialogClassName = "dialog.";
                 if(pkgName != null)
                     dialogClassName += pkgName + ".";
                 dialogClassName += com.xaf.xml.XmlSource.xmlTextToJavaIdentifier(elem.getAttribute("name"), true);
 				autoFind = true;
             }
 
             defnElement.setAttribute("qualified-name", lookupName);
             defnElement.setAttribute("package", pkgName);
 
             findDialogContextClass();
 			try
 			{
 				dialogClass = Class.forName(dialogClassName);
                 defnElement.setAttribute("_class-name", dialogClass.getName());
                 defnElement.setAttribute("_class-file-name", com.xaf.BuildConfiguration.getClassFileName(dialogClass.getName()));
 			}
 		    catch(ClassNotFoundException e)
 			{
 				if(! autoFind)
 	                defnElement.setAttribute("_class-name", e.toString());
 				dialogClass = Dialog.class;
 			}
 
             String directorClassName = elem.getAttribute("director-class");
             if(directorClassName != null && directorClassName.length() > 0)
             {
                 try
                 {
                     directorClass = Class.forName(directorClassName);
                     elem.setAttribute("_director-class-name", directorClassName);
                     elem.setAttribute("_director-class-file-name", com.xaf.BuildConfiguration.getClassFileName(directorClassName));
                 }
                 catch(Exception e)
                 {
                     directorClass = DialogDirector.class;
                     elem.setAttribute("_director-class-name", e.toString());
                 }
             }
             else
                 directorClass = DialogDirector.class;
 		}
 
         public Element getDefnElem() { return defnElement; }
         public String getPackageName() { return pkgName; }
 		public String getLookupName() { return lookupName; }
 		public Class getDialogClass() { return dialogClass; }
 		public Class getDialogContextClass() { return dialogContextClass; }
 
         public void findDialogContextClass()
         {
 			try
 			{
 	            dialogContextClass = Dialog.findDialogContextClass(pkgName, defnElement);
 				if(dialogContextClass != DialogContext.class)
 				{
 					defnElement.setAttribute("_dc-class-name", dialogContextClass.getName());
 					defnElement.setAttribute("_dc-class-file-name", com.xaf.BuildConfiguration.getClassFileName(dialogContextClass.getName()));
 				}
 			}
 			catch(ClassNotFoundException e)
 			{
 				defnElement.setAttribute("_dc-class-name", e.toString());
 			}
         }
 
 		public Dialog getDialog()
 		{
 			if(dialog == null)
 			{
 				try
 				{
 					dialog = (Dialog) dialogClass.newInstance();
 				}
 				catch(Exception e)
 				{
 					dialog = new Dialog();
 				}
				dialog.importFromXml(pkgName, defnElement);
				dialog.setDialogContextClass(dialogContextClass);
                 dialog.setDialogDirectorClass(directorClass);
 			}
 			return dialog;
 		}
 
         public File generateDialogBean(String outputPath, String pkgPrefix) throws IOException
         {
             Dialog activeDialog = new Dialog();
             activeDialog.importFromXml(pkgName, defnElement);
 
             /* figure out the file name we need to create, and create the necessary paths */
             String classNamePrep = new String(lookupName);
             classNamePrep.replace('.', '/');
 
             File javaFilePath = new File(pkgName == null ? outputPath : (outputPath + "/" + pkgName));
             javaFilePath.mkdirs();
 
             File javaFile = new File(javaFilePath, com.xaf.xml.XmlSource.xmlTextToJavaIdentifier(defnElement.getAttribute("name"), true) + "Context.java");
 
             Writer writer = new java.io.FileWriter(javaFile);
             writer.write(activeDialog.getSubclassedDialogContextCode(pkgPrefix));
             writer.close();
 
             findDialogContextClass();
             return javaFile;
         }
 	}
 
 	static final String REQPARAMNAME_DIALOG = "dlg";
 	private ServletContext servletContext;
 	private Map dialogs = new Hashtable();
 
 	public DialogManager(ServletContext servletContext, File file)
 	{
 		this.servletContext = servletContext;
 		loadDocument(file);
 	}
 
 	public Map getDialogs()
 	{
 		reload();
 		return dialogs;
 	}
 
 	public Dialog getDialog(String name)
 	{
 		reload();
 
 		DialogInfo info = (DialogInfo) dialogs.get(name);
 		if(info == null)
 			return null;
 
 		return info.getDialog();
 	}
 
 	public Dialog getDialog(ServletRequest request)
 	{
 		String dialogName = request.getParameter(REQPARAMNAME_DIALOG);
 		if(dialogName == null)
 			dialogName = request.getParameter(Dialog.PARAMNAME_DIALOGQNAME);
 
 		if(dialogName == null)
 			return null;
 		else
 		{
 			Dialog result = getDialog(dialogName);
 			if(result == null)
 			{
 				result = new Dialog();
 				result.setHeading("Dialog '"+dialogName+"' not found!");
 			}
 			return result;
 		}
 	}
 
 	public void catalogNodes()
 	{
 		dialogs.clear();
 
         if(xmlDoc == null)
             return;
 
 		NodeList children = xmlDoc.getDocumentElement().getChildNodes();
 		for(int n = 0; n < children.getLength(); n++)
 		{
 			Node node = children.item(n);
 			if(node.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
             String nodeName = node.getNodeName();
 			if(nodeName.equals("dialogs"))
 			{
 				Element dialogsElem = (Element) node;
 				String stmtPkg = dialogsElem.getAttribute("package");
 
 				NodeList dialogsChildren = node.getChildNodes();
 				for(int c = 0; c < dialogsChildren.getLength(); c++)
 				{
 					Node dialogsChild = dialogsChildren.item(c);
 					if(dialogsChild.getNodeType() != Node.ELEMENT_NODE)
 						continue;
 
 					String scName = dialogsChild.getNodeName();
 	    			if(scName.equals("dialog"))
 					{
 						Element dialogElem = (Element) dialogsChild;
 						DialogInfo di = new DialogInfo(servletContext, stmtPkg, dialogElem);
 		    			dialogs.put(di.getLookupName(), di);
 					}
                     else if(scName.equals("register-field"))
                     {
                         throw new RuntimeException("The register-field tag should be specified under the root tag now (not in dialogs tag).");
                     }
 				}
 			}
 			else if(nodeName.equals("dialog-skin"))
 			{
 				Element skinElem = (Element) node;
 				DialogSkin skin = null;
 				String className = skinElem.getAttribute("class");
 				if(className.length() > 0)
 				{
 					try
 					{
 						Class skinClass = Class.forName(className);
 						skin = (DialogSkin) skinClass.newInstance();
 					}
 					catch(IllegalAccessException e)
 					{
 						errors.add("DialogSkin class '"+className+"' access exception: " + e.toString());
 					}
 					catch(ClassNotFoundException e)
 					{
 						errors.add("DialogSkin class '"+className+"' not found: " + e.toString());
 					}
 					catch(InstantiationException e)
 					{
 						errors.add("DialogSkin class '"+className+"' instantiation exception: " + e.toString());
 					}
 				}
 				else
 				{
 					skin = new StandardDialogSkin();
 				}
 
 				skin.importFromXml(skinElem);
 				SkinFactory.addDialogSkin(skinElem.getAttribute("name"), skin);
 			}
             else if(nodeName.equals("register-field"))
             {
                 Element typeElem = (Element) node;
                 String className = typeElem.getAttribute("class");
                 try
                 {
                     DialogFieldFactory.addFieldType(typeElem.getAttribute("tag-name"), className);
                 }
                 catch(ClassNotFoundException e)
                 {
                     errors.add("Field class '"+className+"' not found: " + e.toString());
                 }
             }
 		}
 
 		addMetaInformation();
 	}
 
     static public long getFieldsCount(Metric fieldsMetric, Node parent)
     {
         long totalsCount = 0;
 
         NodeList children = parent.getChildNodes();
         if(children != null && children.getLength() > 0)
         {
             for(int c = 0; c < children.getLength(); c++)
             {
                 Node child = children.item(c);
                 String nodeName = child.getNodeName();
 
                 if(nodeName.startsWith(DialogField.FIELDTAGPREFIX))
                 {
                     totalsCount++;
 
                     Metric fieldTypeMetric = fieldsMetric.getChild(nodeName);
                     if(fieldTypeMetric == null)
                     {
                         fieldTypeMetric = fieldsMetric.createChildMetricSimple(nodeName);
                         fieldTypeMetric.setFlag(Metric.METRICFLAG_SHOW_PCT_OF_PARENT);
                     }
 
                     fieldsMetric.incrementCount();
                     fieldTypeMetric.incrementCount();
                 }
 
                 totalsCount += getFieldsCount(fieldsMetric, child);
             }
         }
 
         return totalsCount;
     }
 
 	public Metric getMetrics(Metric root)
 	{
 		reload();
 
 		Metric metrics = root.createChildMetricGroup("User Interface");
 		Metric packagesMetric = metrics.createChildMetricSimple("Total Packages");
 		Metric dialogsMetric = metrics.createChildMetricSimple("Total Dialogs");
 		Metric fieldsPerDlgMetric = metrics.createChildMetricAverage("Avg Fields per Dialog");
 		Metric fieldsMetric = metrics.createChildMetricSimple("Total Fields");
 		fieldsMetric.setFlag(Metric.METRICFLAG_SORT_CHILDREN);
 		Metric dialogSkinsMetric = metrics.createChildMetricSimple("Custom Dialog Skins");
 		Metric customFieldTypesMetric = metrics.createChildMetricSimple("Custom Field Types");
 
 		try
 		{
 			packagesMetric.setSum(getSelectNodeListCount("//dialogs"));
 			dialogSkinsMetric.setSum(getSelectNodeListCount("//dialog-skin"));
 			customFieldTypesMetric.setSum(getSelectNodeListCount("//register-field"));
 
 			NodeList dialogsList = selectNodeList("//dialog");
 			dialogsMetric.setSum(dialogsList.getLength());
 			for(int n = 0; n < dialogsList.getLength(); n++)
 			{
 				long fieldCount = getFieldsCount(fieldsMetric, dialogsList.item(n));
 				fieldsPerDlgMetric.incrementAverage(fieldCount);
 			}
 		}
 		catch(Exception e)
 		{
 			metrics.createChildMetricSimple(e.toString());
 		}
 
 		return metrics;
 	}
 }
