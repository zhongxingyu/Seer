 package tap.practica.servidor;
 
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.ClassPathResource;
 
 import tap.practica.estructuras.Alumno;
 
 public class Factory extends UnicastRemoteObject implements FactoryIfaz {
 	public static XmlBeanFactory factory = new XmlBeanFactory(
 			new ClassPathResource("objetos.xml"));
 	public static Alumno a;
 
 	public static void main(String[] args) throws RemoteException,
 			MalformedURLException {
 		System.out.println("Servidor: creando factoría...");
 		Factory fac = new Factory();
 		System.out.println("Servidor: registrando factoría");
 		Naming.rebind("alumnos", fac);
 		System.out.println("Servidor: factoría registrada como alumnos");
 	}
 
 	protected Factory() throws RemoteException {
		//Por ahora existe, debería haber uno dummy
 		a = (Alumno) factory.getBean("q1234");
 	}
 
 	@Override
 	public Alumno getAlumno(String nif) throws RemoteException {
		a = (Alumno) factory.getBean(nif);
 		return a;
 	}
 
 }
