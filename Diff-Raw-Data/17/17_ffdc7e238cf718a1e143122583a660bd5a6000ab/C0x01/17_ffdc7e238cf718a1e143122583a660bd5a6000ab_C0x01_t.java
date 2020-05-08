 package amber.data.map.codec;
 
 import amber.Amber;
 import amber.data.res.Tileset;
 import amber.data.res.Tileset.TileSprite;
 import amber.data.io.ByteStream;
 import amber.data.map.Direction;
 import static amber.data.map.Direction.*;
 import amber.data.map.InvalidMapException;
 import amber.data.map.Layer;
 import amber.data.map.Layer3D;
 import amber.data.map.LevelMap;
 import amber.data.map.LevelMap.Type;
 import static amber.data.map.LevelMap.Type.*;
 import amber.data.map.Tile;
 import amber.data.map.Tile3D;
 import amber.data.map.Tile3D.Angle;
 import static amber.data.map.Tile3D.Angle.*;
 import amber.data.map.TileModel;
 import static amber.data.map.codec.C0x01.Opcode1.*;
 import amber.data.res.Resource;
 import amber.data.sparse.SparseMatrix;
 import amber.data.sparse.SparseVector;
 import amber.gl.model.obj.WavefrontObject;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 
 /**
  *
  * @author Tudor
  */
 public class C0x01 extends Codec {
 
     public interface Opcode1 {
 
         short MAP_MAGIC = 0x1337;
         byte MAP_2D = 0x01;
         byte MAP_3D = 0x02;
         byte TAG_TILESHEET = 0x01;
         byte TAG_AUDIO = 0x02;
         byte TAG_MODEL = 0x03;
         byte MATRIX_SPARSE = 0x01;
         byte MATRIX_DENSE = 0x02;
         byte DIRECTION_NORTH = 0x01;
         byte DIRECTION_EAST = 0x02;
         byte DIRECTION_SOUTH = 0x03;
         byte DIRECTION_WEST = 0x04;
         byte ANGLE_180 = 0x01;
         byte ANGLE_90 = 0x02;
         byte ANGLE_45 = 0x03;
     }
 
     @Override
     public LevelMap loadMap(DataInputStream buffer) throws IOException {
         if (buffer.readShort() != MAP_MAGIC) {
             throw new InvalidMapException("magic number != " + MAP_MAGIC);
         }
         byte bt = buffer.readByte();
         if (bt != MAP_2D && bt != MAP_3D) {
             throw new InvalidMapException("invalid map type");
         }
         Type type = bt == MAP_2D ? _2D : _3D;
         LevelMap map = new LevelMap(buffer.readShort(), buffer.readShort(), type);
 
         HashMap<Short, Tileset.TileSprite> sprites = new HashMap<Short, Tileset.TileSprite>();
         HashMap<Short, WavefrontObject> models = new HashMap<Short, WavefrontObject>();
         int constantsCount = buffer.readShort();
         for (int i = 0; i != constantsCount; i++) {
             byte tag;
             switch (tag = buffer.readByte()) {
                 case TAG_TILESHEET:
                     sprites.put(buffer.readShort(), TileSprite.byId(buffer.readUTF()));
                     break;
                 case TAG_MODEL:
                     short id = buffer.readShort();
                     String name = buffer.readUTF();
                     for (Resource<WavefrontObject> e : Amber.getResourceManager().getModels()) {
                         if (e.getName().equals(name)) {
                             models.put(id, e.get());
                         }
                     }
                     break;
                 case TAG_AUDIO:
                     throw new InvalidMapException("tag not implemented yet");
                 default:
                     throw new InvalidMapException("invalid tag " + tag + " at index " + i);
             }
         }
 
         short layersCount = buffer.readShort();
         int width, height;
         for (int i = 0; i != layersCount; i++) {
             String name = buffer.readUTF();
             width = buffer.readShort();
             height = buffer.readShort();
             Layer level = type == _2D ? new Layer(name, width, height) : new Layer3D(name, width, height);
             int size = buffer.readInt();
             if (size == 0) {
                 map.addLayer(level);
                 continue;
             }
             for (int s = 0; s != size; s++) {
                 byte matrix = buffer.readByte();
                 int z = buffer.readInt();
                 if (matrix == MATRIX_SPARSE) {
                     int full = buffer.readInt();
                     for (int f = 0; f != full; f++) {
                         int x = buffer.readShort();
                         int y = buffer.readShort();
                         short id = buffer.readShort();
                         if (id > 0) {
                             Tile t;
                             if (type == _3D) {
                                 t = new Tile3D(sprites.get(id), directionOf(buffer.readByte()), angleOf(buffer.readByte()));
                             } else {
                                 t = new Tile(sprites.get(id));
                             }
                             level.setTile(x, y, z, t);
                         }
                     }
                 } else if (matrix == MATRIX_DENSE) {
                     for (int x = 0; x != width; x++) {
                         for (int y = 0; y != height; y++) {
                             short id = buffer.readShort();
                             if (id > 0) {
                                 Tile t;
                                 if (type == _3D) {
                                     t = new Tile3D(sprites.get(id), directionOf(buffer.readByte()), angleOf(buffer.readByte()));
                                 } else {
                                     t = new Tile(sprites.get(id));
                                 }
                                 level.setTile(x, y, z, t);
                             }
                         }
                     }
                 } else {
                     throw new InvalidMapException("invalid matrix type " + matrix + " at index " + s);
                 }
             }
 
             if (type == _3D) {
                 int modelCount = buffer.readInt();
                 for (int m = 0; m != modelCount; m++) {
                     ((Layer3D) level).setModel(buffer.readShort(),
                             buffer.readShort(),
                             buffer.readShort(),
                             new TileModel(models.get(buffer.readShort())));
                 }
             }
             map.addLayer(level);
         }
         return map;
     }
 
     @Override
     public void compileMap(LevelMap map, DataOutputStream out) throws IOException {
         ByteStream buffer = ByteStream.writeStream();
         buffer.writeShort(MAP_MAGIC);
         Type type;
         buffer.writeByte((type = map.getType()) == _2D ? MAP_2D : MAP_3D);
         buffer.writeShort(map.getWidth());
         buffer.writeShort(map.getLength());
 
         HashMap<Tileset.TileSprite, Integer> recordedTiles = new HashMap<Tileset.TileSprite, Integer>();
         HashMap<WavefrontObject, Integer> recordedModels = new HashMap<WavefrontObject, Integer>();
 
         int constantsCount = 0;
         ByteStream constants = ByteStream.writeStream();
         ByteStream layers = ByteStream.writeStream();
 
         for (Layer layer : map.getLayers()) {
             layers.writeUTF(layer.getName());
             layers.writeShort(layer.getWidth());
             layers.writeShort(layer.getLength());
 
             layers.writeInt(layer.tileMatrix().nonZeroEntries());
             SparseVector.SparseVectorIterator tileIterator = layer.tileMatrix().iterator();
             while (tileIterator.hasNext()) {
                 SparseMatrix<Tile> matrix = (SparseMatrix<Tile>) tileIterator.next();
                 // This is a two step process. First, we calculate the amount of non-null
                 // tiles in the current plane.
                 // We can also use this phase to compile tile IDs
                 int full = 0;
                 for (int x = 0; x != layer.getWidth(); x++) {
                     for (int y = 0; y != layer.getLength(); y++) {
                         Tile t = matrix.get(x, y);
                         if (t != null) {
                             full++;
                             Tileset.TileSprite spr = t.getSprite();
                             // This is not a recorded tile, so we should save it.
                             if (!recordedTiles.containsKey(spr)) {
                                 constants.writeByte(TAG_TILESHEET).writeShort(recordedTiles.size() + 1).writeUTF(spr.getId());
                                 recordedTiles.put(spr, recordedTiles.size() + 1);
                                 constantsCount++;
                             }
                         }
                     }
                 }
 
                 if (full == 0) {
                     // There are no tiles in this plane, it can be ignored
                     continue;
                 }
 
                 int z = tileIterator.realIndex();
 
                 // Now we fetch the total amount of cells on the current plane.
                 int totalTiles = layer.getWidth() * layer.getLength();
                 // To store in a sparse format, we need 3 pieces of information:
                 // * tile id
                 // * x coordinate
                 // * y coordinate
                 // To store in a solid block, we need only the id, but we need to fill
                 // the entire block. Assuming tile id, x-coord and y-coord
                 // are of the same datatype, we can check whether it is worthwhile to save
                 // in a sparce format with a simple evaluation, below.
                 boolean sparse = totalTiles > full * 3;
                 // We need to identify this plane as being sparse or block based.
                 // We also need to write the plane z-coord, in case a layer was
                 // skipped, or the format is not in sequential order.
 
                 layers.writeByte(sparse ? MATRIX_SPARSE : MATRIX_DENSE).writeInt(z);
 
                 if (sparse) {
                     layers.writeInt(full);
                     for (int x = 0; x != layer.getWidth(); x++) {
                         for (int y = 0; y != layer.getLength(); y++) {
                             Tile t = layer.getTile(x, y, z);
                             int id = t != null ? recordedTiles.get(t.getSprite()) : 0;
                             if (id > 0) {
                                 layers.writeShort(x)
                                         .writeShort(y)
                                         .writeShort(id);
                                 if (type == _3D) {
                                     layers.writeByte(tagByteOf(((Tile3D) t).getDirection()));
                                     layers.writeByte(tagByteOf(((Tile3D) t).getAngle()));
                                 }
                             }
                         }
                     }
                 } else {
                     // Iterate over every row and save each tile id
                     // sequentially.
                     for (int x = 0; x != layer.getWidth(); x++) {
                         for (int y = 0; y != layer.getLength(); y++) {
                             Tile t = layer.getTile(x, y, z);
                             int id = t != null ? recordedTiles.get(t.getSprite()) : 0;
                             layers.writeShort(id);
                             if (id > 0 && type == _3D) {
                                 layers.writeByte(tagByteOf(((Tile3D) t).getDirection()));
                                 layers.writeByte(tagByteOf(((Tile3D) t).getAngle()));
                             }
                         }
                     }
                 }
             }
 
             if (layer instanceof Layer3D) {
                 Layer3D l3d = (Layer3D) layer;
                 int nz = 0;
                for (SparseMatrix<TileModel> matrix : l3d.modelMatrix()) {
                    for (TileModel u : matrix) {
                        if (u != null) {
                             nz++;
                        }
                     }
                 }
                 layers.writeInt(nz);
                 SparseVector.SparseVectorIterator modelIterator = l3d.modelMatrix().iterator();
                 HashMap<WavefrontObject, String> modelIds = new HashMap<WavefrontObject, String>();
                 for (Resource<WavefrontObject> models : Amber.getResourceManager().getModels()) {
                     modelIds.put(models.get(), models.getName());
                 }
                 while (modelIterator.hasNext()) {
                     SparseMatrix.SparseMatrixIterator matrixIterator = ((SparseMatrix<WavefrontObject>) modelIterator.next()).iterator();
                     while (matrixIterator.hasNext()) {
                         TileModel tile = (TileModel) matrixIterator.next();
                         if (tile != null) {
                             WavefrontObject obj = tile.getModel();
                             if (!recordedModels.containsKey(obj)) {
                                if (modelIds.containsKey(obj)) {
                                    constants.writeByte(TAG_MODEL).writeShort(recordedModels.size() + 1).writeUTF(modelIds.get(obj));
                                    recordedModels.put(obj, recordedModels.size() + 1);
                                    constantsCount++;
                                }
                             }
                             layers.writeShort(matrixIterator.realX())
                                     .writeShort(matrixIterator.realY())
                                     .writeShort(modelIterator.realIndex()) // z
                                     .writeShort(recordedModels.get(obj));
                         }
                     }
                 }
             }
         }
 
         buffer.writeShort(constantsCount);
         buffer.writeBytes(constants.getBuffer());
         buffer.writeShort(map.getLayers().size());
         buffer.writeBytes(layers.getBuffer());
 
         out.write(buffer.getBuffer());
     }
 
     protected byte tagByteOf(Direction dir) {
         switch (dir) {
             case NORTH:
                 return DIRECTION_NORTH;
             case EAST:
                 return DIRECTION_EAST;
             case SOUTH:
                 return DIRECTION_SOUTH;
             case WEST:
                 return DIRECTION_WEST;
         }
         return DIRECTION_EAST; // Impossible
     }
 
     protected byte tagByteOf(Angle angle) {
         switch (angle) {
             case _180:
                 return ANGLE_180;
             case _90:
                 return ANGLE_90;
             case _45:
                 return ANGLE_45;
         }
         return ANGLE_180; // Impossible
     }
 
     protected Direction directionOf(byte tag) {
         switch (tag) {
             case DIRECTION_NORTH:
                 return NORTH;
             case DIRECTION_EAST:
                 return EAST;
             case DIRECTION_SOUTH:
                 return SOUTH;
             case DIRECTION_WEST:
                 return WEST;
         }
         return EAST; // Impossible
     }
 
     protected Angle angleOf(byte tag) {
         switch (tag) {
             case ANGLE_180:
                 return _180;
             case ANGLE_90:
                 return _90;
             case ANGLE_45:
                 return _45;
         }
         return _180; // Impossible
     }
 }
