 package com.crudetech.tictactoe.client.swing.grid;
 
 import com.crudetech.tictactoe.game.Grid;
 import com.crudetech.tictactoe.game.LinearRandomAccessGrid;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import static com.crudetech.matcher.RangeIsEquivalent.equivalentTo;
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 public class TicTacToeGridUITest {
 
     private Graphics2D g2d;
     private JTicTacToeGrid grid;
     private TicTacToeGridUI ui;
     private Style style;
     private final int paintOffsetX = 250;
     private final int paintOffsetY = 500;
 
     @Before
     public void setUp() throws Exception {
         g2d = mock(Graphics2D.class);
 
         TicTacToeGridModel model = new TicTacToeGridModel(
                 LinearRandomAccessGrid.of(
                         Grid.Mark.Cross, Grid.Mark.Nought, Grid.Mark.None,
                         Grid.Mark.Cross, Grid.Mark.None, Grid.Mark.None,
                         Grid.Mark.Nought, Grid.Mark.Nought, Grid.Mark.Cross
                 ));
 
         grid = new JTicTacToeGrid(model);
         grid.setSize(1000, 2000);
 
         ui = grid.getUI();
 
         style = new StyleStub();
         ui.setStyle(style);
     }
 
     @Test
     public void backGroundIsPaintedInMiddle() {
         grid.getUI().paint(g2d);
 
         verify(g2d).drawImage(style.getBackgroundImage(), null, 250, 500);
     }
 
     @Test
     public void backgroundIsPositionedAtOriginIfComponentIsSmaller() {
         grid.setSize(10, 10);
         ui.paint(g2d);
 
         verify(g2d).drawImage(style.getBackgroundImage(), null, 0, 0);
     }
 
     @Test
     public void backGroundIsInvalidated() {
         ui.paint(g2d);
 
         List<Widget> widgets = ui.buildPaintList();
         Widget background = widgets.get(0);
 
         Widget expectedBackground = new FilledRectangleWidget(
                 new Rectangle(0, 0, grid.getWidth(), grid.getHeight()), style.getBackgroundColor());
         assertThat(background, is(expectedBackground));
     }
 
     @Test
     public void defaultStyleIsBrush() {
         grid = new JTicTacToeGrid();
         assertThat(Styles.Brush, is(grid.getUI().getStyle()));
     }
 
     @Test
     public void preferredSizeIsStyleSize() throws Exception {
         Dimension expected = style.getPreferredSize();
 
         assertThat(grid.getUI().getPreferredSize(grid), is(expected));
     }
 
     @Test
     public void gridMarksArePaintedFromModel() {
         List<Widget> widgets = ui.buildGridMarkWidgetList(paintOffsetX, paintOffsetY);
 
 
         List<Widget> expected = expectedGridMarkWidgets();
 
         assertThat(widgets, is(equivalentTo(expected)));
     }
 
     private List<Widget> expectedGridMarkWidgets() {
         final BufferedImage cross = style.getCrossImage();
         final BufferedImage nought = style.getNoughtImage();
         final Color backGroundColor = style.getBackgroundColor();
         Rectangle[][] locations = style.getGridMarkLocations();
 
         return asList(
                 new ImageWidget(loc(locations[0][0].getLocation()), cross),
                 new ImageWidget(loc(locations[0][1].getLocation()), nought),
                 new FilledRectangleWidget(loc(locations[0][2]), backGroundColor),
 
                 new ImageWidget(loc(locations[1][0].getLocation()), cross),
                 new FilledRectangleWidget(loc(locations[1][1]), backGroundColor),
                 new FilledRectangleWidget(loc(locations[1][2]), backGroundColor),
 
                 new ImageWidget(loc(locations[2][0].getLocation()), nought),
                 new ImageWidget(loc(locations[2][1].getLocation()), nought),
                 new ImageWidget(loc(locations[2][2].getLocation()), cross)
         );
     }
 
     private Rectangle loc(Rectangle rectangle) {
         Rectangle r = new Rectangle(rectangle);
         r.x += paintOffsetX;
         r.y += paintOffsetY;
         return r;
     }
 
     private Point loc(Point location) {
         return new Point(location.x + paintOffsetX, location.y + paintOffsetY);
     }
 
     @Test
     public void backgroundIsInMiddle() {
         List<Widget> widgets = ui.buildPaintList();
         Widget backgroundImage = widgets.get(1);
 
         assertThat(backgroundImage, is((Widget) new ImageWidget(new Point(paintOffsetX, paintOffsetY), style.getBackgroundImage())));
     }
 
 
     // paint stack is painted in order
     //  paint order: background invalidate, background image, marks
     // model change
     // setStyle triggers repaint
 }
