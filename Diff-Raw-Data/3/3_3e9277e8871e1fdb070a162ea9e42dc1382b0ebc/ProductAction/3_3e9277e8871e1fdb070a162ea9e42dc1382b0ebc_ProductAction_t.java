 
 package com.xone.action.app.product;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.xone.action.base.LogicAction;
 import com.xone.model.hibernate.entity.ImageUploaded;
 import com.xone.model.hibernate.entity.Overhead;
 import com.xone.model.hibernate.entity.Product;
 import com.xone.model.hibernate.entity.ProductGroup;
 import com.xone.model.utils.MyDateUtils;
 import com.xone.model.utils.MyModelUtils;
 import com.xone.service.app.OverheadService;
 import com.xone.service.app.ProductGroupService;
 import com.xone.service.app.ProductService;
 import com.xone.service.app.SubscribeService;
 import com.xone.service.app.utils.AppConstants;
 import com.xone.service.app.utils.MyBeanUtils;
 import com.xone.service.app.utils.MyBeanUtils.CopyRules;
 import com.xone.service.app.utils.MyServerUtils;
 
 public class ProductAction extends LogicAction {
 	
 	
 	private static final long serialVersionUID = -5292513131447154086L;
 	
 	@Autowired
 	protected ProductService productService;
 	
 	@Autowired
 	protected SubscribeService subscribeService;
 	
 	@Autowired
 	protected ProductGroupService productGroupService;
 	
 	@Autowired
 	protected OverheadService overheadService;
 	
 	protected Product product = new Product();
 	protected ProductGroup productGroup = new ProductGroup();
 	protected Overhead overhead = new Overhead();
 	protected String imageUploadPath;
 	protected List<Product> list = new ArrayList<Product>();
 
 	public String listAll() {
 		product.setSaleType(Product.SaleType.NORMAL.getValue());
 		return SUCCESS;
 	}
 	
 	public Enum<?>[] getProductType() {
 		return Product.ProductType.values();
 	}
 	
 	/**
 	 * 手机上滑动更多
 	 * @return
 	 */
 	public String listMobileMore() {
 		return SUCCESS;
 	}
 	
 	public String listSales() {
 		product.setSaleType(Product.SaleType.SALES.getValue());
 		return SUCCESS;
 	}
 	
 	public String listGroups() {
 		product.setSaleType(Product.SaleType.GROUPS.getValue());
 		return SUCCESS;
 	}
 	
 	public String add() {
 		getProduct().setSaleType(Product.SaleType.NORMAL.getValue());
 		return SUCCESS;
 	}
 	
 	public String addSales() {
 		getProduct().setSaleType(Product.SaleType.SALES.getValue());
 		return SUCCESS;
 	}
 	
 	public String addGroups() {
 		getProduct().setSaleType(Product.SaleType.GROUPS.getValue());
 		return SUCCESS;
 	}
 	
 	/**
 	 * 组团操作
 	 * @return
 	 */
 	public String doGroups() {
 		getProductGroup().setUserCreated(getUserId());
 		getProductGroup().setFlagDeleted(ProductGroup.FlagDeleted.NORMAL.getValue());
 		getProductGroup().setDateApply(new Date());
 		getProductGroupService().save(getProductGroup());
 		return SUCCESS;
 	}
 	
 	/**
 	 * 置顶申请
 	 * @return
 	 */
 	public String doTopApply() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("refId", getOverhead().getRefId().toString());
 		params.put("overheadType", getOverhead().getOverheadType());
 		Overhead overhead = getOverheadService().findByMap(params);
 		if (null != overhead && null != overhead.getId()) {
 			return SUCCESS;
 		}
 		Long userId = getUserId();
 		getOverhead().setUserApply(userId);
 		getOverhead().setDateApply(new Date());
 		getOverhead().setUserCreated(userId);
 		getOverheadService().save(getOverhead());
 		return SUCCESS;
 	}
 	
 	public String create() {
 		getProduct().setCheckStatus(Product.CheckStatus.WAITING.getValue());
 		getProduct().setUserCreated(getUserId());
 		List<ImageUploaded> images = super.createImageByParams(getImageUploadPath(), ImageUploaded.RefType.PRODUCT);
 		setProduct(getProductService().save(getProduct(), images));
 		return SUCCESS;
 	}
 	
 	public String listSubscribeProduct() {
 		Map<String, String> param = getRequestMap();
 		param.put("userId", getUserIdString());
 		Map<String, String> params = getSubscribeService().updateSubscribeProductInfo(param);
 		if (null != params && !params.isEmpty()) {
 			getMapValue().putAll(params);
 		}
 		return SUCCESS;
 	}
 	
 	public String listItems() {
 		Map<String, String> map = getRequestMap();
 		int length = MyModelUtils.parseInt(map.get("itemcount"), 0);
 		Map<String, Object> params = new HashMap<String, Object>();
 		if (length >= AppConstants.LIST_ITEM_LENGTH) {
 			getMapValue().put("ITEM_TOO_LONG", "YES");
 		} else {
 			if ("down".equals(map.get("itemaction")) && null != getProduct().getDateCreated()) {
 				params.put("gtDateCreated", MyDateUtils.format(getProduct().getDateCreated()));
 			} else if ("up".equals(map.get("itemaction")) && null != getProduct().getDateCreated()) {
 				params.put("ltDateCreated", MyDateUtils.format(getProduct().getDateCreated()));
 			}
 			if (null != getProduct().getGtDateCreated()) {
 				params.put("gtDateCreated", MyServerUtils.format(getProduct().getGtDateCreated()));
 			}
 			if (!StringUtils.isBlank(getProduct().getProductName())) {
 				params.put("productName", getProduct().getProductName());
 			}
 			if (!StringUtils.isBlank(map.get("exIds"))) {
 				params.put("exIds", Arrays.asList(map.get("exIds").split(",")));
 			}
			if (!StringUtils.isBlank(map.get("productTypes"))) {
				params.put("productTypes", Arrays.asList(map.get("productTypes").split(",")));
			}
 			params.put("saleType", getProduct().getSaleType());
 			params.put("userLevels", getLogicUserLevel());
 			params.put("checkStatus", Product.CheckStatus.PASSED.getValue());
 			params.put("flagDeleted", Product.FlagDeleted.NORMAL.getValue());
 			setList(getProductService().findAllByMap(params));
 		}
 		return SUCCESS;
 	}
 	
 	/**
 	 * 查询产品顶置列表
 	 * @return
 	 */
 	public String listOverheadItems() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("overheadType", getProduct().getSaleType());
 		params.put("checkStatus", Overhead.CheckStatus.PASSED.getValue());
 		params.put("flagDeleted", Overhead.FlagDeleted.NORMAL.getValue());
 		List<Overhead> list = getOverheadService().findAllByMap(params);
 		if (null != list && !list.isEmpty()) {
 			List<Long> ids = new ArrayList<Long>();
 			for (Overhead o : list) {
 				ids.add(o.getRefId());
 			}
 			params.clear();
 			params.put("checkStatus", Product.CheckStatus.PASSED.getValue());
 			params.put("flagDeleted", Product.FlagDeleted.NORMAL.getValue());
 			List<Product> pList = getProductService().findAllByIds(ids, params);
 			if (null != pList && !pList.isEmpty()) {
 				getList().addAll(pList);
 			}
 		}
 		return SUCCESS;
 	}
 	
 	public String itemDetails() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("id", String.valueOf(getProduct().getId()));
 		params.put("checklist", "productCheckList");
 		setProduct(getProductService().findByMap(params));
 		return SUCCESS;
 	}
 	
 	public String item() {
 		return SUCCESS;
 	}
 	
 	public String itemForUser() {
 		return SUCCESS;
 	}
 	
 	public String listAllForUser() {
 		return SUCCESS;
 	}
 	
 	public String listItemsForUser() {
 		Map<String, String> map = getRequestMap();
 		Map<String, String> params = new HashMap<String, String>();
 		if ("down".equals(map.get("itemaction"))) {
 			params.put("gtDateCreated", MyDateUtils.format(getProduct().getDateCreated()));
 		} else if ("up".equals(map.get("itemaction"))) {
 			params.put("ltDateCreated", MyDateUtils.format(getProduct().getDateCreated()));
 		}
 		params.put("saleType", getProduct().getSaleType());
 		params.put("userCreated", String.valueOf(getUserId()));
 		setList(getProductService().findAllByMapForUser(params));
 		return SUCCESS;
 	}
 	
 	public String update() {
 		Product pu = getProduct();
 		if (null == pu || null == pu.getId()) {
 			getMapValue().put("msg", "无此记录.");
 			return ERROR;
 		}
 		Product entity = findById(pu.getId());
 		if (Product.CheckStatus.PASSED.getValue().equals(entity.getCheckStatus())) {
 			getMapValue().put("msg", "已经通过审核的信息不能进行更新操作");
 			return ERROR;
 		}
 		MyBeanUtils.copyProperties(pu, entity, Product.class, new String[] {
 			"ids"
 		}, new CopyRules() {
 			@Override
 			public boolean myCopyRules(Object value) {
 				return (null != value);
 			}
 		});
 		List<ImageUploaded> imageUploadeds = super.createImageByParams(getImageUploadPath(), ImageUploaded.RefType.PRODUCT);
 		entity = getProductService().update(entity, imageUploadeds, pu.getIds());
 		setProduct(entity);
 		return SUCCESS;
 	}
 	
 	public String updateItem() {
 		Long id = getProduct().getId();
 		if (null != id) {
 			Product p = findById(id);
 			if (null == p || null == p.getId()) {
 				getMapValue().put("msg", "无此记录.");
 				return ERROR;
 			}
 			if (Product.CheckStatus.PASSED.getValue().equals(p.getCheckStatus())) {
 				getMapValue().put("msg", "已经通过审核的信息不能进行更新操作");
 				return ERROR;
 			}
 			setProduct(p);
 			return SUCCESS;
 		}
 		getMapValue().put("msg", "缺失数据标识.");
 		return ERROR;
 	}
 	
 	protected Product findById(Long id) {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("id", String.valueOf(id));
 		return getProductService().findByMap(params);
 	}
 	
 	public List<Product> getList() {
 		return list;
 	}
 
 	public void setList(List<Product> list) {
 		this.list = list;
 	}
 
 	public ProductService getProductService() {
 		return productService;
 	}
 
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 
 	public SubscribeService getSubscribeService() {
 		return subscribeService;
 	}
 
 	public void setSubscribeService(SubscribeService subscribeService) {
 		this.subscribeService = subscribeService;
 	}
 
 	public ProductGroupService getProductGroupService() {
 		return productGroupService;
 	}
 
 	public void setProductGroupService(ProductGroupService productGroupService) {
 		this.productGroupService = productGroupService;
 	}
 
 	public ProductGroup getProductGroup() {
 		return productGroup;
 	}
 
 	public OverheadService getOverheadService() {
 		return overheadService;
 	}
 
 	public void setOverheadService(OverheadService overheadService) {
 		this.overheadService = overheadService;
 	}
 
 	public void setProductGroup(ProductGroup productGroup) {
 		this.productGroup = productGroup;
 	}
 
 	public Overhead getOverhead() {
 		return overhead;
 	}
 
 	public void setOverhead(Overhead overhead) {
 		this.overhead = overhead;
 	}
 
 	public Product getProduct() {
 		return product;
 	}
 
 	public void setProduct(Product product) {
 		this.product = product;
 	}
 
 	public String getImageUploadPath() {
 		return imageUploadPath;
 	}
 
 	public void setImageUploadPath(String imageUploadPath) {
 		this.imageUploadPath = imageUploadPath;
 	}
 
 }
