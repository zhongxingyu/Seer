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
 package gov.nih.nci.caintegrator2.common;
 
 import gov.nih.nci.cagrid.common.ZipUtilities;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.DateAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.DatePermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.NumericAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.NumericPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.StringAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.StringPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator2.domain.application.FoldChangeCriterion;
 import gov.nih.nci.caintegrator2.domain.application.GeneNameCriterion;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.ResultValue;
 import gov.nih.nci.caintegrator2.domain.application.SelectedValueCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleSet;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.Hibernate;
 
 /**
  * This is a static utility class used by different caIntegrator2 objects. 
  */
 @SuppressWarnings({ "PMD.CyclomaticComplexity" }) // See method retrieveValueFromRowColumn
 public final class Cai2Util {
     private static final Integer BUFFER_SIZE = 4096;
     private static final String ZIP_FILE_SUFFIX = ".zip";
     
     private Cai2Util() { }
     
     /**
      * Used to see if a Collection of strings contains a string, ignoring case.
      * @param l - collection of strings.
      * @param s - string item to see if exists in the collection.
      * @return true/false value.
      */
     public static boolean containsIgnoreCase(Collection <String> l, String s) {
         Iterator<String> it = l.iterator();
         while (it.hasNext()) {
             if (it.next().equalsIgnoreCase(s)) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Used to see if a Set of ResultRow's contains a specific ResultRow (based on SubjectAssignments matching).
      * This needs to be tweaked, not sure if the algorithm is correct.
      * @param rowSet - set of rows.
      * @param rowToTest - ResultRow item to test if it exists in set.
      * @return true/false value.
      */
     public static boolean resultRowSetContainsResultRow(Set<ResultRow> rowSet, 
                                                         ResultRow rowToTest) {
         for (ResultRow curRow : rowSet) {
             if (curRow.getSubjectAssignment() == rowToTest.getSubjectAssignment()
                     && curRow.getImageSeries() == rowToTest.getImageSeries()
                     && curRow.getSampleAcquisition() == rowToTest.getSampleAcquisition()) {
                     return true;
                 }
             }
         return false;
     }
     
     /**
      * Given a ResultRow and ResultColumn, we can retrieve the specific ResultValue object that 
      * associates the two.  It uses the AnnotationDefinition from the Column to compare so this
      * must be non-null and equal.
      * @param row object to retrieve value from.
      * @param column object to retrieve value from.
      * @return ResultValue, assuming the row/column pair isn't a null value.
      */
     @SuppressWarnings({ "PMD.CyclomaticComplexity" }) // Lots of null checks
     public static ResultValue retrieveValueFromRowColumn(ResultRow row, ResultColumn column) {
         if (row.getValueCollection() != null && !row.getValueCollection().isEmpty()) {
             for (ResultValue value : row.getValueCollection()) {
                 if (value.getColumn().getAnnotationDefinition() != null 
                     && column.getAnnotationDefinition() != null
                     && value.getColumn().getAnnotationDefinition().equals(column.getAnnotationDefinition())) { 
                     return value;
                 }
             }
         }
         return null;
     }
     
     /**
      * Tests a collection of result columns to see if a particular result column already exists in it.
      * @param columns collection of ResultColumns to test.
      * @param column object we are testing to see if it exist in the collection.
      * @return true or false depending on if column exists in result columns colleection
      */
     public static boolean columnCollectionContainsColumn(Collection<ResultColumn> columns, ResultColumn column) {
         if (columns != null && !columns.isEmpty()) {
             for (ResultColumn currentColumn : columns) {
                 if (currentColumn.getAnnotationDefinition() != null 
                         && currentColumn.getAnnotationDefinition().equals(column.getAnnotationDefinition())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * This function checks to see if an AnnotationValue belongs to a PermissibleValue.
      * @param value object to check to see if it belongs to permissible value.
      * @param permissibleValue object that the value uses to validate against.
      * @return true or false value.
      */
     public static boolean annotationValueBelongToPermissibleValue(AbstractAnnotationValue value, 
                                                                   AbstractPermissibleValue permissibleValue) {
         if (value instanceof StringAnnotationValue) {
             return handleStringValues(value, permissibleValue);
         } else if (value instanceof NumericAnnotationValue) {
             return handleNumericValues(value, permissibleValue);
         } else if (value instanceof DateAnnotationValue) {
             return handleDateValues(value, permissibleValue);
         }
         return false; 
     }
     
     private static boolean handleStringValues(AbstractAnnotationValue value, 
                                               AbstractPermissibleValue permissibleValue) {
         if (permissibleValue instanceof StringPermissibleValue) {
             StringPermissibleValue stringPermissibleValue = (StringPermissibleValue) permissibleValue;
             StringAnnotationValue stringValue = (StringAnnotationValue) value;
             if (stringValue.getStringValue().equalsIgnoreCase(stringPermissibleValue.getStringValue())) {
                 return true;
             }
         } else {
             throw new IllegalArgumentException("value is of type String, but permissibleValue is not.");
         }
         return false;
     }
     
     private static boolean handleNumericValues(AbstractAnnotationValue value, 
                                                AbstractPermissibleValue permissibleValue) {
         if (permissibleValue instanceof NumericPermissibleValue) {
             NumericPermissibleValue numericPermissibleValue = (NumericPermissibleValue) permissibleValue;
             NumericAnnotationValue numericValue = (NumericAnnotationValue) value;
             if (numericPermissibleValue.getIsRangeValue() == null 
                 || numericPermissibleValue.getIsRangeValue() == 0) { // Not a ranged value. 
                 if (numericValue.getNumericValue() != null && numericPermissibleValue.getNumericValue() != null 
                     && numericValue.getNumericValue().equals(numericPermissibleValue.getNumericValue())) {
                     return true;
                 }
             } else { // Ranged value
                  if (numericValue.getNumericValue() <= numericPermissibleValue.getHighValue()
                      && numericValue.getNumericValue() >= numericPermissibleValue.getLowValue()) {
                      return true;
                  }
             }
         } else {
             throw new IllegalArgumentException("value is of type Numeric, but permissibleValue is not.");
         }
         return false;
     }
 
     private static boolean handleDateValues(AbstractAnnotationValue value, AbstractPermissibleValue permissibleValue) {
         if (permissibleValue instanceof DatePermissibleValue) {
             DatePermissibleValue datePermissibleValue = (DatePermissibleValue) permissibleValue;
             DateAnnotationValue dateValue = (DateAnnotationValue) value;
             if (dateValue.getDateValue() != null && datePermissibleValue.getDateValue() != null 
                 && dateValue.getDateValue().equals(datePermissibleValue.getDateValue())) {
                 return true;
             }
         } else {
             throw new IllegalArgumentException("value is of type Date, but permissibleValue is not.");
         }
         return false;
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * 
      * @param definition the definition to ensure is loaded from Hibernate.
      */
     public static void loadCollections(AnnotationDefinition definition) {
         loadCollection(definition.getAnnotationValueCollection());
         loadCollection(definition.getPermissibleValueCollection());
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * 
      * @param query the query to ensure is loaded from Hibernate.
      */
     public static void loadCollection(Query query) {
         if (query.getCompoundCriterion() != null) {
             loadCollection(query.getCompoundCriterion().getCriterionCollection());
             for (AbstractCriterion criterion : query.getCompoundCriterion().getCriterionCollection()) {
                 if (criterion instanceof SelectedValueCriterion) {
                     loadCollection(((SelectedValueCriterion) criterion).getValueCollection());
                 } else if (criterion instanceof FoldChangeCriterion) {
                     loadCollection(((FoldChangeCriterion) criterion).getCompareToSampleSet().getSamples());
                 }
             }
         }
         loadCollection(query.getColumnCollection());
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * @param studyConfiguration to load from hibernate.
      */
     public static void loadCollection(StudyConfiguration studyConfiguration) {
        loadCollection(studyConfiguration.getClinicalConfigurationCollection());
         loadGenomicSources(studyConfiguration.getGenomicDataSources());
         Cai2Util.loadCollection(studyConfiguration.getStudy().getAssignmentCollection());
         for (StudySubjectAssignment assignment : studyConfiguration.getStudy().getAssignmentCollection()) {
             loadCollection(assignment.getSampleAcquisitionCollection());
             for (SampleAcquisition sampleAcquisition : assignment.getSampleAcquisitionCollection()) {
                 loadSampleCollections(sampleAcquisition.getSample());
             }
         }
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * @param genomicSources List of GenomicDataSourceConfiguration to load from hibernate.
      */
     public static void loadGenomicSources(List<GenomicDataSourceConfiguration> genomicSources) {
         loadCollection(genomicSources);
         for (GenomicDataSourceConfiguration genomicSource : genomicSources) {
             loadSamples(genomicSource.getSamples());
             loadSamples(genomicSource.getControlSamples());
             loadSamples(genomicSource.getMappedSamples());
             loadSampleSets(genomicSource.getControlSampleSetCollection());
             Hibernate.initialize(genomicSource.getServerProfile());
         }
     }
     
     /**
      * Loads the sample set collection, as well as the subcollections.
      * @param sampleSets to load.
      */
     
     public static void loadSampleSets(Collection<SampleSet> sampleSets) {
         for (SampleSet sampleSet : sampleSets) {
             loadSamples(sampleSet.getSamples());
         }
     }
     
     /**
      * Loads the samples collection, as well as the subcollections.
      * @param samples to load.
      */
     public static void loadSamples(Collection<Sample> samples) {
         loadCollection(samples);
         for (Sample sample : samples) {
             loadSampleCollections(sample);
         }
     }
     
     /**
      * Loads the subcollections for the Sample object.
      * @param sample to load.
      */
     public static void loadSampleCollections(Sample sample) {
         loadCollection(sample.getArrayCollection());
         loadCollection(sample.getArrayDataCollection());
     }
 
     /**
      * Ensure that a persistent collection is loaded.
      * 
      * @param collection the collection to load.
      */
     public static void loadCollection(Collection<? extends Object> collection) {
         Hibernate.initialize(collection);
     }
     
     /**
      * Takes in a directory and zips it up.
      * 
      * @param dir - Directory to zip.
      * @return Zipped file, stored in same location, with same name as the directory with .zip attached.
      * @throws IOException - In case cannot be read.
      */
     public static File zipAndDeleteDirectory(String dir) throws IOException {
         File directory = new File(dir);
         if (!directory.isDirectory()) {
             throw new IllegalArgumentException("Not a directory:  " + dir);
         }
         int index = directory.getPath().indexOf(directory.getName());
         String[] entries = directory.list();
         if (entries.length == 0) {
             return null;
         }
         File zipfile = new File(dir + ZIP_FILE_SUFFIX);
         ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
         addDir(directory, out, index);
         out.close();
         FileUtils.deleteDirectory(directory);
         return zipfile;
     }
     
     /**
      * Adds files to an already created zip file.
      * @param sourceZipfile to add files to.
      * @param files to add to zip file.
      * @return the new zip file.
      * @throws IOException if unable to add to zip file.
      */
     public static File addFilesToZipFile(File sourceZipfile, File... files) throws IOException {
         if (!sourceZipfile.getAbsolutePath().endsWith(ZIP_FILE_SUFFIX)) {
             throw new IllegalArgumentException("The zipfile isn't a .zip type.");
         }
         for (File file : files) {
             ZipUtilities.insertEntry(sourceZipfile, file.getName(), streamFile(file));
         }
         return sourceZipfile;
     }
     
     private static byte[] streamFile(File file) throws IOException {
         InputStream is = new FileInputStream(file);
         long length = file.length();
         if (length > Integer.MAX_VALUE) {
             return null;
         }
         byte[] bytes = new byte[(int) length];
         int offset = 0;
         int numRead = 0;
         while (offset < bytes.length) {
             numRead = is.read(bytes, offset, bytes.length - offset);
             offset += numRead;
             if (numRead <= 0) {
                 break;
             }
         }
         if (offset < bytes.length) {
             throw new IOException("Could not completely read file " + file.getName());
         }
         is.close();
         return bytes;
     }
     
     private static void addDir(File dirObj, ZipOutputStream out, int index) throws IOException {
         File[] files = dirObj.listFiles();
 
         for (int i = 0; i < files.length; i++) {
             File curFile = files[i];
             if (curFile.isDirectory()) {
                 addDir(curFile, out, index);
                 continue;
             }
             addFile(curFile, out, index);
         }
     }
     
     private static void addFile(File curFile, ZipOutputStream out, int index) 
     throws IOException {
         byte[] tmpBuf = new byte[BUFFER_SIZE];
         FileInputStream in = new FileInputStream(curFile);
         String relativePathName = curFile.getPath().substring(index);
         out.putNextEntry(new ZipEntry(relativePathName));
         int len;
         while ((len = in.read(tmpBuf)) > 0) {
             out.write(tmpBuf, 0, len);
         }
         // Complete the entry
         out.closeEntry();
         in.close();
     }
     
     /**
      * Stores a file from an input stream.
      * @param istream stream to store file from.
      * @param filename file to store.
      * @return File that was created.
      * @throws IOException if unable to create file.
      */
     public static File storeFileFromInputStream(InputStream istream, String filename) throws IOException {
         File file = new File(filename);
         OutputStream out = new FileOutputStream(file);
         byte [] buf = new byte[BUFFER_SIZE];
         int len;
         while ((len = istream.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         out.flush();
         out.close();
         istream.close();
         return file;
     }
     
     /**
      * Extract the host name from the url.
      * 
      * @param url
      *            the url
      * @return the host name
      */
     public static String getHostNameFromUrl(String url) {
         if (StringUtils.isEmpty(url)) {
             return null;
         } else {
             try {
                 return url.split("/")[2];
             } catch (Exception e) {
                 return null; // Error parsing url.
             }
         }
     }
     
     /**
      * Used by classes to retrieve a color based on a number (1-10 are different colors, anything else is black).
      * @param colorNumber - number to use.
      * @return - Color object for that number.
      */
     public static Color getColor(int colorNumber) {
         switch(colorNumber) {
             case 1:
                 return Color.GREEN;
             case 2:
                 return Color.BLUE;
             case 3:
                 return Color.RED;
             case 4:
                 return Color.CYAN;
             case 5:
                 return Color.DARK_GRAY;
             case 6:
                 return Color.YELLOW;
             case 7:
                 return Color.LIGHT_GRAY;
             case 8:
                 return Color.MAGENTA;
             case 9:
                 return Color.ORANGE;
             case 10:
                 return Color.PINK;
             default:
                 return Color.BLACK;
         }
     }
     
     /**
      * Recursive function that goes through all criterion in a CompoundCriterion to determine
      * if any of them are genomic based criterion.
      * @param criterion input for the recursive function.
      * @return T/F value.
      */
     public static boolean isCompoundCriterionGenomic(CompoundCriterion criterion) {
         for (AbstractCriterion abstractCriterion : criterion.getCriterionCollection()) {
             if (abstractCriterion instanceof CompoundCriterion) {
                 if (isCompoundCriterionGenomic((CompoundCriterion) abstractCriterion)) {
                     return true;
                 }
             } else if (abstractCriterion instanceof GeneNameCriterion) {
                 return true;
             } else if (abstractCriterion instanceof FoldChangeCriterion) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Creates a query for all genomic data in the study, and limits it to the given clinical queries (if any
      * are given).
      * @param studySubscription for querying.
      * @param clinicalQueries to limit genomic data returned based.
      * @return a Query which contains all genomic data for all clinical queries given (if any).
      */
     public static Query createAllGenomicDataQuery(StudySubscription studySubscription, Set<Query> clinicalQueries) {
         Query query = createQuery(studySubscription);
         query.setResultType(ResultTypeEnum.GENOMIC);
         query.setReporterType(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
         if (clinicalQueries != null && !clinicalQueries.isEmpty()) {
             CompoundCriterion clinicalCompoundCriterions = new CompoundCriterion();
             clinicalCompoundCriterions.setBooleanOperator(BooleanOperatorEnum.OR);
             for (Query clinicalQuery : clinicalQueries) {
                 CompoundCriterion clinicalCompoundCriterion = clinicalQuery.getCompoundCriterion();
                 if (isCompoundCriterionGenomic(clinicalCompoundCriterion)) {
                     throw new IllegalArgumentException("Clinical query has genomic criterion");
                 }
                 clinicalCompoundCriterions.getCriterionCollection().add(clinicalCompoundCriterion);
             }
             query.getCompoundCriterion().getCriterionCollection().add(clinicalCompoundCriterions);
         }
         return query;
     }
 
     private static Query createQuery(StudySubscription studySubscription) {
         Query query = new Query();
         query.setColumnCollection(new HashSet<ResultColumn>());
         query.setCompoundCriterion(new CompoundCriterion());
         query.getCompoundCriterion().setBooleanOperator(BooleanOperatorEnum.AND);
         query.getCompoundCriterion().setCriterionCollection(new HashSet<AbstractCriterion>());
         query.setSubscription(studySubscription);
         return query;
     }
     
     /**
      * Writes a serializable object to disk in the location given. Intended to support
      * testing, this method is provided in the production code-base so that it can be
      * called temporarily from production code to serialize test objects.
      * 
      * @param serializable the object to write to disk.
      * @param filepath the fully-qualified path of the file to write.
      */
     public static void serializeToDisk(Serializable serializable, String filepath) {
         try {
             FileOutputStream fos = new FileOutputStream(filepath);
             ObjectOutputStream oos = new ObjectOutputStream(fos);
             oos.writeObject(serializable);
             oos.close();
             fos.close();
         } catch (FileNotFoundException e) {
             throw new IllegalStateException("Couldn't write object to disk", e);
         } catch (IOException e) {
             throw new IllegalStateException("Couldn't write object to disk", e);
         }
     }
 
 }
