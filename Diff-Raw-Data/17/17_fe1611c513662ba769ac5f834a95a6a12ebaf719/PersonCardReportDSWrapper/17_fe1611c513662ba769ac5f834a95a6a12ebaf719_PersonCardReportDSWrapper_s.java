 package by.vsu.emdsproject.report.jasper;
 
 import by.vsu.emdsproject.model.Questionnaire;
 import by.vsu.emdsproject.model.Student;
 import java.util.HashMap;
 
 /**
  * вытягивает все необходимые данные для отчета персональной карточки студента
  */
 public class PersonCardReportDSWrapper implements ReportDataSourceWrapper {
 
     private Student student;
     public static final String templateName = "/home/anton/PersonCardReport.jasper";
 
     public static class Parameter {
 
         public static final String CARD_NUMBER = "cardNumber";
         public static final String ADMISSION_YEAR = "admissionYear";
         public static final String FIO = "fio";
         public static final String BITH_YEAR = "bithYear";
         public static final String BITH_PLACE = "bithPlace";
         public static final String RECRUIT_OFFICE = "recruitOffice";
         public static final String FACULTY = "faculty";
         public static final String EDUCATION = "education";
         public static final String DUTY = "duty";
         public static final String EDUCATION_START = "educationStart";
         public static final String EDUCATION_END = "educationEnd";
         public static final String RANK = "rank";
         public static final String PARENT_ADDRESS = "parentAddress";
         public static final String ADDRESS = "address";
     }
 
     public PersonCardReportDSWrapper(Student student) {
         this.student = student;
     }
 
     public String getTemplateName() {
         return templateName;
     }
 
     public HashMap getDataSource() {
         Questionnaire questionnaire = student.getQuestionnaire();
         HashMap map = new HashMap();
        map.put(Parameter.CARD_NUMBER, student.getId());
         map.put(Parameter.FIO, student.getLastName() + " " + student.getFirstName() + " " + student.getMiddleName());
        map.put(Parameter.ADMISSION_YEAR, questionnaire.getAdmissionYear());
        map.put(Parameter.BITH_YEAR, student.getBirthDate().getYear());
         map.put(Parameter.BITH_PLACE, questionnaire.getBirthPlace());
         map.put(Parameter.RECRUIT_OFFICE, questionnaire.getRecruitmentOffice());
         map.put(Parameter.FACULTY, questionnaire.getFaculty());
         map.put(Parameter.EDUCATION, questionnaire.getEducation());
         String duty = "не служил";
         if (questionnaire.getDutyStart() != null && questionnaire.getDutyEnd() != null) {
             duty = "c " + questionnaire.getDutyStart() + " по " + questionnaire.getDutyEnd();
         }
         map.put(Parameter.DUTY, duty);
        map.put(Parameter.EDUCATION_START, questionnaire.getEducationStartDate());
        map.put(Parameter.EDUCATION_END, questionnaire.getEducationEndDate());
         map.put(Parameter.RANK, student.getRank());
        map.put(Parameter.PARENT_ADDRESS, questionnaire.getParentAddress());
        map.put(Parameter.ADDRESS, questionnaire.getAddress());
         return map;
     }
 }
