 package cz.admin24.myachievo.connector.http.cmd;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.admin24.myachievo.connector.http.PersistentConnection;
 import cz.admin24.myachievo.connector.http.dto.Project;
 
 public class CmdGetProjects {
     private static final Logger    LOG                  = LoggerFactory.getLogger(CmdGetProjects.class);
 
     private static final String    allProjectsUrlString = "https://timesheet.trask.cz/achievo/dispatch.php?atknodetype=project.projectselector&atkaction=select&atktarget=_15Batkprimkey_15D";
     private static final Pattern   TR_PATTERN           = Pattern.compile("(?ms)<tr id=\"select_.*?</tr>");
     private static final Pattern   TD_PATTERN           = Pattern.compile("(?ms)<td[^>]*>(.*?)</td>");
     private static final Pattern   PROJECT_ID_PATTERN   = Pattern.compile("(?ms)\\['select'\\]\\[([0-9]+)\\]\\['select'\\] = 'project.id%3D%27(.*?)%27';");
     private final SimpleDateFormat dateFormat           = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
     private final URL              allProjectsUrl;
 
 
     public CmdGetProjects() {
         try {
             allProjectsUrl = new URL(allProjectsUrlString);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
 
     public List<Project> execute(PersistentConnection connection) throws IOException {
         List<Project> ret = new ArrayList<Project>();
         String response = connection.doGet(allProjectsUrl);
         // find project ids
         Matcher m = PROJECT_ID_PATTERN.matcher(response);
         Map<String, String> projectIds = new HashMap<String, String>();
         while (m.find()) {
             projectIds.put(m.group(1), m.group(2));
         }
 
         // extract projects
         Matcher trMatcher = TR_PATTERN.matcher(response);
         Integer counter = 0;
         while (trMatcher.find()) {
             CharSequence row = response.subSequence(trMatcher.start(), trMatcher.end());
             LOG.trace("row: \n{}", row);
             Matcher tdMatcher = TD_PATTERN.matcher(row);
             // skip first
             tdMatcher.find();
 
             String id, code, name, coordinator;
             Date begin, end;
             id = projectIds.get(counter.toString());
 
             tdMatcher.find();
             code = tdMatcher.group(1).trim();
             tdMatcher.find();
             name = tdMatcher.group(1).trim();
             tdMatcher.find();
             tdMatcher.find();
             coordinator = tdMatcher.group(1).trim();
             tdMatcher.find();
             begin = parseDate(tdMatcher.group(1).trim());
             tdMatcher.find();
             end = parseDate(tdMatcher.group(1).trim());
 
             Project project = new Project(id, code, name, coordinator, begin, end);
             LOG.debug("Found project:\n{}", project);
 
             counter++;
             ret.add(project);
         }
 
         return ret;
     }
 
 
     private Date parseDate(String date) {
         try {
             return dateFormat.parse(date);
         } catch (ParseException e) {
             LOG.error("Failed to parse date '{}'", date, e);
             return null;
         }
     }
 }
