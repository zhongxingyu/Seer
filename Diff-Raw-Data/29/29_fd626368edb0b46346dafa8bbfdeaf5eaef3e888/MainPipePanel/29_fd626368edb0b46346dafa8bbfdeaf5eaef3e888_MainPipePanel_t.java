 package pleocmd.itfc.gui;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import pleocmd.Log;
 import pleocmd.exc.PipeException;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.pipe.Pipe;
 
 public final class MainPipePanel extends JPanel {
 
 	private static final long serialVersionUID = 5361715509143723415L;
 
 	private final Pipe pipe;
 
 	private final JLabel pipeLabel;
 
 	private final JButton btnModify;
 
 	private final JButton btnSave;
 
 	private final JButton btnLoad;
 
 	public MainPipePanel(final Pipe pipe) {
 		this.pipe = pipe;
 
 		final Layouter lay = new Layouter(this);
 		pipeLabel = new JLabel();
 		updatePipeLabel();
 		lay.addWholeLine(pipeLabel, false);
 
 		btnModify = lay.addButton(Button.Modify,
 				"Modify the currently active pipe", new Runnable() {
 					@Override
 					public void run() {
 						changeConfig();
 					}
 				});
 		lay.addSpacer();
 		btnSave = lay.addButton(Button.SaveTo,
 				"Save the current pipe to a file", new Runnable() {
 					@Override
 					public void run() {
 						writeConfigToFile();
 					}
 				});
 		btnLoad = lay.addButton(Button.LoadFrom,
 				"Load a previously saved pipe from a file", new Runnable() {
 					@Override
 					public void run() {
 						readConfigFromFile();
 					}
 				});
 	}
 
 	private void updatePipeLabel() {
 		pipeLabel.setText(String.format(
 				"Pipe has %d input%s, %d converter and %d output%s", pipe
 						.getInputList().size(),
 				pipe.getInputList().size() == 1 ? "" : "s", pipe
 						.getConverterList().size(),
 				pipe.getOutputList().size(),
 				pipe.getOutputList().size() == 1 ? "" : "s"));
 	}
 
 	public void changeConfig() {
 		Log.detail("GUI-Frame starts configuration");
 		new PipePartConfigFrame(pipe);
 		Log.detail("GUI-Frame is done with configuration");
 		updatePipeLabel();
 		writeConfigToFile(MainFrame.PIPE_CONFIG_FILE);
 	}
 
 	public void writeConfigToFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileNameExtensionFilter(
 				"Pipe-Configuration", "pca"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.getName().contains("."))
				file = new File(file.getPath() + ".pca");
			writeConfigToFile(file);
		}
 	}
 
 	public void writeConfigToFile(final File file) {
 		try {
 			pipe.writeToFile(file);
 		} catch (final IOException e) {
 			Log.error(e);
 		} catch (final PipeException e) {
 			Log.error(e);
 		}
 	}
 
 	public void readConfigFromFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileNameExtensionFilter(
 				"Pipe-Configuration", "pca"));
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 			readConfigFromFile(fc.getSelectedFile());
 	}
 
 	public void readConfigFromFile(final File file) {
 		try {
 			pipe.readFromFile(file);
 		} catch (final IOException e) {
 			Log.error(e);
 		} catch (final PipeException e) {
 			Log.error(e);
 		}
 		updatePipeLabel();
 		writeConfigToFile(MainFrame.PIPE_CONFIG_FILE);
 	}
 
 	public void updateState() {
 		btnModify.setEnabled(!MainFrame.the().isPipeRunning());
 		btnSave.setEnabled(pipe.getInputList().isEmpty()
 				|| !pipe.getConverterList().isEmpty()
 				|| !pipe.getOutputList().isEmpty());
 		btnLoad.setEnabled(!MainFrame.the().isPipeRunning());
 	}
 
 }
