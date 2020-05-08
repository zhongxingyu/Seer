 package demo.client.local.game.gui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.jboss.errai.bus.client.api.base.MessageBuilder;
 import org.jboss.errai.bus.client.api.messaging.MessageBus;
 import org.jboss.errai.bus.client.api.messaging.MessageCallback;
 import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
 import org.jboss.errai.common.client.api.Assert;
 import org.jboss.errai.ui.client.widget.ListWidget;
 import org.jboss.errai.ui.nav.client.local.Page;
 import org.jboss.errai.ui.nav.client.local.PageHidden;
 import org.jboss.errai.ui.nav.client.local.PageShowing;
 import org.jboss.errai.ui.nav.client.local.TransitionTo;
 import org.jboss.errai.ui.shared.api.annotations.DataField;
 import org.jboss.errai.ui.shared.api.annotations.Templated;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.DomEvent.Type;
 import com.google.gwt.event.dom.client.DoubleClickEvent;
 import com.google.gwt.event.dom.client.DoubleClickHandler;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.dom.client.TouchEndEvent;
 import com.google.gwt.event.dom.client.TouchEndHandler;
 import com.google.gwt.event.dom.client.TouchMoveEvent;
 import com.google.gwt.event.dom.client.TouchMoveHandler;
 import com.google.gwt.event.dom.client.TouchStartEvent;
 import com.google.gwt.event.dom.client.TouchStartHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 
 import demo.client.local.game.callbacks.BoardCallback;
 import demo.client.local.game.callbacks.OppCallback;
 import demo.client.local.game.controllers.BoardController;
 import demo.client.local.game.controllers.SecondaryDisplayController;
 import demo.client.local.game.controllers.SecondaryDisplayControllerImpl;
 import demo.client.local.game.handlers.BoardKeyHandler;
 import demo.client.local.game.handlers.BoardMouseHandler;
 import demo.client.local.game.handlers.BoardTouchHandler;
 import demo.client.local.game.tools.BoardMessageBusImpl;
 import demo.client.local.game.tools.Size;
 import demo.client.local.game.tools.Size.SizeCategory;
 import demo.client.local.lobby.Client;
 import demo.client.local.lobby.Lobby;
 import demo.client.shared.message.Command;
 import demo.client.shared.message.ExitMessage;
 import demo.client.shared.meta.ScoreTracker;
 
 /**
  * An Errai Navigation Page providing the UI for a Block Drop game.
  * 
  * @author mbarkley <mbarkley@redhat.com>
  * 
  */
 @Page
 @Templated("Board.html")
 public class BoardPage extends Composite implements ControllableBoardDisplay {
 
   public static final String CANVAS_WRAPPER_ID = "mainCanvas-wrapper";
 
   private static BoardPage instance;
 
   // A mainCanvas for drawing a Block Drop game.
   @DataField("canvas")
   private Canvas mainCanvas = Canvas.createIfSupported();
   private BoardCanvas canvasWrapper;
 
   // A canvas for drawing the next piece in the Block Drop game.
   @DataField("next-piece")
   private Canvas nextPieceCanvas = Canvas.createIfSupported();
 
   // Canvas for displaying an opponent board.
   @DataField("opp-canvas")
   private Canvas oppCanvas = Canvas.createIfSupported();
   private BoardCanvas oppCanvasWrapper;
   private MessageCallback oppCallback;
 
   // For displaying game over prompt.
   @Inject
   @DataField("game-over-panel")
   GameOverPanel gameOverPanel;
 
   // For displaying players scores.
   @Inject
   @DataField("score-list")
   private ListWidget<ScoreTracker, ScorePanel> scoreDisplay;
 
   // For navigating back to the lobby.
   @Inject
   private TransitionTo<Lobby> lobbyTransition;
 
   // For controlling the scoreList and nextPieceCanvas.
   private SecondaryDisplayController secondaryController;
 
   // For controlling the mainCanvas and game loop.
   private BoardController controller;
 
   // For handling updates from the server.
   private BoardCallback boardCallback;
 
   @Inject
   private RequestDispatcher dispatcher;
   @Inject
   private MessageBus messageBus;
 
   private List<HandlerRegistration> handlerRegs = new ArrayList<HandlerRegistration>();
 
   /**
    * Create a BoardPage for displaying a Block Drop game.
    * 
    * This class implements {@link ControllableBoardDisplay ControllableBoardDisplay} in order to
    * wrap a controlled canvas.
    */
   public BoardPage() {
     System.out.println("Initiating BoardModel");
     instance = this;
 
     // Initialize canvases.
     Scheduler.get().scheduleDeferred(new ScheduledCommand() {
       @Override
       public void execute() {
         setWrapperSize(Window.getClientHeight(), Window.getClientWidth());
       }
     });
 
     canvasWrapper = new BoardCanvas(mainCanvas, SizeCategory.MAIN);
     oppCanvasWrapper = new BoardCanvas(oppCanvas, SizeCategory.OPPONENT);
   }
 
   private void setup() {
     // Check that mainCanvas was supported.
     if (mainCanvas != null) {
       System.out.println("Canvas successfully created.");
     }
     else {
       // TODO: Display message to user that HTML5 Canvas is required.
     }
     secondaryController = new SecondaryDisplayControllerImpl(scoreDisplay, nextPieceCanvas);
     controller = new BoardController(this, secondaryController, new BoardMessageBusImpl());
     boardCallback = new BoardCallback(controller, secondaryController);
     oppCallback = new OppCallback(oppCanvasWrapper);
 
     gameOverPanel.setVisible(false);
 
     EventHandler keyHandler = new BoardKeyHandler(controller);
     addHandlerToMainCanvas((KeyUpHandler) keyHandler, KeyUpEvent.getType());
     addHandlerToMainCanvas((KeyDownHandler) keyHandler, KeyDownEvent.getType());
 
     EventHandler mouseHandler = new BoardMouseHandler(controller, mainCanvas.getElement());
     addHandlerToMainCanvas((MouseDownHandler) mouseHandler, MouseDownEvent.getType());
     addHandlerToMainCanvas((MouseMoveHandler) mouseHandler, MouseMoveEvent.getType());
     addHandlerToMainCanvas((MouseUpHandler) mouseHandler, MouseUpEvent.getType());
     addHandlerToMainCanvas((DoubleClickHandler) mouseHandler, DoubleClickEvent.getType());
 
     EventHandler touchHandler = new BoardTouchHandler(controller, mainCanvas.getElement());
     addHandlerToMainCanvas((TouchStartHandler) touchHandler, TouchStartEvent.getType());
     addHandlerToMainCanvas((TouchMoveHandler) touchHandler, TouchMoveEvent.getType());
     addHandlerToMainCanvas((TouchEndHandler) touchHandler, TouchEndEvent.getType());
   }
 
   private void resetCanvasSizes() {
     Map<SizeCategory, Integer[]> dimensionMap = new HashMap<SizeCategory, Integer[]>();
 
     dimensionMap.put(SizeCategory.MAIN, new Integer[] { mainCanvas.getCanvasElement().getClientHeight(),
         mainCanvas.getCanvasElement().getClientWidth() });
     dimensionMap.put(SizeCategory.NEXT, new Integer[] { nextPieceCanvas.getCanvasElement().getClientHeight(),
         nextPieceCanvas.getCanvasElement().getClientWidth() });
     dimensionMap.put(SizeCategory.OPPONENT, new Integer[] { oppCanvas.getCanvasElement().getClientHeight(),
         oppCanvas.getCanvasElement().getClientWidth() });
 
     Size.reset(dimensionMap);
 
     mainCanvas.setCoordinateSpaceWidth(Size.getSize(SizeCategory.MAIN).getCoordWidth());
     mainCanvas.setCoordinateSpaceHeight(Size.getSize(SizeCategory.MAIN).getCoordHeight());
 
     nextPieceCanvas.setCoordinateSpaceWidth(Size.getSize(SizeCategory.NEXT).getCoordWidth());
     nextPieceCanvas.setCoordinateSpaceHeight(Size.getSize(SizeCategory.NEXT).getCoordHeight());
 
     oppCanvas.setCoordinateSpaceWidth(Size.getSize(SizeCategory.OPPONENT).getCoordWidth());
     oppCanvas.setCoordinateSpaceHeight(Size.getSize(SizeCategory.OPPONENT).getCoordHeight());
   }
 
   @PageShowing
   private void start() {
     if (Client.getInstance().getGameRoom() == null)
       lobbyTransition.go();
     else {
       Client.getInstance().getPlayer().setGameId(Client.getInstance().getGameRoom().getId());
       setup();
       controller.startGame();
       // Subscribe to game channel
       messageBus.subscribe("Game" + Client.getInstance().getGameRoom().getId(), boardCallback);
       messageBus.subscribe("Game" + Client.getInstance().getGameRoom().getId(), oppCallback);
       
       handlerRegs.add(Window.addResizeHandler(new ResizeHandler() {
         @Override
         public void onResize(ResizeEvent event) {
           setWrapperSize(event.getHeight(), event.getWidth());
         }
       }));
     }
   }
 
   protected void setWrapperSize(int height, int width) {
     DivElement wrapper = DivElement.as(Document.get().getElementById("game-wrapper"));
     // 0.75 is the ideal ratio
     double ratio = ((double) height) / ((double) width);
 
     // viewport has extra height
     if (ratio >= 0.75) {
       width = width - 2;
       wrapper.getStyle().setWidth(width, Unit.PX);
       wrapper.getStyle().setHeight(0.75 * width, Unit.PX);
     }
     // viewport has extra width
     else {
       height = height - 2;
       wrapper.getStyle().setHeight(height, Unit.PX);
       wrapper.getStyle().setWidth(height / 0.75, Unit.PX);
     }
     resetCanvasSizes();
   }
 
   @PageHidden
   private void leaveGame() {
     if (Client.getInstance().getGameRoom() != null) {
       ExitMessage exitMessage = new ExitMessage();
       exitMessage.setPlayer(Client.getInstance().getPlayer());
       exitMessage.setGame(Client.getInstance().getGameRoom());
       MessageBuilder.createMessage("Relay").command(Command.LEAVE_GAME).withValue(exitMessage).noErrorHandling()
               .sendNowWith(dispatcher);
     }
 
     if (controller != null) {
       controller.stop();
       ((OppCallback) oppCallback).destroy();
     }
 
     removeHandlers();
 
     if (Client.getInstance() != null) {
       if (Client.getInstance().getGameRoom() != null)
         messageBus.unsubscribeAll("Game" + Client.getInstance().getGameRoom().getId());
       Client.getInstance().setGameRoom(null);
     }
   }
 
   private void removeHandlers() {
     for (HandlerRegistration handlerReg : handlerRegs) {
       handlerReg.removeHandler();
     }
     handlerRegs.clear();
   }
 
   @Override
   public void undrawBlock(double x, double y, Block activeBlock) {
     canvasWrapper.undrawBlock(x, y, activeBlock);
   }
 
   @Override
   public void drawBlock(double x, double y, Block activeBlock) {
     canvasWrapper.drawBlock(x, y, activeBlock);
   }
 
   private <H extends EventHandler> void addHandlerToMainCanvas(H handler, Type<H> type) {
     handlerRegs.add(Assert.notNull("Could not get game-wrapper root panel.", RootPanel.get()).addDomHandler(handler,
             type));
   }
 
   @Override
   public void pause() {
     DivElement element = ((DivElement) Document.get().getElementById("pause-overlay"));
     element.setAttribute("style", "display: block");
   }
 
   @Override
   public void unpause() {
     DivElement element = ((DivElement) Document.get().getElementById("pause-overlay"));
     element.removeAttribute("style");
   }
 
   @Override
   public void clearBoard() {
     canvasWrapper.clearBoard();
   }
 
   @Override
   public SizeCategory getSizeCategory() {
     return canvasWrapper.getSizeCategory();
   }
 
   /**
    * Get the instance of BoardPage.
    * 
    * @return The instance of BoardPage that is or was last displayed.
    */
   public static BoardPage getInstance() {
     return instance;
   }
 
   /**
    * Transition the navigation panel to the Lobby page.
    */
   public void goToLobby() {
     lobbyTransition.go();
   }
 
   /**
    * Get the BoardController instance associated with this page.
    * 
    * @return The BoardController instance controlling the local board on this page.
    */
   public BoardController getController() {
     return controller;
   }
 
   @Override
   public void gameOver() {
     gameOverPanel.setVisible(true);
   }
 }
