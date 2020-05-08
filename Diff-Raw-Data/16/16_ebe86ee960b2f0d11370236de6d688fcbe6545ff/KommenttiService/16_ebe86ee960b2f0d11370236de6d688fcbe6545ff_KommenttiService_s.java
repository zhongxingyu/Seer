 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Tsoha.service;
 
 import Tsoha.domain.Kommentti;
 import java.util.List;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.access.prepost.PreAuthorize;
 
 public interface KommenttiService {
 
    @PreAuthorize("hasRole('master')")
     public void remove(Kommentti kommentti) throws DataAccessException;
 
     public List<Kommentti> listAll();
     @PreAuthorize("hasRole('user') or hasRole('master')")
     public Kommentti add(Kommentti kommentti);
 
     public Kommentti findKommentti(Integer id);
 }
