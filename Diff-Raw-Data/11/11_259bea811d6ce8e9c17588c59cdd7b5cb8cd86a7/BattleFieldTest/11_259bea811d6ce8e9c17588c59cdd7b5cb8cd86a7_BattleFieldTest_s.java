 import static org.junit.Assert.*;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class BattleFieldTest {
 	
 	BattleField BF;
 	File tmp;
 	private BufferedWriter bw = null;
 	@Before
 	public void init() throws IOException{
 			tmp = File.createTempFile("testfile", ".txt");
 			tmp.deleteOnExit();
 			try {
 				bw = new BufferedWriter(new FileWriter(tmp.getName(), true));
 				bw.newLine();
 				bw.write("4|4|4 $2A2 $1G3 $4 $"+"\n");
 				bw.close();
 			} catch (IOException e) {
 				System.out.println("IOException in the write method !");
 			} 
 		try {
 			BF = new BattleField(tmp.getName());
 		} catch (IllegalElementException e){ 
 			e.printStackTrace();
 		} catch (IllegalPositionException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testBattleField() {
 		assertEquals(0, BF.getScore());
 		assertEquals(10, BF.getLife());
 	}
 	
 	@Test
 	public void testSetGetFilename() {
 		assertEquals(tmp.getName(), BF.getFilename());
 		BF.setFilename("filenametest.txt");
 		assertEquals("filenametest.txt", BF.getFilename());
 	}
 
 	@Test
 	public void testGetRows() {
 		assertEquals(4, BF.getRows());
 	}
 
 	@Test
 	public void testSetRows() {
 		assertEquals(4, BF.getRows());
 		BF.setRows(6);
 		assertEquals(6, BF.getRows());
 	}
 
 	@Test
 	public void testGetColumns() {
 		assertEquals(4, BF.getColumns());
 	}
 
 	@Test
 	public void testSetColumns() {
 		assertEquals(4, BF.getColumns());
 		BF.setColumns(2);
 		assertEquals(2, BF.getColumns());
 		
 	}
 	
 	@Test
 	public void testGetBattleFieldElement() throws IllegalElementException {
 		BattleFieldElement BFTest[][] = new BattleFieldElement[BF.getRows()][BF.getColumns()];
 		BFTest[0][0] = new Empty(0,0);
 		BFTest[0][1] = new Empty(0,1);
 		BFTest[0][2] = new Empty(0,2);
 		BFTest[0][3] = new Empty(0,3);
 		
 		BFTest[1][0] = new OneStepAlien(1,0);
 		BFTest[1][1] = new OneStepAlien(1,1);
 		BFTest[1][2] = new Empty(1,2);
 		BFTest[1][3] = new Empty(1,3);
 		
 		BFTest[2][0] = new Gun(2,0);
 		BFTest[2][1] = new Empty(2,1);
 		BFTest[2][2] = new Empty(2,2);
 		BFTest[2][3] = new Empty(2,3);
 		
 		BFTest[3][0] = new Empty(3,0);
 		BFTest[3][1] = new Empty(3,1);
 		BFTest[3][2] = new Empty(3,2);
 		BFTest[3][3] = new Empty(3,3);
 		
 		boolean equals = true;
 		for(int r=0; r<BF.getRows(); r++){
 			for(int c=0; c<BF.getRows(); c++){
 				assertEquals(BFTest[r][c], BF.getBattleFieldElement(r,c));
 			}
 		}
 	}
 
 	@Test
 	public void testToString() {
 		assertEquals("4|4|4 $2A2 $1G3 $4 $", BF.toString());
 	}
 
 	@Test
 	public void testBackup() {
 		fail("Not yet implemented");
 	}
 
 	@Test
	public void testGetBattleField() throws IllegalElementException, IllegalPositionException {
 		assertEquals("4|4|4 $2A2 $1G3 $4 $", BF.toString());
 		BF.move();
 		assertEquals("4|4|4 $2A2 $1G3 $4 $",BF.toString());
 		
 	}
 
 	@Test
 	public void testWrite() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testReload() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testClone() throws CloneNotSupportedException {
 		
 	}
 
 	@Test
 	public void testSetBattleFieldElement() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testSetBattleField() {
 		fail("Not yet implemented");
 	}
 
 	@Test
	public void testMoveAS() throws IllegalElementException, IllegalPositionException {
 		BF.move();
 		BattleFieldElement BFTest[][] = new BattleFieldElement[BF.getRows()][BF.getColumns()];
 		BFTest[0][0] = new Empty(0,0);
 		BFTest[0][1] = new Empty(0,1);
 		BFTest[0][2] = new Empty(0,2);
 		BFTest[0][3] = new Empty(0,3);
 		
 		BFTest[1][0] = new Empty(1,0);
 		BFTest[1][1] = new OneStepAlien(1,1);
 		BFTest[1][2] = new OneStepAlien(1,2);
 		BFTest[1][3] = new Empty(1,3);
 		
 		BFTest[2][0] = new Gun(2,0);
 		BFTest[2][1] = new Empty(2,1);
 		BFTest[2][2] = new Empty(2,2);
 		BFTest[2][3] = new Empty(2,3);
 		
 		BFTest[3][0] = new Empty(3,0);
 		BFTest[3][1] = new Empty(3,1);
 		BFTest[3][2] = new Empty(3,2);
 		BFTest[3][3] = new Empty(3,3);
 		
 		boolean equals = true;
 		for(int r=0; r<BF.getRows(); r++){
 			for(int c=0; c<BF.getRows(); c++){
 				assertEquals(BFTest[r][c], BF.getBattleFieldElement(r,c));
 			}
 		}
 	}
 	@Test
 	
	public void testMoveR() throws IllegalElementException, IllegalPositionException, IOException {
 		File tmp = File.createTempFile("MoveRTest", ".txt");
 		tmp.deleteOnExit();
 		try {
 			bw = new BufferedWriter(new FileWriter(tmp.getName(), true));
 			bw.newLine();
 			bw.write("4|4|3 1R$4 $1G3 $4 $"+"\n");
 			bw.close();
 		} catch (IOException e) {
 			System.out.println("IOException in the write method !");
 		} 
 		BattleField BFR = new BattleField(tmp.getName());
		BFR.move();
 		BattleFieldElement BFTest[][] = new BattleFieldElement[BFR.getRows()][BFR.getColumns()];
 		BFTest[0][0] = new Empty(0,0);
 		BFTest[0][1] = new Empty(0,1);
 		BFTest[0][2] = new RedSpacecraft(0,2);
 		BFTest[0][3] = new Empty(0,3);
 		
 		BFTest[1][0] = new Empty(1,0);
 		BFTest[1][1] = new Empty(1,1);
 		BFTest[1][2] = new Empty(1,2);
 		BFTest[1][3] = new Empty(1,3);
 		
 		BFTest[2][0] = new Gun(2,0);
 		BFTest[2][1] = new Empty(2,1);
 		BFTest[2][2] = new Empty(2,2);
 		BFTest[2][3] = new Empty(2,3);
 		
 		BFTest[3][0] = new Empty(3,0);
 		BFTest[3][1] = new Empty(3,1);
 		BFTest[3][2] = new Empty(3,2);
 		BFTest[3][3] = new Empty(3,3);
 		
 		boolean equals = true;
 		for(int r=0; r<BF.getRows(); r++){
 			for(int c=0; c<BF.getRows(); c++){
 				assertEquals(BFTest[r][c], BFR.getBattleFieldElement(r,c));
 			}
 		}
 	}
 	
 	/*public void testMoves() throws IllegalElementException, IllegalPositionException, IOException {
 		File tmp = File.createTempFile("MovesTest", ".txt");
 		tmp.deleteOnExit();
 		try {
 			bw = new BufferedWriter(new FileWriter(tmp.getName(), true));
 			bw.newLine();
 			bw.write("4|4|3 1R$4 $1G3 $4 $"+"\n");
 			bw.close();
 		} catch (IOException e) {
 			System.out.println("IOException in the write method !");
 		} 
 		BattleField BFR = new BattleField(tmp.getName());
 		BFR.move();
 		BattleFieldElement BFTest[][] = new BattleFieldElement[BFR.getRows()][BFR.getColumns()];
 		BFTest[0][0] = new Empty(0,0);
 		BFTest[0][1] = new Empty(0,1);
 		BFTest[0][2] = new RedSpacecraft(0,2);
 		BFTest[0][3] = new Empty(0,3);
 		
 		BFTest[1][0] = new Empty(1,0);
 		BFTest[1][1] = new Empty(1,1);
 		BFTest[1][2] = new Empty(1,2);
 		BFTest[1][3] = new Empty(1,3);
 		
 		BFTest[2][0] = new Gun(2,0);
 		BFTest[2][1] = new Empty(2,1);
 		BFTest[2][2] = new Empty(2,2);
 		BFTest[2][3] = new Empty(2,3);
 		
 		BFTest[3][0] = new Empty(3,0);
 		BFTest[3][1] = new Empty(3,1);
 		BFTest[3][2] = new Empty(3,2);
 		BFTest[3][3] = new Empty(3,3);
 		
 		boolean equals = true;
 		for(int r=0; r<BF.getRows(); r++){
 			for(int c=0; c<BF.getRows(); c++){
 				assertEquals(BFTest[r][c], BFR.getBattleFieldElement(r,c));
 			}
 		}
 	}
 	*/
 	@Test
 	public void testDoTheyMove() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testDoTheyShot() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testAlienCollide() throws IllegalElementException, IllegalPositionException, IOException {
 		/*File tmp = File.createTempFile("AlienCollideTest", ".txt");
 		tmp.deleteOnExit();
 		try {
 			bw = new BufferedWriter(new FileWriter(tmp.getName(), true));
 			bw.newLine();
 			bw.write("4|4|4 $1A2s1 $1G3 $4 $"+"\n");
 			bw.close();
 		} catch (IOException e) {
 			System.out.println("IOException in the write method !");
 		} 
 		BattleField BFTest = new BattleField(tmp.getName());
 		BFTest.move();
 		*/
 	}
 
 }
