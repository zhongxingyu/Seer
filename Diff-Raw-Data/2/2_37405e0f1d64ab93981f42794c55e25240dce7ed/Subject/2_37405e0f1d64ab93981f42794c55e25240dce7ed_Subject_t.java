 package org.pharmgkb;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import org.hibernate.annotations.IndexColumn;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.Type;
 
 import javax.persistence.*;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: whaleyr
  * Date: 8/28/12
  */
 @Entity
 @Table(name="samples")
 public class Subject {
 
   private String m_subjectId;
   private Enum m_Genotyping;
   private Enum m_Phenotyping;
   private Set<Enum> m_SampleSource;
   private String m_Project;
   private Enum m_Gender;
   private String m_Raceself;
   private String m_RaceOMB;
   private String m_Ethnicityreported;
   private String m_EthnicityOMB;
   private String m_Country;
   private Double m_Age;
   private Double m_Height;
   private Double m_Weight;
   private Double m_BMI;
   private String m_Comorbidities;
   private Enum m_Diabetes;
   private Enum m_EverSmoked;
   private Enum m_Currentsmoker;
   private Enum m_Alcohol;
   private Enum m_BloodPressure;
   private Double m_DiastolicBPMax;
   private Double m_DiastolicBPMedian;
   private Double m_SystolicBPMax;
   private Double m_SystolicBPMedian;
   private Double m_CRP;
   private Double m_BUN;
   private Double m_Creatinine;
   private Enum m_Ejectionfraction;
   private Double m_LeftVentricle;
   private Enum m_placeboRCT;
   private Enum m_Clopidogrel;
   private Double m_DoseClopidogrel;
   private Double m_DurationClopidogrel;
   private Enum m_Aspirin;
   private Double m_DoseAspirin;
   private Double m_DurationAspirin;
   private Enum m_Statins;
   private Enum m_PPI;
   private Set<String> m_PPInames;
   private Enum m_Calciumblockers;
   private Enum m_Betablockers;
   private Enum m_ACEInh;
   private Enum m_Anginhblockers;
   private Enum m_Ezetemib;
   private Enum m_GlycoproteinIIaIIIbinhibitor;
   private Double m_Activemetabolite;
   private Enum m_CVevents;
   private Double m_TimeMACE;
   private Enum m_Bleeding;
   private Enum m_MajorBleeding;
   private Enum m_MinorBleeding;
   private Double m_DaysMajorBleeding;
   private Double m_DaysMinorBleeding;
   private Enum m_STEMI;
   private Double m_TimeSTEMI;
   private Enum m_NSTEMI;
   private Double m_TimeNSTEMI;
   private Enum m_Angina;
   private Double m_TimeAngina;
   private Enum m_REVASC;
   private Double m_TimeREVASC;
   private Enum m_Stroke;
   private Double m_Timestroke;
   private Enum m_Otherischemic;
   private Enum m_CongestiveHeartFailure;
   private Double m_TimeheartFailure;
   private Enum m_MechanicalValveReplacement;
   private Double m_TimeMechValve;
   private Enum m_TissueValveReplacement;
   private Double m_TimetissValve;
   private Enum m_Stentthromb;
   private Double m_Timestent;
   private Enum m_Allcausemortality;
   private Double m_Timemortality;
   private Enum m_Cardiovasculardeath;
   private Double m_Timedeath;
   private Enum m_Leftventricularhypertrophy;
   private Double m_TimevenHypertrophy;
   private Enum m_Peripheralvasculardisease;
   private Double m_TimePeriVascular;
   private Enum m_Atrialfibrillation;
   private Double m_TimeAF;
   private Double m_Durationfollowupclinicaloutcomes;
   private Enum m_BloodCell;
   private Double m_Whitecellcount;
   private Double m_Redcellcount;
   private Double m_Plateletcount;
   private Double m_Meanplateletvolume;
   private Double m_Hematocrit;
   private Enum m_Chol;
   private Double m_LDL;
   private Double m_HDL;
   private Double m_TotalCholesterol;
   private Double m_Triglycerides;
   private String m_PlatAggrPheno;
   private String m_Instrument;
   private Double m_Interassayvariation;
   private Double m_Intraassayvariation;
   private String m_Bloodcollectiontype;
   private String m_Sampletype;
   private Enum m_VerifyNowbase;
   private Enum m_VerifyNowpostloading;
   private Enum m_VerifyNowwhileonclopidogrel;
   private Double m_OpticalPlateletAggregometry;
   private Enum m_Preclopidogrelplateletaggregometrybase;
   private Enum m_Postclopidogrelplateletaggregometry;
   private String m_Aggregometryagonist;
   private String m_ADP;
   private String m_Arachadonicacid;
   private String m_Collagen;
   private String m_Plateletaggregometrymethod;
   private Double m_Clopidogrelloadingdose;
   private Double m_PFAmeanEPICollagenclosureBaseline;
   private Double m_PFAmeanADPCollagenclosureBaseline;
   private Double m_PFAmeanEPICollagenclosurePost;
   private Double m_PFAmeanADPCollagenclosurePost;
   private Double m_TimeLoadingPFA;
   private Double m_PFAmeanEPICollagenclosureStandard;
   private Double m_PFAmeanADPCollagenclosureStandard;
   private Double m_VerifyNowbaselineBase;
   private Double m_VerifyNowbaselinePRU;
   private Double m_VerifyNowbaselinepercentinhibition;
   private Double m_VerifyNowpostBase;
   private Double m_VerifyNowpostPRU;
   private Double m_VerifyNowpostpercentinhibition;
   private Double m_TimeloadingVerifyNow;
   private Double m_VerifyNowonclopidogrelBase;
   private Double m_VerifyNowonclopidogrelPRU;
   private Double m_VerifyNowonclopidogrelpercentinhibition;
   private Double m_PAP8baselinemaxADP2;
   private Double m_PAP8baselinemaxADP5;
   private Double m_PAP8baselinemaxADP10;
   private Double m_PAP8baselinemaxADP20;
   private Double m_PAP8baselinemaxcollagen1;
   private Double m_PAP8baselinemaxcollagen2;
   private Double m_PAP8baselinemaxcollagen10;
   private Double m_PAP8baselinemaxcollagen6;
   private Double m_PAP8baselinemaxepi;
   private Double m_PAP8baselinemaxaa;
   private Double m_PAP8baselinelagcollagen1;
   private Double m_PAP8baselinelagcollagen2;
   private Double m_PAP8baselinelagcollagen5;
   private Double m_PAP8baselinelagcollagen10;
   private Double m_PAP8postmaxADP2;
   private Double m_PAP8postmaxADP5;
   private Double m_PAP8postmaxADP10;
   private Double m_PAP8postmaxADP20;
   private Double m_PAP8postmaxcollagen1;
   private Double m_PAP8postmaxcollagen2;
   private Double m_PAP8postmaxcollagen5;
   private Double m_PAP8postmaxcollagen10;
   private Double m_PAP8postmaxepiperc;
   private Double m_PAP8postmaxaaperc;
   private Double m_PAP8postlagcollagen1;
   private Double m_PAP8postlagcollagen2;
   private Double m_PAP8postlagcollagen5;
   private Double m_PAP8postlagcollagen10;
   private Double m_TimeloadingPAP8;
   private Double m_PAP8standardmaxADP2;
   private Double m_PAP8standardmaxADP5;
   private Double m_PAP8standardmaxADP10;
   private Double m_PAP8standardmaxADP20;
   private Double m_PAP8standardmaxcollagen1;
   private Double m_PAP8standardmaxcollagen2;
   private Double m_PAP8standardmaxcollagen5;
   private Double m_PAP8standardmaxcollagen10;
   private Double m_PAP8standardmaxepipct;
   private Double m_PAP8standardmaxaapct;
   private Double m_PAP8standardlagcollagen1;
   private Double m_PAP8standardlagcollagen2;
   private Double m_5PAP8standardlagcollagen5;
   private Double m_PAP8standardlagcollagen10;
   private Double m_ChronologbaselinemaxADP5;
   private Double m_ChronologbaselinemaxADP20;
   private Double m_Chronologbaselinemaxaa;
   private Double m_Chronologbaselinemaxcollagen1;
   private Double m_ChronologbaselinelagADP5;
   private Double m_ChronologbaselinelagADP20;
   private Double m_Chronologbaselinelagaa;
   private Double m_Chronologbaselinelagcollagen1;
   private Double m_ChronologloadingmaxADP5;
   private Double m_ChronologloadingmaxADP20;
   private Double m_Chronologloadingmaxaa;
   private Double m_Chronologloadingmaxcollagen1;
   private Double m_ChronologloadinglagADP5;
   private Double m_ChronologloadinglagADP20;
   private Double m_Chronologloadinglagaa;
   private Double m_Chronologloadinglagcollagen1;
   private Double m_TimeloadingChronolog;
   private Double m_ChronologstandardmaxADP5;
   private Double m_ChronologstandardmaxADP20;
   private Double m_Chronologstandardmaxaa;
   private Double m_Chronologstandardmaxcollagen1;
   private Double m_ChronologstandardlagADP5;
   private Double m_ChronologstandardlagADP20;
   private Double m_Chronologstandardlagaa;
   private Double m_Chronologstandardlagcollagen1;
   private Double m_VASP;
   private String m_AdditionPheno;
   private List<String> m_cyp2c19genotypes;
   private String m_rs4244285;
   private String m_rs4986893;
   private String m_rs28399504;
   private String m_rs56337013;
   private String m_rs72552267;
   private String m_rs72558186;
   private String m_rs41291556;
   private String m_rs6413438;
   private String m_rs12248560;
   private String m_rs662;
   private String m_rs854560;
   private String m_rs1045642;
   private String m_othergenotypes;
   private String m_rs4803418;
   private String m_rs48034189;
   private String m_rs8192719;
   private String m_rs3745274;
   private Double m_absWhiteOnPlavix;
   private Double m_redOnPlavix;
   private Double m_plateletOnPlavix;
   private Double m_meanPlateletVolOnPlavix;
   private Double m_hematocritOnPlavix;
 
   @Id
   @Column(name="Subject_ID")
   public String getSubjectId() {
     return m_subjectId;
   }
 
   public void setSubjectId(String subjectId) {
     m_subjectId = subjectId;
   }
 
   public String toString() {
     return "Subject "+getSubjectId();
   }
 
   @Column(name="Genotyping")
   @Type(type="valueType")
   public Enum getGenotyping() {
     return m_Genotyping;
   }
 
   public void setGenotyping(Enum genotyping) {
     m_Genotyping = genotyping;
   }
 
   @Column(name="Phenotyping")
   @Type(type="valueType")
   public Enum getPhenotyping() {
     return m_Phenotyping;
   }
 
   public void setPhenotyping(Enum phenotyping) {
     m_Phenotyping = phenotyping;
   }
 
   @ElementCollection
   @JoinTable(name="sampleSources", joinColumns = @JoinColumn(name="subject_id"))
   @Column(name="source")
   @Type(type="sampleSourceType")
   public Set<Enum> getSampleSource() {
     return m_SampleSource;
   }
 
   public void setSampleSource(Set<Enum> sampleSource) {
     m_SampleSource = sampleSource;
   }
 
   public void addSampleSource(Enum sampleSource) {
     if (m_SampleSource == null) {
       m_SampleSource = Sets.newHashSet();
     }
     m_SampleSource.add(sampleSource);
   }
 
   @Column(name="Project",nullable = false)
   public String getProject() {
     return m_Project;
   }
 
   public void setProject(String project) {
     m_Project = project;
   }
 
   @Column(name="Gender")
   @Type(type="genderType")
   public Enum getGender() {
     return m_Gender;
   }
 
   public void setGender(Enum gender) {
     m_Gender = gender;
   }
 
   @Column(name="Race_self")
   public String getRaceself() {
     return m_Raceself;
   }
 
   public void setRaceself(String raceself) {
     m_Raceself = raceself;
   }
 
   @Column(name="Race_OMB")
   public String getRaceOMB() {
     return m_RaceOMB;
   }
 
   public void setRaceOMB(String raceOMB) {
     m_RaceOMB = raceOMB;
   }
 
   @Column(name="Ethnicity_reported")
   public String getEthnicityreported() {
     return m_Ethnicityreported;
   }
 
   public void setEthnicityreported(String ethnicityreported) {
     m_Ethnicityreported = ethnicityreported;
   }
 
   @Column(name="Ethnicity_OMB")
   public String getEthnicityOMB() {
     return m_EthnicityOMB;
   }
 
   public void setEthnicityOMB(String ethnicityOMB) {
     m_EthnicityOMB = ethnicityOMB;
   }
 
   @Column(name="Country")
   public String getCountry() {
     return m_Country;
   }
 
   public void setCountry(String country) {
     m_Country = country;
   }
 
   @Column(name="Age")
   public Double getAge() {
     return m_Age;
   }
 
   public void setAge(Double age) {
     m_Age = age;
   }
 
   @Column(name="Height")
   public Double getHeight() {
     return m_Height;
   }
 
   public void setHeight(Double height) {
     m_Height = height;
   }
 
   @Column(name="weight")
   public Double getWeight() {
     return m_Weight;
   }
 
   public void setWeight(Double weight) {
     m_Weight = weight;
   }
 
   @Column(name="BMI")
   public Double getBMI() {
     return m_BMI;
   }
 
   public void setBMI(Double BMI) {
     m_BMI = BMI;
   }
 
   @Column(name="Comorbidities")
   public String getComorbidities() {
     return m_Comorbidities;
   }
 
   public void setComorbidities(String comorbidities) {
     m_Comorbidities = comorbidities;
   }
 
   @Column(name="Diabetes")
   @Type(type="diabetesStatusType")
   public Enum getDiabetes() {
     return m_Diabetes;
   }
 
   public void setDiabetes(Enum diabetes) {
     m_Diabetes = diabetes;
   }
 
   @Column(name="Ever_Smoked")
   @Type(type="valueType")
   public Enum getEverSmoked() {
     return m_EverSmoked;
   }
 
   public void setEverSmoked(Enum everSmoked) {
     m_EverSmoked = everSmoked;
   }
 
   @Column(name="Current_smoker")
   @Type(type="valueType")
   public Enum getCurrentsmoker() {
     return m_Currentsmoker;
   }
 
   public void setCurrentsmoker(Enum currentsmoker) {
     m_Currentsmoker = currentsmoker;
   }
 
   @Column(name="Alcohol")
   @Type(type="alcoholStatusType")
   public Enum getAlcohol() {
     return m_Alcohol;
   }
 
   public void setAlcohol(Enum alcohol) {
     m_Alcohol = alcohol;
   }
 
   @Column(name="Blood_Pressure")
   @Type(type="valueType")
   public Enum getBloodPressure() {
     return m_BloodPressure;
   }
 
   public void setBloodPressure(Enum bloodPressure) {
     m_BloodPressure = bloodPressure;
   }
 
   @Column(name="diastolic_bp_max")
   public Double getDiastolicBPMax() {
     return m_DiastolicBPMax;
   }
 
   public void setDiastolicBPMax(Double diastolicBPMax) {
     m_DiastolicBPMax = diastolicBPMax;
   }
 
   @Column(name="diastolic_bp_median")
   public Double getDiastolicBPMedian() {
     return m_DiastolicBPMedian;
   }
 
   public void setDiastolicBPMedian(Double diastolicBPMedian) {
     m_DiastolicBPMedian = diastolicBPMedian;
   }
 
   @Column(name="systolic_bp_max")
   public Double getSystolicBPMax() {
     return m_SystolicBPMax;
   }
 
   public void setSystolicBPMax(Double systolicBPMax) {
     m_SystolicBPMax = systolicBPMax;
   }
 
   @Column(name="systolic_bp_median")
   public Double getSystolicBPMedian() {
     return m_SystolicBPMedian;
   }
 
   public void setSystolicBPMedian(Double systolicBPMedian) {
     m_SystolicBPMedian = systolicBPMedian;
   }
 
   @Column(name="crp")
   public Double getCRP() {
     return m_CRP;
   }
 
   public void setCRP(Double CRP) {
     m_CRP = CRP;
   }
 
   @Column(name="bun")
   public Double getBUN() {
     return m_BUN;
   }
 
   public void setBUN(Double BUN) {
     m_BUN = BUN;
   }
 
   @Column(name="creatinine")
   public Double getCreatinine() {
     return m_Creatinine;
   }
 
   public void setCreatinine(Double creatinine) {
     m_Creatinine = creatinine;
   }
 
   @Column(name="ejection_fraction")
   @Type(type="valueType")
   public Enum getEjectionfraction() {
     return m_Ejectionfraction;
   }
 
   public void setEjectionfraction(Enum ejectionfraction) {
     m_Ejectionfraction = ejectionfraction;
   }
 
   @Column(name="left_ventricle")
   public Double getLeftVentricle() {
     return m_LeftVentricle;
   }
 
   public void setLeftVentricle(Double leftVentricle) {
     m_LeftVentricle = leftVentricle;
   }
 
   @Column(name="placebo_RCT")
   @Type(type="valueType")
   public Enum getPlaceboRCT() {
     return m_placeboRCT;
   }
 
   public void setPlaceboRCT(Enum placeboRCT) {
     m_placeboRCT = placeboRCT;
   }
 
   @Column(name="clopidogrel")
   @Type(type="valueType")
   public Enum getClopidogrel() {
     return m_Clopidogrel;
   }
 
   public void setClopidogrel(Enum clopidogrel) {
     m_Clopidogrel = clopidogrel;
   }
 
   @Column(name="dose_clopidogrel")
   public Double getDoseClopidogrel() {
     return m_DoseClopidogrel;
   }
 
   public void setDoseClopidogrel(Double doseClopidogrel) {
     m_DoseClopidogrel = doseClopidogrel;
   }
 
   @Column(name="duration_clopidogrel")
   public Double getDurationClopidogrel() {
     return m_DurationClopidogrel;
   }
 
   public void setDurationClopidogrel(Double durationClopidogrel) {
     m_DurationClopidogrel = durationClopidogrel;
   }
 
   @Column(name="aspirin")
   @Type(type="valueType")
   public Enum getAspirin() {
     return m_Aspirin;
   }
 
   public void setAspirin(Enum aspirin) {
     m_Aspirin = aspirin;
   }
 
   @Column(name="dose_aspirin")
   public Double getDoseAspirin() {
     return m_DoseAspirin;
   }
 
   public void setDoseAspirin(Double doseAspirin) {
     m_DoseAspirin = doseAspirin;
   }
 
   @Column(name="duration_aspirin")
   public Double getDurationAspirin() {
     return m_DurationAspirin;
   }
 
   public void setDurationAspirin(Double durationAspirin) {
     m_DurationAspirin = durationAspirin;
   }
 
   @Column(name="statins")
   @Type(type="valueType")
   public Enum getStatins() {
     return m_Statins;
   }
 
   public void setStatins(Enum statins) {
     m_Statins = statins;
   }
 
   @Column(name="ppi")
   @Type(type="valueType")
   public Enum getPPI() {
     return m_PPI;
   }
 
   public void setPPI(Enum PPI) {
     m_PPI = PPI;
   }
 
   @ElementCollection
   @JoinTable(name="samplePpiNames", joinColumns = @JoinColumn(name="subject_id"))
   @Column(name="ppiName")
   public Set<String> getPPInames() {
     return m_PPInames;
   }
 
   public void setPPInames(Set<String> PPInames) {
     m_PPInames = PPInames;
   }
 
   public void addPPIname(String PPIname) {
     if (m_PPInames == null) {
       m_PPInames = Sets.newHashSet();
     }
     m_PPInames.add(PPIname);
   }
 
   @Column(name="calcium_blockers")
   @Type(type="valueType")
   public Enum getCalciumblockers() {
     return m_Calciumblockers;
   }
 
   public void setCalciumblockers(Enum calciumblockers) {
     m_Calciumblockers = calciumblockers;
   }
 
   @Column(name="beta_blockers")
   @Type(type="valueType")
   public Enum getBetablockers() {
     return m_Betablockers;
   }
 
   public void setBetablockers(Enum betablockers) {
     m_Betablockers = betablockers;
   }
 
   @Column(name="ace_inh")
   @Type(type="valueType")
   public Enum getACEInh() {
     return m_ACEInh;
   }
 
   public void setACEInh(Enum ACEInh) {
     m_ACEInh = ACEInh;
   }
 
   @Column(name="Ang_inh_blockers")
   @Type(type="valueType")
   public Enum getAnginhblockers() {
     return m_Anginhblockers;
   }
 
   public void setAnginhblockers(Enum anginhblockers) {
     m_Anginhblockers = anginhblockers;
   }
 
   @Column(name="ezetemib")
   @Type(type="valueType")
   public Enum getEzetemib() {
     return m_Ezetemib;
   }
 
   public void setEzetemib(Enum ezetemib) {
     m_Ezetemib = ezetemib;
   }
 
   @Column(name="glycoprotein_iiaiiib_inhibitor")
   @Type(type="valueType")
   public Enum getGlycoproteinIIaIIIbinhibitor() {
     return m_GlycoproteinIIaIIIbinhibitor;
   }
 
   public void setGlycoproteinIIaIIIbinhibitor(Enum glycoproteinIIaIIIbinhibitor) {
     m_GlycoproteinIIaIIIbinhibitor = glycoproteinIIaIIIbinhibitor;
   }
 
   @Column(name="active_metabolite")
   public Double getActivemetabolite() {
     return m_Activemetabolite;
   }
 
   public void setActivemetabolite(Double activemetabolite) {
     m_Activemetabolite = activemetabolite;
   }
 
   @Column(name="cv_events")
   @Type(type="valueType")
   public Enum getCVevents() {
     return m_CVevents;
   }
 
   public void setCVevents(Enum CVevents) {
     m_CVevents = CVevents;
   }
 
   @Column(name="time_mace")
   public Double getTimeMACE() {
     return m_TimeMACE;
   }
 
   public void setTimeMACE(Double timeMACE) {
     m_TimeMACE = timeMACE;
   }
 
   @Column(name="bleeding")
   @Type(type="valueType")
   public Enum getBleeding() {
     return m_Bleeding;
   }
 
   public void setBleeding(Enum bleeding) {
     m_Bleeding = bleeding;
   }
 
   @Column(name="major_bleeding")
   @Type(type="valueType")
   public Enum getMajorBleeding() {
     return m_MajorBleeding;
   }
 
   public void setMajorBleeding(Enum majorBleeding) {
     m_MajorBleeding = majorBleeding;
   }
 
   @Column(name="minor_bleeding")
   @Type(type="valueType")
   public Enum getMinorBleeding() {
     return m_MinorBleeding;
   }
 
   public void setMinorBleeding(Enum minorBleeding) {
     m_MinorBleeding = minorBleeding;
   }
 
   @Column(name="days_majorbleeding")
   public Double getDaysMajorBleeding() {
     return m_DaysMajorBleeding;
   }
 
   public void setDaysMajorBleeding(Double daysMajorBleeding) {
     m_DaysMajorBleeding = daysMajorBleeding;
   }
 
   @Column(name="days_minorbleeding")
   public Double getDaysMinorBleeding() {
     return m_DaysMinorBleeding;
   }
 
   public void setDaysMinorBleeding(Double daysMinorBleeding) {
     m_DaysMinorBleeding = daysMinorBleeding;
   }
 
   @Column(name="stemi")
   @Type(type="valueType")
   public Enum getSTEMI() {
     return m_STEMI;
   }
 
   public void setSTEMI(Enum STEMI) {
     m_STEMI = STEMI;
   }
 
   @Column(name="time_stemi")
   public Double getTimeSTEMI() {
     return m_TimeSTEMI;
   }
 
   public void setTimeSTEMI(Double timeSTEMI) {
     m_TimeSTEMI = timeSTEMI;
   }
 
   @Column(name="nstemi")
   @Type(type="valueType")
   public Enum getNSTEMI() {
     return m_NSTEMI;
   }
 
   public void setNSTEMI(Enum NSTEMI) {
     m_NSTEMI = NSTEMI;
   }
 
   @Column(name="time_nstemi")
   public Double getTimeNSTEMI() {
     return m_TimeNSTEMI;
   }
 
   public void setTimeNSTEMI(Double timeNSTEMI) {
     m_TimeNSTEMI = timeNSTEMI;
   }
 
   @Column(name="angina")
   @Type(type="valueType")
   public Enum getAngina() {
     return m_Angina;
   }
 
   public void setAngina(Enum angina) {
     m_Angina = angina;
   }
 
   @Column(name="time_angina")
   public Double getTimeAngina() {
     return m_TimeAngina;
   }
 
   public void setTimeAngina(Double timeAngina) {
     m_TimeAngina = timeAngina;
   }
 
   @Column(name="revasc")
   @Type(type="valueType")
   public Enum getREVASC() {
     return m_REVASC;
   }
 
   public void setREVASC(Enum REVASC) {
     m_REVASC = REVASC;
   }
 
   @Column(name="time_revasc")
   public Double getTimeREVASC() {
     return m_TimeREVASC;
   }
 
   public void setTimeREVASC(Double timeREVASC) {
     m_TimeREVASC = timeREVASC;
   }
 
   @Column(name="stroke")
   @Type(type="valueType")
   public Enum getStroke() {
     return m_Stroke;
   }
 
   public void setStroke(Enum stroke) {
     m_Stroke = stroke;
   }
 
   @Column(name="time_stroke")
   public Double getTimestroke() {
     return m_Timestroke;
   }
 
   public void setTimestroke(Double timestroke) {
     m_Timestroke = timestroke;
   }
 
   @Column(name="other_ischemic")
   @Type(type="valueType")
   public Enum getOtherischemic() {
     return m_Otherischemic;
   }
 
   public void setOtherischemic(Enum otherischemic) {
     m_Otherischemic = otherischemic;
   }
 
   @Column(name="congestive_heart_failure")
   @Type(type="valueType")
   public Enum getCongestiveHeartFailure() {
     return m_CongestiveHeartFailure;
   }
 
   public void setCongestiveHeartFailure(Enum congestiveHeartFailure) {
     m_CongestiveHeartFailure = congestiveHeartFailure;
   }
 
   @Column(name="time_heartfailure")
   public Double getTimeheartFailure() {
     return m_TimeheartFailure;
   }
 
   public void setTimeheartFailure(Double timeheartFailure) {
     m_TimeheartFailure = timeheartFailure;
   }
 
   @Column(name="mechanical_valve_replacement")
   @Type(type="valueType")
   public Enum getMechanicalValveReplacement() {
     return m_MechanicalValveReplacement;
   }
 
   public void setMechanicalValveReplacement(Enum mechanicalValveReplacement) {
     m_MechanicalValveReplacement = mechanicalValveReplacement;
   }
 
   @Column(name="time_mechvalve")
   public Double getTimeMechValve() {
     return m_TimeMechValve;
   }
 
   public void setTimeMechValve(Double timeMechValve) {
     m_TimeMechValve = timeMechValve;
   }
 
   @Column(name="tissue_valve_replacement")
   @Type(type="valueType")
   public Enum getTissueValveReplacement() {
     return m_TissueValveReplacement;
   }
 
   public void setTissueValveReplacement(Enum tissueValveReplacement) {
     m_TissueValveReplacement = tissueValveReplacement;
   }
 
   @Column(name="time_tissvalve")
   public Double getTimetissValve() {
     return m_TimetissValve;
   }
 
   public void setTimetissValve(Double timetissValve) {
     m_TimetissValve = timetissValve;
   }
 
   @Column(name="stent_thromb")
   @Type(type="valueType")
   public Enum getStentthromb() {
     return m_Stentthromb;
   }
 
   public void setStentthromb(Enum stentthromb) {
     m_Stentthromb = stentthromb;
   }
 
   @Column(name="time_stent")
   public Double getTimestent() {
     return m_Timestent;
   }
 
   public void setTimestent(Double timestent) {
     m_Timestent = timestent;
   }
 
   @Column(name="all_cause_mortality")
   @Type(type="valueType")
   public Enum getAllcausemortality() {
     return m_Allcausemortality;
   }
 
   public void setAllcausemortality(Enum allcausemortality) {
     m_Allcausemortality = allcausemortality;
   }
 
   @Column(name="time_mortality")
   public Double getTimemortality() {
     return m_Timemortality;
   }
 
   public void setTimemortality(Double timemortality) {
     m_Timemortality = timemortality;
   }
 
   @Column(name="cardiovascular_death")
   @Type(type="valueType")
   public Enum getCardiovasculardeath() {
     return m_Cardiovasculardeath;
   }
 
   public void setCardiovasculardeath(Enum cardiovasculardeath) {
     m_Cardiovasculardeath = cardiovasculardeath;
   }
 
   @Column(name="time_death")
   public Double getTimedeath() {
     return m_Timedeath;
   }
 
   public void setTimedeath(Double timedeath) {
     m_Timedeath = timedeath;
   }
 
   @Column(name="left_ventricular_hypertrophy")
   @Type(type="valueType")
  public Enum  getLeftventricularhypertrophy() {
     return m_Leftventricularhypertrophy;
   }
 
   public void setLeftventricularhypertrophy(Enum leftventricularhypertrophy) {
     m_Leftventricularhypertrophy = leftventricularhypertrophy;
   }
 
   @Column(name="time_venhypertrophy")
   public Double getTimevenHypertrophy() {
     return m_TimevenHypertrophy;
   }
 
   public void setTimevenHypertrophy(Double timevenHypertrophy) {
     m_TimevenHypertrophy = timevenHypertrophy;
   }
 
   @Column(name="peripheral_vascular_disease")
   @Type(type="valueType")
  public Enum  getPeripheralvasculardisease() {
     return m_Peripheralvasculardisease;
   }
 
   public void setPeripheralvasculardisease(Enum peripheralvasculardisease) {
     m_Peripheralvasculardisease = peripheralvasculardisease;
   }
 
   @Column(name="time_perivascular")
   public Double getTimePeriVascular() {
     return m_TimePeriVascular;
   }
 
   public void setTimePeriVascular(Double timePeriVascular) {
     m_TimePeriVascular = timePeriVascular;
   }
 
   @Column(name="atrial_fibrillation")
   @Type(type="valueType")
  public Enum  getAtrialfibrillation() {
     return m_Atrialfibrillation;
   }
 
   public void setAtrialfibrillation(Enum atrialfibrillation) {
     m_Atrialfibrillation = atrialfibrillation;
   }
 
   @Column(name="time_af")
   public Double getTimeAF() {
     return m_TimeAF;
   }
 
   public void setTimeAF(Double timeAF) {
     m_TimeAF = timeAF;
   }
 
   @Column(name="duration_followup_clinical_outcomes")
   public Double getDurationfollowupclinicaloutcomes() {
     return m_Durationfollowupclinicaloutcomes;
   }
 
   public void setDurationfollowupclinicaloutcomes(Double durationfollowupclinicaloutcomes) {
     m_Durationfollowupclinicaloutcomes = durationfollowupclinicaloutcomes;
   }
 
   @Column(name="blood_cell")
   @Type(type="valueType")
  public Enum  getBloodCell() {
     return m_BloodCell;
   }
 
   public void setBloodCell(Enum bloodCell) {
     m_BloodCell = bloodCell;
   }
 
   @Column(name="white_cell_count")
   public Double getWhitecellcount() {
     return m_Whitecellcount;
   }
 
   public void setWhitecellcount(Double whitecellcount) {
     m_Whitecellcount = whitecellcount;
   }
 
   @Column(name="red_cell_count")
   public Double getRedcellcount() {
     return m_Redcellcount;
   }
 
   public void setRedcellcount(Double redcellcount) {
     m_Redcellcount = redcellcount;
   }
 
   @Column(name="platelet_count")
   public Double getPlateletcount() {
     return m_Plateletcount;
   }
 
   public void setPlateletcount(Double plateletcount) {
     m_Plateletcount = plateletcount;
   }
 
   @Column(name="mean_platelet_volume")
   public Double getMeanplateletvolume() {
     return m_Meanplateletvolume;
   }
 
   public void setMeanplateletvolume(Double meanplateletvolume) {
     m_Meanplateletvolume = meanplateletvolume;
   }
 
   @Column(name="hematocrit")
   public Double getHematocrit() {
     return m_Hematocrit;
   }
 
   public void setHematocrit(Double hematocrit) {
     m_Hematocrit = hematocrit;
   }
 
   @Column(name="chol")
   @Type(type="valueType")
  public Enum  getChol() {
     return m_Chol;
   }
 
   public void setChol(Enum chol) {
     m_Chol = chol;
   }
 
   @Column(name="ldl")
   public Double getLDL() {
     return m_LDL;
   }
 
   public void setLDL(Double LDL) {
     m_LDL = LDL;
   }
 
   @Column(name="hdl")
   public Double getHDL() {
     return m_HDL;
   }
 
   public void setHDL(Double HDL) {
     m_HDL = HDL;
   }
 
   @Column(name="total_cholesterol")
   public Double getTotalCholesterol() {
     return m_TotalCholesterol;
   }
 
   public void setTotalCholesterol(Double totalCholesterol) {
     m_TotalCholesterol = totalCholesterol;
   }
 
   @Column(name="triglycerides")
   public Double getTriglycerides() {
     return m_Triglycerides;
   }
 
   public void setTriglycerides(Double triglycerides) {
     m_Triglycerides = triglycerides;
   }
 
   @Column(name="Plat_Aggr_Pheno")
   public String getPlatAggrPheno() {
     return m_PlatAggrPheno;
   }
 
   public void setPlatAggrPheno(String platAggrPheno) {
     m_PlatAggrPheno = platAggrPheno;
   }
 
   @Column(name="instrument")
   public String getInstrument() {
     return m_Instrument;
   }
 
   public void setInstrument(String instrument) {
     m_Instrument = instrument;
   }
 
   @Column(name="Inter_assay_variation")
   public Double getInterassayvariation() {
     return m_Interassayvariation;
   }
 
   public void setInterassayvariation(Double interassayvariation) {
     m_Interassayvariation = interassayvariation;
   }
 
   @Column(name="intra_assay_variation")
   public Double getIntraassayvariation() {
     return m_Intraassayvariation;
   }
 
   public void setIntraassayvariation(Double intraassayvariation) {
     m_Intraassayvariation = intraassayvariation;
   }
 
   @Column(name="blood_collection_type")
   public String getBloodcollectiontype() {
     return m_Bloodcollectiontype;
   }
 
   public void setBloodcollectiontype(String bloodcollectiontype) {
     m_Bloodcollectiontype = bloodcollectiontype;
   }
 
   @Column(name="sample_type")
   public String getSampletype() {
     return m_Sampletype;
   }
 
   public void setSampletype(String sampletype) {
     m_Sampletype = sampletype;
   }
 
   @Column(name="verify_now_base")
   @Type(type="valueType")
  public Enum  getVerifyNowbase() {
     return m_VerifyNowbase;
   }
 
   public void setVerifyNowbase(Enum verifyNowbase) {
     m_VerifyNowbase = verifyNowbase;
   }
 
   @Column(name="verify_now_post_loading")
   @Type(type="valueType")
  public Enum  getVerifyNowpostloading() {
     return m_VerifyNowpostloading;
   }
 
   public void setVerifyNowpostloading(Enum verifyNowpostloading) {
     m_VerifyNowpostloading = verifyNowpostloading;
   }
 
   @Column(name="verify_now_while_on_clopidogrel")
   @Type(type="valueType")
  public Enum  getVerifyNowwhileonclopidogrel() {
     return m_VerifyNowwhileonclopidogrel;
   }
 
   public void setVerifyNowwhileonclopidogrel(Enum verifyNowwhileonclopidogrel) {
     m_VerifyNowwhileonclopidogrel = verifyNowwhileonclopidogrel;
   }
 
   @Column(name="optical_platelet_aggregometry")
   public Double getOpticalPlateletAggregometry() {
     return m_OpticalPlateletAggregometry;
   }
 
   public void setOpticalPlateletAggregometry(Double opticalPlateletAggregometry) {
     m_OpticalPlateletAggregometry = opticalPlateletAggregometry;
   }
 
   @Column(name="Pre_clopidogrel_platelet_aggregometry_base")
   @Type(type="valueType")
  public Enum  getPreclopidogrelplateletaggregometrybase() {
     return m_Preclopidogrelplateletaggregometrybase;
   }
 
   public void setPreclopidogrelplateletaggregometrybase(Enum preclopidogrelplateletaggregometrybase) {
     m_Preclopidogrelplateletaggregometrybase = preclopidogrelplateletaggregometrybase;
   }
 
   @Column(name="Post_clopidogrel_platelet_aggregometry")
   @Type(type="valueType")
  public Enum  getPostclopidogrelplateletaggregometry() {
     return m_Postclopidogrelplateletaggregometry;
   }
 
   public void setPostclopidogrelplateletaggregometry(Enum postclopidogrelplateletaggregometry) {
     m_Postclopidogrelplateletaggregometry = postclopidogrelplateletaggregometry;
   }
 
   @Column(name="aggregometry_agonist")
   public String getAggregometryagonist() {
     return m_Aggregometryagonist;
   }
 
   public void setAggregometryagonist(String aggregometryagonist) {
     m_Aggregometryagonist = aggregometryagonist;
   }
 
   @Column(name="adp")
   public String getADP() {
     return m_ADP;
   }
 
   public void setADP(String ADP) {
     m_ADP = ADP;
   }
 
   @Column(name="arachadonic_acid")
   public String getArachadonicacid() {
     return m_Arachadonicacid;
   }
 
   public void setArachadonicacid(String arachadonicacid) {
     m_Arachadonicacid = arachadonicacid;
   }
 
   @Column(name="collagen")
   public String getCollagen() {
     return m_Collagen;
   }
 
   public void setCollagen(String collagen) {
     m_Collagen = collagen;
   }
 
   @Column(name="platelet_aggregometry_method")
   public String getPlateletaggregometrymethod() {
     return m_Plateletaggregometrymethod;
   }
 
   public void setPlateletaggregometrymethod(String plateletaggregometrymethod) {
     m_Plateletaggregometrymethod = plateletaggregometrymethod;
   }
 
   @Column(name="clopidogrel_loading_dose")
   public Double getClopidogrelloadingdose() {
     return m_Clopidogrelloadingdose;
   }
 
   public void setClopidogrelloadingdose(Double clopidogrelloadingdose) {
     m_Clopidogrelloadingdose = clopidogrelloadingdose;
   }
 
   @Transient
   public Double getPFAmeanEPICollagenclosureBaseline() {
     return m_PFAmeanEPICollagenclosureBaseline;
   }
 
   public void setPFAmeanEPICollagenclosureBaseline(Double PFAmeanEPICollagenclosureBaseline) {
     m_PFAmeanEPICollagenclosureBaseline = PFAmeanEPICollagenclosureBaseline;
   }
 
   @Transient
   public Double getPFAmeanADPCollagenclosureBaseline() {
     return m_PFAmeanADPCollagenclosureBaseline;
   }
 
   public void setPFAmeanADPCollagenclosureBaseline(Double PFAmeanADPCollagenclosureBaseline) {
     m_PFAmeanADPCollagenclosureBaseline = PFAmeanADPCollagenclosureBaseline;
   }
 
   @Transient
   public Double getPFAmeanEPICollagenclosurePost() {
     return m_PFAmeanEPICollagenclosurePost;
   }
 
   public void setPFAmeanEPICollagenclosurePost(Double PFAmeanEPICollagenclosurePost) {
     m_PFAmeanEPICollagenclosurePost = PFAmeanEPICollagenclosurePost;
   }
 
   @Transient
   public Double getPFAmeanADPCollagenclosurePost() {
     return m_PFAmeanADPCollagenclosurePost;
   }
 
   public void setPFAmeanADPCollagenclosurePost(Double PFAmeanADPCollagenclosurePost) {
     m_PFAmeanADPCollagenclosurePost = PFAmeanADPCollagenclosurePost;
   }
 
   @Transient
   public Double getTimeLoadingPFA() {
     return m_TimeLoadingPFA;
   }
 
   public void setTimeLoadingPFA(Double timeLoadingPFA) {
     m_TimeLoadingPFA = timeLoadingPFA;
   }
 
   @Transient
   public Double getPFAmeanEPICollagenclosureStandard() {
     return m_PFAmeanEPICollagenclosureStandard;
   }
 
   public void setPFAmeanEPICollagenclosureStandard(Double PFAmeanEPICollagenclosureStandard) {
     m_PFAmeanEPICollagenclosureStandard = PFAmeanEPICollagenclosureStandard;
   }
 
   @Transient
   public Double getPFAmeanADPCollagenclosureStandard() {
     return m_PFAmeanADPCollagenclosureStandard;
   }
 
   public void setPFAmeanADPCollagenclosureStandard(Double PFAmeanADPCollagenclosureStandard) {
     m_PFAmeanADPCollagenclosureStandard = PFAmeanADPCollagenclosureStandard;
   }
 
   @Transient
   public Double getVerifyNowbaselineBase() {
     return m_VerifyNowbaselineBase;
   }
 
   public void setVerifyNowbaselineBase(Double verifyNowbaselineBase) {
     m_VerifyNowbaselineBase = verifyNowbaselineBase;
   }
 
   @Transient
   public Double getVerifyNowbaselinePRU() {
     return m_VerifyNowbaselinePRU;
   }
 
   public void setVerifyNowbaselinePRU(Double verifyNowbaselinePRU) {
     m_VerifyNowbaselinePRU = verifyNowbaselinePRU;
   }
 
   @Transient
   public Double getVerifyNowbaselinepercentinhibition() {
     return m_VerifyNowbaselinepercentinhibition;
   }
 
   public void setVerifyNowbaselinepercentinhibition(Double verifyNowbaselinepercentinhibition) {
     m_VerifyNowbaselinepercentinhibition = verifyNowbaselinepercentinhibition;
   }
 
   @Transient
   public Double getVerifyNowpostBase() {
     return m_VerifyNowpostBase;
   }
 
   public void setVerifyNowpostBase(Double verifyNowpostBase) {
     m_VerifyNowpostBase = verifyNowpostBase;
   }
 
   @Transient
   public Double getVerifyNowpostPRU() {
     return m_VerifyNowpostPRU;
   }
 
   public void setVerifyNowpostPRU(Double verifyNowpostPRU) {
     m_VerifyNowpostPRU = verifyNowpostPRU;
   }
 
   @Transient
   public Double getVerifyNowpostpercentinhibition() {
     return m_VerifyNowpostpercentinhibition;
   }
 
   public void setVerifyNowpostpercentinhibition(Double verifyNowpostpercentinhibition) {
     m_VerifyNowpostpercentinhibition = verifyNowpostpercentinhibition;
   }
 
   @Transient
   public Double getTimeloadingVerifyNow() {
     return m_TimeloadingVerifyNow;
   }
 
   public void setTimeloadingVerifyNow(Double timeloadingVerifyNow) {
     m_TimeloadingVerifyNow = timeloadingVerifyNow;
   }
 
   @Transient
   public Double getVerifyNowonclopidogrelBase() {
     return m_VerifyNowonclopidogrelBase;
   }
 
   public void setVerifyNowonclopidogrelBase(Double verifyNowonclopidogrelBase) {
     m_VerifyNowonclopidogrelBase = verifyNowonclopidogrelBase;
   }
 
   @Transient
   public Double getVerifyNowonclopidogrelPRU() {
     return m_VerifyNowonclopidogrelPRU;
   }
 
   public void setVerifyNowonclopidogrelPRU(Double verifyNowonclopidogrelPRU) {
     m_VerifyNowonclopidogrelPRU = verifyNowonclopidogrelPRU;
   }
 
   @Transient
   public Double getVerifyNowonclopidogrelpercentinhibition() {
     return m_VerifyNowonclopidogrelpercentinhibition;
   }
 
   public void setVerifyNowonclopidogrelpercentinhibition(Double verifyNowonclopidogrelpercentinhibition) {
     m_VerifyNowonclopidogrelpercentinhibition = verifyNowonclopidogrelpercentinhibition;
   }
 
   @Transient
   public Double getPAP8baselinemaxADP2() {
     return m_PAP8baselinemaxADP2;
   }
 
   public void setPAP8baselinemaxADP2(Double PAP8baselinemaxADP2) {
     m_PAP8baselinemaxADP2 = PAP8baselinemaxADP2;
   }
 
   @Transient
   public Double getPAP8baselinemaxADP5() {
     return m_PAP8baselinemaxADP5;
   }
 
   public void setPAP8baselinemaxADP5(Double PAP8baselinemaxADP5) {
     m_PAP8baselinemaxADP5 = PAP8baselinemaxADP5;
   }
 
   @Transient
   public Double getPAP8baselinemaxADP10() {
     return m_PAP8baselinemaxADP10;
   }
 
   public void setPAP8baselinemaxADP10(Double PAP8baselinemaxADP10) {
     m_PAP8baselinemaxADP10 = PAP8baselinemaxADP10;
   }
 
   @Transient
   public Double getPAP8baselinemaxADP20() {
     return m_PAP8baselinemaxADP20;
   }
 
   public void setPAP8baselinemaxADP20(Double PAP8baselinemaxADP20) {
     m_PAP8baselinemaxADP20 = PAP8baselinemaxADP20;
   }
 
   @Transient
   public Double getPAP8baselinemaxcollagen1() {
     return m_PAP8baselinemaxcollagen1;
   }
 
   public void setPAP8baselinemaxcollagen1(Double PAP8baselinemaxcollagen1) {
     m_PAP8baselinemaxcollagen1 = PAP8baselinemaxcollagen1;
   }
 
   @Transient
   public Double getPAP8baselinemaxcollagen2() {
     return m_PAP8baselinemaxcollagen2;
   }
 
   public void setPAP8baselinemaxcollagen2(Double PAP8baselinemaxcollagen2) {
     m_PAP8baselinemaxcollagen2 = PAP8baselinemaxcollagen2;
   }
 
   @Transient
   public Double getPAP8baselinemaxcollagen10() {
     return m_PAP8baselinemaxcollagen10;
   }
 
   public void setPAP8baselinemaxcollagen10(Double PAP8baselinemaxcollagen10) {
     m_PAP8baselinemaxcollagen10 = PAP8baselinemaxcollagen10;
   }
 
   @Transient
   public Double getPAP8baselinemaxcollagen6() {
     return m_PAP8baselinemaxcollagen6;
   }
 
   public void setPAP8baselinemaxcollagen6(Double PAP8baselinemaxcollagen6) {
     m_PAP8baselinemaxcollagen6 = PAP8baselinemaxcollagen6;
   }
 
   @Transient
   public Double getPAP8baselinemaxaa() {
     return m_PAP8baselinemaxaa;
   }
 
   public void setPAP8baselinemaxaa(Double PAP8baselinemaxaa) {
     m_PAP8baselinemaxaa = PAP8baselinemaxaa;
   }
 
   @Transient
   public Double getPAP8baselinelagcollagen1() {
     return m_PAP8baselinelagcollagen1;
   }
 
   public void setPAP8baselinelagcollagen1(Double PAP8baselinelagcollagen1) {
     m_PAP8baselinelagcollagen1 = PAP8baselinelagcollagen1;
   }
 
   @Transient
   public Double getPAP8baselinelagcollagen2() {
     return m_PAP8baselinelagcollagen2;
   }
 
   public void setPAP8baselinelagcollagen2(Double PAP8baselinelagcollagen2) {
     m_PAP8baselinelagcollagen2 = PAP8baselinelagcollagen2;
   }
 
   @Transient
   public Double getPAP8baselinelagcollagen5() {
     return m_PAP8baselinelagcollagen5;
   }
 
   public void setPAP8baselinelagcollagen5(Double PAP8baselinelagcollagen5) {
     m_PAP8baselinelagcollagen5 = PAP8baselinelagcollagen5;
   }
 
   @Transient
   public Double getPAP8baselinelagcollagen10() {
     return m_PAP8baselinelagcollagen10;
   }
 
   public void setPAP8baselinelagcollagen10(Double PAP8baselinelagcollagen10) {
     m_PAP8baselinelagcollagen10 = PAP8baselinelagcollagen10;
   }
 
   @Transient
   public Double getPAP8baselinemaxepi() {
     return m_PAP8baselinemaxepi;
   }
 
   public void setPAP8baselinemaxepi(Double PAP8baselinemaxepi) {
     m_PAP8baselinemaxepi = PAP8baselinemaxepi;
   }
 
   @Transient
   public Double getPAP8postmaxADP2() {
     return m_PAP8postmaxADP2;
   }
 
   public void setPAP8postmaxADP2(Double PAP8postmaxADP2) {
     m_PAP8postmaxADP2 = PAP8postmaxADP2;
   }
 
   @Transient
   public Double getPAP8postmaxADP5() {
     return m_PAP8postmaxADP5;
   }
 
   public void setPAP8postmaxADP5(Double PAP8postmaxADP5) {
     m_PAP8postmaxADP5 = PAP8postmaxADP5;
   }
 
   @Transient
   public Double getPAP8postmaxADP10() {
     return m_PAP8postmaxADP10;
   }
 
   public void setPAP8postmaxADP10(Double PAP8postmaxADP10) {
     m_PAP8postmaxADP10 = PAP8postmaxADP10;
   }
 
   @Transient
   public Double getPAP8postmaxADP20() {
     return m_PAP8postmaxADP20;
   }
 
   public void setPAP8postmaxADP20(Double PAP8postmaxADP20) {
     m_PAP8postmaxADP20 = PAP8postmaxADP20;
   }
 
   @Transient
   public Double getPAP8postmaxcollagen1() {
     return m_PAP8postmaxcollagen1;
   }
 
   public void setPAP8postmaxcollagen1(Double PAP8postmaxcollagen1) {
     m_PAP8postmaxcollagen1 = PAP8postmaxcollagen1;
   }
 
   @Transient
   public Double getPAP8postmaxcollagen2() {
     return m_PAP8postmaxcollagen2;
   }
 
   public void setPAP8postmaxcollagen2(Double PAP8postmaxcollagen2) {
     m_PAP8postmaxcollagen2 = PAP8postmaxcollagen2;
   }
 
   @Transient
   public Double getPAP8postmaxcollagen5() {
     return m_PAP8postmaxcollagen5;
   }
 
   public void setPAP8postmaxcollagen5(Double PAP8postmaxcollagen5) {
     m_PAP8postmaxcollagen5 = PAP8postmaxcollagen5;
   }
 
   @Transient
   public Double getPAP8postmaxcollagen10() {
     return m_PAP8postmaxcollagen10;
   }
 
   public void setPAP8postmaxcollagen10(Double PAP8postmaxcollagen10) {
     m_PAP8postmaxcollagen10 = PAP8postmaxcollagen10;
   }
 
   @Transient
   public Double getPAP8postmaxepiperc() {
     return m_PAP8postmaxepiperc;
   }
 
   public void setPAP8postmaxepiperc(Double PAP8postmaxepiperc) {
     m_PAP8postmaxepiperc = PAP8postmaxepiperc;
   }
 
   @Transient
   public Double getPAP8postmaxaaperc() {
     return m_PAP8postmaxaaperc;
   }
 
   public void setPAP8postmaxaaperc(Double PAP8postmaxaaperc) {
     m_PAP8postmaxaaperc = PAP8postmaxaaperc;
   }
 
   @Transient
   public Double getPAP8postlagcollagen1() {
     return m_PAP8postlagcollagen1;
   }
 
   public void setPAP8postlagcollagen1(Double PAP8postlagcollagen1) {
     m_PAP8postlagcollagen1 = PAP8postlagcollagen1;
   }
 
   @Transient
   public Double getPAP8postlagcollagen2() {
     return m_PAP8postlagcollagen2;
   }
 
   public void setPAP8postlagcollagen2(Double PAP8postlagcollagen2) {
     m_PAP8postlagcollagen2 = PAP8postlagcollagen2;
   }
 
   @Transient
   public Double getPAP8postlagcollagen5() {
     return m_PAP8postlagcollagen5;
   }
 
   public void setPAP8postlagcollagen5(Double PAP8postlagcollagen5) {
     m_PAP8postlagcollagen5 = PAP8postlagcollagen5;
   }
 
   @Transient
   public Double getPAP8postlagcollagen10() {
     return m_PAP8postlagcollagen10;
   }
 
   public void setPAP8postlagcollagen10(Double PAP8postlagcollagen10) {
     m_PAP8postlagcollagen10 = PAP8postlagcollagen10;
   }
 
   @Transient
   public Double getTimeloadingPAP8() {
     return m_TimeloadingPAP8;
   }
 
   public void setTimeloadingPAP8(Double timeloadingPAP8) {
     m_TimeloadingPAP8 = timeloadingPAP8;
   }
 
   @Transient
   public Double getPAP8standardmaxADP2() {
     return m_PAP8standardmaxADP2;
   }
 
   public void setPAP8standardmaxADP2(Double PAP8standardmaxADP2) {
     m_PAP8standardmaxADP2 = PAP8standardmaxADP2;
   }
 
   @Transient
   public Double getPAP8standardmaxADP5() {
     return m_PAP8standardmaxADP5;
   }
 
   public void setPAP8standardmaxADP5(Double PAP8standardmaxADP5) {
     m_PAP8standardmaxADP5 = PAP8standardmaxADP5;
   }
 
   @Transient
   public Double getPAP8standardmaxADP10() {
     return m_PAP8standardmaxADP10;
   }
 
   public void setPAP8standardmaxADP10(Double PAP8standardmaxADP10) {
     m_PAP8standardmaxADP10 = PAP8standardmaxADP10;
   }
 
   @Transient
   public Double getPAP8standardmaxADP20() {
     return m_PAP8standardmaxADP20;
   }
 
   public void setPAP8standardmaxADP20(Double PAP8standardmaxADP20) {
     m_PAP8standardmaxADP20 = PAP8standardmaxADP20;
   }
 
   @Transient
   public Double getPAP8standardmaxcollagen1() {
     return m_PAP8standardmaxcollagen1;
   }
 
   public void setPAP8standardmaxcollagen1(Double PAP8standardmaxcollagen1) {
     m_PAP8standardmaxcollagen1 = PAP8standardmaxcollagen1;
   }
 
   @Transient
   public Double getPAP8standardmaxcollagen2() {
     return m_PAP8standardmaxcollagen2;
   }
 
   public void setPAP8standardmaxcollagen2(Double PAP8standardmaxcollagen2) {
     m_PAP8standardmaxcollagen2 = PAP8standardmaxcollagen2;
   }
 
   @Transient
   public Double getPAP8standardmaxcollagen5() {
     return m_PAP8standardmaxcollagen5;
   }
 
   public void setPAP8standardmaxcollagen5(Double PAP8standardmaxcollagen5) {
     m_PAP8standardmaxcollagen5 = PAP8standardmaxcollagen5;
   }
 
   @Transient
   public Double getPAP8standardmaxcollagen10() {
     return m_PAP8standardmaxcollagen10;
   }
 
   public void setPAP8standardmaxcollagen10(Double PAP8standardmaxcollagen10) {
     m_PAP8standardmaxcollagen10 = PAP8standardmaxcollagen10;
   }
 
   @Transient
   public Double getPAP8standardmaxepipct() {
     return m_PAP8standardmaxepipct;
   }
 
   public void setPAP8standardmaxepipct(Double PAP8standardmaxepipct) {
     m_PAP8standardmaxepipct = PAP8standardmaxepipct;
   }
 
   @Transient
   public Double getPAP8standardmaxaapct() {
     return m_PAP8standardmaxaapct;
   }
 
   public void setPAP8standardmaxaapct(Double PAP8standardmaxaapct) {
     m_PAP8standardmaxaapct = PAP8standardmaxaapct;
   }
 
   @Transient
   public Double getPAP8standardlagcollagen1() {
     return m_PAP8standardlagcollagen1;
   }
 
   public void setPAP8standardlagcollagen1(Double PAP8standardlagcollagen1) {
     m_PAP8standardlagcollagen1 = PAP8standardlagcollagen1;
   }
 
   @Transient
   public Double getPAP8standardlagcollagen2() {
     return m_PAP8standardlagcollagen2;
   }
 
   public void setPAP8standardlagcollagen2(Double PAP8standardlagcollagen2) {
     m_PAP8standardlagcollagen2 = PAP8standardlagcollagen2;
   }
 
   @Transient
   public Double get5PAP8standardlagcollagen5() {
     return m_5PAP8standardlagcollagen5;
   }
 
   public void set5PAP8standardlagcollagen5(Double a5PAP8standardlagcollagen5) {
     m_5PAP8standardlagcollagen5 = a5PAP8standardlagcollagen5;
   }
 
   @Transient
   public Double getPAP8standardlagcollagen10() {
     return m_PAP8standardlagcollagen10;
   }
 
   public void setPAP8standardlagcollagen10(Double PAP8standardlagcollagen10) {
     m_PAP8standardlagcollagen10 = PAP8standardlagcollagen10;
   }
 
   @Transient
   public Double getChronologbaselinemaxADP5() {
     return m_ChronologbaselinemaxADP5;
   }
 
   public void setChronologbaselinemaxADP5(Double chronologbaselinemaxADP5) {
     m_ChronologbaselinemaxADP5 = chronologbaselinemaxADP5;
   }
 
   @Transient
   public Double getChronologbaselinemaxADP20() {
     return m_ChronologbaselinemaxADP20;
   }
 
   public void setChronologbaselinemaxADP20(Double chronologbaselinemaxADP20) {
     m_ChronologbaselinemaxADP20 = chronologbaselinemaxADP20;
   }
 
   @Transient
   public Double getChronologbaselinemaxaa() {
     return m_Chronologbaselinemaxaa;
   }
 
   public void setChronologbaselinemaxaa(Double chronologbaselinemaxaa) {
     m_Chronologbaselinemaxaa = chronologbaselinemaxaa;
   }
 
   @Transient
   public Double getChronologbaselinemaxcollagen1() {
     return m_Chronologbaselinemaxcollagen1;
   }
 
   public void setChronologbaselinemaxcollagen1(Double chronologbaselinemaxcollagen1) {
     m_Chronologbaselinemaxcollagen1 = chronologbaselinemaxcollagen1;
   }
 
   @Transient
   public Double getChronologbaselinelagADP5() {
     return m_ChronologbaselinelagADP5;
   }
 
   public void setChronologbaselinelagADP5(Double chronologbaselinelagADP5) {
     m_ChronologbaselinelagADP5 = chronologbaselinelagADP5;
   }
 
   @Transient
   public Double getChronologbaselinelagADP20() {
     return m_ChronologbaselinelagADP20;
   }
 
   public void setChronologbaselinelagADP20(Double chronologbaselinelagADP20) {
     m_ChronologbaselinelagADP20 = chronologbaselinelagADP20;
   }
 
   @Transient
   public Double getChronologbaselinelagaa() {
     return m_Chronologbaselinelagaa;
   }
 
   public void setChronologbaselinelagaa(Double chronologbaselinelagaa) {
     m_Chronologbaselinelagaa = chronologbaselinelagaa;
   }
 
   @Transient
   public Double getChronologbaselinelagcollagen1() {
     return m_Chronologbaselinelagcollagen1;
   }
 
   public void setChronologbaselinelagcollagen1(Double chronologbaselinelagcollagen1) {
     m_Chronologbaselinelagcollagen1 = chronologbaselinelagcollagen1;
   }
 
   @Transient
   public Double getChronologloadingmaxADP5() {
     return m_ChronologloadingmaxADP5;
   }
 
   public void setChronologloadingmaxADP5(Double chronologloadingmaxADP5) {
     m_ChronologloadingmaxADP5 = chronologloadingmaxADP5;
   }
 
   @Transient
   public Double getChronologloadingmaxADP20() {
     return m_ChronologloadingmaxADP20;
   }
 
   public void setChronologloadingmaxADP20(Double chronologloadingmaxADP20) {
     m_ChronologloadingmaxADP20 = chronologloadingmaxADP20;
   }
 
   @Transient
   public Double getChronologloadingmaxaa() {
     return m_Chronologloadingmaxaa;
   }
 
   public void setChronologloadingmaxaa(Double chronologloadingmaxaa) {
     m_Chronologloadingmaxaa = chronologloadingmaxaa;
   }
 
   @Transient
   public Double getChronologloadingmaxcollagen1() {
     return m_Chronologloadingmaxcollagen1;
   }
 
   public void setChronologloadingmaxcollagen1(Double chronologloadingmaxcollagen1) {
     m_Chronologloadingmaxcollagen1 = chronologloadingmaxcollagen1;
   }
 
   @Transient
   public Double getChronologloadinglagADP5() {
     return m_ChronologloadinglagADP5;
   }
 
   public void setChronologloadinglagADP5(Double chronologloadinglagADP5) {
     m_ChronologloadinglagADP5 = chronologloadinglagADP5;
   }
 
   @Transient
   public Double getChronologloadinglagADP20() {
     return m_ChronologloadinglagADP20;
   }
 
   public void setChronologloadinglagADP20(Double chronologloadinglagADP20) {
     m_ChronologloadinglagADP20 = chronologloadinglagADP20;
   }
 
   @Transient
   public Double getChronologloadinglagaa() {
     return m_Chronologloadinglagaa;
   }
 
   public void setChronologloadinglagaa(Double chronologloadinglagaa) {
     m_Chronologloadinglagaa = chronologloadinglagaa;
   }
 
   @Transient
   public Double getChronologloadinglagcollagen1() {
     return m_Chronologloadinglagcollagen1;
   }
 
   public void setChronologloadinglagcollagen1(Double chronologloadinglagcollagen1) {
     m_Chronologloadinglagcollagen1 = chronologloadinglagcollagen1;
   }
 
   @Transient
   public Double getTimeloadingChronolog() {
     return m_TimeloadingChronolog;
   }
 
   public void setTimeloadingChronolog(Double timeloadingChronolog) {
     m_TimeloadingChronolog = timeloadingChronolog;
   }
 
   @Transient
   public Double getChronologstandardmaxADP5() {
     return m_ChronologstandardmaxADP5;
   }
 
   public void setChronologstandardmaxADP5(Double chronologstandardmaxADP5) {
     m_ChronologstandardmaxADP5 = chronologstandardmaxADP5;
   }
 
   @Transient
   public Double getChronologstandardmaxADP20() {
     return m_ChronologstandardmaxADP20;
   }
 
   public void setChronologstandardmaxADP20(Double chronologstandardmaxADP20) {
     m_ChronologstandardmaxADP20 = chronologstandardmaxADP20;
   }
 
   @Transient
   public Double getChronologstandardmaxaa() {
     return m_Chronologstandardmaxaa;
   }
 
   public void setChronologstandardmaxaa(Double chronologstandardmaxaa) {
     m_Chronologstandardmaxaa = chronologstandardmaxaa;
   }
 
   @Transient
   public Double getChronologstandardmaxcollagen1() {
     return m_Chronologstandardmaxcollagen1;
   }
 
   public void setChronologstandardmaxcollagen1(Double chronologstandardmaxcollagen1) {
     m_Chronologstandardmaxcollagen1 = chronologstandardmaxcollagen1;
   }
 
   @Transient
   public Double getChronologstandardlagADP5() {
     return m_ChronologstandardlagADP5;
   }
 
   public void setChronologstandardlagADP5(Double chronologstandardlagADP5) {
     m_ChronologstandardlagADP5 = chronologstandardlagADP5;
   }
 
   @Transient
   public Double getChronologstandardlagADP20() {
     return m_ChronologstandardlagADP20;
   }
 
   public void setChronologstandardlagADP20(Double chronologstandardlagADP20) {
     m_ChronologstandardlagADP20 = chronologstandardlagADP20;
   }
 
   @Transient
   public Double getChronologstandardlagaa() {
     return m_Chronologstandardlagaa;
   }
 
   public void setChronologstandardlagaa(Double chronologstandardlagaa) {
     m_Chronologstandardlagaa = chronologstandardlagaa;
   }
 
   @Transient
   public Double getChronologstandardlagcollagen1() {
     return m_Chronologstandardlagcollagen1;
   }
 
   public void setChronologstandardlagcollagen1(Double chronologstandardlagcollagen1) {
     m_Chronologstandardlagcollagen1 = chronologstandardlagcollagen1;
   }
 
   @Transient
   public Double getVASP() {
     return m_VASP;
   }
 
   public void setVASP(Double VASP) {
     m_VASP = VASP;
   }
 
   @Transient
   public String getAdditionPheno() {
     return m_AdditionPheno;
   }
 
   public void setAdditionPheno(String additionPheno) {
     m_AdditionPheno = additionPheno;
   }
 
   @ElementCollection
   @JoinTable(name="sampleGenotypes", joinColumns = @JoinColumn(name="subject_id"))
   @Column(name="genotype")
   @IndexColumn(name="sortOrder", nullable = false)
   public List<String> getCyp2c19genotypes() {
     return m_cyp2c19genotypes;
   }
 
   public void setCyp2c19genotypes(List<String> cyp2c19genotypes) {
     m_cyp2c19genotypes = cyp2c19genotypes;
   }
 
   public void addCyp2c19genotype(String cyp2c19genotype) {
     if (m_cyp2c19genotypes==null) {
       m_cyp2c19genotypes = Lists.newArrayList();
     }
     m_cyp2c19genotypes.add(cyp2c19genotype);
   }
 
   public String getRs4244285() {
     return m_rs4244285;
   }
 
   public void setRs4244285(String rs4244285) {
     m_rs4244285 = rs4244285;
   }
 
   public String getRs4986893() {
     return m_rs4986893;
   }
 
   public void setRs4986893(String rs4986893) {
     m_rs4986893 = rs4986893;
   }
 
   public String getRs28399504() {
     return m_rs28399504;
   }
 
   public void setRs28399504(String rs28399504) {
     m_rs28399504 = rs28399504;
   }
 
   public String getRs56337013() {
     return m_rs56337013;
   }
 
   public void setRs56337013(String rs56337013) {
     m_rs56337013 = rs56337013;
   }
 
   public String getRs72552267() {
     return m_rs72552267;
   }
 
   public void setRs72552267(String rs72552267) {
     m_rs72552267 = rs72552267;
   }
 
   public String getRs72558186() {
     return m_rs72558186;
   }
 
   public void setRs72558186(String rs72558186) {
     m_rs72558186 = rs72558186;
   }
 
   public String getRs41291556() {
     return m_rs41291556;
   }
 
   public void setRs41291556(String rs41291556) {
     m_rs41291556 = rs41291556;
   }
 
   public String getRs6413438() {
     return m_rs6413438;
   }
 
   public void setRs6413438(String rs6413438) {
     m_rs6413438 = rs6413438;
   }
 
   public String getRs12248560() {
     return m_rs12248560;
   }
 
   public void setRs12248560(String rs12248560) {
     m_rs12248560 = rs12248560;
   }
 
   public String getRs662() {
     return m_rs662;
   }
 
   public void setRs662(String rs662) {
     m_rs662 = rs662;
   }
 
   public String getRs854560() {
     return m_rs854560;
   }
 
   public void setRs854560(String rs854560) {
     m_rs854560 = rs854560;
   }
 
   public String getRs1045642() {
     return m_rs1045642;
   }
 
   public void setRs1045642(String rs1045642) {
     m_rs1045642 = rs1045642;
   }
 
   @Column(name="other_genotypes")
   public String getOthergenotypes() {
     return m_othergenotypes;
   }
 
   public void setOthergenotypes(String othergenotypes) {
     m_othergenotypes = othergenotypes;
   }
 
   @Column(name="rs4803418")
   public String getRs4803418() {
     return m_rs4803418;
   }
 
   public void setRs4803418(String rs4803418) {
     m_rs4803418 = rs4803418;
   }
 
   @Column(name="rs48034189")
   public String getRs48034189() {
     return m_rs48034189;
   }
 
   public void setRs48034189(String rs48034189) {
     m_rs48034189 = rs48034189;
   }
 
   @Column(name="rs8192719")
   public String getRs8192719() {
     return m_rs8192719;
   }
 
   public void setRs8192719(String rs8192719) {
     m_rs8192719 = rs8192719;
   }
 
  @Column(name="rs3745274")
   public String getRs3745274() {
     return m_rs3745274;
   }
 
   public void setRs3745274(String rs3745274) {
     m_rs3745274 = rs3745274;
   }
 
   @Column(name="abs_white_on_plavix")
   public Double getAbsWhiteOnPlavix() {
     return m_absWhiteOnPlavix;
   }
 
   public void setAbsWhiteOnPlavix(Double absWhiteOnPlavix) {
     m_absWhiteOnPlavix = absWhiteOnPlavix;
   }
 
   @Column(name="red_on_plavix")
   public Double getRedOnPlavix() {
     return m_redOnPlavix;
   }
 
   public void setRedOnPlavix(Double redOnPlavix) {
     m_redOnPlavix = redOnPlavix;
   }
 
   @Column(name="platelet_on_plavix")
   public Double getPlateletOnPlavix() {
     return m_plateletOnPlavix;
   }
 
   public void setPlateletOnPlavix(Double plateletOnPlavix) {
     m_plateletOnPlavix = plateletOnPlavix;
   }
 
   @Column(name="meanplateletvol_on_plavix")
   public Double getMeanPlateletVolOnPlavix() {
     return m_meanPlateletVolOnPlavix;
   }
 
   public void setMeanPlateletVolOnPlavix(Double meanPlateletVolOnPlavix) {
     m_meanPlateletVolOnPlavix = meanPlateletVolOnPlavix;
   }
 
   @Column(name="hematocrit_on_plavix")
   public Double getHematocritOnPlavix() {
     return m_hematocritOnPlavix;
   }
 
   public void setHematocritOnPlavix(Double hematocritOnPlavix) {
     m_hematocritOnPlavix = hematocritOnPlavix;
   }
 }
