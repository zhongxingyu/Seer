 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.util;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Vector;
 
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.item.CurrencyBL;
 import com.sapienter.jbilling.server.payment.blacklist.BlacklistBL;
 import com.sapienter.jbilling.server.report.Field;
 import com.sapienter.jbilling.server.report.ReportDTOEx;
 import com.sapienter.jbilling.server.report.db.ReportDAS;
 import com.sapienter.jbilling.server.report.db.ReportDTO;
 import com.sapienter.jbilling.server.report.db.ReportFieldDTO;
 import com.sapienter.jbilling.server.report.db.ReportTypeDTO;
 import com.sapienter.jbilling.server.report.db.ReportUserDTO;
 import com.sapienter.jbilling.server.user.AchBL;
 import com.sapienter.jbilling.server.user.EntityBL;
 import com.sapienter.jbilling.server.user.MenuOption;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.db.AchDTO;
 import com.sapienter.jbilling.server.user.db.UserDAS;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.user.partner.PartnerBL;
 import com.sapienter.jbilling.server.user.permisson.db.RoleDTO;
 import com.sapienter.jbilling.server.util.db.LanguageDAS;
 import com.sapienter.jbilling.server.util.db.LanguageDTO;
 import com.sapienter.jbilling.server.util.db.MenuOptionDAS;
 import com.sapienter.jbilling.server.util.db.MenuOptionDTO;
 
 /**
  * 
  * This is the class to provide initialization of DTOs with the entities values.
  * It can't be instantiated, all the methods are static
  * 
  * @author Emil
  */
 
 /*
  * This code can't be testest through a JUnit test case because it will start
  * using local interfaces ... from a remote client ;)
  */
 public class DTOFactory {
 
 	private static final Logger LOG = Logger.getLogger(DTOFactory.class);
 
 	/**
 	 * The constructor is private, do it doesn't get instantiated. All the
 	 * methods then are static.
 	 */
 	private DTOFactory() {
 	}
 
 	/**
 	 * Get's an entity bean of the user, using the username, and then creates a
 	 * DTO with that data. It is not setting permissions or menu
 	 * 
 	 * @param username
 	 * @return UserDTO
 	 * @throws HomeFactoryException
 	 * @throws NamingException
 	 */
 	public static UserDTOEx getUserDTO(String username, Integer entityId)
 			throws NamingException, SessionInternalError {
 
 		LOG.debug("getting the user " + username);
 
 		UserDTO user = new UserDAS().findByUserName(username, entityId);
 		if (user == null)
 			return null;
 		return getUserDTOEx(user);
 	}
 
 	public static UserDTOEx getUserDTOEx(Integer userId)
 			throws NamingException, SessionInternalError {
 
 		LOG.debug("getting the user " + userId);
 
 		UserDTO user = new UserDAS().find(userId);
 		return getUserDTOEx(user);
 	}
 
 	public static UserDTOEx getUserDTOEx(UserDTO user) throws NamingException,
 			SessionInternalError {
 		UserDTOEx dto = new UserDTOEx(user);
 
 		// get the status
 		dto.setStatusId(user.getStatus().getId());
 		dto.setStatusStr(user.getStatus().getDescription(
 				user.getLanguageIdField()));
 		// the subscriber status
 		dto.setSubscriptionStatusId(user.getSubscriberStatus().getId());
 		dto.setSubscriptionStatusStr(user.getSubscriberStatus().getDescription(
 				user.getLanguageIdField()));
 
 		// add the roles
 		Integer mainRole = new Integer(1000);
 		String roleStr = null;
 		dto.getRoles().addAll(user.getRoles());
 		for (RoleDTO role : user.getRoles()) {
 			// the main role is the smallest of them, so they have to be ordered
 			// in the
 			// db in ascending order (small = important);
 			if (role.getId() < mainRole) {
 				mainRole = role.getId();
 				roleStr = role.getTitle(user.getLanguageIdField());
 			}
 		}
 		dto.setMainRoleId(mainRole);
 		dto.setMainRoleStr(roleStr);
 
 		// now get the language
 		LanguageDTO language = new LanguageDAS()
 				.find(user.getLanguageIdField());
 		dto.setLanguageStr(language.getDescription());
 
 		// add the last invoice id
 		InvoiceBL invoiceBL = new InvoiceBL();
 		try {
 			dto.setLastInvoiceId(invoiceBL.getLastByUser(user.getUserId()));
 		} catch (Exception e) {
 			throw new SessionInternalError(e);
 		}
 
 		// make sure the currency is set
 		if (dto.getCurrencyId() == null) {
 			// defaults to the one from the entity
 			dto.setCurrency(user.getEntity().getCurrency());
 		}
 		CurrencyBL currency = new CurrencyBL(dto.getCurrencyId());
 		dto.setCurrencySymbol(currency.getEntity().getSymbol());
 		dto.setCurrencyName(currency.getEntity().getDescription(
 				user.getLanguageIdField()));
 
 		// add a credit card if available
 		if (!user.getCreditCards().isEmpty()) {
 			dto.setCreditCard(user.getCreditCards().iterator().next());
 		}
 
 		if (!user.getAchs().isEmpty()) {
 			AchBL ach = new AchBL(((AchDTO)user.getAchs().toArray()[0]).getId());
 			dto.setAch(ach.getDTO());
 		}
 
 		// if this is a customer, add its dto
 		if (user.getCustomer() != null) {
 
 			dto.setCustomer(user.getCustomer());
 		}
 
 		// if this is a partner, add its dto
 		if (user.getPartner() != null) {
 			PartnerBL partner = new PartnerBL(user.getPartner());
 			dto.setPartner(partner.getDTO());
 		}
 
 		// the locale will be handy
 		try {
 			UserBL bl = new UserBL(user);
 			dto.setLocale(bl.getLocale());
 		} catch (Exception e) {
 			dto.setLocale(new Locale("en"));
 		}
 
 		// if the blacklist plug-in enabled, add the list of blacklist
 		// entries that match this user and set whether their id is blacklisted
 		if (BlacklistBL.isBlacklistEnabled(user.getCompany().getId())) {
 			dto.setBlacklistMatches(BlacklistBL.getBlacklistMatches(user
 					.getId()));
 			dto.setUserIdBlacklisted(BlacklistBL.isUserIdBlacklisted(user
 					.getId()));
 		}
 
 		return dto;
 	}
 
 	public static ReportDTOEx getReportDTOEx(Integer reportId, Integer entityId)
 			throws SessionInternalError {
 		ReportDAS reportHome = new ReportDAS();
 
 		ReportDTO report = reportHome.find(reportId);
 
 		EntityBL entity = new EntityBL(entityId);
 		ReportDTOEx dto = getReportDTOEx(new ReportDAS().find(reportId), entity
 				.getLocale());
 		Collection fields = report.getReportFields();
 
 		for (Iterator it = fields.iterator(); it.hasNext();) {
 			ReportFieldDTO field = (ReportFieldDTO) it.next();
 			Field fieldDto = getFieldDTO(field);
 			fieldDto.setWhereValue(field.getWhereValue());
 
 			dto.addField(fieldDto);
 		}
 		// the fields come sorted by position and id from the DB
 
 		return dto;
 	}
 
 	public static Field getFieldDTO(ReportFieldDTO field) {
 		Field dto = new Field(field.getTableName(), field.getColumnName(),
 				field.getDataType());
 
 		dto.setFunctionName(field.getFunctionName());
 		dto.setFunctionable(field.getFunctionable());
 		dto.setIsGrouped(field.getIsGrouped());
 		dto.setIsShown(field.getIsShown());
 		dto.setOperatorValue(field.getOperatorValue());
 		dto.setOperatorable(field.getOperatorable());
 		dto.setOrdenable(field.getOrdenable());
 		dto.setOrderPosition(field.getOrderPosition());
 		dto.setPositionNumber(field.getPositionNumber());
 		dto.setSelectable(field.getSelectable());
 		dto.setTitleKey(field.getTitleKey());
 		dto.setWherable(field.getWhereable());
 		dto.setWhereValue(field.getWhereValue());
 
 		return dto;
 	}
 
 	public static ReportDTOEx getReportDTOEx(ReportDTO report, Locale locale) {
 
 		ReportDTOEx dto = new ReportDTOEx(report.getTitleKey(), report
 				.getInstructionsKey(), report.getTablesList(), report
 				.getWhereStr(), locale);
 
 		dto.setIdColumn(report.getIdColumn());
 		dto.setId(report.getId());
 		dto.setLink(report.getLink());
 
 		return dto;
 	}
 
 	public static Collection<ReportDTOEx> reportEJB2DTOEx(
 			Collection<ReportDTO> reports, boolean filter) {
 		Vector<ReportDTOEx> dtos = new Vector<ReportDTOEx>();
 
 		for (ReportDTO report : reports) {
 
 			if (filter) {
 				for (ReportTypeDTO type : report.getReportTypes()) {
 					if (type.getShowable() == 1) {
 						dtos.add(getReportDTOEx(report, null));
 						break;
 					}
 				}
 			} else {
 				dtos.add(getReportDTOEx(report, null));
 			}
 
 		}
 
 		return dtos;
 	}
 
 	public static Collection reportEJB2DTO(Collection<ReportDTO> reports) {
 		Vector dtos = new Vector();
 
 		for (ReportDTO report : reports) {
 			dtos.add(getReportDTOEx(report, null));
 		}
 
 		return dtos;
 	}
 
 	public static Collection reportUserEJB2DTO(Collection reports) {
 		Vector dtos = new Vector();
 
 		for (Iterator it = reports.iterator(); it.hasNext();) {
 			ReportUserDTO reportEJB = (ReportUserDTO) it.next();
 			dtos.add(getReportUserDTO(reportEJB));
 		}
 
 		return dtos;
 	}
 
 	public static ReportUserDTO getReportUserDTO(ReportUserDTO rUser) {
 		return new ReportUserDTO(rUser.getId(), rUser.getBaseUser(), rUser
 				.getReport(), rUser.getCreateDatetime(), rUser.getTitle());
 	}
 
 	public static MenuOption getMenuOption(Integer id, Integer languageId)
 			throws NamingException {
 		JNDILookup EJBFactory = JNDILookup.getFactory(false);
 		MenuOptionDAS menuDas = new MenuOptionDAS();
 
 		MenuOptionDTO option = menuDas.find(id);
 		MenuOption opt = new MenuOption();
 		opt.setId(option.getId());
 		opt.setLevelField(option.getLevelField());
 		opt.setLink(option.getLink());
 		opt.setDisplay(option.getDisplay(languageId));
 		// opt.setParentId((option.getParent() == null ? null :
 		// option.getParent().getId()));
 		opt.setParentId((option.getMenuOption() == null ? null : option
 				.getMenuOption().getId()));
 		return opt;
 	}
 
 }
