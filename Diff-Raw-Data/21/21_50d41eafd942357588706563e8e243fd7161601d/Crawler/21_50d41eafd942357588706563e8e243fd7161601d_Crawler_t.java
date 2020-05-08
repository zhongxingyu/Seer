 package news.crawler.url;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import util.BloomFilter;
 import news.filter.UrlFilter;
 import news.model.WebSite;
 
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import util.Config;
 import util.Const;
 import util.Log;
 import db.data.Url;
 
 
 
 /**
  * @time 2013-3-16
  * @author mblank
  *
  */
 public class Crawler{
 		
 	private static String Bloom_File_Path;
 	
 	public Crawler(){
 		Log.getLogger().info("Start TitleCrawler!");
 		Config cfg = new Config(Const.SYS_CONFIG_PATH);
 		Node elm = cfg.selectNode("/Configs/Config[@name='UrlsBloomFilterFilePath']/Path");
 		Bloom_File_Path =elm.getText();
 	}
 	
 	
 	/**
 	 * @param doc
 	 * @return
 	 * @Description:from doc get all the urls
 	 * @mark:there is a basic filter,if the url_title_length less than 
 	 * Const.WebSiteUrlTitleFilerLength,than it will not be return
 	 */
 	public Map<String,String> getALLhrefs(Document doc){
 		Map<String,String> result = new HashMap<String,String>();
 		Elements els = null;
 		els = doc.getElementsByTag("a");
 		String name = "";
 		String url_name = "";
 		Iterator<org.jsoup.nodes.Element> it_el = els.iterator();
 		while(it_el.hasNext()){
 			org.jsoup.nodes.Element el = it_el.next();
 			name = el.text();
 			url_name = el.attr("href");
 			if(name.length()>Const.WebSiteUrlTitleFilerLength){
 				//check url exists or not!
 				if(!result.containsKey(url_name) && name.length() > 5){
 					result.put(url_name, name);
 				}
 			}
 		}	
 		return result;
 	}
 	
 	
 	/**
 	 * @param scr
 	 * @return Map<String,String>, url + title
 	 * @Description: filter of the urls
 	 */
 	public Map<String,String> filterForHrefs(Map<String,String> scr){
 		Map<String,String> results = new HashMap<String,String>();
 		Set<String> st_url = scr.keySet();
 		Iterator<String> it_url = st_url.iterator();
 		while(it_url.hasNext()){
 			String url = it_url.next();
 			if(url.contains(".html")&&(url.length()-url.lastIndexOf(".html")==5)){
 				results.put(url, scr.get(url));
 			}
 			if(url.contains(".shtml")&&(url.length()-url.lastIndexOf(".shtml")==6)){
 				results.put(url, scr.get(url));
 			}
 			if(url.contains(".htm")&&(url.length()-url.lastIndexOf(".htm")==4)){
 				results.put(url, scr.get(url));			
 			}	
 		}	
 		return results;
 	}
 	
 	/**
 	 * @param url
 	 * @return url+title
 	 * @Description:give a url ,return all the urls and titles
 	 */
 	public Map<String,String> getUrls(String url){
 		Map<String,String> result = new HashMap<String,String>();
 		try{
 			Document doc = Jsoup.connect(url).timeout(5000).userAgent(util.Const.CrawlerUserAgent).get();
 			result = getALLhrefs(doc);
 			result = filterForHrefs(result);	
 		}catch(Exception e){
			System.err.println("can't connect to the url: "+url + " will try again..");
			Document doc;
			try {
				doc = Jsoup.connect(url).timeout(5000).userAgent(util.Const.CrawlerUserAgent).get();
				result = getALLhrefs(doc);
				result = filterForHrefs(result);
			} catch (IOException e1) {
				System.err.println("try again, but failed.." + "\t" + url);
			}
 		}	
 		return result;
 	}
 	
 	/**
 	 * @param ws
 	 * @param str_url
 	 * @param url
 	 * @return
 	 * @Description:add some str to the url crawl from the page
 	 */
	public String addSomeConditon(String str_url,String url){
 		
 		///some relative url
 		// 0 - xx/xx.html
 		// 1 - /xx/xx.html
 		// 2 - ./xx/xx.html
 		// 3 - ../../xx/xx.html
 		if(!url.contains("http:")){
 			String[] its = str_url.split("/");
 			if(url.indexOf("/") == 0){
 				if(its.length >= 3){
 					url = its[0] + "//" + its[2] + url; 
 				}else{
 					url = "";
 				}
 			}else if(url.indexOf("./") == 0){
 				if(its.length > 0){
 					StringBuffer tmp = new StringBuffer();
 					tmp.append("http://");
 					for(int i = 2; i < its.length - 1; i++){
 						tmp.append(its[i] + "/");
 					}
 					url = url.replace("./", "");
 					url = tmp.append(url).toString();
 				}else{
 					url = "";
 				}
 			}else if(url.indexOf("../") == 0){
 				StringBuffer tmp = new StringBuffer();
 				tmp.append("http://");
 				String[] tmps = url.split("\\.\\./");
 				String pox = tmps[tmps.length - 1];
 				int k = tmps.length - 1;
 				if(its[its.length - 1].equals("")){
 					k++;
 				}
 				for(int i = 2;i< its.length - k;i++){
 					tmp.append(its[i] + "/");
 				}
 				tmp.append(pox);
 				url = tmp.toString();
 			}else if(url.indexOf(".") != 0){
 				if(its.length >= 3){
					url = its[0] + "//" + its[2] + "/" + url; 
 				}else{
 					url = "";
 				}
 			}else{
 				url = "";
 			}
 		}
 		return url;
 	}
 	
 	/**
 	 * @param tnf
 	 * @param tn
 	 * @return
 	 * @Description:according to the filters in WebSite.xml,filter some urls;
 	 */
 	public boolean filterTitleNews(UrlFilter tnf,Url tn){
 		boolean bo_result = true;
 		String[] check_str = tnf.getStr_filter();
 		String url = tn.getUrl();
 		url = url.replaceAll("//", "/");
 		String[] url_spilt = url.split("/");
 		//there is no filter for the url
 		if(check_str == null || check_str.length == 0 || (check_str.length > 0 && check_str[1].equalsIgnoreCase("null"))){
 			return true;
 		}		
 		try{
 			for(int i=1;i<check_str.length;i++){
 				if(!url_spilt[i].equalsIgnoreCase(check_str[i]))
 					bo_result = false;
 			}	
 		}catch(Exception e){
 			e.printStackTrace();
			System.out.println(tn.getUrl());
 		}
 		if(url.length()<=tnf.getUrl_avg_len()){
 			return false;
 		}
 		return bo_result;
 	}
 	
 	/**
 	 * @param ws
 	 * @return
 	 * @Description:From WebSite,get all the site urls
 	 */
 	public List<Url> getAllTitleNewsFromWebSite(WebSite ws){
 		Map<String,Url> mp_tn = new HashMap<String,Url>();
 		List<Url> ls_result = new ArrayList<Url>();
 		Set<String> st_url = ws.Sites.keySet();
 		Iterator<String> it_url = st_url.iterator();
 		while(it_url.hasNext()){
 			int url_size = 0;
 			String str_url = it_url.next();		
 			Map<String,String> mp_url = new HashMap<String,String>();
 			mp_url = getUrls(str_url);   			
 			Set<String> st_mp = mp_url.keySet();			
 			Iterator<String> it_urls = st_mp.iterator();			
 			while(it_urls.hasNext()){
 				String url = it_urls.next();
				url = addSomeConditon(str_url,url);
 				if(url.length() == 0)
 					continue;
 				Url tn = new Url();
 				tn.setCrawlTime(new java.util.Date());
 				tn.setUrl(url);
 				tn.setTaskStatus(Const.TASKID.get("urlToMysql"));
 				tn.setWebSite(ws.SiteName);
 				tn.setSubtopicId(Const.SUBTOPICID[ws.getSites().get(str_url)]);
 				if(!filterTitleNews(ws.getFilters().get(str_url),tn)){
 					continue;
 				}			
 				if(mp_tn.containsKey(url)){
 					int type = tn.getSubtopicId();
 					type = type | mp_tn.get(url).getSubtopicId();
 					tn.setSubtopicId(type);
 				}
 				mp_tn.put(url, tn);
 				url_size++;
 			}
 			System.out.println(str_url + "\t" + url_size);
 		}
 		Collection<Url> st_tn_mp = mp_tn.values();
 		Iterator<Url> it_tn_mp = st_tn_mp.iterator();
 		while(it_tn_mp.hasNext()){
 			Url re_tn = it_tn_mp.next();
 			if(re_tn.getUrl().contains("http")){
 				re_tn.setUrl(re_tn.getUrl().substring(re_tn.getUrl().indexOf("http")));
 			}
 			ls_result.add(re_tn);
 		}
 		//for gc
 		mp_tn = null;
 		st_url = null;
 		return ls_result;
 	}
 	
 	/**
 	 * @param node_website
 	 * @return
 	 * @Description:read the xml to get WebSite
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public WebSite getWebSiteFromElement(Element node_website){
 		WebSite result = new WebSite();
 		Map<String,Integer> url_types = new HashMap<String,Integer>();			
 		Map<String,UrlFilter> url_filter = new HashMap<String,UrlFilter>();
 		String siteName = "";
 		String str_url = "";
 		int  url_avg_len = 0,title_avg_len=0;
 		int  type = 0,levels = 0;
 		///get the siteName
 		siteName = node_website.attributeValue("name");
 		List urls = node_website.elements();		
 		Iterator<Element> url = urls.iterator();		
 		while(url.hasNext()){
 			Element node_url = url.next();		
 			///get the url 
 			str_url = node_url.element("Addr").getText();
 			///get url's type
 			type = Integer.valueOf(node_url.element("Type").getText());
 			//get all filters
 			Element el_filter = node_url.element("Filter");
 			List<Element> els_filter = el_filter.elements();
 			levels = els_filter.size();
 			String[] str_filter = new String[levels+1];
 			for(int i=0;i<levels;i++){
 				str_filter[i+1] = els_filter.get(i).getText();
 			}
 			Element el_url_len = node_url.element("AvgUrlLength");
 			try{
 				url_avg_len = Integer.valueOf(el_url_len.getText());
 			}catch(Exception e){
 				System.out.println(str_url);
 			}			
 			Element el_title_len = node_url.element("AvgTitleLength");
 			title_avg_len = Integer.valueOf(el_title_len.getText());
 			UrlFilter tnf = new UrlFilter();
 			tnf.setStr_filter(str_filter);
 			tnf.setTitle_avg_len(title_avg_len);
 			tnf.setUrl_avg_len(url_avg_len);
 			url_types.put(str_url, type);
 			url_filter.put(str_url, tnf);
 		}
 		result.setSiteName(siteName);
 		result.setSites(url_types);		
 		result.setFilters(url_filter);	
 		//for gc
 		url_types = null;
 		url_filter = null;
 		return result;
 	}
 		
 	/**
 	 * @return List<WebSite>
 	 * @Description:read WebSites.xml and get all titleNews urls + titles+filter
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public List<Url> getAllTitleNews(){
 		Const.loadTaskid();
 		List<Url> ls_results = new ArrayList<Url>();
 		List<Url> ls_temp = new ArrayList<Url>();
 		///read the WebSites.xml
 		Config cfg = new Config(Const.WEB_SITES_PATH);
 		///get all elements of website
 		List res_nodeList = cfg.selectNodes("/WebSites/WebSite");
 		Iterator<Element> it_nodes = res_nodeList.iterator();		
 		while(it_nodes.hasNext()){
 			Element  node_website = it_nodes.next();
 			WebSite result = new WebSite();	
 			//from xml element get WebSite
 			result = getWebSiteFromElement(node_website);		
 			//get List<titleNews> from WebSite
 			ls_temp = getAllTitleNewsFromWebSite(result);
 			Iterator<Url> it_tn = ls_temp.iterator();
 			while(it_tn.hasNext()){
 				Url tn_temp = it_tn.next();
 				ls_results.add(tn_temp);
 			}	
 		}
 		//for gc
 		ls_temp = null;
 		return ls_results;
 	}
 	
 	public BloomFilter InitBloomFilter(){
 
 		BloomFilter bloomfilter = new BloomFilter();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(Bloom_File_Path));
 			String line = "";
 			while((line = br.readLine())!=null){
 				bloomfilter.add(line.toString().toLowerCase());
 				///for gc
 				line = null;
 			}
 			br.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return bloomfilter;
 	}
 	
 	public void WriterToBloomFile(List<Url> tns){
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Bloom_File_Path),true));
 			for(Url tn : tns){
 				bw.write(tn.getUrl()+"\n");
 			}
 			bw.close();
 			//for gc
 			tns = null;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public  void runTask(){
 		Const.loadTaskid();
 		BloomFilter bf = InitBloomFilter();
 		System.out.println("bloom filter init ok...");	
 			Log.getLogger().info("Start Crawler for titleCrawler");
 			List<Url> NewUrls = new ArrayList<Url>();
 			NewUrls = getAllTitleNews();
 			if(bf == null)
 				return;		
 			List<Url> updateUrls = new ArrayList<Url>();	
 			int max_len_url = 0;
 			for(Url tn : NewUrls){
 				if(!bf.contains(tn.getUrl().toLowerCase())){
 					updateUrls.add(tn);
 					bf.add(tn.getUrl().toLowerCase());
 				}
 				if(tn.getUrl().length() > max_len_url){
 					max_len_url = tn.getUrl().length();
 				}
 			}
 			util.Util.updateDB(updateUrls);
 			WriterToBloomFile(updateUrls);			
 			System.out.println("now end of Crawler..update urls -- " + updateUrls.size());					
 			System.out.println("end of java_gc..so happy~!");	
 	}
 	
 	
 	public static void main(String[] args){
 		while(true){
 			Crawler uc = new Crawler();
 			uc.runTask();
 			//for gc
 			uc = null;
 			try {
 				System.out.println("now end of one crawler,sleep for:"+Const.WebSiteSleepTime/1000/60+" minutes. "+new Date().toString());
 				Thread.sleep(Const.WebSiteSleepTime);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}			
 		}
 	}
 	
 }
