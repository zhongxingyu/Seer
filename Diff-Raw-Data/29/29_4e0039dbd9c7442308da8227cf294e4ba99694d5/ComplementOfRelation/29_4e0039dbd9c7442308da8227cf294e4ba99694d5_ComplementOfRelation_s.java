 package de.langenmaier.u2r3.db;
 
 import java.sql.SQLException;
 import java.util.UUID;
 
 import org.semanticweb.owlapi.model.NodeID;
 import org.semanticweb.owlapi.model.OWLObject;
 import org.semanticweb.owlapi.model.OWLObjectComplementOf;
 import org.semanticweb.owlapi.model.ClassExpressionType;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
 import org.semanticweb.owlapi.normalform.OWLObjectComplementOfExtractor;
 
 import de.langenmaier.u2r3.core.U2R3Reasoner;
 import de.langenmaier.u2r3.db.RelationManager.RelationName;
 import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
 import de.langenmaier.u2r3.util.Pair;
 
 public class ComplementOfRelation extends Relation {
 	
 	protected ComplementOfRelation(U2R3Reasoner reasoner) {
 		super(reasoner);
 		try {
 			tableName = "complementOf";
 			
 			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
 					"id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
 					" left VARCHAR(100)," +
 					" right VARCHAR(100)," +
 					" PRIMARY KEY (left, right))");
 			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");
 
 			create();
 			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public boolean addImpl(OWLAxiom axiom) throws SQLException {
 		/*OWLObjectComplementOf naxiom = (OWLObjectComplementOf) axiom;
 		OWLObjectComplementOfExtractor cex = new OWLObjectComplementOfExtractor();
 		cex.getComplementedClassExpressions(naxiom);*/
 		return false;
 	}
 	
 	@Override
 	public void add(OWLObject o) {
		OWLObjectComplementOf oco = (OWLObjectComplementOf) o;
 		OWLObjectComplementOfExtractor cex = new OWLObjectComplementOfExtractor();
		cex.getComplementedClassExpressions(oco);
		//XXX schauen was diese methode liefert!!!!!!!!!!!
 	}
 
 	@Override
 	public void createDeltaImpl(int id) {
 		try {
 			dropDelta(id);
 			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
 					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
 					" left VARCHAR(100)," +
 					" right VARCHAR(100)," +
 					" leftSourceId UUID," +
 					" leftSourceTable VARCHAR(100)," +
 					" rightSourceId UUID," +
 					" rightSourceTable VARCHAR(100)," +
 					" PRIMARY KEY (left, right))");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void merge(DeltaRelation delta) {
 		throw new U2R3NotImplementedException();
 	}
 
 	@Override
 	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
 			throws SQLException {
 		
 		throw new U2R3NotImplementedException();
 	}
 
 	@Override
 	protected String existsImpl(String... args) {
 		throw new U2R3NotImplementedException();
 	}
 	
 	@Override
 	public void add(NodeID nodeID, OWLClassExpression ce) {
 		OWLObjectComplementOf oco = (OWLObjectComplementOf) ce;
 		//OWLObjectComplementOfExtractor cex = new OWLObjectComplementOfExtractor();
 		//cex.getComplementedClassExpressions(oco);
 		try {
 			addStatement.setString(1, nodeID.toString());
 			NodeID nid = null;
 			if (oco.getOperand().isAnonymous()) {
 				nid = NodeID.getNodeID();
 				addStatement.setString(2, nid.toString());
 			} else {
 				addStatement.setString(2, oco.getOperand().asOWLClass().getIRI().toString());
 			}
 			System.out.println(addStatement);
 			addStatement.execute();
 			if (oco.getOperand().isAnonymous()) {
 				//XXX andere behandlungen aufrufen
 				System.out.println("SOLLTE weiter behandelt werden");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
