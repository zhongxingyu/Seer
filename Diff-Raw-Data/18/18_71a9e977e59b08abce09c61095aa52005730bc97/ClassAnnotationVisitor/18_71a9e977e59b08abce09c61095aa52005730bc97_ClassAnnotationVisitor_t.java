 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.services.efo;
 
 import org.semanticweb.owlapi.model.OWLAnnotation;
 import org.semanticweb.owlapi.model.OWLAnnotationProperty;
 import org.semanticweb.owlapi.model.OWLLiteral;
 import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;
 
 /**
  * @author Olga Melnichuk
  */
 class ClassAnnotationVisitor extends OWLObjectVisitorAdapter {
 
     private enum Literal {
         LABEL {
             @Override
             public boolean matches(OWLAnnotationProperty property) {
                 return property.isLabel();
             }
 
             @Override
             public void updateNode(String literal, EfoNodeImpl node) {
                 node.setTerm(literal);
             }
         },
         ORGANIZATIONAL_CLASS {
             @Override
             public boolean matches(OWLAnnotationProperty property) {
                return  "organizational_class".equals(property.getIRI().getFragment());
             }
 
             @Override
             public void updateNode(String literal, EfoNodeImpl node) {
                 node.setOrganizational(Boolean.valueOf(literal));
             }
         },
         ALTERNATIVE_TERM {
             @Override
             public boolean matches(OWLAnnotationProperty property) {
                 return "alternative_term".equals(property.getIRI().getFragment())
                         || "definition_citation".equals(property.getIRI().getFragment());
             }
 
             @Override
             public void updateNode(String literal, EfoNodeImpl node) {
                 node.addAlternativeTerm(preprocessAlternativeTermString(literal));
             }
 
             private String preprocessAlternativeTermString(String str) {
                 return str == null ? null :
                         str.replaceAll("(\\[accessedResource:[^\\]]+\\])|(\\[accessDate:[^\\]]+\\])", "").trim();
             }
         };
 
         public abstract boolean matches(OWLAnnotationProperty property);
 
         public abstract void updateNode(String literal, EfoNodeImpl node);
 
         public static Literal find(OWLAnnotation annotation) {
             if (!(annotation.getValue() instanceof OWLLiteral)) {
                 return null;
             }
             for(Literal lit : values()) {
                 if (lit.matches(annotation.getProperty())) {
                     return lit;
                 }
             }
             return null;
         }
     }
 
     private final EfoNodeImpl node;
 
     ClassAnnotationVisitor(String nodeId) {
         node = new EfoNodeImpl(nodeId);
     }
 
     @Override
     public void visit(OWLAnnotation annot) {
         Literal lit = Literal.find(annot);
         if (lit == null) {
             return;
         }
 
         lit.updateNode(((OWLLiteral) annot.getValue()).getLiteral(), node);
     }
 
     EfoNodeImpl getNode() {
         return node;
     }
 }
