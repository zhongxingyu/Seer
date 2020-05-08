 /*
  * Copyright (c) 2012 David Kellum
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
 
 import java.util.Collections;
 
 import iudex.html.HTML;
 import iudex.html.HTMLTag;
 
 import javax.xml.stream.XMLStreamReader;
 
 import com.gravitext.xml.producer.Attribute;
 import com.gravitext.xml.producer.Namespace;
 import com.gravitext.xml.tree.Element;
 import com.gravitext.xml.tree.StAXConsumer;
 
 /**
  * StaxConsumer building a Node tree using HTML tags, attributes. If tags are
  * not known in advance, they will be created on the fly (inefficiently) as
  * BANNED tags. Unrecognized attributes will be dropped.
  */
 public class HTMLStAXConsumer extends StAXConsumer
 {
     @Override
     @SuppressWarnings("unchecked")
     protected Element createElement( XMLStreamReader sr )
     {
         HTMLTag tag = HTML.TAGS.get( sr.getLocalName() );
 
         if( tag == null ) {
             tag = new HTMLTag( sr.getLocalName(),
                               new Namespace( sr.getPrefix(),
                                              sr.getNamespaceURI() ),
                                Collections.EMPTY_LIST,
                                HTMLTag.Flag.BANNED );
         }
         Element element = new Element( tag );
 
         return element;
     }
 
     @Override
     protected Attribute findAttribute( XMLStreamReader sr, int i )
     {
         return HTML.ATTRIBUTES.get( sr.getAttributeLocalName( i ) );
     }
 }
