 package jp.gr.java_conf.neko_daisuki.fsyscall;
 
 public class Pid {
 
    private int mPid;
 
     public Pid(int pid) {
         mPid = pid;
     }
 
     public Pid(Pid pid) {
         mPid = pid.getInteger();
     }
 
     public int getInteger() {
         return mPid;
     }
 
     public void setInteger(int pid) {
         mPid = pid;
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
