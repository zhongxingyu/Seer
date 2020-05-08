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
 package gov.nih.nci.caintegrator2.application.arraydata;
 
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Gene;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneExpressionReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterSet;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * Used to load Affymetrix array designs.
  */
 class AffymetrixPlatformLoader extends AbstractPlatformLoader {
     
     private static final Logger LOGGER = Logger.getLogger(AffymetrixPlatformLoader.class);
     
     private static final String PROBE_SET_ID_HEADER = "Probe Set ID";
     private static final String GENE_SYMBOL_HEADER = "Gene Symbol";
     private static final String ENTREZ_GENE_HEADER = "Entrez Gene";
     private static final String ENSEMBL_HEADER = "Ensembl";
     private static final String UNIGENE_ID_HEADER = "UniGene ID";
 
     private final String platformName;
     private final File annotationFile;
     private final Map<String, Gene> symbolToGeneMap = new HashMap<String, Gene>();
     private AffymetrixCdfReader cdfReader;
     private final Map<String, Integer> headerToIndexMap = new HashMap<String, Integer>();
     private CSVReader annotationFileReader;
 
     AffymetrixPlatformLoader(String platformName, File annotationFile) {
         super();
         this.platformName = platformName;
         this.annotationFile = annotationFile;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     Platform load(CaIntegrator2Dao dao) throws PlatformLoadingException {
         Platform platform = createPlatform();
         try {
             handleAnnotationFile(platform, dao);
             dao.save(platform);
         } finally {
             releaseResources();
         }
         return platform;
 
     }
 
     private Platform createPlatform() {
         Platform platform = new Platform();
         platform.setName(platformName);
         platform.setReporterSets(new HashSet<ReporterSet>());
         platform.setVendor("Affymetrix");
         return platform;
     }
 
     private void handleAnnotationFile(Platform platform, CaIntegrator2Dao dao) throws PlatformLoadingException {
         ReporterSet geneReporters = new ReporterSet();
         geneReporters.setPlatform(platform);
         geneReporters.setReporterType(ReporterTypeEnum.GENE_EXPRESSION_GENE.getValue());
         platform.getReporterSets().add(geneReporters);
         geneReporters.setReporters(new HashSet<AbstractReporter>());
         ReporterSet probeSetReporters = new ReporterSet();
         probeSetReporters.setPlatform(platform);
         probeSetReporters.setReporters(new HashSet<AbstractReporter>());
         probeSetReporters.setReporterType(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET.getValue());
         platform.getReporterSets().add(probeSetReporters);
         try {
             annotationFileReader = new CSVReader(new FileReader(annotationFile));
             loadAnnotationHeaders();
             loadAnnotations(geneReporters, probeSetReporters, dao);
         } catch (IOException e) {
             throw new PlatformLoadingException("Couldn't read annotation file " + annotationFile.getName(), e);
         }
     }
 
     private void loadAnnotationHeaders() throws PlatformLoadingException, IOException {
         String[] headers = getHeaders();
         if (headers.length == 0) {
             throw new PlatformLoadingException("Invalid Affymetrix annotation file; headers not found in file: " 
                     + annotationFile.getName());
         }
         for (int i = 0; i < headers.length; i++) {
             headerToIndexMap .put(headers[i], i);
         }
     }
 
     private String[] getHeaders() throws IOException {
         String[] fields;
         while ((fields = annotationFileReader.readNext()) != null) {
             if (isHeadersLine(fields)) {
                 return fields;
             }
         }
         return new String[] {};
     }
 
     private boolean isHeadersLine(String[] fields) {
         return fields.length > 0 && PROBE_SET_ID_HEADER.equals(fields[0]);
     }
 
     private void loadAnnotations(ReporterSet geneReporters, ReporterSet probeSetReporters, CaIntegrator2Dao dao) 
     throws IOException {
         String[] fields;
         while ((fields = annotationFileReader.readNext()) != null) {
             loadAnnotations(fields, geneReporters, probeSetReporters, dao);
         }
     }
     
     private void loadAnnotations(String[] fields, ReporterSet geneReporters, ReporterSet probeSetReporters, 
             CaIntegrator2Dao dao) {
         String symbol = getAnnotationValue(fields, GENE_SYMBOL_HEADER);
         Gene gene = symbolToGeneMap.get(symbol);
         if (gene == null) {
             gene = lookupOrCreateGene(fields, dao);
             addGeneReporter(geneReporters, gene);
         }
         String probeSetName = getAnnotationValue(fields, PROBE_SET_ID_HEADER);
         handleProbeSet(probeSetName, gene, probeSetReporters);
     }
 
     private void addGeneReporter(ReporterSet geneReporters, Gene gene) {
         GeneExpressionReporter geneReporter = new GeneExpressionReporter();
         geneReporter.setGene(gene);
         gene.getReporterCollection().add(geneReporter);
         geneReporter.setName(gene.getSymbol());
         geneReporter.setReporterSet(geneReporters);
         geneReporters.getReporters().add(geneReporter);
     }
 
     private Gene lookupOrCreateGene(String[] fields, CaIntegrator2Dao dao) {
         String symbol = getAnnotationValue(fields, GENE_SYMBOL_HEADER);
         Gene gene = dao.getGene(symbol);
         if (gene == null) {
             gene = createGene(fields);
         }
         symbolToGeneMap.put(symbol, gene);
         return gene;
     }
 
     private Gene createGene(String[] fields) {
         Gene gene = new Gene();
         gene.setReporterCollection(new HashSet<GeneExpressionReporter>());
         gene.setSymbol(getAnnotationValue(fields, GENE_SYMBOL_HEADER));
         gene.setEntrezgeneID(getAnnotationValue(fields, ENTREZ_GENE_HEADER));
         gene.setEnsemblgeneID(getAnnotationValue(fields, ENSEMBL_HEADER));
         gene.setUnigeneclusterID(getAnnotationValue(fields, UNIGENE_ID_HEADER));
         return gene;
     }
 
     private String getAnnotationValue(String[] fields, String header) {
         return fields[headerToIndexMap.get(header)];
     }
 
     private void handleProbeSet(String probeSetName, Gene gene, ReporterSet probeSetReporters) {
         GeneExpressionReporter reporter = new GeneExpressionReporter();
         reporter.setName(probeSetName);
         reporter.setGene(gene);
         reporter.setReporterSet(probeSetReporters);
         probeSetReporters.getReporters().add(reporter);
     }
 
     private void releaseResources() {
         if (cdfReader != null) {
             cdfReader.close();
         }
         closeAnnotationFileREader();
         cdfReader = null;
     }
 
     private void closeAnnotationFileREader() {
         if (annotationFileReader != null) {
             try {
                 annotationFileReader.close();
             } catch (IOException e) {
                 LOGGER.error("Couldn't close annotation file reader for file " + annotationFile.getAbsolutePath());
             }
         }
     }
 
 }
