 package toctep.skynet.backend.test.dal;
 
 import toctep.skynet.backend.dal.domain.BoundingBox;
 import toctep.skynet.backend.dal.domain.Country;
 import toctep.skynet.backend.dal.domain.Place;
 import toctep.skynet.backend.dal.domain.PlaceType;
 
 public class PlaceTest extends DomainTest{
 
 	private Place place;
 	
 	private String id;
 	private PlaceType placeType;
 	private BoundingBox boundingBox;
 	private String name;
 	private String fullName;
 	private Country country;
 	private String streetAddress;
 	private String locality;
 	private String region;
 	private String iso3;
 	private String postalCode;
 	private String phone;
 	private String twitter;
	private String url;
 	private String appId;
 	
 	@Override
 	public void setUp() {
 		super.setUp();
 		
 		place = new Place();
 		
 		id = "0x20";
 		place.setId(id);
 		
 		placeType = new PlaceType();
 		place.setType(placeType);
 		
 		boundingBox = new BoundingBox();
 		place.setBoundingBox(boundingBox);
 		
 		name = "TestName";
 		place.setName(name);
 		
 		fullName = "TestFullName";
 		place.setFullName(fullName);
 		
 		country = new Country();
 		place.setCountry(country);
 		
 		streetAddress = "Teststreet";
 		place.setStreetAddress(streetAddress);
 		
 		locality = "TestLocality";
 		place.setLocality(locality);
 		
 		region = "TestRegion";
 		place.setRegion(region);
 		
 		iso3 = "TestIso3";
 		place.setIso3(iso3);
 		
 		postalCode = "TestPostalCode";
 		place.setPostalCode(postalCode);
 		
 		phone = "TestPhone";
 		place.setPhone(phone);
 		
 		twitter = "TestTwitter";
 		place.setTwitter(twitter);
 		
		url = "TestUrl";
 		place.setUrl(url);
 		
 		appId = "TestAppId";
 		place.setAppId(appId);
 	}
 	
 	@Override
 	public void testCreate() { 
 		assertNotNull(place);
 		assertTrue(id.equals(place.getId()));
 		assertTrue(place.getType() == placeType);
 		assertTrue(place.getBoundingBox() == boundingBox);
 		assertTrue(name.equals(place.getName()));
 		assertTrue(fullName.equals(place.getFullName()));
 		assertTrue(place.getCountry() == country);
 		assertTrue(streetAddress.equals(place.getStreetAddress()));
 		assertTrue(locality.equals(place.getLocality()));
 		assertTrue(region.equals(place.getRegion()));
 		assertTrue(iso3.equals(place.getIso3()));
 		assertTrue(postalCode.equals(place.getPostalCode()));
 		assertTrue(phone.equals(place.getPhone()));
 		assertTrue(twitter.equals(place.getTwitter()));
 		assertTrue(url.equals(place.getUrl()));
 		assertTrue(appId.equals(place.getAppId()));
 	}
 
 	@Override
 	public void testInsert() {
 		place.save();
 		assertEquals(1, placeDao.count());
 		assertEquals(1, place.getId());
 		
 		placeDao.insert(place);
 		assertEquals(1, userDao.count());
 	}
 	
 	@Override
 	public void testSelect() {
 		placeDao.insert(place);
 		
 		Place postPlace = (Place) placeDao.select(place.getId());
 		
 		assertTrue(postPlace.getType().equals(place.getType()));
 		assertTrue(postPlace.getBoundingBox().equals(place.getBoundingBox()));
 		assertTrue(postPlace.getName().equals(place.getName()));
 		assertTrue(postPlace.getFullName().equals(place.getFullName()));
 		assertTrue(postPlace.getCountry().equals(place.getCountry()));
 		assertTrue(postPlace.getStreetAddress().equals(place.getStreetAddress()));
 		assertTrue(postPlace.getLocality().equals(place.getStreetAddress()));
 		assertTrue(postPlace.getRegion().equals(place.getRegion()));
 		assertTrue(postPlace.getIso3().equals(place.getIso3()));
 		assertTrue(postPlace.getPostalCode().equals(place.getIso3()));
 		assertTrue(postPlace.getPhone().equals(place.getPhone()));
 		assertTrue(postPlace.getTwitter().equals(place.getTwitter()));
 		assertTrue(postPlace.getUrl().equals(place.getUrl()));
 		assertTrue(postPlace.getAppId().equals(place.getAppId()));
 	}
 
 	@Override
 	public void testUpdate() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void testDelete() {
 		placeDao.insert(place);
 		assertEquals(1, placeDao.count());
 		placeDao.delete(place);
 		assertEquals(0, placeDao.count());
 	}
 	
 }
