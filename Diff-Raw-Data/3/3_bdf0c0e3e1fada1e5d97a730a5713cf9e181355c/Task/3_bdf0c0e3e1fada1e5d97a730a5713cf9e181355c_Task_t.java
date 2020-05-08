 package fi.helsinki.cs.kohahdus.trainer;
 
 import java.sql.Timestamp;
 import java.util.*;
 
 import javax.security.auth.callback.LanguageCallback;
 
 import fi.helsinki.cs.kohahdus.criteria.Criterion;
 
 //import fi.helsinki.cs.kohahdus.Criterion;
 /**
 *
 * @author  jari
 */
 //Pitisi olla kaikki mit suunnitteludokumentissa pyydetn.
 //Muutoksien jlkeen pitisi kutsua metodeja setAuthor(author)
 //ja setModificationDate().
 
 // Laajennetaan olemassa olevaa Task luokkaa
 public class Task {
 	public static final String TYPE_FULL = "programming";
 	public static final String TYPE_FILL = "fill-in";
 	
 	private String taskID;
 	private String language;
 	private String taskName;
 	private String author;
 	private String description;
 	private String pinput;
 	private String sinput;
 	private String modelAnswer;
 	private String category;
 	private String taskType;
 	private String fillInPreCode;
 	private String fillInPostCode;
 	private boolean useModel;
 	
 	private String passFeedback;
 	private String failFeedback;
 	
 	private boolean hasSucceeded;		// If the student has completed the task successfully
 	private int noOfTries;			// How many times student has tried to complete the task
 	
 	private Date modificationdate;
     
 	
 	
 	
 	
 
 	
 	
 //CONSTRUCTORS
 	/** Creates a new instance of Task */
 	public Task(){
 		this.modificationdate=new Date();
 		this.hasSucceeded=false;
 		this.noOfTries=0;
 	}
 	
 	/** Creates a new instance of Task*/
 	public Task(String taskID){
 		this.taskID=taskID;
 		this.modificationdate=new Date();
 		this.hasSucceeded=false;
 		this.noOfTries=0;
 	}
 	
 	/** Creates a new instance of Task */
 	public Task(String language, String taskname, String auth, String desc,
 			String pinput, String sinput,
 			String modelanswer, String categ, String type,
 			String fillInPre, String fillInPost, boolean useModel,
 			boolean succeed, int tries, String passFeedBack, String failFeedBack) {
 		if (language == null || !(language.equalsIgnoreCase("EN") || language.equalsIgnoreCase("FI"))) {
 			throw new IllegalArgumentException("Language param "+language);
 		}
 		this.language=language.toUpperCase();
 		this.taskName=taskname;
 		this.author=auth;
 		this.description=desc;
 		this.pinput=pinput;
 		this.sinput=sinput;
 		this.modelAnswer=modelanswer;
 		this.category=categ;
 		if (!(type.equalsIgnoreCase(TYPE_FULL) || type.equalsIgnoreCase(TYPE_FILL))) {
 			throw new IllegalArgumentException("Given category "+type+" not valid");
 		}
 		this.taskType=type;
 		this.fillInPreCode=fillInPre;
 		this.fillInPostCode=fillInPost;
 		this.useModel=useModel;
 		this.modificationdate=new Date();
 		this.hasSucceeded=succeed;
 		this.noOfTries=tries;
 		this.passFeedback=passFeedBack;
 		this.failFeedback=failFeedBack;
 	}
 	
 	
 //SET-METHODS
 	/** Set ID for this task */
 	public void setTaskID(String taskID) {
 		this.taskID = taskID;
 	}
 	
 	/** Set the preferred language of this user. The language is either "EN" or "FI
 	 * @param lang either "FI" or "EN"
 	 */
 	public void setLanguage(String lang) {
 		if (lang == null || !(lang.equalsIgnoreCase("EN") || lang.equalsIgnoreCase("FI"))) {
 			throw new IllegalArgumentException("Language param "+lang);
 		}
 		this.language = lang.toUpperCase();
 	}
 	
 	/** Set the name of this task */
 	public void setName(String name) {
 		this.taskName=name;
 	}
 	
 	/** Set "last task modification by" attribute to Name,
 	 *  set last-modification-timestamp to current data and time */
 	public void setAuthor(String name) {
 		this.author=name;
 	}
 	
 	/** Set description (tehtvnanto) of this task */
 	public void setDescription(String desc) {
 		this.description=desc;
 	}
 	
 	/** Set the public input as String of this task */
 	public void setPublicInput(String input) {
 		this.pinput=input;
 	}
 	
 	/** Set the secret input as String of this task */
 	public void setSecretInput(String input) {
 		this.sinput=input;
 	}
 	
 	/** Set model answer code */
 	public void setModelAnswer(String code) {
 		this.modelAnswer=code;
 	}
 	
 	/** Set task category of this task */
 	public void setCategory(String categ) {
 		this.category=categ;
 	}
 	
 	/** Set task type of this task */
 	public void setTitoTaskType(String type) {
 		if (type == null || !(type.equalsIgnoreCase(TYPE_FULL) || type.equalsIgnoreCase(TYPE_FILL))) {
 			throw new IllegalArgumentException("Given task type "+type+" not valid");
 		}
 		this.taskType=type.toLowerCase();
 	}
 	
 	/** Set the code that is prepended before student's code in a fill-in task */
 	public void setFillInPreCode(String code) {
 		this.fillInPreCode=code;
 	}
 	
 	/** Set the code that is appended to student's code in a fill-in task */
 	public void setFillInPostCode(String code) {
 		this.fillInPostCode=code;
 	}
 	
 	/** Set this task as fill-in or create-full-program  */
 	public void setFillInTask(boolean fillIn) {
 		if (fillIn)
 			this.taskType=TYPE_FILL;
 	}
 	
 	/** Sets number of tries for student */
 	public void setNoOfTries(int noOfTries) {
 		if (noOfTries<0) {
 			throw new IllegalArgumentException("Given number of tries "+noOfTries+" not valid");
 		}
 		this.noOfTries = noOfTries;
 	}
 	
 	/** Set the validation method of this task */
 	public void setValidateByModel(boolean useModel){
 		this.useModel=useModel;
 	}
 	
 	/** Set the date and time this task was last modified */
 	public void setModificationDate() {
 		this.modificationdate=new Date();  //sets the current date and time
 	}
 	
 	/** Set the hasSucceeded true or false depending the student has passed the task */
 	public void setHasSucceeded(boolean hasSucceeded) {
 		this.hasSucceeded = hasSucceeded;
 	}    
     
 	/** Return feedback if student passes the task */
 	public void setPassFeedBack(String pass) {
 		this.passFeedback=pass;
 	}
 	
 	/** Return feedback if student fails the task */
 	public void setFailFeedBack(String fail) {
 		this.failFeedback=fail;
 	}
 	
 	
 //GET-METHODS
 	/** Return id of this task */
 	public String getTaskID() {
 		return taskID;
 	}
 	
 	/** Return the language of this task */
 	public String getLanguage() {
 		return language;
 	}
 	
 	/** Return the name of this task */
 	public String getName() {
 		return taskName;
 	}
 	
 	/** Return name of the last person who has modified this task */
 	public String getAuthor() {
 		return author;
 	}
 	
 	/** Return the description (tehtvnanto) of this task */
 	public String getDescription() {
 		return description;
 	}
 	
 	/** Return the public input as String of this task */
 	public String getPublicInput() {
 		return pinput;
 	}
 	
 	/** Return the secret input as String of this task */
 	public String getSecretInput() {
 		return sinput;
 	}
 	
 	/** Return code of the model answer provided by teacher */
 	public String getModelAnswer() {
 		return modelAnswer;
 	}
 	
 	/** Return the task category of this task */
 	public String getCategory() {
 		return category;
 	}
 	
 	/** Return the task type of this task */
 	public String getTitoTaskType() {
 		return taskType;
 	}
 	//sama metodi periaatteessa, mit halutaan?
 	//markus huom: nyt lytyy metodit getTaskType() ja getTasktype(), joista jlkimminen
 	//periytyy eAssarista (lytyy tuolta alempaa). EAssarin metodi palauttaa tyypin, jolla
 	//yhdistetn Displayer ja Analyzer komponentit Taskiin. Ehk olisi parasta nimet oma
 	//metodi uudelleen s.e. sit ei voida sekoittaa eAssarin vastaavaan. 
 	//Vaihdoin oman metodin nimeksi get/setTitoTaskType.
 	//Allaoleva metodi on turha ja voidaan poistaa nyt kun tyyppi lytyy y.o. metodilla.
 	/** Return tasktype as a String (fill-in or programming) */
 	/* deleted permanently ?
 	public String getTaskTypeString() {
 		if (taskType==TYPE_FULL) return "programming";
 		if (taskType==TYPE_FILL) return "fill-in";
 		return "not specified";
 	}
 	*/
 	
 	/** Return the code that is prepended before student's code in a fill-in task */
 	public String getFillInPreCode() {
 		return fillInPreCode;
 	}
 	
 	/** Return the code that is appended to student's code in a fill-in task */
 	public String getFillInPostCode() {
 		return fillInPostCode;
 	}
 	
 	/** Return the number of tries student has tried to make this task */
 	public int getNoOfTries() {
 		return noOfTries;
 	}
 	
 	/** Return true if this is a fill-in task */
 	public boolean isFillInTask() {
 		return taskType==TYPE_FILL;
 	}
 	
 	/** Return true if this is a programming task */
 	public boolean isProgrammingTask() {
 		return taskType==TYPE_FULL;
 	}
 
 	/** Return true if this task is to be validated by comparing results of the student's answer
 	 * to results of teacher's answer */
 	public boolean isValidateByModel() {
 		return useModel;
 	}
 	
 
 	
 	/** Return the date and time this task was last modified */
 	public Date getModificationDate() {
 		return modificationdate;
 	}
 	
 	/** Return true if student has made the task, false if she/he hasn't  */
 	public boolean isHasSucceeded() {
 		return hasSucceeded;
 	}
 	
 	/** Return feedback if student passes the task */
 	public String getPassFeedBack() {
 		return passFeedback;
 	}
 	
 	/** Return feedback if student fails the task */
 	public String getFailFeedBack() {
 		return failFeedback;
 	}
 	
 	
 //OTHER METHODS
 	/** Returns String of the date and time */
 	public String getDateAsString() {
 		return modificationdate.toString();
 	}
 	
 	/** Deserialize String value from XML string. Helper function for initSubClass() */
 	protected static String parseXMLString(String XML, String tagname) {
 		if (XML == null) return "";
 		int begin = XML.indexOf("<" + tagname + ">") + tagname.length() + 2;
 		int end   = XML.indexOf("</" + tagname + ">");
		
		if (begin < 0 || end < 0) return "";
		
 		String value = XML.substring(begin, end);
 		value = value.replaceAll("&gt;", ">");
 		value = value.replaceAll("&lt;", "<");
 		value = value.replaceAll("&amp;", "&");
 		return value;
 	}
 	/** Deserialize boolean value from XML string. Helper function for initSubClass() */
 	protected static boolean parseXMLBoolean(String XML, String tagname) {
 		String value = parseXMLString(XML, tagname);
 		return value.indexOf(0) == 'T';
 	}
 
 	/** Serialize String value to XML string. Helper function for serializeSubClass() */
 	protected static String toXML(String tagname, String value) {
 		value = value.replaceAll("&", "&amp;");
 		value = value.replaceAll(">", "&gt;");
 		value = value.replaceAll("<", "&lt;");
 		return "<" + tagname + ">" + value + "</" + tagname + ">";
 	}
 	
 	/** Serialize boolean value to XML string. Helper function for serializeSubClass() */
 	protected static String toXML(String tagname, boolean value) {
 		return "<" + tagname + ">" + (value ? 'T' : 'F') + "</" + tagname + ">";
 	}
 	
 	/** Return a serialized this Task class into XML-format */
 	public String serializeToXML() {
 		return toXML("language", language) +
 			   toXML("description", description) +
 			   toXML("pinput", pinput) +
 			   toXML("modelAnswer", modelAnswer) +
 			   toXML("category", category) +
 			   toXML("taskType", taskType) +
 			   toXML("fillInPreCode", fillInPreCode) +
 			   toXML("fillInPostCode", fillInPostCode) +
 			   toXML("useModel", useModel) +
 			   toXML("passFeedback", passFeedback) +
 			   toXML("failFeedback", failFeedback);
 	}
 
 	
 	/** Instantiate the Task class's parameters using the serialized form XML.
 	 * @throws RuntimeException exceptions in the deserialization are
 	 * caught and rethrown as uncheckced exception. */
 	public void deserializeFromXML(String xml)  {
 		if (xml == null) return;
 		try {
 			language = parseXMLString(xml, "language");
 			description = parseXMLString(xml, "description");
 			pinput = parseXMLString(xml, "pinput");
 			sinput = parseXMLString(xml, "sinput");
 			modelAnswer = parseXMLString(xml, "modelAnswer");
 			category = parseXMLString(xml, "category");
 			taskType = parseXMLString(xml, "taskType");
 			fillInPreCode = parseXMLString(xml, "fillInPreCode");
 			fillInPostCode = parseXMLString(xml, "fillInPostCode");
 			useModel = parseXMLBoolean(xml, "useModel");
 			passFeedback = parseXMLString(xml, "passFeedback");
 			failFeedback = parseXMLString(xml, "failFeedback");
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 
 
 
 	
 	
 	
 //	 **** NM AINAKIN OVAT VANHASTA EASSARISTA *****	
 	
     private String courseID;
     private String moduleID;
     private String tasktypeID;
     private Tasktype tasktype;
     private int seqNo;
     private Timestamp deadLine;
     private boolean shouldStore;
     private boolean shouldRegister;
     private boolean shouldKnow;
     private boolean shouldEvaluate;
     private int cutoffvalue;
     private String style;	
 	
 	
 	public Task(String taskid, String courseid, String moduleid, String tasktypeid, int seqno, Timestamp deadline,
 				   boolean shouldstore, boolean shouldregister, boolean shouldknowstudent, boolean shouldevaluate, int cvalue,
 				   int nooftries, Tasktype tType) {
 		
 		taskID = taskid;
 		courseID = courseid;
 		moduleID = moduleid;
 		tasktypeID = tasktypeid;
 		seqNo = seqno;
 		deadLine = deadline;
 		shouldStore = shouldstore;
 		shouldRegister = shouldregister;
 		shouldKnow = shouldknowstudent;
 		shouldEvaluate = shouldevaluate;
 		noOfTries = nooftries;
 		cutoffvalue = cvalue;
 		tasktype = tType;
 	}  	
 	
     public boolean shouldBeAnalysed() {
         return shouldEvaluate;
     }    
     public boolean shouldRegisterTry() {
     	return shouldRegister;
     }
     public boolean shouldStoreAnswer() {
         return shouldStore;
     }
     public Tasktype getTasktype() {
         return tasktype;
     }    
     public AnalyserInterface getAnalyser(String language) {
         AnalyserInterface an = tasktype.getAnalyser(language,taskID);
         return an;
     }    
     public boolean wasSuccess(int points) {
         return points>=cutoffvalue;
     }
     public boolean isInTime(Timestamp when) {
         return deadLine.after(when);
     }
     public boolean shouldAllowRetry(int triesSoFar) {
         return noOfTries>triesSoFar;
      }
     public DisplayerInterface getDisplayer(String language) {
         DisplayerInterface disp = tasktype.getDisplayer(language,taskID);
         return disp;
     }    
     public int getCutoffvalue() {
         return cutoffvalue;
     }    
     public String getStyle() {
         return tasktype.getStyle();
     }
 
 	public String getCourseID() {
 		return courseID;
 	}
 
 	public void setCourseID(String courseID) {
 		this.courseID = courseID;
 	}
 
 	public boolean isShouldEvaluate() {
 		return shouldEvaluate;
 	}
 
 	public void setShouldEvaluate(boolean shouldEvaluate) {
 		this.shouldEvaluate = shouldEvaluate;
 	}
 
 	public boolean isShouldKnow() {
 		return shouldKnow;
 	}
 
 	public void setShouldKnow(boolean shouldKnow) {
 		this.shouldKnow = shouldKnow;
 	}
 
 	public boolean isShouldRegister() {
 		return shouldRegister;
 	}
 
 	public void setShouldRegister(boolean shouldRegister) {
 		this.shouldRegister = shouldRegister;
 	}
 
 	public boolean isShouldStore() {
 		return shouldStore;
 	}
 
 	public void setShouldStore(boolean shouldStore) {
 		this.shouldStore = shouldStore;
 	}
 
 	
 
 	public void setCutoffvalue(int cutoffvalue) {
 		this.cutoffvalue = cutoffvalue;
 	}
 
 	public void setTasktype(Tasktype tasktype) {
 		this.tasktype = tasktype;
 	}
 
 
 	
 
 }
