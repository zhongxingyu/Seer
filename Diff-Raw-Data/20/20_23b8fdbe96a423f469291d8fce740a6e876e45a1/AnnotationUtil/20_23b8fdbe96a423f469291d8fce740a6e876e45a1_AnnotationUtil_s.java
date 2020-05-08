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
 package gov.nih.nci.caintegrator2.common;
 
 import gov.nih.nci.caintegrator2.application.study.AnnotationFieldDescriptor;
 import gov.nih.nci.caintegrator2.application.study.AnnotationFieldType;
 import gov.nih.nci.caintegrator2.application.study.AnnotationGroup;
 import gov.nih.nci.caintegrator2.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.ValidationException;
 import gov.nih.nci.caintegrator2.application.study.ValidationResult;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.DateAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.NumericAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.StringAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.text.ParseException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 
 
 /**
  * This is a static utility class used by the UI to display the annotation value. 
  */
 @SuppressWarnings({ "PMD.CyclomaticComplexity" }) // Checking for type and null.
 public final class AnnotationUtil {
 
     private AnnotationUtil() {
         
     }
     
     /**
      * @param abstractAnnotationValues abstractAnnotationValues
      * @param dataValues the values from the current upload file
      * @param filterList the list of display string to filter
      * @return set of distinct available values
      */
     public static Set<String> getAdditionalValue(Collection<AbstractAnnotationValue> abstractAnnotationValues,
             List<String>dataValues, Set<String> filterList) {
         Set<String> results = new HashSet<String>();
         for (String dataValue : dataValues) {
             if (!StringUtils.isBlank(dataValue) && !filterList.contains(dataValue)) {
                 if (NumberUtils.isNumber(dataValue)) {
                     dataValue = NumericUtil.formatDisplay(dataValue);
                 }
                 results.add(dataValue);
             }
         }
         for (AbstractAnnotationValue abstractAnnotationValue : abstractAnnotationValues) {
             String displayString = abstractAnnotationValue.toString();
             if (!StringUtils.isBlank(displayString) && !filterList.contains(displayString)) {
                 results.add(displayString);
             }
         }
         return results;
     }
     
     /**
      * Creates an annotation value for the given value / field descriptor.
      * @param annotationDescriptor to create value for.
      * @param value string value.
      * @return annotation value.
      * @throws ValidationException for validation errors.
      */
     @SuppressWarnings("PMD.CyclomaticComplexity")   // switch statement and argument checking
     public static AbstractAnnotationValue createAnnotationValue(AnnotationFieldDescriptor annotationDescriptor, 
            String value) throws ValidationException {
        if (annotationDescriptor.getDefinition() == null 
                || annotationDescriptor.getDefinition().getDataType() == null) {
            throwValidationException("Type for field " + annotationDescriptor.getName() + " was not set.");
        }
        AnnotationTypeEnum type = annotationDescriptor.getDefinition().getDataType();
        switch (type) {
        case DATE:
            return createDateAnnotationValue(annotationDescriptor, value);
        case STRING:
            return createStringAnnotationValue(annotationDescriptor, value);
        case NUMERIC:
            return createNumericAnnotationValue(annotationDescriptor, value);
        default:
            throwValidationException("Unknown AnnotationDefinitionType: " + type);
            return null;
        }
    }
     
     private static void throwValidationException(String message) throws ValidationException {
         ValidationResult result = new ValidationResult();
         result.setInvalidMessage(message);
         throw new ValidationException(result);
     }
 
    private static StringAnnotationValue createStringAnnotationValue(AnnotationFieldDescriptor annotationDescriptor, 
            String value) {
        StringAnnotationValue annotationValue = new StringAnnotationValue();
        annotationValue.setStringValue(value);
        annotationValue.setAnnotationDefinition(annotationDescriptor.getDefinition());
        annotationDescriptor.getDefinition().getAnnotationValueCollection().add(annotationValue);
        return annotationValue;
    }
 
    private static DateAnnotationValue createDateAnnotationValue(AnnotationFieldDescriptor annotationDescriptor, 
            String value) throws ValidationException {
        DateAnnotationValue annotationValue = new DateAnnotationValue();
        try {
            annotationValue.setDateValue(DateUtil.createDate(value));
        } catch (ParseException e) {
            throwValidationException(createFormatErrorMsg(annotationDescriptor, value, 
                    "The two formats allowed are MM-dd-yyyy and MM/dd/yyyy"));
        }
        annotationValue.setAnnotationDefinition(annotationDescriptor.getDefinition());
        annotationDescriptor.getDefinition().getAnnotationValueCollection().add(annotationValue);
        return annotationValue;
    }
 
    private static NumericAnnotationValue createNumericAnnotationValue(AnnotationFieldDescriptor annotationDescriptor, 
            String value) throws ValidationException {
        NumericAnnotationValue annotationValue = new NumericAnnotationValue();
        try {
            annotationValue.setNumericValue(getNumericValue(value));
        } catch (NumberFormatException e) {
            throwValidationException(createFormatErrorMsg(annotationDescriptor, value, null));
        }
        annotationValue.setAnnotationDefinition(annotationDescriptor.getDefinition());
        annotationDescriptor.getDefinition().getAnnotationValueCollection().add(annotationValue);
        return annotationValue;
    }
    
    private static Double getNumericValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        } else {
            return Double.parseDouble(value);
        }
    }
 
    private static String createFormatErrorMsg(
            AnnotationFieldDescriptor descriptor, String value, String allowedFormats) {
        return "Invalid format for data type '" + descriptor.getDefinition().getDataType()
            + "' on field '" + descriptor.getName()
            + "' of descriptor '" + descriptor.getDefinition().getDisplayName()
            + "' with value = '" + value + "'. " + allowedFormats;
    }
     
     /**
      * Retrieves existing field descriptor, and if that doesn't exist creates a new one.
      * @param dao database access object.
      * @param studyConfiguration study.
      * @param type entity type.
      * @param createNewAnnotationDefinition determines whether to create an annotation definition.
      * @param annotationFieldDescriptorName name of the afd.
      * @param annotationGroupName name of the group (optional, if null or blank will use default study group).
      * @return annotation field descriptor.
      * @throws ValidationException if invalid afd.
      */
     @SuppressWarnings("PMD.ExcessiveParameterList") // necessary parameters.
     public static AnnotationFieldDescriptor retrieveOrCreateFieldDescriptor(CaIntegrator2Dao dao,
             StudyConfiguration studyConfiguration, EntityTypeEnum type, boolean createNewAnnotationDefinition,
             String annotationFieldDescriptorName, String annotationGroupName) 
         throws ValidationException {
         AnnotationFieldDescriptor fieldDescriptor = null;
         if (studyConfiguration != null) {
             fieldDescriptor = studyConfiguration.getExistingFieldDescriptorInStudy(annotationFieldDescriptorName);
             validateFieldDescriptorEntityType(fieldDescriptor, type);
         }
         if (fieldDescriptor == null) {
             fieldDescriptor = createNewAnnotationFieldDescriptor(dao, studyConfiguration, type, 
                     createNewAnnotationDefinition, 
                     annotationFieldDescriptorName, annotationGroupName);
        } else if (createNewAnnotationDefinition && fieldDescriptor.getDefinition() == null) {
             createNewAnnotationDefinition(dao, fieldDescriptor);
         }
         return fieldDescriptor;
     }
 
     @SuppressWarnings("PMD.ExcessiveParameterList") // necessary parameters.
     private static AnnotationFieldDescriptor createNewAnnotationFieldDescriptor(CaIntegrator2Dao dao, 
             StudyConfiguration studyConfiguration,
             EntityTypeEnum type, boolean createNewAnnotationDefinition,
             String annotationFieldDescriptorName,
             String annotationGroupName) {
         AnnotationFieldDescriptor fieldDescriptor = new AnnotationFieldDescriptor();
         fieldDescriptor.setName(annotationFieldDescriptorName);
         fieldDescriptor.setType(AnnotationFieldType.ANNOTATION);
         fieldDescriptor.setAnnotationEntityType(type);
         if (createNewAnnotationDefinition) {
             createNewAnnotationDefinition(dao, fieldDescriptor);
         }
         AnnotationGroup group = studyConfiguration.getStudy().getOrCreateAnnotationGroup(
                 StringUtils.isBlank(annotationGroupName) ? Study.DEFAULT_ANNOTATION_GROUP
                         : annotationGroupName);
         fieldDescriptor.setAnnotationGroup(group);
         group.getAnnotationFieldDescriptors().add(fieldDescriptor);
         return fieldDescriptor;
     }
 
     private static void createNewAnnotationDefinition(CaIntegrator2Dao dao, 
             AnnotationFieldDescriptor fieldDescriptor) {
         AnnotationDefinition annotationDefinition = dao.getAnnotationDefinition(fieldDescriptor.getName(),
                 AnnotationTypeEnum.STRING);
         if (annotationDefinition == null) {
             annotationDefinition = new AnnotationDefinition();
             annotationDefinition.setDefault(fieldDescriptor.getName());
         }
         fieldDescriptor.setDefinition(annotationDefinition);
     }
 
 
     private static void validateFieldDescriptorEntityType(AnnotationFieldDescriptor fieldDescriptor, 
             EntityTypeEnum entityType) throws ValidationException {
         if (fieldDescriptor != null && !entityType.equals(fieldDescriptor.getAnnotationEntityType())) {
             throw new ValidationException(
                     "Found a currently existing field descriptor with the same name '"
                         + fieldDescriptor.getName() + "' in this study of type '"
                         + fieldDescriptor.getAnnotationEntityType() + "' which doesn't match type "
                         + entityType);
         }
     }
     
 }
