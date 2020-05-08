 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cn.orz.pascal.gfmonitor.controller;
 
 import cn.orz.pascal.gfmonitor.model.monitor.SystemMonitor;
 import cn.orz.pascal.gfmonitor.models.entity.system.SystemLog;
 import java.util.ArrayList;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 /**
  *
  * @author pascalm3
  */
 @ManagedBean
 @RequestScoped
 public class SummaryMonitorView {

     private List<SystemLog> systemLogs;
     
     /** Creates a new instance of SummaryMonitorView */
     public SummaryMonitorView() {
         this.systemLogs = new ArrayList<SystemLog>();
        this.systemLogs.add(new SystemLog() {{
            this.setHeapMemoryUsageUsed(100L);

        }});
        this.systemLogs.add(new SystemLog() {{
            this.setHeapMemoryUsageUsed(200L);

        }});
        this.systemLogs.add(new SystemLog() {{
            this.setHeapMemoryUsageUsed(300L);

        }});
     }
 
     public List<SystemLog> getSystemLogs() {
        return systemLogs;
     }
 
     public void setSystemLogs(List<SystemLog> systemLogs) {
         this.systemLogs = systemLogs;
     }
 
 }
