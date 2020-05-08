 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 @Table(name = "courses")
 public class Course extends Model {
     public static final long serialVersionUID = 1L;
     @Id
     @Column(name = "course_ID")
     public Integer courseID;
     @Lob
     @Size(max = 65535)
     @Column(name = "notes")
     public String notes;
     @Column(name = "actual_start_date")
     public Date actualStartDate;
     @NotNull
     @Size(min = 1, max = 255)
     @Column(name = "institution")
     public String institution;
     @NotNull
     @Size(min = 1, max = 255)
     @Column(name = "place")
     public String place;
     @Column(name = "credits")
     public Integer credits;
     @NotNull
     @Size(min = 1, max = 200)
     @Column(name = "course_name")
     public String courseName;
     @NotNull
     @Column(name = "academic_year")
     public int academicYear;
     @NotNull
     @Column(name = "is_in_manifesto")
     public boolean isInManifesto;
     @NotNull
     @Column(name = "is_by_UNITN")
     public boolean isbyUNITN;
     @Column(name = "is_paid")
     public Boolean isPaid;
     @NotNull
     @Column(name = "budgeted_cost")
     public int budgetedCost;
     @NotNull
     @Column(name = "actual_cost")
     public int actualCost;
     @NotNull
     @Size(min = 1, max = 255)
     @Column(name = "planned_course_period")
     public String plannedCoursePeriod;
     @NotNull
     @Column(name = "are_all_marks_defined")
     public boolean areAllMarksDefined;
     @NotNull
     @Size(min = 1, max = 255)
     @Column(name = "url")
     public String url;/*
     @NotNull
     @Size(min = 1, max = 255)
     @Column(name = "votespage")
     public String votespage;*/
     @NotNull
     @Column(name = "is_payment_completed")
     public boolean isPaymentCompleted;
     @NotNull
     @Column(name = "deleted")
     public boolean deleted;
     @JoinColumn(name = "professor", referencedColumnName = "supervisor_ID")
     @ManyToOne
     public Supervisor professor;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
     public Set<CourseEnrollment> coursesEnrollmentSet;
 
     public static Finder<Long,Course> find = new Finder(
       Long.class, Course.class
     );
 
     public static List<Course> all() {
       return find.all();
     }
 
     public static void create(Course course) {
       course.save();
     }
 
     public static void delete(Long id) {
       find.ref(id).delete();
     }
 
     public List<CourseEnrollment> getCoursesEnrollment()
     {
       List<CourseEnrollment> out = new ArrayList(this.coursesEnrollmentSet);
       for (CourseEnrollment ce : out)
        out.refresh(); //force fetching from db
       return out;
     }
 
     public static List<Course> currentCourses() {
       List<Course> out = new ArrayList();
       int currentYear = Course.AcademicYear();
       for (Course c: Course.all())
         if (c.academicYear == currentYear)
           out.add(c);
       return out;
     }
 
     public static List<Course> oldCourses() {
       List<Course> out = new ArrayList();
       int currentYear = Course.AcademicYear();
       for (Course c: Course.all())
         if (c.academicYear < currentYear)
           out.add(c);
       return out;
     }
 
     public static int AcademicYear()
     {
       int currentYear = -1;
       for (Course c : Course.find.all())
             if (c.academicYear > currentYear)
                 currentYear = c.academicYear;
       return currentYear;
     }
 
     public String printActualStartDate() {
       if (this.actualStartDate != null)
         return new java.text.SimpleDateFormat("yyyy-MM-dd").format(this.actualStartDate);
       else
         return "";
     }
 
     public Supervisor getProfessor()
     {
       Supervisor s = this.professor;
       if (s != null)
       {
         String a = s.firstName;//does nothing, force fetching from db
       }
       return s;
     }
 
     public static class CompareByDate implements Comparator<Course> {
       @Override
       public int compare (Course c1, Course c2) {
         //from most recent year to the oldest
         if (c1.academicYear<c2.academicYear)
           return 1;
         else if (c1.academicYear>c2.academicYear)
           return -1;
         //same year -> ordered by startdate
         else
         {
           //null date is the last element
           if (c1.actualStartDate == null)
             return 1;
           else if (c2.actualStartDate == null)
             return -1;
           else
             return c1.actualStartDate.compareTo(c2.actualStartDate)*-1;
         }
       }
     }
 
     public String printType()
     {
       if (this.isInManifesto)
         return "Internal";
       else
         return "External";
     }
 }
