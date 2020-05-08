 package org.vaadin.smartgwt.server.layout;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.vaadin.smartgwt.server.Canvas;
 import org.vaadin.smartgwt.server.types.Alignment;
 
 public class BorderLayoutTest {
 	private BorderLayout borderLayout;
 
 	@Before
 	public void before() {
 		borderLayout = new BorderLayout();
 	}
 
 	@Test
 	public void test_createsNorthHolder() {
 		final HLayout north = getNorthHolder();
 		assertEquals(Integer.valueOf(0), north.getAttributeAsInt("height"));
 		assertEquals("100%", north.getWidthAsString());
 	}
 
 	@Test
 	public void test_createsSouthHolder() {
 		final HLayout south = getSouthHolder();
 		assertEquals(Integer.valueOf(0), south.getAttributeAsInt("height"));
 		assertEquals("100%", south.getWidthAsString());
 	}
 
 	@Test
 	public void test_createsCenterLayoutHolder() {
 		final HLayout centerLayout = (HLayout) borderLayout.getMember(1);
 		assertEquals("*", centerLayout.getHeightAsString());
 	}
 
 	@Test
 	public void test_createsWestHolder() {
 		final Canvas west = getWestHolder();
 		assertEquals(Integer.valueOf(0), west.getAttributeAsInt("width"));
 		assertEquals("100%", west.getHeightAsString());
 	}
 
 	@Test
 	public void test_createsCenterRightLayout() {
 		final Canvas centerRightLayout = ((Layout) borderLayout.getMember(1)).getMember(1);
 		assertEquals("*", centerRightLayout.getWidthAsString());
 		assertEquals(Alignment.RIGHT.getValue(), centerRightLayout.getAttribute("align"));
 	}
 
 	@Test
 	public void test_createsCenterHolder() {
 		final Canvas center = getCenterHolder();
 		assertEquals(Integer.valueOf(0), center.getAttributeAsInt("width"));
 	}
 
 	@Test
 	public void test_createsEastHolder() {
 		final Canvas east = getEastHolder();
 		assertEquals(Integer.valueOf(0), east.getAttributeAsInt("width"));
 	}
 
 	@Test
 	public void test_centerMemberReturnsNullIfNotSet() {
 		assertNull(borderLayout.getCenterMember());
 	}
 
 	@Test
 	public void test_returnsCenterMember() {
 		final Canvas member = new Canvas();
 		borderLayout.setCenterMember(member);
 		assertEquals(member, borderLayout.getCenterMember());
 	}
 
 	@Test
 	public void test_resizesCenterMemberToFull() {
 		Canvas member = new Canvas();
 		borderLayout.setCenterMember(member);
 		assertEquals("100%", member.getWidthAsString());
 		assertEquals("100%", member.getHeightAsString());
 	}
 
 	@Test
 	public void test_resizesCenterHolderToAutoFit() {
 		borderLayout.setCenterMember(new Canvas());
		assertEquals("100%", getCenterHolder().getWidthAsString());
 	}
 
 	@Test
 	public void test_resizesCenterHolderToNothingIfMemberIsNull() {
 		borderLayout.setCenterMember(new Canvas());
 		borderLayout.setCenterMember(null);
 		assertEquals(Integer.valueOf(0), getCenterHolder().getAttributeAsInt("width"));
 		assertEquals(Integer.valueOf(0), getCenterHolder().getAttributeAsInt("height"));
 	}
 
 	@Test
 	public void test_northMemberReturnsNullIfNotSet() {
 		assertNull(borderLayout.getNorthMember());
 	}
 
 	@Test
 	public void test_returnsNorthMember() {
 		final Canvas member = new Canvas();
 		borderLayout.setNorthMember(member);
 		assertEquals(member, borderLayout.getNorthMember());
 	}
 
 	@Test
 	public void test_resizesNorthMemberWidthToFull() {
 		Canvas member = new Canvas();
 		borderLayout.setNorthMember(member);
 		assertEquals("100%", member.getWidthAsString());
 	}
 
 	@Test
 	public void test_resizesNorthHolderToAutoFit() {
 		borderLayout.setNorthMember(new Canvas());
 		assertEquals(Integer.valueOf(1), getNorthHolder().getAttributeAsInt("height"));
 	}
 
 	@Test
 	public void test_resizesNorthHolderToNothingIfMemberIsNull() {
 		borderLayout.setNorthMember(new Canvas());
 		borderLayout.setNorthMember(null);
 		assertEquals(Integer.valueOf(0), getNorthHolder().getAttributeAsInt("height"));
 	}
 
 	@Test
 	public void test_southMemberReturnsNullIfNotSet() {
 		assertNull(borderLayout.getSouthMember());
 	}
 
 	@Test
 	public void test_returnsSouthMember() {
 		final Canvas member = new Canvas();
 		borderLayout.setSouthMember(member);
 		assertEquals(member, borderLayout.getSouthMember());
 	}
 
 	@Test
 	public void test_resizesSouthMemberWidthToFull() {
 		Canvas member = new Canvas();
 		borderLayout.setSouthMember(member);
 		assertEquals("100%", member.getWidthAsString());
 	}
 
 	@Test
 	public void test_resizesSouthHolderToAutoFit() {
 		borderLayout.setSouthMember(new Canvas());
 		assertEquals(Integer.valueOf(1), getSouthHolder().getAttributeAsInt("height"));
 	}
 
 	@Test
 	public void test_resizesSouthHolderToNothingIfMemberIsNull() {
 		borderLayout.setSouthMember(new Canvas());
 		borderLayout.setSouthMember(null);
 		assertEquals(Integer.valueOf(0), getSouthHolder().getAttributeAsInt("height"));
 	}
 
 	@Test
 	public void test_westMemberReturnsNullIfNotSet() {
 		assertNull(borderLayout.getWestMember());
 	}
 
 	@Test
 	public void test_returnsWestMember() {
 		final Canvas member = new Canvas();
 		borderLayout.setWestMember(member);
 		assertEquals(member, borderLayout.getWestMember());
 	}
 
 	@Test
 	public void test_resizesWestMemberHeightToFull() {
 		Canvas member = new Canvas();
 		borderLayout.setWestMember(member);
 		assertEquals("100%", member.getHeightAsString());
 	}
 
 	@Test
 	public void test_resizesWestHolderToAutoFit() {
 		borderLayout.setWestMember(new Canvas());
 		assertEquals(Integer.valueOf(1), getWestHolder().getAttributeAsInt("width"));
 	}
 
 	@Test
 	public void test_resizesWestHolderToNothingIfMemberIsNull() {
 		borderLayout.setWestMember(new Canvas());
 		borderLayout.setWestMember(null);
 		assertEquals(Integer.valueOf(0), getWestHolder().getAttributeAsInt("width"));
 	}
 
 	@Test
 	public void test_eastMemberReturnsNullIfNotSet() {
 		assertNull(borderLayout.getEastMember());
 	}
 
 	@Test
 	public void test_returnsEastMember() {
 		final Canvas member = new Canvas();
 		borderLayout.setEastMember(member);
 		assertEquals(member, borderLayout.getEastMember());
 	}
 
 	@Test
 	public void test_resizesEastMemberHeightToFull() {
 		Canvas member = new Canvas();
 		borderLayout.setEastMember(member);
 		assertEquals("100%", member.getHeightAsString());
 	}
 
 	@Test
 	public void test_resizesEastHolderToAutoFit() {
 		borderLayout.setEastMember(new Canvas());
 		assertEquals(Integer.valueOf(1), getEastHolder().getAttributeAsInt("width"));
 	}
 
 	@Test
 	public void test_resizesEastHolderToNothingIfMemberIsNull() {
 		borderLayout.setEastMember(new Canvas());
 		borderLayout.setEastMember(null);
 		assertEquals(Integer.valueOf(0), getEastHolder().getAttributeAsInt("width"));
 	}
 
 	private HLayout getNorthHolder() {
 		return (HLayout) borderLayout.getMember(0);
 	}
 
 	private HLayout getSouthHolder() {
 		return (HLayout) borderLayout.getMember(2);
 	}
 
 	private Canvas getWestHolder() {
 		return ((Layout) borderLayout.getMember(1)).getMember(0);
 	}
 
 	private Canvas getCenterHolder() {
 		return ((Layout) ((Layout) borderLayout.getMember(1)).getMember(1)).getMember(0);
 	}
 
 	private Canvas getEastHolder() {
 		return ((Layout) ((Layout) borderLayout.getMember(1)).getMember(1)).getMember(1);
 	}
 }
