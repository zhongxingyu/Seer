 package br.edu.cultural.plot;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import br.edu.cultural.network.CulturalNetwork;
 import br.edu.cultural.network.Utils;
 import br.edu.cultural.simulation.CultureDisseminationSimulation;
 
 public class CriticalityPlot extends StandAlonePlot {
 	
 	int series_i = 0;
 	double[][] series;
 	
 	public CriticalityPlot(
 			Class<? extends CultureDisseminationSimulation> simulation_type,
 			Boolean periodic_boundary, Integer network_size, Integer invar_param,
 			Integer var_param_lower, Integer var_param_upper,
 			Boolean is_features_variable, Integer simulation_count, Integer vary_in_steps_of, Long max_epochs) {
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
 		this.max_epochs = max_epochs;
 		series = new double[2][(var_param_upper - var_param_lower + 1)];
 	}
 	
 	protected void run_with_variable_features() {
 		int f_low = var_param_lower;
 		int f_hi = var_param_upper;
 		int traits = invar_param;
 		
 		plotter = new ScatterPlotter("Criticality Plot", String
 				.format("L = %d, q = %d, Truncate = 10^%d, %d Ensembles ", network_size, traits, (int)Math.log10(max_epochs), simulation_count), series, "F", "% largest culture");
 		addStopPlotWindowListener();
 		plotter.pack();
 		plotter.setVisible(true);
 		
 		for (int f = f_low; f <= f_hi  && !plot_aborted; f+=vary_in_steps_of) {
 			System.out.println("F: " + f);
 			plot_point_features(f, traits);
 		}
 		
 	}
 	
 	protected void run_with_variable_traits() {
 		int q_low = var_param_lower;
 		int q_hi = var_param_upper;
 		int features = invar_param;
 		
 		plotter = new ScatterPlotter("Criticality Plot", String
 				.format("L = %d, F = %d, Truncate = 10^%d, %d Ensembles", network_size, invar_param, (int)Math.log10(max_epochs), simulation_count), series, "q", "% largest culture");
 		
 		addStopPlotWindowListener();
 		plotter.pack();
 		plotter.setVisible(true);
 		
 		for (int q = q_low; q <= q_hi && !plot_aborted; q+=vary_in_steps_of) {
 			System.out.println("q: " + q);
 			plot_point_traits(features, q);
 		}
 	}
 
 	private void plot_point_features(int features, int traits) {
 		double[][] cSizes = new double[2][simulation_count];
 		for (int i = 0; i < simulation_count && !plot_aborted; i++) {
 			CultureDisseminationSimulation sim =
 				CultureDisseminationSimulation.factory(
 						(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
 						new CulturalNetwork(network_size, features, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
 			sim.stop_after_epochs(max_epochs);
 			sim.run();
 			Integer[] culture_sizes = new ArrayList<Integer>(sim.nw
 					.count_cultures().values()).toArray(new Integer[0]);
 			Arrays.sort(culture_sizes);
 			Integer largest_culture = (Integer) culture_sizes[culture_sizes.length - 1];
 			cSizes[0][i] = features;
 			cSizes[1][i] = ((double) largest_culture) / (sim.nw.size * sim.nw.size);
 		}
 		double average = Utils.array_average(cSizes[1]);
 		series[0][series_i] = cSizes[0][0];
 		series[1][series_i] = average;
 		series_i++;
 		plotter.setSeries(series);
 	}
 	private void plot_point_traits(int features, int traits) {
 		double[][] cSizes = new double[2][simulation_count];
 		for (int i = 0; i < simulation_count && !plot_aborted; i++) {
 			CultureDisseminationSimulation sim =
 				CultureDisseminationSimulation.factory(
 						(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
 						new CulturalNetwork(network_size, features, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
 			sim.stop_after_epochs(max_epochs);
 			sim.run();
 			Integer[] culture_sizes = new ArrayList<Integer>(sim.nw
 					.count_cultures().values()).toArray(new Integer[0]);
 			Arrays.sort(culture_sizes);
 			Integer largest_culture = (Integer) culture_sizes[culture_sizes.length - 1];
 			cSizes[0][i] = traits;
 			cSizes[1][i] = ((double) largest_culture) / (sim.nw.size * sim.nw.size);
 		}
 		double average = Utils.array_average(cSizes[1]);
 		series[0][series_i] = cSizes[0][0];
 		series[1][series_i] = average;
 		series_i++;
 		plotter.setSeries(series);
 	}
 }
