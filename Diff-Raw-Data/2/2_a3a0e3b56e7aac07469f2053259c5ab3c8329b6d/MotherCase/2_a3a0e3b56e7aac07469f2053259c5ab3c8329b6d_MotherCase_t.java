 package org.motechproject.care.reporting.domain.dimension;
 
 import org.hibernate.annotations.Cascade;
 import org.motechproject.care.reporting.domain.SelfUpdatable;
 import org.motechproject.care.reporting.domain.annotations.ExternalPrimaryKey;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.persistence.*;
 import java.util.Arrays;
 import java.util.Date;
 
 
 @Entity
 @Table(name = "mother_case", uniqueConstraints = @UniqueConstraint(columnNames = "case_id"))
 public class MotherCase extends SelfUpdatable<MotherCase> implements java.io.Serializable {
     private static final Logger logger = LoggerFactory.getLogger("commcare-reporting-mapper");
 
     private int id;
 	private Flw flw;
 	private FlwGroup flwGroup;
     @ExternalPrimaryKey
 	private String caseId;
 	private String caseName;
 	private String caseType;
 	private Date dateModified;
 	private Date serverDateModified;
     private Integer familyNumber;
 	private Integer hhNumber;
 	private String husbandName;
 	private String lastVisitType;
 	private String motherAlive;
 	private Date motherDob;
 	private String motherName;
 	private Date closedOn;
 	private Date add;
 	private Short age;
 	private String birthPlace;
 	private String complications;
 	private Date dateNextBp;
 	private Date dateNextCf;
 	private Date dateNextEb;
 	private Date dateNextPnc;
 	private String eatsMeat;
 	private Date edd;
 	private String enrolledInKilkari;
 	private String familyPlanningType;
 	private Short howManyChildren;
 	private String interestInKilkari;
 	private String lastPregTt;
 	private Date lmp;
 	private String mobileNumber;
 	private Short numBoys;
 	private Date dateCf1;
 	private Date dateCf2;
 	private Date dateCf3;
 	private Date dateCf4;
 	private Date dateCf5;
 	private Date dateCf6;
 	private Date dateEb1;
 	private Date dateEb2;
 	private Date dateEb3;
 	private Date dateEb4;
 	private Date dateEb5;
 	private Date dateEb6;
 	private String allPncOnTime;
 	private Date datePnc1;
 	private Date datePnc2;
 	private Date datePnc3;
 	private String firstPncTime;
 	private Integer pnc1DaysLate;
 	private Integer pnc2DaysLate;
 	private Integer pnc3DaysLate;
 	private Date ttBoosterDate;
 	private String sba;
 	private String sbaPhone;
 	private String accompany;
 	private Date anc1Date;
 	private Date anc2Date;
 	private Date anc3Date;
 	private Date anc4Date;
 	private String cleanCloth;
 	private String coupleInterested;
 	private Date dateBp1;
 	private Date dateBp2;
 	private Date dateBp3;
 	private Date dateLastVisit;
 	private String deliveryType;
 	private Short ifaTablets;
 	private Date ifaTablets100;
 	private String materials;
 	private String maternalEmergency;
 	private String maternalEmergencyNumber;
 	private String phoneVehicle;
 	private String savingMoney;
 	private Date tt1Date;
 	private Date tt2Date;
 	private String vehicle;
 	private String birthStatus;
 	private Date migrateOutDate;
 	private String migratedStatus;
 	private String status;
 	private String term;
 	private Date dateCf7;
     private Date dateDelFu;
     private Date dateNextReg;
     private String institutional;
     private Date dob;
     private Boolean closed;
     private Date creationTime;
     private Date lastModifiedTime;
     private Flw closedBy;
     private String mobileNumberWhose;
     private int bpVisitNum;
     private int wardNumber;
     private int ebVisitNum;
     private int pncVisitNum;
     private int cfVisitNum;
 
     public MotherCase() {
         Date date = new Date();
         creationTime = date;
         lastModifiedTime = date;
 	}
 
     @Id
 	@Column(name = "id", unique = true, nullable = false)
     @GeneratedValue(strategy = GenerationType.IDENTITY)
 	public int getId() {
 		return this.id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "user_id")
     @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
 	public Flw getFlw() {
 		return this.flw;
 	}
 
 	public void setFlw(Flw flw) {
 		this.flw = flw;
 	}
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "owner_id")
     @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
 	public FlwGroup getFlwGroup() {
 		return this.flwGroup;
 	}
 
 	public void setFlwGroup(FlwGroup flwGroup) {
 		this.flwGroup = flwGroup;
 	}
 
 	@Column(name = "case_id", unique = true)
 	public String getCaseId() {
 		return this.caseId;
 	}
 
 	public void setCaseId(String caseId) {
 		this.caseId = caseId;
 	}
 
 	@Column(name = "case_name")
 	public String getCaseName() {
 		return this.caseName;
 	}
 
 	public void setCaseName(String caseName) {
 		this.caseName = caseName;
 	}
 
 	@Column(name = "case_type")
 	public String getCaseType() {
 		return this.caseType;
 	}
 
 	public void setCaseType(String caseType) {
 		this.caseType = caseType;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "date_modified")
 	public Date getDateModified() {
 		return this.dateModified;
 	}
 
 	public void setDateModified(Date dateModified) {
 		this.dateModified = dateModified;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "server_date_modified")
 	public Date getServerDateModified() {
 		return this.serverDateModified;
 	}
 
 	public void setServerDateModified(Date serverDateModified) {
 		this.serverDateModified = serverDateModified;
 	}
 
     @Column(name = "family_number")
 	public Integer getFamilyNumber() {
 		return this.familyNumber;
 	}
 
 	public void setFamilyNumber(Integer familyNumber) {
 		this.familyNumber = familyNumber;
 	}
 
 	@Column(name = "hh_number")
 	public Integer getHhNumber() {
 		return this.hhNumber;
 	}
 
 	public void setHhNumber(Integer hhNumber) {
 		this.hhNumber = hhNumber;
 	}
 
 	@Column(name = "husband_name")
 	public String getHusbandName() {
 		return this.husbandName;
 	}
 
 	public void setHusbandName(String husbandName) {
 		this.husbandName = husbandName;
 	}
 
     @Column(name = "last_visit_type")
     public String getLastVisitType() {
         return lastVisitType;
     }
 
     public void setLastVisitType(String lastVisitType) {
         this.lastVisitType = lastVisitType;
     }
 
     @Column(name = "mother_alive")
 	public String getMotherAlive() {
 		return this.motherAlive;
 	}
 
 	public void setMotherAlive(String motherAlive) {
 		this.motherAlive = motherAlive;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "mother_dob")
 	public Date getMotherDob() {
 		return this.motherDob;
 	}
 
 	public void setMotherDob(Date motherDob) {
 		this.motherDob = motherDob;
 	}
 
 	@Column(name = "mother_name")
 	public String getMotherName() {
 		return this.motherName;
 	}
 
 	public void setMotherName(String motherName) {
 		this.motherName = motherName;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "closed_on")
 	public Date getClosedOn() {
 		return this.closedOn;
 	}
 
 	public void setClosedOn(Date closedOn) {
 		this.closedOn = closedOn;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "add")
 	public Date getAdd() {
 		return this.add;
 	}
 
 	public void setAdd(Date add) {
 		this.add = add;
 	}
 
 	@Column(name = "age")
 	public Short getAge() {
 		return this.age;
 	}
 
 	public void setAge(Short age) {
 		this.age = age;
 	}
 
 	@Column(name = "birth_place")
 	public String getBirthPlace() {
 		return this.birthPlace;
 	}
 
 	public void setBirthPlace(String birthPlace) {
 		this.birthPlace = birthPlace;
 	}
 
 	@Column(name = "complications")
 	public String getComplications() {
 		return this.complications;
 	}
 
 	public void setComplications(String complications) {
 		this.complications = complications;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_next_bp")
 	public Date getDateNextBp() {
 		return this.dateNextBp;
 	}
 
 	public void setDateNextBp(Date dateNextBp) {
 		this.dateNextBp = dateNextBp;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_next_cf")
 	public Date getDateNextCf() {
 		return this.dateNextCf;
 	}
 
 	public void setDateNextCf(Date dateNextCf) {
 		this.dateNextCf = dateNextCf;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_next_eb")
 	public Date getDateNextEb() {
 		return this.dateNextEb;
 	}
 
 	public void setDateNextEb(Date dateNextEb) {
 		this.dateNextEb = dateNextEb;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_next_pnc")
 	public Date getDateNextPnc() {
 		return this.dateNextPnc;
 	}
 
 	public void setDateNextPnc(Date dateNextPnc) {
 		this.dateNextPnc = dateNextPnc;
 	}
 
 	@Column(name = "eats_meat")
 	public String getEatsMeat() {
 		return this.eatsMeat;
 	}
 
 	public void setEatsMeat(String eatsMeat) {
 		this.eatsMeat = eatsMeat;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "edd")
 	public Date getEdd() {
 		return this.edd;
 	}
 
 	public void setEdd(Date edd) {
 		this.edd = edd;
 	}
 
 	@Column(name = "enrolled_in_kilkari")
 	public String getEnrolledInKilkari() {
 		return this.enrolledInKilkari;
 	}
 
 	public void setEnrolledInKilkari(String enrolledInKilkari) {
 		this.enrolledInKilkari = enrolledInKilkari;
 	}
 
 	@Column(name = "family_planning_type")
 	public String getFamilyPlanningType() {
 		return this.familyPlanningType;
 	}
 
 	public void setFamilyPlanningType(String familyPlanningType) {
 		this.familyPlanningType = familyPlanningType;
 	}
 
 	@Column(name = "how_many_children")
 	public Short getHowManyChildren() {
 		return this.howManyChildren;
 	}
 
 	public void setHowManyChildren(Short howManyChildren) {
 		this.howManyChildren = howManyChildren;
 	}
 
 	@Column(name = "interest_in_kilkari")
 	public String getInterestInKilkari() {
 		return this.interestInKilkari;
 	}
 
 	public void setInterestInKilkari(String interestInKilkari) {
 		this.interestInKilkari = interestInKilkari;
 	}
 
 	@Column(name = "last_preg_tt")
 	public String getLastPregTt() {
 		return this.lastPregTt;
 	}
 
 	public void setLastPregTt(String lastPregTt) {
 		this.lastPregTt = lastPregTt;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "lmp")
 	public Date getLmp() {
 		return this.lmp;
 	}
 
 	public void setLmp(Date lmp) {
 		this.lmp = lmp;
 	}
 
 	@Column(name = "mobile_number")
 	public String getMobileNumber() {
 		return this.mobileNumber;
 	}
 
 	public void setMobileNumber(String mobileNumber) {
 		this.mobileNumber = mobileNumber;
 	}
 
 	@Column(name = "num_boys")
 	public Short getNumBoys() {
 		return this.numBoys;
 	}
 
 	public void setNumBoys(Short numBoys) {
 		this.numBoys = numBoys;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_1")
 	public Date getDateCf1() {
 		return this.dateCf1;
 	}
 
 	public void setDateCf1(Date dateCf1) {
 		this.dateCf1 = dateCf1;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_2")
 	public Date getDateCf2() {
 		return this.dateCf2;
 	}
 
 	public void setDateCf2(Date dateCf2) {
 		this.dateCf2 = dateCf2;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_3")
 	public Date getDateCf3() {
 		return this.dateCf3;
 	}
 
 	public void setDateCf3(Date dateCf3) {
 		this.dateCf3 = dateCf3;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_4")
 	public Date getDateCf4() {
 		return this.dateCf4;
 	}
 
 	public void setDateCf4(Date dateCf4) {
 		this.dateCf4 = dateCf4;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_5")
 	public Date getDateCf5() {
 		return this.dateCf5;
 	}
 
 	public void setDateCf5(Date dateCf5) {
 		this.dateCf5 = dateCf5;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_6")
 	public Date getDateCf6() {
 		return this.dateCf6;
 	}
 
 	public void setDateCf6(Date dateCf6) {
 		this.dateCf6 = dateCf6;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_eb_1")
 	public Date getDateEb1() {
 		return this.dateEb1;
 	}
 
 	public void setDateEb1(Date dateEb1) {
 		this.dateEb1 = dateEb1;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_eb_2")
 	public Date getDateEb2() {
 		return this.dateEb2;
 	}
 
 	public void setDateEb2(Date dateEb2) {
 		this.dateEb2 = dateEb2;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_eb_3")
 	public Date getDateEb3() {
 		return this.dateEb3;
 	}
 
 	public void setDateEb3(Date dateEb3) {
 		this.dateEb3 = dateEb3;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_eb_4")
 	public Date getDateEb4() {
 		return this.dateEb4;
 	}
 
 	public void setDateEb4(Date dateEb4) {
 		this.dateEb4 = dateEb4;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_eb_5")
 	public Date getDateEb5() {
 		return this.dateEb5;
 	}
 
 	public void setDateEb5(Date dateEb5) {
 		this.dateEb5 = dateEb5;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_eb_6")
 	public Date getDateEb6() {
 		return this.dateEb6;
 	}
 
 	public void setDateEb6(Date dateEb6) {
 		this.dateEb6 = dateEb6;
 	}
 
 	@Column(name = "all_pnc_on_time")
 	public String getAllPncOnTime() {
 		return this.allPncOnTime;
 	}
 
 	public void setAllPncOnTime(String allPncOnTime) {
 		this.allPncOnTime = allPncOnTime;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_pnc_1")
 	public Date getDatePnc1() {
 		return this.datePnc1;
 	}
 
 	public void setDatePnc1(Date datePnc1) {
 		this.datePnc1 = datePnc1;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_pnc_2")
 	public Date getDatePnc2() {
 		return this.datePnc2;
 	}
 
 	public void setDatePnc2(Date datePnc2) {
 		this.datePnc2 = datePnc2;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_pnc_3")
 	public Date getDatePnc3() {
 		return this.datePnc3;
 	}
 
 	public void setDatePnc3(Date datePnc3) {
 		this.datePnc3 = datePnc3;
 	}
 
 	@Column(name = "first_pnc_time")
 	public String getFirstPncTime() {
 		return this.firstPncTime;
 	}
 
 	public void setFirstPncTime(String firstPncTime) {
 		this.firstPncTime = firstPncTime;
 	}
 
 	@Column(name = "pnc_1_days_late")
 	public Integer getPnc1DaysLate() {
 		return this.pnc1DaysLate;
 	}
 
 	public void setPnc1DaysLate(Integer pnc1DaysLate) {
 		this.pnc1DaysLate = pnc1DaysLate;
 	}
 
 	@Column(name = "pnc_2_days_late")
 	public Integer getPnc2DaysLate() {
 		return this.pnc2DaysLate;
 	}
 
 	public void setPnc2DaysLate(Integer pnc2DaysLate) {
 		this.pnc2DaysLate = pnc2DaysLate;
 	}
 
 	@Column(name = "pnc_3_days_late")
 	public Integer getPnc3DaysLate() {
 		return this.pnc3DaysLate;
 	}
 
 	public void setPnc3DaysLate(Integer pnc3DaysLate) {
 		this.pnc3DaysLate = pnc3DaysLate;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "tt_booster_date")
 	public Date getTtBoosterDate() {
 		return this.ttBoosterDate;
 	}
 
 	public void setTtBoosterDate(Date ttBoosterDate) {
 		this.ttBoosterDate = ttBoosterDate;
 	}
 
 	@Column(name = "sba")
 	public String getSba() {
 		return this.sba;
 	}
 
 	public void setSba(String sba) {
 		this.sba = sba;
 	}
 
 	@Column(name = "sba_phone")
 	public String getSbaPhone() {
 		return this.sbaPhone;
 	}
 
 	public void setSbaPhone(String sbaPhone) {
 		this.sbaPhone = sbaPhone;
 	}
 
 	@Column(name = "accompany")
 	public String getAccompany() {
 		return this.accompany;
 	}
 
 	public void setAccompany(String accompany) {
 		this.accompany = accompany;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "anc_1_date")
 	public Date getAnc1Date() {
 		return this.anc1Date;
 	}
 
 	public void setAnc1Date(Date anc1Date) {
 		this.anc1Date = anc1Date;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "anc_2_date")
 	public Date getAnc2Date() {
 		return this.anc2Date;
 	}
 
 	public void setAnc2Date(Date anc2Date) {
 		this.anc2Date = anc2Date;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "anc_3_date")
 	public Date getAnc3Date() {
 		return this.anc3Date;
 	}
 
 	public void setAnc3Date(Date anc3Date) {
 		this.anc3Date = anc3Date;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "anc_4_date")
 	public Date getAnc4Date() {
 		return this.anc4Date;
 	}
 
 	public void setAnc4Date(Date anc4Date) {
 		this.anc4Date = anc4Date;
 	}
 
 	@Column(name = "clean_cloth")
 	public String getCleanCloth() {
 		return this.cleanCloth;
 	}
 
 	public void setCleanCloth(String cleanCloth) {
 		this.cleanCloth = cleanCloth;
 	}
 
 	@Column(name = "couple_interested")
 	public String getCoupleInterested() {
 		return this.coupleInterested;
 	}
 
 	public void setCoupleInterested(String coupleInterested) {
 		this.coupleInterested = coupleInterested;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_bp_1")
 	public Date getDateBp1() {
 		return this.dateBp1;
 	}
 
 	public void setDateBp1(Date dateBp1) {
 		this.dateBp1 = dateBp1;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_bp_2")
 	public Date getDateBp2() {
 		return this.dateBp2;
 	}
 
 	public void setDateBp2(Date dateBp2) {
 		this.dateBp2 = dateBp2;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_bp_3")
 	public Date getDateBp3() {
 		return this.dateBp3;
 	}
 
 	public void setDateBp3(Date dateBp3) {
 		this.dateBp3 = dateBp3;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_last_visit")
 	public Date getDateLastVisit() {
 		return this.dateLastVisit;
 	}
 
 	public void setDateLastVisit(Date dateLastVisit) {
 		this.dateLastVisit = dateLastVisit;
 	}
 
 	@Column(name = "delivery_type")
 	public String getDeliveryType() {
 		return this.deliveryType;
 	}
 
 	public void setDeliveryType(String deliveryType) {
 		this.deliveryType = deliveryType;
 	}
 
 	@Column(name = "ifa_tablets")
 	public Short getIfaTablets() {
 		return this.ifaTablets;
 	}
 
 	public void setIfaTablets(Short ifaTablets) {
 		this.ifaTablets = ifaTablets;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "ifa_tablets_100")
 	public Date getIfaTablets100() {
 		return this.ifaTablets100;
 	}
 
 	public void setIfaTablets100(Date ifaTablets100) {
 		this.ifaTablets100 = ifaTablets100;
 	}
 
 	@Column(name = "materials")
 	public String getMaterials() {
 		return this.materials;
 	}
 
 	public void setMaterials(String materials) {
 		this.materials = materials;
 	}
 
 	@Column(name = "maternal_emergency")
 	public String getMaternalEmergency() {
 		return this.maternalEmergency;
 	}
 
 	public void setMaternalEmergency(String maternalEmergency) {
 		this.maternalEmergency = maternalEmergency;
 	}
 
 	@Column(name = "maternal_emergency_number")
 	public String getMaternalEmergencyNumber() {
 		return this.maternalEmergencyNumber;
 	}
 
 	public void setMaternalEmergencyNumber(String maternalEmergencyNumber) {
 		this.maternalEmergencyNumber = maternalEmergencyNumber;
 	}
 
 	@Column(name = "phone_vehicle")
 	public String getPhoneVehicle() {
 		return this.phoneVehicle;
 	}
 
 	public void setPhoneVehicle(String phoneVehicle) {
 		this.phoneVehicle = phoneVehicle;
 	}
 
 	@Column(name = "saving_money")
 	public String getSavingMoney() {
 		return this.savingMoney;
 	}
 
 	public void setSavingMoney(String savingMoney) {
 		this.savingMoney = savingMoney;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "tt_1_date")
 	public Date getTt1Date() {
 		return this.tt1Date;
 	}
 
 	public void setTt1Date(Date tt1Date) {
 		this.tt1Date = tt1Date;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "tt_2_date")
 	public Date getTt2Date() {
 		return this.tt2Date;
 	}
 
 	public void setTt2Date(Date tt2Date) {
 		this.tt2Date = tt2Date;
 	}
 
 	@Column(name = "vehicle")
 	public String getVehicle() {
 		return this.vehicle;
 	}
 
 	public void setVehicle(String vehicle) {
 		this.vehicle = vehicle;
 	}
 
 	@Column(name = "birth_status")
 	public String getBirthStatus() {
 		return this.birthStatus;
 	}
 
 	public void setBirthStatus(String birthStatus) {
 		this.birthStatus = birthStatus;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "migrate_out_date")
 	public Date getMigrateOutDate() {
 		return this.migrateOutDate;
 	}
 
 	public void setMigrateOutDate(Date migrateOutDate) {
 		this.migrateOutDate = migrateOutDate;
 	}
 
 	@Column(name = "migrated_status")
 	public String getMigratedStatus() {
 		return this.migratedStatus;
 	}
 
 	public void setMigratedStatus(String migratedStatus) {
 		this.migratedStatus = migratedStatus;
 	}
 
 	@Column(name = "status")
 	public String getStatus() {
 		return this.status;
 	}
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	@Column(name = "term")
 	public String getTerm() {
 		return this.term;
 	}
 
 	public void setTerm(String term) {
 		this.term = term;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_7")
 	public Date getDateCf7() {
 		return this.dateCf7;
 	}
 
 	public void setDateCf7(Date dateCf7) {
 		this.dateCf7 = dateCf7;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_del_fu")
 	public Date getDateDelFu() {
 		return this.dateDelFu;
 	}
 
 	public void setDateDelFu(Date dateDelFu) {
 		this.dateDelFu = dateDelFu;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_next_reg")
 	public Date getDateNextReg() {
 		return this.dateNextReg;
 	}
 
 	public void setDateNextReg(Date dateNextReg) {
 		this.dateNextReg = dateNextReg;
 	}
 
 	@Column(name = "institutional")
 	public String getInstitutional() {
 		return this.institutional;
 	}
 
 	public void setInstitutional(String institutional) {
 		this.institutional = institutional;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "dob")
 	public Date getDob() {
 		return this.dob;
 	}
 
 	public void setDob(Date dob) {
 		this.dob = dob;
 	}
 
 	@Column(name = "closed")
 	public Boolean getClosed() {
 		return this.closed;
 	}
 
 	public void setClosed(Boolean closed) {
 		this.closed = closed;
 	}
 
     @Column(name = "mobile_number_whose")
     public String getMobileNumberWhose() {
         return this.mobileNumberWhose;
     }
 
     public void setMobileNumberWhose(String mobileNumberWhose) {
         this.mobileNumberWhose = mobileNumberWhose;
     }
     
     @Column(name = "ward_number")
     public int getWardNumber() {
         return this.wardNumber;
     }
 
     public void setWardNumber(int wardNumber) {
         this.wardNumber = wardNumber;
     }
     
     @Column(name = "bp_visit_num")
     public int getBpVisitNum() {
         return this.bpVisitNum;
     }
 
     public void setBpVisitNum(int bpVisitNum) {
         this.bpVisitNum = bpVisitNum;
     }
     
     @Column(name = "eb_visit_num")
     public int getEbVisitNum() {
         return this.ebVisitNum;
     }
 
     public void setEbVisitNum(int ebVisitNum) {
         this.ebVisitNum = ebVisitNum;
     }
     
     @Column(name = "pnc_visit_num")
     public int getPncVisitNum() {
         return this.pncVisitNum;
     }
 
     public void setPncVisitNum(int pncVisitNum) {
         this.pncVisitNum = pncVisitNum;
     }
 
     @Column(name = "cf_visit_num")
     public int getCfVisitNum() {
         return this.cfVisitNum;
     }
 
     public void setCfVisitNum(int cfVisitNum) {
         this.cfVisitNum = cfVisitNum;
     }
     
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "creation_time")
     public Date getCreationTime() {
         return creationTime;
     }
 
     public void setCreationTime(Date creationTime) {
         this.creationTime = creationTime;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "last_modified_time")
     public Date getLastModifiedTime() {
         return lastModifiedTime;
     }
 
     public void setLastModifiedTime(Date lastModifiedTime) {
         this.lastModifiedTime = lastModifiedTime;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "closed_by")
     @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
     public Flw getClosedBy() {
         return closedBy;
     }
 
     public void setClosedBy(Flw closedBy) {
         this.closedBy = closedBy;
     }
 
     @Override
     public void updateToLatest(MotherCase updated) {
         validateIfUpdatable(this.caseId, updated.caseId);
 
         if (!isLatest(updated)) {
            logger.warn(String.format("Ignoring mother case update with case id: %s since existing server date modified is %s and new server date modified is %s", this.caseId, this.serverDateModified, updated.serverDateModified));
             return;
         }
         updateFields(updated, Arrays.asList("id", "caseId", "creationTime", "closedOn", "closedBy", "closed"));
     }
 
     @Override
     public void updateLastModifiedTime() {
         this.lastModifiedTime = new Date();
     }
 
     private boolean isLatest(MotherCase updatedObject) {
         if (this.serverDateModified == null)
             return true;
         else if (updatedObject.serverDateModified == null)
             return false;
         return this.serverDateModified.compareTo(updatedObject.serverDateModified) <= 0;
     }
 }
