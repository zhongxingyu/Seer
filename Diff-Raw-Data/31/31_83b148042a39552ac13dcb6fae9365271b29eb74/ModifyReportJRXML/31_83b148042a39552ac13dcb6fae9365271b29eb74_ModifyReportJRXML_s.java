 /**
  * 
  */
 package com.mpower.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.mpower.domain.ReportField;
 import com.mpower.domain.ReportSelectedField;
 import com.mpower.domain.ReportWizard;
 import com.mpower.service.ReportFieldService;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 
 /**
  * @author egreen
  * <p>
  * Object to modify the jrxml document used by jasperserver to generate the report.
  *
  */
 public class ModifyReportJRXML {
 	private ReportWizard reportWizard;
 	private ReportFieldService reportFieldService;
 	
 	/**
      * Constructor for the <tt>XMLModifier</tt>.
      * @param reportWizard ReportWizard that contains the various report options.
      * 
      */
     public ModifyReportJRXML(ReportWizard reportWizard, ReportFieldService reportFieldService) {
     	this.setReportWizard(reportWizard);
     	this.setReportFieldService(reportFieldService);
     	
     }
 	
 	
     /**
 	 * Adds the grand totals to the report.
 	 * 
 	 * @param fileName The name and path of the jrxml file.
 	 */
     public void AddReportSummaryInfo(String fileName) throws IOException, SAXException, ParserConfigurationException {
     	Document document = loadXMLDocument(fileName);
     	
     	Node variable;
     	NodeList nodeList = document.getElementsByTagName("field");
     	Node node = nodeList.item(nodeList.getLength()-1).getNextSibling();
     	Node bkgNode = document.getElementsByTagName("background").item(0);
     	Node jasperReport = document.getElementsByTagName("jasperReport").item(0);
     	
     	
     	HashMap<String, Integer> fieldProperties = getColumnStartingPositions(document); 	
     	HashMap<String, Integer> fieldWidth = getColumnWidths(document);
     	
     	//add the "groupElem" node before the background node
     	Element groupElem = createGroup(document, fieldProperties, fieldWidth, "global_column_0");
     	if(document.getElementsByTagName("group").item(0) != null)
     		jasperReport.insertBefore(groupElem, document.getElementsByTagName("group").item(0));
     	else
     		jasperReport.insertBefore(groupElem, bkgNode);
     	
     	//iterate thru the fields to find the summary fields and create the variables
     	Iterator<?> itFields = getSelectedReportFieldsInOrder().iterator();
     	while (itFields.hasNext()){
     		ReportField f = (ReportField) itFields.next();
     		if (f.getIsSummarized()){
     			if (f.getPerformSummary()){
     				variable =	buildVariableNode(f, "Sum", "global_column_0", document);
         			jasperReport.insertBefore(variable, node);
         		}
     			if (f.getAverage()){
     				variable =	buildVariableNode(f, "Average", "global_column_0", document);
         			jasperReport.insertBefore(variable, node);
     			}
     			if (f.getLargestValue()){
     				variable =	buildVariableNode(f, "Highest", "global_column_0", document);
         			jasperReport.insertBefore(variable, node);
     			}
     			if (f.getSmallestValue()){
     				variable =	buildVariableNode(f, "Lowest", "global_column_0", document);
         			jasperReport.insertBefore(variable, node);
     			}
     			if (f.getRecordCount()){
     				variable =	buildVariableNode(f, "Count", "global_column_0", document);
         			jasperReport.insertBefore(variable, node);
     			}
     		}
     	}
     	
     	saveXMLtoFile(fileName, document);	     
     }
     
     /**
      * Add totals to the groups in the report.
      * 
      * @param fileName The name and path of the jrxml file.
      */
     public void AddGroupSummaryInfo(String fileName) throws IOException, SAXException, ParserConfigurationException {
     	Document document = loadXMLDocument(fileName);
     	
     	NodeList groups = document.getElementsByTagName("group");
     	NodeList groupFooterNodes = document.getElementsByTagName("groupFooter");
     	String groupName = null;
     	Node variable;
     	Node node = document.getElementsByTagName("group").item(0);
     	Node jasperReport = document.getElementsByTagName("jasperReport").item(0);
     	
     	HashMap<String, Integer> fieldProperties = getColumnStartingPositions(document); 	
     	HashMap<String, Integer> fieldWidth = getColumnWidths(document);
     	  	
     	Integer groupsCount = groups.getLength();
     	Integer groupFootersCount = groupFooterNodes.getLength();
     	//remove the groupFooter empty band before adding the new ones
     	for (int i=0;i<groupFootersCount;i++){
     		Node groupFooterNode = groupFooterNodes.item(i);
     		int groupFooterChildrenCount = groupFooterNode.getChildNodes().getLength();
     		for (int childIndex=groupFooterChildrenCount-1;childIndex>=0;childIndex--){
         		groupFooterNode.removeChild(groupFooterNode.getChildNodes().item(childIndex));
         	}
     	}    	
     	
     	//iterate thru the fields to find the summary fields and add the variables and footers
     	Iterator<?> itFields = getSelectedReportFieldsInOrder().iterator();
     	boolean addGroup = false;
     	  List<String> groupsToAdd = new ArrayList<String>();
 
     	while (itFields.hasNext()){
     		ReportField f = (ReportField) itFields.next();
     		if (f.getIsSummarized()){
     			for (int i=0;i<groupsCount;i++){
     	    		//get the group name
     	        	Element group = (Element) groups.item(i);
     	        	if (group != null){
     	            	groupName = group.getAttribute("name");
     	            	if (groupName != null && groupName != ""){
     	            		Node groupFooterNode = groupFooterNodes.item(i);
 	    	            	if (f.getPerformSummary()){
 			    				variable =	buildVariableNode(f, "Sum", groupName, document);
 			        			jasperReport.insertBefore(variable, node);
 			        			addGroup = true;
 			        		 }
 			    			if (f.getAverage()){
 			    				variable =	buildVariableNode(f, "Average", groupName, document);
 			        			jasperReport.insertBefore(variable, node);
 			        			addGroup = true;
 			        		}
 			    			if (f.getLargestValue()){
 			    				variable =	buildVariableNode(f, "Highest", groupName, document);
 			        			jasperReport.insertBefore(variable, node);
 			        			addGroup = true;
 			        		}
 			    			if (f.getSmallestValue()){
 			    				variable =	buildVariableNode(f, "Lowest", groupName, document);
 			        			jasperReport.insertBefore(variable, node);
 			        			addGroup = true;
 			        		}
 			    			if (f.getRecordCount()){
 			    				variable =	buildVariableNode(f, "Count", groupName, document);
 			        			jasperReport.insertBefore(variable, node);
 			        			addGroup = true;
 			        		}
 			    			if (addGroup)
 			    				groupsToAdd.add(groupName);
     	            	}
     	        	}
     			}
     		}
     	}
 	
 		for (int i=0;i<groupsCount;i++){
     		//get the group name
         	Element group = (Element) groups.item(i);
         	if (group != null){
             	groupName = group.getAttribute("name");
             	if (groupName != null && groupName != ""){
             		Node groupFooterNode = groupFooterNodes.item(i);
             		groupFooterNode.appendChild(createGroupFooterBand(document, fieldProperties, fieldWidth, groupName));
             	}
         	}
 		}
 
 		saveXMLtoFile(fileName, document);	     
     }
    
     /**
 	 * Normalizes and saves the modified XML document.
 	 * @param fileName
 	 * @param document
 	 * @throws IOException
 	 */
 	private void saveXMLtoFile(String fileName, Document document)
 			throws IOException {
 		document.normalize();
     	XMLSerializer serializer = new XMLSerializer();
     	serializer.setOutputCharStream(new java.io.FileWriter(fileName));
     	serializer.serialize(document); 
 	}
 
 	/**
 	 * Parses the XML document and returns a HashMap with the columnName and x starting position.
 	 * 
 	 * @param document
 	 * @return HashMap
 	 */
 	private HashMap<String, Integer> getColumnStartingPositions(
 			Document document) {
 		//
     	//get the detail node
     	Node detailNode = document.getElementsByTagName("columnHeader").item(0);
     	HashMap<String, Integer> fieldProperties = new HashMap<String, Integer>();
     	//HashMap<String, Integer> fieldWidth = new HashMap<String, Integer>();
     	//inside the detail node -> textField -> get the textFieldExpression, and the reportelement attr x
         NodeList detailChildList = detailNode.getChildNodes();        
         for (int detailChildIndex=0; detailChildIndex<detailChildList.getLength(); detailChildIndex++) {
     	    if (detailChildList.item(detailChildIndex).getNodeName().equals("band")) {
                 NodeList textFieldList = detailChildList.item(detailChildIndex).getChildNodes();
                 for (int textFieldIndex=0; textFieldIndex<textFieldList.getLength(); textFieldIndex++) {
                 	NodeList textFieldProperties = textFieldList.item(textFieldIndex).getChildNodes();
                 	int x = -1;
                 	int width = -1;
                 	String fieldName = null; 
                     for (int textFieldPropertiesIndex=0; textFieldPropertiesIndex<textFieldProperties.getLength(); textFieldPropertiesIndex++) {
                     	if (textFieldProperties.item(textFieldPropertiesIndex).getNodeName().equals("reportElement")) {
                     		x = Integer.parseInt(textFieldProperties.item(textFieldPropertiesIndex).getAttributes().getNamedItem("x").getNodeValue());
                     		width = Integer.parseInt(textFieldProperties.item(textFieldPropertiesIndex).getAttributes().getNamedItem("width").getNodeValue());
                     	} else if (textFieldProperties.item(textFieldPropertiesIndex).getNodeName().equals("textFieldExpression")) {
                    		//if (textFieldProperties.item(textFieldPropertiesIndex).getTextContent().contains("$F{")) {
                     			String cdata = textFieldProperties.item(textFieldPropertiesIndex).getTextContent(); 
                     			fieldName =cdata.substring(1, cdata.length() - 1);
                    		//}                    			
                     	}
                     }
                 	if (fieldName != null && x != -1)
                 		fieldProperties.put(fieldName, x);
                 		//fieldWidth.put(fieldName, width );
                 }                
             }
         }
 		return fieldProperties;
 	}
 
 	/**
 	 * Parses the XML document and returns a HashMap with the columnName and field width of x.
 	 * 
 	 * @param document
 	 * @return HashMap
 	 */
 	private HashMap<String, Integer> getColumnWidths(
 			Document document) {
 		//parse the document and create a hashmap of the (columnName, x-value)
     	//get the detail node
     	Node detailNode = document.getElementsByTagName("columnHeader").item(0);
     	//HashMap<String, Integer> fieldProperties = new HashMap<String, Integer>();
     	HashMap<String, Integer> fieldWidth = new HashMap<String, Integer>();
     	//inside the detail node -> textField -> get the textFieldExpression, and the reportelement attr x
         NodeList detailChildList = detailNode.getChildNodes();        
         for (int detailChildIndex=0; detailChildIndex<detailChildList.getLength(); detailChildIndex++) {
     	    if (detailChildList.item(detailChildIndex).getNodeName().equals("band")) {
                 NodeList textFieldList = detailChildList.item(detailChildIndex).getChildNodes();
                 for (int textFieldIndex=0; textFieldIndex<textFieldList.getLength(); textFieldIndex++) {
                 	NodeList textFieldProperties = textFieldList.item(textFieldIndex).getChildNodes();
                 	int x = -1;
                 	int width = -1;
                 	String fieldName = null; 
                     for (int textFieldPropertiesIndex=0; textFieldPropertiesIndex<textFieldProperties.getLength(); textFieldPropertiesIndex++) {
                     	if (textFieldProperties.item(textFieldPropertiesIndex).getNodeName().equals("reportElement")) {
                     		x = Integer.parseInt(textFieldProperties.item(textFieldPropertiesIndex).getAttributes().getNamedItem("x").getNodeValue());
                     		width = Integer.parseInt(textFieldProperties.item(textFieldPropertiesIndex).getAttributes().getNamedItem("width").getNodeValue());
                     	} else if (textFieldProperties.item(textFieldPropertiesIndex).getNodeName().equals("textFieldExpression")) {
                    		//if (textFieldProperties.item(textFieldPropertiesIndex).getTextContent().contains("$F{")) {
                     			String cdata = textFieldProperties.item(textFieldPropertiesIndex).getTextContent(); 
                     			fieldName = cdata.substring(1, cdata.length() - 1);
                    		//}                    			
                     	}
                     }
                 	if (fieldName != null && x != -1)
                 		//fieldProperties.put(fieldName, x);
                 		fieldWidth.put(fieldName, width );
                 }                
             }
         }
 		return fieldWidth;
 	}
 	
 	/**
 	 * Loads the XML document.
 	 * @param fileName
 	 * @return
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	private Document loadXMLDocument(String fileName)
 			throws ParserConfigurationException, SAXException, IOException {
 		// Load the report xml document
     	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
     	DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
     	Document document = documentBuilder.parse(new File(fileName));
 		return document;
 	}
     
 	private Element createGroup(Document document,
 			HashMap<String, Integer> fieldProperties,
 			HashMap<String, Integer> fieldWidth,
 			String resetGroup) {
 		//create the "group" element
     	Element group = document.createElement("group");
     	group.setAttribute("name", resetGroup);
     	
     		//create child "groupExpression" of the "group" element with a CDATA section
 	    	Element groupExp = document.createElement("groupExpression");
 	    	groupExp.appendChild(document.createCDATASection("\"Total\""));
 	    	group.appendChild(groupExp);
 	    	
 	    	//create child "groupHeader" of the "group" element 
 	    	Element groupHeader = document.createElement("groupHeader");
 	    	group.appendChild(groupHeader);
 	    		
 	    		//create child "headerBand" of the "groupHeader" element 
 	    		Element headerBand = document.createElement("band");
 	    		groupHeader.appendChild(headerBand);
 	    		
 	    		//create groupFooter
 	    		group.appendChild(createGroupFooter(document, fieldProperties, fieldWidth, resetGroup));
 	 
 	    return group;
 	    		
 	}
 	
 	/**
 	 * Adds a groupFooter to the Jasper Report. <br/>
 	 * Groups can be nested.<br/><br/>
 	 * 
 	 * Example:<br/>
 	 * {@code} group.appendChild(createGroupFooter(document, fieldProperties, fieldWidth, resetGroup));
 	 * 
 	 * @param document XML Document.
 	 * @param fieldProperties Hashmap that contains the columnName and the x-starting position of the column.
 	 * @param fieldWidth Hashmap that contains the columnName and the width of the column.
 	 * @param resetGroup Name of the Group.
 	 * @return Element 
 	 */
 	private Element createGroupFooter(Document document,
 			HashMap<String, Integer> fieldProperties,
 			HashMap<String, Integer> fieldWidth,
 			String resetGroup) {
 		
 	    	//create child "groupFooter" of the "group" element 
 	    	Element groupFooter = document.createElement("groupFooter");
 	    	
 	    	
 		    	//create child "band" of "groupfooter" with attr
 		    	Element band = document.createElement("band");
 		    	band.setAttribute("height", "150");
 		    	groupFooter.appendChild(createGroupFooterBand(document, fieldProperties, fieldWidth, resetGroup));
 		    	
 		    	
 		    return groupFooter;
 	}
 
 	/**
 	 * Adds a groupFooter Band to the Jasper Report. <br/>
 	 * Groups can be nested.<br/><br/>
 	 * 
 	 * Example:<br/>
 	 * {@code} group.appendChild(createGroupFooter(document, fieldProperties, fieldWidth, resetGroup));
 	 * 
 	 * @param document XML Document.
 	 * @param fieldProperties Hashmap that contains the columnName and the x-starting position of the column.
 	 * @param fieldWidth Hashmap that contains the columnName and the width of the column.
 	 * @param resetGroup Name of the Group.
 	 * @return Element 
 	 */
 	private Element createGroupFooterBand(Document document,
 			HashMap<String, Integer> fieldProperties,
 			HashMap<String, Integer> fieldWidth,
 			String resetGroup) {
 		
 			//
 			//Variables
 			Integer bandTotalHeight = 0;
 			Integer rowHeight = 16;  
 			int y = 0;
 	    	int x = 0;
 	    	int width = 0;
 	    	int xCalc = 0;
 			int widthCalc = 0;
 			int totalWidth = 0;
 			//get the total width
 			Iterator<?> itTotalFieldWidth = getSelectedReportFieldsInOrder().iterator();
 	    	while (itTotalFieldWidth.hasNext()){
 	    		ReportField fWidth = (ReportField) itTotalFieldWidth.next();
 	    		int lastColumnX = fieldProperties.get(fWidth.getDisplayName());
 	    		int lastColumnWidth = fieldWidth.get(fWidth.getDisplayName());
 	    		totalWidth = lastColumnX + lastColumnWidth;
     		}
 	    	
 	    	//create child "band" of "groupfooter" with attr
 	    	Element band = document.createElement("band");
 	    	Element frame = document.createElement("frame");
 	    	Element rptElementFrame = document.createElement("reportElement");
 	    	frame.appendChild(rptElementFrame);
 	    	Element box = document.createElement("box");
 	    	frame.appendChild(box);
 	    	Element rectangle = document.createElement("rectangle");
 	    	frame.appendChild(rectangle);
 	    	Element rptElementRectangle = document.createElement("reportElement");
 	    	rectangle.appendChild(rptElementRectangle);
 	    	Element graphicElement = document.createElement("graphicElement");
 	    	rectangle.appendChild(graphicElement);
 	    	Element pen = document.createElement("pen");
 	    	graphicElement.appendChild(pen);
 	    	
 	    	//Add the group Label to the footer section 
 	    	if (resetGroup.compareToIgnoreCase("global_column_0") == 0){
 	    		x = 0;
 	    		width = totalWidth;
 	    		frame.appendChild(buildSummaryLabel("Grand Totals", document, 0, 0, width, rowHeight, false, null));
 				frame.appendChild(addLine(document, 1,  rowHeight+1, totalWidth));
 				y += rowHeight*2+2;
 	    		
 	    	}
 	    	else{
 		    	Iterator<?> itFieldsGroupLabel = getSelectedReportFieldsInOrder().iterator();
 		    	while (itFieldsGroupLabel.hasNext()){
 		    		ReportField fLabel = (ReportField) itFieldsGroupLabel.next();
 		    		if (reportWizard.IsFieldGroupByField(fLabel.getId())){
 		    			x = fieldProperties.get(fLabel.getDisplayName());
 		    			width = fieldWidth.get(fLabel.getDisplayName());
 		    			String groupColumn = resetGroup.substring(resetGroup.indexOf("-") + 1);
 		    			if (fLabel.getDisplayName().compareToIgnoreCase(groupColumn) == 0){
		    				frame.appendChild(buildSummaryLabel(null, document, x, y, totalWidth - x, rowHeight, true, fLabel.getColumnName()));
 		    				frame.appendChild(addLine(document, 1, y+rowHeight+1, totalWidth));
 			    			y += rowHeight*2+2;
 			    			break;
 			    		}
 		    		}
 		    	}
 	    	}	
 		    
 	    	//Add the sum/total to the footer section
 	    	Boolean yFound = false; 
 	    	xCalc = 0;
 			widthCalc = 0;
 	    	Iterator<?> itFieldsSum = getSelectedReportFieldsInOrder().iterator();
 	    	while (itFieldsSum.hasNext()){
 	    		ReportField f = (ReportField) itFieldsSum.next();
 	    		if (f.getIsSummarized()){
 	    			if (f.getPerformSummary()){
 	    				xCalc = fieldProperties.get(f.getDisplayName());
 		    			widthCalc = fieldWidth.get(f.getDisplayName());
 		    			frame.appendChild(buildSummaryNodes(f, "Sum", resetGroup, document, xCalc, widthCalc, y, rowHeight));
 	    				yFound = true;
 	    			}
 	    		}
 	    	}
 	    	if (yFound){ 
 	    		frame.appendChild(addLine(document, x, y-1,  totalWidth - x));
 	    		frame.appendChild(buildSummaryLabel("Sum", document, x, y-rowHeight-2, totalWidth - x, rowHeight, false, null));
 	    		y += rowHeight*2+2;
     		}
 	    	
 	    	//Add the average to the footer section
 	    	yFound = false;
 	    	xCalc = 0;
 			widthCalc = 0;
 	    	Iterator<?> itFieldsAvg = getSelectedReportFieldsInOrder().iterator();
 	    	while (itFieldsAvg.hasNext()){
 	    		ReportField f = (ReportField) itFieldsAvg.next();
 	    		if (f.getIsSummarized()){
 	    			if (f.getAverage()){
 	    				xCalc = fieldProperties.get(f.getDisplayName());
 		    			widthCalc = fieldWidth.get(f.getDisplayName());
 		    			frame.appendChild(buildSummaryNodes(f, "Average", resetGroup, document, xCalc, widthCalc, y, rowHeight));
 	    				yFound = true;
 	    			}
 	    		}
 	    	}
 	    	if (yFound){ 
 	    		frame.appendChild(addLine(document, x, y-1,  totalWidth - x));
 	    		frame.appendChild(buildSummaryLabel("Avg", document, x, y-rowHeight-2, totalWidth - x, rowHeight, false, null));
 	    		y += rowHeight*2+2;
     		}
 	    		    	
 	    	//Add the max to the footer section
 	    	yFound = false;
 	    	xCalc = 0;
 			widthCalc = 0;
 	    	Iterator<?> itFieldsMax = getSelectedReportFieldsInOrder().iterator();
 	    	while (itFieldsMax.hasNext()){
 	    		ReportField f = (ReportField) itFieldsMax.next();
 	    		if (f.getIsSummarized()){
 	    			if (f.getLargestValue()){
 	    				xCalc = fieldProperties.get(f.getDisplayName());
 		    			widthCalc = fieldWidth.get(f.getDisplayName());
 		    			frame.appendChild(buildSummaryNodes(f, "Highest", resetGroup, document, xCalc, widthCalc, y, rowHeight));
 	    				yFound = true;
 	    			}
 	    		}
 	    	}
 	    	if (yFound){ 
 	    		frame.appendChild(addLine(document, x, y-1,  totalWidth - x));
 	    		frame.appendChild(buildSummaryLabel("Max", document, x, y-rowHeight-2, totalWidth - x, rowHeight, false, null));
 	    		y += rowHeight*2+2;
     		}
 	    		    
 	    	//Add the min to the footer section
 	    	yFound = false;
 	    	xCalc = 0;
 			widthCalc = 0;
 	    	Iterator<?> itFieldsMin = getSelectedReportFieldsInOrder().iterator();
 	    	while (itFieldsMin.hasNext()){
 	    		ReportField f = (ReportField) itFieldsMin.next();
 	    		if (f.getIsSummarized()){
 	    			if (f.getSmallestValue()){
 	    				xCalc = fieldProperties.get(f.getDisplayName());
 		    			widthCalc = fieldWidth.get(f.getDisplayName());
 		    			frame.appendChild(buildSummaryNodes(f, "Lowest", resetGroup, document, xCalc, widthCalc, y, rowHeight));
 	    				yFound = true;
 	    			}
 	    		}
 	    	}
 	    	if (yFound){ 
 	    		frame.appendChild(addLine(document, x, y-1,  totalWidth - x));
 	    		frame.appendChild(buildSummaryLabel("Min", document, x, y-rowHeight-2, totalWidth - x, rowHeight, false, null));
 	    		y += rowHeight*2+2;
     		}
 	    	
 	    	//Add count to the footer section
 	    	yFound = false;
 	    	xCalc = 0;
 			widthCalc = 0;
 	    	Iterator<?> itFieldsCount = getSelectedReportFieldsInOrder().iterator();
 	    	while (itFieldsCount.hasNext()){
 	    		ReportField f = (ReportField) itFieldsCount.next();
 	    		if (f.getIsSummarized()){
 	    			if (f.getRecordCount()){
 	    				xCalc = fieldProperties.get(f.getDisplayName());
 		    			widthCalc = fieldWidth.get(f.getDisplayName());
 		    			frame.appendChild(buildSummaryNodes(f, "Count", resetGroup, document, xCalc, widthCalc, y+1, rowHeight));
 	    				yFound = true;
 	    			}
 	    		}
 	    	}
 	    	if (yFound){ 
 	    		frame.appendChild(addLine(document, x, y-1,  totalWidth - x));//(xCalc + widthCalc)-x)
 	    		frame.appendChild(buildSummaryLabel("Count", document, x, y-rowHeight-2, totalWidth - x, rowHeight, false, null));
 	    		y += rowHeight*2+2;
     		}
 	    				    	
 	    	bandTotalHeight = y - rowHeight + 4;
 	    	Integer frameHeight = bandTotalHeight;
 	    	
         	rptElementFrame.setAttribute("stretchType", "RelativeToBandHeight");
 	    	rptElementFrame.setAttribute("mode", "Opaque");
 	    	rptElementFrame.setAttribute("x", "0"); //Integer.toString(x)
 	    	rptElementFrame.setAttribute("y", "0");
 	    	int frameWidth = 0;
 	    	
 	    	
 	    	frameWidth = (totalWidth + 4);
 	    	rptElementFrame.setAttribute("width", Integer.toString(frameWidth));
 	    	rptElementFrame.setAttribute("height", frameHeight.toString());
 	    	rptElementFrame.setAttribute("backcolor", "#EEEFEF");
 	    	box.setAttribute("topPadding", "1");
 	    	box.setAttribute("leftPadding", "1");
 	    	box.setAttribute("bottomPadding", "1");
 	    	box.setAttribute("rightPadding", "1");
 	    	
 	    	//rectangle
 	    	rectangle.setAttribute("radius", "10");
 	    	rptElementRectangle.setAttribute("mode", "Transparent");
 	    	rptElementRectangle.setAttribute("x", "0"); //Integer.toString(x)
 	    	rptElementRectangle.setAttribute("y", "0");
 	    	rptElementRectangle.setAttribute("width", Integer.toString(frameWidth - 2));
 	    	rptElementRectangle.setAttribute("height", Integer.toString(frameHeight - 2));
 	    	rptElementRectangle.setAttribute("forecolor", "#666666");
 	    	rptElementRectangle.setAttribute("backcolor", "#666666");
 	    	pen.setAttribute("lineStyle", "Double");
 	    	
 	    	band.appendChild(frame);
 	    	band.setAttribute("height", bandTotalHeight.toString());
 
 	    	return band;
 	}
 
 
 	/**
 	 * @param document
 	 * @param frame
 	 */
 	private Node addLine(Document document, int x, int y, int width) {
 		Element line = document.createElement("line");
 		//frame.appendChild(line);
 		Element rptElementLine = document.createElement("reportElement");
 		line.appendChild(rptElementLine);
 		rptElementLine.setAttribute("x", Integer.toString(x));
 		rptElementLine.setAttribute("y", Integer.toString(y));
 		rptElementLine.setAttribute("width", Integer.toString(width));
 		rptElementLine.setAttribute("height", "1");
 		rptElementLine.setAttribute("forecolor", "#999999");
 		
 		return line;
 		
 	}
 
 	/**
 	 * create child "textField" of "band" with attributes
 	 */
 	private Node buildSummaryNodes(ReportField f, String calc, String resetGroup, Document document, int x, int width, int y, int height) {
 		
 		String varName = null;
 		String valueClassName = null;
 		String pattern = null;
 		//set the field data type
 		if (calc.compareToIgnoreCase("count") == 0){
 			valueClassName = Long.class.getName();
 			pattern ="";
 		}else{
 			switch (f.getFieldType()) {
 			case NONE:   	valueClassName = String.class.getName(); pattern ="";	 		break;
 			case STRING:   	valueClassName = String.class.getName(); pattern ="";			break;
 			case INTEGER:   valueClassName = Long.class.getName(); 	 pattern ="";	 		break;
 			case DOUBLE:   	valueClassName = String.class.getName(); pattern ="";	 		break;
 			case DATE:   	valueClassName = Date.class.getName();   pattern ="MM/dd/yyyy";	break;
 			case MONEY:   	valueClassName = Float.class.getName();  pattern ="$ 0.00";		break;
 			case BOOLEAN:   valueClassName = Boolean.class.getName();pattern ="";	
 			}
 		}
 		
 		
 		
 		if (resetGroup.compareToIgnoreCase("global_column_0") == 0)
 			varName = "variable-footer_global_" + f.getColumnName()+ "_" + calc;	
 		else
 			varName = "variable-footer_" + resetGroup + "_" + f.getColumnName() + "_" + calc;
 		Element textField = document.createElement("textField");
 		textField.setAttribute("isStretchWithOverflow", "true");
 		textField.setAttribute("evaluationTime", "Group");
 		textField.setAttribute("evaluationGroup", resetGroup);
 		textField.setAttribute("pattern", pattern);
 		//band.appendChild(textField);
 		
 			//create child "reportElement" of "textField" with attr
 			Element reportElement = document.createElement("reportElement");
 			reportElement.setAttribute("key", varName);
 			//reportElement.setAttribute("style", "dj_style_1");
 			reportElement.setAttribute("positionType", "Float");
 			reportElement.setAttribute("stretchType", "RelativeToTallestObject");
 			reportElement.setAttribute("x", Integer.toString(x));
 			reportElement.setAttribute("y",  Integer.toString(y));
 			reportElement.setAttribute("width", Integer.toString(width));
 			reportElement.setAttribute("height", Integer.toString(height));
 //			reportElement.setAttribute("style", "SummaryStyle");
 			textField.appendChild(reportElement);
 			
 			//create child "textElement" of "textField" (it's empty)
 			Element textElement = document.createElement("textElement");
 			textField.appendChild(textElement);
 			
 			////create child "textFieldExpression" of "textField" with attr
 			Element textFieldExpression = document.createElement("textFieldExpression");
 			textFieldExpression.setAttribute("class", valueClassName);
 			textFieldExpression.appendChild(document.createCDATASection("$V{" + varName + "}"));
 			textField.appendChild(textFieldExpression);
 		
 		return textField;
 	}
 	/**
 	 * Adds the static text labels to the report.
 	 */
	private Node buildSummaryLabel(String calc, Document document, int x, int y, int width, int height, Boolean groupHeaderFlag, String groupColumn) {
		Element textField2 = document.createElement("textField");
 		
 			Element reportElement2 = document.createElement("reportElement");
 			reportElement2.setAttribute("key", "global_legend_footer_" + calc);
 			//reportElement2.setAttribute("style", "dj_style_2");
 			reportElement2.setAttribute("x", Integer.toString(x));
 			reportElement2.setAttribute("y", Integer.toString(y));
 			reportElement2.setAttribute("width", Integer.toString(width));
 			reportElement2.setAttribute("height", Integer.toString(height));
 
 			textField2.appendChild(reportElement2);
 			
 			//create child "textElement" of "textField" (it's empty)
 			Element textElement2 = document.createElement("textElement");
 			textField2.appendChild(textElement2);
 			
 			////create child "textFieldExpression" of "textField" with attr
 			Element textFieldExpression2 = document.createElement("textFieldExpression");
			textFieldExpression2.setAttribute("class", "java.lang.String");
 			if (groupHeaderFlag){
 				textFieldExpression2.appendChild(document.createCDATASection("$F{" + groupColumn + "}"));
 				//reportElement2.setAttribute("style", "SummaryStyle");
 			}
 			else{
 				textFieldExpression2.appendChild(document.createCDATASection("\"" + calc + "\""));
 				//reportElement2.setAttribute("style", "SummaryStyleBlue");
 			}
 			textField2.appendChild(textFieldExpression2);
 		
 		return textField2;
 	}
 	
 	/**
 	 * Adds a variable to the JRXML/Jasper Report to store the specified calculation.
 	 * 
 	 * @param columnName Used in the variable name.
 	 * @param calc  Calculation to be performed.
 	 * @param resetGroup Name of the group that the calculation will be performed on.
 	 * @param document XML document.
 	 * @return Element
 	 */
 	private Node buildVariableNode(ReportField f, String calc, String resetGroup, Document document ) {
 		String varName = null;
 		String columnName = f.getColumnName();
 		String valueClassName = null;
 		String initialize = "()";
 		//set the field data type
 		if (calc.compareToIgnoreCase("count") == 0){
 			valueClassName = Long.class.getName();
 			initialize = "(\"0\")";
 		}else{
 			switch (f.getFieldType()) {
 			case NONE:   	valueClassName = String.class.getName(); break;
 			case STRING:   	valueClassName = String.class.getName(); break;
 			case INTEGER:   valueClassName = Long.class.getName(); 	 initialize = "(\"0\")"; break;
 			case DOUBLE:   	valueClassName = String.class.getName(); initialize = "(\"0\")"; break;
 			case DATE:   	valueClassName = Date.class.getName();   break;
 			case MONEY:   	valueClassName = Float.class.getName();  initialize = "(\"0\")"; break;
 			case BOOLEAN:   valueClassName = Boolean.class.getName();
 			}
 		}
 		int test1 = new Integer(3);
 		long test = new Long(0);
 		if (resetGroup.compareToIgnoreCase("global_column_0") == 0)
 			varName = "variable-footer_global_" + columnName + "_" + calc;	
 		else
 			varName = "variable-footer_" + resetGroup + "_" + columnName + "_" + calc;
 		
 		Element variable = document.createElement("variable");
 		variable.setAttribute("name", varName);
 		variable.setAttribute("class", valueClassName);
 		variable.setAttribute("resetType", "Group");
 		variable.setAttribute("resetGroup", resetGroup);
 		variable.setAttribute("calculation", calc);
 		
 		Element variableExpression = document.createElement("variableExpression");
 		variableExpression.appendChild(document.createCDATASection("$F{" + columnName + "}"));
 		variable.appendChild(variableExpression);
 		
 		Element initialValueExpression = document.createElement("initialValueExpression");
 		initialValueExpression.appendChild(document.createCDATASection("new " + valueClassName + initialize));
 		variable.appendChild(initialValueExpression);
 		
 		return variable;
 	}
 
 	/**
 	 * Removes the chart from the group created by dynamic jasper and 
 	 * puts it in the title or lastPageFooter section of the report.
 	 * 
 	 * @param fileName file name of the XML document
 	 * @param chartType The type of chart. Currently we only support Bar and Pie.
 	 * @param location The location you want to put the chart.  (header or footer)
 	 */
 	public void moveChartFromGroup(String fileName, String chartType, String location) throws ParserConfigurationException, SAXException, IOException{
 		//Find the chart node copy it 
 		String chart = null;
 		if (chartType.compareToIgnoreCase("bar") == 0)
 			chart = "barChart";
 		else if (chartType.compareToIgnoreCase("pie") == 0)
 			chart = "pieChart";
 		else
 			chart = null;
 		if (chart != null){
 			Document document = loadXMLDocument(fileName);
 			Node chartNode = document.getElementsByTagName(chart).item(0);
 			if (chartNode != null){
 				Element chartElement = (Element) chartNode;
 				
 				Element chartRptElement = (Element) chartElement.getElementsByTagName("reportElement").item(0);	
 				//Add the printwhenexpression to the chart report element if it goes in the header
 				if (location.compareToIgnoreCase("header") == 0){
 					Element printWhenExpression = document.createElement("printWhenExpression");
 					printWhenExpression.appendChild(document.createCDATASection("new java.lang.Boolean(((Number)$V{PAGE_NUMBER}).doubleValue() == 1)"));
 					chartRptElement.appendChild(printWhenExpression);
 				}
 				
 				//change the band height back to 0
 				Element band = (Element) chartElement.getParentNode();
 				Integer bandHeight = Integer.decode(band.getAttribute("height"));
 				band.setAttribute("height", "0");
 				
 				//remove the group footer band node from the group added for the chart
 				Element chartGroup = (Element) band.getParentNode().getParentNode();
 				Node groupFooter = (Element) chartGroup.getElementsByTagName("groupFooter").item(0);
 				Node groupFooterBand = (Element) ((Element) groupFooter).getElementsByTagName("band").item(0);
 				groupFooter.replaceChild((Node) document.createElement("band"), groupFooterBand);
 				
 				//remove the chart node
 				removeAll(document, Node.ELEMENT_NODE, chart);
 				
 				//add the copied chart node to the title band or the last page footer
 				String position = null;
 				if (location.compareTo("header") == 0)
 					position = "title";
 				else
 					position = "lastPageFooter";
 				
 				Element positionNode = (Element) document.getElementsByTagName(position).item(0);
 				Element positionBandNode = (Element) positionNode.getElementsByTagName("band").item(0);
 				Integer positionBandHeight = Integer.decode(positionBandNode.getAttribute("height"));
 				
 				// set the y value on the chart depends on the position of the chart
 				//set the new band height to allow room for the chart
 				if (position.compareTo("title") == 0){
 					chartRptElement.setAttribute("y", positionBandHeight.toString());	
 					Integer newpositionNodeBandHeight = positionBandHeight + bandHeight;
 					positionBandNode.setAttribute("height", newpositionNodeBandHeight.toString());
 					
 				}
 				else{
 					//change the y for the other elements in the last page footer so the chart is above them
 					NodeList rptElementsInLastPgFt = positionBandNode.getElementsByTagName("reportElement");
 					Integer newpositionNodeBandHeight = positionBandHeight + bandHeight;
 					Integer numberOfRptElements = rptElementsInLastPgFt.getLength();
 					
 					for (int i = 0; i < numberOfRptElements; i++){
 						Integer yRptElement = Integer.decode(((Element) rptElementsInLastPgFt.item(i)).getAttribute("y"));
 						Integer yForOtherElements =  yRptElement + bandHeight;
 						((Element) rptElementsInLastPgFt.item(i)).setAttribute("y", yForOtherElements.toString());
 					}
 					chartRptElement.setAttribute("y", "0");
 					positionBandNode.setAttribute("height", newpositionNodeBandHeight.toString());
 				}
 								
 				positionBandNode.appendChild(chartNode);
 				saveXMLtoFile(fileName, document);	
 			}	
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	public void removeCrossTabDataSubset(String fileName) throws ParserConfigurationException, SAXException, IOException {
 	Document document = loadXMLDocument(fileName);
 	// Remove datasetRun element from the report xml
 	removeAll(document, Node.ELEMENT_NODE, "datasetRun");
 	// Remove detail element from the report xml
 	removeAll(document, Node.ELEMENT_NODE, "detail");
 	// Remove detail element from the report xml
 	removeAll(document, Node.ELEMENT_NODE, "columnHeader");
 	saveXMLtoFile(fileName, document);	     
     }
 
     public static void removeAll(Node node, short nodeType, String name) {
         if (node.getNodeType() == nodeType &&
 	    (name == null || node.getNodeName().equals(name))) {
             node.getParentNode().removeChild(node);
         } else {
             // Visit the children
             NodeList list = node.getChildNodes();
             for (int i=0; i<list.getLength(); i++) {
                 removeAll(list.item(i), nodeType, name);
             }
         }
     }
 
 	private List<ReportField> getSelectedReportFieldsInOrder() {
 		Iterator<ReportSelectedField> itReportSelectedFields = reportWizard.getReportSelectedFields().iterator();
 
 		List<ReportField> selectedReportFieldsList = new LinkedList<ReportField>();
 
 		while (itReportSelectedFields.hasNext()){
 			ReportSelectedField reportSelectedField = (ReportSelectedField) itReportSelectedFields.next();
 			if (reportSelectedField == null) continue;
 			ReportField f = reportFieldService.find(reportSelectedField.getFieldId());
 			if (f == null || f.getId() == -1) continue;
 			f.setAverage(reportSelectedField.getAverage());
 			f.setIsSummarized(reportSelectedField.getIsSummarized());
 			f.setLargestValue(reportSelectedField.getMax());
 			f.setSmallestValue(reportSelectedField.getMin());
 			f.setPerformSummary(reportSelectedField.getSum());
 			f.setSelected(true);
 			selectedReportFieldsList.add(f);
 		}
 		
 		return selectedReportFieldsList; 
 	}
 	
 	/**
 	 * Sets the ReportWizard that contains the various report options.
 	 * @param reportWizard
 	 */
 	public void setReportWizard(ReportWizard reportWizard) {
 		this.reportWizard = reportWizard;
 	}
 
 	/**
 	 * Returns the ReportWizard that contains the various report options.
 	 * @return
 	 */
 	public ReportWizard getReportWizard() {
 		return reportWizard; 
 	}
 
 
 	public void setReportFieldService(ReportFieldService reportFieldService) {
 		this.reportFieldService = reportFieldService;
 	}
 
 
 	public ReportFieldService getReportFieldService() {
 		return reportFieldService;
 	}
 
 
 
 	}
 
 
