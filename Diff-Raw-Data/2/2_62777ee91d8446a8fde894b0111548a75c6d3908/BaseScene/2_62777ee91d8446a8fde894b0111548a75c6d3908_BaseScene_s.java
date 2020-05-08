 /**
  * 
  */
 package nl.lumenon.games.caravel.ardor3d;
 
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Random;
 
 import nl.lumenon.games.caravel.Caravel;
 import nl.lumenon.games.caravel.R;
 import nl.lumenon.games.caravel.ardor3d.ModelLibrary.ModelType;
 import nl.lumenon.utilities.datastructures.graph.Element;
 import nl.lumenon.utilities.datastructures.graph.Graph;
 import nl.lumenon.utilities.datastructures.graph.ElementInterface.ElementType;
 import android.app.Activity;
 import android.os.Handler;
 import android.os.Message;
 
 import com.ardor3d.bounding.BoundingBox;
 import com.ardor3d.framework.Canvas;
 import com.ardor3d.framework.Scene;
 import com.ardor3d.framework.Updater;
 import com.ardor3d.image.Image;
 import com.ardor3d.image.Texture;
 import com.ardor3d.input.Key;
 import com.ardor3d.input.android.AndroidFirstPersonControl;
 import com.ardor3d.input.logical.InputTrigger;
 import com.ardor3d.input.logical.KeyPressedCondition;
 import com.ardor3d.input.logical.TriggerAction;
 import com.ardor3d.input.logical.TwoInputStates;
 import com.ardor3d.intersection.PickResults;
 import com.ardor3d.intersection.PickingUtil;
 import com.ardor3d.intersection.PrimitivePickResults;
 import com.ardor3d.math.ColorRGBA;
 import com.ardor3d.math.Ray3;
 import com.ardor3d.math.Vector3;
 import com.ardor3d.renderer.Camera;
 import com.ardor3d.renderer.Renderer;
 import com.ardor3d.renderer.RendererCallable;
 import com.ardor3d.renderer.queue.RenderBucketType;
 import com.ardor3d.renderer.state.BlendState;
 import com.ardor3d.renderer.state.CullState;
 import com.ardor3d.renderer.state.MaterialState;
 import com.ardor3d.renderer.state.TextureState;
 import com.ardor3d.renderer.state.ZBufferState;
 import com.ardor3d.renderer.state.BlendState.DestinationFunction;
 import com.ardor3d.renderer.state.BlendState.SourceFunction;
 import com.ardor3d.renderer.state.CullState.Face;
 import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
 import com.ardor3d.renderer.state.RenderState.StateType;
 import com.ardor3d.renderer.state.ZBufferState.TestFunction;
 import com.ardor3d.scenegraph.Mesh;
 import com.ardor3d.scenegraph.Node;
 import com.ardor3d.scenegraph.Spatial;
 import com.ardor3d.scenegraph.hint.CullHint;
 import com.ardor3d.scenegraph.hint.DataMode;
 import com.ardor3d.scenegraph.hint.LightCombineMode;
 import com.ardor3d.scenegraph.hint.TransparencyType;
 import com.ardor3d.scenegraph.shape.Box;
 import com.ardor3d.scenegraph.shape.StripBox;
 import com.ardor3d.scenegraph.visitor.Visitor;
 import com.ardor3d.util.GameTaskQueue;
 import com.ardor3d.util.GameTaskQueueManager;
 import com.ardor3d.util.ReadOnlyTimer;
 import com.ardor3d.util.TextureManager;
 import com.ardor3d.util.geom.ClonedCopyLogic;
 import com.ardor3d.util.geom.GeometryTool;
 import com.ardor3d.util.geom.MeshCombiner;
 import com.ardor3d.util.geom.SceneCopier;
 import com.ardor3d.util.geom.SharedCopyLogic;
 import com.ardor3d.util.geom.GeometryTool.MatchCondition;
 
 /**
  * @author Gijs-Jan Roelofs
  *
  */
 public class BaseScene implements Scene{
 
 	/* Static Variables */
 	/* Instance Variables */
 	private Node _rootNode;
 	private DualPassWater _water;
 	private BoardNode _tiles;
 	private UINode _hud;
 	private OrbitCamControl _control;
 	private Ardor3DBase _ardor3d;
 	private Activity _parent;
 	private ModelLibrary _modelLibrary;	
 
 	public BaseScene(Activity parent){
 		_parent=parent;
 		_modelLibrary=new ModelLibrary(parent.getAssets(),parent.getResources());
 	}
 	
 	public void startSetup(Ardor3DBase ardor3d, Handler handler){
 		handler.sendMessage(handler.obtainMessage(0, 0, 0, "Starting Caravel"));
 
 		_ardor3d=ardor3d;
 		
 		//createTestCase(handler);
 		createNormalScene(handler,new Prototype().createTestGraph());
 		configureArdor3D(handler);
 		//_rootNode.getSceneHints().setDataMode(DataMode.VBO);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ardor3d.framework.Scene#renderUnto(com.ardor3d.renderer.Renderer)
 	 */
 	@Override
 	public boolean renderUnto(Renderer renderer) {
 		// execute queue
 		GameTaskQueueManager.getManager(_ardor3d._canvas).getQueue(GameTaskQueue.RENDER)
 				.execute(renderer);
 		
 		// draw root
 		_rootNode.draw(renderer);
 		
 		renderer.renderBuckets();
 		
 		if(_hud!=null)
 		_hud.draw(renderer);
 
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ardor3d.framework.Scene#doPick(com.ardor3d.math.Ray3)
 	 */
 	@Override
 	public PickResults doPick(Ray3 pickRay) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/* Inner Methods */
 	
 	private void configureArdor3D(Handler handler){
 
 		GameTaskQueueManager.getManager(_ardor3d._canvas).render(
 				new RendererCallable<Void>() {
 					public Void call() throws Exception {
 						getRenderer().setBackgroundColor(ColorRGBA.BLUE);
 						return null;
 					}
 				});
 		
 		// new AndroidOrbitControl(new Vector3(0,0,0),new double[]{0.5,10}).registerCallbacks(_ardor3d._logicalLayer);
 		
 		final ZBufferState zb = new ZBufferState();
 		zb.setEnabled(true);
 		zb.setWritable(true);
 		zb.setFunction(TestFunction.LessThan);
 		_rootNode.setRenderState(zb);
 		
 		CullState cs = new CullState();
 		cs.setCullFace(Face.Back);
 		_rootNode.setRenderState(cs);		
 
 		_rootNode.getSceneHints().setLightCombineMode(LightCombineMode.Off);		
 		//rootNode.getSceneHints().setDataMode(DataMode.VBOInterleaved);
 	}
 	
 	private void createNormalScene(Handler handler, Graph board){
 		_rootNode = new Node("Base Node"){
 		    private int frames=0;
 		    private long startTime=System.currentTimeMillis();
 			private int render=0;
 		    
 			public void draw(Renderer renderer){
 				super.draw(renderer);
 
 				final long now = System.currentTimeMillis();
 				final long dt = now - startTime;
 				if (dt > 10000) {
 					final long fps = Math.round(1e3 * frames / dt);
 					System.out.println(fps + " fps "+render);
 					
 					startTime = now;
 					frames = 0;
 					
 					render++;
 					switch(render){
 					case(0):this.getSceneHints().setDataMode(DataMode.Arrays);break;
 					case(1):this.getSceneHints().setDataMode(DataMode.VBO);break;
 					case(2):;render=0;break;
 					}
 				}
 				frames++;
 			}
 		};
 		
 		_ardor3d._rootNode=_rootNode;
 
 		
 		// Create Water Node		
 		//_water = new DualPassWater(_modelLibrary.loadImage("lumenon", "water", ModelType.Rest), _modelLibrary.loadImage("lumenon", "water", ModelType.Rest), "Water Layer",100,100);
 		//_rootNode.attachChild(_water);
 		
 		// Create Island Tiles
 		_tiles = new BoardNode(_modelLibrary,handler, board); 
 		_rootNode.attachChild(_tiles);			
 		
 		_hud = new UINode(handler,_parent.getWindowManager().getDefaultDisplay().getWidth(),_parent.getWindowManager().getDefaultDisplay().getHeight());
 		_rootNode.attachChild(_hud);
 				
 		Camera camera = _ardor3d._canvas.getCanvasRenderer().getCamera();
 		camera.setLocation(-3, 6, -3);
 		camera.lookAt(new Vector3(4,0,4),Vector3.UNIT_Y);
 		
 		_rootNode.updateGeometricState(50,true);
 		_rootNode.updateWorldTransform(true);
 		_rootNode.updateControllers(50);
 		_rootNode.updateWorldBound(true);
 		_rootNode.updateWorldRenderStates(true);	
 
 		handler.sendMessage(handler.obtainMessage(0, 3600, Caravel.LOADING_DONE, "Done"));
 	}
 	
 	private void createTestCase(Handler handler){
 		_rootNode = new Node("Base Node");	
 
 		_ardor3d._rootNode=_rootNode;
 				
 		Camera camera = _ardor3d._canvas.getCanvasRenderer().getCamera();
 		camera.setLocation(0, 0, -3);
 		camera.lookAt(new Vector3(0,0,0),Vector3.UNIT_Y);
 		
 		_rootNode.attachChild(new TestCaseNode());	
 		
 		_rootNode.updateGeometricState(50,true);
 		_rootNode.updateWorldTransform(true);
 		_rootNode.updateControllers(50);
 		_rootNode.updateWorldBound(true);
 		_rootNode.updateWorldRenderStates(true);	
 
 		handler.sendMessage(handler.obtainMessage(0, 3600, Caravel.LOADING_DONE, "Done"));
 	}
 
 	private class CleanUpVisitor implements Visitor{
 		@Override
 		public void visit(Spatial s) {
 			if(s instanceof Mesh){
 				GeometryTool.minimizeVerts((Mesh)s, EnumSet.allOf(MatchCondition.class));
 			}
 			
 			s.clearRenderState(StateType.Material);
 			s.clearRenderState(StateType.Light);
 			s.clearRenderState(StateType.Texture);
 			s.clearRenderState(StateType.Shading);
 			s.clearRenderState(StateType.Blend);
 			s.clearRenderState(StateType.Clip);
 			s.clearRenderState(StateType.ColorMask);
 			s.clearRenderState(StateType.Cull);
 			s.clearRenderState(StateType.Fog);
 			s.clearRenderState(StateType.FragmentProgram);
 			s.clearRenderState(StateType.GLSLShader);
 			s.clearRenderState(StateType.Offset);
 			s.clearRenderState(StateType.Stencil);
 			s.clearRenderState(StateType.VertexProgram);
 			s.clearRenderState(StateType.Wireframe);
 			s.clearRenderState(StateType.ZBuffer);
 		}
 	}
 	
 	public class SceneSetupVisitor implements Visitor {
 		private boolean transparent;
 
 		public SceneSetupVisitor(boolean transparent) {
 			super();
 			this.transparent = transparent;
 		}
 
 		@Override
 		public void visit(Spatial spatial) {
 			if(transparent){
 				final ZBufferState zb = new ZBufferState();
 				zb.setEnabled(true);
 				zb.setWritable(true);
 				zb.setFunction(TestFunction.LessThanOrEqualTo);
 				spatial.setRenderState(zb);
 
 				CullState cs = new CullState();
 				cs.setCullFace(Face.Back);
 				spatial.setRenderState(cs);
 				
 				BlendState as = new BlendState();
 				as.setSourceFunction(SourceFunction.SourceAlpha);
 				as.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
 				as.setTestEnabled(true);				
 				as.setBlendEnabled(true);
 				as.setTestFunction(BlendState.TestFunction.GreaterThanOrEqualTo);
 				spatial.setRenderState(as);
 				
 
 				spatial.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
 				spatial.getSceneHints().setLightCombineMode(LightCombineMode.Off);
 				spatial.getSceneHints().setTransparencyType(TransparencyType.TwoPass);
 				
 			} else {
 				final ZBufferState zb = new ZBufferState();
 				zb.setEnabled(true);
 				zb.setWritable(true);
 				zb.setFunction(TestFunction.LessThanOrEqualTo);
 				spatial.setRenderState(zb);
 
 				CullState cs = new CullState();
 				cs.setCullFace(Face.Back);
 				spatial.setRenderState(cs);
 				
 				BlendState as = new BlendState();
 				as.setSourceFunction(SourceFunction.SourceAlpha);
 				as.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
 				as.setTestEnabled(false);
 				as.setBlendEnabled(false);
 				as.setTestFunction(com.ardor3d.renderer.state.BlendState.TestFunction.Always);
 				as.setEnabled(false);
 				spatial.setRenderState(as);		
 
 				spatial.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);
 				spatial.getSceneHints().setLightCombineMode(LightCombineMode.Off);		
 			}			
 		}			
 	}
 	
 	/* Utility Methods */
 	
 	public void destroy(){
 		_rootNode.detachAllChildren();
 		_rootNode=null;
 		
 		_water.detachAllChildren();
 		_water=null;
 		
 		_tiles.detachAllChildren();
 		((BoardNode)_tiles).destroy();
 		_tiles=null;
 		
 		//constructions_.detachAllChildren();
 		//constructions_=null;
 		
 		//hud.detachAllChildren();
 		//hud=null;
 		
 		_ardor3d=null;
 		_parent=null;
 		
 		_modelLibrary=null;
 		_parent=null;
 	}
 
 	/* Static Methods */
 	
 	public class Prototype{
 
 		public Graph createTestGraph(){
 			Graph<String, String, String> graph = new Graph("Test Board Build");
 			
			String[] names = new String[]{"Grain","Wool","Desert","Wood","Stone", "Ore"};
 			
 			int size = 2;
 			//String[][] board = new String[][]{{"Grain","Wool","Desert","Grain","Wool","Desert"},{"Desert","Wood","Desert"},{"Desert","Desert","Desert"}};
 			Random r = new Random(42);
 			String[][] board = new String[6][6];
 			for (int i = 0; i < board.length; i++) {
 				for (int j = 0; j < board[i].length; j++) {
 					board[i][j]=names[r.nextInt(names.length)];
 				}
 			}
 			
 			for (int i = 0; i < board.length; i++) {
 				for (int j = 0; j < board[i].length; j++) {
 					// Base X/Y
 					double x=i*3, 
 							y=(j*((size*2)*0.866));
 					// Extra off-numbered row Y offset
 					y+=(i%2)*size*0.866;
 					constructHexagon(graph, x, y, size, board[i][j]);
 				}
 			}
 			
 			return graph;
 		}
 		
 		private void constructHexagon(Graph<String, String, String> graph,double x, double y, double size, String name){
 			/* Create Vertices */
 			// We start at the positive for both X&Y, then clockwise
 			Element<String> vA = new Element<String>(name+"- vA", new double[]{x+(size/2),y+(size*0.866)});
 			Element<String> vB = new Element<String>(name+"- vB", new double[]{x+size,y});
 			Element<String> vC = new Element<String>(name+"- vC", new double[]{x+(size/2),y-(size*0.866)});
 			Element<String> vD = new Element<String>(name+"- vD", new double[]{x-(size/2),y-(size*0.866)});
 			Element<String> vE = new Element<String>(name+"- vE", new double[]{x-size,y});
 			Element<String> vF = new Element<String>(name+"- vF", new double[]{x-(size/2),y+(size*0.866)});
 			
 			// Add them to the graph
 			vA=graph.addElement(vA);
 			vB=graph.addElement(vB);
 			vC=graph.addElement(vC);
 			vD=graph.addElement(vD);
 			vE=graph.addElement(vE);
 			vF=graph.addElement(vF);
 			
 			/* Create Edges */
 			Element<String> eA = new Element<String>(name+"- eA", vA, vB);
 			Element<String> eB = new Element<String>(name+"- eB", vB, vC);
 			Element<String> eC = new Element<String>(name+"- eC", vC, vD);
 			Element<String> eD = new Element<String>(name+"- eD", vD, vE);
 			Element<String> eE = new Element<String>(name+"- eE", vE, vF);
 			Element<String> eF = new Element<String>(name+"- eF", vF, vA);
 			
 			// Add them to the graph
 			eA=graph.addElement(eA);
 			eB=graph.addElement(eB);
 			eC=graph.addElement(eC);
 			eD=graph.addElement(eD);
 			eE=graph.addElement(eE);
 			eF=graph.addElement(eF);
 			
 			/* Create Areas */
 			Element<String> hexagon = new Element<String>(name, Arrays.asList(new Element[]{eA,eB,eC,eD,eE,eF}));
 			
 			// Add them to the graph
 			hexagon=graph.addElement(hexagon);
 		}
 
 		public void createTestScene(Handler handler){
 
 			_rootNode = new Node("root");
 			
 			GameTaskQueueManager.getManager(_ardor3d._canvas).render(
 					new RendererCallable<Void>() {
 						public Void call() throws Exception {
 							getRenderer().setBackgroundColor(ColorRGBA.BLUE);
 							return null;
 						}
 					});
 			
 		    final int edge = 30;
 			final Node origNode = new Node();
 	        final Node scene = new Node();
 	        final Node merged = new Node();
 	        scene.getSceneHints().setCullHint(CullHint.Dynamic);
 			
 	        Mesh base = new StripBox("stripbox",new Vector3(),.5,.5,.5);
 	        for (int i = 0, max = edge * edge; i < max; i++) {
 	        	//Mesh box = (Mesh) SceneCopier.makeCopy(base, new SharedCopyLogic());
 	            Mesh box = new Box("stripbox" + i, new Vector3(0, 0, 0), .5, .5, .5);
 	        	//box.setTranslation();
 	        	box.getMeshData().translatePoints(new Vector3(i % edge, i / edge, 0));
 
 	        	box.setModelBound(new BoundingBox());
 	        	box.setSolidColor(ColorRGBA.randomColor(null));
 	            origNode.attachChild(box);
 
 	    		handler.sendMessage(handler.obtainMessage(0, i, 0, "Creating Boxes:"+i));
 	        }
 
 	        // Create a single Mesh from the origNode and its children.
 	        final Mesh merge = MeshCombiner.combine(origNode);
 
 	        
 	        for(int i=1;i<4;i++){
 	        	Spatial box = merge.makeCopy(true);
 	        	box.setTranslation(new Vector3(30*i,0,0));
 	        	//merged.attachChild(box);
 	        	
 	    		handler.sendMessage(handler.obtainMessage(0, (i*edge*edge)+(edge*edge), 0,"Creating Copies of Boxes:"+i));
 	        }
 	        
 	        merged.attachChild(merge);
 	        scene.attachChild(merged);
 	        // attach to scene.. default will be to show the merged version first
 	        _rootNode.attachChild(scene);
 
 	        // and a texture, this will cover both the uncombined and combined meshes.
 	        final TextureState ts = new TextureState();
 	        ts.setTexture(TextureManager.load("images/ardor3d_white_256.jpg", Texture.MinificationFilter.Trilinear, true));
 	        scene.setRenderState(ts);
 
 	        // set a material state that applies our vertex coloring to the lighting equation
 	        final MaterialState ms = new MaterialState();
 	        ms.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
 	        scene.setRenderState(ms);
 
 	        // position our camera to take in all of the mesh
 	        _ardor3d._canvas.getCanvasRenderer().getCamera().setLocation((edge*3) / 2, (edge*3) / 2, 2 * edge);
 
 			handler.sendMessage(handler.obtainMessage(0, 3600, Caravel.LOADING_DONE, "Done"));
 			
 			new AndroidFirstPersonControl().registerCallbacks(_ardor3d._logicalLayer);
 		}
 		
 	}
 
 }
