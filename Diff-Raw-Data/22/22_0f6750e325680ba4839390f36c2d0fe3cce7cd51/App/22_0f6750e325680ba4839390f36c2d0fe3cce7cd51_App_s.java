 package ${package};
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.scripting.FxScriptRunInfo;
 
 import java.util.Map;
 
 /**
  * Experimental [fleXive] console application.
  *
  * <p>
  * Execute with:<br/>
  * {@code  mvn install exec:java -Dexec.mainClass=${package}.App -Dopenejb.base=../openejb/}
  * </p>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class App {
     public static void main(String[] args) throws FxApplicationException {
         FxContext.initializeSystem(1, "consoleapp");
 
        System.out.println(FxSharedUtils.getFlexiveEditionFull());
         System.out.println("Hello [fleXive] World - calling example EJB, listing available content:");
         boolean hasInstances = false;
         for (Map.Entry<FxType, Integer> entry
                 : EJBLookup.getEngine(EJBExample.class).getInstanceCounts().entrySet()) {
             System.out.println(entry.getKey().getLabel() + ": " + entry.getValue());
             hasInstances = true;
         }
 
         // perform some sanity checks
         if (!hasInstances) {
             System.err.println("No content instances found.");
             System.exit(1);
         }
         // check if all run-once scripts were executed successfully
         for (FxScriptRunInfo info : EJBLookup.getScriptingEngine().getRunOnceInformation()) {
             if (!info.isSuccessful()) {
                 System.err.println("Failed to execute runonce script " + info.getName());
                 System.exit(1);
             }
         }
 
         System.exit(0);
     }
 }
