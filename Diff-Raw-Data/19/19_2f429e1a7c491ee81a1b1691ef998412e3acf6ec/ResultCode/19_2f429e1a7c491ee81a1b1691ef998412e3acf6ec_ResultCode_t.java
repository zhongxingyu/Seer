 package org.tsinghua.omedia.consts;
 
 import org.tsinghua.omedia.form.FriendRequestReplyForm;
 
 /**
  * 服务端接口的返回码 常量
  * @author xuhongfeng
  * @see modified by czw
  */
 public class ResultCode extends OmediaConst {
     //服务端错误
     public static final int SERVER_ERROR = -1;
     //请求处理成功
     public static final int SUCCESS = 1;
     //token过期
     public static final int TOKEN_WRONG = 3;
     
     public static class Register extends ResultCode {
         //用户名已存在
        public static final int USERNAME_EXIST = 2;
     }
     
     public static class Login extends ResultCode {
         //验证失败
         public static final int FAILED = 2;
         //客户端版本需要升级
         public static final int SOFTWARE_NEED_UPDATE = 4;
     }
     
     public static class AddFriend extends ResultCode {
         //不能加自己为好友
         public static final int ADD_YOUR_SELF = 4;
         //已经在您的好友列表中
         public static final int ALREADY_FRIEND = 5;
     }
 
     //TODO need to change server
     public static class Settings extends ResultCode {
         //password error
         public static final int PSWERROR = 2;
     }
     
     public static class FriendRequestReply extends ResultCode {
         public static final int ACCEPT_SUCCESS = FriendRequestReplyForm.REPLY_ACCEPT;
         public static final int REJECT_SUCCESS = FriendRequestReplyForm.REPLY_REJECT;
         public static final int FAILED = 4;
     }
 }
