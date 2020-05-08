 package delegates;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.imageio.ImageIO;
 
 import org.joda.time.DateTime;
 
 import akka.event.slf4j.Logger;
 
 import play.Play;
 import play.mvc.Http.MultipartFormData.FilePart;
 import pojos.CityBean;
 import pojos.FileBean;
 import utils.FileUtilities;
 import utils.PlayDozerMapper;
 
 public class UtilitiesDelegate {
 
 	public String uploadDir = Play.application().configuration()
 			.getString("files.home");
 	public String filesBaseURL = Play.application().configuration()
 			.getString("files.baseurl");
 
 	public static UtilitiesDelegate getInstance() {
 		return new UtilitiesDelegate();
 	}
 
 	public List<CityBean> getCities() {
 		List<models.City> modelCities = models.City.all();
 		List<CityBean> pojosCities = new ArrayList<CityBean>();
 		for (models.City city : modelCities) {
 			CityBean cityBean = PlayDozerMapper.getInstance().map(city,
 					CityBean.class);
 			pojosCities.add(cityBean);
 		}
 		return pojosCities;
 	}
 
 	public List<CityBean> getCitiesByCountryId(Long countryId) {
 		List<models.City> modelCities = models.City.readByCountry(countryId);
 		List<CityBean> pojosCities = new ArrayList<CityBean>();
 		for (models.City city : modelCities) {
 			CityBean cityBean = PlayDozerMapper.getInstance().map(city,
 					CityBean.class);
 			pojosCities.add(cityBean);
 		}
 		return pojosCities;
 	}
 
 	public List<CityBean> getCitiesByCountryName(String countryName) {
 		List<models.City> modelCities = models.City
 				.readByCountryName(countryName);
 		List<CityBean> pojosCities = new ArrayList<CityBean>();
 		for (models.City city : modelCities) {
 			CityBean cityBean = PlayDozerMapper.getInstance().map(city,
 					CityBean.class);
 			pojosCities.add(cityBean);
 		}
 		return pojosCities;
 	}
 
 	public List<CityBean> getCitiesByCountryNameAndRegion(String country,
 			String region) {
 		List<models.City> modelCities = models.City.readByCountryNameAndRegion(
 				country, region);
 		List<CityBean> pojosCities = new ArrayList<CityBean>();
 		for (models.City city : modelCities) {
 			CityBean cityBean = PlayDozerMapper.getInstance().map(city,
 					CityBean.class);
 			pojosCities.add(cityBean);
 		}
 		return pojosCities;
 	}
 
 	public List<CityBean> getNewCities(Long lastCityId) {
 		List<models.City> modelCities = models.City.readNewById(lastCityId);
 		List<CityBean> pojosCities = new ArrayList<CityBean>();
 		for (models.City city : modelCities) {
 			CityBean cityBean = PlayDozerMapper.getInstance().map(city,
 					CityBean.class);
 			pojosCities.add(cityBean);
 		}
 		return pojosCities;
 	}
 
 	public List<CityBean> getCityByName(String cityName) {
 		List<models.City> modelCities = models.City.findByName(cityName);
 		List<CityBean> pojosCities = new ArrayList<CityBean>();
 		for (models.City city : modelCities) {
 			CityBean cityBean = PlayDozerMapper.getInstance().map(city,
 					CityBean.class);
 			pojosCities.add(cityBean);
 		}
 		return pojosCities;
 	}
 
 	public CityBean getCitiesById(Long cityId) {
 		models.City city = models.City.read(cityId);
 		CityBean cityBean = PlayDozerMapper.getInstance().map(city,
 				CityBean.class);
 		return cityBean;
 	}
 
 	public FileBean saveFile(FilePart file, FileBean fileBean)
 			throws IOException {
 		/*
 		 * 1. Prepare file metadata before saving the file in the final
 		 * destination
 		 */
 
 		String fileName = "U_" + fileBean.getOwner().toString() + "_" + "D"
 				+ DateTime.now() + "_" + file.getFilename();
 		// TODO check how to make uploadDir also cross-platform
 		String fullPath = uploadDir + FileUtilities.slash + fileName;
 		String contentType = file.getContentType();
 		File uploadFile = file.getFile();
 
 		String[] parts = contentType.split("/");
 		String fileExtension = "";
 
 		if (parts != null && parts.length > 0) {
 			fileExtension = parts[parts.length - 1];
 		}
 
 		Logger.root().debug("Saving File....");
 		Logger.root().debug("--> fileName=" + fileName);
 		Logger.root().debug("--> contentType=" + contentType);
 		Logger.root().debug("--> uploadFile=" + uploadFile);
 
 		/*
 		 * 2. Save the file in the final destination
 		 */
 		File localFile = new File(fullPath);
 		uploadFile.renameTo(localFile);
 		Logger.root().debug("--> localFile=" + localFile);
		if (play.mvc.Controller.request().host().contains("localhost")) {
 			filesBaseURL = "http://localhost/files";
 		}
 		
 		/*
 		 * 3. Save scaled down versions
 		 */
 		File thumbnailFile = null;
 		File smallFile = null;
 		File mediumFile = null;
 		File largeFile = null;
 
 		if (contentType.contains("image")) {
 			thumbnailFile = scalePictureAndSave(localFile, fileExtension,
 					"THUMBNAIL");
 			smallFile = scalePictureAndSave(localFile, fileExtension, "SMALL");
 			mediumFile = scalePictureAndSave(localFile, fileExtension, "MEDIUM");
 			largeFile = scalePictureAndSave(localFile, fileExtension, "LARGE");
 		}
 
 		String thumbnailURI = "";
 		String smallURI = "";
 		String mediumURI = "";
 		String largeURI = "";
 		if (thumbnailFile != null) {
 			thumbnailURI = filesBaseURL + "/" + thumbnailFile.getName();
 		}
 
 		if (smallFile != null) {
 			smallURI = filesBaseURL + "/" + smallFile.getName();
 		}
 
 		if (mediumFile != null) {
 			mediumURI = filesBaseURL + "/" + mediumFile.getName();
 		}
 
 		if (largeFile != null) {
 			largeURI = filesBaseURL + "/" + largeFile.getName();
 		}
 
 		/*
 		 * 4. Generate Hashcode to use as new name
 		 */
 		String hashcode = UUID.nameUUIDFromBytes(fullPath.getBytes())
 				.toString();
 		/*
 		 * 5. Save File metadata in Database
 		 */
 		fileBean.setFilename(fileName);
 		fileBean.setURI(filesBaseURL + "/" + fileName);
 		fileBean.setThumbnailURI(thumbnailURI);
 		fileBean.setMediumURI(mediumURI);
 		fileBean.setSmallURI(smallURI);
 		fileBean.setLargeURI(largeURI);
 		fileBean.setContentType(contentType);
 		fileBean.setCreationDate(DateTime.now());
 		fileBean.setHashcode(hashcode);
 		fileBean.setExtension(fileExtension);
 		Logger.root().debug("--> creationDate=" + fileBean.getCreationDate());
 		Logger.root().debug("--> hashcode=" + fileBean.getHashcode());
 
 		models.File f = PlayDozerMapper.getInstance().map(fileBean,
 				models.File.class);
 		f.save();
 		fileBean.setFileId(f.getFileId());
 		return fileBean;
 	}
 
 	private File scalePictureAndSave(File sourceImageFile,
 			String fileExtension, String size) throws IOException {
 
 		// 1. Read image
 		BufferedImage img = ImageIO.read(sourceImageFile);
 
 		// 2. Create scaled image
 		BufferedImage scaled = null;
 		if (size.equals("THUMBNAIL")) {
 			// thumbnail=150px
 			scaled = FileUtilities.createThumbnail(img);
 		} else if (size.equals("SMALL")) {
 			// small=250px
 			scaled = FileUtilities.createSmall(img);
 		} else if (size.equals("MEDIUM")) {
 			// medium=500px
 			scaled = FileUtilities.createMedium(img);
 		} else {
 			// large=1024px
 			scaled = FileUtilities.createLarge(img);
 		}
 
 		scaled.flush();
 
 		// 3. prepare the path and filename for the scaled image
 		String fileName = size + "_" + sourceImageFile.getName();
 		String fullPath = uploadDir + FileUtilities.slash + fileName;
 		File thumbFile = new File(fullPath);
 
 		// 4. save scaled image
 		ImageIO.write(scaled, fileExtension, thumbFile);
 		return thumbFile;
 	}
 
 	public java.io.File getFile(String hashcode, Long userId, String type) {
 
 		models.File f = models.File.readByHashCodeAndUserId(hashcode, userId);
 
 		if (f != null) {
 
 			if (!f.getContentType().contains("image"))
 				type = "";
 
 			String uploadDir = Play.application().configuration()
 					.getString("files.home");
 			String fileName = f == null ? "" : f.getFilename();
 			String typePrefix = type != null && !type.isEmpty() ? type
 					.toUpperCase() + "_" : "";
 			String fullPath = f == null ? "" : uploadDir + FileUtilities.slash
 					+ typePrefix + fileName;
 
 			return new java.io.File(fullPath);
 		} else {
 			return null;
 		}
 	}
 
 	public java.io.File getFile(String hashcode, Long userId) {
 		return getFile(hashcode, userId, "");
 	}
 	
 	public java.io.File getFileNoLogin(String hashcode, String type) {
 
 		models.File f = models.File.readByHashCode(hashcode);
 
 		if (f != null) {
 
 			if (!f.getContentType().contains("image"))
 				type = "";
 
 			String uploadDir = Play.application().configuration()
 					.getString("files.home");
 			String fileName = f == null ? "" : f.getFilename();
 			String typePrefix = type != null && !type.isEmpty() ? type
 					.toUpperCase() + "_" : "";
 			String fullPath = f == null ? "" : uploadDir + FileUtilities.slash
 					+ typePrefix + fileName;
 
 			return new java.io.File(fullPath);
 		} else {
 			return null;
 		}
 	}
 
 	public java.io.File getFileNoLogin(String hashcode) {
 		return getFileNoLogin(hashcode, "");
 	}
 
 }
