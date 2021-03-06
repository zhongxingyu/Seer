 package org.caleydo.view.histogram;
 
 import static org.caleydo.view.histogram.HistogramRenderStyle.SIDE_SPACING;
 
 import java.awt.Font;
 import java.awt.Point;
 import java.util.ArrayList;
 
 import javax.media.opengl.GL;
 
 import org.caleydo.core.data.collection.Histogram;
 import org.caleydo.core.data.collection.ISet;
 import org.caleydo.core.data.selection.EVAOperation;
 import org.caleydo.core.data.selection.SelectionType;
 import org.caleydo.core.manager.ISetBasedDataDomain;
 import org.caleydo.core.manager.datadomain.DataDomainManager;
 import org.caleydo.core.manager.event.view.ClearSelectionsEvent;
 import org.caleydo.core.manager.event.view.storagebased.RedrawViewEvent;
 import org.caleydo.core.manager.event.view.storagebased.UpdateViewEvent;
 import org.caleydo.core.manager.picking.EPickingMode;
 import org.caleydo.core.manager.picking.EPickingType;
 import org.caleydo.core.manager.picking.Pick;
 import org.caleydo.core.serialize.ASerializedView;
 import org.caleydo.core.util.format.Formatter;
 import org.caleydo.core.util.mapping.color.ColorMapping;
 import org.caleydo.core.util.mapping.color.ColorMappingManager;
 import org.caleydo.core.util.mapping.color.ColorMarkerPoint;
 import org.caleydo.core.util.mapping.color.EColorMappingType;
 import org.caleydo.core.view.ISetBasedView;
 import org.caleydo.core.view.opengl.camera.IViewFrustum;
 import org.caleydo.core.view.opengl.canvas.AGLView;
 import org.caleydo.core.view.opengl.canvas.EDetailLevel;
 import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
 import org.caleydo.core.view.opengl.canvas.listener.ClearSelectionsListener;
 import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
 import org.caleydo.core.view.opengl.canvas.listener.RedrawViewListener;
 import org.caleydo.core.view.opengl.mouse.GLMouseListener;
 import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;
 import org.caleydo.core.view.opengl.util.GLCoordinateUtils;
 import org.caleydo.core.view.opengl.util.overlay.infoarea.GLInfoAreaManager;
 
 import com.sun.opengl.util.j2d.TextRenderer;
 
 /**
  * Rendering the histogram.
  * 
  * @author Alexander Lex
  */
 public class GLHistogram extends AGLView implements ISetBasedView, IViewCommandHandler {
 
 	public final static String VIEW_ID = "org.caleydo.view.histogram";
 
 	boolean bUseDetailLevel = true;
 
 	private Histogram histogram;
 	private ColorMapping colorMapping;
 	// private HistogramRenderStyle renderStyle;
 
 	private boolean bUpdateColorPointPosition = false;
 	private boolean bUpdateLeftSpread = false;
 	private boolean bUpdateRightSpread = false;
 	private boolean bIsFirstTimeUpdateColor = false;
 	private float fColorPointPositionOffset = 0.0f;
 	private int iColorMappingPointMoved = -1;
 
 	protected RedrawViewListener redrawViewListener;
 	protected ClearSelectionsListener clearSelectionsListener;
 
 	private TextRenderer textRenderer;
 
 	float fRenderWidth;
 
 	private ColorMappingManager colorMappingManager;
 
 	private ISetBasedDataDomain dataDomain;
 	private ISet set;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param glCanvas
 	 * @param sLabel
 	 * @param viewFrustum
 	 */
 	public GLHistogram(GLCaleydoCanvas glCanvas, final String sLabel,
 			final IViewFrustum viewFrustum) {
 		super(glCanvas, sLabel, viewFrustum, true);
 
 		viewType = VIEW_ID;
 		registerDataDomains();
 
 		colorMappingManager = ColorMappingManager.get();
 		colorMapping = colorMappingManager
 				.getColorMapping(EColorMappingType.GENE_EXPRESSION);
 
 		renderStyle = new HistogramRenderStyle(this, viewFrustum);
 		textRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 18), true, true);
 		// registerEventListeners();
 	}
 
 	@Override
 	public void registerDataDomains() {
 		ArrayList<String> dataDomainTypes = new ArrayList<String>();
 		dataDomainTypes.add("org.caleydo.datadomain.genetic");
		dataDomainTypes.add("org.caleydo.datadomain.generic");
 
 		DataDomainManager.getInstance().getAssociationManager()
 				.registerDatadomainTypeViewTypeAssociation(dataDomainTypes, viewType);
 	}
 
 	@Override
 	public void init(GL gl) {
 
 		// FIXME: should be called from data domain
 		initData();
 	}
 
 	@Override
 	public void initLocal(GL gl) {
 
 		iGLDisplayListIndexLocal = gl.glGenLists(1);
 		iGLDisplayListToCall = iGLDisplayListIndexLocal;
 		init(gl);
 	}
 
 	@Override
 	public void initRemote(final GL gl, final AGLView glParentView,
 			final GLMouseListener glMouseListener, GLInfoAreaManager infoAreaManager) {
 
 		this.glMouseListener = glMouseListener;
 
 		iGLDisplayListIndexRemote = gl.glGenLists(1);
 		iGLDisplayListToCall = iGLDisplayListIndexRemote;
 		init(gl);
 
 	}
 
 	@Override
 	public void initData() {
 		super.initData();
 		histogram = ((ISetBasedDataDomain) dataDomain).getSet().getHistogram();
 	}
 
 	@Override
 	public void setDetailLevel(EDetailLevel detailLevel) {
 		if (bUseDetailLevel) {
 			super.setDetailLevel(detailLevel);
 			// renderStyle.setDetailLevel(detailLevel);
 		}
 
 	}
 
 	@Override
 	public void displayLocal(GL gl) {
 
 		pickingManager.handlePicking(this, gl);
 
 		if (bIsDisplayListDirtyLocal) {
 			colorMapping = ColorMappingManager.get().getColorMapping(
 					EColorMappingType.GENE_EXPRESSION);
 			buildDisplayList(gl, iGLDisplayListIndexLocal);
 			bIsDisplayListDirtyLocal = false;
 		}
 		iGLDisplayListToCall = iGLDisplayListIndexLocal;
 
 		display(gl);
 		checkForHits(gl);
 
 		// if (eBusyModeState != EBusyModeState.OFF) {
 		// renderBusyMode(gl);
 		// }
 	}
 
 	@Override
 	public void displayRemote(GL gl) {
 		if (bIsDisplayListDirtyRemote) {
 			colorMapping = ColorMappingManager.get().getColorMapping(
 					EColorMappingType.GENE_EXPRESSION);
 			buildDisplayList(gl, iGLDisplayListIndexRemote);
 			bIsDisplayListDirtyRemote = false;
 		}
 		iGLDisplayListToCall = iGLDisplayListIndexRemote;
 
 		display(gl);
 		checkForHits(gl);
 	}
 
 	@Override
 	public void display(GL gl) {
 		// processEvents();
 		if (bUpdateColorPointPosition || bUpdateLeftSpread || bUpdateRightSpread)
 			updateColorPointPosition(gl);
 		// clipToFrustum(gl);
 		//
 		gl.glCallList(iGLDisplayListToCall);
 		// buildDisplayList(gl, iGLDisplayListIndexRemote);
 	}
 
 	private void buildDisplayList(final GL gl, int iGLDisplayListIndex) {
 		gl.glNewList(iGLDisplayListIndex, GL.GL_COMPILE);
 		renderHistogram(gl);
 		renderColorBars(gl);
 		gl.glEndList();
 	}
 
 	/**
 	 * Render the histogram itself
 	 * 
 	 * @param gl
 	 */
 	private void renderHistogram(GL gl) {
 
 		float fSpacing = (viewFrustum.getWidth() - 2 * SIDE_SPACING) / histogram.size();
 		float fContinuousColorRegion = 1.0f / histogram.size();
 
 		float fOneHeightValue = (viewFrustum.getHeight() - 2 * SIDE_SPACING)
 				/ histogram.getLargestValue();
 
 		int iCount = 0;
 
 		for (Integer iValue : histogram) {
 			gl.glColor3fv(colorMapping.getColor(fContinuousColorRegion * iCount), 0);
 			gl.glLineWidth(3.0f);
 			gl.glBegin(GL.GL_POLYGON);
 
 			gl.glVertex3f(fSpacing * iCount + SIDE_SPACING, SIDE_SPACING, 0);
 			gl.glVertex3f(fSpacing * iCount + SIDE_SPACING, SIDE_SPACING + iValue
 					* fOneHeightValue, 0);
 			gl.glColor3fv(colorMapping.getColor(fContinuousColorRegion * (iCount + 1)), 0);
 			gl.glVertex3f(fSpacing * (iCount + 1) + SIDE_SPACING, SIDE_SPACING + iValue
 					* fOneHeightValue, 0);
 			gl.glVertex3f(fSpacing * (iCount + 1) + SIDE_SPACING, SIDE_SPACING, 0);
 			gl.glEnd();
 
 			gl.glBegin(GL.GL_LINE);
 			gl.glVertex3f(0, 0, 0);
 			gl.glVertex3f(2, 2, 2);
 			gl.glEnd();
 
 			iCount++;
 		}
 
 	}
 
 	/**
 	 * Render the color bars for selecting the color mapping
 	 * 
 	 * @param gl
 	 */
 	private void renderColorBars(GL gl) {
 
 		fRenderWidth = (viewFrustum.getWidth() - 2 * SIDE_SPACING);
 		ArrayList<ColorMarkerPoint> markerPoints = colorMapping.getMarkerPoints();
 
 		int iCount = 0;
 
 		for (ColorMarkerPoint markerPoint : markerPoints) {
 			int iColorLinePickingID = pickingManager.getPickingID(iUniqueID,
 					EPickingType.HISTOGRAM_COLOR_LINE, iCount);
 
 			boolean bIsFirstOrLast = false;
 			float fPickingScaling = 0.8f;
 			if (iCount == 0 || iCount == markerPoints.size() - 1)
 				bIsFirstOrLast = true;
 
 			if (markerPoint.hasLeftSpread()) {
 
 				float fLeftSpread = markerPoint.getLeftSpread();
 				int iLeftSpreadPickingID = pickingManager.getPickingID(iUniqueID,
 						EPickingType.HISTOGRAM_LEFT_SPREAD_COLOR_LINE, iCount);
 
 				// the left polygon between the central line and the spread
 				gl.glColor4f(markerPoint.getColor()[0], markerPoint.getColor()[1],
 						markerPoint.getColor()[2], 0.3f);
 
 				float fLeft = SIDE_SPACING + (markerPoint.getValue() - fLeftSpread)
 						* fRenderWidth;
 				float fRight = SIDE_SPACING + markerPoint.getValue() * fRenderWidth;
 
 				// the right part which picks the central line
 				if (!bIsFirstOrLast)
 					gl.glPushName(iColorLinePickingID);
 				gl.glBegin(GL.GL_POLYGON);
 				gl.glVertex3f(fRight + fPickingScaling * (fLeft - fRight), SIDE_SPACING,
 						-0.1f);
 				gl.glVertex3f(fRight + fPickingScaling * (fLeft - fRight),
 						viewFrustum.getHeight() - SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fRight, viewFrustum.getHeight() - SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fRight, SIDE_SPACING, -0.001f);
 				gl.glEnd();
 				if (!bIsFirstOrLast)
 					gl.glPopName();
 
 				// the left part which picks the spread
 				gl.glPushName(iLeftSpreadPickingID);
 				gl.glBegin(GL.GL_POLYGON);
 				gl.glVertex3f(fLeft, SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fLeft, viewFrustum.getHeight() - SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fRight + fPickingScaling * (fLeft - fRight),
 						viewFrustum.getHeight() - SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fRight + fPickingScaling * (fLeft - fRight), SIDE_SPACING,
 						-0.001f);
 				gl.glEnd();
 				gl.glPopName();
 
 				// the left spread line
 				gl.glColor3f(0, 0, 1);
 				gl.glPushName(iLeftSpreadPickingID);
 				gl.glBegin(GL.GL_LINES);
 				gl.glVertex3f(SIDE_SPACING + (markerPoint.getValue() - fLeftSpread)
 						* fRenderWidth, 0, 0);
 				gl.glVertex3f(SIDE_SPACING + (markerPoint.getValue() - fLeftSpread)
 						* fRenderWidth, viewFrustum.getHeight(), 0);
 				gl.glEnd();
 				gl.glPopName();
 				if (fLeftSpread > HistogramRenderStyle.SPREAD_CAPTION_THRESHOLD)
 					renderCaption(gl, markerPoint.getValue() - fLeftSpread);
 
 			}
 
 			if (markerPoint.hasRightSpread()) {
 				float fRightSpread = markerPoint.getRightSpread();
 
 				float fLeft = SIDE_SPACING + markerPoint.getValue() * fRenderWidth;
 				float fRight = SIDE_SPACING + (markerPoint.getValue() + fRightSpread)
 						* fRenderWidth;
 
 				int iRightSpreadPickingID = pickingManager.getPickingID(iUniqueID,
 						EPickingType.HISTOGRAM_RIGHT_SPREAD_COLOR_LINE, iCount);
 
 				// the polygon between the central line and the right spread
 				// the first part which picks the central line
 				gl.glColor4f(markerPoint.getColor()[0], markerPoint.getColor()[1],
 						markerPoint.getColor()[2], 0.3f);
 				if (!bIsFirstOrLast)
 					gl.glPushName(iColorLinePickingID);
 				gl.glBegin(GL.GL_POLYGON);
 				gl.glVertex3f(fLeft, SIDE_SPACING, -0.011f);
 				gl.glVertex3f(fLeft, viewFrustum.getHeight() - SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fLeft + fPickingScaling * (fRight - fLeft),
 						viewFrustum.getHeight() - SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fLeft + fPickingScaling * (fRight - fLeft), SIDE_SPACING,
 						-0.1f);
 				gl.glEnd();
 				if (!bIsFirstOrLast)
 					gl.glPopName();
 
 				// the second part which picks the spread
 				gl.glPushName(iRightSpreadPickingID);
 				gl.glBegin(GL.GL_POLYGON);
 				gl.glVertex3f(fLeft + fPickingScaling * (fRight - fLeft), SIDE_SPACING,
 						-0.011f);
 				gl.glVertex3f(fLeft + fPickingScaling * (fRight - fLeft),
 						viewFrustum.getHeight() - SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fRight, viewFrustum.getHeight() - SIDE_SPACING, -0.1f);
 				gl.glVertex3f(fRight, SIDE_SPACING, -0.1f);
 				gl.glEnd();
 				gl.glPopName();
 
 				// the right spread line
 				gl.glColor3f(0, 0, 1);
 				gl.glPushName(iRightSpreadPickingID);
 				gl.glBegin(GL.GL_LINES);
 				gl.glVertex3f(SIDE_SPACING + (markerPoint.getValue() + fRightSpread)
 						* fRenderWidth, 0, 0);
 				gl.glVertex3f(SIDE_SPACING + (markerPoint.getValue() + fRightSpread)
 						* fRenderWidth, viewFrustum.getHeight(), 0);
 				gl.glEnd();
 				gl.glPopName();
 				if (fRightSpread > HistogramRenderStyle.SPREAD_CAPTION_THRESHOLD)
 					renderCaption(gl, markerPoint.getValue() + fRightSpread);
 
 			}
 
 			// the central line
 			// gl.glColor3f(0, 0, 1);
 			// if (!bIsFirstOrLast)
 			// gl.glPushName(iColorLinePickingID);
 			// gl.glBegin(GL.GL_LINES);
 			// gl.glVertex3f(SIDE_SPACING + markerPoint.getValue() *
 			// fRenderWidth,
 			// 0, 0);
 			// gl.glVertex3f(SIDE_SPACING + markerPoint.getValue() *
 			// fRenderWidth,
 			// viewFrustum.getHeight(), 0);
 			// gl.glEnd();
 			// if (!bIsFirstOrLast)
 			// gl.glPopName();
 
 			renderCaption(gl, markerPoint.getValue());
 
 			iCount++;
 		}
 
 	}
 
 	private void renderCaption(GL gl, float normalizedValue) {
 
 		if (getParentGLCanvas().getSize().getWidth() < 500)
 			return;
 
 		textRenderer.begin3DRendering();
 		textRenderer.setColor(0, 0, 0, 1);
 		gl.glDisable(GL.GL_DEPTH_TEST);
 
 		double correspondingValue = ((ISetBasedDataDomain) dataDomain).getSet()
 				.getRawForNormalized(normalizedValue);
 
 		String text = Formatter.formatNumber(correspondingValue);
 
 		textRenderer.draw3D(text, SIDE_SPACING + normalizedValue * fRenderWidth
 				+ HistogramRenderStyle.CAPTION_SPACING,
 				HistogramRenderStyle.CAPTION_SPACING, 0.001f,
 				GeneralRenderStyle.HEADING_FONT_SCALING_FACTOR);
 		// textRenderer.flush();
 		textRenderer.end3DRendering();
 	}
 
 	/**
 	 * React on drag operations of the color lines and areas
 	 * 
 	 * @param gl
 	 */
 	private void updateColorPointPosition(GL gl) {
 		if (glMouseListener.wasMouseReleased()) {
 			// send out a major update which tells the hhm to update its
 			// textures
 			UpdateViewEvent event = new UpdateViewEvent();
 			event.setSender(this);
 			eventPublisher.triggerEvent(event);
 
 			bUpdateColorPointPosition = false;
 			bUpdateLeftSpread = false;
 			bUpdateRightSpread = false;
 		}
 
 		setDisplayListDirty();
 		Point currentPoint = glMouseListener.getPickedPoint();
 
 		float[] fArTargetWorldCoordinates = GLCoordinateUtils
 				.convertWindowCoordinatesToWorldCoordinates(gl, currentPoint.x,
 						currentPoint.y);
 
 		ArrayList<ColorMarkerPoint> markerPoints = colorMapping.getMarkerPoints();
 		ColorMarkerPoint markerPoint = markerPoints.get(iColorMappingPointMoved);
 
 		float fClickedPointX = fArTargetWorldCoordinates[0];
 
 		if (bIsFirstTimeUpdateColor && bUpdateColorPointPosition) {
 			bIsFirstTimeUpdateColor = false;
 			fColorPointPositionOffset = fClickedPointX - SIDE_SPACING
 					- markerPoint.getValue()
 					* (viewFrustum.getWidth() - 2 * SIDE_SPACING);
 			fClickedPointX -= fColorPointPositionOffset;
 		} else if (bUpdateColorPointPosition) {
 			fClickedPointX -= fColorPointPositionOffset;
 		}
 
 		if (fClickedPointX < SIDE_SPACING)
 			fClickedPointX = SIDE_SPACING;
 		if (fClickedPointX > viewFrustum.getWidth() - SIDE_SPACING)
 			fClickedPointX = viewFrustum.getWidth() - SIDE_SPACING;
 
 		fClickedPointX = (fClickedPointX - SIDE_SPACING)
 				/ (viewFrustum.getWidth() - 2 * SIDE_SPACING);
 
 		if (iColorMappingPointMoved > 0) {
 			ColorMarkerPoint previousPoint = markerPoints
 					.get(iColorMappingPointMoved - 1);
 			float fRightOfPrevious = previousPoint.getValue();
 
 			fRightOfPrevious += previousPoint.getRightSpread();
 
 			float fCurrentLeft = fClickedPointX;
 			if (bUpdateColorPointPosition) {
 				fCurrentLeft -= markerPoint.getLeftSpread();
 				if (fCurrentLeft <= fRightOfPrevious + 0.01f)
 					fClickedPointX = fRightOfPrevious + 0.01f
 							+ markerPoint.getLeftSpread();
 			}
 			if (bUpdateLeftSpread) {
 				if (fCurrentLeft <= fRightOfPrevious + 0.01f)
 					fClickedPointX = fRightOfPrevious + 0.01f;
 			}
 
 		}
 
 		if (iColorMappingPointMoved < markerPoints.size() - 1) {
 			ColorMarkerPoint nextPoint = markerPoints.get(iColorMappingPointMoved + 1);
 			float fLeftOfNext = nextPoint.getValue();
 
 			fLeftOfNext -= nextPoint.getLeftSpread();
 
 			float fCurrentRight = fClickedPointX;
 			if (bUpdateColorPointPosition) {
 				fCurrentRight += markerPoint.getRightSpread();
 				if (fCurrentRight >= fLeftOfNext - 0.01f)
 					fClickedPointX = fLeftOfNext - 0.01f - markerPoint.getRightSpread();
 			}
 			if (bUpdateRightSpread) {
 				if (fCurrentRight >= fLeftOfNext - 0.01f)
 					fClickedPointX = fLeftOfNext - 0.01f;
 			}
 
 		}
 
 		if (bUpdateColorPointPosition) {
 			if (fClickedPointX < 0)
 				fClickedPointX = 0;
 			if (fClickedPointX > 1)
 				fClickedPointX = 1;
 			markerPoint.setValue(fClickedPointX);
 		} else if (bUpdateLeftSpread) {
 			float fTargetValue = markerPoint.getValue() - fClickedPointX;
 			if (fTargetValue < 0.01f)
 				fTargetValue = 0.01f;
 			markerPoint.setLeftSpread(fTargetValue);
 		} else if (bUpdateRightSpread) {
 			float fTargetValue = fClickedPointX - markerPoint.getValue();
 			if (fTargetValue < 0.01f)
 				fTargetValue = 0.01f;
 			markerPoint.setRightSpread(fTargetValue);
 		}
 		colorMapping.update();
 		colorMappingManager.changeColorMapping(colorMapping);
 
 		// RedrawViewEvent event = new RedrawViewEvent();
 		// event.setSender(this);
 		// eventPublisher.triggerEvent(event);
 	}
 
 	@Override
 	public String getDetailedInfo() {
 		return new String("");
 	}
 
 	@Override
 	protected void handlePickingEvents(EPickingType ePickingType,
 			EPickingMode pickingMode, int iExternalID, Pick pick) {
 		if (detailLevel == EDetailLevel.VERY_LOW) {
 			return;
 		}
 		switch (ePickingType) {
 
 		case HISTOGRAM_COLOR_LINE:
 
 			switch (pickingMode) {
 			case CLICKED:
 				bUpdateColorPointPosition = true;
 				bIsFirstTimeUpdateColor = true;
 				iColorMappingPointMoved = iExternalID;
 				break;
 			case MOUSE_OVER:
 
 				break;
 			default:
 				return;
 			}
 			setDisplayListDirty();
 			break;
 		case HISTOGRAM_LEFT_SPREAD_COLOR_LINE:
 			switch (pickingMode) {
 			case CLICKED:
 				bUpdateLeftSpread = true;
 				iColorMappingPointMoved = iExternalID;
 				break;
 			case MOUSE_OVER:
 
 				break;
 			default:
 				return;
 			}
 			setDisplayListDirty();
 			break;
 		case HISTOGRAM_RIGHT_SPREAD_COLOR_LINE:
 			switch (pickingMode) {
 			case CLICKED:
 				bUpdateRightSpread = true;
 				iColorMappingPointMoved = iExternalID;
 				break;
 			case MOUSE_OVER:
 
 				break;
 			default:
 				return;
 			}
 			setDisplayListDirty();
 			break;
 		}
 	}
 
 	@Override
 	public void broadcastElements(EVAOperation type) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public int getNumberOfSelections(SelectionType selectionType) {
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
 
 	@Override
 	public void handleRedrawView() {
 		setDisplayListDirty();
 	}
 
 	@Override
 	public void handleUpdateView() {
 		setDisplayListDirty();
 	}
 
 	@Override
 	public void handleClearSelections() {
 		// nothing to do because histogram has no selections
 	}
 
 	@Override
 	public ASerializedView getSerializableRepresentation() {
 		SerializedHistogramView serializedForm = new SerializedHistogramView(
 				dataDomain.getDataDomainType());
 		serializedForm.setViewID(this.getID());
 		return serializedForm;
 	}
 
 	@Override
 	public void registerEventListeners() {
 		super.registerEventListeners();
 		redrawViewListener = new RedrawViewListener();
 		redrawViewListener.setHandler(this);
 		eventPublisher.addListener(RedrawViewEvent.class, redrawViewListener);
 
 		clearSelectionsListener = new ClearSelectionsListener();
 		clearSelectionsListener.setHandler(this);
 		eventPublisher.addListener(ClearSelectionsEvent.class, clearSelectionsListener);
 	}
 
 	@Override
 	public void unregisterEventListeners() {
 		super.unregisterEventListeners();
 		if (redrawViewListener != null) {
 			eventPublisher.removeListener(redrawViewListener);
 			redrawViewListener = null;
 		}
 		if (clearSelectionsListener != null) {
 			eventPublisher.removeListener(clearSelectionsListener);
 			clearSelectionsListener = null;
 		}
 	}
 
 	@Override
 	public void setSet(ISet set) {
 		this.set = set;
 	}
 
 	@Override
 	public ISetBasedDataDomain getDataDomain() {
 		return dataDomain;
 	}
 
 	@Override
 	public void setDataDomain(ISetBasedDataDomain dataDomain) {
 		this.dataDomain = dataDomain;
 	}
 }
