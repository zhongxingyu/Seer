 //
 // SLIMCurveFitter.java
 //
 
 /*
 Curve Fitter library for fitting exponential decay curves.
 
 Copyright (c) 2010, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.curvefitter;
 
 import com.sun.jna.ptr.DoubleByReference;
 import com.sun.jna.ptr.FloatByReference;
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 import com.sun.jna.Platform;
 
 import imagej.nativelibrary.NativeLibraryUtil;
 
 /**
  * TODO
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/GrayNRCurveFitter.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/GrayNRCurveFitter.java">SVN</a></dd></dl>
  *
  * @author Aivar Grislis grislis at wisc.edu
  */
 public class SLIMCurveFitter extends AbstractCurveFitter {
     static CLibrary s_library;
     public enum AlgorithmType { RLD, LMA };
     private AlgorithmType m_algorithmType;
 
     public interface CLibrary extends Library {
 
         //TODO I'm omitting noise, s/b Poisson or Gaussian with lots of photons???
         //TODO I'm omitting residuals, see below also, same thing...
 
         public int RLD_fit(double xInc,
                            double y[],
                            int fitStart,
                            int fitEnd,
                            double instr[],
                            int nInstr,
                            double sig[],
                            DoubleByReference z,
                            DoubleByReference a,
                            DoubleByReference tau,
                            double fitted[],
                            DoubleByReference chiSquare,
                            double chiSquareTarget
                            );
 
         //TODO I'm omitted noise, see above and restrainType and fitType, for now
         //TODO also covar, alpha, errAxes and chiSqPercent
         //TODO I'm omitting residuals[] aren't residuals = y 0 yFitted??? is there some weighting I'm missing that is time-consuming/impossible to recreate?
 
         public int LMA_fit(double xInc,
                            double y[],
                            int fitStart,
                            int fitEnd,
                            double instr[],
                            int n_instr,
                            double sig[],
                            double param[],
                            int paramFree[],
                            int nParam,
                            double fitted[],
                            DoubleByReference chiSquare,
                            double chiSquareTarget
                            );
 
 
         /* public int nr_GCI_triple_integral_fitting_engine(float xincr, float y[], int fitStart, int fitEnd,
 									   float instr[], int nInstr, int noise, float sig[],
                                                                            FloatByReference z, FloatByReference a, FloatByReference tau,
                                                                            float fitted[], float residuals[],
                                                                            FloatByReference chiSq, float chiSqTarget);
 
            public int nr_GCI_marquardt_fitting_engine(float xincr, float y[], int nData, int fitStart, int fitEnd,
                                                                            float instr[], int nInstr, int noise, float sig[],
                                                                            float param[], int paramFree[], int nParam,
                                                                            int restrainType, int fitType,
                                                                            float fitted[], float residuals[],
                                                                            FloatByReference chiSq);*/
         //,
         //                                                                   float covar[], float alpha[], float errAxes[],
         //                                                                   float chiSqTarget, int chiSqPercent);
 
 
         /*public int nr_GCI_marquardt_fitting_engine(float xincr, float *trans, int ndata, int fit_start, int fit_end,
 								 float prompt[], int nprompt, //TODO ARG is this actually instr[] & ninstr?
 								 noise_type noise, float sig[],
 								 float param[], int paramfree[],
 								 int nparam, restrain_type restrain,
 								 fit_type fit, //TODO ARG void (*fitfunc)(float, float [], float *, float [], int),
 								 float *fitted, float *residuals, float *chisq,
 								 float **covar, float **alpha, float **erraxes,
 									float chisq_target, int chisq_percent) {*/
     }
 
     public SLIMCurveFitter(AlgorithmType algorithmType) {
         m_algorithmType = algorithmType;
     }
 
     public SLIMCurveFitter() {
         m_algorithmType = AlgorithmType.RLD;
     }
 
 
     /**
      * @inheritDoc
      */
     public int fitData(ICurveFitData[] dataArray, int start, int stop) {
         int returnValue = 0;
         if (null == s_library) {
             try {
                 // extract to library path
                //TODO sort out the nameSystem.out.println("extract native library returns " + NativeLibraryUtil.extractNativeLibraryToPath(this.getClass(), "SLIMCurve-2.0-SNAPSHOT"));
                System.out.println("extract native library returns " + NativeLibraryUtil.extractNativeLibraryToPath(this.getClass(), "slim-curve-1.0-SNAPSHOT"));

                 // load once, on-demand
                //TODO sort out the name s_library = (CLibrary) Native.loadLibrary("SLIMCurve", CLibrary.class);
               //TODO test with old code instead: s_library = (CLibrary) Native.loadLibrary("slim-curve-1.0-SNAPSHOT", CLibrary.class);
                s_library = (CLibrary) Native.loadLibrary("SLIMCurve_trimmed_down", CLibrary.class);

                 System.out.println("s_library is " + s_library);
             }
             catch (UnsatisfiedLinkError e) {
                 System.out.println("unable to load dynamic library " + e.getMessage());
                 return 0;
             }
         }
 
         //TODO ARG 9/3/10 these issues still need to be addressed:
 
         //TODO ARG since initial x = fit_start * xincr we have to supply the unused portion of y[] before fit_start.
         // if this data were already premassaged it might be better to get rid of fit_start & _end, just give the
         // portion to be fitted and specify an initial x.
         //TODO ARG August use initial X of 0.
 
         DoubleByReference chiSquare = new DoubleByReference();
         double chiSquareTarget = 1.0; //TODO s/b specified incoming
 
         if (AlgorithmType.RLD.equals(m_algorithmType)) {
             // RLD or triple integral fit
             DoubleByReference z = new DoubleByReference();
             DoubleByReference a = new DoubleByReference();
             DoubleByReference tau = new DoubleByReference();
 
             for (ICurveFitData data: dataArray) {
                 // grab incoming parameters
                 a.setValue(  data.getParams()[2]);
                 tau.setValue(data.getParams()[3]);
                 z.setValue(  data.getParams()[1]);
 
                 int nInstrumentResponse = 0;
                 if (null != m_instrumentResponse) {
                     nInstrumentResponse = m_instrumentResponse.length;
                 }
 
                 returnValue = s_library.RLD_fit(
                         m_xInc,
                         data.getYCount(), //TODO data get data???
                         start,
                         stop,
                         m_instrumentResponse,
                         nInstrumentResponse,
                         data.getSig(),
                         z,
                         a,
                         tau,
                         data.getYFitted(),
                         chiSquare,
                         chiSquareTarget
                         );
                // set outgoing parameters
                 data.getParams()[0] = chiSquare.getValue();
                 data.getParams()[1] = z.getValue();
                 data.getParams()[2] = a.getValue();
                 data.getParams()[3] = tau.getValue();
             }
         }
         else {
             // LMA fit
             for (ICurveFitData data: dataArray) {
                 int nInstrumentResponse = 0;
                 if (null != m_instrumentResponse) {
                     nInstrumentResponse = m_instrumentResponse.length;
                 }
                 returnValue = s_library.LMA_fit(
                         m_xInc,
                         data.getYCount(),
                         start,
                         stop,
                         m_instrumentResponse,
                         nInstrumentResponse,
                         data.getSig(),
                         data.getParams(),
                         toIntArray(m_free),
                         data.getParams().length - 1,
                         data.getYFitted(),
                         chiSquare,
                         chiSquareTarget
                         );
             }
         }
         //TODO error return deserves more thought
         return returnValue;
     }
 
     int[] toIntArray(boolean[] booleanArray) {
         int intArray[] = new int[booleanArray.length];
         for (int i = 0; i < booleanArray.length; ++i) {
             intArray[i] = (booleanArray[i] ? 1 : 0);
         }
         return intArray;
     }
 
     double[] floatToDouble(float[] f) {
         double d[] = new double[f.length];
         for (int i = 0; i < f.length; ++i) {
             d[i] = f[i];
         }
         return d;
     }
 
     float[] doubleToFloat(double[] d) {
         float f[] = new float[d.length];
         for (int i = 0; i < d.length; ++i) {
             f[i] = (float) d[i];
         }
         return f;
     }
 
 }
