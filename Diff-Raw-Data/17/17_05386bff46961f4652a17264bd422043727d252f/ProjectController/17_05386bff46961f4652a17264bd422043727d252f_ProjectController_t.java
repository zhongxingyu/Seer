 package com.jsmadja.wall.projectwall.web.controller;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.jsmadja.wall.projectwall.domain.Build;
 import com.jsmadja.wall.projectwall.domain.Project;
 import com.jsmadja.wall.projectwall.domain.ProjectStatus;
import com.jsmadja.wall.projectwall.domain.Software;
import com.jsmadja.wall.projectwall.domain.SoftwareAccess;
 import com.jsmadja.wall.projectwall.domain.Wall;
 import com.jsmadja.wall.projectwall.service.WallService;
 
 @Controller
 @RequestMapping("/project")
 public class ProjectController {
 
     private static final Logger LOG = Logger.getLogger(ProjectController.class.getName());
 
     private Wall wall;
 
     @Autowired
     WallService wallService;
 
     public ProjectController() {
         wall = new Wall("orange-vallee");
        wall.addSoftwareAccess(new SoftwareAccess(Software.HUDSON, "http://integration.wormee.orange-vallee.net:8080/hudson"));
        wall.addSoftwareAccess(new SoftwareAccess(Software.SONAR, "http://integration.wormee.orange-vallee.net:9000"));
         wall.refreshProjects();
         // wall.addSoftwareAccess(new SoftwareAccess(Software.HUDSON, "http://ci.awired.net/jenkins"));
         // wall.addSoftwareAccess(new SoftwareAccess(Software.SONAR, "http://sonar.awired.net"));
         // wall.addSoftwareAccess(new SoftwareAccess(Software.HUDSON, "http://fluxx.fr.cr:8080/hudson"));
         // wall.addSoftwareAccess(new SoftwareAccess(Software.SONAR, "http://fluxx.fr.cr:9000"));
     }
 
     @PostConstruct
     public void postConstruct() {
         wallService.addWall(wall);
     }
 
     @RequestMapping
     public @ResponseBody Collection<Project> getProjects() {
         Collection<Project> projects = wall.getProjects();
         LOG.info("Projects collection size :" + projects.size());
         return projects;
     }
 
     @RequestMapping("status")
     public @ResponseBody List<ProjectStatus> getStatus() {
         return wall.getStatus();
     }
 
     @RequestMapping("get")
     public @ResponseBody Project getProject(@RequestParam("projectName") String projectName) throws Exception {
         return wall.findProjectByName(projectName);
     }
 
     @RequestMapping("finishTime")
     public @ResponseBody Date getFinishTime(@RequestParam("projectName") String projectName) throws Exception {
         return wall.getEstimatedFinishTime(projectName);
     }
 
     @RequestMapping("build")
     public @ResponseBody Build getBuild(@RequestParam("projectName") String projectName, @RequestParam("buildId") int buildId) throws Exception {
         return wall.findBuildByProjectNameAndBuilderNumber(projectName, buildId);
     }
 
 }
