 package by.vsu.emdsproject.report.aspose.impl;
 
 import by.vsu.emdsproject.model.Student;
 import by.vsu.emdsproject.report.aspose.AsposeReport;
 import com.aspose.words.*;
 
 import java.text.SimpleDateFormat;
 import java.util.regex.Pattern;
 
 public class PersonCardReport extends AsposeReport {
 
     private Student student;
     public static final String TEMPLATE_NAME = "PersonCard.docx";
 
 
     public static Pattern CARD_NUMBER = Pattern.compile("<cardNumber>");
     public static Pattern ADMISSION_YEAR = Pattern.compile("<admissionYear>");
     public static Pattern FIO = Pattern.compile("<fio>");
     public static Pattern BIRTH_YEAR = Pattern.compile("<birthYear>");
     public static Pattern BIRTH_PLACE = Pattern.compile("<birthPlace>");
     public static Pattern RECRUIT_OFFICE = Pattern.compile("<recruitOffice>");
     public static Pattern FACULTY = Pattern.compile("<faculty>");
     public static Pattern EDUCATION = Pattern.compile("<education>");
     public static Pattern DUTY = Pattern.compile("<duty>");
     public static Pattern EDUCATION_START = Pattern.compile("<educationStart>");
     public static Pattern EDUCATION_END = Pattern.compile("<educationEnd>");
     public static Pattern RANK = Pattern.compile("rank");
     public static Pattern PARENT_ADDRESS = Pattern.compile("<parentAddress>");
     public static Pattern ADDRESS = Pattern.compile("<address>");
 
 
     public PersonCardReport(Student student) {
         this.student = student;
     }
 
     @Override
     public Document generate() throws Exception {
 
         Document document = new Document(getTemplateFilePath(TEMPLATE_NAME));
 
         Range range = document.getRange();
         range.replace(ADMISSION_YEAR, student.getQuestionnaire().getAdmissionYear());
         range.replace(FIO, student.getLastName() + " " + student.getFirstName() + " " + student.getMiddleName());
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
         range.replace(BIRTH_YEAR, sdf.format(student.getBirthDate()));
         range.replace(BIRTH_PLACE, student.getQuestionnaire().getBirthPlace());
         range.replace(FACULTY, student.getQuestionnaire().getFaculty());
         range.replace(PARENT_ADDRESS, student.getQuestionnaire().getParentAddress().toString());
         range.replace(ADDRESS, student.getQuestionnaire().getAddress().toString());
         range.replace(EDUCATION, student.getQuestionnaire().getEducationBefore());
         range.replace(DUTY,student.getQuestionnaire().getDuty());
        range.replace(RECRUIT_OFFICE, student.getQuestionnaire().getRecruitmentOffice());
 
         return document;
     }
 }
