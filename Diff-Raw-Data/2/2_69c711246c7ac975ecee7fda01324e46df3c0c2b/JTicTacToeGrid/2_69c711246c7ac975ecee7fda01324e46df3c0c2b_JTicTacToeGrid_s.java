 package com.crudetech.tictactoe.client.swing.grid;
 
 import com.crudetech.collections.Pair;
 import com.crudetech.event.Event;
 import com.crudetech.event.EventListener;
 import com.crudetech.event.EventObject;
 import com.crudetech.event.EventSupport;
 import com.crudetech.functional.UnaryFunction;
 import com.crudetech.tictactoe.game.Grid;
 
 import javax.swing.*;
 import javax.swing.plaf.ComponentUI;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Objects;
 
 import static com.crudetech.query.Query.from;
 
 public class JTicTacToeGrid extends JComponent {
     private final TicTacToeGridModel model;
     private EventSupport<CellClickedEventObject> clickedEvent = new EventSupport<>();
 
     static {
         UIManager.getDefaults().put(TicTacToeGridUI.class.getSimpleName(), TicTacToeGridUI.class.getName());
     }
 
     public JTicTacToeGrid() {
         this(new TicTacToeGridModel());
         updateUI();
     }
 
     public JTicTacToeGrid(TicTacToeGridUI gridUi) {
         this(new TicTacToeGridModel());
         setUI(gridUi);
     }
 
     public JTicTacToeGrid(TicTacToeGridModel model) {
         this.model = model;
         updateUI();
 
         model.changed().addListener(new EventListener<Model.ChangedEventObject<Model<Grid>>>() {
             @Override
             public void onEvent(Model.ChangedEventObject<Model<Grid>> e) {
                 repaint();
             }
         });
 
         addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 onMouseClicked(e);
             }
         });
     }
 
     private void onMouseClicked(MouseEvent e) {
         CellHit hit = cellHitFromMouseEvent(e);
         if (hit.hasHit()) {
             clickedEvent.fireEvent(new CellClickedEventObject(JTicTacToeGrid.this, hit.getHit()));
         }
     }
 
     private CellHit cellHitFromMouseEvent(MouseEvent e) {
         Iterable<Grid.Cell> allCells = getModel().getModelObject().getCells();
         Rectangle[][] hitBoundaries = getUI().getStyle().getGridMarkLocations();
         return new CellHit(allCells, e.getX(), e.getY(), hitBoundaries);
     }
 
     private static class CellHit {
         private final Pair<Rectangle, Grid.Location> hitInfo;
 
         CellHit(Iterable<Grid.Cell> cells, int x, int y, Rectangle[][] cellBoundaries) {
             hitInfo = from(cells).select(isContainedIn(x, y, cellBoundaries)).where(notNoHit()).firstOr(NoHit);
         }
 
         Grid.Location getHit() {
             return hitInfo.getSecond();
         }
 
         boolean hasHit() {
             return !NoHit.equals(hitInfo);
         }
 
         private static Pair<Rectangle, Grid.Location> NoHit = new Pair<>(new Rectangle(-1, -1, -1, -1), Grid.Location.of(Grid.Row.First, Grid.Column.First));
 
         private UnaryFunction<Pair<Rectangle, Grid.Location>, Boolean> notNoHit() {
             return new UnaryFunction<Pair<Rectangle, Grid.Location>, Boolean>() {
                 @Override
                 public Boolean execute(Pair<Rectangle, Grid.Location> rectangleLocationPair) {
                     return !rectangleLocationPair.equals(NoHit);
                 }
             };
         }
 
         private UnaryFunction<Grid.Cell, Pair<Rectangle, Grid.Location>> isContainedIn(final int x, final int y, final Rectangle[][] gridMarkLocations) {
             return new UnaryFunction<Grid.Cell, Pair<Rectangle, Grid.Location>>() {
                 @Override
                 public Pair<Rectangle, Grid.Location> execute(Grid.Cell cell) {
                     Grid.Location location = cell.getLocation();
                     Rectangle hitRect = gridMarkLocations[location.getRow().ordinal()][location.getColumn().ordinal()];
                     if (hitRect.contains(x, y)) {
                         return new Pair<>(hitRect, location);
                     }
                     return NoHit;
                 }
             };
         }
     }
 
     @Override
     public String getUIClassID() {
         return TicTacToeGridUI.class.getSimpleName();
     }
 
     TicTacToeGridUI getUI() {
         return (TicTacToeGridUI) this.ui;
     }
 
     @Override
     public void setUI(ComponentUI newUI) {
         super.setUI(newUI);
     }
 
     @Override
     public void updateUI() {
         setUI(UIManager.getUI(this));
     }
 
     public TicTacToeGridModel getModel() {
         return model;
     }
 
     void raiseMouseEvent(MouseEvent e) {
         super.processMouseEvent(e);
     }
 
     public static class CellClickedEventObject extends EventObject<JTicTacToeGrid> {
         private final Grid.Location expectedCell;
 
        private CellClickedEventObject(JTicTacToeGrid jTicTacToeGrid, Grid.Location expectedCell) {
             super(jTicTacToeGrid);
             this.expectedCell = expectedCell;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             if (!super.equals(o)) return false;
 
             CellClickedEventObject that = (CellClickedEventObject) o;
 
             return Objects.equals(expectedCell, that.expectedCell);
         }
 
         @Override
         public int hashCode() {
             int result = super.hashCode();
             result = 31 * result + Objects.hashCode(expectedCell);
             return result;
         }
 
         @Override
         public String toString() {
             return "CellClickedEventObject{" +
                     "source=" + getSource() +
                     "expectedCell=" + expectedCell +
                     '}';
         }
     }
 
     public Event<CellClickedEventObject> cellClicked() {
         return clickedEvent;
     }
 }
