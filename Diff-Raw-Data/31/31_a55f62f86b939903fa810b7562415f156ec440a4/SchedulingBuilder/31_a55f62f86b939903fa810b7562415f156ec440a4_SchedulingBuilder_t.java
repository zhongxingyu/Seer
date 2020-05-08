 package org.icefaces.samples.showcase.example.ace.dataTable;
 
 import entities.Backend;
 import entities.Composite;
 import entities.Mode;
 import entities.Scheduling;
 
 /*
  * Builder class for creating a new scheduling.
  */
 
 public class SchedulingBuilder {
 
 	private String name;
 	private boolean javaAgentPollable = false;
 	private boolean bankHolidayOnly = false;
 	private Mode mode;
 	private Composite composite;
 	private Backend source;
 	private Backend target;
 	private String requestURL;
 	private String cron = "*";
 	private String description = "";
 	private int id;
 	
 	private static final String LINE_BREAK = "\n";
 
 	public SchedulingBuilder() {
 	}
 
 	public SchedulingBuilder(Scheduling s) {
 		this.name = s.getName();
 		if (s.getJavaAgentPollable() == 1)
 			this.javaAgentPollable = true;
 
 		if (s.getBankHolidayOnly() == 1)
 			this.bankHolidayOnly = true;
 		this.mode = SessionBean.MODES.get(s.getStatusID());
 		this.composite = SessionBean.COMPOSITES.get(s.getServiceID());
 		this.source = SessionBean.BACKENDS.get(s.getSource());
 		this.target = SessionBean.BACKENDS.get(s.getTarget());
 		this.requestURL = s.getRequestURL();
 		this.cron = s.getCron();
 		this.description = s.getDescription();
 		this.id = s.getId();
 	}
 
 	public boolean validate() throws IllegalOperationException {
 		String message = "";
 		boolean error = false;
 		if (this.name == null || this.name.isEmpty()) {
 			error = true;
 			message += "Name cannot be empty!" + LINE_BREAK;
 		}
 		if (this.mode == null) {
 			error = true;
 			message += "Mode was not selected!" + LINE_BREAK;
 		}
 		if (this.composite == null) {
 			error = true;
 			message += "Composite was not selected!" + LINE_BREAK;
 		}
 		if (this.source == null) {
 			error = true;
 			message += "Source was not selected!" + LINE_BREAK;
 		}
 		if (this.target == null) {
 			error = true;
 			message += "Target was not selected!" + LINE_BREAK;
 		}
 		if (this.cron == null || this.cron.isEmpty()) {
 			error = true;
 			message += "CRON cannot be empty!" + LINE_BREAK;
 		}
 		if (this.description == null || this.description.isEmpty()) {
 			error = true;
 			message += "Description cannot be empty!" + LINE_BREAK;
 		}
 		if (this.source.getBackend() == this.target.getBackend()) {
 			error = true;
 			message += "Source cannot be the same as target!" + LINE_BREAK;
 		}
 		if (error)
 			throw new IllegalOperationException(message);
 
 		return true;
 	}
 
 	public Scheduling build() throws IllegalOperationException {
 		this.validate();
 
 		Scheduling s = new Scheduling();
 		s.setName(name);
 		s.setId(id);
 
 		if (this.bankHolidayOnly)
 			s.setBankHolidayOnly(1);
 		else
 			s.setBankHolidayOnly(0);
 		if (this.javaAgentPollable)
 			s.setJavaAgentPollable(1);
 		else
 			s.setJavaAgentPollable(0);
 
 		s.setCron(cron);
 		s.setDescription(description);
 		s.setRequestURL(requestURL);
 		s.setServiceID(this.composite.getId());
 		s.setStatusID(this.mode.getId());
 		s.setSource(this.source.getId());
 		s.setTarget(this.target.getId());
		s.getModeLabel();
		s.getSource();
		s.getTarget();
 		return s;
 	}
 
 	public void sync(Scheduling s) throws IllegalOperationException {
 		this.validate();
 		s.setName(name);
 		s.setId(id);
 
 		if (this.bankHolidayOnly)
 			s.setBankHolidayOnly(1);
 		else
 			s.setBankHolidayOnly(0);
 		if (this.javaAgentPollable)
 			s.setJavaAgentPollable(1);
 		else
 			s.setJavaAgentPollable(0);
 
 		s.setCron(cron);
 		s.setDescription(description);
 		s.setRequestURL(requestURL);
 		s.setServiceID(this.composite.getId());
 		s.setStatusID(this.mode.getId());
 		s.setSource(this.source.getId());
 		s.setTarget(this.target.getId());
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getRequestURL() {
 		return requestURL;
 	}
 
 	public String getCron() {
 		return cron;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public void setCron(String cron) {
 		this.cron = cron;
 	}
 
 	public void setRequestURL(String requestURL) {
 		this.requestURL = requestURL;
 	}
 
 	public boolean isJavaAgentPollable() {
 		return javaAgentPollable;
 	}
 
 	public void setJavaAgentPollable(boolean javaAgentPollable) {
 		this.javaAgentPollable = javaAgentPollable;
 	}
 
 	public boolean isBankHolidayOnly() {
 		return bankHolidayOnly;
 	}
 
 	public void setBankHolidayOnly(boolean bankHolidayOnly) {
 		this.bankHolidayOnly = bankHolidayOnly;
 	}
 
 	public Mode getMode() {
 		return mode;
 	}
 
 	public void setMode(Mode mode) {
 		this.mode = mode;
 	}
 
 	public Composite getComposite() {
 		return composite;
 	}
 
 	public void setComposite(Composite composite) {
 		this.composite = composite;
 	}
 
 	public Backend getSource() {
 		return source;
 	}
 
 	public void setSource(Backend source) {
 		this.source = source;
 	}
 
 	public Backend getTarget() {
 		return target;
 	}
 
 	public void setTarget(Backend target) {
 		this.target = target;
 	}
 
 }
