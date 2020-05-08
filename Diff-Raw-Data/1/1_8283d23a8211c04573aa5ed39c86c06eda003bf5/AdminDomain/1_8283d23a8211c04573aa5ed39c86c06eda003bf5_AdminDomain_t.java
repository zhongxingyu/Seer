 package com.leopin.parkfifty.admin.domain;
 
 import java.util.Random;
 
 import com.leopin.parkfifty.shared.domain.Company;
 import com.leopin.parkfifty.shared.domain.CompanyUser;
 import com.leopin.parkfifty.shared.domain.Entitlements;
 import com.leopin.parkfifty.shared.domain.Location;
 import com.leopin.parkfifty.shared.domain.ParkFacilityTypes;
 import com.leopin.parkfifty.shared.domain.Roles;
 
 public class AdminDomain {
 
 	/**
 	 * Get Company template
 	 * @return Company
 	 */
 	
 	public static Company getCompany() {
 		Random rand = new Random(System.currentTimeMillis());
 		
 		Company company = new Company();
 		int randval = rand.nextInt(99999);
 		company.setName("This is a Good Company " + randval);
		company.setCode("AGOODCOMPANY");
 		company.setEmail("gpinto@bbandt.com");
 		company.setUrl("http://www.ashriv.com");
 		company.setPriPhone("(919) 455-3262");
 		company.setSecPhone("");
 		company.setFax("(919) 447-0110");
 		return company;
 	}
 	/**
 	 * Get Company User template data
 	 * @return CompanyUser
 	 */
 	public static CompanyUser getCompanyUser(Long companyId) {
 			Random rand = new Random(System.currentTimeMillis());
 			int userIdSuffix = rand.nextInt(999);
 			CompanyUser companyUser = new CompanyUser();
 			companyUser.setUserId("gvpinto" + userIdSuffix);
 			companyUser.setPassword("M1ng1L4r2");
 			companyUser.setEntitlements(Entitlements.ADD_USER);
 			companyUser.setRole(Roles.OWNER);
 			companyUser.setTitle("Mr.");
 			companyUser.setFirstName("Glenn");
 			companyUser.setMiddleInitial("J");
 			companyUser.setLastName("Pinto");
 			companyUser.setSuffix("III");
 			companyUser.setPriPhone("(919)455-3262");
 			companyUser.setSecPhone("919 455-3263");
 			companyUser.setFax("");
 			companyUser.setEmail("gvpinto@gmail.com");
 			companyUser.setActive(true);
 			companyUser.setApproved(true);
 			companyUser.setCompanyId(companyId);
 			return companyUser;
 	}
 
 	/**
 	 * get Location template data
 	 * @return Location
 	 */
 	public static Location getLocation() {
 		Location location = new Location();
 		location.setName("Glenn's parking lot. this-is a meaning, and_w");
 		location.setDescription("This is a beautiful parking lot with ample spaces and a secured place with parking");
 		location.setStreet("12808 Baybriar Dr, Ste 200");
 		location.setStreet2("");
 		location.setCity("Raleigh");
 		location.setStateCd("NC");
 		location.setZipCd("27560-5500");
 		location.setCountryCd("USA");
 		location.setGcLat(35.910126f);
 		location.setGcLng(78.717635f);
 		location.setParkFacilityType(ParkFacilityTypes.COVERED);
 		location.setPriPhone("919-455-3262");
 		location.setSecPhone("(919) 455-3262");
 		location.setFax("919 447 0110");
 		location.setEmail("gvpinto@gmail.co.in");
 		location.setTotalCapacity(100);
 		location.setDefaultRate(556);
 		location.setManned(true);
 		location.setMannedDesc("This is a Manned place with 24hrs of security");
 		return location;
 	}
 
 }
