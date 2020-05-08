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
 package gov.nih.nci.caintegrator2.web.action.analysis.cabio;
 
 import gov.nih.nci.cabio.domain.Pathway;
 import gov.nih.nci.cabio.domain.Taxon;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.cabio.CaBioDisplayableGene;
 import gov.nih.nci.caintegrator2.external.cabio.CaBioDisplayablePathway;
 import gov.nih.nci.caintegrator2.external.cabio.CaBioFacade;
 import gov.nih.nci.caintegrator2.external.cabio.CaBioSearchParameters;
 import gov.nih.nci.caintegrator2.web.action.AbstractCaIntegrator2Action;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * 
  */
 public class CaBioSearchAction extends AbstractCaIntegrator2Action {
 
     private static final long serialVersionUID = 1L;
     
     private CaBioFacade caBioFacade;
     private CaBioSearchParameters searchParams = new CaBioSearchParameters();
     private List<CaBioDisplayableGene> caBioGenes = new ArrayList<CaBioDisplayableGene>(); 
     private final List<String> taxonList = new ArrayList<String>();
 
     // JSP Form Hidden Variables
     private String geneSymbolElementId;
     private boolean caBioGeneSearchTopicPublished = false;
     private boolean runSearchSelected = false;
     private boolean runCaBioGeneSearchFromPathways = false;
     // This variable is a space delimited string which contains the indeces of the checked pathway checkboxes.
     private String checkedPathwayBoxes = "";
    private boolean ableToConnect = false;
     
     /**
      * {@inheritDoc}
      */
     public String input() {
         try {
             reset();
            ableToConnect = true;
        } catch (Exception e) {
            addActionError("Unable to connect to caBio: " + e.getMessage());
            ableToConnect = false;
         }
         return SUCCESS;
     }
 
     private void reset() throws ConnectionException {
         getCaBioPathways().clear();
         taxonList.clear();
         taxonList.add(CaBioSearchParameters.ALL_TAXONS);
         taxonList.addAll(caBioFacade.retrieveAllTaxons());
         searchParams = new CaBioSearchParameters();
     }
     
     /**
      * Searches caBio for genes.
      * @return struts result.
      */
     public String searchCaBio() {
         if (runSearchSelected) {
             runSearchSelected = false;
             if (StringUtils.isBlank(searchParams.getKeywords())) {
                 addActionError("Must enter keywords to search on.");
                 return INPUT;
             }
             return retrieveDisplayableValues();
         } else if (runCaBioGeneSearchFromPathways) {
             return runGeneSearchFromPathways();
         }
         return SUCCESS;
     }
 
     private String retrieveDisplayableValues() {
         switch (searchParams.getSearchType()) {
         case GENE_KEYWORDS:
             return retrieveGenesFromCaBio(false);
         case GENE_SYMBOL:
             return retrieveGenesFromCaBio(false);
         case PATHWAYS:
             return retrievePathwaysFromCaBio();
         default:
             addActionError("Unknown search type.");
             return INPUT;
         }
     }
     
     private String runGeneSearchFromPathways() {
         runCaBioGeneSearchFromPathways = false;
         searchParams.getPathways().clear();
         retrievePathwaysFromGuiCheckboxes();
         if (searchParams.getPathways().isEmpty()) {
             addActionError("Must select pathways to search for genes.");
             return INPUT;
         }
         return retrieveGenesFromCaBio(true);
     }
 
     /**
      * Because the GUI doesn't set the checkboxes from the results page, it is artificially set in the 
      * "checkedPathwayBoxes" string variable which is a space delimited string where the values are the 
      * indexes of the checked boxes.  From that we can retrieve the actual pathways the user checked.
      */
     private void retrievePathwaysFromGuiCheckboxes() {
         Set<String> checkedPathwayIndeces = new HashSet<String>();
         checkedPathwayIndeces.addAll(Arrays.asList(checkedPathwayBoxes.split(" ")));
         Integer index = 0;
         for (CaBioDisplayablePathway displayablePathway : getCaBioPathways()) {
             if (checkedPathwayIndeces.contains(index.toString())) {
                 displayablePathway.setChecked(true);
                 Pathway pathway = new Pathway();
                 pathway.setId(Long.valueOf(displayablePathway.getId()));
                 if (!CaBioSearchParameters.ALL_TAXONS.equals(searchParams.getTaxon())) {
                     Taxon taxon = new Taxon();
                     taxon.setCommonName(searchParams.getTaxon());
                     pathway.setTaxon(taxon);
                 }
                 searchParams.getPathways().add(pathway);
             } else {
                 displayablePathway.setChecked(false);
             }
             index++;
         }
     }
 
     private String retrieveGenesFromCaBio(boolean isSearchFromPathwaysSelected) {
         try {
             searchParams.setStudy(getCurrentStudy());
             getCaBioPathways().clear();
             if (isSearchFromPathwaysSelected) {
                 caBioGenes = caBioFacade.retrieveGenesFromPathways(searchParams);
             } else {
                 caBioGenes = caBioFacade.retrieveGenes(searchParams);
             }
             if (caBioGenes.isEmpty()) {
                 addActionError("No results were returned from search.");
                 return INPUT;
             }
         } catch (ConnectionException e) {
             addActionError("Unable to connect to caBio.");
             return ERROR;
         }
         return SUCCESS;
     }
     
     private String retrievePathwaysFromCaBio() {
         try {
             searchParams.setStudy(getCurrentStudy());
             caBioGenes.clear();
             getCaBioPathways().clear();
             getCaBioPathways().addAll(caBioFacade.retrievePathways(searchParams));
             if (getCaBioPathways().isEmpty()) {
                 addActionError("No results were returned from search.");
                 return INPUT;
             }
         } catch (ConnectionException e) {
             addActionError("Unable to connect to caBio.");
             return ERROR;
         }
         return SUCCESS;
     }
 
     /**
      * @return the caBioFacade
      */
     public CaBioFacade getCaBioFacade() {
         return caBioFacade;
     }
 
     /**
      * @param caBioFacade the caBioFacade to set
      */
     public void setCaBioFacade(CaBioFacade caBioFacade) {
         this.caBioFacade = caBioFacade;
     }
 
     /**
      * @return the caBioGenes
      */
     public List<CaBioDisplayableGene> getCaBioGenes() {
         return caBioGenes;
     }
 
     /**
      * @param caBioGenes the caBioGenes to set
      */
     public void setCaBioGenes(List<CaBioDisplayableGene> caBioGenes) {
         this.caBioGenes = caBioGenes;
     }
 
     /**
      * @return the caBioPathways
      */
     public List<CaBioDisplayablePathway> getCaBioPathways() {
         return getDisplayableWorkspace().getCaBioPathways();
     }
 
     /**
      * @return the runSearchSelected
      */
     public boolean isRunSearchSelected() {
         return runSearchSelected;
     }
 
     /**
      * @param runSearchSelected the runSearchSelected to set
      */
     public void setRunSearchSelected(boolean runSearchSelected) {
         this.runSearchSelected = runSearchSelected;
     }
 
     /**
      * @return the runCaBioGeneSearchFromPathways
      */
     public boolean isRunCaBioGeneSearchFromPathways() {
         return runCaBioGeneSearchFromPathways;
     }
 
     /**
      * @param runCaBioGeneSearchFromPathways the runCaBioGeneSearchFromPathways to set
      */
     public void setRunCaBioGeneSearchFromPathways(boolean runCaBioGeneSearchFromPathways) {
         this.runCaBioGeneSearchFromPathways = runCaBioGeneSearchFromPathways;
     }
 
     /**
      * @return the geneSymbolElementId
      */
     public String getGeneSymbolElementId() {
         return geneSymbolElementId;
     }
 
     /**
      * @param geneSymbolElementId the geneSymbolElementId to set
      */
     public void setGeneSymbolElementId(String geneSymbolElementId) {
         this.geneSymbolElementId = geneSymbolElementId;
     }
 
     /**
      * @return the caBioGeneSearchTopicPublished
      */
     public boolean isCaBioGeneSearchTopicPublished() {
         return caBioGeneSearchTopicPublished;
     }
 
     /**
      * @param caBioGeneSearchTopicPublished the caBioGeneSearchTopicPublished to set
      */
     public void setCaBioGeneSearchTopicPublished(boolean caBioGeneSearchTopicPublished) {
         this.caBioGeneSearchTopicPublished = caBioGeneSearchTopicPublished;
     }
 
     /**
      * @return the searchParams
      */
     public CaBioSearchParameters getSearchParams() {
         return searchParams;
     }
 
     /**
      * @param searchParams the searchParams to set
      */
     public void setSearchParams(CaBioSearchParameters searchParams) {
         this.searchParams = searchParams;
     }
 
     /**
      * @return the taxonList
      */
     public List<String> getTaxonList() {
         return taxonList;
     }
 
     /**
      * @return the checkedPathwayBoxes
      */
     public String getCheckedPathwayBoxes() {
         return checkedPathwayBoxes;
     }
 
     /**
      * @param checkedPathwayBoxes the checkedPathwayBoxes to set
      */
     public void setCheckedPathwayBoxes(String checkedPathwayBoxes) {
         this.checkedPathwayBoxes = checkedPathwayBoxes;
     }
 
    /**
     * @return the ableToConnect
     */
    public boolean isAbleToConnect() {
        return ableToConnect;
    }

 }
