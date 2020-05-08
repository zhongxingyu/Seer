 package fr.cg95.cvq.service.document.impl;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.*;
 
 import javax.imageio.ImageIO;
 
 import org.apache.axis.utils.ByteArrayOutputStream;
 import org.apache.log4j.Logger;
 import org.apache.pdfbox.exceptions.COSVisitorException;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.util.PDFMergerUtility;
 import org.springframework.context.ApplicationListener;
 
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.pdf.PdfWriter;
 
 import eu.medsea.mimeutil.MimeUtil2;
 import fr.cg95.cvq.business.authority.LocalAuthority;
 import fr.cg95.cvq.business.document.ContentType;
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentAction;
 import fr.cg95.cvq.business.document.DocumentBinary;
 import fr.cg95.cvq.business.document.DocumentState;
 import fr.cg95.cvq.business.document.DocumentType;
 import fr.cg95.cvq.business.document.DocumentTypeValidity;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.UsersEvent;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.dao.document.IDocumentDAO;
 import fr.cg95.cvq.dao.document.IDocumentTypeDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.exception.CvqDisabledFunctionalityException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.util.translation.ITranslationService;
 
 /**
  * Implementation of the {@link IDocumentService} service.
  *
  * @author bor@zenexity.fr
  */
 public class DocumentService implements IDocumentService, ApplicationListener<UsersEvent> {
 
     static Logger logger = Logger.getLogger(DocumentService.class);
 
     protected ILocalAuthorityRegistry localAuthorityRegistry;
 
     protected IDocumentDAO documentDAO;
     protected IDocumentTypeDAO documentTypeDAO;
     private ITranslationService translationService;
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.READ)
     public Document getById(final Long id)
     throws CvqException, CvqObjectNotFoundException {
         return (Document) documentDAO.findById(Document.class, id);
     }
 
     /**
      * Compute a default end validity date for the given document, according to its
      * type.
      */
     private void computeEndValidityDate(Document document) {
 
         DocumentType docType = document.getDocumentType();
         DocumentTypeValidity docTypeValidity = docType.getValidityDurationType();
 
         Calendar calendar = new GregorianCalendar();
         Date currentDate = new Date();
         calendar.setTime(currentDate);
 
         if (docTypeValidity.equals(DocumentTypeValidity.UNLIMITED)) {
             document.setEndValidityDate(null);
         } else if (docTypeValidity.equals(DocumentTypeValidity.YEAR)) {
             Integer duration = docType.getValidityDuration();
             calendar.add(Calendar.YEAR, duration.intValue());
             document.setEndValidityDate(calendar.getTime());
             logger.debug("Set default end validity date to "
                     + document.getEndValidityDate());
         } else if (docTypeValidity.equals(DocumentTypeValidity.MONTH)) {
             Integer duration = docType.getValidityDuration();
             calendar.add(Calendar.MONTH, duration.intValue());
             document.setEndValidityDate(calendar.getTime());
             logger.debug("Set default end validity date to "
                     + document.getEndValidityDate());
         } else if (docTypeValidity.equals(DocumentTypeValidity.END_YEAR)) {
             calendar.set(Calendar.MONTH, Calendar.DECEMBER);
             calendar.set(Calendar.DAY_OF_MONTH, 31);
             document.setEndValidityDate(calendar.getTime());
             logger.debug("Set default end validity date to "
                     + document.getEndValidityDate());
         } else if (docTypeValidity.equals(DocumentTypeValidity.END_SCHOOL_YEAR)) {
             if (calendar.get(Calendar.MONTH) > Calendar.JUNE)
                 calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
             calendar.set(Calendar.MONTH, Calendar.JUNE);
             calendar.set(Calendar.DAY_OF_MONTH, 30);
             document.setEndValidityDate(calendar.getTime());
             logger.debug("Set default end validity date to "
                     + document.getEndValidityDate());
         }
     }
 
     public void checkDocumentsValidity()
     throws CvqException {
 
         localAuthorityRegistry.browseAndCallback(this, "checkLocalAuthDocumentsValidity", null);
     }
 
     @Context(types = {ContextType.SUPER_ADMIN})
     public void checkLocalAuthDocumentsValidity()
         throws CvqException {
         logger.debug("checkLocalAuthDocumentsValidity() dealing with " 
             + SecurityContext.getCurrentSite().getName());
         for (Long id : documentDAO.listOutdated()) {
             HibernateUtil.beginTransaction();
             updateDocumentState(id, DocumentState.OUTDATED, "", null);
             HibernateUtil.commitTransaction();
             HibernateUtil.closeSession();
         }
     }
 
     public void launchDocumentMissingValuesComputing() {
         localAuthorityRegistry.browseAndCallback(this, "computeMissingValues", null);
     }
 
     @Context(types = {ContextType.SUPER_ADMIN})
     public void computeMissingValues() {
         MimeUtil2 util = new MimeUtil2();
         util.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
         util.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
         for (Long docId : documentDAO.listByMissingComputedValues()) {
             HibernateUtil.beginTransaction();
             Document document;
             try {
                 document = (Document)documentDAO.findById(Document.class, docId);
             } catch (CvqObjectNotFoundException e1) {
                 // document shouldn't disappear...
                 continue;
             }
             boolean modified = false;
             for (DocumentBinary page : document.getDatas()) {
                 if (page.getContentType() == null
                     || page.getContentType().equals(ContentType.OCTET_STREAM)) {
                     try {
                         page.setContentType(ContentType.forString(
                             MimeUtil2.getMostSpecificMimeType(util.getMimeTypes(page.getData()))
                                 .toString()));
                         modified = true;
                     } catch (Exception e) {
                         logger.info("failed to create preview for document_binary " + page.getId(), e);
                     }
                 }
                 if (page.getPreview() == null
                     && page.getContentType() != null && page.getContentType().isAllowed()) {
                     try {
                         createPreview(page);
                         modified = true;
                     } catch (Exception e) {
                         logger.info("failed to create preview for document_binary " + page.getId(), e);
                     }
                 }
             }
             if (modified) {
                 documentDAO.update(document);
             }
             HibernateUtil.commitTransaction();
             HibernateUtil.closeSession();
         }
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public Long create(Document document)
     throws CvqException, CvqObjectNotFoundException {
 
         if (document == null)
             throw new CvqException("No document object provided");
         if (document.getDocumentType() == null)
             throw new CvqException("You must provide a type for your document");
 
         document.setCreationDate(new Date());
         document.setDepositId(SecurityContext.getCurrentUserId());
 
         computeEndValidityDate(document);
 
         Long documentId = documentDAO.create(document);
 
         logger.debug("Created document object with id : " + documentId);
 
         addActionTrace(CREATION_ACTION, DocumentState.PENDING, document);
 
         // when creating a new document in FO, we need it to be persisted before rendering the view
         HibernateUtil.getSession().flush();
 
         return documentId;
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public void modify(final Document document)
     throws CvqException {
 
         if (document == null)
             return;
         documentDAO.update(document);
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public void delete(final Long id)
     throws CvqException, CvqObjectNotFoundException {
 
         Document document = getById(id);
         documentDAO.delete(document);
 
         // when deleting a new document in FO, we need it to be removed from DB before rendering the view
         HibernateUtil.getSession().flush();
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public void addPage(final Long documentId, final byte[] data) throws  CvqException {
         if (data == null || data.length == 0)
             return;
         checkDocumentDigitalizationIsEnabled();
         Document document = getById(documentId);
         checkNewBinaryData(document, document.getDatas().size(), data);
         DocumentBinary documentBinary = new DocumentBinary();
         try {
             documentBinary.setContentType(mimeTypeFromBytes(data));
             documentBinary.setData(data);
             createPreview(documentBinary);
             if (document.getDatas() == null) {
                 List<DocumentBinary> dataList = new ArrayList<DocumentBinary>();
                 dataList.add(documentBinary);
                 document.setDatas(dataList);
             } else
                 document.getDatas().add(documentBinary);
             documentDAO.update(document);
             addActionTrace(PAGE_ADD_ACTION, null, document);
         } catch (CvqModelException cme) {
             throw new CvqModelException(cme.getI18nKey());
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public void modifyPage(final Long documentId, final int dataIndex ,final byte[] data)
             throws CvqException {
         if (data == null || data.length == 0)
             return;
         checkDocumentDigitalizationIsEnabled();
         Document document = getById(documentId);
         checkNewBinaryData(document, dataIndex, data);
         DocumentBinary documentBinary = document.getDatas().get(dataIndex);
         documentBinary.setContentType(mimeTypeFromBytes(data));
         documentBinary.setData(data);
 
         if (document.getDatas().size() == 1) {
             if (document.getState().equals(DocumentState.OUTDATED)) {
                 document.setState(DocumentState.PENDING);
                 document.setValidationDate(null);
                 addActionTrace(STATE_CHANGE_ACTION, DocumentState.PENDING, document);
             }
         }
         createPreview(documentBinary);
         addActionTrace(PAGE_EDIT_ACTION, null, document);
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public void deletePage(final Long documentId, final Integer pageId)
     throws CvqException, CvqObjectNotFoundException {
 
         checkDocumentDigitalizationIsEnabled();
 
         Document document = getById(documentId);
         DocumentBinary documentBinary = document.getDatas().get(pageId);
         document.getDatas().remove(documentBinary);
         documentDAO.delete(documentBinary);
         documentDAO.update(document);
 
         addActionTrace(PAGE_DELETE_ACTION, null, document);
     }
 
     private ContentType mimeTypeFromBytes(final byte[] data) {
         MimeUtil2 util = new MimeUtil2();
         util.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
         util.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
         return ContentType.forString(
             MimeUtil2.getMostSpecificMimeType(util.getMimeTypes(data)).toString());
     }
 
     private void checkNewBinaryData(final Document document, final int index, final byte[] data)
         throws CvqModelException {
         ContentType type = mimeTypeFromBytes(data);
         if (!type.isAllowed())
             throw new CvqModelException("document.message.fileTypeIsNotSupported");
        if (document.getDatas() != null && document.getDatas().size() > 1 && index > 0)
             if (!document.getDatas().get(0).getContentType().equals(type))
                 throw new CvqModelException("document.file.error.contentTypeIsNotSameCompareToOtherPage");
     }
 
     private void checkDocumentDigitalizationIsEnabled()
         throws CvqDisabledFunctionalityException {
         LocalAuthority la = SecurityContext.getCurrentSite();
         if (!la.isDocumentDigitalizationEnabled()) {
             logger.error("checkDocumentDigitalizationIsEnabled() document digitalization is not enabled for site "
                     + la.getName());
             throw new CvqDisabledFunctionalityException();
         }
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.READ)
     public Set<DocumentBinary> getAllPages(final Long documentId)
         throws CvqException {
 
         Document document = getById(documentId);
 
         if (document.getDatas() == null)
             return new LinkedHashSet<DocumentBinary>();
         else
             return new LinkedHashSet<DocumentBinary>(document.getDatas());
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     private void deleteHomeFolderDocuments(Long homeFolderId) {
         List<Document> documents = getHomeFolderDocuments(homeFolderId, -1);
         for (Document document : documents)
             documentDAO.delete(document);
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     private void deleteIndividualDocuments(Long individualId) {
         List<Document> documents = getIndividualDocuments(individualId);
         logger.debug("deleteIndividualDocuments() deleting " + documents.size() + " document(s)");
         for (Document document : documents)
             documentDAO.delete(document);
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Document> getProvidedDocuments(final DocumentType docType,
             final Long homeFolderId, final Long individualId)
             throws CvqException {
 
         if (docType == null)
             throw new CvqException("No document type provided");
         if (homeFolderId == null)
             throw new CvqException("No home folder id provided");
 
         return documentDAO.listProvidedDocuments(docType.getId(),
                 homeFolderId, individualId);
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Document> getHomeFolderDocuments(final Long homeFolderId, int maxResults) {
 
         return documentDAO.listByHomeFolder(homeFolderId, maxResults);
     }
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Document> getIndividualDocuments(final Long individualId) {
 
         return documentDAO.listByIndividual(individualId);
     }
 
     public List<Document> getBySessionUuid(final String sessionUuid) {
         return documentDAO.findBySimpleProperty(Document.class, "sessionUuid", sessionUuid);
     }
 
     @Context(types = {ContextType.ECITIZEN}, privilege = ContextPrivilege.NONE)
     public Integer searchCount(Hashtable<String,Object> searchParams) {
         return documentDAO.searchCount(this.prepareSearchParams(searchParams));
     }
 
     @Context(types = {ContextType.ECITIZEN}, privilege = ContextPrivilege.NONE)
     public List<Document> search(Hashtable<String,Object> searchParams,int max,int offset) {
         return documentDAO.search(this.prepareSearchParams(searchParams),max,offset);
     }
 
     protected Hashtable<String,Object> prepareSearchParams(Hashtable<String,Object> searchParams) {
 
         if (searchParams == null)
             searchParams = new Hashtable<String, Object>();
 
         if (!searchParams.containsKey("homeFolderId") && !searchParams.containsKey("individualId")) {
             Adult user = SecurityContext.getCurrentEcitizen();
             List<Long> individuals = new ArrayList<Long>();
             for (Individual i : user.getHomeFolder().getIndividuals())
                 individuals.add(i.getId());
 
             searchParams.put("homeFolderId",user.getHomeFolder().getId());
             searchParams.put("individualId",individuals);
         }
 
         return searchParams;
     }
 
     // Document Workflow related methods
     // TODO : make workflow method private - migrate unit tests
     //////////////////////////////////////////////////////////
 
     @Context(types = {ContextType.AGENT})
     public void updateDocumentState(final Long id, final DocumentState ds, final String message,
             final Date validityDate)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         if (ds.equals(DocumentState.VALIDATED))
             validate(id, validityDate, message);
         else if (ds.equals(DocumentState.CHECKED))
             check(id,message);
         else if (ds.equals(DocumentState.REFUSED))
             refuse(id, message);
         else if (ds.equals(DocumentState.OUTDATED))
             outDated(id);
         
         Document doc = getById(id);
         if (!doc.getDatas().isEmpty() && doc.getDatas().get(0).getContentType().equals(ContentType.PDF)) {
             mergeDocumentBinary(doc);
         }
     }
 
     @Context(types = {ContextType.AGENT})
     public void validate(final Long id, final Date validityDate, final String message)
         throws CvqException, CvqObjectNotFoundException, CvqInvalidTransitionException {
 
         Document document = getById(id);
         if (document.getState().equals(DocumentState.VALIDATED))
             return;
 
         if (document.getState().equals(DocumentState.PENDING)
                 || document.getState().equals(DocumentState.CHECKED)) {
             try {
                 document.setState(DocumentState.VALIDATED);
                 document.setEndValidityDate(validityDate);
                 document.setValidationDate(new Date());
                 documentDAO.update(document);
             } catch (RuntimeException e) {
                 throw new CvqException("Could not validate document " + e.toString());
             }
         } else {
             throw new CvqInvalidTransitionException(
                     translationService.translate("document.state."
                             + document.getState().toString().toLowerCase()),
                             translationService.translate("document.state."
                                     + DocumentState.VALIDATED.toString().toLowerCase()));
         }
 
         addActionTrace(STATE_CHANGE_ACTION, DocumentState.VALIDATED, document);
     }
 
     @Context(types = {ContextType.AGENT})
     public void check(final Long id, final String message)
         throws CvqException, CvqObjectNotFoundException, CvqInvalidTransitionException {
 
         Document document = getById(id);
         if (document.getState().equals(DocumentState.CHECKED))
             return;
 
         if (!document.getState().equals(DocumentState.PENDING))
             throw new CvqInvalidTransitionException();
 
         document.setState(DocumentState.CHECKED);
         documentDAO.update(document);
         addActionTrace(STATE_CHANGE_ACTION, DocumentState.CHECKED, document);
     }
 
     @Context(types = {ContextType.AGENT})
     public void refuse(final Long id, final String message)
         throws CvqException, CvqObjectNotFoundException, CvqInvalidTransitionException {
 
         Document document = getById(id);
         if (document.getState().equals(DocumentState.REFUSED))
             return;
 
         if (!document.getState().equals(DocumentState.CHECKED)
                 && !document.getState().equals(DocumentState.PENDING))
             throw new CvqInvalidTransitionException();
 
         document.setState(DocumentState.REFUSED);
         documentDAO.update(document);
 
         addActionTrace(STATE_CHANGE_ACTION, DocumentState.REFUSED, document);
     }
 
     private void outDated(final Long id)
         throws CvqException, CvqObjectNotFoundException, CvqInvalidTransitionException {
 
         Document document = getById(id);
         if (document.getState().equals(DocumentState.OUTDATED))
             return;
 
         if (document.getState().equals(DocumentState.REFUSED))
             throw new CvqInvalidTransitionException();
 
         document.setState(DocumentState.OUTDATED);
         documentDAO.update(document);
 
         addActionTrace(STATE_CHANGE_ACTION, DocumentState.OUTDATED, document);
     }
 
     @Context(types = {ContextType.AGENT})
     public DocumentState[] getPossibleTransitions(DocumentState ds)
         throws CvqException {
 
         ArrayList<DocumentState> documentStateList = new ArrayList<DocumentState>();
 
         if (ds.equals(DocumentState.PENDING)) {
             documentStateList.add(DocumentState.VALIDATED);
             documentStateList.add(DocumentState.CHECKED);
             documentStateList.add(DocumentState.REFUSED);
             documentStateList.add(DocumentState.OUTDATED);
         } else if (ds.equals(DocumentState.VALIDATED)) {
             documentStateList.add(DocumentState.OUTDATED);
         } else if (ds.equals(DocumentState.CHECKED)) {
             documentStateList.add(DocumentState.VALIDATED);
             documentStateList.add(DocumentState.REFUSED);
             documentStateList.add(DocumentState.OUTDATED);
         } else if (ds.equals(DocumentState.REFUSED)) {
             // no more transitions available
         } else if (ds.equals(DocumentState.OUTDATED)) {
             documentStateList.add(DocumentState.PENDING);
         }
 
         return documentStateList.toArray(new DocumentState[documentStateList.size()]);
     }
 
     public void addActionTrace(final String label, final DocumentState resultingState,
             final Document document)
         throws CvqException {
 
         DocumentAction documentAction = new DocumentAction();
         documentAction.setAgentId(SecurityContext.getCurrentUserId());
         documentAction.setLabel(label);
         documentAction.setDate(new Date());
         documentAction.setResultingState(resultingState);
 
         if (document.getActions() == null) {
             Set<DocumentAction> actionsSet = new HashSet<DocumentAction>();
             actionsSet.add(documentAction);
             document.setActions(actionsSet);
         } else {
             document.getActions().add(documentAction);
         }
 
         documentDAO.update(document);
     }
 
     public List<DocumentState> getEditableStates() {
         List<DocumentState> result = new ArrayList<DocumentState>();
 
         result.add(DocumentState.PENDING);
         result.add(DocumentState.OUTDATED);
 
         return result;
     }
 
     @Override
     public void onApplicationEvent(UsersEvent homeFolderEvent) {
         logger.debug("onApplicationEvent() got an home folder event of type " + homeFolderEvent.getEvent());
         if (homeFolderEvent.getEvent().equals(UsersEvent.EVENT_TYPE.HOME_FOLDER_DELETE)) {
             logger.debug("onApplicationEvent() gonna delete home folder "
                     + homeFolderEvent.getHomeFolderId());
             deleteHomeFolderDocuments(homeFolderEvent.getHomeFolderId());
         } else if (homeFolderEvent.getEvent().equals(UsersEvent.EVENT_TYPE.INDIVIDUAL_DELETE)) {
             logger.debug("onApplicationEvent() gonna delete individual "
                     + homeFolderEvent.getIndividualId());
             deleteIndividualDocuments(homeFolderEvent.getIndividualId());
         }
     }
 
     private void createPreview(DocumentBinary page) throws CvqException {
         try {
             if (page.getContentType().equals(ContentType.PDF)) {
                 PDDocument pdDoc = null;
                 try {
                     pdDoc = byteToPDDocument(page.getData());
                     PDPage pdPage = (PDPage) pdDoc.getDocumentCatalog().getAllPages().get(0);
                     BufferedImage bufImgPage = pdPage.convertToImage();
                     bufImgPage = (BufferedImage) resize(bufImgPage, 200);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     ImageIO.write(bufImgPage, "png", baos);
                     byte[] imagePage = baos.toByteArray();
                     page.setPreview(imagePage);
                 } finally {
                     if (pdDoc != null) {
                         pdDoc.close();
                     }
                 }
             } else {
                 ByteArrayInputStream baisPage = new ByteArrayInputStream(page.getData());
                 BufferedImage bufImgPage = ImageIO.read(baisPage);
                 bufImgPage = (BufferedImage) resize(bufImgPage, 200);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ImageIO.write(bufImgPage, "png", baos);
                 byte[] imagePage = baos.toByteArray();
                 page.setPreview(imagePage);
             }
         } catch (IOException ioe) {
             throw new CvqException(ioe.getMessage());
         }
     }
 
     /**
      * Resize an image with a new width
      */
     private static Image resize(Image source, int width) {
         int height = (width * source.getHeight(null)) / source.getWidth(null);
         BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = buf.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g.drawImage(source, 0, 0, width, height, null);
         g.dispose();
         return buf;
     }
 
     /**
      * merge all (pdf) binaries from a document in only one (pdf) binary
      */
     @Context(types = {ContextType.ADMIN, ContextType.AGENT})
     public void mergeDocumentBinary(Document doc) throws CvqException {
        if (!doc.getDatas().isEmpty() && doc.getDatas().get(0).getContentType().equals(ContentType.PDF)) {
            if(doc.getDatas().size() > 1)
                mergePdfDocumentBinary(doc);
        } else {
            mergeImageDocumentBinary(doc);
        }
        documentDAO.update(doc);
     }
 
     private void mergePdfDocumentBinary(Document doc) throws CvqException {
         PDDocument pdDoc = null;
         try {
             pdDoc = byteToPDDocument(doc.getDatas().get(0).getData());
             if (!pdDoc.isEncrypted()) {
                 for (int i=1; i<doc.getDatas().size(); i++) {
                     PDDocument pdDocNew = byteToPDDocument(doc.getDatas().get(i).getData());
                     if (!pdDocNew.isEncrypted()) {
                         PDFMergerUtility pmu = new PDFMergerUtility();
                         pmu.appendDocument(pdDoc, pdDocNew);
                     }
                 }
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 pdDoc.save(baos);
                 DocumentBinary docBin = new DocumentBinary(baos.toByteArray());
                 docBin.setContentType(ContentType.PDF);
                 createPreview(docBin);
                 for (DocumentBinary page : doc.getDatas()) {
                     documentDAO.delete(page);
                 }
                 doc.getDatas().clear();
                 doc.getDatas().add(docBin);
                 addActionTrace(MERGE_ACTION, null, doc);
             }
         } catch (IOException ioe) {
             throw new CvqException(ioe.getMessage());
         } catch (COSVisitorException cve) {
             throw new CvqException(cve.getMessage());
         } finally {
             if (pdDoc != null) {
                 try {
                     pdDoc.close();
                 } catch (IOException ioe) {
                     throw new CvqException(ioe.getMessage());
                 }
             }
         }
     }
 
     private void mergeImageDocumentBinary(Document doc) throws CvqException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         com.lowagie.text.Document document = new com.lowagie.text.Document();
         try {
             PdfWriter.getInstance(document, baos);
             document.open();
             float widthPdf = document.getPageSize().getWidth();
             float heightPdf = document.getPageSize().getHeight();
             for (DocumentBinary docBin : doc.getDatas()) {
                 com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(docBin.getData());
                 if (image.getWidth() < widthPdf/4)
                     image.scaleToFit(widthPdf/2, heightPdf/2);
                 else
                     image.scaleToFit(widthPdf - 30, heightPdf - 30);
                 document.add(image);
                 document.newPage();
             }
         } catch (DocumentException de) {
             throw new CvqException(de.getMessage());
         } catch (MalformedURLException mue) {
             throw new CvqException(mue.getMessage());
         } catch (IOException ioe) {
             throw new CvqException(ioe.getMessage());
         }
 
         if (document.isOpen()) {
             document.close();
         }
 
         DocumentBinary mergeImgToPdfDocBin = new DocumentBinary(baos.toByteArray());
         mergeImgToPdfDocBin.setContentType(ContentType.PDF);
         createPreview(mergeImgToPdfDocBin);
         for (DocumentBinary page : doc.getDatas()) {
             documentDAO.delete(page);
         }
         doc.getDatas().clear();
         doc.getDatas().add(mergeImgToPdfDocBin);
         addActionTrace(MERGE_ACTION, null, doc);
     }
 
     @Context(types = {ContextType.ADMIN, ContextType.AGENT})
     public PDDocument byteToPDDocument(byte[] data) throws CvqException {
         ByteArrayInputStream baisPage = new ByteArrayInputStream(data);
         try {
             PDDocument pdDoc;
             pdDoc = PDDocument.load(baisPage);
             return pdDoc;
         } catch (IOException e) {
             throw new CvqException(e.getMessage());
         }
     }
 
     public void setDocumentDAO(final IDocumentDAO documentDAO) {
         this.documentDAO = documentDAO;
     }
 
     public void setDocumentTypeDAO(final IDocumentTypeDAO documentTypeDAO) {
         this.documentTypeDAO = documentTypeDAO;
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setTranslationService(ITranslationService translationService) {
         this.translationService = translationService;
     }
 }
