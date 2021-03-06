 //
 // $Id$
 
 package com.threerings.opengl.compositor;
 
 import java.lang.Comparable;
 
 import com.threerings.config.ConfigEvent;
 import com.threerings.config.ConfigUpdateListener;
 import com.threerings.expr.Executor;
 import com.threerings.expr.Scope;
 import com.threerings.expr.Scoped;
 import com.threerings.expr.DynamicScope;
 import com.threerings.expr.SimpleScope;
 import com.threerings.expr.util.ScopeUtil;
 
 import com.threerings.opengl.compositor.config.RenderEffectConfig;
 import com.threerings.opengl.compositor.config.RenderEffectConfig.Technique;
 import com.threerings.opengl.compositor.config.TargetConfig;
 import com.threerings.opengl.gui.util.Rectangle;
 import com.threerings.opengl.renderer.Renderer;
 import com.threerings.opengl.renderer.TextureRenderer;
 import com.threerings.opengl.util.GlContext;
 
 import static com.threerings.opengl.Log.*;
 
 /**
  * Handles a render effect.
  */
 public class RenderEffect extends DynamicScope
     implements ConfigUpdateListener<RenderEffectConfig>, Renderer.Observer,
         Comparable<RenderEffect>
 {
     /**
      * Handles a single effect target.
      */
     public static abstract class Target extends SimpleScope
     {
         /**
          * Renders this target.
          */
         public void render ()
         {
             // render the previous bits
             if (_config.input == TargetConfig.Input.PREVIOUS) {
                 ((RenderEffect)_parentScope).renderPrevious();
             }
             // execute the steps
             for (Executor executor : _executors) {
                 executor.execute();
             }
         }
 
         @Override // documentation inherited
         public String getScopeName ()
         {
             return "target";
         }
 
         /**
          * Creates a new target.
          */
         protected Target (GlContext ctx, Scope parentScope)
         {
             super(parentScope);
             _ctx = ctx;
         }
 
         /**
          * Sets the configuration of this target.
          */
         protected void setConfig (TargetConfig config)
         {
             _config = config;
 
             // create the executors
             _executors = new Executor[config.steps.length];
             for (int ii = 0; ii < _executors.length; ii++) {
                 _executors[ii] = config.steps[ii].createExecutor(_ctx, this);
             }
         }
 
         /** The renderer context. */
         protected GlContext _ctx;
 
         /** The target config. */
         protected TargetConfig _config;
 
         /** Executors for the target steps. */
         protected Executor[] _executors;
     }
 
     /**
      * Renders to a texture.
      */
     public static class TextureTarget extends Target
     {
         /**
          * Creates a new texture target.
          */
         public TextureTarget (GlContext ctx, Scope parentScope, TargetConfig.Texture config)
         {
             super(ctx, parentScope);
             setConfig(config);
         }
 
         /**
          * Sets the configuration of this target.
          */
         public void setConfig (TargetConfig.Texture config)
         {
             super.setConfig(config);
             _renderer = config.getTextureRenderer(_ctx);
         }
 
         @Override // documentation inherited
         public void render ()
         {
             _renderer.startRender();
             try {
                 super.render();
             } finally {
                 _renderer.commitRender();
             }
         }
 
         @Override // documentation inherited
         public void dispose ()
         {
             super.dispose();
             _renderer.dispose();
         }
 
         /** The texture renderer. */
         protected TextureRenderer _renderer;
     }
 
     /**
      * Renders to the output.
      */
     public static class OutputTarget extends Target
     {
         /**
          * Creates a new output target.
          */
         public OutputTarget (GlContext ctx, Scope parentScope, TargetConfig.Output config)
         {
             super(ctx, parentScope);
             setConfig(config);
         }
 
         /**
          * Sets the configuration of this target.
          */
         public void setConfig (TargetConfig.Output config)
         {
             super.setConfig(config);
         }
     }
 
     /**
      * Creates a new render effect.
      */
     public RenderEffect (GlContext ctx, Scope parentScope, RenderEffectConfig config)
     {
         super("effect", parentScope);
         _ctx = ctx;
 
         Renderer renderer = _ctx.getRenderer();
         _width = renderer.getWidth();
         _height = renderer.getHeight();
         renderer.addObserver(this);
 
         setConfig(config);
     }
 
     /**
      * Sets the configuration of this effect.
      */
     public void setConfig (RenderEffectConfig config)
     {
         if (_config != null) {
             _config.removeListener(this);
         }
         _config = (config == null) ? null : (RenderEffectConfig)config.getInstance(this);
         if (_config != null) {
             _config.addListener(this);
         }
         updateFromConfig();
     }
 
     /**
      * Returns the effect's priority.
      */
     public int getPriority ()
     {
         return _priority;
     }
 
     /**
      * Renders this effect.
      *
      * @param idx the effect's index within the compositor list.
      */
     public void render (int idx)
     {
         // save the index
         _idx = idx;
 
         // render the intermediate targets, then the output
         for (Target target : _targets) {
             target.render();
         }
         _output.render();
     }
 
     // documentation inherited from interface ConfigUpdateListener
     public void configUpdated (ConfigEvent<RenderEffectConfig> event)
     {
         updateFromConfig();
     }
 
     // documentation inherited from interface Renderer.Observer
     public void sizeChanged (int width, int height)
     {
         _width = width;
         _height = height;
         wasUpdated();
     }
 
     // documentation inherited from interface Comparable
     public int compareTo (RenderEffect other)
     {
         return _priority - other._priority;
     }
 
     @Override // documentation inherited
     public void dispose ()
     {
         super.dispose();
         _ctx.getRenderer().removeObserver(this);
     }
 
     /**
      * Renders the previous contents of the compositor queues.
      */
     protected void renderPrevious ()
     {
         _ctx.getCompositor().renderPrevious(_idx);
     }
 
     /**
      * Updates the effect to match its new or modified configuration.
      */
     protected void updateFromConfig ()
     {
         // get the effect priority
         _priority = (_config == null) ? 0 : _config.getPriority(_ctx);
 
         // find a technique to render the effect
         String scheme = ScopeUtil.resolve(_parentScope, "renderScheme", (String)null);
         Technique technique = (_config == null) ?
             NOOP_TECHNIQUE : _config.getTechnique(_ctx, scheme);
         if (technique == null) {
             log.warning("No technique available to render effect.",
                 "config", _config.getName(), "scheme", scheme);
             technique = NOOP_TECHNIQUE;
         }
 
         // create the targets
         if (_targets != null) {
             for (Target target : _targets) {
                 target.dispose();
             }
         }
         _targets = new Target[technique.targets.length];
         for (int ii = 0; ii < _targets.length; ii++) {
             _targets[ii] = technique.targets[ii].createEffectTarget(_ctx, this);
         }
         if (_output != null) {
             _output.dispose();
         }
         _output = technique.output.createEffectTarget(_ctx, this);
     }
 
     /** The application context. */
     protected GlContext _ctx;
 
     /** The identity of the effect instance. */
     @Scoped
     protected String _identity = String.valueOf(System.identityHashCode(this));
 
     /** The width and height of the renderer surface. */
     @Scoped
     protected int _width, _height;
 
     /** The render effect configuration. */
     protected RenderEffectConfig _config;
 
     /** The priority of the effect. */
     protected int _priority;
 
     /** The intermediate targets. */
     protected Target[] _targets;
 
     /** The output target. */
     protected Target _output;
 
     /** Our index in the compositor's effect list. */
     protected int _idx;
 
     /** A technique that does nothing. */
     protected static final Technique NOOP_TECHNIQUE = new Technique();
 }
