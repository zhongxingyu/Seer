 package iotc.parser;
 
 import iotc.Identification;
 import iotc.common.UPnPException;
 import iotc.db.*;
 import iotc.medium.Medium;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.itolab.morihit.clinkx.UPnPRemoteAction;
 
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * コマンド構文解析，実行クラス
  * Created with IntelliJ IDEA.
  * User: atsushi-o
  * Date: 12/12/26
  * Time: 10:44
  * To change this template use File | Settings | File Templates.
  */
 public class CommandExpression {
     private static final ResourceBundle rb;
     private static final Logger LOG;
     static {
         rb = ResourceBundle.getBundle("iotc.i18n.CommandExpression");
         LOG = Logger.getLogger(CommandExpression.class.getName());
     }
 
     /**
      * コマンドの定義
      */
     static enum CommandType {
         /** UPnPコマンドを実行する */
         EXEC_COMMAND    (rb.getString("ct.EXEC_COMMAND.regex"), "device", "command") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 Identification id = getIdentification(args);
                 Command c = id.getCommand();
                 User u = log.getUser();
 
                 if (!checkPower(medium, log, PowerEnum.valueOf(c.getPower()))) return;
 
                 UPnPRemoteAction uppra = EntityMapUtil.dbToUPnP(c);
                 if (!uppra.invoke()) {
                     throw new UPnPException("UPnPRemoteAction invocation failed");
                 }
             }
         },
         /** 指定されたデバイスのコマンド一覧を返す */
         GET_COMLIST     (rb.getString("ct.GET_COMLIST.regex"), "device") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 Identification id = getIdentification(args);
                 User u = log.getUser();
 
                 Device d = (Device)session.load(Device.class, id.getDevice().getId());
                 StringBuilder sb = new StringBuilder();
                 for (Command c : (Set<Command>)d.getCommands()) {
                     if (c.getPower() <= u.getPowerForUserId().getPower()) {
                         sb.append(c.getName()).append(", ");
                     }
                 }
                 if (sb.length() > 0) {
                     sb.setLength(sb.length()-2);
                 } else {
                     sb.append(rb.getString("ct.GET_COMLIST.errorMes"));
                 }
 
                 medium.send(log, log.getUser(), sb.toString());
             }
         },
         /** 指定された部屋のデバイス一覧を返す */
         GET_DEVLIST     (rb.getString("ct.GET_DEVLIST.regex"), "room") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 Identification id = getIdentification(args);
 
                 Room r = (Room)session.load(Room.class, id.getRoom().getId());
                 StringBuilder sb = new StringBuilder();
                 for (Device d : (Set<Device>)r.getDevices()) {
                     sb.append(d.getName()).append(", ");
                 }
                 sb.setLength(sb.length()-2);
 
                 medium.send(log, log.getUser(), sb.toString());
             }
         },
         /** 部屋一覧を返す */
         GET_ROOMLIST    (rb.getString("ct.GET_ROOMLIST.regex")) {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 List<Room> l = session.getNamedQuery("Room.findAll").list();
                 StringBuilder sb = new StringBuilder();
                 for (Room r : l) {
                     sb.append(r.getName()).append(", ");
                 }
                 sb.setLength(sb.length() - 2);
 
                 medium.send(log, log.getUser(), sb.toString());
             }
         },
         /** 任意のユーザの権限を昇格する */
         SET_POWER       (rb.getString("ct.SET_POWER.regex"), "user", "power", "option") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 // TODO: Implement this
                 if (!checkPower(medium, log, PowerEnum.ADMINISTRATOR)) return;
 
                 User u = getUser((String)args.get("user"), session);
                 PowerEnum p = PowerEnum.valueOf((Integer)args.get("power"));
                 if (p == null) {
                     medium.send(log, log.getUser(), rb.getString("ct.SET_POWER.noPower"));
                     return;
                 }
 
                 Power pow = u.getPowerForUserId();
                 pow.setPrevPower(pow.getPower());
                 pow.setPower(p.getId());
                 session.update(pow);
 
                 medium.send(log, log.getUser(), MessageFormat.format(rb.getString("ct.SET_POWER.success"), u.getName(), p.name()));
             }
         },
         /** 任意のユーザにデバイス・コマンドの操作許可を出す */
         SET_POWER_DEVICE    (rb.getString("ct.SET_POWER_DEVICE.regex"), "user", "device", "option") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 // TODO: Implement this
                 if (!checkPower(medium, log, PowerEnum.OWNER)) return;
             }
         },
         /** 別名を設定 */
         SET_ALIAS       (rb.getString("ct.SET_ALIAS.regex"), "alias", "device", "command") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 // TODO: Implement this
             }
         },
         /** センサ一覧を返す */
         GET_SENSLIST    (rb.getString("ct.GET_SENSLIST.regex"), "device") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 Identification id = getIdentification(args);
 
                 Device d = id.getDevice();
                 Iterable<Sensor> sensors = null;
                 if (d == null && id.getRoom() != null) {
                     Query q = session.getNamedQuery("Sensor.findFromRoom");
                     q.setInteger("roomID", id.getRoom().getId());
                     sensors = q.list();
                 } else if (d != null) {
                     d = (Device)session.load(Device.class, d.getId());
                     sensors = d.getSensors();
                 } else {
                     throw new IllegalArgumentException("Room or Device id is not found");
                 }
 
                 StringBuilder sb = new StringBuilder();
                 if (sensors != null) {
                     for (Sensor s : sensors) {
                         sb.append(s.getSensorType().getName()).append(", ");
                     }
                 }
                 if (sb.length() > 0) {
                     sb.setLength(sb.length()-2);
                 } else {
                     sb.append(rb.getString("ct.GET_SENSLIST.errorMes"));
                 }
 
                 medium.send(log, log.getUser(), sb.toString());
             }
         },
         /** センサ値を返す */
         GET_SENSVALUE   (rb.getString("ct.GET_SENSVALUE.regex"), "sensor") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 if (!checkPower(medium, log, PowerEnum.FAMILY)) return;
                 Identification id = getIdentification(args);
 
                 List<Sensor> sensors = id.getSensors();
                 StringBuilder sb = new StringBuilder();
                 if (sensors == null || sensors.size() == 0) {
                     sb.append(rb.getString("ct.error.sensor"));
                 } else {
                     for (Sensor s : sensors) {
                         try {
                             Object o = s.getValue();
                             sb.append(s.getSensorType().getName()).append(": ").append(o)
                                     .append(" ").append(s.getSensorType().getUnit()).append(", ");
                         } catch (UPnPException ex) {
                             LOG.log(Level.INFO, "Getting UPnP device variable failed", ex);
                         }
                     }
                     if (sb.length() > 0) {
                         sb.setLength(sb.length()-2);
                     } else {
                         sb.append(rb.getString("ct.GET_SENSVALUE.upnpErrMes"));
                     }
                 }
 
                 medium.send(log, log.getUser(), sb.toString());
             }
         },
         /** 条件を満たした時にコマンドを実行する */
         TERM_COMMAND    (rb.getString("ct.TERM_COMMAND.regex"), "term", "device", "command") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 //TODO: Implement this
                 if (!checkPower(medium, log, PowerEnum.FAMILY)) return;
             }
         },
         /** 条件を満たした時に通知する */
         TERM_NOTIFY     (rb.getString("ct.TERM_NOTIFY.regex"), "term") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 if (!checkPower(medium, log, PowerEnum.FAMILY)) return;
 
                 Term t = new Term();
                 t.setTerm((String)args.get("term"));
                 t.setUser(log.getUser());
                 Set sens = t.getSensors();
 
                 //TODO: Fix here
                 Matcher m = IDPATTERN.matcher(t.getTerm());
                 while (m.find()) {
                     LOG.info(m.group(0));
                     Identification id = IdentificationParser.parse(m.group(0));
                     LOG.info(id.toString());
                     sens.add(id.getSensors().get(0));
                 }
                 t.setSensors(sens);
 
                 Session s = HibernateUtil.getSessionFactory().openSession();
                 s.beginTransaction();
                 s.save(t);
                 s.getTransaction().commit();
                 s.close();
 
                 medium.send(log, log.getUser(), rb.getString("ct.TERM_NOTIFY.complete"));
             }
         },
         /** センサ情報の購読リクエスト */
         SUBSCRIBE_REQ   (rb.getString("ct.SUBSCRIBE_REQ.regex"), "sensor", "term", "frequency") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 //TODO: Implement this
             }
         },
         /** 未定義コマンド */
         UNKNOWN         ("") {
             @Override protected void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception {
                 medium.send(log, log.getUser(), rb.getString("ct.UNKNOWN.message"));
             }
         };
 
         private static final Pattern IDPATTERN;
         static {
             IDPATTERN = Pattern.compile("([^:]+)::([^:]+)::([^: <>=]+)");
         }
 
         private final Pattern regex;
         private final String[] argNames;
         private CommandType(final String regex, final String... argNames) {
             this.regex = Pattern.compile(regex);
             this.argNames = argNames;
         }
 
         /**
          * 1番目の引数からIdentificationクラスを取得．ない場合は例外を投げる
          * @param args
          * @return
          */
         private static Identification getIdentification(Map<String, Object> args) {
             if (!args.containsKey("ID")) {
                 throw new IllegalArgumentException("Identification is not found");
             }
             return (Identification)args.get("ID");
         }
 
         private static User getUser(String nameOrAlias, Session session) {
             Query q = session.getNamedQuery("User.findFromNameAndAlias");
             q.setString("name", nameOrAlias);
             q.setString("alias", "%"+nameOrAlias+"%");
             List<User> us = q.list();
             if (us.size() == 0) {
                 throw new IllegalArgumentException("This user name is unavailable");
             }
             return us.get(0);
         }
 
         private static boolean checkPower(Medium medium, Log log, PowerEnum pow) {
             if (log.getUser().getPowerForUserId().getPower() < pow.getId()) {
                 medium.send(log, log.getUser(), rb.getString("ct.error.power"));
                 return false;
             }
             return true;
         }
 
         /**
          * ログレコードをアップデートする
          * @param session
          * @param log
          * @param state
          * @param com
          * @param variable
          */
         private static void updateLog(Session session, Log log, LogState state, Command com, String variable) {
             Transaction t = session.beginTransaction();
             try {
                 log.setState(state.getId());
                 log.setCommand(com);
                 log.setComVariable(variable);
 
                 session.merge(log);
 
                 t.commit();
             } catch (HibernateException ex) {
                 LOG.log(Level.SEVERE, "Update log entity failed", ex);
                 t.rollback();
             }
         }
 
         /**
          * ログの状態を変更する
          * @param session
          * @param log
          * @param state
          */
         private static void updateLog(Session session, Log log, LogState state) {
             updateLog(session, log, state, null, null);
         }
 
         /**
          * このコマンドの書式パターンを返す
          * @return
          */
         public Pattern getPattern() {
             return regex;
         }
 
         /**
          * このコマンドの引数名の配列を返す
          * @return
          */
         public String[] getArgNames() {
             return argNames;
         }
 
         /**
          * 与えられたコマンド文から引数のMapを生成する
          * @param com
          * @return
          */
         public Map<String, String> getArgs(String com) {
             Matcher m = regex.matcher(com);
             if (!m.find()) return null;
 
             Map<String, String> ret = new HashMap();
             for (String g : argNames) {
                 ret.put(g, m.group(g));
             }
             return ret;
         }
 
         /**
          * 与えられた文字列がこのコマンドの書式にマッチするかチェックする
          * @param com
          * @return
          */
         public boolean isMatch(String com) {
             LOG.log(Level.FINER, "pattern: {0}, com: {1}", new Object[]{regex.pattern(), com});
             return regex.matcher(com).find();
         }
 
         /**
          * このコマンドの操作を実行する
          *
          * @param medium
          * @param log
          * @param id
          * @param com
          * @return
          */
         public boolean exec(Medium medium, Log log, Identification id, String com) {
             LogState ls = LogState.ERROR;
 
             HashMap<String, Object> args = new HashMap();
             args.put("ID", id);
             args.putAll(getArgs(com));
 
             Session s = HibernateUtil.getSessionFactory().openSession();
             Transaction t = s.beginTransaction();
             try {
                 Log l = (Log)s.load(Log.class, log.getId());
                 process(medium, l, s, args);
                 t.commit();
                 ls = LogState.COMPLETE;
                 return true;
             } catch (Exception ex) {
                 LOG.log(Level.WARNING, "Command "+name()+" execution failed", ex);
                 return false;
             } finally {
                 updateLog(s, log, ls);
                 s.close();
             }
         }
 
         /**
          * コマンド処理の中身
          * @param medium
          * @param log
          * @param session
          * @param args
          * @throws Exception
          */
         protected abstract void process(Medium medium, Log log, Session session, Map<String, Object> args) throws Exception;
 
         /**
          * 与えられた文字列にマッチするコマンドを返す．なければnull
          * @param com
          * @return
          */
         public static CommandType searchCommand(String com) {
             for (CommandType ct : values()) {
                 if (ct.isMatch(com)) return ct;
             }
             return null;
         }
     }
 
 
     private final String commandStr;
     private CommandType type;
     private Identification id;
     private Map<String, String> args;
 
     /**
      * コマンドのマッチングを行い初期化を行う
      * @param command
      */
     public CommandExpression(String command) {
         this.commandStr = command;
         this.type = CommandType.UNKNOWN;
         for (CommandType ct : CommandType.values()) {
             if (ct.isMatch(command)) {
                 this.type = ct;
                 break;
             }
         }
 
         LOG.log(Level.INFO, "Command type: {0}", type.name());
         args = type.getArgs(command);
        LOG.log(Level.INFO, "Arguments: {0}", args.values());
         for (String n : type.getArgNames()) {
             if (n.matches("room|device|sensor")) {
                 id = IdentificationParser.parse(args.get(n));
             } else if (n.matches("command")) {
                 id = IdentificationParser.parseCommand(id, args.get(n));
             }
         }
 
         // Try to parse alias name
         if (type.equals(CommandType.UNKNOWN)) {
             id = IdentificationParser.parse(commandStr);
             if (id.getCommand() != null) {
                 type = CommandType.EXEC_COMMAND;
             }
         }
     }
 
     public String getCommandStr() {
         return commandStr;
     }
 
     public CommandType getType() {
         return type;
     }
 
     public boolean exec(Medium medium, Log log) {
         return this.type.exec(medium, log, id, commandStr);
     }
 }
