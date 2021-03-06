 package org.bonitasoft.studio.migration.custom.migration.startmessageevent;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.expression.ExpressionPackage;
import org.bonitasoft.studio.model.expression.TableExpression;
import org.bonitasoft.studio.model.process.CorrelationTypeActive;
 import org.eclipse.emf.edapt.migration.CustomMigration;
 import org.eclipse.emf.edapt.migration.Instance;
 import org.eclipse.emf.edapt.migration.Metamodel;
 import org.eclipse.emf.edapt.migration.MigrationException;
 import org.eclipse.emf.edapt.migration.Model;
 
 public class StartMessageEventMigration extends CustomMigration {
 
 	@Override
 	public void migrateBefore(Model model, Metamodel metamodel)
 			throws MigrationException {
 		for (Instance ste : model.getAllInstances("process.StartMessageEvent")){
 			Instance tableCorrelation = ste.get("correlation");
			model.delete(tableCorrelation);
 			Instance messageFlow = ste.get("incomingMessag");
			Instance messageSource = messageFlow.get("source");
			List<Instance> events = new ArrayList<Instance>();
			events = messageSource.get("events");
			for (Instance event:events){
				Instance catchMessageEventName = event.get("targetElementExpression");
				if (catchMessageEventName.get("name").equals(ste.get("name"))){
					Instance correlation = event.get("correlation");
					correlation.set("correlationType",metamodel.getEEnumLiteral("process.CorrelationTypeActive.INACTIVE"));
 				}
 			}
 		}
 	}
 }
