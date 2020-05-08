 /*******************************************************************************
  * Copyright (c) 2006-2009
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Software Technology Group - TU Dresden, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.coconut.test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.PrintStream;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.junit.Test;
 import org.reuseware.coconut.compositionprogram.CompositionLink;
 import org.reuseware.coconut.compositionprogram.CompositionProgram;
 import org.reuseware.coconut.compositionprogram.FragmentInstance;
 import org.reuseware.coconut.fragment.ComposedFragment;
 import org.reuseware.coconut.fragment.Fragment;
 
 public class ReusewareFeaturesCompositionTest extends AbstractReusewareCompositionTest {
 	
 	@Override
 	public AbstractReusewareTestHelper getTestHelper() {
 		//return new SokanReusewareTestHelper();
 		return new ResourceSetReusewareTestHelper();
 	}
 	
 	@Test
 	public void testInit() throws Exception {
 		// out store
 		File outStoreFolder = new File("../org.reuseware.coconut.test/out");
 		AbstractReusewareTestHelper.deleteDirectory(outStoreFolder, false, false);
 		outStoreFolder.mkdir();
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"out",
 				new String[] {});
 		
 		// store with default fracol
 		assertRegisterFragmentStore(
 				"org.reuseware.coconut.resource", 
 				"store", 
 				new String[]{});
 	}
 	
 	@Test
 	public void testRepositoryBasics() throws Exception {
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/repositoryBasics",
 				new String[] {"ecore"});
 		CompositionProgram cp = getCompositionProgram(new String[] {
 				"org","reuseware","test","basic","repositoryBasics","composition.ucl"
 		});
 		
 		for(FragmentInstance fi : cp.getFragmentInstances()) {
 			assertNotNull(fi.getUFI() + " fragment missing", fi.getFragment());
 			assertEquals(fi.getUFI(), fi.getFragment().getUFI());
 		}
 		
 		//the cp is reloaded. therefore this is fine
 		assertTrue(cp.getComposedFragments().size() == 0);
 		
 		cp.compose();
 		
 		//now we should have composed fragments
 		assertTrue(cp.getComposedFragments().size() == 4);
 	}
 	
 	
 	@Test
 	public void testNonContributingExtensions() throws Exception {
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/nonContributingExtensions",
 				new String[] {"ecore"});
 		
 		Fragment f; EPackage p; EClassifier c1, c2;
 		
 		f = getFragment(new String[] {
 				"org","reuseware","test","basic","nonContributingExtensions","Empty.ecore"
 		});
 		assertTrue(f instanceof ComposedFragment);
 		
 		p = (EPackage) f.getContents().get(0);
 		
 		
 		
 		//Empty, because the configurations on the target of a step are not further followed
 		assertEquals(0, p.getEClassifiers().size());
 		
 		f = getFragment(new String[] {
 				"org","reuseware","test","basic","nonContributingExtensions","OneA.ecore"
 		});
 		assertTrue(f instanceof ComposedFragment);
 		
 		p = (EPackage) f.getContents().get(0);
 		
 		//Only one EClass A, because the second link is NOT contributing, the same A is "added twice"
 		assertEquals(1, p.getEClassifiers().size());
 		c1 = p.getEClassifiers().get(0);
 		assertEquals("A", c1.getName());
 
 		f = getFragment(new String[] {
 				"org","reuseware","test","basic","nonContributingExtensions","TwoA.ecore"
 		});
 		assertTrue(f instanceof ComposedFragment);
 		
 		p = (EPackage) f.getContents().get(0);
 	
 		//Two EClass A, because both links are contributing; 
 		//Also, no A should be stolen because of config link direction
 		assertEquals(2, p.getEClassifiers().size());
 		c1 = p.getEClassifiers().get(0);
 		assertEquals("A", c1.getName());
 		c2 = p.getEClassifiers().get(1);
 		assertEquals("A", c2.getName());
 		
 		f = getFragment(new String[] {
 				"org","reuseware","test","basic","nonContributingExtensions","OneAWithOp.ecore"
 		});
 		assertTrue(f instanceof ComposedFragment);
 		
 		p = (EPackage) f.getContents().get(0);
 	
 		//One EClass A with Op B, because one link is contributing and pulls in the other
 		assertEquals(1, p.getEClassifiers().size());
 		c1 = p.getEClassifiers().get(0);
 		assertEquals("A", c1.getName());
 		assertEquals(1, ((EClass)c1).getEOperations().size());
 	}
 
 	@Test
 	public void testFragmentReference() throws Exception {
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/fragmentReference",
 				new String[] {"ecore"});
 		
 		Fragment referencedF = getFragment(new String[] {
 				"org","reuseware","test","basic","fragmentReference","typeA.ecore"
 		});
 		
 		Fragment f; EPackage p; EClassifier c; EOperation o;
 		
 		f = getFragment(new String[] {
 				"org","reuseware","test","basic","fragmentReference","noReference.ecore"
 		});
 		EPackage referencedP = (EPackage) referencedF.getContents().get(0);
 		EClassifier referencedC = referencedP.getEClassifiers().get(0);
 		
 		p = (EPackage) f.getContents().get(0);
 		c = p.getEClassifier("B");
 		
 		assertNotNull(c);
 		
 		o = ((EClass)c).getEOperations().get(0);
 		
 		assertNotSame(referencedC, o.getEType());
 		assertSame(o.eResource(), o.getEType().eResource());
 		
 		f = getFragment(new String[] {
 				"org","reuseware","test","basic","fragmentReference","withReference.ecore"
 		});
 		
 		p = (EPackage) f.getContents().get(0);
 		c = p.getEClassifier("B");
 		o = ((EClass)c).getEOperations().get(0);
 
 		assertSame(referencedC, o.getEType());
 		assertNotSame(o.eResource(), o.getEType().eResource());
 	}
 
 	@Test
 	public void testCompositionProgramContext() throws Exception {
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/compositionProgramContext",
 				new String[] {"ecore"});
 		
 		Fragment fA = getFragment(new String[] {
 				"org","reuseware","test","basic","compositionProgramContext","ComposedA.ecore"
 		});
 		Fragment fB = getFragment(new String[] {
 				"org","reuseware","test","basic","compositionProgramContext","ComposedB.ecore"
 		});
 		
 		EPackage pA = (EPackage) fA.getContents().get(0);
 		EClass A = (EClass) pA.getEClassifier("A");
 		
 		assertNotNull(A);
 		
 		EReference r = (EReference) A.getEStructuralFeature("crossReference");
 		
 		EPackage pB = (EPackage) fB.getContents().get(0);
 		EClass B = (EClass) pB.getEClassifier("B");
 		
 		//The context of the composition program allows cross-referencing between different composed fragments
 		assertEquals("The cross-reference should now point at B", B, r.getEType());
 		
 	}
 
 	@Test
 	public void testValueHookWithIndex() throws Exception {
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/valueHookWithIndex",
 				new String[] {"ecore"});
 		
 		Fragment f = getFragment(new String[] {
 				"org","reuseware","test","basic","valueHookWithIndex","valueHookWithIndexComposed.ecore"
 		});
 		
 		EPackage p = (EPackage) f.getContents().get(0);
 		EClassifier c = p.getEClassifiers().get(0);
 		
 		//HOOK should be replaced by TEST in the name String
 		assertTrue("Value 'ExtendedTESTClass' expected, but was '" + c.getName() + "'",
 				c.getName().equals("ExtendedTESTClass"));
 		
 	}
 
 	@Test
 	public void testValueHookContribution() throws Exception {
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/valueHookContribution",
 				new String[] {"ecore"});
 		
 		Fragment f = getFragment(new String[] {
 				"org","reuseware","test","basic","valueHookContribution","composedA.ecore"
 		});
 		
 		EPackage p = (EPackage) f.getContents().get(0);
 		
 		//ensure package we look at is A
 		assertTrue("Expected package A",
 				p.getNsPrefix().equals("A"));
 		
 		//ValuePrototype from B.ecore should change A to B
 		assertTrue("After the composition Package A should have turned into B",
 				p.getName().equals("B"));
 		
 	}
 
 	@Test
 	public void testSamePointNameGrouping() throws Exception {
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/samePointNameGrouping",
 				new String[] {"ecore"});
 		
 		Fragment f = getFragment(new String[] {
 				"org","reuseware","test","basic","samePointNameGrouping","composed","Rec.ecore"
 		});
 		
 		//there should be 5 EPackage root elements: 1 from Rec.ecore and 4 from Contrib.ecore
 		assertEquals(5, f.getContents().size());
 	}
 	
 	@Test
 	public void testRexclBasics() throws Exception {
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/rexclBasics",
 				new String[] {"ecore"});
 		
 		Fragment f = getFragment(new String[] {
 				"org","reuseware","test","basic","rexclBasics","result.ecore"
 		});
 		
 		CompositionProgram cp = getCompositionProgramForComposedFragment(f.getUFI());	
 		assertNotNull(cp);
 		assertFalse("Composition program is empty", cp.getFragmentInstances().isEmpty());
 		assertFalse("Composition program is empty", cp.getCompositionLinks().isEmpty());
 		
 		boolean result = true;
 		for(CompositionLink link : cp.getCompositionLinks()) {
 			link.match();
 			result = result && isLinkValid(link);
 		}
 		assertTrue("Ivalid links in " + cp.getUCPI(), result);
 		
 		assertFalse("Composition result is empty", f.getContents().isEmpty());
 		
 		EPackage p = (EPackage) f.getContents().get(0);
 		
 		//there should 4 classes in here C1-C2-C3-C1 
 		assertEquals(4, p.getEClassifiers().size());
 	}
 	
 	public void testCyclicDependency() throws Exception {
 		PrintStream origError = System.err;
 		ByteArrayOutputStream allOutput = new ByteArrayOutputStream();
 		PrintStream err = new PrintStream(allOutput);
 		System.setErr(err);
 		
 		assertRegisterFragmentStore( 
 				"org.reuseware.coconut.test", 
 				"testdata/cyclicDependency",
 				new String[] {"ecore"});
 		
 		origError.print(allOutput.toString());
 		
 		assertTrue("Cyclic dependency expected",
 				allOutput.toString().contains(
				"[Sokan] Possible cyclic dependency (involved artivatcts: [[org, reuseware, test, basic, cyclicDependency, cyclicDependencyComposed.ecore]])"));
 		
 		System.setErr(origError);
 		err.close();
 	}
 
 }
