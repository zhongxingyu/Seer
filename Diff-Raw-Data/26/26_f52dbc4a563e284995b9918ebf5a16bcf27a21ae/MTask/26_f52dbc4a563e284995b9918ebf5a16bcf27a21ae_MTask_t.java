 
 public class MTask {
     static int idcount = 0;
     public final int id;
     public String execln;
     public final String tid;
     public TASKTYPE ttype;
     public String tname;
     public int flength;
     public byte[] binary;
     public int port = 0;
     public String taskEngine = "none";
     public TASKSTATE status;
     public long start;
     public long finish;
     public String cost = "unknown";
    public String output = "";
     public String owner;
     
     public MTask(Task t){
         tname = t.tname;
         ttype = t.ttype;
         flength = t.flength;
         binary = t.binary;
         id = ++idcount;
         tid = String.valueOf(id);
     }
     
 }
