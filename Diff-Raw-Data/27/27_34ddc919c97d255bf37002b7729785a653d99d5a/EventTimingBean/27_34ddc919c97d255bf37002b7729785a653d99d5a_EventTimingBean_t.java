 package org.dianexus.triceps;
 
 import java.sql.Timestamp;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.sql.Date;
 
 import org.dianexus.triceps.modules.data.*;
import org.dianexus.triceps.InputEncoder;
 import org.apache.log4j.Logger;
 
 
 
 public class EventTimingBean implements VersionIF {
   static Logger logger = Logger.getLogger(EventTimingBean.class);
  	private int pageHitId;
 	private int pageHitEventsId;
 	private String src;
 	private String varName;
 	private String actionType="";
 	private String eventType="";
 	private Timestamp timestamp;
 	private int duration=0;
 	private String value1="";
 	private String value2="";
 	private StringTokenizer str;
 	private String token;
 	private int tokenCount;
 	private StringBuffer sb;
 	// TODO make this declarative
 	private int DBID = 1;
 	
 	
 
 	public EventTimingBean(){
 	
 	}
 	
 	/**
		Parses a single line from Event Timings, storing them within an EventBean
 	 * @param src
 	 * @param pageHitId
 	 * @return
 	 */
 	public EventTimingBean tokenizeEventString(String src,int pageHitId){
 		this.setPageHitId(pageHitId);
 		str = new StringTokenizer(src,",",false);
 		tokenCount=0;
 		while (str.hasMoreTokens()){
 			token = str.nextToken();
 			switch (tokenCount){
 			case 0:{
 				this.setVarName(token);
 				break;
 			}
 			case 1:{
 				this.setActionType(token);
 				break;
 			}
 			case 2:{
 				this.setEventType(token);
 				break;
 			}
 			case 3:{
 				long ts = new Long(token).longValue();
 				Date d = new Date(ts);
 				Timestamp tms = new Timestamp(d.getTime());
 				this.setTimestamp(tms);
 				break;
 			}
 			case 4: {
 				this.setDuration(new Integer(token).intValue());
 				break;
 			}
 			case 5:{
				this.setValue1(InputEncoder.encode(token));
 				break;
 			}
 			case 6:{
				StringBuffer sb2 = new StringBuffer(token);
				// remaining contents may contain commas, and thus be incorrectly treated as tokens
				// so, merge remaining contents into a single value
				while (str.hasMoreTokens()) {
					sb2.append(",").append((String) str.nextToken());
				}
				token = sb2.toString();

				this.setValue2(InputEncoder.encode(token));
 				break;
 			}
 			default:{
				logger.error("Should never get here, but got '" + token + "'");
				break;
 			}
 			}
 			
 			tokenCount++;
 		}
 		
 		return this;
 	}
 	/**
 	 * @return
 	 */
 	public boolean store(){
 		DialogixDAOFactory ddf = DialogixDAOFactory.getDAOFactory(DBID);
 		PageHitEventsDAO phedao = ddf.getPageHitEventsDAO();
 		phedao.setPageHitId(this.getPageHitId());
 		phedao.setVarName(this.getVarName());
 		phedao.setActionType(this.getActionType());
 		phedao.setEventType(this.getEventType());
 		phedao.setTimeStamp(this.getTimestamp());
 		phedao.setDuration(this.getDuration());
 		phedao.setValue1(this.getValue1());
 		phedao.setValue2(this.getValue2());
 		boolean res = phedao.setPageHitEvent();
 		this.setPageHitEventsId(phedao.getPageHitEventId());
 		
 	return res;
 	}
 	
 	/**
 	 * @return
 	 */
 	public boolean delete(){
 		DialogixDAOFactory ddf = DialogixDAOFactory.getDAOFactory(DBID);
 		PageHitEventsDAO phedao = ddf.getPageHitEventsDAO();
 		phedao.getPageHitEvents(pageHitEventsId);
 		return phedao.deletePageHitEvent();
 		
 	}
 	/**
 	 * @return
 	 */
 	public boolean read(){
 		DialogixDAOFactory ddf = DialogixDAOFactory.getDAOFactory(DBID);
 		PageHitEventsDAO phedao = ddf.getPageHitEventsDAO();
 		if(phedao.getPageHitEvents(this.pageHitEventsId)){
 			this.setVarName(phedao.getVarName());
 			this.setActionType(phedao.getActionType());
 			this.setEventType(phedao.getEventType());
 			this.setTimestamp(phedao.getTimeStamp());
 			this.setDuration(phedao.getDuration());
 			this.setValue1(phedao.getValue1());
 			this.setValue2(phedao.getValue2());
 			return true;
 		}
 		else{
 			return false;
 		}
 			
 	}
 	
 	/**
 	 * 
 	 */
 	public void clear(){
		this.setPageHitEventsId(-1);
 		this.setVarName(null);
 		this.setActionType(null);
 		this.setEventType(null);
 		this.setTimestamp(null);
 		this.setDuration(0);
 		this.setValue1(null);
 		this.setValue2(null);
 	}
 	
 	/**
 	 * @return
 	 */
 	public String getVarName() {
 		if(varName==null){
 			varName="";
 		}
 		return varName;
 	}
 
 	/**
 	 * @param varName
 	 */
 	public void setVarName(String varName) {
 		this.varName = varName;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getActionType() {
 		if(actionType==null){
 			actionType="";
 		}
 		return actionType;
 	}
 
 	/**
 	 * @param actionType
 	 */
 	public void setActionType(String actionType) {
 		this.actionType = actionType;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getDuration() {
 		
 		return duration;
 	}
 
 	/**
 	 * @param duration
 	 */
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getEventType() {
 		return eventType;
 	}
 
 	/**
 	 * @param eventType
 	 */
 	public void setEventType(String eventType) {
 		this.eventType = eventType;
 	}
 
 	/**
 	 * @return
 	 */
 	public Timestamp getTimestamp() {
 		return timestamp;
 	}
 
 	/**
 	 * @param timestamp
 	 */
 	public void setTimestamp(Timestamp timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getValue1() {
 		return value1;
 	}
 
 	/**
 	 * @param value1
 	 */
 	public void setValue1(String value1) {
 		this.value1 = value1;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getValue2() {
 		return value2;
 	}
 
 	/**
 	 * @param value2
 	 */
 	public void setValue2(String value2) {
 		this.value2 = value2;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getPageHitId() {
 		return this.pageHitId;
 	}
 
 	/**
 	 * @param displayCount
 	 */
 	public void setPageHitId(int pageHitId) {
 		this.pageHitId = pageHitId;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getPageHitEventsId() {
 		return pageHitEventsId;
 	}
 
 	/**
 	 * @param pageHitEventsId
 	 */
 	public void setPageHitEventsId(int pageHitEventsId) {
 		this.pageHitEventsId = pageHitEventsId;
 	}
 
 }
