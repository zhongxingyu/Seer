 package org.effortless.gen.ui;
 
 import java.util.List;
 
 import org.codehaus.groovy.ast.ClassHelper;
 import org.codehaus.groovy.ast.ClassNode;
 import org.effortless.ann.HashPassword;
 import org.effortless.core.ClassNodeHelper;
 import org.effortless.core.StringUtils;
 import org.effortless.gen.GAnnotation;
 import org.effortless.gen.Transform;
 import org.effortless.gen.GClass;
 import org.effortless.gen.GField;
 import org.effortless.gen.GMethod;
 import org.effortless.gen.InfoModel;
 import org.effortless.model.CriteriaFilter;
 
 public class CreateFinderFilterTransform extends Object implements Transform<GClass> {
 
 	public CreateFinderFilterTransform () {
 		super();
 		initiate();
 	}
 	
 	protected void initiate () {
 		initiateResult();
 	}
 	
 	protected GClass result;
 	
 	protected void initiateResult () {
 		this.result = null;
 	}
 	
 	public GClass getResult () {
 		return this.result;
 	}
 	
 	protected void setResult (GClass newValue) {
 		this.result = newValue;
 	}
 	
 	@Override
 	public void process(GClass clazz) {
 		if (!clazz.hasAnnotation(FILTER_ANN)) {
 			String cName = getFinderFilterName(clazz);
 			GClass cg = new GClass(cName, clazz.getSourceUnit());
 			cg.setSuperClass(cg.createGenericType(CriteriaFilter.class, clazz.getClassNode()));
 			
 			List<GField> fields = InfoModel.getFinderProperties(clazz);
 			if (fields != null) {
 				for (GField field : fields) {
 					addFilterProperty(cg, field);
 				}
 			}
			
			GMethod mg = null;
 	
 	//		protected Class<?> doGetFilterClass () {
 	//			return Item.class;
 	//		}
 			mg = cg.addMethod("doGetFilterClass").setProtected(true).setReturnType(Class.class);
 			mg.addReturn(mg.clazz(clazz.getClassNode()));
 			
 	//		protected void setupConditions () {
 	//			super.setupConditions();
 	//			
 	//			this.lk("name", this.name);
 	//			if (this.name != null && this.name.trim().length() > 0) {
 	//				this.criteria.add(Restrictions.like("name", "%" + this.name + "%"));
 	//			}
 	//		}
 			mg = cg.addMethod("setupConditions").setProtected(true);
 			if (fields != null) {
 				for (GField field : fields) {
 	//				String fName = field.getName();
 					addFilterCondition(mg, field);
 	//				mg.add(mg.call("ilk", mg.cte(fName), mg.field(fName)));
 				}
 //				this.criteria.addOrder(org.hibernate.criterion.Order.desc("id"));
 //				mg.add(mg.call("orderDesc", mg.cte("id")));
 			}
 			mg.add(mg.call(mg.cteSuper(), "setupConditions"));
 
 //			protected void initiateOrderBy () {
 //			this.orderBy = null;
 //		}
 			mg = cg.addMethod("initiateOrderBy").setReturnType(ClassHelper.VOID_TYPE);
 			mg.add(mg.assign(mg.field("orderBy"), mg.cte("id DESC")));
 			
 			
 			clazz.addAnnotation(FILTER_ANN);
 			setResult(cg);
 		}
 	}
 	
 	public static final ClassNode FILTER_ANN = ClassNodeHelper.toClassNode(org.effortless.ann.Filter.class);
 	
 	public String getFinderFilterName (GClass clazz) {
 		String result = null;
 		result = clazz.getName() + "FinderFilter";
 		return result;
 	}
 	
 	public void addFilterProperty(GClass filter, GField field) {
 		if (field != null) {
 			if (field.isString()) {
 				GField filterField = addFilterPropertySimple(filter, field);
 				if (InfoModel.checkPassword(filterField)) {
 					GAnnotation ann = field.getAnnotation(HashPassword.class);
 					String hashAlgorithm = StringUtils.forceNotNull((ann != null ? ann.getValue() : null));
 					if (hashAlgorithm.length() > 0) {
 						filterField.addAnnotation(HashPassword.class, hashAlgorithm);
 					}
 				}
 			}
 			else if (field.isTime()) {
 				addFilterPropertySimple(filter, field);
 			}
 			else if (field.isTimestamp()) {
 				addFilterPropertySimple(filter, field);
 			}
 			else if (field.isDate()) {
 				addFilterPropertySimple(filter, field);
 			}
 			else if (field.isBoolean()) {
 				addFilterPropertyBooleanDouble(filter, field);
 			}
 			else if (field.isInteger()) {
 				addFilterPropertySimple(filter, field);
 			}
 			else if (field.isDouble()) {
 				addFilterPropertySimple(filter, field);
 			}
 			else if (field.isEnum()) {
 				addFilterPropertySimple(filter, field);
 			}
 			else if (field.isFile() || field.isType(org.effortless.model.FileEntity.class)) {
 				addFilterPropertySimple(filter, field);
 			}
 			else if (field.isCollection()|| field.isList()) {
 				addFilterPropertySimple(filter, field);
 			}
 			else {//REF
 				addFilterPropertySimple(filter, field);
 			}
 		}
 	}
 	
 	public GField addFilterPropertySimple (GClass filter, GField field) {
 		return addFilterPropertySimple(filter, field, field.getName());
 	}
 	
 	public void addFilterPropertyBooleanDouble (GClass filter, GField field) {
 		if (field != null) {
 			String fName = field.getName();
 			addFilterPropertySimple(filter, field, fName + "True");
 			addFilterPropertySimple(filter, field, fName + "False");
 		}
 	}
 	
 	protected GField addFilterPropertySimple (GClass filter, GField field, String name) {
 		GField result = null;
 		if (field != null && name != null) {
 			String fName = name;
 
 			filter.removeField(fName);
 //			field = this.addField(field.getType(), name);
 			
 			ClassNode fType = field.getType();
 //			String capfName = StringUtils.capFirst(fName);
 			
 			GField filterField = filter.addField(fType, fName);
 			result = filterField;
 			
 			GMethod mg = null;
 			
 			String initiateName = filterField.getInitiateName();//"initiate" + capfName;
 			mg = filter.addMethod(initiateName).setProtected(true);
 			mg.add(mg.assign(mg.field(fName), mg.cteNull()));
 			
 			String getterName = filterField.getGetterName();//"get" + capfName;
 			mg = filter.addMethod(getterName).setPublic(true).setReturnType(fType);
 //			mg.gPrintln("getting filter property (begin)");
 //			mg.gPrintln(mg.field(fName));
 //			mg.gPrintln("getting filter property (end)");
 			mg.addReturn(mg.field(fName));
 			
 			String setterName = filterField.getSetterName();//"set" + capfName;
 			mg = filter.addMethod(setterName).setPublic(true).addParameter(fType, "newValue");
 			mg.add(mg.call("_setProperty", mg.cte(fName), mg.field(fName), mg.assign(mg.field(fName), mg.var("newValue"))));
 		}
 		return result;
 	}
 
 	public void addFilterCondition(GMethod mg, GField field) {
 		if (mg != null && field != null) {
 			String fName = field.getName();
 			
 			if (field.isString()) {
 				mg.add(mg.call("ilk", mg.cte(fName), mg.field(fName)));
 			}
 			else if (field.isTime()) {
 				mg.add(mg.call("eq", mg.cte(fName), mg.field(fName)));
 			}
 			else if (field.isTimestamp()) {
 				mg.add(mg.call("eq", mg.cte(fName), mg.field(fName)));
 			}
 			else if (field.isDate()) {
 				mg.add(mg.call("eq", mg.cte(fName), mg.field(fName)));
 			}
 			else if (field.isBoolean()) {
 //				mg.add(mg.call("eq", mg.cte(fName), mg.field(fName)));
 				mg.add(mg.call("eqBooleanDouble", mg.cte(fName), mg.field(fName + "True"), mg.field(fName + "False")));
 			}
 			else if (field.isInteger()) {
 				mg.add(mg.call("eq", mg.cte(fName), mg.field(fName)));
 			}
 			else if (field.isDouble()) {
 				mg.add(mg.call("eq", mg.cte(fName), mg.field(fName)));
 			}
 			else if (field.isEnum()) {
 				mg.add(mg.call("eq", mg.cte(fName), mg.field(fName)));
 			}
 			else if (field.isFile() || field.isType(org.effortless.model.FileEntity.class)) {
 				mg.add(mg.call("eqFile", mg.cte(fName), mg.field(fName)));
 			}
 			else if (field.isCollection() || field.isList()) {
 				mg.add(mg.call("in", mg.cte(fName), mg.field(fName)));
 			}
 			else {//REF
 				mg.add(mg.call("eq", mg.cte(fName), mg.field(fName)));
 			}
 		}
 	}
 	
 }
