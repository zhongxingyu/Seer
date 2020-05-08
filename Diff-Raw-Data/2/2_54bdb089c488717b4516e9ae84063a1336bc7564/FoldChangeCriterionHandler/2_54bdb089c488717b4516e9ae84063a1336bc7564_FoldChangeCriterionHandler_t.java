 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.application.query;
 
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValueType;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator2.application.arraydata.DataRetrievalRequest;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.FoldChangeCriterion;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.Gene;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Handler that returns samples matching the given fold change criterion.
  */
 final class FoldChangeCriterionHandler extends AbstractCriterionHandler {
 
     private final FoldChangeCriterion criterion;
 
     private FoldChangeCriterionHandler(FoldChangeCriterion criterion) {
         this.criterion = criterion;
     }
     
     static FoldChangeCriterionHandler create(FoldChangeCriterion foldChangeCriterion) {
         return new FoldChangeCriterionHandler(foldChangeCriterion);
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     Set<ResultRow> getMatches(CaIntegrator2Dao dao, ArrayDataService arrayDataService, Query query, 
             Set<EntityTypeEnum> entityTypes) throws InvalidCriterionException {
         Study study = query.getSubscription().getStudy();
         ReporterTypeEnum reporterType = query.getReporterType();
         configureCompareToSamples(study, criterion.getControlSampleSetName());
         Set<AbstractReporter> reporters = getReporterMatches(dao, study, reporterType);
         DataRetrievalRequest request = new DataRetrievalRequest();
         request.addReporters(reporters);
         request.addArrayDatas(getCandidateArrayDatas(study, reporterType));
         request.addType(ArrayDataValueType.EXPRESSION_SIGNAL);
         ArrayDataValues values = arrayDataService.getFoldChangeValues(request, getCompareToArrayDatas(reporterType));
         return getRows(values, entityTypes);
     }
 
     private Set<ResultRow> getRows(ArrayDataValues values, Set<EntityTypeEnum> entityTypes) {
         ResultRowFactory rowFactory = new ResultRowFactory(entityTypes);
         Set<SampleAcquisition> sampleAcquisitions = new HashSet<SampleAcquisition>();
         for (ArrayData arrayData : values.getArrayDatas()) {
             if (hasFoldChangeMatch(arrayData, values)) {
                 sampleAcquisitions.add(arrayData.getSample().getSampleAcquisition());
             }
         }
         return rowFactory.getSampleRows(sampleAcquisitions);
     }
 
     private boolean hasFoldChangeMatch(ArrayData arrayData, ArrayDataValues values) {
         for (AbstractReporter reporter : values.getReporters()) {
             Float foldChangeValue = values.getFloatValue(arrayData, reporter, ArrayDataValueType.EXPRESSION_SIGNAL);
             if (isFoldChangeMatch(foldChangeValue)) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean isFoldChangeMatch(Float foldChangeValue) {
         switch (criterion.getRegulationType()) {
             case UP:
                 return isFoldsUpMatch(foldChangeValue);
             case DOWN:
                 return isFoldsDownMatch(foldChangeValue);
             case UP_OR_DOWN:
                 return isFoldsUpMatch(foldChangeValue) || isFoldsDownMatch(foldChangeValue);
             case UNCHANGED:
                 return !(isFoldsUpMatch(foldChangeValue) || isFoldsDownMatch(foldChangeValue));
             default:
                 throw new IllegalStateException("Illegal regulation type: " + criterion.getRegulationType());
         }
     }
     
     boolean isGenomicValueMatchCriterion(Set<Gene> genes, Float value) {
         boolean reporterMatch = false;
         for (Gene gene : genes) {
            if (criterion.getGeneSymbol().toUpperCase().contains(gene.getSymbol().toUpperCase())) {
                 reporterMatch = true;
                 break;
             }
         }
         if (reporterMatch && isFoldChangeMatch(value)) {
             return true;
         }
         return false;
     }
 
     private boolean isFoldsDownMatch(Float foldChangeValue) {
         return foldChangeValue <= -criterion.getFoldsDown();
     }
 
     private boolean isFoldsUpMatch(Float foldChangeValue) {
         return foldChangeValue >= criterion.getFoldsUp();
     }
 
     private Collection<ArrayData> getCandidateArrayDatas(Study study, ReporterTypeEnum reporterType) {
         Set<ArrayData> candidateDatas = new HashSet<ArrayData>();
         candidateDatas.addAll(study.getArrayDatas(reporterType));
         candidateDatas.removeAll(getCompareToArrayDatas(reporterType));
         return candidateDatas;
     }
 
     private Collection<ArrayData> getCompareToArrayDatas(ReporterTypeEnum reporterType) {
         Set<ArrayData> compareToDatas = new HashSet<ArrayData>();
         for (Sample sample : criterion.getCompareToSampleSet().getSamples()) {
             compareToDatas.addAll(sample.getArrayDatas(reporterType));
         }
         return compareToDatas;
     }
 
     private void configureCompareToSamples(Study study, String controlSampleSetName) throws InvalidCriterionException {
         if (study.getControlSampleSet(controlSampleSetName) == null
                 || study.getControlSampleSet(controlSampleSetName).getSamples().isEmpty()) {
             throw new InvalidCriterionException(
                     "FoldChangeCriterion is invalid because there are no control samples for study '"
                             + study.getShortTitleText() + "'");
         }
         criterion.setCompareToSampleSet(study.getControlSampleSet(controlSampleSetName));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     Set<AbstractReporter> getReporterMatches(CaIntegrator2Dao dao, Study study, ReporterTypeEnum reporterType) {
         Set<AbstractReporter> reporters = new HashSet<AbstractReporter>();
         Set<String> geneSymbols = new HashSet<String>();
         geneSymbols.addAll(Arrays.asList(criterion.getGeneSymbol().replaceAll("\\s*", "").split(",")));
         reporters.addAll(dao.findReportersForGenes(geneSymbols, reporterType, study));
         return reporters;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     boolean hasEntityCriterion() {
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     boolean hasReporterCriterion() {
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     boolean isEntityMatchHandler() {
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     boolean isReporterMatchHandler() {
         return true;
     }
     
     @Override
     boolean hasCriterionSpecifiedReporterValues() {
         return true;
     }
 
 
 }
