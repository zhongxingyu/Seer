 package edu.ualberta.med.biobank.barcodegenerator.template;
 
 import edu.ualberta.med.biobank.SessionManager;
 import edu.ualberta.med.biobank.common.wrappers.PrinterLabelTemplateWrapper;
 import gov.nih.nci.system.applicationservice.ApplicationException;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * For add/editing/listing/removing templates. Contains several helper functions
  * to access template information.
  * 
  * @author Thomas Polasek 2011
  * 
  */
 public class TemplateStore {
 
    private static final long serialVersionUID = 5502373669875110097L;

     private Map<String, Template> templates;
 
     public TemplateStore() throws ApplicationException {
         templates = new HashMap<String, Template>();
         for (String name : PrinterLabelTemplateWrapper
             .getTemplateNames(SessionManager.getAppService())) {
             templates.put(name, Template.getTemplateByName(name));
         }
     }
 
     public Set<String> getTemplateNames() {
         return templates.keySet();
     }
 
     public Template getTemplate(String name) throws Exception {
         Template t = templates.get(name);
         if (t == null) {
             throw new Exception("template with name " + name + " not found");
         }
         return t;
     }
 
     public void addTemplate(Template updatedTemplate) throws Exception {
         String name = updatedTemplate.getName();
         Template oldTemplate = templates.get(name);
         if (oldTemplate != null) {
             templates.remove(oldTemplate);
         }
         templates.put(name, updatedTemplate);
     }
 
     public void deleteTemplate(Template template) throws Exception {
         String name = template.getName();
         if (!templates.containsKey(name)) {
             throw new Exception("template with name " + name + " not found");
         }
         templates.remove(template);
     }
 
     public void deleteTemplate(String name) throws Exception {
         Template t = templates.get(name);
         if (t == null) {
             throw new Exception("template with name " + name + " not found");
         }
         deleteTemplate(t);
     }
 
 }
