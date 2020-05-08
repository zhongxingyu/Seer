 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.view.response;
 
 import static br.octahedron.cotopaxi.view.TemplatesAttributes.EXCEPTION_ATTRIBUTE;
 import static br.octahedron.cotopaxi.view.TemplatesAttributes.EXCEPTION_CLASS_ATTRIBUTE;
 import static br.octahedron.cotopaxi.view.TemplatesAttributes.EXCEPTION_MESSAGE_ATTRIBUTE;
 import static br.octahedron.cotopaxi.view.TemplatesAttributes.EXCEPTION_STACK_TRACE_ATTRIBUTE;
 import static br.octahedron.cotopaxi.view.TemplatesAttributes.INVALIDATION_FIELDS_ATTRIBUTE;
 import static br.octahedron.cotopaxi.view.TemplatesAttributes.URL_NOT_FOUND_ATTRIBUTE;
 import static br.octahedron.cotopaxi.view.TemplatesAttributes.URL_NOT_FOUND_METHOD_ATTRIBUTE;
 
import java.util.ArrayList;
import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import br.octahedron.cotopaxi.CotopaxiConfigView;
 import br.octahedron.cotopaxi.controller.auth.UserNotAuthorizedException;
 import br.octahedron.cotopaxi.controller.auth.UserNotLoggedException;
 import br.octahedron.cotopaxi.metadata.MetadataHandler;
 import br.octahedron.cotopaxi.metadata.PageNotFoundExeption;
 import br.octahedron.cotopaxi.metadata.annotation.Message.MessageMetadata;
 import br.octahedron.cotopaxi.metadata.annotation.Response.ResponseMetadata;
 import br.octahedron.cotopaxi.model.response.ActionResponse;
 import br.octahedron.cotopaxi.model.response.ExceptionActionResponse;
 import br.octahedron.cotopaxi.model.response.InvalidActionResponse;
 import br.octahedron.cotopaxi.model.response.SuccessActionResponse;
 import br.octahedron.cotopaxi.view.TemplatesAttributes;
 import br.octahedron.cotopaxi.view.formatter.Formatter;
 import br.octahedron.cotopaxi.view.formatter.FormatterBuilder;
 import br.octahedron.cotopaxi.view.formatter.FormatterNotFoundException;
 import br.octahedron.cotopaxi.view.formatter.TemplateFormatter;
 import br.octahedron.cotopaxi.view.formatter.VelocityFormatter;
 import br.octahedron.util.ThreadProperties;
 
 /**
  * A builder for the ViewResponse.
  * 
  * @author Danilo Penna Queiroz - daniloqueiroz@octahedron.com.br
  * 
  */
 public class ViewResponseBuilder {
 
 	private FormatterBuilder builder;
 	private CotopaxiConfigView config;
 
 	public ViewResponseBuilder(CotopaxiConfigView config) {
 		this.config = config;
 		this.builder = new FormatterBuilder(config);
 	}
 
 	/**
 	 * Creates a {@link ViewResponse}
 	 */
 	public ViewResponse getViewResponse(String redirectURL) {
 		return new RedirectViewResponse(redirectURL);
 	}
 
 	/**
 	 * Creates a {@link ViewResponse}
 	 * 
 	 * It will check the exception and create an appropriate {@link ViewResponse}.
 	 */
 	public ViewResponse getViewResponse(Locale lc, Exception ex) {
 		if (ex instanceof UserNotLoggedException) {
 			// if it's a UserNotLoggedException, create a RedirectViewResponse
 			return this.getViewResponse(((UserNotLoggedException) ex).getRedirectURL());
 		} else if (ex instanceof UserNotAuthorizedException) {
 			return this.createForbiddenResponse(lc, ex);
 		} else if (ex instanceof PageNotFoundExeption) {
 			return this.createPageNotFoundResponse(lc, (PageNotFoundExeption) ex);
 		} else {
 			return this.createErrorResponse(lc, ex);
 		}
 	}
 
 	/**
 	 * Creates a {@link ViewResponse}
 	 */
 	public ViewResponse getViewResponse(Locale lc, String format, ActionResponse actionResponse, MetadataHandler metadata)
 			throws FormatterNotFoundException {
 		Map<String, Object> attributes;
 		Formatter fmt;
 		switch (actionResponse.getResult()) {
 		// TODO refactor
 		case SUCCESS:
 			// check if is a redirect
 			// if not
 			SuccessActionResponse sar = (SuccessActionResponse) actionResponse;
 			attributes = this.getResponseAtts(sar, metadata.getResponseMetadata(), metadata.getMessageMetadata());
 			// get and prepare formatter
 			fmt = this.getFormatter(format, metadata.getResponseMetadata());
 			fmt.setLocale(lc);
 			fmt.setAttributes(attributes);
 			if (!fmt.isReady()) {
 				// hum... is it not ready yet? I guess that it's a template formatter! ;-)
 				((TemplateFormatter) fmt).setTemplate(metadata.getTemplateMetadata().getOnSuccess());
 			}
 			// return formatter view
 			return new FormatterViewResponse(fmt, ResultCode.SUCCESS);
 		case VALIDATION_FAILED:
 			// check if is a redirect
 			// if not
 			InvalidActionResponse iar = (InvalidActionResponse) actionResponse;
 			attributes = this.getResponseAtts(iar, metadata.getResponseMetadata(), metadata.getMessageMetadata());
 			// get and prepare formatter
 			fmt = this.getFormatter(format, metadata.getResponseMetadata());
 			fmt.setLocale(lc);
 			fmt.setAttributes(attributes);
 			if (!fmt.isReady()) {
 				// hum... is it not ready yet? I guess that it's a template formatter! ;-)
 				((TemplateFormatter) fmt).setTemplate(metadata.getTemplateMetadata().getOnValidationFail());
 			}
 			// return formatter view
 			return new FormatterViewResponse(fmt, ResultCode.BAD_REQUEST);
 		case EXCEPTION:
 			// check if is a redirect
 			// if not
 			ExceptionActionResponse ear = (ExceptionActionResponse) actionResponse;
 			attributes = this.getResponseAtts(ear, metadata.getResponseMetadata(), metadata.getMessageMetadata());
 			// get and prepare formatter
 			fmt = this.getFormatter(format, metadata.getResponseMetadata());
 			fmt.setLocale(lc);
 			fmt.setAttributes(attributes);
 			if (!fmt.isReady()) {
 				// hum... is it not ready yet? I guess that it's a template formatter! ;-)
 				((TemplateFormatter) fmt).setTemplate(metadata.getTemplateMetadata().getOnError());
 			}
 			// return formatter view
 			return new FormatterViewResponse(fmt, ResultCode.INTERNAL_ERROR);
 		default:
 			// unreachable code, just here due compilation error
 			return null;
 		}
 	}
 
 	/**
 	 * Extracts the attributes from the {@link ActionResponse}
 	 * 
 	 * @param messageMetadata
 	 */
 	private Map<String, Object> getResponseAtts(SuccessActionResponse actionResponse, ResponseMetadata responseMetadata,
 			MessageMetadata messageMetadata) {
 		// add return value to attributes map
 		String returnName = responseMetadata.getReturnName();
 		Object result = actionResponse.getReturnValue();
 		Map<String, Object> attributes = actionResponse.getAttributes();
 		attributes.put(returnName, result);
 		// check message
 		if (messageMetadata.getOnSuccess() != null) {
 			attributes.put(TemplatesAttributes.MESSAGE_ON_SUCCESS.getAttributeKey(), messageMetadata.getOnSuccess());
 		}
 
 		// store on session if necessary
 		// TODO should it be done here?
 		if (responseMetadata.isStoreOnSession()) {
 			ThreadProperties.setProperty(returnName, result);
 		}
 		// return
 		return attributes;
 	}
 
 	/**
 	 * Extracts the attributes from the {@link ActionResponse}
 	 */
 	private Map<String, Object> getResponseAtts(InvalidActionResponse actionResponse, ResponseMetadata responseMetadata,
 			MessageMetadata messageMetadata) {
 		Map<String, Object> attributes = actionResponse.getAttributes();
 		attributes.put(INVALIDATION_FIELDS_ATTRIBUTE.getAttributeKey(), actionResponse.getInvalidAttributes());
 		// check Message
 		if (messageMetadata.getOnValidationFail() != null) {
 			attributes.put(TemplatesAttributes.MESSAGE_ON_VALIDATION_FAILS.getAttributeKey(), messageMetadata.getOnValidationFail());
 		}
 
 		return attributes;
 	}
 
 	/**
 	 * Extracts the attributes from the {@link ActionResponse}
 	 */
 	private Map<String, Object> getResponseAtts(ExceptionActionResponse actionResponse, ResponseMetadata responseMetadata,
 			MessageMetadata messageMetadata) {
 		// generate attributes map to be rendered
 		Throwable ex = actionResponse.getCause();
 		Map<String, Object> attributes = actionResponse.getAttributes();
 		extractExceptionAttributes(attributes, ex);
 		// check Message
 		if (messageMetadata.getOnError() != null) {
 			attributes.put(TemplatesAttributes.MESSAGE_ON_ERROR.getAttributeKey(), messageMetadata.getOnError());
 		}
 
 		return attributes;
 	}
 
 	/**
 	 * Gets a formatter for the given format
 	 */
 	private Formatter getFormatter(String format, ResponseMetadata responseMetadata) throws FormatterNotFoundException {
 		if (format == null) {
 			format = responseMetadata.getFormats()[0];
 		}
 		return this.builder.getFormatter(format);
 	}
 
 	/**
 	 * Creates an appropriate {@link ViewResponse} for a {@link PageNotFoundExeption}.
 	 */
 	private ViewResponse createPageNotFoundResponse(Locale lc, PageNotFoundExeption pnfex) {
 		// generate attributes map to be rendered
 		Map<String, Object> atts = new HashMap<String, Object>();
 		atts.put(URL_NOT_FOUND_ATTRIBUTE.getAttributeKey(), pnfex.getUrl());
 		atts.put(URL_NOT_FOUND_METHOD_ATTRIBUTE.getAttributeKey(), pnfex.getHttpMethod().toString());
 		// get formatter
 		TemplateFormatter formatter = new VelocityFormatter(this.config.getNotFoundTemplate(), atts, lc);
 		return new FormatterViewResponse(formatter, ResultCode.NOT_FOUND);
 	}
 
 	/**
 	 * Creates an appropriate {@link ViewResponse} for an unknown exception.
 	 */
 	private ViewResponse createErrorResponse(Locale lc, Exception ex) {
 		// generate attributes map to be rendered
 		Map<String, Object> attributes = new HashMap<String, Object>();
 		extractExceptionAttributes(attributes, ex);
 		// get and prepare formatter
 		TemplateFormatter formatter = new VelocityFormatter(this.config.getErrorTemplate(), attributes, lc);
 		return new FormatterViewResponse(formatter, ResultCode.INTERNAL_ERROR);
 	}
 
 	private ViewResponse createForbiddenResponse(Locale lc, Exception ex) {
 		// generate attributes map to be rendered
 		Map<String, Object> attributes = new HashMap<String, Object>();
 		extractExceptionAttributes(attributes, ex);
 		// get and prepare formatter
 		TemplateFormatter formatter = new VelocityFormatter(this.config.getForbiddenTemplate(), attributes, lc);
 		return new FormatterViewResponse(formatter, ResultCode.FORBIDDEN);
 	}
 
 	private void extractExceptionAttributes(Map<String, Object> attributes, Throwable ex) {
 		attributes.put(EXCEPTION_ATTRIBUTE.getAttributeKey(), ex);
 		attributes.put(EXCEPTION_CLASS_ATTRIBUTE.getAttributeKey(), ex.getClass().getName());
		attributes.put(EXCEPTION_STACK_TRACE_ATTRIBUTE.getAttributeKey(), extractStackTrace(ex.getStackTrace()));
 		// just to avoid null messages
 		if ( ex.getMessage() != null) { 
 			attributes.put(EXCEPTION_MESSAGE_ATTRIBUTE.getAttributeKey(), ex.getMessage());
 		} else {
 			attributes.put(EXCEPTION_MESSAGE_ATTRIBUTE.getAttributeKey(), "Null");
 		}
 	}

	private Collection<String> extractStackTrace(StackTraceElement[] stackTrace) {
		ArrayList<String> result = new ArrayList<String>();
		for(StackTraceElement element : stackTrace) {
			result.add(element.toString());
		}
		return result;
	}
 }
