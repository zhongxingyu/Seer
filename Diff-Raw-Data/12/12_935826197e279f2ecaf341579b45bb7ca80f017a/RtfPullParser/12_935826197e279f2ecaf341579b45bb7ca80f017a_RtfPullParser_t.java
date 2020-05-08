 /********************************************************************************
  *   This file is part of NRtfTree Library.
  *
  *   JRtfTree Library is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published by
  *   the Free Software Foundation; either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   JRtfTree Library is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with this program. If not, see <http://www.gnu.org/licenses/>.
  ********************************************************************************/
 
 /********************************************************************************
  * Library:		JRtfTree
  * Version:     v0.3
  * Date:		15/11/2012
  * Copyright:   2006-2012 Salvador Gomez
  * Home Page:	http://www.sgoliver.net
  * GitHub:		https://github.com/sgolivernet/jrtftree
  * Class:		RtfPullParser
  * Description:	Pull parser para documentos RTF.
  * ******************************************************************************/
 
 package net.sgoliver.jrtftree.core;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 
 /**
  * Pull parser para documentos RTF.
  */
public class RtfPullParser //In Sync
 {
     //Constantes
 
     public static final int START_DOCUMENT = 0;
     public static final int END_DOCUMENT = 1;
     public static final int KEYWORD = 2;
     public static final int CONTROL = 3;
     public static final int START_GROUP = 4;
     public static final int END_GROUP = 5;
     public static final int TEXT = 6;
 
     //Atributos
 
     private Reader rtf;			//Fichero/Cadena de entrada RTF
     private RtfLex lex;		    //Analizador lxico para RTF
     private RtfToken tok;		//Token actual
     private int currentEvent;   //Evento actual
 
     //Construtores
 
     /**
      * Constructor de la clase.
      */
     public RtfPullParser(String path)
     {
         currentEvent = START_DOCUMENT;
     }
     
     /**
      * Carga un fichero en formato RTF
      * @param path Ruta del fichero a parsear.
      * @throws FileNotFoundException
      * @throws IOException
      */
     public int loadRtfFile(String path) throws FileNotFoundException, IOException
     {
     	int res = 0;
     	
         //Se abre el fichero de entrada
         rtf = new FileReader(path);
 
         //Se crea el analizador lxico para RTF
         lex = new RtfLex(rtf);
 
         return res;
     }
     
     /**
      * Carga una cadena de Texto con formato RTF.
      * @param text Cadena de Texto que contiene el documento.
      * @throws IOException
      */
     public int loadRtfText(String text) throws IOException
     {
     	int res = 0;
     	
         //Se abre el fichero de entrada
         rtf = new StringReader(text);
 
         //Se crea el analizador lxico para RTF
         lex = new RtfLex(rtf);
 
         return res;
     }
 
     //Mtodos Pblicos
 
     /**
      * Obtiene el tipo de evento actual.
      * @return Tipo de evento actual.
      */
     public int getEventType()
     {
         return currentEvent;
     }
 
     /**
      * Obtiene el siguiente elemento del documento.
      * @return Siguiente elemento del documento.
      * @throws IOException
      */
     public int next() throws IOException
     {
         tok = lex.nextToken();
 
         switch (tok.getTokenType())
         {
             case RtfTokenType.GROUP_START:
                 currentEvent = START_GROUP;
                 break;
             case RtfTokenType.GROUP_END:
                 currentEvent = END_GROUP;
                 break;
             case RtfTokenType.KEYWORD:
                 currentEvent = KEYWORD;
                 break;
             case RtfTokenType.CONTROL:
                 currentEvent = CONTROL;
                 break;
             case RtfTokenType.TEXT:
                 currentEvent = TEXT;
                 break;
             case RtfTokenType.EOF:
                 currentEvent = END_DOCUMENT;
                 break;
         }
 
         return currentEvent;
     }
 
     /**
      * Obtiene la palabra clave / smbolo control del elemento actual.
      * @return Palabra clave / smbolo control del elemento actual.
      */
     public String getName()
     {
         return tok.getKey();
     }
 
     /**
      * Obtiene el parmetro del elemento actual.
      * @return Parmetro del elemento actual.
      */
     public int getParam()
     {
         return tok.getParam();
     }
 
     /**
      * Consulta si el elemento actual tiene parmetro.
      * @return Devuelve TRUE si el elemento actual tiene parmetro.
      */
     public boolean getHasParam()
     {
         return tok.getHasParam();
     }
 
     /**
      * Obtiene el texto del elemento actual.
      * @return Texto del elemento actual.
      */
     public String getText()
     {
         return tok.getKey();
     }
 }
