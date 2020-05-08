 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.setup;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import junit.framework.TestCase;
 
 import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EValidatorRegistryImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.gmf.gmfgraph.Canvas;
 import org.eclipse.gmf.gmfgraph.Connection;
 import org.eclipse.gmf.gmfgraph.Node;
 import org.eclipse.gmf.gmfgraph.util.Assistant;
 import org.eclipse.gmf.tests.Plugin;
 
 /**
  * @author artem
  */
 public class TestSetupTest extends TestCase {
 
 	public TestSetupTest(String name) {
 		super(name);
 	}
 
 	public void testLibraryMap() {
 		try {
 			MapDefSource s = new MapDefFileSetup().init(Plugin.createURI("/models/library/library.gmfmap"));
 			doAssert(Diagnostician.INSTANCE.validate(s.getMapping().getDiagram()));
 		} catch (IOException ex) {
 			fail(ex.getMessage());
 		}
 	}
 	
 	public void testLibraryGen() {
 		try {
 			DiaGenSource s = new DiaGenFileSetup().init(Plugin.createURI("/models/library/library.gmfgen"));
			doAssert(tempHackValidateWithoutOurAnnotation(s.getGenDiagram()));
 			
 		} catch (IOException ex) {
 			fail(ex.getMessage());
 		}
 	}
	private Diagnostic tempHackValidateWithoutOurAnnotation(EObject object) {
		return new Diagnostician(new EValidatorRegistryImpl()).validate(object);
	}
 
 	public void testBasicGraphDefModel() {
 		try {
 			DiaDefSource s = new DiaDefFileSetup().init(Assistant.getBasicGraphDef());
 			doDiaDefTests(s);
 		} catch (IOException ex) {
 			fail(ex.getMessage());
 		}
 	}
 
 	public void testDiaDefSetupNoConfig() {
 		DiaDefSource s = new DiaDefSetup(null).init();
 		doDiaDefTests(s);
 	}
 
 	public void testDiaDefGenerateSetupWithConfig() {
 		final boolean[] setupCanvasDef = {false};
 		final boolean[] setupLinkDef = {false};
 		final boolean[] setupNodeDef = {false};
 		DiaDefSource s = new DiaDefSetup(new DiaDefSetup.Config() {
 			public void setupCanvasDef(Canvas canvasDef) {
 				setupCanvasDef[0] = true;
 			}
 			public void setupLinkDef(Connection linkDef) {
 				setupLinkDef[0] = true;
 			}
 			public void setupNodeDef(Node nodeDef) {
 				setupNodeDef[0] = true;
 			}
 		}).init();
 		assertTrue("DiaDefGenerateSetup.Config.setupNodeDef()", setupNodeDef[0]);
 		assertTrue("DiaDefGenerateSetup.Config.setupLinkDef()", setupLinkDef[0]);
 		assertTrue("DiaDefGenerateSetup.Config.setupCanvasDef()", setupCanvasDef[0]);
 		doDiaDefTests(s);
 	}
 
 	public void testDomainModelSetup() {
 		DomainModelSetup s = new DomainModelSetup().init();
 		doDomainSourceTests(s);
 	}
 
 	public void testMultiPackageSetup() throws Exception {
 		DomainModelSource s = new MultiplePackagesDomainModelSetup().init();
 		doDomainSourceTests(s);
 		final HashSet additionalPacks = new HashSet(8);
 		additionalPacks.add(s.getNodeA().getEClass().getEPackage());
 		additionalPacks.add(s.getNodeB().getEClass().getEPackage());
 		additionalPacks.add(s.getLinkAsClass().getEClass().getEPackage());
 
 		DiaDefSource gmfGraph = new DiaDefFileSetup().init(Assistant.getBasicGraphDef());
 		ToolDefSource toolDef = new ToolDefSetup();
 		MapDefSource mapSource = new MapSetup().init(gmfGraph, s, toolDef);
 		final Resource resource = s.getModel().eResource();
 		resource.getContents().add(mapSource.getMapping());
 		resource.getContents().add(toolDef.getRegistry());
		doAssert("Map", tempHackValidateWithoutOurAnnotation(mapSource.getMapping().getDiagram()));
 		doDiaGenTests(new MultiPackageGenSetup(additionalPacks).init(mapSource));
 	}
 
 	public void testDiaGenSetupDM() {
 		doDiaGenTests(new DiaGenSetup().init(new DomainModelSetup().init()));
 	}
 
 	public void testDiaGenSetupMap() {
 		DomainModelSource ds = new DomainModelSetup().init();
 		MapDefSource ms = new MapSetup().init(new DiaDefSetup(null).init(), ds, new ToolDefSetup());
 		doDiaGenTests(new DiaGenSetup().init(ms));
 	}
 
 	private void doDiaDefTests(DiaDefSource s) {
 		doAssert(Diagnostician.INSTANCE.validate(s.getCanvasDef()));
 	}
 
 	private void doDiaGenTests(DiaGenSource s) {
		Diagnostic d = tempHackValidateWithoutOurAnnotation(s.getNodeA());
 		doAssert("GenNode", d);
		d = tempHackValidateWithoutOurAnnotation(s.getLinkC());
 		doAssert("GenLink", d);
		d = tempHackValidateWithoutOurAnnotation(s.getGenDiagram());
 		doAssert("GenDiagram", d);
 	}
 
 	private static void doDomainSourceTests(DomainModelSource s) {
 		doAssert(Diagnostician.INSTANCE.validate(s.getDiagramElement()));
 		doAssert(Diagnostician.INSTANCE.validate(s.getNodeA().getEClass()));
 		doAssert(Diagnostician.INSTANCE.validate(s.getLinkAsRef()));
 		doAssert(Diagnostician.INSTANCE.validate(s.getLinkAsClass().getEClass()));
 		doAssert(Diagnostician.INSTANCE.validate(s.getModel()));
 	}
 
 	private static void doAssert(Diagnostic d) {
 		doAssert("", d);
 	}
 
 	private static void doAssert(String prefix, Diagnostic d) {
 		assertTrue(formatMessage(prefix, d), d.getSeverity() == Diagnostic.OK);
 	}
 
 	private static String formatMessage(String prefix, Diagnostic d) {
 		return prefix + "(severity=" + getSeverityTitle(d) + "):" + getSeverityMessage(d);  
 	}
 
 	private static String getSeverityTitle(Diagnostic d) {
 		if ((d.getSeverity() & Diagnostic.CANCEL) != 0) {
 			return "CANCEL";
 		} else if ((d.getSeverity() & Diagnostic.ERROR) != 0) {
 			return "ERROR";
 		} else if ((d.getSeverity() & Diagnostic.WARNING) != 0) {
 			return "WARN";
 		} else if ((d.getSeverity() & Diagnostic.INFO) != 0) {
 			return "INFO";
 		}
 		assert d.getSeverity() == Diagnostic.OK;
 		return "OK";
 	}
 
 	private static String getSeverityMessage(Diagnostic d) {
 		// walk down to find first leaf with same severity as top-level d
 		for (Iterator it = d.getChildren().iterator(); it.hasNext();) {
 			Diagnostic child = (Diagnostic) it.next();
 			if (child.getSeverity() == d.getSeverity()) {
 				if (child.getChildren().isEmpty()) {
 					return child.getMessage();
 				} else {
 					it = child.getChildren().iterator();
 				}
 			}
 			// else try next sibling
 		}
 		return d.getMessage();
 	}
 }
