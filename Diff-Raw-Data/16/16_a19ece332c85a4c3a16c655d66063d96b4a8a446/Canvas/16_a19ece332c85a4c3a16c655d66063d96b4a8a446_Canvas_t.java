 /**
  *  Copyright (C) 2002-2007  The FreeCol Team
  *
  *  This file is part of FreeCol.
  *
  *  FreeCol is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  FreeCol is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.sf.freecol.client.gui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.logging.Logger;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JDesktopPane;
 import javax.swing.JInternalFrame;
 import javax.swing.JLayeredPane;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.plaf.basic.BasicInternalFrameUI;
 
 import net.sf.freecol.client.FreeColClient;
 import net.sf.freecol.client.gui.action.MapControlsAction;
 import net.sf.freecol.client.gui.i18n.Messages;
 import net.sf.freecol.client.gui.panel.AboutPanel;
 import net.sf.freecol.client.gui.panel.ChatPanel;
 import net.sf.freecol.client.gui.panel.ChooseFoundingFatherDialog;
 import net.sf.freecol.client.gui.panel.ClientOptionsDialog;
 import net.sf.freecol.client.gui.panel.ColonyPanel;
 import net.sf.freecol.client.gui.panel.ColopediaPanel;
 import net.sf.freecol.client.gui.panel.DeclarationDialog;
 import net.sf.freecol.client.gui.panel.EmigrationPanel;
 import net.sf.freecol.client.gui.panel.ErrorPanel;
 import net.sf.freecol.client.gui.panel.EuropePanel;
 import net.sf.freecol.client.gui.panel.EventPanel;
 import net.sf.freecol.client.gui.panel.FreeColDialog;
 import net.sf.freecol.client.gui.panel.FreeColImageBorder;
 import net.sf.freecol.client.gui.panel.GameOptionsDialog;
 import net.sf.freecol.client.gui.panel.IndianSettlementPanel;
 import net.sf.freecol.client.gui.panel.LoadingSavegameDialog;
 import net.sf.freecol.client.gui.panel.MainPanel;
 import net.sf.freecol.client.gui.panel.MapControls;
 import net.sf.freecol.client.gui.panel.MapGeneratorOptionsDialog;
 import net.sf.freecol.client.gui.panel.MonarchPanel;
 import net.sf.freecol.client.gui.panel.NegotiationDialog;
 import net.sf.freecol.client.gui.panel.NewPanel;
 import net.sf.freecol.client.gui.panel.PurchaseDialog;
 import net.sf.freecol.client.gui.panel.QuitDialog;
 import net.sf.freecol.client.gui.panel.RecruitDialog;
 import net.sf.freecol.client.gui.panel.ReportCargoPanel;
 import net.sf.freecol.client.gui.panel.ReportColonyPanel;
 import net.sf.freecol.client.gui.panel.ReportContinentalCongressPanel;
 import net.sf.freecol.client.gui.panel.ReportForeignAffairPanel;
 import net.sf.freecol.client.gui.panel.ReportIndianPanel;
 import net.sf.freecol.client.gui.panel.ReportLabourPanel;
 import net.sf.freecol.client.gui.panel.ReportMilitaryPanel;
 import net.sf.freecol.client.gui.panel.ReportNavalPanel;
 import net.sf.freecol.client.gui.panel.ReportPanel;
 import net.sf.freecol.client.gui.panel.ReportReligiousPanel;
 import net.sf.freecol.client.gui.panel.ReportRequirementsPanel;
 import net.sf.freecol.client.gui.panel.ReportTradePanel;
 import net.sf.freecol.client.gui.panel.ReportTurnPanel;
 import net.sf.freecol.client.gui.panel.SelectAmountDialog;
 import net.sf.freecol.client.gui.panel.ServerListPanel;
 import net.sf.freecol.client.gui.panel.StartGamePanel;
 import net.sf.freecol.client.gui.panel.StatusPanel;
 import net.sf.freecol.client.gui.panel.TilePanel;
 import net.sf.freecol.client.gui.panel.TradeRouteDialog;
 import net.sf.freecol.client.gui.panel.TradeRouteInputDialog;
 import net.sf.freecol.client.gui.panel.TrainDialog;
 import net.sf.freecol.client.gui.panel.VictoryPanel;
 import net.sf.freecol.client.gui.panel.WarehouseDialog;
 import net.sf.freecol.common.ServerInfo;
 import net.sf.freecol.common.model.Colony;
 import net.sf.freecol.common.model.DiplomaticTrade;
 import net.sf.freecol.common.model.Europe;
 import net.sf.freecol.common.model.FoundingFather;
 import net.sf.freecol.common.model.FreeColGameObject;
 import net.sf.freecol.common.model.Game;
 import net.sf.freecol.common.model.GoodsType;
 import net.sf.freecol.common.model.IndianSettlement;
 import net.sf.freecol.common.model.LostCityRumour;
 import net.sf.freecol.common.model.Map;
 import net.sf.freecol.common.model.ModelMessage;
 import net.sf.freecol.common.model.Player;
 import net.sf.freecol.common.model.Settlement;
 import net.sf.freecol.common.model.Tile;
 import net.sf.freecol.common.model.TradeRoute;
 import net.sf.freecol.common.model.Unit;
 import net.sf.freecol.common.model.WorkLocation;
 import net.sf.freecol.common.model.Map.Position;
 import net.sf.freecol.server.generator.MapGeneratorOptions;
 
 /**
  * The main container for the other GUI components in FreeCol. This container is
  * where the panels, dialogs and menus are added. In addition, this is the
  * component in which the map graphics are displayed. <br>
  * <b>Displaying panels and a dialogs</b> <br>
  * <br>
  * <code>Canvas</code> contains methods to display various panels and dialogs.
  * Most of these methods use {@link net.sf.freecol.client.gui.i18n i18n} to get
  * localized text. Here is an example: <br>
  * 
  * <PRE>
  * 
  * if (canvas.showConfirmDialog("choice.text", "choice.yes", "choice.no")) { //
  * DO SOMETHING. }
  * 
  * </PRE>
  * 
  * <br>
  * where "choice.text", "choice.yes" and "choice.no" are keys for a localized
  * message. See {@link net.sf.freecol.client.gui.i18n i18n} for more
  * information. <br>
  * <b>The difference between a panel and a dialog</b> <br>
  * <br>
  * When displaying a dialog, using a <code>showXXXDialog</code>, the calling
  * thread will wait until that dialog is dismissed before returning. In
  * contrast, a <code>showXXXPanel</code>-method returns immediately.
  */
 public final class Canvas extends JDesktopPane {
     private static final Logger logger = Logger.getLogger(Canvas.class.getName());
 
 
 
 
     private static final Integer MAIN_LAYER = JLayeredPane.DEFAULT_LAYER;
 
     private static final Integer STATUS_LAYER = JLayeredPane.POPUP_LAYER;
 
     private static final int EXIT = 0, RECRUIT = 1, PURCHASE = 2, TRAIN = 3, UNLOAD = 4;
 
     /**
      * To save the most recently open dialog in Europe
      * (<code>RecruitDialog</code>, <code>PurchaseDialog</code>, <code>TrainDialog</code>)
      */
     private FreeColDialog europeOpenDialog = null;
 
     private final FreeColClient freeColClient;
 
     private final MainPanel mainPanel;
 
     private final NewPanel newPanel;
 
     private final StartGamePanel startGamePanel;
 
     private final EuropePanel europePanel;
 
     private final StatusPanel statusPanel;
 
     private final ChatPanel chatPanel;
     
     private final AboutPanel aboutPanel;
 
     private final GUI gui;
 
     private final VictoryPanel victoryPanel;
 
     private final ServerListPanel serverListPanel;
 
     private final ClientOptionsDialog clientOptionsDialog;
 
     private final LoadingSavegameDialog loadingSavegameDialog;
     
     private final GUI colonyTileGUI;
 
     private JMenuBar jMenuBar;
 
     private boolean clientOptionsDialogShowing = false;
 
     /**
      * Variable used for detecting resizing.
      */
     private Dimension oldSize = null;
     
     private Dimension initialSize = null;
 
     /**
      * The constructor to use.
      * 
      * @param client main control class.
      * @param size The bounds of this <code>Canvas</code>.
      * @param gui The object responsible of drawing the map onto this component.
      */
     public Canvas(FreeColClient client, Dimension size, GUI gui) {
         this.freeColClient = client;
         this.gui = gui;
 
         colonyTileGUI = new GUI(client, size, freeColClient.getImageLibrary());
 
         initialSize = size;
 
         setLocation(0, 0);
         setSize(size);
         
         setDoubleBuffered(true);
         setOpaque(false);
         setLayout(null);
 
         mainPanel = new MainPanel(this, freeColClient);
         newPanel = new NewPanel(this, freeColClient.getConnectController());
         startGamePanel = new StartGamePanel(this, freeColClient);
         serverListPanel = new ServerListPanel(this, freeColClient, freeColClient.getConnectController());
         europePanel = new EuropePanel(this, freeColClient, freeColClient.getInGameController());
         statusPanel = new StatusPanel(this);
         chatPanel = new ChatPanel(this, freeColClient);
         victoryPanel = new VictoryPanel(this, freeColClient);
         clientOptionsDialog = new ClientOptionsDialog(this, freeColClient);
         loadingSavegameDialog = new LoadingSavegameDialog(this, freeColClient);
         aboutPanel = new AboutPanel(this, freeColClient);
 
         setFocusable(true);
         setFocusTraversalKeysEnabled(false);
         // takeFocus();
 
         // chatDisplayThread = new ChatDisplayThread();
         // chatDisplayThread.start();
 
         // TODO: move shutdown hook from GUI to (say) client!
         Runtime runtime = Runtime.getRuntime();
         runtime.addShutdownHook(new Thread() {
             @Override
             public void run() {
                 freeColClient.getConnectController().quitGame(true);
             }
         });
 
         logger.info("Canvas created.");
     }
 
     @Override
     public Dimension getMinimumSize() {
         return new Dimension(640, 480);
     }
 
     @Override
     public Dimension getPreferredSize() {
         return initialSize;
     }
     
     public GUI getColonyTileGUI() {
         return colonyTileGUI;
     }
 
     /**
      * Returns the <code>ClientOptionsDialog</code>.
      * 
      * @return The <code>ClientOptionsDialog</code>
      * @see net.sf.freecol.client.ClientOptions
      */
     public ClientOptionsDialog getClientOptionsDialog() {
         return clientOptionsDialog;
     }
 
     /**
      * Sets the menu bar. The menu bar will be resized to fit the width of the
      * gui and made visible.
      * 
      * @param mb The menu bar.
      * @see FreeColMenuBar
      */
     public void setJMenuBar(JMenuBar mb) {
         if (jMenuBar != null) {
             remove(jMenuBar);
         }
 
         mb.setLocation(0, 0);
         mb.setSize(getWidth(), (int) mb.getPreferredSize().getHeight());
         add(mb);
 
         jMenuBar = mb;
     }
 
     /**
      * Gets the menu bar.
      * 
      * @return The menu bar.
      * @see FreeColMenuBar
      */
     public JMenuBar getJMenuBar() {
         return jMenuBar;
     }
 
     /**
      * Updates the label displaying the current amount of gold.
      */
     public void updateGoldLabel() {
         getJMenuBar().repaint();
     }
 
     /**
      * Updates the sizes of the components on this Canvas.
      */
     public void updateSizes() {
         if (oldSize == null) {
             oldSize = getSize();
         }
         if (oldSize.width != getWidth() || oldSize.height != getHeight()) {
             if (jMenuBar != null) {
                 jMenuBar.setSize(getWidth(), (int) jMenuBar.getPreferredSize().getHeight());
             }
             MapControlsAction mca = (MapControlsAction) freeColClient.getActionManager().getFreeColAction(MapControlsAction.id);
             MapControls mc = mca.getMapControls();
             if (mc != null && mc.isShowing()) {
                 mc.removeFromComponent(this);
                 mc.addToComponent(this);
             }
             if (europePanel != null) {
                 JInternalFrame f = getInternalFrame(europePanel);
                 if (f != null) {
                     f.setSize(getWidth(), getHeight() - getMenuBarHeight());
                     f.setLocation(0, getMenuBarHeight());
                 }
             }
             gui.setSize(getSize());
            gui.forceReposition();
             oldSize = getSize();
         }
     }
     
     /**
      * Paints this component. This method will use {@link GUI#display} to draw
      * the map/background on this component.
      * 
      * @param g The Graphics context in which to draw this component.
      * @see GUI#display
      */
     @Override
     public void paintComponent(Graphics g) {
         updateSizes();
         Graphics2D g2d = (Graphics2D) g;
         gui.display(g2d);
     }
 
     /**
      * Gets the height of the menu bar.
      * 
      * @return The menubar + any borders.
      */
     public int getMenuBarHeight() {
         if (jMenuBar == null) {
             return 0;
         } else if (jMenuBar instanceof FreeColMenuBar) {
             return ((FreeColMenuBar) jMenuBar).getOpaqueHeight();
         } else {
             return jMenuBar.getHeight();
         }
     }
 
     /**
      * Displays the <code>StartGamePanel</code>.
      * 
      * @param game The <code>Game</code> that is about to start.
      * @param player The <code>Player</code> using this client.
      * @param singlePlayerMode 'true' if the user wants to start a single player
      *            game, 'false' otherwise.
      * @see StartGamePanel
      */
     public void showStartGamePanel(Game game, Player player, boolean singlePlayerMode, int players,
                                    int advantages, boolean additionalNations) {
         closeMenus();
 
         if (game != null && player != null) {
             startGamePanel.initialize(singlePlayerMode, additionalNations, advantages);
             addAsFrame(startGamePanel);
             startGamePanel.requestFocus();
         } else {
             logger.warning("Tried to open 'StartGamePanel' without having 'game' and/or 'player' set.");
         }
     }
 
     /**
      * Displays the <code>ServerListPanel</code>.
      * 
      * @param username The username that should be used when connecting to one
      *            of the servers on the list.
      * @param serverList The list containing the servers retrived from the
      *            metaserver.
      * @see ServerListPanel
      */
     public void showServerListPanel(String username, ArrayList<ServerInfo> serverList) {
         closeMenus();
 
         serverListPanel.initialize(username, serverList);
         addAsFrame(serverListPanel);
         serverListPanel.requestFocus();
     }
 
     /**
      * Displays the <code>VictoryPanel</code>.
      * 
      * @see VictoryPanel
      */
     public void showVictoryPanel() {
         closeMenus();
         victoryPanel.initialize();
         addAsFrame(victoryPanel);
         victoryPanel.requestFocus();
     }
 
     /**
      * Displays the <code>WarehouseDialog</code>.
      * 
      * @param colony the colony for which to show the WarehouseDialog.
      * @see WarehouseDialog
      */
     public boolean showWarehouseDialog(Colony colony) {
         WarehouseDialog warehouseDialog = new WarehouseDialog(this);
         warehouseDialog.initialize(colony);
 
         // TODO: Not a standard dialog, special treatment for now.
         addAsFrame(warehouseDialog);
         warehouseDialog.requestFocus();
  
         boolean response = warehouseDialog.getResponseBoolean();
         remove(warehouseDialog);
         return response;
     }
 
     /**
      * Displays the <code>NegotiationDialog</code>.
      * 
      * @see NegotiationDialog
      */
     public DiplomaticTrade showNegotiationDialog(Unit unit, Settlement settlement, DiplomaticTrade agreement) {
         NegotiationDialog negotiationDialog = new NegotiationDialog(this, unit, settlement, agreement);
         negotiationDialog.initialize();
 
         // TODO: Not a standard dialog, special treatment for now.
         addAsFrame(negotiationDialog);
         DiplomaticTrade offer = (DiplomaticTrade) negotiationDialog.getResponse();
         remove(negotiationDialog);
         return offer;
     }
 
     /**
      * Displays the <code>AboutPanel</code>.
      * 
      * @see AboutPanel
      */
     public void showAboutPanel() {
         closeMenus();
         addAsFrame(aboutPanel);
         aboutPanel.requestFocus();
     }
     
     /**
      * Displays the <code>ChatPanel</code>.
      * 
      * @see ChatPanel
      */
     public void showChatPanel() {
         closeMenus();
         addAsFrame(chatPanel);
         chatPanel.requestFocus();
     }
 
     /**
      * Displays the <code>NewGamePanel</code>.
      * 
      * @see NewPanel
      */
     public void showNewGamePanel() {
         closeMenus();
         addAsFrame(newPanel);
         newPanel.requestFocus();
     }
 
     /**
      * Displays the <code>SelectAmountDialog</code>.
      * @param goodsType an <code>int</code> value
      * @param available an <code>int</code> value
      * @param needToPay a <code>boolean</code> value
      * @return an <code>int</code> value
      */
     public int showSelectAmountDialog(GoodsType goodsType, int available, boolean needToPay) {
         SelectAmountDialog dialog = new SelectAmountDialog(this, goodsType, available, needToPay);
         dialog.initialize();
         addAsFrame(dialog);
         dialog.requestFocus();
         int amount = dialog.getResponseInt();
         remove(dialog);
         return amount;
     }
 
 
     /**
      * Displays a <code>ModelMessage</code> in a modal dialog. The message is
      * displayed in this way:
      * 
      * <ol>
      * <li>The <code>messageID</code> is used to get the message from
      * {@link net.sf.freecol.client.gui.i18n.Messages#message(String)}.
      * <li>Every occuranse of <code>data[x][0]</code> is replaced with
      * <code>data[x][1]</code> for every <code>x</code>.
      * <li>The message is displayed using a modal dialog.
      * </ol>
      * 
      * A specialized panel may be used. In this case the <code>messageID</code>
      * of the <code>ModelMessage</code> if used as a key for this panel.
      * 
      * @param m The <code>ModelMessage</code> to be displayed.
      */
     /*
      * public void oldshowModelMessage(ModelMessage m) { String okText = "ok";
      * String cancelText = "display"; String message = m.getMessageID();
      * 
      * if (message.equals("EventPanel.MEETING_EUROPEANS")) { // Skip for now:
      * //showEventDialog(EventPanel.MEETING_EUROPEANS);
      * freeColClient.getInGameController().nextModelMessage(); } else if
      * (message.equals("EventPanel.MEETING_NATIVES")) { // Skip for now:
      * //showEventDialog(EventPanel.MEETING_NATIVES);
      * freeColClient.getInGameController().nextModelMessage(); } else if
      * (message.equals("EventPanel.MEETING_AZTEC")) { // Skip for now:
      * //showEventDialog(EventPanel.MEETING_AZTEC);
      * freeColClient.getInGameController().nextModelMessage(); } else if
      * (message.equals("EventPanel.MEETING_INCA")) { // Skip for now:
      * //showEventDialog(EventPanel.MEETING_INCA);
      * freeColClient.getInGameController().nextModelMessage(); } else { try {
      * okText = Messages.message(okText); cancelText =
      * Messages.message(cancelText); message = Messages.message(message,
      * m.getData()); } catch (MissingResourceException e) {
      * logger.warning("could not find message with id: " + okText + "."); }
      * 
      * FreeColGameObject source = m.getSource(); if (source instanceof Europe &&
      * !europePanel.isShowing() || (source instanceof Colony || source
      * instanceof WorkLocation) && !colonyPanel.isShowing()) {
      * 
      * FreeColDialog confirmDialog = FreeColDialog.createConfirmDialog(message,
      * okText, cancelText); addCentered(confirmDialog, MODEL_MESSAGE_LAYER);
      * confirmDialog.requestFocus();
      * 
      * if (!confirmDialog.getResponseBoolean()) { remove(confirmDialog); if
      * (source instanceof Europe) { showEuropePanel(); } else if (source
      * instanceof Colony) { showColonyPanel((Colony) source); } else if (source
      * instanceof WorkLocation) { showColonyPanel(((WorkLocation)
      * source).getColony()); } } else { remove(confirmDialog);
      * freeColClient.getInGameController().nextModelMessage(); } } else {
      * FreeColDialog informationDialog = null; if (m.getTypeOfGoods() < 0) {
      * informationDialog = FreeColDialog.createInformationDialog(message,
      * okText); } else { ImageIcon image =
      * getImageProvider().getGoodsImageIcon(m.getTypeOfGoods());
      * informationDialog = FreeColDialog.createInformationDialog(message,
      * okText, image); }
      * 
      * addCentered(informationDialog, MODEL_MESSAGE_LAYER);
      * informationDialog.requestFocus();
      * 
      * informationDialog.getResponse(); remove(informationDialog);
      * 
      * freeColClient.getInGameController().nextModelMessage(); } } }
      */
 
     /**
      * Displays all model messages for the current turn.
      * 
      * @param messages A list of messages to display.
      */
     public void showTurnReport(ArrayList<ModelMessage> messages) {
         final ReportTurnPanel reportTurnPanel = new ReportTurnPanel(this);
         reportTurnPanel.initialize(messages);
         addAsFrame(reportTurnPanel);
         reportTurnPanel.requestFocus();
     }
 
     /**
      * Displays a single ModelMessage.
      * 
      * @param modelMessage The message to display.
      */
     public void showModelMessage(ModelMessage modelMessage) {
         showModelMessages(new ModelMessage[] { modelMessage });
     }
 
     /**
      * Displays a number of ModelMessages.
      * 
      * @param modelMessages
      */
     public void showModelMessages(ModelMessage[] modelMessages) {
         String okText = "ok";
         String cancelText = "display";
         String[] messageText = new String[modelMessages.length];
         ImageIcon[] messageIcon = new ImageIcon[modelMessages.length];
         try {
             okText = Messages.message(okText);
         } catch (MissingResourceException e) {
             logger.warning("could not find message with id: " + okText + ".");
         }
         try {
             cancelText = Messages.message(cancelText);
         } catch (MissingResourceException e) {
             logger.warning("could not find message with id: " + cancelText + ".");
         }
         for (int i = 0; i < modelMessages.length; i++) {
             try {
                 messageText[i] = Messages.message(modelMessages[i].getId(), modelMessages[i].getData());
             } catch (MissingResourceException e) {
                 logger.warning("could not find message with id: " + modelMessages[i].getId() + ".");
             }
 
             messageIcon[i] = getImageIcon(modelMessages[i].getDisplay(), false);
         }
 
         // source should be the same for all messages
         FreeColGameObject source = modelMessages[0].getSource();
         if ((source instanceof Europe && !europePanel.isShowing())
                 || (source instanceof Colony || source instanceof WorkLocation)) {
 
             FreeColDialog confirmDialog = FreeColDialog.createConfirmDialog(messageText, messageIcon, okText,
                     cancelText);
             addAsFrame(confirmDialog);
             confirmDialog.requestFocus();
 
             if (!confirmDialog.getResponseBoolean()) {
                 remove(confirmDialog);
                 if (source instanceof Europe) {
                     showEuropePanel();
                 } else if (source instanceof Colony) {
                     showColonyPanel((Colony) source);
                 } else if (source instanceof WorkLocation) {
                     showColonyPanel(((WorkLocation) source).getColony());
                 }
             } else {
                 remove(confirmDialog);
                 if (!isShowingSubPanel()) {
                     freeColClient.getInGameController().nextModelMessage();
                 }
             }
         } else {
             FreeColDialog informationDialog = FreeColDialog.createInformationDialog(messageText, messageIcon);
             addAsFrame(informationDialog);
             informationDialog.requestFocus();
 
             informationDialog.getResponse();
             remove(informationDialog);
 
             if (!isShowingSubPanel()) {
                 freeColClient.getInGameController().nextModelMessage();
             }
         }
     }
 
     /**
      * Returns the appropriate ImageIcon for Object.
      * 
      * @param display The Object to display.
      * @return The appropriate ImageIcon.
      */
     public ImageIcon getImageIcon(Object display, boolean small) {
         ImageLibrary imageLibrary = getGUI().getImageLibrary();
         Image image = null;
         if (display == null) {
             return new ImageIcon();
         } else if (display instanceof GoodsType) {
             GoodsType goodsType = (GoodsType) display;
             try {
                 image = imageLibrary.getGoodsImage(goodsType);
             } catch (Exception e) {
                 logger.warning("could not find image for goods " + goodsType.getName());
             }
         } else if (display instanceof Unit) {
             Unit unit = (Unit) display;
             try {
                 int unitType = imageLibrary.getUnitGraphicsType(unit);
                 image = imageLibrary.getUnitImage(unitType);
             } catch (Exception e) {
                 logger.warning("could not find image for unit " + unit.getName());
             }
         } else if (display instanceof Settlement) {
             Settlement settlement = (Settlement) display;
             try {
                 int settlementType = imageLibrary.getSettlementGraphicsType(settlement);
                 if (display instanceof Colony) {
                     image = imageLibrary.getColonyImage(settlementType);
                 } else {
                     image = imageLibrary.getIndianSettlementImage(settlementType);
                 }
             } catch (Exception e) {
                 logger.warning("could not find image for settlement " + settlement);
             }
         } else if (display instanceof LostCityRumour) {
             try {
                 image = imageLibrary.getMiscImage(ImageLibrary.LOST_CITY_RUMOUR);
             } catch (Exception e) {
                 logger.warning("could not find image for lost city rumour");
             }
         } else if (display instanceof Player) {
             image = (Image) UIManager.get(((Player) display).getNationID() + ".coatOfArms.image");
         } 
         if (image != null && small) {
             return new ImageIcon(image.getScaledInstance((image.getWidth(null) / 3) * 2,
                                                          (image.getHeight(null) / 3) *2,
                                                          Image.SCALE_SMOOTH));
         } else {
             return (image != null) ? new ImageIcon(image) : null;
         }
     }
 
     /**
      * Shows the <code>DeclarationDialog</code>.
      * 
      * @see DeclarationDialog
      */
     public void showDeclarationDialog() {
         DeclarationDialog declarationDialog = new DeclarationDialog(this, freeColClient);
 
         declarationDialog.initialize();
 
         addAsFrame(declarationDialog);
         declarationDialog.requestFocus();
 
         declarationDialog.getResponseBoolean();
         remove(declarationDialog);
 
     }
 
     /**
      * Displays a dialog with a text and a ok/cancel option.
      * 
      * @param text The text that explains the choice for the user.
      * @param okText The text displayed on the "ok"-button.
      * @param cancelText The text displayed on the "cancel"-button.
      * @return <i>true</i> if the user clicked the "ok"-button and <i>false</i>
      *         otherwise.
      * @see FreeColDialog
      */
     public boolean showConfirmDialog(String text, String okText, String cancelText) {
         return showConfirmDialog(text, okText, cancelText, null);
     }
     
     /**
      * Displays the given dialog.
      * 
      * @param freeColDialog The dialog to be displayed
      * @return The {@link FreeColDialog#getResponse reponse} returned by the dialog.
      */
     public Object showFreeColDialog(FreeColDialog freeColDialog) {
         addAsFrame(freeColDialog);
         freeColDialog.requestFocus();
         Object response = freeColDialog.getResponse();
         remove(freeColDialog);
 
         return response;
     }
 
     /**
      * Displays a dialog with a text and a ok/cancel option.
      * 
      * @param text The text that explains the choice for the user.
      * @param okText The text displayed on the "ok"-button.
      * @param cancelText The text displayed on the "cancel"-button.
      * @param replace An array of strings that will be inserted somewhere in the
      *            text.
      * @return <i>true</i> if the user clicked the "ok"-button and <i>false</i>
      *         otherwise.
      * @see FreeColDialog
      */
     public boolean showConfirmDialog(String text, String okText, String cancelText, String[][] replace) {
         try {
             text = Messages.message(text, replace);
             okText = Messages.message(okText);
             cancelText = Messages.message(cancelText);
         } catch (MissingResourceException e) {
             logger.warning("could not find message with id: " + text + ", " + okText + " or " + cancelText + ".");
         }
 
         FreeColDialog confirmDialog = FreeColDialog.createConfirmDialog(text, okText, cancelText);
         addAsFrame(confirmDialog);
         confirmDialog.requestFocus();
 
         boolean response = confirmDialog.getResponseBoolean();
 
         remove(confirmDialog);
 
         return response;
     }
 
     /**
      * Displays the <code>showPreCombatDialog</code>.
      * 
      * @param attacker an <code>Unit</code> value
      * @param defender an <code>Unit</code> value
      * @param settlement a <code>Settlement</code> value
      * @return a <code>boolean</code> value
      */
     public boolean showPreCombatDialog(Unit attacker, Unit defender, Settlement settlement) {
 
         FreeColDialog preCombatDialog = FreeColDialog.createPreCombatDialog(attacker, defender, settlement, this);
         addAsFrame(preCombatDialog);
         preCombatDialog.requestFocus();
 
         boolean response = preCombatDialog.getResponseBoolean();
 
         remove(preCombatDialog);
 
         return response;
     }
 
     /**
      * Displays a dialog with a text and a ok/cancel option.
      * 
      * @param messages The messages that explains the choice for the user.
      * @param okText The text displayed on the "ok"-button.
      * @param cancelText The text displayed on the "cancel"-button.
      * @return <i>true</i> if the user clicked the "ok"-button and <i>false</i>
      *         otherwise.
      * @see FreeColDialog
      */
     public boolean showConfirmDialog(ModelMessage[] messages, String okText, String cancelText) {
         try {
             okText = Messages.message(okText);
         } catch (MissingResourceException e) {
             logger.warning("could not find message with id: " + okText + ".");
         }
         try {
             cancelText = Messages.message(cancelText);
         } catch (MissingResourceException e) {
             logger.warning("could not find message with id: " + cancelText + ".");
         }
 
         String[] texts = new String[messages.length];
         ImageIcon[] images = new ImageIcon[messages.length];
         for (int i = 0; i < messages.length; i++) {
             String id = messages[i].getId();
             try {
                 texts[i] = Messages.message(id);
             } catch (MissingResourceException e) {
                 logger.warning("could not find message with id: " + id + ".");
             }
             images[i] = getImageIcon(messages[i].getDisplay(), false);
         }
 
         FreeColDialog confirmDialog = FreeColDialog.createConfirmDialog(texts, images, okText, cancelText);
         addAsFrame(confirmDialog);
         confirmDialog.requestFocus();
 
         boolean response = confirmDialog.getResponseBoolean();
 
         remove(confirmDialog);
 
         return response;
     }
 
     /**
      * Checks if the <code>ClientOptionsDialog</code> is visible.
      * 
      * @return <code>true</code> if no internal frames are open.
      */
     public boolean isClientOptionsDialogShowing() {
         return clientOptionsDialogShowing;
     }
 
     /**
      * Checks if mapboard actions should be enabled.
      * 
      * @return <code>true</code> if no internal frames are open.
      */
     public boolean isMapboardActionsEnabled() {
         return !isShowingSubPanel();
     }
 
     /**
      * Checks if this <code>Canvas</code> displaying another panel.
      * <p>
      * Note that the previous implementation could throw exceptions
      * in some cases, thus the change.
      * 
      * @return <code>true</code> if the <code>Canvas</code> is displaying an
      *         internal frame.
      */
     public boolean isShowingSubPanel() {
         Component[] components = getComponents();
         for (Component c : components) {
             if (c instanceof ToolBoxFrame) {
                 continue;
             }
             if (c instanceof JInternalFrame) {
                 return true;
             } else if (c instanceof JInternalFrame.JDesktopIcon) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Displays a dialog for setting game options.
      * 
      * @return <code>true</code> if the game options have been modified, and
      *         <code>false</code> otherwise.
      */
     public boolean showGameOptionsDialog(boolean editable) {
         GameOptionsDialog gameOptionsDialog = new GameOptionsDialog(this, freeColClient);
         gameOptionsDialog.initialize(editable);
 
         // addCentered(gameOptionsDialog, GAME_OPTIONS_LAYER);
         // gameOptionsDialog.requestFocus();
 
         addAsFrame(gameOptionsDialog);
         gameOptionsDialog.requestFocus();
 
         boolean r = gameOptionsDialog.getResponseBoolean();
 
         remove(gameOptionsDialog);
 
         // remove(gameOptionsDialog);
 
         return r;
     }
 
     /**
      * Gets the <code>LoadingSavegameDialog</code>.
      * 
      * @return The <code>LoadingSavegameDialog</code>.
      */
     public LoadingSavegameDialog getLoadingSavegameDialog() {
         return loadingSavegameDialog;
     }
 
     /**
      * Displays a dialog for setting options when loading a savegame. The
      * settings can be retrived directly from {@link LoadingSavegameDialog}
      * after calling this method.
      * 
      * @param publicServer Default value.
      * @param singleplayer Default value.
      * @return <code>true</code> if the "ok"-button was pressed and
      *         <code>false</code> otherwise.
      */
     public boolean showLoadingSavegameDialog(boolean publicServer, boolean singleplayer) {
         loadingSavegameDialog.initialize(publicServer, singleplayer);
 
         addAsFrame(loadingSavegameDialog);
         loadingSavegameDialog.requestFocus();
 
         boolean r = loadingSavegameDialog.getResponseBoolean();
         remove(loadingSavegameDialog);
 
         return r;
     }
 
     /**
      * Displays a dialog for setting client options.
      * 
      * @return <code>true</code> if the client options have been modified, and
      *         <code>false</code> otherwise.
      */
     public boolean showClientOptionsDialog() {
         clientOptionsDialog.initialize();
 
         clientOptionsDialogShowing = true;
         addAsFrame(clientOptionsDialog);
         clientOptionsDialog.requestFocus();
         boolean r = clientOptionsDialog.getResponseBoolean();
         remove(clientOptionsDialog);
         clientOptionsDialogShowing = false;
         freeColClient.getActionManager().update();
 
         return r;
     }
 
     /**
      * Displays a dialog for setting the map generator options.
      * 
      * @param editable The options are only allowed to be changed if this
      *            variable is <code>true</code>.
      * @return <code>true</code> if the options have been modified, and
      *         <code>false</code> otherwise.
      */
     public boolean showMapGeneratorOptionsDialog(boolean editable) {
         final MapGeneratorOptions mgo = freeColClient.getPreGameController().getMapGeneratorOptions();
         return showMapGeneratorOptionsDialog(editable, mgo);
     }
     
     /**
      * Displays a dialog for setting the map generator options.
      * 
      * @param editable The options are only allowed to be changed if this
      *            variable is <code>true</code>.
      * @return <code>true</code> if the options have been modified, and
      *         <code>false</code> otherwise.
      */
     public boolean showMapGeneratorOptionsDialog(boolean editable, MapGeneratorOptions mgo) {
         MapGeneratorOptionsDialog mapGeneratorOptionsDialog = new MapGeneratorOptionsDialog(this, freeColClient);
         mapGeneratorOptionsDialog.initialize(editable, mgo);
 
         addAsFrame(mapGeneratorOptionsDialog);
         mapGeneratorOptionsDialog.requestFocus();
         boolean r = mapGeneratorOptionsDialog.getResponseBoolean();
         remove(mapGeneratorOptionsDialog);
 
         return r;
     }
 
     /**
      * Displays a dialog where the user may choose a file. This is the same as
      * calling:
      * 
      * <br>
      * <br>
      * <code>
      * showLoadDialog(directory, new FileFilter[] {FreeColDialog.getFSGFileFilter()});
      * </code>
      * 
      * @param directory The directory containing the files.
      * @return The <code>File</code>.
      * @see FreeColDialog
      */
     public File showLoadDialog(File directory) {
         return showLoadDialog(directory, new FileFilter[] { FreeColDialog.getFSGFileFilter() });
     }
 
     /**
      * Displays a dialog where the user may choose a file.
      * 
      * @param directory The directory containing the files.
      * @param fileFilters The file filters which the user can select in the
      *            dialog.
      * @return The <code>File</code>.
      * @see FreeColDialog
      */
     public File showLoadDialog(File directory, FileFilter[] fileFilters) {
         FreeColDialog loadDialog = FreeColDialog.createLoadDialog(directory, fileFilters);
 
         addAsFrame(loadDialog);
         loadDialog.requestFocus();
 
         File response = (File) loadDialog.getResponse();
 
         while (response != null && !response.isFile()) {
             errorMessage("noSuchFile");
             response = (File) loadDialog.getResponse();
         }
 
         remove(loadDialog);
 
         return response;
     }
 
     /**
      * Displays a dialog where the user may choose a filename. This is the same
      * as calling:
      * 
      * <br>
      * <br>
      * <code>
      * showSaveDialog(directory, new FileFilter[] {FreeColDialog.getFSGFileFilter()}, defaultName);
      * </code>
      * 
      * @param directory The directory containing the files in which the user may
      *            overwrite.
      * @param defaultName Default filename for the savegame.
      * @return The <code>File</code>.
      * @see FreeColDialog
      */
     public File showSaveDialog(File directory, String defaultName) {
         return showSaveDialog(directory, ".fsg", new FileFilter[] { FreeColDialog.getFSGFileFilter() }, defaultName);
     }
 
     /**
      * Displays a dialog where the user may choose a filename.
      * 
      * @param directory The directory containing the files in which the user may
      *            overwrite.
      * @param standardName This extension will be added to the specified
      *            filename (if not added by the user).
      * @param fileFilters The available file filters in the dialog.
      * @param defaultName Default filename for the savegame.
      * @return The <code>File</code>.
      * @see FreeColDialog
      */
     public File showSaveDialog(File directory, String standardName, FileFilter[] fileFilters, String defaultName) {
         FreeColDialog saveDialog = FreeColDialog.createSaveDialog(directory, standardName, fileFilters, defaultName);
         addAsFrame(saveDialog);
         saveDialog.requestFocus();
 
         File response = (File) saveDialog.getResponse();
 
         remove(saveDialog);
 
         return response;
     }
 
     /**
      * Displays a dialog that asks the user what he wants to do with his scout
      * in the indian settlement.
      * 
      * @param settlement The indian settlement that is being scouted.
      * 
      * @return FreeColDialog.SCOUT_INDIAN_SETTLEMENT_CANCEL if the action was
      *         cancelled, FreeColDialog.SCOUT_INDIAN_SETTLEMENT_SPEAK if he
      *         wants to speak with the chief,
      *         FreeColDialog.SCOUT_INDIAN_SETTLEMENT_TRIBUTE if he wants to
      *         demand tribute, FreeColDialog.SCOUT_INDIAN_SETTLEMENT_ATTACK if
      *         he wants to attack the settlement.
      */
     public int showScoutIndianSettlementDialog(IndianSettlement settlement) {
         FreeColDialog scoutDialog = FreeColDialog.createScoutIndianSettlementDialog(settlement,
                 freeColClient.getMyPlayer());
         addAsFrame(scoutDialog);
         scoutDialog.requestFocus();
 
         int response = scoutDialog.getResponseInt();
 
         remove(scoutDialog);
 
         return response;
     }
 
     /**
      * Displays a dialog that asks the user what he wants to do with his scout
      * in the foreign colony.
      * 
      * @param colony The foreign colony that is being scouted.
      * 
      * @return FreeColDialog.SCOUT_FOREIGN_COLONY_CANCEL if the action was
      *         cancelled, FreeColDialog.SCOUT_FOREIGN_COLONY_NEGOTIATE if he
      *         wants to negotaite with the foreign power,
      *         FreeColDialog.SCOUT_FOREIGN_COLONY_SPY if he wants to
      *         spy the colony, FreeColDialog.SCOUT_FOREIGN_COLONY_ATTACK if
      *         he wants to attack the colony.
      */
     public int showScoutForeignColonyDialog(Colony colony, Unit unit) {
         FreeColDialog scoutDialog = FreeColDialog.createScoutForeignColonyDialog(colony,
                 unit);
         addAsFrame(scoutDialog);
         scoutDialog.requestFocus();
 
         int response = scoutDialog.getResponseInt();
 
         remove(scoutDialog);
 
         return response;
     }
 
     /**
      * Displays a dialog that asks the user what he wants to do with his armed
      * unit in the indian settlement.
      * 
      * @param settlement The indian settlement that is going to be attacked or
      *            demanded.
      * 
      * @return FreeColDialog.SCOUT_INDIAN_SETTLEMENT_CANCEL if the action was
      *         cancelled, FreeColDialog.SCOUT_INDIAN_SETTLEMENT_TRIBUTE if he
      *         wants to demand tribute,
      *         FreeColDialog.SCOUT_INDIAN_SETTLEMENT_ATTACK if he wants to
      *         attack the settlement.
      */
     public int showArmedUnitIndianSettlementDialog(IndianSettlement settlement) {
         FreeColDialog armedUnitDialog = FreeColDialog.createArmedUnitIndianSettlementDialog(settlement, freeColClient
                 .getMyPlayer());
         addAsFrame(armedUnitDialog);
         armedUnitDialog.requestFocus();
 
         int response = armedUnitDialog.getResponseInt();
 
         remove(armedUnitDialog);
 
         return response;
     }
 
     /**
      * Displays a dialog that asks the user what he wants to do with his
      * missionary in the indian settlement.
      * 
      * @param settlement The indian settlement that is being visited.
      * 
      * @return ArrayList with an Integer and optionally a Player refencing the
      *         player to attack in case of "incite indians". Integer can be any
      *         of: FreeColDialog.MISSIONARY_ESTABLISH if he wants to establish a
      *         mission, FreeColDialog.MISSIONARY_DENOUNCE_AS_HERESY if he wants
      *         to denounce the existing (foreign) mission as heresy,
      *         FreeColDialog.MISSIONARY_INCITE_INDIANS if he wants to incite the
      *         indians (requests their support for war against another European
      *         power), FreeColDialog.MISSIONARY_CANCEL if the action was
      *         cancelled.
      */
     public List<Object> showUseMissionaryDialog(IndianSettlement settlement) {
         FreeColDialog missionaryDialog = FreeColDialog.createUseMissionaryDialog(settlement, freeColClient
                 .getMyPlayer());
         addAsFrame(missionaryDialog);
         missionaryDialog.requestFocus();
 
         Integer response = (Integer) missionaryDialog.getResponse();
         ArrayList<Object> returnValue = new ArrayList<Object>();
         // TODO: Find a solution so that we can use a more specialized list.
         returnValue.add(response);
 
         remove(missionaryDialog);
 
         if (response.intValue() == FreeColDialog.MISSIONARY_INCITE_INDIANS) {
             FreeColDialog inciteDialog = FreeColDialog.createInciteDialog(freeColClient.getGame().getEuropeanPlayers(),
                     freeColClient.getMyPlayer());
             addAsFrame(inciteDialog);
             inciteDialog.requestFocus();
 
             Player response2 = (Player) inciteDialog.getResponse();
             if (response2 != null) {
                 returnValue.add(response2);
             } else {
                 returnValue.clear();
                 returnValue.add(new Integer(FreeColDialog.MISSIONARY_CANCEL));
             }
 
             remove(inciteDialog);
         }
 
         return returnValue;
     }
 
     /**
      * Displays a yes/no question to the user asking if he wants to pay the
      * given amount to an indian tribe in order to have them declare war on the
      * given player.
      * 
      * @param enemy The european player to attack.
      * @param amount The amount of gold to pay.
      * 
      * @return true if the players wants to pay, false otherwise.
      */
     public boolean showInciteDialog(Player enemy, int amount) {
         String message = Messages.message("missionarySettlement.inciteConfirm");
         message = message.replaceAll("%player%", enemy.getName());
         message = message.replaceAll("%amount%", String.valueOf(amount));
 
         FreeColDialog confirmDialog = FreeColDialog.createConfirmDialog(message, Messages.message("yes"), Messages
                 .message("no"));
         addAsFrame(confirmDialog);
         confirmDialog.requestFocus();
         boolean result = confirmDialog.getResponseBoolean();
         remove(confirmDialog);
         return result;
     }
 
     /**
      * Displays a dialog with a text field and a ok/cancel option.
      * 
      * @param text The text that explains the action to the user.
      * @param defaultValue The default value appearing in the text field.
      * @param okText The text displayed on the "ok"-button.
      * @param cancelText The text displayed on the "cancel"-button. Use <i>null</i>
      *            to disable the cancel-option.
      * @return The text the user have entered or <i>null</i> if the user chose
      *         to cancel the action.
      * @see FreeColDialog
      */
     public String showInputDialog(String text, String defaultValue, String okText, String cancelText) {
         try {
             text = Messages.message(text);
             okText = Messages.message(okText);
 
             if (cancelText != null) {
                 cancelText = Messages.message(cancelText);
             }
         } catch (MissingResourceException e) {
             logger.warning("could not find message with id: " + text + ", " + okText + " or " + cancelText + ".");
         }
 
         FreeColDialog inputDialog = FreeColDialog.createInputDialog(text, defaultValue, okText, cancelText);
         addAsFrame(inputDialog);
         inputDialog.requestFocus();
 
         String response = (String) inputDialog.getResponse();
 
         // checks if the user entered some text.
         if ((response != null) && (response.length() == 0)) {
             String okTxt = "ok";
             String txt = "enterSomeText";
             try {
                 okTxt = Messages.message(okTxt);
                 txt = Messages.message(txt);
             } catch (MissingResourceException e) {
                 logger.warning("could not find message with id: " + txt + " or " + okTxt + ".");
             }
 
             FreeColDialog informationDialog = FreeColDialog.createInformationDialog(txt, okTxt);
 
             do {
                 remove(inputDialog);
                 addAsFrame(informationDialog);
                 informationDialog.requestFocus();
 
                 informationDialog.getResponse();
                 remove(informationDialog);
 
                 addAsFrame(inputDialog);
                 inputDialog.requestFocus();
 
                 response = (String) inputDialog.getResponse();
             } while ((response != null) && (response.length() == 0));
         }
 
         remove(inputDialog);
 
         return response;
     }
 
     /**
      * Displays a dialog with a text and a cancel-button, in addition to buttons
      * for each of the objects returned for the given <code>Iterator</code>.
      * 
      * @param text The text that explains the choice for the user.
      * @param cancelText The text displayed on the "cancel"-button.
      * @param iterator The <code>Iterator</code> containing the objects to
      *            create buttons for.
      * @return The choosen object, or <i>null</i> for the cancel-button.
      */
     public Object showChoiceDialog(String text, String cancelText, Iterator<?> iterator) {
         ArrayList<Object> a = new ArrayList<Object>();
         while (iterator.hasNext()) {
             a.add(iterator.next());
         }
 
         return showChoiceDialog(text, cancelText, a.toArray());
     }
 
     /**
      * Displays a dialog with a text and a cancel-button, in addition to buttons
      * for each of the objects in the array.
      * 
      * @param text The text that explains the choice for the user.
      * @param cancelText The text displayed on the "cancel"-button.
      * @param objects The array containing the objects to create buttons for.
      * @return The choosen object, or <i>null</i> for the cancel-button.
      */
     public Object showChoiceDialog(String text, String cancelText, Object[] objects) {
         /*
          * try { text = Messages.message(text); cancelText =
          * Messages.message(cancelText); } catch (MissingResourceException e) {
          * logger.warning("could not find message with id: " + text + " or " +
          * cancelText + "."); }
          */
 
         FreeColDialog choiceDialog = FreeColDialog.createChoiceDialog(text, cancelText, objects);
         if (choiceDialog.getHeight() > getHeight() / 3) {
             choiceDialog.setSize(choiceDialog.getWidth(), (getHeight() * 2) / 3);
         }
         addAsFrame(choiceDialog);
         choiceDialog.requestFocus();
 
         Object response = choiceDialog.getResponse();
         remove(choiceDialog);
 
         return response;
     }
 
     /**
      * Shows a status message that cannot be dismissed. The panel will be
      * removed when another component is added to this <code>Canvas</code>.
      * This includes all the <code>showXXX</code>-methods. In addition,
      * {@link #closeStatusPanel()} and {@link #closeMenus()} also removes this
      * panel.
      * 
      * @param message The text message to display on the status panel.
      * @see StatusPanel
      */
     public void showStatusPanel(String message) {
         statusPanel.setStatusMessage(message);
         addCentered(statusPanel, STATUS_LAYER);
     }
 
     /**
      * Closes the <code>StatusPanel</code>.
      * 
      * @see #showStatusPanel
      */
     public void closeStatusPanel() {
         if (statusPanel.isVisible()) {
             remove(statusPanel);
         }
     }
 
     /**
      * Shows a panel displaying Colopedia Information.
      * 
      * @param type The type of colopedia panel to display.
      */
     public void showColopediaPanel(int type) {
         ColopediaPanel colopediaPanel = new ColopediaPanel(this);
         colopediaPanel.initialize(type);
         addAsFrame(colopediaPanel);
         colopediaPanel.requestFocus();
     }
 
     /**
      * Shows a panel displaying Colopedia Information.
      * 
      * @param type The type of colopedia panel to display.
      * @param action The details to display.
      */
     public void showColopediaPanel(int type, int action) {
         ColopediaPanel colopediaPanel = new ColopediaPanel(this);
         colopediaPanel.initialize(type, action);
         addAsFrame(colopediaPanel);
         colopediaPanel.requestFocus();
     }
 
     /**
      * Shows a report panel.
      * 
      * @param classname The class name of the report panel to be displayed.
      */
     public void showReportPanel(String classname) {
         ReportPanel reportPanel = null;
         if ("net.sf.freecol.client.gui.panel.ReportReligiousPanel".equals(classname)) {
             reportPanel = new ReportReligiousPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportLabourPanel".equals(classname)) {
             reportPanel = new ReportLabourPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportForeignAffairPanel".equals(classname)) {
             reportPanel = new ReportForeignAffairPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportIndianPanel".equals(classname)) {
             reportPanel = new ReportIndianPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportTurnPanel".equals(classname)) {
             reportPanel = new ReportTurnPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportContinentalCongressPanel".equals(classname)) {
             reportPanel = new ReportContinentalCongressPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportTradePanel".equals(classname)) {
             reportPanel = new ReportTradePanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportMilitaryPanel".equals(classname)) {
             reportPanel = new ReportMilitaryPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportNavalPanel".equals(classname)) {
             reportPanel = new ReportNavalPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportCargoPanel".equals(classname)) {
             reportPanel = new ReportCargoPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportColonyPanel".equals(classname)) {
             reportPanel = new ReportColonyPanel(this);
         } else if ("net.sf.freecol.client.gui.panel.ReportRequirementsPanel".equals(classname)) {
             reportPanel = new ReportRequirementsPanel(this);
         } else {
             logger.warning("Request for Report panel could not be processed.  Name=" + classname);
         }
 
         if (reportPanel != null) {
             reportPanel.initialize();
             addAsFrame(reportPanel);
             reportPanel.requestFocus();
         }
     }
 
     /**
      * Shows a panel where the player may choose the next founding father to
      * recruit.
      * 
      * @param possibleFoundingFathers The different founding fathers the player
      *            may choose.
      * @return The founding father the player has chosen.
      * @see net.sf.freecol.common.model.FoundingFather
      */
     public int showChooseFoundingFatherDialog(FoundingFather[] possibleFoundingFathers) {
         closeStatusPanel();
 
         ChooseFoundingFatherDialog chooseFoundingFatherDialog = new ChooseFoundingFatherDialog(this);
 
         chooseFoundingFatherDialog.initialize(possibleFoundingFathers);
 
         addAsFrame(chooseFoundingFatherDialog);
         chooseFoundingFatherDialog.requestFocus();
 
         int response = chooseFoundingFatherDialog.getResponseInt();
 
         remove(chooseFoundingFatherDialog);
 
         return response;
     }
 
     /**
      * Shows the {@link EventPanel}.
      * 
      * @param eventID The type of <code>EventPanel</code> to be displayed.
      * @return <code>true</code>.
      */
     public boolean showEventDialog(int eventID) {
         EventPanel eventPanel = new EventPanel(this, freeColClient);
         eventPanel.initialize(eventID);
         addAsFrame(eventPanel);
         eventPanel.requestFocus();
 
         boolean response = eventPanel.getResponseBoolean();
 
         remove(eventPanel);
 
         return response;
     }
 
     /**
      * Displays the <code>EuropePanel</code>.
      * 
      * @see EuropePanel
      */
     public void showEuropePanel() {
         closeMenus();
 
         if (freeColClient.getGame() == null) {
             errorMessage("europe.noGame");
         } else {
             europePanel.initialize(freeColClient.getMyPlayer().getEurope(), freeColClient.getGame());
             JInternalFrame f = addAsSimpleFrame(europePanel);
             f.setBorder(null);
             f.setSize(getWidth(), getHeight() - getMenuBarHeight());
             f.setLocation(0, getMenuBarHeight());
             f.moveToBack();
         }
     }
 
     /**
      * Displays one of the Europe Dialogs for Recruit, Purchase, Train.
      * Closes any currently open Dialogs. 
      * Does not return from this method before the panel is closed.
      * @param intCommand One of {ExIT, RECRUIT, PURCHASE, TRAIN, UNLOAD}
      * @return <code>FreeColDialog.getResponseInt</code>.
      */
     public int showEuropeDialog(int intCommand) {
         // Close any open Europe Dialog (Recruit, Purchase, Train)       
         try {
             if (europeOpenDialog != null) {
                 europeOpenDialog.setResponse(new Integer(-1));
             }
         } catch (NumberFormatException e) {
             logger.warning("Canvas.showEuropeDialog: Invalid europeDialogType");
         }
         
         FreeColDialog localDialog = null;
 
         // Open new Dialog
         switch (intCommand) {
             case EXIT:
             case UNLOAD:
                 return -1;
             case RECRUIT:
                 localDialog = new RecruitDialog(this);
                 break;
             case PURCHASE:
                 localDialog = new PurchaseDialog(this);
                 break;
             case TRAIN:
                 localDialog = new TrainDialog(this);
                 break;
             default:
                 logger.warning("Canvas.showEuropeDialog: Invalid intCommand");
                 return -1;
         }
         localDialog.initialize();
         europeOpenDialog = localDialog; // Set the open dialog to the class variable
         
         addAsFrame(localDialog);
         localDialog.requestFocus();
 
         int response = localDialog.getResponseInt();
 
         remove(localDialog);
         if (europeOpenDialog == localDialog) {
             europeOpenDialog = null;    // Clear class variable when it's closed
         }
         
         return response;
     }
 
     /**
      * Displays the <code>TradeRouteDialog</code>. Does not return from this
      * method before the panel is closed.
      */
     public TradeRoute showTradeRouteDialog(TradeRoute selectedRoute) {
         TradeRouteDialog tradeRouteDialog = new TradeRouteDialog(this);
         tradeRouteDialog.initialize();
         tradeRouteDialog.setSelectedRoute(selectedRoute);
 
         addAsFrame(tradeRouteDialog);
         tradeRouteDialog.requestFocus();
 
         TradeRoute response = (TradeRoute) tradeRouteDialog.getResponse();
 
         remove(tradeRouteDialog);
 
         return response;
     }
 
     /**
      * Displays the <code>TradeRouteInputDialog</code>. Does not return from
      * this method before the panel is closed.
      */
     public boolean showTradeRouteInputDialog(TradeRoute route) {
         TradeRouteInputDialog tradeRouteInputDialog = new TradeRouteInputDialog(this);
         tradeRouteInputDialog.initialize(route);
 
         addAsFrame(tradeRouteInputDialog);
         tradeRouteInputDialog.requestFocus();
 
         boolean response = tradeRouteInputDialog.getResponseBoolean();
 
         remove(tradeRouteInputDialog);
 
         return response;
     }
 
     /**
      * Displays the colony panel of the given <code>Colony</code>.
      * 
      * @param colony The colony whose panel needs to be displayed.
      * @see ColonyPanel
      */
     public void showColonyPanel(Colony colony) {
         freeColClient.getGUI().stopBlinking();
         ColonyPanel colonyPanel = new ColonyPanel(this, freeColClient);
         colonyPanel.initialize(colony, freeColClient.getGame());
         addAsFrame(colonyPanel);
         colonyPanel.requestFocus();
     }
 
     /**
      * Displays the indian settlement panel of the given
      * <code>IndianSettlement</code>.
      * 
      * @param settlement The indian settlement whose panel needs to be
      *            displayed.
      * @see IndianSettlement
      */
     public void showIndianSettlementPanel(IndianSettlement settlement) {
         closeMenus();
 
         IndianSettlementPanel indianSettlementPanel = new IndianSettlementPanel();
         indianSettlementPanel.initialize(settlement);
 
         addAsFrame(indianSettlementPanel);
         indianSettlementPanel.requestFocus();
         indianSettlementPanel.getResponseBoolean();
         remove(indianSettlementPanel);
 
     }
 
     /**
      * Displays the tile panel of the given <code>Tile</code>.
      * 
      * @param tile The tile whose panel needs to be displayed.
      * @see Tile
      */
     public void showTilePanel(Tile tile) {
         TilePanel tilePanel = new TilePanel(this);
         tilePanel.initialize(tile);
 
         addAsFrame(tilePanel);
         tilePanel.requestFocus();
         tilePanel.getResponseBoolean();
         remove(tilePanel);
 
     }
 
     /**
      * Displays the monarch action panel.
      * 
      * @param action The monarch action.
      * @param replace The replacement strings.
      * @return true or false
      * @see net.sf.freecol.common.model.Monarch
      */
     public boolean showMonarchPanel(int action, String[][] replace) {
         closeStatusPanel();
 
         MonarchPanel monarchPanel = new MonarchPanel(this);
         monarchPanel.initialize(action, replace);
         addAsFrame(monarchPanel);
         monarchPanel.requestFocus();
 
         boolean response = monarchPanel.getResponseBoolean();
         remove(monarchPanel);
 
         return response;
     }
 
     /**
      * Shows the panel that allows the user to choose which unit will emigrate
      * from Europe. This method may only be called if the user has William
      * Brewster in congress.
      * 
      * @return The emigrant that was chosen by the user.
      */
     public int showEmigrationPanel() {
         EmigrationPanel emigrationPanel = new EmigrationPanel(this);
         emigrationPanel.initialize(freeColClient.getMyPlayer().getEurope());
 
         addAsFrame(emigrationPanel);
         emigrationPanel.requestFocus();
 
         int response = emigrationPanel.getResponseInt();
 
         remove(emigrationPanel);
 
         return response;
     }
 
     /**
      * Updates the menu bar.
      */
     public void updateJMenuBar() {
         if (jMenuBar instanceof FreeColMenuBar) {
             ((FreeColMenuBar) jMenuBar).update();
         }
     }
 
     /**
      * Resets <code>FreeColMenuBar</code> on this
      * <code>Canvas</code>.
      * 
      * @see FreeColMenuBar
      */
     public void resetFreeColMenuBar() {
         if (jMenuBar instanceof FreeColMenuBar) {
             ((FreeColMenuBar) jMenuBar).reset();
         }
     }
 
     /**
      * Removes the given component from this Container.
      * 
      * @param comp The component to remove from this Container.
      */
     @Override
     public void remove(Component comp) {
         remove(comp, true);
     }
 
     /**
      * Gets the internal frame for the given component.
      * 
      * @param c The component.
      * @return The given component if this is an internal frame or the first
      *         parent that is an internal frame. Returns <code>null</code> if
      *         no internal frame is found.
      */
     private JInternalFrame getInternalFrame(final Component c) {
         Component temp = c;
 
         while (temp != null && !(temp instanceof JInternalFrame)) {
             temp = temp.getParent();
         }
         return (JInternalFrame) temp;
     }
 
     /**
      * Removes the given component from this Container.
      * 
      * @param comp The component to remove from this Container.
      * @param update The <code>Canvas</code> will be enabled, the graphics
      *            repainted and both the menubar and the actions will be updated
      *            if this parameter is <code>true</code>.
      */
     public void remove(Component comp, boolean update) {
         if (comp == null) {
             return;
         }
         final Rectangle updateBounds = comp.getBounds();
         if (comp == jMenuBar) {
             jMenuBar = null;
             super.remove(comp);
         } else {
             final JInternalFrame frame = getInternalFrame(comp);
             if (frame != null && frame != comp) {
                 // updateBounds = frame.getBounds();
                 frame.dispose();
             } else {
                 super.remove(comp);
             }
         }
 
         repaint(updateBounds.x, updateBounds.y, updateBounds.width, updateBounds.height);
 
         final boolean takeFocus = (comp != statusPanel);
         if (update) {
             updateJMenuBar();
             freeColClient.getActionManager().update();
             if (takeFocus && !isShowingSubPanel()) {
                 takeFocus();
             }
         }
     }
 
     /**
      * Adds a component to this Canvas.
      * 
      * @param comp The component to add to this ToEuropePanel.
      * @return The component argument.
      */
     @Override
     public Component add(Component comp) {
         add(comp, null);
         return comp;
     }
 
     /**
      * Adds a component centered on this Canvas. Removes the statuspanel if
      * visible (and <code>comp != statusPanel</code>).
      * 
      * @param comp The component to add to this ToEuropePanel.
      * @return The component argument.
      */
     public Component addCentered(Component comp) {
         addCentered(comp, null);
         return comp;
     }
 
     /**
      * Adds a component centered on this Canvas inside a frame. Removes the
      * statuspanel if visible (and <code>comp != statusPanel</code>).
      * 
      * @param comp The component to add to this JInternalFrame.
      * @return The <code>JInternalFrame</code> that was created and added.
      */
     public JInternalFrame addAsFrame(JComponent comp) {
         return addAsFrame(comp, false);
     }
     
     /**
      * Adds a component centered on this Canvas inside a frame.
      * The frame is considered as a tool box (not counted as a frame
      * by the methods deciding if a panel is being displayed).
      * 
      * <br><br>
      * 
      * Removes the statuspanel if visible (and
      * <code>comp != statusPanel</code>).
      * 
      * @param comp The component to add to this JInternalFrame.
      * @return The <code>JInternalFrame</code> that was created and added.
      */
     public JInternalFrame addAsToolBox(JComponent comp) {
         return addAsFrame(comp, true);
     }
     
     /**
      * Adds a component centered on this Canvas inside a frame. Removes the
      * statuspanel if visible (and <code>comp != statusPanel</code>).
      * 
      * @param comp The component to add to this ToEuropePanel.
      * @param toolBox Should be set to true if the resulting frame
      *      is used as a toolbox (that is: it should not be counted
      *      as a frame).
      * @return The <code>JInternalFrame</code> that was created and added.
      */
     private JInternalFrame addAsFrame(JComponent comp, boolean toolBox) {
         final int FRAME_EMPTY_SPACE = 60;
 
         final JInternalFrame f = (toolBox) ? new ToolBoxFrame() : new JInternalFrame();
         if (f.getContentPane() instanceof JComponent) {
             JComponent c = (JComponent) f.getContentPane();
             c.setOpaque(false);
             c.setBorder(null);
         }
 
         if (comp.getBorder() != null) {
             Image menuborderN = (Image) UIManager.get("menuborder.n.image");
             Image menuborderNW = (Image) UIManager.get("menuborder.nw.image");
             Image menuborderNE = (Image) UIManager.get("menuborder.ne.image");
             Image menuborderW = (Image) UIManager.get("menuborder.w.image");
             Image menuborderE = (Image) UIManager.get("menuborder.e.image");
             Image menuborderS = (Image) UIManager.get("menuborder.s.image");
             Image menuborderSW = (Image) UIManager.get("menuborder.sw.image");
             Image menuborderSE = (Image) UIManager.get("menuborder.se.image");
             final FreeColImageBorder imageBorder = new FreeColImageBorder(menuborderN, menuborderW, menuborderS,
                     menuborderE, menuborderNW, menuborderNE, menuborderSW, menuborderSE);
             // comp.setBorder(BorderFactory.createCompoundBorder(imageBorder,
             // BorderFactory.createEmptyBorder(10, 10, 10, 10)));
 
             f.setBorder(imageBorder);
             comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         } else {
             f.setBorder(null);
         }
 
         final FrameMotionListener fml = new FrameMotionListener(f);
         comp.addMouseMotionListener(fml);
         comp.addMouseListener(fml);
         if (f.getUI() instanceof BasicInternalFrameUI) {
             BasicInternalFrameUI biu = (BasicInternalFrameUI) f.getUI();
             biu.setNorthPane(null);
             biu.setSouthPane(null);
             biu.setWestPane(null);
             biu.setEastPane(null);
         }
 
         f.getContentPane().add(comp);
         f.setOpaque(false);
         f.pack();
         int width = f.getWidth();
         int height = f.getHeight();
         if (width > getWidth() - FRAME_EMPTY_SPACE) {
             width = getWidth() - FRAME_EMPTY_SPACE;
         }
         if (height > getHeight() - FRAME_EMPTY_SPACE) {
             height = getHeight() - FRAME_EMPTY_SPACE;
         }
         f.setSize(width, height);
         addCentered(f);
         f.setName(comp.getClass().getSimpleName());
 
         f.setFrameIcon(null);
         f.setVisible(true);
         f.setResizable(true);
         try {
             f.setSelected(true);
         } catch (java.beans.PropertyVetoException e) {
         }
 
         return f;
     }
 
     /**
      * Adds a component centered on this Canvas inside a frame. Removes the
      * statuspanel if visible (and <code>comp != statusPanel</code>).
      * 
      * The frame cannot be moved or resized.
      * 
      * @param comp The component to add to this ToEuropePanel.
      * @return The <code>JInternalFrame</code> that was created and added.
      */
     public JInternalFrame addAsSimpleFrame(JComponent comp) {
         final JInternalFrame f = new JInternalFrame();
         f.getContentPane().add(comp);
         f.pack();
         addCentered(f);
         f.setName(comp.getClass().getSimpleName());
 
         if (f.getUI() instanceof BasicInternalFrameUI) {
             BasicInternalFrameUI biu = (BasicInternalFrameUI) f.getUI();
             biu.setNorthPane(null);
         }
 
         f.setFrameIcon(null);
         f.setVisible(true);
         f.setResizable(false);
         try {
             f.setSelected(true);
         } catch (java.beans.PropertyVetoException e) {
         }
 
         return f;
     }
 
     /**
      * Removes the mouse listeners for moving the frame of the given component.
      * 
      * @param c The component the listeneres should be removed from.
      */
     public void deactivateMovable(JComponent c) {
         for (MouseListener ml : c.getMouseListeners()) {
             if (ml instanceof FrameMotionListener) {
                 c.removeMouseListener(ml);
             }
         }
         for (MouseMotionListener ml : c.getMouseMotionListeners()) {
             if (ml instanceof FrameMotionListener) {
                 c.removeMouseMotionListener(ml);
             }
         }
     }
 
     /**
      * Adds a component centered on this Canvas. Removes the statuspanel if
      * visible (and <code>comp != statusPanel</code>).
      * 
      * @param comp The component to add to this ToEuropePanel.
      * @param i The layer to add the component to (see JLayeredPane).
      */
     public void addCentered(Component comp, Integer i) {
         comp.setLocation(getWidth() / 2 - comp.getWidth() / 2, (getHeight() + getMenuBarHeight()) / 2
                 - comp.getHeight() / 2);
 
         add(comp, i);
     }
 
     /**
      * Adds a component to this Canvas. Removes the statuspanel if visible (and
      * <code>comp != statusPanel</code>).
      * 
      * @param comp The component to add to this ToEuropePanel.
      * @param i The layer to add the component to (see JLayeredPane).
      */
     public void add(Component comp, Integer i) {
         add(comp, i, true);
     }
 
     /**
      * Adds a component to this Canvas. Removes the statuspanel if visible (and
      * <code>comp != statusPanel</code>).
      * 
      * @param comp The component to add to this ToEuropePanel.
      * @param i The layer to add the component to (see JLayeredPane).
      */
     public void add(Component comp, Integer i, boolean update) {
 
         if (comp != statusPanel && !(comp instanceof JMenuItem) && !(comp instanceof FreeColDialog)
                 && statusPanel.isVisible()) {
             remove(statusPanel, false);
         }
 
         try {
             if (i == null) {
                 super.add(comp);
             } else {
                 super.add(comp, i);
             }
         } catch(Exception e) {
             logger.warning("add component failed with layer " + i);
         }
 
         if (update) {
             updateJMenuBar();
             freeColClient.getActionManager().update();
         }
     }
 
     /**
      * Makes sure that this Canvas takes the focus. It will keep on trying for a
      * while even its request doesn't get granted immediately.
      */
     private void takeFocus() {
         if (!isShowingSubPanel()) {
             requestFocus();
         }
     }
 
     /**
      * Gets the <code>EuropePanel</code>.
      * 
      * @return The <code>EuropePanel</code>.
      */
     public EuropePanel getEuropePanel() {
         return europePanel;
     }
 
     /**
      * Shows the given popup at the given position on the screen.
      * 
      * @param popup The JPopupMenu to show.
      * @param x The x-coordinate at which to show the popup.
      * @param y The y-coordinate at which to show the popup.
      */
     public void showPopup(JPopupMenu popup, int x, int y) {
         // closeMenus();
         popup.show(this, x, y);
         popup.repaint();
     }
 
     /**
      * Shows a tile popup.
      * 
      * @param pos The coordinates of the Tile where the popup occured.
      * @param x The x-coordinate on the screen where the popup needs to be
      *            placed.
      * @param y The y-coordinate on the screen where the popup needs to be
      *            placed.
      * @see TilePopup
      */
     public void showTilePopup(Map.Position pos, int x, int y) {
         if (pos != null) {
             Tile t = freeColClient.getGame().getMap().getTileOrNull(pos.getX(), pos.getY());
 
             if (t != null) {
                 TilePopup tp = new TilePopup(t, freeColClient, this, getGUI());
                 if (tp.hasItem()) {
                     showPopup(tp, x, y);
                 } else if (t.isExplored()) {
                     showTilePanel(t);
                 }
             }
         }
     }
 
     /**
      * Displays an error message.
      * 
      * @param messageID The i18n-keyname of the error message to display.
      */
     public void errorMessage(String messageID) {
         errorMessage(messageID, "Unspecified error: " + messageID);
     }
 
     /**
      * Displays an error message.
      * 
      * @param messageID The i18n-keyname of the error message to display.
      * @param message An alternativ message to display if the resource specified
      *            by <code>messageID</code> is unavailable.
      */
     public void errorMessage(String messageID, String message) {
         if (messageID != null) {
             try {
                 message = Messages.message(messageID);
             } catch (MissingResourceException e) {
                 logger.warning("could not find message with id: " + messageID);
             }
         }
 
         ErrorPanel errorPanel = new ErrorPanel(this);
 
         errorPanel.initialize(message);
         addAsFrame(errorPanel);
         errorPanel.requestFocus();
         errorPanel.getResponse();
         remove(errorPanel);
     }
 
     /**
      * Shows a message with some information and an "OK"-button.
      * 
      * @param messageId The messageId of the message to display.
      */
     public void showInformationMessage(String messageId) {
         showInformationMessage(messageId, null);
     }
 
     /**
      * Shows a message with some information and an "OK"-button.
      * 
      * <br>
      * <br>
      * <b>Example:</b> <br>
      * <code>canvas.showInformationMessage("noNeedForTheGoods", new String[][] {{"%goods%", goods.getName()}});</code>
      * 
      * @param messageId The messageId of the message to display.
      * @param replace All occurances of <code>replace[i][0]</code> in the
      *            message gets replaced by <code>replace[i][1]</code>.
      */
     public void showInformationMessage(String messageId, String[][] replace) {
         String text;
         try {
             text = Messages.message(messageId, replace);
         } catch (MissingResourceException e) {
             text = messageId;
             logger.warning("Missing i18n resource: " + messageId);
         }
         FreeColDialog infoDialog = FreeColDialog.createInformationDialog(new String[] { text },
                 new ImageIcon[] { null });
         addAsFrame(infoDialog);
         infoDialog.requestFocus();
 
         infoDialog.getResponse();
 
         remove(infoDialog);
     }
 
     /**
      * Refreshes this Canvas visually.
      */
     public void refresh() {
         gui.forceReposition();
         repaint(0, 0, getWidth(), getHeight());
     }
 
     /**
      * Refreshes the screen at the specified Tile.
      * 
      * @param x The x-coordinate of the Tile to refresh.
      * @param y The y-coordinate of the Tile to refresh.
      */
     public void refreshTile(int x, int y) {
         if (x >= 0 && y >= 0) {
             repaint(gui.getTileBounds(x, y));
         }
     }
 
     /**
      * Refreshes the screen at the specified Tile.
      * 
      * @param t The tile to refresh.
      */
     public void refreshTile(Tile t) {
         refreshTile(t.getX(), t.getY());
     }
 
     /**
      * Refreshes the screen at the specified Tile.
      * 
      * @param p The position of the tile to refresh.
      */
     public void refreshTile(Position p) {
         refreshTile(p.getX(), p.getY());
     }
 
     /**
      * Closes all the menus that are currently open.
      */
     public void closeMenus() {
         for (JInternalFrame frame : getAllFrames()) {
             frame.dispose();
         }
     }
 
     /**
      * Shows the <code>MainPanel</code>.
      * 
      * @see MainPanel
      */
     public void showMainPanel() {
         closeMenus();
         addCentered(mainPanel, MAIN_LAYER);
         mainPanel.requestFocus();
     }
 
     /**
      * Gets the <code>MainPanel</code>.
      * 
      * @return The <code>MainPanel</code>.
      * @see MainPanel
      */
     public MainPanel getMainPanel() {
         return mainPanel;
     }
 
     /**
      * Closes the {@link MainPanel}.
      */
     public void closeMainPanel() {
         remove(mainPanel);
     }
 
     /**
      * Shows the <code>OpenGamePanel</code>.
      */
     public void showOpenGamePanel() {
         errorMessage("openGame.unimplemented");
     }
 
     /**
      * Gets the <code>StartGamePanel</code> that lies in this container.
      * 
      * @return The <code>StartGamePanel</code>.
      * @see StartGamePanel
      */
     public StartGamePanel getStartGamePanel() {
         return startGamePanel;
     }
 
     /**
      * Tells the map controls that a chat message was recieved.
      * 
      * @param sender The player who sent the chat message to the server.
      * @param message The chat message.
      * @param privateChat 'true' if the message is a private one, 'false'
      *            otherwise.
      * @see GUIMessage
      */
     public void displayChatMessage(Player sender, String message, boolean privateChat) {
         gui.addMessage(new GUIMessage(sender.getName() + ": " + message, sender.getColor()));
     }
 
     /**
      * Displays a chat message originating from this client.
      * 
      * @param message The chat message.
      */
     public void displayChatMessage(String message) {
         displayChatMessage(freeColClient.getMyPlayer(), message, false);
     }
 
     /**
      * Quits the application. This method uses {@link #confirmQuitDialog()} in
      * order to get a "Are you sure"-confirmation from the user.
      */
     public void quit() {
         if (confirmQuitDialog()) {
             freeColClient.quit();
         }
     }
 
     /**
      * Closes all panels, changes the background and shows the main menu.
      */
     public void returnToTitle() {
         // TODO: check if the GUI object knows that we're not
         // inGame. (Retrieve value of GUI::inGame.)  If GUI thinks
         // we're still in the game then log an error because at this
         // point the GUI should have been informed.
         closeMenus();
         removeInGameComponents();
         showMainPanel();
         repaint();
     }
 
     /**
      * Removes components that is only used when in game.
      */
     public void removeInGameComponents() {
         // remove listeners, they will be added when launching the new game...
         KeyListener[] keyListeners = getKeyListeners();
         for (int i = 0; i < keyListeners.length; ++i) {
             removeKeyListener(keyListeners[i]);
         }
 
         MouseListener[] mouseListeners = getMouseListeners();
         for (int i = 0; i < mouseListeners.length; ++i) {
             removeMouseListener(mouseListeners[i]);
         }
 
         MouseMotionListener[] mouseMotionListeners = getMouseMotionListeners();
         for (int i = 0; i < mouseMotionListeners.length; ++i) {
             removeMouseMotionListener(mouseMotionListeners[i]);
         }
 
         if (jMenuBar != null) {
             remove(jMenuBar);
         }
 
         for (Component c : getComponents()) {
             remove(c, false);
         }
         
         // change to default view mode
         getGUI().getViewMode().changeViewMode(ViewMode.MOVE_UNITS_MODE);
     }
 
     /**
      * Checks if this <code>Canvas</code> contains any ingame components.
      * 
      * @return <code>true</code> if there is a single ingame component.
      */
     public boolean containsInGameComponents() {
         KeyListener[] keyListeners = getKeyListeners();
         if (keyListeners.length > 0) {
             return true;
         }
 
         MouseListener[] mouseListeners = getMouseListeners();
         if (mouseListeners.length > 0) {
             return true;
         }
 
         MouseMotionListener[] mouseMotionListeners = getMouseMotionListeners();
         if (mouseMotionListeners.length > 0) {
             return true;
         }
 
         return false;
     }
 
     /**
      * Displays a "Are you sure you want to quit"-dialog in which the user may
      * choose to quit or cancel.
      * 
      * @return <i>true</i> if the user desides to quit and <i>false</i>
      *         otherwise.
      */
     public boolean confirmQuitDialog() {
         QuitDialog quitDialog = new QuitDialog(this);
 
         addAsFrame(quitDialog);
         quitDialog.requestFocus();
         try {
             return quitDialog.getResponseBoolean();
         } finally {
             remove(quitDialog);
         }
     }
 
     /**
      * Returns this <code>Canvas</code>'s <code>GUI</code>.
      * 
      * @return The <code>GUI</code>.
      */
     public GUI getGUI() {
         return gui;
     }
 
     /**
      * Returns the freeColClient.
      * 
      * @return The <code>freeColClient</code> associated with this
      *         <code>Canvas</code>.
      */
     public FreeColClient getClient() {
         return freeColClient;
     }
 
     /**
      * Displays a quit dialog and, if desired, logouts the current game and
      * shows the new game panel.
      */
     public void newGame() {
         if (!showConfirmDialog("stopCurrentGame.text", "stopCurrentGame.yes", "stopCurrentGame.no")) {
             return;
         }
 
         freeColClient.getConnectController().quitGame(true);
         removeInGameComponents();
         showNewGamePanel();
     }
 
     /**
      * A class for frames being used as tool boxes.
      */
     class ToolBoxFrame extends JInternalFrame {
         
     }
 
     /**
      * Handles the moving of internal frames.
      */
     class FrameMotionListener extends MouseAdapter implements MouseMotionListener {
 
         private JInternalFrame f;
 
         private Point loc = null;
 
 
         FrameMotionListener(JInternalFrame f) {
             this.f = f;
         }
 
         @Override
         public void mousePressed(MouseEvent e) {
             if (f.getDesktopPane() == null || f.getDesktopPane().getDesktopManager() == null) {
                 return;
             }
             loc = SwingUtilities.convertPoint((Component) e.getSource(), e.getX(), e.getY(), null);
             f.getDesktopPane().getDesktopManager().beginDraggingFrame(f);
         }
 
         @Override
         public void mouseReleased(MouseEvent e) {
             if (loc == null || f.getDesktopPane() == null || f.getDesktopPane().getDesktopManager() == null) {
                 return;
             }
             f.getDesktopPane().getDesktopManager().endDraggingFrame(f);
         }
 
         public void mouseDragged(MouseEvent e) {
             if (loc == null || f.getDesktopPane() == null || f.getDesktopPane().getDesktopManager() == null) {
                 return;
             }
 
             Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getX(), e.getY(), null);
             int moveX = loc.x - p.x;
             int moveY = loc.y - p.y;
             f.getDesktopPane().getDesktopManager().dragFrame(f, f.getX() - moveX, f.getY() - moveY);
             loc = p;
         }
 
         public void mouseMoved(MouseEvent arg0) {
         }
     };
 }
