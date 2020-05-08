 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.DynamicAttributes;
 import javax.servlet.jsp.tagext.TagSupport;
 
 @SuppressWarnings("serial")
 public class FormTag extends TagSupport implements DynamicAttributes {
 
     private static final String PARAMETER_PREFIX = "_f.";
     public static final String ID_PARAMETER = PARAMETER_PREFIX + "id";
 
     private static final String ATTRIBUTE_PREFIX = FormTag.class.getName() + ".";
     private static final String PROCESSOR_ATTRIBUTE_PREFIX = ATTRIBUTE_PREFIX + "processor/";
 
     /** Map of the MD5 hash of a processor class name to the Class object itself. */
     private static final ConcurrentHashMap<String, Class<? extends FormProcessor2>> PROCESSOR_CLASSES =
             new ConcurrentHashMap<String, Class<? extends FormProcessor2>>();
 
     private String method;
     private Class<? extends FormProcessor2> processorClass;
 
     private String varProcessor;
     private String varSuccess;
     private String varError;
     private String varResult;
 
     private transient Boolean success;
     private transient Object result;
     private transient Object error;
 
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
      * implement {@link FormProcessor2}.
      */
     @SuppressWarnings("unchecked")
     public void setProcessor(String processor) {
         Class<?> pc = ObjectUtils.getClassByName(processor);
 
         ErrorUtils.errorIf(pc == null, processor, "isn't a valid class name!");
         ErrorUtils.errorIf(!FormProcessor2.class.isAssignableFrom(pc), pc.getName(), "doesn't implement [" + FormProcessor2.class.getName() + "]!");
 
         this.processorClass = (Class<? extends FormProcessor2>) pc;
     }
 
     public Class<? extends FormProcessor2> getProcessorClass() {
         return this.processorClass;
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
 
     /**
      * Sets the name of the page-scoped variable to store the form processing
      * result.
      *
      * @return May be {@code null}.
      */
     public void setVarResult(String varResult) {
         this.varResult = varResult;
     }
 
     /** Returns a unique identifier for this form. */
     public String getFormId() {
         return Static.getProcessorId(processorClass) + (getId() != null ? ("/" + getId()) : "");
     }
 
     /** Returns the processor for this form. */
    public FormProcessor2 getProcessorInstance() {
         return Static.getProcessorById(getFormId(), (HttpServletRequest) pageContext.getRequest());
     }
 
     // --- Getters for the transient variables ---
 
     /**
      * Return true if this form was processed successfully, or false if not.
      * Null is returned if this method is called outside the context of this
      * form's start and end tags.
      *
      * @return
      */
     public Boolean isSuccess() {
         return success;
     }
 
     /**
      * Returns the result of processing this form, or null if there was an error
      * or this method is called outside the context of this form's start and
      * end tags.
      *
      * @return
      */
     public Object getResult() {
         return result;
     }
 
     /**
      * Returns the error generated attempting to process this form, or null if
      * if the form was processed successfully or this method is called outside
      * the context of this form's start and end tags.
      *
      * @return
      */
     public Object getError() {
         return error;
     }
 
     // --- TagSupport support ---
 
     @Override
     public int doStartTag() throws JspException {
 
         HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
 
         String formId = getFormId();
        FormProcessor2 processor = getProcessorInstance();
 
         if (!ObjectUtils.isBlank(varProcessor)) {
             pageContext.setAttribute(varProcessor, processor);
         }
 
         // Write the FORM start tag.
         try {
             @SuppressWarnings("all")
             HtmlWriter writer = new HtmlWriter(pageContext.getOut());
 
             writer.writeTag("form",
                     "id", getId(),
                     "method", method.toLowerCase(),
                     "action", "",
                     attributes);
                 writer.writeTag("input",
                         "type", "hidden",
                         "name", ID_PARAMETER,
                         "value", formId);
 
 
                 // If process() was already called by FormFilter
                 String bufferedOutput = FormFilter.Static.getBufferedOutput(formId, request);
                 if (bufferedOutput != null) {
                     writer.write(bufferedOutput);
 
                     success = FormFilter.Static.isFormSuccess(request);
                     result = FormFilter.Static.getFormResult(request);
                     error = FormFilter.Static.getFormError(request);
 
                 } else {
                     // process it
                     try {
                         result = processor.process(request, new PageContextAwareResponse(pageContext));
                         success = true;
 
                     } catch (IOException e) {
                         throw e;
 
                     } catch (Throwable e) {
                         error = e;
                         success = false;
                     }
                 }
 
                 // Set the status vars if they are defined
                 if (!ObjectUtils.isBlank(varSuccess)) {
                     pageContext.setAttribute(varSuccess, isSuccess());
                 }
                 if (!ObjectUtils.isBlank(varResult)) {
                     pageContext.setAttribute(varResult, getResult());
                 }
                 if (!ObjectUtils.isBlank(varError)) {
                     pageContext.setAttribute(varError, getError());
                 }
 
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
 
         } finally {
             // null out the transient variables
             success = null;
             error = null;
             result = null;
         }
 
         return EVAL_PAGE;
     }
 
     // --- DynamicAttribute support ---
 
     @Override
     public void setDynamicAttribute(String uri, String localName, Object value) {
         attributes.put(localName, value != null ? value.toString() : null);
     }
 
     private static class PageContextAwareResponse extends HttpServletResponseWrapper {
 
         private PrintWriter writer;
 
         public PageContextAwareResponse(PageContext pageContext) {
             super((HttpServletResponse) pageContext.getResponse());
             this.writer = new PrintWriter(pageContext.getOut());
         }
 
         @Override
         public PrintWriter getWriter() {
             return writer;
         }
     }
 
     public static final class Static {
 
         public static FormProcessor2 getProcessorById(String id, HttpServletRequest request) {
             if (id == null) {
                 return null;
             }
 
             // Strip off the form ID attribute part of the ID if it exists.
             String processorId;
             int slashAt = id.indexOf('/');
             if (slashAt < 0) {
                 processorId = id;
 
             } else {
                 processorId = id.substring(0, slashAt);
             }
 
             FormProcessor2 processor = (FormProcessor2) request.getAttribute(PROCESSOR_ATTRIBUTE_PREFIX + id);
 
             if (processor == null) {
                 Class<? extends FormProcessor2> processorClass = PROCESSOR_CLASSES.get(processorId);
                 if (processorClass != null) {
                     processor = TypeDefinition.getInstance(processorClass).newInstance();
                 }
                 if (processor != null) {
                     request.setAttribute(PROCESSOR_ATTRIBUTE_PREFIX + id, processor);
                 }
             }
 
             return processor;
         }
 
         private static String getProcessorId(Class<? extends FormProcessor2> processorClass) {
             String hashId = StringUtils.hex(StringUtils.md5(processorClass.getName()));
             PROCESSOR_CLASSES.putIfAbsent(hashId, processorClass);
             return hashId;
         }
     }
 }
