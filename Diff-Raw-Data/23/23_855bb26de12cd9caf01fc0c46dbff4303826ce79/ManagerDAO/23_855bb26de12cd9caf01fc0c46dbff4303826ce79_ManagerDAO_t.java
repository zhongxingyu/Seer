 package com.ebaytools.kernel.dao;
 
 import com.ebaytools.kernel.entity.ItemProperties;
 import com.ebaytools.start.AppContextManager;
 import com.ebaytools.util.Fields;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class ManagerDAO {
     private static ManagerDAO instance;
     private ProductDAO productDAO;
     private ItemDAO itemDAO;
     private ItemPropertiesDAO itemPropetiesDAO;
     private SystemSettingDAO systemSettingDAO;
     private FilterConditionsDAO filterConditionsDAO;
     private FilterDAO filterDAO;
     private FilterValueDAO filterValueDAO;
     private FileSearchingDAO fileSearchingDAO;
 
     private ManagerDAO() {
         ApplicationContext ctx = AppContextManager.getInstance().getCtx();
         this.productDAO = (ProductDAO) ctx.getBean("productDAO");
         this.itemDAO = (ItemDAO) ctx.getBean("itemDAO");
         this.itemPropetiesDAO = (ItemPropertiesDAO) ctx.getBean("itemPropetiesDAO");
         this.systemSettingDAO = (SystemSettingDAO) ctx.getBean("systemSettingDAO");
         this.filterDAO = (FilterDAO) ctx.getBean("filterDAO");
         this.filterConditionsDAO = (FilterConditionsDAO) ctx.getBean("filterConditionsDAO");
         this.filterValueDAO = (FilterValueDAO) ctx.getBean("filterValueDAO");
         this.fileSearchingDAO = (FileSearchingDAO) ctx.getBean("fileSearchingDAO");
     }
 
     public static synchronized ManagerDAO getInstance() {
         if (instance == null) {
             instance = new ManagerDAO();
         }
         return instance;
     }
 
    public synchronized FilterConditionsDAO getFilterConditionsDAO() {
         return filterConditionsDAO;
     }
 
    public synchronized FilterDAO getFilterDAO() {
         return filterDAO;
     }
 
    public synchronized FilterValueDAO getFilterValueDAO() {
         return filterValueDAO;
     }
 
    public synchronized ProductDAO getProductDAO() {
         return productDAO;
     }
 
    public synchronized ItemDAO getItemDAO() {
         return itemDAO;
     }
 
    public synchronized ItemPropertiesDAO getItemPropetiesDAO() {
         return itemPropetiesDAO;
     }
 
    public synchronized SystemSettingDAO getSystemSettingDAO() {
         return systemSettingDAO;
     }
 
    public synchronized FileSearchingDAO getFileSearchingDAO() {
         return fileSearchingDAO;
     }
 
     public static ItemProperties buildItemProperties(Long itemId, Fields name, String value, String type) {
         ItemProperties itemProperties = new ItemProperties();
         itemProperties.setItemId(itemId);
         itemProperties.setName(name.getKey());
         itemProperties.setType(type);
         itemProperties.setValue(value);
         return itemProperties;
     }
 }
