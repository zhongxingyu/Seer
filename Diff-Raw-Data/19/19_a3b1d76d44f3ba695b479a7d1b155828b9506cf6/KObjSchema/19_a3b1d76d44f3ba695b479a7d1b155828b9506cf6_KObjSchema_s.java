 /**
  * 
  */
 package com.k99k.khunter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 
 import com.k99k.tools.JSONTool;
 import com.k99k.tools.StringUtil;
 
 /**
  * KObj与Dao结合使用的schema，用于产生空的KObj和填充及设置KObj属性
  * @author keel
  *
  */
 public class KObjSchema {
 	
 	static final Logger log = Logger.getLogger(KObjSchema.class);
 	
 	/**
 	 * 对应的kobjName
 	 */
 	private String kobjName;
 	/**
 	 * 保存schema的ArrayList,用于验证字段
 	 */
 	private final ArrayList<KObjColumn> columnList = new ArrayList<KObjColumn>();
 	
 	/**
 	 * 保存schema的HashMap,用于获取某一字段
 	 */
 	private final HashMap<String,KObjColumn> columnMap = new HashMap<String, KObjColumn>();
 	
 	/**
 	 * 保存KObjIndex的map
 	 */
 	private final HashMap<String,KObjIndex> indexMap = new HashMap<String, KObjIndex>();
 	
 	private int columnSize = 0;
 	
 	/**
 	 * 初始化Schema,注意父column定义必须先于子column,无数据库操作
 	 * @param columnDefineList
 	 * @param indexDefineList
 	 * @return 是否初始化成功
 	 */
 	public boolean initSchema(String kobjName,ArrayList<HashMap<String,Object>> columnDefineList,ArrayList<HashMap<String,Object>> indexDefineList){
 		this.kobjName = kobjName;
 		int i = 0;
 		for (Iterator<HashMap<String, Object>> iterator = columnDefineList.iterator(); iterator.hasNext();) {
 			HashMap<String, Object> map = iterator.next();
 			int setRe = this.setColumn(map);
 			if (setRe != 0) {
 				ErrorCode.logError(log, 8,setRe, " in initSchema index-"+i+" kobjName:"+kobjName);
 				return false;
 			}
 			i++;
 		}
 		this.columnSize = this.columnList.size();
 		
 		//---------------------------
 		//初始化index
 		i = 0;
 		for (Iterator<HashMap<String, Object>> iterator = indexDefineList.iterator(); iterator.hasNext();) {
 			HashMap<String, Object> map = iterator.next();
 			int setRe = setIndex(map,false);
 			if (setRe != 0) {
 				ErrorCode.logError(log, 8, setRe,  " in initSchema index-"+i+" kobjName:"+kobjName);
 				return false;
 			}
 			i++;
 		}
 		return true;
 	}
 	
 	/**
 	 * FIXME 更新操作
 	 * @param opType
 	 * @return
 	 */
 	public int updateSchema(int opType,Object obj){
 		
 		
 		return 0;
 	}
 
 	/**
 	 * 用HashMap生成一个KObjColumn并进行新增或更新
 	 * @param colMap HashMap<String,Object>
 	 * @return
 	 */
 	public int setColumn(HashMap<String,Object> colMap){
 		try {
 			if (colMap == null) {
 				return 1;
 			}
 			//验证Column的key和value类型
 			if(!JSONTool.checkMapTypes(colMap,new String[]{"col","def","type","intro","len"},new Class[]{String.class,Object.class,Long.class,String.class,Long.class})){
 				//ErrorCode.logError(log, 8,1, "kobjColumn:"+i);
 				return 1;
 			}
 			
 			String col = (String) colMap.get("col");
 			Object def = colMap.get("def");
 			int type = Integer.parseInt(colMap.get("type").toString());
 			String intro = (String) colMap.get("intro");
 			int len = Integer.parseInt(colMap.get("len").toString());
 			//int类型将进行Long转换处理
 			if (type == 1) {
 				if (!StringUtil.isDigits(def.toString())) {
 					return 2;
 				}
 				def = Integer.parseInt(def.toString());
 			}
 			
 			//验证type和def
 			if (!KObjColumn.checkType(type) || !KObjColumn.checkColType(def, type)) {
 				ErrorCode.logError(log, 8,2, " type:"+type+" default:"+def);
 				return 2;
 			}
 			KObjColumn  k = new KObjColumn(col,def,type,intro,len);	
 			//设置验证器
 			if (colMap.containsKey("validator")) {
 				Object o = colMap.get("validator");
 				if (o instanceof String) {
 				}else{
 					//ErrorCode.logError(log, 8,5, "index-"+i+" validator is not String:"+o);
 					return 5;
 				}
 				//"com.k99k.khunter.StringValidator,0,5"为class+type+paras
 				String s = o.toString();
 				if (StringUtil.isStringWithLen(s, 3)) {
 					if (!k.setValidator(o.toString())) {
 						return 5;
 					}
 				}
 			}
 			//父column必须先定义
 			KObjColumn kc = this.columnMap.put(col, k);
 			boolean isAdd = (kc==null) ? true :false;
 			int setsub = this.setSubColumn(k,isAdd);
 			if (setsub != 0) {
 				return setsub;
 			}
 		} catch (Exception e) {
 			return 11;
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * 处理子Column的情况
 	 * @param kc KObjColumn
 	 * @param isAdd 
 	 */
 	private final int setSubColumn(KObjColumn kc,boolean isAdd){
 		String col = kc.getCol();
 		if (col.indexOf('.') > 0) {
 			//获取父Schema
 			int lastDotPosi = col.lastIndexOf('.');
 			String pre = col.substring(0,lastDotPosi);
 			String subKey = col.substring(lastDotPosi+1);
 			KObjColumn preK = this.columnMap.get(pre);
 			if (preK == null) {
 				//ErrorCode.logError(log, 8,3, "index-"+i+" col:"+col);
 				return 3;
 			}
 			if (preK.getType() == 2) {
 				//父Schema为HashMap
 				preK.setSubColumn(subKey, kc);
 			}else if(preK.getType() == 3 && subKey.equals("*")){
 				//父Schema为ArrayList
 				preK.setSubColumn(kc);
 			}else{
 				//ErrorCode.logError(log, 8,4, "index-"+i+" col:"+col);
 				return 4;
 			}
 			
 		}else{
 			if (isAdd) {
 				columnList.add(kc);
 			}else{
 				int i = 0;
 				for (Iterator<KObjColumn> it = columnList.iterator(); it
 						.hasNext();) {
 					KObjColumn kcol = it.next();
 					if (kcol.getCol().equals(kc.getCol())) {
 						columnList.remove(i);
 						columnList.add(i, kc);
 						break;
 					}
 					i++;
 				}
 			}
 		}
 		return 0;
 	}
 	
 	/**
 	 * 添加或新增KObjColumn,如果是子字段则处理子字段
 	 * @param col
 	 * @param 0为成功
 	 */
 	public int setColumn(KObjColumn col){
 		KObjColumn k =this.columnMap.put(col.getCol(), col);
 		return this.setSubColumn(col,(k==null));
 	}
 	
 	public void removeColumn(String key){
		this.columnMap.remove(key);
 	}
 	
 	public boolean containsColumn(String key){
 		return this.columnMap.containsKey(key);
 	}
 	
 	/**
 	 * 添加或新增KObjIndex,不在数据库中操作
 	 * @param index
 	 */
 	public void setIndex(KObjIndex index){
 		this.indexMap.put(index.getCol(), index);
 	}
 	
 	/**
 	 * 添加或新增KObjIndex,同时在数据库中同步
 	 * @param iMap map形式的KObjIndex
 	 * @param synDB 是否同步到数据库
 	 * @return 0为成功
 	 */
 	public int setIndex(HashMap<String,Object> iMap,boolean synDB){
 		int err = 10;
 		try {
 			if(!JSONTool.checkMapTypes(iMap,new String[]{"col","asc","intro","type","unique"},new Class[]{String.class,Boolean.class,String.class,String.class,Boolean.class})){
 				//ErrorCode.logError(log, 8,1, " kobjIndex:"+i);
 				return 10;
 			}
 			String col = (String) iMap.get("col");
 			boolean asc = (Boolean) iMap.get("asc");
 			String intro = (String) iMap.get("intro");
 			String type = (String) iMap.get("type");
 			boolean unique = (Boolean) iMap.get("unique");
 			
 			KObjIndex ki = new KObjIndex(col, asc, type, intro, unique);
 			err = 25;
 			//更新到数据库
 			if (synDB) {
 				if(KObjManager.findKObjConfig(this.kobjName).getDaoConfig().findDao().updateIndex(ki)){
 					this.indexMap.put(col, ki);
 				}else{
 					return 25;
 				}
 			}else{
 				this.indexMap.put(col, ki);
 			}
 			
 		} catch (Exception e) {
 			return err;
 		}
 		return 0;
 	}
 	
 	/**
 	 * 应用索引到数据库
 	 * @param indx
 	 * @return
 	 */
 	public final boolean applyIndex(KObjIndex indx){
 		DaoInterface dao = KObjManager.findKObjConfig(this.kobjName).getDaoConfig().findDao();
 		if (dao == null) {
 			return false;
 		}
 		if(!dao.updateIndex(indx)){
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * 应用全部索引到数据库
 	 * @return
 	 */
 	public final int applyIndexes(){
 		for (Iterator<String> it = this.indexMap.keySet().iterator(); it.hasNext();) {
 			String indx = it.next();
 			if (!this.applyIndex(this.indexMap.get(indx))) {
 				return 17;
 			}
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * 设置KObjIndex,同时更新数据库表
 	 * @param iMap HashMap<String,Object>
 	 * @return
 	 */
 	public final int setIndexToDB(HashMap<String,Object> iMap){
 		try {
 			if(!JSONTool.checkMapTypes(iMap,new String[]{"col","asc","intro","type","unique"},new Class[]{String.class,Boolean.class,String.class,String.class,Boolean.class})){
 				//ErrorCode.logError(log, 8,1, " kobjIndex:"+i);
 				return 10;
 			}
 			String col = (String) iMap.get("col");
 			boolean asc = (Boolean) iMap.get("asc");
 			String intro = (String) iMap.get("intro");
 			String type = (String) iMap.get("type");
 			boolean unique = (Boolean) iMap.get("unique");
 			
 			KObjIndex ki = new KObjIndex(col, asc, type, intro, unique);
 			this.indexMap.put(col, ki);
 			DaoInterface dao = KObjManager.findKObjConfig(this.kobjName).getDaoConfig().findDao();
 			if (dao == null) {
 				return 12;
 			}
 			if(!dao.updateIndex(ki)){
 				return 12;
 			}
 		} catch (Exception e) {
 			return 10;
 		}
 		return 0;
 	}
 	
 	/**
 	 * 删除一个索引，同时在数据库中同步删除
 	 * @param key
 	 * @return
 	 */
 	public final boolean removeIndex(String key){
 		KObjIndex ki = this.indexMap.remove(key);
 		DaoInterface dao = KObjManager.findKObjConfig(this.kobjName).getDaoConfig().findDao();
 		if (dao == null) {
 			return false;
 		}
 		if(!dao.removeIndex(ki)){
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean containsIndex(String key){
 		return this.indexMap.containsKey(key);
 	}
 	
 	
 	/**
 	 * 获取KObjColumn
 	 * @param key
 	 * @return
 	 */
 	public KObjColumn getKObjColumn(String key){
 		return this.columnMap.get(key);
 	}
 	
 	/**
 	 * 获取KObjColumn的HashMap
 	 * @return
 	 */
 	public final HashMap<String,KObjColumn> getKObjColumns(){
 		return this.columnMap;
 	}
 	
 	/**
 	 * 获取KObjIndex
 	 * @param key
 	 * @return
 	 */
 	public KObjIndex getKObjIndex(String key){
 		return this.indexMap.get(key);
 	}
 	
 	/**
 	 * 验证某一字段值
 	 * @param kobjPath 字段的路径,如:tags.tagName
 	 * @param value
 	 * @return 字段不存在或验证失败时返回false
 	 */
 	public  boolean validateColumns(String kobjPath,Object value){
 		KObjColumn k = this.columnMap.get(kobjPath);
 		if (k == null) {
 			return false;
 		}
 		return k.validateColumn(value);
 	}
 	
 	/**
 	 * 验证整个KObj的jsonData
 	 * @param kobjMap HashMap<String,Object>
 	 * @return
 	 */
 	public  boolean validate(HashMap<String,Object> kobjMap){
 		//逐个KObjColumn字段验证
 		for (Iterator<KObjColumn> iterator = this.columnList.iterator(); iterator.hasNext();) {
 			KObjColumn kc = iterator.next();
 			String col = kc.getCol();
 			Object o = kobjMap.get(col);
 			if (o == null) {
 				return false;
 			}
 			if(!kc.validateColumn(o)){
 				return false;
 			}
 		}
 		//字段数量判断
 		if (kobjMap.size() != this.columnSize) {
 			return false;
 		}
 		return true;
 	}
 	
 	
 	/**
 	 * 验证并设置属性,支持子对象设置,注意返回失败仍可能有部分属性设置已完成
 	 * FIXME 实现KObject.clone()方法，在失败时返回原对象
 	 * @param kobjMap HashMap<String,Object>
 	 * @param kobj 可为空对象或已有属性的对象，注意map中的数据会覆盖原属性
 	 * @return 是否设置成功,验证不通过则返回false
 	 */
 	public  boolean setPropFromMap(HashMap<String,Object> kobjMap,KObject kobj){
 		
 		for (Iterator<String> it = kobjMap.keySet().iterator(); it.hasNext();) {
 			String k = it.next();
 			KObjColumn kc = this.columnMap.get(k);
 			if (kc != null) {
 				if(!kc.validateAndSet(kobjMap.get(k), kobj)){
 					return false;
 				}
 			}else if(kobj.containsProp(k)){
 				Object o = kobj.getProp(k);
 				Object newo = kobjMap.get(k);
 				String kType = o.getClass().getName();
 				String s = newo.toString();
 				if (kType.equals(String.class.getName())) {
 					kobj.setProp(k, s);
 				}else if(StringUtil.isDigits(s)){
 					if (kType.equals(Long.class.getName())) {
 						kobj.setProp(k, Long.parseLong(s));
 					}else{
 						kobj.setProp(k, Integer.parseInt(s));
 					}
 				}
 			}
 		}
 		/*
 		//逐个KObjColumn字段验证并设置
 		for (Iterator<KObjColumn> iterator = this.columnList.iterator(); iterator.hasNext();) {
 			KObjColumn kc = iterator.next();
 			String col = kc.getCol();
 			Object o = kobjMap.get(col);
 			if (o == null) {
 				continue;
 			}
 			if(!kc.validateAndSet(o, kobj)){
 				return false;
 			}
 		}
 		//设置KObject默认属性
 		String[] defaultProps = KObject.getDefaultPropsWithOutId();
 		String[] dTypes = KObject.getDefaultPropTypesWithOutId();
 		for (int i = 0,j=defaultProps.length; i < j; i++) {
 			Object o = kobjMap.get(defaultProps[i]);
 			if (o != null) {
 				String s = o.toString();
 				if (!dTypes[i].equals("String") && StringUtil.isDigits(s)) {
 					if (dTypes[i].equals("Long")) {
 						kobj.setProp(defaultProps[i], Long.parseLong(s));
 					}else{
 						kobj.setProp(defaultProps[i], Integer.parseInt(s));
 					}
 				}else{
 					kobj.setProp(defaultProps[i], s);
 				}
 			}
 		}
 		*/
 		return true;
 	}
 	/**
 	 * 设置KObject属性,注意在kobjPath中不包含List，否则返回false
 	 * @param kobj
 	 * @param kobjPath
 	 * @param prop
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean setProp(KObject kobj,String kobjPath,Object prop){
 		if (kobjPath.indexOf('*') >= 0) {
 			return false;
 		}
 		if (!this.validateColumns(kobjPath, prop)) {
 			return false;
 		}
 		int dotPosi = kobjPath.indexOf('.');
 		if (dotPosi >= 0) {
 //			String preKey = kobjPath.substring(0,dotPosi);
 //			KObjColumn k = this.schemaMap.get(preKey);
 			String[] paths = kobjPath.split("\\.");
 			HashMap<String,Object> target = kobj.getPropMap();
 			for (int i = 0; i < paths.length-1; i++) {
 //				if (paths[i].equals("*")) {
 //					//ArrayList
 //					return false;
 //				}else{
 				target = (HashMap<String, Object>) target.get(paths[i]);
 //				}
 			}
 			target.put(paths[paths.length-1], prop);
 		}else{
 			kobj.setProp(kobjPath, prop);
 		}
 		return true;
 	}
 	
 	
 
 	/**
 	 * 创建一个空的KObject,注意只涉及一级KObjColumn设置，子KObjColumn不会再处理,均按父KObjColumn中的默认值设置
 	 * @return KObject
 	 */
 	public KObject createEmptyKObj(){
 		KObject kobj = this.createEmptyKObjNoId();
 		//通过Dao设置KObject的ID,其他参数为KObject默认值
 		DaoInterface dao = KObjManager.findKObjConfig(this.kobjName).getDaoConfig().findDao();
 		kobj.setId(dao.getIdm().nextId());
 		return kobj;
 	}
 	
 	/**
 	 * 创建空ID的空对象,不涉及dao创建id的操作
 	 * @return
 	 */
 	public KObject createEmptyKObjNoId(){
 		KObject kobj = new KObject();
 		for (Iterator<KObjColumn> iterator = this.columnList.iterator(); iterator.hasNext();) {
 			KObjColumn kc = iterator.next();
 			String col = kc.getCol();
 			kobj.setProp(col, kc.getDef());
 		}
 		return kobj;
 	}
 	
 	/**
 	 * 获取所有的索引
 	 * @return HashMap<String,KObjIndex>
 	 */
 	public final HashMap<String,KObjIndex> getIndexes(){
 		return this.indexMap;
 	}
 	
 	/**
 	 * 获取col列表,包含子col对象
 	 * @return
 	 */
 	public final ArrayList<KObjColumn> getColList(){
 		ArrayList<KObjColumn> list = new ArrayList<KObjColumn>();
 		for (Iterator<KObjColumn> iterator = this.columnList.iterator(); iterator.hasNext();) {
 			KObjColumn kc = iterator.next();
 			list.add(kc);
 			subToList(list,kc);
 		}
 		return list;
 	}
 	
 	/**
 	 * 获取所有的column的name,包括KObject自带的,但不包含子column对象
 	 * @return String[]
 	 */
 	public final String[] getAllColNames(){
 		KObject kobj = this.createEmptyKObjNoId();
 		String[] colarr = kobj.getPropMap().keySet().toArray(new String[0]);
 		return colarr;
 	}
 	
 	/**
 	 * 获取某个索引
 	 * @param colOfIndex
 	 * @return
 	 */
 	public KObjIndex getIndex(String colOfIndex){
 		return this.indexMap.get(colOfIndex);
 	}
 
 
 	/**
 	 * 处理子KObjColumn的toMap
 	 * @param cols
 	 * @param kc
 	 */
 	private final void subToMap(ArrayList<HashMap<String,Object>> cols,KObjColumn kc){
 		ArrayList<KObjColumn> subs = kc.getSubColumns();
 		if (subs != null) {
 			for (Iterator<KObjColumn> it2 = subs.iterator(); it2.hasNext();) {
 				KObjColumn kcc = it2.next();
 				cols.add(kcc.toMap());
 				subToMap(cols,kcc);
 			}
 		}
 	}
 	
 	/**
 	 * 处理包含子KObjColumn的toList
 	 * @param colList
 	 * @param kc
 	 */
 	private final void subToList(ArrayList<KObjColumn> colList,KObjColumn kc){
 		ArrayList<KObjColumn> subs = kc.getSubColumns();
 		if (subs != null) {
 			for (Iterator<KObjColumn> it2 = subs.iterator(); it2.hasNext();) {
 				KObjColumn kcc = it2.next();
 				colList.add(kcc);
 				subToList(colList,kcc);
 			}
 		}
 	}
 	
 	/**
 	 * 为配置文件更新用
 	 * @return
 	 */
 	public HashMap<String,Object> toMap() {
 		HashMap<String,Object> map = new HashMap<String, Object>();
 		ArrayList<HashMap<String,Object>> cols = new ArrayList<HashMap<String,Object>>();
 //		for (Iterator<String> it = this.columnMap.keySet().iterator(); it.hasNext();) {
 //			String key =  it.next();
 //			KObjColumn kc = this.columnMap.get(key);
 //			cols.add(kc.toMap());
 //		}
 		for (Iterator<KObjColumn> iterator = this.columnList.iterator(); iterator.hasNext();) {
 			KObjColumn kc = iterator.next();
 			cols.add(kc.toMap());
 			subToMap(cols,kc);
 		}
 		
 		map.put("columns", cols);
 		ArrayList<HashMap<String,Object>> indexes = new ArrayList<HashMap<String,Object>>();
 		for (Iterator<String> it = this.indexMap.keySet().iterator(); it.hasNext();) {
 			String key =  it.next();
 			KObjIndex ki = this.indexMap.get(key);
 			indexes.add(ki.toMap());
 		}
 		map.put("indexes", indexes);
 		return map;
 	}
 
 	/**
 	 * @return the kobjName
 	 */
 	public final String getKobjName() {
 		return kobjName;
 	}
 
 	
 	
 }
