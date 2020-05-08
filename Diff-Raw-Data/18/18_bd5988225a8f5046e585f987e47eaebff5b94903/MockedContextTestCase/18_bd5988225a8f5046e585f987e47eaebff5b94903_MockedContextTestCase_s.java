 package ezhun.smsb.base;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.test.AndroidTestCase;
 import android.test.IsolatedContext;
 import android.test.RenamingDelegatingContext;
 import android.test.mock.MockContentResolver;
 import android.test.mock.MockContext;
 
 import java.util.Hashtable;
 
 /**
  * This is a base class for all TestCases with ability to use MockContext instead of real one.
  */
 public abstract class MockedContextTestCase extends AndroidTestCase {
 	private class ResourcefulMockContext extends MockContext {

 		@Override
 		public Resources getResources() {
 			return getProperContext().getResources();
 		}
 	}
 
 	private Context getProperContext() {
 		return super.getContext();
 	}
 
 	IsolatedContext context;
 	MockContentResolver resolver;
 
 	public Context getMockContext() {
 		assertNotNull(context);
 		return context;
 	}
 
 	public ContentResolver getContentResolver() {
 		assertNotNull(resolver);
 		return resolver;
 	}
 
 	public abstract Hashtable<Uri, ContentProvider> getTestContentProviders();
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 
 		Hashtable<Uri, ContentProvider> settings = getTestContentProviders();
 
 		resolver = new MockContentResolver();
 		final String filenamePrefix = "test.ezhun.";
 		RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
 				new ResourcefulMockContext(), // The context that most methods are delegated to
 				getProperContext(), // The context that file methods are delegated to
 				filenamePrefix);
 		context = new IsolatedContext(resolver, targetContextWrapper);
 
 		for (Uri uri : settings.keySet()) {
 			ContentProvider provider = settings.get(uri);
 			provider.attachInfo(context, null);
 			resolver.addProvider(uri.getAuthority(), provider);
 		}
 
 	}
 
 	protected MockedContextTestCase() {
 	}
 }
