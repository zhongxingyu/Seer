 package ru.kpfu.quantum.spring.controller;
 
 import com.google.gson.Gson;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import ru.kpfu.quantum.service.integration.IntegrationService;
 import ru.kpfu.quantum.spring.entities.Project;
 import ru.kpfu.quantum.spring.entities.ProjectGroup;
 import ru.kpfu.quantum.spring.entities.User;
 import ru.kpfu.quantum.spring.repository.ProjectGroupRepository;
 import ru.kpfu.quantum.spring.repository.ProjectRepository;
 import ru.kpfu.quantum.spring.repository.UserRepository;
 import ru.kpfu.quantum.spring.utils.UserUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 
 @Controller
 public class WorkingController {
 
 
     @Autowired
     private ProjectGroupRepository projectGroupRepository;
 
     @Autowired
     private ProjectRepository projectRepository;
 
     @Autowired
     private UserRepository userRepository;
 
 
     @Autowired
     private IntegrationService integrationService;
 
     Gson gson = new Gson();
 
 
 
     enum Filters {All, Working, Archive}
 
     @RequestMapping("/")
     public String working(HttpServletRequest request) {
        List<ProjectGroup> groups = projectGroupRepository.findAllGroups();
         request.setAttribute("groups", groups);
         request.setAttribute("filters", Filters.values());
         return "working/working";
     }
 
     @RequestMapping(value = "/working/project-list", method = RequestMethod.GET)
     public String getProjectDataList(HttpServletRequest request,
                                      @RequestParam Long groupId,
                                      @RequestParam String filter) {
         List<Project> projects;
         if(filter.equals("All")){
             projects = projectGroupRepository.findOneFetchChildren(groupId).getProjects();
         }
         else{
             if(filter.equals("Working")){
                 projects = projectRepository.findAllByArchive(groupId, false);
             }
             else{
                 projects = projectRepository.findAllByArchive(groupId, true);
             }
         }
         request.setAttribute("records", projects);
         return "working/data-list";
     }
 
     @RequestMapping(value = "/working/group-list", method = RequestMethod.GET)
     public String getGroupDataList(HttpServletRequest request) {
         User currentUser = UserUtils.getUserFromSession(request);
         List<ProjectGroup> groups = projectGroupRepository.findAllGroupsOwnByUser(currentUser);
         request.setAttribute("records", groups);
         return "working/data-list";
     }
 
     @ResponseBody
     @RequestMapping(value = "/working/create-group", method = RequestMethod.POST)
     public String createGroup(HttpServletRequest request,
                               @RequestParam String groupName) {
         User currentUser = UserUtils.getUserFromSession(request);
         ProjectGroup group = new ProjectGroup(groupName, currentUser);
         projectGroupRepository.save(group);
         return String.valueOf(group.getId());
     }
 
     @ResponseBody
     @RequestMapping(value = "/working/create-project", method = RequestMethod.POST)
     public String createProject(@RequestParam Long groupId,
                                 @RequestParam String projectName) {
         Project project = new Project(projectName, "");
         Project saved = projectRepository.save(project);
         ProjectGroup group = projectGroupRepository.findOneFetchChildren(groupId);
         group.getProjects().add(saved);
         projectGroupRepository.save(group);
         return String.valueOf(project.getId());
     }
 
 
     @RequestMapping("/working/get-project")
     public String getCode(HttpServletRequest request,
                           @RequestParam Long projectId){
         Project project = projectRepository.findOne(projectId);
         request.setAttribute("project", project);
         final String imageName = project.getId() + ".png";
         request.setAttribute("imageName", imageName);
         return "working/project";
     }
 
     @ResponseBody
     @RequestMapping("/working/get-code")
     public String getCode(@RequestParam Long projectId){
         Project project = projectRepository.findOne(projectId);
         return project.getCode();
     }
 
 
     @ResponseBody
     @RequestMapping("/working/get-circuit")
     public String getCircuit(@RequestParam String code) throws IOException{
         final String circuit = integrationService.codeToCircuit(code);
         return circuit;
     }
 
     @ResponseBody
     @RequestMapping(value = "/working/save", method = RequestMethod.POST)
     public String save(@RequestParam Long projectId,
                           @RequestParam String code,
                           @RequestParam String typeOfEditor) throws IOException
     {
         Project project = projectRepository.findOne(projectId);
         if(typeOfEditor.equals("text")){
             project.setCode(code);
         }
         else{
             List<String> codes = (List<String>)gson.fromJson(integrationService.circuitToCode(code), List.class);
             String trueCode = codes.get(0);
             project.setCode(trueCode);
         }
         project.setCalculated(false);
         projectRepository.save(project);
         return "";
     }
 
     @RequestMapping(value = "/working/calculate", method = RequestMethod.POST)
     public String calculate(HttpServletRequest request,
                             @RequestParam Long projectId,
                             @RequestParam String code,
                             @RequestParam String typeOfEditor) throws IOException {
         Project project = projectRepository.findOne(projectId);
         project.setCalculated(true);
         final byte[] imageBytes;
         if(typeOfEditor.equals("text")){
             imageBytes = integrationService.codeToFile(code);
             project.setCode(code);
         }
         else{
             List<String> codes = (List<String>)gson.fromJson(integrationService.circuitToCode(code), List.class);
             String trueCode = codes.get(0);
             project.setCode(trueCode);
             imageBytes = integrationService.circuitToFile(code.substring(1, code.length() - 1));
             project.setCode(trueCode);
             System.out.println("first code = " + code);
             System.out.println("after = " + code.substring(1, code.length() - 1));
         }
         final Project saved = projectRepository.save(project);
         final String imageName = saved.getId() + ".png";
         final String imagePath = System.getProperty("jboss.server.data.dir") + "/quantum/media/" + imageName;
         File file = new File(imagePath);
         try(FileOutputStream fileOutputStream = new FileOutputStream(file, false)) {
             fileOutputStream.write(imageBytes);
             fileOutputStream.flush();
         }
         request.setAttribute("imageName", imageName);
         return "working/result";
     }
 
     @ResponseBody
     @RequestMapping(value = "/working/archive", method = RequestMethod.POST)
     public String archive(@RequestParam Long projectId)
     {
         Project project = projectRepository.findOne(projectId);
         project.setArchive(true);
         projectRepository.save(project);
         return "";
     }
 }
