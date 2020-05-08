 package com.orangeleap.tangerine.controller;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 
 import com.orangeleap.tangerine.controller.communication.address.AddressEditor;
 import com.orangeleap.tangerine.controller.communication.email.EmailEditor;
 import com.orangeleap.tangerine.controller.communication.phone.PhoneEditor;
 import com.orangeleap.tangerine.controller.payment.PaymentSourceEditor;
 import com.orangeleap.tangerine.domain.AddressAware;
 import com.orangeleap.tangerine.domain.EmailAware;
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.PaymentSourceAware;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.PhoneAware;
 import com.orangeleap.tangerine.domain.communication.Address;
 import com.orangeleap.tangerine.domain.communication.Email;
 import com.orangeleap.tangerine.domain.communication.Phone;
 import com.orangeleap.tangerine.service.AddressService;
 import com.orangeleap.tangerine.service.EmailService;
 import com.orangeleap.tangerine.service.PaymentSourceService;
 import com.orangeleap.tangerine.service.PhoneService;
 import com.orangeleap.tangerine.type.FormBeanType;
 import com.orangeleap.tangerine.util.StringConstants;
 
 public abstract class TangerineConstituentAttributesFormController extends TangerineFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name="paymentSourceService")
     protected PaymentSourceService paymentSourceService;
     
     @Resource(name="addressService")
     protected AddressService addressService;
     
     @Resource(name="phoneService")
     protected PhoneService phoneService;
     
     @Resource(name="emailService")
     protected EmailService emailService;
     
     protected boolean bindPaymentSource = true;
     protected boolean bindAddress = true;
     protected boolean bindPhone = true;
     protected boolean bindEmail = true;
     
     /**
      * If you do not want to bind to PaymentSources, set to false.  Default is true
      * @param bindPaymentSource
      */
     public void setBindPaymentSource(boolean bindPaymentSource) {
         this.bindPaymentSource = bindPaymentSource;
     }
 
     /**
      * If you do not want to bind to Addresses, set to false.  Default is true
      * @param bindAddress
      */
     public void setBindAddress(boolean bindAddress) {
         this.bindAddress = bindAddress;
     }
 
     /**
      * If you do not want to bind to Phones, set to false.  Default is true
      * @param bindPhone
      */
     public void setBindPhone(boolean bindPhone) {
         this.bindPhone = bindPhone;
     }
 
     /**
      * If you do not want to bind to Emails, set to false.  Default is true
      * @param bindEmail
      */
     public void setBindEmail(boolean bindEmail) {
         this.bindEmail = bindEmail;
     }
     
     protected Person getConstituent(HttpServletRequest request) {
         Long constituentId = getConstituentId(request);
         Person constituent = null;
         if (constituentId != null) {
             constituent = constituentService.readConstituentById(constituentId); // TODO: do we need to check if the user can view this person (authorization)?
         }
         if (constituent == null) {
             throw new IllegalArgumentException("The constituent ID was not found");
         }
         return constituent;
     }
 
     @SuppressWarnings("unchecked")
     protected void addConstituentToReferenceData(HttpServletRequest request, Map refData) {
         Person constituent = getConstituent(request);
         if (constituent != null) {
             refData.put(StringConstants.PERSON, getConstituent(request));
         }
     }
 
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         if (bindPaymentSource) {
             binder.registerCustomEditor(PaymentSource.class, new PaymentSourceEditor(paymentSourceService, this.getConstituentIdString(request)));
         }
         if (bindAddress) {
             binder.registerCustomEditor(Address.class, new AddressEditor(addressService, this.getConstituentIdString(request)));
         }
         if (bindPhone) {
             binder.registerCustomEditor(Phone.class, new PhoneEditor(phoneService, this.getConstituentIdString(request)));
         }
         if (bindEmail) {
             binder.registerCustomEditor(Email.class, new EmailEditor(emailService, this.getConstituentIdString(request)));
         }
     }
 
     @Override
     protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
         if (isFormSubmission(request)) {
             if (bindAddress && command instanceof AddressAware) {
                 this.bindAddress(request, (AddressAware)command);
             }
             if (bindPhone && command instanceof PhoneAware) {
                 this.bindPhone(request, (PhoneAware)command);
             }
             if (bindEmail && command instanceof EmailAware) {
                 this.bindEmail(request, (EmailAware)command);
             }
             if (bindPaymentSource && command instanceof PaymentSourceAware) {
                 this.bindPaymentSource(request, (PaymentSourceAware)command);
             }
         }        
         super.onBind(request, command, errors);
     }
 
     
     @SuppressWarnings("unchecked")
     @Override
     protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
         Map refData = new HashMap();
         this.addConstituentToReferenceData(request, refData);
 
         refDataPaymentSources(request, command, errors, refData);
         refDataAddresses(request, command, errors, refData);
         refDataPhones(request, command, errors, refData);
         refDataEmails(request, command, errors, refData);
         return refData;
     }
     
     @SuppressWarnings("unchecked")
     protected void refDataPaymentSources(HttpServletRequest request, Object command, Errors errors, Map refData) {
         if (bindPaymentSource) {
             Map<String, List<PaymentSource>> paymentSources = paymentSourceService.groupActivePaymentSourcesACHCreditCard(this.getConstituentId(request));
             refData.put(StringConstants.PAYMENT_SOURCES, paymentSources);
         }
     }
     
     @SuppressWarnings("unchecked")
     protected void refDataAddresses(HttpServletRequest request, Object command, Errors errors, Map refData) {
         if (bindAddress) {
             List<Address> addresses = addressService.filterValid(this.getConstituentId(request));
             refData.put(StringConstants.ADDRESSES, addresses);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected void refDataEmails(HttpServletRequest request, Object command, Errors errors, Map refData) {
         if (bindEmail) {
             List<Email> emails = emailService.filterValid(this.getConstituentId(request));
             refData.put(StringConstants.EMAILS, emails);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected void refDataPhones(HttpServletRequest request, Object command, Errors errors, Map refData) {
         if (bindPhone) {
             List<Phone> phones = phoneService.filterValid(this.getConstituentId(request));
             refData.put(StringConstants.PHONES, phones);
         }
     }
     
     protected void bindAddress(HttpServletRequest request, AddressAware addressAware) {
         String selectedAddress = request.getParameter(StringConstants.SELECTED_ADDRESS);
         if (StringConstants.NEW.equals(selectedAddress)) {
             addressAware.setAddressType(FormBeanType.NEW);
             addressAware.getAddress().setUserCreated(true);
         }
         else if (StringConstants.NONE.equals(selectedAddress)) {
             addressAware.setAddressType(FormBeanType.NONE);
            addressAware.getAddress().setUserCreated(false);
         }
         else {
             addressAware.setAddressType(FormBeanType.EXISTING);
            addressAware.getAddress().setUserCreated(false);
         }
     }
 
     protected void bindPhone(HttpServletRequest request, PhoneAware phoneAware) {
         String selectedPhone = request.getParameter(StringConstants.SELECTED_PHONE);
         if (StringConstants.NEW.equals(selectedPhone)) {
             phoneAware.setPhoneType(FormBeanType.NEW);
             phoneAware.getPhone().setUserCreated(true);
         }
         else if (StringConstants.NONE.equals(selectedPhone)) {
             phoneAware.setPhoneType(FormBeanType.NONE);
            phoneAware.getPhone().setUserCreated(false);
         }
         else {
             phoneAware.setPhoneType(FormBeanType.EXISTING);
            phoneAware.getPhone().setUserCreated(false);
         }
     }
 
     protected void bindEmail(HttpServletRequest request, EmailAware emailAware) {
         String selectedEmail = request.getParameter(StringConstants.SELECTED_EMAIL);
         if (StringConstants.NEW.equals(selectedEmail)) {
             emailAware.setEmailType(FormBeanType.NEW);
             emailAware.getEmail().setUserCreated(true);
         }
         else if (StringConstants.NONE.equals(selectedEmail)) {
             emailAware.setEmailType(FormBeanType.NONE);
            emailAware.getEmail().setUserCreated(false);
         }
         else {
             emailAware.setEmailType(FormBeanType.EXISTING);
            emailAware.getEmail().setUserCreated(false);
         }
     }
     
     protected void bindPaymentSource(HttpServletRequest request, PaymentSourceAware paymentSourceAware) {
         String paymentType = request.getParameter(StringConstants.PAYMENT_TYPE);
         if (PaymentSource.ACH.equals(paymentType) || PaymentSource.CREDIT_CARD.equals(paymentType)) {
             String selectedPaymentSource = request.getParameter(StringConstants.SELECTED_PAYMENT_SOURCE);
             if (StringConstants.NEW.equals(selectedPaymentSource)) {
                 paymentSourceAware.setPaymentSourceType(FormBeanType.NEW);
                 paymentSourceAware.getPaymentSource().setUserCreated(true);
                 paymentSourceAware.setPaymentSourcePaymentType();
             }
             else if (StringConstants.NONE.equals(selectedPaymentSource)) {
                 paymentSourceAware.setPaymentSourceType(FormBeanType.NONE);
                paymentSourceAware.getPaymentSource().setUserCreated(false);
             }
             else {
                 paymentSourceAware.setPaymentSourceType(FormBeanType.EXISTING);
                paymentSourceAware.getPaymentSource().setUserCreated(false);
                 paymentSourceAware.setPaymentSourceAwarePaymentType();
             }
         }
     }
 }
