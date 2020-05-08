 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.text;
 
 import org.wings.event.SDocumentEvent;
 import org.wings.event.SDocumentListener;
 import org.wings.util.EditTranscriptGenerator;
 import org.wings.util.SStringBuilder;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.EventListenerList;
 import javax.swing.text.BadLocationException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author hengels
  */
 public class DefaultDocument implements SDocument {
     private final SStringBuilder buffer = new SStringBuilder();
     private EventListenerList listeners = null;
 
     /**
      * Indicates if we should fire event immediately when they arise,
      * or if we should collect them for a later delivery
      */
     private boolean delayEvents = false;
 
     /**
      * All delayed events are stored here
      */
     protected final ArrayList delayedEvents = new ArrayList(5);
 
     public DefaultDocument() {
     }
 
     public DefaultDocument(String text) {
         buffer.append(text);
     }
 
     public void setText(String text) {
         String origText = buffer.toString();
         if (origText.equals(text)) {
             return;
         }
         buffer.setLength(0);
         if (text != null){
             buffer.append(text);
            if((listeners == null) || (listeners.getListenerCount() > 0)){
                // If there are any document listeners: Generate document change events!
            	List actions = EditTranscriptGenerator.generateEvents(origText, text);

                // and fire them!
                fireChangeUpdate(0, buffer.length());
            	for(int i = 0; i < actions.size(); i++){
            		DocumentEvent de = (DocumentEvent) actions.get(i);
            		if(de.getType().equals(DocumentEvent.EventType.INSERT)){
            			fireInsertUpdate(de.getOffset(), de.getLength());
            		} else if(de.getType().equals(DocumentEvent.EventType.REMOVE)){
            			fireRemoveUpdate(de.getOffset(), de.getLength());
            		}
            	}
             }
         }
     }
 
     public String getText() {
         return buffer.length() == 0 ? null : buffer.toString();
     }
 
     public String getText(int offset, int length) throws BadLocationException {
         try {
             return buffer.substring(offset, length);
         } catch (IndexOutOfBoundsException e) {
             throw new BadLocationException(e.getMessage(), offset);
         }
     }
 
     public int getLength() {
         return buffer.length();
     }
 
     public void remove(int offset, int length) throws BadLocationException {
         if (length == 0) {
             return;
         }
         try {
             buffer.delete(offset, offset + length);
             fireRemoveUpdate(offset, length);
         } catch (IndexOutOfBoundsException e) {
             throw new BadLocationException(e.getMessage(), offset);
         }
     }
 
     public void insert(int offset, String string) throws BadLocationException {
         if (string == null || string.length() == 0) {
             return;
         }
         try {
             buffer.insert(offset, string);
             fireInsertUpdate(offset, string.length());
         } catch (IndexOutOfBoundsException e) {
             throw new BadLocationException(e.getMessage(), offset);
         }
     }
 
     public SDocumentListener[] getDocumentListeners() {
     	if (listeners != null) {
             return (SDocumentListener[]) listeners.getListeners(SDocumentListener.class);
         } else {
             return (SDocumentListener[]) Array.newInstance(SDocumentListener.class, 0);
         }
 	}
 
 	public void addDocumentListener(SDocumentListener listener) {
         if (listeners == null)
             listeners = new EventListenerList();
         listeners.add(SDocumentListener.class, listener);
     }
 
     public void removeDocumentListener(SDocumentListener listener) {
         if (listeners == null)
             return;
         listeners.remove(SDocumentListener.class, listener);
     }
 
     protected void fireInsertUpdate(int offset, int length) {
     	SDocumentEvent e = new SDocumentEvent(this, offset, length, SDocumentEvent.INSERT);
 
 		if (delayEvents) {
 			delayedEvents.add(e);
 		} else {
 			if (listeners == null || listeners.getListenerCount() == 0)
 				return;
 
 			Object[] listeners = this.listeners.getListenerList();
 			for (int i = listeners.length - 2; i >= 0; i -= 2) {
 				((SDocumentListener) listeners[i + 1]).insertUpdate(e);
 			}
 		}
     }
 
     protected void fireRemoveUpdate(int offset, int length) {
     	SDocumentEvent e = new SDocumentEvent(this, offset, length, SDocumentEvent.REMOVE);
 
 		if (delayEvents) {
 			delayedEvents.add(e);
 		} else {
 			if (listeners == null || listeners.getListenerCount() == 0)
 				return;
 
 			Object[] listeners = this.listeners.getListenerList();
 			for (int i = listeners.length - 2; i >= 0; i -= 2) {
 				((SDocumentListener) listeners[i + 1]).removeUpdate(e);
 			}
 		}
     }
 
     protected void fireChangeUpdate(int offset, int length) {
     	SDocumentEvent e = new SDocumentEvent(this, offset, length, SDocumentEvent.CHANGE);
 
     	if (delayEvents) {
     		delayedEvents.add(e);
 		} else {
 			if (listeners == null || listeners.getListenerCount() == 0)
 				return;
 
 			Object[] listeners = this.listeners.getListenerList();
 			for (int i = listeners.length - 2; i >= 0; i -= 2) {
 				((SDocumentListener) listeners[i + 1]).changedUpdate(e);
 			}
 		}
     }
 
 	public boolean getDelayEvents() {
 		return delayEvents;
 	}
 
 	public void setDelayEvents(boolean b) {
 		delayEvents = b;
 	}
 
 	public void fireDelayedIntermediateEvents() {
 		for (Iterator iter = delayedEvents.iterator(); iter.hasNext();) {
 			SDocumentEvent e = (SDocumentEvent) iter.next();
 
 			switch (e.getType()) {
 			case SDocumentEvent.INSERT:
 				fireInsertUpdate(e.getOffset(), e.getLength());
 			case SDocumentEvent.REMOVE:
 				fireRemoveUpdate(e.getOffset(), e.getLength());
 			case SDocumentEvent.CHANGE:
 				fireChangeUpdate(e.getOffset(), e.getLength());
 			}
 		}
 		delayedEvents.clear();
 	}
 
 	public void fireDelayedFinalEvents() {
 
 	}
 }
