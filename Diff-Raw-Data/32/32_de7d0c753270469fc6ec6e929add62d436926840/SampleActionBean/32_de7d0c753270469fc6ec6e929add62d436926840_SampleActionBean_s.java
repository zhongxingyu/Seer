 package bbmri.action.Sample;
 
 import bbmri.action.BasicActionBean;
 import bbmri.entities.Biobank;
 import bbmri.entities.Sample;
 import bbmri.service.SampleService;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.IntegerTypeConverter;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import org.apache.commons.lang.RandomStringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.security.PermitAll;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ori
  * Date: 6.4.13
  * Time: 19:50
  * To change this template use File | Settings | File Templates.
  */
 @PermitAll
 @UrlBinding("/Sample")
 public class SampleActionBean extends BasicActionBean {
 
     private static final String WITHDRAW = "/sample_withdraw.jsp";
     private static final String ALL = "/sample_all.jsp";
    private static final String EDIT = "/sample_edit.jsp";
 
     Logger logger = LoggerFactory.getLogger(this.getClass().getName());
 
     @ValidateNestedProperties(value = {
             @Validate(on = {"find"},
                     field = "TNM",
                     maxlength = 7),
             @Validate(on = {"find"},
                     field = "pTNM",
                     maxlength = 7),
             @Validate(on = {"find"},
                     field = "grading",
                     minvalue = 1, maxvalue = 8),
             @Validate(on = {"find"}, field = "tissueType",
                     maxlength = 2),
             @Validate(on = {"find"}, field = "diagnosis",
                     maxlength = 4)
     })
     private Sample sample;
 
     private List<Long> selectedSamples;
 
     public List<Long> getSelectedSamples() {
         return selectedSamples;
     }
 
     public List<Sample> getSamples(){
         Biobank biobank = getLoggedUser().getBiobank();
         if(biobank != null){
             return sampleService.getAllByBiobank(biobank.getId());
         }
         return null;
     }
 
     public Integer getSamplesSize(){
         return getSamples().size();
     }
 
     public void setSelectedSamples(List<Long> selectedSamples) {
         this.selectedSamples = selectedSamples;
     }
 
     private List<Sample> results;
 
     public Sample getSample() {
        if(sample == null){
            sample = getContext().getSample();
        }
         return sample;
     }
 
     public void setSample(Sample sample) {
         this.sample = sample;
     }
 
     public Integer getCount() {
         return sampleService.getCount();
     }
 
     public List<Sample> getResults() {
         if(results == null){
             Sample sampleQuery = getSample();
             if(sampleQuery != null){
                 results = sampleService.getSamplesByQueryAndBiobank(sampleQuery, getLoggedUser().getBiobank());
             }
         }
         return results;
     }
 
     public Integer getResultCount() {
         if (getResults() == null) {
             return 0;
         }
         return results.size();
     }
 
     @DontValidate
     @DefaultHandler
     public Resolution display() {
         logger.debug("DISPLAY");
         return new ForwardResolution(WITHDRAW);
     }
 
     @DontValidate
     @HandlesEvent("allSamples")
     public Resolution allSamples(){
         return new ForwardResolution(ALL);
     }
 
     @DontValidate
     public Resolution withdrawSamples() {
         getContext().setSample(null);
         return new RedirectResolution(WITHDRAW);
     }
 
     private int[] releasedCount;
 
     public int[] getReleasedCount() {
         return releasedCount;
     }
 
     public void setReleasedCount(int[] releasedCount) {
         this.releasedCount = releasedCount;
     }
 
     @DontValidate
     public Resolution find() {
         results = sampleService.getSamplesByQueryAndBiobank(sample, getLoggedUser().getBiobank());
         getContext().setSample(sample);
         return new ForwardResolution(WITHDRAW);
     }
 
     @DontValidate
    @HandlesEvent("save")
    public Resolution save(){
        sampleService.update(sample);
        return new ForwardResolution(ALL);
    }

    @DontValidate
    @HandlesEvent("cancel")
    public Resolution cancel(){
        return new ForwardResolution(ALL);
    }

    @DontValidate
     @HandlesEvent("edit")
     public Resolution edit() {
         sample = sampleService.getById(sample.getId());
         getContext().setSample(sample);
        return new ForwardResolution(EDIT);
     }
 }
