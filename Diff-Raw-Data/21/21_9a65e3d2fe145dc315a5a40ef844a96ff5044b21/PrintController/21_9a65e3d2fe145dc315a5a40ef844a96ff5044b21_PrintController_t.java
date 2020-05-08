 package com.rockontrol.yaogan.controller;
 
 import java.io.File;
 import java.net.URI;
 import java.util.UUID;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.rockontrol.yaogan.service.IPrintImageService;
 
 @Controller
 @RequestMapping(value = { "user/print", "admin/print" })
 public class PrintController {
 
    @Autowired
    private IPrintImageService service;
 
    @RequestMapping(value = "")
    public String print(Model model, @RequestParam("placeId") Long placeId,
          @RequestParam("time") String time, @RequestParam("category") String category) {
       File map = null;
       UUID uuid = UUID.randomUUID();
       try {
          map = service.getMap(placeId, time, category, this.getWebRootPath()
                + "public/temp/" + uuid.hashCode() + ".jpg");// TODO
       } catch (Exception e) {
          e.printStackTrace();
       }
      System.out.println("mapPath" + map.getAbsolutePath());
       model.addAttribute("mapPath", map.getAbsolutePath());
       model.addAttribute("mapName", uuid.hashCode() + ".jpg");
       return "/layers/print";
    }
 
    @RequestMapping("/preview")
    public String preview(Model model, @RequestParam("comment") String comment,
          @RequestParam("mapPath") String mapPath) {
       String fileName = UUID.randomUUID().hashCode() + ".jpg";
       try {
          String webRootPath = this.getWebRootPath();
          File template = service.copyTemplate(webRootPath
                + "public/template/template1.jpg", webRootPath + "public/themeImage/"
                + fileName); // TODO
          service.addComment(template, comment);
          File image = new File(mapPath);

          service.addShapeLayer(template, image);
       } catch (Exception e) {
          e.printStackTrace();
       }
       model.addAttribute("fileName", fileName);
       return "/layers/preview";
    }
 
    /**
     * "yaogan-web/"
     * 
     * @return
     * @throws Exception
     */
    private String getWebRootPath() throws Exception {
       URI uri = PrintController.class.getResource("/").toURI();
      String filePath = uri.toURL().getFile();
       String flag = "WEB-INF";
      String webRootPath = filePath.substring(0, filePath.indexOf(flag));
       return webRootPath;
    }

 }
