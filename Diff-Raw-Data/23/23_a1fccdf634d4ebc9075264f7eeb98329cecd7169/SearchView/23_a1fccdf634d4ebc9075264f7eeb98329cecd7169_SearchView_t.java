 package com.quiltplayer.view.swing.views.impl;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.Serializable;
 import java.util.concurrent.TimeoutException;
 
 import javax.annotation.PostConstruct;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.jdesktop.jxlayer.JXLayer;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.quiltplayer.core.repo.spotify.JotifyRepository;
 import com.quiltplayer.model.jotify.JotifyAlbum;
 import com.quiltplayer.model.jotify.JotifyArtist;
 import com.quiltplayer.model.jotify.JotifySong;
 import com.quiltplayer.view.swing.ColorConstantsDark;
 import com.quiltplayer.view.swing.buttons.QButton;
 import com.quiltplayer.view.swing.labels.SpotifyArtistLabel;
 import com.quiltplayer.view.swing.labels.StringOrCharLabel;
 import com.quiltplayer.view.swing.labels.search.AlbumSearchLabel;
 import com.quiltplayer.view.swing.labels.search.TrackSearchLabel;
 import com.quiltplayer.view.swing.layers.JScrollPaneLayerUI;
 import com.quiltplayer.view.swing.listeners.ArtistListener;
 import com.quiltplayer.view.swing.listeners.ChangeAlbumListener;
 import com.quiltplayer.view.swing.listeners.SearchListener;
 import com.quiltplayer.view.swing.panels.QPanel;
 import com.quiltplayer.view.swing.panels.QScrollPane;
 import com.quiltplayer.view.swing.textfields.QTextField;
 import com.quiltplayer.view.swing.views.View;
 import com.quiltplayer.view.swing.window.Keyboard;
 
 import de.felixbruns.jotify.media.Album;
 import de.felixbruns.jotify.media.Artist;
 import de.felixbruns.jotify.media.Result;
 
 /**
  * Search collection + Spotify
  * 
  * @author Vlado Palczynski
  * 
  */
 @org.springframework.stereotype.Component
 public class SearchView implements Serializable, View {
 
     private static final long serialVersionUID = 1L;
 
     public static final String EVENT_SEARCH = "search";
 
     private JPanel panel;
 
     private JPanel searchPanel;
 
     private JPanel scrollablePanel;
 
     private JTextField searchField;
 
     private JButton searchButton;
 
     @Autowired
     private SearchListener searchListener;
 
     @Autowired
     private ArtistListener artistListener;
 
     @Autowired
     private ChangeAlbumListener changeAlbumListener;
 
     @Autowired
     private Keyboard keyboardPanel;
 
     private Result result;
 
     private FocusListener focusListener = new com.quiltplayer.view.swing.listeners.FocusListener();
 
     private boolean newResult;
 
     @PostConstruct
     public void init() {
         setupSearchField();
         setupSearchButton();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.quiltplayer.view.components.View#getUI()
      */
     @Override
     public JComponent getUI() {
         if (newResult || panel == null) {
             panel = new QPanel();
             panel.setOpaque(true);
             panel.addFocusListener(focusListener);
 
             panel.setLayout(new MigLayout("alignx center, top, wrap 1"));
 
             setupSearchBar();
 
            panel.add(searchPanel, "top, alignx center, gapy 0.5cm 0.5cm");
 
             if (result != null) {
                 scrollablePanel = new QPanel(new MigLayout("wrap 3, alignx center, top"));
                 scrollablePanel.setBackground(ColorConstantsDark.BACKGROUND);
                 scrollablePanel.setOpaque(true);
 
                 setupArtists(panel);
                 setupAlbums(panel);
                 addTracks(panel);
 
                 QScrollPane pane = new QScrollPane(scrollablePanel);
                 final JXLayer<JScrollPane> jx = new JXLayer<JScrollPane>(pane,
                         new JScrollPaneLayerUI());
 
                 panel.add(jx, "w 100%");
             }
 
             newResult = false;
         }
 
         searchField.requestFocus();
 
         return panel;
     }
 
     private void setupSearchBar() {
         searchPanel = new JPanel(new MigLayout("insets 0, top"));
         searchPanel.setOpaque(false);
 
         searchPanel.add(searchField, "west, center, w 6cm, gapy 15, " + QTextField.MIG_HEIGHT);
         searchPanel.add(searchButton, "gapy 15");
     }
 
     private void setupSearchField() {
         searchField = new QTextField(true, keyboardPanel);
 
         KeyListener k = new KeyAdapter() {
             /*
              * (non-Javadoc)
              * 
              * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
              */
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     searchListener.actionPerformed(new ActionEvent(searchField.getText(), 0,
                             EVENT_SEARCH));
                 }
             }
 
         };
 
         searchField.addKeyListener(k);
     }
 
     private void setupArtists(JPanel panel) {
         JPanel artists = new QPanel(new MigLayout("insets 0, wrap 1, top, fill"));
         artists.setBackground(ColorConstantsDark.BACKGROUND);
         SpotifyArtistLabel label = null;
 
         artists.add(new StringOrCharLabel(" Artists"), "w 100%, h 0.75cm");
 
         if (!result.getArtists().isEmpty()) {
             for (Artist artist : result.getArtists()) {
                 label = new SpotifyArtistLabel(new JotifyArtist(artist), artistListener);
 
                 artists.add(label, "left, w 100%");
             }
         }
 
         scrollablePanel.add(artists, "top, wmin 10%, w 30%, wmax 30%, alignx center, gapx 1% 1%");
     }
 
     private void setupAlbums(JPanel panel) {
         JPanel albums = new QPanel(new MigLayout("insets 0, top, wrap 1"));
         albums.setBackground(ColorConstantsDark.BACKGROUND);
 
         albums.add(new StringOrCharLabel(" Albums"), "w 100%, h 0.75cm");
 
         if (!result.getAlbums().isEmpty()) {
             for (Album album : result.getAlbums()) {
                 AlbumSearchLabel label;
                 if (album.getTracks().size() <= 0)
                     try {
                         album = JotifyRepository.getInstance().browse(album);
                     }
                     catch (TimeoutException e) {
                         e.printStackTrace();
                     }
                 label = new AlbumSearchLabel(new JotifyAlbum(album));
 
                 label.addActionListener(changeAlbumListener);
                 albums.add(label, "left, w 100%");
             }
         }
 
         scrollablePanel.add(albums, "top, wmin 10%, w 30%, wmax 30%, alignx center");
     }
 
     private void addTracks(JPanel panel) {
         JPanel tracks = new QPanel(new MigLayout("insets 0, wrap 1"));
         tracks.setOpaque(false);
 
         TrackSearchLabel label = null;
 
         tracks.add(new StringOrCharLabel(" Tracks"), "w 100%, h 0.75cm");
 
         if (!result.getTracks().isEmpty()) {
             for (de.felixbruns.jotify.media.Track track : result.getTracks()) {
                 label = new TrackSearchLabel(new JotifySong(track));
                 label.addActionListener(changeAlbumListener);
                 tracks.add(label, "left, w 100%");
             }
         }
 
         scrollablePanel.add(tracks, "top, wmin 10%, w 30%, wmax 30%, alignx center, gapx 1% 1%");
     }
 
     private void setupSearchButton() {
         searchButton = new QButton("Search Spotify");
 
         searchButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 searchListener.actionPerformed(new ActionEvent(searchField.getText(), 0,
                         EVENT_SEARCH));
             }
         });
     }
 
     /**
      * @param result
      *            the result to set
      */
     public final void setResult(Result result) {
         this.result = result;
 
         newResult = true;
     }
 }
