 package gov.nih.nci.evs.browser.bean;
 
 import gov.nih.nci.evs.browser.properties.AppProperties;
 import gov.nih.nci.evs.browser.utils.*;
 
 import javax.servlet.http.*;
 
 public class NewConceptRequest extends RequestBase {
     // List of session attribute name(s):
     private final String EMAIL = "email";
     private final String OTHER = "other";
     private final String VOCABULARY = "vocabulary";
     private final String TERM = "term";
     private final String SYNONYMS = "synonyms";
     private final String PARENT_CODE = "parentCode";
     private final String DEFINITION = "definition";
     private final String REASON = "reason";
     private final String MESSAGE = "message";
     private final String WARNINGS = "warnings";
     
     // List of return state(s):
     private final String MESSAGE_STATE = "message";
     private final String WARNING_STATE = "warnings";
 
     public NewConceptRequest(HttpServletRequest request) {
         super(request);
         setParameters(new String[] { EMAIL, OTHER, VOCABULARY, TERM, SYNONYMS,
                 PARENT_CODE, DEFINITION, REASON });
     }
 
     protected void setParameters(String[] parameters) {
         super.setParameters(parameters);
 
         // Hack: In content_new_concept.jsp page, the vocabulary comboBox
         // stores the value as the URL instead of the vocabulary name.
         // This is done so that the "Browse" button can display the
         // vocabulary's URL in a separate window kicked off by using
         // javascript displayLinkInNewWindow method. The following
         // lines manually replaces the URL with the vocabulary name.
         // Note: In order for this to work, the URL must be unique.
         String url = _parametersHashMap.get(VOCABULARY);
         String name = AppProperties.getInstance().getVocabularyName(url);
         _parametersHashMap.put(VOCABULARY, name);
     }
     
     public void clear() {
         setParameters(EMPTY_PARAMETERS);
         _request.getSession().setAttribute(WARNINGS, null);
         _request.getSession().setAttribute(MESSAGE, null);
        clearSessionAttributes();
     }
     
     public String clearForm() {
         clear();
         return WARNING_STATE;
     }
 
     public String submitForm() {
         updateSessionAttributes();
         String warnings = validate();
         if (warnings.length() > 0) {
             _request.getSession().setAttribute(WARNINGS, warnings);
             return WARNING_STATE;
         }
 
         AppProperties appProperties = AppProperties.getInstance();
         String mailServer = appProperties.getMailSmtpServer();
         String from = _parametersHashMap.get(EMAIL);
         String[] recipients = appProperties.getContactUsRecipients();
         String subject = getSubject();
         String emailMsg = getEmailMesage();
 
         try {
            if (false) // DYEE
             MailUtils.postMail(mailServer, from, recipients, subject, emailMsg);
         } catch (Exception e) {
             _request.getSession().setAttribute(WARNINGS,
                     e.getLocalizedMessage());
             e.printStackTrace();
             return WARNING_STATE;
         }
 
         clearSessionAttributes(new String[] { /* EMAIL, OTHER, VOCABULARY, */
                 TERM, SYNONYMS, PARENT_CODE, DEFINITION, REASON });
         _request.getSession().setAttribute(WARNINGS, null);
        _request.getSession().setAttribute(MESSAGE, Utils.toHtml(emailMsg));
        return MESSAGE_STATE;
     }
 
     private String validate() {
         StringBuffer buffer = new StringBuffer();
         String email = _parametersHashMap.get(EMAIL);
         if (!MailUtils.isValidEmailAddress(email)) {
             if (buffer.length() > 0)
                 buffer.append("\n");
             buffer.append("* Please enter a valid email address.");
         }
 
         String vocabulary = _parametersHashMap.get(VOCABULARY);
         if (vocabulary == null || vocabulary.length() <= 0) {
             if (buffer.length() > 0)
                 buffer.append("\n");
             buffer.append("* Please select a vocabulary.");
         }
 
         String term = _parametersHashMap.get(TERM);
         if (term == null || term.length() <= 0) {
             if (buffer.length() > 0)
                 buffer.append("\n");
             buffer.append("* Please enter a term.");
         }
         return buffer.toString();
     }
 
     private String getSubject() {
         String term = _parametersHashMap.get(TERM);
         String value = "Request New Concept";
         if (term.length() > 0)
             value += " (" + term + ")";
         return value;
     }
 
     private String getEmailMesage() {
         StringBuffer buffer = new StringBuffer();
         buffer.append(getSubject());
         for (int i = 0; i < _parameters.length; ++i) {
             String parameter = _parameters[i];
             buffer.append("\n* ");
             buffer.append(parameter + ": ");
             buffer.append(_parametersHashMap.get(parameter));
         }
         return buffer.toString();
     }
 }
