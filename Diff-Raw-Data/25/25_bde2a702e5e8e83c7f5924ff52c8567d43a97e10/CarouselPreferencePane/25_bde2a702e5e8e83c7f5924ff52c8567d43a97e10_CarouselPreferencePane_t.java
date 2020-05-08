 package ch9k.plugins.carousel;
 
 import ch9k.core.I18n;
 import ch9k.core.settings.Settings;
 import ch9k.core.settings.SettingsChangeEvent;
 import ch9k.core.settings.SettingsChangeListener;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * Preference pane for a Carousel.
  */
 public class CarouselPreferencePane
         extends JPanel implements SettingsChangeListener, ChangeListener {
     /**
      * Maximum number of images to show.
      */
     public static final String MAX_IMAGES = "maxImages";
 
     /**
      * Settings to manipulate.
      */
     private final Settings settings;
 
     /**
      * MAX_IMAGES spinner.
      */
     private final JSpinner maxImagesSpinner;
 
     /**
      * Constructor.
      * @param settings Settings to manipulate.
      */
     public CarouselPreferencePane(final Settings settings) {
         this.settings = settings;
 
         GroupLayout layout = new GroupLayout(this);
 
         layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
 
         JLabel maxImagesLabel = new JLabel(
                 I18n.get("ch9k.plugins.carousel", "max_images"));
         maxImagesSpinner = new JSpinner(new SpinnerNumberModel(
                 settings.getInt(MAX_IMAGES),
                 5, 30, 1));
 
         layout.setHorizontalGroup(layout.createSequentialGroup()
             .addGroup(layout.createParallelGroup()
                 .addComponent(maxImagesLabel))
             .addGroup(layout.createParallelGroup()
                 .addComponent(maxImagesSpinner)));
 
         layout.setVerticalGroup(layout.createSequentialGroup()
             .addGroup(layout.createParallelGroup()
                 .addComponent(maxImagesLabel)
                .addComponent(maxImagesSpinner, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
 
         setLayout(layout);
 
         maxImagesSpinner.addChangeListener(this);
         settings.addSettingsListener(this);
     }
 
     @Override
     public void settingsChanged(SettingsChangeEvent event) {
         if(MAX_IMAGES.equals(event.getKey())) {
             maxImagesSpinner.setValue(settings.getInt(MAX_IMAGES));
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent event) {
         if(event.getSource() == maxImagesSpinner) {
             settings.setInt(MAX_IMAGES,
                     (Integer) maxImagesSpinner.getValue());
         }
     }
 }
