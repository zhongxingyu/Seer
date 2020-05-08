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
 package gov.nih.nci.caintegrator2.application.geneexpression;
 
 import gov.nih.nci.caintegrator2.application.query.GenomicDataResultRowComparator;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataResultRow;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataResultValue;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Gene;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneExpressionReporter;
 
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Factory for generating GeneExpressionPlotConfiguration's from GenomicDataQueryResults.
  */
 public final class GeneExpressionPlotConfigurationFactory {
 
     private GeneExpressionPlotConfigurationFactory() { }
     
     /**
      * Creates a configuration object from genomic query results.
      * @param genomicResults - results used to create plot configuration.
      * @param genomicValueResultsType - to determine how to display the graph.
      * @return - plot configuration from given results.
      */
     public static GeneExpressionPlotConfiguration createPlotConfiguration(
                     List<GenomicDataQueryResult> genomicResults, GenomicValueResultsTypeEnum genomicValueResultsType) {
         GeneExpressionPlotConfiguration configuration = new GeneExpressionPlotConfiguration();
         configuration.setGenomicValueResultsType(genomicValueResultsType);
         addSampleGroups(genomicResults, configuration);
         return configuration;
     }
 
 
     private static void addSampleGroups(List<GenomicDataQueryResult> genomicResults,
             GeneExpressionPlotConfiguration configuration) {
         PlotSampleGroup allSamplesGroup = new PlotSampleGroup();
         if (genomicResults.size() > 1) {
             configuration.getPlotSampleGroups().add(allSamplesGroup);    
         }
         int numberSubjectsTotal = 0;
         Map<String, PlotReporterGroup> reporterNameToGroupMap = new HashMap<String, PlotReporterGroup>();
         for (GenomicDataQueryResult genomicResult : genomicResults) {
             PlotSampleGroup sampleGroup = new PlotSampleGroup();
             sampleGroup.setName(genomicResult.getQuery().getName());
             configuration.getPlotSampleGroups().add(sampleGroup);
             int numberSubjects = numberSubjectsInGenomicResult(genomicResult);
             if (!sampleGroup.isControlSampleGroup()) { // Don't count control group for total.
                 numberSubjectsTotal += numberSubjects;
             }
             sampleGroup.setNumberSubjects(numberSubjects);
             addReporterGroups(reporterNameToGroupMap, genomicResult, sampleGroup, configuration);
         }
         configureAllSamplesGroup(configuration, allSamplesGroup, reporterNameToGroupMap, numberSubjectsTotal);
     }
 
     private static void addReporterGroups(Map<String, PlotReporterGroup> reporterNameToGroupMap, 
             GenomicDataQueryResult genomicResult, PlotSampleGroup sampleGroup, 
             GeneExpressionPlotConfiguration configuration) {
         Collections.sort(genomicResult.getRowCollection(), new GenomicDataResultRowComparator());
         for (GenomicDataResultRow row : genomicResult.getRowCollection()) {
             addReporterGroups(reporterNameToGroupMap, sampleGroup, configuration, row);
         }
     }
 
     private static void addReporterGroups(Map<String, PlotReporterGroup> reporterNameToGroupMap,
             PlotSampleGroup sampleGroup, GeneExpressionPlotConfiguration configuration, GenomicDataResultRow row) {
         if (!(row.getReporter() instanceof GeneExpressionReporter)) {
             throw new NonGeneExpressionReporterException("Row reporter is not a GeneExpressionReporter.");
         }
         AbstractReporter rowReporter = row.getReporter();
         for (Gene gene : rowReporter.getGenes()) {
             configuration.getGeneNames().add(gene.getSymbol());
         }
         String name = retrieveRowName(row, rowReporter);
         if (!reporterNameToGroupMap.containsKey(name)) {
             configureNewReporterGroup(reporterNameToGroupMap, name);
         } 
         PlotReporterGroup reporterGroup = copyReporterGroup(reporterNameToGroupMap.get(name));
         sampleGroup.getReporterGroups().add(reporterGroup);
         addGeneExpressionValuesToGroup(reporterNameToGroupMap, sampleGroup, row, name, reporterGroup);
     }
 
     private static void addGeneExpressionValuesToGroup(Map<String, PlotReporterGroup> reporterNameToGroupMap,
             PlotSampleGroup sampleGroup, GenomicDataResultRow row, String name, PlotReporterGroup reporterGroup) {
         for (GenomicDataResultValue value : row.getValues()) {
             reporterGroup.getGeneExpressionValues().add(Double.valueOf(value.getValue()));
         }
         if (!sampleGroup.isControlSampleGroup()) { // Don't count control group for total.
             reporterNameToGroupMap.get(name).getGeneExpressionValues().addAll(reporterGroup.getGeneExpressionValues());
         }
     }
     
     private static int numberSubjectsInGenomicResult(GenomicDataQueryResult genomicResult) {
         Set <Long> uniqueSubjects = new HashSet<Long>();
         for (GenomicDataResultColumn column : genomicResult.getColumnCollection()) {
             uniqueSubjects.add(column.getSampleAcquisition().getAssignment().getId());
         }
         return uniqueSubjects.size();
     }
 
     private static String retrieveRowName(GenomicDataResultRow row, AbstractReporter rowReporter) {
         StringBuffer name = new StringBuffer();
         for (Gene gene : rowReporter.getGenes()) {
             if (name.length() > 0) {
                 name.append(' ');
             }
             name.append(gene.getSymbol());
         }
         if (!name.toString().equals(row.getReporter().getName())) {
             name.append(" - ");
             name.append(row.getReporter().getName());
         }
         return name.toString();
     }
     
     private static void configureAllSamplesGroup(GeneExpressionPlotConfiguration configuration, 
             PlotSampleGroup allSamplesGroup, Map<String, PlotReporterGroup> reporterNameToGroupMap, 
             int numberSubjects) {
         if (configuration.getPlotSampleGroups().size() > 1) {
             allSamplesGroup.setName("All");
             allSamplesGroup.setNumberSubjects(numberSubjects);
            for (PlotReporterGroup reporterGroup : retrievePlotReporterGroups(configuration)) {
                 allSamplesGroup.getReporterGroups().add(reporterNameToGroupMap.get(reporterGroup.getName()));
             }
         }
     }
 
    private static List<PlotReporterGroup> retrievePlotReporterGroups(GeneExpressionPlotConfiguration configuration) {
        for (PlotSampleGroup plotSampleGroup : configuration.getPlotSampleGroups()) {
            if (!plotSampleGroup.getReporterGroups().isEmpty()) {
                return plotSampleGroup.getReporterGroups();
            }
        }
        return new ArrayList<PlotReporterGroup>();
    }

     private static void configureNewReporterGroup(Map<String, PlotReporterGroup> reporterNameToGroupMap, String name) {
         PlotReporterGroup group = new PlotReporterGroup();
         reporterNameToGroupMap.put(name, group);
         group.setName(name);
     }
     
     private static PlotReporterGroup copyReporterGroup(PlotReporterGroup group) {
         PlotReporterGroup newGroup = new PlotReporterGroup();
         newGroup.setName(group.getName());
         return newGroup;
     }
     
 
 }
