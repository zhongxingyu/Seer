 package com.mns.mojoinvest.server.engine.portfolio;
 
 import com.mns.mojoinvest.server.engine.model.Fund;
 import com.mns.mojoinvest.server.engine.model.Quote;
 import com.mns.mojoinvest.server.engine.model.dao.QuoteDao;
 import com.mns.mojoinvest.server.engine.transaction.BuyTransaction;
 import com.mns.mojoinvest.server.engine.transaction.SellTransaction;
 import com.mns.mojoinvest.server.engine.transaction.Transaction;
 import org.joda.time.LocalDate;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * A position is a collection of information about a security that the user holds.
  * <p/>
  * A position feed lists all of the positions in a particular portfolio. Each position entry
  * contains the ticker exchange and symbol for a stock, mutual fund, or other security, along
  * with the number of units of that security that the user holds. Each position entry also
  * contains a link to the position's transaction feed.
  * <p/>
  * You can't directly create, update, or delete position entries; positions are derived from
  * transactions. For example, a GOOG position is derived from the buy and sell transactions
  * for GOOG. Google Finance updates the position every time it receives a transaction for that
  * position.
  * <p/>
  * Therefore, to create or modify a position, send appropriate transactions on that position.
  * To delete a position, delete all of its transactions.
  * <p/>
  * Selling all of the shares in a position does not delete the position; it just sets the
  * position's value to zero.
  */
 public class Position {
 
     private QuoteDao quoteDao;
 
     private Fund fund;
 
     private List<Lot> lots;
 
     private final List<Transaction> transactions;
 
     public Position(QuoteDao quoteDao, Fund fund) {
         this.quoteDao = quoteDao;
         this.fund = fund;
         this.lots = new ArrayList<Lot>();
         this.transactions = new ArrayList<Transaction>();
     }
 
     public Fund getFund() {
         return fund;
     }
 
     private Quote getQuote(LocalDate date) {
         //TODO: cache here?
         return quoteDao.get(fund, date);
     }
 
     public List<Lot> getLots() {
         return lots;
     }
 
 
     public List<Transaction> getTransactions() {
         return transactions;
     }
 
     public void add(Transaction transaction) throws PortfolioException {
         if (transaction instanceof BuyTransaction) {
             add((BuyTransaction) transaction);
         } else if (transaction instanceof SellTransaction) {
             add((SellTransaction) transaction);
         } else {
             throw new PortfolioException("Unrecognised transaction type");
         }
     }
 
     public void add(BuyTransaction transaction) throws PortfolioException {
         if (!fund.equals(transaction.getFund())) {
             throw new PortfolioException("Attempt to add a " + transaction.getFund() +
                     " transaction to a " + fund + " position");
         }
         transactions.add(transaction);
         lots.add(new Lot(transaction));
     }
 
     public void add(SellTransaction transaction) throws PortfolioException {
         if (!fund.equals(transaction.getFund())) {
             throw new PortfolioException("Attempt to add a " + transaction.getFund() +
                     " transaction to a " + fund + " position");
         }
         if (!saleIsValid(transaction)) {
             throw new PortfolioException(this + " is not large enough to be able " +
                     "to meet sale " + transaction);
         }
         transactions.add(transaction);
         updateLots(transaction);
     }
 
 
     private boolean saleIsValid(Transaction transaction) {
         BigDecimal openPosition = BigDecimal.ZERO;
         for (Lot lot : lots) {
             openPosition = openPosition.add(lot.getRemainingQuantity(transaction.getDate()));
         }
         return openPosition.compareTo(transaction.getQuantity()) != -1;
     }
 
     public void updateLots(SellTransaction transaction)
             throws PortfolioException {
 
         for (Lot lot : lots) {
             BigDecimal remainingQuantity = lot.getRemainingQuantity(transaction.getDate());
             BigDecimal difference = remainingQuantity.subtract(transaction.getQuantity());
             if (difference.compareTo(BigDecimal.ZERO) < 0) {
 
                 //Commission is split between the virtual transactions
                 BigDecimal commission = transaction.getCommission().divide(new BigDecimal("2"));
 
                 SellTransaction closingTransaction = new SellTransaction(transaction.getFund(),
                         transaction.getDate(), remainingQuantity,
                         transaction.getPrice(), commission);
 
                 lot.addClosingTransaction(closingTransaction);
 
                 transaction = new SellTransaction(transaction.getFund(),
                         transaction.getDate(), difference.negate(), transaction.getPrice(),
                         commission);
 
             } else {
                 lot.addClosingTransaction(transaction);
                 break;
             }
         }
     }
 
     public BigDecimal shares(LocalDate date) {
         BigDecimal shares = BigDecimal.ZERO;
         for (Lot lot : lots) {
             shares = shares.add(lot.getRemainingQuantity(date));
         }
         return shares;
     }
 
     //TODO: Should we invert logic to match lot.closed()?
     public boolean open(LocalDate date) {
         return shares(date).compareTo(BigDecimal.ZERO) > 0;
     }
 
     /*
     The lot calculations are by far the trickiest part of entire process. Once that step is done, the summary values
     for each security are calculated. These are the values that appear in each row under the Performance tab. First,
     cost basis, market value, gain, and todays gain are all computed as the sum of the corresponding values of all the
     lots for a security.
     */
 
     public BigDecimal costBasis(LocalDate date) {
         BigDecimal costBasis = BigDecimal.ZERO;
         for (Lot lot : lots) {
             if (!lot.openedAfter(date)) {
                 costBasis = costBasis.add(lot.costBasis(date));
             }
         }
         return costBasis;
     }
 
     public BigDecimal cashOut(LocalDate date) {
         BigDecimal cashOut = BigDecimal.ZERO;
         for (Lot lot : lots) {
             if (!lot.openedAfter(date)) {
                 cashOut = cashOut.add(lot.cashOut());
             }
         }
         return cashOut;
     }
 
     public BigDecimal cashIn(LocalDate date) {
         BigDecimal cashIn = BigDecimal.ZERO;
         for (Lot lot : lots) {
             cashIn = cashIn.add(lot.cashIn(date));
         }
         return cashIn;
     }
 
     public BigDecimal marketValue(LocalDate date) {
         BigDecimal marketValue = BigDecimal.ZERO;
         for (Lot lot : lots) {
             if (!lot.openedAfter(date)) {
                marketValue = marketValue.add(lot.marketValue(date, getQuote(date).getClose()));
             }
         }
         return marketValue;
     }
 
     public BigDecimal gain(LocalDate date) {
         BigDecimal gain = BigDecimal.ZERO;
         for (Lot lot : lots) {
             if (!lot.openedAfter(date)) {
                gain = gain.add(lot.gain(date, getQuote(date).getClose()));
             }
         }
         return gain;
     }
 
     /*
      * Then the gain percentage is calculated by:
      *  gain percentage = gain / cost basis
      */
     public BigDecimal gainPercentage(LocalDate date) {
         return gain(date).divide(costBasis(date), MathContext.DECIMAL32)
                 //multiply by 100 for percentage
                 .multiply(BigDecimal.TEN.multiply(BigDecimal.TEN));
     }
 
 
     public BigDecimal returnsGain(LocalDate date) {
         BigDecimal returnsGain = BigDecimal.ZERO;
         for (Lot lot : lots) {
             if (!lot.openedAfter(date)) {
                returnsGain = returnsGain.add(lot.returnsGain(date, getQuote(date).getClose()));
             }
         }
         return returnsGain;
     }
 
     /*
      * The total return for each security is calculated similarly: Returns gain and cash out are summed over all the
      * lots for the security, then the total return is calculated by:
      *  total return = returns gain / cash out
      */
     public BigDecimal totalReturn(LocalDate date) {
         return returnsGain(date).divide(cashOut(date).negate(), MathContext.DECIMAL32)
                 //multiply by 100 for percentage
                 .multiply(BigDecimal.TEN.multiply(BigDecimal.TEN));
     }
 }
