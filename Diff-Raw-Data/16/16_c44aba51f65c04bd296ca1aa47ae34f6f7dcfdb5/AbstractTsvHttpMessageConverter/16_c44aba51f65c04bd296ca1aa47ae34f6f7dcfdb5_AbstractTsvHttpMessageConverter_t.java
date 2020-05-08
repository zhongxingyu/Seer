 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.dfci.cccb.mev.dataset.rest.assembly.tsv.prototype;
 
 import java.nio.charset.Charset;
 
 import org.springframework.http.MediaType;
 import org.springframework.http.converter.AbstractHttpMessageConverter;
 
 /**
  * @author levk
  * 
  */
 public abstract class AbstractTsvHttpMessageConverter <T> extends AbstractHttpMessageConverter<T> {
 
   private static final Charset DEFAULT_CHARSET = Charset.forName ("UTF-8");
   public static final String TSV_EXTENSION = "tsv";
  private static final String TSV_TYPE = "text";
   public static final MediaType TSV_MEDIA_TYPE = new MediaType (TSV_TYPE,
                                                                "tab-separated-values",
                                                                 DEFAULT_CHARSET);
 
   /**
    * 
    */
   public AbstractTsvHttpMessageConverter () {
     super (TSV_MEDIA_TYPE);
   }
 }
