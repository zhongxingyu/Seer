 package com.crudetech.tictactoe.delivery.swing.grid;
 
 
 import com.crudetech.event.EventListener;
 import com.crudetech.event.EventObject;
 import com.crudetech.gui.widgets.Point;
 import com.crudetech.tictactoe.delivery.gui.widgets.EvenlyDistributedCellsStyleStub;
 import com.crudetech.tictactoe.delivery.gui.widgets.Style;
 import com.crudetech.tictactoe.delivery.gui.widgets.TicTacToeGridModel;
 import com.crudetech.tictactoe.game.Grid;
 import com.crudetech.tictactoe.ui.CellEventObject;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 public class JTicTacToeGridMouseMoveTest {
     private JTicTacToeGrid grid;
     private Point outsideAnyCell;
     private Point inLastCell;
 
     @Before
     public void setUp() throws Exception {
         grid = new JTicTacToeGrid();
        Style style = EvenlyDistributedCellsStyleStub.builder()
                .withBackgroundImageSize(700, 500)
                .withCellSize(100, 100)
                .build();
        grid.setSize(1400, 1000);
         grid.getUI().setStyle(style);
 
         outsideAnyCell = new Point(10, 10);
 
        final Point lastCellLocation = Point.of(850, 600);
        inLastCell = lastCellLocation.translate(10, 10);
 
        grid.getUI().buildGraphic();
     }
 
     @Test
     public void mouseClickOutsideOfCellDoesNotRaiseCellClickedEvent() throws Exception {
         EventListener<CellEventObject<JTicTacToeGrid>> cellClicked = newEventListenerMock();
         grid.cellClicked().addListener(cellClicked);
 
         MouseEvent click = buildLButtonClickedEvent(outsideAnyCell);
         grid.raiseMouseEvent(click);
 
         verify(cellClicked, never()).onEvent(anyCellEventObject());
     }
 
     @SuppressWarnings("unchecked")
     private CellEventObject<JTicTacToeGrid> anyCellEventObject() {
         return any(CellEventObject.class);
     }
 
     @SuppressWarnings("unchecked")
     private <T extends EventObject<?>> EventListener<T> newEventListenerMock() {
         return mock(EventListener.class);
     }
 
     private MouseEvent buildLButtonClickedEvent(Point pos) {
         return new MouseEvent(grid, MouseEvent.MOUSE_CLICKED,
                 System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, pos.x, pos.y, 1, false, MouseEvent.BUTTON1);
     }
 
     @Test
     public void mouseClickOnCellRaisesCellClickedEventWithCorrectCells() throws Exception {
         EventListener<CellEventObject<JTicTacToeGrid>> cellClicked = newEventListenerMock();
         grid.cellClicked().addListener(cellClicked);
 
         MouseEvent click = buildLButtonClickedEvent(inLastCell);
         grid.raiseMouseEvent(click);
 
         Grid.Location expectedCell = Grid.Location.of(Grid.Row.Third, Grid.Column.Third);
        verify(cellClicked).onEvent(new CellEventObject<>(grid, expectedCell));
     }
 
     @Test
     public void mouseHoverOnCellHighlightsCell() throws Exception {
         TicTacToeGridModel model = grid.getModel();
 
         MouseEvent move = buildMouseMovedEvent(inLastCell);
 
         grid.raiseMouseEvent(move);
 
         assertThat(model.hasHighlightedCell(), is(true));
         assertThat(model.getHighlightedCell(), is(Grid.Location.of(Grid.Row.Third, Grid.Column.Third)));
     }
 
     private MouseEvent buildMouseMovedEvent(Point pos) {
         return new MouseEvent(grid, MouseEvent.MOUSE_MOVED,
                 System.currentTimeMillis(), 0, pos.x, pos.y, 1, false, MouseEvent.NOBUTTON);
     }
 
     @Test
     public void mouseHoverOutsideOfCellsUnHighlights() throws Exception {
         TicTacToeGridModel model = grid.getModel();
         model.highlightCell(Grid.Location.of(Grid.Row.First, Grid.Column.Third));
         MouseEvent move = buildMouseMovedEvent(outsideAnyCell);
 
         grid.raiseMouseEvent(move);
 
         assertThat(model.hasHighlightedCell(), is(false));
     }
 }
