 /*
  *
  *
  */
 package adg.red.models;
 
 import adg.red.locale.LocaleManager;
 import adg.red.utils.RedEntityManager;
 import java.io.Serializable;
 import java.math.BigDecimal;
 
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
  *
  * @author harsimran.maan
  */
 @Entity
 @Table(name = "Enrolment")
 @XmlRootElement
 @NamedQueries(
         {
     @NamedQuery(name = "Enrolment.findAll", query = "SELECT e FROM Enrolment e"),
     @NamedQuery(name = "Enrolment.findByStudentId", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.studentId = :studentId"),
     @NamedQuery(name = "Enrolment.findBySectionId", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.sectionId = :sectionId"),
     @NamedQuery(name = "Enrolment.findByCourseNumber", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.courseNumber = :courseNumber"),
     @NamedQuery(name = "Enrolment.findByDepartmentId", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.departmentId = :departmentId"),
     @NamedQuery(name = "Enrolment.findByTermYear", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.termYear = :termYear"),
     @NamedQuery(name = "Enrolment.findBySessionId", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.sessionId = :sessionId"),
     @NamedQuery(name = "Enrolment.findBySectionTypeId", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.sectionTypeId = :sectionTypeId"),
     // @NamedQuery(name = "Enrolment.findByIsActive", query = "SELECT e FROM Enrolment e WHERE e.isActive = :isActive"),
     @NamedQuery(name = "Enrolment.findByEnrolmentPK", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK=:enrolmentPK"),
     @NamedQuery(name = "Enrolment.findBySectionPK", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.courseNumber = :courseNumber AND e.enrolmentPK.departmentId = :departmentId AND e.enrolmentPK.sectionId = :sectionId AND e.enrolmentPK.sectionTypeId = :sectionTypeId AND e.enrolmentPK.sessionId = :sessionId AND e.enrolmentPK.termYear = :termYear"),
    @NamedQuery(name = "Enrolment.findSumCreditsByStudentId", query = "SELECT CASE WHEN SUM(c.credits) IS NULL THEN 0 ELSE SUM(c.credits) END FROM Course c, Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE c.coursePK.courseNumber = e.enrolmentPK.courseNumber AND c.coursePK.departmentId = e.enrolmentPK.departmentId AND e.enrolmentPK.studentId = :studentId AND e.resultId.resultId = 100 AND e.enrolmentPK.sectionTypeId = 100"),
     @NamedQuery(name = "Enrolment.findActiveEnrolmentsByStudentId", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.studentId = :studentId AND e.resultId.resultId IS NOT NULL AND e.enrolmentPK.sectionTypeId = 100 "),
     @NamedQuery(name = "Enrolment.findEnrolmentsByStudentId", query = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.section LEFT JOIN FETCH e.student LEFT JOIN FETCH e.gradeId LEFT JOIN FETCH e.resultId WHERE e.enrolmentPK.studentId = :studentId AND e.isActive = 1 ")
 })
 public class Enrolment implements Serializable
 {
 
     private static final long serialVersionUID = 1L;
     @EmbeddedId
     protected EnrolmentPK enrolmentPK;
     @Basic(optional = false)
     @Column(name = "isActive")
     private boolean isActive;
     @JoinColumns(
             {
         @JoinColumn(name = "sectionId", referencedColumnName = "sectionId", insertable = false, updatable = false),
         @JoinColumn(name = "courseNumber", referencedColumnName = "courseNumber", insertable = false, updatable = false),
         @JoinColumn(name = "departmentId", referencedColumnName = "departmentId", insertable = false, updatable = false),
         @JoinColumn(name = "termYear", referencedColumnName = "termYear", insertable = false, updatable = false),
         @JoinColumn(name = "sessionId", referencedColumnName = "sessionId", insertable = false, updatable = false),
         @JoinColumn(name = "sectionTypeId", referencedColumnName = "sectionTypeId", insertable = false, updatable = false)
     })
     @ManyToOne(optional = false)
     private Section section;
     @JoinColumn(name = "studentId", referencedColumnName = "studentId", insertable = false, updatable = false)
     @ManyToOne(optional = false)
     private Student student;
     @Column(name = "score")
     private Integer score;
     @JoinColumn(name = "resultId", referencedColumnName = "resultId")
     @ManyToOne
     private Result resultId;
     @JoinColumn(name = "gradeId", referencedColumnName = "gradeId")
     @ManyToOne
     private Grade gradeId;
 
     public Enrolment()
     {
     }
 
     public Enrolment(EnrolmentPK enrolmentPK)
     {
         this.enrolmentPK = enrolmentPK;
     }
 
     public Enrolment(EnrolmentPK enrolmentPK, boolean isActive)
     {
         this.enrolmentPK = enrolmentPK;
         this.isActive = isActive;
     }
 
     public Enrolment(int studentId, int sectionId, int courseNumber, String departmentId, int termYear, int sessionId, int sectionTypeId)
     {
         this.enrolmentPK = new EnrolmentPK(studentId, sectionId, courseNumber, departmentId, termYear, sessionId, sectionTypeId);
     }
 
     public EnrolmentPK getEnrolmentPK()
     {
         return enrolmentPK;
     }
 
     public void setEnrolmentPK(EnrolmentPK enrolmentPK)
     {
         this.enrolmentPK = enrolmentPK;
     }
 
     public boolean getIsActive()
     {
         return isActive;
     }
 
     public void setIsActive(boolean isActive)
     {
         this.isActive = isActive;
     }
 
     public Section getSection()
     {
         return section;
     }
 
     public void setSection(Section section)
     {
         this.section = section;
     }
 
     public Student getStudent()
     {
         return student;
     }
 
     public void setStudent(Student student)
     {
         this.student = student;
     }
 
     @Override
     public int hashCode()
     {
         int hash = 0;
         hash += (enrolmentPK != null ? enrolmentPK.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object)
     {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Enrolment))
         {
             return false;
         }
         Enrolment other = (Enrolment) object;
         if ((this.enrolmentPK == null && other.enrolmentPK != null) || (this.enrolmentPK != null && !this.enrolmentPK.equals(other.enrolmentPK)))
         {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString()
     {
         return "adg.red.models.Enrolment[ enrolmentPK=" + enrolmentPK + " ]";
     }
 
     public void save()
     {
         RedEntityManager.save(this);
     }
 
     public static List<Enrolment> getAllEnrolment()
     {
         return RedEntityManager.getEntityManager().createNamedQuery("Enrolment.findAll").getResultList();
     }
 
     public static List<Enrolment> getEnrolmentBySectionPK(SectionPK sec)
     {
         return RedEntityManager.getEntityManager().createNamedQuery("Enrolment.findBySectionPK")
                 .setParameter("sectionId", sec.getSectionId())
                 .setParameter("courseNumber", sec.getCourseNumber())
                 .setParameter("departmentId", sec.getDepartmentId())
                 .setParameter("sessionId", sec.getSessionId())
                 .setParameter("termYear", sec.getTermYear())
                 .setParameter("sectionTypeId", sec.getSectionTypeId())
                 .getResultList();
     }
 
     public static Enrolment getEnrolmentByEnrolmentPK(EnrolmentPK enrolmentPK) throws Exception
     {
         List<Enrolment> enrolList = RedEntityManager.getEntityManager()
                 .createNamedQuery("Enrolment.findByEnrolmentPK")
                 .setParameter("enrolmentPK", enrolmentPK)
                 .getResultList();
         if (enrolList.size() == 1)
         {
             return enrolList.get(0);
         }
         else
         {
             throw new Exception(LocaleManager.get(33));
         }
     }
 
     public static List<Enrolment> getEnrolmentsByStudentId(int studentId)
     {
         return RedEntityManager.getEntityManager()
                 .createNamedQuery("Enrolment.findEnrolmentsByStudentId")
                 .setParameter("studentId", studentId)
                 .getResultList();
     }
 
     public static List<Enrolment> getActiveEnrolmentsByStudentId(int studentId)
     {
         return RedEntityManager.getEntityManager()
                 .createNamedQuery("Enrolment.findActiveEnrolmentsByStudentId")
                 .setParameter("studentId", studentId)
                 .getResultList();
     }
 
     public static int getSumCreditsByStudentId(int studentId)
     {
         return ((BigDecimal) RedEntityManager.getEntityManager()
                 .createNamedQuery("Enrolment.findSumCreditsByStudentId")
                 .setParameter("studentId", studentId)
                 .getSingleResult()).intValue();
     }
 
     public String getDepartmentIdAndCourseNumber()
     {
         return this.enrolmentPK.getDepartmentId() + " " + this.enrolmentPK.getCourseNumber();
     }
 
     public String getTermYearAndSession()
     {
         return this.enrolmentPK.getTermYear() + " " + Session.getBySessionId(this.enrolmentPK.getSessionId()).getName();
     }
 
     public Integer getScore()
     {
         return score;
     }
 
     public String getScoreAsString()
     {
         return score == null ? "" : score.toString();
     }
 
     public int getCredits()
     {
         return this.section.getCourse().getCredits();
     }
 
     public void setScore(Integer score)
     {
         this.score = score;
     }
 
     public Result getResultId()
     {
         return resultId;
     }
 
     public String getResult()
     {
         return resultId == null ? "" : resultId.getName();
     }
 
     public void setResultId(Result resultId)
     {
         this.resultId = resultId;
     }
 
     public Grade getGradeId()
     {
         return gradeId;
     }
 
     public String getGrade()
     {
         return gradeId == null ? "" : gradeId.getName();
     }
 
     public void setGradeId(Grade gradeId)
     {
         this.gradeId = gradeId;
     }
 
     public String getDepartmentAndCourseAndSection()
     {
         return this.getEnrolmentPK().getDepartmentId() + " "
                 + String.valueOf(this.getEnrolmentPK().getCourseNumber()) + " Section "
                 + String.valueOf(this.getEnrolmentPK().getSectionId());
     }
 
     public String getActivity()
     {
         List<SectionType> sectionTypeList = RedEntityManager.getEntityManager()
                 .createNamedQuery("SectionType.findBySectionTypeId")
                 .setParameter("sectionTypeId", this.getEnrolmentPK().getSectionTypeId())
                 .getResultList();
         return sectionTypeList.get(0).getName();
     }
 
     public String getTerm()
     {
         List<Session> sessionList = RedEntityManager.getEntityManager()
                 .createNamedQuery("Session.findBySessionId")
                 .setParameter("sessionId", this.getEnrolmentPK().getSessionId())
                 .getResultList();
         return sessionList.get(0).getName() + " " + String.valueOf(this.getEnrolmentPK().getTermYear());
     }
 
     public String getCredit()
     {
         List<Course> courseList = RedEntityManager.getEntityManager()
                 .createNamedQuery("Course.findByDepartmentAndCourseNumber")
                 .setParameter("courseNumber", this.getEnrolmentPK().getCourseNumber())
                 .setParameter("departmentId", this.getEnrolmentPK().getDepartmentId())
                 .getResultList();
         return String.valueOf(courseList.get(0).getCredits());
     }
 }
