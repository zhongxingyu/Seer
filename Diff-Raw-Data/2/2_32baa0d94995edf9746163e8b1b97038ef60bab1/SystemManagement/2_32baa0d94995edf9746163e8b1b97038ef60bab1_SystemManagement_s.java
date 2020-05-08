 package com.cqlybest.site.controller.admin;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.cqlybest.common.controller.ControllerHelper;
 import com.cqlybest.common.service.CentralConfig;
 import com.cqlybest.common.service.ImageService;
 import com.cqlybest.common.service.SettingsService;
 
 @Controller
 public class SystemManagement extends ControllerHelper {
 
   @Autowired
   private SettingsService settingsService;
   @Autowired
   private ImageService imageService;
 
   @RequestMapping("/system.do")
   public String system() {
     return "/v1/system";
   }
 
   @RequestMapping("/system/cache.do")
   public String cache(Model model) {
     Boolean cacheEnabled =
         Boolean.TRUE
             .equals(((Map<?, ?>) settingsService.getSettings().get("cache")).get("enabled"));
     model.addAttribute("cacheEnabled", cacheEnabled);
     if (cacheEnabled) {
       String cacheDirPath = centralConfig.get(CentralConfig.CACHE_DIR);
       File cachDir = new File(cacheDirPath);
       List<Map<String, Object>> result = new ArrayList<>();
       Collection<File> files =
           FileUtils.listFiles(cachDir, new String[] {"html", "js", "css", "jpg", "png", "gif"},
               true);
       for (File file : files) {
         Map<String, Object> fileMap = new HashMap<>();
         fileMap.put("name", file.getAbsolutePath().substring(cachDir.getAbsolutePath().length()));
         fileMap.put("time", new Date(file.lastModified()));
         result.add(fileMap);
       }
       Collections.sort(result, new Comparator<Map<String, Object>>() {
         @Override
         public int compare(Map<String, Object> o1, Map<String, Object> o2) {
          return (int) (((Date) o2.get("time")).getTime() - ((Date) o1.get("time")).getTime());
         }
       });
       model.addAttribute("caches", result);
     }
     return "/v1/system/cache";
   }
 
   @RequestMapping("/system/cache/clean.do")
   @ResponseBody
   public void cleanCache() throws IOException {
     FileUtils.cleanDirectory(new File(centralConfig.get(CentralConfig.CACHE_DIR)));
   }
 
   /**
    * 系统设置
    */
   @RequestMapping("/system/settings.do")
   public String settings(Model model) {
     model.addAttribute("settings", settingsService.getSettings());
     return "/v1/system/settings";
   }
 
   /**
    * 更改系统设置
    * 
    * @throws IOException
    */
   @RequestMapping("/system/settings/update.do")
   @ResponseBody
   public void updateSettings(@RequestParam String name,
       @RequestParam(required = false) String value,
       @RequestParam(value = "value[]", required = false) List<String> values) throws IOException {
     Object _value = value == null ? values : value;
     if ("watermark.img".equals(name) || "basic.logo".equals(name)) {
       _value = imageService.getImage(value);
     }
     if ("cache.enabled".equals(name)) {
       _value = "true".equals(value);
       FileUtils.cleanDirectory(new File(centralConfig.get(CentralConfig.CACHE_DIR)));
     }
 
     settingsService.updateSettings(name, _value);
   }
 
   /**
    * 系统信息：环境变量、系统属性
    */
   @RequestMapping("/system/info.do")
   public String info(Model model) {
     model.addAttribute("env", System.getenv());
     model.addAttribute("property", System.getProperties());
     return "/v1/system/info";
   }
 
 }
