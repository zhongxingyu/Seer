 package com.binout.soccer5.controller;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import com.binout.soccer5.entity.Player;
 import javax.ejb.Stateless;
 
 @Stateless
 public class PlayerEJB {
 
     @PersistenceContext
     EntityManager em;
 
     public void removePlayer(Long id) {
         Player p = em.find(Player.class, id);
         em.remove(p);
     }
 
    public Player findPLlayer(String name) {               
         TypedQuery<Player> query = em.createNamedQuery(Player.FIND_BY_NAME, Player.class);
         query.setParameter("name", name);
         List<Player> existing = query.getResultList();
         if (existing.isEmpty()) {
             return null;
         } else {
             return existing.get(0);
         }
     }
     
     public void registerPlayer(Player p) {               
         TypedQuery<Player> query = em.createNamedQuery(Player.FIND_BY_NAME, Player.class);
         query.setParameter("name", p.getName());
         List<Player> existing = query.getResultList();
         if (existing.isEmpty()) {
             em.persist(p);
         }
     }
 
     public List<Player> listPlayers() {
         TypedQuery<Player> query = em.createNamedQuery(Player.FIND_ALL, Player.class);
         return query.getResultList();
     }
 }
