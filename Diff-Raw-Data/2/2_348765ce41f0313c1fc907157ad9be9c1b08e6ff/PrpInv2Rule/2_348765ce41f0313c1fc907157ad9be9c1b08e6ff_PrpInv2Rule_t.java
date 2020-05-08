 package de.langenmaier.u2r3.rules;
 
 import org.apache.log4j.Logger;
 
 import de.langenmaier.u2r3.core.U2R3Reasoner;
 import de.langenmaier.u2r3.db.DeltaRelation;
 import de.langenmaier.u2r3.db.RelationManager.RelationName;
 import de.langenmaier.u2r3.util.Settings.DeletionType;
 
 public class PrpInv2Rule extends ApplicationRule {
 	static Logger logger = Logger.getLogger(PrpInv2Rule.class);
 	
 	PrpInv2Rule(U2R3Reasoner reasoner) {
 		super(reasoner);
 		targetRelation = RelationName.propertyAssertion;
 		
 		//relations on the right side
 		relationManager.getRelation(RelationName.inverseOf).addAdditionRule(this);
 		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
 		
 		//on the left side, aka targetRelation
 		relationManager.getRelation(targetRelation).addDeletionRule(this);
 	}
 	
 	
 	@Override
 	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
 			boolean again, int run) {
 		StringBuilder sql = new StringBuilder(400);
 
 		sql.append("INSERT INTO " + newDelta.getDeltaName());
 		
 		if (settings.getDeletionType() == DeletionType.CASCADING) {
 			sql.append(" (subject, property, object, subjectSourceId, subjectSourceTable, propertySourceId, propertySourceTable, objectSourceId, objectSourceTable)");
 			sql.append("\n\t SELECT prp.object AS subject, inv.left AS property, prp.subject AS object, MIN(prp.id) AS subjectSourceId, '" + RelationName.propertyAssertion + "' AS subjectSourceTable, MIN(inv.id) AS propertySourceId, '" + RelationName.inverseOf + "' AS propertySourceTable, MIN(prp.id) AS objectSourceId, '" + RelationName.propertyAssertion + "' AS objectSourceTable");
 		} else {
 			sql.append("(subject, property, object)");
 			sql.append("\n\t SELECT DISTINCT prp.object AS subject, inv.left AS property, prp.subject AS object");
 		}
 		
 		sql.append("\n\t FROM " + delta.getDeltaName("inverseOf") + " AS inv");
 		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp ON inv.right = prp.property");
 
 		if (again) {
 			sql.append("\n\t WHERE NOT EXISTS (");
 			sql.append("\n\t\t SELECT bottom.subject");
 			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
 			sql.append("\n\t\t WHERE bottom.subject = prp.object AND bottom.property = inv.left AND bottom.object = prp.subject");
 			sql.append("\n\t )");
 		}
 		
 		if (settings.getDeletionType() == DeletionType.CASCADING) {
 			sql.append("\n\t GROUP BY prp.object, inv.left, prp.subject");
 		}
 		return sql.toString();
 	}
 
 	@Override
 	public String toString() {
		return "propertyAssertion(Y, P1, X) :- inverseOf(P1, P2), propertyAssertion(X, P2, Y)";
 	}
 
 }
