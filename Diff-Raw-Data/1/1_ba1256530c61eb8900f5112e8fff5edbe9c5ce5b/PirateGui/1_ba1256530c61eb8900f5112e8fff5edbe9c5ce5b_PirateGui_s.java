 package com.ags.pirate;
 
 import com.ags.pirate.common.configuration.Configuration;
 import com.ags.pirate.common.model.Serie;
 import com.ags.pirate.common.model.Torrent;
 import com.ags.pirate.gui.event.SerieSelectedEvent;
 import com.ags.pirate.gui.event.TorrentFoundEvent;
 import com.ags.pirate.gui.fal.ColorProvider;
 import com.ags.pirate.gui.info.InfoView;
 import com.ags.pirate.gui.listener.SerieSelectedListener;
 import com.ags.pirate.gui.listener.TorrentFoundListener;
 import com.ags.pirate.gui.serie.SeriesView;
 import com.ags.pirate.gui.torrent.TorrentView;
 import net.miginfocom.swing.MigLayout;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.*;
 import javax.swing.border.EtchedBorder;
 import java.awt.*;
 import java.util.List;
 
 /**
  * Application with Swing as GUI.
  *
  * @author Angel
  * @since 17/11/13
  */
 public class PirateGui {
 
     public static final int WIDTH = 800;
     public static final int HEIGHT = 500;
     private static Logger LOGGER = LoggerFactory.getLogger(PirateGui.class);
 
     private JFrame frame;
     private InfoView infoView;
 
     private SeriesView seriesView;
     private TorrentView torrentView;
 
     private void execute() {
         this.createComponents();
         this.applyLookAndFeel();
         this.createListeners();
         this.displayComponents();
     }
 
 
     private void createComponents() {
         //Create and set up the window.
         frame = new JFrame("PirateBay downloader "+ Configuration.getInstance().getProjectVersion());
         frame.setResizable(false);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         MigLayout layout = new MigLayout("", "[0:0,grow 25,fill]0[0:0,grow 75,fill]", "[]0[]");
         frame.getContentPane().setLayout(layout);
 
         //info panel
         infoView = new InfoView();
         infoView.setMinimumSize(new Dimension(0,30));
         frame.add(infoView, BorderLayout.SOUTH);
 
         //series list
         seriesView = new SeriesView();
         frame.add(seriesView, BorderLayout.WEST);
 
         //torrent list
         torrentView = new TorrentView();
         torrentView.setMinimumSize(new Dimension(WIDTH,HEIGHT));
         frame.add(torrentView, BorderLayout.EAST);
     }
 
     /**
      * this could be moved to the components itself but doing this way we centralize
      * all the feel and look properties.
      */
     private void applyLookAndFeel() {
         /** series list **/
         seriesView.applyFeelAndLook();
         /** info texts **/
         infoView.applyFeelAndLook();
         /** torrent list **/
         torrentView.applyFeelAndLook();
     }
 
 
     private void createListeners() {
         seriesView.setSerieSelectedListener(new SerieSelectedListener() {
             @Override
             public void actionPerformed(final SerieSelectedEvent event) {
                 searchTorrents(event.getSerieSelected());
             }
         });
         torrentView.setTorrentsFoundListener(new TorrentFoundListener() {
             @Override
             public void actionPerformed(TorrentFoundEvent event) {
                 updateInfoPanel(event.getCount());
             }
         });
     }
 
 
     private void displayComponents() {
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 
 
     private void searchTorrents(final Serie serie) {
         seriesView.setEnabled(false);
         Runnable runnable = new Runnable() {
             @Override
             public void run() {
                 try {
                     torrentView.searchTorrent(serie);
                 } finally {
                     frame.pack();
                     seriesView.setEnabled(true);
                 }
             }
         };
         new Thread(runnable).start();
         infoView.updateInfoText(new ImageIcon(getClass().getResource("1-1.gif")), "Searching torrents for " + serie.getTitle());
         frame.pack();
     }
 
     private void updateInfoPanel(int size) {
         infoView.updateInfoText(null, "Found " + size + " torrent(s)");
     }
 
 
     public static void main(String[] args) {
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 new PirateGui().execute();
             }
         });
     }
 }
