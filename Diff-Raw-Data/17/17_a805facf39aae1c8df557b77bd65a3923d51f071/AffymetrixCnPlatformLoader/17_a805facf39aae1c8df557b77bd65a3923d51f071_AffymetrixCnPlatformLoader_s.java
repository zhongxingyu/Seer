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
 import gov.nih.nci.caintegrator2.domain.genomic.DnaAnalysisReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Gene;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.logging.api.util.StringUtils;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 //import java.util.List;
 //import java.util.ArrayList;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.log4j.Logger;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * Loader for Affymetrix Copy Number array designs.
  */
 
 class AffymetrixCnPlatformLoader extends AbstractPlatformLoader {
 
     static final String DBSNP_RS_ID_HEADER = "dbSNP RS ID";
     private static final Logger LOGGER = Logger.getLogger(AffymetrixCnPlatformLoader.class);
     private static final String CHIP_TYPE_HEADER = "chip_type";
    private static final String CHIP_TYPE_50K_XBA = "Mapping50K_Xba240";
    private static final String CHIP_TYPE_50K_HIND = "Mapping50K_Hind240";
     private static final String CHIP_TYPE_SNP6 = "GenomeWideSNP_6";
     private static final String VERSION_HEADER = "netaffx-annotation-netaffx-build";
     private static final String GENOME_VERSION_HEADER = "genome-version";
     private static final String PROBE_SET_ID_HEADER = "Probe Set ID";
     private static final String GENE_SYMBOL_HEADER = "Associated Gene";
     private static final String NO_VALUE_INDICATOR = "---";
     private static final String CHROMOSOME_HEADER = "Chromosome";
     private static final String POSITION_HEADER = "Physical Position";
     private Map<String, String> fileHeaders;
 
     AffymetrixCnPlatformLoader(AffymetrixCnPlatformSource source) {
         super(source);
     }
 
     @Override
     Platform load(CaIntegrator2Dao dao) throws PlatformLoadingException {
         Platform platform = createPlatform(PlatformVendorEnum.AFFYMETRIX);
         platform.setName(getPlatformName());
         loadAnnotationFiles(platform, dao);
         return platform;
     }
 
     @Override
     public String getPlatformName() throws PlatformLoadingException {
         return getSource().getPlatformName();
     }
 
     void handleAnnotationFile(File annotationFile, Platform platform, CaIntegrator2Dao dao)
     throws PlatformLoadingException {
         try {
             setAnnotationFileReader(new CSVReader(new FileReader(annotationFile)));
             loadHeaders();
             platform.setVersion(getHeaderValue(VERSION_HEADER));
             ReporterList reporterList = 
                 platform.addReporterList(getHeaderValue(CHIP_TYPE_HEADER), ReporterTypeEnum.DNA_ANALYSIS_REPORTER);
             reporterList.setGenomeVersion(getHeaderValue(GENOME_VERSION_HEADER));
             loadAnnotations(reporterList, dao);
             reporterList.sortAndLoadReporterIndexes();
         } catch (IOException e) {
             throw new PlatformLoadingException("Couldn't read annotation file " + annotationFile.getName(), e);
         }
     }
 
     private void loadAnnotations(ReporterList reporterList, CaIntegrator2Dao dao) throws IOException {
         String[] fields;
         while ((fields = getAnnotationFileReader().readNext()) != null) {
             loadAnnotations(fields, reporterList, dao);
         }
     }
 
     private void loadAnnotations(String[] fields, ReporterList reporterList, CaIntegrator2Dao dao) {
         String[] symbols = getSymbols(fields);
         String probeSetName = getAnnotationValue(fields, PROBE_SET_ID_HEADER);
         Set<Gene> genes = getGenes(symbols, fields, dao);
         handleProbeSet(probeSetName, genes, fields, reporterList);
     }
 
     private void handleProbeSet(String probeSetName, Set<Gene> genes, String[] fields, ReporterList reporterList) {
         DnaAnalysisReporter reporter = new DnaAnalysisReporter();
         reporter.setName(probeSetName);
         reporterList.getReporters().add(reporter);
         reporter.setReporterList(reporterList);
         reporter.getGenes().addAll(genes);
         reporter.setChromosome(getAnnotationValue(fields, CHROMOSOME_HEADER, NO_VALUE_INDICATOR));
         reporter.setPosition(getIntegerValue(fields, getPositionHeader()));
     }
 
     private Integer getIntegerValue(String[] fields, String header) {
         String value = getAnnotationValue(fields, header);
         if (!NumberUtils.isNumber(value)) {
             return null;
         }
         return Integer.parseInt(value);
     }
 
     private Set<Gene> getGenes(String[] symbols, String[] fields, CaIntegrator2Dao dao) {
         Set<Gene> genes = new HashSet<Gene>(symbols.length);
         for (String symbol : symbols) {
             Gene gene = getSymbolToGeneMap().get(symbol.toUpperCase(Locale.getDefault()));
             if (gene == null && !symbol.equals(NO_VALUE_INDICATOR)) {
                 gene = lookupOrCreateGene(fields, symbol, dao);
             }
             if (gene != null) {
                 genes.add(gene);
             }
         }
         return genes;
     }
 
     private String[] getSymbols(String[] fields) {
         //
         // This method parses the value from the gene symbol column
         // which is obtained from the manufacturers platform annotation file.
         // This involves breaking the string down into substrings and
         // then finally extracting the gene symbol.
         //
         // An example of this value is as follows:
         // "NM_181714 // intron // 0 // Hs.21945 // LCA5 // 167691 // Leber congenital amaurosis 5
         // /// NM_001122769 // intron // 0 // Hs.21945 // LCA5 // 167691 // Leber congenital amaurosis 5
         //
         // Note in the above string, the top level separator is /// (3 forward slashes)
         // and the second level separator is // (2 forward slashes)
         // A second example of this value is as follows:
         // LCA5 /// LCA5
 
         // Get the gene symbol field and separate into substrings.
         String[] subField = getAnnotationValue(fields, GENE_SYMBOL_HEADER).split("///");
         
         // extract the symbols from the array of substrings
         Set<String> symbolsSet = parseSubString(subField);
         
         // convert to array
         String[] symbols = symbolsSet.toArray(new String[symbolsSet.size()]);
         
         return symbols;
     }
 
     private Set<String> parseSubString(String[] subField) {
         Set<String> symbols = new HashSet<String>();
         for (String subfield : subField) {
             String tempStr = parseSubString2(subfield);
             if (!StringUtils.isBlank(tempStr)) {
                 symbols.add(tempStr);
             }
         }
         return symbols;
      }
 
 
     private String parseSubString2(String subField) {
         String[] holdSymbols = subField.split("//");
         String symbol = ""; 
 
         if (holdSymbols.length == 1) {
             if (!holdSymbols[0].trim().equalsIgnoreCase(NO_VALUE_INDICATOR)) {
                 symbol = holdSymbols[0].trim();
             }
         } else if ((holdSymbols.length > 4)
                     && (!holdSymbols[4].trim().equalsIgnoreCase(NO_VALUE_INDICATOR))) {                
             symbol = holdSymbols[4].trim();
         }
         
         return symbol;
     }    
 
     private String getHeaderValue(String headerName) {
         return fileHeaders.get(headerName);
     }
 
     private void loadHeaders() throws PlatformLoadingException, IOException {
         AffymetrixAnnotationHeaderReader headerReader = new AffymetrixAnnotationHeaderReader(
                 getAnnotationFileReader());
         fileHeaders = headerReader.getFileHeaders();
         loadAnnotationHeaders(headerReader.getDataHeaders(), getRequiredHeaders());
     }
 
     @Override
     Logger getLogger() {
         return LOGGER;
     }
 
     /**
      * @return the positionHeader
      */
     public String getPositionHeader() {
        String chromosomePositionHeader = POSITION_HEADER;
        
        if (getHeaderValue(CHIP_TYPE_HEADER) == CHIP_TYPE_SNP6) {
            chromosomePositionHeader = "Chromosome Start";
        } else if (getHeaderValue(CHIP_TYPE_HEADER) == CHIP_TYPE_50K_XBA) {
            chromosomePositionHeader = "Physical Position";
        } else if (getHeaderValue(CHIP_TYPE_HEADER) == CHIP_TYPE_50K_HIND) {
            chromosomePositionHeader = "Physical Position";
        }
        return chromosomePositionHeader;
     }
 
     /**
      * @return the requiredHeaders
      */
     private String[] getRequiredHeaders() {
         String [] holdString = {PROBE_SET_ID_HEADER, GENE_SYMBOL_HEADER,
                 CHROMOSOME_HEADER, getPositionHeader()}; 
         return holdString;
     }
 
 }
