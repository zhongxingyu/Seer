 /**
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 package org.eclipse.emf.compare.tests.fullcomparison;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import com.google.common.collect.Lists;
 
 import org.eclipse.emf.compare.match.eobject.EditionDistance;
 import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
 import org.eclipse.emf.compare.tests.suite.AllTests;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.experimental.theories.DataPoints;
 import org.junit.experimental.theories.Theories;
 import org.junit.experimental.theories.Theory;
 import org.junit.runner.RunWith;
 
 /**
  * This is a brute force tests using JUnit4 theories to check a distance function really is a distance
  * function in the mathematical sense. It checks all the axioms using the Ecore.ecore model as an input model.
  * <b>It is going to fail</b> with the EditionDistance implementation, so far I could not implement one which
  * is conform to the triangular inequality axiom preventing us to use any kind of clever sorting. That said it
  * looks like we don't really need it so far, looks like we are going fast enough. I'd like to keep this test
  * around anyway as its neet and it might be of use at some point.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 @RunWith(Theories.class)
 public class DistanceAxiomsTests {
 
 	private DistanceFunction meter;
 
 	private int MAX_DISTANCE = Integer.MAX_VALUE;
 
 	@Before
 	public void setUp() throws Exception {
 		AllTests.fillEMFRegistries();
 		this.meter = new EditionDistance();
 	}
 
 	@DataPoints
 	public static EObject[] allEcore = createFingerPrintsFromModel(EcorePackage.eINSTANCE);
 
 	@Theory
 	public void symetry(EObject a, EObject b) {
 		Assume.assumeTrue(a.eClass() == b.eClass());
		int aTob = meter.distance(a, b);
		int bToa = meter.distance(b, a);
 		assertEquals(aTob, bToa);
 	}
 
 	private static EObject[] createFingerPrintsFromModel(EObject einstance) {
 		return Lists.newArrayList(einstance.eAllContents()).toArray(new EObject[0]);
 	}
 
 	@Theory
 	public void separation(EObject a) {
		assertEquals(0, meter.distance(a, a));
 	}
 
 	@Theory
 	public void triangularInequality(EObject x, EObject y, EObject z) {
 		Assume.assumeTrue(x.eClass() == y.eClass() && x.eClass() == z.eClass());
		int xToz = meter.distance(x, z);
		int xToy = meter.distance(x, y);
		int yToz = meter.distance(y, z);
 		assertTrue("Triangular inequality (x-z <= x-y + y-z ) failed (" + xToz + "<=" + xToy + " + " + yToz
 				+ ")for \nx:" + x.toString() + "\n|y:" + y.toString() + "\n|z:" + z.toString(), xToz <= xToy
 				+ yToz);
 	}
 
 }
