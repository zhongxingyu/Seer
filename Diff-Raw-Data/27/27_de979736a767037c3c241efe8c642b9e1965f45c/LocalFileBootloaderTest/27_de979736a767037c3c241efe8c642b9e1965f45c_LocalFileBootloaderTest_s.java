 package mobi.monaca.framework.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import mobi.monaca.framework.bootloader.LocalFileBootloader;
 import android.test.AndroidTestCase;
 
 public class LocalFileBootloaderTest extends AndroidTestCase {
 	public void testOpenAsset() {
 		// success
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "file:///android_asset/www/hoge");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "file:///android_asset/www/hoge.html");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "file:///android_asset/www/test.ui");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "www/hoge.html");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "file:///android_asset/hoge");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "file:///android_asset/hoge.html");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "file:///android_asset/test.ui");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "hoge.html");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		try {
 			InputStream i = LocalFileBootloader.openAsset(mContext, "hoge");
 			assertNotNull(i);
 		} catch (IOException e) {
 			fail();
 		}
 
 		//failure
 
 		// not exist
 		try {
 			LocalFileBootloader.openAsset(mContext, "");
 			fail();
 		} catch (IOException e) {
 
 		}
 
 		try {
 			LocalFileBootloader.openAsset(mContext, "foobar.txt");
 			fail();
 		} catch (IOException e) {
 
 		}
 
 		try {
 			LocalFileBootloader.openAsset(mContext, "file:///android_asset/foobar.txt");
 			fail();
 		} catch (IOException e) {
 
 		}
 
 
 		//directory
 		try {
 			LocalFileBootloader.openAsset(mContext, "./");
 			fail();
 		} catch (IOException e) {
 		}
 		try {
 			LocalFileBootloader.openAsset(mContext, "www");
 			fail();
 		} catch (IOException e) {
 		}
 		try {
 			LocalFileBootloader.openAsset(mContext, "www/");
 			fail();
 		} catch (IOException e) {
 		}
 		try {
 			LocalFileBootloader.openAsset(mContext, "file:///android_asset/");
 			fail();
 		} catch (IOException e) {
 		}
 		try {
 			LocalFileBootloader.openAsset(mContext, "file:///android_asset/www");
 			fail();
 		} catch (IOException e) {
 		}
 		try {
 			LocalFileBootloader.openAsset(mContext, "file:///android_asset/www/");
 			fail();
 		} catch (IOException e) {
 		}
 
 		// not resolved by this method
 		try {
 			LocalFileBootloader.openAsset(mContext, "file///android_asset/www/../hoge.html");
 			fail();
 		} catch (IOException e) {
 		}
 		try {
 			LocalFileBootloader.openAsset(mContext, "www/../hoge.html");
 			fail();
 		} catch (IOException e) {
 		}
 		try {
			InputStream i = LocalFileBootloader.openAsset(mContext, "/www/hoge.html");
 			fail();
 		} catch (IOException e) {
 		}
 		try {
			InputStream i = LocalFileBootloader.openAsset(mContext, "./www/hoge.html");
 			fail();
 		} catch (IOException e) {
 		}
 	}
 }
