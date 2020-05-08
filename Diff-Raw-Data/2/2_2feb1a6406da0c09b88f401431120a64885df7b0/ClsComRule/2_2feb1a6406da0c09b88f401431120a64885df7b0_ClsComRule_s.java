 package de.langenmaier.u2r3.rules;
 
 import java.sql.SQLException;
 
 import org.apache.log4j.Logger;
 
 import de.langenmaier.u2r3.core.U2R3Reasoner;
 import de.langenmaier.u2r3.db.DeltaRelation;
 import de.langenmaier.u2r3.db.RelationManager.RelationName;
 
 
 public class ClsComRule extends ConsistencyRule {
 	static Logger logger = Logger.getLogger(ClsComRule.class);
 	
 	ClsComRule(U2R3Reasoner reasoner) {
 		super(reasoner);
 		targetRelation = null;
 		
 		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
 		relationManager.getRelation(RelationName.complementOf).addAdditionRule(this);
 	}
 	
 	@Override
 	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
 		long rows = 0;
 		String sql = null;
 		try {
 			sql = buildQuery(delta, aux, false, 0);
 			logger.debug("Checking consistency: " + sql);
 			if (statement.executeQuery(sql).next()) {
 				logger.warn("Inconsistency found!");
 				reasonProcessor.setInconsistent(this);
 			}
 			
 			sql = buildQuery(delta, aux, false, 1);
 			logger.debug("Checking consistency: " + sql);
 			if (statement.executeQuery(sql).next()) {
 				logger.warn("Inconsistency found!");
 				reasonProcessor.setInconsistent(this);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rows;
 	}
 
 	@Override
 	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
 		long rows = 0;
 		String sql = null;
 		try {
 			sql = buildQuery(delta, newDelta, false, 0);
 			logger.debug("Checking consistency: " + sql);
 			if (statement.executeQuery(sql).next()) {
 				logger.warn("Inconsistency found!");
 				reasonProcessor.setInconsistent(this);
 			}
 			
 			sql = buildQuery(delta, newDelta, false, 1);
 			logger.debug("Checking consistency: " + sql);
 			if (statement.executeQuery(sql).next()) {
 				logger.warn("Inconsistency found!");
 				reasonProcessor.setInconsistent(this);
 			}	
 		} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		return rows;
 	}
 
 
 	@Override
 	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
 			boolean again, int run) {
 		StringBuilder sql = new StringBuilder(400);
 
 		sql.append("SELECT 1 AS res");
 		sql.append("\nFROM " + delta.getDeltaName("complementOf") + " AS co");
 		if (run == 0) {
 			sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertion") + " AS ca1 ON co.left = ca1.type");
 			sql.append("\n\t INNER JOIN classAssertion AS ca2 ON co.right = ca2.type");
 		} else if (run == 1) {
 			sql.append("\n\t INNER JOIN classAssertion AS ca1 ON co.left = ca1.type");
 			sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertion") + " AS ca2 ON co.right = ca2.type");
 		}
 
 		return sql.toString();
 	}
 
 	@Override
 	public String toString() {
		return "FALSE :- complementOf(C1, C2), classAssertion(X, C1), propertyAssertion(X, C2)";
 	}
 
 }
