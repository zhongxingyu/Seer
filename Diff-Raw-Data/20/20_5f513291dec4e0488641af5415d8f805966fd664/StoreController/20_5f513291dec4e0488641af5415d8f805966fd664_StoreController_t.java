 package com.acme.fitness.webmvc.controllers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.acme.fitness.domain.exceptions.FitnessDaoException;
 import com.acme.fitness.domain.orders.Store;
 import com.acme.fitness.domain.products.MembershipType;
 import com.acme.fitness.domain.products.Product;
 import com.acme.fitness.domain.products.ProductImage;
 import com.acme.fitness.domain.products.TrainingType;
 import com.acme.fitness.domain.users.User;
 import com.acme.fitness.orders.GeneralOrdersService;
 import com.acme.fitness.products.GeneralProductsService;
 import com.acme.fitness.users.GeneralUsersService;
 
 @Controller
 @RequestMapping(value = "/raktar")
 public class StoreController {
 	private static final Logger logger = LoggerFactory.getLogger(StoreController.class);
 
 	private GeneralProductsService gps;
 	private GeneralOrdersService gos;
 	private GeneralUsersService gus;
 
 	@Autowired
 	public StoreController(GeneralProductsService gps, GeneralOrdersService gos, GeneralUsersService gus) {
 		this.gps = gps;
 		this.gos = gos;
 		this.gus = gus;
 	}
 
 	@RequestMapping(value = "")
 	public String defaultPage() {
 		return "redirect:/raktar/termek";
 	}
 
 	@RequestMapping(value = "termek", method = RequestMethod.GET)
 	public String products(Locale locale, Model model, HttpServletResponse response, HttpServletRequest request) {
 		if (gos.getAllStores().size() != gps.getAllProduct().size()) {
 			for (Product p : gps.getAllProduct()) {
 				gos.addProductToStore(p, 0);
 			}
 		}
 		List<Store> store = new ArrayList<Store>(gos.getAllStores());
 		model.addAttribute("productsInStore", store);
 		return "raktar";
 	}
 
 	@RequestMapping(value = "termek/ujmennyiseg", method = RequestMethod.POST)
 	public String productPlusQuantity(Locale locale, Model model, HttpServletResponse response, HttpServletRequest request, @ModelAttribute("productId") long id,
 			@ModelAttribute("quantity") int quantity) {
 		try {
 			gos.putInProduct(gps.getProductById(id), quantity);
 		} catch (FitnessDaoException e) {
 			e.printStackTrace();
 		}
 		return "redirect:/raktar";
 	}
 
 	@RequestMapping(value = "termek/ujtermek", method = RequestMethod.POST)
 	public String newProduct(Locale locale, Model model, HttpServletResponse response, HttpServletRequest request, @ModelAttribute("product") Product product,
 			@RequestParam("file") MultipartFile file) {
 		ProductImage image = createPictureFromMultipartFile(file);
 
 		Product newProduct = gps.addProduct(product.getName(), product.getDetails(), product.getPrice(), product.getManufacturer(), new Date(), image);
 		logger.info("new product added: " + newProduct);
 
 		return "redirect:/raktar";
 	}
 
 	@RequestMapping(value = "termek/termekmodositas", method = RequestMethod.POST)
 	public String updateProduct(Locale locale, Model model, HttpServletResponse response, HttpServletRequest request, @ModelAttribute("product") Product product,
 			@RequestParam("file") MultipartFile file) {
 		ProductImage image = createPictureFromMultipartFile(file);
 		try {
 			Product existingProduct = gps.getProductById(Long.parseLong(request.getParameter("productId")));
 			if (isImageChanged(image, existingProduct, product)) {
 				gps.updateProductAndSaveImage(existingProduct, image);
 			} else {
 				gps.updateProduct(existingProduct);
 			}
 		} catch (FitnessDaoException e) {
 			e.printStackTrace();
 		}
 		return "redirect:/raktar";
 	}
 
 
 
 	@RequestMapping(value = "termek/torles/{productId}", method = RequestMethod.GET)
 	public String deleteProduct(Locale locale, Model model, HttpServletResponse response, HttpServletRequest request, @PathVariable("productId") long productId) {
 		try {
 			gps.deleteProduct(gps.getProductById(productId));
 		} catch (FitnessDaoException e) {
 			e.printStackTrace();
 		}
 		return "redirect:/raktar";
 	}
 
 	@RequestMapping(value = "berlet")
 	public String membership(Model model, HttpServletResponse response, HttpServletRequest request) {
 		model.addAttribute("membershipsInStore", gps.getAllMembershipTypes());
 		return "raktar";
 	}
 
 	@RequestMapping(value = "berlet/ujberlet", method = RequestMethod.POST)
 	public String newMembership(Locale locale, Model model, HttpServletResponse response, HttpServletRequest request, @ModelAttribute("membership") MembershipType membershipType) {
 		gps.saveMembershipType(membershipType);
 		logger.info("new membership type added: " + membershipType);
 		return "redirect:/raktar/berlet";
 	}
 
 	@RequestMapping(value = "berlet/valtoztat", method = RequestMethod.POST)
 	public String updateMembership(Locale locale, Model model, HttpServletResponse response, HttpServletRequest request, @ModelAttribute("membership") MembershipType membershipType) {
 		gps.updateMembershipType(membershipType);
 		logger.info("membership updated: " + membershipType);
 		return "redirect:/raktar/berlet";
 	}
 
 	@RequestMapping(value = "berlet/torles/{membershipId}", method = RequestMethod.GET)
 	public String deleteMembershipType(@PathVariable("membershipId") long membershipId) {
 		try {
 			gps.deleteMembershipType(gps.getMembershipTypeById(membershipId));
 			logger.info("membership deleted with id: " + membershipId);
 		} catch (FitnessDaoException e) {
 			e.printStackTrace();
 		}
 		return "redirect:/raktar/berlet";
 	}
 
 	@RequestMapping(value = "edzestipus")
 	public String trainingType(Model model, HttpServletResponse response, HttpServletRequest request) {
 		model.addAttribute("trainingTypesInStore", gps.getAllTrainingTypes());
 		model.addAttribute("trainers", gus.getAllTrainers());
 		return "raktar";
 	}
 
 	@RequestMapping(value = "edzestipus/ujedzestipus", method = RequestMethod.POST)
 	public String newTrainingType(HttpServletResponse response, HttpServletRequest request, @ModelAttribute("trainingType") TrainingType trainingType) {
 		try {
 			User trainer = gus.getUserById(Long.parseLong(request.getParameter("trainerId")));
 			try {
 				TrainingType existingTT = gps.getTrainingTypeByTrainer(trainer);
 				existingTT.setDetail(trainingType.getDetail());
 				existingTT.setPrice(trainingType.getPrice());
 				gps.updateTrainingType(existingTT);
 				logger.info("membership is updated: " + existingTT);
 			} catch (FitnessDaoException e) {
 				trainingType.setTrainer(trainer);
 				gps.saveTrainingType(trainingType);
 				logger.info("new membership type added: " + trainingType);
 			}
 		} catch (FitnessDaoException e) {
 			e.printStackTrace();
 		}
 		return "redirect:/raktar/edzestipus";
 	}
 	
 	private boolean isImageChanged(ProductImage image, Product existingProduct, Product product) {
 		updateProductFields(product, existingProduct);
 		boolean isImageChanged = false;
 		if (image != null) {
			if(existingProduct.getProductImage() == null || !areImagesEqual(image, existingProduct)) {
 				isImageChanged = true;
 				existingProduct.setProductImage(image);
 			}
 		}
 		return isImageChanged;
 	}
 	
 	private boolean areImagesEqual(ProductImage image, Product existingProduct) {
 		return Arrays.equals(image.getImage(), existingProduct.getProductImage().getImage()) && image.getMime().equals(existingProduct.getProductImage().getMime());
 	}
 
 	private void updateProductFields(Product product, Product existingProduct) {
 		existingProduct.setName(product.getName());
 		existingProduct.setDetails(product.getDetails());
 		existingProduct.setManufacturer(product.getManufacturer());
 		existingProduct.setPrice(product.getPrice());
 	}
 
 	private ProductImage createPictureFromMultipartFile(MultipartFile file) {
 		ProductImage pi = null;
 
 		if (!file.isEmpty()) {
 			try {
 				byte[] bytes = file.getBytes();
 				String originalFileName = file.getOriginalFilename();
 				String mime = getMimeFromFileName(originalFileName);
 				logger.info("picutres uploaded: " + originalFileName + " mime: " + mime);
 				pi = new ProductImage(mime, bytes);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return pi;
 	}
 
 	private String getMimeFromFileName(String originalFileName) {
 		String[] helper = originalFileName.split("\\.");
 		String mime = "";
 		if (helper.length > 0) {
 			mime = helper[1].toLowerCase();
 		}
 		return mime;
 	}
 }
