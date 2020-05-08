 /* ************************************************************************** *
  * See the NOTICE file distributed with this work for additional information
  * regarding copyright ownership.
  * 
  * This file is licensed to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  * ************************************************************************** */
 package org.webreformatter.commons.uri;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.webreformatter.commons.uri.AbstractUri.QueryItem;
 import org.webreformatter.commons.uri.Path.Builder;
 
 /**
  * @author kotelnikov
  */
 public class UriTest extends TestCase {
 
     /**
      * Constructor for UriTest.
      * 
      * @param name
      */
     public UriTest(String name) {
         super(name);
     }
 
     public void test() {
         Uri uri = new Uri("x?a=Hello world");
         String query = uri.getQuery(false, false);
         assertEquals("a=Hello world", query);
         List<QueryItem> list = uri.getQueryItems();
         assertNotNull(list);
         assertEquals(1, list.size());
         QueryItem item = list.get(0);
         assertEquals("a", item.getName(false, false));
         assertEquals("Hello world", item.getValue(false, false));
         assertEquals("Hello+world", item.getValue(true, false));
         assertEquals("x?a=Hello+world", uri.toString());
 
         uri = new Uri("x?a=Hello%20world");
         query = uri.getQuery(false, false);
         assertEquals("a=Hello world", query);
         list = uri.getQueryItems();
         assertNotNull(list);
         assertEquals(1, list.size());
         item = list.get(0);
         assertEquals("a", item.getName(false, false));
         assertEquals("Hello world", item.getValue(false, false));
         assertEquals("Hello+world", item.getValue(true, false));
         assertEquals("x?a=Hello+world", uri.toString());
 
         uri = new Uri("x?a=Hello+world");
         query = uri.getQuery(false, false);
         assertEquals("a=Hello world", query);
         list = uri.getQueryItems();
         assertNotNull(list);
         assertEquals(1, list.size());
         item = list.get(0);
         assertEquals("a", item.getName(false, false));
         assertEquals("Hello world", item.getValue(false, false));
         assertEquals("Hello+world", item.getValue(true, false));
         assertEquals("x?a=Hello+world", uri.toString());
     }
 
     public void testFragment() {
         Uri uri = new Uri("a#b");
         assertEquals("a#b", uri.toString());
         uri = uri.getBuilder().setFragment("x#y").build();
         assertEquals("a#x%23y", uri.toString());
         assertEquals("x#y", uri.getFragment());
         assertEquals("x%23y", uri.getFragment(true));
         assertEquals("x%23y", uri.getFragment(false));
 
         uri = uri.getBuilder().setFragment("éè#çà").build();
         assertEquals("a#%C3%A9%C3%A8%23%C3%A7%C3%A0", uri.toString());
         assertEquals("a#éè%23çà", uri.getUri(false));
         assertEquals("a#%C3%A9%C3%A8%23%C3%A7%C3%A0", uri.getUri(true));
         assertEquals("éè#çà", uri.getFragment());
         assertEquals("éè%23çà", uri.getFragment(false));
         assertEquals("%C3%A9%C3%A8%23%C3%A7%C3%A0", uri.getFragment(true));
 
         String str = "http://user:pwd@www.foo.bar:8080/a/b/c?x=y&a=b";
         uri = new Uri(str + "#http://www.foo.bar/x/y/z?n=m&f=v#xxx");
         assertEquals(
             str + "#http://www.foo.bar/x/y/z%3Fn=m%26f=v%23xxx",
             uri.getUri());
         assertEquals("http://www.foo.bar/x/y/z?n=m&f=v#xxx", uri.getFragment());
         assertEquals("x=y&a=b", uri.getQuery());
 
         uri = new Uri(str + "#" + str);
         uri = new Uri(str);
         assertEquals(str, uri.toString());
         uri = uri.getBuilder().setFragment(str).build();
         String fragment = uri.getFragment();
         assertEquals(str, fragment);
     }
 
     public void testI18N() {
         String escaped = "http://ru.wikipedia.org/wiki/%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F";
         String nonEscaped = "http://ru.wikipedia.org/wiki/Россия";
 
         Uri uri = new Uri(nonEscaped);
         assertEquals(escaped, uri.toString());
         assertEquals(escaped, uri.getUri(true));
         assertEquals(nonEscaped, uri.getUri(false));
 
         uri = new Uri(nonEscaped);
         assertEquals(escaped, uri.toString());
         assertEquals(escaped, uri.getUri(true));
         assertEquals(nonEscaped, uri.getUri(false));
 
     }
 
     public void testPath() {
         Uri uri = new Uri("http://www.foo.bar/a/b/c/test.txt");
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/a/b/c/", uri.toString());
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/a/b/", uri.toString());
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/a/", uri.toString());
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/", uri.toString());
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/", uri.toString());
 
         uri = new Uri("http://www.foo.bar/a/b/c/test.txt?x=X&y=Y#frag");
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/a/b/c/?x=X&y=Y#frag", uri.toString());
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/a/b/?x=X&y=Y#frag", uri.toString());
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/a/?x=X&y=Y#frag", uri.toString());
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/?x=X&y=Y#frag", uri.toString());
         uri = uri.getParent();
         assertEquals("http://www.foo.bar/?x=X&y=Y#frag", uri.toString());
     }
 
     public void testQuery() {
         testQuery("?x=X&y=Y", "x=X&y=Y", "x", "X", "y", "Y");
         testQuery(
             "http://www.foo.bar/a/b/c/test.txt?x=X&y=Y#frag",
             "x=X&y=Y",
             "x",
             "X",
             "y",
             "Y");
         testQuery(
             "http://www.foo.bar/a/b/c/test.txt?x=X1&y=Y&x=X2#frag",
             "x=X1&y=Y&x=X2",
             "x",
             "X1",
             "x",
             "X2",
             "y",
             "Y");
         Uri.Builder uri = new Uri.Builder();
         uri = uri.addParam("a", "b");
         testQuery(uri.toString(), "a=b", "a", "b");
         uri = uri.addParams("c", "d", "e", "f");
         testQuery(uri.toString(), "a=b&c=d&e=f", "a", "b", "c", "d", "e", "f");
 
         uri = new Uri.Builder("?x=y");
         uri = uri.addParams("z");
         testQuery(uri.toString(), "x=y&z=", "x", "y", "z", "");
     }
 
     private void testQuery(String str, String query, String... params) {
         Uri uri = new Uri(str);
         assertEquals(query, uri.getQuery());
         Map<String, List<String>> map = uri.getQueryMap();
         assertNotNull(map);
         Map<String, List<String>> control = new LinkedHashMap<String, List<String>>();
         int i = 0;
         while (i < params.length) {
             String key = params[i++];
             String value = params[i++];
             List<String> list = control.get(key);
             if (list == null) {
                 list = new ArrayList<String>();
                 control.put(key, list);
             }
             list.add(value);
         }
         assertEquals(control, map);
     }
 
    public void testRelativeUri() {
        testRelativeUri("./abc", null, "./abc");
        testRelativeUri("file://./abc", null, "./abc");
        testRelativeUri("file://.././abc", null, ".././abc");
    }

    public void testRelativeUri(String str, String host, String path) {
        Uri uri = new Uri(str);
        assertEquals(str, uri.toString());
        assertEquals(host, uri.getHost());
        Path p = new Path(path);
        assertEquals(p, uri.getPath());
    }

     protected void testRelativize(String first, String second, String control) {
         Uri a = new Uri(first);
         Uri b = new Uri(second);
         Uri relativePath = a.getRelative(b);
         assertNotNull(relativePath);
         assertEquals(control, relativePath.toString());
         Uri c = new Uri(control);
         assertEquals(c, relativePath);
     }
 
     /**
      * @throws Exception
      */
     public void testRelativizeAndResolve() {
         testRelativizeAndResolve("x:/a/b/c", "x:/a/", "../");
         testRelativizeAndResolve(
             "x:/kotelnikov/tmp/test.html",
             "x:/kotelnikov/",
             "../");
         testRelativizeAndResolve("x:/a/b/c", "x:/a/b/", "./");
         testRelativizeAndResolve("x:/a/b/c/", "x:/a/b/", "../");
         testRelativizeAndResolve("abc:xyz/123", "abc:xyz", "../xyz");
 
         testRelativizeAndResolve("x:/a/b", "x:/a/b/c", "b/c");
         testRelativizeAndResolve("x:/a/b", "x:/a/b/c/", "b/c/");
         testRelativizeAndResolve("x:/a/b/", "x:/a/b/c", "c");
         testRelativizeAndResolve("x:/a/b/", "x:/a/b/c/", "c/");
         testRelativizeAndResolve("x:/a/B/", "x:/a/b/c/", "../b/c/");
     }
 
     protected void testRelativizeAndResolve(
         String first,
         String second,
         String control) {
         Uri a = new Uri(first);
         Uri b = new Uri(second);
         assertEquals(b, a.getResolved(control));
         Uri relativePath = a.getRelative(b);
         assertNotNull(relativePath);
         assertEquals(control, relativePath.toString());
         Uri test = a.getResolved(relativePath);
         assertNotNull(test);
         assertEquals(b, test);
     }
 
     /**
      * @throws Exception
      */
     public void testRelativizeAndResolveWithAuthority() throws Exception {
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b",
             "http://www.foo.bar/a/b",
             "b");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/",
             "http://www.foo.bar/a/b/",
             "./");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/index.html",
             "http://www.foo.bar/a/b/",
             "./");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/",
             "http://www.foo.bar/a/b/d",
             "d");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b",
             "http://www.foo.bar/a/b/d",
             "b/d");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/c",
             "http://www.foo.bar/a/b/d",
             "d");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/c/",
             "http://www.foo.bar/a/b/d",
             "../d");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/c/",
             "http://www.foo.bar/a/b/d/",
             "../d/");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/c/",
             "http://www.foo.bar/x/y/z/",
             "../../../x/y/z/");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/c/",
             "http://www.foo.bar/x/y/z",
             "../../../x/y/z");
         testRelativizeAndResolve(
             "http://www.foo.bar/a/b/c",
             "http://www.foo.bar/x/y/z",
             "../../x/y/z");
     }
 
     /**
      * @throws Exception
      */
     public void testRelativizeAndResolveWithSchemes() throws Exception {
         testRelativizeAndResolve("toto:/a/b/c", "toto:/a/b/d", "d");
         testRelativizeAndResolve("toto:/a/b/c/", "toto:/a/b/d", "../d");
         testRelativizeAndResolve("toto:/a/b/c/", "toto:/a/b/d/", "../d/");
         testRelativizeAndResolve(
             "toto:/a/b/c/",
             "toto:/x/y/z/",
             "../../../x/y/z/");
         testRelativizeAndResolve(
             "toto:/a/b/c/",
             "toto:/x/y/z",
             "../../../x/y/z");
         testRelativizeAndResolve("toto:/a/b/c", "toto:/x/y/z", "../../x/y/z");
         testRelativizeAndResolve("toto:a/b/c", "toto:x/y/z", "../../x/y/z");
     }
 
     /**
      * @throws Exception
      */
     public void testRelativizeWithAuthority() throws Exception {
         testRelativize("x://www.y.com", "x://www.y.com/a", "/a");
         testRelativize("x://www.y.com/", "x://www.y.com/a", "a");
         testRelativize("x://www.y.com/", "x://www.y.com/a/b", "a/b");
         testRelativize("x://www.y.com", "x://www.y.com/a/b", "/a/b");
         testRelativize("x://www.y.com", "x://www.y.com/a/b/", "/a/b/");
         testRelativize("x://www.y.com/a", "x://www.y.com/a/b/", "a/b/");
         testRelativize("x://www.y.com/a/", "x://www.y.com/a/b/", "b/");
         testRelativize("x://www.y.com/a/", "x://www.y.com/a/b", "b");
 
         testRelativize("x://www.y.com/a/b", "x://www.y.com/a/c", "c");
         testRelativize("x://www.y.com/a/b", "x://www.y.com/a/c/d", "c/d");
         testRelativize("x://www.y.com/a/b/", "x://www.y.com/a/c/d", "../c/d");
 
         testRelativize("x://www.y.coma/b", "x://www.y.coma/c", "c");
         testRelativize("x://www.y.coma/b", "x://www.y.coma/c/d", "c/d");
         testRelativize("x://www.y.coma/b", "x://www.y.coma/c/d/", "c/d/");
         testRelativize("x://www.y.coma/b/", "x://www.y.coma/c/d", "../c/d");
         testRelativize("x://www.y.coma/b/", "x://www.y.coma/c/d/", "../c/d/");
 
         testRelativize("x://www.y.com/a/b/c", "x://www.y.com/a/b/c/d", "c/d");
         testRelativize("x://www.y.com/a/b/c/", "x://www.y.com/a/b/c/d", "d");
 
         testRelativize("x://www.y.com/a/b/c", "x://www.y.com/a/b/d", "d");
         testRelativize("x://www.y.com/a/b/c/", "x://www.y.com/a/b/d", "../d");
         testRelativize("x://www.y.com/a/b/c/", "x://www.y.com/a/b/d/", "../d/");
         testRelativize(
             "x://www.y.com/a/b/c/",
             "x://www.y.com/x/y/z/",
             "../../../x/y/z/");
         testRelativize(
             "x://www.y.com/a/b/c/",
             "x://www.y.com/x/y/z",
             "../../../x/y/z");
         testRelativize(
             "x://www.y.com/a/b/c",
             "x://www.y.com/x/y/z",
             "../../x/y/z");
 
         // Different schemas
         testRelativize("x://www.y.com", "tata:", "tata:");
         testRelativize("x://www.y.com/", "tata:/a", "tata:///a");
         testRelativize("x://www.y.com/", "tata:/a/b", "tata:///a/b");
         testRelativize("x://www.y.com", "tata:/a", "tata:///a");
         testRelativize("x://www.y.com", "tata:/a/b", "tata:///a/b");
         testRelativize("x://www.y.com/", "tata:/a", "tata:///a");
         testRelativize("x://www.y.com/", "tata:/a/b", "tata:///a/b");
 
         testRelativize("x://www.y.com/a", "tata:/a/b/", "tata:///a/b/");
         testRelativize("x://www.y.com/a/", "tata:/a/b/", "tata:///a/b/");
         testRelativize("x://www.y.com/a/", "tata:/a/b", "tata:///a/b");
 
     }
 
     /**
      * @throws Exception
      */
     public void testRelativizeWithParameters() throws Exception {
         testRelativize("", "/a?a=b&c=d#xyz", "/a?a=b&c=d#xyz");
         testRelativize("/", "/a?a=b&c=d#xyz", "a?a=b&c=d#xyz");
         testRelativize("/", "/a/b?a=b&c=d#xyz", "a/b?a=b&c=d#xyz");
         testRelativize("", "/a/b?a=b&c=d#xyz", "/a/b?a=b&c=d#xyz");
         testRelativize("", "/a/b/?a=b&c=d#xyz", "/a/b/?a=b&c=d#xyz");
         testRelativize("/a", "/a/b/?a=b&c=d#xyz", "a/b/?a=b&c=d#xyz");
         testRelativize("/a/", "/a/b/?a=b&c=d#xyz", "b/?a=b&c=d#xyz");
         testRelativize("/a/", "/a/b?a=b&c=d#xyz", "b?a=b&c=d#xyz");
 
         testRelativize("/a/b", "/a/c?a=b&c=d#xyz", "c?a=b&c=d#xyz");
         testRelativize("/a/b", "/a/c/d?a=b&c=d#xyz", "c/d?a=b&c=d#xyz");
         testRelativize("/a/b/", "/a/c/d?a=b&c=d#xyz", "../c/d?a=b&c=d#xyz");
 
         testRelativize("/a/b/c", "/a/b/c/d?a=b&c=d#xyz", "c/d?a=b&c=d#xyz");
         testRelativize("/a/b/c/", "/a/b/c/d?a=b&c=d#xyz", "d?a=b&c=d#xyz");
 
         testRelativize("/a/b/c", "/a/b/d?a=b&c=d#xyz", "d?a=b&c=d#xyz");
         testRelativize("/a/b/c/", "/a/b/d?a=b&c=d#xyz", "../d?a=b&c=d#xyz");
         testRelativize("/a/b/c/", "/a/b/d/?a=b&c=d#xyz", "../d/?a=b&c=d#xyz");
         testRelativize(
             "/a/b/c/",
             "/x/y/z/?a=b&c=d#xyz",
             "../../../x/y/z/?a=b&c=d#xyz");
         testRelativize(
             "/a/b/c/",
             "/x/y/z?a=b&c=d#xyz",
             "../../../x/y/z?a=b&c=d#xyz");
         testRelativize(
             "/a/b/c",
             "/x/y/z?a=b&c=d#xyz",
             "../../x/y/z?a=b&c=d#xyz");
         testRelativize("a/b/c", "x/y/z?a=b&c=d#xyz", "../../x/y/z?a=b&c=d#xyz");
 
         testRelativize("a/b/c", "x/../y/z?a=b&c=d#xyz", "../../y/z?a=b&c=d#xyz");
         testRelativize(
             "a/b/c",
             "./x/../y/../z?a=b&c=d#xyz",
             "../../z?a=b&c=d#xyz");
         testRelativize(
             "a/b/c",
             "../../x/../y/../z?a=b&c=d#xyz",
             "../../z?a=b&c=d#xyz");
         testRelativize(
             "A/B/a/b/c",
             "../x/../y/../z?a=b&c=d#xyz",
             "../../../../z?a=b&c=d#xyz");
     }
 
     /**
      * @throws Exception
      */
     public void testRelativizeWithSchemas() throws Exception {
         testRelativize("toto:", "toto:/a", "/a");
         testRelativize("toto:/", "toto:/a", "a");
         testRelativize("toto:/", "toto:/a/b", "a/b");
         testRelativize("toto:", "toto:/a/b", "/a/b");
         testRelativize("toto:", "toto:/a/b/", "/a/b/");
         testRelativize("toto:/a", "toto:/a/b/", "a/b/");
         testRelativize("toto:/a/", "toto:/a/b/", "b/");
         testRelativize("toto:/a/", "toto:/a/b", "b");
 
         testRelativize("toto:/a/b", "toto:/a/c", "c");
         testRelativize("toto:/a/b", "toto:/a/c/d", "c/d");
         testRelativize("toto:/a/b/", "toto:/a/c/d", "../c/d");
 
         testRelativize("toto:a/b", "toto:a/c", "c");
         testRelativize("toto:a/b", "toto:a/c/d", "c/d");
         testRelativize("toto:a/b", "toto:a/c/d/", "c/d/");
         testRelativize("toto:a/b/", "toto:a/c/d", "../c/d");
         testRelativize("toto:a/b/", "toto:a/c/d/", "../c/d/");
 
         testRelativize("toto:/a/b/c", "toto:/a/b/c/d", "c/d");
         testRelativize("toto:/a/b/c/", "toto:/a/b/c/d", "d");
 
         testRelativize("toto:/a/b/c", "toto:/a/b/d", "d");
         testRelativize("toto:/a/b/c/", "toto:/a/b/d", "../d");
         testRelativize("toto:/a/b/c/", "toto:/a/b/d/", "../d/");
         testRelativize("toto:/a/b/c/", "toto:/x/y/z/", "../../../x/y/z/");
         testRelativize("toto:/a/b/c/", "toto:/x/y/z", "../../../x/y/z");
         testRelativize("toto:/a/b/c", "toto:/x/y/z", "../../x/y/z");
         testRelativize("toto:a/b/c", "toto:x/y/z", "../../x/y/z");
 
         testRelativize("toto:a/b/c", "toto:x/../y/z", "../../y/z");
         testRelativize("toto:a/b/c", "toto:./x/../y/../z", "../../z");
         testRelativize("toto:a/b/c", "toto:../../x/../y/../z", "../../z");
         testRelativize("toto:A/B/a/b/c", "toto:../x/../y/../z", "../../../../z");
 
         // Different schemas
         testRelativize("toto:", "tata:", "tata:");
         testRelativize("toto:/", "tata:/a", "tata:///a");
         testRelativize("toto:/", "tata:/a/b", "tata:///a/b");
         testRelativize("toto:", "tata:/a", "tata:///a");
         testRelativize("toto:", "tata:/a/b", "tata:///a/b");
         testRelativize("toto:/", "tata:/a", "tata:///a");
         testRelativize("toto:/", "tata:/a/b", "tata:///a/b");
 
         testRelativize("toto:/a", "tata:/a/b/", "tata:///a/b/");
         testRelativize("toto:/a/", "tata:/a/b/", "tata:///a/b/");
         testRelativize("toto:/a/", "tata:/a/b", "tata:///a/b");
 
     }
 
     public void testResolve() {
         testResolve(
             "http://www.foo.bar/xyz",
             "#toto",
             "http://www.foo.bar/xyz#toto");
         testResolve(
             "http://www.foo.bar/xyz",
             "abc#toto",
             "http://www.foo.bar/abc#toto");
         testResolve(
             "http://www.foo.bar/xyz/",
             "abc#toto",
             "http://www.foo.bar/xyz/abc#toto");
         testResolve(
             "http://www.foo.bar/xyz/",
             "../abc#toto",
             "http://www.foo.bar/abc#toto");
 
         testResolve(
             "http://www.foo.bar/#123",
             "toto",
             "http://www.foo.bar/toto");
         testResolve(
             "http://www.foo.bar/#123",
             "#toto",
             "http://www.foo.bar/#toto");
         testResolve(
             "http://www.foo.bar/xyz#123",
             "#toto",
             "http://www.foo.bar/xyz#toto");
         testResolve(
             "http://www.foo.bar/xyz/#123",
             "#toto",
             "http://www.foo.bar/xyz/#toto");
     }
 
     protected void testResolve(String first, String second, String control) {
         Uri.Builder a = new Uri.Builder(first);
         Uri.Builder b = new Uri.Builder(second);
         Uri.Builder test = a.getResolved(b);
         assertNotNull(test);
         assertEquals(control, test.toString());
     }
 
     /**
      * @throws Exception
      */
     public void testResolveWithParameters() throws Exception {
         testResolve("/", ".?a=b&c=d#xyz", "?a=b&c=d#xyz");
         testResolve("/a/b/c", ".?a=b&c=d#xyz", "/a/b/?a=b&c=d#xyz");
         testResolve("/a/b/c", "././.?a=b&c=d#xyz", "/a/b/?a=b&c=d#xyz");
         testResolve("/a/b/c", "d/././.?a=b&c=d#xyz", "/a/b/d/?a=b&c=d#xyz");
         testResolve("/a/b/c", "d?a=b&c=d#xyz", "/a/b/d?a=b&c=d#xyz");
         testResolve("/a/b/c/", "d?a=b&c=d#xyz", "/a/b/c/d?a=b&c=d#xyz");
         testResolve("/a/b/c/", "../d?a=b&c=d#xyz", "/a/b/d?a=b&c=d#xyz");
         testResolve("/a/b/c/", "../../d?a=b&c=d#xyz", "/a/d?a=b&c=d#xyz");
         testResolve("/a/b/c/", "../../d/../f?a=b&c=d#xyz", "/a/f?a=b&c=d#xyz");
         testResolve(
             "/a/b/c/",
             "././d/./e?a=b&c=d#xyz",
             "/a/b/c/d/e?a=b&c=d#xyz");
         testResolve(
             "/a/b/c/",
             "././d/./e/./.?a=b&c=d#xyz",
             "/a/b/c/d/e/?a=b&c=d#xyz");
         testResolve(
             "/a/b/c/",
             "./././d/../e?a=b&c=d#xyz",
             "/a/b/c/e?a=b&c=d#xyz");
         testResolve("/a/b/c/", "../../../d/../e?a=b&c=d#xyz", "/e?a=b&c=d#xyz");
         testResolve("/a/b/c", "./e/?a=b&c=d#xyz", "/a/b/e/?a=b&c=d#xyz");
         testResolve("/a/b/c", "/e/?a=b&c=d#xyz", "/e/?a=b&c=d#xyz");
         testResolve("/a/b/c", "d/e/f?a=b&c=d#xyz", "/a/b/d/e/f?a=b&c=d#xyz");
         testResolve(
             "/a/b/c",
             "d////e/./././f?a=b&c=d#xyz",
             "/a/b/d/e/f?a=b&c=d#xyz");
         testResolve(
             "/a/b/c/",
             "d////e/./././f?a=b&c=d#xyz",
             "/a/b/c/d/e/f?a=b&c=d#xyz");
         testResolve("/", "e/?a=b&c=d#xyz", "/e/?a=b&c=d#xyz");
         testResolve("", "e?a=b&c=d#xyz", "e?a=b&c=d#xyz");
         testResolve("", "e/?a=b&c=d#xyz", "e/?a=b&c=d#xyz");
         testResolve("a/b", ".././?a=b&c=d#xyz", "/?a=b&c=d#xyz");
         testResolve("/a/b", ".././?a=b&c=d#xyz", "/?a=b&c=d#xyz");
         testResolve("/a/b", "../.?a=b&c=d#xyz", "?a=b&c=d#xyz");
         testResolve("/a/b", "../..?a=b&c=d#xyz", "?a=b&c=d#xyz");
         testResolve("a/b", "../.?a=b&c=d#xyz", "?a=b&c=d#xyz");
         testResolve("a/b", "../..?a=b&c=d#xyz", "?a=b&c=d#xyz");
     }
 
     /**
      * 
      */
     public void testRfcExamples() {
         testUri(
             "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles",
             "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los+Angeles",
             "gopher",
             "spinaltap.micro.umn.edu",
             "/00/Weather/California/Los+Angeles",
             null,
             null);
 
         testUri(
             "foo://example.com:8042/over/there?name=ferret#nose",
             "foo",
             "example.com:8042",
             "/over/there",
             "name=ferret",
             "nose");
 
         assertEquals("urn:example:animal:ferret://nose", new Uri(
             "urn:example:animal:ferret:nose").toString());
         testUri(
             "urn:example:animal:ferret:nose",
             "urn:example:animal:ferret://nose",
             "urn:example:animal:ferret",
             null,
             "nose",
             null,
             null);
         testUri(
             "ftp://ftp.is.co.za/rfc/rfc1808.txt",
             "ftp",
             "ftp.is.co.za",
             "/rfc/rfc1808.txt",
             null,
             null);
 
         // In the original example the control URI is
         // "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles",
         testUri(
             "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles",
             "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los+Angeles",
             "gopher",
             "spinaltap.micro.umn.edu",
             "/00/Weather/California/Los+Angeles",
             null,
             null);
 
         testUri(
             "http://www.math.uio.no/faq/compression-faq/part1.html",
             "http",
             "www.math.uio.no",
             "/faq/compression-faq/part1.html",
             null,
             null);
         testUri(
             "http://localhost:8080/test/abc/part1.html",
             "http",
             "localhost:8080",
             "/test/abc/part1.html",
             null,
             null);
         testUri(
             "http://myName:myPassword@localhost:8080/test/abc/part1.html",
             "http",
             "myName:myPassword@localhost:8080",
             "/test/abc/part1.html",
             null,
             null);
         testUri(
             "mailto:mduerst@ifi.unizh.ch",
             "mailto://mduerst@ifi.unizh.ch",
             "mailto",
             null,
             "mduerst@ifi.unizh.ch",
             null,
             null);
         testUri(
             "news:comp.infosystems.www.servers.unix",
             "news://comp.infosystems.www.servers.unix",
             "news",
             null,
             "comp.infosystems.www.servers.unix",
             null,
             null);
         testUri(
             "telnet://melvyl.ucop.edu/",
             "telnet",
             "melvyl.ucop.edu",
             "/",
             null,
             null);
 
         testUri(":zz", "://zz", null, null, "zz", null, null);
         testUri("xx:yy:zz", "xx:yy://zz", "xx:yy", null, "zz", null, null);
         testUri("xx:::yy:zz", "xx:::yy://zz", "xx:::yy", null, "zz", null, null);
         testUri("//host/path", null, "host", "/path", null, null);
         testUri("toto:/titi", "toto:///titi", "toto", null, "/titi", null, null);
         testUri("toto:titi", "toto://titi", "toto", null, "titi", null, null);
         testUri("toto/titi/tata", null, null, "toto/titi/tata", null, null);
         testUri("/toto/titi/tata", null, null, "/toto/titi/tata", null, null);
         testUri("toto", null, null, "toto", null, null);
         testUri("../../toto", null, null, "../../toto", null, null);
         testUri("./.././../toto", null, null, "./.././../toto", null, null);
         testUri(".", null, null, ".", null, null);
         testUri("..\\..\\toto", null, null, "../../toto", null, null);
         testUri("rdf:type", "rdf://type", "rdf", null, "type", null, null);
         testUri(
             "//host:123/a/b/c",
             "//host:123/a/b/c",
             null,
             "host:123",
             "/a/b/c",
             null,
             null);
 
         testUri(
             "//login:password@:123/a/b/c",
             null,
             "login:password@:123",
             "/a/b/c",
             null,
             null);
 
         Uri uri = new Uri("news:toto");
         assertEquals("news://toto", uri.getUri());
 
         // Formally "invalid" uris
         testUri("123://host/a/b/c", "123", "host", "/a/b/c", null, null);
 
         uri = new Uri("news:toto:titi:tata/x/y/z");
         assertEquals("news:toto:titi://tata/x/y/z", uri.getUri());
         assertEquals("news:toto:titi", uri.getScheme());
         Path path = uri.getPath();
         assertEquals("tata/x/y/z", path.getPath(true));
         assertEquals(4, path.getPathSegmentCount());
         assertEquals("tata", path.getPathSegment(0));
         assertEquals("x", path.getPathSegment(1));
         assertEquals("y", path.getPathSegment(2));
         assertEquals("z", path.getPathSegment(3));
 
         uri = new Uri("news:toto:titi:tata/x/y/z");
         assertEquals("news:toto:titi://tata/x/y/z", uri.getUri());
         assertEquals("news:toto:titi", uri.getScheme());
         assertEquals(3, uri.getSchemeSegmentCount());
         assertEquals("news", uri.getSchemeSegment(0));
         assertEquals("toto", uri.getSchemeSegment(1));
         assertEquals("titi", uri.getSchemeSegment(2));
 
         uri = uri.getBuilder().setScheme("A:B").build();
         assertEquals("A:B://tata/x/y/z", uri.getUri());
         assertEquals("A:B", uri.getScheme());
         assertEquals(2, uri.getSchemeSegmentCount());
         assertEquals("A", uri.getSchemeSegment(0));
         assertEquals("B", uri.getSchemeSegment(1));
 
         uri = uri.getBuilder().appendSchemeSegments("1:2", true).build();
         assertEquals("1:2:A:B://tata/x/y/z", uri.getUri());
         assertEquals("1:2:A:B", uri.getScheme());
         assertEquals(4, uri.getSchemeSegmentCount());
         assertEquals("1", uri.getSchemeSegment(0));
         assertEquals("2", uri.getSchemeSegment(1));
         assertEquals("A", uri.getSchemeSegment(2));
         assertEquals("B", uri.getSchemeSegment(3));
 
         uri = uri.getBuilder().appendSchemeSegments("3:4", false).build();
         assertEquals("1:2:A:B:3:4://tata/x/y/z", uri.getUri());
         assertEquals("1:2:A:B:3:4", uri.getScheme());
         assertEquals(6, uri.getSchemeSegmentCount());
         assertEquals("1", uri.getSchemeSegment(0));
         assertEquals("2", uri.getSchemeSegment(1));
         assertEquals("A", uri.getSchemeSegment(2));
         assertEquals("B", uri.getSchemeSegment(3));
         assertEquals("3", uri.getSchemeSegment(4));
         assertEquals("4", uri.getSchemeSegment(5));
 
         uri = uri.getBuilder().removeFirstSchemeSegments(2).build();
         assertEquals("A:B:3:4://tata/x/y/z", uri.getUri());
         assertEquals("A:B:3:4", uri.getScheme());
         assertEquals(4, uri.getSchemeSegmentCount());
         assertEquals("A", uri.getSchemeSegment(0));
         assertEquals("B", uri.getSchemeSegment(1));
         assertEquals("3", uri.getSchemeSegment(2));
         assertEquals("4", uri.getSchemeSegment(3));
 
         uri = uri.getBuilder().removeLastSchemeSegments(2).build();
         assertEquals("A:B://tata/x/y/z", uri.getUri());
         assertEquals("A:B", uri.getScheme());
         assertEquals(2, uri.getSchemeSegmentCount());
         assertEquals("A", uri.getSchemeSegment(0));
         assertEquals("B", uri.getSchemeSegment(1));
 
         uri = uri.getBuilder().removeLastSchemeSegments(2).build();
         assertEquals("tata/x/y/z", uri.getUri());
         assertEquals(null, uri.getScheme());
         assertEquals(0, uri.getSchemeSegmentCount());
 
         uri = new Uri("A:B:tata/x/y/z");
         uri = uri.getBuilder().removeFirstSchemeSegments(2).build();
         assertEquals("tata/x/y/z", uri.getUri());
         assertEquals(null, uri.getScheme());
         assertEquals(0, uri.getSchemeSegmentCount());
 
         uri = new Uri("A:B:tata/x/y/z");
         uri = uri.getBuilder().removeFirstSchemeSegments(10).build();
         assertEquals("tata/x/y/z", uri.getUri());
         assertEquals(null, uri.getScheme());
         assertEquals(0, uri.getSchemeSegmentCount());
 
         path = uri.getPath();
         assertEquals("tata/x/y/z", path.getPath(true));
         assertEquals(4, path.getPathSegmentCount());
         assertEquals("tata", path.getPathSegment(0));
         assertEquals("x", path.getPathSegment(1));
         assertEquals("y", path.getPathSegment(2));
         assertEquals("z", path.getPathSegment(3));
 
         testUri(
             "axis:http://localhost:8081/services",
             "axis:http",
             "localhost:8081",
             "/services",
             null,
             null);
 
     }
 
     public void testScheme() {
         Uri.Builder uri = new Uri.Builder("");
         uri = uri.setScheme("xxx");
         assertEquals("xxx:", uri.toString());
         assertEquals(new Uri("xxx:"), uri);
 
         uri = new Uri.Builder("mailto:abc@foo.bar");
         assertEquals("mailto", uri.getScheme());
         Path path = uri.getPath();
         assertEquals("abc@foo.bar", path.getPathSegment(0));
     }
 
     public void testUri() {
         testUri("a:b", "a://b", "a", null, "b", null, null);
         testUri(
             "toto:x:y:z/a/b/c?x=y&a=b#fragment",
             "toto:x:y://z/a/b/c?x=y&a=b#fragment",
             "toto:x:y",
             null,
             "z/a/b/c",
             "x=y&a=b",
             "fragment");
         testUri(
             "toto:x:y://a:b@www.google.com:80/z/a/b/c?x=y&a=b#fragment",
             "toto:x:y",
             "a:b@www.google.com:80",
             "/z/a/b/c",
             "x=y&a=b",
             "fragment");
     }
 
     /**
      * @param uri
      * @param scheme
      * @param authority
      * @param path
      * @param query
      * @param fragment
      */
     private void testUri(
         String str,
         String scheme,
         String authority,
         String path,
         String query,
         String fragment) {
         String control = str.replace('\\', '/');
         testUri(str, control, scheme, authority, path, query, fragment);
     }
 
     /**
      * @param uri
      * @param scheme
      * @param authority
      * @param path
      * @param query
      * @param fragment
      */
     private void testUri(
         String str,
         String control,
         String scheme,
         String authority,
         String path,
         String query,
         String fragment) {
         Uri uri = new Uri(str);
         String test = uri.getScheme();
         assertEquals(scheme, test);
         test = uri.getAuthority();
         assertEquals(authority, test);
         test = uri.getPath().toString();
         assertEquals(path, test);
         test = uri.getQuery();
         assertEquals(query, test);
         test = uri.getFragment();
         assertEquals(fragment, test);
         test = uri.getUri();
         assertEquals(control, test);
     }
 
     /**
      * 
      */
     public void testUriModifications() {
         Uri.Builder uri = new Uri.Builder("a/b/c/file.txt?a=b&c=d");
         Builder pathBuilder = uri.getPathBuilder();
         pathBuilder.removeLastPathSegments(1, true);
         assertEquals("a/b/file.txt?a=b&c=d", uri.toString());
         pathBuilder.removeLastPathSegments(1, true);
         assertEquals("a/file.txt?a=b&c=d", uri.toString());
         pathBuilder.removeLastPathSegments(1, true);
         assertEquals("file.txt?a=b&c=d", uri.toString());
         pathBuilder.removeLastPathSegments(1, true);
         assertEquals("file.txt?a=b&c=d", uri.toString());
 
         uri = new Uri.Builder("a/b/c/file.txt?a=b&c=d");
         pathBuilder = uri.getPathBuilder();
         pathBuilder.removeLastPathSegments(1, false);
         assertEquals("a/b/c?a=b&c=d", uri.toString());
         pathBuilder.removeLastPathSegments(1, false);
         assertEquals("a/b?a=b&c=d", uri.toString());
         pathBuilder.removeLastPathSegments(1, false);
         assertEquals("a?a=b&c=d", uri.toString());
         pathBuilder.removeLastPathSegments(1, false);
         assertEquals("?a=b&c=d", uri.toString());
 
         uri = new Uri.Builder("a/b/c/file.txt?a=b&c=d");
         pathBuilder = uri.getPathBuilder();
         pathBuilder.setFileExtension("toto");
         assertEquals("a/b/c/file.toto?a=b&c=d", uri.toString());
 
         uri = new Uri.Builder("a/b/c/?a=b&c=d");
         pathBuilder.setFileExtension("toto");
         assertEquals("a/b/c/?a=b&c=d", uri.toString());
 
         uri = new Uri.Builder("a/b/c/file.txt?a=b&c=d");
         pathBuilder = uri.getPathBuilder();
         assertEquals("file.txt", pathBuilder.getFileName());
         assertEquals("file.txt", pathBuilder.getLastPathSegment());
 
         uri = new Uri.Builder("a/b/c/?a=b&c=d");
         pathBuilder = uri.getPathBuilder();
         assertEquals(null, pathBuilder.getFileName());
         assertEquals("c", pathBuilder.getLastPathSegment());
 
         uri = new Uri.Builder("a/b/c/file.txt?a=b&c=d");
         pathBuilder = uri.getPathBuilder();
         pathBuilder.setFileExtension("toto");
         assertEquals("a/b/c/file.toto?a=b&c=d", uri.toString());
 
         uri = new Uri.Builder("a/b/c/file.txt?a=b&c=d");
         pathBuilder = uri.getPathBuilder();
         pathBuilder.setFileName("titi.tata");
         assertEquals("a/b/c/titi.tata?a=b&c=d", uri.toString());
 
         uri = new Uri.Builder("a/b/c/file.txt?a=b&c=d");
         pathBuilder = uri.getPathBuilder();
         pathBuilder.setFileName(null);
         assertEquals("a/b/c/?a=b&c=d", uri.toString());
 
         uri = new Uri.Builder(
             "ftp://toto:titi@www.foo.com:123/a/b/c/file.txt?a=b&c=d#xx");
         assertEquals("/a/b/c/file.txt?a=b&c=d#xx", uri
             .getFullPathAsUri()
             .toString());
         assertEquals("/a/b/c/file.txt", uri.getPathAsUri().toString());
 
         // uri = uri.getAsPath();
         // assertEquals(new Uri(
         // "/ftp/toto_titi/www.foo.com_123/a/b/c/file.txt!a=b&c=d_xx"),
         // uri.toString());
 
     }
 
     /**
      * 
      */
     public void testUriModificationsWithAuthorities() {
         Uri u = new Uri(
             "http://user:pwd@www.foo.bar:8080/a/b/c?x=y&a=b#fragment");
         assertEquals(
             "http://user:pwd@www.foo.bar:8080/a/b/c?x=y&a=b#fragment",
             u.getUri());
 
         // User info
         Uri uri = u.getBuilder().setUserInfo(null).build();
         assertEquals(
             "http://www.foo.bar:8080/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         uri = u.getBuilder().setUserInfo("user1:pwd1").build();
         assertEquals(
             "http://user1:pwd1@www.foo.bar:8080/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         // Hosts
         uri = u.getBuilder().setHost("bar.com").build();
         assertEquals("http://user:pwd@bar.com:8080/a/b/c?x=y&a=b#fragment", uri
             .getUri()
             .toString());
 
         // Ports
         uri = u.getBuilder().setPort(8181).build();
         assertEquals(
             "http://user:pwd@www.foo.bar:8181/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         // Authority
         uri = u.getBuilder().setAuthority("www.google.com", 80).build();
         assertEquals(
             "http://www.google.com:80/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         uri = u.getBuilder().setAuthority("a:b", "www.google.com", 80).build();
         assertEquals(
             "http://a:b@www.google.com:80/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         uri = u.getBuilder().setAuthority(null).build();
         assertEquals("http:///a/b/c?x=y&a=b#fragment", uri.getUri());
 
         uri = u.getBuilder().setAuthority("a:b@www.google.com:80").build();
         assertEquals(
             "http://a:b@www.google.com:80/a/b/c?x=y&a=b#fragment",
             uri.getUri());
         assertEquals(80, uri.getPort());
         assertEquals("www.google.com", uri.getHost());
         assertEquals("a:b", uri.getUserInfo());
         assertEquals("http", uri.getScheme());
 
         // Local path
         Uri.Builder builder = u.getBuilder();
         Builder pathBuilder = builder.getPathBuilder();
         pathBuilder.setPath("/x/y/z");
         assertEquals(
             "http://user:pwd@www.foo.bar:8080/x/y/z?x=y&a=b#fragment",
             builder.getUri());
 
         pathBuilder.setPath((String) null);
         assertEquals(
             "http://user:pwd@www.foo.bar:8080?x=y&a=b#fragment",
             builder.getUri());
 
         pathBuilder.setPath("/");
         assertEquals(
             "http://user:pwd@www.foo.bar:8080/?x=y&a=b#fragment",
             builder.getUri());
 
         // Full path
         builder.setFullPath((String) null);
         assertEquals("http://user:pwd@www.foo.bar:8080", builder.getUri());
 
         builder.setFullPath("/");
         assertEquals("http://user:pwd@www.foo.bar:8080/", builder.getUri());
 
         // Full path
         builder.setFullPath("x/y");
         assertEquals("http://user:pwd@www.foo.bar:8080/x/y", builder.getUri());
 
         builder.setFullPath("x/y/?a=b&c=d");
         assertEquals(
             "http://user:pwd@www.foo.bar:8080/x/y/?a=b&c=d",
             builder.getUri());
 
         assertEquals("/x/y/", pathBuilder.getPath(true));
         assertEquals("a=b&c=d", builder.getQuery());
 
         builder.setFullPath("x/y/?a=b&c=d#Frag1");
         assertEquals(
             "http://user:pwd@www.foo.bar:8080/x/y/?a=b&c=d#Frag1",
             builder.getUri());
         assertEquals("/x/y/", pathBuilder.getPath(true));
         assertEquals("a=b&c=d", builder.getQuery());
         assertEquals("Frag1", builder.getFragment());
 
         builder.setFullPath("#Frag1");
         assertEquals("http://user:pwd@www.foo.bar:8080#Frag1", builder.getUri());
         assertNull(pathBuilder.getPath(true));
         assertNull(builder.getQuery());
         assertEquals("Frag1", builder.getFragment());
 
         builder.setFullPath("/#Frag1");
         assertEquals(
             "http://user:pwd@www.foo.bar:8080/#Frag1",
             builder.getUri());
         assertEquals("/", pathBuilder.getPath(true));
         assertNull(builder.getQuery());
         assertEquals("Frag1", builder.getFragment());
 
         builder.setAuthority("a:b", null, 80);
         assertEquals("http://a:b@:80/#Frag1", builder.getUri());
 
         builder.setAuthority(null, null, 80);
         assertEquals("http://:80/#Frag1", builder.getUri());
 
         builder.setUri("http://:80/a/b/c?x=y&a=b#fragment");
         pathBuilder = builder.getPathBuilder();
         assertEquals(80, builder.getPort());
         assertEquals(null, builder.getHost());
         assertEquals("http", builder.getScheme());
         assertEquals("/a/b/c", pathBuilder.getPath(true));
         assertEquals("http://:80/a/b/c?x=y&a=b#fragment", builder.getUri());
 
     }
 
     /**
      * 
      */
     public void testUriModificationsWithoutAuthorities() {
         Uri.Builder uri = new Uri.Builder("toto:x:y:z/a/b/c?x=y&a=b#fragment");
         assertEquals("toto:x:y://z/a/b/c?x=y&a=b#fragment", uri.getUri());
 
         // User info
         uri.setUserInfo(null);
         assertEquals("toto:x:y://z/a/b/c?x=y&a=b#fragment", uri.getUri());
         uri.setUserInfo("user1:pwd1");
         assertEquals(
             "toto:x:y://user1:pwd1@/z/a/b/c?x=y&a=b#fragment",
             uri.getUri());
         // assertEquals("z", uri.getHost());
         // assertEquals("/a/b/c", uri.getPath().toString());
 
         // Hosts
         uri.setHost("bar.com");
         assertEquals(
             "toto:x:y://user1:pwd1@bar.com/z/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         // Ports
         uri.setPort(8181);
         assertEquals(
             "toto:x:y://user1:pwd1@bar.com:8181/z/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         uri.setUri("toto:x:y://:8181/z/a/b/c?x=y&a=b#fragment");
         assertEquals("toto:x:y://:8181/z/a/b/c?x=y&a=b#fragment", uri
             .build()
             .toString());
         assertNull(uri.getHost());
         assertNull("x:y", uri.getUserInfo());
         assertEquals(":8181", uri.getAuthority());
         assertEquals(8181, uri.getPort());
 
         // Authority
         uri.setAuthority("www.google.com", 80);
         assertEquals(
             "toto:x:y://www.google.com:80/z/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         uri.setAuthority("a:b", "www.google.com", 80);
         assertEquals(
             "toto:x:y://a:b@www.google.com:80/z/a/b/c?x=y&a=b#fragment",
             uri.getUri());
 
         uri.setAuthority(null);
         assertEquals("toto:x:y:///z/a/b/c?x=y&a=b#fragment", uri.getUri());
 
         uri.setAuthority("a:b@www.google.com:80");
         assertEquals(
             "toto:x:y://a:b@www.google.com:80/z/a/b/c?x=y&a=b#fragment",
             uri.getUri());
         assertEquals(80, uri.getPort());
         assertEquals("www.google.com", uri.getHost());
         assertEquals("a:b", uri.getUserInfo());
         assertEquals("toto:x:y", uri.getScheme());
 
         // Local path
         Builder path = uri.getPathBuilder();
         path.setPath("/x/y/z");
         assertEquals(
             "toto:x:y://a:b@www.google.com:80/x/y/z?x=y&a=b#fragment",
             uri.getUri());
         path.setPath("x/y/z");
         assertEquals(
             "toto:x:y://a:b@www.google.com:80/x/y/z?x=y&a=b#fragment",
             uri.getUri());
         path.setPath("x/y/z/");
         assertEquals(
             "toto:x:y://a:b@www.google.com:80/x/y/z/?x=y&a=b#fragment",
             uri.getUri());
 
         path.setPath((String) null);
         assertEquals(
             "toto:x:y://a:b@www.google.com:80?x=y&a=b#fragment",
             uri.getUri());
 
         // Full path
         uri.setFullPath((String) null);
         assertEquals("toto:x:y://a:b@www.google.com:80", uri.getUri());
 
         // Full path
         uri.setAuthority(null);
         path.setFullPath("x/y");
         assertEquals("toto:x:y://x/y", uri.getUri());
 
         path.setFullPath("x/y/?a=b&c=d");
         assertEquals("toto:x:y://x/y/", uri.getUri());
         assertEquals("x/y/", path.getPath(true));
         assertNull(uri.getQuery());
 
         uri.setFullPath("x/y/?a=b&c=d#Frag1");
         assertEquals("toto:x:y://x/y/?a=b&c=d#Frag1", uri.getUri());
         assertEquals("x/y/", path.getPath(true));
         assertEquals("a=b&c=d", uri.getQuery());
         assertEquals("Frag1", uri.getFragment());
 
         uri.setFullPath("#Frag1");
         assertEquals("toto:x:y://#Frag1", uri.getUri());
         assertNull(path.getPath(true));
         assertNull(uri.getQuery());
         assertEquals("Frag1", uri.getFragment());
 
         uri.setAuthority("a:b", null, 80);
         assertEquals("toto:x:y://a:b@:80#Frag1", uri.getUri());
 
         uri.setAuthority(null, null, 80);
         assertEquals("toto:x:y://:80#Frag1", uri.getUri());
         uri = new Uri.Builder("toto:x:y://:80/z/a/b/c?x=y&a=b#fragment");
         assertEquals(80, uri.getPort());
         assertNull(uri.getHost());
         assertEquals("toto:x:y", uri.getScheme());
         assertEquals("/z/a/b/c", uri.getPath().getPath(true));
         assertEquals("toto:x:y://:80/z/a/b/c?x=y&a=b#fragment", uri.getUri());
 
     }
 
     /**
      * 
      */
     public void testWellFormedUri() {
         Uri uri = new Uri(
             "http://user:password@www.ics.uci.edu:8080/pub/ietf/uri/?x=y&a=b#Related");
         String test = uri.getScheme();
         assertEquals("http", test);
         test = uri.getUserInfo();
         assertEquals("user:password", test);
         test = uri.getHost();
         assertEquals("www.ics.uci.edu", test);
         int port = uri.getPort();
         assertEquals(8080, port);
         test = uri.getAuthority();
         assertEquals("user:password@www.ics.uci.edu:8080", test);
         test = uri.getPath(true);
         assertEquals("/pub/ietf/uri/", test);
         test = uri.getQuery();
         assertEquals("x=y&a=b", test);
         test = uri.getFragment();
         assertEquals("Related", test);
 
         uri = new Uri(
             "http://user:password@www.ics.uci.edu/pub/ietf/uri/?x=y&a=b#Related");
         test = uri.getScheme();
         assertEquals("http", test);
         test = uri.getUserInfo();
         assertEquals("user:password", test);
         test = uri.getHost();
         assertEquals("www.ics.uci.edu", test);
         port = uri.getPort();
         assertEquals(0, port);
         test = uri.getAuthority();
         assertEquals("user:password@www.ics.uci.edu", test);
         test = uri.getPath(true);
         assertEquals("/pub/ietf/uri/", test);
         test = uri.getQuery();
         assertEquals("x=y&a=b", test);
         test = uri.getFragment();
         assertEquals("Related", test);
 
         uri = new Uri(
             "http://user:password@www.ics.uci.edu:pub/ietf/uri/?x=y&a=b#Related");
         test = uri.getScheme();
         assertEquals("http", test);
         test = uri.getUserInfo();
         assertEquals("user:password", test);
         test = uri.getHost();
         assertEquals("www.ics.uci.edu:pub", test);
         port = uri.getPort();
         assertEquals(0, port);
         test = uri.getAuthority();
         assertEquals("user:password@www.ics.uci.edu:pub", test);
         test = uri.getPath(true);
         assertEquals("/ietf/uri/", test);
         test = uri.getQuery();
         assertEquals("x=y&a=b", test);
         test = uri.getFragment();
         assertEquals("Related", test);
 
         uri = new Uri("http://user:password@www.ics.uci.edu");
         test = uri.getScheme();
         assertEquals("http", test);
         test = uri.getUserInfo();
         assertEquals("user:password", test);
         test = uri.getHost();
         assertEquals("www.ics.uci.edu", test);
         port = uri.getPort();
         assertEquals(0, port);
         test = uri.getAuthority();
         assertEquals("user:password@www.ics.uci.edu", test);
         test = uri.getPath(true);
         assertEquals(null, test);
         test = uri.getQuery();
         assertEquals(null, test);
         test = uri.getFragment();
         assertEquals(null, test);
 
         uri = new Uri("http://host?#fragment");
         test = uri.getScheme();
         assertEquals("http", test);
         test = uri.getUserInfo();
         assertEquals(null, test);
         test = uri.getHost();
         assertEquals("host", test);
         port = uri.getPort();
         assertEquals(0, port);
         test = uri.getAuthority();
         assertEquals("host", test);
         test = uri.getPath(true);
         assertEquals(null, test);
         test = uri.getQuery();
         assertEquals(null, test);
         test = uri.getFragment();
         assertEquals("fragment", test);
         test = uri.getUri();
         assertEquals("http://host#fragment", test);
     }
 
 }
