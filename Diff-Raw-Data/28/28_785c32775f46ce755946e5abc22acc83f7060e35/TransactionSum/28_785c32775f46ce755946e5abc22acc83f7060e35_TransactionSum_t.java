 /**
  * TransactionSum.java
  * created: 09.12.2007 12:06:22
  * (c) 2007 by
  * <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
  * This file is part of jgnucashLib-V1 by Marcus Wolschon
  * <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
  * You can purchase support for a sensible hourly rate or
  * a commercial license of this file (unless modified by others)
  * by contacting him directly.
  *
  *  jgnucashLib-V1 is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  jgnucashLib-V1 is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with jgnucashLib-V1.  If not, see <http://www.gnu.org/licenses/>.
  *
  ***********************************
  * Editing this file:
  *  -For consistent code-quality this file should be checked with the
  *   checkstyle-ruleset enclosed in this project.
  *  -After the design of this file has settled it should get it's own
  *   JUnit-Test that shall be executed regularly. It is best to write
  *   the test-case BEFORE writing this class and to run it on every build
  *   as a regression-test.
  */
 package biz.wolschon.finance.jgnucash.widgets;
 
 
 
 //automatically created propertyChangeListener-Support
 import java.awt.BorderLayout;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import biz.wolschon.fileformats.gnucash.GnucashAccount;
 import biz.wolschon.fileformats.gnucash.GnucashFile;
 import biz.wolschon.fileformats.gnucash.GnucashTransaction;
 import biz.wolschon.fileformats.gnucash.GnucashTransactionSplit;
 import biz.wolschon.numbers.FixedPointNumber;
 
 
 /**
  * (c) 2007 by <a href="http://Wolschon.biz>
  * Wolschon Softwaredesign und Beratung</a>.<br/>
  * Project: jgnucashLib-V1<br/>
  * TransactionSum.java<br/>
  * created: 09.12.2007 12:06:22 <br/>
  *<br/><br/>
  * This panel displays a sum of all transaction-splits that are
  * to any of a list of accounts belonging to transactions with at
  * least one split in another list of accounts.<br/>
  * It is very handy for tax- and other reports.
  * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
  */
 public class TransactionSum extends JPanel {
 
     /**
      * For serializing.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Automatically created logger for debug and error-output.
      */
     /**
      * Our logger for debug- and error-ourput.
      */
     private static final Log LOGGER = LogFactory.getLog(TransactionSum.class);
 
     /**
      * The financial data we operate on.
      */
     private GnucashFile myBooks;
 
     /**
      * We only operate on transactions that
      * contain one of these accounts.
      */
     private Set<GnucashAccount> mySourceAccounts;
 
     /**
      * We sum all transaction-splits that are to
      * one of these accounts.
      * @see #mySourceAccounts
      */
     private Set<GnucashAccount> myTargetAccounts;
 
     /**
      * We ignore all transactions that are before this date.
      */
     private Date myMinDate;
     /**
      * We ignore all transactions that are after this date.
      */
     private Date myMaxDate;
 
     /**
      * The type of summations we are to calculate.
      */
     private SUMMATIONTYPE mySummationType = SUMMATIONTYPE.ALL;
 
     /**
      * (c) 2007 by <a href="http://Wolschon.biz>
      * Wolschon Softwaredesign und Beratung</a>.<br/>
      * Project: jgnucashLib-V1<br/>
      * TransactionSum.java<br/>
      * created: 09.12.2007 12:34:53 <br/>
      *<br/><br/>
      * The types of summations we can do.
      * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
      */
     public enum SUMMATIONTYPE {
         /**
          * Sum all splits.
          */
         ALL,
         /**
          * Sum only splits that increase the balance on
          * the targetAccount.
          * @see TransactionSum#myTargetAccounts
          */
         ONLYTO,
         /**
          * Sum only splits that decrease the balance on
          * the targetAccount.
          * @see TransactionSum#myTargetAccounts
          */
         ONLYFROM;
 
         /**
          * @param aProperty parse this string
          * @return and return the value that matches the name
          */
         public static SUMMATIONTYPE getByName(final String aProperty) {
             if (aProperty.equalsIgnoreCase("all")) {
                 return ALL;
             }
 
             if (aProperty.equalsIgnoreCase("to")) {
                 return ONLYTO;
             }
             if (aProperty.equalsIgnoreCase("onlyto")) {
                 return ONLYTO;
             }
 
             if (aProperty.equalsIgnoreCase("from")) {
                 return ONLYFROM;
             }
             if (aProperty.equalsIgnoreCase("onlyfrom")) {
                 return ONLYFROM;
             }
 
             return ALL;
         }
     }
 
     /**
      * The label that contains the sum.
      */
    private final JLabel mySumLabel = new JLabel();
     /**
      * The label that contains the name
      * to display left of the sum.
      */
    private final JLabel myNameLabel = new JLabel();
     /**
      * The label to display a link
      * for a drilldown to the list of
      * transactions covered.
      */
    private final JLabel myDrilldownLabel = new JLabel();
 
     /**
      * @param books The financial data we operate on.
      * @param summationType The type of summations we are to calculate.
      * @param targetAccounts We sum all transaction-splits that are to
      *                       one of these accounts.
      * @param sourceAccounts We only operate on transactions that
      *                       contain one of these accounts.
      * @param name the name to display left of the sum
      * @param minDate We ignore all transactions that are before this date.
      * @param maxDate We ignore all transactions that are after this date.
      * @throws JAXBException if we have issues with the XML-backend
      */
     public TransactionSum(final GnucashFile books,
             final Set<GnucashAccount> sourceAccounts,
             final Set<GnucashAccount> targetAccounts,
             final SUMMATIONTYPE summationType,
             final String name,
             final Date minDate,
             final Date maxDate) throws JAXBException {
         initializeUI(name);
         setBooks(books);
         setSummationType(summationType);
         setSourceAccounts(sourceAccounts);
         setTargetAccounts(targetAccounts);
         setMinDate(minDate);
         setMaxDate(maxDate);
     }
 
     /**
      * Do the actual calculation.
      * @throws JAXBException if we have issues with the XML-backend
      */
     private void reCalculate() throws JAXBException {
         if (getSummationType() == null
           || getSourceAccounts() == null
           || getTargetAccounts() == null
           || getSummationType() == null
           || getMinDate() == null
           || getMaxDate() == null
           || getBooks() == null) {
             mySumLabel.setText("---");
             return;
         }
 
         Set<GnucashAccount> sourceAccounts = new HashSet<GnucashAccount>(
                                    buildTransitiveClosure(getSourceAccounts()));
         Set<GnucashAccount> targetAccounts = new HashSet<GnucashAccount>(
                 buildTransitiveClosure(getTargetAccounts()));
         Set<String> targetAccountsIDs =new HashSet<String>();
         for (GnucashAccount targetAccount : targetAccounts) {
             targetAccountsIDs.add(targetAccount.getId());
         }
 
         ////////////////////////////////////
         // find all applicable transacion
         Set<GnucashTransactionSplit> transactions
                                        = new HashSet<GnucashTransactionSplit>();
         FixedPointNumber sum = new FixedPointNumber(0);
         for (GnucashAccount sourceAccount : sourceAccounts) {
             List<GnucashTransaction> sourceAccountTransactions
                                               = sourceAccount.getTransactions();
             for (GnucashTransaction trans : sourceAccountTransactions) {
                 if (trans.getDatePosted().before(getMinDate())
                    || trans.getDatePosted().after(getMaxDate())) {
                     continue;
                 }
 
                 Collection<? extends GnucashTransactionSplit> splits
                                                             = trans.getSplits();
                 for (GnucashTransactionSplit split : splits) {
                     if (targetAccountsIDs.contains(split.getAccountID())) {
 
                         //don't add a split twice
                         if (transactions.contains(split)) {
                             continue;
                         }
                         transactions.add(split);
                       //TODO: change to the currency of the first sourceAccount.
 
                         switch (getSummationType()) {
                         case ONLYFROM : if (split.getQuantity().isPositive()) {
                             continue;
                         }
                         case ONLYTO : if (!split.getQuantity().isPositive()) {
                             continue;
                         }
                         case ALL: //do nothing
                         default: //do nothing
                         }
 
                         sum = sum.add(split.getQuantity());
                         // do not break here. There may be more then one split
                         // that matches
                     }
                 }
             }
         }
 
         ////////////////////////////////////
         // set output
        Iterator<GnucashAccount> iterator = targetAccounts.iterator();
        if (iterator.hasNext()) {
            mySumLabel.setText(sum.toString() + ""
                    + iterator.next().getCurrencyID());
        } else {
            mySumLabel.setText("no account");
        }
     }
 
     /**
      * Build the transitive closure of a list of accounts
      * by adding all child-accounts.
      * @param accounts the account-list to walk
      * @return a set of all given accounts and all their child-accounts.
      */
     private Collection<? extends GnucashAccount> buildTransitiveClosure(
             final Collection<? extends GnucashAccount> accounts) {
 
         if (accounts.size() == 0) {
             return accounts;
         }
 
         Set<GnucashAccount> retval = new HashSet<GnucashAccount>(accounts);
 
         // TODO implement TransactionSum.buildTransitiveClosure
         for (GnucashAccount account : accounts) {
             Collection<? extends GnucashAccount> allChildren
                            = buildTransitiveClosure(account.getChildren());
             retval.addAll(allChildren);
         }
         return retval;
     }
 
     /**
      * Create the UI-components.
      * @param name the name to display left of the sum
      */
     public void initializeUI(final String name) {
         this.setLayout(new BorderLayout());
         myNameLabel.setText(name);
         mySumLabel.setText("...");
         myDrilldownLabel.setText("");//TODO: implement drilldown
         this.add(myNameLabel, BorderLayout.WEST);
         this.add(mySumLabel, BorderLayout.CENTER);
         this.add(myDrilldownLabel, BorderLayout.EAST);
     }
     //------------------------ support for propertyChangeListeners -------------
 
     /**
      * support for firing PropertyChangeEvents.
      * (gets initialized only if we really have listeners)
      */
     private volatile PropertyChangeSupport myPropertyChange = null;
 
     /**
      * Returned value may be null if we never had listeners.
      * @return Our support for firing PropertyChangeEvents
      */
     protected final PropertyChangeSupport getPropertyChangeSupport() {
         return myPropertyChange;
     }
 
     /**
      * Add a PropertyChangeListener to the listener list.
      * The listener is registered for all properties.
      *
      * @param listener  The PropertyChangeListener to be added
      */
    @Override
     public final void addPropertyChangeListener(
             final PropertyChangeListener listener) {
         if (myPropertyChange == null) {
             myPropertyChange = new PropertyChangeSupport(this);
         }
         myPropertyChange.addPropertyChangeListener(listener);
     }
 
     /**
      * Add a PropertyChangeListener for a specific property.  The listener
      * will be invoked only when a call on firePropertyChange names that
      * specific property.
      *
      * @param propertyName  The name of the property to listen on.
      * @param listener  The PropertyChangeListener to be added
      */
    @Override
     public final void addPropertyChangeListener(final String propertyName,
             final PropertyChangeListener listener) {
         if (myPropertyChange == null) {
             myPropertyChange = new PropertyChangeSupport(this);
         }
         myPropertyChange.addPropertyChangeListener(propertyName, listener);
     }
 
     /**
      * Remove a PropertyChangeListener for a specific property.
      *
      * @param propertyName  The name of the property that was listened on.
      * @param listener  The PropertyChangeListener to be removed
      */
    @Override
     public final void removePropertyChangeListener(final String propertyName,
             final PropertyChangeListener listener) {
         if (myPropertyChange != null) {
             myPropertyChange.removePropertyChangeListener(propertyName,
                     listener);
         }
     }
 
     /**
      * Remove a PropertyChangeListener from the listener list.
      * This removes a PropertyChangeListener that was registered
      * for all properties.
      *
      * @param listener  The PropertyChangeListener to be removed
      */
    @Override
     public final synchronized void removePropertyChangeListener(
             final PropertyChangeListener listener) {
         if (myPropertyChange != null) {
             myPropertyChange.removePropertyChangeListener(listener);
         }
     }
 
     //-------------------------------------------------------
 
     /**
      * @return Returns the books.
      * @see #myBooks
      */
     public GnucashFile getBooks() {
         return myBooks;
     }
 
     /**
      * @param aBooks The books to set.
      * @throws JAXBException if we have issues with the XML-backend
      * @see #myBooks
      */
     public void setBooks(final GnucashFile aBooks) throws JAXBException {
         if (aBooks == null) {
             throw new IllegalArgumentException("null 'aBooks' given!");
         }
 
         Object old = myBooks;
         if (old == aBooks) {
             return; // nothing has changed
         }
         myBooks = aBooks;
         // <<insert code to react further to this change here
         reCalculate();
         PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
         if (propertyChangeFirer != null) {
             propertyChangeFirer.firePropertyChange("aBooks", old, aBooks);
         }
     }
 
     /**
      * @return Returns the sourceAccounts.
      * @see #mySourceAccounts
      */
     public Set<GnucashAccount> getSourceAccounts() {
         return mySourceAccounts;
     }
 
     /**
      * @param aSourceAccounts The sourceAccounts to set.
      * @throws JAXBException if we have issues with the XML-backend
      * @see #mySourceAccounts
      */
     public void setSourceAccounts(final Set<GnucashAccount> aSourceAccounts) throws JAXBException {
         if (aSourceAccounts == null) {
             throw new IllegalArgumentException("null 'aSourceAccounts' given!");
         }
 
         Object old = mySourceAccounts;
         if (old == aSourceAccounts) {
             return; // nothing has changed
         }
         mySourceAccounts = aSourceAccounts;
         // <<insert code to react further to this change here
         reCalculate();
         PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
         if (propertyChangeFirer != null) {
             propertyChangeFirer.firePropertyChange("aSourceAccounts", old,
                     aSourceAccounts);
         }
     }
 
     /**
      * @return Returns the summationType.
      * @see #mySummationType
      */
     public SUMMATIONTYPE getSummationType() {
         return mySummationType;
     }
 
     /**
      * @param aSummationType The summationType to set.
      * @throws JAXBException if we have issues with the XML-backend
      * @see #mySummationType
      */
     public void setSummationType(final SUMMATIONTYPE aSummationType) throws JAXBException {
         if (aSummationType == null) {
             throw new IllegalArgumentException("null 'aSummationType' given!");
         }
 
         Object old = mySummationType;
         if (old == aSummationType) {
             return; // nothing has changed
         }
         mySummationType = aSummationType;
         // <<insert code to react further to this change here
         reCalculate();
         PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
         if (propertyChangeFirer != null) {
             propertyChangeFirer.firePropertyChange("aSummationType", old,
                     aSummationType);
         }
     }
 
     /**
      * @return Returns the targetAccounts.
      * @see #myTargetAccounts
      */
     public Set<GnucashAccount> getTargetAccounts() {
         return myTargetAccounts;
     }
 
     /**
      * @param aTargetAccounts The targetAccounts to set.
      * @throws JAXBException if we have issues with the XML-backend
      * @see #myTargetAccounts
      */
     public void setTargetAccounts(final Set<GnucashAccount> aTargetAccounts) throws JAXBException {
         if (aTargetAccounts == null) {
             throw new IllegalArgumentException("null 'aTargetAccounts' given!");
         }
 
         Object old = myTargetAccounts;
         if (old == aTargetAccounts) {
             return; // nothing has changed
         }
         myTargetAccounts = aTargetAccounts;
         // <<insert code to react further to this change here
         reCalculate();
         PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
         if (propertyChangeFirer != null) {
             propertyChangeFirer.firePropertyChange("aTargetAccounts", old,
                     aTargetAccounts);
         }
     }
 
     /**
      * @return Returns the minDate.
      * @see #myMinDate
      */
     public Date getMinDate() {
         return myMinDate;
     }
 
     /**
      * @param aMinDate The minDate to set.
      * @throws JAXBException if we have issues with the XML-backend
      * @see #myMinDate
      */
     public void setMinDate(final Date aMinDate) throws JAXBException {
         if (aMinDate == null) {
             throw new IllegalArgumentException("null 'aMinDate' given!");
         }
 
         Object old = myMinDate;
         if (old == aMinDate) {
             return; // nothing has changed
         }
         myMinDate = aMinDate;
         // <<insert code to react further to this change here
         reCalculate();
         PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
         if (propertyChangeFirer != null) {
             propertyChangeFirer.firePropertyChange("aMinDate", old, aMinDate);
         }
     }
 
     /**
      * @return Returns the maxDate.
      * @see #myMaxDate
      */
     public Date getMaxDate() {
         return myMaxDate;
     }
 
     /**
      * @param aMaxDate The maxDate to set.
      * @throws JAXBException if we have issues with the XML-backend
      * @see #myMaxDate
      */
     public void setMaxDate(final Date aMaxDate) throws JAXBException {
         if (aMaxDate == null) {
             throw new IllegalArgumentException("null 'aMaxDate' given!");
         }
 
         Object old = myMaxDate;
         if (old == aMaxDate) {
             return; // nothing has changed
         }
         myMaxDate = aMaxDate;
         // <<insert code to react further to this change here
         reCalculate();
         PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
         if (propertyChangeFirer != null) {
             propertyChangeFirer.firePropertyChange("aMaxDate", old, aMaxDate);
         }
     }
 }
 
 
