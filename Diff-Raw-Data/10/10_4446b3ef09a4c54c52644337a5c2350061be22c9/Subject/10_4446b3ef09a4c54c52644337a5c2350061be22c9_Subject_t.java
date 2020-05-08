 package org.pharmgkb;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import org.apache.log4j.Logger;
 import org.hibernate.annotations.IndexColumn;
 import org.hibernate.annotations.Type;
 import org.pharmgkb.enums.Gender;
 import org.pharmgkb.enums.Property;
 import org.pharmgkb.enums.SampleSource;
 import org.pharmgkb.enums.Value;
 import org.pharmgkb.util.IcpcUtils;
 import javax.persistence.*;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: whaleyr
  * Date: 8/28/12
  */
 @Entity
 @Table(name="samples")
 public class Subject {
   private static Logger sf_logger = Logger.getLogger(Subject.class);
 
   private String m_subjectId;
   private Enum m_Genotyping;
   private Enum m_Phenotyping;
   private Set<Enum> m_SampleSource;
   private Integer m_Project;
   private Enum m_Gender;
   private String m_Raceself;
   private String m_RaceOMB;
   private String m_Ethnicityreported;
   private String m_EthnicityOMB;
   private String m_Country;
   private Double m_Age;
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
   private Map<String,String> m_properties;
 
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
 
   @SuppressWarnings("JpaDataSourceORMInspection")
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
   public Integer getProject() {
     return m_Project;
   }
 
   public void setProject(Integer project) {
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
 
   @SuppressWarnings("JpaDataSourceORMInspection")
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
 
   @Column(name="rs4244285")
   public String getRs4244285() {
     return m_rs4244285;
   }
 
   public void setRs4244285(String rs4244285) {
     m_rs4244285 = rs4244285;
   }
 
   @Column(name="rs4986893")
   public String getRs4986893() {
     return m_rs4986893;
   }
 
   public void setRs4986893(String rs4986893) {
     m_rs4986893 = rs4986893;
   }
 
   @Column(name="rs28399504")
   public String getRs28399504() {
     return m_rs28399504;
   }
 
   public void setRs28399504(String rs28399504) {
     m_rs28399504 = rs28399504;
   }
 
   @Column(name="rs56337013")
   public String getRs56337013() {
     return m_rs56337013;
   }
 
   public void setRs56337013(String rs56337013) {
     m_rs56337013 = rs56337013;
   }
 
   @Column(name="rs72552267")
   public String getRs72552267() {
     return m_rs72552267;
   }
 
   public void setRs72552267(String rs72552267) {
     m_rs72552267 = rs72552267;
   }
 
   @Column(name="rs72558186")
   public String getRs72558186() {
     return m_rs72558186;
   }
 
   public void setRs72558186(String rs72558186) {
     m_rs72558186 = rs72558186;
   }
 
   @Column(name="rs41291556")
   public String getRs41291556() {
     return m_rs41291556;
   }
 
   public void setRs41291556(String rs41291556) {
     m_rs41291556 = rs41291556;
   }
 
   @Column(name="rs6413438")
   public String getRs6413438() {
     return m_rs6413438;
   }
 
   public void setRs6413438(String rs6413438) {
     m_rs6413438 = rs6413438;
   }
 
   @Column(name="rs12248560")
   public String getRs12248560() {
     return m_rs12248560;
   }
 
   public void setRs12248560(String rs12248560) {
     m_rs12248560 = rs12248560;
   }
 
   @Column(name="rs662")
   public String getRs662() {
     return m_rs662;
   }
 
   public void setRs662(String rs662) {
     m_rs662 = rs662;
   }
 
   @Column(name="rs854560")
   public String getRs854560() {
     return m_rs854560;
   }
 
   public void setRs854560(String rs854560) {
     m_rs854560 = rs854560;
   }
 
   @Column(name="rs1045642")
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
 
   @ElementCollection
   @MapKeyClass(java.lang.String.class)
   @MapKeyColumn(name="datakey")
   @CollectionTable(name="sampleProperties",joinColumns = @JoinColumn(name="subjectId"))
   @Column(name="datavalue")
   public Map<String, String> getProperties() {
     return m_properties;
   }
 
   public void setProperties(Map<String, String> properties) {
     m_properties = properties;
   }
 
   public String getProperty(Property property) {
     if (m_properties != null && property != null) {
       String value = m_properties.get(property.getShortName());
       return IcpcUtils.isBlank(value) ? null : value;
     }
     return null;
   }
 
   public void addProperties(Map<String, String> properties) throws Exception {
     if (m_properties == null) {
       m_properties = Maps.newHashMap();
     }
     m_properties.putAll(properties);
 
     // pick out all properties that we want to strongly persist for analysis
     setSubjectId(getProperties().get(Property.SUBJECT_ID.getShortName()));
 
     Value genotyping = Value.lookupByName(getProperties().get(Property.GENOTYPING.getShortName()));
     setGenotyping(genotyping);
 
     Value phenotyping = Value.lookupByName(getProperty(Property.PHENOTYPING));
     setPhenotyping(phenotyping);
 
     String sources = getProperty(Property.SAMPLE_SOURCE);
     if (!IcpcUtils.isBlank(sources)) {
       for (String value : Splitter.on(";").trimResults().split(sources)) {
         SampleSource source = SampleSource.lookupByName(value);
         if (source == null) {
           sf_logger.warn("can't find source for : "+value);
           throw new Exception("subject "+getSubjectId()+" can't find source for : "+value);
         }
         else {
           addSampleSource(SampleSource.lookupByName(value));
         }
       }
     }
 
     setProject(Integer.valueOf(getProperty(Property.PROJECT)));
 
     String genderString = getProperty(Property.GENDER);
     setGender(Gender.lookupByName(genderString));
 
    if ((getProperty(Property.RACE_SELF) != null
            && (getProperty(Property.RACE_SELF).equalsIgnoreCase("caucasian") || getProperty(Property.RACE_SELF).equalsIgnoreCase("white"))
        )
        ||
        (getProperty(Property.RACE_OMB) != null
            && (getProperty(Property.RACE_OMB).equalsIgnoreCase("caucasian") || getProperty(Property.RACE_OMB).equalsIgnoreCase("white"))
        )) {
      m_properties.put(Property.RACE_SELF.getShortName(), "caucasian");
      m_properties.put(Property.RACE_OMB.getShortName(), "white");
    }
     setRaceself(getProperty(Property.RACE_SELF));
     setRaceOMB(getProperty(Property.RACE_OMB));
 
     setEthnicityreported(getProperty(Property.ETHNICITY_REPORTED));
     setEthnicityOMB(getProperty(Property.ETHNICITY_OMB));
 
     setCountry(getProperty(Property.COUNTRY));
 
     if (getProperty(Property.AGE) != null) {
       setAge(Double.valueOf(getProperty(Property.AGE)));
     }
     else {
       sf_logger.warn("no age specified for "+getProject()+":"+getSubjectId());
     }
   }
 }
