 /*
  * Copyright 2000-2004 the Jnome development team.
  *
  * @author Marko van Dooren
  * @author Nele Smeets
  * @author Kristof Mertens
  * @author Jan Dockx
  *
  * This file is part of Jnome.
  *
  * Jnome is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  *
  * Jnome is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * Jnome; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 package chameleon.core.statement;
 
 import java.util.List;
 import java.util.ListIterator;
 
 import org.rejuse.java.collections.RobustVisitor;
 import org.rejuse.predicate.TypePredicate;
 
 import chameleon.core.language.Language;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.namespace.Namespace;
 import chameleon.core.namespacepart.NamespacePartElementImpl;
 import chameleon.core.type.Type;
 
 /**
  * @author Marko van Dooren
  */
 
 public abstract class Statement<E extends Statement> extends NamespacePartElementImpl<E,StatementContainer> implements ExceptionSource<E,StatementContainer> {
 
 
     protected Statement() {
     }
 
 
   public Type getNearestType() {
     return parent().getNearestType();
   }
 
   public Namespace getNamespace() {
     return getNearestType().getNamespace();
   }
   
   public abstract E clone();
 
   public CheckedExceptionList getCEL() throws LookupException {
     final CheckedExceptionList cel = getDirectCEL();
     try {
       new RobustVisitor() {
         public Object visit(Object element) throws LookupException {
           cel.absorb(((ExceptionSource)element).getCEL());
           return null;
         }
 
         public void unvisit(Object element, Object undo) {
           //NOP
         }
       }.applyTo(getExceptionSources());
       return cel;
     }
     catch (LookupException e) {
       throw e;
     }
     catch (Exception e) {
       e.printStackTrace();
       throw new Error();
     }
   }
 
   public CheckedExceptionList getDirectCEL() throws LookupException {
     return new CheckedExceptionList(language());
   }
 
   public CheckedExceptionList getAbsCEL() throws LookupException {
     final CheckedExceptionList cel = getDirectAbsCEL();
     try {
       new RobustVisitor() {
         public Object visit(Object element) throws LookupException {
           cel.absorb(((ExceptionSource)element).getAbsCEL());
           return null;
         }
 
         public void unvisit(Object element, Object undo) {
           //NOP
         }
       }.applyTo(children());
       return cel;
     }
     catch (LookupException e) {
       throw e;
     }
     catch (Exception e) {
       e.printStackTrace();
       throw new Error();
     }
   }
 
   public CheckedExceptionList getDirectAbsCEL() throws LookupException {
     return getDirectCEL();
   }
 
 	/**
 	 * All children are of type ExceptionSource.
 	 */
 	public abstract List children();
 
 
   public List getExceptionSources() {
     List result = children();
     new TypePredicate(ExceptionSource.class).filter(result);
     return result;
   }
 
   public final List getSubStatements() {
     List result = children();
     new TypePredicate(Statement.class).filter(result);
     return result;
   }
 
   public Language language() {
     return getNamespace().language();
   }
 
   public boolean before(Statement other) {
     StatementListContainer container = getNearestCommonStatementListContainer(other);
     List myParents = ancestors();
     List otherParents = other.ancestors();
     myParents.add(0, this);
     otherParents.add(0, other);
     Statement myAncestor = (Statement)myParents.get(myParents.indexOf(container) - 1);
     Statement otherAncestor = (Statement)otherParents.get(myParents.indexOf(container) - 1);
     return container.getIndexOf(myAncestor) < container.getIndexOf(otherAncestor);
   }
 
   public StatementListContainer getNearestCommonStatementListContainer(Statement other) {
     List myParents = ancestors();
     List otherParents = other.ancestors();
     ListIterator myIter = myParents.listIterator(myParents.size());
     ListIterator otherIter = otherParents.listIterator(myParents.size());
     Object common = null;
     while ((myIter.hasPrevious()) && (otherIter.hasPrevious())) {
       Object mine = myIter.previous();
       Object others = otherIter.previous();
       if(mine.equals(others)) {
         common = mine;
       }
     }
     if(common instanceof StatementListContainer) {
       return (StatementListContainer)common;
     }
     else {
       return null;
     }
   }
   
  /**
   * The linear lookup strategy of a statement is the lookup strategy used for the element that comes next in a block.
   * For example, the linear lookup strategy of a variable declaration statement includes the declared variable, while its
   * lexicalContext() method does not.
   * 
   * Returns the lexical context by default.
   * @return
   * @throws LookupException
   */
   public LookupStrategy linearContext() throws LookupException {
   	return lexicalContext();
   }
 }
