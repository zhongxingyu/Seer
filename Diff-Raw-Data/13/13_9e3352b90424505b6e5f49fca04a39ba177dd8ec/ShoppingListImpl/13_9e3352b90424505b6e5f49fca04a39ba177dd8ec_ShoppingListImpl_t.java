 package com.app.shopandmeals.models.services;
 
import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.app.shopandmeals.models.dao.IShoppingListDao;
 import com.app.shopandmeals.models.entity.ShopList;
 import com.mongodb.WriteResult;
 
 
 @Service("shoppingListService")
 public class ShoppingListImpl implements IShoppingListService {
 
 	
 	@Autowired
 	private IShoppingListDao shoppingListDao;
	
	static Logger logger = Logger.getLogger(ShoppingListImpl.class.getName());
 
 	@Override
 	public List<ShopList> findAll() {
 		return shoppingListDao.findAll();
 	}
 	
 	@Override
 	public List<ShopList> findAll(String username) {
 		List<ShopList> lists = shoppingListDao.findAll(username);
			if(lists == null || lists.size()==0){
	    		logger.info("No lists available");
				return new ArrayList<ShopList>();
			}
 	    	else
 	    		return lists;
 	}
 
 	@Override
 	public ShopList findById(String shopListId) {
 		return shoppingListDao.findById(shopListId);
 	}
 	
 	@Override
 	public ShopList create(ShopList shopList, String username) {
 	 	Random ran = new Random();
     	if(username!= null){
     		String[] users = new String[]{username};
     		shopList.setUsers(users);
     	}
     	shopList.setId(String.valueOf(ran.nextInt(Integer.MAX_VALUE) + 0));
     	shopList.setDate(new Date());
 		return shoppingListDao.create(shopList);
 		
 	}
 
 	@Override
 	public void delete(ShopList shopList) {
 		shoppingListDao.delete(shopList);
 		
 	}
 
 	@Override
 	public WriteResult update(ShopList shopList) {
 		return shoppingListDao.update(shopList);
 	}
 	
 	@Override
 	public WriteResult updateItems(String id, String[] description) {
 		return shoppingListDao.updateItems(id, description);
 	}
 
 	@Override
 	public WriteResult updateListShared(String id, String username) {
 		return shoppingListDao.updateListShared(id, username);
 	}
 	
 }
