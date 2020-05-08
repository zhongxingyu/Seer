 /*
 * caBIG Open Source Software License
 *
 * Copyright Notice.  Copyright 2008, ScenPro, Inc,  (caBIG Participant).   The Protocol  Abstraction (PA) Application
 * was created with NCI funding and is part of  the caBIG initiative. The  software subject to  this notice  and license
 * includes both  human readable source code form and machine readable, binary, object code form (the caBIG Software).
 *
 * This caBIG Software License (the License) is between caBIG  Participant  and  You.  You (or Your) shall  mean a
 * person or an entity, and all other entities that control, are  controlled by,  or  are under common  control  with the
 * entity.  Control for purposes of this definition means
 *
 * (i) the direct or indirect power to cause the direction or management of such entity,whether by contract
 * or otherwise,or
 *
 * (ii) ownership of fifty percent (50%) or more of the outstanding shares, or
 *
 * (iii) beneficial ownership of such entity.
 * License.  Provided that You agree to the conditions described below, caBIG Participant  grants  You a  non-exclusive,
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable,  transferable  and royalty-free  right and license in its
 * rights in the caBIG Software, including any copyright or patent rights therein, to
 *
 * (i) use,install, disclose, access, operate,  execute, reproduce,  copy, modify, translate,  market,  publicly display,
 * publicly perform, and prepare derivative works of the caBIG Software in any manner and for any  purpose,  and to have
 * or permit others to do so;
 *
 * (ii) make, have made, use, practice, sell, and offer  for sale,  import, and/or  otherwise  dispose of caBIG Software
 * (or portions thereof);
 *
 * (iii) distribute and have distributed  to  and by third   parties the   caBIG  Software  and any   modifications  and
 * derivative works thereof; and (iv) sublicense the  foregoing rights  set  out in (i), (ii) and (iii) to third parties,
 * including the right to license such rights to further third parties. For sake of clarity,and not by way of limitation,
 * caBIG Participant shall have no right of accounting or right of payment from You or Your sub licensees for the rights
 * granted under this License.   This  License  is  granted  at no  charge  to You. Your downloading, copying, modifying,
 * displaying, distributing or use of caBIG Software constitutes acceptance  of  all of the terms and conditions of this
 * Agreement.  If You do not agree to such terms and conditions,  You have no right to download,  copy,  modify, display,
 * distribute or use the caBIG Software.
 *
 * 1.  Your redistributions of the source code for the caBIG Software must retain the above copyright notice, this  list
 * of conditions and the disclaimer and limitation of liability of Article 6 below.   Your redistributions in object code
 * form must reproduce the above copyright notice,  this list of  conditions  and the  disclaimer  of  Article  6  in the
 * documentation and/or other materials provided with the distribution, if any.
 *
 * 2.  Your end-user documentation included with the redistribution, if any,  must include the  following acknowledgment:
 * This product includes software developed by ScenPro, Inc.   If  You  do not include such end-user documentation, You
 * shall include this acknowledgment in the caBIG Software itself, wherever such third-party acknowledgments normally
 * appear.
 *
 * 3.  You may not use the names ScenPro, Inc., The National Cancer Institute, NCI, Cancer Bioinformatics Grid or
 * caBIG to endorse or promote products derived from this caBIG Software.  This License does not authorize You to use
 * any trademarks, service marks, trade names, logos or product names of either caBIG Participant, NCI or caBIG, except
 * as required to comply with the terms of this License.
 *
 * 4.  For sake of clarity, and not by way of limitation, You  may incorporate this caBIG Software into Your proprietary
 * programs and into any third party proprietary programs.  However, if You incorporate the  caBIG Software  into  third
 * party proprietary programs,  You agree  that You are  solely responsible  for obtaining any permission from such third
 * parties required to incorporate the caBIG Software  into such third party proprietary programs and for informing Your
 * sub licensees, including without limitation Your end-users, of their obligation  to  secure  any  required permissions
 * from such third parties before incorporating the caBIG Software into such third party proprietary  software programs.
 * In the event that You fail to obtain such permissions,  You  agree  to  indemnify  caBIG  Participant  for any claims
 * against caBIG Participant by such third parties, except to the extent prohibited by law,  resulting from Your failure
 * to obtain such permissions.
 *
 * 5.  For sake of clarity, and not by way of limitation, You may add Your own copyright statement  to Your modifications
 * and to the derivative works, and You may provide  additional  or  different  license  terms  and  conditions  in  Your
 * sublicenses of modifications of the caBIG  Software,  or  any  derivative  works  of  the caBIG Software as a whole,
 * provided Your use, reproduction,  and  distribution  of the Work otherwise complies with the conditions stated in this
 * License.
 *
 * 6.  THIS caBIG SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES  ( INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN
 * NO EVENT SHALL THE ScenPro, Inc. OR ITS AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT  LIMITED  TO,  PROCUREMENT OF SUBSTITUTE GOODS  OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS caBIG SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */
 
 package gov.nih.nci.lv.web.action;
 
 import gov.nih.nci.lv.dao.LabSearchDAO;
 import gov.nih.nci.lv.dao.StudyParticipantSearchDOA;
 import gov.nih.nci.lv.domain.Identifier;
 import gov.nih.nci.lv.dto.IntegrationHubDto;
 import gov.nih.nci.lv.dto.LabSearchDto;
 import gov.nih.nci.lv.dto.StudyParticipantSearchDto;
 import gov.nih.nci.lv.hub.C3DHub;
 import gov.nih.nci.lv.hub.CAERSHub;
 import gov.nih.nci.lv.hub.LabHub;
 import gov.nih.nci.lv.util.LVConstants;
 import gov.nih.nci.lv.util.LVException;
 import gov.nih.nci.lv.util.LVUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 /**
  * Labs Action class.
  * @author Naveen Amiruddin
  *
  */
 public class LabAction extends LabViewerAction {
     LabSearchDto labSearhDto = new LabSearchDto();
     List<LabSearchDto> labResults = new ArrayList<LabSearchDto>();
     String studySubjectGridId = null;
     private static Logger logger = Logger.getLogger(LabAction.class);
     /**
      * 
      * @return Success
      * @throws Exception on error
      */
     public String list() throws Exception {
         Long studyPartId = null;
         if (studySubjectGridId != null) {
             // the call is from psc, load the study and participant for the given grid id
             setUserInfoInSession();
            logger.debug(" call from PSC grid id = " + studySubjectGridId);
             Identifier identifier = 
                 new StudyParticipantSearchDOA().getIdentiferByStudySubjectGridId(studySubjectGridId);
             setStudyProtocolId(identifier.getProtocol().getId());
             setStudyProtocolInfo();
             if ((getSessionAttr(LVConstants.STUDY_SEARCH_DTO)) == null) {
                 setAttribute(LVConstants.FAILURE_MESSAGE, 
                         "User does not have access to the Study or Study does not exist");
                 return LVConstants.PROTOCOL;
             }
             labSearhDto.setStudyParticipantId(identifier.getParticipant().getId());
             logger.debug("retrieved Protocol id = " + identifier.getProtocol().getId());
             logger.debug("retrieved Participant id = " + identifier.getParticipant().getId());
         }
         labSearhDto.setStudyProtocolId(getStudyProtocolId());
         if (labSearhDto.getStudyParticipantId() == null) {
             studyPartId = getStudyPartIdFromSession();
         } else if (labSearhDto.getStudyParticipantId() != null) {
             studyPartId = labSearhDto.getStudyParticipantId();
         }    
         labSearhDto.setStudyParticipantId(studyPartId); 
         List<StudyParticipantSearchDto> spsDto = new StudyParticipantSearchDOA().search(new StudyParticipantSearchDto(
                 getStudyProtocolId(), studyPartId , getUserName()));
         if (spsDto.isEmpty()) {
             setAttribute(LVConstants.FAILURE_MESSAGE, "Subject not found for a study ");
             setSession(LVConstants.STUDY_PART_SEARCH_DTO, null);
             return LVConstants.PARTICIPANT;
         }
         setSession(LVConstants.STUDY_PART_SEARCH_DTO, spsDto.get(0));  
         labResults = new LabSearchDAO().search(labSearhDto);
         setSession(LVConstants.LAB_RESULTS, labResults);
         setAttribute(LVConstants.TOPIC, "labs");
         return SUCCESS;
     }
     
 
     /**
      * 
      * @return Success
      * @throws Exception on error
      */
     public String c3d() throws Exception {
         return loadLabs(new C3DHub() , LVConstants.C3D);
     }
     
 
     /**
      * 
      * @return Success
      * @throws Exception on error
      */
     public String caers() throws Exception {
         return loadLabs(new CAERSHub(), LVConstants.CAAERS);
     }
     
     
     private String loadLabs(LabHub iHub , String target) throws Exception {
         labResults = (List<LabSearchDto>) getSessionAttr(LVConstants.LAB_RESULTS);
         IntegrationHubDto hubDto = super.getHubDto();
         hubDto.setHubUrl((String) getSessionAttr(LVConstants.HUB_URL));
         try {
             iHub.loadLabs(labSearhDto, labResults, hubDto);
             setAttribute(LVConstants.SUCCESS_MESSAGE, " Labs has been successfully submitted to " + target);
             List<Long> clinicalIds = LVUtils.convertStringToList(labSearhDto.getLabIds(), ",");
             new LabSearchDAO().saveLoadLabStatus(clinicalIds, target);
             labSearhDto.setLabIds(null);
             return list();
         } catch (LVException lve) {
             setAttribute(LVConstants.FAILURE_MESSAGE, lve.getMessage());
         }
         return SUCCESS;
     }
     /**
      * 
      * @param labSearhDto labSearhDto
      */
     public void setLabSearhDto(LabSearchDto labSearhDto) {
         this.labSearhDto = labSearhDto;
     }
 
     /**
      * 
      * @return labSearhDto
      */
     public LabSearchDto getLabSearhDto() {
         return labSearhDto;
     }
 
     /**
      * 
      * @return labResults
      */
     public List<LabSearchDto> getLabResults() {
         return labResults;
     }
 
     /**
      * 
      * @param labResults labResults
      */
     public void setLabResults(List<LabSearchDto> labResults) {
         this.labResults = labResults;
     }
 
     /**
      * 
      * @return studySubjectGridId
      */
     public String getStudySubjectGridId() {
         return studySubjectGridId;
     }
 
     /**
      * 
      * @param studySubjectGridId studySubjectGridId
      */
     public void setStudySubjectGridId(String studySubjectGridId) {
         this.studySubjectGridId = studySubjectGridId;
     }
 }
