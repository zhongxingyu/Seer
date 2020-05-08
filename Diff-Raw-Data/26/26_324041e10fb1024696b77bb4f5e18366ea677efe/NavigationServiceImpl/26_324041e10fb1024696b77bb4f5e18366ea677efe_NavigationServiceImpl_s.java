 /**
  * 
  */
 package de.enwida.web.service.implementation;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import de.enwida.transport.Aspect;
 import de.enwida.web.model.ChartNavigationData;
 import de.enwida.web.model.ProductTree;
 import de.enwida.web.model.ProductTree.ProductAttributes;
 import de.enwida.web.model.User;
 import de.enwida.web.service.interfaces.IAvailibilityService;
 import de.enwida.web.service.interfaces.INavigationService;
 import de.enwida.web.service.interfaces.ISecurityService;
 import de.enwida.web.utils.ChartNavigationLocalizer;
 import de.enwida.web.utils.ObjectMapperFactory;
 import de.enwida.web.utils.ProductLeaf;
 import de.enwida.web.utils.ProductRestriction;
 
 public class NavigationServiceImpl implements INavigationService {
 
     private static Logger logger = Logger.getLogger(NavigationServiceImpl.class);
 
 	@Autowired
 	private ISecurityService securityService;
 	
 	@Autowired
 	private IAvailibilityService availibilityService;
 	
 	@Autowired
 	private ObjectMapperFactory objectMapperFactory;
 	
 	@Autowired
 	private ChartNavigationLocalizer navigationLocalizer;
 	
 	private String jsonDir;
 	private ObjectMapper objectMapper;
 	private Hashtable<Integer, ChartNavigationData> defaultNavigationData =  new Hashtable<Integer, ChartNavigationData>();
 	
 	public NavigationServiceImpl(String jsonDir) {
 		this.jsonDir = jsonDir;
 	}
 	
 	@PostConstruct
 	public void init() throws IOException {
 		objectMapper = objectMapperFactory.create();
 		readJsonNavigationFiles();
 	}
 	
 	@Override
 	public Hashtable<Integer, ChartNavigationData> getAllDefaultNavigationData() {
 		// Clone every stored NavigationData instance
 		final Hashtable<Integer, ChartNavigationData> result = new Hashtable<Integer, ChartNavigationData>();
 		
 		for (final int key : defaultNavigationData.keySet()) {
 			result.put(key, defaultNavigationData.get(key).clone());
 		}
 		return result;
 	}
 
 	public ChartNavigationData getDefaultNavigationData(int chartId) {
 		final ChartNavigationData result = defaultNavigationData.get(chartId);
 		if (result == null) {
 			return null;
 		}
 	    return defaultNavigationData.get(chartId).clone();
 	}
 
     public ChartNavigationData getNavigationData(int chartId, User user, Locale locale) throws Exception {
 	    // Get basic navigation data from hash table and apply
 	    // internationalized properties
         final ChartNavigationData navigationData = getDefaultNavigationData(chartId);
         
         // Fetch the related aspects and shrink the navigation data
         // under security and availability perspective
        shrinkNavigationOnSecurity(navigationData, user);
         shrinkNavigationOnAvailibility(navigationData, user);
         
         // Localize Strings
         return navigationLocalizer.localize(navigationData, chartId, locale);
     }
     
     public ChartNavigationData getNavigationDataUNSECURE(int chartId, User user, Locale locale) {
 	    // Get basic navigation data from hash table and apply
 	    // internationalized properties
         final ChartNavigationData navigationData = defaultNavigationData.get(chartId).clone();
         
         // Skip security and availability checks...
 
         // Localize Strings
         return navigationLocalizer.localize(navigationData, chartId, locale);
     }
 
 	@Override
 	public ChartNavigationData getNavigationDataFromJsonFile(int chartId) throws IOException {
 		final InputStream in = new FileInputStream(new File(jsonDir, chartId + ".json"));
 		return objectMapper.readValue(in, ChartNavigationData.class);
 	}
 	
     public String getJsonDir() {
 		return jsonDir;
 	}
 
 	public void setJsonDir(String jsonDir) {
 		this.jsonDir = jsonDir;
 	}
 
     private interface IProductRestrictionGetter {
         public ProductRestriction getProductRestriction(int productId, int tso, Aspect aspect) throws Exception;
     }
     
     private void shrinkNavigation(ChartNavigationData navigationData, IProductRestrictionGetter service) throws Exception {
         for (final ProductTree productTree : navigationData.getProductTrees()) {
             final List<ProductAttributes> products = productTree.flatten();
             
             for (final ProductAttributes productAttrs : products) {
                 final List<ProductRestriction> restrictions = new ArrayList<ProductRestriction>();
                 for (final Aspect aspect : navigationData.getAspects()) {
                     restrictions.add(service.getProductRestriction(productAttrs.productId, productTree.getTso(), aspect));
                 }
                 final ProductRestriction combinedRestriction = ProductRestriction.combineMaximum(restrictions);
     
                 if (combinedRestriction == null) {
                     // Assume nothing is allowed (i.e. fully restricted)
                     // Delete the corresponding product leaf from the tree
                     productTree.removeProduct(productAttrs.productId);
                 } else {
                     // Apply restrictions to the tree
                     final ProductLeaf leaf = productTree.getLeaf(productAttrs.productId);
                    leaf.setTimeRange(combinedRestriction.getTimeRange());
                    leaf.setResolution(combinedRestriction.getResolutions());
                 }
             }
         }
     }
     
     private void shrinkNavigationOnSecurity(ChartNavigationData navigationData, final User user) throws Exception {
         shrinkNavigation(navigationData, new IProductRestrictionGetter() {
             
             public ProductRestriction getProductRestriction(int productId, int tso, Aspect aspect) throws Exception {
                 return securityService.getProductRestriction(productId, tso, aspect, user);
             }
         });
     }
 
     private void shrinkNavigationOnAvailibility(ChartNavigationData navigationData, final User user) throws Exception {
         shrinkNavigation(navigationData, new IProductRestrictionGetter() {
             
             public ProductRestriction getProductRestriction(int productId, int tso, Aspect aspect) {
                 return availibilityService.getProductRestriction(productId, tso, aspect, user);
             }
         });
     }
     
 	private void readJsonNavigationFiles() {
 		final Pattern fileNamePattern = Pattern.compile("^(\\d+)\\.json$");
 		final File dir = new File(jsonDir);
 		
 		// Create the containing directory if it does not exist
 		if (!dir.exists()) {
 			dir.mkdir();
 			
 			// There won't be any files in the newly created directory
 			return;
 		}
 		
 		for (final File file : dir.listFiles()) {
 			if (file.isDirectory()) {
 				continue;
 			}
 			final Matcher match = fileNamePattern.matcher(file.getName());
 			if (match.matches()) {
 				final int chartId = Integer.parseInt(match.group(1));
 				try {
 				    final ChartNavigationData navigationData = getNavigationDataFromJsonFile(chartId);
 					defaultNavigationData.put(chartId, navigationData);
 				} catch (IOException e) {
 					logger.error("Error while reading navigation JSON (" + file.getName() + "): " + e.getMessage());
 				}
 			}
 		}
 	}
 	
 }
