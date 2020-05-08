 package com.cahoots.eclipse.collab.share;
 
 import javax.inject.Inject;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 import com.cahoots.connection.CahootsConnection;
 import com.cahoots.connection.websocket.CahootsSocket;
 import com.cahoots.eclipse.event.EventRegistrar;
 import com.cahoots.eclipse.indigo.editor.ResourceFinder;
 import com.cahoots.eclipse.indigo.log.Log;
 import com.cahoots.eclipse.indigo.widget.MessageDialog;
 import com.cahoots.eclipse.indigo.widget.MessageDialogStatus;
 import com.cahoots.eclipse.indigo.widget.TextEditorTools;
 import com.cahoots.eclipse.op.OpDocument;
 import com.cahoots.eclipse.swt.SwtDisplayUtils;
 import com.cahoots.events.OpDeleteEventListener;
 import com.cahoots.events.OpInsertEventListener;
 import com.cahoots.events.OpReplaceEventListener;
 import com.cahoots.events.ShareDocumentEventListener;
 import com.cahoots.json.receive.OpDeleteMessage;
 import com.cahoots.json.receive.OpInsertMessage;
 import com.cahoots.json.receive.OpReplaceMessage;
 import com.cahoots.json.receive.ShareDocumentMessage;
 
 /**
  * Clean up this piece of fecal matter with giant set of explosives
  * 
  * @author Hekar
  * 
  */
 public class ShareDocumentRegistrar implements EventRegistrar {
 
 	private static Log logger = Log.getLogger(ShareDocumentRegistrar.class);
 
 	private final ShareDocumentSessionManager incomingShareDocumentManager;
 	private final ShareDocumentManager shareDocumentManager;
 	private final CahootsSocket cahootsSocket;
 	private final TextEditorTools editorTools;
 	private final ResourceFinder resourceFinder;
 	private final IWorkbenchWindow workbenchWindow;
 	private final IEditorRegistry editorRegistry;
 	private final CahootsConnection cahootsConnection;
 	private final MessageDialog messageDialog;
 
 	@Inject
 	public ShareDocumentRegistrar(
 			final ShareDocumentSessionManager shareDocumentSessionManager,
 			final MessageDialog messageDialog,
 			final CahootsConnection cahootsConnection,
 			final IWorkbenchWindow workbenchWindow,
 			final IEditorRegistry editorRegistry,
 			final ResourceFinder resourceFinder,
 			final ShareDocumentManager shareDocumentManager,
 			final CahootsSocket cahootsSocket, final TextEditorTools editorTools) {
 		this.incomingShareDocumentManager = shareDocumentSessionManager;
 		this.messageDialog = messageDialog;
 		this.cahootsConnection = cahootsConnection;
 		this.workbenchWindow = workbenchWindow;
 		this.editorRegistry = editorRegistry;
 		this.resourceFinder = resourceFinder;
 		this.shareDocumentManager = shareDocumentManager;
 		this.cahootsSocket = cahootsSocket;
 		this.editorTools = editorTools;
 	}
 
 	@Override
 	public void registerEvents() {
 		setupDefaultNotifications();
 	}
 
 	private void setupDefaultNotifications() {
 		final ShareDocumentEventListener shareDocumentEventListener = createShareDocumentEventListener();
 		cahootsSocket.addShareDocumentEventListener(shareDocumentEventListener);
 	}
 
 	private ShareDocumentEventListener createShareDocumentEventListener() {
 		final ShareDocumentEventListener incomingShareDocumentEventListener = new ShareDocumentEventListener() {
 			@Override
 			public void onEvent(final ShareDocumentMessage msg) {
 				try {
 					final String documentId = msg.getDocumentId();
 					final String sharer = msg.getSharer();
 					final String opId = msg.getOpId();
 					System.out.println(cahootsConnection.getUsername());
					// final boolean sameUser =
					// sharer.equals(cahootsConnection
 					// .getUsername());
 					// if (sameUser) {
 					// return;
 					// }
 
 					final Runnable runnable = new Runnable() {
						@Override
 						public void run() {
							// Don't prompt if we started the collaboration
							if (!sharer.equals(cahootsConnection.getUsername())) {
								final OpDocument document = new OpDocument(
										opId, documentId);
								final String inviteMessage = String
										.format("%s is requesting to share document %s.",
												sharer, document.getFilename());
								final MessageDialogStatus prompt = messageDialog
										.prompt(workbenchWindow.getShell(),
												"Accept Invite", inviteMessage);
								if (prompt != MessageDialogStatus.OK) {
									return;
								}
 							}
 							final ITextEditor textEditor = getSharedDocumentTextEditor(documentId);
 							setupNotifications(textEditor);
 
 							final IDocument doc = textEditor
 									.getDocumentProvider().getDocument(
 											textEditor.getEditorInput());
 							shareDocumentManager.addDocumentListener(doc, opId,
 									documentId);
 						}
 					};
 
 					SwtDisplayUtils.sync(runnable);
 				} catch (final Exception e) {
 					e.printStackTrace();
 					logger.error(e);
 				}
 			}
 		};
 
 		return incomingShareDocumentEventListener;
 	}
 
 	private ITextEditor getSharedDocumentTextEditor(final String documentId) {
 		final IFile file = resourceFinder.findFileByDocumentId(documentId);
 		final IEditorPart editorPart = resourceFinder.findEditorByFile(file);
 
 		final boolean editorIsAlreadyOpen = (editorPart != null);
 		if (!editorIsAlreadyOpen) {
 			final IEditorDescriptor defaultEditor = editorRegistry
 					.getDefaultEditor(file.getFullPath().toString());
 			final IEditorInput editorInput = new FileEditorInput(file);
 
 			// Open new editor
 			final IWorkbenchPage activePage = workbenchWindow.getActivePage();
 			final IEditorPart editor = editorTools.openEditor(activePage,
 					editorInput, defaultEditor.getId());
 			if (editor == null) {
 				throw new IllegalStateException("Failure to open editor for "
 						+ documentId);
 			}
 
 			final ITextEditor textEditor = editorTools
 					.editorPartToTextEditor(editor);
 			return textEditor;
 		} else {
 			final IEditorInput editorInput = editorPart.getEditorInput();
 			if (editorInput == null) {
 				throw new IllegalStateException(
 						"Failure to get editor input for documentId: "
 								+ documentId);
 			}
 
 			// Open existing editor
 			final IWorkbenchPage activePage = workbenchWindow.getActivePage();
 			// TODO: Make sure this editor has a correct id
 			final String editorId = editorPart.getSite().getId();
 			System.out.println("Editor ID: " + editorId);
 			final IEditorPart editor = editorTools.openEditor(activePage,
 					editorInput, editorId);
 			if (editor == null) {
 				throw new IllegalStateException("Failure to find editor for "
 						+ documentId);
 			}
 
 			final ITextEditor textEditor = editorTools
 					.editorPartToTextEditor(editor);
 			return textEditor;
 		}
 	}
 
 	private void setupNotifications(final ITextEditor textEditor) {
 		if (textEditor == null) {
 			throw new IllegalArgumentException("textEditor cannot be null");
 		}
 
 		final OpInsertEventListener insert = new OpInsertEventListener() {
 			@Override
 			public void onEvent(final OpInsertMessage msg) {
 				final Runnable runnable = new Runnable() {
 					@Override
 					public void run() {
 						try {
 							if (msg.getUser().equals(
 									cahootsConnection.getUsername())) {
 								return;
 							}
 
 							final int start = msg.getStart();
 							final String contents = msg.getContent();
 							final Long tickStamp = msg.getTickStamp();
 
 							final IDocumentProvider documentProvider = textEditor
 									.getDocumentProvider();
 							final IDocument document = documentProvider
 									.getDocument(textEditor.getEditorInput());
 
 							shareDocumentManager.disableEvents();
 							document.replace(start, 0, contents);
 							shareDocumentManager.enableEvents();
 
 						} catch (final BadLocationException e) {
 							e.printStackTrace();
 						}
 					}
 				};
 
 				SwtDisplayUtils.async(runnable);
 			}
 		};
 
 		final OpReplaceEventListener replace = new OpReplaceEventListener() {
 			@Override
 			public void onEvent(final OpReplaceMessage msg) {
 				final Runnable runnable = new Runnable() {
 					@Override
 					public void run() {
 						try {
 							if (msg.getUser().equals(
 									cahootsConnection.getUsername())) {
 								return;
 							}
 
 							final int start = msg.getStart();
 							final String contents = msg.getContent();
 							final int length = msg.getEnd() - msg.getStart();
 
 							if (length == 0) {
 								throw new IllegalStateException(
 										"Length of replace message cannot be 0");
 							} else if (length < 0) {
 								throw new IllegalStateException(
 										"Length of replace message cannot be less than 0");
 							}
 
 							final Long tickStamp = msg.getTickStamp();
 
 							final IDocumentProvider documentProvider = textEditor
 									.getDocumentProvider();
 							final IDocument document = documentProvider
 									.getDocument(textEditor.getEditorInput());
 
 							shareDocumentManager.disableEvents();
 							document.replace(start, length, contents);
 							shareDocumentManager.enableEvents();
 
 						} catch (final BadLocationException e) {
 							e.printStackTrace();
 						}
 					}
 				};
 
 				SwtDisplayUtils.async(runnable);
 			}
 		};
 
 		final OpDeleteEventListener delete = new OpDeleteEventListener() {
 			@Override
 			public void onEvent(final OpDeleteMessage msg) {
 				final Runnable runnable = new Runnable() {
 					@Override
 					public void run() {
 						try {
 							if (msg.getUser().equals(
 									cahootsConnection.getUsername())) {
 								return;
 							}
 
 							final int start = msg.getStart();
 							final int length = msg.getEnd() - msg.getStart();
 							final Long tickStamp = msg.getTickStamp();
 
 							final IDocumentProvider documentProvider = textEditor
 									.getDocumentProvider();
 							final IDocument document = documentProvider
 									.getDocument(textEditor.getEditorInput());
 
 							shareDocumentManager.disableEvents();
 							document.replace(start, length, "");
 							shareDocumentManager.enableEvents();
 
 						} catch (final BadLocationException e) {
 							e.printStackTrace();
 						}
 					}
 				};
 
 				SwtDisplayUtils.async(runnable);
 			}
 		};
 
 		cahootsSocket.addOpInsertEventListener(insert);
 		cahootsSocket.addOpReplaceEventListener(replace);
 		cahootsSocket.addOpDeleteEventListener(delete);
 	}
 
 }
