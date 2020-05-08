 package abey.entities;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 /**
  *
  * @author Anthony
  */
 @Entity
 public class Produit implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     @OneToMany(mappedBy = "produit")
     private List<NoteProduit> notes;
 
     @OneToMany(mappedBy = "produit")
     private List<VenteImmediate> ventesImmediates;
 
     @OneToMany(mappedBy = "produit")
     private List<Enchere> encheres;
 
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 255)
     private String nom;
 
     @Basic(optional = false, fetch = FetchType.LAZY)
     @NotNull
     @Lob
     private String description;
 
     @OneToMany
     private List<Image> images;
 
     @NotNull
     @ManyToOne
     private Categorie categorie;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public List<NoteProduit> getNotes() {
         return notes;
     }
 
     public void setNotes(List<NoteProduit> notes) {
         this.notes = notes;
     }
 
     public List<VenteImmediate> getVentesImmediates() {
         return ventesImmediates;
     }
 
     public void setVentesImmediates(List<VenteImmediate> ventesImmediates) {
         this.ventesImmediates = ventesImmediates;
     }
 
     public List<Enchere> getEncheres() {
         return encheres;
     }
 
     public void setEncheres(List<Enchere> encheres) {
         this.encheres = encheres;
     }
 
     public String getNom() {
         return nom;
     }
 
     public void setNom(String nom) {
         this.nom = nom;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public List<Image> getImages() {
         if (images == null) {
             images = new ArrayList<>();
         }
         return images;
     }
 
     public void setImages(List<Image> images) {
         this.images = images;
     }
 
     public Image getMainImage() {
         if (getImages().size() > 0) {
             return getImages().get(0);
         } else {
             return null;
         }
     }
 
     public void addImage(Image image) {
         getImages().add(image);
     }
 
     public Categorie getCategorie() {
         return categorie;
     }
 
     public void setCategorie(Categorie categorie) {
         this.categorie = categorie;
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
         if (!(object instanceof Produit)) {
             return false;
         }
         Produit other = (Produit) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "abey.entities.Produit[ id=" + id + " ]";
     }
    
     public BigDecimal getPrixMinVenteImmediate() {
         BigDecimal prixMin = BigDecimal.ZERO;
         for (VenteImmediate venteImmediate : ventesImmediates) {
             if (venteImmediate.getStock() > 0
                     && (venteImmediate.getPrix().compareTo(prixMin) < 0 || prixMin == BigDecimal.ZERO)) {
                 prixMin = venteImmediate.getPrix();
             }
         }
         return prixMin;
     }
    
     public BigDecimal getPrixMinEnchere() {
         BigDecimal prixMin = BigDecimal.ZERO;
         for (Enchere enchere : encheres) {
             if (enchere.getMontantCourant().compareTo(prixMin) < 0
                     || prixMin == BigDecimal.ZERO) {
                 prixMin = enchere.getMontantCourant();
             }
         }
         return prixMin;
     }
    
     public BigDecimal getPrixMin() {
         BigDecimal prixMinVenteImmediate = getPrixMinVenteImmediate();
         BigDecimal prixMinEnchere = getPrixMinEnchere();
        return prixMinVenteImmediate.compareTo(prixMinEnchere) < 0 ? prixMinVenteImmediate : prixMinEnchere;
     }
 
 }
