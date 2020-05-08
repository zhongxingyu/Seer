 package spj.database.glavarazdjel;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import spj.database.SpjRepository;
 import spj.shared.domain.GlavaRazdjel;
 import spj.shared.domain.SpjDomainModel;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import java.util.List;
 
 @Repository("glavaRazdjelRepository")
 @Transactional
 public class GlavaRazdjelRepository implements SpjRepository
 {
     @PersistenceContext
     EntityManager entityManager;
 
     @Transactional
     public void create(SpjDomainModel spjDomainModel)
     {
         GlavaRazdjel glavaRazdjel = cast(spjDomainModel);
         entityManager.persist(GlavaRazdjelMapper.createDTO(glavaRazdjel));
     }
 
     @Transactional
     public GlavaRazdjel read(long glavaRazdjelId) throws NoSuchFieldException
     {
         GlavaRazdjelDTO glavaRazdjelDTO = entityManager.find(GlavaRazdjelDTO.class, glavaRazdjelId);
 
         if (glavaRazdjelDTO == null)
         {
             throw new NoSuchFieldException("GlavaRazdjel with id " + glavaRazdjelId + "doesn't exists");
         }
         else
         {
             return GlavaRazdjelMapper.createDomainObject(glavaRazdjelDTO);
         }
     }
 
     @Override
     public boolean exists(long glavaRazdjelId)
     {
         return entityManager.find(GlavaRazdjelDTO.class, glavaRazdjelId) != null;
     }
 
     @Override
     public int count()
     {
         return findAll().size();
     }
 
     @Transactional
     public void update(SpjDomainModel spjDomainModel)
     {
         GlavaRazdjel glavaRazdjel = cast(spjDomainModel);
         entityManager.merge(GlavaRazdjelMapper.createDTO(glavaRazdjel));
     }
 
     @Transactional
     public void delete(SpjDomainModel spjDomainModel)
     {
         GlavaRazdjel glavaRazdjel = cast(spjDomainModel);
         entityManager.remove(GlavaRazdjelMapper.createDTO(glavaRazdjel));
     }
 
     @Override
     public List<? extends SpjDomainModel> findAll()
     {
         TypedQuery<GlavaRazdjelDTO> query = entityManager.createQuery("SELECT * FROM glava_razdjel", GlavaRazdjelDTO.class);
         List<GlavaRazdjelDTO> list= query.getResultList();
         return GlavaRazdjelMapper.createDomainObject(list);
     }
 
     private GlavaRazdjel cast(SpjDomainModel spjDomainModel)
     {
        if(!spjDomainModel.getClass().equals(GlavaRazdjel.class))
         {
             throw new IllegalArgumentException("spjDomainModel not of type GlavaRazdjel");
         }
         return (GlavaRazdjel) spjDomainModel;
     }
 }
