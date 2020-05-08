 package com.muni.fi.pa165.actions.efficiencies;
 
 import com.muni.fi.pa165.actions.base.BaseActionBean;
import com.muni.fi.pa165.dao.service.MonsterWeaponService;
 import com.muni.fi.pa165.dto.MonsterWeaponDto;
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
  * Stripes ActionBean for handling monsterMonsterWeapon operations.
  *
  * @author Aubrey Oosthuizen
  */
 @UrlBinding("/efficiencies/list/{$event}/{monsterWeapon.monster.id}")
 public class MonsterWeaponActionBean extends BaseActionBean implements ValidationErrorHandler {
 
     final static Logger log = LoggerFactory.getLogger(MonsterWeaponActionBean.class);
     
     @SpringBean //Spring can inject even to private and protected fields
     protected MonsterWeaponService monsterMonsterWeaponService;
     
     //--- part for showing a list of monsterMonsterWeapons ----
     private List<MonsterWeaponDto> monsterMonsterWeapons;
 
     //@DontValidate
     @DefaultHandler
     public Resolution list() {
         log.debug("list()");
         monsterMonsterWeapons = monsterMonsterWeaponService.findByMonsterId(Long.parseLong("52"));
         return new ForwardResolution("/monsterMonsterWeapon/list.jsp");
     }
 
 //    public Resolution MonsterWeaponActionBean()
 //    {
 //        log.debug("default or constructor()");
 //        monsterMonsterWeapons = monsterMonsterWeaponService.findAll();
 //        return new ForwardResolution("/monsterMonsterWeapon/lolololol.jsp");
 //    }
     public List<MonsterWeaponDto> getMonsterWeapons() {
         return monsterMonsterWeapons;
     }
     //--- part for adding a monsterMonsterWeapon ----
     @ValidateNestedProperties(value = {
         @Validate(on = {"add", "save"}, field = "name", required = false),
         @Validate(on = {"add", "save"}, field = "monsterMonsterWeaponType", required = false), 
         @Validate(on = {"add", "save"}, field = "monsterMonsterWeaponClass", required = false, minvalue = 0),
         @Validate(on = {"add", "save"}, field = "range", required = false, minvalue = 0),
          @Validate(on = {"add", "save"}, field = "caliber", required = false, minvalue = 0),
          @Validate(on = {"add", "save"}, field = "rounds", required = false, minvalue = 0),
          @Validate(on = {"add", "save"}, field = "description", required = false, minvalue = 0)
     })
     private MonsterWeaponDto monsterMonsterWeapon;
     
 
     public Resolution add() {
         log.debug("add() monsterMonsterWeapon={}", monsterMonsterWeapon);
         getContext().getMessages().add(new SimpleMessage("Called method add"));
         try {
             monsterMonsterWeapon = monsterMonsterWeaponService.save(monsterMonsterWeapon);
         } catch (Exception ex) {
             getContext().getMessages().add(new SimpleMessage(ex.getMessage()));
            // getContext().getMessages().add(new LocalizableMessage("add.message", escapeHTML(monsterMonsterWeapon.getName()), escapeHTML(monsterMonsterWeapon.getDescription().toString())));
         
         }
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         //fill up the data for the table if validation errors occured
         monsterMonsterWeapons = monsterMonsterWeaponService.findAll();
         //return null to let the event handling continue
         return null;
     }
 
     public MonsterWeaponDto getMonsterWeapon() {
         return monsterMonsterWeapon;
     }
 
     public void setMonsterWeapon(MonsterWeaponDto monsterMonsterWeapon) {
         this.monsterMonsterWeapon = monsterMonsterWeapon;
     }
 
     //--- part for deleting a monsterMonsterWeapon ----
     public Resolution delete() {
          getContext().getMessages().add(new SimpleMessage("Called method delete"));
 //        log.debug("delete({})", monsterMonsterWeapon.getMonster());
 //        //only id is filled by the form
 //        monsterMonsterWeapon = monsterMonsterWeaponService.findById(monsterMonsterWeapon.getId());
 //        try {
 //            monsterMonsterWeaponService.delete(monsterMonsterWeapon.getId());
 //       } catch (Exception ex) {
 //           getContext().getMessages().add(new SimpleMessage(ex.getMessage()));
 //            getContext().getMessages().add(new LocalizableMessage("delete.message", escapeHTML(monsterMonsterWeapon.getName()), escapeHTML(monsterMonsterWeapon.getDescription().toString())));        
 //        }
         return new RedirectResolution(this.getClass(), "list");
     }
 
     //--- part for editing a monsterMonsterWeapon ----
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "save"})
     public void loadMonsterWeaponFromDatabase() {
 //        String ids = getContext().getRequest().getParameter("monsterMonsterWeapon.id");
 //        if (ids == null) return;
 //        monsterMonsterWeapon = monsterMonsterWeaponService.findById(Long.parseLong(ids));        
         getContext().getMessages().add(new SimpleMessage("Loaded monsterMonsterWeapon from DB"));
     }
 
     public Resolution edit() {
         log.debug("edit() monsterMonsterWeapon={}", monsterMonsterWeapon);
          getContext().getMessages().add(new SimpleMessage("Called method edit"));
         return new ForwardResolution("/monsterMonsterWeapon/edit.jsp");
     }
 
     public Resolution save() {
          getContext().getMessages().add(new SimpleMessage("Called method save"));
         log.debug("save() monsterMonsterWeapon={}", monsterMonsterWeapon);
         monsterMonsterWeaponService.update(monsterMonsterWeapon);
         return new RedirectResolution(this.getClass(), "list");
     }
 
     public Resolution cancel() {
          getContext().getMessages().add(new SimpleMessage("Called method cancel"));
         log.debug("cancel");
         return new RedirectResolution(this.getClass(), "list");
     }
 }
