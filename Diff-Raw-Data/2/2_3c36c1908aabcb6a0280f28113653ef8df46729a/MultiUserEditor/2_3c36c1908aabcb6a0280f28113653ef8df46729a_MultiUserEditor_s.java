 package org.vaadin.mideaas.frontend;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.vaadin.aceeditor.AceEditor;
 import org.vaadin.aceeditor.AceEditor.DiffEvent;
 import org.vaadin.aceeditor.AceEditor.DiffListener;
 import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
 import org.vaadin.aceeditor.AceMode;
 import org.vaadin.aceeditor.AceTheme;
 import org.vaadin.aceeditor.SuggestionExtension;
 import org.vaadin.aceeditor.TextRange;
 import org.vaadin.aceeditor.client.AceAnnotation;
 import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
 import org.vaadin.aceeditor.client.AceDoc;
 import org.vaadin.aceeditor.client.AceMarker;
 import org.vaadin.aceeditor.client.AceRange;
 import org.vaadin.mideaas.model.AsyncErrorChecker;
 import org.vaadin.mideaas.model.AsyncErrorChecker.ResultListener;
 import org.vaadin.mideaas.model.ErrorChecker.Error;
 import org.vaadin.mideaas.model.MultiUserDoc;
 import org.vaadin.mideaas.model.MultiUserDoc.DifferingUsersChangedListener;
 import org.vaadin.mideaas.model.User;
 import org.vaadin.mideaas.model.UserDoc;
 
 import com.vaadin.annotations.StyleSheet;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 @StyleSheet("ace-markers.css")
 @SuppressWarnings("serial")
 public class MultiUserEditor extends CustomComponent implements DiffListener, DifferingUsersChangedListener, ResultListener, UserDoc.Listener {
 	
 	public enum DocType {
 		BASE,
 		MINE,
 		OTHERS
 	}
 
 	private final User user;
 	private final MultiUserDoc mud;
 	private final AceEditor editor;
 	private Label statusLabel = new Label();
 	private UserDoc activeDoc;
 	private UI ui;
 	
 	private final HorizontalLayout hBar = new HorizontalLayout();
 	private HorizontalOptionGroup group;
 	private Object currentEditorType;
 	private AsyncErrorChecker checker;
 	
 	private Button useThisButton = new Button("Use this code");
 	
 	public MultiUserEditor(User user, MultiUserDoc mud) {
 		super();
 		this.user = user;
 		this.mud = mud;
 		editor = new AceEditor();
 		
 		// Getting the ace javascript files from the same server the app is running on.
 		// We assume they are available at this location.
 		editor.setThemePath("/mideaas/static/ace");
 		editor.setModePath("/mideaas/static/ace");
 		editor.setWorkerPath("/mideaas/static/ace"); 
 		
 		editor.setSizeFull();
 		
 		
 		
 		VerticalLayout layout = new VerticalLayout();
 		layout.setSizeFull();
 		layout.addComponent(hBar);
 		layout.addComponent(editor);
 		layout.setExpandRatio(editor, 1);
 		setCompositionRoot(layout);
 	}
 	
 	public void setErrorChecker(AsyncErrorChecker checker) {
 		this.checker = checker;
 	}
 	
 	public void setMode(AceMode mode) {
 		editor.setMode(mode);
 	}
 	
 	public void setTheme(AceTheme theme) {
 		editor.setTheme(theme);
 	}
 	
 	public void setSuggestionExtension(SuggestionExtension se) {
 		se.extend(editor);
 	}
 	
 	@Override
 	public void attach() {
 		super.attach();
 		ui = UI.getCurrent();
 		
 		group = new HorizontalOptionGroup();
 		
 		group.addValueChangeListener(new ValueChangeListener() {
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				Object val = event.getProperty().getValue();
 				if (val!=null) { // XXX ???
 					changeEditor(val);
 				}
 			}
 		});
 		group.setImmediate(true);
 		
 		statusLabel.setIcon(Icons.TICK_CIRCLE);
 		hBar.addComponent(statusLabel);
 		hBar.addComponent(group);
 		
 		hBar.addComponent(useThisButton);
 		useThisButton.addClickListener(new ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				useCode();
 			}
 		});
 		
 		editor.addDiffListener(this);
 		
 		setDifferingUsers(mud.getDifferingUsers());
 		mud.addDifferingUsersChangedListener(this);
 		
 		changeEditor(user);
 		
 		// Should we always check errors on attach?
 		checkErrors();
 
 	}
 	
 	private void useCode() {
 		mud.getUserDoc(user).setDoc(editor.getDoc());
 		changeEditor(user);
 	}
 
 	private void changeEditor(Object object) {
 		if (currentEditorType==object) {
 			return;
 		}
 		
 		if (object instanceof User) {
 			User u = (User)object;
 			setActiveDoc(u);
 			editor.setReadOnly(u!=user);
 		}
 		else if (object == DocType.BASE) {
 			setActiveDoc(mud.getBase());
 			editor.setReadOnly(true);
 		}
 		
 		currentEditorType = object;
 		group.select(currentEditorType);
 		updateUseThisButton();
 	}
 
 	@Override
 	public void detach() {
 		
 		if (activeDoc!=null) {
 			activeDoc.removeListener(this);
 		}
 		
 		mud.removeDifferingUsersChangedListener(this);
 		
 		super.detach();
 	}
 	
 	private void setActiveDoc(User u) {
 		if (activeDoc!=null) {
 			activeDoc.removeListener(this);
 		}
 		UserDoc ud = mud.createUserDoc(u);
 		activeDoc = ud;
 		activeDoc.addListener(this);
 		editor.setReadOnly(false);
 		editor.setDoc(activeDoc.getDoc());
 	}
 	
 	private void setActiveDoc(AceDoc doc) {
 		if (activeDoc!=null) {
 			activeDoc.removeListener(this);
 		}
 		activeDoc = null;
 		editor.setReadOnly(false);
 		editor.setDoc(doc);
 	}
 
 	@Override
 	public void changed(final AceDoc doc, ChangeType type) {
 //		System.out.println("\nchanged "+user.getName()+" - " + type + "\n" + doc.getText());
 		ui.access(new Runnable() {
 			@Override
 			public void run() {
 				setEditorDoc(doc);
 			}
 		});
 		if (checker!=null) {
 			checker.checkErrors(doc.getText(), this);
 		}
 	}
 	
 	private void checkErrors() {
 		if (checker!=null) {
 			UserDoc doc = mud.getUserDoc(user);
 			if (doc!=null) {
 				String code = doc.getDoc().getText();
 				checker.checkErrors(code, this);
 			}
 		}
 	}
 	
 	private void setEditorDoc(AceDoc doc) {
 		boolean wasReadOnly = editor.isReadOnly();
 		editor.setReadOnly(false);
 		editor.setDoc(doc);
 		editor.setReadOnly(wasReadOnly);
 	}
 	
 	@Override
 	public void errorsChecked(final List<Error> errors) {
 		ui.access(new Runnable() {
 			@Override
 			public void run() {
 				setEditorDoc(docWithErrors(getDoc(), errors));
 			}
 		});
 	}
 
 	private AceDoc docWithErrors(AceDoc doc, List<Error> errors) {
 		HashMap<String, AceMarker> markers = new HashMap<String, AceMarker>(errors.size());
 		HashSet<MarkerAnnotation> manns = new HashSet<MarkerAnnotation>(errors.size());
 		for (Error err : errors) {
 			AceMarker m = markerFromError(newMarkerId(), err, doc.getText());
 			markers.put(m.getMarkerId(), m);
 			AceAnnotation ann = new AceAnnotation(err.message, AceAnnotation.Type.error);
 			manns.add(new MarkerAnnotation(m.getMarkerId(), ann));
 		}
 		return doc.withMarkers(markers).withMarkerAnnotations(manns);		
 	}
 	
 	private long latestMarkerId = 0L;
 	private String newMarkerId() {
 		// TODO ?
 		return "error" + this.hashCode() + (++latestMarkerId);
 	}
 	
 	private static AceMarker markerFromError(String markerId, Error e, String text) {
 		AceRange range = new TextRange(text, e.start, e.start==e.end ? e.start+1 : e.end);
 		String cssClass = "myerrormarker1";
 		AceMarker.Type type = AceMarker.Type.text;
 		boolean inFront = true;
 		AceMarker.OnTextChange onChange = AceMarker.OnTextChange.ADJUST;
 		return new AceMarker(markerId, range, cssClass, type, inFront, onChange);
 	}
 
 	@Override
 	public void diff(DiffEvent e) {
		activeDoc.editorChanged(editor.getDoc());
 	}
 
 	@Override
 	public void differingUsersChanged(final Set<User> users) {
 		ui.access(new Runnable() {
 			@Override
 			public void run() {
 				setDifferingUsers(users);
 			}
 		});
 	}
 
 	private void setDifferingUsers(Set<User> differing) {
 		group.removeAllItems();
 		boolean iDiffer = differing.contains(user);
 		
 		if (!differing.isEmpty()) {
 			if (iDiffer) {
 				group.addItem(DocType.BASE);
 				group.setItemCaption(DocType.BASE, "(shared)");
 			}
 			else {
 				group.addItem(user);
 				group.setItemCaption(user, "(shared)");
 			}
 		}
 		
 		for (User u : differing) {
 			group.addItem(u);
 			group.setItemCaption(u, u.getName());
 		}
 		
 		if (differing.isEmpty()) {
 			changeEditor(user);
 			statusLabel.setIcon(Icons.DOCUMENT_VALID);
 			group.setDescription("The document is in sync.");
 		}
 		else {
 			if (differing.contains(currentEditorType)) {
 				group.select(currentEditorType);
 			}
 			else {
 				changeEditor(user);
 				group.select(user);
 			}
 			
 			if (iDiffer) {
 				statusLabel.setIcon(Icons.DOCUMENT_ERRORS_BY_ME);
 				group.setDescription("The document contains erros by you and " +(differing.size()-1) +" others.");
 			}
 			else {
 				statusLabel.setIcon(Icons.DOCUMENT_ERRORS_NOT_BY_ME);
 				group.setDescription("The document contains erros by " +differing.size() +" others.");
 			}
 		}	
 		
 		updateUseThisButton();
 	}
 	
 	private void updateUseThisButton() {
 		useThisButton.setVisible(currentEditorType!=user);
 	}
 
 	public void setWordWrap(boolean b) {
 		editor.setWordWrap(true);
 	}
 
 	public void addSelectionChangeListener(SelectionChangeListener listener) {
 		editor.addSelectionChangeListener(listener);
 	}
 
 	public void addTextChangeListener(TextChangeListener listener) {
 		editor.addTextChangeListener(listener);
 		
 	}
 
 	public TextRange getSelection() {
 		return editor.getSelection();
 	}
 
 	public AceDoc getDoc() {
 		return editor.getDoc();
 	}
 
 	public void addSuggestionExtension(SuggestionExtension se) {
 		se.extend(editor);
 	}
 
 	
 }
