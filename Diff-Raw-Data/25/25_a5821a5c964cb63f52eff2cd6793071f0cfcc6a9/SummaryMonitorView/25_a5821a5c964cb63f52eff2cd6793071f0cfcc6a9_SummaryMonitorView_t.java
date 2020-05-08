 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cn.orz.pascal.gfmonitor.controller;
 
import cn.orz.pascal.gfmonitor.model.dao.system.SystemLogFacade;
 import cn.orz.pascal.gfmonitor.model.monitor.SystemMonitor;
 import cn.orz.pascal.gfmonitor.models.entity.system.SystemLog;
 import java.util.ArrayList;
 import java.util.List;
import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 /**
  *
  * @author pascalm3
  */
 @ManagedBean
 @RequestScoped
 public class SummaryMonitorView {
    @EJB
    private SystemLogFacade systemLogFacade; 
     private List<SystemLog> systemLogs;
     
     /** Creates a new instance of SummaryMonitorView */
     public SummaryMonitorView() {
         this.systemLogs = new ArrayList<SystemLog>();
     }
 
     public List<SystemLog> getSystemLogs() {
        this.systemLogs = systemLogFacade.findAll();
        System.out.println("step1");
        return this.systemLogs;
     }
 
     public void setSystemLogs(List<SystemLog> systemLogs) {
         this.systemLogs = systemLogs;
     }
 
 }
