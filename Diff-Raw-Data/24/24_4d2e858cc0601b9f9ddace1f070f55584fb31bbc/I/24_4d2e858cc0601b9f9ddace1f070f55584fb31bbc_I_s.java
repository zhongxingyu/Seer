 package summer.inf;
 
 import summer.pojo.Tasklet;
 
 /**
  * 
 * ͷЭʹõĳ
  * 
  * @author zhenzxie
  */
 public final class I {
 
 	public final static class Sys {
 
 		/**
 		 * address of server
 		 */
 		public final static String URL = "127.0.0.1";
 
 		/**
 		 * port that client used to connect to server
 		 */
 		public final static int PORT = 8088;
 
 	}
 
 	/**
 	 * ͻ˺ͷЭʹõţ10ʼ
 	 * 
 	 * @author zhenzxie
 	 */
 	public final static class Req {
 		/**
 		 * ¼š
 		 * <p>
 		 * questCode = LOGIN; requestArgs = {User{id|name,password}}
 		 * <p>
 		 * ӦresponseCode = OK|BAD_*;responseArgs =
 		 * {User{id,name,password,...}|String}
 		 * <p>
 		 * ͣ ¼֧idû¼ɹ {@link Res#OK}аUser󡣵ʧܣ
 		 * {@link Res} BAD_*庬μעͣеĶΪStringΪThrowable
 		 * 
 		 * <p>
 		 * 磺
 		 * 
 		 * <pre class="prettyprint">
 		 * User user = new User();
 		 * user.setId(Long.valueOf(10000));
 		 * user.setPassword(&quot;123456&quot;);
 		 * session.write(Request.createRequest(Req.LOGIN, user));
 		 * </pre>
 		 */
 		public final static int LOGIN = 10;
 
 		/**
 		 * ûϢ޸
 		 * <p>
 		 * requestCode = USER_MODIFIED;reuquestArgs = {User{id,name,...}}
 		 * <p>
 		 * ӦresponseCode = OK|BAD_*;responseArgs = {User{id,name,...}|String}
 		 * <p>
 		 * ͣ޸û֧ʹidû޸ġʹid޸ûϢʱ{@code User}
 		 * ʵΪֵʹname޸ûϢʱ{@code User}
 		 * ʵΪֵֵʵ򱣳־ֵ
 		 * <p>
 		 * <strong>֧޸ûid</strong>
 		 * <p>
 		 * 磺
 		 * 
 		 * <pre class="prettyprint">
 		 * User user = new User()//or use full constructor
 		 * user.setId(Long.valueOf(10000));// modified by id
 		 * user.setName(&quot;sduxzz&quot;);// old value
 		 * user.setPassword(&quot;654321&quot;);// new value
 		 * user.setPermission(7);// new value
 		 * user.setType(1);// old value
 		 * user.setTellphone(&quot;18769783279&quot;);// old value
 		 * user.setAddress(&quot;&quot;);// old value
 		 * session.write(Request.createRequest(Req.USER_MODIFIED, user));
 		 * </pre>
 		 */
 		public final static int USER_MODIFIED = 11;
 
 		/**
 		 * ȡ
 		 * <p>
 		 * requestCode = TASKLET_PULL;requestArgs =
 		 * {User{id},Long,Long,Boolean;String}
 		 * <p>
 		 * ӦresponseCode = OK|BAD_*;responseArgs =
 		 * {Tasklet,Tasklet,...|String}
 		 * <p>
 		 * ͣȡʷδȡrequestArgsеһlongʾʼʱ䣬
 		 * ڶlongʾʱ䣬booleanʾǷɡStringʾԽ򷽷ο
 		 * {@code TASKLET_LIST_ORDER_*} صresponseArgsǰ{@link Tasklet}
 		 * Ľ<strong>ҪȡδlongֵΪ{@link #TASKLET_ALL}
 		 * booleanΪfalseҪȡ longֵΪ{@link #TASKLET_ALL}
 		 * booleanΪtrue
 		 * <p>
 		 * ڻ֧ûȡ:(
 		 * <p>
 		 * ڻֶֶ֧ :(
 		 * <p>
 		 * ڻ֧ͬʱȡʷδ:(
 		 * <p>
 		 * Ϊ˽ԼֻǽTasklet͸ͻˣûкоTaskletItemҪʹ
 		 * {@link Req#TASKLET_ITEM_PULL}
 		 * <p>
 		 * ʱڣҪִе:ڻȡʷʱcurrentTime < last_time +
 		 * cycle,һʷΪһѾִˡʷ
 		 * ڻȡδʱcurrentTime >= last_time + cycle && account !=
 		 * 0,һδ㡣 </strong>
 		 * <p>
 		 * 磺
 		 * 
 		 * <pre class="prettyprint">
 		 * User user = new User();
 		 * user.setId(10000);
 		 * session.write(Req.TASKLET_PULL, user, Long.valueOf(start), Long.valueOf(end),
 		 * 		false);
 		 * 
 		 * User user = new User();
 		 * user.setId(10000);
 		 * session.write(Req.TASKLET_PULL, user, Long.valueOf(TASKLET_ALL),
 		 * 		Long.valueOf(TASKLET_ALL), false);
 		 * </pre>
 		 */
 		public final static int TASKLET_PULL = 12;
 
 		/**
 		 * ȡһ
 		 * <p>
 		 * requestCode = TASKLET_ITEM_PULLL;requestArgs = {Tasklet{id}}
 		 * <p>
 		 * ӦresponseCode = OK | BAD_*;responseArgs =
 		 * {TaskletItem,TaskletItem,...|String}
 		 * <p>
 		 * ͣͨTaskletidһTaskletľݡ
 		 * صresponseArgsаTaskletTaskletItem
 		 * ÿTaskletItemаҪݣݽṹһname-value pair
 		 * <p>
 		 * 磺
 		 * 
 		 * <pre class="prettyprint">
 		 * Tasklet tasklet = new TaskLet();
 		 * tasklet.setId(1L);
 		 * session.write(Req.TASKLET_ITEM_PULL, tasklet);
 		 * </pre>
 		 */
 		public final static int TASKLET_ITEM_PULL = 13;
 
 		/**
 		 * ϴɵ
 		 * <p>
 		 * requestCode = TASKLET_ITEM_PUSH;requestArgs =
 		 * {TaskletItem{id,tasklet_id,stuff_id,name,List,...},...}
 		 * <p>
 		 * ӦresponseCode = OK|BAD_*;responseArgs = {String}
 		 * <p>
 		 * ͣϴĹÿֻϴһItemrequestArgsУidtasklet_id,
 		 * stuff_id,List<TemplateItemArgs>,nameһַϢΪresponseCode
 		 * <p>
 		 * 磺
 		 * 
 		 * <pre class="prettyprint">
 		 * TaskletItem taskletItem = new TaskletItem();
 		 * taskletItem.setId(1L);
 		 * taskletItem.setName(&quot;߸1&quot;);
 		 * taskletItem.setTaskletId(1L);// itemidΪ1LidſʹлõǸid
 		 * taskletItem.setStuffId(1L);// ItemǼidΪ1L豸idſʹлõǸ豸id
 		 * List&lt;TaskletItemArg&gt; list = new ArrayList&lt;TaskletItemArg&gt;();
 		 * TaskletItemArg arg = new TaskletItemArg();
 		 * arg.setId(1L);
 		 * arg.setName(&quot;ֱ&quot;);
 		 * arg.setValue(&quot;0.8&quot;);
 		 * arg.setTaskletItemId(1L);
 		 * arg.setComment(&quot;ֱô䳤أ&quot;);
 		 * list.add(arg);
 		 * taskletItem.setArgList(list);// listΪԺͽԡοTaskletItemArg
 		 * session.write(Request.createRequest(Req.TASKLET_ITEM_PUSH, taskletItem));// ֻһItem
 		 * </pre>
 		 */
 		public final static int TASKLET_ITEM_PUSH = 14;
 
 		/**
 		 * ˳š
 		 * <p>
 		 * requestCode = EXIT; requestArgs = {User{id|name}}
 		 * <p>
 		 * ӦresponseCode = OK | BAD_*;responseArgs = {String}
 		 * <p>
 		 * ͣ˳Ҫûidname˳ɹ{@link Res#OK}˳ʧܣ{@link Res}
 		 * BAD_*庬μעͣеĶString TODO:ȷexitֵʹЩġ
 		 * <p>
 		 * 磺
 		 * 
 		 * <pre class="prettyprint">
 		 * User user = new User();
 		 * user.setId(Long.valueOf(10000));
 		 * session.write(Request.createRequest(Req.EXIT, user));
 		 * </pre>
 		 */
 		public final static int EXIT = 50;
 
 		// --------һЩĬϵֵ-1ʼ--------
 		/**
 		 * ڻȡĬֵ
 		 */
 		public final static long TASKLET_ALL = -1;
 
 		// --------TASKLET--------
 		public final static String TASKLET_LIST_ORDER_TIME_DES = " last_time desc ";
 
 		public final static String TASKLET_LIST_ORDER_TIME_ASC = " last_time asc ";
 
 		public final static String TASKLET_LIST_ORDER_CYCLE_DES = " cycle desc ";
 
 		public final static String TASKLET_LIST_ORDER_CYCYLE_ASC = " cycle asc ";
 	}
 
 	/**
 	 * ͷ˺ͷЭʹõķغţ100ʼ
 	 * 
 	 * @author zhenzxie
 	 */
 	public final static class Res {
 		public final static int OK = 100;
 
 		// --------ͻύ101ʼ--------
 		/**
 		 * ݵĶRequestʹ
 		 */
 		public final static int BAD_REQUEST = 101;
 		/**
 		 * requestCode
 		 */
 		public final static int BAD_REQUESTCODE = 102;
 		/**
 		 * requestArgsΪnull
 		 */
 		public final static int BAD_REQUESTARGS_NULL = 103;
 		/**
 		 * requestArgsΪempty
 		 */
 		public final static int BAD_REQUESTARGS_EMPTY = 104;
 		/**
 		 * requestArgsв̫ٴ
 		 */
 		public final static int BAD_REQUESTARGS_LESS = 105;
 		/**
 		 * requestArgsв̫
 		 */
 		public final static int BAD_REQUESTARGS_MORE = 106;
 		/**
 		 * requestArgsвͲȷ
 		 */
 		public final static int BAD_REQUESTARGS_TYPE = 107;
 		/**
 		 * requestArgsеĲЭ
 		 */
 		public final static int BAD_REQUESTARGS_ARG = 108;
 
 		// --------ͻ˵¼ʧ111ʼ--------
 		/**
 		 * id
 		 */
 		public final static int BAD_LOGIN_ID = 111;
 		/**
 		 * name
 		 */
 		public final static int BAD_LOGIN_NAME = 112;
 		/**
 		 * password
 		 */
 		public final static int BAD_LOGIN_PASSWORD = 113;
 
 		// --------ڲ201ʼ--------
 		public final static int BAD_SYS = 201;
 
 		/**
 		 * codeϢ
 		 * 
 		 * @param code
 		 * @return
 		 */
 		public final static String valueOf(int code) {
 			switch (code) {
 				case OK:
 					return "ɹ!";
 				case BAD_REQUEST:
 					return "ݵϢRequst͵Ķ!message = ";
 				case BAD_REQUESTCODE:
 					return "requestCode!requestCode = ";
 				case BAD_REQUESTARGS_NULL:
 					return "requestArgsΪnull!";
 				case BAD_REQUESTARGS_EMPTY:
 					return "requestArgsΪEmpty!";
 				case BAD_REQUESTARGS_LESS:
 					return "requestArgsеĲ̫!requestArgs = ";
 				case BAD_REQUESTARGS_MORE:
 					return "requestArgsеĲ̫!requestArgs = ";
 				case BAD_REQUESTARGS_TYPE:
 					return "requestArgsеĲͲȷ!requestArgs = ";
 				case BAD_REQUESTARGS_ARG:
 					return "requestArgsеĲЭ飡requestArgs = ";
 				case BAD_LOGIN_ID:
 					return "ûID!ID = ";
 				case BAD_LOGIN_NAME:
 					return "û!name = ";
 				case BAD_LOGIN_PASSWORD:
 					return "!";
 				case BAD_SYS:
 					return "ϵͳڲ!message = ";
 				default:
 					return "";
 			}
 		}
 	}
 }
