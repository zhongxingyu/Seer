 package org.kevoree.library.freepastry;
 
 import org.kevoree.ContainerRoot;
 import org.kevoree.api.service.core.script.KevScriptEngine;
 import org.kevoree.framework.message.StdKevoreeMessage;
 import org.kevoree.macaw.framework.scheduler.Scheduler;
 import org.kevoree.macaw.framework.testcase.TestCase;
 import org.kevoree.macaw.framework.testcase.TestResult;
 import org.macaw.messages.MethodResult;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 16/04/12
  * Time: 11:04
  * <p/>
  * TestCase:
  * 1 - Création d'un réseau, contenant 10 noeuds FreePastry
  * 2 - Ajout d'une centaine de pairs (K,V) sur le noeud 5.
  * 3 - Kill des noeuds 4, 5, 6
  * 4 - Recuperation des pairs ajoutés (dans tous les noeuds actifs)
  * 5 - Restart des noeuds 4, 5 et 6
  * 6 - idem 4
  */
 public class PastryTestCase implements TestCase {
 
 	private final int nbPeerNodes = 10;
 
 	private Scheduler scheduler = null;
 
 	@Override
 	public void setParentScheduler (Scheduler sch) {
 		scheduler = sch;
 	}
 
 	// 1 - Création d'un réseau, contenant 10 noeuds FreePastry
 	@Override
 	public ContainerRoot buildTestModel (KevScriptEngine offlineEngine) {
 		for (int i = 0; i < nbPeerNodes; i++) {
 			offlineEngine.addVariable("nodeName", "node" + i);
 			offlineEngine.append("addNode {nodeName} : JavaSeNode");
 			offlineEngine.append("addComponent freePastry0@{nodeName} : FreePastryNode");
 		}
 		return offlineEngine.interpret();
 	}
 
 
 	@Override
 	public TestResult execute () {
 		step2();
 		step3();
 		step4();
 		step5();
 		step6();
 
 		return null;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	public void step2 () {
 		// 2 - Ajout d'une centaine de pairs (K,V) sur le noeud 5.
 		List<Integer> ids = new ArrayList<Integer>();
 		for (int i = 0; i < 100; i++) {
 			StdKevoreeMessage message = new StdKevoreeMessage();
 			message.putValue("id", "" + i);
 			message.putValue("request", "add");
 			message.putValue("destNodeName", "node5");
 			message.putValue("destComponentName", "freePastry0");
 			ids.add(i);
 			scheduler.sendToTester(message);
 		}
		List<MethodResult> result = scheduler.waitForResponse(ids);
 		// TODO use result to compare with the oracle
 
 	}
 
 	public void step3 () {
 		// 3 - Kill des noeuds 4, 5, 6
 		StringBuilder builder = new StringBuilder();
 		builder.append("removeNode node4\n");
 		builder.append("removeNode node5\n");
 		builder.append("removeNode node6\n");
 		scheduler.executeKevScriptStatement(builder.toString());
 	}
 
 	public void step4 () {
 		// 4 - Recuperation des pairs ajoutés (dans tous les noeuds actifs)
 		List<Integer> ids = new ArrayList<Integer>();
 		for (int i = 0; i < nbPeerNodes; i++) {
 			if (i != 4 && i != 5 && i != 6) {
 				StdKevoreeMessage message = new StdKevoreeMessage();
 				message.putValue("id", "" + 103 + i);
 				message.putValue("request", "getAll");
 				message.putValue("destNodeName", "node" + i);
 				message.putValue("destComponentName", "freePastry0");
 				ids.add(i);
 				scheduler.sendSyncToTester(message);
 			}
 		}
		List<MethodResult> result = scheduler.waitForResponse(ids);
 		// TODO use result to compare with the oracle
 	}
 
 	public void step5 () {
 		// 5 - Restart des noeuds 4, 5 et 6
 		StringBuilder builder = new StringBuilder();
 		builder.append("addNode node4\n");
 		builder.append("addNode node5\n");
 		builder.append("addNode node6\n");
 		builder.append("addComponent freePastry0@node4 : FreePastryNode\n");
 		builder.append("addComponent freePastry0@node5 : FreePastryNode\n");
 		builder.append("addComponent freePastry0@node6 : FreePastryNode\n");
 		scheduler.executeKevScriptStatement(builder.toString());
 	}
 
 	public void step6 () {
 		// 6 - idem 4
 		List<Integer> ids = new ArrayList<Integer>();
 		for (int i = 0; i < nbPeerNodes; i++) {
 			StdKevoreeMessage message = new StdKevoreeMessage();
 			message.putValue("id", "" + 103 + i);
 			message.putValue("request", "getAll");
 			message.putValue("destNodeName", "node" + i);
 			message.putValue("destComponentName", "freePastry0");
 			ids.add(i);
 			scheduler.sendSyncToTester(message);
 		}
		List<MethodResult> result = scheduler.waitForResponse(ids);
 		// TODO use result to compare with the oracle
 	}
 }
