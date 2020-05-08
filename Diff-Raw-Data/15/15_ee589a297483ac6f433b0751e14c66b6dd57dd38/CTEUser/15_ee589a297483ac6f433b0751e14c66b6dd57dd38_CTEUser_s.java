 package user;
 
 import java.lang.Comparable;
 import java.lang.ClassCastException;
 import java.awt.Color;
 import java.net.InetAddress;
 import handler.*;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.lang.Cloneable;
 import java.lang.CloneNotSupportedException;
 
 /**
  * A User of a Collaborative Text Editor
  */
 public class CTEUser implements Comparable, Serializable, Cloneable {
 
     private String _name;
     private Color _cursorColor;
     private TextPosition _cursorPosition;
     private InetAddress _IPAddress;
     private String _uniqueID;
 
     /**
      * Constructor for clone.
      * @Requires
      * String userID != null;
      * Color cursorColor != null;
      * InetAddress IPAdress != null;
      */
     public CTEUser ( String name, InetAddress IPAddress, Color cursorColor, String uniqueID ) throws InvalidUserIDException {
         if(name == "") throw new InvalidUserIDException();
         _cursorPosition = new TextPosition();
         _name = name;
         _cursorColor = cursorColor;
         _IPAddress = IPAddress;
         _uniqueID = uniqueID;
     }
 
     /**
      * Normal Constructor.
      * @Requires
      * String userID != null;
      * Color cursorColor != null;
      * InetAddress IPAdress != null;
      */
     public CTEUser ( String name, InetAddress IPAddress, Color cursorColor ) throws InvalidUserIDException {
         if(name == "") throw new InvalidUserIDException();
         _cursorPosition = new TextPosition();
         _name = name;
         _cursorColor = cursorColor;
         _IPAddress = IPAddress;
        _uniqueID = "" + System.currentTimeMillis();
     }
 
     /*
      * Set the color for this CTEUser.
      * @Requires
      * cursorColor != null
      *  
      * @Ensures
      * _cursorColor == cursorColor      
      */
     public void setCursorColor ( Color cursorColor ) { _cursorColor = cursorColor; }
     
     /***********
      * Queries *
      ***********/
 
 
     /**
      * A unique ID for this User.
      */
     public String getUniqueID ( ) { return _uniqueID; }
 
     /**
      * The InetAddress of this User.
      */
     public InetAddress getIPAddress ( ) { return _IPAddress; }
 
     /**
      * The user Name.
      * @Ensures
      *      Result.length() > 0
      */
     public String getName ( ) { return _name; }
 
 
     /*
      * The position of the cursor.
      * @Ensures
      *      Result != null
      */
     public TextPosition getPosition ( ) { return _cursorPosition; }
 
 
     /************
      * Commands *
      ************/
 
     /**
      * Set the position of this User.
      * @Requires
      *      Tex)Position != null
      */
     public void setPosition ( TextPosition position ) throws OutOfBoundsException { _cursorPosition = position; }
 
     /**
      * Set the name of this User.
      *
      */
     public void setName ( String name ) throws InvalidUserIDException {
         if(name == "") throw new InvalidUserIDException();
         _name= name;
     }
 
     /**
      * The cursor color.
      * @Ensures
      *      Result != null
      */
     public Color getCursorColor ( ) { return _cursorColor; }
 
     /**
      * A String representation of this User.
      */
     public String toString ( ) {
         String returnString = _name + "{ id: " + _uniqueID + " pos: ["+ getPosition().getPosition() + "] }";
         return returnString;
     }
 
     private void writeObject ( ObjectOutputStream out ) throws IOException {
         ObjectOutputStream.PutField fields = out.putFields();
         fields.put("_uniqueID", _uniqueID);
         fields.put("_name", _name);
         fields.put("_cursorColor", _cursorColor);
         fields.put("_cursorPosition", _cursorPosition);
         fields.put("_IPAddress", _IPAddress);
         out.writeFields();
     }
 
     private void readObject ( ObjectInputStream in ) throws IOException, ClassNotFoundException {
         ObjectInputStream.GetField fields = in.readFields();
         _uniqueID = (String) fields.get("_uniqueID", null);
         _name = (String) fields.get("_name", null);
         _cursorColor = (Color) fields.get("_cursorColor", null);
         _cursorPosition = (TextPosition) fields.get("_cursorPosition", null);
         _IPAddress = (InetAddress) fields.get("_IPAddress", null);
     }
 
     @Override
     public Object clone ( ) throws CloneNotSupportedException {
         CTEUser clone = null;
         try {
             clone = new CTEUser(_name, _IPAddress, _cursorColor, _uniqueID);
             TextPosition clonedPosition = (TextPosition) _cursorPosition.clone();
             clone.setPosition(clonedPosition);
         }
         catch (InvalidUserIDException iuede) { iuede.printStackTrace(); }
         catch (OutOfBoundsException oobe) { oobe.printStackTrace(); }
 
         return clone;
     }
 
     @Override
     public boolean equals ( Object other ) {
         if (other == null) { return false; }
         if (other == this) { return true; }
         if (other.getClass() != this.getClass()) { return false; }
 
         CTEUser otherUser = (CTEUser) other;
 
         return _name.equals(otherUser.getName())
             && _uniqueID.equals(otherUser.getUniqueID());
     }
 
     @Override
     public int compareTo ( Object other ) {
         if (!(other instanceof CTEUser)) { throw new ClassCastException("Can only compare to another CTEUser."); }
 
         CTEUser otherUser = (CTEUser) other;
 
         return _cursorPosition.compareTo(otherUser.getPosition());
     }
 
     @Override
     public int hashCode ( ) {
         String result = "" + _uniqueID + _name;
         return result.hashCode();
     }
 }
