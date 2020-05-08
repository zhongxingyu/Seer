 package nl.surfnet.bod.web;
 
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.util.Collection;
 import java.util.List;
 
 import nl.surfnet.bod.domain.Institution;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
 import nl.surfnet.bod.service.InstitutionService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 
 @Controller
 public class InstitutionController {
 
   private InstitutionService institutionService;
 
   private PhysicalResourceGroupRepo physicalResourceGroupRepo;
 
   @Autowired
   public InstitutionController(InstitutionService institutionService,
       PhysicalResourceGroupRepo physicalResourceGroupRepo) {
     this.institutionService = institutionService;
     this.physicalResourceGroupRepo = physicalResourceGroupRepo;
   }
 
   @RequestMapping(value = "/institutions", method = RequestMethod.GET, headers = "accept=application/json")
   public @ResponseBody Collection<Institution> jsonList(@RequestParam(required = false) String q) {
     final Collection<String> existingInstitutions = existingInstitutionNames();
    final String query = StringUtils.hasText(q) ? q.toLowerCase() : "";
 
     return filter(institutionService.getInstitutions(), new Predicate<Institution>() {
       @Override
       public boolean apply(Institution input) {
         String institutionName = input.getName().toLowerCase();
 
         return !existingInstitutions.contains(institutionName) && institutionName.contains(query);
       }
     });
   }
 
   private Collection<String> existingInstitutionNames() {
     List<PhysicalResourceGroup> groups = physicalResourceGroupRepo.findAll();
 
     return newArrayList(transform(groups, new Function<PhysicalResourceGroup, String>() {
       @Override
       public String apply(PhysicalResourceGroup input) {
         return input.getInstitutionName().toLowerCase();
       }
     }));
   }
 
   @RequestMapping(value = "/institutions", method = RequestMethod.GET)
   public String list(final Model uiModel) {
     Collection<Institution> institutions = institutionService.getInstitutions();
 
     uiModel.addAttribute("institutions", institutions);
 
     return "institutions/list";
   }
 
 }
