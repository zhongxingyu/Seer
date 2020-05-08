 package org.young.db;
 
 /**
  * 数据库相关的代码搜集
  *
  * @author by Young.ZHU
  *                on 2012-7-16
  *
  * Package&FileName: org.young.db.DB
  */
 public class DB {
     //~ Static fields/initializers *********************************************
 
     private static long BASE        = System.currentTimeMillis();
     private static int  COUNTER     = 0;
     final static int    STEP_FACTOR = 100;
 
     //~ Methods ****************************************************************
 
     /**
      * 生成唯一码（序列号）
      * 可以生成18位主键的序列号
      *
      * @return
      */
     public static long generateUniqueKey() {
         if (COUNTER > (STEP_FACTOR - 1)) {
             BASE        = System.currentTimeMillis();
             COUNTER     = 0;
         }
 
         return ((BASE * STEP_FACTOR) + (COUNTER++));
     }
    
    /**
     * 根据当前时间生成指定位数的序列号
     * 
     * @param length - 指定长度
     * @return
     */
    public static String generateUniqueKey(int length) {
    	
    	String ret = String.valueOf(System.currentTimeMillis());
    	
    	int size = ret.length();
    	
		if (size < length) {
			for (int i = 0; i < (length - size); i++) {
				ret += "0";
			}
		}

		return ret;
    }
 }
