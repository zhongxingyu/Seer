 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 
 import play.db.ebean.Model;
 
 import com.google.gson.annotations.Expose;
 
 @Entity
 public class SvtTask extends Model {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1800221103277285807L;
 
 	@Id
 	@Expose
 	public Long id;
 
 	@Expose
 	private String name = "No name";
 
 	@Expose
 	public String parentPath = "";
 
 	@ManyToOne
 	public SvtTask parent;
 
 	@ManyToOne
 	public SvtUseCase useCase;
 
 	@Expose
 	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	@OrderBy("id")
 	public List<SvtTask> children = new ArrayList<SvtTask>();
 
 	@Expose
 	public int jobCount = 0;
 
 	@Expose
 	public int jobDoneCount = 0;
 
 	@Expose
 	public String runningJob = "";
 
 	@Expose
 	public Date startDate;
 
 	@Expose
 	public Date endDate;
 
 	@Expose
 	transient private Long duration = null;
 
 	@SuppressWarnings("unused")
 	@Expose
 	transient private String durationStr = "";
 
 	@SuppressWarnings("unused")
 	@Expose
 	transient private int totalJobCount = 0;
 
 	@SuppressWarnings("unused")
 	@Expose
 	transient private int totalJobDoneCount = 0;
 
 	@SuppressWarnings("unused")
 	@Expose
 	transient private String totalRunningJob = "";
 
 	/** State of this task **/
 	@Expose
 	public State state = null;
 
 	/** Status of this task **/
 	@Expose
 	public Status status = Status.NOT_CONCERNED;
 
 	@Expose
 	public Boolean isPause = null;
 
 	@Expose
 	public Boolean isTechPause = null;
 
 	@Expose
 	public Integer cptPause = null;
 
 	@Expose
 	public Boolean waiting = null;
 
 	/**
 	 * Creator
 	 * 
 	 * @param line
 	 * @param parent
 	 * @param cptPause
 	 *          if not null, it's a pause (not a real task)
 	 */
 	public SvtTask(String line, SvtTask parent) {
 		if (parent != null) {
 			parentPath = parent.getPath();
 		}
 		name = line.replaceAll("\t", " ").trim();
 	}
 
 	/**
 	 * Creator (to create a pause)
 	 * 
 	 * @param line
 	 * @param parent
 	 * @param cptPause
 	 * @param isTechPause
 	 */
 	public SvtTask(String line, SvtTask parent, Integer cptPause, Boolean isTechPause) {
 		this(line, parent);
 
 		this.cptPause = cptPause;
 		this.isPause = !isTechPause;
 		this.isTechPause = isTechPause;
 		this.waiting = false;
 	}
 
 	public void incJobs() {
 		jobCount++;
 	}
 
 	public String getPath() {
 		return (parentPath == null ? "" : parentPath) + "/" + name.replaceFirst("^/", "");
 	}
 
 	public void addChild(SvtTask svtTask, int cpt, String line) throws SvtUseCaseFormatException {
 		for (SvtTask task : children) {
 			if (task.name.equals(svtTask.name)) {
 				throw new SvtUseCaseFormatException("Two tasks with the same name " + task.name + " (child of " + name + ")", cpt, line);
 			}
 		}
 		children.add(svtTask);
 	}
 
 	/**
 	 * Get the map path->task for this task and it's children
 	 * 
 	 * @param rootPath
 	 *          the root path to add to this task
 	 * @return
 	 */
 	public Map<String, SvtTask> getMaptask(String rootPath) {
 		Map<String, SvtTask> mapTasks = new HashMap<String, SvtTask>();
 		String path = rootPath + name;
 		if (rootPath.length() != 0) {
 			path = rootPath + "/" + name;
 		}
 
 		for (SvtTask svtTask : children) {
 			mapTasks.putAll(svtTask.getMaptask(path));
 		}
 
 		mapTasks.put(path, this);
 
 		return mapTasks;
 	}
 
 	@Override
 	public void save() {
 		//Logger.debug("save task : " + id + " " + _ebean_intercept.getChangedProps());
		
		//Limit running job to 255 char
		if ((runningJob != null) && (runningJob.length() > 254)) {
			runningJob = runningJob.substring(0, 253);
		}
 
 		super.save();
 	}
 
 	public void initValues() {
 		if ((this.startDate != null) && (this.endDate != null)) {
 			this.duration = (this.endDate.getTime() - this.startDate.getTime()) / (1000 * 60);
 		} else if ((this.startDate != null) && (this.state == State.RUNNING)) {
 			this.duration = (System.currentTimeMillis() - this.startDate.getTime()) / (1000 * 60);
 		}
 
 		if (this.duration != null) {
 			long durMin = this.duration;
 			this.durationStr = (durMin > (60 * 24) ? (long) Math.floor(durMin / (60 * 24)) + "d " : "");
 			durMin = durMin - (60 * 24) * (long) Math.floor(durMin / (60 * 24));
 			this.durationStr += (durMin > 60 ? (long) Math.floor(durMin / 60) + "h " : "");
 			durMin = durMin - (60 * (long) Math.floor(durMin / 60));
 			this.durationStr += durMin + "m";
 		}
 		
 		this.totalJobCount = this.jobCount;
 		this.totalJobDoneCount = this.jobDoneCount;
 		this.totalRunningJob = this.runningJob;
 
 		for (SvtTask svtTask : children) {
 			svtTask.initValues();
 			this.totalJobCount += svtTask.totalJobCount;
 			this.totalJobDoneCount += svtTask.totalJobDoneCount;
 			if ((svtTask.totalRunningJob!= null) && (svtTask.totalRunningJob.length() != 0)) {
 				this.totalRunningJob = svtTask.totalRunningJob;
 			}
 			if ((this.waiting != null) || (svtTask.waiting != null)) {
 				this.waiting = (this.waiting == null ? false : this.waiting) || (svtTask.waiting == null ? false : svtTask.waiting);
 			}
 		}
 
		//Limit running job to 255 char
		if ((runningJob != null) && (runningJob.length() > 254)) {
			runningJob = runningJob.substring(0, 253);
		}

 	}
 }
