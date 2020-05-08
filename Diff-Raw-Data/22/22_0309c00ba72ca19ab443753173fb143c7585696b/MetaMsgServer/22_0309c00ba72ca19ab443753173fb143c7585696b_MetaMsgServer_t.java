 package org.apache.hadoop.hive.metastore.msg;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Semaphore;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hive.conf.HiveConf;
 import org.apache.hadoop.hive.metastore.ObjectStore;
 import org.apache.hadoop.hive.metastore.msg.MSGFactory.DDLMsg;
 import org.apache.hadoop.hive.metastore.tools.HiveMetaTool;
 
 import com.taobao.metamorphosis.Message;
 import com.taobao.metamorphosis.client.MessageSessionFactory;
 import com.taobao.metamorphosis.client.MetaClientConfig;
 import com.taobao.metamorphosis.client.MetaMessageSessionFactory;
 import com.taobao.metamorphosis.client.consumer.ConsumerConfig;
 import com.taobao.metamorphosis.client.consumer.MessageConsumer;
 import com.taobao.metamorphosis.client.consumer.MessageListener;
 import com.taobao.metamorphosis.client.producer.MessageProducer;
 import com.taobao.metamorphosis.client.producer.SendResult;
 import com.taobao.metamorphosis.exception.MetaClientException;
 import com.taobao.metamorphosis.utils.ZkUtils.ZKConfig;
 
 public class MetaMsgServer {
 
   public static final Log LOG = LogFactory.getLog(ObjectStore.class.getName());
   static String zkAddr = "127.0.0.1:3181";
   static Producer producer =  null;
   static int times = 3;
   static MetaMsgServer server = null;
   private static boolean initalized = false;
   private static SendThread send = new SendThread();
 
   static ConcurrentLinkedQueue<DDLMsg> queue = new ConcurrentLinkedQueue<DDLMsg>();
 
   private static ConcurrentLinkedQueue<DDLMsg> failed_queue = new ConcurrentLinkedQueue<DDLMsg>();
 
   static{
     send.start();
   }
 
 
   private  static void initalize() throws MetaClientException{
     server = new MetaMsgServer();
     producer.config(zkAddr);
     producer = Producer.getInstance();
     initalized = true;
   }
 
 
   public static void start() throws MetaClientException{
     if(!initalized){
       initalize();
 
     }
 
   }
 
   public static void sendMsg(DDLMsg msg) {
     queue.add(msg);
     send.release();
   }
 
   //zy  是不是应该release多个
   public static void sendMsg(List<DDLMsg> msgs) {
     queue.addAll(msgs);
     send.release();
   }
 
 
   public static class SendThread extends Thread{
     private static final int MSG_SEND_BATCH=0;
     Semaphore sem  = new Semaphore(MSG_SEND_BATCH);
     @Override
     public void run() {
       // TODO Auto-generated method stub
 
       while(true ){
         try{
           if(queue.isEmpty()){
             LOG.debug("---in sendThread before ac");
             sem.acquire();
             LOG.debug("---in sendThread after ac");
             if(queue.isEmpty()){
               continue;
             }
           }
 
           DDLMsg msg = queue.peek();
           boolean succ = sendDDLMsg(msg);
           if(!succ){
             failed_queue.add(msg);
           }else{
             queue.poll();
             if(!failed_queue.isEmpty()){
               int i=0;
 //              while(i++ < MSG_SEND_BATCH && !failed_queue.isEmpty()){//retry send faild msg
               while( !failed_queue.isEmpty()){//retry send faild msg,old msg should send as soon as possible.
                 DDLMsg retry_msg =failed_queue.peek();
                 if(!sendDDLMsg(retry_msg)){
                   break;
                 }else{
                   failed_queue.poll();
                 }
               }
             }
           }
         } catch (Exception e) {
           LOG.error(e,e);
         }
       }
 
     }
 
     public void release(){
       sem.release();
     }
 
   }
 
 
   public static String getZkAddr() {
     return zkAddr;
   }
 
 
   public static void setZkAddr(String zkAddr) {
     MetaMsgServer.zkAddr = zkAddr;
   }
 
   public static boolean  sendDDLMsg(DDLMsg msg) {
     String jsonMsg = "";
 
 //    switch((int)msg.getEvent_id()){
 //      case MSGType.MSG_NEW_DATABESE : break;
 //      //新建库
 //      case MSGType.MSG_ALTER_DATABESE : break;
 //            //修改库
 //      case MSGType.MSG_ALTER_DATABESE_PARAM : break;
 //            //修改库属性
 //      case MSGType.MSG_DROP_DATABESE : break;
 //            //删除库
 //      case MSGType.MSG_NEW_TALBE : break;
 //            //新建表
 //      case MSGType.MSG_ALT_TALBE_NAME : break;
 //            //修改表名
 //      case MSGType.MSG_ALT_TALBE_DISTRIBUTE : break;
 //            //修改表数据分布
 //      case MSGType.MSG_ALT_TALBE_PARTITIONING : break;
 //            //修改表分区方式
 //      case MSGType.MSG_ALT_TALBE_DEL_COL : break;
 //            //修改表删除列
 //      case MSGType.MSG_ALT_TALBE_ADD_COL : break;
 //            //修改表新增列
 //      case MSGType.MSG_ALT_TALBE_ALT_COL_NAME : break;
 //            //修改表修改列名
 //      case MSGType.MSG_ALT_TALBE_ALT_COL_TYPE : break;
 //            //修改表修改列类型
 //      case MSGType.MSG_ALT_TALBE_ALT_COL_LENGTH : break;
 //            //修改表修改列类型长度
 //      case MSGType.MSG_NEW_PARTITION : break;
 //            // 新建分区
 //      case MSGType.MSG_ALT_PARTITION : break;
 //            //修改分区
 //      case MSGType.MSG_DEL_PARTITION : break;
 //            // 删除分区
 //      case MSGType.MSG_NEW_PARTITION_FILE : break;
 //            //增加分区文件
 //      case MSGType.MSG_ALT_PARTITION_FILE : break;
 //            //修改分区文件
 //      case MSGType.MSG_REP_PARTITION_FILE_CHAGE : break;
 //            //分区文件副本变化
 //      case MSGType.MSG_STA_PARTITION_FILE_CHAGE : break;
 //            //分区文件状态变化
 //      case MSGType.MSG_REP_PARTITION_FILE_ONOFF : break;
 //            //分区文件副本上下线变化
 //      case MSGType.MSG_DEL_PARTITION_FILE : break;
 //            //删除分区文件
 //      case MSGType.MSG_NEW_INDEX : break;
 //            //新建列索引
 //      case MSGType.MSG_ALT_INDEX : break;
 //            //修改列索引
 //      case MSGType.MSG_ALT_INDEX_PARAM : break;
 //            //修改列索引属性
 //      case MSGType.MSG_DEL_INDEX : break;
 //            //删除列索引
 //      case MSGType.MSG_NEW_PARTITION_INDEX : break;
 //            //新建分区索引
 //      case MSGType.MSG_ALT_PARTITION_INDEX : break;
 //            //修改分区索引
 //      case MSGType.MSG_DEL_PARTITION_INDEX : break;
 //            // 删除分区索引
 //      case MSGType.MSG_NEW_PARTITION_INDEX_FILE : break;
 //            //增加分区索引文件
 //      case MSGType.MSG_ALT_PARTITION_INDEX_FILE : break;
 //            //修改分区索引文件
 //      case MSGType.MSG_REP_PARTITION_INDEX_FILE_CHAGE : break;
 //            //分区索引文件副本变化
 //      case MSGType.MSG_STA_PARTITION_INDEX_FILE_CHAGE : break;
 //            //分区索引文件状态变化
 //      case MSGType.MSG_REP_PARTITION_INDEX_FILE_ONOFF : break;
 //            //分区索引文件副本上下线变化
 //      case MSGType.MSG_DEL_PARTITION_INDEX_FILE : break;
 //            //删除分区索引文件
 //      case MSGType.MSG_NEW_NODE : break;
 //            //新增节点
 //      case MSGType.MSG_DEL_NODE : break;
 //            //删除节点
 //      case MSGType.MSG_FAIL_NODE : break;
 //            //节点故障
 //
 //      case MSGType.MSG_DDL_DIRECT_DW1 : break;
 //        //dw1 专用DDL语句
 //      case MSGType.MSG_DDL_DIRECT_DW2 : break;
 //        //dw2 专用DDL语句
 //    }//end of switch
 
 
 
     jsonMsg = MSGFactory.getMsgData(msg);
     LOG.info("---zjw-- send ddl msg:"+jsonMsg);
     boolean success = false;
 
     success = retrySendMsg(jsonMsg, times);
     return success;
   }
 
   private static boolean retrySendMsg(String jsonMsg,int times){
     // FIXME: if server not initialized, just return true;
     if (!initalized) {
       return true;
     }
     if(times <= 0){
       return false;
     }
     //zy
     //第一次失败,第二次发送成功的话依然返回false把..
     //修改
     boolean success = false;
     try{
       success = producer.sendMsg(jsonMsg);
     }catch(InterruptedException ie){
       LOG.error(ie,ie);
       return retrySendMsg(jsonMsg,times-1);
     } catch (MetaClientException e) {
       LOG.error(e,e);
       return retrySendMsg(jsonMsg,times-1);
     }
     return success;
   }
 
   public static class AsyncConsumer {
     final MetaClientConfig metaClientConfig = new MetaClientConfig();
     final ZKConfig zkConfig = new ZKConfig();
     String localhost_name;
     private  ObjectStore.MsgHandler handler ;
     public void consume() throws MetaClientException{
       //init objectstore,handler
       HiveConf hiveConf = new HiveConf(HiveMetaTool.class);
       ObjectStore ob = new ObjectStore();
       ob.setConf(hiveConf);
       handler = ob.new MsgHandler();
 
       try {
         localhost_name = InetAddress.getLocalHost().getHostName();
       } catch (UnknownHostException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
       //设置zookeeper地址
      zkConfig.zkConnect = MetaMsgServer.zkAddr;
       metaClientConfig.setZkConfig(zkConfig);
       // New session factory,强烈建议使用单例
       MessageSessionFactory sessionFactory = new MetaMessageSessionFactory(metaClientConfig);
       // subscribed topic
       final String topic = "meta-test";
       // consumer group
       final String group = "meta-example";
       // create consumer,强烈建议使用单例
 
       //生成处理线程
       MessageConsumer consumer =
       sessionFactory.createConsumer(new ConsumerConfig(group));
       //订阅事件，MessageListener是事件处理接口
       consumer.subscribe(topic, 1024, new MessageListener(){
 
         @Override
         public Executor getExecutor() {
           // TODO Auto-generated method stub
           return null;
         }
 
         @Override
         public void recieveMessages(final Message message) {
           DDLMsg msg = new DDLMsg();
           String data = new String(message.getData());
 
 //          LOG.info("---zy--consume msg: " + data);
           System.out.println(data);
           msg = DDLMsg.fromJson(data);
           if(msg.getLocalhost_name().equals(localhost_name))
           {
             LOG.info("---zy--local msg,no need to refresh " );
 //            handler.refresh(msg);
           }
 //          else
           //just test
           handler.refresh(msg);
         }
 
       }
       );
       consumer.completeSubscribe();
     }
   }
 
 
   public static class Producer {
     private static Producer instance= null;
     private final MetaClientConfig metaClientConfig = new MetaClientConfig();
     private final ZKConfig zkConfig = new ZKConfig();
     private MessageSessionFactory sessionFactory = null;
     // create producer,强烈建议使用单例
     private MessageProducer producer = null;
     // publish topic
     private final String topic = "meta-test";
     private static String  zkAddr = "127.0.0.1:3181";
 
     public static void config(String addr){
       zkAddr = addr;
     }
 
     private Producer() {
         //设置zookeeper地址
 
         zkConfig.zkConnect = zkAddr;
         metaClientConfig.setZkConfig(zkConfig);
         // New session factory,强烈建议使用单例
         connect();
     }
 
     private void connect(){
       try{
         sessionFactory = new MetaMessageSessionFactory(metaClientConfig);
         producer = sessionFactory.createProducer();
         producer.publish(topic);
       }
       catch(MetaClientException e){
         LOG.error(e.getMessage());
       }
       LOG.info("Topic '" + topic + "' has been published.");
     }
 
     public static Producer getInstance() throws MetaClientException {
       if(instance == null){
         instance = new Producer();
       }
       return instance;
     }
 
     boolean sendMsg(String msg) throws MetaClientException, InterruptedException{
         LOG.debug("in send msg:"+msg);
 
         if(producer == null){
           connect();
           if(producer == null){
             return false;
           }
         }
         SendResult sendResult = producer.sendMessage(new Message(topic, msg.getBytes()));
         // check result
 
         boolean success = sendResult.isSuccess();
         if (!success) {
             LOG.error("Send message failed,error message:" + sendResult.getErrorMessage());
         }
         else {
             LOG.debug("Send message successfully,sent to " + sendResult.getPartition());
         }
         return success;
     }
   }
 
   public static void main(String[] args){
 
 
     try {
       new AsyncConsumer().consume();
     } catch (MetaClientException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
 
 }
