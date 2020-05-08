 package cn.mobiledaily.web.controller;
 
 import cn.mobiledaily.module.exhibition.domain.Exhibition;
 import cn.mobiledaily.module.news.domain.News;
 import cn.mobiledaily.common.exception.InternalServerError;
 import cn.mobiledaily.common.exception.InvalidValueException;
 import cn.mobiledaily.common.exception.ValidationException;
 import cn.mobiledaily.module.exhibition.service.ExhibitionService;
 import cn.mobiledaily.common.service.FileService;
 import cn.mobiledaily.module.news.service.NewsService;
 import cn.mobiledaily.common.service.SecurityService;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.util.LinkedList;
 import java.util.List;
 
 @Controller
 @RequestMapping("news")
 public class NewsController {
     private Logger logger = LoggerFactory.getLogger(NewsController.class);
     @Autowired
     private ExhibitionService exhibitionService;
     @Autowired
     private NewsService newsService;
     @Autowired
     private FileService fileService;
     @Autowired
     private SecurityService securityService;
 
     @ResponseStatus(HttpStatus.OK)
     @RequestMapping(value = "put", method = RequestMethod.POST)
     public void saveNews(
             String pwd,
             String exKey,
             String newsKey,
             MultipartFile icon,
             String title,
             MultipartFile content
     ) {
         try {
             if (!securityService.isValidExchangePassword(pwd)) {
                 throw new InvalidValueException("pwd", "****", pwd);
             }
             if (!StringUtils.isAlphanumeric(exKey)) {
                 throw new InvalidValueException("exKey", "[0-9A-Za-z]", exKey);
             }
             if (!StringUtils.isAlphanumeric(newsKey)) {
                 throw new InvalidValueException("newsKey", "[0-9A-Za-z]", newsKey);
             }
             Exhibition exhibition = exhibitionService.findByExKey(exKey);
             if (exhibition == null) {
                 throw new InvalidValueException("exKey", "existed exhibition", exKey);
             }
             News news = newsService.findByNewsKey(newsKey, exhibition);
             if (news == null) {
                 news = new News();
                 news.setExhibition(exhibition);
                news.setNewsKey(newsKey);
             }
             if (StringUtils.isNotEmpty(title)) {
                 news.setTitle(title);
             }
             if (icon != null) {
                 fileService.save(icon.getInputStream(), exKey + "/news/" + newsKey + ".png");
             }
             if (content != null) {
                 fileService.save(content.getInputStream(), exKey + "/news/" + newsKey + ".html");
             }
             newsService.save(news);
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/news/put", e);
             throw new InternalServerError("/news/put", e);
         }
     }
 
     @ResponseStatus(HttpStatus.OK)
     @RequestMapping(value = "delete", method = RequestMethod.POST)
     public void deleteNews(String pwd, String exKey, String newsKey) {
         try {
             if (!securityService.isValidExchangePassword(pwd)) {
                 throw new InvalidValueException("pwd", "****", pwd);
             }
             newsService.delete(exKey, newsKey);
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/news/delete", e);
             throw new InternalServerError("/news/delete", e);
         }
     }
 
     @RequestMapping("find")
     @ResponseBody
     public NewsObjectWrapper findNews(String exKey) {
         try {
             NewsObjectWrapper wrapper = new NewsObjectWrapper();
             List<News> newses = newsService.findByExKey(exKey);
             for (News news : newses) {
                 wrapper.addNews(new NewsObject(news));
             }
             return wrapper;
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/news/find", e);
             throw new InternalServerError("/news/find", e);
         }
     }
 
     public static class NewsObjectWrapper {
         String exKey;
         List<NewsObject> list = new LinkedList<>();
 
         public void addNews(NewsObject newsObject) {
             list.add(newsObject);
         }
 
         public String getExKey() {
             return exKey;
         }
 
         public List<NewsObject> getList() {
             return list;
         }
     }
 
     public static class NewsObject {
         String newsKey;
         String title;
         long createdAt;
 
         public NewsObject(News news) {
             newsKey = news.getNewsKey();
             title = news.getTitle();
             createdAt = news.getCreatedAt().getTime();
         }
 
         public String getNewsKey() {
             return newsKey;
         }
 
         public void setNewsKey(String newsKey) {
             this.newsKey = newsKey;
         }
 
         public String getTitle() {
             return title;
         }
 
         public void setTitle(String title) {
             this.title = title;
         }
 
         public long getCreatedAt() {
             return createdAt;
         }
 
         public void setCreatedAt(long createdAt) {
             this.createdAt = createdAt;
         }
     }
 }
