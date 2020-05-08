 package com.omartech.tdg.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.omartech.tdg.mapper.BrandMapper;
 import com.omartech.tdg.model.Brand;
 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.utils.ProductStatus;
 
 @Service
 public class BrandService {
 	@Autowired
 	private BrandMapper brandMapper;
 	public List<Brand> getBrandListByPage(Page page){
 		return brandMapper.getBrandListByPage(page);
 	}
 	
 	public List<Brand> getBrandListByPageAndSellerId(int sellerId, Page page){
 		return brandMapper.getBrandListByPageAndSellerId(sellerId, page);
 	}
 	public List<Brand> getBrandListBySellerId(int sellerId){
 		return brandMapper.getBrandListBySellerId(sellerId);
 	}
 	
 	public List<Brand> getBrandListByPageAndStatus(Page page, int status){
 		return brandMapper.getBrandListByPageAndStatus(page, status);
 	}
 	public List<Brand> getBrandList(){
 		return brandMapper.getBrandList();
 	}
 	public Brand getBrandById(int id){
 		return brandMapper.getBrandById(id);
 	}
 	
 	public void insertBrand(Brand brand){
 		brand.setStatus(ProductStatus.InProductCreation);
 		brandMapper.insertBrand(brand);
 	}
 	
 	public void deleteBrand(int id){
 		brandMapper.deleteBrand(id);
 	}
 	
 	public void updateBrand(Brand brand){
 		brandMapper.updateBrand(brand);
 	}
 	
 	public void updateBrandStatus(int brandId, int status){
 		Brand brand = brandMapper.getBrandById(brandId);
 		brand.setStatus(status);
 		updateBrand(brand);
 	}
 
 	public BrandMapper getBrandMapper() {
 		return brandMapper;
 	}
 
 	public void setBrandMapper(BrandMapper brandMapper) {
 		this.brandMapper = brandMapper;
 	}
 	
 }
