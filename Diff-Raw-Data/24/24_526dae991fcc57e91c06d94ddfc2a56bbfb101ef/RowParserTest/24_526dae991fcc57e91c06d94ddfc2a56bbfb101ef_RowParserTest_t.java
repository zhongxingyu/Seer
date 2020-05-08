 package ch.zhaw.ocr.BitmapParser;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 
 public class RowParserTest {
 
 	private ContrastMatrix inputMatrix;
 	private List<ContrastMatrix> expectedResultList;
 	
 	private Mockery context;
 	
 	
 	@Before
 	public void setUp() throws Exception {
 		context = new JUnit4Mockery();
 				
 		inputMatrix = new ContrastMatrix(10, 20);
 		
 		fillRow(inputMatrix, 0);
 		fillRow(inputMatrix, 1);
 		fillRow(inputMatrix, 2);
 		
 		fillRow(inputMatrix, 6);
 		fillRow(inputMatrix, 7);
 		fillRow(inputMatrix, 8);
 		fillRow(inputMatrix, 9);
 		fillRow(inputMatrix, 10);
 	
 		fillRow(inputMatrix, 16);
 		fillRow(inputMatrix, 17);
 		fillRow(inputMatrix, 18);
 		fillRow(inputMatrix, 19);
 		
 		//set up desired result		
 		expectedResultList= new LinkedList<ContrastMatrix>();
 		ContrastMatrix cm = new ContrastMatrix(10, 3);
 		cm.invertMatrix();
 		expectedResultList.add(cm);
 		expectedResultList.add(new ContrastMatrix(FunctionalCharacter.carriageReturn));
 		
 		
 		cm = new ContrastMatrix(10, 5);
 		cm.invertMatrix();
 		expectedResultList.add(cm);
 		expectedResultList.add(new ContrastMatrix(FunctionalCharacter.carriageReturn));
 		
 		
 		cm = new ContrastMatrix(10, 4);
 		cm.invertMatrix();
 		expectedResultList.add(cm);
 		
 	}
 	
 	private void fillRow(ContrastMatrix cm, int rowNo){
		for(int x = 0;x < cm.getWidth();x++){
 				cm.setValue(x, rowNo, 1);
 		}
 	}
 
 	@Test
 	public void testParse() {
 		//input matrices
 		final List<ContrastMatrix> inputList = new LinkedList<ContrastMatrix>();
 		inputList.add(inputMatrix);
 		
 		
 		final BitmapParser bp = context.mock(BitmapParser.class);
 		
 		context.checking(new Expectations() {{
 			oneOf (bp).parse(null); will(returnValue(inputList));
 	    }});
 
 		
 		BitmapParser instance = new RowParser(bp);
 		List<ContrastMatrix> parsedList = instance.parse(null);
		for (ContrastMatrix m : expectedResultList) {
			System.out.println(m);
		}
		System.out.println("----------- result --------");
		for (ContrastMatrix m : parsedList) {
			System.out.println(m);
		}
 		assertTrue(parsedList.equals(expectedResultList));
 	}
 	
 
 }
