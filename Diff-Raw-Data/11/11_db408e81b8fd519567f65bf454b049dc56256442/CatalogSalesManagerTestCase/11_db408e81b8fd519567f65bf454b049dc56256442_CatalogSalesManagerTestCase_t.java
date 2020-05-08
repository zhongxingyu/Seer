 package com.salesmanager.test.catalog;
 
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.salesmanager.core.business.catalog.category.model.Category;
 import com.salesmanager.core.business.catalog.category.model.CategoryDescription;
 import com.salesmanager.core.business.catalog.product.model.Product;
 import com.salesmanager.core.business.catalog.product.model.attribute.ProductAttribute;
 import com.salesmanager.core.business.catalog.product.model.attribute.ProductOption;
 import com.salesmanager.core.business.catalog.product.model.attribute.ProductOptionDescription;
 import com.salesmanager.core.business.catalog.product.model.attribute.ProductOptionValue;
 import com.salesmanager.core.business.catalog.product.model.attribute.ProductOptionValueDescription;
 import com.salesmanager.core.business.catalog.product.model.availability.ProductAvailability;
 import com.salesmanager.core.business.catalog.product.model.description.ProductDescription;
 import com.salesmanager.core.business.catalog.product.model.image.ProductImage;
 import com.salesmanager.core.business.catalog.product.model.manufacturer.Manufacturer;
 import com.salesmanager.core.business.catalog.product.model.price.ProductPrice;
 import com.salesmanager.core.business.catalog.product.model.price.ProductPriceDescription;
 import com.salesmanager.core.business.catalog.product.model.type.ProductType;
 import com.salesmanager.core.business.generic.exception.ServiceException;
 import com.salesmanager.core.business.merchant.model.MerchantStore;
 import com.salesmanager.core.business.merchant.model.StoreBranding;
 import com.salesmanager.core.business.reference.country.model.Country;
 import com.salesmanager.core.business.reference.country.model.CountryDescription;
 import com.salesmanager.core.business.reference.currency.model.Currency;
 import com.salesmanager.core.business.reference.language.model.Language;
 import com.salesmanager.test.core.AbstractSalesManagerCoreTestCase;
 
 public class CatalogSalesManagerTestCase extends AbstractSalesManagerCoreTestCase {
 	
 	private static final Date date = new Date(System.currentTimeMillis());
 
 	@Test
 	public void createProduct() throws ServiceException {
 		Language DEFAULT_LANGUAGE = languageService.getByCode("en");
 		Language fr = new Language();
 		fr.setCode("fr");
 		languageService.create(fr);
 		Language FRENCH = fr;
 		
 		//create country
 		Country ca = new Country();
 		ca.setIsoCode("CA");
 		
 		CountryDescription caden = new CountryDescription();
 		caden.setCountry(ca);
 		caden.setLanguage(DEFAULT_LANGUAGE);
 		caden.setName("Canada");
 		caden.setDescription("Canada Country");
 		
 		CountryDescription cadfr = new CountryDescription();
 		cadfr.setCountry(ca);
 		cadfr.setLanguage(FRENCH);
 		cadfr.setName("Canada");
 		cadfr.setDescription("Pays Canada");
 		
 		List<CountryDescription> descriptionsca = new ArrayList<CountryDescription>();
 		descriptionsca.add(caden);
 		descriptionsca.add(cadfr);
 		ca.setDescriptions(descriptionsca);
 		
 		countryService.create(ca);
 		
 		//create a currency
 		Currency currency = new Currency();
 		currency.setCurrency(java.util.Currency.getInstance(Locale.CANADA));
 		currency.setSupported(true);
 		currencyService.create(currency);
 		
 		//create a merchant
 		MerchantStore store = new MerchantStore();
 		store.setCountry(ca);
 		store.setCurrency(currency);
 		store.setDefaultLanguage(DEFAULT_LANGUAGE);
 		store.setInBusinessSince(date);
 		store.setStorename("store name");
 		
 		merchantService.create(store);
 		
 		//create a Manufacturer
 		Manufacturer manufacturer = new Manufacturer();
 		manufacturer.getStores().add(store);
 		
 		manufacturerService.create(manufacturer);
 		
 		//create a product type
 		ProductType productType = new ProductType();
 		productType.setCode("GENERAL");
 		
 		productTypeService.create(productType);
 		
 		Product product = new Product();
 		product.getAuditSection().setDateCreated(date);
 		product.setProductHeight(new BigDecimal(2));
 		product.setSku("TEST");
 		product.setManufacturer(manufacturer);
 		product.setType(productType);
 		
 		
 		//Product description
 		ProductDescription description = new ProductDescription();
 		description.setName("english description");
 		Language language = languageService.getByCode(ENGLISH_LANGUAGE_CODE);
 		description.setLanguage(language);
 		description.setProduct(product);
 		
 		ProductDescription descriptionfr = new ProductDescription();
 		descriptionfr.setName("french description");
 		descriptionfr.setLanguage(FRENCH);
 		descriptionfr.setProduct(product);
 		
 		Set<ProductDescription> descriptions = new HashSet<ProductDescription>();
 		descriptions.add(description);
 		descriptions.add(descriptionfr);
 		
 		product.setDescriptions(descriptions);
 		
 		productService.create(product);
 		
 		//Availability
 		ProductAvailability availability = new ProductAvailability();
 		availability.setProductDateAvailable(date);
 		availability.setProductQuantity(100);
 		availability.setRegion("*");
 		availability.setProduct(product);
 		
 		productAvailabilityService.create(availability);
 		
 		ProductAvailability availabilityCa = new ProductAvailability();
 		availabilityCa.setProductDateAvailable(date);
 		availabilityCa.setProductQuantity(100);
 		availabilityCa.setRegion("CA");
 		availabilityCa.setProduct(product);
 		
 		productAvailabilityService.create(availabilityCa);
 				
 		
 		//Prices
 		/**
 		 * Contains a Base Price for regions (availability) *
 		 * and a base price for availability canada
 		 * for canada there is also an activation price
 		 */
 		ProductPrice dprice = new ProductPrice();
 		dprice.setDefaultPrice(true);
 		dprice.setProductPriceAmount(new BigDecimal(100));
 		dprice.setProductPriceAvailability(availability);
 
 		ProductPriceDescription dpd = new ProductPriceDescription();
 		dpd.setName("Base price");
 		dpd.setProductPrice(dprice);
 		dpd.setLanguage(DEFAULT_LANGUAGE);
 		
 		ProductPriceDescription dpdfr = new ProductPriceDescription();
 		dpdfr.setName("Prix de base");
 		dpdfr.setProductPrice(dprice);
 		dpdfr.setLanguage(FRENCH);
 		
 		Set<ProductPriceDescription> dpriceSet = new HashSet<ProductPriceDescription>();
 		dpriceSet.add(dpd);
 		dpriceSet.add(dpdfr);
 		
 		dprice.setDescriptions(dpriceSet);
 		
 		//Create price
 		productPriceService.create(dprice);
 		
 		//another price for canada
 		ProductPrice dpriceca = new ProductPrice();
 		dpriceca.setDefaultPrice(true);
 		dpriceca.setProductPriceAmount(new BigDecimal(105));
 		dpriceca.setProductPriceAvailability(availabilityCa);
 
 		ProductPriceDescription dpdcaen = new ProductPriceDescription();
 		dpdcaen.setName("Base price");
 		dpdcaen.setProductPrice(dpriceca);
 		dpdcaen.setLanguage(DEFAULT_LANGUAGE);
 		
 		ProductPriceDescription dpdcafr = new ProductPriceDescription();
 		dpdcafr.setName("Prix de base");
 		dpdcafr.setProductPrice(dpriceca);
 		dpdcafr.setLanguage(FRENCH);
 		
 		Set<ProductPriceDescription> dpricecaSet = new HashSet<ProductPriceDescription>();
 		dpricecaSet.add(dpdcaen);
 		dpricecaSet.add(dpdcafr);
 		
 		dpriceca.setDescriptions(dpricecaSet);
 		
 		productPriceService.create(dpriceca);
 		
 		ProductPrice dpriceca2 = new ProductPrice();
 		dpriceca2.setDefaultPrice(false);
 		dpriceca2.setProductPriceAmount(new BigDecimal(10));
 		dpriceca2.setProductPriceAvailability(availabilityCa);
 
 		ProductPriceDescription dpdcaen2 = new ProductPriceDescription();
 		dpdcaen2.setName("Activation price");
 		dpdcaen2.setProductPrice(dpriceca2);
 		dpdcaen2.setLanguage(DEFAULT_LANGUAGE);
 		
 		ProductPriceDescription dpdcafr2 = new ProductPriceDescription();
 		dpdcafr2.setName("Prix d'activation");
 		dpdcafr2.setProductPrice(dpriceca2);
 		dpdcafr2.setLanguage(FRENCH);
 		
 		Set<ProductPriceDescription> dpriceca2Set = new HashSet<ProductPriceDescription>();
 		dpriceca2Set.add(dpdcaen2);
 		dpriceca2Set.add(dpdcafr2);
 		
 		dpriceca2.setDescriptions(dpriceca2Set);
 		
 		productPriceService.create(dpriceca2);
 
 		
 
 		//2 Images
 		ProductImage imagex = new ProductImage();
 		imagex.setDefaultImage(true);
 		imagex.setImageType(1);
 		imagex.setProductImage("/usr/temp/files/images/testx.gif");
 		imagex.setProduct(product);
 		
 		productImageService.create(imagex);
 		
 		ProductImage imagesmallx = new ProductImage();
 		imagesmallx.setImageType(2);
 		imagesmallx.setProductImage("/usr/temp/files/images/test-cartx.gif");
 		imagesmallx.setProduct(product);
 		
 		productImageService.create(imagesmallx);
 		
 
 		/******* ********/
 		//Product Options 
 		
 		//Color option
 		ProductOption option = new ProductOption();
 		option.setProductOptionSortOrder(0);
 		option.setProductOptionType("OPT");
 		
 		ProductOptionDescription po1 = new ProductOptionDescription();
 		po1.setName("Color");
 		po1.setLanguage(DEFAULT_LANGUAGE);
 		po1.setProductOption(option);
 		
 		ProductOptionDescription po2 = new ProductOptionDescription();
 		po2.setName("Couleur");
 		po2.setLanguage(FRENCH);
 		po2.setProductOption(option);
 		
 		Set<ProductOptionDescription> optSet = new HashSet<ProductOptionDescription>();
 		optSet.add(po1);
 		optSet.add(po2);
 		
 		option.setDescriptions(optSet);
 		
 		option.getStores().add(store);
 		//create option
 		productOptionService.create(option);
 		
 		
 		//Size option
 		ProductOption option2 = new ProductOption();
 		option2.setProductOptionSortOrder(1);
 		option2.setProductOptionType("OPT");
 		
 		ProductOptionDescription po3 = new ProductOptionDescription();
 		po3.setName("Size");
 		po3.setLanguage(DEFAULT_LANGUAGE);
 		po3.setProductOption(option2);
 		
 		ProductOptionDescription po4 = new ProductOptionDescription();
 		po4.setName("Grandeur");
 		po4.setLanguage(FRENCH);
 		po4.setProductOption(option2);
 		
 		Set<ProductOptionDescription> optSet2 = new HashSet<ProductOptionDescription>();
 		optSet2.add(po3);
 		optSet2.add(po4);
 		
 		option2.setDescriptions(optSet2);
 		
 		option2.getStores().add(store);
 		//create option
 		productOptionService.create(option2);
 		
 
 		//options values
 		//values for color
 		//black
 		ProductOptionValue optionValue1 = new ProductOptionValue();
 		optionValue1.setProductOptionValueSortOrder(0);
 		
 		ProductOptionValueDescription optionValueDescription1 = new 
 				ProductOptionValueDescription();
 		optionValueDescription1.setLanguage(DEFAULT_LANGUAGE);
 		optionValueDescription1.setName("Black");
 		optionValueDescription1.setProductOptionValue(optionValue1);
 		
 		ProductOptionValueDescription optionValueDescription2 = new 
 				ProductOptionValueDescription();
 		optionValueDescription2.setLanguage(FRENCH);
 		optionValueDescription2.setName("Noir");
 		optionValueDescription2.setProductOptionValue(optionValue1);
 		
 		Set<ProductOptionValueDescription> optionsValueSet1 = new HashSet<ProductOptionValueDescription>();
 		optionsValueSet1.add(optionValueDescription1);
 		optionsValueSet1.add(optionValueDescription2);
 		optionValue1.setDescriptions(optionsValueSet1);
 		
 		optionValue1.getStores().add(store);
 		
 		//create option value black
 		productOptionValueService.create(optionValue1);
 		
 		//white
 		ProductOptionValue optionValue2 = new ProductOptionValue();
 		optionValue2.setProductOptionValueSortOrder(1);
 		
 		ProductOptionValueDescription optionValueDescription3 = new 
 				ProductOptionValueDescription();
 		optionValueDescription3.setLanguage(DEFAULT_LANGUAGE);
 		optionValueDescription3.setName("White");
 		optionValueDescription3.setProductOptionValue(optionValue2);
 		
 		ProductOptionValueDescription optionValueDescription4 = new 
 				ProductOptionValueDescription();
 		optionValueDescription4.setLanguage(FRENCH);
 		optionValueDescription4.setName("Blanc");
 		optionValueDescription4.setProductOptionValue(optionValue2);
 		
 		Set<ProductOptionValueDescription> optionsValueSet2 = new HashSet<ProductOptionValueDescription>();
 		optionsValueSet2.add(optionValueDescription3);
 		optionsValueSet2.add(optionValueDescription4);
 		optionValue2.setDescriptions(optionsValueSet2);
 		
 		optionValue2.getStores().add(store);
 		
 		//create option value white
 		productOptionValueService.create(optionValue2);
 		
 		ProductOptionValue optionValue3 = new ProductOptionValue();
 		optionValue3.setProductOptionValueSortOrder(0);
 		
 		ProductOptionValueDescription optionValueDescription5 = new 
 				ProductOptionValueDescription();
 		optionValueDescription5.setLanguage(DEFAULT_LANGUAGE);
 		optionValueDescription5.setName("Large");
 		optionValueDescription5.setProductOptionValue(optionValue3);
 		
 		ProductOptionValueDescription optionValueDescription6 = new 
 				ProductOptionValueDescription();
 		optionValueDescription6.setLanguage(FRENCH);
 		optionValueDescription6.setName("Grand");
 		optionValueDescription6.setProductOptionValue(optionValue3);
 		
 		Set<ProductOptionValueDescription> optionsValueSet3 = new HashSet<ProductOptionValueDescription>();
 		optionsValueSet3.add(optionValueDescription5);
 		optionsValueSet3.add(optionValueDescription6);
 		optionValue3.setDescriptions(optionsValueSet3);
 		
 		
 		
 		optionValue3.getStores().add(store);
 		
 		//create option value large
 		productOptionValueService.create(optionValue3);
 		
 		
 		Set<ProductAttribute> attributes = new HashSet<ProductAttribute>();
 		
 		ProductAttribute black = new ProductAttribute();
 		black.setProduct(product);
 		black.setProductOption(option);//color
 		black.setProductOptionValue(optionValue1);//black
 		
 		productAttributeService.create(black);
 		
 		attributes.add(black);
 		
 		ProductAttribute white = new ProductAttribute();
 		white.setProduct(product);
 		white.setProductOption(option);//color
 		white.setProductOptionValue(optionValue2);//white
 		
 		productAttributeService.create(white);
 		
 		attributes.add(white);
 		
 		ProductAttribute large = new ProductAttribute();
 		large.setProduct(product);
 		large.setProductOption(option2);//size
 		large.setProductOptionValue(optionValue3);//large
 		
 		productAttributeService.create(large);
 		
 		attributes.add(large);
 		
 		Assert.assertTrue(productService.count() == 1);
 		
 		System.out.println(product.getSku());
 		System.out.println(product.getAttributes().size());
 		System.out.println(product.getAvailabilities().size());
 		System.out.println(product.getImages().size());
 
 	}
 	
 	@Test
 	public void createMerchant() throws ServiceException {
 	
 	
			//create a merchant
 			Country country = countryService.getByCode("CA");
 			Language lang = languageService.getByCode("en");
 			List<Language> langs = languageService.list();
 			Currency currency = currencyService.getByCode("CAD");
 			
 			
 			MerchantStore store = new MerchantStore();
 			store.setCountry(country);
 			store.setCurrency(currency);
 			store.setDefaultLanguage(lang);
 			store.setInBusinessSince(date);
 			store.setStorename("store name");
 			store.setLanguages(langs);
 			
 			merchantService.create(store);
 
 
 			StoreBranding branding = new StoreBranding();
 			branding.setDomainName("www.branding1.com");
 			branding.setStoreEmailAddress("store@email.com");
 			branding.setMerchantSore(store);
 			
 			storeBrandingService.create(branding);
 			
 			
 			store.setBranding(branding);
 			store.setStorecity("test 2");
 			merchantService.update(store);
 
 			
 			
 			
 			store   = merchantService.getMerchantStore(store.getId());
 			System.out.println(store.getId());
 			System.out.println(store.getBranding().getDomainName());
 			System.out.println(store.getDefaultLanguage().getId());
 			System.out.println(store.getLanguages().size());
 			System.out.println(store.getCurrency().getCode());
 			
 			
 	}
 
 	
 	@Test
 	public void createCategoryDescription() throws ServiceException {
 		Language DEFAULT_LANGUAGE = languageService.getByCode("en");
 		
 		Category category = new Category();
 		categoryService.create(category);
 		
 		CategoryDescription description = new CategoryDescription();
 		description.setName("Description");
 		description.setSeUrl("seUrl Test");
 		Language language = languageService.getByCode(ENGLISH_LANGUAGE_CODE);
 		description.setLanguage(language);
 		
 		categoryService.addCategoryDescription(category, description);
 		Assert.assertTrue(categoryService.count() == 1);
 	}
 	
 	@Test
 	public void SetCategoriesBySeUrl() throws ServiceException {
 		Language DEFAULT_LANGUAGE = languageService.getByCode("en");
 		
 		Category category = new Category();
 		categoryService.create(category);
 		
 		CategoryDescription description = new CategoryDescription();
 		description.setName("Description");
 		description.setSeUrl("seUrl Test");
 		Language language = languageService.getByCode(ENGLISH_LANGUAGE_CODE);
 		description.setLanguage(language);
 		
 		categoryService.addCategoryDescription(category, description);
 		
 		//Set<Category> SetBySeUrl = categoryService.SetBySeUrl("seUrl Test");
 		//Assert.assertEquals(SetBySeUrl.size(), 1);
 	}
 	
 	@Test
 	public void SetByLineage() throws ServiceException {
 		Category category = new Category();
 		categoryService.create(category);
 		
 		category.setLineage("lineage test");
 		categoryService.update(category);
 		
 		//Set<Category> SetByLineage = categoryService.listByLineage("lineage test");
 		Assert.assertEquals(categoryService.listByLineage("lineage test").size(), 1);
 	}
 	
 	@Test 
 	public void createCategoryProduct() throws ServiceException {
		
		//not a real test scenario, a new one is in the way
		
/*		Category category = new Category();
 		categoryService.create(category);
 		
 		Product product = new Product();
 		product.getAuditSection().setDateCreated(date);
 		productService.create(product);
 		categoryService.addProduct(category, product);
 		//Assert.assertTrue(category.getProducts().size() == 1);
 		
 		categoryService.removeProduct(category, product);
 		//Assert.assertTrue(category.getProducts().size() == 0);
*/	}
 }
