 /*
  * $Id$
  * Copyright (c) 2004
  * Jukka Lindstrm
  */
 package com.laughingpanda.jira;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 
 import org.apache.log4j.Category;
 import org.apache.log4j.Priority;
 import org.jfree.chart.ChartRenderingInfo;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.entity.StandardEntityCollection;
 
 import com.atlassian.jira.config.properties.ApplicationProperties;
 import com.atlassian.jira.portal.PortletConfiguration;
 import com.atlassian.jira.portal.PortletImpl;
 import com.atlassian.jira.project.version.Version;
 import com.atlassian.jira.project.version.VersionManager;
 import com.atlassian.jira.security.JiraAuthenticationContext;
 import com.atlassian.jira.security.PermissionManager;
 import com.atlassian.jira.security.Permissions;
 
 /**
  * 
  * @author Jukka Lindstrom
  * @author Markus Hjort
  */
 public class ChartPortlet extends PortletImpl {
 
     private static final int IMAGE_CREATION_INTERVAL = 60 * 1000;
     private final static Category log = Category.getInstance(ChartPortlet.class);
     private final VersionManager versionManager;
     private final VersionHistoryChartFactory chartService;
 
     public ChartPortlet(JiraAuthenticationContext authenticationContext, VersionWorkloadHistoryManager manager, VersionManager versionManager, PermissionManager permissionManager, ApplicationProperties properties) {
         super(authenticationContext, permissionManager, properties);
         this.versionManager = versionManager;
         this.chartService = new VersionHistoryChartFactory(manager);
     }
 
     public Map getVelocityParams(PortletConfiguration config) {
         if (config == null) throw new IllegalArgumentException("PortletConfiguration cannot be null.");
         Map model = super.getVelocityParams(config);
 
         int width = 500;
         int height = 300;
         Long versionId = null;
         Date startDate = null;
         try {
             width = config.getLongProperty("chart.width").intValue();
             height = config.getLongProperty("chart.height").intValue();
             versionId = config.getLongProperty("versionId");
            String date = config.getProperty("startDate");
            if (date != null) startDate = getIsoDateFormatter().parse(date);
             else startDate = new Date(0);
         } catch (Exception e) {
             throw new RuntimeException("Error in portlet configuration.", e);
         }
         if (versionId == null) return error(model, "Version (" + versionId + ") is not available.");
         if (versionId.longValue() < 0l) return error(model, "Please, choose a version. Full Projects are not supported");
 
         Collection browsableProjects = permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getUser());
 
         Version version = versionManager.getVersion(versionId);
         if (version == null) return error(model, "Version (" + versionId + ") is not available.");
         if (!browsableProjects.contains(version.getProject())) return error(model, "You don't have correct privileges to view this data.");
 
         try {
             ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
             createTempDir();
             File imageFile = new File(new File(System.getProperty("java.io.tmpdir")), createFileName(width, height, versionId, startDate));
             if (createNewImage(imageFile)) {
                 JFreeChart chart = chartService.makeChart(version, startDate);
                 saveImage(imageFile, width, height, chart, info);
             }
 
             StringWriter imageMap = new StringWriter();
             ChartUtilities.writeImageMap(new PrintWriter(imageMap), imageFile.getName(), info, true);
             model.put("chartFilename", imageFile.getName());
             model.put("imageMapName", imageFile.getName());
             model.put("imageMap", imageMap.toString());
             model.put("versionId", versionId.toString());
         } catch (Exception e) {
             log.log(Priority.ERROR, e);
             throw new RuntimeException(e);
         }
         return model;
     }
 
     private static Map error(Map model, String string) {
         model.put("errorMessage", string);
         return model;
     }
 
     private SimpleDateFormat getIsoDateFormatter() {
         return new SimpleDateFormat("yyyy-MM-dd");
     }
 
     private String createFileName(int width, int height, Long versionId, Date startDate) {
         return "public" + versionId + "-" + getIsoDateFormatter().format(startDate) + "-" + width + "x" + height + ".png";
     }
 
     private boolean createNewImage(File imageFile) {
         return !imageFile.exists() || imageFile.lastModified() < (System.currentTimeMillis() - IMAGE_CREATION_INTERVAL);
     }
 
     private void saveImage(File imageFile, int width, int height, JFreeChart chart, ChartRenderingInfo info) throws IOException {
         ChartUtilities.saveChartAsPNG(imageFile, chart, width, height, info);
     }
 
     private void createTempDir() {
         String tempDirName = System.getProperty("java.io.tmpdir");
         if (tempDirName == null) throw new RuntimeException("Temporary directory system property (java.io.tmpdir) is null");
         File tempDir = new File(tempDirName);
         if (!tempDir.exists()) tempDir.mkdirs();
     }
 
 }
