 package war.webapp.model;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.hibernate.annotations.Cascade;
 
 import war.webapp.util.DateUtil;
 import war.webapp.util.MonthHelper;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Calendar;
 
 @Entity
 @Table(name = "day_duty")
 public class DayDuty extends BaseObject implements Serializable {
     private static final long serialVersionUID = 1842676162177859911L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     @Column(name = "date", nullable = false)
     @Temporal(TemporalType.TIMESTAMP)
     private Calendar date;
 
    @ManyToOne(cascade = {CascadeType.MERGE})
     @JoinColumn(name = "first_user")
     private User firstUser;
 
    @ManyToOne(cascade = {CascadeType.MERGE})
     @JoinColumn(name = "second_user")
     private User secondUser;
 
     @Column(name = "first_user_remarks", nullable = true)
     private String firstUserRemarks;
 
     @Column(name = "second_user_remarks", nullable = true)
     private String secondUserRemarks;
 
     @Column(name = "first_user_punishment", nullable = true)
     private String firstUserPunishment;
 
     @Column(name = "second_user_punishment", nullable = true)
     private String secondUserPunishment;
 
     @Column
     private String floor;
 
     private Boolean isOwnSecondDuty;
     private Boolean isOwnFirstDuty;
 
     public DayDuty() {
     }
 
     public Long getId() {
         return id;
     }
 
     public Calendar getDate() {
         return date;
     }
 
     public User getFirstUser() {
         return firstUser;
     }
 
     public User getSecondUser() {
         return secondUser;
     }
 
     public String getFloor() {
         return floor;
     }
 
     public String getFirstUserRemarks() {
         return firstUserRemarks;
     }
 
     public void setFirstUserRemarks(String firstUserRemarks) {
         this.firstUserRemarks = firstUserRemarks;
     }
 
     public String getSecondUserRemarks() {
         return secondUserRemarks;
     }
 
     public void setSecondUserRemarks(String secondUserRemarks) {
         this.secondUserRemarks = secondUserRemarks;
     }
 
     public String getFirstUserPunishment() {
         return firstUserPunishment;
     }
 
     public void setFirstUserPunishment(String firstUserPunishment) {
         this.firstUserPunishment = firstUserPunishment;
     }
 
     public String getSecondUserPunishment() {
         return secondUserPunishment;
     }
 
     public void setSecondUserPunishment(String secondUserPunishment) {
         this.secondUserPunishment = secondUserPunishment;
     }
 
     @Transient
     public int getStudyWeek() {
         int totalDaysFromFirstSeptember = getDaysCountFromFirstSeptember();
         return (totalDaysFromFirstSeptember / 7) % 4 + 1;
     }
 
     @Transient
     private int getDaysCountFromFirstSeptember() {
         int daysCountFromFirstSeptember = -1;
         for (int i = Calendar.SEPTEMBER; i < date.get(Calendar.MONTH); ++i) {
             daysCountFromFirstSeptember += MonthHelper.getDaysNumInMonth(i);
         }
         if (date.get(Calendar.MONTH) < Calendar.SEPTEMBER) {
             daysCountFromFirstSeptember += 122;
             for (int i = Calendar.JANUARY; i < date.get(Calendar.MONTH); ++i) {
                 daysCountFromFirstSeptember += MonthHelper.getDaysNumInMonth(i);
             }
         }
         int startStudyYear = getStartStudyYear();
         daysCountFromFirstSeptember += DateUtil.getDayOfWeekForFirstSeptember(startStudyYear) - 1;
         daysCountFromFirstSeptember += date.get(Calendar.DAY_OF_MONTH);
         return daysCountFromFirstSeptember;
     }
     
     @Transient
     private int getStartStudyYear() {
         int startStudyYear = date.get(Calendar.YEAR);
         if (date.get(Calendar.MONTH) < Calendar.SEPTEMBER) {
             startStudyYear--;
         }
         return startStudyYear;
     }
     
     @Transient
     public int getDayOfMonth() {
         return date.get(Calendar.DAY_OF_MONTH);
     }
 
     @Transient
     public int getDayOfWeek() {
         return date.get(Calendar.DAY_OF_WEEK);
     }
 
     @Transient
     public boolean isFirstEmpty() {
         return firstUser == null || firstUser.isEmptyUser();
     }
 
     @Transient
     public boolean isSecondEmpty() {
         return secondUser == null || secondUser.isEmptyUser();
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setDate(Calendar date) {
         this.date = date;
     }
 
     public void setFirstUser(User firstUser) {
         this.firstUser = firstUser;
     }
 
     public void setSecondUser(User secondUser) {
         this.secondUser = secondUser;
     }
 
     public void setFloor(String floor) {
         this.floor = floor;
     }
 
     @Column(name = "own_first_duty", columnDefinition = "BIT(1)")
     public Boolean isOwnFirstDuty() {
         return isOwnFirstDuty;
     }
     @Column(name = "own_second_duty", columnDefinition = "BIT(1)")
     public Boolean isOwnSecondDuty() {
         return isOwnSecondDuty;
     }
     public void setOwnFirstDuty(Boolean ownFirstDuty) {
         isOwnFirstDuty = ownFirstDuty;
     }
 
 
 
     public void setOwnSecondDuty(Boolean ownSecondDuty) {
         isOwnSecondDuty = ownSecondDuty;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (!(o instanceof DayDuty)) {
             return false;
         }
 
         final DayDuty dutyUser = (DayDuty) o;
 
         return date.equals(dutyUser.getDate()) && firstUser.equals(dutyUser.getFirstUser())
                 && secondUser.equals(dutyUser.getSecondUser());
     }
 
     /**
      * {@inheritDoc}
      */
     public int hashCode() {
         return (date != null ? date.hashCode() : 0);
     }
 
     /**
      * {@inheritDoc}
      */
     public String toString() {
         return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("date", date.toString())
                 .append("first_shift", firstUser).append("second_shift", secondUser).toString();
     }
 
 }
