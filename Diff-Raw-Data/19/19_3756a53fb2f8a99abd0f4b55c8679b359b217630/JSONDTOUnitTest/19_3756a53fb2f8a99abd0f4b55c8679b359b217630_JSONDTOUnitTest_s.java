 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import jsondto.JSONDTO;
 import jsondto.JSONDTORepresentable;
 import jsondto.Util;
 
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 /**
  * Unit tests for the utilities used by the JSON-DTO module.
  * 
  * @author Jarno Rantanen <jarno@jrw.fi>
  */
 public class JSONDTOUnitTest extends UnitTest {
 
 	/**
 	 * This is a test class standing in for an actual Model object.
 	 * 
 	 */
 	private class Note implements JSONDTORepresentable<Note.DTO> {
 
 		private String title;
 
 		public class DTO implements JSONDTO {
 			public String note_title;
 		}
 
 		@Override
 		public void merge(DTO dto) {
 			this.title = dto.note_title;
 		}
 
 		@Override
 		public DTO toDTO() {
 			DTO dto = new DTO();
 			dto.note_title = this.title;
 			return dto;
 		}
 
 	}
 
 	@Test
 	public void testSimpleSerialization() {
 
 		Note note = new Note();
 		note.title = "foo";
 
 		assertEquals("{\"note_title\":\"foo\"}", Util.getDTOsAsString(note));
 
 	}
 
 	@Test
 	public void testListSerialization() {
 
 		Note note1 = new Note();
 		note1.title = "foo";
 
 		Note note2 = new Note();
 		note2.title = "bar";
 
 		List notes = new ArrayList<Note>();
 		notes.add(note1);
 		notes.add(note2);
 
 		assertEquals("[{\"note_title\":\"foo\"},{\"note_title\":\"bar\"}]", Util.getDTOsAsString(notes));
 
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testIllegalArgumentForSerialization() {
 
 		Util.getDTOsAsString(new Object());
 
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testIllegalArgumentForListSerialization() {
 
 		List list = new ArrayList();
 		list.add(new Object());
 
 		Util.getDTOsAsString(list);
 
 	}
 
 	@Test
 	public void testNullSerialization() {
 
 		assertEquals("null", Util.getDTOsAsString(null));
 
 	}
 
 	@Test
 	public void testEmptyListSerialization() {
 
 		assertEquals("[]", Util.getDTOsAsString(new ArrayList()));
 
 	}
 
 	@Test
 	public void testEmptyObjectSerialization() {
 
 		class EmptyModel implements JSONDTORepresentable<EmptyModel.DTO> {
 
 			class DTO implements JSONDTO {
 			}
 
 			@Override
 			public void merge(DTO dto) {
 			}
 
 			@Override
 			public DTO toDTO() {
 				return new DTO();
 			}
 
 		}
 
 		assertEquals("{}", Util.getDTOsAsString(new EmptyModel()));
 
 	}
 
 	@Test
 	public void testReadingInStreams() throws IOException {
 
 		InputStream is = new ByteArrayInputStream("ABC".getBytes(Util.CHARSET));
 
 		assertEquals("ABC", Util.readStream(is));
 
 	}
 
 }
