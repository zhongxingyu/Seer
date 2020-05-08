 package com.dzer6.ga3.repository;
 
 import com.dzer6.ga3.domain.PreviousOpponent;
 import com.dzer6.ga3.domain.User;
 import java.util.List;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.CrudRepository;
 import org.springframework.data.repository.query.Param;
 import org.springframework.transaction.annotation.Transactional;
 
 @Transactional(readOnly = true)
 public interface PreviousOpponentRepository extends CrudRepository<PreviousOpponent, Long> {
 
     List<PreviousOpponent> findByUserAndOpponent(User user, User opponent);
 
    @Query("select count (ub) from UserBan ub where ub.user = :user")
     long countByUser(@Param("user") User user);
     
 }
