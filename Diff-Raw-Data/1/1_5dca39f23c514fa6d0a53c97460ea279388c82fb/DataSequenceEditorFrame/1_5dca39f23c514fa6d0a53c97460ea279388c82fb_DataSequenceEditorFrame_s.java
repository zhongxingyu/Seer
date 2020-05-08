 package pleocmd.itfc.gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.StyledDocument;
 
 import pleocmd.Log;
 import pleocmd.cfg.ConfigBounds;
 import pleocmd.cfg.ConfigDataMap;
 import pleocmd.cfg.Configuration;
 import pleocmd.cfg.ConfigurationException;
 import pleocmd.cfg.ConfigurationInterface;
 import pleocmd.cfg.Group;
 import pleocmd.exc.OutputException;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.pipe.data.Data;
 import pleocmd.pipe.out.ConsoleOutput;
 import pleocmd.pipe.out.Output;
 import pleocmd.pipe.out.PleoRXTXOutput;
 
 // CS_IGNORE_NEXT The classes this one relies on are mainly GUI components
 public final class DataSequenceEditorFrame extends JDialog implements
 		ConfigurationInterface {
 
 	private static final long serialVersionUID = -5729115559356740425L;
 
 	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");
 
 	private final ConfigDataMap map;
 
 	private final ConfigDataMap mapOrg;
 
 	private final DefaultComboBoxModel cbModel;
 
 	private final JComboBox cbTrigger;
 
 	private final JTextPane tpDataSequence;
 
 	private final JButton btnCopyInput;
 
 	private final JButton btnAddFile;
 
 	private final JButton btnPlaySel;
 
 	private final JButton btnPlayAll;
 
 	private final JButton btnUndo;
 
 	private final JButton btnRedo;
 
 	private List<Output> playOutputList;
 
 	// CS_IGNORE_NEXT Contains only GUI component creation
 	public DataSequenceEditorFrame(final ConfigDataMap cfgMap) {
 		mapOrg = cfgMap;
 		map = new ConfigDataMap(mapOrg.getLabel());
 		map.assignFrom(mapOrg);
 
 		Log.detail("Creating DataSequenceEditorFrame");
 		setTitle("Edit Data Sequence");
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 		// Add components
 		final Layouter lay = new Layouter(this);
 		cbModel = new DefaultComboBoxModel(new Vector<String>());
 		cbTrigger = new JComboBox(cbModel);
 		cbTrigger.setEditable(true);
 		cbTrigger.setMaximumRowCount(2);
 		cbTrigger.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				if ("comboBoxEdited".equals(e.getActionCommand())) {
 					getMap().createContent(
 							getCBTrigger().getSelectedItem().toString());
 					updateComboBoxModel();
 				}
 			}
 
 		});
 		cbTrigger.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(final ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED)
 					updateTextPaneFromMap(e.getItem());
 				else
 					writeTextPaneToMap(e.getItem());
 			}
 		});
 		lay.addWholeLine(cbTrigger, false);
 
 		tpDataSequence = new JTextPane(); // TODO add syntax highlighting
 		tpDataSequence.setPreferredSize(new Dimension(0, 10 * tpDataSequence
 				.getFontMetrics(tpDataSequence.getFont()).getHeight()));
 		lay.addWholeLine(new JScrollPane(tpDataSequence), true);
 
 		lay.addSpacer();
 
 		lay.setSpan(2);
 		btnCopyInput = lay.addButton("Copy From Input History",
 				"bookmark-new.png", "", new Runnable() {
 					@Override
 					public void run() {
 						addFromInputHistory();
 					}
 				});
 		btnAddFile = lay.addButton("Add From File ...",
 				"edit-text-frame-update.png", "", new Runnable() {
 					@Override
 					public void run() {
 						addFromFile();
 					}
 				});
 		lay.setSpan(3);
 		lay.addSpacer();
 		lay.setSpan(1);
 
 		lay.newLine();
 
 		lay.addSpacer();
 
 		btnPlaySel = lay.addButton("Play Selected", "unknownapp",
 				"Sends all currently selected data to "
 						+ "ConsoleOutput and PleoRXTXOutput", new Runnable() {
 					@Override
 					public void run() {
 						playSelected();
 					}
 				});
 		btnPlayAll = lay.addButton("Play All", "unknownapp",
 				"Sends all data in the list to "
 						+ "ConsoleOutput and PleoRXTXOutput", new Runnable() {
 					@Override
 					public void run() {
 						playAll();
 					}
 				});
 		btnUndo = lay.addButton(Button.Undo, new Runnable() {
 			@Override
 			public void run() {
 				undo();
 			}
 		});
 		btnRedo = lay.addButton(Button.Redo, new Runnable() {
 			@Override
 			public void run() {
 				redo();
 			}
 		});
 		lay.addButton(Button.Ok, new Runnable() {
 			@Override
 			public void run() {
 				saveChanges();
 				dispose();
 			}
 		});
 		lay.addButton(Button.Apply, new Runnable() {
 			@Override
 			public void run() {
 				saveChanges();
 			}
 		});
 		lay.addButton(Button.Cancel, new Runnable() {
 			@Override
 			public void run() {
 				dispose();
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
 
 		updateState();
 
 		Log.detail("DataSequenceEditorFrame created");
 		setModal(true);
 		setVisible(true);
 	}
 
 	protected ConfigDataMap getMap() {
 		return map;
 	}
 
 	protected JComboBox getCBTrigger() {
 		return cbTrigger;
 	}
 
 	public void addFromInputHistory() {
 		try {
 			final StyledDocument doc = tpDataSequence.getStyledDocument();
 			final int offset = doc.getParagraphElement(
 					tpDataSequence.getCaretPosition()).getEndOffset();
 			for (final String data : MainFrame.the().getHistory())
 				doc.insertString(offset, data + "\n", null);
 		} catch (final BadLocationException e) {
 			Log.error(e);
 		}
 	}
 
 	public void addFromFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(final File f) {
 				return !f.getName().endsWith(".cfg"); // TODO extension?
 			}
 
 			@Override
 			public String getDescription() {
 				return "Ascii-Textfile containing Data-List";
 			}
 		});
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 			addSequenceFromFile(fc.getSelectedFile());
 	}
 
 	public void addSequenceFromFile(final File fileToAdd) {
 		try {
 			final StyledDocument doc = tpDataSequence.getStyledDocument();
 			final int offset = doc.getParagraphElement(
 					tpDataSequence.getCaretPosition()).getEndOffset();
 			final BufferedReader in = new BufferedReader(new FileReader(
 					fileToAdd));
 			String line;
 			while ((line = in.readLine()) != null)
 				doc.insertString(offset, line.trim() + "\n", null);
 			in.close();
 		} catch (final IOException e) {
 			Log.error(e);
 		} catch (final BadLocationException e) {
 			Log.error(e);
 		}
 	}
 
 	public void playSelected() {
 		/*
 		 * TODO implement play final Object triggerName =
 		 * cbTrigger.getSelectedItem(); writeTextPaneToMap(triggerName); if
 		 * (triggerName != null) { final List<String> trigger =
 		 * map.get(triggerName); if (trigger != null) for (final String command
 		 * : trigger) play(command); }
 		 */
 	}
 
 	public void playAll() {
 		/*
 		 * TODO implement play final Object triggerName =
 		 * cbTrigger.getSelectedItem(); writeTextPaneToMap(triggerName); if
 		 * (triggerName != null) { final List<String> trigger =
 		 * map.get(triggerName); if (trigger != null) for (final String command
 		 * : trigger) play(command); }
 		 */
 	}
 
 	public void play(final Data data) {
 		if (playOutputList == null) {
 			playOutputList = new ArrayList<Output>(2);
 			playOutputList.add(new ConsoleOutput());
 			playOutputList.add(new PleoRXTXOutput());
 		}
 		try {
 			for (final Output out : playOutputList)
 				out.write(data);
 		} catch (final OutputException e) {
 			Log.error(e);
 		}
 	}
 
 	public void undo() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void redo() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void saveChanges() {
 		writeTextPaneToMap(cbTrigger.getSelectedItem());
 		mapOrg.assignFrom(map);
 		try {
 			Configuration.the().writeToDefaultFile();
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void updateComboBoxModel() {
 		final Object lastSelected = cbTrigger.getSelectedItem();
 		cbModel.removeAllElements();
 		for (final String trigger : map.getAllKeys())
 			cbModel.addElement(trigger);
 		if (lastSelected == null || cbModel.getIndexOf(lastSelected) >= 0)
 			getCBTrigger().setSelectedItem(lastSelected);
 		updateTextPaneFromMap(getCBTrigger().getSelectedItem());
 	}
 
 	protected void writeTextPaneToMap(final Object triggerName) {
 		try {
 			if (triggerName == null) {
 				if (tpDataSequence.getDocument().getLength() == 0) return;
 				throw new IOException("No name selected in ComboBox");
 			}
 			map.clearContent(triggerName.toString());
 			final BufferedReader in = new BufferedReader(new StringReader(
 					tpDataSequence.getText()));
 			String line;
 			while ((line = in.readLine()) != null) {
 				line = line.trim();
 				if (!line.isEmpty() && line.charAt(0) != '#')
 					map.addContent(triggerName.toString(), Data
 							.createFromAscii(line));
 			}
 			in.close();
 		} catch (final IOException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void updateTextPaneFromMap(final Object triggerName) {
 		try {
 			final StyledDocument doc = tpDataSequence.getStyledDocument();
 			doc.remove(0, doc.getLength());
 			if (triggerName != null) {
 				final List<Data> dataList = map.getContent(triggerName
 						.toString());
 				if (dataList != null)
 					for (final Data data : dataList)
 						doc.insertString(doc.getLength(), data.toString()
 								+ "\n", null);
 			}
 		} catch (final BadLocationException e) {
 			Log.error(e);
 		}
 	}
 
 	public void updateState() {
 		btnCopyInput.setEnabled(!MainFrame.the().getMainInputPanel()
 				.getHistory().isEmpty());
 		btnAddFile.setEnabled(true);
 		btnPlaySel.setEnabled(false);// TODO
 		btnPlayAll.setEnabled(false);// TODO
 		btnUndo.setEnabled(false);// TODO
 		btnRedo.setEnabled(false); // TODO
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
 
 }
