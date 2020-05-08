 package br.edu.cultural.plot;
 
 import br.edu.cultural.network.CulturalNetwork;
 import br.edu.cultural.network.Utils;
 import br.edu.cultural.simulation.CultureDisseminationSimulation;
 
 public class TruncatedCriticalityPlot extends StandAlonePlot {
 	
 	private static final String[] series_keys = {"0: overlap = 0", "1: 0 < overlap < f", "2: overlap = f"};
 
 	int series_i = 0;
 	// 3 series of 2 axes
 	double[][][] series;
 	
 	public TruncatedCriticalityPlot(
 			Class<? extends CultureDisseminationSimulation> simulation_type,
 			Boolean periodic_boundary, Integer network_size, Integer invar_param,
 			Integer var_param_lower, Integer var_param_upper,
 			Boolean is_features_variable, Integer simulation_count, Integer vary_in_steps_of, Long max_iterations) {
 		super();
 		this.simulation_type = simulation_type;
 		this.periodic_boundary = periodic_boundary;
 		this.network_size = network_size;
 		this.invar_param = invar_param;
 		this.var_param_lower = var_param_lower;
 		this.var_param_upper = var_param_upper;
 		this.is_features_variable = is_features_variable;
 		this.simulation_count = simulation_count;
 		this.vary_in_steps_of = vary_in_steps_of;
 		this.max_epochs = max_iterations;
 		series = new double[3][2][(var_param_upper - var_param_lower + 1)];
 	}
 	
 	protected void run_with_variable_features() {
 		int f_low = var_param_lower;
 		int f_hi = var_param_upper;
 		int traits = invar_param;
 		
 		plotter = new ScatterPlotter("Truncated Criticality Plot", 
 													String.format("L = %d, q = %d, Truncate = 10^%d, %d Ensembles", network_size, traits, (int)Math.log10(max_epochs), simulation_count),
 													new double[2][2], 
 													"F", 
 													"% edges");
 		addStopPlotWindowListener();
 		plotter.pack();
 		plotter.setVisible(true);
 
 		for (int cur_f = f_low; cur_f <= f_hi  && !plot_aborted; cur_f+=vary_in_steps_of) {
 			System.out.println("F: " + cur_f);
 			plot_point_features(cur_f, traits);
 		}
 	}
 
 	protected void run_with_variable_traits() {
 		int q_low = var_param_lower;
 		int q_hi = var_param_upper;
 		int features = invar_param;
 		
 		plotter = new ScatterPlotter("Truncated Criticality Plot", 
 									 String.format("L = %d, F = %d, Truncate = 10^%d, %d Ensembles", network_size, features, (int)Math.log10(max_epochs), simulation_count),
 									 new double[2][2], 
 									 "q", 
 									 "% edges");
 		addStopPlotWindowListener();
 		plotter.pack();
 		plotter.setVisible(true);
 		
 		for (int cur_q = q_low; cur_q <= q_hi && !plot_aborted; cur_q+=vary_in_steps_of) {
 			System.out.println("q: " + cur_q);
 			plot_point_traits(features, cur_q);
 		}
 	}
 	
 	private void plot_point_traits(int features, int traits) {
 		double[][] overlaps = new double[3][simulation_count];
 		for (int i = 0; i < simulation_count  && !plot_aborted; i++) {
 			CultureDisseminationSimulation sim =
 				CultureDisseminationSimulation.factory(
 						(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
 						new CulturalNetwork(network_size, features, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
 			sim.stop_after_epochs(max_epochs);
 			if(this.edges == null) this.edges = sim.nw.n_edges();
 			sim.run();
 			for(int nd = 0; nd < sim.nw.n_nodes; nd++){
 				for(int nbr_idx = 0; nbr_idx < sim.nw.degree(nd); nbr_idx++){
 					int nbr = sim.nw.node_neighbor(nd, nbr_idx);
 					int overlap = 0;
 					for (int f = 0; f < sim.nw.features; f++){
 						if (sim.nw.states[nd][f] == sim.nw.states[nbr][f]){
 							overlap++;
 						}
 					}
 					if(overlap == 0){
 						overlaps[0][i]++;
 					}else if(overlap == sim.nw.features){
 						overlaps[2][i]++;
 					}else{
 						overlaps[1][i]++;
 					}
 				}
 			}
 		}
 		double overlap_none_average = Utils.array_average(overlaps[0]);
 		double overlap_some_average = Utils.array_average(overlaps[1]);
 		double overlap_all_average = Utils.array_average(overlaps[2]);
 		series[0][0][series_i] = traits;
 		series[0][1][series_i] = overlap_none_average / (2*this.edges);
 		series[1][0][series_i] = traits;
 		series[1][1][series_i] = overlap_some_average / (2*this.edges);
 		series[2][0][series_i] = traits;
 		series[2][1][series_i] = overlap_all_average / (2*this.edges);
 		for(int i = 0; i < 3; i++){
 			plotter.addSeries(series[i], series_keys[i]);
 		}
 		series_i++;
 	}
 
 
 
 private void plot_point_features(int features, int traits) {
 	double[][] overlaps = new double[3][simulation_count];
 	for (int i = 0; i < simulation_count  && !plot_aborted; i++) {
 		CultureDisseminationSimulation sim =
 			CultureDisseminationSimulation.factory(
 					(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
 					new CulturalNetwork(network_size, features, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
 		sim.stop_after_epochs(max_epochs);
 		if(this.edges == null) this.edges = sim.nw.n_edges();
 		sim.run();
 		for(int nd = 0; nd < sim.nw.n_nodes; nd++){
 			for(int nbr_idx = 0; nbr_idx < sim.nw.degree(nd); nbr_idx++){
 				int nbr = sim.nw.node_neighbor(nd, nbr_idx);
 				int overlap = 0;
 				for (int f = 0; f < sim.nw.features; f++){
 					if (sim.nw.states[nd][f] == sim.nw.states[nbr][f]){
 						overlap++;
 					}
 				}
 				if(overlap == 0){
 					overlaps[0][i]++;
 				}else if(overlap == sim.nw.features){
 					overlaps[2][i]++;
 				}else{
 					overlaps[1][i]++;
 				}
 			}
 		}
 	}
 	double overlap_none_average = Utils.array_average(overlaps[0]);
 	double overlap_some_average = Utils.array_average(overlaps[1]);
 	double overlap_all_average = Utils.array_average(overlaps[2]);
 	series[0][0][series_i] = features;
 	series[0][1][series_i] = overlap_none_average / (2*this.edges);
 	series[1][0][series_i] = features;
 	series[1][1][series_i] = overlap_some_average / (2*this.edges);
 	series[2][0][series_i] = features;
 	series[2][1][series_i] = overlap_all_average / (2*this.edges);
 	for(int i = 0; i < 3; i++){
 		plotter.addSeries(series[i], i);
 	}
 	series_i++;
 }
 }
