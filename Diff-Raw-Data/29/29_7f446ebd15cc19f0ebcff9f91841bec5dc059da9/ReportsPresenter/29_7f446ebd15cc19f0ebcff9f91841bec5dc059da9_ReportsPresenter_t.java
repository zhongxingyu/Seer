 package com.vaadin.training.bugrap.view.reports;
 
 import com.vaadin.training.bugrap.domain.entity.*;
 import com.vaadin.training.bugrap.service.ReportQuery;
 import com.vaadin.training.bugrap.service.ReportService;
 import com.vaadin.training.bugrap.view.mvp.Presenter;
 import com.vaadin.training.bugrap.view.mvp.View;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ReportsPresenter extends Presenter {
 
     private Project currentProject;
     private ProjectVersion currentVersion;
 
     @Inject
     private ReportService reportService;
 
     @Override
     public void viewEntered(String params) {
         Project project = reportService.findProject();
 
         currentProject = project;
         currentVersion = new ProjectVersion();
         currentVersion.setVersion("");
         currentVersion.setProject(project);
 
         getView().showProject(project);
         getView().showReports(reportService.getReports(null));
     }
 
     public void projectVersionChanged(String version) {
         ReportQuery reportQuery = new ReportQuery();
         ProjectVersion projectVersion = new ProjectVersion();
         projectVersion.setProject(currentProject);
         projectVersion.setVersion(version);
         reportQuery.setVersion(projectVersion);
 
         currentVersion = projectVersion;
 
         List<Report> reports = reportService.getReports(reportQuery);
 
         getView().showReports(reports);
     }
 
     public void reportsStatusFilterChanged(List<ReportStatus> statuses) {
         ReportQuery reportQuery = new ReportQuery();
         reportQuery.setStatuses(statuses);
         reportQuery.setVersion(currentVersion);
 
         List<Report> reports = reportService.getReports(reportQuery);
 
         getView().showReports(reports);
     }
 
     public void reportsCustomFilterChanged(List<ReportStatus> statuses, ArrayList<ReportResolution> reportResolutions) {
         //TODO
     }
 
     @Override
     public ReportsOverviewView getView() {
         return (ReportsOverviewView) super.getView();
     }
 }
