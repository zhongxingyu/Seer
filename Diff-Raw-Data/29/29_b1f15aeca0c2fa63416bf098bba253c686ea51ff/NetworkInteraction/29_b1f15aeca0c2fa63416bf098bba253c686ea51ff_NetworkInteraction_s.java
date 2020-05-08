 package task_network;
 
 import task_tree.TaskTree;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: TheRusskiy
  * Date: 18.11.12
  * Time: 1:34
  * To change this template use File | Settings | File Templates.
  */
 public class NetworkInteraction implements Serializable {
 
     public enum RequestCode {
         NO_CODE,
         SAVE_TO_SERVER,
         LOAD_FROM_SERVER,
         CREATE_NEW_USER,
         DELETE_USER,
         GET_AVAILABLE_TREES;
     }
 
     public enum ReplyCode {
         NO_CODE,
         SUCCESS,
         USER_ALREADY_EXISTS,
         UNKNOWN_REQUEST_CODE,
         ERROR;
     }
 
     private String text = "some text"; //arbitrary message
     private String login;
     private byte[] encryptedPassword;
     private TaskTree tree=null;
     private String treeName;
     private RequestCode requestCode=RequestCode.NO_CODE;
     private ReplyCode replyCode=ReplyCode.NO_CODE;
 
     public ReplyCode getReplyCode() {
         return replyCode;
     }
 
     public void setReplyCode(ReplyCode replyCode) {
         this.replyCode = replyCode;
     }
 
     public void setRequestCode(RequestCode requestCode) {
         this.requestCode = requestCode;
     }
 
     public RequestCode getRequestCode() {
         return requestCode;
     }
 
     private List<String> treeNames;
 
     public List<String> getTreeNames() {
         return treeNames;
     }
 
     public void setTreeNames(List<String> treeNames) {
         this.treeNames = treeNames;
     }
 
     public String getTreeName() {
         return treeName;
     }
 
     public void setTreeName(String treeName) {
         this.treeName = treeName;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
 
     public String getPassword() {
         return CipherDriver.decrypt(encryptedPassword, CipherDriver.getSharedKey());
     }
 
     public void setPassword(String password) {
         encryptedPassword=CipherDriver.encrypt(password, CipherDriver.getSharedKey());
     }
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     public String getText() {
         return text;
     }
 
     public TaskTree getTree() {
         return tree;
     }
 
     public void setTree(TaskTree tree) {
         this.tree = tree;
     }
 
     public NetworkInteraction(String s) {
         text=s;
     }
 
     public NetworkInteraction() {
     }
 
     public boolean equals(Object interaction) {
         //FIXME
         NetworkInteraction interaction1=(NetworkInteraction) interaction;
         if (interaction==null) return false;
         if (interaction1.getText().equals(this.getText())) return true;
         return false;
     }
 }
