 package com.tools.tvguide.managers;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.tools.tvguide.utils.Utility;
 
 import android.content.Context;
 
 public class HotHtmlManager 
 {
     private Context mContext;
     private List<String> mChannelList;
     private List<List<HashMap<String, String>>> mProgramsList;
     
     public class HotEntry
     {
         public String channelName;
         public List<HashMap<String, String>> programList;
     }
     
     public interface ProgramDetailCallback
     {
         void onProfileLoaded(int requestId, String profile);
         void onSummaryLoaded(int requestId, List<String> paragraphs);
         void onPicureLinkParsed(int requestId, String picLink);
         void onActorsLoaded(int requestId, String actors);
         void onPlayTimesLoaded(int requestId, HashMap<String, List<String>> playTimes);
         void onEpisodesLoaded(int requestId, List<HashMap<String, String>> episodes);
     }
     
     public interface EpisodeDetailCallback
     {
         void onEpisodeLoaded(int requestId, List<HashMap<String, String>> episodes);
     }
     
     public HotHtmlManager(Context context)
     {
         mContext = context;
         mChannelList = new ArrayList<String>();
         mProgramsList = new ArrayList<List<HashMap<String,String>>>();
     }
     
     public List<HotEntry> getEntryList()
     {
         List<HotEntry> entryList = new ArrayList<HotHtmlManager.HotEntry>();
         
         try 
         {
             String url = AppEngine.getInstance().getUrlManager().getUrl(UrlManager.URL_HOT);
             String protocol = new URL(url).getProtocol();
             String host = new URL(url).getHost();
             
             Document doc = getDocument(url);
             Elements channels = doc.select("div[class=rb_tv]");
             for (int i=0; i<channels.size(); ++i)
             {
                 Element channel = channels.get(i);
                 Elements links = channel.select("a");
                 String channelName = links.first().text();
                 mChannelList.add(channelName);
             }
             
             Elements programClassifies = doc.select("div[class=rb_tv_jm rb_tv_jm2]");
             for (int i=0; i<programClassifies.size(); ++i)
             {
                 Element programClassify = programClassifies.get(i);
                 Elements programs = programClassify.select("a");
                 List<HashMap<String, String>> programList = new ArrayList<HashMap<String,String>>();
                 for (int j=0; j<programs.size(); ++j)
                 {
                     Element program = programs.get(j);
                     String name = program.text();
                     String href;
                     if (!program.attr("href").contains("http://"))
                         href = protocol + "://" + host + "/" + program.attr("href");
                     else
                         href = program.attr("href");
                     
                     HashMap<String, String> item = new HashMap<String, String>();
                     item.put("name", name);
                     item.put("link", href);
                     programList.add(item);
                 }
                 mProgramsList.add(programList);
             }
             
             // 保证是一一对应的，否则错位，结果肯定不对
             assert(mChannelList.size() == mProgramsList.size());
             
             for (int i=0; i<mChannelList.size(); ++i)
             {
                 HotEntry entry = new HotEntry();
                 entry.channelName = mChannelList.get(i);
                 entry.programList = mProgramsList.get(i);
                 entryList.add(entry);
             }
         }
         catch (IOException e) 
         {
             e.printStackTrace();
         }
         
         return entryList;
     }
     
     public void getProgramDetailAsync(final int requestId, final String programLink, final ProgramDetailCallback callback)
     {
         assert(callback != null);
         new Thread(new Runnable() 
         {
             @Override
             public void run() 
             {
                 try 
                 {
                     String protocol = new URL(programLink).getProtocol();
                     String host = new URL(programLink).getHost();
                     Document doc = getDocument(programLink);
                     Elements outline = doc.select("div[class=tv_info2]");
                     Elements classifyLinks = doc.select("div[class=slide_02_dot slide_list_dot color-blue] a");
                     
                     String summary = "";
                     if (outline.size() > 0)
                         summary = outline.first().toString();
                     
                     // -------------- 获取Profile --------------
                     String profile = "";
                     profile += getProfileElement(summary, "导演");
                     profile += getProfileElement(summary, "编剧");
                     profile += getProfileElement(summary, "主演");
                     profile += getProfileElement(summary, "集数");
                     profile += getProfileElement(summary, "类型");
                     profile += getProfileElement(summary, "上映时间");
                     callback.onProfileLoaded(requestId, profile);
 
                     // -------------- 获取剧情概要 --------------
                     List<String> paragraphs = new ArrayList<String>();
                     boolean found = false;
                     String[] lines = summary.split("\n");
                     for (int i=0; i<lines.length; ++i)
                     {
                         String line = lines[i];
                         if (found == false && line.contains("剧情梗概"))
                         {
                             found = true;
                             continue;
                         }
                         if (found)
                         {
                             String text = Utility.trimChineseSpace(Html2Text(line).trim());
                             if (text.length() > 0)
                                 paragraphs.add(text.trim());
                         }
                     }
                     callback.onSummaryLoaded(requestId, paragraphs);
                     
                     // -------------- 获取图片链接 --------------
                     Elements tv_info_img = doc.select("div[class=tv_info] img");
                     String picLink = "";
                     if (tv_info_img.size() > 0)
                     {
                         picLink = tv_info_img.first().attr("src");
                     }
                     callback.onPicureLinkParsed(requestId, picLink);
                     
                     // -------------- 获取分集信息 --------------
                     List<HashMap<String, String>> episodeList = new ArrayList<HashMap<String,String>>();
                     Elements tv_info_a = doc.select("div[class=tv_info] a");
                     for (int i=0; i<tv_info_a.size(); ++i)
                     {
                         String href = tv_info_a.get(i).attr("href");
                         if (href.contains("CurrentPage"))
                         {
                             HashMap<String, String> item = new HashMap<String, String>();
                             item.put("name", tv_info_a.get(i).text());
                             item.put("link", protocol + "://" + host + "/" + href);
                             episodeList.add(item);
                         }
                     }
                     callback.onEpisodesLoaded(requestId, episodeList);
                     
                     // -------------- 获取演员表 --------------
                     String actors = "";
                     if (classifyLinks.size() > 0)
                     {
                         String actorLink = protocol + "://" + host + "/" + classifyLinks.get(0).attr("href");
                         Document actorDoc = getDocument(actorLink);
                         Elements tv_info2 = actorDoc.select("div[class=tv_info2]");
                         if (tv_info2.size() > 0)
                         {
                             String[] actorLines = tv_info2.first().toString().split("\n");
                             for (int i=0; i<actorLines.length; ++i)
                             {
                                 String actor = Utility.trimChineseSpace(Html2Text(actorLines[i]).trim());
                                 if (actor.length() > 0)
                                     actors += actor + "\n";
                             }
                         }
                     }
                     callback.onActorsLoaded(requestId, actors);
                     
                     // -------------- 获取播出时间 --------------
                     LinkedHashMap<String, List<String>> playTimes = new LinkedHashMap<String, List<String>>();
                     if (classifyLinks.size() > 3)
                     {
                         String playTimeLink = protocol + "://" + host + "/" + classifyLinks.get(3).attr("href");
                         Document playTimeDoc = getDocument(playTimeLink);
                         Elements tv_info2 = playTimeDoc.select("div[class=tv_info2]");
                         if (tv_info2.size() > 0)
                         {
                             Elements channels = tv_info2.select("div[class=tdbg]");
                             // 遍历每个频道，获取相应的节目单
                             for (int i=0; i<channels.size(); ++i)
                             {
                                 Element channel = channels.get(i);
                                 Elements tv_time_uls = channel.parent().select("div[class=tv_time] ul");
                                 List<String> result = new ArrayList<String>();
                                 if (tv_time_uls.size() > 0)
                                 {
                                     // 获取仅与当前频道相关的节目信息，过滤其它频道的节目信息
                                     Elements tv_time_ul = tv_time_uls.first().children();
                                     for (int j=0; j<tv_time_ul.size(); ++j)
                                     {
                                         if (tv_time_ul.get(j).nodeName().equals("li"))
                                         {
                                             Element tv_time = tv_time_ul.get(j);
                                             result.add(tv_time.text());
                                         }
                                     }
                                 }
                                 String channelName = channel.text();
                                 playTimes.put(channelName, result);
                             }
                         }
                     }
                     callback.onPlayTimesLoaded(requestId, playTimes);
                 }
                 catch (IOException e) 
                 {
                     e.printStackTrace();
                 }
             }
         }).start();
     }
     
     public void getEpisodesAsync(final int requestId, final String link, final EpisodeDetailCallback callback)
     {
         assert(callback != null);
         new Thread(new Runnable() 
         {            
             @Override
             public void run() 
             {
                 try 
                 {
                     List<HashMap<String, String>> episodes = new ArrayList<HashMap<String,String>>();
                     Document doc = getDocument(link);
                     Elements content = doc.select("div[class=tv_info2]");
                     
                     if (content.size() > 0)
                     {
                         String html = content.first().toString();
                         Elements titleElements = content.first().select("strong");
                         List<String> titleList = new ArrayList<String>();
                         List<String> plotList = new ArrayList<String>();
                         for (int i=0; i<titleElements.size(); ++i)
                         {
                             titleList.add(titleElements.get(i).text());
                         }
                         
                         String[] lines = html.split("\n");
                         for (int i=0; i<lines.length; ++i)
                         {
                             String line = Html2Text(lines[i]).trim();
                             if (line.length() == 0)
                                 continue;
                             
                             if (titleList.indexOf(line) != -1)        // 找到第X集
                             {
                                 for (int j=i+1; j<lines.length; ++j)  // 寻找与该集对应的剧情
                                 {
                                     String detailString = Utility.trimChineseSpace(Html2Text(lines[j]).trim());
                                     if (detailString.length() > 0)
                                     {
                                         plotList.add(detailString);
                                         break;
                                     }
                                 }
                             }
                         }
                         
                         // 只有两者相等才有意义
                         if (titleList.size() == plotList.size())
                         {
                             for (int i=0; i<titleList.size(); ++i)
                             {
                                 HashMap<String, String> episode = new HashMap<String, String>();
                                 episode.put("title", titleList.get(i));
                                 episode.put("plot", plotList.get(i));
                                 episodes.add(episode);
                             }
                         }
                     }
                     callback.onEpisodeLoaded(requestId, episodes);
                 } 
                 catch (IOException e) 
                 {
                     e.printStackTrace();
                 }
             }
         }).start();
     }
     
     private String getProfileElement(String summary, String element)
     {
         String profile = "";
         Pattern resultPattern = Pattern.compile(element + ".+");       // (.+)为贪婪匹配
         Matcher resultMatcher = resultPattern.matcher(summary);
         if (resultMatcher.find())
         {
             profile += resultMatcher.group();
         }
         if (profile.length() > 0)
             return profile.trim() + "\n";
         else
             return profile;
     }
     
     private Document getDocument(String url) throws IOException
     {
         CacheManager cacheManager = AppEngine.getInstance().getCacheManager();
         String html = cacheManager.get(url);
         Document doc;
         if (html == null)
         {
            doc = Jsoup.connect(url).timeout(10000).get();
             cacheManager.set(url, doc.html());
         }
         else
         {
             doc = Jsoup.parse(html);
         }
         return doc;
     }
     
     /**
      * Copy from Internet
      * @param 含有HTML标签的HTML文本
      * @return 除去HTML标签的文本
      */
     public static String Html2Text(String inputString) 
     {    
         String htmlStr = inputString; // 含html标签的字符串    
         String textStr = "";    
         java.util.regex.Pattern p_script;    
         java.util.regex.Matcher m_script;    
         java.util.regex.Pattern p_style;    
         java.util.regex.Matcher m_style;    
         java.util.regex.Pattern p_html;    
         java.util.regex.Matcher m_html;    
   
         java.util.regex.Pattern p_html1;    
         java.util.regex.Matcher m_html1;    
   
         try
         {
             String regEx_script = "<[//s]*?script[^>]*?>[//s//S]*?<[//s]*?///[//s]*?script[//s]*?>"; // 定义script的正则表达式{或<script[^>]*?>[//s//S]*?<///script>    
             String regEx_style = "<[//s]*?style[^>]*?>[//s//S]*?<[//s]*?///[//s]*?style[//s]*?>"; // 定义style的正则表达式{或<style[^>]*?>[//s//S]*?<///style>    
             String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式    
             String regEx_html1 = "<[^>]+";    
             p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);    
             m_script = p_script.matcher(htmlStr);    
             htmlStr = m_script.replaceAll(""); // 过滤script标签    
   
             p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);    
             m_style = p_style.matcher(htmlStr);    
             htmlStr = m_style.replaceAll(""); // 过滤style标签    
   
             p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);    
             m_html = p_html.matcher(htmlStr);    
             htmlStr = m_html.replaceAll(""); // 过滤html标签    
   
             p_html1 = Pattern.compile(regEx_html1, Pattern.CASE_INSENSITIVE);    
             m_html1 = p_html1.matcher(htmlStr);    
             htmlStr = m_html1.replaceAll(""); // 过滤html标签    
   
             textStr = htmlStr;
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
   
         return textStr;// 返回文本字符串    
     }   
 }
