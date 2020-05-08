 package ru.lspl.analyzer.rcp.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.AnnotationModel;
 
 import ru.lspl.analyzer.rcp.model.annotations.AnnotationWithPosition;
 import ru.lspl.analyzer.rcp.model.annotations.MatchRange;
 import ru.lspl.analyzer.rcp.model.annotations.MatchRangeAnnotation;
 import ru.lspl.analyzer.rcp.model.annotations.MatchRangeBuilder;
 import ru.lspl.patterns.Pattern;
 import ru.lspl.text.Match;
 
 public class DocumentAnnotationModel extends AnnotationModel implements IAnalysisListener {
 
 	private final MatchRangeBuilder matchRangeBuilder = new MatchRangeBuilder();
 
 	private Document document;
 
 	private Set<Pattern> selectedPatterns = Collections.emptySet();
 
 	private Collection<AnnotationWithPosition> annotations = new ArrayList<AnnotationWithPosition>();
 
 	private int lastHoverOffset = -1;
 
 	@Override
 	protected void connected() {
 		document = (Document) fDocument;
 		document.addAnalysisListener( this );
 	}
 
 	@Override
 	protected void disconnected() {
 		document.removeAnalysisListener( this );
 		document = null;
 
 		selectedPatterns = Collections.emptySet();
 		annotations.clear();
 	}
 
 	@Override
 	public void analysisRequired( Document doc ) {
 	}
 
 	@Override
 	public void analysisStarted( Document doc ) {
 	}
 
 	@Override
 	public void analysisCompleted( Document doc ) {
 		// Building selected annotations
 		annotations.clear();
 		addMatchRangeAnnotations( document.getMatches( selectedPatterns ) );
 
 		// Displaying annotations
 		showAllAnnotations();
 
 		fireModelChanged();
 	}
 
 	public Set<Pattern> getSelectedPatterns() {
 		return selectedPatterns;
 	}
 
 	public void setSelectedPatterns( Set<Pattern> selectedPatterns ) {
 		this.selectedPatterns = selectedPatterns;
 
 		analysisCompleted( document );
 	}
 
 	private void addMatchRangeAnnotations( Collection<Match> matches ) {
 		for ( MatchRange m : matchRangeBuilder.buildMatchRanges( matches, MatchRangeAnnotation.MAX_DEPTH ) )
 			annotations.add( new AnnotationWithPosition( new MatchRangeAnnotation( m.depth ), new Position( m.start, m.end - m.start ) ) );
 	}
 
 	public void showHoveredAnnotations( int offset ) {
 		if ( offset == lastHoverOffset )
 			return;
 
		removeAllAnnotations();
 		for ( MatchRange m : matchRangeBuilder.buildMatchRanges( document.findMatchesContainingPosition( offset ), MatchRangeAnnotation.MAX_DEPTH ) ) {
 			try {
 				addAnnotation( new MatchRangeAnnotation( m.depth ), new Position( m.start, m.end - m.start ), false );
 			} catch ( BadLocationException e ) {
 			}
 		}
 		fireModelChanged();
 
 		lastHoverOffset = offset;
 	}
 
 	public void showAllAnnotations() {
		removeAllAnnotations();
 		for ( AnnotationWithPosition ap : annotations ) {
 			try {
 				addAnnotation( ap.annotation, ap.position, false );
 			} catch ( BadLocationException e ) {
 			}
 		}
 		fireModelChanged();
 
 		lastHoverOffset = -1;
 	}
 
 }
