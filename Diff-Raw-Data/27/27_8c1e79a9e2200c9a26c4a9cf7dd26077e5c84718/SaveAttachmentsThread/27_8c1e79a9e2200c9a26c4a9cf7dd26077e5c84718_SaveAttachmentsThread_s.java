 package org.purejava.lnae.threads;
 
 import java.io.File;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import lotus.domino.Database;
 import lotus.domino.Document;
 import lotus.domino.DocumentCollection;
 import lotus.domino.EmbeddedObject;
 import lotus.domino.NotesException;
 import lotus.domino.NotesThread;
 import lotus.domino.RichTextItem;
 import lotus.domino.Session;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.purejava.lnae.swt.App;
 import org.purejava.lnae.swt.ProgressDialog;
 
 public class SaveAttachmentsThread extends Thread {
 
 	private static final String FILE_SEPARATOR = "file.separator";
 
 	// Regex for processing of subject and categories, these
 	// two become the path the attachments are saved into
 	// invalid characters in filenames according to MSDN are / \ * < > ? " : | 
 	// Added "two or more dots" and CRLF as invalid, cause these cause problems in pathnames too
 	private static final String REGEX = "\\.{2,}|\\r\\n|/|\\\\|\\*|<|>|\\?|\"|:|\\|";
 	
 	// regexMild = regex - "\" because backslash is used to distinguish
 	// between directories
 	private static final String REGEX_MILD = "\\.{2,}|\\r\\n|/|\\*|<|>|\\?|\"|:|\\|";
 	
 	// pattern for filename with extension
 	// \1 filename
 	// \2  dot and extension, may be present or not
 	private static final Pattern PATTERN = Pattern.compile("^(.*?)(\\.[\\w\\d]+)?$");
 	
 	private App app;
 	private ProgressDialog progressDialog;
 	private String destination;
 	
 	/**
 	 * Opens an ProgressDialog and runs a task in background
 	 * the task iterates over all documents in the selected 
 	 * database and saves the contained attachments to the
 	 * selected location on the file system
 	 * 
 	 * @author Ralph Plawetzki
 	 * @param app
 	 */
 	public SaveAttachmentsThread(App app) {
 		super();
 		this.app = app;
 	}
 
 	@Override
 	public void run() {
 	
 		app.getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				destination = app.getApplicationGUI().getLabelPath();
 				app.setAborted(false);
 			}
 		});
 		
 		NotesThread.sinitThread();
 		
 		// open PreogressDialog
 		app.getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				progressDialog = new ProgressDialog(app, SWT.CLOSE | SWT.MIN | SWT.APPLICATION_MODAL);
 				progressDialog.open();
 			}
 		});
 		
 		try {
 			
 			Integer currentDoc = 0;
 			Session session = app.getSession();
 			Database db = session.getDatabase(session.getEnvironmentString("MailServer", true), app.getFullPath());
 			DocumentCollection docCollection = db.getAllDocuments();
 			Document document = docCollection.getFirstDocument();
 			
 			outer:while (document != null ) {
 				
 				// show current progress in ProgressDialog
 				currentDoc++;
 				final Integer cD = currentDoc;
 				app.getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						if (! progressDialog.getShell().isDisposed()) progressDialog.setProgress(cD);
 					}
 				});
 				
 				if (document.hasEmbedded()) {
 					String subject = document.getItemValueString("Subject").trim();
 					subject = subject.replaceAll(REGEX, "-");
 					String categories = document.getItemValueString("Categories").trim();
 					categories = categories.replaceAll(REGEX_MILD, "-");
 					String path = destination
 						+ System.getProperty(FILE_SEPARATOR)
 						+ categories
 						+ System.getProperty(FILE_SEPARATOR)
 						+ subject;
 					
 					RichTextItem richTextItem = (RichTextItem) document.getFirstItem("Body");
 					@SuppressWarnings("unchecked")
 					Vector<EmbeddedObject> vector = richTextItem.getEmbeddedObjects();
 					
 					for (EmbeddedObject embeddedObject : vector) {
 						if (embeddedObject.getType() == EmbeddedObject.EMBED_ATTACHMENT) {
 							Matcher matcher = PATTERN.matcher(embeddedObject.getSource());
 							matcher.find();
 							String filename = matcher.group(1);   
 							String extension = matcher.group(2);
 							File p = new File(path);
 							if (!p.exists()) p.mkdirs();
 							String file = path + System.getProperty(FILE_SEPARATOR) + embeddedObject.getSource();
 							int n = 0;
 							if (extension != null) {
 								while (new File(file).exists()) {
 									file = path + System.getProperty(FILE_SEPARATOR) + filename + "_" + ++n + extension;
 								}
 							} else {
 								while (new File(file).exists()) {
 									file = path + System.getProperty(FILE_SEPARATOR) + filename + "_" + ++n;
 								}
 							}
 							embeddedObject.extractFile(file);
 						}
 						embeddedObject.recycle();
 						if (app.isAborted()) break outer;
 					}
 				}
 				document = docCollection.getNextDocument();
 			}
 			docCollection.recycle();
 			db.recycle();
 			
 			// close ProgressDialog
 			app.getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					if (! progressDialog.getShell().isDisposed()) progressDialog.getShell().dispose();
 				}
 			});
 			
 		} catch (final NotesException e) {
			if (! progressDialog.getShell().isDisposed()) progressDialog.getShell().dispose();
 			app.getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					MessageBox mb = new MessageBox(app, SWT.ICON_ERROR | SWT.OK);
 					mb.setText("Lotus Notes Error");
 					mb.setMessage(e.text);
 					mb.open();
 				}
 			});
 		} finally {
 			NotesThread.stermThread();
 		}
 	}
 }
