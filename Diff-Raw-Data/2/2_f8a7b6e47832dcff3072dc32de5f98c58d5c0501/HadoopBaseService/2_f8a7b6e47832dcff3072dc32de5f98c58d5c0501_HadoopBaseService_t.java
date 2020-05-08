 package com.gaoshin.hadoop.hdfs;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hdfs.DFSClient;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.gaoshin.configuration.ConfService;
 import com.gaoshin.job.bean.JobConf;
 
 public class HadoopBaseService {
     @Autowired
     private ConfService confService;
     
     private String hdfsUrl;
     
     public String getHdfsUrl() {
         if(hdfsUrl == null) {
            JobConf conf = confService.getByKey("hadoop.dfs.uri");
             if(conf != null) {
                 hdfsUrl = conf.getValue();
             }
         }
         return hdfsUrl;
     }
     
     public Configuration getConf() {
         Configuration conf = new Configuration();
         conf.set("fs.default.name", getHdfsUrl());
         conf.set("hadoop.job.ugi", "hadoop");
         return conf;
     }
 
     public DFSClient getDfsClient() throws IOException {
         Configuration conf = getConf();
         DFSClient dfsClient = new DFSClient(conf);
         return dfsClient;
     }
 
     public void setHdfsUrl(String hdfsUrl) {
         this.hdfsUrl = hdfsUrl;
     }
 }
