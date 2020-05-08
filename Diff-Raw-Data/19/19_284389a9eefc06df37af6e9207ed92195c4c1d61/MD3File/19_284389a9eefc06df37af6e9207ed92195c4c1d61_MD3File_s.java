 /**
  * Copyright (c) 2007-2008, JAGaToo Project Group all rights reserved.
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
 package org.jagatoo.loaders.models.md3;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 
 import org.jagatoo.datatypes.NamedObject;
 import org.jagatoo.loaders.IncorrectFormatException;
 import org.jagatoo.loaders.ParsingException;
 import org.jagatoo.loaders.models._util.AnimationFactory;
 import org.jagatoo.loaders.models._util.AppearanceFactory;
 import org.jagatoo.loaders.models._util.GeometryFactory;
 import org.jagatoo.loaders.models._util.NodeFactory;
 import org.jagatoo.loaders.models._util.SpecialItemsHandler;
 import org.jagatoo.loaders.models._util.AnimationFactory.AnimationType;
 import org.jagatoo.loaders.models._util.SpecialItemsHandler.SpecialItemType;
 import org.jagatoo.loaders.textures.AbstractTexture;
 import org.jagatoo.logging.JAGTLog;
 import org.jagatoo.util.streams.LittleEndianDataInputStream;
 import org.jagatoo.util.streams.StreamUtils;
 import org.openmali.FastMath;
 import org.openmali.spatial.bounds.BoundsType;
 import org.openmali.vecmath2.AxisAngle3f;
 import org.openmali.vecmath2.Matrix3f;
 import org.openmali.vecmath2.Matrix4f;
 import org.openmali.vecmath2.Point3f;
 import org.openmali.vecmath2.Vector3f;
 import org.openmali.vecmath2.Vertex3f;
 
 /**
  * Reads MD3 model files and pushes them through interfaces to
  * the actual rendering- and scenegraph implementation.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class MD3File
 {
     private final LittleEndianDataInputStream in;
     
     private final MD3Header header;
     
     private MD3Frame[] frames;
     
     private final HashMap<String, NamedObject> shaderCache = new HashMap<String, NamedObject>();
     
     private static final GeometryFactory.GeometryType geomType = GeometryFactory.GeometryType.INDEXED_TRIANGLE_ARRAY;
     private static final float COORDINATE_SCALE = 1.0f / 64f;
     
     static final String fixPath( String path )
     {
         if ( path.indexOf( '\\' ) >= 0 )
         {
             path = path.replace( '\\', '/' );
         }
         
         if ( path.indexOf( ' ' ) >= 0 )
         {
             path = path.replace( " ", "%20" );
         }
         
         return( path );
     }
     
     private void readFrames() throws IOException, IncorrectFormatException, ParsingException
     {
         long t0 = System.currentTimeMillis();
         JAGTLog.debug( "Loading MD3 frames..." );
         
         in.skipBytes( header.frameOffset - in.getPointer() );
         
         frames = new MD3Frame[ header.numFrames ];
         
         for ( int i = 0; i < header.numFrames; i++ )
         {
             frames[i] = MD3Frame.readFrame( in );
         }
         
         JAGTLog.debug( "done. (", ( System.currentTimeMillis() - t0 ) / 1000f, " seconds)" );
     }
     
     private void readTags( boolean convertZup2Yup, float scale, SpecialItemsHandler siHandler ) throws IOException, IncorrectFormatException, ParsingException
     {
         long t0 = System.currentTimeMillis();
         JAGTLog.debug( "Loading MD3 tags..." );
         
         in.skipBytes( header.tagOffset - in.getPointer() );
         
         Vector3f translation = Vector3f.fromPool();
         Matrix3f rotation = Matrix3f.fromPool();
         Matrix4f tmp = Matrix4f.fromPool();
         tmp.setIdentity();
         
         String[] tagNames = new String[header.numTags];
         Matrix4f[][] tagFrames = new Matrix4f[header.numTags][];
         
         for ( int t = 0; t < header.numTags; t++ )
         {
             tagFrames[t] = new Matrix4f[header.numFrames];
         }
         
         for ( int f = 0; f < header.numFrames; f++ )
         {
             for ( int t = 0; t < header.numTags; t++ )
             {
                String name = fixPath( in.readCString( 64, true ) );
                 if ( f == 0 )
                 {
                     tagNames[t] = name;
                 }
                 StreamUtils.readTuple3f( in, translation );
                 StreamUtils.readMatrix3f( in, rotation );
                 
                 if ( convertZup2Yup )
                 {
                     // TODO: How do I do this with a simple matrix multiplication? And is it really simpler?
                     
                     //rotation.mul( Matrix3f.Z_UP_TO_Y_UP, rotation );
                     AxisAngle3f aa = AxisAngle3f.fromPool();
                     aa.set( rotation );
                     aa.set( aa.getX(), aa.getZ(), -aa.getY(), aa.getAngle() );
                     rotation.set( aa );
                     AxisAngle3f.toPool( aa );
                 }
                 
                 Matrix4f transform = new Matrix4f();
                 //transform.setIdentity();
                 transform.set( rotation );
                 if ( convertZup2Yup )
                 {
                     tmp.m03( translation.getX() * scale );
                     tmp.m13( translation.getZ() * scale );
                     tmp.m23( -translation.getY() * scale );
                 }
                 else
                 {
                     tmp.m03( translation.getX() * scale );
                     tmp.m13( translation.getY() * scale );
                     tmp.m23( translation.getZ() * scale );
                 }
                 transform.mul( tmp, transform );
                 
                 tagFrames[t][f] = transform;
             }
         }
         
         Matrix4f.toPool( tmp );
         Vector3f.toPool( translation );
         Matrix3f.toPool( rotation );
         
         for ( int t = 0; t < header.numTags; t++ )
         {
             siHandler.addSpecialItem( SpecialItemType.MOUNT_TRANSFORM, tagNames[t], tagFrames[t] );
         }
         
         JAGTLog.debug( "done. (", ( System.currentTimeMillis() - t0 ) / 1000f, " seconds)" );
     }
     
     private AbstractTexture loadTexture( String texName, URL baseURL, AppearanceFactory appFactory )
     {
         // First try to load the texture by the given name...
         AbstractTexture texture = appFactory.loadOrGetTexture( texName, baseURL, true, true, true, true, true );
         
         if ( !appFactory.isFallbackTexture( texture ) )
             return( texture );
         
         if ( texName.endsWith( ".tga" ) )
         {
             String texBaseName = texName.substring( 0, texName.length() - 4 );
             
             // Try without extension...
             texture = appFactory.loadOrGetTexture( texBaseName, baseURL, true, true, true, true, true );
             
             if ( !appFactory.isFallbackTexture( texture ) )
                 return( texture );
             
             // Try as jpeg...
             texture = appFactory.loadOrGetTexture( texBaseName + ".jpg", baseURL, true, true, true, true, true );
             
             if ( !appFactory.isFallbackTexture( texture ) )
                 return( texture );
         }
         else
         {
             // Try as jpeg...
             texture = appFactory.loadOrGetTexture( texName + ".jpg", baseURL, true, true, true, true, true );
             
             if ( !appFactory.isFallbackTexture( texture ) )
                 return( texture );
         }
         
         int lastSlashPos = texName.lastIndexOf( '/' );
         if ( lastSlashPos >= 0 )
         {
             String texBaseName = texName.substring( lastSlashPos + 1 );
             
             // Try without path...
             texture = appFactory.loadOrGetTexture( texBaseName, baseURL, true, true, true, true, true );
             
             if ( !appFactory.isFallbackTexture( texture ) )
                 return( texture );
             
             if ( texBaseName.endsWith( ".tga" ) )
             {
                 texBaseName = texBaseName.substring( 0, texBaseName.length() - 4 );
                 
                 // Try without path and extension...
                 texture = appFactory.loadOrGetTexture( texBaseName, baseURL, true, true, true, true, true );
                 
                 if ( !appFactory.isFallbackTexture( texture ) )
                     return( texture );
                 
                 // Try as jpeg...
                 texture = appFactory.loadOrGetTexture( texBaseName + ".jpg", baseURL, true, true, true, true, true );
                 
                 if ( !appFactory.isFallbackTexture( texture ) )
                     return( texture );
             }
             else
             {
                 // Try as jpeg...
                 texture = appFactory.loadOrGetTexture( texBaseName + ".jpg", baseURL, true, true, true, true, true );
                 
                 if ( !appFactory.isFallbackTexture( texture ) )
                     return( texture );
             }
         }
         
         /*
          * The texture wasn't found at all.
          * Log the fail and return fallback-texture!
          */
         
         JAGTLog.printlnEx( "Couldn't find texture resource \"", texName, "\"!" );
         
         return( texture );
     }
     
     private NamedObject[] readShaders( int shadersOffset, int numShaders, URL baseURL, AppearanceFactory appFactory, NodeFactory nodeFactory ) throws IOException, IncorrectFormatException, ParsingException
     {
         long t0 = System.currentTimeMillis();
         JAGTLog.debug( "Loading MD3 shaders..." );
         
         in.skipBytes( header.surfaceOffset + shadersOffset - in.getPointer() );
         
         NamedObject[] shaders = new NamedObject[ numShaders ];
         
         for ( int i = 0; i < numShaders; i++ )
         {
             String shaderName = fixPath( in.readCString( 64, true ) );
             int shaderIndex = in.readInt();
             
             NamedObject shader = shaderCache.get( shaderName );
             if ( shader == null )
             {
                 AbstractTexture texture;
                 if ( ( shaderName == null ) || ( shaderName.length() == 0 ) )
                     texture = appFactory.getFallbackTexture();
                 else
                     texture = loadTexture( shaderName, baseURL, appFactory );
                 
                 shader = appFactory.createAppearance( shaderName, 0 );
                 appFactory.applyTexture( texture, 0, shader );
                 
                 shaderCache.put( shaderName, shader );
             }
             
             shaders[ shaderIndex ] = shader;
         }
         
         JAGTLog.debug( "done. (", ( System.currentTimeMillis() - t0 ) / 1000f, " seconds)" );
         
         return( shaders );
     }
     
     private void readTriangles( int trianglesOffset, int numTriangles, GeometryFactory geomFactory, NamedObject geometry ) throws IOException, IncorrectFormatException, ParsingException
     {
         long t0 = System.currentTimeMillis();
         JAGTLog.debug( "Loading MD3 triangles..." );
         
         in.skipBytes( header.surfaceOffset + trianglesOffset - in.getPointer() );
         
         int[] buffer = new int[ 3 ];
         
         for ( int i = 0; i < numTriangles; i++ )
         {
             buffer[0] = in.readInt();
             buffer[1] = in.readInt();
             buffer[2] = in.readInt();
             
             geomFactory.setIndex( geometry, geomType, i * 3, buffer, 0, 3 );
         }
         
         JAGTLog.debug( "done. (", ( System.currentTimeMillis() - t0 ) / 1000f, " seconds)" );
     }
     
     private void readTextureCoordinates( int textureCoordsOffset, int numVertices, GeometryFactory geomFactory, NamedObject geometry ) throws IOException, IncorrectFormatException, ParsingException
     {
         long t0 = System.currentTimeMillis();
         JAGTLog.debug( "Loading MD3 texture-coordinates..." );
         
         in.skipBytes( header.surfaceOffset + textureCoordsOffset - in.getPointer() );
         
         float[] buffer = new float[ 2 ];
         
         for ( int i = 0; i < numVertices; i++ )
         {
             buffer[0] = in.readFloat();
             buffer[1] = 1f - in.readFloat();
             
             geomFactory.setTexCoord( geometry, geomType, 0, 2, i, buffer, 0, 1 );
         }
         
         JAGTLog.debug( "done. (", ( System.currentTimeMillis() - t0 ) / 1000f, " seconds)" );
     }
     
     private void readCoordinatesAndNormals( int numFrames, int coordNormalOffset, int numVertices, GeometryFactory geomFactory, boolean convertZup2Yup, float scale, NamedObject geometry, AnimationFactory animFactory, Object[] keyFrames ) throws IOException, IncorrectFormatException, ParsingException
     {
         long t0 = System.currentTimeMillis();
         JAGTLog.debug( "Loading MD3 vertex-coordinates and -normals (for all frames)..." );
         
         in.skipBytes( header.surfaceOffset + coordNormalOffset - in.getPointer() );
         
         Point3f coord = Point3f.fromPool();
         Vector3f normal = Vector3f.fromPool();
         float[] buffer = new float[ 3 ];
         
         for ( int f = 0; f < numFrames; f++ )
         {
             float[] keyFrameCoords = null;
             float[] keyFrameNormals = null;
             
             if ( numFrames > 1 )
             {
                 keyFrameCoords = new float[ numVertices * 3 ];
                 keyFrameNormals = new float[ numVertices * 3 ];
             }
             
             for ( int i = 0; i < numVertices; i++ )
             {
                 buffer[0] = (float)in.readShort() * scale;
                 buffer[1] = (float)in.readShort() * scale;
                 buffer[2] = (float)in.readShort() * scale;
                 
                 if ( convertZup2Yup )
                 {
                     coord.set( buffer );
                     
                     buffer[0] = coord.getX();
                     buffer[1] = coord.getZ();
                     buffer[2] = -coord.getY();
                 }
                 
                 if ( f == 0 )
                 {
                     geomFactory.setCoordinate( geometry, geomType, i, buffer, 0, 1 );
                 }
                 
                 if ( numFrames > 1 )
                 {
                     System.arraycopy( buffer, 0, keyFrameCoords, i * 3, 3 );
                 }
                 
                 // normal
                 int azimuth = in.readByte();
                 int zenith = in.readByte();
                 float lat = ( zenith * FastMath.TWO_PI ) / 255f;
                 float lng = ( azimuth * FastMath.TWO_PI ) / 255f;
                 
                 buffer[0] = FastMath.cos( lat ) * FastMath.sin( lng );
                 buffer[1] = FastMath.sin( lat ) * FastMath.sin( lng );
                 buffer[2] = FastMath.cos( lng );
                 
                 if ( convertZup2Yup )
                 {
                     normal.set( buffer );
                     
                     normal.add( coord );
                     Matrix3f.Z_UP_TO_Y_UP.transform( normal );
                     normal.sub( coord.getX(), coord.getZ(), -coord.getY() );
                     
                     normal.get( buffer );
                 }
                 
                 if ( f == 0 )
                 {
                     geomFactory.setNormal( geometry, geomType, i, buffer, 0, 1 );
                 }
                 
                 if ( numFrames > 1 )
                 {
                     System.arraycopy( buffer, 0, keyFrameNormals, i * 3, 3 );
                 }
             }
             
             if ( numFrames > 1 )
             {
                 keyFrames[f] = animFactory.createMeshDeformationKeyFrame( keyFrameCoords, keyFrameNormals );
             }
         }
         
         Vector3f.toPool( normal );
         Point3f.toPool( coord );
         
         JAGTLog.debug( "done. (", ( System.currentTimeMillis() - t0 ) / 1000f, " seconds)" );
     }
     
     private void readSurfaces( URL baseURL, AppearanceFactory appFactory, GeometryFactory geomFactory, boolean convertZup2Yup, float scale, NodeFactory nodeFactory, AnimationFactory animFactory, SpecialItemsHandler siHandler, NamedObject rootGroup ) throws IOException, IncorrectFormatException, ParsingException
     {
         long t0 = System.currentTimeMillis();
         JAGTLog.debug( "Loading MD3 surfaces..." );
         
         in.skipBytes( header.surfaceOffset - in.getPointer() );
         
         for ( int s = 0; s < header.numSurfaces; s++ )
         {
             if ( in.readInt() != MD3Header.MAGIC_NUMBER )
                 throw new IncorrectFormatException( "Invalid magic number found at MD3 surfaces block " + s + "." );
             
            String surfaceName = fixPath( in.readCString( 64, true ) );
             /*int flags = */in.readInt();
             int numFrames = in.readInt();
             int numShaders = in.readInt();
             int numVertices = in.readInt();
             int numTriangles = in.readInt();
             int trianglesOffset = in.readInt();
             int shadersOffset = in.readInt();
             int textureCoordsOffset = in.readInt();
             int coordNormalOffset = in.readInt();
             /*int surfaceEnd = */in.readInt();
             
             NamedObject geometry = geomFactory.createInterleavedGeometry( surfaceName,
                                                                           geomType, 3, numVertices, numTriangles * 3, null,
                                                                           Vertex3f.COORDINATES | Vertex3f.NORMALS | Vertex3f.TEXTURE_COORDINATES,
                                                                           false, new int[] { 2 }, null
                                                                         );
             
             Object[] keyFrames = null;
             if ( numFrames > 1 )
             {
                 keyFrames = new Object[ numFrames ];
             }
             
             NamedObject[] shaders = readShaders( shadersOffset, numShaders, baseURL, appFactory, nodeFactory );
             readTriangles( trianglesOffset, numTriangles, geomFactory, geometry );
             readTextureCoordinates( textureCoordsOffset, numVertices, geomFactory, geometry );
             readCoordinatesAndNormals( numFrames, coordNormalOffset, numVertices, geomFactory, convertZup2Yup, scale, geometry, animFactory, keyFrames );
             
             NamedObject shape = nodeFactory.createShape( surfaceName, geometry, ( shaders.length > 0 ) ? shaders[0] : null, BoundsType.SPHERE );
             
             nodeFactory.addNodeToGroup( shape, rootGroup );
             siHandler.addSpecialItem( SpecialItemType.SHAPE, shape.getName(), shape );
             siHandler.addSpecialItem( SpecialItemType.NAMED_OBJECT, shape.getName(), shape );
             
             if ( numFrames > 1 )
             {
                 Object animController = animFactory.createAnimationController( AnimationType.MESH_DEFORMATION_KEY_FRAMES, keyFrames, shape );
                 siHandler.addSpecialItem( SpecialItemType.ANIMATION_CONTROLLERS, null, new Object[] { new Object[] { animController }, keyFrames.length } );
             }
         }
         
         JAGTLog.debug( "done loading ", header.numSurfaces, " surfaces. (", ( System.currentTimeMillis() - t0 ) / 1000f, " seconds)" );
     }
     
     private MD3File( InputStream in, URL baseURL, AppearanceFactory appFactory, GeometryFactory geomFactory, boolean convertZup2Yup, float scale, NodeFactory nodeFactory, AnimationFactory animFactory, SpecialItemsHandler siHandler, NamedObject rootGroup ) throws IOException, IncorrectFormatException, ParsingException
     {
         if ( !( in instanceof BufferedInputStream ) )
             in = new BufferedInputStream( in );
         
         this.in = new LittleEndianDataInputStream( in );
         
         this.header = MD3Header.readHeader( this.in );
         
         readFrames();
         readTags( convertZup2Yup, scale, siHandler );
         readSurfaces( baseURL, appFactory, geomFactory, convertZup2Yup, scale * COORDINATE_SCALE, nodeFactory, animFactory, siHandler, rootGroup );
     }
     
     public static final void load( InputStream in, URL baseURL, AppearanceFactory appFactory, GeometryFactory geomFactory, boolean convertZup2Yup, float scale, NodeFactory nodeFactory, AnimationFactory animFactory, SpecialItemsHandler siHandler, NamedObject rootGroup ) throws IOException, IncorrectFormatException, ParsingException
     {
         new MD3File( in, baseURL, appFactory, geomFactory, convertZup2Yup, scale, nodeFactory, animFactory, siHandler, rootGroup );
     }
 }
