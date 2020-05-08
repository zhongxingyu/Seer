 package pikater.agents.computing;
 
 import java.util.Random;
 
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
 import jade.util.leap.List;
 import pikater.agents.computing.Agent_ComputingAgent.states;
 import pikater.ontology.messages.DataInstances;
 import pikater.ontology.messages.Evaluation;
 import pikater.ontology.messages.Instance;
 import pikater.ontology.messages.Interval;
 import weka.core.Instances;
 
 public class Agent_Perceptron extends Agent_ComputingAgent {
 	//number of epochs of learning
 	int NEpochs = 10;
 	//Learning rate for perceptron
 	double LearningRate = 0.3;
 	//Attributes of data
 	List attributes;
 	//index of class in attributes
 	int class_index;
 	//index of 1. input neuron for each attribute
 	int attr2neur[];
 	//vector of input neurons
 	double input_vec[];
 	//vector of output neurons (one for each possible category in class)
 	double output_vec[];
 	//weight matrix (size: |output_vec|*|input_vec|)
 	double weights[];
 	Random randgen=new Random();
 	
 	@Override
 	protected Evaluation evaluateCA() {
 		/*
 		meanPriorAbsoluteError = m_SumPriorAbsErr / m_WithClass
 		rootMeanPriorSquaredError=Math.sqrt(m_SumPriorSqrErr / m_WithClass);
 		float kappa = -1;//???		
 		float meanAbsoluteError = 0;//m_SumAbsErr / (m_WithClass - m_Unclassified);
 		float relativeAbsoluteError=0;//100 * meanAbsoluteError() / meanPriorAbsoluteError();
 		float rootMeanSquaredError = 0;// Math.sqrt(m_SumSqrErr / (m_WithClass - m_Unclassified))
 		float rootRelativeSquaredError = 0;//100.0 * rootMeanSquaredError()/ rootMeanPriorSquaredError()
 		not nominal: rootMeanSquaredError
 		otherwise m_Incorrect / m_WithClass OR avgCost()?
 		float errorRate;*/
 		int withclass=0;
 		int incorrect=0;
 		//Compute and compare data
 		Iterator itr = onto_test.getInstances().iterator();
 		while (itr.hasNext()) {
 			Instance next_instance = (Instance) itr.next();
 			if(!(Boolean)next_instance.getMissing().get(class_index)){
 				withclass++;
 				int prediction = evaluateModelOnce(next_instance);
 				int y = (int)Math.round(((Double)next_instance.getValues().get(class_index)));
 				
 				if(y!=prediction){
 					incorrect++;
 				}
 					
 			}
 		}
 		//System.out.println();
 		//Evaluation
 		float errorRate = ((float)incorrect)/withclass;
 		pikater.ontology.messages.Evaluation result = new pikater.ontology.messages.Evaluation();
 		result.setError_rate( errorRate);
 		result.setKappa_statistic( -1);//(1-errorRate - chanceAgreement) / (1 - chanceAgreement)
 		result.setMean_absolute_error( 0);
 		result.setRelative_absolute_error(0);
 		result.setRoot_mean_squared_error(0);
 		result.setRoot_relative_squared_error(0);
 
 		return result;
 
 	}
 
 	@Override
 	public String getAgentType() {
 		return new String("Perceptron");
 	}
 
 	@Override
 	protected void getParameters() {
 		
 		agent_options = new pikater.ontology.messages.Agent();
 		agent_options.setName(getLocalName());
 		agent_options.setType(getAgentType());
 		List _options = new ArrayList();
 		//nastavit optiony
 		pikater.ontology.messages.Option opt = new pikater.ontology.messages.Option();
 		opt.setName("N");
 		opt.setData_type("INT");
 		opt.setIs_a_set(false);
 		Interval interval = new Interval();
 		interval.setMin((float)1.0);
 		interval.setMax((float)1000.0);
 		opt.setRange(interval);
 		opt.setMutable(false);//???
 		opt.setDescription("Set the number of epochs to train through. (default 10) ");//???
 		opt.setSynopsis("-N num ");//???
 		opt.setDefault_value("10");
 		//opt.setValue("10");
 		_options.add(opt);
 		
 		opt = new pikater.ontology.messages.Option();
 		opt.setName("L");
 		opt.setData_type("FLOAT");
 		opt.setIs_a_set(false);
 		interval = new Interval();
 		interval.setMin((float)0.0);
 		interval.setMax((float)1.0);
 		opt.setRange(interval);
 		opt.setMutable(false);//???
 		opt.setDescription("Set the learning rate. (default 0.3) ");
 		opt.setSynopsis("-L num ");
 		opt.setDefault_value("0.3");
 		//opt.setValue("0.3");
 		_options.add(opt);
 		
 		agent_options.setOptions(_options);
 	}
 
 	@Override
 	protected DataInstances getPredictions(Instances test,
 			DataInstances ontoTest) {
 		// results to the DataInstancs
 		Iterator itr = ontoTest.getInstances().iterator();
 		while (itr.hasNext()) {
 			Instance next_instance = (Instance) itr.next();
 			double prediction = evaluateModelOnce(next_instance);
 			next_instance.setPrediction(prediction);
 		}
 
 		return ontoTest;
 	}
 
 	public boolean loadAgent(String agentName) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean saveAgent() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	protected void train() throws Exception {
 		working = true;
 		System.out.println("Agent " + getLocalName() + ": Training...");
 		//parameters
 		NEpochs=10;//default
 		LearningRate=0.3;
 			
 		if(current_task.getAgent().getOptions()!=null){
 			Iterator itr = current_task.getAgent().getOptions().iterator();
 			while (itr.hasNext()) {
 				pikater.ontology.messages.Option next_opt = (pikater.ontology.messages.Option) itr
 				.next();
 				if(next_opt.getName().compareTo("N")==0){
 					NEpochs= Integer.parseInt(next_opt.getValue());
 					//System.out.println("Perceptron: parameter N, value:"+ next_opt.getValue());
 				}else if(next_opt.getName().compareTo("L")==0){
 					LearningRate = Double.parseDouble(next_opt.getValue());
 					//System.out.println("Perceptron: parameter L, value:"+ next_opt.getValue());;
 				}else{
 					/*error?*/
 					System.out.println("Perceptron: Unknown parameter "+next_opt.getName() +", value:"+ next_opt.getValue());
 				}
 			}
 		}
 		
 		//create parameters from attributes: input, output, weights
 		if(!createNet(onto_train)){
 			working = false;
 			throw new Exception("Wrong training data type");
 		}
 		int ninst=onto_train.getInstances().size();
 		for(int epoch = 0; epoch < NEpochs; epoch++){
 			/*Iterator inst_iter = onto_train.getInstances().iterator();
 			while (inst_iter.hasNext()) {
 				pikater.ontology.messages.Instance next_inst = (Instance) inst_iter.next();
 				if(!(Boolean)next_inst.getMissing().get(class_index)){
 					setInput(next_inst);
 					computeNet();
 					adaptWeights((int)Math.round(((Double)next_inst.getValues().get(class_index))));
 				}
 			}*/
 			for(int i=0; i<ninst; i++){
 				pikater.ontology.messages.Instance next_inst = 
 					(Instance) onto_train.getInstances().get(randgen.nextInt(ninst));
 				if(!(Boolean)next_inst.getMissing().get(class_index)){
 					setInput(next_inst);
 					computeNet();
 					adaptWeights(((Double)next_inst.getValues().get(class_index)).intValue());
 				}
 			}
 		}
 		
 		state = states.TRAINED; // change agent state
 
 		// write out net parameters
 		//System.out.println(getLocalName() + " " + getOptions());
 
 		working = false;
 
 	}
 	
 	//creation of neural network
 	private boolean createNet(DataInstances instances){
 		attributes = instances.getAttributes();
 		class_index = instances.getClass_index();
 		if(class_index<0)
 			class_index = attributes.size()-1;
 		attr2neur = new int[attributes.size()];
 		int index = 0;
 		int cur_neu = 1;//first neuron for threshold input
 		Iterator attr_iter = attributes.iterator();
 		while (attr_iter.hasNext()) {
 			pikater.ontology.messages.Attribute next_attr=
 				(pikater.ontology.messages.Attribute)attr_iter.next();
 			attr2neur[index]=cur_neu;
 			if(index==class_index){
 				if(next_attr.getType().compareToIgnoreCase("NOMINAL")!=0)
 					return false;//noncodable class
 				//create output
 				output_vec = new double [next_attr.getValues().size()];
 			}else{
 				int code_size;
 				if(next_attr.getType().compareToIgnoreCase("NOMINAL")==0){
 					code_size = next_attr.getValues().size();
 				}else if(next_attr.getType().compareToIgnoreCase("NUMERIC")==0){
 					code_size = 1;
 				}else if(next_attr.getType().compareToIgnoreCase("DATE")==0){
 					code_size = 1;
 				}else
 					return false;//noncodable data: string/relational
 				cur_neu+=code_size;				
 			}
 			
 			index++;
 		}
 		
 		//create input
 		input_vec=new double[cur_neu];
 		//create random weights
 		weights=new double[input_vec.length*output_vec.length];
 		for(int i=0; i<weights.length;i++)
 			weights[i]=randgen.nextDouble()-0.5;//[-0.5,0.5)
 		return true;
 	}
 	
 	//Set the input layer
 	private void setInput(Instance inst){
 		input_vec[0] = -1;//input for threshold
 		for(int i=1; i<input_vec.length; i++)
 			input_vec[i]=0;
 		Iterator val_iter = inst.getValues().iterator();
 		Iterator attr_iter = attributes.iterator();
 		Iterator mis_iter = inst.getMissing().iterator();
 		int index = 0;
 		while (attr_iter.hasNext()) {
 			pikater.ontology.messages.Attribute next_attr=
 				(pikater.ontology.messages.Attribute)attr_iter.next();
 			Double next_val=(Double)val_iter.next();
 			Boolean next_mis=(Boolean)mis_iter.next();
 			if(index!=class_index&& !next_mis){
 				if(next_attr.getType().compareToIgnoreCase("NOMINAL")==0){
 					//NOMINAL
 					int neu_ind =  (attr2neur[index]+next_val.intValue());
 					input_vec[neu_ind]=1;
 				}else{
 					//DATE/NUMERIC
 					input_vec[attr2neur[index]]=next_val;
 				}
 			}
 			index++;
 		}
 	}
 	
 	//compute output of net
 	private void computeNet(){
 		int nin = input_vec.length;
 		int nout= output_vec.length;
 		for(int i=0; i < nout; i++)
 			output_vec[i]=0.0;
 		//y=W*x
 		for(int i=0; i<weights.length; i++){
 			int o_ind = i/nin;
 			int i_ind = i%nin;
 			output_vec[o_ind]+=weights[i]*input_vec[i_ind];
 		}
 		
 	}
 	
 	//Weight adaptation after computation
 	private void adaptWeights(int right_class){
 		int nin = input_vec.length;
 		int nout= output_vec.length;
 		int errors[] = new int[nout];
 		//e=(d-sgn(y))
 		for(int i=0; i<output_vec.length; i++){
 			errors[i]=0;
 			if(i==right_class){
 				if(output_vec[i]<=0){
 					errors[i]=1;
 				}
 			}else{
 				if(output_vec[i]>0){
 					errors[i]=-1;
 				}
 			}
 		}
 		//W=W+l(e*x^T)
 		for(int i=0; i<weights.length; i++){
 			int o_ind = i/nin;
 			int i_ind = i%nin;
 			weights[i]+=LearningRate*errors[o_ind]*input_vec[i_ind];
 		}
 	}
 	//Compute output of the net
 	private int evaluateModelOnce(Instance inst) {
 		setInput(inst);
 		computeNet();
 		int max_ind =0;
 		double max = output_vec[0];
 		for(int i=1; i<output_vec.length; i++){
 			if(output_vec[i]>max){
 				max = output_vec[i];
 				max_ind = i;
 			}
 		}
 		return max_ind;
 	}
 
 }
