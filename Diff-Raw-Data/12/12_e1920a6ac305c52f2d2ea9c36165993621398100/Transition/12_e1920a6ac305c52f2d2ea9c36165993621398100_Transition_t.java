 package com.tort.trade.model;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.hibernate.validator.NotNull;
 
 @Entity
 @Table(name = "TRADE_SRC")
 public class Transition {
 	private Long _id;
 	private Sales _from;
 	private Sales _to;
 	private Long _quant;
 	private Date _date;
 	private Sales _me;
 	private Good _good;
 	private BigDecimal _price = BigDecimal.ZERO;
 	
 	@Id
	@Column(name = "TRD_SEQ")
	@GeneratedValue(strategy = GenerationType.TABLE)
 	public Long getId() {
 		return _id;
 	}
 	public void setId(Long id) {
 		_id = id;
 	}
 	
 	@NotNull
 	@ManyToOne
 	@JoinColumn(name = "TRD_FROM")
 	public Sales getFrom() {
 		return _from;
 	}
 	public void setFrom(Sales from) {
 		_from = from;
 	}
 	
 	@NotNull
 	@ManyToOne
 	@JoinColumn(name = "TRD_TO")
 	public Sales getTo() {
 		return _to;
 	}
 	public void setTo(Sales to) {
 		_to = to;
 	}
 	
 	@NotNull
 	@Column(name = "TRD_QUANT")
 	public Long getQuant() {
 		return _quant;
 	}
 	public void setQuant(Long quant) {
 		_quant = quant;
 	}
 	
 	@NotNull
 	@Column(name = "TRD_DATE")
 	public Date getDate() {
 		return _date;
 	}
 	public void setDate(Date date) {
 		_date = date;
 	}
 	
 	@NotNull
 	@ManyToOne
 	@JoinColumn(name = "TRD_JREF")
 	public Sales getMe() {
 		return _me;
 	}
 	public void setMe(Sales me) {
 		_me = me;
 	}
 	
 	@NotNull
 	@ManyToOne
 	@JoinColumn(name = "TRD_MAT")
 	public Good getGood() {
 		return _good;
 	}
 	public void setGood(Good good) {
 		_good = good;
 	}
 	
 	@NotNull
 	@Column(name = "TRD_PRICE")
 	public BigDecimal getPrice() {
 		return _price;
 	}
 	public void setPrice(BigDecimal price) {
 		_price = price;
 	}	
 }
