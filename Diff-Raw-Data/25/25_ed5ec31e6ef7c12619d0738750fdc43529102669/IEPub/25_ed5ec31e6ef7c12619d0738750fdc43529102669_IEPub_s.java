 /**
  * scrawler - to.networld.scrawler.interfaces
  *
  * Copyright (C) 2010 by Networld Project
  * Written by Alex Oberhauser <alexoberhauser@networld.to>
  * Written by Corneliu Valentin Stanciu <stanciucorneliu@networld.to>
  * All Rights Reserved
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, version 3 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package to.networld.scrawler.interfaces;
 
 import java.util.Vector;
 
 import to.networld.scrawler.annotations.RDFProperty;
 import to.networld.scrawler.annotations.RDFProperty.Type;
 import to.networld.scrawler.common.Ontologies;
 
 /**
  * @author Alex Oberhauser
  */
 public interface IEPub extends IRDFEntity {
 
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="title", type = Type.LITERAL)
 	public abstract String getTitle();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="language", type = Type.LITERAL)
 	public abstract String getLanguage();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="creator", type = Type.LITERAL)
 	public abstract String getAuthor();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="publisher", type = Type.LITERAL)
 	public abstract String getPublisher();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="description", type = Type.LITERAL)
 	public abstract String getDescription();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="date", type = Type.LITERAL)
	public abstract String getOrginalPublicationDate();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="date", type = Type.LITERAL)
 	public abstract String getOPSPublicationDate();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="subject", type = Type.LITERAL)
 	public abstract Vector<String> getSubjects();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="identifer", type = Type.LITERAL)
 	public abstract String getURIIdentifier();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="identifer", type = Type.LITERAL)
 	public abstract String getPrimaryIdentifier();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="source", type = Type.LITERAL)
 	public abstract String getSource();
 	
 	@RDFProperty(ontoURI=Ontologies.dcURI, value="coverage", type = Type.LITERAL)
 	public abstract String getCoverage();
 }
