 package net.cyklotron.cms.modules.views.structure;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.table.comparator.NameComparator;
 import org.objectledge.diff.DetailElement;
 import org.objectledge.diff.DiffUtil;
 import org.objectledge.diff.Sequence;
 import org.objectledge.diff.Splitter;
 import org.objectledge.diff.Element.State;
 import org.objectledge.html.HTMLParagraphSplitter;
 import org.objectledge.html.HTMLService;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.modules.views.documents.BaseSkinableDocumentScreen;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.internal.ProposedDocumentData;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  *
  */
 public class ReviewProposedChanges
     extends BaseStructureScreen
 {
     protected UserManager userManager;
 
     private CategoryService categoryService;
 
     private HTMLService htmlService;
 
     public ReviewProposedChanges(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, StructureService structureService,
         StyleService styleService, SiteService siteService, RelatedService relatedService,
         UserManager userManager, CategoryService categoryService, HTMLService htmlService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager,
                         structureService, styleService, siteService, relatedService);
         this.userManager = userManager;
         this.categoryService = categoryService;
         this.htmlService = htmlService;
     }
 
     @Override
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             long docId = parameters.getLong("doc_id");
             templatingContext.put("doc_id", docId);
 
             DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(
                 coralSession, docId);
 
             boolean isDocEquals;
             Sequence<DetailElement<String>> title;
             Sequence<Sequence<DetailElement<String>>> docAbstract;
             Sequence<Sequence<DetailElement<String>>> content;
             Sequence<DetailElement<String>> validityStart;
             Sequence<DetailElement<String>> validityEnd;
             Sequence<DetailElement<String>> eventStart;
             Sequence<DetailElement<String>> eventEnd;
             Sequence<DetailElement<String>> eventPlace;
             Sequence<DetailElement<String>> organizedBy;
             Sequence<DetailElement<String>> organizedAddress;
             Sequence<DetailElement<String>> organizedPhone;
             Sequence<DetailElement<String>> organizedFax;
             Sequence<DetailElement<String>> organizedEmail;
             Sequence<DetailElement<String>> organizedWww;
             Sequence<DetailElement<String>> sourceName;
             Sequence<DetailElement<String>> sourceUrl;
             Sequence<DetailElement<String>> proposerCredentials;
             Sequence<DetailElement<String>> proposerEmail;
             Sequence<Sequence<DetailElement<String>>> description;
 
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             ProposedDocumentData publishedData = new ProposedDocumentData();
             ProposedDocumentData proposedData = new ProposedDocumentData();
 
             proposedData.fromProposal(node, coralSession);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig(proposedData.getOrigin());
             proposedData.setConfiguration(screenConfig);
             publishedData.setConfiguration(screenConfig);
             publishedData.fromNode(node, categoryService, relatedService, coralSession);
 
             NameComparator<CategoryResource> comparator = new NameComparator<CategoryResource>(
                 i18nContext.getLocale());
             long root_category_1 = screenConfig.getLong("category_id_1", -1);
             long root_category_2 = screenConfig.getLong("category_id_2", -1);
             int categoryDepth = screenConfig.getInt("category_depth", 1);
 
             Set<CategoryResource> availableCategories = new HashSet<CategoryResource>();
             BaseSkinableDocumentScreen.getCategoryList(root_category_1, categoryDepth, true,
                 coralSession, availableCategories);
             BaseSkinableDocumentScreen.getCategoryList(root_category_2, categoryDepth, true,
                 coralSession, availableCategories);
 
             Set<CategoryResource> noAvalilableCategories = new HashSet<CategoryResource>();
             if(publishedData.getSelectedCategories() != null)
             {
                 noAvalilableCategories.addAll(publishedData.getSelectedCategories());
             }
             noAvalilableCategories.removeAll(availableCategories);
 
             isDocEquals = true;
             if(!equals(publishedData.getTitle(), proposedData.getTitle()))
             {
                 title = DiffUtil.diff(proposedData.getTitle(), publishedData.getTitle(),
                     Splitter.CHARACTER_SPLITER);
                 templatingContext.put("title", title);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getAbstract(), proposedData.getAbstract()))
             {
                 docAbstract = DiffUtil.diff(proposedData.getAbstract(),
                     publishedData.getAbstract(), Splitter.NEWLINE_SPLITTER,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("docAbstract", docAbstract);
                 isDocEquals = false;
             }

            String publishedContent = publishedData.getContent() != null ? publishedData
                .getContent() : "";
            String proposedContent = proposedData.getContent() != null ? proposedData.getContent()
                : "";
             String publishedContentForComparison = ProposedDocumentData.cleanupContent(
                publishedContent, htmlService);
            content = DiffUtil.diff(proposedContent, publishedContentForComparison,
                 HTMLParagraphSplitter.INSTANCE, Splitter.WORD_BOUNDARY_SPLITTER);
             if(!content.getState().equals(State.EQUAL))
             {
                 templatingContext.put("content", content);
                 if(proposedData.getContent() != null)
                 {
                     templatingContext.put("proposedHTMLContent", proposedData.getContent());
                 }
                 if(publishedData.getContent() != null)
                 {
                     templatingContext.put("publishedHTMLContent", publishedData.getContent());
                 }
                 isDocEquals = false;
             }
             if(!equals(publishedData.getEventPlace(),proposedData.getEventPlace()))
             {
                 eventPlace = DiffUtil.diff(proposedData.getEventPlace(), publishedData
                     .getEventPlace(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("eventPlace", eventPlace);
                 isDocEquals = false;
             }
 
             if(!equals(proposedData.getEventStart(), publishedData.getEventStart()))
             {
                 String proposedEventStart = proposedData.getEventStart() != null ? proposedData
                     .getEventStart().toString() : null;
                 String publishedEventStart = publishedData.getEventStart() != null ? publishedData
                     .getEventStart().toString() : null;
                 eventStart = DiffUtil.diff(proposedEventStart, publishedEventStart,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                     templatingContext.put("eventStart", eventStart);
                     isDocEquals = false;
             }
 
             if(!equals(proposedData.getEventEnd(), publishedData.getEventEnd()))
             {
                 String proposedEventEnd = proposedData.getEventEnd() != null ? proposedData
                     .getEventEnd().toString() : null;
                 String publishedEventEnd = publishedData.getEventEnd() != null ? publishedData
                     .getEventEnd().toString() : null;
                 eventEnd = DiffUtil.diff(proposedEventEnd, publishedEventEnd,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("eventEnd", eventEnd);
                 isDocEquals = false;
             }
 
             if(!equals(proposedData.getValidityStart(), publishedData.getValidityStart()))
             {
                 String proposedValidityStart = proposedData.getValidityStart() != null ? proposedData
                     .getValidityStart().toString()
                     : null;
                 String publishedValidityStart = publishedData.getValidityStart() != null ? publishedData
                     .getValidityStart().toString()
                     : null;
                 validityStart = DiffUtil.diff(proposedValidityStart, publishedValidityStart,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("validityStart", validityStart);
                 isDocEquals = false;
             }
 
             if(!equals(proposedData.getValidityEnd(), publishedData.getValidityEnd()))
             {
                 String proposedValidityEnd = proposedData.getValidityEnd() != null ? proposedData
                     .getValidityEnd().toString() : null;
                 String publishedValidityEnd = publishedData.getValidityEnd() != null ? publishedData
                     .getValidityEnd().toString()
                     : null;
                 validityEnd = DiffUtil.diff(proposedValidityEnd, publishedValidityEnd,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("validityEnd", validityEnd);
                 isDocEquals = false;
             }
 
             if(!equals(publishedData.getOrganizedBy(), proposedData.getOrganizedBy()))
             {
                 organizedBy = DiffUtil.diff(proposedData.getOrganizedBy(), publishedData
                     .getOrganizedBy(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("organizedBy", organizedBy);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getOrganizedAddress(),proposedData.getOrganizedAddress()))
             {
                 organizedAddress = DiffUtil.diff(proposedData.getOrganizedAddress(), publishedData
                     .getOrganizedAddress(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("organizedAddress", organizedAddress);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getOrganizedFax(),proposedData.getOrganizedFax()))
             {
                 organizedFax = DiffUtil.diff(proposedData.getOrganizedFax(), publishedData
                     .getOrganizedFax(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("organizedFax", organizedFax);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getOrganizedEmail(),proposedData.getOrganizedEmail()))
             {
                 organizedEmail = DiffUtil.diff(proposedData.getOrganizedEmail(), publishedData
                     .getOrganizedEmail(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("organizedEmail", organizedEmail);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getOrganizedPhone(),proposedData.getOrganizedPhone()))
             {
                 organizedPhone = DiffUtil.diff(proposedData.getOrganizedPhone(), publishedData
                     .getOrganizedPhone(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("organizedPhone", organizedPhone);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getOrganizedWww(),proposedData.getOrganizedWww()))
             {
                 organizedWww = DiffUtil.diff(proposedData.getOrganizedWww(), publishedData
                     .getOrganizedWww(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("organizedWww", organizedWww);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getSourceName(),proposedData.getSourceName()))
             {
                 sourceName = DiffUtil.diff(proposedData.getSourceName(), publishedData
                     .getSourceName(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("sourceName", sourceName);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getSourceUrl(),proposedData.getSourceUrl()))
             {
                 sourceUrl = DiffUtil.diff(proposedData.getSourceUrl(),
                     publishedData.getSourceUrl(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("sourceUrl", sourceUrl);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getProposerCredentials(),proposedData.getProposerCredentials()))
             {
                 proposerCredentials = DiffUtil.diff(proposedData.getProposerCredentials(),
                     publishedData.getProposerCredentials(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("proposerCredentials", proposerCredentials);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getProposerEmail(),proposedData.getProposerEmail()))
             {
                 proposerEmail = DiffUtil.diff(proposedData.getProposerEmail(), publishedData
                     .getProposerEmail(), Splitter.CHARACTER_SPLITER);
                 templatingContext.put("proposerEmail", proposerEmail);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getDescription(),proposedData.getDescription()))
             {
                 description = DiffUtil.diff(proposedData.getDescription(), publishedData
                     .getDescription(), Splitter.NEWLINE_SPLITTER, Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("description", description);
                 isDocEquals = false;
             }
 
             List<CategoryResource> publishedDocCategories = new ArrayList<CategoryResource>(
                 publishedData.getSelectedCategories());
             List<CategoryResource> proposedDocCategories = new ArrayList<CategoryResource>(
                 proposedData.getSelectedCategories());
             proposedDocCategories.addAll(noAvalilableCategories);
 
             if(!equals(publishedDocCategories, proposedDocCategories))
             {
                 Collections.sort(publishedDocCategories, comparator);
                 Collections.sort(proposedDocCategories, comparator);
                 templatingContext.put("proposedDocCategories", proposedDocCategories);
                 templatingContext.put("publishedDocCategories", publishedDocCategories);
                 isDocEquals = false;
             }
 
             if(proposedData.isAttachmentsEnabled())
             {
                 if(!equals(proposedData.getAttachments(), publishedData.getAttachments())
                     || !equals(proposedData.getAttachmentDescriptions(), publishedData
                         .getAttachmentDescriptions()))
                 {
                     templatingContext.put("proposedDocAttachments", proposedData.getAttachments());
                     templatingContext
                         .put("publishedDocAttachments", publishedData.getAttachments());
                     templatingContext.put("proposedDocAttachmentsDesc", proposedData
                         .getAttachmentDescriptions());
                     templatingContext.put("publishedDocAttachmentsDesc", publishedData
                         .getAttachmentDescriptions());
 
                     isDocEquals = false;
                 }
             }
             if(!equals(proposedData.getEditorialNote(),""))
             {
                 templatingContext.put("editorial_note", proposedData.getEditorialNote());
             }
             templatingContext.put("remove_request", parameters.getLong("remove_request", 0L));
             templatingContext.put("isDocEquals", isDocEquals);
         }
         catch(Exception e)
         {
             throw new ProcessingException("Exception occured", e);
         }
     }
 
     private boolean equals(String proposed, String published)
     {
         if(proposed == null && published == null)
         {
             return true;
         }
         else if(proposed != null && published != null && proposed.equals(published))
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     private boolean equals(Date proposed, Date published)
     {
         if(proposed == null && published == null)
         {
             return true;
         }
         else if(proposed != null && published != null && proposed.compareTo(published) == 0)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     private <T>boolean equals(List<T> proposed, List<T> published)
     {
         if(proposed == null && published == null)
         {
             return true;
         }
         else if(proposed != null && published != null && proposed.containsAll(published)
             && published.containsAll(proposed))
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         return coralSession.getUserSubject().hasRole(getSite().getTeamMember());
     }
 }
