 package edu.northwestern.bioinformatics.studycalendar.domain.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
 import edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed;
 import edu.northwestern.bioinformatics.studycalendar.domain.TransientCloneable;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Parameter;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Calendar;
 
 /**
  * An amendment is a revision containing all the {@link Delta}s needed to
  * revert a calendar to its previous state.  The stored {@link edu.northwestern.bioinformatics.studycalendar.domain.Study}
  * always reflects the latest approved amendment.
  * <p/>
  * For example, if you have a calendar C with amendments A0, A1, A2, and A3,
  * the calendar loaded from the database will reflect amendment A3.  If you want
  * to see the calendar as it existed at A1, you need to do a reverse merge from 3 to 2
  * and then from 2 to 1.
  * {@link edu.northwestern.bioinformatics.studycalendar.service.AmendmentService#getAmendedStudy}
  * implements this process.
  *
  * @author Rhett Sutphin
  * @see edu.northwestern.bioinformatics.studycalendar.service.DeltaService
  */
 @Entity
 @Table(name = "amendments")
 @GenericGenerator(name = "id-generator", strategy = "native",
     parameters = {
         @Parameter(name = "sequence", value = "seq_amendments_id")
     }
 )
 public class Amendment
     extends AbstractMutableDomainObject 
     implements Revision, NaturallyKeyed, Cloneable, TransientCloneable<Amendment>
 {
     public static final String INITIAL_TEMPLATE_AMENDMENT_NAME = "[Original]";
 
     private boolean memoryOnly;
     private Amendment previousAmendment;
     private Date date;
     private String name;
     private List<Delta<?>> deltas;
     private boolean mandatory;
     private Date releasedDate;
 
     public Amendment() {
         this(null);
     }
 
     public Amendment(String name) {
         this.name = name;
         deltas = new ArrayList<Delta<?>>();
         mandatory = true;
     }
 
     ////// LOGIC
 
     @Transient
     public String getDisplayName() {
         if (INITIAL_TEMPLATE_AMENDMENT_NAME.equals(getName())) {
             return "Initial template";
         } else {
             StringBuilder n = new StringBuilder();
             if (getDate() != null) {
                 n.append(FormatTools.getLocal().formatDate(getDate()));
             } else {
                 n.append("Timeless");
             }
             if (!StringUtils.isBlank(getName())) {
                 n.append(" (").append(getName()).append(')');
             }
             return n.toString();
         }
     }
 
     @Transient
     public String getNaturalKey() {
         return new Key(getDate(), getName()).toString();
     }
 
     public static Key decomposeNaturalKey(String key) {
         return Key.create(key);
     }
 
     public static DateFormat createNaturalKeyDateFormat() {
         return new SimpleDateFormat("yyyy-MM-dd");
     }
 
     @Transient
     public boolean isFirst() {
         return getPreviousAmendment() == null;
     }
 
     /**
      * Is this the internal representation of the unamended original protocol?
      * Note that this is not the same as {@link #isFirst} -- in theory the first version
      * of a protocol that PSC knows about could already be amended.  In that case,
      * {@link #isFirst} would be true but this method would return false.  (Note that
      * there's not currently any way to make this situation happen through the UI,
      * but it may be added later.)
      *
      * @see #isFirst
      */
     @Transient
     public boolean isInitialTemplate() {
         return INITIAL_TEMPLATE_AMENDMENT_NAME.equals(getName());
     }
 
     /**
      * Returns true IFF the candidate is the previous amendment of this
      * one or the previous of any of its previous amendments.  In other words, is the candidate
      * part of the history that terminates in this amendment?
      *
      * @param candidate
      * @return
      */
     public boolean hasPreviousAmendment(Amendment candidate) {
         return this.getPreviousAmendment() != null
                 && (this.getPreviousAmendment() == candidate
                 || this.getPreviousAmendment().hasPreviousAmendment(candidate));
     }
 
     @Transient
     public int getPreviousAmendmentsCount() {
         if (getPreviousAmendment() == null) {
             return 0;
         } else {
             return getPreviousAmendment().getPreviousAmendmentsCount() + 1;
         }
     }
 
     public void addDelta(Delta<?> delta) {
         getDeltas().add(delta);
         delta.setRevision(this);
     }
 
     @Transient
     public Date getLastModifiedDate() {
         if (this.getReleasedDate() == null) {
             return this.getUpdatedDate();
         }
         if (this.getUpdatedDate() != null && this.getUpdatedDate().compareTo(this.getReleasedDate()) > 0) {
             return this.getUpdatedDate();
         } else {
             return this.getReleasedDate();
         }
     }
 
     @Transient
     public Date getUpdatedDate() {
         Date updated = null;
         for (Delta<?> delta : getDeltas()) {
             for (Change change : delta.getChanges()) {
                 if (updated == null) {
                     updated = change.getUpdatedDate();
                 } else if (ComparisonTools.nullSafeCompare(updated, change.getUpdatedDate()) < 0) {
                     updated = change.getUpdatedDate();
                 }
             }
         }
         return updated;
     }
 
     ////// IMPLEMENTATION OF TransientCloneable
 
     @Transient
     public boolean isMemoryOnly() {
         return memoryOnly;
     }
 
     public void setMemoryOnly(boolean memoryOnly) {
         this.memoryOnly = memoryOnly;
         for (Delta<?> delta : getDeltas()) {
             delta.setMemoryOnly(memoryOnly);
         }
         if (getPreviousAmendment() != null) {
             getPreviousAmendment().setMemoryOnly(memoryOnly);
         }
     }
 
     public Amendment transientClone() {
         Amendment clone = clone();
         clone.setMemoryOnly(true);
         return clone;
     }
 
     ////// BEAN PROPERTIES
 
     @OneToMany
     @JoinColumn(name = "amendment_id", nullable = false)
     @OrderBy // order by ID for testing consistency
     public List<Delta<?>> getDeltas() {
         return deltas;
     }
 
     public void setDeltas(List<Delta<?>> deltas) {
         this.deltas = deltas;
     }
 
     @ManyToOne
     @JoinColumn(name = "previous_amendment_id")
     public Amendment getPreviousAmendment() {
         return previousAmendment;
     }
 
     public void setPreviousAmendment(Amendment previousAmendment) {
         this.previousAmendment = previousAmendment;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Column(name = "amendment_date", nullable = false)
     @Temporal(TemporalType.DATE)
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public boolean isMandatory() {
         return mandatory;
     }
 
     public void setMandatory(boolean mandatory) {
         this.mandatory = mandatory;
     }
 
     public Date getReleasedDate() {
         return releasedDate;
     }
 
     public void setReleasedDate(final Date releasedDate) {
         this.releasedDate = releasedDate;
     }
 
     ////// OBJECT METHODS
 
     @Override
     public String toString() {
         return new StringBuffer(getClass().getSimpleName())
                 .append("[date=").append(getDate())
                 .append("; name=").append(getName())
                 .append("; prev=").append(getPreviousAmendment() == null ? null : getPreviousAmendment().getDisplayName())
                 .append(']').toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Amendment)) return false;
 
         Amendment amendment = (Amendment) o;
 
         if (date != null ? !date.equals(amendment.date) : amendment.date != null) return false;
         if (name != null ? !name.equals(amendment.name) : amendment.name != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = previousAmendment != null ? previousAmendment.hashCode() : 0;
         result = 31 * result + (date != null ? date.hashCode() : 0);
         result = 31 * result + (name != null ? name.hashCode() : 0);
         return result;
     }
 
     public Differences deepEquals(Object o) {
         Differences differences = new Differences();
         if (this == o) return differences;
         if (o == null || getClass() != o.getClass()) {
             differences.addMessage("object is not an instance of Amendment");
             return differences;
         }
         Amendment amendment = (Amendment) o;
 
         if (getDate() != null ? !date.equals(amendment.getDate()) : amendment.getDate() != null) {
             differences.addMessage("amendment date " + getDate() +" does not match to " +amendment.getDate());
         }
 
         if (name != null ? !name.equals(amendment.name) : amendment.name != null) {
             differences.addMessage("amendment name " + name +" does not match to " +amendment.name);
         }
 
         if (deltas.size() != amendment.getDeltas().size()) {
             differences.addMessage("No. of deltas " +deltas.size()+" do not match to " +amendment.getDeltas().size());
         } else {
             int index = 0;
             for (Delta delta: amendment.getDeltas()) {
                 Delta matchingDelta = getMatchingDelta(delta.getGridId(), delta.getNode().getGridId(), delta.getClass());
                 if (matchingDelta != null) {
                     Differences deltaDifferences = matchingDelta.deepEquals(delta);
                     if (deltaDifferences.hasDifferences()) {
                         differences.addChildDifferences(delta.getClass().getSimpleName(), deltaDifferences);
                     }
                     index++;
                 } else {
                 differences.addMessage("No matching delta found in released amendment");
                 }
             }
         }
         return differences;
     }
 
     @Override
     public Amendment clone() {
         try {
             Amendment clone = (Amendment) super.clone();
             if (getPreviousAmendment() != null) {
                 clone.setPreviousAmendment(getPreviousAmendment().clone());
             }
             clone.setDeltas(new ArrayList<Delta<?>>(getDeltas().size()));
             for (Delta<?> delta : getDeltas()) {
                 clone.addDelta(delta.clone());
             }
             return clone;
         } catch (CloneNotSupportedException e) {
             throw new StudyCalendarError("Clone is supported", e);
         }
     }
 
     @Transient
     public Delta getMatchingDelta(String gridId, String nodeId) {
         for (Delta delta : this.getDeltas()) {
             if (delta.getGridId().equals(gridId) && delta.getNode() != null && delta.getNode().getGridId().equals(nodeId)) {
                 return delta;
             }
         }
         return null;
     }
 
     @Transient
     public Delta getMatchingDelta(String gridId, String nodeId, Class klass) {
         for (Delta delta :  this.getDeltas()) {
             if (delta.getGridId().equals(gridId) && delta.getClass().equals(klass) &&
                 delta.getNode() != null && delta.getNode().getGridId().equals(nodeId)) {
                 return delta;
             }
         }
         return null;
     }
 
     public static final class Key {
         private Date date;
         private Date dateNext;
         private String name;
         public Key(Date date, String name, Date dateNext) {
             this.date = date;
             this.name = name;
             this.dateNext = dateNext;
         }
 
         public Key(Date date, String name) {
             this.date = date;
             this.name = name;
         }
 
         public static Key create(String keyStr) {
             if (keyStr == null) throw new NullPointerException("Cannot decompose null");
             String dateStr, name = null;
             int tildeLoc = keyStr.indexOf('~');
             if (tildeLoc >= 0) {
                 name = keyStr.substring(tildeLoc + 1);
                 dateStr = keyStr.substring(0, tildeLoc);
             } else {
                 dateStr = keyStr;
             }
 //            String[] dateValue = dateStr.split("\\-");
 //            String dateNextStr = dateValue[0].concat("-").concat(dateValue[1]).concat("-").concat(String.valueOf(Integer.parseInt(dateValue[2])+1));
             Date date;
 //            Date dateNext;
             try {
                 date = createNaturalKeyDateFormat().parse(dateStr);
 //                dateNext = createNaturalKeyDateFormat().parse(dateNextStr);
             } catch (ParseException e) {
                 throw new StudyCalendarValidationException(
                         "Date is not correct format for amendment key (should be yyyy-MM-dd): %s", e, dateStr);
             }
             return new Key(date, name);
         }
 
         public Date getDate() {
             return date;
         }
 
         public void setDate(Date date) {
             this.date = date;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public Date getDateNext() {
             Calendar c1 = Calendar.getInstance();
             c1.setTime(getDate());
             c1.add(Calendar.DATE, 1);
             return c1.getTime();
         }
 
         public void setDateNext(Date dateNext) {
             this.dateNext = dateNext;
         }
 
         @Override
         public String toString() {
             StringBuilder sb = new StringBuilder()
                     .append(createNaturalKeyDateFormat().format(getDate()));
             if (!StringUtils.isBlank(getName())) {
                 sb.append('~').append(getName());
             }
             return sb.toString();
         }
     }
 
 }
