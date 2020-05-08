 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 import java.io.*;
 
 public abstract class Node implements Observer {
     int m_nbFields;
     Field [] m_field;
     Node m_next;
     AppearanceContext m_ac;
     Region m_region;
     int m_type;
     boolean m_isVideo; // needed for overlay
     boolean m_isUpdated; // can be set to true when the node has been updated
     private int m_updatedTime; // time when the node was first updated
     TouchSensor m_sensor = null; // the sensor associated to the group of nodes (should be in Group, except for Switch) 
     private boolean m_isVisible; //MCP: true if Node is in the screen bounds 
     Node (int n) {
         m_nbFields = n;
         m_field = new Field [n];
         //m_region = new Region ();
         m_isUpdated = true;
         m_isVideo = false;
         m_isVisible = false;
     }
     
     static Node decode (DataInputStream is, Node [] table, Decoder decoder) {
         int type = -1;
         int defID = -1;
         int nodeID = -1;
         String protoName;
         Node node = null;
         try {
             type = Decoder.readUnsignedByte(is);
             //System.out.println ("Node.decode: type: "+type);
             switch (type) {
             case 10: // DEF id ProtoDecl
                 defID = is.readInt ();
             case 9: // ProtoDecl
                 protoName = Decoder.readString (is);
                 node = decoder.createProto (protoName, table, is);
                 if (node != null && defID != -1) {
                     table [defID] = node;
                 }
                 break;
             case 4: // USE id
                 defID = is.readInt ();
                 return getNodeByID (defID, table);
             case 2: // DEF id nodeID
                 defID = is.readInt ();
             case 1: // nodeID
                 nodeID = is.read ();
                 node = create (nodeID);
                 if (node != null) {
                     node.read (is, table, decoder);
                     if (defID != -1) {
                         table [defID] = node;
                     }
                 }
                 break;
             case 0: // end of MFNode
                 return (null);
             default:
                 Logger.println ("Node.decode: unknown type: "+type);
             }
         } catch (Exception e) {
             System.out.println ("Node.decode: "+type+"/"+nodeID+" : "+e);
         }
         //System.out.println ("Node.decode: "+type+"/"+nodeID+" : "+node);
         return (node);
     }
     
     Field getFieldByID (int id) {
         return (id > -1 && id < m_nbFields ? m_field [id] : null);
     }
 
     static Node getNodeByID (int defID, Node [] table) {
         return (defID >= 0 && defID < 256 ? table [defID] : null);
     }
 
     static Node create (int id) {
         //Logger.println ("Node.create: ID: "+id);
         switch (id) {
         case NodeTable.Anchor : return new Anchor ();
         case NodeTable.Appearance : return new Appearance ();
         case NodeTable.Bitmap : return new Bitmap ();
         case NodeTable.ColorInterpolator : return new ColorInterpolator ();
         case NodeTable.Color : return new Color ();
         case NodeTable.CompositeTexture2D : return new CompositeTexture2D ();
         case NodeTable.Coordinate2D : return new Coordinate2D ();
         case NodeTable.CoordinateInterpolator2D : return new CoordinateInterpolator2D ();
         case NodeTable.Circle : return new Circle ();
         case NodeTable.FontStyle : return new FontStyle ();
         case NodeTable.Group : return new Group ();
         case NodeTable.ImageTexture : return new ImageTexture ();
         case NodeTable.IndexedFaceSet2D : return new IndexedFaceSet2D ();
         case NodeTable.IndexedLineSet2D : return new IndexedLineSet2D ();
         case NodeTable.Inline : return new Inline ();
         case NodeTable.InputSensor : return new InputSensor ();
         case NodeTable.KeySensor : return new KeySensor ();
         case NodeTable.Layer2D : return new Layer2D ();
         case NodeTable.Material2D : return new Material2D ();
         case NodeTable.MediaSensor : return new MediaSensor ();
         case NodeTable.Message : return new Message ();
         case NodeTable.Style : return new Style ();
 //#ifdef MM.namespace
         case NodeTable.Namespace : return new Namespace ();
 //#endif
         case NodeTable.OrderedGroup : return new OrderedGroup ();
         case NodeTable.PositionInterpolator2D : return new PositionInterpolator2D ();
         case NodeTable.Rectangle : return new Rectangle ();
         case NodeTable.Script : return new Script ();
         case NodeTable.Shape : return new Shape ();
         case NodeTable.Switch : return new Switch ();
         case NodeTable.Text : return new Text ();
         case NodeTable.ScalarInterpolator : return new ScalarInterpolator ();
         case NodeTable.TimeSensor : return new TimeSensor ();
         case NodeTable.Transform2D : return new Transform2D ();
         case NodeTable.WrapText : return new WrapText ();
         case NodeTable.TouchSensor : return new TouchSensor ();
//#ifdef api.richText
         case NodeTable.RichText : return new RichText ();
 //#endif
         
 //#ifdef api.mm
         case NodeTable.AudioClip : return new AudioClip ();
         case NodeTable.MediaControl : return new MediaControl ();
         case NodeTable.MovieTexture : return new MovieTexture ();
         case NodeTable.Recordtexture : return new RecordTexture ();
 //#ifdef MM.Upload
         case NodeTable.Upload : return new Upload ();
 //#endif
         case NodeTable.MotionSensor : return new MotionSensor ();
         case NodeTable.Sound2D : return new Sound2D ();
 //#endif
         
         }
         Logger.println ("Node.create: unknown ID: "+id);
         return (null);
     }
 
     void setField (int id, Field f) {
         if (m_field.length <= id) {
             Field [] tmp = new Field [id+5];
             System.arraycopy(m_field, 0, tmp, 0, m_nbFields);
             m_field = tmp;
         }
         m_field[id] = f;
     }
 
     void read (DataInputStream dis, Node [] table, Decoder decoder) {
         try {
             int id = dis.readByte ();
             while (id != 0) {
                 if (id == -4) { // number fields for the actual script or proto  follows 
                     int nb = dis.readByte ();
                     if (nb < 0) { nb = 256 + nb; }
                     Field [] tmp = new Field [nb];
                     System.arraycopy(m_field, 0, tmp, 0, m_nbFields);
                     m_field = tmp;
                     id = dis.readByte ();
                     continue;
                 } else if (id == -1) { // dynamic field: new field in a Script node
                     id = dis.readByte () -1;
                     int type = dis.readByte ();
                     //System.err.println ("Node.read: DYN field #"+id+"/"+type);
                     setField (id, Field.createFieldById (type));
                     m_nbFields++;
                 } else if (id == -2) { // field is IS
                     id = dis.readByte ()-1;
                     int protoId = dis.readByte ()-1;
                     //System.err.println ("Node.read: IS field #"+id+"/"+protoId);
                     replaceField (id, decoder.getCurrentProto().m_field[protoId]);
                     id = dis.readByte ();
                     continue;
                 } else if (id == -3) { // Script field *and* IS
                     id = dis.readByte ()-1;
                     int protoId = dis.readByte ()-1;
                     //System.err.println ("Node.read: DYN and IS field #"+id+"/"+protoId);
                     setField (id, decoder.getCurrentProto().m_field[protoId]);
                     m_nbFields++;
                     id = dis.readByte ();
                     continue;
                 } else {
                     id--;
                 }
                 if (id >= 0 && id < m_nbFields && m_field[id] != null) {
                     m_field [id].decode (dis, table, decoder);
                 } else {
                     Logger.println ("Node.read: error while creating field #"+ id+" for "+this+" ("+m_nbFields+")");
                 }
                 id = dis.readByte ();
             }
             //init ();
         } catch (Exception e) {
             System.out.println ("####### Node.read: Exception "+e);
             e.printStackTrace ();
         }
     }
 
     //for material and texture
     boolean isTransparent () { return false; }
 
     void activate (Context c, Event e, int time) {
         //System.err.println ("Unexpected call to Node.activate");
         //new Exception ("ARG").printStackTrace ();
     }
 
     // start the rendering of the node
     // This function is called because the compose method called c.addRenderNode (this)
     void render (Context c) { ; }
     
     // start the composition of the node:
     // return true if the node has been updated and need to be redrawn and  
     // the clip parameter must be set with the area that is updated
     boolean compose (Context c, Region clip, boolean forceUpdate) { return false; }
     
     // called whenever the node is just "brought to life"
     void start (Context c) { }
 
     // called whenever the node will not be active anymore
     void stop (Context c) { }
 
     // called whenever the node has to be destroyed => can free all memory EXPLICITELY
     void destroy (Context c) {}
 
 //#ifdef MM.pause
     // called whenever MyCanvas wants to sleep and node has registered as a sleepy node
     // returns SLEEP_CANCELED, SLEEP_FOREVER or sleep until given time
     // Warning: this call is made out of any context: restrict to node local data !
     int getWakeupTime (int time) { return MyCanvas.SLEEP_FOREVER; }
 //#endif
     
     public void fieldChanged (Field field) { }
 
     public void replaceField (int idx, Field newField) {
         //Logger.println ("Node.replaceField "+this+", replacing field "+idx);
         if (m_field [idx].removeObserver (this)) {
             newField.addObserver (this);
         }
         m_field[idx] = newField;
     }
 
     public boolean regionIntersects (Region clip) {
         return m_region.intersects (clip.x0-1, clip.y0-1, clip.x1+1, clip.y1+1);
     }
 
     /**
      * Prevent adding this node to render list when out of bounds !
      * Warning: The m_isUpdated is set to false when node is off bound.
      * @param clip Clip will only be updated with the old clip when 
      *             the object goes out of bounds.
      * @param bounds Bounds are set by the size of the screen or the Layer2D nodes. 
      *               If null, isVisible() will always consider node as not visible.
      * @return true if the node is visible 
      */
     protected boolean isVisible (Region clip, Region bounds) {
         if (bounds != null && m_region.intersects(bounds)) { 
             m_isVisible = true;
         } else {
             if (m_isVisible) {
                 m_isVisible = false;
                 // When node is not visible anymore, add its old region to clip 
                 // to clean its previous position on screen
                 clip.add(m_ac.m_oldRegion);
             }
         }
         return m_isVisible;
     }
     
     /**
      * Nodes can be composed multiple times because of reuse
      * So each time its fields gets updated, it must update itself for each reuse !
      */
     protected boolean isUpdated (boolean forceUpdate) {
         if (m_isUpdated) {
             m_updatedTime = Context.time;
             m_isUpdated = false;
             return true;
         } else if (m_updatedTime == Context.time) {
             return true;
         }
         return forceUpdate;
     }
 }
