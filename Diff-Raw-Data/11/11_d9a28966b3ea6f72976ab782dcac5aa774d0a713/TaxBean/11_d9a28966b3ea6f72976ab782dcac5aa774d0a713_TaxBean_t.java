 package gr.manousos.bean;
 
 import gr.manousos.model.IncomeTax;
 import gr.manousos.model.Taxpayer;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Calendar;
 import java.util.Properties;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.ws.rs.core.MediaType;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 @ManagedBean
 public class TaxBean {
     Properties config = new Properties();
 
     @ManagedProperty(value = "#{loginBean}")
     private LoginBean login;
     private String error;
 
     private float principalTax;
     private float wifeTax;
     private float totalTax;
 
     public String getError() {
 	return error;
     }
 
     public void setError(String error) {
 	this.error = error;
     }
 
     public float getPrincipalTax() {
 	return principalTax;
     }
 
     public void setPrincipalTax(float principalTax) {
 	this.principalTax = principalTax;
     }
 
     public float getWifeTax() {
 	return wifeTax;
     }
 
     public void setWifeTax(float wifeTax) {
 	this.wifeTax = wifeTax;
     }
 
     public float getTotalTax() {
 	return totalTax;
     }
 
     public void setTotalTax(float totalTax) {
 	this.totalTax = totalTax;
     }
 
    public LoginBean getLogin() {
	return login;
    }

    public void setLogin(LoginBean login) {
	this.login = login;
    }

     public TaxBean() {
 
     }
 
     @PostConstruct
     public void init() throws IOException {
 
 	this.error = "Welcome: " + login.getLoggedInUsername();
 
 	IncomeTax tax = null;
 	ClientConfig conf = new DefaultClientConfig();
 	try {
 	    config.load(getClass().getClassLoader().getResourceAsStream(
 		    "config.properties"));
 
 	    Taxpayer taxpayer = null;
 
 	    Client client = Client.create(conf);
 
 	    WebResource restSrv = client.resource(new URI("http://localhost:"
 		    + config.getProperty("web_port") + "/TaxisNet/rest/"));
 	    taxpayer = (Taxpayer) restSrv
 		    .path("UserService/getTaxPayerByUserName/")
 		    .path(login.getLoggedInUsername())
 		    .accept(MediaType.APPLICATION_JSON).get(Taxpayer.class);
 
 	    WebResource restSrv2 = client.resource(new URI("http://localhost:"
 		    + config.getProperty("web_port") + "/TaxisNet/rest/"));
 	    tax = (IncomeTax) restSrv2
		    .path("TaxCalcService/tax/")
 		    .queryParam("tId", taxpayer.getId().toString())
 		    .queryParam(
 			    "year",
 			    String.valueOf(Calendar.getInstance().get(
 				    Calendar.YEAR)))
 		    .accept(MediaType.APPLICATION_JSON).get(IncomeTax.class);
 
 	    this.setPrincipalTax(tax.getPrincipalTax());
 	    this.setWifeTax(tax.getWifeTax());
 	    this.setTotalTax(this.principalTax + this.wifeTax);
 
 	} catch (Exception ex) {
 	    this.error = "Exeption: " + ex.toString() + "<br /> Stack Trace "
 		    + ex.getStackTrace() + "<br /> Caouse " + ex.getCause();
 
 	}
     }
 }
