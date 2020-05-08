 package pl.agh.enrollme.service;
 
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.controller.PassResetController;
 
 import java.security.Principal;
 
 /**
  * Author: Piotr Turek
  */
 public interface IPassResetService {
     @Transactional
    PassResetController createController();
 
     @Transactional
    void resetPassword(PassResetController controller);
 }
