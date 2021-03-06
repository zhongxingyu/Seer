 // $Id: RequestContainerV5.java,v 1.62 2007-09-02 17:51:31 tigran Exp $
 
 package diskCacheV111.poolManager ;
 
 import  diskCacheV111.vehicles.*;
 import diskCacheV111.pools.PoolV2Mode;
 import  diskCacheV111.util.*;
 
 import  dmg.cells.nucleus.*;
 import  dmg.util.*;
 
 import  java.lang.reflect.*;
 import  java.text.*;
 import  java.util.*;
 import  java.util.regex.*;
 import  java.io.*;
 
 public class RequestContainerV5 implements Runnable {
     //
     //
     private final Map<UOID, PoolRequestHandler>     _messageHash   = new HashMap<UOID, PoolRequestHandler>() ;
     private final Map<String, PoolRequestHandler>   _handlerHash   = new HashMap<String, PoolRequestHandler>() ;
     private final CellAdapter _cell;
 
     private String      _warningPath   = "billing" ;
     private long        _retryTimer    = 15 * 60 * 1000 ;
 
     private int         _maxRequestClumping = 1 ;
 
     private String      _onError       = "suspend" ;
     private int         _maxRetries    = 3 ;
     private int         _maxRestore    = -1 ;
     private boolean     _sendCostInfo  = false ;
     private boolean     _sendHitInfo   = false ;
 
     private int         _restoreExceeded = 0 ;
     private boolean     _suspendIncoming = false ;
     private boolean     _suspendStaging  = false ;
 
     private final PoolSelectionUnit  _selectionUnit;
     private final PoolMonitorV5      _poolMonitor;
     private final SimpleDateFormat   _formatter        = new SimpleDateFormat ("MM.dd HH:mm:ss");
     private final ThreadPool         _threadPool ;
     private final Map<PnfsId, CacheException>            _selections       = new HashMap<PnfsId, CacheException>() ;
     private final PartitionManager   _partitionManager ;
     private long               _started          = System.currentTimeMillis();
     private long               _checkFilePingTimer = 10 * 60 * 1000 ;
 
     public RequestContainerV5( CellAdapter cell ,
                                PoolSelectionUnit selectionUnit ,
                                PoolMonitorV5 poolMonitor ,
                                PartitionManager partitionManager ){
 
        _cell             = cell ;
        _selectionUnit    = selectionUnit ;
        _poolMonitor      = poolMonitor ;
        _partitionManager = partitionManager ;
 
        _threadPool = createThreadPool( _cell.getArgs().getOpt("threadPool") ) ;
 
        String sendHitString = _cell.getArgs().getOpt("sendHitInfoMessages" ) ;                 //VP
        if( sendHitString != null ) _sendHitInfo = sendHitString.equals("yes") ;                //VP
        say( "send HitInfoMessages : "+(_sendHitInfo?"yes":"no") ) ;                            //VP
 
        _cell.addCommandListener(_threadPool);
 
        _cell.getNucleus().newThread(this,"Container-ticker").start();
     }
     private ThreadPool createThreadPool( String poolClass ){
 
        ThreadPool threadPool = null ;
        Class    [] classArgs = { dmg.cells.nucleus.CellAdapter.class } ;
        Object   [] objArgs   = { _cell } ;
 
        if( ( poolClass != null ) && ( ! poolClass.equals("") ) ){
           try{
               say("ThreadPool : Trying to initialize : "+poolClass);
               Class cl = Class.forName(poolClass) ;
               Constructor con = cl.getConstructor(classArgs) ;
               threadPool = (ThreadPool)con.newInstance(objArgs);
               say("ThreadPool : Initializing "+poolClass+" succeeded");
           }catch(Exception ee ){
              esay("Problems creating ThreadPool : "+ee );
              esay(ee);
           }
        }
        threadPool = threadPool == null ?
                     new ThreadCounter( _cell ) :
                     threadPool ;
 
        say("ThreadPool : using  ThreadPool : "+threadPool.getClass().getName());
        return threadPool ;
     }
     public void messageArrived( CellMessage cellMessage ){
 
        UOID   uoid    = cellMessage.getLastUOID() ;
        Object message = cellMessage.getMessageObject() ;
 
        PoolRequestHandler handler = null ;
 
        synchronized( _messageHash ){
 
            handler = _messageHash.remove( uoid ) ;
 
            if( handler == null ){
               esay("Unexpected message class 9 "+
                   message.getClass()+" from source = "+
                   cellMessage.getSourceAddress() );
               return;
            }
 
        }
 
        handler.mailForYou( message ) ;
     }
     public void run(){
        try{
           while( ! Thread.interrupted() ){
 
              Thread.sleep(60000) ;
 
              List<PoolRequestHandler> list = null ;
              synchronized( _handlerHash ){
                 list = new ArrayList<PoolRequestHandler>( _handlerHash.values() ) ;
              }
              try{
                 for( PoolRequestHandler handler: list ){
                    if( handler == null )continue ;
                    handler.alive() ;
                 }
              }catch(Throwable t){
                 esay(t) ;
                 continue ;
              }
           }
        }catch(InterruptedException ie ){
           esay("Container-ticker done");
        }
     }
     public void poolStatusChanged(String poolName, int poolStatus) {
         say("Restore Manager : got 'poolRestarted' for " + poolName);
         try {
             List<PoolRequestHandler> list = null;
             synchronized (_handlerHash) {
                 list = new ArrayList<PoolRequestHandler>(_handlerHash.values());
             }
 
             for (PoolRequestHandler rph : list) {
 
                 if (rph == null)
                     continue;
 
 
                 switch( poolStatus ) {
                     case PoolStatusChangedMessage.UP:
                         /*
                          * if pool is up, re-try all request scheduled to this pool
                          * and all requests, which do not have any pool candidates
                          *
                          * in this construction we will fall down to next case
                          */
                         if (rph.getPoolCandidate().equals("<unknown>") ) {
                             say("Restore Manager : retrying : " + rph);
                             rph.retry(false);
                         }
                     case PoolStatusChangedMessage.DOWN:
                         /*
                          * if pool is down, re-try all request scheduled to this
                          * pool
                          */
                         if (rph.getPoolCandidate().equals(poolName) ) {
                             say("Restore Manager : retrying : " + rph);
                             rph.retry(false);
                         }
                 }
 
             }
 
         } catch (Exception ee) {
             esay("Problem retrying pool " + poolName + " (" + ee + ")");
         }
     }
     public void getInfo( PrintWriter pw ){
 
        PoolManagerParameter def = _partitionManager.getParameterCopyOf() ;
 
        pw.println("Restore Controller [$Revision: 1.62 $]\n") ;
        pw.println( "      Retry Timeout : "+(_retryTimer/1000)+" seconds" ) ;
        pw.println( "  Thread Controller : "+_threadPool ) ;
        pw.println( "    Maximum Retries : "+_maxRetries ) ;
        pw.println( "    Pool Ping Timer : "+(_checkFilePingTimer/1000) + " seconds" ) ;
        pw.println( "           On Error : "+_onError ) ;
        pw.println( "       Warning Path : "+_warningPath ) ;
        pw.println( "          Allow p2p : "+( def._p2pAllowed ? "on" : "off" )+
                                           " oncost="+( def._p2pOnCost ? "on" : "off" )+
                                           " fortransfer="+( def._p2pForTransfer ? "on" : "off" ) );
        pw.println( "      Allow staging : "+(def._hasHsmBackend ? "on":"off") ) ;
        pw.println( "Allow stage on cost : "+(def._stageOnCost ? "on":"off") ) ;
        pw.println( "          P2p Slope : "+(float)def._slope ) ;
        pw.println( "     P2p Max Copies : "+def._maxPnfsFileCopies) ;
        pw.println( "          Cost Cuts : idle="+def._minCostCut+",p2p="+def._costCut+
                                        ",alert="+def._alertCostCut+",halt="+def._panicCostCut+
                                        ",fallback="+def._fallbackCostCut) ;
        pw.println( "      Restore Limit : "+(_maxRestore<0?"unlimited":(""+_maxRestore)));
        pw.println( "   Restore Exceeded : "+_restoreExceeded ) ;
        pw.println( "Allow same host p2p : "+getSameHostCopyMode() ) ;
        if( _suspendIncoming )
             pw.println( "   Suspend Incoming : on (not persistant)");
        if( _suspendStaging )
             pw.println( "   Suspend Staging  : on (not persistant)");
     }
     public void dumpSetup( StringBuffer sb ){
        sb.append("#\n# Submodule [rc] : ").append(this.getClass().toString()).append("\n#\n");
        sb.append("rc onerror ").append(_onError).append("\n");
        sb.append("rc set max retries ").append(_maxRetries).append("\n");
        sb.append("rc set retry ").append(_retryTimer/1000).append("\n");
        sb.append("rc set warning path ").append(_warningPath).append("\n");
        sb.append("rc set poolpingtimer ").append(_checkFilePingTimer/1000).append("\n");
        sb.append("rc set max restore ").
           append(_maxRestore<0?"unlimited":(""+_maxRestore)).
           append("\n");
        sb.append("rc set sameHostCopy ").
           append(getSameHostCopyMode()).
           append("\n") ;
        sb.append("rc set max threads ").
           append(_threadPool.getMaxThreadCount()).
           append("\n");
     }
     private String getSameHostCopyMode(){
       PoolManagerParameter def = _partitionManager.getParameterCopyOf() ;
       return def._allowSameHostCopy == PoolManagerParameter.P2P_SAME_HOST_BEST_EFFORT ? "besteffort" :
              def._allowSameHostCopy == PoolManagerParameter.P2P_SAME_HOST_NEVER       ? "never" :
              "notchecked" ;
     }
     public String hh_rc_set_max_threads = "<threadCount> # 0 : no limits" ;
     public String ac_rc_set_max_threads_$_1( Args args ){
        int n = Integer.parseInt(args.argv(0));
        _threadPool.setMaxThreadCount(n);
        return "New max thread count : "+n;
     }
     public String hh_rc_set_sameHostCopy = "never|besteffort|notchecked" ;
     public String ac_rc_set_sameHostCopy_$_1( Args args ){
        String type = args.argv(0) ;
        PoolManagerParameter para = _partitionManager.getDefaultPartitionInfo().getParameter() ;
        synchronized( para ){
           if( type.equals("never") ){
              para._allowSameHostCopy = PoolManagerParameter.P2P_SAME_HOST_NEVER ;
           }else if( type.equals("besteffort") ){
              para._allowSameHostCopy = PoolManagerParameter.P2P_SAME_HOST_BEST_EFFORT ;
           }else if( type.equals("notchecked") ){
              para._allowSameHostCopy = PoolManagerParameter.P2P_SAME_HOST_NOT_CHECKED ;
           }else{
              throw new
              IllegalArgumentException("Value not supported : "+type) ;
           }
        }
        return "" ;
     }
     public String hh_rc_set_max_restore = "<maxNumberOfRestores>" ;
     public String ac_rc_set_max_restore_$_1( Args args ){
        if( args.argv(0).equals("unlimited") ){
           _maxRestore = -1 ;
           return "" ;
        }
        int n = Integer.parseInt(args.argv(0));
        if( n < 0 )
          throw new
          IllegalArgumentException("must be >=0") ;
        _maxRestore = n ;
        return "" ;
     }
     public String hh_rc_select = "[<pnfsId> [<errorNumber> [<errorMessage>]] [-remove]]" ;
     public String ac_rc_select_$_0_3( Args args ){
 
        synchronized( _selections ){
           if( args.argc() == 0 ){
              StringBuilder sb = new StringBuilder() ;
              for( Map.Entry<PnfsId, CacheException > entry: _selections.entrySet() ){
 
                 sb.append(entry.getKey().toString()).
                    append("  ").
                    append(entry.getValue().toString()).
                    append("\n");
              }
              return sb.toString() ;
           }
           boolean remove = args.getOpt("remove") != null ;
           PnfsId  pnfsId = new PnfsId(args.argv(0));
 
           if( remove ){
              _selections.remove( pnfsId ) ;
              return "" ;
           }
           int    errorNumber  = args.argc() > 1 ? Integer.parseInt(args.argv(1)) : 1 ;
           String errorMessage = args.argc() > 2 ? args.argv(2) : ("Failed-"+errorNumber);
 
           _selections.put( pnfsId , new CacheException(errorNumber,errorMessage) ) ;
        }
        return "" ;
     }
     public String hh_rc_set_warning_path = " # where to send the warnings to" ;
     public String ac_rc_set_warning_path_$_0_1( Args args ){
        if( args.argc() > 0 ){
           _warningPath = args.argv(0) ;
        }
        return _warningPath ;
     }
     public String fh_rc_set_poolpingtimer =
     " rc set poolpingtimer <timer/seconds> "+
     ""+
     "    If set to a nonezero value, the restore handler will frequently"+
     "    check the pool whether the request is still pending, failed"+
     "    or has been successful" +
     "";
     public String hh_rc_set_poolpingtimer = "<checkPoolFileTimer/seconds>" ;
     public String ac_rc_set_poolpingtimer_$_1(Args args ){
        _checkFilePingTimer = 1000L * Long.parseLong(args.argv(0));
        return "" ;
     }
     public String hh_rc_set_retry = "<retryTimer/seconds>" ;
     public String ac_rc_set_retry_$_1(Args args ){
        _retryTimer = 1000L * Long.parseLong(args.argv(0));
        return "" ;
     }
     public String hh_rc_set_max_retries = "<maxNumberOfRetries>" ;
     public String ac_rc_set_max_retries_$_1(Args args ){
        _maxRetries = Integer.parseInt(args.argv(0));
        return "" ;
     }
     public String hh_rc_suspend = "[on|off] -all" ;
     public String ac_rc_suspend_$_0_1( Args args ){
        boolean all = args.getOpt("all") != null ;
        if( args.argc() == 0 ){
           if(all)_suspendIncoming = true ;
           _suspendStaging = true ;
        }else{
 
           String mode = args.argv(0) ;
           if( mode.equals("on") ){
               if(all)_suspendIncoming = true ;
               _suspendStaging = true ;
           }else if( mode.equals("off") ){
               if(all)_suspendIncoming = false ;
               _suspendStaging = false ;
           }else{
               throw new
               IllegalArgumentException("Usage : rc suspend [on|off]");
           }
 
        }
        return "" ;
     }
     public String hh_rc_onerror = "suspend|fail" ;
     public String ac_rc_onerror_$_1(Args args ){
        String onerror = args.argv(0) ;
        if( ( ! onerror.equals("suspend") ) &&
            ( ! onerror.equals("fail") )  )
              throw new
              IllegalArgumentException("Usage : rc onerror fail|suspend") ;
 
        _onError = onerror ;
        return "onerror "+_onError ;
     }
     public String fh_rc_retry =
        "NAME\n"+
        "           rc retry\n\n"+
        "SYNOPSIS\n"+
        "           I)  rc retry <pnfsId> [OPTIONS]\n"+
        "           II) rc retry * -force-all [OPTIONS]\n\n"+
        "DESCRIPTION\n"+
        "           Forces a 'restore request' to be retried.\n"+
        "           While  using syntax I , a single request  is retried,\n"+
        "           syntax II retries all requests which reported an error.\n"+
        "           If the '-force-all' options is given, all requests are\n"+
        "           retried, regardless of their current status.\n\n"+
        "           -update-si\n"+
        "                   fetch the storage info again before performing\n"+
        "                   the retry. \n"+
        "\n" ;
     public String hh_rc_retry = "<pnfsId>|* -force-all -update-si" ;
     public String ac_rc_retry_$_1( Args args ) throws CacheException {
        StringBuffer sb = new StringBuffer() ;
        boolean forceAll = args.getOpt("force-all") != null ;
        boolean updateSi = args.getOpt("update-si") != null ;
        if( args.argv(0).equals("*") ){
           ArrayList all = null ;
           //
           // Remember : we are not allowed to call 'retry' as long
           // as we  are holding the _handlerHash lock.
           //
           synchronized( _handlerHash ){
              all = new ArrayList( _handlerHash.values() ) ;
           }
           Iterator it = all.iterator() ;
           while( it.hasNext() ){
              try{
                 PoolRequestHandler rph = (PoolRequestHandler)it.next() ;
                 if( forceAll || ( rph._currentRc != 0 ) )rph.retry(updateSi) ;
              }catch(Exception ee){
                 sb.append(ee.getMessage()).append("\n");
              }
           }
        }else{
           PoolRequestHandler rph = null ;
           synchronized( _handlerHash ){
              rph = (PoolRequestHandler)_handlerHash.get(args.argv(0));
              if( rph == null )
                 throw new
                 IllegalArgumentException("Not found : "+args.argv(0) ) ;
           }
           rph.retry(updateSi) ;
        }
        return sb.toString() ;
     }
     public String hh_rc_failed = "<pnfsId> [<errorNumber> [<errorMessage>]]" ;
     public String ac_rc_failed_$_1_3( Args args ) throws CacheException {
        int    errorNumber = args.argc() > 1 ? Integer.parseInt(args.argv(1)) : 1;
        String errorString = args.argc() > 2 ? args.argv(2) : "Operator Intervention" ;
 
        PoolRequestHandler rph = null ;
 
        synchronized( _handlerHash ){
           rph = _handlerHash.get(args.argv(0));
           if( rph == null )
              throw new
              IllegalArgumentException("Not found : "+args.argv(0) ) ;
        }
        rph.failed(errorNumber,errorString) ;
        return "" ;
     }
     public String hh_rc_destroy = "<pnfsId> # !!!  use with care" ;
     public String ac_rc_destroy_$_1( Args args ) throws CacheException {
 
        PoolRequestHandler rph = null ;
 
        synchronized( _handlerHash ){
           rph = _handlerHash.get(args.argv(0));
           if( rph == null )
              throw new
              IllegalArgumentException("Not found : "+args.argv(0) ) ;
 
           _handlerHash.remove( args.argv(0) ) ;
        }
        return "" ;
     }
     public String hh_rc_ls = " [<regularExpression>] [-w] # lists pending requests" ;
     public String ac_rc_ls_$_0_1( Args args ){
        StringBuilder sb  = new StringBuilder() ;
 
        Pattern  pattern = args.argc() > 0 ? Pattern.compile(args.argv(0)) : null ;
 
        if( args.getOpt("w") == null ){
           List<PoolRequestHandler>    allRequestHandlers = null ;
           synchronized( _handlerHash ){
               allRequestHandlers = new ArrayList<PoolRequestHandler>( _handlerHash.values() ) ;
           }
 
           for( PoolRequestHandler h : allRequestHandlers ){
 
               if( h == null )continue ;
               String line = h.toString() ;
               if( ( pattern == null ) || pattern.matcher(line).matches() )
                  sb.append(line).append("\n");
           }
        }else{
 
            Map<UOID, PoolRequestHandler>  allPendingRequestHandlers   = new HashMap<UOID, PoolRequestHandler>() ;
           synchronized(_messageHash){
               allPendingRequestHandlers.putAll( _messageHash ) ;
           }
 
           for (Map.Entry<UOID, PoolRequestHandler> requestHandler : allPendingRequestHandlers.entrySet()) {
 
                 UOID uoid = requestHandler.getKey();
                 PoolRequestHandler h = requestHandler.getValue();
 
                 if (h == null)
                     continue;
                 String line = uoid.toString() + " " + h.toString();
                 if ((pattern == null) || pattern.matcher(line).matches())
                     sb.append(line).append("\n");
 
             }
         }
        return sb.toString();
     }
     public String hh_xrc_ls = " # lists pending requests (binary)" ;
     public Object ac_xrc_ls( Args args ){
 
        List<PoolRequestHandler> all  = null ;
        synchronized( _handlerHash ){
           all = new ArrayList<PoolRequestHandler>( _handlerHash.values() ) ;
        }
 
        List<RestoreHandlerInfo>          list = new ArrayList<RestoreHandlerInfo>() ;
 
        for( PoolRequestHandler h: all  ){
           if( h  == null )continue ;
           list.add( h.getRestoreHandlerInfo() ) ;
        }
        return list.toArray( new RestoreHandlerInfo[list.size()] ) ;
     }
     private static int __requestCounter = 1 ;
     public void addRequest( CellMessage message ){
 
         boolean enforceP2P = false ;
 
         PoolMgrSelectPoolMsg request =
            (PoolMgrSelectPoolMsg)message.getMessageObject() ;
 
         PnfsId       pnfsId       = request.getPnfsId() ;
         ProtocolInfo protocolInfo = request.getProtocolInfo() ;
         String  hostName    =
                protocolInfo instanceof IpProtocolInfo ?
                ((IpProtocolInfo)protocolInfo).getHosts()[0] :
                "NoSuchHost" ;
 
         String netName      = _selectionUnit.getNetIdentifier(hostName);
         String protocolNameFromInfo = protocolInfo.getProtocol()+"/"+protocolInfo.getMajorVersion() ;
 
         String protocolName = _selectionUnit.getProtocolUnit( protocolNameFromInfo ) ;
         if( protocolName == null ) {
           throw new
             IllegalArgumentException("Protocol not found : "+protocolNameFromInfo);
         }
 
         if( request instanceof PoolMgrReplicateFileMsg ){
            if( request.isReply() ){
                esay("Unexpected PoolMgrReplicateFileMsg arrived (is a reply)");
                return ;
            }else{
                enforceP2P = true ;
            }
         }
         String canonicalName = pnfsId +"@"+netName+"-"+protocolName+(enforceP2P?"-p2p":"")  ;
         //
         //
         PoolRequestHandler handler = null ;
         say( "Adding request for : "+canonicalName ) ;
         synchronized( _handlerHash ){
            //
            handler = _handlerHash.get(canonicalName);
            if( handler == null ){
               _handlerHash.put(
                      canonicalName ,
                      handler = new PoolRequestHandler( pnfsId , canonicalName) ) ;
            }
            handler.addRequest(message) ;
         }
     }
 
 
     // replicate a file
     public String hh_replicate = " <pnfsid> <client IP>";
     public String ac_replicate_$_2(Args args) {
 
         String commandReply = "Replication initiated...";
 
         try {
 
             PnfsId pnfsId = new PnfsId(args.argv(0));
             PnfsGetStorageInfoMessage getStorageInfo = new PnfsGetStorageInfoMessage(
                     pnfsId);
 
             CellMessage request = new CellMessage(new CellPath("PnfsManager"),
                     getStorageInfo);
 
             request = _cell.sendAndWait(request, 30000);
             if (request == null) {
                 throw new Exception(
                         "Timeout : PnfsManager request for storageInfo of "
                                 + pnfsId);
             }
 
             getStorageInfo = (PnfsGetStorageInfoMessage) request
                     .getMessageObject();
             StorageInfo storageInfo = getStorageInfo.getStorageInfo();
 
             // TODO: call p2p direct
             // send message to yourself
             PoolMgrReplicateFileMsg req = new PoolMgrReplicateFileMsg(pnfsId,
                     storageInfo, new DCapProtocolInfo("DCap", 3, 0,
                             args.argv(1), 2222), storageInfo.getFileSize());
 
             _cell.sendMessage( new CellMessage(new CellPath("PoolManager"), req) );
 
         } catch (Exception ee) {
             commandReply = "P2P failed : " + ee.getMessage();
         }
 
         return commandReply;
     }
 
 
 
 
     public void say(String message){
        _cell.say(message) ;
     }
     public void esay(String message){
        _cell.esay(message) ;
     }
     public void esay(Throwable e){
        _cell.esay(e) ;
     }
     private static final String [] ST_STRINGS = {
 
         "Init" , "Done" , "Pool2Pool" , "Staging" , "Waiting" ,
         "WaitingForStaging" , "WaitingForP2P" , "Suspended"
     } ;
 
     ///////////////////////////////////////////////////////////////
     //
     // the read io request handler
     //
     private class PoolRequestHandler  {
 
         protected PnfsId       _pnfsId;
         protected final List<CellMessage>    _messages = new ArrayList<CellMessage>() ;
         protected int          _retryCounter = -1 ;
         private   String       _poolCandidate = null ;
         private   String    _p2pPoolCandidate = null ;
         private   String    _p2pSourcePool    = null ;
 
         private   UOID         _waitingFor    = null ;
         private   long         _waitUntil     = 0 ;
 
         private   String       _state         = "[<idle>]";
         private   int          _mode          = ST_INIT ;
         private   int   _emergencyLoopCounter = 0 ;
         private   int          _currentRc     = 0 ;
         private   String       _currentRm     = "" ;
         private   PoolCostCheckable _bestPool = null ;
         private   long         _started       = System.currentTimeMillis() ;
         private   String       _name          = null ;
 
         private   StorageInfo  _storageInfo   = null ;
         private   ProtocolInfo _protocolInfo  = null ;
 
         private   boolean _enforceP2P            = false ;
         private   int     _destinationFileStatus = Pool2PoolTransferMsg.UNDETERMINED ;
 
         private CheckFilePingHandler  _pingHandler = new CheckFilePingHandler(_checkFilePingTimer) ;
 
         private PoolMonitorV5.PnfsFileLocation _pnfsFileLocation  = null ;
         private PoolManagerParameter           _parameter         = _partitionManager.getParameterCopyOf() ;
 
         private class CheckFilePingHandler {
            private   long         _timeInterval = 0 ;
            private   long         _timer        = 0 ;
            private   long         _lastPing     = 0 ;
            private   String       _candidate    = null ;
            private   UOID         _waitingFor   = null ;
 
            private CheckFilePingHandler( long timerInterval ){
               _timeInterval = timerInterval ;
            }
            private void start( String candidate ){
               if( _timeInterval <= 0L )return ;
               _candidate = candidate ;
               _timer = _timeInterval + System.currentTimeMillis() ;
            }
            private void stop(){
               _candidate = null ;
               synchronized( _messageHash ){
                  if( _waitingFor != null )_messageHash.remove( _waitingFor ) ;
               }
            }
            private void alive(){
              say("CheckFilePingHandler : alive called");
              if( ( _candidate == null ) || ( _timer == 0L ) )return ;
 
              long now = System.currentTimeMillis() ;
              say("CheckFilePingHandler : checking alive timer");
              if( now > _timer ){
                 say("CheckFilePingHandler : sending ping to "+_candidate);
                 sendPingMessage() ;
                 _timer = _timeInterval + now ;
              }
 
 
            }
            private void sendPingMessage(){
 	     CellMessage cellMessage = new CellMessage(
                                  new CellPath( _candidate ),
 	                         new PoolCheckFileMessage(
                                          _candidate,
                                          _pnfsId          )
                                  );
              synchronized( _messageHash ){
                 try{
                   _cell.sendMessage( cellMessage );
                   _waitingFor = cellMessage.getUOID() ;
                   _messageHash.put( _waitingFor , PoolRequestHandler.this ) ;
                 }catch(Exception ee ){
                     esay("Can't send pool ping to "+_candidate + " :");
                     esay(ee);
                 }
              }
            }
            private int replyArrived( PoolCheckFileMessage message ){
               return 0 ;
            }
         }
 
         public PoolRequestHandler( PnfsId pnfsId , String canonicalName ){
 
 
 	    _pnfsId  = pnfsId ;
 	    _name    = canonicalName ;
 	}
         //...........................................................
         //
         // the following methods can be called from outside
         // at any time.
         //...........................................................
         //
         // add request is assumed to be synchronized by a higher level.
         //
         public void addRequest( CellMessage message ){
 
            _messages.add(message);
 
            if( _messages.size() > 1 )return ;
 
            PoolMgrSelectReadPoolMsg request =
                 (PoolMgrSelectReadPoolMsg)message.getMessageObject() ;
 
            _storageInfo  = request.getStorageInfo() ;
            _protocolInfo = request.getProtocolInfo() ;
 
            if( request instanceof PoolMgrReplicateFileMsg ){
               _enforceP2P            = true ;
               _destinationFileStatus = ((PoolMgrReplicateFileMsg)request).getDestinationFileStatus() ;
            }
 
            _pnfsFileLocation =
                 _poolMonitor.getPnfsFileLocation( _pnfsId ,
                                                   _storageInfo ,
                                                   _protocolInfo, request.getLinkGroup()) ;
 
            //
            //
            //
            add(null) ;
         }
         public String getPoolCandidate(){
           return _poolCandidate == null?
                  ( _p2pPoolCandidate == null ? "<unknown>" : _p2pPoolCandidate ) :
                  _poolCandidate  ;
         }
         private String getPoolCandidateState(){
           return
            _poolCandidate != null ?
            _poolCandidate :
            _p2pPoolCandidate != null ? ( _p2pSourcePool + "->" + _p2pPoolCandidate ) :
            "<unknown>" ;
         }
 	public RestoreHandlerInfo getRestoreHandlerInfo(){
 	   return new RestoreHandlerInfo(
 	          _name,
 		  _messages.size(),
 		  _retryCounter ,
                   _started ,
 		  getPoolCandidateState() ,
 		  _state ,
 		  _currentRc ,
 		  _currentRm ) ;
 	}
         public String toString(){
            return _name+" m="+_messages.size()+" r="+
                   _retryCounter+" ["+getPoolCandidateState()+"] ["+_state+"] "+
                   "{"+_currentRc+","+_currentRm+"}" ;
         }
         //
         //
         private void mailForYou( Object message ){
            //
            // !!!!!!!!! remove this
            //
            //if( message instanceof PoolFetchFileMessage ){
            //    say("mailForYou !!!!! reply ignored ") ;
            //    return ;
            //}
            add( message ) ;
         }
         private void alive(){
 
            Object [] command = new Object[1] ;
            command[0] = "alive" ;
 
            add( command ) ;
 
         }
         private void retry(boolean updateSi) throws CacheException {
            Object [] command = new Object[2] ;
            command[0] = "retry" ;
            command[1] = updateSi ? "update" : "" ;
            _pnfsFileLocation.clear() ;
            add( command ) ;
         }
         private void failed( int errorNumber , String errorMessage )
                 throws CacheException {
 
            if( errorNumber > 0 ){
               Object [] command = new Object[3] ;
               command[0] = "failed" ;
               command[1] = Integer.valueOf(errorNumber) ;
               command[2] = errorMessage == null ?
                            ( "Error-"+_currentRc ) :
                            errorMessage ;
 
 
               add( command ) ;
               return ;
            }
            throw new
            IllegalArgumentException("Error number must be > 0");
 
         }
         //...................................................................
         //
         // from now on, methods can only be called from within
         // the state mechanism. (which is thread save because
         // we only allow to run a single thread at a time.
         //
         public void say(String message){
            _cell.say(_pnfsId.toString()+" : "+message) ;
         }
         public void esay(String message){
            _cell.esay(_pnfsId.toString()+" : "+message) ;
         }
         public void esay(Exception e){
            _cell.esay(e) ;
         }
         private void waitFor( long millis ){
            _waitUntil = System.currentTimeMillis() + millis ;
         }
         private void clearSteering(){
            synchronized( _messageHash ){
 
               if( _waitingFor != null )_messageHash.remove( _waitingFor ) ;
            }
            _waitingFor = null ;
            _waitUntil  = 0L ;
 
            //
            // and the ping handler
            //
            _pingHandler.stop() ;
 
 
         }
         private void setError( int errorCode , String errorMessage ){
            _currentRc = errorCode ;
            _currentRm = errorMessage ;
         }
 	private boolean sendFetchRequest( String poolName , StorageInfo storageInfo )
                 throws Exception {
 
 	    CellMessage cellMessage = new CellMessage(
                                 new CellPath( poolName ),
 	                        new PoolFetchFileMessage(
                                         poolName,
                                         storageInfo,
                                         _pnfsId          )
                                 );
             synchronized( _messageHash ){
                 if( ( _maxRestore >=0 ) &&
                     ( _messageHash.size() >= _maxRestore ) )return false ;
                 _cell.sendMessage( cellMessage );
                 _poolMonitor.messageToCostModule( cellMessage ) ;
                 _messageHash.put( _waitingFor = cellMessage.getUOID() , this ) ;
                 _state = "Staging "+_formatter.format(new Date()) ;
             }
             return true ;
 	}
 	private void sendPool2PoolRequest( String sourcePool , String destPool )
                 throws Exception {
 
             Pool2PoolTransferMsg pool2pool =
                   new Pool2PoolTransferMsg(sourcePool,destPool,_pnfsId,_storageInfo) ;
             pool2pool.setDestinationFileStatus( _destinationFileStatus ) ;
             say("Sending pool2pool request : "+pool2pool);
 	    CellMessage cellMessage =
                 new CellMessage(
                                   new CellPath( destPool ),
                                   pool2pool
                                 );
 
             synchronized( _messageHash ){
                 _cell.sendMessage( cellMessage );
                 _poolMonitor.messageToCostModule( cellMessage ) ;
                 if( _waitingFor != null )_messageHash.remove( _waitingFor ) ;
                 _messageHash.put( _waitingFor = cellMessage.getUOID() , this ) ;
                 _state = "[P2P "+_formatter.format(new Date())+"]" ;
             }
 	}
         private boolean answerRequest( int count ){
            //
            //  if there is an error we won't continue ;
            //
            if( _currentRc != 0 )count = 100000 ;
            //
            Iterator messages = _messages.iterator() ;
            for( int i = 0 ; ( i < count ) &&  messages.hasNext()  ; i++ ){
               CellMessage m = (CellMessage)messages.next() ;
               PoolMgrSelectPoolMsg rpm =
                  (PoolMgrSelectPoolMsg)m.getMessageObject() ;
               if( _currentRc == 0 ){
 	         rpm.setPoolName(_poolCandidate);
 	         rpm.setSucceeded();
               }else{
                  rpm.setFailed( _currentRc , _currentRm ) ;
               }
 	      try{
                   m.revertDirection();
                   _cell.sendMessage(m);
                   _poolMonitor.messageToCostModule(m);
 	      }catch (Exception e){
                   esay("Exception requestSucceeded : "+e);
 	          esay(e);
 	      }
               messages.remove();
            }
            return messages.hasNext() ;
         }
         //
         // and the heart ...
         //
         private static final int RT_DONE       = 0 ;
         private static final int RT_OK         = 1 ;
         private static final int RT_FOUND      = 2 ;
         private static final int RT_NOT_FOUND  = 3 ;
         private static final int RT_ERROR      = 4 ;
         private static final int RT_OUT_OF_RESOURCES = 5 ;
         private static final int RT_CONTINUE         = 6 ;
         private static final int RT_COST_EXCEEDED    = 7 ;
         private static final int RT_NOT_PERMITTED    = 8 ;
         private static final int RT_S_COST_EXCEEDED  = 9 ;
         private static final int RT_DELAY  = 10 ;
 
         private static final int ST_INIT        = 0 ;
         private static final int ST_DONE        = 1 ;
         private static final int ST_POOL_2_POOL = 2 ;
         private static final int ST_STAGE       = 3 ;
         private static final int ST_WAITING     = 4 ;
         private static final int ST_WAITING_FOR_STAGING     = 5 ;
         private static final int ST_WAITING_FOR_POOL_2_POOL = 6 ;
         private static final int ST_SUSPENDED   = 7 ;
 
         private static final int CONTINUE        = 0 ;
         private static final int WAIT            = 1 ;
 
         private LinkedList _fifo              = new LinkedList() ;
         private boolean    _stateEngineActive = false ;
         private boolean    _forceContinue     = false ;
         private boolean    _overwriteCost     = false ;
 
         public class RunEngine implements ExtendedRunnable {
            public void run(){
               try{
                  stateLoop() ;
               }finally{
                  synchronized( _fifo ){
                    _stateEngineActive = false ;
                  }
               }
            }
            public void runFailed(){
               synchronized( _fifo ){
                    _stateEngineActive = false ;
               }
            }
            public String toString() {
               return PoolRequestHandler.this.toString();
            }
         }
         private void add( Object obj ){
 
            synchronized( _fifo ){
                say( "Adding Object : "+obj ) ;
                _fifo.addFirst(obj) ;
                if( _stateEngineActive )return ;
                say( "Starting Engine" ) ;
                _stateEngineActive = true ;
 
                _threadPool.invokeLater( new RunEngine() , "Read-"+_pnfsId ) ;
            }
         }
         private void stateLoop(){
 
            Object inputObject ;
            say( "ACTIVATING STATE ENGINE "+_pnfsId+" "+(System.currentTimeMillis()-_started)) ;
 
            while( ! Thread.interrupted() ){
 
               if( ! _forceContinue ){
 
                  synchronized( _fifo ){
                     if( _fifo.size() == 0 ){
                        _stateEngineActive = false ;
                        return ;
                     }
                     inputObject = _fifo.removeLast() ;
                  }
               }else{
                  inputObject = null ;
               }
               _forceContinue = false ;
               try{
                  say("StageEngine called in mode "+
                      ST_STRINGS[_mode]+
                      " with object "+
                         (  inputObject == null ?
                              "(NULL)":
                             (  inputObject instanceof Object [] ?
                                  ((Object[])inputObject)[0].toString() :
                                  inputObject.getClass().getName()
                             )
                         )
                     );
 
                  stateEngine( inputObject ) ;
 
                  say("StageEngine left with   : "+ST_STRINGS[_mode]+
                      "  ("+ ( _forceContinue?"Continue":"Wait")+")");
 
               }catch(Exception ee ){
                  esay("Unexpected Exception in state loop for "+_pnfsId+" : "+ee) ;
                  esay(ee);
               }
            }
         }
 
         private void nextStep( int mode , int shouldContinue ){
            _mode = mode ;
            _forceContinue = shouldContinue == CONTINUE ;
            if( _mode != ST_DONE ){
               _currentRc = 0 ;
               _currentRm = "" ;
            }
         }
         //
         //  askIfAvailable :
         //
         //      default : (bestPool=set,overwriteCost=false) otherwise mentioned
         //
         //      RT_FOUND :
         //
         //         Because : file is on pool which is allowed and has reasonable cost.
         //
         //         -> DONE
         //
         //      RT_NOT_FOUND :
         //
         //         Because : file is not in cache at all
         //
         //         (bestPool=0)
         //
         //         -> _hasHsmBackend : STAGE
         //              else         : Suspended (1010, pool unavailable)
         //
         //      RT_NOT_PERMITTED :
         //
         //         Because : file not in an permitted pool but somewhere else
         //
         //         (bestPool=0,overwriteCost=true)
         //
         //         -> _p2pAllowed ||
         //            ! _hasHsmBackend  : P2P
         //            else              : STAGE
         //
         //      RT_COST_EXCEEDED :
         //
         //         Because : file is in permitted pools but cost is too high.
         //
         //         -> _p2pOnCost          : P2P
         //            _hasHsmBackend &&
         //            _stageOnCost        : STAGE
         //            else                : 127 , "Cost exceeded (st,p2p not allowed)"
         //
         //      RT_ERROR :
         //
         //         Because : - No entry in configuration Permission Matrix
         //                   - Code Exception
         //
         //         (bestPool=0)
         //
         //         -> STAGE
         //
         //
         //
         //  askForPoolToPool( overwriteCost ) :
         //
         //      RT_FOUND :
         //
         //         Because : source and destination pool found and cost ok.
         //
         //         -> DONE
         //
         //      RT_NOT_PERMITTED :
         //
         //         Because : - already too many copies (_maxPnfsFileCopies)
         //                   - file already everywhere (no destination found)
         //                   - SAME_HOST_NEVER : but no valid combination found
         //
         //         -> DONE 'using bestPool'
         //
         //      RT_S_COST_EXCEEDED (only if ! overwriteCost ) :
         //
         //         Because : best source pool exceeds 'alert' cost.
         //
         //         -> _hasHsmBackend &&
         //            _stageOnCost    : STAGE
         //            bestPool == 0   : 194,"File not present in any reasonable pool"
         //            else            : DONE 'using bestPool'
         //
         //      RT_COST_EXCEEDED (only if ! overwriteCost )  :
         //
         //         Because : file is in permitted pools but cost of
         //                   best destination pool exceeds cost of best
         //                   source pool (resp. slope * source).
         //
         //         -> _bestPool == 0 : 192,"File not present in any reasonable pool"
         //            else           : DONE 'using bestPool'
         //
         //      RT_ERROR :
         //
         //         Because : - no source pool (code problem)
         //                   - Code Exception
         //
         //         -> 132,"PANIC : Tried to do p2p, but source was empty"
         //                or exception text.
         //
         //  askForStaging :
         //
         //      RT_FOUND :
         //
         //         Because : destination pool found and cost ok.
         //
         //         -> DONE
         //
         //      RT_NOT_FOUND :
         //
         //         -> 149 , "No pool candidates available or configured for 'staging'"
         //         -> 150 , "No cheap candidates available for 'staging'"
         //
         //      RT_ERROR :
         //
         //         Because : - Code Exception
         //
         private void stateEngine( Object inputObject ) throws Exception {
            int rc = -1;
            switch( _mode ){
 
               case ST_INIT :
 
                  synchronized( _selections ){
 
                     CacheException ce = _selections.get(_pnfsId) ;
                     if( ce != null ){
                        setError(ce.getRc(),ce.getMessage());
                        nextStep( ST_DONE , CONTINUE ) ;
                        return ;
                     }
 
                  }
 
 
                  if( inputObject == null ){
 
 
                     if( _suspendIncoming ){
                           _state = "Suspended (forced) "+_formatter.format(new Date()) ;
                           _currentRc = 1005 ;
                           _currentRm = "Suspend enforced";
                          nextStep( ST_SUSPENDED , WAIT ) ;
                          sendInfoMessage( _pnfsId , _storageInfo ,
                                           _currentRc , "Suspended (forced) "+_currentRm );
                          return ;
                     }
                     _retryCounter ++ ;
                     _pnfsFileLocation.clear() ;
                     //
                     //
                     if( _enforceP2P ){
                         setError(0,"");
                         nextStep(ST_POOL_2_POOL , CONTINUE) ;
                         return ;
                     }
 
                     if( ( rc = askIfAvailable() ) == RT_FOUND ){
 
                        setError(0,"");
                        nextStep( ST_DONE , CONTINUE ) ;
                        say("AskIfAvailable found the object");
                        if (_sendHitInfo ) sendHitMsg(  _pnfsId, (_bestPool!=null)?_bestPool.getPoolName():"<UNKNOWN>", true );   //VP
 
                     }else if( rc == RT_NOT_FOUND ){
                        //
                        //
                        if( _parameter._hasHsmBackend ){
                           nextStep( ST_STAGE , CONTINUE ) ;
                        }else{
                           _state = "Suspended (pool unavailable) "+_formatter.format(new Date()) ;
                           _currentRc = 1010 ;
                           _currentRm = "Suspend";
                           _poolCandidate = "<unknown>" ;
                           nextStep( ST_SUSPENDED , WAIT ) ;
                        }
                        if (_sendHitInfo && _poolCandidate==null)
                            sendHitMsg(  _pnfsId, (_bestPool!=null)?_bestPool.getPoolName():"<UNKNOWN>", false );   //VP
                        //
                     }else if( rc == RT_NOT_PERMITTED ){
                        //
                        //  if we can't read the file because 'read is prohibited'
                        //  we at least must give dCache the chance to copy it
                        //  to another pool (not regarding the cost).
                        //
                        _overwriteCost = true ;
                        //
                        //  if we don't have an hsm we overwrite the p2pAllowed
                        //
                        nextStep( _parameter._p2pAllowed || ! _parameter._hasHsmBackend
                                 ? ST_POOL_2_POOL : ST_STAGE , CONTINUE ) ;
 
                     }else if( rc == RT_COST_EXCEEDED ){
 
                        if( _parameter._p2pOnCost ){
 
                            nextStep( ST_POOL_2_POOL , CONTINUE ) ;
 
                        }else if( _parameter._hasHsmBackend &&  _parameter._stageOnCost ){
 
                            nextStep( ST_STAGE , CONTINUE ) ;
 
                        }else{
 
                            setError( 127 , "Cost exceeded (st,p2p not allowed)" ) ;
                            nextStep( ST_DONE , CONTINUE ) ;
 
                        }
                     }else if( rc == RT_ERROR ){
 
                        nextStep( ST_STAGE , CONTINUE ) ;
                        say("AskIfAvailable returned an error, will continue with Staging");
 
                     }
 
                  }else if( inputObject instanceof Object [] ){
 
                       handleCommandObject( (Object [] ) inputObject ) ;
 
                  }
 
               break ;
 
               case ST_POOL_2_POOL :
               {
 
                  if( inputObject == null ){
 
                     if( ( rc = askForPoolToPool( _overwriteCost ) ) == RT_FOUND ){
 
                        nextStep( ST_WAITING_FOR_POOL_2_POOL , WAIT ) ;
                        _state = "Pool2Pool "+_formatter.format(new Date()) ;
                        setError(0,"");
                        _pingHandler.start(_p2pPoolCandidate) ;
 
                        if (_sendHitInfo ) sendHitMsg(  _pnfsId, (_p2pSourcePool!=null)?_p2pSourcePool:"<UNKNOWN>", true );   //VP
 
                     }else if( rc == RT_NOT_PERMITTED ){
 
                         if( _bestPool == null) {
                             if( _enforceP2P ){
                                nextStep( ST_DONE , CONTINUE ) ;
                             }else if( _parameter._hasHsmBackend && _storageInfo.isStored() ){
                                say("ST_POOL_2_POOL : Pool to pool not permitted, trying to stage the file");
                                nextStep( ST_STAGE , CONTINUE ) ;
                             }else{
                                setError(265,"Pool to pool not permitted");
                                nextStep( ST_SUSPENDED , WAIT ) ;
                             }
                         }else{
                           _poolCandidate = _bestPool.getPoolName() ;
                            say("ST_POOL_2_POOL : chosing hight cost pool "+_poolCandidate);
                           if( _sendCostInfo )sendCostMsg(_pnfsId, _bestPool , false);
 
                           setError(0,"");
                           nextStep( ST_DONE , CONTINUE ) ;
                         }
 
                     }else if( rc == RT_S_COST_EXCEEDED ){
 
                        say("ST_POOL_2_POOL : RT_S_COST_EXCEEDED");
 
                        if( _parameter._hasHsmBackend && _parameter._stageOnCost && _storageInfo.isStored() ){
 
                            if( _enforceP2P ){
                               nextStep( ST_DONE , CONTINUE ) ;
                            }else{
                               say("ST_POOL_2_POOL : staging");
                               nextStep( ST_STAGE , CONTINUE ) ;
                            }
                        }else{
 
                           if( _bestPool != null ){
                              _poolCandidate = _bestPool.getPoolName() ;
                               say("ST_POOL_2_POOL : chosing hight cost pool "+_poolCandidate);
                              if( _sendCostInfo )sendCostMsg(_pnfsId, _bestPool , false);
                              setError(0,"");
                              nextStep( ST_DONE , CONTINUE ) ;
                           }else{
                              //
                              // this can't possibly happen
                              //
                              setError(194,"PANIC : File not present in any reasonable pool");
                              nextStep( ST_DONE , CONTINUE ) ;
                           }
 
                        }
                     }else if( rc == RT_COST_EXCEEDED ){
                        //
                        //
                        if( _bestPool == null ){
                           //
                           // this can't possibly happen
                           //
                           if( _enforceP2P ){
                              nextStep( ST_DONE , CONTINUE ) ;
                           }else{
                              setError(192,"PANIC : File not present in any reasonable pool");
                              nextStep( ST_DONE , CONTINUE ) ;
                           }
 
                        }else{
 
                           _poolCandidate = _bestPool.getPoolName() ;
 
                           if( _sendCostInfo )sendCostMsg(_pnfsId, _bestPool , false);
 
                           say(" found high cost object");
 
                           setError(0,"");
                           nextStep( ST_DONE , CONTINUE ) ;
 
                        }
 
 
                     }else{
 
                        if( _enforceP2P ){
                           nextStep( ST_DONE , CONTINUE ) ;
                        }else if( _parameter._hasHsmBackend && _storageInfo.isStored() ){
                           nextStep( ST_STAGE , CONTINUE ) ;
                        }else{
                           nextStep( ST_SUSPENDED , WAIT ) ;
                        }
 
                     }
 
                  }
               }
               break ;
 
               case ST_STAGE :
 
                  if( inputObject == null ){
 
                     if( _suspendStaging ){
                           _state = "Suspended Stage (forced) "+_formatter.format(new Date()) ;
                           _currentRc = 1005 ;
                           _currentRm = "Suspend enforced";
                          nextStep( ST_SUSPENDED , WAIT ) ;
                          sendInfoMessage( _pnfsId , _storageInfo ,
                                           _currentRc , "Suspended Stage (forced) "+_currentRm );
                          return ;
                     }
 
                     if( ( rc = askForStaging() ) == RT_FOUND ){
 
                        nextStep( ST_WAITING_FOR_STAGING , WAIT ) ;
                        _state = "Staging "+_formatter.format(new Date()) ;
                        setError(0,"");
                        _pingHandler.start(_poolCandidate) ;
 
                     }else if( rc == RT_OUT_OF_RESOURCES ){
 
                        _restoreExceeded ++ ;
                        outOfResources("Restore") ;
 
                     }else{
                        //
                        // we coudn't find a pool for staging
                        //
                        errorHandler() ;
                     }
 
                  }
 
               break ;
               case ST_WAITING_FOR_POOL_2_POOL :
 
                  if( inputObject instanceof Message ){
 
                     if( ( rc =  exercisePool2PoolReply((Message)inputObject) ) == RT_OK ){
 
                        nextStep( _parameter._p2pForTransfer && ! _enforceP2P ? ST_INIT : ST_DONE , CONTINUE ) ;
 
                     }else if( rc == RT_CONTINUE ){
                         //
                         //
                     }else{
                         say("ST_POOL_2_POOL : Pool to pool reported a problem");
                         if( _parameter._hasHsmBackend && _storageInfo.isStored() ){
 
                             say("ST_POOL_2_POOL : trying to stage the file");
                             nextStep( ST_STAGE , CONTINUE ) ;
 
                         }else{
                             errorHandler() ;
                         }
 
                     }
 
                  }else if( inputObject instanceof Object [] ){
 
                     handleCommandObject( (Object []) inputObject ) ;
 
                  }else{
                       //
                       // this message was not for us
                       //
                  }
 
               break ;
               case ST_WAITING_FOR_STAGING :
 
                  if( inputObject instanceof Message ){
 
                     if( ( rc =  exerciseStageReply( (Message)inputObject ) ) == RT_OK ){
 
                        nextStep( _parameter._p2pForTransfer ? ST_INIT : ST_DONE , CONTINUE ) ;
 
                     }else if( rc == RT_DELAY ){
                         _state = "Suspended By HSM request";
                         nextStep( ST_SUSPENDED , WAIT ) ;
                     }else if( rc == RT_CONTINUE ){
 
                     }else{
 
                        errorHandler() ;
 
                     }
                  }else if( inputObject instanceof Object [] ){
 
                     handleCommandObject( (Object []) inputObject ) ;
 
                  }else{
 
                  }
               break ;
               case ST_SUSPENDED :
                  if( inputObject instanceof Object [] ){
 
                     handleCommandObject( (Object []) inputObject ) ;
 
                  }
               return ;
 
               case ST_DONE :
 
                  if( inputObject == null ){
 
                     clearSteering();
                     //
                     // it is essential that we are not within any other
                     // lock when trying to get the handlerHash lock.
                     //
                     synchronized( _handlerHash ){
                        if( answerRequest( _maxRequestClumping ) ){
                            nextStep( ST_INIT , CONTINUE ) ;
                        }else{
                            _handlerHash.remove( _name ) ;
                        }
                     }
                  }
 
               return ;
            }
         }
         private void handleCommandObject( Object [] c ){
 
            String command = c[0].toString() ;
            if( command.equals("failed") ){
 
               clearSteering();
               setError(((Integer)c[1]).intValue(),c[2].toString());
               nextStep(ST_DONE,CONTINUE);
 
            }else if( command.equals("retry") ){
 
               _state = "Retry enforced" ;
               _retryCounter = 0 ;
               clearSteering() ;
               _pnfsFileLocation.clear() ;
               setError(0,"");
               if( ( c.length > 1 ) && c[1].toString().equals("update") )getStorageInfo();
               nextStep(ST_INIT,CONTINUE);
 
            }else if( command.equals("alive") ){
 
               long now = System.currentTimeMillis() ;
               if( ( _waitUntil > 0L ) && ( now > _waitUntil ) ){
                  nextStep(ST_INIT,CONTINUE);
                  clearSteering() ;
               }else{
                  _pingHandler.alive() ;
               }
 
            }
 
         }
         private void getStorageInfo(){
            try{
               PnfsGetStorageInfoMessage getStorageInfo = new PnfsGetStorageInfoMessage( _pnfsId ) ;
 
               CellMessage request = new CellMessage(
                                        new CellPath("PnfsManager") ,
                                        getStorageInfo ) ;
 
               request = _cell.sendAndWait( request , 30000 ) ;
               if( request == null )
                  throw new
                  Exception("Timeout : PnfsManager request for storageInfo of "+_pnfsId);
 
               getStorageInfo = (PnfsGetStorageInfoMessage)request.getMessageObject();
 
               _storageInfo = getStorageInfo.getStorageInfo();
 
            }catch(Exception ee ){
               esay("Fetching storage info failed : "+ee);
            }
         }
         private void outOfResources( String detail ){
 
            clearSteering();
            setError(5,"Resource temporarily unavailable : "+detail);
            nextStep( ST_DONE , CONTINUE ) ;
            _state = "Failed" ;
            sendInfoMessage( _pnfsId , _storageInfo ,
                             _currentRc , "Failed "+_currentRm );
         }
         private void errorHandler(){
            if(_retryCounter == 0 ){
               //
               // retry immediately (stager will take another pool)
               //
               _pnfsFileLocation.clear() ;
               getStorageInfo();
               nextStep( ST_INIT, CONTINUE ) ;
               //
            }else if( _retryCounter < _maxRetries ){
               //
               // now retry only after some time
               //
               _pnfsFileLocation.clear() ;
               getStorageInfo();
               waitFor( _retryTimer ) ;
               nextStep( ST_INIT , WAIT ) ;
               _state = "Waiting "+_formatter.format(new Date()) ;
               //
            }else{
               if( _onError.equals( "suspend" ) ){
                  _state = "Suspended "+_formatter.format(new Date()) ;
                  nextStep( ST_SUSPENDED , WAIT ) ;
                  sendInfoMessage( _pnfsId , _storageInfo ,
                                   _currentRc , "Suspended "+_currentRm );
               }else{
                  nextStep( ST_DONE , WAIT ) ;
                  _state = "Failed" ;
                  sendInfoMessage( _pnfsId , _storageInfo ,
                                   _currentRc , "Failed "+_currentRm );
               }
            }
 
         }
         private int exerciseStageReply( Message messageArrived ){
            try{
 
               if( messageArrived instanceof PoolFetchFileMessage ){
                  PoolFetchFileMessage reply = (PoolFetchFileMessage)messageArrived ;
 
                  int rc;
                  _currentRc = reply.getReturnCode();
 
                  switch(_currentRc) {
                      case 0:
                          _poolCandidate = reply.getPoolName() ;
                          rc = RT_OK;
                          break;
                      case CacheException.HSM_DELAY_ERROR:
                          _currentRm = "Suspend by HSM request : " + reply.getErrorObject() == null ?
                                  "No info" : reply.getErrorObject().toString() ;
                          rc = RT_DELAY;
                          break;
                      default:
                          _currentRm = reply.getErrorObject() == null ?
                                  ( "Error="+_currentRc ) : reply.getErrorObject().toString() ;
 
                          rc =  RT_ERROR ;
                  }
 
                  return rc;
 
               }else if( messageArrived instanceof PoolCheckFileMessage ){
                  PoolCheckFileMessage check = (PoolCheckFileMessage)messageArrived ;
                  say("PoolCheckFileMessage arrived with "+check );
                  return check.getWaiting() ? RT_CONTINUE :
                         check.getHave()    ? RT_OK    :
                                              RT_ERROR ;
               }else{
                  throw new
                  CacheException(204,"Invalid message arrived : "+
                                 messageArrived.getClass().getName());
 
               }
            }catch(Exception ee ){
               _currentRc = ee instanceof CacheException ? ((CacheException)ee).getRc() : 102 ;
               _currentRm = ee.getMessage();
               esay("exerciseStageReply : "+ee ) ;
               esay(ee);
               return RT_ERROR ;
            }
         }
         private int exercisePool2PoolReply( Message messageArrived ){
            try{
 
               if( messageArrived instanceof Pool2PoolTransferMsg ){
                  Pool2PoolTransferMsg reply = (Pool2PoolTransferMsg)messageArrived ;
                  say("Pool2PoolTransferMsg replied with : "+reply);
                  if( ( _currentRc = reply.getReturnCode() ) == 0 ){
                     _poolCandidate = _p2pPoolCandidate ;
                     return RT_OK ;
 
                  }else{
 
                     _currentRm = reply.getErrorObject() == null ?
                                  ( "Error="+_currentRc ) : reply.getErrorObject().toString() ;
 
                     return RT_ERROR ;
 
                  }
               }else if( messageArrived instanceof PoolCheckFileMessage ){
                  PoolCheckFileMessage check = (PoolCheckFileMessage)messageArrived ;
                  say("PoolCheckFileMessage arrived with "+check );
                  return check.getWaiting() ? RT_CONTINUE :
                         check.getHave()    ? RT_OK    :
                                              RT_ERROR ;
               }else{
 
                  throw new
                  CacheException(205,"Invalid message arrived : "+
                                 messageArrived.getClass().getName());
 
               }
            }catch(Exception ee ){
               _currentRc = ee instanceof CacheException ? ((CacheException)ee).getRc() : 102 ;
               _currentRm = ee.getMessage();
               esay("exercisePool2PoolReply : "+ee ) ;
               esay(ee);
               return RT_ERROR ;
            }
         }
         //
         //  calculate :
         //       matrix = list of list of active
         //                pools with file available (sorted)
         //
         //  if empty :
         //        bestPool = 0 , return NOT_FOUND
         //
         //  else
         //        determince best pool by
         //
         //        if allowFallback :
         //           first row for which cost < costCut or
         //           if not found, pool with lowest cost.
         //        else
         //           leftmost pool of first nonzero row
         //
         //  if bestPool > costCut :
         //        return COST_EXCEEDED
         //
         //  chose best pool from row selected above by :
         //     if ( minCostCut > 0 ) :
         //         take all pools of the selected row
         //         with cost < minCostCut and make hash selection.
         //     else
         //         take leftmost pool.
         //
         //  return FOUND
         //
         //  RESULT :
         //      RT_FOUND :
         //         file is on pool which is allowed and has reasonable cost.
         //      RT_NOT_FOUND :
         //         file is not in cache at all
         //      RT_NOT_PERMITTED :
         //         file not in an permitted pool but somewhere else
         //      RT_COST_EXCEEDED :
         //         file is in permitted pools but cost is too high.
         //      RT_ERROR :
         //         - No entry in configuration Permission Matrix
         //         - Code Exception
         //
         private int askIfAvailable(){
 
            String err = null ;
            try{
 
               List avMatrix  = _pnfsFileLocation.getFileAvailableMatrix() ;
               int matrixSize = avMatrix.size() ;
               //
               // the DB matrix has no rows, which
               // means that there are no pools which are allowed
               // to serve this request.
               //
               if( ( matrixSize == 0 ) ||
                   ( _pnfsFileLocation.getAllowedPoolCount() == 0 ) ){
 
                   err="Configuration Error : No entries in Permission Matrix for this request" ;
                   setError(130,err) ;
                   esay("askIfAvailable : "+err);
                   return RT_ERROR ;
 
               }
               //
               // we define the top row as the default parameter set for
               // cases where none of the pools hold the file.
               //
               List paraList   = _pnfsFileLocation.getListOfParameter() ;
               _parameter = (PoolManagerParameter)paraList.get(0);
               //
               // The file is not in the dCache at all.
               //
               if( _pnfsFileLocation.getAcknowledgedPnfsPools().size() == 0 ){
                   say("askIfAvailable : file not in pool at all");
                   return RT_NOT_FOUND ;
               }
               //
               // The file is in the cache but not on a pool where
               // we would be allowed to read it from.
               //
               if( _pnfsFileLocation.getAvailablePoolCount()  == 0 ){
                   say("askIfAvailable : file in cache but not in read-allowed pool");
                   return RT_NOT_PERMITTED ;
               }
               //
               // File is at least on one pool from which we could
               // get it. Now we have to find the pool with the
               // best performance cost.
               // Matrix is assumed to be sorted, so we
               // only have to check the leftmost entry
               // in the list (get(0)). Rows could be empty.
               //
               _bestPool       = null ;
               List bestAv     = null ;
               int  validCount = 0 ;
               List tmpList    = new ArrayList() ;
               int  level      = 0 ;
               boolean allowFallbackOnPerformance = false ;
               for( Iterator i = avMatrix.iterator() ; i.hasNext() ; level++ ){
 
                  List av = (List)i.next() ;
 
                  if( av.size() == 0 )continue ;
 
                  validCount++;
                  PoolCostCheckable cost =(PoolCostCheckable)av.get(0);
                  tmpList.add(cost);
 
                  if( ( _bestPool == null ) ||
                      ( _bestPool.getPerformanceCost() > cost.getPerformanceCost() ) ){
 
                     _bestPool = cost ;
                     bestAv    = av ;
 
                  }
                  _parameter = (PoolManagerParameter)paraList.get(level);
                  allowFallbackOnPerformance = _parameter._fallbackCostCut > 0.0 ;
 
                  if( ( ( ! allowFallbackOnPerformance ) &&
                        ( validCount == 1              )    ) ||
                      ( _bestPool.getPerformanceCost() < _parameter._fallbackCostCut ) )break ;
               }
               //
               // this can't happen because we already know that
               // there are pools which contain the files and which
               // are allowed for us.
               //
               if( _bestPool == null )return RT_NOT_FOUND ;
 
               double bestPoolPerformanceCost = _bestPool.getPerformanceCost() ;
               if(   (  _parameter._costCut     > 0.0                  ) &&
                     (  bestPoolPerformanceCost >= _parameter._costCut )       ){
 
                  if( allowFallbackOnPerformance ){
                     //
                     // if all costs are too high, the above list
                     // has been scanned up to the very end. But it
                     // could be that one of the first rows has a
                     // better cost than the last one, so we have
                     // to correct here.
                     //
                     say("askIfAvailable : allowFallback , recalculation best cost");
                     _bestPool = (PoolCostCheckable)
                                 Collections.min(
                                    tmpList ,
                                     _poolMonitor.getCostComparator(false,_parameter)
                                 ) ;
 
                  }
                  say("askIfAvailable : cost exceeded on all available pools, "+
                      "best pool would have been "+_bestPool);
                  return RT_COST_EXCEEDED ;
               }
 
               if( ( _parameter._panicCostCut > 0.0 ) && ( bestPoolPerformanceCost > _parameter._panicCostCut ) ){
                  say("askIfAvailable : cost of best pool exceeds 'panic' level");
                  setError(125,"Cost of best pool exceeds panic level") ;
                  return RT_ERROR ;
               }
               say("askIfAvailable : Found candidates : "+bestAv);
               //
               //  this part is intended to get rid of duplicates if the
               //  load is decreasing.
               //
               PoolCostCheckable cost = null ;
               TreeMap           list = new TreeMap() ;
 
               if( _parameter._minCostCut > 0.0 ){
 
                  for( int i = 0 , n = bestAv.size() ; i < n ; i++ ){
 
                     cost = (PoolCostCheckable)bestAv.get(i) ;
 
                     double costValue = cost.getPerformanceCost() ;
 
                     if( costValue < _parameter._minCostCut ){
                        //
                        // here we sort it arbitrary but reproducable
                        // (whatever that means)
                        //
                        String poolName = cost.getPoolName() ;
                        say("askIfAvailable : "+poolName+" below "+_parameter._minCostCut+" : "+costValue);
                        list.put(
                           Integer.valueOf((_pnfsId.toString()+poolName).hashCode()) ,
                            cost ) ;
                     }
                  }
 
               }
 
               cost = list.size() > 0 ?
                      (PoolCostCheckable)list.get(list.firstKey()) :
                      (PoolCostCheckable)bestAv.get(0) ;
 
               say( "askIfAvailable : candidate : "+cost ) ;
 
 
               if( _sendCostInfo )sendCostMsg( _pnfsId, cost, false );
 
               _poolCandidate = cost.getPoolName() ;
               setError(0,"") ;
 
               return RT_FOUND ;
 
            }catch(Exception ee ){
               esay(err="Exception in getFileAvailableList : "+ee ) ;
               esay(ee);
               setError(130,err) ;
               return RT_ERROR ;
            }finally{
               say( "askIfAvailable : Took  "+(System.currentTimeMillis()-_started));
            }
 
         }
         //
         // Result :
         //    FOUND :
         //        valid source/destination pair found fitting all constraints.
         //    NOT_PERMITTED :
         //        - already too many copies (_maxPnfsFileCopies)
         //        - file already everywhere (no destination found)
         //        - SAME_HOST_NEVER : but no valid combination found
         //    COST_EXCEEDED :
         //        - slope == 0 : all destination pools > costCut (p2p)
         //          else       : (best destination) > ( slope * source )
         //    S_COST_EXCEEDED :
         //        - all source pools > alert
         //    ERROR
         //        - no source pool (code problem)
         //
 		private int askForPoolToPool(boolean overwriteCost) {
 			try {
 				//
 				//
 				List<PoolCheckable> sources = _pnfsFileLocation.getCostSortedAvailable();
 				//
 				// Here we get the parameter set of the 'read'
 				//
 				PoolManagerParameter parameter = _pnfsFileLocation
 						.getCurrentParameterSet();
 
 				if (sources.size() == 0) {
 
 					setError(132,
 							"PANIC : Tried to do p2p, but source was empty");
 					return RT_ERROR;
 
 				} else if (sources.size() >= parameter._maxPnfsFileCopies) {
 					//
 					// already too many copies of the file
 					//
 					say("askForPoolToPool : already too many copies : "
 							+ sources.size());
 					setError(133, "Not replicated : already too many copies : "
 							+ sources.size());
 					return RT_NOT_PERMITTED;
 
 				} else if ((!overwriteCost)
 						&& (parameter._alertCostCut > 0.0)
 						&& (((PoolCostCheckable) sources.get(0))
 								.getPerformanceCost() > parameter._alertCostCut)) {
 					//
 					// all source are too busy
 					//
 					say("askForPoolToPool : all p2p source(s) are too busy (cost > "
 							+ parameter._alertCostCut + ")");
 					setError(134,
 							"Not replicated : all p2p source(s) are too busy (cost > "
 									+ parameter._alertCostCut + ")");
 					return RT_S_COST_EXCEEDED;
 
 				}
 				//
 				// make sure we are either below costCut or
 				// 'slope' below the source pool.
 				//
 				double maxCost = parameter._slope > 0.01 ? (parameter._slope * ((PoolCostCheckable) sources
 						.get(0)).getPerformanceCost())
 						: parameter._costCut;
 
 				List matrix = _pnfsFileLocation.getFetchPoolMatrix("p2p",
 						_storageInfo, _protocolInfo, _storageInfo
 								.getFileSize());
 
 				if (matrix.size() == 0) {
 					setError(
 							136,
 							"Not replicated : No pool candidates available/configured/left for p2p or file already everywhere");
 					say("askForPoolToPool : No pool candidates available/configured/left for p2p or file already everywhere");
 					return RT_NOT_PERMITTED;
 				}
 				//
 				// Here we get the parameter set of the 'p2p'
 				//
 				parameter = _pnfsFileLocation.getCurrentParameterSet();
 
 				List<PoolCheckable> destinations = null;
 
 				for (Iterator it = matrix.iterator(); it.hasNext();) {
 					destinations = (List) it.next();
 					if (destinations.size() > 0)
 						break;
 				}
 
 //				subtract all source pools from the list of destination pools (those pools already have a copy)
 				for (PoolCheckable dest : destinations) {
 					for (PoolCheckable src : sources) {
 						if (dest.getPoolName().equals( src.getPoolName()) ) {
 							say("removing pool "+dest.getPoolName()+" from dest pool list");
 							destinations.remove(dest);
 						}
 					}
 				}
 
 				//
 				if (destinations.size() == 0) {
 					//
 					// file already everywhere
 					//
 					say("askForPoolToPool : file already everywhere");
 					setError(137, "Not replicated : file already everywhere");
 					return RT_NOT_PERMITTED;
 				}
 
 				if ((!overwriteCost) && (maxCost > 0.0)) {
 
 					List<PoolCheckable> selected = new ArrayList<PoolCheckable>();
 					for (PoolCheckable dest : destinations) {
 						PoolCostCheckable cost = (PoolCostCheckable) dest;
 						if (cost.getPerformanceCost() < maxCost)
 							selected.add(cost);
 					}
 					if (selected.size() == 0) {
 						say("askForPoolToPool : All destination pools exceed cost "
 								+ maxCost);
 						setError(137,
 								"Not replicated : All destination pools exceed cost "
 										+ maxCost);
 						return RT_COST_EXCEEDED;
 					}
 
 					destinations = selected;
 				}
 				//
 				// The 'performance cost' of all destination pools is below
 				// maxCost,
 				// and the destination list is sorted according to the
 				// 'full cost'.
 				//
 
 				_pnfsFileLocation.sortByCost(destinations, true);
 
 				//
 				// loop over all source, destination combinations and find the
 				// most appropriate for (source.hostname !=
 				// destination.hostname)
 				//
 				String sourcePool = null;
 				String destinationPool = null;
 				String bestEffortSourcePool = null;
 				String bestEffortDestinationPool = null;
 				Map map = null;
 
 				for (int s = 0, sMax = sources.size(); s < sMax; s++) {
 
 					PoolCheckable sourceCost = sources.get(s);
 
 					String sourceHost = (String) ((map = sourceCost.getTagMap()) == null ? null : map.get("hostname"));
 
 					for (int d = 0, dMax = destinations.size(); d < dMax; d++) {
 
 						PoolCheckable destinationCost = destinations.get(d);
 
 						if (parameter._allowSameHostCopy == PoolManagerParameter.P2P_SAME_HOST_NOT_CHECKED) {
 							// we take the pair with the least cost without
 							// further hostname checking
 							sourcePool = sourceCost.getPoolName();
 							destinationPool = destinationCost.getPoolName();
 							break;
 						}
 
 						// save the pair with the least cost for later reuse
 						if (bestEffortSourcePool == null)
 							bestEffortSourcePool = sourceCost.getPoolName();
 						if (bestEffortDestinationPool == null)
 							bestEffortDestinationPool = destinationCost
 									.getPoolName();
 
 						say("p2p same host checking : "
 								+ sourceCost.getPoolName() + " "
 								+ destinationCost.getPoolName());
 
 						String destinationHost =
 							(String) ((map = destinationCost.getTagMap()) == null ? null : map.get("hostname"));
 
 						if (sourceHost != null && !sourceHost.equals(destinationHost)) {
 							// we take the first src/dest-pool pair not residing on the same host
 							sourcePool = sourceCost.getPoolName();
 							destinationPool = destinationCost.getPoolName();
 							break;
 						}
 					}
 					if (sourcePool != null && destinationPool != null)
 						break;
 				}
 
 				if (sourcePool == null || destinationPool == null) {
 
 //					ok, we could not find a pair on different hosts, what now?
 
 					if (parameter._allowSameHostCopy == PoolManagerParameter.P2P_SAME_HOST_BEST_EFFORT) {
 						say("P2P : sameHostCopy=bestEffort : couldn't find a src/dest-pair on different hosts, choosing pair with the least cost");
 						sourcePool = bestEffortSourcePool;
 						destinationPool = bestEffortDestinationPool;
 
 					} else if (parameter._allowSameHostCopy == PoolManagerParameter.P2P_SAME_HOST_NEVER) {
 						say("P2P : sameHostCopy=never : no matching pool found");
 						setError(137,
 								"Not replicated : sameHostCopy=never : no matching pool found");
 						return RT_NOT_PERMITTED;
 
 					} else {
 						say("P2P : coding error, bad state");
 						setError(137,
 								"Not replicated : coding error, bad state");
 						return RT_NOT_PERMITTED;
 					}
 				}
 
 				_p2pPoolCandidate = destinationPool;
 				say("P2P : source=" + sourcePool + ";dest=" + destinationPool);
 
 				sendPool2PoolRequest(_p2pSourcePool = sourcePool,
 						_p2pPoolCandidate = destinationPool);
 
 				if (_sendCostInfo)
 					sendCostMsg(_pnfsId, (PoolCostCheckable) (destinations
 							.get(0)), true);// VP
 
 				return RT_FOUND;
 
 			} catch (Exception ee) {
 				int rc = ee instanceof CacheException ? ((CacheException) ee)
 						.getRc() : 182;
 				setError(rc, ee.getMessage());
 				esay(ee.toString());
 				if (!(ee instanceof CacheException))
 					_cell.esay(ee);
 				return RT_ERROR;
 			} finally {
 				say("Selection pool 2 pool took : "
 						+ (System.currentTimeMillis() - _started));
 			}
 		}
         private PoolCostCheckable askForFileStoreLocation(
                   String mode ,
                   String excludePool ) throws Exception {
 
 
             //
             // matrix contains cost for original db matrix minus
             // the pools already containing the file.
             //
             List matrix =
                     _pnfsFileLocation.getFetchPoolMatrix (
                                 mode ,
                                 _storageInfo ,
                                 _protocolInfo ,
                                 _storageInfo.getFileSize() ) ;
 
 
             PoolManagerParameter parameter = _pnfsFileLocation.getCurrentParameterSet() ;
 
             if( matrix.size() == 0 )
                   throw new
                   CacheException( 149 , "No pool candidates available/configured/left for "+mode ) ;
 
 
             PoolCostCheckable cost = null ;
             if( excludePool == null ){
                 int n = 0 ;
                 for( Iterator i = matrix.iterator() ; i.hasNext() ; n++ ){
 
                     parameter = (PoolManagerParameter)_pnfsFileLocation.getListOfParameter().get(n) ;
                     List list = (List)i.next() ;
                     cost = (PoolCostCheckable)list.get(0);
                     if( ( parameter._fallbackCostCut  == 0.0 ) ||
                         ( cost.getPerformanceCost() < parameter._fallbackCostCut ) )break ;
 
                  }
             }else{
 
                  say("askFor "+mode+" : Second shot excluding : "+excludePool ) ;
                  //
                  // find a pool which is not identical to the first candidate
                  //
                  for( Iterator i = matrix.iterator() ; i.hasNext() ; ){
 
                     for( Iterator n = ((List)i.next()).iterator() ; n.hasNext() ; ){
 
                        PoolCostCheckable c  = (PoolCostCheckable)n.next() ;
                        //
                        // skip this one if we tried this last time
                        //
                        if( c.getPoolName().equals(_poolCandidate) )continue;
                        //
                        //
                        if( (  parameter._fallbackCostCut == 0.0 ) ||
                            ( c.getPerformanceCost() < parameter._fallbackCostCut ) ){
                            cost = c ;
                        }
                        break ;
                     }
                     if( cost != null )break ;
                  }
 
            }
            _parameter = parameter ;
 
            if( cost == null )
               throw new
               CacheException( 150 , "No cheap candidates available for '"+mode+"'");
 
            return cost ;
        }
 
         //
         //   FOUND :
         //        - pool candidate found
         //   NOT_FOUND :
         //        - no pools configured
         //        - pools configured but not active
         //        - no pools left after substracting primary candidate.
         //   OUT_OF_RESOURCES :
         //        - too many requests queued
         //
         private int askForStaging(){
 
            try{
 
               PoolCostCheckable cost = askForFileStoreLocation( "cache" , _poolCandidate ) ;
 
               _poolCandidate = cost.getPoolName() ;
 
               say( "askForStaging : poolCandidate -> "+_poolCandidate);
 
               if( ! sendFetchRequest( _poolCandidate , _storageInfo ) )return RT_OUT_OF_RESOURCES ;
 
               setError(0,"");
 
               if( _sendCostInfo )sendCostMsg(_pnfsId, cost, true);//VP
 
               return RT_FOUND ;
 
            }catch( CacheException ce ){
 
                setError( ce.getRc() , ce.getMessage() );
                esay( ce.toString() );
 
                return RT_NOT_FOUND ;
 
            }catch( Exception ee ){
 
               setError( 128 , ee.getMessage() );
               esay(ee) ;
 
               return RT_ERROR ;
 
            }finally{
               say( "Selection cache took : "+(System.currentTimeMillis()-_started));
            }
 
        }
 
     }
 
     private void sendInfoMessage( PnfsId pnfsId ,
                                   StorageInfo storageInfo ,
                                   int rc , String infoMessage ){
       try{
         WarningPnfsFileInfoMessage info =
             new WarningPnfsFileInfoMessage(
                                     "PoolManager","PoolManager",pnfsId ,
                                     rc , infoMessage )  ;
             info.setStorageInfo( storageInfo ) ;
 
         _cell.sendMessage(
          new CellMessage( new CellPath(_warningPath), info )
                              ) ;
 
       }catch(Exception ee){
          esay("Coudn't send WarningInfoMessage : "+ee ) ;
       }
     }
 
     //VP
     public void sendCostMsg( PnfsId pnfsId,
                              PoolCostCheckable checkable,
                              boolean useBoth){
        try {
        /*
           PoolCostInfoMessage msg = new PoolCostInfoMessage(checkable.getPoolName(), pnfsId);
           double cost = _poolMonitor.calculateCost(checkable, useBoth);
           msg.setCost(cost);
           _cell.sendMessage(new CellMessage( new CellPath(_warningPath), msg));
         */
        }catch (Exception ee){
           esay("Couldn't report cost for : "+pnfsId+" : "+ee);
        }
     }
 
     private void sendHitMsg(PnfsId pnfsId, String poolName, boolean cached)
     {
         try {
             PoolHitInfoMessage msg = new PoolHitInfoMessage(poolName, pnfsId);
             msg.setFileCached(cached);
             _cell.sendMessage(new CellMessage( new CellPath(_warningPath), msg));
         } catch (Exception ee) {
             esay("Couldn't report hit info for : "+pnfsId+" : "+ee);
         }
     }
 
     public void setSendCostInfo(boolean sendCostInfo) {
         _sendCostInfo = sendCostInfo;
     }
     //VP
 }
