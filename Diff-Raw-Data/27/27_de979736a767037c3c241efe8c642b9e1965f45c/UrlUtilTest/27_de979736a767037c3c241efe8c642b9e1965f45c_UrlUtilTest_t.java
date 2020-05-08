 package mobi.monaca.framework.test;
 
import mobi.monaca.framework.util.UrlUtil;
 import android.os.Environment;
 import android.test.AndroidTestCase;
 
 public class UrlUtilTest extends AndroidTestCase {
 
 	public void testGetUIFileUrl() {
 		assertEquals("index.ui", UrlUtil.getUIFileUrl("index.html"));
 	//	assertEquals("index.ui", UrlUtil.getUIFileUrl("index.html#hoge")); TODO confirm this should be true
 	}
 
 	public void testIsEmbedding() {
 		assertTrue(UrlUtil.isEmbedding("index.html#embedding"));
 		assertTrue(UrlUtil.isEmbedding("../index.html#embedding"));
 		assertTrue(UrlUtil.isEmbedding("hoge/index.html#embedding"));
 		assertTrue(UrlUtil.isEmbedding("http://monaca.mobi/#embedding"));
 		assertTrue(UrlUtil.isEmbedding("file:///android_asset/www/hoge.html#embedding"));
 		assertTrue(UrlUtil.isEmbedding("file://data/data/hoge.foo/aaa.html#embedding"));
 		assertTrue(UrlUtil.isEmbedding("file://data/data/hoge.foo/aaa.html##embedding"));
 
 		assertFalse(UrlUtil.isEmbedding("index.html"));
 		assertFalse(UrlUtil.isEmbedding("hoge/index.html"));
 		assertFalse(UrlUtil.isEmbedding("http://monaca.mobi"));
 		assertFalse(UrlUtil.isEmbedding("index.html#embedding#hogehoge"));
 		assertFalse(UrlUtil.isEmbedding("file://data/data/hoge.foo/aaa.html#embeddinghoge"));
 		assertFalse(UrlUtil.isEmbedding("file://data/data/hoge.foo/aaa.html#embeddinghoge"));
 	}
 
 	public void testResolveMethod() {
 		eq("http://hoge/fuga", UrlUtil.resolve("http://hoge/hoge", "fuga"));
 		eq("http://hoge/fuga", UrlUtil.resolve("http://hoge/hoge", "./fuga"));
 		eq("http://hoge/fuga", UrlUtil.resolve("http://hoge/hoge/hoge", "../fuga"));
 		eq("http://hoge/fuga", UrlUtil.resolve("http://hoge/hoge", "../fuga"));
 	}
 
 	public void testNormalizeMethod() {
 		eq("http://hoge/hoge", UrlUtil.normalize("http://hoge/hoge"));
 		eq("http://hoge/hoge", UrlUtil.normalize("http://hoge/./hoge"));
 		eq("http://hoge/hoge", UrlUtil.normalize("http://hoge/../hoge"));
 		eq("http://hoge/hoge", UrlUtil.normalize("http://hoge/../../hoge"));
 	}
 
 	public void testIsMonacaUriMethod() {
 		//Built project uri
 		assertTrue(UrlUtil.isMonacaUri(mContext, "file:///android_asset/"));
 		assertTrue(UrlUtil.isMonacaUri(mContext, "file:///android_asset/www/"));
 		assertTrue(UrlUtil.isMonacaUri(mContext, "file:///android_asset/www/index.html"));
 		assertTrue(UrlUtil.isMonacaUri(mContext, "file:///android_asset/www/hoge/"));
 
 		//DebuggerProject uri
 		assertTrue(UrlUtil.isMonacaUri(mContext, "file://" + mContext.getApplicationInfo().dataDir));
 
 		//others
 		assertFalse(UrlUtil.isMonacaUri(mContext, "http://www.google.com/"));
 		assertFalse(UrlUtil.isMonacaUri(mContext, "http://localhost/"));
 		assertFalse(UrlUtil.isMonacaUri(mContext, "hogehoge"));
 		assertFalse(UrlUtil.isMonacaUri(mContext, "file:///data/"));
 		assertFalse(UrlUtil.isMonacaUri(mContext, "file:///data/data"));
 		assertFalse(UrlUtil.isMonacaUri(mContext, Environment.getExternalStorageDirectory().getAbsolutePath()));
 	}
 
 	public void testGetResolvedUrlMethod() {
 		// getResolvedUrl is for file protocol
 		// http protocol should not be resolved
 		eq("http://monaca.mobi", UrlUtil.getResolvedUrl("http://monaca.mobi"));
 		eq("http://monaca.mobi/features/../", UrlUtil.getResolvedUrl("http://monaca.mobi/features/../"));
 		eq("http://blog.asial.co.jp/category/21/../../", UrlUtil.getResolvedUrl("http://blog.asial.co.jp/category/21/../../"));
 
 		eq("file:///android_asset/www", "file:///android_asset/www");
 		eq("file:///android_asset/www/", "file:///android_asset/www/");
 
 		eq("file:///android_asset", UrlUtil.getResolvedUrl("file:///android_asset/"));
 		eq("file:///android_asset", UrlUtil.getResolvedUrl("file:///android_asset/www/../"));
 		eq("file:///android_asset", UrlUtil.getResolvedUrl("file:///android_asset/www/hoge/../../"));
 		eq("file:///android_asset", UrlUtil.getResolvedUrl("file:///android_asset/www/hoge/foo/../../../"));
 
 		eq("file:///data/data/mobi.monaca.framework.psedo", UrlUtil.getResolvedUrl("file:///data/data/mobi.monaca.framework.psedo/"));
 		eq("file:///data/data/mobi.monaca.framework.psedo", UrlUtil.getResolvedUrl("file:///data/data/mobi.monaca.framework.psedo/www/../"));
 		eq("file:///data/data/mobi.monaca.framework.psedo", UrlUtil.getResolvedUrl("file:///data/data/mobi.monaca.framework.psedo/www/hoge/../../"));
 		eq("file:///data/data/mobi.monaca.framework.psedo", UrlUtil.getResolvedUrl("file:///data/data/mobi.monaca.framework.psedo/www/hoge/foo/../../../"));
 	}
 
 	protected void eq(String expected, String actual) {
 		LineComparisonFailure.doAssert(expected, actual);
 	}
 
 }
