 package ru.lspl.analyzer.rcp.editors;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.AnnotationPainter;
 import org.eclipse.jface.text.source.IAnnotationAccess;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackAdapter;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.editors.text.TextEditor;
 
 import ru.lspl.analyzer.rcp.Activator;
 import ru.lspl.analyzer.rcp.model.DocumentAnnotationModel;
 import ru.lspl.analyzer.rcp.model.annotations.MatchRangeAnnotation;
 import ru.lspl.analyzer.rcp.preferences.PreferenceConstants;
 
 public class DocumentTextEditor extends TextEditor {
 
 	public DocumentTextEditor() {
 		setSourceViewerConfiguration( new DocumentTextEditorConfiguration( getPreferenceStore() ) );
 	}
 
 	@Override
 	protected void setDocumentProvider( IEditorInput input ) {
 		setDocumentProvider( ((DocumentEditorInput) input).getDocumentProvider() );
 	}
 
 	@Override
 	protected void performSaveAs( IProgressMonitor progressMonitor ) {
 		FileDialog saveFileDialog = new FileDialog( getSite().getShell(), SWT.SAVE );
 
 		String fileName = saveFileDialog.open();
 
 		if ( fileName != null ) {
 			((DocumentEditorInput) getEditorInput()).getDocument().setFileName( fileName );
 			performSave( true, progressMonitor );
 		}
 	}
 
 	@Override
 	protected ISourceViewer createSourceViewer( Composite parent, IVerticalRuler ruler, int styles ) {
 		String annotationHighlightEvent = Activator.getDefault().getPreferenceStore().getString( PreferenceConstants.ANNOTATION_HIGHLIGHT_EVENT );
 
 		SourceViewer sv = (SourceViewer) super.createSourceViewer( parent, ruler, styles );
 
 		AnnotationPainter annotationPainter = createAnnotationPainter( sv );
 
 		sv.addPainter( annotationPainter );
 		sv.addTextPresentationListener( annotationPainter );
 
 		if ( annotationHighlightEvent.equals( "MOVE" ) ) {
 			sv.getTextWidget().addMouseMoveListener( new MouseMoveListener() {
 
 				@Override
 				public void mouseMove( MouseEvent e ) {
 					getAnnotationModel().showHoveredAnnotations( getMouseOffset( e ) );
 				}
 
 			} );
 			sv.getTextWidget().addMouseTrackListener( new MouseTrackAdapter() {
 
 				@Override
 				public void mouseExit( MouseEvent e ) {
 					getAnnotationModel().showAllAnnotations();
 				}
 
 			} );
 		} else {
 			sv.getTextWidget().addMouseTrackListener( new MouseTrackAdapter() {
 
 				@Override
 				public void mouseHover( MouseEvent e ) {
 					getAnnotationModel().showHoveredAnnotations( getMouseOffset( e ) );
 				}
 
 				@Override
 				public void mouseExit( MouseEvent e ) {
 					getAnnotationModel().showAllAnnotations();
 				}
 
 			} );
 		}
 
 		return sv;
 	}
 
 	protected AnnotationPainter createAnnotationPainter( SourceViewer sv ) {
 		IAnnotationAccess annotationAccess = new IAnnotationAccess() {
 
 			@Override
 			public Object getType( Annotation annotation ) {
 				return annotation.getType();
 			}
 
 			@Override
 			public boolean isMultiLine( Annotation annotation ) {
 				return true;
 			}
 
 			@Override
 			public boolean isTemporary( Annotation annotation ) {
 				return true;
 			}
 
 		};
 
 		AnnotationPainter annotationPainter = new AnnotationPainter( sv, annotationAccess );
 
 		registerMatchRangeAnnotationTypes( annotationPainter );
 
 		return annotationPainter;
 	}
 
 	protected void registerMatchRangeAnnotationTypes( AnnotationPainter annotationPainter ) {
 		RGB baseMax = new RGB( 80, 80, 0 );
 		RGB baseOne = new RGB( 255, 255, 160 );
 		Display display = Display.getDefault();
 
 		for ( int i = 1; i <= MatchRangeAnnotation.MAX_DEPTH; ++i ) {
 			annotationPainter.addHighlightAnnotationType( "ru.lspl.analyzer.match" + i );
 			annotationPainter.setAnnotationTypeColor( "ru.lspl.analyzer.match" + i,
 					lerpColor( baseOne, baseMax, display, MatchRangeAnnotation.MAX_DEPTH - i, MatchRangeAnnotation.MAX_DEPTH - 1 ) );
 		}
 	}
 
 	protected int getMouseOffset( MouseEvent e ) {
 		try {
 			return getSourceViewer().getTextWidget().getOffsetAtLocation( new Point( e.x, e.y ) );
 		} catch ( Throwable ex ) {
 			return -1;
 		}
 	}
 
 	protected DocumentAnnotationModel getAnnotationModel() {
 		DocumentEditorInput editorInput = (DocumentEditorInput) getEditorInput();
 		return (DocumentAnnotationModel) editorInput.getDocumentProvider().getAnnotationModel( editorInput );
 	}
 
 	private Color lerpColor( RGB baseOne, RGB baseMax, Display display, int pos, int max ) {
 		int r = baseMax.red + pos * (baseOne.red - baseMax.red) / max;
 		int g = baseMax.green + pos * (baseOne.green - baseMax.green) / max;
 		int b = baseMax.blue + pos * (baseOne.blue - baseMax.blue) / max;
 
 		return new Color( display, r, g, b );
 	}
 
 }
