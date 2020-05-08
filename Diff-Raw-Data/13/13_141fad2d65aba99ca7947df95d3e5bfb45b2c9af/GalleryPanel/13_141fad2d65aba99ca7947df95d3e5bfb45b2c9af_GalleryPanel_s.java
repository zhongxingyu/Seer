 /*
  * Copyright (C) 2012-2013 Nicolas Magré
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
 package fr.free.movierenamer.ui.panel;
 
 import fr.free.movierenamer.info.ImageInfo.ImageCategoryProperty;
 import fr.free.movierenamer.info.ImageInfo.ImageSize;
 import fr.free.movierenamer.ui.MovieRenamer;
 import fr.free.movierenamer.ui.bean.UIImageLang;
 import fr.free.movierenamer.ui.bean.UIMediaImage;
 import fr.free.movierenamer.ui.settings.UISettings;
 import fr.free.movierenamer.ui.swing.SpinningDial;
 import fr.free.movierenamer.ui.utils.ImageUtils;
 import fr.free.movierenamer.ui.utils.UIUtils;
 import fr.free.movierenamer.ui.worker.WorkerManager;
 import fr.free.movierenamer.ui.worker.impl.GalleryWorker;
 import fr.free.movierenamer.ui.worker.impl.ImageWorker;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import javax.swing.SwingWorker;
 
 /**
  * Class GalleryPanel
  *
  * @author Nicolas Magré
  */
 public class GalleryPanel extends JDialog {
 
   private final static int LOADING_WIDTH = 32;
   private final static int THUMB_WIDTH = 100;
   private final static int THUMB_HEIGHT = 150;
   private static final long serialVersionUID = 1L;
   private final CustomWebImageGallery thumbGallery;
   private List<UIMediaImage> images;
   private List<UIImageLang> languages;
   private final DefaultComboBoxModel languagesModel;
   private final ImageCategoryProperty property;
   private final MovieRenamer mr;
   private final PropertyChangeSupport propertyChange;
   private final SpinningDial loadingGallery;
   private final SpinningDial loadingPreview;
   private GalleryWorker gworker;
   private final ItemListener languageActionPerformed;
   private final ImageCategorySize ics;
 
   private enum ImageCategorySize {
 
     actor(new Dimension(THUMB_WIDTH, THUMB_HEIGHT), new Dimension(THUMB_WIDTH * 2, THUMB_HEIGHT * 2)),
     thumb(new Dimension(THUMB_WIDTH, THUMB_HEIGHT), new Dimension(THUMB_WIDTH * 2, THUMB_HEIGHT * 2)),
     fanart(new Dimension(THUMB_HEIGHT, THUMB_WIDTH), new Dimension(THUMB_HEIGHT * 2, THUMB_WIDTH * 2)),
     logo(new Dimension(THUMB_HEIGHT, THUMB_WIDTH), new Dimension(THUMB_HEIGHT * 2, THUMB_WIDTH * 2)),
     banner(new Dimension(THUMB_HEIGHT, THUMB_WIDTH), new Dimension(THUMB_HEIGHT * 2, THUMB_WIDTH * 2)),
     cdart(new Dimension(THUMB_HEIGHT, THUMB_HEIGHT), new Dimension(THUMB_HEIGHT * 2, THUMB_HEIGHT * 2)),
     clearart(new Dimension(THUMB_HEIGHT, THUMB_WIDTH), new Dimension(THUMB_HEIGHT * 2, THUMB_WIDTH * 2));
     private final Dimension gallery, preview;
     private final SpinningDial loadingGallery;
     private final SpinningDial loadingPreview;
 
     private ImageCategorySize(Dimension gallery, Dimension preview) {
       this.gallery = gallery;
       this.preview = preview;
       this.loadingGallery = new SpinningDial(LOADING_WIDTH, gallery.height);
       this.loadingPreview = new SpinningDial(LOADING_WIDTH, preview.height);
     }
 
     /**
      * @return the gallery
      */
     public Dimension getGalleryDim() {
       return gallery;
     }
 
     /**
      * @return the preview
      */
     public Dimension getPreviewDim() {
       return preview;
     }
 
     /**
      * @return the loadingGallery
      */
     public SpinningDial getLoadingGallery() {
       return loadingGallery;
     }
 
     /**
      * @return the loadingPreview
      */
     public SpinningDial getLoadingPreview() {
       return loadingPreview;
     }
   }
 
   /**
    * Creates new form GalleryPanel
    *
    * @param mr
    * @param property
    */
   public GalleryPanel(MovieRenamer mr, ImageCategoryProperty property) {
     this.mr = mr;
     this.property = property;
 
     ics = ImageCategorySize.valueOf(ImageCategorySize.class, property.name());
 
     loadingGallery = ics.getLoadingGallery();
     loadingPreview = ics.getLoadingPreview();
 
     initComponents();
 
     propertyChange = new PropertyChangeSupport(previewLbl);
     languages = new ArrayList<UIImageLang>();
     languagesModel = new DefaultComboBoxModel();
     languageCbb.setModel(languagesModel);
     languageCbb.setRenderer(UIUtils.iconListRenderer);
 
     boolean useLanguage = false;
     switch (property) {
       case thumb:
       case banner:
       case cdart:
       case logo:
         useLanguage = true;
         break;
       case actor:
       case clearart:
       case fanart:
       case unknown:
         useLanguage = false;
         break;
     }
     previewLbl.setPreferredSize(ics.getPreviewDim());
 
     languageCbb.setVisible(useLanguage);
     languageActionPerformed = new ItemListener() {
       @Override
       public void itemStateChanged(ItemEvent event) {
         if (event.getStateChange() == ItemEvent.SELECTED && languagesModel.getSize() > 0) {
           Locale locale = ((UIImageLang) languagesModel.getSelectedItem()).getLang();
           thumbGallery.removeAllImages();
           previewLbl.setIcon(null);
           List<UIMediaImage> imageslang = getImageByLanguage(images, locale.getCountry());
           thumbGallery.addImages(imageslang);
           getTumbPreview(imageslang);
         }
       }
     };
     languageCbb.addItemListener(languageActionPerformed);
 
     thumbGallery = new CustomWebImageGallery(useLanguage, ics.getGalleryDim());
     thumbGalleryPnl.setLayout(new BorderLayout());
     thumbGallery.setOpaque(false);
     images = new ArrayList<UIMediaImage>();
 
     PropertyChangeSupport changePreview = thumbGallery.getPropertyChange();
     changePreview.addPropertyChangeListener(new PropertyChangeListener() {
       @Override
       public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getPropertyName().equals("selectedImage")) {
           previewLbl.setIcon(loadingPreview);
           UIMediaImage image = thumbGallery.getSelectedImage();
           if (image == null) {
             UISettings.LOGGER.log(Level.SEVERE, "UIMediaImage null");
             return;
           }
 
           if (ImageUtils.isInCache(image.getUri(ImageSize.medium))) {
            previewLbl.setIcon(ImageUtils.getIcon(image.getUri(ImageSize.medium), null, null));
             return;
           }
 
           final ImageWorker<UIMediaImage> imgWorker = new ImageWorker<UIMediaImage>(Arrays.asList(new UIMediaImage[]{image}), null, ImageSize.medium, ics.getPreviewDim(), "");// FIXME
           PropertyChangeListener propertyChange = new PropertyChangeListener() {//FIXME
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
               if (!(evt.getNewValue() instanceof SwingWorker.StateValue)) {
                 return;
               }
 
               if (((SwingWorker.StateValue) evt.getNewValue()).equals(SwingWorker.StateValue.DONE)) {
                 try {
                   Icon icon = imgWorker.get();
                   if (icon != null) {
                     previewLbl.setIcon(icon);
                     GalleryPanel.this.propertyChange.firePropertyChange("updateThumb", null, icon);
                   } else {
                     // TODO
                   }
                 } catch (InterruptedException ex) {
                   UISettings.LOGGER.log(Level.SEVERE, null, ex);
                 } catch (ExecutionException ex) {
                   UISettings.LOGGER.log(Level.SEVERE, null, ex);
                 }
               }
             }
           };
           imgWorker.addPropertyChangeListener(propertyChange);//FIXME
           imgWorker.execute();//FIXME
 
         }
       }
     });
 
     thumbGalleryPnl.add(thumbGallery.getView(false), BorderLayout.CENTER);
 
     setLocationRelativeTo(mr);
     setModal(true);
   }
 
   public ImageCategoryProperty getImageProperty() {
     return property;
   }
 
   public PropertyChangeSupport getPropertyChange() {
     return propertyChange;
   }
 
   private void getTumbPreview(List<UIMediaImage> images) {
     if (gworker != null && !gworker.isDone()) {
       WorkerManager.stop(gworker);
     }
     WorkerManager.fetchImages(this.getClass(), images, this, "ui/unknown.png", ics.getGalleryDim(), ImageSize.small);
   }
 
   public void addImages(List<UIMediaImage> images) {
     this.images.addAll(images);
     for (UIMediaImage image : this.images) {
       UIImageLang imglang = image.getImagelang();
       image.setIcon(loadingGallery);
 
       if (!languages.contains(imglang)) {
         languages.add(imglang);
         languagesModel.addElement(imglang);
       }
     }
 
     if (languages.size() > 0) {
       languagesModel.setSelectedItem(languages.get(0));
     }
   }
 
   public synchronized void addThumbPreview(Icon icon, int index) {
     thumbGallery.setImage(icon, index);
   }
 
   private List<UIMediaImage> getImageByLanguage(List<UIMediaImage> uiimages, String country) {
     List<UIMediaImage> limages = new ArrayList<UIMediaImage>();
 
     for (UIMediaImage image : uiimages) {
       Locale uiimagelang = image.getImagelang().getLang();
 
       if (country.replace("_", "").equalsIgnoreCase(uiimagelang.getCountry())) {
         limages.add(0, image);
       }
     }
 
     return limages;
   }
 
   public void setSelectedIndex(int index) {
     if (index > 0) {
       thumbGallery.setSelectedIndex(index);
     }
   }
 
   public void clear() {
     thumbGallery.removeAllImages();
     images.clear();
     languagesModel.removeAllElements();
     languages.clear();
   }
 
   /**
    * This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {
 
     galleryPnl = new com.alee.laf.panel.WebPanel();
     thumbGalleryPnl = new com.alee.laf.panel.WebPanel();
     previewPnl = new com.alee.laf.panel.WebPanel();
     previewLbl = new com.alee.laf.label.WebLabel();
     languageCbb = new javax.swing.JComboBox();
 
     setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
     thumbGalleryPnl.setFocusable(false);
 
     javax.swing.GroupLayout previewPnlLayout = new javax.swing.GroupLayout(previewPnl);
     previewPnl.setLayout(previewPnlLayout);
     previewPnlLayout.setHorizontalGroup(
       previewPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, previewPnlLayout.createSequentialGroup()
         .addContainerGap()
         .addComponent(languageCbb, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
         .addComponent(previewLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
         .addGap(280, 280, 280))
     );
     previewPnlLayout.setVerticalGroup(
       previewPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(previewPnlLayout.createSequentialGroup()
         .addGroup(previewPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
           .addComponent(previewLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
           .addComponent(languageCbb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         .addGap(0, 12, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout galleryPnlLayout = new javax.swing.GroupLayout(galleryPnl);
     galleryPnl.setLayout(galleryPnlLayout);
     galleryPnlLayout.setHorizontalGroup(
       galleryPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(galleryPnlLayout.createSequentialGroup()
         .addContainerGap()
         .addGroup(galleryPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
           .addComponent(previewPnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .addComponent(thumbGalleryPnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         .addContainerGap())
     );
     galleryPnlLayout.setVerticalGroup(
       galleryPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, galleryPnlLayout.createSequentialGroup()
         .addContainerGap()
         .addComponent(previewPnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addComponent(thumbGalleryPnl, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
         .addContainerGap())
     );
 
     javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
     getContentPane().setLayout(layout);
     layout.setHorizontalGroup(
       layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addComponent(galleryPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
     );
     layout.setVerticalGroup(
       layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(layout.createSequentialGroup()
         .addComponent(galleryPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         .addContainerGap())
     );
 
     pack();
   }// </editor-fold>//GEN-END:initComponents
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private com.alee.laf.panel.WebPanel galleryPnl;
   private javax.swing.JComboBox languageCbb;
   private com.alee.laf.label.WebLabel previewLbl;
   private com.alee.laf.panel.WebPanel previewPnl;
   private com.alee.laf.panel.WebPanel thumbGalleryPnl;
   // End of variables declaration//GEN-END:variables
 }
