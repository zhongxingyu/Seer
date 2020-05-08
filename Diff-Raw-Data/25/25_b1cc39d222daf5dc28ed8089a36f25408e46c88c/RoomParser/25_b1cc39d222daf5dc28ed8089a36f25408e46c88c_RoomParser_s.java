 /*
  * MiningContest
  * Copyright (C) 2011 IndiPlex
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.indiplex.miningcontest.generator;
 
 import de.indiplex.miningcontest.map.MapChunk;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.EnumMap;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  *
  * @author IndiPlex <Cartan12@indiplex.de>
  */
 public class RoomParser {
 
     private static RoomParser instance;
     private EnumMap<MapChunk.Type, Room> rooms = new EnumMap<MapChunk.Type, Room>(MapChunk.Type.class);
 
     private RoomParser() {
     }
 
     private static RoomParser getInstance() {
         if (instance == null) {
             instance = new RoomParser();
         }
         return instance;
     }
 
     public static void main(String[] args) throws IOException, InterruptedException {
         Process exec = Runtime.getRuntime().exec(new String[] {"C:/Program Files (x86)/Git/bin/git", "clone", "lolol"});
         copy(exec.getInputStream(), System.out);
         copy(exec.getErrorStream(), System.err);
         System.out.println(exec.waitFor());
     }
     private static void copy(InputStream in, OutputStream out) throws IOException {
         int i = in.read();
         while (i!=-1) {
             out.write(i);
             i = in.read();
         }
     }
 
     public static Room parseRoom(MapChunk.Type type) {
         try {
             RoomParser rp = getInstance();
             if (rp.rooms.get(type) != null) {
                 return rp.rooms.get(type);
             }
 
             XMLReader xmlReader = XMLReaderFactory.createXMLReader();
             RoomContentHandler roomContentHandler = new RoomContentHandler();
             xmlReader.setContentHandler(roomContentHandler);
            xmlReader.parse(new InputSource(new FileInputStream("plugins/IndiPlex Manager/config/Mining Contest/res/" + type.toString().toLowerCase() + ".xml")));
             Room room = roomContentHandler.getRoom();
             rp.rooms.put(type, room);
             return room;
         } catch (SAXException ex) {
             ex.printStackTrace();
             return null;
         } catch (IOException ex) {
             ex.printStackTrace();
             return null;
         }
     }
 }
