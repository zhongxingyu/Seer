 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan;
 
 import javax.swing.event.ChangeListener;
 import net.colar.netbeans.fan.platform.FanPlatform;
 import org.netbeans.modules.parsing.api.Snapshot;
 import org.netbeans.modules.parsing.api.Task;
 import org.netbeans.modules.parsing.spi.ParseException;
 import org.netbeans.modules.parsing.spi.Parser;
 import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileUtil;
 
 /**
  *
  * Parser impl.
  * Bridges NB parser with parboiled parser
  * Base of a parsing job results
  * @author tcolar
  */
 public class NBFanParser extends Parser
 {
 
 	FanParserTask result;
 
 	@Override
 	public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException
 	{
 		FanPlatform platform = FanPlatform.getInstance(false);
 		String path = snapshot.getSource().getFileObject().getPath();
 		// We don't care for the standard NB indexer
 		// It's slow and annotying and we don't use it (have our on)
		// So we don't allow it to run on the fan repo sources (we index those ourselved from binaries)
 		// TODO: maybe I can make mmy own RespositoryUpdaterImpl that does nothing
 		//  instead of this ugly class name check hack.
 		String taskClass= task.getClass().getName();
		if( platform == null ||
			! FileUtil.isParentOf(platform.getFanHome(), snapshot.getSource().getFileObject()) ||
			! taskClass.startsWith("org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater"))
 		{
 			parse(snapshot);
 		}
		else
 		{
 			FanUtilities.GENERIC_LOGGER.info("Ignoring request to parse Fantom distro source file: "+path);
		}
 	}
 
 	public void parse(Snapshot snapshot)
 	{
 		result = new FanParserTask(snapshot);
 		result.parse();
 		result.parseScope();
 	}
 
 	@Override
 	public Result getResult(Task task) throws ParseException
 	{
 		return getResult();
 	}
 
 	public Result getResult()
 	{
 		return result;
 	}
 
 	@Override
 	public void cancel()
 	{
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void addChangeListener(ChangeListener changeListener)
 	{
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void removeChangeListener(ChangeListener changeListener)
 	{
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 }
