 package org.sukrupa.app.admin;
 
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.sukrupa.app.admin.talents.TalentForm;
 import org.sukrupa.app.admin.talents.TalentsService;
 import org.sukrupa.app.students.TalentValidator;
 import org.sukrupa.student.Student;
 import org.sukrupa.student.Talent;
 import org.sukrupa.student.TalentRepository;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import static java.lang.String.format;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 @Controller
 @RequestMapping("/admin/talents")
 public class TalentsController {
 
 
     private TalentsService talentsService;
     private TalentRepository talentRepository;
 
     @Autowired
     public TalentsController(TalentsService talentsService, TalentRepository talentRepositoryIn) {
         this.talentsService = talentsService;
         this.talentRepository = talentRepositoryIn;
     }
 
     @RequestMapping
     public String list(Map<String, Object> model, TalentForm talentParam){
         return "admin/talents/new";
     }
 
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newTalent(){
         return "admin/talents/new";
     }
 
 
 
     @RequestMapping(value="new", method = RequestMethod.POST)
     public String saveNewTalent(
 
     @ModelAttribute("createTalent") TalentForm talentParam, Map<String, Object> model){
               String trimmedDescription = talentParam.getDescription().trim();
               if(!trimmedDescription.isEmpty())
               {
                   List<String> talentsInDatabase = talentRepository.returnTalentDescriptionsInList(talentRepository.findAllTalents());
                  if(!talentsInDatabase.contains(talentParam.getDescription()))
                   {
                      talentsService.create(talentParam);
                      model.put("talentAddedSuccesfully", true);
                      model.put("talentDescription", talentParam.getDescription());
                   }
                   else
                   {
                       model.put("talentDuplicated", true);
                   }
               }
               else{
                   model.put("talentInvalid", true);
               }
            return "admin/talents/new";
     }
     @RequestMapping()
     public String create(TalentForm talentForm) {
         this.talentsService.create(talentForm) ;
         return null;
     }
 }
