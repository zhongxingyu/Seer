 package com.java_podio.code_gen;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.podio.item.FieldValuesUpdate;
 import com.podio.item.FieldValuesView;
 import com.podio.item.Item;
 import com.podio.item.ItemCreate;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JClassAlreadyExistsException;
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JConditional;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JFieldVar;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JMod;
 import com.sun.codemodel.JPackage;
 import com.sun.codemodel.JVar;
 import com.sun.codemodel.JWhileLoop;
 
 /**
  * Generates a (single) AppWrapper abstract class - the super class of all app
  * wrapper classes.
  * 
  * @see AppWrapper
  */
 public class AppWrapperGenerator {
 
 	private JCodeModel jc;
 
 	private JMember _originalItem;
 
 	private JMember _podioId;
 
 	private JMember _podioRevision;
 
 	private JMember _podioTitle;
 
 	private JMethod _setValue;
 
 	private JDefinedClass appWrapper;
 
 	private JFieldVar _PODIO_DATE_TIME_FORMATTER;
 
 	private JMethod getAppExternalId;
 
 	private JMethod getAppId;
 
 	private JMethod _getFieldValuesUpdateFromDate;
 
 	private JMethod _getFieldValuesUpdateFromApp;
 
 	private JFieldVar _PODIO_DATE_FORMATTER;
 
 	private JMethod _parseDate;
 
 	private JMethod _parseAppField;
 
 	private JMethod _getItemCreate;
 
 	private JMember _podioTags;
 
 	public AppWrapperGenerator(JCodeModel jCodeModel, JPackage jp) throws JClassAlreadyExistsException {
 		this.jc = jCodeModel;
 		appWrapper = jp != null ? jp._class(JMod.ABSTRACT | JMod.PUBLIC, "AppWrapper") : jc._class("AppWrapper");
 
 		// assure all relevant fields/methods are actually created:
 		_setValue();
 		_getItemCreate();
 		_getAppExternalId();
 		_getAppId();
 		_originalItem();
 		_podioId();
 		_podioTitle();
 		_podioTags();
 
 		CodeGenerator.addToString(appWrapper, jc, false);
 	}
 
 	/**
 	 * Generates a/the AppWrapper class.<br>
 	 * On subsequent calls on the same instance, the same object is returned!
 	 * 
 	 * @return
 	 * @throws JClassAlreadyExistsException
 	 * @see {@link AppWrapper}
 	 */
 	public JDefinedClass getAppWrapperClass() {
 		return appWrapper;
 	}
 
 	public JMethod _setValue() {
 		if (_setValue == null) {
 			_setValue = appWrapper.method(JMod.PUBLIC, jc.VOID, "setValue")._throws(ParseException.class);
 			_setValue.javadoc().add(
 					"Fills this objects values from {@code item}.<br>Subclasses should extend this method!");
 			JVar item = _setValue.param(Item.class, "item");
 			_setValue.javadoc().addParam(item);
 			_setValue.javadoc().addThrows(ParseException.class);
 			_setValue.body().invoke(_originalItem().getSetter()).arg(item);
 			_setValue.body().assign(_podioId().getField(), item.invoke("getId"));
 			_setValue.body()._if(item.invoke("getCurrentRevision").ne(JExpr._null()))._then()
 					.assign(_podioRevision().getField(), item.invoke("getCurrentRevision").invoke("getRevision"));
 			_setValue.body().assign(_podioTitle().getField(), item.invoke("getTitle"));
 			_setValue.body().assign(_podioTags().getField(), item.invoke("getTags"));
 		}
 		return _setValue;
 	}
 
 	public JMember _originalItem() {
 		if (_originalItem == null) {
 			_originalItem = CodeGenerator.addMember(appWrapper, "OriginalItem", jc.ref(Item.class),
 					"Stores the original item, as retrieved by java-podio api.", jc);
 		}
 		return _originalItem;
 	}
 
 	public JMember _podioId() {
 		if (_podioId == null) {
 			_podioId = CodeGenerator.addMember(appWrapper, "PodioId", jc.ref(Integer.class),
 					"This represents the internal Podio id of the item.", jc);
 		}
 		return _podioId;
 	}
 
 	public JMember _podioRevision() {
 		if (_podioRevision == null) {
 			_podioRevision = CodeGenerator.addMember(appWrapper, "PodioRevision", jc.ref(Integer.class),
 					"This represents the internal Podio revision of the item.", jc);
 		}
 		return _podioRevision;
 	}
 
 	public JMember _podioTitle() {
 		if (_podioTitle == null) {
 			_podioTitle = CodeGenerator.addMember(appWrapper, "PodioTitle", jc.ref(String.class),
 					"This represents the Podio title of the item.", jc);
 		}
 		return _podioTitle;
 	}
 
 	public JFieldVar _PODIO_DATE_TIME_FORMATTER() {
 		if (_PODIO_DATE_TIME_FORMATTER == null) {
 			_PODIO_DATE_TIME_FORMATTER = appWrapper.field(JMod.PROTECTED | JMod.STATIC | JMod.FINAL,
 					SimpleDateFormat.class, "PODIO_DATE_TIME_FORMATTER",
 					JExpr.direct("new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\")"));
 
 		}
 		return _PODIO_DATE_TIME_FORMATTER;
 	}
 
 	public JFieldVar _PODIO_DATE_FORMATTER() {
 		if (_PODIO_DATE_FORMATTER == null) {
 			_PODIO_DATE_FORMATTER = appWrapper.field(JMod.PROTECTED | JMod.STATIC | JMod.FINAL, SimpleDateFormat.class,
 					"PODIO_DATE_FORMATTER", JExpr.direct("new SimpleDateFormat(\"yyyy-MM-dd\")"));
 
 		}
 		return _PODIO_DATE_FORMATTER;
 	}
 
 	public JMethod _getAppExternalId() {
 		if (getAppExternalId == null) {
 			getAppExternalId = appWrapper.method(JMod.ABSTRACT | JMod.PUBLIC, String.class, "getAppExternalId");
 		}
 		return getAppExternalId;
 	}
 
 	public JMethod _getAppId() throws JClassAlreadyExistsException {
 		if (getAppId == null) {
 			getAppId = appWrapper.method(JMod.PUBLIC | JMod.ABSTRACT, Integer.class, "getAppId");
 		}
 		return getAppId;
 	}
 
 	public JMember _podioTags() {
 		if (_podioTags == null) {
 			_podioTags = CodeGenerator.addMember(appWrapper, "PodioTags", jc.ref(List.class).narrow(String.class),
 					"This represents the Podio tags of the item.", jc);
 		}
 		return _podioTags;
 	}
 
 	public JMethod _getItemCreate() {
 		if (_getItemCreate == null) {
 			_getItemCreate = appWrapper.method(JMod.PUBLIC, ItemCreate.class, "getItemCreate");
 			_getItemCreate
 					.javadoc()
 					.add("As {@link ItemCreate} inherits from {@link ItemUpdate} this method can be used to generate updates!");
 			JVar _itemCreateResult = _getItemCreate.body().decl(jc.ref(ItemCreate.class), "result",
 					JExpr._new(jc.ref(ItemCreate.class)));
 			_getItemCreate.body().add(_itemCreateResult.invoke("setExternalId").arg(JExpr.invoke(_getAppExternalId())));
 			_getItemCreate.body().add(
 					_itemCreateResult.invoke("setRevision").arg(JExpr.invoke(_podioRevision().getGetter())));
 			// _getItemCreate.body()
 			// .add(_itemCreateResult.invoke("setTitle").arg(JExpr.invoke(_podioTitle().getGetter())));
 			_getItemCreate.body().add(_itemCreateResult.invoke("setTags").arg(JExpr.invoke(_podioTags().getGetter())));
 			JVar fieldValuesList = _getItemCreate.body().decl(jc.ref(List.class).narrow(FieldValuesUpdate.class),
 					"fieldValuesList", JExpr._new(jc.ref(ArrayList.class).narrow(FieldValuesUpdate.class)));
 			_getItemCreate.body().add(_itemCreateResult.invoke("setFields").arg(fieldValuesList));
 			_getItemCreate.body()._return(_itemCreateResult);
 		}
 		return _getItemCreate;
 	}
 
 	public JMethod _getFieldValuesUpdateFromApp() {
 		// fieldValuesList.add(new FieldValuesUpdate("extid", "value",
 		// Collections.singletonMap("item_id", getKunde())));
 		// List<Map<String, Map<String, Integer>>>
 		if (_getFieldValuesUpdateFromApp == null) {
 			_getFieldValuesUpdateFromApp = appWrapper.method(JMod.PUBLIC | JMod.STATIC, FieldValuesUpdate.class,
 					"getFieldValuesUpdateFromApp");
 			JVar ids = _getFieldValuesUpdateFromApp.param(jc.ref(List.class).narrow(Integer.class), "ids");
 			JVar externalId = _getFieldValuesUpdateFromApp.param(String.class, "externalId");
 
 			_getFieldValuesUpdateFromApp.body()._if(ids.eq(JExpr._null()))._then()._return(JExpr._null());
 
 			JClass valueType = jc.ref(Map.class).narrow(jc.ref(String.class), jc.ref(Object.class).wildcard());
 
 			JVar idsList = _getFieldValuesUpdateFromApp.body().decl(jc.ref(ArrayList.class).narrow(valueType),
 					"values", JExpr._new(jc.ref(ArrayList.class).narrow(valueType)));
 
 			_getFieldValuesUpdateFromApp.body().directStatement(
 					"for(Integer id : " + ids.name() + "){\n" + idsList.name()
							+ ".add(java.util.Collections.singletonMap(\"value\", id));\n}");
 
 			_getFieldValuesUpdateFromApp.body()._return(
 					JExpr._new(jc.ref(FieldValuesUpdate.class)).arg(externalId).arg(idsList));
 		}
 		return _getFieldValuesUpdateFromApp;
 	}
 
 	public JMethod _getFieldValuesUpdateFromDate() {
 		if (_getFieldValuesUpdateFromDate == null) {
 			_getFieldValuesUpdateFromDate = appWrapper.method(JMod.PUBLIC | JMod.STATIC, FieldValuesUpdate.class,
 					"getFieldValuesUpdateFromDate");
 			JVar date = _getFieldValuesUpdateFromDate.param(Date.class, "date");
 			JVar externalId = _getFieldValuesUpdateFromDate.param(String.class, "externalId");
 
 			JVar dateHashMap = _getFieldValuesUpdateFromDate.body().decl(
 					jc.ref(HashMap.class).narrow(String.class, String.class), "dateHashMap",
 					JExpr._new(jc.ref(HashMap.class).narrow(String.class, String.class)));
 			_getFieldValuesUpdateFromDate.body()
 					.add(dateHashMap.invoke("put").arg("start")
 							.arg(_PODIO_DATE_TIME_FORMATTER().invoke("format").arg(date)));
 			_getFieldValuesUpdateFromDate.body().add(
 					dateHashMap.invoke("put").arg("end").arg(_PODIO_DATE_TIME_FORMATTER().invoke("format").arg(date)));
 			_getFieldValuesUpdateFromDate.body()._return(
 					JExpr._new(jc.ref(FieldValuesUpdate.class)).arg(externalId).arg(dateHashMap));
 		}
 		return _getFieldValuesUpdateFromDate;
 	}
 
 	public JMethod _parseDate() {
 		if (_parseDate == null) {
 			_parseDate = appWrapper.method(JMod.PUBLIC | JMod.STATIC, jc.ref(Date.class), "parseDate")._throws(
 					jc.ref(ParseException.class));
 			JVar formatDateParam = _parseDate.param(jc.ref(String.class), "dateOrDateTime");
 			JConditional cond = _parseDate.body()._if(formatDateParam.invoke("length").lte(JExpr.lit(10)));
 			cond._then()._return(_PODIO_DATE_FORMATTER().invoke("parse").arg(formatDateParam));
 			cond._else()._return(_PODIO_DATE_TIME_FORMATTER().invoke("parse").arg(formatDateParam));
 		}
 		return _parseDate;
 	}
 
 	public JMethod _parseAppField() {
 		// return
 		// JExpr.direct("((java.util.Map<String, java.util.Map<String, Integer>>) "
 		// + jVar.name()
 		// + ".getValues().get(0)).get(\"value\").get(\"item_id\")");
 		if (_parseAppField == null) {
 			_parseAppField = appWrapper.method(JMod.PUBLIC | JMod.STATIC, jc.ref(List.class).narrow(Integer.class),
 					"parseAppField")._throws(jc.ref(ParseException.class));
 			_parseAppField.javadoc().addReturn().add("a list of referenced item ids");
 			JVar parseAppParam = _parseAppField.param(jc.ref(FieldValuesView.class), "fieldValue");
 			JVar result = _parseAppField.body().decl(jc.ref(List.class).narrow(Integer.class), "result",
 					JExpr._new(jc.ref(ArrayList.class).narrow(Integer.class)));
 			JClass entryType = jc.ref(Map.class).narrow(jc.ref(String.class), jc.ref(Object.class).wildcard());
 			JClass entriesType = jc.ref(List.class).narrow(entryType);
 			JVar entries = _parseAppField.body().decl(entriesType, "entries");
 			_parseAppField.body().assign(entries, JExpr.cast(entriesType, parseAppParam.invoke("getValues")));
 			JVar iJVar = _parseAppField.body().decl(jc.ref(Iterator.class).narrow(entryType), "iterator",
 					entries.invoke("iterator"));
 			JWhileLoop loop = _parseAppField.body()._while(iJVar.invoke("hasNext"));
 			JVar value = loop.body().decl(
 					jc.ref(Integer.class),
 					"value",
 					JExpr.cast(jc.ref(Integer.class),
 							JExpr.invoke(JExpr.cast(entryType, iJVar.invoke("next").invoke("get").arg("value")), "get")
 									.arg("item_id")));
 			// JExpr.cast(entryType,
 			// JExpr.invoke(iJVar,"next")/*.invoke("get").arg("value")*/).invoke("get").arg("item_id"));
 			loop.body()._if(value.ne(JExpr._null()))._then()
 					.add(result.invoke("add").arg(value));
 
 			_parseAppField.body()._return(result);
 		}
 		return _parseAppField;
 	}
 }
