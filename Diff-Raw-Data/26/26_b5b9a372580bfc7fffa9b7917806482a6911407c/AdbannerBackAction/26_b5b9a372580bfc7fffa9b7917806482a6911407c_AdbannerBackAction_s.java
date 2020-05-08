 package com.xone.action.back.adbanner;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.xone.action.base.LogicAction;
 import com.xone.model.hibernate.entity.Adbanner;
 import com.xone.model.hibernate.entity.ImageUploaded;
 import com.xone.model.hibernate.support.Pagination;
 import com.xone.model.utils.MyDateUtils;
 import com.xone.service.app.AdbannerService;
 import com.xone.service.app.utils.MyBeanUtils;
 import com.xone.service.app.utils.MyBeanUtils.AssignRules;
 import com.xone.service.app.utils.MyBeanUtils.CopyRules;
 
 public class AdbannerBackAction extends LogicAction {
 	
 	/**
      * 
      */
     private static final long serialVersionUID = -7661392246629533090L;
     @Autowired
 	protected AdbannerService adbannerService;
 	protected Adbanner adbanner = new Adbanner();
 	protected List<Adbanner> list = new ArrayList<Adbanner>();
 	protected Pagination pagination = new Pagination();
 	protected File uploadFile; // 得到上传的文件,此属性对应于表单中文件字段的名称  
     //下面的这两个属性的命名必须遵守上定的规则，即为"表单中文件字段的名称" + "相应的后缀"  
 	protected String uploadFileContentType; // 得到上传的文件的数据类型,  
 	protected String uploadFileFileName; // 得到上传的文件的名称 
 	protected String imageUploadPath;
 	
 	public String adbannerList() throws Exception {
 		Map<String, String> params = new HashMap<String, String>();
 		MyBeanUtils.copyPropertiesToMap(getAdbanner(), params, new CopyRules() {
 			@Override
 			public boolean myCopyRules(Object value) {
 				return null != value;
 			}
 			
 		}, new AssignRules() {
 			@Override
 			public String myAssignRules(Object value) {
 				if (null != value && value instanceof Date) {
 					return MyDateUtils.format((Date)value, "yyyy-MM-dd");
 				}
 				return value.toString();
 			}
 		}, null);
 		params.put("pageSize", String.valueOf(getPagination().getPageSize()));
 		params.put("pageNo", String.valueOf(getPagination().getPageNo()));
 		Pagination p = getAdbannerService().findByParams(params);
 //		List<Adbanner> l = getAdbannerService().findAllByMap(params);
 //		if (null != l && !l.isEmpty()) {
 //			getList().addAll(l);
 //		}
 		setPagination(p);
 		return SUCCESS;
 	}
 	
 	public String adbannerItem() throws Exception {
 		Adbanner entity = getAdbannerService().findById(getAdbanner().getId());
 		if (null == entity || null == entity.getId()) {
 			return ERROR;
 		}
 		setAdbanner(entity);
 		return SUCCESS;
 	}
 	
 	public String adbannerCreate() throws Exception {
 		return SUCCESS;
 	}
 	
 	public String adbannerEdit() throws Exception {
 		Adbanner entity = getAdbannerService().findById(getAdbanner().getId());
 		if (null == entity || null == entity.getId()) {
 			return ERROR;
 		}
 		setAdbanner(entity);
 		return SUCCESS;
 	}
 	
 	public String adbannerSave() throws Exception {
 		ImageUploaded imageUploaded = createUploadImageByFile(imageUploadPath,
 				ImageUploaded.RefType.ABBANNER, getUploadFile(),
 				getUploadFileContentType(), getUploadFileFileName());
 		adbanner.setUserCreated(getUserId());
 		adbanner.setDateCreated(new Date());
 		adbanner.setUserUpdated(getUserId());
 		adbanner.setLastUpdated(new Date());
 		adbanner.setFlagDeleted(Adbanner.FlagDeleted.NORMAL.getValue());
 		setAdbanner(getAdbannerService().save(getAdbanner(), imageUploaded));
 		return SUCCESS;
 	}
 	
 	public String adbannerUpdate() throws Exception {
 		if (!"POST".equalsIgnoreCase(getRequest().getMethod())) {
 			return ERROR;
 		}
 		String opt = null == getRequestMap().get("delete") ? getRequestMap().get("update") : getRequestMap().get("delete");
 		if (!StringUtils.isBlank(opt) && "delete".equals(opt)) {
 			Adbanner entity = getAdbannerService().findById(getAdbanner().getId());
 			if (null == entity || null == entity.getId()) {
 				return ERROR;
 			}
 			getAdbannerService().delete(entity);
 			return "list";
 		}
 		if (!StringUtils.isBlank(opt) && "update".equals(opt)) {
 			Adbanner entity = getAdbannerService().findById(getAdbanner().getId());
 			if (null == entity || null == entity.getId()) {
 				return ERROR;
 			}
 			MyBeanUtils.copyProperties(getAdbanner(), entity, Adbanner.class, null, new CopyRules() {
 				@Override
 				public boolean myCopyRules(Object value) {
 					return (null != value);
 				}
 			});
 			ImageUploaded imageUploaded = null;
 			if (null != getUploadFile()) {
 				imageUploaded = createUploadImageByFile(imageUploadPath,
 						ImageUploaded.RefType.ABBANNER, getUploadFile(),
 						getUploadFileContentType(), getUploadFileFileName());
 			}
 	        adbanner.setUserUpdated(getUserId());
 	        adbanner.setLastUpdated(new Date());
 			setAdbanner(getAdbannerService().update(entity, imageUploaded, getAdbanner().getAdRefId()));
 		}
 		return SUCCESS;
 	}
 	
     public String adbannerDelete() throws Exception {
         Adbanner entity = getAdbannerService().findById(getAdbanner().getId());
         adbannerService.delete(entity);
         return SUCCESS;
     }
 
 	public AdbannerService getAdbannerService() {
 		return adbannerService;
 	}
 
 	public void setAdbannerService(AdbannerService adbannerService) {
 		this.adbannerService = adbannerService;
 	}
 
 	public List<Adbanner> getList() {
 		return list;
 	}
 
 	public void setList(List<Adbanner> list) {
 		this.list = list;
 	}
 
 	public Adbanner getAdbanner() {
 		return adbanner;
 	}
 
 	public void setAdbanner(Adbanner adbanner) {
 		this.adbanner = adbanner;
 	}
 	
 	public Pagination getPagination() {
 		return pagination;
 	}
 
 	public void setPagination(Pagination pagination) {
 		this.pagination = pagination;
 	}
 
 	public File getUploadFile() {
 		return uploadFile;
 	}
 
 	public void setUploadFile(File uploadFile) {
 		this.uploadFile = uploadFile;
 	}
 
 	public String getUploadFileContentType() {
 		return uploadFileContentType;
 	}
 
 	public void setUploadFileContentType(String uploadFileContentType) {
 		this.uploadFileContentType = uploadFileContentType;
 	}
 
 	public String getUploadFileFileName() {
 		return uploadFileFileName;
 	}
 
 	public void setUploadFileFileName(String uploadFileFileName) {
 		this.uploadFileFileName = uploadFileFileName;
 	}
 
 	public String getImageUploadPath() {
 		return imageUploadPath;
 	}
 
 	public void setImageUploadPath(String imageUploadPath) {
 		this.imageUploadPath = imageUploadPath;
 	}
 	
 }
