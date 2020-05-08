 package com.project4.FilterPlayGround;
 
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 import com.anotherbrick.inthewall.EventSubscriber;
 import com.anotherbrick.inthewall.VizCheckBox;
 import com.anotherbrick.inthewall.VizNotificationCenter.EventName;
 import com.anotherbrick.inthewall.VizPanel;
 import com.project4.FilterPlayGround.serializables.AbstractSerializableBox;
 import com.project4.FilterPlayGround.serializables.SerializableFilterBox;
 
 public class FilterBox extends AbstractFilterBox implements EventSubscriber {
 
   public FilterBox(float x0, float y0, float width, float height, VizPanel parent) {
     super(x0, y0, width, height, parent);
 
     outputConnector =
         new BoxConnectorOutgoing(getWidth(), getHeight() / 2, CONNECTOR_SIZE, CONNECTOR_SIZE, this);
     m.notificationCenter.registerToEvent(EventName.BUTTON_PRESSED, this);
     addTouchSubscriber(checkBox);
     checkBox.setSelected(true);
   }
 
   public FilterBox(SerializableFilterBox asb, VizPanel parent) {
     this(asb.getX0(), asb.getY0(), asb.getWidth(), asb.getHeight(), parent);
     setId(asb.getId());
     this.content = asb.getFilter();
     checkBox.setSelected(asb.isSelected());
   }
 
   public static MyColorEnum TEXT_COLOR = MyColorEnum.BLACK;
   public static float TEXT_X = 32;
   public static float TEXT_Y = 20;
   public static float TEXT_SIZE = 12;
 
   private VizCheckBox checkBox = new VizCheckBox(getHeight() / 4, getHeight() / 4, getHeight() / 2,
       getHeight() / 2, this);
   private String content = "";
 
   @Override
   public boolean isTerminal() {
     return false;
   }
 
 
   @Override
   public void setup() {}
 
   @Override
   public boolean draw() {
     super.draw();
     pushStyle();
     fill(TEXT_COLOR);
     textSize(TEXT_SIZE);
     text(content, TEXT_X, TEXT_Y);
     checkBox.draw();
     popStyle();
     return false;
   }
 
   @Override
   public String getFilter() {
     return checkBox.isSelected() ? "+" : "-" + content;
   }
 
   @Override
   public void eventReceived(EventName eventName, Object data) {
     String d = (String) data;
     if (hasFocus() && d.contains("keyboard|")) {
       String symbol = d.split("\\|")[1];
      if (symbol.equals("DEL")) {
        if (content.length() > 0) content = content.substring(0, content.length() - 1);
        return;
      }
      if (symbol.equals("_")) {
        content += " ";
        return;
      }

       content += symbol;
     }
   }
 
   @Override
   public AbstractSerializableBox serialize() {
     return new SerializableFilterBox(getId(), getX0(), getY0(), getWidth(), getHeight(), content,
         checkBox.isSelected());
   }
 
   @Override
   public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
     propagateTouch(x, y, down, touchType);
     return super.touch(x, y, down, touchType);
   }
 
   @Override
   public void modifyPositionWithAbsoluteValue(float newX0, float newY0) {
     super.modifyPositionWithAbsoluteValue(newX0, newY0);
     checkBox.modifyPosition(getHeight() / 4, getHeight() / 4);
   }
 }
