 package cz.cvut.fel.archval.integration.avd;
 
import cz.cvut.fel.archval.core.api.model.validation.ValidationModel;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.openide.filesystems.FileObject;
 import org.openide.util.Exceptions;
 
 /**
  * Implementation of AvdCookie interface.
  * 
  * @author Martin Vejmelka (martin.vejmelka@fel.cvut.cz)
  */
 public class AvdSupport implements AvdCookie {
 
     private AvdDataObject avdDataObject;
 
     public AvdSupport(AvdDataObject avdDataObject) {
         this.avdDataObject = avdDataObject;
     }
 
     /**
      * Returns validation model based on selected AVD file
      *
      * @return validation model to be used to perform validation
      */
    public ValidationModel getValidationModel() {
 
         Logger logger = Logger.getLogger(AvdSupport.class.getName());
 

         logger.log(Level.SEVERE, "Creatig valiation model");
         logger.log(Level.SEVERE, "Reading file: ");
         FileObject fo = avdDataObject.getPrimaryFile();
         try {
             BufferedReader br = new BufferedReader(new InputStreamReader(fo.getInputStream()));
 
             String line = null;
             while ((line = br.readLine()) != null) {
                 logger.log(Level.SEVERE, "line: {0}", line);
             }
 
         } catch (FileNotFoundException ex) {
             Exceptions.printStackTrace(ex);
         } catch (IOException ex) {
             Exceptions.printStackTrace(ex);
         }
 
 
         // TODO: implement
         // throw new UnsupportedOperationException("Not supported yet.");
 
 
 
         return null;
     }
 }
