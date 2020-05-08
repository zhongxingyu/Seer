 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import soot.Modifier;
 import soot.Scene;
 import soot.SootClass;
 import soot.SootMethod;
 import soot.SootMethodRef;
 import soot.Trap;
 import soot.Unit;
 import soot.jimple.InvokeExpr;
 import soot.jimple.Stmt;
 import soot.jimple.internal.JThrowStmt;
 import soot.options.Options;
 import soot.toolkits.graph.TrapUnitGraph;
 import soot.toolkits.graph.UnitGraph;
 
 import com.thoughtworks.xstream.XStream;
 
 import entities.*;
 import entities.Exception;
 
 public class Main {
 	public static void main(String[] args) throws java.lang.Exception {
 		if (args.length != 3) {
 			System.out.println("Usage: eflow <process-dir> <project-name> <project-version>");
 			System.exit(1);
 		}
 
 		String dir = args[0];
 		String projectName = args[1];
 		String projectVersion = args[2];
 
 		new Assembly(projectName, projectVersion, new Date(), "Java");
 
 		List<String> process_dirs = new LinkedList<String>();
 
 		process_dirs.add(dir);
 
 		Options.v().set_allow_phantom_refs(true);
 		Options.v().set_process_dir(process_dirs);
 		Options.v().set_soot_classpath(dir);
 		Options.v().set_prepend_classpath(true);
 
 		Scene.v().loadNecessaryClasses();
 		run(); //processa o bytecode e cria as entidades
 
 		String xml = getXML();
 
 		// Escreve no arquivo output.xml
 		FileOutputStream fos = new FileOutputStream(projectName + "-" + projectVersion + ".xml");
 		fos.write(xml.getBytes());
 		fos.flush();
 		fos.close();
 	}
 	
 	public static void run() {
 		Assembly assembly = Assembly.getInstance();
 		
 		//itera nas classes
 		for (Iterator<SootClass> klassIt = Scene.v().getApplicationClasses().iterator(); klassIt.hasNext();) {
 			final SootClass klass = (SootClass) klassIt.next();
 
 			if (klass.isPhantom())
 				continue;
 
 			Type type = new Type(assembly, klass.getName(), getTypeKind(klass));
 
 			List<SootMethod> methods = klass.getMethods();
 			//itera nos métodos
 			for (Iterator<SootMethod> methodsIt = methods.iterator(); methodsIt.hasNext(); ) {
 				SootMethod sootMethod = (SootMethod) methodsIt.next();
 
 				Method method = new Method(type, sootMethod.getName(), sootMethod.getSignature(), getVisibility(sootMethod.getModifiers()));
 				type.addMethod(method);
 
 				if (!sootMethod.isConcrete())
 					continue;
 
 				try {
 					sootMethod.retrieveActiveBody();
 				} catch (java.lang.Exception e) {
 					System.out.println("Error retrieving active body on " + type.getName() + "#" + method.getFullName());
 					e.printStackTrace(System.out);
 					continue;
 				}
 
 				ArrayList<Unit> listTry= new ArrayList<Unit>();
 				ArrayList<Unit> listFinally= new ArrayList<Unit>();
 				ArrayList<Unit> units = new ArrayList<Unit>();
 
 				UnitGraph graph = new TrapUnitGraph(sootMethod.getActiveBody());
 				for (Iterator<Unit> graphIt = graph.iterator(); graphIt.hasNext();) { //itera nos statements atrás de throws
 					Unit unit = graphIt.next();
 					units.add(unit);
 					if (unit instanceof JThrowStmt) {
 						JThrowStmt throwUnit = (JThrowStmt) unit;
 						String throwType = throwUnit.getOp().getType().toString();
 						
 						if (!throwType.equals("java.lang.Throwable")){ //não é um throw genérico usado pelo compilador
 							new Throw(method, throwType, units.indexOf(unit));
 						}
 					}
 					
 					//cria os MethodCall com targets ficticios
 					if (unit instanceof Stmt) {
 						Stmt stmt = (Stmt) unit;
 						
 						if (stmt.containsInvokeExpr()) {
 							InvokeExpr invokeExpr;
 							invokeExpr = stmt.getInvokeExpr();
 							SootMethodRef methodRef = invokeExpr.getMethodRef();
 
							FakeMethod fakeTarget = new FakeMethod(methodRef.declaringClass().getName(), methodRef.name());
 
 							MethodCall.createWithFakeTarget(assembly, method, fakeTarget, units.indexOf(unit));
 						}
 					}
 				}
 
 				for (Iterator<Trap> i = sootMethod.getActiveBody().getTraps().iterator(); i.hasNext();) {
 					Trap trap = i.next();
 
 					if (trap.getException().getName().equals("java.lang.Throwable")) { //é um finally
 						if (!listFinally.contains(trap.getHandlerUnit())) {
 							listFinally.add(trap.getHandlerUnit());
 						}
 						if (trap.getHandlerUnit() == trap.getEndUnit()) { //é um novo try, sem catchs
 							listTry.add(trap.getEndUnit());
 							new Try(method, units.indexOf(trap.getBeginUnit()), units.indexOf(trap.getEndUnit())); //obs: início e fim do try é errado
 						}
 					} else { //é um catch
 						if (!listTry.contains(trap.getEndUnit())) { //é um novo try de um grupo de catchs
 							listTry.add(trap.getEndUnit());
 							new Try(method, units.indexOf(trap.getBeginUnit()), units.indexOf(trap.getEndUnit()));  //obs: início e fim do try é errado
 						}
 						new Catch(method, trap.getException().getName(), units.indexOf(trap.getBeginUnit()), units.indexOf(trap.getEndUnit()));  //obs: início e fim do try é errado
 					}
 				}
 				
 				method.setQtdFinally(listFinally.size());
 	        }
 		}
 
 		MethodCall.trackActualTargets();
 	}
 	
 	public static String getVisibility(int modifiers) {
 		
 		if ((modifiers | soot.Modifier.PUBLIC) == modifiers) {
 			return "public";
 		} else if ((modifiers | soot.Modifier.PRIVATE) == modifiers) {
 			return "private";
 		} else if ((modifiers | soot.Modifier.PROTECTED) == modifiers) {
 			return "protected";
 		} else {
 			return "unknown";
 		} 
 	}
 
 	public static String getTypeKind(SootClass klass) {
 		String str = "";
 
 		if (klass.isAbstract())
 			str += "Abstract|";
 
 		if (!klass.isInterface())
 			str += "Class|";
 
 		if (klass.isInterface())
 			str += "Interface|";
 
 		if (Modifier.isStatic(klass.getModifiers()))
 			str += "Static|";
 
 		if (Modifier.isEnum(klass.getModifiers()))
 			str += "Enum|";
 
 		return str;
 	}
 
 	public static String getXML() throws java.lang.Exception {
 	    XStream xstream = new XStream();
 	    xstream.autodetectAnnotations(true);
 	    xstream.setMode(XStream.ID_REFERENCES);
 
 	    xstream.alias("assembly", Assembly.class);
 	    xstream.alias("type", Type.class);
 	    xstream.alias("method", Method.class);
 	    xstream.alias("methodCall", MethodCall.class);
 	    xstream.alias("methodException", MethodException.class);
 	    xstream.alias("methodException", Try.class);
 	    xstream.alias("methodException", Throw.class);
 	    xstream.alias("methodException", Catch.class);
 	    xstream.alias("exception", Exception.class);
 	    return xstream.toXML(Assembly.getInstance());
 	}
 }
