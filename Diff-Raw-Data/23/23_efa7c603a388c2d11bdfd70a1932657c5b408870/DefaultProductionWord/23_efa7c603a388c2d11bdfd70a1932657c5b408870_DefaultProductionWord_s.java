 package de.unisiegen.gtitool.core.entities;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.event.EventListenerList;
 
 import de.unisiegen.gtitool.core.Messages;
 import de.unisiegen.gtitool.core.entities.listener.ModifyStatusChangedListener;
 import de.unisiegen.gtitool.core.exceptions.nonterminalsymbol.NonterminalSymbolException;
 import de.unisiegen.gtitool.core.exceptions.terminalsymbol.TerminalSymbolException;
 import de.unisiegen.gtitool.core.parser.ParserOffset;
 import de.unisiegen.gtitool.core.parser.style.PrettyPrintable;
 import de.unisiegen.gtitool.core.parser.style.PrettyString;
 import de.unisiegen.gtitool.core.parser.style.PrettyToken;
 import de.unisiegen.gtitool.core.parser.style.Style;
 import de.unisiegen.gtitool.core.storage.Element;
 import de.unisiegen.gtitool.core.storage.Modifyable;
 import de.unisiegen.gtitool.core.storage.Storable;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 
 
 /**
  * The {@link DefaultProductionWord} entity.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public final class DefaultProductionWord implements ProductionWord
 {
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = -7575345248390546325L;
 
 
   /**
    * The {@link EventListenerList}.
    */
   private EventListenerList listenerList = new EventListenerList ();
 
 
   /**
    * The offset of this {@link DefaultAlphabet} in the source code.
    * 
    * @see #getParserOffset()
    * @see #setParserOffset(ParserOffset)
    */
   private ParserOffset parserOffset = NO_PARSER_OFFSET;
 
 
   /**
    * The {@link ProductionWordMember} list.
    */
   private ArrayList < ProductionWordMember > productionWordMemberList;
 
 
   /**
    * The initial {@link ProductionWordMember} list.
    */
   private ArrayList < ProductionWordMember > initialProductionWordMemberList;
 
 
   /**
    * Allocates a new {@link DefaultProductionWord}.
    */
   public DefaultProductionWord ()
   {
     // ProductionWordMember
     this.productionWordMemberList = new ArrayList < ProductionWordMember > ();
     this.initialProductionWordMemberList = new ArrayList < ProductionWordMember > ();
   }
 
 
   /**
    * Allocates a new {@link DefaultProductionWord}.
    * 
    * @param element The {@link Element}.
    * @throws NonterminalSymbolException If something with the
    *           {@link NonterminalSymbol} is not correct.
    * @throws TerminalSymbolException If something with the
    *           {@link TerminalSymbol} is not correct.
    * @throws StoreException If the {@link Element} can not be parsed.
    */
   public DefaultProductionWord ( Element element )
       throws NonterminalSymbolException, TerminalSymbolException,
       StoreException
   {
     this ();
     // Check if the element is correct
     if ( !element.getName ().equals ( "ProductionWord" ) ) //$NON-NLS-1$
     {
       throw new IllegalArgumentException ( "element " + Messages.QUOTE //$NON-NLS-1$
           + element.getName () + Messages.QUOTE + " is not a production word" ); //$NON-NLS-1$
     }
 
     // Element
     for ( Element current : element.getElement () )
     {
       if ( current.getName ().equals ( "TerminalSymbol" ) ) //$NON-NLS-1$
       {
         add ( new DefaultTerminalSymbol ( current ) );
       }
       else if ( current.getName ().equals ( "NonterminalSymbol" ) ) //$NON-NLS-1$
       {
         add ( new DefaultNonterminalSymbol ( current ) );
       }
       else
       {
         throw new StoreException ( Messages
             .getString ( "StoreException.AdditionalElement" ) ); //$NON-NLS-1$
       }
     }
 
     // Attribute
     if ( element.getAttribute ().size () > 0 )
     {
       throw new StoreException ( Messages
           .getString ( "StoreException.AdditionalAttribute" ) ); //$NON-NLS-1$
     }
     this.initialProductionWordMemberList
         .addAll ( this.productionWordMemberList );
   }
 
 
   /**
    * Allocates a new {@link DefaultProductionWord}.
    * 
    * @param productionWordMembers The array of {@link ProductionWordMember}s.
    */
   public DefaultProductionWord (
       Iterable < ProductionWordMember > productionWordMembers )
   {
     this ();
     // ProductionWordMember
     if ( productionWordMembers == null )
     {
       throw new NullPointerException ( "production word members is null" ); //$NON-NLS-1$
     }
     add ( productionWordMembers );
     this.initialProductionWordMemberList
         .addAll ( this.productionWordMemberList );
   }
 
 
   /**
    * Allocates a new {@link DefaultProductionWord}.
    * 
    * @param productionWordMembers The array of {@link ProductionWordMember}s.
    */
   public DefaultProductionWord ( ProductionWordMember ... productionWordMembers )
   {
     this ();
     // ProductionWordMember
     if ( productionWordMembers == null )
     {
       throw new NullPointerException ( "production word members is null" ); //$NON-NLS-1$
     }
     add ( productionWordMembers );
     this.initialProductionWordMemberList
         .addAll ( this.productionWordMemberList );
   }
 
 
   /**
    * Appends the specified {@link ProductionWordMember}s to the end of this
    * {@link DefaultProductionWord}.
    * 
    * @param productionWordMembers The {@link ProductionWordMember}s to be
    *          appended to this {@link DefaultProductionWord}.
    */
   public final void add (
       Iterable < ProductionWordMember > productionWordMembers )
   {
     if ( productionWordMembers == null )
     {
       throw new NullPointerException ( "production word members is null" ); //$NON-NLS-1$
     }
     for ( ProductionWordMember current : productionWordMembers )
     {
       add ( current );
     }
   }
 
 
   /**
    * Appends the specified {@link ProductionWordMember} to the end of this
    * {@link DefaultProductionWord}.
    * 
    * @param productionWordMember The {@link ProductionWordMember} to be appended
    *          to this {@link DefaultProductionWord}.
    */
   public final void add ( ProductionWordMember productionWordMember )
   {
     // ProductionWordMember
     if ( productionWordMember == null )
     {
       throw new NullPointerException ( "production word member is null" ); //$NON-NLS-1$
     }
     this.productionWordMemberList.add ( productionWordMember );
   }
 
 
   /**
    * Appends the specified {@link ProductionWordMember}s to the end of this
    * {@link DefaultProductionWord}.
    * 
    * @param productionWordMembers The {@link ProductionWordMember}s to be
    *          appended to this {@link DefaultProductionWord}.
    */
   public final void add ( ProductionWordMember ... productionWordMembers )
   {
     if ( productionWordMembers == null )
     {
       throw new NullPointerException ( "production word members is null" ); //$NON-NLS-1$
     }
     for ( ProductionWordMember current : productionWordMembers )
     {
       add ( current );
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#addModifyStatusChangedListener(ModifyStatusChangedListener)
    */
   public final void addModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.add ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#clone()
    */
   @Override
   public final DefaultProductionWord clone ()
   {
     DefaultProductionWord newDefaultProductionWord = new DefaultProductionWord ();
     for ( ProductionWordMember current : this.productionWordMemberList )
     {
       newDefaultProductionWord.add ( current.clone () );
     }
     return newDefaultProductionWord;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Object#equals(Object)
    */
   @Override
   public final boolean equals ( Object other )
   {
     if ( other instanceof DefaultProductionWord )
     {
      DefaultProductionWord defaultWord = ( DefaultProductionWord ) other;
       return this.productionWordMemberList
          .equals ( defaultWord.productionWordMemberList );
     }
     return false;
   }
 
 
   /**
    * Let the listeners know that the modify status has changed.
    */
   @SuppressWarnings ( "unused" )
   private final void fireModifyStatusChanged ()
   {
     ModifyStatusChangedListener [] listeners = this.listenerList
         .getListeners ( ModifyStatusChangedListener.class );
     boolean newModifyStatus = isModified ();
     for ( ModifyStatusChangedListener current : listeners )
     {
       current.modifyStatusChanged ( newModifyStatus );
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Storable#getElement()
    */
   public final Element getElement ()
   {
     Element newElement = new Element ( "ProductionWord" ); //$NON-NLS-1$
     for ( ProductionWordMember current : this.productionWordMemberList )
     {
       newElement.addElement ( current );
     }
     return newElement;
   }
 
 
   /**
    * {@inheritDoc}
    */
   public final ParserOffset getParserOffset ()
   {
     return this.parserOffset;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#hashCode()
    */
   @Override
   public final int hashCode ()
   {
     return this.productionWordMemberList.hashCode ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#isModified()
    */
   public final boolean isModified ()
   {
     if ( !this.initialProductionWordMemberList
         .equals ( this.productionWordMemberList ) )
     {
       return true;
     }
     return false;
   }
 
 
   /**
    * Returns an iterator over the {@link ProductionWordMember}s in this
    * {@link DefaultProductionWord}.
    * 
    * @return An iterator over the {@link ProductionWordMember}s in this
    *         {@link DefaultProductionWord}.
    */
   public final Iterator < ProductionWordMember > iterator ()
   {
     return this.productionWordMemberList.iterator ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#removeModifyStatusChangedListener(ModifyStatusChangedListener)
    */
   public final void removeModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.remove ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#resetModify()
    */
   public final void resetModify ()
   {
     this.initialProductionWordMemberList.clear ();
     this.initialProductionWordMemberList
         .addAll ( this.productionWordMemberList );
   }
 
 
   /**
    * {@inheritDoc}
    */
   public final void setParserOffset ( ParserOffset parserOffset )
   {
     this.parserOffset = parserOffset;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see PrettyPrintable#toPrettyString()
    */
   public final PrettyString toPrettyString ()
   {
     PrettyString prettyString = new PrettyString ();
     if ( this.productionWordMemberList.size () == 0 )
     {
       prettyString.addPrettyToken ( new PrettyToken (
           "\u03B5", Style.TERMINAL_SYMBOL ) ); //$NON-NLS-1$
     }
     else
     {
       for ( ProductionWordMember current : this.productionWordMemberList )
       {
         prettyString.addPrettyPrintable ( current );
       }
     }
     return prettyString;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#toString()
    */
   @Override
   public final String toString ()
   {
     StringBuilder result = new StringBuilder ();
     if ( this.productionWordMemberList.size () == 0 )
     {
       result.append ( "\u03B5" ); //$NON-NLS-1$
     }
     else
     {
       for ( ProductionWordMember current : this.productionWordMemberList )
       {
         result.append ( current.getName () );
       }
     }
     return result.toString ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#toString()
    */
   public final String toStringDebug ()
   {
     StringBuilder result = new StringBuilder ();
     if ( this.productionWordMemberList.size () == 0 )
     {
       result.append ( "\u03B5" ); //$NON-NLS-1$
     }
     else
     {
       for ( ProductionWordMember current : this.productionWordMemberList )
       {
         result.append ( current.getName () );
         if ( current instanceof NonterminalSymbol )
         {
           result.append ( "{N:" + current.getParserOffset () + "}" ); //$NON-NLS-1$ //$NON-NLS-2$
         }
         else if ( current instanceof TerminalSymbol )
         {
           result.append ( "{T:" + current.getParserOffset () + "}" ); //$NON-NLS-1$ //$NON-NLS-2$
         }
         else
         {
           throw new RuntimeException ( "unknown member" ); //$NON-NLS-1$
         }
       }
     }
     return result.toString ();
   }
 }
