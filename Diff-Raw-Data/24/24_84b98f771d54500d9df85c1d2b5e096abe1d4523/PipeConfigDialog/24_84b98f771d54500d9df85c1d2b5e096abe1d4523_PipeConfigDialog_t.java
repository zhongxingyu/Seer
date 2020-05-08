 package pleocmd.itfc.gui;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.CharArrayReader;
 import java.io.CharArrayWriter;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 
 import pleocmd.Log;
 import pleocmd.cfg.ConfigBounds;
 import pleocmd.cfg.Configuration;
 import pleocmd.cfg.ConfigurationInterface;
 import pleocmd.cfg.Group;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.InternalException;
 import pleocmd.exc.PipeException;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 public final class PipeConfigDialog extends JDialog implements
 		ConfigurationInterface {
 
 	private static final long serialVersionUID = 145574241927303337L;
 
 	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");
 
 	private final JButton btnOk;
 
 	private final JButton btnApply;
 
 	private final PipeConfigBoard board;
 
 	private char[] originalPipe;
 
 	public PipeConfigDialog() {
 		Log.detail("Creating Config-Frame");
 		setTitle("Configure Pipe");
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(final WindowEvent e) {
 				close(true);
 			}
 		});
 
 		// Save current pipe's configuration
 		saveCurrentPipe();
 
 		// Add components
 		final Layouter lay = new Layouter(this);
 		board = new PipeConfigBoard();
 		lay.addWholeLine(board, true);
 
 		lay.addButton(Button.Help, Layouter.help(this, getClass()
 				.getSimpleName()));
 		lay.addSpacer();
 		btnOk = lay.addButton(Button.Ok, new Runnable() {
 			@Override
 			public void run() {
 				applyChanges();
 				close(false);
 			}
 		});
 		getRootPane().setDefaultButton(btnOk);
 		btnApply = lay.addButton(Button.Apply, new Runnable() {
 			@Override
 			public void run() {
 				applyChanges();
 			}
 		});
 		lay.addButton(Button.Cancel, new Runnable() {
 			@Override
 			public void run() {
 				Log.detail("Canceled Config-Frame");
 				close(true);
 			}
 		});
 
 		pack();
 		setLocationRelativeTo(null);
 		try {
 			Configuration.the().registerConfigurableObject(this,
 					getClass().getSimpleName());
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 
 		Log.detail("Config-Frame created");
 		// setModal(true);
 		HelpDialog.closeHelpIfOpen();
 		setVisible(true);
 	}
 
 	private void saveCurrentPipe() {
 		final CharArrayWriter out = new CharArrayWriter();
 		try {
 			Configuration.the().writeToWriter(out, Pipe.the());
 		} catch (final IOException e) {
 			throw new InternalException(e);
 		}
 		originalPipe = out.toCharArray();
 	}
 
 	protected void close(final boolean resetChanges) {
 		if (resetChanges) {
 			final CharArrayReader in = new CharArrayReader(originalPipe);
 			try {
 				Configuration.the().readFromReader(new BufferedReader(in),
 						Pipe.the());
 			} catch (final ConfigurationException e) {
 				Log.error(e, "Cannot restore old pipe");
 			} catch (final IOException e) {
 				Log.error(e, "Cannot restore old pipe");
 			}
 			in.close();
 		}
 		try {
 			Configuration.the().unregisterConfigurableObject(this);
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 		dispose();
		MainFrame.the().getMainPipePanel().configDialogDisposed();
 		HelpDialog.closeHelpIfOpen();
 	}
 
 	public void applyChanges() {
 		try {
 			Pipe.the().reset();
 			for (final Input pp : board.getSortedParts(Input.class))
 				Pipe.the().addInput(pp);
 			for (final Converter pp : board.getSortedParts(Converter.class))
 				Pipe.the().addConverter(pp);
 			for (final Output pp : board.getSortedParts(Output.class))
 				Pipe.the().addOutput(pp);
 			saveCurrentPipe();
 			Configuration.the().writeToDefaultFile();
 			MainFrame.the().getMainPipePanel().updateState();
 			MainFrame.the().getMainPipePanel().updatePipeLabel();
 			Log.detail("Applied Config-Frame");
 		} catch (final PipeException e) {
 			Log.error(e);
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 	}
 
 	@Override
 	public Group getSkeleton(final String groupName) {
 		return new Group(groupName).add(cfgBounds);
 	}
 
 	@Override
 	public void configurationAboutToBeChanged() {
 		// nothing to do
 	}
 
 	@Override
 	public void configurationChanged(final Group group) {
 		setBounds(cfgBounds.getContent());
 	}
 
 	@Override
 	public List<Group> configurationWriteback() {
 		cfgBounds.setContent(getBounds());
 		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
 	}
 
 	public void updateState() {
 		btnOk.setEnabled(!MainFrame.the().isPipeRunning());
 		btnApply.setEnabled(!MainFrame.the().isPipeRunning());
 	}
 
 }
