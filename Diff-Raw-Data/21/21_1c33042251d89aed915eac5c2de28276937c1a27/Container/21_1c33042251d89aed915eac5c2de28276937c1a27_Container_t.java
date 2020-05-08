 package org.eclipse.iee.editor.core.container;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.Position;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 
 public class Container {
 
 	private static final Logger logger = Logger.getLogger(Container.class);
 	
 	private String fPadType;
 	private Map<String, String> fPadParams;
 	private String fValue;
 	private String fTextContent;
 
 	private Position fPosition;
 	private Composite fComposite;
 
 	private ControlListener fCompositeResizeListener;
 
 	private ContainerManager fContainerManager;
 	private DocumentAccess fDocumentAccess;
 	protected IDocument fDocument;
 	private StyledText fStyledText;
 
 	/* Setters */
 
 	public void setContainerID(String containerID) {
 		setPadParam("id", containerID);
 	}
 
 	public void setTextContent(String content) {
 		fTextContent = content;
 		fDocumentAccess.requestAccessAction(DocumentAccess.WRITE, this);
 	}
 	public void setPadParam(String name, String value) {
 		if (fPadParams == null) {
 			fPadParams = new HashMap<String, String>();
 		}
 		fPadParams.put(name, value);
 		fDocumentAccess.requestAccessAction(DocumentAccess.WRITE, this);
 	}
 	
 	public void setPadType(String fPadType) {
 		this.fPadType = fPadType;
 		fDocumentAccess.requestAccessAction(DocumentAccess.WRITE, this);
 	}
 	
 	public void setValue(String fValue) {
 		this.fValue = fValue;
 		fDocumentAccess.requestAccessAction(DocumentAccess.WRITE, this);
 	}
 	
 	public void updateSilently(Map<String, String> params, String value) {
 		fPadParams = params;
 		fValue = value;
 	}
 	
 	/* Getters */
 
 	public Map<String, String> getPadParams() {
 		if (fPadParams == null) {
 			fPadParams = new HashMap<String, String>();
 		}
 		return fPadParams;
 	}
 	
 	public String getContainerID() {
 		return getPadParams().get("id");
 	}
 	
 	public String getPadType() {
 		return fPadType;
 	}
 
 	public String getValue() {
 		return fValue;
 	}
 
 	public String getTextContent() {
 		return fTextContent;
 	}
 
 	public Position getPosition() {
 		return fPosition;
 	}
 
 	public Composite getComposite() {
 		return fComposite;
 	}
 
 	public int getLineNumber() {
 		try {
 			return fDocument.getLineOfOffset(fPosition.offset);
 		} catch (BadLocationException e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 			return -1;
 		}
 	}
 
 	public String getContainerManagerID() {
 		return fContainerManager.getContainerManagerID();
 	}
 	
 	public ContainerManager getContainerManager() {
 		return fContainerManager;
 	}
 		
 	/* FUNCTIONS USED IN PAD MANAGER: */
 
 	/**
 	 * Called when SWT-composite needs to be reloaded.
 	 */
 	public void reset() {
 		this.recreateComposite();
 	}
 
 	/**
 	 * Request for container destruction. Called from PadManager.
 	 */
 	public void destroy() {
 		fDocumentAccess.requestAccessAction(DocumentAccess.RELEASE, this);
 	}
 
 	/* FUNCTION USED IN CONTAINER MANAGER: */
 
 	/**
 	 * Creates new container.
 	 */
 	Container(Position position, String containerID,
 			ContainerManager containerManager) {
 		fPosition = position;
 		setContainerID(containerID);
 
 		fContainerManager = containerManager;
 		fDocumentAccess = containerManager.getDocumentAccess();
 		fDocument = containerManager.getDocument();
 		fStyledText = containerManager.getStyledText();
 
 		fComposite = new Composite(fStyledText, SWT.NONE);
 
 		initListeners();
 	}
 	
 	public Container(Position position, String type, Map<String, String> params, String value,
 			ContainerManager containerManager) {
 		fPosition = position;
 		fPadType = type;		
 		fPadParams = params;
 		fValue = value;
 		fContainerManager = containerManager;
 		fDocumentAccess = containerManager.getDocumentAccess();
 		fDocument = containerManager.getDocument();
 		fStyledText = containerManager.getStyledText();
 		
 		fComposite = new Composite(fStyledText, SWT.NONE);
 
 		initListeners();
 	}
 
 	/**
 	 * Sets container's position.
 	 */
 	void updatePosition(int offset, int length) {
 		fPosition.setOffset(offset);
 		fPosition.setLength(length);
 	}
 
 	/**
 	 * This function causes container's SWT-composite get into proper position.
 	 */
 	boolean updatePresentation() {
 		//logger.debug("Updated container's position");
 
 		Point point = fStyledText.getLocationAtOffset(fPosition.getOffset());
		int height = fStyledText.getLineHeight(fPosition.getOffset());
 		Point gabarit = fComposite.getSize();
		int heightOffset = height - gabarit.y;
		Rectangle newBounds = new Rectangle(point.x + StyledTextManager.PAD_LEFT_MARGIN, point.y + heightOffset, gabarit.x, gabarit.y);
 		if (!fComposite.getBounds().equals(newBounds)) {
 			fComposite.setBounds(newBounds);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Sets container's SWT-composite visibility.
 	 */
 	void setVisible(boolean isVisible) {
 		fComposite.setVisible(isVisible);
 	}
 
 	/**
 	 * Disposes containers's SWT-composite. Called from ContainerManager.
 	 */
 	void dispose() {
 		releaseListeners();
 		fComposite.dispose();
 	}
 
 	/* INTERNAL FUNCTIONS: */
 
 	private void initListeners() {
 		fCompositeResizeListener = new ControlListener() {
 
 			@Override
 			public void controlResized(ControlEvent e) {
 				fContainerManager.updateContainerPresentations(Container.this);
 			}
 
 			@Override
 			public void controlMoved(ControlEvent e) {
 			}
 		};
 		setListeners();
 	}
 
 	private void setListeners() {
 		fComposite.addControlListener(fCompositeResizeListener);
 	}
 
 	private void releaseListeners() {
 		fComposite.removeControlListener(fCompositeResizeListener);
 	}
 
 	private void recreateComposite() {
 		releaseListeners();
 		fComposite.dispose();
 		fComposite = new Composite(fStyledText, SWT.NONE);
 		setListeners();
 	}
 
 	/* Functions for comparator */
 
 	/**
 	 * Creates temporary container with position at @param offset, used for
 	 * comparison
 	 * 
 	 * @return Temporary container at @param offset
 	 */
 	static Container atOffset(int offset) {
 		return new Container(new Position(offset, 0));
 	}
 
 	/**
 	 * Private constructor for temporary containers, used for comparison
 	 */
 	private Container(Position position) {
 		fPosition = position;
 	}
 
 	@Override
 	public String toString() {
 		return "[" + getContainerID() + ", " + fPosition + "]";
 	}
 }
