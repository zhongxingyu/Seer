 /**
  * 
  */
 package org.esupportail.opi.web.controllers.parameters;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import org.esupportail.commons.exceptions.ConfigException;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.BeanUtils;
 import org.esupportail.opi.domain.beans.mails.MailContent;
 import org.esupportail.opi.services.mails.MailContentService;
 import org.esupportail.opi.services.mails.MailContentUtils;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.springframework.util.StringUtils;
 
 
 
 /**
  * @author cleprous
  *
  */
 public class MailContentController extends AbstractContextAwareController {
 
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = 2205491714929696837L;
 
 	/*
 	 ******************* PROPERTIES ******************* */
 	/**
 	 * A logger.
 	 */
 	private final Logger log = new LoggerImpl(getClass());
 	
 	/**
 	 * The action to mailContent.
 	 */
 	private ActionEnum actionEnum;
 	
 	/**
 	 * the mailContent.
 	 */
 	private MailContent mailContent;
 	
 	/**
 	 * see {@link MailContentService}.
 	 */
 	private List<MailContentService> mailContentService;
 	
 	/**
 	 * The attribut selected.
 	 */
 	private String attributSelected;
 
 	
 	/*
 	 ******************* INIT ************************* */
 	
 
 
 	/**
 	 * Constructors.
 	 */
 	public MailContentController() {
 		super();
 	}
 	
 	/** 
 	 * @see org.esupportail.opi.web.controllers.AbstractAccessController#afterPropertiesSetInternal()
 	 */
 	@Override
 	public void afterPropertiesSetInternal() {
 		super.afterPropertiesSetInternal();
 		Map<String, MailContentService> mailContentServices = BeanUtils.getBeansOfClass(MailContentService.class);
 		mailContentService = new ArrayList<MailContentService>();
 		for (Entry<String, MailContentService> nameEntry : mailContentServices.entrySet()) {
 			String name = nameEntry.getKey();
 			if (log.isDebugEnabled()) {
 				log.debug("get to mailContentService bean [" + name + "]...");
 			}
 			Object bean = nameEntry.getValue();
 			if (bean == null) {
 				throw new ConfigException("bean [" + name 
 						+ "] is null, " 
 						+ "application contains not mail.");
 			}
 			if (!(bean instanceof MailContentService)) {
 				throw new ConfigException("bean [" + name 
 						+ "] does not implement MailContentService, " 
 						+ "application contains not mail.");
 			}
 			mailContentService.add((MailContentService) bean);
 			if (log.isDebugEnabled()) {
 				log.debug("bean [" + name + "] added to mailContentService list.");
 			}
 		}
 		reset();
 	}
 
 
 
 	/** 
 	 * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
 	 */
 	@Override
 	public void reset() {
 		mailContent = new MailContent();
 		actionEnum = new ActionEnum();
 		attributSelected = null;
 		super.reset();
 	}
 	
 	
 	
 	
 	/*
 	 ******************* CALLBACK ********************** */
 	
 	/**
 	 * List all mails.
 	 * @return String
 	 */
 	public String goAllMails() {
 		reset();
 		return NavigationRulesConst.MANAGED_MAIL_CONTENT;
 	}
 
 	
 	/**
 	 * List all mails.
 	 * @return String
 	 */
 	public String goUpdate() {
 		return NavigationRulesConst.UPDATE_MAIL_CONTENT;
 	}
 
 
 	
 	
 	/*
 	 ******************* METHODS ********************** */
 	
 	/**
 	 * update mailContent in the dataBase.
 	 * @return String
 	 */
 	public String update() {
 		
 		MailContent m = (MailContent) getDomainService()
 			.update(mailContent, getCurrentGest().getLogin());
 		getParameterService().updateMailContent(m);
 		
 		addInfoMessage(null, "INFO.ENTER.SUCCESS");
 		
 		return goAllMails();
 	}
 	
 	/**
 	 * add mailContent in the dataBase.
 	 * @return String
 	 */
 	public String add() {
 		
 		for (MailContent mail : getMailContents()) {
 			if (mail.getCode().equals(mailContent.getCode())) {
 				addErrorMessage(null, "ERROR.FIELD.NOT_UNIQUE", getString("FIELD_LABEL.CODE"));
 				return null;
 			}
 		}
 		
 		MailContent m = (MailContent) getDomainService()
 			.add(mailContent, getCurrentGest().getLogin());
 		getParameterService().addMailContent(m);
 		
 		addInfoMessage(null, "INFO.ENTER.SUCCESS");
 		
 		return goAllMails();
 	}
 	
 	
 	/**
 	 * The one mailContentService with this code.
 	 * @param code
 	 * @return MailContentService
 	 */
 	public MailContentService getMailService(final String code) {
 		for (MailContentService m : mailContentService) {
 			if (m.getCode().equals(code)) {
 				return m;
 			}
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * @return List of SelectItem
 	 */
 	public List<SelectItem> getAttributes() {
 		List<SelectItem> s = new ArrayList<SelectItem>();
 		MailContentService mailS = getMailService(mailContent.getCode());
 		
 		for (String st : mailS.getDefinitions().keySet()) {
 			String keyBundles = mailS.getDefinitions().get(st);
 			s.add(new SelectItem(st, getString(keyBundles)));
 		}
 		
 		
 		return s;
 	}
 	
 	/**
 	 * @param event
 	 */
 	public void selectAttribut(final ValueChangeEvent event) {
 		attributSelected = (String) event.getNewValue();
 	}
 	
 	
 	/**
 	 * update the mailContent with the default value to body and subject.
 	 */
 	public void initDefaultValue() {
 		for (MailContentService m : mailContentService) {
 			if (m.getCode().equals(mailContent.getCode())) {
 				//on charge les valeurs par defaut
 				mailContent.setBody(m.getDefaultBody());
 				mailContent.setSubject(m.getDefaultSubject());
 			}
 		}
 	}
 	/*
 	 ******************* ACCESSORS ******************** */
 	
 	/**
 	 * @return all mailContent.
 	 */
 	public List<MailContent> getMailContents() {
 		return getParameterService().getMailContents();
 	}
 
 	/**
 	 * @return the mailContent
 	 */
 	public MailContent getMailContent() {
 		return mailContent;
 	}
 
 	/**
 	 * @param mailContent the mailContent to set
 	 */
 	public void setMailContent(final MailContent mailContent) {
 		this.mailContent = mailContent;
 	}
 
 	/**
 	 * @param mailContentService the mailContentService to set
 	 */
 	public void setMailContentService(final List<MailContentService> mailContentService) {
 		this.mailContentService = mailContentService;
 	}
 
 
 	/**
 	 * @return ActionEnum
 	 */
 	public ActionEnum getActionEnum() {
 		return actionEnum;
 	}
 
 	/**
 	 * @param actionEnum
 	 */
 	public void setActionEnum(final ActionEnum actionEnum) {
 		this.actionEnum = actionEnum;
 	}
 
 	/**
 	 * @return the attibutSelected
 	 */
 	public String getAttributSelected() {
 		if (StringUtils.hasText(attributSelected)) {
 			return MailContentUtils.makeExpression(attributSelected);
 		}
 		return null;
 	}
 
 	/**
 	 * @param attibutSelected the attibutSelected to set
 	 */
 	public void setAttributSelected(final String attibutSelected) {
 		this.attributSelected = attibutSelected;
 	}
 }
