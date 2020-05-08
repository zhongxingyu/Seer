 package com.hifiremote.jp1;
 
 import java.util.Properties;
 
 public class Function
 {
   public Function(){}
 
   public Function( String name, byte[] data, String notes )
   {
     this.name = name;
     this.hexData = data;
     this.notes = notes;
   }
 
   public Function( String name )
   {
     this.name = name;
   }
 
   public boolean isExternal(){ return false; }
 
   public void store( Properties props, String prefix )
   {
    props.setProperty( prefix + ".name", name );
     if ( hexData != null )
       props.setProperty( prefix + ".hex", Protocol.hex2String( hexData ));
     if ( notes != null )
       props.setProperty( prefix + ".notes", notes );
   }
 
   public void load( Properties props, String prefix )
   {
     System.err.println( "Function.load()" );
     String str = props.getProperty( prefix + ".name" );
     System.err.println( prefix + ".name=" + str );
     if ( str != null )
       setName( str );
     str = props.getProperty( prefix + ".hex" );
     System.err.println( prefix + ".hex=" + str );
     if ( str != null )
       setHex( Protocol.string2hex( str ));
     str = props.getProperty( prefix + ".notes" );
     System.err.println( prefix + ".notes=" + str );
     if ( str != null )
       setNotes( str );
   }
 
   public Function setName( String name )
   {
     this.name = name;
     if ( label != null )
       label.setText( name );
     return this;
   }
 
   public Function setNotes( String notes )
   {
     this.notes = notes;
     return this;
   }
 
   public Function setHex( byte[] values )
   {
     hexData = values;
     return this;
   }
 
   public String toString()
   {
     return name;
   }
   public String getName(){ return name; }
   public String getNotes(){ return notes; }
   public byte[] getHex(){ return hexData; }
 
   public FunctionLabel getLabel()
   {
     if ( label == null )
     {
       label = new FunctionLabel( this );
       if ( assigned())
         label.showAssigned();
     }
     return label;
   }
 
   public void addReference()
   {
     ++refCount;
     if ( label != null )
       label.showAssigned();
   }
 
   public void removeReference()
   {
     if ( refCount > 0 )
       --refCount;
     if (( refCount == 0 ) && ( label != null ))
       label.showUnassigned();
   }
 
   public boolean assigned()
   {
     return ( refCount > 0 );
   }
 
   protected String name = null;
   protected String notes = null;
   protected byte[] hexData = null;
   private FunctionLabel label = null;
   private int refCount = 0;
 }
