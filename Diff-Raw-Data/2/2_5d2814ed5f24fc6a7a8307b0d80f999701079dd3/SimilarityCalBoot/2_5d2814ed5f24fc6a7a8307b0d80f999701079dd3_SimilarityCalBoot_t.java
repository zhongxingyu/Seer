 package gossip.sim;
 
 import edu.bit.dlde.utils.DLDELogger;
 import gossip.Boot;
 import gossip.utils.DateTrans;
 
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * 计算当天的新闻的相似度并保存到sim-info目录
  * 
  * @author ChenJie
  * 
  */
 public class SimilarityCalBoot extends Boot {
 	DLDELogger logger = new DLDELogger();
 
 	/** 专门用来处理计算相似度的实例 **/
 	public SimilarityCalculator calculator = new SimilarityCalculator();
 
 	private String[] fields = { "title", "body" };
 	private int date;
 	public SimilarityCalBoot(int date){
 		this.date = date;
 	}
 	
 	public void process() {
		if(date == 0){ //全部计算
 			Calendar cal = Calendar.getInstance();
 			int day = cal.get(Calendar.DAY_OF_MONTH);
 			int month = cal.get(Calendar.MONTH) + 1;
 			int year = cal.get(Calendar.YEAR);
 			int today = DateTrans.YYMMDDToInt(year, month, day);
 			for(int begin = 20120601; begin <= today; begin = DateTrans.theDayAfterYYMMDD(begin)){
 				process_once(begin);
 			}
 		}else{
 			process_once(date);
 		}
 	}
 	
 	/**
 	 * 计算当天文档的相似度，事实上期望的是它应该一天内被多次调用。
 	 */
 	public void process_once(int date) {
 		try {
 			/** 当前是对body和title域均计算一遍相似读 **/
 			for (String field : fields) {
 				List<SimilarDocPair> sdpList = calculator.getSimilarDocPairs(date, field, 10);// 后退10天求相似度
 				if (sdpList == null)
 					continue;
 				sdpList = calculator.refineSimList(sdpList);
 				calculator.saveSimilarDocPairs(field, sdpList,date+"");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 		DLDELogger logger = new DLDELogger();
 		Calendar cal = Calendar.getInstance();
 		int day = cal.get(Calendar.DAY_OF_MONTH);
 		int month = cal.get(Calendar.MONTH) + 1;
 		int year = cal.get(Calendar.YEAR);
 		logger.info("year:" + year + ",month:" + month + ",day:" + day);
 		int date_int = DateTrans.YYMMDDToInt(year, month, day);
 		for (int i = 0; i < args.length;) {
 			if (args[i].equals("-d")) {
 				try{
 					date_int = Integer.parseInt(args[i + 1]);
 				}catch(Exception e){
 					System.out.println("-d 参数不合法，请输入格式为 YYYYMMDD,如 20121206");
 					System.exit(0);
 				}
 				i += 2;
 			}else if (args[i].equals("-h")) {
 				System.out.println("-d 计算某天的相似度,默認值：今天，例如 20121206；若值为0，则表示全部计算");
 				System.exit(0);
 			}
 		}
 		SimilarityCalBoot scb = new SimilarityCalBoot(0);
 		scb.run();
 	}
 
 }
