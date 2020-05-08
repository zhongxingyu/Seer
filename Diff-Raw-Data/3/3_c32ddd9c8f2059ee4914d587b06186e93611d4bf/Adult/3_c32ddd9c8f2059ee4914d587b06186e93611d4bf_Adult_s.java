 package fr.cg95.cvq.business.users;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Table;
 
 import net.sf.oval.constraint.Email;
 import net.sf.oval.constraint.EqualToField;
 import net.sf.oval.constraint.MatchPattern;
 import net.sf.oval.constraint.MinLength;
 import net.sf.oval.constraint.NotEmpty;
 import net.sf.oval.constraint.NotNull;
 import fr.cg95.cvq.authentication.IAuthenticationService;
 import fr.cg95.cvq.xml.common.AdultType;
 
 @Entity
 @Table(name="adult")
 public class Adult extends Individual {
 
     private static final long serialVersionUID = 1L;
 
     @NotNull(message = "title")
     @Enumerated(EnumType.STRING)
     @Column(name="title",length=16)
     private TitleType title;
 
     @NotEmpty(message = "maidenName")
     @Column(name="maiden_name")
     private String maidenName;
 
     @NotEmpty(message = "nameOfUse")
     @Column(name="name_of_use")
     private String nameOfUse;
 
     @Enumerated(EnumType.STRING)
     @Column(name="family_status",length=32)
     private FamilyStatusType familyStatus;
 
     @NotNull(message = "homePhone", when = "groovy:_this.mobilePhone == null && _this.officePhone == null")
     @NotEmpty(message = "homePhone")
     @MatchPattern(pattern = "^0[1-59][0-9]{8}$", message = "homePhone")
     @Column(name="home_phone",length=32)
     private String homePhone;
 
     @NotNull(message = "mobilePhone", when = "groovy:_this.homePhone == null && _this.officePhone == null")
     @NotEmpty(message = "mobilePhone")
     @MatchPattern(pattern = "^0[67][0-9]{8}$", message = "mobilePhone")
     @Column(name="mobile_phone",length=32)
     private String mobilePhone;
 
     @NotNull(message = "officePhone", when = "groovy:_this.homePhone == null && _this.mobilePhone == null")
     @NotEmpty(message = "officePhone")
     @MatchPattern(pattern = "^0[1-5679][0-9]{8}$", message = "officePhone")
     @Column(name="office_phone",length=32)
     private String officePhone;
 
     @NotNull(message = "email")
     @Email(message = "email")
     @Column(name="email",length=50)
     private String email;
 
     @MatchPattern(pattern = "^[0-9]{7}[A-Z]{0,1}$", message = "cfbn")
     @Column(name="cfbn",length=8)
     private String cfbn;
 
     @Column(name="profession")
     private String profession;
 
     @NotNull(message = "question", profiles = {"login"})
     @NotEmpty(message = "question")
     @Column(name="question")
     private String question;
 
     @NotNull(message = "answer", profiles = {"login"})
     @NotEmpty(message = "answer")
     @Column(name="answer")
     private String answer;
 
     @Column(name="login")
     private String login;
 
     @NotNull(message = "password", profiles = {"login"})
     @MinLength(message = "password", value = IAuthenticationService.passwordMinLength)
     @Column(name="password")
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
             adult.setExternalCapDematId(adultType.getExternalCapdematId());
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
 
     public String getMaidenName() {
         return this.maidenName;
     }
 
     public void setMaidenName(String maidenName) {
         this.maidenName = maidenName;
     }
 
     public String getNameOfUse() {
         return this.nameOfUse;
     }
 
     public void setNameOfUse(String nameOfUse) {
         this.nameOfUse = nameOfUse;
     }
 
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
 
     public String getHomePhone() {
         return this.homePhone;
     }
 
     public void setHomePhone(String homePhone) {
         this.homePhone = homePhone == null ? null : homePhone.replaceAll("[^\\d]", "");
     }
 
     public String getMobilePhone() {
         return this.mobilePhone;
     }
 
     public void setMobilePhone(String mobilePhone) {
         this.mobilePhone = mobilePhone == null ? null : mobilePhone.replaceAll("[^\\d]", "");
     }
 
     public String getOfficePhone() {
         return this.officePhone;
     }
 
     public void setOfficePhone(String officePhone) {
         this.officePhone = officePhone == null ? null : officePhone.replaceAll("[^\\d]", "");
     }
 
     public String getEmail() {
         return this.email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getCfbn() {
         return this.cfbn;
     }
 
     public void setCfbn(String cfbn) {
         this.cfbn = cfbn;
     }
 
     public String getProfession() {
         return this.profession;
     }
 
     public void setProfession(String profession) {
         this.profession = profession;
     }
 
     public String getQuestion() {
         return this.question;
     }
 
     public void setQuestion(String question) {
         this.question = question;
     }
 
     public String getAnswer() {
         return this.answer;
     }
 
     public void setAnswer(String answer) {
         this.answer = answer;
     }
 
     public String getLogin() {
         return this.login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
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
