 /*******************************************************************************
  * Copyright (c) 2012 BestSolution.at and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
  *******************************************************************************/
 package at.bestsolution.bitbucketmgr.app.jemmy;
 
 import junit.framework.Assert;
 
 import org.jemmy.fx.SceneDock;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class SampleTestCase {
 	protected static SceneDock scene;
 	
 	@BeforeClass
 	public static void startApp() throws InterruptedException {
 		try {
 			scene = new SceneDock();
 		} catch(Throwable t ) {
 			t.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void sampleTestMethod() {
//		Assert.fail("Not implemented");
 	}
 }
