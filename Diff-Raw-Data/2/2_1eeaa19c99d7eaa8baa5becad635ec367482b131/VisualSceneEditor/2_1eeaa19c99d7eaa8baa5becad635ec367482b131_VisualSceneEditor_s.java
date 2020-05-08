 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.ui.editors;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 
 import javax.media.opengl.GLContext;
 import javax.media.opengl.GLDrawableFactory;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.opengl.GLCanvas;
 import org.eclipse.swt.opengl.GLData;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPersistableEditor;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 import com.se.simplicity.editor.internal.EditingMode;
 import com.se.simplicity.editor.internal.SceneManager;
 import com.se.simplicity.editor.internal.ScenePickListener;
 import com.se.simplicity.editor.internal.SelectionMode;
 import com.se.simplicity.editor.internal.WidgetManager;
 import com.se.simplicity.editor.internal.WidgetPickListener;
 import com.se.simplicity.editor.internal.engine.DisplayAsyncJOGLCompositeEngine;
 import com.se.simplicity.editor.internal.selection.PickSelection;
 import com.se.simplicity.editor.internal.selection.PickSelectionSource;
 import com.se.simplicity.editor.internal.selection.SceneSelection;
 import com.se.simplicity.editor.ui.editors.outline.SceneOutlinePage;
 import com.se.simplicity.jogl.JOGLComponent;
 import com.se.simplicity.jogl.rendering.SimpleJOGLCamera;
 import com.se.simplicity.jogl.rendering.engine.SimpleJOGLRenderingEngine;
 import com.se.simplicity.model.Model;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.DrawingMode;
 import com.se.simplicity.rendering.Light;
 import com.se.simplicity.rendering.ProjectionMode;
 import com.se.simplicity.rendering.engine.RenderingEngine;
 import com.se.simplicity.scene.Scene;
 import com.se.simplicity.scenegraph.Node;
 import com.se.simplicity.scenegraph.SimpleNode;
 import com.se.simplicity.scenegraph.SimpleTraversal;
 import com.se.simplicity.util.metadata.MetaData;
 import com.se.simplicity.util.metadata.scene.MetaDataScene;
 import com.se.simplicity.vector.SimpleTranslationVectorf4;
 
 /**
  * <p>
  * An eclipse editor that displays a {@link com.se.simplicity.scene.Scene Scene} visually on a 3D canvas using the JOGL rendering environment.
  * </p>
  * 
  * @author Gary Buyn
  */
 public abstract class VisualSceneEditor extends EditorPart implements SceneEditor, ISelectionListener, IPersistableEditor
 {
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.Camera Camera} the {@link com.se.simplicity.scene.Scene Scene} will be viewed through.
      * </p>
      */
     private Camera fCamera;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.DrawingMode DrawingMode} used to render the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      */
     private DrawingMode fDrawingMode;
 
     /**
      * <p>
      * The {@link com.se.simplicity.editor.internal.EditingMode EditingMode} used to manipulate the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      */
     private EditingMode fEditingMode;
 
     /**
      * <p>
      * Determines if this <code>VisualSceneEditor</code> is initialised.
      * </p>
      */
     private boolean fIsInitialised;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.ProjectionMode ProjectionMode} the {@link com.se.simplicity.scene.Scene Scene} is displayed with.
      * </p>
      */
     private ProjectionMode fProjectionMode;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.engine.RenderingEngine RenderingEngine} that will render the {@link com.se.simplicity.rendering.Camera
      * Camera} and {@link com.se.simplicity.editor.internal.Widget Widget}s to the 3D canvas.
      * </p>
      */
     private RenderingEngine fRenderingEngine;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scene.Scene Scene} displayed by this <code>VisualSceneEditor</code>.
      * </p>
      */
     private Scene fScene;
 
     /**
      * <p>
      * The manager for the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      */
     private SceneManager fSceneManager;
 
     /**
      * <p>
      * The selected scene component and primitive.
      * </p>
      */
     private SceneSelection fSelection;
 
     /**
      * <p>
      * Listeners to a change in selection.
      * </p>
      */
     private ArrayList<ISelectionChangedListener> fSelectionChangedListeners;
 
     /**
      * <p>
      * The {@link com.se.simplicity.editor.internal.SelectionMode SelectionMode} to select scene components / primitives with.
      * </p>
      */
     private SelectionMode fSelectionMode;
 
     /**
      * <p>
      * Determines whether the aspect ratio of the {@link com.se.simplicity.rendering.Camera Camera} is synchronised with the aspect ratio of the
      * viewport.
      * </p>
      */
     private boolean fSyncCameraAspectRatio;
 
     /**
      * <p>
      * The manager for the {@link com.se.simplicity.editor.internal.Widget Widget}s used to manipulate the {@link com.se.simplicity.scene.Scene Scene}
      * .
      * </p>
      */
     private WidgetManager fWidgetManager;
 
     /**
      * <p>
      * Determines whether the previous selection originating from a 'WIDGET' pick was successful.
      * </p>
      */
     private boolean fWidgetPickSuccessful;
 
     /**
      * <p>
      * Creates an instance of <code>VisualSceneEditor</code>.
      * </p>
      */
     public VisualSceneEditor()
     {
         super();
 
         fCamera = null;
         fDrawingMode = DrawingMode.FACES;
         fEditingMode = EditingMode.SELECTION;
         fIsInitialised = false;
         fProjectionMode = ProjectionMode.PERSPECTIVE;
         fRenderingEngine = null;
         fSelection = new SceneSelection(null, null);
         fSelectionChangedListeners = new ArrayList<ISelectionChangedListener>();
         fSelectionMode = SelectionMode.MODEL;
         fScene = null;
         fSceneManager = null;
         fSyncCameraAspectRatio = true;
         fWidgetManager = null;
     }
 
     @Override
     public void addSelectionChangedListener(final ISelectionChangedListener listener)
     {
         fSelectionChangedListeners.add(listener);
     }
 
     @Override
     public void createPartControl(final Composite parent)
     {
         Canvas canvas = null;
         GLContext glContext = null;
         if (fRenderingEngine instanceof JOGLComponent)
         {
             // Setup 3D canvas.
             GLData data = new GLData();
             data.doubleBuffer = true;
             data.stencilSize = 1;
             canvas = new GLCanvas(parent, SWT.NONE, data);
 
             // Setup JOGL rendering environment.
             ((GLCanvas) canvas).setCurrent();
             glContext = GLDrawableFactory.getFactory().createExternalGLContext();
             ((JOGLComponent) fCamera).setGL(glContext.getGL());
             ((JOGLComponent) fRenderingEngine).setGL(glContext.getGL());
             fSceneManager.setGL(glContext.getGL());
             fWidgetManager.setGL(glContext.getGL());
         }
 
         // Setup viewport size and Camera synchronisation with 3D canvas size.
         canvas.addControlListener(new ResizeControlListener(this));
 
         // Setup mouse interaction.
         NavigationMouseListener navigationMouseListener = new NavigationMouseListener(fCamera);
         canvas.addMouseListener(navigationMouseListener);
         canvas.addMouseMoveListener(navigationMouseListener);
         canvas.addMouseWheelListener(navigationMouseListener);
 
         canvas.addMouseListener(new SelectionMouseListener(this));
 
         WidgetMouseListener widgetMouseListener = new WidgetMouseListener(fWidgetManager);
         canvas.addMouseListener(widgetMouseListener);
         canvas.addMouseMoveListener(widgetMouseListener);
 
         getSite().setSelectionProvider(this);
         getSite().getPage().addSelectionListener(this);
 
         if (fRenderingEngine instanceof JOGLComponent)
         {
             display((GLCanvas) canvas, glContext);
         }
     }
 
     /**
      * <p>
      * Displays the content for the lifetime of the given <code>GLCanvas</code>.
      * </p>
      * 
      * @param canvas The <code>GLCanvas</code> to display the contents on.
      * @param glContext The <code>GLContext</code> to use when displaying.
      */
     public void display(final GLCanvas canvas, final GLContext glContext)
     {
         DisplayAsyncJOGLCompositeEngine compositeEngine = new DisplayAsyncJOGLCompositeEngine(canvas, glContext);
 
         // Widget picking should occur first because it takes precedence.
         compositeEngine.addEngine(fWidgetManager.getPickingEngine());
 
         compositeEngine.addEngine(fSceneManager.getPickingEngine());
         compositeEngine.addEngine(fRenderingEngine);
 
         compositeEngine.run();
     }
 
     @Override
     public void doSave(final IProgressMonitor monitor)
     {}
 
     @Override
     public void doSaveAs()
     {}
 
     @SuppressWarnings("unchecked")
     @Override
     public Object getAdapter(final Class adapter)
     {
         Object adapterInstance = null;
 
         if (adapter == IContentOutlinePage.class)
         {
             adapterInstance = new SceneOutlinePage(fScene);
         }
         else
         {
             adapterInstance = super.getAdapter(adapter);
         }
 
         return (adapterInstance);
     }
 
     @Override
     public DrawingMode getDrawingMode()
     {
         return (fDrawingMode);
     }
 
     @Override
     public EditingMode getEditingMode()
     {
         return (fEditingMode);
     }
 
     @Override
     public ProjectionMode getProjectionMode()
     {
         return (fProjectionMode);
     }
 
     /**
      * <p>
      * Retrieves the {@link com.se.simplicity.scene.Scene Scene} displayed by this <code>VisualSceneEditor</code>.
      * </p>
      * 
      * @return The {@link com.se.simplicity.scene.Scene Scene} displayed by this <code>VisualSceneEditor</code>.
      */
     public Scene getScene()
     {
         return (fScene);
     }
 
     @Override
     public SceneManager getSceneManager()
     {
         return (fSceneManager);
     }
 
     @Override
     public ISelection getSelection()
     {
         return (fSelection);
     }
 
     /**
      * <p>
      * Retrieves a {@link com.se.simplicity.editor.internal.selection.SceneSelection SceneSelection} based on the given
      * {@link com.se.simplicity.editor.internal.selection.PickSelection PickSelection}.
      * </p>
      * 
      * <p>
      * NOTE: This method assumes that <code>PickSelection</code>s occur in pairs, the first originating from a 'WIDGET' pick and the second
      * originating from a 'SCENE' pick. This allows it to give priority to the selection originating from a 'WIDGET' pick.
      * </p>
      * 
      * @param selection The <code>PickSelection</code> to retrieve the <code>SceneSelection</code> for.
      * 
      * @return A <code>SceneSelection</code> based on the given <code>PickSelection</code>, or null if the selection has not been accepted due to the
      * priority of the 'WIDGET' pick over the 'SCENE' pick.
      */
     protected SceneSelection getSelection(final PickSelection selection)
     {
         SceneSelection sceneSelection = null;
 
         // If the selection originated from a Widget pick.
         if (selection.getSource() == PickSelectionSource.WIDGET_PICK)
         {
             // If the selection is empty, accept the following selection originating from a Scene pick.
             if (selection.isEmpty())
             {
                 fWidgetPickSuccessful = false;
             }
 
             // Otherwise, accept the pick and ignore the following selection originating from a Scene pick.
             else
             {
                 fWidgetPickSuccessful = true;
                 sceneSelection = new SceneSelection(selection.getSceneComponent(), selection.getPrimitive());
             }
         }
 
         // If the selection originated from a Scene pick.
         if (selection.getSource() == PickSelectionSource.SCENE_PICK)
         {
             // If the previous Widget pick was not accepted, accept the pick.
             if (!fWidgetPickSuccessful)
             {
                sceneSelection = new SceneSelection(selection.getSceneComponent(), null);
             }
         }
 
         return (sceneSelection);
     }
 
     @Override
     public SelectionMode getSelectionMode()
     {
         return (fSelectionMode);
     }
 
     @Override
     public WidgetManager getWidgetManager()
     {
         return (fWidgetManager);
     }
 
     @Override
     public void init(final IEditorSite site, final IEditorInput input) throws PartInitException
     {
         setSite(site);
         setInput(input);
         setPartName(input.getName());
 
         fScene = loadScene(input);
 
         initCamera();
         initRenderingEngine();
 
         // Set the Camera as the viewpoint for picking and rendering but do NOT add to the Scene. This stops the Camera from appearing in the
         // various views displaying an analysis of the Scene or being synchronised into the source file.
         fRenderingEngine.setCamera(fCamera);
 
         fSceneManager = new SceneManager();
         fSceneManager.setScene(fScene);
         fSceneManager.setRenderingEngine(fRenderingEngine);
         fSceneManager.init();
         fSceneManager.setCamera(fCamera);
 
         fWidgetManager = new WidgetManager();
         fWidgetManager.setScene(fScene);
         fWidgetManager.setRenderingEngine(fRenderingEngine);
         fWidgetManager.init();
         fWidgetManager.setCamera(fCamera);
 
         fSceneManager.getPickingEngine().addPickListener(new ScenePickListener(this));
         fWidgetManager.getPickingEngine().addPickListener(new WidgetPickListener(this));
 
         fIsInitialised = true;
     }
 
     /**
      * <p>
      * Initialises the {@link com.se.simplicity.rendering.Camera Camera} used to view the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      */
     protected void initCamera()
     {
         fCamera = new SimpleJOGLCamera();
         SimpleNode subjectNode = new SimpleNode();
         SimpleNode cameraNode = new SimpleNode();
         subjectNode.addChild(cameraNode);
         cameraNode.getTransformation().translate(new SimpleTranslationVectorf4(0.0f, 0.0f, 5.0f, 1.0f));
         fCamera.setNode(cameraNode);
     }
 
     /**
      * <p>
      * Initialises the {@link com.se.simplicity.rendering.engine.RenderingEngine RenderingEngine} that will render the
      * {@link com.se.simplicity.rendering.Camera Camera} and {@link com.se.simplicity.editor.internal.Widget Widget}s to the 3D canvas.
      * </p>
      */
     protected void initRenderingEngine()
     {
         // Retrieve preferred rendering environment if one is available.
         String preferredRenderingEngine = null;
         if (fScene instanceof MetaDataScene)
         {
             MetaDataScene metaDataScene = (MetaDataScene) fScene;
             preferredRenderingEngine = (String) metaDataScene.getAttribute("preferredRenderingEngine");
         }
 
         // Initialise Rendering Engine.
         if (preferredRenderingEngine == null)
         {
             fRenderingEngine = new SimpleJOGLRenderingEngine();
         }
         else
         {
             try
             {
                 fRenderingEngine = (RenderingEngine) Class.forName(preferredRenderingEngine).newInstance();
             }
             catch (Exception e)
             {
                 Logger.getLogger(getClass()).warn("Failed to instantiate preferred Rendering Engine, instantiating default.", e);
                 fRenderingEngine = new SimpleJOGLRenderingEngine();
             }
         }
         fRenderingEngine.setScene(fScene);
     }
 
     @Override
     public boolean isDirty()
     {
         return (false);
     }
 
     @Override
     public boolean isSaveAsAllowed()
     {
         return (false);
     }
 
     /**
      * <p>
      * Loads the {@link com.se.simplicity.scene.Scene Scene} to be displayed by this <code>VisualSceneEditor</code>.
      * </p>
      * 
      * @param input The input to load the <code>Scene</code> from.
      * 
      * @throws PartInitException Thrown if the <code>Scene</code> to be loaded.
      * 
      * @return The <code>Scene</code> to be displayed by this <code>VisualSceneEditor</code>.
      */
     protected abstract Scene loadScene(IEditorInput input) throws PartInitException;
 
     @Override
     public void pickForSelection(final Dimension viewportSize, final int x, final int y, final int width, final int height)
     {
         // Widget picking should occur first because it takes precedence.
         fWidgetManager.getPickingEngine().pickViewport(viewportSize, x, y, width, height);
 
         fSceneManager.getPickingEngine().pickViewport(viewportSize, x, y, width, height);
     }
 
     @Override
     public void removeSelectionChangedListener(final ISelectionChangedListener listener)
     {
         fSelectionChangedListeners.remove(listener);
     }
 
     /**
      * <p>
      * Restores the stateful eclipse commands associated with this editor.
      * </p>
      */
     protected void restoreCommands()
     {
         // Restore Commands.
         ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
 
         Command drawingMode = commandService.getCommand("com.se.simplicity.editor.commands.draw");
         drawingMode.getState("org.eclipse.ui.commands.radioState").setValue(fDrawingMode.toString());
 
         Command editingMode = commandService.getCommand("com.se.simplicity.editor.commands.edit");
         editingMode.getState("org.eclipse.ui.commands.radioState").setValue(fEditingMode.toString());
 
         Command projectionMode = commandService.getCommand("com.se.simplicity.editor.commands.projection");
         projectionMode.getState("org.eclipse.ui.commands.radioState").setValue(fProjectionMode.toString());
 
         Command selectionMode = commandService.getCommand("com.se.simplicity.editor.commands.select");
         selectionMode.getState("org.eclipse.ui.commands.radioState").setValue(fSelectionMode.toString());
     }
 
     @Override
     public void restoreState(final IMemento memento)
     {
         if (!fIsInitialised)
         {
             return;
         }
 
         if (memento != null)
         {
             // Restore Drawing Mode.
             fDrawingMode = DrawingMode.valueOf(memento.getString("drawingMode"));
 
             // Restore Editing Mode.
             fEditingMode = EditingMode.valueOf(memento.getString("editingMode"));
 
             // Restore Projection Mode.
             fProjectionMode = ProjectionMode.valueOf(memento.getString("projectionMode"));
 
             // Restore Selection Mode.
             fSelectionMode = SelectionMode.valueOf(memento.getString("selectionMode"));
 
             // Restore selection.
             Object sceneComponent = null;
             Model primitive = null;
 
             String sceneComponentType = memento.getString("sceneComponentType");
             if (sceneComponentType.equals("camera"))
             {
                 for (Camera camera : fScene.getCameras())
                 {
                     sceneComponent = camera;
                     break;
                 }
             }
             else if (sceneComponentType.equals("light"))
             {
                 for (Light light : fScene.getLights())
                 {
                     sceneComponent = light;
                     break;
                 }
             }
             else if (sceneComponentType.equals("node"))
             {
                 SimpleTraversal traversal = new SimpleTraversal(fScene.getSceneGraph().getRoot());
                 while (traversal.hasMoreNodes())
                 {
                     Node node = traversal.getNextNode();
                     if (node instanceof MetaData && ((MetaData) node).getAttribute("name").equals(memento.getString("sceneComponentName")))
                     {
                         sceneComponent = node;
                         break;
                     }
                 }
             }
 
             fSelection = new SceneSelection(sceneComponent, primitive);
             setSelection(fSelection);
         }
 
         // Restore modes.
         setDrawingMode(fDrawingMode);
         setEditingMode(fEditingMode);
         setProjectionMode(fProjectionMode);
         setSelectionMode(fSelectionMode);
 
         // This check is required for unit testing... unfortunately...
         if (PlatformUI.isWorkbenchRunning())
         {
             restoreCommands();
         }
     }
 
     @Override
     public void saveState(final IMemento memento)
     {
         // Save Drawing Mode.
         memento.putString("drawingMode", fDrawingMode.toString());
 
         // Save Editing Mode.
         memento.putString("editingMode", fEditingMode.toString());
 
         // Save Projection Mode.
         memento.putString("projectionMode", fProjectionMode.toString());
 
         // Save Selection Mode.
         memento.putString("selectionMode", fSelectionMode.toString());
 
         // Save selection.
         if (fSelection.getSceneComponent() != null && fSelection.getSceneComponent() instanceof MetaData)
         {
             MetaData metaData = (MetaData) fSelection.getSceneComponent();
 
             memento.putString("sceneComponentName", (String) metaData.getAttribute("name"));
 
             if (fSelection.getSceneComponent() instanceof Camera)
             {
                 memento.putString("sceneComponentType", "camera");
             }
             else if (fSelection.getSceneComponent() instanceof Light)
             {
                 memento.putString("sceneComponentType", "light");
             }
             else if (fSelection.getSceneComponent() instanceof Node)
             {
                 memento.putString("sceneComponentType", "node");
             }
         }
         else
         {
             memento.putString("sceneComponentName", "");
             memento.putString("sceneComponentType", "");
         }
     }
 
     @Override
     public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
     {
         if (!(part instanceof SceneEditor) && selection instanceof SceneSelection)
         {
             setSelectionInternal((SceneSelection) selection);
         }
     }
 
     @Override
     public void setCanvasSize(final Rectangle canvasSize)
     {
         Dimension viewportSize = new Dimension();
         viewportSize.setSize(canvasSize.width, canvasSize.height);
 
         fRenderingEngine.setViewportSize(viewportSize);
         fSceneManager.setViewportSize(viewportSize);
         fWidgetManager.setViewportSize(viewportSize);
 
         if (fSyncCameraAspectRatio)
         {
             fCamera.setFrameAspectRatio((float) canvasSize.height / (float) canvasSize.width);
         }
     }
 
     @Override
     public void setDrawingMode(final DrawingMode drawingMode)
     {
         fDrawingMode = drawingMode;
 
         fSceneManager.setDrawingMode(fDrawingMode);
     }
 
     @Override
     public void setEditingMode(final EditingMode editingMode)
     {
         fEditingMode = editingMode;
 
         fWidgetManager.setEditingMode(fEditingMode);
     }
 
     @Override
     public void setFocus()
     {
         restoreState(null);
     }
 
     @Override
     public void setProjectionMode(final ProjectionMode projectionMode)
     {
         fProjectionMode = projectionMode;
 
         fCamera.setProjectionMode(fProjectionMode);
     }
 
     @Override
     public void setSelection(final ISelection selection)
     {
         ISelection tempSelection = selection;
 
         // If the selection originated from a pick.
         if (tempSelection instanceof PickSelection)
         {
             tempSelection = getSelection((PickSelection) tempSelection);
         }
 
         if (tempSelection instanceof SceneSelection)
         {
             // Set the selection internally.
             setSelectionInternal((SceneSelection) tempSelection);
 
             // Notify listeners.
             SelectionChangedEvent event = new SelectionChangedEvent(this, fSelection);
             for (Object listener : fSelectionChangedListeners)
             {
                 ((ISelectionChangedListener) listener).selectionChanged(event);
             }
         }
     }
 
     /**
      * <p>
      * Sets the selected scene component and primitive.
      * </p>
      * 
      * @param selection The selected scene component and primitive.
      */
     protected void setSelectionInternal(final SceneSelection selection)
     {
         fSelection = selection;
 
         // Update managers.
         fSceneManager.setSelection(fSelection);
         fWidgetManager.setSelection(fSelection);
     }
 
     @Override
     public void setSelectionMode(final SelectionMode selectionMode)
     {
         fSelectionMode = selectionMode;
 
         fSceneManager.setSelectionMode(fSelectionMode);
     }
 
     /**
      * <p>
      * Sets whether the aspect ratio of the {@link com.se.simplicity.rendering.Camera Camera} is synchronised with the aspect ratio of the viewport.
      * </p>
      * 
      * @param syncCameraAspectRatio Determines whether the aspect ratio of the <code>Camera</code> is synchronised with the aspect ratio of the
      * viewport.
      */
     public void setSyncCameraAspectRatio(final boolean syncCameraAspectRatio)
     {
         fSyncCameraAspectRatio = syncCameraAspectRatio;
     }
 
     /**
      * <p>
      * Determines whether the aspect ratio of the {@link com.se.simplicity.rendering.Camera Camera} is synchronised with the aspect ratio of the
      * viewport.
      * </p>
      * 
      * @return True if the aspect ratio of the <code>Camera</code> is synchronised with the aspect ratio of the viewport, false otherwise.
      */
     public boolean syncCameraAspectRatio()
     {
         return (fSyncCameraAspectRatio);
     }
 }
