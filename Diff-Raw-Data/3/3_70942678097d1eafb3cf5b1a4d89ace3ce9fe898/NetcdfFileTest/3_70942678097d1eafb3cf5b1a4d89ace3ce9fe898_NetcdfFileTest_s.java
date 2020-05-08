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
 package gov.nih.nci.caintegrator2.application.arraydata.netcdf;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator2.domain.genomic.Array;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneExpressionReporter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.junit.Test;
 
 import ucar.ma2.InvalidRangeException;
 
 /**
  * 
  */
 public class NetcdfFileTest {
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(NetcdfFileTest.class);
     
    private static final String FILE_NAME = "test/resources/junit-array.nc";
     
     private NetcdfFileReader reader;
 
     
     private final static String arrayName1 = "Array 1";
     private final static String arrayName2 = "Array 2";
     private final static String arrayName3 = "Array 3";
     
     private final static String reporterName1 = "Reporter 1";
     private final static String reporterName2 = "Reporter 2";
     
     private final static Long array1Reporter1Value = Long.valueOf(1);
     private final static Long array2Reporter1Value = Long.valueOf(2);
     private final static Long array3Reporter1Value = Long.valueOf(3);
     
     private final static Long array1Reporter2Value = Long.valueOf(100);
     private final static Long array2Reporter2Value = Long.valueOf(200);
     private final static Long array3Reporter2Value = Long.valueOf(300);
     
     
     @Test
     @SuppressWarnings("PMD.ExcessiveMethodLength")
     public void testNetcdfCreate() throws NetcdfCreationException {
         ArrayDataValues arrayDataValues = new ArrayDataValues();
         /**
          * Setup Reporters 
          */
         GeneExpressionReporter reporter1 = new GeneExpressionReporter();
         reporter1.setId(Long.valueOf(1));
         reporter1.setName(reporterName1);
         
         GeneExpressionReporter reporter2 = new GeneExpressionReporter();
         reporter2.setId(Long.valueOf(2));
         reporter2.setName(reporterName2);
 
         /**
          * Setup Arrays
          */
         Array array1 = new Array();
         array1.setId(Long.valueOf(1));
         array1.setName(arrayName1);
         
         Array array2 = new Array();
         array2.setId(Long.valueOf(2));
         array2.setName(arrayName2);
         
         Array array3 = new Array();
         array3.setId(Long.valueOf(3));
         array3.setName(arrayName3);
         
         // Fill row1 values (for reporter1)
         arrayDataValues.setValue(array1, reporter1, array1Reporter1Value);
         arrayDataValues.setValue(array2, reporter1, array2Reporter1Value);
         arrayDataValues.setValue(array3, reporter1, array3Reporter1Value);
         
         // Fill row2 values (for reporter2)
         arrayDataValues.setValue(array1, reporter2, array1Reporter2Value);
         arrayDataValues.setValue(array2, reporter2, array2Reporter2Value);
         arrayDataValues.setValue(array3, reporter2, array3Reporter2Value);
 
         NetcdfFileWriter writer = new NetcdfFileWriter(arrayDataValues, FILE_NAME);
         
         writer.create();
         
     }
     
     @Test
     public void testRetrievalByReporter() throws NetcdfReadException {
         reader = new NetcdfFileReader(FILE_NAME);
         List<String> reporters = new ArrayList<String>();
         reporters.add(reporterName1);
         reporters.add(reporterName2);
         
         List<ReporterRow> arrayValues;
 
         arrayValues = reader.getArrayDataForReporters(reporters);
 
         assertNotNull(arrayValues);
         /**
          * Uncomment below to see the values in the Logger screen.
          */
 //            for (ReporterRow r : arrayValues) {
 //                LOGGER.info(r.getReporterId());
 //                for (int i = 0; i < r.getArrayValues().length; i++) {
 //                    LOGGER.info(" : " + r.getArrayValues()[i]);
 //                }
 //                LOGGER.info("");
 //            }
         
     }
     
     @Test
     public void testRetrievalByArray() throws NetcdfReadException {
         reader = new NetcdfFileReader(FILE_NAME);
         List<String> arrays = new ArrayList<String>();
         arrays.add(arrayName1);
         arrays.add(arrayName3);
         
         List<ReporterRow> arrayValues;
         
         arrayValues = reader.getArrayDataForArrays(arrays);
     
         assertNotNull(arrayValues);
         /**
          * Uncomment below to see the values in the Logger screen.
          */
 //            for (ReporterRow r : arrayValues) {
 //                LOGGER.info(r.getReporterId());
 //                for (int i = 0; i < r.getArrayValues().length; i++) {
 //                    LOGGER.info(" : " + r.getArrayValues()[i]);
 //                }
 //                LOGGER.info("");
 //            }
         
     }
     
     @Test
     public void testRetrievalByReporterAndArray() throws 
         IOException, 
         InvalidRangeException, 
         NetcdfReadException {
         
         reader = new NetcdfFileReader(FILE_NAME);
         assertEquals(array1Reporter1Value, reader.getArrayData(arrayName1, reporterName1));
         assertEquals(array2Reporter1Value, reader.getArrayData(arrayName2, reporterName1));
         assertEquals(array3Reporter1Value, reader.getArrayData(arrayName3, reporterName1));
         
         assertEquals(array1Reporter2Value, reader.getArrayData(arrayName1, reporterName2));
         assertEquals(array2Reporter2Value, reader.getArrayData(arrayName2, reporterName2));
         assertEquals(array3Reporter2Value, reader.getArrayData(arrayName3, reporterName2));
     }
 
 
 }
