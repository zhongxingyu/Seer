  //*****************************************************
  /* (INCOMPLETE)
  ProgramCourseList.java -- Model for ProgramCourseList table
 
  @Contributors:  Harsimran
 
  @Purpose:   Generates various quesries on ProgramCourseList table
  *
  @Copyright ADG (2013) - Open License
  */
 //*****************************************************
 package adg.red.models;
 
 import java.io.Serializable;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Creating queries to be used in methods followed
  */
 @Entity
 @Table(name = "ProgramCourseList")
 @XmlRootElement
 @NamedQueries(
         {
     @NamedQuery(name = "ProgramCourseList.findAll", query = "SELECT p FROM ProgramCourseList p"),
     @NamedQuery(name = "ProgramCourseList.findByProgramName", query = "SELECT p FROM ProgramCourseList p WHERE p.programCourseListPK.programName = :programName"),
     @NamedQuery(name = "ProgramCourseList.findByProgramDepartmentId", query = "SELECT p FROM ProgramCourseList p WHERE p.programCourseListPK.programDepartmentId = :programDepartmentId"),
     @NamedQuery(name = "ProgramCourseList.findByCourseNumber", query = "SELECT p FROM ProgramCourseList p WHERE p.programCourseListPK.courseNumber = :courseNumber"),
     @NamedQuery(name = "ProgramCourseList.findByCourseDepartmentId", query = "SELECT p FROM ProgramCourseList p WHERE p.programCourseListPK.courseDepartmentId = :courseDepartmentId"),
     @NamedQuery(name = "ProgramCourseList.findByIsActive", query = "SELECT p FROM ProgramCourseList p WHERE p.isActive = :isActive")
 })
 public class ProgramCourseList implements Serializable
 {
 
     private static final long serialVersionUID = 1L;
     @EmbeddedId
     protected ProgramCourseListPK programCourseListPK;
     @Basic(optional = false)
     @Column(name = "isActive")
     private boolean isActive;
 
     /**
      * Public default class constructor
      */
     public ProgramCourseList()
     {
     }
 
     /**
      * Public class constructor using programCourseListPK that contains the
      * primary key attributes of ProgramCourseList (programName,
      * programDepartmentid, courseNumber and courseDepartmentId)
      * <p/>
      * @param programCourseListPK -- Object type ProgramCourseListPk
      */
     public ProgramCourseList(ProgramCourseListPK programCourseListPK)
     {
         this.programCourseListPK = programCourseListPK;
     }
 
     /**
      * Public class constructor using programCourseListPK that contains the
      * primary key attributes of ProgramCourseList (programName,
      * programDepartmentid, courseNumber and courseDepartmentId) + isActive
      * attribute
      * <p/>
      * @param programCourseListPK -- Object type ProgramCourseListPk
      * @param isActive            -- Boolean containing isActive attribute value
      */
     public ProgramCourseList(ProgramCourseListPK programCourseListPK, boolean isActive)
     {
         this.programCourseListPK = programCourseListPK;
         this.isActive = isActive;
     }
 
     /**
      * Public class constructor using attributes of ProgramCourseList
      * (programName, programDepartmentid, courseNumber and courseDepartmentId)
      * <p/>
      * @param programName         -- String containing programName attribute
      * @param programDepartmentId -- String containing programDepartmentId
      *                            attribute
      * @param courseNumber        -- Integer containing courseNumber attribute
      * @param courseDepartmentId  -- String containing the courseDepartmentId
      *                            attribute
      */
     public ProgramCourseList(String programName, String programDepartmentId, int courseNumber, String courseDepartmentId)
     {
         this.programCourseListPK = new ProgramCourseListPK(programName, programDepartmentId, courseNumber, courseDepartmentId);
     }
 
     /**
      * Public getter method that returns the ProgramCourseListPK (which
      * contains: programName, programDepartmentid, courseNumber and
      * courseDepartmentId -attributes) of a ProgramCourseList object
      * <p/>
      * @return -- Object type programCourseListPK
      */
     public ProgramCourseListPK getProgramCourseListPK()
     {
         return programCourseListPK;
     }
 
     /**
      * Public setter method that sets the ProgramCourseListPK of a
      * ProgramCourseList
      * <p/>
      * @param programCourseListPK -- Object type PrgramCourseListPK
      */
     public void setProgramCourseListPK(ProgramCourseListPK programCourseListPK)
     {
         this.programCourseListPK = programCourseListPK;
     }
 
     /**
      * Public method that gets the active/inactive value of a ProgramCourseList
      * <p/>
      * @return -- A boolean indicating ProgramCourseList is active = "true" /
      *         inactive = "false"
      */
     public boolean getIsActive()
     {
         return isActive;
     }
 
     /**
      * Public method that sets the active/inactive value of a ProgramCourseList
      * <p/>
      * @param isActive -- A boolean indicating ProgramCourseList course is
      *                 active = "true" / inactive = "false"
      */
     public void setIsActive(boolean isActive)
     {
         this.isActive = isActive;
     }
 
     /**
      * Public method overriding hasCode() using ProgramCourseList specific hash
      * value. Adds programCourseListPK to make the index unique
      * <p/>
      * @return - Integer unique hashCode index value
      */
     @Override
     public int hashCode()
     {
         int hash = 0;
         hash += (programCourseListPK != null ? programCourseListPK.hashCode() : 0);
         return hash;
     }
 
     /**
      * Public method overriding equals(Object object) method that returns true
      * if the given object is an instance of a ProgramCourseList object and is
      * the same as current ProgramCourseList object
      * <p/>
      * @param object -- Pass any Object
      * <p/>
      * @return -- true if object is of type ProgramCourseList and the same as
      *         current ProgramCourseList object, false otherwise
      */
     @Override
     public boolean equals(Object object)
     {
         if (!(object instanceof ProgramCourseList))
         {
             return false;
         }
         ProgramCourseList other = (ProgramCourseList) object;
         if ((this.programCourseListPK == null && other.programCourseListPK != null) || (this.programCourseListPK != null && !this.programCourseListPK.equals(other.programCourseListPK)))
         {
             return false;
         }
         return true;
     }
 
     /**
      * Public method overriding method for converting object to string by
      * attaching the following string to it: "adg.red.models.ProgramCourseList[
      * programCourseListPK"
      * <p/>
      * @return - String literal
      */
     @Override
     public String toString()
     {
         return "adg.red.models.ProgramCourseList[ programCourseListPK=" + programCourseListPK + " ]";
     }
 }
