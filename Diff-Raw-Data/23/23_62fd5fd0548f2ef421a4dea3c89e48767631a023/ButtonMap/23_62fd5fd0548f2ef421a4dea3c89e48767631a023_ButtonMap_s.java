 package com.hifiremote.jp1;
 
 import java.util.*;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class ButtonMap.
  */
 public class ButtonMap
 {
   
   /**
    * Instantiates a new button map.
    * 
    * @param num the num
    * @param keyCodes the key codes
    */
   public ButtonMap( int num, short[][] keyCodes )
   {
     number = num;
     keyCodeList = keyCodes;
   }
 
   /**
    * Gets the number.
    * 
    * @return the number
    */
   public int getNumber(){ return number; }
   
   /**
    * Gets the key code list.
    * 
    * @return the key code list
    */
   public short[][] getKeyCodeList(){ return keyCodeList; }
   
   /**
    * Sets the buttons.
    * 
    * @param remote the remote
    * 
    * @return the button map
    */
   public ButtonMap setButtons( Remote remote )
   {
     size = 0;
 
     buttons = new Button[ keyCodeList.length ][];
 
     for ( int i = 0; i < keyCodeList.length; i++ )
     {
       short[] keyCodes = keyCodeList[ i ];
       Button[] inner = new Button[ keyCodes.length ];
       buttons[ i ] = inner;
       size += keyCodes.length;
       for ( int j = 0; j < keyCodes.length; j++ )
       {
         short keyCode = keyCodes[ j ];
         Button button = remote.getButton( keyCode );
         if ( button == null )
         {
           System.err.println( "ERROR: ButtonMap " + number + " includes unknown keycode $" +
                               Integer.toHexString( keyCode & 0xFF ) +
                               ", Creating button!" );
           String name = "button" + Integer.toHexString( keyCode & 0xFF ).toUpperCase();
           button = new Button( name, name, keyCode, remote );
           if ( button.getIsShifted())
           {
             Button baseButton = remote.getButton( keyCode & 0x3F );
             if ( baseButton != null )
             {
               button.setBaseButton( baseButton );
               baseButton.setShiftedButton( button );
             }
           }
           else if ( button.getIsXShifted())
           {
             Button baseButton = remote.getButton( keyCode & 0x3F );
             if ( baseButton != null )
             {
               button.setBaseButton( baseButton );
               baseButton.setXShiftedButton( button );
             }
           }
           remote.addButton( button );
         }
         button.addButtonMap( number );
         inner[ j ] = button;
       }
     }
     return this;
   }
 
   /**
    * Checks if is present.
    * 
    * @param b the b
    * 
    * @return true, if is present
    */
   public boolean isPresent( Button b )
   {
     if ( b == null )
       return false;
     return b.inButtonMap( number );
   }
 
   /**
    * Gets the.
    * 
    * @param index the index
    * 
    * @return the button
    */
   public Button get( int index )
   {
     Button rc = null;
     int offset = 0;
     for ( int i = 0; i < buttons.length; i++ )
     {
       Button[] inner = buttons[ i ];
       if ( index  < ( offset + inner.length ))
       {
         rc = inner[ index - offset ];
         break;
       }
       else
         offset += inner.length;
     }
     return rc;
   }
 
   /**
    * Size.
    * 
    * @return the int
    */
   public int size(){ return size; }
 
   /**
    * Parses the bit map.
    * 
    * @param bitMap the bit map
    * @param offset the offset
    * @param digitMapUsed the digit map used
    * 
    * @return the list< button>
    */
   public List< Button > parseBitMap( short[] bitMap, int offset, boolean digitMapUsed )
   {
     List< Button > rc = new ArrayList< Button >();
     int mask = 0x80;
     for ( int i = 0; i < buttons.length; i++ )
     {
      if ( bitMap[ offset ] == 1 )
        return rc;

       boolean useIt = (( bitMap[ offset ] & mask ) != 0 );
       if ( useIt )
       {
         Button[] inner = buttons[ i ];
         for ( int j = 0; j < inner.length; j++ )
           rc.add( inner[ j ]);
       }
       mask >>= 1;
       if ( mask == 1 )
       {
        mask = 0x80;
        offset++;
       }
     }
     return rc;
   }
 
   /**
    * To bit map.
    * 
    * @param digitMapUsed the digit map used
    * @param keyMovesOnly the key moves only
    * @param assignments the assignments
    * 
    * @return the short[]
    */
   public short[] toBitMap( boolean digitMapUsed, boolean keyMovesOnly, ButtonAssignments assignments )
   {
     int len = ( buttons.length + 6 )/ 7;
     if ( len == 0 )
       return new short[ 0 ];
     short[] rc = new short[ len ];
     int index = 0;
     short temp = 0x80;
     int limit = 0;
     for ( int i = 0 ; i < buttons.length; i++ )
     {
       Button[] inner = buttons[ i ];
       for ( int j = 0; j < inner.length; j++ )
       {
         Function func = null;
         if ( inner[ j ] != null )
 //          func = assignments.getAssignment( inner[ j ], inner[ j ].getState());
           func = assignments.getAssignment( inner[ j ], Button.NORMAL_STATE );
         if ( digitMapUsed && ( i == 0 ))
           func = null;
         if ( keyMovesOnly )
           func = null;
         if (( func != null ) &&
              !func.isExternal() &&
             ( func.getHex() != null ))
         {
           rc[ index ] |= temp;
           limit = index;
           break;
         }
       }
 
       temp >>= 1;
       if (temp == 1)
       {
         temp = 0x80;
         ++index;
       }
     }
     rc[ limit++ ] |= 1;
     short[] result = new short[ limit ];
     System.arraycopy(rc,0,result,0,limit);
     return result;
   }
 
   /**
    * To command list.
    * 
    * @param digitMapUsed the digit map used
    * @param keyMovesOnly the key moves only
    * @param assignments the assignments
    * 
    * @return the short[]
    */
   public short[] toCommandList( boolean digitMapUsed, boolean keyMovesOnly, ButtonAssignments assignments )
   {
     int count = 0;
     int funcLen = 0;
     boolean[] flags = new boolean[ buttons.length ];
     for ( int i = 0; i < buttons.length; i++ )
     {
       Button[] inner = buttons[ i ];
       flags[ i ] = false;
       for ( int j = 0; j < inner.length; j++ )
       {
         Function func = assignments.getAssignment( inner[ j ], Button.NORMAL_STATE );
         if ( digitMapUsed && ( i == 0 ))
           func = null;
         if ( keyMovesOnly )
           func = null;
         if (( func != null ) &&
             ( func.getClass() != ExternalFunction.class ) &&
             ( func.getHex() != null ))
         {
           funcLen = func.getHex().length();
           flags[ i ] = true;
           count += ( inner.length * funcLen );
           break;
         }
       }
     }
     short[] rc = new short[ count ];
     short[] zeros = new short[ funcLen ];
     int index = 0;
     for ( int i = 0; i < buttons.length; i++ )
     {
       Button[] inner = buttons[ i ];
       if ( flags[ i ] )
       {
         for ( int j = 0; j < inner.length; j++ )
         {
           short[] hex = null;
           Function func = assignments.getAssignment( inner[ j ], Button.NORMAL_STATE );
           if ( digitMapUsed && ( i == 0 ))
             func = null;
           if (  keyMovesOnly )
             func = null;
           if (( func == null ) ||
               ( func.getClass() == ExternalFunction.class ) ||
               ( func.getHex() == null ))
             hex = zeros;
           else
             hex = func.getHex().getData();
           System.arraycopy( hex, 0, rc, index, funcLen );
           index += funcLen;
         }
       }
     }
     return rc;
   }
 
   /** The number. */
   private int number;
   
   /** The size. */
   private int size = 0;
   
   /** The key code list. */
   private short[][] keyCodeList;
   
   /** The buttons. */
   private Button[][] buttons;
 }
