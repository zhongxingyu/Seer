 /**
  * Author: Fernando Serena (fserena@ciclope.info)
  * Organization: Ciclope Group (UPM)
  * Project: GLORIA
  */
 package eu.gloria.gs.services.api.resources;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.HEAD;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import eu.gloria.gs.services.core.client.GSClientProvider;
 import eu.gloria.gs.services.experiment.ExperimentException;
 import eu.gloria.gs.services.experiment.ExperimentInterface;
 import eu.gloria.gs.services.experiment.base.data.ExperimentInformation;
 import eu.gloria.gs.services.experiment.base.data.ExperimentRuntimeInformation;
 import eu.gloria.gs.services.experiment.base.data.ExperimentType;
 import eu.gloria.gs.services.experiment.base.data.NoSuchExperimentException;
 import eu.gloria.gs.services.experiment.base.data.OperationInformation;
 import eu.gloria.gs.services.experiment.base.data.ParameterInformation;
 import eu.gloria.gs.services.experiment.base.data.ReservationInformation;
 import eu.gloria.gs.services.experiment.base.data.ResultInformation;
 import eu.gloria.gs.services.experiment.base.data.TimeSlot;
 import eu.gloria.gs.services.experiment.base.models.DuplicateExperimentException;
 import eu.gloria.gs.services.experiment.base.models.InvalidUserContextException;
 import eu.gloria.gs.services.experiment.base.operations.ExperimentOperation;
 import eu.gloria.gs.services.experiment.base.operations.ExperimentOperationException;
 import eu.gloria.gs.services.experiment.base.operations.NoSuchOperationException;
 import eu.gloria.gs.services.experiment.base.parameters.NoSuchParameterException;
 import eu.gloria.gs.services.experiment.base.parameters.ExperimentParameter;
 import eu.gloria.gs.services.experiment.base.parameters.ExperimentParameterException;
 import eu.gloria.gs.services.experiment.base.parameters.ParameterType;
 import eu.gloria.gs.services.experiment.base.reservation.ExperimentNotInstantiatedException;
 import eu.gloria.gs.services.experiment.base.reservation.ExperimentReservationArgumentException;
 import eu.gloria.gs.services.experiment.base.reservation.MaxReservationTimeException;
 import eu.gloria.gs.services.experiment.base.reservation.NoReservationsAvailableException;
 import eu.gloria.gs.services.experiment.base.reservation.NoSuchReservationException;
 import eu.gloria.gs.services.experiment.script.NoSuchScriptException;
 import eu.gloria.gs.services.experiment.script.OverlapRTScriptException;
 import eu.gloria.gs.services.experiment.script.data.RTScriptInformation;
 import eu.gloria.gs.services.repository.rt.RTRepositoryException;
 import eu.gloria.gs.services.repository.rt.RTRepositoryInterface;
 import eu.gloria.gs.services.repository.user.InvalidUserException;
 import eu.gloria.gs.services.utils.ObjectResponse;
 
 /**
  * @author Fernando Serena (fserena@ciclope.info)
  * 
  */
 @Path("/experiments")
 public class Experiments extends GResource {
 
 	@Context
 	HttpServletRequest request;
 	@Context
 	private HttpHeaders headers;
 
 	private static ExperimentInterface experiments = GSClientProvider
 			.getOnlineExperimentClient();
 
 	private static RTRepositoryInterface rtRepository = GSClientProvider
 			.getRTRepositoryClient();
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/online/register")
 	public Response registerOnlineExperiment(@QueryParam("name") String name) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			experiments.createOnlineExperiment(name);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (DuplicateExperimentException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/offline/register")
 	public Response registerOfflineExperiment(@QueryParam("name") String name) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			experiments.createOfflineExperiment(name);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (DuplicateExperimentException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/online/list")
 	public Response listOnlineExperiments() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
			List<String> names = experiments.getAllExperiments(ExperimentType.ONLINE);
 
 			return this.processSuccess(names);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/offline/list")
 	public Response listOfflineExperiments() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
			List<String> names = experiments.getAllExperiments(ExperimentType.OFFLINE);
 
 			return this.processSuccess(names);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/active")
 	public Response listActiveExperiments() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			List<ReservationInformation> reservations = experiments
 					.getMyCurrentReservations(null);
 
 			return this.processSuccess(reservations);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoReservationsAvailableException e) {
 			// return this.processError(Status.NOT_FOUND, e);
 			return this.processSuccess(new Object[0]);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/online/active")
 	public Response listActiveOnlineExperiments() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			List<ReservationInformation> reservations = experiments
 					.getMyCurrentReservations(ExperimentType.ONLINE);
 
 			return this.processSuccess(reservations);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoReservationsAvailableException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/offline/active")
 	public Response listActiveOfflineExperiments() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			List<ReservationInformation> reservations = experiments
 					.getMyCurrentReservations(ExperimentType.OFFLINE);
 
 			return this.processSuccess(reservations);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoReservationsAvailableException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/pending")
 	public Response listPendingExperiments() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			List<ReservationInformation> reservations = experiments
 					.getMyPendingReservations(null);
 
 			return this.processSuccess(reservations);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoReservationsAvailableException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/online/pending")
 	public Response listPendingOnlineExperiments() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			List<ReservationInformation> reservations = experiments
 					.getMyPendingReservations(ExperimentType.ONLINE);
 
 			return this.processSuccess(reservations);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoReservationsAvailableException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/offline/pending")
 	public Response listPendingOfflineExperiments() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			List<ReservationInformation> reservations = experiments
 					.getMyPendingReservations(ExperimentType.OFFLINE);
 
 			return this.processSuccess(reservations);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoReservationsAvailableException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/online/slots/available/{year}/{month}/{day}")
 	public Response listAvailableTimeSlots(@PathParam("year") String year,
 			@PathParam("month") String month, @PathParam("day") String day,
 			ListAvailableTimeSlotsRequest data) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			List<TimeSlot> timeSlots = experiments.getAvailableReservations(
 					data.getExperiment(), data.getTelescopes());
 
 			Calendar calendar = Calendar.getInstance();
 			List<TimeSlot> filteredTimeSlots = new ArrayList<TimeSlot>();
 
 			for (TimeSlot timeSlot : timeSlots) {
 				calendar.setTime(timeSlot.getBegin());
 				if (calendar.get(Calendar.DAY_OF_MONTH) == Integer.valueOf(day)
 						&& calendar.get(Calendar.MONTH) == Integer
 								.valueOf(month)
 						&& calendar.get(Calendar.YEAR) == Integer.valueOf(year)) {
 
 					filteredTimeSlots.add(timeSlot);
 
 				}
 			}
 
 			return this.processSuccess(filteredTimeSlots);
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (ExperimentReservationArgumentException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		}
 	}
 
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/online/reserve")
 	public Response reserveExperiment(ReserveOnlineExperimentRequest data) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			TimeSlot timeSlot = new TimeSlot();
 			timeSlot.setBegin(data.getBegin());
 			timeSlot.setEnd(data.getEnd());
 
 			List<String> telescopes = data.getTelescopes();
 			for (String rt : telescopes) {
 				try {
 					if (!rtRepository.containsRT(rt)) {
 						return this.processError(Status.NOT_ACCEPTABLE,
 								"wrong telescopes", rt + " is not a telescope");
 					}
 				} catch (RTRepositoryException e) {
 					return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 				}
 			}
 
 			experiments.reserveExperiment(data.getExperiment(),
 					data.getTelescopes(), timeSlot);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoReservationsAvailableException
 				| ExperimentReservationArgumentException
 				| MaxReservationTimeException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		}
 	}
 
 	@GET
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/cancel")
 	public Response cancelReservation(@PathParam("rid") int rid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			experiments.cancelExperimentReservation(rid);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		}
 	}
 
 	@GET
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/reset")
 	public Response resetContext(@PathParam("rid") int rid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			experiments.resetExperimentContext(rid);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		} catch (ExperimentNotInstantiatedException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/offline/apply")
 	public Response applyForExperiment(
 			@QueryParam("experiment") String experiment) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			experiments.applyForExperiment(experiment);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoReservationsAvailableException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/remaining")
 	public Response getContextRemainingTime(@PathParam("rid") int rid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			ExperimentRuntimeInformation runtimeInfo = experiments
 					.getExperimentRuntimeInformation(rid);
 			long remaining = runtimeInfo.getRemainingTime();
 
 			return this.processSuccess(remaining);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (ExperimentNotInstantiatedException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/elapsed")
 	public Response getContextElapsedTime(@PathParam("rid") int rid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			ExperimentRuntimeInformation runtimeInfo = experiments
 					.getExperimentRuntimeInformation(rid);
 			long elapsed = runtimeInfo.getElapsedTime();
 
 			return this.processSuccess(elapsed);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (ExperimentNotInstantiatedException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}")
 	public Response getExperimentContext(@PathParam("rid") int rid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			ObjectResponse response = experiments.getExperimentContext(rid);
 
 			return this.processSuccess(JSONConverter.fromJSON(
 					(String) response.content, Object.class, null));
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchReservationException | NoSuchParameterException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (ExperimentNotInstantiatedException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/ready")
 	public Response isExperimentReady(@PathParam("rid") int rid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			boolean instantiated = experiments.isExperimentContextReady(rid);
 
 			return this.processSuccess(instantiated);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/info")
 	public Response getReservationInfo(@PathParam("rid") int rid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			ReservationInformation resInfo = experiments
 					.getReservationInformation(rid);
 
 			return this.processSuccess(resInfo);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/parameters/{parameter}")
 	public Response getParameterContextValue(@PathParam("rid") int rid,
 			@PathParam("parameter") String parameter,
 			@QueryParam("tree") String tree) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			ReservationInformation resInfo = experiments
 					.getReservationInformation(rid);
 
 			ParameterInformation paramInfo = experiments
 					.getParameterInformation(resInfo.getExperiment(), parameter);
 
 			String parameterTree = parameter;
 			if (tree != null) {
 				tree = tree.replace("%5B", "[");
 				tree = tree.replace("%5D", "]");
 				parameterTree = parameterTree + "." + tree;
 			}
 
 			ObjectResponse response = experiments.getExperimentParameterValue(
 					rid, parameterTree);
 
 			Object value = null;
 			Class<?> valueType = Object.class;
 			Class<?> elementType = null;
 
 			ParameterType type = paramInfo.getParameter().getType();
 			valueType = type.getValueType();
 			elementType = type.getElementType();
 
 			value = JSONConverter.fromJSON((String) response.content,
 					valueType, elementType);
 
 			return this.processSuccess(value);
 
 		} catch (ExperimentException | ExperimentParameterException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException | NoSuchReservationException
 				| NoSuchParameterException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (ExperimentNotInstantiatedException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		}
 	}
 
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/parameters/{parameter}")
 	public Response setParameterContextValue(@PathParam("rid") int rid,
 			@PathParam("parameter") String parameter,
 			@QueryParam("tree") String tree, Object data) {
 
 		this.setupRegularAuthorization(request);
 
 		Object castedValue = data;
 
 		try {
 
 			ReservationInformation resInfo = experiments
 					.getReservationInformation(rid);
 
 			ExperimentInformation expInfo = experiments
 					.getExperimentInformation(resInfo.getExperiment());
 
 			List<ParameterInformation> parameterInfos = expInfo.getParameters();
 
 			for (ParameterInformation paramInfo : parameterInfos) {
 				if (paramInfo.getName().equals(parameter)) {
 					castedValue = JSONConverter.toJSON(data);
 				}
 			}
 		} catch (ExperimentException | NoSuchExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		} catch (NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 
 		try {
 
 			String parameterTree = parameter;
 			if (tree != null) {
 				tree = tree.replace("%5B", "[");
 				tree = tree.replace("%5D", "]");
 				parameterTree = parameterTree + "." + tree;
 			}
 
 			experiments.setExperimentParameterValue(rid, parameterTree,
 					new ObjectResponse(castedValue));
 			return this.processSuccess();
 
 		} catch (NoSuchReservationException | NoSuchParameterException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (ExperimentNotInstantiatedException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (ExperimentParameterException e) {
 			return this.processError(Status.BAD_REQUEST, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		}
 	}
 
 	private String[] generateStringArgs(Object args) {
 		String[] argStr = new String[1];
 
 		argStr[0] = JSONConverter.toJSON(args);
 
 		return argStr;
 	}
 
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}")
 	public Response registerExperiment(
 			@PathParam("experiment") String experiment,
 			RegisterExperimentRequest data) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			if (data.getType() != null) {
 				if (data.getType().equals("offline")) {
 					experiments.createOfflineExperiment(experiment);
 				} else if (data.getType().equals("online")) {
 					experiments.createOnlineExperiment(experiment);
 				} else {
 					return this.processError(Status.BAD_REQUEST,
 							"experiment type",
 							"an experiment can only be online or offline");
 				}
 			}
 
 			for (String name : data.getParameters().keySet()) {
 
 				ParameterInformation paramInfo = new ParameterInformation();
 				paramInfo.setName(name);
 				paramInfo.setType((String) data.getParameters().get(name)
 						.getType());
 
 				paramInfo.setArguments(this.generateStringArgs(data
 						.getParameters().get(name).getInit()));
 
 				experiments.addExperimentParameter(experiment, paramInfo);
 
 			}
 
 			for (String name : data.getOperations().keySet()) {
 
 				OperationInformation opInfo = new OperationInformation();
 				opInfo.setName(name);
 				opInfo.setType(data.getOperations().get(name).getType());
 
 				opInfo.setArguments(data.getOperations().get(name)
 						.getArguments());
 
 				experiments.addExperimentOperation(experiment, opInfo);
 
 			}
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (NoSuchParameterException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (DuplicateExperimentException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}")
 	public Response getExperiment(@PathParam("experiment") String experiment) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			LinkedHashMap<String, Object> exp = new LinkedHashMap<String, Object>();
 			LinkedHashMap<String, Object> operations = new LinkedHashMap<String, Object>();
 			LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
 
 			ExperimentInformation expInfo = experiments
 					.getExperimentInformation(experiment);
 
 			exp.put("type", expInfo.getType().name().toLowerCase());
 			exp.put("parameters", parameters);
 			exp.put("operations", operations);
 
 			List<OperationInformation> opInfos = expInfo.getOperations();
 
 			if (opInfos != null) {
 				for (OperationInformation opInfo : opInfos) {
 					LinkedHashMap<String, Object> operation = new LinkedHashMap<String, Object>();
 
 					operation.put("type", opInfo.getType());
 					/*
 					 * operation.put("description", opInfo.getOperation()
 					 * .getDescription());
 					 */
 					operation.put("arguments", opInfo.getArguments());
 					operations.put(opInfo.getName(), operation);
 				}
 			}
 
 			List<ParameterInformation> paramInfos = expInfo.getParameters();
 
 			if (paramInfos != null) {
 				for (ParameterInformation paramInfo : paramInfos) {
 					NewParameterRequest parameter = new NewParameterRequest();
 					parameter.setType(paramInfo.getType());
 
 					Object[] objectArgs = new Object[paramInfo.getArguments().length];
 
 					int i = 0;
 					for (Object arg : paramInfo.getArguments()) {
 						Object objectArg = JSONConverter.fromJSON((String) arg,
 								Object.class, null);
 
 						objectArgs[i] = objectArg;
 					}
 
 					parameter.setInit(objectArgs[0]);
 					parameters.put(paramInfo.getName(), parameter);
 				}
 			}
 
 			return this.processSuccess(exp);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@HEAD
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}")
 	public Response emptyExperiment(@PathParam("experiment") String experiment) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			experiments.emptyExperiment(experiment);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/operations")
 	public Response listOperations(@PathParam("experiment") String experiment,
 			@QueryParam("detailed") boolean detailed) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			List<OperationInformation> opInfos = experiments
 					.getExperimentInformation(experiment).getOperations();
 
 			LinkedHashMap<String, Object> operations = new LinkedHashMap<String, Object>();
 
 			if (opInfos != null) {
 				for (OperationInformation opInfo : opInfos) {
 					LinkedHashMap<String, Object> operation = new LinkedHashMap<String, Object>();
 					operation.put("type", opInfo.getType());
 					operation.put("arguments", opInfo.getArguments());
 					operations.put(opInfo.getName(), operation);
 				}
 			}
 
 			return this.processSuccess(operations);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/parameters")
 	public Response listParameters(@PathParam("experiment") String experiment) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			List<ParameterInformation> paramInfos = experiments
 					.getExperimentInformation(experiment).getParameters();
 
 			LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
 
 			if (paramInfos != null) {
 				for (ParameterInformation paramInfo : paramInfos) {
 					NewParameterRequest parameter = new NewParameterRequest();
 					parameter.setType(paramInfo.getType());
 
 					Object[] objectArgs = new Object[paramInfo.getArguments().length];
 
 					int i = 0;
 					for (Object arg : paramInfo.getArguments()) {
 						Object objectArg = JSONConverter.fromJSON((String) arg,
 								Object.class, null);
 
 						objectArgs[i] = objectArg;
 					}
 
 					parameter.setInit(objectArgs[0]);
 					parameters.put(paramInfo.getName(), parameter);
 				}
 			}
 
 			return this.processSuccess(parameters);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/parameters/{parameter}")
 	public Response showParameterInformation(
 			@PathParam("experiment") String experiment,
 			@PathParam("parameter") String parameter) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			ParameterInformation paramInfo = experiments
 					.getExperimentInformation(experiment).getParameter(
 							parameter);
 
 			return this.processSuccess(paramInfo);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@DELETE
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/parameters/{parameter}")
 	public Response deleteParameter(@PathParam("experiment") String experiment,
 			@PathParam("parameter") String parameter) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			experiments.deleteExperimentParameter(experiment, parameter);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/operations/{operation}")
 	public Response showOperationInformation(
 			@PathParam("experiment") String experiment,
 			@PathParam("operation") String operation) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			OperationInformation opInfo = experiments.getExperimentInformation(
 					experiment).getOperation(operation);
 
 			return this.processSuccess(opInfo);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@DELETE
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/operations/{operation}")
 	public Response deleteOperation(@PathParam("experiment") String experiment,
 			@PathParam("operation") String operation) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			experiments.deleteExperimentOperation(experiment, operation);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/execute/{operation}")
 	public Response executeOperation(@PathParam("rid") int rid,
 			@PathParam("operation") String operation) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			experiments.executeExperimentOperation(rid, operation);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
		} catch (NoSuchOperationException | NoSuchExperimentException
				| NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (ExperimentNotInstantiatedException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (InvalidUserContextException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		} catch (ExperimentOperationException e) {
 			return this.processError(Status.BAD_REQUEST, e);
 		}
 	}
 
 	@DELETE
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}")
 	public Response deleteExperiment(@PathParam("experiment") String experiment) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			experiments.deleteExperiment(experiment);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@PUT
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/parameters")
 	public Response addExperimentParameter(
 			@PathParam("experiment") String experiment,
 			ParameterInformation paramInfo) {
 
 		this.setupRegularAuthorization(request);
 
 		String[] argStr = new String[paramInfo.getArguments().length];
 
 		try {
 			int i = 0;
 			for (Object arg : paramInfo.getArguments()) {
 				argStr[i] = JSONConverter.toJSON(arg);
 
 				i++;
 			}
 
 			paramInfo.setArguments(argStr);
 
 			experiments.addExperimentParameter(experiment, paramInfo);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (NoSuchParameterException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		}
 	}
 
 	@PUT
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/operations")
 	public Response addExperimentOperation(
 			@PathParam("experiment") String experiment,
 			OperationInformation opInfo) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			experiments.addExperimentOperation(experiment, opInfo);
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/engine/parameters")
 	public Response getAllParameters() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			Set<String> parameters = experiments.getAllExperimentParameters();
 			return this.processSuccess(parameters);
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/engine/operations")
 	public Response getAllOperations() {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			Set<String> operations = experiments.getAllExperimentOperations();
 			return this.processSuccess(operations);
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/engine/parameters/{name}")
 	public Response getExperimentParameter(@PathParam("name") String name) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			ExperimentParameter parameter = experiments
 					.getExperimentParameter(name);
 
 			LinkedHashMap<String, Object> paramData = new LinkedHashMap<String, Object>();
 
 			paramData.put("name", name);
 			paramData.put("description", parameter.getDescription());
 			paramData.put("value-class", parameter.getType().getValueType());
 			paramData.put("operation-dep", parameter.getType()
 					.isOperationDependent());
 			List<Object> signature = new ArrayList<Object>();
 			paramData.put("signature", signature);
 
 			int i = 0;
 			for (Class<?> arg : parameter.getType().getArgumentTypes()) {
 				LinkedHashMap<String, Object> argData = new LinkedHashMap<String, Object>();
 				argData.put("class", arg);
 
 				try {
 					argData.put("name", parameter.getType().getArgumentNames()
 							.get(i));
 				} catch (Exception e) {
 					argData.put("name", "?");
 				}
 
 				signature.add(argData);
 				i++;
 			}
 
 			return this.processSuccess(paramData);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchParameterException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/engine/operations/{name}")
 	public Response getExperimentOperation(@PathParam("name") String name) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			ExperimentOperation operation = experiments
 					.getExperimentOperation(name);
 
 			LinkedHashMap<String, Object> opData = new LinkedHashMap<String, Object>();
 
 			opData.put("name", name);
 			opData.put("description", operation.getDescription());
 			opData.put("behaviour-classes", operation.getBehaviour().keySet());
 
 			ExperimentParameter[] argumentTypes = operation.getParameterTypes();
 			List<Object> signature = new ArrayList<Object>();
 			opData.put("signature", signature);
 
 			int i = 0;
 			for (ExperimentParameter param : argumentTypes) {
 				LinkedHashMap<String, Object> paramData = new LinkedHashMap<String, Object>();
 				try {
 					paramData.put("name", operation.getParameterNames()[i]);
 				} catch (Exception e) {
 					paramData.put("name", "?");
 				}
 
 				paramData.put("type", param.getName());
 				paramData.put("value-class", param.getType().getValueType());
 				paramData.put("operation-dep", param.getType()
 						.isOperationDependent());
 
 				signature.add(paramData);
 				i++;
 			}
 
 			return this.processSuccess(opData);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchOperationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{experiment}/results")
 	public Response getExperimentResults(
 			@PathParam("experiment") String experiment,
 			@QueryParam("valuesOnly") boolean valuesOnly) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			List<ResultInformation> results = experiments
 					.getExperimentResults(experiment);
 
 			if (results == null) {
 				results = new ArrayList<>();
 			}
 
 			if (valuesOnly) {
 
 				List<Object> values = new ArrayList<>();
 
 				for (ResultInformation result : results) {
 					values.add(JSONConverter.fromJSON(
 							(String) result.getValue(), Object.class, null));
 				}
 
 				return this.processSuccess(values);
 			}
 
 			for (ResultInformation result : results) {
 				result.setValue(JSONConverter.fromJSON(
 						(String) result.getValue(), Object.class, null));
 			}
 
 			return this.processSuccess(results);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/context/{rid}/results")
 	public Response getContextResults(@PathParam("rid") int rid,
 			@QueryParam("valuesOnly") boolean valuesOnly) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			List<ResultInformation> results = experiments
 					.getContextResults(rid);
 
 			if (results == null) {
 				results = new ArrayList<>();
 			}
 
 			if (valuesOnly) {
 
 				List<Object> values = new ArrayList<>();
 
 				for (ResultInformation result : results) {
 					values.add(JSONConverter.fromJSON(
 							(String) result.getValue(), Object.class, null));
 				}
 
 				return this.processSuccess(values);
 			}
 
 			for (ResultInformation result : results) {
 				result.setValue(JSONConverter.fromJSON(
 						(String) result.getValue(), Object.class, null));
 			}
 
 			return this.processSuccess(results);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (ExperimentNotInstantiatedException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (NoSuchReservationException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/scripts/{rt}")
 	public Response registerRTScript(@PathParam("rt") String rt,
 			RegisterRTScriptRequest data) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 			double seconds = data.getSlot().getLength();
 			if (seconds == 0) {
 				data.getSlot().setLength(60000);
 			}
 
 			int sid = experiments.registerRTScript(rt, data.getSlot(),
 					data.getOperation(), JSONConverter.toJSON(data.getInit()),
 					JSONConverter.toJSON(data.getResult()), data.isNotify());
 
 			return this.processSuccess(sid);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchExperimentException | OverlapRTScriptException e) {
 			return this.processError(Status.BAD_REQUEST, e);
 		} catch (InvalidUserException e) {
 			return this.processError(Status.BAD_REQUEST, e);
 		}
 	}
 
 	@GET
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/scripts/{rt}")
 	public Response getAllRTScript(@PathParam("rt") String rt) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			List<RTScriptInformation> allScripts = new ArrayList<RTScriptInformation>();
 
 			List<Integer> scriptIds = experiments.getAllRTScripts(rt);
 			if (scriptIds != null) {
 				for (int id : scriptIds) {
 					RTScriptInformation scriptInfo = experiments
 							.getRTScriptInformation(id);
 					scriptInfo.setInit(JSONConverter.fromJSON(
 							(String) scriptInfo.getInit(), Object.class, null));
 					allScripts.add(scriptInfo);
 				}
 			}
 
 			return this.processSuccess(allScripts);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchScriptException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/scripts/{rt}/operations")
 	public Response getRTScriptAvailableOperations(@PathParam("rt") String rt) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			List<String> operations = experiments
 					.getRTScriptAvailableOperations(rt);
 			return this.processSuccess(operations);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchScriptException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@GET
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/scripts/id/{sid}")
 	public Response getRTScript(@PathParam("sid") int sid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			RTScriptInformation scriptInfo = experiments
 					.getRTScriptInformation(sid);
 
 			scriptInfo.setInit(JSONConverter.fromJSON(
 					(String) scriptInfo.getInit(), Object.class, null));
 
 			return this.processSuccess(scriptInfo);
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchScriptException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		}
 	}
 
 	@DELETE
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/scripts/id/{sid}")
 	public Response removeRTScript(@PathParam("sid") int sid) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			experiments.removeRTScript(sid);
 
 			return this.processSuccess();
 
 		} catch (ExperimentException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		} catch (NoSuchScriptException e) {
 			return this.processError(Status.NOT_FOUND, e);
 		} catch (InvalidUserException e) {
 			return this.processError(Status.FORBIDDEN, e);
 		}
 	}
 
 }
