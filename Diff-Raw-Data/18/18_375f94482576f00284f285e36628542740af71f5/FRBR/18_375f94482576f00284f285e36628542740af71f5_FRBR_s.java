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
 
 package dk.dbc.opensearch.common.fedora;
 
 
 /**
  *
  */
 public enum FRBR
 {
     FRBR_ABRIDGEMENT( "abridgement" ),
     FRBR_ABRIDGEMENTOF( "abridgementOf" ),
     FRBR_ADAPTION( "adaption" ),
     FRBR_ADAPTIONOF( "adaptionOf" ),
     FRBR_ALTERNATE( "alternate" ),
     FRBR_ALTERNATEOF( "alternateOf" ),
     FRBR_ARRANGEMENT( "arrangement" ),
     FRBR_ARRANGEMENTOF( "arrangementOf" ),
     FRBR_CLASSICALWORK( "ClassicalWork" ),
     FRBR_COMPLEMENT( "complement" ),
     FRBR_COMPLEMENTOF( "complementOf" ),
     FRBR_CONCEPT( "Concept" ),
     FRBR_CORPORATEBODY( "CorporateBody" ),
     FRBR_CREATOR( "creator" ),
     FRBR_CREATOROF( "creatorOf" ),
     FRBR_DATA( "Data" ),
     FRBR_EMBODIMENT( "embodiment" ),
     FRBR_EMBODIMENTOF( "embodimentOf" ),
     FRBR_ENDEAVOUR( "Endeavour" ),
     FRBR_EVENT( "Event" ),
     FRBR_EXEMPLAR( "exemplar" ),
     FRBR_EXEMPLAROF( "exemplarOf" ),
     FRBR_EXPRESSION( "Expression" ),
     FRBR_IMAGE( "Image" ),
     FRBR_IMITATION( "imitation" ),
     FRBR_IMITATIONOF( "imitationOf" ),
     FRBR_ITEM( "Item" ),
     FRBR_LEGALWORK( "LegalWork" ),
     FRBR_LITERARYWORK( "LiteraryWork" ),
     FRBR_MANIFESTATION( "Manifestation" ),
     FRBR_MOVINGIMAGE( "MovingImage" ),
     FRBR_OBJECT( "Object" ),
     FRBR_OWNER( "owner" ),
     FRBR_OWNEROF( "ownerOf" ),
     FRBR_PART( "part" ),
     FRBR_PARTOF( "partOf" ),
     FRBR_PERFORMANCE( "Performance" ),
     FRBR_PERSON( "Person" ),
     FRBR_PLACE( "Place" ),
     FRBR_PRODUCER( "producer" ),
     FRBR_PRODUCEROF( "producerOf" ),
     FRBR_REALIZATION( "realization" ),
     FRBR_REALIZATIONOF( "realizationOf" ),
     FRBR_REALIZER( "realizer" ),
     FRBR_REALIZEROF( "realizerOf" ),
     FRBR_RECONFIGURATION( "reconfiguration" ),
     FRBR_RECONFIGURATIONOF( "reconfigurationOf" ),
     FRBR_RELATEDENDEAVOUR( "relatedEndeavour" ),
     FRBR_REPRODUCTION( "reproduction" ),
     FRBR_REPRODUCTIONOF( "reproductionOf" ),
    //FRBR_RESPONSIBLEENTITY( "ResponsibleEntity" ),
     FRBR_RESPONSIBLEENTITY( "responsibleEntity" ),
     FRBR_RESPONSIBLEENTITYOF( "responsibleEntityOf" ),
     FRBR_REVISION( "revision" ),
     FRBR_REVISIONOF( "revisionOf" ),
     FRBR_SCHOLARLYWORK( "ScholarlyWork" ),
     FRBR_SOUND( "Sound" ),
    //FRBR_SUBJECT( "Subject" ),
     FRBR_SUBJECT( "subject" ),
     FRBR_SUCCESSOR( "successor" ),
     FRBR_SUCCESSOROF( "successorOf" ),
     FRBR_SUMMARIZATION( "summarization" ),
     FRBR_SUMMARIZATIONOF( "summarizationOf" ),
     FRBR_SUPPLEMENT( "supplement" ),
     FRBR_SUPPLEMENTOF( "supplementOf" ),
     FRBR_TEXT( "Text" ),
     FRBR_TRANSFORMATION( "transformation" ),
     FRBR_TRANSFORMATIONOF( "transformationOf" ),
     FRBR_TRANSLATION( "translation" ),
     FRBR_TRANSLATIONOF( "translationOf" ),
     FRBR_WORK( "Work" );
 
     private String literal;
     private final String FRBR_NS = "http://purl.org/vocab/frbr/core#";
 
     FRBR( String literal )
     {
         this.literal = literal;
     }
 }
