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
 
 import gov.nih.nci.caintegrator2.domain.application.AbstractGenomicCriterion;
 import gov.nih.nci.caintegrator2.domain.application.FoldChangeCriterion;
 import gov.nih.nci.caintegrator2.domain.application.RegulationTypeEnum;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 
 import com.opensymphony.xwork2.ValidationAware;
 
 /**
  * Wraps access to a single <code>FoldChangeCriterion</code>.
  */
 @SuppressWarnings("PMD.CyclomaticComplexity")   // anonymous inner class
 class FoldChangeCriterionWrapper extends AbstractGenomicCriterionWrapper {
     
     private static final float DEFAULT_FOLDS = 2.0f;
     private static final Float DEFAULT_FOLDS_UNCHANGED_DOWN = 0.8f;
     private static final Float DEFAULT_FOLDS_UNCHANGED_UP = 1.2f;
     private static final String SYMBOL_LABEL = "Gene Symbol";
     private static final String REGULATION_TYPE_LABEL = "Regulation Type";
     static final String FOLD_CHANGE = "Fold Change";
 
     private final FoldChangeCriterion criterion;
 
     FoldChangeCriterionWrapper(GeneExpressionCriterionRow row) {
         this(new FoldChangeCriterion(), row);
     }
 
     @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")  // bogus error; mistakenly thinks isValid is called
     FoldChangeCriterionWrapper(FoldChangeCriterion criterion, GeneExpressionCriterionRow row) {
         super(row);
         this.criterion = criterion;
         if (criterion.getRegulationType() == null) {
             criterion.setRegulationType(RegulationTypeEnum.UP);
             setCriterionDefaults();
         }
         getParameters().add(createGeneSymbolParameter());
         getParameters().add(createRegulationTypeParameter());
         addFoldsParameters();
     }
 
     private void setUpFoldsParameters() {
         setCriterionDefaults();
         removeExistingFoldsParameters();
         addFoldsParameters();
     }
 
     private void addFoldsParameters() {
         switch (criterion.getRegulationType()) {
             case UP:
                 getParameters().add(createFoldsUpParameter());
                 break;
             case DOWN:
                 getParameters().add(createFoldsDownParameter());
                 break;
             case UP_OR_DOWN:
             case UNCHANGED:
                 getParameters().add(createFoldsDownParameter());
                 getParameters().add(createFoldsUpParameter());
                 break;
             default:
                 break;
         }
     }
 
     private void removeExistingFoldsParameters() {
         if (getParameters().size() == 4) {
             getParameters().remove(3);
         }
         if (getParameters().size() == 3) {
             getParameters().remove(2);
         }
     }
 
     private void setCriterionDefaults() {
         switch (criterion.getRegulationType()) {
             case UP:
                 criterion.setFoldsUp(DEFAULT_FOLDS);
                 break;
             case DOWN:
                 criterion.setFoldsDown(DEFAULT_FOLDS);
                 break;
             case UP_OR_DOWN:
                 criterion.setFoldsUp(DEFAULT_FOLDS);
                 criterion.setFoldsDown(DEFAULT_FOLDS);
                 break;
             case UNCHANGED:
                 criterion.setFoldsDown(DEFAULT_FOLDS_UNCHANGED_DOWN);
                 criterion.setFoldsUp(DEFAULT_FOLDS_UNCHANGED_UP);
                 break;
             default:
                 break;
         }
     }
 
     private AbstractCriterionParameter createGeneSymbolParameter() {
         String fieldName = getRow().getOgnlPath() + ".parameters[0]";
         TextFieldParameter geneSymbolParameter = new TextFieldParameter(fieldName, criterion.getGeneSymbol());
         geneSymbolParameter.setLabel(SYMBOL_LABEL);
         ValueHandler geneSymbolHandler = new ValueHandlerAdapter() {
             
             public boolean isValid(String value) {
                 return !StringUtils.isBlank(value);
             }
 
             public void validate(String formFieldName, String value, ValidationAware action) {
                 if (StringUtils.isBlank(value)) {
                    action.addActionError("A value is required for Gene Sybmol");
                 }
             }
 
             public void valueChanged(String value) {
                 criterion.setGeneSymbol(value);
             }
         };
         geneSymbolParameter.setValueHandler(geneSymbolHandler);
         return geneSymbolParameter;
     }
 
     private SelectListParameter<RegulationTypeEnum> createRegulationTypeParameter() {
         OptionList<RegulationTypeEnum> options = new OptionList<RegulationTypeEnum>();
         options.addOption(RegulationTypeEnum.UP.getValue(), RegulationTypeEnum.UP);
         options.addOption(RegulationTypeEnum.DOWN.getValue(), RegulationTypeEnum.DOWN);
         options.addOption(RegulationTypeEnum.UP_OR_DOWN.getValue(), RegulationTypeEnum.UP_OR_DOWN);
         options.addOption(RegulationTypeEnum.UNCHANGED.getValue(), RegulationTypeEnum.UNCHANGED);
         ValueSelectedHandler<RegulationTypeEnum> handler = new ValueSelectedHandler<RegulationTypeEnum>() {
             public void valueSelected(RegulationTypeEnum value) {
                 criterion.setRegulationType(value);
                 setUpFoldsParameters();
             }
         };
         String fieldName = getRow().getOgnlPath() + ".parameters[1]";
         SelectListParameter<RegulationTypeEnum> regulationTypeParameter = 
             new SelectListParameter<RegulationTypeEnum>(fieldName, options, handler, criterion.getRegulationType());
         regulationTypeParameter.setLabel(REGULATION_TYPE_LABEL);
         regulationTypeParameter.setUpdateFormOnChange(true);
         return regulationTypeParameter;
     }
 
     @SuppressWarnings("PMD.CyclomaticComplexity")   // anonymous inner class
     private TextFieldParameter createFoldsUpParameter() {
         final String label = 
             RegulationTypeEnum.UNCHANGED.equals(criterion.getRegulationType()) ? "And" : "Up-regulation folds";
         int parameterIndex = RegulationTypeEnum.UP.equals(criterion.getRegulationType()) ? 2 : 3;
         String fieldName = getRow().getOgnlPath() + ".parameters[" + parameterIndex + "]";
         TextFieldParameter foldsParameter = new TextFieldParameter(fieldName, criterion.getFoldsUp().toString());
         foldsParameter.setLabel(label);
         ValueHandler foldsChangeHandler = new ValueHandlerAdapter() {
 
             public boolean isValid(String value) {
                 return NumberUtils.isNumber(value);
             }
 
             public void validate(String formFieldName, String value, ValidationAware action) {
                 if (!isValid(value)) {
                     action.addActionError("Numeric value required for " + label);
                 }
             }
 
             public void valueChanged(String value) {
                 criterion.setFoldsUp(Float.valueOf(value));
             }
         };
         foldsParameter.setValueHandler(foldsChangeHandler);
         return foldsParameter;
     }
 
     @SuppressWarnings("PMD.CyclomaticComplexity")   // anonymous inner class
     private TextFieldParameter createFoldsDownParameter() {
         final String label = 
             RegulationTypeEnum.UNCHANGED.equals(criterion.getRegulationType()) 
                 ? "Folds between" : "Down-regulation folds";
         String fieldName = getRow().getOgnlPath() + ".parameters[2]";
         TextFieldParameter foldsParameter = new TextFieldParameter(fieldName, criterion.getFoldsDown().toString());
         foldsParameter.setLabel(label);
         ValueHandler foldsChangeHandler = new ValueHandlerAdapter() {
 
             public boolean isValid(String value) {
                 return NumberUtils.isNumber(value);
             }
 
             public void validate(String formFieldName, String value, ValidationAware action) {
                 if (!isValid(value)) {
                     action.addActionError("Numeric value required for " + label);
                 }
             }
 
             public void valueChanged(String value) {
                 criterion.setFoldsDown(Float.valueOf(value));
             }
         };
         foldsParameter.setValueHandler(foldsChangeHandler);
         return foldsParameter;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     AbstractGenomicCriterion getAbstractGenomicCriterion() {
         return criterion;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     String getFieldName() {
         return FOLD_CHANGE;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     CriterionTypeEnum getCriterionType() {
         return CriterionTypeEnum.FOLD_CHANGE;
     }
 
 }
