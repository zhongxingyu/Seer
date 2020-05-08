 package mobi.monaca.framework.test;
 
 import mobi.monaca.framework.MonacaApplication;
 import mobi.monaca.framework.MonacaPageActivity;
 import mobi.monaca.framework.view.MonacaWebView;
 import android.content.Intent;
 import android.test.ActivityUnitTestCase;
import android.test.AndroidTestCase;
 
 public class MonacaWebViewTest extends ActivityUnitTestCase<MonacaPageActivity>  {
 	MonacaWebView target;
 	MonacaPageActivity activity;
 	public MonacaWebViewTest() {
 		super(MonacaPageActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		MonacaApplication application = new MonacaApplication();
 		application.loadAppJsonSetting();
 		setApplication(application);
 		startActivity(new Intent(), null, null);
 		activity = getActivity();
 		target = new MonacaWebView(activity);
 	}
 
 	public void testIsUrlWhiteListedManifestOverriding() {
 		// defined in AndroidManifest.xml not config.xml
 		/* <access origin="http://monaca.mobi*" subdomains="true" /> */
		assertTrue(target.isUrlWhiteListed("http://monaca.mobi/dashboard"));
		assertTrue(target.isUrlWhiteListed("https://ide.monaca.mobi/project/hogehoge?lang==ja"));
 
 		/* <access origin="http://asial.co.jp" subdomains="false" /> */
		assertTrue(target.isUrlWhiteListed("http://asial.co.jp"));
		assertTrue(target.isUrlWhiteListed("https://asial.co.jp/"));
		assertTrue(target.isUrlWhiteListed("http://asial.co.jp/"));
 
		assertFalse(target.isUrlWhiteListed("http://blog.asial.co.jp"));
		assertFalse(target.isUrlWhiteListed("https://blog.asial.co.jp"));
		assertFalse(target.isUrlWhiteListed("http://www.asial.co.jp/mobile.php"));
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		activity.finish();
 	}
 }
