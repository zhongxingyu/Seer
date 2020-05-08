 package gov.nih.nci.nautilus.ui.report;
 
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.Iterator;
 
 import gov.nih.nci.nautilus.resultset.DimensionalViewContainer;
 import gov.nih.nci.nautilus.resultset.Resultant;
 import gov.nih.nci.nautilus.resultset.ResultsContainer;
 import gov.nih.nci.nautilus.resultset.sample.SampleResultset;
 import gov.nih.nci.nautilus.resultset.sample.SampleViewResultsContainer;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 /**
  * @author LandyR
  * Feb 8, 2005
  * 
  */
 public class ClinicalSampleReport implements ReportGenerator {
 
 	/**
 	 * 
 	 */
 	public ClinicalSampleReport () {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.nautilus.ui.report.ReportGenerator#getTemplate(gov.nih.nci.nautilus.resultset.Resultant, java.lang.String)
 	 */
 	public Document getReportXML(Resultant resultant) {
 
 		//	have setter or put in props file
 		String theColors[] = { "B6C5F2","F2E3B5","DAE1F9","C4F2B5","819BE9", "E9CF81" };
 		DecimalFormat resultFormat = new DecimalFormat("0.0000");
 		
 			Document document = DocumentHelper.createDocument();
 
 			Element report = document.addElement( "Report" );
 			Element cell = null;
 			Element data = null;
 			Element dataRow = null;
 			//add the atts
 	        report.addAttribute("reportType", "Copy Number");
 	        //fudge these for now
 	        report.addAttribute("groupBy", "none");
 	        report.addAttribute("queryName", "the query name");
 	        report.addAttribute("sessionId", "the session id");
 	        report.addAttribute("creationTime", "right now");
 
 		    boolean gLinks = false;
 			boolean cLinks = false;
 			StringBuffer sb = new StringBuffer();
 			
 			ResultsContainer  resultsContainer = resultant.getResultsContainer();
 			SampleViewResultsContainer sampleViewContainer = null;
 			if(resultsContainer instanceof DimensionalViewContainer)	{
 				
 				DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
 						// Are we making hyperlinks?
 						if(dimensionalViewContainer.getGeneExprSingleViewContainer() != null)	{
 							// show the geneExprHyperlinks
 							gLinks = true;						
 						}
 						if(dimensionalViewContainer.getCopyNumberSingleViewContainer() != null)	{
 							// show the copyNumberHyperlinks
 							cLinks = true;
 						}
 
 				sampleViewContainer = dimensionalViewContainer.getSampleViewResultsContainer();
 				
 			}
 			else if (resultsContainer instanceof SampleViewResultsContainer)	{
 				
 				sampleViewContainer = (SampleViewResultsContainer) resultsContainer;
 				
 			}
 			
 			Collection samples = sampleViewContainer.getBioSpecimenResultsets();
 			/*
 			sb.append("<div class=\"rowCount\">"+helpFul+samples.size()+" records returned &nbsp;&nbsp;&nbsp;" + links + "</div>\n");
 			sb.append("<table cellpadding=\"0\" cellspacing=\"0\">\n");
 			*/
 			
 			//	set up the headers for this table 
 			Element headerRow = report.addElement("Row").addAttribute("name", "headerRow");
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("SAMPLE");
 				        data = null;
 			        cell = null;
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("AGE at Dx (years)");
 				        data = null;
 			        cell = null;
 					cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("GENDER");
 				        data = null;
 			        cell = null;
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("SURVIVAL (months)");
 				        data = null;
 			        cell = null;
 					cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("DISEASE");
 				        data = null;
 			        cell = null;
 		    //sb.append("<Tr><Td id=\"header\">SAMPLE</td><td id=\"header\">AGE at Dx (years)</td><td id=\"header\">GENDER</td><td id=\"header\">SURVIVAL (months)</td><td id=\"header\">DISEASE</td>");
  		   	
 		    
 			Iterator si = samples.iterator(); 
 			if(si.hasNext())	{
 				SampleResultset sampleResultset =  (SampleResultset)si.next();
    				if(sampleResultset.getGeneExprSingleViewResultsContainer() != null)	{
 					cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("GeneExp");
 				        data = null;
 			        cell = null;
    					//sb.append("<Td id=\"header\">GeneExp</td>");
    				}
    	 		   	if(sampleResultset.getCopyNumberSingleViewResultsContainer()!= null)	{
 	   	 		   	cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("CopyNumber");
 				        data = null;
 			        cell = null;
    	 		   		//sb.append("<td id=\"header\">CopyNumber</td>");
    	 		   	}
    	 		   	//sb.append("</tr>\n");
 			}
 			
    			for (Iterator sampleIterator = samples.iterator(); sampleIterator.hasNext();) {
 
    				SampleResultset sampleResultset =  (SampleResultset)sampleIterator.next();
    				
    	   			String sampleName = sampleResultset.getBiospecimen().getValue().toString();
 				
 				dataRow = report.addElement("Row").addAttribute("name", "dataRow");
 					        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        	data = cell.addElement("Data").addAttribute("type", "data").addText(sampleResultset.getBiospecimen().getValue().toString().substring(2));
    					        	data = null;
    					        cell = null;
 							cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        	data = cell.addElement("Data").addAttribute("type", "data").addText(sampleResultset.getAgeGroup().getValue().toString());
    					        	data = null;
    					        cell = null;
 							cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        	data = cell.addElement("Data").addAttribute("type", "data").addText(sampleResultset.getGenderCode().getValue().toString());
    					        	data = null;
    					        cell = null;
 							cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
   					        	data = cell.addElement("Data").addAttribute("type", "data").addText(sampleResultset.getSurvivalLengthRange().getValue().toString());
    					        	data = null;
    					        cell = null;
 							cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        	data = cell.addElement("Data").addAttribute("type", "data").addText(sampleResultset.getDisease().getValue().toString());
    					        	data = null;
    					        cell = null;
 		   		/*
    	   			sb.append("<tr><td>"+sampleResultset.getBiospecimen().getValue().toString().substring(2)+ "</td>" +
    					"<Td>"+sampleResultset.getAgeGroup().getValue()+ "</td>" +
 					"<td>"+sampleResultset.getGenderCode().getValue()+ "</td>" +
 					"<td>"+sampleResultset.getSurvivalLengthRange().getValue()+ "</td>" +
 					"<Td>"+sampleResultset.getDisease().getValue() + "</td>");
 				*/
 	   			if(sampleResultset.getGeneExprSingleViewResultsContainer() != null)	{
 	   				//TODO: create the links
 					cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    						data = cell.addElement("Data").addAttribute("type", "data").addText("G");
    					    data = null;
    					cell = null;
 	   				//sb.append("<td><a href=\"report.do?s="+sampleName+"_gene&report=gene\">G</a></td>");
 	   			}
 		   		else if (gLinks){
 	   				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    						data = cell.addElement("Data").addAttribute("type", "data").addText(" ");
    					    data = null;
    					cell = null;
 		   			//sb.append("<td>&nbsp;</td>"); //empty cell
 		   		}
 	   			if(sampleResultset.getCopyNumberSingleViewResultsContainer()!= null)	{
 	   				//	TODO: create the links
 	   				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    						data = cell.addElement("Data").addAttribute("type", "data").addText("C");
    					    data = null;
    					cell = null;
 	   				//sb.append("<Td><a href=\"report.do?s="+sampleName +"_copy&report=copy\">C</a></td>");
 	   			}
 	   			else if (cLinks){
 	   				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    						data = cell.addElement("Data").addAttribute("type", "data").addText(" ");
    					    data = null;
    					cell = null;
 		   			//sb.append("<td>&nbsp;</td>"); //empty cell
 		   		}
 	   			
 	   			//report.append("row", row);
 	   			//sb.append("</tr>\n");
     		}
     		//sb.append("</table>\n<br>");
     		//return sb.toString(); 
 		    return document;		     
 	}
 
 }
