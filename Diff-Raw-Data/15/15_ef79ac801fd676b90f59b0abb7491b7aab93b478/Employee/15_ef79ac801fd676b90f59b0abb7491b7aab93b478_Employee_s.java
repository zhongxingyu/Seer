 package com.bus.dto;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.bus.util.HRUtil;
 
 @Entity
 @XmlRootElement
 @Table(name="employee")
 public class Employee implements Serializable{
 	
 	private Integer id;
 	private String documentkey;
 	private String workerid;
 	private String fullname;
 	private String sex;
 	private Date dob;
 	private String identitycode;
 	private String ethnic;
 	private String politicalstatus;
 	private String pob;
 	private Date timeofjoinprc;
 	private String homenumber;
 	private String mobilenumber;
 	private String email;
 	private String othercontact;
 	private String homeaddress;
 	private String postcode;
 	private String marriage;
 	private String qualification;
 	private Date firstworktime;
 	private String workertype;
 	private String joblevel;
 	private String placebelong;
 	private String domiciletype;
 	private String army;
 	private String remark;
 	private Date createtime;
 	private String status;
 	private Account account = new Account();
 	private Department department = new Department();
 	private Position position = new Position();
 	private Hrimage image;
 	private String colleage;
 	private String major;
 	private Set<Contract> contracts;
 	private Date transfertime;
 	private Set<Idmanagement> idcards;
 	
 	private Set<Promoandtransfer> transfers;
 	
 	public Employee(){
 		status = "A";
 	}
 	
 	public void copy(Employee e){
 		this.documentkey = e.getDocumentkey();
 		this.workerid = e.getWorkerid();
 		this.fullname = e.getFullname();
 		this.sex = e.getSex();
 		this.dob = e.getDob();
 		this.identitycode = e.getIdentitycode();
 		this.ethnic = e.getEthnic();
 		this.politicalstatus = e.getPoliticalstatus();
 		this.pob = e.getPob();
 		this.timeofjoinprc = e.getTimeofjoinrpc();
 		this.homenumber = e.getHomenumber();
 		this.mobilenumber = e.getMobilenumber();
 		this.email = e.getEmail();
 		this.othercontact = e.getOthercontact();
 		this.homeaddress = e.getHomeaddress();
 		this.postcode = e.getPostcode();
 		this.marriage = e.getMarriage();
 		this.qualification = e.getQualification();
 		this.firstworktime = e.getFirstworktime();
 		this.workertype = e.getWorkertype();
 		this.joblevel = e.getJoblevel();
 		this.placebelong = e.getPlacebelong();
 		this.domiciletype = e.getDomiciletype();
 		this.army = e.getArmy();
 		this.remark = e.getRemark();
 		this.department = e.getDepartment();
 		this.position = e.getPosition();
 		this.colleage = e.getColleage();
 		this.major = e.getMajor();
 		this.transfertime = e.getTransfertime();
 	}
 	
 	@ManyToOne
 	@JoinColumn(name="creator")
 	public Account getAccount(){
 		return account;
 	}
 	public void setAccount(Account account){
 		this.account = account;
 	}
 	
 	@ManyToOne
 	@JoinColumn(name="departmentid")
 	public Department getDepartment(){
 		return department;
 	}
 	public void setDepartment(Department department){
 		this.department = department;
 	}
 	
 	@ManyToOne
 	@JoinColumn(name="positionid")
 	public Position getPosition(){
 		return position;
 	}
 	public void setPosition(Position position){
 		this.position = position;
 	}
 	
 	@OneToOne
 	@JoinColumn(name="imageid")
 	public Hrimage getImage(){
 		return image;
 	}
 	public void setImage(Hrimage hrimage){
 		this.image = hrimage;
 	}
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name="id")
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	@Column(name="documentkey",nullable = false, length=8)
 	public String getDocumentkey() {
 		return documentkey;
 	}
 
 	public void setDocumentkey(String documentkey) {
 		this.documentkey = documentkey;
 	}
 
 	@Column(name="workerid", nullable = false, length=8)
 	public String getWorkerid() {
 		return workerid;
 	}
 
 	public void setWorkerid(String workerid) {
 		this.workerid = workerid;
 	}
 
 	@Column(name="fullname",nullable=false, length=16)
 	public String getFullname() {
 //		System.out.println(fullname);
 		return fullname;
 	}
 
 	public void setFullname(String fullname) {
 		this.fullname = fullname;
 	}
 
 	@Column(name="sex",nullable=false, length=1)
 	public String getSex() {
 		return sex;
 	}
 
 	public void setSex(String sex) {
 		this.sex = sex;
 	}
 
 	@Column(name="dob",nullable=false)
 	public Date getDob() {
 		return dob;
 	}
 
 	public void setDob(Date dob) {
 		this.dob = dob;
 	}
 
 	@Column(name="identitycode", nullable = false,length=18)
 	public String getIdentitycode() {
 		return identitycode;
 	}
 
 	public void setIdentitycode(String identitycode) {
 		this.identitycode = identitycode;
 	}
 
 	@Column(name="ethnic", nullable=false, length=8)
 	public String getEthnic() {
 		return ethnic;
 	}
 
 	public void setEthnic(String ethnic) {
 		this.ethnic = ethnic;
 	}
 
 	@Column(name="politicalstatus", nullable=false, length=8)
 	public String getPoliticalstatus() {
 		return politicalstatus;
 	}
 
 	public void setPoliticalstatus(String politicalstatus) {
 		this.politicalstatus = politicalstatus;
 	}
 
 	@Column(name="pob", nullable=false, length=8)
 	public String getPob() {
 		return pob;
 	}
 
 	public void setPob(String pob) {
 		this.pob = pob;
 	}
 
 	@Column(name="timeofjoinprc")
 	public Date getTimeofjoinrpc() {
 		return timeofjoinprc;
 	}
 
 	public void setTimeofjoinrpc(Date timeofjoinprc) {
 		this.timeofjoinprc = timeofjoinprc;
 	}
 
 	@Column(name="homenumber",length=64)
 	public String getHomenumber() {
 		return homenumber;
 	}
 
 	public void setHomenumber(String homenumber) {
 		this.homenumber = homenumber;
 	}
 
 	@Column(name="mobilenumber",length=64)
 	public String getMobilenumber() {
 		return mobilenumber;
 	}
 
 	public void setMobilenumber(String mobilenumber) {
 		this.mobilenumber = mobilenumber;
 	}
 
 	@Column(name="email",length=128)
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	@Column(name="othercontact",length=128)
 	public String getOthercontact() {
 		return othercontact;
 	}
 
 	public void setOthercontact(String othercontact) {
 		this.othercontact = othercontact;
 	}
 
 	@Column(name="homeaddress",length=256)
 	public String getHomeaddress() {
 		return homeaddress;
 	}
 
 	public void setHomeaddress(String homeaddress) {
 		this.homeaddress = homeaddress;
 	}
 
 	@Column(name="postcode",length=8)
 	public String getPostcode() {
 		return postcode;
 	}
 
 	public void setPostcode(String postcode) {
 		this.postcode = postcode;
 	}
 
 	@Column(name="marriage",length=1)
 	public String getMarriage() {
 		return marriage;
 	}
 
 	public void setMarriage(String marriage) {
 		this.marriage = marriage;
 	}
 
 	@Column(name="qualification",length=16)
 	public String getQualification() {
 //		System.out.println(qualification);
 		return qualification;
 	}
 
 	public void setQualification(String qualification) {
 		this.qualification = qualification;
 	}
 	
 	@Transient
 	public String getWorkage() {
 		if(this.dob != null){
 			Calendar c = Calendar.getInstance();
 			if(this.firstworktime == null)
			c.setTime(new Date());
 			Calendar cnow = Calendar.getInstance();
 			cnow.setTime(new Date());
 			Integer yearint = cnow.get(Calendar.YEAR) - c.get(Calendar.YEAR);
 			Integer monthint = (cnow.get(Calendar.MONTH) - c.get(Calendar.MONTH) + 12)%12;
 			Integer dayint  = (cnow.get(Calendar.DAY_OF_MONTH) - c.get(Calendar.DAY_OF_MONTH)+30)%30;
 			return yearint.toString()+"年"+monthint.toString()+"月"+dayint.toString()+"日";
 		}else
 			return "";
 	}
 	
 	@Transient
 	public void setWorkage(String workage) {
 		//
 	}
 
 	@Column(name="firstworktime")
 	public Date getFirstworktime() {
 		return firstworktime;
 	}
 
 	public void setFirstworktime(Date firstworktime) {
 		this.firstworktime = firstworktime;
 	}
 
 	@Column(name="workertype",length=16)
 	public String getWorkertype() {
 //		System.out.println(workertype);
 		return workertype;
 	}
 
 	public void setWorkertype(String workertype) {
 		this.workertype = workertype;
 	}
 
 	@Column(name="joblevel",length=8)
 	public String getJoblevel() {
 		return joblevel;
 	}
 
 	public void setJoblevel(String joblevel) {
 		this.joblevel = joblevel;
 	}
 
 	@Column(name="remark",length=256)
 	public String getRemark() {
 		return remark;
 	}
 
 	public void setRemark(String remark) {
 		this.remark = remark;
 	}
 
 	@Column(name="createtime", nullable=false)
 	@Temporal(TemporalType.TIMESTAMP)
 	public Date getCreatetime() {
 		return createtime;
 	}
 
 	public void setCreatetime(Date createtime) {
 		this.createtime = createtime;
 	}
 	
 	@Column(name="status", nullable=false, length=1)
 	public String getStatus() {
 		return status;
 	}
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	@Column(name="placebelong",length=16)
 	public String getPlacebelong() {
 		return placebelong;
 	}
 
 	public void setPlacebelong(String placebelong) {
 		this.placebelong = placebelong;
 	}
 
 	@Column(name="domiciletype",length=16)
 	public String getDomiciletype() {
 //		System.out.println(domiciletype);
 		return domiciletype;
 	}
 
 	public void setDomiciletype(String domiciletype) {
 		this.domiciletype = domiciletype;
 	}
 
 	@Column(name="army",length=64)
 	public String getArmy() {
 		return army;
 	}
 
 	public void setArmy(String army) {
 		this.army = army;
 	}
 
 	@Column(name="colleage",length=128)
 	public String getColleage() {
 		return colleage;
 	}
 
 	public void setColleage(String colleage) {
 		this.colleage = colleage;
 	}
 
 	@Column(name="major",length=128)
 	public String getMajor() {
 		return major;
 	}
 
 	public void setMajor(String major) {
 		this.major = major;
 	}
 
 	@Transient
 	public String getAge() {
 		if(this.dob != null){
 			Calendar c = Calendar.getInstance();
 			c.setTime(this.dob);
 			Calendar cnow = Calendar.getInstance();
 			cnow.setTime(new Date());
 			Integer yearint = cnow.get(Calendar.YEAR) - c.get(Calendar.YEAR);
 			if(cnow.get(Calendar.DAY_OF_YEAR) < c.get(Calendar.DAY_OF_YEAR))
 				yearint--;
 			Integer monthint = (cnow.get(Calendar.MONTH) - c.get(Calendar.MONTH) + 12)%12;
 			Integer dayint  = (cnow.get(Calendar.DAY_OF_MONTH) - c.get(Calendar.DAY_OF_MONTH)+30)%30;
 			return yearint.toString()+"岁"+monthint.toString()+"月"+dayint.toString()+"日";
 		}else
 			return "";
 	}
 
 	@Transient
 	public void setAge(String age) {
 		//
 	}
 
 	@OneToMany(fetch = FetchType.EAGER, mappedBy = "employee")
 	public Set<Contract> getContracts() {
 		return this.contracts;
 	}
  
 	public void setContracts(Set<Contract> contracts) {
 		this.contracts = contracts;
 	}
 	
 	
 	public void setTransfertime(Date date){
 		this.transfertime = date;
 	}
 	@Column(name="transfertime")
 	public Date getTransfertime(){
 		return this.transfertime;
 	}
 
 	@OneToMany(fetch = FetchType.EAGER, mappedBy = "employee")
 	public Set<Idmanagement> getIdcards() {
 		return idcards;
 	}
 
 	public void setIdcards(Set<Idmanagement> idcards) {
 		this.idcards = idcards;
 	}
 	
 	@Transient
 	public String getCreatetimestr(){
 		if(this.createtime != null)
 			return HRUtil.parseDateToString(this.createtime);
 		else
 			return "";
 	}
 	
 	@Transient
 	public String getDobstr(){
 		if(this.dob!= null)
 			return HRUtil.parseDateToString(this.dob);
 		else
 			return "";
 	}
 
 	@OneToMany(fetch = FetchType.EAGER, mappedBy = "employee")
 	public Set<Promoandtransfer> getTransfers() {
 		return transfers;
 	}
 
 	public void setTransfers(Set<Promoandtransfer> transfers) {
 		this.transfers = transfers;
 	}
 	
 }
