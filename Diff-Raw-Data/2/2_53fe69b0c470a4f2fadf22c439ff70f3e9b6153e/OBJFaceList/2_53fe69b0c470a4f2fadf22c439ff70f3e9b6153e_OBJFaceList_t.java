 /**
  * Copyright (c) 2003-2007, Xith3D Project Group all rights reserved.
  * 
  * Portions based on the Java3D interface, Copyright by Sun Microsystems.
  * Many thanks to the developers of Java3D and Sun Microsystems for their
  * innovation and design.
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
 package org.jagatoo.loaders.models.obj;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.openmali.vecmath.TexCoord2f;
 import org.openmali.vecmath.Vector3f;
 
 /**
  * A list of the faces that build up the obj file
  * 
  * @author Kevin Glass
  * @author Marvin Froehlich (aka Qudus)
  */
 public class OBJFaceList
 {
     private List<OBJFace> faces = new ArrayList<OBJFace>();
     private List<Vector3f> verts;
     private List<Vector3f> normals;
     private List<TexCoord2f> texs;
     
     private boolean texturesUsed = false;
     private boolean normalsUsed = false;
     
     private boolean supportNormals = true;
     private boolean supportTextures = true;
     
     public void setFaces( List<OBJFace> faces )
     {
         this.faces = faces;
     }
     
     public List<OBJFace> getFaces()
     {
         return( faces );
     }
     
     /*
     public void setVertexList( List<Vector3f> verts )
     {
         this.verts = verts;
     }
     */
     
     public List<Vector3f> getVertexList()
     {
         return( verts );
     }
     
     /*
     public void setNormalList( List<Vector3f> normals )
     {
         this.normals = normals;
     }
     */
     
     public List<Vector3f> getNormalList()
     {
         return( normals );
     }
     
     /*
     public void setTexList( List<TexCoord2f> texs )
     {
         this.texs = texs;
     }
     */
     
     public List<TexCoord2f> getTexList()
     {
         return( texs );
     }
     
     public void setNormalsUsed( boolean used )
     {
         this.normalsUsed = used;
     }
     
     public boolean normalsUsed()
     {
         return( normalsUsed );
     }
     
     public boolean normalsSupported()
     {
         return( supportNormals );
     }
     
     public void setTexturesUsed( boolean used )
     {
         this.texturesUsed = used;
     }
     
     public boolean texturesUsed()
     {
         return( texturesUsed );
     }
     
     public boolean texturesSupported()
     {
         return( supportTextures );
     }
     
     
     private int parseInt( String token )
     {
         if ( token.isEmpty() )
         {
             return( -1 );
         }
         
         return( Integer.parseInt( token ) );
     }
     
     private int parseVertIndex(String token)
     {
         if ( token.indexOf( "/" ) >= 0 )
         {
             return( parseInt( token.substring( 0, token.indexOf( "/" ) ) ) );
         }
         
         return( parseInt( token ) );
     }
     
     private int parseTextureIndex( String token )
     {
         int result;
         
         int slashPos = token.indexOf( "/" );
         
         if ( slashPos < 0 )
         {
             return( -1 );
         }
         token = token.substring( slashPos + 1 );
         slashPos = token.indexOf( "/" );
         
         if ( slashPos >= 0 )
         {
             result = parseInt( token.substring( 0, slashPos ) );
             if (result != -1)
             {
                 texturesUsed = true;
             }
             
             return( result );
         }
         
         result = parseInt( token );
         if ( result != -1 )
         {
             texturesUsed = true;
         }
         
         return( result );
     }
     
     private int parseNormalIndex( String token )
     {
         int result;
         
         if (token.indexOf( "/" ) < 0)
         {
             return( -1 );
         }
         token = token.substring( token.indexOf( "/" ) + 1 );
         if ( token.indexOf( "/" ) < 0 )
         {
             return( -1 );
         }
         token = token.substring( token.indexOf( "/" ) + 1 );
         
         if ( token.indexOf( "/" ) >= 0 )
         {
             result = parseInt( token.substring( 0, token.indexOf( "/" ) ) );
             if ( result != -1 )
             {
                normalsUsed = true;
             }
             
             return( result );
         }
         
         result = parseInt( token );
         if ( result != -1 )
         {
            normalsUsed = true;
         }
         
         return( result );
     }
     
     public void add( String line, OBJMaterial mat )
     {
         StringTokenizer tokens = new StringTokenizer( line );
         
         OBJFace face = new OBJFace( this, tokens.countTokens() - 1, mat );
         
         tokens.nextToken();
         while ( tokens.hasMoreTokens() )
         {
             String pt = tokens.nextToken();
             
             int vi = parseVertIndex( pt );
             int ti = parseTextureIndex( pt );
             int ni = parseNormalIndex( pt );
             
            face.add( ( vi > 0 ) ? vi - 1 : -1, ( ni > 0 ) ? ni - 1 : -1, ( ti > 0 ) ? ti - 1 : -1 );
         }
         
         faces.add( face );
     }
     
     public OBJFaceList( List<Vector3f> verts, List<Vector3f> normals, List<TexCoord2f> texs )
     {
         this.verts = verts;
         this.normals = normals;
         this.texs = texs;
     }
 }
