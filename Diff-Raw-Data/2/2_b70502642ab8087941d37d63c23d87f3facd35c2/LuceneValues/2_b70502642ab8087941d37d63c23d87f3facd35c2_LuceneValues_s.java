 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.business.fedora.resources;
 
 import java.text.MessageFormat;
 import java.util.regex.Pattern;
 
 /**
  * Encapsulate the sub queries for Lucene filtering.
  * 
  * spring.bean id="filter.Values"
  * @author Andr&eacute; Schenk
  */
 public final class LuceneValues extends Values {
     // The following place holders may be used:
     // {0} : userId
     // {1} : roleId
     // {2} : groupSQL
     // {3} : quotedGroupSQL (used in stored procedures)
     // {4} : list of user grants and user group grants
     // {5} : list of hierarchical containers
     // {6} : list of hierarchical OUs
     private static final String ID_SQL = "permissions-filter.PID:({4})";
 
     // some constants for escaping
     private static final String LUCENE_ESCAPE_CHARS =
         "[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
 
     private static final Pattern LUCENE_PATTERN =
         Pattern.compile(LUCENE_ESCAPE_CHARS);
 
     private static final String REPLACEMENT_STRING = "\\\\$0";
 
     static {
         FUNCTION_MAP.put(FUNCTION_AND, "AND");
         FUNCTION_MAP.put(FUNCTION_OR, "OR");
         FUNCTION_MAP.put(FUNCTION_STRING_CONTAINS, "");
         FUNCTION_MAP.put(FUNCTION_STRING_EQUAL, "AND");
         FUNCTION_MAP.put(FUNCTION_STRING_ONE_AND_ONLY, "");
 
         OPERAND_MAP.put("component", "permissions-filter.component-id");
         OPERAND_MAP.put("content-model", "/properties/content-model/id");
         OPERAND_MAP.put("context", "permissions-filter.context-id");
         OPERAND_MAP.put("created-by", "permissions-filter.created-by");
         OPERAND_MAP.put("latest-release-number",
             "/properties/latest-release/number");
         OPERAND_MAP.put("latest-version-modified-by",
             "/properties/version/modified-by/id");
         OPERAND_MAP.put("latest-version-number", "/properties/version/number");
         OPERAND_MAP.put("latest-version-status", "/properties/version/status");
         OPERAND_MAP.put("lock-date", "/properties/lock-date");
         OPERAND_MAP.put("lock-owner", "/properties/lock-owner");
         OPERAND_MAP.put("lock-status", "/properties/lock-status");
         OPERAND_MAP.put("organizational-unit",
             "/properties/organizational-units/organizational-unit/id");
         OPERAND_MAP.put("public-status", "permissions-filter.public-status");
         OPERAND_MAP.put("subject-id", USER_ID);
         OPERAND_MAP.put("version-modified-by",
             "/properties/version/modified-by/id");
         OPERAND_MAP.put("version-status", "permissions-filter.version.status");
 
         // resource container
         SCOPE_MAP.put("info:escidoc/names:aa:1.0:resource:container:context",
             "permissions-filter.objecttype:container "
                 + "AND permissions-filter.context-id:({4})");
 
         SCOPE_MAP.put("info:escidoc/names:aa:1.0:resource:container:container",
             "permissions-filter.objecttype:container "
                 + "AND permissions-filter.parent:({4})");
 
         SCOPE_MAP
             .put(
                 "info:escidoc/names:aa:1.0:resource:container:hierarchical-containers",
                 "permissions-filter.objecttype:container "
                     + "AND permissions-filter.PID:({5})");
 
         SCOPE_MAP
             .put("info:escidoc/names:aa:1.0:resource:container-id", ID_SQL);
 
         // resource content relation
         SCOPE_MAP.put("info:escidoc/names:aa:1.0:resource:content-relation-id",
             ID_SQL);
 
         // resource context
         SCOPE_MAP.put("info:escidoc/names:aa:1.0:resource:context-id", ID_SQL);
 
         // resource item
         SCOPE_MAP.put("info:escidoc/names:aa:1.0:resource:item:component",
             "permissions-filter.objecttype:item "
                 + "AND permissions-filter.component-id:({4})");
 
         SCOPE_MAP.put("info:escidoc/names:aa:1.0:resource:item:container",
             "permissions-filter.objecttype:item "
                 + "AND permissions-filter.parent:({4})");
 
         SCOPE_MAP.put(
             "info:escidoc/names:aa:1.0:resource:item:container.collection",
             "<info:escidoc/names:aa:1.0:resource:item:container.collection>");
 
         SCOPE_MAP.put("info:escidoc/names:aa:1.0:resource:item:context",
             "permissions-filter.objecttype:item "
                 + "AND permissions-filter.context-id:({4})");
 
         SCOPE_MAP.put(
             "info:escidoc/names:aa:1.0:resource:item:hierarchical-containers",
             "permissions-filter.objecttype:item "
                 + "AND permissions-filter.parent:({5})");
 
         SCOPE_MAP.put("info:escidoc/names:aa:1.0:resource:item-id", ID_SQL);
 
         // resource organizational unit
         SCOPE_MAP
             .put(
                 "info:escidoc/names:aa:1.0:resource:organizational-unit:hierarchical-parents",
                "(permissions-filter.objecttype:organizational-unit "
                     + "AND permissions-filter.PID:({6})");
     }
 
     /**
      * Escape a string.
      * 
      * @param s
      *            string
      * 
      * @return the escaped string
      */
     public String escape(final String s) {
         return LUCENE_PATTERN.matcher(s).replaceAll(REPLACEMENT_STRING);
     }
 
     /**
      * Combine the given operands with AND.
      * 
      * @param operand1
      *            first operand
      * @param operand2
      *            second operand
      * 
      * @return AND conjunction of the given operands
      */
     public String getAndCondition(final String operand1, final String operand2) {
         return MessageFormat.format("({0}) AND ({1})", new Object[] { operand1,
             operand2 });
     }
 
     /**
      * Get a CONTAINS statement with the given operand.
      * 
      * @param operand
      *            operand
      * 
      * @return CONTAINS statement with the given operand
      */
     public String getContainsCondition(final String operand) {
         return operand;
     }
 
     /**
      * Combine the given operands with =.
      * 
      * @param operand1
      *            first operand
      * @param operand2
      *            second operand
      * 
      * @return EQUALS conjunction of the given operands
      */
     public String getEqualCondition(final String operand1, final String operand2) {
         return MessageFormat.format("{0}:{1}", new Object[] { operand1,
             operand2 });
     }
 
     /**
      * Get a condition of the form key=operand1 and value=operand2.
      * 
      * @param operand1
      *            first operand
      * @param operand2
      *            second operand
      * 
      * @return key/value statement of the given operands
      */
     public String getKeyValueCondition(
         final String operand1, final String operand2) {
         return getEqualCondition(operand1, operand2);
     }
 
     /**
      * Get a statement which does not affect another statement when combining it
      * with AND.
      * 
      * @param resourceType
      *            resource type
      * 
      * @return neutral element for AND
      */
     public String getNeutralAndElement(final ResourceType resourceType) {
         return "permissions-filter.objecttype:" + resourceType.getLabel();
     }
 
     /**
      * Combine the given operands with OR.
      * 
      * @param operand1
      *            first operand
      * @param operand2
      *            second operand
      * 
      * @return OR conjunction of the given operands
      */
     public String getOrCondition(final String operand1, final String operand2) {
         return MessageFormat.format("({0}) OR ({1})", new Object[] { operand1,
             operand2 });
     }
 }
