 package fr.cg95.cvq.service.users.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.groovy.control.CompilationFailedException;
 import org.xhtmlrenderer.pdf.ITextRenderer;
 
 import com.lowagie.text.DocumentException;
 
 import fr.cg95.cvq.business.authority.LocalAuthorityResource;
 import fr.cg95.cvq.business.authority.LocalReferentialType;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Type;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.dao.request.IRequestFormDAO;
 import fr.cg95.cvq.exception.CvqException;
import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.ILocalReferentialService;
 import fr.cg95.cvq.service.users.ICertificateService;
 import fr.cg95.cvq.service.users.IIndividualService;
 import fr.cg95.cvq.util.translation.ITranslationService;
 import groovy.text.SimpleTemplateEngine;
 import groovy.text.Template;
 
 /**
  * @author bor@zenexity.fr
  *
  * @todo implement a cache mecanism
  */
 public class CertificateService implements ICertificateService {
 
     private static Logger logger = Logger.getLogger(CertificateService.class);
 
     protected ILocalAuthorityRegistry localAuthorityRegistry;
     protected ITranslationService translationService;
     protected IIndividualService individualService;
     protected ILocalReferentialService localReferentialService;
 
     protected IRequestFormDAO requestFormDAO;
      
     public byte[] generate(Request request) throws CvqException {
         String htmlFilename = 
             StringUtils.uncapitalize(request.getRequestType().getLabel().replace(" ", "")) + "Request";
         File htmlTemplate =
             localAuthorityRegistry.getReferentialResource(Type.CERTIFICATE_TEMPLATE, htmlFilename);
         if (htmlTemplate == null || !htmlTemplate.exists()) {
             logger.warn("generate() certificate template file denoted by name "
                     + htmlFilename + ".html does not exist on filesystem");
             return null;
         }
         File logoFile = localAuthorityRegistry.getLocalAuthorityResourceFile(
                 LocalAuthorityResource.LOGO_PDF.getId(), false);
         
         File cssFile = localAuthorityRegistry.getLocalAuthorityResourceFile(
                 LocalAuthorityResource.Type.CSS,"certificate", true);
         
         Adult requester = null;
         if (request.getRequesterId() != null)
             requester = individualService.getAdultById(request.getRequesterId());
         Individual subject = null;
         if (request.getSubjectId() != null)
             subject = individualService.getById(request.getSubjectId());
         try {
             SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
             Template template = templateEngine.createTemplate(htmlTemplate);
             Map<String, Object> bindings = new HashMap<String, Object>();
            bindings.put("localAuthority", SecurityContext.getCurrentSite());
             bindings.put("rqt", request);
             bindings.put("requester", requester);
             bindings.put("subject", subject);
             bindings.put("lrTypes", getLocalReferentialTypes(request.getRequestType().getLabel()));
             bindings.put("cssPath", cssFile.getAbsolutePath());
             bindings.put("logoPath", logoFile.getAbsolutePath());
             bindings.put("i18n", translationService);
             File htmlCertificateFile = File.createTempFile(htmlFilename, ".html");
             template.make(bindings).writeTo(new FileWriter(htmlCertificateFile));
             
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ITextRenderer renderer = new ITextRenderer();
             renderer.setDocument(htmlCertificateFile);
             renderer.layout();
             renderer.createPDF(baos);
             return baos.toByteArray();
         } catch (CompilationFailedException e) {
             e.printStackTrace();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (DocumentException e) {
             e.printStackTrace();
         }
         return null;
     }
     
     // FIXME - feature duplicated in CertificateService
     // TODO - mutualize
     private Map<String,LocalReferentialType> getLocalReferentialTypes(String requestTypeLabel) {
         Map<String,LocalReferentialType> result = new HashMap<String,LocalReferentialType>();
         try {
             for(LocalReferentialType lrt : 
                 localReferentialService.getLocalReferentialDataByRequestType(requestTypeLabel))
                 result.put(StringUtils.uncapitalize(lrt.getDataName()), lrt);
         } catch (CvqException ce) { /* No localReferentialData found ! */ }
         return result;
     }
     
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setTranslationService(ITranslationService translationService) {
         this.translationService = translationService;
     }
 
     public void setIndividualService(IIndividualService individualService) {
         this.individualService = individualService;
     }
 
     public void setLocalReferentialService(ILocalReferentialService localReferentialService) {
         this.localReferentialService = localReferentialService;
     }
     
 
 }
