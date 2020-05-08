 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.text.AbstractDocument;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.CopyOnWriteTextStore;
 import org.eclipse.jface.text.DefaultLineTracker;
 import org.eclipse.jface.text.GapTextStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.serializer.IntentPositionManager;
 import org.eclipse.mylyn.docs.intent.serializer.IntentSerializer;
 import org.eclipse.mylyn.docs.intent.serializer.ParsedElementPosition;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Document representing an IntentElement located on a Repository.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentEditorDocument extends AbstractDocument implements IDocument {
 
 	public static final String MODELING_PREFIX_DECORATION = "\n";
 
 	public static final String MODELING_SUFFIX_DECORATION = MODELING_PREFIX_DECORATION;
 
 	/**
 	 * The ast of this document (recreated for each save on this document).
 	 */
 	private EObject ast;
 
 	/**
 	 * The serializer used to serialized the given Intent elements.
 	 */
 	private IntentSerializer serializer;
 
 	/**
 	 * The editor associated to this document.
 	 */
 	private IntentEditor associatedEditor;
 
 	/**
 	 * IntentDocument constructor.
 	 * 
 	 * @param editor
 	 *            the intent editor
 	 */
 	public IntentEditorDocument(IntentEditor editor) {
 		super();
 		serializer = new IntentSerializer(MODELING_PREFIX_DECORATION, MODELING_SUFFIX_DECORATION);
 		this.associatedEditor = editor;
 		setTextStore(new CopyOnWriteTextStore(new GapTextStore()));
 		setLineTracker(new DefaultLineTracker());
 		super.completeInitialization();
 	}
 
 	/**
 	 * IntentDocument constructor.
 	 * 
 	 * @param root
 	 *            the element to associate to this IntentDocument.
 	 * @param editor
 	 *            the intent editor
 	 */
 	public IntentEditorDocument(EObject root, IntentEditor editor) {
 		super();
 		serializer = new IntentSerializer(MODELING_PREFIX_DECORATION, MODELING_SUFFIX_DECORATION);
 		this.associatedEditor = editor;
 		this.ast = root;
 		setTextStore(new CopyOnWriteTextStore(new GapTextStore()));
 		setLineTracker(new DefaultLineTracker());
 		super.completeInitialization();
 		set(serializer.serialize(root));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.AbstractDocument#set(java.lang.String)
 	 */
 	@Override
 	public void set(String text) {
 		super.set(text);
 	}
 
 	/**
 	 * Returns the ast of this document (recreated for each save on this document).
 	 * 
 	 * @return the ast of this document (recreated for each save on this document)
 	 */
 	public Object getAST() {
 		return this.ast;
 	}
 
 	/**
 	 * Sets the ast to use.
 	 * 
 	 * @param newAST
 	 *            the ast to use
 	 */
 	public void setAST(EObject newAST) {
 		this.ast = newAST;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.AbstractDocument#replace(int, int, java.lang.String)
 	 */
 	@Override
 	public void replace(int pos, int length, String text) throws BadLocationException {
 		// We don't allow the replacement of a decorated line
 		super.replace(pos, length, text);
 	}
 
 	private IntentPositionManager getPositionManager() {
 		return this.serializer.getPositionManager();
 	}
 
 	/**
 	 * Sets this document's serializer ; can be used for providing new positions informations.
 	 * 
 	 * @param serializer
 	 *            the serializer to set
 	 */
 	public void setSerializer(IntentSerializer serializer) {
 		this.serializer = serializer;
 	}
 
 	/**
 	 * Sets the new value of the ast and refresh the document.
 	 */
 	public void reloadFromAST() {
		reloadFromAST(false);
	}
 
	/**
	 * Sets the new value of the ast and refresh the document.
	 * 
	 * @param syncExec
	 *            if true, use sync exec. async if false
	 */
	protected void reloadFromAST(boolean syncExec) {
		Runnable runnable = new Runnable() {
 			public void run() {
 				if (associatedEditor.getSelectionProvider() != null) {
 					ISelection selection = associatedEditor.getSelectionProvider().getSelection();
 					try {
 						String serializedForm = serializer.serialize(ast);
 						if (!get().equals(serializedForm)) {
 							replace(0, getLength(), serializedForm);
 						}
 					} catch (BadLocationException e) {
 						IntentUiLogger.logError("Error encountered while refreshing the document ", e);
 					}
 					associatedEditor.getSelectionProvider().setSelection(selection);
 				}
 			}
		};
		if (syncExec) {
			Display.getDefault().syncExec(runnable);
		} else {
			Display.getDefault().asyncExec(runnable);
		}
 	}
 
 	/**
 	 * Returns the position of the given element (if the document contains it).
 	 * 
 	 * @param element
 	 *            element from which we want the position
 	 * @return the position of the given element if the document contains it, null otherwise
 	 */
 	public ParsedElementPosition getIntentPosition(EObject element) {
 		ParsedElementPosition positionForElement = getPositionManager().getPositionForElement(element);
 		return positionForElement;
 	}
 
 	/**
 	 * Returns the element located at the given position.
 	 * 
 	 * @param offset
 	 *            the current offset
 	 * @return the element located at the given position
 	 */
 	public EObject getElementAtOffset(int offset) {
 		return getPositionManager().getElementAtPosition(offset);
 	}
 
 	/**
 	 * Handle the fact that the content off this document has been deleted by other users.
 	 */
 	public void unsynchronize() {
 		Display.getDefault().asyncExec(new Runnable() {
 
 			public void run() {
 
 				set("The opened elements are out of sync. (have been deleted by another user. )");
 				// TODO : make the editor and the document unsavable and stop the automatic parsing of the AST
 			}
 		});
 	}
 
 	/**
 	 * Returns the intent editor.
 	 * 
 	 * @return the intent editor
 	 */
 	public IntentEditor getIntentEditor() {
 		return associatedEditor;
 	}
 }
