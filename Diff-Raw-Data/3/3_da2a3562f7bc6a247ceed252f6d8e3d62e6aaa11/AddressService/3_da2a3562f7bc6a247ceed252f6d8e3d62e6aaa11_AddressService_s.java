 package com.kedut.directory.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.kedut.directory.dao.AddressDao;
 import com.kedut.directory.exception.NotFoundException;
 import com.kedut.directory.model.Address;
 import com.kedut.directory.model.Business;
 
 
 @Component
 @Transactional
 public class AddressService {
 	
 	@Autowired
 	private AddressDao addressDao;
 
 	public List<Address> getAddressByBusinessId(Long id) throws NotFoundException{
 		return addressDao.getAddressesByBusinessId(id);
 		
 	}
 
 	public void saveAddress(Address address){
 		addressDao.saveAddress(address);
 		
 	}
 
 
 }
