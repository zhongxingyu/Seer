 /*
  * Thibaut Colar Sep 2, 2009
  */
 package net.colar.netbeans.fan.indexer;
 
 import net.colar.netbeans.fan.FanUtilities;
import net.colar.netbeans.fan.indexer.model.FanDocument;
 import net.colar.netbeans.fan.platform.FanPlatform;
 import net.jot.logger.JOTLogger;
 import org.netbeans.modules.parsing.spi.indexing.Context;
 import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
 import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
 import org.netbeans.modules.parsing.spi.indexing.Indexable;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 
 /**
  * Indexer Factory impl.
  * Registered through layer.xml
  * @author thibautc
  */
 public class FanIndexerFactory extends CustomIndexerFactory
 {
 
 	public static final String NAME = "FanIndexer";
 	public static final int VERSION = 1;
 	private static FanIndexer indexer = new FanIndexer();
 
 	public static FanJarsIndexer getJavaIndexer()
 	{
 		return indexer.getJarsIndexer();
 	}
 
 	public FanIndexerFactory()
 	{
 		FanUtilities.GENERIC_LOGGER.info("Fantom - Inited indexer Factory");
 	}
 
 	public static FanIndexer getIndexer()
 	{
 		return indexer;
 	}
 
 	@Override
 	public CustomIndexer createIndexer()
 	{
 		return indexer;
 	}
 
 	@Override
 	public boolean supportsEmbeddedIndexers()
 	{
 		return false;
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
 
 	@Override
 	public void filesDeleted(Iterable<? extends Indexable> itrbl, Context cntxt)
 	{
 		for (Indexable idx : itrbl)
 		{
 			String path = idx.getURL().getPath();
 			JOTLogger.debug(this, "File deleted: " + path);
 			indexer.requestDelete(path);
 		}
 	}
 
 	@Override
 	public void filesDirty(Iterable<? extends Indexable> itrbl, Context cntxt)
 	{
 		// requestIndexing
 		indexer.index(itrbl, cntxt);
 	}
 
 	/**
 	 * recursive
 	 * @param root
 	 * @param nb
 	 * @return
 	 */
 	private int scanFolder(FileObject root, int nb)
 	{
 		FanPlatform platform = FanPlatform.getInstance(false);
 		if (platform.isConfigured())
 		{
 			// Don't do Fantom distro sources since we have binaries (faster)
 			if (FileUtil.isParentOf(platform.getFanHome(), root))
 			{
 				return nb;
 			}
 		}
 
 		FileObject[] children = root.getChildren();
 		for (FileObject child : children)
 		{
 			if (child.isFolder())
 			{
 				//recurse
 				nb = scanFolder(child, nb);
 			} else
 			{
 				if (child.hasExt("fan") || child.hasExt("fwt"))
 				{
 					if (FanIndexer.checkIfNeedsReindexing(child.getPath(), child.lastModified().getTime()))
 					{
 						JOTLogger.info(this, "ReIndexing: " + root);
 
 						nb++;
 						indexer.requestIndexing(child.getPath());
 					}
 				}
 			}
 		}
 		return nb;
 	}
 
 	@Override
 	public void scanFinished(Context cntxt)
 	{
 	}
 
 	@Override
 	public boolean scanStarted(Context context)
 	{
 		FileObject root = context.getRoot();
 		scanFolder(root, 0);
 		// what does the return mean ?
 		return true;
 	}
 }
 
 
