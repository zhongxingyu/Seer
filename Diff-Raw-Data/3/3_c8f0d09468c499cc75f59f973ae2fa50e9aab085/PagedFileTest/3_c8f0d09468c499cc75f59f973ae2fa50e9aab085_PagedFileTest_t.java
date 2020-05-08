 package com.atteo.jello.tests.performance.store;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.os.Debug;
 import android.test.InstrumentationTestCase;
 import android.test.PerformanceTestCase;
 
 import com.atteo.jello.store.Page;
 import com.atteo.jello.store.PagePool;
 import com.atteo.jello.store.PagedFile;
 import com.atteo.jello.store.StoreModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class PagedFileTest extends InstrumentationTestCase implements
 		PerformanceTestCase {
 	private final String filename = "testfile";
 	private Injector injector;
 
 	public boolean isPerformanceOnly() {
 		return true;
 	}
 
 	public int startPerformance(final Intermediates intermediates) {
 
 		return 1;
 	}
 
 	public void testGetPage() throws IOException {
 		final int FILESIZE = 200;
 		final int TESTSIZE = 10000;
 		assertEquals(FILESIZE-1, PagedFile.addPages(FILESIZE));
 		final PagePool pagePool = injector.getInstance(PagePool.class);
 		Page p = pagePool.acquire();
 		int seed = 1112;
 		
 		Debug.startMethodTracing("jello/testGetPage");
 		for (int i = 0; i < TESTSIZE; i++) {
 			PagedFile.getPage(seed % FILESIZE, p);
 			seed = ((seed*seed)/10)%10000;
 		}
 		Debug.stopMethodTracing();
		
 	}
 	
 	public void testWritePage() throws IOException {
 		final int FILESIZE = 200;
 		final int TESTSIZE = 10000;
 		assertEquals(FILESIZE-1, PagedFile.addPages(FILESIZE));
 		final PagePool pagePool = injector.getInstance(PagePool.class);
 		Page p = pagePool.acquire();
 		int seed = 3432;
 		
 		Debug.startMethodTracing("jello/testWritePage");
 		
 		for (int i = 0; i < TESTSIZE; i++) {
 			PagedFile.writePage(seed % FILESIZE, p);
 			seed = ((seed*seed)/10)%10000;
 		}
 
 		Debug.stopMethodTracing();
 	}
 	
 
 	@Override
 	protected void setUp() throws IOException {
 		injector = Guice.createInjector(new StoreModule(null));
 		final File f = getInstrumentation().getContext().getDatabasePath(
 				filename);
 		f.getParentFile().mkdirs();
 		if (f.exists())
 			f.delete();
 		f.createNewFile();
 		PagedFile.open(f, false);
 	}
 	
 	@Override
 	protected void tearDown() {
 		PagedFile.getFile().delete();
 		PagedFile.close();
 	}
 
 }
