 package cz.cvut.fel.archval.integration.avd;
 
import cz.cvut.fel.archval.core.api.ValidationModelIface;
 
 /**
  * Cookie representing model loading from AVD file.
  *
  * @author Martin Vejmelka (martin.vejmelka@fel.cvut.cz)
  */
 public interface AvdCookie {
 
    public ValidationModelIface getValidationModel();
 }
