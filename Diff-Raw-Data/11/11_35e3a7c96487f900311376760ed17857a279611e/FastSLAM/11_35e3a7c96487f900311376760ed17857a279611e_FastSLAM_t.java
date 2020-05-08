 package team;
 
 import java.awt.*;
 import java.util.*;
 
 import april.config.*;
 import april.jmat.*;
 import april.sim.*;
 import april.vis.*;
 
 public class FastSLAM implements Simulator.Listener
 {
     Config config;
     VisWorld vw;
 
     // Config vals
     double baseline;
     ArrayList<double[]> lmarkGroundTruth = new ArrayList<double[]>();
 
     // Particle management
     int NUM_PARTICLES;
     ArrayList<Particle> particles = new ArrayList<Particle>(NUM_PARTICLES);
 
     // Odometry info
     double[][] odomP;
 
     public void init(Config config_, VisWorld vw_)
     {
         config = config_;
         vw = vw_;
 
         NUM_PARTICLES = config.requireInt("particles.numParticles");
 
         baseline = config.requireDouble("robot.baseline_m");
         odomP = new double[2][2];
         double[] sigLsigR = config.requireDoubles("noisemodels.odometryDiag");
         odomP[0][0] = sigLsigR[0];
         odomP[1][1] = sigLsigR[1];
 
         for (int i = 0;;i++) {
             double[] xy = config.getDoubles("landmarks.l"+i, null);
             if (xy == null)
                 break;
             lmarkGroundTruth.add(xy);
         }
 
         // Init particles
         for (int i = 0; i < NUM_PARTICLES; i++) {
             particles.add(new Particle(config_, new double[3]));
         }
     }
 
     public void update(Simulator.odometry_t odom, ArrayList<Simulator.landmark_t> dets)
     {
        Random random = new Random(589134987);
 
         // Update particle positions by sampling around odom measurement
         //MultiGaussian mg = new MultiGaussian(odomP, odom.obs);
         MultiGaussian mg = new MultiGaussian(odomP, new double[2]);
         for (Particle p: particles) {
             double[] dLdR = mg.sample(random);
             dLdR[0] = odom.obs[0]*(1.0 + dLdR[0]);
             dLdR[1] = odom.obs[1]*(1.0 + dLdR[1]);
             //System.out.printf("%f,%f\n",dLdR[0],dLdR[1]);
             double[] local_xyt = new double[] {(dLdR[0] + dLdR[1])/2,
                                                 0,
                                                 Math.atan2((dLdR[1] - dLdR[0]), baseline)};
 
             p.updateLocation(local_xyt);
         }
 
         // Do feature matching (XXX for now, perfect). Specific to each
         // particle. (Data association)
         double weightTotal = 0;
         for (Particle p: particles) {
             weightTotal += p.associateAndUpdateLandmarks(dets);
         }
 
         ArrayList<Particle> newParticles = new ArrayList<Particle>(NUM_PARTICLES);
         for (int i = 0; i < NUM_PARTICLES; i++) {
             double rand = random.nextDouble()*weightTotal;
             //System.out.printf("%f -- %f\n", rand, weightTotal);
             //int cnt = 0;
             double weightSum = 0;
             for (Particle p: particles) {
                 weightSum += p.getWeight();
                 if (weightSum >= rand) {
                     newParticles.add(p.getSample());
                     break;
                 }
                 //cnt++;
             }
             //System.out.printf("Chose particle %d\n", cnt);
         }
         particles = newParticles;
 
         drawStuff();
     }
 
     // Draw our own updates to screen
     void drawStuff()
     {
         // Pick the particle to render
         Particle best = null;
         double chi2 = Double.MAX_VALUE;
         for (Particle p: particles) {
             double temp = p.getChi2();
             if (chi2 > temp) {
                 chi2 = temp;
                 best = p;
             }
         }
 
         // Render the (best) robot
         {
             VisWorld.Buffer vb = vw.getBuffer("robot-local");
             vb.setDrawOrder(-100);
             VisWorld.Buffer vb2 = vw.getBuffer("robot-best");
             vb2.setDrawOrder(100);
             double[] xyt = best.getPose();
             double[] xyzrpy = new double[]{xyt[0], xyt[1], 0,
                                            0, 0, xyt[2]};
             vb2.addBack(new VisChain(LinAlg.xyzrpyToMatrix(xyzrpy),
                                     new VisRobot(Color.yellow)));
 
 
             VisRobot robot = new VisRobot(new Color(160, 30, 30));
             ArrayList<double[]> points = new ArrayList<double[]>();
             for (Particle p: particles) {
                 xyt = p.getPose();
                 xyzrpy = new double[]{xyt[0], xyt[1], 0,
                                                0, 0, xyt[2]};
                 //vb.addBack(new VisChain(LinAlg.xyzrpyToMatrix(xyzrpy), robot));
                 points.add(LinAlg.resize(xyt,2));
             }
             vb.addBack(new VisPoints(new VisVertexData(points),
                                      new VisConstantColor(new Color(160, 30, 30)),
                                      1));
             vb.swap();
             vb2.swap();
         }
 
         // Render (all) trajectories
         {
             VisWorld.Buffer vb = vw.getBuffer("particle-trajectories");
             vb.setDrawOrder(-100);
             VisWorld.Buffer vb2 = vw.getBuffer("best-trajectory");
             vb2.setDrawOrder(100);
             VisVertexData vvd = new VisVertexData(best.getTrajectory());
             VisConstantColor vccYellow = new VisConstantColor(Color.yellow);
             vb2.addBack(new VisLines(vvd, vccYellow, 1.5, VisLines.TYPE.LINE_STRIP));
 
             /*
             for (Particle p: particles) {
                 vvd = new VisVertexData(p.getTrajectory());
                 VisConstantColor vccRed = new VisConstantColor(new Color(160, 30, 30));
                 vb.addBack(new VisLines(vvd, vccRed, 0.5, VisLines.TYPE.LINE_STRIP));
             }
             vb.swap();*/
             vb2.swap();
         }
 
         // Render landmark estimates for best robot along with lines
         // connecting them to the landmark that spawned them
         ArrayList<double[]> lmarks = best.getLandmarks();
         ArrayList<Integer> ids = best.getIDs();
         {
             VisWorld.Buffer vb = vw.getBuffer("landmark-estimates");
             VisVertexData vvd = new VisVertexData(lmarks);
             VisConstantColor vccRed = new VisConstantColor(Color.red);
             vb.addBack(new VisPoints(vvd, vccRed, 5));
 
             vb.swap();
         }
 
         {
             VisWorld.Buffer vb = vw.getBuffer("landmark-groundTruth");
             ArrayList<double[]> lines = new ArrayList<double[]>();
             VisConstantColor vccCyan = new VisConstantColor(Color.cyan);
             for (int i = 0; i < lmarks.size(); i++) {
                 if (ids.get(i) < 0)
                     continue;
                 lines.add(lmarks.get(i));
                 lines.add(lmarkGroundTruth.get(ids.get(i)));
             }
             VisVertexData vvd = new VisVertexData(lines);
 
             vb.addBack(new VisLines(vvd, vccCyan, 1, VisLines.TYPE.LINES));
 
             vb.swap();
         }
 
     }
 }
