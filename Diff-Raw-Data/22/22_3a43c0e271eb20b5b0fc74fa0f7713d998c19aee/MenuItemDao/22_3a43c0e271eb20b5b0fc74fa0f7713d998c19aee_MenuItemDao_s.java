 package co.silbersoft.anchor.dao;
 
 import co.silbersoft.anchor.models.MenuItem;
 import java.util.List;
 
 public interface MenuItemDao extends Dao<MenuItem>{
     
     List<MenuItem> findItemsByMenu(Long menuId);
     List<MenuItem> findItemsByMenuAndCatagory(Long menuid, String catagory);
     List<String> findCatagoriesByMenu(Long menuid);
    Long findItemId(String name);
     void deleteById(Long menuItemId);
 }
