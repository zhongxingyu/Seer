 package fr.cg95.cvq.business.payment;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.Table;
 
 @Entity
 @Inheritance
 @Table(name="purchase_item")
 @DiscriminatorColumn(name="item_type",discriminatorType=DiscriminatorType.STRING,length=64)
 public abstract class PurchaseItem implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     public static final String SEARCH_BY_BROKER = "broker";
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
     private String label;
     private Double amount;
 
     /**
      * Convey broker information for this item. It can't be null.
      */
    @Column(name="supported_broker")
     private String supportedBroker;
     
     public PurchaseItem(final String label, final Double amount,
             final String supportedBroker) {
         this.label = label;
         this.amount = amount;
         this.supportedBroker = supportedBroker;
     }
 
     public PurchaseItem() {
     }
 
     public final Long getId() {
         return id;
     }
 
     public final void setId(Long id) {
         this.id = id;
     }
 
     public String getLabel() {
         return label;
     }
 
     public final void setLabel(String label) {
         this.label = label;
     }
 
     public abstract String getInformativeFriendlyLabel();
 
     public final Double getAmount() {
         return amount;
     }
 
     public final void setAmount(final Double amount) {
         this.amount = amount;
     }
     
     public float getEuroAmount() {
         return amount.floatValue() / 100;
     }
 
     @Column(name="supported_broker")
     public final String getSupportedBroker() {
         return supportedBroker;
     }
 
     public final void setSupportedBroker(String supportedBroker) {
         this.supportedBroker = supportedBroker;
     }
 
     @Override
     public String toString() {
         return getInformativeFriendlyLabel();
     }
 }
