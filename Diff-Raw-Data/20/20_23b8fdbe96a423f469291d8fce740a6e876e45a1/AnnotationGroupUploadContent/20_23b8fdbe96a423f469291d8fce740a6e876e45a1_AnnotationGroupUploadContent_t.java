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
 package gov.nih.nci.caintegrator2.application.study;
 
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Object that hold the upload file context.
  */
 public class AnnotationGroupUploadContent {
     private String columnName = null;
     private AnnotationFieldType annotationType; 
     private Long cdeId = null;
     private Float version = null;
     private String definitionName;
     private AnnotationTypeEnum dataType = null;
     private EntityTypeEnum entityType = null;
     private boolean permissible = false;
     private boolean visible = false;
     
     /**
      * @return the columnName
      */
     public String getColumnName() {
         return columnName;
     }
     /**
      * @param columnName the columnName to set
      */
     public void setColumnName(String columnName) {
         this.columnName = columnName;
     }
     /**
      * @return the annotationType
      */
     public AnnotationFieldType getAnnotationType() {
         return annotationType;
     }
     /**
      * @param annotationType the annotationType to set
      */
     public void setAnnotationType(String annotationType) {
         this.annotationType = AnnotationFieldType.getByValue(annotationType);
     }
     /**
      * @return the cdeId
      */
     public Long getCdeId() {
         return cdeId;
     }
     /**
      * @param cdeId the cdeId to set
      */
     public void setCdeId(String cdeId) {
         if (!StringUtils.isBlank(cdeId)) {
             this.cdeId = Long.valueOf(cdeId);
         }
     }
     /**
      * @return the version
      */
     public Float getVersion() {
         return version;
     }
     /**
      * @param version the version to set
      */
     public void setVersion(String version) {
         if (!StringUtils.isBlank(version)) {
             this.version = Float.valueOf(version);
         }
     }
     /**
      * @return the definitionName
      */
     public String getDefinitionName() {
         return definitionName;
     }
     /**
      * @param definitionName the definitionName to set
      */
     public void setDefinitionName(String definitionName) {
         this.definitionName = definitionName;
     }
     /**
      * @return the dataType
      */
     public AnnotationTypeEnum getDataType() {
         return dataType;
     }
     /**
      * @param dataType the dataType to set
      */
     public void setDataType(String dataType) {
         this.dataType = AnnotationTypeEnum.getByValue(dataType);
     }
     /**
      * @return the entityType
      */
     public EntityTypeEnum getEntityType() {
         return entityType;
     }
     /**
      * @param entityType the entityType to set
      */
     public void setEntityType(String entityType) {
         this.entityType = EntityTypeEnum.getByValue(entityType);
     }
     /**
      * @return the permissible
      */
     public boolean isPermissible() {
         return permissible;
     }
     /**
      * @param permissible the permissible to set
      */
     public void setPermissible(String permissible) {
         this.permissible = "Yes".equalsIgnoreCase(permissible);
     }
     /**
      * @return the visible
      */
     public boolean isVisible() {
         return visible;
     }
     /**
      * @param visible the visible to set
      */
     public void setVisible(String visible) {
         this.visible = "Yes".equalsIgnoreCase(visible);
     }
 
     /**
      * Create a new annotation field descriptor.
      * @return the annotationFieldDescriptor
      */
     public AnnotationFieldDescriptor createAnnotationFieldDescriptor() {
         AnnotationFieldDescriptor annotationFieldDescriptor = new AnnotationFieldDescriptor();
         annotationFieldDescriptor.setName(getColumnName());
         annotationFieldDescriptor.setAnnotationEntityType(getEntityType());
         annotationFieldDescriptor.setType(getAnnotationType());
        if (AnnotationFieldType.ANNOTATION.equals(annotationFieldDescriptor.getType())) {
            annotationFieldDescriptor.setUsePermissibleValues(isPermissible());    
        }
         annotationFieldDescriptor.setShownInBrowse(isVisible());
         return annotationFieldDescriptor;
     }
 
     /**
      * Locate or create a new annotation definition.
      * @return an annotation definition
      * @throws ValidationException when cdeId is not null
      */
     public AnnotationDefinition createAnnotationDefinition() throws ValidationException {
         if (cdeId != null) {
             throw new ValidationException("Don't know how to create CaDSR annotation definition.");
         }
         AnnotationDefinition annotationDefinition = new AnnotationDefinition();
         annotationDefinition.setKeywords(getDefinitionName());
         annotationDefinition.getCommonDataElement().setLongName(getDefinitionName());
         annotationDefinition.getCommonDataElement().getValueDomain().setDataType(getDataType());
         return annotationDefinition;
     }
 
     /**
      * Validate that the input annotation definition is matching with this content.
      * @param definition to validate
      * @return boolean for matching
      */
     public boolean matching(AnnotationDefinition definition) {
         if (cdeId != null) {
             return matchingCdeId(definition);
         }
         return definition.getKeywords().equals(getDefinitionName())
             && definition.getDataType().equals(getDataType())
             && definition.getCommonDataElement().getLongName().equalsIgnoreCase(getDefinitionName());
     }
 
     private boolean matchingCdeId(AnnotationDefinition definition) {
         return (cdeId.equals(definition.getCommonDataElement().getPublicID())
                     && (version == null
                             || version.equals(Float.valueOf(definition.getCommonDataElement().getVersion()))));
     }
 }
