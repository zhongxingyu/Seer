 package com.robonobo.gui.components;
 
 import static com.robonobo.common.util.CodeUtil.*;
 import static com.robonobo.gui.GuiUtil.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 import java.util.regex.Matcher;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.decorator.HighlighterFactory;
 import org.jdesktop.swingx.decorator.PatternFilter;
 import org.jdesktop.swingx.table.TableColumnExt;
 import org.jdesktop.swingx.table.TableColumnModelExt;
 
 import ca.odell.glazedlists.matchers.MatcherEditor;
 import ca.odell.glazedlists.matchers.MatcherEditor.Event;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.core.Platform;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
 import com.robonobo.core.api.model.Track.PlaybackStatus;
 import com.robonobo.gui.*;
 import com.robonobo.gui.components.base.RBoldMenuItem;
 import com.robonobo.gui.components.base.RMenuItem;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.model.GlazedTrackListTableModel;
 import com.robonobo.gui.model.TrackListTableModel;
 import com.robonobo.gui.panels.MyPlaylistContentPanel;
 import com.robonobo.gui.sheets.ConfirmTrackDeleteSheet;
 
 @SuppressWarnings("serial")
 public class TrackList extends JPanel {
 	static DateFormat dateAddedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 	/** If the track list has more than this many tracks, we show a helpful message while we create/change it as the ui
 	 * might hang for a second or two */
 	public static final int TRACKLIST_SIZE_THRESHOLD = 64;
 	JScrollPane scrollPane;
 	JXTable table;
 	TrackListTableModel model;
 	Icon startingIcon = new SpinnerIcon(16, RoboColor.DARKISH_GRAY);
 	Icon playingIcon = GuiUtil.createImageIcon("/table/play.png");
 	Icon pausedIcon = GuiUtil.createImageIcon("/table/pause.png");
 	Icon downloadingIcon = GuiUtil.createImageIcon("/table/download.png");
 	Log log;
 	RobonoboFrame frame;
 	PopupMenu popupMenu = new PopupMenu();
 	ViewportListener viewportListener;
 
 	public TrackList(final RobonoboFrame frame, TrackListTableModel model) {
 		this.model = model;
 		this.frame = frame;
 		log = LogFactory.getLog(getClass());
 		setLayout(new GridLayout(1, 0));
 		setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
 		table = new JXTable(model);
 		table.setFont(RoboFont.getFont(13, false));
 		table.setRowHeight(21);
 		table.setColumnControlVisible(true);
 		table.setHorizontalScrollEnabled(true);
 		table.setFillsViewportHeight(true);
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		table.setBackground(Color.WHITE);
 		table.setHighlighters(HighlighterFactory.createSimpleStriping());
 		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		// When the selection changes, notify our playback panel
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				frame.mainPanel.getPlaybackPanel().trackSelectionChanged();
 			}
 		});
 		// Cell renderers
 		table.getColumn(0).setCellRenderer(new PlaybackStatusRenderer());
 		TextRenderer tr = new TextRenderer();
 		table.getColumn(1).setCellRenderer(tr);
 		table.getColumn(2).setCellRenderer(tr);
 		table.getColumn(3).setCellRenderer(tr);
 		table.getColumn(4).setCellRenderer(tr);
 		table.getColumn(5).setCellRenderer(tr);
 		table.getColumn(6).setCellRenderer(tr);
 		table.getColumn(7).setCellRenderer(tr);
 		table.getColumn(8).setCellRenderer(new TransferStatusCellRenderer());
 		table.getColumn(9).setCellRenderer(tr);
 		table.getColumn(10).setCellRenderer(tr);
 		table.getColumn(11).setCellRenderer(new DateRenderer());
 		table.getColumn(12).setCellRenderer(tr);
 		// These incantations make the table compatible with glazedlist which does its own sorting inside the model
 		table.setSortable(false);
 		table.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
 		table.getTableHeader().setFont(RoboFont.getFont(13, false));
 		// These throw NoSuchMethodError on j5
 		if (javaMajorVersion() >= 6) {
 			table.setAutoCreateRowSorter(false);
 			table.setRowSorter(null);
 		}
 		// Set up glazedlist auto table sorter
 		scrollPane = new JScrollPane(table);
 		if (model.wantScrollEventsEver())
 			viewportListener = new ViewportListener();
 		if (model instanceof GlazedTrackListTableModel) {
 			GlazedTrackListTableModel gtltm = (GlazedTrackListTableModel) model;
 			// TODO Disabling sorting for now until glazedlists bug is fixed
 //			if (gtltm.canSort()) {
 //				TableComparatorChooser<Track> tcc = TableComparatorChooser.install(table, gtltm.getSortedList(), TableComparatorChooser.SINGLE_COLUMN);
 //				if (viewportListener != null)
 //					tcc.addSortActionListener(viewportListener);
 //			}
 			MatcherEditor<Track> matchEdit = gtltm.getMatcherEditor();
 			if (matchEdit != null && viewportListener != null)
 				matchEdit.addMatcherEditorListener(viewportListener);
 		}
 		TableColumnModelExt cm = (TableColumnModelExt) table.getColumnModel();
 		cm.getColumn(0).setPreferredWidth(22); // Status icon
 		cm.getColumn(1).setPreferredWidth(187); // Title
 		cm.getColumn(2).setPreferredWidth(137); // Artist
 		cm.getColumn(3).setPreferredWidth(139); // Album
 		cm.getColumn(4).setPreferredWidth(44); // Track
 		cm.getColumn(5).setPreferredWidth(40); // Year
 		cm.getColumn(6).setPreferredWidth(47); // Time
 		cm.getColumn(7).setPreferredWidth(60); // Size
 		cm.getColumn(8).setPreferredWidth(160); // Status
 		cm.getColumn(9).setPreferredWidth(80); // Download
 		cm.getColumn(10).setPreferredWidth(80); // Upload
 		cm.getColumn(11).setPreferredWidth(140); // Date Added
 		cm.getColumn(12).setPreferredWidth(300); // Stream Id
 		int[] hiddenCols = model.hiddenCols();
 		List<TableColumn> cols = cm.getColumns(true);
 		for (int i = 0; i < hiddenCols.length; i++) {
 			TableColumnExt colExt = (TableColumnExt) cols.get(hiddenCols[i]);
 			colExt.setVisible(false);
 		}
 		table.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					frame.mainPanel.getPlaybackPanel().playSelectedTracks();
 					e.consume();
 				}
 			}
 
 			public void mousePressed(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			private void maybeShowPopup(MouseEvent e) {
 				if (!e.isPopupTrigger())
 					return;
 				int mouseRow = table.rowAtPoint(e.getPoint());
 				boolean alreadySel = false;
 				for (int selRow : table.getSelectedRows()) {
 					if (selRow == mouseRow) {
 						alreadySel = true;
 						break;
 					}
 				}
 				if (!alreadySel)
 					table.getSelectionModel().addSelectionInterval(mouseRow, mouseRow);
 				popupMenu.refresh();
 				popupMenu.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 		if (viewportListener != null)
 			scrollPane.getViewport().addChangeListener(viewportListener);
 		add(scrollPane, "0,0");
 	}
 
 	public TrackListTableModel getModel() {
 		return model;
 	}
 
 	public JTable getJTable() {
 		return table;
 	}
 
 	public boolean anyStreamsSelected() {
 		return (table.getSelectedRows().length > 0);
 	}
 
 	public List<String> getSelectedStreamIds() {
 		int[] selRows = table.getSelectedRows();
 		List<String> result = new ArrayList<String>(selRows.length);
 		for (int row : selRows) {
 			String sid = model.getStreamId(row);
 			if (sid != null)
 				result.add(sid);
 		}
 		return result;
 	}
 
 	public List<Track> getSelectedTracks() {
 		int[] selRows = table.getSelectedRows();
 		List<Track> result = new ArrayList<Track>(selRows.length);
 		for (int row : selRows) {
 			// This might be null if we are in the middle of deleting rows
 			Track t = model.getTrack(row);
 			if (t != null)
 				result.add(t);
 		}
 		return result;
 	}
 
 	public void clearTableSelection() {
 		int rowCount = table.getRowCount();
 		if (rowCount > 0)
 			table.removeRowSelectionInterval(0, rowCount - 1);
 	}
 
 	/** Returns String[2], first is prev sid, second is next sid, either might be null - we get both at the same time to
 	 * avoid iterating over the list twice */
 	public String[] getPrevAndNextSids(String currentSid) {
 		String[] result = new String[2];
 		int idx = model.getTrackIndex(currentSid);
 		if (idx < 0)
 			return result;
 		int sz = model.getRowCount();
 		if (idx > 0)
 			result[0] = model.getStreamId(idx - 1);
 		if (idx < (sz - 1))
 			result[1] = model.getStreamId(idx + 1);
 		return result;
 	}
 
 	public void scrollTableToStream(String streamId) {
 		int idx = model.getTrackIndex(streamId);
 		if (idx < 0)
 			return;
 		table.scrollRowToVisible(idx);
 	}
 
 	/** Call only on the ui thread */
 	public void deleteSelectedTracks() {
 		log.warn("DEBUG: TrackList (model "+shortClassName(model.getClass())+") deleting tracks");
 		if (!SwingUtilities.isEventDispatchThread())
 			throw new SeekInnerCalmException();
 		if (model.allowDelete()) {
 			if (frame.guiCfg.getConfirmTrackDelete())
 				frame.showSheet(new ConfirmTrackDeleteSheet(frame, model, getSelectedStreamIds()));
 			else {
 				final List<String> selSids = getSelectedStreamIds();
 				if (selSids.size() > 0) {
 					frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 						public void doRun() throws Exception {
 							model.deleteTracks(selSids);
 						}
 					});
 				}
 			}
 		} else {
 			log.warn("DEBUG: Not deleting tracks - model does not allow");
 		}
 	}
 
 	public void updateViewport() {
 		if (viewportListener != null) {
 			runOnUiThread(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					viewportListener.viewportChanged();
 				}
 			});
 		}
 	}
 
 	class PopupMenu extends JPopupMenu implements ActionListener {
 		public PopupMenu() {
 		}
 
 		void refresh() {
 			removeAll();
 			UserConfig uc = frame.ctrl.getMyUserConfig();
 			RMenuItem play = new RBoldMenuItem("Play");
 			play.setActionCommand("play");
 			play.addActionListener(this);
 			add(play);
 			boolean needDownload = false;
 			boolean needShow = false;
 			boolean needPause = false;
 			boolean needResume = false;
 			boolean needCancel = false;
 			for (Track track : getSelectedTracks()) {
 				if (track instanceof CloudTrack)
 					needDownload = true;
 				if (track instanceof SharedTrack)
 					needShow = true;
 				if(track instanceof DownloadingTrack) {
 					needCancel = true;
 					DownloadingTrack dt = (DownloadingTrack) track;
 					switch(dt.getDownloadStatus()) {
 					case Downloading:
 						needPause = true;
 						break;
 					case Paused:
 						needResume = true;
 						break;
 					}
 				}
 			}
 			List<String> selSids = getSelectedStreamIds();
 			boolean needLove = !(frame.ctrl.lovingAll(selSids));
 			if (needLove)
 				addMi("Love", "love");
 			if (needDownload)
 				addMi("Download", "download");
 			if(needPause)
 				addMi("Pause download", "pause");
 			if(needResume)
 				addMi("Resume download", "resume");
 			if(needCancel)
 				addMi("Cancel download", "cancel");
 			String radioCfg = uc.getItem("radioPlaylist");
 			if("manual".equalsIgnoreCase(radioCfg))
 				addMi("Add to Radio", "radio");
 			JMenu plMenu = new JMenu("Add to playlist");
 			RMenuItem newPl = new RMenuItem("New Playlist");
 			newPl.setActionCommand("newpl");
 			newPl.addActionListener(this);
 			plMenu.add(newPl);
 			for (long plId : frame.ctrl.getMyUser().getPlaylistIds()) {
 				Playlist p = frame.ctrl.getKnownPlaylist(plId);
 				if (p != null && !frame.ctrl.isSpecialPlaylist(p.getTitle())) {
 					RMenuItem pmi = new RMenuItem(p.getTitle());
 					pmi.setActionCommand("pl-" + p.getPlaylistId());
 					pmi.addActionListener(this);
 					plMenu.add(pmi);
 				}
 			}
 			add(plMenu);
 			if (model.allowDelete())
 				addMi(model.deleteTracksTooltipDesc(), "delete");
 			
 			String fileManagerName = Platform.getPlatform().fileManagerName();
 			if (needShow && fileManagerName != null)
 				addMi("Show in " + fileManagerName, "show");
 		}
 
 		protected void addMi(String title, String cmd) {
 			RMenuItem rmi = new RMenuItem(title);
 			rmi.setActionCommand(cmd);
 			rmi.addActionListener(this);
 			add(rmi);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String action = e.getActionCommand();
 			if (action.equals("play"))
 				frame.mainPanel.getPlaybackPanel().playSelectedTracks();
 			else if (action.equals("download")) {
 				final List<String> dlSids = new ArrayList<String>();
 				for (Track t : getSelectedTracks()) {
 					if (t instanceof CloudTrack)
 						dlSids.add(t.getStream().getStreamId());
 				}
 				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						for (String sid : dlSids) {
 							frame.ctrl.addDownload(sid);
 						}
 					}
 				});
 			} else if (action.equals("newpl")) {
 				MyPlaylistContentPanel cp = (MyPlaylistContentPanel) frame.mainPanel.getContentPanel("newplaylist");
 				cp.addTracks(getSelectedStreamIds());
 			} else if (action.startsWith("pl-")) {
 				long plId = Long.parseLong(action.substring(3));
 				MyPlaylistContentPanel cp = (MyPlaylistContentPanel) frame.mainPanel.getContentPanel("playlist/" + plId);
 				cp.addTracks(getSelectedStreamIds());
 			} else if (action.equals("delete")) {
 				frame.mainPanel.currentContentPanel().trackList.deleteSelectedTracks();
 			} else if (action.equals("show")) {
 				File showFile = null;
 				for (Track t : getSelectedTracks()) {
 					if (t instanceof SharedTrack) {
 						showFile = ((SharedTrack) t).getFile();
 						break;
 					}
 				}
 				if (showFile != null) {
 					final File finalFile = showFile;
 					frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 						public void doRun() throws Exception {
 							Platform.getPlatform().showFileInFileManager(finalFile);
 						}
 					});
 				}
 			} else if (action.equals("love")) {
 				final List<String> selSids = getSelectedStreamIds();
 				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						frame.ctrl.love(selSids);
 					}
 				});
 			} else if(action.equals("radio")) {
 				frame.ctrl.addToRadio(getSelectedStreamIds());
 			} else if(action.equals("pause")) {
 				final Set<String> pauseSids = new HashSet<String>();
 				for (Track t : getSelectedTracks()) {
 					if(t instanceof DownloadingTrack) {
 						DownloadingTrack dt = (DownloadingTrack) t;
 						if(dt.getDownloadStatus() == DownloadStatus.Downloading)
 							pauseSids.add(t.getStream().getStreamId());
 					}
 				}
 				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						for (String sid : pauseSids) {
 							frame.ctrl.pauseDownload(sid);
 						}
 					}
 				});
 			} else if(action.equals("resume")) {
 				final Set<String> resSids = new HashSet<String>();
 				for (Track t : getSelectedTracks()) {
 					if(t instanceof DownloadingTrack) {
 						DownloadingTrack dt = (DownloadingTrack) t;
 						if(dt.getDownloadStatus() == DownloadStatus.Paused)
 							resSids.add(t.getStream().getStreamId());
 					}
 				}
 				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						for (String sid : resSids) {
 							frame.ctrl.startDownload(sid);
 						}
 					}
 				});
 			} else if(action.equals("cancel")) {
 				final Set<String> dlSids = new HashSet<String>();
 				for (Track t : getSelectedTracks()) {
 					if(t instanceof DownloadingTrack) {
 						dlSids.add(t.getStream().getStreamId());
 					}
 				}
 				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						frame.ctrl.deleteDownloads(dlSids);
 					}
 				});
 			} else
 				log.error("PopupMenu generated unknown action: " + action);
 		}
 	}
 
 	class TextRenderer extends DefaultTableCellRenderer {
 		Font plainFont = RoboFont.getFont(13, false);
 		Font boldFont = RoboFont.getFont(13, true);
 
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 			Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 			((JComponent) result).setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
 			if (column == 1)
 				result.setFont(boldFont);
 			else
 				result.setFont(plainFont);
 			return result;
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			makeTextLookLessRubbish(g);
 			super.paintComponent(g);
 		}
 	}
 
 	class DateRenderer extends DefaultTableCellRenderer {
 		Font font = RoboFont.getFont(13, false);
 
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 			String valStr = (value == null) ? "" : dateAddedFormat.format(value);
 			Component result = super.getTableCellRendererComponent(table, valStr, isSelected, hasFocus, row, column);
 			result.setFont(font);
 			return result;
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			makeTextLookLessRubbish(g);
 			super.paintComponent(g);
 		}
 	}
 
 	class PlaybackStatusRenderer extends DefaultTableCellRenderer {
 		@Override
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 			JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 			lbl.setText(null);
 			lbl.setHorizontalAlignment(JLabel.CENTER);
 			if (value == null) {
 				lbl.setIcon(null);
 				return lbl;
 			}
 			PlaybackStatus status = (PlaybackStatus) value;
 			switch (status) {
 			case Starting:
 				lbl.setIcon(startingIcon);
 				break;
 			case Playing:
 				lbl.setIcon(playingIcon);
 				break;
 			case Paused:
 				lbl.setIcon(pausedIcon);
 				break;
 			case Downloading:
 				lbl.setIcon(downloadingIcon);
 				break;
 			default:
 				lbl.setIcon(null);
 				break;
 			}
 			return lbl;
 		}
 	}
 
 	private class TransferStatusCellRenderer extends DefaultTableCellRenderer {
 		JProgressBar pBar;
 		JPanel pBarPnl;
 		Border lblBorder = BorderFactory.createEmptyBorder(0, 5, 0, 5);
 
 		public TransferStatusCellRenderer() {
 			pBar = new DownloadStatusProgressBar();
 			pBar.setMinimum(0);
 			pBar.setMaximum(100);
 			pBarPnl = new JPanel();
 			pBarPnl.setLayout(new BoxLayout(pBarPnl, BoxLayout.X_AXIS));
 			pBarPnl.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
 			pBarPnl.setOpaque(true);
 			pBarPnl.add(pBar);
 		}
 
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 			JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 			if (value instanceof DownloadingTransferStatus) {
 				// Show a progress bar with how much we're downloading
 				DownloadingTrack d = (DownloadingTrack) model.getTrack(row);
 				if (d == null)
 					return new JLabel();
 				long streamSz = d.getStream().getSize();
 				float complete = (float) d.getBytesDownloaded() / streamSz;
 				int pcnt = (int) (100 * complete);
 				int numSources = d.getNumSources();
 				pBar.setValue(pcnt);
 				if (d.getDownloadStatus() == DownloadStatus.Paused) {
 					pBar.setEnabled(false);
 					pBar.setString("Queued (" + numSources + ")");
 				} else {
 					pBar.setEnabled(true);
 					pBar.setString("Downloading (" + numSources + "): " + pcnt + "%");
 				}
 				Color bg = lbl.getBackground();
 				pBarPnl.setBackground(bg);
 				return pBarPnl;
 			} else {
 				lbl.setBorder(lblBorder);
 				return lbl;
 			}
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			GuiUtil.makeTextLookLessRubbish(g);
 			super.paintComponent(g);
 		}
 	}
 
 	class MultiColumnPatternFilter extends PatternFilter {
 		private int[] cols;
 
 		/** cols are column indices as per model */
 		public MultiColumnPatternFilter(String pattern, int matchFlags, int[] cols) {
 			setPattern(pattern, matchFlags);
 			this.cols = cols;
 		}
 
 		@Override
 		public boolean test(int row) {
 			if (pattern == null)
 				return false;
 			boolean result = false;
 			for (int col : cols) {
 				if (adapter.isTestable(col)) {
 					String text = getInputString(row, col);
 					if (text != null && (text.length() > 0)) {
 						Matcher m = pattern.matcher(text);
 						if (m.find()) {
 							result = true;
 							break;
 						}
 					}
 				}
 			}
 			return result;
 		}
 	}
 
 	class ViewportListener implements ChangeListener, ActionListener, MatcherEditor.Listener<Track> {
 		int firstRow = -1, lastRow = -1;
 		boolean modelChanged = false;
 		JViewport v;
 
 		public ViewportListener() {
 			v = scrollPane.getViewport();
 		}
 
 		public void stateChanged(ChangeEvent e) {
 			// Called when the table is scrolled
 			viewportChanged();
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			// Called when the table is resorted
 			modelChanged = true;
 			viewportChanged();
 		}
 
 		public void changedMatcher(Event<Track> matcherEvent) {
 			// Called when the table is filtered
 			modelChanged = true;
 			viewportChanged();
 		}
 
 		public void viewportChanged() {
 			if (!model.wantScrollEventsNow())
 				return;
 			Point viewPos = v.getViewPosition();
 			Dimension viewSz = v.getExtentSize();
 			int rowHeight = table.getRowHeight();
 			int rowsInViewport = viewSz.height / rowHeight + 1;
 			if (rowsInViewport > model.getRowCount())
 				rowsInViewport = model.getRowCount();
 			int newFirstRow = table.rowAtPoint(viewPos);
 			if (newFirstRow < 0)
 				return;
 			int newLastRow = newFirstRow + rowsInViewport - 1;
 			if ((!modelChanged) && newFirstRow == firstRow && newLastRow == lastRow)
 				return;
 			modelChanged = false;
 			firstRow = newFirstRow;
 			lastRow = newLastRow;
 			int[] modelIdxs = new int[lastRow - firstRow + 1];
 			for (int i = 0; i < modelIdxs.length; i++) {
 				modelIdxs[i] = table.convertRowIndexToModel(firstRow + i);
 			}
 			model.onScroll(modelIdxs);
 		}
 	}
 }
