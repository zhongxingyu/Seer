 package gov.nih.nci.rembrandt.web.helper;
 
 import gov.nih.nci.caintegrator.dto.de.BioSpecimenIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE.GeneSymbol;
 import gov.nih.nci.caintegrator.dto.view.ClinicalSampleView;
 import gov.nih.nci.caintegrator.dto.view.CopyNumberSampleView;
 import gov.nih.nci.caintegrator.dto.view.GeneExprDiseaseView;
 import gov.nih.nci.caintegrator.dto.view.GeneExprSampleView;
 import gov.nih.nci.caintegrator.dto.view.Viewable;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.queryservice.ResultsetManager;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CytobandResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.SampleCopyNumberValuesResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.DiseaseGroupResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.GeneResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.ReporterResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.SampleFoldChangeValuesResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.ViewByGroupResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 
 
 
 
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
 
 public class CSVGenerator  {
 	
 
 	public static final DecimalFormat resultFormat = new DecimalFormat("0.0000");
 	private static Logger logger = Logger.getLogger(CSVGenerator.class);			
 	public static String displayReport(SessionQueryBag queryCollection, boolean csv)	{
 		
 		StringBuffer html = new StringBuffer();
 		StringBuffer errors = new StringBuffer();
 		Resultant resultant;
 			
 		try	{
 			
 			CompoundQuery myCompoundQuery = queryCollection.getCompoundQuery();
 
 			try	{
 				resultant = ResultsetManager.executeCompoundQuery(myCompoundQuery);
 	  		}
 	  		catch (Throwable t)	{
 	  			errors.append("Error executing the query.<Br><Br>");
 	  			logger.error("Error Executing the query");
 	  			return errors.toString();
 	  		}
 
 			if(resultant != null) {      
 		 		ResultsContainer  resultsContainer = resultant.getResultsContainer(); 
 		 		
 		 		String theQuery  =  resultant.getAssociatedQuery().toString();
 
 		 		if(resultsContainer != null)	{
 		 			
 			 		Viewable view = resultant.getAssociatedView();
 			 		
 		 			if (view instanceof GeneExprSampleView)	{ 
 		 				html.append("Gene Expression Fold Change (Tumor/Non-tumor)\n");
 		 				html.append(geneExprSampleView(resultsContainer));
		 				resultant = null;
 		 				return html.toString();
 		 			}
 		 			else if (view instanceof CopyNumberSampleView)	{ 
 		 				html.append("Copy Number Data\n");
 		 				html.append(copyNumberSampleView(resultsContainer));
		 				resultant = null;
 		 				return html.toString();
 		 			}
 		 			else if (view instanceof GeneExprDiseaseView)	{
 		 				html.append("Mean Gene Expression Fold Change for Tumor Sub-types\n");
 		 				html.append(geneExprDiseaseView(resultsContainer));
		 				resultant = null;
 		 				return html.toString();
 		 			}
 	 				else if(view instanceof ClinicalSampleView){
 	 					html.append("Sample Report\n");
 	 					html.append(clinicalSampleView(resultsContainer));
	 					resultant = null;
 	 					return html.toString();
 	 				}	
 	 				else	{
 						errors.append("Error with report view");
						resultant = null;
 						return errors.toString();
 					}
 			 	}
 			 	else	{
 			 		errors.append("No Results Found, Try a Different Query\n");
			 		resultant = null;
 			 		return errors.toString();
 			 	}
 			 } //resultant != null
 			 else	{
 			 	errors.append("Resultant is NULL\n");
			 	resultant = null;
 			 	return errors.toString();
 			 }
 		}
 		
 		catch(Exception e)	{
 			errors.append("Error Displaying the Report.\n");
			resultant = null;
 			return errors.toString();
 		}
 		
 	}
 	
 	
 	
 	public static String clinicalSampleView(ResultsContainer resultsContainer)	{
 			
 			boolean gLinks = false;
 			boolean cLinks = false;
 			StringBuffer sb = new StringBuffer();
 			SampleViewResultsContainer sampleViewContainer = null;
 			if(resultsContainer instanceof DimensionalViewContainer){
 				
 				DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
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
 			
 			Collection samples = sampleViewContainer.getSampleResultsets();
 			sb.append("SAMPLE,AGE at Dx,GENDER,SURVIVAL,DISEASE");
  		   	if(gLinks)
  		   		sb.append(",GeneExp");
  		   	if(cLinks)
  		   		sb.append(",CopyNumber");
  		   	sb.append("\n");
    			for (Iterator sampleIterator = samples.iterator(); sampleIterator.hasNext();) {
    				SampleResultset sampleResultset =  (SampleResultset)sampleIterator.next();
 	   			sb.append(sampleResultset.getSampleIDDE().getValueObject().substring(2)+ "," +
    					sampleResultset.getAgeGroup().getValue()+ "," +
 					sampleResultset.getGenderCode().getValue()+ "," +
 					sampleResultset.getSurvivalLengthRange().getValue()+ "," +
 					sampleResultset.getDisease().getValue());
 	   			if(gLinks)
 	   				sb.append(",G");
 	   			if(cLinks)
 	   				sb.append(",C");
 	   			sb.append("\n");
     		}
     		return sb.toString();
 	}
 	
 	
 	public static String geneExprDiseaseView(ResultsContainer resultsContainer)	{
 		
 		StringBuffer sb = new StringBuffer();
 		GeneExprResultsContainer geneExprDiseaseContainer = (GeneExprResultsContainer) resultsContainer;
 
 					int recordCount = 0;
 					if(geneExprDiseaseContainer != null)	{
 				    	Collection genes = geneExprDiseaseContainer.getGeneResultsets();
 				    	Collection labels = geneExprDiseaseContainer.getGroupsLabels();
 				    	Collection sampleIds = null;
 
 				        String label = null;
 
 				    	sb.append("Gene,Reporter");
 					   
 
 				    	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 				        	label = (String) labelIterator.next();
 				        	sb.append(","+label);
 				    	}
 			
 						sb.append("\n");
 						
 					
 				    	for (Iterator geneIterator = genes.iterator(); geneIterator.hasNext();) {
 				    		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
 				    		Collection reporters = geneResultset.getReporterResultsets();
 				    		
 				    		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 				    			recordCount += reporters.size();
 				    			
 				        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 
 				        		String reporterName = "-";
 				        		try	{
 				        			reporterName = reporterResultset.getReporter().getValue().toString();
 				        		}
 				        		catch(Exception e)	{
 				        			reporterName = "-";
 				        		}
 				        		logger.debug("Reporter: "+ reporterName);
 				        		
 				        		
 				        		GeneSymbol gene = geneResultset.getGeneSymbol();
 				        		String geneSymbol = "-";
 				        		if( gene != null){
 				        			try{
 				        				geneSymbol = geneResultset.getGeneSymbol().getValueObject().toString();
 				        			}
 				        			catch(Exception e){
 				        				geneSymbol = "-";
 				        			}
 				        			logger.debug("Gene Symbol: "+ geneSymbol);
 				        		}
 				        		
 				        		//Collection groupTypes = reporterResultset.getGroupByResultsets();
 				        		//Collection groupTypes = geneExprDiseaseContainer.getGroupByResultsets(geneSymbol,reporterName); //reporterResultset.getGroupResultsets();
 
 				        		sb.append(geneSymbol+"," + reporterName);
 				        		for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 				    	        	label = (String) labelIterator.next();
 				    	        	DiseaseGroupResultset diseaseResultset = (DiseaseGroupResultset) reporterResultset.getGroupByResultset(label);
 				    	        	if(diseaseResultset != null){
 				    	        		try	{
 				    	        			Double ratio = (Double)diseaseResultset.getFoldChangeRatioValue().getValue();
 				    	        			sb.append(","+resultFormat.format(ratio));
 				    	        		}
 				    	        		catch(Exception e)	{
 				    	        			sb.append(",-");
 				    	        		}
 				    	        		try	{
 				    	        			Double pvalue = (Double)diseaseResultset.getRatioPval().getValue();
 				    	        			sb.append(" ("+resultFormat.format(pvalue) + ")");
 				    	        		}
 				    	        		catch(Exception e){
 				    	        			sb.append(" ");
 				    	        		}
 			                   			//Double ratio = (Double)diseaseResultset.getFoldChangeRatioValue().getValue();
 			                   			//Double pvalue = (Double)diseaseResultset.getRatioPval().getValue();
 			                   			//sb.append(","+resultFormat.format(ratio)+" ("+resultFormat.format(pvalue)+")");  
 			                   			}
 			                   		else	{
 			                   			sb.append(",-");
 			                   		}
 				    	    	}
 	   	                   		sb.append("\n");
 				    		}
 				    		// add the line between genes
 				    		//sb.append("\n");
 						    
 				    	}
 				}
 				else	{
 					sb.append("Gene Disease View container is empty");
 				}
 	
 				return sb.toString();
 	}
 
 
 	public static String copyNumberSampleView(ResultsContainer resultsContainer)	{
 		
 				StringBuffer sb = new StringBuffer();
 		    	StringBuffer header = new StringBuffer();
 		    	StringBuffer sampleNames = new StringBuffer();
 		        StringBuffer stringBuffer = new StringBuffer();
 		        StringBuffer theLabels = new StringBuffer();
 		        StringBuffer tempSampleNames = new StringBuffer();
 		        
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
 				
 						Collection cytobands = copyNumberContainer.getCytobandResultsets();
 				    	Collection labels = copyNumberContainer.getGroupsLabels();
 				    	Collection sampleIds = null;
 				    	
 				    	header = new StringBuffer();
 				    	sampleNames = new StringBuffer();
 				    	tempSampleNames = new StringBuffer();
 				        stringBuffer = new StringBuffer();
 				        			        
 				    	sampleNames.append(" , ");
 				    	
 				    	header.append("Cytoband,Reporter");
 
 				    	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 				        	String label = (String) labelIterator.next();
 				        	
 				        	sampleIds = copyNumberContainer.getBiospecimenLabels(label); 
 
 					           	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 					            	tempSampleNames.append("," + sampleIdIterator.next().toString().substring(2)); 
 						        	theLabels.append(","+label); 
 					           	}
 				    	}
 				    	//header.append("\n"); 
 				    	theLabels.append("\n");
 				    	
 						//sb.append(header.toString());
 						//sb.append(sampleNames.toString());
 						
 			    		boolean showLL = true;
 			    		boolean showAcc = true;
 			    		boolean showGenes = true;
 			    		
 				    	for (Iterator cytobandIterator = cytobands.iterator(); cytobandIterator.hasNext();) {
 				    		CytobandResultset cytobandResultset = (CytobandResultset)cytobandIterator.next();
 				    		String cytoband = cytobandResultset.getCytoband().getValue().toString();
 				    		Collection reporters = copyNumberContainer.getRepoterResultsets(cytoband); 
 				    		recordCount += reporters.size();
 				        	for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 				        		
 				        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 				        		String reporterName = reporterResultset.getReporter().getValue().toString();
 				        		Collection groupTypes = copyNumberContainer.getGroupByResultsets(cytoband,reporterName); 
 
 				        		stringBuffer.append(cytoband+","+reporterName);
 				        		
 				        		if(showGenes)	{
 			        				header.append(",Gene Symbols");
 			        				sampleNames.append(", ");
 			        				showGenes = false;
 			        			}
 				        		if(showLL)	{
 			        				header.append(",Locus Link");
 			        				sampleNames.append(", ");
 			        				showLL = false;
 			        			}
 				        		if(showAcc){
 			        				header.append(",Acc.No.");
 			        				sampleNames.append(", ");
 			        				showAcc = false;
 			        			}
 				        		
 				        		//show 3 annotations
 			        			String genes = "";
 			        			try	{
 					        		HashSet geneSymbols = new HashSet(reporterResultset.getAssiciatedGeneSymbols());
 					        		// Collection geneSymbols = reporterResultset.getAssiciatedGeneSymbols();
 					        		if(geneSymbols != null){
 	
 					        			for(Iterator geneIterator = geneSymbols.iterator(); geneIterator.hasNext();)
 					        			{
 					        				try	{
 						        				Object geneObj = geneIterator.next();
 						        				if(geneObj != null){
 							        				genes += geneObj.toString();
 							        				genes += " | ";
 						        				}
 					        				}
 					        				catch(Exception e)	{ }
 					        			}
 					        		}
 					        		else	{
 					        			genes = "xx";
 					        		}
 			        			}
 			        			catch(Exception e)	{
 			        				genes = "xxx";	
 			        			}
 			        			stringBuffer.append(","+genes);
 				        		try	{
 				        			stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
 				        		}
 				        		catch(Exception e) { }
 
 			        			String ll = "";
 			        			try	{
 					        		HashSet locusLinkIds = new HashSet(reporterResultset.getAssiciatedLocusLinkIDs());
 					        		if(locusLinkIds != null){
 					        			for(Iterator LLIterator = locusLinkIds.iterator(); LLIterator.hasNext();)
 					        			{
 					        				try	{
 						        				Object llObj = LLIterator.next();
 						        				if(llObj!=null){
 						        					ll += llObj.toString();
 						        					ll += " | ";
 						        				}
 					        				}
 					        				catch(Exception e) { }
 					        			}	
 					        		}
 					        		else	{
 					        			ll = "xx";
 					        		}
 			        			}
 			        			catch(Exception e){
 			        				ll = "xxx";
 			        			}
 				        		
 				        		stringBuffer.append(","+ll);
 				        		try	{
 				        			stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
 				        		}
 				        		catch(Exception e){ }
 
 			        			String acc = "";
 			        			try	{
 					        		HashSet accNumbers = new HashSet(reporterResultset.getAssiciatedGenBankAccessionNos());
 					        		if(accNumbers!=null)	{
 					        			for(Iterator accIterator = accNumbers.iterator(); accIterator.hasNext();)
 					        			{
 					        				try	{
 						        				Object accObj = accIterator.next();
 						        				if(accObj!=null){
 						        					acc += accObj.toString();
 						        					acc += " | ";
 						        				}	
 					        				}
 					        				catch(Exception e){	}
 					        			}
 	
 					        		}
 					        		else	{
 					        			acc = "xx";
 					        		}
 			        			}
 			        			catch(Exception e){	}
 
 				        		stringBuffer.append(", "+acc);
 				        		try	{
 				        			stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
 				        		}
 				        		catch(Exception e) { }
 				        		//sampleNames.append("\n");
 
 				        		for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 				        			String label = (String) labelIterator.next();
 				        			ViewByGroupResultset groupResultset = (ViewByGroupResultset) reporterResultset.getGroupByResultset(label);
 				        			
 				        			sampleIds = copyNumberContainer.getBiospecimenLabels(label);
 				        			if(groupResultset != null)
 				        			{
 				                     	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 				                       		String sampleId = (String) sampleIdIterator.next();
 				                       		SampleCopyNumberValuesResultset sampleResultset2 = (SampleCopyNumberValuesResultset) groupResultset.getBioSpecimenResultset(sampleId);
 				                       		if(sampleResultset2 != null){
 				                       			Double ratio = (Double)sampleResultset2.getCopyNumber().getValue();
 				                       			if(ratio != null)
 				                       				stringBuffer.append(","+resultFormat.format(ratio));
 				                       			else 
 				                       				stringBuffer.append(",-");
 				                       		}
 				                       		else 
 				                       		{
 				                       			stringBuffer.append(",-");
 				                       		}
 				                       	}
 				        			}
 				        			else	{
 				                    	for(int s=0;s<sampleIds.size();s++) 
 				                    		stringBuffer.append(",-");       
 				                    }
 				         		}
 				        		stringBuffer.append("\n");
 				    		}
 				        	//append the extra row here
 				        	//sb.append("\n");
 				    	}
 				
 				}
 			
 			else	{
 				sb.append("Copy Number container is empty");
 			}	
 				
 				sb.append(header.toString() + theLabels.toString()); // add header
 				sb.append(sampleNames.toString() + tempSampleNames.toString() + "\n"); // add sample rows
 				sb.append(stringBuffer.toString()); // add data
 				
 			return sb.toString();
 				
 	}
 
 
 	public static String geneExprSampleView(ResultsContainer resultsContainer)	{
 		
 				StringBuffer sb = new StringBuffer();
 		    	StringBuffer header = new StringBuffer();
 		    	StringBuffer sampleNames = new StringBuffer();
 		    	StringBuffer tempSampleNames = new StringBuffer();
 		        StringBuffer stringBuffer = new StringBuffer();
 		        StringBuffer theLabels = new StringBuffer();
 		        
 				int recordCount = 0;
 				GeneExprSingleViewResultsContainer geneViewContainer = null;
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
 		
 			    	header = new StringBuffer();
 			    	
 			    	sampleNames = new StringBuffer();
 			        stringBuffer = new StringBuffer();
 			    	tempSampleNames = new StringBuffer();
 			    	
 			    	header.append("Gene,Reporter");
 			    	sampleNames.append(" , "); 
 				   
 			    	theLabels = new StringBuffer();
 			    	
 			    	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 			        	String label = (String) labelIterator.next();
 			        	sampleIds = geneViewContainer.getBiospecimenLabels(label);    	
 			        	//header.append(","+label); 
 			           	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 
 			           		BioSpecimenIdentifierDE bioSpecimenIdentifierDE = (BioSpecimenIdentifierDE) sampleIdIterator.next();
 							    if(bioSpecimenIdentifierDE.getSpecimenName()!= null){
 					            	tempSampleNames.append(","+bioSpecimenIdentifierDE.getSpecimenName().toString().substring(2)); 
 					            	theLabels.append(","+label);
 							    }
 				           	}
 			           	//header.deleteCharAt(header.lastIndexOf("\t"));
 			    	}
 			    	//sampleNames.append("\n");
 			    	theLabels.append("\n"); 
 			    	
 			    	//sb.append(header.toString());
 					//sb.append(sampleNames.toString());
 		
 		    		boolean showLL = true;
 		    		boolean showAcc = true;
 		    		
 			    	for (Iterator geneIterator = genes.iterator(); geneIterator.hasNext();) {
 			    		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
 			    		Collection reporters = geneResultset.getReporterResultsets();
 			    		
 			    		recordCount+=reporters.size();
 			    		
 			    		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 			        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 			        		Collection groupTypes = reporterResultset.getGroupByResultsets();
 			        		String reporterName = reporterResultset.getReporter().getValue().toString();
 
 			        		GeneSymbol gene = geneResultset.getGeneSymbol();
 			        		String geneSymbol = " ";
 			        		if( gene != null){
 			        			geneSymbol = geneResultset.getGeneSymbol().getValueObject().toString();
 			        		}
 			        		stringBuffer.append(geneSymbol+","+reporterName);
 			        		
 			        		//stringBuffer.append(geneResultset.getGeneSymbol().getValueObject().toString()+","+reporterName);
 			        		
 			        		if(showLL)	{
 		        				header.append(",Locus Link");
 		        				sampleNames.append(", ");
 		        				showLL = false;
 		        			}
 			        		if(showAcc){
 		        				header.append(",Acc No");
 		        				sampleNames.append(", ");
 		        				showAcc = false;
 		        			}
 			        		
 			        		String ll = "";
 			        		try	{
 				        		HashSet locusLinkIds = new HashSet(reporterResultset.getAssiciatedLocusLinkIDs());
 				        		if(locusLinkIds != null){
 				        			
 				        			logger.debug("LLs for "+reporterName+": "+locusLinkIds.size());
 				        			for(Iterator LLIterator = locusLinkIds.iterator(); LLIterator.hasNext();)
 				        			{
 				        				try	{
 				        					Object llObj = LLIterator.next();
 				        					if(llObj!=null){
 				        						ll += llObj.toString();
 				        						ll += " | ";
 				        					}
 				        				}
 				        				catch(Exception e){
 				        					
 				        				}
 				        			}
 				        			/*
 				        			if(showLL)	{
 				        				header.append(",Locus Link");
 				        				sampleNames.append(", ");
 				        				showLL = false;
 				        			}
 				        			*/
 				        			//stringBuffer.append(","+ll);
 				        			//stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
 				        			logger.debug("done with this LL");
 				        		}
 				        		else	{
 				        			//stringBuffer.append(",xx");
 				        			ll = "xx";
 				        		}
 			        		}
 			        		catch(Exception e){
 			        			//stringBuffer.append(",xxx");
 			        			ll = "xxx";
 			        		}
 			        		
 			        		stringBuffer.append(","+ll);
 			        		try	{
 			        			stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
 			        		}
 			        		catch(Exception e)	{
 			        			
 			        		}
 			        		
 			        		// do the acc no annotation
 			        		String acc = "";
 			        		try	{
 				        		HashSet accNumbers = new HashSet(reporterResultset.getAssiciatedGenBankAccessionNos());
 				        		if(accNumbers!=null)	{
 				        			
 				        			logger.debug("Acc nos for "+reporterName+": "+accNumbers.size());
 				        			for(Iterator accIterator = accNumbers.iterator(); accIterator.hasNext();)
 				        			{
 				        				try	{
 					        				Object accObj = accIterator.next();
 					        				if(accObj!=null){
 					        					acc += accObj.toString();
 					        					acc += " | ";
 					        				}
 				        				}
 				        				catch(Exception e)	{
 				        					
 				        				}
 				        			}
 				        			/*
 				        			if(showAcc){
 				        				header.append(",Acc No");
 				        				sampleNames.append(", ");
 				        				showAcc = false;
 				        			}
 				        			*/
 				        			//stringBuffer.append(","+acc);
 				        			//stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
 				        			logger.debug("done with this acc");
 				        		}
 				        		else	{
 				        			//stringBuffer.append(",xx");
 				        			acc = "xx";
 				        		}
 			        		}
 			        		catch(Exception e)	{
 			        			//stringBuffer.append(",xxx");
 			        			acc = "xxx";
 			        		}
 			        		
 			        		stringBuffer.append(","+acc);
 		        			try	{
 		        				stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
 		        			}
 		        			catch(Exception e)	{
 		        			}
 		        			
 			        		//sampleNames.append("\n");
 			        		
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
 				                       			if(ratio != null)	{
 				                       				try	{
 				                       					stringBuffer.append(","+resultFormat.format(ratio));
 				                       				}
 				                       				catch(Exception e){
 				                       					logger.error("cant format result");
 				                       					logger.error(e);
                                                         stringBuffer.append(",x");
 				                       				}
 				                       			}
 					                       		else
 					                       			stringBuffer.append(",x ");
 				                       		}
 				                       		else 
 				                       		{
 				                       			stringBuffer.append(",x ");
 				                       		}
 				                       	}
 			                       }
 			                       else	{
 			                       for(int s=0;s<sampleIds.size();s++) 
 			                       		stringBuffer.append(",x ");                      
 			                       }
 			
 			         		}
 			         		
 			        		stringBuffer.append("\n");
 			    		}
 			    		// add the line between genes
 			    		// sb.append("\n");
 			    	}
 				}
 				else {
 					stringBuffer.append("Gene Container is empty<br>");
 				}
 				sb.append(header.toString() + theLabels.toString()); // add header
 				sb.append(sampleNames.toString() + tempSampleNames.toString() + "\n"); // add sample rows
 				sb.append(stringBuffer.toString()); // add data
 				
 			    return sb.toString();
 	
 		
 	}
 
 }
