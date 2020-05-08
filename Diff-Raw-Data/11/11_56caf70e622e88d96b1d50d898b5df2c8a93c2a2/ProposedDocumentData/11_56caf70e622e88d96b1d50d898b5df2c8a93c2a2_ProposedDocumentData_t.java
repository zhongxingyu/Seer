 package net.cyklotron.cms.structure.internal;
 
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.cdata;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.doc;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.dom4jToText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.elm;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.selectFirstText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.textToDom4j;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.ImagingOpException;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.imageio.IIOException;
 import javax.imageio.ImageIO;
 
 import org.apache.activemq.util.ByteArrayInputStream;
 import org.apache.tika.io.IOUtils;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.imgscalr.Scalr;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.html.HTMLException;
 import org.objectledge.html.HTMLService;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.upload.UploadBucket;
 import org.objectledge.upload.UploadContainer;
 import org.objectledge.upload.UploadLimitExceededException;
 import org.objectledge.utils.StringUtils;
 
 import net.cyklotron.cms.CmsNodeResource;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryResourceImpl;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentMetadataHelper;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.PreferredImageSizes;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.DirectoryResourceImpl;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FileResourceImpl;
 import net.cyklotron.cms.files.FilesService;
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
 
     private boolean attachmentsMultiUpload;
 
     private int attachmentsThumbnailSize;
 
     private String uploadBucketId;
 
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
 
     private List<OrganizationData> organizations;
 
     private Set<CategoryResource> availableCategories;
 
     private Set<CategoryResource> selectedCategories;
 
     private List<Resource> attachments;
 
     private List<String> attachmentNames;
 
     private List<String> attachmentTypes;
 
     private List<byte[]> attachmentContents;
 
     private List<String> attachmentDescriptions;
 
     private boolean removalRequested;
 
     // validation
     private String validationFailure;
 
     private final DateFormat format = DateFormat.getDateTimeInstance();
 
     // origin (node where ProposeDocument screen is embedded)
     private NavigationNodeResource origin;
 
     private boolean addDocumentVisualEditor;
     
     private boolean clearOrganizationIfNotMatch;
 
     private String cleanupProfile;
 
     private int imageMaxSize;
 
     private static final String DEFAULT_CLENAUP_PROFILE = "proposeDocument";
 
     protected Logger logger;
 
     public ProposedDocumentData(Parameters configuration, PreferredImageSizes imageSizes,
         Logger logger)
     {
         setConfiguration(configuration, imageSizes);
         this.logger = logger;
     }
 
     public ProposedDocumentData(Logger logger)
     {
         this.logger = logger;
         // remember to call setConfiguration later
     }
 
     public void setConfiguration(Parameters configuration, PreferredImageSizes imageSizes)
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
         attachmentsMultiUpload = configuration.getBoolean("attachments_multi_upload", false);
         attachmentsThumbnailSize = configuration.getInt("attachments_thumbnails_size", 64);
         addDocumentVisualEditor = configuration.getBoolean("add_document_visual_editor", false);
         clearOrganizationIfNotMatch = configuration.getBoolean("clear_org_if_not_match", false);
         cleanupProfile = configuration.get("cleanup_profile", DEFAULT_CLENAUP_PROFILE);
         imageMaxSize = configuration.getInt("attachemnt_images_max_size", imageSizes.getLarge());
     }
 
     public void fromParameters(Parameters parameters, CoralSession coralSession)
         throws EntityDoesNotExistException
     {
         name = stripTags(DocumentMetadataHelper.dec(parameters.get("name", "")));
         title = stripTags(DocumentMetadataHelper.dec(parameters.get("title", "")));
         docAbstract = stripTags(DocumentMetadataHelper.dec(parameters.get("abstract", "")));
         content = parameters.get("content", "");
         eventPlace = stripTags(DocumentMetadataHelper.dec(parameters.get("event_place", "")));
         eventProvince = stripTags(DocumentMetadataHelper.dec(parameters.get("event_province", "")));
         eventPostCode = stripTags(DocumentMetadataHelper.dec(parameters.get("event_postCode", "")));
         eventCity = stripTags(DocumentMetadataHelper.dec(parameters.get("event_city", "")));
         eventStreet = stripTags(DocumentMetadataHelper.dec(parameters.get("event_street", "")));
         organizations = OrganizationData.fromParameters(parameters);
         sourceName = stripTags(DocumentMetadataHelper.dec(parameters.get("source_name", "")));
         sourceUrl = stripTags(DocumentMetadataHelper.dec(parameters.get("source_url", "")));
         proposerCredentials = stripTags(DocumentMetadataHelper.dec(parameters.get("proposer_credentials", "")));
         proposerEmail = stripTags(DocumentMetadataHelper.dec(parameters.get("proposer_email", "")));
         description = stripTags(DocumentMetadataHelper.dec(parameters.get("description", "")));
         editorialNote = stripTags(DocumentMetadataHelper.dec(parameters.get("editorial_note", "")));
 
         validityStart = getDate(parameters, "validity_start");
         validityEnd = getDate(parameters, "validity_end");
         eventStart = getDate(parameters, "event_start");
         eventEnd = getDate(parameters, "event_end");
 
         selectedCategories = new HashSet<CategoryResource>();
         for(long categoryId : parameters.getLongs("selected_categories"))
         {
             if(categoryId != -1)
             {
                 selectedCategories.add(CategoryResourceImpl.getCategoryResource(coralSession,
                     categoryId));
             }
         }
 
         availableCategories = new HashSet<CategoryResource>();
         for(long categoryId : parameters.getLongs("available_categories"))
         {
             availableCategories.add(CategoryResourceImpl.getCategoryResource(coralSession,
                 categoryId));
         }
 
         if(attachmentsEnabled)
         {
             attachmentDescriptions = new ArrayList<String>(attachmentsMaxCount);
             for(int i = 1; i <= attachmentsMaxCount; i++)
             {
                 attachmentDescriptions.add(stripTags(DocumentMetadataHelper.dec(parameters.get("attachment_description_"
                     + i, ""))));
             }
             attachments = new ArrayList<Resource>(attachmentsMaxCount);
             for(int i = 1; i <= attachmentsMaxCount; i++)
             {
                 long fileId = parameters.getLong("attachment_id_" + i, -1);
                 if(fileId != -1)
                 {
                     attachments.add(FileResourceImpl.getFileResource(coralSession, fileId));
                 }
             }
             uploadBucketId = parameters.get("upload_bucket_id", "");
             attachmentContents = new ArrayList<>(attachmentsMaxCount);
             attachmentTypes = new ArrayList<>(attachmentsMaxCount);
             attachmentNames = new ArrayList<>(attachmentsMaxCount);
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
         templatingContext.put("name", DocumentMetadataHelper.enc(name));
         templatingContext.put("title", DocumentMetadataHelper.enc(title));
         templatingContext.put("abstract", DocumentMetadataHelper.enc(docAbstract));
         templatingContext.put("content", DocumentMetadataHelper.enc(content));
         templatingContext.put("event_place", DocumentMetadataHelper.enc(eventPlace));
         templatingContext.put("event_province", DocumentMetadataHelper.enc(eventProvince));
         templatingContext.put("event_postCode", DocumentMetadataHelper.enc(eventPostCode));
         templatingContext.put("event_city", DocumentMetadataHelper.enc(eventCity));
         templatingContext.put("event_street", DocumentMetadataHelper.enc(eventStreet));
         OrganizationData.toTemplatingContext(organizations, templatingContext);
         templatingContext.put("source_name", DocumentMetadataHelper.enc(sourceName));
         templatingContext.put("source_url", DocumentMetadataHelper.enc(sourceUrl));
         templatingContext.put("proposer_credentials", DocumentMetadataHelper.enc(proposerCredentials));
         templatingContext.put("proposer_email", DocumentMetadataHelper.enc(proposerEmail));
         templatingContext.put("description", DocumentMetadataHelper.enc(description));
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
             templatingContext.put("attachments_multi_upload", attachmentsMultiUpload);
             templatingContext.put("attachments_thumbnails_size", attachmentsThumbnailSize);
             templatingContext.put("current_attachments", attachments);
             // fill up with empty strings to make template logic more simple
             while(attachmentDescriptions.size() < attachmentsMaxCount)
             {
                 attachmentDescriptions.add("");
             }
             templatingContext.put("attachment_descriptions", DocumentMetadataHelper.enc(attachmentDescriptions));
             templatingContext.put("upload_bucket_id", uploadBucketId);
         }
         templatingContext.put("editorial_note", DocumentMetadataHelper.enc(editorialNote));
         templatingContext.put("add_document_visual_editor", addDocumentVisualEditor);
         templatingContext.put("clear_org_if_not_match", clearOrganizationIfNotMatch);
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
             organizations = OrganizationData.fromMeta(metaDom, "/meta/organizations");
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
             for(Resource attachment : resources)
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
         node.setDescription(description);
         if(addDocumentVisualEditor)
         {
             node.setContent(content);
         }
         else
         {
             node.setContent(makePara(stripTags(content)));
         }
         node.setAbstract(docAbstract);
         node.setValidityStart(validityStart);
         node.setValidityEnd(validityEnd);
         node.setEventStart(eventStart);
         node.setEventEnd(eventEnd);
         node.setEventPlace(eventPlace);
         Document doc = doc(getMetaElm());
         node.setMeta(dom4jToText(doc));
         node.setOrganizationIds(OrganizationData.getOrganizationIds(organizations));
     }
 
     private Element getMetaElm()
     {
         return elm("meta", elm("authors", elm("author", elm("name", proposerCredentials), elm(
             "e-mail", proposerEmail))), elm("sources", elm("source", elm("name", sourceName), elm(
             "url", sourceUrl))), elm("editor"), elm("event", elm("address", elm("street",
             eventStreet), elm("postcode", eventPostCode), elm("city", eventCity), elm("province",
             eventProvince))), OrganizationData.toMeta(organizations));
     }
 
     public void fromProposal(DocumentNodeResource node, CoralSession coralSession)
     {
         try
         {
             Document proposalDom = textToDom4j(node.getProposedContent());
             name = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/name"));
             title = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/title"));
             docAbstract = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/abstract"));
             // DECODE HTML
             content = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/content"));
             description = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/description"));
             validityStart = text2date(DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/validity/start")));
             validityEnd = text2date(DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/validity/end")));
             eventPlace = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/event/place"));
             eventProvince = DocumentMetadataHelper.dec(selectFirstText(proposalDom,
                 "/document/meta/event/address/province"));
             eventPostCode = DocumentMetadataHelper.dec(selectFirstText(proposalDom,
                 "/document/meta/event/address/postcode"));
             eventCity = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/meta/event/address/city"));
             eventStreet = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/meta/event/address/street"));
             eventStart = text2date(DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/event/start")));
             eventEnd = text2date(DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/event/end")));
             organizations = OrganizationData.fromMeta(proposalDom, "/document/meta/organizations");
             sourceName = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/meta/sources/source/name"));
             sourceUrl = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/meta/sources/source/url"));
             proposerCredentials = DocumentMetadataHelper.dec(selectFirstText(proposalDom,
                 "/document/meta/authors/author/name"));
             proposerEmail = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/meta/authors/author/e-mail"));
             selectedCategories = new HashSet<CategoryResource>();
             for(Element categoryNode : (List<Element>)proposalDom
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
             for(Element attachmentNode : (List<Element>)proposalDom
                 .selectNodes("/document/attachments/attachment"))
             {
                 long fileId = Long.parseLong(attachmentNode.elementTextTrim("ref"));
                 try
                 {
                     attachments.add(FileResourceImpl.getFileResource(coralSession, fileId));
                     attachmentDescriptions.add(DocumentMetadataHelper.dec(attachmentNode.elementText("description")));
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
             editorialNote = DocumentMetadataHelper.dec(selectFirstText(proposalDom, "/document/editorial/note"));
         }
         catch(HTMLException e)
         {
             throw new RuntimeException("malformed proposed changes descriptor in document #"
                 + node.getIdString(), e);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new RuntimeException(
                 "invalid resource id in proposed changes descriptor in document #"
                     + node.getIdString(), e);
         }
     }
 
     public void toProposal(DocumentNodeResource node)
     {
         Element categoriesElm = elm("categories");
         for(CategoryResource category : selectedCategories)
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
         Document doc = doc(elm(
             "document",
             elm("request", removalRequested ? "remove" : "update"),
             elm("origin", elm("ref", origin.getIdString())),
             elm("name", name),
             elm("title", title),
             elm("abstract", cdata(docAbstract)),
             elm("content", cdata(content)),
             elm("description", description),
             elm("editorial", elm("note", editorialNote)),
             elm("validity", elm("start", date2text(validityStart)),
                 elm("end", date2text(validityEnd))),
             elm("event", elm("place", eventPlace), elm("start", date2text(eventStart)),
                 elm("end", date2text(eventEnd))), getMetaElm(), categoriesElm, attachmentsElm));
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
             Document contentDom = htmlService.textToDom4j(content, errorWriter, cleanupProfile);
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
 
     public boolean isFileUploadValid(CoralSession coralSession, Parameters parameters,
         FileUpload fileUpload, FilesService filesService)
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
                 if(attachmentsMultiUpload)
                 {
                     UploadBucket bucket = fileUpload.getBucket(uploadBucketId);
                     if(bucket != null)
                     {
                         List<UploadContainer> containers = new ArrayList<>(bucket.getContainers());
                         // sort containers by id
                         Collections.sort(containers, new Comparator<UploadContainer>()
                             {
                             @Override
                             public int compare(UploadContainer o1, UploadContainer o2)
                             {
                                 return Integer.parseInt(o1.getName())
                                                 - Integer.parseInt(o2.getName());
                             }
                             });
                         Iterator<UploadContainer> contIter = containers.iterator();
                         int i = attachments.size();
                         fileCheck: while(contIter.hasNext() && i < attachmentsMaxCount)
                         {
                             UploadContainer container = contIter.next();
                             String description = parameters.get(
                                 "attachment_description_" + container.getName(), "");
                             attachmentDescriptions.set(i, description);
                             if(!isAttachmentValid(i, container, filesService))
                             {
                                 valid = false;
                                 break fileCheck;
                             }
                             i++;
                         }                        
                     }
                     else
                     {
                        int uploadedFilesCount = parameters.getInt("upload_bucket_size", -1);
                        logger.error("missing or invalid bucket id: " + uploadBucketId + " with "
                            + uploadedFilesCount + " uploaded files reported by browser");
                        if(uploadedFilesCount != 0)
                        {
                            validationFailure = "attachement_async_upload_failed";
                            valid = false;
                        }
                     }
                 }
                 else
                 {
                     fileCheck: for(int i = attachments.size(); i < attachmentsMaxCount; i++)
                     {
                         try
                         {
                             UploadContainer uploadedFile = fileUpload.getContainer("attachment_" + (i + 1));
                             if(uploadedFile != null)
                             {
                                 if(!isAttachmentValid(i, uploadedFile, filesService))
                                 {
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
         }
         return valid;
     }
 
     private boolean isAttachmentValid(int index, UploadContainer uploadedFile,
         FilesService filesService)
     {
         if(uploadedFile.getSize() > attachmentsMaxSize * 1024)
         {
             validationFailure = "attachment_size_exceeded";
             return false;
         }
 
         String fileName = uploadedFile.getFileName();
         String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1).trim().toLowerCase();
         if(!attachmentFormatList.contains(fileExt))
         {
             validationFailure = "attachment_type_not_allowed";
             return false;
         }
 
         try
         {
             byte[] srcBytes = IOUtils.toByteArray(uploadedFile.getInputStream());
             final ByteArrayInputStream is = new ByteArrayInputStream(srcBytes);
             String contentType = filesService.detectMimeType(is, uploadedFile.getFileName());
             byte[] targetBytes = srcBytes;
             if(imageMaxSize != 0 && contentType.startsWith("image/"))
             {
                 is.reset();
                 BufferedImage srcImage;
                 try
                 {
                     srcImage = ImageIO.read(is);
                 }
                 catch(Exception e)
                 {
                     throw new IIOException("image reading error", e);
                 }
                 BufferedImage targetImage = null;
                 try
                 {
                     if(srcImage.getWidth() > imageMaxSize || srcImage.getHeight() > imageMaxSize)
                     {
                         targetImage = Scalr.resize(srcImage, Scalr.Method.AUTOMATIC,
                             Scalr.Mode.AUTOMATIC, imageMaxSize, imageMaxSize);
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         ImageIO.write(targetImage, "jpeg", baos);
                         baos.flush();
                         targetBytes = baos.toByteArray();
                         contentType = "image/jpeg";
                     }
                 }
                 finally
                 {
                     srcImage.flush();
                     if(targetImage != null)
                     {
                         targetImage.flush();
                     }
                 }
             }
             add(attachmentContents, index, targetBytes);
             add(attachmentTypes, index, contentType);
             add(attachmentNames, index,
                 ProposedDocumentData.getAttachmentName(uploadedFile.getFileName(), index));
             return true;
         }
         catch(ImagingOpException | IllegalArgumentException e)
         {
             validationFailure = "image_resize_failed";
         }
         catch(IIOException e)
         {
             validationFailure = "image_format_error";
         }
         catch(IOException e)
         {
             validationFailure = "attachment_processing_error";
         }
         return false;
     }
 
     public void releaseUploadBucket(FileUpload fileUpload)
     {
         if(attachmentsMultiUpload && uploadBucketId != null && uploadBucketId.trim().length() > 0)
         {
             UploadBucket bucket = fileUpload.getBucket(uploadBucketId);
             if(bucket != null)
             {
                 fileUpload.releaseBucket(bucket, "after use");
             }
         }
     }
 
     // adds element at the specified position filling unused leading position with nulls if
     // necessary
     private <T> void add(List<T> list, int i, T item)
     {
         while(list.size() < i)
         {
             list.add(null);
         }
         list.add(i, item);
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
     
     public List<OrganizationData> getOrganizations()
     {
         return organizations;
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
 
     public byte[] getAttachmentContents(int index)
     {
         return attachmentContents.get(index);
     }
 
     public String getAttachmentType(int index)
     {
         return attachmentTypes.get(index);
     }
 
     public String getAttachmentName(int index)
     {
         return attachmentNames.get(index);
     }
 
     public int getNewAttachmentsCount()
     {
         return attachmentContents.size();
     }
 
     public List<Resource> getCurrentAttachments()
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
 
     public void setOrganizations(List<OrganizationData> organizations)
     {
         this.organizations = organizations;
     }
 
     public void setSelectedCategories(Set<CategoryResource> selectedCategories)
     {
         this.selectedCategories = selectedCategories;
     }
 
     public void setAttachments(List<Resource> attachments)
     {
         this.attachments = new ArrayList<Resource>(attachmentsMaxCount);
         attachmentDescriptions = new ArrayList<String>(attachmentsMaxCount);
         for(Resource attachment : attachments)
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
     public String cleanupContent(String content, HTMLService htmlService)
         throws ProcessingException
     {
         if(content == null || content.trim().length() == 0)
         {
             return "";
         }
         try
         {
             StringWriter errorWriter = new StringWriter();
             Document contentDom = htmlService.textToDom4j(content, errorWriter, cleanupProfile);
             if(contentDom == null)
             {
                 throw new ProcessingException("HTML processing failure");
             }
             else
             {
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
 
     public static String getAttachmentName(String fileName, int index)
     {
         StringBuilder buff = new StringBuilder();
         SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
         buff.append(df.format(new Date())); // timestamp
         buff.append("_"); // separator
         buff.append(index); // form field or upload bucket slot index
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
         OrganizationData.dump(organizations, proposalsDump);
         proposalsDump.append("Source name: ").append(sourceName).append("\n");
         proposalsDump.append("Source URL: ").append(sourceUrl).append("\n");
         proposalsDump.append("Proposer credentials: ").append(proposerCredentials).append("\n");
         proposalsDump.append("Proposer email: ").append(proposerEmail).append("\n");
         proposalsDump.append("Administrative description: ").append(proposerEmail).append("\n");
         proposalsDump.append("Content: \n").append(content).append("\n");
         logger.debug(proposalsDump.toString());
     }
 }
