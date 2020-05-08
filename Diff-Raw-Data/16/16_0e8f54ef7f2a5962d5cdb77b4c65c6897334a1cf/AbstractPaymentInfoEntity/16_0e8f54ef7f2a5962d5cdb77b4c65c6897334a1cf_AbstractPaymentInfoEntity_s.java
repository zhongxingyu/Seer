 package com.mpower.domain.model.paymentInfo;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.collections.Factory;
 import org.apache.commons.collections.list.LazyList;
 import org.springframework.core.style.ToStringCreator;
 
 import com.mpower.domain.model.AbstractCustomizableEntity;
 import com.mpower.domain.model.AddressAware;
 import com.mpower.domain.model.EmailAware;
 import com.mpower.domain.model.PaymentSource;
 import com.mpower.domain.model.PaymentSourceAware;
 import com.mpower.domain.model.Person;
 import com.mpower.domain.model.PhoneAware;
 import com.mpower.domain.model.Site;
 import com.mpower.domain.model.communication.Address;
 import com.mpower.domain.model.communication.Email;
 import com.mpower.domain.model.communication.Phone;
 import com.mpower.type.FormBeanType;
 import com.mpower.util.StringConstants;
 
 @SuppressWarnings({ "serial", "unchecked" })
 public abstract class AbstractPaymentInfoEntity extends AbstractCustomizableEntity implements PaymentSourceAware, AddressAware, PhoneAware, EmailAware  {
 
     protected String comments;
     protected String currencyCode = StringConstants.USD;
     protected String paymentType;
     protected Integer checkNumber;
     protected boolean sendAcknowledgment = false;
     protected Date acknowledgmentDate;
     
     protected Person person;
     /** Form bean representation of the DistributionLines */
     protected List<DistributionLine> mutableDistributionLines = LazyList.decorate(new ArrayList<DistributionLine>(), new Factory() {
         public Object create() {
             return new DistributionLine();
         }
     });
     
     /** Domain object representation of the DistributionLines */
     protected List<DistributionLine> distributionLines;
 
     private FormBeanType addressType;
     private FormBeanType phoneType;
     private FormBeanType emailType;
     private FormBeanType paymentSourceType;
 
     protected Address address = new Address();
     protected Phone phone = new Phone();
     protected Email email = new Email();
     protected PaymentSource paymentSource = new PaymentSource(person);
     
     protected Address selectedAddress = new Address();
     protected Phone selectedPhone = new Phone();
     protected Email selectedEmail = new Email();
     protected PaymentSource selectedPaymentSource = new PaymentSource(person);
 
     public Person getPerson() {
         return person;
     }
 
     public void setPerson(Person person) {
         this.person = person;
         if (person != null) {
             if (paymentSource != null) {
                 paymentSource.setPerson(person);
             }
             if (selectedPaymentSource != null) {
                 selectedPaymentSource.setPerson(person);
             }
         }
     }
 
     public String getComments() {
         return comments;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
     }
 
     public String getCurrencyCode() {
         if (currencyCode == null) {
             currencyCode = StringConstants.USD;
         }
         return currencyCode;
     }
 
     public void setCurrencyCode(String currencyCode) {
         this.currencyCode = currencyCode;
     }
 
     @Override
     public void setPaymentType(String paymentType) {
         this.paymentType = paymentType;
     }
 
     @Override
     public String getPaymentType() {
         return paymentType;
     }
 
     public void setCheckNumber(Integer checkNumber) {
         this.checkNumber = checkNumber;
     }
 
     public Integer getCheckNumber() {
         return checkNumber;
     }
 
     public boolean isSendAcknowledgment() {
         return sendAcknowledgment;
     }
 
     public void setSendAcknowledgment(boolean sendAcknowledgment) {
         this.sendAcknowledgment = sendAcknowledgment;
     }
 
     public Date getAcknowledgmentDate() {
         return acknowledgmentDate;
     }
 
     public void setAcknowledgmentDate(Date acknowledgmentDate) {
         this.acknowledgmentDate = acknowledgmentDate;
     }
 
     public List<DistributionLine> getDistributionLines() {
         return distributionLines;
     }
 
     public void setDistributionLines(List<DistributionLine> distributionLines) {
         this.distributionLines = distributionLines;
     }
 
     public List<DistributionLine> getMutableDistributionLines() {
         return mutableDistributionLines;
     }
 
     public void setMutableDistributionLines(List<DistributionLine> mutableDistributionLines) {
         this.mutableDistributionLines = mutableDistributionLines;
     }
 
     public void removeEmptyMutableDistributionLines() {
         Iterator<DistributionLine> mutableLinesIter = mutableDistributionLines.iterator();
         distributionLines = new ArrayList<DistributionLine>();
         while (mutableLinesIter.hasNext()) {
             DistributionLine line = mutableLinesIter.next();
            if (line != null && line.isFieldEntered() == false) {
                mutableLinesIter.remove();
            }
            else {
                distributionLines.add(line);
             }
         }
     }
 
     public void filterValidDistributionLines() {
         distributionLines = new ArrayList<DistributionLine>();
         Iterator<DistributionLine> mutableLinesIter = mutableDistributionLines.iterator();
         while (mutableLinesIter.hasNext()) {
             DistributionLine line = mutableLinesIter.next();
             if (line != null && line.isValid()) {
                 distributionLines.add(line);
             }
         }
     }
 
     @Override
     public FormBeanType getAddressType() {
         return this.addressType;
     }
 
     @Override
     public void setAddressType(FormBeanType type) {
         this.addressType = type;
     }
 
     @Override
     public FormBeanType getPhoneType() {
         return this.phoneType;
     }
 
     @Override
     public void setPhoneType(FormBeanType type) {
         this.phoneType = type;
     }
 
     @Override
     public FormBeanType getEmailType() {
         return this.emailType;
     }
 
     @Override
     public void setEmailType(FormBeanType type) {
         this.emailType = type;
     }
 
     @Override
     public FormBeanType getPaymentSourceType() {
         return this.paymentSourceType;
     }
 
     @Override
     public void setPaymentSourceType(FormBeanType type) {
         this.paymentSourceType = type;
     }
 
     @Override
     public PaymentSource getPaymentSource() {
         return paymentSource;
     }
 
     @Override
     public void setPaymentSource(PaymentSource paymentSource) {
         this.paymentSource = paymentSource;
     }
 
     @Override
     public Address getAddress() {
         return address;
     }
 
     @Override
     public void setAddress(Address address) {
         this.address = address;
     }
 
     @Override
     public Phone getPhone() {
         return phone;
     }
 
     @Override
     public void setPhone(Phone phone) {
         this.phone = phone;
     }
 
     @Override
     public Email getEmail() {
         return email;
     }
 
     @Override
     public void setEmail(Email email) {
         this.email = email;
     }
 
     @Override
     public PaymentSource getSelectedPaymentSource() {
         if (selectedPaymentSource == null) {
             selectedPaymentSource = new PaymentSource(); // created only because spring needs to bind to it
         }
         return selectedPaymentSource;
     }
 
     public void setSelectedPaymentSource(PaymentSource selectedPaymentSource) {
         this.selectedPaymentSource = selectedPaymentSource;
     }
 
     @Override
     public Address getSelectedAddress() {
         if (selectedAddress == null) {
             selectedAddress = new Address(); // created only because spring needs to bind to it
         }
         return selectedAddress;
     }
 
     public void setSelectedAddress(Address selectedAddress) {
         this.selectedAddress = selectedAddress;
     }
 
     @Override
     public Phone getSelectedPhone() {
         if (selectedPhone == null) {
             selectedPhone = new Phone(); // created only because spring needs to bind to it
         }
         return selectedPhone;
     }
 
     public void setSelectedPhone(Phone selectedPhone) {
         this.selectedPhone = selectedPhone;
     }
 
     @Override
     public Email getSelectedEmail() {
         if (selectedEmail == null) {
             selectedEmail = new Email(); // created only because spring needs to bind to it
         }
         return selectedEmail;
     }
 
     public void setSelectedEmail(Email selectedEmail) {
         this.selectedEmail = selectedEmail;
     }
 
     public Site getSite() {
         return person != null ? person.getSite() : null;
     }
 
     @Override
     public void setPaymentSourcePaymentType() {
         if (paymentSource != null) {
             paymentSource.setPaymentType(getPaymentType());
         }
     }
     
     @Override
     public void setPaymentSourceAwarePaymentType() {
         if (selectedPaymentSource != null) {
             setPaymentType(selectedPaymentSource.getPaymentType());
         }
     }
     
     @Override
     public String toString() {
         return new ToStringCreator(this).append(super.toString()).append("paymentType", paymentType).append("currencyCode", currencyCode).
             append("checkNumber", checkNumber).append("sendAcknowledgment", sendAcknowledgment).append("acknowledgmentDate", acknowledgmentDate).append("comments", comments).
             append("address", address).append("phone", phone).append("email", email).append("paymentSource", paymentSource).
             append("paymentSourceType", paymentSourceType).
             append("selectedAddress", selectedAddress).append("selectedPhone", selectedPhone).append("selectedEmail", selectedEmail).append("selectedPaymentSource", selectedPaymentSource).
             append("emailType", emailType).append("phoneType", phoneType).append("addressType", addressType).append("person", person).
             toString();
     }
 }
