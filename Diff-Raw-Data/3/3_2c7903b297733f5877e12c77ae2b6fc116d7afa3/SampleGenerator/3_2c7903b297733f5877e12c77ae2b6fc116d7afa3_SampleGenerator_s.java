 package gov.nih.nci.caintegrator2.domain.genomic;
 
 import static org.junit.Assert.assertEquals;
 import gov.nih.nci.caintegrator2.application.study.AbstractTestDataGenerator;
 
 import java.util.HashSet;
 
 public class SampleGenerator extends AbstractTestDataGenerator<Sample> {
     
     public static final SampleGenerator INSTANCE = new SampleGenerator();
 
     @Override
     public void compareFields(Sample original, Sample retrieved) {
         assertEquals(original.getName(), retrieved.getName());
         assertEquals(original.getArrayDataCollection().size(), retrieved.getArrayDataCollection().size());
         assertEquals(original.getArrayCollection().size(), retrieved.getArrayCollection().size());
     }
 
     @Override
     public Sample createPersistentObject() {
         return new Sample();
     }
 
     @Override
     public void setValues(Sample sample) {
         sample.setName(getUniqueString());
         sample.setArrayCollection(new HashSet<Array>());
         sample.setArrayDataCollection(new HashSet<ArrayData>());
         ArrayData arrayData1 = new ArrayData();
         Array array1 = new Array();
         sample.getArrayCollection().add(array1);
         sample.getArrayDataCollection().add(arrayData1);
     }
 
 
 }
