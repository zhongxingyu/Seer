 package com.zarcillo.dao;
 
 import com.zarcillo.domain.Menu;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author saisa
  */
 @Repository
 public class MenuDAOImpl implements MenuDAO{
     
     @PersistenceContext(type = PersistenceContextType.EXTENDED)
     private EntityManager em;
 
     @Override
     public Menu busqueda(Integer idmenu) {
         return (Menu) em.createNamedQuery("Menu.findByIdmenu").setParameter("idmenu", idmenu).getSingleResult();
     }
 
     @Override
     public List<Menu> listaPorIdmodulo(Integer idmodulo) {
         return em.createNamedQuery("Menu.findByIdmodulo").setParameter("idmodulo",idmodulo).getResultList();
     }
 
     @Override
     public List<Menu> listaPorIdmoduloPorNnivel1(Integer idmodulo, Integer nnivel) {
        return em.createNamedQuery("Menu.findByIdmoduloByNnivel1").setParameter("idmodulo",idmodulo).setParameter("nnivel1", nnivel).getResultList();
     }
     
     
     
     
 }
