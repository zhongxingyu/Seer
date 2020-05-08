 package edu.northwestern.bioinformatics.studycalendar.web.admin;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.validation.Errors;
 import org.restlet.util.Template;
 import edu.nwu.bioinformatics.commons.spring.Validatable;
 import edu.northwestern.bioinformatics.studycalendar.web.GeneratedUriTemplateVariable;
 import static edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration.*;
 
 import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
 
 /**
  * @author Rhett Sutphin
  */
 public class ConfigurationCommand implements Validatable {
     private BindableConfiguration conf;
     private Configuration originalConfiguration;
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     public ConfigurationCommand(Configuration configuration) {
         conf = new BindableConfiguration(configuration);
         originalConfiguration = configuration;
     }
 
     public BindableConfiguration getConf() {
         return conf;
     }
 
     public void validate(Errors errors) {
         Template patientPageUrl = originalConfiguration.get(PATIENT_PAGE_URL);
        if (patientPageUrl !=null) {
            for (String varName : patientPageUrl.getVariableNames()) {
                boolean matched = false;
                for (GeneratedUriTemplateVariable knownVar : GeneratedUriTemplateVariable.values()) {
                    if (knownVar.attributeName().equals(varName)) {
                        matched = true; break;
                    }
                }
                if (!matched) {
                    errors.rejectValue(
                            "conf[" + PATIENT_PAGE_URL.getKey() + "].value",
                            "error.configuration.patientPageUrl.invalidVariable", new Object[] { varName }, "Invalid variable");
                 }
             }
         }
     }
 }
