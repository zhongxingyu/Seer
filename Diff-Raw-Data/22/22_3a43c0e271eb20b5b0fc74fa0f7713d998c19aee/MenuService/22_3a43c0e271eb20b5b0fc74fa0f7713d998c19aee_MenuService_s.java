 package co.silbersoft.anchor.services;
 
 import co.silbersoft.anchor.dao.MenuDao;
 import co.silbersoft.anchor.dao.MenuItemDao;
 import co.silbersoft.anchor.exceptions.GenericDataException;
 import co.silbersoft.anchor.models.Menu;
 import co.silbersoft.anchor.models.MenuItem;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional(readOnly=true)
 public class MenuService {
     
     private Logger log = LoggerFactory.getLogger(MenuService.class);
     
     @Transactional
     public List<MenuItem> getAllByMenu(Long menuid){
         try{
             return menuItemDao.findItemsByMenu(menuid);
         } catch(RuntimeException e){
             throw new GenericDataException(e.getMessage());
         } catch(Exception e){
             throw new GenericDataException(e.getMessage());
         }
     }
    
     @Transactional
     public List<MenuItem> getAllByMenuAndCatagory(Long menuid, String catagory){
         try{
             return menuItemDao.findItemsByMenuAndCatagory(menuid, catagory);
         } catch(RuntimeException e){
             throw new GenericDataException(e.getMessage());
         } catch(Exception e){
             throw new GenericDataException(e.getMessage());
         }
     }
     
     @Transactional
     public List<Menu> getAllMenus(){
        try{
            return menuDao.getAll();
         } catch(RuntimeException e){
             throw new GenericDataException(e.getMessage());
         } catch(Exception e){
             throw new GenericDataException(e.getMessage());
         } 
     }
     
     @Transactional
     public List<String> getCatagoriesForMenu(Long menuid){
         try {
             return menuItemDao.findCatagoriesByMenu(menuid);
         }catch(RuntimeException e){
             throw new GenericDataException(e.getMessage());
         } catch(Exception e){
             throw new GenericDataException(e.getMessage());
         } 
     }
     
     @Transactional(readOnly=false)
     @PreAuthorize("hasRole('Administrator')")
     public void deleteMenuItem(MenuItem m){
         try {
             menuItemDao.delete(m);
         }catch(RuntimeException e){
             throw new GenericDataException(e.getMessage());
         } catch(Exception e){
             throw new GenericDataException(e.getMessage());
         } 
     }
     
     @Transactional(readOnly=false)
     @PreAuthorize("hasRole('Administrator')")
     public void deleteMenuItem(Long itemId){
         try {
             log.info("trying to delete item " + itemId);
             menuItemDao.deleteById(itemId);
         }catch(RuntimeException e){
             throw new GenericDataException(e.getMessage());
         } catch(Exception e){
             throw new GenericDataException(e.getMessage());
         }
     }
     
     @Transactional(readOnly=false)
     @PreAuthorize("hasRole('Administrator')")
     public Long createMenuItem(MenuItem item){
         try{
             menuItemDao.create(item);
            return menuItemDao.findItemId(item.getItemName());
         }catch(RuntimeException e){
             throw new GenericDataException(e.getMessage());
         } catch(Exception e){
             throw new GenericDataException(e.getMessage());
         }
     }
     
     @Transactional(readOnly=false)
     @PreAuthorize("hasRole('Administrator')")
     public void updateMenuItem(MenuItem item){
         try {
             menuItemDao.update(item);
         }catch(RuntimeException e){
             throw new GenericDataException(e.getMessage());
         } catch(Exception e){
             throw new GenericDataException(e.getMessage());
         }
     }
     
    @Autowired MenuDao menuDao;
    @Autowired MenuItemDao menuItemDao;
 }
