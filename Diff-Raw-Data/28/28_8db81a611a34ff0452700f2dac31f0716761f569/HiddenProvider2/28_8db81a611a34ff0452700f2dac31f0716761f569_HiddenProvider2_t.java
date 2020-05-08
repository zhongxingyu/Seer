 package tester.one.impl.provider;
 
 import tester.one.api.Service;
 
 /**
  * Changes in IMPL package class are compatible.
  * <p>
  * By bnd/maven-bundle-plugin convention,
  * 
  * http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html
  * 
  * <pre>
  * <Export-Package> is now assumed to be the set of packages in your local Java sources, 
  * excluding the default package '.' and any packages containing 'impl' or 'internal'.
  * </pre>
  * 
  * Magic 'impl' or 'internal' anywhere in the package name makes it invisible to
  * OSGI, regardless of class/method visibility.
  */
 public class HiddenProvider2 implements Service {
 
 	@Override
 	public void process_1() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void process_2() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void process_3() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void process_4() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void process_5() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void process_6() {
 		// TODO Auto-generated method stub
 
 	}
 
	@Override
	public void process_7() {
		// TODO Auto-generated method stub

	}

 }
