 package com.selfdiagnosis.core.entity;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 
 import org.junit.Test;
 
 import com.selfdiagnosis.SelfDiagnosisConstants;
 import com.selfdiagnosis.core.service.AdminService;
 
 /**
  * Test class to test {@link DiseaseEntity}. Including validators and save
  * entity.
  * 
  * @author mmieszkowski
  * 
  */
 public class DiseaseEntityTest extends EntityTest {
 
     /**
      * Checks disease with blank name.
      */
     @Test
     public void nameIsBlank() {
         DiseaseEntity disease = createValidEntity();
         disease.setName("");
         Set<ConstraintViolation<DiseaseEntity>> constraintViolations = getValidator().validate(disease);
         assertEquals(1, constraintViolations.size());
        assertEquals("{NotBlank.name}", constraintViolations.iterator().next().getMessage());
     }
 
     /**
      * Checks disease with too long name.
      */
     @Test
     public void nameIsTooLong() {
         DiseaseEntity disease = createValidEntity();
         disease.setName("Very long disease name that is longer than one hundred characters. "
                 + "Very long disease name that is longer than one hundred characters.");
         Set<ConstraintViolation<DiseaseEntity>> constraintViolations = getValidator().validate(disease);
         assertEquals(1, constraintViolations.size());
         assertEquals("length must be between 0 and 100", constraintViolations.iterator().next().getMessage());
     }
 
     /**
      * Checks disease with no frequency.
      */
     @Test
     public void frequencyisNull() {
         DiseaseEntity disease = createValidEntity();
         disease.setFrequency(null);
         Set<ConstraintViolation<DiseaseEntity>> constraintViolations = getValidator().validate(disease);
         assertEquals(1, constraintViolations.size());
        assertEquals("{NotBlank.frequency}", constraintViolations.iterator().next().getMessage());
     }
 
     /**
      * Checks disease with frequency out of range.
      */
     @Test
     public void frequencyOutOfRange() {
         DiseaseEntity disease = createValidEntity();
         disease.setFrequency(new Short("-1"));
         Set<ConstraintViolation<DiseaseEntity>> constraintViolations = getValidator().validate(disease);
         assertEquals(1, constraintViolations.size());
         assertEquals("must be between 0 and 100", constraintViolations.iterator().next().getMessage());
 
         disease.setFrequency(new Short("101"));
         constraintViolations = getValidator().validate(disease);
         assertEquals(1, constraintViolations.size());
         assertEquals("must be between 0 and 100", constraintViolations.iterator().next().getMessage());
     }
 
     @Override
     protected DiseaseEntity createValidEntity() {
         return createValidDiseaseEntity();
     }
 
     /**
      * Creates valid disease entity. Can be used in this test or tests of
      * related entities.
      * 
      * @return valid {@link DiseaseEntity}
      */
     public static DiseaseEntity createValidDiseaseEntity() {
         DiseaseEntity disease = new DiseaseEntity();
         disease.setName("Flu");
         disease.setFrequency(SelfDiagnosisConstants.ENTITY_FREQUENCY_MAX);
         return disease;
     }
 
     @Override
     protected DiseaseEntity saveEntity(SelfDiagnosisEntity entity, AdminService adminService) {
         return saveDiseaseEntity(entity, adminService);
     }
 
     /**
      * Saves disease.
      * 
      * @param entity
      *            to save
      * @param adminService
      *            injected service
      * @return saved entity
      */
     public static DiseaseEntity saveDiseaseEntity(SelfDiagnosisEntity entity, AdminService adminService) {
         return (DiseaseEntity) adminService.saveEntity(entity);
     }
 
 }
