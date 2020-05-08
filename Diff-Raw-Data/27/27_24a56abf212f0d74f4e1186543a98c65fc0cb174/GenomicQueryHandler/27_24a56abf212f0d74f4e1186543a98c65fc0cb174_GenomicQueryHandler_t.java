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
 import gov.nih.nci.caintegrator2.common.HibernateUtil;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.FoldChangeCriterion;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataResultRow;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataResultValue;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Runs queries that return <code>GenomicDataQueryResults</code>.
  */
 class GenomicQueryHandler {
     
    private static final float DECIMAL_100 = 100.0f;
     private final Query query;
     private final CaIntegrator2Dao dao;
     private final ArrayDataService arrayDataService;
 
     GenomicQueryHandler(Query query, CaIntegrator2Dao dao, ArrayDataService arrayDataService) {
         this.query = query;
         this.dao = dao;
         this.arrayDataService = arrayDataService;
     }
 
     GenomicDataQueryResult execute() throws InvalidCriterionException {
         ArrayDataValues values = getDataValues();
         return createResult(values);
     }
 
     private GenomicDataQueryResult createResult(ArrayDataValues values) {
         GenomicDataQueryResult result = new GenomicDataQueryResult();
         createResultRows(result, values);
         Map<AbstractReporter, GenomicDataResultRow> reporterToRowMap = createReporterToRowMap(result);
         for (ArrayData arrayData : values.getArrayDatas()) {
             addToResult(values, result, reporterToRowMap, arrayData);
         }
         result.setQuery(query);
         return result;
     }
 
     private void addToResult(ArrayDataValues values, GenomicDataQueryResult result,
         Map<AbstractReporter, GenomicDataResultRow> reporterToRowMap, ArrayData arrayData) {
         GenomicDataResultColumn column = result.addColumn();
         column.setSampleAcquisition(arrayData.getSample().getSampleAcquisition());
         for (AbstractReporter reporter : values.getReporters()) {
             HibernateUtil.loadCollection(reporter.getGenes());
             GenomicDataResultRow row = reporterToRowMap.get(reporter);
             GenomicDataResultValue value = new GenomicDataResultValue();
             value.setColumn(column);
             Float floatValue = values.getFloatValue(arrayData, reporter, ArrayDataValueType.EXPRESSION_SIGNAL);
             if (floatValue != null) {
                value.setValue(Math.round(floatValue * DECIMAL_100) / DECIMAL_100);
             }
             row.getValues().add(value);
         }
     }
 
     private Map<AbstractReporter, GenomicDataResultRow> createReporterToRowMap(GenomicDataQueryResult result) {
         Map<AbstractReporter, GenomicDataResultRow> rowMap = new HashMap<AbstractReporter, GenomicDataResultRow>();
         for (GenomicDataResultRow row : result.getRowCollection()) {
             rowMap.put(row.getReporter(), row);
         }
         return rowMap;
     }
 
     private void createResultRows(GenomicDataQueryResult result, ArrayDataValues values) {
         for (AbstractReporter reporter : values.getReporters()) {
             GenomicDataResultRow row = new GenomicDataResultRow();
             row.setReporter(reporter);
             result.getRowCollection().add(row);
         }
     }
 
     private ArrayDataValues getDataValues() throws InvalidCriterionException {
         Collection<ArrayData> arrayDatas = getMatchingArrayDatas();
         Collection<AbstractReporter> reporters = getMatchingReporters(arrayDatas);
         return getDataValues(arrayDatas, reporters);              
     }
 
     private ArrayDataValues getDataValues(Collection<ArrayData> arrayDatas, Collection<AbstractReporter> reporters) {
         DataRetrievalRequest request = new DataRetrievalRequest();
         request.addReporters(reporters);
         request.addArrayDatas(arrayDatas);
         request.addType(ArrayDataValueType.EXPRESSION_SIGNAL);
         if (isFoldChangeQuery()) {
             return arrayDataService.getFoldChangeValues(request, getControlArrayDatas());
         } else {
             return arrayDataService.getData(request);
         }
     }
 
     private Collection<ArrayData> getControlArrayDatas() {
         Set<ArrayData> arrayDatas = new HashSet<ArrayData>();
         for (Sample sample : getFoldChangeCriterion().getCompareToSampleSet().getSamples()) {
             arrayDatas.addAll(sample.getArrayDatas(query.getReporterType()));
         }
         return arrayDatas;
     }
 
     private boolean isFoldChangeQuery() {
         return getFoldChangeCriterion() != null;
     }
 
     private FoldChangeCriterion getFoldChangeCriterion() {
         return getFoldChangeCriterionFromCompoundCriterion(query.getCompoundCriterion());
     }
 
     private FoldChangeCriterion getFoldChangeCriterion(AbstractCriterion criterion) {
         if (criterion instanceof FoldChangeCriterion) {
             return (FoldChangeCriterion) criterion;
         } else if (criterion instanceof CompoundCriterion) {
             CompoundCriterion compoundCriterion = (CompoundCriterion) criterion;
             return getFoldChangeCriterionFromCompoundCriterion(compoundCriterion);
         } else {
             return null;
         }
     }
 
     private FoldChangeCriterion getFoldChangeCriterionFromCompoundCriterion(CompoundCriterion compoundCriterion) {
         for (AbstractCriterion criterion : compoundCriterion.getCriterionCollection()) {
             FoldChangeCriterion foldChangeCriterion = getFoldChangeCriterion(criterion);
             if (foldChangeCriterion != null) {
                 return foldChangeCriterion;
             }
         }
         return null;
     }
 
     private Collection<ArrayData> getMatchingArrayDatas() throws InvalidCriterionException {
         CompoundCriterionHandler criterionHandler = CompoundCriterionHandler.create(query.getCompoundCriterion());
         if (criterionHandler.hasEntityCriterion()) {
             Set<EntityTypeEnum> samplesOnly = new HashSet<EntityTypeEnum>();
             samplesOnly.add(EntityTypeEnum.SAMPLE);
             Set<ResultRow> rows = criterionHandler.getMatches(dao, 
                     arrayDataService, query, samplesOnly);
             return getArrayDatas(rows, query.getReporterType());
         } else {
             return getAllArrayDatas(query.getReporterType());
         }
     }
 
     private Set<ArrayData> getAllArrayDatas(ReporterTypeEnum reporterType) {
         Set<ArrayData> arrayDatas = new HashSet<ArrayData>();
         for (StudySubjectAssignment assignment : query.getSubscription().getStudy().getAssignmentCollection()) {
             for (SampleAcquisition acquisition : assignment.getSampleAcquisitionCollection()) {
                 addMatchesFromArrayDatas(arrayDatas, acquisition.getSample().getArrayDataCollection(), reporterType);
             }
         }
         return arrayDatas;
     }
 
     private void addMatchesFromArrayDatas(Set<ArrayData> matchingArrayDatas, 
             Collection<ArrayData> candidateArrayDatas,
             ReporterTypeEnum reporterType) {
         for (ArrayData arrayData : candidateArrayDatas) {
             for (ReporterList reporterList : arrayData.getReporterLists()) {
                 if (reporterType.equals(reporterList.getReporterType())) {
                     matchingArrayDatas.add(arrayData);
                 }
             }
         }
     }
 
     private Set<ArrayData> getArrayDatas(Set<ResultRow> rows, ReporterTypeEnum reporterType) {
         Set<ArrayData> arrayDatas = new HashSet<ArrayData>();
         for (ResultRow row : rows) {
             if (row.getSampleAcquisition() != null) {
                 Collection<ArrayData> candidateArrayDatas = 
                     row.getSampleAcquisition().getSample().getArrayDataCollection();
                 addMatchesFromArrayDatas(arrayDatas, candidateArrayDatas, reporterType);
             }
         }
         return arrayDatas;
     }
 
     private Collection<AbstractReporter> getMatchingReporters(Collection<ArrayData> arrayDatas) {
         CompoundCriterionHandler criterionHandler = CompoundCriterionHandler.create(query.getCompoundCriterion());
         if (arrayDatas.isEmpty()) {
             return Collections.emptySet();
         } else if (criterionHandler.hasReporterCriterion()) {
             return criterionHandler.getReporterMatches(dao, query.getSubscription().getStudy(), 
                     query.getReporterType());
         } else {
             return getAllReporters(arrayDatas);
         }
     }
 
     private Collection<AbstractReporter> getAllReporters(Collection<ArrayData> arrayDatas) {
         HashSet<AbstractReporter> reporters = new HashSet<AbstractReporter>();
         for (ArrayData arrayData : arrayDatas) {
             reporters.addAll(arrayData.getReporters());
         }
         return reporters;
     }
 
 }
