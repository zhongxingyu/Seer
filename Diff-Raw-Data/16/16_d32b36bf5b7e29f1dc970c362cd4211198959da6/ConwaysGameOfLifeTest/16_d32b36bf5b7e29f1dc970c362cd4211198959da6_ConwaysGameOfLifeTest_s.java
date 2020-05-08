 package com.soebes.learning.coderetraet;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.testng.annotations.Test;
 
 /**
  * <pre>
  *     0   1   2
  *   +---+---+---+
  * 0 !   !   !   !
  *   +---+---+---+
  * 1 !   !   !   !
  *   +---+---+---+
  * 2 !   !   !   !
  *   +---+---+---+
  * </pre>
  * 
  * @author Karl-Heinz Marbaise
  *
  */
 public class ConwaysGameOfLifeTest {
 
     @Test
 	public void firstTest() {
		Universe universe = new Universe(3);
 		universe.setCellState(new Point(0,0), CellState.Alive);
 		CellState result = universe.getCellState(new Point(0, 0)); 
 		assertThat(result).isEqualTo(CellState.Alive);
 	}
 
 	@Test
 	public void secondTest() {
		Universe universe = new Universe(3);
 		universe.setCellState(new Point(1,0), CellState.Alive);
 		assertThat(universe.getCellState(new Point(1, 0))).isEqualTo(CellState.Alive);
 	}
 
 	@Test
     public void thirdTest() {
        Universe universe = new Universe(3);
         assertThat(universe.getCellState(new Point(1, 0))).isEqualTo(CellState.Dead);
     }
 }
