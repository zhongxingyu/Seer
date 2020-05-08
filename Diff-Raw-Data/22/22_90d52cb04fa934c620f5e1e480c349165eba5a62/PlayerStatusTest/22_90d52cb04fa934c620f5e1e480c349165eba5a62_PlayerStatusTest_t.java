 package com.luzi82.libmbgwalpurgis;
 
 import java.io.FileInputStream;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
import org.junit.Assert;
 import org.junit.Test;
 
 public class PlayerStatusTest {
 
 	@Test
 	public void test() throws Exception {
 		FileInputStream fis = new FileInputStream("testdata/toppage.txt");
 		Document doc = Jsoup.parse(fis, "UTF-8", "http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fmypage.php");
 		fis.close();
 
 		PlayerStatus status=PlayerStatus.toStatus(doc);
 
 		Assert.assertEquals(status.mLp, 73);
 		Assert.assertEquals(status.mLpMax, 130);
 		Assert.assertEquals(status.mBp, 0);
 		Assert.assertEquals(status.mBpMax, 3);
 		Assert.assertEquals(status.mExpToUp, 1650);
 		Assert.assertEquals(status.mCard, 78);
 		Assert.assertEquals(status.mCardMax, 100);
 	}
 
 }
