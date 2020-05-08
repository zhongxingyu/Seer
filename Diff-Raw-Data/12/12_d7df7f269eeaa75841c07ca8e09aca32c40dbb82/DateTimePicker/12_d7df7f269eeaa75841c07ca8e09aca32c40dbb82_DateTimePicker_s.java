 package de.jwic.controls;
 
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONWriter;
 
 import de.jwic.base.IControlContainer;
 import de.jwic.base.IncludeJsOption;
 import de.jwic.base.JavaScriptSupport;
 import de.jwic.base.SessionContext;
 
 /**
  * Date Time Picker Control
  * @author dotto
  *
  */
 @JavaScriptSupport(jsTemplate = "de.jwic.controls.DateTimePicker")
 public class DateTimePicker extends DatePicker {
 
 	private static final Logger log = Logger.getLogger(DateTimePicker.class);
 	private boolean showHour = true;
 	private boolean showMinute = true;
 	private boolean showSecond = true;
 	private boolean showMillisec = false;
 	private boolean showTimezone = false;
 	private Integer stepHour = null;
 	private Integer stepMinute = null;
 	private Integer stepSecond = null;
 	private Integer stepMilliSec = null;
 	private Integer hour = null;
 	private Integer minute = null;
 	private Integer second = null;
 	private Integer millisec = null;
 	private Integer hourMin = null;
 	private Integer minuteMin = null;
 	private Integer secondMin = null;
 	private Integer millisecMin = null;
 	private Integer hourMax = null;
 	private Integer minuteMax = null;
 	private Integer secondMax = null;
 	private Integer millisecMax = null;
 	private Boolean showButtonPanel = null;
 	private Boolean timeOnly = null;
 	private Boolean alwaysSetTime = null;
 	private String separator = null;
 	private DateTimePicker slave;
	private String timeFormat;
 
 	
 	private ControlTpye controlType = ControlTpye.SELECT;
 	
 	/**
 	 * @param container
 	 */
 	public DateTimePicker(IControlContainer container) {
 		super(container);
 	}
 	
 	
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public DateTimePicker(IControlContainer container, String name) {
 		super(container, name);
 	}
 	
 	@Override
 	protected void init() {
 		setTemplateName(DatePicker.class.getName());
 		SessionContext sc = getSessionContext();
 		if(sc.getTimeFormat() != null && sc.getTimeFormat().length() > 0)
 			this.setTimeFormat(sc.getTimeFormat());
 		super.init();
 	}
 
 
 	/**
 	 * @return the showHour
 	 */
 	@IncludeJsOption
 	public boolean isShowHour() {
 		return showHour;
 	}
 
 
 	/**
 	 * @param showHour the showHour to set
 	 */
 	public void setShowHour(boolean showHour) {
 		this.showHour = showHour;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the showMinute
 	 */
 	@IncludeJsOption
 	public boolean isShowMinute() {
 		return showMinute;
 	}
 
 
 	/**
 	 * @param showMinute the showMinute to set
 	 */
 	public void setShowMinute(boolean showMinute) {
 		this.showMinute = showMinute;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the showSecond
 	 */
 	@IncludeJsOption
 	public boolean isShowSecond() {
 		return showSecond;
 	}
 
 
 	/**
 	 * @param showSecond the showSecond to set
 	 */
 	public void setShowSecond(boolean showSecond) {
 		this.showSecond = showSecond;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the showMillisec
 	 */
 	@IncludeJsOption
 	public boolean isShowMillisec() {
 		return showMillisec;
 	}
 
 
 	/**
 	 * @param showMillisec the showMillisec to set
 	 */
 	public void setShowMillisec(boolean showMillisec) {
 		this.showMillisec = showMillisec;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the showTimezone
 	 */
 	@IncludeJsOption
 	public boolean isShowTimezone() {
 		return showTimezone;
 	}
 
 
 	/**
 	 * @param showTimezone the showTimezone to set
 	 */
 	public void setShowTimezone(boolean showTimezone) {
 		this.showTimezone = showTimezone;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the stepHour
 	 */
 	@IncludeJsOption
 	public Integer getStepHour() {
 		return stepHour;
 	}
 
 
 	/**
 	 * @param stepHour the stepHour to set
 	 */
 	public void setStepHour(Integer stepHour) {
 		this.stepHour = stepHour;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the stepMinute
 	 */
 	@IncludeJsOption
 	public Integer getStepMinute() {
 		return stepMinute;
 	}
 
 
 	/**
 	 * @param stepMinute the stepMinute to set
 	 */
 	public void setStepMinute(Integer stepMinute) {
 		this.stepMinute = stepMinute;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the stepSecond
 	 */
 	@IncludeJsOption
 	public Integer getStepSecond() {
 		return stepSecond;
 	}
 
 
 	/**
 	 * @param stepSecond the stepSecond to set
 	 */
 	public void setStepSecond(Integer stepSecond) {
 		this.stepSecond = stepSecond;
 		requireRedraw();
 	}
 	
 	/**
 	 * @return the controlType
 	 */
 	@IncludeJsOption
 	public ControlTpye getControlType() {
 		return controlType;
 	}
 
 
 	/**
 	 * @param controlType the controlType to set
 	 */
 	public void setControlType(ControlTpye controlType) {
 		this.controlType = controlType;
 		requireRedraw();
 	}
 	
 	/**
 	 * @return the stepMilliSec
 	 */
 	@IncludeJsOption
 	public Integer getStepMilliSec() {
 		return stepMilliSec;
 	}
 
 
 	/**
 	 * @param stepMilliSec the stepMilliSec to set
 	 */
 	public void setStepMilliSec(Integer stepMilliSec) {
 		this.stepMilliSec = stepMilliSec;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the hour
 	 */
 	@IncludeJsOption
 	public Integer getHour() {
 		return hour;
 	}
 
 
 	/**
 	 * @param hour the hour to set
 	 */
 	public void setHour(Integer hour) {
 		this.hour = hour;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the minute
 	 */
 	@IncludeJsOption
 	public Integer getMinute() {
 		return minute;
 	}
 
 
 	/**
 	 * @param minute the minute to set
 	 */
 	public void setMinute(Integer minute) {
 		this.minute = minute;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the second
 	 */
 	@IncludeJsOption
 	public Integer getSecond() {
 		return second;
 	}
 
 
 	/**
 	 * @param second the second to set
 	 */
 	public void setSecond(Integer second) {
 		this.second = second;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the millisec
 	 */
 	@IncludeJsOption
 	public Integer getMillisec() {
 		return millisec;
 	}
 
 
 	/**
 	 * @param millisec the millisec to set
 	 */
 	public void setMillisec(Integer millisec) {
 		this.millisec = millisec;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the hourMin
 	 */
 	@IncludeJsOption
 	public Integer getHourMin() {
 		return hourMin;
 	}
 
 
 	/**
 	 * @param hourMin the hourMin to set
 	 */
 	public void setHourMin(Integer hourMin) {
 		this.hourMin = hourMin;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the minuteMin
 	 */
 	@IncludeJsOption
 	public Integer getMinuteMin() {
 		return minuteMin;
 	}
 
 
 	/**
 	 * @param minuteMin the minuteMin to set
 	 */
 	public void setMinuteMin(Integer minuteMin) {
 		this.minuteMin = minuteMin;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the secondMin
 	 */
 	@IncludeJsOption
 	public Integer getSecondMin() {
 		return secondMin;
 	}
 
 
 	/**
 	 * @param secondMin the secondMin to set
 	 */
 	public void setSecondMin(Integer secondMin) {
 		this.secondMin = secondMin;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the millisecMin
 	 */
 	@IncludeJsOption
 	public Integer getMillisecMin() {
 		return millisecMin;
 	}
 
 
 	/**
 	 * @param millisecMin the millisecMin to set
 	 */
 	public void setMillisecMin(Integer millisecMin) {
 		this.millisecMin = millisecMin;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the hourMax
 	 */
 	@IncludeJsOption
 	public Integer getHourMax() {
 		return hourMax;
 	}
 
 
 	/**
 	 * @param hourMax the hourMax to set
 	 */
 	public void setHourMax(Integer hourMax) {
 		this.hourMax = hourMax;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the minuteMax
 	 */
 	@IncludeJsOption
 	public Integer getMinuteMax() {
 		return minuteMax;
 	}
 
 
 	/**
 	 * @param minuteMax the minuteMax to set
 	 */
 	public void setMinuteMax(Integer minuteMax) {
 		this.minuteMax = minuteMax;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the secondMax
 	 */
 	@IncludeJsOption
 	public Integer getSecondMax() {
 		return secondMax;
 	}
 
 
 	/**
 	 * @param secondMax the secondMax to set
 	 */
 	public void setSecondMax(Integer secondMax) {
 		this.secondMax = secondMax;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the millisecMax
 	 */
 	@IncludeJsOption
 	public Integer getMillisecMax() {
 		return millisecMax;
 	}
 
 
 	/**
 	 * @param millisecMax the millisecMax to set
 	 */
 	public void setMillisecMax(Integer millisecMax) {
 		this.millisecMax = millisecMax;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the showButtonPanel
 	 */
 	@IncludeJsOption
 	public Boolean getShowButtonPanel() {
 		return showButtonPanel;
 	}
 
 
 	/**
 	 * @param showButtonPanel the showButtonPanel to set
 	 */
 	public void setShowButtonPanel(Boolean showButtonPanel) {
 		this.showButtonPanel = showButtonPanel;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the timeOnly
 	 */
 	@IncludeJsOption
 	public Boolean getTimeOnly() {
 		return timeOnly;
 	}
 
 
 	/**
 	 * @param timeOnly the timeOnly to set
 	 */
 	public void setTimeOnly(Boolean timeOnly) {
 		this.timeOnly = timeOnly;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the alwaysSetTime
 	 */
 	@IncludeJsOption
 	public Boolean getAlwaysSetTime() {
 		return alwaysSetTime;
 	}
 
 
 	/**
 	 * @param alwaysSetTime the alwaysSetTime to set
 	 */
 	public void setAlwaysSetTime(Boolean alwaysSetTime) {
 		this.alwaysSetTime = alwaysSetTime;
 		requireRedraw();
 	}
 
 
 	/**
 	 * @return the separator
 	 */
 	@IncludeJsOption
 	public String getSeparator() {
 		return separator;
 	}
 
 
 	/**
 	 * @param separator the separator to set
 	 */
 	public void setSeparator(String separator) {
 		this.separator = separator;
 		requireRedraw();
 	}
 	
 	/**
 	 * @return
 	 */
 	@IncludeJsOption
 	public String getDefaultTimezone(){
 		if(getTimeZone() != null){
 			Date today = Calendar.getInstance().getTime();
 			SimpleDateFormat sdf = new SimpleDateFormat("Z");
 			sdf.setTimeZone(getTimeZone());
 			String gmt = sdf.format(today);
 			return gmt;
 		}
 		
 		return "+0000";
 	}
 	
 	/**
 	 * @return the timeFormat
 	 */
 	public String getTimeFormat() {
 		return timeFormat;
 	}
 
 
 	/**
 	 * @param timeFormat the timeFormat to set
 	 */
 	public void setTimeFormat(String timeFormat) {
 		this.timeFormat = timeFormat;
 	}
 	
 	/**
 	 * @return the slave
 	 */
 	public DatePicker getSlave() {
 		return slave;
 	}
 	
 	/**
 	 * Returns the id of the slave.
 	 * @return
 	 */
 	public String getSlaveId() {
 		if(slave == null)
 			return null;
 		
 		return slave.getControlID();
 	}
 
 	/**
 	 * This DateTimePicker can be linked to another DateTimePicker to always update the "slave"
 	 * when this one is updated. When this controls date is changed and the slaves control has
 	 * no date or had the same date as this control had before it was changed, it is updated. 
 	 * This is all done through JavaScript events on the client side.
 	 * @param slave the slave to set
 	 */
 	public void setSlave(DateTimePicker slave) {
 		this.slave = slave;
 	}
 	
 	/**
 	 * Whether to use 'slider' or 'select'
 	 * @author dotto
 	 *
 	 */
 	public enum ControlTpye {
 		SLIDER("slider"), SELECT("select");
 		
 		private String code;
 		 
 		 private ControlTpye(String c) {
 		   code = c;
 		 }
 		 
 		 public String getCode() {
 		   return code;
 		 }
 		 
 		 @Override
 		public String toString() {
 			return getCode();
 		}
 	}
 
 }
