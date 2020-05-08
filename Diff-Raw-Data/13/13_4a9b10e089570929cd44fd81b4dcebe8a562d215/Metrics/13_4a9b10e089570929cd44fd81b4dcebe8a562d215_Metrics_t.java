 package com.reversemind.glia.server;
 
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * Measurable parameters for GliaServer
  */
 public class Metrics implements Serializable {
 
     private Date startDate;
     private long requests = 0L;
     private double timePerRequest = 0.0d;
     private double processingTime = 0.0d;
     private double cpuIdle;
 
    private Sigar sigar = null;
 
     public Metrics() {
         this.startDate = new Date();
        try{
            this.sigar = new Sigar();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        this.cpuIdle = 99.0d;
     }
 
     // TODO need to rename method
     public void addRequest(long deltaTimePerRequest) {
         synchronized (this) {
             requests++;
 
             this.processingTime += deltaTimePerRequest;
             if (this.requests > 0) {
                 this.timePerRequest = (this.processingTime / this.requests);
             }
 
             // just update CPU load
             this.getCpuIdle();
         }
     }
 
     public long elapsedTime() {
         return System.currentTimeMillis() - this.startDate.getTime();
     }
 
     public double getTimePerRequest() {
         return timePerRequest;
     }
 
     public void setTimePerRequest(double timePerRequest) {
         this.timePerRequest = timePerRequest;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public long getRequests() {
         return requests;
     }
 
     public double getProcessingTime() {
         return processingTime;
     }
 
     public void setProcessingTime(double processingTime) {
         this.processingTime = processingTime;
     }
 
     public double getCpuIdle() {
         try {
             this.cpuIdle = sigar.getCpuPerc().getIdle();
        } catch (Exception e) {
         }
         return cpuIdle;
     }
 
     public void setCpuIdle(double cpuIdle) {
         this.cpuIdle = cpuIdle;
     }
 
     @Override
     public String toString() {
         return "Metrics{" +
                 "startDate=" + startDate +
                 ", requests=" + requests +
                 ", timePerRequest=" + timePerRequest +
                 ", processingTime=" + processingTime +
                 ", cpuIdle=" + cpuIdle + "% " +
                 '}';
     }
 }
