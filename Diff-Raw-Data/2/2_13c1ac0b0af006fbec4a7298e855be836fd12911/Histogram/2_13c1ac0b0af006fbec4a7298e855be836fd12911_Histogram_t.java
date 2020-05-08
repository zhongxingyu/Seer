 package mhcs.dan;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  *
  * @author Daniel Hammond
  *
  */
 public class Histogram extends Widget {
 
     private final transient VerticalPanel panel;
     private transient int plain;
     private transient int dormitory;
     private transient int sanitation;
     private transient int foodAndWater;
     private transient int gymAndRelaxation;
     private transient int canteen;
     private transient int power;
     private transient int control;
     private transient int airlock;
     private transient int medical;
     private final transient Context2d context;
 
     /**
      *
      */
     public Histogram() {
         super();
         panel = new VerticalPanel();
         plain = 0;
         dormitory = 0;
         sanitation = 0;
         foodAndWater = 0;
         gymAndRelaxation = 0;
         canteen = 0;
         power = 0;
         control = 0;
         airlock = 0;
         medical = 0;
         Canvas canvas = Canvas.createIfSupported();
         canvas.setHeight("200px");
         canvas.setWidth("522px");
         canvas.setCoordinateSpaceHeight(200);
         canvas.setCoordinateSpaceWidth(522);
         context = canvas.getContext2d();
         panel.add(canvas);
         makeIcons();
         
     }
     
     private void makeIcons() {
         final PopupPanel numberPanel = new PopupPanel(true, true);
         final Label popupLabel = new Label();
         numberPanel.add(popupLabel);
         Grid icons;
         int thumbDim = 50;
         icons = new Grid(1, 10);
         icons.setCellPadding(0);
         
        final Image airlockIcon = new Image("images/Airlock.jpg");
         final Image canteenIcon = new Image("images/canteen.png");
         final Image controlIcon = new Image("images/control.jpg");
         final Image dormIcon = new Image("images/dorm.jpg");
         final Image gymIcon = new Image("images/gym.png");
         final Image medIcon = new Image("images/medical.png");
         final Image plainIcon = new Image("images/plain.png");
         final Image powerIcon = new Image("images/power.jpg");
         final Image sanitationIcon = new Image("images/sanitation.jpg");
         final Image storageIcon = new Image("images/storage.jpg");
         
         airlockIcon.setPixelSize(thumbDim, thumbDim);
         canteenIcon.setPixelSize(thumbDim, thumbDim);
         controlIcon.setPixelSize(thumbDim, thumbDim);
         dormIcon.setPixelSize(thumbDim, thumbDim);
         gymIcon.setPixelSize(thumbDim, thumbDim);
         medIcon.setPixelSize(thumbDim, thumbDim);
         plainIcon.setPixelSize(thumbDim, thumbDim);
         powerIcon.setPixelSize(thumbDim, thumbDim);
         sanitationIcon.setPixelSize(thumbDim, thumbDim);
         storageIcon.setPixelSize(thumbDim, thumbDim);
         
         airlockIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("airlock modules: " + airlock);
                 numberPanel.show();
             }
         });
         
         canteenIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("canteen modules: " + canteen);
                 numberPanel.show();
             }
         });
         
         controlIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("control modules: " + control);
                 numberPanel.show();
             }
         });
         
         dormIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("dormitory modules: " + dormitory);
                 numberPanel.show();
             }
         });
         
         gymIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("gym and relaxation modules: " + gymAndRelaxation);
                 numberPanel.show();
             }
         });
         
         medIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("medical modules: " + medical);
                 numberPanel.show();
             }
         });
         
         plainIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("plain modules: " + plain);
                 numberPanel.show();
             }
         });
         
         powerIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("power modules: " + power);
                 numberPanel.show();
             }
         });
         
         sanitationIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("sanitation modules: " + sanitation);
                 numberPanel.show();
             }
         });
         
         storageIcon.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 numberPanel.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                 popupLabel.setText("food and water modules: " + foodAndWater);
                 numberPanel.show();
             }
         });
         
         icons.setWidget(0, 0, plainIcon);
         icons.setWidget(0, 1, dormIcon);
         icons.setWidget(0, 2, sanitationIcon);
         icons.setWidget(0, 3, storageIcon);
         icons.setWidget(0, 4, gymIcon);
         icons.setWidget(0, 5, canteenIcon);
         icons.setWidget(0, 6, powerIcon);
         icons.setWidget(0, 7, controlIcon);
         icons.setWidget(0, 8, airlockIcon);
         icons.setWidget(0, 9, medIcon);
         panel.add(icons);
     }
 
     /**
      * Checks for consistency in variables.
      */
     private void checkConsistent() {
         assert plain >= 0;
         assert dormitory >= 0;
         assert sanitation >= 0;
         assert foodAndWater >= 0;
         assert gymAndRelaxation >= 0;
         assert canteen >= 0;
         assert power >= 0;
         assert control >= 0;
         assert airlock >= 0;
         assert medical >= 0;
     }
 
     /**
      *
      * @return panel the panel that holds the histogram
      */
     public final Widget get() {
         DecoratorPanel returnPanel = new DecoratorPanel();
         returnPanel.add(panel);
         return returnPanel;
     }
 
     /**
      *
      */
     public final void update() {
         int barWidth = 50;
         int barHeight = 5;
         int barSpacing = 2;
         int totalHeight = 200;
         context.clearRect(barSpacing, totalHeight - plain * barHeight,
                 barWidth, plain * barHeight);
         context.clearRect(barSpacing + barWidth + barSpacing,
                 totalHeight - dormitory * barHeight, barWidth,
                 dormitory * barHeight);
         context.clearRect(barSpacing + 2 * (barWidth + barSpacing),
                 totalHeight - sanitation * barHeight, barWidth,
                 sanitation * barHeight);
         context.clearRect(barSpacing + 3 * (barWidth + barSpacing),
                 totalHeight - foodAndWater * barHeight,
                 barWidth, foodAndWater * barHeight);
         context.clearRect(barSpacing + 4 * (barWidth + barSpacing),
                 totalHeight - gymAndRelaxation * barHeight,
                 barWidth, gymAndRelaxation * barHeight);
         context.clearRect(barSpacing + 5 * (barWidth + barSpacing),
                 totalHeight - canteen * barHeight,
                 barWidth, canteen * barHeight);
         context.clearRect(barSpacing + 6 * (barWidth + barSpacing),
                 totalHeight - power * barHeight,
                 barWidth, power * barHeight);
         context.clearRect(barSpacing + 7 * (barWidth + barSpacing),
                 totalHeight - control * barHeight,
                 barWidth, control * barHeight);
         context.clearRect(barSpacing + 8 * (barWidth + barSpacing),
                 totalHeight - airlock * barHeight,
                 barWidth, airlock * barHeight);
         context.clearRect(barSpacing + 9 * (barWidth + barSpacing),
                 totalHeight - medical * barHeight,
                 barWidth, medical * barHeight);
         resetCounts();
         for (Module mod : ModuleList.get()) {
             if (mod.getType().equals(Module.ModuleType.PLAIN)) {
                 plain++;
             } else if (mod.getType().equals(Module.ModuleType.DORMITORY)) {
                 dormitory++;
             } else if (mod.getType().equals(Module.ModuleType.SANITATION)) {
                 sanitation++;
             } else if (mod.getType().equals(Module.ModuleType.FOOD_AND_WATER)) {
                 foodAndWater++;
             } else if (mod.getType().equals(
                     Module.ModuleType.GYM_AND_RELAXATION)) {
                 gymAndRelaxation++;
             } else if (mod.getType().equals(Module.ModuleType.CANTEEN)) {
                 canteen++;
             } else if (mod.getType().equals(Module.ModuleType.POWER)) {
                 power++;
             } else if (mod.getType().equals(Module.ModuleType.CONTROL)) {
                 control++;
             } else if (mod.getType().equals(Module.ModuleType.AIRLOCK)) {
                 airlock++;
             } else if (mod.getType().equals(Module.ModuleType.MEDICAL)) {
                 medical++;
             }
         }
         context.fillRect(barSpacing, totalHeight - plain * barHeight,
                 barWidth, plain * barHeight);
         context.fillRect(barSpacing + barWidth + barSpacing,
                 totalHeight - dormitory * barHeight, barWidth,
                 dormitory * barHeight);
         context.fillRect(barSpacing + 2 * (barWidth + barSpacing),
                 totalHeight - sanitation * barHeight, barWidth,
                 sanitation * barHeight);
         context.fillRect(barSpacing + 3 * (barWidth + barSpacing),
                 totalHeight - foodAndWater * barHeight,
                 barWidth, foodAndWater * barHeight);
         context.fillRect(barSpacing + 4 * (barWidth + barSpacing),
                 totalHeight - gymAndRelaxation * barHeight,
                 barWidth, gymAndRelaxation * barHeight);
         context.fillRect(barSpacing + 5 * (barWidth + barSpacing),
                 totalHeight - canteen * barHeight, barWidth,
                 canteen * barHeight);
         context.fillRect(barSpacing + 6 * (barWidth + barSpacing),
                 totalHeight - power * barHeight,
                 barWidth, power * barHeight);
         context.fillRect(barSpacing + 7 * (barWidth + barSpacing),
                 totalHeight - control * barHeight,
                 barWidth, control * barHeight);
         context.fillRect(barSpacing + 8 * (barWidth + barSpacing),
                 totalHeight - airlock * barHeight,
                 barWidth, airlock * barHeight);
         context.fillRect(barSpacing + 9 * (barWidth + barSpacing),
                 totalHeight - medical * barHeight,
                 barWidth, medical * barHeight);
         checkConsistent();
     }
 
     private void resetCounts() {
         plain = 0;
         dormitory = 0;
         sanitation = 0;
         foodAndWater = 0;
         gymAndRelaxation = 0;
         canteen = 0;
         power = 0;
         control = 0;
         airlock = 0;
         medical = 0;
     }
 }
