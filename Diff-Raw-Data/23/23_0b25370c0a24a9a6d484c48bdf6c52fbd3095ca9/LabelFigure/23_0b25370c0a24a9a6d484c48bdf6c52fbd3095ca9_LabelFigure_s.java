 /*
  * Created on Jan 19, 2006
  */
 package net.nansore.cedalion.figures;
 
 import net.nansore.cedalion.eclipse.TermContext;
 import net.nansore.cedalion.eclipse.TermVisualizationException;
 import net.nansore.prolog.Compound;
 
 import org.eclipse.draw2d.Label;
 
 /**
  * @author boaz
  */
 public class LabelFigure extends Label implements TermFigure {
 
     public LabelFigure(Compound term, TermContext context) throws TermVisualizationException {
         setForegroundColor(context.getColor());
         setFont(context.getFont(TermContext.NORMAL_FONT));
         setText(translateString(term));
         context.bindFigure(this);
     }
 
 	private String translateString(Compound term) throws TermVisualizationException {
 		try {
			return (String)term.arg(1);			
 		} catch(ClassCastException e) {
 			throw new TermVisualizationException("LabelFigure received non string: " + term.arg(1));
 		}
 	}
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.figures.TermFigure#updateFigure()
      */
     public void updateFigure() throws TermVisualizationException {
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.figures.TermFigure#dispose()
      */
     public void dispose() {
         // TODO Auto-generated method stub
         
     }
     
     
 }
