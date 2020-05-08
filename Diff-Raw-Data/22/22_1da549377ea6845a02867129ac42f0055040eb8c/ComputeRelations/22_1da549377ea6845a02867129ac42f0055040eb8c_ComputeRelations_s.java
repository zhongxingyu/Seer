 package xsched.analysis.db;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import xsched.analysis.utils.DefUseUtils;
 
 import com.ibm.wala.classLoader.CallSiteReference;
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.ipa.callgraph.AnalysisCache;
 import com.ibm.wala.ipa.callgraph.AnalysisOptions;
 import com.ibm.wala.ipa.callgraph.impl.Everywhere;
 import com.ibm.wala.ipa.cha.ClassHierarchy;
 import com.ibm.wala.ssa.DefUse;
 import com.ibm.wala.ssa.IR;
 import com.ibm.wala.ssa.SSAArrayLoadInstruction;
 import com.ibm.wala.ssa.SSAArrayStoreInstruction;
 import com.ibm.wala.ssa.SSACheckCastInstruction;
 import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
 import com.ibm.wala.ssa.SSAGetInstruction;
 import com.ibm.wala.ssa.SSAInstruction;
 import com.ibm.wala.ssa.SSAInvokeInstruction;
 import com.ibm.wala.ssa.SSALoadMetadataInstruction;
 import com.ibm.wala.ssa.SSANewInstruction;
 import com.ibm.wala.ssa.SSAPhiInstruction;
 import com.ibm.wala.ssa.SSAPutInstruction;
 import com.ibm.wala.ssa.SSAReturnInstruction;
 import com.ibm.wala.ssa.SSAThrowInstruction;
 import com.ibm.wala.ssa.SymbolTable;
 import com.ibm.wala.ssa.SSAInstruction.Visitor;
 import com.ibm.wala.types.ClassLoaderReference;
 import com.ibm.wala.types.FieldReference;
 import com.ibm.wala.types.MethodReference;
 import com.ibm.wala.types.Selector;
 import com.ibm.wala.types.TypeName;
 import com.ibm.wala.types.TypeReference;
 
 class ComputeRelations {
 	
 	private final AnalysisCache cache;
 	private final AnalysisOptions options; 
 	
 	private final ExtensionalDatabase database;
 	private final ClassHierarchy classHierarchy;
 	
 	ComputeRelations(FillExtensionalDatabase parent) {		
 		this.cache = parent.cache;
 		this.options = parent.options;
 		
 		database = parent.database;
 		classHierarchy = parent.classHierarchy;
 		
 		addDefaultRelations();
 		
 		//work on all the classes in the hierarchy
 		for(IClass klass : parent.classHierarchy) {
 			processClass(klass);
 		}
 		
 		//for sanity checking, filter out all the array types
 		for(TypeName type : database.types) {
 			if(type.isArrayType()) {
 				TypeName elementType = type.getInnermostElementType();
 				if(elementType.isPrimitiveType()) {
 					database.primitiveArrayTypes.add(type);
 				} else {
 					assert(!elementType.isArrayType());
 					database.objectArrayTypes.add(type, elementType);
 				}				
 			}
 		}
 	}
 	
 	private void addDefaultRelations() {	
 		database.objectTypes.add(ExtensionalDatabase.theImmutableObject, TypeReference.JavaLangObject.getName());	
 		database.visitedTypes.add(TypeReference.findOrCreate(ClassLoaderReference.Application, "Lxsched/Activation").getName());
 	}
 
 	private void processClass(IClass klass) {		
		System.out.println("computing relations for class " + klass.getReference());
 		//**************
 		// add super classes to Assignable relation
 		IClass superKlass = klass;
 		
 		database.visitedTypes.add(klass.getName());
 		
 		while(superKlass != null) {
 			database.assignable.add(superKlass.getReference().getName(), klass.getReference().getName());
 			superKlass = superKlass.getSuperclass();
 		}
 		
 		assert(database.assignable.contains(klass.getReference().getName(), klass.getReference().getName()));
 		
 		for(IClass interf : klass.getAllImplementedInterfaces()) {
 			database.assignable.add(interf.getReference().getName(), klass.getReference().getName());
 		}
 		
 		//**************
 		// add all klass members to members relation
 		TypeReference type = klass.getReference();		
 		for(IMethod method : klass.getAllMethods()) {
 			Selector selector = method.getSelector();			
 			if( ! method.isAbstract()) {
 				database.members.add(type.getName(), selector, method);
 			}
 		}
 		
 		//***************
 		// work on declared methods
 				
 		for(IMethod method : klass.getDeclaredMethods()) {
 			if(method.isNative()) {
 				NativeMethodHandler.processMethod(database, method);
 			} else if( ! method.isAbstract()) {	
 				database.visitedMethods.add(method);
 				new MethodVisitor(method).processMethod();
 			} 
 		}
 	}
 	
 	private class MethodVisitor extends Visitor {
 		
 		private final IMethod method;
 		private final IR ir;
 		private final DefUse defUse;
 		private int currentBCIndex;
 		
 		private MethodVisitor(IMethod method) {
 			this.method = method;
 			
 			ir = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
 			defUse = cache.getSSACache().findOrCreateDU(method, Everywhere.EVERYWHERE, options.getSSAOptions());
 		}
 		
 		private void addToVariableTypes(int variable) {
 			Set<TypeReference> types = DefUseUtils.definedReferenceTypes(ir, defUse, variable);
 			for(TypeReference type : types) {
 				if(type.isReferenceType()) {
 					database.variableTypes.add(ir.getMethod(), variable, type.getName());
 				}
 			}
 		}
 		
 		public void processMethod() {
 			
 			/**
 			 * add constants from the symbol table
 			 */
 			SymbolTable symTab = ir.getSymbolTable();
 			for(int variable = 1; variable <= symTab.getMaxValueNumber(); variable++) {
 				if(symTab.isStringConstant(variable) || symTab.isConstant(variable)) {				
 					database.constants.add(method, variable, ExtensionalDatabase.theImmutableObject);
 					addToVariableTypes(variable);				
 				}
 			}
 			
 			/**
 			 * add method formals variables
 			 */
 			//TODO we only handle formals for java methods here, not formals for native methods!
 			//formals will contain "this" for position 0 for instance methods
 			int[] params = ir.getParameterValueNumbers();
 			for(int i = 0; i < params.length; i++) {
 				if(ir.getParameterType(i).isReferenceType()) {
 					database.formals.add(method, i, params[i]);
 					addToVariableTypes(params[i]);
 				}
 			}
 			
 			/**
 			 * visit all instructions
 			 */
 			currentBCIndex = 0;
 			for(Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext(); ) {
 				it.next().visit(this);
 				currentBCIndex++;
 			}			
 						
 		}
 		
 		/* *******************
 		 * the visitor implementation that
 		 * actually fills the extensional database
 		 * *******************/
 		
 		@Override
 		public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
 			int lhs = instruction.getDef();
 			int rhs = instruction.getArrayRef();
 			FieldReference field = ExtensionalDatabase.arrayElementField;
 			
 			if(DefUseUtils.definesReferenceType(ir, defUse, lhs)) {
 				//***************
 				// load relation
 				database.loadStatements.add(method, lhs, rhs, field);
 				addToVariableTypes(lhs);
 			} else {
 				database.primLoadStatements.add(method, rhs, field);
 			}
 		}
 	
 		@Override
 		public void visitArrayStore(SSAArrayStoreInstruction instruction) {
 			int lhs = instruction.getArrayRef();
 			FieldReference field = ExtensionalDatabase.arrayElementField;
 			
 			if(instruction.getElementType().isReferenceType()) {
 				int rhs = instruction.getValue();
 				database.storeStatements.add(method, lhs, field, rhs);
 			} else {
 				database.primStoreStatements.add(method, lhs, field);
 			}
 		}
 	
 		@Override
 		public void visitCheckCast(SSACheckCastInstruction instruction) {
 			if(DefUseUtils.definesReferenceType(ir, defUse, instruction)) {
 				int lhs = instruction.getResult();
 				int rhs = instruction.getVal();
 				database.assignStatements.add(method, lhs, rhs);
 				addToVariableTypes(lhs);
 			}
 		}
 	
 		@Override
 		public void visitGet(SSAGetInstruction instruction) {	
 			int lhs = instruction.getDef();
 			FieldReference field = instruction.getDeclaredField();
 			
 			if(DefUseUtils.definesReferenceType(ir, defUse, lhs)) {
 				//we assign to reference type
 				if(instruction.isStatic()) {			
 					database.staticLoadStatements.add(method, lhs, field);					
 				} else {
 					int rhs = instruction.getRef();
 					database.loadStatements.add(method, lhs, rhs, field);
 					
 				}
 				addToVariableTypes(lhs);
 			} else {
 				//we assign to primitive type; ignore the assignment but recore the access
 				if(instruction.isStatic()) {
 					database.staticPrimLoadStatements.add(method, field);
 				} else {
 					int rhs = instruction.getRef();
 					database.primLoadStatements.add(method, rhs, field);					
 				}				
 			}		
 		}
 	
 		@Override
 		public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
 			int variable = instruction.getException();
 			database.catchStatements.add(method, variable);
 			addToVariableTypes(variable);
 		}
 			
 		@Override
 		public void visitInvoke(SSAInvokeInstruction instruction) {
 		
 			CallSiteReference callSite = instruction.getCallSite();
 			final MethodReference targetRef = callSite.getDeclaredTarget();
 			
 			if(ActivationInfo.isHBMethod(targetRef)) {
 				assert(instruction.getNumberOfUses() == 2);
 				assert(instruction.getNumberOfDefs() == 1); //invoke always returns an exception object
 				int lhs = instruction.getUse(0);
 				int rhs = instruction.getUse(1);
 				database.arrowStatements.add(method, lhs, rhs);				
 			} else if (ActivationInfo.isScheduleMethod(targetRef)) {
 				//a call to an activation creation method is like a new statement
 				//and like a virtual call to the receiver object with the given selector and params
 				
 				//the effect of the new statement:
 				int lhs = instruction.getDef();
 				Obj newActivation = new Obj.NewObject(method, instruction.getCallSite());
 				database.objectTypes.add(newActivation, ActivationInfo.theActivationTypeName);
 				database.variableTypes.add(method, lhs, ActivationInfo.theActivationTypeName);
 				
 				//the effect of the virtual call
 				String task = ir.getSymbolTable().getStringValue(instruction.getUse(1));
 				if(task == null)
 					throw new RuntimeException("a task MUST be a string constant naming a selector such as 'something(Ljava/lang/Object;Ljava/lang/String;)V;'");
 				
 				Selector selector = Selector.make(task);
 				
 				//add to the schedule statement
 				database.scheduleStatements.add(method, currentBCIndex, lhs, newActivation, selector);
 								
 				int receiver = instruction.getUse(0);
 				database.actuals.add(method, currentBCIndex, 0, receiver);
 				
 				//at i=1 is the task name; params to the activation start at index 2
 				for(int i = 2; i < instruction.getNumberOfParameters(); i++) {
 					int param = instruction.getUse(i);
 					database.actuals.add(method, currentBCIndex, i-1, param);
 				}
 			
 			} else if (ActivationInfo.isNowMethod(targetRef)) {
 				int lhs = instruction.getDef();
 				//not sure why i don't use addVariableTypes() here... probably should...
 				database.variableTypes.add(method, lhs, ActivationInfo.theActivationTypeName);
 				database.nowStatements.add(method, lhs);
 			} else {
 				
 				if(method == null) {
 					System.err.println("Warning: didn't find method " + instruction.getDeclaredTarget() + ". Probably the Cheater ignores the receiver class. Ignoring invoke statement!");
 					return;
 				}
 				
 				//only used by static and special methods, but put it here for DRY reasons
 				IMethod target = null;
 				if(callSite.isFixed()) {
 					target = classHierarchy.resolveMethod(targetRef);				
 					if(target == null) {		
 						//System.err.println("adding " + targetRef.getSelector() + " to ignored Static invokes");						
 						database.ignoredStaticInvokes.add(method, targetRef.getDeclaringClass().getName(), targetRef.getSelector());						
 						return;
 					}
 				}
 				
 				//Note: getNumberOfParameters() contains "this" for non-static calls
 				
 				//add the invoke statements
 				if(callSite.isStatic()) {
 					database.staticClassInvokes.add(method, currentBCIndex, target);
 				} else if (callSite.isSpecial()) {
 					database.staticInstanceInvokes.add(method, currentBCIndex, target);				
 				} else {
 					assert callSite.isDispatch();
 					database.virtualInvokes.add(method, currentBCIndex, targetRef.getSelector());					
 				}
 				
 				//add the actuals
 				//a static call has no this pointer, so the params start at index 0
 				//special and virtual calls have a this pointer and the method params start at index 1
 				//method.getParameterType does the right thing but not MethodReference; but we cannot access the method here because
 				//we are in a virtual call
 				//the other parameters
 				//be careful: instruction.getNumberOfParameters() is not equal to method.getNumberOfParameters!!!
 				for(int i = 0; i < instruction.getNumberOfParameters(); i++) {
 					TypeReference paramType = callSite.isStatic()?
 													targetRef.getParameterType(i)
 													: i==0?
 															targetRef.getDeclaringClass()
 															: targetRef.getParameterType(i-1);
 					if(paramType.isReferenceType()) {
 						int param = instruction.getUse(i);
 						//System.err.println("setting actual " + i + " of method " + callSite + " to " + param + " (" + paramType + ")");
 						database.actuals.add(method, currentBCIndex, i, param);
 					}						
 				}
 				
 				//add the call site return				
 				for(int i = 0; i < instruction.getNumberOfReturnValues(); i++) {
 					if(instruction.getDeclaredResultType().isReferenceType()) {
 						int ret = instruction.getReturnValue(i);
 						database.callSiteReturns.add(method, currentBCIndex, ret);
 						addToVariableTypes(ret);
 					}
 				}
 				
 			}
 		}
 	
 		@Override
 		public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
 			int lhs = instruction.getDef();
 			
 			assert (instruction.getToken() instanceof TypeReference);
 			
 			database.constants.add(method, lhs, ExtensionalDatabase.theImmutableObject);
 			addToVariableTypes(lhs);			
 		}
 	
 		@Override
 		public void visitNew(SSANewInstruction instruction) {			
 			int lhs = instruction.getDef();
 			Obj obj = new Obj.NewObject(method, instruction.getNewSite());
 			
 			database.newStatements.add(method, lhs, obj);
 			database.objectTypes.add(obj, instruction.getNewSite().getDeclaredType().getName());
 			addToVariableTypes(lhs);			
 		}
 	
 		@Override
 		public void visitPhi(SSAPhiInstruction instruction) {
 			if(DefUseUtils.definesReferenceType(ir, defUse, instruction)) {
 				int lhs = instruction.getDef();
 				for(int i = 0; i < instruction.getNumberOfUses(); i++) {
 					int rhs = instruction.getUse(i);
 					database.assignStatements.add(method, lhs, rhs);					
 				}
 				
 				addToVariableTypes(lhs);
 			}
 		}
 		
 		@Override
 		public void visitPut(SSAPutInstruction instruction) {
 			//base.field = source
 			
 			FieldReference field = instruction.getDeclaredField();
 			if(field.getFieldType().isReferenceType()) {				
 				int rhs = instruction.getVal();			
 				if(instruction.isStatic()) {
 					database.staticStoreStatements.add(method, field, rhs);
 				} else {
 					int lhs = instruction.getRef();
 					database.storeStatements.add(method, lhs, field, rhs);
 				}				
 			} else {
 				if(instruction.isStatic()) {
 					database.staticPrimStoreStatements.add(method, field);
 				} else {
 					int lhs = instruction.getRef();
 					database.primStoreStatements.add(method, lhs, field);
 				}				
 			}			
 		}
 	
 		@Override
 		public void visitReturn(SSAReturnInstruction instruction) {
 			if(! instruction.returnsVoid() && ! instruction.returnsPrimitiveType()) {		
 				int result = instruction.getResult();
 				database.methodReturns.add(method, result);
 			}
 		}
 	
 		@Override
 		public void visitThrow(SSAThrowInstruction instruction) {
 			database.methodThrows.add(method, instruction.getException());			
 		}
 	}
 }
