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
 package gov.nih.nci.caintegrator2.web.action.abstractlist;
 
 import gov.nih.nci.caintegrator2.domain.application.GeneList;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.file.FileManager;
 import gov.nih.nci.caintegrator2.web.action.AbstractDeployedStudyAction;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * Provides functionality to list and add array designs.
  */
 @SuppressWarnings("PMD") // See createPlatform method
 public class ManageGeneListAction extends AbstractDeployedStudyAction {
 
     private static final long serialVersionUID = 1L;
     private FileManager fileManager;
     private File geneListFile;
     private String geneListFileContentType;
     private String geneListFileFileName;
     private String geneListName;
     private String description;
     private String geneSymbols;
     private String selectedAction;
     private String geneListId;
     private List<String> geneSymbolList = new ArrayList<String>();
 
     private static final String CREATE_GENE_LIST_ACTION = "createGeneList";
     private static final String GENE_LIST_NAME = "geneListName";
     private static final String GENE_LIST_FILE = "geneListFile";
     private static final String EDIT_PAGE = "editPage";
 
     /**
      * {@inheritDoc}
      */
    public void prepare() {
        super.prepare();
        setInvalidStudyBeingAccessed(false);
    }
    
    /**
     * {@inheritDoc}
     */
     @Override
     protected boolean isFileUpload() {
        return true;
     }
     
     /**
      * @return the Struts result.
      */
     public String execute() {
         if (CREATE_GENE_LIST_ACTION.equals(selectedAction)) {
             return createGeneList();
         }
         return SUCCESS;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         clearErrorsAndMessages();
        prepareValueStack();
        if (getCurrentStudy() == null) {
            setInvalidStudyBeingAccessed(true);
        }
         if (CREATE_GENE_LIST_ACTION.equalsIgnoreCase(selectedAction)) {
             validateGeneListName();
             validateGeneListData();
         } else {
             super.validate();
         }
     }
     
     private void validateGeneListName() {
         if (StringUtils.isEmpty(getGeneListName())) {
             addFieldError(GENE_LIST_NAME, "Gene List name is required");
         } else if (getStudySubscription().getGeneList(getGeneListName()) != null) {
             addFieldError(GENE_LIST_NAME, "Gene List name is duplicate: " + getGeneListName());
         }
     }
     
     private void validateGeneListData() {
         geneSymbolList.clear();
         extractGeneSymbols(getGeneListFile());
         extractGeneSymbols(getGeneSymbols());
         if (geneSymbolList.isEmpty()) {
             addActionError("There is nothing to save, you need to enter some genes or upload from a file.");
         }
     }
     
     private void extractGeneSymbols(String inputSymbols) {
         if (!StringUtils.isEmpty(inputSymbols)) {
             for (String symbol : inputSymbols.split(",")) {
                 geneSymbolList.add(symbol.trim());
             }
         }
     }
     
     private void extractGeneSymbols(File uploadFile) {
         if (uploadFile != null) {
             CSVReader reader;
             try {
                 reader = new CSVReader(new FileReader(uploadFile));
                 String[] symbols;
                 while ((symbols = reader.readNext()) != null) {
                     for (String symbol : symbols) {
                         geneSymbolList.add(symbol.trim()); 
                     }
                 }
                 if (geneSymbolList.isEmpty()) {
                     addFieldError(GENE_LIST_FILE, "The upload file is empty");
                 }
             } catch (IOException e) {
                 addFieldError(GENE_LIST_FILE, " Error reading gene list file");
             }
         }
     }
 
     /**
      * @return SUCCESS
      */
     private String createGeneList() {
         GeneList geneList = new GeneList();
         geneList.setName(getGeneListName());
         geneList.setDescription(getDescription());
         geneList.setSubscription(getStudySubscription());
         getWorkspaceService().createGeneList(geneList, geneSymbolList);
         return EDIT_PAGE;
     }
 
     /**
      * @return the geneListFile
      */
     public File getGeneListFile() {
         return geneListFile;
     }
 
     /**
      * @param geneListFile the geneListFile to set
      */
     public void setGeneListFile(File geneListFile) {
         this.geneListFile = geneListFile;
     }
 
     /**
      * @return the geneListFileContentType
      */
     public String getGeneListFileContentType() {
         return geneListFileContentType;
     }
 
     /**
      * @param geneListFileContentType the geneListFileContentType to set
      */
     public void setGeneListFileContentType(String geneListFileContentType) {
         this.geneListFileContentType = geneListFileContentType;
     }
 
     /**
      * @return the geneListFileFileName
      */
     public String getGeneListFileFileName() {
         return geneListFileFileName;
     }
 
     /**
      * @param geneListFileFileName the platformFileFileName to set
      */
     public void setGeneListFileFileName(String geneListFileFileName) {
         this.geneListFileFileName = geneListFileFileName;
     }
 
     /**
      * @return the fileManager
      */
     public FileManager getFileManager() {
         return fileManager;
     }
 
     /**
      * @param fileManager the fileManager to set
      */
     public void setFileManager(FileManager fileManager) {
         this.fileManager = fileManager;
     }
     
     /**
      * @return the selectedAction
      */
     public String getSelectedAction() {
         return selectedAction;
     }
 
     /**
      * @param selectedAction the selectedAction to set
      */
     public void setSelectedAction(String selectedAction) {
         this.selectedAction = selectedAction;
     }
 
     /**
      * @return the geneListName
      */
     public String getGeneListName() {
         return geneListName;
     }
 
     /**
      * @param geneListName the geneListName to set
      */
     public void setGeneListName(String geneListName) {
         this.geneListName = geneListName;
     }
 
     /**
      * @return the geneListId
      */
     public String getGeneListId() {
         return geneListId;
     }
 
     /**
      * @param geneListId the geneListId to set
      */
     public void setGeneListId(String geneListId) {
         this.geneListId = geneListId;
     }
 
     /**
      * @return the geneSymbols
      */
     public String getGeneSymbols() {
         return geneSymbols;
     }
 
     /**
      * @param geneSymbols the geneSymbols to set
      */
     public void setGeneSymbols(String geneSymbols) {
         this.geneSymbols = geneSymbols;
     }
 
     /**
      * @return the description
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * @param description the description to set
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * @return the geneSymbolList
      */
     public List<String> getGeneSymbolList() {
         return geneSymbolList;
     }
     
     /**
      * @return the study subscription
      */
     public StudySubscription getSubscription() {
         return getStudySubscription();
     }
 }
