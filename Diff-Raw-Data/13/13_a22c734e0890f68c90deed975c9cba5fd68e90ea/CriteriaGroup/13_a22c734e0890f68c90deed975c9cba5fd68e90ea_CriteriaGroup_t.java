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
 package gov.nih.nci.caintegrator2.web.action.query.form;
 
 import gov.nih.nci.caintegrator2.domain.application.AbstractAnnotationCriterion;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.AbstractGenomicCriterion;
 import gov.nih.nci.caintegrator2.domain.application.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
import gov.nih.nci.caintegrator2.domain.application.CopyNumberAlterationCriterion;
 import gov.nih.nci.caintegrator2.domain.application.FoldChangeCriterion;
 import gov.nih.nci.caintegrator2.domain.application.GeneNameCriterion;
 import gov.nih.nci.caintegrator2.domain.application.GenomicCriterionTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.IdentifierCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.application.SubjectListCriterion;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import com.opensymphony.xwork2.ValidationAware;
 
 /**
  * Contains a set of rows that are either logically ANDed or ORed together. There is a
  * single top-level group per query.
  */
 public class CriteriaGroup {
     
     private static final String NULL_ANNOTATION_AFD_TYPE = "~NULL_AFD_TYPE~";
     private final CompoundCriterion compoundCriterion;
     private final QueryForm form;
     private String criterionType = "";
     private final List<AbstractCriterionRow> rows = new ArrayList<AbstractCriterionRow>();
     
 
     CriteriaGroup(QueryForm form) {
         if (form.getQuery() == null || form.getQuery().getCompoundCriterion() == null) {
             throw new IllegalArgumentException("Argument queryForm requires an initialized query.");
         }
         this.form = form;
         this.compoundCriterion = form.getQuery().getCompoundCriterion();
         initializeCriteria(form.getQuery().getSubscription().getStudy(), compoundCriterion.getCriterionCollection());
     }
 
     private void initializeCriteria(Study study, Collection<AbstractCriterion> criterionCollection) {
         Iterator<AbstractCriterion> iterator = criterionCollection.iterator();
         while (iterator.hasNext()) {            
             addCriterionRow(study, iterator.next());
         }
     }
 
     private void addCriterionRow(Study study, AbstractCriterion criterion) {
         AbstractCriterionRow row = createRow(study, getCriterionRowType(criterion));
         rows.add(row);
         row.setCriterion(criterion);
     }
 
     private String getCriterionRowType(AbstractCriterion criterion) {
         if (criterion instanceof AbstractGenomicCriterion) {
             return getGenomicCriterionRowType(criterion);
         } else if (criterion instanceof SubjectListCriterion) {
             return CriterionRowTypeEnum.SAVED_LIST.getValue();
         } else if (criterion instanceof IdentifierCriterion) {
             return CriterionRowTypeEnum.UNIQUE_IDENTIIFER.getValue();
         } else if (criterion instanceof AbstractAnnotationCriterion) {
             return getAnnotationCriterionRowName(criterion);
         } else {
             throw new IllegalArgumentException("Unsupported criterion: " + criterion.getClass());
         }
     }
 
     private String getGenomicCriterionRowType(AbstractCriterion criterion) {
         if (criterion instanceof GeneNameCriterion
                 && GenomicCriterionTypeEnum.COPY_NUMBER.equals(((GeneNameCriterion) criterion)
                         .getGenomicCriterionType())) {
             return CriterionRowTypeEnum.COPY_NUMBER.getValue();
        } else if (criterion instanceof CopyNumberAlterationCriterion) {
            return CriterionRowTypeEnum.COPY_NUMBER.getValue();
         }
         return CriterionRowTypeEnum.GENE_EXPRESSION.getValue();
     }
 
     private String getAnnotationCriterionRowName(AbstractCriterion criterion) {
         AbstractAnnotationCriterion annotationCriterion = (AbstractAnnotationCriterion) criterion; 
         if (annotationCriterion.getAnnotationFieldDescriptor() == null 
                 || annotationCriterion.getAnnotationFieldDescriptor().getAnnotationGroup() == null) {
             return NULL_ANNOTATION_AFD_TYPE;
         }
         return annotationCriterion.getAnnotationFieldDescriptor().getAnnotationGroup().getName();
     }
 
     /**
      * @return the booleanOperator
      */
     public String getBooleanOperator() {
         return compoundCriterion.getBooleanOperator().getValue();
     }
 
     /**
      * @param booleanOperator the booleanOperator to set
      */
     public void setBooleanOperator(String booleanOperator) {
         compoundCriterion.setBooleanOperator(BooleanOperatorEnum.getByValue(booleanOperator));
     }
     
     /**
      * Removes the row at the given row number.
      * @param rowNumber to remove from list.
      */
     public void removeRow(int rowNumber) {
         rows.get(rowNumber).removeCriterionFromQuery();
         rows.remove(rowNumber);
         for (AbstractCriterionRow row : rows) {
             int rowIndex = rows.indexOf(row);
             for (AbstractCriterionParameter parameter : row.getParameters()) {
                 parameter.setRowIndex(rowIndex);
             }
         }
     }
 
     /**
      * Adds a new criterion of the currently selected type.
      * @param study to populate the controlSampleSet options
      */
     public void addCriterion(Study study) {
         rows.add(createRow(study, criterionType));
     }
 
     private AbstractCriterionRow createRow(Study study, String rowType) {
         AbstractCriterionRow criterionRow;
         if (rowType == null) {
             throw new IllegalStateException("Invalid CriterionRowTypeEnum " + rowType);
         }
         if (NULL_ANNOTATION_AFD_TYPE.equals(rowType)) {
             return new InvalidCriterionRow(this);
         }
         if (study.getAnnotationGroup(rowType) != null) {
             criterionRow = new AnnotationCriterionRow(this, rowType);
         } else {
             criterionRow = retrieveCriterionRowByType(study, rowType);
         }
         return criterionRow;
     }
 
     private AbstractCriterionRow retrieveCriterionRowByType(Study study, String rowType) {
         switch (CriterionRowTypeEnum.getByValue(rowType)) {
         case GENE_EXPRESSION:
             return new GeneExpressionCriterionRow(study, this);
         case COPY_NUMBER:
             return new CopyNumberCriterionRow(study, this);
         case SAVED_LIST:
             return new SavedListCriterionRow(this);
         case UNIQUE_IDENTIIFER:
             return new IdentifierCriterionRow(this);
         default:
             throw new IllegalStateException("Invalid CriterionRowTypeEnum " + rowType);
         }
     }
 
     /**
      * @return the criterionTypeName
      */
     public String getCriterionTypeName() {
         return criterionType;
     }
 
     /**
      * @param criterionTypeName the criterionTypeName to set
      */
     public void setCriterionTypeName(String criterionTypeName) {
         criterionType = criterionTypeName;
     }
     
     StudySubscription getSubscription() {
         return getForm().getQuery().getSubscription();
     }
 
     QueryForm getForm() {
         return form;
     }
 
     /**
      * @return the rows
      */
     public List<AbstractCriterionRow> getRows() {
         return rows;
     }
 
     CompoundCriterion getCompoundCriterion() {
         return compoundCriterion;
     }
 
     void validate(ValidationAware action) {
         for (AbstractCriterionRow row : rows) {
             row.validate(action);
         }
     }
 
     void processCriteriaChanges() {
         for (AbstractCriterionRow row : getRows()) {
             row.processCriteriaChanges();
         }
     }
     
     /**
      * @return boolean of having no gene expression criterion
      */
     public boolean hasNoGeneExpressionCriterion() {
         for (AbstractCriterionRow row : getRows()) {
             if (row.getCriterion() instanceof GeneNameCriterion
                     || row.getCriterion() instanceof FoldChangeCriterion) {
                 return false;
             }
         }
         return true;
     }
 }
