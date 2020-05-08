 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import com.sun.tools.javac.code.Type;
 import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
 import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
 
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.TypeVariable;
 import java.lang.reflect.WildcardType;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Represents type in API.
  * User: Jan Hýbl
  * Date: 27.4.12
  * Time: 13:43
  */
 public class APIType {
 
     /**
      * Name of the class (with package name),
      * Examples: java.util.List, java.io.File
      */
     private String name;
 
     /**
      * List of the type arguments of a type.
      * Example: For type <code>java.util.Map<java.lang.Integer, java.lang.String></code>
      * typeArgs list contains two items: <code>java.lang.Integer</code> and <code>java.lang,String</code>.
      */
     private List<APIType> typeArgs = new LinkedList<>();
 
     /**
      * Indicates if the type is array.
      */
     private boolean array = false;
 
     /**
      * Indicates the type of wildcard type.
      * UPPER = extends keyword
      * LOWER = super keyword
      * NULL = no bound.
      */
     private BoundType bound = BoundType.NULL;
 
     public void setBound(BoundType bound) {
         this.bound = bound;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public enum BoundType {
         UPPER, LOWER, NULL;
     }
 
     /**
      * Used for searching of nested classes. Has '$' char instead of '.' for indication of the inner class.
      */
     private String flatName;
     
     private APIModifier accessModifier = APIModifier.UNSET;
 
     public APIType(String name) {
        this.name = name;
         this.flatName = name;
     }
 
     public APIType(String name, boolean array) {
         this.name = name;
         this.array = array;
         this.flatName = name;
     }
 
     public APIType(Type type) {
         this.name = type.tsym.getQualifiedName().toString();
         this.flatName = type.tsym.flatName().toString();
         if(type instanceof Type.ArrayType) {
             this.name = ((Type.ArrayType) type).elemtype.tsym.getQualifiedName().toString();
             this.array = true;
         }
         if(type instanceof Type.WildcardType) {
             this.name = "?";
             Type.WildcardType wt = (Type.WildcardType) type;
 
             switch (wt.kind) {
                 case EXTENDS: bound = BoundType.UPPER;
                     addTypeParameter(new APIType(wt.getExtendsBound()));
                     break;
                 case SUPER: bound = BoundType.LOWER;
                     addTypeParameter(new APIType(wt.getSuperBound()));
                     break;
                 default: bound = BoundType.NULL; break;
             }
         }
         for(Type typeParam : type.getTypeArguments()) {
             addTypeParameter(new APIType(typeParam));
         }
     }
 
     // TODO: poradne implementovat a otestovat nasledujici konstruktory... tohle je jen takova nouzovka...
     public APIType(java.lang.reflect.Type type) {
         if (type instanceof GenericArrayType) {
             GenericArrayType at = (GenericArrayType) type;            
             this.array = true;
             APIType result = new APIType(at.getGenericComponentType());
             this.name = result.getName();
             this.bound = result.getBound();
             this.typeArgs = result.getTypeArgs();
             this.flatName = result.getFlatName();
         } else if(type instanceof ParameterizedTypeImpl) {
             ParameterizedType pt = (ParameterizedType) type;
             this.name = ((Class) ((ParameterizedType) type).getRawType()).getCanonicalName();
             for (java.lang.reflect.Type typeParam : pt.getActualTypeArguments()) {
                 addTypeParameter(new APIType(typeParam));
             }
         } else if (type instanceof WildcardType) {
             WildcardType wt = (WildcardType) type;
             this.name = "?";
             if (wt.getLowerBounds().length > 0) {
                 bound = BoundType.LOWER;
                 addTypeParameter(new APIType(wt.getLowerBounds()[0])); // there should be only one parameter
             } else if (wt.getUpperBounds().length > 0) {
                 bound = BoundType.UPPER;
                 addTypeParameter(new APIType(wt.getUpperBounds()[0])); // there should be only one parameter
             } else {
                 bound = BoundType.NULL;
             }
         } else if(type instanceof TypeVariable) {
             TypeVariable tv = (TypeVariable) type;
             this.name = tv.getName();
         } else if(type instanceof Class) {
             name = ((Class) type).getCanonicalName();
             if(name.endsWith("[]")) {
                 name = name.substring(0,name.length()-2);
                 array = true;
             }
         }
         else this.name = type.toString();
     }
     
     public APIType(Class type) {
         this.name = type.getName();
         this.flatName = type.getCanonicalName();
     }
 
     public APIType(GenericArrayType type) {
 
     }
 
     public APIType(ParameterizedType type) {
 
     }
 
     public APIType(TypeVariable type) {
         this.name = type.getName();
     }
 
     public APIType(WildcardType type) {
 
     }
 
     public void addTypeParameter(APIType param) {
         getTypeArgs().add(param);
     }
 
     public String getName() {
         return name;
     }
 
     public List<APIType> getTypeArgs() {
         return typeArgs;
     }
 
     public boolean isArray() {
         return array;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(getName());
         if(!getTypeArgs().isEmpty()) {
             sb.append('<');
             for(Iterator<APIType> it = getTypeArgs().iterator(); it.hasNext(); ) {
                 sb.append(it.next().toString());
                 if(it.hasNext()) {
                     sb.append(", ");
                 }
             }
             sb.append('>');
         }
 
         if(isArray()) {
             sb.append("[]");
         }
 
         return sb.toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof APIType)) return false;
 
         APIType apiType = (APIType) o;
 
         if (array != apiType.array) return false;
         if (!name.equals(apiType.name)) return false;
         if (typeArgs != null ? !typeArgs.equals(apiType.typeArgs) : apiType.typeArgs != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name.hashCode();
         result = 31 * result + (typeArgs != null ? typeArgs.hashCode() : 0);
         result = 31 * result + (array ? 1 : 0);
         return result;
     }
 
     public String getSimpleName() {
         return getName().substring(0, getName().lastIndexOf("."));
     }
 
     public String getFlatName() {
         if(flatName != null) return flatName;
         else return name;
 
     }
 
     public BoundType getBound() {
         return bound;
     }
 
     public static final APIType voidType = new APIType("void");
 
     // todo: tohle by se dalo optimalizovat, aby se to udelalo jen pri pridani noveho typu a ne pri kazdem cteni.
     public Collection<String> getTypeArgsClassNames() {
         List<String> classNames = new LinkedList<>();
         for(APIType t : typeArgs) {
             classNames.add(t.getName());
         }
         return classNames;
     }
 }
