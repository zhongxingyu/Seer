 package gov.fda.nctr.xdagen;
 
 import static gov.fda.nctr.util.StringFuns.camelCase;
 import static gov.fda.nctr.util.StringFuns.camelCaseInitialLower;
 import static gov.fda.nctr.util.StringFuns.lc;
 import static gov.fda.nctr.util.StringFuns.stringFrom;
 import static gov.fda.nctr.util.StringFuns.writeStringToFile;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import freemarker.cache.ClassTemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 
 import gov.fda.nctr.dbmd.DBMD;
 import gov.fda.nctr.dbmd.ForeignKey;
 import gov.fda.nctr.dbmd.RelId;
 import gov.fda.nctr.dbmd.RelMetaData;
 import gov.fda.nctr.dbmd.DBMD.ForeignKeyScope;
 import gov.fda.nctr.util.StringFuns;
 
 
 public class TypedTableOutputSpecSourcesGenerator {
 
 	protected DBMD dbmd;
 	
 	protected String targetPackage; // Target package for generated classes.
 	
 	protected TypedTableOutputSpecNamer typedTableOutputSpecNamer; // Controls naming of the generated classes and their parent/child addition methods.
 	
 	protected File outputDir;
 	
	protected String defaultTableOutputSpecFactoryExpr; // For use by secondary constructor in which client doesn't supply a TableOutputSpec.Factory instance.
	
 	protected Configuration templateConfig;
 	protected Template classSourceFileTemplate;
 	protected Template prototypesSourceFileTemplate;
 	
 	protected static final String CLASSPATH_TEMPLATES_DIR_PATH = "/templates";
 	protected static final String JAVA_SOURCE_FILE_TEMPLATE =  "TypedTableOutputSpecJavaSource.ftl";
 	protected static final String PROTOTYPES_SOURCE_FILE_TEMPLATE =  "AllTypedTableOutputSpecPrototypesJavaSource.ftl";
 
 	// Primary constructor.
 	public TypedTableOutputSpecSourcesGenerator(DBMD dbmd,
 	                                            String target_java_package,
 	                                            String default_tableoutputspec_factory_expr, // optional
 	                                            TypedTableOutputSpecNamer typedtos_namer)  // optional
 		throws IOException
 	{
 		this.dbmd = dbmd;
 		this.targetPackage = target_java_package;
		this.defaultTableOutputSpecFactoryExpr = default_tableoutputspec_factory_expr == null ?
				"new gov.fda.nctr.xdagen.DefaultTableOutputSpecFactory(dbmd, gov.fda.nctr.xdagen.XmlElementCollectionStyle.INLINE)"
			  : default_tableoutputspec_factory_expr;
 		this.typedTableOutputSpecNamer = typedtos_namer != null ? typedtos_namer : new DefaultTypedTableOutputSpecNamer(dbmd);
 		
 		// Configure template engine.
 		this.templateConfig = new Configuration();
 		this.templateConfig.setTemplateLoader(new ClassTemplateLoader(getClass(), CLASSPATH_TEMPLATES_DIR_PATH));
 		this.templateConfig.setObjectWrapper(new DefaultObjectWrapper());
 		
 		// Load templates.
 		this.classSourceFileTemplate = templateConfig.getTemplate(JAVA_SOURCE_FILE_TEMPLATE);
 		this.prototypesSourceFileTemplate = templateConfig.getTemplate(PROTOTYPES_SOURCE_FILE_TEMPLATE);
 	}
 	
 	
 	public TypedTableOutputSpecSourcesGenerator(DBMD dbmd,
 	                                            String target_java_package)
 		throws IOException
 	{
 		this(dbmd,
 		     target_java_package,
 		     null,
 		     new DefaultTypedTableOutputSpecNamer(dbmd));
 	}
 	
 	
 	public DBMD getDatabaseMetaData()
 	{
 		return dbmd;
 	}
 	
 	public String getTargetPackage()
 	{
 		return targetPackage;
 	}
 
 	public TypedTableOutputSpecNamer getTypedTableOutputSpecNamer()
 	{
 		return typedTableOutputSpecNamer;
 	}
 	
 	public void setTypedTableOutputSpecNamer(TypedTableOutputSpecNamer typedTableOutputSpecNamer)
 	{
 		this.typedTableOutputSpecNamer = typedTableOutputSpecNamer;
 	}
 	
	/* Setting this field allows controlling how a TableOutputSpec.Factory is supplied in the generated classes for cases that an explicit
	 * factory is not provided by their clients. The factory may include references to a DBMD value named "dbmd".  The default is:
	 *   "new gov.fda.nctr.xdagen.DefaultTableOutputSpecFactory(dbmd, gov.fda.nctr.xdagen.XmlElementCollectionStyle.INLINE)"
	 */
	public String getDefaultTableOutputSpecFactoryExpression()
	{
		return defaultTableOutputSpecFactoryExpr;
	}
	
	public void setDefaultTableOutputSpecFactoryExpression(String fe)
	{
		this.defaultTableOutputSpecFactoryExpr = fe;
	}
	
 	public void setOutputDirectory(File output_dir)
 	{
 		this.outputDir = output_dir;
 	}
 	
 	public File getOutputDirectory()
 	{
 		return outputDir;
 	}
 	
 	public String getJavaSource(RelId relid)
 	{
 		Map<String,Object> template_model = new HashMap<String,Object>();
 		
 		template_model.put("target_package", targetPackage);
 		template_model.put("namer", typedTableOutputSpecNamer);
		template_model.put("default_tableoutputspec_factory_expr", defaultTableOutputSpecFactoryExpr);
 		template_model.put("relid", relid);
 		template_model.put("fks_from_child_tables", dbmd.getForeignKeysFromTo(null, relid, DBMD.ForeignKeyScope.REGISTERED_TABLES_ONLY));
 		template_model.put("fks_to_parent_tables",  dbmd.getForeignKeysFromTo(relid, null, DBMD.ForeignKeyScope.REGISTERED_TABLES_ONLY));
 		
 		try
 		{
 			Writer sw = new StringWriter();
 			
 			classSourceFileTemplate.process(template_model, sw);
 		
 			return sw.toString();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new RuntimeException("Failed to produce typed table output spec source file from template: " + e.getMessage());
 		}	
 	}
 	
 	public String getPrototypesJavaSource()
 	{
 		Map<String,Object> template_model = new HashMap<String,Object>();
 		
 		template_model.put("target_package", targetPackage);
 		template_model.put("namer", typedTableOutputSpecNamer);
 		template_model.put("relids", dbmd.getRelationIds());
 		
 		try
 		{
 			Writer sw = new StringWriter();
 			
 			prototypesSourceFileTemplate.process(template_model, sw);
 		
 			return sw.toString();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new RuntimeException("Failed to produce typed table output spec source file from template: " + e.getMessage());
 		}	
 	}
 	
 	public void writeSourceFilesToOutputDir() throws IOException
 	{
 		writeSourceFilesTo(outputDir);
 	}
 	
 	
 	public void writeSourceFilesTo(File output_root_dir) throws IOException
 	{
 		if ( output_root_dir == null || !output_root_dir.isDirectory() )
 			throw new IllegalArgumentException("Expected output directory, got <" + output_root_dir + ">.");
 		
 		String package_as_path = targetPackage.replace('.', File.separatorChar);
 
 		File output_leaf_dir = new File(output_root_dir, package_as_path);
 		output_leaf_dir.mkdirs();
 		
 		for(RelMetaData rmd: dbmd.getRelationMetaDatas())
 		{
 			RelId relid = rmd.getRelationId();
 			
 			String java_src = getJavaSource(relid);
 			
 			String file_name = typedTableOutputSpecNamer.getTypedTableOutputSpecClassName(relid) + ".java";
 			
 			StringFuns.writeStringToFile(java_src,
 			                             new File(output_leaf_dir, file_name));
 		}
 		
 		writeStringToFile(getPrototypesJavaSource(),
 						  new File(output_leaf_dir, typedTableOutputSpecNamer.getPrototypesClassName() + ".java"));
 	}
 	
 	
 	///////////////////////////////////////////////////////////////
 	// Naming interface and default implementation.
 	
 	public static interface TypedTableOutputSpecNamer {
 		
 		public String getTypedTableOutputSpecClassName(RelId relid);
 		
 		public String getChildAdditionMethodName(ForeignKey fk_from_child);
 		
 		public String getParentAdditionMethodName(ForeignKey fk_to_parent);
 		
 		public String getPrototypesClassName();
 		
 		public String getPrototypeMemberName(RelId relid);
 		
 	}
 	
 	public static class DefaultTypedTableOutputSpecNamer implements TypedTableOutputSpecNamer {
 		
 		DBMD dbmd;
 		
 		public DefaultTypedTableOutputSpecNamer(DBMD dbmd)
 		{
 			this.dbmd = dbmd;
 		}
 		
 		@Override
 		public String getTypedTableOutputSpecClassName(RelId relid)
 		{
 			return camelCase(relid.getName()) + "TableOutputSpec";
 		}
 
 		@Override
 		public String getChildAdditionMethodName(ForeignKey fk_from_child)
 		{
 			List<ForeignKey> fks = dbmd.getForeignKeysFromTo(fk_from_child.getSourceRelationId(),
 			                                                 fk_from_child.getTargetRelationId(),
 			                                                 ForeignKeyScope.REGISTERED_TABLES_ONLY);
 			
 			RelId rel_to_add = fk_from_child.getSourceRelationId();
 			
 			if ( fks.size() <= 1 ) // Only one fk from this child to this parent exists: use simple name.
 			{
 				return getSimpleChildAdditionMethodName(rel_to_add);
 			}
 			else // Multiple fks from this child to this parent exist: use properly qualified name to avoid name collision.
 			{
 				return getSimpleChildAdditionMethodName(rel_to_add) +
 				       "ReferencingVia" + camelCase(stringFrom(lc(fk_from_child.getSourceFieldNames()),"_and_"));
 			}
 		}
 		
 		@Override
 		public String getParentAdditionMethodName(ForeignKey fk_to_parent)
 		{
 			List<ForeignKey> fks = dbmd.getForeignKeysFromTo(fk_to_parent.getSourceRelationId(),
 			                                                 fk_to_parent.getTargetRelationId(),
 			                                                 ForeignKeyScope.REGISTERED_TABLES_ONLY);
 			
 			RelId rel_to_add = fk_to_parent.getTargetRelationId();
 			
 			if ( fks.size() <= 1 ) // Only one fk from this child to this parent exists: use simple name.
 			{
 				return getSimpleParentAdditionMethodName(rel_to_add);
 			}
 			else // Multiple fks from this child to this parent exist: use properly qualified name to avoid name collision.
 			{
 				return getSimpleParentAdditionMethodName(rel_to_add) +
 				       "ReferencedVia" + camelCase(stringFrom(lc(fk_to_parent.getSourceFieldNames()),"_and_"));
 			}
 		}
 		
 		@Override
 		public String getPrototypesClassName()
 		{
 			return "Prototypes";
 		}
 		
 		@Override
 		public String getPrototypeMemberName(RelId relid)
 		{
 			return camelCaseInitialLower(relid.getName());
 		}
 
 
 		protected String getSimpleChildAdditionMethodName(RelId child_relid)
 		{
 			return "with" + camelCase(child_relid.getName()) + "List";
 		}
 		
 		protected String getSimpleParentAdditionMethodName(RelId parent_relid)
 		{
 			return "with" + camelCase(parent_relid.getName());
 		}
 	}
 	
 	// Naming interface and default implementation.
 	///////////////////////////////////////////////////////////////
 	
 	
 	
     public static void main(String[] args) throws Exception
     {
         int arg_ix = 0;
         String dbmd_xml_infile_path;
         String target_java_package;
         File output_dir;
     	
         if ( args.length == 3 )
         {
         	dbmd_xml_infile_path = args[arg_ix++];
         	target_java_package = args[arg_ix++];
         	output_dir = new File(args[arg_ix++]);
         	if ( !output_dir.isDirectory() )
         		throw new IllegalArgumentException("Output directory not found.");
         }
         else
         	throw new IllegalArgumentException("Expected arguments: <db-metadata-file> <target-java-package> <output-dir>");
 
         InputStream dbmd_is = new FileInputStream(dbmd_xml_infile_path);
         
         DBMD dbmd = DBMD.readXML(dbmd_is);
         dbmd_is.close();
         
         TypedTableOutputSpecSourcesGenerator g = new TypedTableOutputSpecSourcesGenerator(dbmd,
                                                                                           target_java_package);
         
         g.writeSourceFilesTo(output_dir);
         
         System.exit(0);
     }
 
 }
