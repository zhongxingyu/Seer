 package com.ateneo.uaaptickets.repository;
 
import java.util.List;

 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.stereotype.Repository;
 
import com.ateneo.uaaptickets.entity.Event;
import com.ateneo.uaaptickets.entity.Student;
 import com.ateneo.uaaptickets.entity.Ticket;
 
 @Repository
 public interface TicketRepository extends JpaRepository<Ticket, Integer>{
	List<Ticket> findByEvent(Event event);
	List<Ticket> findByStudent(Student student);
 }
