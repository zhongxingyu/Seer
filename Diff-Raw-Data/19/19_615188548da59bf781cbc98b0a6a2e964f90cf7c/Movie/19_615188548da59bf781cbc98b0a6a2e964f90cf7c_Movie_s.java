 /*
  * Movie.java
  * Transform
  *
  * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.flagstone.transform;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.zip.DataFormatException;
 import java.util.zip.Deflater;
 import java.util.zip.InflaterInputStream;
 
 import com.flagstone.transform.coder.Context;
 import com.flagstone.transform.coder.DecoderRegistry;
 import com.flagstone.transform.coder.DefineTag;
 import com.flagstone.transform.coder.MovieTag;
 import com.flagstone.transform.coder.SWFDecoder;
 import com.flagstone.transform.coder.SWFEncoder;
 import com.flagstone.transform.coder.SWFFactory;
 import com.flagstone.transform.datatype.Bounds;
 import com.flagstone.transform.exception.IllegalArgumentRangeException;
 
 /**
  * Movie is a container class for the objects that represents the data
  * structures in a Flash file.
  *
  * <p>
  * Movie is the core class of the Transform package. It is used to parse and
  * generate Flash files, translating the binary format of the Flash file into an
  * array objects that can be inspected and updated.
  * </p>
  *
  * <p>
  * A Movie object also contains the attributes that make up the header
  * information of the Flash file, identifying the version support, size of the
  * Flash Player screen, etc.
  * </p>
  *
  * <p>
  * Movie is also used to generate the unique identifiers that are used to
  * reference objects. Each call to newIdentifier() returns a unique number for
  * the current. The identifiers are generated using a simple counter. When a
  * movie is decoded this counter is updated each time an object definition is
  * decoded. This allows new objects to be added and ensures that the identifier
  * does not conflict with an existing object.
  * </p>
  */
 //TODO(class)
 public final class Movie {
 
     /** TODO(class). */
    public enum Signature {
        /** TODO(method). */
        FWS(new byte[] { 0x46, 0x57, 0x53}),
        /** TODO(method). */
        CWS(new byte[] { 0x43, 0x57, 0x53});
        
        private byte[] bytes;
        
        private Signature(byte[] data) {
            bytes = Arrays.copyOf(data, data.length);
        }
        
        public boolean matches(byte[] data) {
            return Arrays.equals(bytes, data);
        }
     }
  
     private static final String FORMAT = "Movie: { signature=%s; version=%d;"
             + " frameSize=%s; frameRate=%f; objects=%s }";
 
     private DecoderRegistry registry;
     private CharacterEncoding encoding;
     private int identifier;
 
     private Signature signature;
     private int version;
     private Bounds frameSize;
     private float frameRate;
     private List<MovieTag> objects;
 
     private transient int length;
     private transient int frameCount;
 
     /** TODO(method). */
     public Movie() {
         encoding = CharacterEncoding.UTF8;
         signature = Signature.CWS;
         version = 9;
         objects = new ArrayList<MovieTag>();
     }
 
     /** TODO(method). */
     public Movie(final Movie object) {
 
         if (object.registry != null) {
             registry = object.registry.copy();
         }
 
         identifier = object.identifier;
         encoding = object.encoding;
 
         signature = object.signature;
         version = object.version;
         frameSize = object.frameSize;
         frameRate = object.frameRate;
 
         objects = new ArrayList<MovieTag>(object.objects.size());
 
         for (final MovieTag tag : object.objects) {
             objects.add(tag.copy());
         }
     }
 
     /**
      * Sets the registry containing the object used to decode the different
      * types of object found in a movie.
      *
      * @param registry
      */
     public void setRegistry(final DecoderRegistry registry) {
         this.registry = registry;
     }
 
     /**
      * Sets the initial value for the unique identifier assigned to definition
      * objects.
      *
      * @param aValue
      *            an initial value for the unique identifier.
      */
     public void setIdentifier(final int aValue) {
         identifier = aValue;
     }
 
     /**
      * Sets the encoding scheme for strings encoded and decoded from Flash
      * files.
      */
     public void setEncoding(final CharacterEncoding enc) {
         encoding = enc;
     }
 
     /**
      * Returns a unique identifier that will be assigned to definition objects.
      * In order to reference objects that define items such as shapes, sounds,
      * etc. each must be assigned an identifier that is unique for a given
      * Movie.
      *
      * When binary data is decoded into a sequence of objects, the Movie class
      * tracks each Define tag decoded, recording the highest value. If a new
      * Define tag is added to the array of decoded objects the identifier
      * assigned to the new tag will be guaranteed to be unique.
      *
      * @return an unique identifier for objects that define shapes, sounds, etc.
      *         in a Flash file.
      */
     public int identifier() {
         return ++identifier;
     }
 
     /**
      * Returns the signature identifying that the movie contains Flash.
      */
     public Signature getSignature() {
         return signature;
     }
 
     /**
      * Sets the signature for the Flash data when it is encoded.
      */
     public void setSignature(final Signature sig) {
         signature = sig;
     }
 
     /**
      * Returns the number representing the version of Flash that the movie
      * represents.
      */
     public int getVersion() {
         return version;
     }
 
     /**
      * Sets the Flash version supported in this Movie. Note that there are no
      * restrictions on the objects that can be used in a coder. Using objects
      * that are not supported by an earlier version of the Flash file format may
      * cause the Player to not display the movie correctly or even crash the
      * Player.
      *
      * @param aNumber
      *            the version of the Flash file format that this movie utilises.
      */
     public void setVersion(final int aNumber) {
         if (aNumber < 0) {
             throw new IllegalArgumentRangeException(0, Integer.MAX_VALUE, aNumber);
         }
         version = aNumber;
     }
 
     /**
      * Returns the bounding rectangle that defines the size of the player
      * screen.
      */
     public Bounds getFrameSize() {
         return frameSize;
     }
 
     /**
      * Sets the bounding rectangle that defines the size of the player screen.
      * The coordinates of the bounding rectangle are also used to define the
      * coordinate range. For example if a 400 x 400 pixel rectangle is defined,
      * specifying the values for the x and y coordinates the range -200 to 200
      * sets the centre of the screen at (0,0), if the x and y coordinates are
      * specified in the range 0 to 400 then the centre of the screen will be at
      * (200, 200).
      *
      * @param aBounds
      *            the Bounds object that defines the frame size. Must not be
      *            null.
      */
     public void setFrameSize(final Bounds aBounds) {
         if (aBounds == null) {
             throw new NullPointerException();
         }
         frameSize = aBounds;
     }
 
     /**
      * Returns the number of frames played per second that the movie will be
      * displayed at.
      */
     public float getFrameRate() {
         return frameRate;
     }
 
     /**
      * Sets the number of frames played per second that the Player will display
      * the coder.
      *
      * @param aNumber
      *            the number of frames per second that the movie is played.
      */
     public void setFrameRate(final float aNumber) {
         frameRate = aNumber;
     }
 
     /**
      * Returns the array of objects contained in the Movie.
      */
     public List<MovieTag> getObjects() {
         return objects;
     }
 
     /**
      * Sets the array of objects contained in the Movie.
      *
      * @param anArray
      *            the array of objects that describe a coder. Must not be null.
      */
     public void setObjects(final List<MovieTag> anArray) {
         if (anArray == null) {
             throw new NullPointerException();
         }
         objects = anArray;
     }
 
     /**
      * Adds the object to the Movie.
      *
      * @param anObject
      *            the object to be added to the movie. Must not be null.
      */
     public Movie add(final MovieTag anObject) {
         if (anObject == null) {
             throw new NullPointerException();
         }
         objects.add(anObject);
         return this;
     }
 
     /**
      * Creates and returns a complete copy of this object.
      */
     public Movie copy() {
         return new Movie(this);
     }
 
     /** {@inheritDoc} */
     @Override
     public String toString() {
         return String.format(FORMAT, signature, version, frameSize, frameRate,
                 objects);
     }
 
     /**
      * Decodes the contents of the specified file. An object for each tag
      * decoded from the file is placed in the Movie's object array in the order
      * they were decoded from the file. If an error occurs while reading and
      * parsing the file then an exception is thrown.
      *
      * @param file
      *            the Flash file that will be parsed.
      * @throws DataFormatException
      *             - if the file does not contain Flash data.
      * @throws IOException
      *             - if an I/O error occurs while reading the file.
      */
     public void decodeFromFile(final File file) throws DataFormatException,
             IOException {
         final FileInputStream stream = new FileInputStream(file);
 
         try {
             decodeFromStream(stream);
         } finally {
             stream.close();
         }
     }
 
     /**
      * Decodes the binary Flash data from an input stream. If an error occurs
      * while the data is being decoded an exception is thrown. The array of
      * objects in the Movie will contain the last tag successfully decoded.
      *
      * @param stream
      *            an InputStream from which the objects will be decoded.
      *
      * @throws DataFormatException
      *             if the file does not contain Flash data.
      * @throws IOException
      *             if an I/O error occurs while reading the file.
      */
     public void decodeFromStream(final InputStream stream)
             throws DataFormatException, IOException {
 
         byte[] buffer = new byte[8];
         int bytesRead = stream.read(buffer);
 
         byte[] sig = Arrays.copyOf(buffer, 3);
 
         if (Signature.FWS.matches(sig)) {
             signature = Signature.FWS;
         } else if (Signature.CWS.matches(sig)) {
             signature = Signature.CWS;
         } else {
             throw new DataFormatException("Not SWF Format");
         }
 
         version = buffer[3] & 0x00FF;
 
         length = buffer[4] & 0x00FF;
         length |= (buffer[5] & 0x00FF) << 8;
         length |= (buffer[6] & 0x00FF) << 16;
         length |= (buffer[7] & 0x00FF) << 24;
 
         final InputStream streamIn;
 
         if (signature == Signature.CWS) {
             streamIn = new InflaterInputStream(stream);
         } else {
             streamIn = new DataInputStream(stream);
         }
 
         bytesRead += streamIn.read(buffer, 0, 2);
 
         final int size = 2 + ((12 + ((buffer[0] & 0xF8) >> 1)) >>> 3);
 
         buffer = Arrays.copyOf(buffer, size + 2);
         bytesRead += streamIn.read(buffer, 2, size);
 
         final SWFDecoder decoder = new SWFDecoder(buffer);
         final Context context = new Context().put(Context.VERSION, version);
 
         if (registry == null) {
             context.setRegistry(DecoderRegistry.getDefault());
         } else {
             context.setRegistry(registry);
         }
 
         frameSize = new Bounds(decoder);
         frameRate = decoder.readWord(2, true) / 256.0f;
         frameCount = decoder.readWord(2, false);
 
         buffer = new byte[length - size - 10];
         int read = 0;
         
         do {
            read += streamIn.read(buffer, read, buffer.length-read);
        } while (read < buffer.length);
         
        decoder.setData(buffer);
         decoder.setEncoding(encoding.toString());
 
         final SWFFactory<MovieTag> factory = context.getRegistry().getMovieDecoder();
 
         objects.clear();
         
         if (factory == null) {
             objects.add(new MovieData(decoder.getData()));
         } else {
             MovieTag object;
 
             while (!decoder.eof()) {
 
                 object = factory.getObject(decoder, context);
 
                 if (object instanceof DefineTag) {
                     identifier = ((DefineTag) object).getIdentifier();
                 }
 
                 objects.add(object);
             }
             // Remove the last object which is the end of movie marker.
             objects.remove(objects.size()-1);
         }
     }
 
     /**
      * Encodes the array of objects and writes the data to the specified file.
      * If an error occurs while encoding the file then an exception is thrown.
      *
      * @param file
      *            the Flash file that the movie will be encoded to.
      *
      * @throws IOException
      *             - if an I/O error occurs while writing the file.
      * @throws DataFormatException
      *             if an error occurs when compressing the flash file.
      */
     /** {@inheritDoc} */
     public void encodeToFile(final File file) throws IOException,
             DataFormatException {
         final FileOutputStream stream = new FileOutputStream(file);
 
         try {
             encodeToStream(stream);
         } finally {
             stream.close();
         }
     }
 
     /**
      * Returns the encoded representation of the array of objects that this
      * Movie contains. If an error occurs while encoding the file then an
      * exception is thrown.
      *
      * @param stream
      *            the output stream that the video will be encoded to.
      * @throws IOException
      *             - if an I/O error occurs while encoding the file.
      * @throws DataFormatException
      *             if an error occurs when compressing the flash file.
      */
     public void encodeToStream(OutputStream stream) throws DataFormatException,
         IOException {
         
         final SWFEncoder coder = new SWFEncoder(0);
         final Context context = new Context();
 
         coder.setEncoding(encoding.toString());
         context.getVariables().put(Context.VERSION, version);
 
         frameCount = 0;
 
         length = 14; // Includes End
         length += frameSize.prepareToEncode(coder, context);
 
         for (final MovieTag tag : objects) {
             length += tag.prepareToEncode(coder, context);
             if (tag instanceof ShowFrame) {
                 frameCount += 1;
             }
         }
 
         coder.setData(length);
 
         coder.writeString(signature.toString());
         coder.adjustPointer(-8);
         coder.writeByte(version);
         coder.writeWord(length, 4);
         frameSize.encode(coder, context);
         coder.writeWord((int) (frameRate * 256), 2);
         coder.writeWord(frameCount, 2);
 
         for (final MovieTag tag : objects) {
             tag.encode(coder, context);
         }
         coder.writeWord(0, 2);
 
         byte[] data = new byte[length];
 
         if (signature == Signature.CWS) {
             data = zip(coder.getData(), length);
         } else {
             data = coder.getData();
         }
         stream.write(data);
     }
 
     private byte[] zip(final byte[] bytes, final int len)
             throws DataFormatException {
         final Deflater deflater = new Deflater();
         final byte[] data = new byte[len];
 
         deflater.setInput(bytes, 8, len - 8);
         deflater.finish();
 
         final int bytesCompressed = deflater.deflate(data);
         final byte[] compressedData = new byte[8 + bytesCompressed];
 
         System.arraycopy(bytes, 0, compressedData, 0, 8);
         System.arraycopy(data, 0, compressedData, 8, bytesCompressed);
 
         return compressedData;
     }
 }
