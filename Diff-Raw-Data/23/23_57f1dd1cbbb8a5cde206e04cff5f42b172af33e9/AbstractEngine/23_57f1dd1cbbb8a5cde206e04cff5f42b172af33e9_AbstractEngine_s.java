 package ru.terra.server.engine;
 
 import org.apache.log4j.Logger;
 import ru.terra.server.db.controllers.AbstractJpaController;
 import ru.terra.server.dto.CommonDTO;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class AbstractEngine<Entity, Dto extends CommonDTO> {
     private Logger logger = Logger.getLogger(AbstractEngine.class);
     protected AbstractJpaController<Entity> jpaController;
     protected Class<? extends Entity> entityClass;
 
     public AbstractEngine(AbstractJpaController<Entity> jpaController) {
         this.jpaController = jpaController;
         entityClass = jpaController.getEntityClass();
     }
 
     public List<Entity> listBeans(Boolean all, Integer page, Integer perPage) {
         try {
             return jpaController.list(all, page, perPage);
         } catch (Exception e) {
             logger.error("Unable to list beans", e);
             return new ArrayList<>();
         }
     }
 
     public Entity getBean(Integer id) {
         try {
             return jpaController.get(id);
         } catch (Exception e) {
             logger.error("Unable to get bean", e);
             return null;
         }
     }
 
     public boolean updateBean(Entity bean) {
         try {
             jpaController.update(bean);
             return true;
         } catch (Exception e) {
             logger.error("Unable to update bean", e);
             return false;
         }
     }
 
     public boolean updateDTO(Dto dto) {
         Entity entity = getBean(dto.id);
         if (entity == null)
             return false;
         dtoToEntity(dto, entity);
         updateBean(entity);
         return true;
     }
 
     public Entity createBean(Entity bean) {
         try {
             jpaController.create(bean);
             return bean;
         } catch (Exception e) {
             logger.error("Unable to create bean", e);
             return null;
         }
     }
 
 //    /**
 //     * Create new entity in base by dto
 //     * */
 //    public Dto createDto(Dto dto) {
 //        Entity ret = null;
 //        try {
 //            ret = entityClass.newInstance();
 //        } catch (InstantiationException e) {
 //            logger.error(e);
 //        } catch (IllegalAccessException e) {
 //            logger.error(e);
 //        }
 //        if (ret == null)
 //            return null;
 //        dtoToEntity(dto, ret);
 //        createBean(ret);
 //        return entityToDto(ret);
 //    }
 
     public List<Dto> listDtos(Boolean all, Integer page, Integer perPage) {
        List<Dto> ret = new ArrayList<>();
        for (Entity entity : listBeans(all, page, perPage)) {
            ret.add(entityToDto(entity));
         }
        return ret;
     }
 
     public abstract Dto getDto(Integer id);
 
     public boolean delete(Integer id) {
         try {
             jpaController.delete(id);
             return true;
         } catch (Exception e) {
             logger.error("Unable to delete bean", e);
             return false;
         }
     }
 
     public abstract void dtoToEntity(Dto dto, Entity entity);
 
     public abstract Dto entityToDto(Entity entity);
 }
