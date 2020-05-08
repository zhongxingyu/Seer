 package com.andreou.galc;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 public class Engine {
 
 	private Set<DatumCont>					objects;
 	private Map<String, DatumCont>	objects_index;
 	private Set<Worker>							workers;
 	private Map<String, Worker>			workers_index;
 	private Set<AssignedLabel>			labels;
 
 	// private Set<Category> current_categories = new HashSet<Category>();
 	// private Set<Worker> current_workers = new HashSet<Worker>();
 
 	// private Ipeirotis ip;
 
 	public Engine() {
 
 		SyntheticData data = new SyntheticData();
 		Double data_mu = 0.0;
 		Double data_sigma = 1.0;
 		data.setDataParameters(data_mu, data_sigma);
 
 		Double worker_mu_down = -10.0;
 		Double worker_mu_up = 10.0;
 		Double worker_sigma_down = 0.0;
 		Double worker_sigma_up = 1.0;
 		Double worker_rho_down = 0.1;
 		Double worker_rho_up = 0.9;
 		data.setWorkerParameters(worker_mu_down, worker_mu_up, worker_sigma_down, worker_sigma_up, worker_rho_down,
 				worker_rho_up);
 
		int data_points = 500;
		int workers = 5;
 		data.build(data_points, workers);
 		System.out.println("Data points: " + data_points);
 		System.out.println("Workers: " + workers);
 		
 		System.out.println("Low rho: " + worker_rho_down);
 		System.out.println("High rho: " + worker_rho_up);
 
 		this.objects = data.getObjects();
 		this.objects_index = new TreeMap<String, DatumCont>();
 		for (DatumCont d : this.objects) {
 			objects_index.put(d.getName(), d);
 		}
 
 		this.workers = data.getWorkers();
 		this.workers_index = new TreeMap<String, Worker>();
 		for (Worker w : this.workers) {
 			workers_index.put(w.getName(), w);
 		}
 
 		this.labels = data.getLabels();
 
 		initWorkers();
 
 		// Run until convergence.
 		int round = 1;
 		double epsilon = 0.0001;
 		System.out.print("----\nRound: ");
 		while (true) {
 			System.out.print(round+"... ");
 			double d1 = estimateObjectZetas();
 			//System.out.println("DiffObjects:" + d1);
 			double d2 = estimateWorkerRho();
 			//System.out.println("DiffWorkers:" + d2);
 			round++;
 			if (d1+d2<epsilon) break;
 			if (Double.isNaN(d1+d2)) System.err.println("ERROR: Check for division by 0");
 		}
 		System.out.println("Done!\n----");
 		
 		// Report about distributional estimates
 		Double est_mu = estimateDistributionMu();
 		Double est_sigma = estimateDistributionSigma();
 		System.out.println("True mu = " + data_mu);
 		System.out.println("Estimated mu = " +est_mu);
 		System.out.println("True sigma = " + data_sigma);
 		System.out.println("Estimated sigma = " +est_sigma);
 		
 		// Give report for objects
 		Double avgAbsError = 0.0;
 		Double avgRelError = 0.0;
 		for (DatumCont d : this.objects) {
 			double estZ = d.getZeta();
 			double realZ = (d.getTrueValue()-data_mu)/data_sigma;
 			double absDiff = Math.abs(realZ-estZ);
 			double relDiff = Math.abs(absDiff/realZ);
 			
 			avgAbsError += absDiff/this.objects.size();
 			avgRelError += relDiff/this.objects.size();
 		}
 		System.out.println("Average absolute estimation error for z-values: "+avgAbsError);
 		System.out.println("Average relative estimation error for z-values: "+avgRelError);
 		
 		
 		// Give report for workers
 	// Give report for objects
 			Double avgRhoError = 0.0;
 			Double relRhoError = 0.0;
 			for (Worker w : this.workers) {
 				double estRho = w.getEst_rho();
 				double realRho = w.getTrueRho();
 				double absDiff = Math.abs(realRho-estRho);
 				double relDiff = Math.abs(absDiff/realRho);
 				
 				avgRhoError += absDiff/this.objects.size();
 				relRhoError += relDiff/this.objects.size();
 			}
 			System.out.println("Average absolute estimation error for correlation values: "+avgRhoError);
 			System.out.println("Average relative estimation error for correlation values: "+relRhoError);
 
 	}
 
 	/**
 	 * @return
 	 */
 	private Double estimateDistributionSigma() {
 
 		Double nominator_sigma = 0.0;
 		Double denominator_sigma = 0.0;
 		for (Worker w : this.workers) {
 			Double b = w.getBeta();
 			Double coef = Math.sqrt(b*b-b);
 			Double s = w.getEst_sigma();
 			nominator_sigma += coef * s;
 			denominator_sigma += b;
 		}
 		Double est_sigma =  nominator_sigma/denominator_sigma;
 		return est_sigma;
 	}
 
 	/**
 	 * @return
 	 */
 	private Double estimateDistributionMu() {
 
 		// Estimate mu and sigma of distribution
 		Double nominator_mu = 0.0;
 		Double denominator_mu = 0.0;
 		for (Worker w : this.workers) {
 			Double b = w.getBeta();
 			Double coef = Math.sqrt(b*b-b);
 			Double m = w.getEst_mu();
 			nominator_mu += coef * m;
 			denominator_mu += b;
 		}
 		Double est_mu =  nominator_mu/denominator_mu;
 		return est_mu;
 	}
 
 	private void initWorkers() {
 
 		Generator rhoGenerator = new Generator(Generator.Distribution.UNIFORM);
 
 		// We do not assign the full range from -1 to 1, because
 		// in that case we will not converge into a reasonable equilibrium
 		rhoGenerator.setUniformParameters(0.0, 0.9);
 
 		for (Worker w : this.workers) {
 			Double rho_init = rhoGenerator.nextData();
 			w.setEst_rho(rho_init);
 
 			w.computeZetaValues();
 		}
 
 	}
 
 	private double estimateObjectZetas() {
 
 		// See equation 9
 
 		double diff = 0.0;
 		for (DatumCont d : this.objects) {
 			Double oldzeta = d.getZeta();
 			
 			Double zeta = 0.0;
 			Double betasum = 0.0;
 			for (AssignedLabel al : d.getAssignedLabels()) {
 				String wid = al.getWorker();
 				Worker w = this.workers_index.get(wid);
				zeta +=  Math.sqrt( Math.pow(w.getBeta(), 2) - w.getBeta()) * w.getZeta(al.getLabel());
 				betasum += w.getBeta();
 			}
 
 			d.setZeta(zeta / betasum);
 			
 			if (oldzeta == null) {
 				diff += 1;
 				continue;
 			}
 			
 			diff += Math.abs(d.getZeta() - oldzeta);
 		}
 		return diff;
 
 	}
 
 	private double estimateWorkerRho() {
 
 		// See equation 10
 
 
 		double diff = 0.0;
 		for (Worker w : this.workers) {
 
 			Double sum_prod = 0.0;
 			Double sum_zi = 0.0;
 			Double sum_zij = 0.0;
 			
 			double oldrho = w.getEst_rho();
 			for (AssignedLabel zl : w.getZetaValues()) {
 				String oid = zl.getDatum();
 				DatumCont d = objects_index.get(oid);
 				double z_i = d.getZeta();
 				double z_ij = zl.getLabel();
 
 				sum_prod += z_i * z_ij;
 				sum_zi += z_i * z_i;
 				sum_zij += z_ij * z_ij;
 			}
 			double rho = sum_prod / Math.sqrt(sum_zi * sum_zij);
 
 			w.setEst_rho(rho);
 	
 			//System.out.println(w.toString());
 
 			diff += Math.abs(w.getEst_rho() - oldrho);
 		}
 		return diff;
 	}
 
 	public Set<DatumCont> getObjects() {
 
 		return objects;
 	}
 
 	public void setObjects(Set<DatumCont> objects) {
 
 		this.objects = objects;
 	}
 
 }
