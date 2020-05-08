 package experimental.sweeper;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author greg.matoga@gmail.com
  */
 public class SweeperControllerTest {
 
     private SweeperController sweeperController;
     private MineField mineField;
 
     @Before
     public void setUp() throws Exception {
         mineField = new MineField(1, 1);
         sweeperController = new SweeperController(mineField);
     }
 
     @Test
     public void aZeroMineFieldIsUncoveredDueToNeighboringMineBecameUncovered() {
         // 1 x 1
         // 1 1 1
         // 0 0 0
         //   ^
         //   To uncover
 
         // the situation illustrated above
         mineField = new MineField(3,3);
         mineField.get(0, 1).mine.set(true);
         sweeperController = new SweeperController(mineField);
 
         // uncovering designated element
         System.out.println("MOVING");
        sweeperController.moveOnFieldAt(2, 1);
 
        assertThat(mineField.get(2, 0).uncovered().get(), is(true));
        assertThat(mineField.get(2, 1).uncovered().get(), is(true));
         assertThat(mineField.get(2, 2).uncovered().get(), is(true));
 
        assertThat(mineField.get(1, 0).uncovered().get(), is(true));
        assertThat(mineField.get(1, 1).uncovered().get(), is(true));
        assertThat(mineField.get(1, 2).uncovered().get(), is(true));
     }
 
     @Test
     public void oneFieldGameNoMinesWins() {
         mineField.mineAt(0, 0).set(false);
         sweeperController.moveOnFieldAt(0, 0);
 
         assertWon();
     }
 
     @Test
     public void oneFieldGameWithMineLoses() {
         mineField.mineAt(0, 0).set(true);
         sweeperController.moveOnFieldAt(0, 0);
 
         assertLost();
     }
 
     @Test
     public void aFieldWithTwoMinesInNeighborhoodHasNeighbourCountTwo() {
         mineField = new MineField(3,3);
 
         mineField.get(0,0).mine.set(true);
         mineField.get(1,0).mine.set(true);
 
         Integer minesInNeighborhood = mineField.get(1,1).countMinesInNeighborhood().get();
 
         assertThat(minesInNeighborhood, is(equalTo(2)));
     }
 
     private void assertLost() {
         assertFinished();
         assertThat(sweeperController.isWon().get(), is(false));
     }
 
     private void assertWon() {
         assertFinished();
         assertThat(sweeperController.isWon().get(), is(true));
     }
 
     private void assertFinished() {
         assertThat(sweeperController.isFinished().get(), is(true));
     }
 }
