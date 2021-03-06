 package org.basex.core;
 
 import static org.basex.core.Text.*;
 import static org.basex.util.Token.*;
 import org.basex.core.Commands.Cmd;
 import org.basex.core.Commands.CmdCreate;
 import org.basex.core.Commands.CmdDrop;
 import org.basex.core.Commands.CmdIndex;
 import org.basex.core.Commands.CmdInfo;
 import org.basex.core.Commands.CmdShow;
 import org.basex.core.Commands.CmdUpdate;
 import org.basex.core.proc.CreateUser;
 import org.basex.core.proc.Cs;
 import org.basex.core.proc.Close;
 import org.basex.core.proc.Copy;
 import org.basex.core.proc.CreateDB;
 import org.basex.core.proc.CreateFS;
 import org.basex.core.proc.CreateIndex;
 import org.basex.core.proc.CreateMAB;
 import org.basex.core.proc.Delete;
 import org.basex.core.proc.DropDB;
 import org.basex.core.proc.DropIndex;
 import org.basex.core.proc.DropUser;
 import org.basex.core.proc.Exit;
 import org.basex.core.proc.Export;
 import org.basex.core.proc.Find;
 import org.basex.core.proc.InfoUsers;
 import org.basex.core.proc.IntInfo;
 import org.basex.core.proc.IntOutput;
 import org.basex.core.proc.Help;
 import org.basex.core.proc.Info;
 import org.basex.core.proc.InfoDB;
 import org.basex.core.proc.InfoIndex;
 import org.basex.core.proc.InfoTable;
 import org.basex.core.proc.Insert;
 import org.basex.core.proc.IntStop;
 import org.basex.core.proc.Kill;
 import org.basex.core.proc.List;
 import org.basex.core.proc.Open;
 import org.basex.core.proc.Optimize;
 import org.basex.core.proc.IntPrompt;
 import org.basex.core.proc.Show;
 import org.basex.core.proc.Run;
 import org.basex.core.proc.Set;
 import org.basex.core.proc.Update;
 import org.basex.core.proc.XQueryMV;
 import org.basex.core.proc.XQuery;
 import org.basex.query.QueryContext;
 import org.basex.query.QueryException;
 import org.basex.query.QueryParser;
 import org.basex.util.Array;
 import org.basex.util.InputParser;
 import org.basex.util.Levenshtein;
 import org.basex.util.StringList;
 
 /**
  * This is a parser for command strings, creating {@link Process} instances.
  * Several commands can be formulated in one string and separated by semicolons.
  *
  * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
  * @author Christian Gruen
  */
 public final class CommandParser extends InputParser {
   /** Context. */
   private final Context ctx;
   /** Flag for including internal commands. */
   private final boolean internal;
 
   /**
    * Constructor, parsing the input queries.
    * @param in query input
    * @param c context
    */
   public CommandParser(final String in, final Context c) {
     this(in, c, false);
   }
 
   /**
    * Constructor, parsing internal commands.
    * @param in query input
    * @param c context
    * @param i internal flag
    */
   public CommandParser(final String in, final Context c, final boolean i) {
     ctx = c;
     internal = i;
     init(in);
   }
 
   /**
    * Parses the input and returns a command list.
    * @return commands
    * @throws QueryException query exception
    */
   public Process[] parse() throws QueryException {
     Process[] list = new Process[0];
     if(!more()) return list;
 
     while(true) {
       final Cmd cmd = consume(Cmd.class, null);
       list = Array.add(list, parse(cmd));
       consumeWS();
       if(!more()) return list;
       if(!consume(';')) help(null, cmd);
     }
   }
 
   /**
    * Parse command.
    * @param cmd command
    * @return process
    * @throws QueryException query exception
    */
   private Process parse(final Cmd cmd) throws QueryException {
     switch(cmd) {
       // user commands
 
       case CREATE: case C:
         switch(consume(CmdCreate.class, cmd)) {
           case DATABASE: case DB:
             return new CreateDB(string(cmd), name(null));
           case INDEX:
             return new CreateIndex(consume(CmdIndex.class, cmd));
           case FS:
             return new CreateFS(string(cmd), name(cmd));
           case MAB:
             return new CreateMAB(string(cmd), name(null));
           case USER:
             return new CreateUser(string(cmd), name(cmd));
         }
         break;
       case OPEN: case O:
         return new Open(name(cmd));
       case INFO: case I:
         switch(consume(CmdInfo.class, cmd)) {
           case NULL:
             return new Info();
           case DATABASE: case DB:
             return new InfoDB();
           case INDEX:
             return new InfoIndex();
           case TABLE:
             String arg1 = number(null);
             final String arg2 = arg1 != null ? number(null) : null;
             if(arg1 == null) arg1 = xquery(null);
             return new InfoTable(arg1, arg2);
           case USERS:
             return new InfoUsers();
         }
         break;
       case CLOSE:
         return new Close();
       case LIST:
         return new List();
       case DROP:
         switch(consume(CmdDrop.class, cmd)) {
           case DATABASE: case DB:
             return new DropDB(name(cmd));
           case INDEX:
             return new DropIndex(consume(CmdIndex.class, cmd));
           case USER:
             return new DropUser(name(cmd));
         }
         break;
       case OPTIMIZE:
         return new Optimize();
       case EXPORT:
         return new Export(string(cmd));
       case XQUERY: case X:
         return new XQuery(xquery(cmd));
       case XQUERYMV:
         return new XQueryMV(number(cmd), number(cmd), xquery(cmd));
       case RUN:
         return new Run(string(cmd));
       case FIND:
         return new Find(string(cmd));
       case CS:
         return new Cs(xquery(cmd));
       case COPY:
         return new Copy(xquery(cmd), target(INTO, cmd), pos(cmd));
       case DELETE:
         return new Delete(xquery(cmd));
       case INSERT:
         final CmdUpdate ins = consume(CmdUpdate.class, cmd);
         switch(ins) {
           case ELEMENT:
             String val = name(cmd);
             return new Insert(ins, target(INTO, cmd), pos(cmd), val);
           case TEXT: case COMMENT: case FRAGMENT:
             val = string(cmd);
             return new Insert(ins, target(INTO, cmd), pos(cmd), val);
           case PI:
             val = name(cmd);
             String val2 = string(cmd);
             return new Insert(ins, target(INTO, cmd), pos(cmd), val, val2);
           case ATTRIBUTE:
             val = name(cmd);
             val2 = string(cmd);
             return new Insert(ins, target(INTO, cmd), val, val2);
         }
         break;
       case UPDATE:
         final CmdUpdate upd = consume(CmdUpdate.class, cmd);
         switch(upd) {
           case ELEMENT:
             String val = name(cmd);
             return new Update(upd, target(AT, cmd), val);
           case TEXT: case COMMENT:
             val = string(cmd);
             return new Update(upd, target(AT, cmd), val);
           case PI: case ATTRIBUTE:
             val = name(cmd);
             String val2 = string(cmd);
             return new Update(upd, target(AT, cmd), val, val2);
           default:
         }
         break;
       case SET:
         final String opt = name(cmd);
         String val = string(null);
         final Object type = ctx.prop.object(opt.toUpperCase());
         if(type == null) help(null, cmd);
         return new Set(opt, val);
       case HELP:
         String hc = name(null);
         if(hc != null) {
           qp = qm;
           hc = consume(Cmd.class, cmd).toString();
         }
         return new Help(hc);
       case EXIT: case QUIT: case Q:
         return new Exit();
 
       // internal commands
       case INTPROMPT:
         return new IntPrompt();
       case INTOUTPUT:
         return new IntOutput();
       case INTINFO:
         return new IntInfo();
       case INTSTOP:
         return new IntStop();
 
       // server commands
       case KILL:
         return new Kill();
       case SHOW:
         final CmdShow show = consume(CmdShow.class, cmd);
         switch(show) {
           case DATABASES:
           case SESSIONS:
           case USERS:
             return new Show(show);
           default:
         }
         break;
 
       default:
     }
     return null;
   }
 
   /**
    * Parses and returns a string. Quotes can be used to include spaces.
    * @param cmd referring command; if specified, the result must not be empty
    * @return path
    * @throws QueryException query exception
    */
   private String string(final Cmd cmd) throws QueryException {
     final StringBuilder tb = new StringBuilder();
     consumeWS();
     boolean q = false;
     while(more()) {
       final char c = curr();
       if((c <= ' ' || c == ';') && !q) break;
       if(c == '"') q ^= true;
       else tb.append(c);
       consume();
     }
     return finish(cmd, tb);
   }
 
   /**
    * Parses and returns an xquery expression.
    * @param cmd referring command; if specified, the result must not be empty
    * @return path
    * @throws QueryException query exception
    */
   private String xquery(final Cmd cmd) throws QueryException {
     consumeWS();
     final StringBuilder sb = new StringBuilder();
     if(more() && !curr(';')) {
       final QueryParser p = new QueryParser(new QueryContext(ctx));
       p.init(qu);
       p.qp = qp;
       p.parse(null, false);
       sb.append(qu.substring(qp, p.qp));
       qp = p.qp;
     }
     return finish(cmd, sb);
   }
 
   /**
    * Parses and returns a name. A name can include letters, digits, dashes
    * and periods.
    * @param cmd referring command; if specified, the result must not be empty.
    * @return name
    * @throws QueryException query exception
    */
   private String name(final Cmd cmd) throws QueryException {
     consumeWS();
     final StringBuilder sb = new StringBuilder();
     while(letterOrDigit(curr()) || curr('.') || curr('-')) sb.append(consume());
     return finish(cmd, sb);
   }
 
   /**
    * Parses and returns an XQuery target, prefixed by the specified string.
    * @param pre prefix
    * @param cmd referring command; if specified, the result must not be empty.
    * @return target
    * @throws QueryException query exception
    */
   private String target(final String pre, final Cmd cmd) throws QueryException {
     consumeWS();
     if(!consume(pre) && !consume(pre.toLowerCase())) help(null, cmd);
     return xquery(cmd);
   }
 
   /**
    * Parses and returns a number, prefixed by the keyword {@link Text#AT}.
    * @param cmd referring command; if specified, the result must not be empty.
    * @return position
    * @throws QueryException query exception
    */
   private int pos(final Cmd cmd) throws QueryException {
     consumeWS();
     return consume(AT) || consume(AT.toLowerCase()) ?
         Integer.parseInt(number(cmd)) : 0;
   }
 
   /**
    * Parses and returns a name.
    * @param cmd referring command; if specified, the result must not be empty.
    * @param s input string
    * @return name
    * @throws QueryException query exception
    */
   private String finish(final Cmd cmd, final StringBuilder s)
       throws QueryException {
     if(s.length() != 0) return s.toString();
     if(cmd != null) help(null, cmd);
     return null;
   }
 
   /**
    * Parses and returns a number.
    * @param cmd referring command; if specified, the result must not be empty.
    * @return name
    * @throws QueryException query exception
    */
   private String number(final Cmd cmd) throws QueryException {
     consumeWS();
     final StringBuilder tb = new StringBuilder();
     if(curr() == '-') tb.append(consume());
     while(digit(curr())) tb.append(consume());
     return finish(cmd, tb);
   }
 
   /**
    * Returns the index of the found string or throws an error.
    * @param cmp possible completions
    * @param par parent command
    * @param <E> token type
    * @return index
    * @throws QueryException query exception
    */
   protected <E extends Enum<E>> E consume(final Class<E> cmp, final Cmd par)
       throws QueryException {
 
     final String token = name(null);
     try {
       // return command reference; allow empty strings as input ("NULL")
       final String t = token == null ? "NULL" : token.toUpperCase();
       final E cmd = Enum.valueOf(cmp, t);
      // [CG] should be the same?
      // http://forums.sun.com/thread.jspa?threadID=5336141
 //      if(!(cmd instanceof Cmd)) return cmd;
 //      final Cmd c = (Cmd) cmd;
       if(!(Cmd.class.isInstance(cmd))) return cmd;
       final Cmd c = Cmd.class.cast(cmd);
       if(!c.help() && (internal || !c.internal())) return cmd;
     } catch(final IllegalArgumentException ex) { }
 
     final Enum<?>[] alt = list(cmp, token);
     if(token == null) {
       // no command found
       if(par == null) error(list(alt), CMDNO);
       // show available command extensions
       help(list(alt), par);
     }
 
     // find similar commands
     final byte[] name = lc(token(token));
     final Levenshtein ls = new Levenshtein();
     for(final Enum<?> s : list(cmp, null)) {
       final byte[] sm = lc(token(s.name().toLowerCase()));
      if(ls.similar(name, sm, 0) && s instanceof Cmd)
         error(list(alt), CMDSIMILAR, name, sm);
     }
 
     // unknown command
     if(par == null) error(list(alt), CMDWHICH, token);
     // show available command extensions
     help(list(alt), par);
     return null;
   }
 
   /**
    * Prints some command info.
    * @param alt input alternatives
    * @param cmd input completions
    * @throws QueryException query exception
    */
   protected void help(final StringList alt, final Cmd cmd)
       throws QueryException {
     error(alt, PROCSYNTAX, cmd.help(true));
   }
 
   /**
    * Returns the command list.
    * @param <T> token type
    * @param en enumeration
    * @param i user input
    * @return completions
    */
   private <T extends Enum<T>> Enum<?>[] list(final Class<T> en,
       final String i) {
 
     Enum<?>[] list = new Enum<?>[0];
     final String t = i == null ? "" : i.toUpperCase();
     for(final Enum<?> e : en.getEnumConstants()) {
      if(e instanceof Cmd) {
        final Cmd c = (Cmd) e;
         if(c.help() || c.hidden() || c.internal()) continue;
       }
       if(e.name().startsWith(t)) {
         list = Array.add(list, e);
         //list.add(e.name().toLowerCase());
       }
     }
     return list;
   }
 
   /**
    * Throws an error.
    * @param comp input completions
    * @param m message
    * @param e extension
    * @throws QueryException query exception
    */
   public void error(final StringList comp, final String m, final Object... e)
       throws QueryException {
     final QueryException qe = new QueryException(m, e);
     qe.complete(this, comp);
     throw qe;
   }
 
   /**
    * Converts the specified commands into a string list.
    * @param comp input completions
    * @return string list
    */
   public StringList list(final Enum<?>[] comp) {
     final StringList list = new StringList();
     for(Enum<?> c : comp) list.add(c.name().toLowerCase());
     return list;
   }
 }
