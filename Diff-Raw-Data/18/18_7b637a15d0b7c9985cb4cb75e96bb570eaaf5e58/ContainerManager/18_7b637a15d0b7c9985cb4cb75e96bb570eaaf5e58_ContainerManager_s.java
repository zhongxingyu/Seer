 package org.eclipse.iee.editor.core.container;
 
 import java.util.Iterator;
 import java.util.NavigableSet;
 import java.util.TreeSet;
 import java.util.UUID;
 
 import org.eclipse.core.commands.common.EventManager;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPartitioningException;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.DocumentPartitioningChangedEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension3;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.IDocumentPartitioningListener;
 import org.eclipse.jface.text.IDocumentPartitioningListenerExtension2;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.rules.FastPartitioner;
 import org.eclipse.swt.custom.CaretEvent;
 import org.eclipse.swt.custom.CaretListener;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 
 public class ContainerManager extends EventManager {
 
 	private String fContainerManagerID;
 	
 	private final StyledText fStyledText;
     private final IDocument fDocument;
     private final IDocumentPartitioner fDocumentPartitioner;
 
     private final NavigableSet<Container> fContainers;
     private final ContainerComparator fContainerComparator;
 
 
     /* Public interface */
 
     public Object[] getElements() {
         return fContainers.toArray();
     }
     
     public String[] getContainerIDs() {
     	String[] containerIDs = new String[fContainers.size()];
     	int i = 0;
     	for (Container container : fContainers) {
     		containerIDs[i++] = container.getContainerID();
     	}
     	return containerIDs;
     }
             
     public void RequestContainerAllocation(String containerID, int offset) {
     	String containerEmbeddedRegion =
     		Container.getInitialTextRegion(containerID);
     	
     	try {
 			fDocument.replace(offset, 0, containerEmbeddedRegion);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     public IDocument getDocument() {
     	return fDocument;
    }  
     
     /* Functions for observers */
 
     public void addContainerManagerListener(IContainerManagerListener listener) {
         Assert.isNotNull(listener);
         addListenerObject(listener);
     }
 
     
     public void removeContainerManagerListener(IContainerManagerListener listener) {
         Assert.isNotNull(listener);
         removeListenerObject(listener);
     }
     
     protected void fireContainerCreated(ContainerManagerEvent event) {
         Object[] listeners = getListeners();
         for (int i = 0; i < listeners.length; i++) {
         	((IContainerManagerListener) listeners[i]).containerCreated(event);	
         }
     }
 
     protected void fireContainerRemoved(ContainerManagerEvent event) {
         Object[] listeners = getListeners();
         for (int i = 0; i < listeners.length; i++) {
         	((IContainerManagerListener) listeners[i]).containerRemoved(event);	
         }
     }
     
     protected void fireDebugNotification(ContainerManagerEvent event) {
         Object[] listeners = getListeners();
         for (int i = 0; i < listeners.length; i++) {
             ((IContainerManagerListener) listeners[i]).debugNotification(event);
         }
     }
 
     
     /* Constructor */
     
     public ContainerManager(IDocument document, StyledText styledText) {
     	fContainerManagerID = UUID.randomUUID().toString();
     	
     	fStyledText = styledText;
     	
         fContainerComparator = new ContainerComparator();
     	fContainers = new TreeSet<Container>(fContainerComparator);
         fDocument = document;
 
         fDocumentPartitioner = new FastPartitioner(
             new PartitioningScanner(),
             new String[] { IConfiguration.CONTENT_TYPE_EMBEDDED });
 
         ((IDocumentExtension3) fDocument).setDocumentPartitioner(
             IConfiguration.PARTITIONING_ID, fDocumentPartitioner);
 
         initDocumentListener();
         fDocumentPartitioner.connect(fDocument);
     }
     
     public String getContainerManagerID() {
     	return fContainerManagerID;
     }
         
     
     /* Presentation update */
     
     void updateContainerPresentaions() {
     	Iterator<Container> it = fContainers.iterator();
     	while (it.hasNext()) {
     	    Container container = it.next();
     	    container.setVisiable(true);
     	    container.updatePresentation();
     	}
     }
 
     
     /* Document modification event processing */
     
     protected void initDocumentListener() {  
    	
     	fStyledText.addVerifyListener(new VerifyListener() {
 			@Override
 			public void verifyText(VerifyEvent e) {		
             	/* Disallow modification within Container's text region */
 				if (getContainerHavingOffset(e.start) != null ||
             			getContainerHavingOffset(e.end) != null) {
 					e.doit = false;
 					return;
             	}
 				
 		    	/* Setting all Containers to invisible state */
 				Iterator<Container> it = fContainers.iterator();
 				while (it.hasNext()) {
 					Container c = it.next();
 					c.setVisiable(false);
 				}
 			}
         });
     	
     	
     	/*
     	 * If caret is inside Container's text region, moving it to the beginning of line
     	 */    	
     	fStyledText.addCaretListener(new CaretListener() {
 			@Override
 			public void caretMoved(CaretEvent e) {
 				//int begin = getBeginOfContainerRegion(e.caretOffset);
 				//if (begin != -1) {
 				//	fStyledText.setCaretOffset(begin);
 				//}
 			}    		
     	});
     	
     	
     	class DocumentListener implements
         IDocumentListener, IDocumentPartitioningListener, IDocumentPartitioningListenerExtension2
         {
             private IRegion fChangedPartitioningRegion;
 
             
             public DocumentListener() {
             	fChangedPartitioningRegion = null;
             }
             
             
             @Override
             public void documentPartitioningChanged(DocumentPartitioningChangedEvent event) {
             	fChangedPartitioningRegion = event.getChangedRegion(IConfiguration.PARTITIONING_ID);
             }
    
             
             @Override
             public void documentChanged(DocumentEvent event) {
 
             	/* All pads which placed after 'unmodifiedOffset'
             	 * are considered to be just moved without any other modifications.
             	 *
             	 * It's calculated according following equation
             	 * 'unmodified offset' = max('end of partitioning changed area', 'end of document changed area') - 'moving_delta'
             	 */
 
             	int unmodifiedOffset;
             	final int movingDelta = event.getText().length() - event.getLength();
 
             	if (fChangedPartitioningRegion != null) {
             		unmodifiedOffset = Math.max(
             			event.getOffset() + event.getText().length(),
             			fChangedPartitioningRegion.getOffset() + fChangedPartitioningRegion.getLength());
             		unmodifiedOffset -= movingDelta;
             	} else {
             		unmodifiedOffset = event.getOffset() + event.getLength();
             	}
 
                 /* Positive delta means that unmodified pads move forward.
                  * We have to perform this action before any other modifications to avoid collisions.
                  */
 
                 if (movingDelta > 0) {
                 	moveUnmodifiedPads(unmodifiedOffset, movingDelta);
                 }
 
             	try {
                     if (fChangedPartitioningRegion != null) {
 
                         /* Case 1:
                          * Document partitioning is changed, so updating the set of the pads
                          */
                         onPartitioningChanged(event, unmodifiedOffset);
 
                     } else {
                     	Container current = getContainerHavingOffset(event.getOffset());
                         if (current != null) {
 
                         	/* Case 2:
                              * Changed text area is inside current pad's area, updating it
                              */
                         	onChangesInsidePad(current, event);
                         }
 
                         /* Case 3:
                          * No pad modified, do nothing.
                          */
                     }
                 } catch (BadLocationException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 } catch (BadPartitioningException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 } catch (RuntimeException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
 
                 /* If delta is negative, we move unmodified pads backward,
                  * but after any other modifications are done.
                  */
 
                 if (movingDelta < 0) {
                 	moveUnmodifiedPads(unmodifiedOffset, movingDelta);
                 }
                 
                 fChangedPartitioningRegion = null;
                 
                System.out.println("Iteration");
                 
             	Container.processNextDocumentAccessRequest(fDocument);
                updateContainerPresentaions();
             	     	
                 /* For debug */
                 
                 fireDebugNotification(new ContainerManagerEvent(null, fContainerManagerID));
             }
 
             
             private void onPartitioningChanged(DocumentEvent event, int unmodifiedOffset) throws BadLocationException, BadPartitioningException {
 
             	/* Remove all elements within changed area */
 
                 int beginRegionOffset = Math.min(event.getOffset(), fChangedPartitioningRegion.getOffset());
 
                 Container from = fContainers.ceiling(Container.atOffset(beginRegionOffset));
                 Container	to = fContainers.lower(Container.atOffset(unmodifiedOffset));
 
                 if (from != null && to != null && fContainerComparator.isNotDescending(from, to)) {
                 	NavigableSet<Container> removeSet = fContainers.subSet(from, true, to, true);
                 	if (removeSet != null) {
                 		Container container;
                 		while ((container = removeSet.pollFirst()) != null) {
                 			
                 			// XXX remove container
                 			
                 			container.dispose();
                 			fireContainerRemoved(new ContainerManagerEvent(container, fContainerManagerID));
                 		}
                 	}
                 }
 
                 /* Scanning for new containers */
 
                int offset = beginRegionOffset;
                 while (offset < fChangedPartitioningRegion.getOffset() + fChangedPartitioningRegion.getLength()) {
                     ITypedRegion region = ((IDocumentExtension3) fDocument)
                         .getPartition(IConfiguration.PARTITIONING_ID, offset, false);
 
                     if (region.getType().equals(IConfiguration.CONTENT_TYPE_EMBEDDED)) {
                     	String containerTextRegion = fDocument.get(region.getOffset(), region.getLength());                   	
                         
                     	// XXX add container                    	                    	                    	
                         
                     	String containerID =
                     			Container.getContainerIDFromTextRegion(containerTextRegion);                    	
                     	
                     	Container container = createContainer(
                         	new Position(region.getOffset(), region.getLength()),
                         	containerID);
                         
                        fContainers.add(container);                        
                         fireContainerCreated(new ContainerManagerEvent(container, fContainerManagerID));
                     }
                     offset += region.getLength();
                 }
             }
 
             
             private void onChangesInsidePad(Container container, DocumentEvent event) throws BadLocationException, BadPartitioningException {
             	ITypedRegion region = ((IDocumentExtension3) fDocument)
                 	.getPartition(IConfiguration.PARTITIONING_ID, event.getOffset(), false);
 
                 Assert.isTrue(container.getPosition().getOffset() == region.getOffset());
                 
                 // XXX update container
                 container.updatePosition(region.getOffset(), region.getLength());
             }
 
             
             private void moveUnmodifiedPads(int offset, int delta) {
             	Container from = fContainers.ceiling(Container.atOffset(offset));
             	if (from == null)
                     return;
 
                 NavigableSet<Container> tail = fContainers.tailSet(from, true);
                 Iterator<Container> it = tail.iterator();
                 while (it.hasNext()) {
                     Container container = it.next();
                     Position position = container.getPosition();
                     
                     // XXX update container
                     container.updatePosition(
                     	position.getOffset() + delta,
                     	position.getLength());
                 }
             }
             
 
             @Override public void documentPartitioningChanged(IDocument document) {}
             @Override public void documentAboutToBeChanged(DocumentEvent event) {}
         }
 
         DocumentListener listener = new DocumentListener();
         fDocument.addDocumentPartitioningListener(listener);
         fDocument.addDocumentListener(listener);
     }
     
     
     protected Container getContainerHavingOffset(int offset) {
     	Container c = fContainers.lower(Container.atOffset(offset));
     	if (c != null && c.getPosition().includes(offset)) {
     		return c;
     	}
     	return null;
     }
     
     protected int getBeginOfContainerRegion(int offset) {
 		try {	    	
 	    	Container c = fContainers.lower(Container.atOffset(offset));
 			int	lineNumber = fDocument.getLineOfOffset(offset);
 	
 	    	if (c != null && fDocument.getLineOfOffset(c.getPosition().getOffset()) == lineNumber) {
 	    		return c.getPosition().getOffset();
 	    	}
 	    	
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
     	return -1;
     }
     
     protected Container createContainer(Position position, String containerID) {
     	return new Container(
             position, containerID, fStyledText, this);
     }
 }
