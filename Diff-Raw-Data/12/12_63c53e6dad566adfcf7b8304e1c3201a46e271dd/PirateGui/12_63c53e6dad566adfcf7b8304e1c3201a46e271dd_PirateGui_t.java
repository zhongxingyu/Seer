 package com.ags.pirate;
 
 import com.ags.pirate.configuration.Configuration;
 import com.ags.pirate.event.SerieSelectedEvent;
 import com.ags.pirate.event.TorrentSelectedEvent;
 import com.ags.pirate.gui.SeriesList;
 import com.ags.pirate.gui.TorrentTable;
 import com.ags.pirate.gui.fal.ColorProvider;
 import com.ags.pirate.gui.fal.ScrollBar;
 import com.ags.pirate.listener.SerieSelectedListener;
 import com.ags.pirate.listener.TorrentSelectedListener;
 import com.ags.pirate.model.BeanItemContainer;
 import com.ags.pirate.model.BeanItemTableModel;
 import com.ags.pirate.model.Serie;
 import com.ags.pirate.model.Torrent;
 import com.ags.pirate.service.PirateService;
 import net.miginfocom.swing.MigLayout;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.*;
 import javax.swing.border.EtchedBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
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
     public static final int MAX_WIDTH_SEEDS = 50;
     public static final int MAX_WIDTH_LEECHERS = 60;
     public static final int ROW_HEIGHT = 33;
     private static Logger LOGGER = LoggerFactory.getLogger(Pirate.class);
 
     private PirateService pirateService;
     private JFrame frame;
     private SeriesList seriesAvailable;
     private JPanel infoPanel;
     private TorrentTable torrentTable;
     private JButton searchButton;
     private JPanel seriesPanel;
     private JTextArea searchingText;
     private JTextArea foundTorrentsText;
     private JScrollPane torrentPanel;
 
     private void execute() {
         this.pirateService = new PirateService();
         this.createComponents();
         this.applyLookAndFeel();
         this.createListeners();
         this.displayComponents();
     }
 
 
     private void createComponents() {
         //Create and set up the window.
         frame = new JFrame("PirateBay downloader");
        frame.setResizable(false);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MigLayout layout = new MigLayout("", "[0:0,grow 25,fill]0[0:0,grow 75,fill]", "[]0[]");
         frame.getContentPane().setLayout(layout);
 
         //info panel
         infoPanel = new JPanel();
         infoPanel.setMinimumSize(new Dimension(0,30));
         frame.add(infoPanel, BorderLayout.SOUTH);
 
         //series list
         List<Serie> series = pirateService.getSeries();
         seriesAvailable = new SeriesList(series.toArray(new Serie[series.size()]));
         searchButton = new JButton("Custom search");
         seriesPanel = new JPanel();
         seriesPanel.setLayout(new MigLayout());
         frame.add(seriesPanel, BorderLayout.WEST);
         seriesPanel.add(seriesAvailable, "wrap");
         seriesPanel.add(searchButton);
 
         //torrent list
         torrentTable = new TorrentTable();
         torrentTable.setAutoCreateRowSorter(true);
         torrentPanel = new JScrollPane(torrentTable);
        torrentPanel.setMinimumSize(new Dimension(WIDTH,HEIGHT));
         frame.add(torrentPanel, BorderLayout.EAST);
 
         //info texts
         searchingText = new JTextArea();
         foundTorrentsText = new JTextArea();
 
     }
 
     /**
      * this could be moved to the components itself but doing this way we centralize
      * all the feel and look properties.
      */
     private void applyLookAndFeel() {
         seriesPanel.setBackground(ColorProvider.getMainBackgroundColor());
 //        seriesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 
         /** series **/
         seriesAvailable.setBackground(ColorProvider.getMainBackgroundColor());
         seriesAvailable.setForeground(ColorProvider.getMainFontColor());
         seriesAvailable.setSelectionBackground(ColorProvider.getMainSelectedColor());
         seriesAvailable.setSelectionForeground(ColorProvider.getMainFontColor());
 
 
         /** search button **/
         searchButton.setBackground(ColorProvider.getSecondaryBackgroundColor());
         searchButton.setForeground(ColorProvider.getSecondaryFontColor());
         searchButton.setFocusPainted(false);
 
         /** info botton panel **/
         infoPanel.setBackground(ColorProvider.getMainBackgroundColor());
         infoPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 
         /** info texts **/
         searchingText.setBackground(ColorProvider.getMainBackgroundColor());
         searchingText.setForeground(ColorProvider.getMainFontColor());
         foundTorrentsText.setBackground(ColorProvider.getMainBackgroundColor());
         foundTorrentsText.setForeground(ColorProvider.getMainFontColor());
 
         /** torrent list **/
         torrentTable.setBackground(ColorProvider.getSecondaryBackgroundColor());
         torrentTable.setForeground(ColorProvider.getSecondaryFontColor());
         torrentTable.getTableHeader().setBackground(ColorProvider.getMainBackgroundColor());
         torrentTable.getTableHeader().setForeground(ColorProvider.getMainFontColor());
         torrentPanel.setBackground(ColorProvider.getMainBackgroundColor());
         torrentPanel.getVerticalScrollBar().setUI(new ScrollBar());
         torrentPanel.getViewport().setBackground(ColorProvider.getSecondaryBackgroundColor());
         torrentPanel.setBorder(BorderFactory.createEmptyBorder());
     }
 
 
     private void createListeners() {
         searchButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String input = JOptionPane.showInputDialog(frame, "Search torrents");
                 if (input != null && !input.trim().equals("")) {
                     searchTorrents(new Serie(input, "", ""));
                 }
             }
         });
 
         seriesAvailable.setSerieSelectedListener(new SerieSelectedListener() {
             @Override
             public void actionPerformed(final SerieSelectedEvent event) {
                 searchTorrents(event.getSerieSelected());
             }
         });
     }
 
 
     private void displayComponents() {
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 
     private BeanItemTableModel createTorrentBeanItemTableModel(List<Torrent> torrents) {
         BeanItemContainer<Torrent> beanItemContainer = new BeanItemContainer<Torrent>(torrents);
         BeanItemTableModel tableModel = new BeanItemTableModel(beanItemContainer);
         tableModel.setVisibleColumns(new String[]{"info", "seeds", "leechers"});
         tableModel.setColumnHeader("info", "Torrent's name");
         return tableModel;
     }
 
     private void searchTorrents(final Serie serie) {
         //different thread to update the UI
         seriesAvailable.setEnabled(false);
         Runnable runnable = new Runnable() {
             @Override
             public void run() {
                 try {
                     List<Torrent> torrents = pirateService.getTorrents(serie);
                     if (torrents.size() == 0) {
                         //TODO add alert message
                         LOGGER.warn("no torrents found!");
                     }
                     populateTorrents(torrents);
                 } finally {
                     frame.pack();
                     seriesAvailable.setEnabled(true);
                 }
             }
         };
         new Thread(runnable).start();
 
         infoPanel.removeAll();
         infoPanel.add(new JLabel(new ImageIcon(getClass().getResource("1-1.gif"))));
         searchingText.setText("Searching torrents for " + serie.getTitle());
         infoPanel.add(searchingText);
         frame.pack();
     }
 
     private void populateTorrents(List<Torrent> torrents) {
         infoPanel.removeAll();
         foundTorrentsText.setText("Found " + torrents.size() + " torrent(s)");
         infoPanel.add(foundTorrentsText);
         torrentTable.setTorrentSelectedListener(new TorrentSelectedListener() {
             @Override
             public void actionPerformed(TorrentSelectedEvent event) {
                 pirateService.downloadTorrent(event.getTorrentSelected(), Configuration.getInstance().getUtorrent());
             }
         });
         BeanItemTableModel tableModel = createTorrentBeanItemTableModel(torrents);
         torrentTable.setModel(tableModel);
         torrentTable.getColumn("seeds").setMaxWidth(MAX_WIDTH_SEEDS);
         torrentTable.getColumn("leechers").setMaxWidth(MAX_WIDTH_LEECHERS);
         torrentTable.setRowHeight(ROW_HEIGHT);
     }
 
 
     public static void main(String[] args) {
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 new PirateGui().execute();
             }
         });
     }
 }
