 package za.ac.sun.cs.hons.minke.server.rpc;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import za.ac.sun.cs.hons.minke.client.serialization.entities.location.City;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.location.CityLocation;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.location.Province;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.BranchProduct;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.Brand;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.Category;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.DatePrice;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.Product;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.ProductCategory;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.store.Branch;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.store.Store;
 import za.ac.sun.cs.hons.minke.server.dao.DAOService;
 import za.ac.sun.cs.hons.minke.server.utils.EntityUtils;
 import za.ac.sun.cs.hons.minke.server.utils.json.JSONBuilder;
 import za.ac.sun.cs.hons.minke.server.utils.json.JSONParser;
 
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONObject;
 
 public class EntityRequestServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static boolean initialised = false;
 	Logger log = Logger.getLogger("SERVER");
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 		log.log(Level.INFO, request.toString());
 		initEntities();
 		String requestType = request.getParameter("type");
 		if (requestType == null) {
 			return;
 		}
 		JSONObject obj = null;
 		if (requestType.equals("get_all")) {
 			try {
 				JSONObject cities = JSONBuilder.CitiestoJSON(DAOService.cityDAO
 						.listAll());
 				JSONObject countries = JSONBuilder
 						.CountriestoJSON(DAOService.countryDAO.listAll());
 				JSONObject provinces = JSONBuilder
 						.ProvincestoJSON(DAOService.provinceDAO.listAll());
 				JSONObject products = JSONBuilder
 						.ProductstoJSON(DAOService.productDAO.listAll());
 				JSONObject categories = JSONBuilder
 						.CategoriestoJSON(DAOService.categoryDAO.listAll());
 				JSONObject branches = JSONBuilder
 						.BranchestoJSON(DAOService.branchDAO.listAll());
 				JSONObject brands = JSONBuilder
 						.BrandstoJSON(DAOService.brandDAO.listAll());
 				JSONObject stores = JSONBuilder
 						.StorestoJSON(DAOService.storeDAO.listAll());
 				JSONObject cityLocations = JSONBuilder
 						.CityLocationstoJSON(DAOService.cityLocationDAO
 								.listAll());
 				JSONObject datePrices = JSONBuilder
 						.DatePricestoJSON(DAOService.datePriceDAO.listAll());
 				JSONObject branchProducts = JSONBuilder
 						.BranchProductstoJSON(DAOService.branchProductDAO
 								.listAll());
 				JSONObject productCategories = JSONBuilder
 						.ProductCategoriestoJSON(DAOService.productCategoryDAO
 								.listAll());
 				obj = JSONBuilder.toJSON(cities, countries, provinces,
 						products, categories, branches, stores, cityLocations,
 						branchProducts, datePrices, productCategories, brands);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 		} else if (requestType.equals("update_branchproduct")) {
 			try {
 				obj = JSONBuilder.toJSON(updateBranchProduct(request));
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		response.setContentType("application/json;charset=UTF-8");
 		response.setHeader("Cache-Control", "no-cache");
 		log.log(new LogRecord(Level.INFO, obj.toString()));
 		try {
 			obj.write(response.getWriter());
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 		log.log(Level.INFO, request.toString());
 		initEntities();
 		String requestType = request.getParameter("type");
 		if (requestType == null) {
 			return;
 		}
 		log.log(new LogRecord(Level.INFO, requestType + " "
 				+ requestType.charAt(requestType.length() - 1)));
 		JSONObject obj = null;
 		JSONObject params;
 		try {
 			params = getJSON(request);
 			if (requestType.startsWith("create_branchproduct")) {
 				obj = createBranchProduct(Integer.parseInt(requestType
 						.substring(requestType.length() - 1)), params);
 			} else if (requestType.startsWith("create_branch")) {
 				obj = createBranch(Integer.parseInt(requestType
 						.substring(requestType.length() - 1)), params);
 			} else if (requestType.startsWith("get_product")) {
 				obj = getProduct(params);
 			} else if (requestType.startsWith("get_branchproduct")) {
 				obj = getBranchProduct(params);
 			}
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 		}
 		response.setContentType("application/json;charset=UTF-8");
 		response.setHeader("Cache-Control", "no-cache");
 		if (obj != null) {
 			log.log(new LogRecord(Level.INFO, obj.toString()));
 			try {
 				obj.write(response.getWriter());
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} else {
 			log.log(new LogRecord(Level.INFO, "ERROR"));
 			response.getWriter().write("ERROR");
 		}
 
 	}
 
 	private JSONObject getBranchProduct(JSONObject params) throws JSONException {
 		long productId = JSONParser.parseLong(params, "productId");
 		long branchId = JSONParser.parseLong(params, "branchId");
 		BranchProduct bp = EntityUtils.getBranchProduct(productId, branchId);
 		if (bp == null) {
 			return null;
 		}
 		return JSONBuilder.toJSON(bp);
 	}
 
 	private JSONObject getProduct(JSONObject params) throws JSONException {
 		long productId = JSONParser.parseLong(params, "productId");
 		Product p = EntityUtils.getProduct(productId);
 		if (p == null) {
 			return null;
 		}
 		return JSONBuilder.toJSON(p);
 	}
 
 	private JSONObject createBranchProduct(int type, JSONObject params)
 			throws JSONException {
 		Branch branch = JSONParser.parseBranch(params.getJSONObject("branch"));
 		Category category = null;
 		Brand brand = null;
 		if (type == 0 || type == 1) {
 			brand = JSONParser.parseBrand(params.getJSONObject("brand"));
 		}
 		if (type == 0 || type == 2) {
 			category = JSONParser.parseCategory(params
 					.getJSONObject("category"));
 		} else {
 			category = new Category(params.getJSONObject("branchProductProto")
 					.getString("categoryName"));
 			category.setID(0L);
 		}
 		Object[] ret = EntityUtils.addBranchProduct(JSONParser
 				.parseBranchProductProto(
 						params.getJSONObject("branchProductProto"), branch,
 						brand), category);
 
 		BranchProduct branchProduct = (BranchProduct) ret[0];
 		Product product = branchProduct.getProduct();
 		brand = product.getBrand();
 		ProductCategory pc = (ProductCategory) ret[1];
 		category = pc.getCategory();
 		DatePrice dp = branchProduct.getDatePrice();
 		if (type == 0) {
 			return JSONBuilder.toJSON(JSONBuilder.toJSON(branchProduct),
 					JSONBuilder.toJSON(dp), JSONBuilder.toJSON(product),
 					JSONBuilder.toJSON(pc));
 		} else if (type == 1) {
 			return JSONBuilder.toJSON(JSONBuilder.toJSON(branchProduct),
 					JSONBuilder.toJSON(dp), JSONBuilder.toJSON(product),
 					JSONBuilder.toJSON(pc), JSONBuilder.toJSON(category));
 		} else if (type == 2) {
 			return JSONBuilder.toJSON(JSONBuilder.toJSON(branchProduct),
 					JSONBuilder.toJSON(dp), JSONBuilder.toJSON(product),
 					JSONBuilder.toJSON(pc), JSONBuilder.toJSON(brand));
 		} else {
 			return JSONBuilder.toJSON(JSONBuilder.toJSON(branchProduct),
 					JSONBuilder.toJSON(dp), JSONBuilder.toJSON(product),
 					JSONBuilder.toJSON(pc), JSONBuilder.toJSON(category),
 					JSONBuilder.toJSON(brand));
 		}
 	}
 
 	private JSONObject createBranch(int type, JSONObject params)
 			throws JSONException {
 		log.log(new LogRecord(Level.INFO, String.valueOf(type)));
		log.log(new LogRecord(Level.INFO, params.toString()));
 		Store store = null;
 		City city = null;
 		Province province = null;
 		if (type == 0 || type == 2) {
 			store = JSONParser.parseStore(params.getJSONObject("store"));
 		}
 		if (type == 0 || type == 1) {
 			city = JSONParser.parseCity(params.getJSONObject("city"));
 		}
 		if (type == 2 || type == 3) {
 			province = JSONParser.parseProvince(params
 					.getJSONObject("province"));
 		}
 		Branch branch = EntityUtils.addBranch(JSONParser.parseBranchProto(
 				params.getJSONObject("branchProto"), city, province, store));
		log.info(branch.toString());
 		CityLocation loc = branch.getLocation();
 		store = branch.getStore();
 		city = loc.getCity();
 		if (type == 0) {
 			return JSONBuilder.toJSON(JSONBuilder.toJSON(branch),
 					JSONBuilder.toJSON(loc));
 		} else if (type == 1) {
 			return JSONBuilder.toJSON(JSONBuilder.toJSON(branch),
 					JSONBuilder.toJSON(loc), JSONBuilder.toJSON(store));
 		} else if (type == 2) {
 			return JSONBuilder.toJSON(JSONBuilder.toJSON(branch),
 					JSONBuilder.toJSON(loc), JSONBuilder.toJSON(city));
 		} else {
 			return JSONBuilder.toJSON(JSONBuilder.toJSON(branch),
 					JSONBuilder.toJSON(loc), JSONBuilder.toJSON(store),
 					JSONBuilder.toJSON(city));
 		}
 
 	}
 
 	private JSONObject getJSON(HttpServletRequest request) throws IOException,
 			JSONException {
 		InputStream inputstream = request.getInputStream();
 		String resultstring = convertStreamToString(inputstream);
 		inputstream.close();
 		return new JSONObject(resultstring);
 	}
 
 	private static String convertStreamToString(InputStream is) {
 		String line = "";
 		StringBuilder total = new StringBuilder();
 		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 		try {
 			while ((line = rd.readLine()) != null) {
 				total.append(line);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return total.toString();
 	}
 
 	private void initEntities() {
 		if (!initialised) {
 			DAOService.init();
 			EntityUtils.addData();
 			initialised = true;
 		}
 	}
 
 	private BranchProduct updateBranchProduct(HttpServletRequest request) {
 		String sid = request.getParameter("id");
 		String sprice = request.getParameter("price");
 		if (sid != null && sprice != null) {
 			long id = Long.parseLong(sid);
 			int price = Integer.parseInt(sprice);
 			return EntityUtils.updateBranchProduct(id, price);
 		}
 		return null;
 	}
 
 }
