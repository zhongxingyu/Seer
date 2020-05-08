 /*
  * Thibaut Colar Sep 2, 2009
  */
 package net.colar.netbeans.fan.indexer;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import org.netbeans.modules.parsing.api.Snapshot;
 import org.netbeans.modules.parsing.spi.indexing.Context;
 import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
 import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
 import org.netbeans.modules.parsing.spi.indexing.Indexable;
 import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
 
 /**
  * Indexer Factory impl.
  * @author thibautc
  */
 public class FanIndexerFactory extends EmbeddingIndexerFactory
 {
 
 	static{System.err.println("Fantom - Init indexer Factory");}
 
 	public static final String NAME = "FanIndexer";
 	public static final int VERSION = 1;
 
 	@Override
 	public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot)
 	{
 		if (snapshot.getSource().getFileObject() != null)
 		{
 			return new FanIndexer();
 		}
 		return null;
 	}
 
 	@Override
	public void filesDeleted(Collection<? extends Indexable> deleted, Context context)
 	{
 		try
 		{
 			IndexingSupport is = IndexingSupport.getInstance(context);
 			Iterator<? extends Indexable> it = deleted.iterator();
 			while (it.hasNext())
 			{
 				is.removeDocuments(it.next());
 			}
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
	public void filesDirty(Collection<? extends Indexable> dirty, Context context)
 	{
 		try
 		{
 			IndexingSupport is = IndexingSupport.getInstance(context);
			Iterator<? extends Indexable> it = dirty.iterator();
 			while (it.hasNext())
 			{
 				is.markDirtyDocuments(it.next());
 			}
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String getIndexerName()
 	{
 		return NAME;
 	}
 
 	@Override
 	public int getIndexVersion()
 	{
 		return VERSION;
 	}
 


 }
 
 
