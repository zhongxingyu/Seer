 package com.salesmanager.core.business.shoppingcart.model;
 
 import java.math.BigDecimal;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.EntityListeners;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 import javax.persistence.Transient;
 
 import com.salesmanager.core.business.catalog.product.model.Product;
 import com.salesmanager.core.business.common.model.audit.AuditListener;
 import com.salesmanager.core.business.common.model.audit.AuditSection;
 import com.salesmanager.core.business.common.model.audit.Auditable;
 import com.salesmanager.core.business.customer.model.Customer;
 import com.salesmanager.core.business.generic.model.SalesManagerEntity;
 import com.salesmanager.core.constants.SchemaConstant;
 
 
 @Entity
 @EntityListeners(value = AuditListener.class)
 @Table(name = "SHOPPING_CART_ITEM", schema=SchemaConstant.SALESMANAGER_SCHEMA)
 public class ShoppingCartItem extends SalesManagerEntity<Long, ShoppingCartItem> implements Auditable {
 
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@Column(name = "SHP_CART_ITEM_ID", unique=true, nullable=false)
 	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "SHP_CRT_ITM_SEQ_NEXT_VAL")
 	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
 	private Long id;
 	
 	@ManyToOne(targetEntity = ShoppingCart.class)
 	@JoinColumn(name = "SHP_CART_ID", nullable = false)
 	private ShoppingCart shoppingCart;
	
 	@Column(name="QUANTITY")
 	private Integer quantity;
 	
 	@Column(name="ITEM_NUMBER")
 	private Integer cartItemNuber;
 
 	@Embedded
 	private AuditSection auditSection = new AuditSection();
 	
 	@ManyToOne(targetEntity = Product.class)
 	@JoinColumn(name = "PRODUCT_ID", nullable = false)
 	private Product product;
 	
 
 	
 	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "shoppingCartItem")
 	private Set<ShoppingCartAttributeItem> attributes = new HashSet<ShoppingCartAttributeItem>();
 	
 	@Transient
 	private BigDecimal itemPrice;
 
 
 
 	@Override
 	public AuditSection getAuditSection() {
 		// TODO Auto-generated method stub
 		return auditSection;
 	}
 
 	@Override
 	public void setAuditSection(AuditSection audit) {
 		this.auditSection = audit;
 		
 	}
 
 	@Override
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public void setId(Long id) {
 		this.id = id;
 		
 	}
 
 	public void setProduct(Product product) {
 		this.product = product;
 	}
 
 	public Product getProduct() {
 		return product;
 	}
 
 	public void setAttributes(Set<ShoppingCartAttributeItem> attributes) {
 		this.attributes = attributes;
 	}
 
 	public Set<ShoppingCartAttributeItem> getAttributes() {
 		return attributes;
 	}
 
 	public void setItemPrice(BigDecimal itemPrice) {
 		this.itemPrice = itemPrice;
 	}
 
 	public BigDecimal getItemPrice() {
 		return itemPrice;
 	}
 
 	public void setQuantity(Integer quantity) {
 		this.quantity = quantity;
 	}
 
 	public Integer getQuantity() {
 		return quantity;
 	}
 
 	public Integer getCartItemNuber() {
 		return cartItemNuber;
 	}
 
 	public void setCartItemNuber(Integer cartItemNuber) {
 		this.cartItemNuber = cartItemNuber;
 	}
 
  
 
 }
