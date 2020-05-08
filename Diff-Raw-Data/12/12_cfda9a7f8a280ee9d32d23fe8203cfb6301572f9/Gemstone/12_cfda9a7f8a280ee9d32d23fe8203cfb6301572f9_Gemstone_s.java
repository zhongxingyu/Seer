 /**
  * 
  */
 package com.openappengine.model.product;
 
 import java.math.BigDecimal;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.PrimaryKeyJoinColumn;
 
 /**
  * @author hrishi
  * 
  */
 @Entity(name="prod_product_gemstone")
 @PrimaryKeyJoinColumn(name="PD_PRODUCT_ID")
 public class Gemstone extends Product {
 	
 	private static final long serialVersionUID = 1L;
 	
 	@Column(name="PD_STONE_ID")
 	private String stoneId;
 	
 	@Column(name="PD_SHAPE")
 	private String shape;
 	
 	@Column(name="PD_CARAT",precision=10, scale=6)
 	private BigDecimal carat;
 	
 	@Column(name="PD_COLOR")	
 	private String color;
 	
 	@Column(name="PD_CUT")
 	private String cut;
 	
 	@Column(name="PD_CLARITY")
 	private String clarity;
 	
 	@Column(name="PD_GRADE")
 	private String grade;
 	
 	@Column(name="PD_STAR_SHARPNESS")
 	private String starSharpness;
 
 	@Column(name="PD_ORIGIN")
 	private String origin;
 	
 	@Column(name="PD_HARDNESS")
 	private String hardness;
 	
 	@Column(name="PD_TREATMENT")
 	private String treatment;
 	
 	@Column(name="PD_IMAGE_URL")
 	private String imageUrl;
 	
 	public String getShape() {
 		return shape;
 	}
 
 	public void setShape(String shape) {
 		this.shape = shape;
 	}
 
 	public BigDecimal getCarat() {
 		return carat;
 	}
 
 	public void setCarat(BigDecimal carat) {
 		this.carat = carat;
 	}
 
 	public String getColor() {
 		return color;
 	}
 
 	public void setColor(String color) {
 		this.color = color;
 	}
 
 	public String getCut() {
 		return cut;
 	}
 
 	public void setCut(String cut) {
 		this.cut = cut;
 	}
 
 	public String getClarity() {
 		return clarity;
 	}
 
 	public void setClarity(String clarity) {
 		this.clarity = clarity;
 	}
 
 	public String getGrade() {
 		return grade;
 	}
 
 	public void setGrade(String grade) {
 		this.grade = grade;
 	}
 
 	public String getStoneId() {
 		return stoneId;
 	}
 
 	public void setStoneId(String stoneId) {
 		this.stoneId = stoneId;
 	}
 
 	public String getStarSharpness() {
 		return starSharpness;
 	}
 
 	public void setStarSharpness(String starSharpness) {
 		this.starSharpness = starSharpness;
 	}
 
 	public String getOrigin() {
 		return origin;
 	}
 
 	public void setOrigin(String origin) {
 		this.origin = origin;
 	}
 
 	public String getHardness() {
 		return hardness;
 	}
 
 	public void setHardness(String hardness) {
 		this.hardness = hardness;
 	}
 
 	public String getTreatment() {
 		return treatment;
 	}
 
 	public void setTreatment(String treatment) {
 		this.treatment = treatment;
 	}
 
 	public String getImageUrl() {
 		return imageUrl;
 	}
 
 	public void setImageUrl(String imageUrl) {
 		this.imageUrl = imageUrl;
 	}
 }
