 package org.neo4j.util.xaworker;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import org.neo4j.impl.transaction.xaframework.LogBuffer;
 
 /**
  * Convenience class for a xa data source which handles search entries which
  * is about to be indexed into a search engine.
  */
 public class SearchDataHook extends XaWorkerHook
 {
 	@Override
 	public int getEntrySize()
 	{
 		// byte:	entry type
 		// long:	node id
 		// byte:	operation id
 		return 10;
 	}
 
 	@Override
 	public XaWorkerLogEntry newLogEntry()
 	{
 		return new SearchXaWorkerLogEntry();
 	}
 	
 	@Override
 	public XaWorkerEntry newEntry()
 	{
 		return new SearchDataEntry();
 	}
 
 	public static class SearchXaWorkerLogEntry extends XaWorkerLogEntry
 	{
 		@Override
 		protected Object[] readEntry( ByteBuffer buffer )
 		{
 			Object[] values = new Object[ 3 ];
 			values[ 0 ] = buffer.get();
 			values[ 1 ] = buffer.getLong();
 			values[ 2 ] = buffer.get();
 			return values;
 		}
 
		@Override
 		protected void writeEntry( LogBuffer buffer ) throws IOException
 		{
 			SearchDataEntry entry = ( SearchDataEntry ) this.getEntry();
 			buffer.put( entry.getType() );
 			buffer.putLong( entry.getId() );
 			buffer.put( entry.getMode() );
 		}
 	}
 }
