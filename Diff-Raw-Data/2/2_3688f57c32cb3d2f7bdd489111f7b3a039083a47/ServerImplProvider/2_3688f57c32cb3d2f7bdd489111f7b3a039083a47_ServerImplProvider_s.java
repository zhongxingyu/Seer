 package org.devtcg.sqliteserver.impl;
 
 import org.devtcg.sqliteserver.SQLiteServer;
 import org.devtcg.sqliteserver.impl.binder.ServerImpl;
 
 public class ServerImplProvider {
     private final SQLiteServer mBackend;
     private ServerImpl mServerImpl;
 
     public ServerImplProvider(SQLiteServer backend) {
         mBackend = backend;
     }
 
    public ServerImpl get() {
         if (mServerImpl == null) {
             SQLiteExecutor executor = new SQLiteExecutor(mBackend.getWritableDatabase());
             mServerImpl = new ServerImpl(mBackend.getClass().getSimpleName(),
                     executor, mBackend.getServerName());
         }
         return mServerImpl;
     }
 }
