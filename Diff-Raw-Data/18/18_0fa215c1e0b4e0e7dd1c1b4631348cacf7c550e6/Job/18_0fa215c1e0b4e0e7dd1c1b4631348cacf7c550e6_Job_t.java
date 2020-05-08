 package org.vpac.grisu.backend.model.job;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.annotations.CollectionOfElements;
 import org.simpleframework.xml.Attribute;
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.ElementMap;
 import org.simpleframework.xml.Root;
 import org.vpac.grisu.backend.model.ProxyCredential;
 import org.vpac.grisu.utils.SeveralXMLHelpers;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import au.org.arcs.jcommons.constants.Constants;
 import au.org.arcs.jcommons.utils.JsdlHelpers;
 
 /**
  * This class holds all the relevant information about a job.
  * 
  * @author Markus Binsteiner
  * 
  */
 @Entity
 @Table(name = "jobs")
 @Root
 public class Job implements Comparable<Job> {
 
 	static final Logger myLogger = Logger.getLogger(Job.class.getName());
 
 	// for hibernate
 	private Long id;
 
 	// for the user to remember the job
 	@Attribute
 	private String jobname = null;
 	// the jobhandle that comes back from the job submission
 	@Element
 	private String jobhandle = null;
 
 	// the user's dn
 	@Element
 	private String dn = null;
 
 	// the vo for which the job runs
 	@Element
 	private String fqan = null;
 
 	// the job description
 	private Document jobDescription = null;
 
 	@Element(data=true)
 	private String serializedJobDescription = null;
 
 	// this is the job description that was submitted to the gateway (probably a
 	// gt4 rsl document)
 	@Element(data=true)
 	private String submittedJobDescription = null;
 
 	// the status of the job
 	@Attribute
 	private int status = -1000;
 
 	// the credential that is/was used to submit the job
 	private ProxyCredential credential = null;
 
 	@Element
 	private String submissionType = null;
 
 	@Element
 	private boolean isArchived = false;
 
 	private Date lastStatusCheck = null;
 
 	@ElementMap(entry="property", key="key", attribute=true, inline=true)
 	private Map<String, String> jobProperties = Collections
 	.synchronizedMap(new HashMap<String, String>());
 
 	@Attribute
 	private boolean isBatchJob = false;
 
 	@ElementMap(entry="jobProperty", key="key", attribute=true, inline=true)
 	private Map<Long, String> logMessages = Collections
 	.synchronizedMap(new TreeMap<Long, String>());
 
 	// for hibernate
 	public Job() {
 	}
 
 	/**
 	 * Creates a Job and associates a jsdl document with it straight away. It
 	 * parses this jsdl document for the name of the job, calculates the final
 	 * name and stores it back into the jsdl document. Try to store it as soon
 	 * as possible to prevent duplicate jobnames.
 	 * 
 	 * @param dn
 	 *            the dn of the user who created this job
 	 * @param jsdl
 	 *            the job description in jsdl format
 	 * @param createJobNameMethod
 	 *            the method how to create the jobname (if you have already a
 	 *            job with the same name)
 	 * @throws SAXException
 	 *             if the job description is not valid xml
 	 * @throws XPathExpressionException
 	 *             if the job description does not contain a jobname
 	 */
 	public Job(final String dn, final Document jsdl) throws SAXException,
 	XPathExpressionException {
 		this.dn = dn;
 		// if ( ! JsdlHelpers.validateJSDL(jobDescription) ) throw new
 		// SAXException("Job description not a valid jsdl document");
 		this.jobDescription = jsdl;
 		this.jobname = JsdlHelpers.getJobname(jsdl);
 		try {
 			JsdlHelpers.setJobname(jsdl, this.jobname);
 		} catch (XPathExpressionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw e;
 		}
 		// TODO change the jobname in the jobDescription
 	}
 
 	//
 	// TODO later add requirements
 	// private ArrayList<Requirement> requirements = null;
 
 	/**
 	 * If you use this constructor save the Job object straight away to prevent
 	 * duplicate names.
 	 * 
 	 * @param jobname
 	 *            the (base-)name you want for your job
 	 */
 	public Job(final String dn, final String jobname) {
 		this.dn = dn;
 		this.jobname = jobname;
 	}
 
 	public synchronized void addJobProperties(
 			final Map<String, String> properties) {
 		getJobProperties().putAll(properties);
 	}
 
 	public synchronized void addJobProperty(final String key, final String value) {
 		getJobProperties().put(key, value);
 	}
 
 	public synchronized void addLogMessage(String message) {
 		Date now = new Date();
 		this.logMessages.put(now.getTime(), message);
 	}
 
 	public int compareTo(Job arg0) {
 
 		Long thisSubTime = null;
 		try {
 			thisSubTime = Long.parseLong(this
 					.getJobProperty(Constants.SUBMISSION_TIME_KEY));
 		} catch (Exception e) {
 			thisSubTime = 0L;
 		}
 
 		Long otherSubTime = null;
 		try {
 			otherSubTime = Long.parseLong(arg0
 					.getJobProperty(Constants.SUBMISSION_TIME_KEY));
 		} catch (Exception e) {
 			otherSubTime = 0L;
 		}
 
 		int result = thisSubTime.compareTo(otherSubTime);
 
 		if (result != 0) {
 			return result;
 		} else {
 			return this.jobname.compareTo(arg0.getJobname());
 		}
 	}
 
 	@Override
 	public boolean equals(final Object other) {
 		if (!(other instanceof Job)) {
 			return false;
 		}
 
 		Job otherJob = (Job) other;
 
 		if (this.dn.equals(otherJob.getDn())
 				&& this.jobname.equals(otherJob.getJobname())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Gets the credential for this job which is used to submit it to the
 	 * endpoint.
 	 * 
 	 * @return the credential
 	 */
 	@Transient
 	public ProxyCredential getCredential() {
 		return this.credential;
 	}
 
 	/**
 	 * The dn of the user who created/submits this job.
 	 * 
 	 * @return the dn
 	 */
 	@Column(nullable = false)
 	public String getDn() {
 		return dn;
 	}
 
 	/**
 	 * The fqan of the VO/group for which this job is/was submitted.
 	 * 
 	 * @return the fqan
 	 */
 	public String getFqan() {
 		return fqan;
 	}
 
 	// hibernate
 	@Id
 	@GeneratedValue
 	private Long getId() {
 		return id;
 	}
 
 	/**
 	 * Gets the jsdl job description for this job.
 	 * 
 	 * @return the jsdl document
 	 */
 	@Transient
 	public Document getJobDescription() {
 		// for serialized/archived version
 		if ( (this.jobDescription == null) && StringUtils.isNotBlank(serializedJobDescription) ) {
 			this.jobDescription = SeveralXMLHelpers.fromString(serializedJobDescription);
 		}
 		return this.jobDescription;
 	}
 
 	/**
 	 * Gets the (JobSubmitter-specific) jobhandle with which this job was
 	 * submitted.
 	 * 
 	 * @return the jobhandle or null if the job was not submitted
 	 */
 	public String getJobhandle() {
 		return jobhandle;
 	}
 
 	/**
 	 * Gets the (along with the users' dn unique) name of the job.
 	 * 
 	 * @return the jobname
 	 */
 	@Column(nullable = false)
 	public String getJobname() {
 		return jobname;
 	}
 
 	@CollectionOfElements(fetch = FetchType.EAGER)
 	public synchronized Map<String, String> getJobProperties() {
 		return jobProperties;
 	}
 
 	// /**
 	// * Gets the host to which this job is going to be submitted/was submitted.
 	// *
 	// * @return the hostname
 	// */
 	// public String getSubmissionHost() {
 	// return submissionHost;
 	// }
 
 	// /**
 	// * Sets the host to which this job is going to be submitted.
 	// *
 	// * @param host
 	// * the hostname (like ng2.vpac.org)
 	// */
 	// public void setSubmissionHost(final String host) {
 	// this.submissionHost = host;
 	// }
 
 	@Transient
 	public String getJobProperty(final String key) {
 		return this.jobProperties.get(key);
 	}
 
 	/**
 	 * For hibernate conversion xml-document -> string.
 	 * 
 	 * @return xml string
 	 * @throws TransformerFactoryConfigurationError
 	 * @throws TransformerException
 	 */
 	@Column(length = 15000)
 	private String getJsdl() throws TransformerException {
 		return SeveralXMLHelpers.toString(jobDescription);
 	}
 
 	public Date getLastStatusCheck() {
 
 		if (lastStatusCheck == null) {
 			lastStatusCheck = new Date();
 		}
 		return lastStatusCheck;
 	}
 
 	@CollectionOfElements(fetch = FetchType.EAGER)
 	public Map<Long, String> getLogMessages() {
 		return logMessages;
 	}
 
 	/**
 	 * Gets the status of the job. This does not ask the responsible
 	 * {@link JobSubmitter} about the status but the database. So take care to
 	 * refresh the job status before using this.
 	 * 
 	 * @return the status of the job
 	 */
 	@Column(nullable = false)
 	public int getStatus() {
 		return status;
 	}
 
 	/**
 	 * Gets the type of the {@link JobSubmitter} that was used to submit this
 	 * job.
 	 * 
 	 * @return the type of the submitter (like "GT4")
 	 */
 	public String getSubmissionType() {
 		return submissionType;
 	}
 
 	/**
 	 * Returns the (JobSubmitter-specific) job description (like rsl for gt4).
 	 * 
 	 * @return the job description or null if the job was not submitted yet
 	 */
 	@Column(length = 2550)
 	public String getSubmittedJobDescription() {
 		return submittedJobDescription;
 	}
 
 	@Override
 	public int hashCode() {
 		return this.dn.hashCode() + this.jobname.hashCode();
 	}
 
 	@Transient
 	public boolean isArchived() {
 		return isArchived;
 	}
 
 	public boolean isBatchJob() {
 		return isBatchJob;
 	}
 
 	public void setArchived(boolean archived) {
 		this.isArchived = archived;
 	}
 
 	public void setBatchJob(boolean is) {
 		this.isBatchJob = is;
 	}
 
 	/**
 	 * Connects a job to a credential.
 	 * 
 	 * @param credential
 	 *            the credential
 	 */
 	public void setCredential(final ProxyCredential credential) {
 		this.credential = credential;
 	}
 
 	/**
 	 * Sets the dn of the user who submits this job. Should be only used by
 	 * hibernate
 	 * 
 	 * @param dn
 	 *            the dn
 	 */
 	protected void setDn(final String dn) {
 		this.dn = dn;
 	}
 
 	/**
 	 * Sets the fqan of the VO/group for which this job is going to be
 	 * submitted.
 	 * 
 	 * @param fqan
 	 */
 	public void setFqan(final String fqan) {
 		this.fqan = fqan;
 	}
 
 	// hibernate
 	private void setId(final Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * Sets the job description for this job. Take care that you have got the
 	 * same jobname within this job description and in the jobname property.
 	 * 
 	 * @param jobDescription
 	 *            the job description as jsdl xml document
 	 */
 	public void setJobDescription(final Document jobDescription) {
 		this.jobDescription = jobDescription;
 		this.serializedJobDescription = SeveralXMLHelpers.toString(jobDescription);
 	}
 
 	/**
 	 * Sets the jobhandle. Only a JobSubmitter should use this method.
 	 * 
 	 * @param jobhandle
 	 *            the (JobSubmitter-specific) job handle
 	 */
 	public void setJobhandle(final String jobhandle) {
 		this.jobhandle = jobhandle;
 	}
 
 	// ---------------------
 	// job information
 	// most of this will be removed once only the jobproperties map is used
 	// ---------------------
 	// @CollectionOfElements(fetch = FetchType.EAGER)
 	// public final List<String> getInputFiles() {
 	// return inputFiles;
 	// }
 	//
 	// private void setInputFiles(final List<String> inputFiles) {
 	// this.inputFiles = inputFiles;
 	// }
 	//
 	// public final void addInputFile(final String inputFile) {
 	// this.inputFiles.add(inputFile);
 	// }
 	//
 	// public final void removeInputFile(final String inputFile) {
 	// this.inputFiles.remove(inputFile);
 	// }
 	//
 	// public final String getJob_directory() {
 	// return job_directory;
 	// }
 	//
 	// public final void setJob_directory(final String job_directory) {
 	// this.job_directory = job_directory;
 	// }
 	//
 	// public final String getStderr() {
 	// return stderr;
 	// }
 	//
 	// public final void setStderr(final String stderr) {
 	// this.stderr = stderr;
 	// }
 	//
 	// public final String getStdout() {
 	// return stdout;
 	// }
 	//
 	// public final void setStdout(final String stdout) {
 	// this.stdout = stdout;
 	// }
 	//
 	// public final String getApplication() {
 	// return application;
 	// }
 	//
 	// public final void setApplication(final String application) {
 	// this.application = application;
 	// }
 
 	/**
 	 * Sets the name of this job. Take care that it is unique when combined with
 	 * the users' dn.
 	 * 
 	 * @param jobname
 	 *            the jobname
 	 */
 	private void setJobname(final String jobname) {
 		this.jobname = jobname;
 	}
 
 	private synchronized void setJobProperties(
 			final Map<String, String> jobProperties) {
 		this.jobProperties = jobProperties;
 	}
 
 	/**
 	 * For hibernate conversion string -> xml-document.
 	 * 
 	 * @param jsdl_string
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	private void setJsdl(final String jsdl_string) throws Exception {
 		if ((jsdl_string == null)
 				|| jsdl_string
 				.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")) {
 			return;
 		}
 
 		try {
 			jobDescription = SeveralXMLHelpers.fromString(jsdl_string);
 			serializedJobDescription = jsdl_string;
 		} catch (Exception e) {
 			myLogger
 			.debug("Error saving jsdl for job. That's most probably ok. "
 					+ e.getMessage());
 			// e.printStackTrace();
 			// TODO check what happens here
 		}
 
 	}
 
 	public void setLastStatusCheck(Date lastStatusCheck) {
 		this.lastStatusCheck = lastStatusCheck;
 	}
 
 	private void setLogMessages(Map<Long, String> logMessages) {
 		this.logMessages = logMessages;
 	}
 
 	/**
 	 * Sets the current status of this job. Only a {@link JobSubmitter} should
 	 * use this method.
 	 * 
 	 * @param status
 	 */
 	public void setStatus(final int status) {
 		this.status = status;
 	}
 
 	/**
 	 * Sets the type of the submitter you want to use to submit this job. grisu
 	 * only supports "GT4" at the moment.
 	 * 
 	 * @param submissionType
 	 *            the type of the job submitter
 	 */
 	public void setSubmissionType(final String submissionType) {
 		this.submissionType = submissionType;
 	}
 
 	/**
 	 * Sets the (JobSubmitter-specific) job description. Only a JobSubmitter
 	 * should use this method.
 	 * 
 	 * @param desc
 	 *            the job description in the JobSubmitter-specific format
 	 */
 	public void setSubmittedJobDescription(final String desc) {
 		this.submittedJobDescription = desc;
 	}
 
 }
