 package org.vamdc.tapservice;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.cayenne.BaseContext;
 import org.apache.cayenne.ObjectContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.tapservice.vss2.LogicNode;
 import org.vamdc.tapservice.vss2.Query;
 import org.vamdc.tapservice.vss2.RestrictExpression;
 import org.vamdc.tapservice.vss2.VSSParser;
 import org.vamdc.tapservice.api.RequestInterface;
 import org.vamdc.dictionary.HeaderMetrics;
 import org.vamdc.dictionary.Requestable;
 import org.vamdc.tapservice.util.Setting;
 import org.vamdc.tapservice.util.XSAMSMetrics;
 import org.vamdc.xsams.XSAMSManager;
 import org.vamdc.xsams.XSAMSFactory;
 import org.vamdc.xsams.schema.XSAMSData;
 
 /**
  * Class implementing request processing
  */
 public class RequestProcess implements RequestInterface {
 	private XSAMSManager xsamsroot;
 	private ObjectContext context;
 	private Query query;
 	public boolean Valid;
 	private Date reqstart;
 	private Logger logger;
 
 	private Collection<String> errors = new ArrayList<String>();
 
 	public RequestProcess(XSAMSManager xsamsroot, ObjectContext context,
 			Query queryParser) {
 		initRequest(xsamsroot, context, queryParser);
 	}
 
 	public RequestProcess(String query, Collection<Restrictable> restrictables) {
 		Query parsedQuery = null;
 		try {
 			parsedQuery = VSSParser.parse(query);
 		} catch (IllegalArgumentException e) {
			errors.add("Malformed query: " + e.getMessage());
 		}
 
 		initRequest(XSAMSFactory.getXsamsManager(),
 				BaseContext.getThreadObjectContext(), parsedQuery);
 	}
 
 	private void initRequest(XSAMSManager xsamsroot, ObjectContext context,
 			Query parsedQuery) {
 		this.xsamsroot = xsamsroot;
 		this.context = context;
 		this.query = parsedQuery;
 		this.Valid = false;
 		if (query != null && query.getRestrictsList() != null)
 			this.Valid = query.getRestrictsList().size() > 0;
 
 		logger = LoggerFactory.getLogger("org.vamdc.tapservice");
 		reqstart = new Date();
 
 	}
 
 	public void finishRequest() {
 		// Called before sending data to user, to put time in log
 		if (query != null)
 			logger.info("Request query " + query.getQuery() + " finished in "
 					+ (new Double(new Date().getTime() - reqstart.getTime()))
 					/ 1000.0 + "s");
 		if (query != null && query.getRestrictsTree() != null) {
 			logger.debug("Tree string:" + query.getRestrictsTree().toString());
 			for (RestrictExpression re : query.getRestrictsList()) {
 				logger.debug("Query param:" + re.getColumnName() + "comp"
 						+ re.getOperator() + "val" + re.getValue());
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vamdc.tapservice.RequestInterface#getXsamsManager()
 	 */
 	public XSAMSManager getXsamsManager() {
 		return xsamsroot;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vamdc.tapservice.RequestInterface#getCayenneContext()
 	 */
 	public ObjectContext getCayenneContext() {
 		return context;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vamdc.tapservice.RequestInterface#getRestricts()
 	 */
 	public List<RestrictExpression> getRestricts() {
 		return query.getRestrictsList();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vamdc.tapservice.RequestInterface#getRestrictsTree()
 	 */
 	public LogicNode getRestrictsTree() {
 		return query.getRestrictsTree();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vamdc.tapservice.api.RequestInterface#checkRequestable(Requestable)
 	 * Also checks if tapservice is configured to force source references
 	 */
 	public boolean checkBranch(Requestable branch) {
 		return query.checkSelectBranch(branch);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vamdc.tapservice.RequestInterface#isValid()
 	 */
 	public boolean isValid() {
 		return Valid;
 	}
 
 	/*
 	 * Get user's query
 	 */
 	public String getQueryString() {
 		return query.getQuery();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vamdc.tapservice.RequestInterface#getLogger()
 	 */
 	public Logger getLogger(Class<?> className) {
 		if (className == null)
 			return logger;
 		return LoggerFactory.getLogger(className);
 	}
 
 	/**
 	 * Set headers for response, based on the XSAMS document metrics
 	 * 
 	 * @param base
 	 * @param metrics
 	 *            - metrics attached to produced XSAMS document
 	 * @return
 	 */
 	private static ResponseBuilder setHeaders(ResponseBuilder base,
 			XSAMSMetrics metrics) {
 
 		for (HeaderMetrics hdr : HeaderMetrics.values()) {
 			String header = hdr.name().replace("_", "-");// Headers contain "-",
 															// we use "_" for
 															// variable names
 			base.header(header, metrics.getMetric(hdr));
 		}
 		base.header(
 				"Content-Disposition",
 				"attachment; filename="
 						+ Setting.xsams_idprefix.getValue()
 						+ (new Date().toString().replace(" ", "_")) + ".xsams");
 		return base;
 	}
 
 	/**
 	 * Set headers, based on estimation. Is used for HEAD request processing and
 	 * will be used for stream generation.
 	 * 
 	 * @param base
 	 *            response
 	 * @param metrics
 	 *            estimated response metrics map
 	 * @return response with appended headers
 	 */
 	private ResponseBuilder setEstimateHeaders(ResponseBuilder base,
 			Map<HeaderMetrics, Integer> metrics) {
 		for (HeaderMetrics hdr : HeaderMetrics.values()) {
 			String header = hdr.name().replace("_", "-");// Headers contain "-",
 															// we use "_" for
 															// variable names
 			String value = "0";
 			if (metrics != null && metrics.get(hdr) != null) {
 				value = metrics.get(hdr).toString();
 				base.header(header, value);
 			}
 		}
 		return base;
 	}
 
 	// Returns response with all headers set
 	public Response getResponse() {
 		ResponseBuilder myrb;
 		XSAMSMetrics metrics = new XSAMSMetrics((XSAMSData) xsamsroot);
 		if (!this.isValid()) {
 			myrb = Response.status(Status.BAD_REQUEST)
 					.entity(new VOTableError(errors))
 					.type("text/xml");
 		} else {
 			if (metrics.isEmpty())
 				myrb = Response.noContent();
 			else {
 				myrb = Response.ok((XSAMSData) xsamsroot);
 				setHeaders(myrb, metrics);
 			}
 		}
 
 		return myrb.build();
 	}
 
 	public ResponseBuilder getHeadResponse(Map<HeaderMetrics, Integer> metrics) {
 		ResponseBuilder myrb;
 		if (!this.isValid()) {
 			myrb = Response.status(Status.BAD_REQUEST);
 		} else {
 			if (metrics.isEmpty())
 				myrb = Response.noContent();
 			else {
 				myrb = Response.ok();
 				myrb = setEstimateHeaders(myrb, metrics);
 			}
 		}
 
 		return myrb;
 	}
 
 	@Override
 	public Query getQuery() {
 		return query;
 	}
 
 }
