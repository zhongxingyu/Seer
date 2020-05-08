 package com.blog.samples.webservices.rest;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 
 import com.blog.samples.domain.Fund;
 import com.blog.samples.services.FundService;
 
 /**
  * FundsController class will expose a series of RESTful endpoints
  */
 @Controller
 public class FundsController {
 
 	@Autowired
 	private FundService fundService_i;
 
 	@Autowired
 	private View jsonView_i;
 
 	private static final String DATA_FIELD = "data";
 	private static final String ERROR_FIELD = "error";
 
 	private static final Logger logger_c = Logger
 			.getLogger(FundsController.class);
 
 	/**
 	 * Gets a fund by fund id.
 	 * 
 	 * @param fundId_p
 	 *            the fund id_p
 	 * @return the fund
 	 */
 	@RequestMapping(value = "/rest/funds/{fundId}", method = RequestMethod.GET)
	public ModelAndView getFund(final @PathVariable("fundId") String fundId_p) {
 		Fund fund = null;
 
 		/* validate fund Id parameter */
 		if (isEmpty(fundId_p) || fundId_p.length() < 5) {
 			final String sMessage = "Error invoking getFund - Invalid fund Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		try {
 			fund = fundService_i.getFundById(fundId_p);
 		} catch (final Exception e) {
 			final String sMessage = "Error invoking getFund. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		logger_c.debug("Returing Fund: " + fund.toString());
 		return new ModelAndView(jsonView_i, DATA_FIELD, fund);
 	}
 
 	/**
 	 * Gets all funds.
 	 * 
 	 * @return the funds
 	 */
 	@RequestMapping(value = "/rest/funds/", method = RequestMethod.GET)
 	public ModelAndView getFunds() {
 		List<Fund> funds = null;
 		logger_c.debug("1");

 		try {
 			funds = fundService_i.getAllFunds();
 		} catch (final Exception e) {
 			final String sMessage = "Error getting all funds. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		logger_c.debug("Returing Funds: " + funds.toString());
 		return new ModelAndView(jsonView_i, DATA_FIELD, funds);
 	}
 
 	/**
 	 * Creates a new fund.
 	 * 
 	 * @param fund_p
 	 *            the fund_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = { "/rest/funds/" }, method = { RequestMethod.POST })
	public ModelAndView createFund(@RequestBody final Fund fund_p,
			final HttpServletResponse httpResponse_p, final WebRequest request_p) {
 
 		Fund createdFund;
 		logger_c.debug("Creating Fund: " + fund_p.toString());
 
 		try {
 			createdFund = fundService_i.createFund(fund_p);
 		} catch (final Exception e) {
 			final String sMessage = "Error creating new fund. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		/* set HTTP response code */
 		httpResponse_p.setStatus(HttpStatus.CREATED.value());
 
 		/* set location of created resource */
 		httpResponse_p.setHeader("Location", request_p.getContextPath()
 				+ "/rest/funds/" + fund_p.getFundId());
 
 		/**
 		 * Return the view
 		 */
 		return new ModelAndView(jsonView_i, DATA_FIELD, createdFund);
 	}
 
 	/**
 	 * Updates fund with given fund id.
 	 * 
 	 * @param fund_p
 	 *            the fund_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = { "/rest/funds/{fundId}" }, method = { RequestMethod.PUT })
	public ModelAndView updateFund(@RequestBody final Fund fund_p,
			final @PathVariable("fundId") String fundId_p,
 			final HttpServletResponse httpResponse_p) {
 
 		logger_c.debug("Updating Fund: " + fund_p.toString());
 
 		/* validate fund Id parameter */
 		if (isEmpty(fundId_p) || fundId_p.length() < 5) {
 			final String sMessage = "Error updating fund - Invalid fund Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		Fund fund = null;
 
 		try {
 			fund = fundService_i.updateFund(fund_p);
 		} catch (final Exception e) {
 			final String sMessage = "Error updating fund. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		httpResponse_p.setStatus(HttpStatus.OK.value());
 		return new ModelAndView(jsonView_i, DATA_FIELD, fund);
 	}
 
 	/**
 	 * Deletes the fund with the given fund id.
 	 * 
 	 * @param fundId_p
 	 *            the fund id_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = "/rest/funds/{fundId}", method = RequestMethod.DELETE)
	public ModelAndView removeFund(
			final @PathVariable("fundId") String fundId_p,
 			final HttpServletResponse httpResponse_p) {
 
 		logger_c.debug("Deleting Fund Id: " + fundId_p.toString());
 
 		/* validate fund Id parameter */
 		if (isEmpty(fundId_p) || fundId_p.length() < 5) {
 			final String sMessage = "Error deleting fund - Invalid fund Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		try {
 			fundService_i.deleteFund(fundId_p);
 		} catch (final Exception e) {
 			final String sMessage = "Error invoking getFunds. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		httpResponse_p.setStatus(HttpStatus.OK.value());
 		return new ModelAndView(jsonView_i, DATA_FIELD, null);
 	}
 
 	public static boolean isEmpty(final String s_p) {
		return null == s_p || s_p.trim().length() == 0;
 	}
 
 	/**
 	 * Create an error REST response.
 	 * 
 	 * @param sMessage
 	 *            the s message
 	 * @return the model and view
 	 */
 	private ModelAndView createErrorResponse(final String sMessage) {
 		return new ModelAndView(jsonView_i, ERROR_FIELD, sMessage);
 	}
 
 	/**
 	 * Injector methods.
 	 * 
 	 * @param fundService_p
 	 *            the new fund service
 	 */
 	public void setFundService(final FundService fundService_p) {
 		fundService_i = fundService_p;
 	}
 
 	/**
 	 * Injector methods.
 	 * 
 	 * @param view
 	 *            the new json view
 	 */
 	public void setJsonView(final View view) {
 		jsonView_i = view;
 	}
 
 }
