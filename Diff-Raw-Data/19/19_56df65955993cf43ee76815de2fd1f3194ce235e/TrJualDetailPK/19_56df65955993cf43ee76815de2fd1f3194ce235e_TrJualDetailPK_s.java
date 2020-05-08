 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package training.java.penjualan.domain;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 
 /**
  *
  * @author adi
  */
 
 @Embeddable
 public class TrJualDetailPK implements Serializable{
     
    @Column(name="no_faktur", insertable=false, updatable=false, nullable=false)
     private String noFaktur;
     
    @Column(name="id_barang", insertable=false, updatable=false, nullable=false)
     private Long idBarang;
 
     public TrJualDetailPK() {
     }
 
     public TrJualDetailPK(String noFaktur, Long idBarang) {
         this.noFaktur = noFaktur;
         this.idBarang = idBarang;
     }
 
     public Long getIdBarang() {
         return idBarang;
     }
 
     public void setIdBarang(Long idBarang) {
         this.idBarang = idBarang;
     }
 
     public String getNoFaktur() {
         return noFaktur;
     }
 
     public void setNoFaktur(String noFaktur) {
         this.noFaktur = noFaktur;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final TrJualDetailPK other = (TrJualDetailPK) obj;
         if ((this.noFaktur == null) ? (other.noFaktur != null) : !this.noFaktur.equals(other.noFaktur)) {
             return false;
         }
         if (this.idBarang != other.idBarang && (this.idBarang == null || !this.idBarang.equals(other.idBarang))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 11 * hash + (this.noFaktur != null ? this.noFaktur.hashCode() : 0);
         hash = 11 * hash + (this.idBarang != null ? this.idBarang.hashCode() : 0);
         return hash;
     }
     
     
 }
