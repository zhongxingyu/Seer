 package com.kittens.controller.api;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import com.kittens.database.Dataset;
 import com.kittens.database.User;
 import com.kittens.Utils;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 
 public class AjaxDatasetAPIController extends BaseAPIController {
 
 	// the version of this object
 	private static final long serialVersionUID = 0L;
 
 	/**
 	 * Parses a {@code JSONObject} into a dataset.
 	 * <pre>
 	 * {@code
 	 * {
 	 *     "uuid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
 	 *     "headers": ["a", "b", "c"],
 	 *     "rows": [
 	 *         ["foo", "bar", "baz"],
 	 *         ["foo", "bar", "baz"]
 	 *     ]
 	 * }
 	 * }
 	 * </pre>
 	 */
 	private void updateDatasetData(JsonObject datasetJson) throws SQLException {
 		// fields
 		final String UUID = datasetJson.get("uuid").getAsString();
 		final ArrayList<String> headers = new ArrayList<String>();
 		final ArrayList<Dataset.Row> rows = new ArrayList<Dataset.Row>();
 		// set the fields
 		JsonArray jsonHeaders = datasetJson.getAsJsonArray("headers");
 		for (JsonElement header : jsonHeaders) {
 			headers.add(header.getAsString());
 		}
 		// for each row
 		JsonArray rowsArrayJson = datasetJson.getAsJsonArray("rows");
 		for (JsonElement rowJson : rowsArrayJson) {
 			JsonArray rowArrayJson = rowJson.getAsJsonArray();
 			ArrayList<String> values = new ArrayList<String>();
 			for (JsonElement columnJson : rowArrayJson) {
 				values.add(columnJson.getAsString());
 			}
 			rows.add(new Dataset.Row(values));
 		}
 		database.dropCreateDataset(UUID, headers, rows);
 	}
 	/**
 	 * Handle GET requests.
 	 * Returns the dataset associated with the parameterized UUID.
 	 */
 	@Override public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		User currentSessionUser = getUserOrSendError(request, response);
 		if (currentSessionUser == null) return;
 		final String databaseUUID = request.getParameter("uuid");
 		try {
 			Dataset dataset = database.getDataset(databaseUUID);
			response.setContentType("application/json");
 			response.getWriter().print(gson.toJson(dataset));
 		}
 		catch (SQLException e) {
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return;
 	}
 	/**
 	 * Handle POST requests.
 	 * Creates new datasets for the current user.
 	 */
 	@Override public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		User currentSessionUser = getUserOrSendError(request, response);
 		if (currentSessionUser == null) return;
 		Dataset newDataset = Dataset.newSampleDataset(currentSessionUser);
 		try {
 			database.addDataset(currentSessionUser, newDataset);
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
		response.setContentType("application/json");
 		response.getWriter().print(gson.toJson(newDataset));
 	}
 	/**
 	 * Handle PUT requests.
 	 * Updates datasets for the user.
 	 */
 	@Override public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		User currentSessionUser = getUserOrSendError(request, response);
 		if (currentSessionUser == null) return;
 		final String json = Utils.readStream(request.getInputStream());
 		// System.out.println(json);
 		final JsonObject datasetChangeRequest = parser.parse(json).getAsJsonObject();
 		final String requestType = datasetChangeRequest.get("type").getAsString();
 		if (requestType.equals("meta")) {
 			// change metadata
 			database.updateDatasetMetadata(
 				datasetChangeRequest.get("uuid").getAsString(),
 				datasetChangeRequest.get("name").getAsString(),
 				datasetChangeRequest.get("description").getAsString()
 			);
 		}
 		else if (requestType.equals("data")) {
 			// update data
 			try {
 				updateDatasetData(datasetChangeRequest);
 			}
 			catch (SQLException e) {
 				e.printStackTrace();
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				return;
 			}
 		}
 		else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
		response.setContentType("application/json");
 		response.getWriter().print(json);
 	}
 
 }
