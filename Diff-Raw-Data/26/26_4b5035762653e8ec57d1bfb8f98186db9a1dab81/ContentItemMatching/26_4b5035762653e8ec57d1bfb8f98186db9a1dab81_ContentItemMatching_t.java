 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), hosted at https://github.com/gunterze/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4chee.archive.ejb.query;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.Tuple;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.persistence.criteria.Subquery;
 
 import org.dcm4che.data.Attributes;
 import org.dcm4che.data.Tag;
 import org.dcm4chee.archive.persistence.AttributeFilter;
 import org.dcm4chee.archive.persistence.ContentItem;
 import org.dcm4chee.archive.persistence.ContentItem_;
 /**
  * @author Michael Backhaus <michael.backhaus@agfa.com>
  */
 class ContentItemMatching {
 
     public static Predicate withContentItem(CriteriaBuilder cb,
             CriteriaQuery<Tuple> cq,
             Expression<Collection<ContentItem>> collection, Attributes item,
             String valueType, List<Object> params) {
        if ("CODE".equals(valueType) || "TEXT".equals(valueType)){
            return withContentItem(cq, cb, collection, item, params);
         }
         return null;
     }
 
     private static Predicate withContentItem(CriteriaQuery<Tuple> cq, 
             CriteriaBuilder cb, Expression<Collection<ContentItem>> collection, 
            Attributes item, List<Object> params) {
         Subquery<ContentItem> sq = cq.subquery(ContentItem.class);
         Root<ContentItem> contentItem = sq.from(ContentItem.class);
         sq.select(contentItem);
         ArrayList<Predicate> predicates = new ArrayList<Predicate>(4);
         predicates.add(cb.isMember(contentItem, collection));
        if (!addContentItemPredicates(cq, cb, item, params, contentItem, predicates))
             return null;
 
         sq.where(predicates.toArray(new Predicate[predicates.size()]));
         return cb.exists(sq);
     }
 
     private static boolean addContentItemPredicates(CriteriaQuery<Tuple> cq,
            CriteriaBuilder cb, Attributes item, List<Object> params,
             Root<ContentItem> contentItem, ArrayList<Predicate> predicates) {
         boolean restrict = Matching.add(predicates, 
                 Matching.withCode(cb, cq, 
                         contentItem.get(ContentItem_.conceptName), 
                         item.getNestedDataset(Tag.ConceptNameCodeSequence),
                         false, params));
         restrict = Matching.add(predicates, 
                 Matching.wildCard(cb, 
                         contentItem.get(ContentItem_.relationshipType), 
                         AttributeFilter.getString(item, Tag.RelationshipType),
                         false, params))
                || restrict;
         restrict = Matching.add(predicates, 
                 Matching.withCode(cb, cq, 
                         contentItem.get(ContentItem_.conceptCode), 
                        item.getNestedDataset(Tag.ConceptCodeSequence), false, params))
                || restrict;
         restrict = Matching.add(predicates, 
                 Matching.wildCard(cb, 
                         contentItem.get(ContentItem_.textValue), 
                        AttributeFilter.getString(item, Tag.TextValue), false, params))
                || restrict;
         return restrict;
     }
 }
