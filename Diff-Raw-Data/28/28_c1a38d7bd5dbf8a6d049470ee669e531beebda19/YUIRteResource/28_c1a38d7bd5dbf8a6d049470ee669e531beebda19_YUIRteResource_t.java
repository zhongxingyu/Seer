 /*
  * Copyright 2006 Wyona
  */
 
 package org.wyona.yanel.impl.resources.yuirte;
 
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.log4j.Logger;
 import org.apache.xml.resolver.tools.CatalogResolver;
 import org.w3c.tidy.Tidy;
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.ModifiableV2;
 import org.wyona.yanel.core.attributes.viewable.View;
 import org.wyona.yanel.core.source.SourceResolver;
 import org.wyona.yanel.core.util.PathUtil;
 import org.wyona.yanel.core.util.ResourceAttributeHelper;
 import org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource;
 import org.wyona.yanel.impl.resources.usecase.UsecaseException;
 
 
 /**
  *
  */
 public class YUIRteResource extends ExecutableUsecaseResource {
     
     private static Logger log = Logger.getLogger(YUIRteResource.class);
     
     private static final String PARAMETER_EDIT_PATH = "edit-path";
     private static final String PARAMETER_CONTINUE_PATH = "continue-path";
     private static final String PARAM_SUBMIT_TIDY = "submit-tidy";

     private static final String VIEW_FIX_WELLFORMNESS = "fix-wellformness";
     
     private String content;
 
     protected View processUsecase(String viewID) throws UsecaseException {
         if (getParameter(PARAM_SUBMIT) != null) {
             if (!isWellformed()) {
                 return generateView(VIEW_FIX_WELLFORMNESS);
             }
             execute();
             return generateView(VIEW_DONE);
         } else if (getParameter(PARAM_SUBMIT_TIDY) != null) {
             tidy();
             return generateView(VIEW_FIX_WELLFORMNESS);
         } else if (getParameter(PARAM_CANCEL) != null) {
             cancel();
             return generateView(VIEW_CANCEL);
         } else {
             return generateView(viewID); // this will show the default view if the param is not set
         }
     }
     
     public void execute() throws UsecaseException {
         final String editPath = getEditPath();
         final String content = getContent();
 
        if (log.isDebugEnabled()) log.debug("Content while exectuing: " + content);
        
         try {
             Resource resToEdit = getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath);
             if (ResourceAttributeHelper.hasAttributeImplemented(resToEdit, "Modifiable", "2")) {
                 OutputStream os = ((ModifiableV2) resToEdit).getOutputStream();
                 IOUtils.write(content, os);
                 addInfoMessage("Successfully saved.");
                 setParameter(PARAMETER_CONTINUE_PATH, PathUtil.backToRealm(getPath()) + editPath.substring(1)); // allow jelly template to show link to new event
             } else {
                 addError("The resource you wanted to edit does not implement VieableV2 and is therefor not editable with this editor.");
             }
         } catch (Exception e) {
             log.error("Exception: " + e);
             throw new UsecaseException(e.getMessage(), e);
         }
     }
     
     private void tidy() throws UsecaseException {
        //TODO: tidy should be configured via an external file (e.g. in htdocs)
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         Tidy tidy = new Tidy();
         tidy.setXHTML(true);
         tidy.setInputEncoding("utf-8");
         tidy.setOutputEncoding("utf-8");
         tidy.parse(IOUtils.toInputStream(getContent()), os);
         try {
             content = os.toString("utf-8");
         } catch (Exception e) {
             throw new UsecaseException(e.getMessage(), e);
         }
     }
     
     public void cancel() throws UsecaseException {
         addInfoMessage("Cancled.");
         setParameter(PARAMETER_CONTINUE_PATH, PathUtil.backToRealm(getPath()) + getEditPath().substring(1)); // allow jelly template to show link to new event
     }
 
     public boolean isWellformed() throws UsecaseException {
         try {
            //TODO: code borrowed from YanelServlet.java r40436. see line 902. 1. maybe there is a better way to do so. 2. this code could maybe be refactored into a some xml.util lib. 
             javax.xml.parsers.DocumentBuilderFactory dbf= javax.xml.parsers.DocumentBuilderFactory.newInstance();
             javax.xml.parsers.DocumentBuilder parser = dbf.newDocumentBuilder();
             // NOTE: DOCTYPE is being resolved/retrieved (e.g. xhtml schema from w3.org) also
             //       if isValidating is set to false.
             //       Hence, for performance and network reasons we use a local catalog ...
             //       Also see http://www.xml.com/pub/a/2004/03/03/catalogs.html
             //       resp. http://xml.apache.org/commons/components/resolver/
             // TODO: What about a resolver factory?
             parser.setEntityResolver(new CatalogResolver());
             String content = getContent();
             parser.parse(IOUtils.toInputStream(content));
             return true;
         } catch (org.xml.sax.SAXException e) {
             addError(e.getMessage());
             return false;
         } catch (Exception e) {
             addError(e.getMessage());
             return false;
         }
     }
     
     public String getEditorFormContent() throws UsecaseException {
         try {
            if(log.isDebugEnabled()) log.debug(getEditPath());
             Resource resToEdit = getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), getEditPath());
             if (ResourceAttributeHelper.hasAttributeImplemented(resToEdit, "Modifiable", "2")) {
                 InputStream is = ((ModifiableV2) resToEdit).getInputStream();
                 return StringEscapeUtils.escapeXml(IOUtils.toString(is));
             } else {
                return"The resource you wanted to edit does not implement VieableV2 and is therefor not editable with this editor.";
             }
         } catch (Exception e) {
             log.error("Exception: " + e);
             throw new UsecaseException(e.getMessage(), e);
         }
     }
 
     /**
      * TODO: no lookup resource-type yet
      * @return
      * @throws UsecaseException
      */
     public String getLookup() throws UsecaseException {
         try {
             SourceResolver resolver = new SourceResolver(this);
             Source source = resolver.resolve("yanelresource:/usecases/lookup.html", null);
             InputStream htodoc = ((StreamSource) source).getInputStream();
             return IOUtils.toString(htodoc);
         } catch (Exception e) {
             return "no parameter edit-path found in the request. don't know what to edit";
         }
     }
     
     public String getEditPath() {
         return request.getParameter(PARAMETER_EDIT_PATH);
     }
     
     /**
      * @return 
      */
     private String getContent() {
        if(log.isDebugEnabled()) log.debug("Member field content: " + content);
         if (content != null) {
            if(log.isDebugEnabled()) log.debug("content is allready set.");
             return content;
         } else {
             content = getEnvironment().getRequest().getParameter(getEditPath()); 
         }
         return content;
     }
     
     public String getEscapedContent() {
         return StringEscapeUtils.escapeXml(getContent());
     }
 }
