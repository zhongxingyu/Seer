 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entity;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author Brian GOHIER
  */
 @Entity
 @Table(name = "CLIENT")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Client.findAll", query = "SELECT c FROM Client c"),
     @NamedQuery(name = "Client.findById", query = "SELECT c FROM Client c WHERE c.id = :id"),
     @NamedQuery(name = "Client.findByName", query = "SELECT c FROM Client c WHERE c.name = :name"),
     @NamedQuery(name = "Client.findByAddress", query = "SELECT c FROM Client c WHERE c.address = :address"),
     @NamedQuery(name = "Client.findByPostalcode", query = "SELECT c FROM Client c WHERE c.postalcode = :postalcode"),
     @NamedQuery(name = "Client.findByPhone", query = "SELECT c FROM Client c WHERE c.phone = :phone"),
     @NamedQuery(name = "Client.findByFax", query = "SELECT c FROM Client c WHERE c.fax = :fax"),
     @NamedQuery(name = "Client.findByTarif", query = "SELECT c FROM Client c WHERE c.tarif = :tarif"),
     @NamedQuery(name = "Client.findByDeplacement", query = "SELECT c FROM Client c WHERE c.deplacement = :deplacement"),
     @NamedQuery(name = "Client.findByMail", query = "SELECT c FROM Client c WHERE c.mail = :mail"),
     @NamedQuery(name = "Client.findByInterventionType", query = "SELECT c FROM Client c WHERE c.interventionType = :interventionType"),
     @NamedQuery(name = "Client.findByOperator", query = "SELECT c FROM Client c WHERE c.operator = :operator"),
     @NamedQuery(name = "Client.findByInternetLogin", query = "SELECT c FROM Client c WHERE c.internetLogin = :internetLogin"),
     @NamedQuery(name = "Client.findByInternetPassword", query = "SELECT c FROM Client c WHERE c.internetPassword = :internetPassword"),
     @NamedQuery(name = "Client.findByObservations", query = "SELECT c FROM Client c WHERE c.observations = :observations"),
     @NamedQuery(name = "Client.findBySleeping", query = "SELECT c FROM Client c WHERE c.sleeping = :sleeping")})
 public class Client implements Serializable
 {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "ID")
     private Integer id;
     @Basic(optional = false)
     @NotNull
     @Size(max = 45)
     @Column(name = "NAME")
     private String name;
     @Basic(optional = false)
     @NotNull
     @Size(max = 100)
     @Column(name = "ADDRESS")
     private String address;
     @Basic(optional = false)
     @Column(name = "POSTALCODE")
     private int postalcode;
     @Basic(optional = false)
     @NotNull
     @Column(name = "PHONE")
     private String phone;
     @Column(name = "FAX")
     private String fax;
     @Column(name = "TARIF")
     private Double tarif;
     @Column(name = "DEPLACEMENT")
     private Double deplacement;
     @Basic(optional = false)
     @NotNull
     @Size(max = 30)
     @Column(name = "MAIL")
     private String mail;
     @Size(max = 250)
     @Column(name = "INTERVENTION_TYPE")
     private String interventionType;
     @Size(max = 30)
     @Column(name = "OPERATOR")
     private String operator;
     @Size(max = 30)
     @Column(name = "INTERNET_LOGIN")
     private String internetLogin;
     @Size(max = 64)
     @Column(name = "INTERNET_PASSWORD")
     private String internetPassword;
     @Size(max = 250)
     @Column(name = "OBSERVATIONS")
     private String observations;
     @Column(name = "SLEEPING")
     private Boolean sleeping=false;
     @JoinColumn(name = "ID_USER", referencedColumnName = "ID")
     @ManyToOne
     private CUser idUser;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idClient")
     private List<CUser> cUserList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idClient")
     private List<Workstation> workstationList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idClient")
     private List<Mail> mailList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idClient")
     private List<Software> softwareList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idClient")
     private List<Task> taskList;
 
     public Client() {
     }
 
     public Client(Integer id) {
         this.id = id;
     }
 
     public Client(Integer id, String name, String address, int postalcode, String phone, String mail) {
         this.id = id;
         this.name = name;
         this.address = address;
         this.postalcode = postalcode;
         this.phone = phone;
         this.mail = mail;
     }
 
     public Integer getId() {
         return this.id==null?-1:this.id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public int getPostalcode() {
         return postalcode;
     }
 
     public void setPostalcode(int postalcode) {
         this.postalcode = postalcode;
     }
 
     public String getPhone() {
         return phone;
     }
 
     public void setPhone(String phone) {
         this.phone = phone;
     }
 
     public String getFax() {
         return fax;
     }
 
     public void setFax(String fax) {
         this.fax = fax;
     }
 
     public Double getTarif() {
         return tarif;
     }
 
     public void setTarif(Double tarif) {
         this.tarif = tarif;
     }
 
     public Double getDeplacement() {
         return deplacement;
     }
 
     public void setDeplacement(Double deplacement) {
         this.deplacement = deplacement;
     }
 
     public String getMail() {
         return mail;
     }
 
     public void setMail(String mail) {
         this.mail = mail;
     }
 
     public String getInterventionType() {
         return interventionType;
     }
 
     public void setInterventionType(String interventionType) {
         this.interventionType = interventionType;
     }
 
     public String getOperator() {
         return operator;
     }
 
     public void setOperator(String operator) {
         this.operator = operator;
     }
 
     public String getInternetLogin() {
         return internetLogin;
     }
 
     public void setInternetLogin(String internetLogin) {
         this.internetLogin = internetLogin;
     }
 
     public String getInternetPassword() {
         return internetPassword;
     }
 
     public void setInternetPassword(String internetPassword) {
         this.internetPassword = internetPassword;
     }
 
     public String getObservations() {
         return observations;
     }
 
     public void setObservations(String observations) {
         this.observations = observations;
     }
 
     public Boolean getSleeping() {
         return sleeping;
     }
 
     public void setSleeping(Boolean sleeping) {
         this.sleeping = sleeping;
     }
 
     public CUser getIdUser() {
         return idUser;
     }
 
     public void setIdUser(CUser idUser) {
         this.idUser = idUser;
     }
 
     @XmlTransient
     public List<CUser> getCUserList() {
         return cUserList;
     }
 
     public void setCUserList(List<CUser> cUserList) {
         this.cUserList = cUserList;
     }
 
     @XmlTransient
     public List<Workstation> getWorkstationList() {
         return workstationList;
     }
 
     public void setWorkstationList(List<Workstation> workstationList) {
         this.workstationList = workstationList;
     }
 
     @XmlTransient
     public List<Mail> getMailList() {
         return mailList;
     }
 
     public void setMailList(List<Mail> mailList) {
         this.mailList = mailList;
     }
 
     @XmlTransient
     public List<Software> getSoftwareList() {
         return softwareList;
     }
 
     public void setSoftwareList(List<Software> softwareList) {
         this.softwareList = softwareList;
     }
 
     @XmlTransient
     public List<Task> getTaskList() {
         return taskList;
     }
 
     public void setTaskList(List<Task> taskList) {
         this.taskList = taskList;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Client)) {
             return false;
         }
         Client other = (Client) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     public String getFullString()
     {
         return "entity.Client{" + "id=" + id + ", name=" + name + ", address=" + address + ", postalcode=" + postalcode + ", phone=" + phone + ", fax=" + fax + ", tarif=" + tarif + ", deplacement=" + deplacement + ", mail=" + mail + ", interventionType=" + interventionType + ", operator=" + operator + ", internetLogin=" + internetLogin + ", internetPassword=" + internetPassword + ", observations=" + observations + ", sleeping=" + sleeping + ", idUser=" + idUser + '}';
     }
 
     @Override
     public String toString() {
         return this.name;
     }
 
 }
