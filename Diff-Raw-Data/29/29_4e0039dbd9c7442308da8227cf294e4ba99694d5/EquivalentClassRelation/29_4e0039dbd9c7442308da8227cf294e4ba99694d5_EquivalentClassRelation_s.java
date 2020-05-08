 package de.langenmaier.u2r3.db;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.semanticweb.owlapi.model.ClassExpressionType;
 import org.semanticweb.owlapi.model.NodeID;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
 import org.semanticweb.owlapi.model.OWLAxiom;
 
 import de.langenmaier.u2r3.core.U2R3Reasoner;
 import de.langenmaier.u2r3.db.RelationManager.RelationName;
 import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
 import de.langenmaier.u2r3.util.Pair;
 
 public class EquivalentClassRelation extends Relation {
 	static Logger logger = Logger.getLogger(EquivalentClassRelation.class);
 	
 	protected EquivalentClassRelation(U2R3Reasoner reasoner) {
 		super(reasoner);
 		try {
 			tableName = "equivalentClass";
 			
 			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
 					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
 					" left TEXT," +
 					" right TEXT," +
 					" PRIMARY KEY (left, right))");
 			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");
 
 			create();
 			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean addImpl(OWLAxiom axiom) throws SQLException {
 			OWLEquivalentClassesAxiom naxiom = (OWLEquivalentClassesAxiom) axiom;
 			HashMap<OWLClassExpression, NodeID> refs = new HashMap<OWLClassExpression, NodeID>();
 			for (OWLClassExpression ce1 : naxiom.getClassExpressions()) {
 				for (OWLClassExpression ce2 : naxiom.getClassExpressions()) {
 					if (!ce1.equals(ce2)) {
 						if (ce1.isAnonymous()) {
 							if (!refs.containsKey(ce1)) {
 								refs.put(ce1, NodeID.getNodeID());
 							}
 							addStatement.setString(1, refs.get(ce1).toString());
 						} else {
 							addStatement.setString(1, ce1.asOWLClass().getIRI().toString());
 						}
 						if (ce2.isAnonymous()) {
 							if (!refs.containsKey(ce2)) {
 								refs.put(ce2, NodeID.getNodeID());
 							}
 							addStatement.setString(2, refs.get(ce2).toString());
 						} else {
 							addStatement.setString(2, ce2.asOWLClass().getIRI().toString());
 						}
 
 						System.out.println(addStatement);
 						addStatement.execute();
 					}
 				}
 				if (ce1.isAnonymous()) {
					System.out.println(ce1.getClassExpressionType());
 					if (ce1.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
 						relationManager.getRelation(RelationName.complementOf).add(refs.get(ce1), ce1);
 					} else {
 						throw new U2R3NotImplementedException();
 					}
 				}
 			}
 			return false;
 	}
 
 	@Override
 	public void createDeltaImpl(int id) {
 		try {
 			dropDelta(id);
 			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
 					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
 					" left TEXT," +
 					" right TEXT," +
 					" sourceId1 UUID," +
 					" sourceTable1 VARCHAR(100)," +
 					" sourceId2 UUID," +
 					" sourceTable2 VARCHAR(100)," +
 					" PRIMARY KEY (left, right))");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void merge(DeltaRelation delta) {
 		
 	}
 
 	@Override
 	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
 			throws SQLException {
 				return null;
 	}
 
 	@Override
 	protected String existsImpl(String... args) {
 		throw new U2R3NotImplementedException();
 	}
 
 
 }
