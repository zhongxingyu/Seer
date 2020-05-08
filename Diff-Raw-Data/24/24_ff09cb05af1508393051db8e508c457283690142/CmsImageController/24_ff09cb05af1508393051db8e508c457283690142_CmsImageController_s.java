 package org.otherobjects.cms.controllers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.otherobjects.cms.Url;
 import org.otherobjects.cms.binding.BindService;
 import org.otherobjects.cms.dao.GenericDao;
 import org.otherobjects.cms.datastore.JackrabbitDataStore;
import org.otherobjects.cms.io.OoResourceLoader;
 import org.otherobjects.cms.model.CmsImage;
 import org.otherobjects.cms.model.Editable;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.util.ActionUtils;
 import org.otherobjects.cms.util.RequestUtils;
 import org.otherobjects.cms.validation.ValidatorService;
 import org.otherobjects.cms.views.JsonView;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Controller to process form submissions. Only data for types registered in the TypeService is supported.
  *
  * @author rich
  */
 @Controller
 @SuppressWarnings("unchecked")
 public class CmsImageController
 {
     private final Logger logger = LoggerFactory.getLogger(CmsImageController.class);
 
     @Resource
     private JackrabbitDataStore jackrabbitDataStore;
 
     @Resource
     private TypeService typeService;
 
     @Resource
     private BindService bindService;
 
     @Resource
     private ValidatorService validatorService;
 
    @Resource
    private OoResourceLoader ooResourceLoader;
 
     @RequestMapping({"/image/save"})
    private ModelAndView save(MultipartHttpServletRequest multipartRequest, HttpServletResponse response, CmsImage cmsImage) throws IOException
     {
         ActionUtils actionUtils = new ActionUtils(multipartRequest, response, null, null);
 
         // Prepare object (create or fetch)
         String id = multipartRequest.getParameter("_oo_id");
         String typeName = multipartRequest.getParameter("_oo_type");
         String containerId = multipartRequest.getParameter("_oo_containerId");
         Assert.notNull(typeName, "Type name not specified in _oo_type parameter.");
         TypeDef typeDef = typeService.getType(typeName);
         Assert.notNull(typeDef, "No typeDef found for type: " + typeName);
 
         Editable item = null;
         GenericDao genericDao = jackrabbitDataStore.getDao(typeDef);
 
         if (StringUtils.isNotBlank(id))
         {
             item = (Editable) jackrabbitDataStore.get(id);
         }
         else
         {
             item = (Editable) jackrabbitDataStore.create(typeDef, containerId);
         }
 
         Assert.notNull(item, "Could not prepare item for binding: " + typeName);
 
         // Bind and validate
         BindingResult errors = null;
         errors = bindService.bind(item, typeDef, multipartRequest);
         Validator validator = validatorService.getValidator(item);
         if (validator != null)
             validator.validate(item, errors);
         else
             logger.warn("No validator for item of class " + item.getClass() + " found");
 
         // Save
         boolean success = false;
         if (!(errors != null && errors.hasErrors()))
         {
             // Save new object
             item = (Editable) genericDao.save(item, false);
             success = true;
         }
         
         CmsImage image = (CmsImage) item;
         if (image.getNewFile() != null)
         {
 //            Dimension imageDimensions = ImageUtils.getImageDimensions(image.getNewFile());
 //            image.setOriginalWidth(new Double(imageDimensions.getWidth()).longValue());
 //            image.setOriginalHeight(new Double(imageDimensions.getHeight()).longValue());
 //            //DataFile original = new DataFile(image.getNewFile());
 //            original.setFileName(image.getCode());
 //            original.setCollection(CmsImage.DATA_FILE_COLLECTION_NAME);
 //            original.setPath(CmsImage.ORIGINALS_PATH);
 //            //original = this.dataFileDao.save(original);
 //            image.setOriginalFileName(original.getId());
 //            image.setNewFile(null);
 //
 //            // Create thumbnail
 //            this.cmsImageTool.getThumbnail(image);
         }
         
 
         // Prepare return data
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("success", success);
         data.put("formObject", item);
         if (!success)
         {
             List<Object> jsonErrors = new ArrayList<Object>();
             for (FieldError e : (List<FieldError>) errors.getFieldErrors())
             {
                 Map<String, String> error = new HashMap<String, String>();
                 error.put("id", e.getField());
                 //error.put("msg", requestContext.getMessage(e.getCode(), e.getArguments()));
                 jsonErrors.add(error);
             }
             data.put("success", false);
             data.put("errors", jsonErrors);
         }
 
         // Return
         if (RequestUtils.isXhr(multipartRequest))
         {
             // Return via XHR
             ModelAndView view = new ModelAndView("jsonView");
             view.addObject(JsonView.JSON_DATA_KEY, data);
             return view;
         }
         else
         {
             // Return normally
             if (success)
             {
                 actionUtils.flashInfo("Your object was saved.");
                 Url u = new Url("/otherobjects/workbench/view/" + item.getEditableId());
                 response.sendRedirect(u.toString());
                 return null;
             }
             else
             {
                 actionUtils.flashWarning("Your object could not be saved. See below for errors.");
                 ModelAndView view = new ModelAndView("/otherobjects/templates/legacy/pages/edit");
                 view.addObject("success", success);
                 view.addObject("id", item.getEditableId());
                 view.addObject("typeDef", typeDef);
                 view.addObject("object", item);
                 view.addObject("containerId", containerId);
                 view.addObject("org.springframework.validation.BindingResult.object", errors);
                 return view;
             }
         }
         
         /*
         MultipartFile imageFile = (MultipartFile) multipartRequest.getFileMap().get("newFile");
         this.logger.info("Received file " + imageFile.getOriginalFilename());
 
         CmsImageDao cmsImageDao = (CmsImageDao) this.daoService.getDao(CmsImage.class);
 
         File newFile = File.createTempFile("upload", "img");
         newFile.deleteOnExit();
 
         imageFile.transferTo(newFile);
 
         cmsImage.setPath("/libraries/images/");
         cmsImage.setCode(imageFile.getOriginalFilename());
         cmsImage.setLabel(imageFile.getOriginalFilename());
         cmsImage.setNewFile(newFile);
 
         // some manual binding which is pants
         cmsImage.setDescription(multipartRequest.getParameter("description"));
         cmsImage.setKeywords(multipartRequest.getParameter("keywords"));
         cmsImage.setOriginalProvider(multipartRequest.getParameter("originalProvider"));
         cmsImage.setProviderId(multipartRequest.getParameter("providerId"));
         cmsImage.setCopyright(multipartRequest.getParameter("copyright"));
         
 
         // Look for IPTC tags to read
         Metadata imageMetadata = ImageUtils.getImageMetadata(newFile);
         if (imageMetadata.containsDirectory(IptcDirectory.class))
         {
             Directory iptc = imageMetadata.getDirectory(IptcDirectory.class);
             if (iptc.containsTag(IptcDirectory.TAG_OBJECT_NAME))
                 cmsImage.setLabel(iptc.getString(IptcDirectory.TAG_OBJECT_NAME));
             if (iptc.containsTag(IptcDirectory.TAG_CAPTION))
                 cmsImage.setDescription(iptc.getString(IptcDirectory.TAG_CAPTION));
             if (iptc.containsTag(IptcDirectory.TAG_COPYRIGHT_NOTICE))
                 cmsImage.setCopyright(iptc.getString(IptcDirectory.TAG_COPYRIGHT_NOTICE));
             if (iptc.containsTag(IptcDirectory.TAG_KEYWORDS))
                 cmsImage.setKeywords(iptc.getString(IptcDirectory.TAG_KEYWORDS));
         }
 
         // Get file proprerties
         cmsImageDao.save(cmsImage);
         newFile.delete();
 
         // We have errors so return error messages
         ModelAndView view = new ModelAndView("jsonView");
         view.addObject("mimeOverride", "text/html");
 
         // All OK...
         view.addObject("success", true);
         view.addObject("formObject", cmsImage);
         return view;
         */
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
     public void setBindService(BindService bindService)
     {
         this.bindService = bindService;
     }
 
     public void setValidatorService(ValidatorService validatorService)
     {
         this.validatorService = validatorService;
     }
 }
