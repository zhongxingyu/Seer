 package product.model;
 
 import hibernate.util.HibernateUtil;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 
 public class ProductDAO implements ProductDAO_interface {
 	private static final String GET_ALL_STMT = "from ProductVO order by p_no";
 
 	@Override
 	public void insert(ProductVO productVO) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			session.saveOrUpdate(productVO);
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 	}
 
 	@Override
 	public void update(ProductVO productVO) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			session.saveOrUpdate(productVO);
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 	}
 
 	@Override
 	public void delete(Integer p_no) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			ProductVO productV = new ProductVO();
 			productV.setP_no(p_no);
 			session.delete(productV);
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 
 	}
 
 	@Override
 	public ProductVO findByPrimaryKey(Integer p_no) {
 		ProductVO ProductVO = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			ProductVO = (ProductVO) session.get(ProductVO.class, p_no);
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return ProductVO;
 	}
 
 	@Override
 	public List<ProductVO> getAll() {
 		List<ProductVO> list = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery(GET_ALL_STMT);
 			list = query.list();
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 
 	@Override
 	public List<String> findProductType(String productlist) {
 		List list = new ArrayList();
 		List list1 = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery("SELECT DISTINCT " + productlist
 					+ "  FROM ProductVO");
 			query.setCacheable(true); //啟動Query快取
 			list1 = query.list();
 			int list1_size=list1.size();
 			for (int i = 0; i < list1_size; i++) {
 				String aaa = list1.get(i).toString();
 
 				list.add(aaa);
 			}
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 
 	public List<String> findGrapeType(String productType) {
 		List list = null;
 
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 		try {
 			session.beginTransaction();
 
 			Query query = session
 					.createQuery("SELECT DISTINCT p_grape FROM ProductVO where p_type=?");
 			query.setCacheable(true); //啟動Query快取
 			query.setParameter(0, productType);
 			list = query.list();
 
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 
 	@Override
 	public List<ProductVO> findSomeProduct(String condition,
 			String conditionValue) {
 		Integer values;
 
 		String GET_SOME_STMT = "from ProductVO Where " + condition + " =? ";
 		List<ProductVO> list = null;
 		List list1 = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery(GET_SOME_STMT);
 			if ((condition.equals("p_vol")) || (condition.equals("p_price)"))) {
 				values = (Integer) Integer.parseInt(conditionValue);
 				query.setParameter(0, values);
 				list = query.list();
 			} else {
 				query.setParameter(0, conditionValue);
 				list = query.list();
 			}
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 	public List<ProductVO> findSalesProduct() {
 
 		String GET_SALES_STMT = "from ProductVO where p_sales <>'NONE'";
 		List<ProductVO> list = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery(GET_SALES_STMT);
 			list = query.list();
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 
 	public List<ProductVO> findTopProduct(String conditionValue) {
 
 		String GET_TOP_STMT = "from ProductVO order by p_buy_count desc";
 		List<ProductVO> list = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery(GET_TOP_STMT);
 			query.setCacheable(true); //啟動Query快取
 			query.setFirstResult(0);
 			query.setMaxResults(Integer.parseInt(conditionValue));
 
 			list = query.list();
 			session.getTransaction().commit();
 	
 		}
 
 		catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
	public List<ProductVO> getTopProduct() {

		String GET_TOP_STMT = "from ProductVO order by p_buy_count desc";
		List<ProductVO> list = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			Query query = session.createQuery(GET_TOP_STMT);
//			query.setCacheable(true); //啟動Query快取
			query.setFirstResult(0);
			query.setMaxResults(10);

			list = query.list();
			session.getTransaction().commit();
	
		}

		catch (RuntimeException ex) {
			session.getTransaction().rollback();
			throw ex;
		}
		return list;
	}
	
 	public List<ProductVO> findRandTopProduct(String conditionValue) {
 			//隨機搜尋幾筆商品出來
 		int rndNumber=(int)(Math.random()*10+1);
 		String GET_TOP_STMT = "from ProductVO order by " +rndNumber;
 		List<ProductVO> list = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery(GET_TOP_STMT);
 			query.setFirstResult(0);
 			query.setMaxResults(Integer.parseInt(conditionValue));
 
 			list = query.list();
 			session.getTransaction().commit();
 	
 		}
 
 		catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 	
 	public List<String> findCountry(String area) {
 		List list = null;
 
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 		try {
 			session.beginTransaction();
 
 			Query query = session
 					.createQuery("SELECT DISTINCT p_country FROM ProductVO where p_area=?");
 			query.setCacheable(true); //啟動Query快取
 			query.setParameter(0, area);
 			list = query.list();
 
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 	public List<ProductVO> findProductBetween(String condition,
 			String conditionValue) {
 		Integer values;
 
 		String GET_SOME_STMT = "from ProductVO Where " + condition + " between ? and ? ";
 		List<ProductVO> list = null;
 		List list1 = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery(GET_SOME_STMT);
 			System.out.println(condition);
 			Integer value=Integer.parseInt(conditionValue);
 			if ((condition.equals("p_price"))) {
 				
 				if(value==0){
 					query.setParameter(0,value);
 					query.setParameter(1,1000);
 					list = query.list();
 				}
 				else if(value==1000){
 					query.setParameter(0,value);
 					query.setParameter(1,2000);
 					list = query.list();
 				}
 				else{
 					query.setParameter(0,2000);
 					query.setParameter(1,65535);}
 				list = query.list();
 				
 			} else {
 				
 				Timestamp now =new Timestamp(System.currentTimeMillis());
 				Timestamp oneMonth =new Timestamp(System.currentTimeMillis()-21*24*3600*1000-10*24*3600*1000);
 				System.out.println(now);
 				System.out.println(oneMonth);
 				query.setParameter(0,oneMonth);
 				query.setParameter(1,now);
 				list = query.list();
 			}
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 	@Override
 	public List<ProductVO> findFuzzyProductName(String fuzzyProductName) {
 		List<ProductVO> list = new ArrayList();
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery("FROM ProductVO where p_name like '%" + fuzzyProductName + "%' order by p_no");
 			list = query.list();
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 	//------------------------------------------------------------------------
 	public List<ProductVO> findSpeciallySalesProduct() {
 		List<ProductVO> list = new ArrayList();
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 		try {
 			session.beginTransaction();
 			//  select * from product where p_sales not in('A','B','TIME') order by p_no
 			Query query = session.createQuery(" FROM ProductVO where p_sales NOT IN(?,?,?) order by p_no");
 			query.setParameter(0,"A");
 			query.setParameter(1,"B");
 			query.setParameter(2,"C");
 			list = query.list();
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 	//--------------------------------------------------------------------------
 	public List<ProductVO> getProductSales(String p_sales) {
 		List<ProductVO> list = new ArrayList();
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery("FROM ProductVO where p_sales = '" + p_sales + "' order by p_no");
 			list = query.list();
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 	
 	public Integer getEmergnecyStorage() {
 		Integer storageNum ;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			SQLQuery query = session
 					.createSQLQuery("select COUNT(p_no) from product where p_num < 24");
 			storageNum = (Integer)query.uniqueResult();
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return storageNum;
 	}
 	
 	public List<ProductVO> getEmergencyProduct() {
 		List<ProductVO> list = new ArrayList();
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 		try {
 			session.beginTransaction();
 			Query query = session.createQuery("FROM ProductVO where p_num < 24  order by p_num");
 			list = query.list();
 			session.getTransaction().commit();
 		} catch (RuntimeException ex) {
 			session.getTransaction().rollback();
 			throw ex;
 		}
 		return list;
 	}
 	
 	// -------------------------------------------------------
 	public static void main(String arg[]) {
 		ProductDAO dao = new ProductDAO();
 //		List<ProductVO> list = dao.findProductBetween("p_date", "0");
 //		 List<ProductVO> list = dao.findRandTopProduct("4");
 		List<ProductVO> list = dao.findSpeciallySalesProduct();
 				 for (ProductVO aEmp : list) {
 //				 System.out.println(aEmp.getP_no() + ",");
 //				 System.out.println(aEmp.getP_name() + ",");
 //				 System.out.println(aEmp.getP_pic() + ",");
 //				 System.out.println(aEmp.getP_year() + ",");
 //				 System.out.println(aEmp.getP_rate() + ",");
 //				 System.out.println(aEmp.getP_area() + ",");
 //				 System.out.println(aEmp.getP_intro() + ",");
 //				 System.out.println(aEmp.getP_num() + ",");
 //				 System.out.println(aEmp.getP_price() + ",");
 //				 System.out.println(aEmp.getP_status() + ",");
 //				 System.out.println(aEmp.getP_winery() + ",");
 //				 System.out.println(aEmp.getP_click_count() + ",");
 //				 System.out.println(aEmp.getP_buy_count() + ",");
 //				 System.out.println(aEmp.getP_style() + ",");
 				 System.out.println(aEmp.getP_sales() + ",");
 //				 System.out.println(aEmp.getP_vol() + ",");
 //				 System.out.println(aEmp.getP_alcho() + ",");
 //				 System.out.println(aEmp.getP_date() + ",");
 //				 System.out.println(aEmp.getP_type() + ",");
 //				 System.out.println(aEmp.getP_grape());
 				 System.out.println();
 				 }
 		
 		
 		// List<ProductVO> list = dao.findSomeProduct("p_vol","750");
 		// for (ProductVO aEmp : list) {
 		// System.out.println(aEmp.getP_no() + ",");
 		// System.out.println(aEmp.getP_name() + ",");
 		// System.out.println(aEmp.getP_pic() + ",");
 		// System.out.println(aEmp.getP_year() + ",");
 		// System.out.println(aEmp.getP_rate() + ",");
 		// System.out.println(aEmp.getP_area() + ",");
 		// System.out.println(aEmp.getP_intro() + ",");
 		// System.out.println(aEmp.getP_num() + ",");
 		// System.out.println(aEmp.getP_price() + ",");
 		// System.out.println(aEmp.getP_status() + ",");
 		// System.out.println(aEmp.getP_winery() + ",");
 		// System.out.println(aEmp.getP_click_count() + ",");
 		// System.out.println(aEmp.getP_buy_count() + ",");
 		// System.out.println(aEmp.getP_style() + ",");
 		// System.out.println(aEmp.getP_sales() + ",");
 		// System.out.println(aEmp.getP_vol() + ",");
 		// System.out.println(aEmp.getP_alcho() + ",");
 		// System.out.println(aEmp.getP_date() + ",");
 		// System.out.println(aEmp.getP_type() + ",");
 		// System.out.println(aEmp.getP_grape());
 		// System.out.println();
 		// }
 		// List<String> listStyle = dao.findGrapeType("紅葡萄酒");
 		// for (String aEmp : listStyle) {
 		//
 		// System.out.println(aEmp);
 		// }
 		// 新增
 		// ProductVO productVOO1 = new ProductVO();
 		// productVOO1.setP_name("路易拉圖夏布利一級葡萄園白葡萄酒");
 		// productVOO1.setP_year(2008);
 		// productVOO1.setP_rate(90);
 		// productVOO1.setP_area("法國布根地夏布利");
 		// productVOO1
 		// .setP_intro("路易拉圖夏布利一級葡萄園白葡萄酒是由夏布利一級葡萄園內精選出最好的夏多內葡萄，所釀造出口味特別細緻清雅的白酒。路易拉圖酒廠經由降低葡萄的生產量和不斷進步的種植與釀造技術來確保每年葡萄品質的穩定。路易拉圖夏布利一級葡萄園白葡萄酒在不鏽鋼槽中發酵，以保留新鮮果味。此外，百分之百的乳酸發酵過程，讓酒質更加穩定，口感更加圓潤。家族代代傳承經營的路易拉圖酒廠 (Maison Louis Latour)，是法國布根地伯恩產區五大著名酒商之一，自1797年創立以來，一直堅守傳統和品質，但也勇於革新和創造，以其優良的布根地紅、白酒品質聞名世界，同時也是生產布根地產區以外如Ardche(阿德榭)的優質葡萄酒的先驅。拉圖家族自17世紀以來便開始擁有與增加自有葡萄園，如今在布根地已有50公頃的葡萄園，範圍。從北部的香貝丹(Chambertin)延伸到南邊的歇瓦里耶- 蒙哈榭(Chevalier-Montrachet)，其中有30公頃的特級葡萄園，包括0.8公頃的香貝丹、1公頃的侯馬內-聖維馮(Romanee Saint Vivant)、17公頃的高登(Corton) 以及近9公頃的高登查理曼(Corton-Charlemagne)，並是在金丘(Cote d'Or) 擁有最大比例的特級葡萄園的酒商。路易拉圖所有的自有葡萄園裡所生產的葡萄，皆在阿羅斯-高登 (Aloxe-Corton)的高登格蘭榭堡(Chteau Corton Grancey)裡的酒窖釀製與陳年。這是法國第一個依功能性建造的酒窖，仍維持傳統的運作方式，為固守傳統，至今路易拉圖所有自有葡萄園的酒全部使用人工踩皮，另一特色是紅酒較少使用新橡木桶陳年，而新橡木桶則用於白酒，ㄧ兩年後再使用於紅酒，與某些布根地酒廠方式相反。種種堅持，使路易拉圖白酒富有濃郁的果味與香草、榛果等香氣，紅酒則口感圓潤豐富。路易拉圖酒廠有許多其他與眾不同之處，例如擁有自己的橡木桶酒廠、印刷廠、船運公司，連瓶型也有專用形狀，堅持自己的風格。路易拉圖自第三代掌門人起，便是許多王公貴族的最愛，，在世界各大高級飯店與餐廳，如巴黎蒙地卡羅(Monte Carlo's)飯店、日內瓦Le Beau-Rivage、巴黎麗池飯店(The Paris Ritz)的酒單上都看得到路易拉圖，更榮選為2008年G8高峰會指定用久，是您不容錯過的布根地名廠。");
 		// productVOO1.setP_num(85);
 		// productVOO1.setP_price(1650.0);
 		// productVOO1.setP_status("上架中");
 		// productVOO1.setP_winery("Chardonnay");
 		// productVOO1.setP_click_count(80);
 		// productVOO1.setP_buy_count(90);
 		// productVOO1.setP_style("土味");
 		// productVOO1.setP_sales("RG");
 		// productVOO1.setP_vol(750);
 		// productVOO1.setP_alcho(13.0);
 		// productVOO1.setP_date(new Timestamp(new java.util.Date().getTime()));
 		// productVOO1.setP_type("白葡萄酒");
 		// productVOO1.setP_grape("Pinot Noir");
 		// dao.insert(productVOO1);
 		// -------------------------------------------------
 		// 修改
 		// ProductVO productVOO2 = new ProductVO();
 		// productVOO2.setP_no(1);
 		// productVOO2.setP_name("傑卡斯臻藏巴洛沙席哈紅酒");
 		// productVOO2.setP_year(2008);
 		// productVOO2.setP_rate(80);
 		// productVOO2.setP_area("澳洲");
 		// productVOO2
 		// .setP_intro("得2005 Taster Guide International Wine Judging等主要國際競賽肯定,在1847年，一位年輕的巴伐利亞移民約翰.葛蘭（Johann Gramp）來到了南澳的巴洛沙河谷，在一條名為傑卡斯溪畔種下了他的第一棵葡萄樹。經過129年的穩定發展，1976年正式以『傑卡斯』為品牌名稱在全球上市，時至今日，每年超過8百萬箱的出口量，讓傑卡斯成為澳洲最受歡迎的葡萄酒廠。每天，近2百萬杯的傑卡斯葡萄酒在世界各地的人們手中被享用，質優易飲的傑卡斯風格深植人心。用於臻藏系列的葡萄，採自精選葡萄園的果實，釀酒師精選來自Padthaway, Langhorne Creek, Coonawarra以及克來兒谷地等地區，果實充分熟成富有濃郁品種風味與細緻單寧的葡萄，作為釀造臻藏系列的來源。香氣集中、濃縮風味，在橡木桶內熟成18-22個月（席哈紅酒）。");
 		// productVOO2.setP_num(50);
 		// productVOO2.setP_price(800.0);
 		// productVOO2.setP_status("下架中");
 		// productVOO2.setP_winery("Jacob’s Creek");
 		// productVOO2.setP_click_count(100);
 		// productVOO2.setP_buy_count(150);
 		// productVOO2.setP_style("辛辣味");
 		// productVOO2.setP_sales("B");
 		// productVOO2.setP_vol(750);
 		// productVOO2.setP_alcho(14.5);
 		// productVOO2.setP_date(new Timestamp(new java.util.Date().getTime()));
 		// productVOO2.setP_type("red wine");
 		// productVOO2.setP_grape("Syrah \\ Shiraz");
 		// dao.update(productVOO2);
 		// ---------------------------------------------
 		// 刪除
 		// dao.delete(5);
 		// ------------------------------------------------
 		// 查詢
 
 		// ProductVO productVOO3 = dao.findByPrimaryKey(2);
 		// System.out.println(productVOO3.getP_no() + ",");
 		// System.out.println(productVOO3.getP_name() + ",");
 		// System.out.println(productVOO3.getP_year() + ",");
 		// System.out.println(productVOO3.getP_rate() + ",");
 		// System.out.println(productVOO3.getP_area() + ",");
 		// System.out.println(productVOO3.getP_intro() + ",");
 		// System.out.println(productVOO3.getP_num() + ",");
 		// System.out.println(productVOO3.getP_price() + ",");
 		// System.out.println(productVOO3.getP_status() + ",");
 		// System.out.println(productVOO3.getP_winery() + ",");
 		// System.out.println(productVOO3.getP_click_count() + ",");
 		// System.out.println(productVOO3.getP_buy_count() + ",");
 		// System.out.println(productVOO3.getP_style() + ",");
 		// System.out.println(productVOO3.getP_sales() + ",");
 		// System.out.println(productVOO3.getP_vol() + ",");
 		// System.out.println(productVOO3.getP_alcho() + ",");
 		// System.out.println(productVOO3.getP_date() + ",");
 		// System.out.println(productVOO3.getP_type() + ",");
 		// System.out.println(productVOO3.getP_grape() );
 		// System.out.println();
 		// 查詢
 		// List<ProductVO> list = dao.getAll();
 		// for (ProductVO aEmp : list) {
 		// System.out.println(aEmp.getP_no() + ",");
 		// System.out.println(aEmp.getP_name() + ",");
 		// System.out.println(aEmp.getP_pic() + ",");
 		// System.out.println(aEmp.getP_year() + ",");
 		// System.out.println(aEmp.getP_rate() + ",");
 		// System.out.println(aEmp.getP_area() + ",");
 		// System.out.println(aEmp.getP_intro() + ",");
 		// System.out.println(aEmp.getP_num() + ",");
 		// System.out.println(aEmp.getP_price() + ",");
 		// System.out.println(aEmp.getP_status() + ",");
 		// System.out.println(aEmp.getP_winery() + ",");
 		// System.out.println(aEmp.getP_click_count() + ",");
 		// System.out.println(aEmp.getP_buy_count() + ",");
 		// System.out.println(aEmp.getP_style() + ",");
 		// System.out.println(aEmp.getP_sales() + ",");
 		// System.out.println(aEmp.getP_vol() + ",");
 		// System.out.println(aEmp.getP_alcho() + ",");
 		// System.out.println(aEmp.getP_date() + ",");
 		// System.out.println(aEmp.getP_type() + ",");
 		// System.out.println(aEmp.getP_grape());
 		// System.out.println();
 		// }
 				 
 				 
 //		list = dao.getProductSales("A");
 //		
 //		System.out.println(list.size());
 //		 for (ProductVO aEmp : list) {
 //				 System.out.println(aEmp.getP_no() + ",");
 //				 System.out.println(aEmp.getP_name() + ",");
 //				 System.out.println(aEmp.getP_pic() + ",");
 //				 System.out.println(aEmp.getP_year() + ",");
 //				 System.out.println(aEmp.getP_rate() + ",");
 //				 System.out.println(aEmp.getP_area() + ",");
 //				 System.out.println(aEmp.getP_intro() + ",");
 //				 System.out.println(aEmp.getP_num() + ",");
 //				 System.out.println(aEmp.getP_price() + ",");
 //				 System.out.println(aEmp.getP_status() + ",");
 //				 System.out.println(aEmp.getP_winery() + ",");
 //				 System.out.println(aEmp.getP_click_count() + ",");
 //				 System.out.println(aEmp.getP_buy_count() + ",");
 //				 System.out.println(aEmp.getP_style() + ",");
 //				 System.out.println(aEmp.getP_sales() + ",");
 //		 }
 		
 //		Integer a = dao.getEmergnecyStorage();
 //		System.out.println("達到緊張庫存(24隻以下)之商品總共有 = " + a + "隻");
 //		
 //		List<ProductVO> list = dao.getEmergencyProduct();
 //		System.out.println(list.size());
 		
 	}
 
 }
