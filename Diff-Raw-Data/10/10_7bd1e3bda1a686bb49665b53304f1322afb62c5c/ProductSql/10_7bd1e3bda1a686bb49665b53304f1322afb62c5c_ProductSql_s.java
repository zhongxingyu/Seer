 package prestashop.database;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 public class ProductSql extends BaseSql {
 	
 	private final static String sqlGetIsProduct = "SELECT * FROM ps_product_lang WHERE name = '"+TAG_NAME+"';";
 	private final static String sqlGetLastProductId = "SELECT max(id_product) as id_product FROM ps_product;";
 	private final static String sqlInsertCategoryProduct = "INSERT INTO ps_category_product(id_category,id_product,position) VALUES("+TAG_CATEGORY+","+TAG_PRODUCT+",0);";
 	private final static String sqlGetProductCategory = "SELECT * FROM ps_category_product WHERE id_product="+TAG_PRODUCT+" AND id_category="+TAG_CATEGORY+";";
 	
 	private final static String sqlInsertProductShop = createInsertProductShopSql();
 	private final static String sqlInsertProductLang = createInsertProductLangSql();
 	private final static String sqlInsertProduct = createInsertProductSql();
 	
	private final static String sqlUpdateProduct = "UPDATE ps_product SET wholesale_price='"+TAG_PRICE+"', quantity='"+TAG_QUANTITY+"' WHERE id_product="+TAG_PRODUCT+";";
	private final static String sqlUpdateProductShop = "UPDATE ps_product_shop SET wholesale_price='"+TAG_PRICE+"' WHERE id_product="+TAG_PRODUCT+";";
 	
 
 	public static void update(DbConnector db, String idProduct, String price, String quantity)
 	{
 		int result = db.execute(
 				sqlUpdateProduct.replace(TAG_PRODUCT, idProduct).replace(TAG_PRICE, price).replace(TAG_QUANTITY, quantity)
 				);
 		if (result != 1) {
 			System.out.println("Blad podczas aktualizacji tabeli produktow " + idProduct);
 		}
 		db.execute(
 				sqlUpdateProductShop.replace(TAG_PRODUCT, idProduct).replace(TAG_PRICE, price)
 				);
 		if (result != 1) {
 			System.out.println("Blad podczas aktualizacji tabeli produktow/shop " + idProduct);
 		}
 	}
 	
 	public static String getProductId(DbConnector db, String name)
 	{
 		String id = "-1";
 		try {
 			ResultSet rs = db.executeSelect(
 					sqlGetIsProduct.replace(TAG_NAME, name)
 					);
 			if (rs.next()) {
 				id = Integer.toString(rs.getInt("id_product"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.out.println("Blad sql" + e.getMessage());
 			System.exit(1);
 		}
 		return id;
 	}
 	
 	public static boolean isProductInCategory(DbConnector db, String idProduct, String idCategory)
 	{
 		try {
 			ResultSet rs = db.executeSelect(
 					sqlGetProductCategory.replace(TAG_PRODUCT, idProduct).replace(TAG_CATEGORY, idCategory)
 					);
 			if (rs.next()) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.out.println("Blad sql" + e.getMessage());
 			System.exit(1);
 		}
 		return true;
 	}
 	
 	public static void addProductToCategory(DbConnector db, String idProduct, String idCategory)
 	{
 		System.out.println("Adding product to category: "+idProduct + "-> "+idCategory );
 		int result = db.execute(
 				sqlInsertCategoryProduct.replace(TAG_PRODUCT,idProduct).replace(TAG_CATEGORY,idCategory)
 				);
 		if (result != 1) {
 			System.out.println("Blad podczas laczenia produktu z kategoria");
 			System.exit(1);
 		}
 	}
 	
 	public static void addProductLang(DbConnector db, String idProduct, String name, String shop, String lang)
 	{
 		System.out.println("Adding product shop: "+idProduct + name );
 		int result = db.execute(
 				sqlInsertProductLang.replace(TAG_PRODUCT,idProduct).replace(TAG_SHOP,shop).replace(TAG_LANG, lang).replace(TAG_NAME, name).replace(TAG_REWRITE, prepareLink(name))
 				);
 		if (result != 1) {
 			System.out.println("Blad podczas ustawiania nazwy produktu " + idProduct + name);
 		}
 	}
 	
 	public static void addProductShop(DbConnector db, String idProduct, String idCategory, String shop)
 	{
 		System.out.println("Adding product shop: "+idProduct );
 		int result = db.execute(
 				sqlInsertProductShop.replace(TAG_PRODUCT,idProduct).replace(TAG_SHOP,shop).replace(TAG_CATEGORY,idCategory)
 				);
 		if (result != 1) {
 			System.out.println("Blad podczas laczenia produktu ze sklepem "+idProduct);
 		}
 	}
 	
 	public static String addProduct(DbConnector db, String idCategory, String name, String shop)
 	{
 		System.out.println("Adding product: " + name + " to category "+idCategory);
 		int result = db.execute(
 				sqlInsertProduct.replace(TAG_CATEGORY,idCategory).replace(TAG_SHOP,shop)
 				);
 		if (result != 1) {
 			System.out.println("Blad podczas ustawiania nazwy produktu " + name);
 		}
 		
 		result = -1;
 		try {
 			ResultSet rs = db.executeSelect(
 					sqlGetLastProductId
 					);
 			if (rs.next()) {
 				result = rs.getInt("id_product");
 			} else {
 				result = -2;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.out.println("Blad sql" + e.getMessage());
 			System.exit(1);
 		}
 		return Integer.toString(result);
 	}
 	
 	
 	private static String createInsertProductSql()
 	{
 		HashMap<String,String> params = new HashMap<String, String>();
 		params.put("id_supplier", "0");
 		params.put("id_manufacturer", "0");
 		params.put("id_category_default", TAG_CATEGORY);
 		params.put("id_shop_default", TAG_SHOP);
		params.put("id_tax_rules_group", "1");
 		params.put("on_sale", "0");
 		params.put("online_only", "0");
 		params.put("ean13", "''");
 		params.put("upc", "''");
 		params.put("ecotax", "0");
 		params.put("quantity", "0");
 		params.put("minimal_quantity", "1");
 		params.put("price", "0");
 		params.put("wholesale_price", "0");
 		params.put("unity", "''");
 		params.put("unit_price_ratio", "0");
 		params.put("additional_shipping_cost", "0");
 		params.put("reference", "''");
 		params.put("supplier_reference", "''");
 		params.put("location", "''");
 		params.put("width", "0");
 		params.put("height", "0");
 		params.put("depth", "0");
 		params.put("weight", "0");
 		params.put("out_of_stock", "2");
 		params.put("quantity_discount", "0");
 		params.put("customizable", "0");
 		params.put("uploadable_files", "0");
 		params.put("text_fields", "0");
 		params.put("active", "1");
 		params.put("available_for_order", "1");
 		params.put("available_date", "'0000-00-00'");
 		params.put("`condition`", "'new'");
 		params.put("show_price", "1");
 		params.put("indexed", "1");
 		params.put("visibility", "'both'");
 		params.put("cache_is_pack", "0");
 		params.put("cache_has_attachments", "0");
 		params.put("is_virtual", "0");
 		params.put("cache_default_attribute", "0");
 		params.put("date_add", "NOW()");
 		params.put("date_upd", "NOW()");
 		params.put("advanced_stock_management", "0");
 		
 		return createInsertStatement("ps_product", params);
 	}
 	
 	private static String createInsertProductLangSql()
 	{
 		HashMap<String,String> params = new HashMap<String,String>();
 		params.put("id_product", TAG_PRODUCT);
 		params.put("id_shop", TAG_SHOP);
 		params.put("id_lang", TAG_LANG);
 		params.put("description", "''");
 		params.put("description_short", "''");
 		params.put("link_rewrite", "'"+TAG_REWRITE+"'");
 		params.put("meta_description", "''");
 		params.put("meta_keywords", "''");
 		params.put("meta_title", "''");
 		params.put("name", "'"+TAG_NAME+"'");
 		params.put("available_now", "''");
 		params.put("available_later", "''");
 
 		String statement = createInsertStatement("ps_product_lang", params);
 		return statement;
 		
 	}
 	
 	private static String createInsertProductShopSql()
 	{
 		HashMap<String,String> params = new HashMap<String, String>();
 		params.put("id_product", TAG_PRODUCT);
 		params.put("id_shop", TAG_SHOP);
 		params.put("id_category_default", TAG_CATEGORY);
		params.put("id_tax_rules_group", "1");
 		params.put("on_sale", "0");
 		params.put("online_only", "0");
 		params.put("ecotax", "0");
 		params.put("minimal_quantity", "1");
 		params.put("price", "0");
 		params.put("wholesale_price", "0");
 		params.put("unity", "''");
 		params.put("unit_price_ratio", "0");
 		params.put("additional_shipping_cost", "0");
 		params.put("customizable", "0");
 		params.put("uploadable_files", "0");
 		params.put("text_fields", "0");
 		params.put("active", "1");
 		params.put("available_for_order", "1");
 		params.put("available_date", "'0000-00-00'");
 		params.put("`condition`", "'new'");
 		params.put("show_price", "1");
 		params.put("indexed", "1");
 		params.put("visibility", "'both'");
 		params.put("cache_default_attribute", "0");
 		params.put("advanced_stock_management", "0");
 		params.put("date_add", "NOW()");
 		params.put("date_upd", "NOW()");
 
 		return createInsertStatement("ps_product_shop", params);	
 	}
 }
