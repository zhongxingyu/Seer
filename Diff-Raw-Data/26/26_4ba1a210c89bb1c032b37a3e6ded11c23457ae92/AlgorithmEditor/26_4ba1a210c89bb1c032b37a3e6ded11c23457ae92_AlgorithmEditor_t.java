 /**
  * 
  */
 package de.unisiegen.informatik.bs.alvis.editors;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.antlr.runtime.Token;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.AnnotationModel;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
 import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
 import org.eclipse.jface.text.source.projection.ProjectionSupport;
 import org.eclipse.jface.text.source.projection.ProjectionViewer;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
 
 import de.unisiegen.informatik.bs.alvis.Activator;
 import de.unisiegen.informatik.bs.alvis.compiler.CompilerAccess;
 import de.unisiegen.informatik.bs.alvis.extensionpoints.IExportItem;
 
 /**
  * @author Eduard Boos
  * 
  */
 public class AlgorithmEditor extends AbstractDecoratedTextEditor implements
 		IExportItem, KeyListener {
 
 	private ProjectionAnnotationModel annotationModel;
 	private ProjectionSupport projectionSupport;
 	private Annotation[] oldAnnotations;
 	private Boolean[] isCollapsed;
	private String codeOfLastCompile = "";
 	protected long lastKeyStroke;
 
 	/**
 	 * 
 	 */
 	public AlgorithmEditor() {
 
 		/** filling Tokens to highlight */
 		Color highlightColor = new Color(Display.getCurrent(), new RGB(111, 33,
 				152));
 		ArrayList<String> highlightTokens = new ArrayList<String>();
 		highlightTokens.addAll(CompilerAccess.getDefault().allKeywords());
 		AlgorithmEditorSourceViewerConfiguration sourceViewerConfiguration = new AlgorithmEditorSourceViewerConfiguration(
 				highlightColor, highlightTokens);
 		setSourceViewerConfiguration(sourceViewerConfiguration);
 		/** endof filling */
 		setDocumentProvider(new AlgorithmDocumentProvider());
 	}
 
 	/**
 	 * Returns the Editors TextWidget
 	 * 
 	 * @return the StyledText represented by the TextWidget
 	 */
 	public StyledText getTextWidget() {
 		return getSourceViewer().getTextWidget();
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
 		projectionSupport = new ProjectionSupport(viewer,
 				getAnnotationAccess(), getSharedColors());
 
 		projectionSupport.install();
 
 		// turn projection mode on
 		viewer.doOperation(ProjectionViewer.TOGGLE);
 		annotationModel = viewer.getProjectionAnnotationModel();
 
 		/** compile Algorithm first time */
 		CompilerAccess.getDefault().setDatatypePackages(
 				Activator.getDefault().getAllDatatypesPackagesInPlugIns());
 		CompilerAccess.getDefault().setDatatypes(
 				Activator.getDefault().getAllDatatypesInPlugIns());
 		IFileEditorInput input = (IFileEditorInput) getEditorInput();
 		try {
 			CompilerAccess.getDefault().compile(
 					input.getFile().getRawLocation().toString(), true);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 		}
 		/** endof Compile Algorithm */
 		getSourceViewer().getTextWidget().addKeyListener(this);
 	}
 
 	@Override
 	protected ISourceViewer createSourceViewer(Composite parent,
 			IVerticalRuler ruler, int styles) {
 		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
 				getOverviewRuler(), isOverviewRulerVisible(), styles);
 
 		// ensure decoration support has been created and configured.
 		getSourceViewerDecorationSupport(viewer).install(getPreferenceStore());
 
 		return viewer;
 	}
 
 	@Override
 	public void doSave(IProgressMonitor progressMonitor) {
 		super.doSave(progressMonitor);
 		compileCode(getSourceViewer().getTextWidget().getText());
 		markErrors();
 		calculatePositions();
 	}
 
 	/**
 	 * This method update the editors FoldingStructure by setting the
 	 * annotations of the Editor. The field fPositions is used to create the
 	 * Annotations.
 	 * 
 	 * @param positions
 	 */
 	public void updateFoldingStructure(ArrayList<Position> positions) {
 		/**
 		 * annotation will hold the new Folding Annotations with their
 		 * corresponding Positions
 		 */
 		Annotation[] annotations = new Annotation[positions.size()];
 		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();
 
 		for (int i = 0; i < positions.size(); i++) {
 			ProjectionAnnotation annotation = new ProjectionAnnotation(
 					isCollapsed[i]);
 
 			newAnnotations.put(annotation, positions.get(i));
 
 			annotations[i] = annotation;
 		}
 
 		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
 		oldAnnotations = annotations;
 	}
 
 	/**
 	 * This method searches all available folding Positions and saves them in
 	 * fPositions and runs updateFoldingStructure in a thread to update the
 	 * editors Folding annotations. calculate and add the ProjectionsAnnotations
 	 * for the folding feature
 	 */
 	protected void calculatePositions() {
 
 		/** create empty Position List */
 		ArrayList<Position> foldingPositions = new ArrayList<Position>();
 
 		/** start fill positionList with all MultilineComment positions */
 		try {
 			ITypedRegion[] partitions = getSourceViewer().getDocument()
 					.computePartitioning(0,
 							getSourceViewer().getDocument().getLength());
 			for (ITypedRegion region : partitions) {
 				if (region.getType().equals(
 						AlgorithmPartitionScanner.MULTILINE_COMMENT)) {
 					foldingPositions.add(new Position(region.getOffset(),
 							region.getLength()));
 
 				}
 			}
 		} catch (BadLocationException e) {
 			// TODO handle Exception
 			e.printStackTrace();
 		}
 
 		/** getting all begin and end Tokens from the document */
 		List<Token> beginTokens = CompilerAccess.getDefault().beginBlock();
 		List<Token> endTokens = CompilerAccess.getDefault().endBlock();
 
 		/**
 		 * getting last beginToken before the first end Token to calculate a
 		 * Position
 		 */
 		while (!beginTokens.isEmpty() && !endTokens.isEmpty()) {
 			Token endToken = endTokens.get(0);
 			Token beginToken = null;
 			Iterator<Token> beginTokenIterator = beginTokens.iterator();
 			while (beginTokenIterator.hasNext()) {
 				Token tokenToTest = beginTokenIterator.next();
 				if (tokenToTest.getLine() < endToken.getLine()
 						|| (tokenToTest.getLine() == endToken.getLine() && tokenToTest
 								.getCharPositionInLine() < endToken
 								.getCharPositionInLine())) {
 					beginToken = tokenToTest;
 				} else {
 					break;
 				}
 			}
 
 			// startOfBegin represents the last character
 			// Position in the line of begin
 			if (beginToken != null) {
 				int startOfBegin = (getOffset(beginToken.getLine() - 1,
 						beginToken.getCharPositionInLine()));
 				int endOfEnd = (getOffset(endToken.getLine(), 0));
 				if (endOfEnd < 0) {
 					endOfEnd = getSourceViewer().getDocument().getLength();
 				}
 				int regionLength = endOfEnd - startOfBegin;
 				if (regionLength > 0) {
 					foldingPositions.add(new Position(startOfBegin,
 							regionLength));
 
 				}
 				beginTokens.remove(beginToken);
 			}
 			endTokens.remove(0);
 		}
 
 		/** Test which Regions overlaps, and removing the smaller ones */
 		ArrayList<Position> tmpList = new ArrayList<Position>();
 		for (Position position : foldingPositions) {
 			for (Position currentTestPosition : foldingPositions) {
 				if (!position.equals(currentTestPosition)
 						&& position.overlapsWith(currentTestPosition.offset,
 								currentTestPosition.offset)) {
 					if (position.length > currentTestPosition.length) {
 						tmpList.add(currentTestPosition);
 					} else {
 						tmpList.add(position);
 					}
 				}
 			}
 		}
 		foldingPositions.removeAll(tmpList);
 
 		/** getting old folding state and saving in isCollapsed */
 
 		// initialize isCollapsedArray, all new Folding Positions will be
 		// open(not Collapsed)
 		isCollapsed = new Boolean[foldingPositions.size()];
 		for (int i = 0; i < foldingPositions.size(); i++) {
 			isCollapsed[i] = false;
 		}
 
 		@SuppressWarnings("unchecked")
 		Iterator<Annotation> iter = annotationModel.getAnnotationIterator();
 		while (iter.hasNext()) {
 			Annotation currAnnotation = iter.next();
 			if (currAnnotation.getType().equals(ProjectionAnnotation.TYPE))
 
 			{
 				Position projPosition = annotationModel
 						.getPosition(currAnnotation);
 				ProjectionAnnotation projAnnotation = (ProjectionAnnotation) currAnnotation;
 				for (int i = 0; i < foldingPositions.size(); i++) {
 					Position positionToTest = foldingPositions.get(i);
 					if (positionToTest.overlapsWith(projPosition.offset,
 							projPosition.length)) {
 						isCollapsed[i] = projAnnotation.isCollapsed();
 					}
 				}
 			}
 		}
 
 		/** update the Folding Structure of the Editor in a new thread */
 		final AlgorithmEditor editor = this;
 		final ArrayList<Position> fPositions = foldingPositions;
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				editor.updateFoldingStructure(fPositions);
 			}
 
 		});
 	}
 
 	/**
 	 * This method will delegate the call to AlgorithmErrorMarker and mark the
 	 * file/document opened by the editor
 	 */
 	protected void markErrors() {
 		AlgorithmErrorMarker errorMarker = new AlgorithmErrorMarker(
 				getInputFile(), getSourceViewer().getDocument());
 		errorMarker.markErrors();
 	}
 
 	/**
 	 * This method compiles the code given.
 	 * 
 	 * @param code
 	 *            the code to compile.
 	 * @return true if the code was checked by the compiler. If the code given
 	 *         contains grammatical errors, the return value will be true as
 	 *         well, as long as the compiler runs the method without Exceptions.
 	 */
 	protected boolean compileCode(String code) {
 		try {
 			CompilerAccess.getDefault().compileString(code);
			codeOfLastCompile = code;
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * return the input File for the Editor
 	 * 
 	 * @return the IFile representing the input File for the Editor
 	 */
 	protected IFile getInputFile() {
 		IFileEditorInput fileEditorInput = (IFileEditorInput) getEditorInput();
 		IFile file = fileEditorInput.getFile();
 		return file;
 	}
 
 	@Override
 	public Image getImage() {
 		// no image in this editor
 		return null;
 	}
 
 	public AnnotationModel getAnnotationModel() {
 		return annotationModel;
 	}
 
 	@Override
 	public boolean isRun() {
 		return false;
 	}
 
 	@Override
 	public Object getSourceCode() {
 		return getTextWidget();
 	}
 
 	/**
 	 * Return the offset of the Document at the transmitted line and offset in
 	 * this line.
 	 * 
 	 * @param line
 	 *            the lines
 	 * @param charPositionInLine
 	 *            the offset in the line given.
 	 * @return the offset of the position given by line and charPositionInLine
 	 *         in the current document.
 	 */
 	public int getOffset(int line, int charPositionInLine) {
 		try {
 			return getSourceViewer().getDocument().getLineOffset(line)
 					+ charPositionInLine;
 		} catch (BadLocationException e) {
 			return -1;
 		}
 	}
 
 	/**
 	 * Return the current font size of the Edior.
 	 * 
 	 * @return the current font size.
 	 */
 	public int getCurrentFontSize() {
 		return getTextWidget().getFont().getFontData()[0].getHeight();
 	}
 
 	/**
 	 * Increases the current editor font-size by 4;
 	 * 
 	 * @return the increased font-size
 	 */
 	public int increaseFont() {
 		int newFontSize = getCurrentFontSize() + 4;
 		setFontSize(newFontSize);
 		return newFontSize;
 	}
 
 	/**
 	 * Decreases the current editor font-size by 4.
 	 * 
 	 * @return the decreased font-size
 	 */
 	public int decreaseFont() {
 		int currentFontsize = getCurrentFontSize();
 		int newFontSize;
 		if (currentFontsize > 4) {
 			newFontSize = currentFontsize - 4;
 		} else {
 			newFontSize = currentFontsize;
 		}
 		setFontSize(newFontSize);
 		return newFontSize;
 	}
 
 	/**
 	 * Set the font size of the Editor.
 	 * 
 	 * @param size
 	 *            the size to set
 	 */
 	public void setFontSize(int size) {
 		FontData[] fontData = getTextWidget().getFont().getFontData();
 		for (int i = 0; i < fontData.length; i++) {
 			fontData[i].setHeight(size);
 		}
 		PreferenceConverter.setValue(getPreferenceStore(),
 				getFontPropertyPreferenceKey(), fontData);
 		getTextWidget().update();
 	}
 
 	/**
 	 * Set the font of the Editor.
 	 * 
 	 * @param fd
 	 *            the FontData representing the font to set.
 	 */
 	public void setFont(FontData fd) {
 		PreferenceConverter.setValue(getPreferenceStore(),
 				getFontPropertyPreferenceKey(), fd);
 		getTextWidget().update();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		Thread timeChecker = new Thread(new CheckTimeAndMarkErrors(this,
 				getSourceViewer().getTextWidget().getText()));
 		timeChecker.start();
 	}

	/**
	 * Check whether a new compilation of the editor's code is needed to be up to date.
	 * @return true if last compile was executed with a different code
	 */
	public boolean isCompilationNeeded(String codeToTest) {
		if (codeOfLastCompile.equals(codeToTest)) {
			// no compilation is needed
			return false;
		} else {
			return true;
		}
	}
 }
