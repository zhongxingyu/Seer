 package com.bertvanbrakel.codemucker.bean;
 
 import java.lang.annotation.Annotation;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.bertvanbrakel.codemucker.annotation.Generated;
 import com.bertvanbrakel.codemucker.annotation.Pattern;
 import com.bertvanbrakel.codemucker.annotation.PatternType;
 import com.bertvanbrakel.lang.MapBuilder;
 import com.bertvanbrakel.lang.annotation.NotThreadSafe;
 import com.bertvanbrakel.test.bean.BeanDefinition;
 import com.bertvanbrakel.test.bean.ClassUtils;
 import com.bertvanbrakel.test.bean.PropertyDefinition;
 
 @NotThreadSafe
 public abstract class AbstractBeanWriter extends AbstractClassWriter implements BeanGenerator {
 	
 	protected final BeanBuilderOptions options;
 
 	private final String pkgName;
 	private final String shortClassName;
 	private final String fullClassName;
 	
 	private String indent = "   ";
 	
 	private static final Map<String, String> propertyNameToSafeName = new HashMap<String, String>();
 	
 	static {
 		propertyNameToSafeName.put("long", "Long");
 		propertyNameToSafeName.put("int", "Int");
 		propertyNameToSafeName.put("short", "Short");
 		propertyNameToSafeName.put("double", "Double");
 		propertyNameToSafeName.put("float", "Float");
 		propertyNameToSafeName.put("byte", "bite");
 		propertyNameToSafeName.put("boolean", "Boolean");
 		propertyNameToSafeName.put("char", "character");
 		propertyNameToSafeName.put("if", "If");
 		propertyNameToSafeName.put("else", "Elze");
 		propertyNameToSafeName.put("this", "This");
 		propertyNameToSafeName.put("class", "klass");
 		propertyNameToSafeName.put("super", "superr");
 	}
 	
 	public AbstractBeanWriter(String fullClassName) {
 		this(new BeanBuilderOptions(), fullClassName);
 	}
 	
 	public AbstractBeanWriter(BeanBuilderOptions options, String fullClassName){
 		this.options = options;
 		this.fullClassName = fullClassName;
 		this.shortClassName = ClassUtils.extractShortClassNamePart(fullClassName);
 		this.pkgName = ClassUtils.extractPkgPart(fullClassName);
 	}
 	
 	public String getSourceFilePath(){
 		return fullClassName.replace('.', '/') + ".java";
 	}
 
 	protected void generatePkgDecl() {
 		println("package ${this.pkgName};");
 	}
 	
 	protected void generateImports() {
         if (options.isMarkGeneratedAnything()) {
 			addImport(Generated.class);
 		}
 		if (options.isMarkPatternAnything()) {
 			addImport(Pattern.class);
 			addImport(PatternType.class);
 		}
     }
 
 	protected void generateClassOpen() {
         if (options.isMarkPatternOnClass()) {
 			annotate(Generated.class);
 		}
		println("public class ${this.shortClassName}{");
     }
 
 	protected void generateInterfaceOpen() {
         if (options.isMarkPatternOnClass()) {
 			annotate(Generated.class);
 		}
 		println("public interface ${this.shortClassName}{");
     }
 	
 	protected void generateClassClose() {
         println("}");
     }
 
 	public void generatePropertyFields(BeanDefinition def) {
         //fields
 		for (PropertyDefinition p : def.getProperties()) {
 			if (!p.isIgnore()) {
 				if (options.isMarkeGeneratedFields()) {
 					annotate(Generated.class);
 				}
 				println("private ${property.type} ${property.field};", p);
 			}
 		}
     }
 
 	public void generateSetters(BeanDefinition def) {
         // methods
 		for (PropertyDefinition p : def.getProperties()) {
 			if (!p.isIgnore()) {
 				if (options.isMarkeGeneratedMethods()) {
 					annotate(Generated.class);
 				}
 				println("public void ${property.setter}(${property.type} ${property.field}){ this.${property.field} = ${property.field};}",p);
 			}
 		}
     }
 
 
 	public void generateInterfaceSetters(BeanDefinition def) {
         // methods
 		for (PropertyDefinition p : def.getProperties()) {
 			if (!p.isIgnore()) {
 				if (options.isMarkeGeneratedMethods()) {
 					annotate(Generated.class);
 				}
 				println("public void ${property.setter}(${property.type} ${property.field});",p);
 			}
 		}
     }
 	
 	public void generateGetters(BeanDefinition def) {
         // methods
 		for (PropertyDefinition p : def.getProperties()) {
 			if (!p.isIgnore()) {
 				if (options.isMarkeGeneratedMethods()) {
 					annotate(Generated.class);
 				}
 				println("public ${property.type} ${property.getter}(){ return ${property.field}; }",p);
 			}
 		}
     }
 	
 	public void generateInterfaceGetters(BeanDefinition def) {
         // methods
 		for (PropertyDefinition p : def.getProperties()) {
 			if (!p.isIgnore()) {
 				if (options.isMarkeGeneratedMethods()) {
 					annotate(Generated.class);
 				}
 				println("public ${property.type} ${property.getter}();",p);
 			}
 		}
     }
 	
 	public void generateAccessorCopy(BeanDefinition def) {
 		println("/** Copy properties from one bean to another */");
 		if (options.isMarkeGeneratedMethods()) {
 			annotate(Generated.class);
 		}
         println("public static void copy(${this.type} copyFrom, ${this.type} copyTo){");
 		println("if( copyFrom == null || copyTo == null) return;");
 		for (PropertyDefinition p : def.getProperties()) {
 			if (!p.isIgnore()) {
 				println("copyTo.${property.setter}(copyFrom.${property.getter}());", p);
 			}
 		}
 		println("}");
     }
 	
 	public void generateFieldClone(BeanDefinition def) {
 		if (options.isMarkeGeneratedMethods()) {
 			annotate(Generated.class);
 		}
         println("public ${this.shortClassName} clone(){");
         println("${this.shortClassName} clone = new ${this.shortClassName}();");
         
 		for (PropertyDefinition p : def.getProperties()) {
 			if (!p.isIgnore()) {
 				println("clone.${property.field} = this.${property.field};", p);
 			}
 		}
 		println( "return clone;" );
 		println("}");
     }
 
 	public void generateFieldEquals(BeanDefinition def) {	
 		if (options.isMarkeGeneratedMethods()) {
 			annotate(Generated.class);
 		}
         println("public boolean equals(Object obj){");
 		println("if( this == obj ) return true;");
 		println("if( obj == null ) return false;");
 		println("if( getClass() != obj.getClass() ) return false;");
 		println("${this.type} other = (${this.type})obj;");
 		
 		for (PropertyDefinition p : def.getProperties()) {
 			if (!p.isIgnore()) {
 				if( p.isPrimitive() && !p.isString() ){
 					if( p.isType(Double.TYPE)){
 						println( "if (java.lang.Double.doubleToLongBits(this.${property.field}) != java.lang.Double.doubleToLongBits(other.${property.field})) return false;", p );
 					} else if (p.isType(Float.TYPE)){
 						println( "if (java.lang.Float.floatToIntBits(this.${property.field}) != java.lang.Float.floatToIntBits(other.${property.field})) return false;", p );						
 					} else {
 						println("if(${property.field} != other.${property.field}){ return false; }", p);
 					}
 				} else {
     				println("if(this.${property.field} == null){", p);
     				println("   if( other.${property.field} != null ){ return false; }", p);
     				println("} else if ( !this.${property.field}.equals(other.${property.field} ) ){ return false; }", p);	
     			}
 			} 
 		}
 		println("  return true;");
 		println("}");
     }
 	
 	private void generateFieldHashcode(BeanDefinition def) {	
 		//TODO
     }
 
 	public <T extends Annotation> void annotate(Class<T> annon) {
 		annotate(annon, null);
 	}
 	
 	public <T extends Annotation> void annotate(Class<T> annon, MapBuilder<String, Object> map){
 		String line = "@" + annon.getSimpleName();
 		if (map != null && map.size() > 0) {
 			line = line + "(" + mapToString(map) + ")";
 		}
 		println(line);
 	}
 	
 	protected String mapToString(MapBuilder<String, Object> map){
 		if( map == null ){
 			return "";
 		}
 		StringBuilder sb = new StringBuilder();
 		Map<String, Object> m = map.create();
 		boolean comma = false;
 		for( String key:m.keySet()){
 			if( comma ){
 				sb.append(',');
 			}
 			comma = true;
 			sb.append(key);
 			sb.append("=");
 			Object val = m.get(key);
 			if( val instanceof Enum<?>){
 				Enum<?> e = (Enum<?>)val;
 				sb.append(val.getClass().getSimpleName() + "." + e.name() );
 			} else if( val instanceof Class ){
 				sb.append( ((Class)val).getSimpleName());
 			} else {
 				sb.append(val);
 			}
 		}
 		
 		return sb.toString();		
 	}
 	
 	protected String safeName(String name) {
 		String mappedName = propertyNameToSafeName.get(name);
 		return mappedName == null ? name : mappedName;
 	}
 	
 	protected String fieldName(String name) {
 		if( propertyNameToSafeName.containsKey(name)){
 			return "_" + name;
 		}
 		return name;
 	}
 	
 	public void println(String s, PropertyDefinition def){
 		MapBuilder<String, Object> map = map();
 		if( def != null ){
 			String upperName = ClassUtils.upperFirstChar(def.getName());
 			map		
 			.put("property.name", def.getName())
 			.put("property.safeName", safeName(def.getName()))
 			.put("property.field", fieldName(def.getName()))
 			
 			.put("property.type", ClassUtils.safeToClassName(def.getType()))
 			.put("property.genericType", ClassUtils.safeToClassName(def.getGenericType()))
 			.put("property.upperName",upperName )
 			.put("property.setter","set" + upperName)
 			.put("property.getter","get" + upperName)
 			
 			;
 		
 		}
 		println(s, map);
 	}
 	
 	public  MapBuilder<String, Object> map(){
 		MapBuilder<String, Object> map = emptyMap();
 		map.put("this.pkgName", pkgName);
 		map.put("this.type", shortClassName);
 		map.put("this.shortClassName", shortClassName);
 		map.put("this.fullClassName", fullClassName);
 		
 		map.put("indent", indent);
 
 		return map;
 	}
 
 	
 }
