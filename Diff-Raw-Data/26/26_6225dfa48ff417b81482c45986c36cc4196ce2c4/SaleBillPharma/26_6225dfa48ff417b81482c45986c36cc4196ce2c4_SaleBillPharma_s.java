 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pojos;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author ashutoshsingh
  */
 @Entity
 @Table(name = "sale_bill_pharma")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "SaleBillPharma.findAll", query = "SELECT s FROM SaleBillPharma s"),
     @NamedQuery(name = "SaleBillPharma.findById", query = "SELECT s FROM SaleBillPharma s WHERE s.id = :id"),
     @NamedQuery(name = "SaleBillPharma.findByBillDate", query = "SELECT s FROM SaleBillPharma s WHERE s.billDate = :billDate"),
     @NamedQuery(name = "SaleBillPharma.findByCustomerId", query = "SELECT s FROM SaleBillPharma s WHERE s.customerId = :customerId"),
     @NamedQuery(name = "SaleBillPharma.findByTotalVat", query = "SELECT s FROM SaleBillPharma s WHERE s.totalVat = :totalVat"),
     @NamedQuery(name = "SaleBillPharma.findByTotalAmt", query = "SELECT s FROM SaleBillPharma s WHERE s.totalAmt = :totalAmt"),
     @NamedQuery(name = "SaleBillPharma.findByDiscount", query = "SELECT s FROM SaleBillPharma s WHERE s.discount = :discount"),
     @NamedQuery(name = "SaleBillPharma.findByFinalAmt", query = "SELECT s FROM SaleBillPharma s WHERE s.finalAmt = :finalAmt"),
     @NamedQuery(name = "SaleBillPharma.findByDeliveryAddress", query = "SELECT s FROM SaleBillPharma s WHERE s.deliveryAddress = :deliveryAddress")})
 public class SaleBillPharma implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
     @Basic(optional = false)
     @Column(name = "bill_date")
     private int billDate;
    @Basic(optional = false)
    @Column(name = "customer_id")
    private int customerId;
     @Basic(optional = false)
     @Column(name = "total_vat")
     private float totalVat;
     @Basic(optional = false)
     @Column(name = "total_amt")
     private float totalAmt;
     // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
     @Column(name = "discount")
     private Float discount;
     @Basic(optional = false)
     @Column(name = "final_amt")
     private float finalAmt;
     @Column(name = "delivery_address")
     private String deliveryAddress;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "saleBillNo")
     private List<SaleBillPharmaItem> saleBillPharmaItemList;
 
     public SaleBillPharma() {
     }
 
     public SaleBillPharma(Integer id) {
         this.id = id;
     }
 
    public SaleBillPharma(Integer id, int billDate, int customerId, float totalVat, float totalAmt, float finalAmt) {
         this.id = id;
         this.billDate = billDate;
         this.customerId = customerId;
         this.totalVat = totalVat;
         this.totalAmt = totalAmt;
         this.finalAmt = finalAmt;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public int getBillDate() {
         return billDate;
     }
 
     public void setBillDate(int billDate) {
         this.billDate = billDate;
     }
 
    public int getCustomerId() {
         return customerId;
     }
 
    public void setCustomerId(int customerId) {
         this.customerId = customerId;
     }
 
     public float getTotalVat() {
         return totalVat;
     }
 
     public void setTotalVat(float totalVat) {
         this.totalVat = totalVat;
     }
 
     public float getTotalAmt() {
         return totalAmt;
     }
 
     public void setTotalAmt(float totalAmt) {
         this.totalAmt = totalAmt;
     }
 
     public Float getDiscount() {
         return discount;
     }
 
     public void setDiscount(Float discount) {
         this.discount = discount;
     }
 
     public float getFinalAmt() {
         return finalAmt;
     }
 
     public void setFinalAmt(float finalAmt) {
         this.finalAmt = finalAmt;
     }
 
     public String getDeliveryAddress() {
         return deliveryAddress;
     }
 
     public void setDeliveryAddress(String deliveryAddress) {
         this.deliveryAddress = deliveryAddress;
     }
 
     @XmlTransient
     public List<SaleBillPharmaItem> getSaleBillPharmaItemList() {
         return saleBillPharmaItemList;
     }
 
     public void setSaleBillPharmaItemList(List<SaleBillPharmaItem> saleBillPharmaItemList) {
         this.saleBillPharmaItemList = saleBillPharmaItemList;
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
         if (!(object instanceof SaleBillPharma)) {
             return false;
         }
         SaleBillPharma other = (SaleBillPharma) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "pojos.SaleBillPharma[ id=" + id + " ]";
     }
    
 }
