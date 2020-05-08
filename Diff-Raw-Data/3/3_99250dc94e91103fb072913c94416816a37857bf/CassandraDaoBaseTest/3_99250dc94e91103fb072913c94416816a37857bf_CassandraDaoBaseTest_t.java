 package com.feedly.cassandra.dao;
 
 import static org.junit.Assert.*;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.concurrent.TimeUnit;
 
 import me.prettyprint.cassandra.serializers.AsciiSerializer;
 import me.prettyprint.cassandra.serializers.BytesArraySerializer;
 import me.prettyprint.cassandra.serializers.CompositeSerializer;
 import me.prettyprint.cassandra.serializers.DynamicCompositeSerializer;
 import me.prettyprint.cassandra.serializers.IntegerSerializer;
 import me.prettyprint.cassandra.serializers.LongSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.Composite;
 import me.prettyprint.hector.api.beans.CounterSlice;
 import me.prettyprint.hector.api.beans.DynamicComposite;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.beans.HCounterColumn;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 import me.prettyprint.hector.api.query.SliceCounterQuery;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.feedly.cassandra.EConsistencyLevel;
 import com.feedly.cassandra.PersistenceManager;
 import com.feedly.cassandra.entity.ByteIndicatorSerializer;
 import com.feedly.cassandra.entity.EntityMetadata;
 import com.feedly.cassandra.entity.EntityUtils;
 import com.feedly.cassandra.entity.EnumSerializer;
 import com.feedly.cassandra.entity.IndexMetadata;
 import com.feedly.cassandra.entity.TestPartitioner;
 import com.feedly.cassandra.entity.enhance.CompositeIndexedBean;
 import com.feedly.cassandra.entity.enhance.CounterBean;
 import com.feedly.cassandra.entity.enhance.ESampleEnum;
 import com.feedly.cassandra.entity.enhance.EmbeddedBean;
 import com.feedly.cassandra.entity.enhance.EmbeddedCounterBean;
 import com.feedly.cassandra.entity.enhance.IEnhancedEntity;
 import com.feedly.cassandra.entity.enhance.IndexedBean;
 import com.feedly.cassandra.entity.enhance.ListBean;
 import com.feedly.cassandra.entity.enhance.MapBean;
 import com.feedly.cassandra.entity.enhance.NestedBean;
 import com.feedly.cassandra.entity.enhance.ParentBean;
 import com.feedly.cassandra.entity.enhance.ParentCounterBean;
 import com.feedly.cassandra.entity.enhance.PartitionedIndexBean;
 import com.feedly.cassandra.entity.enhance.SampleBean;
 import com.feedly.cassandra.entity.enhance.SortedMapBean;
 import com.feedly.cassandra.entity.enhance.TtlBean;
 import com.feedly.cassandra.test.CassandraServiceTestBase;
 
 @SuppressWarnings({"unchecked", "rawtypes"})
 public class CassandraDaoBaseTest extends CassandraServiceTestBase
 {
     PersistenceManager _pm;
 
     SampleBeanDao _dao;
     
     MapBeanDao _mapDao;
     SortedMapBeanDao _sortedMapDao;
     
     ListBeanDao _listDao;
     
     ParentBeanDao _parentBeanDao;
     NestedBeanDao _nestedBeanDao;
 
     IndexedBeanDao _indexedDao;
     CompositeIndexedBeanDao _compositeIndexedDao;
     
     CounterBeanDao _counterDao;
     ParentCounterBeanDao _parentCounterDao;
     
     RecordingStrategy _indexedStrategy, _compositeStrategy;
     
     @Before
     public void before()
     {
         _pm = new PersistenceManager();
         _dao = new SampleBeanDao();
         _dao.setKeyspaceFactory(_pm);
         _dao.init();
 
         _counterDao = new CounterBeanDao();
         _counterDao.setKeyspaceFactory(_pm);
         _counterDao.init();
         
         _mapDao = new MapBeanDao();
         _mapDao.setKeyspaceFactory(_pm);
         _mapDao.init();
         
         _sortedMapDao = new SortedMapBeanDao();
         _sortedMapDao.setKeyspaceFactory(_pm);
         _sortedMapDao.init();
         
         _listDao = new ListBeanDao();
         _listDao.setKeyspaceFactory(_pm);
         _listDao.init();
 
         _parentBeanDao = new ParentBeanDao();
         _parentBeanDao.setKeyspaceFactory(_pm);
         _parentBeanDao.init();
         
         _parentCounterDao = new ParentCounterBeanDao();
         _parentCounterDao.setKeyspaceFactory(_pm);
         _parentCounterDao.init();
         
         _nestedBeanDao = new NestedBeanDao();
         _nestedBeanDao.setKeyspaceFactory(_pm);
         _nestedBeanDao.init();
         
         _indexedDao = new IndexedBeanDao();
         _indexedDao.setKeyspaceFactory(_pm);
         _indexedStrategy = new RecordingStrategy();
         _indexedDao.setStaleValueIndexStrategy(_indexedStrategy);
         _indexedDao.init();
         
         _compositeIndexedDao = new CompositeIndexedBeanDao();
         _compositeIndexedDao.setKeyspaceFactory(_pm);
         _compositeStrategy = new RecordingStrategy();
         _compositeIndexedDao.setStaleValueIndexStrategy(_compositeStrategy);
         _compositeIndexedDao.init();
 
 
         configurePersistenceManager(_pm);
         
         _pm.setPackagePrefixes(new String[] {SampleBean.class.getPackage().getName()});
         _pm.init();
     }
 
     
     private void assertBean(String msg, SampleBean bean, ColumnSlice<String, byte[]> columnSlice)
     {
         assertEquals(msg, 9 + (bean.getUnmapped() == null ? 0 : bean.getUnmapped().size()), columnSlice.getColumns().size());
         
         for(HColumn<String, byte[]> col : columnSlice.getColumns())
         {
             String n = col.getName();
             if(n.equals("boolVal"))
                 assertColumn(msg, bean.getBoolVal(), false, col);
             else if(n.equals("charVal"))
                 assertColumn(msg, bean.getCharVal(), false, col);
             else if(n.equals("dateVal"))
                 assertColumn(msg, bean.getDateVal(), false, col);
             else if(n.equals("d"))
                 assertColumn(msg, bean.getDoubleVal(), false, col);
             else if(n.equals("floatVal"))
                 assertColumn(msg, bean.getFloatVal(), false, col);
             else if(n.equals("intVal"))
                 assertColumn(msg, bean.getIntVal(), false, col);
             else if(n.equals("l"))
                 assertColumn(msg, bean.getLongVal(), false, col);
             else if(n.equals("s"))
                 assertColumn(msg, bean.getStrVal(), false, col);
             else if(n.equals("sampleEnum"))
             {
                 byte[] value = col.getValue(); 
                 assertEquals(msg, bean.getSampleEnum(), new EnumSerializer(ESampleEnum.class).fromBytes(value));
                 assertEquals(msg, bean.getSampleEnum().name(), StringSerializer.get().fromBytes(value));
             }
             else
                 assertColumn(bean.getUnmapped().get(n), true, col);
         }
     }
 
     private void assertColumn(Object expected, boolean indicatorExpected, HColumn<?, byte[]> col)
     {
         assertColumn(null,  expected, indicatorExpected, col);
     }
     
     private void assertColumn(String message, Object expected, boolean indicatorExpected, HColumn<?, byte[]> col)
     {
         byte[] value = col.getValue(); 
         
         if(indicatorExpected)
         {
             assertEquals(message, ByteIndicatorSerializer.INDICATOR_BYTES.get(expected.getClass()), col.getValue()[0]);
             value = new byte[col.getValue().length - 1];
             System.arraycopy(col.getValue(), 1, value, 0, value.length);
         }
         assertEquals(message, expected, EntityUtils.getSerializer(expected.getClass()).fromBytes(value));
     }
     
     private <T> void assertBeansEqual(List<T> expecteds, List<T> actuals)
     {
         for(int i = 0; i < Math.min(expecteds.size(), actuals.size()); i++)
         {
             assertEquals("position[" + i + "]", expecteds.get(i), actuals.get(i));
         }
         
         assertEquals("size", expecteds.size(), actuals.size());
     }
     
     
     /*
      * begin test cases
      */
 
     @Test
     public void testSimplePut()
     {
         int numBeans = 5;
         List<SampleBean> beans = new ArrayList<SampleBean>();
         
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = new SampleBean();
             bean.setRowKey(new Long(i));
             bean.setBoolVal(i%2 == 0);
             bean.setCharVal((char) ('a' + i));
             bean.setDateVal(new Date(System.currentTimeMillis() + 60000*i));
             bean.setDoubleVal(i * .1);
             bean.setFloatVal(i / .5f);
             bean.setIntVal(i);
             bean.setLongVal(-i);
             bean.setStrVal("str-" + i);
             bean.setUnmapped(new HashMap<String, Object>());
             bean.setSampleEnum(ESampleEnum.values()[i % ESampleEnum.values().length]);
             
             for(int j = 0; j <= i; j++)
                 bean.getUnmapped().put("unmapped-" + j, j);
             beans.add(bean);
         }
         
         for(SampleBean bean : beans)
         {
             _dao.put(bean);
             assertTrue(((IEnhancedEntity) bean).getModifiedFields().isEmpty());
             assertFalse(((IEnhancedEntity) bean).getUnmappedFieldsModified());
             SliceQuery<Long,String,byte[]> query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), AsciiSerializer.get(), BytesArraySerializer.get());
             query.setKey(bean.getRowKey());
             query.setColumnFamily("sample");
             query.setRange("", "", false, 100);
             ColumnSlice<String, byte[]> columnSlice = query.execute().get();
             
             assertBean("beans[" + bean.getIntVal() + "]", bean, columnSlice);
         }
         
         SampleBean bean0 = beans.get(0);
         //make sure only dirty fields are saved
         bean0.setStrVal("updated");
         bean0.setDoubleVal(100.0);
         bean0.setUnmapped((Map) Collections.singletonMap("unmapped-0", 100));
         IEnhancedEntity bean = (IEnhancedEntity) bean0;
         bean.getModifiedFields().clear(8);
         bean.setUnmappedFieldsModified(false);
         
         _dao.put(bean0);
         bean0.setStrVal("str-0");
         bean0.setUnmapped((Map) Collections.singletonMap("unmapped-0", 0));
 
         SliceQuery<Long,String,byte[]> query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), AsciiSerializer.get(), BytesArraySerializer.get());
         query.setKey(bean0.getRowKey());
         query.setColumnFamily("sample");
         query.setRange("", "", false, 100);
 
         assertBean("dirty test", bean0, query.execute().get());
     }
 
     @Test
     public void testCounterPut()
     {
         int numBeans = 5;
         List<CounterBean> beans = new ArrayList<CounterBean>();
         List<ParentCounterBean> pbeans = new ArrayList<ParentCounterBean>();
         
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean bean = new CounterBean();
             bean.setRowKey(new Long(i));
             bean.setCounterVal(new CounterColumn(i*10));
             beans.add(bean);
 
             ParentCounterBean pbean = new ParentCounterBean();
             pbean.setRowkey(new Long(i));
             pbean.setCounterProp(new CounterColumn(i*20));
             pbean.setEmbeddedProp(createEmbeddedCounterBean(i));
             pbeans.add(pbean);
         }
         
         for(CounterBean bean : beans)
         {
             _counterDao.put(bean);
             assertTrue(((IEnhancedEntity) bean).getModifiedFields().isEmpty());
             assertFalse(((IEnhancedEntity) bean).getUnmappedFieldsModified());
             SliceCounterQuery<Long,String> query = HFactory.createCounterSliceQuery(keyspace, LongSerializer.get(), StringSerializer.get());
             
             query.setKey(bean.getRowKey());
             query.setColumnFamily("counter_cntr");
             query.setRange("", "", false, 100);
             CounterSlice<String> columnSlice = query.execute().get();
             HCounterColumn<String> counterColumn = columnSlice.getColumns().get(0);
             
             assertEquals("beans[" + bean.getRowKey() + "]", 1, columnSlice.getColumns().size());
             assertEquals("beans[" + bean.getRowKey() + "]", bean.getRowKey()*10, counterColumn.getValue());
             assertEquals("c", counterColumn.getName());
             
             bean.getCounterVal().setIncrement(10);
             _counterDao.put(bean);
 
             columnSlice = query.execute().get();
             counterColumn = columnSlice.getColumns().get(0);
             
             assertEquals("beans[" + bean.getRowKey() + "]", 10 + bean.getRowKey()*10, counterColumn.getValue());
             
             /*
              * null out values and resave, ensure values are deleted
              */
             bean.setCounterVal(null);
             _counterDao.put(bean);
             columnSlice = query.execute().get();
             assertTrue(columnSlice.getColumns().isEmpty());
         }
 
         for(ParentCounterBean bean : pbeans)
         {
             _parentCounterDao.put(bean);
             assertTrue(((IEnhancedEntity) bean).getModifiedFields().isEmpty());
             assertFalse(((IEnhancedEntity) bean).getUnmappedFieldsModified());
             SliceCounterQuery<Long,DynamicComposite> query = HFactory.createCounterSliceQuery(keyspace, LongSerializer.get(), DynamicCompositeSerializer.get());
             
             query.setKey(bean.getRowkey());
             query.setColumnFamily("parentcounterbean_cntr");
             query.setRange(new DynamicComposite(""), new DynamicComposite("z"), false, 100);
             CounterSlice<DynamicComposite> columnSlice = query.execute().get();
             
             assertEquals("beans[" + bean.getRowkey() + "]", 2, columnSlice.getColumns().size());
             assertEquals("beans[" + bean.getRowkey() + "]", bean.getRowkey() * 20, columnSlice.getColumnByName(new DynamicComposite("c")).getValue());
             assertEquals("beans[" + bean.getRowkey() + "]", bean.getRowkey(), columnSlice.getColumnByName(new DynamicComposite("e", "c")).getValue());
             
             bean.getCounterProp().setIncrement(10);
             _parentCounterDao.put(bean);
             
             columnSlice = query.execute().get();
             assertEquals("beans[" + bean.getRowkey() + "]", 2, columnSlice.getColumns().size());
             assertEquals("beans[" + bean.getRowkey() + "]", bean.getRowkey() * 20 + 10, columnSlice.getColumnByName(new DynamicComposite("c")).getValue());
             assertEquals("beans[" + bean.getRowkey() + "]", bean.getRowkey(), columnSlice.getColumnByName(new DynamicComposite("e", "c")).getValue());
             
             /*
              * null out values and resave, ensure values are deleted
              */
             bean.getEmbeddedProp().setCounterProp(null);
             _parentCounterDao.put(bean);
             columnSlice = query.execute().get();
             assertEquals("beans[" + bean.getRowkey() + "]", 1, columnSlice.getColumns().size());
             assertEquals("beans[" + bean.getRowkey() + "]", bean.getRowkey() * 20 + 10, columnSlice.getColumnByName(new DynamicComposite("c")).getValue());
             assertNull("beans[" + bean.getRowkey() + "]", columnSlice.getColumnByName(new DynamicComposite("e", "c")));
         }
     }
     
     @Test
     public void testColumnFamilyTtl() throws InterruptedException
     {
         CassandraDaoBase<Long, TtlBean> dao = new CassandraDaoBase<Long, TtlBean>(Long.class, TtlBean.class, EConsistencyLevel.ONE);
         dao.setKeyspaceFactory(_pm);
         dao.init();
 
         TtlBean bean = new TtlBean();
         bean.setRowKey(10L);
         bean.setStrVal1("v1");
         bean.setStrVal2("v2");
         bean.setStrVal3("v3");
         dao.put(bean);
 
         TtlBean tmpl = new TtlBean();
         tmpl.setStrVal1(bean.getStrVal1());
         tmpl.setStrVal2(bean.getStrVal2());
         tmpl.setStrVal3(bean.getStrVal3());
         
         assertEquals(1, dao.mfind(tmpl).size());
         assertEquals(1, dao.rangeFindStats().getNumOps());
                      
         assertEquals(bean, dao.get(bean.getRowKey()));
 
         Thread.sleep(7500);
         dao.rangeFindStats().reset();
         assertEquals(0, dao.mfind(tmpl).size()); //index row should have expired as well
         assertEquals(0, dao.rangeFindStats().getNumOps());
         
         bean.setStrVal1(null);
         assertEquals(bean, dao.get(bean.getRowKey()));
 
         Thread.sleep(5000);
         bean.setStrVal3(null);
         assertEquals(bean, dao.get(bean.getRowKey()));
 
         Thread.sleep(5000);
         assertNull(dao.get(bean.getRowKey()));
     }
 
     @Test
     public void testColumnTtl() throws InterruptedException
     {
         /*
          * simple
          */
         SampleBean bean = new SampleBean();
         bean.setRowKey(10L);
         bean.setTtlVal(2);
 
         _dao.put(bean);
         assertEquals(bean.getTtlVal(), _dao.get(bean.getRowKey()).getTtlVal());
         
         Thread.sleep(3000);
         assertNull(_dao.get(bean.getRowKey()));
         
         /*
          * embedded
          */
         EmbeddedBean embedded = new EmbeddedBean();
         embedded.setStrProp("foo");
         embedded.setDoubleProp(1.0);
         ParentBean parent = new ParentBean();
         parent.setRowkey(10L);
         parent.setEmbeddedProp(embedded);
         parent.setListProp(Collections.singletonList(embedded));
         _parentBeanDao.put(parent);
         assertEquals(parent, _parentBeanDao.get(parent.getRowkey()));
         
         Thread.sleep(3000);
         ParentBean actual = _parentBeanDao.get(parent.getRowkey()); 
         assertNull(actual.getListProp().get(0).getStrProp());
         assertEquals(parent.getEmbeddedProp().getStrProp(), actual.getEmbeddedProp().getStrProp());
 
         Thread.sleep(2000);
         actual = _parentBeanDao.get(parent.getRowkey()); 
         assertNull(actual.getEmbeddedProp());
         
         /*
          * List
          */
         ListBean list = new ListBean();
         list.setRowkey(10L);
         list.setStrProp("Foo");
         list.setListProp(Arrays.<Object>asList("foo1", "foo2"));
         _listDao.put(list);
         assertEquals(list, _listDao.get(list.getRowkey()));
 
         Thread.sleep(3000);
         assertNull(_listDao.get(list.getRowkey()).getListProp());
         
         /*
          * Map
          */
         MapBean map = new MapBean();
         map.setRowkey(10L);
         map.setStrProp("Foo");
         map.setMapProp(new HashMap<String, Object>());
         map.getMapProp().put("key1", "val1");
         _mapDao.put(map);
         
         Thread.sleep(2500);
         assertEquals(map, _mapDao.get(map.getRowkey()));
         map.setMapProp(Collections.<String, Object>singletonMap("key2", "val2"));
         _mapDao.put(map);
         
         
         Thread.sleep(4000);
         map.getMapProp().remove("key1");
         assertEquals(map, _mapDao.get(map.getRowkey()));
     }
     
     @Test
     public void testDelete()
     {
         int numBeans = 5;
         List<SampleBean> beans = new ArrayList<SampleBean>();
         
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = new SampleBean();
             bean.setRowKey(new Long(i));
             bean.setBoolVal(i%2 == 0);
             bean.setCharVal((char) ('a' + i));
             bean.setDateVal(new Date(System.currentTimeMillis() + 60000*i));
             bean.setDoubleVal(i * .1);
             bean.setFloatVal(i / .5f);
             bean.setIntVal(i);
             bean.setLongVal(-i);
             bean.setStrVal("str-" + i);
             bean.setSampleEnum(ESampleEnum.VALUE1);
             bean.setUnmapped(new HashMap<String, Object>());
             for(int j = 0; j <= i; j++)
                 bean.getUnmapped().put("unmapped-" + j, j);
             beans.add(bean);
         }
         
         for(SampleBean bean : beans)
         {
             _dao.put(bean);
         }
         
         SampleBean bean0 = beans.get(0);
         _dao.delete(bean0.getRowKey());
         
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = beans.get(i);
             SliceQuery<Long,String,byte[]> query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), AsciiSerializer.get(), BytesArraySerializer.get());
             query.setKey(bean.getRowKey());
             query.setColumnFamily("sample");
             query.setRange("", "", false, 100);
             ColumnSlice<String, byte[]> columnSlice = query.execute().get();
             
             if(i == 0)
             {
                 assertEquals(0, columnSlice.getColumns().size()); //"tombstone" row still exists
                 assertNull(_dao.get(bean0.getRowKey()));
             }
             else
                 assertBean("beans[" + bean.getIntVal() + "]", bean, columnSlice);
         }
 
         assertEquals(1, _dao.deleteStats().getNumCassandraOps());
         assertEquals(1, _dao.deleteStats().getNumOps());
         assertEquals(0, _dao.deleteStats().getNumCols());
         assertEquals(1, _dao.deleteStats().getNumRows());
         assertEquals(1, _dao.deleteStats().getRecentTimings().length);
         
 
         _dao.mdelete(Arrays.asList(beans.get(1).getRowKey(), beans.get(2).getRowKey()));
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = beans.get(i);
             SliceQuery<Long,String,byte[]> query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), AsciiSerializer.get(), BytesArraySerializer.get());
             query.setKey(bean.getRowKey());
             query.setColumnFamily("sample");
             query.setRange("", "", false, 100);
             ColumnSlice<String, byte[]> columnSlice = query.execute().get();
             
             if(i < 3)
             {
                 assertEquals(0, columnSlice.getColumns().size()); //"tombstone" row still exists
                 assertNull(_dao.get(bean0.getRowKey()));
             }
             else
                 assertBean("beans[" + bean.getIntVal() + "]", bean, columnSlice);
         }
 
         assertEquals(3, _dao.deleteStats().getNumCassandraOps());
         assertEquals(2, _dao.deleteStats().getNumOps());
         assertEquals(0, _dao.deleteStats().getNumCols());
         assertEquals(3, _dao.deleteStats().getNumRows());
         assertEquals(2, _dao.deleteStats().getRecentTimings().length);
     }
 
     @Test
     public void testCounterDelete()
     {
         int numBeans = 5;
         List<CounterBean> beans = new ArrayList<CounterBean>();
         List<ParentCounterBean> pbeans = new ArrayList<ParentCounterBean>();
         
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean bean = new CounterBean();
             bean.setRowKey(new Long(i));
             bean.setCounterVal(new CounterColumn(i*10));
             beans.add(bean);
 
             ParentCounterBean pbean = new ParentCounterBean();
             pbean.setRowkey(new Long(i));
             pbean.setCounterProp(new CounterColumn(i*20));
             pbeans.add(pbean);
 
             _counterDao.put(bean);
             _parentCounterDao.put(pbean);
         }
         
         CounterBean bean0 = beans.get(0);
         ParentCounterBean pbean0 = pbeans.get(0);
         
         _counterDao.delete(bean0.getRowKey());
         _parentCounterDao.delete(pbean0.getRowkey());
         
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean bean = beans.get(i);
             SliceCounterQuery<Long,String> query = HFactory.createCounterSliceQuery(keyspace, LongSerializer.get(), StringSerializer.get());
             query.setKey(bean.getRowKey());
             query.setColumnFamily("counter_cntr");
             query.setRange("", "", false, 100);
             CounterSlice<String> columnSlice = query.execute().get();
             
             if(i == 0)
             {
                 assertEquals(0, columnSlice.getColumns().size()); //"tombstone" row still exists
 //                assertNull(_dao.get(bean0.getRowKey()));
             }
             else
                 assertEquals("beans[" + bean.getRowKey() + "]", 1, columnSlice.getColumns().size());
             
             
             ParentCounterBean pbean = pbeans.get(i);
             SliceCounterQuery<Long, DynamicComposite> pquery = HFactory.createCounterSliceQuery(keyspace, LongSerializer.get(), DynamicCompositeSerializer.get());
             pquery.setKey(pbean.getRowkey());
             pquery.setColumnFamily("parentcounterbean_cntr");
             pquery.setRange(new DynamicComposite(""), new DynamicComposite("z"), false, 100);
             CounterSlice<String> pslice = query.execute().get();
             
             if(i == 0)
             {
                 assertEquals(0, pslice.getColumns().size()); //"tombstone" row still exists
 //                assertNull(_dao.get(bean0.getRowKey()));
             }
             else
                 assertEquals("beans[" + bean.getRowKey() + "]", 1, pslice.getColumns().size());
         }
         
         _counterDao.mdelete(Arrays.asList(beans.get(1).getRowKey(), beans.get(2).getRowKey()));
         _parentCounterDao.mdelete(Arrays.asList(pbeans.get(1).getRowkey(), pbeans.get(2).getRowkey()));
 
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean bean = beans.get(i);
             SliceCounterQuery<Long,String> query = HFactory.createCounterSliceQuery(keyspace, LongSerializer.get(), StringSerializer.get());
             query.setKey(bean.getRowKey());
             query.setColumnFamily("counter_cntr");
             query.setRange("", "", false, 100);
             CounterSlice<String> columnSlice = query.execute().get();
             
             if(i < 3)
             {
                 assertEquals(0, columnSlice.getColumns().size()); //"tombstone" row still exists
 //                assertNull(_dao.get(bean0.getRowKey()));
             }
             else
                 assertEquals("beans[" + bean.getRowKey() + "]", 1, columnSlice.getColumns().size());
             
             ParentCounterBean pbean = pbeans.get(i);
             SliceCounterQuery<Long, DynamicComposite> pquery = HFactory.createCounterSliceQuery(keyspace, LongSerializer.get(), DynamicCompositeSerializer.get());
             pquery.setKey(pbean.getRowkey());
             pquery.setColumnFamily("parentcounterbean_cntr");
             pquery.setRange(new DynamicComposite(""), new DynamicComposite("z"), false, 100);
             CounterSlice<String> pslice = query.execute().get();
             
             if(i < 3)
             {
                 assertEquals(0, pslice.getColumns().size()); //"tombstone" row still exists
 //                assertNull(_dao.get(bean0.getRowKey()));
             }
             else
                 assertEquals("beans[" + bean.getRowKey() + "]", 1, pslice.getColumns().size());
         }
     }
     
     private EmbeddedBean embeddedBean(int cnt, int mult)
     {
         EmbeddedBean rv = new EmbeddedBean();
         rv.setDoubleProp(1.1*mult);
         rv.setStrProp("estr-" + cnt);
         rv.setListProp(new ArrayList<Integer>());
         rv.setMapProp(new HashMap<String, Integer>());
         rv.setUnmappedHandler(new HashMap<String, Object>());
         for(int i = 0; i < cnt; i++)
         {
             rv.getListProp().add(i*mult);
             rv.getMapProp().put("key-" + i, i*mult*2);
             rv.getUnmappedHandler().put("unmapped-"+i, "unmapped-" + i*mult*3);
         }
         
         return rv;
     }
     
     @Test
     public void testEmbeddedPut() throws Exception
     {
         ParentBean bean = new ParentBean();
         bean.setRowkey(10L);
 
         bean.setListProp(new ArrayList<EmbeddedBean>());
         bean.getListProp().add(embeddedBean(1, 1)); //5 vals
         bean.getListProp().add(embeddedBean(2, 2)); //8 vals
         bean.setMapProp(new HashMap<String, EmbeddedBean>());
         bean.getMapProp().put("mapProp1", embeddedBean(1, 3)); //5 vals
         bean.getMapProp().put("mapProp2", embeddedBean(2, 4)); //8 vals
         bean.setStrProp("strProp-val"); //1 val
         
         bean.setEmbeddedProp(new EmbeddedBean()); //will be 5 vals
         bean.getEmbeddedProp().setListProp(new ArrayList<Integer>());
         bean.getEmbeddedProp().getListProp().add(100);
         bean.getEmbeddedProp().getListProp().add(200);
         bean.getEmbeddedProp().setMapProp(new HashMap<String, Integer>());
         bean.getEmbeddedProp().getMapProp().put("mapProp1", 1000);
         bean.getEmbeddedProp().getMapProp().put("mapProp2", 2000);
         bean.getEmbeddedProp().setStrProp("estrProp-val");
         
         _parentBeanDao.put(bean);
 
         assertEmbeddedReset(bean);
         DynamicCompositeSerializer dcs = new DynamicCompositeSerializer();
         SliceQuery<Long, DynamicComposite, byte[]> query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), dcs, BytesArraySerializer.get());
         query.setKey(bean.getRowkey());
         query.setColumnFamily("parentbean");
         
         query.setRange(new DynamicComposite(), new DynamicComposite(), false, 100);
 
         ColumnSlice<DynamicComposite, byte[]> slice = query.execute().get();
         
         assertEquals(5 + 8 + 5 + 8 + 1 + 5, slice.getColumns().size()); 
         assertColumn(bean.getStrProp(), false, slice.getColumnByName(new DynamicComposite("s")));
         assertEmbeddedColumn(bean.getEmbeddedProp(), slice, new DynamicComposite("e"));
         for(int i = 0; i < bean.getListProp().size(); i++)
             assertEmbeddedColumn(bean.getListProp().get(i), slice, new DynamicComposite("l", i));
 
         for(Entry<String, EmbeddedBean> entry : bean.getMapProp().entrySet())
             assertEmbeddedColumn(entry.getValue(), slice, new DynamicComposite("m", entry.getKey()));
     }
 
     private void assertEmbeddedReset(ParentBean... beans)
     {
         for(int i = 0; i < beans.length; i++)
         {
             ParentBean bean = beans[i];
             List<EmbeddedBean> embedded = new ArrayList<EmbeddedBean>();
             embedded.add(bean.getEmbeddedProp());
             embedded.addAll(bean.getListProp());
             embedded.addAll(bean.getMapProp().values());
             for(EmbeddedBean e : embedded)
             {
                 assertTrue(e.getStrProp() + " " + i, ((IEnhancedEntity) e).getModifiedFields().isEmpty());
                 assertFalse(e.getStrProp() + " " + i, ((IEnhancedEntity) e).getUnmappedFieldsModified());
             }
         }
     }
 
 
     private void assertEmbeddedColumn(EmbeddedBean bean,
                                       ColumnSlice<DynamicComposite, byte[]> slice,
                                       DynamicComposite prefix)
     {
         prefix.add("s");
         assertColumn(bean.getStrProp(), false, slice.getColumnByName(prefix));
         prefix.remove(prefix.size()-1);
 
         if(bean.getDoubleProp() != 0)
         {
             prefix.add("d");
             assertColumn(bean.getDoubleProp(), false, slice.getColumnByName(prefix));
             prefix.remove(prefix.size()-1);
         }
 
         prefix.add("m");
         for(Map.Entry entry : bean.getMapProp().entrySet())
         {
             prefix.add(entry.getKey());
             assertColumn(entry.getValue(), false, slice.getColumnByName(prefix));
             prefix.remove(prefix.size()-1);
         }
         prefix.remove(prefix.size()-1);
 
         prefix.add("l");
         for(int i = 0; i < bean.getListProp().size(); i++)
         {
             prefix.add(i);
             assertColumn(bean.getListProp().get(i), false, slice.getColumnByName(prefix));
             prefix.remove(prefix.size()-1);
         }
         prefix.remove(prefix.size()-1);
         
         if(bean.getUnmappedHandler() != null)
         {
             for(Entry<String, Object> entry : bean.getUnmappedHandler().entrySet())
             {
                 prefix.add(entry.getKey());
                 assertColumn(entry.getValue(), true, slice.getColumnByName(prefix));
                 prefix.remove(prefix.size()-1);
             }
         }
     }
 
 
     @Test
     public void testCollectionPut() throws Exception
     {
         /*
          * Map
          */
 
         MapBean mapBean = new MapBean();
         mapBean.setRowkey(10L);
 
         mapBean.setMapProp(new HashMap<String, Object>());
         mapBean.getMapProp().put("longMapProp", 100L);
         mapBean.getMapProp().put("strMapProp", "strMapProp-val");
         mapBean.setStrProp("strProp-val");
         mapBean.setStrProp1("strProp1-val");
         mapBean.setUnmapped((Map)Collections.singletonMap("unmapped-1", "val1"));
         _mapDao.put(mapBean);
         
         DynamicCompositeSerializer dcs = new DynamicCompositeSerializer();
         SliceQuery<Long, DynamicComposite, byte[]> query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), dcs, BytesArraySerializer.get());
         query.setKey(mapBean.getRowkey());
         query.setColumnFamily("mapbean");
         
         query.setRange(new DynamicComposite(), new DynamicComposite(), false, 100);
 
         ColumnSlice<DynamicComposite, byte[]> slice = query.execute().get();
         
         
         assertEquals(5, slice.getColumns().size());
         assertColumn(mapBean.getStrProp(), false, slice.getColumnByName(new DynamicComposite("strProp")));
         assertColumn(mapBean.getStrProp1(), false, slice.getColumnByName(new DynamicComposite("strProp1")));
         assertColumn(mapBean.getMapProp().get("strMapProp"), true, slice.getColumnByName(new DynamicComposite("m", "strMapProp")));
         assertColumn(mapBean.getMapProp().get("longMapProp"), true, slice.getColumnByName(new DynamicComposite("m", "longMapProp")));
         
         //try serializing using unmapped handler with specified serializer 
         assertColumn(mapBean.getUnmapped().get("unmapped-1"), false, slice.getColumnByName(new DynamicComposite("unmapped-1")));
         
         //null out a value and resave
         mapBean.getMapProp().put("strMapProp", null);
         mapBean.setUnmapped(Collections.<String, String>singletonMap("unmapped-1", null));
         _mapDao.put(mapBean);
         
         slice = query.execute().get();
         
         assertEquals(3, slice.getColumns().size());
         assertColumn(mapBean.getStrProp(), false, slice.getColumnByName(new DynamicComposite("strProp")));
         assertColumn(mapBean.getStrProp1(), false, slice.getColumnByName(new DynamicComposite("strProp1")));
         assertColumn(mapBean.getMapProp().get("longMapProp"), true, slice.getColumnByName(new DynamicComposite("m", "longMapProp")));
 
         /*
          * SortedMap
          */
         SortedMapBean sortedMapBean = new SortedMapBean();
         sortedMapBean.setRowkey(10L);
         
         sortedMapBean.setMapProp(new TreeMap<String, Object>());
         sortedMapBean.getMapProp().put("longMapProp", 100L);
         sortedMapBean.getMapProp().put("strMapProp", "strMapProp-val");
         sortedMapBean.setStrProp("strProp-val");
         sortedMapBean.setStrProp1("strProp1-val");
         
         _sortedMapDao.put(sortedMapBean);
         
         query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), dcs, BytesArraySerializer.get());
         query.setKey(sortedMapBean.getRowkey());
         query.setColumnFamily("sortedmapbean");
         
         query.setRange(new DynamicComposite(), new DynamicComposite(), false, 100);
         
         slice = query.execute().get();
         
         
         assertEquals(4, slice.getColumns().size());
         assertColumn(sortedMapBean.getStrProp(), false, slice.getColumnByName(new DynamicComposite("strProp")));
         assertColumn(sortedMapBean.getStrProp1(), false, slice.getColumnByName(new DynamicComposite("strProp1")));
         assertColumn(sortedMapBean.getMapProp().get("strMapProp"), true, slice.getColumnByName(new DynamicComposite("m", "strMapProp")));
         assertColumn(sortedMapBean.getMapProp().get("longMapProp"), true, slice.getColumnByName(new DynamicComposite("m", "longMapProp")));
         
         //null out a value and resave
         sortedMapBean.getMapProp().put("strMapProp", null);
         _sortedMapDao.put(sortedMapBean);
         
         slice = query.execute().get();
         
         assertEquals(3, slice.getColumns().size());
         assertColumn(sortedMapBean.getStrProp(), false, slice.getColumnByName(new DynamicComposite("strProp")));
         assertColumn(sortedMapBean.getStrProp1(), false, slice.getColumnByName(new DynamicComposite("strProp1")));
         assertColumn(sortedMapBean.getMapProp().get("longMapProp"), true, slice.getColumnByName(new DynamicComposite("m", "longMapProp")));
 
         /*
          * List
          */
         
         ListBean bean = new ListBean();
         bean.setRowkey(10L);
         
         bean.setListProp(new ArrayList<Object>());
         bean.getListProp().add("strListProp-val0");
         bean.getListProp().add(100L);
         bean.getListProp().add("strListProp-val2");
         bean.setStrProp("strProp-val");
         bean.setStrProp1("strProp1-val");
         
         _listDao.put(bean);
         
         query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), dcs, BytesArraySerializer.get());
         query.setKey(bean.getRowkey());
         query.setColumnFamily("listbean");
         
         query.setRange(new DynamicComposite(), new DynamicComposite(), false, 100);
         
         slice = query.execute().get();
         
         
         assertEquals(5, slice.getColumns().size());
         assertColumn(bean.getStrProp(), false, slice.getColumnByName(new DynamicComposite("strProp")));
         assertColumn(bean.getStrProp1(), false, slice.getColumnByName(new DynamicComposite("strProp1")));
         assertColumn(bean.getListProp().get(0), true, slice.getColumnByName(new DynamicComposite("l", 0)));
         assertColumn(bean.getListProp().get(1), true, slice.getColumnByName(new DynamicComposite("l", 1)));
         assertColumn(bean.getListProp().get(2), true, slice.getColumnByName(new DynamicComposite("l", 2)));
         
         //null out a value and resave
         bean.getListProp().set(1, null);
         _listDao.put(bean);
         
         slice = query.execute().get();
         
         assertEquals(4, slice.getColumns().size());
         assertColumn(bean.getStrProp(), false, slice.getColumnByName(new DynamicComposite("strProp")));
         assertColumn(bean.getStrProp1(), false, slice.getColumnByName(new DynamicComposite("strProp1")));
         assertColumn(bean.getListProp().get(0), true, slice.getColumnByName(new DynamicComposite("l", 0)));
         assertColumn(bean.getListProp().get(2), true, slice.getColumnByName(new DynamicComposite("l", 1)));
         
         /*
          * Nested
          */
         NestedBean nbean = new NestedBean();
         nbean.setRowkey(10L);
         List<List<Double>> lol = new ArrayList<List<Double>>();
         List<Map<String, String>> lom = new ArrayList<Map<String, String>>();
         List<Map<Long, List<Double>>> lomol = new ArrayList<Map<Long,List<Double>>>();
         
         Map<String, Map<Integer, Integer>> mom = new HashMap<String, Map<Integer,Integer>>();
         Map<String, List<String>> mol = new HashMap<String, List<String>>();
         Map<String, List<Map<String, Date>>> molom = new HashMap<String, List<Map<String,Date>>>();
         
         int numCols = 0;
         int numD1 = 5;
         int numD2 = 7;
         int numD3 = 3;
 
         for(int i = 0; i < numD1; i++)
         {
             lol.add(new ArrayList<Double>());
             lom.add(new HashMap<String, String>());
             lomol.add(new HashMap<Long, List<Double>>());
 
             mom.put("key-" + i, new HashMap<Integer, Integer>());
             mol.put("key-" + i, new ArrayList<String>());
             molom.put("key-" + i, new ArrayList<Map<String,Date>>());
             
             for(int j = 0; j < numD2; j++)
             {
                 lol.get(i).add(i*j*1.1);
                 numCols++;
 
                 lom.get(i).put("key-" + j, String.valueOf(i+j+1.1));
                 numCols++;
 
                 mom.get("key-" + i).put(j, i*j);
                 numCols++;
 
                 mol.get("key-" + i).add(i + "-" + j);
                 numCols++;
                 
                 lomol.get(i).put(new Long(j), new ArrayList<Double>());
                 molom.get("key-" + i).add(new HashMap<String, Date>());
 
                 for(int k = 0; k < numD3; k++)
                 {
                     lomol.get(i).get(new Long(j)).add(i+j+k+.33);
                     numCols++;
 
                     molom.get("key-" + i).get(j).put(i + "-" + j + "-" + k, new Date(System.currentTimeMillis() - i*j*k));
                     numCols++;
                 }
             }
         }
         
 
         
         nbean.setListOfListProp(lol);
         nbean.setListOfMapProp(lom);
         nbean.setListOfMapOfListProp(lomol);
         nbean.setMapOfListProp(mol);
         nbean.setMapOfMapProp(mom);
         nbean.setMapOfListOfMapProp(molom);
         _nestedBeanDao.put(nbean);
 
         query = HFactory.createSliceQuery(keyspace, LongSerializer.get(), dcs, BytesArraySerializer.get());
         query.setKey(nbean.getRowkey());
         query.setColumnFamily("nestedbean");
         
         query.setRange(new DynamicComposite(), new DynamicComposite(), false, 1000);
         
         slice = query.execute().get();
         assertEquals(numCols, slice.getColumns().size());
         
         for(HColumn<DynamicComposite, byte[]> c : slice.getColumns())
         {
             if(c.getName().size() == 3 && c.getName().get(2) instanceof ByteBuffer)
             {
                 c.getName().set(2, IntegerSerializer.get().fromByteBuffer((ByteBuffer) c.getName().get(2))); //dc uses bigint rather than int for some reason...
             }
         }
         for(int i = 0; i < numD1; i++)
         {
             for(int j = 0; j < numD2; j++)
             {
                 assertColumn(nbean.getListOfListProp().get(i).get(j), false, slice.getColumnByName(new DynamicComposite("listOfListProp", i, j)));
                 assertColumn(nbean.getListOfMapProp().get(i).get("key-" + j), false, slice.getColumnByName(new DynamicComposite("listOfMapProp", i, "key-" + j)));
 
                 assertColumn(nbean.getMapOfMapProp().get("key-" + i).get(j), false, slice.getColumnByName(new DynamicComposite("mapOfMapProp", "key-" + i, j)));
                 assertColumn(nbean.getMapOfListProp().get("key-" + i).get(j), false, slice.getColumnByName(new DynamicComposite("mapOfListProp", "key-" + i, j)));
                 
                 for(int k = 0; k < numD3; k++)
                 {
                     assertColumn(nbean.getListOfMapOfListProp().get(i).get(new Long(j)).get(k), false, slice.getColumnByName(new DynamicComposite("listOfMapOfListProp", i, new Long(j), k)));
                     
                     Date exp = nbean.getMapOfListOfMapProp().get("key-" + i).get(j).get(i + "-" + j + "-" + k);
                     HColumn<DynamicComposite, byte[]> act = slice.getColumnByName(new DynamicComposite("mapOfListOfMapProp", "key-" + i, j, i + "-" + j + "-" + k));
                     assertColumn(exp, false, act);
                 }
             }
         }
     }
 
     private EmbeddedCounterBean createEmbeddedCounterBean(long base)
     {
         EmbeddedCounterBean ebean = new EmbeddedCounterBean();
         ebean.setCounterProp(new CounterColumn(base));
         ebean.setStrProp("estr-"+ base);
         
         return ebean;
     }
     
     private ParentCounterBean createParentCounterBean(long key)
     {
         ParentCounterBean bean = new ParentCounterBean();
         long inc = key*10;
         bean.setRowkey(key);
         bean.setCounterProp(new CounterColumn(inc++));
         bean.setStrProp("str-"+key);
         bean.setEmbeddedProp(createEmbeddedCounterBean(inc++));
         
         bean.setListProp(new ArrayList<EmbeddedCounterBean>());
         bean.setMapProp(new HashMap<String, EmbeddedCounterBean>());
         for(int i = 0; i <= key; i++)
             bean.getListProp().add(createEmbeddedCounterBean(inc++));
 
         for(int i = 0; i <= key; i++)
             bean.getMapProp().put("mapkey-" + i, createEmbeddedCounterBean(inc++));
         
         return bean;
     }
     
 
     private void convertParentCounterBean(ParentCounterBean bean)
     {
         bean.setCounterProp(toStoredForm(bean.getCounterProp()));
         convertEmbeddedCounterBean(bean.getEmbeddedProp());
         for(EmbeddedCounterBean e : bean.getListProp())
             convertEmbeddedCounterBean(e);
         
         for(EmbeddedCounterBean e : bean.getMapProp().values())
             convertEmbeddedCounterBean(e);
     }
 
     private void convertEmbeddedCounterBean(EmbeddedCounterBean bean)
     {
         bean.setCounterProp(toStoredForm(bean.getCounterProp()));
     }
 
     private CounterColumn toStoredForm(CounterColumn cc)
     {
         return new CounterColumn(cc.getIncrement(), null);
     }
     
     @Test
     public void testCounterGet()
     {
         int numBeans = 5;
         List<CounterBean> beans = new ArrayList<CounterBean>();
         List<ParentCounterBean> pbeans = new ArrayList<ParentCounterBean>();
         List<Long> keys = new ArrayList<Long>();
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean cbean = new CounterBean();
             cbean.setRowKey((long) i);
             cbean.setCounterVal(new CounterColumn(i*10));
             beans.add(cbean);
 
             CounterBean copy = new CounterBean();
             copy.setRowKey(cbean.getRowKey());
             copy.setCounterVal(new CounterColumn(i*10));
             _counterDao.put(copy); //saving clears increment, use copy for comparisons
 
             ParentCounterBean pbean = createParentCounterBean(i);
             pbeans.add(pbean);
             _parentCounterDao.put(createParentCounterBean(i)); 
             
             keys.add((long) i);
         }
 
         
         List<CounterBean> actualBeans = new ArrayList<CounterBean>();
         List<ParentCounterBean> actualPbeans = new ArrayList<ParentCounterBean>();
         List<CounterBean> bulkActualBeans = new ArrayList<CounterBean>(_counterDao.mget(keys));
         List<ParentCounterBean> bulkActualPbeans = new ArrayList<ParentCounterBean>(_parentCounterDao.mget(keys));
         
         Collections.sort(bulkActualBeans);
         Collections.sort(bulkActualPbeans);
         
         for(int i = 0; i < numBeans; i++)
         {
             actualBeans.add(_counterDao.get(beans.get(i).getRowKey()));
             actualPbeans.add(_parentCounterDao.get(pbeans.get(i).getRowkey()));
 
             //convert counters to stored for easy comparison
             beans.get(i).setCounterVal(toStoredForm(beans.get(i).getCounterVal()));
             convertParentCounterBean(pbeans.get(i));
         }
 
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean bean = beans.get(i), actual = actualBeans.get(i), bulkActual = bulkActualBeans.get(i);
             assertEquals(bean, actual);
             assertEquals(bean, bulkActual);
 
             ParentCounterBean pbean = pbeans.get(i), pactual = actualPbeans.get(i), bulkPactual = bulkActualPbeans.get(i);
             assertEquals(pbean, pactual);
             assertEquals(pbean, bulkPactual);
         }
 
         
         /*
          * range
          */
         actualBeans = new ArrayList<CounterBean>();
         actualPbeans = new ArrayList<ParentCounterBean>();
         for(int i = 0; i < numBeans; i++)
         {
             actualBeans.add(_counterDao.get(beans.get(i).getRowKey(), null, new GetOptions("b", "d")));
             actualPbeans.add(_parentCounterDao.get(pbeans.get(i).getRowkey(), null, new GetOptions(new CollectionProperty("listProp", 1), new CollectionProperty("listProp", 3))));
         }
 
         bulkActualBeans = _counterDao.mget(keys, null, new GetOptions("b", "d"));
         bulkActualPbeans = _parentCounterDao.mget(keys, null, new GetOptions(new CollectionProperty("listProp", 1), new CollectionProperty("listProp", 3)));
 
         Collections.sort(bulkActualBeans);
 
         int nullCnt = 0;
         for(int i = bulkActualPbeans.size() - 1; i >= 0; i--)
         {
             if(bulkActualPbeans.get(i) == null)
             {
                 bulkActualPbeans.remove(i);
                 nullCnt++;
             }
         }
         assertEquals(1, nullCnt);
         Collections.sort(bulkActualPbeans); //can't sort a list with null vals.
         bulkActualPbeans.add(0, null); //add the null value back to preserve the comparison ordering below
         
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean bean = beans.get(i), actual = actualBeans.get(i), bulkActual = bulkActualBeans.get(i);
             assertEquals(bean, actual);
             assertEquals(bean, bulkActual);
 
             ParentCounterBean pbean = pbeans.get(i), pactual = actualPbeans.get(i), bulkPactual = bulkActualPbeans.get(i);
             if(i == 0)
             {
                 assertNull(pactual);
             }
             else
             {
                 for(ParentCounterBean a : Arrays.asList(pactual, bulkPactual))
                 {
                     assertEquals(pbean.getRowkey(), a.getRowkey());
                     assertNull(a.getCounterProp());
                     assertNull(a.getEmbeddedProp());
                     assertNull(a.getMapProp());
                     assertNull(a.getStrProp());
                     if(i >= 1)
                     {
                         for(int j = 1; j <= Math.min(i, 3); j++)
                             assertEquals(pbean.getListProp().get(j), a.getListProp().get(j));
                     }
                 }
             }
         }
         
         /*
          * includes/excludes
          */
         actualBeans = new ArrayList<CounterBean>();
         actualPbeans = new ArrayList<ParentCounterBean>();
         
         Set<Object> includes = new HashSet<Object>();
         includes.add("counterProp");
         includes.add("strProp");
         includes.add(new CollectionProperty("embeddedProp", "s"));
         includes.add("listProp");
 
         for(int i = 0; i < numBeans; i++)
         {
             actualBeans.add(_counterDao.get(beans.get(i).getRowKey(), null, new GetOptions(Collections.singleton("counterVal"), null)));
             actualPbeans.add(_parentCounterDao.get(pbeans.get(i).getRowkey(), null, new GetOptions(includes, null)));
         }
         
         bulkActualBeans = _counterDao.mget(keys, null, new GetOptions(Collections.singleton("counterVal"), null));
         bulkActualPbeans = _parentCounterDao.mget(keys, null, new GetOptions(includes, null));
         
         Collections.sort(bulkActualBeans);
         Collections.sort(bulkActualPbeans);
 
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean bean = beans.get(i), actual = actualBeans.get(i), bulkActual = bulkActualBeans.get(i);
             assertEquals(bean, actual);
             assertEquals(bean, bulkActual);
 
             ParentCounterBean pbean = pbeans.get(i), pactual = actualPbeans.get(i), bulkPactual = bulkActualPbeans.get(i);
             pbean.getEmbeddedProp().setCounterProp(null);
             pbean.setMapProp(null);
             assertEquals(pbean, pactual);
             assertEquals(pbean, bulkPactual);
         }
     }
     
     @Test
     public void testSimpleGet()
     {
         int numBeans = 5;
         List<SampleBean> beans = new ArrayList<SampleBean>();
         
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = new SampleBean();
             bean.setRowKey(new Long(i));
             bean.setBoolVal(i%2 == 0);
             bean.setCharVal((char) ('a' + i));
             bean.setDateVal(new Date(System.currentTimeMillis() + 60000*i));
             bean.setDoubleVal(i * .1);
             bean.setFloatVal(i / .5f);
             bean.setIntVal(i);
             bean.setLongVal(-i);
             bean.setStrVal("str-" + i);
             bean.setSampleEnum(ESampleEnum.values()[i % ESampleEnum.values().length]);
             
             bean.setUnmapped(new HashMap<String, Object>());
             for(int j = 0; j <= 100; j++)
                 bean.getUnmapped().put("unmapped-" + j, "val-" + i + "-" + j);
             
             beans.add(bean);
         }
         
         for(SampleBean bean : beans)
             _dao.put(bean);
         
         int expectedOps = 0;
         assertEquals(expectedOps, _dao.getStats().getNumOps());
         assertEquals(expectedOps, _dao.getStats().getNumCassandraOps());
         for(SampleBean bean : beans)
         {
             SampleBean loaded = _dao.get(bean.getRowKey());
             
             assertTrue(((IEnhancedEntity) loaded).getModifiedFields().isEmpty());
             assertFalse(((IEnhancedEntity) loaded).getUnmappedFieldsModified());
             assertEquals(bean, loaded);
             assertNotNull(loaded.getUnmapped());
             assertFalse(loaded.getUnmapped().isEmpty());
             
             assertEquals(++expectedOps, _dao.getStats().getNumOps());
             assertEquals(110*expectedOps, _dao.getStats().getNumCols());
             assertEquals(2*expectedOps, _dao.getStats().getNumCassandraOps()); //large number of unmapped cols requires addl slice query per row
             assertEquals(expectedOps, _dao.getStats().getRecentTimings().length);
         }
         
         SampleBean bean0 = beans.get(0);
         //test null update
         bean0.setStrVal(null);
         bean0.setLongVal(2000);
         _dao.put(bean0);
         assertEquals(bean0, _dao.get(bean0.getRowKey()));
 
         
         //test partial
         
         SampleBean partial = new SampleBean();
         partial.setRowKey(1000L);
         partial.setStrVal("hello");
         _dao.put(partial);
         assertEquals(partial, _dao.get(partial.getRowKey()));
         
         //test non-existent
         assertNull(_dao.get(-5L));
     }
     
     @Test
     public void testSimpleMget()
     {
         
         try
         {
             _dao.mget(Arrays.asList(-1L, -1L));
             fail("duplicate keys not allowed");
         }
         catch(IllegalArgumentException ex)
         {
             //success
         }
         
         int numBeans = 5;
         List<SampleBean> beans = new ArrayList<SampleBean>();
         List<Long> keys = new ArrayList<Long>();
 
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = new SampleBean();
             bean.setRowKey(new Long(i));
             bean.setBoolVal(i%2 == 0);
             bean.setCharVal((char) ('a' + i));
             bean.setDateVal(new Date(System.currentTimeMillis() + 60000*i));
             bean.setDoubleVal(i * .1);
             bean.setFloatVal(i / .5f);
             bean.setIntVal(i);
             bean.setLongVal(-i);
             bean.setStrVal("str-" + i);
             bean.setSampleEnum(ESampleEnum.values()[i % ESampleEnum.values().length]);
 
             beans.add(bean);
             keys.add(beans.get(i).getRowKey());
             
             bean.setUnmapped(new TreeMap<String, Object>());
             for(int j = 0; j <= 100; j++)
                 bean.getUnmapped().put("unmapped-" + j, "val-" + i + "-" + j);
         }
         
         _dao.mput(beans);
 
         List<Long> keyList = new ArrayList<Long>(keys);
         keyList.add(-5L); //non-existent
         List<SampleBean> actual = new ArrayList<SampleBean>( _dao.mget(keyList) );
 
         Collections.sort(actual);
         assertEquals(beans.size(), actual.size());
         
         for(int i = beans.size() - 1; i >= 0; i--)
         {
             SampleBean loaded = actual.get(i);
             assertTrue(((IEnhancedEntity) loaded).getModifiedFields().isEmpty());
 
             if(loaded.getUnmapped() != null)
                 loaded.setUnmapped(new TreeMap<String, Object>(loaded.getUnmapped()));
             
             assertEquals("bean[" + i + "]", beans.get(i), loaded);
         }
         
         assertEquals(1, _dao.getStats().getNumOps());
         assertEquals(6, _dao.getStats().getNumCassandraOps());
         assertEquals(numBeans*110, _dao.getStats().getNumCols());
         assertEquals(numBeans+1, _dao.getStats().getNumRows());
         assertEquals(1, _dao.getStats().getRecentTimings().length);
     }
 
     @Test
     public void testSimpleMgetAll()
     {
         int numBeans = 201;
         List<SampleBean> beans = new ArrayList<SampleBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = new SampleBean();
             bean.setRowKey(new Long(i));
             bean.setBoolVal(i%2 == 0);
             bean.setCharVal((char) ('a' + i));
             bean.setDateVal(new Date(System.currentTimeMillis() + 60000*i));
             bean.setDoubleVal(i * .1);
             bean.setFloatVal(i / .5f);
             bean.setIntVal(i);
             bean.setLongVal(-i);
             bean.setStrVal("str-" + i);
             bean.setSampleEnum(ESampleEnum.values()[i % ESampleEnum.values().length]);
 
             beans.add(bean);
             keys.add(beans.get(i).getRowKey());
             
             bean.setUnmapped(new TreeMap<String, Object>());
             for(int j = 0; j <= 100; j++)
                 bean.getUnmapped().put("unmapped-" + j, "val-" + i + "-" + j);
         }
         
         _dao.mput(beans);
 
         List<SampleBean> actual = new ArrayList<SampleBean>( _dao.mgetAll() );
 
         Collections.sort(actual);
         assertEquals(beans.size(), actual.size());
         
         for(int i = beans.size() - 1; i >= 0; i--)
         {
             SampleBean loaded = actual.get(i);
             assertEquals("bean[" + i + "]", beans.get(i), loaded);
 
             assertTrue(((IEnhancedEntity) loaded).getModifiedFields().isEmpty());
         }
         
         GetAllOptions options = new GetAllOptions();
         options.setMaxRows(150);
         actual = new ArrayList<SampleBean>(_dao.mgetAll(options));
         assertEquals(150, actual.size());
         Set<Long> actualKeys = new HashSet<Long>();
         for(SampleBean loaded : actual)
         {
             assertTrue(actualKeys.add(loaded.getRowKey()));
             assertEquals(beans.get(loaded.getRowKey().intValue()), loaded);
         }
     }
     
     @Test
     public void testCounterMgetAll()
     {
         int numBeans = 201;
         List<CounterBean> beans = new ArrayList<CounterBean>();
         List<ParentCounterBean> pbeans = new ArrayList<ParentCounterBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             CounterBean bean = new CounterBean();
             bean.setRowKey(new Long(i));
             bean.setCounterVal(new CounterColumn(i));
             
             beans.add(bean);
             keys.add(beans.get(i).getRowKey());
             
             ParentCounterBean pbean = new ParentCounterBean();
             pbean.setRowkey(new Long(i));
 
             //mod result == 0 -> counter cols
             //           == 1 -> normal cols
             //           == 2 -> counter and normal cols
             if(i%3 != 0)
             {
                 pbean.setStrProp("str-"+i);
                 pbean.setEmbeddedProp(new EmbeddedCounterBean());
                 pbean.getEmbeddedProp().setStrProp("estr-"+i);
             }
             
             if(i%3 != 1)
             {
                 pbean.setCounterProp(new CounterColumn(i));
                 pbean.setEmbeddedProp(new EmbeddedCounterBean());
                 pbean.getEmbeddedProp().setCounterProp(new CounterColumn(i*10));
             }
             
             pbeans.add(pbean);
         }
         
         _counterDao.mput(beans);
         _parentCounterDao.mput(pbeans);
         
         List<CounterBean> actuals = new ArrayList<CounterBean>( _counterDao.mgetAll() );
         
         Collections.sort(actuals);
 
         assertEquals(beans.size(), actuals.size());
         
         for(int i = beans.size() - 1; i >= 0; i--)
         {
             CounterBean actual = actuals.get(i);
             assertTrue(((IEnhancedEntity) actual).getModifiedFields().isEmpty());
             assertEquals("bean[" + i + "]", beans.get(i).getRowKey(), actual.getRowKey());
             assertEquals("bean[" + i + "]", i, actual.getCounterVal().getStored());
         }
         
         /*
          * only normal cols
          */
         GetAllOptions options = new GetAllOptions(Collections.singleton("strProp"), null);
         List<ParentCounterBean> pactuals = new ArrayList<ParentCounterBean>( _parentCounterDao.mgetAll(options) );
         
         Collections.sort(pactuals);
         List<ParentCounterBean> pbeans2 = new ArrayList<ParentCounterBean>();
         for(int i = 0; i < pbeans.size(); i++)
         {
             if(i%3 != 0)
                 pbeans2.add(pbeans.get(i));
         }
         
         assertEquals(pbeans2.size(), pactuals.size());
         
         for(int i = 0; i < pbeans2.size(); i++)
         {
             ParentCounterBean pactual = pactuals.get(i);
             assertTrue(((IEnhancedEntity) pactual).getModifiedFields().isEmpty());
             assertEquals("bean[" + i + "]", pbeans2.get(i).getRowkey(), pactual.getRowkey());
             assertEquals("bean[" + i + "]", pbeans2.get(i).getStrProp(), pactual.getStrProp());
         }
 
         /*
          * only counter cols
          */
         options = new GetAllOptions(Collections.singleton("counterProp"), null);
         options.setGetCounterColumns();
         pactuals = new ArrayList<ParentCounterBean>( _parentCounterDao.mgetAll(options) );
         
         Collections.sort(pactuals);
         pbeans2 = new ArrayList<ParentCounterBean>();
         for(int i = 0; i < pbeans.size(); i++)
         {
             if(i%3 != 1)
                 pbeans2.add(pbeans.get(i));
         }
         
         assertEquals(pbeans2.size(), pactuals.size());
         
         for(int i = 0; i < pbeans2.size(); i++)
         {
             ParentCounterBean pactual = pactuals.get(i);
             assertTrue(((IEnhancedEntity) pactual).getModifiedFields().isEmpty());
             assertEquals("bean[" + i + "]", pbeans2.get(i).getRowkey(), pactual.getRowkey());
             assertEquals("bean[" + i + "]", pbeans2.get(i).getRowkey(), pactual.getCounterProp().getStored());
         }
     }
     
     @Test
     public void testEmbeddedGet() throws Exception
     {
         int numBeans = 5;
         List<ParentBean> beans = new ArrayList<ParentBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             ParentBean bean = new ParentBean();
             bean.setRowkey(new Long(i));
             keys.add(bean.getRowkey());
             beans.add(bean);
             bean.setListProp(new ArrayList<EmbeddedBean>());
             bean.getListProp().add(embeddedBean(1, i+1)); 
             bean.getListProp().add(embeddedBean(2, i+2)); 
             bean.getListProp().add(embeddedBean(3, i+3)); 
             bean.setMapProp(new HashMap<String, EmbeddedBean>());
             bean.getMapProp().put("mapProp1", embeddedBean(1, i+3)); //3 vals
             bean.getMapProp().put("mapProp2", embeddedBean(2, i+4)); 
             bean.setStrProp("strProp-"+i); //1 val
 
             bean.setEmbeddedProp(new EmbeddedBean()); //will be 5 vals
             bean.getEmbeddedProp().setListProp(new ArrayList<Integer>());
             bean.getEmbeddedProp().getListProp().add(i+100);
             bean.getEmbeddedProp().getListProp().add(i+200);
             bean.getEmbeddedProp().setMapProp(new HashMap<String, Integer>());
             bean.getEmbeddedProp().getMapProp().put("mapProp1", i+1000);
             bean.getEmbeddedProp().getMapProp().put("mapProp2", i+2000);
             bean.getEmbeddedProp().setStrProp("estrProp-"+i);
         }        
         
         _parentBeanDao.mput(beans);
         
         for(int i = 0; i < numBeans; i++)
         {
             ParentBean expected = beans.get(i);
             ParentBean actual = _parentBeanDao.get(expected.getRowkey());
             assertEquals(expected.getStrProp(), actual.getStrProp());
             assertEquals(expected.getEmbeddedProp(), actual.getEmbeddedProp());
             assertEquals(expected.getListProp(), actual.getListProp());
             assertEquals(expected.getMapProp(), actual.getMapProp());
             
             
             assertEquals(expected, actual);
             assertEmbeddedReset(actual);
         }
         List<ParentBean> bulkActuals = new ArrayList<ParentBean>(_parentBeanDao.mget(keys));
         Collections.sort(bulkActuals);
         assertEquals(beans, bulkActuals);
         assertEmbeddedReset(bulkActuals.toArray(new ParentBean[0]));
         
         /*
          * test partials
          */
         for(long i = 0; i < numBeans; i++)
         {
             Set<Object> includes = new HashSet<Object>();
             includes.add("embeddedProp");
             
             GetOptions options = new GetOptions(includes, null);
             
             ParentBean bean = beans.get((int) i);
             ParentBean expected = new ParentBean();
             expected.setRowkey(bean.getRowkey());
             expected.setEmbeddedProp(bean.getEmbeddedProp());
             ParentBean actualBean = _parentBeanDao.get(i, null, options);
             assertEquals("bean-" + i, expected, actualBean);
 
             expected.setListProp(Arrays.asList(new EmbeddedBean[] {null, bean.getListProp().get(1)}));
             includes.add(new CollectionProperty("listProp", 1));
             actualBean = _parentBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
 
             expected.setMapProp(bean.getMapProp());
             includes.add("mapProp");
             actualBean = _parentBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
             
             includes.clear();
             includes.add(new CollectionProperty("embeddedProp", "s"));
             expected = new ParentBean();
             expected.setRowkey(bean.getRowkey());
             expected.setEmbeddedProp(new EmbeddedBean());
             expected.getEmbeddedProp().setStrProp(bean.getEmbeddedProp().getStrProp());
             actualBean = _parentBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
 
 
         }
 
         /*
          * test partial ranges
          */
         for(long i = 0; i < numBeans; i++)
         {
             //note double prop is not set
             GetOptions options = new GetOptions(new CollectionProperty("embeddedProp", "a"), new CollectionProperty("embeddedProp", "n"));
             
             ParentBean bean = beans.get((int) i);
             ParentBean expected = new ParentBean();
             expected.setRowkey(bean.getRowkey());
             expected.setEmbeddedProp(new EmbeddedBean());
             expected.getEmbeddedProp().setListProp(bean.getEmbeddedProp().getListProp());
             expected.getEmbeddedProp().setMapProp(bean.getEmbeddedProp().getMapProp());
             ParentBean actualBean = _parentBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
         }
 
     }
     
     @Test
     public void testMapGet()
     {
         int numBeans = 5;
         List<MapBean> beans = new ArrayList<MapBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             MapBean bean = new MapBean();
             bean.setRowkey(new Long(i));
             bean.setStrProp("str-" + i);
             keys.add(bean.getRowkey());
             bean.setUnmapped(new HashMap<String, String>());
             for(int j = 0; j <= 150; j++)
                 bean.getUnmapped().put("unmapped-" + j, "val-" + i + "-" + j);
 
             bean.setMapProp(new HashMap<String, Object>());
             for(int j = 50; j <= 200; j++)
                 bean.getMapProp().put("propval-" + j, "val-" + i + "-" + j);
 
             beans.add(bean);
         }
         
         _mapDao.mput(beans);
         int expectedOps = 0;
         
         for(MapBean bean : beans)
         {
             MapBean loaded = _mapDao.get(bean.getRowkey());
             assertTrue(((IEnhancedEntity) loaded).getModifiedFields().isEmpty());
             assertFalse(((IEnhancedEntity) loaded).getUnmappedFieldsModified());
             assertEquals(bean, loaded);
             assertNotNull(loaded.getUnmapped());
             assertFalse(loaded.getUnmapped().isEmpty());
             
             assertEquals(++expectedOps, _mapDao.getStats().getNumOps());
             assertEquals(303*expectedOps, _mapDao.getStats().getNumCols());
             assertEquals(4*expectedOps, _mapDao.getStats().getNumCassandraOps()); 
             assertEquals(expectedOps, _mapDao.getStats().getRecentTimings().length);
         }
         
         
         /*
          * bulk load
          */
         
         //get all
         List<MapBean> loaded = new ArrayList<MapBean>( _mapDao.mgetAll());
         Collections.sort(loaded);
         for(MapBean bean : beans)
         {
             assertTrue(((IEnhancedEntity) bean).getModifiedFields().isEmpty());
         }
         assertBeansEqual(beans, loaded);
 
         //get multiple
         loaded = new ArrayList<MapBean>( _mapDao.mget(keys));
         Collections.sort(loaded);
         for(MapBean bean : beans)
         {
             assertTrue(((IEnhancedEntity) bean).getModifiedFields().isEmpty());
         }
         assertBeansEqual(beans, loaded);
 
         MapBean bean0 = beans.get(0);
         //test null update
         for(int i = 50; i < 75; i++)
             bean0.getMapProp().put("propval-" + i, null);
         assertEquals(151, bean0.getMapProp().size()); //sanity check we are overwriting properties to null
         
         _mapDao.put(bean0);
 
         Iterator<Entry<String, Object>> iter = bean0.getMapProp().entrySet().iterator();
         while(iter.hasNext())
         {
             if(iter.next().getValue() == null)
                 iter.remove();
         }
         
         assertEquals(bean0, _mapDao.get(bean0.getRowkey()));
      
     }
     
     @Test
     public void testSortedMapGet()
     {
         int numBeans = 5;
         List<SortedMapBean> beans = new ArrayList<SortedMapBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             SortedMapBean bean = new SortedMapBean();
             bean.setRowkey(new Long(i));
             bean.setStrProp("str-" + i);
             keys.add(bean.getRowkey());
             
             bean.setMapProp(new TreeMap<String, Object>());
             for(int j = 50; j <= 100; j++)
                 bean.getMapProp().put("propval-" + j, "val-" + i + "-" + j);
             
             beans.add(bean);
         }
         
         _sortedMapDao.mput(beans);
         
         for(SortedMapBean bean : beans)
         {
             SortedMapBean loaded = _sortedMapDao.get(bean.getRowkey());
             assertTrue(((IEnhancedEntity) loaded).getModifiedFields().isEmpty());
             assertEquals(bean, loaded);
         }
         
         //bulk load
         List<SortedMapBean> loaded = new ArrayList<SortedMapBean>( _sortedMapDao.mget(keys));
         Collections.sort(loaded);
         for(SortedMapBean bean : beans)
         {
             assertTrue(((IEnhancedEntity) bean).getModifiedFields().isEmpty());
         }
         assertBeansEqual(beans, loaded);
 
         
         SortedMapBean bean0 = beans.get(0);
         //test null update
         for(int i = 50; i < 75; i++)
             bean0.getMapProp().put("propval-" + i, null);
         assertEquals(51, bean0.getMapProp().size()); //sanity check we are overwriting properties to null
         
         _sortedMapDao.put(bean0);
         
         Iterator<Entry<String, Object>> iter = bean0.getMapProp().entrySet().iterator();
         while(iter.hasNext())
         {
             if(iter.next().getValue() == null)
                 iter.remove();
         }
         
         assertEquals(bean0, _sortedMapDao.get(bean0.getRowkey()));
     }
 
     @Test
     public void testListGet()
     {
         int numBeans = 5;
         List<ListBean> beans = new ArrayList<ListBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             ListBean bean = new ListBean();
             bean.setRowkey(new Long(i));
             bean.setStrProp("str-" + i);
             keys.add(bean.getRowkey());
             bean.setListProp(new ArrayList<Object>());
             for(int j = 0; j <= 100; j++)
                 bean.getListProp().add("val-" + i + "-" + j);
             
             beans.add(bean);
         }
         
         _listDao.mput(beans);
         int expectedOps = 0;
         for(ListBean bean : beans)
         {
             ListBean loaded = _listDao.get(bean.getRowkey());
             assertTrue(((IEnhancedEntity) loaded).getModifiedFields().isEmpty());
             assertEquals(bean, loaded);
 
             assertEquals(++expectedOps, _listDao.getStats().getNumOps());
             assertEquals(102*expectedOps, _listDao.getStats().getNumCols());
             assertEquals(2*expectedOps, _listDao.getStats().getNumCassandraOps()); //large number of unmapped cols requires addl slice query per row
             assertEquals(expectedOps, _listDao.getStats().getRecentTimings().length);
         }
 
         /*
          * bulk load
          */
         //load all
         List<ListBean> loaded = new ArrayList<ListBean>( _listDao.mgetAll());
         Collections.sort(loaded);
         for(ListBean bean : beans)
         {
             assertTrue(((IEnhancedEntity) bean).getModifiedFields().isEmpty());
         }
         assertBeansEqual(beans, loaded);
         
         //load multiple
         loaded = new ArrayList<ListBean>( _listDao.mget(keys));
         Collections.sort(loaded);
         for(ListBean bean : beans)
         {
             assertTrue(((IEnhancedEntity) bean).getModifiedFields().isEmpty());
         }
         assertBeansEqual(beans, loaded);
 
         
         ListBean bean0 = beans.get(0);
         //test null update
         for(int i = 0; i < 50; i++)
             bean0.getListProp().set(i, null);
         
         _listDao.put(bean0);
         
         Iterator<Object> iter = bean0.getListProp().iterator();
         while(iter.hasNext())
         {
             if(iter.next() == null)
                 iter.remove();
         }
         
         assertEquals(bean0, _listDao.get(bean0.getRowkey()));
     }
 
     @Test 
     public void testNestedGet() throws Exception
     {
         List<NestedBean> beans = new ArrayList<NestedBean>();
         List<Long> keys = new ArrayList<Long>();
 
         int numBeans = 10;
         int numD1 = 5;
         int numD2 = 7;
         int numD3 = 3;
 
         for(long n = 0; n < numBeans; n++)
         {
             NestedBean bean = new NestedBean();
             bean.setRowkey(n);
             keys.add(bean.getRowkey());
             List<List<Double>> lol = new ArrayList<List<Double>>();
             List<Map<String, String>> lom = new ArrayList<Map<String, String>>();
             List<Map<Long, List<Double>>> lomol = new ArrayList<Map<Long,List<Double>>>();
             
             Map<String, Map<Integer, Integer>> mom = new HashMap<String, Map<Integer,Integer>>();
             Map<String, List<String>> mol = new HashMap<String, List<String>>();
             Map<String, List<Map<String, Date>>> molom = new HashMap<String, List<Map<String,Date>>>();
 
             for(int i = 0; i < numD1; i++)
             {
                 lol.add(new ArrayList<Double>());
                 lom.add(new HashMap<String, String>());
                 lomol.add(new HashMap<Long, List<Double>>());
                 
                 mom.put("key-" + i, new HashMap<Integer, Integer>());
                 mol.put("key-" + i, new ArrayList<String>());
                 molom.put("key-" + i, new ArrayList<Map<String,Date>>());
                 
                 for(int j = 0; j < numD2; j++)
                 {
                     lol.get(i).add(i*j*1.1);
                     lom.get(i).put("key-" + j, String.valueOf(i+j+1.1));
                     mom.get("key-" + i).put(j, i*j);
                     mol.get("key-" + i).add(i + "-" + j);
                     lomol.get(i).put(new Long(j), new ArrayList<Double>());
                     molom.get("key-" + i).add(new HashMap<String, Date>());
                     
                     for(int k = 0; k < numD3; k++)
                     {
                         lomol.get(i).get(new Long(j)).add(i+j+k+.33);
                         molom.get("key-" + i).get(j).put(i + "-" + j + "-" + k, new Date(System.currentTimeMillis() - i*j*k));
                     }
                 }
             }
             bean.setListOfListProp(lol);
             bean.setListOfMapProp(lom);
             bean.setListOfMapOfListProp(lomol);
             bean.setMapOfListProp(mol);
             bean.setMapOfMapProp(mom);
             bean.setMapOfListOfMapProp(molom);
             
             beans.add(bean);
         }
         
         _nestedBeanDao.mput(beans);
 
         assertEquals(1, _nestedBeanDao.putStats().getNumOps());
         assertEquals((35+35+35+35+35*3+35*3)*numBeans, _nestedBeanDao.putStats().getNumCols());
         assertEquals((35+35+35+35+35*3+35*3)*numBeans, _nestedBeanDao.putStats().getNumCassandraOps());
         assertEquals(numBeans, _nestedBeanDao.putStats().getNumRows());
         assertEquals(1, _nestedBeanDao.putStats().getRecentTimings().length);
 
         int expectedOps = 0;
         for(int i = 0; i < numBeans; i++)
         {
             NestedBean actual = _nestedBeanDao.get((long) i);
             NestedBean expected = beans.get(i);
             
             assertEquals("key " + expected.getRowkey(), expected.getListOfListProp(), actual.getListOfListProp());
             assertEquals("key " + expected.getRowkey(), expected.getListOfMapProp(), actual.getListOfMapProp());
             assertEquals("key " + expected.getRowkey(), expected.getListOfMapOfListProp(), actual.getListOfMapOfListProp());
 
             assertEquals("key " + expected.getRowkey(), expected.getMapOfListProp(), actual.getMapOfListProp());
             assertEquals("key " + expected.getRowkey(), expected.getMapOfMapProp(), actual.getMapOfMapProp());
             assertEquals("key " + expected.getRowkey(), expected.getMapOfListOfMapProp(), actual.getMapOfListOfMapProp());
 
             
             //this is actually sufficient, previous are to aid debugging inequalities
             assertEquals("key " + expected.getRowkey(), expected, actual);
             
             assertEquals(++expectedOps, _nestedBeanDao.getStats().getNumOps());
             assertEquals((35+35+35+35+35*3+35*3)*expectedOps, _nestedBeanDao.getStats().getNumCols());
             assertEquals(4*expectedOps, _nestedBeanDao.getStats().getNumCassandraOps()); //large number of unmapped cols requires addl slice query per row
             assertEquals(expectedOps, _nestedBeanDao.getStats().getRecentTimings().length);
 
         }
         
         List<NestedBean> actual = new ArrayList<NestedBean>(_nestedBeanDao.mget(keys));
         Collections.sort(actual);
         
         assertEquals(beans, actual);
         
         /*
          * test partials
          */
         for(long i = 0; i < numBeans; i++)
         {
             Set<String> includes = new HashSet<String>();
             includes.add("listOfListProp");
             
             GetOptions options = new GetOptions(includes, null);
             
             NestedBean bean = beans.get((int) i);
             NestedBean expected = new NestedBean();
             expected.setRowkey(bean.getRowkey());
             expected.setListOfListProp(bean.getListOfListProp());
             NestedBean actualBean = _nestedBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
 
             expected.setListOfMapProp(bean.getListOfMapProp());
             includes.add("listOfMapProp");
             actualBean = _nestedBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
 
             expected.setListOfMapOfListProp(bean.getListOfMapOfListProp());
             includes.add("listOfMapOfListProp");
             actualBean = _nestedBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
 
             expected.setMapOfMapProp(bean.getMapOfMapProp());
             includes.add("mapOfMapProp");
             actualBean = _nestedBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
 
             expected.setMapOfListProp(bean.getMapOfListProp());
             includes.add("mapOfListProp");
             actualBean = _nestedBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
 
             expected.setMapOfListOfMapProp(bean.getMapOfListOfMapProp());
             includes.add("mapOfListOfMapProp");
             actualBean = _nestedBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
         }
 
         /*
          * test partial ranges
          */
         for(long i = 0; i < numBeans; i++)
         {
             GetOptions options = new GetOptions(new CollectionProperty("listOfMapOfListProp", 2), new CollectionProperty("listOfMapOfListProp", 4));
             
             NestedBean bean = beans.get((int) i);
             NestedBean expected = new NestedBean();
             expected.setRowkey(bean.getRowkey());
             
             List<Map<Long,List<Double>>> subList = new ArrayList<Map<Long,List<Double>>>(bean.getListOfMapOfListProp().subList(2, 5));
             subList.add(0,  null);
             subList.add(0,  null);
             expected.setListOfMapOfListProp(subList);
             NestedBean actualBean = _nestedBeanDao.get(i, null, options);
             assertEquals(expected, actualBean);
             
             options = new GetOptions(new CollectionProperty("mapOfListOfMapProp", "key-2"), new CollectionProperty("mapOfListOfMapProp", "key-4"));
             expected.setMapOfListOfMapProp(new HashMap<String, List<Map<String,Date>>>());
             expected.getMapOfListOfMapProp().put("key-2", bean.getMapOfListOfMapProp().get("key-2"));
             expected.getMapOfListOfMapProp().put("key-3", bean.getMapOfListOfMapProp().get("key-3"));
             expected.getMapOfListOfMapProp().put("key-4", bean.getMapOfListOfMapProp().get("key-4"));
             _nestedBeanDao.get(i, actualBean, options);
             assertEquals(expected, actualBean);
         }
     }
     
     @Test
     public void testGetPartial() throws Exception
     {
         int numBeans = 5;
         List<SampleBean> beans = new ArrayList<SampleBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = new SampleBean();
             bean.setRowKey(new Long(i));
             bean.setBoolVal(i%2 == 0);
             bean.setCharVal((char) ('a' + i));
             bean.setDateVal(new Date(System.currentTimeMillis() + 60000*i));
             bean.setDoubleVal(i * .1);
             bean.setFloatVal(i / .5f);
             bean.setIntVal(i);
             bean.setLongVal(-i);
             bean.setStrVal("str-" + i);
             
             bean.setUnmapped(new HashMap<String, Object>());
             for(int j = 0; j <= 20; j++)
                 bean.getUnmapped().put("unmapped-" + j, "val-" + i + "-" + j);//place them between fixed properties
             
             beans.add(bean);
             keys.add(bean.getRowKey());
         }
 
         _dao.mput(beans);
 
         keys.add(-52345L);//non existent
         keys.add(2, -7234324324L);//non existent
         List<SampleBean> bulkActuals = _dao.mget(keys, null, new GetOptions(null, Collections.singleton("boolVal")));
         List<SampleBean> bulkAllActuals = new ArrayList<SampleBean>(_dao.mgetAll(new GetAllOptions(null, Collections.singleton("boolVal"))));
 
         assertEquals(keys.size(), bulkActuals.size());
         assertNull(bulkActuals.remove(bulkActuals.size()-1));
         assertNull(bulkActuals.remove(2));
         keys.remove(keys.size()-1);
         keys.remove(2);
         
         Collections.sort(bulkActuals);
         Collections.sort(bulkAllActuals);
         
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean saved = beans.get(i);
             SampleBean expected = (SampleBean) saved.clone();
             
             expected.setBoolVal(false); //false is default value for boolean
             expected.setUnmapped(null); //can't efficiently do exclusions and include unmapped columns right now as c* ranges are inclusive
             SampleBean actual = _dao.get(saved.getRowKey(), null, new GetOptions(null, Collections.singleton("boolVal")));
             
             assertEquals(expected, actual);
             assertEquals(expected, bulkActuals.get(i));
             assertEquals(expected, bulkAllActuals.get(i));
         }
         
         Set<String> props = new HashSet<String>();
         props.add("charVal");
         for(int j = 20; j >= 11; j--)
             props.add("unmapped-" + j);
         
         bulkActuals = _dao.mget(keys, null, new GetOptions(props, null));
         bulkAllActuals = new ArrayList<SampleBean>(_dao.mgetAll(new GetAllOptions(props, null)));
         Collections.sort(bulkActuals);
         Collections.sort(bulkAllActuals);
         assertEquals(numBeans, bulkActuals.size());
         assertEquals(numBeans, bulkAllActuals.size());
         
         List<SampleBean> expecteds = new ArrayList<SampleBean>();
         List<SampleBean> actuals = new ArrayList<SampleBean>();
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean saved = beans.get(i);
             SampleBean expected =  new SampleBean();
             expected.setRowKey(saved.getRowKey());
             expected.setCharVal(saved.getCharVal());
             TreeMap<String, Object> unmapped = new TreeMap<String, Object>(saved.getUnmapped());
             for(int j = 10; j >= 0; j--)
                 unmapped.remove("unmapped-" + j);
             
             expected.setUnmapped(unmapped);
             
             
             SampleBean actual = _dao.get(saved.getRowKey(), null, new GetOptions(props, null));
             assertEquals(expected, actual);
             assertEquals(expected, bulkActuals.get(i));
             assertEquals(expected, bulkAllActuals.get(i));
             
             expecteds.add(expected);
             actuals.add(expected);
         }
         
         _dao.mget(keys, bulkActuals, new GetOptions(Collections.singleton("intVal"), null));
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean saved = beans.get(i);
             SampleBean actual = actuals.get(i);
             SampleBean expected = expecteds.get(i);
             expected.setIntVal(saved.getIntVal());
             _dao.get(saved.getRowKey(), actual, new GetOptions(Collections.singleton("intVal"), null)); //update the bean
             
             assertEquals(expected, actual);
             assertEquals(expected, bulkActuals.get(i));
         }
         
     }
     
     @Test
     public void testGetPartialRange() throws Exception
     {
         int numBeans = 5;
         List<SampleBean> beans = new ArrayList<SampleBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             SampleBean bean = new SampleBean();
             bean.setRowKey(new Long(i));
             bean.setBoolVal(i%2 == 0);
             bean.setCharVal((char) ('a' + i));
             bean.setDateVal(new Date(System.currentTimeMillis() + 60000*i));
             bean.setDoubleVal(i * .1);
             bean.setFloatVal(i / .5f);
             bean.setIntVal(i);
             bean.setLongVal(-i);
             bean.setStrVal("str-" + i);
             
             bean.setUnmapped(new HashMap<String, Object>());
             for(int j = 0; j <= 20; j++)
                 bean.getUnmapped().put("unmapped-" + j, "val-" + i + "-" + j);
             
             beans.add(bean);
             keys.add(bean.getRowKey());
         }
         
         _dao.mput(beans);
         List<SampleBean> bulkAllActuals = new ArrayList<SampleBean>(_dao.mgetAll(new GetAllOptions("c", "cv")));
         List<SampleBean> bulkActuals = _dao.mget(keys, null, new GetOptions("c", "cv"));
         List<SampleBean> actuals = new ArrayList<SampleBean>();
         List<SampleBean> expecteds = new ArrayList<SampleBean>();
         Collections.sort(bulkAllActuals);
         Collections.sort(bulkActuals);
         
         for(int i = 0; i < beans.size(); i++)
         {
             SampleBean saved = beans.get(i);
             SampleBean expected = new SampleBean();
             
             expected.setRowKey(saved.getRowKey());
             expected.setCharVal(saved.getCharVal());
             expected.setUnmapped(null); //when using exclude, unmapped properties are ignored
             actuals.add(_dao.get(saved.getRowKey(), null, new GetOptions("c", "cv")));
             
             assertEquals(expected, actuals.get(i));
             assertEquals(expected, bulkActuals.get(i));
             assertEquals(expected, bulkAllActuals.get(i));
             
             expecteds.add(expected);
         }
 
         bulkActuals = _dao.mget(keys, bulkActuals, new GetOptions("d", "daa"));
         for(int i = 0; i < beans.size(); i++)
         {
             SampleBean saved = beans.get(i);
             SampleBean expected = expecteds.get(i);
             
             actuals.set(i, _dao.get(saved.getRowKey(), actuals.get(i), new GetOptions("d", "daa")));//includes double's physical name
             expected.setDoubleVal(saved.getDoubleVal());
             assertEquals(expected, actuals.get(i));
             assertEquals(expected, bulkActuals.get(i));
         }
 
         bulkActuals = _dao.mget(keys, bulkActuals, new GetOptions("unmapped-10", "unmapped-19"));
 
         for(int i = 0; i < beans.size(); i++)
         {
             SampleBean saved = beans.get(i);
             SampleBean expected = expecteds.get(i);
             
             expected.setUnmapped(new HashMap<String, Object>());
             for(int j = 10; j < 20; j++)
                 expected.getUnmapped().put("unmapped-" + j, saved.getUnmapped().get("unmapped-" + j));
 
             actuals.set(i, _dao.get(saved.getRowKey(), actuals.get(i), new GetOptions("unmapped-10", "unmapped-19")));
             assertEquals(expected, actuals.get(i));
             assertEquals(expected, bulkActuals.get(i));
         }
     }
     
 
     @Test
     public void testGetPartialCollectionRange() throws Exception
     {
         int numBeans = 5;
         List<MapBean> mapBeans = new ArrayList<MapBean>();
         List<ListBean> listBeans = new ArrayList<ListBean>();
         List<Long> keys = new ArrayList<Long>();
         
         for(int i = 0; i < numBeans; i++)
         {
             ListBean lbean = new ListBean();
             lbean.setRowkey(new Long(i));
             lbean.setStrProp("str-" + i);
             lbean.setStrProp1("str1-" + i);
             lbean.setListProp(new ArrayList<Object>());
             for(int j = 0; j <= 200; j++)
                 lbean.getListProp().add(i*1000 + j);
 
             MapBean mbean = new MapBean();
             mbean.setRowkey(new Long(i));
             mbean.setStrProp("str-" + i);
             mbean.setStrProp1("str1-" + i);
             mbean.setMapProp(new HashMap<String, Object>());
             for(int j = 0; j <= 200; j++)
                 mbean.getMapProp().put("key-" + j + "-" + i, i*1000 + j);
 
             mapBeans.add(mbean);
             listBeans.add(lbean);
             keys.add(lbean.getRowkey());
         }
 
         _listDao.mput(listBeans);
         _mapDao.mput(mapBeans);
         
 
         /*
          * lists
          */
         //do the same test using bulk API
         List<ListBean> bulkListActuals = _listDao.mget(keys, null, new GetOptions("strProp1", "strProp1"));
         assertEquals(numBeans, bulkListActuals.size());
         Collections.sort(bulkListActuals);
         List<ListBean> singleListActuals = new ArrayList<ListBean>();
         for(int i = 0; i < listBeans.size(); i++)
         {
             ListBean saved = listBeans.get(i);
             ListBean expected = new ListBean();
             expected.setRowkey(saved.getRowkey());
             expected.setStrProp1(saved.getStrProp1()); 
             
             singleListActuals.add(_listDao.get(saved.getRowkey(), null, new GetOptions("strProp1", "strProp1")));
 
             assertEquals(expected, singleListActuals.get(i));
             assertEquals(expected, bulkListActuals.get(i));
         }
 
         List<ListBean> bulkListAllActuals = new ArrayList<ListBean>(_listDao.mgetAll(new GetAllOptions(new CollectionProperty("listProp", 25),  
                                                                                                        new CollectionProperty("listProp", 175))));
         
         _listDao.mget(keys, bulkListActuals, new GetOptions(new CollectionProperty("listProp", 25),  new CollectionProperty("listProp", 175)));
         assertEquals(numBeans, bulkListActuals.size());
         assertEquals(numBeans, bulkListAllActuals.size());
 
         Collections.sort(bulkListAllActuals);
         for(int i = 0; i < listBeans.size(); i++)
         {
             ListBean saved = listBeans.get(i);
 
             ListBean expected = new ListBean();
             expected.setRowkey(saved.getRowkey());
             expected.setStrProp1(saved.getStrProp1()); 
             expected.setListProp(new ArrayList<Object>());
             for(int j = 0; j <= 175; j++)
                 expected.getListProp().add(j < 25 ? null : saved.getListProp().get(j));
 
             _listDao.get(saved.getRowkey(), singleListActuals.get(i), new GetOptions(new CollectionProperty("listProp", 25),  new CollectionProperty("listProp", 175)));
             
             assertEquals(expected, singleListActuals.get(i));
             assertEquals(expected, bulkListActuals.get(i));
 
             expected.setStrProp1(null);
             assertEquals(expected, bulkListAllActuals.get(i));
         }
         
         /*
          * maps
          */
         
         //do the same test using bulk API
         List<MapBean> bulkMapActuals = _mapDao.mget(keys, null, new GetOptions("strProp1", "strProp1"));
         List<MapBean> mapActuals = new ArrayList<MapBean>();
         List<MapBean> expectedMaps = new ArrayList<MapBean>();
         
         assertEquals(numBeans, bulkMapActuals.size());
         Collections.sort(bulkMapActuals);
         
         for(int i = 0; i < mapBeans.size(); i++)
         {
             MapBean saved = mapBeans.get(i);
             MapBean expected = new MapBean();
             expected.setRowkey(saved.getRowkey());
             expected.setStrProp1(saved.getStrProp1()); 
 
             MapBean actual = _mapDao.get(saved.getRowkey(), null, new GetOptions("strProp1", "strProp1"));
             
             assertEquals(expected, actual);
             assertEquals(expected, bulkMapActuals.get(i));
 
             mapActuals.add(actual);
             expectedMaps.add(expected);
         }
         
         
         /*
          * load a range from within a collection
          */
         String start = "key-100", end = "key-201";
         List<MapBean> bulkMapAllActuals = 
                 new ArrayList<MapBean>(_mapDao.mgetAll(new GetAllOptions(new CollectionProperty("mapProp", start), new CollectionProperty("mapProp", end))));
         bulkMapActuals = _mapDao.mget(keys, mapActuals, 
                                       new GetOptions(new CollectionProperty("mapProp", start), new CollectionProperty("mapProp", end)));
 
         assertEquals(numBeans, bulkMapActuals.size());
         assertEquals(numBeans, bulkMapAllActuals.size());
         
         Collections.sort(bulkMapAllActuals);
         for(int i = 0; i < mapBeans.size(); i++)
         {
             MapBean saved = mapBeans.get(i);
             MapBean expected = expectedMaps.get(i);
             mapActuals.set(i, _mapDao.get(saved.getRowkey(), mapActuals.get(i), 
                                           new GetOptions(new CollectionProperty("mapProp", start), new CollectionProperty("mapProp", end))));
             
             expected.setRowkey(saved.getRowkey());
             expected.setMapProp(new HashMap<String, Object>());
             for(Object o : saved.getMapProp().entrySet())
             {
                 Map.Entry<String, Object> e = (Map.Entry<String, Object>) o;
                 String k = e.getKey();
                 if(k.compareTo(start) >= 0 && k.compareTo(end) <= 0)
                     expected.getMapProp().put(k, e.getValue());
                     
             }
             
             assertEquals(expected, mapActuals.get(i));
             assertEquals(expected, bulkMapActuals.get(i));
             
             expected.setStrProp1(null);
             assertEquals(expected, bulkMapAllActuals.get(i));
         }
     }
 
     @Test
     public void testHashFind() throws Exception
     {
         int numBeans = 2*CassandraDaoBase.ROW_RANGE_SIZE+1;//force dao to do multiple ranges
         List<IndexedBean> idxBeans = new ArrayList<IndexedBean>();
         List<CompositeIndexedBean> cIdxBeans = new ArrayList<CompositeIndexedBean>();
         for(int i = 0; i < numBeans; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(new Long(i));
             idxBean.setCharVal('a');
             idxBean.setIntVal(i/10);
             idxBean.setIntVal2(i/2);
             idxBean.setLongVal(i/10L);
             idxBean.setStrVal("strval");
             idxBean.setStrVal2(null);
             
             idxBeans.add(idxBean);
 
             CompositeIndexedBean cIdxBean = new CompositeIndexedBean();
             cIdxBean.setRowKey(new Long(i));
             cIdxBean.setCharVal('a');
             cIdxBean.setIntVal(i/10);
             cIdxBean.setLongVal(i/10);
             cIdxBean.setStrVal("strval");
             
             cIdxBeans.add(cIdxBean);
         }
         
         _indexedDao.mput(idxBeans);
         _compositeIndexedDao.mput(cIdxBeans);
         
         List<IndexedBean> idxActuals;
         IndexedBean idxTmpl = new IndexedBean();
         idxTmpl.setIntVal(5);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
         
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(50, 60), idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
 
         FindOptions options = new FindOptions();
         options.setMaxRows(5); //ensure max rows honored
         assertEquals(5, _indexedDao.mfind(idxTmpl, options).size());
 
         options.setMaxRows(CassandraDaoBase.ROW_RANGE_SIZE*2); 
         assertEquals(10, _indexedDao.mfind(idxTmpl, options).size());
 
         
         CompositeIndexedBean cIdxTmpl = new CompositeIndexedBean();
         cIdxTmpl.setIntVal(5);
         List<CompositeIndexedBean> cIdxActuals = new ArrayList<CompositeIndexedBean>(_compositeIndexedDao.mfind(cIdxTmpl));
 
         Collections.sort(cIdxActuals);
         assertBeansEqual(cIdxBeans.subList(50, 60), cIdxActuals);
         for(CompositeIndexedBean cIdxBean : cIdxActuals)
             assertTrue(((IEnhancedEntity) cIdxBean).getModifiedFields().isEmpty());
 
         _indexedDao.hashFindStats().reset();
         _indexedDao.hashFindIndexStats().reset();
         
         idxTmpl = new IndexedBean();
         idxTmpl.setStrVal("strval");
         Collection<IndexedBean> actualColl = _indexedDao.mfind(idxTmpl);
         idxActuals = new ArrayList<IndexedBean>();
 
         assertEquals(1, _indexedDao.hashFindStats().getNumOps());
         assertEquals(0, _indexedDao.hashFindStats().getNumCassandraOps());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE*5, _indexedDao.hashFindStats().getNumCols());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE, _indexedDao.hashFindStats().getNumRows());
         assertEquals(1, _indexedDao.hashFindStats().getRecentTimings().length);
 
         assertEquals(1, _indexedDao.hashFindIndexStats().getNumOps());
         assertEquals(1, _indexedDao.hashFindIndexStats().getNumCassandraOps());
         assertEquals(0, _indexedDao.hashFindIndexStats().getNumCols());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE, _indexedDao.hashFindIndexStats().getNumRows());
         assertEquals(1, _indexedDao.hashFindIndexStats().getRecentTimings().length);
         
         for(IndexedBean b : actualColl) //test incremental iteration rather than all() method
             idxActuals.add(b);
 
         assertEquals(1, _indexedDao.hashFindStats().getNumOps());
         assertEquals(0, _indexedDao.hashFindStats().getNumCassandraOps());
         assertEquals(numBeans*5, _indexedDao.hashFindStats().getNumCols());
         assertEquals(numBeans, _indexedDao.hashFindStats().getNumRows());
         assertEquals(3, _indexedDao.hashFindStats().getRecentTimings().length);
 
         assertEquals(1, _indexedDao.hashFindIndexStats().getNumOps());
         assertEquals(3, _indexedDao.hashFindIndexStats().getNumCassandraOps());
         assertEquals(0, _indexedDao.hashFindIndexStats().getNumCols());
         assertEquals(numBeans, _indexedDao.hashFindIndexStats().getNumRows());
         assertEquals(3, _indexedDao.hashFindIndexStats().getRecentTimings().length);
 
         
         
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans, idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
 
         //ensure subsequent iteration also works
         idxActuals.clear();
         
         for(IndexedBean b : actualColl)
             idxActuals.add(b);
 
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans, idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
 
         //now update a single bean so that the fetched range is exactly the batch size
         IndexedBean upd = idxBeans.get(0);
         upd.setStrVal("strval-updated");
         _indexedDao.put(upd);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
 
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(1, numBeans), idxActuals);
         
         
         cIdxTmpl = new CompositeIndexedBean();
         cIdxTmpl.setStrVal("strval");
         cIdxActuals = new ArrayList<CompositeIndexedBean>(_compositeIndexedDao.mfind(cIdxTmpl));
         
         Collections.sort(cIdxActuals);
         assertBeansEqual(cIdxBeans, cIdxActuals);
         for(CompositeIndexedBean cIdxBean : cIdxActuals)
             assertTrue(((IEnhancedEntity) cIdxBean).getModifiedFields().isEmpty());
 
         
         /*
          * test non-indexed property filtering
          */
         idxTmpl = new IndexedBean();
         idxTmpl.setIntVal(5);
         idxTmpl.setIntVal2(27);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
         
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(54, 56), idxActuals);
         
         for(int i = 0; i < numBeans; i++)
         {
             IndexedBean idxBean = idxBeans.get(i);
             idxBean.setIntVal(0);
             idxBean.setIntVal2(i == 140 ? 1 : 0);
             idxBeans.add(idxBean);
         }
         _indexedDao.mput(idxBeans);
         idxTmpl.setIntVal(0);
         idxTmpl.setIntVal2(0);
         List<IndexedBean> expecteds = new ArrayList<IndexedBean>(idxBeans.subList(0, 140));
         expecteds.addAll(idxBeans.subList(141, numBeans));
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
         Collections.sort(idxActuals);
         assertBeansEqual(expecteds, idxActuals);
     }
     
     @Test
     public void testHashFindPartialColRange() throws Exception
     {
         int numBeans = CassandraDaoBase.ROW_RANGE_SIZE+1;//force dao to do multiple ranges
         List<IndexedBean> idxBeans = new ArrayList<IndexedBean>();
         List<CompositeIndexedBean> cIdxBeans = new ArrayList<CompositeIndexedBean>();
         for(int i = 0; i < numBeans; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(new Long(i));
             idxBean.setCharVal('c');
             idxBean.setIntVal(i/10);
             idxBean.setLongVal((long) i);
             idxBean.setStrVal("strval");
             idxBean.setStrVal2(null);
             
             idxBeans.add(idxBean);
 
             CompositeIndexedBean cIdxBean = new CompositeIndexedBean();
             cIdxBean.setRowKey(new Long(i));
             cIdxBean.setCharVal('c');
             cIdxBean.setIntVal(i/10);
             cIdxBean.setLongVal(i);
             cIdxBean.setStrVal("strval");
             
             cIdxBeans.add(cIdxBean);
         }
         
         _indexedDao.mput(idxBeans);
         _compositeIndexedDao.mput(cIdxBeans);
         
         IndexedBean idxTmpl = new IndexedBean();
         idxTmpl.setIntVal(5);
         List<IndexedBean> idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl, new FindOptions("longVal", "t")));
         List<IndexedBean> idxExpecteds = new ArrayList<IndexedBean>();
 
         Collections.sort(idxActuals);
         for(int i = 50; i < 60; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(idxBeans.get(i).getRowKey());
             idxBean.setLongVal(idxBeans.get(i).getLongVal());
             idxBean.setStrVal(idxBeans.get(i).getStrVal());
             
             idxExpecteds.add(idxBean);
         }
         
         assertBeansEqual(idxExpecteds, idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
 
         CompositeIndexedBean cIdxTmpl = new CompositeIndexedBean();
         cIdxTmpl.setIntVal(5);
         List<CompositeIndexedBean> cIdxActuals = new ArrayList<CompositeIndexedBean>(_compositeIndexedDao.mfind(cIdxTmpl, new FindOptions("longVal", "t")));
         List<CompositeIndexedBean> cIdxExpecteds = new ArrayList<CompositeIndexedBean>();
 
         for(int i = 50; i < 60; i++)
         {
             CompositeIndexedBean idxBean = new CompositeIndexedBean();
             idxBean.setRowKey(idxBeans.get(i).getRowKey());
             idxBean.setLongVal(idxBeans.get(i).getLongVal());
             idxBean.setStrVal(idxBeans.get(i).getStrVal());
             
             cIdxExpecteds.add(idxBean);
         }
         
         Collections.sort(cIdxActuals);
         assertBeansEqual(cIdxExpecteds, cIdxActuals);
         for(CompositeIndexedBean cIdxBean : cIdxActuals)
             assertTrue(((IEnhancedEntity) cIdxBean).getModifiedFields().isEmpty());
         
         /*
          * try collection properties
          */
         List<ListBean> listBeans = new ArrayList<ListBean>();
         List<Object> coll = new ArrayList<Object>();
         for(int i = 0; i < 20; i++)
             coll.add(i);
         
         for(int i = 0; i < numBeans; i++)
         {
             ListBean bean = new ListBean();
             bean.setRowkey(new Long(i));
             bean.setStrProp("s" + i/10);
             bean.setStrProp1("s2");
             bean.setListProp(new ArrayList<Object>());
             bean.setListProp(coll);
             
             listBeans.add(bean);
         }
         _listDao.mput(listBeans);
 
         ListBean lTmpl = new ListBean();
         lTmpl.setStrProp("s5");
         
         
         /*
          * partial list
          */
         int startIdx = 4, endIdx = 15;
         FindOptions options = new FindOptions(new CollectionProperty("listProp", startIdx), new CollectionProperty("listProp", endIdx));
         List<ListBean> listActuals = new ArrayList<ListBean>( _listDao.mfind(lTmpl, options) );
         Collections.sort(listActuals);
         assertEquals(10, listActuals.size());
         
         List<Object> expectedColl = new ArrayList<Object>();
         for(int i = 0; i <= endIdx; i++)
             expectedColl.add(i < startIdx ? null : i);
         
         for(int i = 50; i < 60; i++)
         {
             ListBean expected = new ListBean();
             expected.setRowkey(listBeans.get(i).getRowkey());
             expected.setListProp(expectedColl);
             assertEquals(expected, listActuals.get(i-50));
         }
 
         /*
          * full list
          */
         options = new FindOptions(Collections.singleton("listProp"), null);
         listActuals = new ArrayList<ListBean>( _listDao.mfind(lTmpl, options) );
         Collections.sort(listActuals);
         assertEquals(10, listActuals.size());
         for(int i = 0; i < 10; i++)
         {
             ListBean expected = new ListBean();
             expected.setRowkey(listBeans.get(50+i).getRowkey());
             expected.setListProp(listBeans.get(50+i).getListProp());
             assertEquals(expected, listActuals.get(i));
         }
 
         //delete a list value. key remains in the DB, ensure no value is returned...
         ListBean lb = listBeans.get(55);
         int cnt = lb.getListProp().size();
         lb.setListProp(new ArrayList<Object>());
         for(int i = 0; i < cnt; i++)
             lb.getListProp().add(null);
         
         _listDao.put(lb);
 
         listActuals = new ArrayList<ListBean>( _listDao.mfind(lTmpl, options) );
         Collections.sort(listActuals);
         assertEquals(9, listActuals.size());
         for(int i = 0; i < 10; i++)
         {
             if(i == 5)
                 continue; //deleted value
             
             ListBean expected = new ListBean();
             expected.setRowkey(listBeans.get(50+i).getRowkey());
             expected.setListProp(listBeans.get(50+i).getListProp());
             assertEquals(expected, listActuals.get(i < 5 ? i : i - 1));
         }
 
     }
     
     @Test
     public void testHashFindPartial() throws Exception
     {
         int numBeans = CassandraDaoBase.ROW_RANGE_SIZE+1;//force dao to do multiple ranges
         List<IndexedBean> idxBeans = new ArrayList<IndexedBean>();
         List<CompositeIndexedBean> cIdxBeans = new ArrayList<CompositeIndexedBean>();
         for(int i = 0; i < numBeans; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(new Long(i));
             idxBean.setCharVal('c');
             idxBean.setIntVal(i/10);
             idxBean.setLongVal((long) i);
             idxBean.setStrVal("strval");
             idxBean.setStrVal2(null);
 
             idxBeans.add(idxBean);
             
             CompositeIndexedBean cIdxBean = new CompositeIndexedBean();
             cIdxBean.setRowKey(new Long(i));
             cIdxBean.setCharVal('c');
             cIdxBean.setIntVal(i/10);
             cIdxBean.setLongVal(i);
             cIdxBean.setStrVal("strval");
             
             cIdxBeans.add(cIdxBean);
         }
         
         _indexedDao.mput(idxBeans);
         _compositeIndexedDao.mput(cIdxBeans);
         
         IndexedBean idxTmpl = new IndexedBean();
         idxTmpl.setIntVal(5);
         List<IndexedBean> idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl, new FindOptions(Collections.singleton("longVal"), null)));
         List<IndexedBean> idxExpecteds = new ArrayList<IndexedBean>();
         
         Collections.sort(idxActuals);
         for(int i = 50; i < 60; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(idxBeans.get(i).getRowKey());
             idxBean.setLongVal(idxBeans.get(i).getLongVal());
             
             idxExpecteds.add(idxBean);
         }
         
         assertBeansEqual(idxExpecteds, idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
         
         CompositeIndexedBean cIdxTmpl = new CompositeIndexedBean();
         cIdxTmpl.setIntVal(5);
         FindOptions options = new FindOptions(null, Collections.singleton("intVal"));
         List<CompositeIndexedBean> cIdxActuals = new ArrayList<CompositeIndexedBean>(_compositeIndexedDao.mfind(cIdxTmpl, options));
         List<CompositeIndexedBean> cIdxExpecteds = new ArrayList<CompositeIndexedBean>();
         
         for(int i = 50; i < 60; i++)
         {
             CompositeIndexedBean idxBean = new CompositeIndexedBean();
             idxBean.setRowKey(idxBeans.get(i).getRowKey());
             idxBean.setCharVal(idxBeans.get(i).getCharVal());
             idxBean.setLongVal(idxBeans.get(i).getLongVal());
             idxBean.setStrVal(idxBeans.get(i).getStrVal());
             
             cIdxExpecteds.add(idxBean);
         }
         
         Collections.sort(cIdxActuals);
         assertBeansEqual(cIdxExpecteds, cIdxActuals);
         for(CompositeIndexedBean cIdxBean : cIdxActuals)
             assertTrue(((IEnhancedEntity) cIdxBean).getModifiedFields().isEmpty());
         
         /*
          * try collection properties
          */
         List<ListBean> listBeans = new ArrayList<ListBean>();
         List<Object> coll = new ArrayList<Object>();
         for(int i = 0; i < 20; i++)
             coll.add(i);
         
         for(int i = 0; i < numBeans; i++)
         {
             ListBean bean = new ListBean();
             bean.setRowkey(new Long(i));
             bean.setStrProp("s" + i/10);
             bean.setStrProp1("s2");
             bean.setListProp(new ArrayList<Object>());
             bean.setListProp(coll);
             
             listBeans.add(bean);
         }
         _listDao.mput(listBeans);
         
         ListBean lTmpl = new ListBean();
         lTmpl.setStrProp("s5");
         int startIdx = 4, endIdx = 15;
         Set<Object> includes = new HashSet<Object>();
         for(int i = startIdx; i <= endIdx; i++)
             includes.add(new CollectionProperty("listProp", i));
         
         List<ListBean> listActuals = new ArrayList<ListBean>( _listDao.mfind(lTmpl, new FindOptions(includes, null)) );
         Collections.sort(listActuals);
         assertEquals(10, listActuals.size());
         
         List<Object> expectedColl = new ArrayList<Object>();
         for(int i = 0; i <= endIdx; i++)
             expectedColl.add(i < startIdx ? null : i);
         
         for(int i = 50; i < 60; i++)
         {
             ListBean expected = new ListBean();
             expected.setRowkey(listBeans.get(i).getRowkey());
             expected.setListProp(expectedColl);
             assertEquals(expected, listActuals.get(i-50));
         }
     }
 
     @Test
     public void testHashFindCounters() throws Exception
     {
         int numBeans = 6;
         List<ParentCounterBean> beans = new ArrayList<ParentCounterBean>();
         List<Long> keys = new ArrayList<Long>();
         for(int i = 0; i < numBeans; i++)
         {
             ParentCounterBean bean = createParentCounterBean(i);
             bean.setStrProp("str-" + i/3);
             beans.add(bean);
 
             bean = createParentCounterBean(i); //use a different bean, saving a counter resets the increment
             bean.setStrProp("str-" + i/3);
             _parentCounterDao.put(bean); 
             
             keys.add((long) i);
         }
 
         ParentCounterBean tmpl = new ParentCounterBean();
         tmpl.setStrProp(beans.get(0).getStrProp());
         
         List<ParentCounterBean> expectedBeans = beans.subList(0, 3);
         List<ParentCounterBean> actualBeans; 
         
         actualBeans = new ArrayList<ParentCounterBean>(_parentCounterDao.mfind(tmpl));
         
         Collections.sort(actualBeans);
         
         //convert counters to stored for easy comparison
         for(int i = 0; i < numBeans; i++)
             convertParentCounterBean(beans.get(i));
 
 
         for(int i = 0; i < expectedBeans.size(); i++)
         {
             ParentCounterBean expected = expectedBeans.get(i), actual = actualBeans.get(i);
             assertEquals("bean-" + i, expected, actual);
         }
 
         
         /*
          * range
          */
         actualBeans = new ArrayList<ParentCounterBean>();
         actualBeans.addAll(_parentCounterDao.mfind(tmpl, new FindOptions(new CollectionProperty("listProp", 1), new CollectionProperty("listProp", 3))));
 
         Collections.sort(actualBeans);
         
         expectedBeans = beans.subList(1, 3); //bean 0 doesn't have any elements in listProp
         assertEquals(expectedBeans.size(), actualBeans.size());
         for(int i = 0; i < expectedBeans.size(); i++) 
         {
             ParentCounterBean pbean = expectedBeans.get(i), actual = actualBeans.get(i);
             assertEquals(pbean.getRowkey(), actual.getRowkey());
             assertNull(actual.getCounterProp());
             assertNull(actual.getEmbeddedProp());
             assertNull(actual.getMapProp());
             assertNull(actual.getStrProp());
             if(i >= 1)
             {
                 for(int j = 1; j <= Math.min(i, 3); j++)
                     assertEquals(pbean.getListProp().get(j), actual.getListProp().get(j));
             }
         }
         
         /*
          * includes/excludes
          */
         
         Set<Object> includes = new HashSet<Object>();
         includes.add("counterProp");
         includes.add("strProp");
         includes.add(new CollectionProperty("embeddedProp", "s"));
         includes.add("listProp");
 
         actualBeans = new ArrayList<ParentCounterBean>(_parentCounterDao.mfind(tmpl, new FindOptions(includes, null)));
         expectedBeans = beans.subList(0, 3);
         assertEquals(expectedBeans.size(), actualBeans.size());
         
         Collections.sort(actualBeans);
 
         for(int i = 0; i < expectedBeans.size(); i++)
         {
             ParentCounterBean bean = expectedBeans.get(i), actual = actualBeans.get(i);
             bean.getEmbeddedProp().setCounterProp(null);
             bean.setMapProp(null);
             assertEquals(bean, actual);
         }
     }
     
     @Test
     public void testRangeIndexUpdate() throws Exception
     {
         IndexedBean idxBean = new IndexedBean();
         idxBean.setRowKey(0L);
         idxBean.setLongVal(100L);
         idxBean.setStrVal("sv");
         idxBean.setStrVal2("sv2");
         _indexedDao.put(idxBean);
         
         idxBean.setStrVal("sv");
         idxBean.setStrVal2("sv2");
         idxBean.setLongVal(200L);
         _indexedDao.put(idxBean);
 
         IndexedBean start = new IndexedBean(), end = new IndexedBean();
         
         //this range will get both values of the index, the stale one and the correct one
         start.setLongVal(0L);
         end.setLongVal(500L);
         
         Collection<IndexedBean> actuals = _indexedDao.mfindBetween(start, end);
         assertEquals(1, actuals.size());
         assertEquals(200L, actuals.iterator().next().getLongVal());
         
         assertEquals(1, _indexedStrategy.records.size());
         assertEquals(1, _indexedStrategy.records.get(0).values.size());
         assertEquals(100L, _indexedStrategy.records.get(0).values.iterator().next().getColumnName().get(0));
 
         _indexedStrategy.records.clear();
         
         idxBean.setStrVal("sv");
         idxBean.setStrVal2("sv2");
         idxBean.setLongVal(300L);
         _indexedDao.put(idxBean);
 
         _indexedDao.delete(idxBean.getRowKey());
 
         assertEquals(0, _indexedDao.mfindBetween(start, end).size());
         assertEquals(1, _indexedStrategy.records.size());
         assertEquals(3, _indexedStrategy.records.get(0).values.size()); //strategy is not deleting values from index
         
         Iterator<StaleIndexValue> iterator = _indexedStrategy.records.get(0).values.iterator();
         assertEquals(100L, iterator.next().getColumnName().get(0));
         assertEquals(200L, iterator.next().getColumnName().get(0));
         assertEquals(300L, iterator.next().getColumnName().get(0));
     }
     
     @Test
     public void testRangeIndexFind() throws Exception
     {
         int numBeans = CassandraDaoBase.ROW_RANGE_SIZE;
         List<IndexedBean> idxBeans = new ArrayList<IndexedBean>();
         List<CompositeIndexedBean> cIdxBeans = new ArrayList<CompositeIndexedBean>();
         for(int i = 0; i < numBeans; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(new Long(i));
             idxBean.setCharVal('c');
             idxBean.setIntVal(i);
             idxBean.setIntVal2(i);
             idxBean.setLongVal(i/10L);
             idxBean.setStrVal("strval");
             idxBean.setStrVal2(null);
 
             idxBeans.add(idxBean);
 
             CompositeIndexedBean cIdxBean = new CompositeIndexedBean();
             cIdxBean.setCharVal('c');
             cIdxBean.setRowKey(new Long(i));
             cIdxBean.setIntVal(i);
             cIdxBean.setLongVal(i/10);
             cIdxBean.setStrVal("strval");
             
             cIdxBeans.add(cIdxBean);
         }
         
         _indexedDao.mput(idxBeans);
         _compositeIndexedDao.mput(cIdxBeans);
         
         assertEquals(1, _indexedDao.putIndexStats().getNumOps());
         assertEquals(3*numBeans, _indexedDao.putIndexStats().getNumCols());
         assertEquals(3*numBeans+2, _indexedDao.putIndexStats().getNumCassandraOps());
         assertEquals(numBeans, _indexedDao.putIndexStats().getNumRows());
 
         
         IndexedBean idxTmpl = new IndexedBean();
         idxTmpl.setLongVal(5L);
         List<IndexedBean> idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
 
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(50, 60), idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
 
         idxTmpl = new IndexedBean();
         idxTmpl.setLongVal(5L);
         idxTmpl.setIntVal2(55); //add post fetch filter...
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
         assertBeansEqual(Collections.singletonList(idxBeans.get(55)), idxActuals);
         
         CompositeIndexedBean cIdxTmpl = new CompositeIndexedBean();
         cIdxTmpl.setLongVal(5);
         List<CompositeIndexedBean> cIdxActuals = new ArrayList<CompositeIndexedBean>(_compositeIndexedDao.mfind(cIdxTmpl));
 
         Collections.sort(cIdxActuals);
         assertBeansEqual(cIdxBeans.subList(50, 60), cIdxActuals);
         for(CompositeIndexedBean cIdxBean : cIdxActuals)
             assertTrue(((IEnhancedEntity) cIdxBean).getModifiedFields().isEmpty());
 
         
         assertEquals(0, _indexedStrategy.records.size());
         assertEquals(0, _compositeStrategy.records.size());
         
         int idx = 50;
         for(IndexedBean bean : idxBeans.subList(50, 55))
         {
             bean.setStrVal2(null);
             bean.setLongVal(idx++ % 2 == 0 ? -1L : null);
         }
         
         _indexedDao.mput(idxBeans.subList(50, 55));
         idxTmpl = new IndexedBean();
         idxTmpl.setLongVal(5L);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(55, 60), idxActuals);
         assertEquals(1, _indexedStrategy.records.size());
         
         StaleIndexUpdateRecord record = _indexedStrategy.records.get(0);
         DynamicCompositeSerializer compositeSer = new DynamicCompositeSerializer();
 
         Set<Long> actualStaleRowKeys = new HashSet<Long>();
         Set<Long> expectedStaleRowKeys = new HashSet<Long>();
         /*
          * validate the clock values
          */
         for(StaleIndexValue stale : record.values)
         {
             actualStaleRowKeys.add((Long) stale.getColumnName().get(1));
             
             SliceQuery<DynamicComposite,DynamicComposite,byte[]> query = 
                     HFactory.createSliceQuery(keyspace, compositeSer, compositeSer, BytesArraySerializer.get());
 
             query.setKey(stale.getRowKey());
             query.setColumnNames(stale.getColumnName());
             query.setColumnFamily("indexedbean_idx");
             List<HColumn<DynamicComposite,byte[]>> columns = query.execute().get().getColumns();
             assertEquals(1, columns.size());
             assertEquals(stale.getClock(), columns.get(0).getClock());
         }
         assertEquals(5, record.values.size());
         for(IndexedBean i : idxBeans.subList(50, 55))
             expectedStaleRowKeys.add(i.getRowKey());
         
         assertEquals(expectedStaleRowKeys, actualStaleRowKeys);
         
         /*
          * do a between find
          */
         IndexedBean endIdxTmpl = new IndexedBean();
         endIdxTmpl.setLongVal(6L);
 
         idxTmpl = new IndexedBean();
         idxTmpl.setLongVal(5L);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfindBetween(idxTmpl, endIdxTmpl));
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(55, 70), idxActuals);
      
         assertEquals(2, _indexedStrategy.records.size());
         
         record = _indexedStrategy.records.get(1);
         assertEquals(5, record.values.size());
 
         actualStaleRowKeys.clear();
         for(StaleIndexValue stale : record.values)
             actualStaleRowKeys.add((Long) stale.getColumnName().get(1));
         
         assertEquals(expectedStaleRowKeys, actualStaleRowKeys);
         
         idxTmpl = new IndexedBean();
         idxTmpl.setLongVal(5L);
         idxTmpl.setIntVal(58);
         endIdxTmpl = new IndexedBean();
         endIdxTmpl.setLongVal(6L);
         endIdxTmpl.setIntVal(63);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfindBetween(idxTmpl, endIdxTmpl));
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(58, 64), idxActuals);
         
         CompositeIndexedBean endCIdxTmpl = new CompositeIndexedBean();
         endCIdxTmpl.setLongVal(6);
         
         cIdxActuals = new ArrayList<CompositeIndexedBean>(_compositeIndexedDao.mfindBetween(cIdxTmpl, endCIdxTmpl));
         Collections.sort(cIdxActuals);
         assertBeansEqual(cIdxBeans.subList(50, 70), cIdxActuals);
     }
       
 
     
 
     @Test
     public void testRangeFindPartial() throws Exception
     {
         int numBeans = 1 + CassandraDaoBase.ROW_RANGE_SIZE * 3;//force dao to do multiple ranges
         List<IndexedBean> idxBeans = new ArrayList<IndexedBean>();
         for(int i = 0; i < numBeans; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(new Long(i));
             idxBean.setCharVal('c');
             idxBean.setIntVal(i);
             idxBean.setLongVal((long) Math.min(i/CassandraDaoBase.COL_RANGE_SIZE, 2));
             idxBean.setStrVal("strval");
             idxBean.setStrVal2(null);
 
             idxBeans.add(idxBean);
 
         }
 
         _indexedDao.mput(idxBeans);
 
         IndexedBean idxTmpl = new IndexedBean();
         idxTmpl.setLongVal(2L);
         Collection<IndexedBean> collection = _indexedDao.mfind(idxTmpl, new FindOptions(Collections.singleton("intVal"), null));
         
         assertEquals(1, _indexedDao.rangeFindStats().getNumOps());
         assertEquals(1, _indexedDao.rangeFindStats().getNumCassandraOps());
         assertEquals(2*CassandraDaoBase.ROW_RANGE_SIZE, _indexedDao.rangeFindStats().getNumCols());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE, _indexedDao.rangeFindStats().getNumRows());
         assertEquals(1, _indexedDao.rangeFindStats().getRecentTimings().length);
 
         assertEquals(1, _indexedDao.rangeFindIndexStats().getNumOps());
         assertEquals(1, _indexedDao.rangeFindIndexStats().getNumCassandraOps());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE, _indexedDao.rangeFindIndexStats().getNumCols());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE, _indexedDao.rangeFindIndexStats().getNumRows());
         assertEquals(1, _indexedDao.rangeFindIndexStats().getRecentTimings().length);
         
         List<IndexedBean> idxActuals = new ArrayList<IndexedBean>(collection);
 
         assertEquals(2, _indexedDao.rangeFindStats().getNumOps());
         assertEquals(2, _indexedDao.rangeFindStats().getNumCassandraOps());
         assertEquals(2*(CassandraDaoBase.ROW_RANGE_SIZE+1), _indexedDao.rangeFindStats().getNumCols());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE+1, _indexedDao.rangeFindStats().getNumRows());
         assertEquals(2, _indexedDao.rangeFindStats().getRecentTimings().length);
 
         assertEquals(1, _indexedDao.rangeFindIndexStats().getNumOps());
         assertEquals(2, _indexedDao.rangeFindIndexStats().getNumCassandraOps());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE+1, _indexedDao.rangeFindIndexStats().getNumCols());
         assertEquals(CassandraDaoBase.ROW_RANGE_SIZE+1, _indexedDao.rangeFindIndexStats().getNumRows());
         assertEquals(2, _indexedDao.rangeFindIndexStats().getRecentTimings().length);
 
         List<IndexedBean> idxExpecteds = new ArrayList<IndexedBean>();
 
         Collections.sort(idxActuals);
         for(int i = 100; i < 301; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(idxBeans.get(i).getRowKey());
             idxBean.setIntVal(idxBeans.get(i).getIntVal());
             idxBean.setLongVal(idxBeans.get(i).getLongVal());
 
             idxExpecteds.add(idxBean);
         }
 
         assertBeansEqual(idxExpecteds.subList(100, 201), idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
         
         IndexedBean startIdxTmpl = new IndexedBean();
         startIdxTmpl.setLongVal(1L);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfindBetween(startIdxTmpl, idxTmpl, new FindBetweenOptions(Collections.singleton("intVal"), null)));
         Collections.sort(idxActuals);
 
         assertBeansEqual(idxExpecteds, idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
 
     }
 
     @Test
     public void testRangeFindPartialColRange() throws Exception
     {
         int numBeans = CassandraDaoBase.ROW_RANGE_SIZE;
         List<IndexedBean> idxBeans = new ArrayList<IndexedBean>();
         for(int i = 0; i < numBeans; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(new Long(i));
             idxBean.setCharVal('c');
             idxBean.setIntVal(i);
             idxBean.setLongVal(i/10L);
             idxBean.setStrVal("strval");
             idxBean.setStrVal2(null);
 
             idxBeans.add(idxBean);
         }
 
         _indexedDao.mput(idxBeans);
 
         IndexedBean idxTmpl = new IndexedBean();
         idxTmpl.setLongVal(5L);
         List<IndexedBean> idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl, new FindOptions("intVal", "t")));
         List<IndexedBean> idxExpecteds = new ArrayList<IndexedBean>();
 
         Collections.sort(idxActuals);
         for(int i = 50; i < 70; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(idxBeans.get(i).getRowKey());
             idxBean.setIntVal(idxBeans.get(i).getIntVal());
             idxBean.setLongVal(idxBeans.get(i).getLongVal());
             idxBean.setStrVal(idxBeans.get(i).getStrVal());
 
             idxExpecteds.add(idxBean);
         }
 
         assertBeansEqual(idxExpecteds.subList(0, 10), idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
         
         IndexedBean endIdxTmpl = new IndexedBean();
         endIdxTmpl.setLongVal(6L);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfindBetween(idxTmpl, endIdxTmpl, new FindBetweenOptions("intVal", "t")));
         Collections.sort(idxActuals);
         assertBeansEqual(idxExpecteds, idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
     }
 
     @Test
     public void testRangeCompositeIndex() throws Exception
     {
         int numBeans = 30;
         List<IndexedBean> idxBeans = new ArrayList<IndexedBean>();
         for(int i = 0; i < numBeans; i++)
         {
             IndexedBean idxBean = new IndexedBean();
             idxBean.setRowKey(new Long(i));
             idxBean.setCharVal('c');
             idxBean.setIntVal(i);
             idxBean.setLongVal((i%5)/2L);
             idxBean.setStrVal("strval-" + i/5);
             idxBean.setStrVal2("strval2-" + i/5);
             
             idxBeans.add(idxBean);
         }
         
         _indexedDao.mput(idxBeans);
         
         List<IndexedBean> idxActuals;
         IndexedBean idxTmpl = new IndexedBean();
         idxTmpl.setStrVal2("strval2-1");
         idxTmpl.setLongVal(1L);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
 
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(7, 9), idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
 
         //try a leading index match
         idxTmpl = new IndexedBean();
         idxTmpl.setStrVal2("strval2-1");
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfind(idxTmpl));
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(5, 10), idxActuals);
 
         IndexedBean startTmpl = new IndexedBean(), endTmpl = new IndexedBean();
         startTmpl.setStrVal("strval-1");
         endTmpl.setStrVal("strval-2");
         endTmpl.setLongVal(1L);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfindBetween(startTmpl, endTmpl));
 
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(5, 14), idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
         
         startTmpl.setStrVal("strval-1");
         startTmpl.setLongVal(2L);
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfindBetween(startTmpl, endTmpl));
 
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(9, 14), idxActuals);
         for(IndexedBean idxBean : idxActuals)
             assertTrue(((IEnhancedEntity) idxBean).getModifiedFields().isEmpty());
         
         //try a leading index match
         startTmpl = new IndexedBean();
         endTmpl = new IndexedBean();
         startTmpl.setStrVal2("strval2-1");
         endTmpl.setStrVal2("strval2-3");
         idxActuals = new ArrayList<IndexedBean>(_indexedDao.mfindBetween(startTmpl, endTmpl));
         Collections.sort(idxActuals);
         assertBeansEqual(idxBeans.subList(5, 20), idxActuals);
     }
 
     @Test
     public void testManyPartitions() throws Exception
     {
         TestPartitioner.partitionHistory().clear();
        TestPartitioner.rangePartitionHistory().clear();
         PartitionIndexBeanDao dao = new PartitionIndexBeanDao();
         dao.setKeyspaceFactory(_pm);
         dao.init();
         
         int numBeans = 1000;
         List<PartitionedIndexBean> idxBeans = new ArrayList<PartitionedIndexBean>();
         for(long i = 0; i < numBeans; i++)
         {
             PartitionedIndexBean idxBean = new PartitionedIndexBean();
             idxBean.setRowKey(i);
             idxBean.setPartitionedValue(i);
             
             idxBeans.add(idxBean);
         }
         dao.mput(idxBeans);
         
         PartitionedIndexBean tmpl = new PartitionedIndexBean();
         tmpl.setPartitionedValue(0L);
         PartitionedIndexBean endTmpl = new PartitionedIndexBean();
         
         //ends up searching 1500 partitions, meaning each partition will initially be asked for one column and then a subsequent get will
         //find an additional column in the partition
         endTmpl.setPartitionedValue(numBeans*3L);
         assertEquals(1000, dao.mfindBetween(tmpl, endTmpl).size());
     }
     
     @Test
     public void testIndexPartitioning() throws Exception
     {
         TestPartitioner.partitionHistory().clear();
        TestPartitioner.rangePartitionHistory().clear();
         PartitionIndexBeanDao dao = new PartitionIndexBeanDao();
         dao.setKeyspaceFactory(_pm);
         dao.init();
         
         int numBeans = 100;
         List<PartitionedIndexBean> idxBeans = new ArrayList<PartitionedIndexBean>();
         for(long i = 0; i < numBeans; i++)
         {
             PartitionedIndexBean idxBean = new PartitionedIndexBean();
             idxBean.setRowKey(i);
             idxBean.setPartitionedValue(i/10);
             
             idxBeans.add(idxBean);
         }
         dao.mput(idxBeans);
         assertEquals(numBeans, TestPartitioner.partitionHistory().size());
         /*
          * check index table row count
          */
         BytesArraySerializer bas = BytesArraySerializer.get();
         RangeSlicesQuery<byte[],byte[],byte[]> query = HFactory.createRangeSlicesQuery(keyspace, bas, bas, bas);
         query.setKeys(null, null);
         query.setColumnFamily("pib_idx");
         query.setRange(null, null, false, 100);
         
         Iterator<Row<byte[], byte[], byte[]>> iterator = query.execute().get().iterator();
         int cnt = 0;
         while(iterator.hasNext())
         {
             cnt++;
             Row<byte[], byte[], byte[]> row = iterator.next();
             assertEquals(20, row.getColumnSlice().getColumns().size());
         }
         
         assertEquals(numBeans/20, cnt);
         
         TestPartitioner.partitionHistory().clear();
         assertTrue(TestPartitioner.rangePartitionHistory().isEmpty());
         
         /*
          * range find
          */
         PartitionedIndexBean tmpl = new PartitionedIndexBean();
         tmpl.setPartitionedValue(5L);
         List<PartitionedIndexBean> actual = new ArrayList<PartitionedIndexBean>(dao.mfind(tmpl));
         Collections.sort(actual);
         
         assertBeansEqual(idxBeans.subList(50, 60), actual);
         assertEquals(1, TestPartitioner.partitionHistory().size());
         assertEquals(1, TestPartitioner.partitionHistory().get(0).get(0).size());
         assertEquals(2L, TestPartitioner.partitionHistory().get(0).get(0).get(0));
         assertTrue(TestPartitioner.rangePartitionHistory().isEmpty());
         
         TestPartitioner.partitionHistory().clear();
         
         
         /*
          * range between find
          */
         PartitionedIndexBean endTmpl = new PartitionedIndexBean();
         endTmpl.setPartitionedValue(7L);
         actual = new ArrayList<PartitionedIndexBean>(dao.mfindBetween(tmpl, endTmpl));
         Collections.sort(actual);
         
         assertBeansEqual(idxBeans.subList(50, 80), actual);
         assertTrue(TestPartitioner.partitionHistory().isEmpty());
         assertEquals(1, TestPartitioner.rangePartitionHistory().size());
         List<List<Object>> expectedRange = new ArrayList<List<Object>>();
         expectedRange.add(Collections.<Object>singletonList(2L));
         expectedRange.add(Collections.<Object>singletonList(3L));
         
         assertBeansEqual(expectedRange, TestPartitioner.rangePartitionHistory().get(0));
         
         TestPartitioner.partitionHistory().clear();
     }
 
     //for entities with multi-column indexes, when writing a value to indexes, all indexed values must be specified, otherwise the index
     //becomes inconsistent
     @Test
     public void testMultiColumnIndexPut()
     {
         List<IndexedBean> beans = new ArrayList<IndexedBean>();
         for(long i = 0; i < 10; i++)
         {
             IndexedBean bean = new IndexedBean();
             bean.setRowKey(i);
             bean.setStrVal(null);
             bean.setStrVal2("sval2");
             bean.setLongVal(i);
             beans.add(bean);
         }
         
         _indexedDao.mput(beans);
         
         //also test WAL cleaned up
         EntityMetadata<IndexedBean> meta = new EntityMetadata<IndexedBean>(IndexedBean.class);
         SliceQuery<byte[],byte[],byte[]> query = HFactory.createSliceQuery(_pm.createKeyspace(EConsistencyLevel.ONE), 
                                                                            BytesArraySerializer.get(), BytesArraySerializer.get(), BytesArraySerializer.get());
         query.setKey(meta.getFamilyNameBytes());
         query.setColumnFamily(PersistenceManager.CF_IDXWAL);
         query.setRange(null, null, false, 100);
         ColumnSlice<byte[],byte[]> slice = query.execute().get();
         assertTrue(slice.getColumns().isEmpty());
         
         
         IndexedBean expected = beans.get(5);
         IndexedBean template = new IndexedBean();
         template.setStrVal2(expected.getStrVal2());
         template.setLongVal(expected.getLongVal());
         
         
         assertEquals(expected, _indexedDao.find(template));
         
         IndexedBean update = new IndexedBean();
         update.setRowKey(expected.getRowKey());
         update.setLongVal(expected.getLongVal()*5);
 
         try
         {
             _indexedDao.put(update);
             fail("partial index put should result in exception");
         }
         catch(IllegalArgumentException ex)
         {
             //success
         }
     }
     
     @Test
     public void testWal() throws Exception
     {
         long startTime = System.currentTimeMillis();
         List<IndexedBean> beans = new ArrayList<IndexedBean>();
         for(long i = 0; i < 10; i++)
         {
             IndexedBean bean = new IndexedBean();
             bean.setRowKey(i);
             bean.setStrVal(null);
             bean.setStrVal2("sval2");
             bean.setLongVal(i);
             beans.add(bean);
         }
         _indexedDao.mput(beans);
 
         for(long i = 0; i < 10; i++)
         {
             IndexedBean bean = beans.get((int) i);
             bean.setRowKey(i);
             bean.setStrVal(null);
             bean.setStrVal2("sval2");
             bean.setLongVal(i);
             beans.add(bean);
         }
 
         dropColumnFamily("indexedbean_idx");
         
         try
         {
             _indexedDao.mput(beans);
         }
         catch(Exception ex)
         {
             //success
         }
         
         //data put failed, wal columns should exist
         EntityMetadata<IndexedBean> meta = new EntityMetadata<IndexedBean>(IndexedBean.class);
         SliceQuery<byte[],Composite,byte[]> query = HFactory.createSliceQuery(_pm.createKeyspace(EConsistencyLevel.ONE), 
                                                                              BytesArraySerializer.get(), CompositeSerializer.get(), BytesArraySerializer.get());
         query.setKey(meta.getFamilyNameBytes());
         query.setColumnFamily(PersistenceManager.CF_IDXWAL);
         query.setRange(null, null, false, 100);
         ColumnSlice<Composite,byte[]> slice = query.execute().get();
         assertEquals(10, slice.getColumns().size());
         
         for(HColumn<Composite, byte[]> col : slice.getColumns())
         {
             Composite colName = col.getName();
             assertEquals(2, colName.size());
             assertEquals(System.currentTimeMillis(), (Long) colName.getComponent(0).getValue(LongSerializer.get()), 1000);
             assertEquals(System.currentTimeMillis()*1000, col.getClock(), TimeUnit.MICROSECONDS.convert(1, TimeUnit.SECONDS));
             long rowkey = LongSerializer.get().fromBytes((byte[]) colName.getComponent(1).getValue(BytesArraySerializer.get()));
             
             boolean found = false;
             for(int i = 0; i < beans.size(); i++)
             {
                 if(beans.get(i).getRowKey().longValue() == rowkey)
                 {
                     beans.remove(i);
                     found = true;
                     break;
                 }
             }
             
             assertTrue("key " + rowkey, found);
         }
         
         _pm.init(); //recreate table
         
         IndexedBean startTmpl = new IndexedBean(), endTmpl = new IndexedBean();
         startTmpl.setLongVal(beans.get(0).getLongVal());
         endTmpl.setLongVal(beans.get(beans.size() - 1).getLongVal());
         
         _indexedDao.checkWal(startTime - 500);
         assertEquals(0, _indexedDao.mfindBetween(startTmpl, endTmpl).size());
 
         assertEquals(1, _indexedDao.walRecoveryStats().getNumOps());
         assertEquals(1, _indexedDao.walRecoveryStats().getNumCassandraOps());
         assertEquals(0, _indexedDao.walRecoveryStats().getNumCols());
         assertEquals(1, _indexedDao.walRecoveryStats().getRecentTimings().length);
         
         _indexedDao.walRecoveryStats().reset();
         Thread.sleep(1000);
 
         _indexedDao.checkWal(System.currentTimeMillis() - 100);
         assertEquals(10, _indexedDao.mfindBetween(startTmpl, endTmpl).size());
         
         assertEquals(1, _indexedDao.walRecoveryStats().getNumOps());
         assertEquals(22, _indexedDao.walRecoveryStats().getNumCassandraOps());
         assertEquals(10, _indexedDao.walRecoveryStats().getNumCols());
         assertEquals(1, _indexedDao.walRecoveryStats().getRecentTimings().length);
     }
     
     @Test
     public void testRangeIndexFindIteration() throws Exception
     {
         PartitionIndexBeanDao dao = new PartitionIndexBeanDao();
         dao.setKeyspaceFactory(_pm);
         dao.init();
 
         int numBeans = 3*CassandraDaoBase.ROW_RANGE_SIZE + 1;
         List<PartitionedIndexBean> idxBeans = new ArrayList<PartitionedIndexBean>();
         for(long i = 0; i < numBeans; i++)
         {
             PartitionedIndexBean idxBean = new PartitionedIndexBean();
             idxBean.setRowKey(i);
             idxBean.setPartitionedValue(0L);
             
             idxBeans.add(idxBean);
         }
         
         PartitionedIndexBean idxTmpl = new PartitionedIndexBean();
         idxTmpl.setPartitionedValue(0L);
 
         /*
          * test partial initial batch
          */
         testRangeFind(dao, idxBeans, idxTmpl, CassandraDaoBase.ROW_RANGE_SIZE/2, null);
 
         /*
          * test complete initial batch
          */
         testRangeFind(dao, idxBeans, idxTmpl, CassandraDaoBase.ROW_RANGE_SIZE, null);
 
         //test max rows
         testRangeFind(dao, idxBeans, idxTmpl, CassandraDaoBase.ROW_RANGE_SIZE, CassandraDaoBase.ROW_RANGE_SIZE/2);
         
         /*
          * test complete subseqent batch
          */
         testRangeFind(dao, idxBeans, idxTmpl, 2*CassandraDaoBase.ROW_RANGE_SIZE, null);
 
         //test max rows
         testRangeFind(dao, idxBeans, idxTmpl, CassandraDaoBase.ROW_RANGE_SIZE, (int) (CassandraDaoBase.ROW_RANGE_SIZE*1.5));
 
         /*
          * partial last batch
          */
         testRangeFind(dao, idxBeans, idxTmpl, numBeans, null);
 
         /*
          * no rows
          */
         idxTmpl.setPartitionedValue(11L);
         assertTrue(dao.mfind(idxTmpl).isEmpty());
         
         /*
          * find between tests
          */
         for(long i = 0; i < numBeans; i++)
         {
             idxBeans.get((int) i).setPartitionedValue(i/10);
         }
         dao.mput(idxBeans);
         
 
         testRangeFindBetween(dao, idxBeans, 0, 5, EFindOrder.NONE, CassandraDaoBase.ROW_RANGE_SIZE);
         testRangeFindBetween(dao, idxBeans, 0, 5, EFindOrder.ASCENDING, 30);
         testRangeFindBetween(dao, idxBeans, 0, 5, EFindOrder.DESCENDING, 30);
 
         testRangeFindBetween(dao, idxBeans, 0, 10, EFindOrder.ASCENDING, 100);
         testRangeFindBetween(dao, idxBeans, 0, 10, EFindOrder.DESCENDING, 100);
         testRangeFindBetween(dao, idxBeans, 0, 15, EFindOrder.ASCENDING, 150);
         testRangeFindBetween(dao, idxBeans, 0, 15, EFindOrder.DESCENDING, 150);
         testRangeFindBetween(dao, idxBeans, 0, 20, EFindOrder.ASCENDING, 200);
         testRangeFindBetween(dao, idxBeans, 0, 20, EFindOrder.DESCENDING, 200);
 
         testRangeFindBetween(dao, idxBeans, 0, 21, EFindOrder.ASCENDING, 201);
         testRangeFindBetween(dao, idxBeans, 0, 21, EFindOrder.DESCENDING, 201);
 
         testRangeFindBetween(dao, idxBeans, 0, 21, EFindOrder.ASCENDING, 101);
         testRangeFindBetween(dao, idxBeans, 0, 21, EFindOrder.DESCENDING, 101);
 
         //test > 100 vals in a partition...
         for(int i = 0; i < numBeans; i++)
         {
             PartitionedIndexBean idxBean = idxBeans.get(i);
             idxBean.setPartitionedValue(i/300L);
         }
 
         dao.mput(idxBeans);
         
         idxTmpl.setPartitionedValue(0L);
         Collection<PartitionedIndexBean> actuals = dao.mfind(idxTmpl);
         Set<Long> keys = new HashSet<Long>();
         for(PartitionedIndexBean actual : actuals)
         {
             long key = actual.getRowKey();
             assertTrue(keys.add(key));
             assertTrue(key >= 0);
             assertTrue(key < 300);
             assertEquals(0, actual.getPartitionedValue());
         }
         assertEquals(300, actuals.size());
 
         FindBetweenOptions betweenOptions = new FindBetweenOptions();
         betweenOptions.setRowOrder(EFindOrder.ASCENDING);
         actuals = dao.mfindBetween(idxTmpl, idxTmpl, betweenOptions);
         keys = new HashSet<Long>();
         for(PartitionedIndexBean actual : actuals)
         {
             long key = actual.getRowKey();
             assertTrue("duplicate key " + key + " size " + keys.size(), keys.add(key));
             assertTrue(key >= 0);
             assertTrue(key < 300);
             assertEquals(0, actual.getPartitionedValue());
         }
         assertEquals(300, actuals.size());
 
         betweenOptions.setRowOrder(EFindOrder.DESCENDING);
         actuals = dao.mfindBetween(idxTmpl, idxTmpl, betweenOptions);
         keys = new HashSet<Long>();
         for(PartitionedIndexBean actual : actuals)
         {
             long key = actual.getRowKey();
             assertTrue("duplicate key " + key + " size " + keys.size(), keys.add(key));
             assertTrue(key >= 0);
             assertTrue(key < 300);
             assertEquals(0, actual.getPartitionedValue());
         }
         assertEquals(300, actuals.size());
         
 
         for(int i = 0; i < numBeans; i++)
         {
             PartitionedIndexBean idxBean = idxBeans.get(i);
             idxBean.setPartitionedValue(i/150L);
         }
         
         dao.mput(idxBeans);
         
         PartitionedIndexBean startTmpl = new PartitionedIndexBean();
         startTmpl.setPartitionedValue(0L);
         idxTmpl.setPartitionedValue(1L);
         FindBetweenOptions options = new FindBetweenOptions();
         options.setMaxRows(280);
         options.setRowOrder(EFindOrder.ASCENDING);
         
         actuals = dao.mfindBetween(startTmpl, idxTmpl, options);
         keys.clear();
         int idx = 0;
         for(PartitionedIndexBean actual : actuals)
         {
             long key = actual.getRowKey();
             assertTrue(keys.add(key));
             assertTrue(key >= 0);
             assertTrue(key < 300);
             assertEquals(idx/150, actual.getPartitionedValue());
             idx++;
         }
         
         assertEquals(280, actuals.size());
     }  
     
     private void testRangeFind(PartitionIndexBeanDao dao, 
                                List<PartitionedIndexBean> beans, 
                                PartitionedIndexBean template, 
                                int batchSize,
                                Integer maxRows)
     {
         dao.mput(beans.subList(0, batchSize));
         FindOptions options = new FindOptions();
         if(maxRows != null)
             options.setMaxRows(maxRows);
         
         List<PartitionedIndexBean> actuals = new ArrayList<PartitionedIndexBean>(dao.mfind(template, options));
 
         if(maxRows == null)
         {
             Collections.sort(actuals);
             List<PartitionedIndexBean> expected = beans.subList(0, maxRows != null ? maxRows : batchSize); 
             assertBeansEqual(expected, actuals);
         }
         else
         {
             //can't guarantee what rows will com back, ensure the full amount came back and each row is unique
             assertEquals(maxRows, actuals.size());
             
             Set<Integer> indexes = new HashSet<Integer>();
             for(PartitionedIndexBean bean : actuals)
             {
                 boolean found = false;
                 for(int i = beans.size() - 1; i >= 0; i--)
                 {
                     if(bean.equals(beans.get(i)))
                     {
                         found = true;
                         assertTrue(bean.toString(), indexes.add(i));
                     }
                     
                 }
                 
                 assertTrue(bean.toString(), found);
             }
         }
     }
 
     private void testRangeFindBetween(PartitionIndexBeanDao dao, List<PartitionedIndexBean> idxBeans, long startVal, long endVal, EFindOrder order, int maxRows)
     {
         FindBetweenOptions options = new FindBetweenOptions();
         options.setMaxRows(maxRows);
         options.setRowOrder(order);
         
         PartitionedIndexBean startTmpl = new PartitionedIndexBean();
         PartitionedIndexBean endTmpl = new PartitionedIndexBean();
 
         startTmpl.setPartitionedValue(startVal);
         endTmpl.setPartitionedValue(endVal);
         
         ArrayList<PartitionedIndexBean> actuals = new ArrayList<PartitionedIndexBean>(dao.mfindBetween(startTmpl, endTmpl, options));
         Set<Long> ids = new HashSet<Long>();
 
         
         if(order == EFindOrder.ASCENDING)
         {
             for(int i = 0; i < actuals.size(); i++)
             {
                 long idxVal = startVal + i/10;
                 PartitionedIndexBean actual = actuals.get(i);
                 long key = actual.getRowKey();
                 long pval = actual.getPartitionedValue();
                 assertTrue("duplicate rowKey" + key, ids.add(key));
                 assertEquals("key " + key, pval, key/10);
                 assertEquals("val " + pval, idxVal, pval);
             }
         }
         else if(order == EFindOrder.DESCENDING)
         {
             for(int i = actuals.size() - 1; i >= 0; i--)
             {
                 long idxVal = endVal - i/10;
                 PartitionedIndexBean actual = actuals.get(i);
                 long key = actual.getRowKey();
                 long pval = actual.getPartitionedValue();
                 assertTrue("duplicate rowKey" + key, ids.add(key));
                 assertEquals("key " + key, pval, key/10);
                 assertEquals("val " + pval, idxVal, pval);
             }
         }
     }
     
     
     private class RecordingStrategy implements IStaleIndexValueStrategy 
     {
         List<StaleIndexUpdateRecord> records = new ArrayList<StaleIndexUpdateRecord>();
         
         @Override
         public void handle(EntityMetadata<?> entity, IndexMetadata index, Keyspace keyspace, Collection<StaleIndexValue> values)
         {
             StaleIndexUpdateRecord record = new StaleIndexUpdateRecord();
             record.entityMetadata = entity;
             record.values = values;
             record.index = index;
             
             records.add(record);
         }
     }
 
     @SuppressWarnings("unused")
     private class StaleIndexUpdateRecord 
     {
         EntityMetadata<?> entityMetadata;
         Collection<StaleIndexValue> values;
         IndexMetadata index;
     }
 
 }
