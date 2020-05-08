 package ua.dp.primat.curriculum.planparser;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.poi.ss.usermodel.Row;
 
 import ua.dp.primat.domain.workload.FinalControlType;
 import ua.dp.primat.domain.workload.LoadCategory;
 import ua.dp.primat.domain.workload.WorkloadType;
 import ua.dp.primat.domain.Cathedra;
 import ua.dp.primat.domain.workload.Discipline;
 import ua.dp.primat.domain.workload.Workload;
 import ua.dp.primat.domain.workload.IndividualControl;
 import ua.dp.primat.domain.StudentGroup;
 
 /**
  * Object, which represents the one curriculum row and
  * this one has methods to create database entities.
  * @author fdevelop
  */
 public final class CurriculumXLSRow {
 
     /**
      * Constructor, that gets atomic info from one row and creates data entities
      * objects (Cathedra, Discipline, Workload).
      */
     public CurriculumXLSRow(StudentGroup group, Row row,
             WorkloadType workloadType, LoadCategory loadCategory,
             int semestersCount, String diffSetOffSymbol) {
 
         //get general info
         this.disciplineName = row.getCell(COL_DISCIPLINE).getStringCellValue();
         this.cathedraName = row.getCell(COL_CATHEDRA).getStringCellValue();
         this.workloadType = workloadType;
         this.loadCategory = loadCategory;
         this.diffSetOff = diffSetOffSymbol;
         this.group = group;
 
         //get info for Exams, Setoffs, Course works
         final String sfmExams = row.getCell(COL_FINALCONTROL).toString();
         final String sfmTests = row.getCell(COL_FINALCONTROL + 1).toString();
         final String sfmCourses = row.getCell(COL_FINALCONTROL + 2).toString();
 
         this.fmExams = parseNumValues(sfmExams, true);
         Arrays.sort(fmExams);
         this.fmTests = parseNumValues(sfmTests, true);
         Arrays.sort(fmTests);
         this.fmCourses = parseNumValues(sfmCourses, true);
         Arrays.sort(fmCourses);
         this.fmDifTests = parseNumValues(sfmTests, false);
         Arrays.sort(fmDifTests);
 
         //get info for individual works
         final String siwSemester = row.getCell(COL_INDIVIDUALTASKS).toString();
         final String siwForm = row.getCell(COL_INDIVIDUALTASKS + 1).toString();
         final String siwWeek = row.getCell(COL_INDIVIDUALTASKS + 2).toString();
         this.indWorks = createIndividualWorkList(siwSemester, siwForm, siwWeek);
 
         //get info for hours
         hoursForSemesters = new HashMap<Integer, WorkHours>();
         for (int sem = 0; sem < semestersCount; sem++) {
             final WorkHours semesterHoursInfo = new WorkHours();
             semesterHoursInfo.setHoursLec(row.getCell(COL_HOURS_LECTURE + COL_HOUROFFSET * sem).getNumericCellValue());
             semesterHoursInfo.setHoursPract(row.getCell(COL_HOURS_PRACTICE + COL_HOUROFFSET * sem).getNumericCellValue());
             semesterHoursInfo.setHoursLab(row.getCell(COL_HOURS_LAB + COL_HOUROFFSET * sem).getNumericCellValue());
             semesterHoursInfo.setHoursInd(row.getCell(COL_HOURS_INDIVIDUAL + COL_HOUROFFSET * sem).getNumericCellValue());
             semesterHoursInfo.setHoursSam(row.getCell(COL_HOURS_SELFWORK + COL_HOUROFFSET * sem).getNumericCellValue());
             if (semesterHoursInfo.getSum() > 0) {
                 hoursForSemesters.put(sem + 1, semesterHoursInfo);
             }
         }
 
         //run the generation process
         generateDatabaseEntries();
     }
 
     /* getters */
     public Cathedra getCathedra() {
         return cathedra;
     }
 
     public Discipline getDiscipline() {
         return discipline;
     }
 
     public List<Workload> getWorkloadList() {
         return workloadList;
     }
 
     /**
      * Parses a string like '1,2  ,3, 7', which is used in Curriculum. There is a
      * special case for Setoffs: it could be as '1, 2, 3d, 4', where 'd' means
      * differential setoff (this char is defined by the diffSetOff language constant).
      * @param fmStr - the input string
      * @param standard - if true, a test of differential setoff will be skipped
      * @return The generated array of parsed integers. If standard was false, the result
      *      will include only marked numbers (like '3d').
      */
     private int[] parseNumValues(String fmStr, boolean standard) {
         final String regex = "\\d+(\\.\\d+)?" + ((standard) ? "([^" + diffSetOff + "]|$)" : diffSetOff);
         final Pattern digits = Pattern.compile(regex);
         final Matcher matcher = digits.matcher(fmStr.replaceAll(",", " "));
 
         final List<Integer> intValues = new ArrayList<Integer>();
         while (matcher.find()) {
             intValues.add((int)Double.parseDouble(matcher.group().trim().replace(diffSetOff, "")));
         }
         
         final int[] intResult = new int[intValues.size()];
         for (int i = 0; i < intResult.length; i++) {
             intResult[i] = intValues.get(i);
         }
         //System.arraycopy(intValues, 0, intResult, 0, intResult.length);
         return intResult;
     }
 
     /**
      * Parses the individual control strings like 'AO, 2mw' and returns
      * an array of token like ['AO','mw','mw'].
      * @param individualControlTypeCell - the input string
      * @return The string array of tokens
      */
     private String[] parseIndividualControlTypes(String individualControlTypeCell) {
         final List<String> listTokens = new ArrayList<String>();
 
         //value, that contains all tokens separated by coma. This value lose one token after one iteration
         String workIndForm = individualControlTypeCell.trim();
         //one received token
         String tokenType;
 
         while (workIndForm.length() > 0) {
             if (workIndForm.indexOf(',') > -1) {
                 tokenType = workIndForm.substring(0, workIndForm.indexOf(',')).trim();
             } else {
                 tokenType = workIndForm.trim();
                 workIndForm = "";
             }
 
             if (tokenType.length() > 0) {
                 if ((tokenType.charAt(0) >= '0') && (tokenType.charAt(0) <= '9')) {
                     final int nextWorksCount = Integer.parseInt(tokenType.substring(0, 1));
                     for (int y = 0; y < nextWorksCount; y++) {
                         listTokens.add(tokenType.substring(1));
                     }
                 } else {
                     listTokens.add(tokenType);
                 }
             }
 
             if (workIndForm.indexOf(',') > -1) {
                 workIndForm = workIndForm.substring(workIndForm.indexOf(',') + 1);
             }
         }
 
         return listTokens.toArray(new String[0]);
     }
 
     /**
      * Parses 3 columns for individual controls works info. It includes semester number,
      * type of control work and its week number of year. If the count of tokens
      * in parameters are different, method returns null.
      *
      * @param siwSemester - cell text for semester number of work
      * @param siwForm - cell text for type of control work
      * @param siwWeek - cell text for number of week
      * @return The array of IndividualControlEntry objects or null, if count of tokens
      *      are different.
      */
     private List<IndividualControlEntry> createIndividualWorkList(String siwSemester, String siwForm, String siwWeek) {
         final int[] semesters = parseNumValues(siwSemester, true);
         final String[] types = parseIndividualControlTypes(siwForm);
         final int[] weeks = parseNumValues(siwWeek, true);
 
         if ((semesters.length != types.length) || (semesters.length != weeks.length)) {
            return new ArrayList<IndividualControlEntry>();
         }
 
         final List<IndividualControlEntry> entries = new ArrayList<IndividualControlEntry>();
         for (int i = 0; i < semesters.length; i++) {
             entries.add(new IndividualControlEntry(semesters[i], types[i], weeks[i]));
         }
 
         return entries;
     }
 
     /**
      * Method, that returns an information about planned course work for specified semester.
      * @param semester
      * @return true - if there is a course work in specified semester
      */
     private boolean getCourseInSemester(int semester) {
         return (Arrays.binarySearch(fmCourses, semester) != -1);
     }
 
     /**
      * Method, that returns an information about final control type for specified semester.
      * @param semester
      * @return Value of FinalControlType, which indicates a type of final control for specified semester
      */
     private FinalControlType getFinalControlTypeInSemester(int semester) {
         if (Arrays.binarySearch(fmExams, semester) > -1) {
             return FinalControlType.Exam;
         } else if (Arrays.binarySearch(fmTests, semester) > -1) {
             return FinalControlType.Setoff;
         } else if (Arrays.binarySearch(fmDifTests, semester) > -1) {
             return FinalControlType.DifferentiableSetoff;
         } else {
             return FinalControlType.Nothing;
         }
     }
 
     /**
      * Executes the generation methods for database objects.
      */
     private void generateDatabaseEntries() {
         generateCathedra();
         generateDiscipline(cathedra);
         generateWorkloadEntries(discipline);
     }
 
     /**
      * Creates new entity Cathedra.
      */
     private void generateCathedra() {
         cathedra = new Cathedra();
         cathedra.setName(cathedraName);
     }
 
     /**
      * Creates new entity Discipline for specified Cathedra.
      * @param cathedra
      */
     private void generateDiscipline(Cathedra cathedra) {
         discipline = new Discipline();
         discipline.setName(disciplineName);
         discipline.setCathedra(cathedra);
     }
 
     /**
      * Generates list of IndividualControls for specified Workload.
      * @param workload
      */
     private void generateIndividualControls(Workload workload) {
         for (IndividualControlEntry ice : indWorks) {
             if (workload.getSemesterNumber() == ice.getSemester()) {
                 final IndividualControl ic = new IndividualControl(ice.getType(),
                         Long.valueOf(ice.getWeekNum()));
                 workload.getIndividualControl().add(ic);
             }
         }
     }
 
     /**
      * Generates list of Workload for the row.
      * @param workload
      */
     private void generateWorkloadEntries(Discipline discipline) {
         workloadList.clear();
         for (Integer i : hoursForSemesters.keySet()) {
             if (hoursForSemesters.get(i) != null) {
                 final Workload workload = new Workload();
                 workload.setDiscipline(discipline);
                 workload.setLoadCategory(loadCategory);
                 workload.setType(workloadType);
                 workload.setStudentGroup(group);
                 workload.setCourseWork(getCourseInSemester(i));
                 workload.setFinalControlType(getFinalControlTypeInSemester(i));
                 workload.setSelfworkHours(Long.valueOf(Math.round(hoursForSemesters.get(i).getHoursSam()
                         + hoursForSemesters.get(i).getHoursInd())));
                 workload.setLaboratoryHours(Long.valueOf(Math.round(hoursForSemesters.get(i).getHoursLab())));
                 workload.setLectionHours(Long.valueOf(Math.round(hoursForSemesters.get(i).getHoursLec())));
                 workload.setPracticeHours(Long.valueOf(Math.round(hoursForSemesters.get(i).getHoursPract())));
                 workload.setSemesterNumber(Long.valueOf(i));
                 //individual controls
                 generateIndividualControls(workload);
                 //add to the result
                 workloadList.add(workload);
             }
         }
     }
 
     /* CONSTANTS */
     /** Excel index for Name of Discipline. */
     public static final int COL_DISCIPLINE = 1;
     /** Excel index for Cathedra of Discipline. */
     public static final int COL_CATHEDRA = 2;
     /** Excel index for type of final control (exam, setoff, differential setoff, course). */
     public static final int COL_FINALCONTROL = 6;
     /** Excel index for types of individual tasks. */
     public static final int COL_INDIVIDUALTASKS = 9;
     /** Excel indexes for hours for the Discipline. */
     public static final int COL_HOURS_LECTURE = 22;
     public static final int COL_HOURS_PRACTICE = 23;
     public static final int COL_HOURS_LAB = 24;
     public static final int COL_HOURS_INDIVIDUAL = 25;
     public static final int COL_HOURS_SELFWORK = 26;
     /** Excel cells count for hours info of one semester. */
     public static final int COL_HOUROFFSET = 6;
     //PRIVATE Variables
     private String diffSetOff;
     private String disciplineName;
     private String cathedraName;
     private WorkloadType workloadType;
     private LoadCategory loadCategory;
     //final monitoring
     private int[] fmExams;
     private int[] fmDifTests;
     private int[] fmTests;
     private int[] fmCourses;
     //individual works
     private List<IndividualControlEntry> indWorks;
     //hours info
     private Map<Integer, WorkHours> hoursForSemesters;
     //DataBase output objects
     private StudentGroup group;
     private Cathedra cathedra;
     private Discipline discipline;
     private List<Workload> workloadList = new ArrayList<Workload>();
 }
