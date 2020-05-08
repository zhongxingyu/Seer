 package pleocmd.itfc.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import pleocmd.Log;
 import pleocmd.exc.PipeException;
 import pleocmd.itfc.gui.icons.IconLoader;
 import pleocmd.pipe.Pipe;
 
 public final class MainPipePanel extends JPanel {
 
 	private static final long serialVersionUID = 5361715509143723415L;
 
 	private final Pipe pipe;
 
 	private final JLabel pipeLabel;
 
 	public MainPipePanel(final Pipe pipe) {
 		this.pipe = pipe;
 
 		setLayout(new GridBagLayout());
 		final GridBagConstraints gbc = ConfigFrame.initGBC();
 		gbc.weightx = 0.0;
 		gbc.gridy = 0;
 		gbc.gridx = 0;
 		pipeLabel = new JLabel();
 		updatePipeLabel();
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(pipeLabel, gbc);
 		gbc.gridwidth = 1;
 
 		++gbc.gridy;
 
 		gbc.gridx = 0;
 		final JButton btnCfgChange = new JButton("Change", IconLoader
 				.getIcon("document-edit.png"));
 		btnCfgChange.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				changeConfig();
 			}
 		});
 		add(btnCfgChange, gbc);
 
 		++gbc.gridx;
 		gbc.weightx = 1.0;
 		add(new JLabel(), gbc);
 		gbc.weightx = 0.0;
 
 		++gbc.gridx;
 		final JButton btnCfgSave = new JButton("Save To ...", IconLoader
 				.getIcon("document-save-as.png"));
 		btnCfgSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				writeConfigToFile();
 			}
 		});
 		add(btnCfgSave, gbc);
 
 		++gbc.gridx;
 		final JButton btnCfgLoad = new JButton("Load From ...", IconLoader
 				.getIcon("document-open.png"));
 		btnCfgLoad.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				readConfigFromFile();
 			}
 		});
 		add(btnCfgLoad, gbc);
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
 
 	public boolean changeConfig() {
 		Log.detail("GUI-Frame starts configuration");
 		final boolean ok = new ConfigFrame(pipe).isOkPressed();
 		Log.detail("GUI-Frame is done with configuration: " + ok);
 		updatePipeLabel();
 		return ok;
 	}
 
 	public void writeConfigToFile() {
 		final JFileChooser fc = new JFileChooser();
 		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
 			writeConfigToFile(fc.getSelectedFile());
 	}
 
 	public void writeConfigToFile(final File file) {
 		try {
 			pipe.writeToFile(file);
 		} catch (final IOException exc) {
 			Log.error(exc);
 		}
 	}
 
 	public void readConfigFromFile() {
 		final JFileChooser fc = new JFileChooser();
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 			readConfigFromFile(fc.getSelectedFile());
 	}
 
 	public void readConfigFromFile(final File file) {
 		try {
 			pipe.readFromFile(file);
			updatePipeLabel();
 		} catch (final IOException exc) {
 			Log.error(exc);
 		} catch (final PipeException exc) {
 			Log.error(exc);
 		}
 	}
 
 }
