 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package kasbon.entity;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  *
  * @author endy
  */
 @Entity
 public class Pengajuan implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     @ManyToOne
     @JoinColumn(name="id_pemohon", nullable=false)
     private Karyawan pemohon;
     
    @Temporal(TemporalType.TIMESTAMP)
     @Column(name="waktu_pengajuan", nullable=false)
     private Date waktuPengajuan = new Date();
     
     @Column(name="nilai_pinjaman", nullable=false)
     private BigDecimal nilaiPinjaman;
     
     @Enumerated(EnumType.STRING)
     @Column(name="status_pengajuan", nullable=false)
     private StatusPengajuan statusPengajuan = StatusPengajuan.DIAJUKAN;
 
     public StatusPengajuan getStatusPengajuan() {
         return statusPengajuan;
     }
 
     public void setStatusPengajuan(StatusPengajuan statusPengajuan) {
         this.statusPengajuan = statusPengajuan;
     }
 
     public BigDecimal getNilaiPinjaman() {
         return nilaiPinjaman;
     }
 
     public void setNilaiPinjaman(BigDecimal nilaiPinjaman) {
         this.nilaiPinjaman = nilaiPinjaman;
     }
 
     public Karyawan getPemohon() {
         return pemohon;
     }
 
     public void setPemohon(Karyawan pemohon) {
         this.pemohon = pemohon;
     }
 
     public Date getWaktuPengajuan() {
         return waktuPengajuan;
     }
 
     public void setWaktuPengajuan(Date waktuPengajuan) {
         this.waktuPengajuan = waktuPengajuan;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
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
         if (!(object instanceof Pengajuan)) {
             return false;
         }
         Pengajuan other = (Pengajuan) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "kasbon.entity.Pengajuan[ id=" + id + " ]";
     }
     
 }
