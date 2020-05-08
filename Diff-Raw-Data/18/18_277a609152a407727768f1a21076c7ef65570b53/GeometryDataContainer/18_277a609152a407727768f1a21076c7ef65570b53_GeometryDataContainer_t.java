 /**
  * Copyright (c) 2007-2011, JAGaToo Project Group all rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 package org.jagatoo.geometry;
 
 import java.nio.FloatBuffer;
 import java.util.Arrays;
 
 import org.jagatoo.opengl.enums.GeometryArrayType;
 import org.openmali.spatial.polygons.Triangle;
 import org.openmali.vecmath2.Colorf;
 import org.openmali.vecmath2.TexCoordf;
 import org.openmali.vecmath2.TupleNf;
 import org.openmali.vecmath2.Vector3f;
 import org.openmali.vecmath2.Vertex3f;
 
 /**
  * This is the advanced (regular) version of a geometry data container.
  * It additionally holds (or can hold) normals, colors, texture-coordinates
  * and vertex-attributes.
  * 
  * @author David Yazel
  * @author Marvin Froehlich (aka Qudus)
  */
 public class GeometryDataContainer extends SimpleGeometryDataContainer
 {
     public static final int NORMALS                = 2;  // equals OpenMaLi's Vertex3f.NORMALS
     public static final int COLORS                 = 4;  // equals OpenMaLi's Vertex3f.COLORS
     public static final int TEXTURE_COORDINATES    = 8;  // equals OpenMaLi's Vertex3f.TEXTURE_COORDINATES
     public static final int VERTEX_ATTRIBUTES      = 16; // equals OpenMaLi's Vertex3f.VERTEX_ATTRIBUTES
     
     protected GeomNioFloatData normals = null;
     protected GeomNioFloatData colors = null;
     protected GeomNioFloatData[] texCoords = null;
     protected GeomNioFloatData[] vertexAttribs = null;
     
     private boolean hasNormals = false;
     private boolean hasColors = false;
     protected int colorSize = 0;
     private final int[] textureUnitSize = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
     private final int[] vertexAttribsSize = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
     
     private int[] texCoordSetMap = new int[ 0 ];
     private int[] texCoordSetMap_public = new int[ 0 ];
     
     protected long normalsOffset = 0L;
     protected long colorsOffset = 0L;
     protected final long[] texCoordsOffsets = new long[] { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };
     protected final long[] vertexAttribsOffsets = new long[] { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };
     private int vertexFormat;
     
     /**
      * @return the format of the vertices in this object.
      */
     public final int getVertexFormat()
     {
         return ( this.vertexFormat );
     }
     
     /**
      * @return <code>true</code>, if this geometry's color component has an alpha channel.
      */
     public final boolean hasColorAlpha()
     {
         if ( colors == null )
             throw new Error( "no colors set" );
         
         return ( colorSize == 4 );
     }
     
     /**
      * @param flag
      * 
      * @return <code>true</code>, if this Geometry has the queried feature(s).
      */
     public final boolean hasFeature( int flag )
     {
         return ( ( flag & vertexFormat ) != 0 );
     }
     
     /*
     **
      * Sets this Geometry's Optimization to be used.
      * 
      * @param opt
      *
     public void setOptimization( Optimization opt )
     {
         if ( opt == null )
             throw new NullPointerException( "Optimization must not be null" );
         
         this.optimization = opt;
     }
     
     **
      * @return this Geometry's Optimization level.
      *
     public final Optimization getOptimization()
     {
         return ( optimization );
     }
     
     **
      * Marks this Geometry's bounds dirty. This will cause a bounds update
      * when the Geometry is next rendered.
      *
     public void setBoundsDirty()
     {
         this.cachedBounds = null;
     }
     
     **
      * @return <code>true</code>, if a bounds update is requested.
      *
     public final boolean isBoundsDirty()
     {
         return ( cachedBounds == null );
     }
     
     void setCachedBounds( Bounds b )
     {
         this.cachedBounds = b;
     }
     
     final Bounds getCachedBounds()
     {
         return ( cachedBounds );
     }
     
     **
      * This method calculates face normals (each orthogonal to its face).
      * 
      * @see #calculateFaceNormals()
      * 
      * @param apply if <code>true</code>, the normals are applied back to the Geometry
      * @param faceNormals must be of size getTrianglesCount(), or <code>null</code>.
      *                    It is filled with the face normals, if not <code>null</code>.
      * @param vertexNormals must be of size getVertexCount(), or <code>null</code>.
      *                      It is filled with the new vertex normals, if not <code>null</code>.
      *
     public void calculateFaceNormals( boolean apply, Vector3f[] faceNormals, Vector3f[] vertexNormals )
     {
         if ( !( this instanceof WriteableTriangleContainer ) )
             throw new Error( "Not an instance of WriteableTriangleContainer" ) );
         
         WriteableTriangleContainer wtc = (WriteableTriangleContainer)this;
         
         Triangle triangle = PickPool.allocateTriangle();
         triangle.setFeatures( Vertex3f.COORDINATES | Vertex3f.NORMALS );
         Vector3f faceNormal = Vector3f.fromPool();
         
         final int numTrangles = wtc.getTriangleCount();
         
         for ( int i = 0; i < numTrangles; i++ )
         {
             wtc.getTriangle( i, triangle );
             
             triangle.getFaceNormal( faceNormal );
             
             if ( apply )
             {
                 triangle.setVertexNormalA( faceNormal );
                 triangle.setVertexNormalB( faceNormal );
                 triangle.setVertexNormalC( faceNormal );
                 
                 this.setTriangle( triangle );
             }
             
             if ( faceNormals != null )
             {
                 if ( faceNormals[ i ] != null )
                     faceNormals[ i ].set( faceNormal );
                 else
                     faceNormals[ i ] = new Vector3f( faceNormal );
             }
             
             if ( vertexNormals != null )
             {
                 if ( vertexNormals[ triangle.getVertexIndexA() ] != null )
                     vertexNormals[ triangle.getVertexIndexA() ].set( faceNormal );
                 else
                     vertexNormals[ triangle.getVertexIndexA() ] = new Vector3f( faceNormal );
                 
                 if ( vertexNormals[ triangle.getVertexIndexB() ] != null )
                     vertexNormals[ triangle.getVertexIndexB() ].set( faceNormal );
                 else
                     vertexNormals[ triangle.getVertexIndexB() ] = new Vector3f( faceNormal );
                 
                 if ( vertexNormals[ triangle.getVertexIndexC() ] != null )
                     vertexNormals[ triangle.getVertexIndexC() ].set( faceNormal );
                 else
                     vertexNormals[ triangle.getVertexIndexC() ] = new Vector3f( faceNormal );
             }
         }
         
         Vector3f.toPool( faceNormal );
     }
     
     **
      * This method calculates face normals and applies them to the Geometry.
      * 
      * @see #calculateFaceNormals(boolean, Vector3f[], Vector3f[])
      *
     public void calculateFaceNormals()
     {
         calculateFaceNormals( true, null, null );
     }
     */
     
     /**
      * Super fast method add a bunch of data right into the data elements.
      * 
      * @param coordData
      * @param texCoordData
      * @param normalData
      * @param colorData
      */
     public void addData( float[] coordData, float[] texCoordData, float[] normalData, float[] colorData )
     {
         if ( coordData != null )
         {
             coords.set( coordData, 0, coordData.length );
         }
         
         if ( texCoordData != null )
         {
             texCoords[ 0 ].set( texCoordData, 0, texCoordData.length );
         }
         
         if ( normalData != null )
         {
             normals.set( normalData, 0, normalData.length );
         }
         
         if ( colorData != null )
         {
             colors.set( colorData, 0, colorData.length );
         }
         
         numVertices += ( coordData.length / 3 );
     }
     
     /**
      * Super fast method for moving a bunch of data into the data elements. The
      * Positions are all translated according to the tuple passed in.
      * 
      * @param translateX
      * @param translateY
      * @param translateZ
      * @param numVertices
      * @param coordData
      * @param texCoordData
      * @param normalData
      * @param colorData
      */
     public void addData( float translateX, float translateY, float translateZ, int numVertices, float[] coordData, float[] texCoordData, float[] normalData, float[] colorData )
     {
         if ( maxVertices <= ( this.numVertices + ( numVertices / 3 ) ) )
         {
             return;
         }
         
         if ( coordData != null )
         {
             final int coordLength = numVertices * 3;
             
             float[] temp = new float[ (int)( coordLength * 1.1 ) ];
             
             System.arraycopy( coordData, 0, temp, 0, coordLength );
             
             for ( int i = 0; i < numVertices; i++ )
             {
                 final int j = i * 3;
                 temp[ j + 0 ] = coordData[ j + 0 ] + translateX;
                 temp[ j + 1 ] = coordData[ j + 1 ] + translateY;
                 temp[ j + 2 ] = coordData[ j + 2 ] + translateZ;
             }
             
             coords.set( temp, 0, coordLength );
         }
         
         if ( texCoordData != null )
         {
             texCoords[ 0 ].set( texCoordData, 0, texCoordData.length );
         }
         
         if ( normalData != null )
         {
             normals.set( normalData, 0, normalData.length );
         }
         
         if ( colorData != null )
         {
             colors.set( colorData, 0, colorData.length );
         }
         
         this.numVertices += ( coordData.length / 3 );
     }
     
     /**
      * Super fast method for moving a bunch of data into the data elements. The
      * Positions are all translated according to the tuple passed in. The color
      * for all the vertices are set the specified value
      * 
      * @param translateX
      * @param translateY
      * @param translateZ
      * @param numVertices
      * @param coordData
      * @param texCoordData
      * @param normalData
      * @param alpha
      */
     public void addData( float translateX, float translateY, float translateZ, int numVertices, float[] coordData, float[] texCoordData, float[] normalData, float alpha )
     {
         int colorLength = numVertices * 4;
         
         // TODO (yvg) Check why colorLength is multiplied by 1.1 - looks
         // like a strange workaround, to be removed if not needed
         float[] tempColor = new float[ (int)( colorLength * 1.1 ) ];
         
         Arrays.fill( tempColor, 0, colorLength, alpha );
         addData( translateX, translateY, translateZ, numVertices, coordData, texCoordData, normalData, tempColor );
     }
     
     /**
      * Directly sets the coordinates data buffer.
      * 
      * @param data
      */
     @Override
     public void setCoordinateData( GeomNioFloatData data )
     {
         super.setCoordinateData( data );
         
         vertexFormat |= COORDINATES;
     }
     
     /**
      * Directly sets the normals data buffer.
      * 
      * @param data
      */
     public void setNormalData( GeomNioFloatData data )
     {
         normals = data;
         
         if ( normals == null )
         {
             hasNormals = false;
             vertexFormat &= ~NORMALS;
         }
         else
         {
             hasNormals = true;
             vertexFormat |= NORMALS;
         }
     }
     
     /**
      * @return the data buffer for normal data.
      */
     public final GeomNioFloatData getNormalsData()
     {
         return ( normals );
     }
     
     /**
      * @return this size of normals (always 3).
      */
     public final int getNormalsSize()
     {
         return ( 3 );
     }
     
     /**
      * @return the offset in the data buffer, if this is interleaved data.
      */
     public final long getNormalsOffset()
     {
         return ( normalsOffset );
     }
     
     /**
      * Directly sets the color data buffer.
      * 
      * @param data
      */
     public void setColorData( GeomNioFloatData data )
     {
         colors = data;
         
         if ( colors == null )
         {
             hasColors = false;
             vertexFormat &= ~COLORS;
         }
         else
         {
             hasColors = true;
             vertexFormat |= COLORS;
             colorSize = colors.getElemSize();
         }
     }
     
     /**
      * @return the data buffer for color data.
      */
     public final GeomNioFloatData getColorData()
     {
         return ( colors );
     }
     
     /**
      * @return this size of colors (always 3 or 4, no alpha / alpha).
      */
     public final int getColorsSize()
     {
         return ( colorSize );
     }
     
     /**
      * @return the offset in the data buffer, if this is interleaved data.
      */
     public final long getColorsOffset()
     {
         return ( colorsOffset );
     }
     
     private final void rebuildTexCoordSetMap()
     {
         int numSetTUs = 0;
         for ( int i = 0; i < texCoords.length; i++ )
         {
             if ( texCoords[ i ] != null )
                 numSetTUs++;
         }
         
         if ( texCoordSetMap.length != numSetTUs )
         {
             texCoordSetMap = new int[ numSetTUs ];
             texCoordSetMap_public = new int[ numSetTUs ];
         }
         
         int j = 0;
         for ( int i = 0; i < texCoords.length; i++ )
         {
             if ( texCoords[ i ] != null )
             {
                 texCoordSetMap[ j++ ] = i;
             }
             else
             {
                 texCoordSetMap[ j++ ] = -1;
             }
         }
         
         System.arraycopy( texCoordSetMap, 0, texCoordSetMap_public, 0, texCoordSetMap.length );
     }
     
     /**
      * Directly sets the tex-coords data buffer for the guven texture-unit.
      * 
      * @param unit
      * @param data
      */
     public void setTexCoordData( int unit, GeomNioFloatData data )
     {
         if ( data == null )
         {
             if ( ( texCoords == null ) || ( unit >= texCoords.length ) )
                 return;
         }
         else if ( unit >= texCoords.length )
         {
             GeomNioFloatData[] newTexCoords = new GeomNioFloatData[ unit + 1 ];
             System.arraycopy( texCoords, 0, newTexCoords, 0, texCoords.length );
             Arrays.fill( newTexCoords, unit + 1, newTexCoords.length, null );
             texCoords = newTexCoords;
         }
         
         texCoords[ unit ] = data;
         
         if ( data == null )
             textureUnitSize[ unit ] = 0;
         else
             textureUnitSize[ unit ] = data.getElemSize();
         
         rebuildTexCoordSetMap();
         
         if ( getNumTextureUnits() == 0 )
             vertexFormat &= ~TEXTURE_COORDINATES;
         else
             vertexFormat |= TEXTURE_COORDINATES;
     }
     
     /**
      * @return the data buffer for tex-coord data for the given texture-unit.
      * 
      * @param unit
      */
     public final GeomNioFloatData getTexCoordsData( int unit )
     {
         if ( texCoords == null )
             return ( null );
         
         if ( texCoords.length > unit )
             return ( texCoords[ unit ] );
         
         return ( null );
     }
     
     /**
      * @param unit the texture unit (always 1, 2, 3 or 4)
      * 
      * @return the size of the texture coordinates of texture unit 'unit'.
      */
     public final int getTexCoordSize( int unit )
     {
         return ( textureUnitSize[ unit ] );
     }
     
     /**
      * @return the number of texture units used in this Geometry.
      */
     public final int getNumTextureUnits()
     {
         return ( texCoordSetMap.length );
     }
     
     /**
      * @return The map for texture coordinates to texture units. (for internal use only!)
      */
     public final int[] getTexCoordSetMap()
     {
         return ( texCoordSetMap_public );
     }
     
     /**
      * Gets the map for texture coordinates to texture units. (for internal use only!)
      * 
      * @param intArray 
      */
     public final void getTexCoordSetMap( int[] intArray )
     {
         System.arraycopy( texCoordSetMap, 0, intArray, 0, texCoordSetMap.length );
     }
     
     /**
      * @param unit the texture unit (always 1, 2, 3 or 4)
      * 
      * @return the offset in the data buffer, if this is interleaved data.
      */
     public final long getTexCoordsOffset( int unit )
     {
         return ( texCoordsOffsets[ unit ] );
     }
     
     /**
      * @return the data buffer for vertex-attributes data.
      * 
      * @param index
      */
     public final GeomNioFloatData getVertexAttribData( int index )
     {
         if ( vertexAttribs == null )
             return ( null );
         
         if ( vertexAttribs.length > index )
             return ( vertexAttribs[ index ] );
         
         return ( null );
     }
     
     /**
      * @return the size of the queried vertex attributes.
      * 
      * @param index
      */
     public final int getVertexAttribSize( int index )
     {
         return ( vertexAttribsSize[ index ] );
     }
     
     /**
      * @param index
      * 
      * @return the offset in the data buffer, if this is interleaved data.
      */
     public final long getVertexAttribsOffset( int index )
     {
         return ( vertexAttribsOffsets[ index ] );
     }
     
     @Override
     protected void createCoordinates()
     {
         super.createCoordinates();
         
         vertexFormat |= COORDINATES;
     }
     
     
     /**
      * @return <code>true</code>, if this geometry contains normal data.
      */
     public final boolean hasNormals()
     {
         return ( hasNormals );
     }
     
     private final void createNormals()
     {
         normals = newNioFloatData( maxVertices, getNormalsSize(), 0, reversed );
         hasNormals = true;
         vertexFormat |= NORMALS;
     }
     
     protected final boolean checkNormalsExistence( boolean b )
     {
         if ( normals == null )
         {
             if ( !b )
                 return ( false );
             
             createNormals();
             
             if ( isInterleaved() )
                 makeInterleaved();
         }
         /*
         else if ( normals.getElemSize() != 3 )
         {
             throw new IllegalArgumentException( "mismatching normals size" ) );
         }
         */
         
         return ( true );
     }
     
     /**
      * Sets the normal of the vertex at the given index.
      * 
      * @param vertexIndex The index of the vertex to modify
      * @param floatArray The new normal data. Its size must be a multiple of 3.
      */
     public void setNormal( int vertexIndex, float[] floatArray )
     {
         if ( !checkNormalsExistence( floatArray != null ) )
             return;
         
         normals.set( vertexIndex, getNormalsSize(), normalsOffset / 4L, floatArray, 0, getNormalsSize() );
     }
     
     /**
      * Sets the normal of the vertex at the given index.
      * 
      * @param vertexIndex THe index of the vertex to modify
      * @param vector3f The new normal
      */
     public void setNormal( int vertexIndex, Vector3f vector3f )
     {
         if ( !checkNormalsExistence( vector3f != null ) )
             return;
         
         normals.set( vertexIndex, getNormalsOffset() / 4L, vector3f );
     }
     
     /**
      * Sets the normals of the vertices starting at the specified index.
      * 
      * @param vertexIndex The index of the first vertex to modify
      * @param floatArray The new normals. Its size must be a multiple of 3.
      */
     public void setNormals( int vertexIndex, float[] floatArray )
     {
         if ( !checkNormalsExistence( floatArray != null ) )
             return;
         
         assert ( floatArray.length % 3 == 0 );
         normals.set( vertexIndex, getNormalsSize(), normalsOffset / 4L, floatArray );
     }
     
     /**
      * Sets the normals of the vertices at the specified index.
      * 
      * @param vertexIndex The index of the first vertex to modify
      * @param vector3fArray The new normals
      */
     public void setNormals( int vertexIndex, Vector3f[] vector3fArray )
     {
         if ( !checkNormalsExistence( vector3fArray != null ) )
             return;
         
         for ( int i = 0; i < vector3fArray.length; i++ )
             normals.set( vertexIndex + i, getNormalsOffset() / 4L, vector3fArray[ i ] );
     }
     
     /**
      * Sets the normals of the vertices starting at the specified index.
      * 
      * @param vertexIndex The index of the first vertex to modify
      * @param floatArray The new normal data. Its size must be a multiple of 3.
      * @param startIndex The first coordinate to use in the given array. The
      *            first element of the array to be used will be startIndex*3.
      * @param length The number of vertices to modify
      */
     public void setNormals( int vertexIndex, float[] floatArray, int startIndex, int length )
     {
         if ( !checkNormalsExistence( floatArray != null ) )
             return;
         
         assert ( floatArray.length % 3 == 0 );
         normals.set( vertexIndex, getNormalsSize(), normalsOffset / 4L, floatArray, startIndex * getNormalsSize(), length * getNormalsSize() );
     }
     
     /**
      * Sets the normals of the vertices starting at the specified index.
      * 
      * @param vertexIndex The index of the first vertex to modify
      * @param vector3fArray The new normals
      * @param startIndex The index of the first coordinate to use in the given
      *            array.
      * @param length The number of vertices to modify
      */
     public void setNormals( int vertexIndex, Vector3f[] vector3fArray, int startIndex, int length )
     {
         if ( !checkNormalsExistence( vector3fArray != null ) )
             return;
         
         for ( int i = 0; i < length; i++ )
             normals.set( vertexIndex + i, getNormalsOffset() / 4L, vector3fArray[ startIndex + i ] );
     }
     
     /**
      * Sets the normal of the vertex at the given index.
      * 
      * @param vertexIndex THe index of the vertex to modify
      * @param x The new normal
      * @param y The new normal
      * @param z The new normal
      */
     public void setNormal( int vertexIndex, float x, float y, float z )
     {
         if ( !checkNormalsExistence( true ) )
             return;
         
         normals.set( vertexIndex, normalsOffset / 4L, x, y, z );
     }
     
     public void getNormal( int vertexIndex, float[] floatArray )
     {
         if ( !hasNormals() )
             throw new IllegalStateException( "No normals defined" );
         
         normals.get( vertexIndex, 3, normalsOffset / 4L, floatArray, 0, 3 );
     }
     
     public Vector3f getNormal( int index, Vector3f normal )
     {
         if ( !hasNormals() )
             throw new IllegalStateException( "No normals defined" );
         
         normals.get( index, getNormalsOffset() / 4L, normal );
         
         return ( normal );
     }
     
     public void getNormals( int vertexIndex, float[] floatArray )
     {
         if ( !hasNormals() )
             throw new IllegalStateException( "No normals defined" );
         
         for ( int i = 0; i < floatArray.length / 3; i++ )
            normals.get( vertexIndex + i, 3, normalsOffset / 4L, floatArray, i * 3, 3 );
     }
     
     public void getNormals( int index0, Vector3f[] vector3fArray )
     {
         if ( !hasNormals() )
             throw new IllegalStateException( "No normals defined" );
         
         for ( int i = vector3fArray.length - 1; i > -1; i-- )
             normals.get( i - index0, getNormalsOffset() / 4L, vector3fArray[ i ] );
     }
     
     /**
      * @return <code>true</code>, if this Geometry contains color data.
      */
     public final boolean hasColors()
     {
         return ( hasColors );
     }
     
     private final void createColors( int colorSize )
     {
         colors = newNioFloatData( maxVertices, colorSize, 0, reversed );
         this.colorSize = colorSize;
         hasColors = true;
         vertexFormat |= COLORS;
     }
     
     protected final boolean checkColorsExistence( int colorSize, boolean b )
     {
         if ( colors == null )
         {
             if ( !b )
                 return ( false );
             
             if ( ( colorSize != 3 ) && ( colorSize != 4 ) )
                 throw new IllegalArgumentException( "illegal color size" );
             
             createColors( colorSize );
             
             if ( isInterleaved() )
                 makeInterleaved();
         }
         else if ( this.colorSize != colorSize )
         {
             throw new IllegalArgumentException( "mismatching color size" );
         }
         
         return ( true );
     }
     
     /**
      * Sets the color of the vertex at the specified index.
      * 
      * @param vertexIndex The index of the vertex to modify
      * @param floatArray The new color data. The first {@link #colorSize}
      *            elements will be used.
      */
     public void setColor( int vertexIndex, float[] floatArray )
     {
         int l = ( floatArray != null ) ? floatArray.length : 0;
         if ( !checkColorsExistence( l, floatArray != null ) )
             return;
         
         colors.set( vertexIndex, colorSize, colorsOffset / 4L, floatArray, 0, colorSize );
     }
     
     /**
      * Sets the color of the vertex at the specified index.
      * 
      * @param vertexIndex The index of the vertex to modify
      * @param colorf The new color.
      */
     public void setColor( int vertexIndex, Colorf colorf )
     {
         int s = ( colorf != null ) ? ( colorf.hasAlpha() ? 4 : 3 ) : 3;
         if ( !checkColorsExistence( s, colorf != null ) )
             return;
         
         colors.set( vertexIndex, colorSize, colorsOffset / 4L, colorf );
     }
     
     /**
      * Sets the colors of the vertices starting at the specified index.
      * 
      * @param vertexIndex The index of the first vertex to modify
      * @param colorSize
      * @param floatArray The new color value. Its size must be a multiple of
      *            {@link #colorSize}.
      */
     public void setColors( int vertexIndex, int colorSize, float[] floatArray )
     {
         if ( ( colorSize != 4 ) && ( colorSize != 3 ) )
             throw new IllegalArgumentException( "Illegal color-size " + colorSize );
         
         if ( !checkColorsExistence( colorSize, floatArray != null ) )
             return;
         
         assert ( floatArray.length % colorSize == 0 );
         
         colors.set( vertexIndex, colorSize, colorsOffset / 4L, floatArray );
     }
     
     /**
      * Sets the colors of the vertices starting at the specified index.
      * 
      * @param vertexIndex The index of the first vertex to modify.
      * @param colorfArray The new color values.
      */
     public void setColors( int vertexIndex, Colorf[] colorfArray )
     {
         int s = ( colorfArray != null ) ? ( colorfArray[ 0 ].hasAlpha() ? 4 : 3 ) : 3;
         if ( !checkColorsExistence( s, colorfArray != null ) )
             return;
         
         for ( int i = 0; i < colorfArray.length; i++ )
             colors.set( vertexIndex + i, colorSize, colorsOffset / 4L, colorfArray[ i ] );
     }
     
     /**
      * Sets the colors of the vertices starting at the specified index.
      * 
      * @param vertexIndex The index of the first vertex to be modified.
      * @param floatArray The new color data. The size of the array must be a
      *            multiple of {@link #colorSize}.
      * @param colorSize
      * @param startIndex The index of the first color in the given array. The
      *            first read item of the array will be startIndex*colorSize.
      * @param length The number of colors to copy
      */
     public void setColors( int vertexIndex, int colorSize, float[] floatArray, int startIndex, int length )
     {
         if ( ( colorSize != 4 ) && ( colorSize != 3 ) )
             throw new IllegalArgumentException( "Illegal color-size " + colorSize );
         
         if ( !checkColorsExistence( colorSize, floatArray != null ) )
             return;
         
         assert ( floatArray.length % colorSize == 0 );
         
         colors.set( vertexIndex, colorSize, colorsOffset / 4L, floatArray, startIndex * colorSize, length * colorSize );
     }
     
     /**
      * Sets the colors of the vertices starting at the given index.
      * 
      * @param vertexIndex The index of the first vertex to modify
      * @param colorfArray The new color data.
      * @param startIndex The index of the first color in the given array
      * @param length The number of vertices to modify.
      */
     public void setColors( int vertexIndex, Colorf[] colorfArray, int startIndex, int length )
     {
         final int colorSize = ( colorfArray != null ) ? ( colorfArray[ 0 ].hasAlpha() ? 4 : 3 ) : 3;
         
         if ( !checkColorsExistence( colorSize, colorfArray != null ) )
             return;
         
         for ( int i = 0; i < length; i++ )
             colors.set( vertexIndex + i, colorSize, colorsOffset / 4L, colorfArray[ startIndex + i ] );
     }
     
     /**
      * Sets the color of the vertex at the specified index.
      * 
      * @param vertexIndex The index of the vertex to modify
      * @param r
      * @param g
      * @param b
      */
     public void setColor( int vertexIndex, float r, float g, float b )
     {
         if ( !checkColorsExistence( 3, true ) )
             return;
         
         colors.set( vertexIndex, colorsOffset / 4L, r, g, b );
     }
     
     /**
      * Sets the color of the vertex at the specified index.
      * 
      * @param vertexIndex The index of the vertex to modify
      * @param r
      * @param g
      * @param b
      * @param a
      */
     public void setColor( int vertexIndex, float r, float g, float b, float a )
     {
         if ( !checkColorsExistence( 4, true ) )
             return;
         
         colors.set( vertexIndex, colorsOffset / 4L, r, g, b, a );
     }
     
     /**
      * Gets the color of the vertex at the specified index.
      * 
      * @param vertexIndex The index of the vertex to modify
      * @param floatArray
      */
     public void getColor( int vertexIndex, float[] floatArray )
     {
         if ( !hasColors() )
             throw new Error( "No colors set" );
         
         colors.get( vertexIndex, colorSize, colorsOffset / 4L, floatArray, 0, colorSize );
     }
     
     /**
      * Gets the color of the vertex at the specified index.
      * 
      * @param vertexIndex The index of the vertex to modify
      * @param colorf The new color.
      * 
      * @return the passed color instance back again.
      */
     public Colorf getColor( int vertexIndex, Colorf colorf )
     {
         if ( !hasColors() )
             throw new Error( "No colors set" );
         
         colors.get( vertexIndex, colorSize, colorsOffset / 4L, colorf );
         
         return ( colorf );
     }
     
     /**
      * Gets the color of the vertex at the specified index.
      * 
      * @param vertexIndex The index of the vertex to modify
      * @param floatArray The new color.
      */
     public void getColors( int vertexIndex, float[] floatArray )
     {
         if ( !hasColors() )
             throw new Error( "No colors set" );
         
         for ( int i = 0; i < floatArray.length / colorSize; i++ )
             colors.get( vertexIndex + i, colorSize, colorsOffset / 4L, floatArray, i, colorSize );
     }
     
     /**
      * @return <code>true</code>, if this Geometry contains texture-coordinate data (for any texture-unit).
      */
     public final boolean hasTextureCoordinates()
     {
         return ( texCoordSetMap.length > 0 );
     }
     
     /**
      * @param unit the texture unit (always 1, 2, 3 or 4)
      * 
      * @return <code>true</code>, if this Geometry contains texture-coordinate data (for the given texture-unit).
      */
     public final boolean hasTextureCoordinates( int unit )
     {
         if ( texCoords == null )
             return ( false );
         
         return ( ( texCoords.length > unit ) && ( texCoords[ unit ] != null ) );
     }
     
     private final void createTexCoord( int unit, int texCoordSize )
     {
         texCoords[ unit ] = newNioFloatData( maxVertices, texCoordSize, 0, reversed );
         textureUnitSize[ unit ] = texCoordSize;
         vertexFormat |= TEXTURE_COORDINATES;
     }
     
     protected final boolean checkTexCoordExistence( int unit, int texCoordSize, boolean b )
     {
         if ( texCoords == null )
         {
             if ( !b )
                 return ( false );
             
             texCoords = new GeomNioFloatData[ unit + 1 ];
             createTexCoord( unit, texCoordSize );
             rebuildTexCoordSetMap();
             
             if ( isInterleaved() )
                 makeInterleaved();
             
             return ( true );
         }
         
         if ( unit < texCoords.length )
         {
             final GeomNioFloatData old = texCoords[ unit ];
             if ( old == null )
             {
                 createTexCoord( unit, texCoordSize );
                 rebuildTexCoordSetMap();
                 
                 if ( isInterleaved() )
                     makeInterleaved();
                 
                 return ( true );
             }
             
             if ( textureUnitSize[ unit ] == texCoordSize )
                 return ( true );
             
             throw new IllegalArgumentException( "mismatching TexCoord size" );
         }
         
         GeomNioFloatData[] texCoords2 = new GeomNioFloatData[ unit + 1 ];
         System.arraycopy( texCoords, 0, texCoords2, 0, texCoords.length );
         texCoords = texCoords2;
         createTexCoord( unit, texCoordSize );
         rebuildTexCoordSetMap();
         
         if ( isInterleaved() )
             makeInterleaved();
         
         return ( true );
     }
     
     /**
      * Sets the texture coordinate of the vertex at the specified index for the
      * specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the vertex to modify
      * @param floatArray The new texture coordinate data. Its size must be 2, 3 or 4.
      */
     public void setTextureCoordinate( int unit, int vertexIndex, float[] floatArray )
     {
         if ( ( floatArray.length < 1 ) || ( floatArray.length > 4 ) )
             throw new IllegalArgumentException( "texture coords must be of size 1, 2, 3 or 4" );
         
         checkTexCoordExistence( unit, floatArray.length, true );
         
         texCoords[ unit ].set( vertexIndex, textureUnitSize[ unit ], texCoordsOffsets[ unit ] / 4L, floatArray, 0, floatArray.length );
     }
     
     /**
      * Sets the texture coordinate of the vertex at the specified index for the
      * specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the vertex to modify
      * @param texCoord
      */
     public void setTextureCoordinate( int unit, int vertexIndex, TexCoordf<?> texCoord )
     {
         checkTexCoordExistence( unit, texCoord.getSize(), true );
         
         texCoords[ unit ].set( vertexIndex, texCoordsOffsets[ unit ] / 4L, texCoord );
     }
     
     /**
      * Sets the texture coordinate of the vertex starting at the specified index
      * for the specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the first vertex to modify
      * @param texCoordSize 1, 2, 3 or 4
      * @param floatArray The new coordinate data. Its size must be a multiple of
      *            2, 3 or 4 depending on texCoordSet format.
      */
     public void setTextureCoordinates( int unit, int vertexIndex, int texCoordSize, float[] floatArray )
     {
         if ( ( texCoordSize < 1 ) || ( texCoordSize > 4 ) )
             throw new IllegalArgumentException( "texture coords must be of size 1, 2, 3 or 4" );
         
         checkTexCoordExistence( unit, texCoordSize, true );
         
         texCoords[ unit ].set( vertexIndex, textureUnitSize[ unit ], texCoordsOffsets[ unit ] / 4L, floatArray, 0, floatArray.length );
     }
     
     /**
      * Sets the texture coordinate of the vertex starting at the specified index
      * for the specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the first vertex to modify
      * @param texCoordArray
      */
     public void setTextureCoordinates( int unit, int vertexIndex, TexCoordf<?>[] texCoordArray )
     {
         checkTexCoordExistence( unit, texCoordArray[ 0 ].getSize(), true );
         
         for ( int i = 0; i < texCoordArray.length; i++ )
             texCoords[ unit ].set( vertexIndex + i, texCoordsOffsets[ unit ] / 4L, texCoordArray[ i ] );
     }
     
     /**
      * Sets the texture coordinate of the vertex starting at the specified index
      * for the specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the first vertex to modify
      * @param texCoordSize
      * @param floatArray The new coordinate data. Its size must be a multiple of 2.
      * @param startIndex
      * @param length
      */
     public void setTextureCoordinates( int unit, int vertexIndex, int texCoordSize, float[] floatArray, int startIndex, int length )
     {
         checkTexCoordExistence( unit, texCoordSize, true );
         
         texCoords[ unit ].set( vertexIndex, textureUnitSize[ unit ], texCoordsOffsets[ unit ] / 4L, floatArray, startIndex * texCoordSize, length * texCoordSize );
     }
     
     /**
      * Sets the texture coordinate of the vertex starting at the specified index
      * for the specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the first vertex to modify
      * @param texCoordArray
      * @param startIndex
      * @param length
      */
     public void setTextureCoordinates( int unit, int vertexIndex, TexCoordf<?>[] texCoordArray, int startIndex, int length )
     {
         checkTexCoordExistence( unit, texCoordArray[ 0 ].getSize(), true );
         
         for ( int i = 0; i < length; i++ )
         {
             texCoords[ unit ].set( vertexIndex + i, texCoordsOffsets[ unit ] / 4L, texCoordArray[ startIndex + i ] );
         }
     }
     
     /**
      * Sets the texture coordinate of the vertex at the specified index for the
      * specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the vertex to modify
      * @param s
      */
     public void setTextureCoordinate( int unit, int vertexIndex, float s )
     {
         checkTexCoordExistence( unit, 1, true );
         
         texCoords[ unit ].set( vertexIndex, texCoordsOffsets[ unit ] / 4L, s );
     }
     
     /**
      * Sets the texture coordinate of the vertex at the specified index for the
      * specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the vertex to modify
      * @param s
      * @param t
      */
     public void setTextureCoordinate( int unit, int vertexIndex, float s, float t )
     {
         checkTexCoordExistence( unit, 2, true );
         
         texCoords[ unit ].set( vertexIndex, texCoordsOffsets[ unit ] / 4L, s, t );
     }
     
     /**
      * Sets the texture coordinate of the vertex at the specified index for the
      * specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the vertex to modify
      * @param s
      * @param t
      * @param r
      */
     public void setTextureCoordinate( int unit, int vertexIndex, float s, float t, float r )
     {
         checkTexCoordExistence( unit, 3, true );
         
         texCoords[ unit ].set( vertexIndex, texCoordsOffsets[ unit ] / 4L, s, t, r );
     }
     
     /**
      * Sets the texture coordinate of the vertex at the specified index for the
      * specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the vertex to modify
      * @param s
      * @param t
      * @param r
      * @param q
      */
     public void setTextureCoordinate( int unit, int vertexIndex, float s, float t, float r, float q )
     {
         checkTexCoordExistence( unit, 4, true );
         
         texCoords[ unit ].set( vertexIndex, texCoordsOffsets[ unit ] / 4L, s, t, r, q );
     }
     
     /**
      * Gets the texture coordinate of the vertex at the specified index for the
      * specified coordinates set.
      * 
      * @param unit The coordinates set.
      * @param vertexIndex The index of the vertex to modify
      * @param floatArray The new texture coordinate data. Its size must be 2, 3 or 4.
      */
     public void getTextureCoordinate( int unit, int vertexIndex, float[] floatArray )
     {
         if ( floatArray == null )
             throw new NullPointerException( "The passed value buffer is null" );
         
         final int size = getTexCoordSize( unit );
         
         if ( size <= 0 )
             throw new Error( "This TextureCoodinate does not exist" );
         
         texCoords[ unit ].get( vertexIndex, size, texCoordsOffsets[ unit ] / 4L, floatArray, 0, size );
     }
     
     /**
      * Gets the texture coordinate of the vertex at the specified index for the
      * specified coordinates set.
      * 
      * @param unit The coordinates set
      * @param vertexIndex The index of the vertex to modify
      * @param texCoord
      * 
      * @param <TexCoordf_> parameter type restriction
      * 
      * @return the texture coordinate
      */
     public <TexCoordf_ extends TexCoordf<?>> TexCoordf_ getTextureCoordinate( int unit, int vertexIndex, TexCoordf_ texCoord )
     {
         final int size = getTexCoordSize( unit );
         
         if ( size <= 0 )
             throw new Error( "This TextureCoodinate does not exist" );
         
         if ( size != texCoord.getSize() )
             throw new Error( "Mismatching texture coord size (" + texCoord.getSize() + " != " + size + ")" );
         
         texCoords[ unit ].get( vertexIndex, texCoordsOffsets[ unit ] / 4L, texCoord );
         
         return ( texCoord );
     }
     
     public void getTextureCoordinates( int unit, int vertexIndex, float[] floatArray )
     {
         if ( !hasTextureCoordinates( unit ) )
             throw new Error( "The texture unti " + unit + " does not yet have texture coordinate data." );
         
         final int size = getTexCoordSize( unit );
         
         texCoords[ unit ].get( vertexIndex, size, texCoordsOffsets[ unit ] / 4L, floatArray, 0, floatArray.length / size );
     }
     
     /**
      * @return <code>true</code>, if this Geometry contains vertex-attribute data.
      */
     public final boolean hasVertexAttributes()
     {
         return ( vertexAttribs != null );
     }
     
     /**
      * @return <code>true</code>, if this Geometry contains vertex-attribute data at the given index.
      * 
      * @param attribIndex
      */
     public final boolean hasVertexAttributes( int attribIndex )
     {
         if ( vertexAttribs == null )
             return ( false );
         
         return ( ( vertexAttribs.length > attribIndex ) && ( vertexAttribs[ attribIndex ] != null ) );
     }
     
     /**
      * @return the number of vertex attributes in the Geometry.
      */
     public final int getVertexAttributesCount()
     {
         if ( vertexAttribs == null )
             return ( 0 );
         
         return ( vertexAttribs.length );
     }
     
     private final void createVertexAttribute( int attribIndex, int attribSize )
     {
         vertexAttribs[ attribIndex ] = newNioFloatData( maxVertices, attribSize, 0, reversed );
         vertexAttribsSize[ attribIndex ] = attribSize;
         vertexFormat |= VERTEX_ATTRIBUTES;
     }
     
     protected final void checkAttributeExistence( int attribIndex, int attribSize, boolean b )
     {
         if ( vertexAttribs == null )
         {
             if ( !b )
                 return;
             
             vertexAttribs = new GeomNioFloatData[ attribIndex + 1 ];
             createVertexAttribute( attribIndex, attribSize );
             
             if ( isInterleaved() )
                 makeInterleaved();
         }
         else if ( attribIndex < vertexAttribs.length )
         {
             final GeomNioFloatData old = vertexAttribs[ attribIndex ];
             if ( old == null )
             {
                 createVertexAttribute( attribIndex, attribSize );
                 
                 if ( isInterleaved() )
                     makeInterleaved();
             }
             else
             {
                 if ( vertexAttribsSize[ attribIndex ] == attribSize )
                     return;
                 
                 throw new IllegalArgumentException( "mismatching attribute size" );
             }
         }
         else
         {
             GeomNioFloatData[] vertexAttribs2 = new GeomNioFloatData[ attribIndex + 1 ];
             System.arraycopy( vertexAttribs, 0, vertexAttribs2, 0, vertexAttribs.length );
             vertexAttribs = vertexAttribs2;
             createVertexAttribute( attribIndex, attribSize );
             
             if ( isInterleaved() )
                 makeInterleaved();
         }
     }
     
     /**
      * Sets the vertex attribute of the vertex at the specified index for the
      * specified attribute.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the vertex to modify
      * @param floatArray The new attribute data. Its size must be 1, 2, 3 or 4.
      */
     public void setVertexAttribute( int attribIndex, int vertexIndex, float[] floatArray )
     {
         if ( ( floatArray.length < 1 ) || ( floatArray.length > 4 ) )
             throw new IllegalArgumentException( "vertex attributes must be of size 1, 2, 3 or 4" );
         
         checkAttributeExistence( attribIndex, floatArray.length, true );
         
         vertexAttribs[ attribIndex ].set( vertexIndex, floatArray.length, vertexAttribsOffsets[ attribIndex ] / 4L, floatArray );
     }
     
     /**
      * Sets the vertex attribute of the vertex at the specified index for the
      * specified attribute.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the vertex to modify
      * @param value The new attribute data.
      */
     public void setVertexAttribute( int attribIndex, int vertexIndex, TupleNf<?> value )
     {
         checkAttributeExistence( attribIndex, value.getSize(), true );
         
         vertexAttribs[ attribIndex ].set( vertexIndex, vertexAttribsOffsets[ attribIndex ] / 4L, value );
     }
     
     /**
      * Sets the vertex attributes.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the first vertex to modify
      * @param values The new attribute data.
      * @param attribSize the size of each attribute element (1, 2, 3, 4)
      */
     public void setVertexAttributes( int attribIndex, int vertexIndex, float[] values, int attribSize )
     {
         if ( ( attribSize < 1 ) || ( attribSize > 4 ) )
             throw new IllegalArgumentException( "vertex attributes must be of size 1, 2, 3 or 4" );
         
         checkAttributeExistence( attribIndex, attribSize, values != null );
         
         if ( values == null )
             return;
         
         vertexAttribs[ attribIndex ].set( vertexIndex, attribSize, vertexAttribsOffsets[ attribIndex ] / 4L, values );
     }
     
     /**
      * Sets the vertex attributes.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the first vertex to modify
      * @param values The new attribute data.
      */
     public void setVertexAttributes( int attribIndex, int vertexIndex, TupleNf<?>[] values )
     {
         int s = ( values != null ) ? values[ 0 ].getSize() : 0;
         checkAttributeExistence( attribIndex, s, values != null );
         
         if ( values == null )
             return;
         
         for ( int i = 0; i < values.length; i++ )
             vertexAttribs[ attribIndex ].set( vertexIndex + i, vertexAttribsOffsets[ attribIndex ] / 4L, values[ i ] );
     }
     
     /**
      * Sets the vertex attributes.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the first vertex to modify
      * @param values The new attribute data.
      * @param attribsSize (1, 2, 3, 4)
      * @param startIndex
      * @param length
      */
     public void setVertexAttributes( int attribIndex, int vertexIndex, float[] values, int attribsSize, int startIndex, int length )
     {
         if ( ( attribsSize < 1 ) || ( attribsSize > 4 ) )
             throw new IllegalArgumentException( "vertex attributes must be of size 1, 2, 3 or 4" );
         
         checkAttributeExistence( attribIndex, attribsSize, values != null );
         
         if ( values == null )
             return;
         
         vertexAttribs[ attribIndex ].set( vertexIndex, attribsSize, vertexAttribsOffsets[ attribIndex ] / 4L, values, startIndex * attribsSize, length * attribsSize );
     }
     
     /**
      * Sets the vertex attributes.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the first vertex to modify
      * @param values The new attribute data.
      * @param startIndex
      * @param length
      */
     public void setVertexAttributes( int attribIndex, int vertexIndex, TupleNf<?>[] values, int startIndex, int length )
     {
         int s = ( values != null ) ? values[ 0 ].getSize() : 0;
         checkAttributeExistence( attribIndex, s, values != null );
         
         if ( values == null )
             return;
         
         for ( int i = 0; i < length; i++ )
         {
             vertexAttribs[ attribIndex ].set( vertexIndex + i, vertexAttribsOffsets[ attribIndex ] / 4L, values[ startIndex + i ] );
         }
     }
     
     /**
      * Sets the vertex attribute of the vertex at the specified index for the
      * specified attribute.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the vertex to modify
      * @param value The new attribute data.
      */
     public void setVertexAttribute( int attribIndex, int vertexIndex, float value )
     {
         checkAttributeExistence( attribIndex, 1, true );
         
         vertexAttribs[ attribIndex ].set( vertexIndex, vertexAttribsOffsets[ attribIndex ] / 4L, value );
     }
     
     public void getVertexAttribute( int attribIndex, int vertexIndex, float[] floatArray )
     {
         if ( !hasVertexAttributes( attribIndex ) )
             throw new Error( "This vertex attribute does not exist." );
         
         vertexAttribs[ attribIndex ].getData( vertexIndex, vertexAttribsOffsets[ attribIndex ] / 4L, floatArray, 0, floatArray.length / getVertexAttribSize( attribIndex ) );
     }
     
     /**
      * Gets the vertex attribute of the vertex at the specified index for the
      * specified attribute.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the vertex to modify
      * @param value The buffer for the attribute data.
      */
     public void getVertexAttribute( int attribIndex, int vertexIndex, TupleNf<?> value )
     {
         if ( !hasVertexAttributes( attribIndex ) )
             throw new IllegalStateException( "vertex attribute " + attribIndex + " does not exist." );
         
         vertexAttribs[ attribIndex ].get( vertexIndex, vertexAttribsOffsets[ attribIndex ] / 4L, value );
     }
     
     public void getVertexAttributes( int attribIndex, int vertexIndex, float[] floatArray )
     {
         if ( !hasVertexAttributes( attribIndex ) )
             throw new Error( "This vertex attribute does not exist." );
         
         vertexAttribs[ attribIndex ].getData( vertexIndex, vertexAttribsOffsets[ attribIndex ] / 4L, floatArray, 0, floatArray.length / getVertexAttribSize( attribIndex ) );
     }
     
     /**
      * Gets the vertex attribute of the vertex at the specified index for the
      * specified attribute.
      * 
      * @param attribIndex The attributes set.
      * @param vertexIndex The index of the vertex to modify
      * 
      * @return the vertex attribute.
      */
     public float getVertexAttribute( int attribIndex, int vertexIndex )
     {
         if ( !hasVertexAttributes( attribIndex ) )
             throw new IllegalStateException( "vertex attribute " + attribIndex + " does not exist." );
         
         return ( vertexAttribs[ attribIndex ].get( vertexIndex, vertexAttribsOffsets[ attribIndex ] / 4L ) );
     }
     
     /**
      * Applies the the n-th Triangle to the GeometryArray.
      * This method must be overridden by concrete classes to fix the vertex-index (e.g. for an IndexedTriangleArray)
      * 
      * @param i0 the first triangle's vertex-index
      * @param i1 the second triangle's vertex-index
      * @param i2 the third triangle's vertex-index
      * @param triangle
      * 
      * @return true, if the triangle could be applied
      */
     @Override
     public boolean setTriangle( int i0, int i1, int i2, Triangle triangle )
     {
         super.setTriangle( i0, i1, i2, triangle );
         
         if ( triangle.hasFeature( Vertex3f.NORMALS ) )
         {
             // write triangle data
             setNormal( i0, triangle.getVertexNormalA() );
             setNormal( i1, triangle.getVertexNormalB() );
             setNormal( i2, triangle.getVertexNormalC() );
         }
         
         if ( triangle.hasFeature( Vertex3f.COLORS ) )
         {
             // write triangle data
             setColor( i0, triangle.getVertexColorA() );
             setColor( i1, triangle.getVertexColorB() );
             setColor( i2, triangle.getVertexColorC() );
         }
         
         if ( triangle.hasFeature( Vertex3f.TEXTURE_COORDINATES ) && ( this.getTexCoordSize( 0 ) == triangle.getTexCoordsSize() ) )
         {
             // write triangle data
             setTextureCoordinate( 0, i0, triangle.getVertexTexCoordA() );
             setTextureCoordinate( 0, i1, triangle.getVertexTexCoordB() );
             setTextureCoordinate( 0, i2, triangle.getVertexTexCoordC() );
         }
         
         return ( true );
     }
     
     /**
      * Retrieves the the n-th Triangle from the GeometryArray.
      * This method must be overridden by concrete classes to fix the vertex-index (e.g. for an IndexedTriangleArray)
      * 
      * @param i0 the first triangle's vertex-index
      * @param i1 the second triangle's vertex-index
      * @param i2 the third triangle's vertex-index
      * @param triangle
      * 
      * @return true, if the triangle could be retrieved
      */
     @Override
     public boolean getTriangle( int i0, int i1, int i2, Triangle triangle )
     {
         super.getTriangle( i0, i1, i2, triangle );
         
         if ( this.hasFeature( GeometryDataContainer.NORMALS ) && triangle.hasFeature( Vertex3f.NORMALS ) )
         {
             // read triangle data
             getNormal( i0, triangle.getVertexNormalA() );
             getNormal( i1, triangle.getVertexNormalB() );
             getNormal( i2, triangle.getVertexNormalC() );
         }
         
         if ( this.hasFeature( GeometryDataContainer.COLORS ) && triangle.hasFeature( Vertex3f.COLORS ) )
         {
             // read triangle data
             getColor( i0, triangle.getVertexColorA() );
             getColor( i1, triangle.getVertexColorB() );
             getColor( i2, triangle.getVertexColorC() );
         }
         
         if ( this.hasFeature( GeometryDataContainer.TEXTURE_COORDINATES ) && triangle.hasFeature( Vertex3f.TEXTURE_COORDINATES ) &&
              ( this.getTexCoordSize( 0 ) == triangle.getTexCoordsSize() ) )
         {
             // read triangle data
             getTextureCoordinate( 0, i0, triangle.getVertexTexCoordA() );
             getTextureCoordinate( 0, i1, triangle.getVertexTexCoordB() );
             getTextureCoordinate( 0, i2, triangle.getVertexTexCoordC() );
         }
         
         triangle.setVertexIndices( i0, i1, i2 );
         
         return ( true );
     }
     
     public float[] getColorRefFloat()
     {
         return ( ( colors == null ) ? null : colors.getData() );
     }
     
     public float[] getNormalRefFloat()
     {
         return ( ( normals == null ) ? null : normals.getData() );
     }
     
     public float[] getTexCoordRefFloat( int unit )
     {
         return ( ( texCoords == null ) ? null : ( ( texCoords[ unit ] == null ) ? null : texCoords[ unit ].getData() ) );
     }
     
     /**
      * Sets up the Geometry to be stored in a single NIO buffer for interleaved geometry.
      * 
      * @param features
      * @param colorAlpha
      * @param tuSizes the sizes of the texture-units (may be null, if not contained in the features mask)
      * @param vaSizes the sizes of the vertex-arrays (may be null, if not contained in the features mask)
      */
     public void makeInterleaved( int features, boolean colorAlpha, int[] tuSizes, int[] vaSizes )
     {
         //if ( isInterleaved() )
         //    return;
         
         int stride = 0;
         
         if ( ( features & GeometryDataContainer.COORDINATES ) != 0 )
         {
             stride += 3;
         }
         
         if ( ( features & GeometryDataContainer.NORMALS ) != 0 )
         {
             stride += 3;
         }
         
         if ( ( features & GeometryDataContainer.COLORS ) != 0 )
         {
             if ( colorAlpha )
             {
                 stride += 4;
             }
             else
             {
                 stride += 3;
             }
         }
         
         if ( ( features & GeometryDataContainer.TEXTURE_COORDINATES ) != 0 )
         {
             for ( int i = 0; i < tuSizes.length; i++ )
             {
                 stride += tuSizes[ i ];
             }
         }
         
         if ( ( features & GeometryDataContainer.VERTEX_ATTRIBUTES ) != 0 )
         {
             for ( int i = 0; i < vaSizes.length; i++ )
             {
                 stride += vaSizes[ i ];
             }
         }
         
         
         final int numVertices = getVertexCount();
         GeomNioFloatData interData = newNioFloatData( numVertices, stride, stride * 4, reversed );
         final FloatBuffer interBuff = interData.getBuffer();
         
         final int colorSize = colorAlpha ? 4 : 3;
         
         float[] values = new float[ 4 ];
         for ( int i = 0; i < numVertices; i++ )
         {
             if ( isInterleaved || ( coords != null ) )
             {
                 getCoordinate( i, values );
                 interBuff.put( values, 0, getCoordinatesSize() );
             }
             
             if ( hasNormals )
             {
                 getNormal( i, values );
                 interBuff.put( values, 0, 3 );
             }
             
             if ( hasColors )
             {
                 values[ 3 ] = 0f;
                 getColor( i, values );
                 interBuff.put( values, 0, colorSize );
             }
             
             if ( tuSizes != null )
             {
                 for ( int t = 0; t < tuSizes.length; t++ )
                 {
                     if ( hasTextureCoordinates( t ) )
                     {
                         values[ 0 ] = 0f; values[ 1 ] = 0f; values[ 2 ] = 0f; values[ 3 ] = 0f;
                         getTextureCoordinate( t, i, values );
                         interBuff.put( values, 0, tuSizes[ t ] );
                     }
                     else
                     {
                         interBuff.position( interBuff.position() + tuSizes[ t ] );
                     }
                 }
             }
             
             if ( vaSizes != null )
             {
                 for ( int j = 0; j < vaSizes.length; j++ )
                 {
                     if ( hasVertexAttributes( j ) )
                     {
                         values[ 0 ] = 0f; values[ 1 ] = 0f; values[ 2 ] = 0f; values[ 3 ] = 0f;
                         getVertexAttribute( j, i, values );
                         interBuff.put( values, 0, vaSizes[ j ] );
                     }
                     else
                     {
                         interBuff.position( interBuff.position() + vaSizes[ j ] );
                     }
                 }
             }
         }
         
         
         if ( tuSizes != null )
         {
             int numTUs = 0;
             for ( int t = 0; t < tuSizes.length; t++ )
             {
                 if ( tuSizes[ t ] > 0 )
                     numTUs++;
             }
             
             texCoordSetMap = new int[ numTUs ];
             int t2 = 0;
             for ( int t = 0; t < tuSizes.length; t++ )
             {
                 if ( tuSizes[ t ] > 0 )
                     texCoordSetMap[ t2++ ] = t;
             }
             
             texCoordSetMap_public = new int[ texCoordSetMap.length ];
             System.arraycopy( texCoordSetMap, 0, texCoordSetMap_public, 0, texCoordSetMap.length );
         }
         
         texCoords = null;
         
         for ( int t = 0; t < textureUnitSize.length; t++ )
         {
             if ( ( tuSizes == null ) || ( tuSizes.length <= t ) )
                 textureUnitSize[ t ] = 0;
             else if ( t < tuSizes.length )
                 textureUnitSize[ t ] = tuSizes[ t ];
             else
                 textureUnitSize[ t ] = 0;
         }
         
         vertexAttribs = null;
         
         for ( int j = 0; j < vertexAttribsSize.length; j++ )
         {
             if ( ( vaSizes == null ) || ( vaSizes.length <= j ) )
                 vertexAttribsSize[ j ] = 0;
             else if ( j < vaSizes.length )
                 vertexAttribsSize[ j ] = vaSizes[ j ];
             else
                 vertexAttribsSize[ j ] = vaSizes[ j ];
         }
         
         
         this.interleavedData = interData;
         
         
         this.coords = ( ( features & COORDINATES ) != 0 ) ? interleavedData : null;
         this.normals = ( ( features & NORMALS ) != 0 ) ? interleavedData : null;
         this.colors = ( ( features & COLORS ) != 0 ) ? interleavedData : null;
         if ( ( features & TEXTURE_COORDINATES ) != 0 )
         {
             this.texCoords = new GeomNioFloatData[ tuSizes.length ];
             for ( int i = 0; i < tuSizes.length; i++ )
             {
                 if ( tuSizes[ i ] > 0 )
                     texCoords[ i ] = interleavedData;
                 else
                     texCoords[ i ] = null;
             }
         }
         else
         {
             this.texCoords = null;
         }
         if ( ( features & VERTEX_ATTRIBUTES ) != 0 )
         {
             this.vertexAttribs = new GeomNioFloatData[ vaSizes.length ];
             for ( int i = 0; i < vaSizes.length; i++ )
             {
                 if ( vaSizes[ i ] > 0 )
                     vertexAttribs[ i ] = interleavedData;
                 else
                     vertexAttribs[ i ] = null;
             }
         }
         else
         {
             this.vertexAttribs = null;
         }
         
         if ( ( features & GeometryDataContainer.COLORS ) != 0 )
         {
             if ( colorAlpha )
                 this.colorSize = 4;
             else
                 this.colorSize = 3;
         }
         else
         {
             this.colorSize = 0;
         }
         
         
         long offset = 0L;
         
         if ( ( features & GeometryDataContainer.COORDINATES ) != 0 )
         {
             coordsOffset = offset * 4L;
             offset += 3L;
         }
         
         if ( ( features & GeometryDataContainer.NORMALS ) != 0 )
         {
             normalsOffset = offset * 4L;
             offset += 3L;
             hasNormals = true;
         }
         
         if ( ( features & GeometryDataContainer.COLORS ) != 0 )
         {
             colorsOffset = offset * 4L;
             if ( colorAlpha )
             {
                 offset += 3L;
             }
             else
             {
                 offset += 4L;
             }
             hasColors = true;
         }
         
         if ( ( features & GeometryDataContainer.TEXTURE_COORDINATES ) != 0 )
         {
             for ( int i = 0; i < tuSizes.length; i++ )
             {
                 texCoordsOffsets[ i ] = offset * 4L;
                 offset += tuSizes[ i ];
             }
         }
         
         if ( ( features & GeometryDataContainer.VERTEX_ATTRIBUTES ) != 0 )
         {
             for ( int i = 0; i < vaSizes.length; i++ )
             {
                 vertexAttribsOffsets[ i ] = offset * 4L;
                 offset += vaSizes[ i ];
             }
         }
         
         
         this.vertexFormat = features;
         this.isInterleaved = true;
     }
     
     /**
      * Sets up the Geometry to be stored in a single NIO buffer for interleaved geometry.
      */
     public void makeInterleaved()
     {
         if ( coords == null )
             throw new Error( "You cannot use the parameterless makeInterleaved() method before any geometry data has been initialized." );
         
         final int features = this.getVertexFormat();
         final boolean colorAlpha = this.hasColors() ? this.hasColorAlpha() : false;
         final int[] tuSizes;
         if ( texCoords == null )
         {
             tuSizes = null;
         }
         else
         {
             tuSizes = new int[ this.texCoords.length ];
             for ( int i = 0; i < texCoords.length; i++ )
             {
                 tuSizes[ i ] = textureUnitSize[ i ];
             }
         }
         final int[] vaSizes;
         if ( vertexAttribs == null )
         {
             vaSizes = null;
         }
         else
         {
             vaSizes = new int[ this.vertexAttribs.length ];
             for ( int i = 0; i < vertexAttribs.length; i++ )
             {
                 vaSizes[ i ] = vertexAttribsSize[ i ];
             }
         }
         
         makeInterleaved( features, colorAlpha, tuSizes, vaSizes );
     }
     
     @Override
     protected void copyFrom( SimpleGeometryDataContainer original, boolean forceDuplicate )
     {
         GeometryDataContainer o = (GeometryDataContainer)original;
         
         super.copyFrom( o, forceDuplicate );
         
         this.texCoordSetMap = ( o.texCoordSetMap != null ) ? o.texCoordSetMap.clone() : null;
         this.texCoordSetMap_public = ( o.texCoordSetMap_public != null ) ? o.texCoordSetMap_public.clone() : null;
         System.arraycopy( o.textureUnitSize, 0, this.textureUnitSize, 0, o.textureUnitSize.length );
         this.colorSize = o.colorSize;
         this.vertexFormat = o.vertexFormat;
         this.hasNormals = o.hasNormals;
         this.hasColors = o.hasColors;
         this.colorSize = o.colorSize;
         this.normalsOffset = o.normalsOffset;
         this.colorsOffset = o.colorsOffset;
         System.arraycopy( o.texCoordsOffsets, 0, this.texCoordsOffsets, 0, o.texCoordsOffsets.length );
         System.arraycopy( o.vertexAttribsOffsets, 0, this.vertexAttribsOffsets, 0, o.vertexAttribsOffsets.length );
         
         if ( !o.isInterleaved() )
         {
             if ( o.hasNormals() )
             {
                 this.normals = o.normals.duplicateGeomData( true );
             }
             
             if ( o.hasColors() )
             {
                 this.colors = o.colors.duplicateGeomData( true );
             }
             
             if ( o.hasTextureCoordinates() )
             {
                 this.texCoords = new GeomNioFloatData[ o.texCoords.length ];
                 for ( int i = 0; i < texCoords.length; i++ )
                 {
                     if ( o.texCoords[ i ] != null )
                     {
                         this.texCoords[ i ] = o.texCoords[ i ].duplicateGeomData( true );
                     }
                 }
             }
             
             if ( o.vertexAttribs != null )
             {
                 this.vertexAttribs = new GeomNioFloatData[ o.vertexAttribs.length ];
                 for ( int i = 0; i < vertexAttribs.length; i++ )
                 {
                     if ( o.vertexAttribs[ i ] != null )
                     {
                         this.vertexAttribs[ i ] = o.vertexAttribs[ i ].duplicateGeomData( true );
                     }
                 }
             }
         }
     }
     
     protected GeometryDataContainer( GeometryArrayType type, boolean hasIndex, int coordsSize, int vertexCount, int[] stripCounts, int indexCount )
     {
         super( type, hasIndex, coordsSize, vertexCount, stripCounts, indexCount );
         
         this.vertexFormat = COORDINATES;
     }
     
     public GeometryDataContainer( GeometryArrayType type, int coordsSize, int vertexCount )
     {
         this( type, false, coordsSize, vertexCount, null, 0 );
     }
     
     public GeometryDataContainer( GeometryArrayType type, int coordsSize, int vertexCount, int indexCount )
     {
         this( type, true, coordsSize, vertexCount, null, indexCount );
     }
     
     public GeometryDataContainer( GeometryArrayType type, int coordsSize, int vertexCount, int[] stripCounts )
     {
         this( type, false, coordsSize, vertexCount, stripCounts, 0 );
     }
     
     public GeometryDataContainer( GeometryArrayType type, int coordsSize, int vertexCount, int[] stripCounts, int indexCount )
     {
         this( type, true, coordsSize, vertexCount, stripCounts, indexCount );
     }
 }
