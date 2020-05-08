 package org.iti.sqlSchemaComparison.frontends.technologies;
 
 import japa.parser.JavaParser;
 import japa.parser.ParseException;
 import japa.parser.ast.CompilationUnit;
 import japa.parser.ast.body.ClassOrInterfaceDeclaration;
 import japa.parser.ast.body.MethodDeclaration;
 import japa.parser.ast.expr.AnnotationExpr;
 import japa.parser.ast.expr.MemberValuePair;
 import japa.parser.ast.expr.NormalAnnotationExpr;
 import japa.parser.ast.type.ClassOrInterfaceType;
 import japa.parser.ast.visitor.VoidVisitorAdapter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iti.sqlSchemaComparison.edge.ForeignKeyRelationEdge;
 import org.iti.sqlSchemaComparison.edge.TableHasColumnEdge;
 import org.iti.sqlSchemaComparison.frontends.ISqlSchemaFrontend;
 import org.iti.sqlSchemaComparison.vertex.ISqlElement;
 import org.iti.sqlSchemaComparison.vertex.SqlColumnVertex;
 import org.iti.sqlSchemaComparison.vertex.SqlElementFactory;
 import org.iti.sqlSchemaComparison.vertex.SqlElementType;
 import org.iti.sqlSchemaComparison.vertex.sqlColumn.IColumnConstraint;
 import org.iti.sqlSchemaComparison.vertex.sqlColumn.PrimaryKeyColumnConstraint;
 import org.jgrapht.Graph;
 import org.jgrapht.graph.DefaultEdge;
 import org.jgrapht.graph.SimpleGraph;
 
 public class JPASchemaFrontend implements ISqlSchemaFrontend {
 
 	private final static String GETTER_PREFIX = "get";
 	private final static String ENTITY = "Entity";
 	private final static String TABLE = "Table";
 	private final static String COLUMN = "Column";
 	private final static String TABLE_NAME = "name";
 	
 	private String filePath;
 	
 	private static class JPAAnnotationVisitor extends VoidVisitorAdapter<Graph<ISqlElement, DefaultEdge>> {
 
 		public Map<String, String> classToTable = new HashMap<>();
 		public Map<String, ClassOrInterfaceDeclaration> classDeclarations = new HashMap<>();
 		
 		private final static String TRANSIENT = "Transient";
 		private final static String ID = "Id";
 		
 		private Graph<ISqlElement, DefaultEdge> schema;
 
 		private ISqlElement lastVisitedClass;
 		
 		public JPAAnnotationVisitor(Graph<ISqlElement, DefaultEdge> schema) {
 			this.schema = schema;
 		}
 		
 		@Override
 		public void visit(ClassOrInterfaceDeclaration n, Graph<ISqlElement, DefaultEdge> arg) {
 
 			if (n.getAnnotations() != null && isAnnotationAvailable(n.getAnnotations(), ENTITY)) {
 				processClass(n);
 			}
 			
 			super.visit(n, arg);
 		}
 
 		private void processClass(ClassOrInterfaceDeclaration n) {
 			String tableName = getTableName(n);
 			ISqlElement table = SqlElementFactory.createSqlElement(SqlElementType.Table, tableName);
 			schema.addVertex(table);
 			
 			lastVisitedClass = table;
 			
 			classToTable.put(n.getName().toString(), tableName);
 			classDeclarations.put(n.getName(), n);
 		}
 
 		@Override
 		public void visit(MethodDeclaration n, Graph<ISqlElement, DefaultEdge> arg) {
 
 			if (isGetter(n) && (n.getAnnotations() == null || !isAnnotationAvailable(n.getAnnotations(), TRANSIENT))) {
 				processMethod(n);
 			}
 			
 			super.visit(n, arg);
 		}
 
 		private boolean isGetter(MethodDeclaration n) {
 			return n.getName().startsWith(GETTER_PREFIX);
 		}
 
 		private void processMethod(MethodDeclaration n) {
 			String id = getColumnName(n);
 			String type = "?";
 			List<IColumnConstraint> constraints = new ArrayList<>();
 			
 			ISqlElement column = new SqlColumnVertex(id, type, lastVisitedClass.getSqlElementId());
 			
 			((SqlColumnVertex) column).setConstraints(constraints);
 			
 			if (isAnnotationAvailable(n.getAnnotations(), ID)) {
 				PrimaryKeyColumnConstraint constraint = new PrimaryKeyColumnConstraint("", column);
 				
 				constraints.add(constraint);
 			}
 			
 			schema.addVertex(column);
 			schema.addEdge(lastVisitedClass, column, new TableHasColumnEdge(lastVisitedClass, column));
 		}
 		
 	}
 	
 	private static class PrimaryKeyVisitor extends VoidVisitorAdapter<Graph<ISqlElement, DefaultEdge>> {
 
 		private Graph<ISqlElement, DefaultEdge> schema;
 		
 		private Map<String, String> classToTable;
 		
 		private Map<String, ClassOrInterfaceDeclaration> classDeclarations = new HashMap<>();
 		
 		public PrimaryKeyVisitor(Graph<ISqlElement, DefaultEdge> schema,
 				Map<String, String> classToTable,
 				Map<String, ClassOrInterfaceDeclaration> classDeclarations) {
 			this.schema = schema;
 			this.classToTable = classToTable;
 			this.classDeclarations = classDeclarations;
 		}
 		
 		@Override
 		public void visit(ClassOrInterfaceDeclaration n, Graph<ISqlElement, DefaultEdge> arg) {
 
 			if (n.getAnnotations() != null && isAnnotationAvailable(n.getAnnotations(), ENTITY)) {
 				processClass(n);
 			}
 			
 			super.visit(n, arg);
 		}
 
 		private void processClass(ClassOrInterfaceDeclaration n) {
 			ISqlElement primaryKeyColumn = getPrimaryKeyOfType(n);
 			
 			if (primaryKeyColumn == null) {
 				primaryKeyColumn = getPrimaryKeyOfSupertypes(n.getExtends());
 				
 				setPrimaryKeyOfTable(n, primaryKeyColumn);
 			}
 		}
 
 		private void setPrimaryKeyOfTable(ClassOrInterfaceDeclaration n, ISqlElement primaryKeyColumn) {
 			if (primaryKeyColumn != null && primaryKeyColumn instanceof SqlColumnVertex) {
 				String tableId = classToTable.get(n.getName().toString());
 				ISqlElement table = SqlElementFactory.getMatchingSqlElement(SqlElementType.Table, tableId, schema.vertexSet());
 				SqlColumnVertex foreignKeyColumn = (SqlColumnVertex)primaryKeyColumn;
 				ISqlElement foreignKeyTable = SqlElementFactory.getMatchingSqlElement(SqlElementType.Table, foreignKeyColumn.getTable(), schema.vertexSet());
 				String id = foreignKeyColumn.getSqlElementId();
 				String type = foreignKeyColumn.getType();
 				List<IColumnConstraint> constraints = new ArrayList<>();
 				
 				ISqlElement column = new SqlColumnVertex(id, type, table.getSqlElementId());
 				
 				((SqlColumnVertex) column).setConstraints(constraints);
 				
 				PrimaryKeyColumnConstraint constraint = new PrimaryKeyColumnConstraint("", column);
 				
 				constraints.add(constraint);
 				
 				schema.addVertex(column);
 				schema.addEdge(table, column, new TableHasColumnEdge(table, column));
 				schema.addEdge(table, column, new ForeignKeyRelationEdge(column, foreignKeyTable, foreignKeyColumn));
 			}			
 		}
 
 		private ISqlElement getPrimaryKeyOfSupertypes(
 				List<ClassOrInterfaceType> supertypes) {
 			
 			ISqlElement primaryKeyColumn = null;
 			
 			for (ClassOrInterfaceType supertype : supertypes) {
 				ClassOrInterfaceDeclaration superclass = classDeclarations.get(supertype.getName());
 				
 				if (superclass != null) {
 					primaryKeyColumn = getPrimaryKeyOfType(superclass);
 				
 					if (primaryKeyColumn != null)
 						break;
 					
 					primaryKeyColumn = getPrimaryKeyOfSupertypes(superclass.getExtends());
 				}
 			}
 			
 			return primaryKeyColumn;
 		}
 
 		private ISqlElement getPrimaryKeyOfType(ClassOrInterfaceDeclaration type) {
 			String tableId = classToTable.get(type.getName().toString());
 			ISqlElement table = SqlElementFactory.getMatchingSqlElement(SqlElementType.Table, tableId, schema.vertexSet());
 			
 			return SqlElementFactory.getPrimaryKey(table, schema);
 		}
 		
 	}
 	
 	private static class ForeignKeyVisitor extends VoidVisitorAdapter<Graph<ISqlElement, DefaultEdge>> {
 
 		private final static String[] RELATIONSHIP_ANNOTATIONS = new String[]
 				{
 					"@ManyToMany",
 					"ManyToOne",
 					"OneToMany",
 					"OneToOne"
 				};
 		
 		private Graph<ISqlElement, DefaultEdge> schema;
 		
 		private Map<String, String> classToTable = new HashMap<>();
 
 		private ISqlElement lastVisitedClass;
 		
 		public ForeignKeyVisitor(Graph<ISqlElement, DefaultEdge> schema, Map<String, String> classToTable) {
 			this.schema = schema;
 			this.classToTable = classToTable;
 		}
 		
 		@Override
 		public void visit(ClassOrInterfaceDeclaration n, Graph<ISqlElement, DefaultEdge> arg) {
 
 			if (n.getAnnotations() != null && isAnnotationAvailable(n.getAnnotations(), ENTITY)) {
 				processClass(n);
 			}
 			
 			super.visit(n, arg);
 		}
 
 		private void processClass(ClassOrInterfaceDeclaration n) {
 			String id = getTableName(n);
 			
 			lastVisitedClass = SqlElementFactory.getMatchingSqlElement(SqlElementType.Table, id, schema.vertexSet());
 		}
 
 		@Override
 		public void visit(MethodDeclaration n, Graph<ISqlElement, DefaultEdge> arg) {
 
 			if (isGetter(n) && (n.getAnnotations() != null && isAnnotationAvailable(n.getAnnotations(), RELATIONSHIP_ANNOTATIONS))) {
 				processMethod(n);
 			}
 			
 			super.visit(n, arg);
 		}
 
 		private boolean isGetter(MethodDeclaration n) {
 			return n.getName().startsWith(GETTER_PREFIX);
 		}
 
 		private void processMethod(MethodDeclaration n) {
 			String columnId = lastVisitedClass.getSqlElementId() + "." + getColumnName(n);
 			String foreignTableId = classToTable.get(n.getType().toString());
 			ISqlElement foreignKeyTable = SqlElementFactory.getMatchingSqlElement(SqlElementType.Table, foreignTableId, schema.vertexSet());
 			ISqlElement referencingColumn = SqlElementFactory.getMatchingSqlColumns(columnId, schema.vertexSet(), true).get(0);
 			ISqlElement foreignKeyColumn = SqlElementFactory.getPrimaryKey(foreignKeyTable, schema);
 			
 			if (referencingColumn != null && foreignKeyColumn != null) {
 				schema.addEdge(referencingColumn, foreignKeyColumn, new ForeignKeyRelationEdge(referencingColumn, foreignKeyTable, foreignKeyColumn));
 			}
 		}
 		
 	}
 
 	private static boolean isAnnotationAvailable(
 			List<AnnotationExpr> annotations, String annotation) {
 
 		return getAnnotation(annotations, annotation) != null;
 	}
 
 	private static boolean isAnnotationAvailable(
 			List<AnnotationExpr> annotations, String[] relationshipAnnotations) {
 
 		for (AnnotationExpr annotation : annotations)
 			if (Arrays.asList(relationshipAnnotations).contains(annotation.getName().toString()))
 				return true;
 		
 		return false;
 	}	
 
 	private static String getTableName(ClassOrInterfaceDeclaration n) {
 		AnnotationExpr tableAnnotation = getAnnotation(n.getAnnotations(), TABLE);
 		
 		if (tableAnnotation != null && tableAnnotation instanceof NormalAnnotationExpr) {
 			NormalAnnotationExpr a = (NormalAnnotationExpr)tableAnnotation;
 			
 			for (MemberValuePair p : a.getPairs()) {
 				if (p.getName().equals(TABLE_NAME))
					return p.getValue().toString().replace("\"", "");
 			}
 		}
 		
 		return n.getName();
 	}
 
 	private static String getColumnName(MethodDeclaration n) {
 		return getColumnName(n, COLUMN);
 	}
 
 	private static String getColumnName(MethodDeclaration n, String annotation) {
 		AnnotationExpr tableAnnotation = getAnnotation(n.getAnnotations(), annotation);
 		
 		if (tableAnnotation != null && tableAnnotation instanceof NormalAnnotationExpr) {
 			NormalAnnotationExpr a = (NormalAnnotationExpr)tableAnnotation;
 			
 			for (MemberValuePair p : a.getPairs()) {
 				if (p.getName().equals(TABLE_NAME))
 					return p.getValue().toString();
 			}
 		}
 		
 		return n.getName().substring(GETTER_PREFIX.length(), n.getName().length()).toLowerCase();
 	}
 	
 	private static AnnotationExpr getAnnotation(List<AnnotationExpr> annotations, String annotation) {
 		if (annotations != null) {
 			for (AnnotationExpr expr : annotations) {
 				if (expr.getName().toString().equals(annotation))
 					return expr;
 			}
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Graph<ISqlElement, DefaultEdge> createSqlSchema() {
 		Graph<ISqlElement, DefaultEdge> schema = null;
 		
 		try {
 			schema = tryCreateSqlSchema();
 		} catch (Exception ex) {
 			ex.printStackTrace(System.err);
 		}
 		return schema;
 	}
 	
 	private static FilenameFilter javaFilenameFilter = new FilenameFilter() {
 		
 		@Override
 		public boolean accept(File arg0, String arg1) {
 			return arg1.endsWith(".java");
 		}
 	};
 
 	private Graph<ISqlElement, DefaultEdge> tryCreateSqlSchema() throws ParseException, IOException {
 		Graph<ISqlElement, DefaultEdge> schema = new SimpleGraph<ISqlElement, DefaultEdge>(DefaultEdge.class);
 		File file = new File(filePath);
 		List<CompilationUnit> cus = new ArrayList<>();
 		Map<String, String> classToTable = new HashMap<>();
 		Map<String, ClassOrInterfaceDeclaration> classDeclarations = new HashMap<>();
 		
 		if (file.isDirectory()) {			
 			for (String f : file.list(javaFilenameFilter)) {
 				File child = new File(file, f);
 				
 				CompilationUnit cu = getCompilationUnit(child.getAbsolutePath());
 				
 				if (cu != null)
 					cus.add(cu);
 			}
 		} else {
 			CompilationUnit cu = getCompilationUnit(filePath);
 			
 			if (cu != null)
 				cus.add(cu);
 		}
 		
 		for (CompilationUnit c : cus) {
 			parseJavaCompilationUnit(c, schema, classToTable, classDeclarations);
 		}
 		
 		for (CompilationUnit c : cus) {
 			crateForeignKeyPrimaryRelationships(c, schema, classToTable, classDeclarations);
 		}
 		
 		for (CompilationUnit c : cus) {
 			createForeignKeyRelationships(c, schema, classToTable);
 		}
 
         return schema;
 	}
 
 	private CompilationUnit getCompilationUnit(String filePath)
 			throws FileNotFoundException, ParseException, IOException {
 		FileInputStream in = new FileInputStream(filePath);
 
         CompilationUnit cu;
         
         try {
             // parse the file
             cu = JavaParser.parse(in);
         } finally {
             in.close();
         }
         
         return cu;
 	}
 
 	private void parseJavaCompilationUnit(CompilationUnit cu, 
 			Graph<ISqlElement, DefaultEdge> schema, 
 			Map<String, String> classToTable, 
 			Map<String, ClassOrInterfaceDeclaration> classDeclarations) {
 		JPAAnnotationVisitor visitor = new JPAAnnotationVisitor(schema);
 		visitor.visit(cu, null);
 		
 		classToTable.putAll(visitor.classToTable);
 		classDeclarations.putAll(visitor.classDeclarations);
 	}
 
 	private void crateForeignKeyPrimaryRelationships(CompilationUnit cu,
 			Graph<ISqlElement, DefaultEdge> schema,
 			Map<String, String> classToTable,
 			Map<String, ClassOrInterfaceDeclaration> classDeclarations) {
 		PrimaryKeyVisitor visitor = new PrimaryKeyVisitor(schema, classToTable, classDeclarations);
 		visitor.visit(cu, null);
 	}
 	
 	private void createForeignKeyRelationships(CompilationUnit cu, 
 			Graph<ISqlElement, DefaultEdge> schema, 
 			Map<String, String> classToTable) {
 		
 		ForeignKeyVisitor visitor = new ForeignKeyVisitor(schema, classToTable);
 		visitor.visit(cu, null);
 	}
 
 	public JPASchemaFrontend(String filePath) {
 		if (filePath == null || filePath == "")
 			throw new NullPointerException("Path to JPA file(s) must not be null or empty!");
 		
 		this.filePath = filePath;
 	}
 	
 }
