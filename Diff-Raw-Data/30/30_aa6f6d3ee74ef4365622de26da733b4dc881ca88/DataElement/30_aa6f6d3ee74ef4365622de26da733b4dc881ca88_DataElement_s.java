 /* 
  * surveyforge-core - Copyright (C) 2006 OPEN input - http://www.openinput.com/
  *
  * This program is free software; you can redistribute it and/or modify it 
  * under the terms of the GNU General Public License as published by the 
  * Free Software Foundation; either version 2 of the License, or (at your 
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. 
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along 
  * with this program; if not, write to 
  *   the Free Software Foundation, Inc., 
  *   59 Temple Place, Suite 330, 
  *   Boston, MA 02111-1307 USA
  *   
  * $Id$
  */
 package org.surveyforge.core.metadata;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.IndexColumn;
 
 // TODO Elaborate on comments
 /**
  * @author jsegura
  */
 @Entity
 public class DataElement implements Serializable
   {
   private static final long serialVersionUID  = 0L;
 
   @Id
   @Column(length = 50)
   @GeneratedValue(generator = "system-uuid")
   @GenericGenerator(name = "system-uuid", strategy = "uuid")
   private String            id;
   /** Version for optimistic locking. */
   @javax.persistence.Version
   private int               lockingVersion;
 
   /**
    * This is a unique and language independent identifier for the data element. The identifier is unique among all other data elements
    * for an object variable (standard data element) or within the scope of a statistical activity.
    */
   @Column(unique = true, length = 50)
   private String            identifier;
   /**  */
   private boolean           multiple          = false;
   /**  */
   private int               maxResponses      = 1;
   /**  */
  @ManyToOne
   @JoinColumn(name = "valueDomain_id")
   private ValueDomain       valueDomain;
   /**  */
   @ManyToOne(cascade = {CascadeType.ALL})
   @JoinColumn(name = "variableStructure_id", insertable = false, updatable = false)
   private DataElement       variableStructure;
   /**  */
   @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
   @IndexColumn(name = "componentElementsIndex")
   @JoinColumn(name = "variableStructure_id")
   private List<DataElement> componentElements = new ArrayList<DataElement>( );
 
 
   private DataElement( )
     {}
 
   /**
    * @param valueDomain
    * @param identifier
    */
   public DataElement( ValueDomain valueDomain, String identifier )
     {
     this.setValueDomain( valueDomain );
     this.setIdentifier( identifier );
     }
 
   /**
    * @return Returns the identifier.
    */
   public String getIdentifier( )
     {
     return this.identifier;
     }
 
   /**
    * @param identifier The identifier to set.
    */
   public void setIdentifier( String identifier )
     {
     if( identifier != null && !identifier.equals( "" ) )
       this.identifier = identifier;
     else
       throw new NullPointerException( );
     }
 
   /**
    * @return Returns the multiple.
    */
   public boolean isMultiple( )
     {
     return this.multiple;
     }
 
   /**
    * @param multiple The multiple to set.
    */
   public void setMultiple( boolean multiple )
     {
     this.multiple = multiple;
     }
 
   /**
    * @return Returns the maxResponses.
    */
   public int getMaxResponses( )
     {
     return this.maxResponses;
     }
 
   /**
    * @param maxResponses The maxResponses to set.
    */
   public void setMaxResponses( int maxResponses )
     {
     if( maxResponses >= 1 )
       this.maxResponses = maxResponses;
     else
       throw new IllegalArgumentException( );
     }
 
   /**
    * @return Returns the valueDomain.
    */
   public ValueDomain getValueDomain( )
     {
     return this.valueDomain;
     }
 
   /**
    * @param valueDomain The valueDomain to set.
    */
   public void setValueDomain( ValueDomain valueDomain )
     {
     if( valueDomain != null )
       this.valueDomain = valueDomain;
     else
       throw new NullPointerException( );
     }
 
 
   /**
    * @return Returns the variableStructure.
    */
   public DataElement getVariableStructure( )
     {
     return this.variableStructure;
     }
 
   /**
    * @param variableStructure The variableStructure to set.
    */
   public void setVariableStructure( DataElement variableStructure )
     {
     if( this.variableStructure != null ) this.variableStructure.removeComponentElement( this );
     this.variableStructure = variableStructure;
     if( this.variableStructure != null ) this.variableStructure.addComponentElement( this );
     }
 
   /**
    * @return Returns the componentElements.
    */
   public List<DataElement> getComponentElements( )
     {
     return Collections.unmodifiableList( this.componentElements );
     }
 
   /**
    * @param componentElements The componentElements to set.
    */
   private void addComponentElement( DataElement componentElement )
     {
     if( componentElement != null )
       this.componentElements.add( componentElement );
     else
       throw new NullPointerException( );
     }
 
   /**
    * @param componentElements The componentElements to set.
    */
   private void removeComponentElement( DataElement componentElement )
     {
     if( componentElement != null )
       this.componentElements.remove( componentElement );
     else
       throw new NullPointerException( );
     }
 
   @Override
   public boolean equals( Object object )
     {
     DataElement other = (DataElement) object;
     return this.getIdentifier( ).equals( other.getIdentifier( ) );
     }
 
   @Override
   public int hashCode( )
     {
     return this.getIdentifier( ).hashCode( );
     }
 
   }
