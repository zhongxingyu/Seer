 package gov.nih.nci.rembrandt.web.xml;
 
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.rembrandt.util.DEUtils;
 
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
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
 	public Document getReportXML(Resultant resultant, Map filterMapParams) {
 
 		//String theColors[] = { "B6C5F2","F2E3B5","DAE1F9","C4F2B5","819BE9", "E9CF81" };
 		DecimalFormat resultFormat = new DecimalFormat("0.0000");
 		String defaultV = "-";
 		
 			Document document = DocumentHelper.createDocument();
 
 			try	{
 			Element report = document.addElement( "Report" );
 			Element cell = null;
 			Element data = null;
 			Element dataRow = null;
 			//add the atts
 	        report.addAttribute("reportType", "Clinical");
 	        //fudge these for now
 	        report.addAttribute("groupBy", "none");
 	        String queryName = resultant.getAssociatedQuery().getQueryName();
 	        
 	        //set the queryName to be unique for session/cache access
 	        report.addAttribute("queryName", queryName);
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
 			
 			Collection samples = sampleViewContainer.getSampleResultsets();
 			/*
 			sb.append("<div class=\"rowCount\">"+helpFul+samples.size()+" records returned &nbsp;&nbsp;&nbsp;" + links + "</div>\n");
 			sb.append("<table cellpadding=\"0\" cellspacing=\"0\">\n");
 			*/
 			
 			//	set up the headers for this table 
 			Element headerRow = report.addElement("Row").addAttribute("name", "headerRow");
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Sample");
 				        data = null;
 			        cell = null;
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Age at Dx (years)");
 				        data = null;
 			        cell = null;
 					cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Gender");
 				        data = null;
 			        cell = null;
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Survival (months)");
 				        data = null;
 			        cell = null;
 					cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Disease");
 				        data = null;
 			        cell = null;
 			        
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 		        	data = cell.addElement("Data").addAttribute("type", "header").addText("Grade");
 		        	data = null;
 	        	   cell = null;
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Race");
 			        	data = null;
 		        	cell = null;
 		        	
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Karnofsky");
 			        	data = null;
 		        	cell = null;
 		        	
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Lansky");
 			        	data = null;
 		        	cell = null;*/
 		        	
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Neurological Exam Outcome");
 			        	data = null;
 		        	cell = null;
 		        	
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("MRI Desc");
 			        	data = null;
 		        	cell = null;
 		        	
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Clinical Evaluation Time Point");
 			        	data = null;
 		        	cell = null;
 		        	
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Followup Date");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Followup Month");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Neuro Evaluation Date");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Steroid Dose Status");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Anti-Convulsant Status");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Radiation Time Point");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Radiation Site");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 /*cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Radiation Dose Start Date");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Radiation Dose Stop Date");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Radiation Fraction Dose");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Radiation Fraction Number");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Radiation Type");
 			        	data = null;
 		        	 cell = null;		        	 
 		        	
 		        	 
 		        	 /*cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Chemo Time Point");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Chemo Agent ID");
 			        	data = null;
 		        	 cell = null;
 		        	 */
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Chemo Agent Name");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Chemo Course Count");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Chemo Dose Start Date");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Chemo Dose Stop Date");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Chemo Study Source");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Chemo Protocol Number");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Surgery Time Point");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Surgery Procedure Title");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Surgery Tumor Histology");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Surgery Date");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("Prior Therapy Surgery Outcome");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 // starting onstudy areas
 		        	 
 		        	 
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Radiation Time Point");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Radiation Site");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Radiation Dose Start Date");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Radiation Dose Stop Date");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Radiation Neurosis Status");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Radiation Fraction Dose");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Radiation Fraction Number");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Radiation Type");
 			        	data = null;
 		        	 cell = null;		        	 
 		        	
 		        	 
 		        	 /*cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Time Point");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Agent ID");
 			        	data = null;
 		        	 cell = null;
 		        	 */
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Agent Name");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	/* cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Regimen Number");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Course Count");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 /*cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Dose Start Date");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Dose Stop Date");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Study Source");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Chemo Protocol Number");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Surgery Time Point");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Surgery Procedure Title");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Surgery Indication ");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Surgery Histo Diagnosis ");
 			        	data = null;
 		        	 cell = null;
 		        	 
 		        	 
 		        	 
 		        	 /*cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Surgery Date");
 			        	data = null;
 		        	 cell = null;*/
 		        	 
 		        	 cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText("OnStudy Therapy Surgery Outcome");
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
 				
 				dataRow = report.addElement("Row").addAttribute("name", "dataRow");
 					        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "sample").addAttribute("group", "sample");
    					        	data = cell.addElement("Data").addAttribute("type", "data").addText(sampleResultset.getSampleIDDE().getValue().toString());
    					        	data = null;
    					        cell = null;
 							cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        	data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNV(sampleResultset.getAgeGroup()));
    					        	data = null;
    					        cell = null;
    					        String theGender = defaultV;
    					        if(!DEUtils.checkNV(sampleResultset.getGenderCode()).equalsIgnoreCase("O"))
    					        	theGender = DEUtils.checkNV(sampleResultset.getGenderCode());
 							cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
 						    	data = cell.addElement("Data").addAttribute("type", "data").addText(theGender);
    					        	data = null;
    					        cell = null;
 							cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
 						    	data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNV(sampleResultset.getSurvivalLengthRange()));
    					        	data = null;
    					        cell = null;
 							cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
 								data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNV(sampleResultset.getDisease()));
    					        	data = null;
    					        cell = null;
    					        
    					        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
 					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getWhoGrade()));
 			        		data = null;
 			        	   cell = null;
    					        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
 	   					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNV(sampleResultset.getRaceDE()));
 				        		data = null;
 				        	cell = null;
 				        	
 				        	cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
 	   					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNV(sampleResultset.getKarnofskyClinicalEvalDE()));
 				        		data = null;
 				        	cell = null;
 				        	
 				        	/*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
 	   					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNV(sampleResultset.getLanskyClinicalEvalDE()));
 				        		data = null;
 				        	cell = null;*/
 				        	
 				        	/*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNV(sampleResultset.getNeuroExamClinicalEvalDE()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNV(sampleResultset.getMriClinicalEvalDE()));
 			        		data = null;
 			        	    cell = null;*/
 				        	
 				        	cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getNeuroExamDescs()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getMriScoreDescs()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	  /*  cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getTimePoints()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getFollowupDates()));
 			        		data = null;
 			        	    cell = null;
 			        	    */
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getFollowupMonths()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getNeuroEvaluationDates()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getSteroidDoseStatuses()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getAntiConvulsantStatuses()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	   /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorRadiationTimePoints()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorRadiationRadiationSites()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	   /* cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorRadiationDoseStartDates()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorRadiationDoseStopDates()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorRadiationFractionDoses()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorRadiationFractionNumbers()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorRadiationRadiationTypes()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorChemoTimePoints()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorChemoagentIds()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorChemoAgentNames()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorChemoCourseCounts()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorChemoDoseStartDates()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorChemoDoseStopDates()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorChemoStudySources()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorChemoProtocolNumbers()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorSurgeryTimePoints()));
 			        		data = null;
 			        	    cell = null;
 			        	    */
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorSurgeryProcedureTitles()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorSurgeryTumorHistologys()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorSurgerySurgeryDates()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getPriorSurgerySurgeryOutcomes()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    
 			        	    // starting onstudy
 			        	    
 			        	    
 			        	   /* cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyRadiationTimePoints()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyRadiationRadiationSites()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyRadiationDoseStartDates()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyRadiationDoseStopDates()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyRadiationFractionDoses()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyRadiationFractionNumbers()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyRadiationNeurosisStatuses()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyRadiationRadiationTypes()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoTimePoints()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoagentIds()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoAgentNames()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoRegimenNumbers()));
 			        		data = null;
 			        	    cell = null;*/
 			        	    
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoCourseCounts()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoDoseStartDates()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoDoseStopDates()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoStudySources()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudyChemoProtocolNumbers()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudySurgeryTimePoints()));
 			        		data = null;
 			        	    cell = null;
 			        	    */
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudySurgeryProcedureTitles()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudySurgeryIndications()));
 			        		data = null;
 			        	    cell = null;			        	    
 			        	  
 			        	    
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudySurgeryHistoDiagnoses()));
 			        		data = null;
 			        	    cell = null;
 			        	    
 			        	    /*cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudySurgerySurgeryDates()));
 			        		data = null;
 			        	    cell = null;
 			        	    */
 			        	    cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    					        data = cell.addElement("Data").addAttribute("type", "data").addText(DEUtils.checkNull(sampleResultset.getOnStudySurgerySurgeryOutcomes()));
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
	   			/*
 		   		else if (gLinks){
 	   				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
   						data = cell.addElement("Data").addAttribute("type", "data").addText("-");
    					    data = null;
    					cell = null;
 		   			//sb.append("<td>&nbsp;</td>"); //empty cell
 		   		}
		   		*/
 	   			if(sampleResultset.getCopyNumberSingleViewResultsContainer()!= null)	{
 	   				//	TODO: create the links
 	   				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
    						data = cell.addElement("Data").addAttribute("type", "data").addText("C");
    					    data = null;
    					cell = null;
 	   				//sb.append("<Td><a href=\"report.do?s="+sampleName +"_copy&report=copy\">C</a></td>");
 	   			}
	   			/*
 	   			else if (cLinks){
 	   				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
   						data = cell.addElement("Data").addAttribute("type", "data").addText("-");
    					    data = null;
    					cell = null;
 		   			//sb.append("<td>&nbsp;</td>"); //empty cell
 		   		}
		   		*/
 	   			
 	   			//report.append("row", row);
 	   			//sb.append("</tr>\n");
     		}
     		//sb.append("</table>\n<br>");
     		//return sb.toString(); 
 			}
 			catch(Exception e)	{
 				//asdf
 				System.out.println(e);
 			}
 		    return document;		     
 	}
 
 }
