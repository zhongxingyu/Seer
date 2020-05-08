 package net.cyklotron.cms.modules.actions.category.query;
 
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.query.CategoryQueryBuilder;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.category.query.CategoryQueryUtil;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * Category query update action.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CategoryQueryCleanCategories.java,v 1.1 2005-05-30 01:01:48 zwierzem Exp $
  */
 public class CategoryQueryCleanCategories
     extends BaseCategoryQueryAddUpdate
 {
     public CategoryQueryCleanCategories(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, CategoryQueryService categoryQueryService,
         CategoryService categoryService, SiteService siteService)
     {
         super(logger, structureService, cmsDataFactory, categoryQueryService, categoryService,
                         siteService);
     }
 
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
     throws ProcessingException
     {
         CategoryQueryResource query = getQuery(coralSession, parameters);
         fixQuery(coralSession, query);
         
         templatingContext.put("result","cleaned_successfully");
     }
 
     protected void fixQuery(CoralSession coralSession, CategoryQueryResource query)
         throws ProcessingException
     {
         Set requiredSet = categoryQueryService.initCategorySelection(coralSession,
                 query.getRequiredCategoryPaths(), "required").keySet();
         Set optionalSet = categoryQueryService.initCategorySelection(coralSession, 
                 query.getOptionalCategoryPaths(), "optional").keySet();
        CategoryQueryBuilder queryBuilder = queryBuilder =
             new CategoryQueryBuilder(requiredSet, optionalSet, query.getUseIdsAsIdentifiers(true));
         query.setQuery(queryBuilder.getQuery());
         query.setRequiredCategoryPaths(
         CategoryQueryUtil.joinCategoryIdentifiers(queryBuilder.getRequiredIdentifiers()));
         query.setOptionalCategoryPaths(
         CategoryQueryUtil.joinCategoryIdentifiers(queryBuilder.getOptionalIdentifiers()));
         query.update();
     }
 
     public boolean checkAccessRights(Context context)
     throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         return checkPermission(context, coralSession, "cms.category.query.modify");
     }
 }
