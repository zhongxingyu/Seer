 package com.luzi82.libmbgwalpurgis;
 
 import java.io.FileInputStream;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
import org.junit.Assert;
 import org.junit.Test;
 
 import com.luzi82.libmbgwalpurgis.RaidBossMatchingFeed.Side;
 
 public class RaidBossMatchingFeedTest {
 
 	@Test
 	public void test() throws Exception {
 		FileInputStream fis = new FileInputStream("testdata/feed.txt");
 		Document doc = Jsoup.parse(fis, "UTF-8", "http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fraid_boss_matching_feed.php");
 		fis.close();
 
 		RaidBossMatchingFeed feed = RaidBossMatchingFeed.toFeed(doc);
 		Assert.assertEquals(5, feed.mUnitList.size());
 
 		RaidBossMatchingFeed.Unit u;
 		u = feed.mUnitList.get(0);
 		Assert.assertEquals(u.mSide, Side.ENEMY);
 		Assert.assertEquals(u.mTime, "02/03 00:50");
 		Assert.assertEquals(u.mPlayer, "black(無糖)");
 		Assert.assertEquals(u.mMessage, "影の魔女 LV25に85,214のﾀﾞﾒｰｼﾞ!");
 	}
 
 }
