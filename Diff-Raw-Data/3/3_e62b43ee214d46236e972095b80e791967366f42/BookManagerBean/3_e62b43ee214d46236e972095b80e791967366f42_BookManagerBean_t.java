 package org.nuxeo.project.sample;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import javax.ejb.PostActivate;
 import javax.ejb.PrePassivate;
 import javax.ejb.Remove;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.EditableValueHolder;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Destroy;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Observer;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.annotations.remoting.WebRemote;
 import org.jboss.seam.core.Events;
 import org.jboss.seam.faces.FacesMessages;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.directory.Session;
 import org.nuxeo.ecm.directory.api.DirectoryService;
 import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
 import org.nuxeo.ecm.platform.ui.web.api.WebActions;
 import org.nuxeo.ecm.webapp.helpers.EventNames;
 import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
 import org.nuxeo.ecm.webapp.pagination.ResultsProvidersCache;
 import org.nuxeo.project.sample.BookResultsProviderFarm.KeywordCriteria;
 import org.nuxeo.runtime.api.Framework;
 
 @Scope(ScopeType.CONVERSATION)
 @Name("bookManager")
 public class BookManagerBean implements BookManager, Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private static final Log log = LogFactory.getLog(BookManagerBean.class);
 
     @PrePassivate
     public void prePassivate() {
         log.debug("prePassivate");
     }
 
     @PostActivate
     public void postActivate() {
         log.debug("postActivate");
     }
 
     @Remove
     @Destroy
     public void destroy() {
         log.debug("destroy");
     }
 
     @In(create = true)
     protected transient NavigationContext navigationContext;
 
     @In(create = true)
     protected transient WebActions webActions;
 
     @In(create = true)
     protected transient CoreSession documentManager;
 
     @In(create = true)
     protected transient FacesMessages facesMessages;
 
     @In(create = true)
     protected transient ResourcesAccessor resourcesAccessor;
 
     @In(required = true)
     protected transient ResultsProvidersCache resultsProvidersCache;
 
     private String firstName;
 
     private String lastName;
 
     private String isbn;
 
     private List<String> keywords;
 
     protected List<SelectItem> keywordList;
 
     private String page;
 
     private int rating;
 
     private String filter;
 
     public String getParentTitle() throws ClientException {
         DocumentModel doc = navigationContext.getCurrentDocument();
         DocumentModel parent = documentManager.getParentDocument(doc.getRef());
         return (String) parent.getProperty("dublincore", "title");
     }
 
     public String getFirstName() {
         if (firstName == null) {
             firstName = "";
         }
         return firstName;
     }
 
     public void setFirstName(String s) {
         firstName = s;
     }
 
     public String getLastName() {
         if (lastName == null) {
             lastName = "";
         }
         return lastName;
     }
 
     public void setLastName(String s) {
         lastName = s;
     }
 
     public String getIsbn() {
         if (isbn == null) {
             return "";
         } else {
             return isbn;
         }
     }
 
     public void setIsbn(String s) {
         isbn = s;
     }
 
     public int getRating() {
         return rating;
     }
 
     public void setRating(int rating) {
         this.rating = rating;
     }
 
     public List<String> getKeywords() {
         return keywords;
     }
 
     public void setKeywords(List<String> keywords) {
         this.keywords = keywords;
     }
 
     public List<SelectItem> getAvailableKeywords() throws ClientException {
         if (keywordList == null) {
             computeKeywordValues();
         }
         return keywordList;
     }
 
     @Observer(value = { EventNames.DOCUMENT_SELECTION_CHANGED }, create = false)
     public void resetKeywordValues() {
         keywordList = null;
     }
 
     private void computeKeywordValues() throws ClientException {
         DirectoryService dirService;
         try {
             // This should work but doesn't in 5.1.3
             // dirService = Framework.getService(DirectoryService.class);
             dirService = Framework.getLocalService(DirectoryService.class);
         } catch (Exception e) {
             throw new ClientException(e);
         }
 
         Session dir = null;
         try {
             dir = dirService.open("book_keywords");
             DocumentModelList entries = dir.getEntries();
             keywordList = new ArrayList<SelectItem>(entries.size());
             for (DocumentModel e : entries) {
                 String label = (String) e.getProperty("vocabulary", "label");
                 SelectItem item = new SelectItem(label);
                 keywordList.add(item);
             }
         } finally {
             if (dir != null) {
                 dir.close();
             }
         }
     }
 
     private static Random rand = new Random();
 
     protected static String[] FIRSTNAMES = { "Steve", "John", "Raoul", "James" };
 
     protected static String[] LASTNAMES = { "Bond", "Einstein", "Tanaka",
             "Presley" };
 
     public void randomFirstName() {
         firstName = FIRSTNAMES[rand.nextInt(FIRSTNAMES.length)];
     }
 
     public void randomLastName() {
         lastName = LASTNAMES[rand.nextInt(LASTNAMES.length)];
     }
 
     /*
      * Validation / change
      */
 
     public void changeData() throws ClientException {
         if (getFirstName().equals(getLastName())) {
             facesMessages.add(FacesMessage.SEVERITY_ERROR,
                     "First name and last name must be different");
         }
 
         DocumentModel document = navigationContext.getChangeableDocument();
         String title = getFirstName() + " " + getLastName();
         document.setProperty("dublincore", "title", title);
         document.setProperty("book", "rating", Long.valueOf(rating));
         document.setProperty("book", "keywords", keywords);
 
         documentManager.saveDocument(document);
         documentManager.save();
     }
 
     public void validation(FacesContext context, UIComponent component,
             Object value) {
         Integer v = (Integer) value;
         if ((v.intValue() % 2) != 0) {
             ((EditableValueHolder) component).setValid(false);
             FacesMessage message = new FacesMessage();
             message.setDetail("The value must be a multiple of 2");
             message.setSummary("Not a multiple of 2");
             message.setSeverity(FacesMessage.SEVERITY_ERROR);
             facesMessages.add(component.getId(), message);
         }
     }
 
     /*
      * Search
      */
     public DocumentModelList getSearchResults() throws Exception {
         DocumentModelList result = documentManager.query("SELECT * FROM Book",
                 10);
         return result;
     }
 
     /*
      * Ajax
      */
 
     public boolean hasFilter() {
         return filter != null;
     }
 
     public String getFilter() {
         return filter;
     }
 
     public void setFilter(String newfilter) {
         if (!(filter == null || filter.equals(newfilter))) {
             resultsProvidersCache.invalidate(BookResultsProviderFarm.KEYWORD_KEY);
         }
         this.filter = newfilter;
     }
 
     /*
      * Seam remoting
      */
 
     /**
      * @param param some string, that is directly passed from the Javascript
      *            code.
      */
     @WebRemote
     public String something(String param) {
         return "It worked: " + param;
     }
 
     /*
      * Wizard
      */
     public String toWizardPage(String page) {
         this.page = page;
         return "bookwizard";
     }
 
     public String getWizardPage() {
         return "/incl/bookwizard_page" + page + ".xhtml";
     }
 
     public String validateWizard() throws ClientException {
         DocumentModel document = navigationContext.getChangeableDocument();
         String title = getFirstName() + " " + getLastName();
         document.setProperty("dublincore", "title", title);
         document.setProperty("book", "isbn", isbn);
         documentManager.saveDocument(document);
         documentManager.save();
         return webActions.setCurrentTabAndNavigate("TAB_VIEW");
     }
 
     /*
      * Books listing in folder
      */
     public List<BookInfo> getBooksInFolder() throws ClientException {
         List<BookInfo> list = new LinkedList<BookInfo>();
 
         DocumentModel folder = navigationContext.getCurrentDocument();
         DocumentModelList children = documentManager.getChildren(
                 folder.getRef(), "Book");
         for (DocumentModel doc : children) {
             String[] keywords = (String[]) doc.getProperty("book", "keywords");
            if(keywords == null) {
            	continue;
            }
             list.add(new BookInfo(doc, Arrays.asList(keywords)));
         }
         return list;
     }
 
     public static class BookInfo {
 
         private DocumentModel doc;
 
         private List<String> labels;
 
         public BookInfo(DocumentModel doc, List<String> labels) {
             this.doc = doc;
             this.labels = labels;
         }
 
         public DocumentModel getDocument() {
             return doc;
         }
 
         public List<String> getLabels() {
             return labels;
         }
 
     }
 
     /*
      * Unused
      */
     public String duplicateSiblings() throws ClientException {
         DocumentModel doc = navigationContext.getCurrentDocument();
         DocumentModel folder = documentManager.getParentDocument(doc.getRef());
         DocumentModel gp = documentManager.getParentDocument(folder.getRef());
 
         // find the other folder names
         Set<String> names = new HashSet<String>();
         for (DocumentModel f : documentManager.getChildren(gp.getRef())) {
             names.add(f.getName());
         }
         // find a new unique name
         String newFolderName = folder.getName();
         while (names.contains(newFolderName)) {
             newFolderName += "copy";
         }
         // create the new folder
         DocumentModel newFolder = documentManager.createDocumentModel(
                 gp.getPathAsString(), newFolderName, folder.getType());
         newFolder.setProperty("dublincore", "title", "Nouveau folder");
         documentManager.createDocument(newFolder);
 
         // create the children
         String newFolderPath = newFolder.getPathAsString();
         for (DocumentModel child : documentManager.getChildren(folder.getRef(),
                 "Book")) {
             DocumentModel newChild = documentManager.createDocumentModel(
                     newFolderPath, child.getName(), "Note");
             String title = child.getProperty("dublincore", "title")
                     + " duplicated";
             newChild.setProperty("dublincore", "title", title);
             documentManager.createDocument(newChild);
         }
         documentManager.save();
 
         Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED, gp);
         return null;
     }
 
     private Path getContainerPath() {
         DocumentModel currentDocument = navigationContext.getCurrentDocument();
         if (currentDocument.getDocumentType().getName().equals("Book"))
             return currentDocument.getPath().removeLastSegments(1);
         return currentDocument.getPath();
     }
 
     public KeywordCriteria getKeywordCriteria() {
         return new KeywordCriteria(getContainerPath(), getFilter());
     }
 
 }
