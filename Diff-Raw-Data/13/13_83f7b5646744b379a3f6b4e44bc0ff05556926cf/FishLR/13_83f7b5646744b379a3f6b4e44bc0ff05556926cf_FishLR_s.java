 package biosim.app.fishlr;
 
 import biosim.core.agent.Agent;
 import biosim.core.body.AbstractFish;
 import biosim.core.body.NotemigonusCrysoleucas;
 import biosim.core.gui.GUISimulation;
 import biosim.core.sim.InitiallyPlacedEnvironment;
 import biosim.core.sim.Environment;
 import biosim.core.sim.Simulation;
 import biosim.core.sim.RectObstacle;
 import biosim.core.util.FastKNN;
 import biosim.core.util.BTFData;
 
 import sim.util.MutableDouble2D;
 import sim.util.Double3D;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.util.ArrayList;
 
 
 public class FishLR implements Agent{
 	AbstractFish fishBody;
 	FastKNN knn;
 	//so this toggles using the kNN result instead of LR
 	// VERY IMPORTANT: Set this to false if you want LR
 	public static boolean USE_KNN_INSTEAD=false;
 
 	/*	
 	OLD LR DATA
 	Speedingforce and turning force:
 	Front-back speed betas: [ 0.00145171  0.00013191]
 	Turn speed betas: [ -4.55837185e-05   5.36143576e-04]
 
 	dvel x and dvel theta:
 	Front-back speed betas: [ 0.07561636 -0.2372422 ]
 	Turn speed betas: [-15.30309163   1.93068044]
 
 	dvel, [avgX, avgY, wallX, wallY]:
 	Front-back speed betas: [-0.06393678  0.23871273  0.01978693 -0.30392783]
 	Turn speed betas: [ -1.73975683  -2.89316081 -16.0708904    6.23293313]
 
 	*/
 	
 	/*
 	public static final double[] FB_BETA = {-0.07561636, 0.2372422};
 	public static final double[] LR_BETA = {15,30309163, -1.93068044};
 	*/
 
 	/*
 	SIGMA REYNOLDS LR SOLUTION
 	best sigmas: [0.1, 1.5, 1.5, 1.5]
 	order of sensors: sep, ori, coh, obs, bias
 (array([[ -2.02403982e-01,   8.93711402e-04,   3.52450984e+00],
        [  6.08884012e-02,  -3.05827021e-03,  -1.37973827e+00],
        [  4.41927515e-02,  -4.43535998e-06,   5.39823109e-02],
        [  3.34279827e-03,  -7.80805057e-04,  -9.54571681e-02],
        [  2.32256714e-02,  -2.81105105e-04,  -3.53574268e-01],
        [ -7.59899506e-03,   1.30615089e-03,   1.67251760e-01],
        [ -2.21745818e-03,   6.62491466e-06,  -3.27223345e-01],
        [  2.48908629e-03,   8.88269855e-06,   2.83629926e-01],
        [  3.89097362e-02,  -1.33854517e-05,  -3.36568323e-02]])
 	*/
 
     public static final double SEP_SIGMA=0.1;	//0.1;
     public static final double ORI_SIGMA=0.2;	//1.5;
     public static final double COH_SIGMA=1.0;	//1.5;
     public static final double OBS_SIGMA=0.05;	//1.5;
 
     public static final int KNN_NEIGHBORS=10;
 
 
     public static final int NUM_FEATURES=9;
     public static double[] X_COMPONENTS = {
 		-2.02403982e-01,
  		 6.08884012e-02,
  		 4.41927515e-02,
  		 3.34279827e-03,
  		 2.32256714e-02,
  		-7.59899506e-03,
  		-2.21745818e-03,
  		 2.48908629e-03,
  		 3.89097362e-02
        // -2.10770959e-01,
   		 // 6.67828818e-02,
   		 // 4.43436505e-02,
   		 // 3.48721976e-03,
   		 // 2.39914970e-02,
   		// -9.14397136e-03,
   		 // 3.50109830e+00, //-1
   		// -2.47327886e+00, //0
   		 // 3.89091469e-02
     };
 
     public static double[] Y_COMPONENTS = {
 		 8.93711402e-04,
 	     3.05827021e-03,
 	     4.43535998e-06,
 	     7.80805057e-04,
 	     2.81105105e-04,
 	     1.30615089e-03,
 	     6.62491466e-06,
 	     8.88269855e-06,
 	     1.33854517e-05
 		// 8.99240632e-04,
  		// -3.11259705e-03,
  		// -5.05655416e-06,
  		// -7.81642848e-04,
  		// -2.80179755e-04,
  		//  1.29906958e-03,
  		// -8.37856684e-03,
  		//  3.29799210e-02,
  		// -1.26449585e-05
     };
 
     public static double[] THETA_COMPONENTS = {
     	3.52450984e+00,
        -1.37973827e+00,
         5.39823109e-02,
        -9.54571681e-02,
        -3.53574268e-01,
         1.67251760e-01,
        -3.27223345e-01,
         2.83629926e-01,
        -3.36568323e-02
 	// 2.74373862e+00,
  	// 	1.20491285e+00,
  	// 	6.03384357e-02,
  	// 	8.04566161e-02,
  	// 	2.04783577e-01,
  	// 	3.43014314e-02,
  	// 	1.01973882e+02, //0
  	// 	1.68133097e+02, //-2
  	// 	2.69606660e-02
     };
 
 	// public static final double[] COMBINED_BETA_X =  {	0.0238146803487, 	//xsep
 	// 													-0.00872664696674, 	//ysep
 	// 													0.0447434782331, 	//xalign
 	// 													0.00298139349923, 	//yalign
 	// 													-0.206953112024, 	//xcohes
 	// 													0.0608517994282, 	//ycohes
 	// 													-0.00232599514171, 	//xobst
 	// 													0.000129982389957, 	//yobst
 	// 													0.0391581524951};	//bias
 
 	// public static final double[] COMBINED_BETA_Y = {	0.000750508637173, 	//xsep
 	// 													-0.00354279959463, 	//ysep
 	// 													-3.45686904782e-06,	//xalign
 	// 													-0.000907278811423,	//yalign
 	// 													-0.000267705370536,	//xcohes
 	// 													0.00139186498825,	//ycohes
 	// 													4.3853175437e-06,	//xobst
 	// 													9.79859587638e-07,	//yobst
 	// 													-1.40972310414e-05};//bias
 
 	// public static final double[] INDEPENDENT_BETA_X = {	0.0231357101335,	//sep
 	// 													0.0447573654891, 	//align
 	// 													-0.207024332986, 	//cohes
 	// 													-0.00236147388765, 	//obst
 	// 													0.0392014263121};	//bias
 
 	// public static final double[] INDEPENDENT_BETA_Y = {	-0.00351918718575,	//sep
 	// 													-0.000901584863935,	//align
 	// 													0.00138962257189,	//cohes
 	// 													1.14346877271e-06,	//obst
 	// 													-1.48782713764e-05};//bias
 	/*	
 	public static final double[] FB_BETA = {0.07561636, -0.2372422};
 	public static final double[] LR_BETA = {15.30309163, -1.93068044};
 	/**/
 	/*
 	public static final double[] FB_BETA = {-0.06393678, 0.23871273,
 											0.01978693, -0.30392783};
 	public static final double[] LR_BETA = {-1.73975683, -2.89316081,
 											-16.0708904,  6.23293313};
 	/**/
 	/*
 	public static final double[] FB_BETA = {0.00831176, -0.0259669, 
 											-0.00091077, 0.00584585};
 	public static final double[] LR_BETA = {-0.06239644, 0.10435975, 
 											-0.10492971,  0.10604713};
 	*/
 	public double oldTime = 0.0;
 	public FishLR(AbstractFish b, FastKNN knn){
 		fishBody = b;
 		this.knn = knn;
 	}
 	public void init(){
 	}
 	public void finish(){
 	}
 	public void act(double time){
 		double[][] nearestK_classes = new double[KNN_NEIGHBORS][3];
 		double[][] nearestK_features = new double[KNN_NEIGHBORS][X_COMPONENTS.length-1];
 		// order of sensors: sep, ori, coh, obs, bias
 		MutableDouble2D sep = new MutableDouble2D();
 		MutableDouble2D ori = new MutableDouble2D();
 		MutableDouble2D coh = new MutableDouble2D();
 		MutableDouble2D wall = new MutableDouble2D();
 		fishBody.getAverageRBFSameTypeVec(sep,SEP_SIGMA);
 		fishBody.getAverageRBFOrientationSameTypeVec(ori,ORI_SIGMA);
 		fishBody.getAverageRBFSameTypeVec(coh,COH_SIGMA);
 		fishBody.getNearestObstacleVec(wall);
 		wall.multiplyIn(Math.exp(-wall.lengthSq()/(2.0*Math.pow(OBS_SIGMA,2))));
 		double[] sensors = new double[X_COMPONENTS.length];
 		double[] features = new double[sensors.length-1];
 		sensors[0] = sep.x;
 		sensors[1] = sep.y;
 		sensors[2] = ori.x;
 		sensors[3] = ori.y;
 		sensors[4] = coh.x;
 		sensors[5] = coh.y;
 		sensors[6] = wall.x * Math.exp(-(Math.pow(wall.x,2)+Math.pow(wall.y,2))/(2.0*Math.pow(OBS_SIGMA,2)));
 		sensors[7] = wall.y * Math.exp(-(Math.pow(wall.x,2)+Math.pow(wall.y,2))/(2.0*Math.pow(OBS_SIGMA,2)));
 		sensors[8] = 1.0;
 		double xvel = 0.0, yvel = 0.0, tvel = 0.0;
 		for(int i=0;i<sensors.length;i++){
 			xvel += X_COMPONENTS[i]*sensors[i];
 			yvel += Y_COMPONENTS[i]*sensors[i];
 			tvel += THETA_COMPONENTS[i]*sensors[i];
 			if(i<features.length){
 				features[i] = sensors[i];
 			}
 		}
 		if(knn != null || USE_KNN_INSTEAD){
 			knn.query(features,nearestK_classes,null,nearestK_features);
 			double avgDist = 0.0;
 			for(int i=0;i<KNN_NEIGHBORS;i++){
 				double tmpD = 0.0;
 				for(int j=0;j<X_COMPONENTS.length-1;j++){
 					tmpD += Math.pow(nearestK_features[i][j]-features[j],2);
 				}
 				avgDist += Math.sqrt(tmpD);
 			}
 			avgDist = avgDist/(double)KNN_NEIGHBORS;
 			// Ok, next we're going to actually use kNN instead of LR, JUST BECAUSE
 			if(USE_KNN_INSTEAD){
 				int rnd_idx = fishBody.getRandom().nextInt(nearestK_classes.length);
 				xvel = nearestK_classes[rnd_idx][0];
 				yvel = nearestK_classes[rnd_idx][1];
 				tvel = nearestK_classes[rnd_idx][2];
 			}
 			// System.out.println(avgDist);
 			((NotemigonusCrysoleucas)fishBody).setAvgDensity(avgDist);
 		}
 		// System.out.println("yvel:"+yvel);
 		fishBody.setDesiredVelocity(xvel, yvel, tvel);
 		oldTime = time;
 	}
 	
 	public static void loadLRCoeff(BufferedReader lrCoeffSrc) throws IOException {
 		ArrayList<String> coeffLines = new ArrayList<String>();
 		for(String line=null; lrCoeffSrc.ready();){
 			line = lrCoeffSrc.readLine();
 			coeffLines.add(line);
 		}
 		int readNumCoeffs = coeffLines.size();
 		if(NUM_FEATURES != readNumCoeffs){
 			System.out.println("[FishLR] WARNING! Number of parsed LR coefficients ("+readNumCoeffs+") different from NUM_FEATURES ("+NUM_FEATURES+")");
 		}
 		X_COMPONENTS = new double[readNumCoeffs];
 		Y_COMPONENTS = new double[readNumCoeffs];
 		THETA_COMPONENTS = new double[readNumCoeffs];
 		for(int i=0;i<readNumCoeffs;i++){
 			String[] tmp = coeffLines.get(i).split(" ");
 			X_COMPONENTS[i] = Double.parseDouble(tmp[0]);
 			Y_COMPONENTS[i] = Double.parseDouble(tmp[1]);
 			THETA_COMPONENTS[i] = Double.parseDouble(tmp[2]);
 		}
 	}
 
 	public static FastKNN loadKNN(BTFData btf) throws IOException{
 		System.out.println("[FishLR] Loading BTF data...");
 		FastKNN knn = new FastKNN(X_COMPONENTS.length-1,3);
 		String[] wallVec = btf.loadColumn("rbfwallvec");
 		String[] dvel = btf.loadColumn("dvel");
 		String[] dbool = btf.loadColumn("dbool");
 		String[] sepvec = btf.loadColumn("rbfsepvec");
 		String[] orivec = btf.loadColumn("rbforivec");
 		String[] cohvec = btf.loadColumn("rbfcohvec");
 		int numRows = dvel.length;
 		double[] sample = new double[X_COMPONENTS.length];
 		double[] classes = new double[3];
 		for(int i=0;i<numRows;i++){
 			String[] tmp = dbool[i].split(" ");
 			if(Boolean.parseBoolean(tmp[0])){
 				tmp = dvel[i].split(" ");
 				classes[0] = Double.parseDouble(tmp[0]);
 				classes[1] = Double.parseDouble(tmp[1]);
 				classes[2] = Double.parseDouble(tmp[2]);
 				tmp = sepvec[i].split(" ");
 				sample[0] = Double.parseDouble(tmp[0]);
 				sample[1] = Double.parseDouble(tmp[1]);
 				tmp = orivec[i].split(" ");
 				sample[0] = Double.parseDouble(tmp[0]);
 				sample[1] = Double.parseDouble(tmp[1]);
 				tmp = cohvec[i].split(" ");
 				sample[0] = Double.parseDouble(tmp[0]);
 				sample[1] = Double.parseDouble(tmp[1]);
 				tmp = wallVec[i].split(" ");
 				sample[6] = Double.parseDouble(tmp[0]);
 				sample[7] = Double.parseDouble(tmp[1]);
 				knn.add(sample,classes);
 			}
 		}
 		System.out.println("[FishLR] Done!");
 		return knn;
 	}
 
 	public static final double WIDTH=2.5;//2.5;
 	public static final double HEIGHT=1.5;//2.5;
 	
 	public static void main(String[] args){
 		try{
 			BTFData btf = null;
 			FastKNN knn = null;
 			Environment env = null;
 			boolean doKNNVis = false, initialPlacement = false, doGui = true, logging = false, walls = true;
 			BufferedReader poseSrc = null;
 			File loggingDir = null;
 			int numFish = 27; //30; //initial tracked number of fish is 27
 			for(int i=0;i<args.length;i++){
 				if(args[i].equalsIgnoreCase("-btf")){
 					btf = new BTFData();
 					btf.loadDir(new File(args[i+1]));
 				}
 				else if(args[i].equalsIgnoreCase("-vis")){
 					doKNNVis=true;
 				}
 				else if(args[i].equalsIgnoreCase("-lr")){
 					loadLRCoeff(new BufferedReader(new FileReader(args[i+1])));
 				}
 				else if(args[i].equalsIgnoreCase("-placed")){
 					initialPlacement = true;
 					poseSrc = new BufferedReader(new FileReader(args[i+1]));
 				}
 				else if(args[i].equalsIgnoreCase("-nogui")){
 					doGui = false;
 				}
 				else if(args[i].equalsIgnoreCase("-logging")){
 					logging = true;
 					if (i<args.length-1 && args[i+1].charAt(0)!='-'){
 						loggingDir = new File(args[i+1]);
 					}
 				}
 				else if(args[i].equalsIgnoreCase("-knn")){
 					USE_KNN_INSTEAD = true;
 				}
 				else if (args[i].equalsIgnoreCase("-nowalls")){
 					walls=false;
 				}
 			}
 			if(initialPlacement){
 				env = new InitiallyPlacedEnvironment(WIDTH,HEIGHT,1.0/30.0);
 				// ((InitiallyPlacedEnvironment)env).parseInitialPoses(btf);
 				numFish = ((InitiallyPlacedEnvironment)env).parseInitialPoses(poseSrc);
 			} else {
 				env = new Environment(WIDTH,HEIGHT,1.0/30.0);
 			}
 			if(doKNNVis || USE_KNN_INSTEAD){
 				knn = loadKNN(btf);
 			}
 			//set up the environment
 			int numLeaderFish = 0;//5;
 			if(walls){
 				env.addObstacle(new RectObstacle(0.01,HEIGHT), WIDTH-0.01,  0.0);//east wall
 				env.addObstacle(new RectObstacle(0.01,HEIGHT),  0.0,  0.0);//west
 				env.addObstacle(new RectObstacle(WIDTH,0.01),  0.0,  0.0);//north
 				env.addObstacle(new RectObstacle(WIDTH,0.01),  0.0, HEIGHT-0.01);//south
 			} else {
 				env.setToroidal(true);				
 			}
 			//add agents
 			NotemigonusCrysoleucas[] bodies = new NotemigonusCrysoleucas[numFish];
 			for(int i=0;i<bodies.length;i++){
 				bodies[i] = new NotemigonusCrysoleucas();
 				env.addBody(bodies[i]);
 			}
 		
 			Agent[] agents = new Agent[numFish];
 			for(int i=0; i< numLeaderFish;i++){
 				agents[i] = new LeaderFish(bodies[i]);
 				bodies[i].setAgent(agents[i]);
 			}
 			for(int i=numLeaderFish;i<agents.length;i++){
 				agents[i] = new FishLR(bodies[i],knn);
 				bodies[i].setAgent(agents[i]);
 			}
 						
 			if(logging){
 				FishLRLogger logger = null;
 				if(loggingDir == null){
 					logger = new FishLRLogger();
 				} else {
 					logger = new FishLRLogger(loggingDir);
 				}
 				logger.setSigmas(SEP_SIGMA,ORI_SIGMA,COH_SIGMA,OBS_SIGMA);
 				env.addLogger(logger);				
 			}
 			if(doGui){
 				Simulation sim = env.newSimulation();
 				GUISimulation gui = new GUISimulation(sim);
 				gui.setPortrayalClass(NotemigonusCrysoleucas.class, biosim.app.fishknn.FishPortrayal.class);
 				biosim.app.fishknn.FishPortrayal.AVG_DIST = 0.0449592693977;
 				biosim.app.fishknn.FishPortrayal.STD_DEV_DIST = 0.0235436856268;
 				biosim.app.fishknn.FishPortrayal.bi=null;
 				// gui.setDisplaySize((int)(WIDTH*380),(int)(HEIGHT*380));
 				gui.setDisplaySize((int)(WIDTH*500),(int)(HEIGHT*500));
 				gui.createController();				
 			} else {
 				env.runSimulation(args);
 			}
 		} catch(IOException ioe){
 			throw new RuntimeException(ioe);
 		}
 	}
 }
