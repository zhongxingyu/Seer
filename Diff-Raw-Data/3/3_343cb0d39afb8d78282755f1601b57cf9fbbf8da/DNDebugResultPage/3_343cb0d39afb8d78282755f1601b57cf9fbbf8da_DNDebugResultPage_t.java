 package cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.dn.debug;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.Model;
 
 import cz.cuni.mff.odcleanstore.datanormalization.impl.DataNormalizerImpl.GraphModification;
 import cz.cuni.mff.odcleanstore.datanormalization.impl.DataNormalizerImpl.RuleModification;
 import cz.cuni.mff.odcleanstore.datanormalization.impl.DataNormalizerImpl.TripleModification;
 import cz.cuni.mff.odcleanstore.datanormalization.rules.DataNormalizationRule;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.Role;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.RedirectWithParamButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.RedirectWithParamButtonWithLabel;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.FrontendPage;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.dn.DNRuleDetailPage;
 
 @AuthorizeInstantiation({ Role.PIC })
 public class DNDebugResultPage extends FrontendPage
 {
 	private static final long serialVersionUID = 1L;
 	
 	public DNDebugResultPage(List<GraphModification> results, Integer ruleGroupId) 
 	{
 		super(
 			"Home > Backend > DN > Groups > Debug results", 
 			"Results of DN rule group debugging"
 		);
 			
 		// register page components
 		//
 		add(new RedirectWithParamButton(
 			DNDebugPage.class,
 			ruleGroupId, 
 			"backToInputLink"
 		));
 		addResultTables(results);
 	}
 
 	private void addResultTables(List<GraphModification> results) 
 	{	
 		ListView<GraphModification> tables = new ListView<GraphModification>("resultTable", results)
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(ListItem<GraphModification> item) {
 				GraphModification result = item.getModelObject();
 				List<ModificationRecord> modifications = flatten(result);
 				
 				ListView<ModificationRecord> rows = new ListView<ModificationRecord>("resultRow", modifications)
 				{
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					protected void populateItem(ListItem<ModificationRecord> item) {
 						ModificationRecord record = item.getModelObject();
 						
 						item.add(new AttributeModifier("class", new Model<String>("dn" + record.getType().toString())));
 						
 						item.add(new Label("modification", record.getType().toString()));
 						
 						item.add(new Label("subject", record.getSubject()));						
 						item.add(new Label("predicate", record.getPredicate()));
 						item.add(new Label("object", record.getObject()));
 						
 						item.add(
 							new RedirectWithParamButtonWithLabel
 							(
 								DNRuleDetailPage.class,
 								"showDNRuleDetailPage",
								record.getRule().getDescription(),
								record.getRule().getId()
 							)
 						);			
 					}
 					
 				};
 				item.add(new Label("graphName", result.getGraphName()));
 				item.add(rows);
 			}		
 		};
 		
 		add(tables);
 	}
 	
 	private class ModificationRecord implements Serializable
 	{
 		private static final long serialVersionUID = 1L;
 
 		DataNormalizationRule rule;
 		ModificationType type;
 		String subject;
 		String predicate;
 		String object;
 		
 		public ModificationRecord(DataNormalizationRule rule, ModificationType type, 
 				String subject, String predicate, String object) {
 			this.rule = rule;
 			this.type = type;
 			this.subject = subject;
 			this.predicate = predicate;
 			this.object = object;
 		}
 
 		public DataNormalizationRule getRule() {
 			return rule;
 		}
 
 		public ModificationType getType() {
 			return type;
 		}
 
 		public String getSubject() {
 			return subject;
 		}
 
 		public String getPredicate() {
 			return predicate;
 		}
 
 		public String getObject() {
 			return object;
 		}
 	}
 	
 	private enum ModificationType { DELETE, INSERT }
 	
 	private List<ModificationRecord> flatten(GraphModification graphMod)
 	{
 		List<ModificationRecord> result = new ArrayList<ModificationRecord>();
 		
 		Iterator<DataNormalizationRule> it = graphMod.getRuleIterator();
 		while (it.hasNext())
 		{
 			DataNormalizationRule rule = it.next();
 			RuleModification ruleMod = graphMod.getModificationsByRule(rule);
 			
 			for (TripleModification tripleMod: ruleMod.getInsertions())
 			{
 				result.add(new ModificationRecord(
 					rule,
 					ModificationType.INSERT,
 					tripleMod.getSubject(),
 					tripleMod.getPredicate(),
 					tripleMod.getObject()
 				));
 			}
 			for (TripleModification tripleMod: ruleMod.getDeletions())
 			{
 				result.add(new ModificationRecord(
 					rule,
 					ModificationType.DELETE,
 					tripleMod.getSubject(),
 					tripleMod.getPredicate(),
 					tripleMod.getObject()
 				));
 			}
 		}
 		
 		return result;
 	}
 }
