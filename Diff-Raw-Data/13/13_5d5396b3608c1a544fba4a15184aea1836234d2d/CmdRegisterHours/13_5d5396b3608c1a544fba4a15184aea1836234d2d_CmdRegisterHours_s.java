 package cz.admin24.myachievo.connector.http.cmd;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.admin24.myachievo.connector.http.PersistentConnection;
 
 public class CmdRegisterHours {
     private static final Logger       LOG               = LoggerFactory.getLogger(CmdRegisterHours.class);
     private final String              dispatchUrlString = "https://timesheet.trask.cz/achievo/dispatch.php?";
     private final URL                 dispatchUrl;
     private final Map<String, String> params            = new HashMap<String, String>();
 
 
    public CmdRegisterHours(Date day, Integer hours, Integer minutes, String projectId, String phaseId, String activityId, String remark) {
         try {
             dispatchUrl = new URL(dispatchUrlString);
         } catch (MalformedURLException e) {
             throw new IllegalStateException("Invalid url", e);
         }
 
         Calendar c = Calendar.getInstance();
         c.setTime(day);
         // params.put("atklevel", "1");
         // params.put("atkprevlevel", "0");
         // params.put("atkstackid", "52163bc221967");
         // params.put("atkreturnbehaviour", "0");
         // params.put("achievo", "rvl46hd624ec0au84dejl2n7j4");
         // params.put("atkescape", "");
         params.put("atkaction", "save");
         params.put("atkprevaction", "admin");
         params.put("atkfieldprefix", "");
         params.put("atknodetype", "timereg.hours");
         params.put("atkprimkey", "");
        params.put("userid", "person.id='586'");
         params.put("entrydate[year]", "" + c.get(Calendar.YEAR));
         params.put("entrydate[month]", "" + (1 + c.get(Calendar.MONTH)));
         params.put("entrydate[day]", "" + (c.get(Calendar.DAY_OF_MONTH)));
         params.put("activitydate[day]", "" + (c.get(Calendar.DAY_OF_MONTH)));
         params.put("activitydate[month]", "" + (1 + c.get(Calendar.MONTH)));
         params.put("activitydate[year]", "" + c.get(Calendar.YEAR));
         params.put("projectid", "project.id='" + projectId + "'");
         params.put("phaseid", "phase.id='" + phaseId + "'");
         params.put("activityid", "activity.id='" + activityId + "'");
         params.put("remark", remark);
         params.put("time[hours]", "" + hours);
         params.put("time[minutes]", "" + minutes);
         params.put("atksaveandclose", "Save and close");
     }
 
 
     public String execute(PersistentConnection connection) throws IOException {
         LOG.debug("Registering hours: {}", params);
         return connection.doPost(params, dispatchUrl);
     }
 }
