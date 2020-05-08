 package Tsoha.service;
 
 /**
  *
  * @author Pessi
  */
 import Tsoha.domain.Arvostelu;
 import java.util.List;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.access.prepost.PreAuthorize;
 
 public interface ArvosteluService {
 
    @PreAuthorize("hasRole('master')")
     public void remove(Arvostelu arvostelu) throws DataAccessException;
 
     public List<Arvostelu> listAll();
     @PreAuthorize("hasRole('user') or hasRole('master')")
     public Arvostelu add(Arvostelu arvostelu);
 
     public Arvostelu findArvostelu(Integer arvosteluId);
 }
