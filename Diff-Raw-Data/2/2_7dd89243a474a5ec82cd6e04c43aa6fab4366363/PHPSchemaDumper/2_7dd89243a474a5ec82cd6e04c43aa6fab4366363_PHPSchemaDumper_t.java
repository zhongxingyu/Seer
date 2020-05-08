 package togos.schemaschemademo;
 
 import java.io.IOException;
 
 import togos.asyncstream.StreamDestination;
 import togos.codeemitter.TextWriter;
 import togos.schemaschema.ComplexType;
 import togos.schemaschema.EnumType;
 import togos.schemaschema.FieldSpec;
 import togos.schemaschema.ForeignKeySpec;
 import togos.schemaschema.IndexSpec;
 import togos.schemaschema.PropertyUtil;
 import togos.schemaschema.Type;
 import togos.schemaschema.namespaces.Application;
 import togos.schemaschema.namespaces.DataTypeTranslation;
 
 public class PHPSchemaDumper implements StreamDestination<ComplexType, Exception>
 {
 	protected final TextWriter tw;
 	protected final String schemaClassNamespace;
 	boolean itemWritten = false;
 	
 	public PHPSchemaDumper( Appendable dest, String schemaClassNamespace ) {
 		this.tw = new TextWriter(dest);
 		this.schemaClassNamespace = schemaClassNamespace;
 	}
 	
 	protected void openArray() throws IOException {
 		tw.write("array(");
 		tw.indentMore();
 		itemWritten = false;
 	}
 	
 	protected void closeArray() throws IOException {
 		tw.indentLess();
 		if( itemWritten ) {
 			tw.endLine();
 			tw.writeIndent();
 		}
 		tw.write(")");
 		itemWritten = true;
 	}
 	
 	protected void openObject(String className) throws IOException {
 		tw.write(className+"::__set_state(array(");
 		tw.indentMore();
 		itemWritten = false;
 	}
 	
 	protected void preItem() throws IOException {
 		if( itemWritten ) tw.write(",");
 		tw.endLine();
 		tw.writeIndent();
 	}
 	
 	protected void writeKey( String k ) throws IOException {
 		preItem();
 		writeScalar(k);
 		tw.write(" => ");
 	}
 	
 	protected void writeScalar( Object o ) throws IOException {
 		if( o == null ) {
 			tw.write("null");
 		} else if( o instanceof String ) {
 			tw.write('"' + o.toString().replace("\\", "\\\\").replace("\"", "\\\"") + '"');
 		} else if( o instanceof Number ) {
 			tw.write(o.toString());
 		} else if( o instanceof Boolean ) {
 			tw.write( ((Boolean)o).booleanValue() ? "true" : "false" );
 		} else {
 			throw new RuntimeException("Don't know how to compile "+o.getClass()+" to PHP literal");
 		}
 	}
 	
 	protected void writeItemValue( Object o ) throws IOException {
 		writeScalar(o);
 		itemWritten = true;
 	}
 	
 	protected void writePair( String k, Object v ) throws IOException {
 		preItem();
 		writeScalar(k);
 		tw.write(" => ");
 		writeItemValue(v);
 	}
 	
 	protected void closeObject() throws IOException {
 		tw.indentLess();
 		if( itemWritten ) {
 			tw.endLine();
 			tw.writeIndent();
 		}
 		tw.write("))");
 		itemWritten = true;
 	}
 	
 	protected void writeDataType( Type t ) throws IOException {
 		String name = t.getName();
 		String sqlType = PropertyUtil.getFirstInheritedScalar(t, DataTypeTranslation.SQL_TYPE, String.class, null);
 		String jsonType = PropertyUtil.getFirstInheritedScalar(t, DataTypeTranslation.JSON_TYPE, String.class, null);
 		String phpType = PropertyUtil.getFirstInheritedScalar(t, DataTypeTranslation.PHP_TYPE, String.class, null);
 		String regex = PropertyUtil.getFirstInheritedScalar(t, DataTypeTranslation.REGEX, String.class, null);
 		
 		if( t instanceof EnumType ) {
 			if( jsonType == null ) jsonType = "string";
 			if( phpType  == null ) jsonType = "string";
 		}
 		
 		openObject(schemaClassNamespace+"_DataType");
 		writeKey("name"); writeItemValue(name);
 		writeKey("sqlTypeName"); writeItemValue(sqlType);
 		writeKey("phpTypeName"); writeItemValue(phpType);
 		writeKey("jsTypeName"); writeItemValue(jsonType);
 		if( regex != null ) {
 			writeKey("regex");
 			writeItemValue(regex);
 		}
 		closeObject();
 	}
 	
 	protected void writeField( FieldSpec fs ) throws IOException {
 		openObject(schemaClassNamespace+"_Field");
 		writeKey("name"); writeItemValue(fs.getName());
 		writeKey("type");
 		writeDataType(fs.getObjectType());
 		closeObject();
 	}
 	
 	protected void writeReference( ForeignKeySpec fk ) throws IOException {
 		openObject(schemaClassNamespace+"_Reference");
 		writeKey("targetClassName"); writeItemValue(fk.target.getName());
 		writeKey("originFieldNames"); openArray();
 		for( ForeignKeySpec.Component fkc : fk.components ) {
 			preItem(); writeItemValue(fkc.localField.getName());
 		}
 		closeArray();
 		writeKey("targetFieldNames"); openArray();
 		for( ForeignKeySpec.Component fkc : fk.components ) {
 			preItem(); writeItemValue(fkc.targetField.getName());
 		}
 		closeArray();
 		closeObject();
 	}
 	
 	protected void writeIndex( IndexSpec is ) throws IOException {
 		openObject(schemaClassNamespace+"_Index");
		writeKey("fieldNames"); openArray();
 		for( FieldSpec f : is.fields ) {
 			preItem(); writeItemValue(f.getName());
 		}
 		closeArray();
 		closeObject();
 	}
 	
 	protected void writeClassSchema( ComplexType type ) throws Exception {
 		boolean hasRestService = PropertyUtil.getFirstInheritedBoolean(type, Application.HAS_REST_SERVICE, false);
 		boolean hasDbTable = PropertyUtil.getFirstInheritedBoolean(type, Application.HAS_DB_TABLE, false);
 		boolean memberSetIsMutable = PropertyUtil.getFirstInheritedBoolean(type, Application.MEMBER_SET_IS_MUTABLE, false);
 		boolean membersAreMutable = PropertyUtil.getFirstInheritedBoolean(type, Application.MEMBERS_ARE_MUTABLE, false);
 		boolean membersArePublic = PropertyUtil.getFirstInheritedBoolean(type, Application.MEMBERS_ARE_PUBLIC, false);
 		
 		openObject(schemaClassNamespace+"_ResourceClass");
 		
 		writePair("name", type.getName());
 		writePair("hasDbTable", hasDbTable);
 		writePair("hasRestService", hasRestService);
 		writePair("memberSetIsMutable", memberSetIsMutable);
 		writePair("membersAreMutable", membersAreMutable);
 		writePair("membersArePublic", membersArePublic);
 		
 		writeKey("fields"); openArray();
 		for( FieldSpec fs : type.getFields() ) {
 			writeKey(fs.getName());
 			writeField(fs);
 		}
 		closeArray();
 		
 		writeKey("references"); openArray();
 		for( ForeignKeySpec fk : type.getForeignKeys() ) {
 			writeKey(fk.getName());
 			writeReference(fk);
 		}
 		closeArray();
 		
 		writeKey("indexes"); openArray();
 		for( IndexSpec is : type.getIndexes() ) {
 			writeKey(is.getName());
 			writeIndex(is);
 		}
 		closeArray();
 		
 		closeObject();
 	}
 	
 	boolean opened = false;
 	
 	protected void ensureSchemaOpened() throws IOException {
 		if( !opened ) {
 			tw.write("<?php\n");
 			tw.write("\n");
 			tw.write("// This file was generated automatically.\n");
 			tw.write("// Don't make changes to it directly unless you like your changes being overwritten.\n");
 			tw.write("\n");
 			tw.write("return ");
 			openObject(schemaClassNamespace);
 			writeKey("resourceClasses");
 			openArray();
 			opened = true;
 		}
 	}
 	
 	@Override public void data(ComplexType type) throws Exception {
 		ensureSchemaOpened();
 		writeKey(type.getName());
 		writeClassSchema(type);
 	}
 	
 	@Override public void end() throws Exception {
 		ensureSchemaOpened();
 		closeArray();
 		closeObject();
 		tw.write(";");
 		tw.endLine();
 	}
 }
