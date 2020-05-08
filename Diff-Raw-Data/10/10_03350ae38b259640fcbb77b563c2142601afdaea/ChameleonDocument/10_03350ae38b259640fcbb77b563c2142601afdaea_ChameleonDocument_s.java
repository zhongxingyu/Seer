 package chameleon.eclipse.editors;
 
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPositionCategoryException;
 import org.eclipse.jface.text.DefaultPositionUpdater;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.TextPresentation;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.texteditor.MarkerUtilities;
 import org.rejuse.predicate.Predicate;
 import org.rejuse.predicate.SafePredicate;
 
 import chameleon.core.Config;
 import chameleon.core.document.Document;
 import chameleon.core.language.Language;
 import chameleon.core.namespace.Namespace;
 import chameleon.core.validation.BasicProblem;
 import chameleon.eclipse.ChameleonEditorPlugin;
 import chameleon.eclipse.connector.EclipseEditorTag;
 import chameleon.eclipse.editors.reconciler.ChameleonPresentationReconciler;
 import chameleon.eclipse.presentation.PresentationManager;
 import chameleon.eclipse.project.ChameleonProjectNature;
 import chameleon.input.ModelFactory;
 import chameleon.input.ParseException;
 import chameleon.input.PositionMetadata;
 
 /**
  * A document for the chameleon framework. The ChameleonDocument contains positions 
  * and a positionUpdater. After initialisation, the ChameleonDocument contains a model, which depends on 
  * the language the Document is used for.
  * 
  * @author Marko van Dooren
  * @author Jef Geerinckx
  * @author Manuel Van Wesemael 
  * @author Joeri Hendrickx 
  * @author Tim Vermeiren
  */
 
 public class ChameleonDocument extends org.eclipse.jface.text.Document {
 	
 	//The compilation unit of the document
 	private Document _doc;
 	
 	//manages the presentation
 	private PresentationManager _presentationManager;
 	//the path to the file of which this document is made
 	private IPath _path;
 	//the last known presentation of the document
 	private TextPresentation _lastpresentation;
 	
 //	//check whether presentation is going on by some reconciler
 //	private boolean _presenting;
 	//when the document contains language errors, they are stored here.
 	private List<ParseException> _parseErrors;	
 	//A listener for the time when the errors change
 	private ActionListener _parseErrorActionListener;
 	//The name of the document.
 	private String _name;
 	//The file of which this document is made
 	private IFile _file;
 	private String _relativePathName;
 
 	/**
 	 * creates a new ChameleonDocument & intializes it.
 	 * Initially there are no parse errors 
 	 * The document receives its name and is parsed when effective
 	 * Support for updating the positions in the document is set
 	 * @param file 
 	 *
 	 */
 	public ChameleonDocument(ChameleonProjectNature projectNature, IFile file, IPath path){
 		super();
 
 		setCompilationUnit(new Document());
 		if(projectNature==null){
 			ChameleonEditorPlugin.showMessageBox("Illegal project", "This document is part of an illegal project. \nCheck if the project is a Chameleon Project.", SWT.ICON_ERROR);
 		}
 
 		_parseErrors = new ArrayList<ParseException>();
 
 		_projectNature = projectNature;
 		initialize();
 
 		_path=path;
 
 		if (file!=null){ 		
 			try {
 				_relativePathName = path.removeFirstSegments(1).toString();
 				parseFile(file);
 			} catch (CoreException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else{
 			IPath projPath = path.removeFirstSegments(1);	 
 			file = projectNature.getProject().getFile(projPath);
 			_relativePathName = projPath.toString();
 		}
 		_name = file.getName();
 		_file = file;
 	}
 
 	public IPath path() {
 		return _path;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * The presentation managager if it is present; else a new presentation manager is made and returned
 	 */
 	public PresentationManager getPresentationManager(){
 		PresentationManager result = _presentationManager;
 		if(result == null) {
 			result = new PresentationManager(this, getProjectNature().presentationModel());
 			_presentationManager = result;
 		}
 		return result;
 	}
 	
 	/**
 	 * Checks whether the paths of the documents are the same
 	 * @param cd
 	 * 	the other chameleonDocument
 	 * @return
 	 * 	true if the paths are identical.
 	 */
 	public boolean isSameDocument(ChameleonDocument cd){
 		return _path.toOSString().equals(cd._path.toOSString());
 	}
 	
 	/**
 	 * Parses the given file. This is done to ensure that the document contents is set after
 	 * construction. In the editor, the set() method will be invoked again by the document provider when
 	 * an editor is created or activated. For now, however, the document provider is not used for reading
 	 * all files in the model (API files, and project files for which no editor is opened).
 	 * @param file
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	private void parseFile(IFile file) throws CoreException, IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
 		String nxt = "";
 		StringBuilder builder = new StringBuilder();
 		builder.append(reader.readLine());
 		while (nxt!=null) {
 			nxt = reader.readLine();
 			if (nxt!=null) {
 				builder.append("\n");
 				builder.append(nxt);
 			}
 		}
 		// QUESTION: does this trigger the reconcilers?
 		set(builder.toString());
 	}
 
 	/*
 	 * The initialisation of the document. 
 	 * PositionCategories are set together with a position updater.
 	 * The meta model is also set here.
 	 */
 	private void initialize(){
 		addPositionCategory(EclipseEditorTag.CHAMELEON_CATEGORY);
 		addPositionUpdater(new DefaultPositionUpdater(EclipseEditorTag.CHAMELEON_CATEGORY));
 	}
 
 
 	/**
 	 * Sets the compilation unit for this document
 	 * physically nothing changes !
 	 * @param cu
 	 * @pre cu must be effective
 	 */
 	public void setCompilationUnit(Document cu) {
 		_doc = cu;
 	}
 	
 	
 
 //	/**
 //	 * 
 //	 * @return the project where this compilation unit is in
 //	 */
 //	public IProject getProject() {
 //		return _project;
 //	}
 	
 	private ChameleonProjectNature _projectNature;
 	
 	/**
 	 * Returns the project nature of the project of this 
 	 * @return
 	 */
 	public ChameleonProjectNature getProjectNature(){
 		return _projectNature;
 //		try {
 //			IProjectNature nature = _project.getNature(ChameleonProjectNature.NATURE);
 //			return (ChameleonProjectNature) nature;
 //		}
 //		//TODO : Marko : why are all these exceptions caught here?
 //		catch (NullPointerException e) {
 //			return null;
 //		} 
 //		catch (CoreException e) {
 //			return null;
 //		}
 //		catch (ClassCastException e) {
 //			return null;
 //		}
 	}
 	
 	public Object getModel(){
 		throw new Error("Apparently this method is used by eclipse.");
 //		return getProjectNature().getModel();
 	}
 	
 	public ModelFactory modelFactory(){
 		return getProjectNature().modelFactory();
 	}
 
 
 	/**
 	 * 
 	 * @return the compilation unit
 	 */
 	public Document compilationUnit() {
 		return chameleonDocument();
 	}
 
 	/**
 	 * 
 	 * @return the compilation unit
 	 */
 	public Document chameleonDocument() {
 		return _doc;
 	}
 
 	/** 
 	 * Empty out the chameleonpositions, none of the decorators are left.
 	 *
 	 */
 	public void dumpPositions() {
 		try {
 			this.removePositionCategory(EclipseEditorTag.CHAMELEON_CATEGORY);
 			this.addPositionCategory(EclipseEditorTag.CHAMELEON_CATEGORY);
 		} catch (BadPositionCategoryException e) {}
 		
 	}
 
 
 	@Override
 	public synchronized void addPosition(Position position) throws BadLocationException {
 		super.addPosition(position);
 	}
 
 
 	@Override
 	public synchronized void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
 		super.addPosition(category, position);
 	}
 
 	@Override
 	public synchronized void removePosition(Position position) {
 		super.removePosition(position);
 	}
 
 
 	@Override
 	public synchronized void removePosition(String category, Position position) throws BadPositionCategoryException {
 		super.removePosition(category, position);
 	}
 	
 	
 
 	@Override
 	public synchronized Position[] getPositions(String category, int offset, int length, boolean canStartBefore, boolean canEndAfter)
 			throws BadPositionCategoryException {
 		return super.getPositions(category, offset, length, canStartBefore, canEndAfter);
 	}
 
 	@Override
 	public synchronized Position[] getPositions(String category) throws BadPositionCategoryException {
 		return super.getPositions(category);
 	}
 
 	public Language language() {
 		return getProjectNature().language();
 	}
 
 	/**
 	 * The text representation for the viewer is changed to the presentation we get from 
 	 * our presentation manager.
 	 * that representation is now the last known one.
 	 * @param viewer
 	 */
 	public void doPresentation(final ITextViewer viewer) {
 		try{
 			_lastpresentation = getPresentationManager().createTextPresentation();
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					// CHANGE in Tim's last version, the following line is absent.
 //					viewer.changeTextPresentation(_lastpresentation, false);
 					try {
 						if(_lastpresentation != null) {
 					    viewer.changeTextPresentation(_lastpresentation, true);
 						}
 					}
 					catch(NullPointerException exc) {
 					}
 				}
 			});	
 		}catch (NullPointerException npe){
 			npe.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * @return 
 	 *   the foldable positions
 	 */
 	public List<Position> getFoldablePositions() {
 		return getPresentationManager().getFoldablePositions();
 	}
 
 	/**
 	 * 
 	 * @return the elements which are folded
 	 */
 	public List<EclipseEditorTag> getFoldedElementsFromModel() {
 		return getPresentationManager().getFoldedElementsFromModel();
 	}
 
 	private int nbNamespaceParts(Namespace ns) {
 		int result = ns.getNamespaceParts().size();
 		for(Namespace namespace:ns.getSubNamespaces()) {
 			result += nbNamespaceParts(namespace);
 		}
 		return result;
 	}
 	
 
 	/**
 	 * Reparse this document.
 	 * 
 	 * The document is basically removed from the project and then added again.
 	 */
 	public void reParse() {
 
 		System.out.println("Starting reparse");
 
 		// to reparse an entire document
 
 		// A. remove all document positions & problem markers
 		System.out.println("DUMPING POSITIONS");
 		dumpPositions();
 		try {
 			getFile().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
 			_parseErrors.clear();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 		// if this is not set, the document is new and has never been parsed before.
 		if (_doc == null) {
 			getProjectNature().addToModel(this);
 		} else {
 
 			// B. remove compilation unit from model tree
 			if(Config.debug()) {
 			  System.out.println("removing compilation unit from tree");
 			}
 			// C. remove Document from project
 			if(Config.debug()) {
 				System.out.println("Remove document from project");
 			}
 			getProjectNature().removeDocument(this);
 
 			// D. Re-add Document to the project (wich will cause it to be parsed)
 			if(Config.debug()) {
 			  System.out.println("Re-add document to project");
 			}
 			Namespace root = getProjectNature().getModel();
 			getProjectNature().addModelElement(this, root);
 		}
 
 		System.out.println("Einde reparse");
 	}
 
 //	/**
 //	 * The textrepresentation for the viewer is changed to the presentation we get
 //	 * from our presenation manager. the presentation is done for the speciefied
 //	 * offset & length that representation is now the last known one.
 //	 * 
 //	 * @param viewer
 //	 * @param offset
 //	 * @param length
 //	 */
 //	public void doPresentation(final ITextViewer viewer, final int offset, final int length) {
 //		try{
 //			_lastpresentation = getPresentationManager().createTextPresentation();
 //			Display.getDefault().asyncExec(new Runnable() {
 //				public void run() {
 //					viewer.changeTextPresentation(_lastpresentation, true);
 //				}
 //			});	
 //		}catch (NullPointerException npe){}
 //		
 //	}
 
 	/**
 	 * A parse error has occured while making the model for the document;
 	 * that error is handled here. It is added to the other existing errors.
 	 * @param exc
 	 */
 	public void handleParseError(ParseException exc) {
 //		if(!parseErrors.contains(exc))
 //				parseErrors.add(exc);
 		boolean alreadyPresent = false;
 		for (ParseException exception : _parseErrors) {
 			if (exception.toString().equals(exc.toString())) {
 				 alreadyPresent = true;
 			}
 		}
 		if(!alreadyPresent) {
 			_parseErrors.add(exc);
 		}
 		if (_parseErrorActionListener!=null) { 
 			_parseErrorActionListener.actionPerformed(null);
 		}
 		
 	}
 
 	public void setParseActionListener(ActionListener listener){
 		this._parseErrorActionListener = listener;
 	}
 
 	/**
 	 * 
 	 * @return all the parse errors for this document
 	 */
 	public List<ParseException> getParseErrors() {
 		return new ArrayList<ParseException>(_parseErrors);
 	}
 
 	/**
 	 * 
 	 * @return the relative path of this document, starting from the project.
 	 */
 	public IPath getPath() {
 		return _path;
 	}
 
 	public String getName() {
 		return _name;
 	}
 
 	public IFile getFile() {
 		return _file;
 	}
 
 	public void removeParseErrors() {
 		_parseErrors.clear();
 		
 	}
 
 	public String getRelativePathName() {
 		
 		return _relativePathName;
 	}
 
 	/**
 	 * Returns the smallest ReferenceEditorTag including the beginoffset of region.
 	 * Returns null if no appropriate editorTag found.
 	 * 
 	 * @param 	region
 	 * 			a region in this document to search a surrounding editorTag from.
 	 * @return	If not null returned, the returned editorTag is a reference editorTag
 	 * 			| result == null || result.getName().equals(EditorTag.REFERENCE)
 	 * @return  The returned editorTag (if any) surrounds the beginoffset of the specified region.
 	 * 			| result == null || result.includes(region.getOffset())
 	 * @autor 	Tim Vermeiren
 	 */
 	public EclipseEditorTag getReferencePositionAtRegion(IRegion region){
 		try {
 			Position[] positions = getPositions(EclipseEditorTag.CHAMELEON_CATEGORY);
 			// Find smallest decorater including the specified region:
 			int minLength = Integer.MAX_VALUE;
 			EclipseEditorTag result = null;
 			for (Position position : positions) {
 				if(position instanceof EclipseEditorTag ){
 					EclipseEditorTag decorator = (EclipseEditorTag) position;
 					if(decorator.getName().equals(PositionMetadata.CROSSREFERENCE)){
 						if(decorator.includes(region.getOffset()) && decorator.getLength()<minLength){
 							result = decorator;
 						}
 					}
 				}
 					
 			}
 			return result;
 		} catch (BadPositionCategoryException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the smallest ReferenceEditorTag including the beginoffset of region.
 	 * Returns null if no appropriate editorTag found.
 	 * 
 	 * @param 	region
 	 * 			a region in this document to search a surrounding editorTag from.
 	 * @return	If not null returned, the returned editorTag is a reference editorTag
 	 * 			| result == null || result.getName().equals(EditorTag.REFERENCE)
 	 * @return  The returned editorTag (if any) surrounds the beginoffset of the specified region.
 	 * 			| result == null || result.includes(region.getOffset())
 	 * @autor 	Tim Vermeiren
 	 */
 	public EclipseEditorTag getReferenceEditorTagAtRegion(IRegion region){
 		final int offset = region.getOffset();
 		// build a predicate that checks if the EditorTag includes the offset:
 		SafePredicate<EclipseEditorTag> predicate = new SafePredicate<EclipseEditorTag>(){
 			@Override
 			public boolean eval(EclipseEditorTag editorTag) {
 				return (editorTag.getName().equals(PositionMetadata.CROSSREFERENCE)) && editorTag.includes(offset);
 			}
 		};
 		Collection<EclipseEditorTag> tags = new TreeSet<EclipseEditorTag>(EclipseEditorTag.lengthComparator);
 		getEditorTagsWithPredicate(predicate, tags);
 		if(tags.size()==0){
 			return null;
 		}
 		return tags.iterator().next();
 	}
 
 	/**
 	 * Returns the smallest EditorTag including the beginoffset of region.
 	 * Returns null if no appropriate editorTag found.
 	 * 
 	 * @param 	offset
 	 * 			the offset in this document to search a surrounding editorTag from.
 	 * @return  The returned editorTag (if any) surrounds the beginoffset of the specified region.
 	 * 			| result == null || result.includes(region.getOffset())
 	 * @autor 	Tim Vermeiren
 	 */
 	public EclipseEditorTag getSmallestEditorTagAtOffset(final int offset){
 		Collection<EclipseEditorTag> tags = getEditorTagsAtOffset(offset, EclipseEditorTag.lengthComparator);
 		if(tags.size()==0){
 			return null;
 		}
 		return tags.iterator().next();
 	}
 	
 	/**
 	 * Returns all EditorTags surrounding the given offset and sorted
 	 * by the given comparator
 	 * 
 	 * @param offset
 	 * @param comparator
 	 * @autor Tim Vermeiren
 	 */
 	public Collection<EclipseEditorTag> getEditorTagsAtOffset(int offset, Comparator<EclipseEditorTag> comparator){
 		// build a predicate that checks if the EditorTag includes the offset:
 		SafePredicate<EclipseEditorTag> predicate = new EclipseEditorTag.SurroundsOffsetPredicate(offset);
 		Collection<EclipseEditorTag> tags = new TreeSet<EclipseEditorTag>(comparator);
 		getEditorTagsWithPredicate(predicate, tags);
 		return tags;
 	}
 
 	/**
 	 * Gives the editorTags satisfying the given predicate.
 	 * 
 	 * @param 	predicate
 	 * 			the predicate containing the condition the editorTag has to satisfy
 	 * param	result
 	 * 			The editorTags satisfying the condition will be added to this collection
 	 * 			| predicate.eval(result) == true
 	 * @post	If the predicate throws an exception the searching is just stopped and the already found
 	 * 			elements will be added to the result
 	 * @autor 	Tim Vermeiren
 	 */
 	public void getEditorTagsWithPredicate(Predicate<EclipseEditorTag> predicate, Collection<EclipseEditorTag> result){
 		try {
 			Position[] positions = getPositions(EclipseEditorTag.CHAMELEON_CATEGORY); // throws BadPositionCategoryException
 			for (Position position : positions) {
 				int count=1;
 				if(position instanceof EclipseEditorTag ){
 					EclipseEditorTag editorTag = (EclipseEditorTag) position;
 					if(predicate.eval(editorTag)){
 //						System.out.println(count+" Found cross reference tag at offset: " +editorTag.getOffset()+"with length: "+editorTag.getLength());
 						result.add(editorTag);
 					}
 				}
 			}
 		} catch (BadPositionCategoryException e) {
 			// There exist no editorTags of the type EditorTag.CHAMELEON_CATEGORY
 			e.printStackTrace();
 		} catch (Exception e) {
 			// the predicate has thrown an exception
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns the word found in document including the given offset.
 	 * For testing purposes only. All word-characters (JavaIdentifierParts to be precise)
 	 * before and after the offset are included.
 	 * 
 	 * @param document
 	 * @param offset
 	 */
 	public  String findWord(int offset) {
 		IRegion wordRegion = findWordRegion(offset);
 		String word = null;
 		try {
 			word = this.get(wordRegion.getOffset(), wordRegion.getLength());
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		return word;
 	}
 
 	/**
 	 * Returns the region of the word in this document including the given offset.
 	 * For testing purposes only. All word-characters (JavaIdentifierParts to be precise)
 	 * before and after the offset are included.
 	 * 
 	 * @see org.eclipse.jdt.internal.ui.text.JavaWordFinder
 	 */
 	public IRegion findWordRegion(int offset) {
 		int start = -2;
 		int end = -1;
 		try {
 			int pos = offset;
 			char c;
 
 			while (pos >= 0) {
 				c = this.getChar(pos);
 				if (!language().isValidIdentifierCharacter(c))
 					break;
 				--pos;
 			}
 			start = pos;
 
 			pos = offset;
 			int length = this.getLength();
 
 			while (pos < length) {
 				c = this.getChar(pos);
 				if (!language().isValidIdentifierCharacter(c))
 					break;
 				++pos;
 			}
 			end = pos;
 
 		} catch (BadLocationException x) {
 		}
 
 		if (start >= -1 && end > -1) {
 			if (start == offset && end == offset) {
 				return new Region(offset, 0);
 			}
 			else if (start == offset) {
 				return new Region(start, end - start);
 			}
 			else {
 				return new Region(start + 1, end - start - 1);
 			}
 		}
 
 		return null;
 	}
 
 	
 	public void markError(BasicProblem problem) {
 		String message = problem.message();
 		HashMap<String, Object> attributes = ChameleonPresentationReconciler.createProblemMarkerMap(message);
 		EclipseEditorTag positionTag = null;
		positionTag = (EclipseEditorTag) problem.element().metadata(PositionMetadata.ALL);
		if(positionTag == null) {
			positionTag = (EclipseEditorTag) problem.element().metadata(PositionMetadata.ALL);
 		}
 		if(positionTag != null) {
 			int offset = positionTag.getOffset();
 			int length = positionTag.getLength();
 			setProblemMarkerPosition(attributes, offset, length);
 		} else {
 			attributes.put(IMarker.LINE_NUMBER, 1);
 			System.out.println("ERROR: element of type "+problem.element().getClass().getName()+" is invalid, but there is no position attached.");
 		}
 		addProblemMarker(attributes);
 	}
 
 	public void addProblemMarker(Map<String, Object> attributes) {
 		try {
 			MarkerUtilities.createMarker(getFile(),attributes,IMarker.PROBLEM);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void addWarningMarker(Map<String, Object> attributes) {
 		try {
 			makeWarning(attributes);
 			MarkerUtilities.createMarker(getFile(),attributes,IMarker.PROBLEM);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void makeWarning(Map<String, Object> attributes) {
 		attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
 	}
 
 	public void setProblemMarkerPosition(Map<String, Object> attributes, int offset, int length) {
 		int lineNumber;
 		try {
 			lineNumber = getLineOfOffset(offset);
 			lineNumber++;
 		} catch (BadLocationException e) {
 			lineNumber = 0;
 		}
     attributes.put(IMarker.CHAR_START, offset);
 		attributes.put(IMarker.CHAR_END, offset + length);
 	  attributes.put(IMarker.LINE_NUMBER, lineNumber);
 	}
 
 
 }
 
 
