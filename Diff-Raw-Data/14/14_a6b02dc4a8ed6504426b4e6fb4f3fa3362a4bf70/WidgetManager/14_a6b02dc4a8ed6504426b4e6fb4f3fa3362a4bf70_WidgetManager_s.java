 package com.se.simplicity.editor.internal;
 
 import java.awt.Dimension;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.media.opengl.GL;
 
 import com.se.simplicity.editor.internal.picking.WidgetJOGLPicker;
 import com.se.simplicity.editor.internal.rendering.WidgetJOGLRenderer;
 import com.se.simplicity.jogl.JOGLComponent;
 import com.se.simplicity.jogl.picking.SimpleJOGLPicker;
 import com.se.simplicity.jogl.picking.engine.SimpleJOGLPickingEngine;
 import com.se.simplicity.jogl.rendering.NamePassingJOGLRenderer;
 import com.se.simplicity.jogl.rendering.NamedJOGLRenderer;
 import com.se.simplicity.jogl.rendering.SimpleJOGLRenderer;
 import com.se.simplicity.jogl.rendering.engine.SimpleJOGLRenderingEngine;
 import com.se.simplicity.jogl.scene.SimpleJOGLScene;
 import com.se.simplicity.picking.engine.PickingEngine;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.engine.RenderingEngine;
 import com.se.simplicity.scene.Scene;
 import com.se.simplicity.scenegraph.SceneGraph;
 import com.se.simplicity.scenegraph.SimpleSceneGraph;
 
 /**
  * <p>
  * Provides the content that will be displayed by a {@link com.se.simplicity.editor.ui.editors.SceneEditor SceneEditor}.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class WidgetManager
 {
     /**
      * <p>
      * The {@link com.se.simplicity.editor.internal.EditingMode EditingMode} used to manipulate the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      */
     private EditingMode fEditingMode;
 
     /**
      * <p>
      * The {@link com.se.simplicity.picking.engine.PickingEngine PickingEngine} used to select the {@link com.se.simplicity.editor.internal.Widget
      * Widget}s.
      * </p>
      */
     private PickingEngine fPickingEngine;
 
     /**
      * <p>
      * Fills the select buffer with {@link com.se.simplicity.editor.internal.Widget Widget} selection data.
      * </p>
      */
    private WidgetJOGLRenderer fPickingRenderer;
 
     /**
      * <p>
      * Renders the {@link com.se.simplicity.editor.internal.Widget Widget}s.
      * </p>
      */
     private WidgetJOGLRenderer fRenderer;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.engine.RenderingEngine RenderingEngine} that will render the
      * {@link com.se.simplicity.editor.internal.Widget Widget}s to the 3D canvas.
      * </p>
      */
     private RenderingEngine fRenderingEngine;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scene.Scene Scene} displayed by the {@link com.se.simplicity.editor.ui.editors.SceneEditor SceneEditor}.
      * </p>
      */
     private Scene fScene;
 
     /**
      * <p>
      * The {@link com.se.simplicity.editor.internal.Widget Widget}s used to manipulate {@link com.se.simplicity.model.Model Model}s.
      * </p>
      */
     private Map<EditingMode, Widget> fWidgets;
 
     /**
      * The {@link com.se.simplicity.scene.Scene Scene} that contains the current {@link com.se.simplicity.editor.internal.Widget Widget}.
      */
     private Scene fWidgetScene;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.engine.RenderingEngine RenderingEngine} used to select the
      * {@link com.se.simplicity.editor.internal.Widget Widget}s.
      * </p>
      */
     private SimpleJOGLRenderingEngine fPickingRenderingEngine;
 
     /**
      * <p>
      * Creates an instance of <code>WidgetManager</code>.
      * </p>
      */
     public WidgetManager()
     {
         fEditingMode = null;
         fPickingEngine = null;
         fPickingRenderer = null;
         fPickingRenderingEngine = null;
         fRenderer = new WidgetJOGLRenderer(new SimpleJOGLRenderer());
         fRenderingEngine = null;
         fScene = null;
         fWidgets = new HashMap<EditingMode, Widget>();
         fWidgetScene = null;
     }
 
     /**
      * <p>
      * Creates an instance of <code>WidgetManager</code>.
      * </p>
      * 
      * @param scene The {@link com.se.simplicity.scene.Scene Scene} displayed by the {@link com.se.simplicity.editor.ui.editors.SceneEditor
      * SceneEditor}.
      */
     public WidgetManager(final Scene scene)
     {
         fScene = scene;
 
         fEditingMode = null;
         fPickingEngine = null;
         fPickingRenderer = null;
         fPickingRenderingEngine = null;
         fRenderer = new WidgetJOGLRenderer(new SimpleJOGLRenderer());
         fRenderingEngine = null;
         fWidgets = new HashMap<EditingMode, Widget>();
         fWidgetScene = null;
     }
 
     /**
      * <p>
      * Retrieves the {@link com.se.simplicity.editor.internal.EditingMode EditingMode} used to manipulate the {@link com.se.simplicity.scene.Scene
      * Scene}.
      * </p>
      * 
      * @return The <code>EditingMode</code> used to manipulate the <code>Scene</code>.
      */
     public EditingMode getEditingMode()
     {
         return (fEditingMode);
     }
 
     /**
      * <p>
      * Retrieves the {@link com.se.simplicity.picking.engine.PickingEngine PickingEngine} used to select the
      * {@link com.se.simplicity.editor.internal.Widget Widget}s.
      * </p>
      * 
      * @return The <code>PickingEngine</code> used to select the <code>Widget</code>s.
      */
     public PickingEngine getPickingEngine()
     {
         return (fPickingEngine);
     }
 
     /**
      * <p>
      * Retrieves the {@link com.se.simplicity.rendering.engine.RenderingEngine RenderingEngine} that will render the
      * {@link com.se.simplicity.editor.internal.Widget Widget}s to the 3D canvas.
      * </p>
      * 
      * @return The <code>RenderingEngine</code> that will render the <code>Widget</code>s to the 3D canvas.
      */
     public RenderingEngine getRenderingEngine()
     {
         return (fRenderingEngine);
     }
 
     /**
      * <p>
      * Retrieves the {@link com.se.simplicity.scene.Scene Scene} displayed by the {@link com.se.simplicity.editor.ui.editors.SceneEditor SceneEditor}.
      * </p>
      * 
      * @return The <code>Scene</code> displayed by the <code>SceneEditor</code>.
      */
     public Scene getScene()
     {
         return (fScene);
     }
 
     /**
      * <p>
      * Retrieves the {@link com.se.simplicity.editor.internal.Widget Widget} currently being used to manipulate {@link com.se.simplicity.model.Model
      * Model}s.
      * </p>
      * 
      * @return The <code>Widget</code> currently being used to manipulate <code>Model</code>s.
      */
     public Widget getWidget()
     {
         return (fWidgets.get(fEditingMode));
     }
 
     /**
      * <p>
      * Initialises this <code>WidgetManager</code>.
      * </p>
      */
     public void init()
     {
         initPickingEngine();
         initWidgets();
     }
 
     /**
      * <p>
      * Initialises the {@link com.se.simplicity.picking.engine.PickingEngine PickingEngine} used to select widget components.
      * </p>
      */
     protected void initPickingEngine()
     {
         fPickingEngine = new SimpleJOGLPickingEngine();
         SimpleJOGLPicker picker = new WidgetJOGLPicker();
         fPickingRenderingEngine = new SimpleJOGLRenderingEngine();
        fPickingRenderer = new WidgetJOGLRenderer(new NamedJOGLRenderer());
 
         fPickingEngine.setPicker(picker);
         picker.setRenderingEngine(fPickingRenderingEngine);
        fPickingRenderingEngine.addRenderer(new NamePassingJOGLRenderer(fPickingRenderer));
 
         fWidgetScene = new SimpleJOGLScene();
         fWidgetScene.setSceneGraph(new SimpleSceneGraph());
     }
 
     /**
      * <p>
      * Initialises the widgets used to manipulate {@link com.se.simplicity.model.Model Model}s.
      * </p>
      */
     protected void initWidgets()
     {
         fWidgets.put(EditingMode.ROTATION, new RotationWidget());
         fWidgets.put(EditingMode.SELECTION, new SelectionWidget());
         fWidgets.put(EditingMode.TRANSLATION, new TranslationWidget());
     }
 
     /**
      * <p>
      * Sets the {@link com.se.simplicity.rendering.Camera Camera} the {@link com.se.simplicity.editor.internal.Widget Widget}s will be viewed through.
      * </p>
      * 
      * @param camera The <code>Camera</code> the <code>Widget</code>s will be viewed through.
      */
     public void setCamera(final Camera camera)
     {
         if (fPickingEngine == null)
         {
             throw new IllegalStateException("This Widget Manager must be initialised before the Camera can be set.");
         }
 
         fRenderer.setCamera(camera);
        fPickingRenderer.setCamera(camera);
 
         fPickingEngine.setCamera(camera);
     }
 
     /**
      * <p>
      * Sets the {@link com.se.simplicity.editor.internal.EditingMode EditingMode} used to manipulate the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      * 
      * @param editingMode The <code>EditingMode</code> used to manipulate the <code>Scene</code>.
      */
     public void setEditingMode(final EditingMode editingMode)
     {
         if (fPickingEngine == null)
         {
             throw new IllegalStateException("This Widget Manager must be initialised before the Editing Mode can be set.");
         }
 
         fEditingMode = editingMode;
 
         fRenderer.setWidget(fWidgets.get(fEditingMode));
        fPickingRenderer.setWidget(fWidgets.get(fEditingMode));
 
         // Include the full Scene in the Widget Renderer and the Widget PickingEngine.
         if (fEditingMode == EditingMode.SELECTION)
         {
             fRenderingEngine.setRendererRoot(fRenderer, fScene.getSceneGraph().getRoot());
 
             fPickingEngine.setScene(fScene);
             fPickingRenderingEngine.setRendererRoot(fPickingRenderer, fScene.getSceneGraph().getRoot());
         }
         else
         {
             Widget widget = fWidgets.get(fEditingMode);
             SceneGraph widgetSceneGraph = fWidgetScene.getSceneGraph();
 
             if (!widgetSceneGraph.getSubgraphRoots().isEmpty())
             {
                 widgetSceneGraph.removeSubgraph(widgetSceneGraph.getSubgraphRoots().get(0));
             }
             widgetSceneGraph.addSubgraph(widget.getRootNode());
 
             // Set the root of the widget to be the root for the Widget Renderer and include it in the Widget PickingEngine's Scene but do NOT add it
             // to the main Scene. This stops the Widget from appearing in the various views displaying an analysis of the Scene or being synchronised
             // into the source file.
             fRenderingEngine.setRendererRoot(fRenderer, widgetSceneGraph.getRoot());
 
             fPickingEngine.setScene(fWidgetScene);
             fPickingRenderingEngine.setRendererRoot(fPickingRenderer, widgetSceneGraph.getRoot());
 
             widget.setSelectedWidgetNode(null);
         }
     }
 
     /**
      * <p>
      * Sets the JOGL rendering environment used to display the {@link com.se.simplicity.editor.internal.Widget Widget}s.
      * </p>
      * 
      * @param gl The JOGL rendering environment used to display the <code>Widget</code>s.
      */
     public void setGL(final GL gl)
     {
         if (fPickingEngine == null)
         {
             throw new IllegalStateException("This Widget Manager must be initialised before the JOGL rendering environment can be set.");
         }
 
         ((JOGLComponent) fPickingEngine).setGL(gl);
         ((JOGLComponent) ((SimpleJOGLPicker) fPickingEngine.getPicker()).getRenderingEngine()).setGL(gl);
     }
 
     /**
      * <p>
      * Sets the {@link com.se.simplicity.rendering.engine.RenderingEngine RenderingEngine} that will render the
      * {@link com.se.simplicity.editor.internal.Widget Widget}s to the 3D canvas.
      * </p>
      * 
      * @param renderingEngine The <code>RenderingEngine</code> that will render the <code>Widget</code>s to the 3D canvas.
      */
     public void setRenderingEngine(final RenderingEngine renderingEngine)
     {
         fRenderingEngine = renderingEngine;
 
         renderingEngine.addRenderer(fRenderer);
         renderingEngine.setRendererRoot(fRenderer, null);
     }
 
     /**
      * <p>
      * Sets the {@link com.se.simplicity.scene.Scene Scene} displayed by this <code>ContentProvider</code>.
      * </p>
      * 
      * @param scene The <code>Scene</code> displayed by this <code>ContentProvider</code>.
      */
     public void setScene(final Scene scene)
     {
         fScene = scene;
     }
 
     /**
      * <p>
      * Sets the currently selected scene component.
      * </p>
      * 
      * @param selectedSceneComponent The currently selected scene component.
      */
     public void setSelectedSceneComponent(final Object selectedSceneComponent)
     {
         for (Entry<EditingMode, Widget> widgetEntry : fWidgets.entrySet())
         {
             widgetEntry.getValue().setSelectedSceneComponent(selectedSceneComponent);
         }
     }
 
     /**
      * <p>
      * Sets the size of the viewport the {@link com.se.simplicity.editor.internal.Widget Widget}s are being viewed through (the size of the 3D
      * canvas).
      * </p>
      * 
      * @param viewportSize The size of the viewport the <code>Widget</code>s are being viewed through (the size of the 3D canvas).
      */
     public void setViewportSize(final Dimension viewportSize)
     {
         if (fPickingEngine == null)
         {
             throw new IllegalStateException("This Widget Manager must be initialised before the viewport size can be set.");
         }
 
         ((SimpleJOGLPicker) fPickingEngine.getPicker()).getRenderingEngine().setViewportSize(viewportSize);
     }
 }
