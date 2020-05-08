 /*
  * Copyright (C) 2008 Herve Quiroz
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.xproc.step;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 
 import java.util.Set;
 
 import net.sf.saxon.s9api.QName;
 import org.trancecode.xproc.Tubular;
 import org.trancecode.xproc.XProcXmlModel;
 
 /**
  * Standard XProc steps.
  * 
  * @author Herve Quiroz
  */
 public final class XProcSteps
 {
     // Internal steps
     public static final QName ANY = Tubular.namespace().newSaxonQName("any");
 
     // Core steps
     public static final QName CATCH = XProcXmlModel.xprocNamespace().newSaxonQName("catch");
     public static final QName CHOOSE = XProcXmlModel.xprocNamespace().newSaxonQName("choose");
     public static final QName FOR_EACH = XProcXmlModel.xprocNamespace().newSaxonQName("for-each");
     public static final QName GROUP = XProcXmlModel.xprocNamespace().newSaxonQName("group");
     public static final QName OTHERWISE = XProcXmlModel.xprocNamespace().newSaxonQName("otherwise");
     public static final QName PIPELINE = XProcXmlModel.xprocNamespace().newSaxonQName("pipeline");
     public static final QName TRY = XProcXmlModel.xprocNamespace().newSaxonQName("try");
     public static final QName VIEWPORT = XProcXmlModel.xprocNamespace().newSaxonQName("viewport");
     public static final QName WHEN = XProcXmlModel.xprocNamespace().newSaxonQName("when");
 
     public static final Set<QName> CORE_STEPS = ImmutableSet.of(CATCH, CHOOSE, FOR_EACH, GROUP, OTHERWISE, PIPELINE,
             TRY, WHEN);
     public static final Set<QName> WHEN_STEPS = ImmutableSet.of(OTHERWISE, WHEN);
 
     // Required steps
     public static final QName ADD_ATTRIBUTE = XProcXmlModel.xprocNamespace().newSaxonQName("add-attribute");
     public static final QName ADD_XML_BASE = XProcXmlModel.xprocNamespace().newSaxonQName("add-xml-base");
     public static final QName COMPARE = XProcXmlModel.xprocNamespace().newSaxonQName("compare");
     public static final QName COUNT = XProcXmlModel.xprocNamespace().newSaxonQName("count");
     public static final QName DELETE = XProcXmlModel.xprocNamespace().newSaxonQName("delete");
     public static final QName DIRECTORY_LIST = XProcXmlModel.xprocNamespace().newSaxonQName("directory-list");
     public static final QName ERROR = XProcXmlModel.xprocNamespace().newSaxonQName("error");
     public static final QName ESCAPE_MARKUP = XProcXmlModel.xprocNamespace().newSaxonQName("escape-markup");
     public static final QName FILTER = XProcXmlModel.xprocNamespace().newSaxonQName("filter");
     public static final QName HTTP_REQUEST = XProcXmlModel.xprocNamespace().newSaxonQName("http-request");
     public static final QName IDENTITY = XProcXmlModel.xprocNamespace().newSaxonQName("identity");
     public static final QName INSERT = XProcXmlModel.xprocNamespace().newSaxonQName("insert");
     public static final QName LABEL_ELEMENTS = XProcXmlModel.xprocNamespace().newSaxonQName("label-elements");
     public static final QName LOAD = XProcXmlModel.xprocNamespace().newSaxonQName("load");
     public static final QName MAKE_ABSOLUTE_URIS = XProcXmlModel.xprocNamespace().newSaxonQName("make-absolute-uris");
     public static final QName NAMESPACE_RENAME = XProcXmlModel.xprocNamespace().newSaxonQName("namespace-rename");
     public static final QName PACK = XProcXmlModel.xprocNamespace().newSaxonQName("pack");
     public static final QName PARAMETERS = XProcXmlModel.xprocNamespace().newSaxonQName("parameters");
     public static final QName RENAME = XProcXmlModel.xprocNamespace().newSaxonQName("rename");
     public static final QName REPLACE = XProcXmlModel.xprocNamespace().newSaxonQName("replace");
     public static final QName SET_ATTRIBUTES = XProcXmlModel.xprocNamespace().newSaxonQName("set-attributes");
     public static final QName SINK = XProcXmlModel.xprocNamespace().newSaxonQName("sink");
     public static final QName SPLIT_SEQUENCE = XProcXmlModel.xprocNamespace().newSaxonQName("split-sequence");
     public static final QName STORE = XProcXmlModel.xprocNamespace().newSaxonQName("store");
     public static final QName STRING_REPLACE = XProcXmlModel.xprocNamespace().newSaxonQName("string-replace");
     public static final QName UNESCAPE_MARKUP = XProcXmlModel.xprocNamespace().newSaxonQName("unescape-markup");
     public static final QName UNWRAP = XProcXmlModel.xprocNamespace().newSaxonQName("unwrap");
     public static final QName WRAP = XProcXmlModel.xprocNamespace().newSaxonQName("wrap");
     public static final QName WRAP_SEQUENCE = XProcXmlModel.xprocNamespace().newSaxonQName("wrap-sequence");
     public static final QName XINCLUDE = XProcXmlModel.xprocNamespace().newSaxonQName("xinclude");
     public static final QName XSLT = XProcXmlModel.xprocNamespace().newSaxonQName("xslt");
 
     public static final Set<QName> REQUIRED_STEPS = ImmutableSet.of(ADD_ATTRIBUTE, ADD_XML_BASE, COMPARE, COUNT,
             DELETE, DIRECTORY_LIST, ERROR, ESCAPE_MARKUP, FILTER, HTTP_REQUEST, IDENTITY, INSERT, LABEL_ELEMENTS, LOAD,
             MAKE_ABSOLUTE_URIS, NAMESPACE_RENAME, PACK, PARAMETERS, RENAME, REPLACE, SET_ATTRIBUTES, SINK,
             SPLIT_SEQUENCE, STORE, STRING_REPLACE, UNESCAPE_MARKUP, UNWRAP, WRAP, XINCLUDE, XSLT);
 
     // Optional steps
     public static final QName EXEC = XProcXmlModel.xprocNamespace().newSaxonQName("exec");
     public static final QName HASH = XProcXmlModel.xprocNamespace().newSaxonQName("hash");
     public static final QName UUID = XProcXmlModel.xprocNamespace().newSaxonQName("uuid");
     public static final QName VALIDATE_WITH_RELANXNG = XProcXmlModel.xprocNamespace().newSaxonQName(
            "validate-with-relaxng");
     public static final QName VALIDATE_WITH_SCHEMATRON = XProcXmlModel.xprocNamespace().newSaxonQName(
             "validate-with-schematron");
     public static final QName VALIDATE_WITH_SCHEMA = XProcXmlModel.xprocNamespace().newSaxonQName(
             "validate-with-xml-schema");
     public static final QName WWW_FORM_URL_DECODE = XProcXmlModel.xprocNamespace().newSaxonQName("www-form-url-decode");
     public static final QName WWW_FORM_URL_ENCODE = XProcXmlModel.xprocNamespace().newSaxonQName("www-form-url-encode");
     public static final QName XQUERY = XProcXmlModel.xprocNamespace().newSaxonQName("xquery");
     public static final QName XSL_FORMATTER = XProcXmlModel.xprocNamespace().newSaxonQName("xsl-formatter");
 
     public static final Set<QName> OPTIONAL_STEPS = ImmutableSet.of(EXEC, HASH, UUID, VALIDATE_WITH_RELANXNG,
             VALIDATE_WITH_SCHEMA, VALIDATE_WITH_SCHEMATRON, WWW_FORM_URL_DECODE, WWW_FORM_URL_ENCODE, XQUERY,
             XSL_FORMATTER);
 
     public static final Set<QName> ALL_STEPS = ImmutableSet.copyOf(Iterables.concat(CORE_STEPS, REQUIRED_STEPS,
             OPTIONAL_STEPS));
 
     private XProcSteps()
     {
         // No instantiation
     }
 }
