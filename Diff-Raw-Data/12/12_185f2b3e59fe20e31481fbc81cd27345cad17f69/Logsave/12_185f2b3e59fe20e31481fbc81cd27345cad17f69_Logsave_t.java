 package sirilog;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Logsave extends JavaPlugin {
     
     public static ArrayList<String[]> logs = new ArrayList<>();
     
     public void savebl(String pl, String a, String bl, String x, String y, String z, String w) throws IOException {
         Calendar cal = Calendar.getInstance();
         String da, mo, ye, h, m, s;
         da  = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        mo  = String.valueOf(cal.get(Calendar.MONTH) + 1);
         ye  = String.valueOf(cal.get(Calendar.YEAR));
         h   = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
         m   = String.valueOf(cal.get(Calendar.MINUTE));
         s   = String.valueOf(cal.get(Calendar.SECOND));
         logs.add(new String[]{da,mo,ye,h,m,s,pl,a,bl,x,y,z,w});
         if (logs.size() >= Configs.config.getInt("recordings.savelimit")){
             finalsave();
         }
     }
 
     public boolean finalsave() throws IOException{
         if (logs.size() <= 0){
             return true;
         }
         switch (Configs.config.getString("recordings.mode")) {
             case "file":
             {
                 Integer s = logs.size();
                 File sirilog = new File("plugins" + File.separator + "SiriLog" + File.separator + "sirilog.log");
                 FileWriter fw = new FileWriter(sirilog, true);
                 try (PrintWriter pw = new PrintWriter(fw)) {
                     Iterator it = logs.iterator();
                     while(it.hasNext()) {
                        String[] line = (String[]) it.next();
                        pw.println(
                                "[" + line[0] + "/" + line[1] + "/" + line[2] +
                                " " + line[3] + ":" + line[4] + ":" + line[5] +
                                "] " + line[6] + " " + line[7] + " " + line[8] +
                                " X=" + line[9] + " Y=" + line[10] + " Z=" + line[11] + " " + line[12]
                        );
                     }
                 }
                 logs.clear();
                 if(Configs.config.getBoolean("recordings.showlogsaved")){
                     System.out.println("[SiriLog] Saved " + s + " log lines");
                 }
                 break;
             }
             case "database":
             {
                 Integer s = logs.size();
                 if (new MySQL().writeData(logs)){
                     logs.clear();
                     if(Configs.config.getBoolean("recordings.showlogsaved")){
                         System.out.println("[SiriLog] Saved " + s + " log lines");
                     }
                 }else{
                     return false;
                 }
                 break;
             }
             
         }
         return true;
     }
 }
