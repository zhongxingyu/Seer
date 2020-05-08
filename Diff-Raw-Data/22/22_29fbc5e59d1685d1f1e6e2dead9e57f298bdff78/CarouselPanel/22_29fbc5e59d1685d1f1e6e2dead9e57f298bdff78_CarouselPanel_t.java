 package ch9k.plugins.carousel;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import javax.swing.JPanel;
 
 /**
  * The main Carousel panel.
  */
 public class CarouselPanel extends JPanel {
     /**
      * The selection model.
      */
     private CarouselImageModel model;
 
     /**
      * The image panel.
      */
     private CarouselImagePanel imagePanel;
 
     /**
      * The image chooser panel.
      */
     private CarouselImageChooserPanel imageChooserPanel;
 
     /**
      * The recommendation panel.
      */
     private CarouselRecommendationPanel recommendationPanel;
 
     /**
      * Constructor.
      * @param model The selection model of the plugin.
      */
     public CarouselPanel(CarouselImageModel model) {
         super(new BorderLayout());
         this.model = model;

        recommendationPanel = new CarouselRecommendationPanel(model);
        add(recommendationPanel, BorderLayout.NORTH);
 
         imagePanel = new CarouselImagePanel(model);
        add(imagePanel, BorderLayout.CENTER);
 
         imageChooserPanel = new CarouselImageChooserPanel(model);
        add(imageChooserPanel, BorderLayout.SOUTH);
     }
 
     /**
      * Disable the plugin.
      */
     public void disablePlugin() {
         imageChooserPanel.disablePlugin();
     }
 }
