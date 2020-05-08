 package org.netvogue.server.webmvc.controllers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.netvogue.server.webmvc.domain.BrandsCarried;
 import org.netvogue.server.webmvc.domain.JsonResponse;
 import org.netvogue.server.webmvc.domain.PhotoWeb;
 import org.netvogue.server.webmvc.domain.ProductLine;
 import org.netvogue.server.webmvc.domain.ProfileInfo;
 import org.netvogue.server.webmvc.domain.ContactInfo;
 import org.netvogue.server.webmvc.domain.UploadedFileResponse;
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.Size;
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.neo4japi.common.NetworkStatus;
 import org.netvogue.server.neo4japi.common.ProductLines;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.domain.*;
 import org.netvogue.server.neo4japi.service.BoutiqueService;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 //This controllers handles both profile and profile settings page
 @Controller
 public class ProfileController {
 	
 	@Autowired NetvogueUserDetailsService userDetailsService;
 	@Autowired UserService 		userService;
 	@Autowired BoutiqueService  boutiqueService;
 	@Autowired UploadManager 	uploadManager;
 
 	@RequestMapping(value = {"/profile/", "/profile/{profileid}"}, method=RequestMethod.GET)
 	public @ResponseBody ProfileInfo getProfileInfo(@ModelAttribute("profileid") String profileid) {
 		System.out.println("ProfileInfo: id is:" + profileid);
 		User user = userDetailsService.getUserFromSession();
 		ProfileInfo profile = new ProfileInfo();
 		
 		if(!profileid.isEmpty()) {
 			User loggedinuser = user;
 			user = userService.getUserByUsername(profileid);
 			if(user == null) {
 				return profile;
 			}
 			NetworkStatus status = userService.getNetworkStatus(loggedinuser.getUsername(), profileid);
 			if(null == status || status == NetworkStatus.BREAKUP) {
 				profile.setNetworkstatus(NetworkStatus.NONE.toString());
 			} else if(status == NetworkStatus.PENDING) {
 				profile.setNetworkstatus(NetworkStatus.PENDING.toString());
 			} else {
 				profile.setNetworkstatus(NetworkStatus.CONFIRMED.toString());
 			}
 		}
 
 		profile.setProfileid(user.getUsername());
 		profile.setName(user.getName());
 		profile.setAboutus(user.getAboutUs());
 		//Set profile pic
 		if(null != user.getProfilePicLink()) {
			String thumburl = uploadManager.getQueryString(user.getProfilePicLink(), ImageType.PROFILE_PIC, Size.PThumb);
			System.out.println("Image path is/Thumbnail url is" + thumburl);
			profile.setProfilepic(thumburl);
 		}
 	
 		//Get ContactInfo
 		ContactInfo contactInfo = new ContactInfo();
 		contactInfo.setAddress(user.getAddress());
 		contactInfo.setCity(user.getCity());
 		contactInfo.setState(user.getState());
 		contactInfo.setCountry(user.getCountry());
 		contactInfo.setZip(String.valueOf(user.getZipCode()));
 		contactInfo.setEmail(user.getEmail());
 		contactInfo.setLandline1(String.valueOf(user.getTelephoneNo1()));
 		contactInfo.setLandline2(String.valueOf(user.getTelephoneNo2()));
 		contactInfo.setMobile(String.valueOf(user.getMobileNo()));
 		contactInfo.setWebsite(user.getWebsite());
 		contactInfo.setYearest(user.getYearofEst());
 		//Add it to profile Info
 		profile.setContactinfo(contactInfo);
 		
 		//Get Productlines Info			
 		Set<Category> productsCarried = user.getProductLinesCarried();
 		int size = productsCarried.size();
 		Set<ProductLine> productLine = new HashSet<ProductLine>();
 		for(Category product: productsCarried) {
 			System.out.println("Name:" +  product.getProductLine().getDesc() + "- size:" + size );
 			ProductLine productTemp = new ProductLine();
 			productTemp.setProductlinename(product.getProductLine().getDesc());
 			productTemp.setSelected(true);
 			productLine.add(productTemp);
 		}
 		profile.setProductlines(productLine);
 		
 		//Get Brands/Stockists carried info
 		Set<User> brandsCarried = user.getUsersCarried();
 		Set<BrandsCarried> brands = new HashSet<BrandsCarried>();
 		for(User product: brandsCarried) {
 			System.out.println("Name:" +  product.getName() + "username:" + product.getUsername());
 			BrandsCarried brand = new BrandsCarried();
 			brand.setBrandname(product.getName());
 			brand.setBrandusername(product.getUsername());
 			brands.add(brand);
 		}
 		profile.setBrandscarried(brands);
 		profile.setStatus(true);
 		
 		System.out.println("ProfileInfo Sent for id:" + profileid);
 		return profile;
 	}
 	
 	@RequestMapping(value = "/profile/aboutus", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setAboutus(@RequestBody String aboutUs) {
 		JsonResponse status = new JsonResponse();
 		User user = userDetailsService.getUserFromSession();
 		try {
 			user.setAboutUs(aboutUs);
 			
 			String error = new String();
 			if(ResultStatus.SUCCESS == userService.SaveUser(user, error))
 				status.setStatus(true);
 			else
 				status.setError(error);
 		} catch(Exception e) {
 			status.setError(e.toString());
 		}
 		return status;
 	}
 	
 	@RequestMapping(value = "/profile/profilepic", method=RequestMethod.POST)
 	public @ResponseBody UploadedFileResponse setProfilePic(Model model, 
 			@RequestParam("files[]") MultipartFile fileupload) {
 		System.out.println("Set Profile Pic: ");
 		UploadedFileResponse response = new UploadedFileResponse();
 			
 		User user = userDetailsService.getUserFromSession();
 		if(null == user) {
 			response.setError("User is not logged in");
 			return response;
 		}
 		
 		List<PhotoWeb> JSONFileData= new ArrayList<PhotoWeb>();
 		
 		System.out.println("Came here" + fileupload.getOriginalFilename());
 		Map<String, Object> uploadMap  = uploadManager.processUpload(fileupload, ImageType.PROFILE_PIC);
 		user.setProfilePicLink((String)uploadMap.get(UploadManager.FILE_ID));
 		String error = new String();
 		if(ResultStatus.SUCCESS == userService.SaveUser(user, error)) {
 			response.setStatus(true);
 		
 			PhotoWeb newPhoto = new PhotoWeb();
 			String thumburl = uploadManager.getQueryString((String)uploadMap.get(UploadManager.FILE_ID), ImageType.PROFILE_PIC, Size.PThumb);
 			System.out.println("Image path is/Thumnail url is" + thumburl);
 			newPhoto.setThumbnail_url(thumburl);
 			String lefturl = uploadManager.getQueryString((String)uploadMap.get(UploadManager.FILE_ID), ImageType.PROFILE_PIC, Size.PTop);
 			newPhoto.setLeft_url(lefturl);
 			newPhoto.setUniqueid((String)uploadMap.get(UploadManager.FILE_ID));
 			JSONFileData.add(newPhoto);
 			
 			response.setFilesuploaded(JSONFileData);
 		} else {
 			response.setError(error);
 		}
 	
 		return response;
 	}
 	
 	@RequestMapping(value = "/profile/contactinfo", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setContactInfo(@RequestBody ContactInfo contactInfo) {
 		JsonResponse status = new JsonResponse();
 		User user = userDetailsService.getUserFromSession();
 		try {
 		
 		//if(ResultStatus.SUCCESS == userService.ValidateEmailAndId(contactInfo.getEmail(), user.getNodeId())) { 
 			user.setAddress(contactInfo.getAddress());
 			user.setCity(contactInfo.getCity());
 			user.setState(contactInfo.getState());
 			user.setCountry(contactInfo.getCountry());
 			user.setZipCode(Integer.parseInt(contactInfo.getZip()));
 			user.setEmail(contactInfo.getEmail());
 			user.setTelephoneNo1(Integer.parseInt(contactInfo.getLandline1()));
 			user.setTelephoneNo1(Integer.parseInt(contactInfo.getLandline1()));
 			user.setMobileNo(Long.parseLong(contactInfo.getMobile()));
 			user.setWebsite(contactInfo.getWebsite());
 			user.setYearofEst(contactInfo.getYearest());
 			
 			String error = new String();
 			if(ResultStatus.SUCCESS == userService.SaveUser(user, error))
 				status.setStatus(true);
 			else
 				status.setError(error);
 		/*} else {
 			status.setError("Email is already existing.Try another one");
 		}*/
 		} catch(Exception e) {
 			status.setError(e.toString());
 		}
 		return status;
 	}
 	
 	@RequestMapping(value = "/profile/productline", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setProductline(@RequestBody ArrayList<String> productLines) {
 		JsonResponse status = new JsonResponse();
 		User user = userDetailsService.getUserFromSession();
 		try {
 			//First remove all the existing categories and replace with the new one
 			user.deleteCategories();
 			//for(Map<String, String> productline: productLines) {
 			//ProductLines productLine = ProductLines.getValueOf(productline.get("productlinename"));
 			for(String productline: productLines) {
 				ProductLines productLine = ProductLines.getValueOf(productline);
 				Category cat = boutiqueService.getOrCreateCategory(productLine);
 				user.updateCategories(cat);
 			}
 			String error = new String();
 			if(ResultStatus.SUCCESS == userService.SaveUser(user, error))
 				status.setStatus(true);
 			else
 				status.setError(error);
 		} catch(Exception e) {
 			System.out.println("Exception is:" + e.getMessage());
 			status.setError(e.toString());
 		}
 		return status;
 	}
 	
 	@RequestMapping(value = "/profile/brandscarried", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setBrandsCarried(@RequestBody ArrayList<String> brandsCarried) {
 		JsonResponse status = new JsonResponse();
 		User user = userDetailsService.getUserFromSession();
 		try {
 			//First remove all the existing categories and replace with the new one
 			user.deleteUsersCarried();
 			for(String productline: brandsCarried) {
 				User newUser = boutiqueService.GetOrCreateUser(productline);
 				user.updateUsersCarried(newUser);
 			}
 			String error = new String();
 			if(ResultStatus.SUCCESS == userService.SaveUser(user, error))
 				status.setStatus(true);
 			else
 				status.setError(error);
 		} catch(Exception e) {
 			System.out.println("Exception is:" + e.getMessage());
 			status.setError(e.toString());
 		}
 		return status;
 	}
 }
