 package controllers;
 
 import static play.data.Form.form;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import models.GroupUserMap;
 import models.MonthlyPayment;
 import models.Payment;
 import models.PaymentArtifact;
 import models.PaymentType;
 import models.User;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.Logger.ALogger;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.html.payments.createForm;
 import views.html.payments.createForm2;
 import views.html.payments.createMonthlyForm;
 import views.html.payments.editForm;
 import views.html.payments.list;
 
 /**
  * Manage a database of payments
  */
 @Security.Authenticated(Secured.class)
 public class Application extends Controller {
 
 	static ALogger logger = play.Logger.of(Application.class);
 
 	/**
 	 * This result directly redirect to application home.
 	 */
 	public static Result GO_HOME = redirect(routes.Application.list(0,
 			"paid_date", "desc", ""));
 
 	/**
 	 * Handle default path requests, redirect to payments list
 	 */
 	public static Result index() {
 		return redirect(routes.Application.listByLoginUserId(0,
 				"paid_date", "desc"));
 	}
 
 	/**
 	 * Display the paginated list of payments.
 	 * 
 	 * @param page
 	 *            Current page number (starts from 0)
 	 * @param sortBy
 	 *            Column to be sorted
 	 * @param order
 	 *            Sort order (either asc or desc)
 	 * @param filter
 	 *            Filter applied on payment names
 	 */
 	public static Result list(int page, String sortBy, String order,
 			String filter) {
 		return ok(list.render(Payment.page(page, 10, sortBy, order, filter),
 				sortBy, order, filter, "payments", null, null));
 	}
 
 	public static Result listByUserId(int page, String sortBy, String order,
 			Long userId) {
 		return ok(list.render(Payment.pageByUserId(page, 10, sortBy, order, userId),
 				sortBy, order, "", "user_payments", null, null));
 	}
 
 	public static Result listByLoginUserId(int page, String sortBy, String order) {
 		return ok(list.render(Payment.pageByUserId(page, 10, sortBy, order, new Long(session().get("userId"))),
 				sortBy, order, "", "user_payments", null, null));
 	}
 
 	public static Result apiList() {
 		List<Payment> payments = Payment.find.findList();
 
     	for(Payment p : payments){
     		p.payee.credential = null;
     		p.payer.credential = null;
     	}
     	
 		return ok(Json.toJson(payments));
 	}
 	
 
 	public static Result apiPaymentArtifacts(Long paymentId) {
 
 		List<PaymentArtifact> artifacts = PaymentArtifact.findByPaymentId(paymentId);
 		
 		List<PaymentArtifact> modifiedArtifacts = new ArrayList<PaymentArtifact>();
 		
 		for(PaymentArtifact artifact : artifacts){
 			artifact.data = null;
 			artifact.payment = null;
 			
 			modifiedArtifacts.add(artifact);
 		}
 		
 		return ok(Json.toJson(modifiedArtifacts));
 	}
 	
 	
 	public static Result apiDeletePaymentArtifact(Long id) throws Exception {
 
 		PaymentArtifact artifact = PaymentArtifact.find.byId(id);
 		Long paymentId = artifact.payment.id;
 		artifact.delete();
 
 		return ok(Json.toJson(paymentId));
 	}
 	
 	public static Result apiGet(Long id) {
 
 		return ok(Json.toJson(Payment.find.byId(id)));
 	}
 	
 	@BodyParser.Of(play.mvc.BodyParser.Json.class)
 	public static Result apiCreatePayment() {
 
 		JsonNode json = request().body().asJson();
 		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
 		Payment payment = null;
 		try {
 
 			payment = new Payment();
			payment.amount = new BigDecimal(json.findPath("amount").getTextValue());
 			payment.endPeriod = df.parse(json.findPath("endPeriod").getTextValue());
 			payment.startPeriod = df.parse(json.findPath("startPeriod").getTextValue());
 			payment.paidDate = df.parse(json.findPath("paidDate").getTextValue());
 			payment.payeeAccountNumber = json.findPath("payeeAccountNumber").getTextValue();
 			payment.paymentType = PaymentType.find.byId(json.findPath("paymentType").findPath("id").getLongValue());
 			payment.reference = json.findPath("reference").getTextValue();
 			payment.remarks = json.findPath("remarks").getTextValue();
 			payment.payer = User.findById(json.findPath("payer").findPath("id").getLongValue());
 			payment.payee = User.findById(json.findPath("payee").findPath("id").getLongValue());
 			payment.save();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		
 		ObjectNode result = Json.newObject();
 		result.put("status", "OK");
 		result.put("message", "Payment id " + payment.id + " has been created.");
 		return ok(result);
 	}
 	
 	@BodyParser.Of(play.mvc.BodyParser.Json.class)
 	public static Result apiUpdatePayment(Long id) {
 
 		JsonNode json = request().body().asJson();
 		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
 		Payment payment = null;
 		try {
 
 			payment = Payment.find.byId(id);
 			payment.amount = new BigDecimal(json.findPath("amount").asDouble());
 			payment.endPeriod = df.parse(json.findPath("endPeriod").getTextValue());
 			payment.startPeriod = df.parse(json.findPath("startPeriod").getTextValue());
 			payment.paidDate = df.parse(json.findPath("paidDate").getTextValue());
 			payment.payeeAccountNumber = json.findPath("payeeAccountNumber").getTextValue();
 			payment.paymentType = PaymentType.find.byId(json.findPath("paymentType").findPath("id").getLongValue());
 			payment.reference = json.findPath("reference").getTextValue();
 			payment.remarks = json.findPath("remarks").getTextValue();
 			payment.payer = User.findById(json.findPath("payer").findPath("id").getLongValue());
 			payment.payee = User.findById(json.findPath("payee").findPath("id").getLongValue());
 			payment.update();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		
 		ObjectNode result = Json.newObject();
 		result.put("status", "OK");
 		result.put("message", "Payment id " + payment.id + " has been updated.");
 		return ok(result);
 	}
 	
 	@BodyParser.Of(play.mvc.BodyParser.Json.class)
 	public static Result apiCreateMonthlyPayment() {
 
 		JsonNode json = request().body().asJson();
 		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
 		Payment payment = null;
 		
 
 
 		logger.debug(json.toString());
 		
 		JsonNode months = json.findPath("months");
 		
 		for(JsonNode month : months){
 			
 			if(month.findPath("selected").asBoolean()){
 
 				try {
 					
 					int year = json.findPath("year").getIntValue();
 					int mth =  month.findPath("id").asInt();
 					
 					Calendar cal = Calendar.getInstance();
 					cal.set(year, mth-1, 1);
 					Date startDate = cal.getTime();
 					cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
 					Date endDate = cal.getTime();
 					
 					Double amount = json.findPath("amount").asDouble();
 					
 					payment = new Payment();
 					payment.amount = new BigDecimal(amount);
 					payment.endPeriod = endDate;
 					payment.startPeriod = startDate;
 					payment.paidDate = df.parse(json.findPath("paidDate").getTextValue());
 					payment.payeeAccountNumber = json.findPath("payeeAccountNumber").getTextValue();
 					payment.paymentType = PaymentType.find.byId(json.findPath("paymentType").findPath("id").getLongValue());
 					payment.reference = json.findPath("reference").getTextValue();
 					payment.remarks = json.findPath("remarks").getTextValue();
 					payment.payer = User.findById(json.findPath("payer").findPath("id").getLongValue());
 					payment.payee = User.findById(json.findPath("payee").findPath("id").getLongValue());
 					payment.save();
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 				
 			}
 		}
 		
 		
 		
 		ObjectNode result = Json.newObject();
 		result.put("status", "OK");
 		result.put("message", "Payment(s) has been created.");
 		return ok(result);
 	}
 
 	/**
 	 * Display the 'edit form' of a existing Payment.
 	 * 
 	 * @param id
 	 *            Id of the payment to edit
 	 */
 	public static Result edit(Long id) {
 
 		logger.debug("hello");
 		Form<Payment> paymentForm = form(Payment.class).fill(
 				Payment.find.byId(id));
 
 		return ok(editForm.render(id, paymentForm, getPaymentArtifacts(id)));
 	}
 
 	private static List<PaymentArtifact> getPaymentArtifacts(Long paymentId) {
 
 		return PaymentArtifact.findByPaymentId(paymentId);
 	}
 
 	/**
 	 * Handle the 'edit form' submission
 	 * 
 	 * @param id
 	 *            Id of the payment to edit
 	 */
 	public static Result update(Long id) {
 		Form<Payment> paymentForm = form(Payment.class).bindFromRequest();
 		if (paymentForm.hasErrors()) {
 			return badRequest(editForm.render(id, paymentForm,
 					getPaymentArtifacts(id)));
 		}
 		paymentForm.get().update(id);
 		flash("success", "Payment " + paymentForm.get().name
 				+ " has been updated");
 		return GO_HOME;
 	}
 
 	/**
 	 * Display the 'new payment form'.
 	 */
 	public static Result create() {
 
 		Payment payment = new Payment();
 
 		Form<Payment> paymentForm = form(Payment.class).fill(payment);
 		return ok(createForm.render(paymentForm));
 	}
 
 	/**
 	 * Display the 'new payment form'.
 	 */
 	public static Result create2() {
 
 		return ok(createForm2.render());
 	}
 
 	/**
 	 * Display the 'new monthly payment form'.
 	 */
 	public static Result createMonthly2() {
 
 		return create2();
 	}
 
 	/**
 	 * Display the 'new payment form'.
 	 */
 	public static Result createMonthly() {
 
 		MonthlyPayment payment = new MonthlyPayment();
 
 		Form<MonthlyPayment> paymentForm = form(MonthlyPayment.class).fill(payment);
 		return ok(createMonthlyForm.render(paymentForm));
 	}
 
 	/**
 	 * Handle the 'new payment form' submission
 	 */
 	public static Result save() {
 		Form<Payment> paymentForm = form(Payment.class).bindFromRequest();
 		if (paymentForm.hasErrors()) {
 			return badRequest(createForm.render(paymentForm));
 		}
 		paymentForm.get().save();
 		flash("success", "Payment " + paymentForm.get().name
 				+ " has been created");
 		return GO_HOME;
 	}
 	
 
 
 	/**
 	 * Handle the 'new payment form' submission
 	 */
 	public static Result saveMonthly() {
 		
 		logger.debug("saveMonthly");
 		
 		Form<MonthlyPayment> paymentForm = form(MonthlyPayment.class).bindFromRequest();
 		if (paymentForm.hasErrors()) {
 			logger.debug("error");
 			return badRequest(createMonthlyForm.render(paymentForm));
 		}
 		
 		
 		MonthlyPayment monthly = paymentForm.get();
 
 		
 		logger.debug(monthly.year.toString());
 		logger.debug(monthly.months.toString());
 		logger.debug(monthly.references.toString());
 		
 		int index=0;
 		Payment payment = null;
 		for(Integer month: monthly.months){
 			
 			if(month != null){
 				Calendar cal = Calendar.getInstance();
 				cal.set(monthly.year, month-1, 1);
 				Date startDate = cal.getTime();
 				logger.debug(String.valueOf(cal.getActualMaximum(Calendar.DATE)));
 				cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
 				Date endDate = cal.getTime();
 				
 				logger.debug(startDate.toString());
 				logger.debug(endDate.toString());
 
 				payment = new Payment();
 				payment.startPeriod = startDate;
 				payment.endPeriod = endDate;
 				payment.amount = monthly.amount;
 				payment.name = monthly.name;
 				payment.paidDate =  monthly.paidDate;
 				payment.payee =  monthly.payee;
 				payment.payeeAccountNumber =  monthly.payeeAccountNumber;
 				payment.payer =  monthly.payer;
 				payment.paymentType =  monthly.paymentType;
 				payment.reference =  monthly.references.get(index);
 				payment.remarks =  monthly.remarks;
 				payment.save();
 				
 			}
 			index++;
 		}
 		
 		flash("success", "Monthly Payments " + paymentForm.get().name
 				+ " has been created");
 		return GO_HOME;
 	}
 
 	/**
 	 * Handle payment deletion
 	 */
 	public static Result delete(Long id) {
 		Payment.find.ref(id).delete();
 		flash("success", "Payment has been deleted");
 		return GO_HOME;
 	}
 
 	public static Result uploadFile(Long paymentId) throws Exception {
 		logger.debug("in upload()");
 		MultipartFormData body = request().body().asMultipartFormData();
 		FilePart filePart = body.getFile("picture");
 		if (filePart != null) {
 			String fileName = filePart.getFilename();
 			String contentType = filePart.getContentType();
 			File file = filePart.getFile();
 
 			Payment payment = Payment.find.byId(paymentId);
 
 			PaymentArtifact artifact = new PaymentArtifact();
 			artifact.name = fileName;
 			artifact.type = contentType;
 			FileInputStream fis = new FileInputStream(file);
 			artifact.data = IOUtils.toByteArray(fis);
 			artifact.payment = payment;
 			artifact.save();
 
 			logger.debug(artifact.id.toString());
 			logger.debug(fileName);
 			return edit(paymentId);
 		} else {
 			flash("error", "Missing file");
 			return edit(paymentId);
 		}
 	}
 	
 	public static Result apiUploadFile(Long paymentId) throws Exception {
 		
 		logger.debug("in apiUploadFile("+paymentId+")");
 		
 		logger.debug(request().body().toString());
 		
 
 		MultipartFormData body = request().body().asMultipartFormData();
 //		FilePart filePart = body.getFile("picture");
 		
 		List<FilePart> files = body.getFiles();
 		
 		for(FilePart filePart : files){
 
 			
 			if (filePart != null) {
 				String fileName = filePart.getFilename();
 				String contentType = filePart.getContentType();
 				File file = filePart.getFile();
 
 				Payment payment = Payment.find.byId(paymentId);
 
 				PaymentArtifact artifact = new PaymentArtifact();
 				artifact.name = fileName;
 				artifact.type = contentType;
 				FileInputStream fis = new FileInputStream(file);
 				artifact.data = IOUtils.toByteArray(fis);
 				artifact.payment = payment;
 				artifact.save();
 
 				logger.debug(artifact.id.toString());
 				logger.debug(fileName);
 				return ok("File uploaded");
 			} else {
 				return ok("Missing file");
 			}
 			
 		}
 
 		return ok("Missing file");
 //		File file = request().body().asRaw().asFile();
 //		  return ok("File uploaded");
 	  
 	}
 
 	public static Result getPaymentArtifact(Long id) throws Exception {
 
 		PaymentArtifact artifact = PaymentArtifact.find.byId(id);
 		
 		logger.debug("download");
 		logger.debug(java.net.URLEncoder.encode(artifact.name, "UTF-8"));
 		
 		File file = new java.io.File("/tmp/"+ java.net.URLEncoder.encode(artifact.id + " " + artifact.name, "UTF-8"));
 		FileUtils.writeByteArrayToFile(file, artifact.data);
 
 		return ok(file);
 	}
 	
 	public static Result deletePaymentArtifact(Long id) throws Exception {
 
 		PaymentArtifact artifact = PaymentArtifact.find.byId(id);
 		Long paymentId = artifact.payment.id;
 		artifact.delete();
 		
 		return edit(paymentId);
 	}
 	
 
 	
 	public static Result getPayments(Long groupId, Long memberId, int page, String sortBy, String order){
 		
 		Long userId = 0L;
 		
 		GroupUserMap gum = GroupUserMap.findByGroupIdAndUserId(groupId, memberId);
 		
 		if(gum != null){
 			userId = gum.user.id;
 		}
 		
 		return ok(list.render(Payment.pageByUserId(page, 10, sortBy, order, userId),
 				sortBy, order, "", "group_user_payments", groupId, memberId));
 	}
 
 }
