 package com.book.identification.task;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import com.book.identification.dao.CategoryDAO;
 import com.book.identification.dao.DAOFactory;
 import com.book.identification.dao.VolumeInfoDAO;
 import com.book.identification.model.Category;
 import com.book.identification.model.VolumeInfo;
 
 public class CreateTreeOfCategories {
 	final static Logger logger = LogManager.getLogger(CreateTreeOfCategories.class);
 	CategoryDAO categoryDAO = DAOFactory.getInstance().getCategoryDAO();
 	
 	public void execute() {
 		long startTime = System.currentTimeMillis();
 		logger.info( CreateTreeOfCategories.class + " start at " + new Date()  );
 		VolumeInfoDAO volumeInfoDAO = DAOFactory.getInstance().getVolumeInfoDAO();
 		List<VolumeInfo> volumeInfos = volumeInfoDAO.findAll();
 		for (VolumeInfo volumeInfo : volumeInfos) {
 			logger.info("Categorizing " + volumeInfo.getTitle());
 			Set<String> stringCategories = volumeInfo.getCategories();
 				
 			
 			for (String stringComposedCategorie : stringCategories) {
 				
 				String[] splitCategories = stringComposedCategorie.replace("[", "").replace("]", "").split("/");
 				
 				Category c = setParents(splitCategories);
 				categoryDAO.getSession().getTransaction().begin();
 					categoryDAO.makePersistent(c);
 				categoryDAO.getSession().getTransaction().commit();
 
 				categoryDAO.getSession().getTransaction().begin();
 					setChildrens(c);
 				categoryDAO.getSession().getTransaction().commit();
 				
 				categoryDAO.getSession().getTransaction().begin();
 					VolumeInfo volumeInfo2 = (VolumeInfo) categoryDAO.getSession().get(VolumeInfo.class, volumeInfo.getEntityId());
 					volumeInfo2.getCategoriess().add(c);
 				categoryDAO.getSession().getTransaction().commit();
 			}
 		}
 		long endTime = System.currentTimeMillis();
 		logger.info( CreateTreeOfCategories.class + " end at " + new Date()  );
 		logger.info( CreateTreeOfCategories.class + " running " + ((endTime - startTime)/1000)  + "seg");
 	}
 
 	private  void setChildrens(Category c) {
 		if(c.getParent()!=null){
 			
 			if(c.getParent().getSubCategories() == null)
 				c.getParent().setSubCategories(new HashSet<Category>());
 			
 			c.getParent().getSubCategories().add(c);
 			setChildrens(c.getParent());
 		}
 		
 	}
 
 	private  Category setParents(String[] splitCategories){
 		int length = splitCategories.length;
 		if(length>0){
 			String stringCategory = StringUtils.trim(splitCategories[length-1]);
 			String[] subarray = null;
 			if(length>1){
 				subarray = ArrayUtils.subarray(splitCategories, 0, length-1);
 			}
 			return buscameOcreame(stringCategory,subarray);
 		}else{
 			return null;
 		}
 	}
 	private  Category buscameOcreame(String stringCategory,String[] subarray) {
 		Category retrieveCategory = categoryDAO.retrieveCategory(stringCategory, subarray);
 		if(retrieveCategory == null){
 			retrieveCategory = new Category();
 			retrieveCategory.setCategory(stringCategory);
			if(subarray != null && subarray.length > 0){
				retrieveCategory.setParent(setParents(subarray));
			}
 		}
 		return retrieveCategory;
 	}
 
 	
 }
