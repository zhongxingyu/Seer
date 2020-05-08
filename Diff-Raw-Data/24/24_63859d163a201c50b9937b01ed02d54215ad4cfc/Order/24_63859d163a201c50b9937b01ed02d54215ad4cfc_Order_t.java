 package oshop.model;
 
 import org.hibernate.annotations.Formula;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.Size;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 @Table(name = "orders")
 public class Order extends BaseEntity<Integer> {
 
     @Size(max = 255)
     @Column(name = "description")
     private String description;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "order_date")
     private Date date = new Date();
 
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "customer_id")
     private Customer customer;
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "shipping_address_id")
     private ShippingAddress shippingAddress;
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "discount_id")
     private Discount discount;
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "additional_payment_id")
     private AdditionalPayment additionalPayment;
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy="order", cascade = {CascadeType.ALL}, orphanRemoval = true)
     private List<OrderHasOrderStates> states = new ArrayList<OrderHasOrderStates>();
 
     @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
     @JoinTable(
         name = "order_products",
         joinColumns = {@JoinColumn(name = "order_id", nullable = false, updatable = false)},
         inverseJoinColumns = { @JoinColumn(name = "product_id", nullable = false, updatable = false) },
         uniqueConstraints = {@UniqueConstraint(name = "UK_order_product", columnNames = {"order_id", "product_id"})}
     )
     private List<Product> products = new ArrayList<Product>();
 
     @Formula("( SELECT s.name FROM order_has_order_states h INNER JOIN order_state s ON h.order_state_id = s.id " +
             "WHERE h.order_id = id ORDER BY h.date DESC, h.id DESC LIMIT 1 )")
     private String currentOrderStateName;
 
     @Formula("( SELECT count(p.product_id) FROM order_products p WHERE p.order_id = id )")
     private Integer productsCount;
 
     @Formula("( SELECT sum(p.price) FROM order_products o INNER JOIN product p on o.product_id = p.id WHERE o.order_id = id )")
     private BigDecimal productsPrice;
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public Customer getCustomer() {
         return customer;
     }
 
     public void setCustomer(Customer customer) {
         this.customer = customer;
     }
 
     public ShippingAddress getShippingAddress() {
         return shippingAddress;
     }
 
     public void setShippingAddress(ShippingAddress shippingAddress) {
         this.shippingAddress = shippingAddress;
     }
 
     public Discount getDiscount() {
         return discount;
     }
 
     public void setDiscount(Discount discount) {
         this.discount = discount;
     }
 
     public AdditionalPayment getAdditionalPayment() {
         return additionalPayment;
     }
 
     public void setAdditionalPayment(AdditionalPayment additionalPayment) {
         this.additionalPayment = additionalPayment;
     }
 
     public List<OrderHasOrderStates> getStates() {
         return states;
     }
 
     public void setStates(List<OrderHasOrderStates> states) {
         this.states = states;
     }
 
     public List<Product> getProducts() {
         return products;
     }
 
     public void setProducts(List<Product> products) {
         this.products = products;
     }
 
     public Integer getProductsCount() {
         return productsCount;
     }
 
     public void setProductsCount(Integer productsCount) {
         this.productsCount = productsCount;
     }
 
     public String getCurrentOrderStateName() {
         return currentOrderStateName;
     }
 
     public void setCurrentOrderStateName(String currentOrderStateName) {
         this.currentOrderStateName = currentOrderStateName;
     }
 
     public BigDecimal getProductsPrice() {
         return productsPrice;
     }
 
     public void setProductsPrice(BigDecimal productsPrice) {
         this.productsPrice = productsPrice;
     }
 
     public void setTotalPrice(BigDecimal productsPrice) {
         // empty
     }
 
     public BigDecimal getTotalPrice() {
        BigDecimal additionalPaymentPrice = this.getAdditionalPayment() != null ? this.getAdditionalPayment().getAmount() : new BigDecimal(0);

        return this.calcDiscount().add(additionalPaymentPrice);
    }

    public BigDecimal calcDiscount() {
         BigDecimal discountPrice = this.getDiscount() != null ? this.getDiscount().getAmount() : new BigDecimal(0);
         byte discountType = this.getDiscount() != null ? this.getDiscount().getType() : 1;
 
         BigDecimal productsPrice = this.getProductsPrice() != null ? this.getProductsPrice() : new BigDecimal(0);
 
        if (discountPrice.compareTo(new BigDecimal(0)) == 1) {
            if (discountType == Discount.Type.FIXED_DISCOUNT.getType()) {
                return EntityUtils.round(productsPrice.subtract(discountPrice));
            } else if (discountType == Discount.Type.PERCENT_DISCOUNT.getType()) {
                return EntityUtils.round(productsPrice.subtract(productsPrice.multiply(discountPrice.divide(new BigDecimal(100)))));
            } else {
                throw new IllegalArgumentException("Discount type not supported: " + discountType);
            }
        } else {
            return EntityUtils.round(productsPrice);
        }
     }
 }
