 package com.anderscore.springbatch.article.domain;
 // Generated 12.04.2010 14:29:28 by Hibernate Tools 3.2.4.GA
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 
 @Entity
 public class DetailEntity implements java.io.Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3392579566515652656L;
 
 	@SequenceGenerator(name="DetailEntity_Generator", sequenceName="DetailEntity_Sequence")
 	@Id @GeneratedValue(generator="DetailEntity_Generator")
 	@Column(nullable = false, precision = 10, scale = 0)
 	private long id;
 
 	@Column(nullable = false, length = 7)
 	private Date detailIdentifier;
 	
 	@Column(nullable = false, precision = 2, scale = 0)
 	private byte byteField;
 	
 	@Column(nullable = false, precision = 3, scale = 0)
 	private short shortField;
 	
	@ManyToOne
 	private MasterEntity masterEntity;
 	
 	public long getId() {
 		return id;
 	}
 
 	public Date getDetailIdentifier() {
 		return detailIdentifier;
 	}
 
 	public void setDetailIdentifier(Date detailIdentifier) {
 		this.detailIdentifier = detailIdentifier;
 	}
 
 	public byte getByteField() {
 		return byteField;
 	}
 
 	public void setByteField(byte byteField) {
 		this.byteField = byteField;
 	}
 
 	public short getShortField() {
 		return shortField;
 	}
 
 	public void setShortField(short shortField) {
 		this.shortField = shortField;
 	}
 
 	public MasterEntity getMasterEntity() {
 		return masterEntity;
 	}
 
 	public void setMasterEntity(MasterEntity masterEntity) {
 		this.masterEntity = masterEntity;
 	}
 }
