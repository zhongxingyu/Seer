 /**
  * BLASTHomologySearch.java
  *
  * 2011.10.10
  *
  * This file is part of the CheMet library
  * 
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.chemet.io.external;
 
import com.sun.tools.javac.resources.version;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.annotation.task.FileParameter;
 import uk.ac.ebi.annotation.task.Parameter;
 import uk.ac.ebi.caf.utility.preference.type.StringPreference;
 import uk.ac.ebi.core.CorePreferences;
 import uk.ac.ebi.interfaces.entities.GeneProduct;
 import uk.ac.ebi.interfaces.identifiers.Identifier;
 import uk.ac.ebi.io.blast.BlastReader;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.Map;
import java.util.prefs.Preferences;
 
 
 /**
  * @name    BLASTHomologySearch - 2011.10.10 <br>
  *          Class description
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  */
 public class BLASTHomologySearch extends RunnableTask {
 
     private static final Logger LOGGER = Logger.getLogger(BLASTHomologySearch.class);
 
     private Map<String, GeneProduct> map;
 
     private String cmd;
 
 
     public BLASTHomologySearch() {
     }
 
 
     /**
      *
      * @param options
      * @param accessionMap A HashMap of query accessions to corresponding products
      */
     public BLASTHomologySearch(Map<String, GeneProduct> map, Identifier id) {
         super(id, "BLASTP", "Local homology search");
         this.map = map;
 
         // tell the update manager what to update
         addAll(map.values());
 
 
     }
 
 
     @Override
     public void prerun() {
 
         cmd = getCommand();
         LOGGER.info("executing: " + cmd);
 
     }
 
 
     @Override
     public void run() {
 
         if (isFinished()) {
             // don't rerun
             return;
         }
 
 
         setRunningStatus();
 
         try {
 
             Process p = Runtime.getRuntime().exec(cmd);
             p.waitFor();
             setCompletedStatus();
 
         } catch (InterruptedException ex) {
             setErrorStatus();
             LOGGER.info("InterruptedException... add to messages");
         } catch (IOException ex) {
             setErrorStatus();
             LOGGER.info("IO Exception... add to messages");
             ex.printStackTrace();
         }
 
     }
 
 
     @Override
     public void postrun() {
         try {
 
             File output = null;
             Integer format = null;
 
             for (FileParameter param : getAnnotations(FileParameter.class)) {
                 if (param.getFlag().equals("-out")) {
                     output = param.getValue();
                 }
             }
             for (Parameter param : getAnnotations(Parameter.class)) {
                 if (param.getFlag().equals("-outfmt")) {
                     format = Integer.parseInt(param.toString());
                 }
             }
 
             // check for missing output... and outfmt..
             if (output == null || format == null) {
                 throw new InvalidParameterException("Output or format missing");
             }
 
             CorePreferences pref = CorePreferences.getInstance();
             String version = ((StringPreference)pref.getPreference("BLASTP_VERSION")).get();
             
             // load results into object
             new BlastReader().load(map, output, format, version, this);
             setCompletedStatus();
 
         } catch (Exception ex) {
             LOGGER.info("An error occured: " + ex.getMessage());
             ex.printStackTrace();
             setErrorStatus();
         }
 
     }
 
 
     public BLASTHomologySearch newInstance() {
         return new BLASTHomologySearch();
     }
 }
