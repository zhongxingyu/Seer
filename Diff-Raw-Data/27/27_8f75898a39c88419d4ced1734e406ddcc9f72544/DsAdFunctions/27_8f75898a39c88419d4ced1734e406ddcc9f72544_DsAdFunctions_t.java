 package edu.thu.keg.mdap.restful.dataset;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.swing.text.html.parser.Entity;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import edu.thu.keg.mdap.DataSetManager;
 import edu.thu.keg.mdap.Platform;
 import edu.thu.keg.mdap.datafeature.DataFeatureType;
 import edu.thu.keg.mdap.datafeature.DataView;
 import edu.thu.keg.mdap.datamodel.AggregatedDataField;
 import edu.thu.keg.mdap.datamodel.DataField;
 import edu.thu.keg.mdap.datamodel.DataSet;
 import edu.thu.keg.mdap.datamodel.GeneralDataField;
 import edu.thu.keg.mdap.datamodel.Query;
 import edu.thu.keg.mdap.datamodel.DataField.FieldFunctionality;
 import edu.thu.keg.mdap.datamodel.DataField.FieldType;
 import edu.thu.keg.mdap.init.MessageInfo;
 import edu.thu.keg.mdap.management.ManagementPlatform;
 import edu.thu.keg.mdap.provider.DataProvider;
 import edu.thu.keg.mdap.provider.DataProviderException;
 import edu.thu.keg.mdap.restful.exceptions.UserNotInPoolException;
 import edu.thu.keg.mdap_impl.datamodel.DataSetImpl;
 import edu.thu.keg.mdap_impl.provider.JdbcProvider;
 
 /**
  * the functions of dataset's administrator operations
  * 
  * @author Yuan Bozhi
  * 
  */
 @Path("/dsa")
 public class DsAdFunctions {
 	@Context
 	UriInfo uriInfo;
 	@Context
 	Request request;
 	@Context
 	ServletContext servletcontext;
 	@Context
 	HttpServletRequest httpServletRequest;
 	@Context
 	HttpServletResponse httpServletResponse;
 
 	HttpSession session = null;
 
 	private static Logger log = Logger.getLogger(DsAdFunctions.class);
 
 	@POST
 	@Path("/runsql")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	// @Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public Response runSQL(@FormParam("dbname") String dbname,
 			@FormParam("sql") String sql, @FormParam("database") String db,
 			@FormParam("username") String username,
 			@FormParam("password") String password) {
 		log.info(uriInfo.getAbsolutePath());
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataProvider provider = null;
 			JdbcProvider jdbc;
 			switch (db) {
 			case "sqlserver":
 				provider = p.getDataProviderManager().getDefaultSQLProvider(
 						dbname);
 				jdbc = (JdbcProvider) provider;
 				jdbc.execute(sql);
 				break;
 			case "oracle":
 				provider = p.getDataProviderManager().getDefaultOracleProvider(
 						dbname, username, password);
 				jdbc = (JdbcProvider) provider;
 				jdbc.execute(sql);
 				break;
 			default:
 				break;
 			}
 			if ((provider == null)) {
 				return Response.status(409).entity("provider not found!\n")
 						.build();
 			}
 		} catch (DataProviderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return Response.status(Status.OK).build();
 		// return Response.created(uriInfo.getAbsolutePath()).build();
 	}
 
 	/**
 	 * create a new dataset
 	 * 
 	 * @param connstr
 	 * @param dataset
 	 * @param JContent
 	 * @return
 	 */
 	@POST
 	@Path("/addds")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	// @Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public Response createDataset(@FormParam("connstr") String connstr,
 			@FormParam("dataset") String dataset,
 			@FormParam("description") String description,
 			@FormParam("loadable") boolean loadable,
 			@FormParam("owner") String owner,
 			@FormParam("permission") String permission,
 			@FormParam("dsfields") JSONArray datafields,
 			@FormParam("limitedusers") JSONArray limitedusers) {
 		/**
 		 * description 数据集描述 loadable 是否可以加载 dsFields 数据域的jsonarray fieldName
 		 * fieldType description isKey
 		 */
 		System.out.println("create dataset");
 		log.info(uriInfo.getAbsolutePath());
 		session = httpServletRequest.getSession();
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException("the cookies is timeout!");
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataProvider provider = p.getDataProviderManager()
 					.getDefaultSQLProvider(connstr);
 			if ((provider == null)) {
 				return Response.status(409).entity("provider not found!\n")
 						.build();
 			}
 			String fieldname = null;
 			FieldType fieldtype = null;
 			String df_description = null;
 			boolean isKey = false;
 			boolean allowNull = false;
 			boolean isDim = false;
 			String func;
 			DataField[] fields = new DataField[datafields.length()];
 			for (int i = 0; i < datafields.length(); i++) {
 				JSONObject field = datafields.getJSONObject(i);
 				fieldname = field.getString("fieldname");
 				fieldtype = DataField.FieldType.parse(field
 						.getString("fieldtype"));
 				df_description = field.getString("description");
 				isKey = field.getBoolean("iskey");
 				allowNull = field.getBoolean("allownull");
 				isDim = field.getBoolean("isdim");
 				func = field.getString("func");
 				fields[i] = new GeneralDataField(fieldname, fieldtype,
 						df_description, isKey, allowNull, isDim,
 						FieldFunctionality.parse(func));
 			}
 			p.getDataSetManager().createDataSet(dataset, owner, description,
 					provider, loadable, fields);
 			List<String> users_list = new ArrayList<>();
 			for (int i = 0; i < limitedusers.length(); i++) {
 				JSONObject user = limitedusers.getJSONObject(i);
 				users_list.add(user.getString("limiteduser"));
 			}
 			p.getDataSetManager().setDataSetPermission(dataset, owner,
 					DataSetImpl.parsePermission(permission), users_list);
 			p.getDataSetManager().saveChanges();
 		} catch (JSONException | IOException e) {
 			// TODO Auto-generated catch block
 			log.warn(e.getMessage());
 		} catch (UserNotInPoolException e) {
 			log.warn(e.getMessage());
 			System.out.println("cating...");
 			// return Response.status(Status.OK).build();
 			try {
 				return Response
 						.created(new URI("http://www.baidu.com"))
 						.entity((new JSONObject()).put("status", "false")
 								.put("msg", e.getMessage()).toString()).build();
 			} catch (JSONException | URISyntaxException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 		return Response.status(Status.OK).build();
 		// return Response.created(uriInfo.getAbsolutePath()).build();
 	}
 
 	@POST
 	@Path("/adddv")
 	public Response createDataview(@FormParam("dataset") String dataset,
 			@FormParam("dataview") String dataview,
 			@FormParam("description") String description,
 			@FormParam("datafeaturetype") String datafuturetype,
 			@FormParam("keys") JSONArray keys,
 			@FormParam("values") JSONArray values) {
 		log.info(uriInfo.getAbsolutePath());
 		session = httpServletRequest.getSession();
 		System.out.println("POST create dataview:\n" + dataset + " " + dataview
 				+ "\n " + keys.toString() + "\n" + values.toString());
 
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			ManagementPlatform mp = (ManagementPlatform) servletcontext
 					.getAttribute("managementplatform");
 			System.out
 					.println("session user=" + session.getAttribute("userid"));
 
 			DataView dv = null;
 			DataSet ds = null;
 			DataField[] ks = null, vs = null, kv = null;
 			Query q = null;
 
 			ds = p.getDataSetManager().getDataSet(dataset);
 			ks = new DataField[keys.length()];
 			for (int i = 0; i < ks.length; i++) {
 				ks[i] = ds.getField(keys.getString(i));
 			}
 			vs = new DataField[values.length()];
 			for (int i = 0; i < vs.length; i++) {
 				vs[i] = ds.getField(values.getString(i));
 			}
 			kv = Arrays.copyOf(ks, ks.length + vs.length);
 			System.arraycopy(vs, 0, kv, ks.length, vs.length);
 			q = ds.getQuery().select(kv);
 			dv = p.getDataSetManager().defineView(dataview,
 					(String) session.getAttribute("userid"), dataset,
 					description, DataFeatureType.valueOf(datafuturetype), q,
 					ks, vs);
 
 			p.getDataSetManager().saveChanges();
 		} catch (IOException | UserNotInPoolException
 				| OperationNotSupportedException | DataProviderException
 				| JSONException | IllegalArgumentException e) {
 
 			try {
 				return Response
 						.ok()
 						.entity(new JSONObject().put("error", e.getMessage())
 								.toString()).build();
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 		return Response.ok().build();
 	}
 
 	@POST
 	@Path("/setdv")
 	public Response setDataview(@FormParam("olddataview") String olddv,
 			@FormParam("dataset") String dataset,
 			@FormParam("dataview") String dataview,
 			@FormParam("description") String description,
 			@FormParam("keys") JSONArray keys,
 			@FormParam("values") JSONArray values) {
 		log.info(uriInfo.getAbsolutePath());
 		session = httpServletRequest.getSession();
 		System.out.println("POST create dataview:\n" + dataset + " " + dataview
 				+ "\n " + keys.toString() + "\n" + values.toString());
 
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			ManagementPlatform mp = (ManagementPlatform) servletcontext
 					.getAttribute("managementplatform");
 			System.out
 					.println("session user=" + session.getAttribute("userid"));
 
 			DataView dv = null;
 			DataSet ds = null;
 			DataField[] ks = null, vs = null, kv = null;
 			Query q = null;
 
 			ds = p.getDataSetManager().getDataSet(dataset);
 			ks = new DataField[keys.length()];
 			for (int i = 0; i < ks.length; i++) {
 				ks[i] = ds.getField(keys.getString(i));
 			}
 			vs = new DataField[values.length()];
 			for (int i = 0; i < vs.length; i++) {
 				vs[i] = ds.getField(values.getString(i));
 			}
 			kv = Arrays.copyOf(ks, ks.length + vs.length);
 			System.arraycopy(vs, 0, kv, ks.length, vs.length);
 			q = ds.getQuery().select(kv);
 			p.getDataSetManager().redefineView(olddv, dataview, description, q,
 					ks, vs);
 
 			p.getDataSetManager().saveChanges();
 		} catch (IOException | UserNotInPoolException
 				| OperationNotSupportedException | DataProviderException
 				| JSONException | IllegalArgumentException e) {
 
 			try {
 				return Response
 						.ok()
 						.entity(new JSONObject().put("error", e.getMessage())
 								.toString()).build();
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 		return Response.ok().build();
 	}
 
 	@POST
 	@Path("/adddvagg")
 	public Response createDataviewAggregated(
 			@FormParam("dataset") String dataset,
 			@FormParam("dataview") String dataview,
 			@FormParam("description") String description,
 			@FormParam("fields") JSONArray fields,
 			@FormParam("funcs") JSONArray funcs) {
 		log.info(uriInfo.getAbsolutePath());
 		session = httpServletRequest.getSession();
 		System.out.println("POST create dataview:\n" + dataset + " " + dataview
 				+ "\n " + fields.toString() + "\n" + funcs.toString());
 
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			ManagementPlatform mp = (ManagementPlatform) servletcontext
 					.getAttribute("managementplatform");
 			System.out
 					.println("session user=" + session.getAttribute("userid"));
 
 			DataView dv = null;
 			DataSet ds = null;
 			DataField[] fs = null;
 			Query q = null;
 
 			ds = p.getDataSetManager().getDataSet(dataset);
 			fs = new DataField[fields.length()];
 			for (int i = 0; i < fs.length; i++) {
 				String fun = funcs.getString(i);
 				if (fun.toLowerCase().equals("gb"))
 					fs[i] = ds.getField(fields.getString(i));
 				else
 					fs[i] = new AggregatedDataField(ds.getField(fields
 							.getString(i)),
 							AggregatedDataField.AggrFunction.valueOf(funcs
 									.getString(i)), fields.getString(i) + " "
 									+ funcs.getString(i));
 			}
 			q = ds.getQuery().select(fs);
 			dv = p.getDataSetManager().defineView(dataview,
 					(String) session.getAttribute("userid"), dataset,
 					description, DataFeatureType.ValueFeature, q);
 
 			p.getDataSetManager().saveChanges();
 		} catch (IOException | UserNotInPoolException
 				| OperationNotSupportedException | DataProviderException
 				| JSONException | IllegalArgumentException e) {
 
 			try {
 				return Response
 						.ok()
 						.entity(new JSONObject().put("error", e.getMessage())
 								.toString()).build();
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 		return Response.ok().build();
 	}
 
 	@POST
 	@Path("/setdvagg")
 	public Response setDataviewAggregated(
 			@FormParam("olddataview") String olddv,
 			@FormParam("dataset") String dataset,
 			@FormParam("dataview") String dataview,
 			@FormParam("description") String description,
 			@FormParam("fields") JSONArray fields,
 			@FormParam("funcs") JSONArray funcs) {
 		log.info(uriInfo.getAbsolutePath());
 		session = httpServletRequest.getSession();
 		System.out.println("POST create dataview:\n" + dataset + " " + dataview
 				+ "\n " + fields.toString() + "\n" + funcs.toString());
 
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			ManagementPlatform mp = (ManagementPlatform) servletcontext
 					.getAttribute("managementplatform");
 			System.out
 					.println("session user=" + session.getAttribute("userid"));
 
 			DataView dv = null;
 			DataSet ds = null;
 			DataField[] fs = null;
 			Query q = null;
 
 			ds = p.getDataSetManager().getDataSet(dataset);
 			fs = new DataField[fields.length()];
 			for (int i = 0; i < fs.length; i++) {
 				String fun = funcs.getString(i);
 				if (fun.toLowerCase().equals("gb"))
 					fs[i] = ds.getField(fields.getString(i));
 				else
 					fs[i] = new AggregatedDataField(ds.getField(fields
 							.getString(i)),
 							AggregatedDataField.AggrFunction.valueOf(funcs
 									.getString(i)), fields.getString(i) + " "
 									+ funcs.getString(i));
 			}
 			q = ds.getQuery().select(fs);
 			p.getDataSetManager().redefineView(olddv, dataview, description, q);
 			p.getDataSetManager().saveChanges();
 		} catch (IOException | UserNotInPoolException
 				| OperationNotSupportedException | DataProviderException
 				| JSONException | IllegalArgumentException e) {
 
 			try {
 				return Response
 						.ok()
 						.entity(new JSONObject().put("error", e.getMessage())
 								.toString()).build();
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 		return Response.ok().build();
 	}
 
 	@POST
 	@Path("/setdsp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	// // @Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public Response setDatasetPermission(@FormParam("dataset") String dataset,
 			@FormParam("dataview") String dataview,
 			@FormParam("owner") String owner,
 			@FormParam("permisson") String permission,
 			@FormParam("limitedusers") JSONArray limitedusers) {
 		log.info(uriInfo.getAbsolutePath());
 		session = httpServletRequest.getSession();
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			List<String> users = null;
 			users = new ArrayList<String>();
 			for (int i = 0; i < limitedusers.length(); i++) {
 				users.add(limitedusers.getJSONObject(i)
 						.getString("limiteduser"));
 			}
 			p.getDataSetManager().setDataSetPermission(dataset, owner,
 					DataSetImpl.parsePermission(permission), users);
 		} catch (UserNotInPoolException | JSONException e) {
 			// TODO Auto-generated catch block
 			log.warn(e.getMessage());

 			try {
 				return Response
						.ok()
						.entity(new JSONObject().put("error", e.getMessage())
								.toString()).build();
			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}

 		}
 		return Response.status(Status.OK).build();
 	}
 
 	@DELETE
 	@Path("/rmds")
 	public void removeDataset(@FormParam("dataset") String dataset) {
 		session = httpServletRequest.getSession();
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			datasetManager.removeDataSet(datasetManager.getDataSet(dataset));
 			datasetManager.saveChanges();
 		} catch (UserNotInPoolException | DataProviderException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@POST
 	@Path("/rmdv")
	public Response removeDataview(@FormParam("dataset") String dataview) {
 		session = httpServletRequest.getSession();
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			datasetManager.removeDataView(datasetManager.getDataView(dataview));
 			datasetManager.saveChanges();
 		} catch (UserNotInPoolException | DataProviderException | IOException e) {
 			try {
				return Response
						.ok()
						.entity(new JSONObject().put("error", e.getMessage())
								.toString()).build();
			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
		return Response.status(Status.OK).build();
 	}
 }
