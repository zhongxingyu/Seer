 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.persistence.Version;
 import javax.validation.constraints.NotNull;
 
 import models.ValueOccurrence.OccurrenceType;
 import play.Logger;
 import play.data.validation.Constraints;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 import utils.PerfLogger;
 
 import com.google.gson.annotations.Expose;
 
 /**
  * task entity managed by Ebean
  */
 @Entity
 @Table(uniqueConstraints = @UniqueConstraint(columnNames = { "environment", "path" }))
 public class Value extends Model {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3330873338215537922L;
 
 	@Id
 	@Expose
 	public Long id;
 	
 	@Version
 	public int version;
 
 	@Constraints.Required
 	@Expose
 	public String name;
 
 	@Constraints.MaxLength(value = 3000)
 	@Expose
 	public String description;
 
 	/** Parent in the hierarchy (Task) **/
 	@ManyToOne
 	@Required
 	@NotNull
 	public Task task;
 
 	@Required
 	@NotNull
 	@Expose
 	@Constraints.MaxLength(value = 30)
 	@Column(length=30)
 	public String environment;
 
 	@Required
 	@NotNull
 	@Expose
 	public String path;
 
 	/** children of the task (opposite of "task") **/
 	@OneToMany(mappedBy = "value", cascade = CascadeType.ALL)
 	// @Expose
 	public List<ValueOccurrence> occurrences = new ArrayList<ValueOccurrence>();
 
 	@Expose
 	@Required
 	@NotNull
 	public OccurrenceType type = OccurrenceType.Result;
 
 	@Expose
 	@Required
 	@NotNull
 	public StatusType statusType = StatusType.None;
 
 	@Expose
 	public Double fixedTarget = 0D;
 
 	@Expose
 	public Float targetPercentWarning = 20F;
 
 	@Expose
 	public Float targetPercentError = 80F;
 
 	@Expose
 	public Double averageValue = 0D;
 
 	@Expose
 	public Boolean shownInDashboard = false;
 
 	/**
 	 * finder
 	 */
 	public static Finder<Long, Value> find = new Finder<Long, Value>(Long.class, Value.class);
 
 	public Value(String name, String environment, String path, Task parent) {
 		this.name = name;
 		this.environment = environment;
 		this.path = path;
 		this.task = parent;
 
 		if (name.equalsIgnoreCase("Status")) {
 			this.statusType = StatusType.FixePercent;
 			this.fixedTarget = 0D;
 		} else if (name.equalsIgnoreCase("duration")) {
 			this.targetPercentWarning = 200F;
 			this.targetPercentError = 800F;
 		}
 	}
 
 	/**
 	 * Get or create a Task
 	 */
 	// public static Value getOrCreateValue(Task task, String name) {
 	// Value value = find.where().eq("task", task).eq("name", name).findUnique();
 	// if (value == null) {
 	// value = new Value();
 	// value.name = name;
 	// value.task = task;
 	//
 	// if (name.equalsIgnoreCase("Status")) {
 	// value.statusType = StatusType.FixePercent;
 	// value.fixedTarget = 0D;
 	// } else if (name.equalsIgnoreCase("duration")) {
 	// value.targetPercentWarning = 200F;
 	// value.targetPercentError = 800F;
 	// }
 	//
 	// value.save();
 	// }
 	//
 	// return value;
 	//
 	// }
 
 	public Double calcAverageValue() {
 
 		Double ret = 0D;
 		int cpt = 0;
 
 		List<ValueOccurrence> lst = ValueOccurrence.find.select("result").where().eq("value.id", id).isNotNull("result").eq("isCorrect", Boolean.TRUE).findList();
 		for (ValueOccurrence valueOccurrence : lst) {
 			cpt++;
 			ret += valueOccurrence.result;
 		}
 		if (cpt != 0) {
 			return ret / cpt;
 		} else {
 			return 0D;
 		}
 	}
 
 	public String averageValueStr() {
 		return ValueOccurrence.format(1D*averageValue, type);
 	}
 
 	public Status calcStatus(Long result) {
 
 		if (result == null) {
 			result = 0L;
 		}
 
 		switch (statusType) {
 		case FixePercent:
 
 			double fixValue = fixedTarget;
 
 			double max = Math.max(Math.abs(fixValue), Math.abs(result));
 			if (max == 0) {
 				return Status.OK;
 			}
 
 			double error = Math.abs(fixValue - result) / max;
 
 			return checkError(error, targetPercentError / 100.0D, targetPercentWarning / 100.0D);
 
 		case FixedSuperior:
 			return checkError(result, targetPercentError, targetPercentWarning);
 
 		case FixedInferior:
 			return checkError(-result, -targetPercentError, -targetPercentWarning);
 
 		case AverageErrorPercent:
 			double average = averageValue;
 
 			max = Math.max(Math.abs(average), Math.abs(result));
 			if (max == 0) {
 				return Status.OK;
 			}
 
 			error = Math.abs(average - result) / max;
 
 			return checkError(error, targetPercentError / 100.0D, targetPercentWarning / 100.0D);
 
 		case None:
 		default:
 			return Status.OK;
 		}
 	}
 
 	/**
 	 * Check error vs. targetPercentParams
 	 * 
 	 * @param error
 	 * @return
 	 */
 	private Status checkError(double error, double errorSup, double warningSup) {
 		if (error > errorSup) {
 			return Status.ERROR;
 		} else if (error > warningSup) {
 			return Status.WARN;
 		} else {
 			return Status.OK;
 		}
 	}
 
 	public void printAll(String tab) {
 		// System.out.println(tab+" v "+id+ " " +name);
 		// if (occurrences != null) {
 		// for (ValueOccurrence occurrence : occurrences) {
 		// System.out.println(occurrence.id+" "+occurrence.date+" "+occurrence.result);
 		// }
 		// }
 	}
 
 	public enum StatusType {
 		None, AverageErrorPercent, FixePercent, FixedSuperior, FixedInferior
 	};
 
 	// NOTE: play framework was bugging out without this method even though it's
 	// supposed to be automatic
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public void save() {
 		PerfLogger.log("Value.save", 1);
 //		Change change = new Change();
 //		save(change);
 //		change.save();
 //	}
 //
 //	public void save(Change change) {
 		averageValue = calcAverageValue();
 
 		PerfLogger.log("Value.save", 2);
 		boolean mustUpdateOccurence = false;
 
 		// Check if is is changed
 		boolean isNew = (id == null);
 		boolean isChanged = ((_ebean_intercept.getChangedProps() != null) && (_ebean_intercept.getChangedProps().size() != 0));
 		boolean isChangeAverage = (isChanged && _ebean_intercept.getChangedProps().contains("averageValue"));
 		boolean isChangeStatusType = (isChanged && _ebean_intercept.getChangedProps().contains("statusType"));
 		boolean isChangeTarget = (isChanged && (_ebean_intercept.getChangedProps().contains("targetPercentWarning") || _ebean_intercept.getChangedProps().contains("targetPercentError")));
 		boolean isChangeFixedTarget = (isChanged && _ebean_intercept.getChangedProps().contains("fixedTarget"));
 
 		// Occurence must be updated
 		mustUpdateOccurence = false;
 		if (isChangeStatusType) {
 			mustUpdateOccurence = true;
 		}
 		if (!mustUpdateOccurence && (statusType == StatusType.AverageErrorPercent) && (isChangeAverage)) {
 			mustUpdateOccurence = true;
 		}
 		if (!mustUpdateOccurence && (statusType == StatusType.FixedInferior) && (isChangeFixedTarget)) {
 			mustUpdateOccurence = true;
 		}
 		if (!mustUpdateOccurence && (statusType == StatusType.FixedSuperior) && (isChangeFixedTarget)) {
 			mustUpdateOccurence = true;
 		}
 		if (!mustUpdateOccurence && (statusType == StatusType.FixePercent) && (isChangeFixedTarget)) {
 			mustUpdateOccurence = true;
 		}
 		if (!mustUpdateOccurence && (statusType != StatusType.None) && (isChangeTarget)) {
 			mustUpdateOccurence = true;
 		}
 
 		PerfLogger.log("Value.save", 3);
 		super.save();
 		
 		PerfLogger.log("Value.save", 4);
 		// add to change if needed
 //		if (isNew) {
 //			change.tasksChildrenChanged.add(this.task);
 //		} else if (isChanged) {
 //			change.valuesChanged.add(this);
 //		}
 		
 		if (mustUpdateOccurence) {
 			for (ValueOccurrence vo : occurrences) {
 				ObjectToUpdate.saveNewObjectToUpdate(vo);
 			}
 		}
 		PerfLogger.log("Value.save", 5);
 
 	}
 
 }
