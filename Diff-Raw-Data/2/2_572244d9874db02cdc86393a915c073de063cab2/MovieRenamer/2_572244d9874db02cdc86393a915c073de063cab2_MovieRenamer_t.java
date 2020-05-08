 /*
  * Movie Renamer
  * Copyright (C) 2012 Nicolas Magré
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.free.movierenamer.ui;
 
 import com.alee.extended.filechooser.WebFileChooser;
 import com.alee.extended.image.transition.TransitionEffect;
 import com.alee.extended.layout.ToolbarLayout;
 import com.alee.extended.panel.TransitionPanel;
 import com.alee.laf.button.WebButton;
 import com.alee.laf.checkbox.WebCheckBox;
 import com.alee.laf.label.WebLabel;
 import com.alee.laf.list.WebList;
 import com.alee.laf.panel.WebPanel;
 import com.alee.laf.separator.WebSeparator;
 import com.alee.laf.text.WebTextField;
 import com.alee.laf.toolbar.WebToolBar;
 import com.alee.managers.tooltip.TooltipManager;
 import com.alee.managers.tooltip.TooltipWay;
 import fr.free.movierenamer.searchinfo.Media;
 import fr.free.movierenamer.ui.settings.Settings;
 import fr.free.movierenamer.ui.utils.ImageUtils;
 import fr.free.movierenamer.ui.utils.MediaRenamed;
 import fr.free.movierenamer.utils.LocaleUtils;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.List;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.*;
 import javax.swing.JToolBar.Separator;
 
 /**
  * Class MovieRenamer
  *
  * @author Nicolas Magré
  */
 public class MovieRenamer extends JFrame {
   
   private static final long serialVersionUID = 1L;
 
   private Settings setting = Settings.getInstance();
   private LoadingDialog loading;
   private List<MediaRenamed> renamedMediaFile;
   // Current variables
   private MovieRenamerMode currentMode;
   private Media currentMedia;
   // Property change
   private PropertyChangeSupport errorSupport;
   private PropertyChangeSupport settingsChange;
   // Media Panel
   private MoviePanel moviePnl;
   private TvShowPanel tvShowPanel;
   // Media Panel container
   private TransitionPanel containerTransitionMediaPanel;
   // File chooser
   private WebFileChooser fileChooser;
   // List model
   private DefaultListModel mediaFileNameModel;
   private DefaultListModel searchResModel;
   
   // UI tools
   public static final Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
   public static final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
 
   public MovieRenamer() {
 
     // Create media panel container and set transition effect
     containerTransitionMediaPanel = new TransitionPanel(moviePnl);
     containerTransitionMediaPanel.setTransitionEffect(TransitionEffect.fade);
 
     initComponents();
     init();
 
 //    //Add drag and drop listener on mediaList
 //    dropFile = new DropFile(setting, renamedMediaFile, new FileWorkerListener(), MovieRenamer.this);
 //    DropTarget dt = new DropTarget(mediaList, dropFile);
 //    dt.setActive(true);
 
     // Set Movie Renamer mode
     currentMode = MovieRenamerMode.MOVIEMODE;
     movieModeBtn.setEnabled(false);
 
     // Create dummy property change support for close loading dialog on error
     errorSupport = new PropertyChangeSupport(new Object());
     errorSupport.addPropertyChangeListener(new PropertyChangeListener() {
 
       @Override
       public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getPropertyName().equals("closeLoadingDial")) {
           if (loading.isShowing()) {
             loading.dispose();
           }
         }
       }
     });
 
     loadInterface();
 
    setIconImage(ImageUtils.getImageFromJAR("ui/icon-32.png"));
     setLocationRelativeTo(null);
 
     setVisible(true);
 
     // Check for Movie Renamer update
     if (setting.checkUpdate) {
       checkUpdate(false);
     }
   }
  
   private void init() {
 
     // File chooser
 //    if (setting.locale.equals("fr")) {// FIXME add to i18n files, move to main
 //      UIManager.put("WebFileChooser.back", "Précédent");
 //      UIManager.put("WebFileChooser.forward", "Suivant");
 //      UIManager.put("WebFileChooser.folderup", "Remonte d'un niveau");
 //      UIManager.put("WebFileChooser.home", "Répertoire d'accueil");
 //      UIManager.put("WebFileChooser.refresh", "Rafraichir");
 //      UIManager.put("WebFileChooser.newfolder", "Crée un nouveau dossier");
 //      UIManager.put("WebFileChooser.delete", "Supprimer");
 //      UIManager.put("WebFileChooser.files.selected", "Fichiers sélectionnés");
 //      UIManager.put("WebFileChooser.cancel", "Annuler");
 //      UIManager.put("WebFileChooser.view", "Changer de vue");
 //      UIManager.put("WebFileChooser.view.tiles", "Détails");
 //      UIManager.put("WebFileChooser.choose", "Ouvrir");
 //    } else {
 //      UIManager.put("WebFileChooser.choose", "Open");
 //    }
 
     // Add button to main toolbar on right
     mainTb.addToEnd(helpBtn);
     mainTb.addToEnd(new JSeparator(JSeparator.VERTICAL));
     mainTb.addToEnd(updateBtn);
     mainTb.addToEnd(settingBtn);
     mainTb.addToEnd(exitBtn);
 
     // Add tooltip 
     TooltipManager.setTooltip(openBtn, openTooltipLbl, TooltipWay.down);
     //TooltipManager.setTooltip(editBtn, new JLabel(LocaleUtils.i18n("edit"), new ImageIcon(getClass().getResource("/image/accessories-text-editor-6-24.png")), SwingConstants.TRAILING), TooltipWay.down);
     TooltipManager.setTooltip(movieModeBtn, new JLabel(LocaleUtils.i18n("movieMode"), ImageUtils.getIconFromJar("ui/movie.png"), SwingConstants.TRAILING), TooltipWay.down);
     TooltipManager.setTooltip(tvShowModeBtn, new JLabel(LocaleUtils.i18n("tvshowMode"), ImageUtils.getIconFromJar("ui/tv.png"), SwingConstants.TRAILING), TooltipWay.down);
     TooltipManager.setTooltip(helpBtn, new JLabel(LocaleUtils.i18n("help"), ImageUtils.getIconFromJar("ui/system-help-3.png"), SwingConstants.TRAILING), TooltipWay.down);
     TooltipManager.setTooltip(updateBtn, new JLabel(LocaleUtils.i18n("updateBtn"), ImageUtils.getIconFromJar("ui/system-software-update-5.png"), SwingConstants.TRAILING), TooltipWay.down);
     TooltipManager.setTooltip(settingBtn, new JLabel(LocaleUtils.i18n("settingBtn"), ImageUtils.getIconFromJar("ui/system-settings.png"), SwingConstants.TRAILING), TooltipWay.down);
     TooltipManager.setTooltip(exitBtn, new JLabel(LocaleUtils.i18n("exitBtn"), ImageUtils.getIconFromJar("ui/application-exit.png"), SwingConstants.TRAILING), TooltipWay.down);
     TooltipManager.setTooltip(searchBtn, new JLabel(LocaleUtils.i18n("search"), ImageUtils.getIconFromJar("ui/search.png"), SwingConstants.TRAILING), TooltipWay.down);
     TooltipManager.setTooltip(renameBtn, new JLabel(LocaleUtils.i18n("rename"), ImageUtils.getIconFromJar("ui/dialog-ok-2.png"), SwingConstants.TRAILING), TooltipWay.down);
 
     // Add media panel container to media split pane
     MediaSp.setBottomComponent(containerTransitionMediaPanel);
     fileFormatField.setText(setting.movieFilenameFormat);
   }
 
   private void loadInterface() {// TODO 
 
     setTitle(Settings.APPNAME + "-" + setting.getVersion() + " " + currentMode.getTitleMode());
   }
 
   /**
    * Clear Movie Renamer interface
    *
    * @param mediaList Clear media list
    * @param searchList Clear search list
    */
   private void clearInterface(boolean mediaList, boolean searchList) { // TODO 
 
   }
 
   /**
    * Check if media type is supported by current mode and ask user on what to do.
    *
    * @return True if current mode support media type, false otherwise
    */
   private boolean checkMediaTypeInCurrentMode() {// TODO 
 
     return true;
   }
 
   /**
    * Search media on web
    */
   private void searchMedia() {// TODO 
 
   }
 
   /**
    * Check for Movie Renamer update
    *
    * @param showAlready Show dialog
    */
   public final void checkUpdate(boolean showAlready) {// TODO
 
   }
 
   /**
    * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         updateBtn = new WebButton();
         settingBtn = new WebButton();
         exitBtn = new WebButton();
         helpBtn = new WebButton();
         openTooltipLbl = new JLabel(new ImageIcon(getClass().getResource("/image/ui/folder-video.png")), SwingConstants.TRAILING);
         renameField = new JTextField();
         thumbChk = new WebCheckBox();
         fanartChk = new WebCheckBox();
         nfoChk = new WebCheckBox();
         mainTb = new WebToolBar();
         openBtn = new WebButton();
         openSep = new WebSeparator();
         movieModeBtn = new WebButton();
         tvShowModeBtn = new WebButton();
         modeSep = new Separator();
         jLabel1 = new JLabel();
         fileFormatField = new WebTextField();
         renameTb = new WebToolBar();
         renameBtn = new WebButton();
         centerPnl = new WebPanel();
         centerSp = new JSplitPane();
         MediaSp = new JSplitPane();
         searchPnl = new WebPanel();
         searchScroll = new JScrollPane();
         searchResultList = new WebList();
         searchTb = new WebToolBar();
         searchLbl = new WebLabel();
         searchBtn = new WebButton();
         searchField = new WebTextField();
         mediaPnl = new WebPanel();
         mediScroll = new JScrollPane();
         mediaList = new WebList();
         mediaTb = new WebToolBar();
         mediaLbl = new WebLabel();
 
         updateBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/system-software-update-5.png")));         updateBtn.setFocusable(false);
         updateBtn.setHorizontalTextPosition(SwingConstants.CENTER);
         updateBtn.setRolloverDarkBorderOnly(true);
         updateBtn.setRolloverDecoratedOnly(true);
         updateBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
         updateBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 updateBtnActionPerformed(evt);
             }
         });
 
         settingBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/system-settings.png")));         settingBtn.setFocusable(false);
         settingBtn.setHorizontalTextPosition(SwingConstants.CENTER);
         settingBtn.setRolloverDarkBorderOnly(true);
         settingBtn.setRolloverDecoratedOnly(true);
         settingBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
         settingBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 settingBtnActionPerformed(evt);
             }
         });
 
         exitBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/application-exit.png")));         exitBtn.setFocusable(false);
         exitBtn.setHorizontalTextPosition(SwingConstants.CENTER);
         exitBtn.setRolloverDarkBorderOnly(true);
         exitBtn.setRolloverDecoratedOnly(true);
         exitBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
         exitBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 exitBtnActionPerformed(evt);
             }
         });
 
         helpBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/system-help-3.png")));         helpBtn.setFocusable(false);
         helpBtn.setHorizontalTextPosition(SwingConstants.CENTER);
         helpBtn.setRolloverDarkBorderOnly(true);
         helpBtn.setRolloverDecoratedOnly(true);
         helpBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
         helpBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 helpBtnActionPerformed(evt);
             }
         });
 
         openTooltipLbl.setText(LocaleUtils.i18n("openFolderBtn")); 
         renameField.setEnabled(false);
 
         thumbChk.setText(LocaleUtils.i18n("thumb"));         thumbChk.setFocusable(false);
 
         fanartChk.setText(LocaleUtils.i18n("fanart"));         fanartChk.setFocusable(false);
 
         nfoChk.setText(LocaleUtils.i18n("xbmcNfo"));         nfoChk.setFocusable(false);
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setMinimumSize(new Dimension(770, 570));
 
         mainTb.setFloatable(false);
         mainTb.setRollover(true);
         mainTb.setRound(10);
 
         openBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/folder-video.png")));         openBtn.setToolTipText(LocaleUtils.i18n("openFolderBtn"));         openBtn.setFocusable(false);
         openBtn.setHorizontalTextPosition(SwingConstants.CENTER);
         openBtn.setRolloverDarkBorderOnly(true);
         openBtn.setRolloverDecoratedOnly(true);
         openBtn.setRolloverShadeOnly(true);
         openBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
         openBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 openBtnActionPerformed(evt);
             }
         });
         mainTb.add(openBtn);
         mainTb.add(openSep);
 
         movieModeBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/movie.png")));         movieModeBtn.setToolTipText(LocaleUtils.i18n("movieMode"));         movieModeBtn.setFocusable(false);
         movieModeBtn.setHorizontalTextPosition(SwingConstants.CENTER);
         movieModeBtn.setRolloverDarkBorderOnly(true);
         movieModeBtn.setRolloverDecoratedOnly(true);
         movieModeBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
         movieModeBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 movieModeBtnActionPerformed(evt);
             }
         });
         mainTb.add(movieModeBtn);
 
         tvShowModeBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/tv.png")));         tvShowModeBtn.setToolTipText(LocaleUtils.i18n("tvshowMode"));         tvShowModeBtn.setFocusable(false);
         tvShowModeBtn.setHorizontalTextPosition(SwingConstants.CENTER);
         tvShowModeBtn.setRolloverDarkBorderOnly(true);
         tvShowModeBtn.setRolloverDecoratedOnly(true);
         tvShowModeBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
         tvShowModeBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 tvShowModeBtnActionPerformed(evt);
             }
         });
         mainTb.add(tvShowModeBtn);
         mainTb.add(modeSep);
 
         jLabel1.setFont(new Font("Ubuntu", 1, 13));         jLabel1.setText(LocaleUtils.i18n("mediaFileFormat"));         mainTb.add(jLabel1);
 
         fileFormatField.setPreferredSize(new Dimension(250, 27));
         fileFormatField.addKeyListener(new KeyAdapter() {
             public void keyReleased(KeyEvent evt) {
                 fileFormatFieldKeyReleased(evt);
             }
         });
         mainTb.add(fileFormatField);
 
         getContentPane().add(mainTb, BorderLayout.PAGE_START);
 
         renameTb.setFloatable(false);
         renameTb.setRollover(true);
         renameTb.setRound(10);
 
         renameBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/dialog-ok-2.png")));         renameBtn.setText(LocaleUtils.i18n("rename"));         renameBtn.setEnabled(false);
         renameBtn.setFocusable(false);
         renameBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 renameBtnActionPerformed(evt);
             }
         });
         renameTb.add(renameBtn);
         //Add rename text field
         renameTb.add(renameField, ToolbarLayout.FILL);
         renameTb.add(thumbChk, ToolbarLayout.END);
         renameTb.add(fanartChk, ToolbarLayout.END);
         renameTb.add(nfoChk, ToolbarLayout.END);
 
         getContentPane().add(renameTb, BorderLayout.PAGE_END);
 
         centerPnl.setMargin(new Insets(1, 1, 1, 1));
         centerPnl.setShadeWidth(2);
 
         centerSp.setDividerLocation(300);
 
         MediaSp.setDividerLocation(170);
         MediaSp.setOrientation(JSplitPane.VERTICAL_SPLIT);
 
         searchPnl.setMargin(new Insets(10, 10, 10, 10));
 
         searchResultList.setFont(new Font("Dialog", 0, 12));         searchScroll.setViewportView(searchResultList);
 
         searchTb.setFloatable(false);
         searchTb.setRollover(true);
         searchTb.setRound(0);
 
         searchLbl.setText(LocaleUtils.i18n("search"));         searchLbl.setFont(new Font("Ubuntu", 1, 14));         searchTb.add(searchLbl);
 
         searchBtn.setIcon(new ImageIcon(getClass().getResource("/image/ui/search.png")));         searchBtn.setEnabled(false);
         searchBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 searchBtnActionPerformed(evt);
             }
         });
 
         searchField.setEnabled(false);
         searchField.addKeyListener(new KeyAdapter() {
             public void keyReleased(KeyEvent evt) {
                 searchFieldKeyReleased(evt);
             }
         });
 
         GroupLayout searchPnlLayout = new GroupLayout(searchPnl);
         searchPnl.setLayout(searchPnlLayout);
         searchPnlLayout.setHorizontalGroup(
             searchPnlLayout.createParallelGroup(Alignment.LEADING)
             .addComponent(searchTb, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(searchPnlLayout.createSequentialGroup()
                 .addComponent(searchField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(searchBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
             .addComponent(searchScroll, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
         );
         searchPnlLayout.setVerticalGroup(
             searchPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(searchPnlLayout.createSequentialGroup()
                 .addComponent(searchTb, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(searchPnlLayout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(searchField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(searchBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(searchScroll, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
         );
 
         searchField.setLeadingComponent(new JLabel(ImageUtils.getIconFromJar("ui/search.png")));
 
         MediaSp.setLeftComponent(searchPnl);
 
         centerSp.setRightComponent(MediaSp);
 
         mediaPnl.setMargin(new Insets(10, 10, 10, 10));
         mediaPnl.setMinimumSize(new Dimension(60, 0));
 
         mediaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         mediScroll.setViewportView(mediaList);
 
         mediaTb.setFloatable(false);
         mediaTb.setRollover(true);
         mediaTb.setRound(0);
 
         mediaLbl.setText(LocaleUtils.i18n("media"));         mediaLbl.setFont(new Font("Ubuntu", 1, 14));         mediaTb.add(mediaLbl);
 
         GroupLayout mediaPnlLayout = new GroupLayout(mediaPnl);
         mediaPnl.setLayout(mediaPnlLayout);
         mediaPnlLayout.setHorizontalGroup(
             mediaPnlLayout.createParallelGroup(Alignment.LEADING)
             .addComponent(mediaTb, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
             .addComponent(mediScroll)
         );
         mediaPnlLayout.setVerticalGroup(
             mediaPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(mediaPnlLayout.createSequentialGroup()
                 .addComponent(mediaTb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(mediScroll, GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE))
         );
 
         centerSp.setLeftComponent(mediaPnl);
 
         GroupLayout centerPnlLayout = new GroupLayout(centerPnl);
         centerPnl.setLayout(centerPnlLayout);
         centerPnlLayout.setHorizontalGroup(
             centerPnlLayout.createParallelGroup(Alignment.LEADING)
             .addComponent(centerSp)
         );
         centerPnlLayout.setVerticalGroup(
             centerPnlLayout.createParallelGroup(Alignment.LEADING)
             .addComponent(centerSp)
         );
 
         getContentPane().add(centerPnl, BorderLayout.CENTER);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
   private void openBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_openBtnActionPerformed
 
   }//GEN-LAST:event_openBtnActionPerformed
 
   private void movieModeBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_movieModeBtnActionPerformed
 
   }//GEN-LAST:event_movieModeBtnActionPerformed
 
   private void tvShowModeBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_tvShowModeBtnActionPerformed
 
   }//GEN-LAST:event_tvShowModeBtnActionPerformed
 
   private void exitBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_exitBtnActionPerformed
     System.exit(0);
   }//GEN-LAST:event_exitBtnActionPerformed
 
   private void settingBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_settingBtnActionPerformed
     final Setting set = new Setting(setting, settingsChange, this);
     java.awt.EventQueue.invokeLater(new Runnable() {
 
       @Override
       public void run() {
         set.setVisible(true);
       }
     });  }//GEN-LAST:event_settingBtnActionPerformed
 
   private void updateBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
     checkUpdate(true);
   }//GEN-LAST:event_updateBtnActionPerformed
 
   private void helpBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_helpBtnActionPerformed
     TooltipManager.showOneTimeTooltip(mediaList, new Point(mediaList.getWidth() / 2, mediaList.getHeight() / 2), "Media list help", TooltipWay.up);
     TooltipManager.showOneTimeTooltip(searchResultList, new Point(searchResultList.getWidth() / 2, searchResultList.getHeight() / 2), "searchResultList list help", TooltipWay.up);
     TooltipManager.showOneTimeTooltip(openBtn, new Point(openBtn.getWidth() / 2, openBtn.getHeight()), openTooltipLbl, TooltipWay.down);
     TooltipManager.showOneTimeTooltip(fileFormatField, new Point(fileFormatField.getWidth() / 2, fileFormatField.getHeight()), "Change filename on the fly", TooltipWay.down);
   }//GEN-LAST:event_helpBtnActionPerformed
 
   private void renameBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_renameBtnActionPerformed
     
   }//GEN-LAST:event_renameBtnActionPerformed
 
   private void searchBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
   
   }//GEN-LAST:event_searchBtnActionPerformed
 
   private void searchFieldKeyReleased(KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
     if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
       searchBtnActionPerformed(null);
     }
   }//GEN-LAST:event_searchFieldKeyReleased
 
   private void fileFormatFieldKeyReleased(KeyEvent evt) {//GEN-FIRST:event_fileFormatFieldKeyReleased
 
   }//GEN-LAST:event_fileFormatFieldKeyReleased
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JSplitPane MediaSp;
     private WebPanel centerPnl;
     private JSplitPane centerSp;
     private WebButton exitBtn;
     private WebCheckBox fanartChk;
     private WebTextField fileFormatField;
     private WebButton helpBtn;
     private JLabel jLabel1;
     private WebToolBar mainTb;
     private JScrollPane mediScroll;
     private WebLabel mediaLbl;
     private WebList mediaList;
     private WebPanel mediaPnl;
     private WebToolBar mediaTb;
     private Separator modeSep;
     private WebButton movieModeBtn;
     private WebCheckBox nfoChk;
     private WebButton openBtn;
     private WebSeparator openSep;
     private JLabel openTooltipLbl;
     private WebButton renameBtn;
     private JTextField renameField;
     private WebToolBar renameTb;
     private WebButton searchBtn;
     private WebTextField searchField;
     private WebLabel searchLbl;
     private WebPanel searchPnl;
     private JList searchResultList;
     private JScrollPane searchScroll;
     private WebToolBar searchTb;
     private WebButton settingBtn;
     private WebCheckBox thumbChk;
     private WebButton tvShowModeBtn;
     private WebButton updateBtn;
     // End of variables declaration//GEN-END:variables
 }
