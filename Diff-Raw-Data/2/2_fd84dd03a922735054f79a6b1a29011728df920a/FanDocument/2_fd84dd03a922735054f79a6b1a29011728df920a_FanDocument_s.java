 /*
  * Thibaut Colar Dec 22, 2009
  */
 package net.colar.netbeans.fan.indexer.model;
 
 import java.util.Date;
 import net.jot.logger.JOTLogger;
 import net.jot.persistance.JOTModel;
 import net.jot.persistance.JOTModelMapping;
 import net.jot.persistance.JOTSQLCondition;
 import net.jot.persistance.JOTTransaction;
 import net.jot.persistance.builders.JOTQueryBuilder;
 
 /**
  * DB model for a document (source)
  * @author thibautc
  */
 public class FanDocument extends JOTModel
 {
 
 	/**
 	 * Filesystem path
 	 * Source path for sources
 	 * LIBRARY/POD PATH for pods (libs)
 	 */
 	public String path = "";
 	// source or binary/lib ?
 	public Boolean isSource = true;
 	public Long tstamp = new Date().getTime();
 
 	@Override
 	protected void customize(JOTModelMapping mapping)
 	{
 		mapping.defineFieldSize("path", 350);
 	}
 
 	public static FanDocument findOrCreateOne(JOTTransaction transaction, String path) throws Exception
 	{
 		JOTSQLCondition cond = new JOTSQLCondition("path", JOTSQLCondition.IS_EQUAL, path);
 		FanDocument doc = (FanDocument) JOTQueryBuilder.selectQuery(transaction, FanDocument.class).where(cond).findOrCreateOne();
 		return doc;
 	}
 
 	public void setPath(String path)
 	{
 		this.path = path;
 	}
 
 	public String getPath()
 	{
 		return path;
 	}
 
 	public Long getTstamp()
 	{
 		return tstamp;
 	}
 
 	public void setTstamp(Long tstamp)
 	{
 		this.tstamp = tstamp;
 	}
 
 	public Boolean isSource()
 	{
 		return isSource;
 	}
 
 	public void setIsSource(Boolean isSource)
 	{
 		this.isSource = isSource;
 	}
 
 	public static void renameDoc(String oldPath, String newPath)
 	{
 		FanDocument doc = findByPath(oldPath);
 		try
 		{
 			if (doc != null)
 			{
 				doc.setPath(newPath);
 				doc.save();
 			}
 		} catch (Exception e)
 		{
 			JOTLogger.logException(FanDocument.class, "Failed renaming doc: " + oldPath, e);
 		}
 	}
 
 	/**
 	 * Each type might be avail from a source but also a pod
 	 * This removes one of those 2 links.
 	 * - If both links are gone, then delete the type altogether.
 	 * - If only one gone, then reindex using the one left
 	 * to make sure we are up2date
 	 * @param trans
 	 * @param path
 	 */
 	@Override
 	public void delete(JOTTransaction trans) throws Exception
 	{
 			FanType.unlinkDocument(trans, getId(), isSource);
 			super.delete(trans);
 	}
 
 	public static FanDocument findById(Long docId)
 	{
 		FanDocument result = null;
 		try
 		{
 			JOTSQLCondition cond = new JOTSQLCondition("id", JOTSQLCondition.IS_EQUAL, docId);
 			result = (FanDocument) JOTQueryBuilder.selectQuery(null, FanDocument.class).where(cond).findOne();
 		} catch (Exception e)
 		{
			JOTLogger.logException(FanDocument.class, "Failed seraching doc: " + docId, e);
 		}
 		return result;
 	}
 
 	public static FanDocument findByPath(String path)
 	{
 		FanDocument result = null;
 		try
 		{
 			JOTSQLCondition cond = new JOTSQLCondition("path", JOTSQLCondition.IS_EQUAL, path);
 			result = (FanDocument) JOTQueryBuilder.selectQuery(null, FanDocument.class).where(cond).findOne();
 		} catch (Exception e)
 		{
 			JOTLogger.logException(FanDocument.class, "Failed searching doc: " + path, e);
 		}
 		return result;
 	}
 }
