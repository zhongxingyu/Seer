 package net.cyklotron.cms.structure.internal;
 
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.cdata;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.doc;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.dom4jToText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.elm;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.selectFirstText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.textToDom4j;
 
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.encodings.HTMLEntityDecoder;
 import org.objectledge.encodings.HTMLEntityEncoder;
 import org.objectledge.html.HTMLException;
 import org.objectledge.html.HTMLService;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.upload.UploadContainer;
 import org.objectledge.upload.UploadLimitExceededException;
 import org.objectledge.utils.StringUtils;
 
 import net.cyklotron.cms.CmsNodeResource;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryResourceImpl;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.DirectoryResourceImpl;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FileResourceImpl;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 
 /**
  * Data object used by ProposeDocument view and action.
  * <p>
  * Feels kind like breaking open door, but I'm not willing to learn formtool just to make one silly
  * screen.
  * </p>
  * <p>
  * Not threadsafe, but there should be no need to share this object among thread.
  * </p>
  * 
  * @author rafal
  */
 public class ProposedDocumentData
 {
     // component configuration
     private boolean calendarTree;
 
     private boolean inheritCategories;
 
     private boolean attachmentsEnabled;
 
     private int attachmentsMaxCount;
 
     private int attachmentsMaxSize;
 
     private String attachmentsAllowedFormats;
 
     private List<String> attachmentFormatList;
 
     private long attachmentDirId;
 
     // form data
     private String name;
 
     private String title;
 
     private String docAbstract;
 
     private String content;
 
     private String eventPlace;
     
     private String eventStreet;
 
     private String eventPostCode;
     
     private String eventCity;
 
     private String eventProvince;
 
     private String organizedBy;
 
     private String organizedProvince;
     
     private String organizedPostCode;
     
     private String organizedCity;
     
     private String organizedStreet;
 
     private String organizedPhone;
 
     private String organizedFax;
 
     private String organizedEmail;
 
     private String organizedWww;
     
     private String organizedId;
 
     private String sourceName;
 
     private String sourceUrl;
 
     private String proposerCredentials;
 
     private String proposerEmail;
 
     private String description;
 
     private String editorialNote;
 
     private Date validityStart;
 
     private Date validityEnd;
 
     private Date eventStart;
 
     private Date eventEnd;
 
     private Set<CategoryResource> availableCategories;
 
     private Set<CategoryResource> selectedCategories;
 
     private List<Resource> attachments;
 
     private List<String> attachmentDescriptions;
 
     private boolean removalRequested;
 
     // validation
     private String validationFailure;
 
     // helper objects
     private static final HTMLEntityEncoder ENCODER = new HTMLEntityEncoder();
 
     private static final HTMLEntityDecoder DECODER = new HTMLEntityDecoder();
 
     private final DateFormat format = DateFormat.getDateTimeInstance();
 
     // origin (node where ProposeDocument screen is embedded)
     private NavigationNodeResource origin;
 
     private boolean addDocumentVisualEditor;
     
     protected Logger logger;
 
     public ProposedDocumentData(Parameters configuration,Logger logger)
     {
         setConfiguration(configuration);
         this.logger = logger;
     }
 
     public ProposedDocumentData(Logger logger)
     {
         this.logger = logger;
         // remember to call setConfiguration later
     }
 
     public void setConfiguration(Parameters configuration)
     {
         calendarTree = configuration.getBoolean("calendar_tree", true);
         inheritCategories = configuration.getBoolean("inherit_categories", true);
 
         attachmentsEnabled = configuration.getBoolean("attachments_enabled", false);
         attachmentsMaxCount = configuration.getInt("attachments_max_count", 0);
         attachmentsMaxSize = configuration.getInt("attachments_max_size", 0);
         attachmentsAllowedFormats = configuration.get("attachments_allowed_formats",
             "jpg gif doc rtf pdf xls");
         attachmentFormatList = Arrays.asList(attachmentsAllowedFormats.toLowerCase().split("\\s+"));
         attachmentDirId = configuration.getLong("attachments_dir_id", -1L);
         addDocumentVisualEditor = configuration.getBoolean("add_document_visual_editor", false);
     }
 
     public void fromParameters(Parameters parameters, CoralSession coralSession)
         throws EntityDoesNotExistException
     {
         name = stripTags(dec(parameters.get("name", "")));
         title = stripTags(dec(parameters.get("title", "")));
         docAbstract = stripTags(dec(parameters.get("abstract", "")));
         content = parameters.get("content", "");
         eventPlace = stripTags(dec(parameters.get("event_place", "")));
         eventProvince = stripTags(dec(parameters.get("event_province", "")));
         eventPostCode = stripTags(dec(parameters.get("event_postcode", "")));
         eventCity = stripTags(dec(parameters.get("event_city", "")));
         eventStreet = stripTags(dec(parameters.get("event_street", "")));
         organizedBy = stripTags(dec(parameters.get("organized_by", "")));
         organizedProvince = stripTags(dec(parameters.get("organized_province", "")));
         organizedPostCode = stripTags(dec(parameters.get("organized_postcode", "")));
         organizedCity = stripTags(dec(parameters.get("organized_city", "")));
         organizedStreet = stripTags(dec(parameters.get("organized_street", "")));
         organizedPhone = stripTags(dec(parameters.get("organized_phone", "")));
         organizedFax = stripTags(dec(parameters.get("organized_fax", "")));
         organizedEmail = stripTags(dec(parameters.get("organized_email", "")));
         organizedWww = stripTags(dec(parameters.get("organized_www", "")));
        organizedId = stripTags(dec(parameters.get("organized_id", "0")));
         sourceName = stripTags(dec(parameters.get("source_name", "")));
         sourceUrl = stripTags(dec(parameters.get("source_url", "")));
         proposerCredentials = stripTags(dec(parameters.get("proposer_credentials", "")));
         proposerEmail = stripTags(dec(parameters.get("proposer_email", "")));
         description = stripTags(dec(parameters.get("description", "")));
         editorialNote = stripTags(dec(parameters.get("editorial_note", "")));
 
         validityStart = getDate(parameters, "validity_start");
         validityEnd = getDate(parameters, "validity_end");
         eventStart = getDate(parameters, "event_start");
         eventEnd = getDate(parameters, "event_end");
 
         selectedCategories = new HashSet<CategoryResource>();
         for (long categoryId : parameters.getLongs("selected_categories"))
         {
             if(categoryId != -1)
             {
                 selectedCategories.add(CategoryResourceImpl.getCategoryResource(coralSession,
                     categoryId));
             }
         }
 
         availableCategories = new HashSet<CategoryResource>();
         for (long categoryId : parameters.getLongs("available_categories"))
         {
             availableCategories.add(CategoryResourceImpl.getCategoryResource(coralSession,
                 categoryId));
         }
 
         if(attachmentsEnabled)
         {
             attachmentDescriptions = new ArrayList<String>(attachmentsMaxCount);
             for (int i = 1; i <= attachmentsMaxCount; i++)
             {
                 attachmentDescriptions.add(stripTags(dec(parameters.get("attachment_description_"
                     + i, ""))));
             }
             attachments = new ArrayList<Resource>(attachmentsMaxCount);
             for (int i = 1; i <= attachmentsMaxCount; i++)
             {
                 long fileId = parameters.getLong("attachment_id_" + i, -1);
                 if(fileId != -1)
                 {
                     attachments.add(FileResourceImpl.getFileResource(coralSession, fileId));
                 }
             }
         }
     }
 
     /**
      * Transfers the data into the templating context.
      * <p>
      * This is needed to keep the exiting templates working
      * </p>
      * 
      * @param templatingContext
      */
     public void toTemplatingContext(TemplatingContext templatingContext)
     {
         templatingContext.put("name", enc(name));
         templatingContext.put("title", enc(title));
         templatingContext.put("abstract", enc(docAbstract));
         templatingContext.put("content", enc(content));
         templatingContext.put("event_place", enc(eventPlace));
         templatingContext.put("event_province", enc(eventProvince));
         templatingContext.put("event_postcode", enc(eventPostCode));
         templatingContext.put("event_city", enc(eventCity));
         templatingContext.put("event_street", enc(eventStreet));
         templatingContext.put("organized_by", enc(organizedBy));
         templatingContext.put("organized_province", enc(organizedProvince));
         templatingContext.put("organized_postcode", enc(organizedPostCode));
         templatingContext.put("organized_city", enc(organizedCity));
         templatingContext.put("organized_street", enc(organizedStreet));
         templatingContext.put("organized_phone", enc(organizedPhone));
         templatingContext.put("organized_fax", enc(organizedFax));
         templatingContext.put("organized_email", enc(organizedEmail));
         templatingContext.put("organized_www", enc(organizedWww));
         templatingContext.put("organized_id", enc(organizedId));
         templatingContext.put("source_name", enc(sourceName));
         templatingContext.put("source_url", enc(sourceUrl));
         templatingContext.put("proposer_credentials", enc(proposerCredentials));
         templatingContext.put("proposer_email", enc(proposerEmail));
         templatingContext.put("description", enc(description));
         setDate(templatingContext, "validity_start", validityStart);
         setDate(templatingContext, "validity_end", validityEnd);
         setDate(templatingContext, "event_start", eventStart);
         setDate(templatingContext, "event_end", eventEnd);
         templatingContext.put("selected_categories", selectedCategories);
         if(attachmentsEnabled)
         {
             templatingContext.put("attachments_enabled", attachmentsEnabled);
             templatingContext.put("attachments_max_count", attachmentsMaxCount);
             int remaining = attachmentsMaxCount - attachments.size();
             remaining = remaining >= 0 ? remaining : 0;
             templatingContext.put("attachments_remaining_count", remaining);
             templatingContext.put("attachments_max_size", attachmentsMaxSize);
             templatingContext.put("attachments_allowed_formats", attachmentsAllowedFormats);
             templatingContext.put("current_attachments", attachments);
             // fill up with empty strings to make template logic more simple
             while(attachmentDescriptions.size() < attachmentsMaxCount)
             {
                 attachmentDescriptions.add("");
             }
             templatingContext.put("attachment_descriptions", enc(attachmentDescriptions));
         }
         templatingContext.put("editorial_note", enc(editorialNote));
         templatingContext.put("add_document_visual_editor", addDocumentVisualEditor);
     }
 
     public void fromNode(DocumentNodeResource node, CategoryService categoryService,
         RelatedService relatedService, CoralSession coralSession)
     {
         // calendarTree
         // inheritCategories
         name = stripTags(node.getName());
         title = stripTags(node.getTitle());
         docAbstract = stripTags(node.getAbstract());
         content = node.getContent();
         description = stripTags(node.getDescription());
         validityStart = node.getValidityStart();
         validityEnd = node.getValidityEnd();
         eventPlace = stripTags(node.getEventPlace());
         eventStart = node.getEventStart();
         eventEnd = node.getEventEnd();
         try
         {
             Document metaDom = textToDom4j(node.getMeta());
             eventProvince = stripTags(selectFirstText(metaDom, "/meta/event/address/province"));
             eventPostCode = stripTags(selectFirstText(metaDom, "/meta/event/address/postcode"));
             eventCity = stripTags(selectFirstText(metaDom, "/meta/event/address/city"));
             eventStreet = stripTags(selectFirstText(metaDom, "/meta/event/address/street"));
             organizedBy = stripTags(selectFirstText(metaDom, "/meta/organisation/name"));
             organizedProvince = stripTags(selectFirstText(metaDom, "/meta/organisation/address/province"));
             organizedPostCode = stripTags(selectFirstText(metaDom, "/meta/organisation/address/postcode"));
             organizedCity = stripTags(selectFirstText(metaDom, "/meta/organisation/address/city"));
             organizedStreet = stripTags(selectFirstText(metaDom, "/meta/organisation/address/street"));
             organizedPhone = stripTags(selectFirstText(metaDom, "/meta/organisation/tel"));
             organizedFax = stripTags(selectFirstText(metaDom, "/meta/organisation/fax"));
             organizedEmail = stripTags(selectFirstText(metaDom, "/meta/organisation/e-mail"));
             organizedWww = stripTags(selectFirstText(metaDom, "/meta/organisation/url"));
             organizedId = stripTags(selectFirstText(metaDom, "/meta/organisation/id"));
             sourceName = stripTags(selectFirstText(metaDom, "/meta/sources/source/name"));
             sourceUrl = stripTags(selectFirstText(metaDom, "/meta/sources/source/url"));
             proposerCredentials = stripTags(selectFirstText(metaDom, "/meta/authors/author/name"));
             proposerEmail = stripTags(selectFirstText(metaDom, "/meta/authors/author/e-mail"));
         }
         catch(HTMLException e)
         {
             throw new RuntimeException("malformed metadada in resource " + node.getIdString(), e);
         }
         selectedCategories = new HashSet<CategoryResource>(Arrays.asList(categoryService
             .getCategories(coralSession, node, false)));
         if(attachmentsEnabled)
         {
             List<Resource> resources = new ArrayList<Resource>(Arrays.asList(relatedService
                 .getRelatedTo(coralSession, node, node.getRelatedResourcesSequence(), null)));
 
             attachments = new ArrayList<Resource>(attachmentsMaxCount);
             attachmentDescriptions = new ArrayList<String>(attachmentsMaxCount);
             if(node.isThumbnailDefined())
             {
                 attachments.add(node.getThumbnail());
                 attachmentDescriptions.add(stripTags(node.getThumbnail().getDescription()));
             }
             for (Resource attachment : resources)
             {
                 if(attachment instanceof FileResource)
                 {
                     attachments.add(attachment);
                     attachmentDescriptions.add(stripTags(((CmsNodeResource)attachment)
                         .getDescription()));
                 }
             }
         }
     }
 
     public void toNode(DocumentNodeResource node)
     {
         // set attributes to new node
         node.setDescription(enc(description));
         if(addDocumentVisualEditor)
         {
             node.setContent(content);
         }
         else
         {
             node.setContent(makePara(stripTags(content)));
         }
         node.setAbstract(enc(docAbstract));
         node.setValidityStart(validityStart);
         node.setValidityEnd(validityEnd);
         node.setEventStart(eventStart);
         node.setEventEnd(eventEnd);
         node.setEventPlace(enc(eventPlace));
         Document doc = doc(getMetaElm());
         node.setMeta(dom4jToText(doc));
 
     }
 
     private Element getMetaElm()
     {
         return elm("meta", elm("authors", elm("author", elm("name", enc(proposerCredentials)), elm(
             "e-mail", enc(proposerEmail)))), elm("sources", elm("source", elm("name",
             enc(sourceName)), elm("url", enc(sourceUrl)))), elm("editor"), elm("event", elm(
             "address", elm("street", enc(eventStreet)), elm("postcode", enc(eventPostCode)), elm(
                 "city", enc(eventCity)), elm("province", enc(eventProvince)))), elm("organisation",
             elm("name", enc(organizedBy)), elm("address", elm("street", enc(eventStreet)), elm(
                 "postcode", enc(eventPostCode)), elm("city", enc(eventCity)), elm("province",
                 enc(eventProvince))), elm("tel", enc(organizedPhone)),
             elm("fax", enc(organizedFax)), elm("e-mail", enc(organizedEmail)), elm("url",
                 enc(organizedWww)), elm("id", enc(organizedId))));
     }
 
     public void fromProposal(DocumentNodeResource node, CoralSession coralSession)
     {
         try
         {
             Document proposalDom = textToDom4j(node.getProposedContent());
             name = dec(selectFirstText(proposalDom, "/document/name"));
             title = dec(selectFirstText(proposalDom, "/document/title"));
             docAbstract = dec(selectFirstText(proposalDom, "/document/abstract"));
             // DECODE HTML
             content = dec(selectFirstText(proposalDom, "/document/content"));
             description = dec(selectFirstText(proposalDom, "/document/description"));
             validityStart = text2date(dec(selectFirstText(proposalDom, "/document/validity/start")));
             validityEnd = text2date(dec(selectFirstText(proposalDom, "/document/validity/end")));
             eventPlace = dec(selectFirstText(proposalDom, "/document/event/place"));
             eventProvince = dec(selectFirstText(proposalDom,"/document/meta/event/address/province"));
             eventPostCode = dec(selectFirstText(proposalDom,"/document/meta/event/address/postcode"));
             eventCity = dec(selectFirstText(proposalDom,"/document/meta/event/address/city"));
             eventStreet = dec(selectFirstText(proposalDom,"/document/meta/event/address/street"));
             eventStart = text2date(dec(selectFirstText(proposalDom, "/document/event/start")));
             eventEnd = text2date(dec(selectFirstText(proposalDom, "/document/event/end")));
             organizedBy = dec(selectFirstText(proposalDom, "/document/meta/organisation/name"));
             organizedProvince = dec(selectFirstText(proposalDom,"/document/meta/organisation/address/province"));
             organizedPostCode = dec(selectFirstText(proposalDom,"/document/meta/organisation/address/postcode"));
             organizedCity = dec(selectFirstText(proposalDom,"/document/meta/organisation/address/city"));
             organizedStreet = dec(selectFirstText(proposalDom,"/document/meta/organisation/address/street"));
             organizedPhone = dec(selectFirstText(proposalDom, "/document/meta/organisation/tel"));
             organizedFax = dec(selectFirstText(proposalDom, "/document/meta/organisation/fax"));
             organizedEmail = dec(selectFirstText(proposalDom, "/document/meta/organisation/e-mail"));
             organizedWww = dec(selectFirstText(proposalDom, "/document/meta/organisation/url"));
             organizedId = dec(selectFirstText(proposalDom, "/document/meta/organisation/id"));
             sourceName = dec(selectFirstText(proposalDom, "/document/meta/sources/source/name"));
             sourceUrl = dec(selectFirstText(proposalDom, "/document/meta/sources/source/url"));
             proposerCredentials = dec(selectFirstText(proposalDom,
                 "/document/meta/authors/author/name"));
             proposerEmail = dec(selectFirstText(proposalDom, "/document/meta/authors/author/e-mail"));
             selectedCategories = new HashSet<CategoryResource>();
             for (Element categoryNode : (List<Element>)proposalDom
                 .selectNodes("/document/categories/category/ref"))
             {
                 long categoryId = Long.parseLong(categoryNode.getTextTrim());
                 try
                 {
                     selectedCategories.add(CategoryResourceImpl.getCategoryResource(coralSession,
                         categoryId));
                 }
                 catch(EntityDoesNotExistException e)
                 {
                     logger.error("Category resource " + categoryId + " assigned to document node #"
                         + node.getId() + " error. " + e.getMessage());
                 }
             }
             attachments = new ArrayList<Resource>();
             attachmentDescriptions = new ArrayList<String>();
             for (Element attachmentNode : (List<Element>)proposalDom
                 .selectNodes("/document/attachments/attachment"))
             {
                 long fileId = Long.parseLong(attachmentNode.elementTextTrim("ref"));
                 try
                 {
                     attachments.add(FileResourceImpl.getFileResource(coralSession, fileId));
                     attachmentDescriptions.add(dec(attachmentNode.elementText("description")));
                 }
                 catch(EntityDoesNotExistException e)
                 {
                     logger.error("File resource #" + fileId + " attached to document node #"
                         + node.getId() + " error. " + e.getMessage());
                 }
             }
             removalRequested = selectFirstText(proposalDom, "/document/request").equals("remove");
             long originId = Long.parseLong(selectFirstText(proposalDom, "/document/origin/ref"));
             origin = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession, originId);
             editorialNote = dec(selectFirstText(proposalDom, "/document/editorial/note"));
         }
         catch(HTMLException e)
         {
             throw new RuntimeException("malformed proposed changes descriptor", e);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new RuntimeException("invalid resource id in proposed changes descriptor", e);
         }
     }
 
     public void toProposal(DocumentNodeResource node)
     {
         Element categoriesElm = elm("categories");
         for (CategoryResource category : selectedCategories)
         {
             categoriesElm.add(elm("category", elm("ref", category.getIdString())));
         }
         Element attachmentsElm = elm("attachments");
         if(attachmentsEnabled)
         {
             Iterator<Resource> attachmentIterator = attachments.iterator();
             Iterator<String> descriptionIterator = attachmentDescriptions.iterator();
             while(attachmentIterator.hasNext())
             {
                 attachmentsElm.add(elm("attachment", elm("ref", attachmentIterator.next()
                     .getIdString()), elm("description", descriptionIterator.next())));
             }
         }
         Document doc = doc(elm("document", elm("request", removalRequested ? "remove" : "update"),
             elm("origin", elm("ref", origin.getIdString())), elm("name", enc(name)), elm("title",
                 enc(title)), elm("abstract", enc(docAbstract)), elm("content", cdata(content)),
             elm("description", enc(description)),
             elm("editorial", elm("note", enc(editorialNote))), elm("validity", elm("start",
                 date2text(validityStart)), elm("end", date2text(validityEnd))), elm("event", elm(
                 "place", enc(eventPlace)), elm("start", date2text(eventStart)), elm("end",
                 date2text(eventEnd))), getMetaElm(), categoriesElm, attachmentsElm));
         node.setProposedContent(dom4jToText(doc));
     }
 
     private static Date text2date(String text)
     {
         if(text.equals("undefined"))
         {
             return null;
         }
         else
         {
             return new Date(Long.parseLong(text));
         }
     }
 
     private static String date2text(Date date)
     {
         if(date == null)
         {
             return "undefined";
         }
         else
         {
             return Long.toString(date.getTime());
         }
     }
 
     // validation
 
     public boolean isValid(HTMLService htmlService)
     {
         if(name.equals(""))
         {
             setValidationFailure("navi_name_empty");
             return false;
         }
         if(title.equals(""))
         {
             setValidationFailure("navi_title_empty");
             return false;
         }
         if(proposerCredentials.equals(""))
         {
             setValidationFailure("proposer_credentials_empty");
             return false;
         }
         try
         {
             StringWriter errorWriter = new StringWriter();
             Document contentDom = htmlService.textToDom4j(content, errorWriter, "proposeDocument");
             if(contentDom == null)
             {
                 setValidationFailure("invalid_html");
                 return false;
             }
             else
             {
                 htmlService.collapseSubsequentBreaksInParas(contentDom);
                 htmlService.trimBreaksFromParas(contentDom);
                 htmlService.removeEmptyParas(contentDom);
                 StringWriter contentWriter = new StringWriter();
                 htmlService.dom4jToText(contentDom, contentWriter, true);
                 content = contentWriter.toString();
             }
         }
         catch(HTMLException e)
         {
             setValidationFailure("invalid_html");
             return false;
         }
         return true;
     }
 
     public boolean isFileUploadValid(CoralSession coralSession, FileUpload fileUpload)
         throws ProcessingException
     {
         boolean valid = true;
         if(attachmentsEnabled)
         {
             // check if attachment_dir_id is configured, points to a directory, and user has write
             // rights
             try
             {
                 DirectoryResource dir = DirectoryResourceImpl.getDirectoryResource(coralSession,
                     attachmentDirId);
                 if(!dir.canAddChild(coralSession, coralSession.getUserSubject()))
                 {
                     validationFailure = "attachment_dir_misconfigured";
                     valid = false;
                 }
             }
             catch(Exception e)
             {
                 validationFailure = "attachment_dir_misconfigured";
                 valid = false;
             }
             if(valid)
             {
                 fileCheck: for (int i = attachments.size(); i < attachmentsMaxCount; i++)
                 {
                     try
                     {
                         UploadContainer uploadedFile = getAttachmentContainer(i, fileUpload);
                         if(uploadedFile != null)
                         {
                             if(uploadedFile.getSize() > attachmentsMaxSize * 1024)
                             {
                                 validationFailure = "attachment_size_exceeded";
                                 valid = false;
                                 break fileCheck;
                             }
                             String fileName = uploadedFile.getFileName();
                             String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)
                                 .trim().toLowerCase();
                             if(!attachmentFormatList.contains(fileExt))
                             {
                                 validationFailure = "attachment_type_not_allowed";
                                 valid = false;
                                 break fileCheck;
                             }
                         }
                     }
                     catch(UploadLimitExceededException e)
                     {
                         validationFailure = "upload_size_exceeded"; // i18n
                         valid = false;
                         break fileCheck;
                     }
                 }
             }
         }
         return valid;
     }
 
     // getters for configuration
 
     public boolean isAttachmentsEnabled()
     {
         return attachmentsEnabled;
     }
 
     public int getAttachmentsMaxCount()
     {
         return attachmentsMaxCount;
     }
 
     // getters
 
     public String getName()
     {
         return name;
     }
 
     public String getTitle()
     {
         return title;
     }
 
     public String getAbstract()
     {
         return docAbstract;
     }
 
     public String getContent()
     {
         return content;
     }
 
     public String getEventPlace()
     {
         return eventPlace;
     }
     
     public String getEventProvince()
     {
         return eventProvince;
     }
     
     public String getEventPostCode()
     {
         return eventPostCode;
     }
     
     public String getEventCity()
     {
         return eventCity;
     }
     
     public String getEventStreet()
     {
         return eventStreet;
     }
 
     public Date getEventStart()
     {
         return eventStart;
     }
 
     public Date getEventEnd()
     {
         return eventEnd;
     }
 
     public Date getValidityStart()
     {
         return validityStart;
     }
 
     public Date getValidityEnd()
     {
         return validityEnd;
     }
 
     public String getOrganizedBy()
     {
         return organizedBy;
     }
 
     public String getOrganizedProvince()
     {
         return organizedProvince;
     }
     
     public String getOrganizedPostCode()
     {
         return organizedPostCode;
     }
     
     public String getOrganizedCity()
     {
         return organizedCity;
     }
     
     public String getOrganizedStreet()
     {
         return organizedStreet;
     }
 
     public String getOrganizedPhone()
     {
         return organizedPhone;
     }
 
     public String getOrganizedFax()
     {
         return organizedFax;
     }
 
     public String getOrganizedEmail()
     {
         return organizedEmail;
     }
 
     public String getOrganizedWww()
     {
         return organizedWww;
     }
     
     public String getOrganizedId()
     {
         return organizedId;
     }
 
     public String getSourceName()
     {
         return sourceName;
     }
 
     public String getSourceUrl()
     {
         return sourceUrl;
     }
 
     public String getProposerCredentials()
     {
         return proposerCredentials;
     }
 
     public String getProposerEmail()
     {
         return proposerEmail;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public String getEditorialNote()
     {
         return editorialNote;
     }
 
     public boolean isCalendarTree()
     {
         return calendarTree;
     }
 
     public boolean isInheritCategories()
     {
         return inheritCategories;
     }
 
     public Set<CategoryResource> getSelectedCategories()
     {
         return selectedCategories;
     }
 
     public Set<CategoryResource> getAvailableCategories()
     {
         return availableCategories;
     }
 
     // attachments
 
     public DirectoryResource getAttachmenDirectory(CoralSession coralSession)
         throws EntityDoesNotExistException
     {
         return DirectoryResourceImpl.getDirectoryResource(coralSession, attachmentDirId);
     }
 
     public String getAttachmentDescription(int index)
     {
         if(index >= 0 && index < attachmentDescriptions.size())
         {
             return attachmentDescriptions.get(index);
         }
         else
         {
             return "";
         }
     }
 
     public String getAttachmentDescription(Resource file)
     {
         return getAttachmentDescription(attachments.indexOf(file));
     }
 
     public List<String> getAttachmentDescriptions()
     {
         return attachmentDescriptions;
     }
 
     public UploadContainer getAttachmentContainer(int index, FileUpload fileUpload)
         throws UploadLimitExceededException
     {
         return fileUpload.getContainer("attachment_" + (index + 1));
     }
 
     public List<Resource> getAttachments()
     {
         return attachments;
     }
 
     public void setTitle(String title)
     {
         this.title = title;
     }
 
     public void setDocAbstract(String docAbstract)
     {
         this.docAbstract = docAbstract;
     }
 
     public void setContent(String content)
     {
         this.content = content;
     }
 
     public void setEventPlace(String eventPlace)
     {
         this.eventPlace = eventPlace;
     }
     
     public void setEventProvince(String eventProvince)
     {
         this.eventProvince = eventProvince;
     }
     
     public void setEventPostCode(String eventPostCode)
     {
         this.eventPostCode = eventPostCode;
     }
     
     public void setEventCity(String eventCity)
     {
         this.eventCity = eventCity;
     }
     
     public void setEventStreet(String eventStreet)
     {
         this.eventStreet = eventStreet;
     }
 
     public void setOrganizedBy(String organizedBy)
     {
         this.organizedBy = organizedBy;
     }
 
     public void setOrganizedProvince(String organizedProvince)
     {
         this.organizedProvince = organizedProvince;
     }
     
     public void setOrganizedPostCode(String organizedPostCode)
     {
         this.organizedPostCode = organizedPostCode;
     }
     
     public void setOrganizedCity(String organizedCity)
     {
         this.organizedCity = organizedCity;
     }
     
     public void setOrganizedStreet(String organizedStreet)
     {
         this.organizedStreet = organizedStreet;
     }
 
     public void setOrganizedPhone(String organizedPhone)
     {
         this.organizedPhone = organizedPhone;
     }
 
     public void setOrganizedFax(String organizedFax)
     {
         this.organizedFax = organizedFax;
     }
 
     public void setOrganizedEmail(String organizedEmail)
     {
         this.organizedEmail = organizedEmail;
     }
 
     public void setOrganizedWww(String organizedWww)
     {
         this.organizedWww = organizedWww;
     }
     
     public void setOrganizedId(String organizedId)
     {
         this.organizedId = organizedId;
     }
 
     public void setSourceName(String sourceName)
     {
         this.sourceName = sourceName;
     }
 
     public void setSourceUrl(String sourceUrl)
     {
         this.sourceUrl = sourceUrl;
     }
 
     public void setProposerCredentials(String proposerCredentials)
     {
         this.proposerCredentials = proposerCredentials;
     }
 
     public void setProposerEmail(String proposerEmail)
     {
         this.proposerEmail = proposerEmail;
     }
 
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     public void setValidityStart(Date validityStart)
     {
         this.validityStart = validityStart;
     }
 
     public void setValidityEnd(Date validityEnd)
     {
         this.validityEnd = validityEnd;
     }
 
     public void setEventStart(Date eventStart)
     {
         this.eventStart = eventStart;
     }
 
     public void setEventEnd(Date eventEnd)
     {
         this.eventEnd = eventEnd;
     }
 
     public void setSelectedCategories(Set<CategoryResource> selectedCategories)
     {
         this.selectedCategories = selectedCategories;
     }
 
     public void setAttachments(List<Resource> attachments)
     {
         this.attachments = new ArrayList<Resource>(attachmentsMaxCount);
         attachmentDescriptions = new ArrayList<String>(attachmentsMaxCount);
         for (Resource attachment : attachments)
         {
             if(attachment instanceof FileResource)
             {
                 this.attachments.add(attachment);
                 attachmentDescriptions.add(((CmsNodeResource)attachment).getDescription());
             }
         }
     }
 
     public void addAttachment(FileResource file)
     {
         attachments.add(file);
         attachmentDescriptions.add(file.getDescription());
     }
 
     public FileResource removeAttachment(long fileId, CoralSession coralSession)
         throws EntityDoesNotExistException
     {
         FileResource file = FileResourceImpl.getFileResource(coralSession, fileId);
         int index = attachments.indexOf(file);
         attachments.remove(index);
         attachmentDescriptions.remove(index);
         return file;
     }
 
     public boolean isRemovalRequested()
     {
         return removalRequested;
     }
 
     public void setRemovalRequested(boolean removalRequested)
     {
         this.removalRequested = removalRequested;
     }
 
     public NavigationNodeResource getOrigin()
     {
         return origin;
     }
 
     public void setOrigin(NavigationNodeResource origin)
     {
         this.origin = origin;
     }
 
     public void setEditorialNote(String editorialNote)
     {
         this.editorialNote = editorialNote;
     }
 
     // utitily
 
     public void setValidationFailure(String validationFailure)
     {
         this.validationFailure = validationFailure;
     }
 
     public String getValidationFailure()
     {
         return validationFailure;
     }
 
     private Date getDate(Parameters parameters, String key)
     {
         if(parameters.isDefined(key) && parameters.get(key).trim().length() > 0)
         {
             return parameters.getDate(key);
         }
         else
         {
             return null;
         }
     }
 
     /**
      * Filters document content according to 'proposeDocument' cleanup profile. This method is
      * called when creating change proposal from document to avoid showing document author any
      * markup they could not edit using the restricted editor.
      * 
      * @param htmlService HTML Service.
      */
     public static String cleanupContent(String content, HTMLService htmlService)
         throws ProcessingException
     {
         if(content == null || content.trim().length() == 0)
         {
             return "";
         }
         try
         {
             StringWriter errorWriter = new StringWriter();
             Document contentDom = htmlService.textToDom4j(content, errorWriter, "proposeDocument");
             if(contentDom == null)
             {
                 throw new ProcessingException("HTML processing failure");
             }
             else
             {   
                 htmlService.collapseSubsequentBreaksInParas(contentDom);
                 htmlService.trimBreaksFromParas(contentDom);
                 htmlService.removeEmptyParas(contentDom);
                 StringWriter contentWriter = new StringWriter();
                 htmlService.dom4jToText(contentDom, contentWriter, true);
                 return contentWriter.toString();
             }
         }
         catch(HTMLException e)
         {
             throw new ProcessingException("HTML processing failure", e);
         }
 
     }
 
     public void cleanupContent(HTMLService htmlService)
         throws ProcessingException
     {
         content = cleanupContent(content, htmlService);
     }
 
     private void setDate(TemplatingContext templatingContext, String key, Date value)
     {
         if(value != null)
         {
             templatingContext.put(key, value.getTime());
         }
     }
 
     private String formatDate(Date date)
     {
         if(date != null)
         {
             return format.format(date);
         }
         else
         {
             return "Undefined";
         }
     }
 
     /**
      * Strips HTML tags from the input string.
      */
     public static String stripTags(String s)
     {
         return s == null ? s : s.replaceAll("<[^>]*?>", " ");
     }
 
     /**
      * Converts newline into HTML paragraphs.
      */
     public static String makePara(String content)
     {
         content = content.replaceAll("\r\n", "\n");
         content = content.replaceAll("\n+", "</p>\n<p>");
         content = "<p>" + content + "</p>";
         content = content.replaceAll("<p>\\s*</p>", "");
         return content;
     }
 
     private String enc(String s)
     {
         return ENCODER.encodeAttribute(s, "UTF-16");
     }
 
     private List<String> enc(List<String> l)
     {
         List<String> result = new ArrayList<String>(l.size());
         for (String s : l)
         {
             result.add(enc(s));
         }
         return l;
     }
 
     private String dec(String s)
     {
         return DECODER.decode(s);
     }
 
     public static String getAttachmentName(String fileName)
     {
         StringBuilder buff = new StringBuilder();
         SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
         buff.append(df.format(new Date())); // timestamp
         buff.append("_"); // separator
         fileName = StringUtils.iso1toUtf8(fileName);
         fileName = StringUtils.unaccentLatinChars(fileName); // unaccent accented latin characters
         fileName = fileName.replaceAll("[^A-Za-z0-9-_.]+", "_"); // squash everything except
         // alphanumerics and allowed
         // punctuation
         fileName = fileName.replaceAll("_{2,}", "_"); // contract sequences of multiple _
         buff.append(fileName);
         return buff.toString();
     }
 
     public void logProposal(Logger logger, DocumentNodeResource node)
     {
         // build proposals log
         StringBuilder proposalsDump = new StringBuilder();
         proposalsDump.append("----------------------------------\n");
         proposalsDump.append("Document id: ").append(node.getIdString()).append("\n");
         proposalsDump.append("Document path: ").append(node.getPath()).append("\n");
         proposalsDump.append("Created: ").append(node.getCreationTime()).append("\n");
         proposalsDump.append("Created by: ").append(node.getCreatedBy().getName()).append("\n");
         proposalsDump.append("Document title: ").append(title).append("\n");        
         
         proposalsDump.append("Event Place: ").append(eventPlace).append("\n");
         proposalsDump.append("Event Province: ").append(eventProvince).append("\n");
         proposalsDump.append("Event Post Code: ").append(eventPostCode).append("\n");
         proposalsDump.append("Event City: ").append(eventCity).append("\n");
         proposalsDump.append("Event Street: ").append(eventStreet).append("\n");
         proposalsDump.append("Event start: ").append(formatDate(eventStart)).append("\n");
         proposalsDump.append("Event end: ").append(formatDate(eventEnd)).append("\n");
         proposalsDump.append("Document validity start: ").append(formatDate(validityStart)).append(
             "\n");
         proposalsDump.append("Document validity end: ").append(formatDate(validityEnd))
             .append("\n");
         proposalsDump.append("Organized by: ").append(organizedBy).append("\n");
         proposalsDump.append("Organizer Province: ").append(organizedProvince).append("\n");
         proposalsDump.append("Organizer Post Code: ").append(organizedPostCode).append("\n");
         proposalsDump.append("Organizer City: ").append(organizedCity).append("\n");
         proposalsDump.append("Organizer Street: ").append(organizedStreet).append("\n");
         proposalsDump.append("Organizer phone: ").append(organizedPhone).append("\n");
         proposalsDump.append("Organizer fax: ").append(organizedFax).append("\n");
         proposalsDump.append("Organizer email: ").append(organizedEmail).append("\n");
         proposalsDump.append("Organizer URL: ").append(organizedWww).append("\n");
         proposalsDump.append("Organizer Id: ").append(organizedId).append("\n");
         proposalsDump.append("Source name: ").append(sourceName).append("\n");
         proposalsDump.append("Source URL: ").append(sourceUrl).append("\n");
         proposalsDump.append("Proposer credentials: ").append(proposerCredentials).append("\n");
         proposalsDump.append("Proposer email: ").append(proposerEmail).append("\n");
         proposalsDump.append("Administrative description: ").append(proposerEmail).append("\n");
         proposalsDump.append("Content: \n").append(content).append("\n");
         logger.debug(proposalsDump.toString());
     }
 }
