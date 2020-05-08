 package controllers;
 
 import play.mvc.With;
 import play.mvc.Router;
 import play.data.validation.Validation;
 import filters.AdminAuthorizationFilter;
 import net.sparkmuse.data.entity.Feedback;
 import net.sparkmuse.ajax.ValidationErrorAjaxResponse;
 import net.sparkmuse.ajax.RedirectAjaxResponse;
 
 import javax.inject.Inject;
 
 import org.apache.commons.lang.StringUtils;
 import com.google.code.twig.ObjectDatastore;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import java.util.List;
 import java.util.HashMap;
 
 /**
  * @author neteller
  * @created: Dec 20, 2010
  */
 @With(AdminAuthorizationFilter.class)
 public class Admin extends SparkmuseController {
 
   @Inject static ObjectDatastore datastore;
 
   public static final void manageFeedback(String key) {
     Feedback feedback = StringUtils.isEmpty(key) ? null : datastore.load(Feedback.class, key);
     List<Feedback> feedbacks = Lists.newArrayList(datastore.find(Feedback.class));
     if (null != feedback) {
       render(feedbacks, feedback);
     }
     else {
       render(feedbacks);
     }
   }
 
   public static final void saveFeedback(String key, String title, String content, String displayContent, boolean isPrivate, List<String> imageKeys) {
     if (Validation.hasErrors()) {
       renderJSON(new ValidationErrorAjaxResponse(validation.errorsMap()));
     }
     else {
       final Feedback f = Feedback.newInstance(key, title, content, displayContent, isPrivate, imageKeys);
       //@todo wtf storeorupdate!?
      //@todo is association causing second updates to be unsaved?
       if (null == datastore.load(Feedback.class, key)) {
         datastore.store(f);
       }
       else {
         datastore.associate(f);
         datastore.update(f);
       }
       final HashMap<String, Object> parameters = Maps.newHashMap();
       parameters.put("key", key);
       final String url = Router.reverse("Admin.manageFeedback", parameters).url;
       renderJSON(new RedirectAjaxResponse(url));
     }
   }
 
 }
