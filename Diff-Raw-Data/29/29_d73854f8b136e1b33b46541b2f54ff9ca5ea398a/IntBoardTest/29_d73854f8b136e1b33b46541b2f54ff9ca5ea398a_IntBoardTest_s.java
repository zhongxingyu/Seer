 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 
 public class IntBoardTest {
 
 
 	@Test
 	public void testAdjacency0()
 	{
 		IntBoard board=new IntBoard(4,4);
 		HashSet<Integer> testList = board.getAdjList(0);
 		Assert.assertTrue(testList.contains(4));
 		Assert.assertTrue(testList.contains(1));
 		Assert.assertEquals(2, testList.size());
 	}
 	@Test
 	public void testTargets0_3()
 	{
 		IntBoard board=new IntBoard(4,4);
 		board.startTargets(0, 3);
 		HashSet<Integer> targets= board.getTargets();
 		Assert.assertEquals(6, targets.size());
 		Assert.assertTrue(targets.contains(12));
 		Assert.assertTrue(targets.contains(9));
 		Assert.assertTrue(targets.contains(1));
 		Assert.assertTrue(targets.contains(6));
 		Assert.assertTrue(targets.contains(3));
 		Assert.assertTrue(targets.contains(4));
 	}
 	@Test
 	public void testRooms() throws FileNotFoundException{
 		int [] boardSize = {23,23};							//setting board dimenstions to be 23x23
 		Board board = new Board();		//creating a board
 		IntBoard intBoard= new IntBoard();						//creating a new IntBoard
 		Assert.assertEquals(11, board.getRooms().size());				//tell the number of rooms. walkway is a room
 		//Jon got rid of the monster buggy test that was here. Writing a new one.
 		/*for(int i=0;i<boardSize[0];i++){
 			for(int j=0;j<boardSize[1];j++){
 				System.out.print(board.getRoomCellAt(i,j).getRoom());
 			}
 			System.out.print("\n");
 		}*/
		System.out.println(board.getRoomCellAt(0, 23).getRoom());
 		Assert.assertEquals('c', board.getRoomCellAt(3, 0).getRoom());
 		Assert.assertEquals('c', board.getRoomCellAt(4, 4).getRoom());
 		Assert.assertEquals('r', board.getRoomCellAt(4, 8).getRoom());
		Assert.assertEquals('r', board.getRoomCellAt(3, 19).getRoom());
 		Assert.assertEquals(board.getNumRows(), boardSize[1]);
 		Assert.assertEquals(board.getNumColumns(), boardSize[1]);
 	}
 }
