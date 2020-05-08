 package com.wixpress.fjarr.IT.servletwithhttpclient;
 
 import com.wixpress.fjarr.IT.BaseItTest;
 import com.wixpress.fjarr.IT.util.ITServer;
import com.wixpress.fjarr.client.*;
 import com.wixpress.fjarr.example.DataStructService;
 import com.wixpress.fjarr.example.DataStructServiceImpl;
 import com.wixpress.fjarr.json.FjarrJacksonModule;
 import com.wixpress.fjarr.json.JsonRpc;
 import com.wixpress.fjarr.json.JsonRpcClientProtocol;
 import com.wixpress.fjarr.server.RpcServlet;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
import org.junit.Ignore;
 
 import javax.servlet.Servlet;
 import java.net.URI;
 
 /**
  * @author alex
  * @since 1/6/13 1:57 PM
  */
 
@Ignore("Fails in team-city")
 public class ServletWithNettyTest extends BaseItTest
 {
 
     @BeforeClass
     public static void init() throws Exception
     {
 
         mapper.registerModule(new FjarrJacksonModule());
         Servlet servlet = new RpcServlet(
                 JsonRpc.server(
                         DataStructService.class, new DataStructServiceImpl(), mapper)
         );
 
         server = new ITServer(9191, new ITServer.ServletPair("/*", servlet));
 
         serviceRoot = "http://127.0.0.1:9191/DataStructService";
 
         final JsonRpcClientProtocol protocol = new JsonRpcClientProtocol(mapper);
         final NettyInvoker invoker = new NettyInvoker(
                 NettyClientConfig.defaults());
         service = RpcClientProxy.create(DataStructService.class,
                 serviceRoot,
                 invoker,
                 protocol);
 
         client = new RpcClient(new URI(serviceRoot), protocol, invoker, null);
 
         server.start();
 
     }
 
     @AfterClass
     public static void fini() throws Exception
     {
         server.stop();
     }
 
 
 }
