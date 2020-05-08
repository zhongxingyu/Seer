 package jo.sm.ui.lwjgl;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.KeyboardFocusManager;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import jo.sm.data.BlockTypes;
 import jo.sm.data.RenderPoly;
 import jo.sm.data.RenderSet;
 import jo.sm.data.SparseMatrix;
 import jo.sm.data.UndoBuffer;
 import jo.sm.logic.RenderPolyLogic;
 import jo.sm.logic.StarMadeLogic;
 import jo.sm.ship.data.Block;
 import jo.sm.ui.RenderPanel;
 import jo.sm.ui.logic.ShipSpec;
 import jo.util.jgl.logic.SceneLogic;
 import jo.util.jgl.obj.JGLCamera;
 import jo.util.jgl.obj.JGLGroup;
 import jo.util.jgl.obj.JGLScene;
 import jo.util.jgl.obj.tri.JGLObj;
 import jo.util.lwjgl.win.JGLCanvas;
 import jo.vecmath.Color4f;
 import jo.vecmath.Point3f;
 import jo.vecmath.Point3i;
 import jo.vecmath.Vector3f;
 import jo.vecmath.logic.Matrix4fLogic;
 
 @SuppressWarnings("serial")
 public class LWJGLRenderPanel extends RenderPanel
 {
     private static final float  PIXEL_TO_RADIANS = (1f/3.14159f/16f);
     private static final float  ROLL_SCALE = 1.1f;
     
     private static final int MOUSE_MODE_NULL = 0;
     private static final int MOUSE_MODE_PIVOT = 1;
     private static final int MOUSE_MODE_SELECT = 2;
     
     private Point               mMouseDownAt;
     private Point3f             mMousePivotAround;
     private int                 mMouseMode;
     private JGLCanvas           mCanvas;
     private JGLScene			mScene;
     private JGLCamera			mUniverse;
     private JGLGroup			mBlocks;
     private JGLGroup			mSelection;
     private JGLGroup			mAxis;
     
     private SparseMatrix<Block> mGrid;
     private SparseMatrix<Block> mFilteredGrid;
     private Set<Short>          mFilter;
     private boolean             mPlainGraphics;
     private UndoBuffer          mUndoer;
     private ShipSpec            mSpec;
     private RenderSet           mTiles;
 
     private Vector3f            mPreTranslate;
     private float               mScale;
     private float               mRotX;
     private float               mRotY;
     Vector3f            		mPOVTranslate;
     private Vector3f            mPostTranslate;
 
     public LWJGLRenderPanel()
     {
         mUndoer = new UndoBuffer();
         mTiles = new RenderSet();
         mPreTranslate = new Vector3f();
         mPOVTranslate = new Vector3f();
         mScale = 1f;
         mRotX = (float)Math.PI;
         mRotY = 0;
         mPostTranslate = new Vector3f();
         mScene = new JGLScene();
         mScene.setBackground(new Color4f());
         mUniverse = new JGLCamera();
         mScene.setNode(mUniverse);
         mBlocks = new JGLGroup();
         mUniverse.getChildren().add(mBlocks);
         mSelection = new JGLGroup();
         mUniverse.getChildren().add(mSelection);
         mAxis = new JGLGroup();
         mUniverse.getChildren().add(mAxis);
         mCanvas = new JGLCanvas();
         mCanvas.setScene(mScene);
         setLayout(new BorderLayout());
         add("Center", mCanvas);
         MouseAdapter ma =  new MouseAdapter(){
             public void mousePressed(MouseEvent ev)
             {               
                 if (ev.getButton() == MouseEvent.BUTTON1)
                     doMouseDown(ev.getPoint(), ev.getModifiers());
             }
             public void mouseReleased(MouseEvent ev)
             {
                 if (ev.getButton() == MouseEvent.BUTTON1)
                     doMouseUp(ev.getPoint(), ev.getModifiers());
             }
             public void mouseDragged(MouseEvent ev)
             {
                 if (mMouseDownAt != null)
                     doMouseMove(ev.getPoint(), ev.getModifiers());
             }
             public void mouseWheelMoved(MouseWheelEvent e)
             {
                 doMouseWheel(e.getWheelRotation());
             }
         };
         mCanvas.addMouseListener(ma);
         mCanvas.addMouseMotionListener(ma);
         mCanvas.addMouseWheelListener(ma);
         KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
         		new LWJGLKeyEventDispatcher(this));
     }
     
     private void doMouseDown(Point p, int modifiers)
     {
         mMouseDownAt = p;
         //System.out.println("MouseMod="+Integer.toHexString(modifiers));
         if ((modifiers&MouseEvent.SHIFT_MASK) != 0)
         {
             RenderPoly tile = getTileAt(p.x, p.y);
             if (tile == null)
                 return;
             mMouseMode = MOUSE_MODE_SELECT;
             StarMadeLogic.getInstance().setSelectedLower(null);
             StarMadeLogic.getInstance().setSelectedUpper(null);
             extendSelection(tile);
         }
         else
         {
             mMouseMode = MOUSE_MODE_PIVOT;
             Point3i pivot = getPointAt(p.x, p.y);
             if (pivot != null)
                 mMousePivotAround = new Point3f(pivot.x, pivot.y, pivot.z);
             else
                 mMousePivotAround = null;
         }
     }
     
     private void doMouseMove(Point p, int modifiers)
     {
         if (mMouseMode == MOUSE_MODE_PIVOT)
         {
             int dx = p.x - mMouseDownAt.x;
             int dy = p.y - mMouseDownAt.y;
             mMouseDownAt = p;
             if ((dx != 0) || (dy != 0))
             {
                 if (mMousePivotAround == null)
                 {
                     if (dx != 0)
                         mUniverse.getCamera().yaw(-dx*PIXEL_TO_RADIANS);
                     if (dy != 0)
                         mUniverse.getCamera().pitch(-dy*PIXEL_TO_RADIANS);
                 }
                 else
                {
                    System.out.println("Pivot around "+mMousePivotAround);
                     mUniverse.getCamera().rotateAround(mMousePivotAround, new Point3f(dx*PIXEL_TO_RADIANS, dy*PIXEL_TO_RADIANS, 0));
                }
                 updateTransform();
             }
         }
         else if (mMouseMode == MOUSE_MODE_SELECT)
         {
             RenderPoly tile = getTileAt(p.x, p.y);
             if (tile != null)
                 extendSelection(tile);
         }
     }
     
     private void doMouseUp(Point p, int modifiers)
     {
         if (mMouseMode == MOUSE_MODE_PIVOT)
         {
             doMouseMove(p, modifiers);
             mMouseDownAt = null;
         }
         else if (mMouseMode == MOUSE_MODE_PIVOT)
         {
             doMouseMove(p, modifiers);
         }
         mMouseMode = MOUSE_MODE_NULL;
     }
 
     private void doMouseWheel(int roll)
     {
         if (roll == 0)
             return;
         mUniverse.getCamera().moveForward(roll*1.0f);
         updateTransform();
     }
     
     private void extendSelection(RenderPoly tile)
     {
         Point3i lowest = new Point3i();
         Point3i highest = new Point3i();
         RenderPolyLogic.getBounds(tile, lowest, highest);
         Point3i lower = StarMadeLogic.getInstance().getSelectedLower();
         if (lower == null)
         {
             lower = lowest;
             StarMadeLogic.getInstance().setSelectedLower(lower);
         }
         else
         {
             lower.x = Math.min(lower.x, lowest.x);
             lower.y = Math.min(lower.y, lowest.y);
             lower.z = Math.min(lower.z, lowest.z);
         }
         Point3i upper = StarMadeLogic.getInstance().getSelectedUpper();
         if (upper == null)
         {
             upper = highest;
             StarMadeLogic.getInstance().setSelectedUpper(upper);
         }
         else
         {
             upper.x = Math.min(upper.x, highest.x);
             upper.y = Math.min(upper.y, highest.y);
             upper.z = Math.min(upper.z, highest.z);
         }
         updateTiles();
     }
 
     @Override
     public void updateTransform()
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public SparseMatrix<Block> getGrid()
     {
         return mGrid;
     }
 
     @Override
     public void setGrid(SparseMatrix<Block> grid)
     {
         mGrid = grid;
         Point3i lower = new Point3i();
         Point3i upper = new Point3i();
         mGrid.getBounds(lower, upper);
         Point3f lookAtThis = new Point3f(upper.x + lower.x, upper.y + lower.y, upper.z + lower.z);
         lookAtThis.scale(.5f);
         float maxModel = Math.max(Math.max(upper.x - lower.x, upper.y - lower.y), upper.z - lower.z);
         Point3f standHere = new Point3f(lookAtThis);
         standHere.z -= maxModel*4;
         Dimension s = getSize();
         float maxScreen = Math.max(s.width, s.height);
         mScale = maxScreen/maxModel/2f;
         //mUniverse.getCamera().setLocation(lookAtThis);
         Matrix4fLogic.lookAt(mUniverse.getCamera(), standHere, lookAtThis);
         //mUniverse.getCamera().scale(mScale);
         System.out.println("Standing at "+standHere+", looking at "+lookAtThis);
         //mUniverse.setTransformer(new SpinningTransformer(new Point3f(0, 20, 0)));
         updateTiles();
     }
 
     @Override
     public void updateTiles()
     {
         if ((mFilter == null) || (mFilter.size() == 0))
             mFilteredGrid = mGrid;
         else
         {
             mFilteredGrid = new SparseMatrix<Block>();
             for (Iterator<Point3i> i = mGrid.iterator(); i.hasNext(); )
             {
                 Point3i p = i.next();
                 Block b = mGrid.get(p);
                 if ((b != null) && mFilter.contains(b.getBlockID()))
                     mFilteredGrid.set(p, b);
             }
         }
         /*
         mSelection.getChildren().clear();
         Point3i lower = StarMadeLogic.getInstance().getSelectedLower();
         Point3i upper = StarMadeLogic.getInstance().getSelectedUpper();
         if ((lower != null) && (upper != null))
             LWJGLRenderLogic.addBox(mSelection, new Point3f(lower), new Point3f(upper), new short[] { BlockTypes.SPECIAL_SELECT_XP, BlockTypes.SPECIAL_SELECT_XM,
                     BlockTypes.SPECIAL_SELECT_YP, BlockTypes.SPECIAL_SELECT_YM,
                     BlockTypes.SPECIAL_SELECT_ZP, BlockTypes.SPECIAL_SELECT_ZM,});
         mAxis.getChildren().clear();
         LWJGLRenderLogic.addBox(mAxis, new Point3f(9,8,8), new Point3f(256+8,8,8), new short[] { BlockTypes.SPECIAL_SELECT_XP });
         LWJGLRenderLogic.addBox(mAxis, new Point3f(8-256,8,8), new Point3f(7,8,8), new short[] { BlockTypes.SPECIAL_SELECT_XM });
         LWJGLRenderLogic.addBox(mAxis, new Point3f(8,9,8), new Point3f(8,256+8,8), new short[] { BlockTypes.SPECIAL_SELECT_YP });
         LWJGLRenderLogic.addBox(mAxis, new Point3f(8,8-256,8), new Point3f(8,7,8), new short[] { BlockTypes.SPECIAL_SELECT_YM });
         LWJGLRenderLogic.addBox(mAxis, new Point3f(8,8,9), new Point3f(8,8,256+8), new short[] { BlockTypes.SPECIAL_SELECT_ZP });
         LWJGLRenderLogic.addBox(mAxis, new Point3f(8,8,8-256), new Point3f(8,8,7), new short[] { BlockTypes.SPECIAL_SELECT_ZM });
         */
         mBlocks.getChildren().clear();
         LWJGLRenderLogic.addBlocks(mBlocks, mFilteredGrid, mPlainGraphics);
         System.out.println("Quads:"+mBlocks.getChildren().size());
 
 /*
         mBlocks.getChildren().clear();
         for (Iterator<Point3i> i = mFilteredGrid.iteratorNonNull(); i.hasNext(); )
         {
         	Point3i p = i.next();
         	Block b = mFilteredGrid.get(p);
         	Point3f center = new Point3f(p.x, p.y, p.z);
         	Color c = BlockTypeColors.getFillColor(b.getBlockID());
         	Point3f color = new Point3f(c.getComponents(null));
         	//System.out.println("Added at "+center+", color="+color);
         	JGLObjCube cube = new JGLObjCube(new Point3f(1, 1, 1), center);
         	cube.setSolidColor(color);
         	cube.setData("point", p);
         	cube.setData("block", b);
         	mBlocks.add(cube);
         }
         System.out.println("Added "+mBlocks.getChildren().size()+" blocks");
         */
     }
 
     @Override
     public RenderPoly getTileAt(double x, double y)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Block getBlockAt(double x, double y)
     {
         JGLObj cube = getObjAt(x, y);
         if (cube == null)
             return null;
         return (Block)cube.getData("block");
     }
 
     public Point3i getPointAt(double x, double y)
     {
         JGLObj cube = getObjAt(x, y);
         if (cube == null)
             return null;
         return (Point3i)cube.getData("point");
     }
 
     public JGLObj getObjAt(double x, double y)
     {
         List<JGLObj> hits = SceneLogic.intersect(mBlocks, (float)x, (float)y);
         if (hits.size() == 0)
         {
             System.out.println("No hits on "+x+","+y+"!");
             return null;
         }
         System.out.println(hits.size()+" hits on "+x+","+y+"!");
         JGLObj best = hits.get(0);
         System.out.println("  "+best.getScreen());
         return best;
     }
 
     @Override
     public Set<Short> getFilter()
     {
         return mFilter;
     }
 
     @Override
     public void setFilter(Set<Short> filter)
     {
         mFilter = filter;
     }
 
     @Override
     public ShipSpec getSpec()
     {
         return mSpec;
     }
 
     @Override
     public void setSpec(ShipSpec spec)
     {
         mSpec = spec;
     }
 
     @Override
     public boolean isPlainGraphics()
     {
         return mPlainGraphics;
     }
 
     @Override
     public void setPlainGraphics(boolean plainGraphics)
     {
         mPlainGraphics = plainGraphics;
     }
 
     @Override
     public boolean isAxis()
     {
         return !mAxis.isCull();
     }
 
     @Override
     public void setAxis(boolean axis)
     {
         mAxis.setCull(!axis);
     }
 
     public UndoBuffer getUndoer()
     {
         return mUndoer;
     }
 
     public void setUndoer(UndoBuffer undoer)
     {
         mUndoer = undoer;
     }
 
     public void undo()
     {
         SparseMatrix<Block> grid = mUndoer.undo();
         if (grid != null)
             setGrid(grid);
     }
 
     public void redo()
     {
         SparseMatrix<Block> grid = mUndoer.redo();
         if (grid != null)
             setGrid(grid);
     }
 
     @Override
     public void setCloseRequested(boolean pleaseClose)
     {
         mCanvas.setCloseRequested(pleaseClose);
     }
 
 	public void moveCamera(Point3i delta)
 	{
 		mUniverse.getCamera().moveRight(delta.x);
 		mUniverse.getCamera().moveUp(delta.y);
 		mUniverse.getCamera().moveForward(delta.z);
 	}
 }
