 package xsched.analysis.db;
 
 import java.util.Collection;
 import java.util.Set;
 
 import xsched.analysis.utils.AnnotationsUtil;
 import xsched.analysis.utils.DefUseUtils;
 
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IField;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.ipa.callgraph.AnalysisCache;
 import com.ibm.wala.ipa.callgraph.AnalysisOptions;
 import com.ibm.wala.ipa.callgraph.impl.Everywhere;
 import com.ibm.wala.ssa.DefUse;
 import com.ibm.wala.ssa.IR;
 import com.ibm.wala.ssa.SSACheckCastInstruction;
 import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
 import com.ibm.wala.ssa.SSAGetInstruction;
 import com.ibm.wala.ssa.SSAInvokeInstruction;
 import com.ibm.wala.ssa.SSANewInstruction;
 import com.ibm.wala.ssa.SSAPhiInstruction;
 import com.ibm.wala.ssa.SSAPutInstruction;
 import com.ibm.wala.ssa.SSAInstruction.Visitor;
 import com.ibm.wala.types.FieldReference;
 import com.ibm.wala.types.MethodReference;
 import com.ibm.wala.types.TypeReference;
 import com.ibm.wala.types.annotations.Annotation;
 
 class ComputeDomains {
 	private final ExtensionalDatabase database;
 	private final AnalysisCache cache;
 	private final AnalysisOptions options;
 	
 	//keep track of how many params the longest method has
 	private int maxNumberOfParameters = 0;
 	private int maxNumberOfVariables = 0;
 	private int maxNumberOfBytecodes = 0;
 	
	private int classesProcessed;
 	ComputeDomains(FillExtensionalDatabase parent) {
 		this.database = parent.database;
 		this.cache = parent.cache;
 		this.options = parent.options;
 		
 		//add some default stuff to the domains
 		addDefaultElements();
 		
 		parent.cheater.cheatBeforeDomainComputation();
 		
		classesProcessed = 0;
 		//work on all the classes in the hierarchy
 		for(IClass klass : parent.classHierarchy) {
			classesProcessed++;
 			processClass(klass);
 		}
 		
 		//***********
 		// ParamPosition domain
 		for(int i = 0; i < maxNumberOfParameters; i++) {
 			database.paramPositions.add(i);
 		}
 		
 		//***********
 		// Variables domain
 		//variables start at 1 but i use 0 as a special id for the method section
 		for(int i = 0; i <= maxNumberOfVariables; i++) {
 			database.variables.add(i);
 		}
 		
 		//***********
 		// Bytecodes domain
 		for(int i = 0; i <= maxNumberOfBytecodes; i++) {
 			database.bytecodes.add(i);
 		}
 	}
 	
 	private void addDefaultElements() {
 		database.objects.add(ExtensionalDatabase.theImmutableObject);
 		database.fields.add(ExtensionalDatabase.arrayElementField);
 				
 	}
 	
 	private void processClass(IClass klass) {		
 		if(ActivationInfo.isActivationClass(klass)) {
 			return;
 		}
 		assert( ! (klass.getName().toString().contains("Lxsched/Activation")));
 		
		System.out.println("computing domain for class # " + classesProcessed + " " + klass.getReference());
 		
 		//********
 		// Types domain
 		database.types.add(klass.getName());
 		
 		//add classes as objects
 		//I don't think I need those because I have the global object and fields and method contain their declaring
 		//class
 		//database.objects.add(new Obj.SpecialObject(klass.getName()));
 				
 		//********
 		// Fields domain
 		for(IField field : klass.getAllInstanceFields()) {
 			//we keep even primitive fields because we might use them in datalog files for datarace checks etc
 			database.fields.add(field.getReference());
 		}
 		for(IField field : klass.getAllStaticFields()) {
 			//we keep even primitive fields because we might use them in datalog files for datarace checks etc
 			database.fields.add(field.getReference());
 		}
 		
 		// Methods and Method References
 		for(IMethod method : klass.getAllMethods()) {
 			//for ParamPosition domain
 			if(maxNumberOfParameters < method.getNumberOfParameters()) {
 				maxNumberOfParameters = method.getNumberOfParameters();
 			}
 			
 			//*******
 			// MethodReference domain
 			//if(klass.getClassLoader().getReference().equals(ClassLoaderReference.Application))
 				//System.out.println("\t... adding method " + method.getSelector());
 			
 			database.selectors.add(method.getSelector());
 			
 			//add param types to domain, just to be sure we have them
 			for(int i = 0; i < method.getNumberOfParameters(); i++) {
 				TypeReference typeRef = method.getParameterType(i);
 				if(typeRef.isReferenceType()) {
 					database.types.add(typeRef.getName());					
 				}
 			}
 			
 			//does method have a body?
 			if(! method.isAbstract()) {
 				processCallableMethod(method);
 			}
 		}
 		
 	}
 
 	private void processCallableMethod(IMethod method) {
 		//************
 		// Methods domain
 		database.methods.add(method);
 		
 		Annotation annotation = AnnotationsUtil.getAnnotation(method, ActivationInfo.theTaskAnnotationTypeName);
 		if(annotation != null) {
 			System.err.println(annotation);
 		}
 		
 		if(method.isNative()) {
 			return;
 		}
 		
 		//Method has a body
 		IR ir = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
 				
 		int numVars = ir.getSymbolTable().getMaxValueNumber();
 		if (maxNumberOfVariables < numVars) {
 			maxNumberOfVariables = numVars;
 		}
 		
 		MethodVisitor visitor = new MethodVisitor(method);
 		ir.visitAllInstructions(visitor);
 		
 		int numBytecodes = ir.getInstructions().length + visitor.numPhis;
 		if (maxNumberOfBytecodes < numBytecodes) {
 			maxNumberOfBytecodes = numBytecodes;
 		}
 			
 		
 	}
 	
 	private class MethodVisitor extends Visitor {
 		int numPhis = 0;
 		private final IMethod method;
 		private final IR ir;
 		private final DefUse defUse;
 		
 		MethodVisitor(IMethod method) {
 			this.method = method;
 			ir = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
 			defUse = cache.getSSACache().findOrCreateDU(method, Everywhere.EVERYWHERE, options.getSSAOptions());
 		}
 		
 		private void addToVariableTypes(int variable) {
 			Set<TypeReference> types = DefUseUtils.definedReferenceTypes(ir, defUse, variable);
 			for(TypeReference ref : types) {
 				if(ref.isReferenceType()) {
 					database.types.add(ref.getName());
 				}
 			}
 		}
 		
 		@Override
 		public void visitGet(SSAGetInstruction instruction) {	
 			FieldReference field = instruction.getDeclaredField();
 			database.fields.add(field);			
 			addToVariableTypes(instruction.getDef());
 		}
 		
 		@Override
 		public void visitCheckCast(SSACheckCastInstruction instruction) {
 			if(DefUseUtils.definesReferenceType(ir, defUse, instruction)) {
 				int lhs = instruction.getResult();
 				addToVariableTypes(lhs);
 			}
 		}
 		
 		@Override
 		public void visitPut(SSAPutInstruction instruction) {
 			FieldReference field = instruction.getDeclaredField();
 			database.fields.add(field);
 		}
 		
 		@Override
 		public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {		
 			int variable = instruction.getException();
 			addToVariableTypes(variable);
 		}
 		
 		@Override
 		public void visitNew(SSANewInstruction instruction) {			
 			//***********
 			//Objects domain
 			Obj creationSite = new Obj.NewObject(method, instruction.getNewSite());				
 			database.objects.add(creationSite);
 			//this shouldn't be necessary in a correct program, but when excluding packages it can happen that we don't see the class of the object
 			database.types.add(instruction.getNewSite().getDeclaredType().getName());
 		}
 		
 		@Override
 		public void visitInvoke(SSAInvokeInstruction instruction) {
 			MethodReference target = ((SSAInvokeInstruction)instruction).getDeclaredTarget();
 			
 			if(ActivationInfo.isScheduleMethod(target)) {
 				database.objects.add(new Obj.NewObject(method, instruction.getCallSite()));
 			}
 			//add selector, in case we exclude files
 			database.selectors.add(target.getSelector());
 			
 			//add the call site return				
 			for(int i = 0; i < instruction.getNumberOfReturnValues(); i++) {
 				TypeReference typeRef = instruction.getDeclaredResultType();
 				if(typeRef.isReferenceType()) {
 					database.types.add(typeRef.getName());					
 				}
 			}
 			
 			database.types.add(target.getDeclaringClass().getName());
 		}
 		
 		@Override
 		public void visitPhi(SSAPhiInstruction instruction) {
 			numPhis ++;
 		}		
 		
 	}
 	
 }
