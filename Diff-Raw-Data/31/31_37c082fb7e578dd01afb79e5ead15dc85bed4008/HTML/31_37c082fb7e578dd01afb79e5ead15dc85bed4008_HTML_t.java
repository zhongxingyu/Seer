 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 /*
  * Copyright 2004 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions andd
  * limitations under the License.
  */
 
 package org.icefaces.ace.util;
 
 /**
  * Constant declarations for HTML rendering.
  *
  * @author Manfred Geiler (latest modification by $Author: svieujot $)
  * @author Anton Koinov
  * @version * $Revision: 1.22 $ $Date: 2005/02/28 20:40:15 $ $Log: HTML.java,v
  *          $
  */
 
 public final class HTML {
 
     private HTML() {
         // disable instantiation
     }
 
     // Common attributes
     public static final String ALIGN_ATTR = "align";
     public static final String DATAFLD_ATTR = "datafld";
     public static final String DATASRC_ATTR = "datasrc";
     public static final String DATAFORMATAS_ATTR = "dataformatas";
     public static final String BORDER_ATTR = "border";
     public static final String WIDTH_ATTR = "width";
     public static final String READONLY_ATTR = "readonly";
     public static final String ACCEPT_ATTR = "accept";
     public static final String ACCEPTCHARSET_ATTR = "accept-charset";
 
     // Common event handler attributes
     public static final String ONCLICK_ATTR = "onclick";
     public static final String ONDBLCLICK_ATTR = "ondblclick";
     public static final String ONMOUSEDOWN_ATTR = "onmousedown";
     public static final String ONMOUSEUP_ATTR = "onmouseup";
     public static final String ONMOUSEOVER_ATTR = "onmouseover";
     public static final String ONMOUSEMOVE_ATTR = "onmousemove";
     public static final String ONMOUSEOUT_ATTR = "onmouseout";
     public static final String ONKEYPRESS_ATTR = "onkeypress";
     public static final String ONKEYDOWN_ATTR = "onkeydown";
     public static final String ONKEYUP_ATTR = "onkeyup";
     public static final String ONCONTEXTMENU_ATTR = "oncontextmenu";
     public static final String[] EVENT_HANDLER_ATTRIBUTES_WITHOUT_ONCLICK =
             {
                     ONDBLCLICK_ATTR,
                     ONMOUSEDOWN_ATTR,
                     ONMOUSEUP_ATTR,
                     ONMOUSEOVER_ATTR,
                     ONMOUSEMOVE_ATTR,
                     ONMOUSEOUT_ATTR,
                     ONKEYPRESS_ATTR,
                     ONKEYDOWN_ATTR,
                     ONKEYUP_ATTR,
                     ONCONTEXTMENU_ATTR
             };
     public static final String[] EVENT_HANDLER_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     EVENT_HANDLER_ATTRIBUTES_WITHOUT_ONCLICK,
                     new String[]{ONCLICK_ATTR});
 
     public static final String ONSUBMIT_ATTR = "onsubmit";
     // Input field event handler attributes
     public static final String ONFOCUS_ATTR = "onfocus";
     public static final String ONBLUR_ATTR = "onblur";
     public static final String ONSELECT_ATTR = "onselect";
     public static final String ONCHANGE_ATTR = "onchange";
     public static final String[] COMMON_FIELD_EVENT_ATTRIBUTES =
             {
                     ONFOCUS_ATTR,
                     ONBLUR_ATTR,
                     ONSELECT_ATTR,
                     ONCHANGE_ATTR
             };
 
     // universal attributes
     public static final String DIR_ATTR = "dir";
     public static final String LANG_ATTR = "lang";
     public static final String STYLE_ATTR = "style";
     public static final String TITLE_ATTR = "title";
     public static final String STYLE_CLASS_ATTR =
             "styleClass"; //"class" cannot be used as property name
     public static final String[] UNIVERSAL_ATTRIBUTES_WITHOUT_STYLE =
             {
                     DIR_ATTR,
                     LANG_ATTR,
                     TITLE_ATTR,
 
                     //NOTE: if changed, please verify universal attributes in HtmlMessageRenderer !
             };
     public static final String[] UNIVERSAL_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     UNIVERSAL_ATTRIBUTES_WITHOUT_STYLE,
                     new String[]{STYLE_ATTR, STYLE_CLASS_ATTR});
 
     //universal, but not the same property-name -
     //styleClass attribute is rendered as such
     public static final String CLASS_ATTR = "class";
 
     // common form field attributes
     public static final String ACCESSKEY_ATTR = "accesskey";
     public static final String TABINDEX_ATTR = "tabindex";
     public static final String DISABLED_ATTR = "disabled";
     public static final String[] COMMON_FIELD_ATTRIBUTES_WITHOUT_DISABLED =
             {
                     ACCESSKEY_ATTR,
                     TABINDEX_ATTR
             };
     public static final String[] COMMON_FIELD_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     COMMON_FIELD_ATTRIBUTES_WITHOUT_DISABLED,
                     new String[]{DISABLED_ATTR});
 
     // Common Attributes    
     public static final String[] COMMON_PASSTROUGH_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     EVENT_HANDLER_ATTRIBUTES,
                     UNIVERSAL_ATTRIBUTES);
     public static final String[] COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_STYLE =
             (String[]) ArrayUtils.concat(
                     EVENT_HANDLER_ATTRIBUTES,
                     UNIVERSAL_ATTRIBUTES_WITHOUT_STYLE);
     public static final String[] COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_ONCLICK =
             (String[]) ArrayUtils.concat(
                     EVENT_HANDLER_ATTRIBUTES_WITHOUT_ONCLICK,
                     UNIVERSAL_ATTRIBUTES);
     public static final String[] COMMON_FIELD_PASSTROUGH_ATTRIBUTES_WITHOUT_DISABLED =
             (String[]) ArrayUtils.concat(
                     COMMON_PASSTROUGH_ATTRIBUTES,
                     COMMON_FIELD_ATTRIBUTES_WITHOUT_DISABLED,
                     COMMON_FIELD_EVENT_ATTRIBUTES);
     public static final String[] COMMON_FIELD_PASSTROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_ONCLICK =
             (String[]) ArrayUtils.concat(
                     COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_ONCLICK,
                     COMMON_FIELD_ATTRIBUTES_WITHOUT_DISABLED,
                     COMMON_FIELD_EVENT_ATTRIBUTES);
 
     // <a>
     public static final String TARGET_ATTR = "target";  //used by <a> and <form>
     public static final String CHARSET_ATTR = "charset";
     public static final String COORDS_ATTR = "coords";
     public static final String HREF_ATTR = "href";
     public static final String HREFLANG_ATTR = "hreflang";
     public static final String REL_ATTR = "rel";
     public static final String REV_ATTR = "rev";
     public static final String SHAPE_ATTR = "shape";
     public static final String TYPE_ATTR = "type";
     public static final String[] ANCHOR_ATTRIBUTES =
             {
                     ACCESSKEY_ATTR,
                     CHARSET_ATTR,
                     COORDS_ATTR,
                     HREFLANG_ATTR,
                     REL_ATTR,
                     REV_ATTR,
                     SHAPE_ATTR,
                     TABINDEX_ATTR,
                     TARGET_ATTR,
                     TYPE_ATTR
             };
     public static final String[] ANCHOR_PASSTHROUGH_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     ANCHOR_ATTRIBUTES,
                     COMMON_PASSTROUGH_ATTRIBUTES);
     public static final String[] ANCHOR_PASSTHROUGH_ATTRIBUTES_WITHOUT_STYLE =
             (String[]) ArrayUtils.concat(
                     ANCHOR_ATTRIBUTES,
                     COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_STYLE);
 
     // <form>
     public static final String ACCEPT_CHARSET_ATTR = "accept-charset";
     public static final String ENCTYPE_ATTR = "enctype";
     public static final String ONRESET_ATTR = "onreset";
     public static final String ONSUMBIT_ATTR = "onsubmit";
     public static final String[] FORM_ATTRIBUTES =
             {
                     ACCEPT_ATTR,
                     ACCEPT_CHARSET_ATTR,
                     ENCTYPE_ATTR,
                     ONRESET_ATTR,
                     ONSUMBIT_ATTR,
                     TARGET_ATTR,
             };
     
     public static final String AUTOCOMPLETE_ATTR = "autocomplete";
     public static final String[] FORM_PASSTHROUGH_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     FORM_ATTRIBUTES,
                     COMMON_PASSTROUGH_ATTRIBUTES);
 
     // values for enctype attribute
     public static String ENCTYPE_MULTIPART_FORMDATA = "multipart/form-data";
 
     // <img>
     public static final String SRC_ATTR = "src";
     public static final String ALT_ATTR = "alt";
     public static final String HEIGHT_ATTR = "height";
     public static final String HSPACE_ATTR = "hspace";
     public static final String ISMAP_ATTR = "ismap";
     public static final String LONGDESC_ATTR = "longdesc";
     public static final String USEMAP_ATTR = "usemap";
     public static final String VSPACE_ATTR = "vspace";
 
     public static final String[] IMG_ATTRIBUTES =
             {
                     ALIGN_ATTR,
                     ALT_ATTR,
                     BORDER_ATTR,
                     HEIGHT_ATTR,
                     HSPACE_ATTR,
                     ISMAP_ATTR,
                     LONGDESC_ATTR,
                     USEMAP_ATTR,
                     VSPACE_ATTR,
                     WIDTH_ATTR
             };
     public static final String[] IMG_PASSTHROUGH_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     IMG_ATTRIBUTES,
                     COMMON_PASSTROUGH_ATTRIBUTES);
 
     // <input>
     public static final String SIZE_ATTR = "size";
     public static final String CHECKED_ATTR = "checked";
     public static final String MAXLENGTH_ATTR = "maxlength";
 
     public static final String[] INPUT_ATTRIBUTES = {
             ALIGN_ATTR,
             ALT_ATTR,
             CHECKED_ATTR,
             DATAFLD_ATTR,
             DATASRC_ATTR,
             DATAFORMATAS_ATTR,
             MAXLENGTH_ATTR,
             READONLY_ATTR,
             SIZE_ATTR,
     };
     public static final String[] INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED =
             (String[]) ArrayUtils.concat(
                     INPUT_ATTRIBUTES,
                     COMMON_FIELD_PASSTROUGH_ATTRIBUTES_WITHOUT_DISABLED);
 
     //values for input-type attribute
     public static final String INPUT_TYPE_SUBMIT = "submit";
     public static final String INPUT_TYPE_IMAGE = "image";
     public static final String INPUT_TYPE_HIDDEN = "hidden";
     public static final String INPUT_TYPE_CHECKBOX = "checkbox";
     public static final String INPUT_TYPE_PASSWORD = "password";
     public static final String INPUT_TYPE_TEXT = "text";
     public static final String INPUT_TYPE_RADIO = "radio";
     public static final String INPUT_TYPE_FILE = "file";
 
     // <button>
     public static final String[] BUTTON_ATTRIBUTES =
             {
                     ALIGN_ATTR,
                     ALT_ATTR,
                     DATAFLD_ATTR,
                     DATASRC_ATTR,
                     DATAFORMATAS_ATTR,
             };
     public static final String[] BUTTON_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED =
             (String[]) ArrayUtils.concat(
                     BUTTON_ATTRIBUTES,
                     COMMON_FIELD_PASSTROUGH_ATTRIBUTES_WITHOUT_DISABLED);
     public static final String[] BUTTON_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_ONCLICK =
             (String[]) ArrayUtils.concat(
                     BUTTON_ATTRIBUTES,
                     COMMON_FIELD_PASSTROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_ONCLICK);
 
     // <iframe>
     public static final String FRAMEBORDER_ATTR = "frameborder";
     public static final String SCROLLING_ATTR = "scrolling";
 
     // <label>
     public static final String FOR_ATTR = "for";
     public static final String[] LABEL_ATTRIBUTES =
             {
                     ACCESSKEY_ATTR,
                     ONBLUR_ATTR,
                     ONFOCUS_ATTR
                     //FOR_ATTR is no pass through !
             };
     public static final String[] LABEL_PASSTHROUGH_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     LABEL_ATTRIBUTES,
                     COMMON_PASSTROUGH_ATTRIBUTES);
 
     // <select>
     public static final String MULTIPLE_ATTR = "multiple";
 
     public static final String[] SELECT_ATTRIBUTES =
             {
                     DATAFLD_ATTR,
                     DATASRC_ATTR,
                     DATAFORMATAS_ATTR,
             };
     public static final String[] SELECT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED =
             (String[]) ArrayUtils.concat(
                     SELECT_ATTRIBUTES,
                     COMMON_FIELD_PASSTROUGH_ATTRIBUTES_WITHOUT_DISABLED);
 
     // <table>
     public static final String BGCOLOR_ATTR = "bgcolor";
     public static final String CELLPADDING_ATTR = "cellpadding";
     public static final String CELLSPACING_ATTR = "cellspacing";
     public static final String FRAME_ATTR = "frame";
     public static final String RULES_ATTR = "rules";
     public static final String SUMMARY_ATTR = "summary";
     public static final String[] TABLE_ATTRIBUTES = {
             ALIGN_ATTR,
             BGCOLOR_ATTR,
             BORDER_ATTR,
             CELLPADDING_ATTR,
             CELLSPACING_ATTR,
             DATAFLD_ATTR,
             DATASRC_ATTR,
             DATAFORMATAS_ATTR,
             FRAME_ATTR,
             RULES_ATTR,
             HEIGHT_ATTR,
             SUMMARY_ATTR,
             WIDTH_ATTR
     };
     public static final String[] TABLE_PASSTHROUGH_ATTRIBUTES =
             (String[]) ArrayUtils.concat(
                     TABLE_ATTRIBUTES,
                     COMMON_PASSTROUGH_ATTRIBUTES);
 
     // <textarea>
     public static final String COLS_ATTR = "cols";
     public static final String ROWS_ATTR = "rows";
     public static final String[] TEXTAREA_ATTRIBUTES =
             {
                     COLS_ATTR,
                     DATAFLD_ATTR,
                     DATASRC_ATTR,
                     DATAFORMATAS_ATTR,
                     READONLY_ATTR,
                     ROWS_ATTR,
             };
     public static final String[] TEXTAREA_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED =
             (String[]) ArrayUtils.concat(
                     TEXTAREA_ATTRIBUTES,
                     COMMON_FIELD_PASSTROUGH_ATTRIBUTES_WITHOUT_DISABLED);
 
     // <input type=file>
     public static final String[] INPUT_FILE_UPLOAD_ATTRIBUTES =
             {
                     ACCEPT_ATTR
             };
     public static final String[] INPUT_FILE_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED =
             (String[]) ArrayUtils.concat(
                     INPUT_FILE_UPLOAD_ATTRIBUTES,
                     INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
 
     /*
 public static final String[] MESSAGE_PASSTHROUGH_ATTRIBUTES =
 (String[]) ArrayUtils.concat(
     new String[] {DIR_ATTR, LANG_ATTR, TITLE_ATTR, STYLE_ATTR, STYLE_CLASS_ATTR},
     EVENT_HANDLER_ATTRIBUTES);
     */
 
     public static final String[] MESSAGE_PASSTHROUGH_ATTRIBUTES_WITHOUT_TITLE_STYLE_AND_STYLE_CLASS =
             (String[]) ArrayUtils.concat(
                     new String[]{DIR_ATTR, LANG_ATTR},
                     EVENT_HANDLER_ATTRIBUTES);
 
 
     // selectOne/Many table
     public static final String[] SELECT_TABLE_PASSTHROUGH_ATTRIBUTES =
             new String[]{STYLE_ATTR, STYLE_CLASS_ATTR, BORDER_ATTR};
 
     //HTML attributes needed for renderding only
     public static final String ID_ATTR = "id";
     public static final String NAME_ATTR = "name";
     public static final String VALUE_ATTR = "value";
     public static final String METHOD_ATTR = "method";
     public static final String ACTION_ATTR = "action";
     public static final String COLSPAN_ATTR = "colspan";
     public static final String ROWSPAN_ATTR = "rowspan";    
     public static final String SCOPE_ATTR = "scope";
     public static final String LABEL_ATTR = "label";
     public static final String SELECTED_ATTR = "selected";
 
     //ARIA Attributes
     public static final String ROLE_ATTR = "role";
     public static final String ARIA_DESCRIBED_BY_ATTR = "aria-describedby";
     public static final String ARIA_DISABLED_ATTR = "aria-disabled";
     public static final String ARIA_LABELLED_BY_ATTR = "aria-labelledby";
    public static final String ARIA_MULTI_SELECTABLE_ATTR = "aria-multiselectable";
 
     //HTML attributes values
     public static final String SCOPE_COLGROUP_VALUE = "colgroup";
 
     //HTML element constants
     public static final String SPAN_ELEM = "span";
     public static final String DIV_ELEM = "div";
     public static final String INPUT_ELEM = "input";
     public static final String BUTTON_ELEM = "button";
     public static final String SELECT_ELEM = "select";
     public static final String OPTION_ELEM = "option";
     public static final String OPTGROUP_ELEM = "optgroup";
     public static final String TEXTAREA_ELEM = "textarea";
     public static final String FORM_ELEM = "form";
     public static final String ANCHOR_ELEM = "a";
     public static final String H1_ELEM = "h1";
     public static final String H2_ELEM = "h2";
     public static final String H3_ELEM = "h3";
     public static final String H4_ELEM = "h4";
     public static final String H5_ELEM = "h5";
     public static final String H6_ELEM = "h6";
     public static final String IFRAME_ELEM = "iframe";
     public static final String IMG_ELEM = "img";
     public static final String LABEL_ELEM = "label";
     public static final String TABLE_ELEM = "table";
     public static final String TR_ELEM = "tr";
     public static final String TH_ELEM = "th";
     public static final String TD_ELEM = "td";
     public static final String TBODY_ELEM = "tbody";
     public static final String TFOOT_ELEM = "tfoot";
     public static final String THEAD_ELEM = "thead";
     public static final String STYLE_ELEM = "style";
     public static final String SCRIPT_ELEM = "script";
     public static final String SCRIPT_TYPE_ATTR = "type";
     public static final String SCRIPT_TYPE_TEXT_JAVASCRIPT = "text/javascript";
     public static final String SCRIPT_LANGUAGE_ATTR = "language";
     public static final String SCRIPT_LANGUAGE_JAVASCRIPT = "JavaScript";
     public static final String MAP_ELEM = "map";
     public static final String UL_ELEM = "ul";
     public static final String OL_ELEM = "ol";
     public static final String LI_ELEM = "li";
     public static final String FIELDSET_ELEM = "fieldset";
 
     //HTML simple element constants
     public static final String BR_ELEM = "br";
 
 
     //HTML entities
     public static final String NBSP_ENTITY = "&#160;";
 
     public static final String HREF_PATH_SEPARATOR = "/";
     public static final String HREF_PATH_FROM_PARAM_SEPARATOR = "?";
     public static final String HREF_PARAM_SEPARATOR = "&";
     public static final String HREF_PARAM_NAME_FROM_VALUE_SEPARATOR = "=";
 
 	public static String[] CLICK_EVENT = {"onclick"};
 
 	public static String[] BLUR_FOCUS_EVENTS = {
 		"onblur",
 		"onfocus"
 	};
 
 	public static String[] CHANGE_SELECT_EVENTS = {
 		"onchange",
 		"onselect"
 	};
 
 	public static String[] COMMON_EVENTS = {
 		"onclick",
 		"ondblclick",
 		"onkeydown",
 		"onkeypress",
 		"onkeyup",
 		"onmousedown",
 		"onmousemove",
 		"onmouseout",
 		"onmouseover",
 		"onmouseup"
 	};
 
 	//StyleClass is omitted
 	public static String[] IMG_ATTRS_WITHOUT_EVENTS = {
 		"alt",
 		"width",
 		"height",
 		"title",
 		"dir",
 		"lang",
 		"ismap",
 		"usemap",
 		"style"
 	};
 
 	//StyleClass is omitted
 	public static String[] LINK_ATTRS_WITHOUT_EVENTS = {
 		"accesskey",
 		"charset",
 		"coords",
 		"dir",
 		"disabled",
 		"hreflang",
 		"rel",
 		"rev",
 		"shape",
 		"tabindex",
 		"style",
 		"target",
 		"title",
 		"type"
 	};
 
 	//StyleClass is omitted
 	public static String[] BUTTON_ATTRS_WITHOUT_EVENTS = {
 		"accesskey",
 		"alt",
 		"dir",
 		"label",
 		"lang",
 		"style",
 		"tabindex",
 		"title",
 		"type"
 	};
 
 	//StyleClass is omitted
 	public static String[] MEDIA_ATTRS = {
 		"height",
 		"width",
 		"style"
 	};
 
     //disabled, readonly, style, styleClass handles by component renderer
 	public static String[] INPUT_TEXT_ATTRS_WITHOUT_EVENTS = {
 		"accesskey",
 		"alt",
         "autocomplete",
 		"dir",
 		"lang",
 		"maxlength",
 		"size",
 		"tabindex",
 		"title"
 	};
 
     public static String[] SELECT_ATTRS_WITHOUT_EVENTS = {
 		"accesskey",
 		"dir",
 		"disabled",
 		"lang",
 		"readonly",
 		"style",
 		"tabindex",
 		"title"
 	};
 
 	public static String[] TEXTAREA_ATTRS = {
 		"cols",
 		"rows"
 	};
 
 	public static String[] LINK_EVENTS = ArrayUtils.concat(COMMON_EVENTS, BLUR_FOCUS_EVENTS);
 	public static String[] BUTTON_EVENTS = ArrayUtils.concat(LINK_EVENTS, CHANGE_SELECT_EVENTS);
 	public static String[] IMG_ATTRS = ArrayUtils.concat(IMG_ATTRS_WITHOUT_EVENTS, COMMON_EVENTS);
 	public static String[] LINK_ATTRS = ArrayUtils.concat(LINK_ATTRS_WITHOUT_EVENTS, LINK_EVENTS);
 	public static String[] BUTTON_ATTRS = ArrayUtils.concat(BUTTON_ATTRS_WITHOUT_EVENTS, BUTTON_EVENTS);
 	public static final String[] INPUT_TEXT_ATTRS = ArrayUtils.concat(INPUT_TEXT_ATTRS_WITHOUT_EVENTS, COMMON_EVENTS, CHANGE_SELECT_EVENTS, BLUR_FOCUS_EVENTS);
     public static final String[] INPUT_TEXTAREA_ATTRS = ArrayUtils.concat(INPUT_TEXT_ATTRS, TEXTAREA_ATTRS);
     public static final String[] SELECT_ATTRS = ArrayUtils.concat(SELECT_ATTRS_WITHOUT_EVENTS, COMMON_EVENTS, CHANGE_SELECT_EVENTS, BLUR_FOCUS_EVENTS);
 }
