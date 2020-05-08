 package com.lunch4you.domain;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Version;
 import javax.validation.constraints.NotNull;
 
 import org.springframework.beans.factory.annotation.Configurable;
 
 @Configurable
 @Entity
 public class OrderItem {
 
 	@Id
 	@GeneratedValue( strategy = GenerationType.AUTO )
 	private Long id;
 
 	@Version
 	private Integer version;
 
 	@NotNull
 	private Integer amount;
 
 	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE } )
 	@JoinColumn( name = "article_id" )
 	private Article article;
 
//	insert="false" update="false"
	@ManyToOne(cascade = CascadeType.ALL)
 	@JoinColumn( name = "order_id", insertable=false, updatable=false  )
 	private Order order;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId( Long id ) {
 		this.id = id;
 	}
 
 	public Integer getVersion() {
 		return version;
 	}
 
 	public void setVersion( Integer version ) {
 		this.version = version;
 	}
 
 	public Integer getAmount() {
 		return amount;
 	}
 
 	public void setAmount( Integer amount ) {
 		this.amount = amount;
 	}
 
 	public Article getArticle() {
 		return article;
 	}
 
 	public void setArticle( Article articleId ) {
 		this.article = articleId;
 	}
 
 	public Order getOrder() {
 		return order;
 	}
 	
 	public void setOrder(Order order) {
 		this.order = order;
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append( '(' ).append( id ).append( ") " );
 		sb.append( "Order item: " ).append( article );
 		sb.append( " * " ).append( amount );
 		sb.append( " = " ).append( article == null ? "N/A" : article.getPrice() * amount ).append( " credit(s)" );
 		return sb.toString();
 	}
 
 }
