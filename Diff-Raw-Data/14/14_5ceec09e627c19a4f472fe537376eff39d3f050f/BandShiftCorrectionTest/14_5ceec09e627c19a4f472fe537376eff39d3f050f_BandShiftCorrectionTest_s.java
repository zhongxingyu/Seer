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
 
 
 import org.junit.Test;
 
 public class BandShiftCorrectionTest {
 
     @Test
     public void testIntegration() throws Exception {
         CorrectionContext correctionContext = new CorrectionContext(Sensor.MODISA);
         BandShiftCorrection bandShiftCorrection = new BandShiftCorrection(correctionContext);
         double[] rrs_wavelengths = {412,443,488,531,547,667};
         double qaa_min = 0.0;
         double qaa_max = 5.0;
         double[] rrs = new double[]{0.00709421,0.00560526,0.00464842,0.00256442,0.00196990,0.000251790};
         double[] qaa = new double[]{0.0189454,0.00553217,0.0133541};
        double[] correctionFacots = bandShiftCorrection.correct(rrs, rrs_wavelengths, qaa, qaa_min, qaa_max);
 
         rrs = new double[]{0.00711314, 0.00559714, 0.00459386, 0.00249029, 0.00189400, 0.000241144};
         qaa = new double[]{0.0192148, 0.00571175, 0.0138207};
        correctionFacots = bandShiftCorrection.correct(rrs, rrs_wavelengths, qaa, qaa_min, qaa_max);
 
     }
 }
