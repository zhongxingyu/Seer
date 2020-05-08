 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.util.stax.handler;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.xml.stax.events.EndElement;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class RelsExtContentRelationsReadHandler extends DefaultHandler {
 
     private static final Pattern SPLIT_PATTERN = Pattern.compile("/");
 
     private final StaxParser parser;
 
     private final List<Map<String, String>> relations = new ArrayList<Map<String, String>>();
 
     private boolean inRdf;
 
     private boolean inRelation;
 
     private static final String PATH = "/RDF/Description";
 
     private String targetId;
 
     private String predicate;
 
     public RelsExtContentRelationsReadHandler(final StaxParser parser) {
         this.parser = parser;
     }
 
     public List<Map<String, String>> getRelations() {
         return this.relations;
     }
 
     public boolean isInRelation() {
         return this.inRelation;
     }
 
     public void setInRelation(final boolean inRelation) {
         this.inRelation = inRelation;
     }
 
     public String getTargetId() {
         return this.targetId;
     }
 
     public void setTargetId(final String targetId) {
         this.targetId = targetId;
     }
 
     public String getPredicate() {
         return this.predicate;
     }
 
     public void setPredicate(final String predicate) {
         this.predicate = predicate;
     }
 
     @Override
     public StartElement startElement(final StartElement element) throws WebserverSystemException {
         final String curPath = parser.getCurPath();
 
         if (curPath.equals(PATH)) {
             this.inRdf = true;
         }
         if (this.inRdf && element.getPrefix().equals(Constants.CONTENT_RELATIONS_NS_PREFIX_IN_RELSEXT)) {
             this.inRelation = true;
 
             final int indexOfResource = element.indexOfAttribute(Constants.RDF_NAMESPACE_URI, "resource");
             if (indexOfResource == -1) {
                 throw new WebserverSystemException("The attribute 'rdf:resource' of the element '"
                     + element.getLocalName() + "' is missing.");
             }
             final String resourceValue = element.getAttribute(indexOfResource).getValue();
             final String[] target = SPLIT_PATTERN.split(resourceValue);
             this.targetId = target[1];
            this.predicate = removeTrailingSlash(element.getNamespace()) + '#' + element.getLocalName();
         }
         return element;
     }
 
     @Override
     public EndElement endElement(final EndElement element) {
 
         if (this.inRelation) {
             final Map<String, String> relationData = new HashMap<String, String>();
             relations.add(relationData);
             relationData.put("predicate", this.predicate);
             relationData.put("target", this.targetId);
 
             this.targetId = null;
             this.predicate = null;
             this.inRelation = false;
         }
 
         return element;
     }
 
    /**
     * Remove trainling slash from String.
     * 
     * @param uri
     * @return
     */
    private String removeTrailingSlash(final String uri) {

        if (uri == null) {
            return uri;
        }

        String uniformUri = uri;

        if (uri.endsWith("/")) {
            uniformUri = uri.substring(0, uri.length() - 1);
        }

        return uniformUri;
    }

 }
