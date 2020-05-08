 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The ProtExpress
  * Software was developed in conjunction with the National Cancer Institute
  * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
  * government employees are authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  *
  * This ProtExpress Software License (the License) is between NCI and You. You (or
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
  * its rights in the ProtExpress Software to (i) use, install, access, operate,
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the ProtExpress Software; (ii) distribute and
  * have distributed to and by third parties the ProtExpress Software and any
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
  * developed by 5AM and the National Cancer Institute. If You do not include
  * such end-user documentation, You shall include this acknowledgment in the
  * Software itself, wherever such third-party acknowledgments normally appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", or "5AM"
  * to endorse or promote products derived from this Software. This License does
  * not authorize You to use any trademarks, service marks, trade names, logos or
  * product names of either NCI or 5AM, except as required to comply with the
  * terms of this License.
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this
  * Software into Your proprietary programs and into any third party proprietary
  * programs. However, if You incorporate the Software into third party
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software
  * into such third party proprietary programs and for informing Your
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
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC. OR THEIR
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package gov.nih.nci.protexpress.util;
 
 import gov.nih.nci.protexpress.domain.experiment.ExperimentRun;
 import gov.nih.nci.protexpress.domain.protocol.InputOutputObject;
 import gov.nih.nci.protexpress.domain.protocol.Protocol;
 import gov.nih.nci.protexpress.domain.protocol.ProtocolApplication;
 
 import java.util.HashMap;
 
 /**
  * Helper class to maintain the Experiment Run, Protocol Application and Action
  * Set information.
  *
  * @author Krishna Kanchinadam
  */
 public final class ExperimentRunHolder {
 
     private ExperimentRun experimentRun;
     private Protocol startProtocol;
     private Protocol endProtocol;
     private String startProtocolLsidString;
     private String endProtocolLsidString;
 
     private ProtocolApplication startProtocolApplication;
     private ProtocolApplication endProtocolApplication;
     private String startProtocolApplicationLsidString;
     private String endProtocolApplicationLsidString;
 
     private HashMap<Integer, ProtocolAction> protocolActionSequenceNumberMap = new HashMap<Integer, ProtocolAction>();
     private HashMap<Long, ProtocolAction> protocolApplicationIdMap = new HashMap<Long, ProtocolAction>();
 
     private final Integer initialActionSequenceNumber = 1;
     private final Integer incrementActionSequence = 5;
     private Integer currentSequenceNumber = 1;
 
     private ProtocolAction startProtocolAction;
     private ProtocolAction endProtocolAction;
 
     /**
      * Default constructor.
      *
      * @param experimentRun
      *            the experiment run.
      */
     public ExperimentRunHolder(ExperimentRun experimentRun) {
         this.experimentRun = experimentRun;
         this.initStartAndEndProtocolsAndApplications();
         this.initProtocolActions();
     }
 
     private void initStartAndEndProtocolsAndApplications() {
         initStartProtocolAndApplication();
         initEndProtocolAndApplication();
     }
 
     private void initStartProtocolAndApplication() {
         String lsidString = "ExperimentRun." + getExperimentRun().getId().toString() + ".StartProtocol";
         setStartProtocolLsidString(lsidString);
 
         startProtocol = new Protocol(getStartProtocolLsidString());
         startProtocol.setDescription("This protocol is the 'parent' protocol of the experiment run.");
 
         lsidString = "ExperimentRun." + getExperimentRun().getId().toString() + ".StartProtocolApplication";
         startProtocolApplication = new ProtocolApplication(
                 getExperimentRun().getDatePerformed(), getExperimentRun(), startProtocol);
         setStartProtocolApplicationLsidString(lsidString);
 
         // set starting inputs for the start protocol application.
         for (ProtocolApplication protApp : getExperimentRun().getProtocolApplications()) {
             for (InputOutputObject input : protApp.getInputs()) {
                 if (input.getOutputOfProtocolApplication() == null) {
                     startProtocolApplication.getInputs().add(input);
                 }
             }
         }
     }
 
     private void initEndProtocolAndApplication() {
         String lsidString = "ExperimentRun." + getExperimentRun().getId().toString() + ".EndProtocol";
         setEndProtocolLsidString(lsidString);
 
         endProtocol = new Protocol(getEndProtocolLsidString());
         String description = "Mark the output data or materials for the experiment run.  "
                 + "Any and all inputs to an application of this type are considered outputs of the experiment run.";
         endProtocol.setDescription(description);
 
         lsidString = "ExperimentRun." + getExperimentRun().getId().toString() + ".EndProtocolApplication";
         endProtocolApplication = new ProtocolApplication(
                 getExperimentRun().getDatePerformed(), getExperimentRun(), endProtocol);
         setEndProtocolApplicationLsidString(lsidString);
 
      // set final outputs for the end protocol application.
         for (ProtocolApplication protApp : getExperimentRun().getProtocolApplications()) {
             for (InputOutputObject output : protApp.getOutputs()) {
                 if (output.getInputToProtocolApplication() == null) {
                     endProtocolApplication.getInputs().add(output);
                 }
             }
         }
     }
 
     /**
      * Initializes the protocolActions.
      */
     private void initProtocolActions() {
         this.initStartProtocolAction();
         // Determine the protocol actions.
         setCurrentSequenceNumber(getInitialActionSequenceNumber());
         for (ProtocolApplication protocolApplication : getExperimentRun().getProtocolApplications()) {
             initProtocolAction(protocolApplication, getCurrentSequenceNumber());
         }
         this.initEndProtocolAction();
 
         // By now, all protocol actions (type=protocol application) should have been setup properly.
         // Determine Predecessor actions.
         for (ProtocolAction protAction : getProtocolActionSequenceNumberMap().values()) {
             boolean includeStartPredecessor = false;
             for (InputOutputObject input : protAction.getProtocolApplication().getInputs()) {
                 if (input.getOutputOfProtocolApplication() != null) {
                     ProtocolAction predecessorAction = getProtocolActionFromProtocolApplicationIdMap(
                             input.getOutputOfProtocolApplication().getId());
                    protAction.getPredecessorActionNumbers().add(predecessorAction.getActionSequenceNumber());
                 } else {
                     includeStartPredecessor = true;
                 }
             }
             // If at least one starting input, have the start protocol action as a predecessor.
             if (includeStartPredecessor) {
                 protAction.getPredecessorActionNumbers().add(getInitialActionSequenceNumber());
             }
         }
     }
 
     private void initStartProtocolAction() {
         ProtocolApplication protApp = new ProtocolApplication(
                 getExperimentRun().getDatePerformed(), getExperimentRun(), getStartProtocol());
         startProtocolAction = new ProtocolAction(protApp, getInitialActionSequenceNumber());
         startProtocolAction.getPredecessorActionNumbers().add(getInitialActionSequenceNumber());
     }
 
     private void initEndProtocolAction() {
         ProtocolApplication protApp = new ProtocolApplication(
                 getExperimentRun().getDatePerformed(), getExperimentRun(), getEndProtocol());
         endProtocolAction = new ProtocolAction(protApp, getCurrentSequenceNumber() + getIncrementActionSequence());
         // All prot apps with "orphan" outputs are predecessors to this one.
         for (ProtocolAction protAction : getProtocolActionSequenceNumberMap().values()) {
             for (InputOutputObject output : protAction.getProtocolApplication().getOutputs()) {
                 if (output.getInputToProtocolApplication() == null) {
                     ProtocolAction predecessorAction = getProtocolActionFromProtocolApplicationIdMap(
                             output.getOutputOfProtocolApplication().getId());
                    endProtocolAction.getPredecessorActionNumbers().add(predecessorAction.getActionSequenceNumber());
                 }
             }
         }
         // If no "orphan" outputs, then list all protocols with no outputs as predecessors.
         // This will ensure that the end protocol action has at least one predecessor (start protocol).
         // and avoid an error with parsing the xar file when importing to cpas.
         if (endProtocolAction.getPredecessorActionNumbers().size() == 0) {
             for (ProtocolAction protAction : getProtocolActionSequenceNumberMap().values()) {
                 if (protAction.getProtocolApplication().getOutputs().size() == 0) {
                     endProtocolAction.getPredecessorActionNumbers().add(protAction.getActionSequenceNumber());
                 }
             }
             if (startProtocolAction.getProtocolApplication().getOutputs().size() == 0) {
                 endProtocolAction.getPredecessorActionNumbers().add(startProtocolAction.getActionSequenceNumber());
             }
         }
     }
 
     private void initProtocolAction(ProtocolApplication protocolApplication, int parentSequenceNumber) {
         if (!getProtocolApplicationIdMap().containsKey(protocolApplication.getId())) {
             // check for parents. if a parent not processed, return.
             for (InputOutputObject input : protocolApplication.getInputs()) {
                 if ((input.getOutputOfProtocolApplication() != null)
                         &&
                         (!getProtocolApplicationIdMap().containsKey(input.getOutputOfProtocolApplication().getId()))) {
                     return;
                 }
             }
 
             setCurrentSequenceNumber(parentSequenceNumber + getIncrementActionSequence());
             ProtocolAction protAction = new ProtocolAction(protocolApplication,  getCurrentSequenceNumber());
 
             // Setup the maps.
             addToProtocolActionSequenceNumberMap(getCurrentSequenceNumber(), protAction);
             addToProtocolApplicationIdMap(protocolApplication.getId(), protAction);
 
             // Recurse through the child protocols.
             for (InputOutputObject output : protocolApplication.getOutputs()) {
                 if (output.getInputToProtocolApplication() != null) {
                     initProtocolAction(output.getInputToProtocolApplication(), getCurrentSequenceNumber());
                 }
             }
         }
     }
 
     private void addToProtocolActionSequenceNumberMap(Integer actionSequenceNumber, ProtocolAction protAction) {
             getProtocolActionSequenceNumberMap().put(actionSequenceNumber, protAction);
     }
 
     private void addToProtocolApplicationIdMap(Long protAppId, ProtocolAction protAction) {
             getProtocolApplicationIdMap().put(protAppId, protAction);
     }
 
     private ProtocolAction getProtocolActionFromProtocolApplicationIdMap(Long protAppId) {
         return getProtocolApplicationIdMap().get(protAppId);
     }
 
     /**
      * Gets the startProtocolAction.
      *
      * @return the startProtocolAction.
      */
     public ProtocolAction getStartProtocolAction() {
         return startProtocolAction;
     }
 
     /**
      * Sets the startProtocolAction.
      *
      * @param startProtocolAction
      *            the startProtocolAction to set.
      */
     public void setStartProtocolAction(ProtocolAction startProtocolAction) {
         this.startProtocolAction = startProtocolAction;
     }
 
     /**
      * Gets the endProtocolAction.
      *
      * @return the endProtocolAction.
      */
     public ProtocolAction getEndProtocolAction() {
         return endProtocolAction;
     }
 
     /**
      * Sets the endProtocolAction.
      *
      * @param endProtocolAction
      *            the endProtocolAction to set.
      */
     public void setEndProtocolAction(ProtocolAction endProtocolAction) {
         this.endProtocolAction = endProtocolAction;
     }
 
     /**
      * Gets the experimentRun.
      *
      * @return the experimentRun.
      */
     public ExperimentRun getExperimentRun() {
         return experimentRun;
     }
 
     /**
      * Sets the experimentRun.
      *
      * @param experimentRun
      *            the experimentRun to set.
      */
     public void setExperimentRun(ExperimentRun experimentRun) {
         this.experimentRun = experimentRun;
     }
 
     /**
      * Gets the startProtocol.
      *
      * @return the startProtocol.
      */
     public Protocol getStartProtocol() {
         return startProtocol;
     }
 
     /**
      * Sets the startProtocol.
      *
      * @param startProtocol
      *            the startProtocol to set.
      */
     public void setStartProtocol(Protocol startProtocol) {
         this.startProtocol = startProtocol;
     }
 
     /**
      * Gets the endProtocol.
      *
      * @return the endProtocol.
      */
     public Protocol getEndProtocol() {
         return endProtocol;
     }
 
     /**
      * Sets the endProtocol.
      *
      * @param endProtocol
      *            the endProtocol to set.
      */
     public void setEndProtocol(Protocol endProtocol) {
         this.endProtocol = endProtocol;
     }
 
     /**
      * Gets the initialActionSequenceNumber.
      *
      * @return the initialActionSequenceNumber.
      */
     public Integer getInitialActionSequenceNumber() {
         return initialActionSequenceNumber;
     }
 
     /**
      * Gets the incrementActionSequence.
      *
      * @return the incrementActionSequence.
      */
     public Integer getIncrementActionSequence() {
         return incrementActionSequence;
     }
 
     /**
      * Gets the currentSequenceNumber.
      *
      * @return the currentSequenceNumber.
      */
     public Integer getCurrentSequenceNumber() {
         return currentSequenceNumber;
     }
 
     /**
      * Sets the currentSequenceNumber.
      *
      * @param currentSequenceNumber
      *            the currentSequenceNumber to set.
      */
     public void setCurrentSequenceNumber(Integer currentSequenceNumber) {
         this.currentSequenceNumber = currentSequenceNumber;
     }
 
     /**
      * Gets the protocolActionSequenceNumberMap.
      *
      * @return the protocolActionSequenceNumberMap.
      */
     public HashMap<Integer, ProtocolAction> getProtocolActionSequenceNumberMap() {
         return protocolActionSequenceNumberMap;
     }
 
     /**
      * Sets the protocolActionSequenceNumberMap.
      *
      * @param protocolActionSequenceNumberMap the protocolActionSequenceNumberMap to set.
      */
     public void setProtocolActionSequenceNumberMap(
             HashMap<Integer, ProtocolAction> protocolActionSequenceNumberMap) {
         this.protocolActionSequenceNumberMap = protocolActionSequenceNumberMap;
     }
 
     /**
      * Gets the protocolApplicationIdMap.
      *
      * @return the protocolApplicationIdMap.
      */
     public HashMap<Long, ProtocolAction> getProtocolApplicationIdMap() {
         return protocolApplicationIdMap;
     }
 
     /**
      * Sets the protocolApplicationIdMap.
      *
      * @param protocolApplicationIdMap the protocolApplicationIdMap to set.
      */
     public void setProtocolApplicationIdMap(
             HashMap<Long, ProtocolAction> protocolApplicationIdMap) {
         this.protocolApplicationIdMap = protocolApplicationIdMap;
     }
 
     /**
      * Gets the startProtocolLsidString.
      *
      * @return the startProtocolLsidString.
      */
     public String getStartProtocolLsidString() {
         return startProtocolLsidString;
     }
 
     /**
      * Sets the startProtocolLsidString.
      *
      * @param startProtocolLsidString
      *            the startProtocolLsidString to set.
      */
     public void setStartProtocolLsidString(String startProtocolLsidString) {
         this.startProtocolLsidString = startProtocolLsidString;
     }
 
     /**
      * Gets the endProtocolLsidString.
      *
      * @return the endProtocolLsidString.
      */
     public String getEndProtocolLsidString() {
         return endProtocolLsidString;
     }
 
     /**
      * Sets the endProtocolLsidString.
      *
      * @param endProtocolLsidString
      *            the endProtocolLsidString to set.
      */
     public void setEndProtocolLsidString(String endProtocolLsidString) {
         this.endProtocolLsidString = endProtocolLsidString;
     }
 
     /**
      * Gets the startProtocolApplication.
      *
      * @return the startProtocolApplication.
      */
     public ProtocolApplication getStartProtocolApplication() {
         return startProtocolApplication;
     }
 
     /**
      * Sets the startProtocolApplication.
      *
      * @param startProtocolApplication
      *            the startProtocolApplication to set.
      */
     public void setStartProtocolApplication(
             ProtocolApplication startProtocolApplication) {
         this.startProtocolApplication = startProtocolApplication;
     }
 
     /**
      * Gets the endProtocolApplication.
      *
      * @return the endProtocolApplication.
      */
     public ProtocolApplication getEndProtocolApplication() {
         return endProtocolApplication;
     }
 
     /**
      * Sets the endProtocolApplication.
      *
      * @param endProtocolApplication
      *            the endProtocolApplication to set.
      */
     public void setEndProtocolApplication(
             ProtocolApplication endProtocolApplication) {
         this.endProtocolApplication = endProtocolApplication;
     }
 
     /**
      * Gets the startProtocolApplicationLsidString.
      *
      * @return the startProtocolApplicationLsidString.
      */
     public String getStartProtocolApplicationLsidString() {
         return startProtocolApplicationLsidString;
     }
 
     /**
      * Sets the startProtocolApplicationLsidString.
      *
      * @param startProtocolApplicationLsidString
      *            the startProtocolApplicationLsidString to set.
      */
     public void setStartProtocolApplicationLsidString(
             String startProtocolApplicationLsidString) {
         this.startProtocolApplicationLsidString = startProtocolApplicationLsidString;
     }
 
     /**
      * Gets the endProtocolApplicationLsidString.
      *
      * @return the endProtocolApplicationLsidString.
      */
     public String getEndProtocolApplicationLsidString() {
         return endProtocolApplicationLsidString;
     }
 
     /**
      * Sets the endProtocolApplicationLsidString.
      *
      * @param endProtocolApplicationLsidString
      *            the endProtocolApplicationLsidString to set.
      */
     public void setEndProtocolApplicationLsidString(
             String endProtocolApplicationLsidString) {
         this.endProtocolApplicationLsidString = endProtocolApplicationLsidString;
     }
 
 }
