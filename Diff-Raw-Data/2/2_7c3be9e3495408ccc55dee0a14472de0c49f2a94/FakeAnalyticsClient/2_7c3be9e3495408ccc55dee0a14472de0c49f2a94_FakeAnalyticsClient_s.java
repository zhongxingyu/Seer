 package net.newfoo.logs.analytics.client;
 /*
  * Copyright (c) 2009 Jim Connell
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 import com.google.gdata.util.common.base.Pair;
 import static java.lang.Math.random;
 
 public class FakeAnalyticsClient implements AnalyticsClient {
 
     public List<Site> getSites() {
         return new ArrayList<Site>() {{
             new Site("blahisnewfoo.com", "123456");
         }};
     }
 
     public List<TimeEntry> hits(String start, String end) {
         final GregorianCalendar calendar = new GregorianCalendar();
         return new ArrayList<TimeEntry>() {{
             for (int i = 0; i < 7; i++) {
                 calendar.roll(Calendar.DAY_OF_YEAR, -7 + i);
                add(new TimeEntry(calendar.getTime(), (long)(random() * 100), (long)(random() * 95), (long)(random() * 55)));
             }
         }};
     }
 
     public void login(String user, String pass) {
 
     }
 
     public void setProfileId(String profileId) {
 
     }
 
     public List<Pair<String, Long>> topPages(String start, String end) {
         return new ArrayList<Pair<String, Long>>() {{
             add(new Pair<String, Long>("newfoo: My Page", 555l));
             add(new Pair<String, Long>("newfoo: My 2nd Page", 222l));
             add(new Pair<String, Long>("newfoo: My 3rd Page", 32l));
             add(new Pair<String, Long>("newfoo: My 4th Page", 10l));
         }};
     }
 
     public List<Pair<String, Long>> topReferrers(String start, String end) {
         return new ArrayList<Pair<String, Long>>() {{
             add(new Pair<String, Long>("google", 555l));
             add(new Pair<String, Long>("msn", 222l));
             add(new Pair<String, Long>("(direct)", 32l));
             add(new Pair<String, Long>("yahoo", 10l));
         }};
     }
 
 }
