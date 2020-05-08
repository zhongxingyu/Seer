 package com.muni.fi.pa165.actions.monster;
 
 import com.muni.fi.pa165.actions.base.BaseActionBean;
 import static com.muni.fi.pa165.actions.base.BaseActionBean.escapeHTML;
 import com.muni.fi.pa165.service.MonsterService;
 import com.muni.fi.pa165.dto.MonsterDto;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationErrorHandler;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 
 /**
  * Stripes ActionBean for handling monster operations.
  *
  * @author Aubrey Oosthuizen
  */
 @UrlBinding("/monster/{$event}/{monster.id}")
 public class MonsterActionBean extends BaseActionBean implements ValidationErrorHandler {
 
     final static Logger log = LoggerFactory.getLogger(MonsterActionBean.class);
     
     @SpringBean //Spring can inject even to private and protected fields
     protected MonsterService monsterService;
     
     //--- part for showing a list of monsters ----
     private List<MonsterDto> monsters;
 
     //@DontValidate
     @DefaultHandler
     public Resolution list() {
         log.debug("list()");
         monsters = monsterService.findAll();
         return new ForwardResolution("/monster/list.jsp");
     }
 
 //    public Resolution MonsterActionBean()
 //    {
 //        log.debug("default or constructor()");
 //        monsters = monsterService.findAll();
 //        return new ForwardResolution("/monster/lolololol.jsp");
 //    }
     public List<MonsterDto> getMonsters() {
         return monsters;
     }
     
     
     //--- part for adding a monster ----
     @ValidateNestedProperties(value = {
         @Validate(on = {"add", "save"}, field = "name", required = false, maxlength = 255),
         @Validate(on = {"add", "save"}, field = "stamina", required = false, maxlength = 10), 
         @Validate(on = {"add", "save"}, field = "height", required = false, minvalue = 10),
         @Validate(on = {"add", "save"}, field = "strength", required = false, minvalue = 10),
          @Validate(on = {"add", "save"}, field = "agility", required = false, minvalue = 10),
          @Validate(on = {"add", "save"}, field = "dangerLevel", required = false, minvalue = 10),
          @Validate(on = {"add", "save"}, field = "weight", required = false, minvalue = 10),
          @Validate(on = {"add", "save"}, field = "description", required = false, minvalue = 255),    
          @Validate(on = {"add", "save"}, field = "imagePath", required = false, minvalue = 255)
             
     })
     private MonsterDto monster;
     
 
     public Resolution add() {
         log.debug("add() monster={}", monster);
         try {
             if (monster.getImagePath()== null || monster.getImagePath().isEmpty())
             {
                 monster.setImagePath("http://careerconfidential.com/images/no-monsterX400.jpg");
             }
             monster = monsterService.save(monster);
             
         } catch (Exception ex) {
             getContext().getMessages().add(new SimpleMessage(ex.getMessage()));
         }
         getContext().getMessages().add(new LocalizableMessage("add.message", escapeHTML(monster.getName()), escapeHTML(monster.getDescription())));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         //fill up the data for the table if validation errors occured
         monsters = monsterService.findAll();
         //return null to let the event handling continue
         return null;
     }
 
     public MonsterDto getMonster() {
         return monster;
     }
 
     public void setMonster(MonsterDto monster) {
         this.monster = monster;
     }
 
    //--- part for deleting a monster ----
    public Resolution delete() {
         log.debug("delete({})", monster.getId());
        //only id is filled by the form
       try {
            monster = monsterService.findById(monster.getId());
        
            monsterService.delete(monster.getId());
        } catch (Exception ex) {
            getContext().getMessages().add(new SimpleMessage(ex.getMessage()));
            getContext().getMessages().add(new LocalizableMessage("delete.message", escapeHTML(monster.getName()), escapeHTML(monster.getAgility().toString())));        
       }
        return new RedirectResolution(this.getClass(), "list");
     }
 
     //--- part for editing a monster ----
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "save"})
     public void loadMonsterFromDatabase() {
         String ids = getContext().getRequest().getParameter("monster.id");
         if (ids == null) return;
         monster = monsterService.findById(Long.parseLong(ids));        
         getContext().getMessages().add(new SimpleMessage("Loaded monster from DB"));
     }
 
     public Resolution edit() {
         log.debug("edit() monster={}", monster);
         return new ForwardResolution("/monster/edit.jsp");
     }
     
         public Resolution gallery() {
         log.debug("edit() monster={}", monster);
         return new ForwardResolution("/monster/gallery.jsp");
     }
 
     public Resolution save() {
         log.debug("save() monster={}", monster);
         monster = getMonster();
         monsterService.update(monster);
         return new RedirectResolution(this.getClass(), "list");
     }
 
     public Resolution cancel() {
         log.debug("cancel");
         return new RedirectResolution(this.getClass(), "list");
     }
 }
