 package edu.hust.k54.controller;
 
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.validation.BindException;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import edu.hust.k54.model.FileUpload;
 import edu.hust.k54.persistence.Donviquanly;
 import edu.hust.k54.persistence.DonviquanlyHome;
 import edu.hust.k54.persistence.Phongban;
 import edu.hust.k54.persistence.PhongbanHome;
 import edu.hust.k54.persistence.Soyeulylich;
 import edu.hust.k54.persistence.SoyeulylichHome;
 import edu.hust.k54.persistence.Taikhoandangnhap;
 
 public class FileUploadController extends SimpleFormController {
 
 	public FileUploadController() {
 		setCommandClass(FileUpload.class);
 		setCommandName("fileUploadForm");
 	}
 
 	@Override
 	protected ModelAndView onSubmit(HttpServletRequest request,
 			HttpServletResponse response, Object command, BindException errors)
 			throws Exception {
 
 		Taikhoandangnhap account = (Taikhoandangnhap) request.getSession()
 				.getAttribute("user");
 		FileUpload file = (FileUpload) command;
 		String typeaccount = request.getParameter("type");
 		MultipartFile multipartFile = file.getFile();
 		System.out.println(typeaccount);
 		String pathFile = "";
 		String filename = "";
 		InputStream sIn = null;
 		OutputStream sOut = null;
 		if (multipartFile != null) {
 			if (multipartFile.getSize() > 0) {
 				sIn = multipartFile.getInputStream();
 				System.out.println("Size: " + multipartFile.getSize());
 				Date now = new Date();
 				filename = "/resources/images/avatar/"
 						+ now.getTime() + "-"
 						+ multipartFile.getOriginalFilename();
 				pathFile = request.getRealPath("") 
 						+ filename;
 				System.err.println(pathFile);
 				System.err.println(multipartFile.getContentType());
 				String type = multipartFile.getContentType();
 				if (!type.startsWith("image")) {
 					if (typeaccount.equals("staff")){
 						ModelAndView modelAndView =  new ModelAndView("sua_thongtincanhan");
 						modelAndView.addObject("error", "File ảnh không hợp lệ");
 						modelAndView.addObject("canbo", account.getSoyeulylich());
 						return modelAndView;
 					} else if (typeaccount.equals("manager")){
 						ModelAndView modelAndView =  new ModelAndView("QL_TTPhongban");
 						modelAndView.addObject("error", "File ảnh không hợp lệ");
						Soyeulylich soyeulylich = account.getSoyeulylich();
						(new SoyeulylichHome()).attachDirty(soyeulylich);
 						modelAndView.addObject("phongban", soyeulylich.getPhongban());
 						return modelAndView;
 					} else {
 						ModelAndView modelAndView =  new ModelAndView("quanly_TTDonVi");
 						modelAndView.addObject("error", "File ảnh không hợp lệ");
						Soyeulylich soyeulylich = account.getSoyeulylich();
						(new SoyeulylichHome()).attachDirty(soyeulylich);
 						modelAndView.addObject("donvi", soyeulylich.getPhongban().getDonviquanly());
 						return modelAndView;
 					}
 				}
 				sOut = new FileOutputStream(pathFile);
 				System.out.println("fileName:"
 						+ multipartFile.getOriginalFilename());
 
 				int readBytes = 0;
 				byte[] buffer = new byte[10000];
 				while ((readBytes = sIn.read(buffer, 0, 10000)) != -1) {
 					sOut.write(buffer, 0, readBytes);
 				}
 				sOut.close();
 				sIn.close();
 				if (typeaccount.equalsIgnoreCase("staff")){
 				SoyeulylichHome soyeulylichHome = new SoyeulylichHome();
				Soyeulylich soyeulylich = account.getSoyeulylich();
				soyeulylichHome.attachDirty(soyeulylich);
 				soyeulylich.setDuongdananh(filename);
 				soyeulylichHome.attachDirty(soyeulylich);
 				soyeulylichHome.getSessionFactory().getCurrentSession().flush();
 				return new ModelAndView("sua_thongtincanhan", "canbo", soyeulylich);
 				}
 				else if (typeaccount.equalsIgnoreCase("manager")){
 					PhongbanHome phongbanHome = new PhongbanHome();
 					Phongban phongban = account.getSoyeulylich().getPhongban();
 					phongbanHome.attachDirty(phongban);
 					phongban.setHinhanh(filename);
 					phongbanHome.attachDirty(phongban);
 					phongbanHome.getSessionFactory().getCurrentSession().flush();
 					return new ModelAndView("QL_TTPhongban", "phongban", phongban);
 				}
 				else {
 					System.out.println("here");
 					DonviquanlyHome donviquanlyHome = new DonviquanlyHome();
 					SoyeulylichHome soyeulylichHome = new SoyeulylichHome();
					Soyeulylich soyeulylich = account.getSoyeulylich();
					soyeulylichHome.attachDirty(soyeulylich);
 					Donviquanly donviquanly = soyeulylich.getPhongban().getDonviquanly();
 					donviquanlyHome.attachDirty(donviquanly);
 					donviquanly.setDuongdananh(filename);
 					donviquanlyHome.getSessionFactory().getCurrentSession().flush();
 					return new ModelAndView("quanly_TTDonVi", "donvi", donviquanly);
 				}
 			}
 
 
 		}
 		return null;
 	}
 
 }
