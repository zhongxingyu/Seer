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
 
 import junit.framework.TestCase;
 
 /**
  * @author kotelnikov
  */
 public class PathTest extends TestCase {
 
     /**
      * Constructor for UriTest.
      * 
      * @param name
      */
     public PathTest(String name) {
         super(name);
     }
 
    public void testEmptyPath() {
        Path.Builder builder = new Path.Builder("/");
        assertEquals("/", builder.toString());
        builder.appendPath("");
        assertEquals("/", builder.toString());
    }

     public void testEscaped(String escaped, String nonEscaped) {
         testEscaped(escaped, escaped, nonEscaped);
     }
 
     public void testEscaped(String str, String escaped, String nonEscaped) {
         Path path = new Path(str);
         assertEquals(escaped, path.toString());
         assertEquals(escaped, path.getPath(true));
         assertEquals(nonEscaped, path.getPath(false));
 
         path = new Path(escaped);
         assertEquals(escaped, path.toString());
         assertEquals(escaped, path.getPath(true));
         assertEquals(nonEscaped, path.getPath(false));
 
         path = new Path(escaped);
         assertEquals(escaped, path.toString());
         assertEquals(escaped, path.getPath(true));
         assertEquals(nonEscaped, path.getPath(false));
     }
 
     public void testEscaping() {
         testEscaped(
             "/wikipedia/%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F",
             "/wikipedia/Россия");
         testEscaped(
             "/wikipedia/%d0%a0%d0%be%d1%81%d1%81%d0%b8%d1%8f",
             "/wikipedia/%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F",
             "/wikipedia/Россия");
         testEscaped(
             "Russia%20%d0%a0%d0%be%d1%81%d1%81%d0%b8%d1%8f",
             "Russia+%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F",
             "Russia+Россия");
         testEscaped("a%20b%20c", "a+b+c", "a+b+c");
         testEscaped(
             "http://www.foo.bar/toto/tata+titi",
             "http:/www.foo.bar/toto/tata+titi",
             "http:/www.foo.bar/toto/tata+titi");
 
     }
 
     protected void testFileName(String str, String name, String extension) {
         Path path = new Path(str);
         assertEquals(name, path.getFileNameWithoutExtension());
         assertEquals(extension, path.getFileExtension());
         if (extension == null) {
             assertEquals(name, path.getFileName());
         } else if (name != null) {
             assertEquals(name + "." + extension, path.getFileName());
         }
 
     }
 
     public void testFileNameAndFileExtension() {
         testFileName("", null, null);
         testFileName("/a/b/c/", null, null);
         testFileName("test", "test", null);
         testFileName("test.txt", "test", "txt");
         testFileName("test.a.b.c.txt", "test.a.b.c", "txt");
         testFileName("/a/b/c/test.a.b.c.txt", "test.a.b.c", "txt");
 
         Path.Builder path = new Path.Builder("test.txt");
         assertEquals("test.txt", path.getLastPathSegment());
         path = path.setFileExtension("toto");
         assertEquals("test.toto", path.toString());
 
         path = new Path.Builder("/a/b/c/test.txt");
         assertEquals("test.txt", path.getLastPathSegment());
         path = path.setFileExtension("toto");
         assertEquals("/a/b/c/test.toto", path.toString());
 
         path = new Path.Builder("/a/b/c/");
         path = path.setFileExtension("toto");
         assertEquals("/a/b/c/", path.toString());
         assertNull(path.getFileName());
         assertEquals("c", path.getLastPathSegment());
     }
 
     public void testPath() {
         testPath("a", "a", "a");
         testPath("a/b", "a/b", "a", "b");
         testPath("/a/b", "/a/b", "a", "b");
         testPath("/a/b/", "/a/b/", "a", "b");
         testPath("a b c d", "a+b+c+d", "a b c d");
         testPath("a b/c d", "a+b/c+d", "a b", "c d");
         testPath("a b/ c d", "a+b/+c+d", "a b", " c d");
         testPath("a b/ c d /", "a+b/+c+d+", "a b", " c d ");
         testPath("/ a b / c d /", "+a+b+/+c+d+", " a b ", " c d ");
         testPath("/../../", "/../../", "..", "..");
 
         // Parent path
         Path.Builder uri = new Path.Builder("/a/b/c/test.txt");
         uri = uri.getParent();
         assertEquals("/a/b/c/", uri.toString());
         uri = uri.getParent();
         assertEquals("/a/b/", uri.toString());
         uri = uri.getParent();
         assertEquals("/a/", uri.toString());
         uri = uri.getParent();
         assertEquals("/", uri.toString());
         uri = uri.getParent();
         assertEquals("/", uri.toString());
 
         uri = new Path.Builder("a/b/c/test.txt");
         uri = uri.getParent();
         assertEquals("a/b/c/", uri.toString());
         uri = uri.getParent();
         assertEquals("a/b/", uri.toString());
         uri = uri.getParent();
         assertEquals("a/", uri.toString());
         uri = uri.getParent();
         assertEquals("", uri.toString());
         uri = uri.getParent();
         assertEquals("", uri.toString());
     }
 
     protected void testPath(String str, String control, String... segments) {
         Path uri = new Path(str);
         assertEquals(control, control);
         int i = 0;
         for (String segment : segments) {
             assertEquals(segment, uri.getPathSegment(i++));
         }
         assertEquals(segments.length, i);
     }
 
     public void testPathAppend() {
         testPathAppend("/a/b/c/", "../", "/a/b/c/../");
 
         testPathAppend("", "/", "/");
         testPathAppend("", "/a/b/c", "/a/b/c");
         testPathAppend("", "/a/b/c/", "/a/b/c/");
         testPathAppend("/a/b/c/", "../", "/a/b/c/../");
         testPathAppend("", "/", "/");
     }
 
     private void testPathAppend(String uri, String path, String control) {
         Path c = new Path(control);
         Path.Builder b = new Path.Builder(uri);
         b.appendPath(path);
         Path test = b.build();
         assertEquals(c, test);
     }
 
     /**
      * @throws Exception
      */
     public void testRelativize() {
         Path a = new Path("/a/b");
         Path b = new Path("/a/b/c");
         Path relative = a.getRelative(b);
         assertEquals("b/c", relative.toString());
         relative = b.getRelative(a);
         assertEquals("../b", relative.toString());
 
         testRelativize("a/b/c", "x/y/z", "../../x/y/z");
         testRelativize("/a/b", "/a/c", "c");
         testRelativize("/a", "/a/b/", "a/b/");
         testRelativize("/a/", "/a/b/", "b/");
 
         testRelativize("", "/a", "/a");
         testRelativize("/", "/a", "a");
         testRelativize("/", "/a/b", "a/b");
         testRelativize("", "/a/b", "/a/b");
         testRelativize("", "/a/b/", "/a/b/");
         testRelativize("/a", "/a/b/", "a/b/");
         testRelativize("/a/", "/a/b/", "b/");
         testRelativize("/a/", "/a/b", "b");
 
         testRelativize("/a/b", "/a/c", "c");
         testRelativize("/a/b", "/a/c/d", "c/d");
         testRelativize("/a/b/", "/a/c/d", "../c/d");
 
         testRelativize("a/b", "a/c", "c");
         testRelativize("a/b", "a/c/d", "c/d");
         testRelativize("a/b", "a/c/d/", "c/d/");
         testRelativize("a/b/", "a/c/d", "../c/d");
         testRelativize("a/b/", "a/c/d/", "../c/d/");
 
         testRelativize("/a/b/c", "/a/b/c/d", "c/d");
         testRelativize("/a/b/c/", "/a/b/c/d", "d");
 
         testRelativize("/a/b/c", "/a/b/d", "d");
         testRelativize("/a/b/c/", "/a/b/d", "../d");
         testRelativize("/a/b/c/", "/a/b/d/", "../d/");
         testRelativize("/a/b/c/", "/x/y/z/", "../../../x/y/z/");
         testRelativize("/a/b/c/", "/x/y/z", "../../../x/y/z");
         testRelativize("/a/b/c", "/x/y/z", "../../x/y/z");
         testRelativize("a/b/c", "x/y/z", "../../x/y/z");
 
         testRelativize("a/b/c", "x/../y/z", "../../y/z");
         testRelativize("a/b/c", "./x/../y/../z", "../../z");
         testRelativize("a/b/c", "a/z", "../z");
         testRelativize("a/b/c", "z", "../../z");
         testRelativize("a/b/c/", "z", "../../../z");
         testRelativize("a/b/c", "z/", "../../z/");
         testRelativize("a/b/c/", "z/", "../../../z/");
         testRelativize("A/B/a/b/c", "../x/../y/../z", "../../../../z");
     }
 
     protected void testRelativize(String first, String second, String control) {
         Path a = new Path(first);
         Path b = new Path(second);
         Path relativePath = a.getRelative(b);
         assertNotNull(relativePath);
         assertEquals(control, relativePath.toString());
         Path c = new Path(control);
         assertEquals(c, relativePath);
     }
 
     /**
      * @throws Exception
      */
     public void testRelativizeAndResolve() {
         testRelativizeAndResolve("/a/b/c", "/a/b/", "./");
 
         testRelativizeAndResolve("/a/b/c", "/a/", "../");
         testRelativizeAndResolve("/a/b/c", "/a/b", "../b");
         testRelativizeAndResolve("/a/b/c", "/x/y/z", "../../x/y/z");
         testRelativizeAndResolve("/a/b/c/", "/a/b/d", "../d");
         testRelativizeAndResolve("/a/c", "/a/b", "b");
         testRelativizeAndResolve("/a/b/c", "/a/b/", "./");
         testRelativizeAndResolve("/a/b/c/", "/a/b/d", "../d");
         testRelativizeAndResolve("/a/b/c/", "/a/b/d/", "../d/");
         testRelativizeAndResolve("/a/b/c", "/a/b/d", "d");
         testRelativizeAndResolve("/a/b/c/", "/a/b/d/", "../d/");
         testRelativizeAndResolve("/a/b/c/", "/x/y/z/", "../../../x/y/z/");
         testRelativizeAndResolve("/a/b/c/", "/x/y/z", "../../../x/y/z");
         testRelativizeAndResolve("/a/b/c", "/x/y/z", "../../x/y/z");
         testRelativizeAndResolve("a/b/c", "x/y/z", "../../x/y/z");
     }
 
     protected void testRelativizeAndResolve(
         String first,
         String second,
         String control) {
         Path a = new Path(first);
         Path b = new Path(second);
         assertEquals(b, a.getResolved(control));
         Path relativePath = a.getRelative(b);
         assertNotNull(relativePath);
         assertEquals(control, relativePath.toString());
         Path test = a.getResolved(relativePath);
         assertNotNull(test);
         assertEquals(b, test);
     }
 
     public void testRemovePath() {
         testRemovePath("a/b", 1, "a");
 
         testRemovePath("/", 1, "/");
         testRemovePath("a/b", 1, "a");
         testRemovePath("/a/b", 1, "/a");
         testRemovePath("/a/b", 2, "/");
         testRemovePath("/a/b/", 2, "/");
         testRemovePath("a/b/", 2, "/");
     }
 
     protected void testRemovePath(String path, int count, String control) {
         Path c = new Path(control);
         Path.Builder p = new Path.Builder(path);
         p.removeLastPathSegments(count);
         Path test = p.build();
         assertEquals(c, test);
     }
 
     /**
      * @throws Exception
      */
     public void testResolve() {
         testResolve("/a/b", "../..", "");
 
         testResolve("/a/b", ".././", "/");
 
         testResolve("/", ".", "");
         testResolve("/a/b/c", ".", "/a/b/");
         testResolve("/a/b/c", "./", "/a/b/");
 
         testResolve("/a/b/c", "././.", "/a/b/");
         testResolve("/a/b/c", "d/././.", "/a/b/d/");
         testResolve("/a/b/c", "d", "/a/b/d");
         testResolve("/a/b/c/", "d", "/a/b/c/d");
         testResolve("/a/b/c/", "../d", "/a/b/d");
         testResolve("/a/b/c/", "../../d", "/a/d");
         testResolve("/a/b/c/", "../../d/../f", "/a/f");
         testResolve("/a/b/c/", "././d/./e", "/a/b/c/d/e");
         testResolve("/a/b/c/", "././d/./e/./.", "/a/b/c/d/e/");
         testResolve("/a/b/c/", "./././d/../e", "/a/b/c/e");
         testResolve("/a/b/c/", "../../../d/../e", "/e");
         testResolve("/a/b/c", "./e/", "/a/b/e/");
         testResolve("/a/b/c", "/e/", "/e/");
         testResolve("/a/b/c", "d/e/f", "/a/b/d/e/f");
         testResolve("/a/b/c", "d////e/./././f", "/a/b/d/e/f");
         testResolve("/a/b/c/", "d////e/./././f", "/a/b/c/d/e/f");
         testResolve("/", "e/", "/e/");
         testResolve("", "e", "e");
         testResolve("", "e/", "e/");
         testResolve("a/b", "../.", "");
         testResolve("a/b", ".././", "/");
         testResolve("/a/b", ".././", "/");
         testResolve("/a/b", "../.", "");
         testResolve("/a/b", "../..", "");
         testResolve("a/b", "../.", "");
         testResolve("a/b", "../..", "");
     }
 
     protected void testResolve(String first, String second, String control) {
         Path.Builder a = new Path.Builder(first);
         Path.Builder b = new Path.Builder(second);
         Path.Builder test = a.getResolved(b);
         assertNotNull(test);
         assertEquals(control, test.toString());
     }
 
     public void testResolveItself() {
         testResolveItself("..", "");
         testResolveItself("../", "/");
         testResolveItself("a/././b/././c", "a/b/c");
         testResolveItself("a/../b/../c/../d", "d");
         testResolveItself("a/b/../c", "a/c");
         testResolveItself("a/b/../../c", "c");
         testResolveItself("a/b/../../../../c", "c");
     }
 
     private void testResolveItself(String path, String control) {
         Path.Builder p = new Path.Builder(path);
         Path.Builder c = new Path.Builder(control);
         Path.Builder test = p.getResolved();
         assertEquals(c, test);
     }
 
     public void testStartsWith() {
         testStartsWith("", "", "");
         testStartsWith("/", "", "/");
         testStartsWith("/a", "/a", "/");
         testStartsWith("/a/", "/a", "/");
         testStartsWith("/a/b/c", "/a", "/b/c");
         testStartsWith("/a/b/c", "/a/b", "/c");
     }
 
     private void testStartsWith(String first, String second, String result) {
         Path.Builder a = new Path.Builder(first);
         Path.Builder b = new Path.Builder(second);
         Path.Builder r = new Path.Builder(result);
         assertTrue(a.startsWith(b));
         Path.Builder test = a.removeFirstPathSegments(b.getPathSegmentCount());
         assertEquals(r, test);
     }
 
 }
