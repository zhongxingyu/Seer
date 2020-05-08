 package toctep.skynet.backend.test.dal;
 
 import toctep.skynet.backend.dal.domain.boundingbox.BoundingBox;
 import toctep.skynet.backend.dal.domain.boundingbox.IBoundingBoxType;
 import toctep.skynet.backend.dal.domain.boundingbox.NullBoundingBoxType;
 
 public class BoundingBoxTest extends DomainTest{
 
 	private BoundingBox boundingBox;
 	
 	private IBoundingBoxType boundingBoxType;
 	private String coordinates;
 	
 	@Override
 	public void setUp() {
 		super.setUp();
 		
 		boundingBox = new BoundingBox();
 		
		boundingBoxType = NullBoundingBoxType.getInstance();
 		boundingBox.setType(boundingBoxType);
 		
 		coordinates = "58.17, 68.20";
 		boundingBox.setCoordinates(coordinates);
 	}
 	
 	@Override
 	public void testCreate() {
 		assertNotNull(boundingBox);
 		assertTrue(boundingBox.getType() == boundingBoxType);
 		assertTrue(coordinates.equals(boundingBox.getCoordinates()));
 	}
 
 	@Override
 	public void testInsert() {	
 		boundingBox.save();
 		assertEquals(1, boundingBoxDao.count());
 		assertTrue(new Long(1).equals(boundingBox.getId()));
 	}
 
 	@Override
 	public void testSelect() {
 		boundingBox.save();
 		
 		BoundingBox postBoundingBox = (BoundingBox) boundingBoxDao.select(boundingBox.getId());
 		
 		assertTrue(postBoundingBox.getType().equals(boundingBox.getType()));
 		assertTrue(postBoundingBox.getCoordinates().equals(boundingBox.getCoordinates()));
 	}
 	
 	@Override
 	public void testUpdate() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void testDelete() {
 		boundingBox.save();
 		assertEquals(1, boundingBoxDao.count());
 		boundingBox.delete();
 		assertEquals(0, boundingBoxDao.count());		
 	}
 }
