 /*******************************************************************************
  * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.emf.compare.epatch.dsl.tests;
 
 import static org.eclipse.emf.compare.epatch.dsl.util.EpatchDSLUtil.parseEpatch;
 import static org.eclipse.emf.compare.epatch.dsl.util.EpatchDSLUtil.serializeEpatch;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.ArrayList;
 
 import org.eclipse.emf.compare.epatch.Epatch;
 import org.eclipse.emf.compare.epatch.EpatchPackage;
 import org.eclipse.emf.compare.epatch.dsl.EpatchStandaloneSetup;
 import org.eclipse.emf.compare.epatch.dsl.util.EpatchDSLUtil;
 import org.eclipse.emf.compare.epatch.recorder.EpatchRecorder;
 import org.eclipse.emf.compare.epatch.tests.testdata.Change;
 import org.eclipse.emf.compare.epatch.tests.util.EmfAssert;
 import org.eclipse.emf.compare.epatch.tests.util.EmfFormatter;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 /**
  * @author Moritz Eysholdt - Initial contribution and API
  */
 @RunWith(Parameterized.class)
 public class EpatchRecorderTest {
 	static {
 		EpatchPackage.eINSTANCE.getEFactoryInstance();
 		EpatchStandaloneSetup.doSetup();
 	}
 
 	@Parameters
 	public static ArrayList<Object[]> getData() {
 		return Change.Util.getAllChanges();
 	}
 
 	public Change change;
 
 	public EpatchRecorderTest(Change ch) {
 		change = ch;
 	}
 
 	@Test
 	public void testParseAndSerialize() {
 		Epatch patch = parseEpatch(change + ".epatch", change.asPatch());
 		String actual = serializeEpatch(patch);
 		// TODO: fix string quotes so that no replacement is needed
		EmfAssert.assertTokensEqual(change.toString(), change.asPatch().replace("'", "\""), actual);
 	}
 
 	@Test
 	public void testRecordedHasCorrectModel() {
 		ResourceSet rs = change.getResourceSet();
		System.out.println(EmfFormatter.objToStr(rs.getResources().get(0).getContents().get(0)));
 		EpatchRecorder rec = new EpatchRecorder(rs, change.toString());
 		change.apply(rs);
 		Epatch recorded = rec.endRecording();
		System.out.println(EmfFormatter.objToStr(rs.getResources().get(0).getContents().get(0)));
 		System.out.println(EpatchDSLUtil.serializeEpatch(recorded));
 		Epatch expected = parseEpatch(change + ".epatch", change.asPatch());
 		System.out.println(EmfFormatter.objToStr(expected));
 		// removeMigrationInfo(expected);
 		EmfAssert.assertEObjectsEqual(expected, recorded);
 	}
 
 	@Test
 	public void testSerializable() {
 		ResourceSet rs = change.getResourceSet();
 		EpatchRecorder rec = new EpatchRecorder(rs, change.toString());
 		change.apply(rs);
 		Epatch recorded = rec.endRecording();
 		String actual = serializeEpatch(recorded);
 		assertNotNull(actual);
 	}
 }
