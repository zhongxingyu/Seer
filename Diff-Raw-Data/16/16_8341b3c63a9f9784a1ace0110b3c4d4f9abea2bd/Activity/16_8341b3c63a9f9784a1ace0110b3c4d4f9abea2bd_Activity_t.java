 package de.aidger.model.models;
 
 import static de.aidger.utils.Translation._;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.aidger.model.AbstractModel;
 import de.unistuttgart.iste.se.adohive.controller.IActivityManager;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IActivity;
 
 /**
  * Represents a single entry in the activity column of the database. Contains
  * functions to retrieve and change the data in the database.
  * 
  * @author aidGer Team
  */
 public class Activity extends AbstractModel<IActivity> implements IActivity {
 
     /**
      * References the corresponding assistant.
      */
     private Integer assistantId;
 
     /**
      * References the course by its unique id.
      */
     private Integer courseId;
 
     /**
      * The date on which this activity took place.
      */
     private Date date;
 
     /**
      * The sender of the activity.
      */
     private String sender;
 
     /**
      * The type of document referenced by this activity.
      */
     private String documentType;
 
     /**
      * The type of the activity.
      */
     private String type;
 
     /**
      * The processor of the activity.
      */
     private String processor;
 
     /**
      * The contents of the activity.
      */
     private String content;
 
     /**
      * Remarks for the activity.
      */
     private String remark;
 
     /**
      * Initializes the Activity class.
      */
     public Activity() {
         validatePresenceOf(new String[] { "date", "type", "processor",
                 "content", "documentType" }, new String[] { _("Date"),
                 _("Type"), _("Processor"), _("Content"), _("Document Type") });
         validateExistanceOf(new String[] { "assistantId" },
             new String[] { _("Assistant") }, new Assistant());
         validateExistanceOf(new String[] { "courseId" },
             new String[] { _("Course") }, new Course());
     }
 
     /**
      * Initializes the Activity class with the given activity model.
      * 
      * @param a
      *            the activity model
      */
     public Activity(IActivity a) {
         this();
         setId(a.getId());
         setAssistantId(a.getAssistantId());
         setContent(a.getContent());
         setCourseId(a.getCourseId());
         setDate(a.getDate());
         setDocumentType(a.getDocumentType());
         setProcessor(a.getProcessor());
         setRemark(a.getRemark());
         setSender(a.getSender());
         setType(a.getType());
 
         setNew(false);
     }
 
     /**
      * Clone the current activity
      */
     @Override
     public Activity clone() {
         Activity a = new Activity();
         a.setAssistantId(assistantId);
         a.setContent(content);
         a.setCourseId(courseId);
         a.setDate(date);
         a.setDocumentType(documentType);
         a.setProcessor(processor);
         a.setRemark(remark);
         a.setSender(sender);
         a.setType(type);
         a.doClone(this);
         return a;
     }
 
     /**
      * Check if two objects are equal.
      * 
      * @param o
      *            The other object
      * @return True if both are equal
      */
     @Override
     public boolean equals(Object o) {
         if (o instanceof Activity) {
             Activity a = (Activity) o;
             return (assistantId == null ? a.assistantId == null : assistantId
                 .equals(a.assistantId))
                     && (id == null ? a.id == null : id.equals(a.id))
                     && (courseId == null ? a.courseId == null : courseId
                         .equals(a.courseId))
                     && (date == null ? a.date == null : date.toString().equals(
                         a.date.toString()))
                     && (content == null ? a.content == null : content
                         .equals(a.content))
                     && (documentType == null ? a.documentType == null
                             : documentType.equals(a.documentType))
                     && (processor == null ? a.processor == null : processor
                         .equals(a.processor))
                     && (remark == null ? a.remark == null : remark
                         .equals(a.remark))
                     && (sender == null ? a.sender == null : sender
                         .equals(a.sender))
                     && (type == null ? a.type == null : type.equals(a.type));
         } else {
             return false;
         }
     }
 
     /**
      * Generate a unique hashcode for this instance.
      * 
      * @return The hashcode
      */
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 97 * hash + assistantId;
         hash = 97 * hash + courseId;
         hash = 97 * hash + (date != null ? date.hashCode() : 0);
         hash = 97 * hash + (sender != null ? sender.hashCode() : 0);
         hash = 97 * hash + (documentType != null ? documentType.hashCode() : 0);
         hash = 97 * hash + (type != null ? type.hashCode() : 0);
         hash = 97 * hash + (processor != null ? processor.hashCode() : 0);
         hash = 97 * hash + (content != null ? content.hashCode() : 0);
         hash = 97 * hash + (remark != null ? remark.hashCode() : 0);
         return hash;
     }
 
     /**
      * Custom validation function
      * 
      * @return True if everything is correct
      */
     public boolean validate() {
         boolean ret = true;
         if (type.length() > 20) {
             addError("type", _("Type"), _("is too long"));
             ret = false;
         }
         if (documentType.length() > 50) {
             addError("documentType", _("Document Type"), _("is too long"));
             ret = false;
         }
        if (processor.length() > 2) {
            addError("processor", _("Processor"), _("is too long"));
            ret = false;
        }
         return ret;
     }
 
     /**
      * Get all activities related to a specified assistant.
      * 
      * @param assistant
      *            The assistant for which all activities should be returned
      * @return The activities related to the assistant or null if none were
      *         found
      */
     @SuppressWarnings("unchecked")
     public List<Activity> getActivities(Assistant assistant)
             throws AdoHiveException {
         IActivityManager mgr = (IActivityManager) getManager();
         return castList(mgr.getActivities(assistant));
     }
 
     /**
      * Get all activities related to a specified course.
      * 
      * @param course
      *            The course for which all activities should be returned
      * @return The activities related to the course or null if none were found
      */
     @SuppressWarnings("unchecked")
     public List<Activity> getActivities(Course course) throws AdoHiveException {
         IActivityManager mgr = (IActivityManager) getManager();
         return castList(mgr.getActivities(course));
     }
 
     /**
      * Get all activities for a specified date range
      * 
      * @param from
      *            Start of the date range
      * @param to
      *            End of the date range
      * @return All activities that occured during that date range
      */
     public List<Activity> getActivities(Date from, Date to)
             throws AdoHiveException {
         IActivityManager mgr = (IActivityManager) getManager();
         return castList(mgr.getActivities(from, to));
     }
 
     /**
      * Get the id of the referenced assistant.
      * 
      * @return The id of the referenced assistant
      */
     @Override
     public Integer getAssistantId() {
         return assistantId;
     }
 
     /**
      * Get the contents of the activity.
      * 
      * @return The contents of the activity
      */
     @Override
     public String getContent() {
         return content;
     }
 
     /**
      * Get the id of the referenced course.
      * 
      * @return The id of the referenced course.
      */
     @Override
     public Integer getCourseId() {
         return courseId;
     }
 
     /**
      * Get the date of the activity.
      * 
      * @return The date of the activity.
      */
     @Override
     public Date getDate() {
         return date;
     }
 
     /**
      * Get the type of document.
      * 
      * @return The type of document
      */
     @Override
     public String getDocumentType() {
         return documentType;
     }
 
     /**
      * Get the processor of the activity.
      * 
      * @return The processor of the activity.
      */
     @Override
     public String getProcessor() {
         return processor;
     }
 
     /**
      * Get remarks to the activity.
      * 
      * @return Remarks to the activity
      */
     @Override
     public String getRemark() {
         return remark;
     }
 
     /**
      * Get the sender of the activity.
      * 
      * @return The sender of the activity.
      */
     @Override
     public String getSender() {
         return sender;
     }
 
     /**
      * Get the type of the activity.
      * 
      * @return The type of the activity
      */
     @Override
     public String getType() {
         return type;
     }
 
     /**
      * Set the id of the assistant referenced by this activity.
      * 
      * @param id
      *            The id of the assistant
      */
     @Override
     public void setAssistantId(Integer id) {
         assistantId = id;
     }
 
     /**
      * Set the contents of the activity.
      * 
      * @param cont
      *            The contents of the activity
      */
     @Override
     public void setContent(String cont) {
         content = cont;
     }
 
     /**
      * Set the id of the course referenced by this activity.
      * 
      * @param id
      *            The id of the course
      */
     @Override
     public void setCourseId(Integer id) {
         courseId = id;
     }
 
     /**
      * Set the date of the activity.
      * 
      * @param dt
      *            The date of the activity
      */
     @Override
     public void setDate(Date dt) {
         date = dt;
     }
 
     /**
      * Set the type of the document
      * 
      * @param type
      *            The type of the document
      */
     @Override
     public void setDocumentType(String type) {
         documentType = type;
     }
 
     /**
      * Set the processor of the activity.
      * 
      * @param proc
      *            The processor of the activity
      */
     @Override
     public void setProcessor(String proc) {
         processor = proc;
     }
 
     /**
      * Set remarks to the activity.
      * 
      * @param rem
      *            Remarks to the activity
      */
     @Override
     public void setRemark(String rem) {
         remark = rem;
     }
 
     /**
      * Set the sender of the activity.
      * 
      * @param send
      *            The sender of the activity
      */
     @Override
     public void setSender(String send) {
         sender = send;
     }
 
     /**
      * Set the type of the activity.
      * 
      * @param typec
      *            The type of the activity
      */
     @Override
     public void setType(String typec) {
         type = typec;
     }
 
     /**
      * Cast the list from interface to real class.
      * 
      * @param list
      *            The list to cast
      * @return The new list
      */
     protected List<Activity> castList(List<IActivity> list) {
         List<Activity> ret = new ArrayList<Activity>();
         for (IActivity act : list) {
             ret.add(new Activity(act));
         }
         return ret;
     }
 
 }
