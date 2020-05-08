 package edu.hust.k54.controller;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import edu.hust.k54.persistence.Capuy;
 import edu.hust.k54.persistence.CapuyHome;
 import edu.hust.k54.persistence.Chucvu;
 import edu.hust.k54.persistence.ChucvuHome;
 import edu.hust.k54.persistence.Danhhieu;
 import edu.hust.k54.persistence.DanhhieuHome;
 import edu.hust.k54.persistence.Dantoc;
 import edu.hust.k54.persistence.DantocHome;
 import edu.hust.k54.persistence.Donviquanly;
 import edu.hust.k54.persistence.DonviquanlyHome;
 import edu.hust.k54.persistence.Giaoducphothong;
 import edu.hust.k54.persistence.GiaoducphothongHome;
 import edu.hust.k54.persistence.Hocham;
 import edu.hust.k54.persistence.HochamHome;
 import edu.hust.k54.persistence.Hocvi;
 import edu.hust.k54.persistence.HocviHome;
 import edu.hust.k54.persistence.Khenthuong;
 import edu.hust.k54.persistence.KhenthuongHome;
 import edu.hust.k54.persistence.Phongban;
 import edu.hust.k54.persistence.PhongbanHome;
 import edu.hust.k54.persistence.Quocgia;
 import edu.hust.k54.persistence.QuocgiaHome;
 import edu.hust.k54.persistence.Soyeulylich;
 import edu.hust.k54.persistence.SoyeulylichHome;
 import edu.hust.k54.persistence.Taikhoandangnhap;
 import edu.hust.k54.persistence.Tongiao;
 import edu.hust.k54.persistence.TongiaoHome;
 import edu.hust.k54.persistence.Xuatthan;
 import edu.hust.k54.persistence.XuatthanHome;
 
 public class ManagerController implements Controller {
 
 	private static final int PERMISSION = 2;
 
 	public ModelAndView handleRequest(HttpServletRequest arg0,
 			HttpServletResponse arg1) throws Exception {
 		ModelAndView modelAndView = null;
 		Guest guestController = new Guest();
 		Staff staffController = new Staff();
 		Taikhoandangnhap taikhoandangnhap = (Taikhoandangnhap) arg0
 				.getSession().getAttribute("user");
 		String uri = arg0.getRequestURI();
 
 		if ((taikhoandangnhap == null)
 				|| taikhoandangnhap.getPermission() < PERMISSION) {
 			modelAndView = new ModelAndView("errorPage");
 			return modelAndView;
 		} else {
 			Integer idphongban = null;
 			if (arg0.getParameter("idphongban") != null) {
 				idphongban = Integer.parseInt(arg0.getParameter("idphongban"));
 				// if(taikhoandangnhap.getSoyeulylich().getPhongban().getIdphongban()
 				// != idphongban){
 				// //bug
 				// modelAndView = new ModelAndView("errorPage");
 				// return modelAndView;
 				// }
 				PhongbanHome phongbanHome = new PhongbanHome();
 				if (uri.contains("quanly/phongban")) {
 					if (arg0.getParameter("phongbantype") == null) {
 
 						Map parameter = arg0.getParameterMap();
 						modelAndView = new ModelAndView("QL_TTPhongban");
 						Phongban phongban = phongbanHome.findById(idphongban);
 						if (parameter.containsKey("update")) {
 							String dienthoai = arg0.getParameter("dienthoai");
 							String fax = arg0.getParameter("fax");
 							String thongtinchung = arg0
 									.getParameter("thongtinchung");
 							String ten = arg0.getParameter("tenphong");
 							phongban.setDienthoai(dienthoai);
 							phongban.setFax(fax);
 							phongban.setTen(ten);
 							phongban.setThongtinchung(thongtinchung);
 							Manager manager = new Manager();
 							manager.capNhatTTPB(phongban);
 							modelAndView.addObject("result",
 									"Cập nhật thành công!");
 							phongban = phongbanHome.findById(idphongban);
 						}
 						List<Donviquanly> donviquanly = guestController
 								.TimDVQL(0, 0, null);
 						modelAndView.addObject("donviquanly", donviquanly);
 						modelAndView.addObject("phongban", phongban);
 					} else {
 
 						Map parameter = arg0.getParameterMap();
 						modelAndView = new ModelAndView("QL_TTPhongban");
 						Phongban phongban = phongbanHome.findById(idphongban);
 						Donviquanly donvi = phongban.getDonviquanly();
 						if (parameter.containsKey("update")) {
 							String dienthoai = arg0.getParameter("dienthoai");
 							String fax = arg0.getParameter("fax");
 							String thongtinchung = arg0
 									.getParameter("thongtinchung");
 							String ten = arg0.getParameter("tenphong");
 							phongban.setDienthoai(dienthoai);
 							phongban.setFax(fax);
 							phongban.setTen(ten);
 							phongban.setThongtinchung(thongtinchung);
 							Manager manager = new Manager();
 							manager.capNhatTTPB(phongban);
 							modelAndView.addObject("result",
 									"Cập nhật thành công!");
 							phongban = phongbanHome.findById(idphongban);
 						}
 						modelAndView.addObject("donvi", donvi);
 						List<Donviquanly> donviquanly = guestController
 								.TimDVQL(0, 0, null);
 						modelAndView.addObject("donviquanly", donviquanly);
 						modelAndView.addObject("phongban", phongban);
 					}
 
 				} else if (uri.contains("quanly/hosocanbo")) {
 					Phongban phongban = phongbanHome.findById(idphongban);
 					modelAndView = new ModelAndView("quanly_canbo");
 					List<Donviquanly> donviquanly = guestController.TimDVQL(0,
 							0, null);
 					modelAndView.addObject("donviquanly", donviquanly);
 					modelAndView.addObject("phongban", phongban);
 				} else if (uri.contains("quanly/themcanbo")) {
 					Phongban phongban = phongbanHome.findById(idphongban);
 					modelAndView = new ModelAndView("them_canbo");
 					List<Donviquanly> donviquanly = guestController.TimDVQL(0,
 							0, null);
 					modelAndView.addObject("donviquanly", donviquanly);
 					Map parameter = arg0.getParameterMap();
 					HochamHome hochamHome = new HochamHome();
 					HocviHome hocviHome = new HocviHome();
 					ChucvuHome chucvuHome = new ChucvuHome();
 					GiaoducphothongHome giaoducphothongHome = new GiaoducphothongHome();
 					CapuyHome capuyHome = new CapuyHome();
 					XuatthanHome xuatthanHome = new XuatthanHome();
 					DanhhieuHome danhhieuHome = new DanhhieuHome();
 					DantocHome dantocHome = new DantocHome();
 					QuocgiaHome quocgiaHome = new QuocgiaHome();
 					TongiaoHome tongiaoHome = new TongiaoHome();
 					DonviquanlyHome donviquanlyHome = new DonviquanlyHome();
 					if (parameter.containsKey("update")) {
 						DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
 						String newName = arg0.getParameter("name");
 						Integer newIdDonVi = Integer.parseInt(arg0
 								.getParameter("choiceDonviquanly"));
 						Integer newIdPhongBan = Integer.parseInt(arg0
 								.getParameter("phongban"));
 						Integer newIdHocVi = Integer.parseInt(arg0
 								.getParameter("hocvi"));
 						Integer newIdChucVu = Integer.parseInt(arg0
 								.getParameter("chucvu"));
 						Integer newIdHocHam = Integer.parseInt(arg0
 								.getParameter("hocham"));
 						Integer newIdCapUy = Integer.parseInt(arg0
 								.getParameter("capuy"));
 						Integer newIdGiaoDucPhoThong = Integer.parseInt(arg0
 								.getParameter("giaoducphothong"));
 						Integer newIdDanhHieu = Integer.parseInt(arg0
 								.getParameter("danhhieu"));
 						Integer newIdXuatThan = Integer.parseInt(arg0
 								.getParameter("xuatthan"));
 						Integer newIdQuocGia = Integer.parseInt(arg0
 								.getParameter("quocgia"));
 						Integer newIdDanToc = Integer.parseInt(arg0
 								.getParameter("dantoc"));
 						Integer newIdTonGiao = Integer.parseInt(arg0
 								.getParameter("tongiao"));
 						Integer newLoaiCanBo = Integer.parseInt(arg0
 								.getParameter("loaicanbo"));
 						Integer newSoHieuCongChuc = Integer.parseInt(arg0
 								.getParameter("sohieucongchuc"));
 						String newSoCMT = arg0.getParameter("chungminhnhandan");
 						String newGioiTinh = arg0.getParameter("gioitinh");
 						String newTenThuongDung = arg0
 								.getParameter("tenthuongdung");
 						Date newNgaySinh = dateFormat.parse(arg0
 								.getParameter("ngaysinh"));
 						String newNoiSinh = arg0.getParameter("noisinh");
 						String newQueQuan = arg0.getParameter("quequan");
 						String newNoiO = arg0.getParameter("noiohiennay");
 						String newSoDT = arg0.getParameter("sodienthoai");
 						String newNgayVaoDang = arg0
 								.getParameter("ngayvaodangchinhthuc");
 						Date newNgayCapNhat = dateFormat.parse(arg0
 								.getParameter("ngaycapnhat"));
 						String newSucKhoe = arg0.getParameter("suckhoe");
 						String newNgonNguBiet = arg0
 								.getParameter("ngonngubiet");
 						Soyeulylich soyeulylich = new Soyeulylich();
 						soyeulylich.setHoten(newName);
 						soyeulylich.setDonviquanly(donviquanlyHome
 								.findById(newIdDonVi));
 						soyeulylich.setPhongban(phongbanHome
 								.findById(newIdPhongBan));
 						soyeulylich.setHocvi(hocviHome.findById(newIdHocVi));
 						soyeulylich.setChucvu(chucvuHome.findById(newIdChucVu));
 						soyeulylich.setHocham(hochamHome.findById(newIdHocHam));
 						soyeulylich.setCapuy(capuyHome.findById(newIdCapUy));
 						soyeulylich.setGiaoducphothong(giaoducphothongHome
 								.findById(newIdGiaoDucPhoThong));
 						soyeulylich.setDanhhieu(danhhieuHome
 								.findById(newIdDanhHieu));
 						soyeulylich.setXuatthan(xuatthanHome
 								.findById(newIdXuatThan));
 						soyeulylich.setQuocgia(quocgiaHome
 								.findById(newIdQuocGia));
 						soyeulylich.setDantoc(dantocHome.findById(newIdDanToc));
 						soyeulylich.setTongiao(tongiaoHome
 								.findById(newIdTonGiao));
 						soyeulylich.setLoaiCb(newLoaiCanBo);
 						soyeulylich.setSohieucongchuc(newSoHieuCongChuc);
 						soyeulylich.setChungminhnhandan(newSoCMT);
 						soyeulylich.setGioitinh(newGioiTinh);
 						soyeulylich.setTenthuongdung(newTenThuongDung);
 						soyeulylich.setNoisinh(newNoiSinh);
 						soyeulylich.setNgaysinh(newNgaySinh);
 						soyeulylich.setQuequan(newQueQuan);
 						soyeulylich.setNoiohiennay(newNoiO);
 						soyeulylich.setSodienthoai(newSoDT);
 						soyeulylich.setNgayvaodangchinhthuc(newNgayVaoDang);
 						soyeulylich.setNgaycapnhat(newNgayCapNhat);
 						soyeulylich.setSuckhoe(newSucKhoe);
 						soyeulylich.setNgonngubiet(newNgonNguBiet);
 						staffController.suaSoYeuLiLich(soyeulylich);
 						modelAndView.addObject("updateSoYeuOK",
 								"Thêm cán bộ thành công!");
 					}
 					modelAndView.addObject("hocham",
 							hochamHome.findByExample(new Hocham()));
 					modelAndView.addObject("hocvi",
 							hocviHome.findByExample(new Hocvi()));
 					modelAndView.addObject("chucvu",
 							chucvuHome.findByExample(new Chucvu()));
 					modelAndView.addObject("capuy",
 							capuyHome.findByExample(new Capuy()));
 					modelAndView.addObject("giaoducphothong",
 							giaoducphothongHome
 									.findByExample(new Giaoducphothong()));
 					modelAndView.addObject("danhhieu",
 							danhhieuHome.findByExample(new Danhhieu()));
 					modelAndView.addObject("xuatthan",
 							xuatthanHome.findByExample(new Xuatthan()));
 					modelAndView.addObject("quocgia",
 							quocgiaHome.findByExample(new Quocgia()));
 					modelAndView.addObject("dantoc",
 							dantocHome.findByExample(new Dantoc()));
 					modelAndView.addObject("tongiao",
 							tongiaoHome.findByExample(new Tongiao()));
 
 				} else if (uri.contains("quanly/khenthuong")) {
 					if (uri.contains("khenthuong/xoakhenthuong")) {
 						KhenthuongHome khenthuongHome = new KhenthuongHome();
 						if (arg0.getParameter("idkhenthuong") != null) {
 							Integer idkhenthuong = Integer.parseInt(arg0
 									.getParameter("idkhenthuong"));
 							Khenthuong khenthuong = khenthuongHome
 									.findById(idkhenthuong);
 							khenthuongHome.delete(khenthuong);
 							khenthuongHome.getSessionFactory()
 									.getCurrentSession().flush();
 						}
 					} else if (uri.contains("khenthuong/themkhenthuong")) {
 
 					}
 					phongbanHome = new PhongbanHome();
 					Phongban phongban = phongbanHome.findById(idphongban);
 					modelAndView = new ModelAndView("thongke_khenthuong");
 					List<Donviquanly> donviquanly = guestController.TimDVQL(0,
 							0, null);
 					ArrayList<Khenthuong> khenthuong = new ArrayList<Khenthuong>();
 					Set<Soyeulylich> dscanbo = phongban.getSoyeulyliches();
 					for (Soyeulylich canbo : dscanbo) {
 						khenthuong.addAll(canbo.getKhenthuongs());
 					}
 
 					modelAndView.addObject("donviquanly", donviquanly);
 					modelAndView.addObject("phongban", phongban);
 					modelAndView.addObject("khenthuong", khenthuong);
 				} else if (uri.contains("quanly/kyluat")) {
 					if (uri.contains("kyluat/xoakyluat")) {
 						KhenthuongHome khenthuongHome = new KhenthuongHome();
 						if (arg0.getParameter("idkyluat") != null) {
 							Integer idkhenthuong = Integer.parseInt(arg0
 									.getParameter("idkyluat"));
 							Khenthuong khenthuong = khenthuongHome
 									.findById(idkhenthuong);
 							khenthuongHome.delete(khenthuong);
 							khenthuongHome.getSessionFactory()
 									.getCurrentSession().flush();
 						}
 					} else if (uri.contains("kyluat/themkyluat")) {
 
 					}
 					phongbanHome = new PhongbanHome();
 					Phongban phongban = phongbanHome.findById(idphongban);
 					modelAndView = new ModelAndView("thongke_kyluat");
 					List<Donviquanly> donviquanly = guestController.TimDVQL(0,
 							0, null);
 					ArrayList<Khenthuong> kyluat = new ArrayList<Khenthuong>();
 					Set<Soyeulylich> dscanbo = phongban.getSoyeulyliches();
 					for (Soyeulylich canbo : dscanbo) {
 						kyluat.addAll(canbo.getKyluats());
 					}
 
 					modelAndView.addObject("donviquanly", donviquanly);
 					modelAndView.addObject("phongban", phongban);
 				} else if (uri.contains("quanly/baocao")) {
 					Phongban phongban = phongbanHome.findById(idphongban);
 					modelAndView = new ModelAndView("report.spms");
 					List<Donviquanly> donviquanly = guestController.TimDVQL(0,
 							0, null);
 					modelAndView.addObject("donviquanly", donviquanly);
 					modelAndView.addObject("phongban", phongban);
 				}
 			}
 
 			Integer idcanbo = null;
 			if (arg0.getParameter("idcanbo") != null) {
 				idcanbo = Integer.parseInt(arg0.getParameter("idcanbo"));
 				SoyeulylichHome soyeulylichHome = new SoyeulylichHome();
 				Taikhoandangnhap curentUser = (Taikhoandangnhap) soyeulylichHome
 						.findById(idcanbo).getTaikhoandangnhaps();
 				if ((taikhoandangnhap.getSoyeulylich().getIdsoyeulylich() != idcanbo)
 						&& (taikhoandangnhap.getPermission() <= curentUser
 								.getPermission())) {
 					modelAndView = new ModelAndView("errorPage");
 					return modelAndView;
 				} else {
 					System.out.println("uri " + uri);
 					if (uri.contains("capnhat/xoacanbo")) {
 						String type = arg0.getParameter("deleteType");
 						if (type.equals("0")) {
 							Soyeulylich soyeulylich = soyeulylichHome
 									.findById(idcanbo);
 							Phongban phongban = soyeulylich.getPhongban();
 							soyeulylichHome.delete(soyeulylich);
 							soyeulylichHome.getSessionFactory()
 									.getCurrentSession().flush();
 							modelAndView = new ModelAndView("quanly_canbo");
 							List<Donviquanly> donviquanly = guestController
 									.TimDVQL(0, 0, null);
 							modelAndView.addObject("donviquanly", donviquanly);
 							modelAndView.addObject("phongban", phongban);
 						} else if (type.equals("1")) {
 							Soyeulylich soyeulylich = soyeulylichHome
 									.findById(idcanbo);
 							Donviquanly donvi = soyeulylich.getPhongban()
 									.getDonviquanly();
 							soyeulylichHome.delete(soyeulylich);
 							soyeulylichHome.getSessionFactory()
 									.getCurrentSession().flush();
 							modelAndView = new ModelAndView("quanly_canboDV");
 							List<Donviquanly> donviquanly = guestController
 									.TimDVQL(0, 0, null);
 							System.out.println("don vi" + donvi.getChucnang());
 							modelAndView.addObject("donviquanly", donviquanly);
 							modelAndView.addObject("donvi", donvi);
 						}
 
 					} else if (uri.contains("thongtin/soyeulylich")) {
 						modelAndView = new ModelAndView("xem_TTcanbo");
 						List<Donviquanly> donviquanly = guestController
 								.TimDVQL(0, 0, null);
 						modelAndView.addObject("donviquanly", donviquanly);
 						modelAndView.addObject("canbo",
 								guestController.TimCB(idcanbo));
 					} else if (uri.contains("thongtin/lylichkhoahoc")) {
 						modelAndView = new ModelAndView("lylichKH");
 						List<Donviquanly> donviquanly = guestController
 								.TimDVQL(0, 0, null);
 						modelAndView.addObject("donviquanly", donviquanly);
 						modelAndView.addObject("canbo",
 								guestController.TimCB(idcanbo));
 					} else if (uri.contains("thongtin/dienbienluong")) {
 						modelAndView = new ModelAndView("dienbienluong");
 						List<Donviquanly> donviquanly = guestController
 								.TimDVQL(0, 0, null);
 						modelAndView.addObject("donviquanly", donviquanly);
 						modelAndView.addObject("canbo",
 								guestController.TimCB(idcanbo));
 					} else if (uri.contains("thongtin/khenthuong")) {
 						modelAndView = new ModelAndView("khenthuong");
 						List<Donviquanly> donviquanly = guestController
 								.TimDVQL(0, 0, null);
 						modelAndView.addObject("donviquanly", donviquanly);
 						modelAndView.addObject("canbo",
 								guestController.TimCB(idcanbo));
 					} else if (uri.contains("thongtin/kyluat")) {
 						modelAndView = new ModelAndView("kyluat");
 						List<Donviquanly> donviquanly = guestController
 								.TimDVQL(0, 0, null);
 						modelAndView.addObject("donviquanly", donviquanly);
 						modelAndView.addObject("canbo",
 								guestController.TimCB(idcanbo));
 					}
 				}
 			}
 			if (uri.contains("search")) {
 				modelAndView = new ModelAndView("timkiem");
 				List<Donviquanly> donviquanly = guestController.TimDVQL(0, 0,
 						null);
 				modelAndView.addObject("donviquanly", donviquanly);
 				String Vien = arg0.getParameter("vien");
 				String PhongBan = arg0.getParameter("phongban");
 				String LoaiCB = arg0.getParameter("loaiCB");
 				String tenCb = arg0.getParameter("tenCanBo");
 				if ((tenCb != "") || (Vien != null) || (PhongBan != null)) {
 					Integer idVien, idPhongBan, loaiCB;
 					if (Vien == null) {
 						idVien = 0;
 					} else {
 						idVien = Integer.parseInt(Vien);
 					}
 
 					if (PhongBan == null) {
 						idPhongBan = 0;
 					} else {
 						idPhongBan = Integer.parseInt(PhongBan);
 					}
 
 					if (LoaiCB == null) {
 						loaiCB = 0;
 					} else {
 						loaiCB = Integer.parseInt(LoaiCB);
 					}
 					System.out.println("id vien = " + idVien
 							+ "id phong ban = " + idPhongBan + "tencanbo = "
 							+ tenCb);
 					List<Soyeulylich> danhsachcanbo = guestController.TimCB(
 							idVien, idPhongBan, ((tenCb == "") ? null : tenCb),
 							loaiCB);
 					modelAndView.addObject("result", danhsachcanbo);
 				}
 				modelAndView = setLink(modelAndView);
 			} else if (uri.contains("info")) {
 				modelAndView = new ModelAndView("info");
 				List<Donviquanly> donviquanly = guestController.TimDVQL(0, 0,
 						null);
 				modelAndView.addObject("donviquanly", donviquanly);
 				modelAndView = setLink(modelAndView);
 			} else if (uri.contains("contact")) {
 				modelAndView = new ModelAndView("contact");
 				List<Donviquanly> donviquanly = guestController.TimDVQL(0, 0,
 						null);
 				modelAndView.addObject("donviquanly", donviquanly);
 				modelAndView = setLink(modelAndView);
 			}
 		}
 
 		return modelAndView;
 	}
 
 	private ModelAndView setLink(ModelAndView view) {
 		view.addObject("homePage", "/k54/home.spms");
 		view.addObject("search", "/k54/manager/search.spms");
 		view.addObject("info", "/k54/manager/info.spms");
 		view.addObject("contact", "/k54/manager/contact.spms");
 		return view;
 	}
 }
