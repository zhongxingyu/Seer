 package de.hh.changeRing.transaction;
 
 import de.hh.changeRing.BaseEntity;
 import de.hh.changeRing.infrastructure.eclipselink.MappingCustomizer;
 import de.hh.changeRing.initialData.InitTestData;
 import de.hh.changeRing.user.DepotItem;
 import de.hh.changeRing.user.User;
 import org.eclipse.persistence.annotations.Customizer;
 import org.joda.time.DateTime;
 
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 import java.math.BigDecimal;
 
 import static de.hh.changeRing.infrastructure.JaxBUtils.DateTimeAdapter;
 import static de.hh.changeRing.user.DepotItemType.in;
 import static de.hh.changeRing.user.DepotItemType.out;
 
 /**
  * ----------------GNU General Public License--------------------------------
  * <p/>
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * ----------------in addition-----------------------------------------------
  * <p/>
  * In addition, each military use, and the use for interest profit will be
  * excluded.
  * Environmental damage caused by the use must be kept as small as possible.
  */
 @XmlAccessorType(XmlAccessType.PROPERTY)
 @Entity
@Customizer(MappingCustomizer.class)
 public class Transaction extends BaseEntity {
     @XmlElement
     @XmlJavaTypeAdapter(DateTimeAdapter.class)
     private DateTime date;
 
     @XmlElement
     @Transient
     private Long fromId;
 
     @SuppressWarnings("JpaDataSourceORMInspection")
     @ManyToOne
     @JoinColumn(name = "from_user_id", nullable = false)
     private User from;
 
     @XmlElement
     @Transient
     private Long toId;
 
     @SuppressWarnings("JpaDataSourceORMInspection")
     @ManyToOne
     @JoinColumn(name = "to_user_id", nullable = false)
     private User to;
 
     @XmlElement
     @Column(scale = 2, precision = 7)
     private BigDecimal amount;
 
     private static long idCounter = 0;
 
     @XmlElement
     private String subject;
     @Column(scale = 2, precision = 7)
     private BigDecimal fromNewBalance;
     @Column(scale = 2, precision = 7)
     private BigDecimal toNewBalance;
 
     public User getFrom() {
         return from;
     }
 
     public DateTime getDate() {
         return date;
     }
 
     public User getTo() {
         return to;
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public Long getId() {
         if (id == null) {
             id = idCounter++;
         }
         return id;
     }
 
     public static Transaction create(User from, User to, BigDecimal amount, String subject) {
         Transaction transaction = new Transaction();
         transaction.from = from;
         transaction.to = to;
         transaction.amount = amount;
         transaction.subject = subject;
         transaction.date = new DateTime();
         transaction.execute();
         return transaction;
     }
 
     private void execute() {
         fromNewBalance = from.getBalance().subtract(amount);
         toNewBalance = to.getBalance().add(amount);
         from.setBalance(fromNewBalance);
         to.setBalance(toNewBalance);
         from.getSentTransactions().add(this);
         to.getReceivedTransactions().add(this);
 	    to.setLastWork(date);
     }
 
     public void wire() {
         if (from == null) {
             from = InitTestData.findUser(fromId);
         }
         if (to == null) {
             to = InitTestData.findUser(toId);
         }
         execute();
     }
 
     public String getSubject() {
         return subject;
     }
 
     public BigDecimal getFromNewBalance() {
         return fromNewBalance;
     }
 
     public BigDecimal getToNewBalance() {
         return toNewBalance;
     }
 
     public DepotItem toDepotItem(User user) {
         return new DepotItem(this, user.equals(getTo())? in: out);
     }
 
     @Override
     public String toString() {
         return "Transaction{" +
                 "date=" + date +
                 ", fromId=" + from.getId() +
                 ", toId=" + to.getId() +
                 ", amount=" + amount +
                 ", subject='" + subject + '\'' +
                 ", fromNewBalance=" + fromNewBalance +
                 ", toNewBalance=" + toNewBalance +
                 '}';
     }
 }
