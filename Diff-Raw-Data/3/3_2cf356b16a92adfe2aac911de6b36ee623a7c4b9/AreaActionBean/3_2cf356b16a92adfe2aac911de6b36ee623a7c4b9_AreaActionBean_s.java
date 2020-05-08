 package com.muni.fi.pa165.actions.area;
 
 import com.muni.fi.pa165.actions.base.BaseActionBean;
 import com.muni.fi.pa165.service.AreaService;
import com.muni.fi.pa165.service.MonsterService;
 import com.muni.fi.pa165.dto.AreaDto;
import com.muni.fi.pa165.dto.MonsterDto;
 import java.util.ArrayList;
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
  * Stripes ActionBean for handling area operations.
  *
  * @author Irina Sedyukova
  */
 @UrlBinding("/area/{$event}/{area.id}")
 public class AreaActionBean extends BaseActionBean implements ValidationErrorHandler {
 
     final static Logger log = LoggerFactory.getLogger(AreaActionBean.class);
     //--- part for showing a list of areas ----
     private List<AreaDto> areas;
 
     //@DontValidate
     @DefaultHandler
     public Resolution list() {
         log.debug("list()");
         areas = areaService.findAll();
         return new ForwardResolution("/area/list.jsp");
     }
 
 
     public List<AreaDto> getAreas() {
         return areas;
     }
     //--- part for adding a area ----
     @ValidateNestedProperties(value = {
         @Validate(on = {"add", "save"}, field = "id", required = true),
         @Validate(on = {"add", "save"}, field = "name", required = true),
         @Validate(on = {"add", "save"}, field = "terrain", required = true), 
         @Validate(on = {"add", "save"}, field = "description", required = false, minvalue = 0),
       
             
     })
     private AreaDto area;
     @SpringBean //Spring can inject even to private and protected fields
     protected AreaService areaService;
 
     public Resolution add() {
         log.debug("add() area={}", area);
 
 
         try {
             area = areaService.save(area);
         } catch (Exception ex) {
             getContext().getMessages().add(new SimpleMessage(ex.getMessage()));
 
         }
         getContext().getMessages().add(new LocalizableMessage("area.add.message", escapeHTML(area.getName())));
 
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         //fill up the data for the table if validation errors occured
 //        areas = areaService.findAll();
         //return null to let the event handling continue
         return null;
     }
 
     public AreaDto getArea() {
         return area;
     }
 
     public void setArea(AreaDto area) {
         this.area = area;
     }
 
     //--- part for deleting a area ----
     public Resolution delete() {
         log.debug("delete({})", area.getId());
         //only id is filled by the form
 //        area = areaService.findById(area.getId());
 //        try {
 //
 //            areaService.delete(area.getId());
 //        } catch (Exception ex) {
 //            getContext().getMessages().add(new SimpleMessage(ex.getMessage()));
 //
 //        }
 //        getContext().getMessages().add(new LocalizableMessage("area.delete.message", escapeHTML(area.getName()));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     //--- part for editing a area ----
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "save"})
     public void loadAreaFromDatabase() {
         String ids = getContext().getRequest().getParameter("area.id");
         if (ids == null) {
             return;
         }
         area = areaService.findById(Long.parseLong(ids));
         
         getContext().getMessages().add(new SimpleMessage("Loaded area from DB"));
     }
 
     public Resolution edit() {
         log.debug("edit() area={}", area);
         return new ForwardResolution("/area/edit.jsp");
     }
 
     public Resolution save() {
         log.debug("save() area={}", area);
         areaService.update(area);
         return new RedirectResolution(this.getClass(), "list");
     }
 
     public Resolution select() {
         log.debug("select() area={}", area);
         this.setArea(areaService.findById(Long.parseLong(getContext().getRequest().getParameter("area.id"))));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     public Resolution cancel() {
         log.debug("cancel");
         return new RedirectResolution("/area/list.jsp");
     }
 
     @Override
     public void preBind() {
         String idStr = getContext().getRequest().getParameter("area.id");
 
         if (idStr == null) {
             this.area = new AreaDto();
         } else {
             this.area = areaService.findById(Long.parseLong(idStr));
         }
     }
 
     public List<Object> getBoundObjects() {
         List<Object> lst = new ArrayList<Object>();
 
         lst.add(getArea());
 
         return lst;
     }
 }
