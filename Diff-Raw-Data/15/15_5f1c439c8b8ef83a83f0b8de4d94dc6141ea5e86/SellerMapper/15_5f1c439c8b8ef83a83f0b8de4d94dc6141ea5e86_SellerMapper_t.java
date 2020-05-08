 package com.omartech.tdg.mapper;
 
 import java.util.List;
 
import org.apache.ibatis.annotations.Param;

 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.model.Seller;
 
 public interface SellerMapper {
 	
 	Seller getSellerByEmail(String email);
 	
 	Seller getSellerByEmailAndPassword(String email,String password);
 
 	void insertSeller(Seller seller);
 	
 	void updateSeller(Seller seller);
 	
	public List<Seller> getSellerListByPage(@Param("page")Page page);
 	
 	public Seller getSellerById(int id);
 }
