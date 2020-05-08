 package models.usermodel;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.*;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import models.AbstractModel;
 import models.usermodel.TacTexAbstractUserModel.Particle;
 import edu.umich.eecs.tac.props.Product;
 import edu.umich.eecs.tac.props.Query;
 
 
 public class EricParticleFilter extends TacTexAbstractUserModel {
 
 	private long _seed = 1263456;
 	private Random _R;
 	private ArrayList<Product> _products;
 	public static final int VIRTUAL_INIT_DAYS = 5;
 	public static final double BURST_PROB = 0.1;
 	private static double[][] STANDARD_MARKOV_PROBS;
 	private static double[][] BURST_MARKOV_PROBS;
 	private HashMap<Product, Particle[]> _next_step;
 	private HashMap<Product, Particle[]> _next_step_burst;
 	private HashMap<Product, Particle[]> _two_steps;
 	private HashMap<Product, Particle[]> _two_steps_burst;
 	
 
 
 	public EricParticleFilter() {
 		_R = new Random(_seed);
 		_particles = new HashMap<Product,Particle[]>();
 		_products = new ArrayList<Product>();
 		_next_step = null;
 		_two_steps = null;
 		
 		_products.add(new Product("flat", "dvd"));
 		_products.add(new Product("flat", "tv"));
 		_products.add(new Product("flat", "audio"));
 		_products.add(new Product("pg", "dvd"));
 		_products.add(new Product("pg", "tv"));
 		_products.add(new Product("pg", "audio"));
 		_products.add(new Product("lioneer", "dvd"));
 		_products.add(new Product("lioneer", "tv"));
 		_products.add(new Product("lioneer", "audio"));
 		System.out.println("added prods");
 		/**
 		 * Initializing markov chain chances
 		 */
 		double[][] smp = new double[UserState.values().length][UserState.values().length];
 		double[][] bmp = new double[UserState.values().length][UserState.values().length];
 		smp[0][0] = 0.99;
 		smp[0][1] = 0.01;
 		smp[1][0] = 0.05;
 		smp[1][1] = 0.20;
 		smp[1][2] = 0.60;
 		smp[1][3] = 0.10;
 		smp[1][4] = 0.05;
 		smp[2][0] = 0.10;
 		smp[2][2] = 0.70;
 		smp[2][3] = 0.20;
 		smp[3][0] = 0.10;
 		smp[3][3] = 0.70;
 		smp[3][4] = 0.20;
 		smp[4][0] = 0.10;
 		smp[4][4] = 0.90;
 		smp[5][0] = 0.80;
 		smp[5][5] = 0.20;
 		
 		bmp[0][0] = 0.80;
 		bmp[0][1] = 0.20;
 		bmp[1][0] = 0.05;
 		bmp[1][1] = 0.20;
 		bmp[1][2] = 0.60;
 		bmp[1][3] = 0.10;
 		bmp[1][4] = 0.05;
 		bmp[2][0] = 0.10;
 		bmp[2][2] = 0.70;
 		bmp[2][3] = 0.20;
 		bmp[3][0] = 0.10;
 		bmp[3][3] = 0.70;
 		bmp[3][4] = 0.20;
 		bmp[4][0] = 0.10;
 		bmp[4][4] = 0.90;
 		bmp[5][0] = 0.80;
 		bmp[5][5] = 0.20;
 		
 		STANDARD_MARKOV_PROBS = smp;
 		BURST_MARKOV_PROBS= bmp;
 		initializeNumParticles();
 		simVirtualDays();
 	}
 
 	public void initializeParticlesFromFile(String filename) {
 		int[][] allStates = new int[NUM_PARTICLES][UserState.values().length];
 
 		/*
 		 * Parse Particle Log
 		 */
 		BufferedReader input = null;
 		try {
 			input = new BufferedReader(new FileReader(filename));
 			String line;
 			int count = 0;
 			while ((line = input.readLine()) != null) {
 				StringTokenizer st = new StringTokenizer(line," ");
 				if(st.countTokens() == UserState.values().length) {
 					for(int i = 0; i < UserState.values().length; i++) {
 						allStates[count][i] = Integer.parseInt(st.nextToken());
 					}
 				}
 				else {
 					break;
 				}
 				count++;
 			}
 			if(count != NUM_PARTICLES-1) {
 				throw new RuntimeException("Problem reading particle file");
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 
 		for(Product prod : _products) {
 			Particle[] particles = new Particle[NUM_PARTICLES];
 			for(int i = 0; i < particles.length; i++) {
 				Particle particle = new Particle(allStates[i]);
 				particles[i] = particle;
 			}
 			_particles.put(prod, particles);
 		}
 	}
 
 	public void saveParticlesToFile(Particle[] particles) throws IOException {
 		FileWriter fstream = new FileWriter("initParticles" + _R.nextLong());
 		BufferedWriter out = new BufferedWriter(fstream);
 		String output = "";
 		for(int i = 0; i < particles.length; i++) {
 			output += particles[i].stateString() + "\n";
 		}
 		out.write(output);
 		out.close();
 	}
 
 	@Override
 	//Assuming no bursts
 	public int getPrediction(Product product, UserState userState) {
 		Particle[] twoSteps = getTwoSteps().get(product);
 		//Particle[] twoSteps = getNextStep().get(product);
 		double curWeight = 0;
 		double curSum = 0.0;
 		for(int i =0; i<twoSteps.length; i++){
 			curSum+=twoSteps[i].getStateCount(userState)*twoSteps[i]._weight;
 			curWeight += twoSteps[i]._weight;
 		}
 		//System.out.println("curPred = "+curSum/curWeight+" bb= "+getPredictionBothBurst(product, userState)+" fb= "+getPredictionFirstBurst(product, userState)+" sb= "+getPredictionSecondBurst(product, userState));
 		return (int)(curSum/curWeight);
 	}
 	
 	public int getPredictionBothBurst(Product product, UserState userState) {
 		Particle[] twoSteps = nextDayBurstProd(getNextBurstStep(), product);
 		double curWeight = 0;
 		double curSum = 0.0;
 		for(int i =0; i<twoSteps.length; i++){
 			curSum+=twoSteps[i].getStateCount(userState)*twoSteps[i]._weight;
 			curWeight += twoSteps[i]._weight;
 		}
 		return (int)(curSum/curWeight);
 	}
 	
 	public int getPredictionFirstBurst(Product product, UserState userState) {
 		Particle[] twoSteps = nextDayPartMap(getNextBurstStep()).get(product);
 		double curWeight = 0;
 		double curSum = 0.0;
 		for(int i =0; i<twoSteps.length; i++){
 			curSum+=twoSteps[i].getStateCount(userState)*twoSteps[i]._weight;
 			curWeight += twoSteps[i]._weight;
 		}
 		return (int)(curSum/curWeight);
 	}
 	
 	public int getPredictionSecondBurst(Product product, UserState userState) {
 		Particle[] twoSteps = nextDayBurstProd(getNextStep(), product);
 		//Particle[] twoSteps = getNextStep().get(product);
 		double curWeight = 0;
 		double curSum = 0.0;
 		for(int i =0; i<twoSteps.length; i++){
 			curSum+=twoSteps[i].getStateCount(userState)*twoSteps[i]._weight;
 			curWeight += twoSteps[i]._weight;
 		}
 		return (int)(curSum/curWeight);
 	}
 	
 	/**
 	 * Problems: _particles.get
 	 */
 	@Override
 	public boolean updateModel(HashMap<Query, Integer> totalImpressions) {
 		//for(Iterator<Query> i = theKeys.iterator(); i.hasNext(); ){
 		for(Product prod : _products){
 			//Query curQuery = i.next();
 			//if(curQuery.getComponent()!=null&&curQuery.getManufacturer()!=null){
 				Product curProd = prod;//new Product(curQuery.getManufacturer(), curQuery.getComponent());
 				//System.out.println(""+curProd.getComponent()+", "+curProd.getManufacturer());
 				int curImp = totalImpressions.get(new Query(prod.getManufacturer(), prod.getComponent()));
 				int expected_non = 0;
 				int expected_burst = 0;
 				for(int i = 0; i< 5; i++){
 					expected_non += (int) (getNextStep().get(prod)[i].getStateCount(UserState.IS)*0.33333333+getNextStep().get(prod)[i].getStateCount(UserState.F2));
 					expected_burst += (int) (getNextBurstProd(prod)[i].getStateCount(UserState.IS)*0.33333333+getNextBurstProd(prod)[i].getStateCount(UserState.F2));
 				}
 				expected_non /= 5;
 				expected_burst /= 5;
 				int bound = (int) ((expected_non+4*expected_burst)/5);
 				if(curImp >= bound){
 					_particles.put(prod, getNextBurstProd(prod));
 				}
 				else
 				{
 					_particles.put(prod, getNextStep().get(prod));
 				}
 				//System.out.print(prod+" imp: "+ curImp + " -- EN: " +expected_non + " -- EB:"+expected_burst);
 				Particle[] curFilt = _particles.get(curProd);
 				//REWEIGHTINGS
 				if(curFilt==null){
 					//System.out.println(""+curQuery.getComponent()+", "+curQuery.getManufacturer());
 				}
 				double[] newWeights = new double[curFilt.length];
 				for(int j = 0; j<curFilt.length; j++){
 					Particle curPart = curFilt[j];
 					double xi = curPart.getStateCount(UserState.IS);
 					double normInput = ((double)(curImp))-curPart.getStateCount(UserState.F2);
 					double comp = normInput-(xi/3.0);
 					double theExp = -1.0*comp*comp/0.4444444444444444444444444444444/xi;
 					newWeights[j] = 0.84628437532163443042211917734116/Math.sqrt(xi)*Math.exp(theExp);
 					
 				}
 				normalizeToOne(newWeights);
 				//RESAMPLING
 				Particle[] resampled = new Particle[NUM_PARTICLES];
 				for(int j = 0; j<NUM_PARTICLES; j++){
 					double curRand =_R.nextDouble();
 					int curIndex = 0;
 					curRand -= newWeights[curIndex];
 					while(curRand>0){
 						curIndex++;
 						curRand -= newWeights[curIndex];
 					}
 					Particle curPart = curFilt[curIndex];
 					resampled[j] = new Particle(curPart.getState(), newWeights[curIndex]/*, curPart.getTransProb(), curPart.getTransVar()*/);
 				}
 				//Replace old one
 				_particles.put(curProd, resampled);
 			//}
 		}
 		//System.out.println();
 		_next_step = null;
 		_two_steps = null;
 		_next_step_burst = null;
 		_two_steps_burst = null;
 		return true;
 	}
 
 	private Particle[] getNextBurstProd(Product prod) {
 		if(_next_step_burst == null){
 			_next_step_burst = new HashMap<Product,Particle[]>();
 		}
 		if(_next_step_burst.get(prod)==null){
 			_next_step_burst.put(prod, nextDayBurstProd(_particles, prod));
 		}
 		return _next_step_burst.get(prod);
 	}
 	
 	private Particle[] nextDayBurstProd(HashMap<Product, Particle[]> particles,
 			Product prod) {
 		Particle[] newArr = new Particle[NUM_PARTICLES];
 		for(int i = 0; i < NUM_PARTICLES; i++) {
 			Particle curPart = particles.get(prod)[i];
 			int[] nextState = new int[UserState.values().length];
 			//double transP = curPart.getTransProb();
 			//double transV = curPart.getTransVar();
 			//double next_prob = randNorm(transP, transP*transV);
 			//double next_var = curPart.getNextTransVar();
 			//for each current state
 			for(int curState = 0; curState<UserState.values().length; curState++){
 				double[] transProbs = new double[UserState.values().length];
 				//create the transition prob to each state
 				for(int j = 0; j < transProbs.length; j++){
 					double curBaseProb;
 					/*if(_R.nextDouble()<=BURST_PROB){
 						curBaseProb = BURST_MARKOV_PROBS[curState][j];
 					}
 					else
 					{*/
 						curBaseProb = BURST_MARKOV_PROBS[curState][j];
 					//}
 					if(curBaseProb == 0){
 						transProbs[j]=0;
 					}
 					else
 					{
 						transProbs[j]=randBinomToNorm(NUM_USERS_PER_PROD, curBaseProb);
 					}
 				}
 				if(curState==2){
 					 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, next_prob);
 					transProbs[5] = randNorm(INIT_TRANS_PROB, INIT_TRANS_PROB*INIT_TRANS_VAR);
 				} else if(curState==3){
 					 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, 2*next_prob);
 					transProbs[5] = randNorm(2*INIT_TRANS_PROB, INIT_TRANS_PROB*INIT_TRANS_VAR*2);
 				} else if(curState==4){
 					 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, 3*next_prob);						 transProbs[5] = next_prob*3;
 					transProbs[5] = randNorm(3*INIT_TRANS_PROB, 3*INIT_TRANS_PROB*INIT_TRANS_VAR);
 				}
 				normalizeToOne(transProbs);
 				for(int j = 0; j < transProbs.length; j++){
 					nextState[j] += curPart.getStateCount(UserState.values()[curState])*transProbs[j];
 				}
 			}
 			int curSum = 0;
 			for(int j = 0; j < nextState.length; j++){
 				curSum += nextState[j];
 			}
 			while(curSum<NUM_USERS_PER_PROD){
 				nextState[0] += 1;
 				curSum++;
 			}
 			while(curSum>NUM_USERS_PER_PROD){
 				nextState[0] -= 1;
 				curSum--;
 			}
 			//System.out.println("Users: " + curSum);
 			
 			//System.out.println("Particle " + i);
 			newArr[i] = new Particle(nextState, curPart.getWeight()/*, next_prob, next_var*/);
 		}
 		return newArr;
 	}
 
 	private HashMap<Product,Particle[]> getNextBurstStep(){
 		for(Product prod: _products){
 			getNextBurstProd(prod);
 		}
 		return _next_step_burst;
 	}
 
 	@Override
 	public AbstractModel getCopy() {
 		return new EricParticleFilter();
 	}
 	
 	private HashMap<Product,Particle[]> getNextStep(){
 		if(_next_step == null){
 			_next_step = nextDayPartMap(_particles);
 		}
 		return _next_step;
 	}
 	
 	private HashMap<Product, Particle[]> nextDayBurst(
 			HashMap<Product, Particle[]> particles) {
 		HashMap<Product, Particle[]> toRet = new HashMap<Product, Particle[]>();
 		for(Product prod : _products) {
 			//System.out.println("Computing product " + prod.getComponent()+ ", "+ prod.getManufacturer());
 			//for each particle
 			Particle[] newArr = new Particle[NUM_PARTICLES];
 			for(int i = 0; i < NUM_PARTICLES; i++) {
 				Particle curPart = particles.get(prod)[i];
 				int[] nextState = new int[UserState.values().length];
 				//double transP = curPart.getTransProb();
 				//double transV = curPart.getTransVar();
 				//double next_prob = randNorm(transP, transP*transV);
 				//double next_var = curPart.getNextTransVar();
 				//for each current state
 				for(int curState = 0; curState<UserState.values().length; curState++){
 					double[] transProbs = new double[UserState.values().length];
 					//create the transition prob to each state
 					for(int j = 0; j < transProbs.length; j++){
 						double curBaseProb;
 						/*if(_R.nextDouble()<=BURST_PROB){
 							curBaseProb = BURST_MARKOV_PROBS[curState][j];
 						}
 						else
 						{*/
 							curBaseProb = BURST_MARKOV_PROBS[curState][j];
 						//}
 						if(curBaseProb == 0){
 							transProbs[j]=0;
 						}
 						else
 						{
 							transProbs[j]=randBinomToNorm(NUM_USERS_PER_PROD, curBaseProb);
 						}
 					}
 					if(curState==2){
 						 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, next_prob);
 						transProbs[5] = randNorm(INIT_TRANS_PROB, INIT_TRANS_PROB*INIT_TRANS_VAR);
 					} else if(curState==3){
 						 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, 2*next_prob);
 						transProbs[5] = randNorm(2*INIT_TRANS_PROB, INIT_TRANS_PROB*INIT_TRANS_VAR*2);
 					} else if(curState==4){
 						 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, 3*next_prob);						 transProbs[5] = next_prob*3;
 						transProbs[5] = randNorm(3*INIT_TRANS_PROB, 3*INIT_TRANS_PROB*INIT_TRANS_VAR);
 					}
 					normalizeToOne(transProbs);
 					for(int j = 0; j < transProbs.length; j++){
 						nextState[j] += curPart.getStateCount(UserState.values()[curState])*transProbs[j];
 					}
 				}
 				int curSum = 0;
 				for(int j = 0; j < nextState.length; j++){
 					curSum += nextState[j];
 				}
 				while(curSum<NUM_USERS_PER_PROD){
 					nextState[0] += 1;
 					curSum++;
 				}
 				while(curSum>NUM_USERS_PER_PROD){
 					nextState[0] -= 1;
 					curSum--;
 				}
 				//System.out.println("Users: " + curSum);
 				
 				//System.out.println("Particle " + i);
 				newArr[i] = new Particle(nextState, curPart.getWeight()/*, next_prob, next_var*/);
 			}
 			toRet.put(prod, newArr);
 		}
 		//System.out.println("next day");		//Particle[] theParts = _particles.get(product);
 		for(Product prod : _products) {
 	//		System.out.println("USERS WITH PREFERENCE " + prod.getComponent()+ ", "+ prod.getManufacturer()+": (particle 0) NS:" + toRet.get(prod)[0].getStateCount(UserState.NS) + ", IS:" + toRet.get(prod)[0].getStateCount(UserState.IS)+ ", F0:" + toRet.get(prod)[0].getStateCount(UserState.F0)+ ", F1:" + toRet.get(prod)[0].getStateCount(UserState.F1)+ ", F2:" + toRet.get(prod)[0].getStateCount(UserState.F2)+ ", T:" + toRet.get(prod)[0].getStateCount(UserState.T));
 		}
 		return toRet;
 	}
 	
 	private HashMap<Product, Particle[]> nextDayPartMap(
 			HashMap<Product, Particle[]> particles) {
 		HashMap<Product, Particle[]> toRet = new HashMap<Product, Particle[]>();
 		for(Product prod : _products) {
 			//System.out.println("Computing product " + prod.getComponent()+ ", "+ prod.getManufacturer());
 			//for each particle
 			Particle[] newArr = new Particle[NUM_PARTICLES];
 			for(int i = 0; i < NUM_PARTICLES; i++) {
 				Particle curPart = particles.get(prod)[i];
 				int[] nextState = new int[UserState.values().length];
 				//double transP = curPart.getTransProb();
 				//double transV = curPart.getTransVar();
 				//double next_prob = randNorm(transP, transP*transV);
 				//double next_var = curPart.getNextTransVar();
 				//for each current state
 				for(int curState = 0; curState<UserState.values().length; curState++){
 					double[] transProbs = new double[UserState.values().length];
 					//create the transition prob to each state
 					for(int j = 0; j < transProbs.length; j++){
 						double curBaseProb;
 						/*if(_R.nextDouble()<=BURST_PROB){
 							curBaseProb = BURST_MARKOV_PROBS[curState][j];
 						}
 						else
 						{*/
 							curBaseProb = STANDARD_MARKOV_PROBS[curState][j];
 						//}
 						if(curBaseProb == 0){
 							transProbs[j]=0;
 						}
 						else
 						{
 							transProbs[j]=randBinomToNorm(NUM_USERS_PER_PROD, curBaseProb);
 						}
 					}
 					if(curState==2){
 						 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, next_prob);
 						transProbs[5] = randNorm(INIT_TRANS_PROB, INIT_TRANS_PROB*INIT_TRANS_VAR);
 					} else if(curState==3){
 						 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, 2*next_prob);
 						transProbs[5] = randNorm(2*INIT_TRANS_PROB, INIT_TRANS_PROB*INIT_TRANS_VAR*2);
 					} else if(curState==4){
 						 //transProbs[5] = randBinomToNorm(NUM_USERS_PER_PROD, 3*next_prob);						 transProbs[5] = next_prob*3;
 						transProbs[5] = randNorm(3*INIT_TRANS_PROB, 3*INIT_TRANS_PROB*INIT_TRANS_VAR);
 					}
 					normalizeToOne(transProbs);
 					for(int j = 0; j < transProbs.length; j++){
 						nextState[j] += curPart.getStateCount(UserState.values()[curState])*transProbs[j];
 					}
 				}
 				int curSum = 0;
 				for(int j = 0; j < nextState.length; j++){
 					curSum += nextState[j];
 				}
 				while(curSum<NUM_USERS_PER_PROD){
 					nextState[0] += 1;
 					curSum++;
 				}
 				while(curSum>NUM_USERS_PER_PROD){
 					nextState[0] -= 1;
 					curSum--;
 				}
 				//System.out.println("Users: " + curSum);
 				
 				//System.out.println("Particle " + i);
 				newArr[i] = new Particle(nextState, curPart.getWeight()/*, next_prob, next_var*/);
 			}
 			toRet.put(prod, newArr);
 		}
 		//System.out.println("next day");		//Particle[] theParts = _particles.get(product);
 		for(Product prod : _products) {
 	//		System.out.println("USERS WITH PREFERENCE " + prod.getComponent()+ ", "+ prod.getManufacturer()+": (particle 0) NS:" + toRet.get(prod)[0].getStateCount(UserState.NS) + ", IS:" + toRet.get(prod)[0].getStateCount(UserState.IS)+ ", F0:" + toRet.get(prod)[0].getStateCount(UserState.F0)+ ", F1:" + toRet.get(prod)[0].getStateCount(UserState.F1)+ ", F2:" + toRet.get(prod)[0].getStateCount(UserState.F2)+ ", T:" + toRet.get(prod)[0].getStateCount(UserState.T));
 		}
 		return toRet;
 	}
 
 	private HashMap<Product,Particle[]> getTwoSteps(){
 		if(_two_steps == null){
 			_two_steps = nextDayPartMap(getNextStep());
 		}
 		return _two_steps;
 	}
 
 	@Override
 	public int getCurrentEstimate(Product product, UserState userState) {
 		Particle[] theParts = _particles.get(product);
 		double curSum = 0.0;
 		double curWeight = 0.0;
 		for(int i =0; i<theParts.length; i++){
 			curSum+=theParts[i].getStateCount(userState)*theParts[i]._weight;
 			curWeight+=theParts[i]._weight;
 		}
 		return (int)(curSum/curWeight);
 	}
 	
 	public static void main(String[] args){
 		EricParticleFilter myPF = new EricParticleFilter();
 		myPF.initializeNumParticles();
 		myPF.simVirtualDays();
 		try {
 			myPF.saveParticlesToFile(myPF._particles.get(new Product("flat","tv")));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Initializes NUM_PARTICLES particles for each product all in the initial user state with 1/NUM_PARTICLES weight each
 	 */
 	private void initializeNumParticles() {
 		for(Product prod : _products) {
 			Particle[] particles = new Particle[NUM_PARTICLES];
 			for(int i = 0; i < particles.length; i++) {
 				Particle particle = new Particle();
 				particles[i] = particle;
 			}
 			_particles.put(prod, particles);
 		}
 		
 	}
 	
 	/**
 	 * Simulates virtual days by moving the particles through their markov chains (no transactions)
 	 */
 	private void simVirtualDays(){
 		//For (5) days
 		for(int day = 0; day <VIRTUAL_INIT_DAYS; day++){
 			//for each products particle filter
 			System.out.println("Computing day " + day);
 			for(Product prod : _products) {
 				//System.out.println("Computing product " + prod.getComponent()+ ", "+ prod.getManufacturer());
 				//for each particle
 				for(int i = 0; i < NUM_PARTICLES; i++) {
 					Particle curPart = _particles.get(prod)[i];
 					int[] nextState = new int[UserState.values().length];
 					//for each current state
 					for(int curState = 0; curState<UserState.values().length; curState++){
 						double[] transProbs = new double[UserState.values().length];
 						//create the transition prob to each state
 						for(int j = 0; j < transProbs.length; j++){
 							double curBaseProb;
 							if(_R.nextDouble()<=BURST_PROB){
 								//Burst
 								curBaseProb = BURST_MARKOV_PROBS[curState][j];
 							}
 							else
 							{
 								//Regular
 								curBaseProb = STANDARD_MARKOV_PROBS[curState][j];
 							}
 							//System.out.println("Got 1");
 							if(curBaseProb == 0){
 								transProbs[j]=0;
 							}
 							else
 							{
 							//	System.out.println("Got 2");
 								transProbs[j]=randBinomToNorm(NUM_USERS_PER_PROD, curBaseProb);
 							//	System.out.println("Got 3");
 							}
 						}
 						normalizeToOne(transProbs);
 						for(int j = 0; j < transProbs.length; j++){
 							nextState[j] += curPart.getStateCount(UserState.values()[curState])*transProbs[j];
 						}
 					}
 					int curSum = 0;
 					for(int j = 0; j < nextState.length; j++){
 						curSum += nextState[j];
 					}
 					while(curSum<NUM_USERS_PER_PROD){
 						nextState[0] += 1;
 						curSum++;
 					}
 					while(curSum>NUM_USERS_PER_PROD){
 						nextState[0] -= 1;
 						curSum--;
 					}
 					//System.out.println("Users: " + curSum);
 					_particles.get(prod)[i]= new Particle(nextState);
 					//System.out.println("Particle " + i);
 				}
 			}
 			for(Product prod : _products) {
 	//			System.out.println("USERS WITH PREFERENCE " + prod.getComponent()+ ", "+ prod.getManufacturer()+": (particle 0) NS:" + _particles.get(prod)[0].getStateCount(UserState.NS) + ", IS:" + _particles.get(prod)[0].getStateCount(UserState.IS)+ ", F0:" + _particles.get(prod)[0].getStateCount(UserState.F0)+ ", F1:" + _particles.get(prod)[0].getStateCount(UserState.F1)+ ", F2:" + _particles.get(prod)[0].getStateCount(UserState.F2)+ ", T:" + _particles.get(prod)[0].getStateCount(UserState.T));
 			}
 		}
 	}
 	
 	/**
 	 * Generates a random number form a normal distribution with mean and variance and a number that is between 1 and 0
 	 * @param mean the mean of the norm dist
 	 * @param variance the variance the norm dist
 	 * @return a normally distributed random double that is between 1 and 0
 	 */
 	public double randBinomToNorm(int numTrials, double curTransProb) throws IndexOutOfBoundsException{
 		if(curTransProb > 1 || curTransProb<0){
 			throw new IndexOutOfBoundsException("WTF");
 		}
 		double mean = curTransProb*numTrials;
 		double variance = curTransProb*(1-curTransProb)*numTrials;
 		double toRet = -1;
 		while(toRet > 1.0 || toRet < 0){
 			toRet = (_R.nextGaussian() * Math.sqrt(variance) + mean)/numTrials;
 		}
 		return toRet;
 	}
 	
 	private double randNorm(double mean, double variance){
 		double toRet = -1;
 		while(toRet > 1.0 || toRet < 0){
 			toRet = (_R.nextGaussian() * Math.sqrt(variance) + mean);
 		}
 		return toRet;
 	}
 	
 	public static  void normalizeToOne(double[] theArr){
 		double curSum = 0;
 		for(int i = 0; i<theArr.length; i++){
 			curSum += theArr[i];
 		}
 		for(int i = 0; i<theArr.length; i++){
 			theArr[i] = theArr[i]/curSum;
 		}
 	}
 
 }
