 package org.motechproject.care.reporting.processors;
 
 import org.motechproject.care.reporting.enums.CaseType;
 import org.motechproject.care.reporting.enums.FormSegment;
 import org.motechproject.care.reporting.factory.FormFactory;
 import org.motechproject.care.reporting.mapper.GenericMapper;
 import org.motechproject.care.reporting.parser.ChildInfoParser;
 import org.motechproject.care.reporting.parser.InfoParser;
 import org.motechproject.care.reporting.parser.MetaInfoParser;
 import org.motechproject.care.reporting.parser.MotherInfoParser;
 import org.motechproject.care.reporting.service.MapperService;
 import org.motechproject.care.reporting.service.Service;
 import org.motechproject.commcare.domain.CommcareForm;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class GenericFormProcessorWorker extends ProcessorWorker {
     private static final Logger logger = LoggerFactory.getLogger("commcare-reporting-mapper");
 
     Class<?> motherForm;
     Class<?> childForm;
     private CommcareForm commcareForm;
     private Map<String, String> metadata;
     String namespace;
     String version;
 
     public GenericFormProcessorWorker(Service service, MapperService mapperService) {
         super(service, mapperService);
     }
 
     public void process(CommcareForm commcareForm) {
         initialize(commcareForm);
         Serializable serializable = parseMotherForm();
         saveForm(serializable, motherForm);
 
         List<Serializable> serializables = parseChildForms();
         saveForm(serializables, childForm);
     }
 
     void initialize(CommcareForm commcareForm) {
         this.commcareForm = commcareForm;
         namespace = namespace(commcareForm);
         version = version(commcareForm);
         InfoParser infoParser = mapperService.getFormInfoParser(namespace, version, FormSegment.METADATA);
         metadata = new MetaInfoParser(infoParser).parse(commcareForm);
 
         motherForm = FormFactory.getForm(namespace, CaseType.MOTHER);
         childForm = FormFactory.getForm(namespace, CaseType.CHILD);
     }
 
     Serializable parseMotherForm() {
         Map<String, String> motherInfo = new HashMap<>(metadata);
         InfoParser infoParser = mapperService.getFormInfoParser(namespace, version, FormSegment.MOTHER);
         motherInfo.putAll(new MotherInfoParser(infoParser).parse(commcareForm));
 
         Object formObject = new GenericMapper().map(motherInfo, motherForm);
 
         setMotherCase(motherInfo.get("caseId"), formObject);
         setFlw(motherInfo.get("userID"), formObject);
 
         return (Serializable) formObject;
     }
 
     List<Serializable> parseChildForms() {
         if (null == childForm)
             return new ArrayList<>();
 
         List<Serializable> childForms = new ArrayList<>();
         InfoParser infoParser = mapperService.getFormInfoParser(namespace, version, FormSegment.CHILD);
         List<Map<String, String>> childDetails = new ChildInfoParser(infoParser).parse(commcareForm);
 
         for (Map<String, String> childDetail : childDetails) {
 
             Map<String, String> childInfo = new HashMap<>(metadata);
             childInfo.putAll(childDetail);
 
             Serializable formObject = (Serializable) new GenericMapper().map(childInfo, childForm);
             setChildCase(childInfo.get("caseId"), formObject);
             setFlw(childInfo.get("userID"), formObject);
             childForms.add(formObject);
         }
         return childForms;
     }
 
     void saveForm(Serializable form, Class<?> type) {
         logger.info(String.format("Started processing form %s", form));
 
         try {
             service.save(type.cast(form));
         } catch (DataAccessException e) {
            logger.error(String.format("Cannot save Form: %s. %s", type.cast(form), e.getRootCause().getMessage()));
         }
 
         logger.info(String.format("Finished processing form %s", form));
     }
 
     void saveForm(List<Serializable> forms, Class<?> type) {
         for (Serializable form : forms) {
             saveForm(form, type);
         }
     }
 
     private String namespace(CommcareForm commcareForm) {
         return attribute(commcareForm, "xmlns");
     }
 
     private String version(CommcareForm commcareForm) {
         return commcareForm.getVersion();
     }
 
     private String attribute(CommcareForm commcareForm, String name) {
         return commcareForm.getForm().getAttributes().get(name);
     }
 }
 
 
