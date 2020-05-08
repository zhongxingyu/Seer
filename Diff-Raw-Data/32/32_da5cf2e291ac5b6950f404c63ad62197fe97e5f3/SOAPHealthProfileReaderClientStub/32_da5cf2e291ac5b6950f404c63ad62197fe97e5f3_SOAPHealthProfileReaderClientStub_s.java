 package introsde.soap.client;
 
 import samples.quickstart.HealthProfileReader;
 import samples.quickstart.HealthProfileReaderLocator;
 import samples.quickstart.HealthProfileReaderSoap11BindingStub;
 
 public class SOAPHealthProfileReaderClientStub {
 
	public static void main(String[] args) {
		try {
			HealthProfileReaderLocator hprLoc = new HealthProfileReaderLocator();			
			HealthProfileReaderSoap11BindingStub stub = new HealthProfileReaderSoap11BindingStub(hprLoc);
			String value = stub.getParameterValue("Pinco", "Pallino", "bmi");
			System.out.println(value);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
 	}
 }
