 package org.silix.the9ull.microbit.control;
 
 import javax.ejb.Stateless;
 import javax.inject.Named;
import java.util.List;
 
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
		List<Long> l = (List<Long>) q.list();
		if(l.size()>0)
			return l.get(0);
		return -1;
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
