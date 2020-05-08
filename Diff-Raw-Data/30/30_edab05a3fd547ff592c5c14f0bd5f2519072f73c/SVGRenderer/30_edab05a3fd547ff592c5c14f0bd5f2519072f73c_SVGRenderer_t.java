 /*
   Modified by The Atlantic Media Company from original source.
   See <https://github.com/one2team/highcharts-serverside-export> for original.
   
   Licensed under the Apache License, Version 2.0 (the "License"); you may not
   use this work except in compliance with the License. You may obtain a copy
   of the License in the LICENSE file, or at:
   
   http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
   License for the specific language governing permissions and limitations
   under the License.
 */
 package com.theatlantic.highcharts.export;
 
 import com.theatlantic.highcharts.export.Renderer.ChartRenderer;
 
 import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
 import java.io.IOException;
import java.io.OutputStreamWriter;
 
 import org.apache.commons.io.IOUtils;
 
 class SVGRenderer extends ChartRenderer {
     
     public SVGRenderer(SVGRendererInternal internal) {
         this.internal = internal;
     }
 
     @Override
     public void render() {
         if (getChartOptions() == null) {
             throw (new RuntimeException("chartOptions must not be null"));
         }
         
         ByteArrayInputStream byteStream = null;
         try {
             final String svg = internal.getSVG(getChartOptions());
             if (svg == null) {
                 throw (new RuntimeException("cannot generate svg"));
             }
             byteStream = new ByteArrayInputStream(svg.getBytes());
            InputStreamReader reader = new InputStreamReader(byteStream);
            IOUtils.copy(reader, getOutputStream(), "UTF-8");
         } catch (IOException e) {
             throw (new RuntimeException(e));
         } finally {
             IOUtils.closeQuietly(byteStream);
         }
     }
     
     private final SVGRendererInternal internal;
 
 }
