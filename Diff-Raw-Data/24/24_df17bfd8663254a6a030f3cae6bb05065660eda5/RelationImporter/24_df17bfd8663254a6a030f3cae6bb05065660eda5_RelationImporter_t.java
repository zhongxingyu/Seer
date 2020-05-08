 package pt.ist.ff.neo;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.index.Index;
 
 import dml.DomainClass;
 import dml.DomainRelation;
 import dml.Role;
 
 public class RelationImporter {
 
     private static final Logger logger = LogManager.getLogger(RelationImporter.class);
 
     public static void importRelation(GraphDatabaseService db, final DomainRelation relation, Connection con) throws SQLException {
 
 	Index<Node> oidIndex = db.index().forNodes("oid");
 
 	Role dataRole = getDataRole(relation);
 
 	if (dataRole == null) {
 	    logger.warn("Ignoring many-to-many relation for now: " + relation.getFullName());
 	    return;
 	}
 
	Role otherRole = dataRole.getOtherRole();
 
 	RelationshipType type = new RelationshipType() {
 
 	    @Override
 	    public String name() {
 		return relation.getName();
 	    }
 	};
 
 	DomainClass classToGet = Util.getTopClass((DomainClass) dataRole.getType());
 
 	String dbName = Util.getDBName(classToGet.getName());
 
 	String relationCol = "OID_" + Util.getDBName(otherRole.getName());
 
 	logger.info("Importing relation " + relation.getName());
 
 	logger.trace("\tRelation is on table " + dbName + ". Column: " + relationCol);
 
 	try (Statement st = con.createStatement();
 		ResultSet rs = st.executeQuery("SELECT OID, " + relationCol + " FROM " + dbName + " WHERE " + relationCol
 			+ " IS NOT NULL")) {
 
 	    int count = 0;
 
 	    Transaction tx = db.beginTx();
 
 	    while (rs.next()) {
 		count++;
 
 		Long oid = rs.getLong("OID");
 		Long otherOid = rs.getLong(relationCol);
 
 		Node one = oidIndex.get("oid", oid).getSingle();
 		Node other = oidIndex.get("oid", otherOid).getSingle();
 
 		if (one == null || other == null) {
		    logger.error("\t\tNode does not exist! Original OID: " + oid + ". Other OID : " + otherOid);
		    logger.error("\t\t\tOne: " + one + " Other: " + other);
 		    continue;
 		}
 
 		one.createRelationshipTo(other, type);
 		other.createRelationshipTo(one, type);
 	    }
 
 	    tx.success();
 	    tx.finish();
 
 	    logger.info("Finished importing relations. Imported " + count + " instances.");
 
 	}
 
     }
 
     private static Role getDataRole(DomainRelation relation) {
 
	if (relation.getFirstRole().getMultiplicityUpper() != 1 && relation.getSecondRole().getMultiplicityUpper() != 1)
	    return null;

	if (relation.getFirstRole().getMultiplicityUpper() != 1)
 	    return relation.getFirstRole();
 
	if (relation.getSecondRole().getMultiplicityUpper() != 1)
 	    return relation.getSecondRole();
 
	return relation.getFirstRole();
     }
 
 }
