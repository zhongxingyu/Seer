 package com.sapienter.jbilling.client.user;
 
 import java.rmi.RemoteException;
 import java.util.HashMap;
 
 import javax.ejb.FinderException;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 
 import com.sapienter.jbilling.client.util.Constants;
 import com.sapienter.jbilling.client.util.CrudActionBase;
 import com.sapienter.jbilling.client.util.PreferencesMap;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.interfaces.UserSession;
 import com.sapienter.jbilling.server.user.ContactDTOEx;
 import com.sapienter.jbilling.server.user.PartnerDTOEx;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 
 public class PartnerCrudAction extends CrudActionBase<PartnerDTOEx> {
 	private static final String FORM = "partner";
 
 	private static final String FIELD_BALANCE = "balance";
 	private static final String FIELD_RATE = "rate";
 	private static final String FIELD_FEE = "fee";
 	private static final String FIELD_FEE_CURRENCY = "fee_currency";
 	private static final String FIELD_ONE_TIME = "chbx_one_time";
 	private static final String FIELD_PERIOD_UNIT_ID = "period_unit_id";
 	private static final String FIELD_PERIOD_VALUE = "period_value";
 	private static final String FIELD_GROUP_PAYOUT = "payout";
 	private static final String FIELD_PROCESS = "chbx_process";
 	private static final String FIELD_CLERK = "clerk";
 	
 	private static final String FORWARD_EDIT = "partner_edit";
 	private static final String FORWARD_LIST = "partner_list";
 	private static final String FORWARD_RANGES = "partner_ranges";
 	private static final String FORWARD_CREATE = "partner_create";
 
 	private static final String MESSAGE_CREATED = "partner.created";
 	private static final String MESSAGE_UPDATED = "partner.updated";
 	
 	private final UserSession myUserSession;
 	
 	public PartnerCrudAction(UserSession userSession){
 		super(FORM, "partner");
 		myUserSession = userSession;
 	}
 	
 	
 	@Override
 	protected ForwardAndMessage doCreate(PartnerDTOEx dto) throws RemoteException {
         // get the user dto from the session. This is the dto with the
         // info of the user to create
         UserDTOEx user = (UserDTOEx) session.getAttribute(Constants.SESSION_CUSTOMER_DTO);
         ContactDTOEx contact = (ContactDTOEx) session.getAttribute(Constants.SESSION_CUSTOMER_CONTACT_DTO);
         // add the partner information just submited to the user to be
         // created
         user.setPartnerDto(dto);
         // make the call
         Integer newUserID = myUserSession.create(user, contact);
         log.debug("Partner created = " + newUserID);
         session.setAttribute(Constants.SESSION_USER_ID, newUserID);
        
        try {
            session.setAttribute(Constants.SESSION_PARTNER_DTO, 
                    myUserSession.getUserDTOEx(newUserID).getPartnerDto());
        } catch (FinderException e) {
            throw new SessionInternalError(e);
        }
         return (request.getParameter("ranges") == null) ?
         		new ForwardAndMessage(FORWARD_LIST, MESSAGE_CREATED) :
         		new ForwardAndMessage(FORWARD_RANGES, MESSAGE_CREATED);
 	}
 	
 	@Override
 	protected ForwardAndMessage doDelete() throws RemoteException {
 		throw new UnsupportedOperationException("Delete mode is not directly supported for partners");
 	}
 
 	@Override
 	protected void preEdit() {
 		super.preEdit();
 		setForward(FORWARD_EDIT);
 	}
 	
 	@Override
 	protected PartnerDTOEx doEditFormToDTO() throws RemoteException {
 		PartnerDTOEx dto = new PartnerDTOEx();
         dto.setBalance(string2float((String) myForm.get(FIELD_BALANCE)));
         dto.setPercentageRate(string2float((String) myForm.get(FIELD_RATE)));
         dto.setReferralFee(string2float((String) myForm.get(FIELD_FEE)));
         if (dto.getReferralFee() != null) {
             dto.setFeeCurrencyId((Integer) myForm.get(FIELD_FEE_CURRENCY));
         }
         dto.setPeriodUnitId((Integer) myForm.get(FIELD_PERIOD_UNIT_ID));
         dto.setPeriodValue(Integer.valueOf((String) myForm.get(FIELD_PERIOD_VALUE)));
         dto.setNextPayoutDate(parseDate(FIELD_GROUP_PAYOUT, "partner.prompt.nextPayout"));
         
         dto.setAutomaticProcess(valueOfCheckBox(FIELD_PROCESS));
         dto.setOneTime(valueOfCheckBox(FIELD_ONE_TIME));
 
         try {
             Integer clerkId = Integer.valueOf((String) myForm.get(FIELD_CLERK));
             UserDTOEx clerk = getUser(clerkId);
             if (!entityId.equals(clerk.getEntityId()) || 
                     clerk.getDeleted().intValue() == 1 ||
                     clerk.getMainRoleId().intValue() > 
                         Constants.TYPE_CLERK.intValue()) {
             	
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("partner.error.clerkinvalid"));
             } else {
                 dto.setRelatedClerkUserId(clerkId);
             }
         } catch (FinderException e) {
             errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("partner.error.clerknotfound"));
         }
         return dto;
 	}
 	
 	@Override
 	protected ForwardAndMessage doUpdate(PartnerDTOEx dto) throws RemoteException {
         dto.setId((Integer) session.getAttribute(Constants.SESSION_PARTNER_ID));
         myUserSession.updatePartner(executorId, dto);
         return (request.getParameter("ranges") == null) ?
         		new ForwardAndMessage(FORWARD_LIST, MESSAGE_UPDATED) :
         		new ForwardAndMessage(FORWARD_RANGES, MESSAGE_CREATED);
 	}
 	
 	@Override
 	protected void resetCachedList() {
         session.removeAttribute(Constants.SESSION_LIST_KEY + "partner");
 	}
 	
 	@Override
 	protected ForwardAndMessage doSetup() throws RemoteException {
 		final ForwardAndMessage result;
         
 		Integer partnerId = (Integer) session.getAttribute(
                 Constants.SESSION_PARTNER_ID);
         
         PartnerDTOEx partner;
         if (partnerId != null) {
             try {
                 partner = myUserSession.getPartnerDTO(partnerId);
             } catch (FinderException e) {
                 throw new SessionInternalError(e);
             }
             result = new ForwardAndMessage(FORWARD_EDIT);
         } else {
             partner = createPartnerFromDefaults();
             result = new ForwardAndMessage(FORWARD_CREATE);
         }
         
         myForm.set(FIELD_BALANCE, float2string(partner.getBalance()));
         if (partner.getPercentageRate() != null) {
             myForm.set(FIELD_RATE, float2string(partner.getPercentageRate()));
         }
         if (partner.getReferralFee() != null) {
             myForm.set(FIELD_FEE, float2string(partner.getReferralFee()));
         }
         myForm.set(FIELD_FEE_CURRENCY, partner.getFeeCurrencyId());
         myForm.set(FIELD_ONE_TIME, Integer.valueOf(1).equals(partner.getOneTime()));
         myForm.set(FIELD_PERIOD_UNIT_ID, partner.getPeriodUnitId());
         myForm.set(FIELD_PERIOD_VALUE, partner.getPeriodValue().toString());
         myForm.set(FIELD_PROCESS, Integer.valueOf(1).equals(partner.getAutomaticProcess()));
         myForm.set(FIELD_CLERK, partner.getRelatedClerkUserId().toString());
 
         setFormDate(FIELD_GROUP_PAYOUT, partner.getNextPayoutDate());
         
         return result;
 	}
 	
 	private PartnerDTOEx createPartnerFromDefaults() throws RemoteException {
 		PartnerDTOEx partner;
 		partner = new PartnerDTOEx();
 		// set the values from the preferences (defaults)
 		Integer[] preferenceIds = new Integer[] {
 				Constants.PREFERENCE_PART_DEF_RATE, 
 				Constants.PREFERENCE_PART_DEF_FEE, 
 				Constants.PREFERENCE_PART_DEF_FEE_CURR, 
 				Constants.PREFERENCE_PART_DEF_ONE_TIME, 
 				Constants.PREFERENCE_PART_DEF_PER_UNIT, 
 				Constants.PREFERENCE_PART_DEF_PER_VALUE, 
 				Constants.PREFERENCE_PART_DEF_AUTOMATIC, 
 				Constants.PREFERENCE_PART_DEF_CLERK, 
 		};
       
 		PreferencesMap prefs = mapEntityParameters(preferenceIds);
 		partner.setPercentageRate(string2float(prefs.getString(Constants.PREFERENCE_PART_DEF_RATE)));
 		partner.setReferralFee(string2float(prefs.getString(Constants.PREFERENCE_PART_DEF_FEE)));
 		if (partner.getReferralFee() != null){
 			partner.setFeeCurrencyId(prefs.getInteger(Constants.PREFERENCE_PART_DEF_FEE_CURR));
 		}
 		partner.setOneTime(prefs.getInteger(Constants.PREFERENCE_PART_DEF_ONE_TIME));
 		partner.setPeriodUnitId(prefs.getInteger(Constants.PREFERENCE_PART_DEF_PER_UNIT));
 		partner.setPeriodValue(prefs.getInteger(Constants.PREFERENCE_PART_DEF_PER_VALUE));
 		partner.setAutomaticProcess(prefs.getInteger(Constants.PREFERENCE_PART_DEF_AUTOMATIC));
 		partner.setRelatedClerkUserId(prefs.getInteger(Constants.PREFERENCE_PART_DEF_CLERK));
 		// some that are not preferences
 		partner.setBalance(new Float(0));
 		return partner;
 	}
 
 	@SuppressWarnings("unchecked")
 	private PreferencesMap mapEntityParameters(Integer[] ids) throws RemoteException {
 		HashMap<Integer, String> result = myUserSession.getEntityParameters(entityId, ids);
 		return new PreferencesMap(result);
 	}
 
 	private Integer valueOfCheckBox(String fieldName){
 		Boolean value = (Boolean) myForm.get(fieldName);
 		return value ? 1 : 0;
 	}
 	
 	
 }
