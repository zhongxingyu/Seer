 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.vis.temporal;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.velocity.VelocityContext;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.html.VelocityTemplateService;
 import org.seasr.meandre.support.io.DOMUtils;
 import org.seasr.meandre.support.io.IOUtils;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import sun.misc.BASE64Encoder;
 
 /**
  * @author Lily Dong
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Lily Dong",
         description = "Generates the necessary HTML and XML files " +
                       "for viewing timeline and store them on the local machine. " +
                       "The two files will be stored under public/resources/timeline/file/. " +
                       "will be stored under public/resources/timeline/js/. " +
                       "For fast browse, dates are grouped into different time slices. " +
                       "The number of time slices is designed as a property. " +
                       "If granularity is not appropriate, adjusts this porperty.",
         name = "Simile Timeline Generator",
         tags = "simile, timeline",
         rights = Licenses.UofINCSA,
         baseURL="meandre://seasr.org/components/tools/",
         dependency = {"protobuf-java-2.0.3.jar", "velocity-1.6.1-dep.jar"},
         resources = {"SimileTimelineGenerator.vm"}
 )
 public class SimileTimelineGenerator extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 	        description = "The source XML document",
 	        name = Names.PORT_XML
 	)
     protected static final String IN_XML = Names.PORT_XML;
 
 	@ComponentInput(
 	        description = "The subdirectory under public resources to " +
 	        		"store HTML and XML files. " +
 	        		"The path name must be specified as subdir1/subdir2/.",
 	        name = Names.PORT_DIRECTORY
 	)
     protected static final String IN_DIRECTORY = Names.PORT_DIRECTORY;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 	        description = "The HTML for the Simile Timeline viewer",
 	        name = Names.PORT_HTML
 	)
 	protected static final String OUT_HTML = Names.PORT_HTML;
 
 
     //--------------------------------------------------------------------------------------------
 
 
 	protected static final String simileVelocityTemplate =
 	    "org/seasr/meandre/components/vis/temporal/SimileTimelineGenerator.vm";
 
     /** Store document title */
     private String docTitle;
 
     /** Store the minimum value of year */
     private int minYear;
 
     /** Store the maximum value of year */
     private int maxYear;
 
     private VelocityContext _context;
 
     private static String delimiter = "/";
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _context = VelocityTemplateService.getInstance().getNewContext();
         _context.put("ccp", ccp);
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
     	String path = DataTypeParser.parseAsString(
     			cc.getDataComponentFromInput(IN_DIRECTORY))[0];
     	path = path.trim();
     	if(path.startsWith(delimiter)) //remove the first delimiter
     		path = path.substring(1);
     	if(path.endsWith(delimiter)) //remove the last delimiter
     		path = path.substring(0, path.length()-1);
 
         Document doc = DataTypeParser.parseAsDomDocument(cc.getDataComponentFromInput(IN_XML));
 
         String dirName = cc.getPublicResourcesDirectory() + File.separator;
         StringBuffer sb = new StringBuffer();
         StringTokenizer st = new StringTokenizer(path, delimiter);
         while(st.hasMoreTokens()) {
         	sb.append(st.nextToken()).append(File.separator);
         }
         dirName += sb.toString();
 
         File dir = new File(dirName);
         if (!dir.exists())
             if (!dir.mkdirs())
                 throw new IOException("The directory '" + dirName + "' could not be created!");
 
         console.finest("Set storage location to " + dirName);
 
         String webUiUrl = cc.getWebUIUrl(true).toString();
         Date now = new Date();
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
         String htmlFileName = "my" + formatter.format(now) + ".html",
                xmlFileName = "my" + formatter.format(now) + ".xml";
         String str = webUiUrl +
         	"public" + delimiter +
         	"resources" + delimiter +
         	path + delimiter;
         String htmlLocation = str + htmlFileName,
                xmlLocation  = str + xmlFileName;
 
         console.finest("htmlFileName=" + htmlFileName);
         console.finest("xmlFileName=" + xmlFileName);
         console.finest("htmlLocation=" + htmlLocation);
         console.finest("xmlLocation=" + xmlLocation);
 
         URI xmlURI = DataTypeParser.parseAsURI(new File(dirName + xmlFileName).toURI());
         URI htmlURI = DataTypeParser.parseAsURI(new  File(dirName + htmlFileName).toURI());
 
         String simileXml = generateXML(doc);
 
         Document xmlDoc = DOMUtils.createDocument(simileXml);
         xmlDoc.normalize();
         if (xmlDoc.getDocumentElement().getElementsByTagName("event").getLength() == 0) {
             outputError("No dates could be extracted from your item(s) - Nothing to display", Level.WARNING);
             return;
         }
 
         Writer xmlWriter = IOUtils.getWriterForResource(xmlURI);
         xmlWriter.write(simileXml);
         xmlWriter.close();
 
         String simileHtml = generateHTML(simileXml, xmlLocation);
 
         Writer htmlWriter = IOUtils.getWriterForResource(htmlURI);
         htmlWriter.write(simileHtml);
         htmlWriter.close();
 
         console.info("The Simile Timeline HTML content was created at " + htmlLocation);
         console.info("The Simile Timeline XML content was created at " + xmlLocation);
 
         cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(simileHtml));
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     private String generateHTML(String simileXml, String simileXmlUrl) throws Exception {
         VelocityTemplateService velocity = VelocityTemplateService.getInstance();
         _context.put("maxYear", maxYear);
         _context.put("minYear", minYear);
         _context.put("simileXmlBase64", new BASE64Encoder().encode(simileXml.getBytes()));
         _context.put("simileXmlUrl", simileXmlUrl);
 
         return velocity.generateOutput(_context, simileVelocityTemplate);
     }
 
     private String generateXML(Document doc)
     throws javax.xml.parsers.ParserConfigurationException,
     javax.xml.transform.TransformerConfigurationException,
     javax.xml.transform.TransformerException{
     	minYear = Integer.MAX_VALUE;
     	maxYear = Integer.MIN_VALUE;
 
     	DocumentBuilderFactory documentBuilderFactory =
     		DocumentBuilderFactory.newInstance();
     	DocumentBuilder documentBuilder =
     		documentBuilderFactory.newDocumentBuilder();
     	Document document = documentBuilder.newDocument();
 
     	Element rootElement = document.createElement("data");
     	document.appendChild(rootElement);
 
 		doc.getDocumentElement().normalize();
 		docTitle = doc.getDocumentElement().getAttribute("docID");
 		console.finest("Root element : " + docTitle);
 		NodeList dateNodes = doc.getElementsByTagName("date");
 
 		for (int i = 0, iMax = dateNodes.getLength(); i < iMax; i++) {
 			Element elEntity = (Element)dateNodes.item(i);
 			String aDate = elEntity.getAttribute("value");
 
 			//standardize date
 
 			String month = null,
 				   day   = null,
 				   year  = null;
 
 			String startMonth = null,
 					 endMonth = null;
 
 			Pattern datePattern = Pattern.compile("(january|jan|february|feb|march|mar|" + //look for month
 					"april|apr|may|june|jun|july|jul|august|aug|september|sept|october|oct|"+
 					"november|nov|december|dec)");
 			Matcher dateMatcher = datePattern.matcher(aDate);
 			if(dateMatcher.find()) { //look for month
 				month = dateMatcher.group(1);
 			} else { //look for season
 				datePattern = Pattern.compile("(spring|summer|fall|winter)");
 				dateMatcher = datePattern.matcher(aDate);
 				if(dateMatcher.find()) {
 					String season = dateMatcher.group(1);
 					if(season.equalsIgnoreCase("spring")) {
 						startMonth = "Mar 21";
 						endMonth = "June 20";
 					} else if(season.equalsIgnoreCase("summer")) {
 						startMonth = "June 21";
 						endMonth = "Sept 20";
 					} else if(season.equalsIgnoreCase("fall")) {
 						startMonth = "Sept 21";
 						endMonth = "Dec 20";
 					} else { //winter
 						startMonth = "Dec 21";
 						endMonth = "Mar 20";
 					}
 				}
 			}
 
 			datePattern = Pattern.compile("(\\b\\d{1,2}((st)?|(nd)?|(rd)?|(th)?)\\b)");
 			dateMatcher = datePattern.matcher(aDate); //look for 1 or 1st or 22 or 22nd or 3rd or 4th
 			if(dateMatcher.find()) {
 				datePattern = Pattern.compile("(\\d{1,2})"); //look for 1 or 22 only
 				dateMatcher = datePattern.matcher(dateMatcher.group(1));
 				if(dateMatcher.find());
 					day = dateMatcher.group(1);
 			}
 
 			datePattern = Pattern.compile("(\\d{3,4})"); //look for year with 3 or 4 digits
 			dateMatcher = datePattern.matcher(aDate);
 			if(dateMatcher.find()) { //look for year
 	            StringBuffer sbHtml = new StringBuffer();
 	            int nr = 0;
 
 			    NodeList sentenceNodes = elEntity.getElementsByTagName("sentence");
 			    for (int idx = 0, idxMax = sentenceNodes.getLength(); idx < idxMax; idx++) {
 			        Element elSentence = (Element)sentenceNodes.item(idx);
 			        String docTitle = elSentence.getAttribute("docTitle");
 			        String theSentence = elSentence.getTextContent();
 
 			        int datePos = theSentence.toLowerCase().indexOf(aDate);
 			        String sentBefore = theSentence.substring(0, datePos);
 			        String sentAfter = theSentence.substring(datePos + aDate.length());
 			        theSentence = StringEscapeUtils.escapeHtml(sentBefore) +
 			                      "<font color='red'>" + StringEscapeUtils.escapeHtml(aDate) + "</font>" +
 			                      StringEscapeUtils.escapeHtml(sentAfter);
                     sbHtml.append("<div onclick='toggleVisibility(this)' style='position:relative' align='left'><b>Sentence ").append(++nr);
                     if (docTitle != null && docTitle.length() > 0)
                         sbHtml.append(" from '" + StringEscapeUtils.escapeHtml(docTitle) + "'");
                     sbHtml.append("</b><span style='display: ' align='left'><table><tr><td>").append(theSentence).append("</td></tr></table></span></div>");
 			    }
 
 			    String sentence = sbHtml.toString();
 
 				year = dateMatcher.group(1);
 				minYear = Math.min(minYear, Integer.parseInt(year));
 				maxYear = Math.max(maxYear, Integer.parseInt(year));
 
 				//year or month year or month day year
 				if(day == null)
 					if(month == null) { //season year
 						if(startMonth != null) {//spring or summer or fall or winter year
 							Element em = document.createElement("event");
 							em.setAttribute("start", startMonth + " " + year);
 							em.setAttribute("end", endMonth + " " + year);
 							em.setAttribute("title", aDate+"("+nr+")");
 							em.appendChild(document.createTextNode(sentence));
 							rootElement.appendChild(em);
 						} else { //year
 							Element em = document.createElement("event");
 							em.setAttribute("start", year);
 							em.setAttribute("title", aDate+"("+nr+")");
 							em.appendChild(document.createTextNode(sentence));
 							rootElement.appendChild(em);
 						}
 					} else { //month year
 						String startDay = month + " 01";
 						int m = 1;
 						if(month.startsWith("feb"))
 							m = 2;
 						else if(month.startsWith("mar"))
 							m = 3;
 						else if(month.startsWith("apr"))
 							m = 4;
 						else if(month.startsWith("may"))
 							m = 5;
 						else if(month.startsWith("jun"))
 							m = 6;
 						else if(month.startsWith("jul"))
 							m = 7;
 						else if(month.startsWith("aug"))
 							m = 8;
 						else if(month.startsWith("sept"))
 							m = 9;
 						else if(month.startsWith("oct"))
 							m = 10;
 						else if(month.startsWith("nov"))
 							m = 11;
 						else if(month.startsWith("dec"))
 							m = 12;
 						int y = Integer.parseInt(year);
 						int numberOfDays = 31;
 						if (m == 4 || m == 6 || m == 9 || m == 11)
 							  numberOfDays = 30;
 						else if (m == 2) {
 							boolean isLeapYear = (y % 4 == 0 && y % 100 != 0 || (y % 400 == 0));
 							if (isLeapYear)
 								numberOfDays = 29;
 							else
 								numberOfDays = 28;
 						}
 						String endDay = month + " " + Integer.toString(numberOfDays);
 						Element em = document.createElement("event");
 						em.setAttribute("start", startDay + " " + year);
 						em.setAttribute("end", endDay + " " + year);
 						em.setAttribute("title", aDate+"("+nr+")");
 						em.appendChild(document.createTextNode(sentence));
 						rootElement.appendChild(em);
 					}
 				else {
 					if(month == null) {//year
 						Element em = document.createElement("event");
 						em.setAttribute("start", year);
 						em.setAttribute("title", aDate+"("+nr+")");
 						em.appendChild(document.createTextNode(sentence));
 						rootElement.appendChild(em);
 					} else { //month day year
 						Element em = document.createElement("event");
 						em.setAttribute("start", month + " " + day + " " + year);
 						em.setAttribute("title", aDate+"("+nr+")");
 						em.appendChild(document.createTextNode(sentence));
 						rootElement.appendChild(em);
 					}
 				}
 			}
 		}
 
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 	    Transformer transformer = transformerFactory.newTransformer();
 	    DOMSource source = new DOMSource(document);
 	    StreamResult result =  new StreamResult(new StringWriter());
 	    transformer.transform(source, result);
 	    return result.getWriter().toString();
     }
 }
