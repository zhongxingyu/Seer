 package outag.audioformats.asf.data;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import outag.audioformats.Tag;
 import outag.audioformats.asf.util.Utils;
 
 /** This structure represents the data of a chunk, wich contains extended content description. <br>
  * These properties are simply represented by {@link outag.audioformats.asf.data.ContentDescriptor} */
 public class ExtendedContentDescription extends Chunk {
 
     /** Contains the properties.  */
     private final ArrayList<ContentDescriptor> descriptors;
 
     /** Stores the ids (names) of inserted content descriptors. <br>
      * If {@link #getDescriptor(String)}is called this field will be filled if
      * <code>null</code>. Any modification of the contents of this object
      * will set this field to <code>null</code>. */
     private HashMap<String, Integer> indexMap = null;
 
     /** Creates an instance. */
     public ExtendedContentDescription() { this(0, BigInteger.valueOf(0)); }
 
     /** Creates an instance.
      * @param pos Position of header object within file or stream.
      * @param chunkLen Length of the represented chunk. */
     public ExtendedContentDescription(long pos, BigInteger chunkLen) {
         super(GUID.GUID_EXTENDED_CONTENT_DESCRIPTION, pos, chunkLen);
         this.descriptors = new ArrayList<ContentDescriptor>();
     }
 
     /** Inserts the given ContentDescriptor.
      * @param toAdd ContentDescriptor to insert. */
     public void addDescriptor(ContentDescriptor toAdd) {
         assert toAdd != null : "Argument must not be null.";
         if (getDescriptor(toAdd.getName()) != null) {
             throw new RuntimeException(toAdd.getName() + " is already present");
         }
         this.descriptors.add(toAdd);
         this.indexMap.put(toAdd.getName(), new Integer(descriptors.size() - 1));
     }
 
     /** Adds or replaces an existing content descriptor.
      * @param descriptor Descriptor to be added or replaced. */
     public void addOrReplace(ContentDescriptor descriptor) {
         assert descriptor != null : "Argument must not be null";
         if (getDescriptor(descriptor.getName()) != null) {
             /*
              * Just remove if exists. Will prevent the indexmap being rebuild.
              */
             remove(descriptor.getName());
         }
         addDescriptor(descriptor);
     }
 
     /** Returns the album entered in the content descriptor chunk.
      * @return Album, <code>""</code> if not defined. */
     public String getAlbum() {
         ContentDescriptor result = getDescriptor(ContentDescriptor.ID_ALBUM);
         return (result == null) ? "" : result.getString();
     }
 
     /** Returns the "WM/AlbumArtist" entered in the extended content description.
      * @return Title, <code>""</code> if not defined. */
     public String getArtist() {
         ContentDescriptor result = getDescriptor(ContentDescriptor.ID_ARTIST);
         return (result == null) ? "" : result.getString();
     }
 
     /** Returns a previously inserted content descriptor.
      * @param name name of the content descriptor.
      * @return <code>null</code> if not present. */
     public ContentDescriptor getDescriptor(String name) {
         if (this.indexMap == null) {
             this.indexMap = new HashMap<String, Integer>();
             for (int i = 0; i < descriptors.size(); i++) {
                 ContentDescriptor current = (ContentDescriptor) descriptors
                         .get(i);
                 indexMap.put(current.getName(), new Integer(i));
             }
         }
         Integer pos = (Integer) indexMap.get(name);
         return (pos == null) ? null : (ContentDescriptor) descriptors.get(pos.intValue());
     }
 
     /** @return Returns the descriptorCount. */
     public long getDescriptorCount() { return descriptors.size(); }
 
     /** Returns a collection of all {@link ContentDescriptor}objects stored in
      * this extended content description. */
     public Collection<ContentDescriptor> getDescriptors() {
         return new ArrayList<ContentDescriptor>(this.descriptors);
     }
 
     /** Returns the Genre entered in the content descriptor chunk. */
     public String getGenre() {
         String result = null;
         ContentDescriptor prop = getDescriptor(ContentDescriptor.ID_GENRE);
         if (prop == null) {
             prop = getDescriptor(ContentDescriptor.ID_GENREID);
             if (prop == null)
                 result = "";
             else {
                 result = prop.getString();
                 if (result.startsWith("(") && result.endsWith(")")) {
                     result = result.substring(1, result.length() - 1);
                     try {
                         int genreNum = Integer.parseInt(result);
                         if (genreNum >= 0 && genreNum < Tag.DEFAULT_GENRES.length)
                             result = Tag.DEFAULT_GENRES[genreNum];
                     } catch (NumberFormatException e) {
                         // Do nothing
                     }
                 }
             }
         } else result = prop.getString();
         return result;
     }
 
     /** Returns the Track entered in the content descriptor chunk. */
     public String getTrack() {
         ContentDescriptor result = getDescriptor(ContentDescriptor.ID_TRACKNUMBER);
         return (result == null) ? "" : result.getString();
     }
 
     /** Returns the Year entered in the extended content description. */
     public String getYear() {
         ContentDescriptor result = getDescriptor(ContentDescriptor.ID_YEAR);
         return (result == null) ? "" : result.getString();
     }
     
     /** Removes the content descriptor with the given name. <br>
      * @param id The id (name) of the descriptor which should be removed.
      * @return The descriptor which is removed. If not present <code>null</code>. */
     public ContentDescriptor remove(String id) {
         ContentDescriptor result = getDescriptor(id);
         if (result != null) {
             descriptors.remove(result);
         }
         this.indexMap = null;
         return result;
     }    
 
     public String prettyPrint() {
         StringBuffer result = new StringBuffer(super.prettyPrint());
         result.insert(0, "\nExtended Content Description:\n");
         ContentDescriptor[] list = (ContentDescriptor[]) descriptors
                 .toArray(new ContentDescriptor[descriptors.size()]);
         Arrays.sort(list);
         for (int i = 0; i < list.length; i++) {
             result.append("   ");
             result.append(list[i]);
             result.append(Utils.LINE_SEPARATOR);
         }
         return result.toString();
     }
 }
