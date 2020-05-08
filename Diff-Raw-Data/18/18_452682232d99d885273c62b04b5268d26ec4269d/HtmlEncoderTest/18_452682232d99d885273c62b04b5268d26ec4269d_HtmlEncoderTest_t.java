 package com.github.t1.webresource;
 
 import static java.util.Arrays.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.io.*;
 import java.net.URI;
 import java.util.*;
 
 import lombok.*;
 
 import org.junit.*;
 
 import com.google.common.collect.*;
 
 public class HtmlEncoderTest {
    private static final String BASE_URI = "http://localhost:8080/demo/resource";
     private final Writer out = new StringWriter();
 
     private HtmlEncoder writer(Object object) {
         return new HtmlEncoder(object, out, URI.create(BASE_URI));
     }
 
     private static String wrapped(String string) {
         return "<html><head></head><body>" + string + "</body></html>";
     }
 
     private String result() {
         return out.toString().replaceAll("\n", "").replace('\"', '\'');
     }
 
     @Test
     public void shouldEncodeNullObject() throws Exception {
         writer(null).write();
 
         assertEquals(wrapped(""), result());
     }
 
     @Test
     public void shouldEncodePrimitiveString() throws Exception {
         writer("dummy").write();
 
         assertEquals(wrapped("dummy"), result());
     }
 
     @Test
     public void shouldEscapeString() throws Exception {
         writer("string & ampersand").write();
 
         assertEquals(wrapped("string &amp; ampersand"), result());
     }
 
     @Test
     public void shouldEncodePrimitiveInteger() throws Exception {
         writer(1234).write();
 
         assertEquals(wrapped("1234"), result());
     }
 
     @Test
     public void shouldEncodeList() throws Exception {
         writer(asList("one", "two", "three")).write();
 
         assertEquals(wrapped("<ul><li>one</li><li>two</li><li>three</li></ul>"), result());
     }
 
     @Test
     public void shouldEncodeEmptyMap() throws Exception {
         Map<String, String> map = new LinkedHashMap<>();
 
         writer(map).write();
 
         assertEquals(wrapped(""), result());
     }
 
     @Test
     public void shouldEncodeOneKeyMap() throws Exception {
         Map<String, String> map = new LinkedHashMap<>();
         map.put("one", "111");
 
         writer(map).write();
 
         assertEquals(wrapped("111"), result());
     }
 
     @Test
     public void shouldEncodeMap() throws Exception {
         Map<String, String> map = new LinkedHashMap<>();
         map.put("one", "111");
         map.put("two", "222");
         map.put("three", "333");
 
         writer(map).write();
 
         assertEquals(wrapped(div("one-0", "one", "111") + div("two-0", "two", "222") + div("three-0", "three", "333")),
                 result());
     }
 
     private String div(String id, String name, String value) {
         return div(id, name, value, "text");
     }
 
     private String div(String id, String name, String value, String type) {
         return "<div>" //
                 + label(id, name) //
                 + "<input id='" + id + "' type='" + type + "' value='" + value + "' readonly/>" //
                 + "</div>";
     }
 
     private String label(String id, String name) {
         return "<label for='" + id + "'>" + name + "</label>";
     }
 
     @Test
     public void shouldEncodeListOfOneElementMapsAsList() throws Exception {
         Map<String, String> map0 = new LinkedHashMap<>();
         map0.put("one", "111");
 
         Map<String, String> map1 = new LinkedHashMap<>();
         map1.put("one", "aaa");
 
         writer(Arrays.asList(map0, map1)).write();
 
         assertEquals(wrapped("<ul><li>111</li><li>aaa</li></ul>"), result());
     }
 
     @Test
     public void shouldEncodeListOfMapsAsTable() throws Exception {
         Map<String, String> map0 = new LinkedHashMap<>();
         map0.put("one", "111");
         map0.put("two", "222");
         map0.put("three", "333");
 
         Map<String, String> map1 = new LinkedHashMap<>();
         map1.put("one", "aaa");
         map1.put("two", "bbb");
         map1.put("three", "ccc");
 
         writer(Arrays.asList(map0, map1)).write();
 
         assertEquals(wrapped("<table>" //
                 + "<thead>" //
                 + "<tr><th>one</th><th>two</th><th>three</th></tr>" //
                 + "</thead><tbody>" //
                 + "<tr><td>111</td><td>222</td><td>333</td></tr>" //
                 + "<tr><td>aaa</td><td>bbb</td><td>ccc</td></tr>" //
                 + "</tbody>" //
                 + "</table>"), result());
     }
 
     @Data
     @AllArgsConstructor
     private static class OneStringPojo {
         private String string;
     }
 
     @Test
     public void shouldWriteOneStringPojoWithoutKey() throws Exception {
         OneStringPojo pojo = new OneStringPojo("str");
 
         writer(pojo).write();
 
         assertEquals(wrapped("str"), result());
     }
 
     @Test
     public void shouldWriteOneStringPojoNullValue() throws Exception {
         OneStringPojo pojo = new OneStringPojo(null);
 
         writer(pojo).write();
 
         assertEquals(wrapped(""), result());
     }
 
     @Test
     public void shouldWriteOneStringPojoListAsUnorderedList() throws Exception {
         OneStringPojo pojo1 = new OneStringPojo("one");
         OneStringPojo pojo2 = new OneStringPojo("two");
         OneStringPojo pojo3 = new OneStringPojo("three");
         List<OneStringPojo> list = asList(pojo1, pojo2, pojo3);
 
         writer(list).write();
 
         assertEquals(wrapped("<ul><li>one</li><li>two</li><li>three</li></ul>"), result());
     }
 
     @AllArgsConstructor
     @SuppressWarnings("unused")
     private static class TwoFieldPojo {
         public String str;
         public Integer i;
     }
 
     @Test
     public void shouldWriteTwoFieldPojoAsSequenceOfDivsWithLabelsAndReadonlyInputs() throws Exception {
         TwoFieldPojo pojo = new TwoFieldPojo("dummy", 123);
 
         writer(pojo).write();
 
         assertThat(result(), containsString(div("i-0", "i", "123")));
         assertThat(result(), containsString(div("str-0", "str", "dummy")));
     }
 
     @Test
     public void shouldWriteTwoFieldPojoListAsTables() throws Exception {
         TwoFieldPojo pojo1 = new TwoFieldPojo("one", 111);
         TwoFieldPojo pojo2 = new TwoFieldPojo("two", 222);
         List<TwoFieldPojo> list = Arrays.asList(pojo1, pojo2);
 
         writer(list).write();
 
         assertEquals(wrapped("<table>" //
                 + "<thead>" //
                 + "<tr><th>str</th><th>i</th></tr>" //
                 + "</thead><tbody>" //
                 + "<tr><td>one</td><td>111</td></tr>" //
                 + "<tr><td>two</td><td>222</td></tr>" //
                 + "</tbody>" //
                 + "</table>"), result());
     }
 
     @Data
     @AllArgsConstructor
     private static class TwoFieldsOneBooleanPojo {
         private boolean b;
         private String str;
     }
 
     @Test
     public void shouldWriteOneBooleanPojoWithoutKey() throws Exception {
         TwoFieldsOneBooleanPojo pojo = new TwoFieldsOneBooleanPojo(true, "dummy");
 
         writer(pojo).write();
 
         assertThat(result(), containsString(div("b-0", "b", "true", "checkbox")));
         assertThat(result(), containsString(div("str-0", "str", "dummy")));
     }
 
     @AllArgsConstructor
     private static class PojoWithOneHtmlHead {
         @HtmlHead
         public String str;
         public Integer i;
     }
 
     @Test
     public void shouldWritePojoWithOneHtmlHead() throws Exception {
         PojoWithOneHtmlHead pojo = new PojoWithOneHtmlHead("dummy", 123);
         assertEquals(123, (int) pojo.i); // cover
 
         writer(pojo).write();
 
         assertThat(result(), containsString("<head><title>dummy</title></head>"));
     }
 
     @AllArgsConstructor
     private static class PojoWithTwoHtmlHeads {
         @HtmlHead
         public String str0;
         @HtmlHead
         public String str1;
     }
 
     @Test
     public void shouldWritePojoWithTwoHtmlHead() throws Exception {
         PojoWithTwoHtmlHeads pojo = new PojoWithTwoHtmlHeads("dummy0", "dummy1");
 
         writer(pojo).write();
 
         assertThat(result(), containsString("<head><title>dummy0 - dummy1</title></head>"));
     }
 
     @Data
     @AllArgsConstructor
     @HtmlStyleSheet("/root-path")
     private static class PojoWithRootPathCss {
         private String str;
     }
 
     @Test
     public void shouldLinkRootPathCssStyleSheet() throws Exception {
         PojoWithRootPathCss pojo = new PojoWithRootPathCss("dummy");
 
         writer(pojo).write();
 
         assertEquals("<html><head>" //
                 + "<link rel='stylesheet' href='/root-path' type='text/css'/>" //
                 + "</head><body>dummy</body></html>", result());
     }
 
     @Test
     public void shouldWritePojoListWithRootPathCssStyleSheet() throws Exception {
         PojoWithRootPathCss pojo1 = new PojoWithRootPathCss("a");
         PojoWithRootPathCss pojo2 = new PojoWithRootPathCss("b");
         List<PojoWithRootPathCss> list = Arrays.asList(pojo1, pojo2);
 
         writer(list).write();
 
         assertEquals("<html><head>" //
                 + "<link rel='stylesheet' href='/root-path' type='text/css'/>" //
                 + "</head><body><ul><li>a</li><li>b</li></ul></body></html>", result());
     }
 
     @Data
     @AllArgsConstructor
     @HtmlStyleSheet(value = "file:src/test/resources/testfile.txt", inline = true)
     private static class PojoWithInlineFileCss {
         private String str;
     }
 
     @Test
     public void shouldInlineFileCssStyleSheet() throws Exception {
         PojoWithInlineFileCss pojo = new PojoWithInlineFileCss("dummy");
 
         writer(pojo).write();
 
         assertEquals("<html><head>" //
                 + "<style>" //
                 + "test-file-contents" //
                 + "</style>" //
                 + "</head><body>dummy</body></html>", result());
     }
 
     @Data
     @AllArgsConstructor
     @HtmlStyleSheet(value = "/stylesheets/main.css", inline = true)
     private static class PojoWithInlineRootResourceCss {
         private String str;
     }
 
     @Test
    @Ignore("needs a running service and I have no idea how to map that to a file-url")
     public void shouldInlineRootResourceCssStyleSheet() throws Exception {
         PojoWithInlineRootResourceCss pojo = new PojoWithInlineRootResourceCss("dummy");
 
         writer(pojo).write();
 
         assertThat(result(), containsString("<head><style>test-file-contents</style></head>"));
     }
 
     @Data
     @AllArgsConstructor
     @HtmlStyleSheet(value = "stylesheets/main.css", inline = true)
     private static class PojoWithInlineLocalResourceCss {
         private String str;
     }
 
     @Test
    @Ignore("needs a running service and I have no idea how to map that to a file-url")
     public void shouldInlineLocalResourceCssStyleSheet() throws Exception {
         PojoWithInlineLocalResourceCss pojo = new PojoWithInlineLocalResourceCss("dummy");
 
         writer(pojo).write();
 
         assertThat(result(), containsString("<head><style>test-file-contents</style></head>"));
     }
 
     @Data
     @AllArgsConstructor
     @HtmlStyleSheet("local-path")
     private static class PojoWithLocalCss {
         private String str;
     }
 
     @Test
     public void shouldLinkLocalCssStyleSheet() throws Exception {
         PojoWithLocalCss pojo = new PojoWithLocalCss("dummy");
 
         writer(pojo).write();
 
         assertThat(result(), containsString("<head>" //
                 + "<link rel='stylesheet' href='/demo/local-path' type='text/css'/>" //
                 + "</head>"));
     }
 
     @Data
     @AllArgsConstructor
     @HtmlStyleSheets({ @HtmlStyleSheet("/root-path"), @HtmlStyleSheet("local-path") })
     private static class PojoWithTwoCss {
         private String str;
     }
 
     @Test
     public void shouldLinkTwoCssStyleSheets() throws Exception {
         PojoWithTwoCss pojo = new PojoWithTwoCss("dummy");
 
         writer(pojo).write();
 
         assertThat(result(), containsString("<head>" //
                 + "<link rel='stylesheet' href='/root-path' type='text/css'/>" //
                 + "<link rel='stylesheet' href='/demo/local-path' type='text/css'/>" //
                 + "</head>"));
     }
 
     @Data
     @AllArgsConstructor
     private static class NestedSetPojo {
         private String str;
         private Set<String> set;
     }
 
     @Test
     public void shouldWriteNestedSetPojo() throws Exception {
         NestedSetPojo pojo = new NestedSetPojo("dummy", ImmutableSet.of("one", "two", "three"));
 
         writer(pojo).write();
 
         assertThat(result(), containsString(div("str-0", "str", "dummy")));
         assertThat(result(), containsString(div("set-0", "set", "[one, two, three]")));
     }
 
     @Data
     @AllArgsConstructor
     private static class NestedListPojo {
         private String str;
         private List<String> list;
     }
 
     @Test
     public void shouldWriteNestedListPojo() throws Exception {
         NestedListPojo pojo = new NestedListPojo("dummy", ImmutableList.of("one", "two", "three"));
 
         writer(pojo).write();
 
         assertThat(result(), containsString(div("str-0", "str", "dummy")));
         assertThat(result(), containsString("<div>" //
                 + label("list-0", "list") //
                 + "<ul>" //
                 + "<li>one</li>" //
                 + "<li>two</li>" //
                 + "<li>three</li>" //
                 + "</ul>" //
                 + "</div>"));
     }
 }
