 package org.tucana.repository;
 
import java.util.List;

 import org.springframework.data.jpa.repository.JpaRepository;
 import org.tucana.domain.Constellation;
 
 /**
  * Repository interface to handle the acess to the database
  */
 public interface ConstellationRepository extends JpaRepository<Constellation, Long> {
	
 }
