 package com.tda.service;
 
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import com.tda.model.Item;
 import com.tda.persistence.ItemDAO;
 
 public class ItemServiceImpl implements ItemService {
 	@Autowired
 	ItemDAO itemDAO;
 
 	public void save(Item item) {
 		itemDAO.save(item);
 	}
 
 	public void delete(Item item) {
 		itemDAO.delete(item);
 	}
 
 	public void update(Item item) {
 		itemDAO.update(item);
 	}
 
 	public Item findById(Long id) {
 		return itemDAO.findById(id);
 	}
 
 	public List<Item> findAll() {
 		return itemDAO.findAll();
 	}
 
 	public List<Item> findByName(String name) {
 		return itemDAO.findByName(name);
 	}
 	
 	public List<Item> findByExample(Item example) {
 		return itemDAO.findByExample(example);
 	}
 
 	public List<Item> findByDescription(String description) {
 		return itemDAO.findByDescription(description);
 	}
 
 	public List<Item> findByRange(int minQ, int maxQ) {
 		return itemDAO.findByRange(minQ, maxQ);
 	}
 
 }
