 package org.apache.hadoop.hive.metastore.msg;
 
 /**
  * For msg definition send to other sub-system.
  * MSGType.
  * @author DENNIS
  */
 public class MSGType {
 
   public static final int MSG_NEW_DATABESE = 1001;
   //新建库
   public static final int MSG_ALTER_DATABESE = 1002;
       //修改库
   public static final int MSG_ALTER_DATABESE_PARAM = 1003;
       //修改库属性
   public static final int MSG_DROP_DATABESE = 1004;
       //删除库
   public static final int MSG_NEW_TALBE = 1101;
       //新建表
   public static final int MSG_ALT_TALBE_NAME = 1102;
       //修改表名
   public static final int MSG_ALT_TALBE_DISTRIBUTE = 1103;          ////这个先不管
       //修改表数据分布
   public static final int MSG_ALT_TALBE_PARTITIONING = 1104;
       //修改表分区方式
   public static final int MSG_ALT_TALBE_DEL_COL = 1201;
       //修改表删除列
   public static final int MSG_ALT_TALBE_ADD_COL = 1202;
       //修改表新增列
   public static final int MSG_ALT_TALBE_ALT_COL_NAME = 1203;
       //修改表修改列名
   public static final int MSG_ALT_TALBE_ALT_COL_TYPE = 1204;
       //修改表修改列类型
   public static final int MSG_ALT_TALBE_ALT_COL_LENGTH = 1205;        ////注意：本事件不会触发！！！
       //表参数变化
   public static final int MSG_ALT_TABLE_PARAM = 1206;
       //修改表修改列类型长度
   public static final int MSG_DROP_TABLE = 1207;        //删除表
 
   public static final int MSG_NEW_PARTITION =  1301;
       // 新建分区
   public static final int MSG_ALT_PARTITION =  1302;
       //修改分区
   public static final int MSG_DEL_PARTITION =  1303;
       // 删除分区
   public static final int MSG_ADD_PARTITION_FILE =  1304;
       //增加分区文件
   public static final int MSG_ALT_PARTITION_FILE =  1305;           ////分区的先不管
       //修改分区文件
  /**
   * 文件副本变化
   */
   public static final int MSG_REP_FILE_CHANGE =  1306;

   public static final int MSG_STA_FILE_CHANGE =  1307;
       //文件状态变化
   public static final int MSG_REP_FILE_ONOFF =  1308;
       //文件副本上下线变化
   public static final int MSG_DEL_PARTITION_FILE =  1309;
       //删除分区文件
   public static final int MSG_FILE_USER_SET_REP_CHANGE = 1310;
       //用户设置的目标副本数目变化
   public static final int MSG_NEW_INDEX =  1401;
       //新建列索引
   public static final int MSG_ALT_INDEX =  1402;
       //修改列索引
   public static final int MSG_ALT_INDEX_PARAM = 1403;
       //修改列索引属性
   public static final int MSG_DEL_INDEX = 1404;
       //删除列索引
   public static final int MSG_NEW_PARTITION_INDEX =  1405;
       //新建分区索引
   public static final int MSG_ALT_PARTITION_INDEX = 1406;           ////
       //修改分区索引
   public static final int MSG_DEL_PARTITION_INDEX = 1407;
       // 删除分区索引
   public static final int MSG_NEW_PARTITION_INDEX_FILE =  1408;
       //增加分区索引文件
   public static final int MSG_ALT_PARTITION_INDEX_FILE = 1409;
       //修改分区索引文件
   public static final int MSG_DEL_PARTITION_INDEX_FILE = 1413;
       //删除分区索引文件
   public static final int MSG_NEW_NODE = 1501;
       //新增节点
   public static final int MSG_DEL_NODE = 1502;
       //删除节点
   public static final int MSG_FAIL_NODE = 1503;
       //节点故障
   public static final int MSG_BACK_NODE = 1504;
       //节点恢复
 
   public static final int MSG_CREATE_SCHEMA = 1601;
   public static final int MSG_MODIFY_SCHEMA_NAME = 1602;
   public static final int MSG_MODIFY_SCHEMA_DEL_COL = 1603;
   public static final int MSG_MODIFY_SCHEMA_ADD_COL = 1604;
   public static final int MSG_MODIFY_SCHEMA_ALT_COL_NAME = 1605;
   public static final int MSG_MODIFY_SCHEMA_ALT_COL_TYPE = 1606;
   public static final int MSG_MODIFY_SCHEMA_PARAM = 1607;
   public static final int MSG_DEL_SCHEMA = 1608;
 
   public static final int MSG_DDL_DIRECT_DW1 = 2001;
   //dw1 专用DDL语句
   public static final int MSG_DDL_DIRECT_DW2 = 2002;
   //dw2 专用DDL语句
 
   public static final int MSG_NEW_NODEGROUP = 3001;     //新增节点组
   public static final int MSG_MODIFY_NODEGROUP = 3002;      //修改节点组
   public static final int MSG_DEL_NODEGROUP = 3003;         //删除节点组
 
   public static final int MSG_CREATE_FILE = 4001;
   public static final int MSG_DEL_FILE = 4002;
 
 
 }
