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
 package org.eclipse.gmf.tests.gen;
 
 import junit.framework.TestCase;
 
 import org.eclipse.emf.codegen.jet.JETException;
 import org.eclipse.gmf.common.codegen.ImportUtil;
 import org.eclipse.gmf.gmfgraph.ConnectionFigure;
 import org.eclipse.gmf.gmfgraph.CustomFigure;
 import org.eclipse.gmf.gmfgraph.Ellipse;
 import org.eclipse.gmf.gmfgraph.Figure;
 import org.eclipse.gmf.gmfgraph.GMFGraphFactory;
 import org.eclipse.gmf.gmfgraph.Label;
 import org.eclipse.gmf.gmfgraph.LineKind;
 import org.eclipse.gmf.gmfgraph.Point;
 import org.eclipse.gmf.gmfgraph.Polygon;
 import org.eclipse.gmf.gmfgraph.PolygonDecoration;
 import org.eclipse.gmf.gmfgraph.PolylineConnection;
 import org.eclipse.gmf.gmfgraph.Rectangle;
 import org.eclipse.gmf.gmfgraph.RoundedRectangle;
import org.eclipse.gmf.gmfgraph.util.RuntimeFQNSwitch;
 import org.eclipse.gmf.graphdef.codegen.FigureGenerator;
 import org.eclipse.jdt.core.compiler.IProblem;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 
 /**
  * TODO generate project, compile and instaniate figures to make sure values are set (like figure's bg/fg color)
  * @author artem
  */
 public class FigureCodegenTestBase extends TestCase {
 	private FigureGenerator figureGenerator;
 
 	public FigureCodegenTestBase(String name) {
 		super(name);
 	}
 	
 	protected final void setCustomFigureGenerator(FigureGenerator generator){
 		figureGenerator = generator;
 	}
 
 	protected final void performTests(Figure f) {
 		generateAndParse(f);
 		//generateAndCompile(getGMFGraphGeneratorConfig(), f);
 	}
 	
 	protected final void generateAndParse(Figure f){
 		try {
 			String res = getGenerator().go(f);
 			
 //			System.err.println("generator result:");
 //			System.err.println(res);
 //			System.err.println("--------------------------------");
 			
 			assertNotNull("Generation should produce code", res);
 			ASTParser p = ASTParser.newParser(AST.JLS3);
 			p.setSource(res.toCharArray());
 			ASTNode astNode = p.createAST(null);
 			assertEquals("Generator is expected to produce cu", astNode.getNodeType(), ASTNode.COMPILATION_UNIT);
 			CompilationUnit cu = (CompilationUnit) astNode;
 			if (getGenerator().getPackageStatement() != null) {
 				assertNotNull("Generator initialized with packageName should produce package statement", cu.getPackage());
 				assertEquals("Package names are different", getGenerator().getPackageStatement(), cu.getPackage().getName().getFullyQualifiedName());
 			}
 			IProblem[] problems = cu.getProblems();
 			for (int i = 0; i < problems.length; i++) {
 				assertFalse(problems[i].getMessage() + ", line:" + problems[i].getSourceLineNumber() + ", pos:" + cu.getColumnNumber(problems[i].getSourceStart()), problems[i].isError());
 			}
 		} catch (JETException ex) {
 			fail(ex.getMessage());
 		}
 	}
 	
 	// custom top-level, hierarchical children. 
 	protected final Figure figure1() {
 		CustomFigure cf = GMFGraphFactory.eINSTANCE.createCustomFigure();
 		cf.setName("MyCylinder");
 		cf.setBundleName("org.eclipse.gmf.runtime.geoshapes");
 		cf.setQualifiedClassName("org.eclipse.gmf.runtime.geoshapes.internal.GeoShapeCylinder");
 		Point p = GMFGraphFactory.eINSTANCE.createPoint();
 		p.setX(1023);
 		p.setY(33);
 		cf.setSize(p);
 		cf.getChildren().add(figure2());
 		Ellipse e = GMFGraphFactory.eINSTANCE.createEllipse();
 		e.setName("Elli");
 		e.setFill(true);
 		cf.getChildren().add(e);
 		return cf;
 	}
 
 	// basic toplevel with plain list of children 
 	protected final Figure figure2() {
 		Rectangle r = GMFGraphFactory.eINSTANCE.createRectangle();
 		r.setName("MyRect");
 		r.setFill(true);
 		r.setLineWidth(3);
 		r.setLineKind(LineKind.LINE_DASHDOT_LITERAL);
 		Label l1 = GMFGraphFactory.eINSTANCE.createLabel();
 		l1.setText("aaaaa");
 		l1.setName("L1");
 		Label l2 = GMFGraphFactory.eINSTANCE.createLabel();
 		l2.setText("bbbbb");
 		l2.setName("L2");
 		r.getChildren().add(l1);
 		r.getChildren().add(l2);
 		return r;
 	}
 
 	// basic toplebel with hierarhical list of children (one of them is custom, another is polygon) 
 	protected final Figure figure3() {
 		RoundedRectangle r = GMFGraphFactory.eINSTANCE.createRoundedRectangle();
 		r.setName("Rounded1");
 		Polygon pg = GMFGraphFactory.eINSTANCE.createPolygon();
 		pg.setName("Polygon1");
 		Point p = GMFGraphFactory.eINSTANCE.createPoint();
 		p.setX(1);
 		p.setY(2);
 		pg.getTemplate().add(p);
 		p = GMFGraphFactory.eINSTANCE.createPoint();
 		p.setX(0);
 		p.setY(3);
 		pg.getTemplate().add(p);
 		p = GMFGraphFactory.eINSTANCE.createPoint();
 		p.setX(-2);
 		p.setY(-1);
 		pg.getTemplate().add(p);
 		r.getChildren().add(pg);
 		r.getChildren().add(figure1());
 		return r;
 	}
 
 	protected final ConnectionFigure ecoreContainmentRef() {
 		PolylineConnection cf = GMFGraphFactory.eINSTANCE.createPolylineConnection();
 		cf.setName("ContainmentRef");
 		PolygonDecoration df = createRhomb();
 		df.setFill(true);
 		cf.setSourceDecoration(df);
 		return cf;
 	}
 
 	protected final PolygonDecoration createRhomb() {
 		PolygonDecoration df = GMFGraphFactory.eINSTANCE.createPolygonDecoration();
 		Point p = GMFGraphFactory.eINSTANCE.createPoint();
 		p.setX(-1);
 		p.setY(1);
 		df.getTemplate().add(p);
 		p = GMFGraphFactory.eINSTANCE.createPoint();
 		p.setX(0);
 		p.setY(0);
 		df.getTemplate().add(p);
 		p = GMFGraphFactory.eINSTANCE.createPoint();
 		p.setX(-1);
 		p.setY(-1);
 		df.getTemplate().add(p);
 		p = GMFGraphFactory.eINSTANCE.createPoint();
 		p.setX(-2);
 		p.setY(0);
 		df.getTemplate().add(p);
 		return df;
 	}
 
 	protected final String getFigurePackageName(){
 		return "org.eclipse.gmf.tests.sample.figures";
 	}
 	
 	protected final String getTestPluginName(){
 		return "org.eclipse.gmf.tests.sample.figures." + getName() + ".t" + System.currentTimeMillis();
 	}
 	
 	protected final String getPluginActivatorClassFQN(){
 		return getFigurePackageName() + ".PluginActivator"; 
 	}
 
 	private FigureGenerator getGenerator() {
 		if (figureGenerator == null) {
 			String packageName = getFigurePackageName();
			figureGenerator = new FigureGenerator(packageName, new ImportUtil(packageName), new RuntimeFQNSwitch());
 		}
 		return figureGenerator;
 	}
 	
 }
