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
 import java.util.Iterator;
 
 import junit.framework.TestCase;
 
 import org.eclipse.emf.common.util.Diagnostic;
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
 			doAssert(Diagnostician.INSTANCE.validate(s.getCanvas()));
 		} catch (IOException ex) {
 			fail(ex.getMessage());
 		}
 	}
 	
//	public void testLibraryGen() {
//		try {
//			DiaGenSource s = new DiaGenFileSetup().init(Plugin.createURI("/models/library/library.gmfgen"));
//			doAssert(Diagnostician.INSTANCE.validate(s.getGenDiagram()));
//		} catch (IOException ex) {
//			fail(ex.getMessage());
//		}
//	}
 
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
 		doAssert(Diagnostician.INSTANCE.validate(s.getDiagramElement()));
 		doAssert(Diagnostician.INSTANCE.validate(s.getNodeA().getEClass()));
 		doAssert(Diagnostician.INSTANCE.validate(s.getLinkAsRef()));
 		doAssert(Diagnostician.INSTANCE.validate(s.getLinkAsClass().getEClass()));
 		doAssert(Diagnostician.INSTANCE.validate(s.getModel()));
 	}
 
 	public void testDiaGenSetupDM() {
 		doDiaGenTests(new DiaGenSetup().init(new DomainModelSetup().init()));
 	}
 
 	public void testDiaGenSetupMap() {
 		doDiaGenTests(new DiaGenSetup().init(new MapSetup().init(new DiaDefSetup(null).init(), new DomainModelSetup().init())));
 	}
 
 	private void doDiaDefTests(DiaDefSource s) {
 		doAssert(Diagnostician.INSTANCE.validate(s.getCanvasDef()));
 	}
 
 	private void doDiaGenTests(DiaGenSource s) {
 		Diagnostic d = Diagnostician.INSTANCE.validate(s.getNodeA());
 		doAssert("GenNode", d);
 		d = Diagnostician.INSTANCE.validate(s.getLinkC());
 		doAssert("GenLink", d);
 		d = Diagnostician.INSTANCE.validate(s.getGenDiagram());
 		doAssert("GenDiagram", d);
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
