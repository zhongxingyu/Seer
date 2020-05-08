 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.DynamicAttributes;
 import javax.servlet.jsp.tagext.TagSupport;
 
 @SuppressWarnings("serial")
 public class FormTag extends TagSupport implements DynamicAttributes {
 
     private static final String PARAMETER_PREFIX = "_f.";
     public static final String ID_PARAMETER = PARAMETER_PREFIX + "id";
 
     private String method;
     private Class<? extends FormProcessor> processorClass;
     private String varProcessor;
     private String varSuccess;
     private String varError;
     private final Map<String, String> attributes = new LinkedHashMap<String, String>();
 
     /**
      * Sets the form submission method.
      *
      * @param method Can't be {@code null}.
      */
     public void setMethod(String method) {
         ErrorUtils.errorIfNull(method, "method");
 
         this.method = method;
     }
 
     /**
      * Sets the name of the processor class.
      *
      * @param processor The class name must be valid, and the class must
      * implement {@link FormProcessor}.
      */
     @SuppressWarnings("unchecked")
     public void setProcessor(String processor) {
         Class<?> pc = ObjectUtils.getClassByName(processor);
 
         ErrorUtils.errorIf(pc == null, processor, "isn't a valid class name!");
         ErrorUtils.errorIf(!FormProcessor.class.isAssignableFrom(pc), pc.getName(), "doesn't implement [" + FormProcessor.class.getName() + "]!");
 
         this.processorClass = (Class<? extends FormProcessor>) pc;
     }
 
     /**
      * Sets the name of the page-scoped variable to store the processor
      * instance.
     *
     * @return May be {@code null}.
      */
     public void setVarProcessor(String varProcessor) {
         this.varProcessor = varProcessor;
     }
 
     /**
      * Sets the name of the page-scoped variable to store the form processing
      * success flag.
     *
     * @return May be {@code null}.
      */
     public void setVarSuccess(String varSuccess) {
         this.varSuccess = varSuccess;
     }
 
     /**
      * Sets the name of the page-scoped variable to store the form processing
      * error.
     *
     * @return May be {@code null}.
      */
     public void setVarError(String varError) {
         this.varError = varError;
     }
 
     // --- TagSupport support ---
 
     @Override
     public int doStartTag() throws JspException {
         HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
         String id = getId();
         FormProcessor processor = TypeDefinition.getInstance(processorClass).newInstance();
 
         // Auto-generate the ID based on the processor class name if it's
         // not explicitly defined.
         if (ObjectUtils.isBlank(id)) {
             id = "p" + StringUtils.hex(StringUtils.md5(processorClass.getName()));
         }
 
         if (!ObjectUtils.isBlank(varProcessor)) {
             pageContext.setAttribute(varProcessor, processor);
         }
 
         // Process if it's either a GET request or if the form's been POST'd.
         if (id.equals(request.getParameter(ID_PARAMETER)) &&
                 ("get".equalsIgnoreCase(method) ||
                 JspUtils.isFormPost(request))) {
             try {
                 processor.processRequest(request);
 
                 if (!ObjectUtils.isBlank(varSuccess)) {
                     pageContext.setAttribute(varSuccess, Boolean.TRUE);
                 }
 
             } catch (Exception error) {
                 if (!ObjectUtils.isBlank(varError)) {
                     pageContext.setAttribute(varError, error);
                 }
             }
         }
 
         // Write the FORM start tag.
         try {
             @SuppressWarnings("all")
             HtmlWriter writer = new HtmlWriter(pageContext.getOut());
 
             writer.writeTag("form",
                     "id", id,
                     "method", method.toLowerCase(),
                     "action", "",
                     attributes);
                 writer.writeTag("input",
                         "type", "hidden",
                         "name", ID_PARAMETER,
                         "value", id);
 
         } catch (IOException error) {
             throw new JspException(error);
         }
 
         return EVAL_BODY_INCLUDE;
     }
 
     @Override
     public int doEndTag() throws JspException {
 
         // Write the FORM end tag.
         try {
             @SuppressWarnings("all")
             HtmlWriter writer = new HtmlWriter(pageContext.getOut());
 
             writer.writeTag("/form");
 
         } catch (IOException error) {
             throw new JspException(error);
         }
 
         return EVAL_PAGE;
     }
 
     // --- DynamicAttribute support ---
 
     @Override
     public void setDynamicAttribute(String uri, String localName, Object value) {
         attributes.put(localName, value != null ? value.toString() : null);
     }
 }
