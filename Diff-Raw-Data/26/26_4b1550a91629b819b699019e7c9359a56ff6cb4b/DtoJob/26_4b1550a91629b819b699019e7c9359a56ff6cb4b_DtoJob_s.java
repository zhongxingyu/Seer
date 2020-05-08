 package grisu.model.dto;
 
 import grisu.control.JobConstants;
 import grisu.jcommons.constants.Constants;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 
 /**
  * This one holds information about a job that was created (and maybe already
  * submitted to the endpoint resource).
  * 
  * You can use this to query information like job-directory and status of the
  * job. Have a look in the Constants class in the GlueInterface module of the
  * Infosystems project for values of keys of possible job properties.
  * 
  * @author Markus Binsteiner
  * 
  */
 @XmlRootElement(name = "job")
 public class DtoJob implements Comparable<DtoJob> {
 
 	static final Logger myLogger = Logger.getLogger(DtoJob.class.getName());
 
 	public static DtoJob createJob(int status,
 			Map<String, String> jobProperties, Set<String> inputFiles,
 			Map<Long, String> logMessages, boolean isArchived) {
 
 		final DtoJob result = new DtoJob();
 
 		result.setStatus(status);
 
 		final List<DtoProperty> list = new LinkedList<DtoProperty>();
 		for (final String key : jobProperties.keySet()) {
 			final DtoProperty temp = new DtoProperty();
 			temp.setKey(key);
 			temp.setValue(jobProperties.get(key));
 			list.add(temp);
 		}
 		final DtoProperty temp2 = new DtoProperty();
 		temp2.setKey(Constants.INPUT_FILE_URLS_KEY);
 		temp2.setValue(StringUtils.join(inputFiles, ","));
 		list.add(temp2);
 		result.setProperties(list);
 
 		if (logMessages != null) {
 			final DtoLogMessages log = new DtoLogMessages();
 			for (final Long date : logMessages.keySet()) {
 				log.addMessage(new Date(date), logMessages.get(date));
 			}
 			result.setLogMessages(log);
 		}
 
 		return result;
 	}
 
 	public static String getProperty(DtoJob job, String key) {
 
 		if (StringUtils.isBlank(key)) {
 			return null;
 		}
 
 		for (DtoProperty p : job.getProperties()) {
 			if (key.equals(p.getKey())) {
 				return p.getValue();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * The list of job properties.
 	 */
 	private List<DtoProperty> properties = new LinkedList<DtoProperty>();
 
 	/**
 	 * The log messages for this job until now.
 	 */
 	private DtoLogMessages logMessages = new DtoLogMessages();
 
 	/**
 	 * The status of the job. Be aware that, depending on how you queried for
 	 * this job, this can be stale information.
 	 */
 	private int status;
 
 	private boolean isArchived = false;
 
 	public void addJobProperty(String key, String value) {
 		properties.add(new DtoProperty(key, value));
 	}
 
 	public int compareTo(DtoJob o) {
 
 		try {
 			Long thisSubTime = null;
 			try {
 				thisSubTime = Long
 						.parseLong(readJobProperty(Constants.SUBMISSION_TIME_KEY));
 			} catch (final Exception e) {
 				thisSubTime = 0L;
 			}
 
 			Long otherSubTime = null;
 			try {
 				otherSubTime = Long.parseLong(o
 						.readJobProperty(Constants.SUBMISSION_TIME_KEY));
 			} catch (final Exception e) {
 				otherSubTime = 0L;
 			}
 
 			final int result = thisSubTime.compareTo(otherSubTime);
 
 			if (result != 0) {
 				return result;
 			} else {
 				String thisJobname = this.jobname();
 				String otherJobname = o.jobname();
 				if (thisJobname == null) {
 					thisJobname = "xxxxxxx";
 				}
 				if (otherJobname == null) {
 					otherJobname = "xxxxxxxx";
 				}
 
 				return thisJobname.compareTo(otherJobname);
 			}
 		} catch (final Exception e) {
 			myLogger.error(e);
 			return 0;
 		}
 	}
 
 	@Override
 	public boolean equals(Object o) {
 
 		if (!(o instanceof DtoJob)) {
 			return false;
 		}
 
 		return ((DtoJob) o).jobname().equals(this.jobname());
 
 	}
 
 	@XmlElement(name = "logmessages")
 	public DtoLogMessages getLogMessages() {
 		return logMessages;
 	}
 
 	@XmlElement(name = "jobproperty")
 	public List<DtoProperty> getProperties() {
 		return properties;
 	}
 
 	@XmlElement(name = "status")
 	public int getStatus() {
 		return status;
 	}
 
 	@Override
 	public int hashCode() {
 		return 253 * jobname().hashCode();
 	}
 
 	@XmlAttribute(name = "archived")
 	public boolean isArchived() {
 		return isArchived;
 	}
 
 	public String jobname() {
 		return propertiesAsMap().get(Constants.JOBNAME_KEY);
 	}
 
 	public String jobProperty(String string) {
 		return propertiesAsMap().get(string);
 	}
 
 	public Map<Date, String> logMessagesAsMap() {
 
 		final Map<Date, String> map = new TreeMap<Date, String>();
 
 		for (final DtoLogMessage msg : getLogMessages().getMessages()) {
 			map.put(msg.getDate(), msg.getMessage());
 		}
 
 		return map;
 	}
 
 	public String logMessagesAsString(boolean withDate) {
 
 		final StringBuffer result = new StringBuffer();
 
 		final Map<Date, String> temp = logMessagesAsMap();
 
 		for (final Date date : temp.keySet()) {
 			if (withDate) {
 				result.append(date.toString() + ": ");
 			}
 			result.append(temp.get(date) + "\n");
 		}
 
 		return result.toString();
 
 	}
 
 	public Map<String, String> propertiesAsMap() {
 
 		final Map<String, String> map = new TreeMap<String, String>();
 
 		for (final DtoProperty prop : getProperties()) {
 			map.put(prop.getKey(), prop.getValue());
 		}
 
 		return map;
 	}
 
 	public String readJobProperty(String key) {
 		return propertiesAsMap().get(key);
 	}
 
 	public void setArchived(boolean isArchived) {
 		this.isArchived = isArchived;
 	}
 
 	public void setLogMessages(DtoLogMessages msgs) {
 		this.logMessages = msgs;
 	}
 
 	public void setProperties(List<DtoProperty> properties) {
 		this.properties = properties;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	public String statusAsString() {
 		return JobConstants.translateStatus(status);
 	}
 }
