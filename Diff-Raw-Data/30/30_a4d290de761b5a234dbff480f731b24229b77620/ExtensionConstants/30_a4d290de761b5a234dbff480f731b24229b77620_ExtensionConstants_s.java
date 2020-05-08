 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension;
 
 import javax.faces.component.NamingContainer;
 
 /**
  * @author shot
  * @author manhole
  * @author yone
  */
 public interface ExtensionConstants {
 
     String XHTML_URI = "http://www.w3.org/1999/xhtml";
 
     //URI
     String TEEDA_EXTENSION_URI = "http://www.seasar.org/teeda/extension";
 
     //Elements
     String BODY_ELEM = "body";
 
     String TITLE_ELEM = "title";
 
     String INFO_MESSAGE_ELEM = "infoMessages";
 
     //Attributes
     String FRACTION_ATTR = "fraction";
 
     String GROUPING_SEPARATOR_ATTR = "groupingSeparator";
 
     String FRACTION_SEPARATOR_ATTR = "fractionSeparator";
 
     String VALIDATOR_STACK_ATTR = "javax.faces.webapp.VALIDATOR_STORE_ATTR";
 
     String CONVERTER_STACK_ATTR = "javax.faces.webapp.CONVERTER_STORE_ATTR";
 
     String PAGE_NAME_ATTR = "pageName";
 
     String ITEMS_NAME_ATTR = "itemsName";
 
     String LABEL_NAME_ATTR = "labelName";
 
     String ITEMS_ATTR = "items";
 
     String KEY_ATTR = "key";
 
     String PROPERTIES_NAME_ATTR = "propertiesName";
 
     String DEFAULT_PROPERTIES_NAME_ATTR = "defaultPropertiesName";
 
     String DEFAULT_KEY_ATTR = "defaultKey";
 
     String PATTERN_ATTR = "pattern";
 
     String LENGTH_ATTR = "length";
 
     String THRESHOD_ATTR = "threshold";
 
     String YEAR_ATTR = "year";
 
     String MONTH_ATTR = "month";
 
     String LAYOUT_ATTR = "layout";
 
     String CALENDAR_CLASS_ATTR = "calendarClass";
 
     String MEDIA_ATTR = "media";
 
     String DEFAULT_KEY = "defaultKey";
 
     String ERROR_STYLE_CLASS = "errorStyleClass";
 
     String COL_ATTR = "col";
 
     String SUBMITTED = "submitted";
 
     String TEMPLATEVALUE_ATTR = "templateValue";
 
     String RENDERJS_ATTR = "renderJs";
 
     String TIME_ATTR = "time";
 
     String INVISIBLE_ATTR = "invisible";
 
     String OMITTAG_ATTR = "omittag";
 
     String BASEVIEWID_ATTR = "baseViewId";
 
     //Suffix and prefix
     String GO_PREFIX = "go";
 
     String DO_PREFIX = "do";
 
     String JUMP_PREFIX = "jump";
 
     String LABEL_SUFFIX = "Label";
 
     String ITEMS_SUFFIX = "Items";
 
     String MESSAGE_SUFFIX = "Message";
 
     String FORM_SUFFIX = "Form";
 
     String FRACTION_SUFFIX = "Fraction";
 
     String GROUPING_SEPARATOR_SUFFIX = "GroupingSeparator";
 
     String FRACTION_SEPARATOR_SUFFIX = "FractionSeparator";
 
     String TEEDA_CURRENY_STYLE_CLASS = "T_currency";
 
     String TEEDA_DATE_STYLE_CLASS = "T_date";
 
     String SAVE_SUFFIX = "Save";
 
     String SESSION_SAVE_SUFFIX = "SessionSave";
 
     String MOCK_PREFIX = "mock";
 
     String TREESAVE_SUFFIX = "TreeSave";
 
     String GRID = "Grid";
 
     String GRID_X = "GridX";
 
     String GRID_Y = "GridY";
 
     String GRID_XY = "GridXY";
 
     String SCROLL_HORIZONTAL = "scrollHorizontal";
 
     String SCROLL_VERTICAL = "scrollVertical";
 
     String ASYNC = "async";
 
     String CALLBACK = "callback";
 
     String PATTERN_SUFFIX = "Pattern";
 
     String LENGTH_SUFFIX = "Length";
 
     String THRESHOLD_SUFFIX = "Threshold";
 
     String TEEDA_HIDDEN_SUFFIX = "TeedaHidden";
 
     String TAKE_OVER_SUFFIX = "_TAKE_OVER";
 
     String REDIRECT_SUFFIX = "_REDIRECT";
 
     String FACES_MESSAGE_AGGREGATION_SUFFIX = "_MESSAGE_AGGREGATION";
 
     String TEEDA_EXTENSION_MESSAGE_CLIENTIDS = "org.seasar.teeda.extension.message.clientids";
 
     //other
     String NAME_SEPARATOR = String.valueOf(NamingContainer.SEPARATOR_CHAR);
 
     String VALIDATION_ERROR_LINE_PREFIX = "(line : ";
 
     String VALIDATION_ERROR_LINE_SUFFIX = ")";
 
     String VALIDATION_ERROR_LINE_MESSAGE = "org.seasar.teeda.extension.component.TForEach.line";
 
     Integer REDIRECT_SCOPE = new Integer(1);
 
     Integer PAGE_SCOPE = new Integer(2);
 
     Integer SUBAPP_SCOPE = new Integer(3);
 
     String EXCEPTOION_PROPERTY = "exception";
 
     String DO_FINISH = "doFinish";
 
     String DO_ONCE_FINISH = "doOnceFinish";
 
     String TAKE_OVER_PARAMETER = "te-takeover";
 
     String REDIRECT_UNIQUE_KEY_PARAMETER = "te-uniquekey";
 
     String CONDITIONS_PARAMETER = "te-conditions";
 
     String REQUEST_ATTRIBUTE_UPLOADED_FILES = "org.seasar.teeda.core.filter.MultipartFormData.itemFiles";
 
     String FILE_UPLOAD_ERROR_MESSAGE = "org.seasar.teeda.extension.component.THtmlFileUpload.ERROR";
 
     String FILE_UPLOAD_SIZE_ERROR_MESSAGE = "org.seasar.teeda.extension.component.THtmlFileUpload.SIZE_ERROR";
 
     String FILE_UPLOAD_FILE_SIZE_ERROR_MESSAGE = "org.seasar.teeda.extension.component.THtmlFileUpload.FILE_SIZE_ERROR";
 
 }
