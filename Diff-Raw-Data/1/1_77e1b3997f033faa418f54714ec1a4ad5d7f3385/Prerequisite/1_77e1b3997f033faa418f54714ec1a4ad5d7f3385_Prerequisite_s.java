<<<<<<< HEAD
 /*
  *
  *
 =======
 //*****************************************************
 /* (INCOMPLETE)
  Prerequisuite.java -- Model for Prerequisuite table
 
  @Contributors:  Harsimran
 
  @Purpose:   Generates various quesries on Prerequisite
  *           tablel that is used in identifying a prereq
  *           course associated with a coutse and any
  *           registration restrictions
 
  @Copyright ADG (2013) - Open License
 >>>>>>> add comment skeleton + comments for prereq
  */
 //*****************************************************
 package adg.red.models;
 
 import adg.red.utils.RedEntityManager;
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinColumns;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Creating queries to be used in methods followed
  */
 @Entity
 @Table(name = "Prerequisite")
 @XmlRootElement
 @NamedQueries(
         {
     @NamedQuery(name = "Prerequisite.findAll", query = "SELECT p FROM Prerequisite p"),
     @NamedQuery(name = "Prerequisite.findByCourseNumber", query = "SELECT p FROM Prerequisite p WHERE p.prerequisitePK.courseNumber = :courseNumber"),
     @NamedQuery(name = "Prerequisite.findByDepartmentId", query = "SELECT p FROM Prerequisite p WHERE p.prerequisitePK.departmentId = :departmentId"),
     @NamedQuery(name = "Prerequisite.findByPreRequisiteNumber", query = "SELECT p FROM Prerequisite p WHERE p.prerequisitePK.preRequisiteNumber = :preRequisiteNumber"),
     @NamedQuery(name = "Prerequisite.findByPreRequisiteDeptId", query = "SELECT p FROM Prerequisite p WHERE p.prerequisitePK.preRequisiteDeptId = :preRequisiteDeptId"),
     @NamedQuery(name = "Prerequisite.findByIsActive", query = "SELECT p FROM Prerequisite p WHERE p.isActive = :isActive"),
     @NamedQuery(name = "Prerequisite.findByIsMust", query = "SELECT p FROM Prerequisite p WHERE p.isMust = :isMust"),
     @NamedQuery(name = "Prerequisite.findByCourseNumberAndDepartmentId", query = "SELECT p FROM Prerequisite p LEFT JOIN FETCH p.course LEFT JOIN FETCH p.course1 WHERE p.prerequisitePK.courseNumber = :courseNumber AND p.prerequisitePK.departmentId = :departmentId")
 })
 public class Prerequisite implements Serializable
 {
 
     private static final long serialVersionUID = 1L;
     @EmbeddedId
     protected PrerequisitePK prerequisitePK;
     @Basic(optional = false)
     @Column(name = "isActive")  //setting prereq course by default to be active
     private boolean isActive;
     @Basic(optional = false)
     @Column(name = "isMust")    //setting prereq course by default to be mandatory
     private boolean isMust;
     @JoinColumns(
             {
         @JoinColumn(name = "preRequisiteDeptId", referencedColumnName = "departmentId", insertable = false, updatable = false),
         @JoinColumn(name = "preRequisiteNumber", referencedColumnName = "courseNumber", insertable = false, updatable = false)
     })
     @ManyToOne(optional = false)
     private Course course;
     @JoinColumns(
             {
         @JoinColumn(name = "departmentId", referencedColumnName = "departmentId", insertable = false, updatable = false),
         @JoinColumn(name = "courseNumber", referencedColumnName = "courseNumber", insertable = false, updatable = false)
     })
     @ManyToOne(optional = false)
     private Course course1;
 
     /**
      * Default public class constructor
      */
     public Prerequisite()
     {
     }
 
     /**
      * Public class constructor
      * <p/>
      * @param prerequisitePK -- Set of all attributes that create the primary
      *                       key for a prerequisite course
      */
     public Prerequisite(PrerequisitePK prerequisitePK)
     {
         this.prerequisitePK = prerequisitePK;
     }
 
     /**
      * Public class constructor
      * <p/>
      * @param prerequisitePK -- Set of all attributes that create the primary
      *                       key for a prerequisite course
      * @param isActive       -- setting value for prereq to be active or
      *                       inactive
      * @param isMust         -- setting prereq course to be mandatory or
      *                       optional
      */
     public Prerequisite(PrerequisitePK prerequisitePK, boolean isActive, boolean isMust)
     {
         this.prerequisitePK = prerequisitePK;
         this.isActive = isActive;
         this.isMust = isMust;
     }
 
     /**
      * Public class constructor
      * <p/>
      * @param courseNumber       -- Number for course requiring this course
      * @param departmentId       -- Department ID for course requiring this
      *                           course
      * @param preRequisiteNumber -- Prereq course number
      * @param preRequisiteDeptId -- Prereq department ID
      */
     public Prerequisite(int courseNumber, String departmentId, int preRequisiteNumber, String preRequisiteDeptId)
     {
         this.prerequisitePK = new PrerequisitePK(courseNumber, departmentId, preRequisiteNumber, preRequisiteDeptId);
     }
 
     /**
      * Public method that gets the set of values that create the primary key for
      * a Prereq course
      * <p/>
      * @return -- Object of type prerequisitePK
      */
     public PrerequisitePK getPrerequisitePK()
     {
         return prerequisitePK;
     }
 
     /**
      * Public method that sets the object primary key set for a Prereq
      * <p/>
      * @param prerequisitePK -- Object of type prerequisitePK
      */
     public void setPrerequisitePK(PrerequisitePK prerequisitePK)
     {
         this.prerequisitePK = prerequisitePK;
     }
 
     /**
      * Public method that gets the active/inactive value of a prereq
      * <p/>
      * @return -- A boolean indicating prereq course is active = "true" /
      *         inactive = "false"
      */
     public boolean getIsActive()
     {
         return isActive;
     }
 
     /**
      * Public method that sets the active/inactive value of a prereq
      * <p/>
      * @param isActive -- A boolean indicating prereq course is active = "true"
      *                 / inactive = "false"
      */
     public void setIsActive(boolean isActive)
     {
         this.isActive = isActive;
     }
 
     /**
      * Public method that gets the is mandatory or is not mandatory attribute of
      * a prereq course
      * <p/>
      * @return -- A boolean indicating prereq course is mandatory = "true" / is
      *         optional = "false"
      */
     public boolean getIsMust()
     {
         return isMust;
     }
 
     /**
      * Public method that sets the is mandatory or is not mandatory attribute of
      * a prereq course
      * <p/>
      * @param isMust-- A boolean indicating prereq course is mandatory = "true"
      *                 / is optional = "false"
      */
     public void setIsMust(boolean isMust)
     {
         this.isMust = isMust;
     }
 
     /**
      * Public method that gets the course object for a prereq course
      * <p/>
      * @return -- Object type Course
      */
     public Course getCourse()
     {
         return course;
     }
 
     /**
      * Public method that sets the course object for a prereq course
      * <p/>
      * @param course -- Object type Course
      */
     public void setCourse(Course course)
     {
         this.course = course;
     }
 
     /**
      * Public method that gets the prereq course for a course
      * <p/>
      * @return -- Object type Course
      */
     public Course getCourse1()
     {
         return course1;
     }
 
     /**
      * Public method that sets the prereq course for a course
      * <p/>
      * @param course1 -- Object type Course
      */
     public void setCourse1(Course course1)
     {
         this.course1 = course1;
     }
 
     /**
      *
      * @return
      */
     @Override
     public int hashCode()
     {
         int hash = 0;
         hash += (prerequisitePK != null ? prerequisitePK.hashCode() : 0);
         return hash;
     }
 
     /**
      *
      * @param object <p/>
      * @return
      */
     @Override
     public boolean equals(Object object)
     {
         if (!(object instanceof Prerequisite))
         {
             return false;
         }
         Prerequisite other = (Prerequisite) object;
         if ((this.prerequisitePK == null && other.prerequisitePK != null) || (this.prerequisitePK != null && !this.prerequisitePK.equals(other.prerequisitePK)))
         {
             return false;
         }
         return true;
     }
 
     /**
      * Public method overriding method for converting object to string by
      * attaching the following string to the beginning:
      * "adg.red.models.Prerequisite[prerequisitePK="
      * <p/>
      * @return - String literal
      */
     @Override
     public String toString()
     {
         return "adg.red.models.Prerequisite[ prerequisitePK=" + prerequisitePK + " ]";
     }
 
     /**
      * Public method that returns the list of all prereq courses for a given
      * course
      * <p/>
      * @param course -- A specific course that we need all the prereqs for
      * <p/>
      * @return -- An object of type List containing all prereqs
      */
     public static List<Prerequisite> getByCourse(Course course)
     {
         return RedEntityManager.getEntityManager().createNamedQuery("Prerequisite.findByCourseNumberAndDepartmentId").setParameter("departmentId", course.getCoursePK().getDepartmentId()).setParameter("courseNumber", course.getCoursePK().getCourseNumber()).getResultList();
     }
 }
