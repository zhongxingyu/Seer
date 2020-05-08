 /**
    Copyright GeekyTheory 2013 (@GeekyTheory)
 
    Licensed under the GPL General Public License, Version 3.0 (the "License"),  
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.gnu.org/licenses/gpl.html
 
    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
    Website: http://geekytheory.com
    Author: Miguel Catalan Bauls <miguel@geekytheory.com>
 */
 
 package com.geekytheory.miguelcatalandev.developerdays.utils;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.widget.TextView;
 
 public class RobotoTextView extends TextView{
 	
 	/*
 	 * http://code.google.com/p/custom-textview/source/browse/custom-textview/ProjectFont/src/com/androcode/projectfont/CustomTextView.java
 	 */
 
 	public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		 try {
              init(attrs);
 		 } catch (Exception e) {
              // Saltara la excepcion si no hemos copiado bien los archivos o
              // hemos indicado fuentes que no existen, en ese caso no hacemos nada
              // y dejamos que actue como un TextView normal
              Log.e("RobotoTextView", "Algunos de los parametros de configuracion no se introdujeron correctamente");
 		 }
 	}
 	
 	public RobotoTextView(Context context, AttributeSet attrs) {
         super(context, attrs);
         
         try {
                 init(attrs);
         } catch (Exception e) {
                 // Saltara la excepcion si no hemos copiado bien los archivos o
                 // hemos indicado fuentes que no existen, en ese caso no hacemos nada
                 // y dejamos que actue como un TextView normal
                 Log.e("RobotoTextView", "Algunos de los parametros de configuracion no se introdujeron correctamente");
         }
         
 	}
 
 	
 	private void init(AttributeSet attrs) throws Exception {
         // Con esto controlamos que podremos seguir usando las fuentes del sistema si queremos
         // Obtenemos la fuente elegida que tiene que estar almacena en el directorio assets
        String fuente = attrs.getAttributeValue("http://schemas.android.com/apk/res/ccom.geekytheory.miguelcatalandev.developerdays", "fuente");
         // Y comprobamos el estilo (Bold = 0x1, Italic = ???)
         String style = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textStyle");
                 
         Typeface font;
                 
         // Si hemos indicado fuente procedemos a configurarla
         if (fuente != null) {
                 // Comprobamos el estilo
                 if (style != null && style.equals("0x1")) { // Bold
                         font = Typeface.createFromAsset(getContext().getAssets(), fuente + "_light.ttf");
             } else if (style != null && style.equals("0x2")) { // Italic
                 font = Typeface.createFromAsset(getContext().getAssets(), fuente + "_light.ttf");
             } else {
                 font = Typeface.createFromAsset(getContext().getAssets(), fuente + "_light.ttf");
             }
                         
                 if (font != null) {
                         setTypeface(font);
                 }
                         
         }
                 
 }
 
 }
