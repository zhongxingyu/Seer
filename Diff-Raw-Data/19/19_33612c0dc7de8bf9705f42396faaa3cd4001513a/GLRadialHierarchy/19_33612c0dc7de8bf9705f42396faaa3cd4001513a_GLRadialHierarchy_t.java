 package org.caleydo.core.view.opengl.canvas.hierarchy;
 
 import gleem.linalg.Vec3f;
 import gleem.linalg.Vec4f;
 
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.lang.Math;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.glu.*;
 
 import org.caleydo.core.data.collection.ESetType;
 import org.caleydo.core.data.collection.ISet;
 import org.caleydo.core.data.selection.ESelectionType;
 import org.caleydo.core.data.selection.EVAOperation;
 import org.caleydo.core.manager.id.EManagedObjectType;
 import org.caleydo.core.manager.picking.EPickingMode;
 import org.caleydo.core.manager.picking.EPickingType;
 import org.caleydo.core.manager.picking.Pick;
 import org.caleydo.core.util.mapping.color.ColorMapping;
 import org.caleydo.core.util.mapping.color.ColorMappingManager;
 import org.caleydo.core.util.mapping.color.EColorMappingType;
 import org.caleydo.core.view.opengl.camera.IViewFrustum;
 import org.caleydo.core.view.opengl.canvas.AGLEventListener;
 import org.caleydo.core.view.opengl.canvas.EDetailLevel;
 import org.caleydo.core.view.opengl.canvas.remote.IGLCanvasRemoteRendering;
 import org.caleydo.core.view.opengl.mouse.PickingJoglMouseListener;
 import org.caleydo.core.view.opengl.util.GLHelperFunctions;
 
 import com.sun.opengl.util.BufferUtil;
 
 /**
  * Rendering the GLHeatMap
  * 
  * @author Alexander Lex
  * @author Marc Streit
  */
 public class GLRadialHierarchy
 	extends AGLEventListener {
 
 	private static final int DISP_HIER_DEPTH_DEFAULT = 4;
 	private ColorMapping colorMapper;
 
 	private Vec4f vecRotation = new Vec4f(-90, 0, 0, 1);
 
 	private Vec3f vecTranslation;
 
 	private float fAnimationTranslation = 0;
 
 	private boolean bIsTranslationAnimationActive = false;
 
 	private float fAnimationTargetTranslation = 0;
 
 	private int iMaxDisplayedHierarchyDepth;
 
 	private HashMap<Integer, PartialDisc> hashPartialDiscs;
 
 	private PartialDisc pdRealRootElement;
 	private PartialDisc pdCurrentRootElement;
 	private PartialDisc pdSelectedElement;
 
 	private GLU glu = new GLU();
 
 	boolean bIsInListMode = false;
 
 	boolean bUseDetailLevel = true;
 	ISet set;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param iViewID
 	 * @param iGLCanvasID
 	 * @param sLabel
 	 * @param viewFrustum
 	 */
 	public GLRadialHierarchy(ESetType setType, final int iGLCanvasID, final String sLabel,
 		final IViewFrustum viewFrustum) {
 		super(iGLCanvasID, sLabel, viewFrustum, true);
 
 		viewType = EManagedObjectType.GL_RADIAL_HIERARCHY;
 
 		ArrayList<ESelectionType> alSelectionTypes = new ArrayList<ESelectionType>();
 		alSelectionTypes.add(ESelectionType.NORMAL);
 		alSelectionTypes.add(ESelectionType.MOUSE_OVER);
 		alSelectionTypes.add(ESelectionType.SELECTION);
 
 		colorMapper = ColorMappingManager.get().getColorMapping(EColorMappingType.GENE_EXPRESSION);
 		hashPartialDiscs = new HashMap<Integer, PartialDisc>();
 		iMaxDisplayedHierarchyDepth = DISP_HIER_DEPTH_DEFAULT;
 	}
 
 	@Override
 	public void init(GL gl) {
 		initTestHierarchy();
 		if (set == null)
 			return;
 	}
 
 	@Override
 	public void initLocal(GL gl) {
 
 		iGLDisplayListIndexLocal = gl.glGenLists(1);
 		iGLDisplayListToCall = iGLDisplayListIndexLocal;
 		init(gl);
 	}
 
 	@Override
 	public void initRemote(final GL gl, final int iRemoteViewID,
 		final PickingJoglMouseListener pickingTriggerMouseAdapter,
 		final IGLCanvasRemoteRendering remoteRenderingGLCanvas) {
 
 		this.remoteRenderingGLCanvas = remoteRenderingGLCanvas;
 
 		this.pickingTriggerMouseAdapter = pickingTriggerMouseAdapter;
 
 		iGLDisplayListIndexRemote = gl.glGenLists(1);
 		iGLDisplayListToCall = iGLDisplayListIndexRemote;
 		init(gl);
 
 	}
 
 	private void initTestHierarchy() {
 
 		iMaxDisplayedHierarchyDepth = 3;
 		pdRealRootElement = new PartialDisc(0, 100, iUniqueID, pickingManager);
 		pdCurrentRootElement = pdRealRootElement;
 		hashPartialDiscs.put(0, pdRealRootElement);
 
 		PartialDisc[] children = new PartialDisc[5];
 		int childID = 0;
 		childID++;
 		children[0] = new PartialDisc(childID, 10, iUniqueID, pickingManager);
 		pdCurrentRootElement.addChild(children[0]);
 		hashPartialDiscs.put(childID, children[0]);
 		childID++;
 		children[1] = new PartialDisc(childID, 40, iUniqueID, pickingManager);
 		pdCurrentRootElement.addChild(children[1]);
 		hashPartialDiscs.put(childID, children[1]);
 		childID++;
 		children[2] = new PartialDisc(childID, 10, iUniqueID, pickingManager);
 		pdCurrentRootElement.addChild(children[2]);
 		hashPartialDiscs.put(childID, children[2]);
 		childID++;
 		children[3] = new PartialDisc(childID, 15, iUniqueID, pickingManager);
 		pdCurrentRootElement.addChild(children[3]);
 		hashPartialDiscs.put(childID, children[3]);
 		childID++;
 		children[4] = new PartialDisc(childID, 25, iUniqueID, pickingManager);
 		pdCurrentRootElement.addChild(children[4]);
 		hashPartialDiscs.put(childID, children[4]);
 
 		PartialDisc[] ch1 = new PartialDisc[2];
 		childID++;
 		ch1[0] = new PartialDisc(childID, 1, iUniqueID, pickingManager);
 		children[0].addChild(ch1[0]);
 		hashPartialDiscs.put(childID, ch1[0]);
 		childID++;
 		ch1[1] = new PartialDisc(childID, 9, iUniqueID, pickingManager);
 		children[0].addChild(ch1[1]);
 		hashPartialDiscs.put(childID, ch1[1]);
 
 		PartialDisc[] ch2 = new PartialDisc[3];
 		childID++;
 		ch2[0] = new PartialDisc(childID, 10, iUniqueID, pickingManager);
 		children[1].addChild(ch2[0]);
 		hashPartialDiscs.put(childID, ch2[0]);
 		childID++;
 		ch2[1] = new PartialDisc(childID, 5, iUniqueID, pickingManager);
 		children[1].addChild(ch2[1]);
 		hashPartialDiscs.put(childID, ch2[1]);
 		childID++;
 		ch2[2] = new PartialDisc(childID, 25, iUniqueID, pickingManager);
 		children[1].addChild(ch2[2]);
 		hashPartialDiscs.put(childID, ch2[2]);
 
 		PartialDisc[] ch4 = new PartialDisc[2];
 		childID++;
 		ch4[0] = new PartialDisc(childID, 10, iUniqueID, pickingManager);
 		children[3].addChild(ch4[0]);
 		hashPartialDiscs.put(childID, ch4[0]);
 		childID++;
 		ch4[1] = new PartialDisc(childID, 5, iUniqueID, pickingManager);
 		children[3].addChild(ch4[1]);
 		hashPartialDiscs.put(childID, ch4[1]);
 
 		PartialDisc[] ch5 = new PartialDisc[3];
 		childID++;
 		ch5[0] = new PartialDisc(childID, 10, iUniqueID, pickingManager);
 		children[4].addChild(ch5[0]);
 		hashPartialDiscs.put(childID, ch5[0]);
 		childID++;
 		ch5[1] = new PartialDisc(childID, 2, iUniqueID, pickingManager);
 		children[4].addChild(ch5[1]);
 		hashPartialDiscs.put(childID, ch5[1]);
 		childID++;
 		ch5[2] = new PartialDisc(childID, 13, iUniqueID, pickingManager);
 		children[4].addChild(ch5[2]);
 		hashPartialDiscs.put(childID, ch5[2]);
 
 	}
 
 	public synchronized void setToListMode(boolean bSetToListMode) {
 		this.bIsInListMode = bSetToListMode;
 		super.setDetailLevel(EDetailLevel.HIGH);
 		bUseDetailLevel = false;
 		setDisplayListDirty();
 	}
 
 	@Override
 	public synchronized void setDetailLevel(EDetailLevel detailLevel) {
 		if (bUseDetailLevel) {
 			super.setDetailLevel(detailLevel);
 			// renderStyle.setDetailLevel(detailLevel);
 		}
 
 	}
 
 	@Override
 	public synchronized void displayLocal(GL gl) {
 		pickingManager.handlePicking(iUniqueID, gl);
 
 		if (bIsDisplayListDirtyLocal) {
 			buildDisplayList(gl, iGLDisplayListIndexLocal);
 			bIsDisplayListDirtyLocal = false;
 		}
 		iGLDisplayListToCall = iGLDisplayListIndexLocal;
 
 		display(gl);
 		checkForHits(gl);
 
 		if (eBusyModeState != EBusyModeState.OFF) {
 			renderBusyMode(gl);
 		}
 	}
 
 	@Override
 	public synchronized void displayRemote(GL gl) {
 		if (bIsDisplayListDirtyRemote) {
 			buildDisplayList(gl, iGLDisplayListIndexRemote);
 			bIsDisplayListDirtyRemote = false;
 		}
 		iGLDisplayListToCall = iGLDisplayListIndexRemote;
 
 		display(gl);
 		checkForHits(gl);
 
 		// pickingTriggerMouseAdapter.resetEvents();
 	}
 
 	@Override
 	public synchronized void display(GL gl) {
 
 		// render(gl);
 		//clipToFrustum(gl);
 		//
 		gl.glCallList(iGLDisplayListToCall);
 
 		// buildDisplayList(gl, iGLDisplayListIndexRemote);
 	}
 
 	private void buildDisplayList(final GL gl, int iGLDisplayListIndex) {
 
 		gl.glNewList(iGLDisplayListIndex, GL.GL_COMPILE);
 		gl.glColor4f(1, 1, 1, 1);
 
 		float fXCenter = viewFrustum.getWidth() / 2;
 		float fYCenter = viewFrustum.getHeight() / 2;
 		gl.glTranslatef(fXCenter, fYCenter, 0);
 
 		int iDisplayedHierarchyDepth =
 			Math.min(iMaxDisplayedHierarchyDepth, pdCurrentRootElement.getHierarchyDepth());
 
 		float fDiscWidth =
 			Math.min(fXCenter - (fXCenter / 10), fYCenter - (fYCenter / 10)) / iDisplayedHierarchyDepth;
 
 		pdCurrentRootElement.drawHierarchyRoot(gl, glu, fDiscWidth, iDisplayedHierarchyDepth);
		
		gl.glTranslatef(-fXCenter, -fYCenter, 0);

 		gl.glEndList();
 	}
 
 	private void render(GL gl) {
 
 		// // gl.glDisable(GL.GL_DEPTH_TEST);
 		// // gl.glEnable(GL.GL_BLEND);
 		// // gl.glBlendFunc(GL.GL_SRC_ALPHA_SATURATE, GL.GL_ONE);
 		//
 		// zTransform += 1.0;
 		// if (zTransform == 360)
 		// zTransform = 0;
 		// // gl.glOrtho(0.0, 1.0, 0.0, 1.0, -1.0, 1.0);
 		// float[] array =
 		// new float[] { 1.0f, 0.0f, -5.0f, 1.0f, 1.0f, -5.0f, 0.0f, 1.0f, -5.0f, 0.0f, 0.0f, -5.0f };
 		// float[] array2 =
 		// new float[] { -1.0f, 0.0f, -2.0f, -1.0f, -1.0f, -2.0f, 0.0f, -1.0f, -2.0f, 0.0f, 0.0f, -2.0f };
 		// byte indices[] = { 0, 2, 3, 1 };
 		// ByteBuffer indexBuffer = BufferUtil.newByteBuffer(indices.length);
 		// indexBuffer.put(indices);
 		// FloatBuffer verticesBuffer = BufferUtil.newFloatBuffer(array.length + array2.length);
 		// // for(int i = 0; i < array.length; i++)
 		// verticesBuffer.put(array);
 		// verticesBuffer.put(array2);
 		//
 		// verticesBuffer.rewind();
 		// indexBuffer.rewind();
 		//
 		// gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
 		// gl.glVertexPointer(3, GL.GL_FLOAT, 0, verticesBuffer);
 		// // GLHelperFunctions.drawAxis(gl);
 		//
 		// int mode = 0;
 		gl.glMatrixMode(GL.GL_MODELVIEW);
 
 		// gl.glLoadIdentity();
 		gl.glTranslatef(2.0f, 2.0f, 0.0f);
 		// gl.glRotatef(zTransform, 0.0f, 0.0f, 1.0f);
 		// glu.gluPartialDisk(x, 1 , 2, 3, 1, 30, 60);
 		// gl.glColor4f(0, 1, 0, 0.8f);
 		// glu.gluPartialDisk(x, 1 , 2, 3, 1, 110, 30);
 
 		// glu.gluPartialDisk(x, 1 , 2, 10, 1, 180, 60);
 		// gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.RADIAL_HIERARCHY_SELECTION,
 		// 44));
 		// if(selection == 44)
 		// gl.glColor4f(1, 1, 0, 1);
 		// else
 		// gl.glColor4f(1, 0, 0, 0.5f);
 		// GLPrimitives.renderPartialDisc(gl, 1, 2, 90, 90);
 		//		
 		// GLPrimitives.renderPartialDisc(gl, 1, 2, 90, 90);
 		// gl.glPopName();
 		//		
 		// gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.RADIAL_HIERARCHY_SELECTION,
 		// 11));
 		// if(selection == 11)
 		// gl.glColor4f(1, 1, 0, 1);
 		// else
 		// gl.glColor4f(1, 0, 0, 1);
 		// GLPrimitives.renderPartialDisc(gl, 1, 2, 180, 90);
 		// GLPrimitives.renderCircle(gl, 1);
 		// gl.glPopName();
 		//		
 		// if(mode == 0)
 		// {
 		// // gl.glBegin(GL.GL_POLYGON);
 		// // gl.glArrayElement(0);
 		// // gl.glArrayElement(1);
 		// // gl.glArrayElement(2);
 		// // gl.glArrayElement(3);
 		// // // gl.glVertex3f(5, 4, zTransform);
 		// // // gl.glVertex3f(5, 5, zTransform);
 		// // // gl.glVertex3f(4, 5, zTransform);
 		// // // gl.glVertex3f(4, 4, zTransform);
 		// // gl.glEnd();
 		// // gl.glBegin(GL.GL_POLYGON);
 		// // gl.glArrayElement(4);
 		// // gl.glArrayElement(5);
 		// // gl.glArrayElement(6);
 		// // gl.glArrayElement(7);
 		// // gl.glVertex3f(5, 4, zTransform);
 		// // gl.glVertex3f(5, 5, zTransform);
 		// // gl.glVertex3f(4, 5, zTransform);
 		// // gl.glVertex3f(4, 4, zTransform);
 		// gl.glEnd();
 		// }
 		// else if(mode == 1)
 		// {
 		// gl.glDrawElements(GL.GL_POLYGON, 4, GL.GL_UNSIGNED_BYTE, indexBuffer);
 		// }
 	}
 
 	@Override
 	public String getDetailedInfo() {
 		return new String("");
 	}
 
 	@Override
 	protected void handleEvents(EPickingType ePickingType, EPickingMode pickingMode, int iExternalID,
 		Pick pick) {
 		if (detailLevel == EDetailLevel.VERY_LOW) {
 			pickingManager.flushHits(iUniqueID, ePickingType);
 			return;
 		}
 		switch (ePickingType) {
 
 			case RAD_HIERARCHY_PDISC_SELECTION:
 				
 				PartialDisc pdTemp = hashPartialDiscs.get(iExternalID);
 				
 				switch (pickingMode) {
 					case CLICKED:
 						
 						if (pdTemp != null && pdTemp != pdRealRootElement) {
 							if (pdSelectedElement != null) {
 								pdSelectedElement.setPDDrawingStrategy(DrawingStrategyManager.getInstance()
 									.getDrawingStrategy(DrawingStrategyManager.PD_DRAWING_STRATEGY_NORMAL));
 							}
 							if (pdTemp == pdCurrentRootElement) {
 								pdCurrentRootElement = pdRealRootElement;
 							}
 							else {
 								pdSelectedElement = pdTemp;
 								pdCurrentRootElement = pdSelectedElement;
 							}
 							setDisplayListDirty();
 						}
 						break;
 					case MOUSE_OVER:
 
 						if (pdTemp != null && pdTemp != pdSelectedElement) {
 							if (pdSelectedElement != null) {
 								pdSelectedElement.setPDDrawingStrategy(DrawingStrategyManager.getInstance()
 									.getDrawingStrategy(DrawingStrategyManager.PD_DRAWING_STRATEGY_NORMAL));
 							}
 							pdSelectedElement = pdTemp;
 							pdSelectedElement.setPDDrawingStrategy(DrawingStrategyManager.getInstance()
 								.getDrawingStrategy(DrawingStrategyManager.PD_DRAWING_STRATEGY_SELECTED));
 							setDisplayListDirty();
 						}
 						break;
 					default:
 						pickingManager.flushHits(iUniqueID, ePickingType);
 						return;
 				}
 				break;
 		}
 
 		pickingManager.flushHits(iUniqueID, ePickingType);
 	}
 
 	public boolean isInListMode() {
 		return bIsInListMode;
 	}
 
 	@Override
 	public void broadcastElements(EVAOperation type) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public int getNumberOfSelections(ESelectionType selectionType) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public String getShortInfo() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void clearAllSelections() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
