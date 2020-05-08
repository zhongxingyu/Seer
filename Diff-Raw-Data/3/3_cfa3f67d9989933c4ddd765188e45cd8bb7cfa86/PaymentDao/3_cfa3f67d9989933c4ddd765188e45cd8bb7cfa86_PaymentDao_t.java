 package eu.comexis.napoleon.server.dao;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 import com.googlecode.objectify.util.DAOBase;
 
 import eu.comexis.napoleon.server.utils.NapoleonDaoException;
 import eu.comexis.napoleon.shared.model.Company;
 import eu.comexis.napoleon.shared.model.Expense;
 import eu.comexis.napoleon.shared.model.FeeUnit;
 import eu.comexis.napoleon.shared.model.Lease;
 import eu.comexis.napoleon.shared.model.Owner;
 import eu.comexis.napoleon.shared.model.Payment;
 import eu.comexis.napoleon.shared.model.PaymentOwner;
 import eu.comexis.napoleon.shared.model.PaymentTenant;
 import eu.comexis.napoleon.shared.model.RealEstate;
 import eu.comexis.napoleon.shared.model.simple.PaymentListItem;
 
 public class PaymentDao<T extends Payment> extends DAOBase{
   protected Class<T> clazz;
   static {
     ObjectifyService.register(Payment.class);
     ObjectifyService.register(PaymentOwner.class);
     ObjectifyService.register(PaymentTenant.class);
   }
   public Class<T> getClazz() {
     return clazz;
   }
   public void setClazz(Class<T> clazz) {
     this.clazz = clazz;
   }
   public PaymentDao() {
   }
   public List<T> getPaymentsForLease(String leaseId, String realEstateId, String companyId){
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
     Key<Lease> leaseKey = new Key<Lease>(estateKey,Lease.class, leaseId);
     LOG.info("Get list payment (" + clazz + ") for lease " + leaseId);
     Query<T> q = ofy().query(clazz);
     q.ancestor(leaseKey);
     List<T> payments = q.order("periodEndDate").list();
     return payments;
   }
   public static Log LOG = LogFactory.getLog(PaymentDao.class);
 
 
   public T getById(String paymentId, String leaseId, String realEstateId, String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
     Key<Lease> leaseKey = new Key<Lease>(estateKey,Lease.class, leaseId);
     T p = ofy().find(new Key<T>(leaseKey, clazz, paymentId));
     p.setLeaseId(leaseId);
     p.setEstateId(realEstateId);
     return p;
   }
   public List<T> getPaymentForLease(Key<Lease> leaseKey) {
     // get the lease for the company
     Query<T> ql = ofy().query(clazz);
     ql.ancestor(leaseKey);
     return ql.list();
   }
   public T isAlreadyPaid(String id,Key<Lease> leaseKey, Date startDate, Date endDate) {
     Query<T> q = ofy().query(clazz);
     q.ancestor(leaseKey);
     List<T> payments = q.filter("periodEndDate >=", startDate).order("periodEndDate").list();
     for (T p:payments){
       if (!p.getId().equals(id) && p.getPeriodStartDate().compareTo(endDate) <= 0){
         return p;
       }
     }
     return null;
   }
   public T update(T payment,String companyId) throws NapoleonDaoException {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return update(payment,companyKey);
   }
   public T update(T payment, Key<Company> companyKey) throws NapoleonDaoException {
     LOG.info("Update Payment");
       String paymentId = payment.getId();
       Key<Lease> leaseKey = null;
       Key<RealEstate> estateKey = null;
       // create unique id if new entity
       
       if (paymentId == null || paymentId.length() == 0) {
         UUID uuid = UUID.randomUUID();
         System.out.println("Creating Uuid " + uuid.toString());
         payment.setId(uuid.toString());
       }
       // set parent
       if (payment.getLeaseKey() == null) {
         if (payment.getLeaseId() != null && payment.getEstateId() != null) {
           estateKey = new Key<RealEstate>(companyKey,RealEstate.class, payment.getEstateId());
           leaseKey =
               new Key<Lease>(estateKey,Lease.class, payment.getLeaseId());
           payment.setLeaseKey(leaseKey);
           
         } else {
           LOG.fatal("Lease cannot be updated, missing parent RealEstate");
           throw new NapoleonDaoException("Oups, problème:-(");
         }
       }else{
         leaseKey =payment.getLeaseKey();
       }
       T actualPayment =isAlreadyPaid(payment.getId(),leaseKey,payment.getPeriodStartDate(),payment.getPeriodEndDate());
       if (actualPayment!=null){
         LOG.info("Payment cannot be updated because there is already a payment for that period " + actualPayment.getId());
         throw new NapoleonDaoException("Il y a déjà un paiement sur cette période");
       }
       // force all the time to be at noon to avoid CEST-GMT problem
       payment.setPaymentDate(setTime(payment.getPaymentDate()));
       payment.setPeriodStartDate(setTime(payment.getPeriodStartDate()));
       payment.setPeriodEndDate(setTime(payment.getPeriodEndDate()));
       Key<T> paymentKey = ofy().put(payment);
       LOG.info("Payment has been updated");
       Lease l = ofy().get(payment.getLeaseKey());
       estateKey=l.getRealEstateKey();
       RealEstate e = ofy().get(l.getRealEstateKey());
       T pt = ofy().get(paymentKey);
       pt.setEstateId(e.getId());
       pt.setLeaseId(l.getId());
       if (pt.getClass().equals(PaymentOwner.class)){
         PaymentOwner po = (PaymentOwner)pt;
         Query<Expense> q = ofy().query(Expense.class);
         q.ancestor(estateKey);
         for (Expense exp: getExpensesToBeCharged(payment.getEstateId(),companyKey)){
           if (exp.getDateInvoice()!=null){
             exp.setChargedToOwnerPeriod(po.getPeriodEndDate());
             ofy().put(exp);
           }
         }
       }
       return pt;
   }
   private Date setTime(Date date){
     if (date!=null){
       Calendar cal = Calendar.getInstance();
       cal.setTime(date);
       cal.add(Calendar.HOUR_OF_DAY, 12);
       cal.set(Calendar.HOUR_OF_DAY, 12);
       date = cal.getTime();
     }
     return date;
   }
   private Float getFee(Float rent, Float fee, FeeUnit unit){
     if (unit.equals(FeeUnit.RENT_PERCENTAGE)){
       return rent * fee/100;
     }else{
       return fee;
     }
   }
   public List<PaymentListItem> getPaymentDashboardForLease(String leaseId, String realEstateId, String companyId){
     LOG.info("Generate payments board");
     try{
       DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
       Key<Company> companyKey = new Key<Company>(Company.class, companyId);
       Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
       Key<Lease> leaseKey = new Key<Lease>(estateKey,Lease.class, leaseId);
       
       String sDate = "";
       PaymentListItem item = null;
       PaymentListItem itemPo = null;
       
       HashMap<String,PaymentListItem> allPaymentsProprio = new HashMap<String,PaymentListItem>();
       Query<PaymentOwner> qpo = ofy().query(PaymentOwner.class);
       qpo.ancestor(leaseKey);
       for (PaymentOwner po: qpo.order("periodEndDate").list()){
         sDate = dateFormat.format(po.getPeriodEndDate());
         LOG.info("Paiement proprio: " + sDate);
         item = new PaymentListItem();
         item.setPaymentOwnerDate(po.getPaymentDate());
         item.setPaidToOwner(po.getAmount()!=null?po.getAmount():0f);
         item.setBalance(po.getBalance()!=null?po.getBalance():0f);
         item.setToBePaidToOwner(item.getPaidToOwner()+ item.getBalance());
         //item.setToBePaidToOwner(po.getRentWithoutFee() + (po.getPreviousbalance()!=null?po.getPreviousbalance():0f));
         item.setExpenses(po.getExpense()!=null?po.getExpense():0f);
         allPaymentsProprio.put(sDate, item);
         
       }
       
       HashMap<String,PaymentListItem> allPayments = new HashMap<String,PaymentListItem>();
       Query<PaymentTenant> qpt = ofy().query(PaymentTenant.class);
       qpt.ancestor(leaseKey);
       
       Float balance = 0f;
       Float fee = 0f;
       ArrayList<String> orderedDateList = new ArrayList<String>();
       for (PaymentTenant pt:qpt.order("periodEndDate").list()){
         sDate = dateFormat.format(pt.getPeriodEndDate());
         orderedDateList.add(sDate); // keep date in order
         LOG.info("Paiement loyer: " + sDate);
         fee = getFee(pt.getAmount(),pt.getFee(),pt.getFeeUnit());
         balance +=pt.getAmount() - fee;
         item = new PaymentListItem();
         item.setFromDate(pt.getPeriodStartDate());
         item.setToDate(pt.getPeriodEndDate());
         item.setPaymentTenantDate(pt.getPaymentDate());
         item.setRent(pt.getAmount());
         item.setFee(getFee(pt.getAmount(),pt.getFee(),pt.getFeeUnit()));
         item.setToBePaidToOwner(balance);
         itemPo = allPaymentsProprio.get(sDate);
         item.setBalance(balance);
         item.setPaidToOwner(0f);
         if (itemPo!=null){
           item.setPaymentOwnerDate(itemPo.getPaymentOwnerDate());
           item.setPaidToOwner(itemPo.getPaidToOwner());
           item.setToBePaidToOwner(itemPo.getToBePaidToOwner());
           item.setBalance(itemPo.getBalance());
           item.setExpenses(itemPo.getExpenses());
           balance = itemPo.getBalance();
         }
         allPayments.put(sDate, item);
       }
       // update the last record with the remaining expense to be charged to owner
       item = allPayments.get(sDate);
       if (item!=null){
         Float totExp = 0f;
         for (Expense e: getExpensesToBeCharged(realEstateId,companyId)){
           if (e.getToBePaidByOwner()!=null){
             totExp+=e.getToBePaidByOwner();
           }
         }
         item.setExpenses((item.getExpenses()!=null ? item.getExpenses():0f) + totExp);
         item.setBalance(item.getBalance() - totExp);
         item.setToBePaidToOwner(item.getBalance() + item.getPaidToOwner());
         allPayments.put(sDate, item);
       }
       
       List<PaymentListItem> paymentDashboard = new ArrayList<PaymentListItem>();
       /*for (PaymentListItem itm: allPayments.values()){
         paymentDashboard.add(itm);
       }*/
       for (String sDte:orderedDateList){
         paymentDashboard.add(allPayments.get(sDte));
       }
       LOG.info("Board is generated");
       return paymentDashboard;
     } catch (Exception e) {
       LOG.fatal("Cannot build payments board: ", e);
       return null;
     }  
   }
   public PaymentOwner getNextPaymentOwnerForLease(String leaseId, String realEstateId, String companyId) throws NapoleonDaoException{
       Key<Company> companyKey = new Key<Company>(Company.class, companyId);
       Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
       Key<Lease> leaseKey = new Key<Lease>(estateKey,Lease.class, leaseId);
       Lease lease = ofy().get(leaseKey);
       RealEstate estate = ofy().get(estateKey);
       Owner owner = ofy().get(estate.getOwnerKey());
       Query<PaymentOwner> q = ofy().query(PaymentOwner.class);
       q.ancestor(leaseKey);
       // pas de possibilité de calcul si pas d'honoraire ni de loyer
       if (lease.getRent()==null || owner.getFee()==null){
         return null;
       }
       // recherche du dernier versement proprio
       PaymentOwner lastpo = q.order("-periodEndDate").get();
       // recherche du premier et du dernier payment de loyer
       Query<PaymentTenant> q2 = ofy().query(PaymentTenant.class);
       q2.ancestor(leaseKey);
       PaymentTenant lastpt = q2.order("-periodEndDate").get();
       q2 = ofy().query(PaymentTenant.class);
       q2.ancestor(leaseKey);
       PaymentTenant firstpt = q2.order("periodStartDate").get();
       PaymentOwner nextPaymentOwner = new PaymentOwner();
       if (lastpo!=null){
         Calendar cal = Calendar.getInstance();
         cal.setTime(lastpo.getPeriodEndDate());
         cal.add(Calendar.DAY_OF_MONTH, 1);
         nextPaymentOwner.setPeriodStartDate(cal.getTime());
       }else{
         if (firstpt!=null){
           nextPaymentOwner.setPeriodStartDate(firstpt.getPeriodStartDate());
         }else{
           // si pas de paiement du locataire, alors pas de paiement proprio
           //noPaymentToPayToOwner
           throw new NapoleonDaoException("Il n'y a pas encore de paiement locataire à verser au propriétaire");
         }
       }
       if (lastpt!=null){
         nextPaymentOwner.setPeriodEndDate(lastpt.getPeriodEndDate());
       }else{
         // si pas de paiement du locataire, alors pas de paiement proprio. Théoriquement on ne devrait pas passer dans ce code.
         //noPaymentToPayToOwner
         throw new NapoleonDaoException("Il n'y a pas encore de paiement locataire à verser au propriétaire");
       }
       if (nextPaymentOwner.getPeriodEndDate().before(nextPaymentOwner.getPeriodStartDate())){
         //noPaymentToPayToOwner
         throw new NapoleonDaoException("Il n'y a pas encore de paiement locataire à verser au propriétaire");
       }
       // calcul du montant des loyers perçu dans la période et du nombre de périodes de loyer.
       // on prend toutes les perceptions qui sont dans la période
       Float sum = new Float("0");
       Long nbrPeriod = 0L;
       q2 = ofy().query(PaymentTenant.class);
       q2.ancestor(leaseKey);
       q2.filter("periodStartDate >=", nextPaymentOwner.getPeriodStartDate());
       for (PaymentTenant pt:q2.list()){
         sum +=pt.getAmount();
         // ajust the fee and rent to the actual values, if owner convention is changed.
         if (pt.getFee()==null || pt.getFeeUnit()==null || !pt.getFee().equals(owner.getFee().floatValue()) || !pt.getFeeUnit().equals(owner.getUnit())){
           // fee changed since last write
           // update the fee
           pt.setFee(owner.getFee().floatValue());
           pt.setFeeUnit(owner.getUnit());
           ofy().put(pt);
         }
         nbrPeriod +=1; // todo, ne pas compter les periodes identiques.
       }
       // calcul du paiement
       // si le proprio prend un %
       Float dueToOwner = new Float("0");
       nextPaymentOwner.setFeeUnit(owner.getUnit());
       nextPaymentOwner.setFee(owner.getFee().floatValue());
       if (owner.getUnit().equals(FeeUnit.RENT_PERCENTAGE)){
         dueToOwner = sum * ((100 - owner.getFee().floatValue())/100);
       }else{
         dueToOwner = sum - (nbrPeriod * owner.getFee().floatValue());
       }
       nextPaymentOwner.setRentWithoutFee(dueToOwner);
       // Calcul du solde (ajoute de l'ancien solde) - dépenses
       Float totExp = 0f;
       for (Expense e: getExpensesToBeCharged(realEstateId,companyId)){
           totExp+=e.getToBePaidByOwner();
       }
       nextPaymentOwner.setExpense(totExp);
       if (lastpo!=null){
         nextPaymentOwner.setPreviousbalance(lastpo.getBalance());
         dueToOwner += lastpo.getBalance();
       }
       dueToOwner -= totExp;
       nextPaymentOwner.setBalance(dueToOwner);
       nextPaymentOwner.setLeaseKey(leaseKey);
       nextPaymentOwner.setEstateId(realEstateId);
       nextPaymentOwner.setLeaseId(leaseId);
       nextPaymentOwner.setRent(sum);
       nextPaymentOwner.setNbrPeriod(nbrPeriod);
       
       return nextPaymentOwner;
 
   }
   public List<Expense> getExpensesToBeCharged(String realEstateId,Key<Company> companyKey) {
     Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
     Query<Expense> q = ofy().query(Expense.class);
     q.ancestor(estateKey);
     ArrayList<Expense> lstExpToCharge = new ArrayList<Expense>();
     for (Expense e: q.filter("chargedToOwnerPeriod", null).list()){
       if (e.getDateInvoice()!=null){
         lstExpToCharge.add(e);
       }
     }
     return lstExpToCharge;
   }
   public List<Expense> getExpensesToBeCharged(String realEstateId, String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return getExpensesToBeCharged(realEstateId,companyKey);
   }
   public PaymentTenant getNextPaymentTenantForLease(String leaseId, String realEstateId, String companyId) throws NapoleonDaoException{
       Key<Company> companyKey = new Key<Company>(Company.class, companyId);
       Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
       Key<Lease> leaseKey = new Key<Lease>(estateKey,Lease.class, leaseId);
       Lease lease = ofy().get(leaseKey);
       RealEstate estate = ofy().get(estateKey);
       Owner owner = ofy().get(estate.getOwnerKey());
       PaymentTenant nextPaymentTenant = new PaymentTenant();
       Query<PaymentTenant> q2 = ofy().query(PaymentTenant.class);
       q2.ancestor(leaseKey);
       
       if (lease.getRent()==null){
         //Oups
         throw new NapoleonDaoException();
       }
       // chercher le dernier loyer percu
       PaymentTenant lastpt = q2.order("-periodEndDate").get();
       Calendar cal = Calendar.getInstance();
       // si pas encore de perception
       if (lastpt==null){
         nextPaymentTenant.setPeriodStartDate(lease.getStartDate());
       }else{
         cal.setTime(lastpt.getPeriodEndDate());
         cal.add(Calendar.DAY_OF_MONTH, 1);
         nextPaymentTenant.setPeriodStartDate(cal.getTime());
       }
       // si la date de début est supérieur à la date de fin de bail, alors, plus de payement possible.
       if (nextPaymentTenant.getPeriodStartDate().after(lease.getEndDate())){
         //noMorePayments
         throw new NapoleonDaoException("Tous les paiements ont déjà été enregistrés pour cette location");
       }
       
       nextPaymentTenant.setAmount(lease.getRent());
       // on se place a la fin du mois courant (on se place au debut du mois suivant et on retire un jour)
       cal.setTime(nextPaymentTenant.getPeriodStartDate());
       cal.add(Calendar.MONTH, 1);
       cal.add(Calendar.DAY_OF_MONTH, -1);
       nextPaymentTenant.setPeriodEndDate(cal.getTime()); 
       // si la date de fin de période est supérieur à celle du bail, on ramène cette date de fin à la fin du bail
       // cas des baux inférieurs à 1 mois
       if (nextPaymentTenant.getPeriodEndDate().after(lease.getEndDate())){
         nextPaymentTenant.setPeriodEndDate(lease.getEndDate());  
       }
       nextPaymentTenant.setLeaseKey(leaseKey);
       nextPaymentTenant.setEstateId(realEstateId);
       nextPaymentTenant.setLeaseId(leaseId);
       // keep the values from lease and owner convention
       nextPaymentTenant.setRent(lease.getRent());
       nextPaymentTenant.setFee(owner.getFee() != null ? owner.getFee().floatValue() : 0);
       nextPaymentTenant.setFeeUnit(owner.getUnit());
       return nextPaymentTenant;
   }
 }
