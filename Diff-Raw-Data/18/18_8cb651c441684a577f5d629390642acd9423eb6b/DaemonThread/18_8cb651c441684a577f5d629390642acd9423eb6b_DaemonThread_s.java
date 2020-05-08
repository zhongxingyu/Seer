 package it.uniroma1.dis.jaco.server;
 
 import it.uniroma1.dis.jaco.composition.Composition;
 import it.uniroma1.dis.jaco.model.Behavior;
 import it.uniroma1.dis.jaco.model.CommunityState;
 import it.uniroma1.dis.jaco.model.ComputedComposition;
 import it.uniroma1.dis.jaco.model.NodeWithTransitions;
 import it.uniroma1.dis.jaco.model.PossibleState;
 import it.uniroma1.dis.jaco.model.TargetState;
 import it.uniroma1.dis.jaco.model.TargetTransition;
 import it.uniroma1.dis.jaco.model.Transition;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.ws.rs.WebApplicationException;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 public class DaemonThread extends Thread {
 
 	BlockingQueue<String> queue = Queue.instance.getModel();
 
 	public void run() {
 		while (true) {
 			try {
 				if (queue.isEmpty()) {
 					// System.out.println("Going to sleep...");
 					Thread.sleep(500);
 				} else {
 					System.out.println(queue.toString());
 					String requestID = queue.peek();
 
 					prepareFilesForComposition(requestID);
 					askForComposition(requestID);
 					convertCompositionToXML(requestID);
 
 					queue.remove();
 				}
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void convertCompositionToXML(String requestID) {
 		String baseFilePath = System.getProperty("catalina.base")
 				+ File.separator + "JacoStorage" + File.separator + requestID;
 
 		// Read from SMV file the names of the behaviors
 		List<String> behaviorsNames = new LinkedList<String>();
 		behaviorsNames.add("Target");
 
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					new DataInputStream(new FileInputStream(baseFilePath
 							+ File.separator + "Composition" + File.separator
 							+ "Composition.smv"))));
 			String fileLine;
 			Pattern behaviorNamePattern = Pattern
 					.compile("^\\s+(s\\d+) : (\\w+)\\(index, operation\\)");
 
 			while ((fileLine = br.readLine()) != null) {
 				Matcher matcher = behaviorNamePattern.matcher(fileLine);
 				while (matcher.find()) {
 					// System.out.println("Regexp matched.");
 					System.out.println(matcher.group(2));
 					behaviorsNames.add(matcher.group(2));
 				}
 			}
 
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
		ComputedComposition computedComposition = new ComputedComposition(true);
 
 		HashMap<String, TargetState> targetStates = new HashMap<String, TargetState>();
 		// Read states from Composition.txt file
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					new DataInputStream(new FileInputStream(baseFilePath
 							+ File.separator + "Composition" + File.separator
 							+ "Composition.txt"))));
 			String fileLine;
 			Pattern stateDescriptionPattern = Pattern
 					.compile("State (\\d*) : State\\s*-> <operation:(\\w*), state:(\\w+), ([state:[\\w]*, ]*)index:(\\d*)>");
 			Pattern stateNamePattern = Pattern.compile("state:(\\w*),");
 			// Pattern transitionPattern =
 			// Pattern.compile("From\\s*(\\d*)\\s*to\\s*(\\d*)");
 
 			while ((fileLine = br.readLine()) != null) {
 				Matcher matcher = stateDescriptionPattern.matcher(fileLine);
 				while (matcher.find()) {
 					// System.out.println("Regexp matched.");
 					// System.out.println(matcher.group(1) + ", " +
 					// matcher.group(2));
 					// behaviorsNames.add(matcher.group(2));
 
 					if (matcher.group(3).equals("start_st"))
 						continue;
 
 					// targetStatesId.put(new Integer(matcher.group(1)),
 					// matcher.group(3));
 					TargetState ts;
 					if (targetStates.containsKey(matcher.group(3)))
 						ts = targetStates.get(matcher.group(3));
 					else {
 						ts = new TargetState(matcher.group(3));
 						targetStates.put(matcher.group(3), ts);
 					}
 
 					/*
 					 * TargetState ts = new TargetState(matcher.group(3),
 					 * Integer.parseInt(matcher.group(1))); if(
 					 * !targetStates.containsKey(matcher.group(3)) )
 					 * targetStates.put(matcher.group(3), ts);
 					 */
 
 					System.out.println("Target state: " + matcher.group(3));
 
 					System.out.println("Available behaviors: ");
 					// OrchestrationState os = new OrchestrationState(
 					// Integer.parseInt(matcher.group(1)) );
 					CommunityState cs = new CommunityState();
 					String[] stateDescriptions = matcher.group(4).split("\\s");
 					for (int i = 0; i < stateDescriptions.length; i++) {
 						String desc = stateDescriptions[i];
 						// System.out.println(desc);
 						Matcher mat = stateNamePattern.matcher(desc);
 						while (mat.find()) {
 							System.out.println("  " + behaviorsNames.get(i + 1)
 									+ ": " + mat.group(1));
 							cs.addBehaviorState(behaviorsNames.get(i + 1),
 									mat.group(1));
 						}
 					}
 					// ts.addCommunityState(cs);
 
 					PossibleState ps = ts.contains(cs);
 					if (ps == null)
 						ps = new PossibleState(cs);
 
 					// targetStates.get(matcher.group(3)).addOrchestrationState(os);
 
 					System.out.println("Next action: " + matcher.group(2));
 					int index = Integer.parseInt(matcher.group(5));
 					System.out.println("Next NPC to be invoked: "
 							+ behaviorsNames.get(index));
 					System.out
 							.println("=======================================");
 
 					TargetTransition tt = new TargetTransition(
 							behaviorsNames.get(index), matcher.group(2));
 					// ts.addTransition(tt);
 					ps.addTransition(tt);
 
 					ts.addPossibleState(ps);
 					// computedComposition.addTargetState(ts);
 				}
 				/*
 				 * Matcher transitionMatcher =
 				 * transitionPattern.matcher(fileLine);
 				 * while(transitionMatcher.find()) { String sourceNodeName =
 				 * targetStatesId.get(new Integer(transitionMatcher.group(1)));
 				 * if (sourceNodeName == null) continue; String
 				 * destinationNodeName = targetStatesId.get(new
 				 * Integer(transitionMatcher.group(2)));
 				 * System.out.println("From " + sourceNodeName + " to " +
 				 * destinationNodeName);
 				 * targetStates.get(sourceNodeName).getTargetTransition
 				 * ().setTargetNode(destinationNodeName); }
 				 */
 			}
 
 			System.out.println(targetStates.size() + " states found.");
 
 			for (TargetState ts : targetStates.values())
 				computedComposition.addTargetState(ts);
 
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// Write Composition to XML file
 		try {
 			String baseDir = System.getProperty("catalina.base")
 					+ File.separator + "JacoStorage" + File.separator
 					+ requestID;
 			File compositionXmlFile = new File(baseDir + File.separator
 					+ "Composition" + File.separator + "Composition.xml");
 
 			synchronized (compositionXmlFile) {
 				new File(baseFilePath).mkdirs();
 				compositionXmlFile.createNewFile();
 
 				JAXBContext jaxbContext = JAXBContext
 						.newInstance(ComputedComposition.class);
 				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
 						true);
 
 				jaxbMarshaller.marshal(computedComposition, compositionXmlFile);
 			}
 		} catch (JAXBException e) {
 			e.printStackTrace();
 			// throw new WebApplicationException(500);
 		} catch (IOException e) {
 			// e.printStackTrace();
 			throw new WebApplicationException(500);
 		}
 	}
 
 	private void prepareFilesForComposition(String requestID)
 			throws IOException {
 		String baseFilePath = System.getProperty("catalina.base")
 				+ File.separator + "JacoStorage" + File.separator + requestID;
 
 		Behavior target = null;
 		List<Behavior> behaviors = new LinkedList<Behavior>();
 
 		try {
 			JAXBContext jaxbContext = JAXBContext.newInstance(Behavior.class);
 			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
 
 			target = (Behavior) jaxbUnmarshaller.unmarshal(new File(
 					baseFilePath + File.separator + "Target" + File.separator
 							+ "Target.xml"));
 
 			File[] xmlFiles = new File(baseFilePath + File.separator
 					+ "Behaviors").listFiles();
 			for (File file : xmlFiles) {
 				Behavior b = (Behavior) jaxbUnmarshaller.unmarshal(file);
 				behaviors.add(b);
 			}
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		}
 
 		String baseDir = baseFilePath + File.separator + "Composition";
 		File compositionSMVFile = new File(baseDir + File.separator
 				+ "Composition.smv");
 
 		if (!compositionSMVFile.exists()) {
 			new File(baseDir).mkdirs();
 			compositionSMVFile.createNewFile();
 		}
 		PrintWriter pw;
 
 		try {
 			pw = new PrintWriter(new BufferedWriter(new FileWriter(
 					compositionSMVFile)));
 
 			// Main Module
 			pw.println("MODULE main");
 			pw.println("VAR");
 			pw.println(" env: Env(sys.index);");
 			pw.println(" sys: Sys;");
 			pw.println("DEFINE");
 			pw.println(" good := (sys.initial & env.initial)|!(env.failure);");
 			pw.println("");
 			// End of Main Module
 
 			// Sys Module
 			pw.println("MODULE Sys");
 			pw.println("VAR");
 			pw.println(" index : 0.." + behaviors.size() + ";");
 			pw.println("INIT");
 			pw.println(" index = 0");
 			pw.println("TRANS");
 			pw.println(" case");
 			pw.println("  index = 0 : next(index) != 0;");
 			pw.println("  index != 0 : next(index) != 0;");
 			pw.println(" esac");
 			pw.println("DEFINE");
 			pw.println(" initial := (index = 0);");
 			pw.println("");
 			// End of Sys Module
 
 			// Env module
 			pw.println("MODULE Env(index)");
 
 			pw.println("VAR");
 			pw.print(" operation : {start_op");
 			{
 				Set<String> listOfActions = new HashSet<String>();
 				for (Behavior b : behaviors) {
 					List<NodeWithTransitions> nodes = b.getFiniteStateMachine();
 					for (NodeWithTransitions n : nodes) {
 						List<Transition> tr_list = n.getTransitions();
 						for (Transition t : tr_list)
 							listOfActions.add(t.getActionName());
 					}
 				}
 
 				for (String action : listOfActions)
 					pw.print(", " + action);
 			}
 			// for each possible action, pw.print(action);
 			pw.println("};");
 			pw.println(" target : Target(operation);");
 			for (int i = 0; i < behaviors.size(); i++)
 				pw.println(" s" + (i + 1) + " : " + behaviors.get(i).getName()
 						+ "(index, operation);");
 
 			pw.println("DEFINE");
 			pw.print(" initial := (");
 			for (int i = 0; i < behaviors.size(); i++)
 				pw.print("s" + (i + 1) + ".initial & ");
 			pw.println("target.initial & operation = start_op);");
 
 			pw.print(" failure := (");
 			for (int i = 0; i < behaviors.size(); i++) {
 				if (i == behaviors.size() - 1)
 					pw.print("s" + (i + 1) + ".failure");
 				else
 					pw.print("s" + (i + 1) + ".failure | ");
 			}
 			pw.print(") | (target.final & !(");
 			for (int i = 0; i < behaviors.size(); i++) {
 				if (i == behaviors.size() - 1)
 					pw.print("s" + (i + 1) + ".final");
 				else
 					pw.print("s" + (i + 1) + ".final & ");
 			}
 			pw.println("));");
 			pw.println("");
 			// End of Env module
 
 			// Target module
 			pw.println("MODULE Target(op)");
 			pw.println("VAR");
 			pw.print(" state : {start_st");
 			for (NodeWithTransitions n : target.getFiniteStateMachine())
 				pw.print(", " + n.getNodeName());
 			pw.println("};");
 
 			pw.println("INIT");
 			pw.println(" state = start_st & op = start_op");
 
 			pw.println("TRANS");
 			pw.println(" case");
 			{
 				List<NodeWithTransitions> nodes = target
 						.getFiniteStateMachine();
 				// First line
 				pw.print("  state = start_st & op = start_op : next(state) = "
 						+ nodes.get(0).getNodeName() + " & next(op) in {");
 
 				for (int i = 0; i < nodes.size(); i++) {
 					NodeWithTransitions node = nodes.get(i);
 					List<Transition> trans = node.getTransitions();
 
 					if (i == 0) {
 						for (int j = 0; j < trans.size(); j++) {
 							if (j == 0)
 								pw.print(trans.get(j).getActionName());
 							else
 								pw.print(", " + trans.get(j).getActionName());
 						}
 						pw.println("};");
 					}
 
 					if (trans == null)
 						continue;
 					for (int j = 0; j < trans.size(); j++) {
 						Transition t = trans.get(j);
 						pw.print("  state = " + node.getNodeName() + " & op = "
 								+ t.getActionName() + " : next(state) = "
 								+ t.getDestinationNodes().get(0)
 								+ " & next(op) in {");
 						String destNode = t.getDestinationNodes().get(0);
 
 						for (NodeWithTransitions n : nodes) {
 							if (n.getNodeName().equals(destNode)) {
 								List<Transition> tr_list = n.getTransitions();
 								if (tr_list == null)
 									continue;
 								for (int k = 0; k < tr_list.size(); k++) {
 									if (k == 0)
 										pw.print(tr_list.get(k).getActionName());
 									else
 										pw.print(", "
 												+ tr_list.get(k)
 														.getActionName());
 								}
 							}
 						}
 						pw.println("};");
 					}
 					// pw.println("};");
 
 				}
 			}
 			pw.println(" esac");
 
 			pw.println("DEFINE");
 			pw.println(" initial := state = start_st & op = start_op;");
 			pw.print(" final := state in {");
 			{
 				List<NodeWithTransitions> nodes = target
 						.getFiniteStateMachine();
 				for (int i = 0; i < nodes.size(); i++) {
 					if (i == 0)
 						pw.print(nodes.get(i).getNodeName());
 					else
 						pw.print(", " + nodes.get(i).getNodeName());
 				}
 			}
 			pw.println("};");
 			pw.println("");
 			// End of Target module
 
 			// Services modules
 			for (int bi = 0; bi < behaviors.size(); bi++) {
 				Behavior b = behaviors.get(bi);
 				pw.println("MODULE " + b.getName() + "(index, operation)");
 				pw.println("VAR");
 				pw.print(" state : {start_st");
 				for (NodeWithTransitions n : b.getFiniteStateMachine())
 					pw.print(", " + n.getNodeName());
 				pw.println("};");
 
 				pw.println("INIT");
 				pw.println(" state = start_st");
 
 				pw.println("TRANS");
 				pw.println(" case");
 				{
 					List<NodeWithTransitions> nodes = b.getFiniteStateMachine();
 					pw.println("  state = start_st & operation = start_op & index = 0 : next(state) = "
 							+ nodes.get(0).getNodeName() + ";");
 					pw.println("  (index != " + (bi + 1)
 							+ ") : next(state) = state;");
 
 					for (NodeWithTransitions node : nodes)
 						for (Transition trans : node.getTransitions()) {
 							pw.print("  (state = " + node.getNodeName()
 									+ " & operation = " + trans.getActionName()
 									+ ") : next(state) in {");
 							List<String> destNodes = trans
 									.getDestinationNodes();
 							for (int l = 0; l < destNodes.size(); l++) {
 								if (l == 0)
 									pw.print(destNodes.get(l));
 								else
 									pw.print(", " + destNodes.get(l));
 							}
 							pw.println("};");
 						}
 				}
 
 				pw.println(" esac");
 
 				pw.println("DEFINE");
 				pw.println(" initial := state = start_st & operation = start_op & index = 0;");
 				pw.print(" failure := index = " + (bi + 1) + " & !(");
 				{
 					List<NodeWithTransitions> nodes = b.getFiniteStateMachine();
 
 					for (int i = 0; i < nodes.size(); i++) {
 						NodeWithTransitions node = nodes.get(i);
 						List<Transition> trans = node.getTransitions();
 						if (i != 0)
 							pw.print("|");
 						pw.print(" (state = " + node.getNodeName()
 								+ " & operation in {");
 						for (int j = 0; j < trans.size(); j++) {
 							if (j == 0)
 								pw.print(trans.get(j).getActionName());
 							else
 								pw.print(", " + trans.get(j).getActionName());
 						}
 						pw.print("}) ");
 					}
 				}
 				pw.println(");");
 				pw.print(" final := state in {");
 				{
 					List<NodeWithTransitions> nodes = b.getFiniteStateMachine();
 					for (int i = 0; i < nodes.size(); i++) {
 						if (i == 0)
 							pw.print(nodes.get(i).getNodeName());
 						else
 							pw.print(", " + nodes.get(i).getNodeName());
 					}
 				}
 				pw.println("};");
 				pw.println("");
 			}
 			// End of Services modules
 
 			pw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void askForComposition(String requestID) {
 		String baseFilePath = System.getProperty("catalina.base")
 				+ File.separator + "JacoStorage" + File.separator + requestID
 				+ File.separator + "Composition";
 		Composition c = new Composition(baseFilePath);
 		c.doComposition();
 	}
 }
