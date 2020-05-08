 /*******************************************************************************
  * Copyright (c) 2010 Bolton University, UK.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the License
  * which accompanies this distribution in the file LICENSE.txt
  *******************************************************************************/
 package org.rulez.magwas.styledhtml;
 
 import java.io.File;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import uk.ac.bolton.archimate.editor.model.IModelExporter;
 import uk.ac.bolton.archimate.model.IArchimateModel;
 
 import org.rulez.magwas.styledhtml.Widgets;
 import org.rulez.magwas.styledhtml.IPreferenceConstants;
 import org.rulez.magwas.styledhtml.steps.StepFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * Styled HTML Exporter of Archimate model
  * <p>
  * Input is the style directory, containing:<ul>
  * 		<li>a file called style.xslt: the xml stylesheet to be applied to the "rich model file"</li>
  * 		<li>any other files needed for the presentation of resulting html</li></ul>
  * 
  * <p>
  * In the future (If I still think it is a good idea), the style directory may contain a file containing the name of scripts to be run on archirich.xml and index.html.
  * <p>
  * Output is the report directory, containing:<ul>
  * 		<li>copy of contents of the style directory</li>
  * 		<li>archirich.xml : it is the model file, but each document tags are enriched with HTMLReportExporter's paqrseCharsAndLinks.</li>
  * 		<li>diagrams in png: all diagrams are saved to ID.png, where ID is the id of the diagram</li>
  * 		<li>index.html: the result of applying style.xslt to archirich.xml</li>
  * 
  * @author Árpád Magosányi
  */
 public class StyledHtml implements IModelExporter {
 	
 	private EventLog log;
 
     public StyledHtml() {
     }
     
     public void export(IArchimateModel model) {
     	log = new EventLog("Styled export");
     	log.issueInfo(null, null, "starting styled export", EventLog.now());
     	try {
         	String stylepath = StyledHtmlPlugin.INSTANCE.getPreferenceStore().getString(IPreferenceConstants.STYLE_PATH);
         	File stylefile = new File(stylepath);
         	Document style;
     		if ((null != stylefile) && stylefile.exists()) {
     			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     			DocumentBuilder db;
     			try {
     				db = dbf.newDocumentBuilder();
     				style = db.parse(stylefile);
     			} catch (Exception e) {
     				Widgets.tellProblem("Problem loading style file", e.toString());
     	        	log.printStackTrace(e);
     	        	return;
     			}
     		} else {
     			Widgets.tellProblem("Not exporting", "nonexistent style");
     			return;
     		}
 
         	Boolean ask = StyledHtmlPlugin.INSTANCE.getPreferenceStore().getBoolean(IPreferenceConstants.OUT_ASK);
         	String opath = StyledHtmlPlugin.INSTANCE.getPreferenceStore().getString(IPreferenceConstants.OUT_PATH);
         	File targetdir;
         	if((!ask) || (opath == null)) {
         		String lastpath = StyledHtmlPlugin.INSTANCE.getPreferenceStore().getString(IPreferenceConstants.LAST_STYLED_PATH);
         		if (null == lastpath) {
         			StyledHtmlPlugin.INSTANCE.getPreferenceStore().setValue(IPreferenceConstants.LAST_STYLED_PATH, opath);
         		}
         		targetdir = Widgets.askSaveFile(IPreferenceConstants.LAST_STYLED_PATH,null);
         	} else {
         		targetdir = new File(opath);
         	}
         	if(targetdir == null) {
         		return;
         	}
         	File styledir = new File(stylefile.getParent());
         	StepFactory sf = new StepFactory(log,model,styledir, targetdir);
         	NodeList styles = style.getElementsByTagName("style");
         	for(int i=0;i<styles.getLength();i++) {
         		Element s = (Element) styles.item(i);
             	sf.get("style").doit(s,targetdir);        	
         	}
    		
     	} catch (Exception e) {
 			log.issueError(null, null, "Export problem", e.getMessage());
 			e.printStackTrace();
     	}
     	log.show();
     }
 }
