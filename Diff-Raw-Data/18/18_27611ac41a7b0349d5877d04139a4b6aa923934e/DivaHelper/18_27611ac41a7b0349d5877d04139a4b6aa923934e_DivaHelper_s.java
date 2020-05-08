 package diva.helpers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 
 import diva.BoolVariableValue;
 import diva.Configuration;
 import diva.Context;
 import diva.Dimension;
 import diva.EnumLiteral;
 import diva.EnumVariable;
 import diva.EnumVariableValue;
 import diva.Scenario;
 import diva.VariabilityModel;
 import diva.Variable;
 import diva.VariableValue;
 import diva.Variant;
 import diva.alloy.AlloyWrapper;
 
 public class DivaHelper {
 
 	public static VariabilityModel load(File file) {
 		ResourceSet rs = new ResourceSetImpl();
 		URI xmiuri = URI.createFileURI(file.getAbsolutePath());
 		Resource model = rs.createResource(xmiuri);
 		try {
 			model.load(null);
 			return (VariabilityModel) model.getContents().get(0);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static void save(VariabilityModel model, String uri) {
 		// Create a resource set.
 		ResourceSet resourceSet = new ResourceSetImpl();
 
 		// Register the default resource factory -- only needed for stand-alone!
 		// this tells EMF to use XML to save the model
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
 				Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
 
 		// Get the URI of the model file.
 		URI fileURI = URI.createFileURI(new File(uri).getAbsolutePath());
 
 		// Create a resource for this file.
 		Resource resource = resourceSet.createResource(fileURI);
 
 		// Add the model objects to the contents.
 		resource.getContents().add(model);
 
 		// Save the contents of the resource to the file system.
 		try
 		{
 			resource.save(Collections.EMPTY_MAP); // the map can pass special saving options to the operation
 		} catch (IOException e) {
 			e.printStackTrace();
 			/* error handling */
 		}
 	}
 
 	public static void computeSuitableConfigurations(VariabilityModel model, int attempt) {
 
 		if (attempt >= 10) {
 			System.out.println("Simulation has been interrupted/re-started 10 times. Please re-start your whole JVM and re-start the simulation from the last stored partial configuration");
 			return;
 		}
 
 		final Map<String, Variant> variants = new HashMap<String, Variant>();
 		for(Dimension d : model.getDimension()) {
 			for(Variant v : d.getVariant()) {
 				variants.put(v.getId(), v);
 			}
 		}
 
 		final StringBuilder builder = new StringBuilder();
 		model.toAlloy(builder);
 		final String model2Alloy = builder.toString();
 
 		int size = 0;
 		int processedContext = 0;
 		for(Scenario scn : model.getSimulation().getScenario()) {
 			size += scn.getContext().size();
 		}
 		//System.out.println("#cpu: " + Runtime.getRuntime().availableProcessors());
 		final int threadNumber = Math.min(Runtime.getRuntime().availableProcessors()-1, size);
 		final ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
 		final List<AlloyWrapper> tasks = new LinkedList<AlloyWrapper>();
 		//final BlockingQueue<Future<Entry<Context, List<Configuration>>>> done = new LinkedBlockingDeque<Future<Entry<Context, List<Configuration>>>>();
 
 		final ExecutorCompletionService<Entry<Context, List<Configuration>>> ecs = new ExecutorCompletionService<Entry<Context, List<Configuration>>>(executor/*, done*/);
 
 
 		for(Scenario scn : model.getSimulation().getScenario()) {
 			for(Context ctx : scn.getContext()) {	
 				if(ctx.getConfiguration().size() == 0) {//to enable incremental checking
 					AlloyWrapper worker = new AlloyWrapper(model2Alloy, ctx, new  HashMap<>(variants));
 					tasks.add(worker);
 					ecs.submit(worker);
 				} else {
 					processedContext++;
 				}
 			}
 		}
 		executor.shutdown();
 
 		try {
 			int last = 0;
 			int nullCounter = 0;
 			while (processedContext < size && nullCounter < 5) {
 				final long sleepTime = 2*Math.max(125, (size-processedContext)/threadNumber);
 				Future<Entry<Context, List<Configuration>>> entry = ecs.poll();
 				while(entry != null) {
 					if (entry.get() != null) {
 						Context ctx = entry.get().getKey();
 						if (entry.get().getValue() != null) {
 							try {
 								ctx.getConfiguration().addAll(entry.get().getValue());
 							} catch (InterruptedException | ExecutionException e) {
 								e.printStackTrace();
 							}
 							processedContext++;
 						}
 					} else {
 						entry.cancel(true);
 					}
 					entry = ecs.poll();
 				}
 				if (processedContext != last) {
 					nullCounter = 0;
 					last = processedContext;
 					//DivaHelper.save(model, tempFile.getAbsolutePath());
 					//System.out.println("Partial Simulation with " + processedContext + " out of " + size + " contexts has been stored:\n\t" + tempFile.getAbsolutePath());
 					System.out.println("Main (aggregator) thread will sleep " + sleepTime + " ms");
 					Thread.currentThread().sleep(sleepTime);
 				} else {
 					nullCounter++;
 					System.out.println("No context has been processed, yet.\nMain (aggregator) thread will sleep " + Math.min(10000, nullCounter*sleepTime) + " ms");
 					Thread.currentThread().sleep(Math.min(10000, nullCounter*sleepTime));
 				}
 			}
 
 			if (nullCounter > 0) {
 				System.out.println("Simulation has been interrupted");
 				final File tempFile = File.createTempFile("PartialSimulation", ".diva");
 				DivaHelper.save(model, tempFile.getAbsolutePath());
 				System.out.println("Let's re-launch it!");
 				System.out.println("Partial Simulation with " + processedContext + " out of " + size + " contexts has been stored here:\n\t" + tempFile.getAbsolutePath());
 				executor.shutdownNow();
 				executor.awaitTermination(5, TimeUnit.SECONDS);
 				computeSuitableConfigurations(model, ++attempt);
 			}
 
 
 
 		} catch (InterruptedException | IOException | ExecutionException e) {
 			e.printStackTrace();
 		} finally {
 			executor.shutdownNow();
 			for(AlloyWrapper w : tasks) {
 				w.kill();
 			}
 			tasks.clear();
 			try {
 				final File tempFile = File.createTempFile("PartialSimulation", ".diva");
 				DivaHelper.save(model, tempFile.getAbsolutePath());
 				System.out.println("Partial Simulation with " + processedContext + " out of " + size + " contexts has been stored here:\n\t" + tempFile.getAbsolutePath());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 	
 	public static void toThingML(VariabilityModel model, String fileURI) {
 		Map<String, State> states = new HashMap<String, State>();
 		Scenario scn = null;
 
 		if (model.getSimulation() != null) {
 			for(Scenario s : model.getSimulation().getScenario()) {
 				if ("EXHAUSTIVE".equals(s.getName())) {
 					scn = s;
 					break;
 				}
 			}
 			if (scn != null) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("datatype Boolean\n");	
 				builder.append("@c_type \"uint8_t\"\n");
 				builder.append("@java_type \"Boolean\";\n\n");
 
 				for(Variable v : model.getContext()) {
 					if (v instanceof EnumVariable) {
 						builder.append("enumeration Enum" + v.getNameNoSpace() + "\n"); 
 						builder.append("@java_type \"Byte\"\n");
 						builder.append("@c_type \"uint8_t\"\n");
 						builder.append("{\n");
 						int i = 0;
 						for(EnumLiteral l : ((EnumVariable)v).getLiteral()) {
 							builder.append(l.getNameNoSpace() + " @enum_val \""+ (i++) +"\"\n");
 						}
 						builder.append("}\n\n");
 					}
 				}
 
 				builder.append("thing fragment ContextMsgs {\n");
 				for(Variable v : model.getContext()) {
 					builder.append("message " + v.getNameNoSpace() + "(");
 					if (v instanceof EnumVariable) {
 						builder.append("value : Enum" + v.getNameNoSpace());
 					} else {
 						builder.append("status : Boolean");
 					}
 					builder.append(");\n");
 				}
 				builder.append("}\n\n");
 
 				builder.append("thing Adaptation includes ContextMsgs {\n\n");
 				builder.append("provided port contextEvents {\n");
 				for(Variable v : model.getContext()) {
 					builder.append("receives " + v.getNameNoSpace() + "\n");
 				}				
 				builder.append("}\n\n");
 
 				for(Context ctx : scn.getContext()) {
 					Configuration cfg = ctx.bestConfiguration();
 					if (cfg != null) {
 						State s = new State(cfg.id(model));
 						states.put(cfg.id(model), s);
 					}
 				}
 				
 				builder.append("statechart logic init " + ((states.size()>0) ? ((State) states.values().toArray()[0]).name : "XXX") + " {//TODO: change initial state\n");
 
 				for(Context ctx : scn.getContext()) {
 					final Configuration cfg = ctx.bestConfiguration();
 					final State source = states.get(cfg.id(model));
 					for(Context ctx2 : scn.getContext()) {
 						final Set<VariableValue> diff = ctx.changes_from(ctx2);
 						if (diff.size() == 1) {
 							final Configuration cfg2 = ctx2.bestConfiguration();
 							final State target = states.get(cfg2.id(model));
 							
 							final VariableValue vv = diff.iterator().next();
 							
 							final String event = vv.getVariable().getNameNoSpace();
							final String guard = (vv instanceof EnumVariableValue) ? "ce.value == Enum" + ((EnumVariableValue) vv).getVariable().getNameNoSpace() + ":" + ((EnumVariableValue) vv).getLiteral().getNameNoSpace() : null;
 							final Transition t = new Transition(target, event, guard);
 							
 							if (!source.equals(target)) { //We exclude self transitions
								if (vv instanceof BoolVariableValue) {
									if (((BoolVariableValue) vv).isBool()) {
										System.out.println("DEBUG = " + source.targets.add(t));
									}
								} else if (vv instanceof EnumVariableValue) {
									System.out.println("DEBUG = " + source.targets.add(t));
								}
 							}
 						}
 					}
 				}
 
 				for(Entry<String, State> e : states.entrySet()) {
 					builder.append("state " + e.getKey() + "{\n");
 					builder.append(e.getValue().toString());
 					builder.append("}\n\n");
 				}
 
 				builder.append("}\n\n");
 				builder.append("}\n\n");
 
 				builder.append("thing AdaptationGUI includes ContextMsgs\n");
 				builder.append("@mock \"true\"\n{\n\n");
 				builder.append("required port contextEvents {\n");
 				for(Variable v : model.getContext()) {
 					builder.append("sends " + v.getNameNoSpace() + "\n");
 				}				
 				builder.append("}\n\n");
 				builder.append("}\n\n");
 
 				builder.append("configuration InteractiveMode {\n\n");
 				builder.append("instance gui : AdaptationGUI\n");
 				builder.append("instance adapt : Adaptation\n\n");
 
 				builder.append("connector gui.contextEvents => adapt.contextEvents\n");
 				builder.append("}\n");
 
 				System.out.println(builder.toString());
 
 				PrintWriter writer = null;
 				try {
 					writer = new PrintWriter(fileURI);
 					writer.write(builder.toString());
 				} catch (FileNotFoundException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				finally {
 					if (writer != null)
 						writer.close();
 				}
 			}
 		}
 	}
 }
