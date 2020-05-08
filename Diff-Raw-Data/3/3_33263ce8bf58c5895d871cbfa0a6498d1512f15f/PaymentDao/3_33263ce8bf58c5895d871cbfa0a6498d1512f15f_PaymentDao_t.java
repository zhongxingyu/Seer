 package eu.comexis.napoleon.server.dao;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Query;
 import com.googlecode.objectify.util.DAOBase;
 
 import eu.comexis.napoleon.shared.model.Company;
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
     List<T> payments = q.list();
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
   public T update(T payment,String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return update(payment,companyKey);
   }
   public T update(T payment, Key<Company> companyKey) {
     LOG.info("Update PaymentOwner");
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
         return null;
       }
     }else{
       leaseKey =payment.getLeaseKey();
     }
     T actualPayment =isAlreadyPaid(payment.getId(),leaseKey,payment.getPeriodStartDate(),payment.getPeriodEndDate());
     if (actualPayment!=null){
       LOG.info("Payment cannot be updated because there is already a payment for that period " + actualPayment.getId());
       return null;
     }
     try {
       Key<T> paymentKey = ofy().put(payment);
       LOG.info("Payment has been updated");
       Lease l = ofy().get(payment.getLeaseKey());
       RealEstate e = ofy().get(l.getRealEstateKey());
       T pt = ofy().get(paymentKey);
       pt.setEstateId(e.getId());
       pt.setLeaseId(l.getId());
       return pt;
     } catch (Exception e) {
       LOG.fatal("Payment cannot be updated: ", e);
       return null;
     }
   }
   public List<PaymentListItem> getPaymentDashboardForLease(String leaseId, String realEstateId, String companyId){
     DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy/MM/dd");
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
     Key<Lease> leaseKey = new Key<Lease>(estateKey,Lease.class, leaseId);
     Lease lease = ofy().get(leaseKey);
     RealEstate estate = ofy().get(estateKey);
     Owner owner = ofy().get(estate.getOwnerKey());
     Query<PaymentTenant> qpt = ofy().query(PaymentTenant.class);
     qpt.ancestor(leaseKey);
     Query<PaymentOwner> qpo = ofy().query(PaymentOwner.class);
     qpo.ancestor(leaseKey);
     HashMap<String,PaymentListItem> allPayments = new HashMap<String,PaymentListItem>();
     String sDate = "";
     for (PaymentTenant pt:qpt.list()){
       PaymentListItem item = null;
       sDate = DATE_FORMAT.format(pt.getPeriodEndDate());
       if (allPayments.containsKey(sDate)){
         item = allPayments.get(sDate);
       }else{
         item = new PaymentListItem();
         allPayments.put(sDate, item);
       }
       item.setFromDate(pt.getPeriodStartDate());
       item.setToDate(pt.getPeriodEndDate());
       item.setRent(lease.getRent());
       item.setFee(owner.getFee().floatValue());
     }
     return null;
   }
   public PaymentOwner getNextPaymentOwnerForLease(String leaseId, String realEstateId, String companyId){
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
     Key<Lease> leaseKey = new Key<Lease>(estateKey,Lease.class, leaseId);
     Lease lease = ofy().get(leaseKey);
     RealEstate estate = ofy().get(estateKey);
     Owner owner = ofy().get(estate.getOwnerKey());
     Query<PaymentOwner> q = ofy().query(PaymentOwner.class);
     q.ancestor(leaseKey);
     if (lease.getRent()==null || owner.getFee()==null){
       return null;
     }
     PaymentOwner lastpo = null;
     for (PaymentOwner po:q.list()){
       if (lastpo==null){
         lastpo = po;
       }
       if (lastpo!=null && po.getPeriodEndDate().after(lastpo.getPeriodEndDate())){
         lastpo = po;
       }
     }
     Query<PaymentTenant> q2 = ofy().query(PaymentTenant.class);
     q2.ancestor(leaseKey);
     PaymentTenant lastpt = null;
     PaymentTenant firstpt = null;
     for (PaymentTenant pt:q2.list()){
       if (lastpt==null){
         lastpt = pt;
       }
       if (firstpt==null){
         firstpt = pt;
       }
       if (lastpt!=null && pt.getPeriodEndDate().after(lastpt.getPeriodEndDate())){
         lastpt = pt;
       }
       if (firstpt!=null && pt.getPeriodStartDate().before(firstpt.getPeriodStartDate())){
         firstpt = pt;
       }
     }
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
         return null;
       }
     }
     if (lastpt!=null){
       nextPaymentOwner.setPeriodEndDate(lastpt.getPeriodEndDate());
     }else{
       // si pas de paiement du locataire, alors pas de paiement proprio. Théoriquement on ne devrait pas passer dans ce code.
       return null;
     }
    if (nextPaymentOwner.getPeriodEndDate().before(nextPaymentOwner.getPeriodStartDate())){
      return null;
    }
     // calcul du montant des loyers perçu dans la période et du nombre de périodes de loyer.
     // on prend toutes les perceptions qui sont dans la période
     Float sum = new Float("0");
     Integer nbrPeriod = 0;
     q2.filter("periodStartDate >=", nextPaymentOwner.getPeriodStartDate());
     for (PaymentTenant pt:q2.list()){
       sum +=pt.getAmount();
       nbrPeriod +=1; // todo, ne pas compter les periodes identiques.
     }
     // calcul du paiement
     // si le proprio prend un %
     Float dueToOwner = new Float("0");
     nextPaymentOwner.setFeeUnit(owner.getUnit());
     nextPaymentOwner.setFee(owner.getFee().floatValue());
     if (owner.getUnit().equals(FeeUnit.RENT_PERCENTAGE)){
       dueToOwner = sum * (100 - owner.getFee().floatValue())/100;
     }else{
       dueToOwner = nbrPeriod * owner.getFee().floatValue();
     }
     nextPaymentOwner.setRentWithoutFee(dueToOwner);
     // Calcul du solde (ajoute de l'ancien solde)
     if (lastpo!=null){
       nextPaymentOwner.setPreviousbalance(lastpo.getBalance());
       dueToOwner += lastpo.getBalance();
     }
     nextPaymentOwner.setBalance(dueToOwner);
     nextPaymentOwner.setLeaseKey(leaseKey);
     nextPaymentOwner.setEstateId(realEstateId);
     nextPaymentOwner.setLeaseId(leaseId);
     return nextPaymentOwner;
   }
   public PaymentTenant getNextPaymentTenantForLease(String leaseId, String realEstateId, String companyId){
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
     Key<Lease> leaseKey = new Key<Lease>(estateKey,Lease.class, leaseId);
     Lease lease = ofy().get(leaseKey);
     RealEstate estate = ofy().get(estateKey);
     PaymentTenant nextPaymentTenant = new PaymentTenant();
     Query<PaymentTenant> q2 = ofy().query(PaymentTenant.class);
     q2.ancestor(leaseKey);
     PaymentTenant lastpt = null;
     if (lease.getRent()==null){
       return null;
     }
     // chercher le dernier loyer percu
     for (PaymentTenant pt:q2.list()){
       if (lastpt==null){
         lastpt = pt;
       }
       if (pt.getPeriodEndDate().after(lastpt.getPeriodEndDate())){
         lastpt = pt;
       }
     }
     Calendar cal = Calendar.getInstance();
     // si pas encore de perception
     if (lastpt==null){
       nextPaymentTenant.setPeriodStartDate(lease.getStartDate());
     }else{
       cal.setTime(lastpt.getPeriodEndDate());
       cal.add(Calendar.DAY_OF_MONTH, 1);
       nextPaymentTenant.setPeriodStartDate(cal.getTime());
     }
     nextPaymentTenant.setAmount(lease.getRent());
     // on ajoute 1 mois à la date de début
     cal.setTime(nextPaymentTenant.getPeriodStartDate());
     cal.add(Calendar.DAY_OF_MONTH, 28);
     nextPaymentTenant.setPeriodEndDate(cal.getTime());    
     nextPaymentTenant.setLeaseKey(leaseKey);
     nextPaymentTenant.setEstateId(realEstateId);
     nextPaymentTenant.setLeaseId(leaseId);
     return nextPaymentTenant;
   }
 }
