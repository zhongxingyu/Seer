 package com.project4.FilterPlayGround;
 
 import java.util.ArrayList;
 
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 import com.anotherbrick.inthewall.TouchEnabled;
 import com.anotherbrick.inthewall.VizButton;
 import com.anotherbrick.inthewall.VizPanel;
 import com.project4.FilterPlayGround.serializables.AbstractSerializableBox;
 
 public abstract class AbstractFilterBox extends VizPanel implements TouchEnabled {
 
   protected BoxConnectorIngoing inputConnector;
   protected BoxConnectorOutgoing outputConnector;
   private Integer id = 0;
 
   protected MyColorEnum COLOR = MyColorEnum.DARK_WHITE;
 
   public AbstractBoxConnector getInputConnector() {
     return inputConnector;
   }
 
   public AbstractBoxConnector getOutputConnector() {
     return outputConnector;
   }
 
   public float CONNECTOR_SIZE = 20;
 
   public float REMOVE_BUTTON_DEFAULT_X = 60;
   public float REMOVE_BUTTON_DEFAULT_Y = 25;
   public float REMOVE_BUTTON_DEFAULT_HEIGHT = 12;
   public float REMOVE_BUTTON_DEFAULT_WIDTH = 26;
 
   public AbstractFilterBox(float x0, float y0, float width, float height, VizPanel parent) {
     super(x0, y0, width, height, parent);
     inputConnector =
         new BoxConnectorIngoing(0, getHeight() / 2, CONNECTOR_SIZE, CONNECTOR_SIZE, this);
   }
 
   public abstract boolean needKeyboard();
 
   public float getRemoveX() {
     return REMOVE_BUTTON_DEFAULT_X;
   }
 
   public float getRemoveY() {
     return REMOVE_BUTTON_DEFAULT_Y;
   }
 
   protected VizButton removeButton;
   private ArrayList<AbstractFilterBox> ingoingConnections = new ArrayList<AbstractFilterBox>();
 
   public void addIngoingConnection(AbstractFilterBox afb) {
     ingoingConnections.add(afb);
   }
 
   public void removeIngoingConnection(AbstractFilterBox afb) {
     ingoingConnections.remove(afb);
   }
 
   public abstract String getFilter();
 
   float spanX;
   float spanY;
 
   public boolean draw() {
     pushStyle();
     background(COLOR);
     inputConnector.draw();
     if (!isTerminal()) {
       outputConnector.draw();
     }
     removeButton.draw();
     popStyle();
     if (dragging) {
      modifyPositionWithAbsoluteValue(m.touchX - spanX, m.touchY - spanY);
     }
     return false;
   }
 
   @Override
   public void setup() {
     removeButton =
         new VizButton(getRemoveX(), getRemoveY(), REMOVE_BUTTON_DEFAULT_WIDTH,
             REMOVE_BUTTON_DEFAULT_HEIGHT, this);
     removeButton.setText("Canc");
     removeButton.setStyle(MyColorEnum.LIGHT_GRAY, MyColorEnum.WHITE, MyColorEnum.DARK_GRAY, 255f,
         255f, 10);
     removeButton.setStylePressed(MyColorEnum.MEDIUM_GRAY, MyColorEnum.WHITE, MyColorEnum.DARK_GRAY,
         255f, 10);
     addTouchSubscriber(removeButton);
   }
 
   @Override
   public void modifyPosition(float newX0, float newY0) {
     super.modifyPosition(newX0, newY0);
     inputConnector.modifyPosition(0, getHeight() / 2);
     if (!isTerminal()) outputConnector.modifyPosition(getWidth(), getHeight() / 2);
     removeButton.modifyPosition(getRemoveX(), getRemoveY());
   }
 
   @Override
   public void modifyPositionWithAbsoluteValue(float newX0, float newY0) {
     super.modifyPositionWithAbsoluteValue(newX0, newY0);
     inputConnector.modifyPosition(0, getHeight() / 2);
     if (!isTerminal()) outputConnector.modifyPosition(getWidth(), getHeight() / 2);
     removeButton.modifyPosition(getRemoveX(), getRemoveY());
   }
 
   public ArrayList<AbstractFilterBox> getIngoingConnections() {
     return ingoingConnections;
   }
 
   private boolean focus = false;
 
   public void setFocus(boolean focus) {
     this.focus = focus;
   }
 
   public boolean hasFocus() {
     return focus;
   }
 
   private boolean dragging = false;
 
   public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
 
     if (down) {
       dragging = true;
       // setModal(true);
       spanX = m.touchX - getX0Absolute();
       spanY = m.touchY - getY0Absolute();
     } else {
       dragging = false;
       // setModal(false);
     }
 
     return false;
   }
 
   public abstract boolean isTerminal();
 
   public abstract AbstractSerializableBox serialize();
 
   public Integer getId() {
     return id;
   }
 
   public void setId(Integer id) {
     this.id = id;
     removeButton.name = id + "|" + "remove";
   }
 
 }
