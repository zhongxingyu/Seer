 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.stdlib;
 
 import jason.JasonException;
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.IntendedMeans;
 import jason.asSemantics.InternalAction;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.LogicalFormula;
 import jason.asSyntax.ObjectTerm;
 import jason.asSyntax.ObjectTermImpl;
 import jason.asSyntax.PlanBody;
 import jason.asSyntax.PlanBodyImpl;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 import jason.asSyntax.PlanBody.BodyType;
 
 import java.util.Iterator;
 
 // TODO: comments
 public class foreach extends DefaultInternalAction {
 
 	private static InternalAction singleton = null;
 	public static InternalAction create() {
 		if (singleton == null) 
 			singleton = new foreach();
 		return singleton;
 	}
 	
     @SuppressWarnings("unchecked")
 	@Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         try {
         	IntendedMeans im    = ts.getC().getSelectedIntention().peek();
         	PlanBody      foria = im.getCurrentStep();
 
         	Iterator<Unifier> iu;
         	
             if (args.length != 3) {
 	            // first execution of while
             	if ( !(args[0] instanceof LogicalFormula))
             		throw new JasonException("The first argument of .for must be a logical formula.");
 	            if ( !args[1].isPlanBody())
 	        		throw new JasonException("The second argument of .for must be a plan body term.");
             	
	        	// get the solutions for the loop
 	            LogicalFormula logExpr = (LogicalFormula)args[0];
 	            iu = logExpr.logicalConsequence(ts.getAg(), un.copy());
 	        	((Structure)foria.getBodyTerm()).addTerm(new ObjectTermImpl(iu));
             } else {
             	// restore the solutions
             	iu = (Iterator<Unifier>)((ObjectTerm)args[2]).getObject();
             }
             
             if (iu.hasNext()) {
             	un.clear();
             	un.compose(iu.next());
             	PlanBody whattoadd = (PlanBody)args[1].clone(); 
 	            whattoadd.add(new PlanBodyImpl(BodyType.internalAction, (Term)foria.getBodyTerm().clone())); 
 	        	whattoadd.setAsBodyTerm(false);
 	        	foria.add(1,whattoadd);
 	        	//System.out.println("new body="+foria.getBodyNext());
             }
             return true;
         } catch (ArrayIndexOutOfBoundsException e) {
             throw new JasonException("The internal action 'for' has not received the required arguments.");
         } catch (JasonException e) {
         	throw e;
         } catch (Exception e) {
             throw new JasonException("Error in internal action 'for': " + e, e);
         }
     }
 }
