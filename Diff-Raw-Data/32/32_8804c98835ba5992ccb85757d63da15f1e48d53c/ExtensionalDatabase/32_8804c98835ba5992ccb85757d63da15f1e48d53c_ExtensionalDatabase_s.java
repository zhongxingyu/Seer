 package xsched.analysis.db;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 import xsched.analysis.bddbddb.BinaryRelation;
 import xsched.analysis.bddbddb.Domain;
 import xsched.analysis.bddbddb.QuaternaryRelation;
 import xsched.analysis.bddbddb.QuinaryRelation;
 import xsched.analysis.bddbddb.Relation;
 import xsched.analysis.bddbddb.TernaryRelation;
 import xsched.analysis.bddbddb.UnaryRelation;
 
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.types.ClassLoaderReference;
 import com.ibm.wala.types.FieldReference;
 import com.ibm.wala.types.Selector;
 import com.ibm.wala.types.TypeName;
 
 public class ExtensionalDatabase {
 
 	//TODO: pass some estimations (based on nr. methods etc) so that we can give a rough size for the domains to make it more efficient.
 	
 	/* **************
 	 * Domains
 	 */
 	public final Domain<Obj> objects = new Domain<Obj>("Object");
 	public final Domain<Integer> bytecodes = new Domain<Integer>("BC");
 	public final Domain<Integer> variables = new Domain<Integer>("Variable");
 	public final Domain<FieldReference> fields = new Domain<FieldReference>("Field");
 	//TODO we don't use TypeReference because that defines a type as a tuple of class loader and type
 	//but class loaders are hierarchical which we cannot handle in this flat type domain.
 	//one possibility (the more correct one) would be to flatten the types by adding a type for each class loader
 	//(e.g., <Application, Ljava/lang/Object> and <Primordial, Ljava/lang/Object>)
 	//or we just use the name and don't handle all this class loader craziness correctly.
 	public final Domain<TypeName> types = new Domain<TypeName>("Type");  
 	public final Domain<IMethod> methods = new Domain<IMethod>("Method");
 	public final Domain<Selector> selectors = new Domain<Selector>("Selector");
 	public final Domain<Integer> paramPositions = new Domain<Integer>("ParamPosition");
 	
 	/* **************
 	 * special domain elements
 	 */
 	public static final FieldReference arrayElementField = FieldReference.findOrCreate(ClassLoaderReference.Primordial, "gen_array", "element", "Ljava/lang/Object");
 	
 	public static final Obj theImmutableObject = new Obj.SpecialObject("an Immutable Object");
 	
 	/* **************
 	 * Relations
 	 */
 	public UnaryRelation<IMethod> roots = new UnaryRelation<IMethod>("roots", methods, "Method0");
 	
 	/*
 	 * statements
 	 */
 	public TernaryRelation<IMethod, Integer, Obj> newStatements = 
 		new TernaryRelation<IMethod, Integer, Obj>("new", methods, variables, objects, "Method0_Variable0_Object0");
 	
 	public QuinaryRelation<IMethod, Integer, Integer, Obj, Selector> scheduleStatements = 
 		new QuinaryRelation<IMethod, Integer, Integer, Obj, Selector>("schedule", methods, bytecodes, variables, objects, selectors, "Method0_BC0_Variable0_Object0_Selector0");
 		
 	public BinaryRelation<IMethod, Integer> nowStatements = 
 		new BinaryRelation<IMethod, Integer>("now", methods, variables, "Method0_Variable0");
 		
 	public TernaryRelation<IMethod, Integer, Obj> constants = 
 		new TernaryRelation<IMethod, Integer, Obj>("constant", methods, variables, objects, "Method0_Variable0_Object0");
 	
 	//(base, field, dest) => dest = base.field
 	public QuaternaryRelation<IMethod, Integer, Integer, FieldReference> loadStatements =
 		new QuaternaryRelation<IMethod, Integer, Integer, FieldReference>("load", methods, variables, variables, fields, "Method0_Variable0_Variable1_Field0");
 	
 	//load of a primitive field; we still want to know about the access but we don't want to know the value
 	public TernaryRelation<IMethod, Integer, FieldReference> primLoadStatements =
 		new TernaryRelation<IMethod, Integer, FieldReference>("primLoad", methods, variables, fields, "Method0_Variable0_Field0");
 	
 	//(base, field, dest) => dest = base.field
 	public TernaryRelation<IMethod, Integer, FieldReference> staticLoadStatements =
 		new TernaryRelation<IMethod, Integer, FieldReference>("staticLoad", methods, variables, fields, "Method0_Variable0_Field0");
 		
 	public BinaryRelation<IMethod, FieldReference> staticPrimLoadStatements =
 		new BinaryRelation<IMethod, FieldReference>("staticPrimLoad", methods, fields, "Method0_Field0");
 	
 	//(base, field, source) => base.field = source
 	public QuaternaryRelation<IMethod, Integer, FieldReference, Integer> storeStatements =
 		new QuaternaryRelation<IMethod, Integer, FieldReference, Integer>("store", methods, variables, fields, variables, "Method0_Variable0_Field0_Variable1");
 	
 	//(base, field, source) => base.field = source
 	public TernaryRelation<IMethod, Integer, FieldReference> primStoreStatements =
 		new TernaryRelation<IMethod, Integer, FieldReference>("primStore", methods, variables, fields, "Method0_Variable0_Field0");
 	
 	public TernaryRelation<IMethod, FieldReference, Integer> staticStoreStatements =
 		new TernaryRelation<IMethod, FieldReference, Integer>("staticStore", methods, fields, variables, "Method0_Field0_Variable0");
 		
 	//(base, field, source) => base.field = source
 	public BinaryRelation<IMethod, FieldReference> staticPrimStoreStatements =
 		new BinaryRelation<IMethod, FieldReference>("staticPrimStore", methods, fields, "Method0_Field0");
 	
 	public TernaryRelation<IMethod, Integer, Integer> assignStatements =
 		new TernaryRelation<IMethod, Integer, Integer>("assign", methods, variables, variables, "Method0_Variable0_Variable1");
 	
 	public TernaryRelation<IMethod, Integer, Integer> arrowStatements =
 		new TernaryRelation<IMethod, Integer, Integer>("arrow", methods, variables, variables, "Method0_Variable0_Variable1");
 	
 	public BinaryRelation<IMethod, Integer> methodReturns =
 		new BinaryRelation<IMethod, Integer>("methodReturn", methods, variables, "Method0_Variable0");
 	
 	public BinaryRelation<IMethod, Integer> methodThrows =
 		new BinaryRelation<IMethod, Integer>("methodThrow", methods, variables, "Method0_Variable0");
 	
 	/*
 	 * Types
 	 */
 	public TernaryRelation<IMethod, Integer, TypeName> variableTypes =
 		new TernaryRelation<IMethod, Integer, TypeName>("variableType", methods, variables, types, "Method0_Variable0_Type0");
 	
 	public BinaryRelation<Obj, TypeName> objectTypes =
 		new BinaryRelation<Obj, TypeName>("objectType", objects, types, "Object0_Type0");
 	
 	public BinaryRelation<TypeName, TypeName> assignable =
 		new BinaryRelation<TypeName, TypeName>("assignable", types, types, "Type0_Type1");
 	
 	public TernaryRelation<TypeName, Selector, IMethod> members =
 		new TernaryRelation<TypeName, Selector, IMethod>("member", types, selectors, methods, "Type0_Selector0_Method0");
 	
 	/*
 	 * control flow
 	 */
 	
 	public QuaternaryRelation<IMethod, Integer, Integer, Integer> actuals =
 		new QuaternaryRelation<IMethod, Integer, Integer, Integer>("actual", methods, bytecodes, paramPositions, variables, "Method0_BC0_ParamPosition0_Variable0");
 	
 	public TernaryRelation<IMethod, Integer, Integer> formals =
 		new TernaryRelation<IMethod, Integer, Integer>("formal", methods, paramPositions, variables, "Method0_ParamPosition0_Variable0");
 		
 	/*
 	 * Call Sites 
 	 */
 	
 	public TernaryRelation<IMethod, Integer, IMethod> staticClassInvokes =
 		new TernaryRelation<IMethod, Integer, IMethod>("staticClassInvoke", methods, bytecodes, methods, "Method0_BC0_Method1");
 	
 	public TernaryRelation<IMethod, Integer, IMethod> staticInstanceInvokes =
 		new TernaryRelation<IMethod, Integer, IMethod>("staticInstanceInvoke", methods, bytecodes, methods, "Method0_BC0_Method1");
 	
 	public TernaryRelation<IMethod, Integer, Selector> virtualInvokes =
 		new TernaryRelation<IMethod, Integer, Selector>("virtualInvoke", methods, bytecodes, selectors, "Method0_BC0_Selector0");
 	
 	public TernaryRelation<IMethod, Integer, Integer> callSiteReturns =
 		new TernaryRelation<IMethod, Integer, Integer>("callSiteReturn", methods, bytecodes, variables, "Method0_BC0_Variable0");
 	
 	public BinaryRelation<IMethod, Integer> catchStatements =
 		new BinaryRelation<IMethod, Integer>("callSiteCatch", methods, variables, "Method0_Variable0");
 	
 	/* *******************
 	 * Methods
 	 */
 	
 	public void domainsAreComplete() {
 		for(Relation<?> relation : relations()) {
 			relation.zero();
 		}	
 	}
 	
 	public void save(String dirName) {
 		for(Domain<?> domain : domains()) {
 			domain.save(dirName, true);
 		}
 		
 		for(Relation<?> relation : relations()) {
 			relation.save(dirName);
 		}
 		
 	}
 	
 	private ArrayList<Domain<?>> domains() {
 		ArrayList<Domain<?>> result = new ArrayList<Domain<?>>();
 		for(Field f : this.getClass().getFields()) {
 			if(Domain.class.isAssignableFrom(f.getType())) {
 				try {
 					result.add((Domain<?>) f.get(this));
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return result;
 	}
 	
 	private ArrayList<Relation<?>> relations() {
 		ArrayList<Relation<?>> result = new ArrayList<Relation<?>>();
 		for(Field f : this.getClass().getFields()) {
 			if(Relation.class.isAssignableFrom(f.getType())) {
 				try {
 					result.add((Relation<?>) f.get(this));
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return result;
 	}
 	
 }
