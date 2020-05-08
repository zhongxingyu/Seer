 /**
  * ************************************************************************
  * 
  *    server-objects - a contrib to the Qooxdoo project that makes server 
  *    and client objects operate seamlessly; like Qooxdoo, server objects 
  *    have properties, events, and methods all of which can be access from
  *    either server or client, regardless of where the original object was
  *    created.
  * 
  *    http://qooxdoo.org
  * 
  *    Copyright:
  *      2010 Zenesis Limited, http://www.zenesis.com
  * 
  *    License:
  *      LGPL: http://www.gnu.org/licenses/lgpl.html
  *      EPL: http://www.eclipse.org/org/documents/epl-v10.php
  *      
  *      This software is provided under the same licensing terms as Qooxdoo,
  *      please see the LICENSE file in the Qooxdoo project's top-level directory 
  *      for details.
  * 
  *    Authors:
  *      * John Spackman (john.spackman@zenesis.com)
  * 
  * ************************************************************************
  */
 package com.zenesis.qx.remote.test.simple;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import com.zenesis.qx.remote.ProxyManager;
 import com.zenesis.qx.remote.annotations.Method;
 import com.zenesis.qx.remote.test.properties.ITestArrays;
 import com.zenesis.qx.remote.test.properties.ITestExceptions;
 import com.zenesis.qx.remote.test.properties.ITestProperties;
 import com.zenesis.qx.remote.test.properties.TestArrays;
 import com.zenesis.qx.remote.test.properties.TestExceptions;
 import com.zenesis.qx.remote.test.properties.TestProperties;
 
 public class TestBootstrap implements ITestBootstrap {
 	
 	private ITestScalars testScalars = new TestScalars();
 	private final TestProperties testProperties = new TestProperties();
 	private final TestExceptions testExceptions = new TestExceptions();
 	private final ITestArrays testArrays = new TestArrays();
 	
 	private TestProperties clientTestProperties;
 	private Pippo lastPippo;
 
 	@Override
 	public ITestScalars getTestScalars() {
 		return testScalars;
 	}
 
 	@Override
 	public boolean verifyTestScalars(ITestScalars testScalars) {
 		return testScalars == this.testScalars;
 	}
 
 	@Override
 	public ITestProperties getTestProperties() {
 		ProxyManager.preloadProperty(testProperties, "onDemandPreload", testProperties.getOnDemandPreload());
 		return testProperties;
 	}
 
 	@Override
 	public boolean checkClientTestProperties() {
 		if (clientTestProperties == null || clientTestProperties == testProperties)
 			return false;
 		String watched = clientTestProperties.getWatchedString();
 		return "setByClientProperty".equals(watched);
 	}
 
 	@Override
 	public TestProperties getClientTestProperties() {
 		return clientTestProperties;
 	}
 
 	@Override
 	public void setClientTestProperties(TestProperties props) {
 		this.clientTestProperties = props;
 	}
 
 	@Override
 	public boolean checkNewTestProperties(ITestProperties props) {
 		if (props == null || props == testProperties)
 			return false;
 		String watched = props.getWatchedString();
 		return "setByClientMethod".equals(watched);
 	}
 
 	@Override
 	public ITestExceptions getTestExceptions() {
 		return testExceptions;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.zenesis.qx.remote.test.simple.ITestBootstrap#getTestArrays()
 	 */
 	@Override
 	public ITestArrays getTestArrays() {
 		return testArrays;
 	}
 	
 	@Method
 	public Pippo getPippo() {
 		return lastPippo = new Pippo();
 	}
 	
 	@Method
 	public boolean wasLastPippoDisposed() {
 		boolean has = ProxyManager.getTracker().hasProxied(lastPippo);
		return has;
 	}
 
 	@Method
 	public String testPippoArray(Pippo[] pippos) {
 		String str = "";
 		for (int i = 0; i < pippos.length; i++) {
 			str += "Pippo #" + i + ": name=" + pippos[i].getName();
 		}
 		System.out.println("Received pippos: " + str);
 		return str;
 	}
 	
 	@Method
 	public String testPippoArray2(Pippo[] pippos) {
 		String str = "";
 		for (int i = 0; i < pippos.length; i++) {
 			str += "Pippo #" + i + ": name=" + pippos[i].getName();
 		}
 		System.out.println("Received pippos: " + str);
 		return str;
 	}
 	
 	@Method
 	public int waitForMillis(int millis) {
 		try {
 			Thread.sleep(millis);
 		}catch(InterruptedException e) {
 			// Nothing
 		}
 		return millis;
 	}
 	
 	@Method
 	public Date getTodaysDate() {
 		return new Date();
 	}
 	
 	@Method
 	public boolean isYesterday(Date dt) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(new Date());
 		cal.add(Calendar.DATE, -1);
 		return dt.getYear() + 1900 == cal.get(Calendar.YEAR) &&
 				dt.getMonth() == cal.get(Calendar.MONTH) &&
 				dt.getDate() == cal.get(Calendar.DAY_OF_MONTH);
 	}
 }
