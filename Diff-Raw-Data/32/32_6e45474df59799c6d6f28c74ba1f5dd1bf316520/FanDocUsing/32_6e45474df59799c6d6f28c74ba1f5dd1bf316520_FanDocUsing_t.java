 /*
  * Thibaut Colar Dec 22, 2009
  */
 
 package net.colar.netbeans.fan.indexer.model;
 
 import java.util.Vector;
 import net.jot.persistance.JOTModel;
 import net.jot.persistance.JOTModelMapping;
 import net.jot.persistance.JOTSQLCondition;
 import net.jot.persistance.JOTTransaction;
 import net.jot.persistance.builders.JOTQueryBuilder;
 
 /**
 * DB Model for a source document's using statements
  * @author thibautc
  */
 public class FanDocUsing extends JOTModel
 {
 	public Long documentId = -1L;
 	public String qualifiedType = ""; // how long ?
 
 	@Override
 	protected void customize(JOTModelMapping mapping)
 	{
 		mapping.defineFieldSize("qualifiedType", 255);
 	}
 
 	public static Vector<FanDocUsing> findAllForDoc(JOTTransaction transaction, long doc) throws Exception
 	{
 		JOTSQLCondition cond = new JOTSQLCondition("documentId", JOTSQLCondition.IS_EQUAL, doc);
 		return (Vector<FanDocUsing>)JOTQueryBuilder.selectQuery(transaction, FanDocUsing.class).where(cond).find().getAllResults();
 	}
 
 	public long getDocumentId()
 	{
 		return documentId;
 	}
 
 	public void setDocumentId(long documentId)
 	{
 		this.documentId = documentId;
 	}
 
 	public String getType()
 	{
 		return qualifiedType;
 	}
 
 	public void setType(String type)
 	{
 		this.qualifiedType = type;
 	}
 
 }
