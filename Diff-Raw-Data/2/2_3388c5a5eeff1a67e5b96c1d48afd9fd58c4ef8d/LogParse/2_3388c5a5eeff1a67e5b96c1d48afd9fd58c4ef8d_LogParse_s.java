 package com.emar.recsys.user.log;
 
 import java.lang.reflect.Field;
 import java.net.URI;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 
 import com.emar.recsys.user.log.LogNameParse.LOG_PLAT;
 import com.emar.recsys.user.log.LogNameParse.LOG_TYPE;
 
 /**
  * 通用日志解析类
  * @author zhoulm
  * @Done UT,  TYPE=[pv, click, order, browse, goodsview, bidreq, search].
  */
 public class LogParse {
 	public static String EMAR = "emar", PLAT = "plat", EQUEA = "=";
 	public static String MAGIC = "@@@", 
 			SEPA = "\u0001", SEPA_MR = "\t", SEPA_CAMPS = ",";
 	
 	public BaseLog base;
 	
 	public LogNameParse logpath;
 	
 	private Field field;
 	/*
 	// 可能的处理日志的类型
 	private static String[] type = {
 		"pv", "click", "order", 
 		"browse", "event", // emarbox
 //		"cookie", 
 		"search",  // gouwuke 
 		"bidreq", "biddetail"
 	}; 
 	private static String[] plat = {
 		"yiqifa", "gouwuke", "yigao", "emarbox", "rtb",
 		"chuchuang"  //yiqifa
 	};
 	*/
 	public LogParse() throws ParseException {
 		// 用无效内存创建对象
 		base =new BaseLog("");
 		logpath = new LogNameParse("");
 	}
 	public void reset() {
 		base.reset();
 //		logpath.reset();
 	}
 	/**
 	 * 按 统一格式构造 日志的KEY 
 	 * @return
 	 */
 	public String buildUidKey() {
 		if (!this.base.status) 
 			return null;
 		
 		String res;
 		final int LenMin = 5;
 		if(base.user_id != null && base.user_id.length() > LenMin) 
 			res = EMAR + MAGIC + base.user_id + MAGIC + EMAR;//@Mod 不再区别平台
 		else if(base.plat_user_id != null && base.plat_user_id.length() > LenMin)
 			res = PLAT + MAGIC + base.plat_user_id + MAGIC + logpath.plat;
 		// 增加两种平台ID
 //		else if(base.user_cookieid != null && base.user_cookieid.length() > LenMin)
 //			res = PLAT + MAGIC + base.user_cookieid + MAGIC + logpath.plat;//解析时即合并为plat_user_id.
 		else if(base.ip != null && base.ip.split(".").length == 4)
 			res = PLAT + MAGIC + base.ip + MAGIC + logpath.plat;
 		else
 			res = null;
 		return res;
 	}
 	
 	public String getTime() {
 		if (!this.base.status) 
 			return null;
 		
 		String time = null;
 		time = this.base.time;
		if (time.length() == 0)
 			time = null;  // 不容许为空串
 		if (time == null && this.logpath.status)
 			time = this.logpath.date + this.logpath.hour + "0000";
 		return time;
 	}
 	
 	public void parse(String line, String path) throws ParseException {
 		logpath.fill(path);
 		
 		if(logpath.status) { 
 			// 行为类别优先
 			switch(logpath.type) {
 			case pv:
 				switch(logpath.plat) {
 				case chuchuang:
 					// 橱窗的日志格式单独处理
 					this.base.ChuPVParse(line);
 					break;
 				default:
 					this.base.PVParse(line);
 					break;
 				}
 				break;
 			case click:
 				switch (logpath.plat) {
 				case chuchuang:
 					this.base.ChuClickParse(line);
 					break;
 				default:
 					this.base.ClickParse(line);
 					break;
 				}
 				break;
 			case order:
 				this.base.OrderParse(line);
 				break;
 			case browse:
 				this.base.EmarboxBrowse(line);
 				break;
 			case goodsview:
 				this.base.EmarboxBProd(line);
 				break;
 			case bidreq:
 				this.base.BidRequest(line);
 				break;
 			case search:
 				this.base.Search(line);
 				break;
 //			case // other data.
 			default:  // all other data.
 				System.out.println("[Info] unsupport LOG type.");
 				break;
 			}
 			 
 		}
 		
 	}
 	
 	public String toString() { 
 		if(this.base != null && this.logpath != null)
 			return String.format("baseparse=[%s]\tpathparse=[%s]", this.base.toString(), 
 					this.logpath.toString());
 		else 
 			return null;
 	}
 	
 	/**
 	 * 基于反射动态获取对应的 字段的值
 	 * @param fname
 	 * @return
 	 */
 	public Object getField(String fname) {
 		Object res = null;
 		if(fname == null) {
 			return res;
 		}
 		try {
 			field = BaseLog.class.getField(fname);
 			res = field.get(this.base);
 		} catch (NoSuchFieldException e1) {
 			try {
 				field = LogNameParse.class.getField(fname);
 				res = field.get(this.logpath);
 			} catch(NoSuchFieldException e2) {
 				try {
 					field = LogParse.class.getField(fname);
 					res = field.get(this);
 				} catch(NoSuchFieldException e3) {
 				} catch (Exception e4) {
 					e4.printStackTrace();
 				}
 			} catch (Exception e5) {
 				e5.printStackTrace();
 			}
 		} catch (Exception e6) {
 			e6.printStackTrace();
 		}
 		return res;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		FSDataInputStream in = null;
 		Configuration conf = new Configuration();
 		FileSystem fs = null;
 		String[] input = new String[] {
 			// plat=11
 			"/data/stg/s_sw_pv_log/20130626/i_chuchuang_pv_20130626_22.dat",
 			"/data/stg/s_sw_click_log/20130626/i_chuchuang_click_20130626_22.dat",
 			// plat = 2
 			"/data/stg/s_ad_pv_log/20130522/2/i_yigao_pv_20130522_00.dat",
 			// plat = 3
 			"/data/stg/s_browse_log/20130606/3/i_emarbox_browse_20130606_20.dat",
 			"/data/stg/s_browse_prod_log/20130606/3/i_emarbox_goodsview_20130606_22.dat",
 			// plat=1,2 order 中无 2
 			"/data/stg/s_ad_click_log/20130522/1/i_yiqifa_click_20130522_00.dat",
 			"/data/stg/s_order_log/20130522/1/i_yiqifa_order_20130522_00.dat",
 			
 			// plat=4
 			"/data/stg/s_search_log/20130522/4/i_gouwuke_search_20130522_23.dat",
 			// plat=RTB 
 			"/data/stg/s_adx_request_log/20130522/i_rtb_bidreq_22_20130522.dat",
 			"/data/stg/s_camp_succ_log/20130606/i_dsp_monitor_22_20130606.dat"
 		};
 		List<String> oneline = new ArrayList<String>(10);
 		String lineccpv = "5KiifgIQoCqqzzzooqQQSkphwXLVzpoYRzoQrJEQoCqqzzzooqQTBK1ObN1J5T76http://p.yigao.com/imprImg.jsp?tc=&cc=&bgc=&bc=D9D9D9&tb=0&cb=0&tu=0&cu=0&uid=79787&zid=143842&pid=6&w=300&h=250&t=1&a=1&c=1&sid=395bcbeea2b11dfc&ua=Mozilla/5.0%20%28Windows%20NT%206.1%3B%20WOW64%29%20AppleWebKit/537.17%20%28KHTML%2C%20like%20Gecko%29%20Chrome/24.0.1312.57%20Safari/537.17&n=Netscape&p=http:&v=5.0%20%28Windows%20NT%206.1%3B%20WOW64%29%20AppleWebKit/537.17%20%28KHTML%2C%20like%20Gecko%29%20Chrome/24.0.1312.57%20Safari/537.17&r=http%3A//www.baidu.com/s%3Fwd%3D%25E6%2598%25A5%25E5%25A8%2587%25E4%25B8%258E%25E5%25BF%2597%25E6%2598%258E2%26rsv_bp%3D0%26ch%3D%26tn%3Dbaidu%26bar%3D%26rsv_spt%3D3%26ie%3Dutf-8%26rsv_sug3%3D2%26rsv_sug4%3D130%26rsv_sug1%3D1%26oq%3D%25E6%2598%25A5%25E5%25A8%2587%26rsp%3D1%26f%3D3%26rsv_sug5%3D0%26rsv_sug%3D0%26rsv_sug2%3D0%26inputT%3D5047&ho=www.tangdou.com&l=http%3A//www.tangdou.com/html/movie/201204/20120420_412373.html&ax=0&ay=0&rx=0&ry=0&os=unknown&scr=1440_900&ck=true&s=1&ww=1029&wh=628&ym=&fs=0&pvid=19bb5cc7222459a44a3707942f258023&yhc=&msid=7256b333d5b94333http://showad.gouwuke.com/windowControler.do?flag=sw&oid=451174&gsid=656970&sn=cctangdou.gouwuke.com&winid=26510&modelName=300-250Y&euid=143842Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17124.231.39.227201306262200002651037878,37880,37882,37884,37886,37888,37890,37892,37894,37896,37878,37880,37882,37884,37886,37888,37890,37892,37894,37896254,254,254,254,254,254,254,254,5227,5227,254,254,254,254,254,254,254,254,5227,5227";
 		String lineccc = "FDE7953741D24E778EDDFB09E835236BS1BPn2OQoCqqzzQzTToCDj663FYmRgQawrw4wchQoCqqzzQq3QCQqc1MQw07RGfxhttp://img.55bbs.com/55bbs/201305/iframe_auto_6809.htmlhttp://showad.gouwuke.com/windowControler.do?flag=sw&oid=451173&gsid=656969&sn=cc55bbs.gouwuke.com&winid=21927&modelName=728-90ZY&euid=tongpeiMozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0)27.217.66.272013062622000021927361224862";
 		String lygpv = "a4bfa05be036eaf98ee9df1db7f2d5ee3d2051f0-ab1f-3637-8b12-c1b4096bdbe2119.138.145.13720130522000000http://www.jjwxc.net/onebook.php?novelid=311645http://www.baidu.com/s?ie=utf-8&bs=%E4%B8%83%E8%89%B2%E4%B9%8B%E6%97%85&f=8&rsv_bp=1&wd=%E4%B8%83%E8%89%B2%E4%B9%8B%E6%97%85%E7%99%BD%E8%AF%A1%E8%93%9D%E5%BC%82&rsv_sug3=5&rsv_sug=0&rsv_sug4=299&rsv_sug1=2&inputT=28870Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; KB974488)113959385001122606";
 		oneline.add(lineccpv);
 		oneline.add(lineccc);
 		oneline.add(lygpv);
 		String[] eres = {  // 期望结果
 			"status=true	ad_modality=null,ad_zone_id=null,camp_id=null,camp_ids=254,254,254,254,254,254,254,254,5227,5227,254,254,254,254,254,254,254,254,5227,5227,click_id=null,domain=null,idea_id=null,ip=124.231.39.227,mate_ids=37878,37880,37882,37884,37886,37888,37890,37892,37894,37896,37878,37880,37882,37884,37886,37888,37890,37892,37894,37896,oper_time=null,order_id=null,order_no=null,order_price=null,orig_prod_type=null,page_url=http://p.yigao.com/imprImg.jsp?tc=&cc=&bgc=&bc=D9D9D9&tb=0&cb=0&tu=0&cu=0&uid=79787&zid=143842&pid=6&w=300&h=250&t=1&a=1&c=1&sid=395bcbeea2b11dfc&ua=Mozilla/5.0%20%28Windows%20NT%206.1%3B%20WOW64%29%20AppleWebKit/537.17%20%28KHTML%2C%20like%20Gecko%29%20Chrome/24.0.1312.57%20Safari/537.17&n=Netscape&p=http:&v=5.0%20%28Windows%20NT%206.1%3B%20WOW64%29%20AppleWebKit/537.17%20%28KHTML%2C%20like%20Gecko%29%20Chrome/24.0.1312.57%20Safari/537.17&r=http%3A//www.baidu.com/s%3Fwd%3D%25E6%2598%25A5%25E5%25A8%2587%25E4%25B8%258E%25E5%25BF%2597%25E6%2598%258E2%26rsv_bp%3D0%26ch%3D%26tn%3Dbaidu%26bar%3D%26rsv_spt%3D3%26ie%3Dutf-8%26rsv_sug3%3D2%26rsv_sug4%3D130%26rsv_sug1%3D1%26oq%3D%25E6%2598%25A5%25E5%25A8%2587%26rsp%3D1%26f%3D3%26rsv_sug5%3D0%26rsv_sug%3D0%26rsv_sug2%3D0%26inputT%3D5047&ho=www.tangdou.com&l=http%3A//www.tangdou.com/html/movie/201204/20120420_412373.html&ax=0&ay=0&rx=0&ry=0&os=unknown&scr=1440_900&ck=true&s=1&ww=1029&wh=628&ym=&fs=0&pvid=19bb5cc7222459a44a3707942f258023&yhc=&msid=7256b333d5b94333,plat_user_id=RzoQrJEQoCqqzzzooqQTBK1ObN1J5T76,prod_cnt=null,prod_name=null,prod_no=null,prod_price=null,prod_type_name=null,pv_id=5KiifgIQoCqqzzzooqQQSkphwXLVzpoY,refer_url=http://showad.gouwuke.com/windowControler.do?flag=sw&oid=451174&gsid=656970&sn=cctangdou.gouwuke.com&winid=26510&modelName=300-250Y&euid=143842,sw_id=26510,time=20130626220000,title=null,user_agent=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17,user_cookieid=RzoQrJEQoCqqzzzooqQTBK1ObN1J5T76,user_id=null",
 			"status=true	ad_modality=null,ad_zone_id=null,camp_id=null,camp_ids=4862,click_id=FDE7953741D24E778EDDFB09E835236B,domain=null,idea_id=null,ip=27.217.66.27,mate_ids=36122,oper_time=null,order_id=null,order_no=null,order_price=null,orig_prod_type=null,page_url=http://img.55bbs.com/55bbs/201305/iframe_auto_6809.html,plat_user_id=wrw4wchQoCqqzzQq3QCQqc1MQw07RGfx,prod_cnt=null,prod_name=null,prod_no=null,prod_price=null,prod_type_name=null,pv_id=S1BPn2OQoCqqzzQzTToCDj663FYmRgQa,refer_url=http://showad.gouwuke.com/windowControler.do?flag=sw&oid=451173&gsid=656969&sn=cc55bbs.gouwuke.com&winid=21927&modelName=728-90ZY&euid=tongpei,sw_id=21927,time=20130626220000,title=null,user_agent=Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0),user_cookieid=wrw4wchQoCqqzzQq3QCQqc1MQw07RGfx,user_id=null",
 			"status=true	ad_modality=1,ad_zone_id=139593,camp_id=85001,camp_ids=null,click_id=null,domain=null,idea_id=122606,ip=119.138.145.137,mate_ids=null,oper_time=20130522000000,order_id=null,order_no=null,order_price=null,orig_prod_type=null,page_url=http://www.jjwxc.net/onebook.php?novelid=311645,plat_user_id=3d2051f0-ab1f-3637-8b12-c1b4096bdbe2,prod_cnt=null,prod_name=null,prod_no=null,prod_price=null,prod_type_name=null,pv_id=a4bfa05be036eaf98ee9df1db7f2d5ee,refer_url=http://www.baidu.com/s?ie=utf-8&bs=%E4%B8%83%E8%89%B2%E4%B9%8B%E6%97%85&f=8&rsv_bp=1&wd=%E4%B8%83%E8%89%B2%E4%B9%8B%E6%97%85%E7%99%BD%E8%AF%A1%E8%93%9D%E5%BC%82&rsv_sug3=5&rsv_sug=0&rsv_sug4=299&rsv_sug1=2&inputT=28870,sw_id=null,time=null,title=null,user_agent=Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; KB974488),user_cookieid=null,user_id=null"
 		};
 		
 		try {
 			LogParse lparse = new LogParse();
 
 			for(int i = 0; i < oneline.size(); ++i) {
 				lparse.base.isdebug = false;
 				lparse.parse(oneline.get(i), input[i]);
 				lparse.parse("86175b52-2432-4bd3-93a8-0db57dae4690\u000113704561999337850194\u00012013060606296932\u000128159303\u0001Zegda正大 夏装新品 都市休闲男条纹短袖POLO衫CB122H25\u000179.0\u00011\u00017\u00017\u000179.0\u00016954\u000120130606040041\u0001ule.com", "i_yiqifa_order_20130606_04.dat");
 				System.out.println("logpath-info:\t" + lparse.logpath.toString()
 						+ "\nout:\t" + lparse.base
 						+ "\ninpath:\t" + input[i] + "\nindata:\t" + oneline.get(i) 
 						+ "\nlogparse:\t" + lparse.base
 						+ "\n[uid, plat-uid]:\n" + lparse.base.user_id + "\t" + lparse.base.plat_user_id);
 				break;
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
    
 }
