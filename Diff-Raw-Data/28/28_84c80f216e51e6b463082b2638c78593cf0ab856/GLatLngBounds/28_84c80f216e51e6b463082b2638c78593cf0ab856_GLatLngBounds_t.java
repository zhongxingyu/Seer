 /*
  *
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.gmap3.api;
 
 import wicket.contrib.gmap3.js.Constructor;
 
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * Represents an Google Maps API's <a href= "http://www.google.com/apis/maps/documentation/reference.html#GLatLngBounds"
  * >GLatLngBounds</a>.
  */
 public class GLatLngBounds implements GValue {
 
     /**
      * Default serialVersionUID.
      */
     private static final long serialVersionUID = 1L;
 
     private GLatLng _sw;
     private GLatLng _ne;
 
     /**
      * Use subsequent calls to #extend to define the boundaries.
      */
     public GLatLngBounds() {
 
     }
 
     /**
      * Construct.
      *
      * @param sw
      * @param ne
      */
     public GLatLngBounds(final GLatLng sw, final GLatLng ne) {
         _sw = sw;
         _ne = ne;
     }
 
    public GLatLngBounds(final List<? extends GLatLng> list) {
         for (final GLatLng point : list) {
             extend(point);
         }
     }
 
     public GLatLngBounds extend(final GLatLng latLng) {
         if (_sw == null) {
             _sw = latLng;
             _ne = latLng;
         } else {
             final double s = Math.min(getS(), latLng.getLat());
             final double w = Math.min(getW(), latLng.getLng());
             final double n = Math.max(getN(), latLng.getLat());
             final double e = Math.max(getE(), latLng.getLng());
             _sw = new GLatLng(s, w);
             _ne = new GLatLng(n, e);
         }
         return this;
     }
 
     public GLatLng getSW() {
         return _sw;
     }
 
     public GLatLng getNE() {
         return _ne;
     }
 
     public GLatLng getCenter() {
         return new GLatLng((getS() + getN()) / 2, (getW() + getE()) / 2);
     }
 
     public double getS() {
         return _sw.getLat();
     }
 
     public double getN() {
         return _ne.getLat();
     }
 
     public double getW() {
         return _sw.getLng();
     }
 
     public double getE() {
         return _ne.getLng();
     }
 
     @Override
     public String toString() {
         return getJSconstructor();
     }
 
     /**
      * @see wicket.contrib.gmap.api.GValue#getJSconstructor()
      */
     @Override
     public String getJSconstructor() {
         return new Constructor("google.maps.LatLngBounds").add(_sw.getJSconstructor()).add(_ne.getJSconstructor())
                 .toJS();
     }
 
     @Override
     public int hashCode() {
         return _sw.hashCode() ^ _ne.hashCode();
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (obj instanceof GLatLngBounds) {
             final GLatLngBounds t = (GLatLngBounds)obj;
             return t._sw.equals(_sw) && t._ne.equals(_ne);
         }
         return false;
     }
 
     /**
      * ((37.34068368469045, -122.48519897460936), (37.72184917678752, -121.79855346679686))
      */
     public static GLatLngBounds parse(final String value) {
         StringTokenizer tokenizer;
         try {
             tokenizer = new StringTokenizer(value, "(, )");
         } catch (final NullPointerException e) {
             return null;
         }
         if (tokenizer.countTokens() != 4) {
             return null;
         }
 
         final GLatLng sw = new GLatLng(Float.valueOf(tokenizer.nextToken()), Float.valueOf(tokenizer.nextToken()));
         final GLatLng ne = new GLatLng(Float.valueOf(tokenizer.nextToken()), Float.valueOf(tokenizer.nextToken()));
         return new GLatLngBounds(sw, ne);
     }
 
     /**
      * Workaround for http://code.google.com/p/gmaps-api-issues/issues/detail?id=3117.
      * @param _getBounds
      * @return
      */
     public static GLatLngBounds mapToFitBounds(final GLatLngBounds bounds) {
         GLatLng sw = bounds.getSW();
         GLatLng ne = bounds.getNE();
 
         double s = sw.getLat();
         double w = sw.getLng();
         double n = ne.getLat();
         double e = ne.getLng();
 
         final double dlng = (w - e) / 2.;
         final double dlat = (s - n) / 2.;
         final double clng = (w + e) / 2.;
         final double clat = (s + n) / 2.;
 
         // work around a bug in google maps...///
         w = clng + dlng / 1.5;
         e = clng - dlng / 1.5;
         s = clat + dlat / 1.5;
         n = clat - dlat / 1.5;
 
         sw = new GLatLng(s,w);
         ne = new GLatLng(n,e);
         final GLatLngBounds correctedBounds = new GLatLngBounds(sw,ne);
         return correctedBounds;
     }
 
 }
