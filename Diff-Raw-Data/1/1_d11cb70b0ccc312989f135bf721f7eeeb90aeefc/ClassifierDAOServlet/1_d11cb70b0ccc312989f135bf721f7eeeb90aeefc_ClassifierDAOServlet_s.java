 package dao;
 
 import helper.Deserializer;
 import helper.MapUtils;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.javaml.classification.AbstractClassifier;
 import net.sf.javaml.core.Instance;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.datasource.SingleConnectionDataSource;
 
 import classifier.Oracle;
 import classifier.SimpleOracle;
 import classifierImplementation.WeightedKNearestNeighbors;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.samwong.hk.roomservice.api.commons.dataFormat.AuthenticationDetails;
 import com.samwong.hk.roomservice.api.commons.dataFormat.Report;
 import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
 import com.samwong.hk.roomservice.api.commons.dataFormat.ResponseWithReports;
 import com.samwong.hk.roomservice.api.commons.dataFormat.RoomStatistic;
 import com.samwong.hk.roomservice.api.commons.dataFormat.TrainingData;
 import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
 import com.samwong.hk.roomservice.api.commons.helper.InstanceFriendlyGson;
 import com.samwong.hk.roomservice.api.commons.parameterEnums.Classifier;
 import com.samwong.hk.roomservice.api.commons.parameterEnums.Operation;
 import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
 import com.samwong.hk.roomservice.api.commons.parameterEnums.ReturnCode;
 
 /**
  * Servlet implementation class ClassifierDAOServlet
  */
 
 /**
  * @author wongsam
  * 
  */
 
 @WebServlet("/api")
 public class ClassifierDAOServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger log = Logger.getLogger(ClassifierDAOServlet.class);
 	private ClassifierDAO classifierDAO;
 	private Oracle oracle;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public ClassifierDAOServlet() {
 		super();
 		classifierDAO = new SQLiteBackedClassifierDAO();
 		String dbUrl = "jdbc:sqlite:" + System.getProperty("catalina.base")
 				+ "/test.sqlite";
 		log.info(dbUrl);
 		try {
 			classifierDAO.setDataSource(new SingleConnectionDataSource(
 					org.sqlite.JDBC.createConnection(dbUrl, new Properties()),
 					true));
 			oracle = new SimpleOracle(classifierDAO);
 		} catch (SQLException e) {
 			log.fatal("Failed to create connection to " + dbUrl, e);
 		}
 
 		// Setup all classifiers
 		Map<Classifier, AbstractClassifier> classifiers = new HashMap<Classifier, AbstractClassifier>();
 		classifiers.put(Classifier.WKNN, new WeightedKNearestNeighbors(5));
 	}
 
 	@Override
 	protected void doDelete(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		Map<String, String[]> parameters = request.getParameterMap();
 		log.info("Received DELETE request with params:" + MapUtils.printParameterMap(parameters));
 		String operation = Deserializer.getSingleParameter(parameters,
 				ParameterKey.OPERATION);
 		PrintWriter out = response.getWriter();
 		try {
 			/* Return if there is no DELETE parameter */
 			if (!operation.equals(Operation.DELETE.toString())) {
 				log.warn("Erroneous DELETE request: " + parameters);
 				out.print(returnJson(
 						ReturnCode.NO_RESPONSE,
 						"You didn't explicitly say you want to perform a DELETE. To be on the safe side, this request has been ignored."));
 				return;
 			}
 			try {
 				WifiInformation wifiInformation = Deserializer
 						.getDejsonifiedWifiInformation(parameters);
 				Instance instance = Deserializer.wifiInformationToInstance(
 						wifiInformation, classifierDAO);
 				instance.setClassValue(parameters.get(ParameterKey.ROOM
 						.toString())[0]);
 				AuthenticationDetails auenticationDetails = Deserializer
 						.getAuenticationDetails(parameters);
 				classifierDAO.deleteClassification(instance,
 						auenticationDetails);
 				out.print(returnJson(ReturnCode.OK, "DELETE has been completed"));
 			} catch (Exception e) {
 				log.error("Failed to perform DELETE with param map: "
 						+ parameters, e);
 				out.print(returnJson(ReturnCode.UNRECOVERABLE_EXCEPTION,
 						"Something went wrong while performing DELETE. " + e));
 			}
 		} finally {
 			out.close();
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		Map<String, String[]> parameters = request.getParameterMap();
 		log.info("Received GET request with params:" + MapUtils.printParameterMap(parameters));
 
 		PrintWriter out = response.getWriter();
 		try {
 			String operation = Deserializer.getSingleParameter(parameters,
 					ParameterKey.OPERATION);
 			AuthenticationDetails auenticationDetails = Deserializer
 					.getAuenticationDetails(parameters);
 			log.info(auenticationDetails);
 
 			if (operation.equals(Operation.GET_LIST_OF_ROOMS.toString())) {
 				List<String> roomList = classifierDAO
 						.getRoomList(auenticationDetails);
 				out.print(new Gson().toJson(roomList));
 				return;
 			}
 		} catch (Exception e) {
 			Response errorResponse = new Response().withReturnCode(
 					ReturnCode.ILLEGAL_ARGUMENT).withExplanation(e.toString());
 			out.print(new Gson().toJson(errorResponse));
 			return;
 		} finally {
 			out.close();
 		}
 
 	}
 
 	/* Shoved upload batch training from PUT to POST because there is a limit on url length.
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		Map<String, String[]> parameters = request.getParameterMap();
 		log.info("Received POST request with params:" + MapUtils.printParameterMap(parameters));
 
 		PrintWriter out = response.getWriter();
 		try {
 			String operation = Deserializer.getSingleParameter(parameters,
 					ParameterKey.OPERATION);
 			AuthenticationDetails authenticationDetails = Deserializer
 					.getAuenticationDetails(parameters);
 			log.info(authenticationDetails);
 
 			if (operation.equals(Operation.CLASSIFY.toString())) {
 				ResponseWithReports responseWithReports = new ResponseWithReports();
 
 				WifiInformation observation = Deserializer
 						.getDejsonifiedWifiInformation(parameters);
 				log.info(observation);
 
 				Instance instance = Deserializer.wifiInformationToInstance(
 						observation, classifierDAO);
 				log.info(instance);
 
 				Map<String, String> specialRequests = Deserializer
 						.getSpecialRequestsForClassifier(parameters);
 				log.info(specialRequests);
 
 				try {
 					// convert String to enum Classifier
 					String classifierAsString = parameters
 							.get(ParameterKey.CLASSIFIER.toString())[0];
 					Classifier classifier = null;
 					classifier = Classifier.valueOf(classifierAsString);
 
 					List<Report> result = new ArrayList<Report>();
 					result.addAll(oracle.classify(instance, classifier,
 							authenticationDetails, specialRequests));
 					responseWithReports.withReports(result);
 
 					out.print(new Gson().toJson(responseWithReports));
 					return;
 				} catch (NoSuchAlgorithmException e) {
 					responseWithReports
 							.setReturnCode(ReturnCode.NO_SUCH_ALGORITHM);
 					responseWithReports.setExplanation(e.toString());
 					out.print(new Gson().toJson(responseWithReports));
 					return;
 				}
 			} else if (operation.equals(Operation.UPLOAD_TRAINING_DATA
 					.toString())) {
 				// Process the batch of training data
 				String json = Deserializer.getSingleParameter(parameters,
 						ParameterKey.BATCH_TRAINING_DATA);
 				TrainingData trainingData = InstanceFriendlyGson.gson.fromJson(
 						json, new TypeToken<TrainingData>() {
 						}.getType());
 				classifierDAO
 						.saveInstances(trainingData, authenticationDetails);
 				out.print(returnJson(ReturnCode.OK,
 						"Training data has been saved"));
 				return;
 			}
 		} catch (Exception e) {
 			Response errorResponse = new ResponseWithReports()
 			.withReturnCode(ReturnCode.ILLEGAL_ARGUMENT).withExplanation(e.toString());
 			out.print(new Gson().toJson(errorResponse));
 			return;
 		} finally {
 			out.close();
 		}
 	}
 	
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPut(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Detect intent, it is either reporting a correct classification or
 		// trying to contribute training data
 		// Two ways, either by Report, or by WifiInformation
 		Map<String, String[]> parameters = request.getParameterMap();
 		log.info("Received PUT request with params:" + MapUtils.printParameterMap(parameters));
 
 		PrintWriter out = response.getWriter();
 		try {
 			String operation = Deserializer.getSingleParameter(parameters,
 					ParameterKey.OPERATION);
 			AuthenticationDetails authenticationDetails = Deserializer
 					.getAuenticationDetails(parameters);
 			if (operation.equals(Operation.CONFIRM_VALID_CLASSIFICATION
 					.toString())) {
 				// Just a single report, ie client validates the classification
 				// is correct.
 				// So add this new instance to db.
 				String room = Deserializer.getSingleParameter(parameters,
 						ParameterKey.ROOM);
 				String instanceAsJson = Deserializer.getSingleParameter(
 						parameters, ParameterKey.INSTANCE);
 				Instance instance = InstanceFriendlyGson.gson.fromJson(
 						instanceAsJson, new TypeToken<Instance>() {
 						}.getType());
 				classifierDAO.saveInstance(instance, room,
 						authenticationDetails);
 				out.print(returnJson(ReturnCode.OK, "Instance has been saved"));
 				return;
 			} else if(operation.equals(Operation.UPLOAD_STATISTICS.toString())){
 				log.info("Saving statistics");
 				String listOfStatJson = Deserializer.getSingleParameter(parameters,
 						ParameterKey.VALIDATION_STATISTICS);
 				List<RoomStatistic> stats = new Gson().fromJson(listOfStatJson, new TypeToken<List<RoomStatistic>>() {
 						}.getType());
 				classifierDAO.saveStatistics(stats);
 			} else {
 				out.print(returnJson(ReturnCode.NO_RESPONSE,
 						"Supported Operations: UPLOAD_TRAINING_DATA & CONFIRM_VALID_CLASSIFICATION."));
 				return;
 			}
 		} catch (IllegalArgumentException e) {
 			out.print(returnJson(ReturnCode.ILLEGAL_ARGUMENT, e.getMessage()));
 			return;
 		} finally {
 			out.close();
 			log.info("put done, connection closed, results returned");
 		}
 
 	}
 
 	private String returnJson(ReturnCode returnCode, String explanation) {
 		String json = new Gson().toJson(
 				new Response().withExplanation(explanation).withReturnCode(
 						returnCode), new TypeToken<Response>() {
 				}.getType());
 		log.info("json response:" + json);
 		return json;
 	}
 
 }
