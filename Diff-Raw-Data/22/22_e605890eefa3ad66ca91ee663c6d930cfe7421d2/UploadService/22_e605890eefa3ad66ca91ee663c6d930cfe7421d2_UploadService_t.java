 package com.megalogika.sv.service;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.binding.message.MessageBuilder;
 import org.springframework.binding.message.MessageContext;
 import org.springframework.stereotype.Service;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.webflow.execution.RequestContext;
 
 import com.megalogika.sv.model.Friend;
 import com.megalogika.sv.model.Photo;
 import com.megalogika.sv.model.Product;
 import com.megalogika.sv.model.Transformation;
 
 @Service("uploadService")
 public class UploadService {
 	
 	protected transient Logger logger = Logger.getLogger(UploadService.class);
 	
 	@Autowired
 	FileService fileService;
 	@Autowired
 	private 
 	CropService cropService;
 
 	public boolean upload(Friend friend) {
 		MultipartFile file = friend.getFile();
 		if (null == file || file.isEmpty() || file.getSize() == 0) {
 			friend.setThumb(null);
 		} else {
 			if (!fileService.isImage(file)) {
 				return false;
 			}
 			try {
 				String fileName = fileService.putFileToPlace(file);
 				friend.setThumb(fileName);
 			} catch (IOException e) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 	
 	private boolean isFileSpecified(MultipartFile photoFile, Photo photo) {
 		return (!photoFile.isEmpty() || StringUtils.hasText(photo.getOriginalPhoto()));
 	}
 	
 	private boolean checkFileSpecified(MessageContext messageContext, Photo photo, MultipartFile photoFile, String msgKey, String msgSource) {
 		if (! isFileSpecified(photoFile, photo)) {
 			messageContext.addMessage(
 					new MessageBuilder().error().source(msgSource)
 							.defaultText("You must specify a photo!")
 							.code(msgKey).build());
 			return false;
 		}
 		return true;
 	}
 
 	public Photo getPhoto(MultipartHttpServletRequest mpRequest, String fieldName) throws IOException {
 		
 		logger.debug("RETRIEVING PHOTO FOR: " + fieldName);
 		
 		MultipartFile photoFile = getFile(mpRequest, fieldName);
 		
 		Photo ret = new Photo();
 		
 		if (isFileSpecified(photoFile, ret)) {
 			if (null != photoFile && !photoFile.isEmpty() && photoFile.getSize() > 0) {
 				if (isImageFileValid(photoFile)) {
 					if ( isImageValid(photoFile, 200)) {
 						try {
 							String fileName = fileService.putFileToPlace(photoFile);
 							
 							logger.debug("RESULTING FILENAME: " + fileName);
 							
 							ret.setOriginalPhoto(fileName);
 							ret.setPhoto(fileName);
 						} catch (IOException e) {
 							logger.error(e);
 						}
 					} else {
 						logger.error("PHOTO ERROR: image too small");
 					}
 				} else {
 					logger.error("PHOTO ERROR: invalid image file");
 				}
 			} else {
 				logger.error("PHOTO ERROR: empty file or smth.");
 			}
 		} else {
 			logger.error("PHOTO ERROR: NO UPLOADED FILE?");
 		}
 		
 		logger.debug("RETURNING: " + ret);
 		
 		return ret;
 	}
 	
 	public boolean uploadProductPhoto(MessageContext messageContext, RequestContext requestContext, Product product) {
 		Assert.notNull(messageContext, "Missing message context (==null)!");
 		Assert.notNull(requestContext, "Missing request context (==null)!");
 		Assert.notNull(product, "Missing product (==null)!");
 		
 		MultipartFile photoFile = null;
 		
 		try {
 			photoFile = getFile(requestContext, "labelPhoto");
 		} catch (Exception e) {
 			logger.error("While getting labelPhoto, ", e);
 			photoFile = null;
 		}
 
 		if (null == product.getLabel()) {
 			product.setLabel(new Photo());
 		}
 		
 		if(!checkFileSpecified(messageContext, product.getLabel(), photoFile, "uploadService.productPhotoNotSpecified", "labelPhoto")) {
 			return false;
 		}
 
 		if (null != photoFile && !photoFile.isEmpty() && photoFile.getSize() > 0) {
 			if (!validateImageFile(messageContext, photoFile, "labelPhoto")) {
 				return false;
 			}
 			try {
 				if (!validateImageSize(messageContext, photoFile, 217, "labelPhoto")) {
 					return false;
 				}
 				String fileName = fileService.putFileToPlace(photoFile);
 				product.getLabel().setOriginalPhoto(fileName);
 				product.getLabel().setResizedPhoto(fileName);
 				product.getLabel().setPhoto(fileName);
 			} catch (IOException e) {
 				addIOExceptionError(messageContext, "labelPhoto");
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public boolean uploadConservantsPhoto(MessageContext messageContext, RequestContext requestContext, Product product) {
 		Assert.notNull(messageContext, "Missing message context (==null)!");
 		Assert.notNull(requestContext, "Missing request context (==null)!");
 		Assert.notNull(product, "Missing product (==null)!");
 		
 		MultipartFile photoFile = getFile(requestContext, "ingredientsPhoto");
 		
 		if (null == product.getIngredients()) {
 			product.setIngredients(new Photo());
 		}
 		
 		if(!checkFileSpecified(messageContext, product.getIngredients(), photoFile, "uploadService.productIngredientsPhotoNotSpecified", "ingredientsPhoto")) {
 			return false;
 		}
 		if (null != photoFile && !photoFile.isEmpty() && photoFile.getSize() > 0) {
 			if (!validateImageFile(messageContext, photoFile, "ingredientsPhoto")) {
 				return false;
 			}
 			try {
 				if (!validateImageSize(messageContext, photoFile, 456, "ingredientsPhoto")) {
 					return false;
 				}
 				String fileName = fileService.putFileToPlace(photoFile);
 				product.getIngredients().setOriginalPhoto(fileName);
 				product.getIngredients().setResizedPhoto(fileName);
 				product.getIngredients().setPhoto(fileName);
 			} catch (IOException e) {
 				addIOExceptionError(messageContext, "ingredientsPhoto");
 				return false;
 			}
 		}
 		
 		logger.debug("INGREDIENTS PHOTO: " + product.getIngredients());
 		
 		return true;
 	}
 
 	public MultipartFile getFile(RequestContext requestContext, String fieldName) {
 		Assert.notNull(fieldName, "Missing field name (==null)!");
 		Assert.notNull(requestContext, "Missing request context (==null)!");
 		
 		return requestContext.getRequestParameters().getRequiredMultipartFile(fieldName);
 	}
 	
 	public MultipartFile getFile(MultipartHttpServletRequest mpRequest, String fieldName) {
 		Assert.notNull(mpRequest, "Missing multipart request (==null)!");
 		Assert.notNull(fieldName, "Missing fieldName (==null)!");
 		
 		return mpRequest.getFile(fieldName);
 	}
 
 
 	protected boolean isImageFileValid(MultipartFile file) {
 		return fileService.isImage(file);
 	}
 
 	protected boolean isImageValid(MultipartFile file, int size) throws IOException {
 		Assert.notNull(file, "Missing file object (==null)!");
 		Assert.notNull(file, "Missing size (==null)!");
 		BufferedImage image = ImageIO.read(file.getInputStream());
		return image.getWidth() > size && image.getHeight() > size;
 	}
 	
 	protected boolean validateImageFile(MessageContext messageContext, MultipartFile file, String sourceField) {
 		Assert.notNull(messageContext, "Missing message context (==null)!");
 		Assert.notNull(file, "Missing file object (==null)!");
 		Assert.notNull(sourceField, "Missing source field (==null)!");
 		if (! isImageFileValid(file)) {
 			messageContext.addMessage(new MessageBuilder().error().source(sourceField).defaultText("Nurodytas netinkamo formato failas. Nurodykite jpg failą.").code("uploadService.wrongFileType").build());
 			return false;
 		}
 		return true;
 	}
 	
 	protected boolean validateImageSize(MessageContext messageContext, MultipartFile file, int size, String sourceField) throws IOException {
 		BufferedImage image = ImageIO.read(file.getInputStream());	
 		if (! isImageValid(file, size)) {
 			messageContext.addMessage(new MessageBuilder().error().source(sourceField).defaultText("The file you specified is too small.").code("uploadService.fileTooSmall").build());
 			return false;
 		}
 		return true;
 	}
 
 	private void addIOExceptionError(MessageContext messageContext, String sourceField) {
 		messageContext.addMessage(new MessageBuilder().error().source(sourceField).defaultText(
 				"Nepavyko nukopijuoti failo į jo paskirties katalogą. Susisiekite su administratoriumi.").code("uploadService.ioexception").build());
 	}
 
 	public FileService getFileService() {
 		return fileService;
 	}
 
 	public void setFileService(FileService fileService) {
 		this.fileService = fileService;
 	}
 
 	public void setCropService(CropService cropService) {
 		this.cropService = cropService;
 	}
 
 	public CropService getCropService() {
 		return cropService;
 	}
 }
