 package cz.admin24.myachievo.connector.http.cmd;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.admin24.myachievo.connector.http.PersistentConnection;
 import cz.admin24.myachievo.connector.http.dto.WorkReport;
 
 public class CmdGetWorkReportFromHtml {
     private static final Logger    LOG        = LoggerFactory.getLogger(CmdGetWorkReportFromHtml.class);
     private static final String[]  PARAMS     = new String[] { "$USER_ID", "$START_DAY", "$START_MONTH", "$START_YEAR", "$END_DAY", "$END_MONTH", "$END_YEAR" };
     // "22 August 2013"
     private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
     // Locale.US);
     private final String           urlString  = "https://timesheet.trask.cz/achievo/dispatch.php?atkprevlevel=0&atkescape=&atknodetype=reports.hoursurvey&atkaction=report&projectid=&roleid=&organizationid=&departmentid=all&employer_id=all&userid=$USER_ID&functionlevelswitch=all&activityid=&startdate%5Bday%5D=$START_DAY&startdate%5Bmonth%5D=$START_MONTH&startdate%5Byear%5D=$START_YEAR&enddate%5Bday%5D=$END_DAY&enddate%5Bmonth%5D=$END_MONTH&enddate%5Byear%5D=$END_YEAR&startperiod=0&endperiod=0&remark=&outputType=0&col%5B%5D=activitydate&col%5B%5D=userid&col%5B%5D=phaseid&col%5B%5D=projectid&col%5B%5D=activityid&col%5B%5D=remark&col%5B%5D=time&col%5B%5D=functionlevel&col%5B%5D=employer_id&orderby%5B0%5D=activitydate&orderdirection%5B0%5D=asc&orderby%5B1%5D=&orderdirection%5B1%5D=asc&orderby%5B2%5D=&orderdirection%5B2%5D=asc&aggregate=0";
     private final Date             from;
     private final Date             to;
 
 
     public CmdGetWorkReportFromHtml(Date from, Date to) {
         this.from = from;
         this.to = to;
     }
 
 
     public List<WorkReport> execute(PersistentConnection connection) throws IOException {
         List<WorkReport> ret = new ArrayList<WorkReport>();
         Calendar cFrom = Calendar.getInstance();
         Calendar cTo = Calendar.getInstance();
         cFrom.setTime(from);
         cTo.setTime(to);
 
         String[] params = new String[] {
                 connection.getUserId(),
                 "" + cFrom.get(Calendar.DAY_OF_MONTH),
                 "" + (cFrom.get(Calendar.MONTH) + 1),
                 "" + cFrom.get(Calendar.YEAR),
                 "" + cTo.get(Calendar.DAY_OF_MONTH),
                 "" + (cTo.get(Calendar.MONTH) + 1),
                 "" + cTo.get(Calendar.YEAR)
         };
         LOG.trace("Work report params: {}", params.toString());
         URL url;
         try {
             url = new URL(StringUtils.replaceEach(urlString, PARAMS, params));
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(e);
         }
         String html = connection.doGet(url);
         LOG.trace("Loaded HTML:\n{}", html);
         String table = StringUtils.substringBetween(html, "<!-- records -->", "<!-- totals row -->");
         // there are probably no reports
         if (table == null) {
             return Collections.EMPTY_LIST;
         }
         String[] rows = StringUtils.substringsBetween(table, "<tr id=\"rl_1", "</tr>");
         for (String row : rows) {
             String[] cols = StringUtils.substringsBetween(row, "<td valign=\"top", "</td>");
 
             String id = "NOT_KNOWN";
             int base = 0;
             if (cols.length >= 12) {
                 // it has edit support, this record is RW for achievo
                 id = trim(StringUtils.substringBetween(cols[0], "hoursbase.id='", "'"));
                 base = 1;
             }
             Date date = parseDate(trim(StringUtils.substringAfter(cols[base + 1], ">")));
            String project = StringUtils.substringAfter(trim(StringUtils.substringAfter(cols[base + 2], ">")), ": ");
 
             String phase = trim(StringUtils.substringAfter(cols[base + 3], ">"));
             String activity = trim(StringUtils.substringAfter(cols[base + 5], ">"));
             String remark = trim(StringUtils.substringAfter(cols[base + 6], ">"));
             String time = trim(StringUtils.substringAfter(cols[base + 7], ">"));
             Integer hours = parseHours(time);
             Integer minutes = parseMinutes(time);
 
             WorkReport r = new WorkReport(id, date, project, phase, activity, remark, hours, minutes);
             ret.add(r);
         }
 
         LOG.debug("Found reports:\n{}", ret);
         return ret;
     }
 
 
     private String trim(String str) {
         return StringUtils.trim(str);
     }
 
 
     private Integer parseMinutes(String string) {
         try {
             return Integer.valueOf(StringUtils.substringAfter(string, ":"));
         } catch (NumberFormatException e) {
             LOG.error("Failed to parse minutes from '{}'", string, e);
         }
         return null;
     }
 
 
     private Integer parseHours(String string) {
         try {
             return Integer.valueOf(StringUtils.substringBefore(string, ":"));
         } catch (NumberFormatException e) {
             LOG.error("Failed to parse hours from '{}'", string, e);
         }
         return null;
     }
 
 
     private Date parseDate(String string) {
         try {
             return dateFormat.parse(string);
         } catch (ParseException e) {
             LOG.error("Failed to parse date '{}'", string, e);
         }
         return null;
     }
 }
