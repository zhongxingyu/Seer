 package pleocmd.itfc.gui;
 
 import java.awt.Dimension;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
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
 
 	private final JList triggerList;
 
 	private final DataSequenceEditorListModel triggerModel;
 
 	private final JButton btnAddTrigger;
 
 	private final JButton btnRenameTrigger;
 
 	private final JButton btnRemoveTrigger;
 
 	private final JTextPane tpDataSequence;
 
 	private final JButton btnCopyInput;
 
 	private final JButton btnAddFile;
 
 	private final JButton btnPlaySel;
 
 	private final JButton btnPlayAll;
 
 	private final JButton btnUndo;
 
 	private final JButton btnRedo;
 
 	private String trigger;
 
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
 		triggerModel = new DataSequenceEditorListModel();
 		triggerList = new JList(triggerModel);
 		triggerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		lay.addWholeLine(new JScrollPane(triggerList), false);
 		triggerList.addListSelectionListener(new ListSelectionListener() {
 
 			@Override
 			public void valueChanged(final ListSelectionEvent e) {
 				triggerIndexChanged();
 			}
 
 		});
 		lay.add(new JLabel("Trigger:"), false);
 		btnAddTrigger = lay.addButton("Add", "list-add", "", new Runnable() {
 			@Override
 			public void run() {
 				addNewTrigger();
 			}
 		});
 		btnRenameTrigger = lay.addButton("Rename", "edit-rename", "",
 				new Runnable() {
 					@Override
 					public void run() {
 						renameSelectedTrigger();
 					}
 				});
 		btnRemoveTrigger = lay.addButton("Remove", "list-remove", "",
 				new Runnable() {
 					@Override
 					public void run() {
 						removeSelectedTrigger();
 					}
 
 				});
 
 		lay.addSpacer();
 		lay.newLine();
 
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
 
 		updateTriggerModel();
 		updateState();
 
 		Log.detail("DataSequenceEditorFrame created");
 		setModal(true);
 		setVisible(true);
 	}
 
 	protected void triggerIndexChanged() {
 		final Object newTrigger = triggerList.getSelectedValue();
 		if (trigger == newTrigger) return;
 		writeTextPaneToMap();
 		trigger = (String) newTrigger;
 		updateState();
 		updateTextPaneFromMap();
 	}
 
 	protected void updateTriggerModel() {
 		final Object lastSelected = trigger;
 		triggerModel.set(map.getAllKeysSorted(new Comparator<String>() {
 			@Override
 			public int compare(final String o1, final String o2) {
 				return o1.compareTo(o2);
 			}
 		}));
 		triggerList.setSelectedValue(lastSelected, true);
 		updateState(); // TODO needed?
 	}
 
 	protected void addNewTrigger() {
 		final String name = JOptionPane.showInputDialog(this,
 				"Name of the new trigger", "Add new trigger",
 				JOptionPane.PLAIN_MESSAGE);
 		if (name != null) {
 			map.createContent(name);
 			updateTriggerModel();
 			triggerList.setSelectedValue(name, true);
 		}
 	}
 
 	protected void renameSelectedTrigger() {
 		if (trigger != null) {
 			final String name = (String) JOptionPane.showInputDialog(this,
 					"New Name of the trigger", "Rename trigger",
 					JOptionPane.PLAIN_MESSAGE, null, null, trigger);
 			if (name != null) {
 				map.renameContent(trigger, name);
 				trigger = name;
 				updateTriggerModel();
 				triggerList.setSelectedValue(name, true);
 			}
 		}
 	}
 
 	protected void removeSelectedTrigger() {
 		if (trigger != null) {
 			map.removeContent(trigger);
 			final StyledDocument doc = tpDataSequence.getStyledDocument();
 			try {
 				doc.remove(0, doc.getLength());
 			} catch (final BadLocationException e) {
 				Log.error(e);
 			}
 			trigger = null;
 			updateTriggerModel();
 		}
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
 		writeTextPaneToMap();
 		mapOrg.assignFrom(map);
 		try {
 			Configuration.the().writeToDefaultFile();
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void writeTextPaneToMap() {
 		Log.detail("Writing TextPane to map with '%s'", trigger);
 		try {
 			if (trigger == null) {
 				if (tpDataSequence.getDocument().getLength() == 0) return;
 				throw new IOException("No name selected in JList");
 			}
 			map.clearContent(trigger);
 			final BufferedReader in = new BufferedReader(new StringReader(
 					tpDataSequence.getText()));
 			String line;
 			while ((line = in.readLine()) != null) {
 				line = line.trim();
 				if (!line.isEmpty() && line.charAt(0) != '#')
 					map.addContent(trigger, Data.createFromAscii(line));
 			}
 			in.close();
 		} catch (final IOException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void updateTextPaneFromMap() {
 		Log.detail("Updating TextPane from map with '%s'", trigger);
 		try {
 			final StyledDocument doc = tpDataSequence.getStyledDocument();
 			doc.remove(0, doc.getLength());
 			if (trigger != null) {
 				final List<Data> dataList = map.getContent(trigger);
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
 		btnAddTrigger.setEnabled(true);
 		btnRenameTrigger.setEnabled(trigger != null);
 		btnRemoveTrigger.setEnabled(trigger != null);
 		tpDataSequence.setEnabled(trigger != null);
 		btnCopyInput.setEnabled(trigger != null
 				&& !MainFrame.the().getMainInputPanel().getHistory().isEmpty());
 		btnAddFile.setEnabled(trigger != null);
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
