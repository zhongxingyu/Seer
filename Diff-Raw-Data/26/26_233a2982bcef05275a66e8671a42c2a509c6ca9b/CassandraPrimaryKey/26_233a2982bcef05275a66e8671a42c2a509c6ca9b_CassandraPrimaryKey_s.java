 package com.joyveb.support.cassandra;
 
 import lombok.Data;
 
 /**
  * @author limj
  * @date 2013.02.22
  *       <p>
  *       ColumnFamily的key，所有ColumnFamily POJO都继承此类
  *       <p>
  *       注意：1、POJO所有字段类型都是对象
  *       <p>
  *       2、Cassandra table 字段都大写
  */
 @Data
public class CassandraPrimaryKey<K> {
 
 	protected K key;
 
 }
