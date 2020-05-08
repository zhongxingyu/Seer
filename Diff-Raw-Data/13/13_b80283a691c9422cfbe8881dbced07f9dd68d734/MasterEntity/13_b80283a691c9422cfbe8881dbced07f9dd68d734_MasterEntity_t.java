 package com.anderscore.springbatch.article.domain;
 // Generated 12.04.2010 14:29:28 by Hibernate Tools 3.2.4.GA
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
 
 @Entity
 public class MasterEntity implements java.io.Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1125765863392543308L;
 
 	@Id
 	@Column(nullable = false, precision = 2, scale = 0)
 	private long id;
 
 	@Column(nullable = false, precision = 8, scale = 0)
 	private int masterIdentifier;
 	
 	@Column(precision = 9, scale = 0)
 	private Integer integerField;
 	
 	@Column(nullable = false, precision = 2, scale = 0)
 	private byte byteField;
 	
 	@Column(length = 7)
 	private Date dateField;
 
 	@Column(length = 10)
 	private String stringField;
 
 	@Column(precision = 1, scale = 0)
 	private Boolean booleannField;
 
 	@Column(precision = 4, scale = 0)
 	private Short shortField;
 
 	@Column(length = 1)
 	private Character characterField;
 
 	@Column(precision = 18)
 	private BigDecimal bigDecimalField;
 
	@OneToOne
 	private DetailEntity detailEntity;
 	
 	public long getId() {
 		return id;
 	}
 
 	public int getMasterIdentifier() {
 		return masterIdentifier;
 	}
 
 	public void setMasterIdentifier(int masterIdentifier) {
 		this.masterIdentifier = masterIdentifier;
 	}
 
 	public Integer getIntegerField() {
 		return integerField;
 	}
 
 	public void setIntegerField(Integer integerField) {
 		this.integerField = integerField;
 	}
 
 	public byte getByteField() {
 		return byteField;
 	}
 
 	public void setByteField(byte byteField) {
 		this.byteField = byteField;
 	}
 
 	public Date getDateField() {
 		return dateField;
 	}
 
 	public void setDateField(Date dateField) {
 		this.dateField = dateField;
 	}
 
 	public String getStringField() {
 		return stringField;
 	}
 
 	public void setStringField(String stringField) {
 		this.stringField = stringField;
 	}
 
 	public Boolean getBooleannField() {
 		return booleannField;
 	}
 
 	public void setBooleannField(Boolean booleannField) {
 		this.booleannField = booleannField;
 	}
 
 	public Short getShortField() {
 		return shortField;
 	}
 
 	public void setShortField(Short shortField) {
 		this.shortField = shortField;
 	}
 
 	public Character getCharacterField() {
 		return characterField;
 	}
 
 	public void setCharacterField(Character characterField) {
 		this.characterField = characterField;
 	}
 
 	public BigDecimal getBigDecimalField() {
 		return bigDecimalField;
 	}
 
 	public void setBigDecimalField(BigDecimal bigDecimalField) {
 		this.bigDecimalField = bigDecimalField;
 	}
 
 	public DetailEntity getDetailEntity() {
 		return detailEntity;
 	}
 
 	public void setDetailEntity(DetailEntity detailEntity) {
 		this.detailEntity = detailEntity;
 	}
 }
