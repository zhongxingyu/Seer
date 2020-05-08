 package com.tools.tvguide.managers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.tools.tvguide.utils.HtmlUtils;
 
 import android.content.Context;
 import android.util.Log;
 
 public class ProgramHtmlManager 
 {
     private Context mContext;
     
     public ProgramHtmlManager(Context context)
     {
         assert (context != null);
         mContext = context;
     }
     
     public interface ProgramDetailCallback
     {
         void onTitleLoaded(int requestId, String title);
         void onProfileLoaded(int requestId, String profile);
         void onSummaryLoaded(int requestId, String summary);
         void onPictureLinkParsed(int requestId, String link);
         void onEpisodeLinkParsed(int requestId, String link);
     }
     
     public void getProgramDetailAsync(final int requestId, final String programUrl, final ProgramDetailCallback callback)
     {
         assert (callback != null);
         new Thread(new Runnable() 
         {
             @Override
             public void run() 
             {
                 try 
                 {
                     Document doc = HtmlUtils.getDocument(programUrl);
                     
                     // -------------- 获取Title --------------
                     // 返回结果
                     String title = "";
                     Element titleElement = doc.select("h1.lt[itemprop=name]").first();
                     if (titleElement != null)
                     {
                         title = titleElement.text().trim();
                     }
                     if (title != null && !title.equals(""))
                         callback.onTitleLoaded(requestId, title);
                     
                     // -------------- 获取Profile --------------
                     // 返回结果
                     String profile = "";
                     Elements profileElements = doc.select("div.abstract-wrap table.obj_meta tbody tr");
                     for (int i=0; i<profileElements.size(); ++i)
                     {
                         Element profileElement = profileElements.get(i);
                         String key = "";
                         String value = "";
                         
                         Element keyElement = profileElement.select("td").first();
                         if (keyElement != null)
                             key = keyElement.ownText();
                         
                         Element valueElement = profileElement.select("td").last();
                         if (valueElement != null)
                             value = valueElement.text();
                         
                         profile += key + value + "\n";
                     }
                     
                     callback.onProfileLoaded(requestId, profile);
                     
                     // -------------- 获取图片链接 --------------
                     // 返回结果
                     String picLink = null;
                     Element divElement = doc.getElementById("mainpic");
                     if (divElement != null)
                     {
                         Element imgElement = divElement.select("img[src]").first();
                         if (imgElement != null)
                             picLink = imgElement.attr("abs:src");
                     }
                     if (picLink != null)
                         callback.onPictureLinkParsed(requestId, picLink);
                     
                     // -------------- 获取剧情概要 --------------
                     // 返回结果
                     String summary = "";
                    Element descriptionElement = null;
                    if (programUrl.contains("competition"))     // 赛事：无detail页
                    {
                        descriptionElement = doc.getElementsByAttributeValue("itemprop", "description").first();
                    }
                    else
                    {
                        String descriptionLink = programUrl + "/detail";
                        Document descriptionDoc = HtmlUtils.getDocument(descriptionLink);
                        descriptionElement = descriptionDoc.getElementsByAttributeValue("itemprop", "description").first();
                    }
                    
                     if (descriptionElement != null)
                     {
                         Elements pargfs = descriptionElement.select("p");
                         for (int i=0; i<pargfs.size(); ++i)
                         {
                            if (pargfs.get(i).text().trim().equals(""))
                                continue;
                             summary += "　　" + pargfs.get(i).text() + "\n\n";
                         }
                     }
                     
                     callback.onSummaryLoaded(requestId, summary);
                     
                     // -------------- 获取分集链接 --------------
                     String episodesLink = null;
                     Element dlElement = doc.select("dl.hdtab").first();
                     if (dlElement != null)
                     {
                         Elements links = dlElement.select("a[href]");
                         for (int i=0; i<links.size(); ++i)
                         {
                             String text = links.get(i).ownText();
                             if (text.startsWith("分集剧情"))
                                 episodesLink = links.get(i).attr("abs:href");
                         }
                     }
                     if (episodesLink != null)
                         callback.onEpisodeLinkParsed(requestId, episodesLink);
                 }
                 catch (IOException e) 
                 {
                     e.printStackTrace();
                 }
             }
         }).start();
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
