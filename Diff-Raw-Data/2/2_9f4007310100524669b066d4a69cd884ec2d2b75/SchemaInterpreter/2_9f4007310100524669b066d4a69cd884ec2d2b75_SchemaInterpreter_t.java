 package togos.schemaschema.parser;
 
 import static togos.schemaschema.PropertyUtil.isTrue;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import togos.asyncstream.BaseStreamSource;
 import togos.asyncstream.StreamDestination;
 import togos.asyncstream.StreamUtil;
 import togos.lang.BaseSourceLocation;
 import togos.lang.InterpretError;
 import togos.lang.SourceLocation;
 import togos.schemaschema.BaseSchemaObject;
 import togos.schemaschema.ComplexType;
 import togos.schemaschema.EnumType;
 import togos.schemaschema.FieldSpec;
 import togos.schemaschema.ForeignKeySpec;
 import togos.schemaschema.IndexSpec;
 import togos.schemaschema.Predicate;
 import togos.schemaschema.Predicates;
 import togos.schemaschema.PropertyUtil;
 import togos.schemaschema.SchemaObject;
 import togos.schemaschema.Type;
 import togos.schemaschema.Types;
 import togos.schemaschema.parser.ast.Block;
 import togos.schemaschema.parser.ast.Command;
 import togos.schemaschema.parser.ast.Parameterized;
 import togos.schemaschema.parser.ast.Phrase;
 import togos.schemaschema.parser.ast.Word;
 
 public class SchemaInterpreter extends BaseStreamSource<SchemaObject> implements StreamDestination<Command>
 {
 	public static class RedefinitionError extends InterpretError {
 		private static final long serialVersionUID = 1L;
 		
 		public RedefinitionError( String message, SourceLocation sLoc ) {
 			super(message, sLoc);
 		}
 	}
 	
 	protected static String singleString( Parameterized p, String contextDescription ) throws InterpretError {
 		if( p.parameters.length != 0 ) {
 			throw new InterpretError( contextDescription + " cannot take arguments", p.sLoc );
 		}
 		return p.subject.unquotedText();
 	}
 	
 	protected static void applyFieldModifier( Modifier m, ComplexType t, FieldSpec f ) {
 		if( m instanceof FieldModifier ) {
 			((FieldModifier)m).apply(t, f);
 		} else {
 			m.apply(f);
 		}
 	}
 	
 	//// Command interpreters ////
 	
 	interface CommandInterpreter {
 		/**
 		 * @return true if this interpreter recognized and interpreted the command.
 		 * @throws Exception
 		 */
 		public boolean interpretCommand( Command cmd, int cmdPrefixLength ) throws Exception;
 	}
 	
 	abstract class DefinitionCommandInterpreter implements CommandInterpreter {
 		protected abstract void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws Exception;
 		
 		@Override public boolean interpretCommand( Command cmd, int cmdPrefixLength ) throws Exception {
 			ensureNoParameters(cmd.subject, "symbol being defined");
 			Phrase cmdPhrase = cmd.subject.subject;
 			boolean allowRedefinition;
 			if( cmdPrefixLength >= 2 && cmdPhrase.startsWithWord("redefine") ) {
 				cmdPhrase = cmdPhrase.tail(1);
 				--cmdPrefixLength;
 				allowRedefinition = true;
 			} else {
 				allowRedefinition = false;
 			}
 			interpretDefinition( cmdPhrase.tail(cmdPrefixLength).unquotedText(), cmd.modifiers, cmd.body, allowRedefinition, cmd.sLoc );
 			return true;
 		}
 	}
 	
 	class ClassDefinitionCommandInterpreter extends DefinitionCommandInterpreter {
 		final Type metaClass;
 		public ClassDefinitionCommandInterpreter( Type metaClass ) {
 			this.metaClass = metaClass;
 		}
 		public ClassDefinitionCommandInterpreter() {
 			this(null);
 		}
 		
 		public ComplexType parseClass( String name, Parameterized[] modifiers, Block body, SourceLocation sLoc )
 			throws InterpretError
 		{
 			ComplexType t = new ComplexType( name, sLoc );
 			if( metaClass != null ) PropertyUtil.add( t.properties, Predicates.IS_MEMBER_OF, metaClass );
 			
 			for( Command fieldCommand : body.commands ) {
 				for( Parameterized fieldNameParameter : fieldCommand.subject.parameters ) {
 					throw new InterpretError("Field name cannot have parameters", fieldNameParameter.sLoc );
 				}
 				
 				String foreignTypeName = null;
 				Block referenceBody = null;
 				ArrayList<Modifier> _fieldModifiers = new ArrayList<Modifier>();
 				for( Parameterized mod : fieldCommand.modifiers ) {
 					if( "reference".equals(mod.subject.unquotedText()) ) {
 						if( mod.parameters.length != 1 ) {
 							throw new InterpretError(
 								"'reference' field modifier takes a single parameter: "+
 								"the name of the type being referenced.  Got "+
 								mod.parameters.length+" parameters", mod.sLoc
 							);
 						}
 						foreignTypeName = singleString( mod.parameters[0] ,"foreign type name" );
 						if( fieldCommand.body == null ) {
 							throw new InterpretError(
 								"'reference' field specification requires a block", fieldCommand.sLoc
 							);
 						}
 						if( fieldCommand.body.commands.length == 0 ) {
 							throw new InterpretError(
 								"'reference' field specificatino requires at least one foreign key component",
 								fieldCommand.body.sLoc
 							);
 						}
 						referenceBody = fieldCommand.body;
 					} else {
 						ModifierSpec ms = fieldModifiers.get(mod.subject);
 						_fieldModifiers.add( ms.bind( SchemaInterpreter.this, mod.parameters, mod.sLoc ) );
 					}
 				}
 				
 				if( referenceBody != null ) {
 					String fieldName = fieldCommand.subject.subject.unquotedText();
 					
 					assert foreignTypeName != null;
 					ComplexType foreignType = foreignTypeName.equals(name) ? t : (ComplexType)types.get(foreignTypeName);
 					
 					ArrayList<ForeignKeySpec.Component> fkComponents = new ArrayList<ForeignKeySpec.Component>();
 					for( Command fkCommand : referenceBody.commands ) {
 						String foreignFieldName = singleString( fkCommand.subject, "foreign field name" );
 						FieldSpec foreignField = foreignType.getField(foreignFieldName);
 						if( foreignField == null ) {
 							throw new InterpretError("Foreign key constraint references non-existent field '"+foreignFieldName+"' on type '"+foreignTypeName+"'", fkCommand.subject.sLoc);
 						}
 						
 						Command localFieldNode;
 						if( fkCommand.body.commands.length > 0 ) {
 							if( fkCommand.body.commands.length != 1 ) {
 								throw new InterpretError(
 									"Foreign key component requires exactly 0 or 1 local field specifications; given "+
 									fkCommand.body.commands.length, fkCommand.body.sLoc
 								);
 							}
 							if( fkCommand.modifiers.length != 0 ){
 								throw new InterpretError(
 									"Modifiers not allowed for foreign field specification",
 									fkCommand.modifiers[0].sLoc
 								);
 							}
 							localFieldNode = fkCommand.body.commands[0];
 						} else {
 							localFieldNode = fkCommand;
 						}
 						
 						Type foreignFieldType = foreignField.getObjectType();
 						
 						String localFieldName = singleString(localFieldNode.subject, "local field name");
 						FieldSpec localField = getSimpleField( t, localFieldNode );
 						
 						// If the local field already specifies a type, it must match
 						// the foreign field's type:
 						for( Type localFieldType : localField.getObjectTypes() ) {
 							if( localFieldType != foreignFieldType ) {
 								throw new InterpretError(
 									"Local copy of reference field '"+localFieldName+"' "+
 									"specifies a type ("+localFieldType.getName()+") "+
 									"that is different than the foreign field's type ("+foreignFieldType.getName()+")",
 									localFieldNode.sLoc
 								);
 							}
 						}
 						PropertyUtil.add( localField.getProperties(), Predicates.OBJECTS_ARE_MEMBERS_OF, foreignFieldType );
 						
 						fkComponents.add( new ForeignKeySpec.Component(foreignField, localField) );
 						
 						for( Modifier m : _fieldModifiers ) {
 							// This is here so that key(index) or like such as modifiers have their effects
 							// transferred from the reference field to its constituent parts.
 							// It might make sense to disallow other types of modifiers here.
 							if( m instanceof FieldModifier ) {
 								((FieldModifier)m).apply( t, localField );
 							} else {
 								m.apply( localField );
 							}
 						}
 					}
 					
 					t.addForeignKey( new ForeignKeySpec(t.getName()+" "+fieldName, foreignType, fkComponents, fieldCommand.sLoc) );
 				} else {
 					// Note that in this case, _fieldModifiers is ignored; defineSimpleField does its own gathering.
					defineSimpleField( t, fieldCommand );
 				}
 			}
 			
 			for( Parameterized mod : modifiers ) {
 				ModifierSpec m = classModifiers.get(mod.subject);
 				m.bind(SchemaInterpreter.this, mod.parameters, mod.sLoc).apply(t);
 			}
 			
 			if( isTrue(t, Predicates.IS_SELF_KEYED) ) {
 				t.addIndex(new IndexSpec("primary", t.getFields(), sLoc));
 			}
 			
 			return t;
 		}
 
 		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws Exception {
 			Type t = parseClass( name, modifiers, body, sLoc );
 			
 			defineType( t, allowRedefinition );
 			CommandInterpreter instanceInterpreter;
 			if( PropertyUtil.getFirstInheritedValue(t, Predicates.EXTENDS, (SchemaObject)null) == Types.CLASS ) {
 				// If the defined class extends class, then instances will themselves be classes
 				// and can use ClassDefinitionCommandInterpreter
 				instanceInterpreter = new ClassDefinitionCommandInterpreter( t );
 			} else {
 				// Otherwise instances are just generic objects and
 				// will use the plain old object interpreter
 				instanceInterpreter = new ObjectCommandInterpreter();
 			}
 			commandInterpreters.put(t.getName(), instanceInterpreter, allowRedefinition, t.getSourceLocation());
 		}
 	}
 	
 	class EnumDefinitionCommandInterpreter extends DefinitionCommandInterpreter {
 		private EnumType parseEnum( String name, Parameterized[] modifiers, Block body, SourceLocation sLoc ) throws InterpretError {
 			EnumType t = new EnumType(name, sLoc);
 			
 			for( Parameterized mod : modifiers ) {
 				classModifiers.get(mod.subject).bind(SchemaInterpreter.this, mod.parameters, mod.sLoc).apply(t);
 			}
 			
 			for( Command c : body.commands ) {
 				ensureNoParameters(c.subject, "enum value");
 				if( c.body.commands.length > 0 ) {
 					throw new InterpretError("Enum value body is ignored", c.body.sLoc);
 				}
 				
 				SchemaObject obj = t.addValidValue(c.subject.subject.unquotedText(), c.sLoc);
 				for( Parameterized mod : c.modifiers ) {
 					generalModifiers.get(mod.subject).bind(SchemaInterpreter.this, mod.parameters, mod.sLoc).apply(obj);
 				}
 			}
 			
 			return t;
 		}
 		
 		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws Exception {
 			defineType( parseEnum( name, modifiers, body, sLoc ), allowRedefinition );
 		}
 	}
 	
 	class ObjectCommandInterpreter extends DefinitionCommandInterpreter {
 		public ObjectCommandInterpreter() { }
 		
 		protected void defineObject( SchemaObject obj, boolean allowRedefinition ) throws Exception {
 			things.put( obj.getName(), obj, allowRedefinition, obj.getSourceLocation() );
 			_data( obj );
 		}
 		
 		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws Exception {
 			defineObject( parseObject( name, modifiers, body, sLoc ), allowRedefinition );
 		}
 	}
 	
 	class PropertyDefinitionCommandInterpreter extends DefinitionCommandInterpreter {
 		final SymbolLookupContext<ModifierSpec> modifierLookupContext;
 		
 		public PropertyDefinitionCommandInterpreter( SymbolLookupContext<ModifierSpec> modifierLookupContext ) {
 			this.modifierLookupContext = modifierLookupContext;
 		}
 		
 		protected void definePredicate( Predicate pred, boolean allowRedefinition ) throws Exception {
 			predicates.put( pred.getName(), pred, allowRedefinition, pred.getSourceLocation() );
 			defineModifier( modifierLookupContext, pred.getName(), new SimplePredicateModifierSpec(pred), allowRedefinition, pred.getSourceLocation() );
 			_data( pred );
 		}
 		
 		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws Exception {
 			definePredicate( parseProperty( name, modifiers, body, sLoc ), allowRedefinition );
 		}
 	}
 	
 	class ModifierDefinitionCommandInterpreter extends DefinitionCommandInterpreter {
 		final SymbolLookupContext<ModifierSpec> registeredModifiers;
 		public ModifierDefinitionCommandInterpreter( SymbolLookupContext<ModifierSpec> predefinedModifiers ) {
 			this.registeredModifiers = predefinedModifiers;
 		}
 		
 		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws Exception {
 			defineModifier( registeredModifiers, name, parseModifierSpec(name, modifiers, body, registeredModifiers), allowRedefinition, sLoc );
 		}
 	}
 	
 	//// Modifiers
 	
 	interface ModifierSpec {
 		public Modifier bind( SchemaInterpreter sp, Parameterized[] params, SourceLocation sLoc ) throws InterpretError;
 	}
 	
 	interface Modifier {
 		public void apply( SchemaObject subject );
 	}
 	
 	interface FieldModifier extends Modifier {
 		public void apply( ComplexType classObject, FieldSpec fieldSpec );
 	}
 	
 	public static class SimplePredicateModifierSpec implements ModifierSpec {
 		final Predicate predicate;
 		
 		public SimplePredicateModifierSpec( Predicate p ) {
 			this.predicate = p;
 		}
 		
 		@Override public Modifier bind(SchemaInterpreter sp, Parameterized[] params, SourceLocation sLoc) throws InterpretError {
 			final Set<SchemaObject> values = new LinkedHashSet<SchemaObject>();
 			if( params.length == 0 ) {
 				values.add( BaseSchemaObject.forScalar(Boolean.TRUE, sLoc) );
 			} else {
 				for( Parameterized p : params ) {
 					values.add( sp.evaluate( predicate, p ) );
 				}
 			}
 			
 			return new Modifier() {
 				public void apply(SchemaObject subject) {
 					PropertyUtil.addAll( subject.getProperties(), predicate, values );					
 				}
 			};
 		}
 	}
 	
 	/** Modifier that is equivalent to a set of property -> value pairs */
 	public static class AliasModifier implements Modifier, ModifierSpec {
 		final String name;
 		final Map<Predicate,Set<SchemaObject>> propertyValues;
 		
 		public AliasModifier( String name, Map<Predicate,Set<SchemaObject>> propertyValues ) {
 			this.name = name;
 			this.propertyValues = propertyValues;
 		}
 		
 		@Override
 		public Modifier bind(SchemaInterpreter sp, Parameterized[] params, SourceLocation sLoc) throws InterpretError {
 			if( params.length > 0 ) {
 				throw new InterpretError(name+" modifier takes no arguments", params[0].sLoc);
 			}
 			return this;
 		}
 		
 		@Override
 		public void apply(SchemaObject subject) {
 			PropertyUtil.addAll( subject.getProperties(), propertyValues );
 		}
 	}
 	
 	public static class FieldIndexModifierSpec implements ModifierSpec {
 		public static FieldIndexModifierSpec INSTANCE = new FieldIndexModifierSpec();
 		
 		@Override
 		public Modifier bind(SchemaInterpreter sp, Parameterized[] params, final SourceLocation sLoc) throws InterpretError {
 			final ArrayList<String> indexNames = new ArrayList<String>();
 			
 			if( params.length == 0 ) {
 				throw new InterpretError("Index modifier with no arguments is useless", sLoc);
 			}
 			for( Parameterized indexParam : params ) {
 				for( Parameterized indexParamParam : indexParam.parameters ) {
 					throw new InterpretError("Index name takes no parameters, but parameters given", indexParamParam.sLoc);
 				}
 				indexNames.add( indexParam.subject.unquotedText() );
 			}
 
 			return new FieldModifier() {
 				@Override
 				public void apply(SchemaObject subject) {
 					throw new UnsupportedOperationException("Index field modifier doesn't support apply(subject) -- use the other apply method");
 				}
 				
 				@Override
 				public void apply(ComplexType classObject, FieldSpec fieldSpec) {
 					for( String indexName : indexNames ) {
 						IndexSpec index = classObject.getIndex(indexName);
 						if( index == null ) {
 							index = new IndexSpec(indexName, sLoc);
 							classObject.addIndex(index);
 						}
 						index.fields.add( fieldSpec );
 					}
 				}
 			};
 		}
 	}
 	
 	/**
 	 * Used to specify an new (anonymous) enum type for a single field
 	 */
 	public static class EnumModifierSpec implements ModifierSpec {
 		final Predicate pred;
 		public EnumModifierSpec( Predicate pred ) {
 			this.pred = pred;
 		}
 		
 		@Override
 		public Modifier bind(SchemaInterpreter sp, final Parameterized[] params, final SourceLocation sLoc) throws InterpretError {
 			for( Parameterized p : params ) {
 				for( Parameterized pp : p.parameters ) {
 					throw new InterpretError( "Enum values cannot themselves take parameters", pp.sLoc);
 				}
 			}
 			return new Modifier() {
 				@Override public void apply(SchemaObject subject) {
 					EnumType er = new EnumType(subject.getName(), sLoc);
 					for( Parameterized p : params ) {
 						er.addValidValue(p.subject.unquotedText(), p.subject.sLoc);
 					}
 					PropertyUtil.add( subject.getProperties(), pred, er );
 				}
 			};
 		}
 	}
 	
 	////
 	
 	class SymbolLookupContext<T> {
 		final SymbolLookupContext<? super T> parent;
 		final String name;
 		final Map<String,T> values = new HashMap<String,T>();
 		final Class<T> valueClass;
 		SymbolLookupContext( SymbolLookupContext<? super T> parent, String name, Class<T> valueClass ) {
 			this.parent = parent;
 			this.name = name;
 			this.valueClass = valueClass;
 		}
 		
 		protected void dump( PrintStream dest ) {
 			SymbolLookupContext<?> ctx = this;
 			while( ctx != null ) {
 				dest.println(ctx.name+" context");
 				for( Map.Entry<String,?> e : ctx.values.entrySet() ) {
 					dest.println("  "+e.getKey()+" : "+e.getValue()+" ("+e.getValue().getClass().getName()+")");
 				}
 				ctx = ctx.parent;
 			}
 		}
 		
 		public boolean isDefined( String name ) {
 			SymbolLookupContext<? super T> ctx = this;
 			while( ctx != null ) {
 				if( ctx.values.containsKey(name) ) return true;
 				ctx = ctx.parent;
 			}
 			return false;
 		}
 		
 		public T get(String name, Class<T> requiredType, boolean throwOnNotFound, boolean throwOnWrongType, SourceLocation refLoc) throws InterpretError {
 			SymbolLookupContext<? super T> ctx = this;
 			while( ctx != null ) {
 				Object o = ctx.values.get(name);
 				if( o != null ) {
 					if( requiredType.isInstance(o) ) {
 						return requiredType.cast(o);
 					} else {
 						if( throwOnWrongType ) {
 							throw new InterpretError(this.name + " '"+name+"' is not a "+requiredType.getName()+", but "+o.getClass().getName(), refLoc);
 						}
 						return null;
 					}
 				}
 				ctx = ctx.parent;
 			}
 			if( throwOnNotFound ) {
 				//dump( System.err );
 				throw new InterpretError("'"+name+"' is not defined as a "+this.name, refLoc);
 			}
 			return null;
 		}
 		
 		public T get(Phrase p) throws InterpretError {
 			return get(p.unquotedText(), valueClass, true, true, p.sLoc); 
 		}
 		
 		public T get(String name) throws InterpretError {
 			return get(name, valueClass, true, true, BaseSourceLocation.NONE); 
 		}
 		
 		public void put(String name, T value, boolean allowRedefinition, SourceLocation sLoc) throws RedefinitionError {
 			if( isDefined(name) && !allowRedefinition ) {
 				throw new RedefinitionError("Redefining "+this.name+" '"+name+"'", sLoc);
 			}
 			
 			SymbolLookupContext<? super T> ctx = this;
 			while( ctx != null ) {
 				ctx.values.put(name, value);
 				ctx = ctx.parent;
 			}
 		}
 	}
 	
 	protected SymbolLookupContext<SchemaObject> things = new SymbolLookupContext<SchemaObject>(null, "thing", SchemaObject.class);
 	protected SymbolLookupContext<Type> types = new SymbolLookupContext<Type>(things, "type", Type.class);
 	protected SymbolLookupContext<Predicate> predicates = new SymbolLookupContext<Predicate>(things, "predicate", Predicate.class);
 	protected SymbolLookupContext<ModifierSpec> generalModifiers = new SymbolLookupContext<ModifierSpec>(null, "general modifier", ModifierSpec.class);
 	protected SymbolLookupContext<ModifierSpec> fieldModifiers = new SymbolLookupContext<ModifierSpec>(generalModifiers, "field modifier", ModifierSpec.class);
 	protected SymbolLookupContext<ModifierSpec> classModifiers = new SymbolLookupContext<ModifierSpec>(generalModifiers, "class modifier", ModifierSpec.class);
 	protected SymbolLookupContext<CommandInterpreter> commandInterpreters = new SymbolLookupContext<CommandInterpreter>(null, "command interpreter", CommandInterpreter.class); 
 	
 	public SchemaInterpreter() { }
 	
 	public void defineType( Type t, boolean allowRedefinition ) throws Exception {
 		types.put( t.getName(), t, allowRedefinition, t.getSourceLocation() );
 		HashMap<Predicate,Set<SchemaObject>> appliedProperties = new HashMap<Predicate,Set<SchemaObject>>();
 		PropertyUtil.add(appliedProperties, Predicates.OBJECTS_ARE_MEMBERS_OF, t);
 		fieldModifiers.put( t.getName(), new AliasModifier(Predicates.OBJECTS_ARE_MEMBERS_OF.getName(), appliedProperties), allowRedefinition, t.getSourceLocation() );
 		_data( t );
 	}
 	
 	public void defineType( Type t ) throws Exception {
 		defineType( t, false );
 	}
 	
 	boolean allowIsLessModifierShorthand = true;
 	
 	protected void defineModifier( SymbolLookupContext<ModifierSpec> modifierMap, String name, ModifierSpec spec, boolean allowRedefinition, SourceLocation sLoc )
 		throws InterpretError 
 	{
 		modifierMap.put( name, spec, allowRedefinition, sLoc );
 		if( allowIsLessModifierShorthand && name.startsWith("is ") ) {
 			String shorthand = name.substring(3);
 			//System.err.println("Duplicate '"+name+"' as '"+shorthand+"'");
 			if( !modifierMap.isDefined(shorthand) ) { 
 				modifierMap.put( shorthand, spec, false, sLoc );
 			}
 		}
 	}
 	
 	public void defineFieldModifier( String name, ModifierSpec spec ) throws InterpretError {
 		defineModifier(fieldModifiers, name, spec, false, BaseSourceLocation.NONE );
 	}
 	
 	public void defineClassModifier( String name, ModifierSpec spec ) throws InterpretError {
 		defineModifier(classModifiers, name, spec, false, BaseSourceLocation.NONE );
 	}
 	
 	protected void definePredicate( Predicate pred, SymbolLookupContext<ModifierSpec> modifierContext, boolean allowRedefinition )
 		throws Exception
 	{
 		predicates.put( pred.getName(), pred, false, pred.getSourceLocation() );
 		defineModifier( modifierContext, pred.getName(), new SimplePredicateModifierSpec(pred), false, pred.getSourceLocation() );
 		_data( pred );
 	}
 	
 	public void defineFieldPredicate( Predicate pred ) throws Exception {
 		definePredicate( pred, fieldModifiers, false );
 	}
 	
 	public void defineClassPredicate( Predicate pred ) throws Exception {
 		definePredicate( pred, classModifiers, false );
 	}
 	
 	public void defineGenericPredicate( Predicate pred ) throws Exception {
 		definePredicate( pred, generalModifiers, false );
 	}
 	
 	public void defineCommand( String name, CommandInterpreter interpreter, boolean allowRedefinition, SourceLocation sLoc )
 		throws InterpretError
 	{
 		commandInterpreters.put( name, interpreter, allowRedefinition, sLoc );
 	}
 	
 	public void defineCommand( String name, CommandInterpreter interpreter ) {
 		try {
 			commandInterpreters.put( name, interpreter, false, BaseSourceLocation.NONE );
 		} catch( InterpretError e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	protected SchemaObject evaluate( SchemaObject v, Parameterized[] parameters ) throws InterpretError {
 		if( parameters.length > 0 ) {
 			throw new InterpretError("Don't know how to parameterize "+v.getClass(), parameters[0].sLoc);
 		}
 		return v;
 	}
 	
 	/**
 	 * @param context predicate whose object we are evaluating; may be null
 	 * @param p Parameterized representation of the value
 	 * @return
 	 * @throws InterpretError
 	 */
 	protected SchemaObject evaluate( Predicate context, Parameterized p ) throws InterpretError {
 		if( p.subject.words.length == 1 && p.subject.words[0].quoting == Token.Type.DOUBLE_QUOTED_STRING ) {
 			return BaseSchemaObject.forScalar(p.subject.unquotedText(), p.subject.sLoc);
 		}
 		// TODO: Parse number literals
 		
 		String name = p.subject.unquotedText();
 		Set<SchemaObject> possibleValues = new LinkedHashSet<SchemaObject>();
 		if( context != null ) {
 			for( Type t : context.getObjectTypes() ) {
 				if( t instanceof EnumType ) {
 					for( SchemaObject enumValue : ((EnumType)t).getValidValues() ) {
 						if( name.equals(enumValue.getName()) ) {
 							possibleValues.add(enumValue);
 						}
 					}
 				}
 			}
 		}
 		
 		if( possibleValues.size() == 0 ) {
 			SchemaObject v = things.get(p.subject);
 			if( v != null ) possibleValues.add( v );
 		}
 		
 		if( possibleValues.size() == 0 ) {
 			throw new InterpretError("Unrecognized symbol: "+Word.quote(name), p.subject.sLoc);
 		} else if( possibleValues.size() > 1 ) {
 			// TODO: list definition locations
 			throw new InterpretError("Symbol "+Word.quote(name)+" is ambiguous", p.subject.sLoc);
 		} else {
 			for( SchemaObject v : possibleValues ) {
 				return evaluate( v, p.parameters );
 			}
 			throw new RuntimeException("Somehow foreach body wasn't evaluated for single-item set");
 		}
 	}
 	
 	protected FieldSpec defineSimpleField(
 		ComplexType objectType,
 		Command fieldCommand
 	) throws InterpretError {
 		String fieldName = singleString(fieldCommand.subject, "field name");
 		if( objectType.hasField(fieldName) ) {
 			throw new InterpretError( "Field '"+fieldName+"' already defined", fieldCommand.sLoc );
 		}
 		
 		FieldSpec fieldSpec = new FieldSpec( fieldName, fieldCommand.sLoc );
 		
 		for( Parameterized modifier : fieldCommand.modifiers ) {
 			ModifierSpec ms = fieldModifiers.get(modifier.subject);
 			Modifier m = ms.bind( this, modifier.parameters, modifier.sLoc );
 			applyFieldModifier( m, objectType, fieldSpec );
 		}
 		
 		objectType.addField( fieldSpec );
 		return fieldSpec;
 	}
 	
 	protected FieldSpec getSimpleField(
 		ComplexType objectType,
 		Command fieldCommand
 	) throws InterpretError {
 		String fieldName = singleString(fieldCommand.subject, "field name");
 		FieldSpec fieldSpec = objectType.getField(fieldName);
 		if( fieldSpec == null ) {
 			fieldSpec = defineSimpleField( objectType, fieldCommand );
 		} else if( fieldCommand.modifiers.length > 0 ) {
 			throw new InterpretError( "Cannot redefine field '"+fieldName+"'", fieldCommand.sLoc );
 		}
 		return fieldSpec;
 	}
 	
 	protected SchemaObject parseObject( String name, Parameterized[] modifiers, Block body, SourceLocation sLoc ) throws InterpretError {
 		BaseSchemaObject obj = new BaseSchemaObject( name, sLoc );
 		for( Parameterized p : modifiers ) {
 			ModifierSpec ms = generalModifiers.get(p.subject);
 			ms.bind(this, p.parameters, p.sLoc).apply(obj);
 		}
 		for( Command c : body.commands ) {
 			throw new InterpretError("Object literals cannot have a block", c.sLoc );
 		}
 		return obj;
 	}
 	
 	protected Predicate parseProperty( String name, Parameterized[] modifiers, Block body, SourceLocation sLoc ) throws InterpretError {
 		Predicate pred = new Predicate(name, sLoc);
 		for( Parameterized p : modifiers ) {
 			ModifierSpec ms = fieldModifiers.get(p.subject);
 			ms.bind(this, p.parameters, p.sLoc).apply(pred);
 		}
 		for( Command c : body.commands ) {
 			throw new InterpretError("Property definitions cannot have a block", c.sLoc );
 		}
 		return pred;
 	}
 	
 	private ModifierSpec parseModifierSpec(final String name, Parameterized[] modifierModifiers, Block body, final SymbolLookupContext<ModifierSpec> predefinedModifiers) throws InterpretError {
 		final ArrayList<Modifier> subModifiers = new ArrayList<Modifier>();
 		if( body.commands.length != 1 ) {
 			throw new InterpretError("modifier definition must have exactly 1 command, "+body.commands.length+" given",
 				body.commands.length == 0 ? body.sLoc : body.commands[1].sLoc );
 		}
 		for( Command cmd : body.commands ) {
 			for( Parameterized p : cmd.getSubjectAndModifiers() ) {
 				ModifierSpec ms = predefinedModifiers.get(p.subject);
 				subModifiers.add( ms.bind(this, p.parameters, p.sLoc) );
 			}
 		}
 		return new ModifierSpec() {
 			@Override
 			public Modifier bind(SchemaInterpreter sp, Parameterized[] params, SourceLocation sLoc) throws InterpretError {
 				if( params.length > 0 ) {
 					throw new InterpretError("Custom "+predefinedModifiers.name+" '"+name+"' takes no parameters", sLoc);
 				}
 				return new FieldModifier() {
 					@Override
 					public void apply( ComplexType classObject, FieldSpec fieldSpec ) {
 						for( Modifier m : subModifiers ) {
 							applyFieldModifier( m, classObject, fieldSpec );
 						}
 					}
 					
 					@Override public void apply( SchemaObject subject ) {
 						for( Modifier m : subModifiers ) {
 							m.apply(subject);
 						}
 					}
 				};
 			}
 		};
 	}
 	
 	protected void ensureNoParameters( Parameterized s, String context ) throws InterpretError {
 		for( Parameterized classNameParameter : s.parameters ) {
 			throw new InterpretError(context + " cannot have parameters", classNameParameter.sLoc );
 		}
 	}
 	
 	@Override public void data(Command value) throws Exception {
 		Phrase cmd = value.subject.subject;
 		if( cmd.words.length >= 2 ) {
 			Phrase typeName = cmd.head(cmd.words.length-1);
 			if( cmd.words.length >= 3 && cmd.startsWithWord("redefine") ) {
 				typeName = typeName.tail(1);
 			}
 			CommandInterpreter ci = commandInterpreters.get(typeName);
 			if( ci.interpretCommand(value, cmd.words.length-1) ) {
 				return;
 			}
 		}
 		
 		throw new InterpretError("Unrecognised command: '"+cmd+"'", value.sLoc);
 	}
 
 	@Override public void end() throws Exception {
 		_end();
 	}
 	
 	//// Convenience methods for when you don't feel like setting
 	//// up your own tokenizers and yaddah yaddah yaddah
 	
 	public void parse( Reader r, String sourceName ) throws IOException, InterpretError {
 		Tokenizer t = new Tokenizer();
 		if( sourceName != null ) t.setSourceLocation( sourceName, 1, 1 );
 		Parser p = new Parser();
 		p.pipe(this);
 		t.pipe(p);
 		try {
 			StreamUtil.pipe( r, t, true );
 		} catch( InterpretError e ) {
 			throw e;
 		} catch( Exception e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public void parse( String source, String sourceName ) throws InterpretError {
 		try {
 			parse( new StringReader(source), sourceName );
 		} catch( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 }
