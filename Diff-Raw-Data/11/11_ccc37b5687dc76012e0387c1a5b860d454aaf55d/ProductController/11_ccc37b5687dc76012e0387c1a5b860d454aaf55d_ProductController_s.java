 package com.euroit.militaryshop.web.controller.admin;
 
 import com.euroit.militaryshop.adapter.MediaAdapter;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.euroit.militaryshop.enums.DictionaryName;
 import com.euroit.militaryshop.service.CategoryService;
 import com.euroit.militaryshop.service.DictionaryEntryService;
 import com.euroit.militaryshop.service.ItemService;
 import com.euroit.militaryshop.service.ProductService;
 import com.euroit.militaryshop.web.controller.admin.form.ItemsFilterForm;
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.support.MutableSortDefinition;
 import org.springframework.beans.support.PagedListHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 @RequestMapping("/admin/product")
 public class ProductController extends BaseAdminController {
 	private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
 
 	private static final int ITEMS_PAGE_SIZE = 5;
 
     ProductService productService;
     BlobstoreService blobstoreService;
     CategoryService categoryService;
     MediaAdapter mediaAdapter;
     ItemService itemService;
     private DictionaryEntryService dictionaryEntryService;
 
     @Autowired
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 
 	@Autowired
 	public void setBlobstoreService(BlobstoreService blobstoreService) {
 		this.blobstoreService = blobstoreService;
 	}
 
 	@Autowired
 	public void setCategoryService(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 
 	@Autowired
 	public void setMediaAdapter(MediaAdapter mediaAdapter) {
 		this.mediaAdapter = mediaAdapter;
 	}
 
 	@Autowired
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 
 	@Autowired
     public void setDictionaryEntryService(DictionaryEntryService dictionaryEntryService) {
         this.dictionaryEntryService = dictionaryEntryService;
     }
 
     @RequestMapping(value = "{productId}", method = RequestMethod.GET)
 	public ModelAndView productInfo(ItemsFilterForm filterForm, @PathVariable("productId") long productId,
 	        ProductDto validatedProduct,
 			@RequestParam(defaultValue = "0", required = false) int itemsPage, 
 			@RequestParam(required = false, value = "sort", defaultValue = "") String sortProperty,
 			@RequestParam(required = false, value = "asc", defaultValue = "true") boolean asc) {
 		
 		ModelAndView mav = new ModelAndView("admin.product.edit");
 		
 		ProductDto productDto = validatedProduct;
 		
 		if (validatedProduct == null || validatedProduct.getId() == 0) {
 		    productDto = productService.getProductById(productId);
 		}
 		
 		if (productDto.getDefaultItemId() != 0) {
             MilitaryShopItemDto defaultItem = itemService.getItemById(productDto.getDefaultItemId());
             productDto.setSelectedItem(defaultItem);                
         }
 		
 		mav.addObject(productDto);
 		mav.addObject("allCategories", categoryService.getAllCategories());
         mav.addObject("colorDictEntries", dictionaryEntryService.getEntriesByDictName(DictionaryName.COLOR));
         mav.addObject("materialDictEntries", dictionaryEntryService.getEntriesByDictName(DictionaryName.MATERIAL));
         mav.addObject("sizeDictEntries", dictionaryEntryService.getEntriesByDictName(DictionaryName.SIZE));
         mav.addObject("filterForm", filterForm);
 		final PagedListHolder<Object> itemsPagination 
             = createItemsPaginationAndSort(productId, filterForm, 
                     itemsPage, sortProperty, asc);
 		mav.addObject("items", itemService.getItemsForProduct(productId, 
 		        filterForm.getColorFilter(), filterForm.getMaterialFilter(), filterForm.getSizeFilter(), 
 		        itemsPagination.getSort(), itemsPage, ITEMS_PAGE_SIZE));
         mav.addObject("itemsPagination", itemsPagination);
 		
 		return mav;
 	}
 
 	private PagedListHolder<Object> createItemsPaginationAndSort(long productId, ItemsFilterForm filterForm, int itemsPage, String sortProperty, boolean asc) {
 		//unsafe but should contain casting to int
 		final PagedListHolder<Object> pagedListHolder = new PagedListHolder<Object>(
 				Collections.nCopies((int)itemService.countItemsForProduct(productId, filterForm.getColorFilter(), 
 				        filterForm.getMaterialFilter(), filterForm.getSizeFilter()), 
 				        new Object()));
		
 		pagedListHolder.setPage(itemsPage);
 		pagedListHolder.setPageSize(ITEMS_PAGE_SIZE);
 				
 		MutableSortDefinition sort = new MutableSortDefinition();
 		switch (sortProperty) {
 		    case "color":
 		    case "material":
 		    case "size":
             case "shortName":
 		        sort.setProperty(sortProperty);
 		        sort.setAscending(asc);
 		        break;
 		    default:
 		        break;
 		}
 		
         pagedListHolder.setSort(sort);
 		return pagedListHolder;
 	}
 
 	@RequestMapping(value = "save", method = RequestMethod.POST)
 	public ModelAndView saveProduct(@Valid ProductDto productDto, BindingResult bindingResult) {
 		ModelAndView mav;
 
         if (bindingResult.hasErrors()) {
 
             if (productDto.getId() != 0) {
                 return productInfo(new ItemsFilterForm(), productDto.getId(), (ProductDto)bindingResult.getTarget(), 0, "", true);
             } else {
                 mav = new ModelAndView("admin.product.add");
             }
 
             mav.addObject("allCategories", categoryService.getAllCategories());
             mav.addObject(bindingResult.getTarget());
 
             return mav;
         }
 		
 		long productId = productService.createOrSave(productDto);
         mav = new ModelAndView(String.format("redirect:/admin/product/%s", productId));
 
 		return mav;
 	}
 	
 	@RequestMapping(value = "list", method = RequestMethod.GET)
 	public ModelAndView allProducts() {
 		ModelAndView mav = new ModelAndView("admin.product.list");
 		
 		List<ProductDto> productList = productService.getAllProducts();
 		mav.addObject("products", productList);
 		
 		return mav;
 	}
 	
 	@Deprecated
 	@RequestMapping(value = "upload/bigImage", method = RequestMethod.POST)
 	public String uploadProductBigImage(HttpServletRequest request, ProductDto productDto) {
 		Map<String, List<BlobKey>> map = blobstoreService.getUploads(request);
 		BlobKey blobKey = map.get("bigImage").get(0);
 		mediaAdapter.updateMedia(blobKey, productDto.getBigImageName());
 				
 		return String.format("redirect:/admin/product/%s", productDto.getId());
 	}
 	
 	@Deprecated
 	@RequestMapping(value = "upload/smallImage", method = RequestMethod.POST)
 	public String uploadProductSmallImage(HttpServletRequest request, ProductDto productDto) {
 		Map<String, List<BlobKey>> map = blobstoreService.getUploads(request);
 		BlobKey blobKey = map.get("smallImage").get(0);
 		mediaAdapter.updateMedia(blobKey, productDto.getSmallImageName());
 				
 		return String.format("redirect:/admin/product/%s", productDto.getId());
 	}
 	
 	@RequestMapping(value = "add", method = RequestMethod.GET)
 	public ModelAndView addProduct() {
 		ModelAndView mav = new ModelAndView("admin.product.add");
 		mav.addObject(new ProductDto());
 		mav.addObject("allCategories", categoryService.getAllCategories());
 		return mav;
 	}
 	
 	@RequestMapping(value="set/default/item", method = RequestMethod.POST)
 	public ModelAndView setDefaultItem(ProductDto productDto) {
 		ModelAndView mav = new ModelAndView(String.format("redirect:/admin/product/%s#items", productDto.getId()));
 		productService.updateDefaultItem(productDto.getId(), productDto.getDefaultItemId());
 		return mav;
 	}
 }
