 package bbmri.action;
 
 import bbmri.entities.Biobank;
 import bbmri.entities.Sample;
 import bbmri.entities.User;
 import bbmri.service.SampleService;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.IntegerTypeConverter;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import org.apache.commons.lang.RandomStringUtils;
 
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ori
  * Date: 7.11.12
  * Time: 0:34
  * To change this template use File | Settings | File Templates.
  */
 public class AddSampleActionBean extends BasicActionBean {
 
     @ValidateNestedProperties(value = {
            @Validate(on = {"create"},
                    field = "sampleID", required = true,
                     minlength = 13, maxlength = 13),
             @Validate(on = {"create"},
                     field = "TNM", required = true,
                     minlength = 7, maxlength = 7),
             @Validate(on = {"create"},
                     field = "pTNM", required = true,
                     minlength = 7, maxlength = 7),
             @Validate(on = {"create"},
                     field = "grading", required = true,
                     minvalue = 1, maxvalue = 8),
             @Validate(on = {"create"}, field = "tissueType", required = true,
                     minlength = 2, maxlength = 2),
             @Validate(on = {"create"}, field = "numOfSamples", required = true,
                     minvalue = 1),
             @Validate(on = {"create"}, field = "numOfAvailable", required = true,
                     minvalue = 1),
             @Validate(on = {"create"}, field = "diagnosis", required = true,
                     minlength = 4, maxlength = 4)
     })
     private Sample sample;
 
     @SpringBean
     private SampleService sampleService;
 
 
     @Validate(converter = IntegerTypeConverter.class, on = {"generateRandomSample"},
             required = true, minvalue = 1, maxvalue = 100)
     private Integer numOfRandom;
 
     public Integer getCount(){
         return sampleService.getCount();
     }
 
     public Integer getNumOfRandom() {
         return numOfRandom;
     }
 
     public void setNumOfRandom(Integer numOfRandom) {
         this.numOfRandom = numOfRandom;
     }
 
     public void setSample(Sample sample) {
         this.sample = sample;
     }
 
     public Sample getSample() {
         return sample;
     }
 
     @DefaultHandler
     public Resolution zobraz() {
         return new ForwardResolution("/sample_create.jsp");
     }
 
     public Resolution create() {
 
         Biobank biobank = getLoggedUser().getBiobank();
         if (biobank != null) {
             sampleService.create(sample, biobank.getId());
         }
         return new RedirectResolution("/sample_create.jsp");
     }
 
     public Resolution generateRandomSample() {
         Biobank biobank = getLoggedUser().getBiobank();
         if (biobank != null) {
             for (int i = 0; i < numOfRandom; i++) {
                 generateSample();
                 sampleService.create(sample, biobank.getId());
             }
         }
         return new RedirectResolution("/sample_create.jsp");
     }
 
     public void generateSample() {
         RandomStringUtils randomStringUtils = new RandomStringUtils();
         Random generator = new Random();
         sample = new Sample();
         sample.setDiagnosis(randomStringUtils.random(4, true, true));
         sample.setGrading(generator.nextInt(8) + 1);
         sample.setNumOfAvailable(generator.nextInt(20) + 1);
         sample.setNumOfSamples(sample.getNumOfAvailable() + generator.nextInt(10));
         sample.setSampleID(randomStringUtils.random(13, true, true));
         sample.setTNM(randomStringUtils.random(7, true, true));
         sample.setpTNM(randomStringUtils.random(7, true, true));
         sample.setTissueType(randomStringUtils.random(2, true, true));
     }
 
 
 }
 
 
