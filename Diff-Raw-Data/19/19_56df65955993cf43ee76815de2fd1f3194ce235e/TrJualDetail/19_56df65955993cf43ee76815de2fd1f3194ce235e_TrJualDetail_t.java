 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package training.java.penjualan.domain;
 
 import java.math.BigDecimal;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 /**
  *
  * @author adi
  */
 
 @Entity
 @Table(name="tr_jual_detail")
 public class TrJualDetail {
     
     @Id
     private TrJualDetailPK idJual;
     
     @ManyToOne
    @JoinColumn(name="no_faktur", referencedColumnName="no_faktur", insertable=false, updatable=false, nullable=false)
     private TrJualHeader header;
     
     @ManyToOne
    @JoinColumn(name="id_barang", referencedColumnName="id",insertable=false, updatable=false, nullable=false)
     private Barang barang;
     
     @Column(name="qty")
     private Integer qty;
     
     @Column(name="harga")
     private BigDecimal harga;
     
     @Column(name="total")
     private BigDecimal total;
 
     public Barang getBarang() {
         return barang;
     }
 
     public void setBarang(Barang barang) {
         this.barang = barang;
        this.idJual.setIdBarang(barang.getId());
     }
 
     public BigDecimal getHarga() {
         return harga;
     }
 
     public void setHarga(BigDecimal harga) {
         this.harga = harga;
     }
 
     public TrJualHeader getHeader() {
         return header;
     }
 
     public void setHeader(TrJualHeader header) {
         this.header = header;
        this.idJual.setNoFaktur(header.getNoFaktur());
     }
 
     public Integer getQty() {
         return qty;
     }
 
     public void setQty(Integer qty) {
         this.qty = qty;
     }
 
     public BigDecimal getTotal() {
         return total;
     }
 
     public void setTotal(BigDecimal total) {
         this.total = total;
     }
 
     public TrJualDetailPK getIdJual() {
         return idJual;
     }
 
     public void setIdJual(TrJualDetailPK idJual) {
         this.idJual = idJual;
     }
     
     
 }
