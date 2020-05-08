 package com.digt.web;
 
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.shindig.auth.AuthInfo;
 import org.apache.shindig.auth.SecurityToken;
 import org.apache.shindig.common.servlet.GuiceServletContextListener;
 import org.apache.shindig.protocol.RestfulCollection;
 import org.apache.shindig.social.opensocial.spi.PersonService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.digt.common.utils.ActivityGenerator;
 import com.digt.model.StoreItem;
 import com.digt.spi.StoreService;
 import com.digt.web.beans.json.Message;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Level;
 import org.apache.shindig.social.opensocial.spi.CollectionOptions;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.servlet.HandlerMapping;
 
 @Controller
 @RequestMapping(value = WebUtil.APP_PFX)
 public class StoreController {
 
 	private static final Logger LOG = Logger.getLogger(
 			StoreController.class.getName());
 
 	@Inject
 	ActivityGenerator activGen;
 	@Inject
 	PersonService personSvc;
 	@Inject
 	StoreService storeSvc;
 	@Autowired
 	private ServletContext ctx;
 	
 	/**
 	 * Импорт документа из файла
 	 * Путь к создаваемому файлу берется из URL запроса, следующего после параметра {ws}
      * задающего название хранилища
      * 
 	 * @param request
 	 * @param response
 	 * @param file Элемент input http формы типа file
      * @param action Должен иметь значение 'importfile'
      * @param workspace Имя хранилища
      * 
 	 * @return статус операции Message
 	 * @see com.digt.web.beans.json.Message
      * @see MultipartFile
      * @exception ProtocolException
 	 */
 	@RequestMapping(value = "/store/{ws}/**", method = RequestMethod.POST, params = "action=importfile")
 	public ResponseEntity<String> importStoreItem(
 			HttpServletRequest request,
 			HttpServletResponse response,
             @PathVariable("ws") String workspace,
 			@RequestParam("file") MultipartFile file) throws InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		
         String path = getPath(request);
         Message ret;
         InputStream is = null;
         try {
             is = file.getInputStream();
             path += "/" + file.getOriginalFilename();
            storeSvc.createItem(WebUtil.USER_ME, workspace, path, is, request.getContentType(), token);
             ret = new Message(true, "Operation successful");
         } catch (Exception ex) {
             LOG.log(Level.SEVERE, null, ex);
             ret = new Message(false, "Operation error: " + ex.getMessage());
         } finally {
             try {
                 if (is != null) is.close();
             } catch (IOException e) {}
         }
         
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.add("Content-type", "text/html; charset=UTF-8");
 		ResponseEntity<String> res = 
                         new ResponseEntity<String>(ret.toString(), 
                         responseHeaders, HttpStatus.OK);
 		
 		return res;
 	}
     
     /**
      * Создание каталога
      * Путь к создаваемому каталогу берется из URL запроса, следующего после параметра {ws}
      * задающего название хранилища
      * 
      * @param request
      * @param workspace Имя хранилища
      * @param action Должен иметь значение 'createfolder'
      * 
      * @return статус операции Message
      * @see com.digt.web.beans.json.Message
      * @exception ProtocolException
     */
     @RequestMapping(value = "/store/{ws}/**", method = RequestMethod.POST, params = "action=createfolder")
     public ResponseEntity<String> createFolder(
             HttpServletRequest request,
             @PathVariable("ws") String workspace) {
         SecurityToken token = new AuthInfo(request).getSecurityToken();
 
         String path = getPath(request);
 
         storeSvc.createItem(WebUtil.USER_ME, workspace, path, null, null, token);
         Message ret = new Message(true, "Operation successful");
 
         HttpHeaders responseHeaders = new HttpHeaders();
         responseHeaders.add("Content-type", "text/html; charset=UTF-8");
         ResponseEntity<String> res = 
                         new ResponseEntity<String>(ret.toString(), 
                         responseHeaders, HttpStatus.OK);
 
         return res;
     }
     
     /**
      * Удаление объекта
      * Путь к удаляемому объекту берется из URL запроса, следующего после параметра {ws}
      * 
      * @param request
      * @param workspace
      * @param action
       
      * @return статус операции Message
      * @see com.digt.web.beans.json.Message
      * @exception ProtocolException
      */
     @RequestMapping(value = "/store/{ws}/**", method = RequestMethod.POST, params = "action=delete")
     public ResponseEntity<String> deleteItem(
             HttpServletRequest request,
             @PathVariable("ws") String workspace) {
         
         SecurityToken token = new AuthInfo(request).getSecurityToken();
         String path = getPath(request);
         storeSvc.deleteItem(WebUtil.USER_ME, workspace, path, token);
         Message ret = new Message(true, "Operation successful");
 
         HttpHeaders responseHeaders = new HttpHeaders();
         responseHeaders.add("Content-type", "text/html; charset=UTF-8");
         ResponseEntity<String> res = 
                         new ResponseEntity<String>(ret.toString(), 
                         responseHeaders, HttpStatus.OK);
 
         return res;
     }
 
     /**
      * Перемещение объекта
      * Путь к перемещаемому объекту берется из URL запроса, следующего после параметра {ws}
      * 
      * @param request
      * @param workspace
      * @param action
       
      * @return статус операции Message
      * @see com.digt.web.beans.json.Message
      * @exception ProtocolException
      */
     @RequestMapping(value = "/store/{ws}/**", method = RequestMethod.POST, params = "action=move")
     public ResponseEntity<String> moveItem(
             HttpServletRequest request,
             @RequestParam("to") String newPath,
             @PathVariable("ws") String workspace) {
         
         SecurityToken token = new AuthInfo(request).getSecurityToken();
         String path = getPath(request);
         storeSvc.moveItem(WebUtil.USER_ME, workspace, path, newPath, token);
         Message ret = new Message(true, "Operation successful");
 
         HttpHeaders responseHeaders = new HttpHeaders();
         responseHeaders.add("Content-type", "text/html; charset=UTF-8");
         ResponseEntity<String> res = 
                         new ResponseEntity<String>(ret.toString(), 
                         responseHeaders, HttpStatus.OK);
 
         return res;
     }
 
     /**
      * 
      * @param request
      * @param workspace
      * @param userIds
      * @param rights
      * @return 
      */
     @RequestMapping(value = "/store/{ws}/**", method = RequestMethod.POST, params = "action=move")
     public ResponseEntity<String> setRights(
             HttpServletRequest request,
             @PathVariable("ws") String workspace,
             @RequestParam("objId") String[] objIds,
             @RequestParam("acl") String[] rights) {
         
         SecurityToken token = new AuthInfo(request).getSecurityToken();
         String path = getPath(request);
         storeSvc.setAcl(WebUtil.USER_ME, 
                 ImmutableSet.copyOf(objIds), 
                 ImmutableSet.copyOf(rights),
                 workspace, path, token);
         Message ret = new Message(true, "Operation successful");
         HttpHeaders responseHeaders = new HttpHeaders();
         responseHeaders.add("Content-type", "text/html; charset=UTF-8");
         ResponseEntity<String> res = 
                         new ResponseEntity<String>(ret.toString(), 
                         responseHeaders, HttpStatus.OK);
 
         return res;
         
     }
 
     /**
 	 * Получение информaции об объекте хранилища:
      * Путь к запрашиваемому объекту, берется из URL запроса, следующего после параметра {ws}
 	 * 
 	 * @param request
      * @param ws Имя хранилища
      * 
 	 * @return Коллекция RestfulCollection<StoreItem> в формате JSON
      * @see CollectionOptions
      * @exception ProtocolException
 	 */
 	@RequestMapping(value = "/store/{ws}/**", method = RequestMethod.GET)
 	public @ResponseBody RestfulCollection<StoreItem> getStoreItems(
 			HttpServletRequest request,
             @PathVariable("ws") String workspace) 
 			throws InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
         String path = getPath(request);
         
         LOG.log(Level.FINE, "Retrieving {0}", path);
         CollectionOptions opts = WebUtil.getOptions(request);
         RestfulCollection<StoreItem> res = storeSvc.getItems(
                 WebUtil.USER_ME, workspace, path, opts, token)
                 .get();
         
 		return res;
 	}
 	
 	/**
 	 * Экспорт объекта хранилища:
      * Путь к экспортиромуему объекту, берется из URL запроса, следующего после параметра {ws}
 	 * 
 	 * @param request
      * @param action Параметер должен иметь значение 'exportfile'
      * @param ws Имя хранилища
      * 
 	 * @return Содержимое файла в виде потока байт 
 	 * @see CertificateController#getCertificateList
 	 * @throws ProtocolException
 	 */
 	@RequestMapping(value = "/store/{ws}/**", method = RequestMethod.GET, params="action=exportfile")
 	public void exportStoreItem(
 			HttpServletRequest request,
 			HttpServletResponse response,
             @PathVariable("ws") String workspace) 
 			throws InterruptedException, ExecutionException, IOException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
         String path = getPath(request);
         
         StoreItem item = storeSvc.getItems(
                 WebUtil.USER_ME, workspace, path, null, token)
                 .get()
                 .getEntry()
                 .get(0);
         
         String mimeType = (String) item.getProperties().get(StoreItem.PROP_MIME_TYPE);
         if (mimeType == null) mimeType = "application/octet-stream";
         response.addHeader("Content-type", mimeType);
 		response.addHeader("Content-Disposition", "attachement; filename=\"" + item.getName() + "\"");
         
         OutputStream os = response.getOutputStream();
         storeSvc.getItemData(WebUtil.USER_ME, workspace, path, os, token);
         os.close();
 	}
 
     private String getPath(HttpServletRequest request) {
  		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
         if (!path.isEmpty()) path = "/" + path;
         
         return path;
     }
     
     @PostConstruct
 	protected void postInit() throws ParserConfigurationException
 	{
 		Injector injector = (Injector)ctx.getAttribute(
 				GuiceServletContextListener.INJECTOR_ATTRIBUTE);
 		injector.injectMembers(this);
 	}
 	
 }
