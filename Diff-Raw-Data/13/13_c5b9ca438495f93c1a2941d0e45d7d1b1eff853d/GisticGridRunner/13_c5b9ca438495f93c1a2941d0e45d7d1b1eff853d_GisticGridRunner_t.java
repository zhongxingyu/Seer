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
 package gov.nih.nci.caintegrator2.application.analysis.grid.gistic;
 
 import edu.wustl.icr.asrv1.segment.ChromosomalSegment;
 import gov.nih.nci.caintegrator2.domain.analysis.GisticResult;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.ParameterException;
 
 import java.math.BigInteger;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.log4j.Logger;
 import org.genepattern.gistic.common.GisticI;
 import org.genepattern.gistic.common.GisticUtils;
 import org.genepattern.gistic.stubs.types.InvalidParameterException;
 import org.oasis.wsrf.faults.BaseFaultTypeDescription;
 
 /**
  * Runs the GenePattern grid service Gistic.
  */
 public class GisticGridRunner {
    
    private static final Logger LOGGER = Logger.getLogger(GisticGridRunner.class);
 
     private final GisticI client;
     
     /**
      * Public Constructor.
      * @param client of grid service.
      */
     public GisticGridRunner(GisticI client) {
         this.client = client;
     }
 
     
     /**
      * Runs GISTIC based on input parameters.
      * @param parameters from user input.
      * @param gisticSamplesMarkers the wrapper object that contains the gistic samples and markers.
      * @return Gistic Results.
      * @throws ConnectionException 
      * @throws ParameterException if have invalid parameter.
      * @throws ConnectionException if unable to connect.
      */
     public List<GisticResult> execute(GisticParameters parameters, GisticSamplesMarkers gisticSamplesMarkers) 
     throws ParameterException, ConnectionException {
         try {
             ChromosomalSegment[] cnvSegmentsToIgnore = retrieveCnvSegmentsToIgnore(parameters);
             return retrieveMarkerResultList(client.runAnalysis(parameters.createParameterList(), 
                     cnvSegmentsToIgnore, parameters.createGenomeBuild(), 
                     gisticSamplesMarkers.getMarkers(), gisticSamplesMarkers.getSamples()));
         } catch (InvalidParameterException e) {
             throw new ParameterException("The given parameters were invalid: " + getDescription(e), e);
         } catch (RemoteException e) {
            LOGGER.error("Couldn't complete GISTIC job", e);
             throw new ConnectionException("Unable to connect to server.", e);
         }
     }
     
     private String getDescription(InvalidParameterException e) {
         StringBuffer description = new StringBuffer();
         for (BaseFaultTypeDescription typeDescription : e.getDescription()) {
             if (description.length() > 0) {
                 description.append(", ");
             }
             description.append(typeDescription.get_value());
         }
         return description.toString();
     }
 
     private ChromosomalSegment[] retrieveCnvSegmentsToIgnore(GisticParameters parameters) {
         ChromosomalSegment[] cnvSegmentsToIgnore;
         if (parameters.getCnvSegmentsToIgnoreFile() != null) {
             cnvSegmentsToIgnore = 
                 GisticUtils.readChromosomalSegmentsFile(parameters.getCnvSegmentsToIgnoreFile().getAbsolutePath());
         } else {
             // Right now the GISTIC grid service doesn't allow for a null or empty cnvSegments parameter,
             // so making up fake data so that it doesn't ignore anything.
             cnvSegmentsToIgnore = new ChromosomalSegment[1];
             ChromosomalSegment segment1 = new ChromosomalSegment();
             segment1.setChromosomeNumber("invalidChromosome");
             segment1.setSegmentEnd(BigInteger.ONE);
             segment1.setSegmentStart(BigInteger.ZERO);
             cnvSegmentsToIgnore[0] = segment1;
         }
         return cnvSegmentsToIgnore;
     }
 
     private List<GisticResult> retrieveMarkerResultList(org.genepattern.gistic.GisticResult[] gisticResults) {
         List<GisticResult> gisticResultList = new ArrayList<GisticResult>();
        if (gisticResults != null) {
            for (org.genepattern.gistic.GisticResult gisticResult : gisticResults) {
                gisticResultList.add(createCai2GisticResult(gisticResult));
            }
         }
         return gisticResultList;
     }
 
     private GisticResult createCai2GisticResult(org.genepattern.gistic.GisticResult gisticResult) {
         GisticResult cai2GisticResult = new GisticResult();
         cai2GisticResult.setAmplification(gisticResult.isAmplification());
         cai2GisticResult.setAmplitude(gisticResult.getAmplitude());
         cai2GisticResult.setFrequency(gisticResult.getFrequency());
         cai2GisticResult.setQvalue(gisticResult.getQValue());
         cai2GisticResult.setScore(gisticResult.getScore());
         return cai2GisticResult;
     }
 
 }
