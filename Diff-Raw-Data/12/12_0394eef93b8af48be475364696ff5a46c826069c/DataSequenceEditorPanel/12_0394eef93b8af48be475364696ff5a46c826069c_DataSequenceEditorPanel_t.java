 package pleocmd.itfc.gui.dse;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.StyledDocument;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 import javax.swing.undo.UndoManager;
 import javax.swing.undo.UndoableEdit;
 
 import pleocmd.Log;
 import pleocmd.api.PleoCommunication;
 import pleocmd.cfg.ConfigItem;
 import pleocmd.exc.FormatException;
 import pleocmd.exc.InternalException;
 import pleocmd.exc.PipeException;
 import pleocmd.itfc.gui.Layouter;
 import pleocmd.itfc.gui.MainFrame;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.pipe.data.Data;
 import pleocmd.pipe.out.ConsoleOutput;
 import pleocmd.pipe.out.Output;
 import pleocmd.pipe.out.PleoRXTXOutput;
 import pleocmd.pipe.out.PrintType;
 
 public abstract class DataSequenceEditorPanel extends JPanel {
 
 	private static final long serialVersionUID = -3019900508373635307L;
 
 	private final JTextPane tpDataSequence;
 
 	private final UndoManager tpUndoManager;
 
 	private final JButton btnCopyInput;
 
 	private final JButton btnAddFile;
 
 	private final JButton btnPlaySel;
 
 	private final JButton btnPlayAll;
 
 	private final JButton btnUndo;
 
 	private final JButton btnRedo;
 
 	private List<Output> playOutputList;
 
 	private final JLabel lblErrorFeedback;
 
 	private final Timer errorLabelTimer = new Timer("ErrorLabelTimer", true);
 
 	private TimerTask errorLabelTimerTask;
 
 	public DataSequenceEditorPanel() {
 		final Layouter lay = new Layouter(this);
 
 		tpUndoManager = new UndoManager();
 		tpDataSequence = new JTextPane();
 		tpDataSequence.setPreferredSize(new Dimension(0, 10 * tpDataSequence
 				.getFontMetrics(tpDataSequence.getFont()).getHeight()));
 		tpDataSequence.addCaretListener(new CaretListener() {
 			@Override
 			public void caretUpdate(final CaretEvent e) {
 				updateState();
 			}
 		});
 		final DataSequenceEditorKit kit = new DataSequenceEditorKit(this);
 		tpDataSequence.setEditorKitForContentType("text/datasequence", kit);
 		tpDataSequence.setContentType("text/datasequence");
 		tpDataSequence.setFont(tpDataSequence.getFont().deriveFont(Font.BOLD));
 		tpDataSequence.getDocument().addUndoableEditListener(
 				new UndoableEditListener() {
 					@Override
 					public void undoableEditHappened(final UndoableEditEvent e) {
 						addUndo(e.getEdit());
 					}
 				});
 
 		lay.addWholeLine(new JScrollPane(tpDataSequence), true);
 
 		lblErrorFeedback = new JLabel("");
 		lblErrorFeedback.setForeground(Color.RED);
 		lay.addWholeLine(lblErrorFeedback, false);
 
 		lay.setSpan(2);
 		btnCopyInput = lay.addButton("Copy From Input History",
 				"bookmark-new.png",
 				"Copy all history from console input into this Data list",
 				new Runnable() {
 					@Override
 					public void run() {
 						addFromInputHistory();
 					}
 				});
 		btnAddFile = lay.addButton("Add From File ...",
 				"edit-text-frame-update.png",
 				"Copy the contents of a file into this Data list",
 				new Runnable() {
 					@Override
 					public void run() {
 						addFromFile();
 					}
 				});
 
 		lay.newLine();
 
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
 		lay.addSpacer();
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
 	}
 
 	protected void addFromInputHistory() {
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
 
 	protected void addFromFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(final File f) {
 				final String name = f.getName();
 				return !name.endsWith(".pbd") && !name.endsWith(".pca");
 			}
 
 			@Override
 			public String getDescription() {
 				return "Ascii-Textfile containing Data-List";
 			}
 		});
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 			addSequenceFromFile(fc.getSelectedFile());
 	}
 
 	protected void addSequenceFromFile(final File fileToAdd) {
 		try {
 			final StyledDocument doc = tpDataSequence.getStyledDocument();
 			int offset = doc.getParagraphElement(
 					tpDataSequence.getCaretPosition()).getEndOffset();
 			final BufferedReader in = new BufferedReader(new FileReader(
 					fileToAdd));
 			try {
 				String line;
 				while ((line = in.readLine()) != null) {
 					line = line.trim() + "\n";
 					doc.insertString(offset, line, null);
 					offset += line.length();
 				}
 			} finally {
 				in.close();
 			}
 		} catch (final IOException e) {
 			Log.error(e);
 		} catch (final BadLocationException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void clear() {
 		final StyledDocument doc = tpDataSequence.getStyledDocument();
 		try {
 			doc.remove(0, doc.getLength());
 		} catch (final BadLocationException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void playSelected() {
 		final String sel = tpDataSequence.getSelectedText();
 		if (sel != null) for (final String line : sel.split("\n"))
 			try {
 				play(Data.createFromAscii(line));
 			} catch (final IOException e) {
 				Log.error(e);
 			} catch (final FormatException e) {
 				Log.error(e);
 			}
 	}
 
 	protected void playAll() {
 		final String text = tpDataSequence.getText();
 		if (text != null) for (final String line : text.split("\n"))
 			try {
 				play(Data.createFromAscii(line));
 			} catch (final IOException e) {
 				Log.error(e);
 			} catch (final FormatException e) {
 				Log.error(e);
 			}
 	}
 
 	protected void play(final Data data) {
 		if (playOutputList == null)
 			try {
 				String device = null;
 				for (final Output out : MainFrame.the().getPipe()
 						.getOutputList())
 					if (out instanceof PleoRXTXOutput)
 						device = ((ConfigItem<?>) out.getGroup().get("Device"))
 								.getContent();
 				if (device == null)
 					device = JOptionPane.showInputDialog(this,
 							"Set Pleo device name for playback:",
 							PleoCommunication.getHighestPort().getName());
 				if (device == null) return;
 				playOutputList = new ArrayList<Output>(2);
 				final Output outC = new ConsoleOutput(PrintType.DataAscii);
 				outC.configure();
 				outC.init();
 				playOutputList.add(outC);
 				final Output outP = new PleoRXTXOutput(device);
 				outP.configure();
 				outP.init();
 				playOutputList.add(outP);
 
 			} catch (final Throwable e) { // CS_IGNORE catch all here
 				Log.error(e);
 			}
 
 		try {
 			for (final Output out : playOutputList)
 				out.write(data);
 		} catch (final Throwable e) { // CS_IGNORE catch all here
 			Log.error(e);
 		}
 	}
 
 	protected void freeResources() {
 		try {
 			if (playOutputList != null) for (final Output out : playOutputList)
 				out.close();
 		} catch (final PipeException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void addUndo(final UndoableEdit edit) {
 		if (edit.isSignificant()) {
 			tpUndoManager.addEdit(edit);
 			updateState();
 		}
 	}
 
 	protected void undo() {
 		try {
 			tpUndoManager.undo();
 		} catch (final CannotUndoException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void redo() {
 		try {
 			tpUndoManager.redo();
 		} catch (final CannotRedoException e) {
 			Log.error(e);
 		}
 	}
 
 	protected List<Data> writeTextPaneToList() throws IOException,
 			FormatException {
 		final List<Data> res = new ArrayList<Data>();
 		final BufferedReader in = new BufferedReader(new StringReader(
 				tpDataSequence.getText()));
 		String line;
 		while ((line = in.readLine()) != null) {
 			line = line.trim();
 			if (!line.isEmpty() && line.charAt(0) != '#')
 				res.add(Data.createFromAscii(line));
 		}
 		in.close();
 		return res;
 	}
 
 	public void writeTextPaneToWriter(final Writer out) throws IOException {
 		final BufferedReader in = new BufferedReader(new StringReader(
 				tpDataSequence.getText()));
 		String line;
 		while ((line = in.readLine()) != null) {
 			out.write(line);
 			out.write('\n');
 		}
 		in.close();
 		out.flush();
 	}
 
 	protected void updateTextPaneFromList(final List<Data> dataList) {
 		clear();
 		final StyledDocument doc = tpDataSequence.getStyledDocument();
 		if (dataList != null)
 			try {
 				for (final Data data : dataList)
 					doc.insertString(doc.getLength(), data.asString() + "\n",
 							null);
 			} catch (final BadLocationException e) {
 				throw new InternalException(e);
 			}
 		tpUndoManager.discardAllEdits();
 		updateState();
 	}
 
 	public void updateTextPaneFromReader(final BufferedReader in)
 			throws IOException {
 		clear();
 		final StyledDocument doc = tpDataSequence.getStyledDocument();
 		if (in != null) try {
 			String line;
 			while ((line = in.readLine()) != null)
 				doc.insertString(doc.getLength(), line + "\n", null);
 		} catch (final BadLocationException e) {
 			throw new InternalException(e);
 		}
 		tpUndoManager.discardAllEdits();
 		updateState();
 	}
 
 	void updateErrorLabel(final String text) {
 		if (text.equals(lblErrorFeedback.getText())) return;
 		lblErrorFeedback.setText(text);
 		Color.RGBtoHSB(255, 0, 0, null);
 		final Color src = Color.RED;
 		final Color trg = lblErrorFeedback.getBackground();
 		lblErrorFeedback.setForeground(src);
 		if (errorLabelTimerTask != null) errorLabelTimerTask.cancel();
 		errorLabelTimerTask = new FadeTimerTask(src.getRed(), src.getGreen(),
 				src.getBlue(), trg.getRed(), trg.getGreen(), trg.getBlue());
 		errorLabelTimer.schedule(errorLabelTimerTask, 1000, 100);
 	}
 
 	protected abstract void stateChanged();
 
 	protected void updateState() {
		tpDataSequence.setEnabled(isEnabled());
 		btnCopyInput.setEnabled(MainFrame.the().getHistory().size() > 0);
 		btnAddFile.setEnabled(true);
 		btnPlaySel.setEnabled(tpDataSequence.getSelectedText() != null);
 		btnPlayAll.setEnabled(tpDataSequence.getDocument().getLength() > 0);
 		btnUndo.setEnabled(tpUndoManager.canUndo());
 		btnRedo.setEnabled(tpUndoManager.canRedo());
 		stateChanged();
 	}
 
 	protected JLabel getLblErrorFeedback() {
 		return lblErrorFeedback;
 	}
 
 	public UndoManager getTpUndoManager() {
 		return tpUndoManager;
 	}
 
 	public JTextPane getTpDataSequence() {
 		return tpDataSequence;
 	}
 
 	private class FadeTimerTask extends TimerTask {
 
 		private double cur0;
 		private double cur1;
 		private double cur2;
 		private final double inc0;
 		private final double inc1;
 		private final double inc2;
 		private int steps;
 
 		FadeTimerTask(final int src0, final int src1, final int src2,
 				final int trg0, final int trg1, final int trg2) {
 			cur0 = src0;
 			cur1 = src1;
 			cur2 = src2;
 			inc0 = (trg0 - src0) / 10.0;
 			inc1 = (trg1 - src1) / 10.0;
 			inc2 = (trg2 - src2) / 10.0;
 		}
 
 		@Override
 		public void run() {
 			cur0 = Math.min(255, Math.max(0, cur0 + inc0));
 			cur1 = Math.min(255, Math.max(0, cur1 + inc1));
 			cur2 = Math.min(255, Math.max(0, cur2 + inc2));
 			getLblErrorFeedback().setForeground(
 					new Color((int) cur0, (int) cur1, (int) cur2));
 			if (++steps == 10) cancel();
 		}
 
 	}
 
 }
