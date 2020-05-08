 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.endy.belajar.jee.banking.service;
 
 import com.artivisi.endy.belajar.jee.banking.entity.Nasabah;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.ejb.LocalBean;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 /**
  *
  * @author student14
  */
 @Stateless
 @LocalBean
 public class BankingServiceBean {
     @PersistenceContext(unitName = "belajar-jee-ejbPU")
     private EntityManager em;
 
     public void simpan(Nasabah n){
        em.persist(n);
     }
     
     public Nasabah cariNasabahById(Long id){
         return em.find(Nasabah.class, id);
     }
     
     public Long hitungSemuaNasabah(){
         return (Long) em.createQuery("select count(n) from Nasabah n")
                 .getSingleResult();
     }
     
     public List<Nasabah> cariSemuaNasabah(Integer start, Integer rows){
         return em.createQuery("select n from Nasabah n order by n.nama")
                 .setFirstResult(start)
                 .setMaxResults(rows)
                 .getResultList();
     }
 }
