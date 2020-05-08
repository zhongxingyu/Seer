 package org.roger.study.ExClient.Protocol;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.roger.study.ExClient.test.Report;
 
 import java.nio.ByteBuffer;
 
 import static org.roger.study.ExClient.configuration.RunTime.setPolicyKey;
 import static org.roger.study.ExClient.controller.HandleChannel.getUser;
 import static org.roger.study.ExClient.controller.TestCounter.setRes;
 import static org.roger.study.ExClient.controller.TestCounter.setSentOk;
 
 /**
  * Created with IntelliJ IDEA.
  * User: next
  * Date: 13-8-22
  * Time: 下午1:00
  * To change this template use File | Settings | File Templates.
  */
 public class ParaResponse {
     private static final String serverProtocolVersionHeader = "MS-Server-ActiveSync";
     HttpResponse response;
     Channel channel;
 
 
     public ParaResponse(HttpResponse response, Channel channel) {
         this.response = response;
         this.channel = channel;
     }
 
     public BuildRequest.Type next()  {
        if (!response.getStatus().equals(HttpResponseStatus.OK))  {
            Report.getError(getUser(channel) ,response.getStatus().toString());
             return BuildRequest.Type.None;
        }
 
         if (response.containsHeader(serverProtocolVersionHeader)
                 && response.getHeader(HttpHeaders.Names.CONTENT_LENGTH).equals("0"))  //It's Options Response
             return BuildRequest.Type.Provision;
 
         int length = Integer.parseInt(response.getHeader(HttpHeaders.Names.CONTENT_LENGTH));
         if (length > 0) {
             //page and first commands
             ByteBuffer byteBuffer = response.getContent().toByteBuffer();
             return getDataAndNext(byteBuffer);
         }else if (length == 0)  {
             setSentOk(channel.getId());
         }
 
         setRes(channel.getId());
 
         return BuildRequest.Type.None;
     }
 
     private BuildRequest.Type getDataAndNext(ByteBuffer byteBuffer) {
         byte[] bytes = byteBuffer.array();
 
         ParaCmd para = new ParaCmd(bytes);
         if (para.isProvision())  {
             int status1 = para.Status(ParaCmd.StatusType.DeviceInformation);
             int status2 = para.Status(ParaCmd.StatusType.Policy);
             if (status1 == 1)  {
                 Report.logon(getUser(channel), true);
                 boolean res = paraPolicyKey(para);
                 Report.paraPolicyKey(getUser(channel), res);
                 return BuildRequest.Type.AckProvision;
             }else if (status2 == 1)  {
                 Report.ackPolicyKey(getUser(channel), true);
                 boolean res = paraPolicyKey(para);
                 Report.paraPolicyKey(getUser(channel), res);
                 return BuildRequest.Type.SendMail;
             }else {
                 if (status1 > 0)  {
                     Report.logon(getUser(channel), false);
                 }else if (status2 > 0) {
                     Report.ackPolicyKey(getUser(channel), false);
                 }
             }
         }
 
         return BuildRequest.Type.None;  //To change body of created methods use File | Settings | File Templates.
     }
 
 
     private boolean paraPolicyKey(ParaCmd para) {
         String user = getUser(channel);
         if (user.isEmpty())
             return false;
 
         Long key = para.policyKey();
         if (key > 0)  {
             setPolicyKey(user, Long.valueOf((key)));
             return true;
         }
         return false;
     }
 }
