 package org.eclipselabs.recommenders.test.codesearch.rcp.indexer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.recommenders.codesearch.rcp.index.Fields;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.AllDeclaredFieldNamesIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.AnnotationsIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.DeclaredFieldNamesIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.DeclaredFieldTypesIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.DeclaringTypeIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.DocumentTypeIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.FieldsReadIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.FieldsWrittenIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.FullTextIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.InstanceOfIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.ModifiersIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.ProjectNameIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.QualifiedNameIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.ResourcePathIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.SimpleNameIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.TimestampIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.UsedFieldsInFinallyIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.UsedFieldsInTryIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.UsedMethodsIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.UsedTypesIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.interfaces.IIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.interfaces.ITryCatchBlockIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.visitor.CompilationUnitVisitor;
 import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
 import org.eclipse.recommenders.utils.Tuple;
 import org.eclipse.xtend2.lib.StringConcatenation;
 import org.eclipse.xtext.xbase.lib.CollectionLiterals;
 import org.eclipse.xtext.xbase.lib.Conversions;
 import org.eclipse.xtext.xbase.lib.Exceptions;
 import org.eclipselabs.recommenders.test.codesearch.rcp.indexer.TestBase;
 import org.junit.Test;
 
 @SuppressWarnings("all")
 public class TestGeneralScenarios extends TestBase {
   @Test
   public void testDocumentCounts() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       this.exercise(code, _simpleNameIndexer);
       this.assertNumDocs(1);
   }
   
   @Test
   public void testDocumentCounts02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void test() {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       this.exercise(code, _simpleNameIndexer);
       this.assertNumDocs(2);
   }
   
   @Test
   public void testDocumentCounts03() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.List;");
       _builder.newLine();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.newLine();
       _builder.append("public class MyClass {\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public List test() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("return null;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       this.exercise(code, _simpleNameIndexer);
       this.assertNumDocs(3);
   }
   
   @Test
   public void testFriendlyNameIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       this.exercise(code, _simpleNameIndexer);
       String _s = this.s(Fields.SIMPLE_NAME, "MyClass");
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFriendlyNameIndexer02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void test() {}");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       this.exercise(code, _simpleNameIndexer);
       String _s = this.s(Fields.SIMPLE_NAME, "test");
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFriendlyNameIndexer03() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       this.exercise(code, _simpleNameIndexer);
       String _s = this.s(Fields.SIMPLE_NAME, "map");
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFullyQualifiedNameIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       QualifiedNameIndexer _qualifiedNameIndexer = new QualifiedNameIndexer();
       this.exercise(code, _qualifiedNameIndexer);
       String _s = this.s(Fields.QUALIFIED_NAME, "LMyClass");
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFullyQualifiedNameIndexer02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void test() {}");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       QualifiedNameIndexer _qualifiedNameIndexer = new QualifiedNameIndexer();
       this.exercise(code, _qualifiedNameIndexer);
       String _s = this.s(Fields.QUALIFIED_NAME, "LMyClass.test()V");
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFullyQualifiedNameIndexer03() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map mapInstance;");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       QualifiedNameIndexer _qualifiedNameIndexer = new QualifiedNameIndexer();
       this.exercise(code, _qualifiedNameIndexer);
       String _s = this.s(Fields.QUALIFIED_NAME, "LMyClass.mapInstance");
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDocumentTypeIndexerClassOnly() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       this.exercise(code, _documentTypeIndexer);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDocumentTypeIndexerClassAndField() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       this.exercise(code, _documentTypeIndexer);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
       String _s_1 = this.s(Fields.TYPE, Fields.TYPE_FIELD);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s_1);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l_1);
   }
   
   @Test
   public void testDocumentTypeIndexerClassAndMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void test() {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("} ");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       this.exercise(code, _documentTypeIndexer);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
       String _s_1 = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s_1);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l_1);
   }
   
   @Test
   public void testDocumentTypeIndexerClassMethodAndTryCatch() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.newLine();
       _builder.append("public class MyClass {\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void test() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("if(map != null) { throw new Exception(); }");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("return null;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       this.exercise(code, _documentTypeIndexer);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
       String _s_1 = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s_1);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l_1);
       String _s_2 = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_2);
       List<String> _l_2 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertField(_l_2);
   }
   
   @Test
   public void testUsedTypesIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.List;");
       _builder.newLine();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.newLine();
       _builder.append("public class MyClass {\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       UsedTypesIndexer _usedTypesIndexer = new UsedTypesIndexer();
       this.exercise(code, _usedTypesIndexer);
       String _s = this.s(Fields.USED_TYPES, "Ljava/util/Map");
       ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(_s);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testUsedTypesIndexer02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.List;");
       _builder.newLine();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.newLine();
       _builder.append("public class MyClass {\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public List test() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("return null;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       UsedTypesIndexer _usedTypesIndexer = new UsedTypesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_usedTypesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.USED_TYPES, "Ljava/util/Map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
       String _s_2 = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_3 = this.s(Fields.USED_TYPES, "Ljava/util/List");
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_2, _s_3);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertField(_l_1);
   }
   
   @Test
   public void testUsedTypesIndexer03() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.newLine();
       _builder.append("public class MyClass {\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void test() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("return null;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       UsedTypesIndexer _usedTypesIndexer = new UsedTypesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_usedTypesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.USED_TYPES, "Ljava/util/Map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
       String _s_2 = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_3 = this.s(Fields.USED_TYPES, "Ljava/util/Map");
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_2, _s_3);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertField(_l_1);
   }
   
   @Test
   public void testUsedTypesIndexerNoPrimitivesStringObjectEtc() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.newLine();
       _builder.append("public class MyClass {\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("String f;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Object o1;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void test() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("String g;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Object o2;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       UsedTypesIndexer _usedTypesIndexer = new UsedTypesIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _usedTypesIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.USED_TYPES, "Ljava/lang/String");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertNotField(_l);
       String _s_2 = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_3 = this.s(Fields.USED_TYPES, "Ljava/lang/String");
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_2, _s_3);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertNotField(_l_1);
       String _s_4 = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_5 = this.s(Fields.USED_TYPES, "Ljava/lang/Object");
       ArrayList<String> _newArrayList_3 = CollectionLiterals.<String>newArrayList(_s_4, _s_5);
       List<String> _l_2 = this.l(((String[])Conversions.unwrapArray(_newArrayList_3, String.class)));
       this.assertNotField(_l_2);
       String _s_6 = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_7 = this.s(Fields.USED_TYPES, "Ljava/lang/Object");
       ArrayList<String> _newArrayList_4 = CollectionLiterals.<String>newArrayList(_s_6, _s_7);
       List<String> _l_3 = this.l(((String[])Conversions.unwrapArray(_newArrayList_4, String.class)));
       this.assertNotField(_l_3);
   }
   
   @Test
   public void testUsedMethodsIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.newLine();
       _builder.append("public class MyClass {\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public List test() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Map<String, String> map;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("map.put(\"\", \"\");");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       UsedMethodsIndexer _usedMethodsIndexer = new UsedMethodsIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_usedMethodsIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.USED_METHODS, "Ljava/util/Map.put(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
       String _s_2 = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_3 = this.s(Fields.USED_METHODS, "Ljava/util/Map.put(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_2, _s_3);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertField(_l_1);
   }
   
   @Test
   public void testUsedMethodsIndexer02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyClass {\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public List test() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Map<String, String> map;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("map.put(\"\", \"\");");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       UsedMethodsIndexer _usedMethodsIndexer = new UsedMethodsIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_usedMethodsIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.USED_METHODS, "Ljava/util/Map.put(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
       String _s_2 = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_3 = this.s(Fields.USED_METHODS, "Ljava/util/Map.put(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_2, _s_3);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertField(_l_1);
       String _s_4 = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_5 = this.s(Fields.USED_METHODS, "Ljava/util/Map.put(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
       ArrayList<String> _newArrayList_3 = CollectionLiterals.<String>newArrayList(_s_4, _s_5);
       List<String> _l_2 = this.l(((String[])Conversions.unwrapArray(_newArrayList_3, String.class)));
       this.assertField(_l_2);
   }
   
   @Test
   public void testDeclaringTypeIndexerMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void foo() {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       DeclaringTypeIndexer _declaringTypeIndexer = new DeclaringTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_simpleNameIndexer, _declaringTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.SIMPLE_NAME, "foo");
       String _s_1 = this.s(Fields.DECLARING_TYPE, "LMyClass");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaringTypeIndexerField() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       DeclaringTypeIndexer _declaringTypeIndexer = new DeclaringTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_simpleNameIndexer, _declaringTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.SIMPLE_NAME, "map");
       String _s_1 = this.s(Fields.DECLARING_TYPE, "LMyClass");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaringTypeIndexerType() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public class SubClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       SimpleNameIndexer _simpleNameIndexer = new SimpleNameIndexer();
       DeclaringTypeIndexer _declaringTypeIndexer = new DeclaringTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_simpleNameIndexer, _declaringTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
      String _s = this.s(Fields.SIMPLE_NAME, "MyClass$SubClass");
       String _s_1 = this.s(Fields.DECLARING_TYPE, "LMyClass");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaringTypeIndexerVarUsage() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void testMethod123() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("String s;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       DeclaringTypeIndexer _declaringTypeIndexer = new DeclaringTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _declaringTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_VARUSAGE);
       String _s_1 = this.s(Fields.DECLARING_TYPE, "LMyClass");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaringTypeIndexerTryCatch() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void testMethod123() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try{} catch(Exception ex){}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       DeclaringTypeIndexer _declaringTypeIndexer = new DeclaringTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _declaringTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.DECLARING_TYPE, "LMyClass");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testProjectNameIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ProjectNameIndexer _projectNameIndexer = new ProjectNameIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_projectNameIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i, "projectName");
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.PROJECT_NAME, "projectName");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testProjectNameIndexer02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void myMethod() {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ProjectNameIndexer _projectNameIndexer = new ProjectNameIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_projectNameIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i, "projectName");
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.PROJECT_NAME, "projectName");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testProjectNameIndexer03() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("MyClass test;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ProjectNameIndexer _projectNameIndexer = new ProjectNameIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_projectNameIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i, "projectName");
       String _s = this.s(Fields.TYPE, Fields.TYPE_FIELD);
       String _s_1 = this.s(Fields.PROJECT_NAME, "projectName");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testProjectNameIndexer04() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void myMethod() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) {}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ProjectNameIndexer _projectNameIndexer = new ProjectNameIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_projectNameIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i, "projectName");
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.PROJECT_NAME, "projectName");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testResourcePathIndexer() {
     try {
       {
         StringConcatenation _builder = new StringConcatenation();
         _builder.append("public class MyClass {");
         _builder.newLine();
         _builder.append("}");
         _builder.newLine();
         final CharSequence code = _builder;
         IWorkspace _workspace = ResourcesPlugin.getWorkspace();
         JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "projectName");
         final JavaProjectFixture fixture = _javaProjectFixture;
         String _string = code.toString();
         Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string);
         final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
         ICompilationUnit _first = struct.getFirst();
         final ICompilationUnit cu = _first;
         ASTNode _parse = TestBase.parse(cu);
         ASTNode cuParsed = _parse;
         CompilationUnitVisitor _compilationUnitVisitor = new CompilationUnitVisitor(this.f.index);
         CompilationUnitVisitor visitor = _compilationUnitVisitor;
         ResourcePathIndexer _resourcePathIndexer = new ResourcePathIndexer();
         DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
         ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_resourcePathIndexer, _documentTypeIndexer);
         List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
         visitor.addIndexer(_i);
         cuParsed.accept(visitor);
         this.f.index.commit();
         String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
         CompilationUnit _compilationUnitFromAstNode = this.getCompilationUnitFromAstNode(cuParsed);
         String _path = ResourcePathIndexer.getPath(_compilationUnitFromAstNode);
         String _s_1 = this.s(Fields.RESOURCE_PATH, _path);
         ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
         List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
         this.assertField(_l);
       }
     } catch (Exception _e) {
       throw Exceptions.sneakyThrow(_e);
     }
   }
   
   @Test
   public void testResourcePathIndexer02() {
     try {
       {
         StringConcatenation _builder = new StringConcatenation();
         _builder.append("public class MyClass {");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("public void myMethod() {");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("}");
         _builder.newLine();
         _builder.append("}");
         _builder.newLine();
         final CharSequence code = _builder;
         IWorkspace _workspace = ResourcesPlugin.getWorkspace();
         JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "projectName");
         final JavaProjectFixture fixture = _javaProjectFixture;
         String _string = code.toString();
         Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string);
         final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
         ICompilationUnit _first = struct.getFirst();
         final ICompilationUnit cu = _first;
         ASTNode _parse = TestBase.parse(cu);
         ASTNode cuParsed = _parse;
         CompilationUnitVisitor _compilationUnitVisitor = new CompilationUnitVisitor(this.f.index);
         CompilationUnitVisitor visitor = _compilationUnitVisitor;
         ResourcePathIndexer _resourcePathIndexer = new ResourcePathIndexer();
         DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
         ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_resourcePathIndexer, _documentTypeIndexer);
         List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
         visitor.addIndexer(_i);
         cuParsed.accept(visitor);
         this.f.index.commit();
         String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
         CompilationUnit _compilationUnitFromAstNode = this.getCompilationUnitFromAstNode(cuParsed);
         String _path = ResourcePathIndexer.getPath(_compilationUnitFromAstNode);
         String _s_1 = this.s(Fields.RESOURCE_PATH, _path);
         ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
         List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
         this.assertField(_l);
       }
     } catch (Exception _e) {
       throw Exceptions.sneakyThrow(_e);
     }
   }
   
   @Test
   public void testResourcePathIndexer03() {
     try {
       {
         StringConcatenation _builder = new StringConcatenation();
         _builder.append("public class MyClass {");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("MyClass test;");
         _builder.newLine();
         _builder.append("}");
         _builder.newLine();
         final CharSequence code = _builder;
         IWorkspace _workspace = ResourcesPlugin.getWorkspace();
         JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "projectName");
         final JavaProjectFixture fixture = _javaProjectFixture;
         String _string = code.toString();
         Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string);
         final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
         ICompilationUnit _first = struct.getFirst();
         final ICompilationUnit cu = _first;
         ASTNode _parse = TestBase.parse(cu);
         ASTNode cuParsed = _parse;
         CompilationUnitVisitor _compilationUnitVisitor = new CompilationUnitVisitor(this.f.index);
         CompilationUnitVisitor visitor = _compilationUnitVisitor;
         ResourcePathIndexer _resourcePathIndexer = new ResourcePathIndexer();
         DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
         ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_resourcePathIndexer, _documentTypeIndexer);
         List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
         visitor.addIndexer(_i);
         cuParsed.accept(visitor);
         this.f.index.commit();
         String _s = this.s(Fields.TYPE, Fields.TYPE_FIELD);
         CompilationUnit _compilationUnitFromAstNode = this.getCompilationUnitFromAstNode(cuParsed);
         String _path = ResourcePathIndexer.getPath(_compilationUnitFromAstNode);
         String _s_1 = this.s(Fields.RESOURCE_PATH, _path);
         ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
         List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
         this.assertField(_l);
       }
     } catch (Exception _e) {
       throw Exceptions.sneakyThrow(_e);
     }
   }
   
   @Test
   public void testResourcePathIndexer04() {
     try {
       {
         StringConcatenation _builder = new StringConcatenation();
         _builder.append("public class MyClass {");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("public void myMethod() {");
         _builder.newLine();
         _builder.append("\t\t");
         _builder.append("try {}");
         _builder.newLine();
         _builder.append("\t\t");
         _builder.append("catch(Exception ex) {}");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("}");
         _builder.newLine();
         _builder.append("}");
         _builder.newLine();
         final CharSequence code = _builder;
         IWorkspace _workspace = ResourcesPlugin.getWorkspace();
         JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "projectName");
         final JavaProjectFixture fixture = _javaProjectFixture;
         String _string = code.toString();
         Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string);
         final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
         ICompilationUnit _first = struct.getFirst();
         final ICompilationUnit cu = _first;
         ASTNode _parse = TestBase.parse(cu);
         ASTNode cuParsed = _parse;
         CompilationUnitVisitor _compilationUnitVisitor = new CompilationUnitVisitor(this.f.index);
         CompilationUnitVisitor visitor = _compilationUnitVisitor;
         ResourcePathIndexer _resourcePathIndexer = new ResourcePathIndexer();
         DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
         ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_resourcePathIndexer, _documentTypeIndexer);
         List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
         visitor.addIndexer(_i);
         cuParsed.accept(visitor);
         this.f.index.commit();
         String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
         CompilationUnit _compilationUnitFromAstNode = this.getCompilationUnitFromAstNode(cuParsed);
         String _path = ResourcePathIndexer.getPath(_compilationUnitFromAstNode);
         String _s_1 = this.s(Fields.RESOURCE_PATH, _path);
         ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
         List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
         this.assertField(_l);
       }
     } catch (Exception _e) {
       throw Exceptions.sneakyThrow(_e);
     }
   }
   
   @Test
   public void testResourcePathIndexer05() {
     try {
       {
         StringConcatenation _builder = new StringConcatenation();
         _builder.append("public class MyClass {");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("public void myMethod() {");
         _builder.newLine();
         _builder.append("\t\t");
         _builder.append("String a = \"\";");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("}");
         _builder.newLine();
         _builder.append("}");
         _builder.newLine();
         final CharSequence code = _builder;
         IWorkspace _workspace = ResourcesPlugin.getWorkspace();
         JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "projectName");
         final JavaProjectFixture fixture = _javaProjectFixture;
         String _string = code.toString();
         Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string);
         final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
         ICompilationUnit _first = struct.getFirst();
         final ICompilationUnit cu = _first;
         ASTNode _parse = TestBase.parse(cu);
         ASTNode cuParsed = _parse;
         CompilationUnitVisitor _compilationUnitVisitor = new CompilationUnitVisitor(this.f.index);
         CompilationUnitVisitor visitor = _compilationUnitVisitor;
         ResourcePathIndexer _resourcePathIndexer = new ResourcePathIndexer();
         DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
         ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_resourcePathIndexer, _documentTypeIndexer);
         List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
         visitor.addIndexer(_i);
         cuParsed.accept(visitor);
         this.f.index.commit();
         String _s = this.s(Fields.TYPE, Fields.TYPE_VARUSAGE);
         CompilationUnit _compilationUnitFromAstNode = this.getCompilationUnitFromAstNode(cuParsed);
         String _path = ResourcePathIndexer.getPath(_compilationUnitFromAstNode);
         String _s_1 = this.s(Fields.RESOURCE_PATH, _path);
         ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
         List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
         this.assertField(_l);
       }
     } catch (Exception _e) {
       throw Exceptions.sneakyThrow(_e);
     }
   }
   
   @Test
   public void testModifiersIndexerClass() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ModifiersIndexer _modifiersIndexer = new ModifiersIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_modifiersIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.MODIFIERS, "public");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testModifiersIndexerClass02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public abstract class MyClass {");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ModifiersIndexer _modifiersIndexer = new ModifiersIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_modifiersIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.MODIFIERS, Fields.MODIFIER_PUBLIC);
       String _s_2 = this.s(Fields.MODIFIERS, Fields.MODIFIER_ABSTRACT);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1, _s_2);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testModifiersIndexerMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void doSomethingNow123413() {}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ModifiersIndexer _modifiersIndexer = new ModifiersIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_modifiersIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.MODIFIERS, Fields.MODIFIER_PUBLIC);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testModifiersIndexerMethod02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public abstract class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void doSomethingNow123413() {}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ModifiersIndexer _modifiersIndexer = new ModifiersIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_modifiersIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.MODIFIERS, Fields.MODIFIER_PUBLIC);
       String _s_2 = this.s(Fields.MODIFIERS, Fields.MODIFIER_STATIC);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1, _s_2);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testModifiersIndexerField() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private Map map;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ModifiersIndexer _modifiersIndexer = new ModifiersIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_modifiersIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_FIELD);
       String _s_1 = this.s(Fields.MODIFIERS, Fields.MODIFIER_PRIVATE);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testModifiersIndexerField02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public final class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("protected static Map map;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       ModifiersIndexer _modifiersIndexer = new ModifiersIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_modifiersIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_FIELD);
       String _s_1 = this.s(Fields.MODIFIERS, Fields.MODIFIER_PROTECTED);
       String _s_2 = this.s(Fields.MODIFIERS, Fields.MODIFIER_STATIC);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1, _s_2);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
       String _s_3 = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_4 = this.s(Fields.MODIFIERS, Fields.MODIFIER_FINAL);
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_3, _s_4);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertField(_l_1);
   }
   
   @Test
   public void testDeclaredFieldNamesClass() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public final class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DeclaredFieldNamesIndexer _declaredFieldNamesIndexer = new DeclaredFieldNamesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_declaredFieldNamesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
      String _s_1 = this.s(Fields.DECLARED_FIELD_NAMES, "map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaredFieldNamesClassWithInitalizer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public final class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map = new HashMap();");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DeclaredFieldNamesIndexer _declaredFieldNamesIndexer = new DeclaredFieldNamesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_declaredFieldNamesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
      String _s_1 = this.s(Fields.DECLARED_FIELD_NAMES, "map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaredFieldNamesMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public final class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("void doSomethingElse() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DeclaredFieldNamesIndexer _declaredFieldNamesIndexer = new DeclaredFieldNamesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_declaredFieldNamesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
      String _s_1 = this.s(Fields.DECLARED_FIELD_NAMES, "map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaredFieldNamesTryCatch() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public final class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("void doSomethingElse() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) { Map map; }");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DeclaredFieldNamesIndexer _declaredFieldNamesIndexer = new DeclaredFieldNamesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_declaredFieldNamesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
      String _s_1 = this.s(Fields.DECLARED_FIELD_NAMES, "map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaredFieldTypesClass() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public final class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DeclaredFieldTypesIndexer _declaredFieldTypesIndexer = new DeclaredFieldTypesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_declaredFieldTypesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.DECLARED_FIELD_TYPES, "Ljava/util/Map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaredFieldTypesMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public final class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("void doSomethingElse() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Map map;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DeclaredFieldTypesIndexer _declaredFieldTypesIndexer = new DeclaredFieldTypesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_declaredFieldTypesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.DECLARED_FIELD_TYPES, "Ljava/util/Map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testDeclaredFieldTypesTry() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public final class MyClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("void doSomethingElse() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) { Map map; }");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DeclaredFieldTypesIndexer _declaredFieldTypesIndexer = new DeclaredFieldTypesIndexer();
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_declaredFieldTypesIndexer, _documentTypeIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.DECLARED_FIELD_TYPES, "Ljava/util/Map");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testAllFieldNamesIndexerClass() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private Map theMapyMap;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AllDeclaredFieldNamesIndexer _allDeclaredFieldNamesIndexer = new AllDeclaredFieldNamesIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _allDeclaredFieldNamesIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "theMapyMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testAllFieldNamesIndexerMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private Map theMapyMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("void doThisAndThat() {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AllDeclaredFieldNamesIndexer _allDeclaredFieldNamesIndexer = new AllDeclaredFieldNamesIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _allDeclaredFieldNamesIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "theMapyMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testAllFieldNamesIndexerMethod02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private Map theMapyMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("void doThisAndThat() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("private Map someOtherMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AllDeclaredFieldNamesIndexer _allDeclaredFieldNamesIndexer = new AllDeclaredFieldNamesIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _allDeclaredFieldNamesIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "theMapyMap");
       String _s_2 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "someOtherMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1, _s_2);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testAllFieldNamesIndexerTryCatch() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private Map theMapyMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("void doThisAndThat() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) {}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AllDeclaredFieldNamesIndexer _allDeclaredFieldNamesIndexer = new AllDeclaredFieldNamesIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _allDeclaredFieldNamesIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "theMapyMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testAllFieldNamesIndexerTryCatch02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("public class MyOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private Map theMapyMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("void doThisAndThat() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("private Map someOtherMap;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AllDeclaredFieldNamesIndexer _allDeclaredFieldNamesIndexer = new AllDeclaredFieldNamesIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _allDeclaredFieldNamesIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "theMapyMap");
       String _s_2 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "someOtherMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1, _s_2);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testAllFieldNamesIndexerClass02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MyOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private Map theMapyMap;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AllDeclaredFieldNamesIndexer _allDeclaredFieldNamesIndexer = new AllDeclaredFieldNamesIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _allDeclaredFieldNamesIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "theMapyMap");
       String _s_2 = this.s(Fields.ALL_DECLARED_FIELD_NAMES, "stackTrace");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1, _s_2);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFullTextIndexerClass() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MOtherException extends IOException {");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       FullTextIndexer _fullTextIndexer = new FullTextIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _fullTextIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       StringConcatenation _builder_1 = new StringConcatenation();
       _builder_1.append("public class MOtherException extends IOException {");
       _builder_1.newLine();
       _builder_1.append("}");
       String _string = _builder_1.toString();
       String _s_1 = this.s(Fields.FULL_TEXT, _string);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFullTextIndexerMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MyTinyException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void theEasiestMethodEver() {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       FullTextIndexer _fullTextIndexer = new FullTextIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _fullTextIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       StringConcatenation _builder_1 = new StringConcatenation();
       _builder_1.append("public static void theEasiestMethodEver(){");
       _builder_1.newLine();
       _builder_1.append("}");
       String _string = _builder_1.toString();
       String _s_1 = this.s(Fields.FULL_TEXT, _string);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFullTextIndexerTryCatch() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MyRandomException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void theWorstMethodEver() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("//This is a comment");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       FullTextIndexer _fullTextIndexer = new FullTextIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _fullTextIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       StringConcatenation _builder_1 = new StringConcatenation();
       _builder_1.append("catch (Exception ex) {");
       _builder_1.newLine();
       _builder_1.append("}");
       String _string = _builder_1.toString();
       String _s_1 = this.s(Fields.FULL_TEXT, _string);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFullTextIndexerField() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MyOtherOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("Map theWorldMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void theBestMethodEver() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("//This is a comment");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       FullTextIndexer _fullTextIndexer = new FullTextIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _fullTextIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_FIELD);
       StringConcatenation _builder_1 = new StringConcatenation();
       _builder_1.append("Map theWorldMap;");
       String _string = _builder_1.toString();
       String _s_1 = this.s(Fields.FULL_TEXT, _string);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFieldsReadIndexerMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MyOtherOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Map theWorldMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void theBestMethodEver() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("MyOtherOtherException m;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Object o = m.theWorldMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       FieldsReadIndexer _fieldsReadIndexer = new FieldsReadIndexer();
       FieldsWrittenIndexer _fieldsWrittenIndexer = new FieldsWrittenIndexer();
       ArrayList<IIndexer> _newArrayList = CollectionLiterals.<IIndexer>newArrayList(_documentTypeIndexer, _fieldsReadIndexer, _fieldsWrittenIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.FIELDS_READ, "LMyOtherOtherException.theWorldMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFieldsReadIndexerClass() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class Testclass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Object someObject = null;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Testclass ob = new Testclass();");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Object anObject = ob.someObject;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       FieldsReadIndexer _fieldsReadIndexer = new FieldsReadIndexer();
       FieldsWrittenIndexer _fieldsWrittenIndexer = new FieldsWrittenIndexer();
       ArrayList<IIndexer> _newArrayList = CollectionLiterals.<IIndexer>newArrayList(_documentTypeIndexer, _fieldsReadIndexer, _fieldsWrittenIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.FIELDS_READ, "LTestclass.someObject");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFieldsReadIndexerTryCatch() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class Testclass extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Object someObject = null;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Object theWorldMap = (new Testclass()).someObject;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void theBestMethodEver() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("Testclass c = new Testclass();");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("Object myObject = c.someObject;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       FieldsReadIndexer _fieldsReadIndexer = new FieldsReadIndexer();
       FieldsWrittenIndexer _fieldsWrittenIndexer = new FieldsWrittenIndexer();
       ArrayList<IIndexer> _newArrayList = CollectionLiterals.<IIndexer>newArrayList(_documentTypeIndexer, _fieldsReadIndexer, _fieldsWrittenIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.FIELDS_READ, "LTestclass.someObject");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testFieldsWrittenIndexerMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MyOtherOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Map theWorldMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void theBestMethodEver() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("MyOtherOtherException m = null;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("m.theWorldMap = null;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       FieldsReadIndexer _fieldsReadIndexer = new FieldsReadIndexer();
       FieldsWrittenIndexer _fieldsWrittenIndexer = new FieldsWrittenIndexer();
       ArrayList<IIndexer> _newArrayList = CollectionLiterals.<IIndexer>newArrayList(_documentTypeIndexer, _fieldsReadIndexer, _fieldsWrittenIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.FIELDS_WRITTEN, "LMyOtherOtherException.theWorldMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testUsedFieldsInTryIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MyOtherOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Map theWorldMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void theBestMethodEver() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("MyOtherOtherException m = null;");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("m.theWorldMap = null;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} finally {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       UsedFieldsInTryIndexer _usedFieldsInTryIndexer = new UsedFieldsInTryIndexer();
       ArrayList<ITryCatchBlockIndexer> _newArrayList = CollectionLiterals.<ITryCatchBlockIndexer>newArrayList(_documentTypeIndexer, _usedFieldsInTryIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.USED_FIELDS_IN_TRY, "LMyOtherOtherException.theWorldMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testUsedFieldsInFinallyIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.Map;");
       _builder.newLine();
       _builder.append("import java.io.IOException;");
       _builder.newLine();
       _builder.append("public class MyOtherOtherException extends IOException {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public Map theWorldMap;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static void theBestMethodEver() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} finally {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("MyOtherOtherException m = null;");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("m.theWorldMap = null;");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       UsedFieldsInFinallyIndexer _usedFieldsInFinallyIndexer = new UsedFieldsInFinallyIndexer();
       ArrayList<ITryCatchBlockIndexer> _newArrayList = CollectionLiterals.<ITryCatchBlockIndexer>newArrayList(_documentTypeIndexer, _usedFieldsInFinallyIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.USED_FIELDS_IN_FINALLY, "LMyOtherOtherException.theWorldMap");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testAnnotationIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("@Deprecated");
       _builder.newLine();
       _builder.append("public class MyAnnotatedClass {");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AnnotationsIndexer _annotationsIndexer = new AnnotationsIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _annotationsIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.ANNOTATIONS, "Ljava/lang/Deprecated");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testAnnotationIndexer02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("@SuppressWarnings({\"unchecked\", \"rawtypes\"})");
       _builder.newLine();
       _builder.append("public class MyAnnotatedClass {");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AnnotationsIndexer _annotationsIndexer = new AnnotationsIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _annotationsIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.ANNOTATIONS, "Ljava/lang/SuppressWarnings");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
       String _s_2 = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_3 = this.s(Fields.ANNOTATIONS, "Ljava/lang/SuppressWarnings:unchecked");
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_2, _s_3);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertField(_l_1);
       String _s_4 = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_5 = this.s(Fields.ANNOTATIONS, "Ljava/lang/SuppressWarnings:rawtypes");
       ArrayList<String> _newArrayList_3 = CollectionLiterals.<String>newArrayList(_s_4, _s_5);
       List<String> _l_2 = this.l(((String[])Conversions.unwrapArray(_newArrayList_3, String.class)));
       this.assertField(_l_2);
   }
   
   @Test
   public void testAnnotationIndexer03() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("import java.util.List;");
       _builder.newLine();
       _builder.append("public class MyAnnotatedClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("@SuppressWarnings(\"rawtypes\")");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public static String printLabel(List l) {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       AnnotationsIndexer _annotationsIndexer = new AnnotationsIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _annotationsIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.ANNOTATIONS, "Ljava/lang/SuppressWarnings");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
       String _s_2 = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_3 = this.s(Fields.ANNOTATIONS, "Ljava/lang/SuppressWarnings:rawtypes");
       ArrayList<String> _newArrayList_2 = CollectionLiterals.<String>newArrayList(_s_2, _s_3);
       List<String> _l_1 = this.l(((String[])Conversions.unwrapArray(_newArrayList_2, String.class)));
       this.assertField(_l_1);
   }
   
   @Test
   public void testInstanceOfIndexerClass() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyInstanceOfClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void operation() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Object a = new String();");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("if(a instanceof Exception) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("//Somethin\'s fishy");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} ");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       InstanceOfIndexer _instanceOfIndexer = new InstanceOfIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _instanceOfIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _s_1 = this.s(Fields.INSTANCEOF_TYPES, "Ljava/lang/Exception");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testInstanceOfIndexerMethod() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyInstanceOfClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void operation() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Object a = new String();");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("if(a instanceof Exception) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("//Somethin\'s fishy");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("} ");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       InstanceOfIndexer _instanceOfIndexer = new InstanceOfIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _instanceOfIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _s_1 = this.s(Fields.INSTANCEOF_TYPES, "Ljava/lang/Exception");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testInstanceOfIndexerTryCatch() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyInstanceOfClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void operation() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("Object a = new String();");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("if(a instanceof Exception) {");
       _builder.newLine();
       _builder.append("\t\t\t\t");
       _builder.append("//Somethin\'s fishy");
       _builder.newLine();
       _builder.append("\t\t\t");
       _builder.append("} ");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       InstanceOfIndexer _instanceOfIndexer = new InstanceOfIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _instanceOfIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _s_1 = this.s(Fields.INSTANCEOF_TYPES, "Ljava/lang/Exception");
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertField(_l);
   }
   
   @Test
   public void testTimestampIndexer() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyInstanceOfClass {");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       TimestampIndexer _timestampIndexer = new TimestampIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _timestampIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_CLASS);
       String _timeString = TimestampIndexer.getTimeString();
       String _substring = _timeString.substring(0, 8);
       String _s_1 = this.s(Fields.TIMESTAMP, _substring);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertFieldStartsWith(_l);
   }
   
   @Test
   public void testTimestampIndexer02() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyInstanceOfClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void operation() {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       TimestampIndexer _timestampIndexer = new TimestampIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _timestampIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_METHOD);
       String _timeString = TimestampIndexer.getTimeString();
       String _substring = _timeString.substring(0, 8);
       String _s_1 = this.s(Fields.TIMESTAMP, _substring);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertFieldStartsWith(_l);
   }
   
   @Test
   public void testTimestampIndexer03() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyInstanceOfClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private String s;");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       TimestampIndexer _timestampIndexer = new TimestampIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _timestampIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_FIELD);
       String _timeString = TimestampIndexer.getTimeString();
       String _substring = _timeString.substring(0, 8);
       String _s_1 = this.s(Fields.TIMESTAMP, _substring);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertFieldStartsWith(_l);
   }
   
   @Test
   public void testTimestampIndexer04() {
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("public class MyInstanceOfClass {");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public void operation() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("try {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("catch(Exception ex) {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       final CharSequence code = _builder;
       DocumentTypeIndexer _documentTypeIndexer = new DocumentTypeIndexer();
       TimestampIndexer _timestampIndexer = new TimestampIndexer();
       ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList(_documentTypeIndexer, _timestampIndexer);
       List<IIndexer> _i = this.i(((IIndexer[])Conversions.unwrapArray(_newArrayList, IIndexer.class)));
       this.exercise(code, _i);
       String _s = this.s(Fields.TYPE, Fields.TYPE_TRYCATCH);
       String _timeString = TimestampIndexer.getTimeString();
       String _substring = _timeString.substring(0, 8);
       String _s_1 = this.s(Fields.TIMESTAMP, _substring);
       ArrayList<String> _newArrayList_1 = CollectionLiterals.<String>newArrayList(_s, _s_1);
       List<String> _l = this.l(((String[])Conversions.unwrapArray(_newArrayList_1, String.class)));
       this.assertFieldStartsWith(_l);
   }
 }
