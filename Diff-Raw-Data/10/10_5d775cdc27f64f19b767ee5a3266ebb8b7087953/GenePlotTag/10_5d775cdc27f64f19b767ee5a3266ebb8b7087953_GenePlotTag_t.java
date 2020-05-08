 package gov.nih.nci.rembrandt.web.taglib;
 
 import gov.nih.nci.caintegrator.enumeration.GeneExpressionDataSetType;
 import gov.nih.nci.rembrandt.dto.lookup.DiseaseTypeLookup;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
import gov.nih.nci.rembrandt.util.MoreStringUtils;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.graphing.data.GeneExpressionPlot;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 
 
 
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
 
 public class GenePlotTag extends AbstractGraphingTag {
 	
 	public String algorithm = "";
 	public int doStartTag() {
 		JspWriter out = pageContext.getOut();
 		try {
 			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
 			String geneSymbol = "";
 			//can come from the action or via a get param..never both, never neither
 			if(request.getAttribute("geneSymbol") != null)	{
				geneSymbol = MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String) request.getAttribute("geneSymbol")));
 			}
 			else if(request.getParameter("geneSymbol") != null)	{
				geneSymbol = MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String) request.getParameter("geneSymbol")));
 			}
 			
			String reporter = request.getParameter("reporter") != null ? MoreStringUtils.cleanString(MoreStringUtils.specialCharacters, ((String) request.getParameter("reporter"))) : null;
 			
 			GeneExpressionDataSetType geType = algorithm.equals(RembrandtConstants.REPORTER_SELECTION_AFFY) ? GeneExpressionDataSetType.GeneExpressionDataSet : GeneExpressionDataSetType.UnifiedGeneExpressionDataSet;
 			
 			HashMap charts = GeneExpressionPlot.generateBarChart(geneSymbol, reporter, pageContext.getSession(), new PrintWriter(out), geType);
 			String filename = (String) charts.get("errorBars");
 			String ffilename = (String) charts.get("noErrorBars");
 			String bwFilename = (String) charts.get("bwFilename");
 			String medianFilename = (String) charts.get("medianBars");
 			String legendHtml = (String) charts.get("legend");
 			Map<String,Long> sampleCountMap = (Map<String,Long>)charts.get("diseaseSampleCountMap");
 			String size = (String) charts.get("size");
 			
 			String graphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + filename;
 			String fgraphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + ffilename;
 			String bwgraphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + bwFilename;
 			String mediangraphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + medianFilename;
 			String defaultURL = mediangraphURL; //fgraphURL;
 			String defaultFilename = medianFilename;
 			if(reporter!=null)	{
 				defaultURL = bwgraphURL;
 				defaultFilename = bwFilename;
 			}
 			
 			if(reporter==null){
 				out.print("<div style=\"text-align:left;margin-left:120px; \"> <span style='font-weight:bold'>Data Selection:</span> ");
 				
 				if(algorithm.equals(RembrandtConstants.REPORTER_SELECTION_UNI))	{
 					out.print("<a href=\"graph.do?geneSymbol="+geneSymbol+"&alg="+RembrandtConstants.REPORTER_SELECTION_AFFY+"\">"+RembrandtConstants.REPORTER_SELECTION_AFFY+"</a>");
 				}
 				else	{
 					out.print(RembrandtConstants.REPORTER_SELECTION_AFFY);
 				}
 				out.print(" | ");
 				
 				if(algorithm.equals(RembrandtConstants.REPORTER_SELECTION_AFFY))	{
 					out.print("<a href=\"graph.do?geneSymbol="+geneSymbol+"&alg="+RembrandtConstants.REPORTER_SELECTION_UNI+"\">"+RembrandtConstants.REPORTER_SELECTION_UNI+"</a>");
 				}
 				else	{
 					out.print(RembrandtConstants.REPORTER_SELECTION_UNI);
 				}
 				
 				out.print("<br/><br/><span style='font-weight:bold'>Graph Type:</span> <a href=\"javascript:toggleGenePlot('"+medianFilename+"');\" name=\"graphTypeLinks\"  id=\""+medianFilename+"_link\">Median</a> | ");
 				out.print("<a href=\"javascript:toggleGenePlot('"+ffilename+"');\" name=\"graphTypeLinks\"  id=\""+ffilename+"_link\">Geometric Mean</a> | ");
 				out.print("<a href=\"javascript:toggleGenePlot('"+filename+"');\" name=\"graphTypeLinks\" id=\""+filename+"_link\">Log2 Intensity</a>");
 				//if(algorithm.equals(RembrandtConstants.REPORTER_SELECTION_AFFY))	{
 					out.print(" | <a href=\"javascript:toggleGenePlot('"+bwFilename+"');\" name=\"graphTypeLinks\"  id=\""+bwFilename+"_link\">Box and Whisker Log2 Intensity</a><br/> ");
 				//}
 				out.print("</div><br/>");
 			}
 			
 			//if(size.indexOf("LARGE")!=-1){
 				out.println("<br/><a class=\"message\" style=\"text-decoration:underline\" id=\"graphLink\" href=\""+defaultURL+"\" target=\"_blank\">Click here to open plot in a new window</a><br/><br/>");
 			//}
 			out.println("<h2>Gene Expression Plot ("+geneSymbol.toUpperCase()+")</h2>");
 			if(reporter!=null){
 				out.print("<h2>" + reporter + "</h2>");
 			}
 			out.print("<div style=\"width:700px; overflow:auto;\"><img alt=\"GeneChart JFreechart Plot\" src=\""+ defaultURL+"\" border=0 usemap=\"#"+defaultFilename+"\" id=\"geneChart\"></div>");
 			
 			if(reporter==null)	{
 				out.print("<div id=\"legend\">" + legendHtml + "</div>"); //this is for the custom legend
 			}
 			//init the links
 			out.print("\n\n<script type=\"text/javascript\">toggleGenePlot('"+defaultFilename+"');</script>\n");
 			
 			out.println("<fieldset style='width:350px; text-align:left; padding:3px'>");
 			out.println("<legend>Abbreviations of Group Names</legend>");
 			out.println("<table>\n");
 			DiseaseTypeLookup[] diseaseTypes = LookupManager.getDiseaseType();
 			if(diseaseTypes != null)	{
                	for (int i = 0; i< diseaseTypes.length ; i++) {
                     if(!diseaseTypes[i].getDiseaseType().equals("CELL_LINE")){
 					String diseaseType = diseaseTypes[i].getDiseaseType()!=null ? diseaseTypes[i].getDiseaseType() : "N/A";
 					String diseaseDesc = diseaseTypes[i].getDiseaseDesc() != null ? diseaseTypes[i].getDiseaseDesc() : "N/A";
 					Long count = sampleCountMap.get(diseaseType);
 					if(diseaseType.equalsIgnoreCase(gov.nih.nci.rembrandt.util.RembrandtConstants.ASTRO))	{
 				    	diseaseType = diseaseType.substring(0,5);
 				    }	
 					if(count != null && count > 0){
 						out.println("<tr><td>"+diseaseType+":</td><td>" + diseaseDesc + " ("+count+") </td></tr>\n" );
 					}
                     }
 				}
 				out.println("</table>\n");
 				out.println("</fieldset>");
 			}
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return EVAL_BODY_INCLUDE;
 	}
 	public int doEndTag() throws JspException {
 		
 		return doAfterEndTag(EVAL_PAGE);
 	}
 	public void reset() {
 		//nada
 	}
 	public String getAlgorithm() {
 		return algorithm;
 	}
 	public void setAlgorithm(String algorithm) {
 		this.algorithm = algorithm;
 	}
 	
 
 }
