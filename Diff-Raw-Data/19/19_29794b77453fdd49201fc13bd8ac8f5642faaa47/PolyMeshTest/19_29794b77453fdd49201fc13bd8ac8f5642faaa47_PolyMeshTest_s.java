 package net.untoldwind.moredread.model.mesh.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import net.untoldwind.moredread.model.mesh.PolyFace;
 import net.untoldwind.moredread.model.mesh.PolyMesh;
 import net.untoldwind.moredread.model.mesh.Vertex;
 import net.untoldwind.moredread.model.state.XMLStateWriter;
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 import org.junit.Test;
 
 import com.jme.math.Vector3f;
 
 public class PolyMeshTest {
 	private final static boolean DEBUG = false;
 
 	@Test
 	public void testSimpleCreate() {
 		final PolyMesh cube = new PolyMesh();
 
 		// Left Back Bottom
 		final Vertex vertex1 = cube.addVertex(new Vector3f(-1, -1, -1));
 		assertNotNull("vertex1", vertex1);
 		assertEquals("vertex1.index", 0, vertex1.getIndex());
 		// Right Back Bottom
 		final Vertex vertex2 = cube.addVertex(new Vector3f(1, -1, -1));
 		assertNotNull("vertex2", vertex2);
 		assertEquals("vertex2.index", 1, vertex2.getIndex());
 		// Right Front Bottom
 		final Vertex vertex3 = cube.addVertex(new Vector3f(1, 1, -1));
 		assertNotNull("vertex3", vertex3);
 		assertEquals("vertex3.index", 2, vertex3.getIndex());
 		// Left Front Bottom
 		final Vertex vertex4 = cube.addVertex(new Vector3f(-1, 1, -1));
 		assertNotNull("vertex4", vertex4);
 		assertEquals("vertex4.index", 3, vertex4.getIndex());
 		// Left Back Top
 		final Vertex vertex5 = cube.addVertex(new Vector3f(-1, -1, 1));
 		assertNotNull("vertex5", vertex5);
 		assertEquals("vertex5.index", 4, vertex5.getIndex());
 		// Right Back Top
 		final Vertex vertex6 = cube.addVertex(new Vector3f(1, -1, 1));
 		assertNotNull("vertex6", vertex6);
 		assertEquals("vertex6.index", 5, vertex6.getIndex());
 		// Right Front Top
 		final Vertex vertex7 = cube.addVertex(new Vector3f(1, 1, 1));
 		assertNotNull("vertex7", vertex7);
 		assertEquals("vertex7.index", 6, vertex7.getIndex());
 		// Left Front Top
 		final Vertex vertex8 = cube.addVertex(new Vector3f(-1, 1, 1));
 		assertNotNull("vertex8", vertex8);
 		assertEquals("vertex8.index", 7, vertex8.getIndex());
 
 		final PolyFace bottom = cube.addFace(0, 1, 2, 3);
 		assertNotNull("bottom", bottom);
		assertEquals("bottom.index", 0, bottom.getIndex());
 		assertEquals("bottom.verticies", 4, bottom.getVertices().size());
 		final PolyFace top = cube.addFace(4, 5, 6, 7);
 		assertNotNull("top", top);
		assertEquals("top.index", 1, top.getIndex());
 		assertEquals("top.verticies", 4, top.getVertices().size());
 		final PolyFace left = cube.addFace(0, 3, 7, 4);
 		assertNotNull("left", left);
		assertEquals("left.index", 2, left.getIndex());
 		assertEquals("left.verticies", 4, left.getVertices().size());
 		final PolyFace right = cube.addFace(1, 2, 6, 5);
 		assertNotNull("right", right);
		assertEquals("right.index", 3, right.getIndex());
 		assertEquals("right.verticies", 4, right.getVertices().size());
 		final PolyFace back = cube.addFace(0, 1, 5, 4);
 		assertNotNull("back", back);
		assertEquals("back.index", 4, back.getIndex());
 		assertEquals("back.verticies", 4, back.getVertices().size());
 		final PolyFace front = cube.addFace(2, 3, 7, 6);
 		assertNotNull("front", front);
		assertEquals("front.index", 5, front.getIndex());
 		assertEquals("front.verticies", 4, front.getVertices().size());
 
 		assertEquals("vertex 0", vertex1, cube.getVertex(0));
 		assertEquals("vertex 1", vertex2, cube.getVertex(1));
 		assertEquals("vertex 2", vertex3, cube.getVertex(2));
 		assertEquals("vertex 3", vertex4, cube.getVertex(3));
 		assertEquals("vertex 4", vertex5, cube.getVertex(4));
 		assertEquals("vertex 5", vertex6, cube.getVertex(5));
 		assertEquals("vertex 6", vertex7, cube.getVertex(6));
 		assertEquals("vertex 7", vertex8, cube.getVertex(7));
 
 		assertEquals(3, vertex1.getFaces().size());
 		assertTrue(vertex1.getFaces().contains(left));
 		assertTrue(vertex1.getFaces().contains(bottom));
 		assertTrue(vertex1.getFaces().contains(back));
 
 		assertEquals(3, vertex2.getFaces().size());
 		assertTrue(vertex2.getFaces().contains(right));
 		assertTrue(vertex2.getFaces().contains(bottom));
 		assertTrue(vertex2.getFaces().contains(back));
 
 		assertEquals(3, vertex3.getFaces().size());
 		assertTrue(vertex3.getFaces().contains(right));
 		assertTrue(vertex3.getFaces().contains(bottom));
 		assertTrue(vertex3.getFaces().contains(front));
 
 		assertEquals(3, vertex4.getFaces().size());
 		assertTrue(vertex4.getFaces().contains(left));
 		assertTrue(vertex4.getFaces().contains(bottom));
 		assertTrue(vertex4.getFaces().contains(front));
 
 		assertEquals(3, vertex5.getFaces().size());
 		assertTrue(vertex5.getFaces().contains(left));
 		assertTrue(vertex5.getFaces().contains(top));
 		assertTrue(vertex5.getFaces().contains(back));
 
 		assertEquals(3, vertex6.getFaces().size());
 		assertTrue(vertex6.getFaces().contains(right));
 		assertTrue(vertex6.getFaces().contains(top));
 		assertTrue(vertex6.getFaces().contains(back));
 
 		assertEquals(3, vertex7.getFaces().size());
 		assertTrue(vertex7.getFaces().contains(right));
 		assertTrue(vertex7.getFaces().contains(top));
 		assertTrue(vertex7.getFaces().contains(front));
 
 		assertEquals(3, vertex8.getFaces().size());
 		assertTrue(vertex8.getFaces().contains(left));
 		assertTrue(vertex8.getFaces().contains(top));
 		assertTrue(vertex8.getFaces().contains(front));
 	}
 
 	@Test
 	public void writeStateTest() throws Exception {
 		final PolyMesh cube = createCube();
 		final XMLStateWriter writer = new XMLStateWriter("mesh");
 
 		cube.writeState(writer);
 
 		final SAXReader reader = new SAXReader();
 		reader.setIgnoreComments(true);
 		reader.setStripWhitespaceText(true);
 		final Document expected = reader.read(getClass().getResourceAsStream(
 				"polymesh-state1.xml"));
 
 		if (DEBUG) {
 			final XMLWriter xmlWriter = new XMLWriter(System.out, OutputFormat
 					.createPrettyPrint());
 			xmlWriter.write(writer.getDocument());
 			xmlWriter.flush();
 		}
 
 		assertDocument(expected, writer.getDocument());
 	}
 
 	private void assertDocument(final Document expected, final Document actual) {
 		assertElement(expected.getRootElement(), actual.getRootElement());
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void assertElement(final Element expected, final Element actual) {
 		assertEquals(expected.getPath(), expected.getName(), actual.getName());
 		assertEquals(expected.getPath(), expected.getText().trim(), actual
 				.getText().trim());
 
 		final List<Element> expectedElements = expected.elements();
 		final List<Element> actualElements = actual.elements();
 
 		assertEquals(expected.getPath(), expectedElements.size(),
 				actualElements.size());
 		for (int i = 0; i < expectedElements.size(); i++) {
 			assertElement(expectedElements.get(i), actualElements.get(i));
 		}
 	}
 
 	private PolyMesh createCube() {
 		final PolyMesh cube = new PolyMesh();
 
 		cube.addVertex(new Vector3f(-1, -1, -1));
 		cube.addVertex(new Vector3f(1, -1, -1));
 		cube.addVertex(new Vector3f(1, 1, -1));
 		cube.addVertex(new Vector3f(-1, 1, -1));
 		cube.addVertex(new Vector3f(-1, -1, 1));
 		cube.addVertex(new Vector3f(1, -1, 1));
 		cube.addVertex(new Vector3f(1, 1, 1));
 		cube.addVertex(new Vector3f(-1, 1, 1));
 
 		cube.addFace(0, 1, 2, 3);
 		cube.addFace(4, 5, 6, 7);
 		cube.addFace(0, 3, 7, 4);
 		cube.addFace(1, 2, 6, 5);
 		cube.addFace(0, 1, 5, 4);
 		cube.addFace(2, 3, 7, 6);
 
 		return cube;
 	}
 }
