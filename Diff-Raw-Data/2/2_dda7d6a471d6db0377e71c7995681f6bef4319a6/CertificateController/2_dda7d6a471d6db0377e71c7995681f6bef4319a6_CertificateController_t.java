 package com.digt.web;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.shindig.auth.AuthInfo;
 import org.apache.shindig.auth.SecurityToken;
 import org.apache.shindig.common.servlet.GuiceServletContextListener;
 import org.apache.shindig.protocol.ProtocolException;
 import org.apache.shindig.protocol.RestfulCollection;
 import org.apache.shindig.protocol.model.FilterOperation;
 import org.apache.shindig.social.opensocial.spi.CollectionOptions;
 import org.apache.shindig.social.opensocial.spi.GroupId;
 import org.apache.shindig.social.opensocial.spi.UserId;
 import org.bouncycastle.pkcs.PKCS10CertificationRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.digt.common.utils.ActivityGenerator;
 import com.digt.common.utils.LogUtils;
 import com.digt.jpa.AccessRightDb;
 import com.digt.jpa.CertPublishRequestDb;
 import com.digt.jpa.CertificateAclDb;
 import com.digt.jpa.CertificateRequestDb;
 import com.digt.jpa.spi.CertificateService;
 import com.digt.jpa.spi.CryptoService;
 import com.digt.model.CertificateContainer;
 import com.digt.model.CertificateRequest;
 import com.digt.model.CertificateTrustedList;
 import com.digt.model.PersonCertificate;
 import com.digt.model.PersonCertificate.Type;
 import com.digt.web.beans.CertificateBean;
 import com.digt.web.beans.json.Message;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.name.Named;
 
 @Controller
 public class CertificateController {
 	
 	@Inject
 	ActivityGenerator activGen;
 	@Autowired
 	private ServletContext ctx;
 	@Inject
 	private CertificateService certSvc;
 	@Inject
 	private CryptoService crypto;
 	@Inject(optional=true) 
 	@Named("com.digt.web.maxcertfilesize")
 	private static final int MAX_CERT_FILE_SIZE = 8192;
 	@Inject(optional=true) 
 	@Named("com.digt.web.maxctlfilesize")
 	private static final int MAX_CTL_FILE_SIZE = 65536;
 	
 	private static final Logger LOG = Logger.getLogger(
 			CertificateController.class.getName());
 	
 	/**
 	 * Экспорт сертификата в файл.
 	 * 
 	 * @param request
 	 * @param certId
 	 * @return Сертификат в формате PEM.
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 */
 	@RequestMapping(value = "/cert/get/{id}", method = RequestMethod.GET)
 	public ResponseEntity<String> getCertificate(HttpServletRequest request, 
 			@PathVariable("id") String certId) 
 			throws InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		GroupId groupId = WebUtil.getGroup(request);
 		
 		CollectionOptions options = WebUtil.getOptions(request);
 		options.setFilter(CertificateService.FILTERBY_OID);
 		options.setFilterValue(certId);
 		options.setFilterOperation(FilterOperation.equals);
 		
 		PersonCertificate cert = certSvc.getCertificates(ImmutableSet.of(userId), groupId, 
 				options, token).get().getEntry().get(0);
 		
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.add("Content-type", "text/plain; charset=UTF-8");
 		responseHeaders.add("Content-Disposition", "attachement; filename=\"cert.pem\"");
 		ResponseEntity<String> res = new ResponseEntity<String>(
 				"-----BEGIN CERTIFICATE-----\n" + 
 				Base64.encodeBase64String(cert.getCertificate()) + 
 				 "\n-----END CERTIFICATE-----", 
 				 responseHeaders, HttpStatus.OK);
 		
 		return res;
 	}
 
 	/**
 	 * Экспорт ключевого контейнера в файл.
 	 * 
 	 * @param request
 	 * @param contId
 	 * @return Ключевой контейнер в DER формате.
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 */
 	@RequestMapping(value = "/container/get/{id}", method = RequestMethod.GET)
 	public ResponseEntity<byte[]> getContainer(HttpServletRequest request, 
 			@PathVariable("id") String contId) 
 			throws InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 
 		CollectionOptions options = WebUtil.getOptions(request);
 		options.setFilter(CertificateService.FILTERBY_OID);
 		options.setFilterValue(contId);
 		options.setFilterOperation(FilterOperation.equals);
 		
 		CertificateContainer cont = certSvc.getContainers(ImmutableSet.of(userId),
 				options, token).get().getEntry().get(0);
 		
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.add("Content-type", "application/octet-stream");
 		responseHeaders.add("Content-Disposition", "attachement; filename=\"container.p12\"");
 		ResponseEntity<byte[]> res = new ResponseEntity<byte[]>(cont.getData(), responseHeaders, HttpStatus.OK);
 		
 		return res;
 	}
 	
 	/**
 	 * Получение списка сертификатов согласно заданным параметрам, содержащимся в request:
 	 * 		<ul>
 	 * 		<li><b>filterBy</b> критерий отбора</li>
 	 * 		<li><b>filterValue</b> значение критерия отбора</li>
 	 * 		<li><b>filterOp</b> тип операции сравнения (startsWith, contains, equals, )</li>
 	 * 		<li><b>sortBy</b> критерий сортировки</li>
 	 * 		<li><b>sortOrder</b> порядок сортировки (asc, desc)</li>
 	 * 		<li><b>startIndex</b> индекс первой возвращаемой записи</li>
 	 * 		<li><b>count</b> кол-во возвращаемых записей</li>
 	 * 		<li><b>groupId</b> критерий отбора по группе (@all, @self, @friends или id группы)</li>
 	 * 		</ul>
 	 * 
 	 * @param request
 	 * @return Коллекция сертификатов com.digt.model.PersonCertificate
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 * @see com.digt.model.PersonCertificate
 	 */
 	@RequestMapping(value = "/cert/get", method = RequestMethod.GET)
 	public @ResponseBody RestfulCollection<PersonCertificate> getCertificateList(
 			HttpServletRequest request) 
 			throws InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		CollectionOptions options = WebUtil.getOptions(request);
 		
 		return certSvc.getCertificates(ImmutableSet.of(userId), WebUtil.getGroup(request), 
 					options, token).get();
 	}
 
 	/**
 	 * Импорт сертификата из файла.
 	 * 
 	 * @param request
 	 * @param file
 	 * @param type - тип хранилища сертификата
 	 * @param containerId - привязка к существующему ключевому контейнеру
 	 * @return статус операции Message
 	 * @see com.digt.web.beans.json.Message
 	 * @see com.digt.model.PersonCertificate.Type
 	 */
 	@RequestMapping(value = "/cert/import", method = RequestMethod.POST)
 	public ResponseEntity<String> importCertificate(
 			HttpServletRequest request, 
 			@RequestParam("file") MultipartFile file,
 			@RequestParam("type") Type type,
 			@RequestParam("key") Long containerId)
 	{
 		Message ret = null;
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		try
 		{
 			if (file.getSize() > 0 && file.getSize() <= MAX_CERT_FILE_SIZE)
 			{
 					Long certId = certSvc.addCertificate(userId, file.getBytes(), file.getName(), type, token).get();
 					if (containerId != null && containerId > 0)
 					{
 						certSvc.bindContainer(userId, certId, containerId, token);
 					}
 					activGen.generateActivity(request, "Загружен сертификат", 
 							"Пользователь загрузил сертификат");
 					ret = new Message(true, "Выполнено.");
 			} else {
 				ret = new Message(false, "Некорректный размер файла. Файл больше " + MAX_CERT_FILE_SIZE + " байт.");
 			}
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			ret = new Message(false, "Ошибка импорта сертификата.");
 		}
 		
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.add("Content-type", "text/html; charset=UTF-8");
 		//responseHeaders.setContentType(MediaType.TEXT_HTML);
 		ResponseEntity<String> res = new ResponseEntity<String>(ret.toString(), responseHeaders, HttpStatus.OK);
 		
 		return res;
 	}
 	
 	@RequestMapping(value = "/cert/publish", method = RequestMethod.POST)
 	public @ResponseBody Message publishCertificate(HttpServletRequest request, 
 			@RequestParam("certId") Long certId)
 	{
 		Message ret = null;
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		try {
 			PersonCertificate cert = certSvc.getCertificates(userId, 
 					ImmutableSet.of(certId), token).get().getEntry().get(0);
 			String[] fields = cert.getDn().split(",");
 			String emailAddress = null;
 			for (String field: fields)
 			{
 				if (field.startsWith("EMAIL=") || field.startsWith("E=") || field.startsWith("EMAILADDRESS="))
 				{
 					emailAddress = field.split("=")[1];
 					break;
 				}
 			}
 			if (emailAddress != null)
 			{
 				certSvc.publishCertificate(cert, emailAddress);
 				ret = new Message(true, "Выполнено. Запрос на подтверждение операции отправлен на " + emailAddress);
 			} else {
 				ret = new Message(false, "Сертификат не содержит корректного e-mail адреса.");
 			}
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			ret = new Message(false, "Ошибка публикации сертификата.");
 		}
 		
 		return ret;
 	}
 
 	@RequestMapping(value = "/cert/publish/confirm", method = RequestMethod.GET)
 	public String publishConfirm(HttpServletRequest request,
 			Model model,
 			@RequestParam("id") String secret)
 	{
 		CertPublishRequestDb req = null;
 		try {
 			req = (CertPublishRequestDb) certSvc.getCertPublishRequest(secret).get();
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 		}
 		
 		if (req != null)
 		{
 			if (req.getCertificate() != null)
 			{
 				CertificateAclDb acl = new CertificateAclDb();
 				acl.setType(AccessRightDb.Type.ALL);
 				acl.setCertificate(req.getCertificate());
 				certSvc.store(acl);
 				model.addAttribute("message", new FlashMap.Message(FlashMap.MessageType.success, "Публикация сертификата подтверждена"));
 			} else {
 				model.addAttribute("message", new FlashMap.Message(FlashMap.MessageType.success, "Cертификат не найден"));
 			}
 			certSvc.remove(req);
 		} else {
 			model.addAttribute("message", new FlashMap.Message(FlashMap.MessageType.error, "Запрос на подтверждение не найден"));
 		}
 		return "publish_confirm";
 	}
 
 	/**
 	 * Импорт ключевого контейнера.
 	 * 
 	 * @param request
 	 * @param file
 	 * @param pin Пин-код для зашифрованного ключевого контейнера, передается в случае 
 	 * 			  необходимости импорта сертификата из ключевого контейнера
 	 * @return статус операции Message
 	 * @see com.digt.web.beans.json.Message
 	 */
 	@RequestMapping(value = "/container/import", method = RequestMethod.POST)
 	public ResponseEntity<String> importContainer(HttpServletRequest request, 
 			@RequestParam("file") MultipartFile file,
 			@RequestParam("pin") String pin)
 	{
 		Message ret = null;
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		
 		try
 		{
 			if (file.getSize() > 0 && file.getSize() <= MAX_CERT_FILE_SIZE)
 			{
 					certSvc.addContainer(userId, file.getBytes(), pin, token).get();
 					activGen.generateActivity(request, "Загружен ключевой контейнер", 
 							"Пользователь загрузил ключевой контейнер");
 					
 					ret = new Message(true, "Выполнено.");
 			} else {
 				ret = new Message(false, "Некорректный размер файла. Файл больше " + MAX_CERT_FILE_SIZE + "байт.");
 			}
 		} catch (Exception e) {
 			ret = new Message(false, "Ошибка импорта файла контейнера.");
 		}
 		
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.add("Content-type", "text/html; charset=UTF-8");
 		ResponseEntity<String> res = new ResponseEntity<String>(ret.toString(), responseHeaders, HttpStatus.OK);
 		
 		return res;
 	}
 
 	@RequestMapping(value = "/cert/generate", method = RequestMethod.POST)
 	public @ResponseBody Message generateCertificate(
 			HttpServletRequest request,
 			@Valid CertificateBean form,
 			BindingResult result) throws IOException
 	{
 		Message ret = null;
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		try
 		{
 			if (!result.hasErrors())
 			{
 				crypto.generateCertificate(userId, form.getPin(), form.getDN(), token).get();
 				String title = "Выдан сертификат";
 				activGen.generateActivity(request, title, 
 						title + " "  + form.getDN());
 				ret = new Message(true, "Выполнено.");
 			} else {
 				ret = new Message(false, "Поле " + result.getFieldError().getField() 
 						+ " содержит некорректные данные " + result.getFieldError().getDefaultMessage());
 			}
 		} catch (Exception e) {
 			ret = new Message(false, "Ошибка создания сертификата.");
 		}
 		
 		return ret;
 	}
 
 	@RequestMapping(value = "/cert/delete", method = RequestMethod.POST)
 	public @ResponseBody Message deleteCertificate(HttpServletRequest request, @RequestParam("id") String[] certId) 
 			throws ProtocolException, InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		
 		certSvc.deleteCertificate(userId, Arrays.asList(certId), token);
 		String title = "Удалён сертификат";
 		activGen.generateActivity(request, title, title);
 		
 		return new Message(true, "Deleted successfully");
 	}
 	
 	/**
 	 * Получение списка ключевых контейнеров, согласно заданным параметрам, содержащимся в request.
 	 * 
 	 * @param request
 	 * @return Колекция CertificateContainer
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 * @see com.digt.model.CertificateContainer
 	 */
 	@RequestMapping(value = "/container/get", method = RequestMethod.GET)
 	public @ResponseBody RestfulCollection<CertificateContainer> getContainerList(HttpServletRequest request) 
 			throws InterruptedException, ExecutionException 
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		CollectionOptions options = WebUtil.getOptions(request);
 		return certSvc.getContainers(ImmutableSet.of(userId), options, token).get();
 	}
 	
 	@RequestMapping(value = "/container/delete", method = RequestMethod.POST)
 	public @ResponseBody Message deleteContainer(HttpServletRequest request, @RequestParam("id") String[] containerId) 
 			throws ProtocolException, InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		certSvc.deleteContainer(userId, Arrays.asList(containerId), token);
 		String title = "Удалён ключевой контейнер";
 		activGen.generateActivity(request, title, title);
 
 		return new Message(true, "Удалено.");
 	}
 	
 	/**
 	 * Импорт файла запроса на сертификат
 	 * 
 	 * @param request
 	 * @param file
 	 * @return статус операции Message
 	 * @see com.digt.web.beans.json.Message
 	 */
 	@RequestMapping(value = "/certreq/import", method = RequestMethod.POST)
 	public ResponseEntity<String> importCertRequest(
 			HttpServletRequest request, 
 			@RequestParam("file") MultipartFile file) {
 		Message ret = null;
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		try
 		{
 			if (file.getSize() > 0 && file.getSize() <= MAX_CERT_FILE_SIZE)
 			{
 				PKCS10CertificationRequest req = new PKCS10CertificationRequest(file.getBytes());
 				CertificateRequestDb reqDb = new CertificateRequestDb();
 				reqDb.setDn(req.getSubject().toString());
 				reqDb.setData(req.getEncoded());
 				certSvc.addCertRequest(userId, reqDb, token).get();
 				activGen.generateActivity(request, "Загружен запрос на выпуск сертификата", 
 						"Пользователь загрузил файл запроса на выпуск сертификата");
 					
 				ret = new Message(true, "Выполнено.");
 			} else {
 				ret = new Message(false, "Некорректный размер файла: " + file.getSize());
 			}
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			ret = new Message(false, "Ошибка импорта сертификата.");
 		}
 		
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.add("Content-type", "text/html; charset=UTF-8");
 		ResponseEntity<String> res = new ResponseEntity<String>(ret.toString(), responseHeaders, HttpStatus.OK);
 		
 		return res;
 	}
 			
 
 	/**
 	 * Экспорт запроса на сертификат в файл.
 	 * 
 	 * @param request
 	 * @param requestId
 	 * @return Файл в формате PEM
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 */
 	@RequestMapping(value = "/certreq/get/{id}", method = RequestMethod.GET)
 	public ResponseEntity<String> getCertRequest(HttpServletRequest request,
 			@PathVariable("id") String requestId) throws InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 
 		CollectionOptions options = WebUtil.getOptions(request);
 		options.setFilter(CertificateService.FILTERBY_OID);
 		options.setFilterValue(requestId);
 		options.setFilterOperation(FilterOperation.equals);
 
 		CertificateRequest req = certSvc.getCertRequests(ImmutableSet.of(userId), options, token).get().getEntry().get(0);
 		
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.add("Content-type", "text/plain; charset=UTF-8");
 		responseHeaders.add("Content-Disposition", "attachement; filename=\"certreq.csr\"");
 		ResponseEntity<String> res = new ResponseEntity<String>(
 				"-----BEGIN CERTIFICATE REQUEST-----\n" + 
 				Base64.encodeBase64String(req.getData()) + 
 				 "\n-----END CERTIFICATE REQUEST-----", 
 				 responseHeaders, HttpStatus.OK);
 		return res;
 	}
 
 	/**
 	 * Получение списка запросов на выпуск сертификата, согласно заданным параметрам, содержащимся в request.
 	 *
 	 * @param request
 	 * @return Коллекция CertificateRequest
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 * @see com.digt.model.CertificateRequest
 	 */
 	@RequestMapping(value = "/certreq/get", method = RequestMethod.GET)
 	public @ResponseBody RestfulCollection<CertificateRequest> getCertRequestList(HttpServletRequest request) 
 			throws InterruptedException, ExecutionException 
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		CollectionOptions options = WebUtil.getOptions(request);
 		return certSvc.getCertRequests(ImmutableSet.of(userId), options, token).get();
 	}
 	
 	@RequestMapping(value = "/certreq/delete", method = RequestMethod.POST)
 	public @ResponseBody Message deleteCertRequest(HttpServletRequest request, @RequestParam("id") String[] certReqId) 
 			throws ProtocolException, InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		certSvc.deleteCertRequest(userId, Arrays.asList(certReqId), token);
 		String title = "Удалён запрос на выпуск сертификата";
 		activGen.generateActivity(request, title, title);
 
 		return new Message(true, "Удалено.");
 	}
 	
 	/**
 	 * Экспорт списка доверенных сертификатов в файл.
 	 * @param request
 	 * @param ctlId
 	 * @return Файл в формате DER
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 */
 	@RequestMapping(value = "/ctl/get/{id}", method = RequestMethod.GET)
 	public ResponseEntity<byte[]> getCtl(HttpServletRequest request, @PathVariable("id") Long ctlId) 
 			throws InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		GroupId groupId = WebUtil.getGroup(request);
 
 		CollectionOptions options = WebUtil.getOptions(request);
 		options.setFilter(CertificateService.FILTERBY_OID);
 		options.setFilterOperation(FilterOperation.equals);
 		options.setFilterValue(ctlId.toString());
 		
 		CertificateTrustedList ctl = certSvc.getCertificateTrustedList(ImmutableSet.of(userId), groupId,
 				options, token).get().getEntry().get(0);
 		
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.add("Content-type", "application/octet-stream");
 		responseHeaders.add("Content-Disposition", "attachement; filename=\"certlist.stl\"");
 		ResponseEntity<byte[]> res = new ResponseEntity<byte[]>(ctl.getData(), responseHeaders, HttpStatus.OK);
 		
 		return res;
 	}
 
 	/**
 	 * Получение списка списков доверенных сертификатов, согласно заданным параметрам, содержащимся в request.
 	 *
 	 * @param request
 	 * @return Коллекция CertificateTrustedList
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 * @see com.digt.model.CertificateTrustedList
 	 */
 	@RequestMapping(value = "/ctl/get", method = RequestMethod.GET)
 	public @ResponseBody RestfulCollection<CertificateTrustedList> getCtlList(
 			HttpServletRequest request) 
 			throws InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		CollectionOptions options = WebUtil.getOptions(request);
 		return certSvc.getCertificateTrustedList(ImmutableSet.of(userId), WebUtil.getGroup(request), 
 					options, token).get();
 	}
 	
 	@RequestMapping(value = "/ctl/delete", method = RequestMethod.POST)
 	public @ResponseBody Message deleteCtl(HttpServletRequest request, @RequestParam("id") String[] ctlId) 
 			throws ProtocolException, InterruptedException, ExecutionException
 	{
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		certSvc.deleteCertificateTrustedList(userId, Arrays.asList(ctlId), token);
 		String title = "Удалён список доверенных сертификатов";
 		activGen.generateActivity(request, title, title);
 
 		return new Message(true, "Удалено.");
 	}
 
 	/**
 	 * Импорт файла списка доверенных сертификатов.
 	 * 
 	 * @param request
 	 * @param file
 	 * @return статус операции Message
 	 * @see com.digt.web.beans.json.Message
 	 */
 	@RequestMapping(value = "/ctl/import", method = RequestMethod.POST)
 	public ResponseEntity<String> importCtl(HttpServletRequest request, 
 			@RequestParam("file") MultipartFile file)
 	{
 		Message ret = null;
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = new UserId(UserId.Type.me, null);
 		
 		try
 		{
 			if (file.getSize() > 0 && file.getSize() <= MAX_CTL_FILE_SIZE)
 			{
 				certSvc.addCertificateTrustedList(userId, file.getBytes(), token).get();
 				activGen.generateActivity(request, "Загружен список доверенных сертификатов", 
 						"Пользователь загрузил список доверенных сертификатов");
 					
 					ret = new Message(true, "Выполнено.");
 			} else {
 				ret = new Message(false, "Некорректный размер файла. Файл больше " + MAX_CTL_FILE_SIZE + "байт.");
 			}
 		} catch (Exception e) {
 			ret = new Message(false, "Ошибка импорта файла списка доверенных сертификатов.");
 		}
 		
 		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-type", "text/html");
 		ResponseEntity<String> res = new ResponseEntity<String>(ret.toString(), responseHeaders, HttpStatus.OK);
 		
 		return res;
 	}
 
 	
 
 	@PostConstruct
 	protected void postInit()
 	{
 		Injector injector = (Injector)ctx.getAttribute(
 				GuiceServletContextListener.INJECTOR_ATTRIBUTE);
 		injector.injectMembers(this);
 	}
 
 }
