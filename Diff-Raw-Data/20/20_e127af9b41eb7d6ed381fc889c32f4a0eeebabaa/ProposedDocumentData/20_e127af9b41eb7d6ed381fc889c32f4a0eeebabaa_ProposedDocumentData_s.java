 package net.cyklotron.cms.structure.internal;
 
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.doc;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.dom4jToText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.elm;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.selectFirstText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.textToDom4j;
 
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
 
 /**
  * Data object used by ProposeDocument view and action.
  * <p>
  * Feels kind like breaking open door, but I'm not willing to learn formtool just to make one silly
  * screen.
  * </p>
  * 
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
     private String organizedBy;
     private String organizedAddress;
     private String organizedPhone;
     private String organizedFax;
     private String organizedEmail;
     private String organizedWww;
     private String sourceName;
     private String sourceUrl;
     private String proposerCredentials;
     private String proposerEmail;
     private String description;
     private Date validityStart;
     private Date validityEnd;
     private Date eventStart;
     private Date eventEnd;
     private Set<CategoryResource> availableCategories;
     private Set<CategoryResource> selectedCategories;
     private List<Resource> attachments;
     private List<String> attachmentDescriptions;
 
     // validation
     private String validationFailure;
 
     // helper objects
     private static final HTMLEntityEncoder ENCODER = new HTMLEntityEncoder();
     private final DateFormat format = DateFormat.getDateTimeInstance();
 
     public ProposedDocumentData(Parameters configuration)
     {
         calendarTree = configuration.getBoolean("calendar_tree", true);
         inheritCategories = configuration.getBoolean("inherit_categories", true);
         
         attachmentsEnabled = configuration.getBoolean("attachments_enabled", false);
         attachmentsMaxCount = configuration.getInt("attachments_max_count", 0);
         attachmentsMaxSize = configuration.getInt("attachments_max_size", 0);
         attachmentsAllowedFormats = configuration.get(
             "attachments_allowed_formats", "jpg gif doc rtf pdf xls");
         attachmentFormatList = Arrays.asList(attachmentsAllowedFormats.toLowerCase().split("\\s+"));
         attachmentDirId = configuration.getLong("attachments_dir_id");
     }
     
     public void fromParameters(Parameters parameters, CoralSession coralSession)
         throws EntityDoesNotExistException
     {
         name = parameters.get("name", "");
         title = parameters.get("title", "");
         docAbstract = parameters.get("abstract", "");
         content = parameters.get("content", "");
         eventPlace = parameters.get("event_place", "");
         organizedBy = parameters.get("organized_by", "");
         organizedAddress = parameters.get("organized_address", "");
         organizedPhone = parameters.get("organized_phone", "");
         organizedFax = parameters.get("organized_fax", "");
         organizedEmail = parameters.get("organized_email", "");
         organizedWww = parameters.get("organized_www", "");
         sourceName = parameters.get("source_name", "");
         sourceUrl = parameters.get("source_url", "");
         proposerCredentials = parameters.get("proposer_credentials", "");
         proposerEmail = parameters.get("proposer_email", "");
         description = parameters.get("description", "");
         
         validityStart = getDate(parameters, "validity_start");
         validityEnd = getDate(parameters, "validity_end");
         eventStart = getDate(parameters, "event_start");
         eventEnd = getDate(parameters, "event_end");
         
         selectedCategories = new HashSet<CategoryResource>();
         for(long categoryId : parameters.getLongs("selected_categories"))
         {   
             if(categoryId != -1)
             {
                 selectedCategories.add(CategoryResourceImpl.getCategoryResource(coralSession, categoryId));
             }
         }
 
         availableCategories = new HashSet<CategoryResource>();
         for(long categoryId : parameters.getLongs("available_categories"))
         {
             availableCategories.add(CategoryResourceImpl.getCategoryResource(coralSession, categoryId));
         }        
         
         if(attachmentsEnabled)
         {
             attachmentDescriptions = new ArrayList<String>(attachmentsMaxCount);
             for(int i = 1; i <= attachmentsMaxCount; i++)
             {
                 attachmentDescriptions.add(parameters.get("attachment_description_"+i, ""));
             }
             attachments = new ArrayList<Resource>(attachmentsMaxCount);
             for(int i = 1; i <= attachmentsMaxCount; i++)
             {
                 String fileId = parameters.get("existing_attachment_id_"+i, null);
                 if(fileId != null)
                 {
                     attachments.add(FileResourceImpl.getFileResource(coralSession, Long.parseLong(fileId)));
                 }
             }
         }
     }
 
     /**
      * Transfers the data into the templating context. 
      * 
      * <p>
      * This is needed to keep the exiting templates working
      * </p>
      * 
      * @param templatingContext
      */
     public void toTemplatingContext(TemplatingContext templatingContext)
     {
         templatingContext.put("name", name);
         templatingContext.put("title", title);
         templatingContext.put("abstract", docAbstract);
         templatingContext.put("content", content);
         templatingContext.put("event_place", eventPlace);
         templatingContext.put("organized_by", organizedBy);
         templatingContext.put("organized_address", organizedAddress);
         templatingContext.put("organized_phone", organizedPhone);
         templatingContext.put("organized_fax", organizedFax);
         templatingContext.put("organized_email", organizedEmail);
         templatingContext.put("organized_www", organizedWww);
         templatingContext.put("source_name", sourceName);
         templatingContext.put("source_url", sourceUrl);
         templatingContext.put("proposer_credentials", proposerCredentials);
         templatingContext.put("proposer_email", proposerEmail);
         templatingContext.put("description", description);
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
             templatingContext.put("attachment_descriptions", attachmentDescriptions);
         }
     }
     
     public void fromNode(DocumentNodeResource node, CategoryService categoryService,
         RelatedService relatedService, HTMLService htmlService, CoralSession coralSession)
     {
         // calendarTree
         // inheritCategories
         name = node.getName();
         title = node.getTitle();
         docAbstract = node.getAbstract();
         content = node.getContent();
         description = node.getDescription();
         validityStart = node.getValidityStart();
         validityEnd = node.getValidityEnd();        
         eventPlace = node.getEventPlace();
         eventStart = node.getEventStart();
         eventEnd = node.getEventEnd();
         try
         {
             Document metaDom = textToDom4j(node.getMeta());
            organizedBy = selectFirstText(metaDom, "/document/meta/organisation/name");
            organizedAddress = selectFirstText(metaDom, "/document/meta/organisation/address");
            organizedPhone = selectFirstText(metaDom, "/document/meta/organisation/tel");
            organizedFax = selectFirstText(metaDom, "/document/meta/organisation/fax");
            organizedEmail = selectFirstText(metaDom, "/document/meta/organisation/e-mail");
            organizedWww = selectFirstText(metaDom, "/document/meta/organisation/url");
            sourceName = selectFirstText(metaDom, "/document/meta/sources/source/name");
            sourceUrl = selectFirstText(metaDom, "/document/meta/sources/source/url");
            proposerCredentials = selectFirstText(metaDom, "/document/meta/authors/author/name");
            proposerEmail = selectFirstText(metaDom, "/document/meta/authors/author/e-mail");
         }
         catch(HTMLException e)
         {
             throw new RuntimeException("malformed metadada in resource "+node.getIdString(), e);
         }
         selectedCategories = new HashSet<CategoryResource>(Arrays.asList(categoryService
             .getCategories(coralSession, node, false)));
         if(attachmentsEnabled)
         {
             attachments = new ArrayList<Resource>(Arrays.asList(relatedService.getRelatedTo(coralSession,
                 node, node.getRelatedResourcesSequence(), null)));
             attachmentDescriptions = new ArrayList<String>(attachmentsMaxCount);
             for(Resource attachment : attachments)
             {
                 if(attachment instanceof CmsNodeResource)
                 {
                     attachmentDescriptions.add(((CmsNodeResource)attachment).getDescription());
                 }
                 else
                 {
                     attachmentDescriptions.add("");
                 }
             }
             // fill up with empty strings to make template logic more simple
             while(attachmentDescriptions.size() < attachmentsMaxCount)
             {
                 attachmentDescriptions.add("");
             }
         }
     }
     
     public void toNode(DocumentNodeResource node)
     {
         // set attributes to new node
         node.setDescription(enc(description));
         content = setContent(node, content);
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
         return elm("meta", elm("authors", elm("author", elm("name",
             enc(proposerCredentials)), elm("e-mail", enc(proposerEmail)))), elm("sources", elm(
             "source", elm("name", enc(sourceName)), elm("url", enc(sourceUrl)))), elm("editor"),
             elm("organisation", elm("name", enc(organizedBy)),
                 elm("address", enc(organizedAddress)), elm("tel", enc(organizedPhone)), elm("fax",
                     enc(organizedFax)), elm("e-mail", enc(organizedEmail)), elm("url",
                     enc(organizedWww)), elm("id", "0")));
     }
     
     public void fromProposal(DocumentNodeResource node, CoralSession coralSession)
     {
         try
         {
             Document proposalDom = textToDom4j(node.getProposedContent());
             name = selectFirstText(proposalDom, "/document/name");
             title = selectFirstText(proposalDom, "/document/title");
             docAbstract = selectFirstText(proposalDom, "/document/abstract");
             // DECODE HTML
             content = selectFirstText(proposalDom, "/document/content");
             description = selectFirstText(proposalDom, "/document/description");
             validityStart = text2date(selectFirstText(proposalDom, "/document/validity/start"));
             validityEnd = text2date(selectFirstText(proposalDom, "/document/validity/end"));        
             eventPlace = selectFirstText(proposalDom, "/document/event/place");
             eventStart = text2date(selectFirstText(proposalDom, "/document/event/start"));
             eventEnd = text2date(selectFirstText(proposalDom, "/document/event/end"));
             organizedBy = selectFirstText(proposalDom, "/document/meta/organisation/name");
             organizedAddress = selectFirstText(proposalDom, "/document/meta/organisation/address");
             organizedPhone = selectFirstText(proposalDom, "/document/meta/organisation/tel");
             organizedFax = selectFirstText(proposalDom, "/document/meta/organisation/fax");
             organizedEmail = selectFirstText(proposalDom, "/document/meta/organisation/e-mail");
             organizedWww = selectFirstText(proposalDom, "/document/meta/organisation/url");
             sourceName = selectFirstText(proposalDom, "/document/meta/sources/source/name");
             sourceUrl = selectFirstText(proposalDom, "/document/meta/sources/source/url");
             proposerCredentials = selectFirstText(proposalDom, "/document/meta/authors/author/name");
             proposerEmail = selectFirstText(proposalDom, "/document/meta/authors/author/e-mail");
             for(Element categoryNode : (List<Element>)proposalDom.selectNodes("/document/categories/category/ref"))
             {
                 long categoryId = Long.parseLong(categoryNode.getTextTrim());
                 selectedCategories.add(CategoryResourceImpl.getCategoryResource(coralSession, categoryId));                
             }
             for(Element attachmentNode : (List<Element>)proposalDom.selectNodes("/document/attachments/attachment"))
             {
                 long fileId = Long.parseLong(attachmentNode.elementTextTrim("ref"));
                 attachments.add(FileResourceImpl.getFileResource(coralSession, fileId));
                 attachmentDescriptions.add(attachmentNode.elementText("description"));
             }
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
         for(CategoryResource category : selectedCategories)
         {
             categoriesElm.add(elm("category", elm("ref", category.getIdString())));
         }
         Element attachmentsElm = elm("attachments");
         Iterator<Resource> attachmentIterator = attachments.iterator();
         Iterator<String> descriptionIterator = attachmentDescriptions.iterator();
         while(attachmentIterator.hasNext())
         {
             attachmentsElm.add(elm("attachment",
                 elm("ref", attachmentIterator.next().getIdString()), elm("description",
                     descriptionIterator.next())));
         }
         Document doc = doc(elm("document", elm("name", enc(name)), elm("title", enc(title)), elm(
             "abstract", enc("docAbstract")), elm("content", enc(content)), elm("description",
             enc(description)), elm("validity", elm("start", date2text(validityStart)), elm("end",
             date2text(validityEnd))), elm("event", elm("place", enc(eventPlace)), elm("start",
             date2text(eventStart)), elm("end", date2text(eventEnd))), getMetaElm(), categoriesElm, attachmentsElm));
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
     
     public boolean isValid()
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
         return true;
     }
     
     public boolean isFileUploadValid(CoralSession coralSession, FileUpload fileUpload)
         throws ProcessingException
     {
         boolean valid = true;
         if(attachmentsEnabled)
         {
             // check if attachment_dir_id is configured, points to a directory, and user has write rights
             try
             {
                 DirectoryResource dir = DirectoryResourceImpl.getDirectoryResource(coralSession, attachmentDirId);
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
                             String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1).trim()
                                 .toLowerCase();
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
                         validationFailure =  "upload_size_exceeded"; // i18n
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
         return enc(name);
     }
     
     public String getTitle()
     {
         return enc(title);
     }    
 
     public Date getValidityStart()
     {
         return validityStart;
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
         return attachmentDescriptions.get(index);
     }
     
     public UploadContainer getAttachmentContainer(int index, FileUpload fileUpload)
         throws UploadLimitExceededException
     {
         return fileUpload.getContainer("attachment_" + (index + 1));
     }
     
     public int getAttachmentsCurrentCount()
     {
         return attachments.size();
     }
     
     public void addAttachment(FileResource file)
     {
         attachments.add(file);
         attachmentDescriptions.add(file.getDescription());
     }
     
     public void removeAttachment(long fileId, CoralSession coralSession)
         throws EntityDoesNotExistException
     {
         FileResource file = FileResourceImpl.getFileResource(coralSession, fileId);
         int index = attachments.indexOf(file);
         attachments.remove(index);
         attachmentDescriptions.remove(index);
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
     
     private String setContent(DocumentNodeResource node, String content)
     {
         content = content.replaceAll("\r\n", "\n");
         content = content.replaceAll("\n", "</p>\n<p>");
         content = "<p>" + content + "</p>";
         content = content.replaceAll("<p>\\s*</p>", "");
         node.setContent(content);
         return content;
     }
        
     private String enc(String s)
     {
         s = s.replaceAll("<[^>]*?>", " "); // strip html tags
         return ENCODER.encodeAttribute(s, "UTF-16");
     }    
     
     public static String getAttachmentName(String fileName)
     {
         StringBuilder buff = new StringBuilder();
         SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
         buff.append(df.format(new Date())); // timestamp
         buff.append("_"); // separator
         fileName = StringUtils.iso1toUtf8(fileName);
         fileName = StringUtils.unaccentLatinChars(fileName); // unaccent accented latin characters
         fileName = fileName.replaceAll("[^A-Za-z0-9-_.]+", "_"); // squash everything except alphanumerics and allowed punctuation
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
         proposalsDump.append("Event start: ").append(formatDate(eventStart)).append("\n");
         proposalsDump.append("Event end: ").append(formatDate(eventEnd)).append("\n");
         proposalsDump.append("Document validity start: ").append(formatDate(validityStart)).append(
             "\n");
         proposalsDump.append("Document validity end: ").append(formatDate(validityEnd))
             .append("\n");
         proposalsDump.append("Organized by: ").append(organizedBy).append("\n");
         proposalsDump.append("Organizer address: ").append(organizedAddress).append("\n");
         proposalsDump.append("Organizer phone: ").append(organizedPhone).append("\n");
         proposalsDump.append("Organizer fax: ").append(organizedFax).append("\n");
         proposalsDump.append("Organizer email: ").append(organizedEmail).append("\n");
         proposalsDump.append("Organizer URL: ").append(organizedWww).append("\n");
         proposalsDump.append("Source name: ").append(sourceName).append("\n");
         proposalsDump.append("Source URL: ").append(sourceUrl).append("\n");
         proposalsDump.append("Proposer credentials: ").append(proposerCredentials).append("\n");
         proposalsDump.append("Proposer email: ").append(proposerEmail).append("\n");
         proposalsDump.append("Administrative description: ").append(proposerEmail).append("\n");
         proposalsDump.append("Content: \n").append(content).append("\n");
         logger.debug(proposalsDump.toString());
     }
 }
