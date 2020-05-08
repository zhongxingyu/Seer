 package com.sk.domain.coupon;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorType;
 import javax.persistence.Entity;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Table;
 
 import com.sk.domain.BaseEntity;
 
 @Entity
@Table(name="catalog")
 @Inheritance(strategy=InheritanceType.SINGLE_TABLE)
 @DiscriminatorColumn(
 	    name="couponType",
 	    discriminatorType=DiscriminatorType.STRING
 	)
 public abstract class Coupon extends BaseEntity{
 
 	private static final long serialVersionUID = -1836790232427858843L;
 	
 	public abstract void setCouponHolder(CouponHolder couponHolder);
 	public abstract CouponHolder getCouponHolder();
 
 	@Column(length=10)
 	private String couponString;
 	
 	private double discount;
 	private Boolean used;
 	
 	public String getCouponString() {
 		return couponString;
 	}
 	public void setCouponString(String couponString) {
 		this.couponString = couponString;
 	}
 	public double getDiscount() {
 		return discount;
 	}
 	public void setDiscount(double discount) {
 		this.discount = discount;
 	}
 	public Boolean isUsed() {
 		return used;
 	}
 	public Boolean getUsed() {
 		return used;
 	}
 	public void setUsed(Boolean used) {
 		this.used = used;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result
 				+ ((couponString == null) ? 0 : couponString.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj){
 			return true;
 		}
 		if(!(obj instanceof Coupon)){
 			return false;
 		}
 		Coupon other = (Coupon) obj;
 		if (couponString == null || !couponString.equals(other.couponString)){
 			return false;
 		}
 		return true;
 	}
 	
 }
