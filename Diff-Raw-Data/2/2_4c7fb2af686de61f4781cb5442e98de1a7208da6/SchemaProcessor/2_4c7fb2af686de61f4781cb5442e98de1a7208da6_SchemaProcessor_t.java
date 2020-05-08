 package togos.schemaschemademo;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import togos.asyncstream.BaseStreamSource;
 import togos.asyncstream.StreamDestination;
 import togos.codeemitter.FileUtil;
 import togos.codeemitter.WordUtil;
 import togos.codeemitter.sql.SQLEmitter;
 import togos.codeemitter.structure.rdb.TableDefinition;
 import togos.function.Function;
 import togos.lang.BaseSourceLocation;
 import togos.lang.CompileError;
 import togos.schemaschema.ComplexType;
 import togos.schemaschema.PropertyUtil;
 import togos.schemaschema.SchemaObject;
 import togos.schemaschema.namespaces.Application;
 import togos.schemaschema.namespaces.Core;
 import togos.schemaschema.namespaces.DataTypeTranslation;
 import togos.schemaschema.namespaces.RDB;
 import togos.schemaschema.namespaces.Types;
 import togos.schemaschema.parser.CommandInterpreters;
 import togos.schemaschema.parser.SchemaInterpreter;
 
 public class SchemaProcessor
 {
 	protected static Function<String,String> PASCAL_CASIFIER = new Function<String,String>() {
 		@Override public String apply(String phrase) throws RuntimeException {
 			return WordUtil.toPascalCase(phrase);
 		}
 	};
 	protected static Function<String,String> POSTGRES_CASIFIER = new Function<String,String>() {
 		@Override public String apply(String phrase) throws RuntimeException {
 			return phrase.toLowerCase().replaceAll("[^a-z0-9]","");
 		}
 	};
 	
 	static class TableClassFilter<E extends Throwable> extends BaseStreamSource<ComplexType, E> implements StreamDestination<SchemaObject, E>
 	{
 		@Override
 		public void data(SchemaObject value) throws E {
 			if( value instanceof ComplexType && PropertyUtil.getFirstInheritedBoolean(value, Application.HAS_DB_TABLE, false) ) {
 				_data((ComplexType)value);
 			}
 		}
 	}
 	
 	static String USAGE_TEXT =
 		"Usage: SchemaProcessor [options] [schema file]\n" +
 		"Options:\n" +
 		"  -o-schema-php <file.php>\n" +
 		"  -php-schema-class-namespace <namespace>\n" +
 		"  -o-db-scripts <dir>\n" +
 		"  -o-create-tables-script <file>\n" +
 		"  -o-drop-tables-script <file>\n" +
 		"  -? or -h ; output help text and exit";
 	
 	public static void main( String[] args ) throws Exception {
 		String sourceFilename = "-";
 		String phpSchemaClassNamespace = "TOGoS_Schema";
 		File outputSchemaPhpFile = null;
 		File outputCreateTablesScriptFile = null;
 		File outputDropTablesScriptFile = null;
 		Function<String,String> tableNamer = POSTGRES_CASIFIER;
 		Function<String,String> columnNamer = POSTGRES_CASIFIER;
 		
 		for( int i=0; i<args.length; ++i ) {
 			if( "-?".equals(args[i]) || "-h".equals(args[i]) || "--help".equals(args[i]) ) {
 				System.out.println(USAGE_TEXT);
 				System.exit(0);
 			} else if( "-o-schema-php".equals(args[i]) ) {
 				outputSchemaPhpFile = new File(args[++i]);
 			} else if( "-php-schema-class-namespace".equals(args[i]) ) {
 				phpSchemaClassNamespace = args[++i];
 			} else if( "-o-db-scripts".equals(args[i]) ) {
 				File dir = new File(args[++i]);
 				outputCreateTablesScriptFile = new File(dir, "create-tables.sql");
				outputDropTablesScriptFile = new File(dir, "drop-tables.sql");
 			} else if( "-o-create-tables-script".equals(args[i]) ) {
 				outputCreateTablesScriptFile = new File(args[++i]);
 			} else if( "-o-drop-tables-script".equals(args[i]) ) {
 				outputDropTablesScriptFile = new File(args[++i]);
 			} else if( !args[i].startsWith("-") ) {
 				sourceFilename = args[i];
 			} else {
 				System.err.println("Unrecognized argument: "+args[i]);
 				System.err.println(USAGE_TEXT);
 				System.exit(1);
 			}
 		}
 		
 		boolean didSomething = false;
 		
 		Reader sourceReader = "-".equals(sourceFilename) ?
 			new InputStreamReader(System.in) :
 			new FileReader(sourceFilename);
 		
 		SchemaInterpreter sp = new SchemaInterpreter();
 		sp.defineImportable( Application.NS );
 		sp.defineImportable( Core.NS );
 		sp.defineImportable( Core.RDF_NS );
 		sp.defineImportable( Core.RDFS_NS );
 		sp.defineImportable( DataTypeTranslation.NS );
 		sp.defineImportable( RDB.NS );
 		sp.defineImportable( Types.NS );
 		
 		sp.defineFieldModifier("key", SchemaInterpreter.FieldIndexModifierSpec.INSTANCE );
 		sp.defineFieldModifier("index", SchemaInterpreter.FieldIndexModifierSpec.INSTANCE );
 		
 		CommandInterpreters.defineTypeDefinitionCommands(sp);
 		CommandInterpreters.defineImportCommand(sp);
 		
 		TableClassFilter<CompileError> tableClassFilter = new TableClassFilter<CompileError>();
 		
 		if( outputCreateTablesScriptFile != null ) {
 			FileUtil.mkParentDirs(outputCreateTablesScriptFile);
 			final FileWriter createTablesWriter = new FileWriter(outputCreateTablesScriptFile);
 			final SQLEmitter createTablesSqlEmitter = new SQLEmitter(createTablesWriter);
 			final TableCreationSQLGenerator tcsg = new TableCreationSQLGenerator(createTablesSqlEmitter, tableNamer, columnNamer);
 			tcsg.pipe(new StreamDestination<TableDefinition, CompileError>() {
 				@Override public void data(TableDefinition td) throws CompileError {
 					try {
 						createTablesSqlEmitter.emitTableCreation(td);
 					} catch( CompileError e ) {
 						throw e;
 					} catch( Exception e ) {
 						throw new CompileError(e, BaseSourceLocation.NONE);
 					}
 				};
 				@Override public void end() throws CompileError {
 					try {
 						createTablesWriter.close();
 					} catch( Exception e ) {
 						throw new CompileError(e, BaseSourceLocation.NONE);
 					}
 				}
 			});
 			tableClassFilter.pipe(tcsg);
 			didSomething = true;
 		}
 
 		if( outputDropTablesScriptFile != null ) {
 			FileUtil.mkParentDirs(outputDropTablesScriptFile);
 			final ArrayList<String> tableList = new ArrayList<String>();
 			final FileWriter dropTablesWriter = new FileWriter(outputDropTablesScriptFile);
 			final SQLEmitter dropTablesSqlEmitter = new SQLEmitter(dropTablesWriter);
 			final TableCreationSQLGenerator tcsg = new TableCreationSQLGenerator(dropTablesSqlEmitter, tableNamer, columnNamer);
 			tcsg.pipe(new StreamDestination<TableDefinition, CompileError>() {
 				@Override public void data(TableDefinition td) throws CompileError {
 					tableList.add(td.name);
 				}
 				@Override public void end() throws CompileError {
 					try {
 						Collections.reverse(tableList);
 						for( String tableName : tableList ) {
 							dropTablesSqlEmitter.emitDropTable(tableName);
 						}
 						dropTablesWriter.close();
 					} catch( CompileError e ) {
 						throw e;
 					} catch( Exception e ) {
 						throw new CompileError(e, BaseSourceLocation.NONE);
 					}
 				}
 			});
 			tableClassFilter.pipe(tcsg);
 			didSomething = true;
 		}
 		
 		if( outputSchemaPhpFile != null ) {
 			FileUtil.mkParentDirs(outputSchemaPhpFile);
 			
 			final FileWriter phpSchemaWriter = new FileWriter(outputSchemaPhpFile);
 			final PHPSchemaDumper psd = new PHPSchemaDumper(phpSchemaWriter, phpSchemaClassNamespace);
 			tableClassFilter.pipe(new StreamDestination<ComplexType, CompileError>() {
 				@Override public void data(ComplexType value) throws CompileError {
 					try {
 						psd.data(value);
 					} catch( CompileError e ) {
 						throw e;
 					} catch( Exception e ) {
 						throw new CompileError(e, BaseSourceLocation.NONE);
 					}
 				};
 				@Override public void end() throws CompileError {
 					try {
 						psd.end();
 						phpSchemaWriter.close();
 					} catch( CompileError e ) {
 						throw e;
 					} catch( Exception e ) {
 						throw new CompileError(e, BaseSourceLocation.NONE);
 					}
 				}
 			});
 			didSomething = true;
 		}
 		
 		sp.pipe( tableClassFilter );
 		
 		sp.parse( sourceReader, sourceFilename );
 		
 		if( !didSomething ) {
 			System.err.println("Warning: Didn't do anything.  Maybe you want to -o-<something>");
 		}
 	}
 }
