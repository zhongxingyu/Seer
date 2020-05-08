 package BE;
 
 import java.util.Calendar;
 
 public class MembershipFee
 {
 
     private int memberId;
     private int typeId;
     private Calendar invoiceSent;
     private Calendar paymentReceived;
     private boolean isPaid;
 
    /**
      * Constructor for membership fee with everything
     * @param memberId member id
     * @param typeId membership tyoe id
     * @param invoiceSent when the invoice was sent
     * @param paymentReceived when the payment was received
     * @param isPaid isPaid = false: membership fee has not been paid. isPaid =
     * true: membership fee has been paid
      */
     public MembershipFee(int memberId, int typeId, Calendar invoiceSent, Calendar paymentReceived, boolean isPaid)
     {
         this.memberId = memberId;
         this.typeId = typeId;
         this.isPaid = isPaid;
         this.invoiceSent = invoiceSent;
         this.paymentReceived = paymentReceived;
     }
 
    /**
      * Constructor for membership fee with just a membership fee object
     * @param mf A given membership fee
      */
     public MembershipFee(MembershipFee mf)
     {
         this(mf.memberId, mf.typeId, mf.invoiceSent, mf.paymentReceived, mf.isPaid);
     }
 
     /**
      * Returns the member id of the member fee
      * @return the memberId
      */
     public int getMemberId()
     {
         return memberId;
     }
 
     /**
      * Returns the membership type ID
      * @return the typeId
      */
     public int getTypeId()
     {
         return typeId;
     }
 
     /**
      * Sets the membership type ID
      * @param typeId the typeId to set
      */
     public void setTypeId(int typeId)
     {
         this.typeId = typeId;
     }
 
     /**
      * Returns the date the invoice was sent
      * @return the invoiceSent
      */
     public Calendar getInvoiceSent()
     {
         return invoiceSent;
     }
 
     /**
      * Sets the date the invoice was sent
      * @param invoiceSent the invoiceSent to set
      */
     public void setInvoiceSent(Calendar invoiceSent)
     {
         this.invoiceSent = invoiceSent;
     }
 
     /**
      * Returns the date the payment was received
      * @return the paymentReceived
      */
     public Calendar getPaymentReceived()
     {
         return paymentReceived;
     }
 
     /**
      * Sets the date the payment was received
      * @param paymentReceived the paymentReceived to set
      */
     public void setPaymentReceived(Calendar paymentReceived)
     {
         this.paymentReceived = paymentReceived;
     }
 
     /**
      * Returns a boolean. False if the membership fee has not been paid, true if it has been paid
      * @return the isPaid
      */
     public boolean isIsPaid()
     {
         return isPaid;
     }
 
     /**
     * Sets the boolean to either true or false depending on if the membership
     * has been paid
      * @param isPaid the isPaid to set
      */
     public void setIsPaid(boolean isPaid)
     {
         this.isPaid = isPaid;
     }
 }
