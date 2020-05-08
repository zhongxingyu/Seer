 package com.sk.service;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.sk.domain.Shopper;
 import com.sk.domain.coupon.Coupon;
 import com.sk.domain.coupon.ShopperCoupon;
 import com.sk.domain.dao.CouponDao;
 
 @Service
 public class CouponService {
 
 	@Autowired
 	private CouponDao couponDao;
 	
	public CouponService(){}
	
 	public CouponService(CouponDao couponDao) {
 		this.couponDao = couponDao;
 	}
 
 	public void createCouponForShopper(Shopper shopper, double discountAmount, int numberOfCoupons) {
 		
 		for (int i = 0; i < numberOfCoupons; i++) {
 			ShopperCoupon coupon = new ShopperCoupon();
 			coupon.setDiscount(discountAmount);
 			coupon.setShopper(shopper);
 			coupon.setUsed(Boolean.FALSE);
 
 			String couponString = prepareCouponString();
 			
 			coupon.setCouponString(couponString);
 			couponDao.persist(coupon);
 		}
 	}
 
 	private String prepareCouponString() {
 		String couponString;
 		Coupon existingCoupon;
 		do{
 			couponString = RandomStringUtils.randomAlphabetic(10);
 			existingCoupon = couponDao.findByCouponString(couponString);
 		}while(existingCoupon != null);
 		return couponString;
 	}
 
 }
