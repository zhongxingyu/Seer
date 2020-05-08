 package transformers;
 import java.util.List;
 import java.util.Map;
 
 import polyglot.ast.Instanceof;
 
 import soot.Body;
 import soot.BodyTransformer;
 import soot.RefType;
 import soot.Scene;
 import soot.ShortType;
 import soot.SootClass;
 import soot.SootField;
 import soot.SootFieldRef;
 import soot.SootMethod;
 import soot.SootMethodRef;
 import soot.Trap;
 import soot.Unit;
 import soot.Value;
 import soot.jimple.*;
 import soot.jimple.toolkits.scalar.Evaluator;
 import soot.util.Chain;
 import sootInsert.Main;
 import source.Channel;
 import util.Util;
 
 public class AppletContextsTransformer extends BodyTransformer {
 
 	private static AppletContextsTransformer instance = new AppletContextsTransformer();
 
 	public static  AppletContextsTransformer v() {
 		return instance;
 	}
 
 	@Override
 	protected void internalTransform(Body body, String phaseName, Map options) {
 		SootMethod method = body.getMethod();
 		String signature = Util.getSignatureFromSignature(method.getSignature());
 		List<Channel> channels = Main.getChannelsFromSite(signature);
 
 		if (channels != null) { //é um método que está associado ao menos com um canal
 			transformContext(body, signature, channels);
 		}
 
 		if (method.getSignature().matches("<.*:\\svoid process\\(javacard.framework.APDU\\)>")) {
 			transformProccess(body);
 		}
 	}
 
 	private void transformProccess(Body body) {
 		Chain<Unit> units = body.getUnits();
 		Unit init         = units.getFirst(); //inicio da trap
 		Unit last         = units.getLast(); //fim da trap
 
 		soot.Local catchRefLocal = soot.jimple.Jimple.v().newLocal("$r" + body.getLocalCount(), soot.RefType.v("user.EChannelExceptions")); // local que guarda a exceção capturada => e
 		body.getLocals().add(catchRefLocal);
 
 		soot.jimple.CaughtExceptionRef caughtRef = soot.jimple.Jimple.v().newCaughtExceptionRef();
 		soot.jimple.Stmt caughtIdentity = soot.jimple.Jimple.v().newIdentityStmt(catchRefLocal, caughtRef);
 		units.insertAfter(caughtIdentity, last);
 		//fim catch
 
 		SootClass echannelExceptionKlass = Scene.v().getSootClass("user.EChannelExceptions");
 
 		Trap t = Jimple.v().newTrap(echannelExceptionKlass, init, last, caughtIdentity);
 		body.getTraps().add(t);
 
 		SootClass isoExceptionKlass = Scene.v().getSootClass("javacard.framework.ISOException");
 
 		SootMethodRef isoThrowItRef = isoExceptionKlass.getMethodByName("throwIt").makeRef();                      //ISOException.throwIt(short)
 		SootMethodRef eChannelThrowItRef = echannelExceptionKlass.getMethodByName("throwIt").makeRef();           //EChannelExceptions.throwIt(short)
 		SootMethodRef getReasonRef = echannelExceptionKlass.getSuperclass().getMethodByName("getReason").makeRef();//EChannelExceptions.getReason()
 
 		VirtualInvokeExpr eReason = Jimple.v().newVirtualInvokeExpr(catchRefLocal, getReasonRef);                  //e.getReason()
 
 		soot.Local eReasonLocal = soot.jimple.Jimple.v().newLocal("$r" + body.getLocalCount(), ShortType.v());
 		body.getLocals().add(eReasonLocal);
 		soot.Local jcmlCodeLocal = soot.jimple.Jimple.v().newLocal("$r" + body.getLocalCount(), ShortType.v());
 		body.getLocals().add(jcmlCodeLocal);
 
 		Stmt eReasonLocalAssignment = Jimple.v().newAssignStmt(eReasonLocal, eReason);
 		units.insertAfter(eReasonLocalAssignment, caughtIdentity);
 
 		InvokeStmt finalInvokeThrowItStmt = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(eChannelThrowItRef, eReasonLocal));
 
 		InvokeStmt isoInvokeThrowItStmt = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(isoThrowItRef, eReasonLocal));
 
 		Value condition1 = Jimple.v().newGeExpr(eReasonLocal, IntConstant.v(0));//colocar os dois em outro canto
 		Value condition2 = Jimple.v().newLeExpr(eReasonLocal, IntConstant.v(Main.getDadosCanais().listaDeCanaisSerializavel.size()));//colocar os dois em outro canto
 
 		IfStmt ifStmt1 = Jimple.v().newIfStmt(condition1, isoInvokeThrowItStmt);
 		IfStmt ifStmt2 = Jimple.v().newIfStmt(condition2, ifStmt1);
 		GotoStmt gotoInvokeThrowIt = Jimple.v().newGotoStmt(finalInvokeThrowItStmt);
 
 		/* if (stmt2) vai para if (stmt1)
 		 * goto finalInvoke
 		 * if (stmt1) vai para isoInvoke
 		 * finalInvoke
 		 * isoInvoke
 		*/
 		units.insertAfter(ifStmt2, eReasonLocalAssignment);
 		units.insertAfter(gotoInvokeThrowIt, ifStmt2);
 		units.insertAfter(ifStmt1, gotoInvokeThrowIt);
 		units.insertAfter(finalInvokeThrowItStmt, ifStmt1);
 		units.insertAfter(isoInvokeThrowItStmt, finalInvokeThrowItStmt);
 		units.addLast(Jimple.v().newReturnVoidStmt());
 	}
 
     //usado para aplicar as transformações nos raising sites
 	private void transformContext(Body body, String signature, List<Channel> channels) {
 		//todo método termina com return?
 		Chain<Unit> units = body.getUnits();
 
 		Unit init       = units.getFirst(); //inicio da trap
 		Unit last       = units.getLast(); //fim da trap
 		Unit returnStmt = units.getLast();
 
 		if (!(returnStmt instanceof ReturnStmt)) {
 			returnStmt = Jimple.v().newReturnVoidStmt();
 			units.addLast(returnStmt);
 		}
 
 		//construindo a trap
 		soot.Local catchRefLocal = soot.jimple.Jimple.v().newLocal("$r" + body.getLocalCount(), soot.RefType.v("user.EChannelExceptions")); // local que guarda a exceção capturada => e
 		body.getLocals().add(catchRefLocal);
 
 		soot.jimple.CaughtExceptionRef caughtRef = soot.jimple.Jimple.v().newCaughtExceptionRef();
 		soot.jimple.Stmt caughtIdentity = soot.jimple.Jimple.v().newIdentityStmt(catchRefLocal, caughtRef);
 		units.insertAfter(caughtIdentity, last);
 		//fim catch
 
 		SootClass echannelExceptionKlass = Scene.v().getSootClass("user.EChannelExceptions");
 
 		Trap t = Jimple.v().newTrap(echannelExceptionKlass, init, last, caughtIdentity);
 		body.getTraps().add(t);
 
 		SootMethodRef getInstanceRef = echannelExceptionKlass.getMethodByName("getInstance").makeRef();    //getInstance()
 		SootMethodRef setReasonRef = echannelExceptionKlass.getMethodByName("setReason").makeRef();    //getInstance()
 		SootMethodRef getReasonRef = echannelExceptionKlass.getSuperclass().getMethodByName("getReason").makeRef();//getReason()
 
 		//short eReason
 		soot.Local eReasonLocal = soot.jimple.Jimple.v().newLocal("$r" + body.getLocalCount(), ShortType.v());
 		body.getLocals().add(eReasonLocal);
 		//short jcmlCode
 		soot.Local jcmlCodeLocal = soot.jimple.Jimple.v().newLocal("$r" + body.getLocalCount(), ShortType.v());
 		body.getLocals().add(jcmlCodeLocal);
 		//EChannelExceptions instance
 		soot.Local instanceLocal = soot.jimple.Jimple.v().newLocal("$r" + body.getLocalCount(), echannelExceptionKlass.getType());
 		body.getLocals().add(instanceLocal);
 
 		//instance = EChannelExceptions.getInstance()
 		Stmt instanceAssignment = Jimple.v().newAssignStmt(instanceLocal, Jimple.v().newStaticInvokeExpr(getInstanceRef));
 		units.insertAfter(instanceAssignment, caughtIdentity);
 
 		//eReason = e.getReason()
 		Stmt eReasonLocalAssignment = Jimple.v().newAssignStmt(eReasonLocal, Jimple.v().newVirtualInvokeExpr(catchRefLocal, getReasonRef));
 		units.insertAfter(eReasonLocalAssignment, instanceAssignment);
 
 		//instance.setReason(eReason)
 		Stmt setReasonAssignment = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(instanceLocal, setReasonRef, eReasonLocal));
 		units.insertAfter(setReasonAssignment, eReasonLocalAssignment);
 
 		//throw instance
 		Stmt throwInstance = Jimple.v().newThrowStmt(instanceLocal);
 		units.insertAfter(throwInstance, setReasonAssignment);
 
 		for (Channel channel : channels) {
 			//instance.setReason(EChannelExceptions.SW_?_ERROR) #1
 			SootFieldRef jcmlErrorCodeRef = Scene.v().makeFieldRef(echannelExceptionKlass, Util.eChannelTipoToStatic(channel.tipo), ShortType.v(), true);
 			StaticFieldRef jcmlErrorCode = Jimple.v().newStaticFieldRef(jcmlErrorCodeRef);
 			Stmt jcmlCodeAssignment = Jimple.v().newAssignStmt(jcmlCodeLocal, jcmlErrorCode); //$r4 = EChannelExceptions.SW_?_ERROR
			Stmt setReasonLocalAssignment = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(instanceLocal, setReasonRef, jcmlCodeLocal));
 
 			//throw instance #2
 			Stmt throwInstance2 = Jimple.v().newThrowStmt(instanceLocal);
 
 			Value condition = Jimple.v().newEqExpr(eReasonLocal, jcmlCodeLocal);//colocar os dois em outro canto
 
 			//if (eReason == EChanellExceptions.SW_?_ERROR) -> #1
 			IfStmt ifStmt = Jimple.v().newIfStmt(condition, setReasonLocalAssignment);
 
 			units.insertBefore(jcmlCodeAssignment, setReasonAssignment);
 			units.insertAfter(ifStmt, jcmlCodeAssignment);
 			units.insertAfter(setReasonLocalAssignment, throwInstance);
 			units.insertAfter(throwInstance2, setReasonLocalAssignment);
 		}
 	}
 }
