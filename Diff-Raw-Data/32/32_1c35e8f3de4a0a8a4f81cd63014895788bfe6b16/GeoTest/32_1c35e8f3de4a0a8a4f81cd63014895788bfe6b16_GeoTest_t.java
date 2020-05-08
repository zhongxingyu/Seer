 package toctep.skynet.backend.test.dal;
 
 import toctep.skynet.backend.dal.domain.geo.Geo;
 import toctep.skynet.backend.dal.domain.geo.IGeoType;
 import toctep.skynet.backend.dal.domain.geo.NullGeoType;
 
 public class GeoTest extends DomainTest{
 
 	private Geo geo;
 	
 	private IGeoType geoType;
 	private String coordinates;
 	
 	@Override
 	public void setUp() {
 		super.setUp();
 		
 		geo = new Geo();
 		
		geoType = NullGeoType.getInstance();
 		geo.setType(geoType);
 		
 		coordinates = "58.17, 68.20";
 		geo.setCoordinates(coordinates);
 	}
 	
 	@Override
 	public void testCreate() {
 		assertNotNull(geo);
 		assertTrue(geo.getType() == geoType);
 		assertTrue(coordinates.equals(geo.getCoordinates()));
 	}
 
 	@Override
 	public void testInsert() {
 		geo.save();
 		assertEquals(1, geoDao.count());
 		assertTrue(new Long(1).equals(geo.getId()));
 	}
 	
 	@Override
 	public void testSelect() {
 		geo.save();
 		
 		Geo postGeo = (Geo) geoDao.select(geo.getId());
 		
 		assertTrue(postGeo.getType().equals(geo.getType()));
 		assertTrue(postGeo.getCoordinates().equals(geo.getCoordinates()));
 	}
 
 	@Override
 	public void testUpdate() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void testDelete() {
 		geo.save();
 		assertEquals(1, geoDao.count());
 		geo.delete();
 		assertEquals(0, geoDao.count());		
 	}
 	
 }
