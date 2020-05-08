 
 
 package fr.cg95.cvq.business.request.school;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.oval.constraint.*;
 import fr.cg95.cvq.business.authority.*;
 import fr.cg95.cvq.business.request.*;
 import fr.cg95.cvq.business.users.*;
 import fr.cg95.cvq.service.request.LocalReferential;
 import fr.cg95.cvq.service.request.condition.IConditionChecker;
 
 import javax.persistence.*;
 import org.hibernate.annotations.Index;
 import org.hibernate.annotations.Type;
 
 /**
  * Generated class file, do not edit !
  */
 @Entity
 @Table(name="global_school_registration_request")
 public class GlobalSchoolRegistrationRequestData implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     public static final Map<String, IConditionChecker> conditions =
         new HashMap<String, IConditionChecker>(RequestData.conditions);
 
     private Long id;
 
     public GlobalSchoolRegistrationRequestData() {
       
         acceptationReglementInterieur = Boolean.valueOf(false);
       
         estDerogation = Boolean.valueOf(false);
       
         estPeriscolaire = Boolean.valueOf(false);
       
         estRestauration = Boolean.valueOf(false);
       
     }
 
     @Override
     public GlobalSchoolRegistrationRequestData clone() {
         GlobalSchoolRegistrationRequestData result = new GlobalSchoolRegistrationRequestData();
         
           
             
         result.setAcceptationReglementInterieur(acceptationReglementInterieur);
       
           
         
           
             
         result.setEstDerogation(estDerogation);
       
           
         
           
             
         result.setEstPeriscolaire(estPeriscolaire);
       
           
         
           
             
         result.setEstRestauration(estRestauration);
       
           
         
           
             
         result.setIdEcoleDerog(idEcoleDerog);
       
           
         
           
             
         result.setIdEcoleSecteur(idEcoleSecteur);
       
           
         
           
             
         result.setInformationsComplementairesDerogation(informationsComplementairesDerogation);
       
           
         
           
             
         result.setLabelEcoleDerog(labelEcoleDerog);
       
           
         
           
             
         result.setLabelEcoleSecteur(labelEcoleSecteur);
       
           
         
           
             
         List<fr.cg95.cvq.business.request.LocalReferentialData> motifsDerogationEcoleList = new ArrayList<fr.cg95.cvq.business.request.LocalReferentialData>();
         for (LocalReferentialData object : motifsDerogationEcole) {
             motifsDerogationEcoleList.add(object.clone());
         }
         result.setMotifsDerogationEcole(motifsDerogationEcoleList);
       
           
         
         return result;
     }
 
     public final void setId(final Long id) {
         this.id = id;
     }
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     public final Long getId() {
         return this.id;
     }
 
   
     
     private Boolean acceptationReglementInterieur;
 
     public void setAcceptationReglementInterieur(final Boolean acceptationReglementInterieur) {
         this.acceptationReglementInterieur = acceptationReglementInterieur;
     }
 
  
     @Column(name="acceptation_reglement_interieur"  )
       
     public Boolean getAcceptationReglementInterieur() {
         return this.acceptationReglementInterieur;
     }
   
     
       @NotNull(
         
         
         profiles = {"enfant"},
         message = "estDerogation"
       )
     
     private Boolean estDerogation;
 
     public void setEstDerogation(final Boolean estDerogation) {
         this.estDerogation = estDerogation;
     }
 
  
     @Column(name="est_derogation"  )
       
     public Boolean getEstDerogation() {
         return this.estDerogation;
     }
   
     
       @NotNull(
         
         
         profiles = {"periscolaire"},
         message = "estPeriscolaire"
       )
     
     private Boolean estPeriscolaire;
 
     public void setEstPeriscolaire(final Boolean estPeriscolaire) {
         this.estPeriscolaire = estPeriscolaire;
     }
 
  
     @Column(name="est_periscolaire"  )
       
     public Boolean getEstPeriscolaire() {
         return this.estPeriscolaire;
     }
   
     
       @NotNull(
         
         
         profiles = {"restauration"},
         message = "estRestauration"
       )
     
     private Boolean estRestauration;
 
     public void setEstRestauration(final Boolean estRestauration) {
         this.estRestauration = estRestauration;
     }
 
  
     @Column(name="est_restauration"  )
       
     public Boolean getEstRestauration() {
         return this.estRestauration;
     }
   
     
       @NotNull(
         
         
           when = "groovy:def active = true;" +
           
             
             "active &= _this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             "return active",
         
         profiles = {"enfant"},
         message = "idEcoleDerog"
       )
     
       @NotBlank(
         
         
           when = "groovy:def active = true;" +
           
             
             "active &= _this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             "return active",
         
         profiles = {"enfant"},
         message = "idEcoleDerog"
       )
     
     private String idEcoleDerog;
 
     public void setIdEcoleDerog(final String idEcoleDerog) {
         this.idEcoleDerog = idEcoleDerog;
     }
 
  
     @Column(name="id_ecole_derog"  )
       
     public String getIdEcoleDerog() {
         return this.idEcoleDerog;
     }
   
     
       @NotNull(
         
         
           when = "groovy:def active = true;" +
           
             
             "active &= !_this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             "return active",
         
         profiles = {"enfant"},
         message = "idEcoleSecteur"
       )
     
       @NotBlank(
         
         
           when = "groovy:def active = true;" +
           
             
             "active &= !_this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             "return active",
         
         profiles = {"enfant"},
         message = "idEcoleSecteur"
       )
     
     private String idEcoleSecteur;
 
     public void setIdEcoleSecteur(final String idEcoleSecteur) {
         this.idEcoleSecteur = idEcoleSecteur;
     }
 
  
     @Column(name="id_ecole_secteur"  )
       
     public String getIdEcoleSecteur() {
         return this.idEcoleSecteur;
     }
   
     
       @MaxLength(
         
           value = 1024,
         
         
           when = "groovy:def active = true;" +
           
             "active &= _this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             
             "return active",
         
         profiles = {"enfant"},
         message = "informationsComplementairesDerogation"
       )
     
       @MatchPattern(
         
          pattern = "^[\\w\\W]{0,1024}$",
         
         
           when = "groovy:def active = true;" +
           
             "active &= _this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             
             "return active",
         
         profiles = {"enfant"},
         message = "informationsComplementairesDerogation"
       )
     
     private String informationsComplementairesDerogation;
 
     public void setInformationsComplementairesDerogation(final String informationsComplementairesDerogation) {
         this.informationsComplementairesDerogation = informationsComplementairesDerogation;
     }
 
  
     @Column(name="informations_complementaires_derogation" , length=1024 )
       
     public String getInformationsComplementairesDerogation() {
         return this.informationsComplementairesDerogation;
     }
   
     
       @NotNull(
         
         
           when = "groovy:def active = true;" +
           
             
             "active &= _this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             "return active",
         
         profiles = {"enfant"},
         message = "labelEcoleDerog"
       )
     
       @NotBlank(
         
         
           when = "groovy:def active = true;" +
           
             
             "active &= _this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             "return active",
         
         profiles = {"enfant"},
         message = "labelEcoleDerog"
       )
     
     private String labelEcoleDerog;
 
     public void setLabelEcoleDerog(final String labelEcoleDerog) {
         this.labelEcoleDerog = labelEcoleDerog;
     }
 
  
     @Column(name="label_ecole_derog"  )
       
     public String getLabelEcoleDerog() {
         return this.labelEcoleDerog;
     }
   
     
       @NotNull(
         
         
           when = "groovy:def active = true;" +
           
             
             "active &= !_this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             "return active",
         
         profiles = {"enfant"},
         message = "labelEcoleSecteur"
       )
     
       @NotBlank(
         
         
           when = "groovy:def active = true;" +
           
             
             "active &= !_this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             "return active",
         
         profiles = {"enfant"},
         message = "labelEcoleSecteur"
       )
     
     private String labelEcoleSecteur;
 
     public void setLabelEcoleSecteur(final String labelEcoleSecteur) {
         this.labelEcoleSecteur = labelEcoleSecteur;
     }
 
  
     @Column(name="label_ecole_secteur"  )
       
     public String getLabelEcoleSecteur() {
         return this.labelEcoleSecteur;
     }
   
     
       @LocalReferential(
         
         
           when = "groovy:def active = true;" +
           
             "active &= _this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             
             "return active",
         
         profiles = {"enfant"},
         message = "motifsDerogationEcole"
       )
     
       @MinSize(
         
           value = 1,
         
         
           when = "groovy:def active = true;" +
           
             "active &= _this.conditions['estDerogation'].test(_this.estDerogation.toString());" +
                 
               
             
             
             "return active",
         
         profiles = {"enfant"},
         message = "motifsDerogationEcole"
       )
     
     private List<fr.cg95.cvq.business.request.LocalReferentialData> motifsDerogationEcole;
 
     public void setMotifsDerogationEcole(final List<fr.cg95.cvq.business.request.LocalReferentialData> motifsDerogationEcole) {
         this.motifsDerogationEcole = motifsDerogationEcole;
     }
 
  
     @ManyToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
     @JoinTable(name="global_school_registration_request_motifs_derogation_ecole",
             joinColumns=
                 @JoinColumn(name="global_school_registration_request_id"),
             inverseJoinColumns=
                 @JoinColumn(name="motifs_derogation_ecole_id"))
     @OrderColumn(name="motifs_derogation_ecole_index")
       
     public List<fr.cg95.cvq.business.request.LocalReferentialData> getMotifsDerogationEcole() {
         return this.motifsDerogationEcole;
     }
   
 }
