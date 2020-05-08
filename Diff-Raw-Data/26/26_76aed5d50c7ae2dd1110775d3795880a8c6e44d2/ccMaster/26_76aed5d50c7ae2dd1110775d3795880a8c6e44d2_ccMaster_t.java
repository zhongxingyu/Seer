 package oop;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import oop.ccClass;
 import oop.ccManualBlock;
 import oop.ccMethod;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 public class ccMaster extends Visitor {
 	
 	private ccClass currentClass;
 	private int mainCounter;
 	private LinkedList<ccMainMethod> mainMethodList;
 	private LinkedList<ccClass> classList;
 	private LinkedList<String> modifierList;
 	private String[] argumentType;
 	private String[] argumentName;
 	private ccBlock latestBlock;
 	private HashSet mangleNames;
 	private File directory;
 	private String[] currentPackage;
 	private LinkedList<LinkedList<String>> printToConstructors;
 	private LinkedList<String> printToCurrentConstructor;
 	
 	
 	public ccMaster(LinkedList<String> dependencies, HashSet mangleList, File dir){
 		ASTGenerator ast = new ASTGenerator();
 		mangleNames = mangleList;
 		directory = dir;
 		classList = new LinkedList<ccClass>();
 		mainMethodList = new LinkedList<ccMainMethod>();
 		javaLangMethods();
 		printToConstructors = new LinkedList<LinkedList<String>>();
 		for (String next : dependencies){
 			modifierList = new LinkedList<String>();
 			this.dispatch(ast.generateAST(next));
 		}
 		currentClass = classList.get(2);
 		mainCounter = 0;
 		for (int count=0; count<dependencies.size(); count++){
 			modifierList = new LinkedList<String>();
 			String nextFile = dependencies.get(count);
 			printToCurrentConstructor = printToConstructors.get(count);
 			new Visitor() {
 				private int constructorCounter;
 				private boolean constructorFlag;
 				private boolean addedConstructor;
 				private int methodCounter;
 				private HashMap<String, String> parameterNames;
 				
 				public void visitClassDeclaration(GNode n){
 					String name = (String)n.getString(1);
 					for(int i=0; i < classList.size(); i++){
 						
 						if(name.equals(classList.get(i).getName())){
 							currentClass = classList.get(i);
 						}
 					}
 					constructorCounter = 1;
 					if(currentClass.constructorOverWritten){
 						constructorCounter = 0;
 					}
 					constructorFlag = false;
 					methodCounter = 0;
 					addedConstructor = false;
 					visit(n);
 				}
 				public void visitConstructorDeclaration(GNode n){
 					parameterNames = new HashMap<String, String>();
 					constructorFlag = true;
 					visit(n);
 					constructorFlag = false;
 					currentClass.getConstructorAtIndex(constructorCounter).setBlock(latestBlock);
 					constructorCounter++;
 				}
 				
 				public void visitMethodDeclaration(GNode n){
 					String name = (String)n.getString(3);
 					parameterNames = new HashMap<String, String>();
 					visit(n);
 					if(name.trim().equals("main")){
 						latestBlock.addLineFront(currentClass.getName() + " __this = new __" + currentClass.getName() + "();\n" );
 						mainMethodList.get(mainCounter).setBlock(latestBlock);
 						mainCounter++;
 					}
 					else{
 						currentClass.getMethodAtIndex(methodCounter).setBlock(latestBlock);
 						methodCounter++;
 					}
 				}
 				
 				public void visitFormalParameter(GNode n){
 					parameterNames.put(n.getString(3),new ccStatement(n.getGeneric(1), latestBlock).publish());
 				}
 				public void visitBlock (GNode n){
 					latestBlock = new ccBlock(n, currentClass.getFields(), parameterNames, classList, currentClass.getName(), constructorFlag);
 					if(constructorFlag){
 						for(String iter : printToCurrentConstructor){
 							if(!latestBlock.hasDeclared(iter.substring(0, iter.indexOf('=')).trim()))		latestBlock.addLineFront(iter);
 						}
 					}
 				}
 				public void visit(Node n) {
 					for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 				}
 			}.dispatch(ast.generateAST(nextFile));
 		}
 		
 		try{
 			this.publishToFiles();
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 
 	}
 	/**
 	 * Adds Object, String and Class into the class list data structure.
 	 */
 	private void javaLangMethods(){
 		//Object
 		currentClass = new ccClass("Object", "public", false);
 		classList.add(currentClass);
 		currentClass.addInheritedMethods();
 		
 		//String
 		currentClass = new ccClass("String", "public", false);
 		classList.add(currentClass);
 		argumentType = new String[0];
 		argumentName = new String[0];
 		currentClass.addMethod(new ccMethod("length", currentClass, "public", "int32_t", argumentType, argumentName));
 		currentClass.addMethod(new ccMethod("charAt", currentClass, "public", "char", argumentType, argumentName));
 		currentClass.addInheritedMethods();
 		
 		//Class
 		currentClass = new ccClass("Class", "public", false);
 		classList.add(currentClass);
 		argumentType = new String[0];
 		argumentName = new String[0];
 		currentClass.addMethod(new ccMethod("getName", currentClass, "public", "Class", argumentType, argumentName));
 		currentClass.addMethod(new ccMethod("getSuperclass", currentClass, "public", "String", argumentType, argumentName));
 		currentClass.addMethod(new ccMethod("isPrimitive", currentClass, "public", "Class", argumentType, argumentName));
 		currentClass.addMethod(new ccMethod("isArray", currentClass, "public", "String", argumentType, argumentName));
 		currentClass.addMethod(new ccMethod("getComponentType", currentClass, "public", "Class", argumentType, argumentName));
 		argumentType = new String[1];
 		argumentType[0] = "Object";
 		argumentName = new String[1];
 		argumentName[0] = "o";
 		currentClass.addMethod(new ccMethod("isInstance", currentClass, "public", "String", argumentType, argumentName));
 		currentClass.addInheritedMethods();
 	}
 	/**
 	 * Printy Thingy
 	 * 
 	 * @throws IOException
 	 */
 	public void publishToFiles() throws IOException{
 		LinkedList<String> blockLines;
 		File file;
 		FileWriter fw;
 		BufferedWriter out;
 		for(ccMainMethod mainMethod : mainMethodList){
 			
 			file = new File(directory.getAbsolutePath() + "/main_" + mainMethod.getParentClass().getName() + ".cc");
 			fw = new FileWriter(file);
 			out = new BufferedWriter(fw);
 			
 			//includes
 			for(int i=3; i < classList.size(); i++){
 				out.write("#include \"" + classList.get(i).getName() + ".h\"\n");
 			}
 			out.write("#include \"java_lang.h\"\n");
 
 			//usingNameSpace
 			HashSet<String> usingNameSpaceList = new HashSet<String>();
 			for(int q = 3; q < classList.size(); q++){
 				String usingNameSpace = "using namespace ";
 				for(int i = 0; i<classList.get(q).getPackage().size(); i++){
 					if(i>0){usingNameSpace += "::";}
 					usingNameSpace += classList.get(q).getPackage().get(i);
 				}
 			usingNameSpaceList.add(usingNameSpace + ";\n");
 			}
 			usingNameSpaceList.add("using namespace java::lang;\n");
 			for(String s : usingNameSpaceList){
 				out.write(s);
 			}
 			
 			out.write(mainMethod.publishDeclaration() + "\n");
 			blockLines = mainMethod.publishBlock();
 			while(!blockLines.isEmpty()){;
 				out.write(blockLines.remove(0));
 			}
 			
 			out.write("\n");
 			out.close();
 		}
 		
 		for(int i=3; i < classList.size(); i++){
 			file = new File(directory.getAbsolutePath() + "/" + classList.get(i).getName() + ".cc");
 			fw = new FileWriter(file);
 			out = new BufferedWriter(fw);
 			
 			//includes
 			out.write("#include \"" + classList.get(i).getName() + ".h\"\n");
 			out.write("#include \"java_lang.h\"\n");
 			out.write("#include \"ptr.h\"\n");
 			
 			//namespaces
 			int packageNumber = classList.get(i).getPackage().size();
 			for(int q = 0; q < packageNumber; q++){
 				out.write("namespace " + classList.get(i).getPackage().get(q)+ "{\n");
 			}
 			
 			out.write("\n");
 			
 			//class variables that are set as they are declared
 			for(int j=0; j < classList.get(i).getInstanceVariables().size(); j++){
 				out.write(classList.get(i).getInstanceVariables().get(j));
 			}
 			
 			out.write("\n");
 			
 			for(int j=0; j < classList.get(i).getConstructorCount(); j++){
 				out.write(classList.get(i).getConstructorAtIndex(j).publishDeclaration() + " \n");
 				blockLines = classList.get(i).getConstructorAtIndex(j).publishBlock();
 				while(!blockLines.isEmpty()){
 					out.write(blockLines.remove(0));
 				}
 				out.write("\n");
 			}
 			
 			for(int j=0; j < classList.get(i).getMethodCount(); j++){
 				out.write(classList.get(i).getMethodAtIndex(j).publishDeclaration() + " \n");
 				blockLines = classList.get(i).getMethodAtIndex(j).publishBlock();
 				while(!blockLines.isEmpty()){
 					out.write(blockLines.remove(0));
 				}
 				out.write("\n");
 			}
 			
 			String qualifiedPackageName = "";
 			for(int q = 0; q < packageNumber; q++){
 				qualifiedPackageName += classList.get(i).getPackage().get(q) + ".";
 			}
 			
 			//__class
 			String superClass = classList.get(i).getSuperClass().getName();
			
			out.write("Class " + classList.get(i).get_Name()+ "::__class() {\n");
			out.write("  " + superClass + " __superClass = new __" + superClass);
			if(superClass.equals("String"))
				out.write("(\"\");\n");
			else
				out.write("();\n");
		    out.write("  static Class k = \n" +
 		        "  new __Class(__rt::literal(\""+ qualifiedPackageName + classList.get(i).getName() + "\"), __superClass->__vptr->getClass(__superClass), __rt::null(), false);\n" +
 		        "  return k;\n" +
 		      	"}\n");
 			
 			//__vtable
 			out.write(classList.get(i).get_Name() + "_VT" + " " + classList.get(i).get_Name() + "::__vtable;\n");
 			
 			//namespace brackets
 			for(int q = 1; q < packageNumber; q++){
 				out.write("}\n");
 			}
 			
 			out.write("}\n");
 			out.close();
 		}
 	}
 	
 	public LinkedList<ccMainMethod> getMainMethodList(){
 		return mainMethodList;
 	}
 	
 	public LinkedList<ccClass> getClassList(){
 		return classList;
 	}
 	
 
 	public void visitCompilationUnit(GNode n){
 		visit(n);
 		printToConstructors.add(new LinkedList<String>());
 		for(int i=3; i < classList.size(); i++){
 			classList.get(i).addInheritedMethods();
 			LinkedList<ccVariable> vars = new LinkedList(classList.get(i).getFields().values());
 			for(ccVariable nextVar : vars){
 				if(nextVar.somethingToDeclare()){
 					printToConstructors.getLast().add(nextVar.declare());
 				}
 			}
 		}
 		/* It's the glorious visitor-within-a-visitor that makes the blocks! It's the blocker! */
 		
 	}
 	public void visitClassDeclaration(GNode n) throws Exception{
 		String name = (String)n.getString(1);
 		String access = "public";
 		boolean isStatic = false;
 		dispatch(n.getNode(0));
 		for(int i = 0; i < modifierList.size(); i++){
 			if(modifierList.get(i).matches("public|private|protected")){
 				access = modifierList.get(i);
 			}
 			else if(modifierList.get(i).matches("static")){
 				isStatic = true;
 			}
 		}
 		modifierList.clear();
 		classList.add(new ccClass(name, access, isStatic));
 		currentClass = classList.getLast();
 		currentClass.addPackage(currentPackage);
 		if(null != n.getNode(3)){
 			String extension = n.getNode(3).getNode(0).getNode(0).getString(0);
 			boolean extensionCheck = false;
 			for(int i = 3; i < classList.size()-1; i++){
 				if(classList.get(i).getName().contentEquals(extension)){
 					currentClass.addSuper(classList.get(i));
 					extensionCheck = true;
 					break;
 				}
 			}
 			if(!(extensionCheck || extension.contentEquals("Object"))){
 				System.out.println("Incorrect argument ordering: file containing class \"" + name + 
 						"\" should proceed file containing class \"" + extension + "\".");
 				throw new Exception();
 			}
 		}
 		else{
 			currentClass.addSuper(classList.get(0));
 		}
 		visit(n);
 		addDefaultMethods(currentClass);
 	}
 	
 	public void visitPackageDeclaration(GNode n){
 		Node qualifiedIdentifier = n.getNode(1);
 		currentPackage = new String[qualifiedIdentifier.size()];
 		for(int i = 0; i < qualifiedIdentifier.size(); i++){
 			currentPackage[i] = (qualifiedIdentifier.getString(i));
 		}
 	}
 	
 	public void visitFieldDeclaration(GNode n){
 		
 		String name = (String)n.getNode(2).getNode(0).getString(0);
 		String type = (String)n.getNode(1).getNode(0).getString(0);
 		ccDeclaration declarationStatement = new ccDeclaration(n, null);
 		boolean isArray = (declarationStatement.getTypes().contains("__rt::Array"));
 		boolean isStatic = declarationStatement.getModifiers().contains("static");
 		currentClass.addField(name, type, isStatic);
 		String isConst = "";
 		if(declarationStatement.getModifiers().contains("const")){isConst="const";}
 		if(isStatic){
 				currentClass.addInstanceVariable(isConst +" " + declarationStatement.getTypes() + " " + currentClass.get_Name() + "::" + declarationStatement.publishShort() + "\n");
 		}
 		if((!isStatic)&&(declarationStatement.declaresToValue())){
 			currentClass.findField(name).setDeclarationStatement(declarationStatement.publishShort());
 		}
 	}
 	public void visitConstructorDeclaration(GNode n){
 		String name = (String)n.getString(2);
 		String access = "public";
 		dispatch(n.getNode(0));
 		for(int i = 0; i < modifierList.size(); i++){
 			if(modifierList.get(i).matches("public|private|protected")){
 				access = modifierList.get(i);
 			}
 		}
 		modifierList.clear();
 		Node param = n.getNode(3);
 		argumentType = new String[param.size()];
 		argumentName = new String[param.size()];
 		for(int i = 0; i < param.size(); i++){
 			argumentType[i] = new ccStatement(param.getNode(i).getGeneric(1)).publish();
 			argumentName[i] = param.getNode(i).getString(3);
 		}
 		currentClass.addConstructor(new ccConstructor(name, access, argumentType, argumentName, currentClass));
 	}
 	public void visitMethodDeclaration(GNode n){
 		String name = (String)n.getString(3);
 		String access = "protected";
 		String returnType = "void";
 		boolean isStatic = false;
 		dispatch(n.getNode(0));
 		for(int i = 0; i < modifierList.size(); i++){
 			if(modifierList.get(i).matches("public|private|protected")){
 				access = modifierList.get(i);
 			}
 			else if(modifierList.get(i).matches("static")){
 				isStatic = true;
 			}
 		}
 		modifierList.clear();
 		if(n.getNode(2).hasName("VoidType")){ /* nope, we already good */}
 		else{
 			returnType = new ccStatement(n.getGeneric(2)).publish();
 		}
 		
 		Node param = n.getNode(4);
 		argumentType = new String[param.size()];
 		argumentName = new String[param.size()];
 		for(int i = 0; i < param.size(); i++){
 			argumentType[i] = new ccStatement(param.getNode(i).getGeneric(1)).publish();
 			argumentName[i] = param.getNode(i).getString(3);
 		}
 		if(name.matches("main")){
 			mainMethodList.add(new ccMainMethod(currentClass, access, returnType, argumentType, argumentName, isStatic));
 		}
 		else{
 			
 			ccMethod newMethod = new ccMethod(name, currentClass, access, returnType, argumentType, argumentName, isStatic);
 			Iterator manIterate = mangleNames.iterator();
 			while (manIterate.hasNext()){
 				String mangleTest = currentClass.getName() + "^" + name;
 				if(manIterate.next().toString().contains(mangleTest))	newMethod.mangleName();
 			}
 			currentClass.addMethod(newMethod);
 		}
 	}
 	public void visitModifier(GNode n){
 		for(Object s: n){
 			if (s instanceof String)
 				modifierList.add((String)s);
 		}
 	}
 
 	public void addDefaultMethods(ccClass clas){
 		ccManualBlock deleteBlock = new ccManualBlock();
 		deleteBlock.addCustomLine("{  delete __this;}");
 		ccMethod delete = new ccMethod("__delete", clas, "public", "void", new String[0], new String[0]);
 		delete.setBlock(deleteBlock);
 		delete.changeThisToPointer();
 		clas.addMethod(delete);
 	}
 	
 	public void visit(Node n) {
 		for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 	}
 }
