 import com.cyberbotics.webots.controller.*;
 import games.Game;
 import util.FilesFunctions;
 import util.Util;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: annapawlicka
  * Date: 01/03/2013
  * Time: 19:50
  * E-puck controller that controls the evolution of games and runs trials. All sensorimotor inputs come from this
  * controller.
  * Fitness function of games is the variance of the actors (Fisher's Law).
  */
 
 // TODO save the best indiv and test it
 
 public class EpuckController extends Robot {
 
     // Global variables
     private int GAME_POP_SIZE = 1;
     private int NN_POP_SIZE = 50;
     private final int LEFT = 0;
     private final int RIGHT = 1;
     private final int TIME_STEP = 128;              // [ms]
     private final int PS_RANGE = 3800;
     private final int SPEED_RANGE = 500;
     private final int NB_DIST_SENS = 8;             // Number of IR proximity sensors
     private final double OBSTACLE_THRESHOLD = 3000;
     private final int TRIAL_DURATION = 60000;       // Evaluation duration of one individual - 30 sec [ms]
     private final int NB_INPUTS = 7;
     private final int NB_OUTPUTS = 2;
     private int NB_WEIGHTS = NB_INPUTS * NB_OUTPUTS + NB_OUTPUTS;   // No hidden layer
     private int NB_CONSTANTS = 5;
     private float weights[];
 
     // Evolution of games
     private Game[] populationOfGames;
     private double[] gameFitness;                           // Fitness of games (variance of actors)
     private double[] sumOfFitnesses;                        // Sum of fitnesses of each actor (total of all games)
     private double[][] sortedfitnessGames;                  // Population of games sorted byte fitness
     private double[][] agentsFitness;                       // Fitness of agents for each game
     private double ELITISM_RATIO = 0.1;
     private double REPRODUCTION_RATIO = 0.4;                // If not using roulette wheel (truncation selection), we need reproduction ratio
     private double CROSSOVER_PROBABILITY = 0.5;             // Probability of having a crossover
     private double MUTATION_PROBABILITY = 0.1;              // Probability of mutating each weight-value in a genome    private int GENE_MIN = -1;                              // Range of genes: minimum value
     private int GENE_MIN = -1;
     private int GENE_MAX = 1;                               // Range of genes: maximum value
     private double MUTATION_SIGMA = 0.2;                    // Mutations follow a Box-Muller distribution from the gene with this sigma
     private int generation = 0;                             // Generation counter
     //If 1, evolution takes place. If 0, then the best individual obtained during the previous evolution is tested for an undetermined amount of time.
     private int EVOLVING = 1;
 
     //Log variables
     private double minFitGame = 0.0, avgFitGame = 0.0, bestFitGame = 0.0, absBestFitGame = 0.0;
     private int bestGame = -1, absBestGame = -1;
 
     // Mode of robot
     private static int mode;
     private final int SIMULATION = 0;               // for robot.get_mode() function
     private final int REALITY = 2;                  // for robot.get_mode() function
 
     // 8 IR proximity sensors
     private int proximitySensorsNo = 8;
     private DistanceSensor[] ps;
     private float[] ps_offset;
     private int[] PS_OFFSET_SIMULATION = new int[]{300, 300, 300, 300, 300, 300, 300, 300};
     private int[] PS_OFFSET_REALITY = new int[]{480, 170, 320, 500, 600, 680, 210, 640};
 
     // 3 IR floor color sensors
     private int floorSensorsNo = 3;
     private DistanceSensor[] fs;
     private double[] fs_value = new double[]{0, 0, 0};
     private double maxIRActivation;
 
     // Light sensor
     private LightSensor lightSensorRight;
     private LightSensor lightSensorLeft;
     private double ls_value_left;
     private double ls_value_right;
 
     // LEDs
     private int ledsNo = 8;
     private LED[] led = new LED[ledsNo];
 
     // Differential Wheels
     private DifferentialWheels robot = new DifferentialWheels();
     private double[] speed;
 
     // GPS
     private GPS gps;
     private double[] position;
     private double[] states = new double[11];               // The sensor values  8+3
 
     // Emitter and Receiver
     private Emitter emitter;
     private Receiver receiver;
     private Emitter gameEmitter;
     private Receiver gameReceiver;
 
     // Logging
     private BufferedWriter out1, out2, out3, out4, out5;
     private FileWriter file1, file2, file3, file4, file5;
 
     private int step;
     private Random random = new Random();
 
     private int indiv;
 
 
     public void run() throws Exception {
 
         while (step(TIME_STEP) != -1) {
 
             int i;
             if (mode != getMode()) {
                 mode = getMode();
                 if (mode == SIMULATION) {
                     for (i = 0; i < NB_DIST_SENS; i++) ps_offset[i] = PS_OFFSET_SIMULATION[i];
                     System.out.println("Switching to SIMULATION.\n\n");
                 } else if (mode == REALITY) {
                     for (i = 0; i < NB_DIST_SENS; i++) ps_offset[i] = PS_OFFSET_REALITY[i];
                     System.out.println("\nSwitching to REALITY.\n\n");
                 }
             }
 
             int m = gameReceiver.getQueueLength();
             if (m > 0) {
                 byte[] flag = gameReceiver.getData();
 
                 if (flag[0] == 1) { // is flag 1 is received, evolution can be started (the frequency is set by supervisor)
 
                     /* Start evolution of games */
 
                     // 1. Calculate fitness of each game by computing variance of actor fitnesses on that game
                     setGameFitness();
                     // 2. Sort populationOfGames by fitness
                     sortPopulation(sortedfitnessGames, gameFitness);
                     // 3. Find best, average and worst game
                     bestFitGame = sortedfitnessGames[0][0]; // fitness score of best indiv
                     minFitGame = sortedfitnessGames[GAME_POP_SIZE - 1][0];  // fitness score of worst indiv
                     bestGame = (int) sortedfitnessGames[0][1]; // index of best individual
                     avgFitGame = util.Util.mean(gameFitness);
                     // 4. Log best, average and worst fitness score - writes to the file
                     if (bestFitGame > absBestFitGame) {
                         absBestFitGame = bestFitGame;
                         absBestGame = bestGame;
                         FilesFunctions.logBest(out3, generation, NB_CONSTANTS, absBestGame, populationOfGames);
                     }
                     System.out.println("Best game fitness score: \n" + bestFitGame);
                     System.out.println("Average game fitness score: \n" + avgFitGame);
                     System.out.println("Worst game fitness score: \n" + minFitGame);
 
                     // 5. Write data to files
                     FilesFunctions.logFitnessCases(out1, avgFitGame, generation, bestFitGame, minFitGame);
                     FilesFunctions.logCompFitnesses(out2, generation, gameFitness);
                     // Log the generation data  - stores constants
                     try {
                         FilesFunctions.logLastGeneration(populationOfGames);
                     } catch (IOException e) {
                         e.getMessage();
                     }
 
                     try {
                         FilesFunctions.logAllGameGenomes(out5, generation, populationOfGames);
                     } catch (IOException e) {
                         System.err.println(e.getMessage());
                     }
 
                     // 6. Rank populationOfGames, select best individuals and create new generation
                     //createNewPopulation();
 
                     // 7. Reset evolution variables
                     generation++;
                     //System.out.println("\nGAME GENERATION \n" + generation);
                     avgFitGame = 0.0;
                     bestFitGame = 0;
                     bestGame = 0;
                     minFitGame = 0;
                     // Reset all fitness arrays
                     resetAllFitnessArrays();
 
                 }
                 gameReceiver.nextPacket();
             }
             if (step == 0) {
                 int n = receiver.getQueueLength();
                 // Wait for new genome
                 if (n > 0) {
                     byte[] genes = receiver.getData();
                     // Set neural network weights
                     for (i = 0; i < NB_WEIGHTS; i++) weights[i] = genes[i];
                     receiver.nextPacket();
 
                 }
             }
 
             step++;
 
             if (step < TRIAL_DURATION / TIME_STEP) {
                 // Drive robot
                 runTrial();
             } else {
                 // Add all fitnesses for each game
                 setGameFitness();
                 // Send message to indicate end of trial - next actor will be called
                 float msg[] = {0};
                 byte[] msgInBytes = Util.float2Byte(msg);
                 emitter.send(msgInBytes);
                 // Reinitialize counter
                 step = 0;
 
                 // If all individuals finished their trials
                 if ((indiv + 1) < NN_POP_SIZE) {
                     indiv++;
                 } else {
                     indiv = 0;
                     // Send normalised fitness scores to supervisor
                     for (i = 0; i < sumOfFitnesses.length; i++) {
                         float message[] = {(float) sumOfFitnesses[i], i, 2};
                         byte[] messageInBytes = Util.float2Byte(message);
                         emitter.send(messageInBytes);
                     }
                     float end[] = {1};
                     byte[] endMsg = Util.float2Byte(end);
                     emitter.send(endMsg);
                 }
             }
 
         }
     }
 
     /**
      * A single trial during which one action is performed.
      *
      * @return Returns current fitness score
      */
     private void runTrial() throws Exception {
 
         double[] outputs = new double[NB_OUTPUTS];
 
         updateSenorReadings();
         run_neural_network(states, outputs);
 
         speed[LEFT] = SPEED_RANGE * outputs[0];
         speed[RIGHT] = SPEED_RANGE * outputs[1];
 
         // Set wheel speeds to output values
         robot.setSpeed(speed[LEFT], speed[RIGHT]);
 
         // Stop the robot if it is against an obstacle
         for (int i = 0; i < NB_DIST_SENS; i++) {
             double tmpps = (((ps[i].getValue()) - ps_offset[i]) < 0) ? 0 : ((ps[i].getValue()) - ps_offset[i]);
 
             if (OBSTACLE_THRESHOLD < tmpps) {// proximity sensors
                 speed[LEFT] = 0;
                 speed[RIGHT] = 0;
                 break;
             }
         }
         // light: 0 - white, approx 1400 - black
         double light = (ls_value_right + ls_value_left) / 2;
 
         computeFitness(speed, position, maxIRActivation, fs_value[1], light);
     }
 
 
     /**
      * Method to calculate fitness score - fitness function that have evolvable constants
      *
      * @param speed
      * @param position
      * @param maxIRActivation
      * @param floorColour
      * @param light
      */
     public void computeFitness(double[] speed, double[] position, double maxIRActivation, double floorColour,
                                double light) throws Exception {
 
         // Avoid obstacles:
         //agentsFitness[indiv][0] += Util.mean(speed) * (1 - Math.sqrt( (Math.abs((speed[LEFT]-speed[RIGHT]))) * (1-Util.normalize(0, 4000, maxIRActivation))) );
 
         // Follow wall
         //agentsFitness[indiv][1] += Util.mean(speed) * Util.normalize(0, 4000, maxIRActivation);
 
         // Follow black line
         //agentsFitness[indiv][2] += Util.mean(speed) * (1 - Math.sqrt( (Math.abs((speed[LEFT]-speed[RIGHT]))) * (1-Util.normalize(0, 900, floorColour))) );
 
         // Go to the light source
         agentsFitness[indiv][0] += (1 - Util.normalize(0, 1400, light));
 
 
         /*for (int i = 0; i < GAME_POP_SIZE; i++) {
             try {
                 agentsFitness[indiv][i] +=
                         (populationOfGames[i].getConstants()[0] * Util.mean(speed)) *
                                 (1 - (populationOfGames[i].getConstants()[1] * Math.sqrt(Math.abs((speed[LEFT] - speed[RIGHT]))))) *
                                 (1 - (populationOfGames[i].getConstants()[2] * Util.normalize(0, 4000, maxIRActivation))) *
                                 (1 - (populationOfGames[i].getConstants()[3] * Util.normalize(0, 900, floorColour))) *
                                 (populationOfGames[i].getConstants()[4] * light);
             } catch (Exception e) {
                 System.err.println("Error: " + e.getMessage());
             }
         }*/
     }
 
     /**
      * Reset all fitness arrays
      */
     private void resetAllFitnessArrays() {
         int i;
         for (i = 0; i < sumOfFitnesses.length; i++) sumOfFitnesses[i] = 0;
         for (i = 0; i < agentsFitness.length; i++) {
             for (int j = 0; j < agentsFitness[i].length; j++) agentsFitness[i][j] = 0;
         }
         for (i = 0; i < gameFitness.length; i++) gameFitness[i] = 0;
         for (i = 0; i < sortedfitnessGames.length; i++) {
             for (int j = 0; j < sortedfitnessGames[i].length; j++) sortedfitnessGames[i][j] = 0;
         }
     }
 
     /**
      * Fill in gameFitness array with each individual's score
      */
     private void setGameFitness() {
         int i, j;
         double[][] actorFitPerGame = new double[GAME_POP_SIZE][NN_POP_SIZE];
         for (i = 0; i < agentsFitness.length; i++) {
             for (j = 0; j < agentsFitness[i].length; j++) {
                 actorFitPerGame[j][i] = agentsFitness[i][j];
             }
         }
 
         // Normalise
         normaliseFitnessScore(actorFitPerGame);
         // Fitness of games doesn't need to be normalised as it's a variance over already normalised actors fitness
         for (i = 0; i < gameFitness.length; i++) gameFitness[i] = Util.variance(actorFitPerGame[i]);
 
         // Update (sum up) array of actors fitness scores so that it can be sent to supervisor
         for (i = 0; i < actorFitPerGame.length; i++) {
             for (j = 0; j < actorFitPerGame[i].length; j++) {
                 sumOfFitnesses[j] += actorFitPerGame[i][j];
             }
         }
 
         FilesFunctions.logAllCompFit(out4, actorFitPerGame, generation);
 
     }
 
     /**
      * Normalise fitness scores to a value between 0 and 1
      *
      * @param fitnessScores
      */
     private void normaliseFitnessScore(double[][] fitnessScores) {
         int i, j;
         double min, max;
 
         for (i = 0; i < fitnessScores.length; i++) {
             min = Util.min(fitnessScores[i]);   // find min and max separately for each game
             max = Util.max(fitnessScores[i]);
             for (j = 0; j < fitnessScores[i].length; j++) {
                 double temp = 0;
                 try {
                     temp = Util.normalize(min, max, fitnessScores[i][j]);
                 } catch (Exception e) {
                     System.err.println("Error while normalizing: " + e.getMessage());
                 }
                 fitnessScores[i][j] = temp;
             }
         }
     }
 
 
     /**
      * Sort whole population according to fitness score of each individual. Uses quickSort.
      */
     private void sortPopulation(double[][] sortedfitness, double[] fitness) {
         int i;
         //sort populationOfNN by fitness
         for (i = 0; i < sortedfitness.length; i++) {
             sortedfitness[i][0] = fitness[i];   //fitness score
             sortedfitness[i][1] = i;            //keep index
         }
         quickSort(sortedfitness, 0, sortedfitness.length - 1);
     }
 
     /**
      * Standard fast algorithm to sort populationOfNN by fitness
      *
      * @param fitness Array that stores fitness and index of each individual.
      * @param left    Min index of the array
      * @param right   Max index of the array
      */
     private void quickSort(double fitness[][], int left, int right) {
         double[] pivot = new double[2];
         int l_hold, r_hold;
 
         l_hold = left;
         r_hold = right;
         pivot[0] = fitness[left][0];
         pivot[1] = fitness[left][1];
         while (left < right) {
             while ((fitness[right][0] <= pivot[0]) && (left < right))
                 right--;
             if (left != right) {
                 fitness[left][0] = fitness[right][0];
                 fitness[left][1] = fitness[right][1];
                 left++;
             }
             while ((fitness[left][0] >= pivot[0]) && (left < right))
                 left++;
             if (left != right) {
                 fitness[right][0] = fitness[left][0];
                 fitness[right][1] = fitness[left][1];
                 right--;
             }
         }
         fitness[left][0] = pivot[0];
         fitness[left][1] = pivot[1];
         pivot[0] = left;
         left = l_hold;
         right = r_hold;
         if (left < (int) pivot[0]) quickSort(fitness, left, (int) pivot[0] - 1);
         if (right > (int) pivot[0]) quickSort(fitness, (int) pivot[0] + 1, right);
     }
 
     /**
      * Based on the fitness of the last generation, generate a new games for the next generation.
      */
     private void createNewPopulation() {
 
         Game[] newpop = new Game[GAME_POP_SIZE];
         for (int i = 0; i < newpop.length; i++) {
             newpop[i] = new Game(true, NB_CONSTANTS);
         }
         double elitism_counter = GAME_POP_SIZE * ELITISM_RATIO;
         int i, j;
 
         // Create new populationOfNN
         for (i = 0; i < GAME_POP_SIZE; i++) {
 
             // The elitism_counter best individuals are simply copied to the new populationOfNN
             if (i < elitism_counter) {
                 for (j = 0; j < NB_CONSTANTS; j++)
                     newpop[i].setConstants(j, populationOfGames[(int) sortedfitnessGames[i][1]].getConstants()[j]);
             }
             // The other individuals are generated through the crossover of two parents
             else {
 
                 // Select non-elitist individual
                 int ind1;
                 ind1 = (int) (elitism_counter + random.nextFloat() * (GAME_POP_SIZE * REPRODUCTION_RATIO - elitism_counter));
 
                 // If we will do crossover, select a second individual
                 if (random.nextFloat() < CROSSOVER_PROBABILITY) {
                     int ind2;
                     do {
                         ind2 = (int) (elitism_counter + random.nextFloat() * (GAME_POP_SIZE * REPRODUCTION_RATIO - elitism_counter));
                     } while (ind1 == ind2);
                     ind1 = (int) sortedfitnessGames[ind1][1];
                     ind2 = (int) sortedfitnessGames[ind2][1];
                     newpop[i].crossover(ind1, ind2, newpop[i], NB_CONSTANTS, populationOfGames);
                 } else { //if no crossover was done, just copy selected individual directly
                     for (j = 0; j < NB_CONSTANTS; j++)
                         newpop[i].setConstants(j, populationOfGames[(int) sortedfitnessGames[ind1][1]].getConstants()[j]);
                 }
             }
         }
 
         // Mutate new populationOfGames and copy back to pop
         for (i = 0; i < GAME_POP_SIZE; i++) {
             if (i < elitism_counter) { //no mutation for elitists
                 for (j = 0; j < NB_CONSTANTS; j++) {
                     populationOfGames[i].copy(newpop[i]);
                 }
             } else { // Mutate others with probability per gene
                 for (j = 0; j < NB_CONSTANTS; j++)
                     if (random.nextFloat() < MUTATION_PROBABILITY)
                         populationOfGames[i].setConstants(j, populationOfGames[i].mutate(GENE_MIN, GENE_MAX, newpop[i].getConstants()[j], MUTATION_SIGMA));
                     else
                         populationOfGames[i].copy(newpop[i]);
             }
             // Reset fitness
             resetAllFitnessArrays();
         }
     }
 
 
     /**
      * Read values from sensors into arrays.
      */
     private void updateSenorReadings() {
 
         maxIRActivation = 0;
         for (int j = 0; j < NB_INPUTS; j++) {
             states[j] = ps[j].getValue() - ps_offset[j] < 0 ? 0 : (ps[j].getValue() - (ps_offset[j]) / PS_RANGE);
             //get max IR activation
             if (states[j] > maxIRActivation) maxIRActivation = states[j];
         }
 
         for (int i = 0; i < floorSensorsNo; i++) {
             fs_value[i] = fs[i].getValue();
         }
 
         //Get position of the e-puck
         position = gps.getValues();
 
         // Get reading from light sensors
         ls_value_right = lightSensorRight.getValue();
         ls_value_left = lightSensorLeft.getValue();
 
     }
 
     private void run_neural_network(double[] inputs, double[] outputs) {
         int i, j;
         int weight_counter = 0;
 
         for (i = 0; i < NB_OUTPUTS; i++) {
             double sum = 0.0;
             for (j = 0; j < NB_INPUTS; j++) {
                 sum += inputs[j] * weights[weight_counter];
                 weight_counter++;
             }
             outputs[i] = Math.tanh(sum + weights[weight_counter]);
             weight_counter++;
         }
     }
 
     private void initialiseGames(Game[] games) {
 
         /*Game 1: Avoid obstacles */
       /*  games[0].setConstants(0, 1);    // Drive fast
         games[0].setConstants(1, 1);    // Try to steer straight
         games[0].setConstants(2, 1);    // Minimise IR proximity sensors activation
         games[0].setConstants(3, 0);    // Ignore floor colour/light
 
 
         *//*Game 2: Follow black line *//*
         games[1].setConstants(0, 1);    // Drive fast
         games[1].setConstants(1, 1);    // Drive straight
         games[1].setConstants(2, 0);    // Avoid obstacles/walls
         games[1].setConstants(3, 1);    // Max black line /light
 
         *//* Game 3: Follow the wall *//*
         games[2].setConstants(0, 1);    // Drive fast
         games[2].setConstants(1, 1);    // Drive straight
         games[2].setConstants(2, -0.5f);   // Maximise prox sensors activation
         games[2].setConstants(3, 0);    // Max black line/light*/
 
         /* Follow the light */
         games[0].setConstants(0, 1);    // Drive fast
         games[0].setConstants(1, 1);    // Drive straight
         games[0].setConstants(2, 0);    // Min prox sensor activation
         games[0].setConstants(3, 1);    // Black line/light
 
 
     }
 
     /**
      * Method to initialise e-puck's sensors and data structures/variables.
      */
     public void reset() {
 
         int i, j;
         mode = 1;
         step = 0;
         indiv = 0;
 
         // Games
         populationOfGames = new Game[GAME_POP_SIZE];
         for (i = 0; i < GAME_POP_SIZE; i++) populationOfGames[i] = new Game(false, NB_CONSTANTS);
         initialiseGames(populationOfGames);
 
         sumOfFitnesses = new double[NN_POP_SIZE];
         for (i = 0; i < GAME_POP_SIZE; i++) sumOfFitnesses[i] = 0.0;
 
         sortedfitnessGames = new double[GAME_POP_SIZE][2];
         for (i = 0; i < GAME_POP_SIZE; i++) {
             for (j = 0; j < 2; j++) {
                 sortedfitnessGames[i][j] = 0.0;
             }
         }
         gameFitness = new double[GAME_POP_SIZE];
         for (i = 0; i < GAME_POP_SIZE; i++) gameFitness[i] = 0.0f;
 
         // Agents
         agentsFitness = new double[NN_POP_SIZE][GAME_POP_SIZE];
         for (i = 0; i < agentsFitness.length; i++) {
             for (j = 0; j < agentsFitness[i].length; j++) agentsFitness[i][j] = 0;
         }
 
         /* Initialise IR proximity sensors */
         ps = new DistanceSensor[proximitySensorsNo];
         ps[0] = getDistanceSensor("ps0");
         ps[0].enable(TIME_STEP);
         ps[1] = getDistanceSensor("ps1");
         ps[1].enable(TIME_STEP);
         ps[2] = getDistanceSensor("ps2");
         ps[2].enable(TIME_STEP);
         ps[3] = getDistanceSensor("ps3");
         ps[3].enable(TIME_STEP);
         ps[4] = getDistanceSensor("ps4");
         ps[4].enable(TIME_STEP);
         ps[5] = getDistanceSensor("ps5");
         ps[5].enable(TIME_STEP);
         ps[6] = getDistanceSensor("ps6");
         ps[6].enable(TIME_STEP);
         ps[7] = getDistanceSensor("ps7");
         ps[7].enable(TIME_STEP);
 
         ps_offset = new float[NB_DIST_SENS];
         for (i = 0; i < ps_offset.length; i++) {
             ps_offset[i] = PS_OFFSET_SIMULATION[i];
         }
 
         maxIRActivation = 0;
 
         /* Enable GPS sensor to determine position */
         gps = new GPS("gps");
         gps.enable(TIME_STEP);
         position = new double[3];
         for (i = 0; i < position.length; i++) {
             position[i] = 0.0f;
         }
 
         /* Initialise LED lights */
         for (i = 0; i < ledsNo; i++) {
             led[i] = getLED("led" + i);
         }
 
         /* Initialise light sensor */
         lightSensorRight = getLightSensor("ls0");
         lightSensorRight.enable(TIME_STEP);
         lightSensorLeft = getLightSensor("ls7");
         lightSensorLeft.enable(TIME_STEP);
 
         /* Initialise IR floor sensors */
         fs = new DistanceSensor[floorSensorsNo];
         for (i = 0; i < fs.length; i++) {
             fs[i] = getDistanceSensor("fs" + i);
             fs[i].enable(TIME_STEP);
         }
 
         /* Initialise states array */
         for (i = 0; i < states.length; i++) {
             states[i] = 0.0;
         }
         speed = new double[2];
         // Speed initialization
         speed[LEFT] = 0;
         speed[RIGHT] = 0;
 
         emitter = getEmitter("emitterepuck");
         receiver = getReceiver("receiver");
         receiver.enable(TIME_STEP);
 
         gameEmitter = getEmitter("gamesemitterepuck");
         gameEmitter.setChannel(1);
         gameReceiver = getReceiver("gamesreceiverepuck");
         gameReceiver.setChannel(1);
         gameReceiver.enable(TIME_STEP);
 
         weights = new float[NB_WEIGHTS];
 
         // Logging
         try {
             file1 = new FileWriter("results.txt");
         } catch (IOException e) {
             System.out.println("Cannot open results.txt file.");
         }
 
         out1 = new BufferedWriter(file1);
         try {
             out1.write("generation , average fitness, worst fitness, best fitness");
             out1.write("\n");
 
         } catch (IOException e) {
             System.err.println("" + e.getMessage());
         }
 
         try {
             file2 = new FileWriter("results:fitness_games.txt");
         } catch (IOException e) {
             System.err.println("Cannot write to file: fitness_games.txt");
         }
         out2 = new BufferedWriter(file2);
         try {
             out2.write("Generation, ");
             for (i = 0; i < GAME_POP_SIZE; i++) out2.write("Game " + i + ", ");
             out2.write("\n");
         } catch (IOException e) {
             System.out.println("Error writing to genome.txt: " + e.getMessage());
         }
 
         try {
             file3 = new FileWriter("results:bestgenome_games.txt");
         } catch (IOException e) {
             System.err.println("Cannot open bestgenome_games.txt file.");
         }
 
         out3 = new BufferedWriter(file3);
 
         try {
             file4 = new FileWriter("results:comp_fitness.txt");
         } catch (IOException e) {
             System.err.println("Cannot open comp_fitness.txt file.");
         }
 
         out4 = new BufferedWriter(file4);
 
         try {
             file5 = new FileWriter("all_games_genomes.txt");
         } catch (IOException e) {
             System.err.println("Cannot open all_games_genomes.txt file.");
         }
 
         out5 = new BufferedWriter(file5);
         try {
             FilesFunctions.logAllGameGenomes(out5, generation, populationOfGames);
         } catch (IOException e) {
             System.err.println(e.getMessage());
         }
         System.out.println("e-puck has been initialised.");
     }
 
 
     public static void main(String[] args) throws Exception {
         EpuckController controller = new EpuckController();
         controller.reset();
         controller.run();
     }
 }
 
