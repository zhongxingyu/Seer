 package za.ac.sun.cs.hons.minke.server.utils.json;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import za.ac.sun.cs.hons.minke.client.serialization.entities.location.City;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.location.CityLocation;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.location.Country;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.location.Province;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.BranchProduct;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.Brand;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.Category;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.DatePrice;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.product.Product;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.store.Branch;
 import za.ac.sun.cs.hons.minke.client.serialization.entities.store.Store;
 
 import com.google.appengine.labs.repackaged.org.json.JSONArray;
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONObject;
 
 public class JSONParser {
 
 	public static ArrayList<Product> parseProducts(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("products");
 		ArrayList<Product> products = new ArrayList<Product>();
 		for (int i = 0; i < array.length(); i++) {
 			products.add(parseProduct(array.getJSONObject(i)));
 		}
 		return products;
 	}
 
 	public static Product parseProduct(JSONObject obj) throws JSONException {
 		Product product = new Product();
 		product.setBrandID(obj.getLong("brandId"));
 		product.setID(obj.getLong("id"));
 		product.setName(obj.getString("name"));
 		product.setMeasurement(obj.getString("measure"));
 		product.setSize(obj.getDouble("size"));
 		return product;
 	}
 
 	public static ArrayList<Category> parseCategories(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("categories");
 		ArrayList<Category> categories = new ArrayList<Category>();
 		for (int i = 0; i < array.length(); i++) {
 			categories.add(parseCategory(array.getJSONObject(i)));
 		}
 		return categories;
 	}
 
 	public static Category parseCategory(JSONObject obj) throws JSONException {
 		Category category = new Category();
 		category.setID(obj.getLong("id"));
 		category.setName(obj.getString("name"));
 		return category;
 	}
 
 	public static ArrayList<City> parseCities(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("cities");
 		ArrayList<City> cities = new ArrayList<City>();
 		for (int i = 0; i < array.length(); i++) {
 			cities.add(parseCity(array.getJSONObject(i)));
 		}
 		return cities;
 	}
 
 	public static City parseCity(JSONObject obj) throws JSONException {
 		City city = new City();
 		city.setProvinceID(obj.getLong("provinceId"));
 		city.setID(obj.getLong("id"));
 		city.setName(obj.getString("name"));
 		city.setLat(obj.getDouble("lat"));
 		city.setLon(obj.getDouble("lon"));
 		return city;
 	}
 
 	public static ArrayList<Country> parseCountries(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("countries");
 		ArrayList<Country> countries = new ArrayList<Country>();
 		for (int i = 0; i < array.length(); i++) {
 			countries.add(parseCountry(array.getJSONObject(i)));
 		}
 		return countries;
 	}
 
 	public static Country parseCountry(JSONObject obj) throws JSONException {
 		Country country = new Country();
 		country.setID(obj.getLong("id"));
 		country.setName(obj.getString("name"));
 		return country;
 	}
 
 	public static ArrayList<Province> parseProvinces(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("provinces");
 		ArrayList<Province> provinces = new ArrayList<Province>();
 		for (int i = 0; i < array.length(); i++) {
 			provinces.add(parseProvince(array.getJSONObject(i)));
 		}
 		return provinces;
 	}
 
 	public static Province parseProvince(JSONObject obj) throws JSONException {
 		Province province = new Province();
 		province.setCountryID(obj.getLong("countryId"));
 		province.setID(obj.getLong("id"));
 		province.setName(obj.getString("name"));
 		return province;
 	}
 
 	public static ArrayList<BranchProduct> parseBranchProducts(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("branchProducts");
 		ArrayList<BranchProduct> branchProducts = new ArrayList<BranchProduct>();
 		for (int i = 0; i < array.length(); i++) {
 			branchProducts.add(parseBranchProduct(array.getJSONObject(i)));
 		}
 		return branchProducts;
 	}
 
 	public static BranchProduct parseBranchProduct(JSONObject obj)
 			throws JSONException {
 		BranchProduct branchProduct = new BranchProduct();
 		branchProduct.setBranchID(obj.getLong("branchId"));
 		branchProduct.setID(obj.getLong("id"));
 		branchProduct.setDatePriceID(obj.getLong("datePriceId"));
 		branchProduct.setProductID(obj.getLong("productId"));
 		return branchProduct;
 	}
 
 	public static ArrayList<DatePrice> parseDatePrices(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("datePrices");
 		ArrayList<DatePrice> datePrices = new ArrayList<DatePrice>();
 		for (int i = 0; i < array.length(); i++) {
 			datePrices.add(parseDatePrice(array.getJSONObject(i)));
 		}
 		return datePrices;
 	}
 
 	public static DatePrice parseDatePrice(JSONObject obj) throws JSONException {
 		DatePrice datePrice = new DatePrice();
 		datePrice.setBranchProductID(obj.getLong("branchProductID"));
 		datePrice.setID(obj.getLong("id"));
 		datePrice.setPrice(obj.getInt("price"));
 		datePrice.setDate(obj.getLong("date"));
 		return datePrice;
 	}
 
 	public static ArrayList<Store> parseStores(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("stores");
 		ArrayList<Store> stores = new ArrayList<Store>();
 		for (int i = 0; i < array.length(); i++) {
 			stores.add(parseStore(array.getJSONObject(i)));
 		}
 		return stores;
 	}
 
 	public static Store parseStore(JSONObject obj) throws JSONException {
 		Store store = new Store();
 		store.setID(obj.getLong("id"));
 		store.setName(obj.getString("name"));
 		return store;
 	}
 
 	public static ArrayList<Brand> parseBrands(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("brands");
 		ArrayList<Brand> brands = new ArrayList<Brand>();
 		for (int i = 0; i < array.length(); i++) {
 			brands.add(parseBrand(array.getJSONObject(i)));
 		}
 		return brands;
 	}
 
 	public static Brand parseBrand(JSONObject obj) throws JSONException {
 		Brand brand = new Brand();
 		brand.setID(obj.getLong("id"));
 		brand.setName(obj.getString("name"));
 		return brand;
 	}
 
 	public static ArrayList<Branch> parseBranches(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("branches");
 		ArrayList<Branch> branches = new ArrayList<Branch>();
 		for (int i = 0; i < array.length(); i++) {
 			branches.add(parseBranch(array.getJSONObject(i)));
 		}
 		return branches;
 	}
 
 	public static Branch parseBranch(JSONObject obj) throws JSONException {
 		Branch branch = new Branch();
 		branch.setCityLocationID(obj.getLong("cityLocationId"));
 		branch.setID(obj.getLong("id"));
 		branch.setName(obj.getString("name"));
 		branch.setStoreID(obj.getLong("storeId"));
 		return branch;
 	}
 
 	public static ArrayList<CityLocation> parseCityLocations(JSONObject obj)
 			throws JSONException {
 		JSONArray array = obj.getJSONArray("cityLocations");
 		ArrayList<CityLocation> cityLocations = new ArrayList<CityLocation>();
 		for (int i = 0; i < array.length(); i++) {
 			cityLocations.add(parseCityLocation(array.getJSONObject(i)));
 		}
 		return cityLocations;
 	}
 
 	public static CityLocation parseCityLocation(JSONObject obj)
 			throws JSONException {
 		CityLocation cityLocation = new CityLocation();
 		cityLocation.setCityID(obj.getLong("cityId"));
 		cityLocation.setID(obj.getLong("id"));
 		cityLocation.setName(obj.getString("name"));
 		cityLocation.setLat(obj.getDouble("lat"));
 		cityLocation.setLon(obj.getDouble("lon"));
 		return cityLocation;
 	}
 
 	public static Branch parseBranchProto(JSONObject obj, City city,
 			Province province, Store store) throws JSONException {
 		Branch branch = new Branch();
 		branch.setID(0L);
 		branch.setName(obj.getString("name"));
 		if(store == null){
 			store = new Store(obj.getString("storeName"));
 			store.setID(0L);
 		}else{
 			branch.setStore(store);
 		}
 		double lat = obj.getDouble("lat");
 		double lon = obj.getDouble("lon");
 		if (city == null) {
 			city = new City(obj.getString("cityName"), province, lat, lon);
 			city.setID(0L);
 		}
 		CityLocation cl = new CityLocation(branch.getName(), city, lat, lon);
 		cl.setID(0L);
 		branch.setLocation(cl);
 		branch.setStore(store);
 		return branch;
 	}
 
 	public static BranchProduct parseBranchProductProto(JSONObject obj,
 			Branch branch, Brand brand) throws JSONException {
 		BranchProduct branchProduct = new BranchProduct();
 		if (brand == null) {
 			brand = new Brand(obj.getString("brandName"));
 			brand.setID(0L);
 		}
 		Product product = new Product(obj.getString("name"), brand,
 				obj.getDouble("size"), obj.getString("measure"));
 		product.setID(0L);
 		branchProduct.setProduct(product);
 		DatePrice dp = new DatePrice();
 		dp.setDate(new Date());
 		dp.setPrice(obj.getInt("price"));
 		dp.setID(0L);
 		branchProduct.setDatePrice(dp);
 		branchProduct.setBranch(branch);
 		branchProduct.setID(0L);
 		return branchProduct;
 	}
 
 	public static long parseLong(JSONObject obj, String param)
 			throws JSONException {
 		return obj.getLong(param);
 	}
 
 }
