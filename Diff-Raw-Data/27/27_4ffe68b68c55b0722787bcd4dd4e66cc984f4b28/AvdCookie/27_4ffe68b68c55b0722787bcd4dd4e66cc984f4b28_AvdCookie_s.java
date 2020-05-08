 package cz.cvut.fel.archval.integration.avd;
 
import cz.cvut.fel.archval.core.api.model.validation.ValidationModel;
 
 /**
  * Cookie representing model loading from AVD file.
  *
  * @author Martin Vejmelka (martin.vejmelka@fel.cvut.cz)
  */
 public interface AvdCookie {
 
    public ValidationModel getValidationModel();
 }
