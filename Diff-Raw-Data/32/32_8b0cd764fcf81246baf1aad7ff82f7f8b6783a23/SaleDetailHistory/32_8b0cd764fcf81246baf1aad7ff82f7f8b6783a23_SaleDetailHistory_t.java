 // Copyright 2013 Structure Eng Inc.
 
 package com.structureeng.persistence.model.history.sale;
 
 import com.structureeng.persistence.model.history.AbstractHistoryTenantModel;
 import com.structureeng.persistence.model.product.ProductPrice;
 import com.structureeng.persistence.model.sale.Sale;
 import com.structureeng.persistence.model.sale.SaleDetail;
 
 import com.google.common.base.Preconditions;
 
 import java.math.BigDecimal;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 /**
  * Define the revision for the {@code SaleDetail} entities.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  */
 @Entity
 @Table(name = "saleDetailHistory", uniqueConstraints = {
         @UniqueConstraint(name = "saleDetailHistoryUK",
                 columnNames = {"idSaleDetail", "idAudit"})})
 public class SaleDetailHistory extends AbstractHistoryTenantModel {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "idSaleDetailHistory")
     private Long id;
 
     @JoinColumn(name = "idSaleDetail", referencedColumnName = "idSaleDetail", nullable = false)
     @ManyToOne(cascade = CascadeType.REFRESH)
     private SaleDetail saleDetail;
 
     @JoinColumn(name = "idSale", referencedColumnName = "idSale", nullable = false)
     @ManyToOne(cascade = CascadeType.REFRESH)
     private Sale sale;
 
    @JoinColumn(name = "idProductPrice", referencedColumnName = "idProductPrice",
             nullable = false)
     @ManyToOne(cascade = CascadeType.REFRESH)
    private ProductPrice productPrice;
 
     @Column(name = "lineOrder", nullable = false)
     private int lineOrder;
 
     @Column(name = "quantity", nullable = false, precision = 13, scale = 4)
     private BigDecimal quantity;
 
     @Column(name = "price", nullable = false, precision = 13, scale = 4)
     private BigDecimal price;
 
     @Column(name = "tax", nullable = false, precision = 13, scale = 4)
     private BigDecimal tax;
 
     @Column(name = "total", nullable = false, precision = 13, scale = 4)
     private BigDecimal total;
 
     @Override
     public Long getId() {
         return id;
     }
 
     @Override
     public void setId(Long id) {
         this.id = Preconditions.checkNotNull(id);
     }
 
     public SaleDetail getSaleDetail() {
         return saleDetail;
     }
 
     public void setSaleDetail(SaleDetail saleDetail) {
         this.saleDetail = Preconditions.checkNotNull(saleDetail);
     }
 
     public Sale getSale() {
         return sale;
     }
 
     public void setSale(Sale sale) {
         this.sale = Preconditions.checkNotNull(sale);
     }
 
    public ProductPrice getProductPrice() {
        return productPrice;
     }
 
    public void setProductPrice(ProductPrice productPrice) {
        this.productPrice = Preconditions.checkNotNull(productPrice);
     }
 
     public int getLineOrder() {
         return lineOrder;
     }
 
     public void setLineOrder(int lineOrder) {
         this.lineOrder = Preconditions.checkNotNull(lineOrder);
     }
 
     public BigDecimal getQuantity() {
         return quantity;
     }
 
     public void setQuantity(BigDecimal quantity) {
         this.quantity = Preconditions.checkNotNull(quantity);
     }
 
     public BigDecimal getPrice() {
         return price;
     }
 
     public void setPrice(BigDecimal price) {
         this.price = Preconditions.checkNotNull(price);
     }
 
     public BigDecimal getTax() {
         return tax;
     }
 
     public void setTax(BigDecimal tax) {
         this.tax = Preconditions.checkNotNull(tax);
     }
 
     public BigDecimal getTotal() {
         return total;
     }
 
     public void setTotal(BigDecimal total) {
         this.total = Preconditions.checkNotNull(total);
     }
 }
