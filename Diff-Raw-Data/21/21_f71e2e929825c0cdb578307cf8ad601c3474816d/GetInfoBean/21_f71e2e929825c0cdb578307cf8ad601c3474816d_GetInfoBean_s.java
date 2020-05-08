 package org.silix.the9ull.microbit.control;
 
 import javax.ejb.Stateless;
 import javax.inject.Named;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.silix.the9ull.microbit.model.SingletonSessionFactory;
 import org.silix.the9ull.microbit.model.UserP;
 
 @Stateless
 @Named
 public class GetInfoBean implements GetInfoBeanRemote {
 	
 	private Session session;
 	
 	public GetInfoBean() {
 		session = SingletonSessionFactory.getSession();
 	}
 
 	@Override
 	public long numberOfUsers() {
 		Query q = session.createQuery("select count(usr) from UserP usr");
		return (Long) q.list().get(0);
 	}
 
 	@Override
 	public double valueBtcUsd() {
 		// TODO Auto-generated method stub
 		// External call (cache value into db? Good idea!)
 		return 12.84;
 	}
 
 	@Override
 	public double valueBtcEur() {
 		// TODO Auto-generated method stub
 		return 10.10;
 	}
 
 	@Override
 	public int getIdFromAddress(String address) {
 		Query q = session.createQuery("from UserP usr where usr.deposit_address="+address);
		UserP u = (UserP) q.list().get(0);
		return u.getId();
 	}
 
 	@Override
 	public String getAddressFromId(int id) {
 		Query q = session.createQuery("from UserP usr where usr.id="+id);
		UserP u = (UserP) q.list().get(0);
		return u.getDeposit_address();
 	}
 	
 }
