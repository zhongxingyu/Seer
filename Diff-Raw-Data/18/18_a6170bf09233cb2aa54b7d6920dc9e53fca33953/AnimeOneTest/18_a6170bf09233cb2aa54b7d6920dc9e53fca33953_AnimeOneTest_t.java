 package net.hisme.masaki.kyoani.test.schedule_service;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.test.AndroidTestCase;
 import net.hisme.masaki.kyoani.models.Account;
 import net.hisme.masaki.kyoani.models.Schedule;
 import net.hisme.masaki.kyoani.models.Schedules;
 import net.hisme.masaki.kyoani.schedule_service.AnimeOne;
 import net.hisme.masaki.kyoani.schedule_service.exception.LoginFailureException;
 import net.hisme.masaki.kyoani.schedule_service.exception.NetworkUnavailableException;
 import net.hisme.masaki.kyoani.schedule_service.exception.SessionExpiredException;
 import net.hisme.masaki.kyoani.utils.StringUtils;
 
 public class AnimeOneTest extends AndroidTestCase {
   AnimeOne anime_one;
 
   public void setUp() {
     this.anime_one = new AnimeOne();
     anime_one.clearUpdatedDate();
   }
 
   public void testNeedUpdate() {
     assertTrue(anime_one.needUpdate());
   }
 
   public void testParseMypageOnSuccess() throws SessionExpiredException, LoginFailureException {
     Schedules schedules = anime_one.parseMypage(loadMypageTestData("mypage_success"), "1月15日");
     assertEquals(3, schedules.size());
 
     Schedule schedule = schedules.get(0);
     assertEquals("機動戦士ガンダムAGE", schedule.getName());
     assertEquals("TBS", schedule.getChannel());
     assertEquals("17:00", schedule.getStartString());
 
     schedule = schedules.get(2);
     assertEquals("アクエリオンEVOL", schedule.getName());
     assertEquals("テレビ東京", schedule.getChannel());
     assertEquals("25:35", schedule.getStartString());
   }
   
  public void testParseMypageNoPrograms() throws SessionExpiredException, LoginFailureException {
    Schedules schedules = anime_one.parseMypage(loadMypageTestData("mypage_no_programs"), "4月3日");
    assertEquals(0, schedules.size());
   }
  
  
   public void testParseMypageNotIncludeTargetDay() throws SessionExpiredException, LoginFailureException {
     Schedules schedules = anime_one.parseMypage(loadMypageTestData("mypage_success"), "3月22日");
     assertEquals(0, schedules.size());
   }
 
   public void testHttpGet() {
     String body = anime_one.httpGet("https://github.com/masarakki/KyoAni");
     Matcher match = Pattern.compile("masarakki/KyoAni",
         Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.UNIX_LINES).matcher(body);
     assertTrue(match.find());
   }
 
   public void testLogin() throws NetworkUnavailableException {
     assertFalse(anime_one.login(new Account("unko", "chinko")));
   }
 
   public String loadMypageTestData(String filename) {
     return StringUtils.getString(this.getContext().getAssets(), "test_data/anime_one/" + filename + ".html");
   }
 }
