 package com.xone.action.web.product;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.xone.action.base.LogicAction;
 import com.xone.model.hibernate.entity.Adbanner;
 import com.xone.model.hibernate.entity.ImageUploaded;
 import com.xone.model.hibernate.entity.Product;
 import com.xone.model.hibernate.support.Pagination;
 import com.xone.model.utils.MyDateUtils;
 import com.xone.service.app.AdbannerService;
 import com.xone.service.app.ProductGroupService;
 import com.xone.service.app.ProductService;
 import com.xone.service.app.utils.MyBeanUtils;
 import com.xone.service.app.utils.MyBeanUtils.AssignRules;
 import com.xone.service.app.utils.MyBeanUtils.CopyRules;
 
 public class ProductWebAction extends LogicAction {
 
 	private static final long serialVersionUID = -249087929101371902L;
 
 	@Autowired
 	protected ProductService productService;
 	protected List<Product> list = new ArrayList<Product>();
 	protected Product product = new Product();
 	protected Pagination pagination = new Pagination();
 
 	protected File uploadFile1;
 	protected String uploadFile1ContentType;
 	protected String uploadFile1FileName;
 
 	protected File uploadFile2;
 	protected String uploadFile2ContentType;
 	protected String uploadFile2FileName;
 
 	protected File uploadFile3;
 	protected String uploadFile3ContentType;
 	protected String uploadFile3FileName;
 
 	protected String imageUploadPath;
 
 	protected ProductGroupService productGroupService;
 
 	protected Integer orderedProductNum;
 	protected Integer orderedPersonNum;
 
 	protected String searchType = "1";
 	protected String searchKey;
 
 	@Autowired
 	protected AdbannerService adbannerService;
 	protected List<Adbanner> adList = new ArrayList<Adbanner>();
 
 	public Enum<?>[] getFlagDeleted() {
 		return Product.FlagDeleted.values();
 	}
 
 	public Enum<?>[] getCheckStatus() {
 		return Product.CheckStatus.values();
 	}
 
 	public Enum<?>[] getProductType() {
 		return Product.ProductType.values();
 	}
 
 	public Enum<?>[] getSaleType() {
 		return Product.SaleType.values();
 	}
 
 	public String productList() throws Exception {
 		Map<String, String> params = new HashMap<String, String>();
 		MyBeanUtils.copyPropertiesToMap(getProduct(), params, new CopyRules() {
 			@Override
 			public boolean myCopyRules(Object value) {
 				return null != value;
 			}
 
 		}, new AssignRules() {
 			@Override
 			public String myAssignRules(Object value) {
 				if (null != value && value instanceof Date) {
 					return MyDateUtils.format((Date) value, "yyyy-MM-dd");
 				}
 				return value.toString();
 			}
 		}, null);
 
 		params.put("userApply", getUserId().toString());
 		params.put("flagDeleted", Product.FlagDeleted.NORMAL.getValue());
 
 		params.put("pageSize", String.valueOf(getPagination().getPageSize()));
 		params.put("pageNo", String.valueOf(getPagination().getPageNo()));
 		Pagination p = getProductService().findByParams(params);
 		setPagination(p);
 		return SUCCESS;
 	}
 
 	public String productNormalList() throws Exception {
 		product.setSaleType(Product.SaleType.NORMAL.getValue());
 
 		return productList();
 	}
 	
 	public String productSalesList() throws Exception {
 		product.setSaleType(Product.SaleType.SALES.getValue());
 		
 		return productList();
 	}
 	
 	public String productGroupsList() throws Exception {
 		product.setSaleType(Product.SaleType.GROUPS.getValue());
 		
 		return productList();
 	}
 
 	public String productListAjax() throws Exception {
 		Map<String, String> params = new HashMap<String, String>();
 		MyBeanUtils.copyPropertiesToMap(getProduct(), params, new CopyRules() {
 			@Override
 			public boolean myCopyRules(Object value) {
 				return null != value;
 			}
 
 		}, new AssignRules() {
 			@Override
 			public String myAssignRules(Object value) {
 				if (null != value && value instanceof Date) {
 					return MyDateUtils.format((Date) value, "yyyy-MM-dd");
 				}
 				return value.toString();
 			}
 		}, null);
 
 		params.put("flagDeleted", Product.FlagDeleted.NORMAL.getValue());
 
 		params.put("pageSize", String.valueOf(getPagination().getPageSize()));
 		params.put("pageNo", String.valueOf(getPagination().getPageNo()));
 		Pagination p = getProductService().findByParams(params);
 		setPagination(p);
 		return SUCCESS;
 	}
 
 	public String productItem() throws Exception {
 		Product entity = getProductService().findById(getProduct().getId());
 		if (null == entity || null == entity.getId()) {
 			return ERROR;
 		}
 
 		orderedPersonNum = getProductGroupService().getOrderPersonNum(
 				entity.getId());
 		orderedProductNum = getProductGroupService().getOrderProductNum(
 				entity.getId());
 
 		setProduct(entity);
 		return SUCCESS;
 	}
 
 	public String productCreate() throws Exception {
 //		product.setProductNum("0");
 //		product.setProductPrice("0");
 
 		return SUCCESS;
 	}
 	
 	public String productNormalCreate() throws Exception {
 		product.setSaleType(Product.SaleType.NORMAL.getValue());
 //		product.setProductNum("0");
 //		product.setProductPrice("0");
 		
 		return SUCCESS;
 	}
 
 	public String productSalesCreate() throws Exception {
 		product.setSaleType(Product.SaleType.SALES.getValue());
 //		product.setProductNum("0");
 //		product.setProductPrice("0");
 
 		return SUCCESS;
 	}
 
 	public String productGroupsCreate() throws Exception {
 		product.setSaleType(Product.SaleType.GROUPS.getValue());
 //		product.setProductNum("0");
 //		product.setProductPrice("0");
 
 		return SUCCESS;
 	}
 
 	public String productEdit() throws Exception {
 		Product entity = getProductService().findById(getProduct().getId());
 		if (null == entity || null == entity.getId()) {
 			return ERROR;
 		}
 
 		orderedPersonNum = getProductGroupService().getOrderPersonNum(
 				entity.getId());
 		orderedProductNum = getProductGroupService().getOrderProductNum(
 				entity.getId());
 
 		setProduct(entity);
 		return SUCCESS;
 	}
 
 	public String productSave() throws Exception {
 		product.setUserCreated(getUserId());
 		product.setDateCreated(new Date());
 		product.setUserUpdated(getUserId());
 		product.setLastUpdated(new Date());
 
 		product.setUserApply(getUserId());
 		product.setDateApply(new Date());
 		try {
 			setProduct(getProductService().save(getProduct(), getImageList()));
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			if( e instanceof FileUploadException){
 				addFieldError("uploadFile1", "请上传小于2M的png,jpg,gif图片");
 			}
 			return INPUT;
 		}
 
		setProduct(getProductService().save(getProduct()));
 		return SUCCESS;
 	}
 	
 	private List<ImageUploaded> getImageList() {
 		List<ImageUploaded> images = new ArrayList<ImageUploaded>();
 		if (uploadFile1 != null) {
 			ImageUploaded imageUploaded1 = createUploadImageByFile(
 					imageUploadPath, ImageUploaded.RefType.PRODUCT,
 					getUploadFile1(), getUploadFile1ContentType(),
 					getUploadFile1FileName());
 			images.add(imageUploaded1);
 		}
 		if (uploadFile2 != null) {
 			ImageUploaded imageUploaded2 = createUploadImageByFile(
 					imageUploadPath, ImageUploaded.RefType.PRODUCT,
 					getUploadFile2(), getUploadFile2ContentType(),
 					getUploadFile2FileName());
 			images.add(imageUploaded2);
 		}
 		if (uploadFile3 != null) {
 			ImageUploaded imageUploaded3 = createUploadImageByFile(
 					imageUploadPath, ImageUploaded.RefType.PRODUCT,
 					getUploadFile3(), getUploadFile3ContentType(),
 					getUploadFile3FileName());
 			images.add(imageUploaded3);
 		}
 
 		return images;
 	}
 
 	public String productUpdate() throws Exception {
 		if (!"POST".equalsIgnoreCase(getRequest().getMethod())) {
 			return ERROR;
 		}
 		String opt = null == getRequestMap().get("delete") ? getRequestMap()
 				.get("update") : getRequestMap().get("delete");
 		if (!StringUtils.isBlank(opt) && "delete".equals(opt)) {
 			getProductService().updateCloseRecord(getProduct().getId(), getUserId());
 			product.setCheckStatus(Product.CheckStatus.CLOSED.getValue());
 			return "item";
 		}
 		if (!StringUtils.isBlank(opt) && "update".equals(opt)) {
 			Product entity = getProductService().findById(getProduct().getId());
 			if (null == entity || null == entity.getId()) {
 				return ERROR;
 			}
 			MyBeanUtils.copyProperties(getProduct(), entity, Product.class,
 					null, new CopyRules() {
 						@Override
 						public boolean myCopyRules(Object value) {
 							return (null != value);
 						}
 					});
 			entity.setUserUpdated(getUserId());
 			entity.setLastUpdated(new Date());
 			try {
 				setProduct(getProductService().update(entity, getImageList(),
 						product.getIds()));
 			} catch (Exception e) {
 				log.error(e.getMessage(), e);
 				if( e instanceof FileUploadException){
 					addFieldError("uploadFile1", "请上传小于2M的png,jpg,gif图片");
 				}
 				return INPUT;
 			}
 		}
 		return SUCCESS;
 	}
 
 	public String list() {
 		Map<String, String> params = new HashMap<String, String>();
 		MyBeanUtils.copyPropertiesToMap(getProduct(), params, new CopyRules() {
 			@Override
 			public boolean myCopyRules(Object value) {
 				return null != value;
 			}
 
 		}, new AssignRules() {
 			@Override
 			public String myAssignRules(Object value) {
 				if (null != value && value instanceof Date) {
 					return MyDateUtils.format((Date) value, "yyyy-MM-dd");
 				}
 				return value.toString();
 			}
 		}, null);
 		
 		// nav search
 		if ("1".equals(searchType) && !StringUtils.isBlank(searchKey)) {
 			params.put("productName", searchKey);
 		}
 		params.put("flagDeleted", Product.FlagDeleted.NORMAL.getValue());
 		params.put("checkStatus", Product.CheckStatus.PASSED.getValue());
 
 		params.put("pageSize", String.valueOf(getPagination().getPageSize()));
 		params.put("pageNo", String.valueOf(getPagination().getPageNo()));
 		Pagination p = getProductService().findByParams(params);
 		setPagination(p);
 		
 		// get ad
 		setAdList(getAdbannerService().findItemsByMap(
 				new HashMap<String, String>()));
 		return SUCCESS;
 	}
 
 	public String item() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("id", String.valueOf(getProduct().getId()));
 		setProduct(getProductService().findByMap(params));
 
 		// get ad
 		setAdList(getAdbannerService().findItemsByMap(
 				new HashMap<String, String>()));
 		return SUCCESS;
 	}
 
 	public ProductService getProductService() {
 		return productService;
 	}
 
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 
 	public List<Product> getList() {
 		return list;
 	}
 
 	public void setList(List<Product> list) {
 		this.list = list;
 	}
 
 	public Product getProduct() {
 		return product;
 	}
 
 	public void setProduct(Product product) {
 		this.product = product;
 	}
 
 	public Pagination getPagination() {
 		return pagination;
 	}
 
 	public void setPagination(Pagination pagination) {
 		this.pagination = pagination;
 	}
 
 	public File getUploadFile1() {
 		return uploadFile1;
 	}
 
 	public void setUploadFile1(File uploadFile1) {
 		this.uploadFile1 = uploadFile1;
 	}
 
 	public String getUploadFile1ContentType() {
 		return uploadFile1ContentType;
 	}
 
 	public void setUploadFile1ContentType(String uploadFile1ContentType) {
 		this.uploadFile1ContentType = uploadFile1ContentType;
 	}
 
 	public String getUploadFile1FileName() {
 		return uploadFile1FileName;
 	}
 
 	public void setUploadFile1FileName(String uploadFile1FileName) {
 		this.uploadFile1FileName = uploadFile1FileName;
 	}
 
 	public File getUploadFile2() {
 		return uploadFile2;
 	}
 
 	public void setUploadFile2(File uploadFile2) {
 		this.uploadFile2 = uploadFile2;
 	}
 
 	public String getUploadFile2ContentType() {
 		return uploadFile2ContentType;
 	}
 
 	public void setUploadFile2ContentType(String uploadFile2ContentType) {
 		this.uploadFile2ContentType = uploadFile2ContentType;
 	}
 
 	public String getUploadFile2FileName() {
 		return uploadFile2FileName;
 	}
 
 	public void setUploadFile2FileName(String uploadFile2FileName) {
 		this.uploadFile2FileName = uploadFile2FileName;
 	}
 
 	public File getUploadFile3() {
 		return uploadFile3;
 	}
 
 	public void setUploadFile3(File uploadFile3) {
 		this.uploadFile3 = uploadFile3;
 	}
 
 	public String getUploadFile3ContentType() {
 		return uploadFile3ContentType;
 	}
 
 	public void setUploadFile3ContentType(String uploadFile3ContentType) {
 		this.uploadFile3ContentType = uploadFile3ContentType;
 	}
 
 	public String getUploadFile3FileName() {
 		return uploadFile3FileName;
 	}
 
 	public void setUploadFile3FileName(String uploadFile3FileName) {
 		this.uploadFile3FileName = uploadFile3FileName;
 	}
 
 	public String getImageUploadPath() {
 		return imageUploadPath;
 	}
 
 	public void setImageUploadPath(String imageUploadPath) {
 		this.imageUploadPath = imageUploadPath;
 	}
 
 	public ProductGroupService getProductGroupService() {
 		return productGroupService;
 	}
 
 	public void setProductGroupService(ProductGroupService productGroupService) {
 		this.productGroupService = productGroupService;
 	}
 
 	public Integer getOrderedProductNum() {
 		return orderedProductNum;
 	}
 
 	public void setOrderedProductNum(Integer orderedProductNum) {
 		this.orderedProductNum = orderedProductNum;
 	}
 
 	public Integer getOrderedPersonNum() {
 		return orderedPersonNum;
 	}
 
 	public void setOrderedPersonNum(Integer orderedPersonNum) {
 		this.orderedPersonNum = orderedPersonNum;
 	}
 
 	public String getSearchType() {
 		return searchType;
 	}
 
 	public void setSearchType(String searchType) {
 		this.searchType = searchType;
 	}
 
 	public String getSearchKey() {
 		return searchKey;
 	}
 
 	public void setSearchKey(String searchKey) {
 		this.searchKey = searchKey;
 	}
 
 	public AdbannerService getAdbannerService() {
 		return adbannerService;
 	}
 
 	public void setAdbannerService(AdbannerService adbannerService) {
 		this.adbannerService = adbannerService;
 	}
 
 	public List<Adbanner> getAdList() {
 		return adList;
 	}
 
 	public void setAdList(List<Adbanner> adList) {
 		this.adList = adList;
 	}
 
 }
