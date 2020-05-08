 package com.robonobo.gui.components;
 
 import static com.robonobo.common.util.CodeUtil.*;
 import static com.robonobo.gui.GuiUtil.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.event.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.decorator.*;
 import org.jdesktop.swingx.decorator.SortOrder;
 import org.jdesktop.swingx.table.TableColumnExt;
 import org.jdesktop.swingx.table.TableColumnModelExt;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.core.api.RobonoboException;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
 import com.robonobo.core.api.model.Track.PlaybackStatus;
 import com.robonobo.gui.*;
 import com.robonobo.gui.components.base.RBoldMenuItem;
 import com.robonobo.gui.components.base.RMenuItem;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.model.TrackListTableModel;
 import com.robonobo.gui.panels.MyPlaylistContentPanel;
 
 @SuppressWarnings("serial")
 public class TrackList extends JPanel {
 	/** If the track list has more than this many tracks, we show a helpful message while we create/change it as the ui might hang for a second or two */
	public static final int TRACKLIST_SIZE_THRESHOLD = 256;
 
 	JScrollPane scrollPane;
 	JXTable table;
 	TrackListTableModel model;
 	Icon startingIcon = new SpinnerIcon(16, RoboColor.DARKISH_GRAY);
 	Icon playingIcon = GuiUtil.createImageIcon("/table/play.png", null);
 	Icon pausedIcon = GuiUtil.createImageIcon("/table/pause.png", null);
 	Icon downloadingIcon = GuiUtil.createImageIcon("/table/download.png", null);
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
 				frame.getPlaybackPanel().trackSelectionChanged();
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
 		table.getColumn(11).setCellRenderer(tr);
 		table.getColumn(12).setCellRenderer(tr);
 
 		// NOTE disabling sorting for now as it causes massive performance hits when tracks are being inserted
 		table.setSortable(false);
 		
 		// Render table header as not bold and with sorting arrows
 		// NOTE massively irritating bug in java5 (maybe mac only, but they're the only ones stuck on j5 anyway) that
 		// renders the table header as white if we set a custom renderer here. So we only do it in java 6+ - means that
 		// java5 users don't see the sorting arrow, but it's better than a white header
 		if (javaMajorVersion() >= 6) {
 			table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
 				ImageIcon ascSortIcon = GuiUtil.createImageIcon("/icon/arrow_up.png", null);
 				ImageIcon descSortIcon = GuiUtil.createImageIcon("/icon/arrow_down.png", null);
 
 				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
 						boolean hasFocus, int row, int column) {
 					JLabel result = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
 							row, column);
 					result.setFont(RoboFont.getFont(12, false));
 					result.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
 					SortOrder so = TrackList.this.table.getSortOrder(column);
 					if (so.isSorted()) {
 						if (so.isAscending())
 							setIcon(ascSortIcon);
 						else
 							setIcon(descSortIcon);
 					} else
 						result.setIcon(null);
 					result.setHorizontalTextPosition(SwingConstants.LEFT);
 					return result;
 				}
 			});
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
 					frame.getPlaybackPanel().play();
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
 
 		scrollPane = new JScrollPane(table);
 		if (model.wantScrollEventsEver()) {
 			viewportListener = new ViewportListener();
 			scrollPane.getViewport().addChangeListener(viewportListener);
 		}
 		add(scrollPane, "0,0");
 	}
 
 	public void filterTracks(String filterStr) {
 		table.clearSelection();
 		if (filterStr == null || filterStr.length() == 0) {
 			table.setFilters(null);
 		} else {
 			final String lcf = filterStr.toLowerCase();
 			// Only include rows that have a matching title, artist, album or
 			// year
 			final int[] cols = { 1, 2, 3, 7 };
 			table.setFilters(new FilterPipeline(new MultiColumnPatternFilter(lcf, Pattern.CASE_INSENSITIVE, cols)));
 		}
 		updateViewport();
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
 		int[] selRows = getSelectedRowsAsPerModel();
 		List<String> result = new ArrayList<String>(selRows.length);
 		for (int row : selRows) {
 			String sid = model.getStreamId(row);
 			if (sid != null)
 				result.add(sid);
 		}
 		return result;
 	}
 
 	public List<Track> getSelectedTracks() {
 		int[] selRows = getSelectedRowsAsPerModel();
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
 		table.removeRowSelectionInterval(0, table.getRowCount() - 1);
 	}
 
 	public String getNextStreamId(String curStreamId) {
 		int modelIndex = model.getTrackIndex(curStreamId);
 		if (modelIndex < 0)
 			return null;
 		int tblIndex = table.convertRowIndexToView(modelIndex);
 		if (tblIndex >= table.getRowCount() - 1)
 			return null;
 		int nextModelIndex = table.convertRowIndexToModel(tblIndex + 1);
 		return model.getStreamId(nextModelIndex);
 	}
 
 	public String getPrevStreamId(String curStreamId) {
 		int modelIndex = model.getTrackIndex(curStreamId);
 		if (modelIndex < 0)
 			return null;
 		int tblIndex = table.convertRowIndexToView(modelIndex);
 		if (tblIndex <= 0)
 			return null;
 		int prevModelIndex = table.convertRowIndexToModel(tblIndex - 1);
 		return model.getStreamId(prevModelIndex);
 	}
 
 	public void scrollTableToStream(String streamId) {
 		int modelIndex = model.getTrackIndex(streamId);
 		if (modelIndex < 0)
 			return;
 		int viewIndex = table.convertRowIndexToView(modelIndex);
 		if (viewIndex < 0 || viewIndex >= table.getRowCount() - 1)
 			return;
 		table.scrollRowToVisible(viewIndex);
 	}
 
 	protected String getStreamIdAtRow(int viewIndex) {
 		int modelIndex = table.convertRowIndexToModel(viewIndex);
 		return model.getStreamId(modelIndex);
 	}
 
 	public int[] getSelectedRowsAsPerModel() {
 		int[] tableRows = table.getSelectedRows();
 		int[] modelRows = new int[tableRows.length];
 		for (int i = 0; i < tableRows.length; i++) {
 			modelRows[i] = table.convertRowIndexToModel(tableRows[i]);
 		}
 		return modelRows;
 	}
 
 	public void deleteSelectedTracks() {
 		if (model.allowDelete()) {
 			SwingUtilities.invokeLater(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					final List<String> selSids = getSelectedStreamIds();
 					if (selSids.size() > 0) {
 						frame.getController().getExecutor().execute(new CatchingRunnable() {
 							public void doRun() throws Exception {
 								model.deleteTracks(selSids);
 							}
 						});
 					}
 				}
 			});
 		}
 	}
 
 	public void updateViewport() {
 		if(viewportListener != null) {
 			runOnUiThread(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					viewportListener.checkViewportAndFire();
 				}
 			});
 		}
 	}
 	
 	class PopupMenu extends JPopupMenu implements ActionListener {
 		public PopupMenu() {
 		}
 
 		void refresh() {
 			removeAll();
 			RMenuItem play = new RBoldMenuItem("Play");
 			play.setActionCommand("play");
 			play.addActionListener(this);
 			add(play);
 			boolean needDownload = false;
 			for (Track track : getSelectedTracks()) {
 				if (track instanceof CloudTrack) {
 					needDownload = true;
 					break;
 				}
 			}
 			if (needDownload) {
 				RMenuItem dl = new RMenuItem("Download");
 				dl.setActionCommand("download");
 				dl.addActionListener(this);
 				add(dl);
 			}
 			JMenu plMenu = new JMenu("Add to playlist");
 			RMenuItem newPl = new RMenuItem("New Playlist");
 			newPl.setActionCommand("newpl");
 			newPl.addActionListener(this);
 			plMenu.add(newPl);
 			for (long plId : frame.getController().getMyUser().getPlaylistIds()) {
 				Playlist p = frame.getController().getPlaylist(plId);
 				RMenuItem pmi = new RMenuItem(p.getTitle());
 				pmi.setActionCommand("pl-" + p.getPlaylistId());
 				pmi.addActionListener(this);
 				plMenu.add(pmi);
 			}
 			add(plMenu);
 			if (model.allowDelete()) {
 				RMenuItem del = new RMenuItem("Delete");
 				del.setActionCommand("delete");
 				del.addActionListener(this);
 				add(del);
 			}
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String action = e.getActionCommand();
 			if (action.equals("play"))
 				frame.getPlaybackPanel().play();
 			else if (action.equals("download")) {
 				for (Track t : getSelectedTracks()) {
 					if (t instanceof CloudTrack) {
 						try {
 							frame.getController().addDownload(t.getStream().getStreamId());
 						} catch (RobonoboException ex) {
 							log.error("Caught exception adding download from popup menu", ex);
 						}
 					}
 				}
 			} else if (action.equals("newpl")) {
 				MyPlaylistContentPanel cp = (MyPlaylistContentPanel) frame.getMainPanel()
 						.getContentPanel("newplaylist");
 				cp.addTracks(getSelectedStreamIds());
 			} else if (action.startsWith("pl-")) {
 				long plId = Long.parseLong(action.substring(3));
 				MyPlaylistContentPanel cp = (MyPlaylistContentPanel) frame.getMainPanel().getContentPanel(
 						"playlist/" + plId);
 				cp.addTracks(getSelectedStreamIds());
 			} else if (action.equals("delete")) {
 				frame.getMainPanel().currentContentPanel().getTrackList().deleteSelectedTracks();
 			} else
 				log.error("PopupMenu generated unknown action: " + action);
 		}
 	}
 
 	class TextRenderer extends DefaultTableCellRenderer {
 		Font plainFont = RoboFont.getFont(13, false);
 		Font boldFont = RoboFont.getFont(13, true);
 
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
 				boolean hasFocus, int row, int column) {
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
 			GuiUtil.makeTextLookLessRubbish(g);
 			super.paintComponent(g);
 		}
 	}
 
 	class PlaybackStatusRenderer extends DefaultTableCellRenderer {
 		@Override
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
 				boolean hasFocus, int row, int column) {
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
 
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
 				boolean hasFocus, int row, int column) {
 			JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 			if (value instanceof DownloadingTransferStatus) {
 				DownloadingTransferStatus dStat = (DownloadingTransferStatus) value;
 				// Show a progress bar with how much we're downloading
 				String streamId = getStreamIdAtRow(row);
 				DownloadingTrack d = (DownloadingTrack) model.getTrack(model.getTrackIndex(streamId));
 				if (d == null)
 					return new JLabel();
 				long streamSz = d.getStream().getSize();
 				float complete = (float) d.getBytesDownloaded() / streamSz;
 				int pcnt = (int) (100 * complete);
 				int numSources = d.getNumSources();
 				if (d.getDownloadStatus() == DownloadStatus.Paused) {
 					pBar.setValue(0);
 					pBar.setEnabled(false);
 					pBar.setString("queued");
 				} else {
 					pBar.setValue(pcnt);
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
 
 	class ViewportListener implements ChangeListener {
 		int firstRow = -1, lastRow = -1;
 		JViewport v;
 		
 		public ViewportListener() {
 			v = scrollPane.getViewport();
 		}
 
 		public void stateChanged(ChangeEvent e) {
 			checkViewportAndFire();
 		}
 
 		public void checkViewportAndFire() {
 			if(!model.wantScrollEventsNow())
 				return;
 			Point viewPos = v.getViewPosition();
 			Dimension viewSz = v.getExtentSize();
 			int newFirstRow = table.rowAtPoint(viewPos);
 			int newLastRow = table.rowAtPoint(new Point(viewPos.x, viewPos.y + viewSz.height));
 			if(newFirstRow == firstRow && newLastRow == lastRow)
 				return;
 			if(newFirstRow < 0 || newLastRow < 0)
 				return;
 			firstRow = newFirstRow;
 			lastRow = newLastRow;
 			int[] modelIdxs = new int[lastRow - firstRow + 1];
 			for(int i=0;i<modelIdxs.length;i++) {
 				modelIdxs[i] = table.convertRowIndexToModel(firstRow+i);
 			}
 			getModel().onScroll(modelIdxs);
 		}
 	}
 }
