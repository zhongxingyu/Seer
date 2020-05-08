 package com.nanhua.trading.web.ui.customer;
 
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.nanhua.trading.domain.account.Authority;
 import com.nanhua.trading.domain.correlator.Correlator;
 import com.nanhua.trading.domain.correlator.ServiceInstance;
 import com.nanhua.trading.domain.customer.Customer;
 import com.nanhua.trading.domain.datadict.NetworkType;
 import com.nanhua.trading.domain.servicesupplier.ServiceSupplier;
 
 public class CustomerHelper {
 	
 	public static Customer createTester(){
 		List<Customer> list = Customer.findCustomersByName("test");
 		Integer i = 1;
 		for (Customer c : list) {
 			if(StringUtils.isNumeric(c.getUsername().substring(4))){
 				i = Integer.valueOf(c.getUsername().substring(4));
 				i = i+1;
 				break;
 			}
 		}
 		
 		
 		Customer test = new Customer();
 		test.setUsername("test"+i);
 		test.setPassword("password");
 		test.setEnabled(true);
 		test.setEmail(test.getUsername()+"@nawaa.com");
 		test.getAuthoritiesBySet().add(new Authority("ROLE_CUSTOMER"));
 		test.setAddress("地址");
 		test.setName("测试用户"+test.getUsername());
 		test.setMobile("111111111");
 		test.setPhone("111111");
 		Correlator cor = new Correlator();
 		cor.setName("服务器1");
 		cor.setNetworkType(NetworkType.findNetworkByName("联通"));
 		cor.setHostname("127.0.0.1");
 		cor.setPort("55555");
 		ServiceInstance si = new ServiceInstance();
 		si.setServiceid("ctp_trade_test");
 		si.setSupplier(ServiceSupplier.findSupplierByPrefix("ctp_trade_test"));
 		si.setCorrelator(cor);
 		cor.getServiceInstances().add(si);
 		
 		si = new ServiceInstance();
 		si.setServiceid("ctp_md_test");
 		si.setSupplier(ServiceSupplier.findSupplierByPrefix("ctp_md_test"));
 		si.setCorrelator(cor);
 		cor.getServiceInstances().add(si);
 		
 		
 		si = new ServiceInstance();
 		si.setServiceid("ks_trade_test");
 		si.setSupplier(ServiceSupplier.findSupplierByPrefix("ks_trade_test"));
 		si.setCorrelator(cor);
 		cor.getServiceInstances().add(si);
 		
 		si = new ServiceInstance();
 		si.setServiceid("ks_md_test");
 		si.setSupplier(ServiceSupplier.findSupplierByPrefix("ks_md_test"));
 		si.setCorrelator(cor);
 		cor.getServiceInstances().add(si);
 		
 		test.getCorrelators().add(cor);
 		try{
 		test.persist();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return test;
 	}
 	
 }
