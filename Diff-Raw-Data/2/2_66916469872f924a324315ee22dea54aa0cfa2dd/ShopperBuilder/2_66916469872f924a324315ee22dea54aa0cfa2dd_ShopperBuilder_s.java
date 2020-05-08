 package com.sk.util.builder;
 
 import org.apache.commons.lang.RandomStringUtils;
 
 import com.sk.domain.Shopper;
 
 public class ShopperBuilder extends BaseBuilder<Shopper, ShopperBuilder>{
 
 	private String email = RandomStringUtils.random(10);
	private String name = RandomStringUtils.random(10);
 	private String encryptedCardNo;
 	private String encryptedCVC;
 	
 	public ShopperBuilder email(String email){
 		this.email = email;
 		return this;
 	}
 	
 	public ShopperBuilder name(String name){
 		this.name = name;
 		return this;
 	}
 	
 	public ShopperBuilder cardNo(String encryptedCardNo){
 		this.encryptedCardNo= encryptedCardNo;
 		return this;
 	}
 	
 	public ShopperBuilder cvc(String encryptedCVC){
 		this.encryptedCVC = encryptedCVC;
 		return this;
 	}
 	
 	@Override
 	public Shopper doBuild(){
 		Shopper shopper = new Shopper();
 		shopper.setEmail(email);
 		shopper.setName(name);
 		shopper.setEncryptedCardNo(encryptedCardNo);
 		shopper.setEncryptedCVC(encryptedCVC);
 		
 		return shopper;
 	}
 }
