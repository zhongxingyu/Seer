 /*
  * Copyright (C) 2012 Neo Visionaries Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.neovisionaries.animation;
 
 
 /**
  * Base implementation of {@link Interpolator}.
  *
  * @author Takahiko Kawasaki
  */
 public abstract class InterpolatorBase implements Interpolator
 {
     /**
      * Calculate an interpolated value.
      *
      * <p>
      * The implementation of {@link #interpolate} of {@link InterpolatorBase}
      * checks given arguments and then does the following.
      * </p>
      *
      * <ol>
      * <li>If 'timeRatio' is 0 or if 'from' and 'to' are identical, copy
      *     the content of 'from' to 'output' and return.</li>
      * <li>Otherwise, if 'timeRatio' is 1, copy the content of 'to' to
      *     'output' and return.</li>
      * <li>Otherwise, call {@link #doInterpolate(float[], float[], int,
      *      float, float[]) doInterpolate()}.
      * </ol>
      *
      * @throws IllegalArgumentException
      * <ul>
      * <li>'timeRatio' is less than 0.</li>
      * <li>'timeRation' is greater than 1.</li>
      * <li>'componentCount' is less than 1.</li>
      * <li>'output' is null.</li>
      * <li>The length of 'output' is less than 'componentCount'.</li>
     * <li>'from' is null (this check is not done if 'timeRatio' is 0).</li>
      * <li>The length of 'from' is less than 'componentCount' 
     *     (this check is not done if 'timeRatio' is 0).</li>
     * <li>'to' is null (this check is not done if 'timeRatio' is 1).</li>
     * <li>The length of 'to' is less than 'componentCount'
      *     (this check is not done if 'timeRatio' is 1).</li>
      * </ul>
      */
     @Override
     public final void interpolate(float[] from, float[] to, int componentCount, float timeRatio, float[] output)
     {
         if (timeRatio < 0 || 1 < timeRatio)
         {
             throw new IllegalArgumentException("ratio < 0 || 1 < ratio");
         }
 
         if (componentCount < 1)
         {
             throw new IllegalArgumentException("componentCount < 1");
         }
 
         if (output == null)
         {
             throw new IllegalArgumentException("output == null");
         }
 
         if (output.length < componentCount)
         {
             throw new IllegalArgumentException("output.length < size");
         }
 
        if (timeRatio != 0)
         {
             if (from == null)
             {
                 throw new IllegalArgumentException("from == null");
             }
             else if (from.length < componentCount)
             {
                 throw new IllegalArgumentException("from.length < componentCount");
             }
         }
 
        if (timeRatio != 1)
         {
             if (to == null)
             {
                 throw new IllegalArgumentException("to == null");
             }
             else if (to.length < componentCount)
             {
                 throw new IllegalArgumentException("to.length < componentCount");
             }
         }
 
         if (timeRatio == 0 || from == to)
         {
             System.arraycopy(from, 0, output, 0, componentCount);
         }
         else if (timeRatio == 1)
         {
             System.arraycopy(to, 0, output, 0, componentCount);
         }
         else
         {
             doInterpolate(from, to, componentCount, timeRatio, output);
         }
     }
 
 
     protected abstract void doInterpolate(float[] from, float[] to, int componentCount, float timeRatio, float[] output);
 }
