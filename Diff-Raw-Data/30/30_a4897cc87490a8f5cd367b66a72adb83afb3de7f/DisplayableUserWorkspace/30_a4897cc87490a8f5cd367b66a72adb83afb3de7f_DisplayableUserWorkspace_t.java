 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caArray
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caArray Software License (the License) is between NCI and You. You (or 
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
  * its rights in the caArray Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caArray Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator Software and any 
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
 package gov.nih.nci.caintegrator2.web;
 
 import gov.nih.nci.caintegrator2.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.web.action.query.DisplayableQueryResult;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.util.ValueStack;
 
 /**
  * Session singleton object used for displaying user workspace items such as Study's, Queries, and Lists.
  */
 public class DisplayableUserWorkspace {
 
     /**
      * Value to use in UI for no study in drop down list.
      */
     public static final Long NO_STUDY_SELECTED_ID = Long.valueOf(-1L);
     
     private static final String USER_WORKSPACE_VALUE_STACK_KEY = "workspace";
     private static final String CURRENT_STUDY_SUBSCRIPTION_VALUE_STACK_KEY = "studySubscription";
     private static final String CURRENT_STUDY_VALUE_STACK_KEY = "study";
     private static final String CURRENT_QUERY_VALUE_STACK_KEY = "query";
     private static final String CURRENT_QUERY_RESULT_VALUE_STACK_KEY = "queryResult";
     private static final String CURRENT_GENOMIC_RESULT_VALUE_STACK_KEY = "genomicDataQueryResult";
     
     private Long currentStudySubscriptionId;
     private Query query;
     private DisplayableQueryResult queryResult;
     private GenomicDataQueryResult genomicDataQueryResult;
 
     /**
      * Refreshes the workspace for this session, ensuring it is attached to the current Hibernate request.
      *
      * @param workspaceService service used to 
      */
     void refresh(WorkspaceService workspaceService) {
         setUserWorkspace(workspaceService.getWorkspace());
         if (getCurrentStudySubscriptionId() == null  && getUserWorkspace().getDefaultSubscription() != null) {
             currentStudySubscriptionId = getUserWorkspace().getDefaultSubscription().getId();
         }
        putCurrentStudyOnValueStack();
         refreshQuery();
        putQueryObjectsOnValueStack();
     }
 
     private void refreshQuery() {
         if (query != null) {
             if (query.getId() != null) {
                 retrieveQueryFromStudySubscription();
             } else {
                 refreshQueryAssociations();
             }
         }
     }
 
     private void refreshQueryAssociations() {
         query.setSubscription(getCurrentStudySubscription());
     }
 
     private void retrieveQueryFromStudySubscription() {
         for (Query nextQuery : getCurrentStudySubscription().getQueryCollection()) {
             if (nextQuery.equals(query)) {
                 query = nextQuery;
             }
         }
     }
 
     /**
      * @return the userWorkspace
      */
     public UserWorkspace getUserWorkspace() {
         return (UserWorkspace) getValueStack().findValue(USER_WORKSPACE_VALUE_STACK_KEY);
     }
 
     private ValueStack getValueStack() {
         return ActionContext.getContext().getValueStack();
     }
 
     private void setUserWorkspace(UserWorkspace userWorkspace) {
         getValueStack().set(USER_WORKSPACE_VALUE_STACK_KEY, userWorkspace);
     }
 
     /**
      * @return the currentStudySubscription
      */
     public StudySubscription getCurrentStudySubscription() {
         return (StudySubscription) getValueStack().findValue(CURRENT_STUDY_SUBSCRIPTION_VALUE_STACK_KEY);
     }
 
     /**
      * @param currentStudySubscription the currentStudySubscription to set
      */
     public void setCurrentStudySubscription(StudySubscription currentStudySubscription) {
         if (currentStudySubscription == null) {
             setCurrentStudySubscriptionId(null);
         } else {
             setCurrentStudySubscriptionId(currentStudySubscription.getId());
         }
     }
 
     /**
      * @return the currentStudySubscriptionId
      */
     public Long getCurrentStudySubscriptionId() {
         return currentStudySubscriptionId;
     }
 
     /**
      * @param currentStudySubscriptionId the currentStudySubscriptionId to set
      */
     public void setCurrentStudySubscriptionId(Long currentStudySubscriptionId) {
        if (currentStudySubscriptionId == null || currentStudySubscriptionId.equals(NO_STUDY_SELECTED_ID)) {
            setQuery(null);
            setQueryResult(null);
            setGenomicDataQueryResult(null);
        }
         this.currentStudySubscriptionId = currentStudySubscriptionId;
        putCurrentStudyOnValueStack();
     }
 
    private void putCurrentStudyOnValueStack() {
         if (getUserWorkspace() == null) {
             return;
         }
         StudySubscription currentStudySubscription = null;
         Study currentStudy = null;
         for (StudySubscription subscription : getUserWorkspace().getSubscriptionCollection()) {
             if (subscription.getId().equals(getCurrentStudySubscriptionId())) {
                 currentStudySubscription = subscription;
                 currentStudy = subscription.getStudy();
             }
         }
         getValueStack().set(CURRENT_STUDY_SUBSCRIPTION_VALUE_STACK_KEY, currentStudySubscription);
         getValueStack().set(CURRENT_STUDY_VALUE_STACK_KEY, currentStudy);
    }
    
    private void putQueryObjectsOnValueStack() {
         getValueStack().set(CURRENT_QUERY_VALUE_STACK_KEY, getQuery());
         getValueStack().set(CURRENT_QUERY_RESULT_VALUE_STACK_KEY, getQueryResult());
         getValueStack().set(CURRENT_GENOMIC_RESULT_VALUE_STACK_KEY, getGenomicDataQueryResult());
     }
 
     /**
      * @return the query
      */
     public Query getQuery() {
         return query;
     }
 
     /**
      * @param query the query to set
      */
     public void setQuery(Query query) {
         this.query = query;
         getValueStack().set(CURRENT_QUERY_VALUE_STACK_KEY, query);
     }
 
     /**
      * @return the queryResult
      */
     public DisplayableQueryResult getQueryResult() {
         return queryResult;
     }
 
     /**
      * @param queryResult the queryResult to set
      */
     public void setQueryResult(DisplayableQueryResult queryResult) {
         this.queryResult = queryResult;
         getValueStack().set(CURRENT_QUERY_RESULT_VALUE_STACK_KEY, queryResult);
     }
 
     /**
      * @return the genomicDataQueryResult
      */
     public GenomicDataQueryResult getGenomicDataQueryResult() {
         return genomicDataQueryResult;
     }
 
     /**
      * @param genomicDataQueryResult the genomicDataQueryResult to set
      */
     public void setGenomicDataQueryResult(GenomicDataQueryResult genomicDataQueryResult) {
         this.genomicDataQueryResult = genomicDataQueryResult;
         getValueStack().set(CURRENT_GENOMIC_RESULT_VALUE_STACK_KEY, genomicDataQueryResult);
     }
 
 }
