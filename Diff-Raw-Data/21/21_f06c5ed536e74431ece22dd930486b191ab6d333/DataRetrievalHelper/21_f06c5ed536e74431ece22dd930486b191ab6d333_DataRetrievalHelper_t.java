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
 package gov.nih.nci.caintegrator2.external.caarray;
 
 import gov.nih.nci.caarray.domain.AbstractCaArrayObject;
 import gov.nih.nci.caarray.domain.array.AbstractDesignElement;
 import gov.nih.nci.caarray.domain.array.AbstractProbe;
 import gov.nih.nci.caarray.domain.array.ArrayDesign;
 import gov.nih.nci.caarray.domain.data.DataRetrievalRequest;
 import gov.nih.nci.caarray.domain.data.DataSet;
 import gov.nih.nci.caarray.domain.data.FloatColumn;
 import gov.nih.nci.caarray.domain.data.HybridizationData;
 import gov.nih.nci.caarray.domain.data.QuantitationType;
 import gov.nih.nci.caarray.domain.hybridization.Hybridization;
 import gov.nih.nci.caarray.domain.project.Experiment;
 import gov.nih.nci.caarray.domain.sample.Extract;
 import gov.nih.nci.caarray.domain.sample.LabeledExtract;
 import gov.nih.nci.caarray.services.data.DataRetrievalService;
 import gov.nih.nci.caarray.services.search.CaArraySearchService;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator2.application.arraydata.PlatformHelper;
 import gov.nih.nci.caintegrator2.application.arraydata.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Array;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Responsible for retrieving array data from caArray.
  */
 class DataRetrievalHelper {
 
     private final DataRetrievalService dataRetrievalService;
     private final GenomicDataSourceConfiguration genomicSource;
     private final CaArraySearchService searchService;
     private final CaIntegrator2Dao dao;
     private final Map<Hybridization, Sample> hybridizationToSampleMap = new HashMap<Hybridization, Sample>();
 
     DataRetrievalHelper(GenomicDataSourceConfiguration genomicSource, DataRetrievalService dataRetrievalService,
             CaArraySearchService searchService, CaIntegrator2Dao dao) {
                 this.genomicSource = genomicSource;
                 this.dataRetrievalService = dataRetrievalService;
                 this.searchService = searchService;
                 this.dao = dao;
     }
 
     ArrayDataValues retrieveData() throws ConnectionException {
         DataSet dataSet = dataRetrievalService.getDataSet(createRequest());
         return convertToArrayDataValues(dataSet);
     }
 
     private DataRetrievalRequest createRequest() {
         DataRetrievalRequest request = new DataRetrievalRequest();
         request.addQuantitationType(getSignal());
         addHybridizations(request);
         return request;
     }
 
     private void addHybridizations(DataRetrievalRequest request) {
         List<Sample> samplesToRetrieve;
         if (!genomicSource.getMappedSamples().isEmpty()) {
             samplesToRetrieve = genomicSource.getMappedSamples();
         } else {
             samplesToRetrieve = genomicSource.getSamples();
         }
         for (Sample sample : samplesToRetrieve) {
             gov.nih.nci.caarray.domain.sample.Sample caArraySample = getCaArraySample(sample, 
                     genomicSource.getExperimentIdentifier());
             Set<Hybridization> hybridizations = getHybridizations(caArraySample);
             addToSampleMap(sample, hybridizations);
             request.getHybridizations().addAll(hybridizations);
         }
     }
 
     private void addToSampleMap(Sample sample, Set<Hybridization> hybridizations) {
         for (Hybridization hybridization : hybridizations) {
             hybridizationToSampleMap.put(hybridization, sample);
         }
     }
 
     private Set<Hybridization> getHybridizations(gov.nih.nci.caarray.domain.sample.Sample caArraySample) {
         Set<Hybridization> hybridizations = new HashSet<Hybridization>();
         Set<Extract> extracts = getLoadedCaArrayObjects(caArraySample.getExtracts());
         for (Extract extract : extracts) {
             hybridizations.addAll(getHybridizations(extract));
         }
         return hybridizations;
     }
 
     private Set<Hybridization> getHybridizations(Extract extract) {
         Set<Hybridization> hybridizations = new HashSet<Hybridization>();
         Set<LabeledExtract> labeledExtracts = getLoadedCaArrayObjects(extract.getLabeledExtracts());
         for (LabeledExtract labeledExtract : labeledExtracts) {
             hybridizations.addAll(labeledExtract.getHybridizations());
         }
         return hybridizations;
     }
 
     private QuantitationType getSignal() {
         QuantitationType signalSearch = new QuantitationType();
         signalSearch.setName("CHPSignal");
         return searchService.search(signalSearch).iterator().next();
     }
 
     private ArrayDataValues convertToArrayDataValues(DataSet dataSet) 
     throws ConnectionException {
         ArrayDataValues values = new ArrayDataValues();
         for (HybridizationData hybridizationData : dataSet.getHybridizationDataList()) {
             hybridizationData.setDataSet(dataSet);
             loadArrayDataValues(hybridizationData, values);
         }
         return values;
     }
 
     private void loadArrayDataValues(HybridizationData hybridizationData, ArrayDataValues arrayDataValues) 
     throws ConnectionException {
         PlatformHelper platformHelper = 
             new PlatformHelper(getPlatform(hybridizationData.getHybridization()));
         Array array = createArray(hybridizationData.getHybridization(), platformHelper.getPlatform());
         QuantitationType quantitationType = hybridizationData.getDataSet().getQuantitationTypes().get(0);
         List<AbstractDesignElement> probeSets = 
             hybridizationData.getDataSet().getDesignElementList().getDesignElements();
         float[] values = ((FloatColumn) hybridizationData.getColumn(quantitationType)).getValues();
         for (int i = 0; i < probeSets.size(); i++) {
             arrayDataValues.setValue(array, getReporter(probeSets.get(i), platformHelper), values[i]);
         }
     }
 
     private Platform getPlatform(Hybridization hybridization) {
         Hybridization loadedHybridization = getLoadedCaArrayObject(hybridization);
         ArrayDesign arrayDesign = getLoadedCaArrayObject(loadedHybridization.getArray()).getDesign();
         return dao.getPlatform(arrayDesign.getName());
     }
 
     private Array createArray(Hybridization hybridization, Platform platform) {
         Array array = new Array();
         array.setPlatform(platform);
         array.setName(hybridization.getName());
         array.setSampleCollection(new HashSet<Sample>());
        Sample sample = getAssociatedSample(hybridization);
        array.getSampleCollection().add(sample);
         ArrayData arrayData = new ArrayData();
         arrayData.setArray(array);
         array.setArrayData(arrayData);
        arrayData.setSample(sample);
         arrayData.setStudy(genomicSource.getStudyConfiguration().getStudy());
        if (sample.getArrayDataCollection() == null) {
            sample.setArrayDataCollection(new HashSet<ArrayData>());
        }
        if (sample.getArrayCollection() == null) {
            sample.setArrayCollection(new HashSet<Array>());
        }
        sample.getArrayCollection().add(array);
        sample.getArrayDataCollection().add(arrayData);
         return array;
     }
 
     private Sample getAssociatedSample(Hybridization hybridization) {
         return hybridizationToSampleMap.get(hybridization);
     }
 
     private AbstractReporter getReporter(AbstractDesignElement designElement, PlatformHelper platformHelper) {
         return platformHelper.getReporter(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET, 
                 ((AbstractProbe) designElement).getName()); 
     }
 
     private gov.nih.nci.caarray.domain.sample.Sample getCaArraySample(Sample sample, String experimentIdentifier) {
         gov.nih.nci.caarray.domain.sample.Sample searchSample = new gov.nih.nci.caarray.domain.sample.Sample();
         searchSample.setExperiment(new Experiment());
         searchSample.getExperiment().setPublicIdentifier(experimentIdentifier);
         searchSample.setName(sample.getName());
         return searchService.search(searchSample).get(0);
     }
 
     private <T extends AbstractCaArrayObject> Set<T> getLoadedCaArrayObjects(Set<T> objects) {
         Set<T> loadedObjects = new HashSet<T>(objects.size());
         for (T t : objects) {
             loadedObjects.add(getLoadedCaArrayObject(t));
         }
         return loadedObjects;
     }
 
     private <T extends AbstractCaArrayObject> T getLoadedCaArrayObject(T object) {
         return searchService.search(object).get(0);
     }
 
 }
