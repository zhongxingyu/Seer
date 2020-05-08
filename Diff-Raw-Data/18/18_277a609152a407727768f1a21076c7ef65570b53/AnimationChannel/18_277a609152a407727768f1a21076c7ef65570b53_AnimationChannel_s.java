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
 
 package org.jagatoo.loaders.models.collada;
 
 import org.jagatoo.loaders.models.collada.datastructs.animation.DaeJoint;
 import org.jagatoo.loaders.models.collada.datastructs.visualscenes.COLLADATransform;
 import org.jagatoo.loaders.models.collada.stax.XMLChannel;
 import org.openmali.FastMath;
 import org.openmali.vecmath2.*;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  */
 public class AnimationChannel
 {
     private final DaeJoint joint;
     private final float[] timeline;
 
     private Object values;
 
     private final XMLChannel.ChannelType type;
 
     private Class<?> elemClass;
     private final int ord;
 
     public DaeJoint getJoint()
     {
         return ( joint );
     }
 
     public float[] getTimeline()
     {
         return ( timeline );
     }
 
     public XMLChannel.ChannelType getType()
     {
         return ( type );
     }
 
     public Object getValues()
     {
         return ( values );
     }
 
     public Class<?> getElemClass()
     {
         return ( elemClass );
     }
 
     public int getOrd()
     {
         return ( ord );
     }
 
     public static AnimationChannel create( DaeJoint joint, float[] timeline, float[] output, int stride, int i, int j, Object targetValue, int ord )
     {
         Object values = initValues( timeline.length, targetValue );
         setValues( values, output, stride, i, j, targetValue.getClass() );
 
         return ( new AnimationChannel( joint, timeline, values, targetValue.getClass(), ord ) );
     }
 
     public final void update( float[] timeline, float[] output, int stride, int i, int j, Object targetValue )
     {
         //if this is "packed" channel expand it
         if ( timeline.length == 2 &&
                 timeline[ 0 ] == this.timeline[ 0 ] &&
                 timeline[ timeline.length - 1 ] == this.timeline[ this.timeline.length - 1 ]
                 )
         {
             float f = output[ 0 ];
             output = new float[this.timeline.length];
             Arrays.fill( output, f );
         }
         else if ( this.timeline.length == 2 &&
                 this.timeline[ 0 ] == timeline[ 0 ] &&
                 this.timeline[ this.timeline.length - 1 ] == timeline[ timeline.length - 1 ]
                 )
         {
             Object o = Array.get( values, 0 );
             values = Array.newInstance( values.getClass().getComponentType(), timeline.length );
             Arrays.fill( ( Object[] ) values, o );
         }
 
         setValues( values, output, stride, i, j, targetValue.getClass() );
     }
 
     private static void setValues( Object values, float[] output, int stride, int i, int j, Class<?> clazz )
     {
         for ( int k = 0; k < Array.getLength( values ); k++ )
         {
             Object value = Array.get( values, k );
             if ( clazz == Tuple3f.class || clazz == Vector3f.class || clazz == Quaternion4f.class )
             {
                 setTupleNf( value, output, stride, i, k );
             }
             else if ( clazz == AxisAngle3f.class )
             {
                 setAxisAngle3f( value, output, stride, i, k );
             }
             else if ( clazz == Matrix4f.class )
             {
                 setMatrix4f( value, output, i, j, k );
             }
             else
             {
                 throw new Error( "Unknown class of animation channel value: " + clazz );
             }
         }
     }
 
     private static void setMatrix4f( Object value, float[] output, int i, int j, int k )
     {
         if ( i < 0 && j < 0 ) //set whole matrix value ( stride == 16 )
         {
             for ( int x = 0; x < 4; x++ )
             {
                 for ( int y = 0; y < 4; y++ )
                 {
                    ( ( Matrix4f ) value ).set( x, y, output[ x * 4 + y ] );
                 }
             }
         }
         else if ( i >= 0 && j >= 0 ) //set ith,jth component of value ( stride == 1 )
         {
             if ( i == 3 && j == 3 )
             {
                 ( ( Matrix4f ) value ).set( i, j, 1.0f );   // max exporter bug             
             }
             else
             {
                 ( ( Matrix4f ) value ).set( i, j, output[ k ] );
             }
         }
         else
         {
             throw new Error( "Wrong animation target indices for matrix: i=" + i + ", j=" + j );
         }
     }
 
     private static void setAxisAngle3f( Object value, float[] output, int stride, int i, int k )
     {
         if ( i < 0 ) //set whole value
         {
             for ( int ii = 0; ii < stride; ii++ )
             {
                 if ( ii < 3 )
                 {
                     ( ( AxisAngle3f ) value ).setValue( ii, output[ k * stride + ii ] );
                 }
                 else
                 {
                     ( ( AxisAngle3f ) value ).setAngle( FastMath.toRad( output[ k * stride + ii ] ) );
                 }
             }
         }
         else //set ith component of value ( stride == 1 )
         {
             if ( i < 3 )
             {
                 ( ( AxisAngle3f ) value ).setValue( i, output[ k ] );
             }
             else
             {
                 ( ( AxisAngle3f ) value ).setAngle( FastMath.toRad( output[ k ] ) );
             }
         }
     }
 
     private static void setTupleNf( Object value, float[] output, int stride, int i, int k )
     {
         if ( i < 0 ) //set whole value
         {
             for ( int ii = 0; ii < stride; ii++ )
             {
                 ( ( TupleNf ) value ).setValue( ii, output[ k * stride + ii ] );
             }
         }
         else //set ith component of value ( stride == 1 )
         {
             ( ( TupleNf ) value ).setValue( i, output[ k ] );
         }
     }
 
     private static Object initValues( int size, Object targetValue )
     {
         Class<?> clazz = targetValue.getClass();
         Object values = Array.newInstance( clazz, size );
         try
         {
             Constructor<?> ctor = clazz.getDeclaredConstructor( clazz == Vector3f.class ? Tuple3f.class : clazz ); //copy constructor
             ctor.setAccessible( true );
             for ( int i = 0; i < Array.getLength( values ); i++ )
             {
                 try
                 {
                     Array.set( values, i, ctor.newInstance( targetValue ) );
                 }
                 catch ( InstantiationException e )
                 {
                     throw new Error( e );
                 }
                 catch ( IllegalAccessException e )
                 {
                     throw new Error( e );
                 }
                 catch ( InvocationTargetException e )
                 {
                     throw new Error( e );
                 }
             }
         }
         catch ( NoSuchMethodException e )
         {
             throw new Error( e );
         }
 
         return ( values );
     }
 
     public final boolean isEmpty()
     {
         return ( timeline.length == 0 );
     }
 
     public static AnimationChannel createEmpty( DaeJoint joint, Class<?> elemClass )
     {
         return ( new AnimationChannel( joint, new float[0], Array.newInstance( elemClass, 0 ), elemClass, -1 ) );
     }
 
     public static AnimationChannel merge( DaeJoint joint, List<AnimationChannel> l, Class<?> clazz )
     {
         AnimationChannel ac;
         l = removeEmptyChannels( l );
         if ( l.isEmpty() )
         {
             ac = createEmpty( joint, clazz );
         }
         else
         {
             ac = l.get( 0 );
             float[] tl = ac.getTimeline();
             Object v = ac.getValues();
             v = adjustValues( ac, v );
             for ( int i = 0; i < l.size(); i++ )
             {
                 if ( i + 1 == l.size() )
                 {
                     break;
                 }
                 AnimationChannel ac2 = l.get( i + 1 );
                 if ( !Arrays.equals( tl, ac2.getTimeline() ) )
                 {
                     throw new Error( "Can't merge animation channels for joint " + joint + "." );
                 }
                 Object v2 = ac2.getValues();
                 v2 = adjustValues( ac2, v2 );
                 for ( int j = 0; j < Array.getLength( v ); j++ )
                 {
                     merge( v, v2, j, clazz );
                 }
             }
             ac.elemClass = clazz;
             ac.values = v;
         }
 
         return ( ac );
     }
 
     // modifies elements of v
     private static void merge( Object v, Object v2, int j, Class<?> clazz )
     {
         if ( clazz == Vector3f.class )
         {
             Vector3f o = ( Vector3f ) Array.get( v, j );
             o.add( ( Vector3f ) Array.get( v2, j ) );
         }
         else if ( clazz == Quaternion4f.class )
         {
             Quaternion4f o = ( Quaternion4f ) Array.get( v, j );
             o.mul( ( Quaternion4f ) Array.get( v2, j ) );
             o.normalize();
         }
         else if ( clazz == Tuple3f.class )
         {
             Tuple3f o = ( Tuple3f ) Array.get( v, j );
             Tuple3f o2 = ( Tuple3f ) Array.get( v2, j );
             o.mul( o2.getX(), o2.getY(), o2.getZ() );
         }
         else
         {
             throw new Error( "Unknown class:" + clazz );
         }
     }
 
     private static List<AnimationChannel> removeEmptyChannels( List<AnimationChannel> l )
     {
         for ( Iterator<AnimationChannel> it = l.iterator(); it.hasNext(); )
         {
             AnimationChannel ac = it.next();
             if ( ac.isEmpty() )
             {
                 it.remove();
             }
         }
 
         return ( l );
     }
 
     private static Object adjustValues( AnimationChannel ac, Object v )
     {
         if ( ac.getElemClass() == AxisAngle3f.class )
         {
             Object v1 = Array.newInstance( Quaternion4f.class, Array.getLength( v ) );
             for ( int i = 0; i < Array.getLength( v ); i++ )
             {
                 AxisAngle3f o = ( AxisAngle3f ) Array.get( v, i );
                 Quaternion4f q = new Quaternion4f();
                 q.set( o );
                 Array.set( v1, i, q );
             }
             return ( v1 );
         }
 
         return ( v );
     }
 
     private AnimationChannel( DaeJoint joint, float[] timeline, Object values, Class<?> elemClass, int ord )
     {
         this.joint = joint;
         this.timeline = timeline;
         this.values = values;
         this.elemClass = elemClass;
         if ( ( type = COLLADATransform.getTransformType( elemClass ) ) == null )
         {
             throw new Error( "Unknown class:" + elemClass );
         }
         this.ord = ord;
     }
 }
