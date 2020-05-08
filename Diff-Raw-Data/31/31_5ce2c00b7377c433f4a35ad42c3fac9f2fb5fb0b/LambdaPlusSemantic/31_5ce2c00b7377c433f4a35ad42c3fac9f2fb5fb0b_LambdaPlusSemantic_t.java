 package languish.lib.bootstrap.lambdaplus;
 
 import java.util.List;
 import java.util.Map;
 
 import languish.base.Term;
 import languish.base.Terms;
 import languish.parsing.api.SemanticModule;
 import languish.parsing.api.SemanticModules;
 import languish.serialization.StringTreeSerializer;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.hjfreyer.util.Tree;
 
 public class LambdaPlusSemantic {
 
 	public static final Map<String, Function<String, Object>> IDENTITY_LEAF_RULES =
 			SemanticModules.getIdentityLeafRules(ImmutableList.of(
 					"ABS",
 					"APP",
 					"CONS",
 					"CAR",
 					"CDR",
 					"EQUALS",
 					"REF",
 					"NULL",
 					"IMPORT_DIRECTIVE",
 					"INTEGER_LIT",
 					",",
 					";;"));
 
 	public static final Map<String, Function<String, Object>> LEAF_RULES =
 			ImmutableMap.<String, Function<String, Object>> builder() //
 					.putAll(IDENTITY_LEAF_RULES)
 					.put("STRING_LIT", new Function<String, Object>() {
 						@Override
 						public Object apply(String arg) {
 							return arg.substring(1, arg.length() - 1);
 						}
 					})
 					.build();
 
 	public static final Map<String, Function<List<Object>, Object>> EMPTY_RULES =
 			SemanticModules.getEmptyListLeafRules(ImmutableList.of(
 					"EMPTY_IMPORT",
 					"IMPORT_TAIL_END"));
 
 	public static final Map<String, Function<List<Object>, Object>> INODE_RULES =
 			ImmutableMap.<String, Function<List<Object>, Object>> builder() //
 					.putAll(EMPTY_RULES)
 					.put(
 							"LambdaPlusGrammar.COMPILATION_UNIT",
 							new Function<List<Object>, Object>() {
 								@SuppressWarnings("unchecked")
 								@Override
 								public Object apply(List<Object> arg) {
 									List<String> imports = (List<String>) arg.get(0);
 									Term term = (Term) arg.get(1);
 
 									return Tree.inode(
 											Tree.<String> copyOf(imports),
 											StringTreeSerializer.serialize(term));
 								}
 							})
 					.put("IMPORT_STATEMENT", new Function<List<Object>, Object>() {
 						@SuppressWarnings("unchecked")
 						@Override
 						public Object apply(List<Object> arg) {
 							String car = (String) arg.get(1);
 							List<String> cdr = (List<String>) arg.get(2);
 
 							return ImmutableList.builder().add(car).addAll(cdr).build();
 						}
 					})
 					.put("IMPORT_TAIL_CONT", new Function<List<Object>, Object>() {
 						@SuppressWarnings("unchecked")
 						@Override
 						public Object apply(List<Object> arg) {
 							String car = (String) arg.get(1);
 							List<String> cdr = (List<String>) arg.get(2);
 
 							return ImmutableList.builder().add(car).addAll(cdr).build();
 						}
 					})
 					.put("ABS_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							Term arg1 = (Term) arg.get(2);
 
 							return Terms.abs(arg1);
 						}
 					})
 					.put("APP_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							Term arg1 = (Term) arg.get(2);
 							Term arg2 = (Term) arg.get(3);
 
 							return Terms.app(arg1, arg2);
 						}
 					})
 					.put("CONS_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							Term arg1 = (Term) arg.get(2);
 							Term arg2 = (Term) arg.get(3);
 
 							return Terms.cons(arg1, arg2);
 						}
 					})
 					.put("CAR_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							Term cons = (Term) arg.get(2);
 
 							return Terms.car(cons);
 						}
 					})
 					.put("CDR_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							Term cons = (Term) arg.get(2);
 
 							return Terms.cdr(cons);
 						}
 					})
 					.put("IS_PRIMITIVE_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							Term arg1 = (Term) arg.get(2);
 
 							return Terms.isPrimitive(arg1);
 						}
 					})
 					.put("NATIVE_APPLY_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							String name = (String) arg.get(2);
 							Term arg1 = (Term) arg.get(3);
 
 							return Terms.nativeApply(name, arg1);
 						}
 					})
 					.put("REF_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							int ref = Integer.parseInt((String) arg.get(2));
 
 							return Terms.ref(ref);
 						}
 					})
 					.put("NULL_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							return Terms.NULL;
 						}
 					})
 					.put("STRING_LIT_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							return Terms.primObj(arg.get(0));
 						}
 					})
 					.put("INT_LIT_TERM", new Function<List<Object>, Object>() {
 						@Override
 						public Object apply(List<Object> arg) {
 							return Terms.primObj(Integer.parseInt((String) arg.get(0)));
 						}
 					})
 					.build();
 
	public static final SemanticModule LAMBDA_PLUS_SEMANTIC = new SemanticModule(
			LEAF_RULES,
			INODE_RULES);
 
 }
