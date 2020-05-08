 /*
 Copyright (c) 2013 Robby, Kansas State University.        
 All rights reserved. This program and the accompanying materials      
 are made available under the terms of the Eclipse Public License v1.0 
 which accompanies this distribution, and is available at              
 http://www.eclipse.org/legal/epl-v10.html                             
 */
 
 package edu.ksu.cis.santos.mdcf.dms.test;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.StandardCharsets;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.lang3.tuple.Pair;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Multimap;
 import com.google.common.io.Files;
 
 import edu.ksu.cis.santos.mdcf.dml.ast.Attribute;
 import edu.ksu.cis.santos.mdcf.dml.ast.BasicType;
 import edu.ksu.cis.santos.mdcf.dml.ast.Declaration;
 import edu.ksu.cis.santos.mdcf.dml.ast.Feature;
 import edu.ksu.cis.santos.mdcf.dml.ast.Invariant;
 import edu.ksu.cis.santos.mdcf.dml.ast.Member;
 import edu.ksu.cis.santos.mdcf.dml.ast.Model;
 import edu.ksu.cis.santos.mdcf.dml.symbol.SymbolTable;
 import edu.ksu.cis.santos.mdcf.dml.symbol.SymbolTable.Kind;
 import edu.ksu.cis.santos.mdcf.dms.ModelExtractor;
 
 /**
  * @author <a href="mailto:robby@k-state.edu">Robby</a>
  */
 public class SymbolTableTest {
 
   private static boolean GENERATE_EXPECTED = false;
   private static SymbolTable ST = SymbolTable.of(ModelExtractor
       .extractModel(new String[] { "edu.ksu.cis.santos.mdcf.dms.example" }));
 
  private static final String lineSep = "\n";
 
   @Test
   public void allAttributes() throws Exception {
     allMembers(Attribute.class);
   }
 
   @Test
   public void allInvariants() throws Exception {
     allMembers(Invariant.class);
   }
 
   @Test
   public void allMembers() throws Exception {
     allMembers(Member.class);
   }
 
   <T extends Member> void allMembers(final Class<T> clazz) throws Exception {
     final SymbolTable st = SymbolTableTest.ST;
     final StringBuilder sb = new StringBuilder();
 
     final String className = clazz.getSimpleName();
 
     final String title = "All (Declared and Inherited) " + className + "s";
     appendln(sb, title);
     for (int i = 0; i < title.length(); i++) {
       sb.append('=');
     }
     appendln(sb);
 
     final Set<String> featureNames = featureOrDeviceNames();
 
     for (final String featureName : featureNames) {
       appendln(sb);
       sb.append("* ");
       sb.append(shorten(featureName));
       sb.append(": ");
       final Collection<Pair<Feature, T>> c = st.filterp(
           st.allMemberMap(featureName),
           clazz).values();
       for (final Pair<Feature, T> p : c) {
         sb.append('(');
         sb.append(shorten(p.getLeft().name));
         sb.append(", ");
         final Member right = p.getRight();
         sb.append(right instanceof Attribute ? "attr:" : "inv:");
         sb.append(right.name);
         sb.append("), ");
       }
       deleteLastChars(sb, 2);
     }
 
     testExpectedResult(
         "dms.test.symbol.all" + className.toLowerCase() + "s",
         sb.toString());
   }
 
   void appendln(final StringBuilder sb) {
     sb.append(SymbolTableTest.lineSep);
   }
 
   void appendln(final StringBuilder sb, final String line) {
     sb.append(line);
     sb.append(SymbolTableTest.lineSep);
   }
 
   void assertEquals(final File expected, final File result) throws Exception {
     final String expectedContent = Files.toString(
         expected,
         StandardCharsets.US_ASCII);
     final String resultContent = Files.toString(
         result,
         StandardCharsets.US_ASCII);
     assertThat(resultContent).isEqualTo(expectedContent);
   }
 
   @Test
   public void declarationKindAsserts() {
     final SymbolTable st = SymbolTableTest.ST;
     for (final Model m : st.models) {
       for (final Declaration d : m.declarations) {
         final String declarationName = d.name;
         if (d instanceof BasicType) {
           assertThat(st.kind(declarationName)).isEqualTo(Kind.BasicType);
           assertThat(st.isBasicType(declarationName)).isTrue();
           assertThat(st.isFeature(declarationName)).isFalse();
           assertThat(st.isRequirement(declarationName)).isFalse();
           assertThat(st.basicType(declarationName)).isNotNull();
         } else if (d instanceof Feature) {
           assertThat(st.kind(declarationName)).isEqualTo(Kind.Feature);
           assertThat(st.isBasicType(declarationName)).isFalse();
           assertThat(st.isFeature(declarationName)).isTrue();
           assertThat(st.isRequirement(declarationName)).isFalse();
           assertThat(st.feature(declarationName)).isNotNull();
         } else {
           assertThat(st.isBasicType(declarationName)).isFalse();
           assertThat(st.isFeature(declarationName)).isFalse();
           assertThat(st.isRequirement(declarationName)).isTrue();
           assertThat(st.requirement(declarationName)).isNotNull();
         }
       }
     }
 
     assertThat(st.basicTypes()).isNotEmpty();
     assertThat(st.features()).isNotEmpty();
     assertThat(st.requirements()).isNotEmpty();
   }
 
   @Test
   public void declaredAttributes() throws Exception {
     declaredMembers(Attribute.class);
   }
 
   @Test
   public void declaredInvariants() throws Exception {
     declaredMembers(Invariant.class);
   }
 
   @Test
   public void declaredMemberAsserts() {
     final SymbolTable st = SymbolTableTest.ST;
     for (final Feature f : st.features()) {
       final String featureName = f.name;
       final Map<String, Member> mm = st.declaredMemberMap(featureName);
       final Map<String, Attribute> am = st.declaredAttributeMap(featureName);
       final Map<String, Invariant> im = st.declaredInvariantMap(featureName);
       for (final Member m : f.members) {
         final String memberName = m.name;
         assertThat(mm.containsKey(memberName)).isTrue();
         assertThat(mm.get(memberName)).isEqualTo(m);
         if (m instanceof Attribute) {
           assertThat(am.containsKey(memberName)).isTrue();
           assertThat(am.get(memberName)).isEqualTo((Attribute) m);
         } else {
           assertThat(im.containsKey(memberName)).isTrue();
           assertThat(im.get(memberName)).isEqualTo((Invariant) m);
         }
       }
     }
   }
 
   @Test
   public void declaredMembers() throws Exception {
     declaredMembers(Member.class);
   }
 
   <T extends Member> void declaredMembers(final Class<T> clazz)
       throws Exception {
     final SymbolTable st = SymbolTableTest.ST;
     final StringBuilder sb = new StringBuilder();
 
     final String className = clazz.getSimpleName();
 
     final String title = "Declared " + className + "s";
     appendln(sb, title);
     for (int i = 0; i < title.length(); i++) {
       sb.append('=');
     }
     appendln(sb);
 
     final Set<String> featureNames = featureOrDeviceNames();
 
     for (final String featureName : featureNames) {
       appendln(sb);
       sb.append("* ");
       sb.append(shorten(featureName));
       sb.append(": ");
       final Collection<T> c = st.filter(
           st.declaredMemberMap(featureName),
           clazz).values();
       for (final T t : c) {
         sb.append(t instanceof Attribute ? "attr:" : "inv:");
         sb.append(t.name);
         sb.append(", ");
       }
       deleteLastChars(sb, 2);
     }
 
     testExpectedResult("dms.test.symbol.declared" + className.toLowerCase()
         + "s", sb.toString());
   }
 
   void deleteLastChars(final StringBuilder sb, final int n) {
     final int length = sb.length();
     sb.delete(length - 2, length);
   }
 
   Set<String> featureOrDeviceNames() {
     final SymbolTable st = SymbolTableTest.ST;
     final Set<String> featureNames = new TreeSet<String>();
     for (final Feature f : st.features()) {
       featureNames.add(f.name);
     }
     return featureNames;
   }
 
   String shorten(final String name) {
     return name.startsWith("edu.ksu.cis.santos.mdcf.dms.example.") ? name
         .substring("edu.ksu.cis.santos.mdcf.dms.example.".length()) : name;
   }
 
   @Test
   public void subTransitiveMap() throws Exception {
     superSubTransitiveMap(false);
   }
 
   void superSubTransitiveMap(final boolean isSuper) throws Exception {
     final SymbolTable st = SymbolTableTest.ST;
     final StringBuilder sb = new StringBuilder();
 
     final String title = (isSuper ? "Super" : "Sub") + " Transitive Map";
     appendln(sb, title);
     for (int i = 0; i < title.length(); i++) {
       sb.append('=');
     }
     appendln(sb);
 
     final Multimap<String, String> m = isSuper ? st.superTransitiveMap() : st
         .subTransitiveMap();
     for (final String name : ImmutableSortedSet.copyOf(m.keySet())) {
       appendln(sb);
       sb.append("* ");
       sb.append(shorten(name));
       sb.append(": ");
       for (final String superName : m.get(name)) {
         sb.append(shorten(superName));
         sb.append(", ");
       }
       deleteLastChars(sb, 2);
     }
 
     testExpectedResult(
         "dms.test.symbol." + (isSuper ? "supermap" : "submap"),
         sb.toString());
   }
 
   @Test
   public void superTransitiveMap() throws Exception {
     superSubTransitiveMap(true);
   }
 
   void testExpectedResult(final String name, final String content)
       throws URISyntaxException, IOException, Exception {
     final File testDir = new File(new URI(getClass().getResource("").toURI()
         .toString().replace("/bin/", "/src/test/resources/")));
 
     final File expected = new File(testDir, "expected/" + name + ".rst");
     final File result = new File(testDir, "result/" + name + ".rst");
     if (SymbolTableTest.GENERATE_EXPECTED || !expected.exists()) {
       Files.write(content, expected, StandardCharsets.US_ASCII);
     } else {
       Files.write(content, result, StandardCharsets.US_ASCII);
       assertEquals(expected, result);
     }
   }
 }
