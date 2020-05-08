 package ads.controller;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.FileUploadException;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ads.model.AdsDAO;
 import ads.model.AdsVO;
 
 import product.model.ProductDAO;
 import product.model.ProductVO;
 
 public class AdsFacade {
 	HttpServletRequest request = null;
 	HttpServletResponse response = null;
 
 	public AdsFacade() {
 		System.out.println("AdsFacade Constructor without Parameter!");
 	}
 
 	public AdsFacade(HttpServletRequest request, HttpServletResponse response) {
 		this.request = request;
 		this.response = response;
 	}
 
 	// 處理上傳圖片
 	void saveAdsImg() throws IOException {
 		if (!ServletFileUpload.isMultipartContent(request)) {
 			throw new IllegalArgumentException(
 					"Request is not multipart, please 'multipart/form-data' enctype for your form.");
 		}
 
 		ServletFileUpload uploadHandler = new ServletFileUpload(
 				new DiskFileItemFactory());
 		PrintWriter writer;
 		writer = response.getWriter();
 		response.setContentType("application/json");
 		JSONArray json = new JSONArray();
 		try {
 			List<FileItem> items = uploadHandler.parseRequest(request);
 			for (FileItem item : items) {
 				if (!item.isFormField()) {
 					File file = new File(request.getServletContext()
 							.getRealPath("/") + "images/ad/", item.getName());
 					item.write(file);
 
 					// 複製一份圖片到 C 槽，因每次重部署 webapps 下的圖片都會被砍掉
 					cloneFile(file);
 
 					JSONObject jsono = new JSONObject();
 					jsono.put("name", item.getName());
 					jsono.put("size", item.getSize());
 					jsono.put(
 							"url",
 							request.getServletContext().getRealPath("/")
 									+ "product/advertisements?pic="
 									+ item.getName());
 					jsono.put(
 							"delete_url",
 							request.getServletContext().getRealPath("/")
 									+ "product/advertisements?delfile="
 									+ item.getName());
 					jsono.put("delete_type", "GET");
 					json.put(jsono);
 					System.out.println(json.toString());
 				}
 			}
 		} catch (FileUploadException e) {
 			throw new RuntimeException(e);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		} finally {
 			writer.write(json.toString());
 			writer.close();
 		}
 	} // end of saveAdsImg()
 
 	// 複製圖片到 C 槽
 	private void cloneFile(File file) {
 		File folderPath = new File("C:\\images\\ad");
 		if (!folderPath.exists()) {
 			folderPath.mkdirs();
 		}
 
 		File fileC = new File("C:\\images\\ad", file.getName());
 
 		try {
 			InputStream is = new FileInputStream(file);
 			OutputStream os = new FileOutputStream(fileC);
 
 			byte[] b = new byte[1024];
 			int len;
 
 			while ((len = is.read(b)) != -1) {
 				os.write(b, 0, len);
 			}
 
 			is.close();
 			os.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	} // end of cloneFile()
 
 	// ajax 搜尋商品們
 	public void querySearchProducts(String queryProducts) throws IOException {
 
 		ProductDAO productDAO = new ProductDAO();
 		List<ProductVO> list = productDAO.findFuzzyProductName(queryProducts);
 
 		JSONArray jsonArray = new JSONArray();
 		String p_noStr = null;
 		String p_name = null;
 		String str = null;
 		// 搜尋產品編號的結果
 		try {
 			Integer p_no = Integer.parseInt(queryProducts);
 			ProductVO productVO_Pk = (new ProductDAO()).findByPrimaryKey(p_no);
 			p_noStr = productVO_Pk.getP_no().toString();
 			p_name = productVO_Pk.getP_name();
 			str = p_no + ": " + p_name;
 			jsonArray.put(str);
 		} catch (NumberFormatException e) {
 		}
 		// 搜尋產品名的結果
 		for (ProductVO productVO : list) {
 			p_noStr = productVO.getP_no().toString();
 			p_name = productVO.getP_name();
 			str = p_noStr + ": " + p_name;
 			jsonArray.put(str);
 		}
 
 		response.setCharacterEncoding("UTF-8");
 		response.getWriter().write(jsonArray.toString());
 	}
 
 	// 新增或修改 DB 裡的廣告資料
 	public void setAds(String fileName, String productName) throws IOException {
 
 		JSONObject jsonObj = new JSONObject();
 		JSONArray jsonArray = new JSONArray();
 		ProductVO productVO = new ProductVO();
 
 		// 避免直接針對server輸入的productName不合規格
 		try {
 			Integer p_no = Integer.parseInt((productName.split(":"))[0]);
 			productVO = (new ProductDAO()).findByPrimaryKey(p_no);
 		} catch (Exception e) {
 			jsonArray.put("請輸入正確商品編號，或由搜尋商品欄搜尋並選擇商品。");
 			e.printStackTrace();
 		}
 		// 其他驗證
 		if (productName == null || productName.trim().length() == 0) {
 			jsonArray.remove(0);
 			jsonArray.put("尚未選擇商品。");
 		}
 		if (fileName == null || fileName.trim().length() == 0) {
 			jsonArray.put("尚未選擇圖片。");
 			fileName = null;
 		}
 		if (productVO == null)
 			jsonArray.put("您所輸入的商品不存在，請先至商品管理頁面新增商品。");
 
 		if (jsonArray.isNull(0)) {
 			AdsVO adsVO = null;
 			AdsDAO adsDAO = new AdsDAO();
 			List<AdsVO> list = adsDAO.findByAds_filename(fileName);
 
 			if (list.size() == 0) {
 				adsVO = new AdsVO();
 				adsVO.setAds_filename(fileName);
 				adsVO.setProductVO(productVO);
 				adsDAO.insert(adsVO);
 			} else {
 				adsVO = list.get(0);
 				adsVO.setProductVO(productVO);
 				adsDAO.update(adsVO);
 			}
 			try {
 				jsonArray.put(productName + " "	+ fileName + " 廣告設定成功。");
 				jsonObj.put("AdsSuccess", jsonArray);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				jsonObj.put("AdsErr", jsonArray);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		System.out.println("********JSON************"+jsonObj.toString());
 		response.getWriter().write(jsonObj.toString());
 
 	}
 
 
 	// 撈所有的廣告
 	public void getAll() throws IOException {
 		JSONObject jsonObj = null;
 		JSONArray jsonArray = new JSONArray();
 		
 		List<AdsVO> list = new AdsDAO().getAll();
 		for (AdsVO adsVO : list){
 			try {
 				jsonObj = new JSONObject();
 				jsonObj.put("ads_no" ,adsVO.getAds_no());
 				jsonObj.put("ads_filename" ,adsVO.getAds_filename());
 				jsonObj.put("p_name" ,adsVO.getProductVO().getP_no() +": "+ adsVO.getProductVO().getP_name());
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			jsonArray.put(jsonObj);
 		}
 		
 		System.out.println("********JSON************"+jsonArray.toString());
 		response.getWriter().write(jsonArray.toString());
 		
 	}
 
	// 移除廣告設定
	public void remove(Integer ads_no) throws IOException {
		System.out.println("*********************Ads REMOVE******************");
		
		AdsDAO adsDAO = new AdsDAO();
		adsDAO.delete(ads_no);
	}

	// 修改廣告設定
	public void edit(Integer ads_no) throws IOException {
		System.out.println("*********************Ads EDIT******************");
	}
	

 }
