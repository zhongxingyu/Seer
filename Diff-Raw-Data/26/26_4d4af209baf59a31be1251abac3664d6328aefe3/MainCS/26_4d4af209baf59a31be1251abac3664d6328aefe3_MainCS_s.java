 package synthesis;
 
 import static java.lang.System.out;
 import graphAutomata.GraphAutomata;
 import graphLTS.LTS;
 import graphLTS.NoStateExistException;
 import graphLTS.State;
 import graphLTS.gLTS;
 import graphLTS.gState;
 import graphLTS.gTransition;
 import graphTransformation.Graph;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class MainCS {
 
 	public static void putIntoFile(String content, String path , String fileName){
 		try {			
 			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + fileName + ".dot")));
 			writer.write(content);
 			writer.close();
 			//			String command = "dot -Tjpg -o " + fileName + ".jpg " + fileName + ".dot";
 			//			Runtime.getRuntime().exec(command);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*	public static gLTS buildReconfigurationPlan(String name, Graph model)
 	{
 		// state 0 is assigned to the NA state
 		State s0 = new State("p0");		
 		State s1 = new State("p1");		
 		State s2 = new State("p2");		
 		State s3 = new State("p3");		
 		gState gs0 = new gState(s0, model);
 		gState gs1 = new gState(s1, model);
 		gState gs2 = new gState(s2, model);
 		gState gs3 = new gState(s3, model);
 		ArrayList<gState> finals = new ArrayList<gState>();
 		ArrayList<gState> states = new ArrayList<gState>();
 		states.add(gs0);
 		states.add(gs1);
 		states.add(gs2);
 		states.add(gs3);
 		//	finals.add(gs3);
 
 		ArrayList<gTransition> trans = new ArrayList<gTransition>();
 
 		Event eadd = new Event(ACTIVE.OUTPUT, "env","add", "lgr");
 		gTransition t2 = new gTransition(eadd,gs0,gs1);
 		trans.add(t2);
 		/*
 		Event edis = new Event(ACTIVE.OUTPUT, "env","del", "ch");
 		gTransition t3 = new gTransition(edis,gs1,gs2);
 		trans.add(t3);
 
 		Event econ = new Event(ACTIVE.OUTPUT, "env","conn", "receiver,lgr");
 		gTransition t4 = new gTransition(econ,gs2,gs3);
 		trans.add(t4);
 	 */
 	/*	return new gLTS(name, gs0, states, finals, trans);
 	}
 	 */
	private static  ArrayList<LTS> buildComponentModel(String path) throws IOException, NoStateExistException {
 
 		LTS logger = LTS.parseLTS("logger", "lgr", "q0","q1");
 		logger.addInitRemovalStates("q0" , "q0");
 
 		LTS cache = LTS.parseLTS("cache","ch", "c0","c2");
 		cache.addInitRemovalStates("c0" , "c1@c0");
 
 		LTS receiver = LTS.parseLTS("receiver","receiver","a0","a0");
 		receiver.addInitRemovalStates("a0" , "a1");
 
 		LTS dispatcher = LTS.parseLTS("dispatcher","disp","d0","d4");
 		dispatcher.addInitRemovalStates("d0" , "d0");
 
 		LTS server1 = LTS.parseLTS("server1","srv1","s0","s6");
 		server1.addInitRemovalStates("s0" , "s0");
 
 		LTS server2 = LTS.parseLTS("server2","srv2","v0","v7");
 		server2.addInitRemovalStates("v0" , "v0");
 
 		LTS server3 = LTS.parseLTS("server3","srv3","w0","w4");
 		server3.addInitRemovalStates("w0" , "w0");
 
 		LTS DBA = LTS.parseLTS("DBA","DBA","k0","k1");
 		DBA.addInitRemovalStates("k0" , "k0");
 
 		LTS DBB = LTS.parseLTS("DBB","DBB","b0","b1");
 		DBB.addInitRemovalStates("b0" , "b0");
 
 		LTS agg = LTS.parseLTS("agg","agg","g0","g1");
 		agg.addInitRemovalStates("g0" , "g0");
 
 		out.flush();
		putIntoFile(logger.draw(), path, "logger");
		putIntoFile(receiver.draw(),path, "receiver");
		putIntoFile(logger.draw(), path, "logger");
		putIntoFile(cache.draw(), path, "cache");
		putIntoFile(dispatcher.draw(), path, "dispatcher");
		putIntoFile(server1.draw(), path, "server1");
		putIntoFile(server2.draw(), path, "server2");
		putIntoFile(server3.draw(), path, "server3");
		putIntoFile(DBA.draw(), path, "DBA");
		putIntoFile(agg.draw(),path,  "agg");
 
 		ArrayList<LTS> ltslist = new ArrayList<LTS>();
 		//the order of adding LTSs to the model should be same as their ID in structural model
 		ltslist.add(receiver);
 		ltslist.add(cache);
 		ltslist.add(logger);
 		ltslist.add(dispatcher);
 		ltslist.add(server1);
 		ltslist.add(server2);
 		ltslist.add(server3);
 		ltslist.add(DBA);
 		ltslist.add(DBB);
 		ltslist.add(agg);
 
 		return ltslist;
 
 	}
 
 	private static Graph getHeavyLoadStructure(String path)
 	{
 		String nodes = "1:receiver;2:ch;4:disp;5:srv1;6:srv2;8:DBA;7:srv3;9:DBB;10:agg";
 		String edges = 
 				"1->2:receiverch;"
 						+ "2->1:chreceiver;"
 						+ "1->4:receiverdisp;"
 						+ "4->5:dispsrv1;"
 						+ "4->6:dispsrv2;"
 						+ "8->5:DBAsrv1;"
 						+ "8->6:DBAsrv2;"
 						+ "9->6:DBBsrv2;"
 						+ "9->5:DBBsrv1;"
 						+ "9->7:DBBsrv3;"
 						+ "5->10:srv1agg;"
 						+ "6->10:srv2agg;"
 						+ "6->7:srv2srv3;"
 						+ "10->2:aggch;"
 						+ "7->6:srv3srv2";
 
 		Graph model = new Graph("model", "(10,15)"+ nodes + "|" + edges);
 		/*	
 		String nodes = "1:receiver;3:lgr";
 		String edges = 
 				"1->3:receiverlgr";
 		Graph model = new Graph("model", "(2,1)"+ nodes + "|" + edges);
 		 */
 		out.println("The initial structure of system is:\n" + model.toDot());
 		putIntoFile( model.toDot(), path, "structure");
 
 		return model;
 	}
 
 	@SuppressWarnings("unused")
 	private static Graph getLightLoadStructure(String path)
 	{
 		String nodes = "1:receiver;4:disp;5:srv1;6:srv2;8:DBA;7:srv3;9:DBB;10:agg";
 		String edges = 
 				"1->3:receiverlgr;"
 						+ "1->4:receiverdisp;"
 						+ "4->5:dispsrv1;"
 						+ "4->6:dispsrv2;"
 						+ "4->7:dispsrv3;"
 						+ "8->5:DBAsrv1;"
 						+ "8->6:DBAsrv2;"
 						+ "9->6:DBBsrv2;"
 						+ "9->5:DBBsrv1;"
 						+ "9->7:DBBsrv3;"
 						+ "5->10:srv1agg;"
 						+ "6->10:srv2agg"
 						+ "6->7:srv2srv3;"
 						+ "7->6:srv3srv2"
 						;
 		Graph model = new Graph("model", "(10,16)"+ nodes + "|" + edges);
 		/*	
 		String nodes = "1:receiver;3:lgr";
 		String edges = 
 				"1->3:receiverlgr";
 		Graph model = new Graph("model", "(2,1)"+ nodes + "|" + edges);
 		 */
 		out.println("The initial structure of system is:\n" + model.toDot());
 		putIntoFile( model.toDot(), path, "structure");
 
 		return model;
 	}
 
 	@SuppressWarnings("unused")
 	private static Graph getTestStructure()
 	{
 		String nodes = "1:receiver;2:ch";
 		String edges = 
 				"1->2:receiverch;"
 						+ "1->1:receiverreceiver;"
 						+ "2->1:chreceiver"
 						//						+ "1->4:receiverdisp"
 						;
 		Graph model = new Graph("model", "(2,2)"+ nodes + "|" + edges);
 		out.println("The initial structure of system is:\n" + model.toDot());
 		//		putIntoFile( model.toDot(), "structure");
 
 		return model;
 	}
 
 	private static void test_synthesise_phase2() throws IOException, NoStateExistException {
 
 		String outPath = "/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/output/";
 		ArrayList<LTS>  ltslist = buildComponentModel(outPath);
 		Graph model = getHeavyLoadStructure(outPath);
 
 		// create an empty plan
 		gState planS0 = new gState(new State("p0"), model);
 		HashMap<String,gState> planStates = new HashMap<String, gState>();
 		HashMap<String,gState> planFinalStates = new HashMap<String, gState>();
 		planStates.put("p0" , planS0);
 		planFinalStates.put("p0" , planS0);
 		HashMap<String, gTransition> trans = new HashMap<String, gTransition>();
 		gLTS plan = new gLTS("plan", planS0, planStates, planFinalStates, trans);
 		//	out.println("The reconfiguration plan:\n" + plan.draw());
 
 		//create the system model
 		gLTS gts = new gLTS();
 		gts = gts.applyReconfiguration(ltslist, plan,false);
 		//gts.transToIntStates();
 		gts.removeStateSplitter();
 		//	putIntoFile( gts.draw(), "model");
 
 		GraphAutomata spec = GraphAutomata.parseGraphAutomata("spec", "s0", "s0", outPath);
 		//spec.finalstates.add(spec.s0);
 
 		Synthesizer synth = new Synthesizer();
 		gLTS adaptor = synth.synthesize(gts, spec,plan);
 		//	putIntoFile( adaptor.draw(), "adaptor");
 
 	}
 
 	private static void test_synthesise_phase(String outPath) throws IOException, NoStateExistException {
 
 		GraphAutomata spec = GraphAutomata.parseGraphAutomata("specTest", "s0", "s0", outPath);
 
 		Graph model = new Graph("model", "(1,1)1:plant|1->1:plantplant");
 
 		gState planS0 = new gState(new State("p0"), model);
 		HashMap<String,gState> planStates = new HashMap<String, gState>();
 		HashMap<String,gState> planFinalStates = new HashMap<String, gState>();
 		planStates.put("p0" , planS0);
 		planFinalStates.put("p0" , planS0);
 		HashMap<String, gTransition> trans = new HashMap<String, gTransition>();
 		gLTS plan = new gLTS("plan", planS0, planStates, planFinalStates, trans);
 
 		LTS agg = LTS.parseLTS("plant","plant","g0","g1");
 		agg.addInitRemovalStates("g0" , "g0");
 		ArrayList<LTS> ltslist = new ArrayList<LTS>();
 		ltslist.add(agg);
 
 		gLTS gts = new gLTS();
 		gts = gts.applyReconfiguration(ltslist, plan,false);
 		out.println("The system model is:\n" + gts.draw());
 		gts.removeStateSplitter();
 
 		Synthesizer synth = new Synthesizer();
 		gLTS adaptor = synth.synthesize(gts, spec,plan);
 
 		//		putIntoFile( adaptor.draw(), "adaptor");
 	}
 
 	private static void synthesize(String outPath) throws IOException, NoStateExistException {
 
 		long startTime = System.currentTimeMillis();
 
 		ArrayList<LTS>  ltslist = buildComponentModel(outPath);
 		Graph model = getHeavyLoadStructure(outPath);
 		gLTS plan = gLTS.parsegLTS("plan", "plan", "pa", model);
 		out.println("The reconfiguration plan:\n" + plan.draw());
 
 		gLTS gts = new gLTS();
 		gts = gts.applyReconfiguration(ltslist, plan,false);
 		//gts.transToIntStates();
 		gts.removeStateSplitter();
 		//	putIntoFile( gts.draw(), "model");
 
 		long endTime = System.currentTimeMillis();
 		long duration = endTime - startTime;
 		out.println("\nModel Construction Time:" + Long.toString(duration));
 
 		startTime = System.currentTimeMillis();
 		GraphAutomata spec = GraphAutomata.parseGraphAutomata("spec", "s0", "s0",outPath);
 		//spec.finalstates.add(spec.s0);
 
 
 		Synthesizer synth = new Synthesizer();
 		gLTS adaptor = synth.synthesize(gts, spec, plan);
 		endTime = System.currentTimeMillis();
 		duration = endTime - startTime;
 		out.println("Synthesis Time:" + Long.toString(duration));
 
 		//printEachConfigAdaptor(adaptor, plan);	
 
 		adaptor = synth.removeCycles(adaptor, outPath);
 		printEachConfigAdaptor(adaptor, plan, outPath);
 	}
 
 
 	private static void printEachConfigAdaptor(gLTS adaptor, gLTS plan, String path) {
 		for(gState s:plan.states.values())
 		{
 			gLTS configAdaptor = adaptor.getGLTSofConfig(s.s.ID);
 			out.println("The configuration controller in " + s.s.ID + 
 					":" + Integer.toString(configAdaptor.states.size()));
 			putIntoFile( configAdaptor.draw(), path , "adaptor"+s.s.ID);
 		}
 
 	}
 
 	public static void main(String[] args) throws IOException, NoStateExistException {
		String outPath = "/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/inputs/";
 		synthesize(outPath);
 		//test_synthesise_phase();
 		//	test_synthesise_phase2();
 	}
 
 
 }
