  package tests;
 
 
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import entity.Member;
 import entity.MemberStatus;
 import entity.Provider;
 import entity.Service;
 import entity.ServiceRendered;
 
 public class ServiceRenderedTest {
 
 	private Service service1, service2;
 	private Member member1, member2;
 	private Provider provider1, provider2;
 	
 	@Before
 	public void setUp() throws Exception {
 		List<Member> members = Member.getMembers();
 		for(Member i_member : members){
 			i_member.delete();
 		}
 		
 		List<Provider> providers = Provider.getProviders();
 		for(Provider i_provider: providers){
 			i_provider.delete();
 		}
 		
 		List<Service> services = Service.getServices();
 		for(Service i_service: services){
 			i_service.delete();
 		}
 		
 		member1 = new Member("John Doe",MemberStatus.ACTIVE,
 			"123 foobar st.","Foobar City","OR","96502",
 			"foobar@example.com");
 		member1.save();
 		
 		member2 = new Member("Jane Doe",MemberStatus.ACTIVE,
 			"125 foobar st.","Foobar City","OR","96502",
 			"foobar@example.org");
 		member2.save();
 		
 		provider1 = new Provider("Yogi Bear","yogi@example.net");
 		provider1.save();
 		
 		provider2 = new Provider("Boo Boo","boo_boo@example.net");
 		provider2.save();
 		
 		service1 = new Service("Therapy", new BigDecimal("250.25"));
 		service1.save();
 		
 		service2 = new Service("Massage", new BigDecimal("100.30"));
 		service2.save();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		List<Member> members = Member.getMembers();
 		for(Member i_member : members){
 			i_member.delete();
 		}
 		
 		List<Provider> providers = Provider.getProviders();
 		for(Provider i_provider: providers){
 			i_provider.delete();
 		}
 		
 		List<Service> services = Service.getServices();
 		for(Service i_service: services){
 			i_service.delete();
 		}
 	}
 	
 	@Test
 	public void testServicesRendered() throws Exception{
 		ServiceRendered sr1, sr2;
 		DateFormat df = DateFormat.getDateInstance();
 		DateFormat dtf = DateFormat.getDateTimeInstance();
 		
 		sr1 = new ServiceRendered(
 			dtf.parse("Jan 16, 2011 8:13:24 PM"),
 			df.parse("Jan 16, 2011"),
 			new BigDecimal("200.03"), provider1, service1, member1, "FOOBAR");
 		sr1.save();
 		
 		sr2 = new ServiceRendered(
 			dtf.parse("Jan 19, 2011 8:27:55 PM"),
 			df.parse("Jan 18, 2011"),
 			new BigDecimal("200.03"), provider1, service1, member1, "BARFOO");
 		sr2.save();
 		
 		sr1.delete();
 		sr1.save();
 		
 		int trans_id = sr1.getTransactionID();
 		
 		sr2.setComments("Blogarsis");
 		sr2.setFee(new BigDecimal("430.50"));
 		sr2.setMember(member2);
 		sr2.setProvider(provider2);
 		sr2.setService(service2);
 		sr2.setServiceLogged(dtf.parse("Dec 3, 2007 7:50:45 am"));
 		sr2.setServiceProvided(df.parse("Nov 25, 2006"));
 		
 		sr2.save();
 		
 		sr1 = ServiceRendered.getServicesRenderedByProvider(provider1).get(0);
 		sr2 = ServiceRendered.getServicesRenderedMember(member2).get(0);
 		
 		Assert.assertEquals(trans_id, sr1.getTransactionID());
 		Assert.assertEquals(df.parse("Jan 16, 2011"),sr1.getServiceRendered());
 		Assert.assertEquals(dtf.parse("Jan 16, 2011 8:13:24 pm"), sr1.getServiceLogged());
 		Assert.assertTrue(new BigDecimal("200.03").compareTo(sr1.getFee()) == 0);
 		
 		Assert.assertEquals(member2.getMemberId(), sr2.getMember().getMemberId());
 		Assert.assertEquals(provider2.getProviderId(), sr2.getProvider().getProviderId());
 		Assert.assertEquals(service2.getServiceId(), sr2.getService().getServiceId());
 		Assert.assertEquals("Blogarsis", sr2.getComments());
 		
 		sr1.delete();
 		sr2.delete();
 	}
 	
 	@Test
 	public void testEquals() throws Exception {
 		ServiceRendered sr1, sr2;
 		Object empty = new Object();
 		
 		DateFormat df = DateFormat.getDateInstance();
 		DateFormat dtf = DateFormat.getDateTimeInstance();
 		
 		sr1 = new ServiceRendered(
 			dtf.parse("Jan 16, 2011 8:13:24 PM"),
 			df.parse("Jan 16, 2011"),
 			new BigDecimal("200.03"), provider1, service1, member1, "FOOBAR");
 		sr1.save();
 		
 		sr2 = new ServiceRendered(
 			dtf.parse("Jan 19, 2011 8:27:55 PM"),
 			df.parse("Jan 18, 2011"),
 			new BigDecimal("200.03"), provider2, service2, member2, "BARFOO");
 		
 		sr2.save();
 
 		ServiceRendered sr1_dup = ServiceRendered.getServicesRenderedMember(member1).get(0);
 		
 		Assert.assertEquals(sr1,sr1);
 		Assert.assertEquals(sr1_dup,sr1);
 		Assert.assertEquals(sr1.toString(), sr1_dup.toString());
 		
 		Assert.assertFalse(sr1.equals(null));
 		Assert.assertFalse(sr1.equals(empty));
 		
 		Assert.assertFalse(sr1.equals(sr2));
 		
 		Map<ServiceRendered,Integer> map = new HashMap<ServiceRendered,Integer> ();
 		map.put(sr1, new Integer(2));
 		map.put(sr2, new Integer(2));
 		
 		Assert.assertEquals(new Integer(1),map.get(sr1));
 		Assert.assertEquals(new Integer(2),map.get(sr2));
 	}
 }
