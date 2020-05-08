 package fr.cg95.cvq.business.users;
 
 import org.apache.commons.lang3.StringUtils;
 
 import net.sf.oval.constraint.Email;
 import net.sf.oval.constraint.EqualToField;
 import net.sf.oval.constraint.MatchPattern;
 import net.sf.oval.constraint.MinLength;
 import net.sf.oval.constraint.NotEmpty;
 import net.sf.oval.constraint.NotNull;
 import fr.cg95.cvq.authentication.IAuthenticationService;
 import fr.cg95.cvq.xml.common.AdultType;
 
 /**
  * @hibernate.joined-subclass
  *  table="adult"
  *  lazy="false"
  * @hibernate.joined-subclass-key
  *  column="id"
  *
  * @author bor@zenexity.fr
  */
 public class Adult extends Individual {
 
     private static final long serialVersionUID = 1L;
 
     @NotNull(message = "title")
     private TitleType title;
 
     @NotEmpty(message = "maidenName")
     private String maidenName;
 
     @NotEmpty(message = "nameOfUse")
     private String nameOfUse;
 
     private FamilyStatusType familyStatus;
 
    @NotNull(message = "adultPhones", when = "groovy:_this.mobilePhone == null && _this.officePhone == null")
     @NotEmpty(message = "homePhone")
     @MatchPattern(pattern = "^0[1-59][0-9]{8}$", message = "homePhone")
     private String homePhone;
 
    @NotNull(message = "adultPhones", when = "groovy:_this.homePhone == null && _this.officePhone == null")
     @NotEmpty(message = "mobilePhone")
     @MatchPattern(pattern = "^0[67][0-9]{8}$", message = "mobilePhone")
     private String mobilePhone;
 
    @NotNull(message = "adultPhones", when = "groovy:_this.homePhone == null && _this.mobilePhone == null")
     @NotEmpty(message = "officePhone")
     @MatchPattern(pattern = "^0[1-59][0-9]{8}$", message = "officePhone")
     private String officePhone;
 
     @NotNull(message = "email")
     @Email(message = "email")
     private String email;
 
     private String cfbn;
     private String profession;
 
     @NotNull(message = "question", profiles = {"login"})
     @NotEmpty(message = "question")
     private String question;
 
     @NotNull(message = "answer", profiles = {"login"})
     @NotEmpty(message = "answer")
     private String answer;
 
     private String login;
 
     @NotNull(message = "password", profiles = {"login"})
     @MinLength(message = "password", value = IAuthenticationService.passwordMinLength)
     private String password;
 
     @SuppressWarnings("unused")
     @NotNull(message = "confirmPassword", profiles = {"login"})
     @EqualToField(message = "confirmPassword", value = "password", profiles = {"login"})
     private String confirmPassword;
 
     public AdultType modelToXml() {
         AdultType adultType = AdultType.Factory.newInstance();
         fillCommonXmlInfo(adultType);
         if (getTitle() != null)
             adultType.setTitle(fr.cg95.cvq.xml.common.TitleType.Enum.forString(getTitle().toString()));
         if (getMaidenName() != null)
             adultType.setMaidenName(getMaidenName());
         adultType.setNameOfUse(getNameOfUse());
         if (getFamilyStatus() != null)
             adultType.setFamilyStatus(fr.cg95.cvq.xml.common.FamilyStatusType.Enum.forString(getFamilyStatus().toString()));
         if (getHomePhone() != null)
             adultType.setHomePhone(getHomePhone());
         if (getMobilePhone() != null)
             adultType.setMobilePhone(getMobilePhone());
         if (getOfficePhone() != null)
             adultType.setOfficePhone(getOfficePhone());
         if (getEmail() != null)
             adultType.setEmail(getEmail());
         if (getCfbn() != null)
             adultType.setCfbn(getCfbn());
         if (getProfession() != null)
             adultType.setProfession(getProfession());
         if (getLogin() != null)
             adultType.setLogin(getLogin());
         // FIXME : do we include such information when we export user information ??
         if (getPassword() != null)
             adultType.setPassword(getPassword());
         if (getQuestion() != null)
             adultType.setQuestion(getQuestion());
         if (getAnswer() != null)
             adultType.setAnswer(getAnswer());
         return adultType;
     }
 
     public static Adult xmlToModel(AdultType adultType) {
         if (adultType != null) {
             Adult adult = new Adult();
             adult.fillCommonModelInfo(adultType);
             adult.setLogin(adultType.getLogin());
             adult.setPassword(adultType.getPassword());
             if (adultType.getTitle() != null)
                 adult.setTitle(TitleType.forString(adultType.getTitle().toString()));
             if (adultType.getMaidenName() != null)
                 adult.setMaidenName(adultType.getMaidenName());
             adult.setNameOfUse(adultType.getNameOfUse());
             if (adultType.getFamilyStatus() != null)
                 adult.setFamilyStatus(FamilyStatusType.forString(adultType.getFamilyStatus().toString()));
             adult.setHomePhone(adultType.getHomePhone());
             adult.setMobilePhone(adultType.getMobilePhone());
             adult.setOfficePhone(adultType.getOfficePhone());
             adult.setEmail(adultType.getEmail());
             adult.setCfbn(adultType.getCfbn());
             adult.setProfession(adultType.getProfession());
             if (adultType.getQuestion() != null)
                 adult.setQuestion(adultType.getQuestion());
             if (adultType.getAnswer() != null)
                 adult.setAnswer(adultType.getAnswer());
             
             return adult;
         } else {
             return null;
         }
     }
 
     /**
      * @hibernate.property
      *  column="title"
      *  length="16"
      */
     public TitleType getTitle() {
         return this.title;
     }
 
     public void setTitle(TitleType title) {
         this.title = title;
     }
 
     public void setTitleType(String title) {
         TitleType[] allTitleTypes = TitleType.allTitleTypes;
         for (int i=0; i < allTitleTypes.length; i++) {
             TitleType titleType = allTitleTypes[i];
             if (titleType.toString().equals(title))
                 this.title = titleType;
         }
     }
 
     /**
      * @hibernate.property
      *  column="maiden_name"
      */
     public String getMaidenName() {
         return this.maidenName;
     }
 
     public void setMaidenName(String maidenName) {
         this.maidenName = maidenName;
     }
 
     /**
      * @hibernate.property
      *  column="name_of_use"
      */
     public String getNameOfUse() {
         return this.nameOfUse;
     }
 
     public void setNameOfUse(String nameOfUse) {
         this.nameOfUse = nameOfUse;
     }
 
     /**
      * @hibernate.property
      *  column="family_status"
      *  length="32"
      */
     public FamilyStatusType getFamilyStatus() {
         return this.familyStatus;
     }
 
     public void setFamilyStatus(FamilyStatusType familyStatus) {
         this.familyStatus = familyStatus;
     }
 
     public void setFamilyStatus(String familyStatus) {
         FamilyStatusType[] allFamilyStatusTypes = FamilyStatusType.allFamilyStatusTypes;
         for (int i=0; i < allFamilyStatusTypes.length; i++) {
             FamilyStatusType fs = allFamilyStatusTypes[i];
             if (fs.toString().equals(familyStatus))
                 this.familyStatus = fs;
         }
     }
 
     /**
      * @hibernate.property
      *  column="home_phone"
      *  length="32"
      */
     public String getHomePhone() {
         return this.homePhone;
     }
 
     public void setHomePhone(String homePhone) {
         this.homePhone = homePhone == null ? null : homePhone.replaceAll("[^\\d]", "");
     }
 
     /**
      * @hibernate.property
      *  column="mobile_phone"
      *  length="32"
      */
     public String getMobilePhone() {
         return this.mobilePhone;
     }
 
     public void setMobilePhone(String mobilePhone) {
         this.mobilePhone = mobilePhone == null ? null : mobilePhone.replaceAll("[^\\d]", "");
     }
 
     /**
      * @hibernate.property
      *  column="office_phone"
      *  length="32"
      */
     public String getOfficePhone() {
         return this.officePhone;
     }
 
     public void setOfficePhone(String officePhone) {
         this.officePhone = officePhone == null ? null : officePhone.replaceAll("[^\\d]", "");
     }
 
     /**
      * @hibernate.property
      *  column="email"
      *  length="50"
      */
     public String getEmail() {
         return this.email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     /**
      * @hibernate.property
      *  column="cfbn"
      *  length="8"
      */
     public String getCfbn() {
         return this.cfbn;
     }
 
     public void setCfbn(String cfbn) {
         this.cfbn = cfbn;
     }
 
     /**
      * @hibernate.property
      *  column="profession"
      */
     public String getProfession() {
         return this.profession;
     }
 
     public void setProfession(String profession) {
         this.profession = profession;
     }
 
     /**
      * @hibernate.property
      *  column="question"
      */
     public String getQuestion() {
         return this.question;
     }
 
     public void setQuestion(String question) {
         this.question = question;
     }
 
     /**
      * @hibernate.property
      *  column="answer"
      */
     public String getAnswer() {
         return this.answer;
     }
 
     public void setAnswer(String answer) {
         this.answer = answer;
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
      *  column="password"
      */
     public String getPassword() {
         return this.password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setConfirmPassword(String confirmPassword) {
         this.confirmPassword = confirmPassword;
     }
 }
