 package org.denis.webview.util.io;
 
 import org.junit.Test;
 
 /**
  * @author Denis Zhdanov
  * @since 06/27/2010
  */
 public class UrlDecodingReaderTest extends AbstractReplacingFilterReaderTest<UrlDecodingReader> {
 
     public UrlDecodingReaderTest() {
         super(UrlDecodingReader.class);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void invalidHexSymbols() throws Exception {
         getReader("%AG").read(new char[5]);
     }
 
     @Test(expected = IllegalStateException.class)
     public void incompleteHexData() throws Exception {
         UrlDecodingReader reader = getReader("%A");
         reader.read(new char[5]);
 
         // We perform the reading second time in order to get EOF indication.
         reader.read(new char[5]);
     }
 
     @Test
     public void noReplacements() throws Exception {
         doTest("test", "test");
     }
 
     @Test
     public void spaceReplacement() throws Exception {
         doTest("this+is+a+test", "this is a test");
     }
 
     @Test
     public void hexReplacement() throws Exception {
         doTest("%26%231090%3B%26%231077%3B%26%231089%3B%26%231090%3B", "&#1090;&#1077;&#1089;&#1090;");
     }
 
     @Test
     public void mixedText() throws Exception {
         doTest(
             "this+is+a+%26%231090%3B%26%231077%3B%26%231089%3B%26%231090%3B",
             "this is a &#1090;&#1077;&#1089;&#1090;"
         );
     }
 
     @Test
     public void nonAsciiSymbols() throws Exception {
         doTest(
             "ide=%D1%80%D1%83%D1%81%D1%81%D0%BA%D0%BE%D0%B5+%D1%81%D0%BB%D0%BE%D0%B2%D0%BE&language=java",
             "ide=русское слово&language=java"
         );
     }

    @Test
    public void cyclingNotParsedData() throws Exception {
        int bufferSize = AbstractReplacingFilterReader.DEFAULT_INTERNAL_BUFFER_SIZE;
        StringBuilder input = new StringBuilder();
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < bufferSize - 2; ++i) {
            input.append('x');
            expected.append('x');
        }
        input.append("%3D123");
        expected.append("=123");
        doTest(input.toString(), expected.toString());
    }
 }
