 package com.robonobo.gui.components;
 
 import static com.robonobo.gui.GuiUtil.*;
 import static com.robonobo.gui.RoboColor.*;
 import static javax.swing.SwingUtilities.*;
 
 import java.awt.*;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.*;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jdesktop.swingx.renderer.DefaultListRenderer;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.core.Platform;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.model.*;
 import com.robonobo.gui.RoboFont;
 import com.robonobo.gui.components.base.RLabel12;
 import com.robonobo.gui.components.base.RMenuItem;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.model.PlaylistListModel;
 import com.robonobo.gui.model.StreamTransfer;
 import com.robonobo.gui.panels.ContentPanel;
 import com.robonobo.gui.panels.LeftSidebar;
 import com.robonobo.gui.sheets.DeletePlaylistSheet;
 import com.robonobo.gui.sheets.SharePlaylistSheet;
 
 @SuppressWarnings("serial")
 public class PlaylistList extends LeftSidebarList implements UserListener, PlaylistListener, LoginListener {
 	private static final int MAX_LBL_WIDTH = 170;
 	ImageIcon playlistIcon;
 	PopupMenu popup = new PopupMenu();
 	Log log = LogFactory.getLog(getClass());
 
 	public PlaylistList(LeftSidebar sideBar, RobonoboFrame frame) {
 		super(sideBar, frame, new PlaylistListModel(frame.ctrl));
 		playlistIcon = createImageIcon("/icon/playlist.png", null);
 		setCellRenderer(new CellRenderer());
 		setName("robonobo.playlist.list");
 		setAlignmentX(0.0f);
 		setMaximumSize(new Dimension(65535, 65535));
 		// We do the listener stuff here rather than in the model as we may need to reselect or resize as a consequence
 		frame.ctrl.addUserListener(this);
 		frame.ctrl.addPlaylistListener(this);
 		frame.ctrl.addLoginListener(this);
 		setTransferHandler(new DnDHandler());
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			private void maybeShowPopup(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					int idx = locationToIndex(e.getPoint());
 					if (idx != getSelectedIndex())
 						setSelectedIndex(idx);
 					popup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		});
 	}
 
 	public PlaylistListModel getModel() {
 		return (PlaylistListModel) super.getModel();
 	}
 
 	public void selectPlaylist(final Playlist p) {
 		invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				setSelectedIndex(getModel().getPlaylistIndex(p));
 			}
 		});
 	}
 
 	@Override
 	public void loginSucceeded(User me) {
 		runOnUiThread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				getModel().clear();
 			}
 		});
 	}
 
 	@Override
 	public void loginFailed(String reason) {
 		// Do nothing
 	}
 
 	@Override
 	public void userChanged(final User u) {
 		if (u.getUserId() != frame.ctrl.getMyUser().getUserId())
 			return;
 		runOnUiThread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				Playlist selP = selectedPlaylist();
 				boolean selPGone = false;
 				// Check for removed playlists
 				List<Playlist> toRm = new ArrayList<Playlist>();
 				for (Playlist p : getModel()) {
 					if (!u.getPlaylistIds().contains(p.getPlaylistId()))
 						toRm.add(p);
 				}
 				for (Playlist p : toRm) {
 					if (p.equals(selP))
 						selPGone = true;
 					getModel().remove(p);
 				}
 				// Removing items might have buggered up the selection, so put it back.
 				// If the selected playlist has gone, then go to my library
 				if (selP != null) {
 					if (selPGone)
 						frame.leftSidebar.selectMyMusic();
 					else {
 						int idx = getModel().getPlaylistIndex(selP);
 						setSelectedIndex(idx);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void playlistChanged(final Playlist p) {
 		User me = frame.ctrl.getMyUser();
 		if (p.getOwnerIds().contains(me.getUserId())) {
 			// Don't include special playlists, they're handled separately
 			if(frame.ctrl.isSpecialPlaylist(p.getTitle()))
 				return;
 			Long plId = p.getPlaylistId();
 			Set<Long> myPlIds = frame.ctrl.getMyUser().getPlaylistIds();
 			if (!myPlIds.contains(plId))
 				log.error("Error updating playlist: playlist says it's mine, but my playlist ids do not contain it");
 			runOnUiThread(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					Playlist selP = selectedPlaylist();
 					boolean needReselect = p.equals(selP);
 					getModel().insertSorted(p);
 					if (needReselect) {
 						int idx = getModel().getPlaylistIndex(p);
 						setSelectedIndex(idx);
 					}
 					revalidate();
 				}
 			});
 		}
 	}
 
 	@Override
 	public void gotPlaylistComments(final long plId, boolean anyUnseen, Map<Comment, Boolean> comments) {
 		if (!anyUnseen) {
 			return;
 		}
 		// If this playlist is selected and the comments tab is showing, comments have already been seen
 		Playlist selP = selectedPlaylist();
 		if(selP != null && selP.getPlaylistId() == plId) {
 			ContentPanel cp = frame.mainPanel.getContentPanel("playlist/"+plId);
 			if(cp.tabPane.getSelectedIndex() == 1)
 				return;
 		}
 		runOnUiThread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				final PlaylistListModel m = getModel();
 				if (!m.hasPlaylist(plId)) {
 					return;
 				}
 				m.setHasComments(plId, true);
 			}
 		});
 	}
 
 	public void markPlaylistCommentsAsRead(long plId) {
 		PlaylistListModel m = getModel();
 		m.setHasComments(plId, false);
 	}
 
 	@Override
 	public void userConfigChanged(UserConfig cfg) {
 		// Do nothing
 	}
 
 	@Override
 	protected void itemSelected(int index) {
 		PlaylistListModel m = getModel();
 		final Playlist p = m.getPlaylistAt(index);
 		final long plId = p.getPlaylistId();
 		String pnlName = "playlist/" + plId;
 		frame.mainPanel.selectContentPanel(pnlName);
 		int unseen = m.numUnseen(index);
 		if (unseen > 0) {
 			m.markAllAsSeen(index);
 			frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					frame.ctrl.markAllAsSeen(p);
 				}
 			});
 		}
 		// If the panel's comments tab is showing, mark comments as read
 		ContentPanel cp = frame.mainPanel.getContentPanel(pnlName);
 		if(cp.tabPane.getSelectedIndex() == 1)
 			markPlaylistCommentsAsRead(plId);
 	}
 
 	private Playlist selectedPlaylist() {
 		return (getSelectedIndex() < 0) ? null : getModel().getPlaylistAt(getSelectedIndex());
 	}
 
 	class PopupMenu extends JPopupMenu implements ActionListener {
 		public PopupMenu() {
 			addItem("Post to facebook...", "fb");
 			addItem("Post to twitter...", "twit");
 			addItem("Share...", "share");
 			addItem("Delete", "del");
 		}
 
 		private void addItem(String text, String cmd) {
 			RMenuItem rmi = new RMenuItem(text);
 			rmi.setActionCommand(cmd);
 			rmi.addActionListener(this);
 			add(rmi);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			int selIdx = getSelectedIndex();
 			if (selIdx < 0)
 				return;
 			Playlist p = getModel().getPlaylistAt(selIdx);
 			String action = e.getActionCommand();
 			if (action.equals("fb"))
 				frame.postToFacebook(p);
 			else if (action.equals("twit"))
 				frame.postToTwitter(p);
 			else if (action.equals("share")) {
 				SharePlaylistSheet sps = new SharePlaylistSheet(frame, p);
 				frame.showSheet(sps);
 			} else if (action.equals("del")) {
 				DeletePlaylistSheet dps = new DeletePlaylistSheet(frame, p);
 				frame.showSheet(dps);
 			}
 		}
 	}
 
 	class ItemLbl extends RLabel12 {
 		public ItemLbl() {
 			setOpaque(true);
 			setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
			setMaximumSize(new Dimension(MAX_LBL_WIDTH, 20));
			setPreferredSize(new Dimension(MAX_LBL_WIDTH, 20));
 		}
 	}
 
 	class CellRenderer extends DefaultListRenderer {
 		JLabel lbl = new ItemLbl();
 		Font normalFont, boldFont;
 
 		public CellRenderer() {
 			normalFont = RoboFont.getFont(12, false);
 			boldFont = RoboFont.getFont(12, true);
 		}
 
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 			String text = (String) value;
 			PlaylistListModel m = getModel();
 			int unseen = m.numUnseen(index);
 			Playlist p = m.getPlaylistAt(index);
 			boolean hasCmts = m.hasComments(p.getPlaylistId());
 			boolean useBold = false;
 			boolean useRed = false;
 			if (unseen > 0) {
 				text = text + "[" + unseen + "]";
 				useBold = true;
 			}
 			if (hasCmts) {
 				useBold = true;
 				useRed = true;
 			}
 			if (useBold)
 				lbl.setFont(boldFont);
 			else
 				lbl.setFont(normalFont);
 			lbl.setText(text);
 			lbl.setIcon(playlistIcon);
 			if (isSelected) {
 				lbl.setBackground(LIGHT_GRAY);
 				lbl.setForeground(BLUE_GRAY);
 			} else {
 				lbl.setBackground(MID_GRAY);
 				lbl.setForeground(DARK_GRAY);
 			}
 			if (useRed)
 				lbl.setForeground(GREEN);
 			return lbl;
 		}
 	}
 
 	class DnDHandler extends TransferHandler {
 		@Override
 		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
 			for (DataFlavor dataFlavor : transferFlavors) {
 				if (dataFlavor.equals(StreamTransfer.DATA_FLAVOR))
 					return true;
 			}
 			return Platform.getPlatform().canDnDImport(transferFlavors);
 		}
 
 		@Override
 		public boolean importData(JComponent comp, Transferable t) {
 			Playlist p = getModel().getPlaylistAt(getSelectedIndex());
 			String cpName = "playlist/" + p.getPlaylistId();
 			ContentPanel cp = frame.mainPanel.getContentPanel(cpName);
 			return cp.importData(comp, t);
 		}
 	}
 }
