 package togos.schemaschema.parser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import togos.lang.ParseError;
 import togos.schemaschema.FieldSpec;
 import togos.schemaschema.ForeignKeySpec;
 import togos.schemaschema.IndexSpec;
 import togos.schemaschema.ObjectType;
 import togos.schemaschema.Type;
 import togos.schemaschema.Types;
 import togos.schemaschema.parser.ast.Block;
 import togos.schemaschema.parser.ast.Command;
 import togos.schemaschema.parser.ast.Parameterized;
 import togos.schemaschema.parser.asyncstream.BaseStreamSource;
 import togos.schemaschema.parser.asyncstream.StreamDestination;
 
 public class SchemaParser<V extends ObjectType> extends BaseStreamSource<V> implements StreamDestination<Command>
 {
 	protected static String singleString( Parameterized p, String contextDescription ) throws ParseError {
 		if( p.parameters.length != 0 ) {
 			throw new ParseError( contextDescription + " cannot take arguments", p.sLoc );
 		}
 		return p.subject.unquotedText();
 	}
 	
 	protected Map<String,Type> types = new HashMap<String,Type>();
 	
 	protected V process( ObjectType c ) {
 		return (V)c;
 	}
 	
 	static Pattern KEY_COMPONENT_MOD_PATTERN = Pattern.compile("(.*) (key|index) component");
 	
 	protected FieldSpec defineSimpleField(
 		Command fieldCommand,
 		LinkedHashMap<String,FieldSpec> fieldSpecs,
 		LinkedHashMap<String,IndexSpec> indexSpecs,
 		LinkedHashMap<String,ForeignKeySpec> fkSpecs
 	) throws ParseError {
 		String fieldName = singleString(fieldCommand.subject, "field name");
 		if( fieldSpecs.containsKey(fieldName) ) {
 			throw new ParseError( "Field '"+fieldName+"' already defined", fieldCommand.sLoc );
 		}
 		
 		Type type = null;
 		boolean isNullable = false;
 		LinkedHashSet<String> indexNames = new LinkedHashSet();
 		for( Parameterized modifier : fieldCommand.modifiers ) {
 			String modText = modifier.subject.unquotedText();
 			Matcher m;
 			if( "nullable".equals(modText) ) {
 				isNullable = true;
 			} else if( types.containsKey(modText) ) {
 				if( type != null ) {
 					throw new ParseError(
 						"Cannot redefine type of '"+fieldName+"' from '"+type.getName()+"' to '"+modText,
 						modifier.sLoc
 					);
 				}
 				type = types.get(modText);
			} else if( (m = KEY_COMPONENT_MOD_PATTERN.matcher(modText)).matches() ) {
 				indexNames.add( m.group(1) );
 			} else {
 				throw new ParseError(
 					"Unrecognised field modifier: '"+modText+"'", modifier.sLoc
 				);
 			}
 		}
 		
 		FieldSpec fieldSpec = new FieldSpec( fieldName, type, isNullable );
 		for( String indexName : indexNames ) {
 			IndexSpec index = indexSpecs.get(indexName);
 			if( index == null ) {
 				index = new IndexSpec(indexName);
 				indexSpecs.put(indexName, index);
 			}
 			index.fields.put( fieldName, fieldSpec );
 		}
 		
 		fieldSpecs.put( fieldSpec.name, fieldSpec );
 		return fieldSpec;
 	}
 	
 	protected FieldSpec getSimpleField(
 		Command fieldCommand,
 		LinkedHashMap<String,FieldSpec> fieldSpecs,
 		LinkedHashMap<String,IndexSpec> indexSpecs,
 		LinkedHashMap<String,ForeignKeySpec> fkSpecs
 	) throws ParseError {
 		String fieldName = singleString(fieldCommand.subject, "field name");
 		FieldSpec fieldSpec = fieldSpecs.get(fieldName);
 		if( fieldSpec == null ) {
 			fieldSpec = defineSimpleField(fieldCommand, fieldSpecs, indexSpecs, fkSpecs);
 		} else if( fieldCommand.modifiers.length > 0 ) {
 			throw new ParseError( "Cannot redefine field '"+fieldName+"'", fieldCommand.sLoc );
 		}
 		return fieldSpec;
 	}
 	
 	public ObjectType parseClass( String name, Parameterized[] modifiers, Block body ) throws ParseError {
 		LinkedHashMap<String,FieldSpec> fieldSpecs = new LinkedHashMap<String,FieldSpec>();
 		LinkedHashMap<String,IndexSpec> indexSpecs = new LinkedHashMap<String,IndexSpec>();
 		LinkedHashMap<String,ForeignKeySpec> fkSpecs = new LinkedHashMap<String,ForeignKeySpec>();
 		
 		for( Command fieldCommand : body.commands ) {
 			for( Parameterized fieldNameParameter : fieldCommand.subject.parameters ) {
 				throw new ParseError("Field name cannot have parameters", fieldNameParameter.sLoc );
 			}
 			Block referenceBody = null;
 			for( Parameterized mod : fieldCommand.modifiers ) {
 				if( "reference".equals(mod.subject.unquotedText()) ) {
 					if( mod.parameters.length != 1 ) {
 						throw new ParseError(
 							"'reference' field modifier takes a single parameter: "+
 							"the name of the type being referenced.  Got "+
 							mod.parameters.length+" parameters", mod.sLoc
 						);
 					}
 					if( fieldCommand.body == null ) {
 						throw new ParseError(
 							"'reference' field specification requires a block", fieldCommand.sLoc
 						);
 					}
 					if( fieldCommand.body.commands.length == 0 ) {
 						throw new ParseError(
 							"'reference' field specificatino requires at least one foreign key component",
 							fieldCommand.body.sLoc
 						);
 					}
 					referenceBody = fieldCommand.body;
 				}
 			}
 			if( referenceBody != null ) {
 				ArrayList<ForeignKeySpec.Component> fkComponents = new ArrayList<ForeignKeySpec.Component>();
 				for( Command fkCommand : referenceBody.commands ) {
 					String foreignFieldName = singleString( fkCommand.subject, "foreign field name" );
 					
 					Command localFieldNode;
 					if( fkCommand.body != null ) {
 						if( fkCommand.body.commands.length != 1 ) {
 							throw new ParseError(
 								"Foreign key component requires exactly 0 or 1 local field specifications; given "+
 								fkCommand.body.commands.length, fkCommand.body.sLoc
 							);
 						}
 						if( fkCommand.modifiers.length != 0 ){
 							throw new ParseError(
 								"Modifiers not allowed for foreign field specification",
 								fkCommand.modifiers[0].sLoc
 							);
 						}
 						localFieldNode = fkCommand.body.commands[0];
 					} else {
 						localFieldNode = fkCommand;
 					}
 					
 					String localFieldName = singleString(localFieldNode.subject, "local field name");
 					FieldSpec localField = getSimpleField( localFieldNode, fieldSpecs, indexSpecs, fkSpecs );;
 
 					
 					// TODO: Implement rest of this
 				}
 				//fkSpec
 				//fieldType = new ForeignKeyReferenceType( singleString(mod.parameters[0], "referenced class name"), Types.REFERENCE, fkSpec);
 			} else {
 				defineSimpleField( fieldCommand, fieldSpecs, indexSpecs, fkSpecs );
 			}
 		}
 		
 		boolean selfKeyed = false;
 		for( Parameterized mod : modifiers ) {
 			if( "self-keyed".equals(mod.subject.unquotedText()) ) {
 				selfKeyed = true;
 			} else {
 				throw new ParseError("Unrecognised class modifier: '"+mod.subject+"'", mod.sLoc);
 			}
 		}
 		
 		if( selfKeyed ) {
 			indexSpecs.put("primary", new IndexSpec("primary", fieldSpecs));
 		}
 		
 		return new ObjectType( name, Types.OBJECT, fieldSpecs, indexSpecs, fkSpecs );
 	}
 	
 	@Override public void data(Command value) throws Exception {
 		String cmd = value.subject.subject.words[0].text;
 		if( "class".equals(cmd) ) {
 			for( Parameterized classNameParameter : value.subject.parameters ) {
 				throw new ParseError("Class name cannot have parameters", classNameParameter.sLoc );
 			}
 			_data( process(parseClass( value.subject.subject.tail().unquotedText(), value.modifiers, value.body )) );
 		} else {
 			throw new ParseError("Unrecognised command: '"+cmd+"'", value.sLoc);
 		}
 	}
 
 	@Override public void end() throws Exception {
 		_end();
 	}
 	
 	public Map<String,V> parse( String source ) throws ParseError {
 		final Map<String,V> types = new LinkedHashMap<String,V>();
 		
 		Tokenizer t = new Tokenizer();
 		Parser p = new Parser();
 		this.pipe(new StreamDestination<V>() {
 			@Override public void data(V t) {
 				types.put( t.name, t );
 			}
 			@Override public void end() { } 
 		});
 		p.pipe(this);
 		t.pipe(p);
 		try {
 			t.data( source.toCharArray() );
 			t.end();
 		} catch( ParseError e ) {
 			throw e;
 		} catch( Exception e ) {
 			throw new RuntimeException(e);
 		}
 		return types;
 	}
 }
