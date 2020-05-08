 package gov.nih.nci.rembrandt.web.xml;
 
 import gov.nih.nci.caintegrator.dto.de.BioSpecimenIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE.GeneSymbol;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.ReporterResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.SampleFoldChangeValuesResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.ViewByGroupResultset;
 import gov.nih.nci.rembrandt.web.helper.FilterHelper;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 /**
  * @author LandyR
  * Feb 8, 2005
  * 
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class GeneExprSampleReport implements ReportGenerator{
 
 	/**
 	 * 
 	 */
 	public GeneExprSampleReport() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.nautilus.ui.report.ReportGenerator#getTemplate(gov.nih.nci.nautilus.resultset.Resultant, java.lang.String)
 	 */
 	
 	public Document getReportXML(Resultant resultant, Map filterMapParams) {
 
 		DecimalFormat resultFormat = new DecimalFormat("0.0000");
 		String delim = " | ";
 		/* testing hardcoded vals - these will be params of this method soon */
 		/*
 		ArrayList g = new ArrayList();
 		g.add("EGFR");
 		g.add("VEGF");
 		String tmp_filter_type = "hide";
 		String tmp_filter_element = "gene";
 
 		HashMap filterMapParams = new HashMap();
 		filterMapParams.put("filter_string", g);
 		filterMapParams.put("filter_type", tmp_filter_type);
 		filterMapParams.put("filter_element", tmp_filter_element);
 		*/
 		
 		ArrayList filter_string = new ArrayList();	// hashmap of genes | reporters | cytobands
 		String filter_type = "show"; 		// show | hide
 		String filter_element = "none"; 	// none | gene | reporter | cytoband
 
 		if(filterMapParams.containsKey("filter_string") && filterMapParams.get("filter_string") != null)
 			filter_string = (ArrayList) filterMapParams.get("filter_string");
 		if(filterMapParams.containsKey("filter_type") && filterMapParams.get("filter_type") != null)		
 			filter_type = (String) filterMapParams.get("filter_type");
 		if(filterMapParams.containsKey("filter_element") && filterMapParams.get("filter_element") != null)		
 			filter_element = (String) filterMapParams.get("filter_element");
 			
 		Document document = DocumentHelper.createDocument();
 
 			Element report = document.addElement( "Report" );
 			Element cell = null;
 			Element data = null;
 			Element dataRow = null;
 			//add the atts
 	        report.addAttribute("reportType", "Gene Expression Sample");
 	        //fudge these for now
 	        report.addAttribute("groupBy", "none");
 	        String queryName = resultant.getAssociatedQuery().getQueryName();
 	        //set the queryName to be unique for session/cache access
 	        report.addAttribute("queryName", queryName);
 	        report.addAttribute("sessionId", "the session id");
 	        report.addAttribute("creationTime", "right now");
 		    //hold a message to display on the report
 	        report.addAttribute("msg", (resultant.isOverLimit() ? "over limit" : ""));
 		    ResultsContainer  resultsContainer = resultant.getResultsContainer();
 		    
 			GeneExprSingleViewResultsContainer geneViewContainer = null;
 			StringBuffer sb = new StringBuffer();
 			
 			//String helpFul = helpLink + "?sect=sample" + helpLinkClose;
 			
 			DimensionalViewContainer dimensionalViewContainer = null;
 			int recordCount = 0;
 			int totalSamples = 0;
 			
 			if(resultsContainer instanceof DimensionalViewContainer)	{
 				dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
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
 		    	
 		    	//header.append("<table cellpadding=\"0\" cellspacing=\"0\">\n<tr>\n");
 		    	StringBuffer sampleNames = new StringBuffer();
 		        StringBuffer stringBuffer = new StringBuffer();
 		    	
 				
 				Element headerRow = report.addElement("Row").addAttribute("name", "headerRow");
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Gene");
 				        data = null;
 			        cell = null;
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Reporter");
 				        data = null;
 			        cell = null;
 			        
 			        //starting annotations
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "csv").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Locus link");
 				        data = null;
 			        cell = null;
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "csv").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("GenBank Acc");
 				        data = null;
 			        cell = null;
 
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "csv").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("GO Id");
 				        data = null;
 			        cell = null;
 			        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "csv").addAttribute("group", "header");
 				        data = cell.addElement("Data").addAttribute("type", "header").addText("Pathways");
 				        data = null;
 			        cell = null;
 
 		        
 			        Element sampleRow = report.addElement("Row").addAttribute("name", "sampleRow");
 			        cell = sampleRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText(" ");
 			        	data = null;
 			        cell = null;
 			        cell = sampleRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText(" ");
 			        	data = null;
 			        cell = null;
 			        
 			        cell = sampleRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "csv").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText(" ");
 			        	data = null;
 			        cell = null;
 			        cell = sampleRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "csv").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText(" ");
 			        	data = null;
 			        cell = null;
 			        cell = sampleRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "csv").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText(" ");
 			        	data = null;
 			        cell = null;
 			        cell = sampleRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "csv").addAttribute("group", "header");
 			        	data = cell.addElement("Data").addAttribute("type", "header").addText(" ");
 			        	data = null;
 			        cell = null;
 					
 		        //set up the header for the table	        
 		    	//header.append("<Td id=\"header\">Gene</td>\n<td id=\"header\">Reporter</td>\n");        
 		    	//sampleNames.append("<tr><Td> &nbsp;</td><Td> &nbsp;</tD>"); 
 
 		    	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 		        	String label = (String) labelIterator.next();
 		        	sampleIds = geneViewContainer.getBiospecimenLabels(label);    	
 //			    	theColspan += sampleIds.size();
 			    	totalSamples += sampleIds.size();
 
 			    	cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", label).addAttribute("group", label);
 				        		data = cell.addElement("Data").addAttribute("type", "header").addText(label+" Samples");
 					        	data = null;
 					        cell = null;
 		        	//header.append("<td colspan="+sampleIds.size()+" class='"+label+"' id=\"header\">"+label+" Samples</td>"); 
 			    	
 			           	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 
 			           		BioSpecimenIdentifierDE bioSpecimenIdentifierDE = (BioSpecimenIdentifierDE) sampleIdIterator.next();
 							cell = sampleRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", label).addAttribute("group", label);
 						        //data = cell.addElement("Data").addAttribute("type", "header").addText(s.substring(2));
 							    if(bioSpecimenIdentifierDE.getSpecimenName()!= null){
							    	data = cell.addElement("Data").addAttribute("type", "header").addAttribute("specimen", bioSpecimenIdentifierDE.getSpecimenName()).addText(bioSpecimenIdentifierDE.getSpecimenName());
 							    }
 							    else{
 							        data = cell.addElement("Data").addAttribute("type", "header").addText(bioSpecimenIdentifierDE.getSampleId());
 							    }
 						    	data = null;
 						    cell = null;
 			            	//sampleNames.append("<td class='"+label+"' id=\"header\"><a href=\"report.do?s="+s+"&report=ss\">"+s.substring(2)+"</a></td>"); 
 			            	//header.append("\t");
 			 
 			           	}
 		           	//header.deleteCharAt(header.lastIndexOf("\t"));
 		    	}
 		    	//sampleNames.append("</tr>");
 		    	//header.append("</tr>"); 
 		    	
 		    	/* done with the headerRow and SampleRow Elements, time to add data rows */
 					
 		    	for (Iterator geneIterator = genes.iterator(); geneIterator.hasNext();) {
 		    		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
 		    		Collection reporters = geneResultset.getReporterResultsets();
                     String the_gene = "";
 		    		/*  hard code filter for now */
                     if(geneResultset.getGeneSymbol()!= null){
 	        		the_gene = geneResultset.getGeneSymbol().getValueObject().toString();
                     }
 
 		    		//if(!the_gene.equalsIgnoreCase(filter_string))	{
 	        		if(FilterHelper.checkFilter(filter_element, "gene", the_gene, filter_type, filter_string))	{
 	        		//if(!filter_element.equals("gene") || (filter_element.equals("gene") && !filter_string.contains(the_gene)))	{
 			    		recordCount+=reporters.size();
 
 			    		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 			        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 			        		Collection groupTypes = reporterResultset.getGroupByResultsets();
 			        		String reporterName = reporterResultset.getReporter().getValue().toString();
 			        		
 			        		/* test filtration by reporter */
 			        		//if(!filter_element.equals("reporter") || (filter_element.equals("reporter") && !filter_string.contains(reporterName)))	{		
 			        		if(FilterHelper.checkFilter(filter_element, "reporter", reporterName, filter_type, filter_string))	{   	
 				        		GeneSymbol gene = geneResultset.getGeneSymbol();
 				        		//String geneSymbol = "&#160;";
 				        		String geneSymbol = "-";
 				        		if( gene != null){
 				        			geneSymbol = geneResultset.getGeneSymbol().getValueObject().toString();
 				        		}
 				        		
 				        		dataRow = report.addElement("Row").addAttribute("name", "dataRow");
 							        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "gene").addAttribute("group", "header");
 							        	data = cell.addElement("Data").addAttribute("type", "header").addText(geneSymbol);
 							        	data = null;
 							        cell = null;
 							        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "reporter").addAttribute("group", "header");
 							        	data = cell.addElement("Data").addAttribute("type", "header").addText(reporterName);
 							        	data = null;
 							        cell = null;
 				        		//sb.append("<tr><td>"+geneSymbol+"</td><td>"+reporterName+"</td>");
 				        		
 							        
 							        /*
 							         * adding our 4 annotations. this code needs to be cleaned up...
 							         * 
 							         * I will pull all this logic out into a seperate Annotations Class ...
 							         * or something like that ASAP
 							         * 
 							         * RCL
 							         * 
 							         */
 							        String ll = "";							        
 				        			try	{
 						        		HashSet locusLinkIds = new HashSet(reporterResultset.getAssiciatedLocusLinkIDs());
 						        		if(locusLinkIds != null){
 						        			ll = StringUtils.join(locusLinkIds.toArray(), delim);
 						        		}
 						        		else	{
 						        			ll = "-";
 						        		}
 				        			}
 				        			catch(Exception e){
 				        				ll = "--";
 				        			}
 					        		
 
 				        			String acc = "";
 				        			try	{
 						        		HashSet accNumbers = new HashSet(reporterResultset.getAssiciatedGenBankAccessionNos());
 						        		if(accNumbers!=null)	{
 						        			acc = StringUtils.join(accNumbers.toArray(), delim);		
 						        		}
 						        		else	{
 						        			acc = "-";
 						        		}
 				        			}
 				        			catch(Exception e){	}
 				        			
 				        			String go_ids = "";
 				        			try	{
 						        		HashSet go_idsHS = new HashSet(reporterResultset.getAssociatedGOIds());
 						        		if(go_idsHS!=null)	{
 						        			go_ids = StringUtils.join(go_idsHS.toArray(), delim);
 						        		}
 						        		else	{
 						        		    go_ids = "-";
 						        		}
 				        			}
 				        			catch(Exception e){	}
 							        
 				        			String pathways = "";
 				        			try	{
 						        		HashSet pathwaysHS = new HashSet(reporterResultset.getAssociatedPathways());
 						        		if(pathwaysHS!=null)	{
 						        			pathways = StringUtils.join(pathwaysHS.toArray(), delim);
 						        		}
 						        		else	{
 						        		    pathways = "-";
 						        		}
 				        			}
 				        			catch(Exception e){	}
 				        			
 				        			 /*
 							         * 
 							         *  actually add the annotations to the report
 							         * 
 							         */
 							        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "csv").addAttribute("group", "header");
 							        	data = cell.addElement("Data").addAttribute("type", "header").addText(ll);
 							        	data = null;
 							        cell = null;
 							        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "csv").addAttribute("group", "header");
 							        	data = cell.addElement("Data").addAttribute("type", "header").addText(acc);
 							        	data = null;
 							        cell = null;
 							        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "csv").addAttribute("group", "header");
 							        	data = cell.addElement("Data").addAttribute("type", "header").addText(go_ids);
 							        	data = null;
 							        cell = null;
 							        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "csv").addAttribute("group", "header");
 							        	data = cell.addElement("Data").addAttribute("type", "header").addText(pathways);
 							        	data = null;
 							        cell = null;
 							        
 				        		for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 				        			String label = (String) labelIterator.next();
 				        			ViewByGroupResultset groupResultset = (ViewByGroupResultset) reporterResultset.getGroupByResultset(label);
 				        			
 					        			sampleIds = geneViewContainer.getBiospecimenLabels(label);
 					        			String hClass = label;
 					        			if(groupResultset != null)	{
 					                     	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 					                     	
 					                     		BioSpecimenIdentifierDE sampleId = (BioSpecimenIdentifierDE) sampleIdIterator.next();
 					                       		SampleFoldChangeValuesResultset biospecimenResultset = (SampleFoldChangeValuesResultset) groupResultset.getBioSpecimenResultset(sampleId.getSpecimenName());
 					                       		if(biospecimenResultset != null){
 					                       			
 					                       			if(biospecimenResultset.isHighlighted())
 					                       					hClass="highlighted";
 					                       			else
 					                       					hClass = label;
 					                       			
 					                       			Double ratio = (Double)biospecimenResultset.getFoldChangeRatioValue().getValue();
 					                       			if(ratio != null)	{
 					                       				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", hClass).addAttribute("group", label);
 						    					        	data = cell.addElement("Data").addAttribute("type", "data").addText(resultFormat.format(ratio));
 						    					        	data = null;
 						    					        cell = null;
 					                       			
 						                       			//sb.append("<Td class='"+label+"'>"+resultFormat.format(ratio)+" </td>");
 					                       			}
 						                       		else	{
 						                       			cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", hClass).addAttribute("group", label);
 						    					        	data = cell.addElement("Data").addAttribute("type", "data").addText("-");
 						    					        	data = null;
 						    					        cell = null;
 						                      			//sb.append("<td class='"+label+"'>-</td>");
 						                       		}
 					                       		}
 					                       		else	{
 					                       				cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", hClass).addAttribute("group", label);
 						    					        	data = cell.addElement("Data").addAttribute("type", "data").addText("-");
 						    					        	data = null;
 						    					        cell = null;
 					                       			//sb.append("<td class='"+label+"'>-</td>");
 					                       		}
 					                       	}
 				                       }
 				                       else	{
 				                       	for(int s=0;s<sampleIds.size();s++)	{
 				                       		cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", hClass).addAttribute("group", label);
 						    					data = cell.addElement("Data").addAttribute("type", "data").addText("-");
 						    					data = null;
 						    				cell = null;
 				                       		//sb.append("<td class='"+label+"'>-</td>");
 				                       	}
 				                       }
 				
 				         		}
 				         		
 				        		//sb.append("</tr>\n");
 				    		}	/* end reporter filter */
 			    		//sb.append("<tr><td colspan=\""+theColspan+"\" class=\"geneSpacerStyle\">&nbsp;</td></tr>\n");
 			    		} 
 			    	} /* end gene filter  */
 					//sb.append("</table>");
 		    	} 
 			}
 			else {
 				//TODO: handle this error
 				sb.append("<br><Br>Gene Container is empty<br>");
 			}
 		    
 		    //return "<div class=\"rowCount\">"+ helpFul +recordCount+" records returned. " + totalSamples +" samples returned. &nbsp;&nbsp;&nbsp;" + links  + "</div>\n" + sb.toString();
  
 		    return document;
 	}
 /*
 	public boolean checkFilter(String filter_element, String f_element, String name, String filter_type, ArrayList filter_string)	{
 		if(filter_type.equals("hide") && (!filter_element.equals(f_element) || (filter_element.equals(f_element) && !filter_string.contains(name)))) 
 			return true;
 		else if(filter_type.equals("show") && (!filter_element.equals(f_element) || (filter_element.equals(f_element) && filter_string.contains(name))))
 			return true;	
 		else if(!filter_type.equals("show") && !filter_type.equals("hide"))
 			return true;
 		else
 			return false;
 	}
 */
 }
