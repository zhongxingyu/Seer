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
 
 package dk.dbc.opensearch.components.harvest;
 
 import java.lang.UnsupportedOperationException;
 import org.w3c.dom.Document;
 /**
  * 
  */
 public class Job implements IJob
 {
     
     private IIdentifier identifier;
     private Document referenceData;
 
     public Job( IIdentifier identifier, Document referenceData )
     {
         this.identifier = identifier;
         this.referenceData = referenceData;
     }
     
     public IIdentifier getIdentifier()
     {
         return identifier;
     }
 
     public Document getReferenceData()
     {
         return referenceData;
     }
 
 }
