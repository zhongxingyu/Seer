 /**
  * This file is part of Waarp Project.
  * 
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
  * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
  * 
  * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
  * 
  * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Waarp . If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.waarp.gateway.kernel;
 
 import java.net.SocketAddress;
 import java.util.LinkedHashMap;
 
 import org.jboss.netty.handler.codec.http.multipart.FileUpload;
 import org.waarp.common.exception.FileTransferException;
 import org.waarp.common.exception.InvalidArgumentException;
 import org.waarp.common.logging.WaarpInternalLogger;
 import org.waarp.common.logging.WaarpInternalLoggerFactory;
 import org.waarp.common.utility.WaarpStringUtils;
 import org.waarp.gateway.kernel.AbstractHttpField.FieldPosition;
 
 /**
  * @author Frederic Bregier
  * 
  */
 public class HttpPage {
 	/**
 	 * Internal Logger
 	 */
 	private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
 			.getLogger(HttpPage.class);
 
 	public static enum PageRole {
 		HTML, MENU, GETDOWNLOAD, POST, PUT, POSTUPLOAD, DELETE, ERROR;
 	}
 
 	/*
 	 * pagename, fileform, header, footer, beginform, endform, nextinform, uri, pagerole, errorpage,
 	 * classname, fields
 	 */
 	public String pagename;
 	public String fileform;
 	public String header;
 	public String footer;
 	public String beginform;
 	public String endform;
 	public String nextinform;
 	public String uri;
 	public PageRole pagerole;
 	public String errorpage;
 	public String classname;
 	public LinkedHashMap<String, AbstractHttpField> fields;
 	public HttpBusinessFactory httpBusinessFactory;
 
 	/**
 	 * 
 	 * @param pagename
 	 * @param fileform
 	 * @param header
 	 * @param footer
 	 * @param beginform
 	 * @param endform
 	 * @param nextinform
 	 * @param uri
 	 * @param pagerole
 	 * @param errorpage
 	 * @param classname
 	 * @param fields
 	 * @throws ClassNotFoundException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 */
 	public HttpPage(String pagename, String fileform, String header, String footer,
 			String beginform, String endform, String nextinform,
 			String uri, PageRole pagerole, String errorpage,
 			String classname, LinkedHashMap<String, AbstractHttpField> fields)
 			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
 		this.pagename = pagename;
 		this.fileform = fileform;
 		if (this.fileform != null && this.fileform.length() == 0) {
 			this.fileform = null;
 		}
 		this.header = header;
 		this.footer = footer;
 		this.beginform = beginform;
 		this.endform = endform;
 		this.nextinform = nextinform;
 		this.uri = uri;
 		this.pagerole = pagerole;
 		this.errorpage = errorpage;
 		this.classname = classname;
 		this.fields = fields;
 		@SuppressWarnings("unchecked")
 		Class<HttpBusinessFactory> clasz = (Class<HttpBusinessFactory>) Class.forName(classname);
 		this.httpBusinessFactory = clasz.newInstance();
 	}
 
 	/**
 	 * Called at the beginning of every request to get the current HttpBusinessFactory to use.
 	 * 
 	 * @param remoteAddress
 	 *            the remote socket address in use
	 * @param reference
	 *            the associated reference object used for that URI
 	 * @return AbstractHttpBusinessRequest to use during the request
 	 */
 	public AbstractHttpBusinessRequest newRequest(SocketAddress remoteAddress) {
 		LinkedHashMap<String, AbstractHttpField> linkedHashMap = new LinkedHashMap<String, AbstractHttpField>(
 				this.fields.size());
 		for (AbstractHttpField field : this.fields.values()) {
 			AbstractHttpField newfield = field.clone();
 			if (pagerole != PageRole.MENU) {
 				newfield.fieldvalue = null;
 			}
 			linkedHashMap.put(field.fieldname, newfield);
 		}
 		return this.httpBusinessFactory.getNewHttpBusinessRequest(remoteAddress, linkedHashMap,
 				this);
 	}
 
 	public String getPageValue(String value) {
 		if (this.fileform != null && value != null) {
 			try {
 				return WaarpStringUtils.readFileException(fileform + value);
 			} catch (InvalidArgumentException e) {
 			} catch (FileTransferException e) {
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * 
 	 * @param reference
 	 * @return the Html results for all pages except Get (result of a download)
 	 * @throws HttpIncorrectRequestException
 	 */
 	public String getHtmlPage(AbstractHttpBusinessRequest reference)
 			throws HttpIncorrectRequestException {
 		if (this.pagerole == PageRole.HTML) {
 			// No handling of variable management, use MENU instead
 			String value = reference.getHeader();
 			logger.debug("Debug: " + (value != null));
 			if (value == null || value.length() == 0) {
 				value = getPageValue(this.header);
 			}
 			StringBuilder builder = null;
 			if (value == null) {
 				builder = new StringBuilder();
 			} else {
 				builder = new StringBuilder(value);
 			}
 			value = reference.getBeginForm();
 			if (value == null || value.length() == 0) {
 				value = getPageValue(this.beginform);
 			}
 			if (value != null) {
 				builder.append(value);
 			}
 			value = reference.getEndForm();
 			if (value == null || value.length() == 0) {
 				value = getPageValue(this.endform);
 			}
 			if (value != null) {
 				builder.append(value);
 			}
 			value = reference.getFooter();
 			if (value == null || value.length() == 0) {
 				value = getPageValue(this.footer);
 			}
 			if (value != null) {
 				builder.append(value);
 			}
 			return builder.toString();
 		}
 		boolean isForm = reference.isForm();
 		String value = reference.getHeader();
 		if (value == null || value.length() == 0) {
 			value = getPageValue(this.header);
 		}
 		StringBuilder builder = null;
 		if (value == null) {
 			builder = new StringBuilder();
 		} else {
 			builder = new StringBuilder(value);
 		}
 		LinkedHashMap<String, AbstractHttpField> requestFields = reference
 				.getLinkedHashMapHttpFields();
 		if (!isForm) {
 			value = reference.getBeginForm();
 			if (value == null || value.length() == 0) {
 				value = getPageValue(this.beginform);
 			}
 			if (value != null) {
 				builder.append(value);
 			} else {
 				builder.append("<BR><TABLE border=1><TR>");
 				for (AbstractHttpField field : requestFields.values()) {
 					if (field.fieldvisibility) {
 						builder.append("<TH>");
 						builder.append(field.fieldinfo);
 						builder.append("</TH>");
 					}
 				}
 				builder.append("<TR>");
 			}
 		} else {
 			value = reference.getBeginForm();
 			if (value == null || value.length() == 0) {
 				value = getPageValue(this.beginform);
 			}
 			if (value != null) {
 				builder.append(value);
 			}
 		}
 		boolean first = true;
 		for (AbstractHttpField field : requestFields.values()) {
 			if (field.fieldvisibility) {
 				// to prevent that last will have a next field form
 				if (first) {
 					first = false;
 				} else {
 					value = reference.getNextFieldInForm();
 					if (value == null || value.length() == 0) {
 						value = getPageValue(this.nextinform);
 					}
 					if (value != null) {
 						builder.append(value);
 					}
 				}
 				value = reference.getFieldForm(field);
 				if (value == null || value.length() == 0) {
 					if (isForm) {
 						value = field.getHtmlFormField(this);
 					} else {
 						value = field.getHtmlTabField(this);
 					}
 				}
 				if (value != null) {
 					builder.append(value);
 				}
 			}
 		}
 		if (!isForm) {
 			value = reference.getEndForm();
 			if (value == null || value.length() == 0) {
 				value = getPageValue(this.endform);
 			}
 			if (value != null) {
 				builder.append(value);
 			} else {
 				builder.append("</TABLE><BR>");
 			}
 		} else {
 			value = reference.getEndForm();
 			if (value == null || value.length() == 0) {
 				value = getPageValue(this.endform);
 			}
 			if (value != null) {
 				builder.append(value);
 			}
 		}
 		value = reference.getFooter();
 		if (value == null || value.length() == 0) {
 			value = getPageValue(this.footer);
 		}
 		if (value != null) {
 			builder.append(value);
 		}
 		return builder.toString();
 	}
 
 	/**
 	 * Set the value to the field according to fieldname.
 	 * 
 	 * If the field is not registered, the field is ignored.
 	 * 
 	 * @param reference
 	 * @param fieldname
 	 * @param value
 	 * @param position
 	 * @throws HttpIncorrectRequestException
 	 */
 	public void setValue(AbstractHttpBusinessRequest reference, String fieldname, String value,
 			FieldPosition position)
 			throws HttpIncorrectRequestException {
 		LinkedHashMap<String, AbstractHttpField> requestFields = reference
 				.getLinkedHashMapHttpFields();
 		AbstractHttpField field = requestFields.get(fieldname);
 		if (field != null) {
 			if (field.fieldposition == FieldPosition.ANY || field.fieldposition == position) {
 				field.setStringValue(value);
 				if (field.fieldtovalidate) {
 					if (!reference.isFieldValid(field)) {
 						throw new HttpIncorrectRequestException("Field unvalid: " + fieldname);
 					}
 				}
 			} else {
 				throw new HttpIncorrectRequestException("Invalid position: " + position +
 						" while field is supposed to be in " + field.fieldposition);
 			}
 		}
 	}
 
 	/**
 	 * Set the value to the field according to fieldname.
 	 * 
 	 * If the field is not registered, the field is ignored.
 	 * 
 	 * @param reference
 	 * @param fieldname
	 * @param value
 	 * @throws HttpIncorrectRequestException
 	 */
 	public void setValue(AbstractHttpBusinessRequest reference, String fieldname,
 			FileUpload fileUpload)
 			throws HttpIncorrectRequestException {
 		LinkedHashMap<String, AbstractHttpField> requestFields = reference
 				.getLinkedHashMapHttpFields();
 		AbstractHttpField field = requestFields.get(fieldname);
 		if (field != null) {
 			field.setFileUpload(fileUpload);
 			if (field.fieldtovalidate) {
 				if (!reference.isFieldValid(field)) {
 					throw new HttpIncorrectRequestException("Field unvalid: " + fieldname);
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param reference
 	 * @return True if the request is fully valid
 	 */
 	public boolean isRequestValid(AbstractHttpBusinessRequest reference) {
 		LinkedHashMap<String, AbstractHttpField> requestFields = reference
 				.getLinkedHashMapHttpFields();
 		for (AbstractHttpField field : requestFields.values()) {
 			if (field.fieldmandatory && !field.present) {
 				logger.warn("Request invalid since the following field is absent: "
 						+ field.fieldname);
 				return false;
 			}
 		}
 		return reference.isRequestValid();
 	}
 
 	/**
 	 * Convenient method to get the fields list
 	 * 
 	 * @param reference
 	 * @return the fields list from the current AbstractHttpBusinessRequest
 	 */
 	public LinkedHashMap<String, AbstractHttpField> getFieldsForRequest(
 			AbstractHttpBusinessRequest reference) {
 		return reference.getLinkedHashMapHttpFields();
 	}
 
 	/**
 	 * Convenient method to get the value of one field
 	 * 
 	 * @param reference
 	 * @param fieldname
 	 * @return the String value
 	 */
 	public String getValue(AbstractHttpBusinessRequest reference, String fieldname) {
 		AbstractHttpField field = reference.getLinkedHashMapHttpFields().get(fieldname);
 		if (field != null) {
 			return field.fieldvalue;
 		}
 		return null;
 	}
 
 	/**
 	 * Convenient method to get the value of one field
 	 * 
 	 * @param reference
 	 * @param fieldname
 	 * @return the FileUpload value
 	 */
 	public FileUpload getFileUpload(AbstractHttpBusinessRequest reference, String fieldname) {
 		AbstractHttpField field = reference.getLinkedHashMapHttpFields().get(fieldname);
 		if (field != null) {
 			return field.fileUpload;
 		}
 		return null;
 	}
 
 	/**
 	 * Convenient method to get one field
 	 * 
 	 * @param reference
 	 * @param fieldname
 	 * @return the AbstractHttpField value
 	 */
 	public AbstractHttpField getField(AbstractHttpBusinessRequest reference, String fieldname) {
 		return reference.getLinkedHashMapHttpFields().get(fieldname);
 	}
 }
