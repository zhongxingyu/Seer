 package com.neopets;
 
 import org.apache.http.client.CookieStore;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.junit.Test;
 
 import java.io.File;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 public class CookieJarTest {
   private static final int COOKIES_COUNT = 40;
  private static final String TEMP_FILE_PATH = "cookiJar.temp";
 
   @Test
   public void testLoad() throws Exception {
     BasicCookieStore store = new BasicCookieStore();
     for (int i = 0; i < COOKIES_COUNT; i++) {
       BasicClientCookie cookie = new BasicClientCookie("cookie_" + i, "value_ " + i);
       store.addCookie(cookie);
     }
     File file = new File(TEMP_FILE_PATH);
     CookieJar jar = new CookieJar(file);
     jar.save(store);
     CookieStore loadedStore = jar.load();
     file.delete();
 
     List<Cookie> loadedCookies = loadedStore.getCookies();
     List<Cookie> expectedCookies = store.getCookies();
     for (int i = 0; i < COOKIES_COUNT; i++) {
       Cookie expectedCookie = expectedCookies.get(i);
       Cookie loadedCookie = loadedCookies.get(i);
       assertEquals(expectedCookie.getName(), loadedCookie.getName());
       assertEquals(expectedCookie.getValue(), loadedCookie.getValue());
     }
   }
 
 }
