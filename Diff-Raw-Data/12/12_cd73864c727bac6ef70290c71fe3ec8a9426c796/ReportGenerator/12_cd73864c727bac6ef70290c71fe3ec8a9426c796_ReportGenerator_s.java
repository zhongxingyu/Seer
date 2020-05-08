 package com.abperf;
 
 import java.util.*;
 import models.Domain;
 import models.Domain.DomainAccess;
 import models.PageView;
 import models.Project;
 import play.Play;
 import play.libs.IO;
 import play.templates.TemplateLoader;
 
 public class ReportGenerator {
     final private Project project;
 
     final private static String templateDir = "app/views/report/";
 
     public ReportGenerator(final Project project) {
         this.project = project;
     }
 
     public void execute() {
         project.reportGeneratedAt = new Date();
         project.reportPageViews = project.pageViews;
         project.reportOutputDir = "reports/"
                 + project.id + "-"
                 + project.pageViews + "-"
                 + project.getUUID() + "-"
                 + System.currentTimeMillis() + "/";
 
         Play.getFile(project.reportOutputDir).mkdirs();
 
         renderIndex();
         renderDomainsJSON();
         renderPageViewsJSON();
         IO.copyDirectory(Play.getFile(templateDir + "js/"), Play.getFile(project.reportOutputDir + "js/"));
         IO.copyDirectory(Play.getFile(templateDir + "css/"), Play.getFile(project.reportOutputDir + "css/"));
        IO.copyDirectory(Play.getFile(templateDir + "images/"), Play.getFile(project.reportOutputDir + "images/"));
 
         project.save();
     }
 
     private void renderIndex() {
         final Map<String, Object> renderArgs = new HashMap<String, Object>(3);
         renderArgs.put("project", project);
 
         _writeToOutputFile("index.html", _renderTemplate("index.html", renderArgs));
     }
 
     private void renderDomainsJSON() {
         final List<Domain> domains = Domain.find("project = ? and access = ?", project, DomainAccess.PUBLIC).fetch();
         _writeToOutputFile("public-domains.json", Domain.toJSONarray(domains).toString());
     }
 
     private void renderPageViewsJSON() {
         final List<PageView> views = PageView.find("project = ?", project).fetch();
         _writeToOutputFile("page-views.json", PageView.toJSONarray(views).toString());
     }
 
     private String _renderTemplate(final String filename, final Map<String, Object> renderArgs) {
         return TemplateLoader.load(templateDir + filename).render(renderArgs);
     }
 
     private void _writeToOutputFile(final String filename, final String content) {
         IO.writeContent(content, Play.getFile(project.reportOutputDir + filename));
     }
 }
