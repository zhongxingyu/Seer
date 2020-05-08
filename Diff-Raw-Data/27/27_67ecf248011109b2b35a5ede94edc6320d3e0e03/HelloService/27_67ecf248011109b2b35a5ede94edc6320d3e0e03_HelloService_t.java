 package com.george.ejb;
 
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.ejb.LocalBean;
 import javax.ejb.SessionContext;
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.inject.Named;
import javax.persistence.PersistenceException;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 
 import com.george.cdi.Cliente;
 import com.george.cdi.CustomerDatabase;
 import com.george.jpa.Customer;
 import com.george.jpa.dao.DAO;
 
 /**
  * Session Bean implementation class HelloService
  */
 @Stateless
 @LocalBean
 @Path("/hello")
 @Named
 public class HelloService {
     
     @Resource
     private SessionContext sessionContext;
     
     @Inject @CustomerDatabase
     private Map<String,AtomicInteger> customerMap;
     
     @Inject
     private DAO<Customer> dao;
     
     @Inject
     private Cliente cliente;
     
     @PostConstruct
     public void carregar() {
         System.out.println("Carregou");
     }
     
     /**
      * Default constructor. 
      */
     public HelloService() {
     }
     
     @GET
     public String getName(@QueryParam("name") @DefaultValue("World") String name) {
         AtomicInteger ai = customerMap.get(name);
         if (ai == null) {
             ai = new AtomicInteger();
             customerMap.put(name, ai);
         }
         int value = ai.incrementAndGet();
         return cliente.getNome()+" - Hello "+name+ " ! =>"+value;
     }
     
     public void doSomething() {
         System.out.println(dao.buscaPorId(1L));
         System.out.println("Context: "+sessionContext);
         System.out.println("Button has been clicked ! "+cliente.getNome());
        throw new PersistenceException("This is a persistence Exception");
     }
 
 }
