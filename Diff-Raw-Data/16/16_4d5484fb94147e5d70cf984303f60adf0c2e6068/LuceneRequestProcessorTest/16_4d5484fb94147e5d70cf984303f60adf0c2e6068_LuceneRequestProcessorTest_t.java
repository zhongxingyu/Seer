 package com.gentics.cr.lucene.search;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.HashMap;
 
 import junit.framework.Assert;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.TermVector;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.gentics.cr.CRConfigStreamLoader;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.configuration.EnvironmentConfiguration;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.lucene.didyoumean.DidyoumeanIndexExtension;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
 import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
 
 public class LuceneRequestProcessorTest {
 	private static CRConfigUtil config = null;
 	private static RequestProcessor rp=null;
 	private static LuceneIndexLocation location=null;
 	
 	@BeforeClass
 	public static void setUp() throws CRException, URISyntaxException, IOException {
 		EnvironmentConfiguration.loadEnvironmentProperties();
 		config = new CRConfigStreamLoader("search", LuceneRequestProcessorTest.class.getResourceAsStream("search.properties"));
 		rp = config.getNewRequestProcessorInstance(1);
 		CRConfigUtil rpConfig = config.getRequestProcessorConfig(1);
 		location = LuceneIndexLocation.getIndexLocation(rpConfig);
 		IndexAccessor accessor = location.getAccessor();
 		
 		addDoc(accessor, "content:content1", "category:cars", "contentid:10007.1");
 		addDoc(accessor, "content:audi", "category:cars", "contentid:10007.2");
 		addDoc(accessor, "content:saab", "category:cars", "contentid:10007.3");
 		addDoc(accessor, "content:volvo", "category:cars", "contentid:10007.4");
 		addDoc(accessor, "content:ford", "category:cars", "contentid:10007.5");
 		addDoc(accessor, "content:pagani", "category:cars", "contentid:10007.6");
 		addDoc(accessor, "content:potatoe", "category:plants", "contentid:10007.7");
 		addDoc(accessor, "content:flower", "category:plants", "contentid:10007.8");
 		addDoc(accessor, "content:tree", "category:plants", "contentid:10007.9");
 		
 		DidyoumeanIndexExtension dymProvider = ((LuceneRequestProcessor) rp).getCRSearcher().getDYMProvider();
 		AbstractUpdateCheckerJob dymIndexJob = dymProvider.createDYMIndexJob(location);
 		dymIndexJob.run();
 	}
 	
 	@Test
 	public void testConfig() {
 		Assert.assertNotNull(rp);
 	}
 	
 	@Test
 	public void testSimpleSearch() throws CRException {
 		CRRequest request = new CRRequest();
 		request.setRequestFilter("category:plants");
 		Collection<CRResolvableBean> objects = rp.getObjects(request);
 		Assert.assertEquals("The Search did not find all items.", 3, objects.size());
 		for(CRResolvableBean bean : objects) {
 			Assert.assertEquals("Object was not in category plants.", "plants", bean.get("category"));
 		}
 	}
 	
 	@Test
 	public void testMetaResolvableSearch() throws CRException {
 		CRRequest request = new CRRequest();
 		request.set("metaresolvable", "true");
 		request.setRequestFilter("category:plants");
 		Collection<CRResolvableBean> objects = rp.getObjects(request);
 		Assert.assertEquals("The Search did not find all items.", 4, objects.size());
 		
 		CRResolvableBean metabean = objects.iterator().next();
 		Assert.assertNotNull(metabean);
 		Assert.assertEquals("Hitcount did not match the expected value.", 3, metabean.getInteger("totalhits", 0));
 	}
 	
 	@Test
 	public void testDYMMetaResolvableSearch() throws CRException {
 		CRRequest request = new CRRequest();
 		request.set("metaresolvable", "true");
 		request.setRequestFilter("content:frd");
 		Collection<CRResolvableBean> objects = rp.getObjects(request);
 		
 		CRResolvableBean metabean = objects.iterator().next();
 		HashMap<String, String[]> suggestions = (HashMap<String, String[]>) metabean.get("suggestions");
 		Assert.assertNotNull(suggestions);
 		Assert.assertEquals("Suggestion count did not match the expected value.", 1, suggestions.get("frd").length);
 		Assert.assertEquals("Suggestion did not match the expected value.", "ford", suggestions.get("frd")[0]);
 	}
 	
	@AfterClass
	public static void tearDown() throws IOException {
		DidyoumeanIndexExtension dymProvider = ((LuceneRequestProcessor) rp).getCRSearcher().getDYMProvider();
		AbstractUpdateCheckerJob dymIndexJob = dymProvider.createDYMIndexDeleteJob(location);
		dymIndexJob.run();
		Assert.assertEquals("DYM Index should be empty.", 0, dymProvider.getDidyoumeanLocation().getDocCount());
	}
	
 	/**
 	 * Adds a Document to the index.
 	 * @param ia
 	 * @param fields
 	 * @return
 	 * @throws CorruptIndexException
 	 * @throws IOException
 	 */
 	private static Document addDoc(IndexAccessor ia, String... fields) throws CorruptIndexException, IOException {
 		Document document = new Document();
 		for (String field : fields) {
 			String name = field.replaceAll(":.*", "");
 			String value = field.substring(name.length() + 1);
 			document.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED,
 					TermVector.WITH_POSITIONS_OFFSETS));
 		}
 		IndexWriter writer = ia.getWriter();
 		writer.addDocument(document);
 		ia.release(writer);
 		return document;
 	}
 }
