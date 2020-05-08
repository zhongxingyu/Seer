 package com.domain.shop;
 
 import com.domain.customer.User;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.repository.CrudRepository;
 import org.springframework.data.repository.PagingAndSortingRepository;
 
 import java.util.List;
 
 /**
  * @author Stanislav Kurilin
  */
 public interface ProductRepository extends CrudRepository<Product, String>, PagingAndSortingRepository<Product, String> {
     List<Product> findBySeller(User user);
 
     @Override
     Page<Product> findAll(Pageable pageable);
 
    @Override
    Product save(Product entity);
 
     Product findByTitle(String title);
     //TODO: [stas] pageable retrieve with custom soring (by price/date)
 }
