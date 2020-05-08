 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.Map;
 
 import org.jdom2.Document;
 import org.jdom2.JDOMException;
 
 import edu.stanford.services.explorecourses.Course;
 import edu.stanford.services.explorecourses.Department;
 import edu.stanford.services.explorecourses.MeetingSchedule;
 import edu.stanford.services.explorecourses.Instructor;
 import edu.stanford.services.explorecourses.School;
 import edu.stanford.services.explorecourses.Section;
 import edu.stanford.services.explorecourses.ExploreCoursesConnection;
 
 /** Prints a list of all courses offered at Stanford in the current academic year **/
 public class FetchCourses {
   public static void main(String[] args) throws IOException, JDOMException {
     init();
     ExploreCoursesConnection connection = new ExploreCoursesConnection();
 
     for(School s : connection.getSchools()) {
       for(Department d : s.getDepartments()) {
         if (d.getCode().equals("ENVRINST")) continue; // This one breaks things.
         writeDepartmentClassesToFile(d, connection);
       }
     }
   }
 
   private static void init() {
     List<String> s = permissibleClassComponents;
     s.add("LEC"); // Lecture
     s.add("SEM"); // Seminar
     s.add("DIS"); // Discussion
     s.add("LAB"); // Lab
     s.add("LBS"); // Lab Section
     s.add("ACT"); // Activity
     s.add("IDS"); // Intro Dial, Sophmore?
     s.add("ISF"); // Introsem, Freshman
     s.add("ISS"); // Introsem, Sophormore
     s.add("LNG"); // Language
     s.add("PRA"); // Practicum
     s.add("PRC"); // Practicum
   }
 
   private static List<String> permissibleClassComponents = new ArrayList<String>();
   private static List<String> departments = new ArrayList<String>();
 
   private static void writeDepartmentClassesToFile(Department d, ExploreCoursesConnection con) {
     String filename = d.getCode();
     filename = filename.replace(' ', '_');
     filename = filename.replace('&', '+');
     filename = filename.replace('/', '-');
     filename = filename.replace(',', '_');
     filename += ".js";
 
     try {
       List<String> courses = new ArrayList<String>();
       for (Course c : con.getCoursesByQuery(d.getCode())) {
         if (tooHighLevelClass(c.getSubjectCodeSuffix())) continue;
         String course = convertCourseToJSONKeyObjectPair(c);
         if (course != null) courses.add(course);
       }
       Collections.sort(courses);
 
       if (courses.size() == 0) return;
 
       File file = new File("../client/models/data/" + filename);
       file.createNewFile();
       FileWriter fw = new FileWriter(file.getAbsoluteFile());
       BufferedWriter bw = new BufferedWriter(fw);
       bw.write("{");
       for (int i = 0; i < courses.size(); i++) {
         if (i != 0) {
           bw.write(",\n");
         }
         bw.write(courses.get(i));
       }
       bw.write("}");
       bw.close();
     } catch (IOException e) {
       e.printStackTrace();
       System.out.println("Fuck IOException " + d.getCode());
     } catch (JDOMException e) {
       e.printStackTrace();
       System.out.println("Fuck JDOMException (What the hell is that!?!) " + d.getCode());
     }
 
   }
 
   private static String convertCourseToJSONKeyObjectPair(Course c) {
     String s = "\"" + c.getSubjectCodeSuffix() + "\":{";
     s += "\"department\":\"" + c.getSubjectCodePrefix() + "\",";
     s += "\"number\":\"" + c.getSubjectCodeSuffix() + "\",";
    s += "\"title\":\"" + convertToJSONEscapedString(c.getTitle()) + "\",";
    s += "\"description\":\"" + convertToJSONEscapedString(c.getDescription()) + "\",";
     s += "\"min_units\":" + c.getMinimumUnits() + ",";
     s += "\"max_units\":" + c.getMaximumUnits() + ",";
     String GERSSatisfied = parseGERString(c.getGeneralEducationRequirementsSatisfied());
     s += "\"gers\":" + GERSSatisfied + ",";
 
     Map<String, String> sections = getPrimaryAndSecodaryComponentSections(c);
     // Case where a class has no valid sections
     if (sections == null) return null;
     s += "\"primary\":" + sections.get("PRIMARY");
     if (sections.containsKey("SECONDARY")) {
       s += ",\"secondary\":" + sections.get("SECONDARY");
     }
 
     s += "}";
 
     return s;
   }
 
  /* Used to convert things like &quot; to " but then also escape it. */
  private static String convertToJSONEscapedString(String s) {
    // Convert the HTML things to actual characters.
    s = s.replace("&amp;", "&");
    s = s.replace("&#39;", "'");
    s = s.replace("&quot;", "\"");
    s = s.replace("\t", "\\t");
    // Escape the " now
    return s.replace("\"", "\\\"");
  }

   private static String parseGERString(String gers) {
     String[] split = gers.split(",");
     for (int i = 0; i < split.length; i++) {
       split[i] = split[i].trim();
       if (split[i].startsWith("GER:")) split[i] = split[i].substring(4);
     }
     String s = "[";
     for (int i = 0; i < split.length; i++) {
       if (split[i].length() != 0) {
         if (i != 0) s += ",";
         s += "\"" + split[i] + "\"";
       }
     }
     s += "]";
     return s;
   }
 
   private static Map<String, String> getPrimaryAndSecodaryComponentSections(Course c) {
     String dep = c.getSubjectCodePrefix();
 
     Set<String> components = new HashSet<String>();
     Map<String, ArrayList<String>> autumnClasses = new HashMap<String, ArrayList<String>>();
     Map<String, ArrayList<String>> winterClasses = new HashMap<String, ArrayList<String>>();
     Map<String, ArrayList<String>> springClasses = new HashMap<String, ArrayList<String>>();
 
     for (Section s : c.getSections()) {
       String term = s.getTerm();
       // Ignore summer classes.
       if (term.contains("Summer")) continue;
       String component = s.getComponent();
       // Ignore CS discussions. (Maybe not.)
       //if (dep.equals("CS") && s.getComponent().equals("DIS")) continue;
       String JSON = convertSectionToJSONObject(s);
       // Sections that have 'empty' meetings will return null;
       if (JSON == null) continue;
 
       // Only add the component once we know it has a valid section.
       components.add(component);
 
       Map<String, ArrayList<String>> relevantQuarter = autumnClasses;
       if (term.contains("Winter")) relevantQuarter = winterClasses;
       if (term.contains("Spring")) relevantQuarter = springClasses;
 
       if (!relevantQuarter.containsKey(component)) {
         ArrayList<String> newComponentList = new ArrayList<String>();
         newComponentList.add(JSON);
         relevantQuarter.put(component, newComponentList);
       } else {
         relevantQuarter.get(component).add(JSON);
       }
     }
 
     // For classes not taught this year and classes where EVERY section
     // is an 0 length one.
     if (components.size() == 0) return null;
     Map<String, String> sectionMap = new HashMap<String, String>();
     Map<String, String> whichComponent = decideWhichComponentIsPrimary(components);
     String pAutumn = convertListToJSONArray(autumnClasses.get(whichComponent.get("PRIMARY")));
     String pWinter = convertListToJSONArray(winterClasses.get(whichComponent.get("PRIMARY")));
     String pSpring = convertListToJSONArray(springClasses.get(whichComponent.get("PRIMARY")));
     sectionMap.put("PRIMARY", "[" + pAutumn + "," + pWinter + "," + pSpring + "]");
     if (whichComponent.containsKey("SECONDARY")) {
       String sAutumn = convertListToJSONArray(autumnClasses.get(whichComponent.get("SECONDARY")));
       String sWinter = convertListToJSONArray(winterClasses.get(whichComponent.get("SECONDARY")));
       String sSpring = convertListToJSONArray(springClasses.get(whichComponent.get("SECONDARY")));
       sectionMap.put("SECONDARY", "[" + sAutumn + "," + sWinter + "," + sSpring + "]");
     }
     return sectionMap;
   }
 
   // List of possible component pairs:
   // DIS LEC (Lots!)
   // DIS SEM (Also lots)
   // LAB LEC
   // LBS LEC
   // LEC LBS
   // PRA LEC
   // ISF DIS
   // LAB LBS
   // PRA WKS
   // PRC WKS
   private static Map<String, String> decideWhichComponentIsPrimary(Set<String> c) {
     Map<String, String> mapping = new HashMap<String, String>();
     String[] components = c.toArray(new String[0]);
     if (components.length == 1) {
       mapping.put("PRIMARY", components[0]);
       return mapping;
     }
 
     // Lecture is ALWAYS primary, so we have special cases for those.
     if (components[0].equals("LEC")) {
       mapping.put("PRIMARY", "LEC");
       mapping.put("SECONDARY", components[1]);
       return mapping;
     }
     if (components[1].equals("LEC")) {
       mapping.put("PRIMARY", "LEC");
       mapping.put("SECONDARY", components[0]);
       return mapping;
     }
 
     // Meanwhile, DIS is ALWAYS secondary.
     if (components[0].equals("DIS")) {
       mapping.put("PRIMARY", components[1]);
       mapping.put("SECONDARY", "DIS");
       return mapping;
     }
     if (components[1].equals("DIS")) {
       mapping.put("PRIMARY", components[0]);
       mapping.put("SECONDARY", "DIS");
       return mapping;
     }
 
     // Lab Sections are always secondary too.
     if (components[0].equals("LBS")) {
       mapping.put("PRIMARY", components[1]);
       mapping.put("SECONDARY", "LBS");
       return mapping;
     }
     if (components[1].equals("LBS")) {
       mapping.put("PRIMARY", components[0]);
       mapping.put("SECONDARY", "LBS");
       return mapping;
     }
 
     System.out.println("AHHHHHHHHHHHHH   " + components[0] + components[1]);
     return null;
   }
 
   /*
    * Convert a Section into a JSON object. Returns null if the section has
    * 'empty' meeting times. (Ones that last 0 minutes.)
    */
   private static String convertSectionToJSONObject(Section sec) {
     if (!permissibleClassComponents.contains(sec.getComponent())) return null;
     String s = "{";
     // This is causing problems right now.
     s += "\"instructors\":" + "[],"; //convertInstructorSetToJSONArray(sec.getInstructors());
     String meetingSchedules = convertMeetingScheduleSetToJSONArray(sec.getMeetingSchedules());
 
     // Ignore sections that never meet.
     if (meetingSchedules == null) return null;
     s += "\"meeting-times\":" + meetingSchedules;
     s += "}";
     return s;
   }
 
   /*
    * Converts a Set of instructors into JSON array string.
    */
   private static String convertInstructorSetToJSONArray(Set<Instructor> set) {
     boolean isFirst = true;
     String s = "[";
     for (Instructor i : set) {
       if (!isFirst) s += ",";
       s += "\"" + i.getName() + "\"";
       isFirst = false;
     }
     s += "]";
     return s;
   }
 
   /*
    * Converts a set of meeting schedules into a JSON array string. Returns null
    * if any of the meetings last 0 minutes.
    */
   private static String convertMeetingScheduleSetToJSONArray(Set<MeetingSchedule> set) {
     if (set.size() == 0) return null;
     boolean isFirst = true;
     boolean failed = false;
     String s = "[";
     for (MeetingSchedule m : set) {
       if (!isFirst) s += ",";
       String mJSON = convertMeetingScheduleToJSONObject(m);
       if (mJSON == null) {
         failed = true;
         break;
       }
       s += mJSON;
       isFirst = false;
     }
     s += "]";
     return (failed ? null : s);
   }
 
   /*
    * Converts a single meeting schedule into a JSON string. Returns null if the
    * meeting lasts 0 minutes.
    */
   private static String convertMeetingScheduleToJSONObject(MeetingSchedule m) {
     if (m.getStartTime().equals(m.getEndTime())) return null;
     String s = "{";
     s += "\"days\":" + convertDaysToBooleanArray(m.getDays()) + ",";
     s += "\"start\":\"" + m.getStartTime() + "\",";
     s += "\"end\":\"" + m.getEndTime() + "\"";
     s += "}";
     return s;
   }
 
   /*
    * Creates a boolean array from a list of days.
    */
   private static String convertDaysToBooleanArray(String days) {
     String str = "[";
     str += days.contains("Monday") ? "true" : "false";
     str += ",";
     str += days.contains("Tuesday") ? "true" : "false";
     str += ",";
     str += days.contains("Wednesday") ? "true" : "false";
     str += ",";
     str += days.contains("Thursday") ? "true" : "false";
     str += ",";
     str += days.contains("Friday") ? "true" : "false";
     str += "]";
     return str;
   }
 
   /*
    * Converts an List<String> into a JSON array string.
    */
   private static String convertListToJSONArray(List<String> list) {
     if (list == null) return "[]";
     String str = "[";
     for (int i = 0; i < list.size(); i++) {
       str += list.get(i);
       if (i != list.size() - 1) str += ",";
     }
     str += "]";
     return str;
   }
 
   // TODO: Decide if I need this. *Shouldn't* be a problem after I am more
   // intelligent about gathering section/meeting info.
   private static boolean tooHighLevelClass(String className) {
     //System.out.print(className);
     if (className.length() < 3) return false;
     char first = className.charAt(0);
     char second = className.charAt(1);
     char third = className.charAt(2);
     if (Character.isDigit(first) && Character.isDigit(second) && Character.isDigit(third)) {
       if (Integer.parseInt(className.substring(0, 3)) > 399) {
         //System.out.println(": Too high!");
         return true;
       }
     }
     //System.out.println(": OK!");
     return false;
   }
 }
