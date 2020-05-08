 package us.digitalasylum.repository;
 
 
 import org.springframework.data.repository.PagingAndSortingRepository;
 import org.springframework.data.repository.query.Param;
 import us.digitalasylum.repository.entities.Item;
 
 import java.util.List;
 
 public interface ItemRepository extends PagingAndSortingRepository<Item,Long> {
     public Item findByLink(@Param("link") String link);
     public Item findByGuid(@Param("guid") String guid);
     //public List<Item> findByFeedCategoryId(@Param("categoryId") Long id);
    public List<Item> findByChannelFeedCategory_Id(@Param("categoryId") Long id);
 }
