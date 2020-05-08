 package org.silix.the9ull.microbit.control;
 
 import javax.ejb.Stateless;
 import javax.inject.Named;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.silix.the9ull.microbit.model.SingletonSessionFactory;
 import org.silix.the9ull.microbit.model.UserP;
 
 @Stateless
 @Named
 public class GetInfoBean implements GetInfoBeanRemote {
 	
 	private Session session;
 	private MtGoxInfo mtgox;
 	
 	public GetInfoBean() {
 		session = SingletonSessionFactory.getSession();
 		mtgox = new MtGoxInfo(10*60);
 	}
 
 	@Override
 	public long numberOfUsers() {
 		Query q = session.createQuery("select count(usr) from UserP usr");
 		@SuppressWarnings("unchecked")
 		List<Long> l = (List<Long>) q.list();
 		if(l.size()>0)
 			return l.get(0);
 		return -1;
 	}
 
 	@Override
 	public BigDecimal valueBtcUsd() {
 		// TODO Auto-generated method stub
 		// External call (cache value into db? Good idea!)
 		if(!mtgox.update("USD")){
 			System.out.println("MtGox API problem");
 		}
 		return mtgox.getUsd();
 	}
 
 	@Override
 	public BigDecimal valueBtcEur() {
 		if(!mtgox.update("EUR")){
 			System.out.println("MtGox API problem");
 		}
 		return mtgox.getEur();
 	}
 
 	@Override
 	public int getIdFromAddress(String address) {
 		Query q = session.createQuery("from UserP usr where usr.deposit_address='"+address+"'");
 		List<UserP> l = (List<UserP>) q.list();
 		if(l.size()>0)
 			return l.get(0).getId();
 		return -1;
 	}
 
 	@Override
 	public String getAddressFromId(int id) {
		Query q = session.createQuery("from UserP usr where usr.id="+id);
		List<UserP> l = (List<UserP>) q.list();
		if(l.size()>0)
			return l.get(0).getDeposit_address();
		return "";
 	}
 	
 }
