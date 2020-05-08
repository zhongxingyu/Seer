 package org.cheyou.web;
 
 import java.io.File;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.struts2.util.ServletContextAware;
 import org.cheyou.dao.BrandService;
 import org.cheyou.dao.model.Brand;
 import org.cheyou.util.ContextUtil;
 import org.cheyou.util.FileTool;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 public class BrandAction extends ActionSupport implements ServletContextAware {
 	
 	private int id;
 	private String brandName;
 	private String brandImg;
 	private String updateImg = "0";
 	private BrandService brandService = ContextUtil.getBean(BrandService.class, "brandService");
 	
 	private File img;
 	private String imgContentType;
 	private String imgFileName;
 	private ServletContext context;
	//C:/Documents and Settings/jian_li/workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/cheyou/img/upload
 	private static final String UPLOAD_FILE_PATH = "img/upload/brands/";
 	
 	
 	public String getUpdateImg() {
 		return updateImg;
 	}
 
 	public void setUpdateImg(String updateImg) {
 		this.updateImg = updateImg;
 	}
 
 	public String getBrandImg() {
 		return brandImg;
 	}
 
 	public void setBrandImg(String brandImg) {
 		this.brandImg = brandImg;
 	}
 
 	public File getImg() {
 		return img;
 	}
 
 	public void setImg(File img) {
 		this.img = img;
 	}
 
 	public String getImgContentType() {
 		return imgContentType;
 	}
 
 	public void setImgContentType(String imgContentType) {
 		this.imgContentType = imgContentType;
 	}
 
 	public String getImgFileName() {
 		return imgFileName;
 	}
 
 	public void setImgFileName(String imgFileName) {
 		this.imgFileName = imgFileName;
 	}
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getBrandName() {
 		return brandName;
 	}
 
 	public void setBrandName(String brandName) {
 		this.brandName = brandName;
 	}
 
 	public String add() throws Exception {
 		String imgPath = saveUploadFile();
 	
 		Brand brand = new Brand();
 		brand.setName(brandName);
 		brand.setImg(imgPath);
 		brandService.addBrand(brand);
 		
 		List<Brand> brands = brandService.getAllBrands();
 		ActionContext.getContext().put("brands", brands);
 		
 		return SUCCESS;
 	}
 	
 	public String init() throws Exception {
 		List<Brand> brands = brandService.getAllBrands();
 		ActionContext.getContext().put("brands", brands);
 		
 		return SUCCESS;
 	}
 	
 	public String delete() throws Exception {
 		brandService.delete(id);
 		
 		List<Brand> brands = brandService.getAllBrands();
 		ActionContext.getContext().put("brands", brands);
 		
 		return SUCCESS;
 	}
 	
 	public String view() throws Exception {
 		Brand brand = brandService.fetch(id);
 		brandName = brand.getName();
 		brandImg = brand.getImg();
 		
 		return INPUT;
 	}
 	
 	public String save() throws Exception {
 		Brand brand = brandService.fetch(id);
 		brand.setName(brandName);
 		if(updateImg.equals("1")) {
 			// 修改图片
 			String imgPath = saveUploadFile();
 			brand.setImg(imgPath);
 		}
 		brandService.updateBrand(brand);
 		
 		List<Brand> brands = brandService.getAllBrands();
 		ActionContext.getContext().put("brands", brands);
 		
 		return SUCCESS;
 	}
 	public void setServletContext(ServletContext context) {
 		this.context = context;
 		
 	}
 	
 	private String saveUploadFile() {
 		try {
 			String targetDir = context.getRealPath(UPLOAD_FILE_PATH);
 			//System.out.println(targetDir);
 			String targetFileName = FileTool.generateFileName(imgFileName);
 			File targetFile = new File(targetDir, targetFileName);
 			FileUtils.copyFile(img, targetFile);
 			
 			return UPLOAD_FILE_PATH + targetFileName;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		}
 	}
 }
