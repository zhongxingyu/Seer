 package pleocmd.itfc.gui;
 
 import javax.swing.JDialog;
 import javax.swing.JTabbedPane;
 
 import pleocmd.Log;
 import pleocmd.exc.PipeException;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 public final class PipePartConfigFrame extends JDialog {
 
 	private static final long serialVersionUID = -1967218353263515865L;
 
 	private final PipePartPanel<Input> pppInput;
 
 	private final PipePartPanel<Converter> pppConverter;
 
 	private final PipePartPanel<Output> pppOutput;
 
 	private final Pipe pipe;
 
 	public PipePartConfigFrame(final Pipe pipe) {
 		this.pipe = pipe;
 		pppInput = new PipePartPanel<Input>("in");
 		pppConverter = new PipePartPanel<Converter>("cvt");
 		pppOutput = new PipePartPanel<Output>("out");
 
 		Log.detail("Creating Config-Frame");
 		setTitle("Configure Pipe");
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
		pppInput.getTableModel().clear();
		pppInput.getTableModel().addPipeParts(pipe.getInputList());
		pppConverter.getTableModel().clear();
		pppConverter.getTableModel().addPipeParts(pipe.getConverterList());
		pppOutput.getTableModel().clear();
		pppOutput.getTableModel().addPipeParts(pipe.getOutputList());
 
 		// Add components
 		final Layouter lay = new Layouter(this);
 		final JTabbedPane tabs = new JTabbedPane();
 		tabs.addTab("Input", pppInput);
 		tabs.addTab("Converter", pppConverter);
 		tabs.addTab("Output", pppOutput);
 		lay.addWholeLine(tabs, true);
 
 		lay.addSpacer();
 		getRootPane().setDefaultButton(lay.addButton(Button.Ok, new Runnable() {
 			@Override
 			public void run() {
 				applyChanges();
 				dispose();
 			}
 		}));
 		lay.addButton(Button.Apply, new Runnable() {
 			@Override
 			public void run() {
 				applyChanges();
 			}
 		});
 		lay.addButton(Button.Cancel, new Runnable() {
 			@Override
 			public void run() {
 				Log.detail("Canceled Config-Frame");
 				dispose();
 			}
 		});
 
 		// Center window on screen
 		setSize(700, 400);
 		setLocationRelativeTo(null);
 
 		Log.detail("Config-Frame created");
 		setModal(true);
 		setVisible(true);
 	}
 
 	public void applyChanges() {
 		try {
 			pipe.reset();
 			for (int i = 0; i < pppInput.getTableModel().getRowCount(); ++i)
 				pipe.addInput(pppInput.getTableModel().getPipePart(i));
 			for (int i = 0; i < pppConverter.getTableModel().getRowCount(); ++i)
 				pipe.addConverter(pppConverter.getTableModel().getPipePart(i));
 			for (int i = 0; i < pppOutput.getTableModel().getRowCount(); ++i)
 				pipe.addOutput(pppOutput.getTableModel().getPipePart(i));
 			Log.detail("Applied Config-Frame");
 		} catch (final PipeException e) {
 			Log.error(e);
 		}
 	}
 
 }
