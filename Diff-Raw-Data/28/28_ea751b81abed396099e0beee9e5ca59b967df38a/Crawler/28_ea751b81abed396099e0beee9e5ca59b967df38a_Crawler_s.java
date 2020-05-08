 package news.crawler.Article;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.dom4j.Node;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import news.index.ArticleTitleIndex;
 
 
 import util.ChineseSplit;
 import util.Config;
 import util.Const;
 
 import db.HSession;
 import db.data.Article;
 import db.data.Ddf;
 import db.data.Url;
 
 
 ///for article content words
 final class ArticleContent{
 	public Article at;
 	public Map<String,Integer> words;
 }
 
 public class Crawler {
 	
 	
 	private String HQL;
 	///articles in the memory
 	private Map<Integer,Article> mem_ats;
 	///a inverse index in the memory
 	private Map<String,Set<Integer>> mem_index;
 	private List<Article> update_index ;
 	private ArticleTitleIndex ati ;
 	private String ArticleIndexPath ;
 	
 	///will be loaded into memory
 	private Map<String,Integer> TDF;
 	private int TDF_doc_num;
 	private int TDF_word_num;			
 	private String TDF_Path ;
 	
 	//DF for every day in the DB
 	private Map<String,Map<String,Integer>> DDF;
 	private Map<String,Integer> DDF_doc_num;
 	private Map<String,Integer> DDF_word_num;
 	
 	
 	private void initTDF(){
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(TDF_Path));
 			String line = "";
 			//first line is doc_num \t word_num
 			line = br.readLine();
 			String[] nums = line.split("\t");
 			TDF_doc_num = Integer.valueOf(nums[0]);
 			TDF_word_num = Integer.valueOf(nums[1]);
 			while((line = br.readLine())!= null){
 				String[] its = line.split("\t");
 				int count = Integer.valueOf(its[1]);
 				TDF.put(its[0], count);
 			}
 			br.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Crawler(){
 		Const.loadTaskid();
 		mem_ats = new HashMap<Integer,Article>();
 		mem_index = new HashMap<String,Set<Integer>>();
 		update_index = new ArrayList<Article>();
 		Config cfg = new Config(Const.SYS_CONFIG_PATH);
 		Node elm_ai = cfg.selectNode("/Configs/Config[@name='ArticleIndex']/Path");
 		Node elm_tdf = cfg.selectNode("/Configs/Config[@name='TotalDF']/Path");
 		ArticleIndexPath = elm_ai.getText();
 		TDF_Path = elm_tdf.getText();
 		TDF = new HashMap<String,Integer>();
 		initTDF();
 		DDF_doc_num = new HashMap<String,Integer>();
 		DDF_word_num = new HashMap<String,Integer>();
 		DDF = new  HashMap<String,Map<String,Integer>>();
 		ati = new ArticleTitleIndex(ArticleIndexPath);
 		HQL = "from Url as obj where obj.taskStatus="+Const.TASKID.get("urlToMysql");
 	}
 	
 	//find the same article in the memory and update memory
 	private boolean FindInMemory(ArticleContent ac){
 		Article at = ac.at;
 		boolean mem_find = false;
 		Set<Integer> at_tmp_ids = new HashSet<Integer>();		
 		//first check from the memory map
 		List<String> at_title_words = ChineseSplit.SplitStr(at.getTitle());
 		for(String at_title_word : at_title_words){
 			if(at_title_word.length() > 1){
 				Set<Integer> tmp_ids = new HashSet<Integer>();
 				if(mem_index.containsKey(at_title_word)){
 					///find same id and add it to 
 					tmp_ids = mem_index.get(at_title_word);
 					at_tmp_ids.addAll(tmp_ids);
 				}else{
 					tmp_ids.add(at.getId());
 					mem_index.put(at_title_word, tmp_ids);
 				}
 			}
 		}
 		double score = -1;
 		for(Integer at_tmp_id : at_tmp_ids){
 			if(mem_ats.containsKey(at_tmp_id)){
 				Article tmp_at = mem_ats.get(at_tmp_id);
 				score =  util.Similarity.ContentOverlap(tmp_at.getContent(),at.getContent());
 				if(score > 0.85){
 					mem_find = true;
 					if(at.getImgs().length() > 0 && tmp_at.getImgs().length() == 0){
 						tmp_at.setImgs(at.getImgs());
 					}
 					tmp_at.setNumber(tmp_at.getNumber() + 1);
 					tmp_at.setSameurls(tmp_at.getSameurls() + " " + at.getId());
 					System.out.println(tmp_at.getId() + "\t" + tmp_at.getSameurls());
 					mem_ats.put(tmp_at.getId(), tmp_at);
 					System.out.println("find in memory..." + tmp_at.getId() + "\t" + at.getId());
 					break;
 				}
 			}
 		}
 		return mem_find;
 	}
 	
 	//find same article from index in the disk
 	private void FindInIndex(ArticleContent ac){
 		Article at = ac.at;
 		int sameid = ati.checkSameInIndex(at);
 		if(sameid > 0){
 			Article sameat = new Article();
 			if(mem_ats.containsKey(sameid)){
 				sameat = mem_ats.get(sameid);
 			}else{
 				sameat = util.Util.getArticleById(sameid);
 			}
 			sameat.setNumber(sameat.getNumber() + 1);
 			sameat.setSameurls(sameat.getSameurls() + " " + at.getId());
 			mem_ats.put(sameat.getId(), sameat);
 			System.out.println("find " + sameid + " --- " + at.getId() + " -- " + at.getTitle());
 		}else if(sameid != -2){
 			at.setNumber(0);
 			at.setSameurls("");
 			at.setTaskstatus(Const.TASKID.get("UrlToArticle"));				
 			mem_ats.put(at.getId(), at);
 			List<String> tmp_words = ChineseSplit.SplitStr(at.getTitle());
 			for(String tmp_word : tmp_words){
 				Set<Integer> tmp_ids = new HashSet<Integer>();
 				if(tmp_word.length() > 1){
 					if(mem_index.containsKey(tmp_word)){
 						tmp_ids = mem_index.get(tmp_word);
 					}
 					tmp_ids.add(at.getId());
 					mem_index.put(tmp_word, tmp_ids);
 				}
 			}
 			update_index.add(at);
 		}
 	}	
 	
 	///write the memory tdf to disk files
 	private void updateTDF(List<ArticleContent> acs){
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(TDF_Path));
 			for(ArticleContent ac : acs){
 				Map<String,Integer> words = ac.words;
 				Iterator<String> it_wds = words.keySet().iterator();
 				while(it_wds.hasNext()){
 					String name = it_wds.next();
 					if(TDF.containsKey(name)){
 						TDF.put(name, 1+ TDF.get(name));
 					}else{
 						TDF.put(name, 1);
 					}
 				}
 			}
 			TDF_doc_num += acs.size();
 			TDF_word_num = TDF.size();
 			bw.write(TDF_doc_num + "\t" + TDF_word_num + "\n");
 			Iterator<String> out_words = TDF.keySet().iterator();
 			while(out_words.hasNext()){
 				String word = out_words.next();
 				bw.write(word + "\t" + TDF.get(word) + "\n");
 			}
 			bw.close();
 			//for gc
 			out_words = null;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<Ddf> CombineDBWithMemory(){
 		List<Ddf> updates = new ArrayList<Ddf>();
 		Session session = new HSession().createSession();
 		Iterator<String> days = DDF.keySet().iterator();
 		while(days.hasNext()){
 			String day = days.next();
 			String hql = "from Ddf as obj where obj.day= '" + day + "'";
 			Query query = session.createQuery(hql);
 			List<Ddf>  tmp = (List<Ddf>)query.list();
 			Ddf today = new Ddf();
 			today.setDay(day);
 			Map<String,Integer> ddf_words = new HashMap<String,Integer>();
 			if(tmp.size() != 0){
 				today = tmp.get(0);
 				ddf_words = util.Util.getDdfMap(today.getWords());
 			}
 			Iterator<String> upwords = DDF.get(day).keySet().iterator();
 			while(upwords.hasNext()){
 				String upwd = upwords.next();
 				if(ddf_words.containsKey(upwd)){
 					ddf_words.put(upwd, ddf_words.get(upwd) + DDF.get(day).get(upwd));
 				}else{
 					ddf_words.put(upwd, DDF.get(day).get(upwd));
 				}
 			}
 			String out = util.Util.DdfMapToStr(ddf_words);
 			today.setWords(out);
			today.setDocnum(today.getDocnum() + DDF_doc_num.get(day));
			today.setWordnum(today.getWordnum() + DDF_word_num.get(day));
 			updates.add(today);
 		}
 		return updates;
 	}
 	
 	private void updateDDF(List<ArticleContent> acs){
 		
 		for(ArticleContent ac : acs){
 			String day = util.Util.getDateStr(ac.at.getPublishtime());
 			int num = 0;
 			Iterator<String> words = ac.words.keySet().iterator();
 			Map<String,Integer> tmp_day_words = new HashMap<String,Integer>();
 			if(DDF.containsKey(day)){
 				tmp_day_words = DDF.get(day);
 			}
 			while(words.hasNext()){
 				String wd = words.next();
 				if(tmp_day_words.containsKey(wd)){
 					tmp_day_words.put(wd, tmp_day_words.get(wd) + ac.words.get(wd));
 				}else{
 					tmp_day_words.put(wd, ac.words.get(wd));
 					num++;
 				}
 			}
 			if(DDF_word_num.containsKey(day)){
 				DDF_word_num.put(day, DDF_word_num.get(day) + num);
 				DDF_doc_num.put(day, DDF_doc_num.get(day) + 1);
 			}else{
 				DDF_word_num.put(day, num);
 				DDF_doc_num.put(day, 1);
 			}
 			DDF.put(day, tmp_day_words);
 			List<Ddf> updates = CombineDBWithMemory();
 			util.Util.updateDB(updates);
 		}
 	}
 	
 	private void runTask(){
 		
 		///get all urls which haven't been downloaded per batch is 1500
 		List<Url> urls = util.Util.getElementsFromDB(HQL,500);			
 		int tmp_number = 0;
 		List<ArticleContent> update_idf = new ArrayList<ArticleContent>();
 		for(Url url : urls){
 			//for some special site
 			//now ifeng blocked our ip , so can't connect to it
 			if(url.getUrl().contains("ifeng")){
 				url.setTaskStatus(-1);
 				continue;
 			}
 			///extract article content
 			Extractor etor = new Extractor(url.getUrl());			
 			Article at = new Article();			
 			at = etor.getArticleFromUrl(url);	
 			Map<String,Integer> at_ct_words = ChineseSplit.SplitStrWithPosTFS(at.getContent());
 			ArticleContent ac = new ArticleContent();
 			ac.at = at;
 			ac.words = at_ct_words;
 			if(url.getTaskStatus() < 0)
 				continue;				
 			update_idf.add(ac);			
 			if(FindInMemory(ac)){
 				continue;
 			}else{
 				FindInIndex(ac);
 			}		
 			tmp_number++;
 			if(tmp_number % 100 == 0){
 				System.out.println(url.getId());
 			}
 		}
 		
 		updateDDF(update_idf);
 		updateTDF(update_idf);
 		///update DB
 		List<Article> toupdate = new ArrayList<Article>();
 		toupdate.addAll(mem_ats.values());
 		util.Util.updateDB(toupdate);
 		util.Util.updateDB(urls);
 		//update index	
 		ati.update(update_index);
 		///update the IDF and DDF
 		//for gc
 		update_idf = null;
 		urls = null;
 		toupdate = null;		
 	}
 
 	
 	public static void main(String[] args){
 		while(true){
 			Crawler ac = new Crawler();
 			ac.runTask();
 			//for gc test
 			ac.mem_ats = null;
 			ac.mem_index = null;
 			ac.update_index = null;
 			ac = null;
 			try {
 				System.out.println("now end of one crawler,sleep for:"+Const.AritcleSleepTime /1000 /60 +" minutes. "+new Date().toString());
 				Thread.sleep(Const.AritcleSleepTime );
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}			
 		}
 	}
 	
 
 }
