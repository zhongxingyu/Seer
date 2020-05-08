 package net.sf.beezle.ssass;
 
 import com.yahoo.platform.yui.compressor.CssCompressor;
 import net.sf.beezle.mork.misc.GenericException;
 import net.sf.beezle.ssass.scss.Output;
 import net.sf.beezle.ssass.scss.Stylesheet;
 import net.sf.beezle.sushi.fs.World;
 import net.sf.beezle.sushi.fs.file.FileNode;
 import net.sf.beezle.sushi.io.OS;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 public class SsassTest {
     private static final Main main = new Main();
     private static final World world = main.getConsole().world;
 
     @Test
     public void empty() throws IOException {
         css2();
     }
 
     @Test
     public void charset() throws IOException {
         css2("@charset \"ISO-8859-1\";");
     }
 
     @Test
     public void imports() throws IOException {
         css2(
                 "@import url('/css/typography.css');",
                 "@import url('/css/layout.css') print;",
                 "@import url('/css/color.css') foo, bar;");
     }
 
     @Test
     public void comment() throws IOException {
         cssCmp(new String[]{
                 "@import url('/css/typography.css');",
                 "@import url('/css/color.css') foo, bar;"},
                 new String[]{
                         "@import url('/css/typography.css');",
                        "/* hi */",
                         "@import url('/css/color.css') foo, bar;"}, true);
     }
 
     @Test
     public void ruleset() throws IOException {
         css2("p {",
                 "  margin-top: abc;",
                 "  margin-right: xyz !important",
                 "}");
     }
 
     @Test
     public void media() throws IOException {
         css2(
                 "@media print {",
                 "  p {",
                 "    font-family: Arial,Helvetica,sans-serif;",
                 "    font-size: 11px;",
                 "    font-weight: bold",
                 "  }",
                 "}",
                 "@media screen {",
                 "  p {",
                 "    font-family: Arial,Helvetica,sans-serif;",
                 "    font-size: 11px;",
                 "    font-weight: bold",
                 "  }",
                 "  p.print {",
                 "    display: none",
                 "  }",
                 "}");
     }
 
     @Test
     public void mediaQuery() throws IOException {
         css3("@media (min-width: -100px) {",
              "}");
         css3("@media handheld and (min-width: 20em), screen and (min-width: 20em) {",
              "}");
         css3("@media not handheld {",
                 "}");
         css3("@media only handheld {",
              "}");
     }
 
     @Test
     public void namespace() throws IOException {
         css3("@namespace toto \"http://toto.example.org\";",
                 "@namespace url(\"http://example.com/foo\");");
     }
 
     @Test
     public void combinators() throws IOException {
         css3("a b {",
                 "  one: lime",
                 "}");
         css3("a+b {",
                 "  one: lime",
                 "}");
         css3("a>b {",
                 "  one: lime",
                 "}");
         css3("a~b {",
                 "  one: lime",
                 "}");
     }
 
     @Test
     public void color() throws IOException {
         css3("em {",
              "  one: lime;",
              "  two: rgb(0,255,0);",
              "  three: hsl(0,100%,50%);",
              "  four: hsla(240,100%,50%,0.5)",
              "}");
     }
 
     @Test
     public void page() throws IOException {
         css2(
                 "@page {",
                 "  size: 21.0cm 14.85cm;",
                 "  margin-top: 1.5cm;",
                 "  margin-bottom: 2cm",
                 "}",
                 "@page :left {",
                 "  margin-left: 1.5cm;",
                 "  margin-right: 2cm",
                 "}",
                 "@page :right {",
                 "  margin-left: 2cm;",
                 "  margin-right: 1.5cm",
                 "}");
     }
 
     @Test
     public void pseudo() throws IOException {
         css2(
                 ":foo {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "::foo {",
                 "  background: #C1B49A",
                 "}");
     }
 
     @Test
     public void pseudoFunctionSelector() throws IOException {
         css2(
                 "tr:nth-child(odd) td {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "a:b(1+2) td {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "a:b(+2) td {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "a:b(2dimension) td {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "a:b(-\"FOO\") td {",
                 "  background: #C1B49A",
                 "}");
     }
 
     @Test
     public void function() throws IOException {
         css2(
                 "p:before {",
                 "  counter-increment: paras 1;",
                 "  content: \"New Paragraph:\" counter(paras,decimal) \":\"",
                 "}");
     }
 
     @Test
     public void typeSelector() throws IOException {
         css2(
                 "a {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "|b {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "foo|b {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "*|c {",
                 "  background: #C1B49A",
                 "}");
     }
 
     @Test
     public void universal() throws IOException {
         css2(
                 "* {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "|* {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "a|* {",
                 "  background: #C1B49A",
                 "}");
         css2(
                 "*|* {",
                 "  background: #C1B49A",
                 "}");
     }
 
     @Test
     public void term1() throws IOException {
         css2("term1 {",
                 "  number: 42;",
                 "  percentage: 2%;",
                 "  length-px: 3px;",
                 "  length-cm: 4cm;",
                 "  length-mm: 5mm;",
                 "  length-in: 6in;",
                 "  length-pt: 7pt;",
                 "  length-pc: 8pc;",
                 "  a: -3;",
                 "  b: +4%;",
                 "  c: -100cm;",
                 "  d: 12em;",
                 "  e: 13ex;",
                 "  f: 20deg;",
                 "  g: 21rad;",
                 "  h: 22grad;",
                 "  i: 0s;",
                 "  j: 1ms;",
                 "  k: 2hz;",
                 "  l: 3khz",
                 "}");
     }
 
     @Test
     public void term2() throws IOException {
         css2("term2 {",
                 "  string1: \"str\";",
                 "  string2: 'str';",
                 "  ident: ident;",
                 "  url: url(foo);",
                 "  resolution: 12dpi;",
                 "  resolution2: 12dpcm;",
                 "  hexcolor: #name;",
                 "  function: foo(bar)",
                 "}");
     }
 
     @Test
     public void attrib() throws IOException {
         css2(
                 "foo[bar=\"x\"] {",
                 "  background-color: yellow",
                 "}",
                 "a|foo[b|bar=\"x\"] {",
                 "  background-color: yellow",
                 "}",
                 "a[title~=Web] {",
                 "  background-color: yellow",
                 "}",
                 "[lang|=fr] {",
                 "  background-color: grey",
                 "}");
     }
 
     @Test
     public void negation() throws IOException {
         css3(
                 "not([bar=\"x\"]) {",
                 "  background-color: yellow",
                 "}",
                 "not(a) {",
                 "  background-color: yellow",
                 "}");
     }
 
     @Test
     public void complex() throws IOException, GenericException {
         FileNode src;
         Stylesheet s;
 
         src = world.guessProjectHome(getClass()).join("src/test/complex.css");
         s = main.parse(src.getAbsolute());
         assertEquals(src.readString(), Output.prettyprint(s));
         assertEquals(compress(src.readString()), Output.compress(s));
     }
 
     //-- compression issues
 
     @Ignore // TODO
     public void weiredFunctionCompression() throws IOException {
         css2(
                 "p:before {",
                 "  counter-increment: paras 1;",
                 "  content: \"New Paragraph: \" counter(paras,decimal) \": \"",
                 "}");
     }
 
     @Ignore // TODO
     public void hexColorCompress() throws IOException {
         css2(
                 "foo[bar=\"x\"] {",
                 "  background-color: #ffffff",
                 "}");
     }
 
     //-- ssass
 
     @Test
     public void variable() throws IOException {
         sass(
                 "$blue: \"abc\";\n" +
                 ".content-navigation {\n" +
                 "  border-color: $blue;\n" +
                 "}\n",
                 ".content-navigation {\n" +
                 "  border-color: \"abc\"\n" +
                 "}\n"
                 );
     }
 
     @Test
     public void nestedRuleset() throws IOException {
         sass(  "table.hl {\n" +
                 "  margin: 2em 0;\n" +
                 "  td.ln {\n" +
                 "    text-align: right;\n" +
                 "  }\n" +
                 "}",
 
                 "table.hl {\n" +
                 "  margin: 2em 0\n" +
                 "}\n" +
                 "table.hl td.ln {\n" +
                 "  text-align: right\n" +
                 "}\n");
     }
 
     @Test
     public void nestedRulesetTwice() throws IOException {
         sass(  "top {\n" +
                 "  a: b;\n" +
                 "  mid {\n" +
                 "    foo: bar;\n" +
                 "    bottom {\n" +
                 "      x: y;\n" +
                 "    }\n" +
                 "  }\n" +
                 "}",
                 "top {\n" +
                 "  a: b\n" +
                 "}\n" +
                 "top mid {\n" +
                 "  foo: bar\n" +
                 "}\n" +
                 "top mid bottom {\n" +
                 "  x: y\n" +
                 "}\n");
     }
 
     @Test
     public void nestedProperties() throws IOException {
         sass(   "li {\n" +
                 "  font: {\n" +
                 "    family: serif;\n" +
                 "    weight: bold;\n" +
                 "    size: 1.2em;\n" +
                 "  }\n" +
                 "}",
                 "li {\n" +
                 "  font-family: serif;\n" +
                 "  font-weight: bold;\n" +
                 "  font-size: 1.2em\n" +
                 "}\n");
     }
 
     @Test
     public void nestedPropertiesTwice() throws IOException {
         sass(   "top {\n" +
                 "  a: 1;\n" +
                 "  mid: {\n" +
                 "    b: 2;\n" +
                 "    bottom: {\n" +
                 "      c: 3;\n" +
                 "    }\n" +
                 "  }\n" +
                 "}",
                 "top {\n" +
                 "  a: 1;\n" +
                 "  mid-b: 2;\n" +
                 "  mid-bottom-c: 3\n" +
                 "}\n");
     }
 
     @Test
     public void mixinProperties() throws IOException {
         sass   ("@mixin left {\n" +
                 "  float: left;\n" +
                 "}\n" +
                 "\n" +
                 "#data {\n" +
                 "  @include left;\n" +
                 "}",
                 "#data {\n" +
                 "  float: left\n" +
                 "}\n");
     }
 
     @Test
     public void mixinRuleset() throws IOException {
         sass   ("@mixin table-base {\n" +
                 "  th {\n" +
                 "    text-align: center;\n" +
                 "    font-weight: bold;\n" +
                 "  };\n" +
                 "  td, th {padding: 2px}\n" +
                 "}\n" +
                 "#data {\n" +
                 "  @include table-base;\n" +
                 "}",
 
                 "#data {\n" +
                 "\n" +
                 "}\n" +
                 "#data th {\n" +
                 "  text-align: center;\n" +
                 "  font-weight: bold\n" +
                 "}\n" +
                 "#data td, #data th {\n" +
                 "  padding: 2px\n" +
                 "}\n");
     }
 
     @Test
     public void mixinArgument() throws IOException {
        sass   ("@mixin left ($dist) {\n" +
                 "  float: left;\n" +
                 "  margin-left: $dist;\n" +
                 "}\n" +
                 "#data {\n" +
                 "  @include left(10px);\n" +
                 "}\n",
                 "#data {\n" +
                 "  float: left;\n" +
                 "  margin-left: 10px\n" +
                 "}\n");
     }
 
     @Test
     public void mixinArgumentLists() throws IOException {
        sass   ("@mixin point ($x, $y) {\n" +
                 "  x: $x;\n" +
                 "  y: $y;\n" +
                 "}\n" +
                 "#a {\n" +
                 "  @include point(1, 2);\n" +
                 "}\n" +
                 "#b {\n" +
                 "  @include point(10, 11);\n" +
                 "}\n",
                 "#a {\n" +
                 "  x: 1;\n" +
                 "  y: 2\n" +
                 "}\n" +
                 "#b {\n" +
                 "  x: 10;\n" +
                 "  y: 11\n" +
                 "}\n");
     }
 
     //--
 
     private void css2(String... lines) throws IOException {
         cssCmp(lines, lines, true);
     }
 
     private void css3(String... lines) throws IOException {
         cssCmp(lines, lines, false);
     }
 
     private void cssCmp(String[] expectedLines, String[] actual, boolean level2) throws IOException {
         String expected;
         FileNode tmp;
         Stylesheet s;
 
         expected = OS.CURRENT.lineSeparator.join(expectedLines);
         tmp = (FileNode) world.getTemp().createTempFile().writeLines(actual);
         s = main.parse(tmp.getAbsolute());
         assertTrue(s != null);
         tmp.delete();
         try {
             assertEquals(expected, Output.prettyprint(s).trim());
             if (level2) {
                 assertEquals(compress(expected), Output.compress(s));
             }
         } catch (GenericException e) {
             fail(e.getMessage());
         }
     }
 
     private String compress(String css) throws IOException {
         StringReader src;
         StringWriter dest;
 
         src = new StringReader(css);
         dest = new StringWriter();
         new CssCompressor(src).compress(dest, 30000);
         return dest.toString();
     }
 
     private void sass(String sass, String css) throws IOException {
         FileNode tmp;
         Stylesheet s;
 
         tmp = (FileNode) world.getTemp().createTempFile().writeString(sass);
         s = main.parse(tmp.getAbsolute());
         if (s == null) {
             throw new IOException("syntax error");
         }
         tmp.delete();
         try {
             assertEquals(css, Output.prettyprint(s));
         } catch (GenericException e) {
             fail(e.getMessage());
         }
     }
 }
