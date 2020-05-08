 package com.artisztikum.ac.ac;
 
 import java.net.URL;
 
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlAccessOrder;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorOrder;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 
 import com.artisztikum.ac.ac.adapters.TaskPriorityAdapter;
 
 /**
  * An ActiveCollab task.
  *
  * @author Adam DAJKA (dajka@artisztikum.hu)
  *
  */
 @XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
 @XmlAccessorType(XmlAccessType.FIELD)
 @Data
 @EqualsAndHashCode
 public final class Task
 {
 	/**
 	 * Internal id. Refer to AC api docs, we never use this.
 	 */
 	private Long id;
 
 	/**
 	 * The task id. As in the permalink.
 	 */
 	@XmlElement(name = "task_id")
 	private Long taskId;
 
 	/**
 	 * Name of the task.
 	 */
 	private String name;
 
 	/**
 	 * Priority of the task.
 	 */
 	@XmlJavaTypeAdapter(TaskPriorityAdapter.class)
 	private TaskPriority priority;
 
 	/**
 	 * Body (opening comment).
 	 */
 	private String body;
 
 	/**
 	 * Version. Used for cache validation.
 	 */
 	private Long version;
 
 	/**
 	 * The permalink to the task in the AC web interface.
 	 */
 	private URL permalink;
 
 	/**
 	 * The assignee of this task.
 	 */
 	private User assignee;
 
 	/**
 	 * The milestone of this task.
 	 */
 	@XmlElement(name = "milestone")
 	private Milestone milestone;
 	/**
 	 * The creation datetime.
 	 */
 	@XmlElement(name = "created_on")
 	private ACDate createdOn;
 
 	/**
 	 * The user id of the creator.
 	 */
 	@XmlElement(name = "created_by")
 	private User createdBy;
 
 	/**
 	 * Last updated datetime.
 	 */
 	@XmlElement(name = "updated_on")
 	private ACDate updatedOn;
 
 	/**
 	 * The user id of the last updater.
 	 */
 	@XmlElement(name = "updated_by")
 	private User updatedBy;
 
 	/**
 	 * Due on date.
 	 */
 	@XmlElement(name = "due_on")
 	private ACDate dueOn;
 
	/**
	 * Overwrite the values for empty subelements to {@code null} in order to easily tested in the velocity macros.
	 *
	 * @param u
	 *            The unmarshaller
	 * @param parent
	 *            The parent
	 */
 	public void afterUnmarshal(final Unmarshaller u, final Object parent)
 	{
 		if (null == assignee.getId()) {
 			assignee = null;
 		}
 
 		if (null == updatedBy.getId()) {
 			updatedBy = createdBy;
 		}
 
 		if (null == updatedOn) {
 			updatedOn = createdOn;
 		}
 	}
 }
