 package com.spartansoftwareinc.otter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import static com.spartansoftwareinc.otter.TMXEventType.*;
 import static com.spartansoftwareinc.otter.TestUtil.*;
 
 import org.junit.*;
 
 import static org.junit.Assert.*;
 
 public class TestTMXEventWriter {
 
     @Test
     public void testSimple() throws Exception {
         File tmp = File.createTempFile("otter", ".tmx");
         Writer w = new OutputStreamWriter(new FileOutputStream(tmp), "UTF-8");
         TMXWriter writer = TMXWriter.createTMXEventWriter(w);
         writer.startTMX();
         Header header = getHeader();
         writer.writeHeader(header);
         writer.startBody();
         writer.endBody();
         writer.endTMX();
         w.close();
         Reader r = new InputStreamReader(new FileInputStream(tmp), "UTF-8");
         TMXReader reader = TMXReader.createTMXEventReader(r);
         List<TMXEvent> events = readEvents(reader);
         checkEvent(events.get(0), TMXEventType.START_TMX);
         checkEvent(events.get(1), TMXEventType.HEADER);
         Header rHeader = events.get(1).getHeader();
         assertNotNull(rHeader);
         assertEquals(header, rHeader);
         checkProperty(header.getProperties().get(0), "type1", "Property");
         checkNote(header.getNotes().get(0), "This is a note");
         checkEvent(events.get(2), START_BODY);
         checkEvent(events.get(3), END_BODY);
         checkEvent(events.get(4), END_TMX);
         tmp.delete();
     }
     
     void testRoundtripTUs(List<TU> tus) throws Exception {
         File tmp = File.createTempFile("otter", ".tmx");
         Writer w = new OutputStreamWriter(new FileOutputStream(tmp), "UTF-8");
         TMXWriter writer = TMXWriter.createTMXEventWriter(w);
         writer.startTMX();
         Header header = getHeader();
         writer.writeHeader(header);
         writer.startBody();
         for (TU tu : tus) {
             writer.writeTu(tu);
         }
         writer.endBody();
         writer.endTMX();
         w.close();
         Reader r = new InputStreamReader(new FileInputStream(tmp), "UTF-8");
         TMXReader reader = TMXReader.createTMXEventReader(r);
         List<TU> roundtripTUs = readTUs(reader);
         assertEquals(tus.size(), roundtripTUs.size());
         assertEquals(tus, roundtripTUs);
         tmp.delete();
     }
     
     @Test
     public void testTu() throws Exception {
         TU tu = new TU("en-US");
         tu.tuvBuilder("en-US")
             .text("Hello ")
             .bpt(1, "<b>")
             .text("paired")
             .ept(1, "</b>")
             .text(" world")
             .build();
         testRoundtripTUs(Collections.singletonList(tu));
     }
     
     @Test
     public void testTuWithSubflow() throws Exception {
         TU tu = new TU("en-US");
         TUVBuilder b = tu.tuvBuilder("en-US");
         b
             .text("Tag containing ")
             .bpt(1, new ComplexContent().addCodes("<a href='#' title='") 
                                         .addSubflow(b.nested().text("Subflow text")) 
                                         .addCodes("'>"))
             .text("a subflow")
             .ept(1, "</a>")
             .text(".")
             .build();
         testRoundtripTUs(Collections.singletonList(tu));
     }
     
     // Same as previous test, except we call build() on the 
     // nested builder before passing it
     @Test
     public void testTuWithSubflowBuilt() throws Exception {
         TU tu = new TU("en-US");
         TUVBuilder b = tu.tuvBuilder("en-US");
         b.text("Tag containing ")
          .bpt(1, new ComplexContent().addCodes("<a href='#' title='") 
                                      .addSubflow(b.nested().text("Subflow text").build()) 
                                      .addCodes("'>")) // pass mixed content
          .text("a subflow")
          .ept(1, "</a>")
          .text(".")
          .build();
         testRoundtripTUs(Collections.singletonList(tu));
     }
 
     @Test
     public void testSimpleTuHighlight() throws Exception {
         TU tu = new TU("en-US");
         tu.tuvBuilder("en-US")
                 .text("Content containing ")
                 .hi("highlighted")
                 .text(" text").build();
         testRoundtripTUs(Collections.singletonList(tu));
     }
     
     @Test
     public void testTuHighlighWithTags() throws Exception {
         TU tu = new TU("en-US");
         TUVBuilder b = tu.tuvBuilder("en-US");
         b.text("Content containing ")
             .hi(b.nested().text("highlighted text including ")
                     .bpt(1, "<b>")
                     .text("tag content")
                     .ept(1, "</b>"))
             .build();
         testRoundtripTUs(Collections.singletonList(tu));
     }
     
     // Same as previous test, except we call build() on the 
     // nested builder before passing it
     @Test
     public void testTuWithHighlightBuilt() throws Exception {
         TU tu = new TU("en-US");
         TUVBuilder b = tu.tuvBuilder("en-US");
         b.text("Content containing ")
             .hi(b.nested().text("highlighted text including ")
                     .bpt(1, "<b>")
                     .text("tag content")
                     .ept(1, "</b>").build())
             .build();
         testRoundtripTUs(Collections.singletonList(tu));
     }
     
     @Test
     public void testDeepHiNesting() throws Exception {
         // <seg>A<hi x="1">B<hi x="2">C</hi>D</hi>.</seg>
         TU tu = new TU("en-US");
         TUVBuilder b = tu.tuvBuilder("en-US");
         b.text("A");
         TUVBuilder nested = b.nested(); 
         nested.text("B")
               .hi(nested.nested().text("C"))
               .text("D");
         b.hi(nested).text(".");
         tu.addTUV(b.build());
         testRoundtripTUs(Collections.singletonList(tu));
     }
     
     
     @Test
     public void testPhAttributes() throws Exception {
         TU tu = new TU("en-US");
         // <seg>A<ph x="1" type="break" assoc="p">&lt;br/&gt;</ph>B</seg>
         TUVBuilder b = tu.tuvBuilder("en-US");
         b.text("A")
          .tag(new PlaceholderTag(1, "index", "<br/>").setAssoc("p"))
          .text("B");
         tu.addTUV(b.build());
         testRoundtripTUs(Collections.singletonList(tu));
     }
     
     @Test
     public void testMultipleTags() throws Exception {
         TU tu = new TU("en-US");
         TUVBuilder b = tu.tuvBuilder("en-US");
         b.text("A")
          .tag(new PlaceholderTag(1, "index", "<br />"))
          .text("B")
          .tag(new BeginTag(2, 1, new ComplexContent()
                                              .addCodes("<a href='#' title='")
                                              .addSubflow(b.nested().text("this is translatable"))
                                              .addCodes("'>"))
                      .setType("link"))
          .text("C")
          .ept(1, "</a>");
        tu.addTUV(b.build());
        testRoundtripTUs(Collections.singletonList(tu));
     }
     
     @Test
     public void testUnmatchedPairedTagConversion() throws Exception {
         File tmp = File.createTempFile("otter", ".tmx");
         Writer w = new OutputStreamWriter(new FileOutputStream(tmp), "UTF-8");
         TMXWriter writer = TMXWriter.createTMXEventWriter(w);
         writer.startTMX();
         writer.writeHeader(getHeader());
         writer.startBody();
         TU tu = new TU();
         TUV src = new TUV("en-US");
         src.addContent(new TextContent("Dangling "));
         src.addContent(new BeginTag(1));
         src.addContent(new TextContent(" tag"));
         tu.addTUV(src);
         writer.writeEvent(new TUEvent(tu));
         writer.endBody();
         writer.endTMX();
         w.close();
         Reader r = new InputStreamReader(new FileInputStream(tmp), "UTF-8");
         TMXReader reader = TMXReader.createTMXEventReader(r);
         List<TU> tus = readTUs(reader);
         assertEquals(1, tus.size());
         TU tgtTu = tus.get(0);
         Map<String, TUV> tuvs = tgtTu.getTuvs();
         assertEquals(1, tuvs.size());
         TUV tgtTuv = tuvs.get("en-US");
         assertNotNull(tgtTuv);
         List<TUVContent> contents = tgtTuv.getContents();
         assertEquals(3, contents.size());
         assertEquals(new TextContent("Dangling "), contents.get(0));
         IsolatedTag it = new IsolatedTag(IsolatedTag.Pos.BEGIN);
         assertEquals(it, contents.get(1));
         assertEquals(new TextContent(" tag"), contents.get(2));
     }
     
     private Header getHeader() {
         Header header = new Header();
         header.setCreationTool("Otter TMX");
         header.setCreationToolVersion("1.0");
         header.setSegType("sentence");
         header.setTmf("Otter TMX");
         header.setAdminLang("en-US");
         header.setSrcLang("en-US");
         header.setDataType("text");
         header.addProperty(new Property("type1", "Property"));
         header.addNote(new Note("This is a note"));
         return header;
     }
     
     @Test
     public void testRoundtripHeader() throws Exception {
         testRoundtrip("/header.tmx");
     }
     
     @Test
     public void testRoundtripBody() throws Exception {
         testRoundtrip("/body.tmx");
     }
     
     @Test
     public void testRoundtripPairedTags() throws Exception {
         testRoundtrip("/paired_tags.tmx");
     }
 
     @Test
     public void testRoundtripIsolatedTags() throws Exception {
         testRoundtrip("/it_tag.tmx");
     }
     
     @Test
     public void testHiTags() throws Exception {
         testRoundtrip("/hi_tag.tmx");
     }
 
     @Test
     public void testNestedHiTags() throws Exception {
         testRoundtrip("/hi_nested.tmx");
     }
     
     @Test
     public void testSubflow() throws Exception {
         testRoundtrip("/subflow.tmx");
     }
     
     public void testRoundtrip(String resourceName) throws Exception {
         InputStream is = getClass().getResourceAsStream(resourceName);
         TMXReader reader = TMXReader.createTMXEventReader(
                             new InputStreamReader(is, "UTF-8"));
         File tmp = File.createTempFile("otter", ".tmx");
         List<TMXEvent> events = readEvents(reader);
         Writer w = new OutputStreamWriter(new FileOutputStream(tmp), "UTF-8");
         TMXWriter writer = TMXWriter.createTMXEventWriter(w);
         for (TMXEvent e : events) {
             writer.writeEvent(e);
         }
         w.close();
         
         // Now verify!
         TMXReader roundtripReader = TMXReader.createTMXEventReader(
                         new InputStreamReader(new FileInputStream(tmp), "UTF-8"));
         List<TMXEvent> roundtripEvents = readEvents(roundtripReader);
         assertEquals(events, roundtripEvents);
         tmp.delete();
     }
     
     // TODO: unittest that verifies that we write the required attributes
     // TODO: properties with encoding and xml:lang properties
     // TODO: notes with encoding and xml:lang properties
 }
