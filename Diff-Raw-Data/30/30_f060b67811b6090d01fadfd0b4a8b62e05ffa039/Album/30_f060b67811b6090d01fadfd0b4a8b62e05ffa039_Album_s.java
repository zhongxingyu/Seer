 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageModel;
 
 import java.io.Serializable;
 import java.text.DecimalFormat;
 
 /**
  * @author BAVAY Emilien - THILLAYE Joachim
  */
 public class Album implements Serializable{
    private Integer idAlbum, prcRemise,idArtiste;
     private String titre, image, artiste, label, labelImg;
     private double prix, prixPromo;
     private Boolean promo;
     
     public Album() {}
     // Getters
     public Integer getIdAlbum() {return idAlbum;}
     public String getTitre() {return titre;}
     
     
     public double getPrix()
     {
         return prix;
     }
     
     public double getPrixPromo()
     {
         return prixPromo;
     }
     
     public String getPrixFormat() 
     {        
         DecimalFormat myFormatter = new DecimalFormat("#.##");
         String output = myFormatter.format(prix);
         return output;
     }
     public String getPrixPromoFormat() 
     {        
         DecimalFormat myFormatter = new DecimalFormat("#.##");
         String output = myFormatter.format(prixPromo);
         return output;
     }
     public String getImage() {return image;}
     public String getArtiste() {return artiste;}
     public String getLabel() {return label;}
     public String getLabelImg() {return labelImg;}
     public Boolean getPromo() {return promo;}
     public Integer getPrcRemise() {return prcRemise;}
     
     // Setters
     public void setIdAlbum(Integer idAlbum) {this.idAlbum = idAlbum;}
     public void setTitre(String titre) {this.titre = titre;}
     public void setPrix(double prix) {this.prix = prix;}
     public void setPrixPromo(double prixPromo) {this.prixPromo = prixPromo;}        
     public void setArtiste(String artiste) {this.artiste = artiste;}
     public void setImage(String image) {this.image = image;}
     public void setLabel(String label) {this.label = label;}
     public void setLabelImg(String labelImg) {this.labelImg = labelImg;}
     public void setPromo(Boolean promo) {this.promo = promo;}
     public void setPrcRemise(Integer prcRemise) {this.prcRemise = prcRemise;}

    /**
     * @return the idArtiste
     */
    public Integer getIdArtiste() {
        return idArtiste;
    }

    /**
     * @param idArtiste the idArtiste to set
     */
    public void setIdArtiste(Integer idArtiste) {
        this.idArtiste = idArtiste;
    }
     
 }
