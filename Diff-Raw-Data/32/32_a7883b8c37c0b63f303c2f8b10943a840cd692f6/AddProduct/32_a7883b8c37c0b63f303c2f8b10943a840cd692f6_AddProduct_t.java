 package domain;
 import display.FrontCommand;
 import data.*;
 import java.util.*;
 import java.io.IOException;
 import javax.servlet.ServletException;
 
 public class AddProduct extends FrontCommand {
 	public void process() throws ServletException, IOException {
 	ProductFinder prods = (ProductFinder) context.getBean("productFinder");
 	
 	ProductRowGateway prod = prods.create();
 
 	if (request.getParameter("name") != null) {
 		
 		String name = request.getParameter("name");
 		prod.setName(name);
 		String idBrand = request.getParameter("idBrand");
 		String idCategory = request.getParameter("idCategory");
 		String idGenre = request.getParameter("idGenre");
 		String price = request.getParameter("price");
 		String description = request.getParameter("description");
 		prod.setidBrand(Integer.parseInt(idBrand));
 		prod.setidCategory(Integer.parseInt(idCategory));
 		prod.setidGenre(Integer.parseInt(idGenre));
 		prod.setPrice(Integer.parseInt(price));
 		prod.setDescription(description);
 	 
 		prod.insert();
 		response.sendRedirect("domain.ListProducts");
 	}
 	CategoryFinder categories = (CategoryFinder) context.getBean("categoryFinder");
 	List<CategoryRowGateway> data_cats = categories.findAll();
 	List param_cats = new ArrayList();
 	for (int i = 0; i < data_cats.size(); i++) {
 		CategoryRowGateway cat = data_cats.get(i);
 		Map item = new HashMap();
 		item.put("idCategory",cat.getidCategory());
 		item.put("name",cat.getName());
 		param_cats.add(item);
     }
 	
 	BrandFinder brands = (BrandFinder) context.getBean("brandFinder");
 	List<BrandRowGateway> data_brands = brands.findAll();
 	List param_brands = new ArrayList();
 	for (int i = 0; i < data_brands.size(); i++) {
 		BrandRowGateway brand = data_brands.get(i);
 		Map item = new HashMap();
 		item.put("idBrand",brand.getidBrand());
 		item.put("brand",brand.getName());
 		param_brands.add(item);
     }
 	
 	ColorFinder colors = (ColorFinder) context.getBean("colorFinder");
 	List<ColorRowGateway> data_colors = colors.findAll();
 	List param_colors = new ArrayList();
 	for (int i = 0; i < data_colors.size(); i++) {
 		ColorRowGateway color = data_colors.get(i);
 		Map item = new HashMap();
 		item.put("idColor",color.getidColor());
 		item.put("color",color.getColor());
 		param_colors.add(item);
     }
 	
 	GenreFinder genres = (GenreFinder) context.getBean("genreFinder");
 	List<GenreRowGateway> data_genres = genres.findAll();
 	List param_genres = new ArrayList();
 	for (int i = 0; i < data_genres.size(); i++) {
 		GenreRowGateway genre = data_genres.get(i);
 		Map item = new HashMap();
 		item.put("idGenre",genre.getidGenre());
 		item.put("genre",genre.getGenre());
 		param_genres.add(item);
     }
 	
 	SizeFinder sizes = (SizeFinder) context.getBean("sizeFinder");
 	List<SizeRowGateway> data_sizes = sizes.findAll();
 	List param_sizes = new ArrayList();
 	for (int i = 0; i < data_sizes.size(); i++) {
 		SizeRowGateway size = data_sizes.get(i);
 		Map item = new HashMap();
 		item.put("idSize",size.getidSize());
 		item.put("size",size.getSize());
 		param_sizes.add(item);
     }
 	
	SubcategoriesFinder subcategories = (SubcategoriesFinder) context.getBean("subcategoriesFinder");
	List<SubcategoriesRowGateway> data_subcategories = subcategories.findAll();
 	List param_subcategories = new ArrayList();
 	for (int i = 0; i < data_subcategories.size(); i++) {
		SubcategoriesRowGateway subcategory = data_subcategories.get(i);
 		Map item = new HashMap();
 		item.put("idSubcategory",subcategory.getidSubcategories());
		item.put("subcategory",subcategory.getName());
 		param_subcategories.add(item);
    }
 	
 	request.setAttribute("categories",param_cats);
 	request.setAttribute("brands",param_brands);
 	request.setAttribute("colors",param_colors);
 	request.setAttribute("genres",param_genres);
 	request.setAttribute("sizes",param_sizes);
	request.setAttribute("subcategories",param_subcategories);
 	
 	
     forward("/addProduct.jsp");
   }
 }
