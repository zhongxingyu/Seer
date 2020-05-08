 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.bot.tests;
 
 import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.util.List;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.graphiti.bot.tests.util.ITestConstants;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.MultiText;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.testtool.sketch.SketchFeatureProvider;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.internal.command.CreateModelObjectCommand;
 import org.eclipse.graphiti.ui.internal.editor.GFFigureCanvas;
 import org.eclipse.graphiti.ui.internal.policy.ShapeXYLayoutEditPolicy;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swtbot.eclipse.gef.finder.finders.PaletteFinder;
 import org.eclipse.swtbot.eclipse.gef.finder.matchers.AbstractToolEntryMatcher;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.hamcrest.Description;
 import org.junit.Test;
 
 public class GFInteractionComponentTests extends AbstractGFTests {
 
 	/**
 	 * 
 	 */
 	private static final String SHAPE_NAME = "Connection";
 	/**
 	 * 
 	 */
 	private static final String INPUT_STRING = "Graphiti Rulez";
 
 	public GFInteractionComponentTests() {
 		super();
 	}
 
 	@Test
 	public void testSelectionTool() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		final SWTBotGefEditor ed = getGefEditor();
 		createClassesAndConnection(x, y, diagramEditor, ed, null);
 		Thread.sleep(1000);
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(SHAPE_NAME, 200, 50);
 				ed.drag("ConnectionDecorator", 200, 400);
 			}
 		});
 		Thread.sleep(1000);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testMarqueeTool() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		final SWTBotGefEditor ed = getGefEditor();
 		createClassesAndConnection(x, y, diagramEditor, ed, "Marquee");
 		Thread.sleep(1000);
 		// Select the newly added shapes with the marquee tool.
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(x - 10, y - 10, x + 500, y + 500);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.activateTool("Select");
 				ed.drag(x + 50, y + 50, x + 200, y + 50);
 			}
 		});
 		Thread.sleep(1000);
 		SWTBotGefEditPart editPart = ed.getEditPart(SHAPE_NAME);
 		IFigure figure = ((GraphicalEditPart) editPart.part()).getFigure();
		assertEquals(x + 150, figure.getBounds().x);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testGFFigureCanvas() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// find diagram
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 				TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						// add a class to the diagram
 						addClassToDiagram(fp, currentDiagram, x, y, SHAPE_NAME);
 					}
 				});
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// move class-shape to origin
 				ed.drag(SHAPE_NAME, -100, -100);
 			}
 		});
 		Thread.sleep(500);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// get instance of GFFigureCanvas
 				GFFigureCanvas gfFigureCanvas = getGFCanvas();
 
 				// do some scrolling
 				Event e = new Event();
 				e.doit = true;
 				e.stateMask = 0;
 
 				e.detail = SWT.ARROW_UP;
 				gfFigureCanvas.getHorizontalBar().notifyListeners(SWT.Selection, e);
 				gfFigureCanvas.getVerticalBar().notifyListeners(SWT.Selection, e);
 
 				e.detail = SWT.ARROW_DOWN;
 				gfFigureCanvas.getHorizontalBar().notifyListeners(SWT.Selection, e);
 				gfFigureCanvas.getVerticalBar().notifyListeners(SWT.Selection, e);
 
 				e.detail = SWT.PAGE_UP;
 				gfFigureCanvas.getHorizontalBar().notifyListeners(SWT.Selection, e);
 				gfFigureCanvas.getVerticalBar().notifyListeners(SWT.Selection, e);
 
 				e.detail = SWT.PAGE_DOWN;
 				gfFigureCanvas.getHorizontalBar().notifyListeners(SWT.Selection, e);
 				gfFigureCanvas.getVerticalBar().notifyListeners(SWT.Selection, e);
 			}
 		});
 		Thread.sleep(500);
 		
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				Event event = new Event();
 				event.type = SWT.KeyDown;
 				event.keyCode = SWT.DEL;
 				event.character = SWT.DEL;
 				event.stateMask = 0; // SWT.SHIFT;
 				Display.getDefault().post(event);
 			}
 		});
 		Thread.sleep(500);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				Event event = new Event();
 				event.type = SWT.KeyDown;
 				event.keyCode = SWT.CR;
 				event.character = SWT.CR;
 				event.stateMask = 0; // SWT.SHIFT;
 				Display.getDefault().post(event);
 			}
 		});
 		Thread.sleep(500);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				GFFigureCanvas gfFigureCanvas = getGFCanvas(ed);
 				// regain space
 				gfFigureCanvas.regainSpace();
 			}
 		});
 		Thread.sleep(500);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testMouseLocation() throws Exception {
 		/*
 		 * regression test for CSN 0120031469 0003790113 2008
 		 */
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// find diagram
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 				TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						// add a class to the diagram
 						addClassToDiagram(fp, currentDiagram, x, y, SHAPE_NAME);
 					}
 				});
 			}
 		});
 		Thread.sleep(1000);
 
 		// move class-shape to the origin (0,0)
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// move class-shape to origin
 				ed.drag(SHAPE_NAME, 0, 0);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// get instance of GFFigureCanvas
 				GFFigureCanvas gfFigureCanvas = getGFCanvas(ed);
 				if (gfFigureCanvas == null)
 					return;
 				// scroll shape into visible region
 				Event e = new Event();
 				e.doit = true;
 				e.stateMask = 0;
 
 				e.detail = SWT.ARROW_UP;
 				gfFigureCanvas.getHorizontalBar().notifyListeners(SWT.Selection, e);
 				gfFigureCanvas.getVerticalBar().notifyListeners(SWT.Selection, e);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 					Point p = getOrigin(ed);
 					Display display = GraphitiUiInternal.getWorkbenchService().getShell().getDisplay();
 					Event event = createMouseEvent(p.x + 35, p.y + 35, 0, 0, 0);
 					event.type = SWT.MouseMove;
 					event.widget = diagramEditor.getGraphicalViewer().getControl();
 					event.display = display;
 					display.post(event);
 			}
 		});
 		Thread.sleep(1000);
 
 		PictogramElement[] selectedPictogramElements = diagramEditor.getSelectedPictogramElements();
 		for (PictogramElement pictogramElement : selectedPictogramElements) {
 			if (pictogramElement instanceof ContainerShape) {
 				GraphicsAlgorithm containerShapeGa = pictogramElement.getGraphicsAlgorithm();
 				Rectangle rectangle = new Rectangle(containerShapeGa.getX(), containerShapeGa.getY(), containerShapeGa.getWidth(),
 						containerShapeGa.getHeight());
 				org.eclipse.draw2d.geometry.Point mouseLocation = diagramEditor
 						.calculateRealMouseLocation(diagramEditor.getMouseLocation());
 				boolean mouseIsInsideShape = rectangle.contains(mouseLocation);
 				assertEquals(" Wrong mouse coordinates :  ", true, mouseIsInsideShape);
 				break;
 			}
 		}
 		Thread.sleep(500);
 		shutdownEditor(diagramEditor);
 	}
 
 
 	@Test
 	public void testContextButtons() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 
 				TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						addClassToDiagram(fp, currentDiagram, x, y, SHAPE_NAME);
 					}
 				});
 
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				Robot r;
 				try {
 					r = new Robot();
 					Point p = getOrigin(ed);
 					r.mouseMove(p.x + 150, p.y + 150);
 				} catch (AWTException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				try {
 					final Robot robot = new Robot();
 					robot.setAutoDelay(1);
 					try {
 						robot.mousePress(InputEvent.BUTTON3_MASK);
 					} catch (RuntimeException e) {
 						e.printStackTrace();
 					} finally {
 						robot.mouseRelease(InputEvent.BUTTON3_MASK);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				try {
 					final Robot robot = new Robot();
 					robot.setAutoDelay(1);
 					try {
 						robot.keyPress(KeyEvent.VK_ESCAPE);
 						robot.keyRelease(KeyEvent.VK_ESCAPE);
 						robot.keyPress(KeyEvent.VK_DELETE);
 					} catch (RuntimeException e) {
 						e.printStackTrace();
 					} finally {
 						robot.keyRelease(KeyEvent.VK_DELETE);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				try {
 					final Robot robot = new Robot();
 					robot.setAutoDelay(1);
 
 					try {
 						robot.keyPress(KeyEvent.VK_ENTER);
 					} catch (RuntimeException e) {
 						e.printStackTrace();
 					} finally {
 						robot.keyRelease(KeyEvent.VK_ENTER);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 		});
 		Thread.sleep(1000);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testDropOnConnection() throws Exception {
 		final int xOfShape1 = 100;
 		final int yOfShape1 = 100;
 		final int xOfShape2 = 100;
 		final int yOfShape2 = 400;
 		final int xOfShape3 = 400;
 		final int yOfShape3 = 100;
 		final int xOfShape4 = 400;
 		final int yOfShape4 = 400;
 		final int width = 60;
 		final int height = 60;
 		final int DIL = 30;
 
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if (!("Rectangle".equals(createFeature.getName())))
 						continue;
 
 					Rectangle rectangle = new Rectangle(xOfShape1, yOfShape1, width, height);
 					ICreateContext createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 					Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 							createContext, rectangle);
 					commandStack.execute(createCommand);
 
 					rectangle = new Rectangle(xOfShape2, yOfShape2, width, height);
 					createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 					createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature, createContext,
 							rectangle);
 					commandStack.execute(createCommand);
 
 					rectangle = new Rectangle(xOfShape3, yOfShape3, width, height);
 					createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 					createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature, createContext,
 							rectangle);
 					commandStack.execute(createCommand);
 
 					rectangle = new Rectangle(xOfShape4, yOfShape4, width, height);
 					createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 					createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature, createContext,
 							rectangle);
 					commandStack.execute(createCommand);
 				}
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.activateTool("free");
 				ed.drag(xOfShape1 + DIL, yOfShape1 + DIL, xOfShape3 + DIL, yOfShape3 + DIL);
 				ed.click(xOfShape3 + DIL, yOfShape3 + DIL);
 				ed.activateTool("free");
 				ed.drag(xOfShape1 + DIL, yOfShape1 + DIL, xOfShape2 + DIL, yOfShape2 + DIL);
 				ed.click(xOfShape2 + DIL, yOfShape2 + DIL);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.activateTool("Rectangle");
 				// click on connection to insert rectangle between two other rectangles
 				ed.click((xOfShape1 + 60 + xOfShape3) / 2, (yOfShape1 + 60 + yOfShape3) / 2);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(xOfShape4 + DIL, yOfShape4 + DIL, xOfShape1 + DIL, (yOfShape2 + yOfShape1 + 2 * DIL) / 2);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(xOfShape1 + DIL, yOfShape1 + DIL, (xOfShape1 + 2 * DIL + xOfShape3) / 2, (yOfShape2 + yOfShape1 + 2 * DIL) / 2);
 			}
 		});
 		Thread.sleep(1000);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testDirectEditingMultiText() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if (!(SketchFeatureProvider.CF_RECTANGLE_MULTI_TEXT.equals(createFeature.getName())))
 						continue;
 
 					Rectangle rectangle = new Rectangle(x, y, 100, 100);
 					ICreateContext createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 					Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 							createContext, rectangle);
 					commandStack.execute(createCommand);
 				}
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				diagramEditor.getMouseLocation().x = 150;
 				diagramEditor.getMouseLocation().y = 150;
 				SWTBotGefEditPart ep = ed.mainEditPart().children().get(0);
 				ep.activateDirectEdit();
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.directEditType(INPUT_STRING);
 			}
 		});
 
 		// Check results
 		SWTBotGefEditPart ep = ed.mainEditPart().children().get(0);
 		Shape shape = (Shape) ep.part().getModel();
 		String value = ((MultiText) shape.getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0)).getValue();
 		assertEquals(INPUT_STRING, value);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testDirectEditingSingleText() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if (SketchFeatureProvider.CF_RECTANGLE_SINGLE_TEXT.equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(x, y, 100, 100);
 						ICreateContext createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 						break;
 					}
 				}
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				diagramEditor.getMouseLocation().x = 150;
 				diagramEditor.getMouseLocation().y = 150;
 				SWTBotGefEditPart ep = ed.mainEditPart().children().get(0);
 				ep.activateDirectEdit();
 			}
 		});
 		Thread.sleep(1000);
 
 		final Robot robot = new Robot();
 		robot.setAutoDelay(50);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				robot.keyPress(KeyEvent.VK_CONTROL);
 				robot.keyPress(KeyEvent.VK_SPACE);
 				robot.keyRelease(KeyEvent.VK_SPACE);
 				robot.keyRelease(KeyEvent.VK_CONTROL);
 				robot.keyPress(KeyEvent.VK_ENTER);
 				robot.keyRelease(KeyEvent.VK_ENTER);
 			}
 		});
 		Thread.sleep(1000);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testDropIntoContainer() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if ("Rectangle".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(x, y, 100, 100);
 						ICreateContext createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					} else if ("Rectangle Container".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(x + 300, y - 50, 200, 200);
 						ICreateContext createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					}
 				}
 			}
 
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(x + 50, y + 50, x + 300 + 100, y + 50);
 			}
 		});
 		Thread.sleep(1000);
 		assertTrue(ed.mainEditPart().children().size() == 1);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testConnectionReassign() throws Exception {
 		final int xContainer1 = 100;
 		final int yContainer1 = 100;
 		final int xContainer2 = 300;
 		final int yContainer2 = 100;
 		final int xContainer3 = 100;
 		final int yContainer3 = 300;
 		final int containerSize = 100;
 		final int rectangleSize = 60;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if ("Rectangle".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(xContainer1 + containerSize, yContainer1 + containerSize, rectangleSize,
 								rectangleSize);
 						ICreateContext createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 
 						rectangle = new Rectangle(xContainer2 + containerSize, yContainer2 + containerSize, rectangleSize, rectangleSize);
 						createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					} else if ("Rectangle Container".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(xContainer1, yContainer1, containerSize, containerSize);
 						ICreateContext createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 
 						rectangle = new Rectangle(xContainer2, yContainer2, containerSize, containerSize);
 						createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 
 						rectangle = new Rectangle(xContainer3, yContainer3, containerSize, containerSize);
 						createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					}
 				}
 			}
 
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				int currX = xContainer1 + containerSize + rectangleSize / 2;
 				int currY = yContainer1 + containerSize + rectangleSize / 2;
 				ed.drag(currX, currY, currX - (containerSize + rectangleSize) / 2, currY - (containerSize + rectangleSize) / 2);
 				currX = xContainer2 + containerSize + rectangleSize / 2;
 				currY = yContainer2 + containerSize + rectangleSize / 2;
 				ed.drag(currX, currY, currX - (containerSize + rectangleSize) / 2, currY - (containerSize + rectangleSize) / 2);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.activateTool("free");
 				ed.click(100 + containerSize / 2, 100 + containerSize / 2);
 				ed.click(300 + containerSize / 2, 100 + containerSize / 2);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.activateDefaultTool();
 				// move to connection start point
 				ed.click(300 + containerSize / 2 - rectangleSize / 2 - 5, 100 + containerSize / 2); // middle
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(300 + containerSize / 2 - rectangleSize / 2 - 1, 100 + containerSize / 2, 300 + containerSize / 2 - rectangleSize
 						/ 2 - 10, 100 + containerSize / 2);
 			}
 		});
 		Thread.sleep(1000);
 		// This is an asynchronous callto the UI.
 		ed.drag(100 + containerSize / 2, 100 + containerSize / 2, 100 + containerSize / 2, 300 + containerSize / 2);
 		Thread.sleep(1000);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testCreateConnection() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if ("Rectangle".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(x, y, 100, 100);
 						ICreateContext createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 						rectangle = new Rectangle(x + 200, y, 100, 100);
 						createContext = ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 						createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					}
 				}
 			}
 
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// activate create-freeform-connection tool
 				ed.activateTool("free");
 				ed.drag(150, 150, 350, 150);
 				ed.click(350, 150);
 			}
 		});
 		Thread.sleep(1000);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// activate selection tool
 				ed.activateDefaultTool();
 				// move to line-center and select line
 				ed.click(250, 150); // middle
 			}
 		});
 		Thread.sleep(500);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(250, 150, 250, 100);
 			}
 		});
 		Thread.sleep(500);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(250, 100, 250, 200);
 			}
 		});
 		Thread.sleep(500);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(250, 200, 250, 150);
 			}
 		});
 		Thread.sleep(500);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(250, 150, 250, 195);
 			}
 		});
 		Thread.sleep(500);
 
 		// remove the connection via context menu
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.click(250, 195);
 				ed.clickContextMenu("Remove");
 			}
 		});
 		Thread.sleep(1000);
 		shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testResizeAndPosition() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		final SWTBotGefEditor ed = getGefEditor();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				// find diagram
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 				TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						// add a class to the diagram
 						addClassToDiagram(fp, currentDiagram, x, y, SHAPE_NAME);
 					}
 				});
 			}
 		});
 		Thread.sleep(1000);
 
 		// select shape
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				final SWTBotGefEditPart editPart = ed.getEditPart(SHAPE_NAME);
 				ed.select(editPart);
 			}
 		});
 		Thread.sleep(1000);
 
 		// resize shape
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ed.drag(100, 100, 120, 120);
 			}
 		});
 		Thread.sleep(1000);
 
 		SWTBotGefEditPart editPart = ed.getEditPart(SHAPE_NAME);
 		IFigure figure = ((GraphicalEditPart) editPart.part()).getFigure();
 		assertEquals(120, figure.getBounds().x);
 		assertEquals(120, figure.getBounds().y);
 	}
 
 	@Test
 	public void testSketchAll() throws Exception {
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		final SWTBotGefEditor ed = getGefEditor();
 		PaletteFinder paletteFinder = new PaletteFinder(diagramEditor.getEditDomain());
 		List<PaletteEntry> objectCreationTools = paletteFinder.findEntries(new AbstractToolEntryMatcher() {
 
 			@Override
 			public void describeTo(Description description) {
 				// Do nothing
 			}
 
 			@Override
 			protected boolean matches(ToolEntry toolEntry) {
 				if (toolEntry.getParent().getLabel() == "Objects")
 					return true;
 				return false;
 			}
 		});
 		final int x = 10;
 		final int y = 6 * objectCreationTools.size();
 		final int[] counter = new int[] { 0 };
 		for (PaletteEntry o : objectCreationTools) {
 			if (o instanceof ToolEntry) {
 				final ToolEntry toolEntry = (ToolEntry) o;
 				syncExec(new VoidResult() {
 					@Override
 					public void run() {
 						ed.activateTool(toolEntry.getLabel());
 						int currX = x + (counter[0] * 5);
 						int currY = y - (5 * counter[0]);
 						ed.drag(currX, currY, currX + 10, currY + 10);
 					}
 				});
 				//				Thread.sleep(300);
 				counter[0]++;
 			}
 		}
 		Thread.sleep(1000);
 		assertEquals(objectCreationTools.size(), ed.mainEditPart().children().size());
 		shutdownEditor(diagramEditor);
 	}
 
 	// @Test
 	// public void testEventing() throws Exception {
 	// final int x = 100;
 	// final int y = 100;
 	// final DiagramEditor diagramEditor =
 	// openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 	// final SWTBotGefEditor ed = getGefEditor();
 	// syncExec(new VoidResult() {
 	// @Override
 	// public void run() {
 	// IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 	// IFeatureProvider fp = dtp.getFeatureProvider();
 	//
 	// CommandStack commandStack =
 	// diagramEditor.getEditDomain().getCommandStack();
 	//
 	// ICreateFeature[] createFeatures = fp.getCreateFeatures();
 	// for (ICreateFeature createFeature : createFeatures) {
 	// if ("Rectangle".equals(createFeature.getName())) {
 	// Rectangle rectangle = new Rectangle(x, y, 100, 100);
 	// ICreateContext createContext =
 	// ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 	// Command createCommand = new
 	// CreateModelObjectCommand(diagramEditor.getConfigurationProvider(),
 	// createFeature,
 	// createContext, rectangle);
 	// commandStack.execute(createCommand);
 	// } else if ("Rectangle Container".equals(createFeature.getName())) {
 	// Rectangle rectangle = new Rectangle(x + 300, y - 50, 200, 200);
 	// ICreateContext createContext =
 	// ShapeXYLayoutEditPolicy.createCreateContext(dtp.getDiagram(), rectangle);
 	// Command createCommand = new
 	// CreateModelObjectCommand(diagramEditor.getConfigurationProvider(),
 	// createFeature,
 	// createContext, rectangle);
 	// commandStack.execute(createCommand);
 	// }
 	// }
 	// }
 	//
 	// });
 	// Thread.sleep(1000);
 	//
 	// syncExec(new VoidResult() {
 	// @Override
 	// public void run() {
 	// ed.drag(x + 50, y + 50, x + 300 + 100, y + 50);
 	// }
 	// });
 	// Thread.sleep(1000);
 	// assertTrue(ed.mainEditPart().children().size() == 1);
 	// }
 
 	private void createClassesAndConnection(final int x, final int y, final DiagramEditor diagramEditor, final SWTBotGefEditor ed,
 			final String toolToActivate) {
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 
 				TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						addClassesAndReferenceToDiagram(fp, currentDiagram, x, y, SHAPE_NAME, x, y + 300, "ConnectionDecorator");
 					}
 				});
 				if (toolToActivate != null)
 					ed.activateTool(toolToActivate);
 			}
 		});
 	}
 
 	protected Event createMouseEvent(int x, int y, int button, int stateMask, int count) {
 		Event event = new Event();
 		event.time = (int) System.currentTimeMillis();
 		event.x = x;
 		event.y = y;
 		event.button = button;
 		event.stateMask = stateMask;
 		event.count = count;
 		return event;
 	}
 }
