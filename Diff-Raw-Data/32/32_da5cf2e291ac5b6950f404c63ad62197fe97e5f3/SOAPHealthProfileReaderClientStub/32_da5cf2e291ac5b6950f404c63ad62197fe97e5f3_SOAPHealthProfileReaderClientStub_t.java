 package introsde.soap.client;
 
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import org.apache.axis.AxisFault;

 import samples.quickstart.HealthProfileReader;
 import samples.quickstart.HealthProfileReaderLocator;
 import samples.quickstart.HealthProfileReaderSoap11BindingStub;
import service.health.pojos.xsd.HealthProfile;
 
 public class SOAPHealthProfileReaderClientStub {
 
	public static void main(String[] args) throws RemoteException,
			MalformedURLException {
		HealthProfileReaderLocator hprLoc = new HealthProfileReaderLocator();
		java.net.URL endpoint = new java.net.URL(
				hprLoc.getHealthProfileReaderHttpSoap11EndpointAddress());
		HealthProfileReaderSoap11BindingStub stub = new HealthProfileReaderSoap11BindingStub(
				endpoint, hprLoc);
		String value = stub.getParameterValue("Pinco", "Pallino", "bmi");
		System.out.println("Service 'getParameterValue' called asking for bmi. Result ="+value);

		HealthProfile hp = stub.getHealthProfile("Pinco", "Pallino");

		System.out.println("Health Profile obtained\n--> Height="
				+ hp.getHeight() + "\n--> Weight=" + hp.getWeight()
				+ "\n--> BMI=" + hp.getBMI());
 	}
 }
