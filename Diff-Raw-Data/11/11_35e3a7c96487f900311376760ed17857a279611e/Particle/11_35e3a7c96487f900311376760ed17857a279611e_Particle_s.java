 package team;
 
 import java.util.*;
 
 import april.jmat.*;
 import april.sim.*;
 import april.config.*;
 
 public class Particle
 {
     Config config;
 
     // List of previous points (trajectory) ... last of which is our current pos
     ArrayList<double[]> trajectory = new ArrayList<double[]>();
     double[] xyt = new double[3];
 
     // List of EKFs for landmark observations. Map landmark IDs to EKFs
     HashMap<Integer, LandmarkEKF> observations = new HashMap<Integer, LandmarkEKF>();
 
     double weight = 1.0;  // Particle weight for resampling
 
     public Particle(Config config_)
     {
         this(config_, null);
     }
 
     public Particle(Config config_, double[] start_)
     {
         config = config_;
 
         if (start_ != null) {
             trajectory.add(LinAlg.resize(start_, 2));
             xyt = LinAlg.copy(start_);
         }
     }
 
     public void updateLocation(double[] local_xyt)
     {
         xyt = LinAlg.xytMultiply(xyt, local_xyt);
         trajectory.add(LinAlg.resize(xyt, 2));
     }
 
     public double[] getPose() {
         return LinAlg.copy(xyt);
     }
 
     public ArrayList<double[]> getTrajectory() {
         return trajectory;
     }
 
     public ArrayList<double[]> getLandmarks()
     {
         ArrayList<double[]> landmarks = new ArrayList<double[]>();
         for (LandmarkEKF ekf: observations.values()) {
             landmarks.add(ekf.getPosition());
         }
 
         return landmarks;
     }
 
     public ArrayList<Integer> getIDs()
     {
         ArrayList<Integer> ids = new ArrayList<Integer>();
         for (LandmarkEKF ekf: observations.values()) {
             ids.add(ekf.getRealID());
         }
 
         return ids;
     }
 
     // Return IDs for each of the landmarks in the same order as dets are listed.
     // Pass back new particle weight
     public double associateAndUpdateLandmarks(ArrayList<Simulator.landmark_t> dets)
     {
         // XXX Data association, currently perfect
         ArrayList<Integer> ids = new ArrayList<Integer>();
         for (Simulator.landmark_t det: dets) {
             ids.add(det.id);
         }
 
         // Update the landmark
         for (int i = 0; i < ids.size(); i++) {
             updateLandmark(dets.get(i).obs, ids.get(i), dets.get(i).id);
         }
 
         // Calculate new weight
         for (int i = 0; i < ids.size(); i++) {
             double[] r = observations.get(ids.get(i)).getResidual(dets.get(i).obs[0], dets.get(i).obs[1], xyt);
             Matrix P = observations.get(ids.get(i)).getLinearizedCovariance(dets.get(i).obs[0], dets.get(i).obs[1], xyt);
 
             double w0 = 2*Math.PI*Math.sqrt(P.det());
             double chi2 = LinAlg.dotProduct(P.inverse().transposeTimes(r), r);
             double w1 = Math.exp(-0.5*chi2);
             weight *= w1/w0;
         }
 
         //updateWeight(w);
 
         return weight;
     }
 
     // Given an (r,theta), do data association and add appropriate landmark
     // Return the residual after the update
     public double[] updateLandmark(double[] obs, int id, int realID)
     {
         if(observations.containsKey(id)) {
             observations.get(id).update(obs[0], obs[1], xyt);
         } else {
             observations.put(id, new LandmarkEKF(config, obs[0], obs[1], xyt));
             observations.get(id).setID(realID);
         }
         return observations.get(id).getResidual(obs[0], obs[1], xyt);
     }
 
     public void updateWeight(double factor)
     {
         weight *= factor;
     }
 
     public void normalize(double normalizationFactor)
     {
         weight /= normalizationFactor;
     }
 
     public double getWeight()
     {
         return weight;
     }
 
     public double getChi2()
     {
         // The Chi2 is directly associated with the error for each
         // landmark. Sum those errors.
         double chi2 = 0;
         for (LandmarkEKF ekf: observations.values()) {
             chi2 += ekf.getChi2();
         }
 
         return chi2;
     }
 
     // Return a sample for this particle (deep copy it but reset weight)
     public Particle getSample()
     {
         Particle p = new Particle(config);
         weight = 1.0;
         for (double[] xy: trajectory) {
             p.trajectory.add(LinAlg.copy(xy));
         }
 
         p.xyt = LinAlg.copy(xyt);
 
         for (Integer key: observations.keySet()) {
             p.observations.put(key, observations.get(key).copy());
         }
 
         return p;
     }
 }
