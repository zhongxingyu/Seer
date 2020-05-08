 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.fi.muni.pa165.calorycounter.frontend;
 
 import cz.fi.muni.pa165.calorycounter.serviceapi.ActivityService;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.ActivityDto;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.UserRole;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.WeightCategory;
 import java.io.IOException;
 import net.sourceforge.stripes.action.Before;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.LocalizableMessage;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.LocalizableError;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Martin Bryndza
  */
 @RequireLogin(role = UserRole.ADMIN)
 @UrlBinding("/admin/activities/{$event}/{$activity.activityId}")
 public class ActivitiesAdministrationActionBean extends BaseActionBean {
 
     final static Logger log = LoggerFactory.getLogger(ActivitiesAdministrationActionBean.class);
 
     @SpringBean //Spring can inject even to private and protected fields
     protected ActivityService activityService;
 
     private boolean removeDeprecated;
     @ValidateNestedProperties(value = {
         @Validate(on = {"confirmCreateActivity", "confirmEditActivity"}, field = "activityName", required = true)
     })
     private ActivityDto activity;
     private boolean delete;
     private final WeightCategory[] categories = WeightCategory.values();
 
     public WeightCategory[] getCategories() {
         return categories;
     }
 
     public boolean isDelete() {
         return delete;
     }
 
     public void setActivity(ActivityDto activity) {
         this.activity = activity;
     }
 
     public ActivityDto getActivity() {
         return activity;
     }
 
     public boolean isRemoveDeprecated() {
         return removeDeprecated;
     }
 
     public void setRemoveDeprecated(boolean removeDeprecated) {
         this.removeDeprecated = removeDeprecated;
     }
 
     public void validateWeightCalories() {
         for (WeightCategory category : WeightCategory.values()) {
             Integer amount = activity.getWeightCalories().get(category);
             if (amount == null || amount < 0) {
                 activity.setCaloriesAmount(category, 0);
             }
         }
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "delete", "restore"})
     public void loadRecordFromDatabase() {
         String id = getContext().getRequest().getParameter("activity.activityId");
         if (id == null) {
             return;
         }
         activity = activityService.get(Long.parseLong(id));
     }
 
     @DefaultHandler
     public Resolution def() {
         log.debug("def()");
         return new ForwardResolution("/index.jsp");
     }
 
     public Resolution updateFromPage() {
         log.debug("updateFromPage");
        return new ForwardResolution("/administrator/activity/update.jsp");
     }
 
     public Resolution update() {
         log.debug("update()");
         try {
             activityService.updateFromPage(removeDeprecated);
             this.getContext().getMessages().add(new LocalizableMessage("activities.update.success"));
         } catch (IOException e) {
             this.getContext().getValidationErrors().addGlobalError(new LocalizableError("activities.update.IOError"));
         }
         return new ForwardResolution("/administrator/activity/message.jsp");
     }
 
     public Resolution cancelOperation() {
         log.debug("cancelOperation()");
         delete = false;
         return new RedirectResolution(ActivitiesActionBean.class);
     }
 
     public Resolution create() {
         log.debug("create(): " + activity);
         delete = false;
         return new ForwardResolution("/administrator/activity/create.jsp");
     }
 
     public Resolution confirmCreate() {
         log.debug("confirmCreate(): " + activity);
         validateWeightCalories();
         activityService.create(activity);
         this.getContext().getMessages().add(new LocalizableMessage("activity.create.success", escapeHTML(activity.getActivityName().toString())));
         return new RedirectResolution("/administrator/activity/create.jsp");
     }
 
     public Resolution edit() {
         log.debug("edit(): " + activity);
         delete = false;
         return new ForwardResolution("/administrator/activity/edit.jsp");
     }
 
     public Resolution confirmEdit() {
         log.debug("confirmEdit(): " + activity);
         validateWeightCalories();
         activityService.update(activity);
         this.getContext().getMessages().add(new LocalizableMessage("activity.edit.success", escapeHTML(activity.getActivityName().toString())));
         return new ForwardResolution("/administrator/activity/message.jsp");
     }
 
     public Resolution delete() {
         log.debug("delete(): " + activity);
         delete = true;
         return new ForwardResolution("/administrator/activity/delete.jsp");
     }
 
     public Resolution confirmDelete() {
         log.debug("confirmDelete(): " + activity);
         delete = false;
         activityService.remove(activity.getActivityId());
         this.getContext().getMessages().add(new LocalizableMessage("activity.delete.success", escapeHTML(activity.getActivityName())));
         return new ForwardResolution("/administrator/activity/message.jsp");
     }
 
     public Resolution restore() {
         log.debug("restore(): " + activity);
         delete = true;
         return new ForwardResolution("/administrator/activity/restore.jsp");
     }
 
     public Resolution confirmRestore() {
         log.debug("confirmRestore(): " + activity);
         delete = false;
         activityService.remove(activity.getActivityId());
         this.getContext().getMessages().add(new LocalizableMessage("activity.restore.success", escapeHTML(activity.getActivityName())));
         return new ForwardResolution("/administrator/activity/message.jsp");
     }
 }
