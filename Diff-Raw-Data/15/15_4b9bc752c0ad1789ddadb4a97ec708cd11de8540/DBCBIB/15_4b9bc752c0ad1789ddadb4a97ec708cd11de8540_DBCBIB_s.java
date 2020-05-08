 /*
 This file is part of opensearch.
 Copyright © 2009, Dansk Bibliotekscenter a/s,
 Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
 opensearch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 opensearch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package dk.dbc.opensearch.common.metadata;
 
 import javax.xml.namespace.QName;
 
 /**
  * Enum type reflecting object properties in the ontology
  * {http://oss.dbc.dk/rdf/dkbib}dbcbib. The types found herein
  * describes the valid predicates that can be expressed between
  * objects of types Work and Manifestation.  And Handling of of
  * Collections.
  */
 public enum DBCBIB implements IPredicate
 {
     IS_MEMBER_OF_WORK( "isMemberOfWork"),
     HAS_MANIFESTATION( "hasManifestation" ),
     IS_OWNED_BY( "isOwnedBy" ),
    IS_MEMBER_OF_COlECTION( "isMemberOfCollection");
     
     private String localName;
     private final String NS = "http://oss.dbc.dk/rdf/dkbib#";
     private final String prefix = "dbcbib";
 
     DBCBIB( String name )
     {
         this.localName = name;
     }
 
     public QName getPredicate()
     {
         return new QName( NS, this.localName, prefix );
     }
 
     @Override
     public String getPredicateString()
     {       
         return NS+localName; 
     }
 }
