 /**
  * 
  */
 package cz.su.GroupClearing;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Currency;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Locale;
 
 /**
  * @author su
  * 
  */
 public class ClearingTransaction {
 
 	public class ParticipantInfo {
 		BigDecimal value;
 		boolean marked;
         long id;
 
 		public ParticipantInfo(long anId, BigDecimal aValue, boolean aMark) {
 			value = aValue;
 			marked = aMark;
             id = anId;
 		}
 
 		public BigDecimal getValue() {
 			return value;
 		}
 
 		public boolean isMarked() {
 			return marked;
 		}
 
 		public void setMarked(boolean aMark) {
 			marked = aMark;
 		}
 
 		public void setValue(BigDecimal aValue) {
 			value = aValue;
 		}
 	
         public long getId() {
             return id;
         }
     }
 
 	private final long id;
 	private final long eventId;
 	/**
 	 * Amount value 
      */
 	private BigDecimal amount;
 	/**
 	 * Sum of payments.
 	 * 
 	 * This is a convenience value for checking whether payments are equal to
 	 * the total value of transaction.
 	 */
 	private BigDecimal negativeAmount;
 	/**
 	 * Sum of positive amounts of participants.
 	 * 
 	 * This is a convenience value for checking whether payments are equal to
 	 * the total value of transaction.
 	 */
 	private BigDecimal positiveAmount;
 	private String name;
 	private final GregorianCalendar calendarDate;
 	private Currency currency;
 	private final HashMap<Long, ParticipantInfo> participantsInfo;
 	private String note;
     /**
      * Rate with respect to default currency of event.
      *
      * If default currency of event is A and currency of this
      * transaction is B, then 1B = rate A. E.g. amount of transaction
      * in A is obtained by multiplying rate * amount. Multiplication
      * by rate should not be used when A is the same currency as B,
      * rate is stored as a double, which should be enough in cases
      * A!=B, but might not work in case A==B.
      */
     private BigDecimal rate;
 	/**
 	 * If the amount should be split evenly among the participants.
 	 */
 	private boolean splitEvenly = true;
 	/**
 	 * @brief Id of receiver of the money amount.
 	 * 
 	 *        This is usually a person who paid for something and now he/she
 	 *        should get money from others. This person is called receiver
 	 *        because in general it is the person who is now receiving money
 	 *        from others as transaction needs not to be connected to an actual
 	 *        payment to outsiders.
 	 * 
 	 *        Note, that in case this value is -1, no explicit receiver is set.
 	 *        It does not necessarily mean that the transaction is unbalanced,
 	 *        because in participantsInfo the values of participants can be both
 	 *        positive and negative. Which means that transaction can be general
 	 *        in this sense. In case of general transaction you should keep
 	 *        amount zero while the actual amounts are then positiveAmount and
 	 *        negativeAmount, the former with sum of positive values and the
 	 *        latter with the sum of negative values in participantsInfo hash
 	 *        map.
 	 */
 	private long receiverId = -1;
 
 	public ClearingTransaction(long anId, long anEventId) {
 		super();
 		id = anId;
 		eventId = anEventId;
 		calendarDate = new GregorianCalendar();
 		currency = Currency.getInstance(Locale.getDefault());
 		amount = BigDecimal.ZERO;
 		positiveAmount = BigDecimal.ZERO;
 		negativeAmount = BigDecimal.ZERO;
         rate = BigDecimal.ONE;
 		participantsInfo = new HashMap<Long, ParticipantInfo>();
 		note = "";
 		receiverId = -1;
 		splitEvenly = true;
 	}
 
 	public Date getDate() {
 		return calendarDate.getTime();
 	}
 
 	public void setDate(int year, int month, int day) {
 		calendarDate.set(Calendar.YEAR, year);
 		calendarDate.set(Calendar.MONTH, month);
 		calendarDate.set(Calendar.DAY_OF_MONTH, day);
 	}
 
 	public void setDate(Date aDate) {
 		calendarDate.setTime(aDate);
 	}
 
 	public int getYear() {
 		return calendarDate.get(Calendar.YEAR);
 	}
 
 	public int getMonth() {
 		return calendarDate.get(Calendar.MONTH);
 	}
 
 	public int getDayOfMonth() {
 		return calendarDate.get(Calendar.DAY_OF_MONTH);
 	}
 
 	public void setAmount(BigDecimal newAmount) {
 		amount = newAmount;
 	}
 
 	public BigDecimal getAmount() {
 		return amount;
 	}
 
 	public BigDecimal getPositiveAmount() {
 		return positiveAmount;
 	}
 
 	public BigDecimal getNegativeAmount() {
 		return negativeAmount;
 	}
 
     public BigDecimal getBalance() {
         return negativeAmount.subtract(positiveAmount);
     }
 
     public boolean hasNonzeroValues() {
         return (positiveAmount.signum() != 0
                 || negativeAmount.signum() != 0
                 || amount.signum() != 0
                 || receiverId >= 0);
     }
 
 	public Currency getCurrency() {
 		return currency;
 	}
 
 	public void setCurrency(Currency currency) {
 		this.currency = currency;
 	}
 
 	public long getReceiverId() {
 		return receiverId;
 	}
 
 	public void setReceiverId(long anId) {
 		receiverId = anId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 
 	public boolean getSplitEvenly() {
 		return splitEvenly;
 	}
 
     /**@brief Sets the split evenly flag.
      *
      * @param split New value of splitEvenly flag.
      *
      * Note, that when changing the type of transaction, the values of
      * participants and amount may change, this is because their
      * meaning in each kind of transaction is different.
      */
 	public void setSplitEvenly(boolean split) {
 		splitEvenly = split;
     }
 
 	public long getId() {
 		return id;
 	}
 
 	public long getEventId() {
 		return eventId;
 	}
 
 	/**
 	 * @brief Sets value of participant with given id in this transaction.
 	 */
 	public void setAndSaveParticipantValue(long participantId, BigDecimal aValue, GCDatabase db) {
 		Long participantIdObject = Long.valueOf(participantId);
 		ParticipantInfo info = participantsInfo.get(participantIdObject);
         if (info != null && aValue.compareTo(info.getValue()) == 0) {
             // No change.
             return;
         }
         if (info == null) {
             info = new ParticipantInfo(participantId, aValue,
                     aValue.signum() != 0);
             participantsInfo.put(participantIdObject, info);
             db.updateTransactionParticipantValue(eventId,
                     id, participantId, info.getValue(), info.isMarked());
         } else {
            if (info.getValue().signum() > 0) {
                positiveAmount = positiveAmount.subtract(info.getValue());
            } else {
                negativeAmount = negativeAmount.add(info.getValue());
            }
             info.setValue(aValue);
             info.setMarked(aValue.signum() != 0);
         }
         db.updateTransactionParticipantValue(eventId,
                 id, participantId, info.getValue(), info.isMarked());
         if (splitEvenly) {
             splitEvenly = false;
             db.updateTransactionSplitEvenly(this);
         }
         if (aValue.signum() > 0) {
             positiveAmount = positiveAmount.add(aValue);
         } else {
             negativeAmount = negativeAmount.subtract(aValue);
         }
         if (positiveAmount.signum() < 0) {
             positiveAmount = BigDecimal.ZERO;
         }
         if (negativeAmount.signum() < 0) {
             negativeAmount = BigDecimal.ZERO;
         }
         if (amount.compareTo(positiveAmount) != 0) {
             amount = positiveAmount;
             db.updateTransactionAmount(this);
         }
 	}
 
 	/**
 	 * @brief Sets mark of participant with given id.
 	 * 
 	 *        If participant with given id does not exist yet, it is inserted
 	 *        with value 0 only in case aMark is true.
 	 */
 	public void setParticipantMark(long participantId, boolean aMark) {
 		Long participantIdObject = Long.valueOf(participantId);
 		ParticipantInfo info = participantsInfo.get(participantIdObject);
 		if (info != null) {
 			info.setMarked(aMark);
 		} else {
 			if (aMark) {
 				info = new ParticipantInfo(participantId, BigDecimal.ZERO, true);
 				participantsInfo.put(participantIdObject, info);
 			}
 		}
 	}
 
 	public BigDecimal getParticipantValue(long participantId) {
 		Long participantIdObject = Long.valueOf(participantId);
 		ParticipantInfo info = participantsInfo.get(participantIdObject);
 		if (info != null) {
 			return info.getValue();
 		}
 		return BigDecimal.ZERO;
 	}
 
 	public boolean isParticipantMarked(long participantId) {
 		Long participantIdObject = Long.valueOf(participantId);
 		ParticipantInfo info = participantsInfo.get(participantIdObject);
 		if (info != null) {
 			return info.isMarked();
 		}
 		return false;
 	}
 
 	public void addParticipant(long participantId, BigDecimal aValue, boolean aMark) {
 		ParticipantInfo info = new ParticipantInfo(participantId, aValue, aMark);
         if (aMark) {
             if (aValue.signum() > 0) {
                 positiveAmount = positiveAmount.add(aValue);
             }
             else {
                 negativeAmount = negativeAmount.subtract(aValue);
             }
         }
         participantsInfo.put(Long.valueOf(participantId), info);
 	}
 
     public BigDecimal recomputeAndSaveChanges(GCDatabase db) {
         if (splitEvenly) {
             positiveAmount = BigDecimal.ZERO;
             negativeAmount = BigDecimal.ZERO;
             BigDecimal negAmount = BigDecimal.ZERO;
             BigDecimal share = BigDecimal.ZERO;
             BigDecimal shiftedOne = 
                 BigDecimal.ONE.movePointLeft(currency.getDefaultFractionDigits());
             long numberOfMarkedParticipants = 0;
             for (ParticipantInfo info : participantsInfo.values()) {
                 if (info.isMarked()) {
                     ++ numberOfMarkedParticipants;
                 }
             }
 			if (numberOfMarkedParticipants > 0) {
                 BigDecimal markedNumber = new BigDecimal(numberOfMarkedParticipants);
 				share = amount.divide(markedNumber,
                         currency.getDefaultFractionDigits(), BigDecimal.ROUND_DOWN);
                 negAmount = share.multiply(markedNumber);
             }
             for (ParticipantInfo info : participantsInfo.values()) {
                 BigDecimal value = BigDecimal.ZERO;
                 if (info.isMarked()) {
                     value = share.negate();
                     if (negAmount.compareTo(amount) < 0) {
                         value = value.subtract(shiftedOne);
                         negAmount = negAmount.add(shiftedOne);
                     }
                 }
                 if (info.getId() == receiverId) {
                     value = value.add(amount);
                 }
                 if (value.signum() > 0) {
                     positiveAmount = positiveAmount.add(value);
                 } else {
                     negativeAmount = negativeAmount.subtract(value);
                 }
                 if (info.getValue().compareTo(value) != 0) {
                     info.setValue(value);
                     if (db != null) {
                         db.updateTransactionParticipantValue(eventId,
                                 id, info.getId(), value, info.isMarked());
                     }
                 }
             }
         } else {
             // Go through all participants and unmark those with 0 value.
            positiveAmount = BigDecimal.ZERO;
            negativeAmount = BigDecimal.ZERO;
                 for (ParticipantInfo info : participantsInfo.values()) {
                     boolean oldMark = info.isMarked();
                     info.setMarked (info.getValue().signum() != 0);
                     if (oldMark != info.isMarked()) {
                         if (db != null) {
                             db.updateTransactionParticipantValue(eventId,
                                     id, info.getId(), info.getValue(),
                                     info.isMarked());
                         }
                     }
                     if (info.getValue().signum() > 0) {
                         positiveAmount = positiveAmount.add(info.getValue());
                     }
                     else {
                         negativeAmount = negativeAmount.subtract(info.getValue());
                     }
                 }
             if (amount.compareTo(positiveAmount) != 0) {
                 amount = positiveAmount;
                 if (db != null) {
                     db.updateTransactionAmount(this);
                 }
             }
         }
         return negativeAmount.subtract(positiveAmount);
     }
   
     public void setRate(BigDecimal rate) {
         this.rate = rate;
     }
 
     public BigDecimal getRate() {
         return rate;
     }
 
     public void resetValues(GCDatabase db) {
         for (ParticipantInfo info : participantsInfo.values()) {
             info.setValue(BigDecimal.ZERO);
             info.setMarked(splitEvenly);
         }
         positiveAmount = negativeAmount = amount = BigDecimal.ZERO;
         receiverId = -1;
         if (db != null) {
             db.resetTransactionParticipantsValues(this, splitEvenly);
             db.updateTransactionAmount(this);
             db.updateTransactionReceiverId(this);
         }
     }
 
 	@Override
 	public String toString() {
 		return "ClearingTransaction [id=" + id + ", eventId=" + eventId
 				+ ", calendarDate=" + calendarDate + ", amount=" + amount
 				+ ", positiveAmount=" + positiveAmount + ", negativeAmount="
 				+ negativeAmount + ", currency=" + currency + ", participants="
 				+ participantsInfo + ", note=" + note + "]";
 	}
 }
