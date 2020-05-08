 /* ===============================================================================
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.cms.applications.contenttool.actions;
 
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.applications.databeans.AssetKeyDefinition;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
 import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.cms.entities.management.GroupProperties;
 import org.infoglue.cms.entities.management.GroupPropertiesVO;
 import org.infoglue.cms.entities.management.RoleProperties;
 import org.infoglue.cms.entities.management.RolePropertiesVO;
 import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
 import org.infoglue.cms.entities.management.UserProperties;
 import org.infoglue.cms.entities.management.UserPropertiesVO;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.util.BrowserBean;
 
 import webwork.action.ActionContext;
 import webwork.multipart.MultiPartRequestWrapper;
 import com.mullassery.imaging.Imaging;
 import com.mullassery.imaging.ImagingFactory;
 
 /**
  * @author Mattias Bogeblad
  * @version 1.0
  * @since InfoglueCMS 2.4
  * 
  */
 
 public class ImageEditorAction extends InfoGlueAbstractAction
 {
     private final static Logger logger = Logger.getLogger(ImageEditorAction.class.getName());
 
 	private static final long serialVersionUID = 1L;
 
 	private DigitalAssetVO digitalAssetVO = null;
 	private String modifiedFileUrl = "";
 	private int xpos1, ypos1, xpos2, ypos2 = 0;
 	private int width, height;
 	private String keepRatio = "false";
 	private String bestFit = "true";
 	
 	private Integer contentVersionId = null;
 	private Integer digitalAssetId   = null;
 	private String closeOnLoad;
 	private ContentVersionVO contentVersionVO;
 	private ContentTypeDefinitionVO contentTypeDefinitionVO;
 	private String workingFileName;
 	
 	private String digitalAssetKey   = null;
 	private boolean isUpdated       = false;
 	private String reasonKey;
 	//private DigitalAssetVO updatedDigitalAssetVO = null;
 	private Integer contentTypeDefinitionId;
 	
 	private ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 	private Imaging imaging = ImagingFactory.createImagingInstance(ImagingFactory.AWT_LOADER, ImagingFactory.JAVA2D_TRANSFORMER);
         	
 	/**
 	 * 
 	 */
 	
     public String doExecute() throws Exception
     {
     	ceb.throwIfNotEmpty();
 	
     	this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
     	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
         this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
 
         if(this.workingFileName == null)
         {
 	        String filePath = DigitalAssetController.getDigitalAssetFilePath(this.digitalAssetVO.getDigitalAssetId());
 	        //System.out.println("filePath:" + filePath);
 	    	BufferedImage original = javax.imageio.ImageIO.read(new File(filePath));
 	
	    	workingFileName = "imageEditorWK_" + System.currentTimeMillis() + "_" + this.getInfoGluePrincipal().getName().hashCode() + "_" + digitalAssetVO.getDigitalAssetId() + ".png";
 	    	File outputFile = new File(getImageEditorPath() + File.separator + workingFileName);
 			javax.imageio.ImageIO.write(original, "PNG", outputFile);
 		}
         
     	this.modifiedFileUrl = getImageEditorBaseUrl() + workingFileName;
     	//System.out.println("modifiedFileUrl:" + modifiedFileUrl);
     
         return "success";
     }    
 
     public String doResize() throws Exception
     {
     	ceb.throwIfNotEmpty();
 
     	this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
     	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
         this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
 
     	File file = new File(getImageEditorPath() + File.separator + workingFileName);
     	BufferedImage original = javax.imageio.ImageIO.read(file);
 		
     	//scaleXY()
     	//BufferedImage image = imaging.scale(original, 1.2f, 0.6f);
     	
     	//scaleEqually() {
     	//BufferedImage image = imaging.scale(original, 0.5f);
 
     	int originalWidth = original.getWidth();
     	int originalHeight = original.getHeight();
     	float aspect = (float)originalWidth / (float)originalHeight;
     	BufferedImage image = null;
     	/*
     	System.out.println("originalWidth: " + originalWidth);
     	System.out.println("originalHeight: " + originalHeight);
     	System.out.println("height: " + height);
     	System.out.println("width: " + width);
     	System.out.println("keepRatio: " + keepRatio);
     	System.out.println("bestFit: " + bestFit);
     	*/
     	
     	if(keepRatio.equalsIgnoreCase("true"))
     	{
         	if(height == -1 && width != -1)
         	{
         		aspect = (float)width / (float)originalWidth;
         		//System.out.println("aspect1:" + aspect);
         	}
         	else if(width == -1 && height != -1)
         	{
         		aspect = (float)height / (float)originalHeight;
         		//System.out.println("aspect2:" + aspect);
         	}
         	else
         	{
         		aspect = (float)width / (float)originalWidth;
         		//System.out.println("aspect3:" + aspect + " from " + width + "/" + originalWidth + "(" + ((float)width / (float)originalWidth) + ")");
         	}
         	
         	//System.out.println("aspect:" + aspect);
         	
         	image = imaging.scale(original, aspect);        	
     	}
     	else
     	{
 	    	image = imaging.resize(original, width, height, false);
     	}
     	
    	workingFileName = "imageEditorWK_" + System.currentTimeMillis() + "_" + this.getInfoGluePrincipal().getName().hashCode() + "_" + digitalAssetVO.getDigitalAssetId() + ".png";    	
     	File outputFile = new File(getImageEditorPath() + File.separator + workingFileName);
     	outputFile.mkdirs();
     	javax.imageio.ImageIO.write(image, "PNG", outputFile);
 
     	//System.out.println("outputFile:" + outputFile.length());
 		this.modifiedFileUrl = getImageEditorBaseUrl() + workingFileName;
 		//System.out.println("modifiedFileUrl:" + modifiedFileUrl);
 		
         return "successResize";
     }    
 
     public String doCrop() throws Exception
     {
     	ceb.throwIfNotEmpty();
 
     	this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
     	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
         this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
 
     	File file = new File(getImageEditorPath() + File.separator + workingFileName);
     	BufferedImage original = javax.imageio.ImageIO.read(file);
 
     	BufferedImage image = imaging.crop(original, xpos1, ypos1, xpos2 - xpos1, ypos2 - ypos1);
 
    	workingFileName = "imageEditorWK_" + System.currentTimeMillis() + "_" + this.getInfoGluePrincipal().getName().hashCode() + "_" + digitalAssetVO.getDigitalAssetId() + ".png";    	
     	File outputFile = new File(getImageEditorPath() + File.separator + workingFileName);
     	javax.imageio.ImageIO.write(image, "PNG", outputFile);
 
     	//System.out.println("outputFile:" + outputFile.length());
 		this.modifiedFileUrl = getImageEditorBaseUrl() + workingFileName;
 		//System.out.println("modifiedFileUrl:" + modifiedFileUrl);
 		
         return "successCrop";
     }    
 
     public String doSave() throws Exception
     {
     	ceb.throwIfNotEmpty();
 
     	this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(this.digitalAssetId);
     	this.contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
         this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO.getContentId());
 
     	File file = new File(getImageEditorPath() + File.separator + workingFileName);
     	//System.out.println("saving file:" + file.getAbsolutePath());
 
    		String contentType = digitalAssetVO.getAssetContentType();
    		
		String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
		if(fromEncoding == null)
			fromEncoding = "iso-8859-1";
		
		String toEncoding = CmsPropertyHandler.getUploadToEncoding();
		if(toEncoding == null)
			toEncoding = "utf-8";
		
		this.digitalAssetKey = new String(digitalAssetKey.getBytes(fromEncoding), toEncoding);

     	DigitalAssetVO newAsset = new DigitalAssetVO();
 		newAsset.setAssetContentType(contentType);
 		newAsset.setAssetKey(this.digitalAssetKey);
 		newAsset.setAssetFileName(digitalAssetVO.getAssetFileName());
 		newAsset.setAssetFilePath(digitalAssetVO.getAssetFilePath());
 		newAsset.setAssetFileSize(new Integer(new Long(file.length()).intValue()));
 		InputStream is = new FileInputStream(file);
 		
 		if(this.contentTypeDefinitionId != null && digitalAssetKey != null)
 		{
 			AssetKeyDefinition assetKeyDefinition = ContentTypeDefinitionController.getController().getDefinedAssetKey(contentTypeDefinitionVO.getSchemaValue(), digitalAssetKey);
 			
 			if(assetKeyDefinition != null)
 			{
 				if(assetKeyDefinition.getMaximumSize().intValue() < new Long(file.length()).intValue())
 				{   
 				    file.delete();
 				    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnSizeText";
                 	return "uploadFailed";
 				}
 				if(assetKeyDefinition.getAllowedContentTypes().startsWith("image"))
 				{
 				    if(!contentType.startsWith("image"))
 				    {
 					    file.delete();
 					    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnTypeNotImageText";
 	                	return "uploadFailed";						        
 				    }
 
 				    Image image = javax.imageio.ImageIO.read(file);
 				    int width = image.getWidth(null);
 				    int height = image.getHeight(null);
 				    
 				    String allowedWidth = assetKeyDefinition.getImageWidth();
 				    String allowedHeight = assetKeyDefinition.getImageHeight();
 				    
 				    if(!allowedWidth.equals("*"))
 				    {
 				        Integer allowedWidthNumber = new Integer(allowedWidth.substring(1));
 				        if(allowedWidth.startsWith("<") && width >= allowedWidthNumber.intValue())
 				        {
 					        file.delete();
 						    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageToWideText";
 		                	return "uploadFailed";			
 				        }
 				        if(allowedWidth.startsWith(">") && width <= allowedWidthNumber.intValue())
 				        {
 					        file.delete();
 						    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageNotWideEnoughText";
 		                	return "uploadFailed";			
 				        }
 				        if(!allowedWidth.startsWith(">") && !allowedWidth.startsWith("<") && width != new Integer(allowedWidth).intValue())
 				        {
 				            file.delete();
 						    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageWrongWidthText";
 		                	return "uploadFailed";	
 				        }
 				    }
 				    
 				    if(!allowedHeight.equals("*"))
 				    {
 				        Integer allowedHeightNumber = new Integer(allowedHeight.substring(1));
 				        if(allowedHeight.startsWith("<") && height >= allowedHeightNumber.intValue())
 				        {
 					        file.delete();
 						    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageToHighText";
 		                	return "uploadFailed";			
 				        }
 				        if(allowedHeight.startsWith(">") && height <= allowedHeightNumber.intValue())
 				        {
 					        file.delete();
 						    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageNotHighEnoughText";
 		                	return "uploadFailed";			
 				        }
 				        if(!allowedHeight.startsWith(">") && !allowedHeight.startsWith("<") && height != new Integer(allowedHeight).intValue())
 				        {
 				            file.delete();
 						    this.reasonKey = "tool.contenttool.fileUpload.fileUploadFailedOnImageWrongHeightText";
 		                	return "uploadFailed";	
 				        }
 				    }
 				}
 			}
 		}
 		
 		if(this.contentVersionId != null)
 		    digitalAssetVO = DigitalAssetController.create(newAsset, is, this.contentVersionId);
  		//else
 		//    digitalAssetVO = DigitalAssetController.create(newAsset, is, this.entity, this.entityId);
 
    	workingFileName = "imageEditorWK_" + System.currentTimeMillis() + "_" + this.getInfoGluePrincipal().getName().hashCode() + "_" + digitalAssetVO.getDigitalAssetId() + ".png";
 		boolean deleted = file.delete();
 		//System.out.println("file:" + file.getAbsolutePath() + ":" + deleted);
 
 		cleanOldWorkingFiles(true);
 		
 		closeOnLoad = "true";
 		
         return "successSaveAndExit";
     }    
 
     /**
      * This method preserves space by only allowing 5 historic images and also cleaning up after a save totally.
      * All files older than 1 day are also removed.
      */
     private void cleanOldWorkingFiles(boolean cleanAll) throws Exception
     {
     	File workingAssetsDir = new File(getImageEditorPath());
 
     	final String matchString = "_" + this.getInfoGluePrincipal().getName() + "_" + this.digitalAssetId;
     	FilenameFilter filter = new FilenameFilter() 
     	{
     		public boolean accept(File dir, String name) 
             {
     			//System.out.println("name: " + name + ":" + name.indexOf(matchString));
     			//System.out.println("matchString: " + matchString);
     			return name.indexOf(matchString) > -1;
             }
         };
         
     	File[] files = workingAssetsDir.listFiles(filter);
     	for(int i=0; i < files.length; i++)
     	{
     		File file = files[i];
     		//System.out.println("file:" + file.getName());
     		boolean deleted = file.delete();
     	}
 
     	File[] allFiles = workingAssetsDir.listFiles();
     	for(int i=0; i < allFiles.length; i++)
     	{
     		File file = allFiles[i];
     		long modified = file.lastModified();
     		long difference = System.currentTimeMillis() - modified;
     		if(difference > (1000 * 60 * 60 * 12))
     			file.delete();
     	}
     }
     
     private String getImageEditorPath()
     {
     	String path = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "imageEditor";
     	File dir = new File(path);
     	dir.mkdirs();
     	return path;
     }
 
     private String getImageEditorBaseUrl()
     {
     	return CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/imageEditor/";
     }
 
     public void setDigitalAssetKey(String digitalAssetKey)
 	{
 		this.digitalAssetKey = digitalAssetKey;
 	}
 	    
 	public List getDefinedAssetKeys()
 	{
 		return ContentTypeDefinitionController.getController().getDefinedAssetKeys(this.contentTypeDefinitionVO.getSchemaValue());
 	}
 
 	public Integer getDigitalAssetId()
 	{
 		return digitalAssetId;
 	}
 
 	public void setDigitalAssetId(Integer digitalAssetId)
 	{
 		this.digitalAssetId = digitalAssetId;
 	}
 
 	public String getDigitalAssetKey()
 	{
 		return digitalAssetKey;
 	}
 
 	public boolean getIsUpdated()
 	{
 		return isUpdated;
 	}
 
 	public Integer getContentVersionId()
 	{
 		return contentVersionId;
 	}
 
 	public void setContentVersionId(Integer contentVersionId)
 	{
 		this.contentVersionId = contentVersionId;
 	}
     
     public String getCloseOnLoad()
     {
         return closeOnLoad;
     }
     
     public void setCloseOnLoad(String closeOnLoad)
     {
         this.closeOnLoad = closeOnLoad;
     }
 
 	public String getModifiedFileUrl() 
 	{
 		return modifiedFileUrl;
 	}
 
 	public int getXpos1() 
 	{
 		return xpos1;
 	}
 
 	public void setXpos1(int xpos1) 
 	{
 		this.xpos1 = xpos1;
 	}
 
 	public int getXpos2() 
 	{
 		return xpos2;
 	}
 
 	public void setXpos2(int xpos2) 
 	{
 		this.xpos2 = xpos2;
 	}
 
 	public int getYpos1() 
 	{
 		return ypos1;
 	}
 
 	public void setYpos1(int ypos1) 
 	{
 		this.ypos1 = ypos1;
 	}
 
 	public int getYpos2() 
 	{
 		return ypos2;
 	}
 
 	public void setYpos2(int ypos2) 
 	{
 		this.ypos2 = ypos2;
 	}
 
 	public String getWorkingFileName() 
 	{
 		return workingFileName;
 	}
 
 	public void setWorkingFileName(String workingFileName) 
 	{
 		this.workingFileName = workingFileName;
 	}
 
 	public int getHeight() 
 	{
 		return height;
 	}
 
 	public void setHeight(int height) 
 	{
 		this.height = height;
 	}
 
 	public int getWidth() 
 	{
 		return width;
 	}
 
 	public void setWidth(int width) 
 	{
 		this.width = width;
 	}
 
 	public void setKeepRatio(String keepRatio) 
 	{
 		this.keepRatio = keepRatio;
 	}
 
 	public void setBestFit(String bestFit) {
 		this.bestFit = bestFit;
 	}
     
 }
