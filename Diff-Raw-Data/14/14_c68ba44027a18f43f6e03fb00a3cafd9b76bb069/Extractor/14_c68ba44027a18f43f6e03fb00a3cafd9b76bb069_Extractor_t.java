 package news.crawler.Article;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import news.model.NewsPage;
 import news.model.NewsPageTags;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import db.data.Article;
 import db.data.Url;
 
 /**
 * @PackageName:news.crawler.Article
 * @ClassName: NewsContentExtractor
 * @author: mblank
 * @date: 2013-3-13 下午9:58:01
 * @Description: TODO
 * @Marks: TODO
 */
 public class Extractor {
 	
 	private Document Doc;	
 	private NewsPageTags Npt ;
 	private int state = 1;
 	
 		
 	
 	public int getState() {
 		return state;
 	}
 
 	//////////for some urls which has templete
 	public Extractor(String url){
 		try {
 			Doc = Jsoup.connect(url).timeout(5000).userAgent("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22").get();
 			if(Doc != null){
 				List<String> sites = NewsPageTags.getSiteName();
 				for(String site : sites){
 					if(url.contains(site)){
 						Npt = new NewsPageTags(site);
 						state = 2;
 						break;
 					}
 				}
 			}				
 		} catch (IOException e) {
 			System.out.println("can't connect to " + url);
 			state = -1;
 		}catch(Exception e){
 			e.printStackTrace();
 			state = -2;
 		}
 	}
 	
 	public String getHtml(){
 		if(Doc != null)
 			return Doc.html();
 		return "";
 	}
 	
 	public Article getArticleFromUrl(Url url){
 		Article at = new Article();
 		at.setUrl(url.getUrl());
 		at.setId(url.getId());
 		at.setCrawltime(new Date());
 		///if has no templete use general extractor methods
 		if(getState() == 1){
 			Element mainEle = getMainElement();
 			String paras = getMainParagraph(mainEle);
 			List<String> imgs = getImgsG(mainEle);
 			String title = getTitleG();
 			////construct a new article
 			at.setPublishtime(new Date());
 			at.setTitle(title);
 			at.setContent(paras);
 			StringBuilder sb_imgs = new StringBuilder();
 			for(String img : imgs){
 				sb_imgs.append("!##!" + img);
 			}
 			at.setImgs(sb_imgs.toString());				
 		}else if(getState() == 2){
 			at.setTitle(getTitle());
 			at.setPublishtime(getPublishTime());
 			at.setContent(getContent());
 			at.setImgs(getImgs());
 		}
 		if(at.getContent() == null || at.getContent().length() < 500){
 			url.setTaskStatus(-3);
 		}else{
 			url.setTaskStatus(getState());
 		}
 		//for gc
 		return at;
 	}
 	
 	
 	public String getInformation(NewsPage np){
 		String result = "";
 		if(np != null){
 			///first get the id tag,then get class and subtag , last get the tag
 			if(np.getId() != null){
 				Element id = Doc.getElementById(np.getId());
 				if(id == null)
 					return "";
 				if(np.getSubTag() == null){
 					result =  id.text();
 				}else{
 					Elements subels = id.getElementsByTag(np.getSubTag());
 					int subindex = np.getSubTagIndex() - 1;
 					if(subindex >= 0 && subels.size() > subindex){						
 						result = subels.get(subindex).text();
 					}else if(subindex == -1){
 						for(Element subel : subels){
 							///special for news imgs
 							if(subel.tagName().equals("img")){
 								if(np.getBaseUrl().indexOf("http") == 0){
 									result += "!##!" + np.getBaseUrl();
 								}else{
 									result += "!##!" ;
 								}
 								if(np.getTextAtt() != null && subel.attr(np.getTextAtt()).length() > 0 ){
 									result += subel.attr("src");
 								}else if(np.getTextAtt() == null){
 									result +=  subel.attr("src");																	
 								}
 							}else if(subel.text().length() > 0){
 								result += "!##!" + subel.text();
 							}
 						}
 					}
 				}
 			}else if(np.getTagClass() != null){
 				Elements tags = Doc.getElementsByClass(np.getTagClass());
 				if(tags == null)
 					return "";
 				for(Element tag : tags){
 					if(np.getSubTag() != null){
 						Elements subtags= tag.getElementsByTag(np.getSubTag());
 						int subtagindex = np.getSubTagIndex() -1;
 						if(subtags.size() > subtagindex && subtagindex >= 0){
 							result = subtags.get(subtagindex).text();
 							break;
 						}else if(subtagindex == -1){
 							for(Element subel : subtags){
 								if(subel.tagName().equals("img")){
 									if(np.getBaseUrl().indexOf("http") == 0){
 										result += "!##!" + np.getBaseUrl();
 									}else{
 										result += "!##!" ;
 									}
 									if(np.getTextAtt() != null && subel.attr(np.getTextAtt()).length() > 0 ){
 										result += subel.attr("src");
 									}else if(np.getTextAtt() == null){
 										result +=  subel.attr("src");																	
 									}
 								}else if(subel.text().length() > 0){
 									result += "!##!" + subel.text();
 								}
 							}
 						}
 					}else{
 						result = tag.text();
 						break;
 					}					
 				}
 			}else if(np.getTag() != null){
 				Elements tags = Doc.getElementsByTag(np.getTag());
 				if(tags == null)
 					return "";
 				if(tags.size() > 0){
 					result = tags.get(0).text();
 				}
 			}
 		}
 		return result;
 	}
 	
 	public String getTitle(){
 		String title = "";
 		if(Doc == null || Npt == null)
 			return "";
 		NewsPage nptitle = Npt.getTitle();
 		title = getInformation(nptitle);
 		if(title.length() == 0){
 			Elements titles = Doc.getElementsByTag("title");
 			if(titles != null && titles.size() >0){
 				title = titles.get(0).text();
 			}				
 		}
 		return title;
 	}
 	
 	private Date getDateFromStr(String time){
 		Date result = new Date();
 		time = time.replaceAll("年", "-");
 		time = time.replaceAll("月", "-");
 		time = time.replaceAll("日\\s?", "-");
 		time = time.replaceAll("\\s", "-");
 		time = time.replaceAll(":", "-");
 		time = time.replace("\t;", "-");
 		StringBuilder sb = new StringBuilder();
 		for(int i = 0 ;i<time.length();i++){
 			if(time.charAt(i) <= 128){
 				sb.append(time.charAt(i));
 			}
 		}		
 		time = sb.toString();
 		String[] its = time.split("-");
 		String timeFormat = "";
 		if(its.length == 6){
 			timeFormat = "yyyy-MM-dd-HH-mm-ss";
 		}else if(its.length == 5){
 			timeFormat = "yyyy-MM-dd-HH-mm";
 		}else if(its.length == 3){
 			timeFormat = "yyyy-MM-dd";
 		}
 		SimpleDateFormat sdf=new  SimpleDateFormat(timeFormat);
 		try{
 			result = sdf.parse(time);
 		}catch(Exception e){
 			result = null;
 		}		
 		return result;
 	}
 	
 	public Date getPublishTime(){
 		String time = "";
 		if(Doc == null || Npt == null)
 			return null;
 		NewsPage nptime = Npt.getTime();
 		time = getInformation(nptime);
 		if(time.length() == 0){
 			time = Doc.html();
 		}
 		///use regex to extract time
 		Pattern pattern = Pattern.compile("\\d{2,4}.\\d{1,2}.\\d{1,2}.?\\s?\\d{1,2}:\\d{1,2}:?\\d{0,2}");
 		Matcher matcher = pattern.matcher(time);
 		if(matcher.find()){
 			time = matcher.group().trim();
 		}
 		return getDateFromStr(time);
 	}
 	
 	public String getSource(){
 		String source = "";
 		if(Doc == null || Npt == null)
 			return "";
 		NewsPage npsource = Npt.getSource();
 		source = getInformation(npsource);
 		return source;
 	}
 	
 	public String getContent(){
 		String content = "";
 		if(Doc == null || Npt == null)
 			return "";
 		NewsPage npcontent = Npt.getContent();
 		content = getInformation(npcontent);
 		return content;
 	}
 	
 	public String getNewsTags(){
 		String newstags = "";
 		if(Doc == null || Npt == null)
 			return "";
 		NewsPage npnewstags = Npt.getNewsTags();
 		newstags = getInformation(npnewstags);
 		return newstags;
 	}
 	
 	public String getImgs(){
 		String imgs = "";
 		if(Doc == null || Npt == null)
 			return "";
 		NewsPage npimgs = Npt.getImgs();
 		imgs = getInformation(npimgs);		
 		return imgs;
 	}
 
 	
 	////////////for some url which has no templete
 	
 	
 	private  Double MAX = 0.0;
 	private  String TEXT = "";
 	
 	class TagCheck{
 		public double text_num = 1;
 		public double tag_num = 1;
 		public double link_num = 1;		
 	}
 	
 	private TagCheck showTag(Element element, int level){
 
 		TagCheck total = new TagCheck();
 		total.tag_num = 1;
 		if(element.tagName().equals("a")){
 			total.link_num ++;
 		}
 		if(element.children().size() == 0){
 			return total;
 		}
 		for(Element el : element.children()){
 			TagCheck tmp = new TagCheck();
 			tmp = showTag(el,level+1);
 			if(tmp.text_num != 0 ){
 				total.tag_num += tmp.tag_num ;
 				total.link_num += tmp.link_num;
 			}	
 			if(el.tagName().equalsIgnoreCase("p") ||  el.tagName().equalsIgnoreCase("br")){
 				total.text_num++;
 			}			
 		}	
 		Double res = element.text().length() / total.tag_num * total.text_num ;/// total.link_num;
 		if(res > MAX ){
 			MAX = res;
 			TEXT = res.toString();
 			if(!element.tagName().equalsIgnoreCase("img") || !element.tagName().equalsIgnoreCase("a")||
 					!element.tagName().equalsIgnoreCase("font")||!element.tagName().equalsIgnoreCase("p")){
 				element.tagName(TEXT);
 			}			
 		}	
 		return total;
 	}
 	
 	public Element getMainElement(){
 		Element main = null;
 		if(Doc == null){
 			return null;
 		}
 		Elements els = Doc.getElementsByTag("body");
 		if(els.size() > 0){
 			showTag(els.get(0),0);
 		}			
 		else
 			return null;
 		if(TEXT.length() <= 0)
 			return null;
 		Elements mains = Doc.getElementsByTag(TEXT);
 		if(mains.size() > 0){
 			main = mains.get(0);
 		}		
 		return main;
 			
 	}
 		
 	public String getMainParagraph(Element main){
 		StringBuilder result = new StringBuilder();
 			if(main != null){
 				int num = main.children().size();
 				int check = 0;
 				for(Element el : main.children()){
 					check++;
 					if((el.text().contains("编辑")|| el.text().toLowerCase().contains("copyright")
 							|| el.text().contains("本报记者") || el.text().contains("原标题"))
 							&& (el.children().size() ==0 && num - check < 2))
 						continue;
 					if(el.tagName().equals("a") || el.tagName().equals("img"))
 						continue;
 					if(el.children().size() >1 || el.text().trim().length() < 2 
 							|| el.tagName().equalsIgnoreCase("span") 
 							|| el.tagName().equalsIgnoreCase("div")||el.tagName().equalsIgnoreCase("strong")||el.tagName().contains("."))
 						continue;									
 					result.append("!##!" + el.text().trim());
 				}
 				if(result.length() == 0 && !main.text().toLowerCase().contains("copyright")){
 					result.append("!##!" +main.text());
 				}
 			}
 		return result.toString();
 	}
 		
 	public List<String> cleanImgUrls(List<String> scrs, String line){
 		String html = Doc.html();
 		List<String> results = new ArrayList<String>();
 		if(line.indexOf("，") >= 0){
 			line = line.substring(0,line.indexOf("，"));
 		}
 		if(line.indexOf(",") >= 0){
 			line = line.substring(0,line.indexOf(","));
 		}
 		int index_last_line = html.indexOf(line);
 		
 		if(index_last_line < 0){
 			return scrs;
 		}		
 		for(String scr : scrs){
 			int tmp = html.indexOf(scr);			
 			if(tmp < index_last_line){
 				results.add(scr);
 			}
 		}
 		return results;
 	}
 	
 	public List<String> getImgsG(Element main){
 		
 		List<String> urls = new ArrayList<String>();
 		if(main == null){
 			return urls;
 		}
 		Elements els = main.getElementsByTag("img");
 		for(Element el : els){
 			if(el.children().size() == 0 && el.tagName().equals("img")){
 				String tmp_url = el.attr("src");
 				if(tmp_url.length() > 0 && tmp_url.indexOf("http") == 0 ){
 					urls.add(tmp_url);
 				}
 			}
 		}
 		return urls;
 	}
 	
 	private String cleanTitle(String rawTitle){
 		if(rawTitle.indexOf("_") > 0){
 			rawTitle = rawTitle.substring(0, rawTitle.indexOf("_"));
 		}
 		if(rawTitle.indexOf("(") > 0){
 			rawTitle = rawTitle.substring(0, rawTitle.indexOf("("));
 		}
 		if(rawTitle.indexOf("[") > 0){
 			rawTitle = rawTitle.substring(0, rawTitle.indexOf("["));
 		}
 		if(rawTitle.indexOf("--") > 0){
 			rawTitle = rawTitle.substring(0, rawTitle.indexOf("--"));
 		}
		//no use now
//		if(rawTitle.indexOf("-") > 0){
//			rawTitle = rawTitle.substring(0, rawTitle.indexOf("-"));
//		}
 		return rawTitle;
 	}
 		
 	public String getTitleG(){
 		String result = "";
 		if(Doc == null)
 			return result;
 		String rawTitle = Doc.title();
 		result = rawTitle;
 		Elements elh1 = Doc.getElementsByTag("h1");
 		if(elh1.size() > 0){
 			for(Element el : elh1){
 				if(el.children().size() == 0){
 					result = el.text();
 					break;
 				}
 			}
 			if(!rawTitle.contains(result)){
 				result = rawTitle;
 			}else{
 				return result;
 			}
 		}		
 		result = cleanTitle(result);		
 		return result;
 	}
 	
 	
 	
 }
