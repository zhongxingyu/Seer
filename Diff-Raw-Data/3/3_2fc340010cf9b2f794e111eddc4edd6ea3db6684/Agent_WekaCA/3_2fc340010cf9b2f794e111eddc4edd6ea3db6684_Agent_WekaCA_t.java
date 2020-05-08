 package pikater.agents.computing;
 
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
 import jade.util.leap.List;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import pikater.gui.java.MyWekaOption;
 import pikater.ontology.messages.DataInstances;
 import pikater.ontology.messages.Instance;
 import pikater.ontology.messages.Interval;
 import weka.classifiers.Classifier;
 import weka.classifiers.Evaluation;
 import weka.core.Instances;
 import weka.core.Option;
 
 public class Agent_WekaCA extends Agent_ComputingAgent {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3594051562022044000L;
 	private Classifier cls = null;//TODO: constructor
 	private String agentType = null;
 	private String wekaClassName = null;
 	
 	protected Classifier getModelObject(){
 		return cls;
 	}
 
 	protected boolean setModelObject(Classifier _cls){
 		cls = _cls;
 		agentType = null;
 		setWekaClassName(cls.getClass().getName());
 		return true;
 		
 	}
 	@Override
 	public String getAgentType() {
 		return agentType;	
 	}
 	
 	public void setWekaClassName(String _className){
 		wekaClassName = _className;
 		String[] namelst = wekaClassName.split("\\.");
 		if(namelst.length>0)
 			agentType = namelst[namelst.length-1];
 	}
 	
 	public void createClassifierClass(){
 		//TODO: Create cls according to agentType!!!
 		if(wekaClassName == null || wekaClassName.length()==0)
 			return;
 			//
 		try {
 			//TODO: May take options as a second parameter:
 			cls = Classifier.forName(wekaClassName,null);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 	@Override
 	protected void train() throws Exception {
 		working = true;
 		System.out.println("Agent " + getLocalName() + ": Training...");
 
 		cls=null;
 		createClassifierClass();//new cls
 		if(cls==null)
 			throw new Exception("Weka classifier class hasn't been created (Wrong type?).");
 		if (OPTIONS.length > 0) {
 			cls.setOptions(OPTIONS);
 		}
 		cls.buildClassifier(train);
 		state = states.TRAINED; // change agent state
 		OPTIONS = cls.getOptions();
 
 		// write out net parameters
 		System.out.println(getLocalName() + " " + getOptions());
 
 		working = false;
 	}
 
 	protected String getOptFileName(){
 		return "/options/"+getAgentType() +".opt";
 	}
 
 	protected Evaluation test(){
 		working = true;
 		System.out.println("Agent " + getLocalName() + ": Testing...");
 
 		// evaluate classifier and print some statistics
 		Evaluation eval = null;
 		try {
 			eval = new Evaluation(train);
 			eval.evaluateModel(cls, test);
 			System.out.println(eval.toSummaryString(getLocalName() + " agent: "
 					+ "\nResults\n=======\n", false));
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		working = false;
 		return eval;
 	}
 
 	@Override
 	protected pikater.ontology.messages.Evaluation evaluateCA() {
 		Evaluation eval = test();
 
 		pikater.ontology.messages.Evaluation result = new pikater.ontology.messages.Evaluation();
 		result.setError_rate((float) eval.errorRate());
 
 		try {
 			result.setKappa_statistic((float) eval.kappa());
 		} catch (Exception e) {
 			result.setKappa_statistic(-1);
 		}
 
 		result.setMean_absolute_error((float) eval.meanAbsoluteError());
 
 		try {
 			result.setRelative_absolute_error((float) eval
 					.relativeAbsoluteError());
 		} catch (Exception e) {
 			result.setRelative_absolute_error(-1);
 		}
 
 		result.setRoot_mean_squared_error((float) eval.rootMeanSquaredError());
 		result.setRoot_relative_squared_error((float) eval
 				.rootRelativeSquaredError());
 
 		return result;
 	}
 
 	@Override
 	protected DataInstances getPredictions(Instances test,
 			DataInstances onto_test) {
 
 		double pre[] = new double[test.numInstances()];
 		for (int i = 0; i < test.numInstances(); i++) {
 			try {
 				pre[i] = getModelObject().classifyInstance(test.instance(i));
                                System.err.println(pre[i]);
 			} catch (Exception e) {
                                e.printStackTrace();
 				pre[i] = Integer.MAX_VALUE;
 			}
 		}
 
 		// copy results to the DataInstancs
 		int i = 0;
 		Iterator itr = onto_test.getInstances().iterator();
 		while (itr.hasNext()) {
 			Instance next_instance = (Instance) itr.next();
 			next_instance.setPrediction(pre[i]);
 			i++;
 		}
 
 		return onto_test;
 	}
 
 
 	private pikater.ontology.messages.Option convertOption(
 			MyWekaOption _weka_opt) {
 		pikater.ontology.messages.Option opt = new pikater.ontology.messages.Option();
 		Interval interval = null;
 		opt.setMutable(_weka_opt.mutable);
 
 		interval = new Interval();
 		interval.setMin(_weka_opt.lower);
 		interval.setMax(_weka_opt.upper);
 		opt.setRange(interval);
 
 		if (_weka_opt.set != null) {
 			// copy array to List
 			List set = new ArrayList();
 			for (int i = 0; i < _weka_opt.set.length; i++) {
 				set.add(_weka_opt.set[i]);
 			}
 			opt.setSet(set);
 		}
 
 		opt.setIs_a_set(_weka_opt.isASet);
 
 		interval = new Interval();
 		interval.setMin(_weka_opt.numArgsMin);
 		interval.setMax(_weka_opt.numArgsMax);
 		opt.setNumber_of_args(interval);
 
 		opt.setData_type(_weka_opt.type.toString());
 		opt.setDescription(_weka_opt.description);
 		opt.setName(_weka_opt.name);
 		opt.setSynopsis(_weka_opt.synopsis);
 		opt.setDefault_value(_weka_opt.default_value);
 		opt.setValue(_weka_opt.default_value);
 		return opt;
 	}
 
 	@Override
 	protected void getParameters() {
 		//set the Agent type according to the arguments
 		if(OPTIONS_ARGS==null || OPTIONS_ARGS.length!=1 ){
 			System.err.println("Wrong arguments of WekaCA");
 			return;//TODO: error
 		}
 		setWekaClassName((String)OPTIONS_ARGS[0]);
 		createClassifierClass();//in order not to have cls==null
 		 
 		// fills the global Options vector
 
 		System.out.println(getLocalName() + ": The options are: ");
 
 		String optPath = System.getProperty("user.dir") + getOptFileName();
 
 		agent_options = new pikater.ontology.messages.Agent();
 		agent_options.setName(getLocalName());
 		agent_options.setType(getAgentType());
 		// read options from file
 		try {
 			/* Sets up a file reader to read the options file */
 			FileReader input = new FileReader(optPath);
 			System.out.println("OK:"+optPath);
 			/*
 			 * Filter FileReader through a Buffered read to read a line at a
 			 * time
 			 */
 			BufferedReader bufRead = new BufferedReader(input);
 
 			String line; // String that holds current file line
 			int count = 0; // Line number of count
 			// Read first line
 			line = bufRead.readLine();
 			count++;
 
 			// list of ontology.messages.Option
 			List _options = new ArrayList();
 
 			// Read through file one line at time. Print line # and line
 			while (line != null) {
 				System.out.println("    " + count + ": " + line);
 
 				// parse the line
 				String delims = "[ ]+";
 				String[] params = line.split(delims, 7);
 
 				if (params[0].equals("$")) {
 
 					MyWekaOption.dataType dt = MyWekaOption.dataType.BOOLEAN;
 
 					if (params[2].equals("boolean")) {
 						dt = MyWekaOption.dataType.BOOLEAN;
 					}
 					if (params[2].equals("float")) {
 						dt = MyWekaOption.dataType.FLOAT;
 					}
 					if (params[2].equals("int")) {
 						dt = MyWekaOption.dataType.INT;
 					}
 					if (params[2].equals("mixed")) {
 						dt = MyWekaOption.dataType.MIXED;
 					}
 
 					String[] default_options = ((Classifier)getModelObject()).getOptions();
 
 					Enumeration en = ((Classifier)getModelObject()).listOptions();
 					while (en.hasMoreElements()) {
 
 						Option next = (weka.core.Option) en.nextElement();
 						String default_value = "False";
 						for (int i = 0; i < default_options.length; i++) {
 							if (default_options[i].equals("-" + next.name())) {
 								if (default_options[i].startsWith("-")) {
 									// if the next array element is again an
 									// option name,
 									// (or it is the last element)
 									// => it's a boolean parameter
 									if (i == default_options.length - 1) {
 										default_value = "True";
 									} else {
 										// if
 										// (default_options[i+1].startsWith("-")){
 										if (default_options[i + 1]
 												.matches("\\-[A-Z]")) {
 											default_value = "True";
 										} else {
 											default_value = default_options[i + 1];
 										}
 									}
 								}
 							}
 						}
 
 						if ((next.name()).equals(params[1])) {
 							MyWekaOption o;
 							if (params.length > 4) {
 
 								o = new MyWekaOption(next.description(), next
 										.name(), next.numArguments(), next
 										.synopsis(), dt, new Integer(params[3])
 										.intValue(), new Integer(params[4])
 										.intValue(), params[5], default_value,
 										params[6]);
 
 							} else {
 								o = new MyWekaOption(next.description(), next
 										.name(), next.numArguments(), next
 										.synopsis(), dt, 0, 0, "",
 										default_value, "");
 							}
 
 							// convert&save o to options vector
 							_options.add(convertOption(o));
 						}
 					}
 
 				}
 
 				line = bufRead.readLine();
 
 				count++;
 			}
 			agent_options.setOptions(_options);
 			bufRead.close();
 
 		} catch (ArrayIndexOutOfBoundsException e) {
 			/*
 			 * If no file was passed on the command line, this exception is
 			 * generated. A message indicating how to the class should be called
 			 * is displayed
 			 */
 			System.out.println("Usage: java ReadFile filename\n");
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println(getLocalName()
 					+ ": Reading options from .opt file failed.");
 		}
 		// Save the agent's options
 
 		/*
 		 * Enumeration en = cls.listOptions();
 		 * 
 		 * while(en.hasMoreElements()){ Option next =
 		 * (weka.core.Option)en.nextElement();
 		 * System.out.println("  "+next.description()+ ", " +next.name()+ ", "
 		 * +next.numArguments()+ ", " +next.synopsis() ); System.out.println();
 		 * }
 		 */
 
 		/*
 		 * System.out.println("MyWekaOptions: "); for (Enumeration e =
 		 * Options.elements() ; e.hasMoreElements() ;) { MyWekaOption next =
 		 * (MyWekaOption)e.nextElement(); System.out.print(next.name+" ");
 		 * System.out.print(next.lower+" "); System.out.print(next.upper+" ");
 		 * System.out.print(next.type+" ");
 		 * System.out.print(next.numArgsMin+" ");
 		 * System.out.print(next.numArgsMax+" "); System.out.println(next.set);
 		 * System.out.println("------------"); }
 		 */
 	} // end getParameters
 
 
 }
