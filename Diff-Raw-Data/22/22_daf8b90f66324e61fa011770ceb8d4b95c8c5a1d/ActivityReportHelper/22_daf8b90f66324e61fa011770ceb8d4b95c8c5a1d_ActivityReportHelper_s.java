 /**
  * 
  */
 package de.aidger.utils.reports;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.reports.ActivityEmployment;
 import de.aidger.view.UI;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * The class creates the activity employment objects for one assistant for the
  * activity report.
  * 
  * @author aidGer Team
  */
 public class ActivityReportHelper {
 
     /**
      * The courses of the given assistant, along with all of their employments.
      */
     private final ArrayList<CourseEmployment> courseEmployments = new ArrayList<CourseEmployment>();
 
     /**
      * Initializes a new ActivityReportHelper.
      */
     public ActivityReportHelper() {
     }
 
     /**
      * Gets the index of a course in the courseEmployments vector.
      * 
      * @param name
      *            The name of the course.
      * @return The index of the course. -1 if it doesn't exist.
      */
     private int getIndexOf(String name) {
         int i = 0;
         for (CourseEmployment courseEmployment : courseEmployments) {
             if (courseEmployment.getName().equals(name)) {
                 return i;
             }
             i++;
         }
         // The course doesn't exist.
         return -1;
     }
 
     /**
      * Adds an employment to its course in the vector of courses.
      * 
      * @param name
      *            The name of the course.
      * @param employment
      *            The employment.
      */
     private void addCourseEmployment(String name, Employment employment) {
         if (getIndexOf(name) > -1) {
             courseEmployments.get(getIndexOf(name)).addEmployment(employment);
         } else {
             // The course doesn't exist yet. Create a new one.
             courseEmployments.add(new CourseEmployment(name, employment));
         }
     }
 
     /**
      * This model represents a course and all of its employments.
      * 
      * @author aidGer Team
      */
     private class CourseEmployment {
         /**
          * The name of the course.
          */
         private final String name;
 
         /**
          * The employments belonging to the course.
          */
         private final ArrayList<Employment> employments;
 
         /**
          * Initializes a new CourseEmployment with the given name and its first
          * employment.
          * 
          * @param name
          *            The name of the course.
          * @param employment
          *            The first employment of the course.
          */
         public CourseEmployment(String name, Employment employment) {
             this.name = name;
             employments = new ArrayList<Employment>();
             employments.add(employment);
         }
 
         /**
          * Adds an employment to this course.
          * 
          * @param employment
          *            The employment to add
          */
         public void addEmployment(Employment employment) {
             int i = 0;
             boolean employmentInserted = false;
             for (Employment thisEmployment : employments) {
                 /*
                  * Add the employments in chronological order.
                  */
                 if (thisEmployment.getYear() * 12 + thisEmployment.getMonth() >= employment
                     .getYear()
                         * 12 + employment.getMonth()) {
                     employments.add(i, employment);
                     employmentInserted = true;
                     break;
                 }
                 i++;
             }
             if (!employmentInserted) {
                 employments.add(employment);
             }
         }
 
         /**
          * Returns the name of this course.
          * 
          * @return The name of the course
          */
         public String getName() {
             return name;
         }
 
         /**
          * Returns the employments of this course.
          * 
          * @return The employments of this course.
          */
         public ArrayList<Employment> getEmployments() {
             return employments;
         }
     }
 
     /**
      * Determines all of the activity employments of a given assistant.
      * 
      * @param assistant
      *            The assistant of which to get the employments.
      * @return The employments of this assistant.
      */
     public ArrayList<ActivityEmployment> getEmployments(Assistant assistant) {
         ArrayList<ActivityEmployment> activityEmployments = new ArrayList<ActivityEmployment>();
         try {
             List<Employment> employments = new Employment()
                 .getEmployments(assistant);
             /*
              * Map all the employments to their courses and sort them in
              * chronological order.
              */
             for (Employment employment : employments) {
                 ICourse course = new Course().getById(employment.getCourseId());
                 addCourseEmployment(course.getDescription() + "("
                         + course.getSemester() + ")", employment);
             }
             /*
              * Create at least one activity employment for each course.
              */
             for (CourseEmployment courseEmployment : courseEmployments) {
                 /*
                  * Activity employments will be listed as time-period, course
                  * and hour count. Because employments can have breaks, we need
                  * to check if this is the case. If it is, we need to create a
                  * new activity employment for the next time-period. Therefore,
                  * we will set the name, year and month of the first employment.
                  */
                 ActivityEmployment activityEmployment = new ActivityEmployment();
                 activityEmployment.setCourse(courseEmployment.getName());
                 int firstYear = courseEmployment.getEmployments().get(0)
                     .getYear();
                 int firstMonth = courseEmployment.getEmployments().get(0)
                     .getMonth();
                 /*
                  * This is used to check if the employment is in the
                  * time-period. It is equal to the amount of months from
                  * 1.1.0000 until today.
                  */
                 int firstTotalMonth = firstYear * 12 + firstMonth;
                 activityEmployment.addYear((short) firstYear);
                 activityEmployment.addMonth((byte) firstMonth);
                 /*
                  * This is the difference between the first month and the
                  * current one.
                  */
                 int difference = 1;
                 int count = 0;
                 double hourCount = 0;
                 for (Employment employment : courseEmployment.getEmployments()) {
                     if (employment.getYear() * 12 + employment.getMonth() <= firstTotalMonth
                             + difference) {
                         /*
                          * The employment is in the same month as the previous
                          * one or one month after it.
                          */
                         hourCount = hourCount + employment.getHourCount();
                         if (employment.getYear() * 12 + employment.getMonth() == firstYear
                                 * 12 + firstMonth + difference) {
                             /*
                              * If it was one month after the previous one, we
                              * will check for the next month.
                              */
                             difference++;
                         }
                     } else {
                         /*
                          * There is a break of at least one month between the
                          * previous employment and the current one. We will have
                          * to save the current activity employment and create a
                          * new one.
                          */
                         activityEmployment.addHours(hourCount);
                         int currentYear = ((firstTotalMonth + difference - 1) / 12);
                         int currentMonth = (firstMonth + difference - 1) % 12;
                         activityEmployment.addYear((short) (currentYear));
                         activityEmployment.addMonth((byte) (currentMonth));
                         activityEmployments.add(activityEmployment);
                         activityEmployment = new ActivityEmployment();
                         /*
                          * Create a new activity employment with a new name,
                          * starting month and year and a new difference. The
                          * hour count needs to be reset as well.
                          */
                         firstYear = employment.getYear();
                         firstMonth = employment.getMonth();
                         firstTotalMonth = firstYear * 12 + firstMonth;
                         difference = 1;
                         hourCount = employment.getHourCount();
                         activityEmployment
                             .setCourse(courseEmployment.getName());
                         activityEmployment.addYear((short) firstYear);
                         activityEmployment.addMonth((byte) firstMonth);
                     }
                     count++;
                     if (count == courseEmployment.getEmployments().size()) {
                         /*
                          * If this was the last employment of this course, the
                          * activity employment needs to be closed. Add all the
                          * relevant data to it and then add it to the vector.
                          */
                         activityEmployment
                             .setCourse(courseEmployment.getName());
                         activityEmployment.addHours(hourCount);
                         int size = courseEmployment.getEmployments().size();
                         activityEmployment.addYear(courseEmployment
                             .getEmployments().get(size - 1).getYear());
                         activityEmployment.addMonth(courseEmployment
                             .getEmployments().get(size - 1).getMonth());
                         activityEmployments.add(activityEmployment);
                     }
                 }
             }
         } catch (AdoHiveException e) {
             UI.displayError(e.toString());
         }
         return activityEmployments;
     }
 
     /**
      * Returns the given activity employment as an object array
      * 
      * @param employment
      *            The activity employment.
      * @return The activity employment as an array.
      */
     public Object[] getEmploymentArray(ActivityEmployment employment) {
         Object[] returnArray = new Object[3];
         int monthSize = employment.getMonths().size();
         int yearSize = employment.getYears().size();
         String firstMonth = "";
         String lastMonth = "";
         if (employment.getMonths().get(0) < 10) {
             firstMonth = "0" + employment.getMonths().get(0);
         } else {
             firstMonth = employment.getMonths().get(0).toString();
         }
         if (employment.getMonths().get(monthSize - 1) < 10) {
             lastMonth = "0" + employment.getMonths().get(monthSize - 1);
         } else {
             lastMonth = employment.getMonths().get(monthSize - 1).toString();
         }
         returnArray[0] = firstMonth + "." + employment.getYears().get(0)
                 + " - " + lastMonth + "."
                 + employment.getYears().get(yearSize - 1);
         returnArray[1] = employment.getCourse();
        returnArray[2] = employment.getHours();
         return returnArray;
     }
 }
