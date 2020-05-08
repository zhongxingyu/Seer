 /**
  * Copyright (c) 2012 Maven Lab Private Limited. All rights reserved.
  */
 package com.mavenlab.caspian.model;
 
 import java.lang.reflect.Constructor;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Index;
 
 import com.mavenlab.caspian.util.NumberGenerator;
 
 /**
  * Class : Job.java
  * 
  * @author <a href="mailto:yogie@mavenlab.com">Yogie Kurniawan</a>
  * @since Jul 27, 2012 11:51:31 AM
  */
 @Entity
 @Table(name = "jobs")
 @SequenceGenerator(name = "SEQ_JOB", sequenceName = "SEQ_JOB", initialValue = 1, allocationSize = 1)
 @NamedQueries({
 		@NamedQuery(name = "caspian.entity.job.findByBookingId", query = "FROM Job " + "WHERE bookingId = :bookingId ORDER BY id"),
 		@NamedQuery(name = "caspian.entity.job.findByJobNo", query = "FROM Job " + "WHERE jobNo = :jobNo"),
 		@NamedQuery(name = "caspian.entity.job.findByJobId", query = "FROM Job " + "WHERE jobId = :jobId") })
 public class Job extends EntityBase {
 
 	private static final long serialVersionUID = -4078290243262424659L;
 
 	public final static String DISPATCHING = "DISPATCHING";
 	public final static String PENDING = "PENDING";
 	public final static String CONFIRMED = "CONFIRMED";
 	public final static String ONBOARD = "ONBOARD";
 	public final static String COMPLETED = "COMPLETED";
 	public final static String INCOMPLETED = "INCOMPLETED";
 	public final static String CANCELLED = "CANCELLED";
	public final static String FAILED = "FAILED";
 	public final static String INVALID = "INVALID";
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JOB")
 	private Long id;
 
 	@Column(name = "job_id")
 	@Index(name = "idx_job_id")
 	private Long jobId;
 
 	@Column(name = "job_no", length = 64)
 	@Index(name = "idx_job_no")
 	private String jobNo;
 
 	@Column(name = "job_status", length = 64)
 	private String jobStatus;
 
 	@Column(name = "taxi_number", length = 16)
 	private String taxiNumber;
 	
 	@Column(name = "booking_fee", length = 16)
 	private String bookingFee;
 
 	@Column(name = "eta", length = 16)
 	private String eta;
 
 	@Column(name = "taxi_type", length = 64)
 	private String taxiType;
 
 	@Column(name = "latitude", length = 64)
 	private String latitude;
 
 	@Column(name = "longitude", length = 64)
 	private String longitude;
 
 	@Column(name = "taxi_company", length = 64)
 	private String taxiCompany;
 
 	@Column(name = "booking_id")
 	@Index(name = "idx_job_booking_id")
 	private Long bookingId;
 
 	/**
 	 * {@link Constructor}
 	 */
 	public Job() {
 		super();
 		jobId = NumberGenerator.nextNumber();
 		jobStatus = DISPATCHING;
 	}
 
 	/**
 	 * {@link Constructor}
 	 * 
 	 * @param booking
 	 */
 	public Job(Booking booking) {
 		super();
 		jobId = NumberGenerator.nextNumber();
 		jobStatus = DISPATCHING;
 		bookingId = booking.getId();
 	}
 
 	/**
 	 * Get Formatted Text
 	 * 
 	 * @return {@link String}
 	 */
 	public String getFormattedText() {
 		return "JOBNO=" + jobNo + "|STATUS=" + jobStatus + "|TN=" + taxiNumber + "|ETA=" + eta + "|TT=" + taxiType + "|LAT=" + latitude + "|LONG=" + longitude + "|MODEL=" + taxiCompany;
 
 	}
 
 	/**
 	 * @return the id
 	 */
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the jobNo
 	 */
 	public String getJobNo() {
 		return jobNo;
 	}
 
 	/**
 	 * @param jobNo
 	 *            the jobNo to set
 	 */
 	public void setJobNo(String jobNo) {
 		this.jobNo = jobNo;
 	}
 
 	/**
 	 * @return the jobId
 	 */
 	public Long getJobId() {
 		return jobId;
 	}
 
 	/**
 	 * @param jobId
 	 *            the jobId to set
 	 */
 	public void setJobId(Long jobId) {
 		this.jobId = jobId;
 	}
 
 	/**
 	 * @return the jobStatus
 	 */
 	public String getJobStatus() {
 		return jobStatus;
 	}
 
 	/**
 	 * @param jobStatus
 	 *            the jobStatus to set
 	 */
 	public void setJobStatus(String jobStatus) {
 		this.jobStatus = jobStatus;
 	}
 
 	/**
 	 * @return the bookingId
 	 */
 	public Long getBookingId() {
 		return bookingId;
 	}
 
 	/**
 	 * @param bookingId
 	 *            the bookingId to set
 	 */
 	public void setBookingId(Long bookingId) {
 		this.bookingId = bookingId;
 	}
 
 	/**
 	 * @return the taxiNumber
 	 */
 	public String getTaxiNumber() {
 		return taxiNumber;
 	}
 
 	/**
 	 * @param taxiNumber
 	 *            the taxiNumber to set
 	 */
 	public void setTaxiNumber(String taxiNumber) {
 		this.taxiNumber = taxiNumber;
 	}
 
 	/**
 	 * @return the bookingFee
 	 */
 	public String getBookingFee() {
 		return bookingFee;
 	}
 
 	/**
 	 * @param bookingFee the bookingFee to set
 	 */
 	public void setBookingFee(String bookingFee) {
 		this.bookingFee = bookingFee;
 	}
 
 	/**
 	 * @return the eta
 	 */
 	public String getEta() {
 		return eta;
 	}
 
 	/**
 	 * @param eta
 	 *            the eta to set
 	 */
 	public void setEta(String eta) {
 		this.eta = eta;
 	}
 
 	/**
 	 * @return the taxiType
 	 */
 	public String getTaxiType() {
 		return taxiType;
 	}
 
 	/**
 	 * @param taxiType
 	 *            the taxiType to set
 	 */
 	public void setTaxiType(String taxiType) {
 		this.taxiType = taxiType;
 	}
 
 	/**
 	 * @return the latitude
 	 */
 	public String getLatitude() {
 		return latitude;
 	}
 
 	/**
 	 * @param latitude
 	 *            the latitude to set
 	 */
 	public void setLatitude(String latitude) {
 		this.latitude = latitude;
 	}
 
 	/**
 	 * @return the longitude
 	 */
 	public String getLongitude() {
 		return longitude;
 	}
 
 	/**
 	 * @param longitude
 	 *            the longitude to set
 	 */
 	public void setLongitude(String longitude) {
 		this.longitude = longitude;
 	}
 
 	/**
 	 * @return the taxiCompany
 	 */
 	public String getTaxiCompany() {
 		return taxiCompany;
 	}
 
 	/**
 	 * @param taxiCompany
 	 *            the taxiCompany to set
 	 */
 	public void setTaxiCompany(String taxiCompany) {
 		this.taxiCompany = taxiCompany;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Job [id=" + id + ", jobId=" + jobId + ", jobNo=" + jobNo + ", jobStatus=" + jobStatus + ", taxiNumber=" + taxiNumber + ", eta=" + eta + ", taxiType=" + taxiType + ", latitude=" + latitude + ", longitude=" + longitude + ", taxiCompany=" + taxiCompany + ", bookingId=" + bookingId + "]";
 	}
 
 }
