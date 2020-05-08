 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 355027: Move of connection decorators when zoom level != 100 behaves weird
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
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.graphiti.bot.tests.util.ITestConstants;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.DefaultFeatureProviderWrapper;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.context.impl.CustomContext;
 import org.eclipse.graphiti.internal.command.CommandContainer;
 import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.MultiText;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.testtool.ecore.TestToolBehavior;
 import org.eclipse.graphiti.testtool.sketch.SketchFeatureProvider;
 import org.eclipse.graphiti.testtool.sketch.features.ToggleDecorator;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateFreeformConnectionFeature;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateGaContainerFeature;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateGaShapeFeature;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.internal.command.CreateModelObjectCommand;
 import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
 import org.eclipse.graphiti.ui.internal.editor.GFFigureCanvas;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.eclipse.gef.finder.finders.PaletteFinder;
 import org.eclipse.swtbot.eclipse.gef.finder.matchers.AbstractToolEntryMatcher;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
 import org.hamcrest.Description;
 import org.junit.Test;
 
 public class GFInteractionComponentTests extends AbstractGFTests {
 
 	private static final int SHORT_DELAY = 500;
 	private static final int DELAY = 1000;
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
 		createClassesAndConnection(x, y, diagramEditor.getDiagramTypeProvider(), null, SHAPE_NAME);
 		Thread.sleep(DELAY);
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.getGefEditor().drag(SHAPE_NAME, 200, 50);
 				ed.getGefEditor().drag("ConnectionDecorator", 200, 400);
 			}
 		});
 		Thread.sleep(DELAY);
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testHidePalette() throws Exception {
 		boolean oldValue = TestToolBehavior.showFlyoutPalette;
 		TestToolBehavior.setShowFlyoutPalette(true);
 		DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		assertNotNull(diagramEditor.getEditDomain().getPaletteViewer());
 		page.shutdownEditor(diagramEditor);
 		TestToolBehavior.setShowFlyoutPalette(false);
 		diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		assertNull(diagramEditor.getEditDomain().getPaletteViewer());
 		page.shutdownEditor(diagramEditor);
 		TestToolBehavior.setShowFlyoutPalette(oldValue);
 	}
 
 	@Test
 	public void testMarqueeTool() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		createClassesAndConnection(x, y, diagramEditor.getDiagramTypeProvider(), "Marquee", SHAPE_NAME);
 		Thread.sleep(DELAY);
 		// Select the newly added shapes with the marquee tool.
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.drag(x - 10, y - 10, x + SHORT_DELAY, y + SHORT_DELAY);
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.getGefEditor().activateTool("Select");
 				ed.getGefEditor().drag(x + 50, y + 50, x + 200, y + 50);
 			}
 		});
 		Thread.sleep(DELAY);
 		IFigure figure = ed.getFigureWithLabel(SHAPE_NAME);
 		// Drag might not be accurate, add tolerance +-1
 		assertTrue((x + 149 <= figure.getBounds().x) && (figure.getBounds().x <= x + 151));
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testGFFigureCanvas() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		syncExec(new VoidResult() {
 			public void run() {
 				// find diagram
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 				executeInRecordingCommand(diagramEditor, new Runnable() {
 					public void run() {
 						// add a class to the diagram
 						addClassToDiagram(fp, currentDiagram, x, y, SHAPE_NAME);
 					}
 				});
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				// move class-shape to origin
 				ed.getGefEditor().drag(SHAPE_NAME, -100, -100);
 			}
 		});
 		Thread.sleep(SHORT_DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				// get instance of GFFigureCanvas
 				GFFigureCanvas gfFigureCanvas = ed.getGFCanvas();
 
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
 		Thread.sleep(SHORT_DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				Event event = new Event();
 				event.type = SWT.KeyDown;
 				event.keyCode = SWT.DEL;
 				event.character = SWT.DEL;
 				event.stateMask = 0; // SWT.SHIFT;
 				Display.getDefault().post(event);
 			}
 		});
 		Thread.sleep(SHORT_DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				Event event = new Event();
 				event.type = SWT.KeyDown;
 				event.keyCode = SWT.CR;
 				event.character = SWT.CR;
 				event.stateMask = 0; // SWT.SHIFT;
 				Display.getDefault().post(event);
 			}
 		});
 		Thread.sleep(SHORT_DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				GFFigureCanvas gfFigureCanvas = ed.getGFCanvas();
 				// regain space
 				gfFigureCanvas.regainSpace();
 			}
 		});
 		Thread.sleep(SHORT_DELAY);
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testMouseLocation() throws Exception {
 		/*
 		 * regression test for CSN 0120031469 0003790113 2008
 		 */
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				// find diagram
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 				executeInRecordingCommand(diagramEditor, new Runnable() {
 					public void run() {
 						// add a class to the diagram
 						addClassToDiagram(fp, currentDiagram, x, y, SHAPE_NAME);
 					}
 				});
 			}
 		});
 		Thread.sleep(DELAY);
 		// move class-shape to the origin (0,0)
 		ed.drag(SHAPE_NAME, 0, 0);
 		Thread.sleep(DELAY);
 		syncExec(new VoidResult() {
 			public void run() {
 				// get instance of GFFigureCanvas
 				GFFigureCanvas gfFigureCanvas = ed.getGFCanvas();
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
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				Point p = ed.getOrigin();
 				Display display = GraphitiUiInternal.getWorkbenchService().getShell().getDisplay();
 				Event event = createMouseEvent(p.x + 35, p.y + 35, 0, 0, 0);
 				event.type = SWT.MouseMove;
 				event.widget = diagramEditor.getGraphicalViewer().getControl();
 				event.display = display;
 				display.post(event);
 			}
 		});
 		Thread.sleep(DELAY);
 
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
 		Thread.sleep(SHORT_DELAY);
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testContextButtons() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 				executeInRecordingCommand(diagramEditor, new Runnable() {
 					public void run() {
 						addClassToDiagram(fp, currentDiagram, x, y, SHAPE_NAME);
 					}
 				});
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				Robot r;
 				try {
 					r = new Robot();
 					Point p = ed.getOrigin();
 					r.mouseMove(p.x + 150, p.y + 150);
 				} catch (AWTException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
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
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
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
 		Thread.sleep(DELAY);
 		page.shutdownEditor(diagramEditor);
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
 
 		syncExec(new VoidResult() {
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if (!("Rectangle".equals(createFeature.getName())))
 						continue;
 
 					Rectangle rectangle = new Rectangle(xOfShape1, yOfShape1, width, height);
 					ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 					Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 							createContext, rectangle);
 					commandStack.execute(createCommand);
 
 					rectangle = new Rectangle(xOfShape2, yOfShape2, width, height);
 					createContext = createCreateContext(dtp.getDiagram(), rectangle);
 					createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature, createContext,
 							rectangle);
 					commandStack.execute(createCommand);
 
 					rectangle = new Rectangle(xOfShape3, yOfShape3, width, height);
 					createContext = createCreateContext(dtp.getDiagram(), rectangle);
 					createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature, createContext,
 							rectangle);
 					commandStack.execute(createCommand);
 
 					rectangle = new Rectangle(xOfShape4, yOfShape4, width, height);
 					createContext = createCreateContext(dtp.getDiagram(), rectangle);
 					createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature, createContext,
 							rectangle);
 					commandStack.execute(createCommand);
 				}
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.getGefEditor().activateTool("free");
 				ed.getGefEditor().drag(xOfShape1 + DIL, yOfShape1 + DIL, xOfShape3 + DIL, yOfShape3 + DIL);
 				ed.getGefEditor().click(xOfShape3 + DIL, yOfShape3 + DIL);
 				ed.getGefEditor().activateTool("free");
 				ed.getGefEditor().drag(xOfShape1 + DIL, yOfShape1 + DIL, xOfShape2 + DIL, yOfShape2 + DIL);
 				ed.getGefEditor().click(xOfShape2 + DIL, yOfShape2 + DIL);
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.getGefEditor().activateTool("Rectangle");
 				// click on connection to insert rectangle between two other rectangles
 				ed.getGefEditor().click((xOfShape1 + 60 + xOfShape3) / 2, (yOfShape1 + 60 + yOfShape3) / 2);
 			}
 		});
 		Thread.sleep(DELAY);
 		ed.drag(xOfShape4 + DIL, yOfShape4 + DIL, xOfShape1 + DIL, (yOfShape2 + yOfShape1 + 2 * DIL) / 2);
 		Thread.sleep(DELAY);
 		ed.drag(xOfShape1 + DIL, yOfShape1 + DIL, (xOfShape1 + 2 * DIL + xOfShape3) / 2, (yOfShape2 + yOfShape1 + 2 * DIL) / 2);
 		Thread.sleep(DELAY);
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testDirectEditingMultiText() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if (!(SketchFeatureProvider.CF_RECTANGLE_MULTI_TEXT.equals(createFeature.getName())))
 						continue;
 
 					Rectangle rectangle = new Rectangle(x, y, 100, 100);
 					ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 					Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 							createContext, rectangle);
 					commandStack.execute(createCommand);
 				}
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				diagramEditor.getMouseLocation().x = 150;
 				diagramEditor.getMouseLocation().y = 150;
 				SWTBotGefEditPart ep = ed.getGefEditor().mainEditPart().children().get(0);
 				ep.activateDirectEdit();
 			}
 		});
 		Thread.sleep(DELAY);
 		ed.getGefEditor().directEditType(INPUT_STRING);
 		// Check results
 		SWTBotGefEditPart ep = ed.getGefEditor().mainEditPart().children().get(0);
 		Shape shape = (Shape) ep.part().getModel();
 		String value = ((MultiText) shape.getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0)).getValue();
 		assertEquals(INPUT_STRING, value);
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testDirectEditingSingleText() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if (SketchFeatureProvider.CF_RECTANGLE_SINGLE_TEXT.equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(x, y, 100, 100);
 						ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 						break;
 					}
 				}
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				diagramEditor.getMouseLocation().x = 150;
 				diagramEditor.getMouseLocation().y = 150;
 				SWTBotGefEditPart ep = ed.getGefEditor().mainEditPart().children().get(0);
 				ep.activateDirectEdit();
 			}
 		});
 		Thread.sleep(DELAY);
 
 		final Robot robot = new Robot();
 		robot.setAutoDelay(50);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				robot.keyPress(KeyEvent.VK_CONTROL);
 				robot.keyPress(KeyEvent.VK_SPACE);
 				robot.keyRelease(KeyEvent.VK_SPACE);
 				robot.keyRelease(KeyEvent.VK_CONTROL);
 				robot.keyPress(KeyEvent.VK_ENTER);
 				robot.keyRelease(KeyEvent.VK_ENTER);
 			}
 		});
 		Thread.sleep(DELAY);
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testDropIntoContainer() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if ("Rectangle".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(x, y, 100, 100);
 						ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					} else if ("Rectangle Container".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(x + 300, y - 50, 200, 200);
 						ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					}
 				}
 			}
 
 		});
 		Thread.sleep(DELAY);
 		ed.drag(x + 50, y + 50, x + 300 + 100, y + 50);
 		Thread.sleep(DELAY);
 		assertTrue(ed.getGefEditor().mainEditPart().children().size() == 1);
 		page.shutdownEditor(diagramEditor);
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
 
 		syncExec(new VoidResult() {
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if ("Rectangle".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(xContainer1 + containerSize, yContainer1 + containerSize, rectangleSize,
 								rectangleSize);
 						ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 
 						rectangle = new Rectangle(xContainer2 + containerSize, yContainer2 + containerSize, rectangleSize, rectangleSize);
 						createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					} else if ("Rectangle Container".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(xContainer1, yContainer1, containerSize, containerSize);
 						ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 
 						rectangle = new Rectangle(xContainer2, yContainer2, containerSize, containerSize);
 						createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 
 						rectangle = new Rectangle(xContainer3, yContainer3, containerSize, containerSize);
 						createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					}
 				}
 			}
 
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				int currX = xContainer1 + containerSize + rectangleSize / 2;
 				int currY = yContainer1 + containerSize + rectangleSize / 2;
 				ed.drag(currX, currY, currX - (containerSize + rectangleSize) / 2, currY - (containerSize + rectangleSize) / 2);
 				currX = xContainer2 + containerSize + rectangleSize / 2;
 				currY = yContainer2 + containerSize + rectangleSize / 2;
 				ed.drag(currX, currY, currX - (containerSize + rectangleSize) / 2, currY - (containerSize + rectangleSize) / 2);
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.getGefEditor().activateTool("free");
 				ed.getGefEditor().click(100 + containerSize / 2, 100 + containerSize / 2);
 				ed.getGefEditor().click(300 + containerSize / 2, 100 + containerSize / 2);
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.getGefEditor().activateDefaultTool();
 				// move to connection start point
 				ed.getGefEditor().click(300 + containerSize / 2 - rectangleSize / 2 - 5, 100 + containerSize / 2); // middle
 			}
 		});
 		Thread.sleep(DELAY);
 		ed.drag(300 + containerSize / 2 - rectangleSize / 2 - 1, 100 + containerSize / 2, 300 + containerSize / 2 - rectangleSize / 2 - 10,
 				100 + containerSize / 2);
 		ed.drag(100 + containerSize / 2, 100 + containerSize / 2, 100 + containerSize / 2, 300 + containerSize / 2);
 		Thread.sleep(DELAY);
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testCreateConnection() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 				IFeatureProvider fp = dtp.getFeatureProvider();
 				CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 				ICreateFeature[] createFeatures = fp.getCreateFeatures();
 				for (ICreateFeature createFeature : createFeatures) {
 					if ("Rectangle".equals(createFeature.getName())) {
 						Rectangle rectangle = new Rectangle(x, y, 100, 100);
 						ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 						rectangle = new Rectangle(x + 200, y, 100, 100);
 						createContext = createCreateContext(dtp.getDiagram(), rectangle);
 						createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 								createContext, rectangle);
 						commandStack.execute(createCommand);
 					}
 				}
 			}
 
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				// activate create-freeform-connection tool
 				ed.getGefEditor().activateTool("free");
 				ed.getGefEditor().drag(150, 150, 350, 150);
 				ed.getGefEditor().click(350, 150);
 			}
 		});
 		Thread.sleep(DELAY);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				// activate selection tool
 				ed.getGefEditor().activateDefaultTool();
 				// move to line-center and select line
 				ed.getGefEditor().click(250, 150); // middle
 			}
 		});
 		Thread.sleep(SHORT_DELAY);
 		ed.drag(250, 150, 250, 100);
 		Thread.sleep(SHORT_DELAY);
 		ed.drag(250, 100, 250, 200);
 		Thread.sleep(SHORT_DELAY);
 		ed.drag(250, 200, 250, 150);
 		Thread.sleep(SHORT_DELAY);
 		ed.drag(250, 150, 250, 195);
 		Thread.sleep(SHORT_DELAY);
 
 		// remove the connection via context menu
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.getGefEditor().click(250, 195);
 				ed.getGefEditor().clickContextMenu("Remove");
 			}
 		});
 		Thread.sleep(DELAY);
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testResizeAndPosition() throws Exception {
 		final int x = 100;
 		final int y = 100;
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 
 		syncExec(new VoidResult() {
 			public void run() {
 				// find diagram
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 				executeInRecordingCommand(diagramEditor, new Runnable() {
 					public void run() {
 						addClassToDiagram(fp, currentDiagram, x, y, SHAPE_NAME);
 					}
 				});
 			}
 		});
 		Thread.sleep(DELAY);
 		// select shape
 		syncExec(new VoidResult() {
 			public void run() {
 				ed.getGefEditor().select(SHAPE_NAME);
 			}
 		});
 		Thread.sleep(DELAY);
 		// resize shape
 		ed.drag(100, 100, 120, 120);
 		Thread.sleep(DELAY);
 		Diagram diagram = diagramEditor.getDiagramTypeProvider().getDiagram();
 		Shape shape = diagram.getChildren().get(0);
 		assertEquals(120, shape.getGraphicsAlgorithm().getX());
 		assertEquals(120, shape.getGraphicsAlgorithm().getX());
 		page.shutdownEditor(diagramEditor);
		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testSketchAll() throws Exception {
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		PaletteFinder paletteFinder = new PaletteFinder(diagramEditor.getEditDomain());
 		List<PaletteEntry> objectCreationTools = paletteFinder.findEntries(new AbstractToolEntryMatcher() {
 
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
 					public void run() {
 						ed.getGefEditor().activateTool(toolEntry.getLabel());
 						int currX = x + (counter[0] * 5);
 						int currY = y - (5 * counter[0]);
 						ed.getGefEditor().drag(currX, currY, currX + 10, currY + 10);
 					}
 				});
 				//				Thread.sleep(300);
 				counter[0]++;
 			}
 		}
 		Thread.sleep(DELAY);
 		assertEquals(objectCreationTools.size(), ed.getGefEditor().mainEditPart().children().size());
 		page.shutdownEditor(diagramEditor);
 	}
 
 	@Test
 	public void testMoveConnectionDecorator() throws InterruptedException {
 		// Test for Bug 355027: Move of connection decorators when zoom level != 100 behaves weird
 		final DiagramEditor diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		final IDiagramTypeProvider dtp = diagramEditor.getDiagramTypeProvider();
 		final IFeatureProvider fp = ((DefaultFeatureProviderWrapper) dtp.getFeatureProvider()).getInnerFeatureProvider();
 		final CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 
 		// Preparation work
 		syncExec(new VoidResult() {
 			public void run() {
 
 				// Create an outer container
 				ICreateFeature createFeature = new SketchCreateGaContainerFeature(fp, "Rounded Rectangle Container",
 						"draw rounded rectangle", RoundedRectangle.class);
 				Rectangle rectangle = new Rectangle(0, 0, 300, 200);
 				ICreateContext createContext = createCreateContext(dtp.getDiagram(), rectangle);
 				Command createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature,
 						createContext, rectangle);
 				commandStack.execute(createCommand);
 				ContainerShape outerShape = (ContainerShape) dtp.getDiagram().getChildren().get(0);
 
 				// Create two children in the container
 				createFeature = new SketchCreateGaShapeFeature(fp, SketchFeatureProvider.CF_RECTANGLE_SINGLE_TEXT,
 						"draw rectangle with a single line text", org.eclipse.graphiti.mm.algorithms.Rectangle.class);
 				rectangle = new Rectangle(50, 50, 51, 51);
 				createContext = createCreateContext(outerShape, rectangle);
 				createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature, createContext,
 						rectangle);
 				commandStack.execute(createCommand);
 				Shape leftShape = outerShape.getChildren().get(0);
 
 				createFeature = new SketchCreateGaShapeFeature(fp, SketchFeatureProvider.CF_RECTANGLE_SINGLE_TEXT,
 						"draw rectangle with a single line text", org.eclipse.graphiti.mm.algorithms.Rectangle.class);
 				rectangle = new Rectangle(150, 50, 51, 51);
 				createContext = createCreateContext(outerShape, rectangle);
 				createCommand = new CreateModelObjectCommand(diagramEditor.getConfigurationProvider(), createFeature, createContext,
 						rectangle);
 				commandStack.execute(createCommand);
 				Shape rightShape = outerShape.getChildren().get(1);
 
 				// Create a connection between the child shapes
 				final ICreateConnectionFeature[] ccfs = new ICreateConnectionFeature[] { new SketchCreateFreeformConnectionFeature(fp,
 						"free", "freeform connection") };
 				Anchor sourceAnchor = getPeService().getChopboxAnchor(leftShape);
 				Anchor targetAnchor = getPeService().getChopboxAnchor(rightShape);
 				final CreateConnectionContext ccc = new CreateConnectionContext();
 				ccc.setSourceAnchor(sourceAnchor);
 				ccc.setTargetAnchor(targetAnchor);
 				executeInRecordingCommand(diagramEditor, new Runnable() {
 					public void run() {
 						for (ICreateConnectionFeature ccf : ccfs) {
 							if (ccf.canCreate(ccc)) {
 								ccf.execute(ccc);
 							}
 						}
 					}
 				});
 				// Show the connection decorators
 				ToggleDecorator feature = new ToggleDecorator(fp);
 				CustomContext context = new CustomContext();
 				context.setPictogramElements(new PictogramElement[] { dtp.getDiagram() });
 				GenericFeatureCommandWithContext gfcwc = new GenericFeatureCommandWithContext(feature, context);
 				CommandContainer commandContainer = new CommandContainer(fp);
 				commandContainer.add(gfcwc);
 				GefCommandWrapper gefCommandWrapper = new GefCommandWrapper(commandContainer, ed.getTransactionalEditingDomain());
 				commandStack.execute(gefCommandWrapper);
 
 			}
 		});
 		Thread.sleep(SHORT_DELAY);
 
 		// Do the move of the connection decorator with standard zoom level (100%)
 		ed.drag(115, 80, 135, 110);
 		ConnectionDecorator connectionDecorator = dtp.getDiagram().getConnections().get(0).getConnectionDecorators().get(0);
 		/*
 		 * Strange numbers below are ok: they are "calculated" using the
 		 * previous offset of the decorator, the new move point and the position
 		 * the drag operation starts
 		 */
 		assertEquals(17, connectionDecorator.getGraphicsAlgorithm().getX());
 		assertEquals(5, connectionDecorator.getGraphicsAlgorithm().getY());
 		Thread.sleep(SHORT_DELAY);
 
 		// Set zoom level to next zoom level (150%)
 		syncExec(new VoidResult() {
 			public void run() {
 				SWTBotMenu viewMenu = new SWTWorkbenchBot().menu("View");
 				viewMenu.click();
 				SWTBotMenu zoomInItem = viewMenu.menu("Zoom In");
 				zoomInItem.click();
 			}
 		}); 
 		Thread.sleep(SHORT_DELAY);
 
 		// Do the move of the connection decorator with zoom level as set before
 		ed.drag(198, 150, 220, 180);
 		/*
 		 * Strange numbers below are ok: they are "calculated" using the
 		 * previous offset of the decorator, the new move point, the position
 		 * the drag operation starts and the zoom factor
 		 */
 		connectionDecorator = dtp.getDiagram().getConnections().get(0).getConnectionDecorators().get(0);
 		assertEquals(33, connectionDecorator.getGraphicsAlgorithm().getX());
 		assertEquals(25, connectionDecorator.getGraphicsAlgorithm().getY());
 		Thread.sleep(SHORT_DELAY);
 		page.shutdownEditor(diagramEditor);
 	}
 }
