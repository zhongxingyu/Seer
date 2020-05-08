 package com.sapienter.jbilling.client.order;
 
 import java.rmi.RemoteException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 
 import com.sapienter.jbilling.client.util.Constants;
 import com.sapienter.jbilling.client.util.CrudActionBase;
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.interfaces.ItemSession;
 import com.sapienter.jbilling.interfaces.ItemSessionHome;
 import com.sapienter.jbilling.interfaces.NewOrderSession;
 import com.sapienter.jbilling.interfaces.OrderSession;
 import com.sapienter.jbilling.interfaces.OrderSessionHome;
 import com.sapienter.jbilling.server.item.PromotionDTOEx;
 import com.sapienter.jbilling.server.order.NewOrderDTO;
 import com.sapienter.jbilling.server.order.OrderDTOEx;
 import com.sapienter.jbilling.server.order.OrderPeriodDTOEx;
 import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
 
 public class OrderCrudAction extends CrudActionBase<NewOrderDTO> {
 	private static final String FORM_ORDER = "order";
 
 	private static final String FORWARD_EDIT = "order_edit";
 	private static final String FORWARD_ITEMS = "order_items";
 
 	private static final String FIELD_PROMO_CODE = "promotion_code";
 	private static final String FIELD_BILLING_TYPE = "billingType";
 	private static final String FIELD_ANTICIPATE_PERIODS = "anticipate_periods";
 	private static final String FIELD_NOTES = "notes";
 	private static final String FIELD_ADD_NOTES_IN_INVOICE = "chbx_notes";
 	private static final String FIELD_OWN_INVOICE = "chbx_own_invoice";
 	private static final String FIELD_DF_FM = "chbx_df_fm";
 	private static final String FIELD_DUE_DATE_VALUE = "due_date_value";
 	private static final String FIELD_DUE_DATE_UNIT_ID = "due_date_unit_id";
 	private static final String FIELD_GROUP_NEXT_BILLABLE = "next_billable";
 	private static final String FIELD_GROUP_UNTIL = "until";
 	private static final String FIELD_GROUP_SINCE = "since";
 	private static final String FIELD_NOTIFY = "chbx_notify";
 	private static final String FIELD_PERIOD = "period";
 
 	private final NewOrderSession myNewOrderSession;
 	
 	public OrderCrudAction(NewOrderSession newOrderSession){
 		super(FORM_ORDER, "order");
 		myNewOrderSession = newOrderSession;
 		LOG = Logger.getLogger(OrderCrudAction.class);
 	}
 	
 	@Override
 	protected ForwardAndMessage doSetup() throws RemoteException {
         OrderDTOEx dto = (OrderDTOEx) session.getAttribute(
                 Constants.SESSION_ORDER_DTO);
         
         myForm.set(FIELD_PERIOD, dto.getPeriodId());
         myForm.set(FIELD_NOTIFY, isIntegerTrue(dto.getNotify()));
         setFormDate(FIELD_GROUP_SINCE, dto.getActiveSince());
         setFormDate(FIELD_GROUP_UNTIL, dto.getActiveUntil());
         setFormDate(FIELD_GROUP_NEXT_BILLABLE, dto.getNextBillableDay());
         myForm.set(FIELD_DUE_DATE_UNIT_ID, dto.getDueDateUnitId());
         myForm.set(FIELD_DUE_DATE_VALUE, getStringOrNull(dto.getDueDateValue()));
         myForm.set(FIELD_DF_FM, isIntegerTrue(dto.getDfFm()));
         myForm.set(FIELD_OWN_INVOICE, isIntegerTrue(dto.getOwnInvoice()));
         myForm.set(FIELD_ADD_NOTES_IN_INVOICE, isIntegerTrue(dto.getNotesInInvoice()));
         myForm.set(FIELD_NOTES, dto.getNotes());
         myForm.set(FIELD_ANTICIPATE_PERIODS, getStringOrNull(dto.getAnticipatePeriods()));
         myForm.set(FIELD_BILLING_TYPE, dto.getBillingTypeId());
         if (dto.getPromoCode() != null) {
             myForm.set(FIELD_PROMO_CODE, dto.getPromoCode());
         }
         
         return new ForwardAndMessage(FORWARD_EDIT);
 	}
 	
 	@Override
 	protected void preEdit() {
 		super.preEdit();
 		setForward(FORWARD_EDIT);
 	}
 	
 	@Override
 	protected NewOrderDTO doEditFormToDTO() throws RemoteException {
         // this is kind of a wierd case. The dto in the session is all
         // it is required to edit.
 
 		NewOrderDTO summary = (NewOrderDTO) session.getAttribute(
                 Constants.SESSION_ORDER_SUMMARY);
         
 		summary.setPeriod((Integer) myForm.get(FIELD_PERIOD));
         summary.setActiveSince(parseDate(FIELD_GROUP_SINCE, "order.prompt.activeSince"));
         summary.setActiveUntil(parseDate(FIELD_GROUP_UNTIL, "order.prompt.activeUntil"));
         summary.setNextBillableDay(parseDate(FIELD_GROUP_NEXT_BILLABLE, "order.prompt.nextBillableDay"));
 
         summary.setBillingTypeId((Integer) myForm.get(FIELD_BILLING_TYPE));
         summary.setPromoCode((String) myForm.get(FIELD_PROMO_CODE));
         
         summary.setNotify(fromCheckBox(FIELD_NOTIFY));
         summary.setDfFm(fromCheckBox(FIELD_DF_FM));
         summary.setOwnInvoice(fromCheckBox(FIELD_OWN_INVOICE));
         summary.setNotesInInvoice(fromCheckBox(FIELD_ADD_NOTES_IN_INVOICE));
         summary.setNotes((String) myForm.get(FIELD_NOTES));
 
         summary.setAnticipatePeriods(getIntegerFieldValue(FIELD_ANTICIPATE_PERIODS));
         summary.setPeriodStr(getOptionDescription(summary.getPeriod(), Constants.PAGE_ORDER_PERIODS));
         summary.setBillingTypeStr(getOptionDescription(summary.getBillingTypeId(), Constants.PAGE_BILLING_TYPE));
         summary.setDueDateUnitId((Integer) myForm.get(FIELD_DUE_DATE_UNIT_ID));
         summary.setDueDateValue(getIntegerFieldValue(FIELD_DUE_DATE_VALUE));
 
         // return any date validation errors to user
         if (!errors.isEmpty()) {
             return null;
         }
         
         // if she wants notification, we need a date of expiration
 		if (isIntegerTrue(summary.getNotify()) && summary.getActiveUntil() == null) {
 			addError("order.error.notifyWithoutDate", "order.prompt.notify");
 			return null;
 		}
         
         // validate the dates if there is a date of expiration
         if (summary.getActiveUntil() != null) {
 
         	Date start = summary.getActiveSince() != null ?
                     summary.getActiveSince() : 
                    Calendar.getInstance().getTime();
             start = Util.truncateDate(start);
             // it has to be grater than the starting date
             if (!summary.getActiveUntil().after(start)) {
             	addError("order.error.dates", "order.prompt.activeUntil");
                 return null;
             }
             
             // only if it is a recurring order
             if (!summary.getPeriod().equals(new Integer(1))) {
                 // the whole period has to be a multiple of the period unit
                 // This is true, until there is support for prorating.
                 JNDILookup EJBFactory = null;
                 OrderSessionHome orderHome;
                 OrderPeriodDTOEx period;
                 try {
                     EJBFactory = JNDILookup.getFactory(false);
                     orderHome = (OrderSessionHome) EJBFactory.lookUpHome(
                             OrderSessionHome.class,
                             OrderSessionHome.JNDI_NAME);
         
                     OrderSession orderSession = orderHome.create();
                     period = orderSession.getPeriod(
                             languageId, summary.getPeriod());
                 } catch (Exception e) {
                     throw new SessionInternalError("Validating date periods", 
                             OrderCrudAction.class, e);
                 }
 
                 GregorianCalendar toTest = new GregorianCalendar();
                 toTest.setTime(start);
                 while (toTest.getTime().before(summary.getActiveUntil())) {
                     toTest.add(MapPeriodToCalendar.map(period.getUnitId()),
                             period.getValue().intValue());
                 }
                 if (!toTest.getTime().equals(summary.getActiveUntil())) {
                     LOG.debug("Fraction of a period:" + toTest.getTime() +
                             " until: " + summary.getActiveUntil());
                     addError("order.error.period");
                     return null;
                 }
             }
         }
 
        // validate next billable day
        OrderDTOEx orderDTO = (OrderDTOEx) session.getAttribute(
                Constants.SESSION_ORDER_DTO);
        
         // if a date was submitted, check that it is >= old date or
         // greater than today if old date is null.
         if (summary.getNextBillableDay() != null) {
             if (orderDTO != null && orderDTO.getNextBillableDay() != null) {
                 if (summary.getNextBillableDay().before(
                         Util.truncateDate(orderDTO.getNextBillableDay()))) {
 
                 	// new date is less than old date
                     addError("order.error.nextBillableDay.hasOldDate");
                     return null;                    
                 }
             } else if (!summary.getNextBillableDay().after(
                     Calendar.getInstance().getTime())) {
                 
             	// old date doesn't exist and new date is not after todays date
                 addError("order.error.nextBillableDay.noOldDate");
                 return null;                    
             }
         } else {
             // else no date was submitted, check that old date isn't null
             if (orderDTO != null && orderDTO.getNextBillableDay() != null) {
                 addError("order.error.nextBillableDay.null");
                 return null;
             }
         }
 
         // now process this promotion if specified
         if (summary.getPromoCode() != null && 
                 summary.getPromoCode().length() > 0) {
             try {
                 JNDILookup EJBFactory = JNDILookup.getFactory(false);
                 ItemSessionHome itemHome =
                         (ItemSessionHome) EJBFactory.lookUpHome(
                         ItemSessionHome.class,
                         ItemSessionHome.JNDI_NAME);
         
                 ItemSession itemSession = itemHome.create();
                 PromotionDTOEx promotion = itemSession.getPromotion(
                         (Integer) session.getAttribute(
                         Constants.SESSION_ENTITY_ID_KEY), 
                         summary.getPromoCode());
                 
                 if (promotion == null) {
                     addError("promotion.error.noExist", "order.prompt.promotion");
                     return null;
                 } 
                 
                 // if this is an update or the promotion hasn't been 
                 // used by the user
                 if (summary.getId() != null || itemSession.
                         promotionIsAvailable(promotion.getId(),
                             summary.getUserId(), 
                             promotion.getCode()).booleanValue()) {
 
                 	summary = myNewOrderSession.addItem(
                             promotion.getItemId(), 1,
                             summary.getUserId(), entityId);
                     
                 	session.setAttribute(Constants.SESSION_ORDER_SUMMARY, 
                             summary);
                 } else {
                     addError("promotion.error.alreadyUsed",
 							"order.prompt.promotion");
                     return null;
                 }                                
                             
             } catch (Exception e) {
             	LOG.warn("Validating promotion", e);
             }
         }
         return summary;
 	}
 	
 	@Override
 	protected ForwardAndMessage doUpdate(NewOrderDTO dto) throws RemoteException {
 		return new ForwardAndMessage(FORWARD_ITEMS);
 	}
 	
 	@Override
 	protected ForwardAndMessage doCreate(NewOrderDTO dto) throws RemoteException {
 		return new ForwardAndMessage(FORWARD_ITEMS);
 	}
 	
 	@Override
 	protected ForwardAndMessage doDelete() throws RemoteException {
 		throw new UnsupportedOperationException(
 				"Direct delete is not available for orders");
 	}
 	
 	@Override
 	protected void resetCachedList() {
 		//
 	}
 	
 	private int fromCheckBox(String fieldName){
 		return asInteger((Boolean) myForm.get(fieldName));
 	}
 	
 	private int asInteger(boolean b){
 		return b ? 1 : 0;
 	}
 
 	private boolean isIntegerTrue(Integer integer){
 		return integer != null && integer.intValue() == 1;
 	}
 	
 	private String getStringOrNull(Integer integer){
 		return integer == null ? null : integer.toString();
 	}
 	
 	private String getOptionDescription(Integer id, String optionType){
 		return getFormHelper().getOptionDescription(id, optionType);
 	}
 	
 	private void addError(String arg0, String arg1){
 		addError(new ActionError(arg0, arg1));
 	}
 	
 	private void addError(String msg){
 		addError(new ActionError(msg));
 	}
 	
 	private void addError(ActionError err){	
 		errors.add(ActionErrors.GLOBAL_ERROR, err);
 	}
 	
 }
