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
 
 import gov.nih.nci.caintegrator2.application.CaIntegrator2BaseService;
 import gov.nih.nci.caintegrator2.application.analysis.geneexpression.GenesNotFoundInStudyException;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceDataTypeEnum;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.common.QueryUtil;
 import gov.nih.nci.caintegrator2.domain.application.AbstractAnnotationCriterion;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.QueryResult;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.application.SubjectList;
 import gov.nih.nci.caintegrator2.domain.application.SubjectListCriterion;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.imaging.ImageSeriesAcquisition;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator2.external.ncia.NCIABasket;
 import gov.nih.nci.caintegrator2.external.ncia.NCIADicomJob;
 import gov.nih.nci.caintegrator2.external.ncia.NCIAImageAggregationTypeEnum;
 import gov.nih.nci.caintegrator2.external.ncia.NCIAImageAggregator;
 import gov.nih.nci.caintegrator2.file.FileManager;
 import gov.nih.nci.caintegrator2.web.action.query.DisplayableResultRow;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Implementation of the QueryManagementService interface.
  */
 @SuppressWarnings("PMD.CyclomaticComplexity") // See handleCheckedRowForImageStudy()
 @Transactional(propagation = Propagation.REQUIRED)
 public class QueryManagementServiceImpl extends CaIntegrator2BaseService implements QueryManagementService {
     
     private ResultHandler resultHandler;
     private ArrayDataService arrayDataService;
     private FileManager fileManager;
 
     /**
      * @param resultHandler the resultHandler to set
      */
     public void setResultHandler(ResultHandler resultHandler) {
         this.resultHandler = resultHandler;
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(readOnly = true)
     public QueryResult execute(Query query) throws InvalidCriterionException {
         return new QueryTranslator(retrieveQueryToExecute(query), getDao(), 
                 arrayDataService, resultHandler).execute();
     }
     
     /**
      * {@inheritDoc}
      */
     @Transactional(readOnly = true)
     public GenomicDataQueryResult executeGenomicDataQuery(Query query) throws InvalidCriterionException {
         return new GenomicQueryHandler(retrieveQueryToExecute(query), 
                 getDao(), arrayDataService).execute();
     }
     
     private Query retrieveQueryToExecute(Query query) throws InvalidCriterionException {
         try {
             query.getCompoundCriterion().validateGeneExpressionCriterion();
             if (QueryUtil.isQueryGenomic(query)) {
                 addPlatformToQuery(query);
             }
             Query queryToExecute = query.clone();
             addGenesNotFoundToQuery(query);
             query.setHasMaskedValues(queryToExecute.getCompoundCriterion().isHasMaskedCriteria());
             if (query.isHasMaskedValues()) {
                 maskCompoundCriterion(queryToExecute.getCompoundCriterion());
             }
             checkCriterionColumnsForMasks(query);
             return queryToExecute;
         } catch (CloneNotSupportedException e) {
             throw new IllegalStateException("Unable to clone query.");
         }
     }
 
     private void addPlatformToQuery(Query query) throws InvalidCriterionException {
         Set<String> platformNames = retrieveGeneExpressionPlatformsForStudy(query.getSubscription().getStudy());
         if (platformNames.size() == 1) {
             query.setPlatform(getDao().getPlatform(platformNames.iterator().next()));
         } else {
             Set<String> allPlatformNames = query.getCompoundCriterion().getAllPlatformNames();
             if (allPlatformNames.size() != 1) {
                throw new InvalidCriterionException("A genomic query must contain exactly 1 platform.  " 
                        + "This one contains " + allPlatformNames.size() + " platforms.  " 
                       + "Please create a query with a 'Gene Name' criterion, and select a platform.");
             }
             query.setPlatform(getDao().getPlatform(allPlatformNames.iterator().next()));
         }
     }
     
     private void checkCriterionColumnsForMasks(Query query) {
         if (!query.isHasMaskedValues()) {
             for (ResultColumn column : query.getColumnCollection()) {
                 if (column.getAnnotationFieldDescriptor() != null 
                      && !column.getAnnotationFieldDescriptor().getAnnotationMasks().isEmpty()) {
                     query.setHasMaskedValues(true);
                     break;
                 }
             }
         }
     }
     
     private void maskCompoundCriterion(CompoundCriterion compoundCriterion) {
         Set<AbstractCriterion> criterionToRemove = new HashSet<AbstractCriterion>();
         Set<AbstractCriterion> criterionToAdd = new HashSet<AbstractCriterion>();
         for (AbstractCriterion criterion : compoundCriterion.getCriterionCollection()) {
             if (!(criterion instanceof CompoundCriterion)) {
                 AbstractCriterion newCriterion = retrieveMaskedCriterion(criterion);
                 if (!criterion.equals(newCriterion)) {
                     criterionToAdd.add(newCriterion);
                     criterionToRemove.add(criterion);
                 }
             } else {
                 maskCompoundCriterion((CompoundCriterion) criterion);
             }
         }
         compoundCriterion.getCriterionCollection().addAll(criterionToAdd);
         compoundCriterion.getCriterionCollection().removeAll(criterionToRemove);
     }
     
     private AbstractCriterion retrieveMaskedCriterion(AbstractCriterion abstractCriterion) {
         if (abstractCriterion instanceof AbstractAnnotationCriterion
                 && ((AbstractAnnotationCriterion) abstractCriterion).getAnnotationFieldDescriptor() != null
                 && !((AbstractAnnotationCriterion) abstractCriterion).getAnnotationFieldDescriptor()
                     .getAnnotationMasks().isEmpty()) {
                 return AbstractAnnotationMaskHandler.createMaskedCriterion(
                         ((AbstractAnnotationCriterion) abstractCriterion).getAnnotationFieldDescriptor()
                                 .getAnnotationMasks(), abstractCriterion);
             }
         return abstractCriterion;
     }
     
     private void addGenesNotFoundToQuery(Query query) throws InvalidCriterionException {
         List<String> allGeneSymbols = query.getCompoundCriterion().getAllGeneSymbols();
         query.getGeneSymbolsNotFound().clear();
         if (!allGeneSymbols.isEmpty() && !isQueryOnAllGenes(allGeneSymbols)) {
             try {
                 query.getGeneSymbolsNotFound().addAll(validateGeneSymbols(query.getSubscription(), allGeneSymbols));
             } catch (GenesNotFoundInStudyException e) {
                 throw new InvalidCriterionException(e.getMessage(), e);
             }
         }
     }
     
     private boolean isQueryOnAllGenes(Collection<String> allGeneSymbols) {
         if (allGeneSymbols.contains("")) {
             return true;
         }
         return false;
     }
     
     /**
      * {@inheritDoc}
      */
     public Set<String> retrieveGeneExpressionPlatformsForStudy(Study study) {
         Set<String> platformsInStudy = new HashSet<String>();
         for (Platform platform : arrayDataService.getPlatformsInStudy(
                 study, GenomicDataSourceDataTypeEnum.EXPRESSION)) {
             platformsInStudy.add(platform.getName());
         }
         return platformsInStudy;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Query> createQueriesFromSubjectLists(StudySubscription subscription) {
         List<Query> queries = new ArrayList<Query>();
         for (SubjectList subjectList : subscription.getStudy().getStudyConfiguration().getSubjectLists()) {
             queries.add(createQueryFromSubjectList(subscription, subjectList));
         }
         for (SubjectList subjectList : subscription.getSubjectLists()) {
             queries.add(createQueryFromSubjectList(subscription, subjectList));
         }
         return queries;
     }
 
     /**
      * {@inheritDoc}
      */
     public Query createQueryFromSubjectList(StudySubscription subscription, SubjectList subjectList) {
         Query query = new Query();
         SubjectListCriterion criterion = new SubjectListCriterion();
         criterion.getSubjectListCollection().add(subjectList);
         query.setName(subjectList.getName());
         query.setDescription(subjectList.getDescription());
         query.setCompoundCriterion(new CompoundCriterion());
         query.getCompoundCriterion().setBooleanOperator(BooleanOperatorEnum.AND);
         query.setColumnCollection(new HashSet<ResultColumn>());
         query.setSubscription(subscription);
         query.setResultType(ResultTypeEnum.CLINICAL);
         query.getCompoundCriterion().getCriterionCollection().add(criterion);
         query.setSubjectListQuery(true);
         query.setSubjectListVisibility(subjectList.getVisibility());
         return query;
     }
     
     /**
      * {@inheritDoc}
      */
     public NCIADicomJob createDicomJob(List<DisplayableResultRow> checkedRows) {
         NCIADicomJob dicomJob = new NCIADicomJob();
         dicomJob.setImageAggregationType(retrieveAggregationType(checkedRows));
         fillImageAggregatorFromCheckedRows(dicomJob, checkedRows);
         return dicomJob;
 
     }
 
     /**
      * {@inheritDoc}
      */
     public NCIABasket createNciaBasket(List<DisplayableResultRow> checkedRows) {
         NCIABasket basket = new NCIABasket();
         basket.setImageAggregationType(retrieveAggregationType(checkedRows));
         fillImageAggregatorFromCheckedRows(basket, checkedRows);
         return basket;
     }
     
     private NCIAImageAggregationTypeEnum retrieveAggregationType(List<DisplayableResultRow> rows) {
         NCIAImageAggregationTypeEnum aggregationType = NCIAImageAggregationTypeEnum.IMAGESERIES;
         for (DisplayableResultRow row : rows) {
             if (row.getImageSeries() == null) {
                 aggregationType = NCIAImageAggregationTypeEnum.IMAGESTUDY;
                 break;
             }
         }
         return aggregationType;
     }
 
     private void fillImageAggregatorFromCheckedRows(NCIAImageAggregator imageAggregator, 
                                                     List<DisplayableResultRow> checkedRows) {
         for (DisplayableResultRow row : checkedRows) {
             switch(imageAggregator.getImageAggregationType()) {
             case IMAGESERIES:
                 handleCheckedRowForImageSeries(imageAggregator, row);
                 break;
             case IMAGESTUDY:
                 handleCheckedRowForImageStudy(imageAggregator, row);
                 break;
             default:
                 throw new IllegalStateException("Aggregate Level Type is unknown.");
             }
         }
     }
 
     private void handleCheckedRowForImageSeries(NCIAImageAggregator imageAggregator, DisplayableResultRow row) {
         if (row.getImageSeries() != null) {
             imageAggregator.getImageSeriesIDs().add(row.getImageSeries().getIdentifier());
         } else {
             throw new IllegalArgumentException(
                 "Aggregation is based on Image Series, and a row doesn't contain an Image Series.");
         }
         
     }
     
     @SuppressWarnings("PMD.CyclomaticComplexity") // Null checks
     private void handleCheckedRowForImageStudy(NCIAImageAggregator imageAggregator, DisplayableResultRow row) {
         StudySubjectAssignment studySubjectAssignment = row.getSubjectAssignment();
         if (studySubjectAssignment != null) {
             studySubjectAssignment = getDao().get(row.getSubjectAssignment().getId(), StudySubjectAssignment.class);
         }
         if (studySubjectAssignment != null 
             && studySubjectAssignment.getImageStudyCollection() != null
             && !studySubjectAssignment.getImageStudyCollection().isEmpty()) {
             for (ImageSeriesAcquisition imageStudy : studySubjectAssignment.getImageStudyCollection()) {
                 imageAggregator.getImageStudyIDs().add(imageStudy.getIdentifier());
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public File createCsvFileFromGenomicResults(GenomicDataQueryResult result) {
         File csvFile = new File(fileManager.getNewTemporaryDirectory("tempGenomicResultsDownload"), 
                 "genomicData-" + System.currentTimeMillis() + ".csv");
         return GenomicDataFileWriter.writeAsCsv(result, csvFile);
     }
 
     /**
      * {@inheritDoc}
      */
     public void save(Query query) {
         if (query.getId() == null) {
             getDao().save(query);
         } else {
             getDao().merge(query);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void delete(Query query) {
         getDao().delete(query);
     }
     
     /**
      * {@inheritDoc}
      */
     public List<String> validateGeneSymbols(StudySubscription studySubscription, List<String> geneSymbols)
             throws GenesNotFoundInStudyException {
         List<String> genesNotFound = new ArrayList<String>();
         Set<String> genesInStudy = getDao().retrieveGeneSymbolsInStudy(geneSymbols, studySubscription.getStudy());
         if (genesInStudy.isEmpty()) {
             throw new GenesNotFoundInStudyException("None of the specified genes were found in study.");
         }
         for (String geneSymbol : geneSymbols) {
             if (!Cai2Util.containsIgnoreCase(genesInStudy, geneSymbol)) {
                 genesNotFound.add(geneSymbol);
             }
         }
         if (!genesNotFound.isEmpty()) {
             Collections.sort(genesNotFound);
         }
         return genesNotFound;
     }
 
     /**
      * @return the arrayDataService
      */
     public ArrayDataService getArrayDataService() {
         return arrayDataService;
     }
 
     /**
      * @param arrayDataService the arrayDataService to set
      */
     public void setArrayDataService(ArrayDataService arrayDataService) {
         this.arrayDataService = arrayDataService;
     }
 
     /**
      * @return the fileManager
      */
     public FileManager getFileManager() {
         return fileManager;
     }
 
     /**
      * @param fileManager the fileManager to set
      */
     public void setFileManager(FileManager fileManager) {
         this.fileManager = fileManager;
     }
 
 
 }
