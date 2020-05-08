 package com.rameses.rcp.control;
 
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.ui.UIControl;
 import com.rameses.rcp.util.UIControlUtil;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.AffineTransform;
 import java.beans.Beans;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  *
  * @author Windhel
  */
 
 public class XImageViewer extends JPanel implements UIControl {
     
     private Binding binding;
     private int index;
     private boolean advanced;
     private boolean fitImage;
     private String[] depends;
     private boolean dynamic;
     
     //calculate dimension on demand
     private int width = -1;
     private int height = -1;
     
     private String emptyImage;
     private Icon emptyImageIcon;
     
     private JSlider zoomSlider;
     private JCheckBox fitImgCheckBox = new JCheckBox("Fit:");
     private TitledBorder sliderBorder;
     private JScrollPane scrollPane = new JScrollPane();
     private ImageCanvas canvas  = new ImageCanvas();
     private JPanel columnHeader = new JPanel();
     private JLabel lblZoom = new JLabel("Zoom: 100%");
     
     private double zoomLevel = 1;
     private double fitPercentageWidth = 1.0;
     private double fitPercentageHeight = 1.0;
     private double scaleWidth = 1.0;
     private double scaleHeight = 1.0;
     private double scale = 1.0;
     private AffineTransform at;
     
     
     public XImageViewer() {
         if( !Beans.isDesignTime() ) {
             init();
         }
     }
     
     private void init() {
         super.setLayout(new BorderLayout());
         super.setBorder(BorderFactory.createEtchedBorder());
         
         scrollPane.setBorder(BorderFactory.createEmptyBorder());
         scrollPane.setViewportView(canvas);
         
         super.add(scrollPane, BorderLayout.CENTER);
         
         fitImgCheckBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(fitImage)
                     fitImage = false;
                 else
                     fitImage = true;
                 
                 if( fitImage ) {
                     zoomSlider.setValue(100);
                     zoomSlider.setEnabled(false);
                 } else {
                     zoomSlider.setEnabled(true);
                 }
                 
                 
                 repaint();
             }
         });
         setFitImage(true);
     }
     
     public void setLayout(LayoutManager mgr) {;}
     
     public void refresh() {
         if ( dynamic ) {
             EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     render();
                 }
             });
         }
     }
     
     public void load() {
         if ( !dynamic ) {
             EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     render();
                 }
             });
         }
     }
     
     public int compareTo(Object o) {
         return UIControlUtil.compare(this, o);
     }
     
     //<editor-fold defaultstate="collapsed" desc="  helper method(s)  ">
     private Image getImage() {
         Image image = null;
         try {
             Object value = null;
             
             try {
                 value = UIControlUtil.getBeanValue(this);
             } catch(Exception e) {;}
             
             if( value != null ) 
             {
                 if(value instanceof String) 
                 {
                     InputStream is = getClass().getClassLoader().getResourceAsStream((String) value);
                     image = ImageIO.read(is);
                 } 
                 else if(value instanceof byte[]) 
                 {
                     image = ImageIO.read(new ByteArrayInputStream((byte[])value));
                 } 
                 else if(value instanceof Image) 
                 {
                     image = (Image) value;
                 } 
                 else if(value instanceof ImageIcon) 
                 {
                     image = ((ImageIcon)value).getImage();
                 } 
                 else if(value instanceof InputStream) 
                 {
                     image = ImageIO.read((InputStream)value);
                 } 
                 else if(value instanceof File) 
                 {
                     image = new ImageIcon(((File)value).toURL()).getImage();
                 } 
                 else if(value instanceof URL) 
                 {
                     image = new ImageIcon((URL) value).getImage();
                 }
             }
         } catch(Exception ex) {
             ex.printStackTrace();
         }
         
         return image;
     }
         
     private void attachAdvancedOptions() {
         if( advanced ) {
             //if ( columnHeader == null ) {
             columnHeader.setBorder(BorderFactory.createEtchedBorder());
             columnHeader.setLayout( new FlowLayout(FlowLayout.LEFT, 1, 1) );
             
             zoomSlider = new JSlider(10,200,100);
             zoomSlider.addChangeListener(new ChangeListener() 
             {
                 public void stateChanged(ChangeEvent e) 
                 {
                     zoomLevel = (zoomSlider.getValue()/100.00);
                     //sliderBorder.setTitle("Zoom: " + (int)(zoomLevel * 100) + "%");
                     lblZoom.setText("Zoom: " + (int)(zoomLevel * 100) + "%");
                     
                     canvas.repaint();
                     canvas.revalidate();
                 }
             });
             if( fitImage ) zoomSlider.setEnabled(false);
             
             columnHeader.add(lblZoom);
             columnHeader.add(zoomSlider);
             columnHeader.add(fitImgCheckBox);
             //}
             add(columnHeader, BorderLayout.NORTH);
         } else {
             remove(columnHeader);
         }
     }
     
     private void render() {
         if(advanced == true) {
             scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
             scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         } else if(fitImage == true) {
             advanced = false;
             scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
             scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
         }
     }
     //</editor-fold>
     
     
     //<editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     public String[] getDepends() {
         return depends;
     }
     
     public void setDepends(String[] depends) {
         this.depends = depends;
     }
     
     public int getIndex() {
         return index;
     }
     
     public void setIndex(int index) {
         this.index = index;
     }
     
     public void setBinding(Binding binding) {
         this.binding = binding;
     }
     
     public Binding getBinding() {
         return binding;
     }
     
     public boolean isAdvanced() {
         return advanced;
     }
     
     public void setAdvanced(boolean advanced) {
         this.advanced = advanced;
         attachAdvancedOptions();
     }
     
     public boolean isFitImage() {
         return fitImage;
     }
     
     public void setFitImage(boolean fitImage) {
         this.fitImage = fitImage;
         if( fitImgCheckBox != null )
             fitImgCheckBox.setSelected(fitImage);
     }
     
     public boolean isDynamic() {
         return dynamic;
     }
     
     public void setDynamic(boolean dynamic) {
         this.dynamic = dynamic;
     }
         
     public String getEmptyImage() {
         return emptyImage;
     }
     
     public void setEmptyImage(String emptyImageName) {
         this.emptyImage = emptyImageName;
     }
     
     public Icon getEmptyImageIcon() {
         return emptyImageIcon;
     }
     
     public void setEmptyImageIcon(Icon emptyImageIcon) {
         this.emptyImageIcon = emptyImageIcon;
     }
     
     public void setBackground(Color bg) {
         super.setBackground(bg);
         if( canvas != null ) canvas.setBackground(bg);
     }
     //</editor-fold>
     
     
     //<editor-fold defaultstate="collapsed" desc="  ImageCanvas (class)  ">
     private class ImageCanvas extends JPanel {
         
         public void paintComponent(Graphics g) {
             super.paintComponent(g);
             
             Image image = getImage();            
             boolean usingEmptyIcon = false;
             
             if ( image == null && emptyImageIcon instanceof ImageIcon ) {
                 usingEmptyIcon = true;
                 image = ((ImageIcon) emptyImageIcon).getImage();
             }
             
             if ( image == null ) return;
             
             width = (int) (image.getWidth(this) * zoomLevel);
             height = (int) (image.getHeight(this) * zoomLevel);
             
             if( isFitImage() && !Beans.isDesignTime() ) {
                 calculateFit(image);
                 Graphics2D g2 = (Graphics2D)g.create();
                 at = AffineTransform.getTranslateInstance(fitPercentageWidth, fitPercentageHeight);
                 at.scale(scale, scale);
                 g2.drawImage(image, at, this);
                 updateSize(image.getWidth(this), image.getHeight(this));
                 g2.dispose();
             } else {
                 g.drawImage(image, 0, 0, width, height, null);
                 updateSize(width, height);
                 g.dispose();
             }
             
             if( !usingEmptyIcon ) image.flush();
         }
         
         private void updateSize(final int width, final int height) {
             EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     setPreferredSize(new Dimension(width, height));
                 }
             });
         }
         
         private void calculateFit(Image image) {
             scaleWidth = scrollPane.getViewport().getExtentSize().getWidth() / width;
             scaleHeight = scrollPane.getViewport().getExtentSize().getHeight() / height;
             scale = Math.min(scaleWidth, scaleHeight);
             fitPercentageWidth = (scrollPane.getViewport().getExtentSize().getWidth() - (scale * image.getWidth(this)))/2;
             fitPercentageHeight = (scrollPane.getViewport().getExtentSize().getHeight() - (scale * image.getHeight(this)))/2;
         }
     }
     //</editor-fold>
     
 }
