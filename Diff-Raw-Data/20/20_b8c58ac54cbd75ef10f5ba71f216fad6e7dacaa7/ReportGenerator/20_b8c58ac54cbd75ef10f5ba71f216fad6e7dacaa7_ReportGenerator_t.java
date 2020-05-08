 package gov.nih.nci.nautilus.ui;
 
 import gov.nih.nci.nautilus.query.CompoundQuery;
 import gov.nih.nci.nautilus.query.QueryCollection;
 import gov.nih.nci.nautilus.resultset.DimensionalViewContainer;
 import gov.nih.nci.nautilus.resultset.Resultant;
 import gov.nih.nci.nautilus.resultset.ResultsContainer;
 import gov.nih.nci.nautilus.resultset.ResultsetManager;
 import gov.nih.nci.nautilus.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.nautilus.resultset.copynumber.CytobandResultset;
 import gov.nih.nci.nautilus.resultset.copynumber.SampleCopyNumberValuesResultset;
 import gov.nih.nci.nautilus.resultset.gene.DiseaseGroupResultset;
 import gov.nih.nci.nautilus.resultset.gene.GeneExprResultsContainer;
 import gov.nih.nci.nautilus.resultset.gene.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.nautilus.resultset.gene.GeneResultset;
 import gov.nih.nci.nautilus.resultset.gene.ReporterResultset;
 import gov.nih.nci.nautilus.resultset.gene.SampleFoldChangeValuesResultset;
 import gov.nih.nci.nautilus.resultset.gene.ViewByGroupResultset;
 import gov.nih.nci.nautilus.resultset.sample.SampleResultset;
 import gov.nih.nci.nautilus.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.nautilus.view.ClinicalSampleView;
 import gov.nih.nci.nautilus.view.CopyNumberSampleView;
 import gov.nih.nci.nautilus.view.GeneExprDiseaseView;
 import gov.nih.nci.nautilus.view.GeneExprSampleView;
 import gov.nih.nci.nautilus.view.Viewable;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * @author Landyr
  * Date: Nov 3, 2004
  * 
  */
 public class ReportGenerator  {
 	
 //	public static String theColors[] = {"0073E6","FFFF61"};
 	
 	public static final DecimalFormat resultFormat = new DecimalFormat("0.0000");
 		
 	public static String links = "<a href=\"#queryInfo\">[Query Information]</a> | <a href=\"jsp/geneViewReportCSV.jsp\">[Download this report for Excel]</a> | <a href=\"menu.do\">[Back to Menu]</a>\n";
 		
 	public static String displayReport(QueryCollection queryCollection, String[] theColors, boolean csv, HttpServletRequest request)	{
 		
 		StringBuffer html = new StringBuffer();
 		StringBuffer errors = new StringBuffer();
 		Resultant resultant;
 		errors.append("<br><a href=\"menu.do\">[Back to Menu]</a><br><Br>");
 			
 		try	{
 			
 			CompoundQuery myCompoundQuery = queryCollection.getCompoundQuery();
 
 			try	{
 				resultant = ResultsetManager.executeCompoundQuery(myCompoundQuery);
 	  		}
 	  		catch (Throwable t)	{
 	  			errors.append("Error executing the query.<Br><Br>");
 	  			errors.append(t.getStackTrace().toString());
 	  			return errors.toString();
 	  		}
 
 			if(resultant != null) {      
 		 		ResultsContainer  resultsContainer = resultant.getResultsContainer(); 
 		 		
 		 		String theQuery  =  resultant.getAssociatedQuery().toString();
 
 		 		if(resultsContainer != null)	{
 		 			
 			 		Viewable view = resultant.getAssociatedView();
 			 		 
 			 		//4 views here, returning the String of HTML for report
 			 		// need to add the html buffer here
 			 		
 		 			if (view instanceof GeneExprSampleView)	{ 
 		 				html.append("<div class=\"title\">Gene Expression Fold Change (Tumor/Non-tumor)</div>\n");
 		 				html.append(geneExprSampleView(resultsContainer, theColors));
 		 				return html.toString();
 		 			}
 		 			else if (view instanceof CopyNumberSampleView)	{ 
 		 				html.append("<div class=\"title\">Copy Number Data</div>\n");
 		 				html.append(copyNumberSampleView(resultsContainer, theColors));
 		 				return html.toString();
 		 			}
 		 			else if (view instanceof GeneExprDiseaseView)	{
 		 				html.append("<div class=\"title\">Mean Gene Expression Fold Change for Tumor Sub-types</div>\n");
 		 				html.append(geneExprDiseaseView(resultsContainer, theColors));
 		 				return html.toString();
 		 			}
 	 				else if(view instanceof ClinicalSampleView){
 	 					html.append("<div class=\"title\">Sample Report</div>\n");
 	 					html.append(clinicalSampleView(resultsContainer, theColors, request));
 	 					return html.toString();
 	 				}	
 	 				else	{
 						errors.append("Error with report view<Br><Br>");
 						return errors.toString();
 					}
 			 	}
 			 	else	{
 			 		errors.append("<b>No Results Found, Try a Different Query</b><br>\n");
 			 		return errors.toString();
 			 	}
 			 } //resultant != null
 			 else	{
 			 	errors.append("Resultant is NULL<br>\n");
 			 	return errors.toString();
 			 }
 		}
 		
 		catch(Exception e)	{
 			errors.append("Error Displaying the Report.<Br>\n");
 			return errors.toString();
 		}
 		
 	}
 	
 	
 	
 	public static String clinicalSampleView(ResultsContainer resultsContainer, String[] theColors, HttpServletRequest request)	{
 			
 			boolean gLinks = false;
 			boolean cLinks = false;
 			StringBuffer sb = new StringBuffer();
 			System.out.println("HERE IS THE CLINICAL VIEW");
 			SampleViewResultsContainer sampleViewContainer = null;
 			if(resultsContainer instanceof DimensionalViewContainer){
 				
 				DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
 					//	CopyNumberSingleViewResultsContainer copyNumberContainer = dimensionalViewContainer.getCopyNumberSingleViewContainer();
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
 				
 			}else if (resultsContainer instanceof SampleViewResultsContainer){
 				
 				sampleViewContainer = (SampleViewResultsContainer) resultsContainer;
 				
 			}
 			
 			Collection samples = sampleViewContainer.getBioSpecimenResultsets();
 			sb.append("<div class=\"rowCount\">"+samples.size()+" records returned &nbsp;&nbsp;&nbsp;" + links + "</div>\n");
 			sb.append("<table cellpadding=\"0\" cellspacing=\"0\">\n");
 			sb.append("<Tr><Td id=\"header\">SAMPLE</td><td id=\"header\">AGE at Dx</td><td id=\"header\">GENDER</td><td id=\"header\">SURVIVAL</td><td id=\"header\">DISEASE</td>");
  		   	if(gLinks)
  		   		sb.append("<Td id=\"header\">GeneExp</td>");
  		   	if(cLinks)
  		   		sb.append("<td id=\"header\">CopyNumber</td>");
  		   	sb.append("</tr>\n");
    			for (Iterator sampleIterator = samples.iterator(); sampleIterator.hasNext();) {
 
    				SampleResultset sampleResultset =  (SampleResultset)sampleIterator.next();
    	   			String sampleName = sampleResultset.getBiospecimen().getValue().toString();
 	   			sb.append("<tr><td>"+sampleResultset.getBiospecimen().getValue().toString().substring(2)+ "</td>" +
    					"<Td>"+sampleResultset.getAgeGroup().getValue()+ "</td>" +
 					"<td>"+sampleResultset.getGenderCode().getValue()+ "</td>" +
 					"<td>"+sampleResultset.getSurvivalLengthRange().getValue()+ "</td>" +
 					"<Td>"+sampleResultset.getDisease().getValue() + "</td>");
 	   			if(gLinks)	{
	   				sb.append("<td><a href=\"report.do?s="+sampleName+"_gene&report=gene\">G</a></td>");
	   				request.getSession(true).setAttribute( sampleName+"_gene", sampleResultset.getGeneExprSingleViewResultsContainer() );
	   				
 	   			}
 		   		
 	   			//	sb.append("<td><a href=\"report.do?s="+ sampleResultset.getBiospecimen().getValue().toString() +"\">G</a></td>");
 	   			if(cLinks)	{
					request.getSession(true).setAttribute( sampleName+"_copy", sampleResultset.getCopyNumberSingleViewResultsContainer() );
	   				sb.append("<Td><a href=\"report.do?s="+sampleName +"_copy&report=copy\">C</a></td>");
 	   			}
 
 	   			sb.append("</tr>\n");
     		}
     		sb.append("</table>\n<br>");
     		return sb.toString();
 	}
 	
 	
 	public static String geneExprDiseaseView(ResultsContainer resultsContainer, String[] theColors)	{
 		
 		StringBuffer sb = new StringBuffer();
 		GeneExprResultsContainer geneExprDiseaseContainer = (GeneExprResultsContainer) resultsContainer;
 		StringBuffer css = new StringBuffer();
 		int recordCount = 0;
 					if(geneExprDiseaseContainer != null)	{
 				    	Collection genes = geneExprDiseaseContainer.getGeneResultsets();
 				    	Collection labels = geneExprDiseaseContainer.getGroupsLabels();
 				    	Collection sampleIds = null;
 
 				    	/*
 				    	StringBuffer header = new StringBuffer();
 				    	StringBuffer sampleNames = new StringBuffer();
 				        StringBuffer stringBuffer = new StringBuffer();
 				        */
 				        
 				    	//get group size (as Disease or Agegroup )from label.size
 				        String label = null;
 				        
 				    	sb.append("<table cellpadding=\"0\" cellspacing=\"0\">\n");
 				    	
 				        //set up the header for the table
 				    	sb.append("<tr><Td id=\"header\">Gene</td><td id=\"header\">Reporter</td>");
 					   
 				    	ArrayList cssLabels = new ArrayList();
 				    	
 				    	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 				        	label = (String) labelIterator.next();
 				        	sb.append("<Td id=\"header\" class=\""+label+"\">"+label+"</td>");
 				        	cssLabels.add(label);
 				    	}
 			
 						sb.append("</tr>\n");
 						
 						css.append("<style>\n");
 						String color = "";
 						String font = "";
 						
 						for (int i = 0; i < cssLabels.size(); i++) {
 							int currentColor = i;
 							if(currentColor < theColors.length)	{
 								color = theColors[currentColor];	
 							}
 							else	{
 							 currentColor = i - theColors.length;
 							 color = theColors[currentColor];
 							}
 							css.append("td."+(String)(cssLabels.get(i))+ " { background-color: #"+color+"; }\n");
 						}
 						css.append("</style>\n");
 						
 				    	for (Iterator geneIterator = genes.iterator(); geneIterator.hasNext();) {
 				    		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
 				    		String geneSymbol = geneResultset.getGeneSymbol().getValue().toString();
 				    		Collection reporters = geneExprDiseaseContainer.getRepoterResultsets(geneSymbol); 
 
 				    		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 				    			recordCount += reporters.size();
 				    			
 				        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 				        		String reporterName = reporterResultset.getReporter().getValue().toString();
 				        		Collection groupTypes = geneExprDiseaseContainer.getGroupByResultsets(geneSymbol,reporterName); //reporterResultset.getGroupResultsets();
 				        	//	stringBuffer = new StringBuffer();
 							
 							/*
 				        		if(reporterName.length()< 10){ //Remove this from table
 				        			reporterName= reporterName+"        ";
 				        			reporterName = reporterName.substring(0,10);
 				        		}
 				        	*/
 				        		
 				        		sb.append("<tr><td>"+geneSymbol+"</td><td>" + reporterName + "</td>");
 				        		for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 				    	        	label = (String) labelIterator.next();
 				    	        	DiseaseGroupResultset diseaseResultset = (DiseaseGroupResultset) reporterResultset.getGroupByResultset(label);
 				    	        	if(diseaseResultset != null){
 			                   			Double ratio = (Double)diseaseResultset.getFoldChangeRatioValue().getValue();
 			                   			Double pvalue = (Double)diseaseResultset.getRatioPval().getValue();
 			                   			sb.append("<td class=\""+label+"\">"+resultFormat.format(ratio)+" ("+resultFormat.format(pvalue)+")"+"</td>");  
 			                   			}
 			                   		else	{
 			                   			sb.append("<Td class=\""+label+"\">-</td>");
 			                   		}
 				    	    	}
 	   	                   		sb.append("</tr>");
 				    		}
 				    		// add the line between genes
 				    		sb.append("<tr><td colspan=\""+(labels.size() + 2)+"\" class=\"geneSpacerStyle\">&nbsp;</td></tr>\n");
 						    
 				    	}
 					sb.append("</table>\n\n");
 				}
 				else	{
 					sb.append("<Br><br>Gene Disease View container is empty");
 				}
 	
 				return "<div class=\"rowCount\">"+recordCount+" records returned &nbsp;&nbsp;&nbsp;" + links + "</div>\n" + css.toString() + sb.toString();
 	}
 
 
 	public static String copyNumberSampleView(ResultsContainer resultsContainer, String[] theColors)	{
 		
 				StringBuffer sb = new StringBuffer();
 				int recordCount = 0;
 				CopyNumberSingleViewResultsContainer copyNumberContainer = null;
 
 				if(resultsContainer instanceof DimensionalViewContainer)	{
 					DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
 					if(dimensionalViewContainer != null)	{
 						copyNumberContainer = dimensionalViewContainer.getCopyNumberSingleViewContainer();
 					}
 				}
 				else if(resultsContainer instanceof CopyNumberSingleViewResultsContainer)	{ //for single
 					copyNumberContainer = (CopyNumberSingleViewResultsContainer) resultsContainer;
 				}
 				if(copyNumberContainer != null)	{		
 					
 					if(copyNumberContainer.getCytobandResultsets().size() > 0)	{
 		
 						Collection cytobands = copyNumberContainer.getCytobandResultsets();
 				    	Collection labels = copyNumberContainer.getGroupsLabels();
 				    	Collection sampleIds = null;
 				    	
 				    	StringBuffer header = new StringBuffer();
 				    	StringBuffer sampleNames = new StringBuffer();
 				        StringBuffer stringBuffer = new StringBuffer();
 				        			        
 				        ArrayList cssLabels = new ArrayList(); //create the CSS dynamically
 				        
 				        //set up the header for the table
 				        sampleNames.append("<Tr>");
 				    	sampleNames.append("<Td>&nbsp;</td><Td>&nbsp;</td>");
 				    	
 				    	header.append("<tr>");
 				    	header.append("<Td id=\"header\">Cytoband</td><td id=\"header\">Reporter</td>");
 					   
 				    	int theColspan = 2; // the 2 <Td>'s above
 				    	
 				    	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 				        	String label = (String) labelIterator.next();
 				        	
 				        	sampleIds = copyNumberContainer.getBiospecimenLabels(label); 
 				        	header.append("<Td colspan='"+sampleIds.size()+"' class=\""+label+"\" id=\"header\">"+label+"</td>"); 
 		  		        	cssLabels.add(label);
 		  		        	   	
 					           	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 					            	sampleNames.append("<td class=\""+label+"\" id=\"header\">" + sampleIdIterator.next().toString().substring(2)+"</td>"); 
 					            	theColspan += sampleIds.size();
 					           	}
 				    	}
 				    	header.append("</tr>\n"); 
 				    	sampleNames.append("</tr>\n");
 				    	
 				    	//generate the CSS once we have all the labels
 						StringBuffer css = new StringBuffer();
 						css.append("<style>\n");
 						String color = "";
 						String font = "";
 						for (int i = 0; i < cssLabels.size(); i++) {
 								int currentColor = i;
 								if(currentColor < theColors.length)	{
 									color = theColors[currentColor];	
 								}
 								else	{
 								 currentColor = i - theColors.length;
 								 color = theColors[currentColor];
 								}
 								css.append("td."+(String)(cssLabels.get(i))+ " { background-color: #"+color+"; }\n");
 							}
 						css.append("</style>\n");
 						
 						sb.append(css.toString());
 						
 						sb.append("<table cellpadding=\"0\" cellspacing=\"0\">\n");
 						sb.append(header.toString());
 						sb.append(sampleNames.toString());
 						
 				    	for (Iterator cytobandIterator = cytobands.iterator(); cytobandIterator.hasNext();) {
 				    		CytobandResultset cytobandResultset = (CytobandResultset)cytobandIterator.next();
 				    		String cytoband = cytobandResultset.getCytoband().getValue().toString();
 				    		Collection reporters = copyNumberContainer.getRepoterResultsets(cytoband); 
 				    		recordCount += reporters.size();
 				        	for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 				        		
 				        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 				        		String reporterName = reporterResultset.getReporter().getValue().toString();
 				        		Collection groupTypes = copyNumberContainer.getGroupByResultsets(cytoband,reporterName); 
 				        		//stringBuffer = new StringBuffer();
 				        		
 								/*
 				        		if(reporterName.length()< 10){ //Remove this from table
 				        			reporterName= reporterName+"        ";
 				        			reporterName = reporterName.substring(0,10);
 				        		}
 				        		*/
 				        		sb.append("<tr><td>"+cytoband+"</td><td>"+reporterName+"</td>");
 				        		for (Iterator groupIterator = groupTypes.iterator(); groupIterator.hasNext();) {
 				        			ViewByGroupResultset groupResultset = (ViewByGroupResultset)groupIterator.next();
 				        			String label = groupResultset.getType().getValue().toString();
 				        			sampleIds = copyNumberContainer.getBiospecimenLabels(label);
 				                     	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 				                       		String sampleId = (String) sampleIdIterator.next();
 				                       		SampleCopyNumberValuesResultset sampleResultset2 = (SampleCopyNumberValuesResultset) groupResultset.getBioSpecimenResultset(sampleId);
 				                       		if(sampleResultset2 != null){
 				                       			Double ratio = (Double)sampleResultset2.getCopyNumber().getValue();
 				                       			sb.append("<td class='"+label+"'>"+resultFormat.format(ratio)+"</td>");  
 				                       			}
 				                       		else 
 				                       		{
 				                       			sb.append("<td class='"+label+"'>-</td>");
 				                       		}
 				                       	}
 				         		}
 				        		sb.append("</tr>");
 				    		}
 				        	//append the extra row here
 				        	sb.append("<tr><td colspan=\""+theColspan+"\" class=\"geneSpacerStyle\">&nbsp;</td></tr>\n");
 				    	}
 					sb.append("</table><Br><br>");	
 				}
 				
 				else	{
 					sb.append("<br><br>Copy Number container is empty");
 				}
 				
 			}
 			else	{
 				sb.append("<br><br>Copy Number container is empty");
 			}	
 				
 			return "<div class=\"rowCount\">"+recordCount+" records returned &nbsp;&nbsp;&nbsp;" + links + "</div>\n" + sb.toString();
 				
 	}
 
 
 	public static String geneExprSampleView(ResultsContainer resultsContainer, String[] theColors)	{
 				GeneExprSingleViewResultsContainer geneViewContainer = null;
 				StringBuffer sb = new StringBuffer();
 				int recordCount = 0;
 				if(resultsContainer instanceof DimensionalViewContainer)	{
 					DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
 					if(dimensionalViewContainer != null)	{
 						geneViewContainer = dimensionalViewContainer.getGeneExprSingleViewContainer();
 					}
 				}
 				else if(resultsContainer instanceof GeneExprSingleViewResultsContainer)	{ //for single
 					geneViewContainer = (GeneExprSingleViewResultsContainer) resultsContainer;
 				}
 				if(geneViewContainer != null)	{
 			    	Collection genes = geneViewContainer.getGeneResultsets();
 			    	Collection labels = geneViewContainer.getGroupsLabels();
 			    	Collection sampleIds = null;
 		
 			    	StringBuffer header = new StringBuffer();
 			    	
 			    	header.append("<table cellpadding=\"0\" cellspacing=\"0\">\n<tr>\n");
 			    	StringBuffer sampleNames = new StringBuffer();
 			        StringBuffer stringBuffer = new StringBuffer();
 			    	
 			        //set up the header for the table
 			    	header.append("<Td id=\"header\">Gene</td>\n<td id=\"header\">Reporter</td>\n");
 			    	sampleNames.append("<tr><Td> &nbsp;</td><Td> &nbsp;</tD>"); 
 				   
 			    	int theColspan = 2; // the 2 <Td>'s above
 			    	
 					ArrayList cssLabels = new ArrayList();
 				   
 			    	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 			        	String label = (String) labelIterator.next();
 			        	sampleIds = geneViewContainer.getBiospecimenLabels(label);    	
 				    	theColspan += sampleIds.size();
 			        	header.append("<td colspan="+sampleIds.size()+" class='"+label+"' id=\"header\">"+label+"</td>"); 
 				    	cssLabels.add(label);
 				    	
 				           	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 				            	sampleNames.append("<td class='"+label+"' id=\"header\">"+sampleIdIterator.next().toString().substring(2)+"</td>"); 
 				            	header.append("\t");
 				           	}
 			           	header.deleteCharAt(header.lastIndexOf("\t"));
 			    	}
 			    	sampleNames.append("</tr>");
 			    	header.append("</tr>"); 
 			    	
 					//generate the CSS once we have all the labels
 					StringBuffer css = new StringBuffer();
 					css.append("<style>\n");
 					String color = "";
 					String font = "";
 					
 					for (int i = 0; i < cssLabels.size(); i++) {
 					/*
 						if(i%2 == 0)
 						{
 							color = theColors[1];
 							font = theColors[0];
 						}
 						else
 						{
 							color = theColors[0];
 							font = theColors[1];
 						}
 					
 						css.append("td."+(String)(cssLabels.get(i))+ " { background-color: #"+color+"; color: #"+font+" }\n");
 					*/	
 						
 						int currentColor = i;
 						
 						if(currentColor < theColors.length)	{
 							color = theColors[currentColor];	
 						}
 						else	{
 						 currentColor = i - theColors.length;
 						 color = theColors[currentColor];
 						}
 
 						//font = "000000";
 						//css.append("td."+(String)(cssLabels.get(i))+ " { background-color: #"+color+"; color: #"+font+" }\n");
 					    css.append("td."+(String)(cssLabels.get(i))+ " { background-color: #"+color+"; }\n");
 					}
 					css.append("</style>\n");
 					
 					
 					sb.append(css.toString());
 			    	sb.append(header.toString());
 					sb.append(sampleNames.toString());
 		
 		    		//String geneBreak = " style=\"border-bottom: 1px solid black\"";
 		    		
 					
 			    	for (Iterator geneIterator = genes.iterator(); geneIterator.hasNext();) {
 			    		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
 			    		Collection reporters = geneResultset.getReporterResultsets();
 			    		
 			    		recordCount+=reporters.size();
 			    		
 			    		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 			        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 			        		Collection groupTypes = reporterResultset.getGroupByResultsets();
 			        	//	stringBuffer = new StringBuffer();
 			        		String reporterName = reporterResultset.getReporter().getValue().toString();
 
 			        		sb.append("<tr><td>"+geneResultset.getGeneSymbol().getValueObject().toString()+"</td><td>"+
 			    					reporterName+"</td>");
 			        		for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 			        			String label = (String) labelIterator.next();
 			        			ViewByGroupResultset groupResultset = (ViewByGroupResultset) reporterResultset.getGroupByResultset(label);
 			        			
 				        			sampleIds = geneViewContainer.getBiospecimenLabels(label);
 				        			if(groupResultset != null)
 			        				{
 				                     	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 				                       		String sampleId = (String) sampleIdIterator.next();
 				                       		SampleFoldChangeValuesResultset biospecimenResultset = (SampleFoldChangeValuesResultset) groupResultset.getBioSpecimenResultset(sampleId);
 				                       		if(biospecimenResultset != null){
 				                       			Double ratio = (Double)biospecimenResultset.getFoldChangeRatioValue().getValue();
 				                       			if(ratio != null)
 					                       			sb.append("<Td class='"+label+"'>"+resultFormat.format(ratio)+" </td>");                                 
 					                       		else
 					                      			sb.append("<td class='"+label+"'>-</td>");
 				                       		}
 				                       		else 
 				                       		{
 				                       			sb.append("<td class='"+label+"'>-</td>");
 				                       		}
 				                       	}
 			                       }
 			                       else	{
 			                       for(int s=0;s<sampleIds.size();s++) 
 			                        	sb.append("<td class='"+label+"'>-</td>");                      
 			                       }
 			
 			         		}
 			         		
 			        		sb.append("</tr>\n");
 			    		}
 			    		// add the line between genes
 			    		sb.append("<tr><td colspan=\""+theColspan+"\" class=\"geneSpacerStyle\">&nbsp;</td></tr>\n");
 			    	}
 						sb.append("</table>");
 				}
 				else {
 					sb.append("<br><Br>Gene Container is empty<br>");
 				}
 			    
 			    return "<div class=\"rowCount\">"+recordCount+" records returned &nbsp;&nbsp;&nbsp;" + links + "</div>\n" + sb.toString();
 	
 		
 	}
 
 }
