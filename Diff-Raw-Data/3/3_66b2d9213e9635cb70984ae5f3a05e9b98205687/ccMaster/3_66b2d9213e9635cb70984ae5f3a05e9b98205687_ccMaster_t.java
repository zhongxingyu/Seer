 package oop.JavaGrinder.cc;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import oop.ccClass;
 import oop.ccManualBlock;
 import oop.ccMethod;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 //import xtc.util.Pair;
 //import xtc.util.Melon;
 //import xtc.util.Grape;
 
 public class ccMaster extends Visitor {
 	
 	private ccClass currentClass;
 	private ccMainMethod mainMethod;
 	private LinkedList<ccClass> classList;
 	private LinkedList<String> modifierList;
 	private LinkedList<String> setInstanceVariables;
 	private String[] argumentType;
 	private String[] argumentName;
 	private ccBlock latestBlock;
 	private HashSet mangleNames;
 	private File directory;
 	private String[] currentPackage;
 	
 	
 	public ccMaster(HashSet dependencies, HashSet mangleList, File dir){
 		
 		Iterator iterate = dependencies.iterator();
 		ASTGenerator ast = new ASTGenerator();
 		mangleNames = mangleList;
 		directory = dir;
 		while (iterate.hasNext()){
 			classList = new LinkedList<ccClass>();
 			modifierList = new LinkedList<String>();
 			String nextFile = (String)iterate.next();
 			this.dispatch(ast.generateAST(nextFile));
 			try{
 				this.publishToFiles();
 			} catch (IOException e){
 				e.printStackTrace();
 			}
 		}
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
 		if(mainMethod!=null){
 			file = new File(directory.getAbsolutePath() + "/main.cc");
 			fw = new FileWriter(file);
 			out = new BufferedWriter(fw);
 			
 			//includes
 			for(int i=0; i < classList.size(); i++){
 				out.write("#include \"" + classList.get(i).getName() + ".cc\"\n");
 			}
 
 			//namespaces
 			int packageNumber = classList.get(0).getPackage().size();
 			for(int q = 0; q < packageNumber; q++){
 				out.write("namespace " + classList.get(0).getPackage().get(q)+ "{\n");
 			}
 
 			
 			out.write(mainMethod.publishDeclaration() + "{\n");
 			blockLines = mainMethod.publishBlock();
 			while(!blockLines.isEmpty()){;
 				out.write(blockLines.remove(0));
 			}
 			
 			//namespace brackets
 			for(int q = 0; q < packageNumber; q++){
 				out.write("}\n");
 			}
 			
 			out.write("}\n");
 			out.close();
 			mainMethod = null;
 		}
 		
 		
 		for(int i=0; i < classList.size(); i++){
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
 			for(int j=0; j < setInstanceVariables.size(); j++){
 				out.write(setInstanceVariables.get(j) + ";\n");
 			}
 			
 			out.write("\n");
 			
 			for(int j=0; j < classList.get(i).getConstructorCount(); j++){
 				out.write(classList.get(i).getConstructorAtIndex(j).publishDeclaration() + " {\n");
 				blockLines = classList.get(i).getConstructorAtIndex(j).publishBlock();
 				while(!blockLines.isEmpty()){
 					out.write(blockLines.remove(0));
 				}
 				out.write("}\n");
 			}
 			
 			for(int j=0; j < classList.get(i).getMethodCount(); j++){
 				out.write(classList.get(i).getMethodAtIndex(j).publishDeclaration() + " {\n");
 				blockLines = classList.get(i).getMethodAtIndex(j).publishBlock();
 				while(!blockLines.isEmpty()){
 					out.write(blockLines.remove(0));
 				}
 				out.write("}\n");
 			}
 			
 			//namespace brackets
 			for(int q = 1; q < packageNumber; q++){
 				out.write("}\n");
 			}
 			
 			out.write("}\n");
 			out.close();
 		}
 	}
 	
 	public ccMainMethod getMainMethod(){
 		return mainMethod;
 	}
 	
 	public LinkedList<ccClass> getClassList(){
 		return classList;
 	}
 	
 
 	public void visitCompilationUnit(GNode n){
 		visit(n);
 		/* It's the glorious visitor-within-a-visitor that makes the blocks! It's the blocker! */
 		new Visitor() {
 			private int constructorCounter;
 			private int methodCounter;
 			
 			public void visitClassDeclaration(GNode n){
 				String name = (String)n.getString(1);
 				for(int i=0; i < classList.size(); i++){
 					if(name == classList.get(i).getName()){
 						currentClass = classList.get(i);
 					}
 				}
 				constructorCounter = 0;
 				methodCounter = 0;
 				visit(n);
 				addDefaultMethods(currentClass);
 			}
 			public void visitConstructorDeclaration(GNode n){
 				visit(n);
 				currentClass.getConstructorAtIndex(constructorCounter).setBlock(latestBlock);
 				constructorCounter++;
 			}
 			
 			public void visitMethodDeclaration(GNode n){
 				String name = (String)n.getString(3);
 				visit(n);
 				if(name.matches("main")){
 					mainMethod.setBlock(latestBlock);
 				}
 				else{
 					currentClass.getMethodAtIndex(methodCounter).setBlock(latestBlock);
 					methodCounter++;
 				}
 			}
 			public void visitBlock (GNode n){
 				latestBlock = new ccBlock(n, currentClass.getFields());
 			}
 			
 			public void addDefaultMethods(ccClass clas){
 				ccManualBlock deleteBlock = new ccManualBlock();
 				deleteBlock.addCustomLine("  delete __this;");
 				ccMethod delete = new ccMethod("__delete", clas, "public", "void", new String[0], new String[0]);
 				delete.setBlock(deleteBlock);
 				delete.changeThisToPointer();
 				clas.addMethod(delete);
 			}
 			
 			public void visit(Node n) {
 				for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 			}
 		}.dispatch(n);
 	}
 	public void visitClassDeclaration(GNode n){
 		setInstanceVariables = new LinkedList<String>();
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
 		visit(n);
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
 		currentClass.addField(name, type);
 		if(null != n.getNode(2).getNode(0).get(2)){
			ccDeclaration declarationStatement = new ccDeclaration(n);
	        setInstanceVariables.add(currentClass.get_Name() + "::" + " " + declarationStatement.publish() + "\n");
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
 			argumentType[i] = param.getNode(i).getNode(1).getNode(0).getString(0);
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
 			returnType = n.getNode(2).getNode(0).getString(0);
 		}
 		
 		Node param = n.getNode(4);
 		argumentType = new String[param.size()];
 		argumentName = new String[param.size()];
 		for(int i = 0; i < param.size(); i++){
 			argumentType[i] = param.getNode(i).getNode(1).getNode(0).getString(0);
 			argumentName[i] = param.getNode(i).getString(3);
 		}
 		if(name.matches("main")){
 			mainMethod = new ccMainMethod(currentClass, access, returnType, argumentType, argumentName, isStatic);
 		}
 		else{
 			currentClass.addMethod(new ccMethod(name, currentClass, access, returnType, argumentType, argumentName, isStatic));
 		}
 	}
 	public void visitModifier(GNode n){
 		for(Object s: n){
 			if (s instanceof String)
 				modifierList.add((String)s);
 		}
 	}
 
 	
 	public void visit(Node n) {
 		for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 	}
 }
