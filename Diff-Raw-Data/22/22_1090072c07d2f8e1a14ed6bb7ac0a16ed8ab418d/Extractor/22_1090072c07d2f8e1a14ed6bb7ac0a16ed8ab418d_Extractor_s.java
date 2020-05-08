 package com.imdevice.WebSpider;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.parser.Tag;
 import org.jsoup.safety.Cleaner;
 import org.jsoup.safety.Whitelist;
 import org.jsoup.select.Elements;
 
 public class Extractor {
 
     public static final String ATTR_CONTENT_SCORE = "contentScore";
     public static final String DOM_DEFAULT_CHARSET = "utf-8";
     public static final String bonus="(^|\\s)(post|hentry|entry|article|single|main)[-_]?(content|single|text|body|box)?(\\s|$)";
     public static final String deduction="(?i)comment|meta|footer|footnote|subcontent|title";
     public static final String noise="(?i)[-_]?(googleAd|dig|jiathis|author|ignore|comment|reply|recommend|related|"
     		+ "meta|copyright|header|footer|footnote|sns|share|social|tag|nav|prenext|sidebar|krSide|widget-container|widget|"
     		+ "profile|button|btn|filed|weixinzone|clients|geek-observer|copyform)[-_]?";
     protected Document doc = null;
     private ArrayList<Element> scoredNodes = new ArrayList<Element>();
     private ArrayList<Element> matchedNodes = new ArrayList<Element>();
     protected String url="";
     protected String chart="";
    public String title="";
     public String clearContent="";
     public double factor=0.98;
     public boolean debug=false;
    
     public String getUrl() {
 		return url;
 	}
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
     public Extractor(){
     	
     }
     public Extractor(String url){
     	this.url=url;
     }
     private Element preClean(Element element){
     	//此过滤针对整个body
     	//String[] junkTags = {"style", "form", "iframe", "script", "button", "input", "textarea"};
     	//for(String tagName:junkTags)element.getElementsByTag(tagName).remove();
     	// 需要删除的标签 (2013/6/26 有些正文居然放在form里面！)
     	element.select("style,iframe,script,button,input,textarea,header,footer,hr,noscript,nav").remove();
     	//删除内容块内的噪音干扰
     	//String noise="(?i)[-_]?(googleAd|dig|jiathis|author|ignore|comment|reply|recommend|related|meta|copyright|header|footer|footnote|sns|share|social|tag|nav|prenext|sidebar|krSide|widget-container|widget|profile|button|btn|filed)[-_]?";
     	String noiseQuery="[class~="+noise+"],[id~="+noise+"]";
     	Elements noiseEs=element.select(noiseQuery);
     	Pattern bonusReg =Pattern.compile(bonus);
     	for(Element ne:noiseEs){
     		//System.out.println("ne.id:"+ne.id());
     		//System.out.println("ne.className:"+ne.className());
     		if(!ne.tagName().equals("article")){
     			if(!bonusReg.matcher(ne.id().toLowerCase()).find()){
     			//if(!ne.id().toLowerCase().matches(bonus)){
     				if(!bonusReg.matcher(ne.className().toLowerCase()).find()){
     				//if(!ne.className().toLowerCase().matches(bonus)){
     					ne.remove();        				
     				}
     			}
     		}
     	}
     	element.select("[href*=javascript:]").remove();
     	// 需要删除的属性
     	String[] junkAttrs = {"style", "onload", "onclick", "onmouseover", "align", "border", "margin"};
     	for(String attributeKey:junkAttrs)element.select("*").removeAttr(attributeKey);
     	
     	//Removes "all span\font and that <a> has <img/> as direct children" node from the DOM, and moves its children up into the node's parent.
     	Elements spans=element.getElementsByTag("span");
     	for(Element span:spans)span.unwrap();
 		Elements fonts=element.getElementsByTag("font");
 		for(Element font:fonts)font.unwrap();
 		Elements imgs=element.select("img");
 		for(Element img:imgs){
 			Element imgWrap=img.parent();
 			if(imgWrap.tagName().equals("a"))imgWrap.unwrap();
 		}
 		
 		//remove html comment <!-- -->
     	String html=element.html().replaceAll("(?is)<!--.*?-->", "");
     	//remove special char
     	html=html.replaceAll("&.{2,6};|&#.{2,6};", "");
     	//remove continuous <a>
     	//String a2a="((<(?i)(li|p|span)[^>]*>)?\\s*<a[^>]*>.*</a>\\s*(</(?i)(li|p|span)>)?\\s*){2,}";
     	//html=html.replaceAll(a2a, "");
     	String a2a="((<(li|p|span)\\s*[^>]*>)?\\s*(<a\\s*[^>]*>[^<]*</a>)\\s*(</(li|p|span)>)?\\s*){2,}";
     	Pattern a2aReg=Pattern.compile(a2a, Pattern.CASE_INSENSITIVE);
     	a2aReg.matcher(html).replaceAll("");
     	element.html(html);
         if(!debug){
 			Elements es=element.getElementsMatchingText("转载请注明|本文链接地址");
 	    	for(Element e:es){
 	    		if(e.text().length()<100)e.remove();
 	    	} 
         }
         return element;
     }
     private String clean(Element element){
     	if(null==element) return "";
     	//此过滤针对已提取的Content Block
 
     	//change relative URL to absolute URL
     	Elements imgs=element.select("img[src]");
     	for(Element img:imgs){
     		img.attr("src", img.absUrl("src"));
     	}
     	if(doc.baseUri().startsWith("http://songshuhui.net")){
     		for(Element img:imgs){
         		img.attr("src", img.attr("file"));
         	}
     	}
         // remove all blank html tags
     	if(!debug){//上线后使用此条件
 	        Elements children=element.children();
 	        ArrayList<Element> blankNotes=new ArrayList<Element>();
 	        String mime="img,embed,video,audio,canvas,object";
 	        for(Element child:children){
 	        	if(!child.hasText()&&child.select(mime).isEmpty()){
 	        		blankNotes.add(child);
 	        		child.remove();
 	        	}
 	        }
         
         	element.select("*").removeAttr("class");       	
         }
         return element.html()==null?"":element.html();
     }
     private String ad_hoc(String feed){
     	switch (feed){
     	case "http://www.36kr.com/feed":
     		break;
     	case "":
     		break;
     	}
     	String result="";
     	return result;
     }
     private int getContentScore(Element p){
     	int cs=0;
     	String t=p.ownText();//.text();
     	if(t.length()>4){
 			cs+=t.length();
 			cs+=(t.split("，").length-1)*10;
 			cs+=(t.split("。").length-1)*10;
 			cs+=(t.split("、").length-1)*10;
 			cs+=(t.split("：").length-1)*3;
 			cs+=(t.split("！").length-1)*8;
 			cs+=(t.split(",").length-1)*6;
 			cs+=(t.split("!").length-1)*6;
 			cs+=(t.split(".").length-1)*2;
 			cs+=(t.split(":").length-1)*2;
 		}
     	return cs;
     }
     private int getStructureScore(Element p){
     	int ss=0;
     	String className=p.className();
 		String id=p.id();
 		
 		if(className.matches(deduction)){
 			ss-=50;
 		}else if(className.matches(bonus)){
 			ss+=50;
 		}
 		if(id.matches(deduction)){
 			ss-=50;
 		}else if(id.matches(bonus)){
 			ss+=50;
 		}		
     	return ss;
     }
     private void recordScore(Element sp,int cs, double factor){
     	int recordScore=sp.attr(ATTR_CONTENT_SCORE).isEmpty()?0:Integer.parseInt(sp.attr(ATTR_CONTENT_SCORE));
 		recordScore+=(cs*factor);
 		if(!scoredNodes.contains(sp)){
 			int ss=getStructureScore(sp);
 			recordScore+=ss;
 			if(debug)sp.addClass("Scored");
 			scoredNodes.add(sp);
 		}
 		sp.attr(ATTR_CONTENT_SCORE, recordScore+"");
     }
     public Element getTopBox(String bodyHtml){
     	Document doc =Jsoup.parseBodyFragment(bodyHtml);
     	return getTopBox(doc);
     }
     public Element getTopBox(Document doc){
     	findTitle();
     	Element body=preClean(doc.body());
     	Elements articles=body.getElementsByTag("<article>");
     	if(!articles.isEmpty())body=articles.first();
     	Element topBox=null;
 /*    	Elements imgs=body.select("img");
 		for(Element img:imgs){
 			matchedNodes.add(img.parent());
 		}*/
     	String punctuation="，、。；！？‘’“”,\\.;!\'\"";
     	String regex="["+punctuation+"][^"+punctuation+"]{5,}["+punctuation+"]";
     	Elements allParagraphs=body.getElementsMatchingOwnText(regex);//body.getElementsByTag("p");
     	for(Element p:allParagraphs){
     		String tag_p=p.tagName().toLowerCase();
     		if(debug)p.addClass("matched");
     		if(tag_p.matches("a"))continue;
     		matchedNodes.add(p);
     		int cs=getContentScore(p);
     		Element sp=/*p.parent();*/tag_p.equals("div")?p:p.parent();   
     		recordScore(sp,cs,1);
     		
     		String tag_sp=sp.tagName().toLowerCase();
     		if(tag_p.equals("div")){
     			Element spp=p.parent();
     			recordScore(spp,cs,factor);
     		}
     		if(tag_sp.matches("p|blockquote|pre")){
     			Element spp=sp.parent();
     			recordScore(spp,cs,factor);
     		}
     		
     	}
     	if(scoredNodes.size()>0){
 	    	int topBoxIndex=0;
 	    	int topScore=0;
 	    	int i=0;
 	    	for(Element node:scoredNodes){
 	    		int nodeScore=node.attr(ATTR_CONTENT_SCORE).isEmpty()?0:Integer.parseInt(node.attr(ATTR_CONTENT_SCORE));
 	    		if(nodeScore>topScore){
 	    			topScore=nodeScore;
 	    			topBoxIndex=i;
 	    		}
 	    		i++;
 	    	}
 	    	topBox=scoredNodes.get(topBoxIndex);
 	    	topBox.attr("isTop", "top");
 	    	if(debug)topBox.removeClass("Scored").addClass("TopScored");
     	}
     	return topBox;
     }
     public Element getContentBox(Element topBox){
     	//如果有match到的段落在topBox之外的，往上追溯2层，如在此2层之内则取该层为topBox=
     	Element contentBox=topBox;
     	Elements a=topBox.getAllElements();
     	Elements ap=topBox.parent().getAllElements();
     	Elements app=topBox.parent().parent().getAllElements();
     	for(Element m:matchedNodes){
     		if(!a.contains(m)){
     			if(ap.contains(m)){
     				contentBox=topBox.parent();continue;
     			}else if(app.contains(m)){
     				contentBox=topBox.parent().parent();break;
     			}
     		}
     	}
     	
 /*    	Element parent=topBox.parent();
     	Element contentBox=topBox;
     	while( !parent.attr(ATTR_CONTENT_SCORE).isEmpty()){
     		int parentScore=Integer.parseInt(parent.attr(ATTR_CONTENT_SCORE));
     		if(parentScore<50)break;
     		contentBox=parent;
     		parent=parent.parent();
     	}
 */
     	if(debug)contentBox.addClass("top-2-parent");
     	return contentBox;
     }
     public void findTitle(){
     	Elements hs=doc.body().select("h1,h2,h3,h4,h5");
     	boolean found=false;
     	for(Element h:hs){
     		for(Element c:h.getAllElements()){
     			if(title.indexOf(c.text().trim())>-1){
     				title=c.text().trim();found=true;
     				break;
     			}    				
     		}
     		if(found){h.remove();break;}    		
     	}
     	if(!found&&title.length()>0)title=title.split("[-_|]")[0].trim();    	
     }
     public String getTitle(){
     	return title;
     }
     public Element getTopBox1(Document doc){
     	Element body=preClean(doc.body());
     	Element topBox=null;
     	String punctuation="，、。：；！？‘’“”,;!\'\"";
     	String regex="["+punctuation+"]+[^\\s"+punctuation+"]+["+punctuation+"]+";
     	Elements allParagraphs=body.getElementsMatchingOwnText(regex);//body.getElementsByTag("p");
     	for(Element p:allParagraphs){
     		if(p.tagName().matches("(?i)a|h1|h2|h3"))continue;
     		Element scoredNode=p.tagName().equalsIgnoreCase("div")?p:p.parent();
     		int contentScore=0;
     		String className=scoredNode.className();
     		String id=scoredNode.id();
     		if(className.matches("(?i)(comment|meta|footer|footnote)")){
     			contentScore-=50;
     		}else if(className.matches("(?i)((^|\\s)(post|hentry|entry[-]?(content|text|body)?|article[-]?(content|text|body)?)(\\s|$))")){
     			contentScore+=25;
     		}
     		if(id.matches("(?i)(comment|meta|footer|footnote)")){
     			contentScore-=50;
     		}else if(id.matches("(?i)((^|\\s)(post|hentry|entry[-]?(content|text|body)?|article[-]?(content|text|body)?)(\\s|$))")){
     			contentScore+=25;
     		}
     		if(p.text().length()>4){
     			contentScore+=p.text().length();
     			String t=p.text();
     			contentScore+=(t.split("，").length-1)*10;
     			contentScore+=(t.split("。").length-1)*10;
     			contentScore+=(t.split("、").length-1)*10;
     			contentScore+=(t.split("：").length-1)*8;
     			contentScore+=(t.split("！").length-1)*8;
     			contentScore+=(t.split(",").length-1)*6;
     			contentScore+=(t.split("!").length-1)*6;
     			contentScore+=(t.split(".").length-1)*2;
     			contentScore+=(t.split(":").length-1)*3;
     		}
     		int recordScore=scoredNode.attr(ATTR_CONTENT_SCORE).isEmpty()?0:Integer.parseInt(scoredNode.attr(ATTR_CONTENT_SCORE));
     		recordScore+=contentScore;
     		scoredNode.attr(ATTR_CONTENT_SCORE, recordScore+"");
     		if(!scoredNodes.contains(scoredNode)){
     			scoredNodes.add(scoredNode);
     			scoredNode.addClass("Scored");
     		}
     		if(p.tagName().equalsIgnoreCase("div")){
     			Element parent=p.parent();
     			int parentscore=parent.attr(ATTR_CONTENT_SCORE).isEmpty()?0:Integer.parseInt(parent.attr(ATTR_CONTENT_SCORE));
     			parentscore+=contentScore*0.9;
     			if(!scoredNodes.contains(parent)){
 	    			String parentclassName=parent.className();
 	        		String parentid=parent.id();
 	        		if(parentclassName.matches("(?i)(comment|meta|footer|footnote)")){
 	        			parentscore-=50;
 	        		}else if(parentclassName.matches("(?i)((^|\\s)(post|hentry|entry[-]?(content|text|body)?|article[-]?(content|text|body)?)(\\s|$))")){
 	        			parentscore+=25;
 	        		}
 	        		if(parentid.matches("(?i)(comment|meta|footer|footnote)")){
 	        			parentscore-=50;
 	        		}else if(parentid.matches("(?i)((^|\\s)(post|hentry|entry[-]?(content|text|body)?|article[-]?(content|text|body)?)(\\s|$))")){
 	        			parentscore+=25;
 	        		}
     			}
     			parent.attr(ATTR_CONTENT_SCORE, parentscore+"");
         		if(!scoredNodes.contains(parent)){
         			scoredNodes.add(parent);
         			if(debug)parent.addClass("Scored");
         		}
     		}
     	}
     	//topBox=doc.createElement("div");
     	if(scoredNodes.size()>0){
 	    	int topBoxIndex=0;
 	    	int topScore=0;
 	    	int i=0;
 	    	for(Element node:scoredNodes){
 	    		int nodeScore=node.attr(ATTR_CONTENT_SCORE).isEmpty()?0:Integer.parseInt(node.attr(ATTR_CONTENT_SCORE));
 	    		if(nodeScore>topScore){
 	    			topScore=nodeScore;
 	    			topBoxIndex=i;
 	    		}
 	    		i++;
 	    	}
 	    	topBox=scoredNodes.get(topBoxIndex);
 	    	topBox.attr("isTop", "top");
 	    	if(debug)topBox.removeClass("Scored").addClass("TopScored");
     	}
     	return topBox;
     }
     public String drawChart(){
     	chart="<script type='text/javascript' src='jscharts.js'></script>";
     	chart+="<div id='chart_container'>Loading chart...</div>";
     	chart+="<script type='text/javascript'>";
     	chart+="var myChart = new JSChart('chart_container', 'bar', '');";
     	String chartdata="";
     	String colorize="";
     	String color="";
     	if(scoredNodes.size()>0){
     		for(Element node:scoredNodes){
     			int nodeScore=node.attr(ATTR_CONTENT_SCORE).isEmpty()?0:Integer.parseInt(node.attr(ATTR_CONTENT_SCORE));
     			chartdata+="['"+node.tagName()+"',"+nodeScore+"],";
     			color=node.attr("isTop").isEmpty()?"#FBE000":"#D60301";
     			colorize+="'"+color+"',";
     		}
     		chartdata=chartdata.substring(0,chartdata.length()-1);
     		colorize=colorize.substring(0,colorize.length()-1);
     	}
     	chart+="myChart.setDataArray([";
     	chart+=chartdata;
     	chart+="]);";
     	chart+="myChart.colorize(["+colorize+"]);";
     	chart+="myChart.setSize(800, 300);";
     	chart+="myChart.setBarValues(true);";
     	chart+="myChart.setBarOpacity(0.7);";
     	chart+="myChart.setBarSpacingRatio(5);";
     	chart+="myChart.setBarBorderWidth(0);";
     	chart+="myChart.setAxisValuesColor('#408F7F');";
     	chart+="myChart.setAxisNameX('');";
     	chart+="myChart.setAxisNameY('');";
     	chart+="myChart.setAxisNameColor('#408F7F');";
     	chart+="myChart.setAxisColor('#5DB0A0');";
     	chart+="myChart.setGridOpacity(0.8);";
     	chart+="myChart.setGridColor('#B9D7C9');";
     	chart+="myChart.setAxisValuesAngle(90);";
     	chart+="myChart.draw();";
     	chart+="</script>";
     	return chart;
     }
     public void extract(){
     	if(url.length()<5)return;
     	try {
 			doc=Jsoup.connect(url).get();
 			title=doc.title();
 			clearContent=clean(getContentBox(getTopBox(doc)));
 			if(debug)clearContent=doc.body().html()+drawChart();
 		} catch (Exception e) {
 			e.printStackTrace();
 			title="Error:"+e.getLocalizedMessage();
 			clearContent=e.getMessage();
 		}
     }
     public String getContent(){
     	return clearContent;
 
     }

     public String getContent(String html){
     	try {
     		doc=Jsoup.parseBodyFragment(html);
     		clearContent=clean(getContentBox(getTopBox(doc)));
 			if(debug)clearContent=doc.body().html()+drawChart();
 		} catch (Exception e) {
 			clearContent=e.getMessage();
 			e.printStackTrace();
 		}	
     	return clearContent;
     }
     /**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String url="";
 		//url="http://aio.zol.com.cn/337/3377553.html";
 		//url="http://www.ifanr.com/204906";
 		//url="http://thenextweb.com/google/2012/11/27/google-connects-its-play-store-with-google-public-reviews-will-now-feature-your-name-and-picture/?fromcat=all";
 		//url="http://www.36kr.com/p/174158.html";
 		// url="http://www.ifanr.com/200362";
 		// url="http://www.cnbeta.com/articles/215155.htm";
 		// url="http://cn.engadget.com/2012/11/22/jolla-wont-support-sailfish-on-nokia-n9/";
 		// url="http://cn.engadget.com/2012/11/22/nintendo-wii-u-finally-gets-youtube-app-works-on-gamepad-too/";
 		//url="http://www.engadget.com/2012/11/22/limited-edition-nook-simple-touch-hits-79-this-black-friday/";
 		//url="http://www.cnetnews.com.cn/2012/1123/2132633.shtml";
 		//url="http://www.cnetnews.com.cn/2012/1122/2132605.shtml";
 		//url="http://www.cnetnews.com.cn/2012/1123/2132607.shtml";
 		//url="http://java.chinaitlab.com/base/839277.html";
 		//url="http://tech.sina.com.cn/i/2012-11-23/07327824820.shtml";
 		//url="http://jsoup.org/cookbook/extracting-data/example-list-links";
 		//url="http://wenwen.soso.com/z/q137883977.htm";
 		//url="http://www.huxiu.com/article/6373/1.html";
 		//url="http://www.ibm.com/developerworks/cn/java/j-lo-jsouphtml/";
 		//url="http://tech.sina.com.cn/t/2012-11-29/02287840224.shtml";
 		//url="http://cn.engadget.com/2012/11/29/wsj-sharp-courting-us-firms-for-investments/";
 		//url="http://cn.engadget.com/2012/11/29/amd-considering-texas-campus-sale/";
 		//url="http://www.cnbeta.com/articles/216237.htm";
 		//url="http://www.huxiu.com/article/6588/1.html";
 		//url="http://www.jpbeta.net/2012/12/ique-3ds-xl-1-2012-1201/";
 		//url="http://www.cnbeta.com/articles/216970.htm";
 		//url="http://digi.tech.qq.com/a/20121207/000491.htm";
 		//url="http://www.chinaaet.com/article/index.aspx?id=24135";
		url="http://songshuhui.net/archives/82946";
 		URL u;
 		try {
 			u = new URL(url);
 			String host=u.getHost();
 			System.out.println(host);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		Extractor extrator=new Extractor(url);
 		extrator.debug=false;
 		extrator.extract();
 		System.out.println(extrator.getTitle());
 		System.out.println(extrator.getContent());
 		//System.out.println(extrator.drawChart());
 	}
 
 }
