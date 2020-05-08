 package tests;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import parser.GameParser;
 
 import exceptions.InvalidBoardFileException;
 import exceptions.InvalidBoardSizeException;
 
 public class GameParserTest {
 
 	@SuppressWarnings("unused")
 	@Test(expected = FileNotFoundException.class)
 	public void testFileNotFound() throws IOException,
 			InvalidBoardFileException, InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(new File(
 				"boards/tests/FileNotFound.board"));
 	}
 
 	@Test(expected = InvalidBoardSizeException.class)
 	public void testInvalidBoardSize() throws IOException,
 			InvalidBoardFileException, InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(new File(
 				"boards/tests/InvalidBoardSize.board"));
 		parser.parse();
 	}
 
 	@Test(expected = InvalidBoardFileException.class)
 	public void testCorruptFile() throws IOException,
 			InvalidBoardFileException, InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(new File(
 				"boards/tests/CorruptFile.board"));
 		parser.parse();
 	}
 
 	@Test(expected = InvalidBoardFileException.class)
 	public void testInvalidTile() throws IOException,
 			InvalidBoardFileException, InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(new File(
 				"boards/tests/InvalidTile.board"));
 		parser.parse();
 	}
 
 	@Test
 	public void testValidTileRotation() throws IOException,
 			InvalidBoardFileException, InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(new File(
 				"boards/tests/ValidTileRotation.board"));
 		parser.parse();
 	}
 
 	@Test(expected = InvalidBoardFileException.class)
 	public void testInvalidTileRotation() throws IOException,
 			InvalidBoardFileException, InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(new File(
 				"boards/tests/InvalidTileRotation.board"));
 		parser.parse();
 	}
 
 	@Test
 	public void testValidTileColor() throws IOException,
 			InvalidBoardFileException, InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(new File(
 				"boards/tests/ValidTileColor.board"));
 		parser.parse();
 	}
 
 	@Test(expected = InvalidBoardFileException.class)
 	public void testInvalidTileColor() throws IOException,
 			InvalidBoardFileException, InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(new File(
 				"boards/tests/InvalidTileColor.board"));
 		parser.parse();
 	}
 
 	@Test
 	public void testSpaces() throws IOException, InvalidBoardFileException,
 			InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(
 				new File("boards/tests/Spaces.board"));
 		parser.parse();
 	}
 
	@Test
 	public void testMissingParameters() throws IOException, InvalidBoardFileException,
 			InvalidBoardSizeException {
 
 		GameParser parser = new GameParser(
 				new File("boards/tests/MissingParameters.board"));
 		parser.parse();
 	}
 	
 }
