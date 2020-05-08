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
 
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.coppa.po.Id;
 import gov.nih.nci.lv.auth.LabViewerAuthorizationHelper;
 import gov.nih.nci.lv.dao.StudySearchDAO;
 import gov.nih.nci.lv.domain.Protocol;
 import gov.nih.nci.lv.dto.IntegrationHubDto;
 import gov.nih.nci.lv.dto.StudyParticipantSearchDto;
 import gov.nih.nci.lv.dto.StudySearchDto;
 import gov.nih.nci.lv.util.LVConstants;
 import gov.nih.nci.lv.util.LVException;
 import gov.nih.nci.lv.util.LVPropertyReader;
 
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
import org.apache.log4j.Logger;
 import org.apache.struts2.ServletActionContext;
 import org.globus.gsi.GlobusCredential;
 import org.iso._21090.II;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.Preparable;
 /**
  * 
  * @author NAmiruddin
  *
  */
 public class LabViewerAction extends ActionSupport implements Preparable {
     
     private static final long serialVersionUID = 1234573645L;
     private Long studyProtocolId = null;
     private Long studyParticipantId = null;
    private static Logger logger = Logger.getLogger(LabViewerAction.class);
     
     /**
      * {@inheritDoc}
      */    
     public void prepare() {
         
     }
     /**
      * {@inheritDoc}
      */    
     public String execute() throws Exception {
         return SUCCESS;
     }
 
     /**
     *
     * @return studyProtocolId
     */
    public Long getStudyProtocolId() {
        if (studyProtocolId == null) {
            // if the value is not, then it will be in session
            return getStudyProtocolIdFromSession();
        }
        return studyProtocolId;
    }
 
    /**
    *
    * @return studyProtocolId
    */
    Long getStudyProtocolIdentifier() {
       return studyProtocolId;
    }
    
    
 
    /**
    *
    * @return studyProtocolId
    */
     public Long getStudyProtocolIdFromSession() {
        StudySearchDto ssDto = (StudySearchDto) getSession().getAttribute(LVConstants.STUDY_SEARCH_DTO);
        return (ssDto != null ? ssDto.getId() : null);
    }
     /**
     *
     * @return studyProtocolId
     */
     public Long getStudyPartIdFromSession() {
         StudyParticipantSearchDto spsDto = (StudyParticipantSearchDto) getSession().getAttribute(
              LVConstants.STUDY_PART_SEARCH_DTO);
         return (spsDto != null ? spsDto.getId() : null);
     }   
    /**
     * 
     * @return protocol
     */
     Protocol createProtocolObj() {
        Protocol protocol = new Protocol();
        protocol.setId(studyProtocolId);
        return protocol;
     }
    /**
     *
     * @param studyProtocolId studyProtocolId
     */
    public void setStudyProtocolId(Long studyProtocolId) {
        this.studyProtocolId = studyProtocolId;
    }
    
    
    /**
     * 
     * @return studyParticipantId
     */
     public Long getStudyParticipantId() {
         if (studyParticipantId == null) {
             return getStudyPartIdFromSession();
         }
         return studyParticipantId;
     }
     /**
      * 
      * @param studyParticipantId studyParticipantId
      */
     public void setStudyParticipantId(Long studyParticipantId) {
         this.studyParticipantId = studyParticipantId;
     }
     
     String getUserName() {
         return (String) getSessionAttr(LVConstants.USER_NAME);
     }
     /**
      * return the current http session.
      * @return HttpSession
      */
     public HttpSession getSession() {
         return ServletActionContext.getRequest().getSession();
     }
     
    
     /**
      * return the current http session.
      * @return HttpServletRequest
      */
     public HttpServletRequest getRequest() {
         return ServletActionContext.getRequest();
     }   
     
     void setAttribute(String arg0, Object arg1) {
         ServletActionContext.getRequest().setAttribute(arg0, arg1);
     }
     void setSession(String arg0, Object arg1) {
         ServletActionContext.getRequest().getSession().setAttribute(arg0, arg1);
     }
     Object getSessionAttr(String arg0) {
         return getSession().getAttribute(arg0);
     }
     void setStudyProtocolInfo() throws Exception {
         //@todo : throw exception if it returned 0 result
         List<StudySearchDto> ssDtos = new StudySearchDAO().search(new StudySearchDto(studyProtocolId, getUserName()));
         StudySearchDto ssDto = null;
         if (!ssDtos.isEmpty()) {
             ssDto = ssDtos.get(0);
         }
         //@todo: this to be removed
         getRequest().setAttribute(LVConstants.STUDY_SEARCH_DTO, ssDto);
         getSession().setAttribute(LVConstants.STUDY_SEARCH_DTO, ssDto);
     }
     //@todo : throw exception when gridIdentifity is null
     void setUserInfoInSession() {
         String gridIDentity = (String) getSession().getAttribute(LVConstants.CAGRID_SSO_GRID_IDENTITY);
        logger.debug(" grid id = " + gridIDentity);
         if (gridIDentity != null) {
             int beginIndex = gridIDentity.lastIndexOf("=");
             int endIndex = gridIDentity.length();
             setSession(LVConstants.USER_NAME, gridIDentity.substring(beginIndex + 1, endIndex));
             setSession(LVConstants.USER_ROLES, 
                     new LabViewerAuthorizationHelper().getUserRoles((String) getSessionAttr(LVConstants.USER_NAME)));
            logger.debug(" setting user info in session for " + gridIDentity.substring(beginIndex + 1, endIndex));
         }
     }
 
     /**
      * this method will be called when coming from login using csm.
      * @throws LVException 
      */
     void setUserInfoInSession(String username) throws LVException {
         setDefaults();
         setSession(LVConstants.USER_NAME, username);
         setSession(LVConstants.USER_ROLES, 
                 new LabViewerAuthorizationHelper().getUserRoles((String) getSessionAttr(LVConstants.USER_NAME)));
         enableMenu();
     }
     
     private void setDefaults() throws LVException {
         setSession(LVConstants.ADMIN_ACCESS, "no");
         setSession(LVConstants.STUDY_ACCESS, "no");
         setSession(LVConstants.ALLOW_ACCESS, "no");
         setSession(LVConstants.CAAERS_ACCESS, "no");
         setProperty(LVPropertyReader.getProperty());
     }
     
     void setProperty(Properties prop) {
         setSession(LVConstants.VERSION, prop.getProperty(LVConstants.VERSION));
         setSession("help_link", prop.getProperty(LVConstants.HELP_LINK));
         setSession(LVConstants.CAAERS_URL, prop.getProperty(LVConstants.CAAERS_URL));
         setSession(LVConstants.C3D_URL, prop.getProperty(LVConstants.C3D_URL));
         setSession(LVConstants.HUB_URL, prop.getProperty(LVConstants.HUB_URL));
         setSession(LVConstants.WEBSSO_ENABLED, Boolean.parseBoolean(prop.getProperty(LVConstants.WEBSSO_ENABLED)));
     }
     /**
      * this method to be called after calling setuserInfoInSession.
      */
     private void enableMenu() {
         Set<SuiteRole> userRoles = (Set<SuiteRole>) getSessionAttr(LVConstants.USER_ROLES);
         // todo : throw error on null
         if (userRoles != null) {
             if (userRoles.contains(SuiteRole.SYSTEM_ADMINISTRATOR)) {
                 setSession(LVConstants.ADMIN_ACCESS, "yes");
                 setSession(LVConstants.ALLOW_ACCESS, "yes");
             }
             if (userRoles.contains(SuiteRole.LAB_DATA_USER)) {
                 setSession(LVConstants.STUDY_ACCESS, "yes");
                 setSession(LVConstants.ALLOW_ACCESS, "yes");
             }
             if (userRoles.contains(SuiteRole.AE_REPORTER)) {
                 setSession(LVConstants.CAAERS_ACCESS, "yes");
             }
         }
     }
     void labViewerSetup() throws LVException {
         setDefaults();
         setUserInfoInSession();
         enableMenu();
     }
     IntegrationHubDto getHubDto() {
         IntegrationHubDto hubDto = new IntegrationHubDto();
         hubDto.setCredentialEpr((String) getSessionAttr(LVConstants.CAGRID_SSO_DELEGATION_SERVICE_EPR));
         hubDto.setGlobusCredential((GlobusCredential) getSessionAttr(LVConstants.CAGRID_SSO_GRID_CREDENTIAL));
         hubDto.setUserName((String) getSessionAttr(LVConstants.USER_NAME));
         hubDto.setStudyProtocolExtn(((StudySearchDto) getSessionAttr(LVConstants.STUDY_SEARCH_DTO)).getNciIdentifier());
         hubDto.setHubUrl((String) getSessionAttr(LVConstants.HUB_URL));
         hubDto.setExternalIdentifier("CTODS");
         return hubDto;
     }
     
     void setStudyIdentifier(IntegrationHubDto iHubDto) {
         II assignedIdentifier = new II();
         StudySearchDto ssDto = (StudySearchDto) getSessionAttr(LVConstants.STUDY_SEARCH_DTO);
         assignedIdentifier.setRoot(LVConstants.STUDY_PROTOCOL_ROOT);
         assignedIdentifier.setExtension(ssDto.getNciIdentifier());
         iHubDto.setCoppaIi(assignedIdentifier);
         
     }
 
     Id setCoppaEntityId(String extn , String root) {
         Id identifier = new Id();
         identifier.setRoot(root);
         identifier.setExtension(extn);
         return identifier;
         
     }
     /**
      * true means suite mode, false means standalone mode.
      * @return
      * @throws LVException
      */
     boolean isSuiteMode() throws LVException {
         return  Boolean.parseBoolean(LVPropertyReader.getPropertyValue(LVConstants.WEBSSO_ENABLED));  
     }
 }
