 /**
  * 
  */
 package com.k99k.khunter;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * KObj的一列
  * @author keel
  *
  */
 public class KObjColumn {
 	
 	private String col;
 	
 	private Object def;
 	
 	private int type;
 	
 	private String intro;
 	
 	private int len;
 	
 	private KObjColumnValidate validator;
 	
 	/**
 	 * 如此字段为ArrayList,则此值保存List的子KObjColumn
 	 */
 	private KObjColumn subColForList;
 	
 	/**
 	 * 如果此字段为HashMap,则此值保存子Map所有子KObjColumn
 	 */
 	private HashMap<String,KObjColumn> subColMap = new HashMap<String, KObjColumn>();
 	
 	/**
 	 * 如此字段为HashMap的子字段,则此值暂存key的name
 	 */
 	private String keyName;
 	
 	public KObjColumn(){
 		initKeyName();
 	}
 	
 	
 	/**
 	 * @param col 字段名
 	 * @param def 默认值
 	 * @param type 类型(0-6)
 	 * @param intro 说明
 	 * @param len 长度
 	 */
 	public KObjColumn(String col, Object def, int type, String intro, int len) {
 		super();
 		this.col = col;
 		this.def = def;
 		this.intro = intro;
 		this.len = len;
 		setType(type);
 		initKeyName();
 	}
 
 	/**
 	 * 字段类型
 	 * <pre>
 0 - String,
 1 - Integer,
 2 - HashMap,
 3 - ArrayList ,
 4 - Long,
 5 - Boolean,
 6 - Date,
 7 - Double
 	 </pre>
 	 */
 	public static final String[] KOBJ_COLUMN_TYPES = new String[]{
 		String.class.getName(),
 		Integer.class.getName(),
 		HashMap.class.getName(),
 		ArrayList.class.getName() ,
 		Long.class.getName(),
 		Boolean.class.getName(),
 		Date.class.getName(),
 		Double.class.getName()
 	};
 	
 	private static final HashMap<String,Object> emptyMap = new HashMap<String, Object>();
 	private static final ArrayList<Object> emptyList = new ArrayList<Object>();
 	private static final Date emptyDate = createEmptyDate();
 	
 	private static final Date createEmptyDate(){
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.YEAR, 2000);
 		c.set(Calendar.MONTH, 1);
 		c.set(Calendar.DATE, 1);
 		c.set(Calendar.HOUR_OF_DAY, 0);
 		c.set(Calendar.MINUTE, 0);
 		c.set(Calendar.SECOND, 0);
 		return c.getTime();
 	}
 	
 	/**
 	 * 各字段类型默认值
 	 * Date为:Tue Feb 01 00:00:00 CST 2000
 	 */
 	public static final Object[] KOBJ_COLUMN_EMPTY_VALUE = new Object[]{
 		"",
 		0,
 		emptyMap,
 		emptyList ,
 		0,
 		false,
 		emptyDate,//Tue Feb 01 00:00:00 CST 2000
 		0
 	};
 	
 	public static void main(String[] args) {
 		
 		for (int i = 0; i < KObjColumn.KOBJ_COLUMN_TYPES.length; i++) {
 			System.out.println(KObjColumn.KOBJ_COLUMN_TYPES[i]);
 		}
 		System.out.println("---------------");
 		String s = "abc";
 		int i = 3;
 		long l = 23L;
 		HashMap<String, String> m = new HashMap<String, String>();
 		ArrayList<String> al = new ArrayList<String>();
 		Date d = new Date();
 		Boolean b = true;
 		double f = 23.5;
 		System.out.println("s :"+checkColType(s, 0));
 		System.out.println("s :"+checkColType(s, 1));
 		System.out.println("i :"+checkColType(i, 1));
 		System.out.println("l :"+checkColType(l, 4));
 		System.out.println("m :"+checkColType(m, 2));
 		System.out.println("al :"+checkColType(al, 3));
 		System.out.println("b :"+checkColType(b, 5));
 		System.out.println("d :"+checkColType(d, 6));
 		System.out.println("f :"+checkColType(f, 7));
 		
 		System.out.println("emptyDate:"+KOBJ_COLUMN_EMPTY_VALUE[6]);
 		
 		System.out.println("---------------");
 		String col = "comms.*.tags";
 		int lastDotPosi = col.lastIndexOf('.');
 		if (lastDotPosi >=0) {
 			String pre = col.substring(0,lastDotPosi);
 			String subKey = col.substring(lastDotPosi+1);
 			System.out.println(pre);
 			System.out.println(subKey);
 		}
 		
 	}
 	
 	/**
 	 * 验证字段类型
 	 * @param columData 注意：为null时不验证
 	 * @param type KOBJ_COLUMN_TYPES下标
 	 * @return
 	 */
 	public static final boolean checkColType(Object columnData,int type){
 		if (columnData == null) {
 			return true;
 		}
 		if (!checkType(type)) {
 			return false;
 		}
 		String clazzName = columnData.getClass().getName();
 		if (clazzName.equals(KOBJ_COLUMN_TYPES[type])) {
 			return true;
 		}
 		//System.out.print("col:"+columnData.getClass().getName()+" type:"+KOBJ_COLUMN_TYPES[type]+" - ");
 		return false;
 	}
 	
 	/**
 	 * 获取子KObjColumn
 	 * @return ArrayList<KObjColumn>
 	 */
 	public ArrayList<KObjColumn> getSubColumns(){
 		ArrayList<KObjColumn> list = null;
 		if (this.type == 2) {
 			if (this.subColMap == null) {
 				return null;
 			}
 			list = new ArrayList<KObjColumn>();
 			for (Iterator<String> it = this.subColMap.keySet().iterator(); it.hasNext();) {
 				String key = it.next();
 				list.add(this.subColMap.get(key));
 			}
 		}else if(this.type == 3){
 			if (this.subColForList == null) {
 				return null;
 			}
 			list = new ArrayList<KObjColumn>();
 			list.add(this.subColForList);
 		}else{
 			return null;
 		}
 		return list;
 	}
 	
 	/**
 	 * 验证本字段,如果有子字段则轮循验证子字段
 	 * @param columnData
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean validateColumn(Object columnData){
 		if (columnData == null) {
 			return false;
 		}
 		//验证类型
 		if (columnData.getClass().getName().equals(KOBJ_COLUMN_TYPES[this.type])) {
 			//验证子字段为HashMap时
 			if (this.type == 2) {
 				if (this.subColMap==null) {
 					return false;
 				}
 				HashMap<String,Object> m = (HashMap<String,Object>)columnData;
 				for (Iterator<String> iterator = m.keySet().iterator(); iterator
 						.hasNext();) {
 					String key = iterator.next();
 					KObjColumn sub = this.subColMap.get(key);
 					if (sub == null || !sub.validateColumn(key, m.get(key))) {
 						return false;
 					}
 				}
 			}
 			//子字段为ArrayList时
 			else if (this.type == 3) {
 				if (this.subColForList == null) {
 					return false;
 				}
 				ArrayList<Object> al = (ArrayList<Object>)columnData;
 				for (Iterator<Object> iterator = al.iterator(); iterator.hasNext();) {
 					Object obj = iterator.next();
 					if (!this.subColForList.validateColumn(obj)) {
 						return false;
 					}
 				}
 			}
 			//非父字段,调用KObjColumnValidate接口
 			else if(this.validator != null && !this.validator.validate(columnData)) {
 				return false;
 			}
 		}else{
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * 验证本字段,如果有子字段则轮循验证子字段,同时设置KObject字段
 	 * @param columnData
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean validateAndSet(Object columnData,KObject kobj){
 		if (columnData == null) {
 			return false;
 		}
 		//处理Long
 		if (this.type == 1 && columnData.getClass().getName().equals(KOBJ_COLUMN_TYPES[4])) {
 			columnData = Integer.parseInt(columnData.toString());
 		};
 		//验证类型
 		if (columnData.getClass().getName().equals(KOBJ_COLUMN_TYPES[this.type])) {
 			//验证子字段为HashMap时
 			if (this.type == 2) {
				if (this.subColMap==null || (kobj.getProp(keyName) == null) || (!kobj.getProp(keyName).getClass().getName().equals(HashMap.class.getName()))) {
 					return false;
 				}
 				HashMap<String,Object> m = (HashMap<String,Object>)columnData;
 				HashMap<Object,Object> koc = (HashMap<Object, Object>) kobj.getProp(keyName);
 				for (Iterator<String> iterator = m.keySet().iterator(); iterator
 						.hasNext();) {
 					String key = iterator.next();
 					KObjColumn sub = this.subColMap.get(key);
 					Object val = m.get(key);
 					if (sub == null || !sub.validateColumn(key, val)) {
 						return false;
 					}
 					koc.put(key, val);
 				}
 			}
 			//子字段为ArrayList时
 			else if (this.type == 3) {
				if (this.subColForList == null || (kobj.getProp(keyName) == null) || (!kobj.getProp(keyName).getClass().getName().equals(ArrayList.class.getName()))) {
 					return false;
 				}
 				ArrayList<Object> al = (ArrayList<Object>)columnData;
 				ArrayList<Object> koc = (ArrayList<Object>)kobj.getProp(this.keyName);
 				for (Iterator<Object> iterator = al.iterator(); iterator.hasNext();) {
 					Object obj = iterator.next();
 					if (!this.subColForList.validateColumn(obj)) {
 						return false;
 					}
 					koc.add(obj);
 				}
 			}
 			//非父字段,调用KObjColumnValidate接口
 			else if(this.validator != null && !this.validator.validate(columnData)) {
 				return false;
 			}
 			//单个属性直接设置
 			kobj.setProp(this.keyName, columnData);
 		}else{
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * 当此字段为HashMap子字段时验证其key和value,如果有子字段则轮循验证子字段
 	 * @param key
 	 * @param value
 	 * @return
 	 */
 	public boolean validateColumn(String key,Object value){
 		if (key == null || value == null) {
 			return false;
 		}
 		//验证key
 		if (key.equals(this.keyName)) {
 			//验证值
 			return this.validateColumn(value);
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * 验证key,当此字段为Map的子字段时
 	 * @param key
 	 * @return
 	 */
 	private final void initKeyName(){
 		int subPo = this.col.lastIndexOf('.');
 		if (subPo>=0) {
 			this.keyName = this.col.substring(subPo+1);
 		}else{
 			this.keyName = this.col;
 		}
 	}
 	
 	/**
 	 * 如此字段为HashMap,此方法用于设置其子字段KObjColumn
 	 * @param key
 	 */
 	public void setSubColumn(String key,KObjColumn kobjCol){
 		this.subColMap.put(key, kobjCol);
 	}
 	
 	/**
 	 * 如此字段为ArrayList,设置子字段KObjColumn
 	 * @param subColForList
 	 */
 	public void setSubColumn(KObjColumn subColForList){
 		this.subColForList = subColForList;
 	}
 	
 	/**
 	 * 验证type值是否合法
 	 * @param type 值：0-7
 	 * @return
 	 */
 	public static final boolean checkType(int type){
 		if (type>7 || type <0){
 			return false;
 		}
 		return true;
 	}
 	
 //	/**
 //	 * 设置
 //	 * @param columnData
 //	 * @return
 //	 */
 //	public boolean setColumn(Map<String,Object> columnData ){
 //		
 //		return true;
 //	}
 
 
 	/**
 	 * @return the col
 	 */
 	public final String getCol() {
 		return col;
 	}
 
 
 	/**
 	 * @return the validator
 	 */
 	public final KObjColumnValidate getValidator() {
 		return validator;
 	}
 
 
 	/**
 	 * @param validator the validator to set
 	 */
 	public final void setValidator(KObjColumnValidate validator) {
 		this.validator = validator;
 	}
 	
 	/**
 	 * 用字符串设置Validator
 	 * @param validatorConfig 如"com.k99k.khunter.StringValidator,0,5"为class+type+paras
 	 * @return
 	 */
 	public final boolean setValidator(String validatorConfig){
 		if (validatorConfig == null || validatorConfig.indexOf(',')<0) {
 			return false;
 		}
 		try {
 			String[] vaArr = validatorConfig.split(",");
 			String vClass = vaArr[0];
 			int vType = Integer.parseInt(vaArr[1]);
 			String[] vParas = null;
 			if (vaArr.length > 2) {
 				vParas = new String[vaArr.length-2];
 				for (int j = 0; j < vaArr.length-2; j++) {
 					vParas[j] = vaArr[j+2];
 				}
 			}
 			Object v = KIoc.loadClassInstance("file:/"+HTManager.getClassPath(), vClass);
 			if (v instanceof KObjColumnValidate) {
 				KObjColumnValidate validator = (KObjColumnValidate)v;
 				//设置参数并初始化
 				validator.initType(vType, vParas);
 				this.setValidator(validator);
 				return true;
 			}else{
 				return false;
 			}
 		} catch (Exception e) {
 			ErrorCode.logError(KObjSchema.log, 8, 9, e, " --in KObjColumn setValidator");
 			return false;
 		}
 	}
 	
 	public final String getValidatorString(){
 		if (this.validator == null) {
 			return "";
 		}
 		return this.validator.toString();
 	}
 
 
 	/**
 	 * @param col the col to set
 	 */
 	public final void setCol(String col) {
 		this.col = col;
 		initKeyName();
 	}
 
 
 	/**
 	 * @return the def
 	 */
 	public final Object getDef() {
 		return def;
 	}
 
 
 	/**
 	 * @param def the def to set
 	 */
 	public final void setDef(Object def) {
 		this.def = def;
 	}
 
 
 
 
 	/**
 	 * @return the type
 	 */
 	public final int getType() {
 		return type;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public final void setType(int type) {
 		if (!checkType(type)){
 			throw new RuntimeException("column type error!");
 		}
 		this.type = type;
 	}
 
 	/**
 	 * @return the intro
 	 */
 	public final String getIntro() {
 		return intro;
 	}
 
 
 	/**
 	 * @param intro the intro to set
 	 */
 	public final void setIntro(String intro) {
 		this.intro = intro;
 	}
 
 
 	/**
 	 * @return the len
 	 */
 	public final int getLen() {
 		return len;
 	}
 
 
 	/**
 	 * @param len the len to set
 	 */
 	public final void setLen(int len) {
 		this.len = len;
 	}
 
 	/**
 	 * toMap用于生成配置文件
 	 * @return
 	 */
 	public final HashMap<String,Object> toMap(){
 		HashMap<String,Object> m = new HashMap<String, Object>();
 		m.put("col", this.col);
 		m.put("def", this.def);
 		m.put("intro", this.intro);
 		m.put("type", this.type);
 		m.put("len", this.len);
 		if (this.validator != null) {
 			m.put("validator", this.validator.toString());
 		}
 		return m;
 	}
 	
 }
