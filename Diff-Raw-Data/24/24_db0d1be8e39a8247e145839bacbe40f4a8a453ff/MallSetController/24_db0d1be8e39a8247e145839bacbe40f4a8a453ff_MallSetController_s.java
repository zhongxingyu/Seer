 /**
  * 
  */
 package net.chat.controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.chat.constants.PageConstants;
 import net.chat.domain.WxAccount;
 import net.chat.domain.mall.WxMall;
 import net.chat.domain.mall.WxPrdtSubCategory;
 import net.chat.domain.mall.WxProduct;
 import net.chat.domain.mall.WxProductCategory;
 import net.chat.domain.mall.WxProductPic;
 import net.chat.domain.mall.WxProductPrice;
 import net.chat.formbean.MallProductForm;
 import net.chat.formbean.SimpleBean;
 import net.chat.service.AccountService;
 import net.chat.service.mall.MallProductService;
 import net.chat.service.mall.MallService;
 import net.chat.utils.AppContext;
 
 import org.apache.commons.io.FileUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.data.domain.Page;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.CollectionUtils;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 /**
  * @author bo.chen
  * 
  */
 @Controller
 @RequestMapping("/mallset")
 public class MallSetController {
 
 	@Autowired
 	private MallService mallService;
 
 	@Autowired
 	AccountService accountService;
 
 	@Autowired
 	MallProductService productService;
 
 	@RequestMapping("/mall")
 	public String mall(
 			@RequestParam(value = "accountId", required = false) Long accountId,
 			Model model) {
 		Long userId = AppContext.getUserId();
 		List<WxAccount> accounts = accountService.findAccountByUserId(userId);
 		model.addAttribute("accounts", accounts);
 		if (CollectionUtils.isEmpty(accounts))
 			return "redirect:/account/add";
 		if (null == accountId)
 			accountId = accounts.get(0).getId();
 
 		WxMall wxmall = mallService.findMallByAccountId(accountId);
 		if (null == wxmall) {
 			wxmall = new WxMall();
 			wxmall.setAccountId(accountId);
 		}
 		model.addAttribute("wxmall", wxmall);
 
 		return PageConstants.PAGE_MALL_SET;
 	}
 
 	@RequestMapping("/savemall")
 	public String saveMall(WxMall wxmall,
 			@RequestParam(required = false) MultipartFile imageFile,
 			Model model, HttpServletRequest req) throws IOException {
 		wxmall = mallService.saveMall(wxmall);
 		if (null != imageFile && !imageFile.isEmpty()) {
 			long mallId = wxmall.getId();
 			String realpath = req.getSession().getServletContext()
 					.getRealPath("/mallimg/images/");
 			String suffix = imageFile.getOriginalFilename().substring(
 					imageFile.getOriginalFilename().lastIndexOf("."));
 			String imageUrl = mallId + File.separator + UUID.randomUUID()
 					+ suffix;
 			File file = new File(realpath + File.separator + mallId);
 			if (!file.exists())
 				file.mkdir();
 			FileUtils.copyInputStreamToFile(imageFile.getInputStream(),
 					new File(realpath + File.separator + imageUrl));
 			wxmall.setPicUrl(imageUrl);
 			mallService.saveMall(wxmall);
 		}
 
 		return "redirect:/mallset/init?accountId=" + wxmall.getAccountId();
 	}
 
 	@InitBinder
 	protected void initBinder(HttpServletRequest request,
 			ServletRequestDataBinder binder) throws Exception {
 		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		CustomDateEditor dateEditor = new CustomDateEditor(format, true);
 		binder.registerCustomEditor(Date.class, dateEditor);
 
 	}
 
 	@RequestMapping("/category")
 	public String category(
 			@RequestParam(value = "mallId", required = false) Long mallId,
 			Model model) {
 		Long userId = AppContext.getUserId();
 		List<WxMall> malls = mallService.findMallByUserId(userId);
 		model.addAttribute("malls", malls);
 		if (CollectionUtils.isEmpty(malls))
 			return "redirect:/mallset/mall";
 
 		if (null == mallId)
 			mallId = malls.get(0).getId();
 
 		List<WxProductCategory> categorys = mallService
 				.findProductCategoryByMallId(mallId);
 		model.addAttribute("categorys", categorys);
 
 		List<SimpleBean> styles = PageConstants.buildCategoryStyleList();
 		model.addAttribute("styles", styles);
 
 		return PageConstants.PAGE_MALL_CATE;
 	}
 
 	@RequestMapping("/categorydetail/{mallId}")
 	public String categoryDetail(@PathVariable("mallId") Long mallId,
 			@RequestParam(value = "cateId", required = false) Long cateId,
 			Model model) {
 		List<SimpleBean> styles = PageConstants.buildCategoryStyleList();
 		model.addAttribute("styles", styles);
 		WxProductCategory category = new WxProductCategory();
 		category.setMallId(mallId);
 		if (null != cateId) {
 			category = mallService.findWxProductCategoryById(cateId);
 		}
 		model.addAttribute("category", category);
 		return PageConstants.PAGE_MALL_CATEDETAIL;
 	}
 
 	@ResponseBody
 	@RequestMapping("/savecategory")
 	public long saveCategory(WxProductCategory category, Model model) {
 		mallService.save(category);
 		return category.getMallId();
 	}
 
 	@RequestMapping("/subcategory")
 	public String subcategory(
 			@RequestParam(value = "page", defaultValue = "1") int pageNo,
 			@RequestParam(value = "mallId", required = false) Long mallId,
 			@RequestParam(value = "categoryId", required = false) Long categoryId,
 			Model model) {
 
 		Long userId = AppContext.getUserId();
 		List<WxMall> malls = mallService.findMallByUserId(userId);
 		model.addAttribute("malls", malls);
 		if (CollectionUtils.isEmpty(malls))
 			return "redirect:/mallset/mall";
 
 		if (null == mallId)
 			mallId = malls.get(0).getId();
 
 		List<WxProductCategory> categorys = mallService
 				.findProductCategoryByMallId(mallId);
 		model.addAttribute("categorys", categorys);
 		model.addAttribute("mallId", mallId);
 		Page<WxPrdtSubCategory> subcategorys;
 		if (categoryId == null || categoryId == 1) {
 			subcategorys = mallService.findAllSubCategory(categorys, pageNo);
 		} else {
 			subcategorys = mallService.findSubCategoryByCategoryId(categoryId,
 					pageNo);
 			model.addAttribute("categoryId", categoryId);
 		}
 
 		model.addAttribute("subcategorys", subcategorys);
 		return PageConstants.PAGE_MALL_SUB_CATE;
 
 	}
 
 	@RequestMapping("/subcategorydetail/{categoryId}")
 	public String subcategorydetail(
 			@PathVariable("categoryId") Long categoryId,
 			@RequestParam(value = "subcateId", required = false) Long subcateId,
 			Model model) {
 		WxProductCategory category = mallService
 				.findWxProductCategoryById(categoryId);
 		List<WxProductCategory> categorys = mallService
 				.findProductCategoryByMallId(category.getMallId());
 		model.addAttribute("categorys", categorys);
 		WxPrdtSubCategory subcategory = new WxPrdtSubCategory();
 		subcategory.setCategoryId(categoryId);
 		if (null != subcateId) {
 			subcategory = mallService
 					.findPrdtSubCategoryBySubCategoryId(subcateId);
 		}
 		model.addAttribute("subcategory", subcategory);
 		return PageConstants.PAGE_MALL_SUB_CATEDETAIL;
 	}
 
 	@RequestMapping("/deletesubcategory/{subcateId}")
 	public String deletesubcategory(@PathVariable("subcateId") Long subcateId,
 			Model model, HttpServletRequest req) {
 		WxPrdtSubCategory subcategory = mallService
 				.findPrdtSubCategoryBySubCategoryId(subcateId);
 		WxProductCategory category = mallService
 				.findWxProductCategoryById(subcategory.getCategoryId());
 		String oldPath = req.getSession().getServletContext()
 				.getRealPath("/mallimg/images" + subcategory.getPicUrl());
 		File oldSubCategoryFile = new File(oldPath);
 		oldSubCategoryFile.delete();
 		mallService.deleteSubCategory(subcateId);
 		return "redirect:/mallset/subcategory?mallId=" + category.getMallId()
 				+ "&categoryId=" + category.getId();
 	}
 
 	@RequestMapping("/submitSubCategory")
 	public String saveSubCategory(WxPrdtSubCategory subCategory,
 			@RequestParam(required = false) MultipartFile imageFile,
 			Model model, HttpServletRequest req) throws IOException {
 		WxProductCategory category = mallService
 				.findWxProductCategoryById(subCategory.getCategoryId());
 		if (null != imageFile && !imageFile.isEmpty()) {
 			String realpath = req.getSession().getServletContext()
 					.getRealPath("/mallimg/images/" + category.getMallId());
 			String suffix = imageFile.getOriginalFilename().substring(
 					imageFile.getOriginalFilename().lastIndexOf("."));
 			DateFormat format = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
 			String imageUrl = format.format(new Date()) + UUID.randomUUID()
 					+ suffix;
 			FileUtils.copyInputStreamToFile(imageFile.getInputStream(),
 					new File(realpath + File.separator + imageUrl));
 			if (subCategory.getId() == null) {
 				subCategory.setPicUrl(File.separator + category.getMallId()
 						+ File.separator + imageUrl);
 				mallService.save(subCategory);
 			} else {
 				String oldPath = req
 						.getSession()
 						.getServletContext()
 						.getRealPath(
 								"/mallimg/images" + subCategory.getPicUrl());
 				File oldSubCategoryFile = new File(oldPath);
 				oldSubCategoryFile.delete();
 				subCategory.setPicUrl(File.separator + category.getMallId()
 						+ File.separator + imageUrl);
 				mallService.editSubCategory(subCategory);
 			}
 		}
 		return "redirect:/mallset/subcategory?mallId=" + category.getMallId()
 				+ "&categoryId=" + subCategory.getCategoryId();
 	}
 
 	@RequestMapping("/product")
 	public String product(
 			@RequestParam(value = "page", defaultValue = "1") int pageNo,
 			@RequestParam(value = "mallId", required = false) Long mallId,
 			@RequestParam(value = "categoryId", required = false) Long categoryId,
 			@RequestParam(value = "subcategoryId", required = false) Long subcategoryId,
 			Model model) {
 		Long userId = AppContext.getUserId();
 		List<WxMall> malls = mallService.findMallByUserId(userId);
 		model.addAttribute("malls", malls);
 		if (CollectionUtils.isEmpty(malls))
 			return "redirect:/mallset/mall";
 
 		if (null == mallId)
 			mallId = malls.get(0).getId();
 
 		List<WxProductCategory> categorys = mallService
 				.findProductCategoryByMallId(mallId);
 		model.addAttribute("categorys", categorys);
 		model.addAttribute("mallId", mallId);
 		if (null == categoryId) {
 			categoryId = categorys.get(0).getId();
 		}
 		List<WxPrdtSubCategory> subcategorys = mallService
 				.findSubCategoryByCategoryId(categoryId, 1).getContent();
 		model.addAttribute("subcategorys", subcategorys);
 		model.addAttribute("categoryId", categoryId);
 		if (null == subcategoryId) {
 			subcategoryId = subcategorys.get(0).getId();
 		}
 		model.addAttribute("subcategoryId", subcategoryId);
 		Page<MallProductForm> productForm = productService
 				.findAllProductBySubcategory(subcategoryId, pageNo);
 		model.addAttribute("productForms", productForm);
 		return PageConstants.PAGE_MALL_PRODUCT;
 	}
 
 	@RequestMapping("/productdetail")
 	public String productdetail(
 			@RequestParam(value = "mallId", required = false) Long mallId,
 			@RequestParam(value = "productId", required = false) Long productId,
 			Model model) {
 		Long userId = AppContext.getUserId();
 		List<WxMall> malls = mallService.findMallByUserId(userId);
 		model.addAttribute("malls", malls);
 		if (CollectionUtils.isEmpty(malls))
 			return "redirect:/mallset/mall";
 		if (null == mallId)
 			mallId = malls.get(0).getId();
 		model.addAttribute("mallId", mallId);
 		List<WxPrdtSubCategory> subcategorys = mallService.findAllSubCategory();
 		model.addAttribute("subcategorys", subcategorys);
 		WxProduct product = new WxProduct();
 		product.setMallId(mallId);
 		if (null != productId) {
 			product = mallService.findProductByProductId(productId);
 
 		}
 		model.addAttribute("product", product);
 		return PageConstants.PAGE_MALL_PRODUCT_DETAIL;
 	}
 
 	@RequestMapping("/submitProduct")
 	public String submitProduct(
 			@RequestParam(value = "listSubcategory", required = false) List<Long> listSubcategory,
 			WxProduct product,
 			@RequestParam(required = false) MultipartFile imageFile,
 			Model model, HttpServletRequest req) throws IOException {
 		Long productId = null;
 		if (product.getId() != null) {
 			productId = productService.editProduct(product, listSubcategory,
 					imageFile);
 		} else {
 			productId = productService.saveProduct(product, listSubcategory,
 					imageFile);
 		}
 
 		return "redirect:/mallset/productExtention/" + productId;
 	}
 
 	@RequestMapping("/productExtention/{productId}")
 	public String productExtention(@PathVariable("productId") Long productId,
 			Model model) {
 		Long mallId = mallService.findProductByProductId(productId).getMallId();
 		List<WxProductPrice> priceList = productService
 				.findProductPrice(productId);
 		List<WxProductPic> pics = productService.findExtentionPic(productId);
 		WxProductPrice price = null;
 		if (!priceList.isEmpty()) {
 			price = priceList.get(0);
 		} else {
 			price = new WxProductPrice();
 			price.setProductId(productId);
 		}
 		model.addAttribute("mallId", mallId);
 		model.addAttribute("pics", pics);
 		model.addAttribute("price", price);
 		return PageConstants.PAGE_MALL_PRODUCT_EXTENTION;
 	}
 
 	@RequestMapping("/submitproductExtention")
 	public String submitproductExtention(
 			@RequestParam(required = false) MultipartFile imageFile,
 			@RequestParam(value = "defaultPic", required = false) Long defaultPic,
 			WxProductPrice price, Model model) throws IOException {
 		if (price != null && price.getSalePrice() != null)
 			productService.saveProductPrice(price);
 		if (imageFile != null && imageFile.getSize() != 0)
 			productService.saveProductPic(imageFile, price.getProductId());
 		if (defaultPic != null)
 			productService.setProductPicDefault(defaultPic);
 		return "redirect:/mallset/product";
 	}
 }
