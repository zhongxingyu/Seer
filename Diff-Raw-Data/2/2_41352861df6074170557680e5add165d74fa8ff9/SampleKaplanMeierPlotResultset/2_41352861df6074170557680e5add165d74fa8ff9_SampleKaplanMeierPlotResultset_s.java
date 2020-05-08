 package gov.nih.nci.rembrandt.queryservice.resultset.kaplanMeierPlot;
 
 import gov.nih.nci.caintegrator.dto.de.DatumDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.rembrandt.queryservice.resultset.gene.ReporterResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.BioSpecimenResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleResultset;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author SahniH
  * Date: Nov 11, 2004
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
 
 public class SampleKaplanMeierPlotResultset extends SampleResultset{
 	private Map reporters = new HashMap();
 	/**
 	 * @param biospecimenID
 	 */
 	public SampleKaplanMeierPlotResultset(SampleIDDE sampleIDDE) {		
 		this.setSampleIDDE(sampleIDDE);
 	}
 
 	/**
 	 * @param reporterResultset Adds reporterResultset to this DiseaseGeneExprPlotResultset object.
 	 */
 	public void addReporterResultset(ReporterResultset reporterResultset){
 		if(reporterResultset != null && reporterResultset.getReporter() != null){
 			reporters.put(reporterResultset.getReporter().getValue().toString(), reporterResultset);
 		}
 	}
 	/**
 	 * @param reporterResultset Removes reporterResultset from this DiseaseGeneExprPlotResultset object.
 	 */
 	public void removeResultset(ReporterResultset reporterResultset){
 		if(reporterResultset != null && reporterResultset.getReporter() != null){
 			reporters.remove(reporterResultset.getReporter().getValue().toString());
 		}
 	}
     /**
      * @param reporter
 	 * @return reporterResultset Returns reporterResultset for this DiseaseGeneExprPlotResultset.
 	 */
     public ReporterResultset getReporterResultset(String reporter){
     	if(reporter != null){
 			return (ReporterResultset) reporters.get(reporter);
 		}
     		return null;
     }
 	/**
 	 * @return reporterResultset Returns reporterResultset to this DiseaseGeneExprPlotResultset object.
 	 */
     public Collection getReporterResultsets(){
     		return reporters.values();
     }
 	/**
 	 * @return mean of all reporters
 	 */
     public Double getMeanReporterValue(){
     Double mean = null;    
 	Collection reporters = getReporterResultsets();
 	int numberOfReporters = reporters.size();
 	if(numberOfReporters > 0){
 		double reporterValues = 0.0;
 		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 			ReporterResultset reporter = (ReporterResultset) reporterIterator.next();
 			double value = new Double(reporter.getValue().getValue().toString()).doubleValue();
 			reporterValues += value;
 		}
 		mean = new Double (reporterValues / numberOfReporters);		
 	}
 	return mean;
     }
     /**
 	 * @return returns median of all reporters
 	 */
     public Double getMedianReporterValue(){
     Double median = null;    
 	Collection reporters = getReporterResultsets();
 	List<Double> reporterValues = new ArrayList<Double>();
 	int numberOfReporters = reporters.size();
 	if(numberOfReporters > 0){
 		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 			ReporterResultset reporter = (ReporterResultset) reporterIterator.next();
 			double value = new Double(reporter.getValue().getValue().toString()).doubleValue();
 			reporterValues.add(value);
 		}
 		median = median(reporterValues);		
 	}
 	return median;
     }
 	//  ================================================== median
 	//  List must be first sorted
 	private Double median(List<Double> list) {
 	   Collections.sort(list);
 	   int middle = list.size()/2;  // subscript of middle element
 	   if (list.size()%2 == 1) {
 	       // Odd number of elements -- return the middle one.
 	       return list.get(middle);
 	   } else {
 	      // Even number -- return average of middle two
 	      // Must cast the numbers to double before dividing.
	      return (list.get(middle) + list.get(middle+1) / 2.0);
 	   }
 	}//end method median
 	/**
 	 * @param none Removes all reporterResultset in this DiseaseGeneExprPlotResultset object.
 	 */
     public void removeAllReporterResultsets(){
     	reporters.clear();
     }
 
 	/**
 	 * @return Returns the reporters.
 	 */
 	public Map getReporters() {
 		return reporters;
 	}
 	/**
 	 * @return Returns the reporter Names.
 	 */
 	public List getReporterNames() {
         List reporterNames = new ArrayList();
         Collection reporterList = reporters.keySet();
         reporterNames.addAll(reporterList);
 		return reporterNames;
 	}
 	/**
 	 * @param reporters The reporters to set.
 	 */
 	public void setReporters(Map reporters) {
 		this.reporters = reporters;
 	}
     
     public String toString() {
     	if(this.getCensor() != null && getSurvivalLength() != null){
     		return "Census: "+this.getCensor().getValue()+" Survival Length: "+ getSurvivalLength();
     	}
     		return super.toString();
     }
 }
