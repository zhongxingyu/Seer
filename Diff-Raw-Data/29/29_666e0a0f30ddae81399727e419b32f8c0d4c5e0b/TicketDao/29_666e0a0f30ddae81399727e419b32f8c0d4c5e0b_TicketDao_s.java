 package org.lifecycle.persistence;
 
 import org.hibernate.SessionFactory;
 import org.lifecycle.domain.Ticket;
 
 import java.util.List;
 
 public class TicketDao extends AbstractDao<Ticket> {
 
     public TicketDao(SessionFactory sessionFactory) {
         super(sessionFactory);
     }
 
     public List<Ticket> findByLabel(String label) {
        return list(sql("select * from tickets t where t.id in (select l.ticket_id from LABELS l where l.label = '" + label + "')"));
     }
 }
