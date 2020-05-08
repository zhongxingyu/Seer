 package org.jagatoo.spatial.polygons;
 
 import org.jagatoo.datatypes.Ray3f;
 import org.jagatoo.datatypes.Vertex3f;
 import org.openmali.FastMath;
 import org.openmali.vecmath.Color3f;
 import org.openmali.vecmath.Matrix3f;
 import org.openmali.vecmath.Point3f;
 import org.openmali.vecmath.TexCoord2f;
 import org.openmali.vecmath.Tuple3f;
 import org.openmali.vecmath.Vector3f;
 
 /**
  * A Triangle is composed of three vertices.
  * 
  * @author Marvin Froehlich (aka Qudus)
  * @author Arne Mueller [added ray-intersection methods]
  * @author Amos Wenger (aka BlueSky)
  * @author Andrew Hanson (aka Patheros) [Made Triangle GC-friendly]
  */
 public class Triangle extends Polygon
 {
     private Point3f coordA = null;
     private Point3f coordB = null;
     private Point3f coordC = null;
     
     private Vector3f normalA = null;
     private Vector3f normalB = null;
     private Vector3f normalC = null;
     
     private Color3f colorA = null;
     private Color3f colorB = null;
     private Color3f colorC = null;
     
     private TexCoord2f texCoordA = null;
     private TexCoord2f texCoordB = null;
     private TexCoord2f texCoordC = null;
     
     private Vector3f faceNormal = null;
     
     private int vertexIndexA = -1;
     private int vertexIndexB = -1;
     private int vertexIndexC = -1;
     
     
     /*
      * static final fields that are reused for computations, so the
      * computations don't tax the garbage collector
      */
     private static final Point3f  tmpPnt  = new Point3f();
     private static final Matrix3f tmpMat  = new Matrix3f();
     private static final Vector3f tmpVec1 = new Vector3f();
     private static final Vector3f tmpVec2 = new Vector3f();
     private static final Vector3f tmpVec3 = new Vector3f();
     private static final Vector3f tmpVec4 = new Vector3f();
     private static final Vector3f tmpVec5 = new Vector3f();
     private static final Vector3f tmpVec6 = new Vector3f();
     
     
     public void setVertexCoordA( Tuple3f coord )
     {
         if ( this.coordA == null )
             this.coordA = new Point3f( coord );
         else
             this.coordA.set( coord );
         
         addFeature( Vertex3f.COORDINATES );
     }
     
     public void setVertexCoordB( Tuple3f coord )
     {
         if ( this.coordB == null )
             this.coordB = new Point3f( coord );
         else
             this.coordB.set( coord );
         
         addFeature( Vertex3f.COORDINATES );
     }
     
     public void setVertexCoordC( Tuple3f coord )
     {
         if ( this.coordC == null )
             this.coordC = new Point3f( coord );
         else
             this.coordC.set( coord );
         
         addFeature( Vertex3f.COORDINATES );
     }
     
     public void getVertexCoordA( Tuple3f coord )
     {
         coord.set( this.coordA );
     }
     
     public Tuple3f getVertexCoordA()
     {
         return( this.coordA );
     }
     
     public void getVertexCoordB( Tuple3f coord )
     {
         coord.set( this.coordB );
     }
     
     public Tuple3f getVertexCoordB()
     {
         return( this.coordB );
     }
     
     public void getVertexCoordC( Tuple3f coord )
     {
         coord.set( this.coordC );
     }
     
     public Tuple3f getVertexCoordC()
     {
         return( this.coordC );
     }
     
     
     public void setVertexCoords( Tuple3f coordA, Tuple3f coordB, Tuple3f coordC )
     {
         setVertexCoordA( coordA );
         setVertexCoordB( coordB );
         setVertexCoordC( coordC );
     }
     
     public void getVertexCoords( Tuple3f coordA, Tuple3f coordB, Tuple3f coordC )
     {
         getVertexCoordA( coordA );
         getVertexCoordB( coordB );
         getVertexCoordC( coordC );
     }
     
     
     public void setVertexNormalA( Vector3f normal )
     {
         if ( this.normalA == null )
             this.normalA = new Vector3f( normal );
         else
             this.normalA.set( normal );
         
         addFeature( Vertex3f.NORMALS );
     }
     
     public void setVertexNormalB( Vector3f normal )
     {
         if ( this.normalB == null )
             this.normalB = new Vector3f( normal );
         else
             this.normalB.set( normal );
         
         addFeature( Vertex3f.NORMALS );
     }
     
     public void setVertexNormalC( Vector3f normal )
     {
         if ( this.normalC == null )
             this.normalC = new Vector3f( normal );
         else
             this.normalC.set( normal );
         
         addFeature( Vertex3f.NORMALS );
     }
     
     public void getVertexNormalA( Vector3f normal )
     {
         normal.set( this.normalA );
     }
     
     public Vector3f getVertexNormalA()
     {
         return( this.normalA );
     }
     
     public void getVertexNormalB( Vector3f normal )
     {
         normal.set( this.normalB );
     }
     
     public Vector3f getVertexNormalB()
     {
         return( this.normalB );
     }
     
     public void getVertexNormalC( Vector3f normal )
     {
         normal.set( this.normalC );
     }
     
     public Vector3f getVertexNormalC()
     {
         return( this.normalC );
     }
     
     
     public void setVertexNormals( Vector3f normalA, Vector3f normalB, Vector3f normalC )
     {
         setVertexNormalA( normalA );
         setVertexNormalB( normalB );
         setVertexNormalC( normalC );
     }
     
     public void getVertexNormals( Vector3f normalA, Vector3f normalB, Vector3f normalC )
     {
         getVertexNormalA( normalA );
         getVertexNormalB( normalB );
         getVertexNormalC( normalC );
     }
     
     
     public void setVertexColorA( Color3f color )
     {
         if ( this.colorA == null )
             this.colorA = new Color3f( color );
         else
             this.colorA.set( color );
         
         addFeature( Vertex3f.COLORS_3 );
     }
     
     public void setVertexColorB( Color3f color )
     {
         if ( this.colorB == null )
             this.colorB = new Color3f( color );
         else
             this.colorB.set( color );
         
         addFeature( Vertex3f.COLORS_3 );
     }
     
     public void setVertexColorC( Color3f color )
     {
         if ( this.colorC == null )
             this.colorC = new Color3f( color );
         else
             this.colorC.set( color );
         
         addFeature( Vertex3f.COLORS_3 );
     }
     
     public void getVertexColorA( Color3f color )
     {
         color.set( this.colorA );
     }
     
     public Color3f getVertexColorA()
     {
         return( this.colorA );
     }
     
     public void getVertexColorB( Color3f color )
     {
         color.set( this.colorB );
     }
     
     public Color3f getVertexColorB()
     {
         return( this.colorB );
     }
     
     public void getVertexColorC( Color3f color )
     {
         color.set( this.colorC );
     }
     
     public Color3f getVertexColorC()
     {
         return( this.colorC );
     }
     
     
     public void setVertexColors( Color3f colorA, Color3f colorB, Color3f colorC )
     {
         setVertexColorA( colorA );
         setVertexColorB( colorB );
         setVertexColorC( colorC );
     }
     
     public void getVertexColors( Color3f colorA, Color3f colorB, Color3f colorC )
     {
         getVertexColorA( colorA );
         getVertexColorB( colorB );
         getVertexColorC( colorC );
     }
     
     
     public void setVertexTexCoordA( TexCoord2f texCoord )
     {
         if ( this.texCoordA == null )
             this.texCoordA = new TexCoord2f( texCoord );
         else
             this.texCoordA.set( texCoord );
         
         addFeature( Vertex3f.TEXTURE_COORDINATES_2 );
     }
     
     public void setVertexTexCoordB( TexCoord2f texCoord )
     {
         if ( this.texCoordB == null )
             this.texCoordB = new TexCoord2f( texCoord );
         else
             this.texCoordB.set( texCoord );
         
         addFeature( Vertex3f.TEXTURE_COORDINATES_2 );
     }
     
     public void setVertexTexCoordC( TexCoord2f texCoord )
     {
         if ( this.texCoordC == null )
             this.texCoordC = new TexCoord2f( texCoord );
         else
             this.texCoordC.set( texCoord );
         
         addFeature( Vertex3f.TEXTURE_COORDINATES_2 );
     }
     
     public void getVertexTexCoordA( TexCoord2f texCoord )
     {
         texCoord.set( this.texCoordA );
     }
     
     public TexCoord2f getVertexTexCoordA()
     {
         return( this.texCoordA );
     }
     
     public void getVertexTexCoordB( TexCoord2f texCoord )
     {
         texCoord.set( this.texCoordB );
     }
     
     public TexCoord2f getVertexTexCoordB()
     {
         return( this.texCoordB );
     }
     
     public void getVertexTexCoordC( TexCoord2f texCoord )
     {
         texCoord.set( this.texCoordC );
     }
     
     public TexCoord2f getVertexTexCoordC()
     {
         return( this.texCoordC );
     }
     
     
     public void setVertexTexCoords( TexCoord2f texCoordA, TexCoord2f texCoordB, TexCoord2f texCoordC )
     {
         setVertexTexCoordA( texCoordA );
         setVertexTexCoordB( texCoordB );
         setVertexTexCoordC( texCoordC );
     }
     
     public void getVertexTexCoords( TexCoord2f texCoordA, TexCoord2f texCoordB, TexCoord2f texCoordC )
     {
         getVertexTexCoordA( texCoordA );
         getVertexTexCoordB( texCoordB );
         getVertexTexCoordC( texCoordC );
     }
     
     
     public void setVertexA( Tuple3f coord, Vector3f normal, Color3f color, TexCoord2f texCoord )
     {
         if ( coord != null )
         {
             addFeature( Vertex3f.COORDINATES );
             setVertexCoordA( coord );
         }
         
         if ( normal != null )
         {
             addFeature( Vertex3f.NORMALS );
             setVertexNormalA( normal );
         }
         
         if ( color != null )
         {
             addFeature( Vertex3f.COLORS_3 );
             setVertexColorA( color );
         }
         
         if ( texCoord != null )
         {
             addFeature( Vertex3f.TEXTURE_COORDINATES_2 );
             setVertexTexCoordA( texCoord );
         }
     }
     
     public void getVertexA( Tuple3f coord, Vector3f normal, Color3f color, TexCoord2f texCoord )
     {
         if ( ( coord != null ) && ( hasFeature( Vertex3f.COORDINATES ) ) )
             getVertexCoordA( coord );
         if ( ( normal != null ) && ( hasFeature( Vertex3f.NORMALS ) ) )
             getVertexNormalA( normal );
         if ( ( color != null ) && ( hasFeature( Vertex3f.COLORS_3 ) ) )
             getVertexColorA( color );
         if ( ( texCoord != null ) && ( hasFeature( Vertex3f.TEXTURE_COORDINATES_2 ) ) )
             getVertexTexCoordA( texCoord );
     }
     
     
     public void setVertexB( Tuple3f coord, Vector3f normal, Color3f color, TexCoord2f texCoord )
     {
         if ( coord != null )
         {
             addFeature( Vertex3f.COORDINATES );
             setVertexCoordB( coord );
         }
         
         if ( normal != null )
         {
             addFeature( Vertex3f.NORMALS );
             setVertexNormalB( normal );
         }
         
         if ( color != null )
         {
             addFeature( Vertex3f.COLORS_3 );
             setVertexColorB( color );
         }
         
         if ( texCoord != null )
         {
             addFeature( Vertex3f.TEXTURE_COORDINATES_2 );
             setVertexTexCoordB( texCoord );
         }
     }
     
     public void getVertexB( Tuple3f coord, Vector3f normal, Color3f color, TexCoord2f texCoord )
     {
         if ( ( coord != null ) && ( hasFeature( Vertex3f.COORDINATES ) ) )
             getVertexCoordB( coord );
         if ( ( normal != null ) && ( hasFeature( Vertex3f.NORMALS ) ) )
             getVertexNormalB( normal );
         if ( ( color != null ) && ( hasFeature( Vertex3f.COLORS_3 ) ) )
             getVertexColorB( color );
         if ( ( texCoord != null ) && ( hasFeature( Vertex3f.TEXTURE_COORDINATES_2 ) ) )
             getVertexTexCoordB( texCoord );
     }
     
     
     public void setVertexC( Tuple3f coord, Vector3f normal, Color3f color, TexCoord2f texCoord )
     {
         if ( coord != null )
         {
             addFeature( Vertex3f.COORDINATES );
             setVertexCoordC( coord );
         }
         
         if ( normal != null )
         {
             addFeature( Vertex3f.NORMALS );
             setVertexNormalC( normal );
         }
         
         if ( color != null )
         {
             addFeature( Vertex3f.COLORS_3 );
             setVertexColorC( color );
         }
         
         if ( texCoord != null )
         {
             addFeature( Vertex3f.TEXTURE_COORDINATES_2 );
             setVertexTexCoordC( texCoord );
         }
     }
     
     public void getVertexC( Tuple3f coord, Vector3f normal, Color3f color, TexCoord2f texCoord )
     {
         if ( ( coord != null ) && ( hasFeature( Vertex3f.COORDINATES ) ) )
             getVertexCoordC( coord );
         if ( ( normal != null ) && ( hasFeature( Vertex3f.NORMALS ) ) )
             getVertexNormalC( normal );
         if ( ( color != null ) && ( hasFeature( Vertex3f.COLORS_3 ) ) )
             getVertexColorC( color );
         if ( ( texCoord != null ) && ( hasFeature( Vertex3f.TEXTURE_COORDINATES_2 ) ) )
             getVertexTexCoordC( texCoord );
     }
     
     
     /**
      * Calculates the face normal from the cross product of edge AC and AB.
      * 
      * @param faceNormal
      */
     public void getFaceNormalACAB( Vector3f faceNormal )
     {
         tmpVec1.sub( getVertexCoordC(), getVertexCoordA() );
         tmpVec2.sub( getVertexCoordB(), getVertexCoordA() );
         
         faceNormal.cross( tmpVec1, tmpVec2 );
     }
     
     /**
      * Calculates the face normal from the cross product of edge AC and AB.
      */
    public Vector3f getFaceNormalACAB()
     {
         getFaceNormalACAB( faceNormal );
         
         return( faceNormal );
     }
     
     
     /**
      * Calculates the face normal from the cross product of edge BA and BC.
      * 
      * @param faceNormal
      */
     public void getFaceNormalBABC( Vector3f faceNormal )
     {
         tmpVec1.sub( getVertexCoordA(), getVertexCoordB() );
         tmpVec2.sub( getVertexCoordC(), getVertexCoordB() );
         
         faceNormal.cross( tmpVec1, tmpVec2 );
     }
     
     /**
      * Calculates the face normal from the cross product of edge BA and BC.
      */
     public Vector3f getFaceNormalBABC()
     {
         getFaceNormalBABC( faceNormal );
         
         return( faceNormal );
     }
     
     
     /**
      * Calculates the face normal from the cross product of edge AB and AC.
      * 
      * @param faceNormal
      */
     public void getFaceNormalCBCA( Vector3f faceNormal )
     {
         tmpVec1.sub( getVertexCoordB(), getVertexCoordC() );
         tmpVec2.sub( getVertexCoordA(), getVertexCoordC() );
         
         faceNormal.cross( tmpVec1, tmpVec2 );
     }
     
     /**
      * Calculates the face normal from the cross product of edge AB and AC.
      */
     public Vector3f getFaceNormalCBCA()
     {
         getFaceNormalCBCA( faceNormal );
         
         return( faceNormal );
     }
     
     /**
      * Calculates the face normal and writes it to the parameter.
      * 
      * @param faceNormal
      */
     public void getFaceNormal( Vector3f faceNormal )
     {
         if ( !hasFeature( Vertex3f.NORMALS ) )
             throw( new NullPointerException( "You need vertex normals to calculate the face normal" ) );
         
         getFaceNormalACAB( tmpVec4 );
         if ( tmpVec4.angle( getVertexNormalA() ) > FastMath.PI_HALF )
             tmpVec4.scale( -1f );
         
         getFaceNormalBABC( tmpVec5 );
         if ( tmpVec5.angle( getVertexNormalB() ) > FastMath.PI_HALF )
             tmpVec5.scale( -1f );
         
         getFaceNormalCBCA( tmpVec6 );
         if ( tmpVec6.angle( getVertexNormalC() ) > FastMath.PI_HALF )
             tmpVec6.scale( -1f );
         
         
         final float angleAB = tmpVec4.angle( tmpVec5 );
         final float angleAC = tmpVec4.angle( tmpVec6 );
         
         if ( ( angleAB > 0.001f ) && ( angleAC > 0.001f ) )
         {
             faceNormal.set( tmpVec5 );
             return;
         }
         
         final float angleBC = tmpVec5.angle( tmpVec6 );
         
         if ( ( angleBC > 0.001f ) && ( angleAB > 0.001f ) )
         {
             faceNormal.set( tmpVec6 );
             return;
         }
         
         if ( ( angleBC > 0.001f ) && ( angleAC > 0.001f ) )
         {
             faceNormal.set( tmpVec4 );
             return;
         }
         
         faceNormal.set( tmpVec4 );
     }
     
     /**
      * Calculates and returns the face normal.
      */
     public Vector3f getFaceNormal()
     {
         if ( faceNormal == null )
             faceNormal = new Vector3f();
         
         getFaceNormal( faceNormal );
         
         return( faceNormal );
     }
     
     
     /**
      * Sets the index of the vertexA (just meta info).
      * 
      * @param index
      */
     public void setVertexIndexA( int index )
     {
         this.vertexIndexA = index;
     }
     
     /**
      * @return the index of the vertexA (just meta info).
      */
     public int getVertexIndexA()
     {
         return( vertexIndexA );
     }
     
     /**
      * Sets the index of the vertexB (just meta info).
      * 
      * @param index
      */
     public void setVertexIndexB( int index )
     {
         this.vertexIndexB = index;
     }
     
     /**
      * @return the index of the vertexB (just meta info).
      */
     public int getVertexIndexB()
     {
         return( vertexIndexB );
     }
     
     /**
      * Sets the index of the vertexC (just meta info).
      * 
      * @param index
      */
     public void setVertexIndexC( int index )
     {
         this.vertexIndexC = index;
     }
     
     /**
      * @return the index of the vertexC (just meta info).
      */
     public int getVertexIndexC()
     {
         return( vertexIndexC );
     }
     
     /**
      * Sets the indices of the vertices A, B, C (just meta info).
      * 
      * @param indexA
      * @param indexB
      * @param indexC
      */
     public void setVertexIndices( int indexA, int indexB, int indexC )
     {
         setVertexIndexA( indexA );
         setVertexIndexB( indexB );
         setVertexIndexC( indexC );
     }
     
     
     public int sign3D( Tuple3f a, Tuple3f b, Tuple3f c, Tuple3f d )
     {
         tmpMat.setRow( 0, a.x - d.x, a.y - d.y, a.z - d.z );
         tmpMat.setRow( 1, b.x - d.x, b.y - d.y, b.z - d.z );
         tmpMat.setRow( 2, c.x - d.x, c.y - d.y, c.z - d.z );
         
         final float det = tmpMat.determinant();
         final float EPSILON = 0.00001f;
         
         if ( det > EPSILON )
             return( 1 );
         else if ( det < -EPSILON )
             return( -1 );
         else
             return( 0 );
     }
     
     /**
      * Does a quick ray-intersection test, that doesn't very precise.
      * It provides a reliable negative-boolean result.
      * 
      * @param pickRay
      * 
      * @return true, if an intersection is possible
      */
     public boolean quickIntersectionTest( Ray3f pickRay )
     {
         tmpPnt.scaleAdd( 100000.0f, pickRay.getDirection(), pickRay.getOrigin() );
         
         final int i = sign3D( tmpPnt, coordA, pickRay.getOrigin(), coordB );
         final int j = sign3D( tmpPnt, coordC, coordB, pickRay.getOrigin() );
         final int k = sign3D( tmpPnt, coordA, coordC, pickRay.getOrigin() );
         
         if ( i == 0 && j == 0 )
             return( true ); // intersects in C
         if ( i == 0 && k == 0 )
             return( true ); // intersects in A
         if ( j == 0 && k == 0 )
             return( true ); // intersects in B
         if ( i == 0 && j == k )
             return( true ); // intersects in AC
         if ( j == 0 && i == k )
             return( true ); // intersects in BC
         if ( k == 0 && j == i )
             return( true ); // intersects in AB
         if ( i == j && j == k )
             return( true ); // intersects inside
         
         return( false ); // does not intersect
     }
     
     /**
      * Tests the triangle for intersection with a ray.
      * 
      * @param rayOrigin
      * @param rayDirection
      * 
      * @return the distance between the ray origin and the intersection point
      */
     public float intersects( Point3f rayOrigin, Vector3f rayDirection )
     {
         coordB.sub( coordA );
         coordC.sub( coordA );
         tmpVec2.set( coordB );
         tmpVec3.set( coordC );
         tmpVec1.cross( tmpVec2, tmpVec3 );
         
         tmpVec2.set( rayOrigin );
         
         if ( tmpVec1.length() == 0 )
         {
             return( -1f );
         }
         
         tmpVec2.sub( coordA );
         final float d = -tmpVec1.dot( tmpVec2 );
         final float e = tmpVec1.dot( rayDirection );
         
         if ( Math.abs( e ) < 0.00001f )
         {
             return( -1f );
         }
         
         final float r = d / e;
         if ( r < 0.0f )
         {
             return( -1f );
         }
         
         return( r * r );
     }
     
     /**
      * Tests the triangle for intersection with a ray.
      * 
      * @param ray
      * 
      * @return the distance between the ray origin and the intersection point
      */
     public float intersects( Ray3f ray )
     {
         return( intersects( ray.getOrigin(), ray.getDirection() ) );
     }
     
     /**
      * Tests the triangle for intersection with a ray.<br>
      * This firs uses quickIntersectionTest() to cheaply test for a possible intersection.
      * 
      * @param ray
      * @param nearestDist the nearest distance to be accepted (for optimizations)
      * 
      * @return the distance between the ray origin and the intersection point
      */
     public float intersects( Ray3f ray, float nearestDist )
     {
         /*
          * first check, if any intersection with the triangle can result in a
          * nearer result.
          * if this is possible, check if there is an intersection with the
          * triangle.
          */
         if ( quickIntersectionTest( ray ) )
         {
             // if there is an intersection return the exact position
             return( intersects( ray ) );
         }
         
         return( -1f );
     }
     
     
     public Triangle( int features )
     {
         super( features );
     }
     
     public Triangle()
     {
         this( Vertex3f.COORDINATES | Vertex3f.NORMALS | Vertex3f.COLORS_3 | Vertex3f.TEXTURE_COORDINATES_2 );
     }
     
     /*
     public static final void main( String[] args )
     {
         Triangle trian = new Triangle( COORDINATES | NORMALS );
         
         trian.setVertexA( new Point3f( 0, 0, 0 ), new Vector3f( 0, 0, +1 ), null, null );
         trian.setVertexB( new Point3f( 1, 0, 0 ), new Vector3f( 0, 0, +1 ), null, null );
         trian.setVertexC( new Point3f( 1, 1, 0 ), new Vector3f( 0, 0, -1 ), null, null );
         
         System.out.println( trian.getFaceNormal() );
     }
     */
 }
