 /*
  * Copyright (C) 2013 Brockmann Consult GmbH (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option)
  * any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see http://www.gnu.org/licenses/
  */
 
 package org.esa.beam.occci.bandshift;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Procedure and related utility to predict IOPs at given wavelengths starting
  * from known values at other wavelengths, by applying a spectral model.
  * The IOPs taken into account are pythoplankton absorption aph,
  * detritus+gelbstoff absorption adg and particle backscattering bbp.
  *
  * @author Frédéric Mélin (European Commission/JRC/IES/WRES)
  * @author Gert Sclep (European Commission/JRC/IES/WRES)
  * @author MarcoZ
  */
 public class IopSpectralModel {
 
     private static final Map<String, Double> BRICAUD_A;
     private static final Map<String, Double> BRICAUD_B;
 
     static {
         BRICAUD_A = new HashMap<String, Double>();
         BRICAUD_A.put("412", 0.0323);
         BRICAUD_A.put("443", 0.0394);
         BRICAUD_A.put("488", 0.0279);
         BRICAUD_A.put("510", 0.0180);
         BRICAUD_A.put("531", 0.0115);
         BRICAUD_A.put("547", 0.00845);
         BRICAUD_A.put("667", 0.01685);
         BRICAUD_A.put("665", 0.0152);
         BRICAUD_A.put("413", 0.032775);
         BRICAUD_A.put("490", 0.0274);
         BRICAUD_A.put("560", 0.0062);
         BRICAUD_A.put("555", 0.007);
         BRICAUD_A.put("670", 0.0189);
 
         BRICAUD_B = new HashMap<String, Double>();
         BRICAUD_B.put("412", 0.286);
         BRICAUD_B.put("443", 0.3435);
         BRICAUD_B.put("488", 0.369);
         BRICAUD_B.put("510", 0.260);
         BRICAUD_B.put("531", 0.134);
         BRICAUD_B.put("547", 0.0625);
         BRICAUD_B.put("667", 0.140);
         BRICAUD_B.put("665", 0.134);
         BRICAUD_B.put("413", 0.28775);
         BRICAUD_B.put("490", 0.361);
         BRICAUD_B.put("560", 0.016);
         BRICAUD_B.put("555", 0.0315);
         BRICAUD_B.put("670", 0.149);
     }
 
     /**
      * Retrieves the A and B Bricaud coefficient for given wavelengths.
      */
     public static double[] getABBricaud(double wl) {
         // a and b at 443 derived from values previously in a_bb_prediction
         // a_443 = 0.0394 and k (= 1/(1-b))= 1.52323
         String wavelengthAsString = Integer.toString((int)wl);
         double aBricaud = BRICAUD_A.get(wavelengthAsString);
         double bBricaud = BRICAUD_B.get(wavelengthAsString);
         return new double[]{aBricaud, bBricaud};
     }
 
     /**
      * Using a spectral model, IOPs at a given wavelength are converted to a set of desired wavelengths.
      * This is done for phytoplankton absorption, for detritus-gelbstoff absorption and for particle backscattering.
      * The phytoplankton absorption is converted using the Bricaud formula (see Bricaud et al. Variability in the chlorophyll-specific absorption coefficients
      * of natural phytoplankton: Analysis and parameterization. Journal of Geophysical Research. 1995;100(95):321–332), and this requires the Bricaud
      * coefficients A and B to be given for both the input as for the output wavelengths. The detritus-gelbstoff and particle backscattering
      * are spectrally evolved using the same spectral slope as used in QAAv5 (See Z. Lee, B. Lubac, J. Werdell, R. Arnone: An update of the quasi-analytical algorithm
      * (QAA_v5): International Ocean Color Group software report). To calculate these slopes, the below water remote sensing reflectances in the blue and green band
      * need to be given as input.
      * <p/>
      * @param
      *    lambda_in: in, required, type="float/fltarr(m)"
      *      start wavelength(s) of the spectral model, in general the start wavelength used is the same for each of the individual conversions
      *
      *    a_in: in, required, type="float/fltarr(m)"
      *      Bricaud A coefficient for the wavelengths given in `lambda_in`
      *
      *    b_in: in, required, type="float/fltarr(m)"
      *      Bricaud B coefficient for the wavelengths given in `lambda_in`
      *
      *    aph_in: in, required, type="float/fltarr(m)/fltarr(m,n)"
      *     phytoplankton absorption for the wavelengths given in `lambda_in` (column dimension m), for 1 or more records (if >1: row dimension n)
      *
      *    adg_in: in, required, type="float/fltarr(m)/fltarr(m,n)"
      *     CDOM+detritus absorption for the wavelengths given in `lambda_in` (column dimension m), for 1 or more records (if >1: row dimension n)
      *
      *    bbp_in: in, required, type="float/fltarr(m)/fltarr(m,n)"
      *      particulate back-scattering for the wavelengths given in `lambda_in` (column dimension m), for 1 or more records (if >1: row dimension n)
      *
      *    rrs_blue_in: in, required, type="float/fltarr(n)"
      *      below water remote sensing reflection in the blue band (443 nm), for 1 or more records (if >1: column dimension n)
      *
      *    rrs_green_in: in, required, type="float/fltarr(n)"
      *      below water remote sensing reflection in the green band (547 or 555 or 560 nm, depending on sensor used), for 1 or more records (if >1: column dimension n)
      *
      *    lambda_out: in, required, type="float/fltarr(m)"
      *     end wavelengths to which `aph_in`, `adg_in` and `bbp_in` should be evolved towards
      *
      *     the dimension m is equal to the dimension m of `lambda_in`
      *
      *    a_out: in, required, type="float/fltarr(m)"
      *     Bricaud A coefficient for the wavelengths given in `lambda_out`
      *
      *    b_out: in, required, type="float/fltarr(m)"
      *     Bricaud B coefficient for the wavelengths given in `lambda_out`
      *
      * @return array of
      *    aph_out: out, type="float/fltarr(m)/fltarr(m,n)"
      *     phytoplankton absorption for the wavelengths given in `lambda_out` (column dimension m), for 1 or more records (if >1: row dimension n), obtained using Bricaud formula starting from values in `aph_in`
      *
      *    adg_out: out, type="float/fltarr(m)/fltarr(m,n)"
      *     CDOM+detritus absorption for the wavelengths given in `lambda_out` (column dimension m), for 1 or more records (if >1: row dimension n), obtained applying QAAv5 spectral slopes starting from values in `adg_in`.
      *
      *    bbp_out: out, type="float/fltarr(m)/fltarr(m,n)"
      *      particulate back-scattering for the wavelengths given in `lambda_out` (column dimension m), for 1 or more records (if >1: row dimension n), obtained applying QAAv5 spectral slopes starting from value in `bbp_in`
      */
     public static double[] iopSpectralModel(double lambda_in, double a_in, double b_in, double aph_in, double adg_in, double bbp_in, double rrs_blue_in, double rrs_green_in,
                                             double lambda_out, double a_out, double b_out) {
 
         double k = 1.52323; //  = 1/(1-b_443)
         double a_443 = 0.0394;
 
         double S = 0.015;
         double rat = rrs_blue_in / rrs_green_in;
         double sdg = S + 0.002 / (0.6 + rat);
 
         double yy = 2.0 * (1.0 - 1.2 * Math.exp(-0.9 * rat));
 
         // Chla concentration; Bricaud et al. (1995)
         double Chla = Math.pow((aph_in / a_in) ,(1 / (1 - b_in)));
 
         //IOPs for input wavelengths
         double ll = lambda_out - lambda_in;
         double aph_out = a_out * Math.pow(Chla, (1 - b_out));
         double adg_out = adg_in * Math.exp(-sdg * ll);
         double bbp_out = bbp_in * Math.pow((lambda_in / lambda_out), yy);
 
         return new double[] {aph_out, adg_out, bbp_out};
     }
 }
