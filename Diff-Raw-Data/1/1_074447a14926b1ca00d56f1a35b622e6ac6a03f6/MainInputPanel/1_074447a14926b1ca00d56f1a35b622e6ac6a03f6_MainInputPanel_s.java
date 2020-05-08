 package pleocmd.itfc.gui;
 
 import java.awt.EventQueue;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.AbstractListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.filechooser.FileFilter;
 
 import pleocmd.Log;
 import pleocmd.StandardInput;
 
 public final class MainInputPanel extends JPanel {
 
 	private static final long serialVersionUID = 8130292678723649962L;
 
 	private final HistoryListModel historyListModel;
 
 	private final JList historyList;
 
 	private final JTextField consoleInput;
 
 	private final JButton btnSend;
 
 	private final JButton btnSendEOS;
 
 	private final JButton btnRead;
 
 	private final JButton btnClear;
 
 	public MainInputPanel() {
 		final Layouter lay = new Layouter(this);
 
 		historyListModel = new HistoryListModel();
 		historyList = new JList(getHistoryListModel());
 		lay.addWholeLine(new JScrollPane(getHistoryList(),
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), true);
 		getHistoryList().addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(final MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					final int idx = getHistoryList().getSelectedIndex();
 					if (idx != -1)
 						putInput(getHistoryListModel().getElementAt(idx)
 								.toString());
 				}
 			}
 		});
 
 		consoleInput = new JTextField();
 		consoleInput.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(final KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) putConsoleInput();
 			}
 		});
 		lay.addWholeLine(consoleInput, false);
 
 		btnSend = lay.addButton("Send", "system-run",
 				"Send text from input-field to the pipe", new Runnable() {
 					@Override
 					public void run() {
 						putConsoleInput();
 					}
 				});
 		btnSendEOS = lay.addButton("Send EOS", "media-playback-stop",
 				"Send end-of-stream signal", new Runnable() {
 					@Override
 					public void run() {
 						closeConsoleInput();
 					}
 				});
 		btnRead = lay.addButton("Read From ...", "document-import",
 				"Uses the whole contents of a file as it was "
 						+ "entered in the input field", new Runnable() {
 					@Override
 					public void run() {
 						readConsoleInputFromFile();
 					}
 				});
 		lay.addSpacer();
 		btnClear = lay.addButton("Clear History", "archive-remove",
 				"Clears the history list of recently entered input",
 				new Runnable() {
 					@Override
 					public void run() {
 						clearHistory();
 					}
 				});
 	}
 
 	public void putConsoleInput() {
 		putInput(consoleInput.getText());
 		consoleInput.setText("");
 	}
 
 	public void putInput(final String input) {
 		try {
 			StandardInput.the().put((input + "\n").getBytes("ISO-8859-1"));
 			getHistoryListModel().add(input);
 			EventQueue.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					final int size = getHistoryListModel().getSize() - 1;
 					getHistoryList().scrollRectToVisible(
 							getHistoryList().getCellBounds(size, size));
 					updateState();
 				}
 			});
 		} catch (final IOException exc) {
 			Log.error(exc);
 		}
 	}
 
 	public void closeConsoleInput() {
 		try {
 			StandardInput.the().close();
 		} catch (final IOException exc) {
 			Log.error(exc);
 		}
 	}
 
 	public void readConsoleInputFromFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(final File f) {
 				return true;
 			}
 
 			@Override
 			public String getDescription() {
 				return "Ascii-Textfile containing Data-List";
 			}
 		});
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 			readConsoleInputFromFile(fc.getSelectedFile());
 	}
 
 	public void readConsoleInputFromFile(final File file) {
 		try {
 			final BufferedReader in = new BufferedReader(new FileReader(file));
 			String line;
 			while ((line = in.readLine()) != null)
 				StandardInput.the().put((line + '\n').getBytes("ISO-8859-1"));
 			in.close();
 		} catch (final IOException exc) {
 			Log.error(exc);
 		}
 	}
 
 	public void clearHistory() {
 		getHistoryListModel().clear();
 		updateState();
 	}
 
 	public List<String> getHistory() {
 		return getHistoryListModel().getAll();
 	}
 
 	protected JList getHistoryList() {
 		return historyList;
 	}
 
 	protected HistoryListModel getHistoryListModel() {
 		return historyListModel;
 	}
 
 	class HistoryListModel extends AbstractListModel {
 
 		private static final long serialVersionUID = 4510015901086617192L;
 
 		private final List<String> history = new ArrayList<String>();
 
 		@Override
 		public int getSize() {
 			return history.size();
 		}
 
 		@Override
 		public Object getElementAt(final int index) {
 			return history.get(index);
 		}
 
 		public void add(final String line) {
 			history.add(line);
 			fireIntervalAdded(this, history.size() - 1, history.size() - 1);
 		}
 
 		public void clear() {
 			final int size = history.size();
 			history.clear();
 			fireIntervalRemoved(this, 0, size - 1);
 		}
 
 		public List<String> getAll() {
 			return Collections.unmodifiableList(history);
 		}
 
 	}
 
 	public void updateState() {
 		final boolean ready = MainFrame.the().isPipeRunning()
 				&& !StandardInput.the().isClosed();
 		consoleInput.setEnabled(ready);
 		btnSend.setEnabled(ready);
 		btnSendEOS.setEnabled(ready);
 		btnRead.setEnabled(ready);
 		btnClear.setEnabled(historyListModel.getSize() > 0);
 	}
 
 }
