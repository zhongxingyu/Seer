 package fr.cg95.cvq.business.users;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.sf.oval.constraint.AssertValid;
 import net.sf.oval.constraint.NotEmpty;
 import net.sf.oval.constraint.NotNull;
 import net.sf.oval.constraint.Past;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import fr.cg95.cvq.business.Historizable;
 import fr.cg95.cvq.xml.common.BirthPlaceType;
 import fr.cg95.cvq.xml.common.IndividualRoleType;
 import fr.cg95.cvq.xml.common.IndividualType;
 
 
 /**
  * @hibernate.class
  *  table="individual"
  *  lazy="false"
  *
  * @author bor@zenexity.fr
  */
 public class Individual implements Historizable, Serializable {
 
     // Search fields used in DAO and Service Layer
     
     public static final String SEARCH_BY_FIRSTNAME = "firstName";
     public static final String SEARCH_BY_BIRTHDATE = "birthDate";
     
     public static final String SEARCH_BY_LASTNAME = "lastName";
     public static final String SEARCH_BY_INDIVIDUAL_ID = "individualId";
     public static final String SEARCH_BY_HOME_FOLDER_ID = "homeFolderId";
     public static final String SEARCH_BY_HOME_FOLDER_STATE = "homeFolderState";
     public static final String SEARCH_BY_HOME_FOLDER_STATUS = "homeFolderStatus";
     public static final String SEARCH_IS_HOME_FOLDER_RESPONSIBLE = "isHomeFolderResponsible";
     
 	private static final long serialVersionUID = 1L;
 
 	/** identifier field */
     private Long id;
     
     /**
      * the external identifier that is dynamically set for each external service
      * that provide us this information. It is not persisted.
      */
     private String externalId;
     
     /**
      * the external CapDemat identifier that is dynamically set before
      * talking to an external service.
      */
     private String externalCapDematId;
 
     private Integer version;
     private String login;
     private String publicKey;
     /** Liberty Alliance federation key */
     private String federationKey;
 
     @NotNull(message = "lastName")
     @NotEmpty(message = "lastName")
     private String lastName;
 
     @NotNull(message = "firstName")
     @NotEmpty(message = "firstName")
     private String firstName;
 
     @NotEmpty(message = "firstName2")
     private String firstName2;
 
     @NotEmpty(message = "firstName3")
     private String firstName3;
 
     @NotNull(message = "birthDate", when = "groovy:_this instanceof fr.cg95.cvq.business.users.Child")
     @Past(message = "birthDate")
     private Date birthDate;
 
     private String birthCountry;
     private String birthCity;
     private String birthPostalCode;
 
     @NotNull(message = "sex")
     private SexType sex;
 
     private Date creationDate;
     private ActorState state;
 
     @NotNull(message = "adress", when = "groovy:_this instanceof fr.cg95.cvq.business.users.Adult")
     @AssertValid(message = "adress", when = "groovy:_this instanceof fr.cg95.cvq.business.users.Adult")
     private Address adress;
 
    //@NotNull(message = "homeFolder")
     private HomeFolder homeFolder;
 
     private Set<IndividualRole> individualRoles;
 
     /** default constructor */
     public Individual() {
         this.sex = SexType.getDefaultSexType();
     }
 
     public void fillCommonXmlInfo(IndividualType individualType) {
 
         Calendar calendar = Calendar.getInstance();
 
         if (this.id != null)
             individualType.setId(this.id.longValue());
         if (this.externalId != null)
             individualType.setExternalId(this.externalId);
         if (this.externalCapDematId != null)
             individualType.setExternalCapdematId(this.externalCapDematId);
         if (this.login != null)
             individualType.setLogin(this.login);
         if (this.publicKey != null)
             individualType.setPublicKey(this.publicKey);
         individualType.setLastName(this.lastName);
         individualType.setFirstName(this.firstName);
         if (this.firstName2 != null)
             individualType.setFirstName2(this.firstName2);
         if (this.firstName3 != null)
             individualType.setFirstName3(this.firstName3);
         if (this.birthDate != null) {
             calendar.setTime(birthDate);
             individualType.setBirthDate(calendar);
         }
 
         if (this.birthCity != null || this.birthPostalCode != null) {
             BirthPlaceType birthPlaceType = individualType.addNewBirthPlace();
             birthPlaceType.setCity(this.birthCity);
             birthPlaceType.setPostalCode(this.birthPostalCode);
         }
         
         if (this.sex != null)
             individualType.setSex(fr.cg95.cvq.xml.common.SexType.Enum.forString(this.sex.toString()));
         if (this.creationDate != null) {
             calendar.setTime(this.creationDate);
             individualType.setCreationDate(calendar);
         }
         if (this.adress != null)
             individualType.setAddress(Address.modelToXml(this.adress));
         if (this.state != null)
             individualType.setState(fr.cg95.cvq.xml.common.ActorStateType.Enum.forString(this.state.toString()));
         if (this.individualRoles != null && !this.individualRoles.isEmpty()) {
             IndividualRoleType roles[] =
                 new IndividualRoleType[this.individualRoles.size()];
             int i = 0;
             for (IndividualRole individualRole : this.individualRoles) {
                 roles[i++] = IndividualRole.modelToXml(individualRole);
             }
             individualType.setRoleArray(roles);
         }
     }
 
     public void fillCommonModelInfo(IndividualType individualType) {
 
         if (individualType.getId() == 0)
             setId(null);
         else
             setId(new Long(individualType.getId()));
         setLogin(individualType.getLogin());
         if (individualType.getPublicKey() != null)
             setPublicKey(individualType.getPublicKey());
         setLastName(individualType.getLastName());
         setFirstName(individualType.getFirstName());
         setFirstName2(individualType.getFirstName2());
         setFirstName3(individualType.getFirstName3());
         if (individualType.getBirthDate() != null)
             setBirthDate(individualType.getBirthDate().getTime());
         if (individualType.getBirthPlace() != null) {
             setBirthCountry(individualType.getBirthPlace().getCity());
             setBirthPostalCode(individualType.getBirthPlace().getPostalCode());
         }
         if (individualType.getSex() != null)
             setSex(SexType.forString(individualType.getSex().toString()));
         if (individualType.getCreationDate() != null) {
             setCreationDate(individualType.getCreationDate().getTime());
         }
         if (individualType.getState() != null)
             setState(ActorState.forString(individualType.getState().toString()));
         setAdress(Address.xmlToModel(individualType.getAddress()));
     }
 
     public static Individual xmlToModel(fr.cg95.cvq.xml.common.IndividualType individualType) {
         Individual individual = new Individual();
         individual.fillCommonModelInfo(individualType);
 
         return individual;
     }
 
     public static IndividualType modelToXml(Individual individual) {
         if (individual instanceof Adult) {
             Adult adult = (Adult) individual;
             return Adult.modelToXml(adult);
         } else if (individual instanceof Child) {
             Child child = (Child) individual;
             return Child.modelToXml(child);
         } else {
             IndividualType individualType = IndividualType.Factory.newInstance();
             individual.fillCommonXmlInfo(individualType);
             return individualType;
         }
     }
 
     /**
      * @hibernate.id
      *  generator-class="sequence"
      *  column="id"
      */
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getExternalId() {
         return externalId;
     }
 
     public void setExternalId(String externalId) {
         this.externalId = externalId;
     }
 
     public String getExternalCapDematId() {
         return externalCapDematId;
     }
 
     public void setExternalCapDematId(String externalCapDematId) {
         this.externalCapDematId = externalCapDematId;
     }
 
     /**
      * @hibernate.version
      *  column="version"
      */
     public Integer getVersion() {
         return this.version;
     }
 
     public void setVersion(Integer version) {
         this.version = version;
     }
 
     public void setVersion(String version) {
         this.version = new Integer(version);
     }
 
     /**
      * @hibernate.property
      *  column="login"
      *  unique="true"
      */
     public String getLogin() {
         return this.login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     /**
      * @hibernate.property
      *  column="public_key"
      *  unique="true"
      *  length="50"
      */
     public String getPublicKey() {
         return this.publicKey;
     }
 
     public void setPublicKey(String publicKey) {
         this.publicKey = publicKey;
     }
 
     /**
      * @hibernate.property
      *  column="federation_key"
      *  unique="true"
      *  length="64"
      */
     public String getFederationKey() {
         return this.federationKey;
     }
 
     public void setFederationKey(String federationKey) {
         this.federationKey = federationKey;
     }
 
     /**
      * @hibernate.property
      *  column="last_name"
      *  length="38"
      */
     public String getLastName() {
         return this.lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     /**
      * @hibernate.property
      *  column="first_name"
      *  length="38"
      */
     public String getFirstName() {
         return this.firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     /**
      * @hibernate.property
      *  column="first_name_2"
      *  length="38"
      */
     public String getFirstName2() {
         return this.firstName2;
     }
 
     public void setFirstName2(String firstName2) {
         this.firstName2 = firstName2;
     }
 
     /**
      * @hibernate.property
      *  column="first_name_3"
      *  length="38"
      */
     public String getFirstName3() {
         return this.firstName3;
     }
 
     public void setFirstName3(String firstName3) {
         this.firstName3 = firstName3;
     }
 
     /**
      * @hibernate.property
      *  column="birth_date"
      */
     public Date getBirthDate() {
         return this.birthDate;
     }
 
     public void setBirthDate(Date birthDate) {
         this.birthDate = birthDate;
     }
 
     public void setBirthDate(String birthDate) {
         if (birthDate == null || birthDate.equals("")) {
             this.birthDate = null;
         } else {
             DateFormat df = DateFormat.getDateInstance();
             try {
                 this.birthDate = df.parse(birthDate);
             } catch (java.text.ParseException pe) {
                 // hmm, worrying isn't it ?
             }
         }
     }
 
     /**
      * @hibernate.property
      *  column="birth_country"
      */
     public String getBirthCountry() {
         return this.birthCountry;
     }
 
     public void setBirthCountry(String birthCountry) {
         this.birthCountry = birthCountry;
     }
 
     /**
      * @hibernate.property
      *  column="birth_city"
      *  length="32"
      */
     public String getBirthCity() {
         return this.birthCity;
     }
 
     public void setBirthCity(String birthCity) {
         this.birthCity = birthCity;
     }
 
     /**
      * @hibernate.property
      *  column="birth_postal_code"
      *  length="5"
      */
     public String getBirthPostalCode() {
         return this.birthPostalCode;
     }
 
     public void setBirthPostalCode(String birthPostalCode) {
         this.birthPostalCode = birthPostalCode;
     }
 
     /**
      * @hibernate.property
      *  column="sex"
      *  length="8"
      */
     public SexType getSex() {
         return this.sex;
     }
 
     public void setSex(SexType sex) {
         this.sex = sex;
     }
 
     public void setSex(String sex) {
         this.sex = SexType.forString(sex);
     }
 
     /**
      * @hibernate.property
      *  column="creation_date"
      */
     public Date getCreationDate() {
         return this.creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public void setCreationDate(String creationDate) {
         if (creationDate == null || creationDate.equals("")) {
             this.creationDate = null;
         } else {
             DateFormat df = DateFormat.getDateInstance();
             try {
                 this.creationDate = df.parse(creationDate);
             } catch (java.text.ParseException pe) {
                 // hmm, worrying isn't it ?
             }
         }
     }
 
     /**
      * @hibernate.property
      *  column="state"
      *  length="16"
      *  not-null="true"
      */
     public ActorState getState() {
         return this.state;
     }
 
     public void setState(ActorState state) {
         this.state = state;
     }
 
     public void setState(String state) {
         this.state = ActorState.forString(state);
     }
     
     /**
      * @hibernate.many-to-one
      *  class="fr.cg95.cvq.business.users.Address"
      *  column="adress_id"
      *  cascade="all"
      */
     public Address getAdress() {
         return this.adress;
     }
 
     public void setAdress(Address adress) {
         this.adress = adress;
     }
 
     /**
      * @hibernate.many-to-one
      *  class="fr.cg95.cvq.business.users.HomeFolder"
      *  column="home_folder_id"
      */
     public HomeFolder getHomeFolder() {
         return this.homeFolder;
     }
 
     public void setHomeFolder(HomeFolder homeFolder) {
         this.homeFolder = homeFolder;
     }
 
     /**
      * @hibernate.set
      *  lazy="false"
      *  cascade="all"
      *  order-by="id asc"
      * @hibernate.key
      *  column="owner_id"
      * @hibernate.one-to-many
      *  class="fr.cg95.cvq.business.users.IndividualRole"
      */
     public Set<IndividualRole> getIndividualRoles() {
         return individualRoles;
     }
 
     public Set<RoleType> getIndividualRoles(final Long individualId) {
         Set<RoleType> result = new HashSet<RoleType>();
         for (IndividualRole individualRole : individualRoles) {
             if (individualRole.getIndividualId() != null
                     && individualRole.getIndividualId().equals(individualId))
                 result.add(individualRole.getRole());
         }
         
         return result;
     }
     
     public void setIndividualRoles(Set<IndividualRole> individualRoles) {
         this.individualRoles = individualRoles;
     }
 
     public String getFullName() {
         return getLastName() + " " + getFirstName();
     }
     
     @Override
     public String toString() {
         return new ToStringBuilder(this)
             .append("name", getFullName())
             .append("id", getId())
             .toString();
     }
 }
