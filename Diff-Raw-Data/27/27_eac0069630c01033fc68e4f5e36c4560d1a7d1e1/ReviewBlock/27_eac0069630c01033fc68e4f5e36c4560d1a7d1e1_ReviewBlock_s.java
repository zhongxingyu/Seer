 package com.ngdb.web.components.article;
 
 import com.ngdb.entities.Registry;
 import com.ngdb.entities.Reviewer;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.element.Review;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.services.infrastructure.CurrentUser;
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.annotations.*;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.TreeSet;
 
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 
 public class ReviewBlock {
 
     @Property
     @Parameter
     private Article article;
 
     @Property
     private Review review;
 
     @Inject
     private CurrentUser currentUser;
 
    private Collection<String> suggestions = new TreeSet<String>();

     @Property
     private String label;
 
     @Property
     private String url;
 
     @Property
     private String mark;
 
     @Inject
     private Registry registry;
 
    @Property
    private Set<Review> reviews;

     @Inject
     private Reviewer reviewer;
 
     @Inject
     private AjaxResponseRenderer ajaxResponseRenderer;
 
     @InjectComponent
     private Zone reviewZone;
 
    void onActivate() {
        suggestions.addAll(registry.findAllTags());
    }

    @SetupRender
    void init() {
        reviews = reviewer.reviewsOf(article);
     }
 
     public double getAverageMarkAsDouble() {
         return reviewer.getAverageMarkOf(article);
     }
 
    public int getReviewsCount() {
        return reviews.size();
    }

     @CommitAfter
     @OnEvent(value = EventConstants.SUCCESS, component = "reviewForm")
     public void onSuccessFromReviewForm() {
         if (isNotBlank(label) && isNotBlank(url) && isNotBlank(mark)) {
             article.updateModificationDate();
             currentUser.addReviewOn(article, label, url, mark);
         }
         ajaxResponseRenderer.addRender(reviewZone);
     }
 
     public User getUser() {
         return currentUser.getUser();
     }
 
     public String getStars() {
         return review.getMarkAsStars();
     }
 
 }
