 package br.edu.cultural.plot;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.swing.AbstractAction;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 
 import net.miginfocom.swing.MigLayout;
 import br.edu.cultural.simulation.CultureDisseminationSimulation;
 import br.edu.cultural.ui.ClassNameComboBoxRenderer;
 
 public abstract class StandAlonePlot implements Runnable {
 	
 	protected static final int NW_REFRESH_ADJUST = 100;
 
 	@SuppressWarnings("serial")
 	public static void start_from_dialog(JFrame parent, final Class<? extends StandAlonePlot> plot_type) {
 		final JPanel setup = new JPanel(new MigLayout("fillx"));
 		
 		final JComboBox simulation_type_in = new JComboBox();
 		simulation_type_in.setRenderer(new ClassNameComboBoxRenderer());
 		for (Class<?> cl : CultureDisseminationSimulation.simulationClasses) {
 			simulation_type_in.addItem(cl);
 		}
 		
 		final JCheckBox periodic_boundary_in = new JCheckBox("Periodic network boundary");
 		periodic_boundary_in.setSelected(true);
 		
 		SpinnerNumberModel size_spinner_model = new SpinnerNumberModel(240, 2, 9999, 1);
 		final JSpinner size_in = new JSpinner(size_spinner_model);
 		
 		final JLabel invar_in_lbl = new JLabel("Features");
 		final JLabel var_in_lbl = new JLabel("Traits");
 		
 		ButtonGroup variable_parameter_in = new ButtonGroup();
 
 		final JRadioButton traits_is_variable = new JRadioButton();
 		traits_is_variable.setAction(new AbstractAction("traits") {
 			public void actionPerformed(ActionEvent e) {
 				updateLabels();
 			}
 
 			private void updateLabels() {
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						invar_in_lbl.setText(traits_is_variable.isSelected()? "Features: " : "Traits: ");
 						var_in_lbl.setText((traits_is_variable.isSelected()? "Traits: " : "Features: "));
 					}
 				});
 			}
 		});
 		
 		final JRadioButton features_is_variable = new JRadioButton(new AbstractAction("features") {
 			public void actionPerformed(ActionEvent e) {
 				updateLabels();
 			}
 			
 			private void updateLabels() {
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						invar_in_lbl.setText(traits_is_variable.isSelected()? "Features: " : "Traits: ");
 						var_in_lbl.setText((traits_is_variable.isSelected()? "Traits: " : "Features: "));
 					}
 				});
 			}
 		});
 		
 		variable_parameter_in.add(features_is_variable);
 		variable_parameter_in.add(traits_is_variable);
 		traits_is_variable.doClick();
 		
 		SpinnerNumberModel invar_param_in_model = new SpinnerNumberModel(2, 2, 9999, 1);
 		final JSpinner invar_in = new JSpinner(invar_param_in_model);
 		
 		SpinnerNumberModel var_param_lower_in_model = new SpinnerNumberModel(2, 2, 9999, 1);
 		final JSpinner var_lower_in = new JSpinner(var_param_lower_in_model);
 		
 		SpinnerNumberModel var_param_upper_in_model = new SpinnerNumberModel(60, 2, 9999, 1);
 		final JSpinner var_upper_in = new JSpinner(var_param_upper_in_model);
 		
 		SpinnerNumberModel steps_in_model = new SpinnerNumberModel(1, 1, 9999, 1);
 		final JSpinner steps_in = new JSpinner(steps_in_model);
 		
 		SpinnerNumberModel simulation_count_model = new SpinnerNumberModel(10, 1, 9999, 1);
 		final JSpinner simulation_count_in = new JSpinner(simulation_count_model);
 		
 		final SpinnerNumberModel stop_spinner_model = new SpinnerNumberModel(10, 0, (int)Math.log10(Long.MAX_VALUE), 1);
 		JSpinner stop_after_iterations = new JSpinner(stop_spinner_model);
 	
 		final JDialog plotDialog = new JDialog(parent, "New Plot...");
 		
 		final JButton go = new JButton();
 		go.setText("Start.");
 		go.setAction(new AbstractAction("Start.") {
 			private static final long serialVersionUID = -9175379487336433833L;
 			@SuppressWarnings("unchecked")
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				StandAlonePlot pl = null;
 				try {
 					pl = plot_type.getConstructor(Class.class, Boolean.class, Integer.class, Integer.class,
 							Integer.class, Integer.class,
 							Boolean.class, Integer.class, Integer.class, Long.class)
 							.newInstance((
 								Class<? extends CultureDisseminationSimulation>)simulation_type_in.getSelectedItem(), 
 								periodic_boundary_in.isSelected(),
 								((Number)size_in.getValue()).intValue(), 
 								((Number)invar_in.getValue()).intValue(),
 								((Number)var_lower_in.getValue()).intValue(),
 								((Number)var_upper_in.getValue()).intValue(),
 								(features_is_variable.isSelected()),
 								((Number)simulation_count_in.getValue()).intValue(), 
 								((Number)steps_in.getValue()).intValue(),
 								(long)Math.pow(10, stop_spinner_model.getNumber().longValue()));
 					
 				// this stuff just hurts my eyes, really
 				} catch (IllegalArgumentException e1) {
 					e1.printStackTrace();
 				} catch (SecurityException e1) {
 					e1.printStackTrace();
 				} catch (InstantiationException e1) {
 					e1.printStackTrace();
 				} catch (IllegalAccessException e1) {
 					e1.printStackTrace();
 				} catch (InvocationTargetException e1) {
 					e1.printStackTrace();
 				} catch (NoSuchMethodException e1) {
 					e1.printStackTrace();
 				}
 
 				pl.startPlot();
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						plotDialog.setVisible(false);
 						plotDialog.dispose();
 					}
 				});
 			}
 		});
 		
 		Dimension four_digit_spinner_dim = new Dimension(36,18);
 		size_in.setPreferredSize(four_digit_spinner_dim);
 		invar_in.setPreferredSize(four_digit_spinner_dim);
 		var_lower_in.setPreferredSize(four_digit_spinner_dim);
 		var_upper_in.setPreferredSize(four_digit_spinner_dim);
 		steps_in.setPreferredSize(four_digit_spinner_dim);
 		simulation_count_in.setPreferredSize(four_digit_spinner_dim);
 		
 		setup.add(simulation_type_in, "spanx 3, wrap");
 		setup.add(periodic_boundary_in, "spanx 3, wrap");
 		setup.add(new JLabel("Network size: "), "wrap");
 		setup.add(size_in, "wrap");
 		setup.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
 		setup.add(new JLabel("Vary: "), "split 3, spanx 3");
 		setup.add(features_is_variable, "");
 		setup.add(traits_is_variable, "wrap");
 		setup.add(invar_in_lbl, "wrap");
 		setup.add(invar_in, "wrap");
 		setup.add(var_in_lbl, "spany 2");
 		setup.add(new JLabel("Lower bound: "), "split 2");
 		setup.add(var_lower_in, "wrap");
 		setup.add(new JLabel("Upper bound: "), "split 2");
 		setup.add(var_upper_in, "wrap");
 		setup.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
 		setup.add(new JLabel("Step size: "), "wrap");
 		setup.add(steps_in, "wrap");
 		setup.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
 		setup.add(new JLabel("Average results from: "), "split 3, spanx 3");
 		setup.add(simulation_count_in, "");
 		setup.add(new JLabel(" simulations."), "wrap");
 		setup.add(new JSeparator(SwingConstants.HORIZONTAL), "spanx 3, grow, wrap, gaptop 9, gapbottom 9");
 		setup.add(new JLabel("Stop simulation after 10^"), "split 3, spanx 3");
 		setup.add(stop_after_iterations, "");
 		setup.add(new JLabel("Monte Carlo steps."), "wrap");
 		setup.add(new JSeparator(SwingConstants.HORIZONTAL), "spanx 3, grow, wrap, gaptop 9, gapbottom 9");
 		setup.add(go, "spanx 3, al center, wrap");
 		
 		plotDialog.add(setup);
 		plotDialog.setPreferredSize(new Dimension(420, 560));
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				plotDialog.pack();
 				plotDialog.setVisible(true);
 			}
 		});
 	}
 
 	private Thread worker;
 
 	protected void startPlot() {
 		this.worker = new Thread(this);
 		this.worker.start();
 	}
 	
 	protected boolean plot_aborted = false;
 	protected void stopPlot(){
 		this.plot_aborted  = true;
		System.out.println("plot aborted.");
 	}
 
 	protected Class<? extends CultureDisseminationSimulation> simulation_type;
 	protected boolean periodic_boundary;
 	protected int network_size;
 	protected int invar_param;
 	protected int var_param_lower;
 	protected int var_param_upper;
 	protected int simulation_count;
 	protected boolean is_features_variable;
 	protected int vary_in_steps_of;
 	protected long max_epochs;
 	protected Integer edges = null;
 	protected ScatterPlotter plotter = null;
 
 	@Override
 	public void run() {
 		if(is_features_variable){
 			run_with_variable_features();
 		}else{
 			run_with_variable_traits();
 		}
 	}
 
 	protected abstract void run_with_variable_traits();
 
 	protected abstract void run_with_variable_features();
 	
 	protected void addStopPlotWindowListener() {
 		plotter.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				StandAlonePlot.this.stopPlot();
 			}
 		});
 	}
 	
 	public static void main(String [] args){
 		start_from_dialog(null, CriticalityPlot.class);
 		start_from_dialog(null, TruncatedCriticalityPlot.class);
 	}
 }
