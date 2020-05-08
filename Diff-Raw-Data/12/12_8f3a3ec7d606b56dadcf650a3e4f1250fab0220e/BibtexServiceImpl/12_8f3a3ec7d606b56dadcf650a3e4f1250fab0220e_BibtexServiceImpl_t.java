 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.spring.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 import wad.spring.repository.ReferenceRepository;
 
 /**
  *
  * @author tonykova
  */
@Service
 public class BibtexServiceImpl implements BibtexService {
     @Autowired
     ReferenceRepository referenceRepository;
     
     @Override
     public String generateBibtex() {
         return "trolol";
     }
     
    
    private String parseScandit(){
        
        
        return "trololoo";
    }
    
    
 }
