 package org.codemucker.jmutate.ast;
 
 import java.io.Closeable;
 import java.io.File;
 import java.util.List;
 
 import org.codemucker.jfind.Root;
 import org.codemucker.jfind.RootVisitor;
 import org.codemucker.lang.IBuilder;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.orientechnologies.orient.core.Orient;
 import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
 import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
 import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 
 public class JSearchEngine implements Closeable {
 
	private final OGraphDatabase db;
 	private final JAstParser parser;
 
 	public static Builder builder(){
 		return new Builder();
 	}
 	
 	private JSearchEngine(File dbDirectoy, List<Root> roots, JAstParser parser){
 		Preconditions.checkNotNull(dbDirectoy,"expect indexing db directory");
 		Preconditions.checkNotNull(roots,"expect roots");
 		Preconditions.checkNotNull(parser, "expect parser");
 		
 		this.parser = parser;
         db = Orient.instance()
        	.getDatabaseFactory()
        	.createGraphDatabase("local:" + dbDirectoy.getAbsolutePath());
        
         //TODO:turn off locking while we index
         //TODO:put it in a reusable location so multiple calls find the already indexed code
         
         db.create();
         
         db.declareIntent(new OIntentMassiveInsert());
         OGlobalConfiguration.TX_USE_LOG.setValue(false);
         OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(false);
         OGlobalConfiguration.CACHE_LEVEL2_ENABLED.setValue(false);
         index(roots);
       
         //reset intent
         db.declareIntent(null);
         OGlobalConfiguration.TX_USE_LOG.setValue(true);
         OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(true);
         OGlobalConfiguration.CACHE_LEVEL2_ENABLED.setValue(true);
 	}
 
 	public void index(List<Root> roots) {
 		RootVisitor indexer = new JSearchIndexingVisitor(db,parser);
 		for(Root root:roots){
 			root.accept(indexer);
 		}
 	}
 	
 	//TODO:add search query
 	public void find(){
 		ORecordIteratorClass<ODocument> types = db.browseClass("Type");
 		System.out.println("JSearchEngine:browse DB");
 		for (ODocument doc : types) {
 			System.out.println("JSearchEngine:fqdn " + doc.field("fqdn"));
 			System.out.println("     path:" + doc.field("resource"));
 		}
 		//db.query(ONativeQuery<OQueryContextNative>, iArgs)
 		System.out.println("JSearchEngine:end browse DB");
 	}
 	
 	public void findPatternNamedX(){
 		
 	}
 
 	@Override
 	public void close() {
         db.close();
 	}
 	
 	public static class Builder {
 		private File dbDirectory;
 		private JAstParser parser;
 		private List<Root> roots = Lists.newArrayList();
 		
 		public JSearchEngine build(){
 			Preconditions.checkNotNull(dbDirectory, "expect a database drectory to be set (or use defaults)");
 			Preconditions.checkNotNull(parser, "expect a parser to be set (or use defaults)");
 			
 			return new JSearchEngine(dbDirectory,roots,parser);
 		}
 		
 		public Builder setDefaults(){
 			setDefaultDBDirectory();
 			setDefaultParser();
 			return this;
 		}
 
 		public Builder setDefaultDBDirectory(){
 			setDbDirectory("c:/tmp/orientdb_codemucker_search_test" + System.currentTimeMillis());
 			return this;
 		}
 		
 		public Builder setDefaultParser(){
 			setParser(JAstParser.builder().setDefaults().build());
 			return this;
 		}
 
 		public Builder setRoots(IBuilder<? extends Iterable<Root>> rootsBuilder){
 			setRoots(rootsBuilder.build());
 			return this;
 		}
 		
 		public Builder setRoots(Iterable<Root> roots){
 			this.roots = Lists.newArrayList(roots);
 			return this;
 		}
 		
 		public Builder addRoot(Root root){
 			this.roots.add(root);
 			return this;
 		}
 		
 		public Builder setParser(IBuilder<JAstParser> builder){
 			setParser(builder.build());
 			return this;
 		}
 		
 		public Builder setParser(JAstParser parser){
 			this.parser = parser;
 			return this;
 		}
 		
 		
 		public Builder setDbDirectory(String dbDirectory){
 			setDbDirectory(new File(dbDirectory));
 			return this;
 		}
 		
 		public Builder setDbDirectory(File dbDirectory){
 			this.dbDirectory = dbDirectory;
 			return this;
 		}
 	}
 }
