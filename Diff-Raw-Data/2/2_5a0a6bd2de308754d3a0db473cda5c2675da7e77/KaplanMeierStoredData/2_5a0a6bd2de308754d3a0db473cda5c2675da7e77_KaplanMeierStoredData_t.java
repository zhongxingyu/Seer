 package gov.nih.nci.caintegrator.ui.graphing.data.kaplanmeier;
 
 
 import gov.nih.nci.caintegrator.ui.graphing.data.CachableGraphData;
 
 import java.text.DecimalFormat;
 import java.util.Collection;
 
 /**
  * The purpose of this class is to maintain and hold relevant stored
  * data related to the execution of a Kaplan-Meier Plot
  * 
  * @author BauerD
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
 
 public class KaplanMeierStoredData implements CachableGraphData{
 	
 	
 	private Double upVsRestPvalue = null;
     
 	private Double downVsRestPvalue = null;
    
     private Double intVsRestPvalue = null;
    	
 	private String geneSymbol;
 
 	private double upFold = 2.0;
 
 	private double downFold = 2.0;
 
 	private String chartTitle = null;
 
 	private Double upVsDownPvalue = null;
 	
 	private Double sampleList1VsSampleList2 = null;
 
 	private Double upVsIntPvalue = null;
 
 	private Double downVsIntPvalue = null;
 
 	private Double upVsRest = null;
 
 	private Double downVsRest = null;
 
 	private Double intVsRest = null;
 
 	private Integer upSampleCount = new Integer(0);
 
 	private Integer downSampleCount = new Integer(0);
 
 	private Integer intSampleCount = new Integer(0);
 
 	private Integer allSampleCount = new Integer(0);
 	
 	private Integer sampleList1Count = new Integer(0);
 	
 	private Integer sampleList2Count = new Integer(0);
 
 	private Collection<KaplanMeierSampleInfo> allSamples;
 
 	private Collection<KaplanMeierSampleInfo> upSamples;
 
 	private Collection<KaplanMeierSampleInfo> downSamples;
 
 	private Collection<KaplanMeierSampleInfo> intSamples;
 	
 	private Collection<KaplanMeierSampleInfo> sampleList1;
 	
 	private Collection<KaplanMeierSampleInfo> sampleList2;
 	
 	private Integer numberOfPlots;
 
 	private Collection<KaplanMeierPlotPointSeriesSet> plotPointSeriesCollection;
 
 	private String downLabel;
 
 	private String upLabel;
 	
 	private String samplePlot1Label;
 	
 	private String samplePlot2Label;
 
 	private String id;
      
     private DecimalFormat resultFormat = new DecimalFormat("0.0000");
 
 
 	/**
 	 * @return Returns the chartTitle.
 	 */
 	public String getChartTitle() {
 		return chartTitle;
 	}
 
 	/**
 	 * @param chartTitle The chartTitle to set.
 	 */
 	public void setChartTitle(String chartTitle) {
 		this.chartTitle = chartTitle;
 	}
 
 	/**
 	 * @return Returns the downFold.
 	 */
 	public double getDownFold() {
 		return downFold;
 	}
 
 	/**
 	 * @param downFold The downFold to set.
 	 */
 	public void setDownFold(double downFold) {
 		this.downFold = downFold;
 	}
 
 	/**
 	 * @return Returns the downSampleCount.
 	 */
 	public Integer getDownSampleCount() {
 		return downSampleCount;
 	}
 
 	/**
 	 * @param downSampleCount The downSampleCount to set.
 	 */
 	public void setDownSampleCount(Integer downSampleCount) {
 		this.downSampleCount = downSampleCount;
 	}
 
 	/**
 	 * @return Returns the downVsIntPvalue.
 	 */
 	public Double getDownVsIntPvalue() {
 		return downVsIntPvalue;
 	}
 
 	/**
 	 * @param downVsIntPvalue The downVsIntPvalue to set.
 	 */
 	public void setDownVsIntPvalue(Double downVsIntPvalue) {
 		this.downVsIntPvalue = new Double(resultFormat.format(downVsIntPvalue));
 	}
 
 	/**
 	 * @return Returns the downVsRest.
 	 */
 	public Double getDownVsRest() {
 		return downVsRest;
 	}
 
 	/**
 	 * @param downVsRest The downVsRest to set.
 	 */
 	public void setDownVsRest(Double downVsRest) {
 		this.downVsRest = new Double(resultFormat.format(downVsRest));
 	}
 
 	/**
 	 * @return Returns the geneSymbol.
 	 */
 	public String getGeneSymbol() {
 		return geneSymbol;
 	}
 
 	/**
 	 * @param geneSymbol The geneSymbol to set.
 	 */
 	public void setGeneSymbol(String geneSymbol) {
 		this.geneSymbol = geneSymbol;
 	}
 
 	/**
 	 * @return Returns the intSampleCount.
 	 */
 	public Integer getIntSampleCount() {
 		return intSampleCount;
 	}
 
 	/**
 	 * @param intSampleCount The intSampleCount to set.
 	 */
 	public void setIntSampleCount(Integer intSampleCount) {
 		this.intSampleCount = intSampleCount;
 	}
 
 	/**
 	 * @return Returns the intVsRest.
 	 */
 	public Double getIntVsRest() {
 		return intVsRest;
 	}
 
 	/**
 	 * @param intVsRest The intVsRest to set.
 	 */
 	public void setIntVsRest(Double intVsRest) {
 		this.intVsRest = new Double(resultFormat.format(intVsRest));
 	}
 
 	/**
 	 * @return Returns the upFold.
 	 */
 	public double getUpFold() {
 		return upFold;
 	}
 
 	/**
 	 * @param upFold The upFold to set.
 	 */
 	public void setUpFold(double upFold) {
 		this.upFold = upFold;
 	}
 
 	/**
 	 * @return Returns the upSampleCount.
 	 */
 	public Integer getUpSampleCount() {
 		return upSampleCount;
 	}
 
 	/**
 	 * @param upSampleCount The upSampleCount to set.
 	 */
 	public void setUpSampleCount(Integer upSampleCount) {
 		this.upSampleCount = upSampleCount;
 	}
 
 	/**
 	 * @return Returns the upVsDownPvalue.
 	 */
 	public Double getUpVsDownPvalue() {
 		return upVsDownPvalue;
 	}
 
 	/**
 	 * @param upVsDownPvalue The upVsDownPvalue to set.
 	 */
 	public void setUpVsDownPvalue(Double upVsDownPvalue) {
 		this.upVsDownPvalue = new Double(resultFormat.format(upVsDownPvalue));
 	}
 
 	/**
 	 * @return Returns the upVsIntPvalue.
 	 */
 	public Double getUpVsIntPvalue() {
 		return upVsIntPvalue;
 	}
 
 	/**
 	 * @param upVsIntPvalue The upVsIntPvalue to set.
 	 */
 	public void setUpVsIntPvalue(Double upVsIntPvalue) {
 		this.upVsIntPvalue = new Double(resultFormat.format(upVsIntPvalue));        
 	}
 
 	/**
 	 * @return Returns the upVsRest.
 	 */
 	public Double getUpVsRest() {
 		return upVsRest;
 	}
 
 	/**
 	 * @param upVsRest The upVsRest to set.
 	 */
 	public void setUpVsRest(Double upVsRest) {
 		this.upVsRest = new Double(resultFormat.format(upVsRest));
 	}
 	
 
 	public void setIntSamples(Collection<KaplanMeierSampleInfo> intSamples) {
 		this.intSamples = intSamples;
 		
 	}
 
 	public void setDownSamples(Collection<KaplanMeierSampleInfo> downSamples) {
 		this.downSamples = downSamples;
 		
 	}
 
 	public void setUpSamples(Collection<KaplanMeierSampleInfo> upSamples) {
 		this.upSamples = upSamples;
 		
 	}
 	
 	public void setAllSamples(Collection<KaplanMeierSampleInfo> allSamples) {
 		this.allSamples = allSamples;
 		
 	}
 
 	/**
 	 * @return Returns the allSampleCount.
 	 */
 	public Integer getAllSampleCount() {
 		return allSampleCount;
 	}
 
 	/**
 	 * @param allSampleCount The allSampleCount to set.
 	 */
 	public void setAllSampleCount(Integer allSampleCount) {
 		this.allSampleCount = allSampleCount;
 	}
 
 	/**
 	 * @return Returns the downVsRestPvalue.
 	 */
 	public Double getDownVsRestPvalue() {
 		return downVsRestPvalue;
 	}
 
 	/**
 	 * @param downVsRestPvalue The downVsRestPvalue to set.
 	 */
 	public void setDownVsRestPvalue(Double downVsRestPvalue) {
 		this.downVsRestPvalue = new Double(resultFormat.format(downVsRestPvalue));
 	}
 
 	/**
 	 * @return Returns the intVsRestPvalue.
 	 */
 	public Double getIntVsRestPvalue() {
 		return intVsRestPvalue;
 	}
 
 	/**
 	 * @param intVsRestPvalue The intVsRestPvalue to set.
 	 */
 	public void setIntVsRestPvalue(Double intVsRestPvalue) {
 		this.intVsRestPvalue = new Double(resultFormat.format(intVsRestPvalue));
 	}
 
 	/**
 	 * @return Returns the upVsRestPvalue.
 	 */
 	public Double getUpVsRestPvalue() {
 		return upVsRestPvalue;
 	}
 
 	/**
 	 * @param upVsRestPvalue The upVsRestPvalue to set.
 	 */
 	public void setUpVsRestPvalue(Double upVsRestPvalue) {
 		this.upVsRestPvalue = new Double(resultFormat.format(upVsRestPvalue));
 	}
 
 	/**
 	 * @return Returns the allSamples.
 	 */
 	public Collection<KaplanMeierSampleInfo> getAllSamples() {
 		return allSamples;
 	}
 
 	/**
 	 * @return Returns the downSamples.
 	 */
 	public Collection<KaplanMeierSampleInfo> getDownSamples() {
 		return downSamples;
 	}
 
 	/**
 	 * @return Returns the intSamples.
 	 */
 	public Collection<KaplanMeierSampleInfo> getIntSamples() {
 		return intSamples;
 	}
 
 	/**
 	 * @return Returns the upSamples.
 	 */
 	public Collection<KaplanMeierSampleInfo> getUpSamples() {
 		return upSamples;
 	}
 
 	/**
 	 * @return Returns the numberOfPlots.
 	 */
 	public Integer getNumberOfPlots() {
 		return numberOfPlots;
 	}
 
 	/**
 	 * @param numberOfPlots The numberOfPlots to set.
 	 */
 	public void setNumberOfPlots(Integer numberOfPlots) {
 		this.numberOfPlots = numberOfPlots;
 	}
 
 	public void setPlotPointSeriesCollection(Collection<KaplanMeierPlotPointSeriesSet> plotPointSeriesSetCollection) {
 		this.plotPointSeriesCollection = plotPointSeriesSetCollection;
 		
 	}
 
 	public void setDownLabel(String downLabel) {
 		this.downLabel = downLabel;				
 	}
 	
 	public void setUpLabel(String upLabel) {
 		this.upLabel = upLabel;
 	}
 
 	/**
 	 * @return Returns the downLabel.
 	 */
 	public String getDownLabel() {
 		return downLabel;
 	}
 
 	/**
 	 * @return Returns the plotPointSeriesCollection.
 	 */
 	public Collection<KaplanMeierPlotPointSeriesSet> getPlotPointSeriesCollection() {
 		return plotPointSeriesCollection;
 	}
 
 	/**
 	 * @return Returns the upLabel.
 	 */
 	public String getUpLabel() {
 		return upLabel;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 		
 	}
 
 	public Object getDataset() {
 		return getPlotPointSeriesCollection();
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @return Returns the sampleList1Count.
 	 */
 	public Integer getSampleList1Count() {
 		return sampleList1Count;
 	}
 
 	/**
 	 * @param sampleList1Count The sampleList1Count to set.
 	 */
 	public void setSampleList1Count(Integer sampleList1Count) {
 		this.sampleList1Count = sampleList1Count;
 	}
 
 	/**
 	 * @return Returns the sampleList1VsSampleList2.
 	 */
 	public Double getSampleList1VsSampleList2() {
 		return sampleList1VsSampleList2;
 	}
 
 	/**
 	 * @param sampleList1VsSampleList2 The sampleList1VsSampleList2 to set.
 	 */
 	public void setSampleList1VsSampleList2(Double sampleList1VsSampleList2) {
		this.sampleList1VsSampleList2 = new Double(resultFormat.format(sampleList1VsSampleList2));
 	}
 
 	/**
 	 * @return Returns the sampleList2Count.
 	 */
 	public Integer getSampleList2Count() {
 		return sampleList2Count;
 	}
 
 	/**
 	 * @param sampleList2Count The sampleList2Count to set.
 	 */
 	public void setSampleList2Count(Integer sampleList2Count) {
 		this.sampleList2Count = sampleList2Count;
 	}
 
 	/**
 	 * @return Returns the samplePlot1Label.
 	 */
 	public String getSamplePlot1Label() {
 		return samplePlot1Label;
 	}
 
 	/**
 	 * @param samplePlot1Label The samplePlot1Label to set.
 	 */
 	public void setSamplePlot1Label(String samplePlot1Label) {
 		this.samplePlot1Label = samplePlot1Label;
 	}
 
 	/**
 	 * @return Returns the samplePlot2Label.
 	 */
 	public String getSamplePlot2Label() {
 		return samplePlot2Label;
 	}
 
 	/**
 	 * @param samplePlot2Label The samplePlot2Label to set.
 	 */
 	public void setSamplePlot2Label(String samplePlot2Label) {
 		this.samplePlot2Label = samplePlot2Label;
 	}
 
 	/**
 	 * @return Returns the sampleList1.
 	 */
 	public Collection<KaplanMeierSampleInfo> getSampleList1() {
 		return sampleList1;
 	}
 
 	/**
 	 * @param sampleList1 The sampleList1 to set.
 	 */
 	public void setSampleList1(Collection<KaplanMeierSampleInfo> sampleList1) {
 		this.sampleList1 = sampleList1;
 	}
 
 	/**
 	 * @return Returns the sampleList2.
 	 */
 	public Collection<KaplanMeierSampleInfo> getSampleList2() {
 		return sampleList2;
 	}
 
 	/**
 	 * @param sampleList2 The sampleList2 to set.
 	 */
 	public void setSampleList2(Collection<KaplanMeierSampleInfo> sampleList2) {
 		this.sampleList2 = sampleList2;
 	}
 }
