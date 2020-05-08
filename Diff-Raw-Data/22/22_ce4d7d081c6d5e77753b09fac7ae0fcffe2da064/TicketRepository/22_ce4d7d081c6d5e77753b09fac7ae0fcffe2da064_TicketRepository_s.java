 package com.ateneo.uaaptickets.repository;
 
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.stereotype.Repository;
 
 import com.ateneo.uaaptickets.entity.Ticket;
 
 @Repository
 public interface TicketRepository extends JpaRepository<Ticket, Integer>{

 }
