/*
 * Copyright (c) 2010-2012 David Kellum
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

 package iudex.html.tree;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.transform.Source;
 
 import com.gravitext.xml.tree.Element;
 import com.gravitext.xml.tree.StAXUtils;
 
 public class HTMLStAXUtils extends StAXUtils
 {
     public static Element readDocument( XMLStreamReader sr )
         throws XMLStreamException
     {
         return new HTMLStAXConsumer().readDocument( sr );
     }
 
     public static Element staxParse( Source source )
         throws XMLStreamException
     {
         return readDocument( staxReader( source ) );
     }
 }
