 package net.sf.freecol.client.gui.panel;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.logging.Logger;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 
 import net.sf.freecol.client.control.InGameController;
 import net.sf.freecol.client.gui.Canvas;
 import net.sf.freecol.client.gui.i18n.Messages;
 import net.sf.freecol.common.model.Building;
 import net.sf.freecol.common.model.ColonyTile;
 import net.sf.freecol.common.model.Goods;
 import net.sf.freecol.common.model.Unit;
 
 /**
  * This label holds Unit data in addition to the JLabel data, which makes it
  * ideal to use for drag and drop purposes.
  */
 public final class UnitLabel extends JLabel implements ActionListener {
     public static final String COPYRIGHT = "Copyright (C) 2003-2005 The FreeCol Team";
 
     public static final String LICENSE = "http://www.gnu.org/licenses/gpl.html";
 
     public static final String REVISION = "$Revision$";
 
     private static Logger logger = Logger.getLogger(UnitLabel.class.getName());
 
     public static final int ARM = 0, MOUNT = 1, TOOLS = 2, DRESS = 3, WORKTYPE_FOOD = 4, WORKTYPE_SUGAR = 5,
             WORKTYPE_TOBACCO = 6, WORKTYPE_COTTON = 7, WORKTYPE_FURS = 8, WORKTYPE_LUMBER = 9, WORKTYPE_ORE = 10,
             WORKTYPE_SILVER = 11, CLEAR_SPECIALITY = 12, ACTIVATE_UNIT = 13, FORTIFY = 14, SENTRY = 15, COLOPEDIA = 16,
             LEAVE_TOWN = 17;
 
     public static final int WORK_AT_SOMEWHERE = 100,
         WORK_AT_TOWN_HALL = WORK_AT_SOMEWHERE+Building.TOWN_HALL,
         WORK_AT_CARPENTER = WORK_AT_SOMEWHERE+Building.CARPENTER,
         WORK_AT_BLACKSMITH = WORK_AT_SOMEWHERE+Building.BLACKSMITH,
         WORK_AT_TOBACCONIST = WORK_AT_SOMEWHERE+Building.TOBACCONIST,
         WORK_AT_WEAVER = WORK_AT_SOMEWHERE+Building.WEAVER,
         WORK_AT_DISTILLER = WORK_AT_SOMEWHERE+Building.DISTILLER,
         WORK_AT_FUR_TRADER = WORK_AT_SOMEWHERE+Building.FUR_TRADER,
         WORK_AT_SCHOOLHOUSE = WORK_AT_SOMEWHERE+Building.SCHOOLHOUSE,
         WORK_AT_ARMORY = WORK_AT_SOMEWHERE+Building.ARMORY,
         WORK_AT_CHURCH = WORK_AT_SOMEWHERE+Building.CHURCH,
         WORK_AT_STOCKADE = WORK_AT_SOMEWHERE+Building.STOCKADE,
         WORK_AT_WAREHOUSE = WORK_AT_SOMEWHERE+Building.WAREHOUSE,
         WORK_AT_STABLES = WORK_AT_SOMEWHERE+Building.STABLES,
         WORK_AT_DOCK = WORK_AT_SOMEWHERE+Building.DOCK,
         WORK_AT_PRINTING_PRESS = WORK_AT_SOMEWHERE+Building.PRINTING_PRESS,
         WORK_AT_CUSTOM_HOUSE = WORK_AT_SOMEWHERE+Building.CUSTOM_HOUSE,
         WORK_AT_LASTBUILDING = WORK_AT_CUSTOM_HOUSE+1;
         
     private final Unit unit;
 
     private final Canvas parent;
 
     private boolean selected;
 
     private boolean ignoreLocation;
 
     private InGameController inGameController;
 
 
     /**
      * Initializes this JLabel with the given unit data.
      * 
      * @param unit The Unit that this JLabel will visually represent.
      * @param parent The parent that knows more than we do.
      */
     public UnitLabel(Unit unit, Canvas parent) {
         ImageProvider lib = parent.getImageProvider();
         int type = lib.getUnitGraphicsType(unit);
         setIcon(lib.getUnitImageIcon(type));
         setDisabledIcon(lib.getUnitImageIcon(type, true));
         this.unit = unit;
         setDescriptionLabel(unit.getName());
         this.parent = parent;
         selected = false;
 
         setSmall(false);
         setIgnoreLocation(false);
 
         this.inGameController = parent.getClient().getInGameController();
     }
 
     /**
      * Initializes this JLabel with the given unit data.
      * 
      * @param unit The Unit that this JLabel will visually represent.
      * @param parent The parent that knows more than we do.
      * @param isSmall The image will be smaller if set to <code>true</code>.
      */
     public UnitLabel(Unit unit, Canvas parent, boolean isSmall) {
         this(unit, parent);
         setSmall(isSmall);
         setIgnoreLocation(false);
     }
 
     /**
      * Initializes this JLabel with the given unit data.
      * 
      * @param unit The Unit that this JLabel will visually represent.
      * @param parent The parent that knows more than we do.
      * @param isSmall The image will be smaller if set to <code>true</code>.
      * @param ignoreLocation The image will not include production or state
      *            information if set to <code>true</code>.
      */
     public UnitLabel(Unit unit, Canvas parent, boolean isSmall, boolean ignoreLocation) {
         this(unit, parent);
         setSmall(isSmall);
         setIgnoreLocation(ignoreLocation);
     }
 
     /**
      * Returns the parent Canvas object.
      * 
      * @return This UnitLabel's Canvas.
      */
     public Canvas getCanvas() {
         return parent;
     }
 
     /**
      * Returns this UnitLabel's unit data.
      * 
      * @return This UnitLabel's unit data.
      */
     public Unit getUnit() {
         return unit;
     }
 
     /**
      * Sets whether or not this unit should be selected.
      * 
      * @param b Whether or not this unit should be selected.
      */
     public void setSelected(boolean b) {
         selected = b;
     }
 
     /**
      * Sets whether or not this unit label should include production and state
      * information.
      * 
      * @param b Whether or not this unit label should include production and
      *            state information.
      */
     public void setIgnoreLocation(boolean b) {
         ignoreLocation = b;
     }
 
     /**
      * Makes a smaller version.
      * 
      * @param isSmall The image will be smaller if set to <code>true</code>.
      */
     public void setSmall(boolean isSmall) {
         if (isSmall) {
             setPreferredSize(null);
             ImageIcon imageIcon = (parent.getImageProvider().getUnitImageIcon(parent.getImageProvider()
                     .getUnitGraphicsType(unit)));
             // setIcon(new
             // ImageIcon(imageIcon.getImage().getScaledInstance(imageIcon.getIconWidth()
             // / 2, imageIcon.getIconHeight() / 2, Image.SCALE_DEFAULT)));
             setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance((imageIcon.getIconWidth() / 3) * 2,
                     (imageIcon.getIconHeight() / 3) * 2, Image.SCALE_SMOOTH)));
 
             ImageIcon disabledImageIcon = (parent.getImageProvider().getUnitImageIcon(parent.getImageProvider()
                     .getUnitGraphicsType(unit), true));
             setDisabledIcon(new ImageIcon(disabledImageIcon.getImage().getScaledInstance(
                     (imageIcon.getIconWidth() / 3) * 2, (imageIcon.getIconHeight() / 3) * 2, Image.SCALE_SMOOTH)));
             setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
         } else {
             if (unit.getLocation() instanceof ColonyTile) {
                 setSize(new Dimension(parent.getImageProvider().getTerrainImageWidth(0) / 2, parent
                         .getImageProvider().getUnitImageHeight(parent.getImageProvider().getUnitGraphicsType(unit))));
             } else {
                 setPreferredSize(null);
             }
 
             setIcon(parent.getImageProvider().getUnitImageIcon(parent.getImageProvider().getUnitGraphicsType(unit)));
             setDisabledIcon(parent.getImageProvider().getUnitImageIcon(
                     parent.getImageProvider().getUnitGraphicsType(unit), true));
             if (unit.getLocation() instanceof ColonyTile) {
                 setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
             } else {
                 setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
             }
         }
 
     }
 
     /**
      * Gets the description label.
      * 
      * The description label is a tooltip with the unit name and description of
      * the terrain its on if applicable *
      * 
      * @return This UnitLabel's description label.
      */
     public String getDescriptionLabel() {
         return getToolTipText();
     }
 
     /**
      * Sets the description label.
      * 
      * The description label is a tooltip with the unit name and description of
      * the terrain its on if applicable
      * 
      * @param label The string to set the label to.
      */
     public void setDescriptionLabel(String label) {
         setToolTipText(label);
 
     }
 
     /**
      * Paints this UnitLabel.
      * 
      * @param g The graphics context in which to do the painting.
      */
     public void paintComponent(Graphics g) {
 
         if (getToolTipText() == null) {
             setToolTipText(unit.getName());
         }
 
         if (ignoreLocation || selected || (!unit.isCarrier() && unit.getState() != Unit.SENTRY)) {
             setEnabled(true);
         } else if (unit.getOwner() != parent.getClient().getMyPlayer() && unit.getColony() == null) {
             setEnabled(true);
         } else {
             setEnabled(false);
         }
 
         super.paintComponent(g);
         if (ignoreLocation)
             return;
 
         if (getParent() instanceof ColonyPanel.OutsideColonyPanel || 
             getParent() instanceof ColonyPanel.InPortPanel || 
             getParent().getParent() instanceof ReportUnitPanel) {
             int x = (getWidth() - getIcon().getIconWidth()) / 2;
             int y = (getHeight() - getIcon().getIconHeight()) / 2;
             parent.getGUI().displayOccupationIndicator(g, unit, x, y);
         } else if (unit.getLocation() instanceof ColonyTile) {
             int workType = unit.getWorkType();
             if (workType == Goods.FOOD && unit.getLocation() instanceof ColonyTile
                 && !((ColonyTile) unit.getLocation()).getWorkTile().isLand()) {
                 workType = Goods.FISH;
             }
 
             int production = unit.getFarmedPotential(unit.getWorkType(), ((ColonyTile) unit.getLocation())
                     .getWorkTile());
 
             ProductionLabel pl = new ProductionLabel(workType, production, getCanvas());
             g.translate(0, 10);
             pl.paintComponent(g);
             g.translate(0, -10);
        } else if (unit.isNaval() && unit.isUnderRepair()) {
             BufferedImage repairImage = parent.getGUI().createStringImage((Graphics2D) g,
                     Messages.message("underRepair", "%turns%", Integer.toString(unit.getTurnsForRepair())),
                     Color.RED, getWidth(), 12);
             g.drawImage(repairImage, (getWidth() - repairImage.getWidth()) / 2,
                     (getHeight() - repairImage.getHeight()) / 2, null);
         }
     }
 
     /**
      * Analyzes an event and calls the right external methods to take care of
      * the user's request.
      * 
      * @param event The incoming action event
      */
     public void actionPerformed(ActionEvent event) {
         String command = event.getActionCommand();
         try {
             if (command.startsWith("assign")) {
                 Unit teacher = (Unit) unit.getGame().getFreeColGameObject(command.substring(6));
                 inGameController.assignTeacher(unit, teacher);
                 Component uc = getParent();
                 while (uc != null) {
                     if (uc instanceof ColonyPanel) {
                         ((ColonyPanel) uc).reinitialize();
                         break;
                     }
                     uc = uc.getParent();
                 }
                 return;
             }
             int intCommand = Integer.valueOf(command).intValue();
             if (intCommand == ACTIVATE_UNIT) {
                 parent.getGUI().setActiveUnit(unit);
             } else if (intCommand == FORTIFY) {
                 inGameController.changeState(unit, Unit.FORTIFYING);
             } else if (intCommand == SENTRY) {
                 inGameController.changeState(unit, Unit.SENTRY);
             } else if (!unit.isCarrier()) {
                 switch (intCommand) {
                 case ARM:
                     inGameController.equipUnit(unit, Goods.MUSKETS, ((unit.isArmed()) ? 0 : 50));
                     break;
                 case MOUNT:
                     inGameController.equipUnit(unit, Goods.HORSES, ((unit.isMounted()) ? 0 : 50));
                     break;
                 case TOOLS:
                     int tools = 100;
                     if (!unit.isPioneer() && (event.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                         String s = getCanvas().showInputDialog("toolsEquip.text", "100", "ok", "cancel");
                         tools = -1;
 
                         while (s != null && tools == -1) {
                             try {
                                 tools = Integer.parseInt(s);
                             } catch (NumberFormatException e) {
                                 getCanvas().errorMessage("notANumber");
                                 s = getCanvas().showInputDialog("goodsTransfer.text", "100", "ok", "cancel");
                             }
                         }
                         if (s == null)
                             break;
                     }
                     inGameController.equipUnit(unit, Goods.TOOLS, ((unit.isPioneer()) ? 0 : tools));
                     break;
                 case DRESS:
                     inGameController.equipUnit(unit, Goods.CROSSES, ((unit.isMissionary()) ? 0 : 1));
                     break;
                 case WORKTYPE_FOOD:
                 case WORKTYPE_SUGAR:
                 case WORKTYPE_TOBACCO:
                 case WORKTYPE_COTTON:
                 case WORKTYPE_FURS:
                 case WORKTYPE_LUMBER:
                 case WORKTYPE_ORE:
                 case WORKTYPE_SILVER:
                     // Gets goodsType relative to FOOD
                     int goodsType = intCommand - WORKTYPE_FOOD + Goods.FOOD;
                     // Move unit to best producing ColonyTile
                     ColonyTile bestTile = unit.getColony().getVacantColonyTileFor(unit, goodsType);
                     inGameController.work(unit, bestTile);
                     // Change workType
                     inGameController.changeWorkType(unit, goodsType);
                     break;
                 case LEAVE_TOWN:
                     inGameController.putOutsideColony(unit);
                     break;
                 case CLEAR_SPECIALITY:
                     inGameController.clearSpeciality(unit);
                     break;
                 case COLOPEDIA:
                     getCanvas().showColopediaPanel(ColopediaPanel.COLOPEDIA_UNIT, unit.getType());
                     break;
                 default:
                     if (intCommand >= WORK_AT_SOMEWHERE && intCommand <= WORK_AT_LASTBUILDING ) {
                         int buildingType = intCommand - WORK_AT_SOMEWHERE ;
                         Building building = unit.getColony().getBuilding(buildingType);
                         inGameController.work(unit, building);
                     } else {
                         logger.warning("Invalid action");
                     }
                 }
                 setIcon(parent.getImageProvider().getUnitImageIcon(parent.getImageProvider().getUnitGraphicsType(unit)));
                 setDisabledIcon(parent.getImageProvider().getUnitImageIcon(
                         parent.getImageProvider().getUnitGraphicsType(unit), true));
 
                 Component uc = getParent();
                 while (uc != null) {
                     if (uc instanceof ColonyPanel) {
                         if (unit.getColony() == null) {
                             parent.remove(uc);
                             parent.getClient().getActionManager().update();
                         } else {
                             ((ColonyPanel) uc).reinitialize();
                         }
 
                         break;
                     } else if (uc instanceof EuropePanel) {
                         break;
                     }
 
                     uc = uc.getParent();
                 }
 
                 // repaint(0, 0, getWidth(), getHeight());
                 // uc.refresh();
             }
         } catch (NumberFormatException e) {
             logger.warning("Invalid action number");
         }
     }
 }
