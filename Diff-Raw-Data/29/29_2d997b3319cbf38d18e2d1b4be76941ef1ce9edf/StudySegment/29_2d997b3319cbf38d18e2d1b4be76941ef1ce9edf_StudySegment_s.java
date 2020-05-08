 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.DayRange;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.DefaultDayRange;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.EmptyDayRange;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Parameter;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import java.util.*;
 
 /**
  * @author Rhett Sutphin
  */
 @Entity
 @Table (name = "study_segments")
 @GenericGenerator(name="id-generator", strategy = "native",
     parameters = {
         @Parameter(name="sequence", value="seq_study_segments_id")
     }
 )
 public class StudySegment extends PlanTreeInnerNode<Epoch, Period, SortedSet<Period>> implements Named {
     public static final String TEMPORARY_NAME = "[Unnamed study segment]";
 
     private String name = TEMPORARY_NAME;
     private Integer cycleLength;
 
     ////// LOGIC
 
     public Class<Epoch> parentClass() { return Epoch.class; }
     public Class<Period> childClass() { return Period.class; }
 
     @Override protected SortedSet<Period> createChildrenCollection() { return new TreeSet<Period>(); }
 
     public void addPeriod(Period period) {
         addChild(period);
     }
 
     @Transient
     public String getQualifiedName() {
         StringBuilder sb = new StringBuilder();
         sb.append(getEpoch() == null ? getName() : getEpoch().getName());
         if (getEpoch() != null && getEpoch().isMultipleStudySegments()) {
             sb.append(": ").append(getName());
         }
         return sb.toString();
     }
 
     @Transient
     public int getLengthInDays() {
         return getDayRange().getDayCount();
     }
 
     @Transient
     public DayRange getDayRange() {
         if (getPeriods().size() == 0) {
             return EmptyDayRange.INSTANCE;
         } else {
             DefaultDayRange range = new DefaultDayRange(Integer.MAX_VALUE, Integer.MIN_VALUE);
             for (Period period : getPeriods()) {
                 range.addDayRange(period.getTotalDayRange());
             }
             return range;
         }
     }
 
     @Override
     public Period findNaturallyMatchingChild(String key) {
         Collection<Period> found = findMatchingChildrenByName(key);
         if (found.size() == 1) return found.iterator().next();
         found = findMatchingChildrenByGridId(key);
         if (found.size() == 1) return found.iterator().next();
         return null;
     }
 
     @Transient
     public boolean getHasTemporaryName() {
         return TEMPORARY_NAME.equals(getName());
     }
 
     ////// BEAN PROPERTIES
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     // This is annotated this way so that the IndexColumn in the parent
     // will work with the bidirectional mapping
     @ManyToOne (fetch = FetchType.LAZY)
     @JoinColumn(insertable=false, updatable=false)
     public Epoch getEpoch() {
         return getParent();
     }
 
     public void setEpoch(Epoch epoch) {
         setParent(epoch);
     }
 
     @OneToMany(mappedBy = "studySegment")
     @Cascade(value = { CascadeType.ALL })
     @Sort(type = SortType.NATURAL)
     public SortedSet<Period> getPeriods() {
         return getChildren();
     }
 
     public void setPeriods(SortedSet<Period> periods) {
         setChildren(periods);
     }
     
     public Integer getCycleLength() {
         return cycleLength;
     }
 
     public void setCycleLength(Integer cycleLength) {
         this.cycleLength = cycleLength;
     }
 
     public Differences deepEquals(Object o) {
         Differences differences =  new Differences();
         if (this == o) return differences;
         if (o == null || !(o instanceof StudySegment)){
             differences.addMessage("not an instance of StudySegment");
             return differences;
         }
 
         StudySegment studySegment = (StudySegment) o;
         if (name != null ? !name.equals(studySegment.name) : studySegment.name != null) {
             differences.addMessage(String.format("StudySegment name %s differs to %s", name, studySegment.name));
         }
 
 
         if (getPeriods() != null && studySegment.getPeriods() != null) {
             if (getPeriods().size() != studySegment.getPeriods().size()) {
                 differences.addMessage(String.format("total no.of periods %d differs to %d",
                         getPeriods().size(), studySegment.getPeriods().size()));
             } else {
                 Iterator iterator1 = getPeriods().iterator();
                 Iterator iterator2 = studySegment.getPeriods().iterator();
                 while (iterator1.hasNext() && iterator2.hasNext()) {
                     Period period1 = (Period)iterator1.next();
                     Period period2 = (Period)iterator2.next();
                     Differences periodDifferences = period1.deepEquals(period2);
                     if (periodDifferences.hasDifferences()) {
                         differences.addChildDifferences(String.format("StudySegment %s", name), periodDifferences);
                     }
                 }
             }
         }
 
         return differences; 
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof StudySegment)) return false;
 
         StudySegment studySegment = (StudySegment) o;
        if (name != null ? !name.equals(studySegment.name) : studySegment.name != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result;
         result = (name != null ? name.hashCode() : 0);
         return result;
     }
 }
