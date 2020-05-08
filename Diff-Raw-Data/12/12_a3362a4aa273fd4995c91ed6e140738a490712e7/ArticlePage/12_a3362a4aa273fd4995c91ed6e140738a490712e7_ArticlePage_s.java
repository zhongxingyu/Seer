 package de.flower.rmt.ui.page.blog;
 
 import de.flower.rmt.model.db.entity.BArticle;
 import de.flower.rmt.service.IBlogManager;
 import de.flower.rmt.ui.model.BArticleModel;
 import de.flower.rmt.ui.page.base.AbstractCommonBasePage;
 import de.flower.rmt.ui.page.base.NavigationPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * displays a blog article and related comments.
  *
  * @author flowerrrr
  */
 public class ArticlePage extends AbstractCommonBasePage {
 
     public final static String PARAM_ARTICLEID = "post";
 
     private final static Logger log = LoggerFactory.getLogger(ArticlePage.class);
 
     @SpringBean
     private IBlogManager blogManager;
 
     /**
      * Deep link support
      *
      * @param params
      */
     public ArticlePage(PageParameters params) {
         BArticle article = null;
         try {
             Long id = params.get(PARAM_ARTICLEID).toLong();
             article = blogManager.loadArticleById(id);
         } catch (Exception e) {
             log.warn("ArticlePage accessed with invalid parameter: " + e.toString());
             throw new AbortWithHttpErrorCodeException(404, "Invalid page parameter: " + e.getMessage());
         }
         // as associations are often needed use a fully initialized event object model.
         init(new BArticleModel(article));
     }
 
     public static PageParameters getPageParams(Long articleId) {
         checkNotNull(articleId);
         return new PageParameters().set(PARAM_ARTICLEID, articleId);
     }
 
     private void init(IModel<BArticle> model) {
         setDefaultModel(model);
         setHeadingModel(new PropertyModel(model, "heading"), null);
         addMainPanel(new ArticlePanel(model));
         addSecondaryPanel(new BlogSecondaryPanel());
     }
 
     @Override
     public String getActiveTopBarItem() {
         return NavigationPanel.BLOG;
     }
 }
