 package com.quiltplayer.view.swing.panels.playlistpanels;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 
 import javax.annotation.PostConstruct;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.quiltplayer.controller.PlayerListener;
 import com.quiltplayer.model.Album;
 import com.quiltplayer.model.impl.NullAlbum;
 import com.quiltplayer.view.swing.ColorConstantsDark;
 import com.quiltplayer.view.swing.buttons.QSongButton;
 import com.quiltplayer.view.swing.effects.CrossFader;
 import com.quiltplayer.view.swing.panels.AlbumPresentationPanel;
 import com.quiltplayer.view.swing.panels.components.SongsComponent;
 
 /**
  * Represents the playlist panel. One Panel will give you information about the album, tracks and so
  * forth. The other will give you information regarding the artist.
  * 
  * @author Vlado Palczynski
  */
 @org.springframework.stereotype.Component
 public class AlbumPlaylistPanel extends JPanel {
 
     private Logger log = Logger.getLogger(AlbumPlaylistPanel.class);
 
     private static final long serialVersionUID = 1L;
 
     private QSongButton currentSongLabel;
 
     private SongsComponent songsComponent;
 
     @Autowired
     private AlbumPresentationPanel albumPresentationPanel;
 
     @Autowired
     private PlayerListener playerListener;
 
     private transient Album album;
 
     @Autowired
     private CrossFader crossFader;
 
     public AlbumPlaylistPanel() {
         super(new MigLayout("debug, insets 0, wrap 1, fill"));
         setOpaque(true);
 
         setBackground(ColorConstantsDark.PLAYLIST_BACKGROUND);
     }
 
     @PostConstruct
     public void init() {
         this.album = new NullAlbum();
 
         add(albumPresentationPanel, "north, gapy 0.5cm 0.3cm");
         add(crossFader, "north, h 45%");
     }
 
     public void changeAlbum(final Album album) {
         log.debug("Changing album...");
         this.album = album;
 
         crossFader.setImages(album.getImages());
 
         albumPresentationPanel.update(album);
 
         setupSongsPanel();
 
         repaint();
 
         log.debug("Album is changed...");
     }
 
     private void setupSongsPanel() {
         if (songsComponent != null)
             remove(songsComponent);
 
         songsComponent = new SongsComponent(album, playerListener);
 
        add(songsComponent, "north");
 
         songsComponent.repaint();
     }
 
     /*
      * @see javax.swing.JPanel#updateUI()
      */
     public void updateAlbumUI() {
         // setupImageControlPanel(true);
     }
 
     public Component[] getSongLabels() {
         return songsComponent.getComponents();
     }
 
     /**
      * @param currentSongLabel
      *            the currentSongLabel to set
      */
     public void setCurrentSongLabel(final QSongButton currentSongLabel) {
         this.currentSongLabel = currentSongLabel;
     }
 
     /**
      * @param currentSongLabel
      *            the currentSongLabel to set
      */
     public void inactivateCurrentSongLabel() {
         if (currentSongLabel != null) {
             currentSongLabel.setInactive();
         }
     }
 
     /**
      * @return the currentSongLabel
      */
     public QSongButton getCurrentSongLabel() {
         return currentSongLabel;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.swing.JComponent#paint(java.awt.Graphics)
      */
     @Override
     public void paint(Graphics g) {
         Graphics2D g2d = (Graphics2D) g;
 
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         super.paint(g);
     }
 }
